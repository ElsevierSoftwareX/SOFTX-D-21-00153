package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
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
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.CSTN.DCSemantics;
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
 * Simple class to determine the average execution time (and std dev) of the CSTN DC checking algorithm on a input set of CSTNs.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNRunningTime {
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
	static final String VERSIONandDATE = "2.2, November, 15 2017";// Added the possibility to test CSTNwoNodeLabelEpsilon and CSTN2CSTN0

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNRunningTime");

	/**
	 * CSV separator
	 */
	static final String CSVSep = ";";

	/**
	 * The input file names. Each file has to contain a CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTN graph in GraphML format.", metaVar = "CSTN_file_names", handler = StringArrayOptionHandler.class)
	private String[] fileNameInput;

	/**
	 * Parameter for asking to determine a NON optimized DC checking.
	 * 
	 * @Option(required = false, name = "-excludeR1andR2rules", usage = "DC checking without using rules R1 and R2") private boolean excludeR1R2 = false;
	 */

	/**
	 * Parameter for asking how many time to check the DC for each CSTN.
	 */
	@Option(required = false, name = "-numRepetitionDCCheck", usage = "Number of time to re-execute DC checking")
	private int nDCRepetition = 30;

	/**
	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
	 */
	@Option(required = false, name = "-cuttingEdgeFactor", usage = "Cutting factor for reducing edge values. Default value is 1, i.e., no cut.")
	private int cuttingEdgeFactor = 1;

	/**
	 * Parameter for asking to save in a recent format
	 */
	@Option(required = false, name = "-convertToNewFormat", usage = "Convert the file to the new format. When this option is given, no other options are possible.")
	private boolean convertToNewFormat = false;

	/**
	 * Parameter for asking to DC check epsilon DC reducing to IR DC
	 */
	@Option(required = false, name = "-cstn2cstn0", usage = "Check the epsilon-DC of the input instance using IR-DC on a correspondig streamlined CSTN. It transforms the input CSTN and then check its IR-DC. Epsilon value can be specified using -reactionTime parameter.")
	private boolean cstn2cstn0 = false;

	/**
	 * Parameter for asking to remove a value from all constraints.
	 */
	@Option(required = false, name = "-removeValue", usage = "Value to be removed from any edge. Default value is null.")
	private int removeValue = Constants.INT_NULL;

	/**
	 * Parameter for avoiding DC check
	 */
	@Option(required = false, name = "-noDCCheck", usage = "Do not execute DC check. Just determine graph characteristics.")
	private boolean noDCCheck = false;

	/**
	 * Parameter for asking time in sec instead of ns
	 */
	@Option(required = false, name = "-timeInS", usage = "Determine time in s instead of ns.")
	private boolean timeInS = false;

	/**
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-timeOut", usage = "Time in seconds. Default is 1200 s = 20 m")
	private int timeOut = 1200; // 20 min

	/**
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-nCPUs", usage = "Number of virtual CPUs that are reserved for this execution. Default is 1.")
	private int nCPUs = 1;

	/**
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, data will be added.", metaVar = "outputFile")
	private File fOutput = null;
	/**
	 * Parameter for asking reaction time.
	 */
	@Option(required = false, name = "-reactionTime", depends = "{-semantics ε}", usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Parameter for asking DC semantics.
	 */
	@Option(required = false, name = "-semantics", usage = "DC semantics. Possible values are: IR, ε, Std. Default is Std.")
	private DCSemantics dcSemantics = DCSemantics.Std;

	/**
	 * Parameter for asking whether to consider node labels during the DC check.
	 */
	@Option(required = false, name = "-woNodeLabels", usage = "Check DC transforming the network in an equivalent CSTN without node labels.")
	private boolean woNodeLabels = false;

	/**
	 * Parameter for asking whether to consider only Lp,qR0, qR3* rules.
	 */
	@Option(required = false, name = "-onlyLPQR0QR3", usage = "Check DC considering only rules LP, qR0 and QR3.")
	private boolean onlyLPQR0QR3 = false;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private boolean versionReq = false;

	/**
	 * Class for representing edge labeled values.
	 */
	final static Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

	/**
	 * 
	 */
	private List<File> inputCSTNFile;

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
			// System.err.println("Example: java -jar CSTNRunningTime.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTN_file_name0> <CSTN_file_name1>...");
			return false;
		}
		if (this.reactionTime <= 0) {
			System.err.println("Reaction time must be > 0");
			return false;
		}
		// LOG.finest("File number: " + this.fileNameInput.length);
		// LOG.finest("File names: " + Arrays.deepToString(this.fileNameInput));
		this.inputCSTNFile = new ArrayList<>(this.fileNameInput.length);
		for (String fileName : this.fileNameInput) {
			File file = new File(fileName);
			if (!file.exists()) {
				System.err.println("File " + fileName + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			this.inputCSTNFile.add(new File(fileName));
		}

		if (this.fOutput != null) {
			if (this.fOutput.isDirectory()) {
				System.err.println("Output file is a directory.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			// filename has to end with .csv
			if (!this.fOutput.getName().endsWith(".csv")) {
				this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".csv"));
			}
			try {
				this.output = new PrintStream(new FileOutputStream(this.fOutput, true), true);
			} catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		} else {
			this.output = System.out;
		}
		if (!this.convertToNewFormat) {
			this.output.println("\"CSTN Name\""
					+ CSVSep + "#nodes"
					+ CSVSep + "#edges"
					+ CSVSep + "#propositions"
					+ CSVSep + "#min edge value"
					+ CSVSep + "#max edge value"
					+ CSVSep + "average time " + (this.timeInS ? "[s]" : "[ns]")
					+ CSVSep + "std. dev. " + (this.timeInS ? "[s]" : "[ns]")
					+ CSVSep + "Dynamyc Consistent"
					+ CSVSep + "#label propagation rule"
					+ CSVSep + "#R0"
					// + CSVSep + "#R1"
					// + CSVSep + "#R2"
					+ CSVSep + "#R3"
			// + CSVSep + "#NegQLoop"
			// + CSVSep + "#SemiNegQLopp"
			);
		}
		return true;
	}

	@SuppressWarnings({ "javadoc" })
	private static class RunState {
		long startTime;
		long total;
		long current;

		/**
		 * @param startTime in milliseconds
		 * @param total
		 * @param current
		 */
		public RunState(long startTime, long total, long current) {
			this.current = current;
			this.total = total;
			this.startTime = startTime;
		}

		/**
		 */
		void printProgress() {
			this.current++;
			if (this.current > this.total)
				this.current = this.total;

			long now = System.currentTimeMillis();
			long eta = this.current == 0 ? 0 : (this.total - this.current) * (now - this.startTime) / this.current;

			String etaHms = this.current == 0 ? "N/A"
					: String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
							TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
							TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

			final int scaleFactor = 50;// [0--100]
			StringBuilder string = new StringBuilder(140);
			int percent = (int) (this.current * 100 / this.total);
			int percentScaled = (int) (this.current * scaleFactor / this.total);
			string.append('\r')
					.append(String.format("%s %3d%% [", new Time(now).toString(), percent))
					.append(String.join("", Collections.nCopies(percentScaled, "=")))
					.append('>')
					.append(String.join("", Collections.nCopies(scaleFactor - percentScaled, " ")))
					.append(']')
					.append(String.join("", Collections.nCopies((int) (Math.log10(this.total)) - (int) (Math.log10(this.current)), " ")))
					.append(String.format(" %d/%d, ETA: %s", this.current, this.total, etaHms));

			System.out.print(string);
		}

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
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		LOG.finest("CSTNRunningTime " + VERSIONandDATE + "\nStart...");
		System.out.println("CSTNRunningTime " + VERSIONandDATE + "\n" + (new Time(System.currentTimeMillis())).toString() + ": Start of execution...");
		final CSTNRunningTime tester = new CSTNRunningTime();

		if (!tester.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");

		if (tester.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

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
		int nProcessor = tester.nCPUs;// Runtime.getRuntime().availableProcessors();

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
		final ExecutorService cstnExecutor = Executors.newFixedThreadPool(nProcessor,
				new AffinityThreadFactory("cstnWorker", AffinityStrategies.DIFFERENT_CORE));

		RunState runState = new RunState(System.currentTimeMillis(), tester.inputCSTNFile.size(), 0);
		List<Future<Boolean>> future = new ArrayList<>();
		for (File file : tester.inputCSTNFile) {
			future.add(cstnExecutor.submit(() -> cstnWorker(tester, file, runState, edgeFactory)));
		}
		System.out.println((new Time(System.currentTimeMillis())).toString() + ": #Tasks queued: " + future.size());
		// wait all tasks have been finished and count!
		int nTaskSuccessfullyFinished = 0;
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
		String msg = "Number of instances processed successfully over total: " + nTaskSuccessfullyFinished + "/" + tester.inputCSTNFile.size() + ".";
		LOG.info(msg);
		System.out.println("\n" + (new Time(System.currentTimeMillis())).toString() + ": " + msg);
		// executor shutdown!
		try {
			System.out.println((new Time(System.currentTimeMillis())).toString() + ": Shutdown executors.");
			cstnExecutor.shutdown();
			cstnExecutor.awaitTermination(2, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println((new Time(System.currentTimeMillis())).toString() + ": Tasks interrupted.");
		} finally {
			if (!cstnExecutor.isTerminated()) {
				System.err.println((new Time(System.currentTimeMillis())).toString() + ": Cancel non-finished tasks.");
			}
			cstnExecutor.shutdownNow();
			System.out.println((new Time(System.currentTimeMillis())).toString() + ": Shutdown finished.\nExecution finished.");
		}
		if (tester.fOutput != null) {
			tester.output.close();
		}
	}

	/**
	 * @param tester
	 * @param file
	 * @param runState
	 * @param edgeFactory
	 * @return true if required task ends successfully, false otherwise.
	 */
	static private boolean cstnWorker(CSTNRunningTime tester, File file, RunState runState, LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory) {
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

		if (tester.convertToNewFormat || tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {
			if (tester.convertToNewFormat)
				LOG.info("Convert the CSTN file " + file.getName() + " to the recent format...");
			else {
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
								e.removeLabel(entry.getKey());
						}
					}
				}
			}
			String suffix = (tester.convertToNewFormat) ? "_converted" : "_cutted";
			CSTNUGraphMLWriter graphWrite = new CSTNUGraphMLWriter(null);
			try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + suffix));) {
				graphWrite.save(graphToCheck, writer);
			} catch (Exception e) {
				LOG.warning("File " + file.getAbsolutePath() + suffix + " cannot be written. Details: " + e.getMessage());
				return false;
			}
			if (tester.convertToNewFormat)
				return true;
		}

		// In order to start with well-defined cstn, we preliminary make a check.
		// Use g because next instructions change the structure of graph.
		LabeledIntGraph g = new LabeledIntGraph(graphToCheck, labeledIntValueMap);

		final CSTN cstn = makeCSTNInstance(tester, g);

		String msg;
		try {
			cstn.initAndCheck();
		} catch (Exception e) {
			msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
					+ " is not a not well-defined instance. Details:" + e.getMessage()
					+ "\nIgnored.";
			System.out.println(msg);
			LOG.severe(msg);
			return false;
		}
		int min = -cstn.getMaxWeight();
		int max = min;
		for (LabeledIntEdge e : g.getEdgesArray()) {
			for (Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v > max)
					max = v;
			}
		}
		int nEdges = g.getEdgeCount();

		String rowToWrite = String.format("%s"
				+ CSVSep + "%d"
				+ CSVSep + "%d"
				+ CSVSep + "%d"
				+ CSVSep + "%d"
				+ CSVSep + "%d",
				file.getName(),
				graphToCheck.getVertexCount(),
				nEdges,
				graphToCheck.getObserverCount(),
				min,
				max);

		if (tester.noDCCheck) {
			synchronized (tester.output) {
				tester.output.printf(rowToWrite + "\n");
			}
			runState.printProgress();
			return true;
		}

		msg = (new Time(System.currentTimeMillis())).toString() + ": Determining DC check execution time of " + file.getName()
				+ " repeating DC check for " + tester.nDCRepetition + " times.";
		// System.out.println(msg);
		LOG.info(msg);

		CSTNCheckStatus status = cstnDCChecker(tester, cstn, graphToCheck, runState);

		if (status == null || !status.finished) {
			// time out or generic error
			rowToWrite = String.format(rowToWrite
					+ CSVSep + "%E"
					+ CSVSep + "%E"
					+ CSVSep + "%s\n",
					(double) tester.timeOut,
					0.0,
					((status != null && status.executionTimeNS != Constants.INT_NULL) ? "Timeout of " + tester.timeOut + " seconds."
							: "Generic error. See log."));
			synchronized (tester.output) {
				tester.output.print(rowToWrite);
			}
			runState.printProgress();
			return false;
		}

		Double localAvg = (double) status.executionTimeNS;
		Double localStdDev = (double) status.stdDevExecutionTimeNS;
		if (tester.timeInS) {
			localAvg = localAvg / 1E9;
			localStdDev = localStdDev / 1E9;
		}
		LOG.info(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
		LOG.info(file.getName() + " is " + status.consistency);
		LOG.info(file.getName() + " average required time " + (tester.timeInS ? "[s]: " : "[ns]: ") + localAvg);
		LOG.info(file.getName() + " std. deviation " + (tester.timeInS ? "[s]: " : "[ns]: ") + localStdDev);

		rowToWrite = String.format(rowToWrite
				+ CSVSep + "%E"
				+ CSVSep + "%E"
				+ CSVSep + "%s"
				+ CSVSep + "%d"
				+ CSVSep + "%d"
				+ CSVSep + "%d"
				+ "\n",
				localAvg,
				localStdDev,
				((!tester.noDCCheck) ? (status.finished ? status.consistency : "false") : "-"),
				status.labeledValuePropagationCalls,
				status.r0calls,
				status.r3calls);
		synchronized (tester.output) {
			tester.output.print(rowToWrite);
		}
		runState.printProgress();
		return true;
	}

	/**
	 * @param tester
	 * @param cstn
	 * @param file
	 * @param executor
	 */
	@SuppressWarnings({ "javadoc" })
	static private CSTNCheckStatus cstnDCChecker(CSTNRunningTime tester, CSTN cstn, LabeledIntGraph graphToCheck, RunState runState) {
		String msg;
		boolean cstnOK = true;
		LabeledIntGraph g = null;
		CSTNCheckStatus status = new CSTNCheckStatus();
		SummaryStatistics localSummaryStat = new SummaryStatistics();
		for (int j = 0; j < tester.nDCRepetition && cstnOK && !status.timeout; j++) {
			LOG.info("Test " + (j + 1) + "/" + tester.nDCRepetition + " for CSTN " + graphToCheck.getFileName().getName());

			// It is necessary to reset the graph!
			g = new LabeledIntGraph(graphToCheck, labeledIntValueMap);
			cstn.setG(g);

			try {
				status = cstn.dynamicConsistencyCheck(tester.timeOut);
			} catch (CancellationException ex) {
				msg = (new Time(System.currentTimeMillis())).toString() + ": Cancellation has occurred. " + graphToCheck.getFileName() + " CSTNU is ignored.";
				System.out.println(msg);
				LOG.severe(msg);
				cstnOK = false;
				status.consistency = false;
				continue;
			} catch (Exception e) {
				msg = (new Time(System.currentTimeMillis())).toString() + ": a different exception has occurred. " + graphToCheck.getName()
						+ " CSTNU is ignored.\nError details:"
						+ e.getMessage();
				System.out.println(msg);
				LOG.severe(msg);
				cstnOK = false;
				status.consistency = false;
				continue;
			}
			localSummaryStat.addValue(status.executionTimeNS);
		} // end for checking repetition for a single file

		if (status.timeout || !cstnOK) {
			if (status.timeout) {
				msg = ("\n" + new Time(System.currentTimeMillis())).toString() + ": Timeout occurred. " + graphToCheck.getFileName() + " CSTNU is ignored.";
				System.out.println(msg);
			}
			return status;
		}

		msg = (new Time(System.currentTimeMillis())).toString() + ": done! It is " + ((!status.consistency) ? "NOT " : "") + "DC.";
		// System.out.println(msg);
		LOG.info(msg);

		status.executionTimeNS = (long) localSummaryStat.getMean();
		status.stdDevExecutionTimeNS = (long) localSummaryStat.getStandardDeviation();

		return status;
	}

	@SuppressWarnings({ "javadoc" })
	private static CSTN makeCSTNInstance(CSTNRunningTime tester, LabeledIntGraph g) {
		CSTN cstn;
		if (tester.cstn2cstn0) {
			return new CSTN2CSTN0(tester.reactionTime, g);
		}
		switch (tester.dcSemantics) {
		case ε:
			if (tester.onlyLPQR0QR3) {
				cstn = (tester.woNodeLabels) ? new CSTN3RwoNodeLabelEpsilon(tester.reactionTime, g) : null;
			} else {
				cstn = (tester.woNodeLabels) ? new CSTNwoNodeLabelEpsilon(tester.reactionTime, g) : new CSTNEpsilon(tester.reactionTime, g);
			}
			break;
		case IR:
			if (tester.onlyLPQR0QR3) {
				cstn = (tester.woNodeLabels) ? new CSTN3RwoNodeLabelIR(g) : new CSTN3RIR(g);
			} else {
				cstn = (tester.woNodeLabels) ? new CSTNwoNodeLabelIR(g) : new CSTNIR(g);
			}
			break;
		default:
			cstn = (tester.woNodeLabels) ? new CSTNwoNodeLabel(g) : new CSTN(g);
			break;
		}
		return cstn;
	}
}