package it.univr.di.cstnu;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.univr.di.cstnu.WellDefinitionException.Type;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
import it.univr.di.labeledvalue.Literal;

/**
 * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU). It does not manage overflow cases when summing edge values.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNU {
	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class CSTNUCheckStatus {
		/**
		 * True if the network is controllable so far.
		 */
		boolean controllable = true;
		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings("javadoc")
		int cycles = 0, r0calls = 0, r1calls = 0, r2calls = 0, r3calls = 0, stdRuleCalls = 0,
				upperCaseRuleCalls = 0, lowerCaseRuleCalls = 0, crossCaseRuleCalls = 0, caseLabelRemovalRuleCalls = 0;

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
			return ("The check is" + (this.finished ? " " : " NOT") + " finished after " + this.cycles + " cycle(s).\n"
					+ ((this.finished) ? "the consistency check has determined that given network is" + (this.controllable ? " " : " NOT ")
							+ " dynamic controllable.\n" : "")
					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
					+ "Rule R1 has been applied " + this.r1calls + " times.\n"
					+ "Rule R2 has been applied " + this.r2calls + " times.\n"
					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
					+ "Standard Rule has been applied " + this.stdRuleCalls + " times.\n"
					+ "Upper Case Rule has been applied " + this.upperCaseRuleCalls + " times.\n"
					+ "Lower Case Rule has been applied " + this.lowerCaseRuleCalls + " times.\n"
					+ "Cross Case Rule has been applied " + this.crossCaseRuleCalls + " times.\n"
					+ "Case Removal Rule has been applied " + this.caseLabelRemovalRuleCalls + " times.\n"
					+ "The global execution time has been " + executionTime + " ms. approx.");
		}
	}

	/**
	 * Flag to activate optimization of labeled values.
	 */
	boolean labelOptimization = true;

	/**
	 * Flag to activate instantaneous reaction.
	 */
	boolean instantaneousReaction = false;// TODO it cannot be true till we fix some issue about the equivalence between instantaneousReaction in Morris
						// and instantaneousReaction in rules and R0-R3

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTNU.class.getName());

	/**
	 * Shortcut
	 */
	static int nullInt = LabeledIntNodeSetMap.INT_NULL;

	/**
	 * The name for the reference node.
	 */
	private static final String ZeroNodeName = "Z";

	/**
	 * Constructor for CSTNU.
	 *
	 * @param withOptimization
	 *                true if propagation rules have also to remove redundant labeled values or redundant propositions in labels.
	 * @param withInstantaneousReaction
	 *                true if propagation rules have to assume that CSTN instances are executed by engines that can react instantaneously to observations of
	 *                propositions. TODO It must be false for now!
	 */
	public CSTNU(final boolean withOptimization, final boolean withInstantaneousReaction) {
		this.labelOptimization = withOptimization;
		if (withInstantaneousReaction)
			throw new IllegalArgumentException("Instantaneous Reaction still not implemented");
		this.instantaneousReaction = withInstantaneousReaction;
	}

	/**
	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
	 *
	 * @param g
	 *                the original graph that has to be checked. If the check is successful, g is modified and it contains all minimized constraints;
	 *                otherwise, it is not modified.
	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
	 * @throws it.univr.di.cstnu.WellDefinitionException
	 *                 if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	public CSTNUCheckStatus dynamicControllabilityCheck(final LabeledIntGraph g) throws WellDefinitionException {
		final CSTNUCheckStatus status = new CSTNUCheckStatus();
		if (g == null)
			return status;

		String originalName = g.getName();

		LabeledIntGraph currentGraph, nextGraph, distanceGraph;
		currentGraph = new LabeledIntGraph(g, this.labelOptimization);
		currentGraph.setName("Current graph");
		try {
			initUpperLowerLabelDataStructure(currentGraph);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		final int n = currentGraph.getVertexCount();
		int k = currentGraph.getUpperLabeledEdges().size();
		if (k == 0) {
			k = 1;
		}
		int p = currentGraph.getPropositions().size();
		if (p == 0) {
			p = 1;
		}

		nextGraph = new LabeledIntGraph(currentGraph, this.labelOptimization);
		nextGraph.setName("New graph");
		distanceGraph = new LabeledIntGraph(this.labelOptimization);

		int maxCycles = p * ((n * n) + (n * k) + k);// cstnu
		// int maxCycles = (int) (Math.pow(2,p) * Math.pow(n, 3) *
		// -getSumOfNegativeEdgeValues(g) );//cstn
		// int maxCycles = -CSTNU.getSumOfNegativeEdgeValues(g) * n;// cstn
		if (maxCycles == 0) {
			maxCycles = 2;
		}
		CSTNU.LOG.info("The maximum number of possible cycles is " + maxCycles);

		int i;

		long startTime = System.nanoTime();
		for (i = 1; i <= maxCycles && status.controllable && !status.finished; i++) {
			if (CSTNU.LOG.isLoggable(Level.FINE))
				CSTNU.LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
			oneStepDynamicControllability(currentGraph, nextGraph, distanceGraph, status);
			if (!status.controllable)
				continue;
			status.finished = currentGraph.hasSameEdgesOf(nextGraph);
			if (status.controllable && !status.finished) {
				currentGraph.copy(nextGraph);
				nextGraph.setName("nextGraph");
				currentGraph.setName("currentGraph");
			}
			if (CSTNU.LOG.isLoggable(Level.FINE))
				CSTNU.LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
		}
		status.executionTime = (System.nanoTime() - startTime) / 1000000;

		if (!status.controllable) {
			if (CSTNU.LOG.isLoggable(Level.INFO)) {
				CSTNU.LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an INCONTROLLABILITY.\nStatus: " + status);
				CSTNU.LOG.log(Level.FINER, "Final un-controllable graph: " + nextGraph);
			}
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		if (i > maxCycles && !status.finished) {
			if (CSTNU.LOG.isLoggable(Level.INFO)) {
				CSTNU.LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + status);
				CSTNU.LOG.log(Level.FINER, "Last determined graph: " + nextGraph);
			}
			status.controllable = status.finished;
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		// controllable && finished
		if (CSTNU.LOG.isLoggable(Level.INFO)) {
			CSTNU.LOG.log(Level.INFO, "Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles
					+ ".\nStatus: " + status);
		}
		nextGraph.copyCleaningRedundantLabels(currentGraph);
		// Put all data structures of currentGraph in g
		g.takeIn(nextGraph);
		g.setName(originalName);
		return status;
	}

	// /**
	// * <p>
	// * main.
	// * </p>
	// *
	// * @param args
	// * an array of {@link java.lang.String} objects.
	// */
	// public static void main(final String[] args) {

	// System.out.printf("UPPER CASE\n");
	// g = new LabeledIntGraph();
	// dc = new LabeledIntEdge("DC");
	// ca = new LabeledIntEdge("CA");
	// ca.mergeUpperLabelValue(Label.parse("AB"), "B", 3);
	// dc.mergeLabeledValue(Label.parse("B"), -13);
	// dc.mergeLabeledValue(Label.parse("C"), 11);
	// C = new LabeledNode("C");
	// g.addEdge(dc, new LabeledNode("D"), C);
	// g.addEdge(ca, C, new LabeledNode("A"));
	// g1 = new LabeledIntGraph(g);
	//
	// System.out.printf("G: %s\n", g);
	// System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
	// CSTNU.upperCaseRule(g, g1);
	// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	// System.out.printf("G: %s\n", g1);

	// System.out.printf("LOWER CASE\n");
	// g = new LabeledIntGraph();
	// dc = new LabeledIntEdge("DC");
	// ca = new LabeledIntEdge("CA");
	// dc.mergeLowerLabelValue(Label.parse("AB"), "c", 3);
	// ca.mergeLabeledValue(Label.parse("B"), -13);
	// ca.mergeLabeledValue(Label.parse("C"), -11);
	// C = new LabeledNode("C");
	// g.addEdge(dc, new LabeledNode("D"), C);
	// g.addEdge(ca, C, new LabeledNode("A"));
	// g1 = new LabeledIntGraph(g);
	//
	// System.out.printf("G: %s\n", g);
	// System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
	// CSTNU.lowerCaseRule(g, g1);
	// System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	// System.out.printf("G: %s\n", g1);
	//
	//
	// System.out.printf("CROSS CASE\n");
	// g = new LabeledIntGraph();
	// dc = new LabeledIntEdge("DC");
	// ca = new LabeledIntEdge("CA");
	// dc.mergeLowerLabelValue(Label.parse("AB"), "c", 3);
	// ca.mergeUpperLabelValue(Label.parse("B¬C"), "D", -3);
	// C = new LabeledNode("C");
	// g.addEdge(dc, new LabeledNode("D"), C);
	// g.addEdge(ca, C, new LabeledNode("A"));
	// g1 = new LabeledIntGraph(g);
	//
	// // System.out.printf("G: %s\n", g);
	// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
	// CSTNU.crossCaseRule(g, g1);
	// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	// System.out.printf("G: %s\n", g1);

	// System.out.printf("LABEL REMOVAL CASE\n");
	// g = new LabeledIntGraph();
	// final LabeledIntEdge ab = new LabeledIntEdge("AB");
	// ca = new LabeledIntEdge("CA");
	// ca.mergeUpperLabelValue(Label.parse("AB"), "B", 3);
	// ca.mergeUpperLabelValue(Label.parse("¬B"), "B", 4);
	// ab.mergeLowerLabelValue(Label.parse("B"), "b", 13);
	// final LabeledNode A = new LabeledNode("A");
	// g.addEdge(ab, A, new LabeledNode("B"));
	// g.addEdge(ca, new LabeledNode("C"), A);
	// g1 = new LabeledIntGraph(g);
	//
	// // System.out.printf("G: %s\n", g);
	// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
	// CSTNU.caseLabelRemovalRule(g, g1);
	// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	// // System.out.printf("G: %s\n", g1);
	//

	// System.out.printf("R1-R3 CASE\n");
	// g = new LabeledIntGraph();
	// LabeledNode P = new LabeledNode("P", new Literal('p'));
	// LabeledNode X = new LabeledNode("X");
	// LabeledNode Y = new LabeledNode("Y");
	// LabeledIntEdge px = new LabeledIntEdge("PX");
	// px.mergeLabeledValue(Label.parse("AB"), -10);
	// px.mergeUpperLabelValue(Label.parse("AB"), "C", -11);
	// LabeledIntEdge yx = new LabeledIntEdge("YX");
	// yx.mergeLabeledValue(Label.parse("BGp"), -4);
	// yx.mergeUpperLabelValue(Label.parse("BGp"), "C", -7);
	// yx.mergeUpperLabelValue(Label.parse("BG¬p"), "C", -4);
	//
	// LabeledIntEdge xy = new LabeledIntEdge("XY");
	// xy.mergeLabeledValue(Label.parse("BGp"), 5);
	// xy.mergeUpperLabelValue(Label.parse("BGp"), "C", 8);
	// xy.mergeUpperLabelValue(Label.parse("BG¬p"), "C", 9);
	//
	// g.addEdge(px, P, X);
	// g.addEdge(yx, Y, X);
	// g.addEdge(xy, X, Y);
	// g1 = new LabeledIntGraph(g);
	//
	// System.out.printf("G: %s\n", g);
	// // System.out.printf("G1.hasSameEdge(G): %s\n", g1.hasAllEdgesOf(g));
	// CSTNU.labelModificationR1R2R3(g);
	// // System.out.printf("G.hasSameEdge(G1): %s\n", g.hasAllEdgesOf(g1));
	// System.out.printf("G: %s\n", g);
	// }

	// /**
	// * Determines the minimal distance between all pair of vertexes modifying
	// * the given graph if there is no negative cycle.<br>
	// * This method uses the
	// * {@link LabeledIntNodeSetTreeMap#summedTo(LabeledIntNodeSetTreeMap)} in
	// * order to determine the right labels for the edge.
	// *
	// * @param g
	// * the graph
	// * @return true if the matrix represents the minimal distance between all
	// * pairs of nodes, false if there is a negative cycle at least. If
	// * the response is false, the matrix does not represent a distance
	// * graph!
	// */
	// // static public boolean minimalDistanceGraph(final LabeledIntGraph g) {
	// final int n = g.getVertexCount();
	// final LabeledNode[] node = g.getVerticesArray();
	// LabeledNode iV, jV, kV;
	// LabeledIntEdge ik, kj, ij;
	// LabeledIntNodeSetTreeMap newLabelSet;
	//
	// for (int k = 0; k < n; k++) {
	// kV = node[k];
	// for (int i = 0; i < n; i++) {
	// iV = node[i];
	// for (int j = 0; j < n; j++) {
	// if ((k == i) && (i == j)) {
	// continue;
	// }
	// jV = node[j];
	// ik = g.findEdge(iV, kV);
	// kj = g.findEdge(kV, jV);
	// if ((ik == null) || (kj == null)) {
	// continue;
	// }
	//
	// newLabelSet = (LabeledIntNodeSetTreeMap) ((LabeledIntNodeSetTreeMap)
	// ik.getLabeledValueMap()).summedTo(kj.getLabeledValueMap());
	// if (newLabelSet.size() > 0) {
	// ij = g.findEdge(iV, jV);
	// if (ij == null) {
	// ij = new LabeledIntEdge("e" + node[i].getName() + node[j].getName(),
	// LabeledIntEdge.Type.derived, CSTNU.labelOptimization);
	// g.addEdge(ij, iV, jV);
	// }
	// ij.mergeLabeledValue(newLabelSet);
	// if (i == j) // check negative cycles
	// if (newLabelSet.isThereNegativeValues()) return false;
	// }
	// }
	// }
	// }
	// return true;
	// }

	/**
	 * Determines the minimal distance between all pairs of vertexes of the given graph if the graph does not contain any negative cycles. If the graph
	 * contains a negative cycle, the method stops and returns false (the graph is anyway modified).
	 *
	 * @param g
	 *                the graph
	 * @return true if the input graph does not contain any negative cycle, false otherwise.
	 */
	public boolean minimalDistanceGraphFast(final LabeledIntGraph g) {
		final int n = g.getVertexCount();
		final LabeledNode[] node = g.getVerticesArray();
		LabeledNode iV, jV, kV;
		LabeledIntEdge ik, kj, ij;
		int v;
		Label l;
		LabeledIntNodeSetTreeMap ijMap = null;
		for (int k = 0; k < n; k++) {
			kV = node[k];
			for (int i = 0; i < n; i++) {
				iV = node[i];
				for (int j = 0; j < n; j++) {
					if ((k == i) && (i == j)) {
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

					final Set<Object2IntMap.Entry<Label>> ikMap = ik.labeledValueSet();
					final Set<Object2IntMap.Entry<Label>> kjMap = kj.labeledValueSet();
					if ((k == i) || (k == j)) {
						ijMap = new LabeledIntNodeSetTreeMap(ij.getLabeledValueMap(), g.isOptimize());// this is necessary to avoid
																// concurrent access to the
																// same map by the iterator.
					} else {
						ijMap = null;
					}
					for (final Object2IntMap.Entry<Label> ikL : ikMap) {
						for (final Object2IntMap.Entry<Label> kjL : kjMap) {
							l = ikL.getKey().conjunction(kjL.getKey());
							if (l == null) {
								continue;
							}
							l = l.conjunction(nodeLabelConjunction);// It is necessary to propagate with node labels!
							if (l == null) {
								continue;
							}
							if (ij == null) {
								ij = new LabeledIntEdge("e" + node[i].getName() + node[j].getName(),
										LabeledIntEdge.Type.derived, labelOptimization);
								g.addEdge(ij, iV, jV);
							}
							v = ikL.getValue() + kjL.getValue();
							if (ijMap != null) {
								ijMap.put(l, v);
							} else {
								ij.mergeLabeledValue(l, v);
							}
							if (i == j) // check negative cycles
								if (v < 0 || ij.getMinValue() < 0) {
									CSTNU.LOG.finer("Found a negative cycle on node " + iV.getName() + ": "
											+ ((ijMap != null) ? ijMap : ij) + "\nIn details, ik=" + ik + ", kj="
											+ kj + ",  v=" + v + ", ij.getValue(" + l + ")=" + ij.getValue(l));
									return false;
								}
						}
					}
					if (ijMap != null) {
						ij.setLabeledValue(ijMap);
					}
				}
			}
		}
		return true;
	}

	// /**
	// * Checks the controllability of a labeled STNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints.
	// * This method does not simplify
	// * @param g
	// * the original graph that has to be checked. If the check is successful, g is modified and it contains all minimized constraints;
	// * otherwise, it is not modified.
	// * @return true the graph is dynamically controllable, false otherwise.
	// */
	// public boolean stnuRules(final LabeledIntGraph g) {
	// if (g == null)
	// return false;
	//
	// LabeledIntGraph currentGraph, nextGraph, distanceGraph;
	// currentGraph = new LabeledIntGraph(g, labelOptimization);
	// try {
	// initUpperLowerLabelDataStructure(currentGraph);
	// } catch (final IllegalArgumentException e) {
	// throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
	// }
	// final int n = currentGraph.getVertexCount();
	// int k = currentGraph.getUpperLabeledEdges().size();
	// if (k == 0) {
	// k = 1;
	// }
	// int p = currentGraph.getPropositions().size();
	// if (p == 0) {
	// p = 1;
	// }
	// int i;
	//
	// CSTNUCheckStatus status = new CSTNUCheckStatus();
	//
	// distanceGraph = CSTNU.makeAllMaxProjection(currentGraph);
	// CSTNU.LOG.finer("Initial All Max Projection check.\n");
	// if (!minimalDistanceGraphFast(distanceGraph)) {
	// CSTNU.LOG.info("The initial distance graph has negative cycles: stop!");
	// return false;
	// }
	// CSTNU.LOG.finer("done");
	//
	// boolean reductionApplied = false;
	// nextGraph = new LabeledIntGraph(currentGraph, true);
	// long startTime = System.nanoTime();
	// for (i = 1; i <= (n * k * p); i++) {
	// reductionApplied = false;
	// CSTNU.LOG.info("*** Cycle " + i + " ***");
	// CSTNU.LOG.finer("Rules phase...");
	// reductionApplied = noCaseRule(currentGraph, nextGraph, status) ? true : reductionApplied;
	// reductionApplied = upperCaseRule(currentGraph, nextGraph, status) ? true : reductionApplied;
	// reductionApplied = crossCaseRule(currentGraph, nextGraph, status) ? true : reductionApplied;
	// reductionApplied = lowerCaseRule(currentGraph, nextGraph, status) ? true : reductionApplied;
	// reductionApplied = caseLabelRemovalRule(currentGraph, nextGraph, status) ? true : reductionApplied;
	// CSTNU.LOG.finer("done.");
	// currentGraph = new LabeledIntGraph(nextGraph, true);
	// status.cycles++;
	// if (!reductionApplied) {
	// status.finished = true;
	// CSTNU.LOG.finer("No more reductions applied at cycle " + i);
	// break;
	// }
	// }
	//
	// distanceGraph = CSTNU.makeAllMaxProjection(currentGraph);
	// CSTNU.LOG.finer("All Max Projection check.\n");
	// if (!minimalDistanceGraphFast(distanceGraph)) {
	// CSTNU.LOG.info("The all max projection graph has negative cycles: stop!\n");
	// status.controllable = false;
	// status.executionTime = (System.nanoTime() - startTime) / 1000000;
	// return false;
	// }
	// status.executionTime = (System.nanoTime() - startTime) / 1000000;
	// CSTNU.LOG.info("Stable state reached. Number of minimization cycles: " + ((i > (n * k * p)) ? (i - 1) : i));
	// // Put all data structures of currentGraph in g
	// CSTNU.LOG.finer("Original graph: " + g);
	// CSTNU.LOG.finer("Determined graph: " + currentGraph);
	// g.takeIn(currentGraph);
	// status.controllable = true;
	// return true;
	// }

	/**
	 * Apply Morris Label Removal Reduction (see page 1196 of the article MM2005).
	 *
	 * <pre>
	 *     l_1, b, x    l_2, B, z                    l_1 l_2, z
	 * B  &lt;---------A &lt;----------C and z&ge;-x adds A &lt;-----------C
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a reduction is applied at least
	 */
	@SuppressWarnings("static-method")
	boolean caseLabelRemovalRule(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}

		CSTNU.LOG.finer("Label Removal Rule: start.");
		final Set<LabeledIntEdge> lowerCaseEdge = currentGraph.getLowerLabeledEdges();
		/*
		 * I use the same node/edge ids of the Morris paper: A, B, C for nodes, and CA, AB for edges.
		 */
		LabeledNode A, B;
		boolean reductionApplied = false;
		Set<Object2IntMap.Entry<Entry<Label, String>>> ABMap;
		LabeledIntEdge BA;

		for (final LabeledIntEdge AB : lowerCaseEdge) {
			ABMap = AB.getLowerLabelSet();
			if (ABMap.size() == 0)
				continue;
			A = currentGraph.getSource(AB);
			B = currentGraph.getDest(AB);
			BA = currentGraph.findEdge(B, A);

			final Set<LabeledIntEdge> CAEdgesWithUpperCaseLabel = new ObjectArraySet<>();
			for (final LabeledIntEdge e : currentGraph.getInEdges(A)) {
				if ((e.getUpperLabelSet().size() > 0) && !currentGraph.getSource(e).equalsByName(B)) {
					CAEdgesWithUpperCaseLabel.add(e);
				}
			}
			for (final LabeledIntEdge CA : CAEdgesWithUpperCaseLabel) {
				final LabeledIntEdge CAInNext = nextGraph.findEdge(nextGraph.getNode(currentGraph.getSource(CA).getName()),
						nextGraph.getNode(currentGraph.getDest(CA).getName()));
				if (CAInNext == null)
					throw new IllegalArgumentException("The edge CAInNext cannot be null!");
				for (final Object2IntMap.Entry<Entry<Label, String>> upperCaseEntryOfCA : CA.getUpperLabelSet()) {
					final String upperCaseNodeName = upperCaseEntryOfCA.getKey().getValue();
					if (!upperCaseNodeName.equals(B.getName()))
						continue;// Rule condition!
					final Label l2 = upperCaseEntryOfCA.getKey().getKey();
					final int z = upperCaseEntryOfCA.getValue();

					for (final Object2IntMap.Entry<Entry<Label, String>> lowerCaseEntryOfAB : ABMap) {
						final Label l1 = lowerCaseEntryOfAB.getKey().getKey();

						int x = lowerCaseEntryOfAB.getValue();
						if (x == Constants.INT_NULL || z < -x)
							continue;

						// With guarded link it is not sufficient to be lower than the lower case value (that represents the guard).
						int x1 = BA.getValue(l1);
						if (x1 == Constants.INT_NULL || z < x1) {
							if (LOG.isLoggable(Level.FINEST)) {
								LOG.log(Level.FINEST, "Case Label Removal not applied to edge " + CA 
										+ ": lower bound of " + BA + " is greater than " + z);
							}
							continue;
						}
						final Label l1l2 = l1.conjunction(l2);
						if (l1l2 == null)
							continue;
						final int oldZ = CAInNext.getValue(l1l2);
						final String oldCA = CA.toString();

						if (CAInNext.mergeLabeledValue(l1l2, z)) {
							reductionApplied = true;
							status.caseLabelRemovalRuleCalls++;
							CSTNU.LOG.finer("Case Label Removal applied to edge " + oldCA + ":\n" + "partic: " + B.getName()
									+ " <---(" + l1 + ", "
									// + ((x < 0) ? "" : lowerCaseEntryOfAB.getKey().getValue().toLowerCase()) + ", " + x
									+ (lowerCaseEntryOfAB.getKey().getValue().toLowerCase()) + ", " + x
									+ ")--- " + A.getName() + " <---(" + l2 + ", " + upperCaseNodeName.toUpperCase() + ", "
									+ z + ")--- " + currentGraph.getSource(CA).getName() + "\nresult: " + A.getName()
									+ " <---(" + l1l2 + ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", " + z + ")--- "
									+ currentGraph.getSource(CA).getName() + "; oldValue: " + oldZ);
							if (z != oldZ) {
								CSTNU.LOG.finer("LabeledIntEdge " + CA.getName() + " after the rule: " + CA.toString());
							}
						}
					}
				}
			}
		}
		CSTNU.LOG.finer("Label Removal Rule: end.");
		return reductionApplied;
	}

	/**
	 * Checks whether the new label to add or to use to replace another one subsumes the conjunction of labels of endpoint of the constraint.
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
			CSTNU.LOG.warning("One parameter is null. source: " + source + ", dest: " + dest + ", new label to add: " + newLabel + ", edgeName: "
					+ edgeName + ". Please, check parameter.");
			return false;
		}
		final Label labelConjunction = source.getLabel().conjunction(dest.getLabel());
		if (!newLabel.subsumes(labelConjunction)) {
			CSTNU.LOG.warning("Subsumption check for a label generated by rule " + ruleName + " on edge " + edgeName + ".\nThe new label, '"
					+ newLabel + "', does not subsume the conjunction of node labels '" + labelConjunction + "'.\nIt is reject!");
			return false;
		}
		return true;
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property:<br>
	 * any labeled valued of the edge is consistent and subsumes both labels of two endpoints.
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
			CSTNU.LOG.warning("One parameter is null at least. Please, check parameter.");
			return false;
		}

		final Label labelConjunction = tail.getLabel().conjunction(head.getLabel());
		if (labelConjunction == null) {
			CSTNU.LOG.warning("Two endpoints don not allow any constraint because the have inconsisten labels.");
			throw new WellDefinitionException("Two endpoints don not allow any constraint because the have inconsisten labels.",
					WellDefinitionException.Type.LabelInconsistent);
		}
		// check the ordinary labeled values
		for (final Object2IntMap.Entry<Label> entry : e.getLabeledValueMap().entrySet()) {
			if (!entry.getKey().subsumes(labelConjunction)) {
				CSTNU.LOG.warning("Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				throw new WellDefinitionException(
						"Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
						WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the upper case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				CSTNU.LOG.warning("Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				throw new WellDefinitionException(
						"Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
						WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the lower case labeled values
		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getLowerLabelSet()) {
			if (!entry.getKey().getKey().subsumes(labelConjunction)) {
				CSTNU.LOG.warning("Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.");
				throw new WellDefinitionException(
						"Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.",
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
	 * @param g
	 *                the current graph containing the node.
	 * @param node
	 *                the current node to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition2Property(final LabeledIntGraph g, final LabeledNode node) throws WellDefinitionException {
		if ((g == null) || (node == null)) {
			CSTNU.LOG.warning("One parameter is null at least. Please, check parameter.");
			return false;
		}

		final Label nodeLabel = node.getLabel();
		if (nodeLabel.isEmpty())
			return true;

		// check the observation node
		for (final Literal l : nodeLabel.getAllAsStraight()) {
			final LabeledNode obs = g.getObservator(l);

			if (obs == null) {
				CSTNU.LOG.warning("Observation node of literal " + l + " of node " + node + " does not exist.");
				throw new WellDefinitionException("Observation node of literal " + l + " of node " + node + " does not exist.",
						WellDefinitionException.Type.ObservationNodeDoesNotExist);
			}

			final Label obsLabel = obs.getLabel();
			if (!nodeLabel.subsumes(obsLabel)) {
				CSTNU.LOG.warning("Label of node " + node + " does not subsume label of obs node " + obs);
				throw new WellDefinitionException("Label of node " + node + " does not subsume label of obs node " + obs,
						WellDefinitionException.Type.LabelNotSubsumes);
			}

			final LabeledIntEdge e = g.findEdge(node, obs);
			if ((e == null) || (e.getMinValue() == CSTNU.nullInt) || (e.getMinValue() > 0)) {
				CSTNU.LOG.warning("There is no constraint to execute obs node " + obs + " before node " + node);
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
	 * @param g
	 *                the current graph containing the node.
	 * @param e
	 *                the current edge to check.
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinition3Property(final LabeledIntGraph g, final LabeledIntEdge e) throws WellDefinitionException {
		if ((g == null) || (e == null)) {
			CSTNU.LOG.warning("One parameter is null at least. Please, check parameter.");
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
					CSTNU.LOG.warning("Observation node of literal " + l + " present in label " + edgeLabel + " of edge " + e
							+ " does not exist.");
					throw new WellDefinitionException("Observation node of literal " + l + " present in label " + edgeLabel + " of edge "
							+ e + " does not exist.", WellDefinitionException.Type.ObservationNodeDoesNotExist);
				}

				final Label obsLabel = obs.getLabel();
				if (!edgeLabel.subsumes(obsLabel)) {
					CSTNU.LOG.warning("Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs);
					throw new WellDefinitionException("Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs,
							WellDefinitionException.Type.LabelNotSubsumes);
				}
			}
		}
		return true;
	}

	/**
	 * @param g
	 * @return true if the g is a CSTNU well defined.
	 * @throws WellDefinitionException
	 */
	static boolean checkWellDefinitionProperties(final LabeledIntGraph g) throws WellDefinitionException {
		boolean flag = false;
		CSTNU.LOG.info("Checking if graph is well defined...");
		for (final LabeledIntEdge e : g.getEdges()) {
			flag = CSTNU.checkWellDefinition1Property(g.getSource(e), g.getDest(e), e);
			flag = flag && CSTNU.checkWellDefinition3Property(g, e);
		}
		for (final LabeledNode node : g.getNodes()) {
			flag = flag && CSTNU.checkWellDefinition2Property(g, node);
		}
		CSTNU.LOG.info(((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well defined graph!\n"));
		return flag;
	}

	/**
	 * Apply Morris cross case reduction (see page 1196 of the article).
	 *
	 * <pre>
	 *      l1, B:x        l2, c:y                          l1l2, B:x+y
	 * A  &lt;-----------C &lt;----------D and x&le;0, B!=C adds A &lt;-------------D
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a reduction is applied at least
	 */
	boolean crossCaseRule(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}

		final Set<LabeledIntEdge> lowerCaseEdges = currentGraph.getLowerLabeledEdges();
		/*
		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
		 */
		LabeledNode A, C, D;
		LabeledIntEdge DA;
		boolean reductionApplied = false;
		Set<Object2IntMap.Entry<Entry<Label, String>>> DCMap;

		CSTNU.LOG.finer("Cross Case Rule: start.");
		for (final LabeledIntEdge DC : lowerCaseEdges) {
			DCMap = DC.getLowerLabelSet();
			if (DCMap.size() == 0)
				continue;
			D = currentGraph.getSource(DC);
			C = currentGraph.getDest(DC);

			// Select only Upper Case edges from C and containing one negative value at least (in the following it is
			// necessary to check that Upper Label different from C
			final Set<LabeledIntEdge> upperCaseEdgeFromC = new ObjectArraySet<>();
			int v;
			for (final LabeledIntEdge e : currentGraph.getOutEdges(C)) {
				if (((v = e.getMinUpperLabeledValue()) != CSTNU.nullInt) && (v <= 0)) {// FIXME: if instantaneousReaction, v < 0 ?
					upperCaseEdgeFromC.add(e);
				}
			}

			for (final LabeledIntEdge upperCaseEdge : upperCaseEdgeFromC) {
				A = currentGraph.getDest(upperCaseEdge);

				for (final Object2IntMap.Entry<Entry<Label, String>> entryLabelUpperCaseEdge : upperCaseEdge.getUpperLabelSet()) {
					final String upperCaseNodeName = entryLabelUpperCaseEdge.getKey().getValue();
					if (upperCaseNodeName.equalsIgnoreCase(C.getName()))
						continue;// Rule condition!

					final int x = entryLabelUpperCaseEdge.getValue();
					if (x > 0)
						continue; // Rule condition!
					// if (x == 0 && C.equalsByName(A))//FIXME: It seems that <= 0 is not correct.
					// continue; // Rule condition!
					final Label l1 = entryLabelUpperCaseEdge.getKey().getKey();

					DA = nextGraph.findEdge(nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));
					for (final Object2IntMap.Entry<Entry<Label, String>> entryLowerCaseEdgeToC : DCMap) {
						final int y = entryLowerCaseEdgeToC.getValue();
						final Label l2 = entryLowerCaseEdgeToC.getKey().getKey();
						final Label l1l2 = l1.conjunction(l2);
						if (l1l2 == null)
							continue;
						if (DA == null) {
							DA = new LabeledIntEdge("e" + D.getName() + A.getName(), LabeledIntEdge.Type.derived,
									this.labelOptimization);
							nextGraph.addEdge(DA, nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));
							// nextGraph.addUpperLabeledEdge(DA);
							CSTNU.LOG.finer("crossCaseRule: added edge " + DA.getName());

						}
						final int z = CSTNU.sumAndOverflowCheck(x, y, l1l2);
						final int oldZ = DA.getUpperLabelValue(l1l2, upperCaseNodeName);
						final String oldDA = DA.toString();

						if (DA.mergeUpperLabelValue(l1l2, upperCaseNodeName, z)) {
							reductionApplied = true;
							status.crossCaseRuleCalls++;
							CSTNU.LOG.finer("Cross Case applied to edge " + oldDA + ":\n partic: " + A.getName() + " <---(" + l1
									+ ", " + upperCaseNodeName.toUpperCase() + ", " + x + ")--- " + C.getName() + " <---("
									+ l2 + ", " + entryLowerCaseEdgeToC.getKey().getValue().toLowerCase() + ", " + y
									+ ")--- " + D.getName() + "\nresult: " + A.getName() + " <---(" + l1l2 + ", "
									+ upperCaseNodeName.toUpperCase() + ", " + z + ")--- " + D.getName() + "; oldValue: "
									+ oldZ);
							if (z != oldZ)
								CSTNU.LOG.finer("LabeledIntEdge " + DA.getName() + " after the ruel: " + DA.toString());
						}
					}
				}
			}
		}
		CSTNU.LOG.finer("Cross Case Rule: end.");
		return reductionApplied;
	}

	/**
	 * Initializes all data structures before the execution of controllability checking algorithm. This method has to be called before the application of
	 * any propagation/reduction rule. If the given instance does not contain a node named 'Z' (assumed to be the first node to be executed), the method
	 * adds it and all the necessary temporal for assuring that Z will be the first executed node.
	 * 
	 * @param g
	 *                a cstnu instance to check.
	 * @throws IllegalArgumentException
	 *                 if the graph is null or it is not well formed.
	 */
	public void initUpperLowerLabelDataStructure(final LabeledIntGraph g) throws IllegalArgumentException {
		if (g == null)
			throw new IllegalArgumentException("The graph is null!");

		g.clearCache();

		final SortedSet<LabeledIntEdge> edgeSet = new ObjectRBTreeSet<>(g.getEdges());
		for (final LabeledIntEdge e : edgeSet) {

			if (CSTNU.LOG.isLoggable(Level.FINEST))
				CSTNU.LOG.log(Level.FINEST, "Edge e: " + e);
			// Sanity check for the label:
			// set one label if endpoints have one and edge hasn't any.

			// WD1 is checked and adjusted here
			final LabeledNode s = g.getSource(e);
			final LabeledNode d = g.getDest(e);
			final Label conjunctLabel = s.getLabel().conjunction(d.getLabel());
			if (CSTNU.LOG.isLoggable(Level.FINEST)) {
				CSTNU.LOG.log(Level.FINEST, "Source label: " + s.getLabel() + "; dest label: " + d.getLabel() + " new label: " + conjunctLabel);
			}
			if (conjunctLabel == null) {
				CSTNU.LOG.warning("Found a inconsistent label between the endopoint of edge " + e + ". Edge removed!");
				g.removeEdge(e);
				continue;
			}
			if (!conjunctLabel.isEmpty()) {
				Label l1;
				for (Object2IntMap.Entry<Label> entry : e.labeledValueSet()) {
					l1 = entry.getKey();
					if (l1.conjunction(conjunctLabel) == null) {
						CSTNU.LOG.warning("Found a labeled value in " + e + " inconsistent with the conjunction of node labels, "
								+ conjunctLabel + ". Labeled value removed");
						e.removeLabel(l1);
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
				// The merge removed labels...
				g.removeEdge(e);
				continue;
			}

			// now I can check the WD3 property
			try {
				CSTNU.checkWellDefinition3Property(g, e);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (!e.isContingentEdge()) {
				continue;
			}
			/***
			 * Manage contingent link.
			 */
			/**
			 * From svn version 74 I try to introduce the management of guarded link (contingent link with different paired requirement
			 * constraints). The idea is to set the upper/lower case label if and only if there is NO upper/lower case label.
			 */
			final int initialValue = e.getMinValueConsistentWith(conjunctLabel);
			if (initialValue == CSTNU.nullInt)
				throw new IllegalArgumentException("Contingent edge " + e + " cannot be inizialized because it hasn't an initial value.");
			if (initialValue == 0 && !this.instantaneousReaction)
				throw new IllegalArgumentException(
						"Contingent edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0<x<y<∞.");

			// e.clearLabels();

			// if (caseLabel != null) {
			// // manage a wait contingent link
			// if (caseLabel.matches(Constants.upperCaseLabel)) {
			// e.mergeUpperLabelValue(l, caseLabel, initialValue);
			// } else {
			// CSTNU.LOG.warning("Contingent edge with a wait default label that
			// is lower case!");
			// e.mergeLowerLabelValue(l, caseLabel, initialValue);
			// }
			// continue;
			// }

			boolean insertUpperLowerCaseValue = false;
			LabeledIntEdge eInverted = g.findEdge(d, s);
			CSTNU.LOG.finer("Edge " + e + " is contingent. Found its companion: " + eInverted);
			if (eInverted == null) {
				// Since
				throw new IllegalArgumentException("Contingent edge " + e + " is alone. The companion contingent edge between " + d.getName()
						+ " and " + s.getName() + " does not exist. It must!");
				// eInverted = new LabeledIntEdge("e" + d.getName() + s.getName(), LabeledIntEdge.Type.derived, this.labelOptimization);
				// g.addEdge(eInverted, d, s);
				// CSTNU.LOG.warning("Edge " + e + " is contingent. Its companion is null, so a new one has been created: " + eInverted);
				// // in this case we can add the case label
				// insertUpperLowerCaseValue = true;
			} // else {
			if ((initialValue == eInverted.getMinValueConsistentWith(conjunctLabel))) {
				throw new IllegalArgumentException(
						"Contingent edge " + e
								+ " cannot have a bound equals to the bound of its companion. The two bounds [x,y] have to be 0<x<y<∞.");
			}
			if ((initialValue <= 0) && (eInverted.getLowerLabelValue(conjunctLabel, s) == CSTNU.nullInt)) {
				CSTNU.LOG.warning("Edge " + e + " is contingent with a negative value but the inverted " + eInverted
						+ " does not contain a lower case value: (" + conjunctLabel + ", " + s.getName() + ").");
				insertUpperLowerCaseValue = true;
			}
			if ((initialValue > 0) && (eInverted.getUpperLabelValue(conjunctLabel, d) == CSTNU.nullInt)) {
				CSTNU.LOG.warning("Edge " + e + " is contingent with a positive value but the inverted " + eInverted
						+ " does not contain an UPPER case value: (" + conjunctLabel + ", " + d.getName() + ").");
				insertUpperLowerCaseValue = true;
			}
			// }
			if (insertUpperLowerCaseValue) {
				if (initialValue <= 0) {
					eInverted.mergeLowerLabelValue(conjunctLabel, s, -initialValue);
					CSTNU.LOG.warning("Insert the lower label value: (" + conjunctLabel + ", " + s.getName().toLowerCase() + ", "
							+ (-initialValue) + ") to edge " + eInverted.getName());
				} else {
					eInverted.mergeUpperLabelValue(conjunctLabel, d, -initialValue);
					CSTNU.LOG.warning("Insert the upper label value: (" + conjunctLabel + ", " + s.getName().toUpperCase() + ", "
							+ (-initialValue) + ") to edge " + eInverted.getName());

				}
			}
		} // end edges for cycle

		// init two useful structures
		g.getLowerLabeledEdges();
		g.getPropositions();

		// Start of well definition and properties about nodes (w.r.t. the Z node)!
		LabeledNode Z = g.getZ();
		if (Z == null) {
			Z = g.getNode(CSTNU.ZeroNodeName);
			if (Z == null) {
				// We add by authority!
				Z = new LabeledNode(CSTNU.ZeroNodeName);
				Z.setX(0.0);
				Z.setY(0.0);
				g.addVertex(Z);
				if (CSTNU.LOG.isLoggable(Level.WARNING))
					CSTNU.LOG.log(Level.WARNING, "No " + CSTNU.ZeroNodeName + " node found: added!");
			}
			g.setZ(Z);
		}
		final SortedSet<LabeledNode> nodeSet = new ObjectRBTreeSet<>(g.getVertices());
		for (final LabeledNode node : nodeSet) {
			// Check that obs-node has no in its label the proposition observed!
			final Literal obs = node.getPropositionObserved();
			final Label label = node.getLabel();
			if (obs != null) {
				if (label.contains(obs)) {
					if (CSTNU.LOG.isLoggable(Level.WARNING))
						CSTNU.LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label
								+ "' of the observation node '" + node.getName() + "'. Removed!");
				}
				label.remove(obs);
			}

			try {
				CSTNU.checkWellDefinition2Property(g, node);
			} catch (final WellDefinitionException ex) {
				if (ex.getType() == Type.ObservationNodeDoesNotOccurBefore) {
					for (final Literal l1 : label.getAllAsStraight()) {
						final LabeledNode obsl1 = g.getObservator(l1);
						LabeledIntEdge e = g.findEdge(node, obsl1);
						if (e == null) {
							e = new LabeledIntEdge("e" + node.getName() + obsl1.getName(), LabeledIntEdge.Type.derived,
									this.labelOptimization);
							g.addEdge(e, node, obsl1);
							CSTNU.LOG.warning("It is necessary to add a preceding constraint between node '" + node.getName()
									+ "' and node '" + obsl1.getName() + "' to satisfy WD2.");
						}
						e.mergeLabeledValue(label, 0);
					}
				} else
					throw new IllegalArgumentException(ex.getMessage());
			}
		}

		if (!Z.getLabel().isEmpty()) {
			if (CSTNU.LOG.isLoggable(Level.WARNING))
				CSTNU.LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
			Z.setLabel(Label.emptyLabel);
		}
		// Now I assuring that each node has a edge to Z.
		for (final LabeledNode node : nodeSet) {
			if (node == Z) {
				continue;
			}
			LabeledIntEdge e = g.findEdge(node, Z);
			if (e == null) {
				e = new LabeledIntEdge("e" + node.getName() + Z.getName(), LabeledIntEdge.Type.derived, this.labelOptimization);
				g.addEdge(e, node, Z);
				CSTNU.LOG.info("It is necessary to add a preceding constraint between node '" + node.getName() + "' and node '" + Z.getName()
						+ "' because Z must be the first node.");
			}
			e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
		}
	}

	/**
	 * OLD VERSION; IT DOES NOT MANAGE ALL POSSIBLE CASES Applies the Negative QStar Rules, also known as R6.<br>
	 * In this method all values are negative! Determine the less negative value (max value) among the set of negative value built considering, for each
	 * edge ObservationNode-->Z, the max negative value regardless the label (min value).
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @return true if one rule has been applied one time at least.
	 */
	// static boolean negativeQStarR6(LabeledIntGraph currentGraph,
	// LabeledIntGraph nextGraph) {
	// if (currentGraph == null || nextGraph == null) {
	// CSTNU.LOG.info("One parameter is null. Game over");
	// return false;
	// }
	// final Map<Literal, LabeledNode> obsMap = currentGraph.getObservables();
	// if (obsMap == null) return false;
	// final Collection<LabeledNode> obsNodes = obsMap.values();
	//
	// // Check all obsNode-->Z edges and determine the less negative value
	// among the most negative one of each edge
	// LabeledNode Z = currentGraph.Z;
	// int maxOfMin = Integer.MIN_VALUE;
	// for (final LabeledNode obsNode : obsNodes) {
	// LabeledIntEdge eObsZ = currentGraph.findEdge(obsNode, Z);// initial check
	// of process guarantees that eObsZ always exists.
	// // Determine the max negative value
	// Integer minValue = eObsZ.getMinValue();
	//
	// if (minValue != null) {
	// if (maxOfMin < minValue) maxOfMin = minValue;
	// }
	// }
	// LOG.finest("The less negative values found among all most negative ones
	// present in edges Obs-->Z is " + maxOfMin);
	// if (maxOfMin < 0) {
	// boolean applied = false;
	// LabeledNode nextZ = nextGraph.Z;
	// // Add maxOfMin to each edge as unlabeled value
	// for (final LabeledNode obsNode : nextGraph.getObservables().values()) {
	// LabeledIntEdge eObsZ = nextGraph.findEdge(obsNode, nextZ);// initial
	// check of process guarantees that eObsZ always exists.
	// // Determine the max negative value
	// String logMessage = "R6 adds a labeled value to edge " + eObsZ
	// + "\nresult: "
	// + Z.getName() + " <---(" + obsNode.getLabel() + ", " + maxOfMin + ")--- "
	// + obsNode.getName();
	// if (eObsZ.mergeLabeledValue(obsNode.getLabel(), maxOfMin)) {
	// CSTNU.LOG.finer(logMessage);
	// R6calls++;
	// applied = true;
	// }
	// }
	// return applied;
	// }
	// return false;
	// }

	/**
	 * Applies rule R0 and R2: label containing a proposition that can be decided only in the future, is simplified removing such proposition.
	 *
	 * <pre>
	 * R0:
	 * P? --[a p,U,-w]--&gt; X adds in P? --[a,U,-w]--&gt; X when w &lt;0
	 * 
	 * R2:
	 * P? &lt;--[a p,U,w]-- X  adds in P? &lt;--[a,U,max{0,w}]-- X;
	 * 
	 * where:
	 * U can be ◇ or an upper letter.
	 * p can be the positive o the negative literal associated to proposition observed in P?.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param status
	 *                CSTNUCheckStatus representing the status of the checking algorithm.
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0R2(final LabeledIntGraph currentGraph, final CSTNUCheckStatus status) {

		boolean ruleApplied = false;
		final Map<Literal, LabeledNode> obsMap = currentGraph.getObservedAndObservator();
		if (obsMap == null)
			return false;
		final Collection<LabeledNode> obsNodes = obsMap.values();

		Literal p;
		Collection<LabeledIntEdge> outEdges, inEdges;
		CSTNU.LOG.finer("Label Modification R0 and R2: start.");
		for (final LabeledNode obsNode : obsNodes) {
			p = obsNode.getPropositionObserved();

			// R0 rule
			outEdges = currentGraph.getOutEdges(obsNode);
			LabeledNode destNode = null;
			for (final LabeledIntEdge edge : outEdges) {
				boolean r0Applied = false;
				// ordinary labels
				destNode = currentGraph.getDest(edge);
				final Set<Object2IntMap.Entry<Label>> edgeLabeledValueSet = new ObjectArraySet<>(edge.labeledValueSet());
				for (final Object2IntMap.Entry<Label> entryObs : edgeLabeledValueSet) {
					final int w = entryObs.getValue();
					if ((w > 0) || (instantaneousReaction && (w == 0))) {
						continue;
					}

					final Label l = entryObs.getKey();
					if (edge.getValue(l) == CSTNU.nullInt) {
						continue;// it is possible that in a previous cycle the
								// label has been removed.
					}
					if (l.getLiteralWithSameName(p) == null) {
						continue;
					}

					final Label labelWithouP = new Label(l);
					labelWithouP.remove(p);
					labelWithouP.remove(p.getComplement());

					// if (!checkNodeLabelsSubsumption(obsNode, destNode,
					// labelWithouP, edge.getName(), "R0")) {
					// // It means that 'X' label contains 'p', but node 'X'
					// must occur before P? => graph not well defined!
					// final String msg = "Rule R0: new label does not subsume
					// labels of P? and X. It means that P? or X label contains
					// 'p'." +
					// "\n***This is not possible. LabeledIntGraph not well
					// formed.***";
					// CSTNU.LOG.severe(msg);
					// throw new IllegalStateException(msg);
					// }

					// Prepare the log message now with old values of the edge.
					// If R0 modifies, then we can log it correctly.
					final String logMessage = "R0 adds a label to edge " + edge + ":\npartic: " + destNode.getName() + " <---(" + l + ", "
							+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")--- " + obsNode.getName() + "\nresult: "
							+ destNode.getName() + " <---(" + labelWithouP + ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", "
							+ w + ")--- " + obsNode.getName();

					edge.putLabeledValueToRemovedList(l, w);
					edge.removeLabel(l);
					status.r0calls++;
					r0Applied = true;
					if (edge.mergeLabeledValue(labelWithouP, w)) {
						CSTNU.LOG.finer(logMessage);
					}
				}

				// upper-case labels
				final Set<Object2IntMap.Entry<Entry<Label, String>>> edgeUpperLabeledValueSet = new ObjectArraySet<>(edge.getUpperLabelSet());
				for (final Object2IntMap.Entry<Entry<Label, String>> entryObs : edgeUpperLabeledValueSet) {
					final int w = entryObs.getValue();
					if ((w > 0) || (instantaneousReaction && (w == 0))) {
						continue;
					}

					final Label l = entryObs.getKey().getKey();

					final String nodeName = entryObs.getKey().getValue();
					if (edge.getUpperLabelValue(l, nodeName) == CSTNU.nullInt) {
						continue;// it is possible that in a previous cycle the
								// label has been removed.
					}
					if (l.getLiteralWithSameName(p) == null) {
						continue;
					}

					final Label labelWithouP = new Label(l);
					labelWithouP.remove(p);
					labelWithouP.remove(p.getComplement());

					// if (!checkNodeLabelsSubsumption(obsNode, destNode,
					// labelWithouP, edge.getName(), "R0")) {
					// // It means that 'X' label contains 'p', but node 'X'
					// must occur before P? => graph not well defined!
					// final String msg = "Rule R0: new label does not subsume
					// labels of P? and X. It means that P? or X label contains
					// 'p'." +
					// "\n***This is not possible. LabeledIntGraph not well
					// formed.***";
					// CSTNU.LOG.severe(msg);
					// throw new IllegalStateException(msg);
					// }

					// Prepare the log message now with old values of the edge.
					// If R0 modifies, then we can log it correctly.
					final String logMessage = "R0 adds a label to edge " + edge + ":\npartic: " + destNode.getName() + " <---(" + l + ", "
							+ nodeName + ", " + w + ")--- " + obsNode.getName() + "\nresult: " + destNode.getName() + " <---("
							+ labelWithouP + ", " + nodeName + ", " + w + ")--- " + obsNode.getName();

					edge.removeUpperLabel(l, nodeName);
					edge.putUpperLabeledValueToRemovedList(l, nodeName, w);
					status.r0calls++;
					r0Applied = true;
					if (edge.mergeUpperLabelValue(labelWithouP, nodeName, w)) {
						CSTNU.LOG.finer(logMessage);
					}
				}
				if (r0Applied) {
					CSTNU.LOG.finer("LabeledIntEdge " + edge.getName() + " after the rule R0: " + edge.toString());
					ruleApplied = true;
				}
			}

			// R2 rule
			inEdges = currentGraph.getInEdges(obsNode);
			for (final LabeledIntEdge edgeInModification : inEdges) {
				// CSTNU.LOG.finer("R2 to be applied to edge " + edge.getName()
				// + ": "+edge.toString());

				final LabeledNode sourceNode = currentGraph.getSource(edgeInModification);
				boolean r2Applied = false;
				// destNode == obsNode;
				// ordinary labels
				final Set<Object2IntMap.Entry<Label>> edgeLabeledValueSet = new ObjectArraySet<>(edgeInModification.labeledValueSet());
				for (final Object2IntMap.Entry<Label> entryObs : edgeLabeledValueSet) {
					final int w = entryObs.getValue();
					final Label l = entryObs.getKey();

					if (edgeInModification.getValue(l) == CSTNU.nullInt) {
						continue;// it is possible that in a previous cycle the
								// label has been
					}
					// removed thanks to label optimization.
					if (l.getLiteralWithSameName(p) == null) {
						continue;
					}

					final Label labelWithouP = new Label(l);
					labelWithouP.remove(p);
					labelWithouP.remove(p.getComplement());

					if (w > 0) {
						// R2 rule
						if (!CSTNU.checkNodeLabelsSubsumption(sourceNode, obsNode, labelWithouP, edgeInModification.getName(), "R2")) {
							// It means that 'X' label contains 'p'!
							// The labeled value has to be substituted by a 0 labeled value because by WD2 the node ha to be
							// after the observation node.
							CSTNU.LOG.finer("Details about subsumption check of R2 applied to edge " + edgeInModification
									+ ":\npartic: " + obsNode.getName() + " <---(" + l + ", "
									+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")--- " + sourceNode.getName()
									+ "\nresult: " + obsNode.getName() + " <---("
									+ sourceNode.getLabel().conjunction(obsNode.getLabel()) + ", "
									+ Constants.EMPTY_UPPER_CASE_LABELstring + ", 0)--- " + sourceNode.getName());

							edgeInModification.mergeLabeledValue(l, 0);
							continue;
						}

						// Prepare the log message now with old values of the
						// edge. If R2 modifies, then we can log it correctly.
						final String logMessage = "R2 adds a labels to edge " + edgeInModification + ":\npartic: " + obsNode.getName()
								+ " <---(" + l + ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")--- "
								+ sourceNode.getName() + "\nresult: " + obsNode.getName() + " <---(" + labelWithouP + ", "
								+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")--- " + sourceNode.getName();

						edgeInModification.putLabeledValueToRemovedList(l, w);
						edgeInModification.removeLabel(l);
						status.r2calls++;
						r2Applied = true;
						if (edgeInModification.mergeLabeledValue(labelWithouP, w)) {
							CSTNU.LOG.finer(logMessage);
						}
					} else {
						// w<=0
						// R2 special CASE
						if (!CSTNU.checkNodeLabelsSubsumption(sourceNode, obsNode, labelWithouP, edgeInModification.getName(), "R2")) {
							CSTNU.LOG.finer("R2 CANNOT be applied because node label to " + edgeInModification + ": "
									+ obsNode.getName() + " <---(" + l + ", " + Constants.EMPTY_UPPER_CASE_LABELstring
									+ ", " + w + ")--- (" + sourceNode.getName() + ", " + sourceNode.getLabel() + ")");
							continue;
						}

						// Prepare the log message now with old values of the
						// edge. If R2 modifies, then we can log it correctly.
						final String logMessage = "R2 adds a label to edge " + edgeInModification + ":\npartic: " + obsNode.getName()
								+ " <---(" + l + ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")--- "
								+ sourceNode.getName() + "\nresult: " + obsNode.getName() + " <---(" + l + ", "
								+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")" + "(" + labelWithouP + ", "
								+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + 0 + ")--- " + sourceNode.getName();

						if (edgeInModification.mergeLabeledValue(labelWithouP, 0)) {
							r2Applied = true;
							status.r2calls++;
							CSTNU.LOG.finer(logMessage);
						}
					}
				}

				// upper-case labels
				final Set<Object2IntMap.Entry<Entry<Label, String>>> edgeUpperLabeledValueSet = new ObjectArraySet<>(
						edgeInModification.getUpperLabelSet());
				for (final Object2IntMap.Entry<Entry<Label, String>> entryObs : edgeUpperLabeledValueSet) {
					final int w = entryObs.getValue();
					final Label l = entryObs.getKey().getKey();
					final String nodeName = entryObs.getKey().getValue();
					if (edgeInModification.getUpperLabelValue(l, nodeName) == CSTNU.nullInt) {
						continue;// it is possible that in a previous
					}
					// cycle the label has been
					// removed.
					if (l.getLiteralWithSameName(p) == null) {
						continue;
					}

					final Label labelWithouP = new Label(l);
					labelWithouP.remove(p);
					labelWithouP.remove(p.getComplement());

					if (w > 0) {
						// R2 rule
						if (!CSTNU.checkNodeLabelsSubsumption(sourceNode, obsNode, labelWithouP, edgeInModification.getName(), "R2")) {
							// It means that 'X' label contains 'p'!
							// The labeled value has to be substituted by a 0 labeled value because by WD2 the node ha to be
							// after the observation node.
							CSTNU.LOG.finer("Details about subsumption check of R2 to " + edgeInModification + ":\npartic: "
									+ obsNode.getName() + " <---(" + l + ", " + nodeName + ", " + w + ")--- "
									+ sourceNode.getName() + "\nresult: " + obsNode.getName() + " <---("
									+ sourceNode.getLabel().conjunction(obsNode.getLabel()) + ", " + nodeName + ", 0)--- "
									+ sourceNode.getName());

							edgeInModification.removeUpperLabel(l, nodeName);
							edgeInModification.putUpperLabeledValueToRemovedList(l, nodeName, w);
							edgeInModification.mergeUpperLabelValue(sourceNode.getLabel().conjunction(obsNode.getLabel()), nodeName,
									0);
						}

						// Prepare the log message now with old values of the
						// edge. If R2 modifies, then we can log it correctly.
						final String logMessage = "R2 adds a label to edge " + edgeInModification + ":\n" + "partic: "
								+ obsNode.getName() + " <---(" + l + ", " + nodeName + ", " + w + ")--- " + sourceNode.getName()
								+ "\nresult: " + obsNode.getName() + " <---(" + labelWithouP + ", " + nodeName + ", " + w
								+ ")--- " + sourceNode.getName();
						edgeInModification.removeUpperLabel(l, nodeName);
						edgeInModification.putUpperLabeledValueToRemovedList(l, nodeName, w);
						status.r2calls++;
						r2Applied = true;
						if (edgeInModification.mergeUpperLabelValue(labelWithouP, nodeName, w)) {
							CSTNU.LOG.finer(logMessage);
						}
					} else {
						// R2 special case
						if (!CSTNU.checkNodeLabelsSubsumption(sourceNode, obsNode, labelWithouP, edgeInModification.getName(), "R2")) {
							CSTNU.LOG.finer("Details because R2 cannot be applied to " + edgeInModification + obsNode.getName()
									+ " <---(" + l + ", " + nodeName + ", " + w + ")--- (" + sourceNode.getName() + ", "
									+ sourceNode.getLabel() + ")");
							continue;
						}
						// Prepare the log message now with old values of the
						// edge. If R2 modifies, then we can log it correctly.
						final String logMessage = "R2 applied to edge " + edgeInModification + ":\npartic: " + obsNode.getName()
								+ " <---(" + l + ", " + nodeName + ", " + w + ")--- " + sourceNode.getName() + "\nresult: "
								+ obsNode.getName() + " <---(" + l + ", " + nodeName + ", " + w + ")" + "(" + labelWithouP
								+ ", " + nodeName + ", " + 0 + ")--- " + sourceNode.getName();
						if (edgeInModification.mergeUpperLabelValue(labelWithouP, nodeName, 0)) {
							CSTNU.LOG.finer(logMessage);
							status.r2calls++;
							r2Applied = true;
						}
					}
				}
				if (r2Applied) {
					CSTNU.LOG.finer("LabeledIntEdge " + edgeInModification.getName() + " after the rule R2: "
							+ edgeInModification.toString());
					ruleApplied = true;
				}
			}
		}
		CSTNU.LOG.finer("Label Modification R0 and R2: end.");
		return ruleApplied;
	}

	/**
	 * Rule R1 remove propositions from labels of edge X--&gt;Y that cannot be evaluated when the edge has to be considered.
	 *
	 * <pre>
	 * if P? --[ab, ◇, w]--&gt; X --[bgp,U,v]--&gt; Y  and w&le;0 and v&lt;w,
	 * then the constraint between X and Y is modified as X --[abg,U,v]--[bgp,U,v]--&gt; Y,
	 * where U can be ◇ or an upper letter.
	 * </pre>
	 *
	 * @param obs
	 *                observation node
	 * @param nX
	 *                x node
	 * @param nY
	 *                y node
	 * @param eXY
	 *                LabeledIntEdge containing the constrain to modify
	 * @param eObsX
	 *                LabeledIntEdge connecting observation node and x.
	 * @param p
	 *                the Observation proposition (it is redundant... only to speed up the method)
	 * @param eXYnew
	 *                LabeledIntEdge that will contain the new determined labels.
	 * @param g
	 *                graph where to search observation nodes.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a rule has been applied.
	 */
	private boolean labelModificationR1Action(final LabeledNode obs, final LabeledNode nX, final LabeledNode nY, final LabeledIntEdge eXY,
			final LabeledIntEdge eObsX, final Literal p, final LabeledIntEdge eXYnew, final LabeledIntGraph g, CSTNUCheckStatus status) {
		if (eXY == null)
			return false;

		boolean ruleApplied = false;

		final Set<Object2IntMap.Entry<Label>> obs_xLabeledValueSet = new ObjectArraySet<>(eObsX.labeledValueSet());

		// CSTNU.LOG.finer("Label Modification R1: start.");
		CSTNU.LOG.finest("obs_xLabeledValueSet: " + obs_xLabeledValueSet.toString());
		// Merge all possible labeled values and Upper Case labeled values of
		// edges between Y and X in a single set.
		final Set<Object2IntMap.Entry<Entry<Label, String>>> x_yLabeledValueSet = eXY.getAllUpperCaseAndOrdinaryLabeledValuesSet();

		for (final Object2IntMap.Entry<Label> entryObs : obs_xLabeledValueSet) {
			final int w = entryObs.getValue();
			final Label l = entryObs.getKey();

			if ((w > 0) || (l.getLiteralWithSameName(p) != null)) {
				continue; // R1 works with negative w associated to a label
						// without 'p'.
			}

			for (final Object2IntMap.Entry<Entry<Label, String>> entryXY : x_yLabeledValueSet) {
				final int v = entryXY.getValue();
				// condition on v value and its relation with w.
				// w is surely <=0; v can be any value. R1 has to applied when
				// v<=-w (in case of instantaneous, v<-w). So, if v<=-w, then R1
				// is applied.
				if ((v > -w) || (instantaneousReaction && (v == -w))) {
					continue; // R1 cannot be applied
				}

				final Label l1 = new Label(entryXY.getKey().getKey());
				if (l1.getLiteralWithSameName(p) == null) {
					continue;
				}
				if (!l.isConsistentWith(l1)) {
					continue;
				}

				final String UCLabelXY = entryXY.getKey().getValue();
				boolean r1Applied = false;

				final Label l1WithoutP = new Label(l1);
				l1WithoutP.remove(p);
				l1WithoutP.remove(p.getComplement());
				final Label[] alphaBetaGammaPart = CSTNU.getAlphaBetaGamma(l, l1WithoutP, nX != g.getZ());
				final Label alpha = alphaBetaGammaPart[0];
				final Label beta = alphaBetaGammaPart[1];
				final Label gamma1 = CSTNU.getGammaCleaned(alphaBetaGammaPart[2], p, g);
				final Label abg1 = alpha.conjunction(beta).conjunction(gamma1);

				if (abg1 == null) {
					final String msg = "The label alpha beta gamma is null and it cannot be null: \n" + "alpha: " + alpha + "; beta: "
							+ beta + "; gamma': " + gamma1;
					CSTNU.LOG.severe(msg);
					throw new IllegalArgumentException(msg);
				}
				if (!CSTNU.checkNodeLabelsSubsumption(nX, nY, abg1, eXY.getName(), "R1")) {
					// I check if the label subsumes the label of the endpoints before to proceed.
					// It should not necessary, but I put here as a guard!
					CSTNU.LOG.finer("Detail about the error of application R1 to edge " + eXY + ":\npartic: " + obs.getName() + " ---(" + l
							+ ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")---> " + nX.getName() + " ---(" + l1
							+ ", " + UCLabelXY + ", " + v + ")---> " + nY.getName() + "\nresult: " + nX.getName() + " ---(" + abg1
							+ ", " + UCLabelXY + ", " + v + ")---> " + nY.getName());
					continue;
				}

				if (UCLabelXY.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
					eXYnew.putLabeledValueToRemovedList(l1, v);
					r1Applied = eXYnew.mergeLabeledValue(abg1, v);
				} else {
					eXYnew.putUpperLabeledValueToRemovedList(l1, UCLabelXY, v);
					r1Applied = eXYnew.mergeUpperLabelValue(abg1, UCLabelXY, v);
				}
				if (r1Applied) {
					CSTNU.LOG.finer("R1 adds a label to edge " + eXY + ":\npartic: " + obs.getName() + " ---(" + l + ", "
							+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")---> " + nX.getName() + " ---(" + l1 + ", "
							+ UCLabelXY + ", " + v + ")---> " + nY.getName() + "\nresult: " + nX.getName() + " ---(" + abg1 + ", "
							+ UCLabelXY + ", " + v + ")---> " + nY.getName());
					ruleApplied = true;
					status.r1calls++;
				}
				// CSTNU.LOG.finer("LabeledIntEdge " + eXYnew.getName() + "
				// after the R1 application to no case label: " +
				// eXYnew.toString());
				// FIXME: new version if (alpha.size() == 0) continue;
				// final Set<Label> notAlphaSet = getNotAlpha(alpha, g);
				// for (final Label notAlpha : notAlphaSet) {
				// abg1 = l1.conjunction(notAlpha);
				// r1Applied = false;
				// if (!checkNodeLabelsSubsumption(nX, nY, abg1, eXY.getName(),
				// "R1")) {
				// // I check if the label subsumes the label of the endpoints
				// before to proceed.
				// // It should not necessary, but I put here as a guard!
				// CSTNU.LOG.finer("Detail about the error of application R1 to
				// edge " + eXY
				// + ":\npartic: "
				// + obs.getName() + " ---(" + l + ", " +
				// Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")---> "
				// + nX.getName()
				// + " ---(" + l1 + ", " + UCLabelXY + ", " + v + ")---> " +
				// nY.getName()
				// + "\nresult: " + nX.getName() + " ---(" + abg1 + ", " +
				// UCLabelXY + ", " + v + ")---> " + nY.getName());
				// throw new IllegalStateException("Rule R1 cannot determine
				// label that does not subsume node labels!");
				// }
				// if (UCLabelXY.equals(Constants.EMPTY_UPPER_CASE_LABELstring))
				// {
				// r1Applied = eXYnew.mergeLabeledValue(abg1, v);
				// } else {
				// r1Applied = eXYnew.mergeUpperLabelValue(abg1, UCLabelXY, v);
				// }
				// if (r1Applied) {
				// CSTNU.LOG.finer("R1 applied to edge " + eXY
				// + ":\npartic: "
				// + obs.getName() + " ---(" + l + ", " +
				// Constants.EMPTY_UPPER_CASE_LABELstring + ", " + w + ")---> "
				// + nX.getName()
				// + " ---(" + l1 + ", " + UCLabelXY + ", " + v + ")---> " +
				// nY.getName()
				// + "\nresult: " + nX.getName() + " ---(" + abg1 + ", " +
				// UCLabelXY + ", " + v + ")---> " + nY.getName());
				// R1calls++;
				// ruleApplied = true;
				// }
				// }
			}
		}
		if (ruleApplied) {
			CSTNU.LOG.finer("LabeledIntEdge " + eXYnew.getName() + " after R1 application: " + eXYnew.toString());
		}
		// CSTNU.LOG.finer("Label Modification R1: end.");
		return ruleApplied;
	}

	/**
	 * Applies the rules R1 and R3 about the simplification of constraint having label decided in the future.<br>
	 * For about the rules, see javadoc of method
	 * {@link #labelModificationR1Action(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, Literal, LabeledIntEdge, LabeledIntGraph, CSTNUCheckStatus)}
	 * and
	 * {@link #labelModificationR3Action(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, Literal, LabeledIntEdge, LabeledIntGraph, CSTNUCheckStatus)}
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if one rule has been applied one time at least.
	 * @see CSTNU#labelModificationR1Action(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, Literal, LabeledIntEdge, LabeledIntGraph,
	 *      CSTNUCheckStatus)
	 * @see CSTNU#labelModificationR3Action(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, Literal, LabeledIntEdge,
	 *      LabeledIntGraph, CSTNUCheckStatus)
	 */
	boolean labelModificationR1R3(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}

		boolean ruleApplied = false;
		final Map<Literal, LabeledNode> obsMap = currentGraph.getObservedAndObservator();
		if (obsMap == null)
			return false;
		final Collection<LabeledNode> obsNodes = obsMap.values();

		CSTNU.LOG.finer("Label Modification R1 and R3: start.");
		for (final LabeledNode obsNode : obsNodes) {
			final Literal p = obsNode.getPropositionObserved();
			if (p == null)
				throw new IllegalArgumentException("The observation node " + obsNode + " has no specification of observed proposition!");
			for (final LabeledIntEdge eObsX : currentGraph.getOutEdges(obsNode)) { // node
												// X
				final LabeledNode nX = currentGraph.getDest(eObsX);
				if ((eObsX.getMinValue() == CSTNU.nullInt) || (eObsX.getMinValue() > 0)) {
					continue;// reduction rule
				}

				// R1 rules
				for (final LabeledIntEdge eXY : currentGraph.getOutEdges(nX)) {
					final LabeledNode nY = currentGraph.getDest(eXY);
					final LabeledIntEdge eXYnew = nextGraph.findEdge(nextGraph.getNode(nX.getName()), nextGraph.getNode(nY.getName()));
					ruleApplied = (labelModificationR1Action(obsNode, nX, nY, eXY, eObsX, p, eXYnew, currentGraph, status)) ? true
							: ruleApplied;
				}

				// R3 Rules
				for (final LabeledIntEdge eYX : currentGraph.getInEdges(nX)) {
					final LabeledNode nY = currentGraph.getSource(eYX);
					if (nY.equalsByName(obsNode)) {
						continue;
					}
					final LabeledIntEdge eYXnew = nextGraph.findEdge(nextGraph.getNode(nY.getName()), nextGraph.getNode(nX.getName()));
					ruleApplied = (labelModificationR3Action(obsNode, nX, nY, eYX, eObsX, p, eYXnew, currentGraph, status)) ? true
							: ruleApplied;
				}
			}
		}
		CSTNU.LOG.finer("Label Modification R1R3: end.");
		return ruleApplied;
	}

	/**
	 * Rule R3 requires to be considered in more cases than R1.
	 *
	 * <pre>
	 * if P? --[ab, U ,w]--&gt; X &lt;--[bgp, U1, v]-- Y  and (w &le;0 and v&ge;w)
	 * then the following constraint between Y and X is added
	 * X &lt;--[abg, U1, max{v,w}]--[bgp, U1, v]-- Y,
	 * when U is empty or is equal to U1.
	 * </pre>
	 *
	 * @param nObs
	 *                observation node
	 * @param nX
	 *                x node
	 * @param nY
	 *                y node
	 * @param eYX
	 *                LabeledIntEdge containing the constrain to modify
	 * @param eObsX
	 *                LabeledIntEdge connecting observation node and x.
	 * @param p
	 *                the Observation proposition (it is redundant... only to speed up the method)
	 * @param eYXnew
	 *                LabeledIntEdge that will contain the new determined labels.
	 * @param g
	 *                graph where to search observation points.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a rule has been applied.
	 */
	@SuppressWarnings("static-method")
	private boolean labelModificationR3Action(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nY, final LabeledIntEdge eYX,
			final LabeledIntEdge eObsX, final Literal p, final LabeledIntEdge eYXnew, final LabeledIntGraph g, CSTNUCheckStatus status) {
		if (eYX == null)
			return false;

		boolean ruleApplied = false;

		// Merge all possible labeled values and Upper Case labeled values of
		// edges between Obs and X in a single set.
		final Set<Object2IntMap.Entry<Entry<Label, String>>> obs_xLabeledValueSet = eObsX.getAllUpperCaseAndOrdinaryLabeledValuesSet();

		// Merge all possible labeled values and Upper Case labeled values of
		// edges between Y and X in a single set.
		final Set<Object2IntMap.Entry<Entry<Label, String>>> y_xLabeledValueSet = eYX.getAllUpperCaseAndOrdinaryLabeledValuesSet();

		// CSTNU.LOG.finest("Label Modification R3: start.");

		for (final Object2IntMap.Entry<Entry<Label, String>> entryObsX : obs_xLabeledValueSet) {
			final int w = entryObsX.getValue();
			if (w > 0) {
				continue; // R3 works with negative w.
			}

			final Label l = entryObsX.getKey().getKey();
			if (l.getLiteralWithSameName(p) != null) {
				continue; // R3 works with a w associated to a label without 'p'.
			}

			final String UCLabelObsX = entryObsX.getKey().getValue();

			for (final Object2IntMap.Entry<Entry<Label, String>> entryYX : y_xLabeledValueSet) {
				final int v = entryYX.getValue();

				final Label l1 = new Label(entryYX.getKey().getKey());
				if (l1.getLiteralWithSameName(p) == null) {
					continue;
				}
				if (!l.isConsistentWith(l1)) {
					continue;
				}

				final String UCLabelYX = entryYX.getKey().getValue();
				if (!UCLabelObsX.equals(Constants.EMPTY_UPPER_CASE_LABELstring) && !UCLabelObsX.equals(UCLabelYX)) {
					continue;
				}

				final Label l1WithoutP = new Label(l1);
				l1WithoutP.remove(p);
				l1WithoutP.remove(p.getComplement());
				final Label[] alphaBetaGammaPart = CSTNU.getAlphaBetaGamma(l, l1WithoutP, nX != g.getZ());
				final Label alpha = alphaBetaGammaPart[0];
				final Label beta = alphaBetaGammaPart[1];
				final Label gamma1 = CSTNU.getGammaCleaned(alphaBetaGammaPart[2], p, g);
				CSTNU.LOG.finest("Rule R3 details alpha=" + alpha);
				CSTNU.LOG.finest("Rule R3 details beta=" + beta);
				CSTNU.LOG.finest("Rule R3 details gamma1=" + gamma1);
				final Label abg1 = alpha.conjunction(beta).conjunction(gamma1);

				if (abg1 == null) {
					final String msg = "The label alpha beta gamma is null and it cannot be null: \n" + "alpha: " + alpha + "; beta: "
							+ beta + "; gamma: " + gamma1;
					CSTNU.LOG.severe(msg);
					throw new IllegalArgumentException(msg);
				}

				// I check if the label subsumes the label of the endpoints
				// before to proceed
				if (!CSTNU.checkNodeLabelsSubsumption(nY, nX, abg1, eYX.getName(), "R3")) {
					CSTNU.LOG.finer("Details because R3 cannot be applied to edge " + eYX + ":\n" + nObs.getName() + " ---(" + l + ", "
							+ UCLabelObsX + ", " + w + ")---> " + nX.getName() + " <---(" + l1 + ", " + UCLabelYX + ", " + v
							+ ")--- " + nY.getName());
					continue;
				}

				boolean r3Applied = false;
				// R3 RULE
				// v >= w (in case of instantaneous activations, the case v=w has been already sort out not allowing to reach this line.).
				if (v >= w) {
					// R3 rule
					if (UCLabelYX.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
						eYXnew.putLabeledValueToRemovedList(l1, v);
						r3Applied = eYXnew.mergeLabeledValue(abg1, v);
					} else {
						eYXnew.putUpperLabeledValueToRemovedList(l1, UCLabelYX, v);
						r3Applied = eYXnew.mergeUpperLabelValue(abg1, UCLabelYX, v);
					}
					if (r3Applied) {
						// R3 changed the edge
						CSTNU.LOG.finer("R3 adds a label to edge " + eYX + ":\n" + "partic: " + nObs.getName() + " ---(" + l + ", "
								+ UCLabelObsX + ", " + w + ")---> " + nX.getName() + " <---(" + l1 + ", " + UCLabelYX + ", " + v
								+ ")--- " + nY.getName() + "\nresult: " + nX.getName() + " <---(" + abg1 + ", " + UCLabelYX
								+ ", " + v + ")--- " + nY.getName());
						status.r3calls++;
						ruleApplied = true;
					}
					// FIXME: new version if (alpha.size() == 0) continue; // ||
					// v < w) continue;// v <w for R5 that has no a part with
					// negative alpha
					// final Set<Label> notAlphaSet = getNotAlpha(alpha, g);
					// r3Applied = false;
					// for (final Label notAlpha : notAlphaSet) {
					// abg1 = l1.conjunction(notAlpha);
					// // I check if the label subsumes the label of the
					// endpoints before to proceed
					// if (!checkNodeLabelsSubsumption(nY, nX, abg1,
					// eYX.getName(), "R3")) continue;
					// if
					// (UCLabelYX.equals(Constants.EMPTY_UPPER_CASE_LABELstring))
					// r3Applied = eYXnew.mergeLabeledValue(abg1, v);
					// else
					// r3Applied = eYXnew.mergeUpperLabelValue(abg1, UCLabelYX,
					// v);
					// if (r3Applied) {
					// // R3 changed the edge
					// R3calls++;
					// CSTNU.LOG.finer("R3 applied to edge considering not
					// alpha:\n"
					// + "partic: " + nObs.getName() + " ---(" + l + ", " +
					// UCLabelObsX + ", " + w + ")---> " + nX.getName()
					// + " <---(" + l1 + ", " + UCLabelYX + ", " + v + ")--- " +
					// nY.getName()
					// + "\nresult: " + nX.getName() + " <---(" + abg1 + ", " +
					// UCLabelYX + ", " + v + ")--- " + nY.getName());
					// ruleApplied = true;
					// }
					// }
				} else {
					// R3 special case rule
					// It requires both w and v negative and v<w.
					if (UCLabelYX.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
						r3Applied = eYXnew.mergeLabeledValue(abg1, w);
					} else {
						r3Applied = eYXnew.mergeUpperLabelValue(abg1, UCLabelYX, w);
					}
					if (r3Applied) {
						// RR changed the edge
						status.r3calls++;
						CSTNU.LOG.finer("R3 adds a label to edge " + eYX + ":\n" + "partic: " + nObs.getName() + " ---(" + l + ", "
								+ UCLabelObsX + ", " + w + ")---> " + nX.getName() + " <---(" + l1 + ", " + UCLabelYX + ", " + v
								+ ")--- " + nY.getName() + "\nresult: " + nX.getName() + " <---(" + l1 + ", " + UCLabelYX + ", "
								+ v + ")(" + abg1 + ", " + UCLabelYX + ", " + w + ")--- " + nY.getName());
						ruleApplied = true;
					}
				}
			}
		}
		if (ruleApplied) {
			CSTNU.LOG.finer("LabeledIntEdge " + eYXnew.getName() + " after R3 application: " + eYXnew.toString());
		}
		CSTNU.LOG.finest("Label Modification R3: end.");
		return ruleApplied;
	}

	/**
	 * Apply Morris lower case reduction (see page 1196 of the article).
	 *
	 * <pre>
	 *     l1, x     l2, c:y                    l1l2, x+y
	 * A  &lt;-------C &lt;--------D and x&le;0 adds A &lt;-----------D
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a reduction is applied at least
	 */
	boolean lowerCaseRule(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}
		final Set<LabeledIntEdge> lowerCaseEdge = currentGraph.getLowerLabeledEdges();
		/*
		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
		 */
		LabeledNode A, C, D;
		LabeledIntEdge DA;
		boolean reductionApplied = false;
		Set<Object2IntMap.Entry<Entry<Label, String>>> DCMap;
		CSTNU.LOG.finer("Lower Case Rule: start.");
		for (final LabeledIntEdge DC : lowerCaseEdge) {
			DCMap = DC.getLowerLabelSet();
			if (DCMap.size() == 0)
				continue;
			D = currentGraph.getSource(DC);
			C = currentGraph.getDest(DC);

			for (final LabeledIntEdge CA : currentGraph.getOutEdges(C)) {
				A = currentGraph.getDest(CA);
				// With guarded it is necessary to check loop!
				// if (A.equalsByName(D) || A.equalsByName(C)) continue;// A==D
				// is a loop =0; A==C, if x<0, then there is a negative loop
				if ((CA.getMinValue() == CSTNU.nullInt) || (CA.getMinValue() > 0))
					continue; // rule condition!

				final Set<Object2IntMap.Entry<Label>> CAMap = CA.labeledValueSet();

				DA = nextGraph.findEdge(nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));

				for (final Object2IntMap.Entry<Entry<Label, String>> entryDC : DCMap) {
					final Label l2 = entryDC.getKey().getKey();
					// LabeledNode contNode =
					// entryContingent.getKey().getValue();
					final int y = entryDC.getValue();
					// redundant check
					// if (!contNode.equalsByName(C)) {
					// throw new IllegalStateException(
					// "Found a lower case label that has destination different
					// from the label: " + DC);
					// }
					for (final Object2IntMap.Entry<Label> entryCA : CAMap) {
						final int x = entryCA.getValue();
						if (x > 0) // FIXME if instantaneousReaction is true, v>=0 ??
							continue;
						final Label l1 = entryCA.getKey();
						final Label l1l2 = l1.conjunction(l2);
						if (l1l2 == null) {
							continue;
							// I check if the label subsumes the label of the endpoints before to proceed
							// if (!checkNodeLabelsSubsumption(D, A, l1l2, (DA
							// == null) ? "e" + D.getName() + A.getName() :
							// DA.getName(), "Lower Case
							// Rule"))
							// continue;// THERE IS A MORE GENERAL CHECK
						}

						if (DA == null) {
							DA = new LabeledIntEdge("e" + D.getName() + A.getName(), LabeledIntEdge.Type.derived,
									this.labelOptimization);
							nextGraph.addEdge(DA, nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));
							CSTNU.LOG.finer("lowerCaseRule: added edge " + DA.getName());
						}
						final int oldZ = DA.getValue(l1l2);
						final int z = CSTNU.sumAndOverflowCheck(y, x, l1l2);
						final String oldDA = DA.toString();

						if (DA.mergeLabeledValue(l1l2, z)) {
							reductionApplied = true;
							status.lowerCaseRuleCalls++;
							CSTNU.LOG.finer("Lower Case applied to edge " + oldDA + ":\n" + "partic: " + A.getName() + " <---(" + l1
									+ ", " + Constants.EMPTY_UPPER_CASE_LABELstring + ", " + x + ")--- " + C.getName()
									+ " <---(" + l2 + ", " + entryDC.getKey().getValue().toLowerCase() + ", " + y + ")--- "
									+ D.getName() + "\nresult: " + A.getName() + " <---(" + l1l2 + ", "
									+ Constants.EMPTY_UPPER_CASE_LABELstring + ", " + z + ")--- " + D.getName()
									+ "; old value: " + oldZ);
							if (z != oldZ) {
								CSTNU.LOG.finer("LabeledIntEdge " + DA.getName() + " after the rule: " + DA.toString());
							}
						}
					}
				}
			}
		}
		CSTNU.LOG.finer("Lower Case Rule: end.");
		return reductionApplied;
	}

	/**
	 * Build the distance graph from g considering all standard constraints and adding all upper label constraints as standard constraints.
	 *
	 * @param g
	 *                a CSTNU instance
	 * @return the all max projection of the graph g.
	 */
	static LabeledIntGraph makeAllMaxProjection(final LabeledIntGraph g) {
		if (g == null)
			return null;
		final LabeledIntGraph distance = new LabeledIntGraph(g, true);

		for (final LabeledIntEdge e : distance.getUpperLabeledEdges()) {
			for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
				e.mergeLabeledValue(entry.getKey().getKey(), entry.getValue());
			}
		}
		return distance;
	}

	/**
	 * Applies the Negative QStar Rules, also known as R6. This is the new verion.<br>
	 * In this method all values are negative! Given a labeled value (l, -w) in a edge Z&lt;---P?, it checks whether in each edge Z&lt;---O? where O? is the
	 * observation point relating a proposition forming the label 'l' there is one or more labeled values containing 'p'.<br>
	 * If yes (in all edges!), then the rule adds to each Z&lt;---O edge, the labeled value (l', min), where min is the less negative value among the
	 * checked labeled values and l' is the label formed making the conjunction of all the checked label removing all opposite literals and literal
	 * associated to the considered observation nodes.<br>
	 * If no, returns.
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @return true if one rule has been applied one time at least.
	 */
	@Deprecated
	static boolean negativeQStarR6(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph) {
		CSTNU.LOG.finer("Label Modification R6: start.");
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}
		final Map<Literal, LabeledNode> obsMap = currentGraph.getObservedAndObservator();
		if (obsMap == null)
			return false;
		final Collection<LabeledNode> obsNodes = obsMap.values();

		// Check all obsNode-->Z edges
		final LabeledNode Z = currentGraph.getZ();
		for (final LabeledNode obsNode : obsNodes) {
			final LabeledIntEdge eObsZ = currentGraph.findEdge(obsNode, Z);// initial check of process guarantees that eObsZ always exists.
			final Literal l = obsNode.getPropositionObserved();

			for (final it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryObsZ : eObsZ.labeledValueSet()) {
				final Label label = entryObsZ.getKey();
				if (label.isEmpty()) {
					continue;
				}
				final int w = entryObsZ.getValue();
				int lessNegative = w;
				Boolean edgeSolvingLabelFound = false;
				final ObjectArraySet<Literal> literalRemoved = new ObjectArraySet<>();
				literalRemoved.add(l);
				literalRemoved.add(l.getComplement());
				final Label newLabel = new Label();
				final ObjectArraySet<LabeledIntEdge> edgesToUpdate = new ObjectArraySet<>();

				for (final Literal l1 : label.getAllAsStraight()) {
					final LabeledNode othObs = currentGraph.getObservator(l1);
					if (othObs == null)
						throw new RuntimeException(
								"LabeledIntGraph does not containt a necessary observation node! That is impossible!");

					final LabeledIntEdge eOthObsZ = currentGraph.findEdge(othObs, Z);
					if (eOthObsZ == null)
						throw new RuntimeException("LabeledIntGraph does not containt a necessary edge to Z! That is impossible!");

					// scan all labeled values of the edge Z<---OthObs
					final Label l2 = new Label();
					boolean found = false;
					for (final it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryOthObsZ : eOthObsZ.labeledValueSet()) {
						final Label label1 = entryOthObsZ.getKey();
						if (label1.isEmpty()) {
							continue;
						}
						final int w1 = entryOthObsZ.getValue();
						if (label1.contains(l) || label1.contains(l.getComplement())) {
							if (lessNegative < w1) {
								lessNegative = w1;
							}
							found = true;
							for (final Literal ll : label1.getAll()) {
								if (l2.isConsistentWith(ll)) {
									l2.conjunct(ll);
								} else {
									l2.remove(ll.getComplement());
									literalRemoved.add(ll);
									literalRemoved.add(ll.getComplement());
								}
							}
						}
					}
					if (found) {
						literalRemoved.add(l1);
						literalRemoved.add(l1.getComplement());
						newLabel.remove(l1);
						newLabel.remove(l1.getComplement());
						for (final Literal ll : literalRemoved) {
							l2.remove(ll);
						}
						for (final Literal ll : l2.getAll()) {
							if (newLabel.isConsistentWith(ll)) {
								newLabel.conjunct(ll);
							} else {
								literalRemoved.add(ll);
								literalRemoved.add(ll.getComplement());
								newLabel.remove(ll.getComplement());
							}
						}
						edgesToUpdate.add(nextGraph.getEdge(eOthObsZ.getName()));
					} else {
						edgeSolvingLabelFound = true;
					}
				}

				if (edgeSolvingLabelFound) {
					continue;
				}
				edgesToUpdate.add(nextGraph.getEdge(eObsZ.getName()));
				// We can add the minimal value to all edges in edgesToUpdate
				for (final LabeledIntEdge e : edgesToUpdate) {
					CSTNU.LOG.finer("R6 is adding to " + e + " the following value: (" + newLabel + ", " + lessNegative + ")...");
					if (e.mergeLabeledValue(newLabel, lessNegative)) {
						CSTNU.LOG.finer("done!");
					} else {
						CSTNU.LOG.finer("NOT DONE because already done in the past");
					}
				}
				// CSTNU.R6calls++;
			}
		}
		CSTNU.LOG.finer("Label Modification R6: end.");
		return false;
	}

	/**
	 * Apply Morris no case reduction to all constraints but the Upper or Lower Label ones.
	 *
	 * <pre>
	 *     l1, x       l2, y                    l1l2, x+y
	 * A  &lt;-------C &lt;--------D and x&le;0 adds A &lt;----------D
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a reduction is applied at least
	 */
	boolean noCaseRule(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}

		final int n = currentGraph.getVertexCount();
		final LabeledNode[] node = currentGraph.getVerticesArray();
		LabeledNode A, D, C, AinNextGraph, DinNextGraph;
		LabeledIntEdge CA, DC, DA;
		boolean ruleApplied = false;

		CSTNU.LOG.finer("No-Case Rule: start.");
		for (int k = 0; k < n; k++) {
			C = node[k];
			for (final Iterator<LabeledIntEdge> eCAIter = currentGraph.getOutEdges(C).iterator(); eCAIter.hasNext();) {
				CA = eCAIter.next();
				A = currentGraph.getDest(CA);
				if (C == A) {
					continue;// self loop are useless!
				}
				for (final Iterator<LabeledIntEdge> eDCIter = currentGraph.getInEdges(C).iterator(); eDCIter.hasNext();) {
					DC = eDCIter.next();
					D = currentGraph.getSource(DC);
					if ((D == C) || (A == D)) {
						continue; // self loop are useless!
					}

					// if ((CA == null) || (DC == null)) continue; // Luke ||
					// CA.isContingentEdge() || DC.isContingentEdge()) continue;
					// // Luke rule condition!
					DA = nextGraph.findEdge((DinNextGraph = nextGraph.getNode(D.getName())),
							(AinNextGraph = nextGraph.getNode(A.getName())));
					// if ((DA != null) && DA.isContingentEdge()) continue; //
					// Luke no contingent constraints.

					final Set<Object2IntMap.Entry<Label>> CAMap = CA.labeledValueSet();
					final Set<Object2IntMap.Entry<Label>> DCMap = DC.labeledValueSet();
					for (final Object2IntMap.Entry<Label> CAEntry : CAMap) {
						final Label l1 = CAEntry.getKey();
						final int x = CAEntry.getValue();
						for (final Object2IntMap.Entry<Label> DCEntry : DCMap) {
							final Label l2 = DCEntry.getKey();
							final Label l1l2 = l1.conjunction(l2);
							if (l1l2 == null) {
								continue;
							}
							final int y = DCEntry.getValue();

							if (DA == null) {
								DA = new LabeledIntEdge("e" + D.getName() + A.getName(), LabeledIntEdge.Type.derived,
										this.labelOptimization);
								nextGraph.addEdge(DA, DinNextGraph, AinNextGraph);
								CSTNU.LOG.finer("No-CaseRule: added edge " + DA.getName());
							}
							final int oldZ = DA.getValue(l1l2);
							final int z = CSTNU.sumAndOverflowCheck(x, y, l1l2);
							final String oldDA = DA.toString();
							if (DA.mergeLabeledValue(l1l2, z)) {
								ruleApplied = true;
								status.stdRuleCalls++;
								CSTNU.LOG.finer("No-Case applied to edge " + oldDA + ":\n" + "partic: " + A.getName() + " <---("
										+ l1 + ", ◇, " + x + ")--- " + C.getName() + " <---(" + l2 + ", ◇, " + y
										+ ")--- " + D.getName() + "\nresult: " + A.getName() + " <---(" + l1l2 + ", ◇, "
										+ z + ")--- " + D.getName() + "; old value: " + oldZ);
								if (z != oldZ) {
									CSTNU.LOG.finer("LabeledIntEdge " + DA.getName() + " after No-Case Rule: "
											+ DA.toString());
								}
							}
						}
					}
				}
			}
		}
		CSTNU.LOG.finer("No-Case Rule: end.");
		return ruleApplied;
	}

	/**
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initUpperLowerLabelDataStructure(LabeledIntGraph)}.
	 * 
	 * @param currentGraph
	 *                the current graph. It will be never overwritten.
	 * @param nextGraph
	 *                the next graph. It has be instantiate as a copy of current graph. It will contain the results of reduction at the end of the
	 *                execution.
	 * @param distanceGraph
	 *                the AllMax Projection of nextGraph. It will be instantiate by the method.
	 * @param status
	 *                object representing the status of the check.
	 * @return a {@link CSTNUCheckStatus} object representing the status of the check.
	 * @throws WellDefinitionException
	 *                 if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then there is a
	 *                 problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllability(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph,
			final LabeledIntGraph distanceGraph,
			final CSTNUCheckStatus status) throws WellDefinitionException {

		boolean reductionApplied = false;
		status.cycles++;

		LabeledIntGraph originalGraph = new LabeledIntGraph(labelOptimization);
		originalGraph.copy(currentGraph);
		distanceGraph.copy(CSTNU.makeAllMaxProjection(currentGraph));
		CSTNU.LOG.finest("distanceGraph:" + distanceGraph);
		CSTNU.LOG.info("Current graph: " + currentGraph.getName());
		CSTNU.LOG.info("AllMax Projection check... on " + distanceGraph.getName());
		if (!minimalDistanceGraphFast(distanceGraph)) {
			CSTNU.LOG.info("The all max projection graph has negative loops at the start of cycle " + status.cycles + ": stop!");
			status.controllable = false;
			status.finished = true;
			return status;
		}
		CSTNU.LOG.info("AllMax Projection check done.\n");

		CSTNU.LOG.info("Label modifications phase...");
		labelModificationR0R2(currentGraph, status);
		reductionApplied = !originalGraph.hasSameEdgesOf(currentGraph);
		nextGraph.copy(currentGraph);

		labelModificationR1R3(currentGraph, nextGraph, status);

		reductionApplied = !currentGraph.hasSameEdgesOf(nextGraph) || reductionApplied; // .hasAllEdgesOf is first
		CSTNU.LOG.info("Label modifications phase done.\n");
		currentGraph.copy(nextGraph);

		CSTNU.LOG.info("Rules phase...");
		noCaseRule(currentGraph, nextGraph, status);
		upperCaseRule(currentGraph, nextGraph, status);
		lowerCaseRule(currentGraph, nextGraph, status);
		crossCaseRule(currentGraph, nextGraph, status);
		caseLabelRemovalRule(currentGraph, nextGraph, status);
		// reductionApplied = CSTNU.labelModificationR4(currentGraph, nextGraph)
		// ? true : reductionApplied;
		reductionApplied = !currentGraph.hasSameEdgesOf(nextGraph) || reductionApplied; // .hasAllEdgesOf is first because I need to log!
		CSTNU.LOG.info("Rules phase done.\n");

		CSTNU.checkWellDefinitionProperties(nextGraph);

		if (!reductionApplied) {
			CSTNU.LOG.info("No more reductions applied at cycle " + status.cycles + ".\n");
			status.finished = true;
		}
		return status;
	}

	/**
	 * Apply Morris upper case reduction (see page 1196 of the article).
	 *
	 * <pre>
	 *      l1, B:x     l2, y             l1l2, B:x+y
	 * A  &lt;---------C &lt;--------D adds A &lt;-------------D
	 * </pre>
	 *
	 * @param currentGraph
	 *                the originating graph.
	 * @param nextGraph
	 *                the graph where to write the new constraints.
	 * @param status
	 *                CSTNUCheckStatus object representing the status of a checking algorithm run.
	 * @return true if a reduction is applied at least
	 */
	boolean upperCaseRule(final LabeledIntGraph currentGraph, final LabeledIntGraph nextGraph, CSTNUCheckStatus status) {
		if ((currentGraph == null) || (nextGraph == null)) {
			CSTNU.LOG.info("One parameter is null. Game over");
			return false;
		}

		final Set<LabeledIntEdge> upperCaseEdge = currentGraph.getUpperLabeledEdges();

		/*
		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
		 */
		LabeledNode A, C, D;
		LabeledIntEdge DA;
		boolean reductionApplied = false;
		Set<Object2IntMap.Entry<Entry<Label, String>>> CAMap;
		CSTNU.LOG.finer("Upper Case Rule: start.");
		for (final LabeledIntEdge CA : upperCaseEdge) {
			CAMap = CA.getUpperLabelSet();
			if (CAMap.size() == 0)
				continue;
			C = currentGraph.getSource(CA);
			A = currentGraph.getDest(CA);

			for (final LabeledIntEdge DC : currentGraph.getInEdges(C)) {
				D = currentGraph.getSource(DC);
				// if (C.equalsByName(D) || A.equalsByName(D)) continue;// it is useless to consider self loop.NO, it is necessary with
				// guarded links!

				DA = nextGraph.findEdge(nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));
				final Set<Object2IntMap.Entry<Label>> DCMap = DC.labeledValueSet();

				for (final Object2IntMap.Entry<Entry<Label, String>> entryCA : CAMap) {
					final Label l1 = entryCA.getKey().getKey();
					final String upperCaseNode = entryCA.getKey().getValue();
					final int x = entryCA.getValue();

					for (final Object2IntMap.Entry<Label> entryDC : DCMap) {
						final Label l2 = entryDC.getKey();
						final Label l1l2 = l1.conjunction(l2);
						if (l1l2 == null)
							continue;
						// if (!checkNodeLabelsSubsumption(D, A, l1l2, (DA ==
						// null) ? "e" + D.getName() + A.getName() :
						// DA.getName(), "Upper Case Rule"))
						// continue;// there is a more general check
						final int y = entryDC.getValue();
						if (DA == null) {
							DA = new LabeledIntEdge("e" + D.getName() + A.getName(), LabeledIntEdge.Type.derived,
									this.labelOptimization);
							nextGraph.addEdge(DA, nextGraph.getNode(D.getName()), nextGraph.getNode(A.getName()));
							CSTNU.LOG.finer("upperCaseRule: added edge " + DA.getName());
						}
						final int oldZ = DA.getUpperLabelValue(l1l2, upperCaseNode);
						final int z = sumAndOverflowCheck(x, y, l1l2);
						final String oldDA = DA.toString();
						// if ((oldZ != null) && (oldZ <= z)) continue;

						if (DA.mergeUpperLabelValue(l1l2, upperCaseNode, z)) {
							reductionApplied = true;
							status.upperCaseRuleCalls++;
							CSTNU.LOG.finer("Upper Case applied to edge " + oldDA + ":\n" + "partic: " + A.getName() + " <---(" + l1
									+ ", " + upperCaseNode.toUpperCase() + ", " + x + ")--- " + C.getName() + " <---(" + l2
									+ ", -," + y + ")--- " + D.getName() + "\nresult: " + A.getName() + " <---(" + l1l2
									+ ", " + upperCaseNode.toUpperCase() + ", " + z + ")--- " + D.getName()
									+ "; old value: " + oldZ);
							if (z != oldZ) {
								CSTNU.LOG.finer("LabeledIntEdge " + DA.getName() + " after the rule: " + DA.toString());
							}
						}
					}
				}
			}
		}
		CSTNU.LOG.finer("Upper Case Rule: end.");
		return reductionApplied;
	}

	// /**
	// * Applies "Observation Case" rule.
	// *
	// * <pre>
	// * Y &lt;---[αβp, ◇, u]--- X
	// * ---[βγ¬p, ◇, -v]--&gt;
	// * and u&lt;v
	// * adds P? &lt;--[αβγ, ◇, 0]-- Y
	// * </pre>
	// *
	// * @param currentGraph
	// * @param nextGraph
	// * @return true if a simplification has been applied.
	// */
	// @Deprecated
	// static boolean observationCaseRule(LabeledIntGraph currentGraph,
	// LabeledIntGraph nextGraph) {
	// if ((currentGraph == null) || (nextGraph == null)) {
	// CSTNU.LOG.info("One parameter is null. Game over");
	// return false;
	// }
	//
	// final int n = currentGraph.getVertexCount();
	// final LabeledNode[] node = currentGraph.getVerticesArray();
	// LabeledNode X, Y, P;
	// LabeledIntEdge XY, YX, YP;
	// boolean reductionApplied = false;
	//
	// CSTNU.LOG.finer("Observation Case Rule: start.");
	// for (int i = 0; i < n; i++) {
	// X = node[i];
	// for (int j = 0; j < n; j++) {
	// if (i == j) continue; // self loop are useless!
	// Y = node[j];
	// XY = currentGraph.findEdge(X, Y);
	// YX = currentGraph.findEdge(Y, X);
	// if ((XY == null) || (YX == null)) continue;
	//
	// final Set<Entry<Label, Integer>> XYMap = XY.labeledValueSet();
	// final Set<Entry<Label, Integer>> YXMap = YX.labeledValueSet();
	// for (final Entry<Label, Integer> XYEntry : XYMap) {
	// final Label l1 = XYEntry.getKey();
	// final Integer u = XYEntry.getValue();
	// for (final Entry<Label, Integer> YXEntry : YXMap) {
	// final Label l2 = YXEntry.getKey();
	// final Integer v = YXEntry.getValue();
	// if (v >= 0 || u >= -v) continue;
	//
	// Literal p = l1.getUniqueDifferentLiteral(l2);
	// if (p == null) continue;
	//
	// P = nextGraph.getObservable(p);
	// // if (P == null) {
	// // throw new NullPointerException("P cannot be null because the found
	// literal is " + p);
	// // }
	// YP = nextGraph.findEdge(nextGraph.getNode(Y.getName()),
	// nextGraph.getNode(P.getName()));
	// CSTNU.LOG.finest("X node: " + X);
	// CSTNU.LOG.finest("Y node: " + Y);
	// CSTNU.LOG.finest("XY edge: " + XY);
	// CSTNU.LOG.finest("YX edge: " + YX);
	// CSTNU.LOG.finest("P node: " + P);
	// CSTNU.LOG.finest("YP edge: " + YP);
	// final Label l1WithoutP = new Label(l1);
	// l1WithoutP.remove(p);
	// l1WithoutP.remove(p.getComplement());
	// final Label l2WithoutP = new Label(l2);
	// l2WithoutP.remove(p);
	// l2WithoutP.remove(p.getComplement());
	// final Label[] alphaBetaGammaPart = CSTNU.getAlphaBetaGamma(l1WithoutP,
	// l2WithoutP);
	// final Label alpha = alphaBetaGammaPart[0];
	//
	// Label abg = alpha.conjunction(l2WithoutP);
	// if (abg == null) continue;
	// // if (!checkNodeLabelsSubsumption(Y, P, abg, (YP == null) ? "e" +
	// Y.getName() + P.getName() : YP.getName(), "Observation Case Rule"))
	// // continue;// THERE IS A MORE GENERAL CHECK
	//
	// if (YP == null) {
	// YP = new LabeledIntEdge("e" + Y.getName() + P.getName(),
	// LabeledIntEdge.Type.derived);
	// nextGraph.addEdge(YP, nextGraph.getNode(Y.getName()),
	// nextGraph.getNode(P.getName()));
	// LOG.finer("observationCaseRule: added edge " + YP.getName());
	// }
	// final Integer oldZ = YP.getValue(abg);
	// final String oldYP = YP.toString();
	// if ((oldZ != null) && (oldZ <= 0)) continue;
	// if (YP.mergeLabeledValue(abg, 0)) {
	// reductionApplied = true;
	// ObsCaseRule++;
	// CSTNU.LOG.finer("Observation Case Rule applied to edge " + oldYP + ":\n"
	// + "partic: " + X.getName() + " <---(" + l2 + ", ◇, " + v + ")--- " +
	// Y.getName()
	// + " <---(" + l1 + ", ◇, " + u + ")--- " + X.getName() + "\nresult: " +
	// P.getName()
	// + " <---(" + abg + ", ◇, 0)--- " + Y.getName() + "; old value: " + oldZ);
	// CSTNU.LOG.finer("LabeledIntEdge " + YP.getName() + " after the rule: " +
	// YP.toString());
	// }
	// }
	// }
	// }
	// }
	// CSTNU.LOG.finer("Observation Case Rule: end.");
	// return reductionApplied;
	// }

	/**
	 * Simple method to determine the three parts of labels as required by lemma R1 and R2.<br>
	 * Label[0] is the sublabel of labelEdgeFromP not in common with labelWithoutP.<br>
	 * Label[1] is the sublabel of labelEdgeFromP in common with labelWithoutP. <br>
	 * Label[2] is the sublabel of labelWithoutP not in common with labelEdgeFromP.
	 *
	 * @param labelEdgeFromP
	 * @param labelWithoutP
	 * @param strict
	 * @return an array of three labels as required by lemma R1 and R2.
	 */
	private static Label[] getAlphaBetaGamma(final Label labelEdgeFromP, final Label labelWithoutP, boolean strict) {
		final Label[] alphaBetaGamma = new Label[3];
		alphaBetaGamma[0] = labelEdgeFromP.getSubLabelIn(labelWithoutP, false, strict);
		alphaBetaGamma[1] = labelEdgeFromP.getSubLabelIn(labelWithoutP, true, strict);
		alphaBetaGamma[2] = labelWithoutP.getSubLabelIn(labelEdgeFromP, false, strict);

		return alphaBetaGamma;
	}

	/**
	 * @param gamma
	 *                the original gamma
	 * @param p
	 *                the literal
	 * @param g
	 *                the graph where there are the observation nodes of propositions.
	 * @return gamma' that contains only the literals of gamma having observation nodes that do not depend by literal p. Null if any parameter is null.
	 */
	private static Label getGammaCleaned(final Label gamma, final Literal p, final LabeledIntGraph g) {
		if ((gamma == null) || (p == null) || (g == null))
			return null;
		final Label gamma1 = new Label();
		for (final Literal l : gamma.toArray()) {
			final LabeledNode obs = g.getObservator(l);
			CSTNU.LOG.finest("Observation for " + l + " in gamma: " + obs);
			final Label obsLabel = obs.getLabel();
			CSTNU.LOG.finest("Label for " + obs.getName() + ": " + obsLabel);

			if (obsLabel.getLiteralWithSameName(p) == null) {
				gamma1.conjunct(l);
			}
			CSTNU.LOG.finest("Gamma1 update to:" + gamma1);
		}
		return gamma1;
	}

	/**
	 * Returns the sum of all negative values (ignoring their labels) present in the edges of a graph. If an edge has more than one negative values, only
	 * the minimum among them is considered. For contingent link, also the lower case value is considered.
	 *
	 * @param g
	 * @return the sum of all negative value (negative value)
	 */
	@SuppressWarnings("unused")
	private static int getSumOfNegativeEdgeValues(final LabeledIntGraph g) {
		int sum = 0;
		if ((g == null) || (g.getEdgeCount() == 0))
			return sum;

		for (final LabeledIntEdge e : g.getEdges()) {
			final int min = e.getMinValue();
			// int minLC = e.getMinLowerLabeledValue();
			if ((min != CSTNU.nullInt) && (min < 0)) {
				// if (minLC != null) {
				// if (min.compareTo(minLC) > 0 ) min = minLC;
				// }
				sum += min;
			}
		}
		CSTNU.LOG.finer("The sum of all negative values is " + sum);
		return sum;
	}

	/**
	 * @param a
	 * @param b
	 * @param l
	 * @return the sum of a and b. If it is over the int capacity and the label does not contain a unknown literal, it throw an overflow exception.
	 */
	static private int sumAndOverflowCheck(final int a, final int b, final Label l) {
		if ((a == LabeledIntNodeSetMap.INT_NULL) || (b == LabeledIntNodeSetMap.INT_NULL) || (l == null))
			throw new IllegalArgumentException("At least one parameter is null: a=" + a + ", b=" + b + ", label=" + l);
		long sum = (long) a + (long) b;
		if (sum > Integer.MAX_VALUE) {
			if (l.getAllUnknown().length == 0)
				throw new IllegalStateException("Overflow in the standard labeled valued sum: (" + a + " + " + b + ", " + l + ").");
			sum = Constants.INT_POS_INFINITE;
		}
		return (int) sum;
	}

}
