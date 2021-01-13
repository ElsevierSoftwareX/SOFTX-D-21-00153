// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.STN.STNCheckStatus;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and qR3*
 * rules.<br>
 * This class is the base class for some other specialized in which DC semantics is defined in a different way.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 * @param <E> kind of edges
 */
public abstract class AbstractCSTN<E extends CSTNEdge> {

	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class CSTNCheckStatus extends STNCheckStatus {

		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings({ "javadoc" })
		public int r0calls = 0, r3calls = 0, labeledValuePropagationCalls = 0, potentialUpdate;

		/**
		 * Reset all indexes.
		 */
		@Override
		public void reset() {
			super.reset();
			this.r0calls = 0;
			this.r3calls = 0;
			this.labeledValuePropagationCalls = 0;
			this.potentialUpdate = 0;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("The check is");
			if (!this.finished)
				sb.append(" NOT");
			sb.append(" finished after ").append(this.cycles).append(" cycle(s).\n");
			if (this.finished) {
				sb.append("The consistency check has determined that given network is ");
				if (!this.consistency)
					sb.append("NOT ");
				sb.append("consistent.\n");
			}
			sb.append("Some statistics:\nR0 has been applied ").append(this.r0calls).append(" times.\n");
			sb.append("R3 has been applied ").append(this.r3calls).append(" times.\n");
			sb.append("Labeled Propagation has been applied ").append(this.labeledValuePropagationCalls).append(" times.\n");
			sb.append("Potentials updated ").append(this.potentialUpdate).append(" times.\n");
			if (this.timeout)
				sb.append("Checking has been interrupted because execution time exceeds the given time limit.\n");

			if (this.executionTimeNS != Constants.INT_NULL)
				sb.append("The global execution time has been ").append(this.executionTimeNS).append(" ns (~").append((this.executionTimeNS / 1E9))
						.append(" s.)");
			return sb.toString();
		}
	}

	/**
	 * Value for dcSemantics
	 */
	static public enum DCSemantics {
		/**
		 * Instantaneous reaction semantics
		 */
		IR,
		/**
		 * Standard semantics
		 */
		Std,
		/**
		 * ε-reaction semantics
		 */
		ε
	}

	/**
	 * @author posenato
	 * @param <E> Type of edge
	 */
	public static class EdgesToCheck<E extends Edge> implements Iterable<E> {
		/**
		 * 
		 */
		public boolean alreadyAddAllIncidentsToZ;
		/**
		 * It must be a set because an edge could be added more times!
		 */
		public ObjectRBTreeSet<E> edgesToCheck;

		/**
		 * 
		 */
		public EdgesToCheck() {
			this.edgesToCheck = new ObjectRBTreeSet<>();
			this.alreadyAddAllIncidentsToZ = false;
		}

		/**
		 * A simple constructor when the initial set of edges is available.
		 * 
		 * @param coll collection to copy
		 */
		public EdgesToCheck(Collection<? extends E> coll) {
			this.edgesToCheck = new ObjectRBTreeSet<>(coll);
			this.alreadyAddAllIncidentsToZ = false;
		}

		/**
		 * Clear the set.
		 */
		public void clear() {
			this.edgesToCheck.clear();
			this.alreadyAddAllIncidentsToZ = false;
		}

		@Override
		public Iterator<E> iterator() {
			return this.edgesToCheck.iterator();
		}

		/**
		 * @return the number of edges in the set.
		 */
		public int size() {
			return (this.edgesToCheck != null) ? this.edgesToCheck.size() : 0;
		}

		/**
		 * Add an edge without any check.
		 * 
		 * @param enSnD
		 * @return true if this set did not already contain the specified element
		 */
		final boolean add(E enSnD) {
			return this.edgesToCheck.add(enSnD);
		}

		/**
		 * Check if the edge that has to be add has one end-point that is an observer. In positive case, it adds
		 * all in edges to the destination node for guaranteeing that R3* can be applied again with new values.
		 * 
		 * @param enSnD
		 * @param nS
		 * @param nD
		 * @param Z
		 * @param g
		 * @param applyReducedSetOfRules
		 */
		final void add(E enSnD, LabeledNode nS, LabeledNode nD, LabeledNode Z, TNGraph<E> g, boolean applyReducedSetOfRules) {
			// in any case, the edge has to be added.
			this.edgesToCheck.add(enSnD);
			// then,
			if (!nS.isObserver())
				return;
			// add all incident to nD
			if (nD != Z) {
				if (!applyReducedSetOfRules)
					this.edgesToCheck.addAll(g.getInEdges(nD));
				return;
			}

			if (this.alreadyAddAllIncidentsToZ)
				return;
			this.edgesToCheck.addAll(g.getInEdges(Z));
			this.alreadyAddAllIncidentsToZ = true;
		}

		/**
		 * Add a set of edges without any check.
		 * 
		 * @param eSet
		 * @return true if this set changed after the add.
		 */
		final boolean addAll(Collection<E> eSet) {
			return this.edgesToCheck.addAll(eSet);
		}

		/**
		 * Copy fields reference of into this.
		 * After this method, this and input share the internal fields.
		 * 
		 * @param input
		 */
		void takeIn(EdgesToCheck<E> input) {
			if (input == null)
				return;
			this.edgesToCheck = input.edgesToCheck;
			this.alreadyAddAllIncidentsToZ = input.alreadyAddAllIncidentsToZ;
		}
	}

	/**
	 * Acts as a queue and a set.
	 * An element is enqueued only if it is not already present.
	 * 
	 * @author posenato
	 */
	public static class NodesToCheck implements ObjectSet<LabeledNode>, PriorityQueue<LabeledNode> {
		/**
		 * It must be a queue without replication set because a node may be added more times!
		 */
		public ObjectArrayFIFOSetQueue<LabeledNode> nodes2check;

		/**
		 * 
		 */
		public NodesToCheck() {
			this.nodes2check = new ObjectArrayFIFOSetQueue<>();
		}

