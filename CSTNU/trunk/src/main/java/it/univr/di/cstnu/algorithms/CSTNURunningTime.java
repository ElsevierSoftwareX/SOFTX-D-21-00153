package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
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
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to determine the average execution time (and std dev) of the CSTNU DC checking algorithm given a set of CSTNUs.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNURunningTime {
	/**
	 * Version
	 */
	// static final String VERSIONandDATE = "1.2, September, 29 2015";
	// static final String VERSIONandDATE = "1.3, December, 30 2015";
	// static final String VERSIONandDATE = "1.6, October, 05 2017";
	// static final String VERSIONandDATE = "1.8, October, 12 2017";
	static final String VERSIONandDATE = "1.9, October, 13 2017";// improved log of timeout instances

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNURunningTime");

	/**
	 * Represent a DC check task that can be interrupted by a timeout.
	 * 
	 * @author posenato
	 */
	public static class DCTask implements Callable<CSTNUCheckStatus> {
		/**
		 * 
		 */
		CSTNU cstnuChecker;

		/**
		 * @param cstnChecker
		 */
		public DCTask(CSTNU cstnChecker) {
			this.cstnuChecker = cstnChecker;
		}

		@Override
		public CSTNUCheckStatus call() throws WellDefinitionException {
			return this.cstnuChecker.dynamicControllabilityCheck();
		}
	}

	/**
	 * CSV separator
	 */
	static final char CSVSep = ';';

	/**
	 * The input file names. Each file has to contain a CSTNU graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTNU graph in GraphML format.", metaVar = "CSTNU_file_names", handler = StringArrayOptionHandler.class)
	private String[] fileNameInput;

	/**
	 * Parameter for asking how many time to check the DC for each CSTNU.
	 */
	@Option(required = false, name = "-numRepetitionDCCheck", usage = "Number of time to re-execute DC checking")
	private int nDCRepetition = 30;

	/**
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, data will be added.", metaVar = "outputFile")
	private File fOutput = null;

	/**
	 * time in seconds instead of ms.
	 */
	@Option(required = false, name = "-timeInS", usage = "Determine time in s instead of ns.")
	private boolean timeInS = false;

	/**
	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
	 */
	@Option(required = false, name = "-cuttingEdgeFactor", usage = "Cutting factor for reducing edge values. Default value is 1, i.e., no cut.")
	private int cuttingEdgeFactor = 1;

	/**
	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
	 */
	@Option(required = false, name = "-removeValue", usage = "Value to be removed from any edge. Default value is null.")
	private int removeValue = Constants.INT_NULL;

	/**
	 * Parameter for avoiding DC check
	 */
	@Option(required = false, name = "-noDCCheck", usage = "Do not execute DC check. Just determine graph characteristics.")
	private boolean noDCCheck = false;

	/**
	 * Parameter for asking timeout in sec.
	 */
	@Option(required = false, name = "-timeOut", usage = "Time in seconds. Default is 1800 s = 30 m")
	private int timeOut = 1800; // 30 min

	/**
	 * Parameter for asking reaction time.
	 */
	// @Option(required = false, name = "-reactionTime", usage = "Reaction time. It must be >= 0.")
	// private int reactionTime = 0;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Class for representing edge labeled values.
	 */
	final static Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private boolean versionReq = false;

	/**
	 * 
	 */
	private File[] inputCSTNUFile;

	/**
	 * Execution times
	 */
	long[] executionTime;

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
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNURunningTime [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNURunningTime" +
			// parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTNU_file_name0> <CSTNU_file_name1>...");
			return false;
		}
		LOG.finest("File number: " + this.fileNameInput.length);
		LOG.finest("File names: " + Arrays.deepToString(this.fileNameInput));
		this.inputCSTNUFile = new File[this.fileNameInput.length];
		int i = 0;
		for (String fileName : this.fileNameInput) {
			this.inputCSTNUFile[i] = new File(fileName);
			if (!this.inputCSTNUFile[i].exists()) {
				System.err.println("File " + this.inputCSTNUFile[i] + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			i++;
		}
		LOG.finest("File: " + Arrays.deepToString(this.inputCSTNUFile));
		// if (this.reactionTime < 0) {
		// System.err.println("Reaction time must be ≥ 0");
		// return false;
		// }

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
				this.output.println("\"CSTNUName\""
						+ CSVSep + "#nodes"
						+ CSVSep + "#edges"
						+ CSVSep + "#obs"
						+ CSVSep + "#ctg"
						+ CSVSep + "minValue"
						+ CSVSep + "maxValue"
						+ CSVSep + "DC"
						+ CSVSep + "RunningTimeMean" + (this.timeInS ? "[s]" : "[ns]")
						+ CSVSep + "RunningTimeStdDev" + (this.timeInS ? "[s]" : "[ns]")
						+ CSVSep + "#R0"
						// + CSVSep + "\"#R1\" "
						// + CSVSep + "\"#R2\" "
						+ CSVSep + "#R3"
						+ CSVSep + "#LNC"
						+ CSVSep + "#LowerCaseRuleCall"
						+ CSVSep + "#LUC+FLUC+LCUC"
						+ CSVSep + "#CrossCaseRuleCall"
						+ CSVSep + "#CaseLabelRemovalRuleCall"
				// + CSVSep + "#NegQLoop"
				// + CSVSep + "#SemiNegQLopp"
				);
			} catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		} else {
			this.output = System.out;
		}
		return true;
	}

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		CSTNURunningTime tester = new CSTNURunningTime();

		LOG.finest("Start...");
		System.out.println(tester.getClass().getName() + " " + VERSIONandDATE + "\nStart of execution...");

		if (!tester.manageParameters(args))
			return;

		LOG.finest("Parameters ok!");
		System.out.println("Parameters ok!");
		if (tester.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2016, Roberto Posenato");
			return;
		}

		LabeledIntGraph g;
		CSTNU cstnu;
		CSTNU.CSTNUCheckStatus status = new CSTNUCheckStatus();
		LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeSupplier<>(labeledIntValueMap);
		CSTNUGraphMLReader graphMLReader;
		ExecutorService executor = Executors.newSingleThreadExecutor(); // if tester.noDCCheck is true, executor will not be used!
		Future<CSTNUCheckStatus> future;
		SummaryStatistics globalSummaryStat = new SummaryStatistics(), localSummaryStat = new SummaryStatistics();

		// For each graph, solve it, save its times in an array
		for (File file : tester.inputCSTNUFile) {
			System.out.println("Analyzing file " + file.getName() + "...");
			LOG.fine("Loading " + file.getName() + "...");
			graphMLReader = new CSTNUGraphMLReader(file, labeledIntValueMap);
			g = graphMLReader.readGraph();
			LOG.fine("...done!");
			if (g == null) {
				System.err.println("File " + file.getName() + " does not contain a valid CSTN instance. It has been ignored.");
				continue;
			}

			if (tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {
				if (tester.cuttingEdgeFactor > 1)
					System.out.println("Cutting all edge values by a factor " + tester.cuttingEdgeFactor + "...");
				if (tester.removeValue != Constants.INT_NULL)
					System.out.println("Removing all edge values equal to " + tester.removeValue + "...");
				if (tester.cuttingEdgeFactor > 1) {
					for (LabeledIntEdge e : g.getEdgesArray()) {
						LabeledIntEdgePluggable e1 = edgeFactory.get(e);
						for (Entry<Label> entry : e.getLabeledValueSet()) {
							int v = entry.getIntValue() / tester.cuttingEdgeFactor;
							e1.mergeLabeledValue(entry.getKey(), v);
						}
						// for (Entry<java.util.Map.Entry<Label, String>> entry : e.getLowerLabelSet()) {
						// e1.mergeLowerLabelValue(entry.getKey().getKey(), entry.getKey().getValue(), entry.getIntValue() / tester.cuttingEdgeFactor);
						// }
						// for (Entry<java.util.Map.Entry<Label, String>> entry : e.getUpperLabelSet()) {
						// e1.mergeUpperLabelValue(entry.getKey().getKey(), entry.getKey().getValue(), entry.getIntValue() / tester.cuttingEdgeFactor);
						// }
						((LabeledIntEdgePluggable) e).takeIn(e1);
					}
				}
				if (tester.removeValue != Constants.INT_NULL) {
					int value = tester.removeValue;
					for (LabeledIntEdge e : g.getEdgesArray()) {
						LabeledIntEdge e1 = edgeFactory.get(e);
						for (Entry<Label> entry : e.getLabeledValueSet()) {
							int v = entry.getIntValue();
							if (v == value)
								continue;
							e1.mergeLabeledValue(entry.getKey(), entry.getIntValue() / tester.cuttingEdgeFactor);
						}
						((LabeledIntEdgePluggable) e).takeIn((LabeledIntEdgePluggable) e1);
					}
				}
				CSTNUGraphMLWriter graphWrite = new CSTNUGraphMLWriter(new StaticLayout<>(g));
				try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "_cutted"))) {
					graphWrite.save(g, writer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			System.out.println("Min max edge value determination...");
			// In order to determine the exac values of lower case edges, I exploit CSTNU init method
			cstnu = new CSTNU(g);
			String msg;
			try {
				cstnu.initAndCheck();
			} catch (WellDefinitionException e) {
				msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
						+ " is not a not well-defined instance. CSTNU is ignored.\nError details:"
						+ e.getMessage();
				System.out.println(msg);
				LOG.severe(msg);
				continue;
			}
			int min = -cstnu.maxWeight;
			int max = min;
			for (LabeledIntEdge e : g.getEdgesArray()) {
				for (Entry<Label> entry : e.getLabeledValueSet()) {
					int v = entry.getIntValue();
					if (v > max)
						max = v;
				}
			}
			int nEdges = g.getEdgeCount();

			tester.output.printf(
					"%s"
							+ CSVSep + "%d"
							+ CSVSep + "%d"
							+ CSVSep + "%d"
							+ CSVSep + "%d"
							+ CSVSep + "%d"
							+ CSVSep + "%d",
					file.getName(),
					g.getVertexCount(),
					nEdges,
					g.getObserverCount(),
					g.getContingentCount(),
					min,
					max);

			boolean cstnOK = true;
			Double localAvg = Double.NaN, localStdDev = Double.NaN;
			if (!tester.noDCCheck) {
				msg = (new Time(System.currentTimeMillis())).toString() + ": Determining DC check execution time of " + file.getName()
						+ " repeating DC check for " + tester.nDCRepetition + " times.";
				System.out.println(msg);
				LOG.info(msg);
				localSummaryStat.clear();
				localAvg = Double.NaN;
				localStdDev = Double.NaN;
				for (int j = 0; j < tester.nDCRepetition && cstnOK; j++) {
					LOG.fine("Test " + j + ", CSTNU: " + file.getName());
					// It is necessary to reset the graph even for the first time becuse it has been already initialized!
					graphMLReader = new CSTNUGraphMLReader(file, labeledIntValueMap);// to be sure that the reader reloads the graph!
					g = graphMLReader.readGraph();
					g.setFileName(file);
					cstnu.setG(g);

					future = executor.submit(new DCTask(cstnu));
					try {
						// status = cstnu.dynamicControllabilityCheck();
						status = future.get(tester.timeOut, TimeUnit.SECONDS);
					} catch (CancellationException | InterruptedException | ExecutionException | TimeoutException ex) {
						msg = (new Time(System.currentTimeMillis())).toString() + ": timeout has occurred. " + file.getName()
								+ " CSTNU is ignored.\nError details:"
								+ ex.getMessage();
						System.out.println(msg);
						LOG.severe(msg);
						cstnOK = false;
						status.consistency = false;
						continue;
					}
					localSummaryStat.addValue(status.executionTimeNS);
				}
				msg = (new Time(System.currentTimeMillis())).toString() + ": done! It is " + ((!status.consistency) ? "NOT " : "") + "DC.";
				System.out.println(msg);
				LOG.info(msg);
				if (!cstnOK) {
					// There is a problem... in the stats we write TIMEOUT
					tester.output.printf(CSVSep + "%E"
							+ CSVSep + "%E"
							+ CSVSep + "%s"
							+ "\n",
							(double) tester.timeOut, 0.0, "TIMEOUT after " + tester.timeOut + " seconds.");
					continue;
				}
				localAvg = localSummaryStat.getMean();
				localStdDev = localSummaryStat.getStandardDeviation();
				if (tester.timeInS) {
					localAvg = localAvg / 1E9;
					localStdDev = localStdDev / 1E9;
				}
				LOG.finer(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
				LOG.finer(file.getName() + " is " + status.consistency);
				LOG.finer(file.getName() + " average required time " + (tester.timeInS ? "[s]: " : "[ns]: ") + localAvg);
				LOG.finer(file.getName() + " std. deviation " + (tester.timeInS ? "[s]: " : "[ns]: ") + localStdDev);
			} // end DC check

			globalSummaryStat.addValue(localSummaryStat.getMean());
			if (cstnOK && !tester.noDCCheck) {
				tester.output.printf(
						CSVSep + "%s"
								+ CSVSep + "%e"
								+ CSVSep + "%e"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								+ CSVSep + "%d"
								// + CSVSep + "%d"
								// + CSVSep + "%d"
								+ "\n",
						(status.finished ? status.consistency : "false"),
						localAvg,
						localStdDev,
						status.r0calls,
						// , status.r1calls
						// , status.r2calls
						status.r3calls,
						status.labeledValuePropagationcalls,
						status.lowerCaseRuleCalls,
						status.upperCaseRuleCalls,
						status.crossCaseRuleCalls,
						status.letterRemovalRuleCalls
				// status.qAllNegLoop,
				// status.qSemiNegLoop
				);
			}
		} // end list of files to process

		if (tester.fOutput != null) {
			tester.output.close();
		}
		System.out.println("\nFINAL REPORT\nNumber of CSTNU checked: " + globalSummaryStat.getN());
		System.out.println("Average execution time: " + globalSummaryStat.getMean() + " ns (" + (globalSummaryStat.getMean() / 1E9) + " s)");
		System.out.println("Std. Deviation execution time: " + globalSummaryStat.getStandardDeviation() + " ns ("
				+ (globalSummaryStat.getStandardDeviation() / 1E9) + " s)");
		// executor shutdown!
		try {
			System.out.println("Shutdown executor");
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("Tasks interrupted");
		} finally {
			if (!executor.isTerminated()) {
				System.err.println("Cancel non-finished tasks");
			}
			executor.shutdownNow();
			System.out.println("Shutdown finished");
		}
	}
}