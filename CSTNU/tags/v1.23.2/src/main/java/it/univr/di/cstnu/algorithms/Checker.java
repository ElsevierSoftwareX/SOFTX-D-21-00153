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
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.CSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

/**
 * Simple class to determine the average execution time (and std dev) of the CSTN(U) DC checking algorithm on a input set of CSTN(U)s.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class Checker {
	@SuppressWarnings("javadoc")

	/**
	 * Utility internal class
	 * 
	 * @author posenato
	 */
	public static class GlobalStatisticKey implements Comparable<GlobalStatisticKey> {
		protected int nodes;
		protected int propositions;
		protected int contingents;

		public GlobalStatisticKey(final int nodes, final int propositions, final int contingents) {
			this.nodes = nodes;
			this.propositions = propositions;
			this.contingents = contingents;
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
			if (!(o instanceof GlobalStatisticKey))
				return false;
			return ((GlobalStatisticKey) o).compareTo(this) == 0;
		}

		public int getNodes() {
			return this.nodes;
		}

		public int getPropositions() {
			return this.propositions;
		}

		public int getContingent() {
			return this.contingents;
		}

		@Override
		public int hashCode() {
			return this.nodes + 1000 * this.propositions + 10000 * this.contingents;
		}

	}

	@SuppressWarnings({ "javadoc" })
	private static class RunMeter {

		static final int maxMeterSize = 50;// [0--100]

		long current;
		long startTime;
		long total;

		/**
		 * @param startTime in milliseconds
		 * @param total number of time to show
		 * @param current number of time to show
		 */
		public RunMeter(long startTime, long total, long current) {
			this.current = current;
			this.total = total;
			this.startTime = startTime;
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
	 * For deciding which kind of instances has to be checked.
	 * 
	 * @author posenato
	 */
	@SuppressWarnings("javadoc")
	private static enum CstnType {
		cstn, cstnu
	}

	/**
	 * CSV separator
	 */
	static final String CSVSep = ";	";

	/**
	 * Global header
	 */
	static final String GLOBAL_HEADER = "\n\nGlobal statistics\n"
			+ "#CSTNs"
			+ CSVSep + "#nodes"
			+ CSVSep + "#contingent"
			+ CSVSep + "#propositions"
			+ CSVSep + "averageTime[s]"
			+ CSVSep + "std.Dev.[s]\n";

	/**
	 * 
	 */
	static final String GLOBAL_HEADER_ROW = "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%E"
			+ CSVSep + "%E\n";

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
			+ CSVSep + "averageTime[s]"
			+ CSVSep + "std.Dev.[s]"
			+ CSVSep + "DC"
			+ CSVSep + "#R0"
			+ CSVSep + "#R3"
			+ CSVSep + "#LNC";
	/**
	 * 
	 */
	static final String OUTPUT_HEADER_CSTNU = OUTPUT_HEADER
			+ CSVSep + "#LUC+FLUC+LCUC"// upperCaseRuleCalls
			+ CSVSep + "#LLC"// lowerCaseRuleCalls
			+ CSVSep + "#LCC"// crossCaseRuleCalls
			+ CSVSep + "#LLR";// letterRemovalRuleCalls

	/**
	 * 
	 */
	static final String OUTPUT_ROW_GRAPH = "%s"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d"
			+ CSVSep + "%d";

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME = CSVSep
			+ "%E"// average time
			+ CSVSep + "%E"// std dev
			+ CSVSep + "%s";// true of false for DC

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME_STATS = OUTPUT_ROW_TIME
			+ CSVSep + "%d"// r0
			+ CSVSep + "%d"// r3
			+ CSVSep + "%d";// lp;

	/**
	 * 
	 */
	static final String OUTPUT_ROW_TIME_STATS_CSTNU = CSVSep + "%d"// upperCaseRuleCalls
			+ CSVSep + "%d"// lowerCaseRuleCalls
			+ CSVSep + "%d"// crossCaseRuleCalls
			+ CSVSep + "%d";// letterRemovalRuleCalls

	/**
	 * Class for representing edge labeled values.
	 */
	final static Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNRunningTime");

	/**
	 * DateFormatter
	 */
	static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

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
	static final String VERSIONandDATE = "2.25, January, 17 2018";// Improved print of statistics.

	/**
	 * @param tester
	 * @param cstn
	 * @param file
	 * @param executor
	 */
	@SuppressWarnings({ "javadoc" })
	static private CSTNCheckStatus DCChecker(Checker tester, CSTN cstn, LabeledIntGraph graphToCheck, RunMeter runState) {
		String msg;
		boolean checkInterrupted = false;
		LabeledIntGraph g = null;
		CSTNCheckStatus status = (tester.cstnType == CstnType.cstnu) ? new CSTNUCheckStatus() : new CSTNCheckStatus();
		SummaryStatistics localSummaryStat = new SummaryStatistics();
		for (int j = 0; j < tester.nDCRepetition && !checkInterrupted && !status.timeout; j++) {
			LOG.info("Test " + (j + 1) + "/" + tester.nDCRepetition + " for CSTN " + graphToCheck.getFileName().getName());

			// It is necessary to reset the graph!
			g = new LabeledIntGraph(graphToCheck, labeledIntValueMap);
			cstn.setG(g);

			try {
				status = (tester.cstnType == CstnType.cstnu) ? ((CSTNU) cstn).dynamicControllabilityCheck()
						: (cstn.dynamicConsistencyCheck());
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
	 * @param file
	 * @param runState
	 * @param globalExecutionTimeStatisticsMap
	 * @param edgeFactory
	 * @return true if required task ends successfully, false otherwise.
	 */
	static private boolean worker(Checker tester, File file, RunMeter runState,
			Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> globalExecutionTimeStatisticsMap,
			LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory) {
		// System.out.println("Analyzing file " + file.getName() + "...");
		LOG.finer("Loading " + file.getName() + "...");
		CSTNUGraphMLReader graphMLReader;
		try {
			graphMLReader = new CSTNUGraphMLReader(file, labeledIntValueMap);
		} catch (FileNotFoundException e2) {
			String msg = "File " + file.getName() + " cannot be loaded. Details: " + e2.getMessage() + ".\nIgnored.";
			LOG.warning(msg);
			System.out.println(msg);
			return false;
		}
		LabeledIntGraph graphToCheck = null;
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

		if (tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {
			if (tester.cuttingEdgeFactor > 1) {
				LOG.info("Cutting all edge values by a factor " + tester.cuttingEdgeFactor + "...");
				for (LabeledIntEdge e : graphToCheck.getEdgesArray()) {
					LabeledIntEdgePluggable e1 = edgeFactory.get(e);
					for (Entry<Label> entry : e.getLabeledValueSet()) {
						int v = entry.getIntValue() / tester.cuttingEdgeFactor;
						e1.mergeLabeledValue(entry.getKey(), v);
					}
					((LabeledIntEdgePluggable) e).takeIn(e1);
				}
			}
			if (tester.removeValue != Constants.INT_NULL) {
				LOG.info("Removing all edge values equal to " + tester.removeValue + "...");

				int value = tester.removeValue;
				for (LabeledIntEdge e : graphToCheck.getEdgesArray()) {
					for (Entry<Label> entry : e.getLabeledValueSet()) {
						if (entry.getIntValue() == value)
							e.removeLabeledValue(entry.getKey());
					}
				}
			}
			String suffix = "_cutted";
			CSTNUGraphMLWriter graphWrite = new CSTNUGraphMLWriter(null);
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + suffix));) {
				graphWrite.save(graphToCheck, writer);
			} catch (Exception e) {
				LOG.warning("File " + file.getAbsolutePath() + suffix + " cannot be written. Details: " + e.getMessage());
				return false;
			}
		}

		// In order to start with well-defined cstn, we preliminary make a check.
		// Use g because next instructions change the structure of graph.
		LabeledIntGraph g = new LabeledIntGraph(graphToCheck, labeledIntValueMap);

		// Statistics about min, max values and #edges have to be done before init!
		int min = 0;
		int max = 0;
		for (LabeledIntEdge e : g.getEdgesArray()) {
			for (Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v > max) {
					max = v;
					continue;
				}
				if (v < min)
					min = v;
			}
		}
		int nEdges = g.getEdgeCount();

		final CSTN cstn = makeCSTNInstance(tester, g);

		try {
			cstn.initAndCheck();
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
				graphToCheck.getContingentCount(),
				min,
				max);

		if (tester.noDCCheck) {
			synchronized (tester.output) {
				tester.output.println(rowToWrite);
			}
			runState.printProgress();
			return true;
		}

		GlobalStatisticKey globalStatisticsKey = new GlobalStatisticKey(graphToCheck.getVertexCount(), graphToCheck.getObserverCount(),
				graphToCheck.getContingentCount());
		SummaryStatistics globalExecutionTimeStatistics = globalExecutionTimeStatisticsMap.get(globalStatisticsKey);
		if (globalExecutionTimeStatistics == null) {
			globalExecutionTimeStatistics = new SummaryStatistics();
			globalExecutionTimeStatisticsMap.put(globalStatisticsKey, globalExecutionTimeStatistics);
		}

		String msg = getNow() + ": Determining DC check execution time of " + file.getName()
				+ " repeating DC check for " + tester.nDCRepetition + " times.";
		// System.out.println(msg);
		LOG.info(msg);

		CSTNCheckStatus status = DCChecker(tester, cstn, graphToCheck, runState);

		if (!status.finished) {
			// time out or generic error
			rowToWrite += String.format(OUTPUT_ROW_TIME,
					status.executionTimeNS / 1E9,
					Double.NaN,
					((status.executionTimeNS != Constants.INT_NULL) ? "Timeout of " + tester.timeOut + " seconds." : "Generic error. See log."));
			synchronized (tester.output) {
				tester.output.println(rowToWrite);
			}
			globalExecutionTimeStatistics.addValue(tester.timeOut);
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

		rowToWrite += String.format(OUTPUT_ROW_TIME_STATS,
				localAvg,
				((tester.nDCRepetition > 1) ? localStdDev : Double.NaN),
				((!tester.noDCCheck) ? (status.finished ? Boolean.toString(status.consistency).toUpperCase() : "FALSE") : "-"),
				status.r0calls,
				status.r3calls,
				status.labeledValuePropagationCalls);
		if (tester.cstnType == CstnType.cstnu) {
			rowToWrite += String.format(OUTPUT_ROW_TIME_STATS_CSTNU,
					((CSTNUCheckStatus) status).upperCaseRuleCalls,
					((CSTNUCheckStatus) status).lowerCaseRuleCalls,
					((CSTNUCheckStatus) status).crossCaseRuleCalls,
					((CSTNUCheckStatus) status).letterRemovalRuleCalls);
		}

		synchronized (tester.output) {
			tester.output.println(rowToWrite);
		}
		runState.printProgress();
		return true;
	}

	@SuppressWarnings("javadoc")
	private static String getNow() {
		return dateFormatter.format(new Date());
	}

	/**
	 * Allows to check the execution time of DC checking algorithm giving a set of instances.
	 * The set of instances are checked in parallel if the machine is a multi-cpus one.<br>
	 * Moreover, this method tries to exploit <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity</a> if kernel allows it.<br>
	 * So, if it is possible to reserve some CPU modifying the kernel as explained in <a href="https://github.com/OpenHFT/Java-Thread-Affinity">thread affinity
	 * page</a>.
	 * it is possible to run the parallel thread in the better conditions.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
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

		final LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeSupplier<>(labeledIntValueMap);
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
		 * To collect statistics w.r.t. the dimension of CSTNs
		 */
		Object2ObjectMap<GlobalStatisticKey, SummaryStatistics> groupStatistics = new Object2ObjectAVLTreeMap<>();

		System.out.println(getNow() + ": #Processors for computation: " + nCPUs);
		System.out.println(getNow() + ": Instances to check are " + tester.cstnType + " instances.");
		RunMeter runMeter = new RunMeter(System.currentTimeMillis(), tester.instances.size(), 0);
		runMeter.printProgress(0);

		List<Future<Boolean>> future = new ArrayList<>();

		int nTaskSuccessfullyFinished = 0;
		for (File file : tester.instances) {
			if (nCPUs > 0) {
				future.add(cstnExecutor.submit(() -> worker(tester, file, runMeter, groupStatistics, edgeFactory)));
			} else {
				if (worker(tester, file, runMeter, groupStatistics, edgeFactory))
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

		for (it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<GlobalStatisticKey, SummaryStatistics> entry : groupStatistics.object2ObjectEntrySet()) {
			GlobalStatisticKey globalStatisticsKey = entry.getKey();
			tester.output.printf(GLOBAL_HEADER_ROW, entry.getValue().getN(), globalStatisticsKey.getNodes(), globalStatisticsKey.getContingent(),
					globalStatisticsKey.getPropositions(),
					entry.getValue().getMean(), entry.getValue().getStandardDeviation());
		}

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

	@SuppressWarnings({ "javadoc" })
	private static CSTN makeCSTNInstance(Checker tester, LabeledIntGraph g) {
		if (tester.cstnType == CstnType.cstnu) {
			if (tester.potential)
				return new CSTNUPotential(g, tester.timeOut);
			if (tester.cstnu2cstn)
				return new CSTNU2CSTN(g, tester.timeOut);
			return new CSTNU(g, tester.timeOut, tester.onlyLPQR0QR3OrToZ);
		}
		if (tester.cstn2cstn0) {
			return new CSTN2CSTN0(tester.reactionTime, g, tester.timeOut);
		}
		CSTN cstn;
		switch (tester.dcSemantics) {
		case ε:
			if (tester.onlyLPQR0QR3OrToZ) {
				cstn = (tester.woNodeLabels) ? new CSTNEpsilon3RwoNodeLabels(tester.reactionTime, g, tester.timeOut)
						: new CSTNEpsilon3R(tester.reactionTime, g, tester.timeOut);
			} else {
				cstn = (tester.woNodeLabels) ? new CSTNEpsilonwoNodeLabels(tester.reactionTime, g, tester.timeOut)
						: new CSTNEpsilon(tester.reactionTime, g, tester.timeOut);
			}
			break;
		case IR:
			if (tester.onlyLPQR0QR3OrToZ) {
				cstn = (tester.woNodeLabels) ? new CSTNIR3RwoNodeLabels(g, tester.timeOut) : new CSTNIR3R(g, tester.timeOut);
			} else {
				cstn = (tester.woNodeLabels) ? new CSTNIRwoNodeLabels(g, tester.timeOut) : new CSTNIR(g, tester.timeOut);
			}
			break;
		default:
			cstn = (tester.woNodeLabels) ? new CSTNwoNodeLabel(g, tester.timeOut) : new CSTN(g, tester.timeOut);
			break;
		}
		return cstn;
	}

	/**
	 * Parameter for asking to DC check epsilon DC reducing to IR DC
	 */
	@Option(required = false, name = "-cstn2cstn0", usage = "Check the epsilon-DC of the input instance using IR-DC on a correspondig streamlined CSTN. It transforms the input CSTN and then check its IR-DC. Epsilon value can be specified using -reactionTime parameter.")
	private boolean cstn2cstn0 = false;

	/**
	 * Parameter for asking to DC check epsilon DC reducing to IR DC
	 */
	@Option(required = false, name = "-cstnu2cstn", usage = "Check the CSTNU DC of the input instance reducing the check to the equivalent streamlined CSTN instance and checking this last one using IR-DC. It is incompatible with -potential option.")
	private boolean cstnu2cstn = false;

	/**
	 * Parameter for asking to DC check using potential
	 */
	@Option(required = false, name = "-potential", usage = "Check the DC of the input instance using potential DC algorithm (ONLY FOR CSTNU).")
	private boolean potential = false;

	/**
	 * Which type of CSTN are input files
	 */
	@Option(required = true, name = "-type", usage = "Specify if input files contain CSTN or CSTNU instances.")
	private CstnType cstnType = CstnType.cstn;

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
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, data will be added.", metaVar = "outputFile")
	private File outputFile = null;

	/**
	 * 
	 */
	private List<File> instances;

	/**
	 * Roberto: I verified that with such kind of computation, using more than one thread to check more files in parallel reduces the single performance!!!
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-nCPUs", usage = "Number of virtual CPUs that are reserved for this execution. Default is 0=no CPU reserved, there is only one thread for all the process. With more thread, the global performance increases, but each file can requires more time given to the fact that there is a competition among threads to access to he memory.")
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
	@Option(required = false, name = "-onlyLPQR0QR3", aliases = { "-limitToZ" }, usage = "Check DC considering only rules LP, qR0 and QR3.")
	private boolean onlyLPQR0QR3OrToZ = false;

	/**
	 * Output stream to outputFile
	 */
	private PrintStream output = null;

	/**
	 * Parameter for asking reaction time.
	 */
	@Option(required = false, name = "-reactionTime", depends = { "-semantics ε" }, usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Parameter for asking to remove a value from all constraints.
	 */
	@Option(required = false, name = "-removeValue", usage = "Value to be removed from any edge. Default value is null.")
	private int removeValue = Constants.INT_NULL;

	/**
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-timeOut", usage = "Time in seconds. Default is 1200 s = 20 m")
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
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNRunningTime [options...] arguments...");
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
					+ "Copyright © 2017, Roberto Posenato");
			return true;
		}

		if (this.reactionTime <= 0) {
			System.err.println("Reaction time must be > 0");
			return false;
		}
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
			this.instances.add(new File(fileName));
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
		if (this.cstnType == CstnType.cstn) {
			this.output.println(OUTPUT_HEADER);
		} else {
			this.output.println(OUTPUT_HEADER_CSTNU);
		}
		return true;
	}
}