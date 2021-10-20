// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.STN.STNCheckStatus;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.STNUEdge.Pair;
import it.univr.di.cstnu.graph.STNUEdgeInt;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.cstnu.visualization.CSTNUStaticLayout;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

/**
 * Simple class to represent and consistency-check Simple Temporal Network with Uncertainty (STNU) where the edge weight are signed integer.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class STNU {

	/**
	 * Possible DC checking algorithm
	 */
	public static enum CheckAlgorithm {
		/**
		 * Morris cubic algorithm
		 */
		Morris2014,
		/**
		 * Cairo, Rizzi and Hunsberger RUL^- algorithm
		 */
		RUL2018,
		/**
		 * Luke's version of RUL^- algorithm
		 */
		RUL2020
		/**
		 * Only for checking BellmanFord
		 * BellmanFord
		 */
	}

	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class STNUCheckStatus extends STNCheckStatus {

		// controllability = super.consistency!

		/**
		 * Counters about the # of application of different rules.
		 */
		public int addedEdges = 0;

		/**
		 * Reset all indexes.
		 */
		@Override
		public void reset() {
			super.reset();
			this.cycles = this.addedEdges = 0;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("The check is");
			if (!this.finished)
				sb.append(" NOT");
			sb.append(" finished");
			if (this.cycles > 0)
				sb.append(" after ").append(this.cycles).append(" cycle(s).\n");
			else
				sb.append(".\n");
			if (this.finished) {
				sb.append("The consistency check has determined that given network is ");
				if (!this.consistency)
					sb.append("NOT ");
				sb.append("controllable.\n");
			}
			sb.append("Added edges during the check ").append(this.addedEdges).append(".\n");
			// sb.append("UpperCaseRule has been applied ").append(this.calls).append(" times.\n");
			// sb.append("LowerCaseRule has been applied ").append(this.lowerCaseCalls).append(" times.\n");
			// sb.append("CrossCaseRule has been applied ").append(this.crossCaseCalls).append(" times.\n");
			if (this.timeout)
				sb.append("Checking has been interrupted because execution time exceeds the given time limit.\n");

			if (this.executionTimeNS != Constants.INT_NULL)
				sb.append("The global execution time has been ").append(this.executionTimeNS).append(" ns (~").append((this.executionTimeNS / 1E9))
						.append(" s.)");
			return sb.toString();
		}

		/**
		 * Sets the controllability value
		 * 
		 * @param state new controllability value
		 */
		public void setControllability(boolean state) {
			this.consistency = state;
		}

		/**
		 * @return true if the controllability status is true
		 */
		public boolean isControllability() {
			return this.consistency;
		}
	}

	/**
	 * Status for negative nodes in Morris 2014 algorithm or for contingent edges in RUL2020.
	 * 
	 * @author posenato
	 */
	private enum ElementStatus {
		/**
		 * unstarted MUST be the first element of this enum!
		 */
		unstarted, started, finished
	}

	/**
	 * Suffix for file name
	 */
	public static final String FILE_NAME_SUFFIX = ".stnu";
	/**
	 * The name for the initial node.
	 */
	public static final String ZERO_NODE_NAME = "Z";

	/**
	 * logger
	 */
	private static Logger LOG = Logger.getLogger(STNU.class.getName());

	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 1.0 - April, 08 2020";
