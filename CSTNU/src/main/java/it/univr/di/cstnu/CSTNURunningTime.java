package it.univr.di.cstnu;

import it.univr.di.cstnu.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.LabeledIntGraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.logging.Logger;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.spi.StringArrayOptionHandler;

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
	static final String VERSIONandDATE = "1.2, September, 29 2015";

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger("it.univr.di.cstnu.CSTNURunningTime");

	/**
	 * CSV separator
	 */
	static final char CSVSep = ';';

	/**
	 * The input file names. Each file has to contain a CSTNU graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "Input files. Each input file has to be a CSTNU graph in GraphML format.", metaVar = "CSTNU_file_names",
			handler = StringArrayOptionHandler.class)
	private String[] fileNameInput;

	/**
	 * Parameter for asking to determine a NON optimized DC checking.
	 */
	@Option(required = false, name = "-NOoptimized", usage = "DC checking without label optimization")
	private boolean noOptimized = false;

	/**
	 * Parameter for asking to determine a NON optimized DC checking.
	 * TODO si attiverà un giorno!
	 */
//	@Option(required = false, name = "-NOinstantaneousReaction", usage = "DC checking without assuming instantaneous reactions")
	private boolean noInstantaneousReaction = true;
	
	/**
	 * Parameter for asking how many time to check the DC for each CSTN.
	 */
	@Option(required = false, name = "-numRepetitionDCCheck", usage = "Number of time to re-execute DC checking")
	private int nDCRepetition = 30;

	/**
	 * Output file where to write the determined experimental execution times in CSV format.
	 */
	@Option(required = false, name = "-o", aliases = "--output"
			, usage = "Output to this file in CSV format. If file is already present, it is overwritten."
			, metaVar = "outputFile")
	private File fOutput = null;

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
		}
		catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNURunningTime [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			// System.err.println("Example: java -cp CSTNU-<version>.jar -cp it.univr.di.cstnu.CSTNURunningTime" + parser.printExample(OptionHandlerFilter.REQUIRED) +
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
						+ CSVSep + "\"average time (ms)\""
						+ CSVSep + "\"std. dev. (ms)\""
						+ CSVSep + "\"Dynamyc Consistent\""
						+ CSVSep + "\"#label propagation rule exec.\""
						+ CSVSep + "\"#R0\""
						+ CSVSep + "\"#R1\""
						+ CSVSep + "\"#R2\""
						+ CSVSep + "\"#R3\""
						+ CSVSep + "\"#StdRuleCall\""
						+ CSVSep + "\"#LowerCaseRuleCall\""
						+ CSVSep + "\"#UpperCaseRuleCall\""
						+ CSVSep + "\"#CrossCaseRuleCall\""
						+ CSVSep + "\"#CaseLabelRemovalRuleCall\""
						);
			}
			catch (IOException e) {
				System.err.println("Output file cannot be created: " + e.getMessage());
				parser.printUsage(System.err);
				System.err.println();
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>main.</p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		LOG.finest("Start...");
		CSTNURunningTime tester = new CSTNURunningTime();

		if (!tester.manageParameters(args)) return;

		LOG.finest("Parameters ok!");
		if (tester.versionReq) {
			System.out.print(tester.getClass().getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2015, Roberto Posenato");
			return;
		}

		CSTNU cstnu = new CSTNU(!tester.noOptimized, !tester.noInstantaneousReaction);
		CSTNUCheckStatus status = new CSTNUCheckStatus();
		LabeledIntGraph g = null;

		SummaryStatistics globalSummaryStat = new SummaryStatistics(), localSummaryStat = new SummaryStatistics();
		// For each graph, solve it, save its times in an array
		for (File file : tester.inputCSTNUFile) {
			LOG.fine("Loading " + file.getName() + "...");
			g = LabeledIntGraph.load(file);
			LOG.fine("...done!");
			if (g == null) {
				System.err.println("File " + file.getName() + " does not contain a valid CSTNU instance. It has been ignored.");
				continue;
			}
			LOG.fine("DC check " + file.getName() + "...");
			boolean cstnuOK = true;
			localSummaryStat.clear();
			for (int j = 0; j < tester.nDCRepetition && cstnuOK; j++) {
				LOG.fine("Test " + j + ", CSTNU: " + file.getName());
				if (j != 0) g = LabeledIntGraph.load(file);
				try {
					status = cstnu.dynamicControllabilityCheck(g);
				}
				catch (WellDefinitionException e) {
					System.err.println(file.getName() + " has a problem about its definition. It has been ignored.\nError details:" + e.getMessage());
					cstnuOK = false;
					continue;
				}
				localSummaryStat.addValue(status.executionTime);
			}
			LOG.fine("...done!");
			if (!cstnuOK) continue;
			LOG.fine(file.getName() + " has been checked (algorithm ends in a stable state): " + status.finished);
			LOG.fine(file.getName() + " is " + status.controllable);
			LOG.fine(file.getName() + " average required time (ms): " + localSummaryStat.getMean());
			LOG.fine(file.getName() + " std. deviation (ms): " + localSummaryStat.getStandardDeviation());
			if (tester.output != null) {
				tester.output.println("\"" + file.getName() + "\""
						+ CSVSep + localSummaryStat.getMean()
						+ CSVSep + localSummaryStat.getStandardDeviation()
						+ CSVSep + (status.finished ? status.controllable : "")
						+ CSVSep + status.r0calls
						+ CSVSep + status.r1calls
						+ CSVSep + status.r2calls
						+ CSVSep + status.r3calls
						+ CSVSep + status.stdRuleCalls
						+ CSVSep + status.lowerCaseRuleCalls
						+ CSVSep + status.upperCaseRuleCalls
						+ CSVSep + status.crossCaseRuleCalls
						+ CSVSep + status.caseLabelRemovalRuleCalls
						);
			}
			globalSummaryStat.addValue(localSummaryStat.getMean());
		}

		System.out.println("Number of CSTNU checked: " + globalSummaryStat.getN());
		System.out.println("Average execution time: " + globalSummaryStat.getMean());
		System.out.println("Std. Deviation execution time: " + globalSummaryStat.getStandardDeviation());
	}

}
