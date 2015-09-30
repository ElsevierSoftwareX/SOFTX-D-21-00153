package it.univr.di.cstnu;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.univr.di.cstnu.WellDefinitionException.Type;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
import it.univr.di.labeledvalue.Literal;
import it.univr.di.labeledvalue.ValueNodeSetPair;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import edu.uci.ics.jung.io.GraphMLWriter;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
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
		boolean consistency = true;
		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings("javadoc")
		int cycles = 0, r0calls = 0, r1calls = 0, r2calls = 0, r3calls = 0, labeledValuePropagationcalls = 0;

		/**
		 * Execution time in milliseconds.
		 */
		long executionTime = 0;

		/**
		 * True if no rule can be applied anymore.
		 */
		boolean finished = false;

		@Override
		public String toString() {
			return ("The check is "
					+ (this.finished ? "" : "NOT")
					+ " finished after "
					+ this.cycles
					+ " cycle(s).\n"
					+ ((this.finished) ? "the consistency check has determined that given network is " + (this.consistency ? "" : "NOT ")
							+ "consistent.\n"
							: "")
					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
					+ "Rule R1 has been applied " + this.r1calls + " times.\n"
					+ "Rule R2 has been applied " + this.r2calls + " times.\n"
					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
					+ "Rule Labeled Propagation has been applied " + this.labeledValuePropagationcalls + " times.\n"
					+ "The global execution time has been " + executionTime + " ms. approx.");
		}
	}

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTN.class.getName());

	/**
	 * Version of the class
	 */
	static final String VERSIONandDATE = "2.0 - May, 28 2015";

	/**
	 * The name for the reference node.
	 */
	private static final String ZeroNodeName = "Z";

	/**
	 * <p>
	 * checkWellDefinitionProperties.
	 * </p>
	 *
	 * @param g
	 *                a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @return true if the g is a CSTN well defined.
	 * @throws it.univr.di.cstnu.WellDefinitionException
	 *                 if any.
	 */
	public static boolean checkWellDefinitionProperties(final LabeledIntGraph g) throws WellDefinitionException {
		boolean flag = false;
		if (CSTN.LOG.isLoggable(Level.FINER)) {
			CSTN.LOG.log(Level.FINER, "Checking if graph is well defined...");
		}
		for (final LabeledIntEdge e : g.getEdges()) {
			flag = CSTN.checkWellDefinition1Property(g.getSource(e), g.getDest(e), e);
			flag = flag && CSTN.checkWellDefinition3Property(g, e);
		}
		for (final LabeledNode node : g.getNodes()) {
			flag = flag && CSTN.checkWellDefinition2Property(g, node);
		}
		if (CSTN.LOG.isLoggable(Level.FINER)) {
			CSTN.LOG.log(Level.FINER, ((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well defined graph!\n"));
		}
		return flag;
	}

	/**
	 * Help method to initialize and check the CSTN represented by graph g. The {@link #dynamicConsistencyCheck(LabeledIntGraph, boolean, boolean)} calls
	 * this method before to execute the check.
	 *
	 * @param g
	 *                a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @param instantaneousReaction
	 *                true if the CSTN g reacts instantaneously to observation of propositions.
	 * @return true if the graph is a well formed CSTN.
	 */
	public static boolean initAndCheck(final LabeledIntGraph g, boolean instantaneousReaction) {
		if (g == null)
			throw new IllegalArgumentException("The graph is null!");

		g.clearCache();

		SortedSet<LabeledIntEdge> edgeSet = new ObjectRBTreeSet<>(g.getEdges());
		for (final LabeledIntEdge e : edgeSet) {

			if (CSTN.LOG.isLoggable(Level.FINEST))
				CSTN.LOG.log(Level.FINEST, "Init. Checkin edge e: " + e);
			// Sanity check for the label:
			// set one label if endpoints have one and edge hasn't any.
			// WD1 is checked and adjusted here
			final LabeledNode s = g.getSource(e);
			final LabeledNode d = g.getDest(e);
			final Label conjunctLabel = s.getLabel().conjunction(d.getLabel());
			if (CSTN.LOG.isLoggable(Level.FINEST)) {
				CSTN.LOG.log(Level.FINEST, "Source label: " + s.getLabel() + "; dest label: " + d.getLabel() + " new label: " + conjunctLabel);
			}
			if (conjunctLabel == null) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Node " + s + " and node " + d + " have inconsistent labels but there is an edge, " + e
							+ " beetwen them. Edge removed!");
				}
				g.removeEdge(e);
				continue;
			}
			if (!conjunctLabel.isEmpty()) {
				Label l1;
				for (final Object2IntMap.Entry<Label> entry : e.labeledValueSet()) {
					l1 = entry.getKey();
					if (l1.conjunction(conjunctLabel) == null) {
						if (CSTNU.LOG.isLoggable(Level.WARNING)) {
							CSTNU.LOG.log(Level.WARNING, "Found labeled value " + l1 + " in " + e
									+ " inconsistent with the conjunction of node labels, "
									+ conjunctLabel + ". Removed");
						}
						e.removeLabel(entry.getKey());
					} else {
						if (!l1.subsumes(conjunctLabel)) {
							CSTNU.LOG.warning("Found a labeled value in " + e
									+ " that does not subsume the conjunction of node labels, " + conjunctLabel
									+ ". It has been fixed.");
							int v = entry.getIntValue();
							e.removeLabel(l1);
							e.putLabeledValue(l1.conjunction(conjunctLabel), v);
						}
					}
				}
			}
			if (e.size() == 0) {
				// The merge removed all labels...
				g.removeEdge(e);
				continue;
			}

			// now I can check the WD3 property
			try {
				CSTN.checkWellDefinition3Property(g, e);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (e.isContingentEdge()) {
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, "Found a contingent edge: " + e
							+ ". The consistency check does not difference between ordinary and contingent edges.");
			}
		}

		// Init two useful structures
		g.getPropositions();

		// Start of well definition and properties about nodes (w.r.t. the Z node)!
		LabeledNode Z = g.getZ();
		if (Z == null) {
			Z = g.getNode(CSTN.ZeroNodeName);
			if (Z == null) {
				// We add by authority!
				Z = new LabeledNode(CSTN.ZeroNodeName);
				g.addVertex(Z);
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, "No " + ZeroNodeName + " node found: added!");
			}
			g.setZ(Z);
		}
		final SortedSet<LabeledNode> nodeSet = new ObjectRBTreeSet<>(g.getVertices());
		for (final LabeledNode node : nodeSet) {
			// Check that observation node has no in the proposition observed its label!
			final Literal obs = node.getPropositionObserved();
			final Label label = node.getLabel();
			if (obs != null) {
				if (label.contains(obs)) {
					if (CSTN.LOG.isLoggable(Level.WARNING))
						CSTN.LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label
								+ "' of the observation node '" + node.getName() + "'. Removed!");
				}
				label.remove(obs);
			}

			try {
				CSTN.checkWellDefinition2Property(g, node);
			} catch (final WellDefinitionException ex) {
				if (ex.getType() == Type.ObservationNodeDoesNotOccurBefore) {
					for (final Literal l1 : label.getAllAsStraight()) {
						final LabeledNode obsl1 = g.getObservator(l1);
						LabeledIntEdge e = g.findEdge(node, obsl1);
						if (e == null) {
							e = newEdgeInCSTN(node.getName() + "_" + obsl1.getName(), LabeledIntEdge.Type.derived, g.isOptimize(),
									g);
							g.addEdge(e, node, obsl1);
							if (CSTN.LOG.isLoggable(Level.WARNING)) {
								CSTN.LOG.log(Level.WARNING,
										"It is necessary to add a constraint to guarantee that node '" + node.getName()
												+ "' occurs after node '" + obsl1.getName()
												+ "' to satisfy WD2.");
							}
						}
						e.mergeLabeledValue(label, 0);
					}
				} else
					throw new IllegalArgumentException(ex.getMessage());
			}
		}

		if (!Z.getLabel().isEmpty()) {
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
			Z.setLabel(Label.emptyLabel);
		}
		// Now I assuring that each node has a edge to Z.
		for (final LabeledNode node : nodeSet) {
			if (node == Z) {
				continue;
			}
			LabeledIntEdge e = g.findEdge(node, Z);
			if (e == null) {
				e = newEdgeInCSTN(node.getName() + "_" + Z.getName(), LabeledIntEdge.Type.derived, g.isOptimize(), g);
				g.addEdge(e, node, Z);
				if (CSTN.LOG.isLoggable(Level.INFO)) {
					CSTN.LOG.log(Level.INFO, "It is necessary to add a constraint to guarantee that node '" + node.getName()
							+ "' occurs after node '" + Z.getName());// +
					// "' because Z must be the first node. Be careful, we operate with integer, it is
					// necessary to set -1 the distance!");
				}
			}
			e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
		}

		// It is better to normalize with respect to the label modification rules before starting the DC check.
		CSTNCheckStatus status = new CSTNCheckStatus();
		edgeSet = new ObjectRBTreeSet<>(g.getEdges());
		for (final LabeledIntEdge e : edgeSet) {
			final LabeledNode s = g.getSource(e);
			final LabeledNode d = g.getDest(e);

			// Normalize with respect to R0--R3
			if (s.isObsevator()) {
				CSTN.labelModificationR0(g, s, d, e, status, instantaneousReaction);
			}
			if (d.isObsevator()) {
				CSTN.labelModificationR2(g, d, s, e, status, instantaneousReaction);
			}
			CSTN.labelModificationR1(g, s, d, e, status, instantaneousReaction);
			CSTN.labelModificationR3(g, d, s, e, status, instantaneousReaction);
		}
		if (CSTN.LOG.isLoggable(Level.INFO))
			CSTN.LOG.log(Level.INFO, "A preliminary application of label modification rules has been done: " + status.toString());

		return true;
	}

	/**
	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args
	 *                an array of {@link java.lang.String} objects.
	 */
	public static void main(String[] args) {
		LOG.finest("Start...");
		CSTN cstn = new CSTN(true);

		if (!cstn.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTN.class.getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2015, Roberto Posenato");
			return;
		}

		LOG.finest("Loading graph...");
		LabeledIntGraph g = LabeledIntGraph.load(cstn.fInput);
		if (g == null) {
			System.out.println("It was not possible to load the given CSTN");
			return;
		}
		LOG.finest("LabeledIntGraph loaded!");

		LOG.finest("DC Checking...");
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck(g, true, false);
		} catch (WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		LOG.finest("LabeledIntGraph minimized!");
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given cstn is Dynamic consistent!");
			} else {
				System.out.println("The given cstn is Dynamic consistent!");
			}
			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}

		if (cstn.fOutput != null) {
			GraphMLWriter<LabeledNode, LabeledIntEdge> graphWriter = new it.univr.di.cstnu.graph.GraphMLWriter(new StaticLayout<>(g));
			try {
				graphWriter.save(g, new PrintWriter(cstn.output));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Executes one step of the dynamic consistency check: for each possible triangle of the network, label propagation rule is applied and, on the
	 * resulting edge, all other rules R0--R3 are also applied.
	 *
	 * @param currentGraph
	 *                the current graph. At the end of the procedure, it will contain the results of reductions.
	 * @param instantaneousReaction
	 *                true is it is admitted that observation points and other points depending from the observed proposition can be executed in
	 * @param excludeR1R2
	 *                true if one wants to exclude the application of rules R1 and R2. NOT YET APPLIED the same 'instant'.
	 * @param status
	 *                the record where to store statistics and exit status of the execution. BE CAREFULL, this procedure cannot verified if the DC is
	 *                finished or not. So, the status.finished field is not update by this procedure.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException
	 *                 if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then there is a
	 *                 problem in the rules coding.
	 */
	public static CSTNCheckStatus oneStepDynamicConsistency(final LabeledIntGraph currentGraph, final boolean instantaneousReaction,
			final boolean excludeR1R2,
			final CSTNCheckStatus status) throws WellDefinitionException {

		/*
		 * Label Propagation Apply label propagation rule to all possible node triple (A,B,C). Exploiting a possible sparse graph, the triple are
		 * generated making, for each node B, two cycles: one to find all possible predecessors of B, each called A, and one to find all successors of
		 * B, each called C. In this way, the triple is used as: A --> B --> C
		 */
		LabeledNode A, B, C;
		LabeledIntEdge AB, BC, AC;

		LabeledNode[] node = currentGraph.getVerticesArray();
		LabeledNode Z = currentGraph.getZ();

		status.cycles++;

		if (CSTN.LOG.isLoggable(Level.FINER)) {
			CSTN.LOG.log(Level.FINER, "");
			CSTN.LOG.log(Level.FINER, "Start application labeled propagation rule+R0+R3.");
		}
		final int n = currentGraph.getVertexCount();
		for (int k = 0; k < n; k++) {
			B = node[k];
			for (final Iterator<LabeledIntEdge> eABIter = currentGraph.getInEdges(B).iterator(); eABIter.hasNext();) {
				AB = eABIter.next();
				A = currentGraph.getSource(AB);
				for (final Iterator<LabeledIntEdge> eBCIter = currentGraph.getOutEdges(B).iterator(); eBCIter.hasNext();) {
					BC = eBCIter.next();
					C = currentGraph.getDest(BC);
					if (C == B)
						continue;// self loop on the second pair in not useful. The only loop that has to be maintain is A == C
					// Now it is possible to propagate the labels with the standard rules
					CSTN.labelPropagationRule(currentGraph, A, B, C, AB, BC, Z, status);
					if (!status.consistency) {
						return status;
					}
					AC = currentGraph.findEdge(A, C);
					if (AC == null)
						continue;

					if (A.isObsevator()) {
						// R0 on the resulting new values
						CSTN.labelModificationR0(currentGraph, A, C, AC, status, instantaneousReaction);
					}

					if (!excludeR1R2 && C.isObsevator()) {
						// R2 on the resulting new values.
						CSTN.labelModificationR2(currentGraph, C, A, AC, status, instantaneousReaction);
					}

					// R3 on the resulting new values
					CSTN.labelModificationR3(currentGraph, C, A, AC, status, instantaneousReaction);

					if (A.isObsevator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						CSTN.labelModificationR0(currentGraph, A, C, AC, status, instantaneousReaction);
					}

					if (!excludeR1R2) {
						// R1 on the resulting new values.
						CSTN.labelModificationR1(currentGraph, A, C, AC, status, instantaneousReaction);
						if (C.isObsevator())
							CSTN.labelModificationR2(currentGraph, C, A, AC, status, instantaneousReaction);// It should be like R0!
																	// To verify
																	// experimentally.
					}
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3."
					+ "\nSituation after the labeled propagation rule+R0+R3.");
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "\n");
		return status;
	}

	/**
	 * Checks whether the new label to add or to use for replacing another one subsumes the conjunction of labels of a constraint's endpoints.
	 *
	 * @param source
	 *                starting node of the constraint
	 * @param dest
	 *                ending node of the constraint
	 * @param newLabel
	 *                new label to add or to use
	 * @param edgeName
	 *                name of constraint
	 * @param ruleName
	 *                name of rule applied to determine the new label.
	 * @return false if the check fails, true otherwise
	 */
	static boolean checkNodeLabelsSubsumption(final LabeledNode source, final LabeledNode dest, final Label newLabel, final String edgeName,
			final String ruleName) {
		if ((source == null) || (dest == null) || (newLabel == null) || (edgeName == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "One parameter is null. source: " + source + ", dest: " + dest + ", new label to add: " + newLabel
						+ ", edgeName: "
						+ edgeName + ". Please, check parameter.");
			}
			return false;
		}
		final Label labelConjunction = source.getLabel().conjunction(dest.getLabel());
		if (!newLabel.subsumes(labelConjunction)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "Subsumption check for a label generated by rule " + ruleName + " on edge " + edgeName
						+ ".\nThe new label, '" + newLabel + "', does not subsume the conjunction of node labels '" + labelConjunction
						+ "'.\nIt is rejected!");
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the first well definition property:<br>
	 * <blockquote>Any labeled valued of the edge is consistent and subsumes both labels of two endpoints.</blockquote>
	 *
	 * @param e
	 *                edge representing a labeled constraint.
	 * @param tail
	 *                the source node of the edge.
	 * @param head
	 *                the destination node of the edge.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition1Property(final LabeledNode tail, final LabeledNode head, final LabeledIntEdge e) throws WellDefinitionException {
		if ((e == null) || (tail == null) || (head == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
			}
			return false;
		}

		final Label labelConjunction = tail.getLabel().conjunction(head.getLabel());
		if (labelConjunction == null) {
			final String msg = "The two endpoints don't allow any constraint because they have inconsistent labels.";
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, msg);
			throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
		}
		// check the ordinary labeled values
		for (final Object2IntMap.Entry<Label> entry : e.getLabeledValueMap().entrySet()) {
			if (!entry.getKey().subsumes(labelConjunction)) {
				final String msg = "Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the upper case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				final String msg = "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the lower case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getLowerLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				final String msg = "Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		return true;
	}

	/**
	 * Checks whether the label of a node satisfies the second well definition property:<br>
	 * <blockquote>For each literal present in a node label label:
	 * <ol>
	 * <li>the label of the observation node of the considered literal is subsumed by the label of the current node.
	 * <li>the observation node is constrained to occur before the current node.
	 * </ol>
	 * </blockquote>
	 *
	 * @param g
	 *                the current graph containing the node.
	 * @param node
	 *                the current node to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition2Property(final LabeledIntGraph g, final LabeledNode node) throws WellDefinitionException {
		if ((g == null) || (node == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
			return false;
		}

		final Label nodeLabel = node.getLabel();
		if (nodeLabel.isEmpty())
			return true;

		// check the observation node
		for (final Literal l : nodeLabel.getAllAsStraight()) {
			final LabeledNode obs = g.getObservator(l);
			if (obs == null) {
				final String msg = "Observation node of literal " + l + " of node " + node + " does not exist.";
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
			}

			final Label obsLabel = obs.getLabel();
			if (!nodeLabel.subsumes(obsLabel)) {
				final String msg = "Label of node " + node + " does not subsume label of obs node " + obs;
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}

			final LabeledIntEdge e = g.findEdge(node, obs);
			if ((e == null) || (e.getMinValue() == LabeledIntNodeSetMap.INT_NULL) || (e.getMinValue() > 0)) {
				final String msg = "There is no constraint to execute obs node " + obs + " before node " + node;
				if (CSTN.LOG.isLoggable(Level.WARNING))
					CSTN.LOG.log(Level.WARNING, msg);
				throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotOccurBefore);
			}
		}
		return true;
	}

	/**
	 * Checks whether each labeled value of an edge 'e' satisfies the third well definition property:<br>
	 * <blockquote>For each literal present in any label of 'e', the label of the observation node of the considered literal is subsumed by the label of the
	 * edge.</blockquote>
	 *
	 * @param g
	 *                the current graph containing the node.
	 * @param e
	 *                the current edge to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition3Property(final LabeledIntGraph g, final LabeledIntEdge e) throws WellDefinitionException {
		if ((g == null) || (e == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
			return false;
		}

		final Set<Object2IntMap.Entry<Entry<Label, String>>> allLabeledValuesSet = e.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		allLabeledValuesSet.addAll(e.getLowerLabelSet());
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : allLabeledValuesSet) {
			final Label edgeLabel = entry.getKey().getKey();
			if (edgeLabel.isEmpty())
				continue;

			// check the observation node
			for (final Literal l : edgeLabel.getAllAsStraight()) {
				final LabeledNode obs = g.getObservator(l);
				if (obs == null) {
					final String msg = "Observation node of literal " + l + " present in label " + edgeLabel + " of edge " + e
							+ " does not exist.";
					if (CSTN.LOG.isLoggable(Level.WARNING))
						CSTN.LOG.log(Level.WARNING, msg);
					throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
				}

				final Label obsLabel = obs.getLabel();
				if (!edgeLabel.subsumes(obsLabel)) {
					final String msg = "Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs;
					if (CSTN.LOG.isLoggable(Level.WARNING))
						CSTN.LOG.log(Level.WARNING, msg);
					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
				}
			}
		}
		return true;
	}

	/**
	 * @param newLabel
	 * @param value
	 * @param newEdge
	 * @return true if the value associated to newLabel is NOT a negative infinity OR newLabel does contain unknown literals.
	 */
	static boolean isNewLabeledValueNotANegativeLoopEspression(final Label newLabel, final int value, final LabeledIntEdge newEdge) {
		if ((value == Constants.INT_NEG_INFINITE) && !newLabel.containsUnknown()) {
			if (CSTN.LOG.isLoggable(Level.FINER))
				CSTN.LOG.log(Level.FINER, "Found a negative loop that has determined a -∞ in the edge " + newEdge);
			return false;
		}
		return true;
	}

	/**
	 * Applies rule R0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 *
	 * <pre>
	 * R0:
	 * P? --[α p, w]--&gt; X changes in P? --[α', w]--&gt; X when w &lt;0 (w&le;0 for instantaneous reaction)
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?.
	 * α is a label
	 * α' is α without 'p' and P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param P
	 *                the observation node
	 * @param X
	 *                the other node
	 * @param PX
	 *                the edge connecting P? ---&gt; X
	 * @param status
	 * @param instantaneousReaction
	 *                true is it is admitted that observation points and other points depending from the observed proposition can be executed in the same
	 *                'instant'.
	 * @return true if the rule has been applied one time at least.
	 */
	static boolean labelModificationR0(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledIntEdge PX,
			final CSTNCheckStatus status, final boolean instantaneousReaction) {
		boolean ruleApplied = false, mergeStatus;
		final Literal p = P.getPropositionObserved();
		if (p == null) {
			if (CSTN.LOG.isLoggable(Level.FINER))
				CSTN.LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
			return false;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R0: start.");
		final ObjectArraySet<Label> obsXLabelSet = new ObjectArraySet<>(PX.getLabeledValueMap().keys());// It is necessary to have a copy of current
														// labels
														// for updating them without problems.
		for (final Label l : obsXLabelSet) {
			final int w = PX.getValue(l);
			if (w == LabeledIntNodeSetMap.INT_NULL)
				continue;// it is possible that in a previous cycle the label 'l' has been removed.
			if ((w > 0) || (instantaneousReaction && (w == 0)))
				continue;
			if (l.getLiteralWithSameName(p) == null)
				continue;

			final Label alphaPrime = new Label(l);
			alphaPrime.removeAllLiteralsWithSameName(p);
			alphaPrime.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(P));

			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (CSTN.LOG.isLoggable(Level.FINER)) {
				logMessage = "R0 simplifies a label of edge " + PX.getName()
						+ ":\nsource: " + P.getName() + " ---(" + l + ", " + Constants.formatInt(w) + ")---> " + X.getName()
						+ "\nresult: " + P.getName() + " ---(" + alphaPrime + ", " + Constants.formatInt(w) + ")---> " + X.getName();
			}

			PX.putLabeledValueToRemovedList(l, w);
			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
			status.r0calls++;
			ruleApplied = true;
			mergeStatus = PX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus && CSTN.LOG.isLoggable(Level.FINER))
				CSTN.LOG.log(Level.FINER, logMessage);
			if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(alphaPrime, w, PX))) {
				if (CSTN.LOG.isLoggable(Level.FINEST))
					CSTN.LOG.log(Level.FINEST, "Found an inconsistency. Label Modification R0: end.");
				status.finished = true;
				return ruleApplied;
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R0: end.");

		return ruleApplied;
	}

	/**
	 * Rule R1 adds a simplified labeled value for each labeled value of edge X--&gt;Y that cannot be evaluated when X--&gt;Y has to be considered.
	 *
	 * <pre>
	 * if P? --[ab, w]--&gt; X --[bgp, v]--&gt; Y  and w&le;0 and v&lt;-w (v&le;-w for instantaneous reaction),
	 * then the constraint between X and Y is modified as X --[abg', v]--[bgp, v]--&gt; Y
	 * where g' is g without p and the P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param nX
	 *                x node
	 * @param nY
	 *                y node
	 * @param eXY
	 *                LabeledIntEdge containing the constrain to modify
	 * @param status
	 * @param instantaneousReaction
	 * @return true if a rule has been applied.
	 */
	static boolean labelModificationR1(final LabeledIntGraph currentGraph, final LabeledNode nX, final LabeledNode nY, final LabeledIntEdge eXY,
			final CSTNCheckStatus status, final boolean instantaneousReaction) {

		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R1: start.");
		boolean ruleApplied = false;
		final LabeledNode Z = currentGraph.getZ();

		final ObjectArraySet<LabeledIntEdge> edgeFromObsSet = CSTN.getEdgeFromObservators(currentGraph, nX);
		if (edgeFromObsSet == null)
			return false;

		final ObjectArraySet<Label> XYLabelSet = new ObjectArraySet<>(eXY.getLabeledValueMap().keys());// It is necessary to have a copy of current
														// labels for
														// updating them without problems.

		for (LabeledIntEdge eObsX : edgeFromObsSet) {
			LabeledNode nObs = currentGraph.getSource(eObsX);

			for (Object2IntMap.Entry<Label> entryObsX : eObsX.labeledValueSet()) {
				final int w = entryObsX.getIntValue();
				if (w > 0)
					continue; // R1 work with negative w.

				final Label obsXLabel = entryObsX.getKey();
				final Literal p = nObs.getPropositionObserved();

				for (final Label XYLabel : XYLabelSet) {
					if (!XYLabel.contains(p))
						continue;
					final int v = eXY.getValue(XYLabel);
					if (v == LabeledIntNodeSetMap.INT_NULL)
						continue;// it is possible that in a previous cycle the label 'labelXY' has been removed.

					// Condition on v value and its relation with w.
					// w is surely <=0; v can be any value. R1 has to applied when v<=-w (in case of instantaneous, v<-w)...
					if ((v > -w) || (instantaneousReaction && (v == -w)))
						continue; // R1 cannot be applied

					if ((nY != Z) && (!obsXLabel.isConsistentWith(XYLabel) || obsXLabel.containsUnknown()))
						continue; // ... and when it is consistent with
								// label P?-->X (if Y is Z, the rule can
								// be applied using
								// ¿ literals!)
					final Label abg1 = CSTN.makeAlphaBetaGammaPrime(currentGraph, nObs, p, obsXLabel, XYLabel, nY != Z);
					if (CSTN.LOG.isLoggable(Level.FINEST))
						CSTN.LOG.log(Level.FINEST, "Rule R1 details alphaBetaGamma1=" + abg1);

					if (!CSTN.checkNodeLabelsSubsumption(nX, nY, abg1, eXY.getName(), "R1")) {
						// I check if the label subsumes the label of the endpoints before to proceed.
						// It should not necessary, but I put here as a guard!
						if (CSTN.LOG.isLoggable(Level.FINER))
							CSTN.LOG.log(Level.FINER, "Detail about the error of application R1 to edge " + eXY
									+ ":\nsource: "
									+ nObs.getName() + " ---(" + obsXLabel + ", " + Constants.formatInt(w) + ")---> "
									+ nX.getName() + " ---(" + XYLabel + ", "
									+ Constants.formatInt(v) + ")---> " + nY.getName()
									+ "\nresult: " + nX.getName() + " ---(" + abg1 + ", " + Constants.formatInt(v)
									+ ")---> " + nY.getName());
						throw new IllegalStateException("Rule R1 cannot determine label that does not subsume node labels!");
					}

					eXY.putLabeledValueToRemovedList(XYLabel, v);
					ruleApplied = eXY.mergeLabeledValue(abg1, v);
					if (ruleApplied) {
						if (CSTN.LOG.isLoggable(Level.FINER))
							CSTN.LOG.log(Level.FINER, "R1 adds a label to edge " + eXY
									+ ":\nsource: "
									+ nObs.getName() + " ---(" + obsXLabel + ", " + Constants.formatInt(w) + ")---> "
									+ nX.getName() + " ---(" + XYLabel + ", "
									+ Constants.formatInt(v) + ")---> "
									+ nY.getName()
									+ "\nresult: add " + nX.getName() + " ---(" + abg1 + ", " + Constants.formatInt(v)
									+ ")---> " + nY.getName());
						status.r1calls++;
					}
					if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(abg1, v, eXY)))
						return ruleApplied;
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R1: end.");
		return ruleApplied;
	}

	/**
	 * In presence of a label containing a proposition that can be decided only in the future, Rule 2 adds a new label one without the preposition and with
	 * a suitable value. In more details,
	 * 
	 * <pre>
	 * R2:
	 * P? &lt;--[α p,w]-- X  changes in P? &lt;--[α',max{w,0}]-- X
	 * where:
	 * p can be the positive o the negative literal associated to proposition observed in P?.
	 * α is a label
	 * α' is α without p and P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param P
	 * @param X
	 * @param XP
	 * @param status
	 * @param instantaneousReaction
	 *                true is it is admitted that observation points and other points depending from the observed proposition can be executed in the same
	 *                'instant'.
	 * @return true if the rule has been applied one time at least.
	 */
	static boolean labelModificationR2(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledIntEdge XP,
			final CSTNCheckStatus status, final boolean instantaneousReaction) {
		boolean ruleApplied = false;
		final Literal p = P.getPropositionObserved();
		if (p == null) {
			if (CSTN.LOG.isLoggable(Level.FINE))
				CSTN.LOG.log(Level.FINE, "Method labelModificationR2 called passing a non observation node as first parameter!");
			return false;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R2: start.");
		final ObjectArraySet<Label> XPLabelSet = new ObjectArraySet<>(XP.getLabeledValueMap().keys());// It is necessary to have a copy of current
														// labels
		// for updating them without problems.
		for (final Label l : XPLabelSet) {
			final int w = XP.getValue(l);
			if (w == LabeledIntNodeSetMap.INT_NULL)
				continue;// it is possible that in a previous cycle the label 'l' has been removed.
			final int max = Math.max(w, 0);
			if (l.getLiteralWithSameName(p) == null)
				continue;

			final Label alphaPrime = new Label(l);
			alphaPrime.removeAllLiteralsWithSameName(p);
			alphaPrime.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(P));

			if (!CSTN.checkNodeLabelsSubsumption(X, P, alphaPrime, XP.getName(), "R2")) {
				if (w > 0) {
					// R2 rule
					// It means that 'X' label contains 'p'!
					// The labeled value has to be substituted by a 0 labeled value because by WD2 the node has to be after the observation
					// node.
					if (CSTN.LOG.isLoggable(Level.FINER))
						CSTN.LOG.log(Level.FINER,
								"Since after R2, the label does not subsumes the conjection of node labes, it means that "
										+ X.getName()
										+ " has 'p' in its label. It has to be after " + P.getName() + ". Edge: " + XP
										+ ":\nsource: " + P + "<--(" + l + ", " + Constants.formatInt(w) + ")--" + X
										+ "\nresult: " + P.getName() + " <---(" + X.getLabel().conjunction(P.getLabel())
										+ ", 0)--- " + X.getName());
					XP.mergeLabeledValue(X.getLabel().conjunction(P.getLabel()), 0);
				} else {
					if (CSTN.LOG.isLoggable(Level.FINER))
						CSTN.LOG.log(Level.FINER, "R2 CANNOT be applied because subsumption check of R2 applied to edge " + XP
								+ ":\nsource: " + P + " <---(" + l + ", " + Constants.formatInt(w) + ")--- " + X);
				}
				continue;
			}

			// Prepare the log message now with old values of the edge. If it modifies, then we can log it correctly.
			final String logMessage = "R2 simplifies a label of edge " + XP
					+ ":\nsource: " + P.getName() + " <--(" + l + ", " + Constants.formatInt(w) + ")--- " + X.getName()
					+ "\nresult: " + P.getName() + " <--(" + alphaPrime + ", " + Constants.formatInt(max) + ")--- " + X.getName();

			XP.putLabeledValueToRemovedList(l, w);
			status.r2calls++;
			ruleApplied = true;
			if (XP.mergeLabeledValue(alphaPrime, max) && CSTN.LOG.isLoggable(Level.FINER))
				CSTN.LOG.log(Level.FINER, logMessage);
			if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(alphaPrime, max, XP)))
				return ruleApplied;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R2: end.");
		return ruleApplied;
	}

	/**
	 * Rule R3 applies the following modification of labels:
	 * 
	 * <pre>
	 * if P? --[ab, w]--&gt; X &lt;--[bgp, v]-- Y  and w &le;0
	 * then the constraint between Y and X is modified adding the following label:
	 * X &lt;--[abg', max{w,v}]-- Y	 * 
	 * where g' is g without p and the children of P?.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param nX
	 *                x node
	 * @param nY
	 *                y node
	 * @param eYX
	 *                LabeledIntEdge containing the constrain to modify
	 * @param status
	 * @param instantaneousReaction
	 * @return true if a rule has been applied.
	 */
	static boolean labelModificationR3(final LabeledIntGraph currentGraph, final LabeledNode nX, final LabeledNode nY,
			final LabeledIntEdge eYX, final CSTNCheckStatus status, final boolean instantaneousReaction) {

		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R3: start.");

		boolean ruleApplied = false;
		final LabeledNode Z = currentGraph.getZ();

		final ObjectArraySet<LabeledIntEdge> edgeFromObsSet = CSTN.getEdgeFromObservators(currentGraph, nX);
		if (edgeFromObsSet == null)
			return false;

		final ObjectArraySet<Label> YXLabelSet = new ObjectArraySet<>(eYX.getLabeledValueMap().keys());// It is necessary to have a copy of current
														// labels for
														// updating them without problems.
		for (LabeledIntEdge eObsX : edgeFromObsSet) {
			LabeledNode nObs = currentGraph.getSource(eObsX);

			for (Object2IntMap.Entry<Label> entryObsX : eObsX.labeledValueSet()) {
				final int w = entryObsX.getIntValue();
				if (w > 0)
					continue; // R3 work with negative w.

				final Label labelObsX = entryObsX.getKey();
				final Literal p = nObs.getPropositionObserved();

				for (final Label YXLabel : YXLabelSet) {
					if (!YXLabel.contains(p))
						continue;

					final int v = eYX.getValue(YXLabel);
					if (v == LabeledIntNodeSetMap.INT_NULL)
						continue;// it is possible that in a previous cycle the label 'labelYX' has been removed.

					if ((nX != Z) && !labelObsX.isConsistentWith(YXLabel))
						continue;

					final int max = Math.max(w, v);

					Label abg1 = CSTN.makeAlphaBetaGammaPrime(currentGraph, nObs, p, labelObsX, YXLabel, nX != Z);
					// If Z is involved, then it is necessary to remove also all children of q-literals! (Check TIME 2015 paper)
					if (nX == Z) {
						for (Literal unknow : abg1.getAllUnknown()) {
							abg1.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(currentGraph.getObservator(unknow)));
						}
					}
					if (CSTN.LOG.isLoggable(Level.FINEST))
						CSTN.LOG.log(Level.FINEST, "Rule R3 details alphaBetaGamma1=" + abg1);

					eYX.putLabeledValueToRemovedList(YXLabel, v);
					ruleApplied = eYX.mergeLabeledValue(abg1, max);
					if (ruleApplied) {
						if (CSTN.LOG.isLoggable(Level.FINER))
							CSTN.LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eYX.getName() + ":\n"
									+ "source: " + nObs.getName() + " ---(" + labelObsX + ", " + Constants.formatInt(w)
									+ ")---> " + nX.getName()
									+ " <---(" + YXLabel + ", " + Constants.formatInt(v) + ")--- " + nY.getName()
									+ "\nresult: add " + nX.getName() + " <---(" + abg1 + ", " + Constants.formatInt(max)
									+ ")--- " + nY.getName());
						status.r3calls++;
					}
					if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(abg1, max, eYX))) {
						if (CSTN.LOG.isLoggable(Level.FINEST))
							CSTN.LOG.log(Level.FINEST, "Found an inconsistency.\nLabel Modification R3: end.");
						status.finished = true;
						return ruleApplied;
					}
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "Label Modification R3: end.");
		return ruleApplied;
	}

	/**
	 * Applies the labeled propagation rule:
	 * 
	 * <pre>
	 * if A --[l1, x]--&gt; B --[l2, y]--&gt; C, then A --[l1l2, x+y]--&gt; C
	 * where l1l2 is the extended conjunction when A is the Z node, otherwise is the standard conjunction.
	 * In case l1l2 contains 'unknown literals' (¿p for example), then l1l2 has to not contain any children of such unknown literals.
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param A
	 * @param B
	 * @param C
	 * @param AB
	 * @param BC
	 * @param Z
	 *                the Z node in currentGraph (only to speed-up)
	 * @param status
	 * @return true if a reduction has been applied.
	 */
	static boolean labelPropagationRule(final LabeledIntGraph currentGraph, final LabeledNode A, final LabeledNode B, final LabeledNode C,
			final LabeledIntEdge AB, final LabeledIntEdge BC, final LabeledNode Z, final CSTNCheckStatus status) {

		LabeledIntEdge AC = currentGraph.findEdge(A, C);
		if (AC == null) {
			AC = newEdgeInCSTN(A.getName() + "_" + C.getName(), LabeledIntEdge.Type.derived, currentGraph.isOptimize(), currentGraph);
			currentGraph.addEdge(AC, A, C);
		}

		boolean ruleApplied = false;
		for (final Object2IntMap.Entry<Label> ABEntry : AB.labeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			final int x = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : BC.labeledValueSet()) {
				final Label labelBC = BCEntry.getKey();
				final int y = BCEntry.getIntValue();
				final SortedSet<String> nodeSetBC = BC.getNodeSet(labelBC);
				int sum = LabeledIntNodeSetTreeMap.sumWithOverflowCheck(x, y);

				final boolean isNegativePathToZ = (C == Z) && (y < 0) && (x < 0);
				Label newLabelAC = (isNegativePathToZ && x < 0) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
				if (newLabelAC == null)
					continue;

				SortedSet<String> sigma = null;
				int oldZ = AC.getValue(newLabelAC);
				if (oldZ != Constants.INT_NULL && sum > oldZ)
					continue;

				if (isNegativePathToZ) {
					// It is necessary to remove all children of unknown literals (TIME15)
					for (Literal unknownLit : newLabelAC.getAllUnknown()) {
						newLabelAC.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(currentGraph.getObservator(unknownLit)));
					}
					// newLabel can be changed!
					oldZ = AC.getValue(newLabelAC);
					if (oldZ != Constants.INT_NULL && sum > oldZ)
						continue;

					/*
					 * Special case A --[beta, x]--> B --[alpha, y, sigma1]--> Z with x<0 and y<0 results to be A --[alpha beta, sum,
					 * sigma2]--> Z where sum = x+y and sigma2 = sigma1 ∪ A, if A is not in sigma1, otherwise sum = -infinity and sigma2 =
					 * null. If AC contains [alpha beta, w, sigma], then w is overwritten if w> sum and, in any case, sigma = sigma +
					 * sigma2.
					 */
					final boolean isAInNodeSet = (nodeSetBC == null) ? false : nodeSetBC.contains(A.getName());
					if (isAInNodeSet) {
						sum = Constants.INT_NEG_INFINITE;
						if (CSTN.LOG.isLoggable(Level.FINEST))
							CSTN.LOG.log(Level.FINEST, "Negative qLoop detected during the updated of " + AC.getName() + ": "
							// + "source: " + A.getName() + " --(" + labelAB + ", " + Constants.formatInt(x) + ")--> " + B.getName()
							// + " --(" + labelBC
							// + ", "
							// + Constants.formatInt(y) + ")--> " + C.getName()
									+ A.getName() + " is in the node set of label '" + labelBC + "' of the edge "
									+ BC.getName()
									+ ". New labeled value = (" + newLabelAC + ", -" + Constants.INFINITY_SYMBOLstring
									+ ").");
					}
					if (sum != Constants.INT_NEG_INFINITE) {
						// when z == Constants.INT_NEG_INFINITE, the value can propagate without node set.
						sigma = ValueNodeSetPair.newSetInstance();
						sigma.add(A.getName());
						if (nodeSetBC != null) {
							sigma.addAll(nodeSetBC);
						} else {
							sigma.add(B.getName());
						}
						// I add only B because the endpoints are specified by the edge.
						if (CSTN.LOG.isLoggable(Level.FINEST)) {
							CSTN.LOG.log(Level.FINEST, "The node set of edge BC is " + nodeSetBC);
							CSTN.LOG.log(Level.FINEST, "The new CANDIDATE node set to add to label '" + newLabelAC + "' on edge "
									+ AC.getName()
									+ " is " + sigma);
						}
					}
				}

				// I have to prepare the log before the execution of the merge!
				String log = null;
				if (CSTN.LOG.isLoggable(Level.FINER)) {
					log = "Label Propagation Rule applied to edge " + AC.getName()
							+ ":\nsource: " + A.getName() + " --(" + labelAB + ", " + Constants.formatInt(x) + ")--> " + B.getName()
							+ " --(" + labelBC + ", " + Constants.formatInt(y)
							+ ((nodeSetBC != null && !nodeSetBC.isEmpty()) ? ", " + nodeSetBC.toString() : "")
							+ ")--> " + C.getName()
							+ "\nresult: " + A.getName() + " --(" + newLabelAC + ", "
							+ ((oldZ == LabeledIntNodeSetMap.INT_NULL || sum < oldZ) ? Constants.formatInt(sum)
									: Constants.formatInt(oldZ))// it could be
					// add only the node set
							+ ((sigma != null && !sigma.isEmpty()) ? ", " + sigma.toString() : "")
							+ ")--> " + C.getName() + "; old value: "
							+ Constants.formatInt(oldZ) + ", " + AC.getNodeSet(newLabelAC);
				}

				if (AC.mergeLabeledValue(newLabelAC, sum, sigma)) {
					ruleApplied = true;
					status.labeledValuePropagationcalls++;
					CSTN.LOG.log(Level.FINER, log);
				}
				if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(newLabelAC, sum, AC))) {
					status.finished = true;
					return ruleApplied;
				}
			}
		}
		if (A == C) { // self loop, check if there is a negative loop!
			final int min = AC.getMinValueAmongLabelsWOUnknown();
			if ((min != LabeledIntNodeSetMap.INT_NULL) && (min < 0)) {
				if (CSTN.LOG.isLoggable(Level.FINER))
					CSTN.LOG.log(Level.FINER, "Found a negative loop in node " + AC.toString());
				status.finished = true;
				status.consistency = false;
			}
		}
		if (AC.labeledValueSet().isEmpty())
			currentGraph.removeEdge(AC);
		return ruleApplied;
	}

	/**
	 * Simple method to determine the label to add in rules R1 and R3, cleaned by the proposition observed and by all children of the observation node
	 * 'observator'.<br>
	 * In case that the given labels are inconsistent, if the 'strict' parameter is true, the method returns null, otherwise it returns a label in which
	 * opposite literals are represented as 'unknown' literals.
	 *
	 * @param currentGraph
	 *                the current graph
	 * @param observator
	 *                observator node
	 * @param observed
	 *                the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is
	 *                just for speeding up.
	 * @param labelFromObs
	 *                label of the edge from observator
	 * @param labelToClean
	 *                label to modify
	 * @param strict
	 * @return alphaBetaGamma1 already completed.
	 */
	static Label makeAlphaBetaGammaPrime(final LabeledIntGraph currentGraph, final LabeledNode observator, final Literal observed, final Label labelFromObs,
			final Label labelToClean,
			final boolean strict) {

		Label sourceLabel = new Label(labelToClean);
		sourceLabel.removeAllLiteralsWithSameName(observed);
		/*
		 * Label[0] is the sub-label of labelFromObs not in common with sourceLabel.<br> Label[1] is the sub-label of labelFromObs in common with
		 * sourceLabel.<br> Label[2] is the sub-label of labelFromObs not in common with sourceLabel. <br>
		 */
		final Label alpha = labelFromObs.getSubLabelIn(sourceLabel, false, strict);
		final Label beta = labelFromObs.getSubLabelIn(sourceLabel, true, strict);
		Label gamma1 = sourceLabel.getSubLabelIn(labelFromObs, false, strict);

		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST,
					"labelEdgeFromObs = " + labelFromObs + ", labelWithoutP=" + sourceLabel + ",  alpha = " + alpha + ", beta=" + beta
							+ " gamma=" + gamma1);

		gamma1.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(observator));
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "gamma1=" + gamma1);
		if (strict) {
			Label ab = alpha.conjunction(beta);
			if (ab != null)
				return ab.conjunction(gamma1);
			return null;
		}
		return alpha.conjunctionExtended(beta).conjunctionExtended(gamma1);
	}

	/**
	 * Determine the set of edges P?-->nX where P? is an observator node and nX is the given node.
	 * 
	 * @param currentGraph
	 * @param nX
	 *                the given node.
	 * @return the set of edges P?-->nX, null if nX is empty o there is no observator.
	 */
	private static ObjectArraySet<LabeledIntEdge> getEdgeFromObservators(final LabeledIntGraph currentGraph, final LabeledNode nX) {
		int numObs;
		if (currentGraph == null || nX == null || (numObs = currentGraph.getObservators().size()) == 0)
			return null;

		ObjectArraySet<LabeledIntEdge> fromObs = new ObjectArraySet<>();

		if (currentGraph.getIncidentEdges(nX).size() < numObs) {
			for (LabeledIntEdge e : currentGraph.getIncidentEdges(nX)) {
				if (currentGraph.getSource(e).isObsevator())
					fromObs.add(e);
			}
		} else {
			LabeledIntEdge e;
			for (LabeledNode n : currentGraph.getObservators()) {
				if ((e = currentGraph.findEdge(n, nX)) != null) {
					fromObs.add(e);
				}
			}
		}
		if (fromObs.isEmpty())
			return null;
		return fromObs;
	}

	/**
	 * Flag to activate optimization of labeled values.
	 */
	boolean labelOptimization = true;

	/**
	 * The input file containing the CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "input file. Input file has to be a CSTN graph in GraphML format.", metaVar = "CSTNU_file_name")
	private File fInput;

	/**
	 * Output file where to write the XML representing the minimal CSTN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "CSTN_file_name")
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
	 * Constructor for CSTN.
	 *
	 * @param withOptimization
	 *                true if the propagation of labeled value has also to remove redundant labeled values.
	 */
	public CSTN(final boolean withOptimization) {
		this.labelOptimization = withOptimization;
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance and, if the instance is consistent, determines all the minimal ranges for the constraints. <br>
	 * All label containing proposition that cannot be evaluated at run time are removed.<br>
	 * This method tries to execute only LP with R0 and R3 for at most |P| cycles.
	 *
	 * @param g
	 *                the original graph that has to be checked. If the check is successful, g is modified and it contains all minimized constraints;
	 *                otherwise, it is not modified.
	 * @param instantaneousReactions
	 *                true is it is admitted that observation points and other points depending from the observed proposition can be executed in the same
	 *                'instant'.
	 * @param excludeR1R2
	 *                true if one wants to exclude the application of rules R1 and R2.
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.WellDefinitionException
	 *                 if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then there is a
	 *                 problem in the rules coding.
	 */
	public CSTNCheckStatus dynamicConsistencyCheck(final LabeledIntGraph g, final boolean instantaneousReactions, boolean excludeR1R2)
			throws WellDefinitionException {

		final CSTNCheckStatus status = new CSTNCheckStatus();
		if (g == null)
			return status;

		String originalName = g.getName();
		LabeledIntGraph currentGraph, nextGraph;
		currentGraph = new LabeledIntGraph(g, this.labelOptimization);
		currentGraph.setName("Current graph");
		try {
			CSTN.initAndCheck(currentGraph, instantaneousReactions);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		nextGraph = new LabeledIntGraph(currentGraph, this.labelOptimization);
		nextGraph.setName("New graph");

		final int propositionN = currentGraph.getPropositions().size();
		// TODO: trovare il numero giusto di iterazioni
		final int maxCycles = propositionN * 10;
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "The maximum number of possible cycles is " + maxCycles);

		int i;

		long startTime = System.nanoTime();
		for (i = 1; (i <= maxCycles) && status.consistency && !status.finished; i++) {
			if (CSTN.LOG.isLoggable(Level.FINE))
				CSTN.LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
			CSTN.oneStepDynamicConsistency(nextGraph, instantaneousReactions, excludeR1R2, status);
			status.finished = currentGraph.hasSameEdgesOf(nextGraph);
			if (status.consistency && !status.finished) {
				currentGraph.copy(nextGraph);
				nextGraph.setName("nextGraph");
				currentGraph.setName("currentGraph");
			}
			if (CSTN.LOG.isLoggable(Level.FINE))
				CSTN.LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
		}
		status.executionTime = (System.nanoTime() - startTime) / 1000000;

		if (!status.consistency) {
			if (CSTN.LOG.isLoggable(Level.INFO)) {
				CSTN.LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an inconsistency.\nStatus: " + status);
				CSTN.LOG.log(Level.FINER, "Final inconsistent graph: " + nextGraph);
			}
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		if (i > maxCycles && !status.finished) {
			if (CSTN.LOG.isLoggable(Level.INFO)) {
				CSTN.LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + status);
				CSTN.LOG.log(Level.FINER, "Last determined graph: " + nextGraph);
			}
			status.consistency = status.finished;
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		// consistent && finished
		if (CSTN.LOG.isLoggable(Level.INFO)) {
			CSTN.LOG.log(Level.INFO, "Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: "
					+ status);
		}
		nextGraph.copyCleaningRedundantLabels(currentGraph);
		// Put all data structures of currentGraph in g
		g.takeIn(nextGraph);
		g.setName(originalName);
		return status;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			if (!fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (fOutput != null) {
				if (fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!fOutput.getName().endsWith(".cstn"))
					fOutput.renameTo(new File(fOutput.getAbsolutePath() + ".cstn"));
				if (fOutput.exists()) {
					fOutput.delete();
				}
				try {
					fOutput.createNewFile();
					output = new PrintStream(fOutput);
				} catch (IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				output = System.out;
			}
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java CSTN [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-1.7.1-SNAPSHOT.jar" + parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " <CSTN_file_name>");
			return false;
		}
		return true;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 * 
	 * @param name
	 *                the proposed name. If an edge with name already exists, then name is modified adding an suitable integer suche that the name becomes
	 *                unique in 'g'.
	 * @param type
	 *                the type of edge to create.
	 * @param optimize
	 *                true if the labeled values in the edge have to be optimized
	 * @param g
	 *                the graph in which edge has to be added. This method cannot add the edge!
	 * @return an edge with a unique name.
	 */
	static private LabeledIntEdge newEdgeInCSTN(String name, LabeledIntEdge.Type type, boolean optimize, LabeledIntGraph g) {
		int i = g.getEdgeCount();
		String name1 = new String(name);
		while (g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		LabeledIntEdge e = new LabeledIntEdge(name1, type, optimize);
		return e;
	}
}
