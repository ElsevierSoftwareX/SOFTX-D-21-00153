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

/**
 * Simple class to determine the average execution time (and std dev) of the CSTN DC checking algorithm given a set of CSTNs.
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
	static final String VERSIONandDATE = "1.4, November, 09 2017";// code cleaned

	/**
	 * Represent a DC check task that can be interrupted by a timeout.
	 * 
	 * @author posenato
	 */
	public static class DCTask implements Callable<CSTNCheckStatus> {
		/**
		 * 
		 */
		CSTN cstnChecker;

		/**
		 * @param cstnChecker
		 */
		public DCTask(CSTN cstnChecker) {
			this.cstnChecker = cstnChecker;
		}

		@Override
		public CSTNCheckStatus call() throws WellDefinitionException {
			return this.cstnChecker.dynamicConsistencyCheck();
		}
	}

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
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, data will be added.", metaVar = "outputFile")
	private File fOutput = null;
	/**
	 * Parameter for asking reaction time.
	 */
	@Option(required = false, name = "-reactionTime", usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Parameter for asking DC semantics.
	 */
	@Option(required = false, name = "-semantics", usage = "DC semantics. Default is the std one.")
	private DCSemantics dcSemantics = DCSemantics.Std;

	/**
	 * Parameter for asking whether to consider node labels during the DC check.
	 */
	@Option(required = false, name = "-woNodeLabel", usage = "Check DC transforming the network in an equivalent CSTN without node labels.")
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
	private File[] inputCSTNFile;

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
		LOG.finest("File number: " + this.fileNameInput.length);
		LOG.finest("File names: " + Arrays.deepToString(this.fileNameInput));
		this.inputCSTNFile = new File[this.fileNameInput.length];
		int i = 0;
		for (String fileName : this.fileNameInput) {
			this.inputCSTNFile[i] = new File(fileName);
			if (!this.inputCSTNFile[i].exists()) {
				System.err.println("File " + this.inputCSTNFile[i] + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			i++;
		}
		LOG.finest("File: " + Arrays.deepToString(this.inputCSTNFile));

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

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

		LOG.finest("CSTNRunningTime " + VERSIONandDATE + "\nStart...");
		System.out.println("CSTNRunningTime " + VERSIONandDATE + "\nStart of execution...");
		CSTNRunningTime tester = new CSTNRunningTime();

		if (!tester.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");

		if (tester.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		LabeledIntGraph g = new LabeledIntGraph(labeledIntValueMap);
		CSTN cstn = null;
		if (tester.woNodeLabels) {
			if (tester.dcSemantics == DCSemantics.IR) {
				if (tester.onlyLPQR0QR3) {
					cstn = new CSTNir3RwoNodeLabel(g);
				} else {
					cstn = new CSTNirwoNodeLabel(g);
				}
			} else {
				cstn = new CSTNwoNodeLabel(g);
			}
		} else {
			if (tester.onlyLPQR0QR3) {
				cstn = new CSTNir3R(g);
			} else {
				switch (tester.dcSemantics) {
				case ε:
					cstn = new CSTNepsilon(tester.reactionTime, g);
					break;
				case IR:
					cstn = new CSTNir(g);
					break;
				default:
					cstn = new CSTN(g);
					break;
				}
			}
		}

		SummaryStatistics globalSummaryStat = new SummaryStatistics(), localSummaryStat = new SummaryStatistics();
		CSTNUGraphMLReader graphMLReader;
		LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeSupplier<>(labeledIntValueMap);
		ExecutorService executor = Executors.newSingleThreadExecutor(); // if tester.noDCCheck is true, executor will not be used!

		// For each graph, solve it, save its times in an array
		for (File file : tester.inputCSTNFile) {
			System.out.println("Analyzing file " + file.getName() + "...");
			LOG.fine("Loading " + file.getName() + "...");
			graphMLReader = new CSTNUGraphMLReader(file, labeledIntValueMap);
			g = graphMLReader.readGraph();
			LOG.fine("...done!");
			if (g == null) {
				System.err.println("File " + file.getName() + " does not contain a valid CSTN instance. It has been ignored.");
				continue;
			}

			if (tester.convertToNewFormat || tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {
				if (tester.convertToNewFormat)
					System.out.println("Convert the CSTN file to the recent format...");
				else {
					if (tester.cuttingEdgeFactor > 1) {
						System.out.println("Cutting all edge values by a factor " + tester.cuttingEdgeFactor + "...");
						for (LabeledIntEdge e : g.getEdgesArray()) {
							LabeledIntEdgePluggable e1 = edgeFactory.get(e);
							for (Entry<Label> entry : e.getLabeledValueSet()) {
								int v = entry.getIntValue() / tester.cuttingEdgeFactor;
								e1.mergeLabeledValue(entry.getKey(), v);
							}
							((LabeledIntEdgePluggable) e).takeIn(e1);
						}
					}
					if (tester.removeValue != Constants.INT_NULL) {
						System.out.println("Removing all edge values equal to " + tester.removeValue + "...");

						int value = tester.removeValue;
						for (LabeledIntEdge e : g.getEdgesArray()) {
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
					graphWrite.save(g, writer);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (tester.convertToNewFormat)
					continue;
			}

			// In order to start with well-defined cstn, we preliminary make a check.
			cstn.setG(g);
			String msg;
			try {
				cstn.initAndCheck();
			} catch (WellDefinitionException e) {
				msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
						+ " is not a not well-defined instance. CSTNU is ignored.\nError details:"
						+ e.getMessage();
				System.out.println(msg);
				LOG.severe(msg);
				continue;
			}
			int min = -cstn.maxWeight;
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
							+ CSVSep + "%d",
					file.getName(),
					g.getVertexCount(),
					nEdges,
					g.getObserverCount(),
					min,
					max);

			if (tester.noDCCheck) {
				tester.output.printf("\n");
				continue;
			}

			msg = (new Time(System.currentTimeMillis())).toString() + ": Determining DC check execution time of " + file.getName()
					+ " repeating DC check for " + tester.nDCRepetition + " times.";
			System.out.println(msg);
			LOG.info(msg);

			localSummaryStat.clear();

			boolean timeOut = false;
			boolean cstnOK = true;
			Future<CSTNCheckStatus> future;
			CSTNCheckStatus status = new CSTNCheckStatus();

			for (int j = 0; j < tester.nDCRepetition && cstnOK; j++) {
				LOG.fine("Test " + j + ", CSTN: " + file.getName());
				// It is necessary to reset the graph!
				graphMLReader = new CSTNUGraphMLReader(file, labeledIntValueMap);// reader must reload g.
				g = graphMLReader.readGraph();
				cstn.setG(g);

				future = executor.submit(new DCTask(cstn));

				try {
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
				} catch (Exception e) {
					msg = (new Time(System.currentTimeMillis())).toString() + ": a different exception has occurred. " + file.getName()
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

			msg = (new Time(System.currentTimeMillis())).toString() + ": done! It is " + ((!status.consistency) ? "NOT " : "") + "DC.";
			System.out.println(msg);
			LOG.info(msg);
			if (!cstnOK) {
				// There is a problem... in the stats we write TIMEOUT
				tester.output.printf(CSVSep + "%E"
						+ CSVSep + "%E"
						+ CSVSep + "%s"
						+ "\n",
						(double) tester.timeOut, 0.0, ((timeOut) ? "TIMEOUT after " + tester.timeOut + " seconds." : "Generic error. See log."));
				continue;
			}

			globalSummaryStat.addValue(localSummaryStat.getMean());

			Double localAvg = localSummaryStat.getMean();
			Double localStdDev = localSummaryStat.getStandardDeviation();
			if (tester.timeInS) {
				localAvg = localAvg / 1E9;
				localStdDev = localStdDev / 1E9;
			}
			LOG.info(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
			LOG.info(file.getName() + " is " + status.consistency);
			LOG.info(file.getName() + " average required time " + (tester.timeInS ? "[s]: " : "[ns]: ") + localAvg);
			LOG.info(file.getName() + " std. deviation " + (tester.timeInS ? "[s]: " : "[ns]: ") + localStdDev);

			tester.output.printf(CSVSep + "%E"
					+ CSVSep + "%E"
					+ CSVSep + "%s"
					+ CSVSep + "%d"
					+ CSVSep + "%d"
					// + CSVSep +"%d"
					// + CSVSep +"%d"
					+ CSVSep + "%d"
					// + CSVSep + "%d"
					// + CSVSep + "%d"
					+ "\n",
					localAvg,
					localStdDev,
					((!tester.noDCCheck) ? (status.finished ? status.consistency : "false") : "-"),
					status.labeledValuePropagationcalls,
					status.r0calls,
					// , status.r1calls
					// , status.r2calls
					status.r3calls
			// status.qAllNegLoop,
			// status.qSemiNegLoop
			);

		} // end for of files to process

		if (tester.fOutput != null) {
			tester.output.close();
		}
		if (!tester.convertToNewFormat) {
			System.out.println("\nFINAL REPORT\nNumber of CSTN checked: " + globalSummaryStat.getN());
			System.out.println("Average execution time: " + globalSummaryStat.getMean() + " ns (" + (globalSummaryStat.getMean() / 1E9) + " s)");
			System.out.println("Std. Deviation execution time: " + globalSummaryStat.getStandardDeviation() + " ns ("
					+ (globalSummaryStat.getStandardDeviation() / 1E9) + " s)");
		}
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
