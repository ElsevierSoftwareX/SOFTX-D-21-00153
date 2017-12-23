package it.univr.di.cstnu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.sql.Time;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

import edu.uci.ics.jung.io.GraphIOException;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
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
public class CSTNU2CSTNRunningTime {

	/**
	 * Version
	 */
	static final String VERSIONandDATE = "1, September, 27 2016";

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNU2CSTNRunningTime");

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
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file in CSV format. If file is already present, it is overwritten.", metaVar = "outputFile")
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
	 * Parameter for asking to not use Ω
	 */
	@Option(required = false, name = "-noUseΩ", usage = "Do not add and use Ω node.")
	private boolean noUseΩ = false;

	/**
	 * Parameter for asking timeout in sec.
	 */
	// @Option(required = false, name = "-timeOut", usage = "Time in seconds. Default is 1200 s = 20 m")
	// private int timeOut = 1200; // 20 min

	/**
	 * Parameter for asking reaction time.
	 */
	@Option(required = false, name = "-reactionTime", usage = "Reaction time. It must be >= 0. (not implemented yet)")
	private int reactionTime = 0;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Class for representing edge labeled values.
	 */
	final Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

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
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNU2CSTNRunningTime [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.algorithms.CSTNURunningTime" +
			// parser.printExample(OptionHandlerFilter.REQUIRED) +
			// " <CSTNU_file_name0> <CSTNU_file_name1>...");
			return false;
		}
		LOG.finest("File number: " + fileNameInput.length);
		LOG.finest("File names: " + Arrays.deepToString(fileNameInput));
		inputCSTNUFile = new File[fileNameInput.length];
		int i = 0;
		for (String fileName : fileNameInput) {
			inputCSTNUFile[i] = new File(fileName);
			if (!inputCSTNUFile[i].exists()) {
				System.err.println("File " + inputCSTNUFile[i] + " does not exit.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			i++;
		}
		LOG.finest("File: " + Arrays.deepToString(inputCSTNUFile));
		if (reactionTime < 0) {
			System.err.println("Reaction time must be ≥ 0");
			return false;
		}

		if (fOutput != null) {
			if (fOutput.isDirectory()) {
				System.err.println("Output file is a directory.");
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
			if (!fOutput.getName().endsWith(".csv")) {
				fOutput.renameTo(new File(fOutput.getAbsolutePath() + ".csv"));
			}
			if (fOutput.exists()) {
				fOutput.delete();
			}
			try {
				fOutput.createNewFile();
				output = new PrintStream(fOutput);
				output.println("\"CSTNU Name\""
						+ CSVSep + "#nodes "
						+ CSVSep + "#edges "
						+ CSVSep + "#obs "
						+ CSVSep + "#contingent "
						+ CSVSep + "minValue "
						+ CSVSep + "maxValue "
						+ CSVSep + "Dynamyc_Consistent "
						+ CSVSep + "average_time " + (timeInS ? "[s]" : "[ns]")
						+ CSVSep + "std._dev. " + (timeInS ? "[s]" : "[ns]"));
			} catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		}
		return true;
	}

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(String[] args) throws FileNotFoundException, GraphIOException {
		LOG.finest("Start...");
		System.out.println("Start of execution...");
		CSTNU2CSTNRunningTime tester = new CSTNU2CSTNRunningTime();

		if (!tester.manageParameters(args))
			return;

		LOG.finest("Parameters ok!");
		System.out.println("Parameters ok!");
		if (tester.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2016, Roberto Posenato");
			return;
		}

		CSTNU2CSTN cstnuByCSTNChecker = new CSTNU2CSTN(tester.reactionTime, !tester.noUseΩ, tester.labeledIntValueMap);
		CSTNU2CSTN.CSTNUCheckStatus status = new CSTNU2CSTN.CSTNUCheckStatus();
		LabeledIntGraph g = null;
		LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeFactory<>(cstnuByCSTNChecker.labeledIntValueMap);
		GraphMLReader<LabeledIntGraph> graphMLReader;
		GraphMLWriter graphWrite;
		SummaryStatistics globalSummaryStat = new SummaryStatistics(), localSummaryStat = new SummaryStatistics();
		// For each graph, solve it, save its times in an array
		for (File file : tester.inputCSTNUFile) {
			System.out.println("Analyzing file " + file.getName() + "...");
			LOG.fine("Loading " + file.getName() + "...");
			graphMLReader = new GraphMLReader<>(file, cstnuByCSTNChecker.labeledIntValueMap);
			g = graphMLReader.readGraph();
			LOG.fine("...done!");
			if (g == null) {
				System.err.println("File " + file.getName() + " does not contain a valid CSTNU instance. It has been ignored.");
				continue;
			}
			if (tester.cuttingEdgeFactor > 1 || tester.removeValue != Constants.INT_NULL) {

				if (tester.cuttingEdgeFactor > 1)
					System.out.println("Cutting all edge values by a factor " + tester.cuttingEdgeFactor + "...");
				if (tester.removeValue != Constants.INT_NULL)
					System.out.println("Removing all edge values equal to " + tester.removeValue + "...");
				if (tester.cuttingEdgeFactor > 1) {
					for (LabeledIntEdge e : g.getEdgesArray()) {
						LabeledIntEdgePluggable e1 = edgeFactory.create(e);
						for (Entry<Label> entry : e.labeledValueSet()) {
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
						LabeledIntEdge e1 = edgeFactory.create(e);
						for (Entry<Label> entry : e.labeledValueSet()) {
							int v = entry.getIntValue();
							if (v == value)
								continue;
							e1.mergeLabeledValue(entry.getKey(), entry.getIntValue() / tester.cuttingEdgeFactor);
						}
						((LabeledIntEdgePluggable) e).takeIn((LabeledIntEdgePluggable) e1);
					}
				}
				graphWrite = new GraphMLWriter(null);
				OutputStreamWriter writer = null;
				try {
					writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "_cutted"));
					graphWrite.save(g, writer);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} finally {
					try {
						if (writer != null)
							writer.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
			System.out.println("Min max edge value determination...");
			int min = Constants.INT_POS_INFINITE;
			int max = Constants.INT_NEG_INFINITE;
			for (LabeledIntEdge e : g.getEdgesArray()) {
				for (Entry<Label> entry : e.labeledValueSet()) {
					int v = entry.getIntValue();
					if (v > max)
						max = v;
					if (v < min)
						min = v;
				}
			}
			int nEdges = g.getEdgeCount();
			LabeledIntGraph cstnGraph = new LabeledIntGraph(cstnuByCSTNChecker.labeledIntValueMap);

			// I save the corresponding CSTN before DC checking
			OutputStreamWriter writer = null;
			try {
				cstnuByCSTNChecker.initUpperLowerLabelDataStructure();// it is necessary for transforming
			} catch (WellDefinitionException e) {
				String msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
						+ " is not a not well-defined instance. CSTNU has been ignored.\nError details:"
						+ e.getMessage();
				System.err.println(msg);
				LOG.warning(msg);
				continue;
			}
			cstnuByCSTNChecker.transform(g, cstnGraph);
			graphWrite = new GraphMLWriter(null);
			try {
				writer = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath() + "2.cstn"));
				graphWrite.save(cstnGraph, writer);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					if (writer != null)
						writer.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

			
			Double localAvg = Double.NaN, localStdDev = Double.NaN;
			if (!tester.noDCCheck) {
				String msg = (new Time(System.currentTimeMillis())).toString() + ": Determining DC check execution time of " + file.getName()
						+ " repeating DC check for " + tester.nDCRepetition + " times.";
				System.out.println(msg);
				LOG.fine(msg);

				
				boolean cstnOK = true;
				localSummaryStat.clear();
				localAvg = Double.NaN;
				localStdDev = Double.NaN;
				for (int j = 0; j < tester.nDCRepetition && cstnOK; j++) {
					LOG.fine("Test " + j + ", CSTNU: " + file.getName());
					// checking
					try {
						status = cstnuByCSTNChecker.dynamicControllabilityCheck(g, cstnGraph);
					} catch (WellDefinitionException ex) {
						msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
								+ " is not a not well-defined instance. CSTNU has been ignored.\nError details:"
								+ ex.getMessage();
						System.err.println(msg);
						LOG.warning(msg);
						cstnOK = false;
						continue;
					} catch (java.lang.IllegalArgumentException ex1) {
						msg = (new Time(System.currentTimeMillis())).toString() + ": " + file.getName()
								+ " is not checkable. CSTNU has been ignored.\nError details:"
								+ ex1.getMessage();
						System.err.println(msg);
						LOG.warning(msg);
						cstnOK = false;
						if (tester.output != null) {
							tester.output.printf("%s"
									+ CSVSep + "%d"
									+ CSVSep + "%d"
									+ CSVSep + "%d"
									+ CSVSep + "%d"
									+ CSVSep + "%d"
									+ CSVSep + "%d"
									+ CSVSep + "%s"
									+ "\n",
									file.getName(),
									g.getVertexCount(),
									nEdges,
									g.getPropositions().size(),
									g.getLowerLabeledEdges().size(),
									min,
									max,
									"-");
						}
						continue;
					}
					localSummaryStat.addValue(status.executionTimeNS);
				}
				msg = (new Time(System.currentTimeMillis())).toString() + ": done!";
				System.out.println(msg);
				LOG.fine(msg);
				if (!cstnOK)
					continue;
				localAvg = localSummaryStat.getMean();
				localStdDev = localSummaryStat.getStandardDeviation();
				if (tester.timeInS) {
					localAvg = localAvg / 1E9;
					localStdDev = localStdDev / 1E9;
				}
				LOG.fine(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
				LOG.fine(file.getName() + " is " + status.controllable);
				LOG.fine(file.getName() + " average required time " + (tester.timeInS ? "[s]: " : "[ns]: ") + localAvg);
				LOG.fine(file.getName() + " std. deviation " + (tester.timeInS ? "[s]: " : "[ns]: ") + localStdDev);
				// executor.shutdownNow();
			}
			if (tester.output != null) {
				tester.output.printf("%s"
						+ CSVSep + "%d"
						+ CSVSep + "%d"
						+ CSVSep + "%d"
						+ CSVSep + "%d"
						+ CSVSep + "%d"
						+ CSVSep + "%d"
						+ CSVSep + "%s"
						+ CSVSep + "%e"
						+ CSVSep + "%e"
						+ "\n",
						file.getName(),
						g.getVertexCount(),
						nEdges,
						g.getPropositions().size(),
						g.getLowerLabeledEdges().size(),
						min,
						max,
						((!tester.noDCCheck) ? (status.finished ? status.controllable : "not finished") : "-"),
						localAvg,
						localStdDev);
			}
			if (tester.output != null)
				globalSummaryStat.addValue(localSummaryStat.getMean());
		}

		if (tester.output != null) {
			System.out.println("Number of CSTNU checked: " + globalSummaryStat.getN());
			System.out.println("Average execution time: " + globalSummaryStat.getMean() + " ns (" + (globalSummaryStat.getMean() / 1E9) + " s)");
			System.out.println("Std. Deviation execution time: " + globalSummaryStat.getStandardDeviation() + " ns ("
					+ (globalSummaryStat.getStandardDeviation() / 1E9) + " s)");
		}

		if (tester.fOutput != null) {
			tester.output.close();
		}
	}

}