//	static final String VERSIONandDATE = "Version 1.1 - January 19, 2020";// fixed only the nome for the logger.
	static final String VERSIONandDATE = "Version 1.1.1 - October 13, 2021";// Fixed PriorityQueue made generics

	/**
	 * <p>
	 * main.
	 * </p>
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws org.xml.sax.SAXException if any.
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		STNU stnu = new STNU();
		System.out.println(stnu.getVersionAndCopyright());
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		if (!stnu.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");
		if (stnu.versionReq) {
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loading graph...");
			}
		}
		TNGraphMLReader<STNUEdge> graphMLReader = new TNGraphMLReader<>();
		stnu.setG(graphMLReader.readGraph(stnu.fInput, STNUEdgeInt.class));
		float initalEdgeN = stnu.getG().getEdgeCount();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("STNU Graph loaded!\nNow, it is time to check it...");
			}
		}
		STNUCheckStatus status;
		try {
			status = stnu.dynamicControllabilityCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.isControllability()) {
				System.out.println("The given STNU is dynamic controllable!");
			} else {
				System.out.println("The given STNU is NOT dynamic controllable!");
			}
			System.out.println("Details: " + status);
			// System.out.println("Graph checked: " + stnu.getGChecked());
			System.out.println(String.format("The percentage of added edges: %5.2f%%", (status.addedEdges / initalEdgeN * 100)));
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}
	}

	/**
	 * @param edge must be not null
	 * @return the min value between the ordinary value and the lower-case one. If the edge has only an upper-case value, it returns {@link Constants#INT_NULL}.
	 */
	private static int getMinValueBetweenOrdinaryAndLowerCaseValue(final STNUEdge edge) {
		// this method is used by BellmanFord alg.
		final int lv = (edge.isLowerCase()) ? edge.getLabeledValue() : Constants.INT_NULL;
		final int v = edge.getValue();
		if (v == Constants.INT_NULL)
			return lv;
		if (lv == Constants.INT_NULL)
			return v;
		return Math.min(lv, v);
	}

	/**
	 * @param edge must be not null. It is assumed that the network is normalized.
	 * @return the upper value if the edge is a upper-case edge. the ordinary value, otherwise.
	 *         If the edge has only a lower-case value, it returns {@link Constants#INT_POS_INFINITE}.
	 */
	private static int getUpperOrOrdinaryValue(final STNUEdge edge) {
		if (edge.isUpperCase())
			return edge.getLabeledValue();
		final int v = edge.getValue();
		if (v == Constants.INT_NULL)
			return Constants.INT_POS_INFINITE;
		return v;
	}

	/**
	 * Utility map that returns the activation time point (node) associated to a contingent time point.
	 */
	private Object2ObjectMap<LabeledNode, LabeledNode> activationNode = null;

	/**
	 * Check status
	 */
	private STNUCheckStatus checkStatus = new STNUCheckStatus();

	/**
	 */
	@Option(required = false, name = "-cleaned", usage = "Output a cleaned result. A result cleaned graph does not contain empty edges or labeled values containing unknown literals.")
	private boolean cleanCheckedInstance = true;

	/**
	 * Represent contingent links also as ordinary constraints.
	 */
	@Option(required = false, name = "-contingentAlsoAsOrdinary", usage = "Represent contingent links also as ordinary links.")
	private boolean contingentAlsoAsOrdinary = false;

	/**
	 * Which algorithm to use for consistency check. Default is Morris2014.
	 */
	@Option(required = false, name = "-a", aliases = "--alg", usage = "Which DC checking algorithm to use.")
	private CheckAlgorithm defaultControllabilityCheckAlg = CheckAlgorithm.RUL2020;

	/**
	 * The input file containing the STN graph in GraphML format.
	 */
	@Argument(required = false, index = 0, usage = "file_name must be the input STNU graph in GraphML format.", metaVar = "file_name")
	private File fInput;

	/**
	 * Output file where to write the XML representing the minimal STN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "output_file_name")
	private File fOutput = null;

	/**
	 * Input TNGraph.
	 */
	TNGraph<STNUEdge> g = null;

	/**
	 * TNGraph on which to operate.
	 */
	private TNGraph<STNUEdge> gCheckedCleaned = null;

	/**
	 * Horizon value. A node that has to be executed after such time means that it has not to be executed!
	 */
	private int horizon = Constants.INT_NULL;

	/**
	 * Utility map that return the edge containing the lower case constraint of a contingent link given the contingent time point.<br>
	 * In other words, if the contingent link is <code>(A, 1, 3, C)</code>,
	 * this maps contains <code>C --&gt; (A, c(1), C)</code>.
	 */
	private Object2ObjectMap<LabeledNode, STNUEdge> lowerContingentEdge = null;

	/**
	 * Utility map that return the edge containing the upper case constraint of a contingent link given the contingent time point.<br>
	 * In other words, if the contingent link is <code>(A, 1, 3, C)</code>,
	 * this maps contains <code>C --&gt; (C, C:-3, A)</code>.
	 */
	private Object2ObjectMap<LabeledNode, STNUEdge> upperContingentEdge = null;

	/**
	 * Absolute value of the max negative weight determined during initialization phase.
	 */
	private int maxWeight = Constants.INT_NULL;

	/**
	 */
	@Option(required = false, name = "-save", usage = "Save the checked instance.")
	private boolean save = false;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	private int timeOut = 2700;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private boolean versionReq = false;

	/**
	 * Z node of the graph.
	 * Utility reference for many method. #initAndCheck sets this value.
	 */
	private LabeledNode Z = null;

	/**
	 * <p>
	 * Constructor for STNU.
	 * </p>
	 *
	 * @param graph TNGraph to check
	 */
	public STNU(TNGraph<STNUEdge> graph) {
		this();
		this.setG(graph);// sets also checkStatus!
	}

	/**
	 * <p>
	 * Constructor for STNU.
	 * </p>
	 *
	 * @param graph TNGraph to check
	 * @param giveTimeOut timeout for the check
	 */
	public STNU(TNGraph<STNUEdge> graph, int giveTimeOut) {
		this(graph);
		this.timeOut = giveTimeOut;
	}

	/**
	 * Default constructor.
	 */
	STNU() {
	}

	/**
	 * Checks the consistency of a STN instance within timeout seconds.
	 * During the execution of this method, the given graph is modified. <br>
	 * If the check is successful, all constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * <br>
	 * After a check, {@link #getGChecked} returns the graph resulting after the check.
	 *
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	public STNUCheckStatus dynamicControllabilityCheck() throws WellDefinitionException {
		return dynamicControllabilityCheck(this.defaultControllabilityCheckAlg);
	}

	/**
	 * <p>
	 * dynamicControllabilityCheck.
	 * </p>
	 *
	 * @param alg a {@link it.univr.di.cstnu.algorithms.STNU.CheckAlgorithm} object.
	 * @return a {@link it.univr.di.cstnu.algorithms.STNU.STNUCheckStatus} object.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	public STNUCheckStatus dynamicControllabilityCheck(CheckAlgorithm alg) throws WellDefinitionException {
		try {
			initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The STNU graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("The STNU has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}
		Instant startInstant = Instant.now();

		switch (alg) {
		case Morris2014:
			this.checkStatus.setControllability(morris2014());
			break;
		case RUL2018:
			this.checkStatus.setControllability(rul2018());
			break;
		// case BellmanFord:// it is just for experiments
		// this.checkStatus.setControllability(bellmanFordOL() != null);
		// break;
		default:
		case RUL2020:
			this.checkStatus.setControllability(rul2020());
			break;
		}
		Instant endInstant = Instant.now();
		this.checkStatus.finished = true;
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.isControllability()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "Found an inconsistency.\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Final uncontrollable graph: " + this.g);
					}
				}
			}
			if (this.save) {
				this.saveGraphToFile();
			}
			return this.checkStatus;
		}
		// consistent && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Stable state reached. Status: " + this.checkStatus);
			}
		}
		if (this.cleanCheckedInstance) {
			this.gCheckedCleaned = this.g;
		}
		if (this.save) {
			this.saveGraphToFile();
		}
		return this.checkStatus;
	}

	/**
	 * <p>
	 * Getter for the field <code>checkStatus</code>.
	 * </p>
	 *
	 * @return the checkStatus
	 */
	public STNUCheckStatus getCheckStatus() {
		return this.checkStatus;
	}

	/**
	 * <p>
	 * Getter for the field <code>defaultControllabilityCheckAlg</code>.
	 * </p>
	 *
	 * @return the defaultConsistencyCheckAlg
	 */
	public CheckAlgorithm getDefaultControllabilityCheckAlg() {
		return this.defaultControllabilityCheckAlg;
	}

	/**
	 * <p>
	 * Getter for the field <code>fOutput</code>.
	 * </p>
	 *
	 * @return the fOutput
	 */
	public File getfOutput() {
		return this.fOutput;
	}

	/**
	 * <p>
	 * Getter for the field <code>g</code>.
	 * </p>
	 *
	 * @return the g
	 */
	final public TNGraph<STNUEdge> getG() {
		return this.g;
	}

	/**
	 * <p>
	 * getGChecked.
	 * </p>
	 *
	 * @return the resulting graph of a check. It is up to the called to be sure the the returned graph is the result of a check.
	 *         It can be used also by subclasses with a proper cast.
	 * @see #setOutputCleaned(boolean)
	 */
	public TNGraph<STNUEdge> getGChecked() {
		if (this.cleanCheckedInstance && this.getCheckStatus().finished && this.getCheckStatus().isControllability())
			return this.gCheckedCleaned;
		return this.g;
	}

	/**
	 * <p>
	 * Getter for the field <code>maxWeight</code>.
	 * </p>
	 *
	 * @return the maxWeight
	 */
	final public int getMaxWeight() {
		return this.maxWeight;
	}

	/**
	 * <p>
	 * getVersionAndCopyright.
	 * </p>
	 *
	 * @return version and copyright string
	 */
	public String getVersionAndCopyright() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		try {
			return this.getClass().getName() + " " + this.getClass().getDeclaredField("VERSIONandDATE").get(this)
					+ "\nSPDX-License-Identifier: LGPL-3.0-or-later, Roberto Posenato.\n";

		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("Not possible exception I think :-)");
		}
	}

	/**
	 * Makes the STNU check and initialization. The STNU instance is represented by graph g.
	 * If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level. If the method cannot fix such not-well-defined constraints, it raises a
	 * {@link it.univr.di.cstnu.algorithms.WellDefinitionException}.
	 *
	 * @return true if the graph is a well formed
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	public boolean initAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Starting initial well definition check.");
			}
		}
		this.g.clearCache();
		this.gCheckedCleaned = null;
		this.Z = this.g.getZ();
		this.activationNode = new Object2ObjectOpenHashMap<>();
		this.lowerContingentEdge = new Object2ObjectOpenHashMap<>();
		this.upperContingentEdge = new Object2ObjectOpenHashMap<>();

		// Checks the presence of Z node!
		// this.Z = this.g.getZ(); already done in setG()
		if (this.Z == null) {
			this.Z = this.g.getNode(STNU.ZERO_NODE_NAME);
			if (this.Z == null) {
				// We add by authority!
				this.Z = this.g.getNodeFactory().get(STNU.ZERO_NODE_NAME);
				this.Z.setX(10);
				this.Z.setY(10);
				this.g.addVertex(this.Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + STNU.ZERO_NODE_NAME + " node found: added!");
				}
			}
			this.g.setZ(this.Z);
		} else {
			if (!this.Z.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
				}
				this.Z.setLabel(Label.emptyLabel);
			}
		}

		// Checks well definiteness of edges and determine maxWeight
		int minNegWeight = 0;
		for (final STNUEdge e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, "Initial Checking edge e: " + e);
				}
			}
			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);

			if (s == d) {
				// loop are not admissible
				this.g.removeEdge(e);
				continue;
			}
			if (e.isEmpty()) {
				e.isEmpty();
				this.g.removeEdge(e);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Empty edge " + e + " has been removed.");
					}
				}
				continue;
			}
			int initialValue = e.getValue();
			if (initialValue != Constants.INT_NULL && initialValue < minNegWeight)
				minNegWeight = initialValue;

			if (!e.isContingentEdge()) {
				continue;
			}
			// Check contingent properties!

			int labeledValue = e.getLabeledValue();

			if (initialValue == Constants.INT_NULL && labeledValue == Constants.INT_NULL) {
				throw new WellDefinitionException(
						"Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
			}

			STNUEdge eInverted = this.g.findEdge(d, s);
			if (eInverted == null) {
				throw new WellDefinitionException("Contingent edge " + e + " is alone. The companion contingent edge between " + d.getName()
						+ " and " + s.getName() + " does not exist while it must exist!");
			}
			if (!eInverted.isContingentEdge()) {
				throw new WellDefinitionException("Edge " + e + " is contingent while the companion edge " + eInverted + " is not contingent!\nIt must be!");
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST))
					LOG.log(Level.FINEST, "Edge " + e + " is contingent. Found its companion: " + eInverted);
			}
			/**
			 * Memo.
			 * If current initialValue is negative, current edge is the lower bound C--->A. The lower case labeled value has to be put in the inverted edge.
			 * If the lower case labeled value is already present, it must be equal.
			 * If current initialValue is positive, current edge is the upper bound A--->C. The upper case labeled value has to be put in the inverted edge.
			 * If the upper case labeled value is already present, it must be equal.
			 * if current initialValue is undefined, then we assume that the contingent link is already set.
			 */
			if (initialValue != Constants.INT_NULL) {
				int eInvertedInitialValue;
				int lowerCaseValue = Constants.INT_NULL;
				int upperCaseValue = Constants.INT_NULL;
				eInvertedInitialValue = eInverted.getValue();

				if (initialValue < 0) {
					// e : A<---C
					// d s
					// eInverted : A--->C
					// d s
					// current edge 'e' is the lower bound.
					lowerCaseValue = eInverted.getLabeledValue();
					ALetter contingentALetter = new ALetter(s.getName());

					if (lowerCaseValue != Constants.INT_NULL && -initialValue != lowerCaseValue) {
						throw new WellDefinitionException(
								"Edge " + e + " is contingent with a negative value and the inverted " + eInverted
										+ " already contains a ***different*** lower case value: "
										+ eInverted.getLabeledValueFormatted()
										+ ".");
					}
					if (lowerCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
						throw new WellDefinitionException("Edge " + e + " is contingent with a negative value but the inverted " + eInverted
								+ " does not contain a lower case value neither a proper initial value. ");
					}

					if (lowerCaseValue == Constants.INT_NULL) {
						lowerCaseValue = -initialValue;
						eInverted.setLabeledValue(contingentALetter, lowerCaseValue, false);

						if (!this.contingentAlsoAsOrdinary)
							e.setValue(Constants.INT_NULL);

						if (eInvertedInitialValue != Constants.INT_NULL) {
							upperCaseValue = -eInvertedInitialValue;
							e.setLabeledValue(contingentALetter, upperCaseValue, true);
							if (!this.contingentAlsoAsOrdinary)
								eInverted.setValue(Constants.INT_NULL);

							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LOG.log(Level.FINEST, "Inserted the upper label value: "
											+ e.getLabeledValueFormatted()
											+ " to edge " + e);
								}
							}
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST)) {
								LOG.log(Level.FINEST, "Inserted the lower label value: "
										+ eInverted.getLabeledValueFormatted()
										+ " to edge " + eInverted);
							}
						}
					}
					// In order to speed up the checking, prepare some auxiliary data structure
					this.lowerContingentEdge.put(s, eInverted);
					this.upperContingentEdge.put(s, e);
					this.activationNode.put(s, d);
					s.setContingent(true);

				} else {
					// e : A--->C
					// eInverted : C--->A
					ALetter contingentALetter = new ALetter(d.getName());
					upperCaseValue = eInverted.getLabeledValue();

					if (upperCaseValue != Constants.INT_NULL && -initialValue != upperCaseValue) {
						throw new WellDefinitionException(
								"Edge " + e + " is contingent with a positive value and the inverted " + eInverted
										+ " already contains a ***different*** upper case value: "
										+ eInverted.getLabeledValueFormatted() + ".");
					}
					if (upperCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
						throw new WellDefinitionException("Edge " + e + " is contingent with a positive value but the inverted " + eInverted
								+ " does not contain a upper case value neither a proper initial value. ");
					}

					if (upperCaseValue == Constants.INT_NULL) {
						upperCaseValue = -initialValue;
						eInverted.setLabeledValue(contingentALetter, upperCaseValue, true);

						if (!this.contingentAlsoAsOrdinary)
							e.setValue(Constants.INT_NULL);

						if (eInvertedInitialValue != Constants.INT_NULL) {
							lowerCaseValue = -eInvertedInitialValue;
							e.setLabeledValue(contingentALetter, lowerCaseValue, false);

							if (!this.contingentAlsoAsOrdinary)
								eInverted.setValue(Constants.INT_NULL);

							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LOG.log(Level.FINEST,
											"Inserted the lower label value: " + e.getLabeledValueFormatted()
													+ " to edge " + e);
								}
							}
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST)) {
								LOG.log(Level.FINEST, "Inserted the upper label value: " + eInverted.getLabeledValueFormatted()
										+ " to edge " + eInverted);
							}
						}
					}
					// In order to speed up the checking, prepare some auxiliary data structure
					this.lowerContingentEdge.put(d, e);
					this.upperContingentEdge.put(d, eInverted);
					this.activationNode.put(d, s);
					d.setContingent(true);
				}
			} else {
				// here initialValue is null.
				// UC and LC values are already present.
				Pair pair = e.getNodeLabel();
				if (pair != null) {
					ALetter ctg = pair.getFirst();
					if (pair.getSecond()) {// it is a upper case value
						// check that the node name is correct
						if (!ctg.toString().equals(s.getName())) {
							throw new WellDefinitionException(
									"Edge " + e + " is upper case contingent edge but the name of node is not the name of contingent node: "
											+ "\n upper case label: " + ctg
											+ "\n ctg node: " + s);
						}
						this.activationNode.put(s, d);
						this.upperContingentEdge.put(s, e);
					} else {// it is a lower case value
						if (!ctg.toString().equals(d.getName())) {
							throw new WellDefinitionException(
									"Edge " + e + " is upper case contingent edge but the name of node is not the name of contingent node: "
											+ "\n upper case label: " + ctg
											+ "\n ctg node: " + d);
						}
						this.lowerContingentEdge.put(d, e);
						this.activationNode.put(d, s);
					}
				}
			}
			// it is necessary to check max value
			int m = e.getLabeledValue();
			// LOG.warning("m value: " + m);
			if (m != Constants.INT_NULL && m < minNegWeight)
				minNegWeight = m;
			m = eInverted.getLabeledValue();
			if (m != Constants.INT_NULL && m < minNegWeight)
				minNegWeight = m;
		} // end contingent edges cycle

		// manage maxWeight value
		this.maxWeight = -minNegWeight;

		// Determine horizon value
		long product = ((long) this.maxWeight) * (this.g.getVertexCount() - 1);// Z doesn't count!
		// if (product >= Constants.INT_POS_INFINITE) {
		// throw new ArithmeticException(
		// "Horizon value is not representable by an integer. maxWeight = " + this.maxWeight + ", #vertices = " + this.g.getVertexCount());
		// }
		this.horizon = (int) product;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "The horizon value is " + String.format("%6d", product));
		}

		/*
		 * Checks well definiteness of nodes
		 */
		// final Collection<LabeledNode> nodeSet = this.g.getVertices();
		// for (final LabeledNode node : nodeSet) {
		// // 3. Checks that each node different from Z has an edge to Z
		// if (node == this.Z)
		// continue;
		// boolean added = false;
		// STNUEdge edge = this.g.findEdge(node, this.Z);
		// if (edge == null) {
		// edge = makeNewEdge(node.getName() + "_" + this.Z.getName(), ConstraintType.internal);
		// this.g.addEdge(edge, node, this.Z);
		// edge.setValue(0);
		// added = true;
		// }
		// if (edge.getValue() == Constants.INT_NULL || edge.getValue() > 0) {
		// edge.setValue(0);
		// added = true;
		// }
		// if (Debug.ON) {
		// if (added) {
		// if (LOG.isLoggable(Level.FINE)) {
		// LOG.log(Level.FINE,
		// "Added " + edge.getName() + ": " + node.getName() + "--(0)-->" + this.Z.getName());
		// }
		// }
		// }
		// }
		//
		this.checkStatus.reset();
		this.checkStatus.initialized = true;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Initial well definition check done!");
			}
		}
		return true;
	}

	/**
	 * <p>
	 * isContingentAlsoAsOrdinary.
	 * </p>
	 *
	 * @return the contingentAlsoAsOrdinary
	 */
	public final boolean isContingentAlsoAsOrdinary() {
		return this.contingentAlsoAsOrdinary;
	}

	/**
	 * <p>
	 * isOutputCleaned.
	 * </p>
	 *
	 * @return the fOutputCleaned
	 */
	public boolean isOutputCleaned() {
		return this.cleanCheckedInstance;
	}

	/**
	 * The equivalent normal-form of the distance graph associated to a STNU.
	 * In a normal-form distance-graph, each contingent link has a 0 lower bound.
	 *
	 * @return true if the current graph was made in normal form, false otherwise
	 */
	public boolean makeNormalForm() {
		try {
			initAndCheck();
		} catch (WellDefinitionException e) {
			throw new IllegalArgumentException("The STNU graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		if (Debug.ON) {
			LOG.finer("Normalization started.");
		}

		/**
		 * MAKING LOWER CASE VALUE == 0
		 */
		for (Entry<LabeledNode, STNUEdge> entry : this.lowerContingentEdge.entrySet()) {
			STNUEdge lowerEdge = entry.getValue();
			int lowerValue = lowerEdge.getLabeledValue();
			if (lowerValue == 0)
				continue;

			LabeledNode cntgNode = entry.getKey();
			LabeledNode activNode = this.activationNode.get(cntgNode);
			STNUEdge upperEdge = this.g.findEdge(cntgNode, activNode);

			// Adds the support node
			LabeledNode aNClone = new LabeledNode(activNode);
			aNClone.setName("____" + activNode.getName());
			aNClone.setX(activNode.getX() - 100);
			if (!this.g.addVertex(aNClone))
				throw new IllegalArgumentException("Cannot add node " + aNClone);

			// Move all edges incident to activation node but the contingents ones to support node.
			for (STNUEdge edgeGoing2ActNode : this.g.getInEdges(activNode)) {
				if (edgeGoing2ActNode.equalsByName(upperEdge)) {
					continue;
				}
				LabeledNode s = this.g.getSource(edgeGoing2ActNode);
				this.g.removeEdge(edgeGoing2ActNode);
				edgeGoing2ActNode.setName(s.getName() + "--" + aNClone.getName());
				this.g.addEdge(edgeGoing2ActNode, s, aNClone);
				this.checkStatus.addedEdges++;
			}
			for (STNUEdge edgeOutGoingFromActNode : this.g.getOutEdges(activNode)) {
				if (edgeOutGoingFromActNode.equalsByName(lowerEdge)) {
					continue;
				}
				LabeledNode d = this.g.getDest(edgeOutGoingFromActNode);
				this.g.removeEdge(edgeOutGoingFromActNode);
				edgeOutGoingFromActNode.setName(aNClone.getName() + "--" + d.getName());
				this.g.addEdge(edgeOutGoingFromActNode, aNClone, d);
				this.checkStatus.addedEdges++;
			}
			ALetter aletter = new ALetter(cntgNode.getName());
			lowerEdge.setLabeledValue(aletter, 0, false);
			int newUpperValue = upperEdge.getLabeledValue() + lowerValue;
			upperEdge.setLabeledValue(aletter, newUpperValue, true);

			// connect the clone node to the activation node
			STNUEdge e1 = makeNewEdge(aNClone.getName() + "_-" + activNode.getName());
			e1.setValue(lowerValue);
			this.g.addEdge(e1, aNClone, activNode);
			this.checkStatus.addedEdges++;
			e1 = makeNewEdge(activNode.getName() + "_-" + aNClone.getName());
			e1.setValue(-lowerValue);
			this.g.addEdge(e1, activNode, aNClone);
			this.checkStatus.addedEdges++;
		}
		return true;
	}

	/**
	 * Stores the graph after a check to the file.
	 *
	 * @see #getGChecked()
	 */
	public void saveGraphToFile() {
		if (this.fOutput == null) {
			if (this.fInput == null) {
				LOG.info("Input file and output file are null. It is not possible to save the result in automatic way.");
				return;
			}
			String outputName;
			try {
				outputName = this.fInput.getCanonicalPath().replaceFirst(FILE_NAME_SUFFIX + "$", "");
			} catch (IOException e) {
				System.err.println(
						"It is not possible to save the result. Field fOutput is null and no the standard output file can be created: " + e.getMessage());
				return;
			}
			if (!this.getCheckStatus().finished) {
				outputName += "_notFinishedCheck";
				if (this.getCheckStatus().timeout) {
					outputName += "_timeout_" + this.timeOut;
				}
			} else {
				outputName += "_checked_" + ((this.getCheckStatus().isControllability() ? "DC" : "NOTDC"));
			}
			outputName += FILE_NAME_SUFFIX;
			this.fOutput = new File(outputName);
			LOG.info("Output file name is " + this.fOutput.getAbsolutePath());
		}

		TNGraph<STNUEdge> g1 = this.getGChecked();
		g1.setInputFile(this.fOutput);
		g1.setName(this.fOutput.getName());
		g1.removeEmptyEdges();

		CSTNUStaticLayout<STNUEdge> layout = new CSTNUStaticLayout<>(g1);
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(layout);
		try {
			graphWriter.save(g1, this.fOutput);
		} catch (IOException e) {
			System.err.println(
					"It is not possible to save the result. File " + this.fOutput + " cannot be created: " + e.getMessage());
			return;
		}
		LOG.info("Checked instance saved in file " + this.fOutput.getAbsolutePath());
	}

	/**
	 * <p>
	 * Setter for the field <code>contingentAlsoAsOrdinary</code>.
	 * </p>
	 *
	 * @param contingentAlsoAsOrdinary1 the contingentAlsoAsOrdinary to set
	 */
	public final void setContingentAlsoAsOrdinary(boolean contingentAlsoAsOrdinary1) {
		this.contingentAlsoAsOrdinary = contingentAlsoAsOrdinary1;
	}

	/**
	 * <p>
	 * setDefaultConsistencyCheckAlg.
	 * </p>
	 *
	 * @param defaultConsistencyCheckAlg1 the defaultConsistencyCheckAlg to set
	 */
	public void setDefaultConsistencyCheckAlg(CheckAlgorithm defaultConsistencyCheckAlg1) {
		this.defaultControllabilityCheckAlg = defaultConsistencyCheckAlg1;
	}

	/**
	 * <p>
	 * Setter for the field <code>fOutput</code>.
	 * </p>
	 *
	 * @param fileOutput the file where to save the result.
	 */
	public void setfOutput(File fileOutput) {
		this.fOutput = fileOutput;
	}

	/**
	 * Considers the given graph as the graph to check (graph will be modified).
	 * Clear all internal parameter.
	 *
	 * @param graph set internal TNGraph to g. It cannot be null.
	 */
	public void setG(TNGraph<STNUEdge> graph) {
		if (graph == null)
			throw new IllegalArgumentException("Input graph is null!");
		this.reset();
		this.g = graph;
		this.Z = graph.getZ();// Don't remove this assignment!
	}

	/**
	 * Set to true for having the result graph cleaned of empty edges and labeled values having unknown literals.
	 *
	 * @param clean the resulting graph
	 */
	public void setOutputCleaned(boolean clean) {
		this.cleanCheckedInstance = clean;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 *
	 * @param name the proposed name. If an edge with name already exists, then name is modified adding an suitable integer such that the name becomes unique
	 *            in 'g'.
	 * @param type the type of edge to create.
	 * @return an edge with a unique name.
	 */
	private STNUEdge makeNewEdge(final String name, final Edge.ConstraintType type) {
		int i = this.g.getEdgeCount();
		String name1 = name;
		while (this.g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final STNUEdge e = this.g.getEdgeFactory().get(name1);
		e.setConstraintType(type);
		return e;
	}

	/**
	 * Help method setting type = ConstraintType.internal
	 * 
	 * @param name the name for the edge
	 * @return the new edge
	 * @see #makeNewEdge(String, ConstraintType)
	 */
	private STNUEdge makeNewEdge(final String name) {
		return this.makeNewEdge(name, ConstraintType.internal);
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args the input args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	@SuppressWarnings({ "deprecation" })
	private boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			if (this.fInput == null) {
				try (Scanner consoleScanner = new Scanner(System.in, "UTF-8")) {
					System.out.print("Insert STNU file name (absolute file name): ");
					String fileName = consoleScanner.next();
					this.fInput = new File(fileName);
				}
			}
			if (!this.fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (this.fOutput != null) {
				if (this.fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!this.fOutput.getName().endsWith(".stnu")) {
					if (!this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".stnu"))) {
						String m = "File " + this.fOutput.getAbsolutePath() + " cannot be renamed.";
						LOG.severe(m);
						throw new RuntimeException(m);
					}
				}
				if (this.fOutput.exists()) {
					if (!this.fOutput.delete()) {
						String m = "File " + this.fOutput.getAbsolutePath() + " cannot be deleted.";
						LOG.severe(m);
						throw new RuntimeException(m);
					}
				}
			}
		} catch (final CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass().getName() + " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-*.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " file_name");
			return false;
		}
		return true;
	}

	/**
	 * Determines the minimal distance in this STN between any node and the sink one (node Z) using the BellmanFord algorithm.
	 * The minimal distance is stored as potential value in each node.<br>
	 * If the graph contains a negative cycle, it returns false.<br>
	 * For dc-checking STNUs, Morris' 2014 algorithm is the easiest algorithm to implement because it
	 * does not need any potential functions to reweight the edges in the graph.<br>
	 * It only back-propagates along non-negative edges. It makes recursive function calls to dcbackprop. use a global vector to
	 * keep track of status of each "negative node": *not-yet-encountered*, *already-started*, *successfully-completed*.<br>
	 * That way, when you enter a recursive call, you can check its status (see first few lines of pseudocode for the algorithm in morris-2014) before
	 * proceeding.
	 * 
	 * @return true if the graph is dynamic controllable (DC), false otherwise.
	 */
	boolean morris2014() {
		this.contingentAlsoAsOrdinary = false; // Morris consider only labeled value for contingent links.
		if (!this.makeNormalForm())
			return false;
		// determine negative nodes, i.e., nodes target of negative ordinary edges or upper-case edge.
		Object2ObjectMap<LabeledNode, ElementStatus> negativeNodes = new Object2ObjectOpenHashMap<>();
		for (LabeledNode node : this.g.getVerticesArray()) {
			for (STNUEdge inEdge : this.g.getInEdges(node)) {
				if (getUpperOrOrdinaryValue(inEdge) < 0) {
					negativeNodes.put(node, ElementStatus.unstarted);
				}
			}
		}
		assert negativeNodes.size() == negativeNodes.keySet().size();

		// main morris2014 cycle
		for (LabeledNode X : negativeNodes.keySet()) {
			if (!morris2014DCBackpropagation(X, negativeNodes))
				return false;
		}
		return true;
	}

	/**
	 * Recursive procedure for {@link #morris2014()} method.
	 * Calculates distance backwards from potential moat edges.
	 * 
	 * @param X must not be null
	 * @param negativeNodes a set of negative nodes. It must contain source.
	 * @return true if no negative semi-reducible was met. False, otherwise.
	 */
	private boolean morris2014DCBackpropagation(LabeledNode X, Object2ObjectMap<LabeledNode, ElementStatus> negativeNodes) {
		if (Debug.ON) {
			LOG.finer("X node  " + X.getName() + " starts.");
		}
		ElementStatus sourceStatus = negativeNodes.get(X);
		if (sourceStatus.equals(ElementStatus.started)) {
			if (Debug.ON) {
				LOG.info("Found a semi-reducibile negative cycle since source node " + X.getName() + " was already met.");
			}
			return false;
		}
		if (sourceStatus.equals(ElementStatus.finished)) {
			if (Debug.ON) {
				LOG.finer("Source node  " + X.getName() + " was already analyzed. Continue.");
			}
			return true;
		}
		this.checkStatus.cycles++;// Counts how many time this procedure was called;
		negativeNodes.put(X, ElementStatus.started);

		Object2IntOpenHashMap<LabeledNode> distance = new Object2IntOpenHashMap<>(this.g.getVertexCount());
		distance.defaultReturnValue(Constants.INT_POS_INFINITE);
		distance.put(X, 0);

		PriorityQueue<LabeledNode> queue = new PriorityQueue<>();
		for (STNUEdge e2Source : this.g.getInEdges(X)) {
			int v = getUpperOrOrdinaryValue(e2Source);
			if (v >= 0)
				continue;
			LabeledNode s = this.g.getSource(e2Source);
			if (Debug.ON) {
				LOG.finer("Queue.add(" + s.getName() + ", " + v + ")");
			}
			distance.put(s, v);
			queue.insertOrDecrease(s, v);
		}
		assert queue.size() != 0;
		while (queue.size() != 0) {
			LabeledNode U = queue.extractMin();
			if (Debug.ON) {
				LOG.finest("Analyze X node " + X.getName() + " and U node " + U.getName());
			}
			int distU = distance.getInt(U);
			if (distU >= 0) {
				STNUEdge newE = this.g.findEdge(U, X);
				if (newE == null) {
					newE = makeNewEdge(U.getName() + '_' + X.getName(), ConstraintType.internal);
					newE.setValue(distU);
					this.g.addEdge(newE, U, X);
					this.checkStatus.addedEdges++;
					if (Debug.ON) {
						LOG.finer("Added " + newE.toString() + ". Continue with the next node.");
					}
				} else {
					if (distU < newE.getValue() || newE.getValue() == Constants.INT_NULL) {
						newE.setValue(distU);
						if (Debug.ON) {
							LOG.finer("Adjusted " + newE.toString() + ". Continue with the next node.");
						}
					}
				}
				continue;
			}
			if (negativeNodes.containsKey(U)) {
				if (Debug.ON) {
					LOG.finer("Node " + U.getName() + " is negative. Analyze it recursively.");
				}
				if (!morris2014DCBackpropagation(U, negativeNodes))
					return false;
			}

			for (STNUEdge eVU : this.g.getInEdges(U)) {
				// it is possible that there exist edges like A---(c:4),5--->C
				LabeledNode V = this.g.getSource(eVU);
				if (eVU.isLowerCase()) {
					if (X != V) {
						morris2014UpdateDistance(V, eVU.getLabeledValue(), distU, queue, distance);
					} else {
						if (Debug.ON) {
							LOG.finer("Found an unsuitable edge " + eVU);
						}
					}
				}
				int valueVU = eVU.getValue();
				if (valueVU == Constants.INT_NULL || valueVU < 0)
					continue;
				morris2014UpdateDistance(V, valueVU, distU, queue, distance);
			}
		}
		if (Debug.ON) {
			LOG.finer("Source node " + X.getName() + " completed.");
		}
		negativeNodes.put(X, ElementStatus.finished);
		return true;
	}

	/**
	 * Updates the distance values between node (V) preceding the source (U) of a negative edge and the destination node (X) of the negative edge.
	 * 
	 * @param V
	 * @param valueVU Assumed to be >= 0.
	 * @param distU
	 * @param queue
	 * @param distance
	 */
	private static void morris2014UpdateDistance(LabeledNode V, int valueVU, int distU, PriorityQueue<LabeledNode> queue,
			Object2IntOpenHashMap<LabeledNode> distance) {

		int newValue = Constants.sumWithOverflowCheck(distU, valueVU);
		if (newValue < distance.getInt(V)) {
			if (Debug.ON) {
				LOG.finer("Update distance of " + V.getName() + " using edge value " + valueVU + " and distance " + distU + ": old value: "
						+ distance.getInt(V) + " new: " + newValue);
			}
			distance.put(V, newValue);
			assert distance.getInt(V) == newValue;
			queue.insertOrDecrease(V, newValue);
			assert queue.getPriority(V) == newValue;
		}
	}

	/**
	 * Data structure for RUL algorithm.
	 * 
	 * @author posenato
	 */
	private static class RULGlobalInfo {
		/**
		 * Potential of each node.
		 */
		Object2IntMap<LabeledNode> nodePotential;

		/**
		 * Status of each upperCase edge
		 */
		Object2ObjectMap<STNUEdge, ElementStatus> upperCaseEdgeStatus;

		/**
		 * upperCaseEdgeFromActivation for rurl2020OneStepBackProp
		 */
		Object2ObjectMap<LabeledNode, STNUEdge> upperCaseEdgeFromActivation;

		RULGlobalInfo(Object2IntMap<LabeledNode> potential, int nUpperCaseEdges) {
			this.nodePotential = potential;
			this.upperCaseEdgeStatus = new Object2ObjectOpenHashMap<>(nUpperCaseEdges);
			this.upperCaseEdgeStatus.defaultReturnValue(ElementStatus.unstarted);
			this.upperCaseEdgeFromActivation = null;
		}
	}

	/**
	 * Data structure for RUL algorithm.
	 * 
	 * @author posenato
	 */
	private static class RULLocalInfo {
		/**
		 * Node distance.
		 */
		Object2IntMap<LabeledNode> distanceFrom;

		/**
		 * CCLoop is true if the loop $C$ to $C$ has length < Delta_C
		 */
		boolean ccLoop;

		/**
		 * Edges to check
		 */
		Object2ObjectMap<LabeledNode, STNUEdge> unstartedUCEdges;

		/**
		 * Default constructor
		 * 
		 * @param defaultDistance default distance when map does not contasin the key
		 */
		RULLocalInfo(int defaultDistance) {
			this.distanceFrom = new Object2IntOpenHashMap<>();
			this.distanceFrom.defaultReturnValue(defaultDistance);
			this.ccLoop = false;
			this.unstartedUCEdges = null;
		}

	}

	/**
	 * RUL^- algorithm<br>
	 * 
	 * @return true if the graph is dynamic controllable (DC), false otherwise.
	 */
	boolean rul2018() {
		this.contingentAlsoAsOrdinary = false; // RUL consider only labeled value in contingent links.
		int k;

		if (this.getG() == null)
			return false;

		k = this.getG().getContingentNodeCount();

		Object2IntMap<LabeledNode> h = bellmanFordOL();
		if (h == null) {
			if (Debug.ON) {
				LOG.info("Found an incosistency in G_LO graph. Giving up!");
			}
			return false;
		}
		if (k == 0) {
			// it is an STN
			if (Debug.ON) {
				LOG.info("The CSTNU ha no contingent time point! Finished!");
			}
			return true;
		}
		if (Debug.ON) {
			LOG.info("RUL2018 started");
		}
		ObjectArrayList<LabeledNode> U = new ObjectArrayList<>(this.upperContingentEdge.keySet());
		assert U.size() == k : "Number of contingents node is not equal to the number of upper case edges: " + k + "," + U.size();
		ObjectArrayList<LabeledNode> S = new ObjectArrayList<>();
		S.push(U.top());// push arbitrary element of U onto S keeping in U
		assert U.size() == k : "U.top() removed a node";

		if (Debug.ON) {
			LOG.finer("Stack U (size: " + U.size() + "): " + U.toString());
			LOG.finer("Stack S: " + S.toString());
		}
		while (S.size() != 0) {
			this.checkStatus.cycles++;// Counts how many time this procedure was called;

			LabeledNode C = S.top();
			LabeledNode A = this.getActivationNode().get(C);
			final int x = this.lowerContingentEdge.get(C).getLabeledValue();
			final int y = -this.upperContingentEdge.get(C).getLabeledValue();
			final int DeltaC = y - x;
			if (Debug.ON) {
				LOG.finer("Considering contingent link (" + A.getName() + ", " + x + ", " + y + ", " + C.getName() + ")");
			}
			rul2018CloseRelaxLower(h, C, DeltaC);
			rul2018ApplyUpper(A, x, y, C);
			h = rul2018UpdatePotential(h, A);
			if (h == null) {
				if (Debug.ON) {
					LOG.info("Found an incosistency in G_LO graph. Giving up!");
				}
				return false;
			}
			if (Debug.ON) {
				LOG.finer("Considering other contingent t.p. w.r.t. contingent link (" + A.getName() + ", " + x + ", " + y + ", " + C.getName() + ")");
			}
			LabeledNode C1 = null;
			int eValue;
			for (LabeledNode C1i : U) {
				if (C1i == C)
					continue;
				final LabeledNode A1 = this.getActivationNode().get(C1i);
				assert A1 != null : "A1 must be an activation time-point.";
				final STNUEdge e = this.g.findEdge(A1, C);
				if (e == null)
					continue;
				eValue = e.getValue();
				if (eValue == Constants.INT_NULL)
					continue;
				if (eValue < DeltaC) {
					C1 = C1i;
					break;
				}
			}
			if (C1 != null) {
				if (Debug.ON) {
					LOG.finer("Found contingent t.p. " + C1 + " w.r.t. contingent link (" + A.getName() + ", " + x + ", " + y + ", " + C.getName() + ")");
				}

				if (S.contains(C1)) {
					if (Debug.ON) {
						LOG.info("Found a negative cycle involving " + C1 + " and " + C + ". Giving up!");
					}
					return false;
				}
				S.push(C1);
			} else {
				if (Debug.ON) {
					LOG.finer("Found no other contingent t.p. w.r.t. contingent link (" + A.getName() + ", " + x + ", " + y + ", " + C.getName() + ")");
				}
				U.remove(C);
				S.pop();
				if (U.size() != 0 && S.size() == 0) {
					S.push(U.top());
				}
				if (Debug.ON) {
					LOG.finer("Stack U (size: " + U.size() + "): " + U.toString());
					LOG.finer("Stack S: " + S.toString());
				}
			}
		}
		return true;
	}

	/**
	 * RUL^- algorithm: cloreRelaxLower procedure<br>
	 * Adds to this.g all ordinary edges (V,v,C) that can be generated by applications of the RELAX^- and LOWER^- rules.
	 * 
	 * @param h the potential to update
	 * @param C a contingent time point
	 * @param DeltaC the y-c value of contingent link associated to C.
	 */
	private void rul2018CloseRelaxLower(Object2IntMap<LabeledNode> h, LabeledNode C, int DeltaC) {
		if (Debug.ON) {
			LOG.finer("rul2018CloseRelaxLower with " + C.getName() + ", Delta: " + DeltaC);
		}
		PriorityQueue<LabeledNode> Q = new PriorityQueue<>();
		LabeledNode W;
		for (STNUEdge e : this.g.getInEdges(C)) {
			if (!e.isOrdinaryEdge())
				continue;
			W = this.g.getSource(e);
			Q.insertOrDecrease(W, Constants.sumWithOverflowCheck(h.getInt(W), e.getValue()));
		}
		if (Debug.ON) {
			LOG.finer("Q: " + Q.toString());
		}

		while (Q.size() != 0) {
			W = Q.extractMin();
			Object2IntMap<LabeledNode> edges2C = rul2018ApplyRelaxLower(W, C, DeltaC);
			if (Debug.ON) {
				LOG.finer("Node W: " + W + ". edges2C: " + edges2C.toString());
			}
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<LabeledNode> entry : edges2C.object2IntEntrySet()) {
				LabeledNode V = entry.getKey();
				int v = entry.getIntValue();
				STNUEdge currVC = this.g.findEdge(V, C);
				if (Debug.ON) {
					LOG.finer("currVC: (" + V.getName() + ", " + v + ", " + C.getName() + ")=" + currVC);
				}
				int min;
				if (currVC == null) {
					STNUEdge newE = makeNewEdge(V.getName() + "__" + C.getName());
					newE.setValue(v);
					this.g.addEdge(newE, V, C);
					this.checkStatus.addedEdges++;
					if (Debug.ON) {
						LOG.finer("Added edge: " + newE.toString());
					}
					min = v;
				} else {
					min = currVC.getValue();
					if (min == Constants.INT_NULL || v < min) {
						currVC.setValue(v);
						min = v;
						if (Debug.ON) {
							LOG.finer("Adjusted edge: " + currVC.toString());
						}
					}
				}
				if (Debug.ON) {
					LOG.finer("V pot: " + h.getInt(V) + ". min " + min);
				}
				int newKey = Constants.sumWithOverflowCheck(h.getInt(V), min);
				Q.insertOrDecrease(V, newKey);
				if (Debug.ON) {
					LOG.finer("Fixed: " + V.getName() + " at " + newKey + " in Q.");
				}
			}
		}
	}

	/**
	 * RUL^- algorithm: applyRelaxLower procedure<br>
	 * 
	 * @param W a node
	 * @param C a contingen time-point
	 * @param DeltaC the y-x value of the contingent link associated to C
	 * @return The set of all edges (V,v,C) obtained by applying RELAX^- or LOWER^- to ordinary or lower-case edge
	 *         (V,u,W) and ordinary edge (W,l,C).
	 */
	private Object2IntMap<LabeledNode> rul2018ApplyRelaxLower(LabeledNode W, LabeledNode C, int DeltaC) {
		Object2IntMap<LabeledNode> newEdges = new Object2IntOpenHashMap<>();
		STNUEdge eWC = this.g.findEdge(W, C);
		int deltaWC = (eWC == null || !eWC.isOrdinaryEdge()) ? Constants.INT_POS_INFINITE : eWC.getValue();
		if (deltaWC >= DeltaC)
			return newEdges;
		if (W.isContingent()) {
			LabeledNode Aw = this.activationNode.get(W);
			assert Aw != null;
			assert Aw != C;
			STNUEdge lowerEdgeAW = this.lowerContingentEdge.get(W);
			if (Debug.ON) {
				LOG.finer("W is contingent: " + W.getName() + ". Activation node is " + Aw + ". x: " + lowerEdgeAW.getLabeledValue() +
						" Added: (" + Aw.getName() + ", " + lowerEdgeAW.getLabeledValue() + deltaWC + ")");
			}
			newEdges.put(Aw, Constants.sumWithOverflowCheck(lowerEdgeAW.getLabeledValue(), deltaWC));
			return newEdges;
		}
		assert !W.isContingent();
		for (STNUEdge eVW : this.g.getInEdges(W)) {
			assert eVW.isLowerCase() == false;
			LabeledNode V = this.g.getSource(eVW);
			if (V == C)
				continue;
			int eVWvalue = eVW.getValue();
			if (eVWvalue == Constants.INT_NULL) // it is an upper-case
				continue;
			newEdges.put(V, Constants.sumWithOverflowCheck(eVWvalue, deltaWC));
			if (Debug.ON) {
				LOG.finer("W is not contingent: " + W.getName() + ". V:" + V.getName() + ". delta_vw: " + eVWvalue +
						" Added: (" + V.getName() + ", " + eVWvalue + deltaWC + ")");
			}
		}
		return newEdges;
	}

	/**
	 * RUL^- algorithm: applyUpper procedure<br>
	 * Adds to the graph all new edges (V,w,A) obtained by applying UPPER^- to any ordinary edge (V,v,C) and the UC-edge (C,C:-y,A).
	 * 
	 * @param C a contingent time point
	 * @param A the activation time point of contingent link (A,C)
	 * @param x the lower bound of contingent link (A,C)
	 * @param y the upper bound of contingent link (A,C)
	 */
	private void rul2018ApplyUpper(LabeledNode A, int x, int y, LabeledNode C) {
		final int DeltaC = y - x;
		for (STNUEdge eVC : this.g.getInEdges(C)) {
			int v = eVC.getValue();
			if (v == Constants.INT_NULL)
				continue;
			LabeledNode V = this.g.getSource(eVC);
			STNUEdge eVA = this.g.findEdge(V, A);
			int deltaVA = (eVA == null || !eVA.isOrdinaryEdge()) ? Constants.INT_POS_INFINITE : eVA.getValue();
			int newDeltaVA = (v < DeltaC) ? Math.min(deltaVA, -x) : Math.min(deltaVA, v - y);
			if (eVA == null) {
				eVA = makeNewEdge(V.getName() + "_" + A.getName());
				eVA.setValue(newDeltaVA);
				this.g.addEdge(eVA, V, A);
				this.checkStatus.addedEdges++;
				if (Debug.ON) {
					LOG.finer("Added edge: " + eVA.toString());
				}
			} else {
				eVA.setValue(newDeltaVA);
				if (Debug.ON) {
					LOG.finer("Adjusted edge: " + eVA.toString());
				}
			}
		}
	}

	/**
	 * RUL^- algorithm: updatePotential procedure<br>
	 * 
	 * @param h out-of-date potential
	 * @param A an activation time point
	 * @return the update potential considering edges terminating at A.
	 */
	private Object2IntMap<LabeledNode> rul2018UpdatePotential(Object2IntMap<LabeledNode> h, LabeledNode A) {

		Object2IntMap<LabeledNode> newH = new Object2IntOpenHashMap<>(h);
		PriorityQueue<LabeledNode> newQ = new PriorityQueue<>();

		newQ.insertOrDecrease(A, 0);
		LabeledNode V;
		int w;
		while (newQ.size() != 0) {
			BasicEntry<LabeledNode> entry = newQ.extractMinEntry();
			LabeledNode W = entry.getKey();
			for (STNUEdge eVW : this.g.getInEdges(W)) {
				V = this.g.getSource(eVW);
				if (eVW.isLowerCase()) {
					// redundant code but useful for checking the correctness
					w = eVW.getLabeledValue();
					int Vpot = newH.getInt(V);
					int newVpot = Constants.sumWithOverflowCheck(newH.getInt(W), -w);
					if (Vpot < newVpot) {
						newH.put(V, newVpot);
						int newKey = Constants.sumWithOverflowCheck(h.getInt(V), -newVpot);
						newQ.insertOrDecrease(V, newKey);
					}
				}
				w = eVW.getValue();
				if (w == Constants.INT_NULL)// it is an UpperCase
					continue;
				int Vpot = newH.getInt(V);
				int newVpot = Constants.sumWithOverflowCheck(newH.getInt(W), -w);
				if (Vpot < newVpot) {
					newH.put(V, newVpot);
					int newKey = Constants.sumWithOverflowCheck(h.getInt(V), -newVpot);
					if (!newQ.insertOrDecrease(V, newKey))
						return null;
				}
			}
		}
		return newH;
	}

	/**
	 * Determines the minimal distance between source node (added by the method) and any node using the BellmanFord algorithm.
	 * The minimal distance is determined considering the ordinary an the lower case values of the input STNU.
	 * The minimal distance is returned as map (node, value).
	 * If the graph contains a negative cycle, it returns null.
	 * 
	 * @return the potential if the network is consistent, null otherwise.
	 */
	Object2IntMap<LabeledNode> bellmanFordOL() {
		final Collection<LabeledNode> nodes = this.g.getVertices();
		final int n = nodes.size();
		final Collection<STNUEdge> edges = this.g.getEdges();
		int v;
		Object2IntOpenHashMap<LabeledNode> h = new Object2IntOpenHashMap<>(n);
		h.defaultReturnValue(0);
		for (LabeledNode V : this.g.getVertices()) {
			h.put(V, 0);
		}

		LabeledNode s, d;
		int w;
		for (int i = 1; i < n; i++) {// n-1 rounds
			for (STNUEdge e : edges) {
				w = getMinValueBetweenOrdinaryAndLowerCaseValue(e);// OK
				if (w == Constants.INT_NULL)// it is an upper edge
					continue;
				s = this.g.getSource(e);
				d = this.g.getDest(e);
				v = Constants.sumWithOverflowCheck(h.getInt(d), -w);
				if (h.getInt(s) < v) {
					if (Debug.ON) {
						LOG.finest("BF: " + s.getName() + " value from " + Constants.formatInt(h.getInt(s)) + " to " + Constants.formatInt(v));
					}
					h.put(s, v);
				}
			}
		}
		// check if a negative cycle is present
		for (STNUEdge e : edges) {
			w = getMinValueBetweenOrdinaryAndLowerCaseValue(e);// OK
			if (w == Constants.INT_NULL)// it is an upper edge
				continue;
			s = this.g.getSource(e);
			d = this.g.getDest(e);
			v = Constants.sumWithOverflowCheck(h.getInt(d), -w);
			if (h.getInt(s) < v) {
				if (Debug.ON) {
					STN.LOG.finer("BF inconsitency:" + s.getName() + " value from "
							+ Constants.formatInt(h.getInt(s)) + " to " + Constants.formatInt(v));
				}
				this.checkStatus.setControllability(false);
				this.checkStatus.finished = true;
				return null;
			}
		}
		if (Debug.ON) {
			LOG.finest("BF: potential determined: " + h);
		}
		return h;
	}

	/**
	 * Luke's version of RUL^- algorithm: recRULdcCheck procedure.<br>
	 * 
	 * @return true if the graph is dynamic controllable (DC), false otherwise.
	 */
	boolean rul2020() {
		this.contingentAlsoAsOrdinary = false; // RUL consider only labeled value in contingent links.
		int k;

		if (this.getG() == null)
			return false;
		k = this.getG().getContingentNodeCount();

		RULGlobalInfo globalInfo = new RULGlobalInfo(bellmanFordOL(), k);
		if (globalInfo.nodePotential == null) {
			if (Debug.ON) {
				LOG.info("Found an inconsistency in G_LO graph. Giving up!");
			}
			return false;
		}
		if (k == 0) {
			// it is an STN
			if (Debug.ON) {
				LOG.info("The CSTNU ha no contingent time point! Finished!");
			}
			return true;
		}

		// Prepare some auxiliary data structure necessary for rul2020OneStepBackProp
		globalInfo.upperCaseEdgeFromActivation = new Object2ObjectOpenHashMap<>();

		for (Entry<LabeledNode, STNUEdge> entry : this.upperContingentEdge.entrySet()) {
			STNUEdge upperCaseEdge = entry.getValue();
			LabeledNode actNode = this.activationNode.get(entry.getKey());
			globalInfo.upperCaseEdgeFromActivation.put(actNode, upperCaseEdge);
		}

		assert k == globalInfo.upperCaseEdgeFromActivation.size() : "Number of contingents is not equal to the number of upper case edges: " + k + ","
				+ globalInfo.upperCaseEdgeFromActivation.size();

		for (STNUEdge upperCaseEdge : this.upperContingentEdge.values()) {
			if (!rul2020BackPropagation(upperCaseEdge, globalInfo)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Luke's version of RUL^- back propagation algorithm.<br>
	 * Side effects: Modifies contents of graph and globalInfo.
	 * 
	 * @param currentUEdge an upper-case edge (C,C:-y,A).
	 * @param globalInfo global data structure for the algorithm
	 * @return true if no negative circuit was found (graph is still DC), false otherwise.
	 */
	private boolean rul2020BackPropagation(STNUEdge currentUEdge, RULGlobalInfo globalInfo) {
		ElementStatus statusCurrentUEdge = globalInfo.upperCaseEdgeStatus.get(currentUEdge);
		if (Debug.ON) {
			LOG.finer("Check upper-case edge: " + currentUEdge + ". Status: " + statusCurrentUEdge);
		}
		if (statusCurrentUEdge == ElementStatus.started) {
			return false;
		}
		if (statusCurrentUEdge == ElementStatus.finished) {
			return true;
		}
		this.checkStatus.cycles++;// Counts how many time this procedure was called;

		globalInfo.upperCaseEdgeStatus.put(currentUEdge, ElementStatus.started);
		LabeledNode C = this.g.getSource(currentUEdge);
		LabeledNode A = this.g.getDest(currentUEdge);
		int y = -currentUEdge.getLabeledValue();
		STNUEdge currentEdgeInverted = this.g.findEdge(A, C);
		int x = currentEdgeInverted.getLabeledValue();
		int DeltaC = y - x;
		if (Debug.ON) {
			assert (DeltaC > 0);
			LOG.finest("Delta upper-case edge: " + DeltaC);
		}
		RULLocalInfo localInfo = new RULLocalInfo(Constants.INT_POS_INFINITE);

		// queue contains the adjusted distance from X to C
		PriorityQueue<LabeledNode> queue = new PriorityQueue<>();

		for (STNUEdge e : this.g.getInEdges(C)) {
			if (!e.isOrdinaryEdge())
				continue;
			LabeledNode X = this.g.getSource(e);
			queue.insertOrDecrease(X, Constants.sumWithOverflowCheck(globalInfo.nodePotential.getInt(X), e.getValue()));
		}
		if (Debug.ON) {
			LOG.finest("Queue: " + queue);
		}
		boolean cycle = true;
		do {
			if (!rul2020OneStepBackProp(C, DeltaC, queue, globalInfo, localInfo))
				return false;
			if (localInfo.unstartedUCEdges.size() != 0) {
				for (STNUEdge e : localInfo.unstartedUCEdges.values()) {
					if (!rul2020BackPropagation(e, globalInfo)) {
						return false;
					}
				}
				queue.clear();
				for (LabeledNode X : localInfo.unstartedUCEdges.keySet()) {
					queue.insertOrDecrease(X, Constants.sumWithOverflowCheck(localInfo.distanceFrom.getInt(X), globalInfo.nodePotential.getInt(X)));
					localInfo.distanceFrom.put(X, Constants.INT_POS_INFINITE);
				}
			} else {
				cycle = false;
			}
		} while (cycle);
		if (Debug.ON) {
			LOG.finest("localInfo.distanceFrom: " + localInfo.distanceFrom);
		}
		if (localInfo.ccLoop && rul2020FwdPropNotDC(C, DeltaC, localInfo.distanceFrom, globalInfo.nodePotential))
			return false;

		boolean addedEdge = false;
		for (LabeledNode X : this.g.getVertices()) {
			if (X == C)
				continue;
			int deltaXC = localInfo.distanceFrom.getInt(X);
			if (deltaXC == Constants.INT_POS_INFINITE || deltaXC < DeltaC)
				continue;
			int newValueXA = deltaXC - y;
			STNUEdge eXA = this.g.findEdge(X, A);
			if (eXA == null) {
				eXA = makeNewEdge(X.getName() + "_" + A.getName());
				eXA.setValue(newValueXA);
				this.g.addEdge(eXA, X, A);
				this.checkStatus.addedEdges++;
				if (Debug.ON) {
					LOG.fine("Added edge: " + eXA);
				}
			} else {
				if (Debug.ON) {
					LOG.fine("Update edge: " + eXA + " to value " + newValueXA);
				}
				if (newValueXA < eXA.getValue()) {
					eXA.setValue(newValueXA);
				}
			}
			addedEdge = true;
		}
		if (addedEdge) {
			globalInfo.nodePotential = rul2018UpdatePotential(globalInfo.nodePotential, A);
		}
		if (globalInfo.nodePotential == null) {
			if (Debug.ON) {
				LOG.finer("The determination of new potential from " + A + " found an inconsistency. Giving up!");
			}
			return false;
		}
		globalInfo.upperCaseEdgeStatus.put(currentUEdge, ElementStatus.finished);
		return true;
	}

	/**
	 * Luke's version of RUL^- one step back propagation algorithm.<br>
	 * Side effects: Modifies contents of localInfo.
	 * 
	 * @param C contingent time-point
	 * @param DeltaC the difference y-x of contingent link associated to C
	 * @param Q a priority queue
	 * @param globalInfo the global checking data structure
	 * @param localInfo the local checking data structure
	 * @return false iff back-propagation from C reveals STNU to be non-DC.
	 */
	private boolean rul2020OneStepBackProp(LabeledNode C, int DeltaC, PriorityQueue<LabeledNode> Q, RULGlobalInfo globalInfo,
			RULLocalInfo localInfo) {

		localInfo.unstartedUCEdges = new Object2ObjectOpenHashMap<>();
		if (Debug.ON) {
			LOG.finest("OneStepBackProp start. Node C: " + C
					+ "\tDeltaC: " + DeltaC
					+ "\tQueue: " + Q);
		}
		while (Q.size() != 0) {
			BasicEntry<LabeledNode> minEntry = Q.extractMinEntry();
			LabeledNode X = minEntry.getKey();
			int Xkey = minEntry.getIntValue();
			int deltaXC = Xkey - globalInfo.nodePotential.getInt(X);
			STNUEdge upperCaseEdgeFromX = globalInfo.upperCaseEdgeFromActivation.get(X);

			if (Debug.ON) {
				LOG.finest("OneStepBackProp. Considering node " + X
						+ "\tXkey: " + Xkey
						+ "\tdeltaXC: " + deltaXC
						+ "\tupper-case edge from " + X + ": " + upperCaseEdgeFromX
						+ "\tdistanceFrom " + X + ": " + Constants.formatInt(localInfo.distanceFrom.getInt(X)));
			}

			if (deltaXC >= localInfo.distanceFrom.getInt(X)) {
				continue;
			}

			localInfo.distanceFrom.put(X, deltaXC);
			if (Debug.ON) {
				LOG.finest("OneStepBackProp. New distanceFrom for " + X + ": " + deltaXC);
			}

			if (deltaXC >= DeltaC) {
				if (Debug.ON) {
					LOG.finest("OneStepBackProp. New distanceFrom for " + X + " is greater than DeltaC " + DeltaC);
				}
				continue;
			}

			if (X == C) {
				if (Debug.ON) {
					LOG.finest("OneStepBackProp. Node X == C: " + X + "==" + C);
				}
				if (deltaXC < 0) {// case 2
					if (Debug.ON) {
						LOG.finest("OneStepBackProp. Node X == C and deltaXC < 0. Giving up! deltaC: " + deltaXC);
					}
					return false;
				}
				localInfo.ccLoop = true;
				if (Debug.ON) {
					LOG.finest("OneStepBackProp. ccLoop true");
				}
			} else {
				ElementStatus ucFromXStatus = globalInfo.upperCaseEdgeStatus.get(upperCaseEdgeFromX);
				if (Debug.ON) {
					LOG.finest("OneStepBackProp. status of edge from " + X + ". Edge: " + upperCaseEdgeFromX + ". Status " + ucFromXStatus);
				}
				if (upperCaseEdgeFromX != null) {
					if (ucFromXStatus == ElementStatus.unstarted) {
						localInfo.unstartedUCEdges.put(X, upperCaseEdgeFromX);// case 3a
						continue;
					}
					if (ucFromXStatus == ElementStatus.started) {
						if (Debug.ON) {
							LOG.finer("Found case 3b. Giving up!");
						}
						return false;// case 3b
					}
				}
				// case 3c or 4
				for (BasicEntry<LabeledNode> entry : rul2020NewApplyRelaxLower(X, DeltaC, deltaXC)) {
					LabeledNode W = entry.getKey();
					int deltaWC = entry.getIntValue();
					STNUEdge wc = this.g.findEdge(W, C);
					if (wc != null && !wc.isOrdinaryEdge())// it does not represent an ordinary edge, so ignore it!
						wc = null;
					if (Debug.ON) {
						LOG.finest("OneStepBackProp. (W,deltaWC): " + entry + ".  Edge WC:" + wc);
					}
					if (wc == null || deltaWC < wc.getValue()) {
						int newKey = Constants.sumWithOverflowCheck(deltaWC, globalInfo.nodePotential.getInt(W));
						if (Debug.ON) {
							LOG.finest("OneStepBackProp. potential(" + W + "): " + globalInfo.nodePotential.getInt(W)
									+ "\t newKey for " + W + ": " + newKey);
						}
						if (Debug.ON) {
							PriorityQueue.Status Wstatus = Q.getStatus(W);
							LOG.finest("OneStepBackProp. queue status of node " + W + ": " + Wstatus);
						}
						Q.insertOrDecrease(W, newKey);
						if (Debug.ON) {
							LOG.finest("OneStepBackProp. Queue after adding " + W + ":" + Q);
						}
					}
				}
			}
		}
		return true;

	}

	/**
	 * Luke's implementation of RUL^- algorithm: fwdPropNotDC<br>
	 * 
	 * @param V
	 * @param DeltaC
	 * @param deltaVC
	 * @return a list of pair (node, int) obtained applying RELAX^- and LOWER^- rules to all LO-edges incoming to V, together with the edge (V,deltaVC,C).
	 */
	private ObjectList<BasicEntry<LabeledNode>> rul2020NewApplyRelaxLower(LabeledNode V, int DeltaC, int deltaVC) {

		ObjectList<BasicEntry<LabeledNode>> edges = new ObjectArrayList<>();
		if (deltaVC >= DeltaC) {
			if (Debug.ON) {
				LOG.finest("rul2020NewApplyRelaxLower. Resulting edges for " + V + ": " + edges);
			}
			return edges; // The RELAX^- and LOWER^- rules don't apply
		}
		STNUEdge lowerCaseEdge = this.lowerContingentEdge.get(V);
		if (lowerCaseEdge != null) {// == V is a contingent node
			// Apply LOWER^-
			if (Debug.ON) {
				LOG.finest("rul2020NewApplyRelaxLower. Apply LOWER considering: " + lowerCaseEdge);
			}
			edges.add(new BasicEntry<>(this.activationNode.get(V), Constants.sumWithOverflowCheck(lowerCaseEdge.getLabeledValue(), deltaVC)));
		} else {
			for (STNUEdge e : this.g.getInEdges(V)) {
				if (!e.isOrdinaryEdge())
					continue;
				LabeledNode W = this.g.getSource(e);
				// Apply RELAX^-
				if (Debug.ON) {
					LOG.finest("rul2020NewApplyRelaxLower. Apply RELAX considering: " + e);
				}
				edges.add(new BasicEntry<>(W, Constants.sumWithOverflowCheck(e.getValue(), deltaVC)));
			}
		}
		if (Debug.ON) {
			LOG.finest("rul2020NewApplyRelaxLower. Resulting edges for " + V + ": " + edges);
		}
		return edges;
	}

	/**
	 * Luke's implementation of RUL^- algorithm: Algorithm 11.<br>
	 * 
	 * @param A an activation time-point.
	 * @param globalPotential a potential function for G_{lo} excluding edges terminating at A.
	 * @return a potential function for G_{lo} (including edges terminating at A); or null if G_{lo} is inconsistent.
	 *         private Object2IntMap<LabeledNode> rul2020UpdatePotential(LabeledNode A, Object2IntMap<LabeledNode> globalPotential) {
	 *         Object2IntMap<LabeledNode> newGlobalPotential = new Object2IntOpenHashMap<>(globalPotential);
	 *         PriorityQueue newQ = new PriorityQueue();
	 *         newQ.insertOrDecrease(A, 0);
	 *         LabeledNode U;
	 *         while (newQ.size() != 0) {
	 *         BasicEntry<LabeledNode> entry = newQ.extractMinEntry();
	 *         LabeledNode V = entry.getKey();
	 *         for (STNUEdge e : this.g.getInEdges(V)) {
	 *         if (!e.isOrdinaryEdge())
	 *         continue;
	 *         U = this.g.getSource(e);
	 *         if (!rul2020UpdateValue(U, e.getValue(), V, newGlobalPotential, globalPotential, newQ)) {
	 *         return null;
	 *         }
	 *         }
	 *         if (V.isContingent()) {// this check excludes edge that are UC ones.
	 *         U = this.activationNode.get(V);
	 *         STNUEdge lowerCaseEdge = this.lowerContingentEdge.get(V);
	 *         if (lowerCaseEdge == null) {
	 *         return null;
	 *         }
	 *         if (!rul2020UpdateValue(U, lowerCaseEdge.getLabeledValue(), V, newGlobalPotential, globalPotential, newQ)) {
	 *         return null;
	 *         }
	 *         }
	 *         }
	 *         return newGlobalPotential;
	 *         }
	 */

	/**
	 * Luke's implementation of RUL^- algorithm: Algorithm 12.<br>
	 * Side effects: modifies Q and newPotential.
	 * 
	 * @param U
	 * @param deltaUV
	 * @param V
	 * @param newPotential
	 * @param oldPotential
	 * @param Q
	 * @return true iff updating the potential function newH to accommodate the edge (U,deltaUV,V) does not discover a negative loop.
	 *         private static boolean rul2020UpdateValue(LabeledNode U, int deltaUV, LabeledNode V, Object2IntMap<LabeledNode> newPotential,
	 *         Object2IntMap<LabeledNode> oldPotential, PriorityQueue Q) {
	 *         int newVal = newPotential.getInt(V) - deltaUV;
	 *         if (newPotential.getInt(U) < newVal) { // newPotential does not satisfy edge: newPotential[V] - newPotential[U] > delta_{UV}
	 *         newPotential.put(U, newVal);
	 *         int newKey = oldPotential.getInt(U) - newPotential.getInt(U);
	 *         if (Q.getStatus(U) == NodeStatus.wasPresent) {
	 *         return false;// Attempt to update already-popped time-point signals negative loop
	 *         }
	 *         Q.insertOrDecrease(U, newKey);
	 *         }
	 *         return true;
	 *         }
	 */

	/**
	 * Luke's implementation of RUL^- algorithm: Algorithm 13.<br>
	 * 
	 * @param C a contingent node
	 * @param DeltaC the difference y-x of contingent link associated to C
	 * @param distanceFrom distances from X to C computed during back-propagation.
	 * @param globalPotential global potential
	 * @return true iff forward propagation discovered a negative loop. Otherwise, the lower case from A to C is reduce away.
	 */
	private boolean rul2020FwdPropNotDC(LabeledNode C, int DeltaC, Object2IntMap<LabeledNode> distanceFrom,
			Object2IntMap<LabeledNode> globalPotential) {

		PriorityQueue<LabeledNode> queue = new PriorityQueue<>();
		queue.insertOrDecrease(C, -globalPotential.getInt(C));

		if (Debug.ON) {
			LOG.finest("rul2020fwdPropNotDC: Node C: " + C + ", DeltaC: " + DeltaC);
		}
		while (queue.size() != 0) {
			BasicEntry<LabeledNode> minEntry = queue.extractMinEntry();
			LabeledNode X = minEntry.getKey();
			int Xkey = minEntry.getIntValue();
			int deltaCX = Xkey + globalPotential.getInt(X);
			if (Debug.ON) {
				LOG.finest("rul2020fwdPropNotDC: Node X: " + X + ", Xkey: " + Xkey + ", deltaCX: " + deltaCX + ", distanceFrom: " + distanceFrom.getInt(X));
			}

			if (distanceFrom.getInt(X) >= DeltaC) {
				if (Debug.ON) {
					LOG.finest("rul2020fwdPropNotDC: Node X: " + X + ", distanceFrom: " + distanceFrom.getInt(X) + " is greater than " + DeltaC + ". Ignore!");
				}
				continue;
			}
			if (deltaCX < 0) {
				if (Debug.ON) {
					LOG.finest("rul2020fwdPropNotDC: Node X: " + X + ", deltaCX: " + deltaCX + " is less than " + DeltaC + ". Return true!");
				}
				return true;
			}
			for (STNUEdge e : this.g.getOutEdges(X)) {
				int eValue = getMinValueBetweenOrdinaryAndLowerCaseValue(e);// It is ok
				if (eValue == Constants.INT_NULL)// is an upper edge
					continue;
				LabeledNode Y = this.g.getDest(e);
				int newKey = Constants.sumWithOverflowCheck(deltaCX, eValue);
				newKey = Constants.sumWithOverflowCheck(newKey, -globalPotential.getInt(Y));// lower case o no-case value
				queue.insertOrDecrease(Y, newKey);
			}
		}
		return false;
	}

	/**
	 * Resets all internal structures.
	 */
	void reset() {
		this.g = null;
		this.Z = null;
		this.maxWeight = 0;
		this.horizon = 0;
		this.checkStatus.reset();
		this.activationNode = null;
		this.lowerContingentEdge = null;
	}

	/**
	 * <p>
	 * getFILE_NAME_SUFFIX.
	 * </p>
	 *
	 * @return the fILE_NAME_SUFFIX
	 */
	public static final String getFILE_NAME_SUFFIX() {
		return FILE_NAME_SUFFIX;
	}

	/**
	 * <p>
	 * getZeroNodeName.
	 * </p>
	 *
	 * @return the zeroNodeName
	 */
	public static final String getZeroNodeName() {
		return ZERO_NODE_NAME;
	}

	/**
	 * <p>
	 * getLOG.
	 * </p>
	 *
	 * @return the lOG
	 */
	public static final Logger getLOG() {
		return LOG;
	}

	/**
	 * <p>
	 * getVersionanddate.
	 * </p>
	 *
	 * @return the version
	 */
	public static final String getVersionanddate() {
		return VERSIONandDATE;
	}

	/**
	 * <p>
	 * Getter for the field <code>activationNode</code>.
	 * </p>
	 *
	 * @return the activationNode map if the network has been {@link #initAndCheck()}, null otherwise.
	 */
	public final Object2ObjectMap<LabeledNode, LabeledNode> getActivationNode() {
		if (this.activationNode == null)
			return null;
		return this.activationNode;
	}

	/**
	 * <p>
	 * isCleanCheckedInstance.
	 * </p>
	 *
	 * @return the cleanCheckedInstance
	 */
	public final boolean isCleanCheckedInstance() {
		return this.cleanCheckedInstance;
	}

	/**
	 * <p>
	 * Getter for the field <code>fInput</code>.
	 * </p>
	 *
	 * @return the fInput
	 */
	public final File getfInput() {
		return this.fInput;
	}

	/**
	 * <p>
	 * Getter for the field <code>gCheckedCleaned</code>.
	 * </p>
	 *
	 * @return the gCheckedCleaned
	 */
	public final TNGraph<STNUEdge> getgCheckedCleaned() {
		return this.gCheckedCleaned;
	}

	/**
	 * <p>
	 * Getter for the field <code>horizon</code>.
	 * </p>
	 *
	 * @return the horizon
	 */
	public final int getHorizon() {
		return this.horizon;
	}

	/**
	 * Given a contingent link is <code>(A, 1, 3, C)</code>, the returned map contains <code>C --&gt; (A, c(1), C)</code>.
	 *
	 * @return the lowerContingentEdge map
	 */
	public final Object2ObjectMap<LabeledNode, STNUEdge> getLowerContingentEdge() {
		return this.lowerContingentEdge;
	}

	/**
	 * Given a contingent link is <code>(A, 1, 3, C)</code>, the returned map contains <code>C --&gt; (C, C:-3, A)</code>.
	 *
	 * @return the lowerContingentEdge map
	 */
	public final Object2ObjectMap<LabeledNode, STNUEdge> getUpperContingentEdge() {
		return this.upperContingentEdge;
	}

	/**
	 * <p>
	 * Getter for the field <code>timeOut</code>.
	 * </p>
	 *
	 * @return the timeOut
	 */
	public final int getTimeOut() {
		return this.timeOut;
	}

	/**
	 * <p>
	 * isVersionReq.
	 * </p>
	 *
	 * @return the versionReq
	 */
	public final boolean isVersionReq() {
		return this.versionReq;
	}

	/**
	 * <p>
	 * getZ.
	 * </p>
	 *
	 * @return the z
	 */
	public final LabeledNode getZ() {
		return this.Z;
	}

	/**
	 * <p>
	 * isSave.
	 * </p>
	 *
	 * @return the save
	 */
	public final boolean isSave() {
		return this.save;
	}

	/**
	 * <p>
	 * Setter for the field <code>save</code>.
	 * </p>
	 *
	 * @param s the save to set
	 */
	public final void setSave(boolean s) {
		this.save = s;
	}

}
