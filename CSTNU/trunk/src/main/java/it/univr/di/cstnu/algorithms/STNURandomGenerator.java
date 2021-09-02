// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import com.google.common.io.Files;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.STNU.CheckAlgorithm;
import it.univr.di.cstnu.algorithms.STNU.STNUCheckStatus;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.LabeledNodeSupplier;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.labeledvalue.Constants;

/**
 * Allows one to build random STNU instances specifying:
 *
 * <ul>
 * <li>number of wanted DC/NOT DC instances</li>
 * </ul>
 * And the following parameters that characterize each generated instance:
 * <ul>
 * <li>number nodes
 * <li>number of contingent nodes
 * <li> max weight for each edge
 * <li> max weight for each contingent link (upper value)
 * <li> max in-degree for each node
 * <li> max out-degree for each node
 * <li> probability to have an edge between any pair of nodes
 * </ul>
 *
 * The class generates the wanted instances, building each one randomly and, then, DC checking it for stating its DC property.
 *
 * @author posenato
 * @version 2.1
 */
public class STNURandomGenerator {

	/**
	 * Version of the class
	 */
	// static public final String VERSIONandDATE = "Version 2.0 - April, 20 2020";
	static public final String VERSIONandDATE = "Version 2.1 - July, 17 2020";// switch to Rul2020 checking algorithm
	/**
	 * Base name for generated files
	 */
	static final String BASE_NAME = "stnu";

	/**
	 * Name of sub dir containing DC instances
	 */
	static final String DC_SUB_DIR_NAME = "Controllable";

	/**
	 * Name of the root directory
	 */
	static final String BASE_DIR_NAME = "Instances";

	/**
	 * Default edge name prefix
	 */
	static final String EDGE_NAME_PREFIX = "E";

	/**
	 * Default edge probability for each pair of nodes
	 */
	static final double EDGE_PROBABILITY = .2d;

	/**
	 * Default lane number
	 */
	static final int LANES = 5;

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(STNURandomGenerator.class.getName());

	/**
	 * Maximum checks for a network
	 */
	static final int MAX_CHECKS = 50;

	/**
	 * Max distance between lower and upper value in a contingent link
	 */
	static final int MAX_CONTINGENT_RANGE = 10;

	/**
	 * Max weight value
	 */
	static final int MAX_CONTINGENT_WEIGHT = 20;

	/**
	 * Min max weight value
	 */
	static final int MIN_MAX_WEIGHT = 150;

	/**
	 * Min number of nodes
	 */
	static final int MIN_NODES = 4;

	/**
	 * Default node name prefix
	 */
	static final String NODE_NAME_PREFIX = "N";

	/**
	 * Name of sub dir containing NOT DC instances
	 */
	static final String NOT_DC_SUB_DIR_NAME = "NotControllable";

	/**
	 * Default son probability
	 */
	static final double SON_PROBABILITY = .8d;

	/**
	 * Checker
	 */
	static final Class<STNU> STNU_CLASS = it.univr.di.cstnu.algorithms.STNU.class;

	/**
	 * Default factor for modifying an edge
	 */
	static final double WEIGHT_MODIFICATION_FACTOR = .04d;

	/**
	 * Default x shift for node position
	 */
	static final double X_SHIFT = 150d;

	/**
	 * Default y shift for node position
	 */
	static final double Y_SHIFT = 150d;

	/**
	 * <p>
	 * main.
	 * </p>
	 *
	 * @param args input args
	 * @throws java.io.FileNotFoundException if any.
	 * @throws java.io.IOException if any.
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		STNURandomGenerator generator = new STNURandomGenerator();
		System.out.println(generator.getVersionAndCopyright());
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

		String fileNamePrefix = generator.createFolders();
		generator.createReadmeFiles();

		final String numberFormat = makeNumberFormat(generator.dcInstances);

		TNGraphMLWriter stnuWriter = new TNGraphMLWriter(null);
		ObjectPair<TNGraph<STNUEdge>> instances = null;

		int notDCinstancesDone = 0;

		for (int dcInstancesDone = 0; dcInstancesDone < generator.dcInstances; dcInstancesDone++) {

			do {
				instances = generator.buildAPairRndTNInstances(notDCinstancesDone < generator.notDCInstances);
			} while (instances.getFirst() == null);

			// save the dc instance
			String indexNumber = String.format(numberFormat, (dcInstancesDone + generator.startingIndex));
			String fileName = "dc" + fileNamePrefix + "_" + indexNumber + ".stnu";
			File outputFile = getNewFile(generator.dcSubDir, fileName);
			stnuWriter.save(instances.getFirst(), outputFile);
			System.out.println("DC instance " + fileName + " saved.");
			// plain format
			fileName = "dc" + fileNamePrefix + "_" + indexNumber + ".plainStnu";
			outputFile = getNewFile(generator.dcSubDir, fileName);
			stnuPlainWriter(instances.getFirst(), outputFile);

			if (generator.dense) {
				// make the dense version
				TNGraph<STNUEdge> denseInstance = generator.makeDenseInstance(instances.getFirst());
				fileName = "dc" + fileNamePrefix + "_dense_" + indexNumber + ".stnu";
				outputFile = getNewFile(generator.dcSubDir, fileName);
				stnuWriter.save(denseInstance, outputFile);
				System.out.println("DC instance " + fileName + " saved.");
				fileName = "dc" + fileNamePrefix + "_dense_" + indexNumber + ".plainStnu";
				outputFile = getNewFile(generator.dcSubDir, fileName);
				stnuPlainWriter(denseInstance, outputFile);

			}

			if (notDCinstancesDone < generator.notDCInstances) {
				// save the NOT DC instance
				fileName = "notDC" + fileNamePrefix + "_" + indexNumber + ".stnu";
				outputFile = getNewFile(generator.notDCSubDir, fileName);
				stnuWriter.save(instances.getSecond(), outputFile);
				System.out.println("NOT DC instance " + fileName + " saved.");
				notDCinstancesDone++;
				fileName = "notDC" + fileNamePrefix + "_" + indexNumber + ".plainStnu";
				outputFile = getNewFile(generator.notDCSubDir, fileName);
				stnuPlainWriter(instances.getSecond(), outputFile);

				if (generator.dense) {
					// make the dense version
					TNGraph<STNUEdge> denseInstance = generator.makeDenseInstance(instances.getSecond());
					fileName = "notDC" + fileNamePrefix + "_dense_" + indexNumber + ".stnu";
					outputFile = getNewFile(generator.notDCSubDir, fileName);
					stnuWriter.save(denseInstance, outputFile);
					System.out.println("NOT DC instance " + fileName + " saved.");
					fileName = "notDC" + fileNamePrefix + "_dense_" + indexNumber + ".plainStnu";
					outputFile = getNewFile(generator.notDCSubDir, fileName);
					stnuPlainWriter(denseInstance, outputFile);
				}
			}
		}
		System.out.println("Execution finished.");
	}

	/**
	 * Given an instance, determines a dense version of it adding redundant edges according with {@link #density} and {@link #minNEdges} parameters.
	 * 
	 * @param instance
	 * @return the dense version
	 */
	private TNGraph<STNUEdge> makeDenseInstance(TNGraph<STNUEdge> instance) {
		if (instance == null)
			return null;
		LOG.finer("Making a dense instance. Required density: " + this.density + ". Required edges: " + this.minNEdges);

		TNGraph<STNUEdge> denseI = new TNGraph<>(instance, instance.getEdgeImplClass());
		int nCurrentEdges = denseI.getEdgeCount(), missingEdges = this.minNEdges - nCurrentEdges;
		LOG.finer("Current #edges: " + nCurrentEdges + ". Missing edges: " + missingEdges);

		int nNodeWithOutEdges = 0;
		for (LabeledNode node : denseI.getVertices()) {
			if (denseI.outDegree(node) > 0)
				nNodeWithOutEdges++;
		}
		if (missingEdges > 0) {
			int hop = 4;
			int edgesForNode = missingEdges / nNodeWithOutEdges;
			LOG.finer("#edges for node: " + edgesForNode);
			while (missingEdges > 0) {
				boolean stillPossibleToAdd = false;
				for (LabeledNode node : denseI.getVertices()) {
					int add = addRedundantNewEdges(node, null, 0, hop, edgesForNode, denseI);
					if (add != Constants.INT_NULL) {
						missingEdges -= add;
						stillPossibleToAdd = true;
						LOG.finer("Added #edges: " + add + ". Missing edges: " + missingEdges);
					}
					if (missingEdges <= 0)
						break;
				}
				if (!stillPossibleToAdd) {
					LOG.warning("It is not possible to add more edges! Givin up!");
					break;
				}
				hop++;
			}
		}
		return denseI;
	}