		/**
		 * A simple constructor when the initial set of nodes is available.
		 * 
		 * @param coll collection to scan
		 */
		public NodesToCheck(Collection<LabeledNode> coll) {
			this();
			for (LabeledNode node : coll) {
				this.nodes2check.enqueue(node);
			}
		}

		@Override
		public boolean add(LabeledNode e) {
			return this.nodes2check.add(e);
		}

		@Override
		public boolean addAll(Collection<? extends LabeledNode> coll) {
			for (LabeledNode node : coll) {
				this.nodes2check.add(node);
			}
			return true;
		}

		/**
		 * Clear the set.
		 */
		@Override
		public void clear() {
			this.nodes2check.clear();
		}

		@Override
		public Comparator<? super LabeledNode> comparator() {
			throw new UnsupportedOperationException("comparator");
		}

		/**
		 * @param o node
		 * @return true if o is present.
		 */
		@Override
		public boolean contains(Object o) {
			return this.nodes2check.contains(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			throw new UnsupportedOperationException("containsAll");
		}

		/**
		 * @return the first LabeledNode in the queue
		 */
		@Override
		public LabeledNode dequeue() {
			return this.nodes2check.dequeue();
		}

		/**
		 * @param node Node to enqueue
		 */
		@Override
		public final void enqueue(LabeledNode node) {
			this.nodes2check.enqueue(node);
		}

		@Override
		public LabeledNode first() {
			throw new UnsupportedOperationException("first");
		}

		/**
		 * @return true if there is no element
		 */
		@Override
		public boolean isEmpty() {
			return this.nodes2check.isEmpty();
		}

		@Override
		public ObjectIterator<LabeledNode> iterator() {
			return this.nodes2check.iterator();
		}

		@Override
		public boolean remove(Object o) {
			return this.nodes2check.remove(o);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException("removeAll");
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException("retainAll");
		}

		/**
		 * @return the number of edges in the set.
		 */
		@Override
		public int size() {
			return this.nodes2check.size();
		}

		/**
		 * @return the queue as an array. If there is no element, the array is empty.
		 */
		@Override
		public LabeledNode[] toArray() {
			return (this.nodes2check != null) ? this.nodes2check.toArray(new LabeledNode[this.nodes2check.size()]) : new LabeledNode[0];
		}

		@Override
		public <T> T[] toArray(T[] a) {
			throw new UnsupportedOperationException("toArray");
		}

		@Override
		public String toString() {
			return this.nodes2check.toString();
		}

		/**
		 * Copy fields reference of into this.
		 * After this method, this and input share the internal fields.
		 * 
		 * @param input
		 */
		void takeIn(NodesToCheck input) {
			if (input == null)
				return;
			this.nodes2check = input.nodes2check;
		}
	}

	/**
	 * Suffix for file name
	 */
	public final static String FILE_NAME_SUFFIX = ".cstn";

	/**
	 * The name for the initial node.
	 */
	public final static String ZERO_NODE_NAME = "Z";

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(AbstractCSTN.class.getName());
	/**
	 * Version of the class
	 */
	static final String VERSIONandDATE = "Version 1.0 - June, 12 2019";// Refactoring CSTN class

	/**
	 * Determines the minimal distance between all pair of vertexes modifying the given consistent graph.
	 * If the graph contains a negative cycle, it returns false and the graph contains the edges that
	 * have determined the negative cycle.
	 *
	 * @param g the graph
	 * @return true if the graph is consistent, false otherwise.
	 *         If the response is false, the edges do not represent the minimal distance between nodes.
	 * @param <E> a E object.
	 */
	static public <E extends CSTNEdge> boolean getMinimalDistanceGraph(final TNGraph<E> g) {
		final int n = g.getVertexCount();
		final EdgeSupplier<E> edgeFactory = g.getEdgeFactory();
		final LabeledNode[] node = g.getVerticesArray();
		LabeledNode iV, jV, kV;
		E ik, kj, ij;
		int v;
		Label ijL;

		boolean consistent = true;
		for (int k = 0; k < n; k++) {
			kV = node[k];
			for (int i = 0; i < n; i++) {
				iV = node[i];
				for (int j = 0; j < n; j++) {
					if ((k == i) || (k == j)) {
						continue;
					}
					jV = node[j];
					Label nodeLabelConjunction = iV.getLabel().conjunction(jV.getLabel());
					if (nodeLabelConjunction == null)
						continue;

					ik = g.findEdge(iV, kV);
					kj = g.findEdge(kV, jV);
					if ((ik == null) || (kj == null)) {
						continue;
					}
					ij = g.findEdge(iV, jV);

					final ObjectSet<Object2IntMap.Entry<Label>> ikMap = ik.getLabeledValueSet();
					final ObjectSet<Object2IntMap.Entry<Label>> kjMap = kj.getLabeledValueSet();

					for (final Object2IntMap.Entry<Label> ikL : ikMap) {
						for (final Object2IntMap.Entry<Label> kjL : kjMap) {
							ijL = ikL.getKey().conjunction(kjL.getKey());
							if (ijL == null) {
								continue;
							}
							ijL = ijL.conjunction(nodeLabelConjunction);// It is necessary to propagate with node labels!
							if (ijL == null) {
								continue;
							}
							if (ij == null) {
								ij = edgeFactory.get("e" + node[i].getName() + node[j].getName());
								ij.setConstraintType(Edge.ConstraintType.derived);
								g.addEdge(ij, iV, jV);
							}
							v = ikL.getIntValue() + kjL.getIntValue();
							ij.mergeLabeledValue(ijL, v);
							if (i == j) // check negative cycles
								if (v < 0 || ij.getMinValue() < 0) {
									CSTNU.LOG.finer("Found a negative cycle on node " + iV.getName() + ": " + (ij)
											+ "\nIn details, ik=" + ik + ", kj=" + kj + ",  v=" + v + ", ij.getValue(" + ijL + ")=" + ij.getValue(ijL));
									consistent = false;
								}
						}
					}
				}
			}
		}
		return consistent;
	}

