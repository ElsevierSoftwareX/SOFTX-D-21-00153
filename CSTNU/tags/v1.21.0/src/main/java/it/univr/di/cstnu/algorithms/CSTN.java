package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
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

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1).
 * This class uses LP, R0, qR0, R3* and qR3* rules and is the base class for some other specialized in which DC semantics is defined in a different way.
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
		public int cycles = 0, r0calls = 0, r3calls = 0, labeledValuePropagationcalls = 0;// , qAllNegLoop = 0, qSemiNegLoop = 0;
		// r1calls = 0, r2calls = 0,
		/**
		 * Execution time in nanoseconds.
		 */
		public long executionTimeNS = Constants.INT_NULL;

		/**
		 * True if no rule can be applied anymore.
		 */
		public boolean finished = false;

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
			this.labeledValuePropagationcalls = 0;
			// this.qAllNegLoop = 0;
			// this.qSemiNegLoop = 0;
			this.executionTimeNS = Constants.INT_NULL;
			this.finished = false;
			this.initialized = false;
		}

		@Override
		public String toString() {
			return ("The check is "
					+ (this.finished ? "" : "NOT")
					+ " finished after "
					+ this.cycles
					+ " cycle(s).\n"
					+ ((this.finished)
							? "the consistency check has determined that given network is " + (this.consistency ? "" : "NOT ") + "consistent.\n"
							: "")
					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
					// + "Rule R1 has been applied " + this.r1calls + " times.\n"
					// + "Rule R2 has been applied " + this.r2calls + " times.\n"
					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
					+ "Rule Labeled Propagation has been applied " + this.labeledValuePropagationcalls + " times.\n"
					// + "Negative qLoops: " + this.qAllNegLoop + "\n"
					// + "Negative qLoops with positive edge: " + this.qSemiNegLoop + "\n"
					+ ((this.executionTimeNS != Constants.INT_NULL)
							? "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)"
							: ""));
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
		 * It must be a set because an edge could be added more times!
		 */
		public ObjectRBTreeSet<LabeledIntEdge> edgesToCheck;
		/**
		 * 
		 */
		public boolean alreadyAddAllIncidentsToZ;

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
		 * Check if the edge that has to be add has one end-point that is an observer. In positive case, it adds
		 * all incident edges to the observation t.p.
		 * 
		 * @param enSnD
		 * @param nS
		 * @param nD
		 * @param Z
		 * @param g
		 */
		void add(LabeledIntEdge enSnD, LabeledNode nS, LabeledNode nD, LabeledNode Z, LabeledIntGraph g) {
			// in any case, the edge has to be added.
			this.edgesToCheck.add(enSnD);
			// then,
			if (this.alreadyAddAllIncidentsToZ || !nS.isObserver())
				return;
			// add all incident to nD
			this.edgesToCheck.addAll(g.getIncidentEdges(nD));
			this.alreadyAddAllIncidentsToZ = true;
		}

		/**
		 * Makes as {@link #add(LabeledIntEdge, LabeledNode, LabeledNode, LabeledNode, LabeledIntGraph)} but only when nD != Z.
		 * 
		 * @param edge
		 * @param nS
		 * @param nD
		 * @param Z
		 * @param g
		 */
		void addIfZ(LabeledIntEdge edge, LabeledNode nS, LabeledNode nD, LabeledNode Z, LabeledIntGraph g) {
			// in any case, the edge has to be added.
			this.edgesToCheck.add(edge);
			// then,
			if (this.alreadyAddAllIncidentsToZ || nD != Z || !nS.isObserver())
				return;
			// add all incident to nD
			this.edgesToCheck.addAll(g.getInEdges(Z));
			this.alreadyAddAllIncidentsToZ = true;
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
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTN.class.getName());

	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.3 - October, 4 2016";
	// static final String VERSIONandDATE = "Version 4.0 - October, 25 2016";// added management not all negative edges in a negative qLoop
	// static final public String VERSIONandDATE = "Version 5.0 - April, 03 2017";// re-factored
	static final public String VERSIONandDATE = "Version  5.2 - October, 16 2017";// better log management. This version uses LP,R0,R3*,qLP,qR0,qR3* and
																					// horizon!

	/**
	 * The name for the initial node.
	 */
	public static String ZeroNodeName = "Z";

	/**
	 * The name for the initial node.
	 */
	public static String OmegaNodeName = "Ω";

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Start...");
			}
		}
		final CSTN cstn = new CSTN();

		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Parameters ok!");
			}
		}
		if (cstn.versionReq) {
			System.out.println(CSTN.class.getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Loading graph...");
			}
		}
		CSTNUGraphMLReader graphMLReader = new CSTNUGraphMLReader(cstn.fInput, LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("LabeledIntGraph loaded!\nStandard DC Checking...");
			}
		}
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("LabeledIntGraph minimized!");
			}
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

		if (cstn.fOutput != null) {
			final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(new StaticLayout<>(cstn.g));
			try {
				graphWriter.save(cstn.g, new PrintWriter(cstn.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
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
	 * The input file containing the CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "input file. Input file has to be a CSTN graph in GraphML format.", metaVar = "CSTN_file_name")
	File fInput;

	/**
	 * Output file where to write the XML representing the minimal CSTN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "CSTN_file_name")
	File fOutput = null;

	/**
	 * Graph on which to operate.
	 */
	LabeledIntGraph g = null;

	/**
	 * WD2.2 epsilon value.
	 * It is 1 to assert that involved time points have to be just after relative observation time points.<br>
	 * <b>Such value and WD2.2 property is not necessary as required in the past because Dynamic Execution definition already contains them.</b>
	 * On the other hand, propagation rules needs such value to be complete.
	 * Therefore, WD2.2 is not more required as CSTN property but it is imposed as propagation rules.
	 */
	int wd2epsilon = 1;

	/**
	 * Absolute value of the max negative weight determined during initialization phase.
	 */
	int maxWeight = Constants.INT_NULL;

	/**
	 * Horizon value. A node that has to be executed after such time means that it has not to be executed!
	 */
	int horizon = Constants.INT_NULL;

	/**
	 * Output stream to fOutput
	 */
	PrintStream output = null;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	boolean versionReq = false;

	/**
	 * Default constructor.
	 */
	CSTN() {
	}

	/**
	 * Constructor for
	 * 
	 * @param g graph to check
	 */
	public CSTN(LabeledIntGraph g) {
		this();
		this.setG(g);// sets also checkStatus!
	}

	/**
	 * checkWellDefinitionProperties.
	 * It checks only WD1, WD2 (light version).
	 *
	 * @return true if the g is a CSTN well defined.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	public boolean checkWellDefinitionProperties() throws WellDefinitionException {
		boolean flag = false;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Checking if graph is well defined...");
			}
		}
		for (final LabeledIntEdge e : this.g.getEdges()) {
			flag = checkWellDefinitionProperty1and3(this.g.getSource(e), this.g.getDest(e), e, false);
		}
		for (final LabeledNode node : this.g.getNodes()) {
			flag = flag && checkWellDefinitionProperty2(node, false);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, ((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well-defined graph!\n"));
			}
		}
		return flag;
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property (WD1):<br>
	 * any labeled valued of the edge has a label that is consistent and subsumes both labels of two endpoints.
	 * As a sanity check, this method checks also that for each literal there exists an observation time point.
	 * Since in 2017-04-07 it has been shown that WD3 is not more necessary,
	 * this method was augmented by the check of WD3 (edge label honesty) but with the change that, now, if a edge
	 * label is dishonest, it is fixed without throwing exceptions!
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
				String msg = "Found a labeled value in " + eSN + " inconsistent with the conjunction of node labels, "
						+ conjunctedLabel + ".";
				if (hasToBeFixed) {
					eSN.removeLabel(currentLabel);
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
					eSN.removeLabel(currentLabel);
					currentLabel = currentLabel.conjunction(conjunctedLabel);
					eSN.putLabeledValue(currentLabel, v);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, "Fixed as required!");
						}
					}
					continue;
				}
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
			// Checks if label subsumes all observer-t.p. labels of observer t.p. whose proposition is present into the label.
			// WD3 property.
			Label currentLabelModified = new Label(currentLabel);
			for (final char l : currentLabel.getPropositions()) {
				LabeledNode obs = this.g.getObserver(l);
				if (obs == null) {
					final String msg = "Observation node of literal " + l + " of label " + currentLabel + " in edge " + eSN + " does not exist.";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, msg);
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
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING, "Label " + currentLabel + " of edge " + eSN + " does not subsume label of obs node " + obs
										+ " and cannot be expanded because it becomes inconsistent.");
							}
						}
						throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
					}
				}
			}
			if (!currentLabelModified.equals(currentLabel)) {
				int v = entry.getIntValue();
				eSN.removeLabel(currentLabel);
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
	 * Checks whether the label of a node satisfies the second well definition property:<br>
	 * <blockquote>For each literal present in a node label label:
	 * <ol>
	 * <li><s>the label of the observation node of the considered literal is subsumed by the label of the current node.</s>
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
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, msg);
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
			// if (hasToBeFixed) {
			LabeledIntEdge e = this.g.findEdge(node, obs);
			if ((e == null) || ((v = e.getValue(nodeLabel)) == Constants.INT_NULL) || (v > 0)) {// WD2.2 ICAPS paper
				// WD2.2 has been proved to be redundant. So, it can be removed. Here we maintain a light version of it.
				// Light version: a node with label having 'p' has to be just after P?, i.e., P?<---[0,p]---X_p.
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
					e.mergeLabeledValue(nodeLabel, -this.wd2epsilon);// this is not necessary, but it can speed up the DC checking.
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING, "Fixed adding " + pairAsString(nodeLabel, -this.wd2epsilon) + " to " + e);
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
	 * Checks the dynamic consistency of a CSTN instance.
	 * During the execution of this method, the given graph is modified. <br>
	 * If the check is successful, all constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
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
	 * Checks the dynamic consistency of a CSTN instance without initialize the network.<br>
	 * This method can be used ONLY when it is guaranteed that the network is already initialize by method {@link #initAndCheck()}.
	 * In case of doubts, use {@link #dynamicConsistencyCheck()}.
	 *
	 * @return the final status of the checking with some statistics.
	 */
	CSTNCheckStatus dynamicConsistencyCheckWOInit() {
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("Graph has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}
		EdgesToCheck edgesToCheck = new EdgesToCheck(this.g.getEdges());
		@SuppressWarnings("unused")
		final int propositionN = this.g.getObserverCount();
		final int nodeN = this.g.getVertexCount();
		// TODO: trovare il numero giusto di iterazioni
		final int maxCycles = (int) (Math.pow(nodeN, 3)); // * Math.pow(2, propositionN));
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "The maximum number of possible cycles is " + maxCycles);
			}
		}

		// final boolean hierarchyMap = LabeledIntEdge.labeledValueMapFactory.createLabeledIntMap().getClass().equals(LabeledIntHierarchyMap.class);
		int i;
		Instant startInstant = Instant.now();
		for (i = 1; (i <= maxCycles) && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}
			oneStepDynamicConsistencyByEdges(edgesToCheck);// Don't use this. because such method is override!

			if (this.checkStatus.consistency && !this.checkStatus.finished) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						StringBuilder log = new StringBuilder();
						log.append("During the check n. " + i + ", " + edgesToCheck.size()
								+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
						for (LabeledIntEdge e : edgesToCheck) {
							if (e.size() == 0)
								continue;
							log.append("Edge " + e + "\n");
						}
						LOG.log(Level.FINER, log.toString());
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
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Final inconsistent graph: " + this.g);
					}
				}
			}
			return this.checkStatus;
		}

		if ((i > maxCycles) && !this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Last determined graph: " + this.g);
					}
				}
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			return this.checkStatus;
		}

		// consistent && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO,
						"Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: " + this.checkStatus);
			}
		}
		// Just an experiment to find the most heavy edge
		// LabeledIntEdge max = new LabeledIntEdge("guard",LabeledIntEdge.ConstraintType.internal, Label.emptyLabel, 0, false);
		// for (LabeledIntEdge e : currentGraph.getEdgesArray()) {
		// if (e.labeledValueSet().size() > max.labeledValueSet().size()) {
		// max = e;
		// }
		// }
		// System.out.println("Edge with maximum set of labels: "+max);
		LabeledIntGraph optimizedGraph = new LabeledIntGraph(this.g.getName(), this.g.getInternalLabeledValueMapImplementationClass());
		optimizedGraph.copyCleaningRedundantLabels(this.g);
		this.g = optimizedGraph;
		return this.checkStatus;
	}

	/**
	 * @return the checkStatus
	 */
	public CSTNCheckStatus getCheckStatus() {
		return this.checkStatus;
	}

	/**
	 * Determine the set of edges P?-->nX where P? is an observer node and nX is the given node.
	 * 
	 * @param nX the given node.
	 * @return the set of edges P?-->nX, an empty set if nX is empty or there is no observer or there is no such edges.
	 */
	public ObjectList<LabeledIntEdge> getEdgeFromObserversToNode(final LabeledNode nX) {

		if (nX == this.g.getZ()) {
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
	 * @return the g
	 */
	public LabeledIntGraph getG() {
		return this.g;
	}

	/**
	 * Help method to initialize and check the CSTN represented by graph g. The {@link #dynamicConsistencyCheck()} calls this method before
	 * to execute the check. If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level.
	 * Since the current DC checking algorithm is complete only the CSTN instance contains an upper bound to the distance between Z (the first node) and Ω (the
	 * last node),
	 * this procedure add such upper bound (= #nodes * max weight value).
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	public boolean initAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Starting initial well definition check.");
			}
		}
		this.g.clearCache();

		// Checks the presence of Z node!
		LabeledNode Z = this.g.getZ();
		if (Z == null) {
			Z = this.g.getNode(CSTN.ZeroNodeName);
			if (Z == null) {
				// We add by authority!
				Z = new LabeledNode(CSTN.ZeroNodeName);
				Z.setX(5);
				Z.setY(5);
				this.g.addVertex(Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + CSTN.ZeroNodeName + " node found: added!");
				}
			}
			this.g.setZ(Z);
		} else {
			if (!Z.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
				}
				Z.setLabel(Label.emptyLabel);
			}
			CSTN.ZeroNodeName = Z.getName();
		}
		// Checks the presence of Ω node!
		LabeledNode Ω = this.g.getΩ();
		if (Ω == null) {
			Ω = this.g.getNode(CSTN.OmegaNodeName);
			if (Ω == null) {
				// We add by authority!
				Ω = new LabeledNode(CSTN.OmegaNodeName);
				Ω.setX(5);
				Ω.setY(50);
				this.g.addVertex(Ω);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + CSTN.OmegaNodeName + " node found: added!");
				}
			}
			this.g.setΩ(Ω);
		} else {
			if (!Ω.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Ω node has not empty label. Label removed!");
				}
				Ω.setLabel(Label.emptyLabel);
			}
			CSTN.OmegaNodeName = Ω.getName();
		}

		// Checks well definiteness of edges and determine maxWeight
		this.maxWeight = 0;
		for (final LabeledIntEdge e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, "Initial Checking edge e: " + e);
				}
			}
			// Determines the absolute max weight value
			for (Object2IntMap.Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v < this.maxWeight)
					this.maxWeight = v;
				// else if (v > this.maxWeight)
				// this.maxWeight = v;
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
		this.maxWeight = -this.maxWeight;
		// Determine horizon value
		long product = ((long) this.maxWeight) * this.g.getVertexCount();
		if (product >= Constants.INT_POS_INFINITE) {
			throw new ArithmeticException("Horizon value is not representable by an integer.");
		}
		this.horizon = (int) product;

		// Init two useful structures
		this.g.getPropositions();

		// Checks well definiteness of nodes
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {

			// 1. Checks that observation node doesn't have the observed proposition in its label!
			final char obs = node.getPropositionObserved();
			final Label label = node.getLabel();
			if (obs != Constants.UNKNOWN && label.contains(obs)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName()
								+ "'. Removed!");
					}
				}
				label.remove(obs);
			}

			// WD2 is checked and adjusted here
			try {
				checkWellDefinitionProperty2(node, true);
			} catch (final WellDefinitionException ex) {
				throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
			}

			// 3. Checks that each node has an edge to Z.
			if (node != Z) {
				LabeledIntEdge edgeToZ = this.g.findEdge(node, Z);
				if (edgeToZ == null) {
					edgeToZ = makeNewEdge(node.getName() + "_" + CSTN.ZeroNodeName, ConstraintType.internal);
					this.g.addEdge(edgeToZ, node, Z);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs after '" + CSTN.ZeroNodeName + "'.");
						}
					}
				}
				edgeToZ.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
			}

			// 4. Checks that each node has an edge from Ω
			if (node != Ω) {
				LabeledIntEdge edgeFromΩ = this.g.findEdge(Ω, node);
				if (edgeFromΩ == null) {
					edgeFromΩ = makeNewEdge(CSTN.OmegaNodeName + "_" + node.getName(), ConstraintType.internal);
					this.g.addEdge(edgeFromΩ, Ω, node);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs before '" + CSTN.OmegaNodeName
											+ "'.");
						}
					}
				}
				edgeFromΩ.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be before Ω!
			}
		}
		// Edge from Z to Ω
		LabeledIntEdge eZΩ = this.g.findEdge(Z, Ω);
		if (eZΩ == null) {
			eZΩ = makeNewEdge(CSTN.ZeroNodeName + "_" + CSTN.OmegaNodeName, ConstraintType.internal);
			this.g.addEdge(eZΩ, Z, Ω);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							"It is necessary to add a constraint to guarantee that '" + Ω.getName() + "' occurs at most " + this.horizon + " after '"
									+ CSTN.ZeroNodeName + "'.");
				}
			}
		}
		eZΩ.mergeLabeledValue(Label.emptyLabel, this.horizon);

		// It is better to normalize with respect to the label modification rules before starting the DC check.
		// Such normalization assures only that redundant labels are removed (w.r.t. R0)
		// Q* are not solved by this normalization!
		this.checkStatus.reset();
		try {
			for (final LabeledIntEdge e : this.g.getEdges()) {
				final LabeledNode s = this.g.getSource(e);
				final LabeledNode d = this.g.getDest(e);

				// Normalize with respect to R0--R3
				if (s.isObserver()) {
					labelModificationR0qR0(s, d, Z, e);
				}
				this.labelModificationR3qR3(s, d, Z, e);
				if (s.isObserver()) {
					// again because R3 could have add a new value;
					labelModificationR0qR0(s, d, Z, e);
				}
			}
		} catch (IllegalStateException ex) {
			String logMsg = "Graph is not well defined: " + ex.getMessage();
			LOG.severe(logMsg);
			throw new WellDefinitionException(logMsg);
		}
		// if (Debug.ON && LOG.isLoggable(Level.FINEST)) {
		// LOG.log(Level.FINEST, "A preliminary application of label modification rules has been done: " + this.checkStatus.toString());
		// }
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Initial well definition check done!");
			}
		}
		this.checkStatus.initialized = true;
		return true;
	}

	/**
	 * Applies the labeled propagation rule:<br>
	 * <b>Standard DC semantics is assumed.</b><br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics or epsilon-reaction time.</b>
	 * The rule is the EqLp+ not yet published.
	 * 
	 * <pre>
	 * if A --[α, u]--&gt; B --[β, v]--&gt; C, then A --[(α★β)†, u+v]--&gt; C if u<0
	 * 
	 * α,β in Q*
	 * (α★β)† is the label without children of unknown.
	 *
	 * If A==C and u+v < 0, then
	 * - if (α★β)† does not contain ¿ literals, the network is not DC
	 * - if (α★β)† contains ¿ literals, the u+v becomes -∞
	 * 
	 * Be careful, in order to propagate correctly possibly -∞ self-loop, it is necessary call this method also for triple like with nodes A == B or B==C!
	 * </pre>
	 * 
	 * @param nA CANNOT BE NULL!
	 * @param nB CANNOT BE NULL!
	 * @param nC CANNOT BE NULL!
	 * @param eAB CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eBC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eAC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @return true if a reduction has been applied.
	 */
	boolean labeledPropagationqLP(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB,
			final LabeledIntEdge eBC, LabeledIntEdge eAC) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false;
		Label nAnCLabel = nA.getLabel().conjunction(nC.getLabel());
		if (nAnCLabel == null)
			return false;

		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			/**
			 * If there is a self loop containing a (-∞, q*), it must be propagated! NO MORE USED!
			 */
			final int u = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet()) {
				final Label labelBC = BCEntry.getKey();
				final int v = BCEntry.getIntValue();
				boolean qLabel = false;
				Label newLabelAC = null;
				if ((u >= 0) && (v < 0)) {
					newLabelAC = labelAB.conjunction(labelBC);
					if (newLabelAC == null) {
						continue;
					}
				} else {
					newLabelAC = labelAB.conjunctionExtended(labelBC);
					qLabel = newLabelAC.containsUnknown();
					if (qLabel) {
						removeChildrenOfUnknown(newLabelAC);
					}
				}
				if (!newLabelAC.subsumes(nAnCLabel)) {
					if (Debug.ON)
						LOG.log(Level.FINEST,
								"New alphaBeta label " + newLabelAC + " does not subsume node labels " + nAnCLabel + ". New value cannot be added.");
					continue;
				}

				/**
				 * 2017-05-04 Roberto verifies that it is faster to propagate all values (positive and negative).
				 */
				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(u, v);
				int oldValue = eAC.getValue(newLabelAC);

				if (nA == nC) {
					if (sum >= 0) {
						// it would be a redundant edge
						continue;
					}
					// sum is negative!
					if (!qLabel) {
						eAC.mergeLabeledValue(newLabelAC, sum);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								String log = "Label Propagation Rule applied to edge " + eAC.getName()
										+ ":\nsource: "
										+ nA.getName() + " ---" + pairAsString(labelAB, u) + "---> " + nB.getName() + " ---" + pairAsString(labelBC, v)
										+ "---> "
										+ nC.getName()
										+ "\nresult: "
										+ nA.getName() + " ---" + pairAsString(newLabelAC, sum) + "---> " + nC.getName()
										+ "; old value: " + Constants.formatInt(oldValue);
								LOG.log(Level.FINER, log + "\n***\nFound a negative loop " + pairAsString(newLabelAC, sum) + " in the edge  " + eAC + "\n***");
							}
						}
						this.checkStatus.consistency = false;
						this.checkStatus.finished = true;
						this.checkStatus.labeledValuePropagationcalls++;
						return true;
					}
					sum = Constants.INT_NEG_INFINITE;
				} else {
					// in the case of A != C, a value is stored only if it is more negative than the current one.
					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						continue;
					}
				}
				// here sum has to be add!
				// I have to prepare the log before the execution of the merge!
				String log = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = "Label Propagation Rule applied to edge " + eAC.getName()
								+ ":\nsource: "
								+ nA.getName() + " ---" + pairAsString(labelAB, u) + "---> " + nB.getName() + " ---" + pairAsString(labelBC, v)
								+ "---> "
								+ nC.getName()
								+ "\nresult: "
								+ nA.getName() + " ---" + pairAsString(newLabelAC, sum) + "---> " + nC.getName()
								+ "; old value: " + Constants.formatInt(oldValue);
					}
				}

				if (eAC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					this.checkStatus.labeledValuePropagationcalls++;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, log);
						}
					}
					// if (sum == Constants.INT_NEG_INFINITE && nA == nC && u != Constants.INT_NEG_INFINITE && v != Constants.INT_NEG_INFINITE) {
					// if (v >= 0)
					// this.checkStatus.qSemiNegLoop++;
					// else
					// this.checkStatus.qAllNegLoop++;
					// }
				}
			}
		}
		return ruleApplied;
	}

	/**
	 * Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Standard DC semantics is assumed.</b>
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
	 * @param nZ
	 * @param eObsX the edge connecting nObs? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0qR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge eObsX) {
		// Visibility is package because there is Junit Class test that checks this method.
		boolean ruleApplied = false, mergeStatus;
		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R0: start.");
			}
		}
		if (nX.getLabel().contains(p)) {
			// Table 1 ICAPS
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST,
							"R0: Proposition " + p + " is present in the X label '" + nX.getLabel() + ". WD1 must be preserved, so R0 cannot be applied.");
				}
			}
			return false;
		}

		final ObjectSet<Label> obsXLabelSet = eObsX.getLabeledValueMap().keySet();

		for (final Label l : obsXLabelSet) {
			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = eObsX.getValue(l);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w > 0) {// Table 1 ICAPS paper.
				// When X==Z, w must be < 0 to apply rule. w==0 is not considered because it doesn't occur since each node is at least 0 distance from Z.
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
			if (alphaPrime == null) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					logMessage = "R0 simplifies a label of edge " + eObsX.getName()
							+ ":\nsource: " + nObs.getName() + " ---" + pairAsString(l, w) + "---> " + nX.getName()
							+ "\nresult: " + nObs.getName() + " ---" + pairAsString(alphaPrime, w) + "---> " + nX.getName();
				}
			}

			this.checkStatus.r0calls++;
			ruleApplied = true;
			mergeStatus = eObsX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus) {
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
	 * It is assumed that nS!=nD!
	 * 
	 * @param nS node
	 * @param nD node
	 * @param nZ
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSD) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: start.");
			}
		}
		boolean ruleApplied = false;

		ObjectList<LabeledIntEdge> Obs2nDEdges = this.getEdgeFromObserversToNode(nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();
		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);

			if (nObs.equalsByName(nS))
				continue;
			final char p = nObs.getPropositionObserved();

			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// WD1 must be preserved!
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "R3: Proposition " + p + " is present in the nS label '" + nS.getLabel() + " or nD label " + nD.getLabel()
								+ ". WD1 must be preserved, so R3 cannot be applied.");
					}
				}
				continue;
			}
			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.getLabeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (w > 0 || (w == 0 && nD == nZ)) { // Table 1 ICAPS
					// (w == 0 && nD==Z), it means that P? is executed at 0. So, even if v==0 (it cannot be v>0),
					// the constraint does not imply an implicit constraint (stripping p). So, we don't touch the constraint.
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

					final int max = Math.max(w, v);

					Label newLabel = (nD != nZ) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					ruleApplied = eSD.mergeLabeledValue(newLabel, max);
					if (ruleApplied) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "---> " + nD.getName()
										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
										+ "\nresult: add " + nD.getName() + " <---" + pairAsString(newLabel, max) + "--- " + nS.getName());
							}
						}
						this.checkStatus.r3calls++;
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
	 * Simple method to determine the label αβγ' for rule R3*.<br>
	 * See Table 1 and Table 2 ICAPS 2016 paper.
	 * Rule R3* is:
	 * 
	 * <pre>
	 * if P? --[αβ, w]--&gt; nD &lt;--[βγp, v]-- nS  and w &le; 0 (ε)
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[αβγ', max{w',v}]-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * ε>0 is the reaction time.
	 * w' is w or w-ε according with the kind of semantics.
	 * </pre>
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

		Label labelToCleanWOp = new Label(labelToClean);
		labelToCleanWOp.remove(observed);

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
		final Label gamma = labelToCleanWOp.getSubLabelIn(labelFromObs, false);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + " γ: " + gamma + "\n.");
			}
		}
		gamma.remove(this.g.getChildrenOf(nObs));

		Label alphaBetaGamma = alpha.conjunction(beta).conjunction(gamma);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append(", αβγ'=" + alphaBetaGamma);
		}

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
	 * @param nZ
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up.)
	 * @param labelFromObs label of the edge from observer
	 * @return α'
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeAlphaPrime(final LabeledNode nX, final LabeledNode nObs, final LabeledNode nZ, final char observed, final Label labelFromObs) {

		if (nX.getLabel().contains(observed))
			return null;

		StringBuilder logStr;
		if (Debug.ON) {
			logStr = new StringBuilder();
			if (LOG.isLoggable(Level.FINEST))
				logStr.append("labelEdgeFromObs = " + labelFromObs);
		}

		Label alphaPrime = new Label(labelFromObs);
		alphaPrime.remove(observed);
		alphaPrime.remove(this.g.getChildrenOf(nObs));
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				logStr.append(", labelWithoutPandChildren=" + alphaPrime);
		}

		if (nX == nZ && alphaPrime.containsUnknown()) {
			removeChildrenOfUnknown(alphaPrime);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				logStr.append(", α'=" + alphaPrime);
			}
		}
		if (!alphaPrime.subsumes(nX.getLabel().conjunction(nObs.getLabel()))) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, logStr.toString() + " α' does not subsume labels from nodes:" + nX.getLabel().conjunction(nObs.getLabel()));
				}
			}
			return null;
		}
		return alphaPrime;
	}

	/**
	 * Simple method to determine the label (β*γ)† to use in rules qR3*.
	 * See Table 1 and Table 2 ICAPS 2016.
	 * 
	 * <pre>
	 * if P? --[γ, w]--&gt; Z &lt;--[βp'θ, v]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[(β*γ)†, max{w',v}]-- nS
	 * where:
	 * p' is any literal (¿p included) of p.
	 * γ does not contain p' or P? children.
	 * β cannot contain children of P?
	 * θ contains only children of P?.
	 * (β*γ)† is the q-label obtained by removing children of any q-literals that appear in β*γ
	 * ε>0 is the reaction time.
	 * w' is w or w-ε according with the kind of semantics.
	 * </pre>
	 * 
	 * @param nS
	 * @param nZ
	 * @param nObs
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observer
	 * @param labelToClean
	 * @return αβγ'
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeBetaGammaDagger4qR3(final LabeledNode nS, final LabeledNode nZ, final LabeledNode nObs, final char observed,
			final Label labelFromObs, Label labelToClean) {

		final StringBuilder slog = new StringBuilder();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append("labelEdgeFromObs = " + labelFromObs);
		}
		if (labelFromObs.contains(observed)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST,
							slog.toString() + " (β*γ)† cannot be calculated because labelFromObs contains the prop " + observed + " that has to be removed.");
				}
			}
			return null;
		}

		final Label beta = new Label(labelToClean);
		beta.remove(observed);

		Label childrenOfP = this.g.getChildrenOf(nObs);
		if (childrenOfP != null && !childrenOfP.isEmpty()) {
			Label test = new Label(labelFromObs);
			test.remove(childrenOfP);
			if (!labelFromObs.equals(test)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								slog.toString() + " (β*γ)† cannot exist because labelFromObs contains a child of " + observed
										+ " that has to be removed. The children are " + childrenOfP);
					}
				}
				return null;
			}
			beta.remove(childrenOfP);
		}

		Label betaGamma = labelFromObs.conjunctionExtended(beta);

		// remove all children of unknowns.
		removeChildrenOfUnknown(betaGamma);

		if (!betaGamma.subsumes(nS.getLabel())) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + "(β*γ)† does not subsume labels from nodes:" + nS.getLabel());
				}
			}
			return null;
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
					+ " <graph_file_name>");
			return false;
		}
		return true;
	}

	/**
	 * Executes one step of the dynamic consistency check: for each edge in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule is
	 * applied
	 * two times: one time having the edge as first edge, one time having the edge as second edge.
	 * All modified or new edges are returned in the set 'edgesToCheck'.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck edgesToCheck) {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;
		final LabeledNode Z = this.g.getZ();

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "\nStart application labeled propagation rule+R0+R3.");
			}
		}
		/**
		 * March, 06 2016 I try to apply the rules on all edges that have been modified in the previous cycle.
		 */
		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		int i = 1, n = edgesToCheck.size();
		for (LabeledIntEdge AB : edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n***Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			// initAndCheck does not resolve completely a qStar.
			// It is necessary to check here the edge before to consider the second edge.
			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
			edgeCopy = this.g.getEdgeFactory().get(AB);
			if (A.isObserver()) {
				// R0 on the resulting new values
				labelModificationR0qR0(A, B, Z, AB);
			}
			labelModificationR3qR3(A, B, Z, AB);
			if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// R0 on the resulting new values
				labelModificationR0qR0(A, B, Z, AB);
			}
			if (!AB.equalsAllLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(AB, A, B, Z, this.g);
			}

			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().get(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationqLP(A, B, C, AB, BC, AC);

				/**
				 * I need to clean values on AC
				 * March, 8 2016 By an experimental results, it seems that the following clean code is not necessary. Without it, the final number of rule
				 * applications does not change!
				 */
				// if (A.isObserver()) {
				// // R0 on the resulting new values
				// this.labelModificationR0(currentGraph, A, C, AC, status);
				// }
				//
				// // if (!this.excludeR1R2 && C.isObserver()) {
				// // // R2 on the resulting new values.
				// // this.labelModificationR2(currentGraph, C, A, AC, status);
				// // }
				//
				// // R3 on the resulting new values
				// this.labelModificationR3(currentGraph, A, C, AC, status);
				// if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// // R0 on the resulting new values
				// this.labelModificationR0(currentGraph, A, C, AC, status);
				// }
				//
				// // if (!this.excludeR1R2) {
				// // // R1 on the resulting new values.
				// // this.labelModificationR1(currentGraph, A, C, AC, status);
				// // if (C.isObserver()) {
				// // this.labelModificationR2(currentGraph, C, A, AC, status);// It should be like R0! To verify
				// // // experimentally.
				// // }
				// // }
				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					newEdgesToCheck.add(AC, A, C, Z, this.g);
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AC)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.add(AC, A, C, Z, this.g);
				}

				if (!this.checkStatus.consistency)
					return this.checkStatus;
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().get(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationqLP(C, A, B, CA, AB, CB);

				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
					newEdgesToCheck.add(CB, C, B, Z, this.g);
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(CB)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.add(CB, C, B, Z, this.g);
				}

				if (!this.checkStatus.consistency)
					return this.checkStatus;
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3.");
			}
		}
		edgesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
		}
		if (!this.checkStatus.consistency)
			this.checkStatus.finished = true;
		return this.checkStatus;
	}

	/**
	 * Executes one step of the dynamic consistency check: for each possible triangle of the network, label propagation rule is applied and, on the resulting
	 * edge, all other rules R0--R3 are also applied.
	 *
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {

		LabeledNode B, C;
		LabeledIntEdge AC;// AB, BC
		boolean createEdge = false;
		final LabeledNode Z = this.g.getZ();

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "\nStart application labeled propagation rule+R0+R3.");
			}
		}
		/**
		 * March, 03 2016 I try to apply the rules on all edges making a by-row-visit to the adjacency matrix.
		 */
		for (LabeledNode A : this.g.getVertices()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering edges outgoing from " + A);
				}
			}
			for (LabeledIntEdge AB : this.g.getOutEdges(A)) {
				B = this.g.getDest(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				// Since in some graphs it is possible that there is not BC, we apply R0 and R3 to AB
				if (A.isObserver()) {
					// R0 on the resulting new values
					labelModificationR0qR0(A, B, Z, AB);
				}
				this.labelModificationR3qR3(A, B, Z, AB);
				if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
					// R0 on the resulting new values
					labelModificationR0qR0(A, B, Z, AB);
				}
				for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
					C = this.g.getDest(BC);
					// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

					if (B.isObserver()) {
						// R0 on the resulting new values
						labelModificationR0qR0(B, C, Z, BC);
					}
					this.labelModificationR3qR3(B, C, Z, BC);
					if (B.isObserver()) {// R3 can add new values that have to be minimized.
						// R0 on the resulting new values
						labelModificationR0qR0(B, C, Z, BC);
					}
					// Now it is possible to propagate the labels with the standard rules
					AC = this.g.findEdge(A, C);
					// I need to preserve the old edge to compare below
					createEdge = (AC == null);
					if (createEdge) {
						AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					}

					this.labeledPropagationqLP(A, B, C, AB, BC, AC);

					if (!this.checkStatus.consistency)
						return this.checkStatus;

					@SuppressWarnings("null")
					boolean empty = AC.isEmpty();
					if (createEdge && !empty) {
						// the new CB has to be added to the graph!
						this.g.addEdge(AC, A, C);
					} else {
						if (empty)
							continue;
					}

					if (A.isObserver()) {
						// R0 on the resulting new values
						labelModificationR0qR0(A, C, Z, AC);
					}

					// if (!this.excludeR1R2 && C.isObserver()) {
					// // R2 on the resulting new values.
					// this.labelModificationR2(currentGraph, C, A, AC, status);
					// }

					// R3 on the resulting new values
					this.labelModificationR3qR3(A, C, Z, AC);

					if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						labelModificationR0qR0(A, C, Z, AC);
					}

					// if (!this.excludeR1R2) {
					// // R1 on the resulting new values.
					// this.labelModificationR1(currentGraph, A, C, AC, status);
					// if (C.isObserver()) {
					// this.labelModificationR2(currentGraph, C, A, AC, status);// It should be like R0! To verify
					// // experimentally.
					// }
					// }
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
	 * Modifies label removing all children of possibly present unknown literals in label.
	 * 
	 * @param label
	 * @return the label modified.
	 */
	Label removeChildrenOfUnknown(Label label) {
		Label old = null;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				old = new Label(label);
			}
		}
		for (final char unknownLit : label.getAllUnknown()) {
			label.remove(this.g.getChildrenOf(this.g.getObserver(unknownLit)));
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST) && !label.equals(old)) {
				LOG.log(Level.FINEST, "Remove children of unknown has changed label " + old + " to " + label);
			}
		}
		return label;
	}

	/**
	 * @param g the g to set
	 */
	void setG(LabeledIntGraph g) {
		if (g == null)
			throw new IllegalArgumentException("Input graph is null!");
		this.g = g;
		this.maxWeight = Constants.INT_NEG_INFINITE;
		this.horizon = Constants.INT_NEG_INFINITE;
		this.checkStatus.reset();
	}

}