	/**
	 * Adds 'edgesForNode' redundant edges having source 'node'. For determining the destination node, it makes 'hop' hops.
	 * If all the edges cannot be added because already present or it is not possible making them after 'hop' hops, it tries to add the remaning making fewer
	 * hops.
	 * 
	 * @param source
	 * @param dest
	 * @param weight
	 * @param requiredEdges
	 * @param hop
	 * @param denseDc
	 * @return the number of edges added if it was possible. If there is no possibility to add edges because there not |paths| = hop, returns
	 *         {@link Constants#INT_NULL}.
	 */
	private int addRedundantNewEdges(LabeledNode source, LabeledNode dest, int weight, int hop, int requiredEdges, TNGraph<STNUEdge> denseDc) {
		if (requiredEdges <= 0)
			return 0;
		if (hop == 0) {
			STNUEdge e = denseDc.findEdge(source, dest);
			if (e == null) {
				STNUEdge newEdge = denseDc.getEdgeFactory().get(source.getName() + "-" + dest.getName());
				newEdge.setValue(weight + 10);
				denseDc.addEdge(newEdge, source, dest);
				LOG.finer("Added edge " + newEdge);
				return 1;
			}
			LOG.finer("Cannot add an edge because it is already present: " + e);
			return 0;
		}
		// I have to make a hop
		int added = Constants.INT_NULL;
		int w;
		if (dest == null)
			dest = source;
		if (denseDc.outDegree(dest) == 0) {
			LOG.finer("Node " + dest.getName() + " has no neighbours.");
			return added;
		}
		int rAdd = 0;
		for (STNUEdge edge : denseDc.getOutEdges(dest)) {
			if (added != Constants.INT_NULL && requiredEdges - added <= 0) {
				LOG.finer("No more edges are necessary!");
				break;
			}
			w = edge.getValue();
			if (w == Constants.INT_NULL) {
				LOG.finer("Edge is contingent, ignore. Details: " + edge);
				continue;// contingent link
			}
			LabeledNode nextNode = denseDc.getDest(edge);
			int stillRequiredEdges = (added == Constants.INT_NULL) ? requiredEdges : requiredEdges - added;
			rAdd = addRedundantNewEdges(source, nextNode, weight + w, hop - 1, stillRequiredEdges, denseDc);
			if (rAdd != Constants.INT_NULL) {
				if (added == Constants.INT_NULL)
					added = rAdd;
				else
					added += rAdd;
			}
		}
		if (added > 0) {
			LOG.finer("Added " + added + " edges.Return.");
			return added;
		}
		// since it was no possible to add at desired hop, I try to add here
		if (source != dest) {
			LOG.finer("No edges were added. Try to add with present node" + dest);
			if (denseDc.findEdge(source, dest) == null) {
				STNUEdge newEdge = denseDc.getEdgeFactory().get(source.getName() + "-" + dest.getName());
				newEdge.setValue(weight + 10);
				denseDc.addEdge(newEdge, source, dest);
				LOG.finer("Added edge " + newEdge);
				return 1;
			}
		}
		return added;
	}

	/**
	 * @param n
	 * @return a string format "%0&lt;i&gt;d" where <code>i</code> is the max between 3 and the digit number of <code>n</code>.
	 */
	private static String makeNumberFormat(int n) {
		int nDigits = (int) Math.floor(Math.log10(n)) + 1;
		if (nDigits < 3)
			nDigits = 3;
		return "%0" + nDigits + "d";
	}