	/**
	 * Stops a computation if current instant is after the <code>timeoutInstant</code>
	 * setting <code>status.timeout=true</code>.<br>
	 * As courtesy, it sets also <code>status.consistency=status.finished=false</code>.
	 * 
	 * @param timeoutInstant
	 * @param status
	 * @return true if timeOut has been reached.
	 */
	static final boolean checkTimeOutAndAdjustStatus(Instant timeoutInstant, CSTNCheckStatus status) {
		if (Instant.now().isAfter(timeoutInstant)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Time out occurred!");
				}
			}
			status.timeout = true;
			status.consistency = false;
			status.finished = false;
			return true;
		}
		return false;
	}

	/**
	 * @param label
	 * @param value
	 * @return the conventional representation of a labeled value
	 */
	static final String pairAsString(Label label, int value) {
		return AbstractLabeledIntMap.entryAsString(label, value);
	}

	/**
	 * Check status
	 */
	CSTNCheckStatus checkStatus = new CSTNCheckStatus();

	/**
	 */
	@Option(required = false, name = "-cleaned", usage = "Output a cleaned result. A result cleaned graph does not contain empty edges or labeled values containing unknown literals.")
	boolean cleanCheckedInstance = true;

	/**
	 * The input file containing the CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "file_name must be the input CSTN graph in GraphML format.", metaVar = "file_name")
	File fInput;

	/**
	 * Output file where to write the XML representing the minimal CSTN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "output_file_name")
	File fOutput = null;

	/**
	 * Input TNGraph.
	 */
	TNGraph<E> g = null;

	/**
	 * TNGraph on which to operate.
	 */
	TNGraph<E> gCheckedCleaned = null;

	/**
	 * Horizon value. A node that has to be executed after such time means that it has not to be executed!
	 */
	int horizon = Constants.INT_NULL;

	/**
	 * Absolute value of the max negative weight determined during initialization phase.
	 */
	int maxWeight = Constants.INT_NULL;

	/**
	 * Check using full set of rules R0, qR0, R3, qR3, LP, qLP or the reduced set qR0, qR3, LP.
	 */
	boolean propagationOnlyToZ = false;

	/**
	 * Check using also R2
	 */
	boolean propagationFromZ = false;

	/**
	 * WD2.2 epsilon value called also reaction time in ICAPS 18.
	 * It is > 0 in standard CSTN, >= 0 in IR, > epsilon in Epsilon CSTN.
	 * Even when it is 0, the dynamic consistency def. excludes that a t.p. X having p in its label can be executed at the same time of t.p. P?.
	 * This is because, at time t, the history is the same and, therefore, X should be executed at t in very scenario, even in the one where it cannot stay!
	 * <b>Such value and WD2.2 property is not necessary as required in the past because Dynamic Execution definition already contains it.</b>
	 * On the other hand, propagation rules needs such value to be complete.
	 * Therefore, WD2.2 is not more required as CSTN property but it is imposed as propagation rule.
	 */
	@Option(required = false, name = "-r", aliases = "--reactionTime", usage = "Reaction time. It must be >= 0.")
	int reactionTime = 1;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	int timeOut = 2700;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	boolean versionReq = false;

	/**
	 * If false, node labels are ignored during the check.
	 */
	boolean withNodeLabels = true;

	/**
	 * Z node of the graph.
	 * Utility reference for many method. #initAndCheck sets this value.
	 */
	LabeledNode Z = null;

	/**
	 * <p>
	 * Constructor for AbstractCSTN.
	 * </p>
	 *
	 * @param graph TNGraph to check
	 */
	public AbstractCSTN(TNGraph<E> graph) {
		this();
		this.setG(graph);// sets also checkStatus!
	}

	/**
	 * <p>
	 * Constructor for AbstractCSTN.
	 * </p>
	 *
	 * @param graph TNGraph to check
	 * @param giveTimeOut timeout for the check
	 */
	public AbstractCSTN(TNGraph<E> graph, int giveTimeOut) {
		this(graph);
		this.timeOut = giveTimeOut;
	}

	/**
	 * Default constructor.
	 */
	AbstractCSTN() {
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance within timeout seconds.
	 * During the execution of this method, the given graph is modified. <br>
	 * If the check is successful, all constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * <br>
	 * After a check, {@link #getGChecked} returns the graph resulting after the check.
	 *
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	abstract public CSTNCheckStatus dynamicConsistencyCheck() throws WellDefinitionException;

	/**
	 * <p>
	 * Getter for the field <code>checkStatus</code>.
	 * </p>
	 *
	 * @return the checkStatus
	 */
	public CSTNCheckStatus getCheckStatus() {
		// CSTNU override this
		return this.checkStatus;
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
	final public TNGraph<E> getG() {
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
	public TNGraph<E> getGChecked() {
		if (this.cleanCheckedInstance && this.getCheckStatus().finished && this.getCheckStatus().consistency)
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
	 * Getter for the field <code>reactionTime</code>.
	 * </p>
	 *
	 * @return the reactionTime
	 */
	final public int getReactionTime() {
		return this.reactionTime;
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
		String s = "\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2019, Roberto Posenato.\n";
		try {
			s = this.getClass().getName() + " " + this.getClass().getDeclaredField("VERSIONandDATE").get(this)
					+ s;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			//
		}
		return s;
	}

	/**
	 * Initializes the CSTN instance represented by graph g.
	 *
	 * @return true if the graph is a well formed
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if the initial graph is not well defined.
	 * @see #coreCSTNInitAndCheck()
	 */
	public boolean initAndCheck() throws WellDefinitionException {
		boolean status = coreCSTNInitAndCheck();
		if (status)
			addUpperBounds();
		return status;
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
	 * <p>
	 * isWithNodeLabels.
	 * </p>
	 *
	 * @return the withNodeLabels
	 */
	public final boolean isWithNodeLabels() {
		return this.withNodeLabels;
	}

	/**
	 * Stores the graph after a check to the file {@link #getfOutput()}.
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
				outputName += "_checked_" + ((this.getCheckStatus().consistency) ? "DC" : "NOTDC");
			}
			outputName += FILE_NAME_SUFFIX;
			this.fOutput = new File(outputName);
			LOG.info("Output file name is " + this.fOutput.getAbsolutePath());
		}

		TNGraph<E> g1 = this.getGChecked();
		g1.setInputFile(this.fOutput);
		g1.setName(this.fOutput.getName());
		g1.removeEmptyEdges();

		StaticLayout<E> layout = new StaticLayout<>(g1);
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(layout);
		graphWriter.save(g1, this.fOutput);
		LOG.info("Checked instance saved in file " + this.fOutput.getAbsolutePath());
	}

	/**
	 * Setter for the field <code>fOutput</code>.
	 *
	 * @param fileOutput the file where to save the result.
	 */
	public void setfOutput(File fileOutput) {
		if (fileOutput == null)
			return;
		if (!fileOutput.getName().endsWith(FILE_NAME_SUFFIX)) {
			fileOutput.renameTo(new File(fileOutput.getAbsolutePath() + FILE_NAME_SUFFIX));
		}
		if (fileOutput.exists()) {
			fileOutput.delete();
		}
		this.fOutput = fileOutput;
	}

	/**
	 * Considers the given graph as the graph to check (graph will be modified).
	 * Clear all auxiliary variables.
	 *
	 * @param graph set internal TNGraph to g. It cannot be null.
	 */
	public void setG(TNGraph<E> graph) {
		// CSTNU overrides this.
		if (graph == null)
			throw new IllegalArgumentException("Input graph is null!");
		reset();
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
	 * <p>
	 * Setter for the field <code>withNodeLabels</code>.
	 * </p>
	 *
	 * @param withNodeLabels1 true if node labels have to be considered.
	 */
	public void setWithNodeLabels(boolean withNodeLabels1) {
		this.withNodeLabels = withNodeLabels1;
		this.setG(this.g);// reset all
	}

	/**
	 * If true, the propagations are made for edges ending to Z.
	 *
	 * @param propagationOnlyToZ1 true if propagations have to be done only to Z
	 */
	public void setPropagationOnlyToZ(boolean propagationOnlyToZ1) {
		this.propagationOnlyToZ = propagationOnlyToZ1;
		this.setG(this.g);// reset all
	}

	/**
	 * checkWellDefinitionProperties.
	 * It checks WD1, WD2 and WD3.
	 *
	 * @return true if the g is a CSTN well defined.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	boolean checkWellDefinitionProperties() throws WellDefinitionException {
		boolean flag = false;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Checking if graph is well defined...");
			}
		}
		for (final E e : this.g.getEdges()) {
			flag = checkWellDefinitionProperty1and3(this.g.getSource(e), this.g.getDest(e), e, false);
		}
		for (final LabeledNode node : this.g.getNodes()) {
			flag = flag && checkWellDefinitionProperty2(node, false);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, ((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well-defined graph!\n"));
			}
		}
		return flag;
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property (WD1):<br>
	 * <em>any labeled valued of the edge has a label that subsumes both labels of two endpoints.</em>
	 * In case a label is not WD1 and <code>hasToBeFixed</code>, then it is fixed. Otherwise a {@link WellDefinitionException} is raised.
	 * <br>
	 * Moreover, it checks the well definition 3 property (WD3):
	 * <em>a label subsumes all observer-t.p. labels of observer t.p.s whose propositions are present into the label.</em>
	 * 
	 * @param nS the source node of the edge. It must be not null!
	 * @param nD the destination node of the edge. It must be not null!
	 * @param eSN edge representing a labeled constraint. It must be not null!
	 * @param hasToBeFixed true for fixing well-definition errors that can be fixed!
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	boolean checkWellDefinitionProperty1and3(final LabeledNode nS, final LabeledNode nD, final E eSN, boolean hasToBeFixed)
			throws WellDefinitionException {

		final Label conjunctedLabel = nS.getLabel().conjunction(nD.getLabel());
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Source label: " + nS.getLabel() + "; dest label: " + nD.getLabel() + " conjuncted label: " + conjunctedLabel);
			}
		}
		if (conjunctedLabel == null) {
			final String msg = "Two endpoints do not allow any constraint because the have inconsisten labels."
					+ "\nHead node: " + nD
					+ "\nTail node: " + nS
					+ "\nConnecting edge: " + eSN;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, msg);
				}
			}
			throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
		}
		// check the ordinary labeled values
		for (Label currentLabel : eSN.getLabeledValueMap().keySet()) {
			int v = eSN.getValue(currentLabel);
			if (v == Constants.INT_NULL) {
				continue;
			}
			if (!currentLabel.isConsistentWith(conjunctedLabel)) {
				String msg = "Found a labeled value in " + eSN + " that is not consistent with the conjunction of node labels, "
						+ conjunctedLabel + ".";
				if (hasToBeFixed) {
					eSN.removeLabeledValue(currentLabel);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, msg + " Labeled value '" + currentLabel + "' removed.");
						}
					}
					continue;
				}
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
			}
			if (!currentLabel.subsumes(conjunctedLabel)) {
				final String msg = "Labeled value " + pairAsString(currentLabel, v) + " of edge " + eSN.getName()
						+ " does not subsume the endpoint labels '" + conjunctedLabel + "'.";
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, msg);
					}
				}
				if (hasToBeFixed) {
					eSN.removeLabeledValue(currentLabel);
					currentLabel = currentLabel.conjunction(conjunctedLabel);
					eSN.putLabeledValue(currentLabel, v);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, "Fixed as required!");
						}
					}
				} else {
					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
				}
			}
			// Checks if label subsumes all observer-t.p. labels of observer t.p. whose proposition is present into the label.
			// WD3 property.
			Label currentLabelModified = currentLabel;
			for (final char l : currentLabel.getPropositions()) {
				LabeledNode obs = this.g.getObserver(l);
				if (obs == null) {
					final String msg = "Observation node of literal " + l + " of label " + currentLabel + " in edge " + eSN + " does not exist.";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.SEVERE)) {
							LOG.log(Level.SEVERE, msg);
						}
					}
					throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
				}
				// Checks WD3 and adjusts
				final Label obsLabel = obs.getLabel();
				if (!currentLabel.subsumes(obsLabel)) {
					final String msg = "Label " + currentLabel + " of edge " + eSN + " does not subsume label " + obsLabel + " of obs node " + obs
							+ ". It has been fixed.";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, msg);
						}
					}
					currentLabelModified = currentLabelModified.conjunction(obsLabel);
					if (currentLabelModified == null) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.SEVERE)) {
								LOG.log(Level.SEVERE, "Label " + currentLabel + " of edge " + eSN + " does not subsume label of obs node " + obs
										+ " and cannot be expanded because it becomes inconsistent.");
							}
						}
						throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
					}
				}
			}
			if (!currentLabelModified.equals(currentLabel)) {
				eSN.removeLabeledValue(currentLabel);
				eSN.putLabeledValue(currentLabelModified, v);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Labeled value " + pairAsString(currentLabelModified, v) + " replace dishonest labeled value "
								+ pairAsString(currentLabelModified, v) + " in edge " + eSN + ".");
					}
				}
			}
		}
		return true;
	}

	/**
	 * Checks whether the label of a node satisfies the well definition 2 property (WD2):<br>
	 * <blockquote>For each literal present in a node label label:
	 * <ol>
	 * <li>the label of the observation node of the considered literal is subsumed by the label of the current node.
	 * <li>the observation node is constrained to occur before the current node.
	 * </ol>
	 * </blockquote>
	 * [2017-04-07] Posenato
	 * It has been proved that this property is not necessary for DC checking.
	 * I maintain it just to add an order among nodes and obs ones for speeding up the algorithm.
	 * 
	 * @param node the current node to check. It must be not null!
	 * @param hasToBeFixed true to add the required precedences.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	boolean checkWellDefinitionProperty2(final LabeledNode node, boolean hasToBeFixed) throws WellDefinitionException {
		final Label nodeLabel = node.getLabel();
		if (nodeLabel.isEmpty())
			return true;

		int v;
		String msg;
		LabeledNode obs;
		// Checks whether the node label is well defined w.r.t. each involved observation node label.
		for (final char l : nodeLabel.getPropositions()) {
			obs = this.g.getObserver(l);
			if (obs == null) {
				msg = "Observation node of literal " + l + " of node " + node + " does not exist.";
				if (Debug.ON) {
					if (LOG.isLoggable(Level.SEVERE)) {
						LOG.log(Level.SEVERE, msg);
					}
				}
				throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
			}

			// No more necessary as required property, but the algorithm guarantees it as property.
			final Label obsLabel = obs.getLabel();
			Label newNodeLabel;
			if (!nodeLabel.subsumes(obsLabel)) {
				newNodeLabel = nodeLabel.conjunction(obsLabel);
				if (newNodeLabel == null) {
					msg = "Label of node " + node + " is not consistent with label of obs node " + obs
							+ " but it should be subsume it! The network is not well defined.";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.SEVERE)) {
							LOG.log(Level.SEVERE, msg);
						}
					}
					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
				}

				if (hasToBeFixed)
					node.setLabel(newNodeLabel);
				msg = "Label of node " + node + " does not subsume label of obs node " + obs + ((hasToBeFixed) ? ". It has been adjusted!" : ".");
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, msg);
					}
				}
				// it is necessary to restart because the set of propositions has been changed!
				if (hasToBeFixed)
					checkWellDefinitionProperty2(node, hasToBeFixed);
			}
		}

		// LabelNode is ok with all involved observation node labels.
		// It is possible to check and assure that node is after such observation nodes.
		for (final char l : nodeLabel.getPropositions()) {
			obs = this.g.getObserver(l);
			E e = this.g.findEdge(node, obs);
			if ((e == null) || ((v = e.getValue(nodeLabel)) == Constants.INT_NULL) || (v > 0)) {// WD2.2 ICAPS paper
				// WD2.2 has been proved to be redundant. So, it can be removed. Here we maintain a light version of it.
				// Light version: a node with label having 'p' has to be just after P?, i.e., P?⟵[0,p]---X_p.
				msg = "WD2.2 simplified: There is no constraint to execute obs node " + obs + " before node " + node;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, msg);
					}
				}
				if (hasToBeFixed) {
					if (e == null) {
						e = makeNewEdge(node.getName() + "_" + obs.getName(), E.ConstraintType.internal);
						this.g.addEdge(e, node, obs);
					}
					e.mergeLabeledValue(nodeLabel, -this.reactionTime);// this is not necessary, but it can speed up the DC checking.
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, "Fixed adding " + pairAsString(nodeLabel, -this.reactionTime) + " to " + e);
						}
					}
					continue;
					// Since it is redundant, it cannot raise an exception!
					// throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotOccurBefore);
				}
			}
		}
		return true;
	}

	/**
	 * Makes the CSTN check and initialization. The CSTN instance is represented by graph g.
	 * If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level. If the method cannot fix such not-well-defined constraints, it raises a
	 * {@link WellDefinitionException}.
	 * <br>
	 * Since the current DC checking algorithm is complete only if the CSTN instance contains an upper bound to the distance between Z (the first node) and
	 * each node, this procedure add such upper bound (= #nodes * max weight value) to each node.<br>
	 * <b>Note</b>
	 * This method is necessary for allowing the building of special subclass initAndCheck (in subclasses of subclasses).
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	final boolean coreCSTNInitAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Starting initial well definition check.");
			}
		}
		this.g.clearCache();
		this.gCheckedCleaned = null;
		this.Z = this.g.getZ();

		// Checks the presence of Z node!
		// this.Z = this.g.getZ(); already done in setG()
		if (this.Z == null) {
			this.Z = this.g.getNode(AbstractCSTN.ZERO_NODE_NAME);
			if (this.Z == null) {
				// We add by authority!
				this.Z = this.g.getNodeFactory().get(AbstractCSTN.ZERO_NODE_NAME);
				this.Z.setX(10);
				this.Z.setY(10);
				this.g.addVertex(this.Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + AbstractCSTN.ZERO_NODE_NAME + " node found: added!");
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

		if (this.withNodeLabels) {
			// check if at least one node has label
			boolean foundLabel = false;
			for (LabeledNode node : this.g.getVertices()) {
				if (!node.getLabel().isEmpty()) {
					foundLabel = true;
					break;
				}
			}
			this.withNodeLabels = foundLabel;
		}
		// Checks well definiteness of edges and determine maxWeight
		int minNegWeight = 0, maxWeight1 = 0;
		for (final E e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Initial Checking edge e: " + e);
				}
			}
			// Determines the absolute max weight value
			for (Object2IntMap.Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v < minNegWeight) {
					minNegWeight = v;
				} else {
					if (v > maxWeight1) {
						maxWeight1 = v;
					}
				}
			}

			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);

			if (s == d) {
				// loop are not admissible
				this.g.removeEdge(e);
				continue;
			}
			// WD1 is checked and adjusted here
			if (this.withNodeLabels) {
				try {
					checkWellDefinitionProperty1and3(s, d, e, true);
				} catch (final WellDefinitionException ex) {
					throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
				}
			}
			if (e.isEmpty()) {
				// The merge removed labels...
				this.g.removeEdge(e);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Labels fixing on edge " + e + " removed all labels. Edge " + e + " has been removed.");
					}
				}
				continue;
			}

			// if (e.isContingentEdge()) {
			// if (Debug.ON) {
			// if (LOG.isLoggable(Level.WARNING)) {
			// LOG.log(Level.WARNING,
			// "Found a contingent edge: " + e + ". The consistency check does not difference between ordinary and contingent edges.");
			// }
			// }
			// }
		}

		// manage maxWeight value
		if (this.propagationOnlyToZ) {
			this.maxWeight = -minNegWeight;
		} else {
			this.maxWeight = (-minNegWeight > maxWeight1) ? -minNegWeight : maxWeight1;
		}
		// Determine horizon value
		long product = ((long) this.maxWeight) * (this.g.getVertexCount() - 1);// Z doesn't count!
		if (product >= Constants.INT_POS_INFINITE) {
			throw new ArithmeticException(
					"Horizon value is not representable by an integer. maxWeight = " + this.maxWeight + ", #vertices = " + this.g.getVertexCount());
		}
		this.horizon = (int) product;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "The horizon value is " + String.format("%6d", product));
		}

		// Init two useful structures
		this.g.getPropositions();

		/*
		 * Checks well definiteness of nodes
		 */
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {

			if (this.withNodeLabels) {
				// 1. Checks that observation node doesn't have the observed proposition in its label!
				final char obs = node.getPropositionObserved();
				if (obs != Constants.UNKNOWN) {
					Label label = node.getLabel();
					if (label.contains(obs)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING,
										"Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName()
												+ "'. Removed!");
							}
						}
						label = label.remove(obs);
						node.setLabel(label);
					}
				}

				// WD2 is checked and adjusted here
				try {
					checkWellDefinitionProperty2(node, true);
				} catch (final WellDefinitionException ex) {
					throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
				}
			}
			// 3. Checks that each node has an edge to Z and and edge from Z with bound = horizon.
			if (node != this.Z) {
				// LOWER BOUND FROM Z
				E edge = this.g.findEdge(node, this.Z);
				if (edge == null) {
					edge = makeNewEdge(node.getName() + "_" + this.Z.getName(), ConstraintType.internal);
					this.g.addEdge(edge, node, this.Z);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs after '"
											+ AbstractCSTN.ZERO_NODE_NAME
											+ "'.");
						}
					}
				}
				Label nodeLabel;

				if (this.withNodeLabels) {
					nodeLabel = node.getLabel();
				} else {
					nodeLabel = Label.emptyLabel;
					node.setLabel(nodeLabel);
				}

				boolean added = edge.mergeLabeledValue(nodeLabel, 0);// in any case, all nodes must be after Z!
				if (Debug.ON) {
					if (added) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER,
									"Added " + edge.getName() + ": " + node.getName() + "--" + pairAsString(nodeLabel, 0) + "-->" + this.Z.getName());
						}
					}
				}
			}
		}

		// it is usefull to apply R0 before starting, otherwise first cycles of algorithm can propagate dirty values before R0 can clean it
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Preliminary cleaning by R0");
			}
		}
		for (LabeledNode obs : this.g.getObservers()) {
			if (this.propagationOnlyToZ) {
				labelModificationR0qR0(obs, this.Z, this.g.findEdge(obs, this.Z));
			} else {
				for (E e : this.g.getOutEdges(obs)) {
					labelModificationR0qR0(obs, this.g.getDest(e), e);
				}
			}
		}
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
	 * Determines the set of edges P?-->nX where P? is an observer node and nX is the given node.
	 * 
	 * @param nX the given node.
	 * @return the set of edges P?-->nX, an empty set if nX is empty or there is no observer or there is no such edges.
	 */
	final ObjectList<E> getEdgeFromObserversToNode(final LabeledNode nX) {
		if (nX == this.Z) {
			return this.g.getObserver2ZEdges();
		}
		final ObjectList<E> fromObs = new ObjectArrayList<>();

		Collection<LabeledNode> obsSet = this.g.getObservers();
		if (obsSet.size() == 0)
			return fromObs;

		E e;
		for (final LabeledNode n : obsSet) {
			if ((e = this.g.findEdge(n, nX)) != null) {
				fromObs.add(e);
			}
		}
		return fromObs;
	}

	/**
	 * Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Standard DC semantics is assumed.</b>
	 * Derived classes can modify rule conditions of this method overriding {@link #mainConditionForSkippingInR0qR0(int)}.
	 * 
	 * <pre>
	 * R0:
	 * P? --[w, α p]--&gt; X 
	 * changes in 
	 * P? --[w, α']--&gt; X when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * It is assumed that P? != X.<br>
	 * Rule qR0 has X==Z.
	 * 
	 * @param nObs the observation node
	 * @param nX the other node
	 * @param eObsX the edge connecting nObs? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0qR0(final LabeledNode nObs, final LabeledNode nX, final E eObsX) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false, mergeStatus;

		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN)
			return false;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R0: start.");
			}
		}
		if (this.withNodeLabels) {
			if (nX.getLabel().contains(p)) {
				// It is a strange case because only with IR it is possible to manage such case.
				// In all other case is the premise of a negative loop.
				// We let this possibility
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								"R0qR0: Proposition " + p + " is present in the X label '" + nX.getLabel()
										+ "'. Rule cannot be applied.");
					}
				}
				return false;
			}
		}
		final ObjectSet<Label> obsXLabelSet = eObsX.getLabeledValueMap().keySet();

		for (final Label alpha : obsXLabelSet) {
			if (alpha == null || !alpha.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = eObsX.getValue(alpha);
			final Label alphaPrime = labelModificationR0qR0Core(nObs, nX, alpha, w);

			if (alphaPrime == alpha) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					logMessage = "R0 simplifies a label of edge " + eObsX.getName()
							+ ":\nsource: " + nObs.getName() + " ---" + pairAsString(alpha, w) + "⟶ " + nX.getName()
							+ "\nresult: " + nObs.getName() + " ---" + pairAsString(alphaPrime, w) + "⟶ " + nX.getName() + "\n";
				}
			}

			mergeStatus = eObsX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus) {
				ruleApplied = true;
				this.checkStatus.r0calls++;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, logMessage);
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R0: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Execute the core of {@link #labelModificationR0qR0(LabeledNode, LabeledNode, CSTNEdge)}.<br>
	 * It can be used for applying the rule to a specific pair (w, alpha).<br>
	 * Returns the label to use for storing the new value considering rule R0qR0.
	 * 
	 * @param nP the observation node. Per efficiency reason, there is no a security check!
	 * @param nX the other node
	 * @param alpha
	 * @param w
	 * @return the newLabel adjusted if the rule has been applied, original label otherwise.
	 */
	Label labelModificationR0qR0Core(final LabeledNode nP, final LabeledNode nX, final Label alpha, int w) {
		final char p = nP.getPropositionObserved();
		if (this.withNodeLabels) {
			if (nX.getLabel().contains(p)) {
				// It is a strange case because only with IR it is possible to manage such case.
				// In all other case is the premise of a negative loop.
				// We let this possibility
				return alpha;
			}
		}

		if (w == Constants.INT_NULL || mainConditionForSkippingInR0qR0(w)) {
			return alpha;
		}

		final Label alphaPrime = makeAlphaPrime(nX, nP, p, alpha);
		if (alphaPrime == null || alphaPrime.equals(alpha)) {
			return alpha;
		}
		this.checkStatus.r0calls++;
		return alphaPrime;
	}

	/**
	 * Returns true if {@link CSTN#labelModificationR0qR0} method has to not apply.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link CSTN#labelModificationR0qR0} method.
	 * 
	 * @param w
	 * @return true if the rule has to not apply.
	 */
	@SuppressWarnings("static-method")
	boolean mainConditionForSkippingInR0qR0(final int w) {
		// Table 1 ICAPS paper for standard DC
		// w must be <= 0 for applying the rule.
		return w > 0;
	}

	/**
	 * Returns true if {@link CSTN#labelModificationR3qR3} method has to not apply.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link CSTN#labelModificationR3qR3} method.
	 * 
	 * @param w
	 * @param nD
	 * @return true if the rule has to not apply
	 */
	boolean mainConditionForSkippingInR3qR3(final int w, final LabeledNode nD) {
		// Table 1 ICAPS paper for standard DC
		// When nD==Z, it is possible to skip the rule even when w==0 because the value on the other edge, v, can be negative or 0 at most (it cannot be v>0
		// because nD==Z). Then, the max == 0, and the resulting constraint is already represented by the fact that any nodes is after or at Z in any scenario.
		return w > 0 || (w == 0 && nD == this.Z);
	}

	/**
	 * Simple method to determine the label αβγ' for rule {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, CSTNEdge)}.<br>
	 * See Table 1 and Table 2 ICAPS 2016 paper.
	 * 
	 * @param nS
	 * @param nD
	 * @param nObs
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observer
	 * @param labelToClean
	 * @return alphaBetaGamma' if all conditions are satisfied. null otherwise.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeAlphaBetaGammaPrime4R3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nObs, final char observed,
			final Label labelFromObs, Label labelToClean) {
		StringBuilder slog;
		if (Debug.ON) {
			slog = new StringBuilder();
			if (LOG.isLoggable(Level.FINEST))
				slog.append("labelEdgeFromObs = " + labelFromObs);
		}
		if (this.withNodeLabels) {
			if (labelFromObs.contains(observed) || nS.getLabel().contains(observed) || nD.getLabel().contains(observed)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								slog.toString() + " αβγ' cannot be calculated because labelFromObs or lables of nodes contain the prop " + observed
										+ " that has to be removed.");
					}
				}
				return null;
			}
		}
		Label labelToCleanWOp = labelToClean.remove(observed);
		final Label alpha = labelFromObs.getSubLabelIn(labelToCleanWOp, false);
		if (alpha.containsUnknown()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + " α contains unknow: " + alpha);
				}
			}
			return null;
		}
		final Label beta = labelFromObs.getSubLabelIn(labelToCleanWOp, true);
		if (beta.containsUnknown()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + " β contains unknow " + beta);
				}
			}
			return null;
		}
		Label gamma = labelToCleanWOp.getSubLabelIn(labelFromObs, false);

		if (this.withNodeLabels) {
			gamma = gamma.remove(this.g.getChildrenOf(nObs));
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + " γ: " + gamma + "\n.");
			}
		}
		Label alphaBetaGamma = alpha.conjunction(beta).conjunction(gamma);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append(", αβγ'=" + alphaBetaGamma);
		}

		if (this.withNodeLabels) {
			if (alphaBetaGamma == null)
				return null;
			if (!alphaBetaGamma.subsumes(nD.getLabel().conjunction(nS.getLabel()))) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, slog.toString() + " αβγ' does not subsume labels from nodes:" + nD.getLabel().conjunction(nS.getLabel()));
					}
				}
				return null;
			}
		}
		return alphaBetaGamma;
	}

	/**
	 * Simple method to determine the α' to use in rules R0 and in rule qR0.
	 * Check paper TIME15 and ICAPS 2016 about CSTN sound&amp;complete DC check.
	 * α' is obtained by α removing all children of the observed proposition.
	 * If X==Z, then it is necessary also to remove all children of unknown from α'.
	 * 
	 * @param nX the destination node
	 * @param nObs observer node
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up.)
	 * @param labelFromObs label of the edge from observer
	 * @return α'
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeAlphaPrime(final LabeledNode nX, final LabeledNode nObs, final char observed, final Label labelFromObs) {
		if (this.withNodeLabels && !this.propagationOnlyToZ) {
			if (nX.getLabel().contains(observed))
				return null;
		}
		Label alphaPrime = labelFromObs.remove(observed);
		if (this.withNodeLabels) {
			alphaPrime = alphaPrime.remove(this.g.getChildrenOf(nObs));
			if (nX == this.Z && alphaPrime.containsUnknown()) {
				alphaPrime = removeChildrenOfUnknown(alphaPrime);
			}
			if (!alphaPrime.subsumes(nX.getLabel().conjunction(nObs.getLabel()))) {
				return null;
			}
		}
		return alphaPrime;
	}

	/**
	 * Simple method to determine the label (β*γ)† to use in rules qR3* {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, CSTNEdge)}.<br>
	 * See Table 1 and Table 2 ICAPS 2016 paper.
	 * 
	 * @param nS
	 * @param nObs
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observer
	 * @param labelToClean
	 * @return αβγ'
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeBetaGammaDagger4qR3(final LabeledNode nS, final LabeledNode nObs, final char observed, final Label labelFromObs, Label labelToClean) {
		if (this.withNodeLabels) {
			if (labelFromObs.contains(observed)) {
				return null;
			}
		}
		Label beta = labelToClean.remove(observed);
		if (this.withNodeLabels) {
			Label childrenOfP = this.g.getChildrenOf(nObs);
			if (childrenOfP != null && !childrenOfP.isEmpty()) {
				Label test = labelFromObs.remove(childrenOfP);
				if (!labelFromObs.equals(test)) {
					return null;// labelFromObs must not contain p or its children.
				}
				beta = beta.remove(childrenOfP);
			}
		}
		Label betaGamma = labelFromObs.conjunctionExtended(beta);
		if (this.withNodeLabels) {
			// remove all children of unknowns.
			betaGamma = removeChildrenOfUnknown(betaGamma);
			if (!betaGamma.subsumes(nS.getLabel())) {
				return null;
			}
		}
		return betaGamma;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 *
	 * @param name the proposed name. If an edge with name already exists, then name is modified adding an suitable integer such that the name becomes unique
	 *            in 'g'.
	 * @param type the type of edge to create.
	 * @return an edge with a unique name.
	 */
	E makeNewEdge(final String name, final Edge.ConstraintType type) {
		int i = this.g.getEdgeCount();
		String name1 = name;
		while (this.g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final E e = this.g.getEdgeFactory().get(name1);
		e.setConstraintType(type);
		return e;
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
			if (!this.fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (this.fOutput != null) {
				this.setfOutput(this.fOutput);
				// try {
				// this.fOutput.createNewFile();
				// new OutputStreamWriter(new FileOutputStream(this.fOutput));
				// } catch (final IOException e) {
				// throw new CmdLineException(parser, "Output file cannot be created.");
				// }
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
	 * Utility method to simply override {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, CSTNEdge)}
	 * in derived class without rewriting all method.
	 * Some derived classes have only to change the value for the new constraint.
	 * Overriding this method is sufficient for overriding {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, CSTNEdge)}.
	 * 
	 * @param w
	 * @param v
	 * @return true if the rule has to not apply
	 */
	@SuppressWarnings("static-method")
	int newValueInR3qR3(final int v, final int w) {
		// Table 1 ICAPS2016 paper for standard and IR DC
		return (v >= w) ? v : w;
	}

	/**
	 * Returns a new label removing all children of possibly present unknown literals in <code>l</code>.
	 * <code>l</code> is unchanged!
	 * 
	 * @param l
	 * @return the label modified.
	 */
	final Label removeChildrenOfUnknown(Label l) {
		for (final char unknownLit : l.getAllUnknown()) {
			l = l.remove(this.g.getChildrenOf(this.g.getObserver(unknownLit)));
		}
		return l;
	}

	/**
	 * Resets all internal structures
	 */
	void reset() {
		this.g = null;
		this.Z = null;
		this.maxWeight = 0;
		this.horizon = 0;
		this.checkStatus.reset();
	}

	/**
	 * The upper bounds from Z to each node have to be set after the horizon is determined.
	 * Since, the horizon depends on edge values and CSTNs, CSTNUs, CSTNPSUs have different type of edges, it is better that
	 * each class adds such edges after the determination of horizon.
	 * Therefore, such edges cannot be determine in {@link #coreCSTNInitAndCheck}
	 */
	void addUpperBounds() {
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {
			// Checks that each node has an edge from Z with bound = horizon.
			if (node != this.Z) {
				// UPPER BOUND FROM Z
				E edge = this.g.findEdge(this.Z, node);
				if (edge == null) {
					edge = makeNewEdge(this.Z.getName() + "_" + node.getName(), ConstraintType.internal);
					this.g.addEdge(edge, this.Z, node);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs before the horizon (if it occurs).");
						}
					}
				}
				boolean added = edge.mergeLabeledValue(node.getLabel(), this.horizon);
				if (Debug.ON) {
					if (added) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER,
									"Added " + edge.getName() + ": " + this.Z.getName() + "--" + pairAsString(node.getLabel(), this.horizon) + "-->"
											+ node.getName() + ". Results: " + edge);
						}
					}
				}
			}
		}

	}
}
