//package it.univr.di.cstnu;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.PrintStream;
//import java.sql.Time;
//import java.util.Arrays;
//import java.util.logging.Logger;
//
//import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
//import org.kohsuke.args4j.Argument;
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;
//import org.kohsuke.args4j.Option;
//import org.kohsuke.args4j.spi.StringArrayOptionHandler;
//
//import edu.uci.ics.jung.io.GraphIOException;
//import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
//import it.univr.di.cstnu.CSTN_NodeSet.CSTNCheckStatus;
//import it.univr.di.cstnu.graph.GraphMLReader;
//import it.univr.di.cstnu.graph.GraphMLWriter;
//import it.univr.di.cstnu.graph.LabeledIntEdge;
//import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
//import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
//import it.univr.di.cstnu.graph.LabeledIntGraph;
//import it.univr.di.cstnu.graph.StaticLayout;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
//
///**
// * Simple class to determine the average execution time (and std dev) of the CSTN_NodeSet DC checking algorithm given a set of CSTNs.
// *
// * @author posenato
// * @version $Id: $Id
// */
//@Deprecated
//public class CSTNRunningTime_NodeSet {
//
//	/**
//	 * Version
//	 */
//	// static final String VERSIONandDATE = "1.0, March, 22 2015";
//	static final String VERSIONandDATE = "1.2, December, 18 2015";
//
//	/**
//	 * class logger
//	 */
//	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNRunningTime_NodeSet");
//
//	/**
//	 * CSV separator
//	 */
//	static final String CSVSep = ";\t";
//
//	/**
//	 * The input file names. Each file has to contain a CSTN_NodeSet graph in GraphML format.
//	 */
//	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTN_NodeSet graph in GraphML format.", metaVar = "CSTN_file_names", handler = StringArrayOptionHandler.class)
//	private String[] fileNameInput;
//
//	/**
//	 * Parameter for asking to determine a NON optimized DC checking.
//	 * 
//	 * @Option(required = false, name = "-excludeR1andR2rules", usage = "DC checking without using rules R1 and R2") private boolean excludeR1R2 = false;
//	 */
//
//	/**
//	 * Parameter for asking how many time to check the DC for each CSTN_NodeSet.
//	 */
//	@Option(required = false, name = "-numRepetitionDCCheck", usage = "Number of time to re-execute DC checking")
//	private int nDCRepetition = 30;
//
//	/**
//	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
//	 */
//	@Option(required = false, name = "-cuttingEdgeFactor", usage = "Cutting factor for reducing edge values. Default value is 1, i.e., no cut.")
//	private int cuttingEdgeFactor = 1;
//
//	/**
//	 * Parameter for asking how much to cut all edge values (for studying pseudo-polynomial characteristics)
//	 */
//	@Option(required = false, name = "-removeValue", usage = "Value to be removed from any edge. Default value is null.")
//	private int removeValue = Constants.INT_NULL;
//
//	/**
//	 * Parameter for avoiding DC check
//	 */
//	@Option(required = false, name = "-noDCCheck", usage = "Do not execute DC check. Just determine graph characteristics.")
//	private boolean noDCCheck = false;
//
//	/**
//	 * Parameter for asking time in sec instead of ns
//	 */
//	@Option(required = false, name = "-timeInS", usage = "Determine time in s instead of ns.")
//	private boolean timeInS = false;
//
//	/**
//	 * Output file where to write the determined experimental execution times in CSV format.
//	 */
//	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, it is overwritten.", metaVar = "outputFile")
//	private File fOutput = null;
//	/**
//	 * Parameter for asking reaction time.
//	 */
//	@Option(required = false, name = "-reactionTime", usage = "Reaction time. It must be > 0.")
//	private int reactionTime = 1;
//
//	/**
//	 * Output stream to fOutput
//	 */
//	private PrintStream output = null;
//
//	/**
//	 * Software Version.
//	 */
//	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
//	private boolean versionReq = false;
//
//	/**
//	 * 
//	 */
//	private File[] inputCSTNFile;
//
//	/**
//	 * Execution times
//	 */
//	long[] executionTime;
//
//	/**
//	 * Simple method to manage command line parameters using args4j library.
//	 * 
//	 * @param args
//	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
//	 */
//	private boolean manageParameters(String[] args) {
//		CmdLineParser parser = new CmdLineParser(this);
//		try {
//			parser.parseArgument(args);
//		} catch (CmdLineException e) {
//			// if there's a problem in the command line, you'll get this exception. this will report an error message.
//			System.err.println(e.getMessage());
//			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNRunningTime_NodeSet [options...] arguments...");
//			// print the list of available options
//			parser.printUsage(System.err);
//			System.err.println();
//
//			// print option sample. This is useful some time
//			// System.err.println("Example: java -jar CSTNRunningTime_NodeSet.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) +
//			// " <CSTN_file_name0> <CSTN_file_name1>...");
//			return false;
//		}
//		if (reactionTime <= 0) {
//			System.err.println("Reaction time must be ≥ 0");
//			return false;
//		}
//		LOG.finest("File number: " + fileNameInput.length);
//		LOG.finest("File names: " + Arrays.deepToString(fileNameInput));
//		inputCSTNFile = new File[fileNameInput.length];
//		int i = 0;
//		for (String fileName : fileNameInput) {
//			inputCSTNFile[i] = new File(fileName);
//			if (!inputCSTNFile[i].exists()) {
//				System.err.println("File " + inputCSTNFile[i] + " does not exit.");
//				parser.printUsage(System.err);
//				System.err.println();
//				return false;
//			}
//			i++;
//		}
//		LOG.finest("File: " + Arrays.deepToString(inputCSTNFile));
//
//		if (fOutput != null) {
//			if (fOutput.isDirectory()) {
//				System.err.println("Output file is a directory.");
//				parser.printUsage(System.err);
//				System.err.println();
//				return false;
//			}
//			if (!fOutput.getName().endsWith(".csv")) {
//				fOutput.renameTo(new File(fOutput.getAbsolutePath() + ".csv"));
//			}
//			if (fOutput.exists()) {
//				fOutput.renameTo(new File(fOutput.getAbsoluteFile() + ".old"));
//				fOutput.delete();
//			}
//			try {
//				fOutput.createNewFile();
//				output = new PrintStream(fOutput);
//			} catch (IOException e) {
//				System.err.println("Output file cannot be created: " + e.getMessage());
//				parser.printUsage(System.err);
//				System.err.println();
//				return false;
//			}
//		} else {
//			output = System.out;
//		}
//		output.println("\"CSTN_NodeSet Name\""
//				+ CSVSep + "#nodes"
//				+ CSVSep + "#edges"
//				+ CSVSep + "#propositions"
//				+ CSVSep + "#min edge value"
//				+ CSVSep + "#max edge value"
//				+ CSVSep + "average time " + (timeInS ? "[s]" : "[ns]")
//				+ CSVSep + "std. dev. " + (timeInS ? "[s]" : "[ns]")
//				+ CSVSep + "Dynamyc Consistent"
//				+ CSVSep + "#label propagation rule"
//				+ CSVSep + "#R0"
//				// + CSVSep + "#R1"
//				// + CSVSep + "#R2"
//				+ CSVSep + "#R3");
//		return true;
//	}
//
//	/**
//	 * <p>
//	 * main.
//	 * </p>
//	 *
//	 * @param args
//	 *            an array of {@link java.lang.String} objects.
//	 * @throws FileNotFoundException 
//	 * @throws GraphIOException 
//	 */
//	public static void main(String[] args) throws FileNotFoundException, GraphIOException {
//		LOG.finest("Start...");
//		System.out.println("Start of execution...");
//		CSTNRunningTime_NodeSet tester = new CSTNRunningTime_NodeSet();
//
//		if (!tester.manageParameters(args))
//			return;
//
//		LOG.finest("Parameters ok!");
//		System.out.println("Parameters ok!");
//		if (tester.versionReq) {
//			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
//					+ "Copyright © 2015, Roberto Posenato");
//			return;
//		}
//
//		CSTN_NodeSet cstn = new CSTN_NodeSet(tester.reactionTime);
//		CSTN_NodeSet.CSTNCheckStatus status = new CSTNCheckStatus();
//		@SuppressWarnings("unchecked")
//		LabeledIntEdgeFactory<LabeledIntNodeSetMap> edgeFactory = (LabeledIntEdgeFactory<LabeledIntNodeSetMap>) new LabeledIntEdgeFactory<>(cstn.labeledIntValueMapClass);
//
//		LabeledIntGraph g = null;
//
//		SummaryStatistics globalSummaryStat = new SummaryStatistics(), localSummaryStat = new SummaryStatistics();
//		// For each graph, solve it, save its times in an array
//		for (File file : tester.inputCSTNFile) {
//			System.out.println("Analyzing file " + file.getName() + "...");
//			LOG.fine("Loading " + file.getName() + "...");
//			GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(file, cstn.labeledIntValueMapClass);
//			g = graphMLReader.readGraph();
//
//			LOG.fine("...done!");
//			if (tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {
//
//				if (tester.cuttingEdgeFactor > 1)
//					System.out.println("Cutting all edge values by a factor " + tester.cuttingEdgeFactor + "...");
//				if (tester.removeValue != Constants.INT_NULL)
//					System.out.println("Removing all edge values equal to " + tester.removeValue + "...");
//				if (tester.cuttingEdgeFactor > 1) {
//					for (LabeledIntEdge e : g.getEdgesArray()) {
//						LabeledIntEdgePluggable e1 = edgeFactory.create(e.getName()); // = new LabeledIntEdge_NodeSet(e.getName(), e.getConstraintType());
//						e1.setConstraintType(e.getConstraintType());
//						for (Entry<Label> entry : e.labeledValueSet()) {
//							int v = entry.getIntValue() / tester.cuttingEdgeFactor;
//							e1.mergeLabeledValue(entry.getKey(), v);
//						}
//						// for (Entry<java.util.Map.Entry<Label, String>> entry : e.getLowerLabelSet()) {
//						// e1.mergeLowerLabelValue(entry.getKey().getKey(), entry.getKey().getValue(), entry.getIntValue() / tester.cuttingEdgeFactor);
//						// }
//						// for (Entry<java.util.Map.Entry<Label, String>> entry : e.getUpperLabelSet()) {
//						// e1.mergeUpperLabelValue(entry.getKey().getKey(), entry.getKey().getValue(), entry.getIntValue() / tester.cuttingEdgeFactor);
//						// }
//						((LabeledIntEdgePluggable) e).takeIn(e1);
//					}
//				}
//				if (tester.removeValue != Constants.INT_NULL) {
//					int value = tester.removeValue;
//					for (LabeledIntEdge e : g.getEdgesArray()) {
////						LabeledIntEdge_NodeSet e1 = new LabeledIntEdge_NodeSet(e.getName(), e.getConstraintType());
//						LabeledIntEdgePluggable e1 = edgeFactory.create(e.getName()); // = new LabeledIntEdge_NodeSet(e.getName(), e.getConstraintType());
//						e1.setConstraintType(e.getConstraintType());
//						for (Entry<Label> entry : e.labeledValueSet()) {
//							int v = entry.getIntValue();
//							if (v == value)
//								continue;
//							e1.mergeLabeledValue(entry.getKey(), entry.getIntValue() / tester.cuttingEdgeFactor);
//						}
//						((LabeledIntEdgePluggable) e).takeIn(e1);
//					}
//				}
//				GraphMLWriter graphWrite = new GraphMLWriter(new StaticLayout<>(g));
//				OutputStreamWriter writer = null;
//				try {
//					writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "_cutted"));
//					graphWrite.save(g, writer);
//				} catch (FileNotFoundException e1) {
//					e1.printStackTrace();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				} finally {
//					try {
//						if (writer != null)
//							writer.close();
//					} catch (IOException e1) {
//						e1.printStackTrace();
//					}
//				}
//			}
//			System.out.println("Min max edge value determination...");
//			int min = Constants.INT_POS_INFINITE;
//			int max = Constants.INT_NEG_INFINITE;
//			for (LabeledIntEdge e : g.getEdgesArray()) {
//				for (Entry<Label> entry : e.labeledValueSet()) {
//					int v = entry.getIntValue();
//					if (v > max)
//						max = v;
//					if (v < min)
//						min = v;
//				}
//			}
//			int nEdges = g.getEdgeCount();
//
//			Double localAvg = Double.NaN, localStdDev = Double.NaN;
//			if (!tester.noDCCheck) {
//				String msg = (new Time(System.currentTimeMillis())).toString() + ": Determining DC check execution time of " + file.getName()
//						+ " repeating DC check for " + tester.nDCRepetition + " times.";
//				System.out.println(msg);
//				LOG.fine(msg);
//				boolean cstnOK = true;
//				localSummaryStat.clear();
//				localAvg = Double.NaN;
//				localStdDev = Double.NaN;
//				for (int j = 0; j < tester.nDCRepetition && cstnOK; j++) {
//					LOG.fine("Test " + j + ", CSTN_NodeSet: " + file.getName());
//					if (j != 0) {
//						graphMLReader = new GraphMLReader<>(file, cstn.labeledIntValueMapClass);
//						g = graphMLReader.readGraph();
//					}
//					try {
//						status = cstn.dynamicConsistencyCheck(g);
//					} catch (WellDefinitionException e) {
//						msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
//								+ " has a problem about its definition. It has been ignored.\nError details:"
//								+ e.getMessage();
//						System.err.println(msg);
//						LOG.warning(msg);
//						cstnOK = false;
//						continue;
//					}
//					localSummaryStat.addValue(status.executionTimeNS);
//				}
//				msg = (new Time(System.currentTimeMillis())).toString() + ": done!";
//				System.out.println(msg);
//				LOG.fine(msg);
//				if (!cstnOK)
//					continue;
//				localAvg = localSummaryStat.getMean();
//				localStdDev = localSummaryStat.getStandardDeviation();
//				if (tester.timeInS) {
//					localAvg = localAvg / 1E9;
//					localStdDev = localStdDev / 1E9;
//				}
//				LOG.fine(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
//				LOG.fine(file.getName() + " is " + status.consistency);
//				LOG.fine(file.getName() + " average required time " + (tester.timeInS ? "[s]: " : "[ns]: ") + localAvg);
//				LOG.fine(file.getName() + " std. deviation " + (tester.timeInS ? "[s]: " : "[ns]: ") + localStdDev);
//			}
//			if (tester.output != null) {
//				tester.output.printf("%s"
//						+ CSVSep + "%d"
//						+ CSVSep + "%d"
//						+ CSVSep + "%d"
//						+ CSVSep + "%d"
//						+ CSVSep + "%d"
//						+ CSVSep + "%e"
//						+ CSVSep + "%e"
//						+ CSVSep + "%s"
//						+ CSVSep + "%d"
//						+ CSVSep + "%d"
//						// + CSVSep +"%d"
//						// + CSVSep +"%d"
//						+ CSVSep + "%d"
//						+ "\n",
//						file.getName(), g.getVertexCount(), nEdges, g.getPropositions().size(), min, max,
//						localAvg,
//						localStdDev,
//						((!tester.noDCCheck) ? (status.finished ? status.consistency : "false") : "-"),
//						status.labeledValuePropagationcalls,
//						status.r0calls,
//						// , status.r1calls
//						// , status.r2calls
//						status.r3calls);
//			}
//			if (tester.output != null)
//				globalSummaryStat.addValue(localSummaryStat.getMean());
//		}
//
//		if (tester.output != null) {
//			System.out.println("Number of CSTN_NodeSet checked: " + globalSummaryStat.getN());
//			System.out.println("Average execution time: " + globalSummaryStat.getMean() + " ns (" + (globalSummaryStat.getMean() / 1E9) + " s)");
//			System.out.println("Std. Deviation execution time: " + globalSummaryStat.getStandardDeviation() + " ns ("
//					+ (globalSummaryStat.getStandardDeviation() / 1E9) + " s)");
//		}
//	}
//
//}
