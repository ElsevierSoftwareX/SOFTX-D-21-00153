package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import com.google.common.io.Files;

import edu.uci.ics.jung.graph.util.Pair;
import it.unimi.dsi.fastutil.chars.CharArrayList;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.Literal;

/**
 * Allows one to build random CSTN instances specifying:
 * 
 * <pre>
 * - # of wanted DC/NOT DC instances
 * And the following parameters that characterize each generated instance:
 * - # nodes
 * - # propositions
 * - # negative qLoops
 * - # nodes in each qLoop
 * - # observation nodes in each qLoop
 * - max weight for each edge
 * - probability to have an edge between any pair of nodes
 * </pre>
 * 
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
	 * Base name for generated files
	 */
	static final String BASE_NAME = "cstn";

	/**
	 * Name of the root directory
	 */
	static final String DIR_NAME = "Instances";

	/**
	 * Name of sub dir containing DC instances
	 */
	static final String DC_SUB_DIR_NAME = "Consistent";

	/**
	 * Name of sub dir containing NOT DC instances
	 */
	static final String NOT_DC_SUB_DIR_NAME = "NotConsistent";

	/**
	 * Min number of nodes
	 */
	static final int MIN_NODES = 2;

	/**
	 * Min number of nodes in a qLoop
	 */
	static final int MIN_NODES_QLOOP = 2;

	/**
	 * Min number of propositions
	 */
	static final int MIN_PROPOSITIONS = 1;

	/**
	 * Min max weight value
	 */
	static final int MIN_WEIGHT = 10;

	/**
	 * Default negative value of each qLoop
	 */
	static final int QLOOP_VALUE = -1;

	/**
	 * Version of the class
	 */
	static public final String VERSIONandDATE = "Version 0 - November, 23 2018";

	/**
	 * @param args
	 * @throws IOException if results cannot be stored
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
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

		int notDCinstancesDone = 0;
		Pair<LabeledIntGraph> instances = null;
		// dcCstn, notDcCstn;

		String fileNamePrefix = generator.createFolders();

		int nDigits = (int) Math.floor(Math.log10(generator.dcInstances)) + 1;
		if (nDigits < 3)
			nDigits = 3;
		final String numberFormat = "%0" + nDigits + "d";

		CSTNUGraphMLWriter cstnWriter = new CSTNUGraphMLWriter(null);

		for (int dcInstancesDone = 0; dcInstancesDone < generator.dcInstances; dcInstancesDone++) {

			instances = generator.buildAPairRndCSTNInstances(notDCinstancesDone < generator.notDCInstances);

			// save the dc instance
			String fileName = "dc" + fileNamePrefix + "_" + String.format(numberFormat, dcInstancesDone) + ".cstn";
			try (PrintWriter writer = new PrintWriter(new File(generator.dcSubDir, fileName))) {
				cstnWriter.save(instances.getFirst(), writer);
			}

			if (notDCinstancesDone < generator.notDCInstances) {
				// save the NOT DC instance
				fileName = "notDC" + fileNamePrefix + "_" + String.format(numberFormat, dcInstancesDone) + ".cstn";
				try (PrintWriter writer = new PrintWriter(new File(generator.notDCSubDir, fileName))) {
					cstnWriter.save(instances.getSecond(), writer);
				}
				notDCinstancesDone++;
			}
		}
		System.out.println("Execution finished.");
	}

	/**
	 * Number of wanted DC random CSTN instances.
	 */
	@Option(required = true, name = "--dcInstances", usage = "Number of wanted DC random CSTN instances.")
	private int dcInstances = 10;

	/**
	 * Subdir containing DC instances
	 */
	private File dcSubDir;

	/**
	 * The edge probability between any two nodes.
	 */
	@Option(required = false, name = "--edgeProb", usage = "The edge probability between any two nodes.")
	private double edgeProb = .2;

	/**
	 * Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x])
	 */
	@Option(required = false, name = "--maxWeightValue", usage = "Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x]).")
	private int maxWeight = MIN_WEIGHT * 5;

	/**
	 * Number of nodes in each random CSTN instance.
	 */
	@Option(required = true, name = "--nodes", usage = "Number of nodes in each CSTN instance.")
	private int nNodes = MIN_NODES;

	/**
	 * Number of nodes in each qLoop.
	 */
	@Option(required = true, name = "--nodesInQLoop", usage = "Number of nodes in each qLoop.")
	private int nNodesQLoop = MIN_NODES_QLOOP;

	/**
	 * Number of observation nodes in each qLoop.
	 */
	@Option(required = true, name = "--obsNodesInQLoop", usage = "Number of observation nodes in each qLoop.")
	private int nObsQLoop = MIN_PROPOSITIONS;

	/**
	 * Number of propositions in each qLoop.
	 */
	@Option(required = true, name = "--propsInQLoop", usage = "Number of propositions in in each qLoop.")
	private int nPropsQLoop = MIN_PROPOSITIONS;

	/**
	 * Weight sum of each qLoop.
	 */
	@Option(required = false, name = "--qLoopValue", usage = "Weight sum of each qLoop.")
	private int qLoopValue = QLOOP_VALUE;

	/**
	 * Number of wanted NOT DC random CSTN instances.
	 */
	@Option(required = true, name = "--notDCInstances", usage = "Number of wanted NOT DC random CSTN instances.")
	private int notDCInstances = 10;

	/**
	 * Subdir containing not DC instances
	 */
	private File notDCSubDir;

	/**
	 * Number of propositions in each random CSTN instance.
	 */
	@Option(required = true, name = "--propositions", usage = "Number of propositions in each CSTN instance.")
	private int nPropositions = MIN_PROPOSITIONS;

	/**
	 * Number of negative qLoops in each random CSTN instance.
	 */
	@Option(required = true, name = "--qLoops", usage = "Number of negative qLoops in each CSTN instance.")
	private int nQLoops = MIN_PROPOSITIONS;


	/**
	 * Random generator used in the building of labels.
	 */
	private Random rnd = new Random(System.currentTimeMillis());

	/**
	 * It cannot be used outside.
	 */
	private CSTNRandomGenerator() {
	}

	/**
	 * @param dcInstances
	 * @param notDCInstances
	 * @param nodes
	 * @param propositions
	 * @param qLoops
	 * @param nodesInQloop
	 * @param obsInQLoop
	 * @param edgeProb
	 * @param maxWeight
	 * @throws IllegalArgumentException if one or more parameters has/have not valid value/s.
	 */
	public CSTNRandomGenerator(int dcInstances, int notDCInstances, int nodes, int propositions, int qLoops, int nodesInQloop, int obsInQLoop, double edgeProb,
			int maxWeight) throws IllegalArgumentException {
		this.dcInstances = dcInstances;
		this.notDCInstances = notDCInstances;
		this.nNodes = nodes;
		this.nPropositions = propositions;
		this.nQLoops = qLoops;
		this.nNodesQLoop = nodesInQloop;
		this.nObsQLoop = obsInQLoop;
		this.edgeProb = edgeProb;
		this.maxWeight = maxWeight;
		this.checkParameters();
	}

	/**
	 * Builds a pair of DC and not DC of CSTN instances using the building parameters.
	 * The not DC instance is build adding one or more constraints to the previous generated DC instance.
	 * 
	 * @param alsoNotDcInstance false if the not DC instances is required. If false, the returned not DC instance is an empty graph.
	 * @return a pair of DC and not DC of CSTN instances. if alsoNotDcInstance is false, the returned not DC instance is an empty graph.
	 */
	public Pair<LabeledIntGraph> buildAPairRndCSTNInstances(boolean alsoNotDcInstance) {

		LabeledIntGraph dcGraph = new LabeledIntGraph(LabeledIntTreeMap.class),
				notDCGraph = new LabeledIntGraph(LabeledIntTreeMap.class);

		// Add all node but Z (Z is not considered)
		double shift = 200, x = 0, y = 0;
		for (int i = 0; i < this.nNodes; i++) {
			LabeledNode node = new LabeledNode("n" + i);
			x += shift;
			node.setX(x);
			node.setY(y);
			if ((i % 5) == 0) {
				x = 0;
				y += shift;
			}
			dcGraph.addVertex(node);
			LOG.finer("Node added: " + node);
		}

		// qLoops contains, for each qLoop, the index of first and last node in the qLoop
		int[][] qLoopIndexes = new int[this.nQLoops][2];
		for (int i = 0; i < this.nQLoops; i++) {
			qLoopIndexes[i][0] = i * this.nNodesQLoop;
			qLoopIndexes[i][1] = (i + 1) * this.nNodesQLoop - 1;
		}
		LOG.finer("qLoopIndexes: " + Arrays.deepToString(qLoopIndexes));

		// Propositions
		char[] proposition = new char[this.nPropositions];
		for (char i = 'a'; i < 'a' + this.nPropositions; i++) {
			proposition[i - 'a'] = i;
		}
		LOG.finer("proposition: " + Arrays.toString(proposition));

		// Propositions used in each qLoop
		char[][] qLoopPropositions = new char[this.nQLoops][this.nPropsQLoop];
		int k = 0;
		for (int i = 0; i < this.nQLoops; i++) {
			for (int j = 0; j < this.nPropsQLoop; j++) {
				qLoopPropositions[i][j] = proposition[k++];
			}
		}
		LOG.finer("qLoopPropositions: " + Arrays.deepToString(qLoopPropositions));

		// Observation t.p. in qLoop
		CharList leftProposition = new CharArrayList(proposition);
		for (int i = 0; i < this.nQLoops; i++) {
			int indexNodeInQLoop = qLoopIndexes[i][0];
			CharSet qloopProp = new CharArraySet(qLoopPropositions[i]);
			for (int j = 0; j < this.nObsQLoop; j++) {
				// choose a proposition not belonging to the one associated to qLoop
				char p = ' ';
				boolean search = true;
				while (search) {
					k = this.rnd.nextInt(leftProposition.size());
					p = leftProposition.getChar(k);
					search = qloopProp.contains(p);
				}
				LabeledNode obs = dcGraph.getNode("n" + (indexNodeInQLoop + j));
				LOG.finer("Node in qLoop " + i + " transformed in obs: " + obs + "\tProposition: " + p);
				obs.setObservable(p);
				leftProposition.rem(p);
			}
		}

		// Remaining observation t.p.
		k = leftProposition.size();
		if (k > 0) {
			int firstIndexNodeNotInQLoop = qLoopIndexes[this.nQLoops-1][1]+1;
			if ((firstIndexNodeNotInQLoop+k) >= this.nNodes) {
				throw new IllegalStateException("No free nodes for setting remaining observation time points.");
			}
			LOG.finer("firstIndexNodeNotInQLoop: " + firstIndexNodeNotInQLoop
					+ "\nThe following nodes are transformed in obs ones and they stay outside qLoops.");
			for (; k > 0; k--) {
				LabeledNode obs = dcGraph.getNode("n" + firstIndexNodeNotInQLoop++);
				char p = leftProposition.get(k - 1);
				LOG.finer("Node transformed in obs: " + obs + "\tProposition: " + p);
				obs.setObservable(p);
			}
		}

		// Add all qLoops
		for (int i = 0; i < this.nQLoops; i++) {
			LOG.finer("Random generation of qLoop " + i);
			buildQLoop(dcGraph, qLoopIndexes[i][0], qLoopIndexes[i][1], qLoopPropositions[i]);
		}


		CSTN cstn = new CSTN(new LabeledIntGraph(dcGraph, dcGraph.getInternalLabeledValueMapImplementationClass()));

		try {
			CSTNCheckStatus status = cstn.dynamicConsistencyCheck();
			LOG.finer("RANDOM cstn is consistent? " + status.consistency);
		} catch (WellDefinitionException e) {
			e.printStackTrace();
		}

		// per ogni coppia di nodi non in qloop aggiungo un arco secondo la probabilità
		// per ogni coppia di nodi (nonin_qloop, in_qloop), aggiungo un arco secondo la probabilità
		return new Pair<>(dcGraph, notDCGraph);
	}

	/**
	 * Creates a negative qLoop among nodes having index in [firstIndex, lastIndex] choosing weights randomly using a normal distribution
	 * and associating to them random labels from proposition in qLoopPropositions.
	 * 
	 * @param g
	 * @param firstIndex
	 * @param lastIndex
	 * @param qLoopPropositions
	 */
	private void buildQLoop(LabeledIntGraph g, int firstIndex, int lastIndex, char[] qLoopPropositions) {
		// add an edge with a noraml distributed value and random label inconsistent with the last one.
		// for last edge, label must be inconsistent also with the following.
		NormalDistribution normalRnd = new NormalDistribution(0, 1.5);
		LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = g.getEdgeFactory();
		int sum = 0;
		int weight;
		Label label, firstLabel = Label.emptyLabel;
		Label previousLabel = Label.emptyLabel;
		LabeledIntEdge e;
		boolean oddEdges = ((this.nNodesQLoop % 2) != 0);
		for (int i = firstIndex; i < lastIndex; i++) {
			weight = ((int) Math.round(normalRnd.sample() * this.maxWeight)) % this.maxWeight;
			sum += weight;
			label = rndLabel(qLoopPropositions, previousLabel, null);
			if (i == firstIndex) {
				if (oddEdges) {
					while (label.size() < 2) {
						label = rndLabel(qLoopPropositions, previousLabel, null);
					}
				}
				firstLabel = label;
			}
			e = edgeFactory.get("n" + i + "-n" + (i + 1));
			e.mergeLabeledValue(label, weight);
			g.addEdge(e, g.getNode("n" + i), g.getNode("n" + (i + 1)));
			previousLabel = label;
			LOG.finer("Added edge: " + e);
		}

		//last edge
		weight = this.qLoopValue - sum;
		e = edgeFactory.get("n" + lastIndex + "-n" + (firstIndex));
		label = rndLabel(qLoopPropositions, previousLabel, firstLabel);
		e.mergeLabeledValue(label, weight);
		g.addEdge(e, g.getNode("n" + lastIndex), g.getNode("n" + (firstIndex)));
		LOG.finer("Added edge: " + e);
	}

	/**
	 * @param propositions that can be use for building the random label.
	 * @param previousLabel a not null label.
	 * @param nextLabel a possible null label.
	 * @return a random label that is inconsistent with previousLabel and nextLabel, if these last are significant
	 */
	private Label rndLabel(char[] propositions, Label previousLabel, Label nextLabel) {

		Label label = Label.emptyLabel;

		char l;
		char state;
		boolean isInconsitentWithPrevious = false;
		for (int i = 0; i < propositions.length; i++) {
			if (this.rnd.nextBoolean()) {
				l = propositions[i];
				state = this.rnd.nextBoolean() ? Literal.STRAIGHT : Literal.NEGATED;
				label = label.conjunction(l, state);
				if (previousLabel.isEmpty())
					continue;
				if (!isInconsitentWithPrevious) {
					if (!previousLabel.isEmpty() && label.isConsistentWith(previousLabel)) {
						Literal[] literals = label.getSubLabelIn(previousLabel, true).getLiterals();
						if (literals.length == 0)
							continue;
						Literal l1 = literals[0];
						label = label.remove(l1).conjunction(l1.getComplement());
					}
					isInconsitentWithPrevious = true;
				}
			}
		}
		if (label.isEmpty()) {
			// Since the choice of proposition is random, it may occur that no proposition is chosen.
			// label cannot be empty, so a literal is added!
			if (previousLabel.isEmpty()) {
				label = label.conjunction(
						Literal.valueOf(propositions[this.rnd.nextInt(propositions.length)], this.rnd.nextBoolean() ? Literal.STRAIGHT : Literal.NEGATED));
			} else {
				Literal l1 = previousLabel.getLiterals()[0];
				label = label.conjunction(l1.getComplement());
			}
		}

		if (nextLabel != null && !nextLabel.isEmpty() && label.isConsistentWith(nextLabel)) {
			// The following code works only if the nextLabel contains at least two literals
			boolean goOn = true;
			for (int i = 0; i < propositions.length && goOn; i++) {
				l = propositions[i];
				char stateInLabel = label.getState(l);
				char stateInPre = previousLabel.getState(l);
				char stateInNext = nextLabel.getState(l);

				if (stateInLabel == Literal.ABSENT) {
					if (stateInNext != Literal.ABSENT) {
						label = label.conjunction(Literal.valueOf(l, stateInNext).getComplement());
						goOn = false;
					}
				} else {
					if (stateInLabel == stateInPre || stateInPre == Literal.ABSENT) {
						if (stateInNext == stateInLabel) {
							label = label.remove(l).conjunction(Literal.valueOf(l, stateInNext).getComplement());
							goOn = false;
						}
					}
				}
			}
		}
		return label;
	}

	/**
	 * Creates the main directory and the two sub dirs that will contain the random instances.
	 * 
	 * @return prefix to use for creating file names
	 * @throws IOException
	 */
	private String createFolders() throws IOException {
		File baseDir = new File(DIR_NAME);

		if (baseDir.exists()) {
			File oldDir = new File(DIR_NAME + "_" + System.currentTimeMillis());
			Files.move(baseDir, oldDir);
		}
		baseDir.mkdirs();
		String suffix = "_" + this.nNodes + "nodes_" + this.nPropositions + "props_" + this.nQLoops + "qLoops_" + this.nNodesQLoop + "nodeQLoop";

		this.dcSubDir = new File(baseDir, DC_SUB_DIR_NAME + suffix);
		this.dcSubDir.mkdir();
		this.notDCSubDir = new File(baseDir, NOT_DC_SUB_DIR_NAME + suffix);
		this.notDCSubDir.mkdir();

		final String log = "Main directory where generated instances are saved: " + baseDir.getCanonicalPath()
				+ "\nSub dir for DC instances:\t\t" + this.dcSubDir.getPath()
				+ "\nSub dir for NOT DC instances:\t" + this.notDCSubDir.getPath();
		LOG.fine(log);
		System.out.println(log);

		return suffix;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			checkParameters();
		} catch (final CmdLineException | IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java " + this.getClass().getName() + " [options...]");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Required options: java -jar CSTNU-*.*.*-SNAPSHOT.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED));
			return false;
		}
		return true;
	}

	/**
	 * @throws IllegalArgumentException if a parameter is not valid.
	 */
	private void checkParameters() throws IllegalArgumentException {
		if (this.nPropositions < MIN_PROPOSITIONS || this.nPropositions > Label.NUMBER_OF_POSSIBLE_PROPOSITIONS)
			throw new IllegalArgumentException(
					"The number of propositions is not valid. Valid range = [" + MIN_PROPOSITIONS + ", " + Label.NUMBER_OF_POSSIBLE_PROPOSITIONS + "].");

		if (this.nQLoops < 0 || this.nQLoops > this.nNodes / MIN_NODES_QLOOP)
			throw new IllegalArgumentException("The number of qLoops is not valid. Valid range = [0, " + this.nNodes / MIN_NODES_QLOOP + "].");

		if (this.nNodesQLoop < MIN_NODES_QLOOP || this.nNodesQLoop > this.nNodes / this.nQLoops)
			throw new IllegalArgumentException(
					"The number of nodes in each qLoop is not valid. Valid range = [" + MIN_NODES_QLOOP + ", " + this.nNodes / this.nQLoops + "].");

		if ((this.nNodesQLoop % 2) != 0 && this.nPropsQLoop == 1)
			throw new IllegalArgumentException(
					"The number of nodes in each qLoop is not valid. It must be even when the number of propositions in qLoops is 1");

		if (this.nNodes < MIN_NODES || this.nNodes < this.nQLoops * this.nNodesQLoop + this.nPropositions)
			throw new IllegalArgumentException(
					"The number of nodes is not valid. Value has to be greater than the sum of nodes of all qLoops and the number of propositions. Value "
							+ MIN_NODES + " is the minimum.");

		if (this.nObsQLoop < 0 || this.nObsQLoop * this.nQLoops >= this.nPropositions) {
			int k = (this.nPropositions / this.nQLoops - 1);
			if (k == 0) {
				throw new IllegalArgumentException(
						"The number of observation time points in each qLoop is not valid because at least one observation time point must be set outside any qLoop.");
			}
			throw new IllegalArgumentException(
					"The number of observation time points in each qLoop is not valid. Valid range = [0, " + k + "].");
		}
		if (this.nPropsQLoop < 0 || this.nPropsQLoop * this.nQLoops > this.nPropositions)
			throw new IllegalArgumentException(
					"The number of propositions used in each qLoop is not valid. Valid range = [" + MIN_PROPOSITIONS + ", "
							+ (this.nPropositions / this.nQLoops) + "].");

		if (this.qLoopValue > QLOOP_VALUE)
			throw new IllegalArgumentException(
					"The negative value of each qLoop is not valid. It must be less or equal than " + QLOOP_VALUE);

		if (this.maxWeight < MIN_WEIGHT)
			throw new IllegalArgumentException(
					"The maximum edge weight value is not valid. Valid range = [" + MIN_WEIGHT + ", " + Integer.MAX_VALUE + "].");

		if (this.edgeProb < 0 || this.edgeProb > 1.0)
			throw new IllegalArgumentException(
					"The edge probability is not valid. Valid range = [0.0, 1.0].");

		if (this.dcInstances < 0 || this.dcInstances < this.notDCInstances)
			throw new IllegalArgumentException(
					"The number of wanted DC instances is not valid. It must positive and at least as notDCInstances value.");

		if (this.notDCInstances < 0 || this.dcInstances < this.notDCInstances)
			throw new IllegalArgumentException(
					"The number of wanted not DC instances is not valid. It must positive and at mosta as dcInstances value.");

	}

	/**
	 * Print version of the this class in System.out.
	 */
	public void printVersion() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		System.out.println(this.getClass().getName() + " " + VERSIONandDATE + ".\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2019, Roberto Posenato");
	}

	/**
	 * @return the dcInstances
	 */
	public int getDcInstances() {
		return this.dcInstances;
	}

	/**
	 * @return the edgeProb
	 */
	public double getEdgeProb() {
		return this.edgeProb;
	}

	/**
	 * @return the maxWeight
	 */
	public int getMaxWeight() {
		return this.maxWeight;
	}

	/**
	 * @return the nNodes
	 */
	public int getnNodes() {
		return this.nNodes;
	}

	/**
	 * @return the nNodesQLoop
	 */
	public int getnNodesQLoop() {
		return this.nNodesQLoop;
	}

	/**
	 * @return the nObsQLoop
	 */
	public int getnObsQLoop() {
		return this.nObsQLoop;
	}

	/**
	 * @return the notDCInstances
	 */
	public int getNotDCInstances() {
		return this.notDCInstances;
	}

	/**
	 * @return the notDCSubDir
	 */
	public File getNotDCSubDir() {
		return this.notDCSubDir;
	}

	/**
	 * @return the nPropositions
	 */
	public int getnPropositions() {
		return this.nPropositions;
	}

	/**
	 * @return the nQLoops
	 */
	public int getnQLoops() {
		return this.nQLoops;
	}

}
