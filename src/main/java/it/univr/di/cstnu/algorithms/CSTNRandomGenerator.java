package it.univr.di.cstnu.algorithms;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import it.univr.di.Debug;
import it.univr.di.labeledvalue.Label;

/**
 * Allows one to build random CSTN instances specifying:
 * - # of wanted DC/NOT DC instances
 * And the following parameters that characterize each generated instance:
 * - # nodes
 * - # propositions
 * - # negative qLoops
 * - # nodes in each qLoop
 * - # observation nodes in each qLoop
 * - max weight for each edge
 * - probability to have an edge between any pair of nodes
 * The class generates the wanted instances, building each one randomly and, then, DC checking it for stating its DC property.
 *
 * @author posenato
 */
public class CSTNRandomGenerator {

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTNRandomGenerator.class.getName());

	/**
	 * Version of the class
	 */
	static public final String VERSIONandDATE = "Version 0 - November, 23 2018";

	/**
	 * Min number of nodes
	 */
	static final int MIN_NODES = 20;

	/**
	 * Min number of nodes in a qLoop
	 */
	static final int MIN_NODES_QLOOP = 2;

	/**
	 * Min number of propositions
	 */
	static final int MIN_PROPOSITIONS = 2;

	/**
	 * Min max weight value
	 */
	static final int MIN_WEIGHT = 10;

	/**
	 * Number of wanted DC random CSTN instances.
	 */
	@Option(required = true, name = "--dcInstances", usage = "Number of wanted DC random CSTN instances.")
	int dcInstances = 10;

	/**
	 * Number of wanted NOT DC random CSTN instances.
	 */
	@Option(required = true, name = "--notDCInstances", usage = "Number of wanted NOT DC random CSTN instances.")
	int notDCInstances = 10;

	/**
	 * Number of nodes in each random CSTN instance.
	 */
	@Option(required = true, name = "--nodes", usage = "Number of nodes in each CSTN instance.")
	int nNodes = MIN_NODES;

	/**
	 * Number of propositions in each random CSTN instance.
	 */
	@Option(required = true, name = "--propositions", usage = "Number of propositions in each CSTN instance.")
	int nPropositions = MIN_PROPOSITIONS;

	/**
	 * Number of negative qLoops in each random CSTN instance.
	 */
	@Option(required = true, name = "--qLoops", usage = "Number of negative qLoops in each CSTN instance.")
	int nQLoops = MIN_PROPOSITIONS;

	/**
	 * Number of nodes in each qLoop.
	 */
	@Option(required = true, name = "--nodesInQLoop", usage = "Number of nodes in each qLoop.")
	int nNodesQLoop = MIN_NODES_QLOOP;

	/**
	 * Number of observation nodes in each qLoop.
	 */
	@Option(required = true, name = "--obsNodesInQLoop", usage = "Number of observation nodes in each qLoop.")
	int nObsQLoop = MIN_PROPOSITIONS;

	/**
	 * Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x])
	 */
	@Option(required = false, name = "--maxWeightValue", usage = "Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x]).")
	int maxWeight = MIN_WEIGHT * 5;

	/**
	 * The edge probability between any two nodes.
	 */
	@Option(required = false, name = "--edgeProb", usage = "The edge probability between any two nodes.")
	double edgeProb = .2;


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CSTNRandomGenerator generator = new CSTNRandomGenerator();

		generator.printVersion();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		if (!generator.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");


		System.out.println("Execution finished.");
	}


	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	@SuppressWarnings("deprecation")
	boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			
			if (this.nPropositions < 2 || this.nPropositions > Label.NUMBER_OF_POSSIBLE_PROPOSITIONS)
				throw new CmdLineException(parser,
						"The number of propositions is not valid. Valid range = [" + MIN_PROPOSITIONS + ", " + Label.NUMBER_OF_POSSIBLE_PROPOSITIONS + "].");

			if (this.nQLoops < 0 || this.nQLoops > this.nNodes / MIN_NODES_QLOOP)
				throw new CmdLineException(parser, "The number of qLoops is not valid. Valid range = [0, " + this.nNodes / MIN_NODES_QLOOP + "].");

			if (this.nNodesQLoop < MIN_NODES_QLOOP || this.nNodesQLoop > this.nNodes / this.nQLoops)
				throw new CmdLineException(parser,
						"The number of nodes in each qLoop is not valid. Valid range = [" + MIN_NODES_QLOOP + ", " + this.nNodes / this.nQLoops + "].");

			if (this.nNodes < MIN_NODES || this.nNodes < this.nQLoops * this.nNodesQLoop + this.nPropositions)
				throw new CmdLineException(parser,
						"The number of nodes is not valid. Value has to be greater than the sum of nodes of all qLoops and the number of propositions. Value "
								+ MIN_NODES + " is the minimum.");

			if (this.nObsQLoop < MIN_PROPOSITIONS)
				throw new CmdLineException(parser,
						"The number of propositions used in each qLoop is not valid. Valid range = [" + MIN_PROPOSITIONS + ", " + this.nPropositions + "].");

			if (this.maxWeight < MIN_WEIGHT)
				throw new CmdLineException(parser,
						"The maximum edge weight value is not valid. Valid range = [" + MIN_WEIGHT + ", " + Integer.MAX_VALUE + "].");

			if (this.edgeProb < 0 || this.edgeProb > 1.0)
				throw new CmdLineException(parser,
						"The edge probability is not valid. Valid range = [0.0, 1.0].");

		} catch (final CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass().getName() + " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-*.*.*-SNAPSHOT.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " file_name");
			return false;
		}
		return true;
	}

	/**
	 * Print version of the this class in System.out.
	 */
	public void printVersion() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		System.out.println(this.getClass().getName() + " " + VERSIONandDATE + ".\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2019, Roberto Posenato");
	}


}