	/**
	 * Saves the given stnu in file filename in the Hunsberger's format.<br>
	 * Hunsberger format example:
	 * 
	 * <pre>
	 * # KIND OF NETWORK
	 * STNU
	 * # Num Time-Points
	 * 5
	 * # Num Ordinary Edges
	 * 4
	 * # Num Contingent Links
	 * 2
	 * # Time-Point Names
	 * A0 C0 A1 C1 X
	 * # Ordinary Edges
	 * X 12 C0
	 * C1 11 C0
	 * C0 -7 X
	 * C0 -1 C1
	 * # Contingent Links
	 * A0 1 3 C0
	 * A1 1 10 C1
	 * ...
	 * </pre>
	 * 
	 * @param graph
	 * @param outputFile
	 * @return true if the instance was saved correctly. False otherwise.
	 */
	static private boolean stnuPlainWriter(TNGraph<STNUEdge> graph, File outputFile) {
		if (graph == null || outputFile == null)
			return false;
		LOG.finest("Start to save the instance in a plain format");

		try (PrintWriter writer = new PrintWriter(outputFile, "UTF-8")) {
			writer.println("# Nodes and contingent links saved in random order.");
			writer.println("# KIND OF NETWORK\nSTNU");
			writer.println("# Num Time-Points\n" + graph.getVertexCount());
			writer.println("# Num Ordinary Edges\n" + (graph.getEdgeCount() - 2 * graph.getContingentNodeCount()));
			writer.println("# Num Contingent Links\n" + graph.getContingentNodeCount());
			writer.println("# Time-Point Names");
			// Luke asked to save nodes in random order :/
			ObjectArrayList<LabeledNode> randomVertexList = new ObjectArrayList<>(graph.getVertices());
			Collections.shuffle(randomVertexList);
			for (LabeledNode node : randomVertexList) {
				writer.print("'" + node.getName() + "' ");
			}
			writer.println();
			writer.println("# Ordinary Edges");
			ObjectArrayList<STNUEdge> randomContingentList = new ObjectArrayList<>();
			for (STNUEdge edge : graph.getEdges()) {
				if (edge.isContingentEdge()) {
					if (edge.getValue() > 0)
						randomContingentList.add(edge);
					continue;
				}
				writer.println(
						"'" + graph.getSource(edge).getName() + "' " + Constants.formatInt(edge.getValue()) + " '" + graph.getDest(edge).getName() + "'");
			}
			writer.println("# Contingent Links");
			Collections.shuffle(randomContingentList);
			for (STNUEdge e : randomContingentList) {
				LabeledNode activation = graph.getSource(e);
				LabeledNode contingent = graph.getDest(e);
				STNUEdge lower = graph.findEdge(contingent, activation);
				writer.println("'" + activation.getName() + "' " + (-lower.getValue()) + " " + (e.getValue()) + " '" + contingent.getName() + "'");
			}
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/** 
	 * Base directory for saving the random instances.
	 */
	@Option(required=false, name="--baseOutputDir", usage = "Root directory where to create the subdirs containing the DC/notDC instance.")
	String baseDirName = BASE_DIR_NAME;
	
	/**
	 * Number of wanted DC random STNU instances.
	 */
	@Option(required = true, name = "--dcInstances", usage = "Number of wanted DC random STNU instances.")
	private int dcInstances = 10;

	/**
	 * Subdir containing DC instances
	 */
	private File dcSubDir = null;

	/**
	 * The edge probability between any two nodes.
	 */
	@Option(required = false, name = "--edgeProb", usage = "The edge probability between any two nodes in case of general random network.\n"
			+ "In case of tree random network, it is the probability of edge between nodes of the same level. This is equivalent to the density of the graph.")
	private double edgeProb = EDGE_PROBABILITY;

	/**
	 * The node in-degree
	 */
	@Option(required = false, name = "--inDegree", usage = "The maximal node indegree. If a node has such indegree, no incoming random edge can be added.")
	private int inDegree = 10;

	/**
	 * Max contingent edge weight value
	 */
	@Option(required = false, name = "--maxContingentWeightValue", usage = "Max contingent weight value.")
	private int maxContingentWeight = MAX_CONTINGENT_WEIGHT;

	/**
	 * Max contingent edge weight value
	 */
	@Option(required = false, name = "--maxContingentRange", usage = "Max contingent range between random upper and lower values.")
	private int maxContingentRange = MAX_CONTINGENT_RANGE;

	/**
	 * Max edge weight value (If x is the max weight value, the range for each STNU ordinary link may be [-x, x])
	 */
	@Option(required = false, name = "--maxWeightValue", usage = "Max edge weight value (If x is the max weight value, the range for each STNU ordinary link may be [-x, x]).")
	private int maxWeight = MIN_MAX_WEIGHT;

	/**
	 * Number of contingent nodes.
	 */
	@Option(required = true, name = "--ctgNodes", usage = "Number of contingent node in each STNU instance.")
	private int nCtgNodes = 1;

	/**
	 * Number of nodes in each random CSTN instance.
	 */
	@Option(required = true, name = "--nodes", usage = "Number of nodes in each STNU instance.")
	private int nNodes = MIN_NODES;

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
	 * Son number
	 */
	@Option(depends = { "--treeNetwork" }, required = false, name = "--sons", usage = "The maximum number of sons. It must be a value in [1, #nodes].")
	private int nSons = 2;

	/**
	 * The node out-degree
	 */
	@Option(required = false, name = "--outDegree", usage = "The maximal node outdegree. If a node has such outdegree, no outgoing random edge can be added.")
	private int outDegree = 10;

	/**
	 * Tree random network
	 */
	@Option(required = false, name = "--treeNetwork", usage = "The random network must be a tree having Z as root.")
	private boolean randomTree = false;

	/**
	 * Lane random network
	 */
	@Option(forbids = {
			"--treeNetwork" }, required = false, name = "--laneNetwork", usage = "The random network must be a graph like swimming lanes made by sequences of nodes connected by some random edges. It is incompatible with --treeNetwork.")
	private boolean randomLane = false;

	/**
	 * Dense random network
	 */
	@Option(required = false, name = "--dense", usage = "Complete the network making it dense. --density parameter says how much.")
	private boolean dense = false;

	/**
	 * The node out-degree
	 */
	@Option(required = false, depends = {
			"--dense" }, name = "--density", usage = "The density of the network. The network is made dense adding edges to it that does not chenge its DC property till a density index is satisfied.")
	private double density = 0.5d;

	/**
	 * Min ## edges
	 */
	private int minNEdges = 0;

	/**
	 * Pool random network
	 */
	@Option(depends = { "--laneNetwork" }, forbids = { "--treeNetwork" }, required = false, name = "--lanes", usage = "The number of swimming lanes.")
	private int lanes = LANES;

	/**
	 * Random generator used in the building of labels.
	 */
	private SecureRandom rnd = new SecureRandom();

	/**
	 * Son probability
	 */
	@Option(depends = { "--treeNetwork" }, required = false, name = "--sonProbability", usage = "The probability that in parent node can have a son.")
	private double sonProb = SON_PROBABILITY;

	/**
	 */
	@Option(required = false, name = "--startingIndex", usage = "Index of the first generated instance.")
	private int startingIndex = 0;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	private int timeOut = 60 * 15;

	/**
	 * weight adjustment. This value is determined in the constructor.
	 */
	private int weightAdjustment;

	/**
	 * Local temporary network
	 */
	private File tmpNetwork;

	/**
	 * <p>
	 * Constructor for STNURandomGenerator.
	 * </p>
	 *
	 * @param givenDcInstances a int.
	 * @param givenNotDCInstances a int.
	 * @param nodes a int.
	 * @param nCtgNodes1 a int.
	 * @param givenMaxContingentWeight a int.
	 * @param edgeProbability a double.
	 * @param givenMaxWeight a int.
	 * @throws java.lang.IllegalArgumentException if one or more parameters has/have not valid value/s.
	 * @throws java.lang.IllegalArgumentException if any.
	 */
	public STNURandomGenerator(int givenDcInstances, int givenNotDCInstances, int nodes, int nCtgNodes1, double edgeProbability, int givenMaxWeight,
			int givenMaxContingentWeight) throws IllegalArgumentException {
		this();
		this.dcInstances = givenDcInstances;
		this.notDCInstances = givenNotDCInstances;
		this.nNodes = nodes;
		this.nCtgNodes = nCtgNodes1;
		this.edgeProb = edgeProbability;
		this.maxWeight = givenMaxWeight;
		this.maxContingentWeight = givenMaxContingentWeight;
		this.checkParameters();// it is necessary!
	}

	/**
	 * It cannot be used outside.
	 */
	private STNURandomGenerator() {
		try {
			this.tmpNetwork = File.createTempFile("currentNetwork", "xml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * <p>
	 * getDcInstanceNumber.
	 * </p>
	 *
	 * @return the dcInstances
	 */
	public int getDcInstanceNumber() {
		return this.dcInstances;
	}

	/**
	 * <p>
	 * Getter for the field <code>edgeProb</code>.
	 * </p>
	 *
	 * @return the edgeProb
	 */
	public double getEdgeProb() {
		return this.edgeProb;
	}

	/**
	 * <p>
	 * Getter for the field <code>maxWeight</code>.
	 * </p>
	 *
	 * @return the maxWeight
	 */
	public int getMaxWeight() {
		return this.maxWeight;
	}

	/**
	 * <p>
	 * getNodeNumber.
	 * </p>
	 *
	 * @return the nNodes
	 */
	public int getNodeNumber() {
		return this.nNodes;
	}

	/**
	 * <p>
	 * getNotDCInstanceNumber.
	 * </p>
	 *
	 * @return the notDCInstances
	 */
	public int getNotDCInstanceNumber() {
		return this.notDCInstances;
	}

	/**
	 * <p>
	 * Getter for the field <code>notDCSubDir</code>.
	 * </p>
	 *
	 * @return the notDCSubDir
	 */
	public File getNotDCSubDir() {
		return this.notDCSubDir;
	}

	/**
	 * <p>
	 * getObsQLoopNumber.
	 * </p>
	 *
	 * @return the nObsQLoop
	 */
	public int getObsQLoopNumber() {
		return this.nCtgNodes;
	}

	/**
	 * @return version and copyright string
	 */
	public String getVersionAndCopyright() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		String s = "\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2021, Roberto Posenato.\n";
		try {
			s = this.getClass().getName() + " " + this.getClass().getDeclaredField("VERSIONandDATE").get(this)
					+ s;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			//
		}
		return s;
	}

	/**
	 * Decides randomly if an edge with a random value has to be added to <code>g</code>. If yes, it adds it as edge between node given
	 * by addedNodes[firstNodeIndex] and node addedNodes[secondNodeIndex].
	 * 
	 * @param firstNodeIndex
	 * @param secondNodeIndex
	 * @param g
	 * @param addedNodes
	 * @param edgeFactory
	 * @param addedEdges
	 * @return the added edge, if added; null otherwise.
	 */
	private STNUEdge addRndEdge(int firstNodeIndex, int secondNodeIndex, TNGraph<STNUEdge> g, EdgeSupplier<STNUEdge> edgeFactory,
			LabeledNode[] addedNodes, ObjectList<STNUEdge> addedEdges) {
		boolean isEdgeToAdd = this.rnd.nextDouble() <= this.edgeProb;
		if (!isEdgeToAdd) {
			return null;
		}
		LabeledNode firstNode = addedNodes[firstNodeIndex];
		LabeledNode secondNode = addedNodes[secondNodeIndex];
		if (g.outDegree(firstNode) > this.outDegree || g.inDegree(secondNode) > this.inDegree) {
			LOG.finer("Edge cannot added because inDegree or outDegree bound would be violated.");
			return null;
		}

		int weight = this.rnd.nextInt(this.maxWeight);
		STNUEdge e = edgeFactory.get(firstNode.getName() + "-" + secondNode.getName());
		e.setValue((this.rnd.nextBoolean() ? -weight / 4 : weight)); // A quarter of the weight when it must be negative: to make more probable DC instances
																		// with negative
																		// values.
		g.addEdge(e, firstNode, secondNode);
		addedEdges.add(e);
		LOG.finer("Added edge: " + e);
		return e;
	}

	/**
	 * Adds a pair of edges between node given by addedNodes[firstNodeIndex] and node addedNodes[secondNodeIndex] such that addedNodes[firstNodeIndex] precedes
	 * addedNodes[secondNodeIndex]. The edges values are chosen randomly.
	 * 
	 * @param firstNodeIndex first node
	 * @param secondNodeIndex second node
	 * @param g must be not null
	 * @param addedNodes must be not null
	 * @param edgeFactory factory of edges
	 * @param addedEdges If null, the generated edges are not stored.
	 */
	private void addPrecedenceEdges(int firstNodeIndex, int secondNodeIndex, TNGraph<STNUEdge> g, EdgeSupplier<STNUEdge> edgeFactory,
			LabeledNode[] addedNodes, ObjectList<STNUEdge> addedEdges) {

		LabeledNode firstNode = addedNodes[firstNodeIndex];
		LabeledNode secondNode = addedNodes[secondNodeIndex];

		// forward edge weight
		int positiveWeight = this.rnd.nextInt(this.maxWeight);

		// backward edge weight
		int negativeWeight = -this.rnd.nextInt(this.maxWeight);
		int sum = positiveWeight + negativeWeight;
		while (sum < 0) {
			positiveWeight = this.rnd.nextInt(this.maxWeight);
			negativeWeight = -this.rnd.nextInt(this.maxWeight);
			sum = positiveWeight + negativeWeight;
		}
		STNUEdge e = edgeFactory.get(firstNode.getName() + "-" + secondNode.getName());
		e.setValue(positiveWeight);
		g.addEdge(e, firstNode, secondNode);
		if (addedEdges != null)
			addedEdges.add(e);
		LOG.finer("Added edge: " + e);

		e = edgeFactory.get(secondNode.getName() + "-" + firstNode.getName());
		e.setValue(negativeWeight);
		g.addEdge(e, secondNode, firstNode);
		if (addedEdges != null)
			addedEdges.add(e);
		LOG.finer("Added edge: " + e);

		return;
	}

	/**
	 * Adds randomly a forward or a backward edge between node given by addedNodes[firstNodeIndex] and node addedNodes[secondNodeIndex].
	 * The probability of the adding an edge is {@link #edgeProb}.
	 * The edge value is chosen randomly considering {@link #maxWeight}.
	 * 
	 * @param firstNodeIndex first node
	 * @param secondNodeIndex second node
	 * @param g must be not null
	 * @param addedNodes must be not null
	 * @param edgeFactory factory to create a new edge for 'g'.
	 * @param addedEdges If null, the generated edges are not stored.
	 */
	private void addForwardOrBackwardEdge(int firstNodeIndex, int secondNodeIndex, TNGraph<STNUEdge> g, EdgeSupplier<STNUEdge> edgeFactory,
			LabeledNode[] addedNodes, ObjectList<STNUEdge> addedEdges) {

		LabeledNode firstNode = addedNodes[firstNodeIndex];
		LabeledNode secondNode = addedNodes[secondNodeIndex];
		STNUEdge forwardEdge = null, backEdge = null;
		int forwardWeight = 0, backwardWeight = 0;

		/**
		 * At first sight, one can argue that a negative forward edge is a backward edge.
		 * This implementation does not exploit such property because it has to consider also the inDegre and the outDegree of each node.
		 */
		boolean isEdgeToAdd = this.rnd.nextDouble() <= this.edgeProb
				&& (g.findEdge(firstNode, secondNode) == null)
				&& g.outDegree(firstNode) < this.outDegree && g.inDegree(secondNode) < this.inDegree;
		// forward edge weight
		if (isEdgeToAdd) {
			forwardWeight = ((this.rnd.nextBoolean()) ? -1 : 1) * this.rnd.nextInt(this.maxWeight);
			if (forwardWeight < 0)
				forwardWeight /= 1.5;
			forwardEdge = edgeFactory.get(firstNode.getName() + "-" + secondNode.getName());
			forwardEdge.setValue(forwardWeight);
			g.addEdge(forwardEdge, firstNode, secondNode);
			if (addedEdges != null)
				addedEdges.add(forwardEdge);
			LOG.finer("Added forward edge: " + forwardEdge);
			return;// we try to put a backward edge ONLY if a forward was not added.
			// because we have experimented that it is quite difficult to create DC instanced adding loop
		}

		// we consider the possibility to add a backardEdge
		isEdgeToAdd = this.rnd.nextDouble() <= this.edgeProb
				&& (g.findEdge(secondNode, firstNode) == null)
				&& g.inDegree(firstNode) < this.inDegree && g.outDegree(secondNode) < this.outDegree;
		if (isEdgeToAdd) {
			backwardWeight = ((this.rnd.nextBoolean()) ? -1 : 1) * this.rnd.nextInt(this.maxWeight);
			if (backwardWeight < 0)
				backwardWeight /= 1.5;
			backEdge = edgeFactory.get(secondNode.getName() + "-" + firstNode.getName());
			backEdge.setValue(backwardWeight);
			g.addEdge(backEdge, secondNode, firstNode);
			if (addedEdges != null)
				addedEdges.add(backEdge);
			LOG.finer("Added backward edge: " + backEdge);
		}
	}

	/**
	 * Adjusts the weight of all edges (increase/decrease) considering {@link #weightAdjustment}.
	 * 
	 * @param addedEdges
	 * @param increase
	 */
	private void adjustEdgeWeights(STNUEdge[] addedEdges, boolean increase) {
		/*
		 * Don't implement the following actions:
		 * 1. Limit the adjustment to <= this.maxWeight because, otherwise, many values are squeezed to this.maxWeight.
		 * 2. Adjust only positive values when increase and negative value when !increase because it never reach a DC instance.
		 */
		int sign = (increase) ? +1 : -1;
		for (STNUEdge e : addedEdges) {
			int oldV = e.getValue();
			int adjustment = sign * this.weightAdjustment;
			if (increase && oldV < 0)
				adjustment /= 2;

			e.setValue(oldV + adjustment);
		}
	}

	/**
	 * Builds a pair of DC and not DC of CSTN instances using the building parameters.
	 * The not DC instance is build adding one or more constraints to the previous generated DC instance.
	 * 
	 * @param alsoNotDcInstance false if the not DC instances is required. If false, the returned not DC instance is an empty tNGraph.
	 * @return a pair of DC and not DC of CSTN instances. If the first member is null, it means that a generic error in the building
	 *         has occurred. If alsoNotDcInstance is false, the returned not DC instance is null.
	 */
	private ObjectPair<TNGraph<STNUEdge>> buildAPairRndTNInstances(boolean alsoNotDcInstance) {

		LOG.info("Start building a new random instance");
		TNGraph<STNUEdge> randomGraph = new TNGraph<>(EdgeSupplier.DEFAULT_STNU_EDGE_CLASS),
				notDCGraph = null;
		LabeledNodeSupplier nodeFactory = randomGraph.getNodeFactory();
		EdgeSupplier<STNUEdge> edgeFactory = randomGraph.getEdgeFactory();

		LOG.info("Adding " + this.nNodes + " nodes of which " + this.nCtgNodes + " will be contingents.");
		LabeledNode[] addedNodes = this.generateNodesAndContingentLinks(randomGraph, nodeFactory, edgeFactory);

		// Adding edges.
		STNUEdge[] addedEdges;
		if (this.randomTree) {
			addedEdges = this.generateRandomTree(randomGraph, addedNodes, edgeFactory);
		} else {
			if (this.randomLane) {
				addedEdges = this.generateRandomLanes(randomGraph, addedNodes, edgeFactory);
			} else {
				addedEdges = this.generateRandomEdges(randomGraph, addedNodes, edgeFactory);
			}
		}

		TNGraph<STNUEdge> lastDC = randomGraph;

		TNGraphMLWriter cstnWriter = new TNGraphMLWriter(null);

		int checkN = 0;// number of checks
		boolean nonDCfound = false, DCfound = false;
		STNU stnu = new STNU(randomGraph, this.timeOut);
		stnu.setDefaultConsistencyCheckAlg(CheckAlgorithm.RUL2020);

		STNUCheckStatus status;

		while (true) {
			stnu.reset();
			stnu.setG(new TNGraph<>(randomGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS));
			if (LOG.isLoggable(Level.FINER)) {
				try {
					cstnWriter.save(stnu.getG(), this.tmpNetwork);
				} catch (IOException e) {
					System.err.println(
							"It is not possible to save the result. File " + this.tmpNetwork + " cannot be created: " + e.getMessage()
									+ ". Computation continues.");
				}

				LOG.finer("Current cstn saved as 'current.stnu' before checking.");
			}
			try {
				LOG.fine("DC Check started.");

				status = stnu.dynamicControllabilityCheck();

				checkN++;
				LOG.fine("DC Check finished.");
			} catch (Exception ex) {
				String fileName = "error" + System.currentTimeMillis() + ".stnu";
				LOG.finer("DC Check interrupted for the following reason: " + ex.getMessage() + ". Instance is saved as " + fileName + ".");
				File d = getNewFile(this.dcSubDir.getParentFile(), fileName);
				try {
					Files.move(this.tmpNetwork, d);
				} catch (IOException e1) {
					LOG.finer("Problem to save 'current.stnu' as non valid instance for logging. Program continues anyway.");
				}
				return new ObjectPair<>(null, null);
			}
			if (status.timeout) {
				String fileName = "timeOut" + System.currentTimeMillis() + ".stnu";
				LOG.finer("DC Check finished for timeout. Instance is saved as " + fileName + ".");
				File d = getNewFile(this.dcSubDir.getParentFile(), fileName);
				try {
					Files.move(this.tmpNetwork, d);
				} catch (IOException ex) {
					LOG.finer("Problem to save 'current.cstn' as time out instance. Program continues anyway.");
				}
				return new ObjectPair<>(null, null);
			}
			if (status.isControllability()) {
				LOG.finer("Random instance is DC.");
				lastDC = new TNGraph<>(randomGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
				DCfound = true;
				if (!nonDCfound && alsoNotDcInstance) {
					LOG.finer("Now, a not DC instance must be generated. Tentative #" + checkN);
					// we lower the edge value
					adjustEdgeWeights(addedEdges, false);
				} else {
					LOG.finer("The pair has been found after " + checkN + " iterations.");
					return new ObjectPair<>(lastDC, notDCGraph);
				}
			} else {
				LOG.finer("Random instance is not DC.");
				notDCGraph = new TNGraph<>(randomGraph, EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
				nonDCfound = true;
				if (!DCfound) {
					LOG.finer("Now, a DC instance must be generated. Tentative #" + checkN);
					adjustEdgeWeights(addedEdges, true);
				} else {
					LOG.finer("The pair has been found after " + checkN + " iterations.");
					return new ObjectPair<>(lastDC, notDCGraph);
				}
			}
			if (checkN > MAX_CHECKS) {
				LOG.finer("This network was checked more than " + MAX_CHECKS
						+ " times without finding the wanted pair. Program continues witho another network.");
				return new ObjectPair<>(null, null);
			}
		}
	}

	/**
	 * Generates {@link #lanes} lanes of tasks and adding constraints between pair of tasks in different lanes.
	 * 
	 * @param randomGraph
	 * @param addedNodes
	 * @param edgeFactory
	 * @return the array of random edges added.
	 */
	private STNUEdge[] generateRandomLanes(TNGraph<STNUEdge> randomGraph, LabeledNode[] addedNodes, EdgeSupplier<STNUEdge> edgeFactory) {
		LOG.finer("The generation of random tree edges is started.");
		ObjectList<STNUEdge> addedEdges = new ObjectArrayList<>();
		int N = this.nNodes;// Z is counted
		int L = this.lanes;

		/**
		 * laneBondary[i-1]+1 is the index of the first node in lane i.
		 * laneBondary[i] is the index of the last node in lane i.
		 */
		int[] laneBondary = new int[L + 1];
		laneBondary[0] = 0;// Z does not enter in the lanes!
		laneBondary[L] = this.nNodes;

		double nodesPerLane = ((double) N) / L;
		double k = nodesPerLane;
		int bound;
		for (int i = 1; i < L; i++) {
			bound = (int) Math.round(k);
			if (addedNodes[bound].getName().startsWith("A")) {
				bound++;
				k = bound;
			}
			laneBondary[i] = bound;
			k += nodesPerLane;
		}

		double x = 0.0, y = 0.0;
		for (int l = 1; l <= L; l++) {
			y = 0;
			x += X_SHIFT;
			LOG.finer("Creating lane " + l + " using nodes in range [" + (laneBondary[l - 1] + 1) + ", " + laneBondary[l] + "].");
			for (int currentNode = laneBondary[l - 1] + 1, relativeI = 0; currentNode <= laneBondary[l]; currentNode++, y += Y_SHIFT, relativeI++) {
				addedNodes[currentNode].setX(x);
				addedNodes[currentNode].setY(y);

				if (currentNode == laneBondary[l])
					continue;// for the last node in the lane, there is no other thing to do
				if (addedNodes[currentNode].getName().startsWith("A"))
					continue;// for the pair (A,C) edges are already set as contingent ones.
				addPrecedenceEdges(currentNode, currentNode + 1, randomGraph, edgeFactory, addedNodes, null);

				// put some edges among siblings.
				for (int secondLane = l + 1; secondLane <= L; secondLane++) {
					int secondLaneLB = laneBondary[secondLane - 1] + 1;
					int secondIndex = secondLaneLB + relativeI + ((this.rnd.nextBoolean()) ? -1 : 1) * this.rnd.nextInt(3);
					if (secondIndex < secondLaneLB)
						secondIndex = secondLaneLB;
					if (secondIndex > laneBondary[secondLane])
						secondIndex = laneBondary[secondLane];
					addForwardOrBackwardEdge(currentNode, secondIndex, randomGraph, edgeFactory, addedNodes, addedEdges);
				}
			}
		}

		LOG.finer("The generation of lane edges is finished.");
		return addedEdges.toArray(new STNUEdge[addedEdges.size()]);

	}

	/**
	 * Generates the network nodes adding to the graph randomGraph and returns them as an array.
	 * For contingent nodes, it also generate the contingent link (the two edges representing the link).
	 * 
	 * @param randomGraph the graph where to add nodes and contingent links.
	 * @param nodeFactory factory for nodes
	 * @param edgeFactory factory for edges
	 * @return the array of random nodes generated.
	 */
	private LabeledNode[] generateNodesAndContingentLinks(TNGraph<STNUEdge> randomGraph, LabeledNodeSupplier nodeFactory,
			EdgeSupplier<STNUEdge> edgeFactory) {
		LOG.finer("The generation of nodes is started.");

		LabeledNode[] addedNodes = new LabeledNode[this.nNodes + 1];// Z is not considered in the total number of nodes.
		int indexLastAddedNode = 0, indexLastAddedCtg = 0, indexAddedNodesArray = 0;
		int nSlots = this.nCtgNodes + 1;
		double nNodes4Slot = (this.nNodes - 2.0 * this.nCtgNodes) / nSlots;

		// add nodes and ctg link interleaving nodes and contingent ones (with their activation nodes).
		double x = 0, y = -Y_SHIFT + 30;
		for (int slot = 1; slot <= nSlots; slot++) {
			// each slot (but last one) is composed by int(nNodes4Slot) nodes + a contingent link
			// edges among ordinary nodes and contingent ones are added after

			// add ordinary nodes
			x = X_SHIFT;
			y += Y_SHIFT;
			while (++indexLastAddedNode <= (nNodes4Slot * slot)) {
				// LOG.finer("indexLastAddedNode: " + indexLastAddedNode + " <= " + nNodes4Slot * slot);
				LabeledNode node = nodeFactory.get(NODE_NAME_PREFIX + indexLastAddedNode);
				node.setX(x);
				node.setY(y);
				randomGraph.addVertex(node);
				addedNodes[++indexAddedNodesArray] = node;
				LOG.finer("Node added: " + node.getName());
				x += X_SHIFT;
			}
			indexLastAddedNode--;
			if (slot == nSlots)
				continue;// after last slot, no contingent link

			// added a contingent pair
			indexLastAddedCtg++;
			LabeledNode actNode = nodeFactory.get("A" + indexLastAddedCtg);
			actNode.setX(x);
			actNode.setY(y);
			randomGraph.addVertex(actNode);
			addedNodes[++indexAddedNodesArray] = actNode;
			LOG.finer("Activation node added: " + actNode);
			x += X_SHIFT;

			LabeledNode ctgNode = nodeFactory.get("C" + indexLastAddedCtg);
			ctgNode.setX(x);
			ctgNode.setY(y);
			randomGraph.addVertex(ctgNode);
			addedNodes[++indexAddedNodesArray] = ctgNode;
			LOG.finer("Contingent node added: " + ctgNode);

			// add contingent edges
			int upperValue = 2 + this.rnd.nextInt(this.maxContingentWeight);
			if (upperValue > this.maxContingentWeight)
				upperValue = this.maxContingentWeight;
			int lowerValue = upperValue;
			while (lowerValue >= upperValue) {
				lowerValue = upperValue - (this.rnd.nextInt(this.maxContingentRange));
				if (lowerValue <= 0)
					lowerValue = 1;
			}

			STNUEdge e = edgeFactory.get(EDGE_NAME_PREFIX + actNode.getName() + "-" + ctgNode.getName());
			e.setConstraintType(ConstraintType.contingent);
			e.setValue(upperValue);
			randomGraph.addEdge(e, actNode, ctgNode);
			LOG.finer("Upper link added: " + e);

			e = edgeFactory.get(EDGE_NAME_PREFIX + ctgNode.getName() + "-" + actNode.getName());
			e.setConstraintType(ConstraintType.contingent);
			e.setValue(-lowerValue);
			randomGraph.addEdge(e, ctgNode, actNode);
			LOG.finer("Lower link added: " + e);
		}

		assert (indexLastAddedCtg == this.nCtgNodes);
		assert (indexAddedNodesArray == this.nNodes);

		LabeledNode Z = randomGraph.getNodeFactory().get("Z");
		Z.setX(0);
		Z.setY(0);
		randomGraph.addVertex(Z);
		addedNodes[0] = Z;
		LOG.finer("Node Z added.");
		LOG.finer("The generation of nodes is finished.");

		return addedNodes;
	}

	/**
	 * Generates random edges considering only the edge probability.
	 * For each ordered pair on nodes, a random edge is added with probability {@link #edgeProb}.
	 * 
	 * @param randomGraph
	 * @param addedNodes
	 * @param edgeFactory
	 * @return the array of random edges added.
	 */
	private STNUEdge[] generateRandomEdges(TNGraph<STNUEdge> randomGraph, LabeledNode[] addedNodes, EdgeSupplier<STNUEdge> edgeFactory) {
		LOG.finer("The generation of edges is started.");
		ObjectList<STNUEdge> addedEdges = new ObjectArrayList<>();
		STNUEdge e, eI;
		for (int i = 1; i <= this.nNodes; i++) {// We do not put upper bound to Z.
			int k = i + ((addedNodes[i].getName().startsWith("A")) ? 2 : 1);// between activation and contingent nodes no random edges.
			for (int j = k; j <= this.nNodes; j++) {
				// avoid directed edge between activation and contingent link
				String source = addedNodes[i].getName().substring(0, 1);
				String dest = addedNodes[j].getName().substring(0, 1);
				if ((source.equals("A") || source.equals("C")) && (dest.equals("A") || dest.equals("C"))) {
					LOG.finer("Avoiding a direct edge between different contingent link endpoints.");
					continue;
				}
				e = addRndEdge(i, j, randomGraph, edgeFactory, addedNodes, addedEdges);
				eI = addRndEdge(j, i, randomGraph, edgeFactory, addedNodes, addedEdges);

				// avoid negative 2-edge loops
				if (e == null || eI == null)
					continue;
				int s = e.getValue() + eI.getValue();
				if (s >= 0)
					continue;
				LOG.finer("Negative loop " + e + " <-> " + eI + " Sum: " + s);
				if (e.getValue() < 0) {
					e.setValue(-s);
					LOG.finer("Changed " + e);
				} else {
					eI.setValue(-s);
					LOG.finer("Changed " + eI);
				}
			}
		}
		LOG.finer("The generation of edges is finished.");
		return addedEdges.toArray(new STNUEdge[addedEdges.size()]);
	}

	/**
	 * Generates a random tree having Z as root.
	 * 
	 * @param randomGraph
	 * @param addedNodes
	 * @param edgeFactory
	 * @return the array of random edges added.
	 */
	private STNUEdge[] generateRandomTree(TNGraph<STNUEdge> randomGraph, LabeledNode[] addedNodes, EdgeSupplier<STNUEdge> edgeFactory) {
		LOG.finer("The generation of random tree edges is started.");
		ObjectList<STNUEdge> addedEdges = new ObjectArrayList<>();
		int N = this.nNodes + 1;// Z is counted
		int K = this.nSons;

		IntList parents = new IntArrayList();
		parents.add(0);
		IntList sons = new IntArrayList();
		// for each height h, connect a parent node at height h with its sons
		int h = 0;
		double x = 0.0, y = 0.0;
		for (int absoluteI = 1; absoluteI < N; h++) {
			y += Y_SHIFT;
			x = -X_SHIFT;
			for (int parent : parents) {
				boolean add = false;
				int i = 0;
				x += X_SHIFT;
				for (; i < K; i++) {
					int son = (this.rnd.nextDouble() <= this.sonProb || (!add && i == K - 1)) ? absoluteI++ : 0;// 0 is not used
					if (son >= N)// if Z is counted put =
						break;
					if (son == 0)
						continue;
					sons.add(son);
					add = true;
					addedNodes[son].setX(x);
					x += X_SHIFT;
					addedNodes[son].setY(y);
					addPrecedenceEdges(parent, son, randomGraph, edgeFactory, addedNodes, null);

					if (addedNodes[son].getName().startsWith("A")) {
						// the contingent node is put as brother even if it is the last
						son = absoluteI++;
						sons.add(son);
						addedNodes[son].setX(x);
						x += X_SHIFT;
						addedNodes[son].setY(y);
						add = true;
						i++;
					}
				}
				if (absoluteI >= N)
					break;
			}
			int nSon = sons.size();
			if (nSon > 5) {
				// put some edges among siblings.
				for (int i = 0; i < nSon; i++) {
					for (int j = i + 1; j < nSon; j++) {
						addForwardOrBackwardEdge(sons.getInt(i), sons.getInt(j), randomGraph, edgeFactory, addedNodes, addedEdges);
					}
				}
			}
			parents.clear();
			parents.addAll(sons);
			sons.clear();
			LOG.finer("Tree height " + h + " completed.");
		}

		LOG.finer("The generation of tree edges is finished.");
		return addedEdges.toArray(new STNUEdge[addedEdges.size()]);
	}

	/**
	 * @throws IllegalArgumentException if a parameter is not valid.
	 */
	private void checkParameters() throws IllegalArgumentException {
		if (this.nNodes < MIN_NODES || this.nNodes < this.nCtgNodes * 2 + 1)
			throw new IllegalArgumentException(
					"The number of nodes is not valid. Value has to be greater than the double of the number of contingent nodes. Value " + MIN_NODES
							+ " is the minimum.");

		if (this.nCtgNodes < 0) {
			throw new IllegalArgumentException(
					"The number of contingent nodes is not valid. The value must be equal to or greater than 0.");
		}

		if (this.maxWeight < MIN_MAX_WEIGHT)
			throw new IllegalArgumentException(
					"The maximum edge weight value is not valid. Valid range = [" + MIN_MAX_WEIGHT + ", " + Integer.MAX_VALUE + "].");

		if (this.maxWeight * (this.nNodes) >= Constants.INT_POS_INFINITE)
			throw new IllegalArgumentException(
					"The maximum edge weight value combined with the number of nodes is not valid. maxWeight * #nodes must be < " + Constants.INT_POS_INFINITE);

		if (this.maxContingentWeight <= 0)
			throw new IllegalArgumentException(
					"The maximum contingent edge weight value is not valid. Valid range = [1, " + Integer.MAX_VALUE + "].");

		if (this.maxContingentRange < 1 || this.maxContingentRange >= this.maxContingentWeight)
			throw new IllegalArgumentException(
					"The maximum contingent edge range is not valid. Valid values in [1, " + this.maxContingentWeight + "].");

		if (this.maxWeight * (this.nNodes) >= Constants.INT_POS_INFINITE)
			throw new IllegalArgumentException(
					"The maximum edge weight value combined with the number of nodes is not valid. maxWeight * #nodes must be < " + Constants.INT_POS_INFINITE);

		if (this.maxContingentWeight * (this.nNodes) >= Constants.INT_POS_INFINITE)
			throw new IllegalArgumentException(
					"The maximum contingent edge weight value combined with the number of nodes is not valid. maxContingentWeight * #nodes must be < "
							+ Constants.INT_POS_INFINITE);

		if (this.edgeProb < 0 || this.edgeProb > 1.0)
			throw new IllegalArgumentException(
					"The edge probability is not valid. Valid range = [0.0, 1.0].");

		if (this.dcInstances < 0 || this.dcInstances < this.notDCInstances)
			throw new IllegalArgumentException(
					"The number of wanted DC instances is not valid. It must positive and at least as notDCInstances value.");

		if (this.notDCInstances < 0 || this.dcInstances < this.notDCInstances)
			throw new IllegalArgumentException(
					"The number of wanted not DC instances is not valid. It must positive and at mosta as dcInstances value.");
		this.weightAdjustment = (int) (this.maxWeight * WEIGHT_MODIFICATION_FACTOR);

		if (this.randomTree) {
			if (this.nSons < 1 || this.nSons > this.nNodes) {
				throw new IllegalArgumentException(
						"The number of wanted sons is not valid. It must be in [1, " + this.nNodes + "].");
			}
			if (this.sonProb <= 0 || this.sonProb > 1) {
				throw new IllegalArgumentException(
						"The son probability must be a value (0, 1].");
			}
		}
		if (this.randomLane) {
			if (this.lanes < 1 || this.lanes > this.nNodes) {
				throw new IllegalArgumentException(
						"The number of wanted lanes is not valid. It must be in [1, " + this.nNodes + "].");
			}
		}
		if (this.dense) {
			if (this.density < 0 || this.density > 1) {
				throw new IllegalArgumentException(
						"The density is not valid. It must be in [0, 1].");
			}
			this.minNEdges = (int) (this.nNodes * (this.nNodes - 1) * this.density);
			if (this.nNodes * (this.inDegree + this.outDegree) < this.minNEdges) {
				throw new IllegalArgumentException(
						"The density is not compatible with the inDegree and outDegree values. Lower the density or increase inDegree and/or outDegree.");
			}
		}
	}

	/**
	 * Creates the main directory and the two sub dirs that will contain the random instances.
	 * 
	 * @return prefix to use for creating file names
	 * @throws IOException if any directory cannot be created or moved.
	 */
	private String createFolders() throws IOException {
		File baseDir = new File(this.baseDirName);

		if (!baseDir.exists()) {
			if(!baseDir.mkdirs()) {
				String m = "Directory " + baseDir.getAbsolutePath() + " cannot be created!";
				LOG.severe(m);
				throw new RuntimeException(m);
			}
		}
		String suffix = "_" + String.format(makeNumberFormat(this.nNodes), this.nNodes) + "nodes_"
				+ String.format(makeNumberFormat(this.nCtgNodes), this.nCtgNodes) + "ctgs_"
				+ this.maxWeight + "maxWeight_" + this.maxContingentWeight + "maxCtgWeight_";
		if (this.randomTree) {
			suffix += this.nSons + "aryTree_" + this.sonProb + "sonProb";
		} else {
			if (this.randomLane) {
				suffix += this.lanes + "lanes_";
			} else {
				suffix += this.inDegree + "inDegree_" + this.outDegree + "outDegree";
			}
		}

		this.dcSubDir = new File(baseDir, DC_SUB_DIR_NAME + suffix);
		if (!this.dcSubDir.exists()) {
			if (!this.dcSubDir.mkdir()) {
				String m = "Directory " + this.dcSubDir.getAbsolutePath() + " cannot be created!";
				LOG.severe(m);
				throw new RuntimeException(m);
			}
		}

		this.notDCSubDir = new File(baseDir, NOT_DC_SUB_DIR_NAME + suffix);
		if (!this.notDCSubDir.exists()) {
			if (!this.notDCSubDir.mkdir()) {
				String m = "Directory " + this.notDCSubDir.getAbsolutePath() + " cannot be created!";
				LOG.severe(m);
				throw new RuntimeException(m);
			}
		}

		final String log = "Main directory where generated instances are saved: " + baseDir.getCanonicalPath()
				+ "\nSub dir for DC instances:\t\t" + this.dcSubDir.getPath()
				+ "\nSub dir for NOT DC instances:\t" + this.notDCSubDir.getPath();
		LOG.fine(log);
		System.out.println(log);

		return suffix;

	}

	/**
	 * Creates a file using child as name.
	 * If child is already present, the already present child is renamed as child+"~" (recursively).
	 * 
	 * @param parent
	 * @param child
	 * @return a File that is not present with name child[~]*
	 */
	static private File getNewFile(File parent, String child) {
		File newFile = new File(parent, child);
		if (newFile.isFile()) {
			File bakFile = getNewFile(parent, child + "~");
			try {
				Files.move(newFile, bakFile);
			} catch (IOException e) {
				throw new RuntimeException("Cannot rename file " + child + " to " + child + "~: " + e.getMessage());
			}
		}
		return newFile;
	}

	/**
	 * Creates the two README files that describe the content of the two sub dir this.DCSubDir and this.notDCSubDir.
	 * 
	 * @throws IOException if any file cannot be created.
	 */
	private void createReadmeFiles(STNURandomGenerator this) throws IOException {
		if (this.dcSubDir == null || this.notDCSubDir == null)
			return;

		String readmeText = "STNU RANDOM INSTANCE BENCHMARK\n" +
				"==============================\n\n" +
				"This directory contains %04d %s STNU random instances.\n" + // generator.dcInstances +
				"Each instance is built generating by a random process tuned by the following parameters.\n" +
				"The parameter values used for generating the instances are:\n" +
				"#nodes:\t\t\t\t" + String.format("%4d", this.nNodes) + "\n" +
				"#contingent:\t\t" + String.format("%4d", this.nCtgNodes) + "\n" +
				"#nodeOutdegree:\t\t" + String.format("%4d", this.outDegree) + "\n" +
				"#nodeInDegree:\t\t" + String.format("%4d", this.inDegree) + "\n" +
				"#edgeProbability:\t" + String.format("%4.2f", this.edgeProb) + "\n" +
				"#max weight:\t\t" + String.format("%4d", this.maxWeight) + "\n" +
				"#max contingent w.:\t" + String.format("%4d", this.maxContingentWeight) + "\n";
		if (this.randomTree) {
			readmeText += "#treeArity:\t\t" + String.format("%4d", this.nSons) + "\n" +
					"#sonProbability:\t" + String.format("%4.2f", this.sonProb) + "\n";
		}
		if (this.randomLane) {
			readmeText += "#lanes:\t\t" + String.format("%4d", this.lanes) + "\n";
		}
		readmeText += "\n";

		String readmeStr = "README";
		try (PrintWriter writer = new PrintWriter(getNewFile(this.dcSubDir, readmeStr), "UTF-8")) {
			writer.format(readmeText, this.dcInstances, "dynamic controllable (DC)");
			writer.close();
		}
		try (PrintWriter writer = new PrintWriter(getNewFile(this.notDCSubDir, readmeStr), "UTF-8")) {
			writer.format(readmeText, this.notDCInstances, "NOT dynamic controllable (NOTDC)");
			writer.close();
		}
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args the arguments from the command line.
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args); // parse the arguments.

			this.checkParameters(); // check the parameters!

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
}
