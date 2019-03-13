package it.univr.di.cstnu.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.Literal;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and qR3*
 * rules.<br>
 * This class is the base class for some other specialized in which DC semantics is defined in a different way.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN {

	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class CSTNCheckStatus {
		/**
		 * True if the network is consistent so far.
		 */
		public boolean consistency = true;

		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings("javadoc")
		public int cycles = 0, r0calls = 0, r3calls = 0, labeledValuePropagationCalls = 0;

		/**
		 * Execution time in nanoseconds.
		 */
		public long executionTimeNS = Constants.INT_NULL;

		/**
		 * True if no rule can be applied anymore.
		 */
		public boolean finished = false;

		/**
		 * The statistics of 6 rules about potential are grouped into an array for efficiency reasons.
		 */
		public int[] potentialCalls = new int[6];

		/**
		 * Standard Deviation of Execution time if this last one is a mean. In nanoseconds.
		 */
		public long stdDevExecutionTimeNS = Constants.INT_NULL;

		/**
		 * True if check has been interrupted because a give time-out has occurred.
		 */
		public boolean timeout = false;

		/**
		 * True if all data structured have been initialized.
		 */
		boolean initialized = false;

		/**
		 * Reset all indexes.
		 */
		public void reset() {
			this.consistency = true;
			this.cycles = 0;
			this.r0calls = 0;
			this.r3calls = 0;
			this.labeledValuePropagationCalls = 0;
			for (int i = this.potentialCalls.length; i-- != 0;) {
				this.potentialCalls[i] = 0;
			}
			this.executionTimeNS = this.stdDevExecutionTimeNS = Constants.INT_NULL;
			this.finished = this.timeout = false;
			this.initialized = false;
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
			for (int i = 0; i < this.potentialCalls.length; i++) {
				if (i == 1)
					continue;
				sb.append("Potential rule ");
				if (i == 0)
					sb.append("1-2").append(" has been applied ").append(this.potentialCalls[i] + this.potentialCalls[i + 1]);
				else
					sb.append(i).append(" has been applied ").append(this.potentialCalls[i]);
				sb.append(" times.\n");
			}
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
	 */
	public static class EdgesToCheck implements Iterable<LabeledIntEdge> {
		/**
		 * 
		 */
		public boolean alreadyAddAllIncidentsToZ;
		/**
		 * It must be a set because an edge could be added more times!
		 */
		public ObjectRBTreeSet<LabeledIntEdge> edgesToCheck;

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
		 * @param coll
		 */
		public EdgesToCheck(Collection<LabeledIntEdge> coll) {
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
		public Iterator<LabeledIntEdge> iterator() {
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
		final boolean add(LabeledIntEdge enSnD) {
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
		final void add(LabeledIntEdge enSnD, LabeledNode nS, LabeledNode nD, LabeledNode Z, LabeledIntGraph g, boolean applyReducedSetOfRules) {
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
		final boolean addAll(Collection<LabeledIntEdge> eSet) {
			return this.edgesToCheck.addAll(eSet);
		}

		/**
		 * Copy fields reference of into this.
		 * After this method, this and input share the internal fields.
		 * 
		 * @param input
		 */
		void takeIn(EdgesToCheck input) {
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
		 * @param coll
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
		public boolean addAll(Collection<? extends LabeledNode> c) {
			throw new UnsupportedOperationException("addAll");
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
		 * @param o
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
		 * @param node
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
			return (this.nodes2check != null) ? this.nodes2check.toArray() : new LabeledNode[0];
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
	public static String FILE_NAME_SUFFIX = ".cstn";

	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.3 - October, 4 2016";
	// static final String VERSIONandDATE = "Version 4.0 - October, 25 2016";// added management not all negative edges in a negative qLoop
	// static final public String VERSIONandDATE = "Version 5.0 - April, 03 2017";// re-factored
	// static final public String VERSIONandDATE = "Version 5.2 - October, 16 2017";// better log management. This version uses LP,R0,R3*,qLP,qR0,qR3* and
	// horizon!
	// static final public String VERSIONandDATE = "Version 5.3 - November, 9 2017";// Replaced Omega node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 5.4 - November, 17 2017";// Adjusted LP
	// static final public String VERSIONandDATE = "Version 5.5 - November, 23 2017";// Adjusted skipping condition in LP
	// static final public String VERSIONandDATE = "Version 5.6 - November, 23 2017";// Horizon tweaking
	// static final public String VERSIONandDATE = "Version 5.7 - December, 13 2017";// Code tweaking
	// static final public String VERSIONandDATE = "Version 5.8 - January, 17 2019";// Code tweaking
	static final public String VERSIONandDATE = "Version 6 - January, 31 2019";// Infty value management moved to nodes!

	/**
	 * The name for the initial node.
	 */
	public static String ZeroNodeName = "Z";
	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTN.class.getName());

	/**
	 * Determines the minimal distance between all pair of vertexes modifying the given consistent graph.
	 * If the graph contains a negative cycle, it returns false and the graph contains the edges that
	 * have determined the negative cycle.
	 *
	 * @param g the graph
	 * @return true if the graph is consistent, false otherwise.
	 *         If the response is false, the edges do not represent the minimal distance between nodes.
	 */
	static public boolean getMinimalDistanceGraph(final LabeledIntGraph g) {
		final int n = g.getVertexCount();
		final LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = g.getEdgeFactory();
		final LabeledNode[] node = g.getVerticesArray();
		LabeledNode iV, jV, kV;
		LabeledIntEdge ik, kj, ij;
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
								ij.setConstraintType(LabeledIntEdge.ConstraintType.derived);
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
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTN(), "Standard DC");
	}

	/**
	 * Says if a new labeled value like (-14, α¿p) is an edge going to obs t.p. P?. in this case, the new value must not be store.<br>
	 * Example Q ---(-14, α¿p)⟶ P? is useless, so it must not be stored.
	 * 
	 * @param obs the node having the incoming edge.
	 * @param newLabel
	 * @param value
	 * @param log
	 * @return true if the value does not be stored, false otherwise
	 */
	static boolean checkAndApplyUnknownAfterObs(LabeledNode obs, Label newLabel, int value, String log) {
		if (!obs.isObserver())
			return false;
		char p = obs.getPropositionObserved();
		if (newLabel.contains(Literal.valueOf(p, Literal.UNKNONW))) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					log += "\n dot not store value " + pairAsString(newLabel, value) + " because " + obs.getName() + " is the observation t.p. of proposition '"
							+ p + "'.";
					LOG.log(Level.FINER, log);
				}
			}
			return true;
		}
		return false;
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
	 * @param args
	 * @param cstn
	 * @param kindOfChecking
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	static void defaultMain(final String[] args, final CSTN cstn, String kindOfChecking) throws IOException, ParserConfigurationException, SAXException {
		cstn.printVersion();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");
		if (cstn.versionReq) {
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loading graph...");
			}
		}
		CSTNUGraphMLReader graphMLReader = new CSTNUGraphMLReader(cstn.fInput, LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("LabeledIntGraph loaded!\n" + kindOfChecking + " Checking...");
			}
		}
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given CSTN is Dynamic consistent!");
			} else {
				System.out.println("The given CSTN is not Dynamic consistent!");
			}
			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}
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
	 * Graph on which to operate.
	 */
	LabeledIntGraph g = null;

	/**
	 * Graph on which to operate.
	 */
	LabeledIntGraph gCheckedCleaned = null;

	/**
	 * Horizon value. A node that has to be executed after such time means that it has not to be executed!
	 */
	int horizon = Constants.INT_NULL;

	/**
	 * Absolute value of the max negative weight determined during initialization phase.
	 */
	int maxWeight = Constants.INT_NULL;

	/**
	 * Output stream to fOutput
	 */
	PrintStream output = null;

	/**
	 * Check using full set of rules R0, qR0, R3, qR3, LP, qLP or the reduced set qR0, qR3, LP.
	 */
	@Option(required = false, name = "-limitToZ", usage = "Check DC propagating only values on edges to Z.")
	boolean propagationOnlyToZ = false;

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
	 * DCchecking can be done also assuming that all node labels are empty.
	 * This assumption usually make the checking slower in medium size network, but faster in small ones.
	 * So, in standard class it is assumed to consider also node labels.
	 * Derived classes can put this values false for checking the network with the alternative approach.
	 */
	boolean withNodeLabels = true;

	/**
	 * DCChecking requires to use unknown literals to be complete.
	 * This flag can disable unknown literals if one want to verify if they are necessary for a specific check.
	 */
	boolean withUnknown = true;

	/**
	 * Z node of the graph.
	 * Utility reference for many method. #initAndCheck sets this value.
	 */
	LabeledNode Z = null;

	/**
	 * @param g graph to check
	 */
	public CSTN(LabeledIntGraph g) {
		this();
		this.setG(g);// sets also checkStatus!
	}

	/**
	 * @param g graph to check
	 * @param timeOut timeout for the check
	 */
	public CSTN(LabeledIntGraph g, int timeOut) {
		this(g);
		this.timeOut = timeOut;
	}

	/**
	 * Default constructor.
	 */
	CSTN() {
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance within timeout seconds.
	 * During the execution of this method, the given graph is modified. <br>
	 * If the check is successful, all constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * <br>
	 * After a check, {@link #getGChecked} returns the graph resulting after the check.
	 * 
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	public CSTNCheckStatus dynamicConsistencyCheck() throws WellDefinitionException {
		try {
			initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		return dynamicConsistencyCheckWOInit();
	}

	/**
	 * @return the checkStatus
	 */
	public CSTNCheckStatus getCheckStatus() {
		// CSTNU override this
		return this.checkStatus;
	}

	/**
	 * @return the fOutput
	 */
	public File getfOutput() {
		return this.fOutput;
	}

	/**
	 * @return the g
	 */
	final public LabeledIntGraph getG() {
		return this.g;
	}

	/**
	 * @return the resulting graph of a check. It is up to the called to be sure the the returned graph is the result of a check.
	 * @see #setOutputCleaned(boolean)
	 */
	final public LabeledIntGraph getGChecked() {
		if (this.cleanCheckedInstance && this.getCheckStatus().finished && this.getCheckStatus().consistency)
			return this.gCheckedCleaned;
		return this.g;
	}

	/**
	 * @return the maxWeight
	 */
	final public int getMaxWeight() {
		return this.maxWeight;
	}

	/**
	 * @return the reactionTime
	 */
	final public int getReactionTime() {
		return this.reactionTime;
	}

	/**
	 * Initializes the CSTN instance represented by graph g. The {@link #dynamicConsistencyCheck()} calls this method before
	 * to execute the check. If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level. If the method cannot fix such not-well-defined constraints, it raises a
	 * {@link WellDefinitionException}.
	 * <br>
	 * Since the current DC checking algorithm is complete only if the CSTN instance contains an upper bound to the distance between Z (the first node) and
	 * each node, this procedure add such upper bound (= #nodes * max weight value) to each node.
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
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

		// Checks the presence of Z node!
		// this.Z = this.g.getZ(); already done in setG()
		if (this.Z == null) {
			this.Z = this.g.getNode(CSTN.ZeroNodeName);
			if (this.Z == null) {
				// We add by authority!
				this.Z = new LabeledNode(CSTN.ZeroNodeName);
				this.Z.setX(10);
				this.Z.setY(10);
				this.g.addVertex(this.Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + CSTN.ZeroNodeName + " node found: added!");
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
		for (final LabeledIntEdge e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Initial Checking edge e: " + e);
				}
			}
			// Determines the absolute max weight value
			for (Object2IntMap.Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v < minNegWeight)
					minNegWeight = v;
			}

			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);

			if (s == d) {
				// loop are not admissible
				this.g.removeEdge(e);
				continue;
			}
			// WD1 is checked and adjusted here
			try {
				checkWellDefinitionProperty1and3(s, d, e, true);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
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

			if (e.isContingentEdge()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING,
								"Found a contingent edge: " + e + ". The consistency check does not difference between ordinary and contingent edges.");
					}
				}
			}
		}

		// manage maxWeight value
		this.maxWeight = -minNegWeight;
		// Determine horizon value
		long product = ((long) this.maxWeight) * (this.g.getVertexCount() - 1);// Z doesn't count!
		if (product >= Constants.INT_POS_INFINITE) {
			throw new ArithmeticException("Horizon value is not representable by an integer.");
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
		boolean thereIsASignificantNodeLabel = false;
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {

			// 1. Checks that observation node doesn't have the observed proposition in its label!
			final char obs = node.getPropositionObserved();
			Label label = node.getLabel();
			if (obs != Constants.UNKNOWN && label.contains(obs)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName()
								+ "'. Removed!");
					}
				}
				label = label.remove(obs);
				node.setLabel(label);
			}

			// WD2 is checked and adjusted here
			try {
				checkWellDefinitionProperty2(node, true);
			} catch (final WellDefinitionException ex) {
				throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
			}

			// 3. Checks that each node has an edge to Z and and edge from Z with bound = horizon.
			if (node != this.Z) {
				// LOWER BOUND FROM Z
				LabeledIntEdge edge = this.g.findEdge(node, this.Z);
				if (edge == null) {
					edge = makeNewEdge(node.getName() + "_" + this.Z.getName(), ConstraintType.internal);
					this.g.addEdge(edge, node, this.Z);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs after '" + CSTN.ZeroNodeName
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

				edge.mergeLabeledValue(nodeLabel, 0);// in any case, all nodes must be after Z!

				// UPPER BOUND FROM Z
				edge = this.g.findEdge(this.Z, node);
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
				edge.mergeLabeledValue(nodeLabel, this.horizon);
			}

			if (this.withNodeLabels) {
				// maybe all node labels are empty... so this.withNodeLabels can be reset.
				thereIsASignificantNodeLabel |= !node.getLabel().isEmpty();
			}
		}

		// if withNodeLabel has been set false in a derived class, such assignment has to be preserved.
		if (this.withNodeLabels) {
			// it can be reset...in that case, algorithm is faster.
			this.withNodeLabels &= thereIsASignificantNodeLabel;
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
	 * @return the fOutputCleaned
	 */
	public boolean isOutputCleaned() {
		return this.cleanCheckedInstance;
	}

	/**
	 * @return the withNodeLabels
	 */
	public final boolean isWithNodeLabels() {
		return this.withNodeLabels;
	}

	/**
	 * @return the withUnknown
	 */
	public final boolean isWithUnknown() {
		return this.withUnknown;
	}

	/**
	 * Executes one step of the dynamic consistency check.<br>
	 * For each possible triangle of the network, label propagation rule is applied and, on the resulting
	 * edge, all other rules, R0, R3 and potential ones, are also applied.<br>
	 * <em>This method is offered for studying the propagation node by node. It is not efficient!<br>
	 * {@link #dynamicConsistencyCheck()} uses a different propagation technique!</em>
	 * 
	 * @return the update status (for convenience. The status is also stored in {@link #checkStatus}).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {
		LabeledNode B, C;
		LabeledIntEdge AC;// AB, BC
		boolean createEdge = false;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "\nStart application labeled propagation rule+R0+R3.");
			}
		}
		/**
		 * March, 03 2016 I try to apply the rules on all edges making a by-row-visit to the adjacency matrix.
		 */
		NodesToCheck newNodesToCheck = new NodesToCheck();
		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		for (LabeledNode A : this.g.getVertices()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering node " + A + ".\nFirst: potential propagation.");
				}
			}
			potentialR3_4_5_6(A, false, newNodesToCheck, newEdgesToCheck);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return this.checkStatus;
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Done.\nSecond: label propagation considering edges outgoing from " + A);
				}
			}
			for (LabeledIntEdge AB : this.g.getOutEdges(A)) {
				B = this.g.getDest(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				if (B == this.Z || !this.propagationOnlyToZ) {
					// Since in some graphs it is possible that there is not BC, we apply R0 and R3 to AB
					if (A.isObserver()) {
						// R0 on the resulting new values
						labelModificationR0qR0(A, B, AB);
					}
					this.labelModificationR3qR3(A, B, AB);
					if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						labelModificationR0qR0(A, B, AB);
					}
				}
				for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
					C = this.g.getDest(BC);
					// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to possibly create -∞

					if (C == this.Z || !this.propagationOnlyToZ) {
						if (B.isObserver()) {
							// R0 on the resulting new values
							labelModificationR0qR0(B, C, BC);
						}
						this.labelModificationR3qR3(B, C, BC);
						if (B.isObserver()) {// R3 can add new values that have to be minimized.
							// R0 on the resulting new values
							labelModificationR0qR0(B, C, BC);
						}
					}
					// Now it is possible to propagate the labels with the standard rules
					AC = this.g.findEdge(A, C);
					// I need to preserve the old edge to compare below
					createEdge = (AC == null);
					if (createEdge) {
						AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					}

					labelPropagation(A, B, C, AB, BC, AC);

					@SuppressWarnings("null")
					boolean empty = AC.isEmpty();
					if (createEdge && !empty) {
						// the new CB has to be added to the graph!
						this.g.addEdge(AC, A, C);
					} else {
						if (empty)
							continue;
					}
					if (!this.checkStatus.consistency) {
						this.checkStatus.finished = true;
						return this.checkStatus;
					}

					if (C == this.Z || !this.propagationOnlyToZ) {
						if (A.isObserver()) {
							// R0 on the resulting new values
							labelModificationR0qR0(A, C, AC);
						}
						// R3 on the resulting new values
						this.labelModificationR3qR3(A, C, AC);

						if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
							// R0 on the resulting new values
							labelModificationR0qR0(A, C, AC);
						}
					}
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Finished label propagation considering edges outgoing from " + A);
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3."
						+ "\nSituation after the labeled propagation rule+R0+R3.\n");
			}
		}
		return this.checkStatus;
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
	 * Stores the graph after a check to the file {@link #fOutput}.
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

		LabeledIntGraph g1 = this.getGChecked();
		g1.setInputFile(this.fOutput);
		g1.setName(this.fOutput.getName());
		g1.removeEmptyEdges();

		StaticLayout<LabeledIntEdge> layout = new StaticLayout<>(g1);
		final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(layout);
		try (Writer out = new BufferedWriter(new FileWriter(this.fOutput))) {
			graphWriter.save(g1, out);
		} catch (final Exception e) {
			System.err.println("Something is wrong and it is not possible to save the result. The program does not stop. Error: " + e.getMessage());
		}
		LOG.info("Checked instance saved in file " + this.fOutput.getAbsolutePath());
	}

	/**
	 * @param fOutput the file where to save the result.
	 */
	public void setfOutput(File fOutput) {
		this.fOutput = fOutput;
	}

	/**
	 * Considers the given graph as the graph to check (graph will be modified).
	 * Clear all {@link #maxWeight}, {@link #horizon} and {@link #checkStatus}.
	 * 
	 * @param g set internal graph to g. It cannot be null.
	 */
	public void setG(LabeledIntGraph g) {
		// CSTNU overrides this.
		if (g == null)
			throw new IllegalArgumentException("Input graph is null!");
		reset();
		this.g = g;
		this.Z = g.getZ();// Don't remove this assignment!
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
	 * @param withNodeLabels the withNodeLabels to set
	 */
	public void setWithNodeLabels(boolean withNodeLabels) {
		this.withNodeLabels = withNodeLabels;
		this.setG(this.g);// reset all
	}

	/**
	 * @param withUnknown the withUnknown to set
	 */
	public void setWithUnknown(boolean withUnknown) {
		this.withUnknown = withUnknown;
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
		for (final LabeledIntEdge e : this.g.getEdges()) {
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
	boolean checkWellDefinitionProperty1and3(final LabeledNode nS, final LabeledNode nD, final LabeledIntEdge eSN, boolean hasToBeFixed)
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
		for (final Object2IntMap.Entry<Label> entry : eSN.getLabeledValueMap().entrySet()) {
			Label currentLabel = entry.getKey();
			if (!currentLabel.isConsistentWith(conjunctedLabel)) {
				String msg = "Found a labeled value in " + eSN + " that does not subsume the conjunction of node labels, "
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
				final String msg = "Labeled value " + pairAsString(currentLabel, entry.getIntValue()) + " of edge " + eSN.getName()
						+ " does not subsume the endpoint labels '" + conjunctedLabel + "'.";
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, msg);
					}
				}
				if (hasToBeFixed) {
					int v = entry.getIntValue();
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
					final String msg = "Label " + currentLabel + " of edge " + eSN + " does not subsume label of obs node " + obs + ". It has been fixed.";
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
				int v = entry.getIntValue();
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
			LabeledIntEdge e = this.g.findEdge(node, obs);
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
						e = makeNewEdge(node.getName() + "_" + obs.getName(), LabeledIntEdge.ConstraintType.derived);
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
	 * Checks the dynamic consistency of a CSTN instance without initialize the network.<br>
	 * This method can be used ONLY when it is guaranteed that the network is already initialize by method {@link #initAndCheck()}.
	 * 
	 * @return the final status of the checking with some statistics.
	 */
	CSTNCheckStatus dynamicConsistencyCheckWOInit() {
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("Graph has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}
		EdgesToCheck edgesToCheck = new EdgesToCheck(this.g.getEdges());
		NodesToCheck nodesToCheck = new NodesToCheck();
		final int propositionN = this.g.getObserverCount();
		final int nodeN = this.g.getVertexCount();
		int m = (this.getMaxWeight() != 0) ? this.getMaxWeight() : 1;
		// From CSTNU TIME 2018: m |T|^2 3^|P|
		int maxCycles = m * nodeN * nodeN * (int) Math.pow(propositionN, 3);
		if (maxCycles < 0)
			maxCycles = Integer.MAX_VALUE;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "The maximum number of possible cycles is " + maxCycles);
			}
		}

		int i;
		Instant startInstant = Instant.now();
		Instant timeoutInstant = startInstant.plusSeconds(this.timeOut);
		for (i = 1; (i <= maxCycles) && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}

			if (this.propagationOnlyToZ) {
				oneStepDynamicConsistencyByEdgesLimitedToZ(edgesToCheck, nodesToCheck, timeoutInstant);
			} else {
				oneStepDynamicConsistencyByEdges(edgesToCheck, nodesToCheck, timeoutInstant);// Don't use 'this.' because such method is overrided!
			}

			if (!this.checkStatus.finished) {
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					if (Debug.ON) {
						String msg = "During the check # " + i + ", " + this.timeOut + " seconds timeout occured. ";
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, msg);
						}
					}
					this.saveGraphToFile();
					this.checkStatus.executionTimeNS = ChronoUnit.NANOS.between(startInstant, Instant.now());
					return this.checkStatus;
				}
				if (this.checkStatus.consistency) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINE)) {
							StringBuilder log = new StringBuilder("During the check # " + i + ", " + edgesToCheck.size()
									+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
							for (LabeledIntEdge e : edgesToCheck) {
								if (e.size() == 0)
									continue;
								log.append("Edge " + e + "\n");
							}
							LOG.log(Level.FINE, log.toString());
						}
					}
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
				}
			}
		}
		Instant endInstant = Instant.now();
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an inconsistency.\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Final inconsistent graph: " + this.g);
					}
				}
			}
			this.saveGraphToFile();
			return this.checkStatus;
		}

		if ((i > maxCycles) && !this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Last determined graph: " + this.g);
					}
				}
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			this.saveGraphToFile();
			return this.checkStatus;
		}

		// consistent && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO,
						"Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: " + this.checkStatus);
			}
		}
		this.gCheckedCleaned = new LabeledIntGraph(this.g.getName(), this.g.getInternalLabeledValueMapImplementationClass());
		this.gCheckedCleaned.copyCleaningRedundantLabels(this.g);
		this.saveGraphToFile();
		return this.checkStatus;
	}

	/**
	 * Determines the set of edges P?-->nX where P? is an observer node and nX is the given node.
	 * 
	 * @param nX the given node.
	 * @return the set of edges P?-->nX, an empty set if nX is empty or there is no observer or there is no such edges.
	 */
	final ObjectList<LabeledIntEdge> getEdgeFromObserversToNode(final LabeledNode nX) {
		if (nX == this.Z) {
			return this.g.getObserver2ZEdges();
		}
		final ObjectList<LabeledIntEdge> fromObs = new ObjectArrayList<>();

		Collection<LabeledNode> obsSet = this.g.getObservers();
		if (obsSet.size() == 0)
			return fromObs;

		LabeledIntEdge e;
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
	boolean labelModificationR0qR0(final LabeledNode nObs, final LabeledNode nX, final LabeledIntEdge eObsX) {
		// Visibility is package because there is Junit Class test that checks this method.

		if (this.propagationOnlyToZ) {
			if (nX != this.Z)
				return false;
		}

		boolean ruleApplied = false, mergeStatus;

		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, "Method labelModificationR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
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
			if (w == Constants.INT_NULL || mainConditionForSkippingInR0qR0(w)) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(nX, nObs, p, alpha);
			if (alphaPrime == null) {
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
	 * Returns the label to use for storing the new value considering rule R0qR0.
	 * <b>Standard DC semantics is assumed.</b>
	 * Derived classes can modify rule conditions of this method overriding {@link #mainConditionForSkippingInR0qR0(int)}.
	 * 
	 * <pre>
	 * R0:
	 * nP? --[w, α p]--&gt; nX 
	 * changes in 
	 * nP? --[w, α']--&gt; nX when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * It is assumed that P? != X.<br>
	 * Rule qR0 has X==Z.
	 * 
	 * @param nP the observation node. Per efficiency reason, there is no a security check!
	 * @param nX the other node
	 * @param alpha
	 * @param w
	 * @return the newLabel adjusted if the rule has been applied, original label otherwise.
	 */
	Label labelModificationR0qR0Light(final LabeledNode nP, final LabeledNode nX, final Label alpha, int w) {
		if (this.propagationOnlyToZ) {
			if (nX != this.Z)
				return alpha;
		}
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
	 * <h1>Rule R3*</h1>
	 * <b>Standard DC semantics is assumed.</b><br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics.</b>
	 * 
	 * <pre>
	 * if P? --[w, αβ]--&gt; nD &lt;--[v, βγp]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[max{w,v}, αβγ']-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * </pre>
	 *
	 * <h2>Rule qR3*</h2>
	 * 
	 * <pre>
	 * if P? --[w, γ]--&gt; Z &lt;--[v, βθp']-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[max{w,v}, (γ★β)†]-- nS
	 * where:
	 * β, θ and γ are in Q*.
	 * p' is p or ¬p or ¿p
	 * γ does not contain p' and any of its children.
	 * β does not contain any children of p'.
	 * θ contains only children of p'.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * (γ★β)† is the extended conjunction without any children of unknown literals.
	 * </pre>
	 * 
	 * Moreover, it calls {@link #potentialR3(LabeledNode, LabeledNode, LabeledIntEdge, ObjectSet)} to determine if
	 * potentials present in node nD have to be propagated to nS.
	 * 
	 * @param nS node must be different from nD
	 * @param nD node must be different from nS
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final LabeledIntEdge eSD) {

		if (this.propagationOnlyToZ) {
			if (nD != this.Z)
				return false;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: start.");
			}
		}
		boolean ruleApplied = false;

		boolean nSisObs = nS.isObserver();

		ObjectList<LabeledIntEdge> Obs2nDEdges = this.getEdgeFromObserversToNode(nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();

		Label allLiteralsSD = Label.emptyLabel;
		for (Label l : SDLabelSet) {
			if (this.withUnknown) {
				allLiteralsSD = allLiteralsSD.conjunctionExtended(l);
			} else {
				allLiteralsSD = allLiteralsSD.conjunction(l);
				if (allLiteralsSD == null) {
					allLiteralsSD = Label.emptyLabel;
					break;
				}
			}
		}

		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);
			if (nObs == nS)
				continue;

			final char p = nObs.getPropositionObserved();

			if (!allLiteralsSD.contains(p)) {
				// no label in nS-->nD contain any literal of p.
				continue;
			}
			if (this.withNodeLabels) {
				if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// WD1 must be preserved!
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "R3: Proposition " + p + " is present in the nS label '" + nS.getLabel() + " or nD label " + nD.getLabel()
									+ ". WD1 must be preserved, so R3 cannot be applied.");
						}
					}
					continue;
				}
			}

			String firstLog = "R3 considers edge " + eSD.getName() + " and observation t.p. " + nObs.getName();

			ObjectSet<Label> nDPotentialLabel = nD.getPotential().keySet();
			// all labels from current Obs
			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.getLabeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (mainConditionForSkippingInR3qR3(w, nD)) {
					continue;
				}

				final Label ObsDLabel = entryObsD.getKey();

				for (final Label SDLabel : SDLabelSet) {
					if (SDLabel == null || !SDLabel.contains(p)) {
						continue;
					}

					final int v = eSD.getValue(SDLabel);
					if (v == Constants.INT_NULL) {
						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
						continue;
					}

					Label newLabel = (nD != this.Z) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					final int max = newValueInR3qR3(v, w);

					String log = "";
					if (Debug.ON) {
						log = firstLog
								+ "\nsource: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "⟶ " + nD.getName()
								+ " ⟵" + pairAsString(SDLabel, v) + "---" + nS.getName();
					}

					// If nS is an obs, apply R0 to the new label.
					if (nSisObs) {
						newLabel = labelModificationR0qR0Light(nS, nD, newLabel, max);
					}
					// Before storing the new value, checks that it can be stored!
					if (potentialR6(nS, nD, newLabel, max, eSD, log))
						continue;
					if (checkAndApplyUnknownAfterObs(nD, newLabel, max, log))
						continue;

					ruleApplied = eSD.mergeLabeledValue(newLabel, max);

					if (ruleApplied) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, log
										+ "\nresult: add " + nD.getName() + " ⟵" + pairAsString(newLabel, max) + "--- " + nS.getName() + "\n");
								firstLog = "";
							}
						}
						this.checkStatus.r3calls++;
						potentialR3(nS, nD, eSD, nDPotentialLabel);
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Applies the labeled propagation rule:<br>
	 * <b>Standard DC semantics is assumed.</b><br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics or epsilon-reaction time.</b><br>
	 * The rule implements 2018-11 qLP+, submitted to ICAPS19.
	 * 
	 * <pre>
	 * if A ---(u,α)⟶ B ---(v,β)⟶ C and (u+v < 0 and u < 0) or (u+v < 0 and αβ in P*)
	 * then A ---[(α★β)†, u+v]⟶ C 
	 * 
	 * α,β in Q*
	 * (α★β)† is the label without children of unknown.
	 *
	 * If A==C and u+v < 0, then
	 * - if (α★β)† does not contain ¿ literals, the network is not DC
	 * - if (α★β)† contains ¿ literals, the u+v becomes -∞ and IT IS STORED as labeled value in node A!
	 * </pre>
	 * 
	 * Before storing a new value, it checks the value considering potential rules and R0 one to guarantee to store a necessary value with the shortest label.
	 * 
	 * @param nA first node.
	 * @param nB second node.
	 * @param nC third node.
	 * @param eAB edge nA⟶nB
	 * @param eBC edge nB⟶nC
	 * @param eAC edge nA⟶nC
	 * @return true if a reduction has been applied.
	 */
	boolean labelPropagation(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB,
			final LabeledIntEdge eBC, LabeledIntEdge eAC) {
		// * Be careful, in order to propagate correctly possibly -∞ self-loop, it is necessary call this method also for triple like with nodes A == B or B==C!
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false;
		Label nAnCLabel = null;
		if (this.withNodeLabels) {
			nAnCLabel = nA.getLabel().conjunction(nC.getLabel());
			if (nAnCLabel == null)
				return false;
		}

		boolean nAisAnObserver = nA.isObserver();
		char proposition = nA.getPropositionObserved();

		ObjectSet<Object2IntMap.Entry<Label>> setToReuse = new ObjectArraySet<>();
		String firstLog = "Labeled Propagation Rule considers edges " + eAB.getName() + ", " + eBC.getName() + " for " + eAC.getName();
		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			final int u = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet(setToReuse)) {
				final int v = BCEntry.getIntValue();
				int sum = Constants.sumWithOverflowCheck(u, v);
				if (sum > 0) {
					// // It is not necessary to propagate positive values.
					// // Less propagations, less useless labeled values.
					// 2018-01-25: I verified that for some negative instances, avoiding the positive propagations can increase the execution time.
					// For positive instances, surely avoiding the positive propagations shorten the execution time.
					continue;
				}
				final Label labelBC = BCEntry.getKey();
				boolean qLabel = false;
				Label newLabelAC = null;
				if (this.propagationOnlyToZ || mainConditionForSkippingInLP(u, v)) {
					newLabelAC = labelAB.conjunction(labelBC);
					if (newLabelAC == null) {
						continue;
					}
				} else {
					newLabelAC = (this.withUnknown) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
					if (newLabelAC == null)
						continue;
					qLabel = newLabelAC.containsUnknown();
					if (qLabel && this.withNodeLabels) {
						newLabelAC = removeChildrenOfUnknown(newLabelAC);
						qLabel = newLabelAC.containsUnknown();
					}
				}
				if (this.withNodeLabels) {
					if (!newLabelAC.subsumes(nAnCLabel)) {
						if (Debug.ON)
							LOG.log(Level.FINEST,
									"New alphaBeta label " + newLabelAC + " does not subsume node labels " + nAnCLabel + ". New value cannot be added.");
						continue;
					}
				}

				int oldValue = eAC.getValue(newLabelAC);

				// Prepare the log in advance in order to avoid repetition
				String log = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = firstLog
								+ "\nsource: " + nA.getName() + " ---" + pairAsString(labelAB, u) + "⟶ "
								+ nB.getName() + " ---" + pairAsString(labelBC, v) + "⟶ " + nC.getName();
					}
				}
				if (nA == nC) {
					if (sum < 0 && potentialR1_2(nA, newLabelAC, log)) {
						// The labeled value is negative and label is in Q*.
						// The -∞ value is now stored on node A (==C) as potential value if label is in Q*/P*, otherwise, a negative loop has been found!
						ruleApplied = true;
						this.checkStatus.labeledValuePropagationCalls++;
						if (!this.checkStatus.consistency)
							return true;
					}
					continue;
				} // end if nA==nC

				// in the case of A != C
				if (oldValue != Constants.INT_NULL && sum >= oldValue) {
					// a value is stored only if it is more negative than the current one.
					continue;
				}

				// Checks the new labeled value w.r.t. some rules before storing it.
				if (potentialR6(nA, nC, newLabelAC, oldValue, eAC, log))
					continue;
				if (checkAndApplyUnknownAfterObs(nC, newLabelAC, sum, log))
					continue;

				// here sum has to be add!
				if (eAC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					this.checkStatus.labeledValuePropagationCalls++;
					// // R0qR0 rule
					if (nAisAnObserver && newLabelAC.contains(proposition)) {
						Label newLabelAC1 = labelModificationR0qR0Light(nA, nC, newLabelAC, sum);
						if (!newLabelAC1.equals(newLabelAC)) {
							newLabelAC = newLabelAC1;
							eAC.mergeLabeledValue(newLabelAC, sum);
						}
					}
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							log += "\nresult: "
									+ nA.getName() + " ---" + pairAsString(newLabelAC, sum) + "⟶ " + nC.getName()
									+ "; old value: " + Constants.formatInt(oldValue) + "\n";
							LOG.log(Level.FINER, log);
							firstLog = "";
						}
					}
				}
			}
		}
		return ruleApplied;

	}

	/**
	 * Returns true if {@link #labelPropagation} method has to not apply.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link #labelPropagation} method.
	 * 
	 * @param u
	 * @param v
	 * @return true if the rule has to not apply.
	 */
	@SuppressWarnings("static-method")
	boolean mainConditionForSkippingInLP(final int u, final int v) {
		// Table 1 2016 ICAPS paper for standard DC extended with rules on page 6 of file noteAboutLP.tex
		// Moreover, Luke and I verified on 2018-11-22 that with u≤0, qLP+ can be applied.
		return u > 0;
	}

	/**
	 * Returns true if {@link #labelModificationR0qR0} method has to not apply.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link #labelModificationR0qR0} method.
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
	 * Returns true if {@link #labelModificationR3qR3} method has to not apply.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link #labelModificationR3qR3} method.
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
	 * Simple method to determine the label αβγ' for rule {@link #labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}.<br>
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
	 * Simple method to determine the label (β*γ)† to use in rules qR3* {@link #labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}.<br>
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
		Label betaGamma = (this.withUnknown) ? labelFromObs.conjunctionExtended(beta) : labelFromObs.conjunction(beta);
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
	LabeledIntEdgePluggable makeNewEdge(final String name, final LabeledIntEdge.ConstraintType type) {
		int i = this.g.getEdgeCount();
		String name1 = name;
		while (this.g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final LabeledIntEdgePluggable e = this.g.getEdgeFactory().get(name1);
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
				if (this.fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!this.fOutput.getName().endsWith(".cstn")) {
					this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".cstn"));
				}
				if (this.fOutput.exists()) {
					this.fOutput.delete();
				}
				try {
					this.fOutput.createNewFile();
					this.output = new PrintStream(this.fOutput);
				} catch (final IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				this.output = System.out;
			}
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
	 * Utility method to simply override {@link #labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * in derived class without rewriting all method.
	 * Some derived classes have only to change the value for the new constraint.
	 * Overriding this method is sufficient for overriding {@link #labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}.
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
	 * Executes one step of the dynamic consistency check.
	 * For each edge in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule is applied two times:
	 * one time having the edge as first edge, one time having the edge as second edge.<br>
	 * All modified or new edges are returned in the set 'edgesToCheck'.<br>
	 * It is assumed that {@link #propagationOnlyToZ} is false!
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param nodesToCheck set of nodes having at least one (-∞, l) that must be propagated.
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck edgesToCheck, final NodesToCheck nodesToCheck, Instant timeoutInstant) {

		if (this.propagationOnlyToZ)
			throw new IllegalStateException("oneStepDynamicConsistencyByEdges can be called only when propagationOnlyToZ is false.");

		LabeledNode A, B, C;
		LabeledIntTreeMap sourceNodeOriginalPotential;
		LabeledIntEdge AC, CB, edgeCopy;
		boolean nodeToAdd = false;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "*** Starting of application labeled propagation rule+R0+R3+node potential propagation.");
			}
		}
		/**
		 * March, 06 2016 I try to apply the rules on all edges that have been modified in the previous cycle.
		 */
		EdgesToCheck newEdgesToCheck = new EdgesToCheck(edgesToCheck.edgesToCheck);
		EdgesToCheck newEdgesToCheckR0R3 = new EdgesToCheck();
		NodesToCheck newNodesToCheck = new NodesToCheck();
		int i = 1, j = 1, n;
		// Find a stable state using R0 e R3.
		while (edgesToCheck.size() != 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "*** R3R0 CYCLE " + (j++));
				}
			}
			n = edgesToCheck.size();
			for (LabeledIntEdge AB : edgesToCheck) {
				nodeToAdd = true;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "*** R3R0: considering edge " + (i++) + "/" + n + ": " + AB.getName());
					}
				}
				A = this.g.getSource(AB);
				B = this.g.getDest(AB);
				sourceNodeOriginalPotential = new LabeledIntTreeMap(A.getPotential());
				// initAndCheck does not resolve completely a qStar.
				// It is necessary to check here the edge before to consider the second edge.
				// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
				// It is assumed that {@link #propagationOnlyToZ} is false!
				edgeCopy = this.g.getEdgeFactory().get(AB);
				if (A.isObserver()) {
					// // R0 on the resulting new values
					labelModificationR0qR0(A, B, AB);
				}
				labelModificationR3qR3(A, B, AB);
				// if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// // R0 on the resulting new values
				// labelModificationR0qR0(A, B, AB);
				// }
				if (!AB.equalsAllLabeledValues(edgeCopy)) {
					newEdgesToCheckR0R3.add(AB, A, B, this.Z, this.g, this.propagationOnlyToZ);
					newEdgesToCheck.add(AB, A, B, this.Z, this.g, this.propagationOnlyToZ);
				}
				if (nodeToAdd && !A.getPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to A
					newNodesToCheck.enqueue(A);
					nodeToAdd = false;
				}
				//
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
			edgesToCheck.clear();
			edgesToCheck.addAll(newEdgesToCheckR0R3.edgesToCheck);
			newEdgesToCheckR0R3.clear();
			i = 1;
		}

		edgesToCheck.addAll(newEdgesToCheck.edgesToCheck);
		newEdgesToCheck.clear();
		n = edgesToCheck.size();
		// now it is time to propagate a stable configuration
		for (LabeledIntEdge AB : edgesToCheck) {
			nodeToAdd = true;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "*** LP: considering edge " + (i++) + "/" + n + ": " + AB.getName());
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			sourceNodeOriginalPotential = new LabeledIntTreeMap(A.getPotential());
			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);
				// It is necessary to consider also self loop to store first negative loop.
				// In this class, (-∞,q), where q\in Q* are not more propagate on edges, but on nodes!

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().get(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(A, B, C, AB, BC, AC);

				/**
				 * 2016-03-08: It has been experimented that it is not necessary to clean AC values using R0 and R3
				 */
				boolean edgeModified = false;
				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					edgeModified = true;
				} else {
					// CB was already present and it has been changed!
					edgeModified = (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AC));
				}

				if (edgeModified) {
					newEdgesToCheck.add(AC, A, C, this.Z, this.g, this.propagationOnlyToZ);
					potentialR3(A, C, AC, null);
				}

				if (nodeToAdd && !A.getPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to A
					newNodesToCheck.enqueue(A);
					nodeToAdd = false;
				}
				if (!this.checkStatus.consistency) {
					// it must be here because a potential can be modified!
					this.checkStatus.finished = true;
					return this.checkStatus;
				}
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			sourceNodeOriginalPotential = new LabeledIntTreeMap(B.getPotential());
			nodeToAdd = true;
			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);
				if (C == B)// it has been checked in the Step 1/2
					continue;

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().get(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(C, A, B, CA, AB, CB);

				boolean edgeModified = false;
				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
				} else {
					// CB was already present and it has been changed!
					edgeModified = edgeCopy != null && !edgeCopy.equalsAllLabeledValues(CB);
				}

				if (edgeModified) {
					newEdgesToCheck.add(CB, C, B, this.Z, this.g, this.propagationOnlyToZ);
					potentialR3(C, B, CB, null);
				}

				if (nodeToAdd && !B.getPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to B
					newNodesToCheck.enqueue(B);
					nodeToAdd = false;
				}
				if (!this.checkStatus.consistency) {
					// it must be here because a potential can be modified!
					this.checkStatus.finished = true;
					return this.checkStatus;
				}
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.\n"
						+ "Starts execution of propagation -∞ from nodes.");
			}
		}

		// Manage -∞ on nodes!
		while (newNodesToCheck.size() != 0) {
			nodesToCheck.enqueue(newNodesToCheck.dequeue());
		}
		while (nodesToCheck.size() != 0) {
			LabeledNode node = nodesToCheck.dequeue();
			potentialR3_4_5_6(node, false, newNodesToCheck, newEdgesToCheck);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return this.checkStatus;
			}
			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return this.checkStatus;
			}
		}
		if (!this.checkStatus.consistency) {
			this.checkStatus.finished = true;
			return this.checkStatus;
		}
		// check the halt conditions
		edgesToCheck.clear();
		nodesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0 && newNodesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
			nodesToCheck.takeIn(newNodesToCheck);
		}
		return this.checkStatus;
	}

	/**
	 * Executes one step of the dynamic consistency check.
	 * For each edge B-->Z in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule
	 * is applied to A-->B-->Z for all A-->B.
	 * All modified or new edges are returned in the set 'edgesToCheck'.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param nodesToCheck
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	CSTNCheckStatus oneStepDynamicConsistencyByEdgesLimitedToZ(final EdgesToCheck edgesToCheck, final NodesToCheck nodesToCheck, Instant timeoutInstant) {
		// This version consider only pair of edges going to Z, i.e., in the form A-->B-->Z,
		// 2018-01-25: with this method, performances worsen.
		LabeledNode B, A;
		LabeledIntEdge AZ, edgeCopy;
		LabeledIntTreeMap sourceNodeOriginalPotential;
		boolean nodeToAdd = false;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "\nStart application labeled propagation rule+R0+R3.");
			}
		}

		EdgesToCheck newEdgesToCheck = new EdgesToCheck(edgesToCheck.edgesToCheck);
		EdgesToCheck newEdgesToCheckR0R3 = new EdgesToCheck();
		NodesToCheck newNodesToCheck = new NodesToCheck();
		int i = 1, j = 1, n;
		// Find a stable state using R0 and R3.
		while (edgesToCheck.size() != 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n\n*** R3R0 CYCLE " + (j++));
				}
			}
			n = edgesToCheck.size();
			for (LabeledIntEdge BZ : edgesToCheck) {
				if (this.g.getDest(BZ) != this.Z)
					continue;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "\n\n*** R3R0: considering edge " + (i++) + "/" + n + ": " + BZ.getName());
					}
				}
				B = this.g.getSource(BZ);
				sourceNodeOriginalPotential = new LabeledIntTreeMap(B.getPotential());
				// initAndCheck does not resolve completely a qStar.
				// It is necessary to check here the edge before to consider the second edge.
				// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
				edgeCopy = this.g.getEdgeFactory().get(BZ);
				if (B.isObserver()) {
					// R0 on the resulting new values
					labelModificationR0qR0(B, this.Z, BZ);
				}

				labelModificationR3qR3(B, this.Z, BZ);

				if (!BZ.equalsAllLabeledValues(edgeCopy)) {
					newEdgesToCheckR0R3.add(BZ, B, this.Z, this.Z, this.g, this.propagationOnlyToZ);
					newEdgesToCheck.add(BZ, B, this.Z, this.Z, this.g, this.propagationOnlyToZ);
				}

				if (nodeToAdd && !B.getPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to B
					newNodesToCheck.enqueue(B);
					nodeToAdd = false;
				}

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
			edgesToCheck.clear();
			edgesToCheck.addAll(newEdgesToCheckR0R3.edgesToCheck);
			newEdgesToCheckR0R3.clear();
			i = 1;
		}

		edgesToCheck.addAll(newEdgesToCheck.edgesToCheck);
		newEdgesToCheck.clear();
		n = edgesToCheck.size();

		// now it is time to propagate a stable configuration
		for (LabeledIntEdge BZ : edgesToCheck) {
			nodeToAdd = true;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n\n*** LP: considering edge " + (i++) + "/" + n + ": " + BZ.getName());
				}
			}
			B = this.g.getSource(BZ);
			sourceNodeOriginalPotential = new LabeledIntTreeMap(this.Z.getPotential());
			/**
			 * Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->Z
			 */
			for (LabeledIntEdge AB : this.g.getInEdges(B)) {
				A = this.g.getSource(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				AZ = this.g.findEdge(A, this.Z);
				// I need to preserve the old edge to compare below
				if (AZ != null) {
					edgeCopy = this.g.getEdgeFactory().get(AZ);
				} else {
					AZ = makeNewEdge(A.getName() + "_" + this.Z.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(A, B, this.Z, AB, BZ, AZ);

				boolean edgeModified = false;
				if (edgeCopy == null && !AZ.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AZ, A, this.Z);
				} else {
					// CB was already present and it has been changed!
					edgeModified = edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AZ);
				}

				if (edgeModified) {
					newEdgesToCheck.add(AZ, A, this.Z, this.Z, this.g, this.propagationOnlyToZ);
					potentialR3(A, this.Z, AZ, null);
				}

				if (nodeToAdd && !this.Z.getPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to B
					newNodesToCheck.enqueue(this.Z);
					nodeToAdd = false;
				}
				if (!this.checkStatus.consistency) {
					this.checkStatus.finished = true;
					return this.checkStatus;
				}

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.\n" +
						"Starts execution of propagation -∞ from nodes.");
			}
		}

		// Manage -∞ on nodes!
		while (newNodesToCheck.size() != 0) {
			nodesToCheck.enqueue(newNodesToCheck.dequeue());
		}
		while (nodesToCheck.size() != 0) {
			LabeledNode node = nodesToCheck.dequeue();
			potentialR3_4_5_6(node, true, newNodesToCheck, newEdgesToCheck);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return this.checkStatus;
			}
			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return this.checkStatus;
			}
		}
		if (!this.checkStatus.consistency) {
			this.checkStatus.finished = true;
			return this.checkStatus;
		}

		edgesToCheck.clear();
		nodesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0 && newNodesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
			nodesToCheck.takeIn(newNodesToCheck);
		}
		return this.checkStatus;
	}

	/**
	 * Checks the labeled -∞ before putting in the potential of a node.
	 * If the label does not contain unknowns, then it stores the value, logs that a negative loop has been found,
	 * sets the consistency of the <code>this.status=false</code> and returns.
	 * Otherwise, it stores the value and returns.
	 * In any case, if the <code>node</code> is an observation time-point, the method removes from <code>label</code>
	 * possible literal associated to the proposition observed by <code>node</code> before storing the <code>(-∞, label)</code>.
	 * 
	 * @param node the node where to add the <code>(-∞, label)</code>.
	 * @param label
	 * @param log possible text to log as prefix to the log of this method.
	 * @return true if the value has been added, false otherwise.
	 */
	boolean potentialR1_2(LabeledNode node, Label label, String log) {
		if (node.isObserver())
			// Potential Rule 2 and RO
			label = label.remove(node.getPropositionObserved());
		if (Debug.ON) {
			log += "\nputAndCheckNegativeInfty on " + node.toString()
					+ "added: " + pairAsString(label, Constants.INT_NEG_INFINITE);
		}
		if (!label.containsUnknown()) {
			node.putPotential(label, Constants.INT_NEG_INFINITE);
			// Negative loop!
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, log + "\n***\nFound a negative loop in node " + node + "\n***");
				}
			}
			this.checkStatus.consistency = false;
			this.checkStatus.finished = true;
			this.checkStatus.potentialCalls[0]++;
			return true;
		}
		if (node.putPotential(label, Constants.INT_NEG_INFINITE)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, log);
				}
			}
			this.checkStatus.potentialCalls[0]++;
			return true;
		}
		return false;
	}

	/**
	 * Checks and applies Labeled Potential Rule 3 to node <code>nS</code>.
	 * 
	 * <pre>
	 * Potential Rule 3
	 * if nS ---(u,α)⟶ nD[(-∞,β)] and u ≤ 0,  
	 * then add nS[(α★β)†]
	 * </pre>
	 * 
	 * @param nS
	 * @param nD
	 * @param eSD
	 * @param nDPotentialLabel if null, method checks nD potential. This parameter is useful to speed-up method calls with the same nD
	 * @return true if the rule has been applied.
	 */
	boolean potentialR3(LabeledNode nS, LabeledNode nD, LabeledIntEdge eSD, ObjectSet<Label> nDPotentialLabel) {
		if (nS == nD)
			return false;
		if (nDPotentialLabel == null)
			nDPotentialLabel = nD.getPotential().keySet();
		if (nD.getPotential().isEmpty())
			return false;
		String log = "";
		boolean ruleApplied = false;

		for (Entry<Label> entry : eSD.getLabeledValueSet()) {
			int u = entry.getIntValue();
			if (u > 0)
				continue;
			Label alpha = entry.getKey();
			for (Label beta : nDPotentialLabel) {
				Label alphaBeta = alpha.conjunctionExtended(beta);
				if (this.withNodeLabels) {
					alphaBeta = removeChildrenOfUnknown(alphaBeta);
				}
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = "Labeled Potential Propagation Rule 3 applied to edge " + eSD.getName()
								+ ":\nsource: "
								+ nS.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + nD.getName() + "{"
								+ pairAsString(beta, Constants.INT_NEG_INFINITE) + "}";
					}
				}
				if (potentialR1_2(nS, alphaBeta, log)) {
					if (!this.checkStatus.consistency)
						return true;
					ruleApplied = true;
					this.checkStatus.potentialCalls[2]++;
				}
			}
		}
		return ruleApplied;
	}

	/**
	 * Propagates (-∞, q) with q ∈ Q* from node potential to other node potentials.
	 * Potential Rules 1 and 2, 3, and 6 have been implemented as methods that must be used ALSO inside labelPropagation and Q3 methods for speeding up the
	 * algorithm.
	 * In this method, rules 3, 4, 5, and 6 are rechecked only for node nY.
	 * 
	 * <pre>
	 * 4) if Y?[(-∞,β)] and A ---(u,α y')⟶ B u ≤ 0
	 *    then add A ---(u,(α★β)†)⟶ B
	 *  
	 * 5) if P? ---(u,α)⟶ A  and  B[(-∞,β p')] and u ≤ 0
	 *    then B ---(u,(α★β)†)⟶ A
	 * 
	 * α in Q*
	 * β in Q* / P* 
	 * (α★β)† is the label without children of unknown.
	 * y' = y or ¬y or ¿y
	 * p' = p or ¬p or ¿p
	 * </pre>
	 * 
	 * @param nY
	 * @param limitToZ true if method has to check only edge going to Z.
	 * @param newNodesToCheck
	 * @param newEdgesToCheck
	 * @return true if the rule has been applied, false otherwise.
	 */
	boolean potentialR3_4_5_6(LabeledNode nY, boolean limitToZ, NodesToCheck newNodesToCheck, EdgesToCheck newEdgesToCheck) {
		boolean ruleApplied = false;
		String log = "", firstLog = "";
		ObjectSet<Label> nYPotentialLabel = nY.getPotential().keySet();

		if (!nYPotentialLabel.isEmpty()) {
			/**
			 * Case 3
			 * if X ---(u,α)⟶ Y[(-∞,β)] and u ≤ 0,
			 * then add X[(α★β)†]
			 * Again the rule is applied to guarantee that there is no unpropagated potential.
			 * 
			 * @see {@link checkAndApplyPotentialRule3}
			 */
			for (LabeledIntEdge yInEdge : this.g.getInEdges(nY)) {
				LabeledNode nS = this.g.getSource(yInEdge);
				if (potentialR3(nS, nY, yInEdge, nYPotentialLabel)) {
					newNodesToCheck.enqueue(nS);
					ruleApplied = true;
					if (!this.checkStatus.consistency) {
						this.checkStatus.finished = true;
						return true;
					}
				}
			}
			/**
			 * Case 6
			 * if Y[(-∞,β)] ---(u,α)⟶ X and u ≤ 0 and α subsumes β,
			 * then DELETE (u,α) from the edge.
			 *** Applied also in #labelPropagation() and R3qR3***
			 */
			if (nY != this.Z) {
			for (LabeledIntEdge yOutEdge : this.g.getOutEdges(nY)) {
				LabeledNode nD = this.g.getDest(yOutEdge);
				for (Entry<Label> entry : yOutEdge.getLabeledValueSet()) {
					int u = entry.getIntValue();
					Label alpha = entry.getKey();
					ruleApplied |= potentialR6(nY, nD, alpha, u, yOutEdge, "");
				}
			}
			}
			/**
			 * Case 4 (very expensive!)
			 * if Y?[(-∞,β)] and A ---(u,α y')⟶ B and u ≤ 0
			 * then add A ---(u,(α★β)†)⟶ B
			 */
			if (nY.isObserver()) {
				char proposition = nY.getPropositionObserved();
				if (!nYPotentialLabel.isEmpty()) {
					Collection<LabeledIntEdge> edgeSet = (limitToZ) ? this.g.getInEdges(this.Z) : this.g.getEdges();
					for (LabeledIntEdge e : edgeSet) {
						if (Debug.ON) {
							firstLog = "Labeled Potential Propagation Rule 4 considers edge " + e.getName() + "\nand observation t.p.: " + nY.getName();
						}
						LabeledNode A = this.g.getSource(e), B = this.g.getDest(e);
						boolean modified = false;
						for (Entry<Label> entry : e.getLabeledValueSet()) {
							int u = entry.getIntValue();
							if (u > 0)
								continue;
							Label alpha = entry.getKey();
							if (!alpha.contains(proposition))
								continue;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									log = firstLog + "\nsource: " + A.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + B.getName();
								}
							}
							alpha = alpha.remove(proposition);
							for (Label beta : nYPotentialLabel) {
								Label alphaBeta = alpha.conjunctionExtended(beta);
								if (this.withNodeLabels) {
									alphaBeta = removeChildrenOfUnknown(alphaBeta);
								}
								if (alpha.equals(alphaBeta))
									continue;
								if (potentialR6(A, B, alphaBeta, u, e, log))
									continue;
								if (checkAndApplyUnknownAfterObs(B, alphaBeta, u, log))
									continue;
								if (e.mergeLabeledValue(alphaBeta, u)) {
									modified = true;
									this.checkStatus.potentialCalls[3]++;
									if (Debug.ON) {
										if (LOG.isLoggable(Level.FINER)) {
											LOG.log(Level.FINER, log
													+ " and " + nY.getName() + "[" + pairAsString(beta, Constants.INT_NEG_INFINITE) + "]"
													+ "\nresult: "
													+ A.getName() + " ---" + pairAsString(alphaBeta, u) + "⟶ " + B.getName());
											firstLog = "";
										}
									}
								}
							}
						}
						if (modified) {
							ruleApplied = true;
							newEdgesToCheck.add(e, this.g.getSource(e), this.g.getDest(e), this.Z, this.g, this.propagationOnlyToZ);
						}
					}
					// Second part of Rule 4 that considers node's potentials.
					for (LabeledNode node : this.g.getNodes()) {
						boolean modified = false;
						for (Entry<Label> entry : node.getPotential().entrySet()) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									firstLog = "Labeled Potential Propagation Rule 4 considers node " + node.getName()
											+ "\nand observation t.p.: " + nY.getName();
								}
							}
							Label alpha = entry.getKey();
							if (!alpha.contains(proposition))
								continue;
							alpha = alpha.remove(proposition);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									log = firstLog + "\nsource: " + node.getName() + node.getPotential();
								}
							}
							for (Label beta : nYPotentialLabel) {
								Label alphaBeta = alpha.conjunctionExtended(beta);
								if (this.withNodeLabels) {
									alphaBeta = removeChildrenOfUnknown(alphaBeta);
								}
								if (potentialR1_2(node, alphaBeta, log)) {
									modified = true;
									this.checkStatus.potentialCalls[3]++;
									if (!this.checkStatus.consistency) {
										this.checkStatus.finished = true;
										return true;
									}
									if (Debug.ON) {
										if (LOG.isLoggable(Level.FINER)) {
											LOG.log(Level.FINER, log
													+ " and " + nY.getName() + "[" + pairAsString(beta, Constants.INT_NEG_INFINITE) + "]"
													+ "\nresult: "
													+ node.getName() + node.getPotential());
											firstLog = "";
										}
									}
								}
							}
						}
						if (modified) {
							ruleApplied = true;
							newNodesToCheck.enqueue(node);
						}
					}
				}
			} // nY is an observer
		} // nY potential is not empty
		/**
		 * Case 5
		 * if P? ---(u,α)⟶ A and B[(-∞,β p')] and u ≤ 0
		 * then B ---(u,(α★β)†)⟶ A
		 * From here Y = P?
		 */
		// First of all, determines all nodes and its potential labels having label containing the proposition.
		if (!nY.isObserver())
			return false;
		char proposition = nY.getPropositionObserved();
		Object2ObjectArrayMap<LabeledNode, ObjectSet<Label>> cleanedNodeLabelMapWithPropositionInPotential = new Object2ObjectArrayMap<>();
		for (LabeledNode node : this.g.getVertices()) {
			for (Label beta : node.getPotential().keySet()) {
				if (beta.contains(proposition)) {
					ObjectSet<Label> labelSet = cleanedNodeLabelMapWithPropositionInPotential.get(node);
					if (labelSet == null) {
						labelSet = new ObjectArraySet<>();
						cleanedNodeLabelMapWithPropositionInPotential.put(node, labelSet);
					}
					labelSet.add(beta.remove(proposition));
				}
			}
		}
		if (cleanedNodeLabelMapWithPropositionInPotential.isEmpty()) {
			return ruleApplied;
		}
		// Then, for each edge outgoing from P to A having negative values, add an edge from the B to A.
		Collection<LabeledIntEdge> edgeSet;
		if (limitToZ) {
			edgeSet = new ObjectArrayList<>();
			edgeSet.add(this.g.findEdge(nY, this.Z));
		} else {
			edgeSet = this.g.getOutEdges(nY);
		}
		for (LabeledIntEdge YA : edgeSet) {
			LabeledNode A = this.g.getDest(YA);

			for (Entry<Label> entry : YA.getLabeledValueSet()) {
				int u = entry.getIntValue();
				if (u > 0)
					continue;
				Label alpha = entry.getKey();
				for (java.util.Map.Entry<LabeledNode, ObjectSet<Label>> nodeAndPotential : cleanedNodeLabelMapWithPropositionInPotential.entrySet()) {
					LabeledNode B = nodeAndPotential.getKey();
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							firstLog = "Labeled Potential Propagation Rule 5 applied to edge " + YA.getName()
									+ "\nand node " + B.getName();
						}
					}
					if (A == B)
						continue;
					LabeledIntEdge e = this.g.findEdge(B, A);
					if (e == null) {
						e = makeNewEdge(B.getName() + "_" + A.getName(), LabeledIntEdge.ConstraintType.derived);
						this.g.addEdge(e, B, A);
					}
					boolean modified = false;
					for (Label beta : nodeAndPotential.getValue()) {
						Label alphaBeta = alpha.conjunctionExtended(beta);
						if (this.withNodeLabels) {
							alphaBeta = removeChildrenOfUnknown(alphaBeta);
						}
						if (checkAndApplyUnknownAfterObs(A, alphaBeta, u, log))
							continue;

						if (e.mergeLabeledValue(alphaBeta, u)) {
							modified = true;
							this.checkStatus.potentialCalls[4]++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, firstLog
											+ "\nSource: " + nY.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + A.getName() + " and node "
											+ B.getName() + "[" + beta + "]"
											+ "\nResult: " + B.getName() + " ---" + pairAsString(alphaBeta, u) + "⟶ " + A.getName());
									firstLog = "";
								}
							}
						}
					}
					if (modified) {
						ruleApplied = true;
						newEdgesToCheck.add(e, B, A, this.Z, this.g, this.propagationOnlyToZ);
					}
				}
			}
		}
		return ruleApplied;
	}

	/**
	 * Checks and applies Labeled Potential Rule 6 to edge <code>eSD</code>.
	 * 
	 * <pre>
	 * Potential Rule 6
	 * if nS[(-∞,β)] ---(value, α)⟶ nD and value ≤ 0 and α subsumes β,  
	 * then DELETE (u,α) from the edge.
	 * </pre>
	 * 
	 * @param nS the origin node of the edge
	 * @param nD
	 * @param alpha the new label to store
	 * @param value the present value
	 * @param eSD the edge
	 * @param preLog possible log string to prefix the log of this method.
	 * @return true if the rule was applied, false otherwise.
	 */
	boolean potentialR6(LabeledNode nS, LabeledNode nD, Label alpha, int value, LabeledIntEdge eSD, String preLog) {
		if (nS == nD || nS.getPotential().isEmpty() || value > 0)
			return false;
		for (Label beta : nS.getPotential().keySet()) {
			if (alpha.subsumes(beta)) {
				if (value != Constants.INT_NULL) {
					// it may be possible that in the edge there is already a value associated to newLabel.
					// in such a case, it must be removed.
					eSD.removeLabeledValue(alpha);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, preLog
									+ "\nLabeled Potential Propagation Rule 6 is applicable:\n"
									+ nS.getName() + nS.getPotential() + " removes value " + pairAsString(alpha, value) + " in "
									+ eSD.getName()
									+ "\nResults:" + eSD);
						}
					}
				}
				// the newLabel (and its value) must not be stored.
				this.checkStatus.potentialCalls[5]++;
				return true;
			}
		}
		return false;
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
}
