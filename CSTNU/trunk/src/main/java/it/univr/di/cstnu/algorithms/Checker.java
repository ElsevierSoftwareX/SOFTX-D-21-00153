// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.univr.di.cstnu.algorithms.AbstractCSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.AbstractCSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.algorithms.STN.STNCheckStatus;
import it.univr.di.cstnu.algorithms.STNU.CheckAlgorithm;
import it.univr.di.cstnu.algorithms.STNU.STNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.CSTNEdgePluggable;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.CSTNUEdgePluggable;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNEdgeInt;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.STNUEdgeInt;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraph.NetworkType;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

/**
 * Simple class to determine the average execution time (and std dev) of the (C)STN(U) DC checking algorithm on a given set of (C)STN(U)s.
 * It lacks to check STN network because the class is not present yet (2019-06-13).
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class Checker {

	/**
	 * Utility internal class
	 * 
	 * @author posenato
	 */
	public static class GlobalStatisticKey implements Comparable<GlobalStatisticKey> {
		/**
		 * # contingents
		 */
		protected int contingents;
		/**
		 * # of nodes
		 */
		protected int nodes;
		/**
		 * # of propositions
		 */
		protected int propositions;

		/**
		 * default constructor
		 * 
		 * @param inputNodes #nodes
		 * @param inputPropositions #prop
		 * @param inputContingents # cont
		 */
		public GlobalStatisticKey(final int inputNodes, final int inputPropositions, final int inputContingents) {
			this.nodes = inputNodes;
			this.propositions = inputPropositions;
			this.contingents = inputContingents;
		}

		@Override
		public int compareTo(GlobalStatisticKey o) {
			int d = this.nodes - o.nodes;
			if (d == 0) {
				int d1 = this.contingents - o.contingents;
				if (d1 == 0)
					return this.propositions - o.propositions;
				return d1;
			}
			return d;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof GlobalStatisticKey))
				return false;
			return ((GlobalStatisticKey) o).compareTo(this) == 0;
		}

		/**
		 * @return #cont
		 */
		public int getContingent() {
			return this.contingents;
		}

		/**
		 * @return #nodes
		 */
		public int getNodes() {
			return this.nodes;
		}

		/**
		 * @return # prop
		 */
		public int getPropositions() {
			return this.propositions;
		}

		@Override
		public int hashCode() {
			return (this.contingents + this.propositions * 31) * 31 + this.nodes;
		}

	}

	@SuppressWarnings({ "javadoc" })
	static class RunMeter {

		static final int maxMeterSize = 50;// [0--100]

		long current;
		long startTime;
		long total;

		/**
		 * @param inputStartTime in milliseconds
		 * @param inputTotal number of time to show
		 * @param inputCurrent number of time to show
		 */
		public RunMeter(long inputStartTime, long inputTotal, long inputCurrent) {
			this.current = inputCurrent;
			this.total = inputTotal;
			this.startTime = inputStartTime;
		}

		void printProgress() {
			if (this.current < this.total)
				this.current++;
			printProgress(this.current);
		}

		/**
		 * Each call of method, advance this.current and print the meter.
		 */
		void printProgress(long givenCurrent) {

			long now = System.currentTimeMillis();
			long eta = givenCurrent == 0 ? 0 : (this.total - givenCurrent) * (now - this.startTime) / givenCurrent;

			String etaHms = givenCurrent == 0 ? "N/A"
					: String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
							TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
							TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

			StringBuilder string = new StringBuilder(140);
			int percent = (int) (givenCurrent * 100 / this.total);
			int percentScaled = (int) (givenCurrent * maxMeterSize / this.total);
			string.append('\r')
					.append(String.format("%s %3d%% [", new Time(now).toString(), percent))
					.append(String.join("", Collections.nCopies(percentScaled, "=")))
					.append('>')
					.append(String.join("", Collections.nCopies(maxMeterSize - percentScaled, " ")))
					.append(']')
					.append(String.join("",
							Collections.nCopies((int) (Math.log10(this.total)) - (int) (Math.log10((givenCurrent < 1) ? 1 : givenCurrent)), " ")))
					.append(String.format(" %d/%d, ETA: %s", givenCurrent, this.total, etaHms));

			System.out.print(string);
		}

	}

	/**
	 * CSV separator
	 */
	static final String CSVSep = ";\t";

	/**
	 * DateFormatter
	 */
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	/**
	 * Default implementation class for CSTNEdge
	 */
	static final Class<? extends CSTNEdge> EDGE_IMPL_CLASS = CSTNEdgePluggable.class;

	/**
	 * Global header
	 */
	static final String GLOBAL_HEADER = "\n\nGlobal statistics\n"
			+ "#CSTNs"
			+ CSVSep + "#nodes"
			+ CSVSep + "#contingent"
			+ CSVSep + "#propositions"
			+ CSVSep + "avgExeTime[s]"
			+ CSVSep + "std.dev.[s]"
			+ CSVSep + "avgRules/Cycles"
			+ CSVSep + "std.dev."
			+ CSVSep + "avgAddedEdgesRate"
			+ CSVSep + "std.dev."
			+ CSVSep + "\n";
	/**
	 * 
	 */
	static final String GLOBAL_HEADER_ROW = "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%E"
			+ CSVSep + "%E"
			+ CSVSep + "%E"
			+ CSVSep + "%E"
			+ CSVSep + "%E"
			+ CSVSep + "%E"
			+ CSVSep + "\n";

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger(Checker.class.getName());

	/**
	 * Output file header
	 */
	static final String OUTPUT_HEADER = "fileName"
			+ CSVSep + "#nodes"
			+ CSVSep + "#edges"
			+ CSVSep + "#propositions"
			+ CSVSep + "#ctg"
			+ CSVSep + "#minEdgeValue"
			+ CSVSep + "#maxEdgeValue"
			+ CSVSep + "avgTime[s]"
			+ CSVSep + "std.dev.[s]"
			+ CSVSep + "#ruleExecuted/cyles"
			+ CSVSep + "DC";

	/**
	 * Header
	 */
	static final String OUTPUT_HEADER_STNU = OUTPUT_HEADER
			+ CSVSep + "rateAddedEdges"
			+ CSVSep;

	/**
	 * CSTN header
	 */
	static final String OUTPUT_HEADER_CSTN = OUTPUT_HEADER
			+ CSVSep + "#R0"
			+ CSVSep + "#R3"
			+ CSVSep + "#LP"
			+ CSVSep + "#PotentialUpdate"
			+ CSVSep;
	/**
	 * CSTNU header
	 */
	static final String OUTPUT_HEADER_CSTNU = OUTPUT_HEADER_CSTN
			+ "#LUC+FLUC+LCUC"// upperCaseRuleCalls
			+ CSVSep + "#LLC"// lowerCaseRuleCalls
			+ CSVSep + "#LCC"// crossCaseRuleCalls
			+ CSVSep + "#LLR"// letterRemovalRuleCalls
			+ CSVSep;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_GRAPH = "%s"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME = "%E"// average time
			+ CSVSep + "%E"// std dev
			+ CSVSep + "%d"// #ruleExecuted or cycles
			+ CSVSep + "%s"// true of false for DC
			+ CSVSep;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME_CSTN = "%d"// r0
			+ CSVSep + "%d"// r3
			+ CSVSep + "%d"// LNC
			+ CSVSep + "%d"// PotentialUpdate
			+ CSVSep;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME_STNU = "%E"// added edges
			+ CSVSep;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME_STATS_CSTNU = "%d"// upperCaseRuleCalls
			+ CSVSep + "%d"// lowerCaseRuleCalls
			+ CSVSep + "%d"// crossCaseRuleCalls
			+ CSVSep + "%d"// letterRemovalRuleCalls
			+ CSVSep;

	/**
	 * Version
	 */
	// static final String VERSIONandDATE = "1.0, March, 22 2015";
	// static final String VERSIONandDATE = "1.1, November, 18 2015";
	// static final String VERSIONandDATE = "1.2, October, 10 2017";
	// static final String VERSIONandDATE = "1.3, October, 16 2017";// executor code cleaned
	// static final String VERSIONandDATE = "1.4, November, 09 2017";// code cleaned
	// static final String VERSIONandDATE = "2.0, November, 13 2017";// Multi-thread version.
	// static final String VERSIONandDATE = "2.1, November, 14 2017";// Multi-thread version. Fixed a slip!
	// static final String VERSIONandDATE = "2.2, November, 15 2017";// Added the possibility to test CSTNEpsilonwoNodeLabels and CSTN2CSTN0
	// static final String VERSIONandDATE = "2.23, November, 30 2017";// Improved the print of statistics file: std dev is print only when # checks > 1
	// static final String VERSIONandDATE = "2.24, December, 04 2017";// Added CSTNEpsilon3R
	// static final String VERSIONandDATE = "2.25, January, 17 2018";// Improved print of statistics.
	// static final String VERSIONandDATE = "2.26, December, 18 2018";// Improved print of statistics adding the total number of applied rules
	// static final String VERSIONandDATE = "2.27, June, 9 2019";// Refactoring Edge
	// static final String VERSIONandDATE = "2.5, November, 09 2019";// Removed all potential counters
	// static final String VERSIONandDATE = "3, June, 29 2020";// Add check for STN and STNU
	static final String VERSIONandDATE = "3.1, July, 28 2020";// Refined stats for STNU

	/**
	 * Allows to check the execution time of DC checking algorithm giving a set of instances.
	 * The set of instances are checked in parallel if the machine is a multi-cpus one.<br>
	 * Moreover, this method tries to exploit <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity</a> if kernel allows it.<br>
	 * So, if it is possible to reserve some CPU modifying the kernel as explained in <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity
	 * page</a>.
	 * it is possible to run the parallel thread in the better conditions.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException if instances contains syntax errors
	 * @throws ParserConfigurationException if input parameters contains errors
	 * @throws IOException if files are not readable
	 */
	@SuppressWarnings("null")
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		LOG.finest("Checker " + VERSIONandDATE + "\nStart...");
		System.out.println("Checker " + VERSIONandDATE + "\n" + getNow() + ": Start of execution.");
		final Checker tester = new Checker();

		if (!tester.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (tester.versionReq)
			return;
		// All parameters are set

		/**
		 * <a id="affinity">AffinityLock allows to lock a CPU to a thread.</a>
		 * It seems that allows better performance when a CPU-bound task has to be executed!
		 * To work, it requires to reserve some CPUs.
		 * In our server I modified /boot/grub/grub.cfg adding "isolcpus=4,5,6,7,8,9,10,11" to the line that boot the kernel to reserve 8 CPUs.
		 * Such CPU have (socketId-coreId): 4(0-4), 5(0-5), 6(1-0), 7(1-1), 8(1-2), 9(1-3), 10(1-4), 11(1-5).
		 * Then I reboot the server.
		 * This class has to be started as normal (no using taskset!)
		 * I don't modify in /etc/default/grub and, then, update-grub because it changes a lot of things.
		 * **NOTE**
		 * After some simulations on AMD AMD Opteron™ 4334, I discovered that:
		 * 0) The best performance is obtained checking one file at time!
		 * 1) It doesn't worth to run more than 2 processor in parallel because this kind of app does not allow to scale. For each added process,
		 * the performance lowers about 10%.
		 * 2) Running two processes in the two different sockets lowers the performance about the 20%! It is better to run the two process on the same socket.
		 * 3) Therefore, I modified /boot/grub/grub.cfg setting "isolcpus=8,9,10,11"
		 */
		int nCPUs = tester.nCPUs;// Runtime.getRuntime().availableProcessors();

		// Logging stuff for learning Affinity behaviour.
		// System.out.println("Base CPU affinity mask: " + AffinityLock.BASE_AFFINITY);
		// System.out.println("Reserved CPU affinity mask: " + AffinityLock.RESERVED_AFFINITY);
		// System.out.println("Current CPU affinity: " + Affinity.getCpu());
		// CpuLayout cpuLayout = AffinityLock.cpuLayout();
		// System.out.println("CPU Layout: " + cpuLayout.toString());
		// for (int k = 11; k > 3; k--) {
		// System.out.println("Cpu " + k + "\nSocket: " + cpuLayout.socketId(k) + ". Core:" + cpuLayout.coreId(k));
		// }
		/**
		 * check all files in parallel.
		 */
		/*
		 * 1st method using streams (parallelStream)
		 * Very nice, but it suffers of a known problem with streams:
		 * the use of default ForkJoinPool in the implementation of parallel() makes possible that
		 * a heavy task can block following tasks.
		 */
		// tester.inputCSTNFile.parallelStream().forEach(file -> cstnWorker(tester, file, executor, edgeFactory));

		/*
		 * 2nd method using Callable.
		 * A newFixedThreadPool executor create nProcessor threads and pipeline all process associated to file to such pool.
		 * There is no problem if one thread requires a lot of time.
		 * Final synchronization is obtained requesting .get from Callable.
		 * AffinityThreadFactory allows to lock a thread in one core for all the time (less overhead)
		 */
		final ExecutorService cstnExecutor = (nCPUs > 0) ? Executors.newFixedThreadPool(nCPUs,
				new AffinityThreadFactory("cstnWorker", AffinityStrategies.DIFFERENT_CORE)) : null;

		/**
		 * To collect statistics w.r.t. the dimension of networks
		 */
		Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> groupExecutionTimeStatistics = new Object2ObjectAVLTreeMap<>();
		Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> groupRuleExecutionStatistics = new Object2ObjectAVLTreeMap<>();
		Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> groupAddedEdgeStatistics = new Object2ObjectAVLTreeMap<>();

		System.out.println(getNow() + ": #Processors for computation: " + nCPUs);
		System.out.println(getNow() + ": Instances to check are " + tester.networkType + " instances.");
		RunMeter runMeter = new RunMeter(System.currentTimeMillis(), tester.instances.size(), 0);
		runMeter.printProgress(0);

		List<Future<Boolean>> future = new ArrayList<>();

		int nTaskSuccessfullyFinished = 0;
		for (File file : tester.instances) {
			if (nCPUs > 0) {
				future.add(cstnExecutor
						.submit(() -> tester.worker(file, runMeter, groupExecutionTimeStatistics, groupRuleExecutionStatistics, groupAddedEdgeStatistics)));
			} else {
				if (tester.worker(file, runMeter, groupExecutionTimeStatistics, groupRuleExecutionStatistics, groupAddedEdgeStatistics))
					nTaskSuccessfullyFinished++;
			}
		}
		if (nCPUs > 0) {
			// System.out.println(getNow() + ": #Tasks queued: " + future.size());
			// wait all tasks have been finished and count!
			for (Future<Boolean> f : future) {
				try {
					if (f.get()) {
						nTaskSuccessfullyFinished++;
					}
				} catch (Exception ex) {
					System.out.println("\nA problem occured during a check: " + ex.getMessage() + ". File ignored.");
				} finally {
					if (!f.isDone()) {
						LOG.warning("It is necessary to cancel the task before continuing.");
						f.cancel(true);
					}
				}
			}
		}
		String msg = "Number of instances processed successfully over total: " + nTaskSuccessfullyFinished + "/" + tester.instances.size() + ".";
		LOG.info(msg);
		System.out.println("\n" + getNow() + ": " + msg);

		tester.output.printf(GLOBAL_HEADER);

		for (it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<GlobalStatisticKey, SummaryStatistics> entry : groupExecutionTimeStatistics
				.object2ObjectEntrySet()) {
			GlobalStatisticKey globalStatisticsKey = entry.getKey();
			tester.output.printf(GLOBAL_HEADER_ROW, entry.getValue().getN(), globalStatisticsKey.getNodes(), globalStatisticsKey.getContingent(),
					globalStatisticsKey.getPropositions(),
					entry.getValue().getMean(), entry.getValue().getStandardDeviation(),
					groupRuleExecutionStatistics.get(globalStatisticsKey).getMean(),
					groupRuleExecutionStatistics.get(globalStatisticsKey).getStandardDeviation(),
					groupAddedEdgeStatistics.get(globalStatisticsKey).getMean(),
					groupAddedEdgeStatistics.get(globalStatisticsKey).getStandardDeviation());
		}
		tester.output.printf("\n\n\n");

		if (nCPUs > 0) {
			// executor shutdown!
			try {
				System.out.println(getNow() + ": Shutdown executors.");
				cstnExecutor.shutdown();
				cstnExecutor.awaitTermination(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				System.err.println(getNow() + ": Tasks interrupted.");
			} finally {
				if (!cstnExecutor.isTerminated()) {
					System.err.println(getNow() + ": Cancel non-finished tasks.");
				}
				cstnExecutor.shutdownNow();
				System.out.println(getNow() + ": Shutdown finished.\nExecution finished.");
			}
		}
		if (tester.output != null) {
			tester.output.close();
		}
	}

	/**
	 * @return the current date formatted.
	 */
	private static String getNow() {
		return dateFormatter.format(new Date());
	}

	/**
	 * 
	 */
	EdgeSupplier<Edge> currentEdgeFactory;

	/**
	 * Class for representing edge .
	 */
	Class<? extends Edge> currentEdgeImplClass;

	/**
	 * Parameter for asking to DC check epsilon DC reducing to IR DC
	 */
	@Option(required = false, name = "-cstn2cstn0", usage = "Check the epsilon-DC of the input instance using IR-DC on a correspondig streamlined CSTN. It transforms the input CSTN and then check its IR-DC. Epsilon value can be specified using -reactionTime parameter.")
	private boolean cstn2cstn0 = false;

	/**
	 * Which type of CSTN are input files
	 */
	@Option(required = true, name = "-type", usage = "Specify if input files contain STN, STNU, CSTN or CSTNU instances (use one of such keywords: STN STNU CSTN CSTNU).")
	private TNGraph.NetworkType networkType = NetworkType.CSTN;

	/**
	 * Parameter for asking to DC check epsilon DC reducing to IR DC
	 */
	@Option(required = false, name = "-cstnu2cstn", usage = "Check the CSTNU DC of the input instance reducing the check to the equivalent streamlined CSTN instance and checking this last one using IR-DC. It is incompatible with -potential option.")
	private boolean cstnu2cstn = false;

	/**
	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
	 */
	@Option(required = false, name = "-cuttingEdgeFactor", usage = "Cutting factor for reducing edge values. Default value is 1, i.e., no cut.")
	private int cuttingEdgeFactor = 1;

	/**
	 * Parameter for asking DC semantics.
	 */
	@Option(required = false, name = "-semantics", usage = "DC semantics. Possible values are: IR, ε, Std. Default is Std.")
	private DCSemantics dcSemantics = DCSemantics.Std;

	/**
	 * The input file names. Each file has to contain a CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTN graph in GraphML format.", metaVar = "CSTN_file_names", handler = StringArrayOptionHandler.class)
	private String[] inputFiles;

	/**
	 * 
	 */
	private List<File> instances;

	/**
	 * Roberto: I verified that with such kind of computation, using more than one thread to check more files in parallel reduces the single performance!!!
	 */
	@Option(required = false, name = "-nCPUs", usage = "Number of virtual CPUs that are reserved for this execution. Default is 0=no CPU reserved, there is only one thread for all the DC checking executions: such thread can allocated to a core, then desallocated and reallocated to another core. With nCPUs=1, there is only thread but such thread is allocated to a core till its end. With more thread, the global performance increases, but each file can requires more time because there is a competition among threads to access to the memory.")
	private int nCPUs = 0;

	/**
	 * Parameter for asking how many time to check the DC for each CSTN.
	 */
	@Option(required = false, name = "-numRepetitionDCCheck", usage = "Number of time to re-execute DC checking")
	private int nDCRepetition = 30;

	/**
	 * Parameter for avoiding DC check
	 */
	@Option(required = false, name = "-noDCCheck", usage = "Do not execute DC check. Just determine graph characteristics.")
	private boolean noDCCheck = false;

	/**
	 * Parameter for asking whether to consider only Lp,qR0, qR3* rules.
	 */
	@Option(required = false, name = "-onlyLPQR0QR3", aliases = {
			"-limitToZ" }, depends = { "-semantics" }, usage = "Check DC considering only rules LP, qR0 and QR3.")
	private boolean onlyLPQR0QR3OrToZ = false;

	/**
	 * Output stream to outputFile
	 */
	private PrintStream output = null;

	/**
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, data will be added.", metaVar = "outputFile")
	private File outputFile = null;

	/**
	 * Check a CSTN instance using BellamanFord algorithm
	 */
	@Option(required = false, name = "-potential", depends = { "-type cstn" }, usage = "Check a CSTN instance using BellamanFord algorithm.")
	private boolean potential = false;

	/**
	 * Parameter for asking reaction time.
	 */
	@Option(required = false, name = "-reactionTime", depends = { "-semantics ε" }, usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Parameter for asking to remove a value from all constraints.
	 */
	@Option(required = false, name = "-removeValue", usage = "Value to be removed from any edge. Default value is a value representing NULL.")
	private int removeValue = Constants.INT_NULL;

	/**
	 */
	@Option(required = false, name = "-save", usage = "Save all checked instances.")
	private boolean save = false;

	/**
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-timeOut", usage = "Time in seconds.")
	private int timeOut = 1200; // 20 min

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = { "--version" }, usage = "Version")
	private boolean versionReq = false;

	/**
	 * Parameter for asking whether to consider node labels during the DC check.
	 */
	@Option(required = false, name = "-woNodeLabels", usage = "Check DC transforming the network in an equivalent CSTN without node labels.")
	private boolean woNodeLabels = false;

	/**
	 * Parameter for asking which algorithm to use for checking STNU.
	 */
	@Option(required = false, name = "-stnuCheck", usage = "Which algorithm to use for checking STNU network. (use one of such keywords: Morris2014 RUL2020)")
	private STNU.CheckAlgorithm stnuCheckAlgorithm = CheckAlgorithm.RUL2020;

	/**
	 * @param tester
	 * @param cstn
	 * @param file
	 * @param executor
	 */
	@SuppressWarnings({ "javadoc", "unchecked", "rawtypes", "null" })
	private CSTNCheckStatus DCChecker(CSTN cstn, CSTNU cstnu, RunMeter runState) {
		String msg;
		boolean checkInterrupted = false;
		CSTNCheckStatus status = null;
		TNGraph<? extends CSTNEdge> graphToCheck = null;
		TNGraph g = null;// even though raw is dangerous, here it is safe!

		if (isCSTNU()) {
			status = new CSTNUCheckStatus();
			graphToCheck = cstnu.g;
		} else if (isCSTN()) {
			status = new CSTNCheckStatus();
			graphToCheck = cstn.g;
		}

		SummaryStatistics localSummaryStat = new SummaryStatistics();
		for (int j = 0; j < this.nDCRepetition && !checkInterrupted && !status.timeout; j++) {
			LOG.info("Test " + (j + 1) + "/" + this.nDCRepetition + " for (C)STN(U) " + graphToCheck.getFileName().getName());

			// It is necessary to reset the graph!
			g = new TNGraph(graphToCheck, this.currentEdgeImplClass);
			if (isCSTNU()) {
				cstnu.setG(g);
			} else {
				cstn.setG(g);
			}
			try {
				status = (isCSTNU()) ? cstnu.dynamicControllabilityCheck() : (cstn.dynamicConsistencyCheck());
			} catch (CancellationException ex) {
				msg = getNow() + ": Cancellation has occurred. " + graphToCheck.getFileName() + " CSTN(U) is ignored.";
				System.out.println(msg);
				LOG.severe(msg);
				checkInterrupted = true;
				status.consistency = false;
				continue;
			} catch (Exception e) {
				msg = getNow() + ": a different exception has occurred. " + graphToCheck.getFileName()
						+ " CSTN(U) is ignored.\nError details: " + e.toString();
				System.out.println(msg);
				LOG.severe(msg);
				checkInterrupted = true;
				status.consistency = false;
				continue;
			}
			localSummaryStat.addValue(status.executionTimeNS);
		} // end for checking repetition for a single file

		if (status.timeout || checkInterrupted) {
			if (status.timeout) {
				msg = "\n\n" + getNow() + ": Timeout or interrupt occurred. " + graphToCheck.getFileName() + " CSTN(U) is ignored.\n";
				System.out.println(msg);
			}
			return status;
		}

		msg = getNow() + ": done! It is " + ((!status.consistency) ? "NOT " : "") + "DC.";
		// System.out.println(msg);
		LOG.info(msg);

		status.executionTimeNS = (long) localSummaryStat.getMean();
		status.stdDevExecutionTimeNS = (long) localSummaryStat.getStandardDeviation();

		return status;
	}

	/**
	 * @param tester
	 * @param cstn
	 * @param file
	 * @param executor
	 */
	@SuppressWarnings({ "javadoc", "unchecked", "rawtypes", "null" })
	private STNCheckStatus STNUDCChecker(STN stn, STNU stnu, RunMeter runState) {
		String msg;
		boolean checkInterrupted = false;
		STNCheckStatus status = null;
		TNGraph<? extends STNEdge> graphToCheck = null;
		TNGraph g = null;// even though raw is dangerous, here it is safe!

		if (isSTN()) {
			status = new STNCheckStatus();
			graphToCheck = stn.g;
		} else if (isSTNU()) {
			status = new STNUCheckStatus();
			graphToCheck = stnu.g;
		}

		SummaryStatistics localSummaryStat = new SummaryStatistics();
		for (int j = 0; j < this.nDCRepetition && !checkInterrupted && !status.timeout; j++) {
			LOG.info("Test " + (j + 1) + "/" + this.nDCRepetition + " for STN(U) " + graphToCheck.getFileName().getName());

			// It is necessary to reset the graph!
			g = new TNGraph(graphToCheck, this.currentEdgeImplClass);
			if (isSTN()) {
				stn.setG(g);
			} else {
				stnu.setG(g);
			}
			try {
				status = (isSTNU()) ? stnu.dynamicControllabilityCheck(this.stnuCheckAlgorithm) : (stn.consistencyCheck());
			} catch (CancellationException ex) {
				msg = getNow() + ": Cancellation has occurred. " + graphToCheck.getFileName() + " STN(U) is ignored.";
				System.out.println(msg);
				LOG.severe(msg);
				checkInterrupted = true;
				status.consistency = false;
				continue;
			} catch (Exception e) {
				msg = getNow() + ": a different exception has occurred. " + graphToCheck.getFileName()
						+ " STN(U) is ignored.\nError details: " + e.toString();
				System.out.println(msg);
				LOG.severe(msg);
				checkInterrupted = true;
				status.consistency = false;
				continue;
			}
			localSummaryStat.addValue(status.executionTimeNS);
		} // end for checking repetition for a single file

		if (status.timeout || checkInterrupted) {
			if (status.timeout) {
				msg = "\n\n" + getNow() + ": Timeout or interrupt occurred. " + graphToCheck.getFileName() + " STN(U) is ignored.\n";
				System.out.println(msg);
			}
			return status;
		}

		msg = getNow() + ": done! It is " + ((!status.consistency) ? "NOT " : "") + "DC.";
		// System.out.println(msg);
		LOG.info(msg);

		status.executionTimeNS = (long) localSummaryStat.getMean();
		status.stdDevExecutionTimeNS = (long) localSummaryStat.getStandardDeviation();

		return status;
	}

	/**
	 * @param status current status
	 * @return the number of executed rules.
	 */
	private int getNumberExecutedRules(STNCheckStatus status) {
		int nRules = status.propagationCalls;

		if (this.isCSTN()) {
			CSTNCheckStatus s = ((CSTNCheckStatus) status);
			nRules = s.r0calls + s.r3calls + s.labeledValuePropagationCalls + s.potentialUpdate;
		}
		if (this.isSTNU()) {
			STNUCheckStatus s = ((STNUCheckStatus) status);
			nRules = s.cycles;
		}
		if (isCSTNU())
			nRules += ((CSTNUCheckStatus) status).zEsclamationRuleCalls +
					((CSTNUCheckStatus) status).lowerCaseRuleCalls +
					((CSTNUCheckStatus) status).crossCaseRuleCalls +
					((CSTNUCheckStatus) status).letterRemovalRuleCalls;
		return nRules;
	}

	/**
	 * @return true if the input graph represents a CSTN instance
	 */
	private boolean isCSTN() {
		return this.networkType == NetworkType.CSTN;
	}

	/**
	 * @return true if the input graph represents a STN instance
	 */
	private boolean isSTN() {
		return this.networkType == NetworkType.STN;
	}

	/**
	 * @return true if the input graph represents a STNU instance
	 */
	private boolean isSTNU() {
		return this.networkType == NetworkType.STNU;
	}

	/**
	 * @return true if the input graph represents a CSTNU instance
	 */
	private boolean isCSTNU() {
		return this.networkType == NetworkType.CSTNU;
	}

	/**
	 * @param g input graph
	 * @return an stn instance
	 */
	private STN makeSTNInstance(TNGraph<STNEdge> g) {
		if (this.isSTN()) {
			return new STN(g, this.timeOut);
		}
		throw new IllegalArgumentException("Required a STN instance but the input graph is not a STN graph.");
	}

	/**
	 * @param g input graph
	 * @return an stnu instance
	 */

	private STNU makeSTNUInstance(TNGraph<STNUEdge> g) {
		if (this.isSTNU()) {
			STNU stnu = new STNU(g, this.timeOut);
			stnu.setSave(this.save);
			return stnu;
		}
		throw new IllegalArgumentException("Required a STNU instance but the input graph is not a STNU graph.");
	}

	/**
	 * @param g input graph
	 * @return a cstnu instance
	 */

	private CSTNU makeCSTNUInstance(TNGraph<CSTNUEdge> g) {
		if (this.isCSTNU()) {
			if (this.cstnu2cstn) {
				return new CSTNU2CSTN(g, this.timeOut);
			}
			return new CSTNU(g, this.timeOut, this.onlyLPQR0QR3OrToZ);
		}
		throw new IllegalArgumentException("Required a CSTNU instance but the graph is not a CSTNU graph.");
	}

	/**
	 * @param g input graph
	 * @return a cstn instance
	 */

	private CSTN makeCSTNInstance(TNGraph<CSTNEdge> g) {
		if (this.cstn2cstn0) {
			return new CSTN2CSTN0(this.reactionTime, g, this.timeOut);
		}
		if (this.potential)// the implicit semantics is IR
			return new CSTNPotential(g, this.timeOut);
		CSTN cstn = null;
		switch (this.dcSemantics) {
		case ε:
			if (this.onlyLPQR0QR3OrToZ) {
				cstn = (this.woNodeLabels) ? new CSTNEpsilon3RwoNodeLabels(this.reactionTime, g, this.timeOut)
						: new CSTNEpsilon3R(this.reactionTime, g, this.timeOut);
			} else {
				cstn = (this.woNodeLabels) ? new CSTNEpsilonwoNodeLabels(this.reactionTime, g, this.timeOut)
						: new CSTNEpsilon(this.reactionTime, g, this.timeOut);
			}
			break;
		case IR:
			if (this.onlyLPQR0QR3OrToZ) {
				cstn = (this.woNodeLabels) ? new CSTNIR3RwoNodeLabels(g, this.timeOut) : new CSTNIR3R(g, this.timeOut);
			} else {
				cstn = (this.woNodeLabels) ? new CSTNIRwoNodeLabels(g, this.timeOut) : new CSTNIR(g, this.timeOut);
			}
			break;
		case Std:
		default:
			if (this.onlyLPQR0QR3OrToZ) {
				throw new IllegalArgumentException(
						"For standard semantics there is no DC checking algorithm that works only considering constraints ending to Z.");
			}
			cstn = (this.woNodeLabels) ? new CSTNwoNodeLabel(g, this.timeOut) : new CSTN(g, this.timeOut);
			break;
		}
		return cstn;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args input arguments
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.Checker [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -jar Checker.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTN_file_name0> <CSTN_file_name1>...");
			return false;
		}
		if (this.versionReq) {
			System.out.print(this.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017-2020, Roberto Posenato");
			return true;
		}

		if (this.outputFile != null) {
			if (this.outputFile.isDirectory()) {
				System.err.println("Output file is a directory.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			// filename has to end with .csv
			if (!this.outputFile.getName().endsWith(".csv")) {
				this.outputFile.renameTo(new File(this.outputFile.getAbsolutePath() + ".csv"));
			}
			try {
				this.output = new PrintStream(new FileOutputStream(this.outputFile, true), true);
			} catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		} else {
			this.output = System.out;
		}

		if (this.reactionTime <= 0) {
			System.err.println("Reaction time must be > 0");
			return false;
		}

		String suffix = "";
		if (this.networkType == NetworkType.CSTN) {
			this.output.println(OUTPUT_HEADER_CSTN + CSVSep);
			this.currentEdgeImplClass = CSTNEdgePluggable.class;
			suffix = "cstn";
		} else {
			if (this.networkType == NetworkType.CSTNU) {
				this.output.println(OUTPUT_HEADER_CSTNU);
				this.currentEdgeImplClass = CSTNUEdgePluggable.class;
				suffix = "cstnu";
			} else {
				if (this.networkType == NetworkType.STNU) {
					this.output.println(OUTPUT_HEADER_STNU + CSVSep);
					this.currentEdgeImplClass = STNUEdgeInt.class;
					suffix = "stnu";
				} else {
					if (this.networkType == NetworkType.STN) {
						this.output.println(OUTPUT_HEADER + CSVSep);
						this.currentEdgeImplClass = STNEdgeInt.class;
						suffix = "stn";
					} else {
						String msg = "Type of network not managed by current version of this class. Game over :-/";
						throw new IllegalArgumentException(msg);
					}
				}
			}
		}
		this.currentEdgeFactory = new EdgeSupplier<>(this.currentEdgeImplClass);

		// LOG.finest("File number: " + this.fileNameInput.length);
		// LOG.finest("File names: " + Arrays.deepToString(this.fileNameInput));
		this.instances = new ArrayList<>(this.inputFiles.length);
		for (String fileName : this.inputFiles) {
			File file = new File(fileName);
			if (!file.exists()) {
				System.err.println("File " + fileName + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			if (!file.getName().endsWith(suffix)) {
				System.err.println("File " + fileName + " has not the right suffix associated to the suffix of the given network type (right suffix: "
						+ suffix + "). Game over :-/");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			this.instances.add(file);
		}

		return true;
	}

	/**
	 * @param <E1> type of edge
	 * @param <M1> type of labeled value map
	 * @param file input file
	 * @param runState current state
	 * @param globalExecutionTimeStatisticsMap global statistics
	 * @param globalRuleExecutionStatisticsMap another global statistics
	 * @param globalAddedEdgeStatisticsMap only for STNU
	 * @return true if required task ends successfully, false otherwise.
	 */
	@SuppressWarnings({ "unchecked", "null" })
	private <E1 extends Edge, M1 extends LabeledIntMap> boolean worker(File file, RunMeter runState,
			Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> globalExecutionTimeStatisticsMap,
			Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> globalRuleExecutionStatisticsMap,
			Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> globalAddedEdgeStatisticsMap) {
		// System.out.println("Analyzing file " + file.getName() + "...");
		LOG.finer("Loading " + file.getName() + "...");
		TNGraphMLReader<E1> graphMLReader;
		try {
			graphMLReader = new TNGraphMLReader<>(file, (Class<E1>) this.currentEdgeImplClass);
		} catch (FileNotFoundException e2) {
			String msg = "File " + file.getName() + " cannot be loaded. Details: " + e2.getMessage() + ".\nIgnored.";
			LOG.warning(msg);
			System.out.println(msg);
			return false;
		}
		TNGraph<E1> graphToCheck = null;
		try {
			graphToCheck = graphMLReader.readGraph();
		} catch (IOException | ParserConfigurationException | SAXException e2) {
			String msg = "File " + file.getName() + " cannot be parsed. Details: " + e2.getMessage() + ".\nIgnored.";
			LOG.warning(msg);
			System.out.println(msg);
			return false;
		}
		LOG.finer("...done!");
		if (graphToCheck == null) {
			LOG.warning("File " + file.getName() + " does not contain a valid CSTN instance.\nIgnored.");
			return false;
		}

		/**
		 * *************************************************
		 * Possible required modifications of the structure.
		 * *************************************************
		 */
		if (this.cuttingEdgeFactor > 1 || this.removeValue != Constants.INT_NULL) {
			if (this.cuttingEdgeFactor > 1) {
				LOG.info("Cutting all edge values by a factor " + this.cuttingEdgeFactor + "...");
				for (Edge e : graphToCheck.getEdges()) {
					if (e.isSTNEdge()) {
						((STNEdge) e).setValue(((STNEdge) e).getValue() / this.cuttingEdgeFactor);
					}
					if (this.isCSTN() || this.isCSTNU()) {
						CSTNEdge eNew = (CSTNEdge) this.currentEdgeFactory.get(e);
						for (Entry<Label> entry : ((CSTNEdge) e).getLabeledValueSet()) {
							int v = entry.getIntValue() / this.cuttingEdgeFactor;
							eNew.mergeLabeledValue(entry.getKey(), v);
						}
						((CSTNEdge) e).takeIn(eNew);
					}
				}
			}
			if (this.removeValue != Constants.INT_NULL && (this.isCSTN() || this.isCSTNU())) {
				LOG.info("Removing all edge values equal to " + this.removeValue + "...");

				int value = this.removeValue;
				for (Edge e : graphToCheck.getEdges()) {
					if (e.isSTNEdge()) {
						if (((STNEdge) e).getValue() == value) {
							graphToCheck.removeEdge(e.getName());
						}
					}
					for (Entry<Label> entry : ((CSTNEdge) e).getLabeledValueSet()) {
						if (entry.getIntValue() == value)
							((CSTNEdge) e).removeLabeledValue(entry.getKey());
					}
				}
			}
			String suffix = "_cutted";
			TNGraphMLWriter graphWrite = new TNGraphMLWriter(null);

			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + suffix));) {
				graphWrite.save(graphToCheck, writer);
			} catch (Exception e) {
				LOG.warning("File " + file.getAbsolutePath() + suffix + " cannot be written. Details: " + e.getMessage());
				return false;
			}
		}

		// In order to start with well-defined cstn, we preliminary make a check.
		// Use g because next instructions change the structure of graph.
		TNGraph<E1> g = new TNGraph<>(graphToCheck, (Class<E1>) this.currentEdgeImplClass);

		// Statistics about min, max values and #edges have to be done before init!
		int min = 0;
		int max = 0;
		for (Edge e : g.getEdges()) {
			if (e.isSTNEdge() || e.isSTNUEdge()) {
				int v = ((STNEdge) e).getValue();
				if (v == Constants.INT_NULL)
					continue;
				if (v > max) {
					max = v;
					continue;
				}
				if (v < min)
					min = v;
			}
			if (e.isCSTNEdge() || e.isCSTNUEdge()) {
				for (Entry<Label> entry : ((CSTNEdge) e).getLabeledValueSet()) {
					int v = entry.getIntValue();
					if (v > max) {
						max = v;
						continue;
					}
					if (v < min)
						min = v;
				}
			}
			if (e.isSTNUEdge() && e.isContingentEdge()) {
				int v = ((STNUEdge) e).getLabeledValue();
				if (v == Constants.INT_NULL)
					continue;
				if (v > max) {
					max = v;
					continue;
				}
				if (v < min)
					min = v;
			}
		}
		int nEdges = g.getEdgeCount();

		CSTN cstn = null;
		CSTNU cstnu = null;
		STNU stnu = null;
		STN stn = null;

		TNGraph<CSTNEdge> gCSTN;
		TNGraph<CSTNUEdge> gCSTNU;
		TNGraph<STNEdge> gSTN;
		TNGraph<STNUEdge> gSTNU;

		if (this.isCSTN()) {
			gCSTN = (TNGraph<CSTNEdge>) new TNGraph<>(graphToCheck, (Class<E1>) this.currentEdgeImplClass);
			cstn = makeCSTNInstance(gCSTN);
		} else {
			if (this.isCSTNU()) {
				gCSTNU = (TNGraph<CSTNUEdge>) new TNGraph<>(graphToCheck, (Class<E1>) this.currentEdgeImplClass);
				cstnu = makeCSTNUInstance(gCSTNU);
			} else {
				if (this.isSTNU()) {
					gSTNU = (TNGraph<STNUEdge>) new TNGraph<>(graphToCheck, (Class<E1>) this.currentEdgeImplClass);
					stnu = makeSTNUInstance(gSTNU);
				} else {
					gSTN = (TNGraph<STNEdge>) new TNGraph<>(graphToCheck, (Class<E1>) this.currentEdgeImplClass);
					stn = makeSTNInstance(gSTN);
				}
			}
		}
		try {
			if (this.isCSTNU()) {
				cstnu.initAndCheck();
			} else {
				if (this.isCSTN()) {
					cstn.initAndCheck();
				} else {
					if (this.isSTNU()) {
						stnu.initAndCheck();
					} else {
						stn.initAndCheck();
					}
				}
			}
		} catch (Exception e) {
			String msg = getNow() + ": " + file.getName()
					+ " is not a not well-defined instance. Details:" + e.getMessage()
					+ "\nIgnored.";
			System.out.println(msg);
			LOG.severe(msg);
			return false;
		}

		String rowToWrite = String.format(OUTPUT_ROW_GRAPH,
				file.getName(),
				graphToCheck.getVertexCount(),
				nEdges,
				graphToCheck.getObserverCount(),
				graphToCheck.getContingentNodeCount(),
				min,
				max);

		if (this.noDCCheck) {
			synchronized (this.output) {
				this.output.println(rowToWrite);
			}
			runState.printProgress();
			return true;
		}

		GlobalStatisticKey globalStatisticsKey = new GlobalStatisticKey(graphToCheck.getVertexCount(), graphToCheck.getObserverCount(),
				graphToCheck.getContingentNodeCount());
		SummaryStatistics globalExecutionTimeStatistics = globalExecutionTimeStatisticsMap.get(globalStatisticsKey);
		SummaryStatistics globalRuleExecutionStatistics = globalRuleExecutionStatisticsMap.get(globalStatisticsKey);
		SummaryStatistics globalAddedEdgeStatistics = globalAddedEdgeStatisticsMap.get(globalStatisticsKey);
		if (globalExecutionTimeStatistics == null) {
			globalExecutionTimeStatistics = new SummaryStatistics();
			globalExecutionTimeStatisticsMap.put(globalStatisticsKey, globalExecutionTimeStatistics);
		}
		if (globalRuleExecutionStatistics == null) {
			globalRuleExecutionStatistics = new SummaryStatistics();
			globalRuleExecutionStatisticsMap.put(globalStatisticsKey, globalRuleExecutionStatistics);
		}
		if (globalAddedEdgeStatistics == null) {
			globalAddedEdgeStatistics = new SummaryStatistics();
			globalAddedEdgeStatisticsMap.put(globalStatisticsKey, globalAddedEdgeStatistics);
		}
		String msg = getNow() + ": Determining DC check execution time of " + file.getName()
				+ " repeating DC check for " + this.nDCRepetition + " times.";
		// System.out.println(msg);
		LOG.info(msg);

		STNCheckStatus status;
		if (this.isCSTN() || this.isCSTNU()) {
			status = DCChecker(cstn, cstnu, runState);
		} else {
			status = STNUDCChecker(stn, stnu, runState);
		}

		if (!status.finished) {
			// time out or generic error
			rowToWrite += String.format(OUTPUT_ROW_TIME,
					status.executionTimeNS / 1E9,
					Double.NaN,
					getNumberExecutedRules(status),
					((status.executionTimeNS != Constants.INT_NULL) ? "Timeout of " + this.timeOut + " seconds." : "Generic error. See log."));
			synchronized (this.output) {
				this.output.println(rowToWrite);
			}
			globalExecutionTimeStatistics.addValue(this.timeOut);
			runState.printProgress();
			return false;
		}

		Double localAvg = status.executionTimeNS / 1E9;
		Double localStdDev = status.stdDevExecutionTimeNS / 1E9;

		globalExecutionTimeStatistics.addValue(localAvg);

		LOG.info(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
		LOG.info(file.getName() + " is " + status.consistency);
		LOG.info(file.getName() + " average required time [s]" + localAvg);
		LOG.info(file.getName() + " std. deviation [s]" + localStdDev);

		int nRules = getNumberExecutedRules(status);
		globalRuleExecutionStatistics.addValue(nRules);

		rowToWrite += String.format(OUTPUT_ROW_TIME,
				localAvg,
				((this.nDCRepetition > 1) ? localStdDev : Double.NaN),
				nRules,
				((!this.noDCCheck) ? (status.finished ? Boolean.toString(status.consistency).toUpperCase() : "FALSE") : "-"));
		if (isCSTN()) {
			rowToWrite += String.format(OUTPUT_ROW_TIME_CSTN,
					((CSTNCheckStatus) status).r0calls,
					((CSTNCheckStatus) status).r3calls,
					((CSTNCheckStatus) status).labeledValuePropagationCalls,
					((CSTNCheckStatus) status).potentialUpdate);
		}
		if (isSTNU()) {
			double edgeRate = ((double) ((STNUCheckStatus) status).addedEdges) / nEdges;
			globalAddedEdgeStatistics.addValue(edgeRate);
			rowToWrite += String.format(OUTPUT_ROW_TIME_STNU, edgeRate);
		}
		if (isCSTNU()) {
			rowToWrite += String.format(OUTPUT_ROW_TIME_STATS_CSTNU,
					((CSTNUCheckStatus) status).zEsclamationRuleCalls,
					((CSTNUCheckStatus) status).lowerCaseRuleCalls,
					((CSTNUCheckStatus) status).crossCaseRuleCalls,
					((CSTNUCheckStatus) status).letterRemovalRuleCalls);
		}

		synchronized (this.output) {
			this.output.println(rowToWrite);
		}
		runState.printProgress();
		return true;
	}
}