package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
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
import it.unimi.dsi.fastutil.chars.CharList;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
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
	 * Version of the class
	 */
	// static public final String VERSIONandDATE = "Version 0 - November, 23 2018";
	// static public final String VERSIONandDATE = "Version 0.5 - November, 28 2018";
	static public final String VERSIONandDATE = "Version 0.6 - March, 29 2019";

	/**
	 * Checker
	 */
	static final Class<CSTNPotential> CSTN_CLASS = it.univr.di.cstnu.algorithms.CSTNPotential.class;

	/**
	 * Base name for generated files
	 */
	static final String BASE_NAME = "cstn";

	/**
	 * Name of sub dir containing DC instances
	 */
	static final String DC_SUB_DIR_NAME = "Consistent";

	/**
	 * Name of the root directory
	 */
	static final String DIR_NAME = "Instances";

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(CSTNRandomGenerator.class.getName());

	/**
	 * Min max weight value
	 */
	static final int MIN_MAX_WEIGHT = 50;

	/**
	 * Min number of nodes
	 */
	static final int MIN_NODES = 4;

	/**
	 * Min number of nodes in a qLoop
	 */
	static final int MIN_NODES_QLOOP = 2;

	/**
	 * Min number of propositions
	 */
	static final int MIN_PROPOSITIONS = 1;

	/**
	 * Name of sub dir containing NOT DC instances
	 */
	static final String NOT_DC_SUB_DIR_NAME = "NotConsistent";

	/**
	 * Default negative value of each qLoop
	 */
	static final int QLOOP_VALUE = -1;

	/**
	 * Default factor for modifying an edge
	 */
	static final double WEIGHT_MODIFICATION_FACTOR = .03d;

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
		if (!manageParameters(args, generator))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");

		String fileNamePrefix = createFolders(generator);
		createReadmeFiles(generator);

		int nDigits = (int) Math.floor(Math.log10(generator.dcInstances)) + 1;
		if (nDigits < 3)
			nDigits = 3;
		final String numberFormat = "%0" + nDigits + "d";

		CSTNUGraphMLWriter cstnWriter = new CSTNUGraphMLWriter(null);
		Pair<LabeledIntGraph> instances = null;
		int notDCinstancesDone = 0;

		for (int dcInstancesDone = 0; dcInstancesDone < generator.dcInstances; dcInstancesDone++) {

			instances = generator.buildAPairRndCSTNInstances(notDCinstancesDone < generator.notDCInstances);

			// save the dc instance
			String fileName = "dc" + fileNamePrefix + "_" + String.format(numberFormat, dcInstancesDone) + ".cstn";
			try (PrintWriter writer = new PrintWriter(new File(generator.dcSubDir, fileName))) {
				cstnWriter.save(instances.getFirst(), writer);
				System.out.println("DC instance " + fileName + " saved.");
			}

			if (notDCinstancesDone < generator.notDCInstances) {
				// save the NOT DC instance
				fileName = "notDC" + fileNamePrefix + "_" + String.format(numberFormat, dcInstancesDone) + ".cstn";
				try (PrintWriter writer = new PrintWriter(new File(generator.notDCSubDir, fileName))) {
					cstnWriter.save(instances.getSecond(), writer);
					System.out.println("NOT DC instance " + fileName + " saved.");
				}
				notDCinstancesDone++;
			}
		}
		System.out.println("Execution finished.");
	}

	/**
	 * Creates the main directory and the two sub dirs that will contain the random instances.
	 * 
	 * @param generator instance of this class containing all parameter values for building the prefix.
	 * @return prefix to use for creating file names
	 * @throws IOException if any directory cannot be created or moved.
	 */
	static private String createFolders(CSTNRandomGenerator generator) throws IOException {
		File baseDir = new File(DIR_NAME);

		if (!baseDir.exists()) {
			baseDir.mkdirs();
		}
		String suffix = "_" + generator.nNodes + "nodes_" + generator.nPropositions + "props_"
				+ generator.nQLoops + "qLoops_" + generator.nNodesQLoop + "nodeInQLoop_"
				+ generator.nPropsQLoop + "propInQLoop_" + generator.nObsQLoop + "obsInQLoop";

		generator.dcSubDir = new File(baseDir, DC_SUB_DIR_NAME + suffix);
		if (generator.dcSubDir.exists()) {
			File oldDir = new File(generator.dcSubDir.getAbsolutePath() + "_" + System.currentTimeMillis());
			Files.move(generator.dcSubDir, oldDir);
		}
		generator.dcSubDir.mkdir();

		generator.notDCSubDir = new File(baseDir, NOT_DC_SUB_DIR_NAME + suffix);
		if (generator.notDCSubDir.exists()) {
			File oldDir = new File(generator.notDCSubDir.getAbsolutePath() + "_" + System.currentTimeMillis());
			Files.move(generator.notDCSubDir, oldDir);
		}
		generator.notDCSubDir.mkdir();

		final String log = "Main directory where generated instances are saved: " + baseDir.getCanonicalPath()
				+ "\nSub dir for DC instances:\t\t" + generator.dcSubDir.getPath()
				+ "\nSub dir for NOT DC instances:\t" + generator.notDCSubDir.getPath();
		LOG.fine(log);
		System.out.println(log);

		return suffix;

	}

	/**
	 * Creates the two README files that describe the content of the two sub dir this.DCSubDir and this.notDCSubDir.
	 * 
	 * @param generator instance of this class containing all parameter values for building the prefix.
	 * @throws IOException if any file cannot be created.
	 */
	static private void createReadmeFiles(CSTNRandomGenerator generator) throws IOException {
		if (generator.dcSubDir == null || generator.notDCSubDir == null)
			return;

		String readmeText = "CSTN RANDOM INSTANCE BENCHMARK\n" +
				"==============================\n\n" +
				"This directory contains %04d %s CSTN random instances.\n" + // generator.dcInstances +
				"Each instance is built generating a number of negative q-loops and adding after a random number of edges\n " +
				"connecting q-loops and nodes in no q-loop.\n" +
				"The parameter values used for generating the instances are:\n" +
				"#nodes:\t\t\t\t" + String.format("%4d", generator.nNodes) + "\n" +
				"#propositions:\t\t" + String.format("%4d", generator.nPropositions) + "\n" +
				"#qLoops:\t\t\t" + String.format("%4d", generator.nQLoops) + "\n" +
				"#nodesInQLoop:\t\t" + String.format("%4d", generator.nNodesQLoop) + "\n" +
				"#propInQLoop:\t\t" + String.format("%4d", generator.nPropsQLoop) + "\n" +
				"#obsInQLoop:\t\t" + String.format("%4d", generator.nObsQLoop) + "\n" +
				"#nodeOutdegree:\t\t" + String.format("%4d", generator.outDegree) + "\n" +
				"#nodeInDegree:\t\t" + String.format("%4d", generator.inDegree) + "\n" +
				"#edgeProbability:\t" + String.format("%4.2f", generator.edgeProb) + "\n" +
				"#max weight:\t\t" + String.format("%4d", generator.maxWeight) + "\n\n" +
				"Legenda\n" +
				"-------\n" +
				"#qLoops: number of negative q-loops put in the instance.\n" +
				"#nodesInQLoop: number of nodes composing each q-loop.\n" +
				"#propInQLoop: number of propositions used for building labels in a q-loop.\n" +
				"#obsInQLoop: number of observation time points present in each q-loop.\n";

		try (PrintWriter writer = new PrintWriter(new File(generator.dcSubDir, "README"))) {
			writer.format(readmeText, generator.dcInstances, "dynamic consistent (DC)");
			writer.close();
		}
		try (PrintWriter writer = new PrintWriter(new File(generator.notDCSubDir, "README"))) {
			writer.format(readmeText, generator.notDCInstances, "NOT dynamic consistent (NOTDC)");
			writer.close();
		}
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args the arguments from the command line.
	 * @param generator the instance of generator in which the arguments are stored.
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	static private boolean manageParameters(final String[] args, CSTNRandomGenerator generator) {
		final CmdLineParser parser = new CmdLineParser(generator);
		try {
			// parse the arguments.
			parser.parseArgument(args);
			generator.checkParameters();
		} catch (final CmdLineException | IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java " + generator.getClass().getName() + " [options...]");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Required options: java -jar CSTNU-*.*.*-SNAPSHOT.jar " + generator.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED));
			return false;
		}
		return true;
	}

	/**
	 * @param s1
	 * @param s2
	 * @return an array that is the s1 / s2. s1 and s2 are not modified.
	 */
	static char[] setDifference(char[] s1, char[] s2) {
		char[] result = s1.clone();

		if (s2 == null || s2.length == 0)
			return result;
		int k = 0;
		for (int i = s1.length; i-- != 0;) {
			for (int j = s2.length; j-- != 0;) {
				if (s1[i] == s2[j]) {
					result[i] = 0;
					k++;
				}
			}
		}
		if (k == 0)
			return result;
		char[] result1 = new char[result.length - k];
		int j = 0;
		for (int i = 0; i < result.length; i++) {
			if (result[i] == 0)
				continue;
			result1[j++] = result[i];
		}
		return result1;
	}

	/**
	 * Number of wanted DC random CSTN instances.
	 */
	@Option(required = true, name = "--dcInstances", usage = "Number of wanted DC random CSTN instances.")
	private int dcInstances = 10;

	/**
	 * Subdir containing DC instances
	 */
	private File dcSubDir = null;

	/**
	 * The edge probability between any two nodes.
	 */
	@Option(required = false, name = "--edgeProb", usage = "The edge probability between any two nodes.")
	private double edgeProb = .1;

	/**
	 * The node in-degree
	 */
	@Option(required = false, name = "--inDegree", usage = "The maximal node indegree. If a node has such indegree, no incoming random edge can be added.")
	private int inDegree = 4;

	/**
	 * Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x])
	 */
	@Option(required = false, name = "--maxWeightValue", usage = "Max edge weight value (If x is the max weight value, the range for each CSTN link may be [-x, x]).")
	private int maxWeight = MIN_MAX_WEIGHT * 3;

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
	 * Number of wanted NOT DC random CSTN instances.
	 */
	@Option(required = true, name = "--notDCInstances", usage = "Number of wanted NOT DC random CSTN instances.")
	private int notDCInstances = 10;

	/**
	 * Subdir containing not DC instances
	 */
	private File notDCSubDir = null;

	/**
	 * Number of propositions in each random CSTN instance.
	 */
	@Option(required = true, name = "--propositions", usage = "Number of propositions in each CSTN instance.")
	private int nPropositions = MIN_PROPOSITIONS;

	/**
	 * Number of propositions in each qLoop.
	 */
	@Option(required = true, name = "--propsInQLoop", usage = "Number of propositions in in each qLoop.")
	private int nPropsQLoop = MIN_PROPOSITIONS;

	/**
	 * Number of negative qLoops in each random CSTN instance.
	 */
	@Option(required = true, name = "--qLoops", usage = "Number of negative qLoops in each CSTN instance.")
	private int nQLoops = MIN_PROPOSITIONS;

	/**
	 * The node out-degree
	 */
	@Option(required = false, name = "--outDegree", usage = "The maximal node outdegree. If a node has such outdegree, no outgoing random edge can be added.")
	private int outDegree = 4;

	/**
	 * Weight sum of each qLoop.
	 */
	@Option(required = false, name = "--qLoopValue", usage = "Weight sum of each qLoop.")
	private int qLoopValue = QLOOP_VALUE;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	int timeOut = 60 * 15;

	/**
	 * Random generator used in the building of labels.
	 */
	private Random rnd = new Random(System.currentTimeMillis());

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
	 * It cannot be used outside.
	 */
	private CSTNRandomGenerator() {
	}

	/**
	 * Builds a pair of DC and not DC of CSTN instances using the building parameters.
	 * The not DC instance is build adding one or more constraints to the previous generated DC instance.
	 * 
	 * @param alsoNotDcInstance false if the not DC instances is required. If false, the returned not DC instance is an empty graph.
	 * @return a pair of DC and not DC of CSTN instances. if alsoNotDcInstance is false, the returned not DC instance is an empty graph.
	 */
	public Pair<LabeledIntGraph> buildAPairRndCSTNInstances(boolean alsoNotDcInstance) {

		LabeledIntGraph randomGraph = new LabeledIntGraph(LabeledIntTreeMap.class),
				notDCGraph = new LabeledIntGraph(LabeledIntTreeMap.class);

		// Add all node but Z (Z is not considered)
		double shift = 200, x = 0, y = 0;
		int nodesInQloops = this.nNodesQLoop * this.nQLoops;
		int divisor;
		for (int i = 0; i < this.nNodes; i++) {
			LabeledNode node = new LabeledNode("n" + i);
			if (i != 0) {
				divisor = (i > nodesInQloops) ? 2 * this.nNodesQLoop : this.nNodesQLoop;
				if (i % divisor == 0) {
					x = 0;
					y += shift;
				}
			}
			x += shift;
			node.setX(x);
			node.setY((i % 2 == 0) ? y + 20 : y - 20);

			randomGraph.addVertex(node);
			LOG.finest("Node added: " + node);
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
		CharList qLoopProps = null;
		for (int i = 0; i < this.nQLoops; i++) {
			int indexNodeInQLoop = qLoopIndexes[i][0];
			qLoopProps = new CharArrayList(qLoopPropositions[i]);
			for (int j = 0; j < this.nObsQLoop; j++) {
				// choose a proposition not belonging to the one associated to qLoop
				char p = ' ';
				boolean search = true;
				while (search) {
					k = this.rnd.nextInt(leftProposition.size());
					p = leftProposition.getChar(k);
					search = qLoopProps.contains(p);
				}
				LabeledNode obs = randomGraph.getNode("n" + (indexNodeInQLoop + j));
				LOG.finer("Node in qLoop " + i + " transformed in obs: " + obs + "\tProposition: " + p);
				obs.setObservable(p);
				leftProposition.rem(p);
			}
		}

		// Remaining observation t.p.
		k = leftProposition.size();
		if (k > 0) {
			int firstIndexNodeNotInQLoop = qLoopIndexes[this.nQLoops - 1][1] + 1;
			if ((firstIndexNodeNotInQLoop + k) >= this.nNodes) {
				throw new IllegalStateException("No free nodes for setting remaining observation time points.");
			}
			LOG.finer("firstIndexNodeNotInQLoop: " + firstIndexNodeNotInQLoop
					+ "\nThe following nodes are transformed in obs ones and they stay outside qLoops.");
			for (; k > 0; k--) {
				LabeledNode obs = randomGraph.getNode("n" + firstIndexNodeNotInQLoop++);
				char p = leftProposition.getChar(k - 1);
				LOG.finer("Node transformed in obs: " + obs + "\tProposition: " + p);
				obs.setObservable(p);
			}
		}

		// Add all qLoops
		for (int i = 0; i < this.nQLoops; i++) {
			LOG.finer("Random generation of qLoop " + i);
			buildQLoop(randomGraph, qLoopIndexes[i][0], qLoopIndexes[i][1], qLoopPropositions[i]);
		}

		// For any pair of nodes, add an edge with probability this.edgeProb.
		// The edge value is a random positive value between 0 and this.maxValue
		// The label is a random label composed by propositions that are not in qLoop associate to nodes (if any).
		char[] propsWithout1Qloop, propsToUse = new char[0];
		LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = randomGraph.getEdgeFactory();
		ObjectList<LabeledIntEdge> addedEdges = new ObjectArrayList<>();
		LOG.finer("Starting adding edges among qLoops and free nodes");
		for (int i = 0; i < nodesInQloops; i++) {
			// one node in a qLoop, the other in other qLoop or free. I exploit the order of nodes!
			propsWithout1Qloop = setDifference(proposition, qLoopPropositions[i / this.nNodesQLoop]);
			for (int j = i + this.nNodesQLoop; j < this.nNodes; j++) {
				propsToUse = (j < nodesInQloops) ? setDifference(propsWithout1Qloop, qLoopPropositions[j / this.nNodesQLoop]) : propsWithout1Qloop;
				LOG.finest("propsToUse: " + Arrays.toString(propsToUse));
				addRndEdge(i, j, propsToUse, randomGraph, edgeFactory, addedEdges);
				addRndEdge(j, i, propsToUse, randomGraph, edgeFactory, addedEdges);
			}
		}
		for (int i = nodesInQloops; i < this.nNodes; i++) {
			for (int j = i + 1; j < this.nNodes; j++) {
				addRndEdge(i, j, proposition, randomGraph, edgeFactory, addedEdges);
				addRndEdge(j, i, proposition, randomGraph, edgeFactory, addedEdges);
			}
		}

		// Rename all obs nodes
		for (LabeledNode node : randomGraph.getVertices()) {
			if (node.isObserver()) {
				char p = node.getPropositionObserved();
				node.setName(String.valueOf(p).toUpperCase() + "?");
			}
		}

		LabeledIntGraph lastDC = randomGraph;

		CSTNUGraphMLWriter cstnWriter = new CSTNUGraphMLWriter(null);

		int checkN = 0;
		boolean notDCfound = false;
		CSTN cstn;
		try {
			cstn = CSTN_CLASS.getDeclaredConstructor(new Class[] { LabeledIntGraph.class, int.class }).newInstance(randomGraph, this.timeOut);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e2) {
			throw new RuntimeException("The class " + CSTN_CLASS + " for the checker is not available: " + e2.getMessage());
		}
		cstn.withNodeLabels = false;
		while (true) {
			try {
				cstn.setG(new LabeledIntGraph(randomGraph, randomGraph.getInternalLabeledValueMapImplementationClass()));
				checkN++;
				if (LOG.isLoggable(Level.FINER)) {
					try (PrintWriter writer = new PrintWriter(new File(this.dcSubDir.getParent(), "current.cstn"))) {
						cstnWriter.save(cstn.getG(), writer);
						LOG.finer("Current cstn saved as 'current.cstn' before checking.");
					} catch (IOException e) {
						LOG.finer("Problem to save 'current.cstn' " + e.getMessage() + "\n Program continues.");
					}
				}
				LOG.fine("DC Check started.");
				CSTNCheckStatus status = cstn.dynamicConsistencyCheck();
				LOG.fine("DC Check finished.");
				if (status.timeout) {
					String fileName = "timeOut" + System.currentTimeMillis() + ".cstn";
					LOG.finer("DC Check finished for timeout. Instance is saved as " + fileName + ".");
					File s = new File(this.dcSubDir.getParent(), "current.cstn");
					File d = new File(this.dcSubDir.getParent(), fileName);
					try {
						Files.move(s, d);
					} catch (IOException e) {
						LOG.finer("Problem to save 'current.cstn' as time out instance.\n Program continues.");
					}
					
					LOG.finer("The instance is modified changing edge values. Then, it will be re-checked.");
					int sign = (notDCfound) ? 1 : -1;
					for (LabeledIntEdge e : addedEdges) {
						Entry<Label> entry = e.getMinLabeledValue();
						e.mergeLabeledValue(entry.getKey(), (int) (entry.getIntValue() + sign * this.maxWeight * WEIGHT_MODIFICATION_FACTOR));
					}
				}
				if (!status.consistency) {
					if (checkN == 1) {
						LOG.finer("Original random instance is stored as the not DC instance that will be returned.");
						notDCGraph = new LabeledIntGraph(randomGraph, randomGraph.getInternalLabeledValueMapImplementationClass());
						notDCfound = true;
						LOG.finer("Original random instance is NOT DC. Weights will be increased!");
						for (LabeledIntEdge e : addedEdges) {
							Entry<Label> entry = e.getMinLabeledValue();
							e.removeLabeledValue(entry.getKey());
							e.mergeLabeledValue(entry.getKey(), (int) (entry.getIntValue() + this.maxWeight * WEIGHT_MODIFICATION_FACTOR));
						}
						checkN = 0;
					} else {
						LOG.finer("Found a not DC instance. The pair has been found after " + checkN + " iterations.");
						notDCGraph = new LabeledIntGraph(randomGraph, randomGraph.getInternalLabeledValueMapImplementationClass());
						return new Pair<>(lastDC, notDCGraph);
					}
				} else {
					lastDC = new LabeledIntGraph(randomGraph, randomGraph.getInternalLabeledValueMapImplementationClass());
					if (!notDCfound && alsoNotDcInstance) {
						LOG.finer("Original random instance is DC. Now, a not DC instance must be generated.");
						// we lower the edge values
						for (LabeledIntEdge e : addedEdges) {
							Entry<Label> entry = e.getMinLabeledValue();
							e.mergeLabeledValue(entry.getKey(), (int) (entry.getIntValue() - this.maxWeight * WEIGHT_MODIFICATION_FACTOR));
						}
					} else {
						LOG.finer("The pair has been found after " + checkN + " iterations.");
						return new Pair<>(lastDC, notDCGraph);
					}
				}
			} catch (WellDefinitionException e1) {
				e1.printStackTrace();
			}
		}
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

	/**
	 * Print version of the this class in System.out.
	 */
	public void printVersion() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		System.out.println(this.getClass().getName() + " " + VERSIONandDATE + ".\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2019, Roberto Posenato");
	}

	/**
	 * @param firstNodeIndex
	 * @param secondNodeIndex
	 * @param propsToUse
	 * @param g
	 * @param edgeFactory
	 * @param addedEdges
	 * @return the added edge, if added; null otherwise.
	 */
	private LabeledIntEdge addRndEdge(int firstNodeIndex, int secondNodeIndex, char[] propsToUse, LabeledIntGraph g,
			LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory, ObjectList<LabeledIntEdge> addedEdges) {
		boolean isEdgeToAdd = this.rnd.nextDouble() <= this.edgeProb;
		if (!isEdgeToAdd) {
			return null;
		}
		LabeledNode firstNode = g.getNode("n" + firstNodeIndex);
		LabeledNode secondNode = g.getNode("n" + secondNodeIndex);
		if (g.outDegree(firstNode) >= this.outDegree || g.inDegree(secondNode) >= this.inDegree) {
			return null;
		}

		Label label = rndLabel(propsToUse, null, null);
		int weight = this.rnd.nextInt(this.maxWeight);
		LabeledIntEdge e = edgeFactory.get("n" + firstNodeIndex + "-n" + (secondNodeIndex));
		e.mergeLabeledValue(label, weight);
		g.addEdge(e, firstNode, secondNode);
		addedEdges.add(e);
		LOG.finer("Added edge: " + e);
		return e;
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
		for (int i = firstIndex; i < lastIndex; i++) {
			weight = ((int) Math.round(normalRnd.sample() * this.maxWeight)) % this.maxWeight;
			sum += weight;
			label = rndLabel(qLoopPropositions, previousLabel, null);
			if (i == firstIndex) {
				if (this.nPropsQLoop > 1) {
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

		// last edge
		weight = this.qLoopValue - sum;
		e = edgeFactory.get("n" + lastIndex + "-n" + (firstIndex));
		label = rndLabel(qLoopPropositions, previousLabel, firstLabel);
		e.mergeLabeledValue(label, weight);
		g.addEdge(e, g.getNode("n" + lastIndex), g.getNode("n" + (firstIndex)));
		LOG.finer("Added edge: " + e);
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

		if (this.maxWeight < MIN_MAX_WEIGHT)
			throw new IllegalArgumentException(
					"The maximum edge weight value is not valid. Valid range = [" + MIN_MAX_WEIGHT + ", " + Integer.MAX_VALUE + "].");

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
	 * @param propositions that can be use for building the random label.
	 * @param previousLabel a possible null label. If it null or empty, then the returned label is just a random one!
	 * @param nextLabel a possible null label.
	 * @return a random label that is inconsistent with previousLabel and nextLabel, if these last are significant
	 */
	private Label rndLabel(char[] propositions, Label previousLabel, Label nextLabel) {

		Label label = Label.emptyLabel;

		if (previousLabel == null)
			previousLabel = Label.emptyLabel;
		if (nextLabel == null)
			nextLabel = Label.emptyLabel;

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
					if (label.isConsistentWith(previousLabel)) {
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

		if (!nextLabel.isEmpty() && label.isConsistentWith(nextLabel)) {
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

}
