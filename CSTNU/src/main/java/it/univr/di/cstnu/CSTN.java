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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
	 * Simple class to represent the status of the checking algorithm during the execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class CheckStatus {
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
					+ ((this.finished) ? "the consistency check has determined that given network is " + (this.consistency ? "" : "NOT ") + "consistent.\n"
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
	static final String VERSIONandDATE = "1.1, May, 04 2015";

	/**
	 * The name for the reference node.
	 */
	private static final String ZeroNodeName = "Z";

	/**
	 * <p>
	 * checkWellDefinitionProperties.
	 * </p>
	 *
	 * @param g a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @return true if the g is a CSTN well defined.
	 * @throws it.univr.di.cstnu.WellDefinitionException if any.
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
	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args an array of {@link java.lang.String} objects.
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
		CheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck(g, true, false, false);
		}
		catch (WellDefinitionException e) {
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
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Checks whether the new label to add or to use to replace another one subsumes the conjunction of labels of endpoint of the constraint.
	 *
	 * @param source starting node of the constraint
	 * @param dest ending node of the constraint
	 * @param newLabel new label to add or to use
	 * @param edgeName name of constraint
	 * @param ruleName name of rule applied to determine the new label.
	 * @return false if the check fails, true otherwise
	 */
	static boolean checkNodeLabelsSubsumption(final LabeledNode source, final LabeledNode dest, final Label newLabel, final String edgeName,
			final String ruleName) {
		if ((source == null) || (dest == null) || (newLabel == null) || (edgeName == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "One parameter is null. source: " + source + ", dest: " + dest + ", new label to add: " + newLabel + ", edgeName: "
						+ edgeName
						+ ". Please, check parameter.");
			}
			return false;
		}
		final Label labelConjunction = source.getLabel().conjunction(dest.getLabel());
		if (!newLabel.subsumes(labelConjunction)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "Subsumption check for a label generated by rule " + ruleName + " on edge " + edgeName
						+ ".\nThe new label, '" + newLabel + "', does not subsume the conjunction of node labels '" + labelConjunction + "'.\nIt is reject!");
			}
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property:<br>
	 * any labeled valued of the edge is consistent and subsumes both labels of two endpoints.
	 *
	 * @param e edge representing a labeled constraint.
	 * @param tail the source node of the edge.
	 * @param head the destination node of the edge.
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
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "Two endpoints don not allow any constraint because the have inconsisten labels.");
			}
			throw new WellDefinitionException("Two endpoints don not allow any constraint because the have inconsisten labels.",
					WellDefinitionException.Type.LabelInconsistent);
		}
		// check the ordinary labeled values
		for (final Object2IntMap.Entry<Label> entry : e.getLabeledValueMap().entrySet()) {
			if (!entry.getKey().subsumes(labelConjunction)) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				}
				throw new WellDefinitionException("Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
						WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the upper case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				}
				throw new WellDefinitionException("Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
						WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the lower case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getLowerLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				}
				throw new WellDefinitionException("Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
						WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		return true;
	}

	/**
	 * Checks whether the label of a node satisfies the well definition 2 property:<br>
	 * for each literal present in a node label label:<br>
	 * 1) the label of the observation node of the considered literal is subsumed by the label of the current node.<br>
	 * 2) the observation node is constrained to occur before the current node.
	 *
	 * @param g the current graph containing the node.
	 * @param node the current node to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition2Property(final LabeledIntGraph g, final LabeledNode node) throws WellDefinitionException {
		if ((g == null) || (node == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
			}
			return false;
		}

		final Label nodeLabel = node.getLabel();
		if (nodeLabel.isEmpty()) return true;

		// check the observation node
		for (final Literal l : nodeLabel.getAllAsStraight()) {
			final LabeledNode obs = g.getObservator(l);

			if (obs == null) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Observation node of literal " + l + " of node " + node + " does not exist.");
				}
				throw new WellDefinitionException("Observation node of literal " + l + " of node " + node + " does not exist.",
						WellDefinitionException.Type.ObservationNodeDoesNotExist);
			}

			final Label obsLabel = obs.getLabel();
			if (!nodeLabel.subsumes(obsLabel)) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "Label of node " + node + " does not subsume label of obs node " + obs);
				}
				throw new WellDefinitionException("Label of node " + node + " does not subsume label of obs node " + obs,
						WellDefinitionException.Type.LabelNotSubsumes);
			}

			final LabeledIntEdge e = g.findEdge(node, obs);
			if ((e == null) || (e.getMinValue() == LabeledIntNodeSetMap.INT_NULL) || (e.getMinValue() > 0)) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) {
					CSTN.LOG.log(Level.WARNING, "There is no constraint to execute obs node " + obs + " before node " + node);
				}
				throw new WellDefinitionException("There is no constraint to execute obs node " + obs + " before node " + node,
						WellDefinitionException.Type.ObservationNodeDoesNotOccurBefore);
			}
		}
		return true;
	}

	/**
	 * Checks whether each labeled value of an edge 'e' satisfies the well definition 3 property:<br>
	 * for each literal present in any label of 'e':<br>
	 * 1) the label of the observation node of the considered literal is subsumed by the label of the edge.<br>
	 *
	 * @param g the current graph containing the node.
	 * @param e the current edge to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition3Property(final LabeledIntGraph g, final LabeledIntEdge e) throws WellDefinitionException {
		if ((g == null) || (e == null)) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) {
				CSTN.LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
			}
			return false;
		}

		final Set<Object2IntMap.Entry<Entry<Label, String>>> allLabeledValuesSet = e.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		allLabeledValuesSet.addAll(e.getLowerLabelSet());
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : allLabeledValuesSet) {
			final Label edgeLabel = entry.getKey().getKey();
			if (edgeLabel.isEmpty()) {
				continue;
			}

			// check the observation node
			for (final Literal l : edgeLabel.getAllAsStraight()) {
				final LabeledNode obs = g.getObservator(l);

				if (obs == null) {
					if (CSTN.LOG.isLoggable(Level.WARNING)) {
						CSTN.LOG.log(Level.WARNING, "Observation node of literal " + l + " present in label " + edgeLabel + " of edge " + e
								+ " does not exist.");
					}
					throw new WellDefinitionException("Observation node of literal " + l + " present in label " + edgeLabel + " of edge " + e
							+ " does not exist.", WellDefinitionException.Type.ObservationNodeDoesNotExist);
				}

				final Label obsLabel = obs.getLabel();
				if (!edgeLabel.subsumes(obsLabel)) {
					if (CSTN.LOG.isLoggable(Level.WARNING)) {
						CSTN.LOG.log(Level.WARNING, "Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs);
					}
					throw new WellDefinitionException("Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs,
							WellDefinitionException.Type.LabelNotSubsumes);
				}
			}
		}
		return true;
	}

	/**
	 * @param newLabel
	 * @param value
	 * @param newEdge
	 * @return true if the value associated to newLabel is NOT infinity OR newLabel does contain unknown literals.
	 */
	static boolean isNewLabeledValueNotANegativeLoopEspression(final Label newLabel, final int value, final LabeledIntEdge newEdge) {
		if ((value == Constants.INT_NEG_INFINITE) && !newLabel.containsUnknown()) {
			if (CSTN.LOG.isLoggable(Level.FINER)) {
				CSTN.LOG.log(Level.FINER, "Found a negative loop that has determined a -∞ in the edge " + newEdge);
			}
			return false;
		}
		return true;
	}

	/**
	 * Applies rule R0: label containing a proposition that can be decided only in the future, is simplified removing such proposition.
	 *
	 * <pre>
	 * R0:
	 * P? --[α p, w]--&gt; X changes in P? --[α', w]--&gt; X when w &lt;0 (w&lt;0 for instantaneous reaction)
	 * 
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?.
	 * α is a label
	 * α' is α without P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param P the observation node
	 * @param X the other node
	 * @param PX the edge connecting P? ---&gt; X
	 * @param PXinNextGraph the edge connecting P? to X in the new graph.
	 * @param status
	 * @param instantaneousReaction true is it is admitted that observation points and other points depending from the observed proposition can
	 *            be executed in the same 'instant'.
	 *
	 * @return true if the rule has been applied one time at least.
	 */
	static boolean labelModificationR0(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledIntEdge PX,
			final LabeledIntEdge PXinNextGraph,
			final CheckStatus status, final boolean instantaneousReaction) {
		boolean ruleApplied = false, mergeStatus;
		final Literal p = P.getPropositionObserved();
		if (p == null) {
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, "Method labelModificationR0 called passing a non observation node as first parameter!");
			return false;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R0: start.");
		for (final Object2IntMap.Entry<Label> obsXLabeledValue : PX.labeledValueSet()) {
			final int w = obsXLabeledValue.getIntValue();
			final Label l = obsXLabeledValue.getKey();
			if ((w > 0) || (instantaneousReaction && (w == 0))) continue;
			if (l.getLiteralWithSameName(p) == null) continue;
			if (PX.getValue(l) == LabeledIntNodeSetMap.INT_NULL) continue;// it is possible that in a previous cycle the label has been removed.

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

			PXinNextGraph.putLabeledValueToRemovedList(l, w);
			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
			status.r0calls++;
			ruleApplied = true;
			mergeStatus = PXinNextGraph.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus && CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, logMessage);
			if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(alphaPrime, w, PXinNextGraph))) {
				if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Found an inconsistency. Label Modification R0: end.");
				status.finished = true;
				return ruleApplied;
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R0: end.");

		return ruleApplied;
	}

	/**
	 * Rule R1 adds a modified labeled value for each one of edge X--&gt;Y that cannot be evaluated when the edge has to be considered.
	 *
	 * <pre>
	 * if P? --[ab, w]--&gt; X --[bgp, v]--&gt; Y  and w&le;0 and v&lt;-w (v&lt;-w for instantaneous reaction),
	 * then the constraint between X and Y is modified as X --[abg', v]--[bgp, v]--&gt; Y
	 * where
	 * g' is g without the P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 *
	 * @param nObs observation node
	 * @param nX x node
	 * @param nY y node
	 * @param eXY LabeledIntEdge containing the constrain to modify
	 * @param eObsX LabeledIntEdge connecting observation node and x.
	 * @param eXYnew LabeledIntEdge that will contain the new determined labels.
	 * @param status
	 * @param instantaneousReaction
	 * @return true if a rule has been applied.
	 */
	static boolean labelModificationR1(final LabeledIntGraph currentGraph, final LabeledNode nObs, final LabeledNode nX, final LabeledNode nY,
			final LabeledIntEdge eObsX,
			final LabeledIntEdge eXY,
			final LabeledIntEdge eXYnew, final CheckStatus status, final boolean instantaneousReaction) {
		if (CSTN.LOG.isLoggable(Level.FINEST)) {
			CSTN.LOG.log(Level.FINEST, "Label Modification R1: start.");
		}
		boolean ruleApplied = false;
		final Literal p = nObs.getPropositionObserved();
		final LabeledNode Z = currentGraph.getZ();
		for (final Object2IntMap.Entry<Label> entryObsX : eObsX.labeledValueSet()) {
			final int w = entryObsX.getIntValue();
			final Label labelObsX = entryObsX.getKey();
			if ((w > 0) || (labelObsX.getLiteralWithSameName(p) != null)) continue; // R1 works with negative w associated to a label without 'p'.

			for (final Object2IntMap.Entry<Label> entryXY : eXY.labeledValueSet()) {
				final int v = entryXY.getIntValue();
				// condition on v value and its relation with w.
				// w is surely <=0; v can be any value. R1 has to applied when v<=-w (in case of instantaneous, v<-w).
				if ((v > -w) || (instantaneousReaction && (v == -w))) continue; // R1 cannot be applied
				final Label labelXY = entryXY.getKey();
				if (labelXY.getLiteralWithSameName(p) == null) continue; // R1 is applied when edge X-->Y contains a label having 'p'
				if ((nY != Z) && (!labelObsX.isConsistentWith(labelXY) || labelObsX.containsUnknown())) continue; // and it is consistent with label P?-->X (if
																													// Y is Z, the rule can be applied using ¿
																													// literals!)
				final Label labelXYWithoutP = new Label(labelXY);
				labelXYWithoutP.removeAllLiteralsWithSameName(p);

				final Label abg1 = CSTN.makeAlphaBetaGammaPrime(currentGraph, nObs, labelObsX, labelXYWithoutP, nY != Z);
				if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Rule R1 details alphaBetaGamma1=" + abg1);

				if (!CSTN.checkNodeLabelsSubsumption(nX, nY, abg1, eXY.getName(), "R1")) {
					// I check if the label subsumes the label of the endpoints before to proceed.
					// It should not necessary, but I put here as a guard!
					if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "Detail about the error of application R1 to edge " + eXY
							+ ":\nsource: "
							+ nObs.getName() + " ---(" + labelObsX + ", " + Constants.formatInt(w) + ")---> " + nX.getName() + " ---(" + labelXY + ", "
							+ Constants.formatInt(v) + ")---> " + nY.getName()
							+ "\nresult: " + nX.getName() + " ---(" + abg1 + ", " + Constants.formatInt(v) + ")---> " + nY.getName());
					throw new IllegalStateException("Rule R1 cannot determine label that does not subsume node labels!");
				}

				eXYnew.putLabeledValueToRemovedList(labelXY, v);
				ruleApplied = eXYnew.mergeLabeledValue(abg1, v);
				if (ruleApplied) {
					if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "R1 adds a label to edge " + eXY
							+ ":\nsource: "
							+ nObs.getName() + " ---(" + labelObsX + ", " + Constants.formatInt(w) + ")---> " + nX.getName() + " ---(" + labelXY + ", "
							+ Constants.formatInt(v) + ")---> "
							+ nY.getName()
							+ "\nresult: add " + nX.getName() + " ---(" + abg1 + ", " + Constants.formatInt(v) + ")---> " + nY.getName());
					status.r1calls++;
				}
				if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(abg1, v, eXYnew))) return ruleApplied;
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R1: end.");
		return ruleApplied;
	}

	/**
	 * Applies rule R2: label containing a proposition that can be decided only in the future, is simplified
	 * removing such proposition.
	 *
	 * <pre>
	 * R2:
	 * P? &lt;--[α p,w]-- X  changes in P? &lt;--[α',max{w,0}]-- X
	 * where:
	 * p can be the positive o the negative literal associated to proposition observed in P?.
	 * α is a label
	 * α' is α without P? children.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param P
	 * @param X
	 * @param XP
	 * @param XPinNextGraph
	 * @param status
	 * @param instantaneousReaction true is it is admitted that observation points and other points depending from the observed proposition can
	 *            be executed in the same 'instant'.
	 *
	 * @return true if the rule has been applied one time at least.
	 */
	static boolean labelModificationR2(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledIntEdge XP,
			final LabeledIntEdge XPinNextGraph,
			final CheckStatus status, final boolean instantaneousReaction) {
		boolean ruleApplied = false;
		final Literal p = P.getPropositionObserved();
		if (p == null) {
			if (CSTN.LOG.isLoggable(Level.WARNING))
				CSTN.LOG.log(Level.WARNING, "Method labelModificationR2 called passing a non observation node as first parameter!");
			return false;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R2: start.");
		for (final Object2IntMap.Entry<Label> x_ObsLabeledValue : XP.labeledValueSet()) {
			final int w = x_ObsLabeledValue.getValue();
			final int max = Math.max(w, 0);
			final Label l = x_ObsLabeledValue.getKey();
			if (l.getLiteralWithSameName(p) == null) continue;
			if (XP.getValue(l) == LabeledIntNodeSetMap.INT_NULL) continue;// it is possible that in a previous cycle the label has been removed.

			final Label alphaPrime = new Label(l);
			alphaPrime.removeAllLiteralsWithSameName(p);
			alphaPrime.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(P));

			if (!CSTN.checkNodeLabelsSubsumption(X, P, alphaPrime, XP.getName(), "R2")) {
				if (w > 0) {
					// R2 rule
					// It means that 'X' label contains 'p'!
					// The labeled value has to be substituted by a 0 labeled value because by WD2 the node ha to be after the observation node.
					if (CSTN.LOG.isLoggable(Level.FINER))
						CSTN.LOG.log(Level.FINER, "Since after R2, the label does not subsumes the conjection of node labes, it means that " + X.getName()
								+ " has 'p' in its label. It has to be after " + P.getName() + ". Edge: " + XP
								+ ":\nsource: " + P + " <---(" + l + ", " + Constants.formatInt(w) + ")--- " + X
								+ "\nresult: " + P.getName() + " <---(" + X.getLabel().conjunction(P.getLabel()) + ", 0)--- " + X.getName());
					XPinNextGraph.mergeLabeledValue(X.getLabel().conjunction(P.getLabel()), 0);
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

			XPinNextGraph.putLabeledValueToRemovedList(l, w);
			// XPinNextGraph.removeLabel(l); If w>0, then the label is removed my optimization. Otherwise the label has to be preserved.
			status.r2calls++;
			ruleApplied = true;
			if (XPinNextGraph.mergeLabeledValue(alphaPrime, max) && CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, logMessage);
			if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(alphaPrime, max, XPinNextGraph))) return ruleApplied;
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R2: end.");
		return ruleApplied;
	}

	/**
	 * Rule R3
	 *
	 * <pre>
	 * if P? --[ab, w]--&gt; X &lt;--[bgp, v]-- Y  and w &le;0
	 * then the constraint between Y and X is modified adding the following label:
	 * X &lt;--[abg', max{w,v}]-- Y
	 * 
	 * where g' is g but the children of P?.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param nObs observation node
	 * @param nX x node
	 * @param nY y node
	 * @param eYX LabeledIntEdge containing the constrain to modify
	 * @param eObsX LabeledIntEdge connecting observation node and x.
	 * @param eYXnew LabeledIntEdge that will contain the new determined labels.
	 * @param status
	 * @param instantaneousReaction
	 * @return true if a rule has been applied.
	 */
	static boolean labelModificationR3(final LabeledIntGraph currentGraph, final LabeledNode nObs, final LabeledNode nX, final LabeledNode nY,
			final LabeledIntEdge eObsX,
			final LabeledIntEdge eYX,
			final LabeledIntEdge eYXnew, final CheckStatus status, final boolean instantaneousReaction) {
		if (CSTN.LOG.isLoggable(Level.FINEST)) {
			CSTN.LOG.log(Level.FINEST, "Label Modification R3: start.");
		}
		boolean ruleApplied = false;
		final Literal p = nObs.getPropositionObserved();
		final LabeledNode Z = currentGraph.getZ();
		for (final Object2IntMap.Entry<Label> entryObsX : eObsX.labeledValueSet()) {
			final int w = entryObsX.getValue();
			final Label labelObsX = entryObsX.getKey();

			if ((w > 0) || (labelObsX.getLiteralWithSameName(p) != null)) continue; // R3 work with negative w.

			for (final Object2IntMap.Entry<Label> entryYX : eYX.labeledValueSet()) {
				final int v = entryYX.getIntValue();
				final int max = Math.max(w, v);

				final Label labelYX = entryYX.getKey();
				if (labelYX.getLiteralWithSameName(p) == null) continue;// R3 simplifies 'p' on labelYX!
				if ((nX != Z) && !labelObsX.isConsistentWith(labelYX)) continue;
				final Label labelYXWithoutP = new Label(labelYX);
				labelYXWithoutP.removeAllLiteralsWithSameName(p);

				//FIXME If Z is involved, then I had to remove also all children of q-literals! (Check TIME 2015 paper)
				final Label abg1 = CSTN.makeAlphaBetaGammaPrime(currentGraph, nObs, labelObsX, labelYXWithoutP, nX != Z);
				if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Rule R3 details alphaBetaGamma1=" + abg1);

				eYXnew.putLabeledValueToRemovedList(labelYX, v);
				ruleApplied = eYXnew.mergeLabeledValue(abg1, max);
				if (ruleApplied) {
					if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eYX.getName() + ":\n"
							+ "source: " + nObs.getName() + " ---(" + labelObsX + ", " + Constants.formatInt(w) + ")---> " + nX.getName()
							+ " <---(" + labelYX + ", " + Constants.formatInt(v) + ")--- " + nY.getName()
							+ "\nresult: add " + nX.getName() + " <---(" + abg1 + ", " + Constants.formatInt(max) + ")--- " + nY.getName());
					status.r3calls++;
				}
				if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(abg1, max, eYXnew))) {
					if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Found an inconsistency.\nLabel Modification R3: end.");
					status.finished = true;
					return ruleApplied;
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Label Modification R3: end.");
		return ruleApplied;
	}

	/**
	 * Applies the labeled propagation rule.
	 *
	 * <pre>
	 * if A --[l1, x]--&gt; B --[l2, y]--&gt; C, then A --[l1l2, x+y]--&gt; C
	 * where l1l2 is the extended conjunction when A is the Z node, otherwise is the standard conjunction.
	 * </pre>
	 *
	 * @param currentGraph the originating graph.
	 * @param A
	 * @param B
	 * @param C
	 * @param AB
	 * @param BC
	 * @param Z the Z node in currentGraph (only to speed-up)
	 * @param nextGraph the graph where to write the new constraints.
	 * @param status
	 * @return true if a reduction has been applied.
	 */
	static boolean labelPropagationRule(final LabeledIntGraph currentGraph, final LabeledNode A, final LabeledNode B, final LabeledNode C,
			final LabeledIntEdge AB, final LabeledIntEdge BC,
			final LabeledNode Z, final LabeledIntGraph nextGraph, final CheckStatus status) {

		final LabeledIntEdge AC = currentGraph.findEdge(A, C);
		final LabeledNode AinNextGraph = nextGraph.getNode(A.getName());
		final LabeledNode CinNextGraph = nextGraph.getNode(C.getName());
		LabeledIntEdge ACinNextGraph = nextGraph.findEdge(AinNextGraph, CinNextGraph);
		boolean ACinNextGraphCreated = false;
		if (ACinNextGraph == null) {
			ACinNextGraphCreated = true;
			ACinNextGraph = new LabeledIntEdge("e" + A.getName() + C.getName(), LabeledIntEdge.Type.derived, nextGraph.isOptimize());
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

//				final boolean isNegativePathToZ = (C == Z) && (y<0) && (x<0);//original
				// FIXME
				final boolean isNegativePathToZ = (C == Z) && (y < 0) && (x < 0);// Node set have to be managed also when we add a positive value of a q-Loop
																					// (x>0)
				Label newLabelAC = (isNegativePathToZ && x < 0) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
				if (newLabelAC == null) continue;

				final int oldZ = (AC != null) ? AC.getValue(newLabelAC) : LabeledIntNodeSetMap.INT_NULL;
				SortedSet<String> sigma = null;
				if (isNegativePathToZ) {
					/*
					 * The node set is update only if the new value is better the old one.
					 */
					if (oldZ != Constants.INT_NULL && sum > oldZ) continue;
					/*
					 * Special case
					 * A --[beta, x]--> B --[alpha, y, sigma1]--> Z with x<0 and y<0
					 * results to be
					 * A --[alpha beta, sum, sigma2]--> Z
					 * where sum = x+y and sigma2 = sigma1 ∪ A, if A is not in sigma1,
					 * otherwise sum = -infinity and sigma2 = null.
					 * If AC contains [alpha beta, w, sigma], then w is overwritten if w> sum and, in any case, sigma = sigma + sigma2.
					 */
					// Clean the new label from children
					for (Literal unknownLit : newLabelAC.getAllUnknown()) {
						newLabelAC.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(currentGraph.getObservator(unknownLit)));
					}
					final boolean isAInNodeSet = (nodeSetBC == null) ? false : nodeSetBC.contains(A.getName());
					if (isAInNodeSet) {
						sum = Constants.INT_NEG_INFINITE;
						if (CSTN.LOG.isLoggable(Level.FINEST))
							CSTN.LOG.log(Level.FINEST, "Negative qLoop detected during the updated of " + ACinNextGraph.getName() + ": "
									// + "source: " + A.getName() + " --(" + labelAB + ", " + Constants.formatInt(x) + ")--> " + B.getName() + " --(" + labelBC
									// + ", "
//									+ Constants.formatInt(y) + ")--> " + C.getName()
									+ A.getName() + " is in the node set of label '" + labelBC + "' of the edge " + BC.getName()
									+ ". New labeled value = (" + newLabelAC + ", -" + Constants.INFINITY_SYMBOLstring + ").");
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
							CSTN.LOG.log(Level.FINEST, "The new CANDIDATE node set to add to label '" + newLabelAC + "' on edge " + ACinNextGraph.getName()
									+ " is " + sigma);
						}
					}
				}

				// I have to prepare the log before the execution of the merge!
				String log = null;
				if (CSTN.LOG.isLoggable(Level.FINER)) {
					log = "Label Propagation Rule applied to edge " + ACinNextGraph.getName()
							+ ":\nsource: " + A.getName() + " --(" + labelAB + ", " + Constants.formatInt(x) + ")--> " + B.getName()
							+ " --(" + labelBC + ", " + Constants.formatInt(y)
							+ ((nodeSetBC != null && !nodeSetBC.isEmpty()) ? ", " + nodeSetBC.toString() : "")
							+ ")--> " + C.getName()
							+ "\nresult: " + A.getName() + " --(" + newLabelAC + ", "
							+ ((oldZ == LabeledIntNodeSetMap.INT_NULL || sum < oldZ) ? Constants.formatInt(sum) : Constants.formatInt(oldZ))// it could be
							// add only the node set
							+ ((sigma != null && !sigma.isEmpty()) ? ", " + sigma.toString() : "")
							+ ")--> " + C.getName() + "; old value: "
							+ Constants.formatInt(oldZ) + ", " + ((AC != null) ? AC.getNodeSet(newLabelAC) : "");
				}

				if (ACinNextGraph.mergeLabeledValue(newLabelAC, sum, sigma)) {
					ruleApplied = true;
					status.labeledValuePropagationcalls++;
					CSTN.LOG.log(Level.FINER, log);
				}
				if (!(status.consistency = CSTN.isNewLabeledValueNotANegativeLoopEspression(newLabelAC, sum, ACinNextGraph))) {
					status.finished = true;
					return ruleApplied;
				}
			}
		}
		if (A == C) { // self loop, check if there is a negative loop!
			final int min = ACinNextGraph.getMinValueAmongLabelsWOUnknown();
			if ((min != LabeledIntNodeSetMap.INT_NULL) && (min < 0)) {
				if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "Found a negative loop in node " + ACinNextGraph.toString());
				status.finished = true;
				status.consistency = false;
			}
		}
		if (ACinNextGraphCreated && ruleApplied) {
			nextGraph.addEdge(ACinNextGraph, AinNextGraph, CinNextGraph);
//			if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "Labeled propagation rule: added edge " + ACinNextGraph);
		}
		return ruleApplied;
	}

	/**
	 * Simple method to determine the three parts of labels as required by rules R1 and R3.<br>
	 * Label[0] is the sublabel of labelEdgeFromObs not in common with labelWithoutP.<br>
	 * Label[1] is the sublabel of labelEdgeFromObs in common with labelWithoutP.<br>
	 * Label[2] is the sublabel of labelEdgeFromObs not in common with labelEdgeFromP. <br>
	 * Be careful!
	 * If the two labels are inconsistent, Label[1] contains, for each literal making inconsistent the two labels, the literal in unknown state. <br>
	 *
	 * @param currentGraph
	 * @param observator
	 * @param labelEdgeFromObs
	 * @param labelWithoutP
	 * @param strict
	 * @return alphaBetaGamma1 already completed.
	 */
	static Label makeAlphaBetaGammaPrime(final LabeledIntGraph currentGraph, final LabeledNode observator, final Label labelEdgeFromObs,
			final Label labelWithoutP,
			final boolean strict) {
		final Label alpha = labelEdgeFromObs.getSubLabelIn(labelWithoutP, false, strict);
		final Label beta = labelEdgeFromObs.getSubLabelIn(labelWithoutP, true, strict);
		final Label gamma1 = labelWithoutP.getSubLabelIn(labelEdgeFromObs, false, strict);
		if (CSTN.LOG.isLoggable(Level.FINEST))
			CSTN.LOG.log(Level.FINEST, "labelEdgeFromObs = " + labelEdgeFromObs + ", labelWithoutP=" + labelWithoutP + ",  alpha = " + alpha + ", beta=" + beta
					+ " gama=" + gamma1);
		gamma1.removeAllLiteralsWithSameName(currentGraph.getChildrenOf(observator));
		if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "gama1=" + gamma1);
		if (strict) {
			Label ab = alpha.conjunction(beta);
			if (ab != null) return ab.conjunction(gamma1);
			return null;
		}
		return alpha.conjunctionExtended(beta).conjunctionExtended(gamma1);
	}

	/**
	 * Executes one step of the dynamic consistency check in the new version.
	 * <p>
	 * One step consists into an exhaustive label propagation with the application of R0 on resulting new edges, followed by an exhaustive R3 application.
	 *
	 * @param currentGraph the current graph. It is just read. No modification is made on it.
	 * @param nextGraph the next graph. At the end of the procedure, it will contain the results of reductions. Since it cannot be null, it is required to be
	 *            equal to the current graph. For efficiency reasons, no check is made.
	 * @param n # of vertices.
	 *
	 * @param instantaneousReaction true is it is admitted that observation points and other points depending from the observed proposition can be executed in
	 *            the same 'instant'.
	 * @param labelOptimization true if the possibly new generated edges have to remove redundant labeled values.
	 * @param status the record where to store statistics and exit status of the execution.
	 * @param mainCycle index of this method invocation (just for logging).
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	// FIXME Cambiato in modo che applichi LP ad ongi triangolo. Per ogni applicazione, sul grafo nuovo si applica subito R0 e R3.
	static CheckStatus oneStepDynamicConsistencyNEW(LabeledIntGraph currentGraph, LabeledIntGraph nextGraph, final int n, final boolean instantaneousReaction,
			final boolean labelOptimization, final CheckStatus status, int mainCycle)
			throws WellDefinitionException {

		/*
		 * Label Propagation
		 * Apply label propagation rule to all possible node triple (A,B,C) until no new value is generated.
		 * Exploiting a possible sparse graph, the triple are generated making, for each node B, two cycles:
		 * one to find all possible A and, for each A, one to find all possible C.
		 * In this way, the triple is used to mainly consider:
		 * A --> B --> C
		 */
		LabeledNode A, B, C, AinNextGraph, CinNextGraph;
		LabeledIntEdge AB, ACinNextGraph, BC;

		LabeledNode[] node = currentGraph.getVerticesArray();
		LabeledNode Z = currentGraph.getZ();

		status.cycles++;

		if (CSTN.LOG.isLoggable(Level.FINER)) {
			CSTN.LOG.log(Level.FINER, "");
			CSTN.LOG.log(Level.FINER, "Start application labeled propagation rule+R0+R3. Cycle " + mainCycle);
		}
		for (int k = 0; k < n; k++) {
			B = node[k];
			for (final Iterator<LabeledIntEdge> eABIter = currentGraph.getInEdges(B).iterator(); eABIter.hasNext();) {
				AB = eABIter.next();
				A = currentGraph.getSource(AB);
				AinNextGraph = nextGraph.getNode(A.getName());
				boolean isAanObservation = A.getPropositionObserved() != null;
				for (final Iterator<LabeledIntEdge> eBCIter = currentGraph.getOutEdges(B).iterator(); eBCIter.hasNext();) {
					BC = eBCIter.next();
					C = currentGraph.getDest(BC);
					if (C == B) continue;// self loop on the second pair in not useful. The only loop it has to be maintain is A == C
					// Now it is possible to propagate the labels with the standard rules
					CSTN.labelPropagationRule(currentGraph, A, B, C, AB, BC, Z, nextGraph, status);
					if (!status.consistency) {
						return status;
					}
					CinNextGraph = nextGraph.getNode(C.getName());
					ACinNextGraph = nextGraph.findEdge(AinNextGraph, CinNextGraph);
					if (ACinNextGraph != null) {
						if (isAanObservation) {
							// R0 on the resulting new values
							CSTN.labelModificationR0(nextGraph, AinNextGraph, CinNextGraph, ACinNextGraph, ACinNextGraph, status, instantaneousReaction);
						}
						// R3 on the resulting new values
						CSTN.applyR3(nextGraph, AinNextGraph, CinNextGraph, ACinNextGraph, status, instantaneousReaction);
					}
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3. Cycle " + mainCycle
					+ "\nSituation after the labeled propagation rule+R0+R3.");

		status.finished = currentGraph.hasSameEdgesOf(nextGraph);
		if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "\n");

		return status;
	}

	/**
	 * 
	 * @param currentGraph
	 * @param nY
	 * @param nX
	 * @param eYX
	 * @param status
	 * @param instantaneousReaction
	 * @return
	 */
	static CheckStatus applyR3(final LabeledIntGraph currentGraph, final LabeledNode nY, final LabeledNode nX, final LabeledIntEdge eYX,
			final CheckStatus status, final boolean instantaneousReaction) {

		if (CSTN.LOG.isLoggable(Level.FINER)) {
			CSTN.LOG.log(Level.FINER, "");
			CSTN.LOG.log(Level.FINER, "Start application rule R3.");
		}
		// I want to reuse labelModificationR3(), so I prepare all data for calling that metods.

		// In order to check all labeled values of eYX, it is necessary to find all observation node involved.
		Set<LabeledNode> observator = new TreeSet<>();
		for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> label : eYX.labeledValueSet()) {
			for (Literal l : label.getKey().getAllAsStraight()) {
				LabeledNode n = currentGraph.getObservator(l);
				if (n != null) observator.add(n);
			}
		}

		LabeledIntEdge ePX;
		for (LabeledNode P : observator) {
			ePX = currentGraph.findEdge(P, nX);
			if (ePX == null) continue;

			CSTN.labelModificationR3(currentGraph, P, nX, nY, ePX, eYX, eYX, status, instantaneousReaction);
			if (!status.consistency) {
				return status;
			}
			// Rule R0
			if (nY.getPropositionObserved() != null) {
				CSTN.labelModificationR0(currentGraph, nY, nX, eYX, eYX, status, instantaneousReaction);
			}
			if (!status.consistency) {
				return status;
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "End application rule R3.");

		return status;
	}

	/**
	 * Executes one step of the dynamic consistency check.
	 * <p>
	 * One step consists in the application of all known rules (one time) to all edges/triangles of the network.
	 *
	 * @param currentGraph the current graph. It is just read. No modification is made on it.
	 * @param nextGraph the next graph. At the end of the procedure, it will contain the results of reductions. Since it cannot be null, it is required to be
	 *            equal to the current graph. For efficiency reasons, no check is made.
	 * @param n # of vertices.
	 *
	 * @param instantaneousReaction true is it is admitted that observation points and other points depending from the observed proposition can be executed in
	 *            the same 'instant'.
	 * @param onlyOnZ true if one wants to apply R0 and R3 only when node Z is involved.
	 * @param excludeR1R2 true if one wants to exclude the application of rules R1 and R2.
	 * @param labelOptimization true if the possibly new generated edges have to remove redundant labeled values.
	 * @param status the record where to store statistics and exit status of the execution.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	static CheckStatus oneStepDynamicConsistency4StepByStepExecution(LabeledIntGraph currentGraph, LabeledIntGraph nextGraph, final int n,
			final boolean instantaneousReaction, final boolean onlyOnZ, final boolean excludeR1R2, final boolean labelOptimization, final CheckStatus status)
			throws WellDefinitionException {

		/*
		 * All possible node triple (A,B,C) are generated.
		 * For each triple the rule are applied according with the rule conditions.
		 * Exploiting a possible sparse graph, the triple are generated making, for each node B, two cycles:
		 * one to find all possible A and, for each A, one to find all possible C.
		 * In this way, the triple is used to mainly consider:
		 * A --> B --> C
		 * Since R3 rule needs B <-- C, for that rule also the edge B<--C is considered.
		 */
		LabeledNode C, B, A, C1;
		LabeledIntEdge AB, ABinNextGraph, BC, BCinNextGraph, CB, CBinNextGraph;

		LabeledNode[] node = currentGraph.getVerticesArray();
		LabeledNode Z = currentGraph.getZ();

		status.cycles++;
		// There are two separate visit of the graph (it is necessary for the structure of label propagation rule and R3 one)

		// FIRST
		if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "Start application rule R0--R3.");
		for (int k = 0; k < n; k++) {
			B = node[k];
			for (final Iterator<LabeledIntEdge> eABIter = currentGraph.getInEdges(B).iterator(); eABIter.hasNext();) {
				AB = eABIter.next();
				A = currentGraph.getSource(AB);
				if (A == B) continue;// self loop on the first pair in not useful.

				if (A.getPropositionObserved() != null) {
					// Rule R0
					ABinNextGraph = nextGraph.findEdge(nextGraph.getNode(A.getName()), nextGraph.getNode(B.getName()));
					if (!onlyOnZ || (onlyOnZ && B == Z)) CSTN.labelModificationR0(currentGraph, A, B, AB, ABinNextGraph, status, instantaneousReaction);
					if (!status.consistency) return status;

					for (final Iterator<LabeledIntEdge> eC1BIter = currentGraph.getInEdges(B).iterator(); eC1BIter.hasNext();) {
						// Rule R3
						CB = eC1BIter.next();
						C1 = currentGraph.getSource(CB);
						if (A == C1) continue;
						CBinNextGraph = nextGraph.findEdge(nextGraph.getNode(C1.getName()), nextGraph.getNode(B.getName()));
						if (!onlyOnZ || (onlyOnZ && B == Z))
							CSTN.labelModificationR3(currentGraph, A, B, C1, AB, CB, CBinNextGraph, status, instantaneousReaction);
						if (!status.consistency) return status;
					}
				}
				if (B.getPropositionObserved() != null && !excludeR1R2) {
					// Rule R2
					ABinNextGraph = nextGraph.findEdge(nextGraph.getNode(A.getName()), nextGraph.getNode(B.getName()));
					CSTN.labelModificationR2(currentGraph, B, A, AB, ABinNextGraph, status, instantaneousReaction);
					if (!status.consistency) return status;
					// labelOptimizationR0(B,C,BC); It would be checked two times!
				}

				if (!excludeR1R2) {
					for (final Iterator<LabeledIntEdge> eBCIter = currentGraph.getOutEdges(B).iterator(); eBCIter.hasNext();) {
						BC = eBCIter.next();
						C = currentGraph.getDest(BC);
						if (C == B) continue;// self loop on the second pair in not useful. The only loop it has to be maintain is A == C

						if (A.getPropositionObserved() != null) {
							// Rule R1
							if (A != C) {// self loop is useful only for the labeled std propagation rule
								BCinNextGraph = nextGraph.findEdge(nextGraph.getNode(B.getName()), nextGraph.getNode(C.getName()));
								CSTN.labelModificationR1(currentGraph, A, B, C, AB, BC, BCinNextGraph, status, instantaneousReaction);
								if (!status.consistency) return status;
							}
						}
						// if (C.getObservable()!=null) labelModificationR2(C,B,BC); It would be checked two times!
					}
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "End application rule R0--R3.\n\nSituation after the application of R0--R3 rules.");

		final boolean noChanged = currentGraph.hasSameEdgesOf(nextGraph);

		currentGraph.copy(nextGraph);
		node = currentGraph.getVerticesArray();
		Z = currentGraph.getZ();
		nextGraph.setName("nextGraph");
		currentGraph.setName("currentGraph");

		// SECOND
		if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "Start application labeled propagation rule.");
		for (int k = 0; k < n; k++) {
			B = node[k];
			for (final Iterator<LabeledIntEdge> eABIter = currentGraph.getInEdges(B).iterator(); eABIter.hasNext();) {
				AB = eABIter.next();
				A = currentGraph.getSource(AB);
				for (final Iterator<LabeledIntEdge> eBCIter = currentGraph.getOutEdges(B).iterator(); eBCIter.hasNext();) {
					BC = eBCIter.next();
					C = currentGraph.getDest(BC);
					if (C == B) continue;// self loop on the second pair in not useful. The only loop it has to be maintain is when A == C
					// Now it is possible to propagate the labels with the standard rules
					CSTN.labelPropagationRule(currentGraph, A, B, C, AB, BC, Z, nextGraph, status);
					if (!status.consistency) return status;
				}
			}
		}
		if (CSTN.LOG.isLoggable(Level.FINER))
			CSTN.LOG.log(Level.FINER, "End application labeled propagation rule.\n\nSituation after the labeled propagation rule.");

		status.finished = currentGraph.hasSameEdgesOf(nextGraph) && noChanged;

		return status;
	}

	/**
	 * Initializes all data structure before the execution of consistency checking algorithm.
	 * <p>
	 *
	 * @param g the graph to check.
	 * @param labelOptimization true if the added edges should have labeled value set minimized during the check.
	 * @return true. If there is any problem, it throws an exception saying the error.
	 * @throws IllegalArgumentException if the graph is null or it not contains Z node or it is not well formed.
	 */
	private static boolean initAndCheck(final LabeledIntGraph g, final boolean labelOptimization) throws IllegalArgumentException {
		if (g == null) throw new IllegalArgumentException("The graph is null!");

		g.clearCache();

		final SortedSet<LabeledIntEdge> edgeSet = new ObjectRBTreeSet<>(g.getEdges());
		for (final LabeledIntEdge e : edgeSet) {

			if (CSTN.LOG.isLoggable(Level.FINEST)) CSTN.LOG.log(Level.FINEST, "Init. Checkin edge e: " + e);
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
							CSTNU.LOG.log(Level.WARNING, "Found labeled value " + l1 + " in " + e + " inconsistent with the conjunction of node labels, "
									+ conjunctLabel + ". Removed");
						}
						e.removeLabel(entry.getKey());
					} else {
						if (!l1.subsumes(conjunctLabel)) {
							CSTNU.LOG.warning("Found a labeled value in " + e + " that does not subsume the conjunction of node labels, " + conjunctLabel
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
			}
			catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (e.isContingentEdge()) {
				if (CSTN.LOG.isLoggable(Level.WARNING)) CSTN.LOG.log(Level.WARNING, "Found a contingent edge: " + e
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
				if (CSTN.LOG.isLoggable(Level.WARNING)) CSTN.LOG.log(Level.WARNING, "No " + ZeroNodeName + " node found: added!");
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
						CSTN.LOG.log(Level.WARNING,
								"Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName() + "'. Removed!");
				}
				label.remove(obs);
			}

			try {
				CSTN.checkWellDefinition2Property(g, node);
			}
			catch (final WellDefinitionException ex) {
				if (ex.getType() == Type.ObservationNodeDoesNotOccurBefore) {
					for (final Literal l1 : label.getAllAsStraight()) {
						final LabeledNode obsl1 = g.getObservator(l1);
						LabeledIntEdge e = g.findEdge(node, obsl1);
						if (e == null) {
							e = new LabeledIntEdge("e" + node.getName() + obsl1.getName(), LabeledIntEdge.Type.derived, labelOptimization);
							g.addEdge(e, node, obsl1);
							if (CSTN.LOG.isLoggable(Level.WARNING)) {
								CSTN.LOG.log(Level.WARNING, "It is necessary to add a constraint to guarantee that node '" + node.getName()
										+ "' occurs after node '"
										+ obsl1.getName() + "' to satisfy WD2.");
							}
						}
						e.mergeLabeledValue(label, 0);
					}
				} else throw new IllegalArgumentException(ex.getMessage());
			}
		}

		if (!Z.getLabel().isEmpty()) {
			if (CSTN.LOG.isLoggable(Level.WARNING)) CSTN.LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
			Z.setLabel(Label.emptyLabel);
		}
		// Now I assuring that each node has a edge to Z.
		for (final LabeledNode node : nodeSet) {
			if (node == Z) {
				continue;
			}
			LabeledIntEdge e = g.findEdge(node, Z);
			if (e == null) {
				e = new LabeledIntEdge("e" + node.getName() + Z.getName(), LabeledIntEdge.Type.derived, labelOptimization);
				g.addEdge(e, node, Z);
				if (CSTN.LOG.isLoggable(Level.INFO)) {
					CSTN.LOG.log(Level.INFO, "It is necessary to add a constraint to guarantee that node '" + node.getName() + "' occurs after node '"
							+ Z.getName());// +
											// "' because Z must be the first node. Be careful, we operate with integer, it is necessary to set -1 the distance!");
				}
			}
			e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
		}
		return true;
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
	@Option(required = false, name = "-o", aliases = "--output"
			, usage = "output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output."
			, metaVar = "CSTN_file_name")
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
	 * <p>
	 * Constructor for CSTN.
	 * </p>
	 *
	 * @param withOptimization true if the propagation of labeled value has also to remove redundand labeled values.
	 */
	public CSTN(final boolean withOptimization) {
		this.labelOptimization = withOptimization;
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance and, if the instance is consistent, determines all the minimal
	 * ranges for the constraints. <br>
	 * All label containing proposition that cannot be evaluated at run time are removed.<br>
	 * This method tries to execute only LP with R0 and R3 for at most |P| cycles.
	 *
	 * @param g the original graph that has to be checked. If the check is successful, g is modified and it contains all
	 *            minimized constraints; otherwise, it is not modified.
	 * @param instantaneousReactions true is it is admitted that observation points and other points depending from the observed proposition can be executed
	 *            in the same 'instant'.
	 * @param onlyOnZ true if one wants to apply R0 and R3 only when node Z is involved.
	 * @param excludeR1R2 true if one wants to exclude the application of rules R1 and R2.
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this
	 *             exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CheckStatus dynamicConsistencyCheck(final LabeledIntGraph g, final boolean instantaneousReactions, boolean onlyOnZ, boolean excludeR1R2)
			throws WellDefinitionException {
		final CheckStatus status = new CheckStatus();
		if (g == null) return status;

		String originalName = g.getName();
		LabeledIntGraph currentGraph, nextGraph;
		currentGraph = new LabeledIntGraph(g, this.labelOptimization);
		currentGraph.setName("Current graph");
		try {
			CSTN.initAndCheck(currentGraph, this.labelOptimization);
		}
		catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		nextGraph = new LabeledIntGraph(currentGraph, this.labelOptimization);
		nextGraph.setName("New graph");

		final int n = currentGraph.getVertexCount();
		final int propositionN = currentGraph.getPropositions().size();
		// TODO: trovare il numero giusto di iterazioni
		final int maxCycles = propositionN * 10;
		if (CSTN.LOG.isLoggable(Level.FINER)) CSTN.LOG.log(Level.FINER, "The maximum number of possible cycles is " + maxCycles);

		int i;

		long startTime = System.nanoTime();
		for (i = 1; (i <= maxCycles) && status.consistency && !status.finished; i++) {
			if (CSTN.LOG.isLoggable(Level.FINE)) CSTN.LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
			CSTN.oneStepDynamicConsistencyNEW(currentGraph, nextGraph, n, instantaneousReactions, this.labelOptimization, status, i);
//			CSTN.oneStepDynamicConsistency4StepByStepExecution(currentGraph, nextGraph, n, instantaneousReactions, onlyOnZ, excludeR1R2, this.labelOptimization, status);
			if (status.consistency && !status.finished) {
				currentGraph.copy(nextGraph);
				nextGraph.setName("nextGraph");
				currentGraph.setName("currentGraph");
			}
			if (CSTN.LOG.isLoggable(Level.FINE)) CSTN.LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
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
	 * Help method to initialize and check the CSTN represented by graph g.
	 * The {@link #dynamicConsistencyCheckOLD(LabeledIntGraph, boolean, boolean, boolean)} calls this method before to procede the check.
	 *
	 * @param g a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @return true if the graph is a well formed CSTN.
	 */
	public boolean initAndCheck(final LabeledIntGraph g) {
		return CSTN.initAndCheck(g, this.labelOptimization);
	}

	/**
	 * Help method to execute one step of the checking algorithm.
	 *
	 * @param currentGraph a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @param nextGraph a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @param instantaneousReaction a boolean.
	 * @param onlyOnZ true if one wants to apply R0 and R3 only when node Z is involved.
	 * @param excludeR1R2 true if one wants to exclude the application of rules R1 and R2.
	 * @param status a {@link it.univr.di.cstnu.CSTN.CheckStatus} object.
	 * @return status the execution
	 * @throws it.univr.di.cstnu.WellDefinitionException if any.
	 */
	public CheckStatus oneStepDynamicConsistency(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, final boolean instantaneousReaction,
			final boolean onlyOnZ, final boolean excludeR1R2, final CheckStatus status) throws WellDefinitionException {
		return CSTN.oneStepDynamicConsistency4StepByStepExecution(currentGraph, nextGraph, currentGraph.getVertexCount(), instantaneousReaction, onlyOnZ,
				excludeR1R2,
				this.labelOptimization, status);
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

			if (!fInput.exists()) throw new CmdLineException(parser, "Input file does not exist.");

			if (fOutput != null) {
				if (fOutput.isDirectory()) throw new CmdLineException(parser, "Output file is a directory.");
				if (!fOutput.getName().endsWith(".cstn"))
					fOutput.renameTo(new File(fOutput.getAbsolutePath() + ".cstn"));
				if (fOutput.exists()) {
					fOutput.delete();
				}
				try {
					fOutput.createNewFile();
					output = new PrintStream(fOutput);
				}
				catch (IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				output = System.out;
			}
		}
		catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java CSTN [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-1.7.1-SNAPSHOT.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) + " <CSTN_file_name>");
			return false;
		}
		return true;
	}
}
