//package it.univr.di.cstnu;
//
//import java.util.Collection;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.kohsuke.args4j.Option;
//
//import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectSet;
//import it.univr.di.cstnu.algorithms.CSTN;
//import it.univr.di.cstnu.algorithms.WellDefinitionException;
//import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
//import it.univr.di.cstnu.algorithms.WellDefinitionException.Type;
//import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
//import it.univr.di.cstnu.graph.LabeledIntEdge;
//import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
//import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
//import it.univr.di.cstnu.graph.LabeledIntGraph;
//import it.univr.di.cstnu.graph.LabeledNode;
//import it.univr.di.labeledvalue.AbstractLabeledIntMap;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.LabeledIntMapFactory;
//import it.univr.di.labeledvalue.LabeledIntTreeMap;
//import it.univr.di.labeledvalue.Literal.State;
//
///**
// * Simple class to represent and check Conditional Simple Temporal NetworkPartially Shrinkable  with Uncertainty (CSTNPSU).
// * 
// * FIXME: DA SISTEMARE COMPLETAMENTE It must import all new improvements from CSTNU.java
// * 
// * @author Roberto Posenato
// * @version $Id: $Id
// */
//public class CSTNPSU {
//	/**
//	 * Simple class to represent the status of the checking algorithm during an execution.
//	 *
//	 * @author Roberto Posenato
//	 */
//	public static class CSTNUCheckStatus {
//		/**
//		 * True if the network is controllable so far.
//		 */
//		public boolean controllable = true;
//
//		/**
//		 * Counters about the # of application of different rules.
//		 */
//		@SuppressWarnings("javadoc")
//		public int cycles = 0, r0calls = 0, r1calls = 0, r2calls = 0, r3calls = 0, labelPropagationRuleCalls = 0,
//				upperCaseRuleCalls = 0, lowerCaseRuleCalls = 0, crossCaseRuleCalls = 0, caseLabelRemovalRuleCalls = 0;
//
//		/**
//		 * Execution time in milliseconds.
//		 */
//		public long executionTimeNS = Constants.INT_NULL;
//
//		/**
//		 * True if no rule can be applied anymore.
//		 */
//		public boolean finished = false;
//
//		@Override
//		public String toString() {
//			return ("The check is" + (this.finished ? " " : " NOT") + " finished after " + this.cycles + " cycle(s).\n"
//					+ ((this.finished) ? "the consistency check has determined that given network is" + (this.controllable ? " " : " NOT ")
//							+ " dynamic controllable.\n" : "")
//					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
//					+ "Rule R1 has been applied " + this.r1calls + " times.\n"
//					+ "Rule R2 has been applied " + this.r2calls + " times.\n"
//					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
//					+ "Label Propagation Rule has been applied " + this.labelPropagationRuleCalls + " times.\n"
//					+ "Upper Case Rule has been applied " + this.upperCaseRuleCalls + " times.\n"
//					+ "Lower Case Rule has been applied " + this.lowerCaseRuleCalls + " times.\n"
//					+ "Cross Case Rule has been applied " + this.crossCaseRuleCalls + " times.\n"
//					+ "Case Removal Rule has been applied " + this.caseLabelRemovalRuleCalls + " times.\n"
//					+ "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)");
//		}
//	}
//
//	/**
//	 * logger
//	 */
//	static Logger LOG = Logger.getLogger(CSTNPSU.class.getName());
//
//	/**
//	 * Version of the class
//	 */
//	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
//	static final String VERSIONandDATE = "Version  3.2 - June, 14 2016";
//
//	/**
//	 * The name for the reference node.
//	 */
//	private static final String ZeroNodeName = "Z";
//
//	/**
//	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property:<br>
//	 * any labeled valued of the edge is consistent and subsumes both labels of two endpoints.
//	 *
//	 * @param source the source node of the edge.
//	 * @param destination the destination node of the edge.
//	 * @param e edge representing a labeled constraint.
//	 * @param hasToBeFixed true for fixing well-definition errors that can be fixed!
//	 * @return false if the check fails, true otherwise
//	 * @throws WellDefinitionException
//	 */
//	static private boolean checkWellDefinition1Property(final LabeledNode source, final LabeledNode destination, final LabeledIntEdge e, boolean hasToBeFixed) 
//			throws WellDefinitionException {
//		if ((e == null) || (source == null) || (destination == null)) {
//			if (LOG.isLoggable(Level.WARNING)) {
//				LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
//			}
//			return false;
//		}
//
//		final Label conjunctedLabel = source.getLabel().conjunction(destination.getLabel());
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Source label: " + source.getLabel() + "; dest label: " + destination.getLabel() + " conjuncted label: " + conjunctedLabel);
//		}
//		if (conjunctedLabel == null) {
//			final String msg = "Two endpoints do not allow any constraint because the have inconsisten labels."
//					+ "\nHead node: " + destination
//					+ "\nTail node: " + source
//					+ "\nConnecting edge: " + e;
//			if (LOG.isLoggable(Level.WARNING)) {
//				LOG.log(Level.WARNING, msg);
//			}
//			if (hasToBeFixed) {
//				e.clear();
//				return false;
//			}
//			throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
//		}
//		// check the ordinary labeled values
//		for (final Object2IntMap.Entry<Label> entry : e.getLabeledValueMap().entrySet()) {
//			Label valueLabel = entry.getKey();
//			if (valueLabel.conjunction(conjunctedLabel) == null) {
//				String msg = "Found a labeled value in " + e + " inconsistent with the conjunction of node labels, "
//						+ conjunctedLabel + "."; 
//				if (hasToBeFixed) {
//					e.removeLabel(valueLabel);
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, msg + " Labeled value '"+valueLabel+"' removed.");
//					}
//					continue;
//				} 
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
//			} 
//			if (!valueLabel.subsumes(conjunctedLabel)) {
//				final String msg = "Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				if (hasToBeFixed) {
//					int v = entry.getIntValue();
//					e.removeLabel(valueLabel);
//					e.putLabeledValue(valueLabel.conjunction(conjunctedLabel), v);
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, "It has been fixed!");
//					}
//					continue;
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//		}
//		// check the upper case labeled values
//		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
//			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
//				final String msg = "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//		}
//		// check the lower case labeled values
//		for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getLowerLabelSet()) {
//			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
//				final String msg = "Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Checks whether the label of a node satisfies the second well definition property:<br>
//	 * <blockquote>For each literal present in a node label label:
//	 * <ol>
//	 * <li>the label of the observation node of the considered literal is subsumed by the label of the current node.
//	 * <li>the observation node is constrained to occur before the current node.
//	 * </ol>
//	 * </blockquote>
//	 *
//	 * @param g the current graph containing the node.
//	 * @param node the current node to check.
//	 * @param reactionTime the reaction time of the system to the observation 
//	 * @return false if the check fails, true otherwise
//	 * @throws WellDefinitionException
//	 */
//	private static boolean checkWellDefinition2Property(final LabeledIntGraph g, final LabeledNode node, final int reactionTime) throws WellDefinitionException {
//		if ((g == null) || (node == null)) {
//			if (LOG.isLoggable(Level.WARNING)) {
//				LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
//			}
//			return false;
//		}
//
//		final Label nodeLabel = node.getLabel();
//		if (nodeLabel.isEmpty())
//			return true;
//
//		// check the observation node
//		int v;
//		for (final char l : nodeLabel.getPropositions()) {
//			final LabeledNode obs = g.getObservator(l);
//			if (obs == null) {
//				final String msg = "Observation node of literal " + l + " of node " + node + " does not exist.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
//			}
//
//			final Label obsLabel = obs.getLabel();
//			if (!nodeLabel.subsumes(obsLabel)) {
//				final String msg = "Label of node " + node + " does not subsume label of obs node " + obs;
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//
//			LabeledIntEdge e = g.findEdge(node, obs);
//			if ((e == null) || ((v = e.getMinValue()) == Constants.INT_NULL) || (v > -reactionTime)) {// WD2.2 ICAPS paper
//				final String msg = "There is no constraint to execute obs node " + obs + " before node " + node;
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotOccurBefore);
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Checks whether each labeled value of an edge 'e' satisfies the third well definition property:<br>
//	 * <blockquote>For each literal present in any label of 'e', the label of the observation node of the considered literal is subsumed by the label of the
//	 * edge.</blockquote>
//	 *
//	 * @param g the current graph containing the node.
//	 * @param e the current edge to check.
//	 * @return false if the check fails, true otherwise
//	 * @throws WellDefinitionException
//	 */
//	static private boolean checkWellDefinition3Property(final LabeledIntGraph g, final LabeledIntEdge e) throws WellDefinitionException {
//		if ((g == null) || (e == null)) {
//			if (LOG.isLoggable(Level.WARNING)) {
//				LOG.log(Level.WARNING, "One parameter is null at least. Please, check parameter.");
//			}
//			return false;
//		}
//
//		final Set<Object2IntMap.Entry<Entry<Label, String>>> allLabeledValuesSet = e.getAllUpperCaseAndOrdinaryLabeledValuesSet();
//		allLabeledValuesSet.addAll(e.getLowerLabelSet());
//		for (final Object2IntMap.Entry<Entry<Label, String>> entry : allLabeledValuesSet) {
//			final Label edgeLabel = entry.getKey().getKey();
//			if (edgeLabel.isEmpty()) {
//				continue;
//			}
//
//			// check the observation node
//			for (final char l : edgeLabel.getPropositions()) {
//				final LabeledNode obs = g.getObservator(l);
//				if (obs == null) {
//					final String msg = "Observation node of proposition '" + l + "' present in label '" + edgeLabel + "' of edge " + e
//							+ " does not exist.";
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, msg);
//					}
//					throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
//				}
//
//				final Label obsLabel = obs.getLabel();
//				if (!edgeLabel.subsumes(obsLabel)) {
//					final String msg = "Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs;
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, msg);
//					}
//					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//				}
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Returns the sum of all negative values (ignoring their labels) present in the edges of a graph. If an edge has more than one negative values, only
//	 * the minimum among them is considered. For contingent link, also the lower case value is considered.
//	 *
//	 * @param g
//	 * @return the sum of all negative value (negative value)
//	 */
//	@SuppressWarnings("unused")
//	private static int getSumOfNegativeEdgeValues(final LabeledIntGraph g) {
//		int sum = 0;
//		if ((g == null) || (g.getEdgeCount() == 0))
//			return sum;
//
//		for (final LabeledIntEdge e : g.getEdges()) {
//			final int min = e.getMinValue();
//			// int minLC = e.getMinLowerLabeledValue();
//			if ((min != Constants.INT_NULL) && (min < 0)) {
//				// if (minLC != null) {
//				// if (min.compareTo(minLC) > 0 ) min = minLC;
//				// }
//				sum += min;
//			}
//		}
//		LOG.finer("The sum of all negative values is " + sum);
//		return sum;
//	}
//
//	/**
//	 * @param label
//	 * @param value
//	 * @return the conventional representation of a labeled value
//	 */
//	private static final String pairAsString(Label label, int value) {
//		return AbstractLabeledIntMap.entryAsString(label, value);
//	}
//
//	/**
//	 * @param label
//	 * @param value
//	 * @param nodeName 
//	 * @param lower 
//	 * @return the conventional representation of a labeled value
//	 */
//	private static final String tripleAsString(Label label, int value, String nodeName, boolean lower) {
//		return LabeledContingentIntTreeMap.entryAsString(label, value, nodeName, lower);
//	}
//
//	/**
//	 * Class for representing edge labeled values.
//	 */
//	Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;
//
//	/**
//	 * Factory for labeled int map.
//	 */
//	final LabeledIntMapFactory<? extends LabeledIntMap> labeledIntMapFactory = new LabeledIntMapFactory<>(labeledIntValueMap);
//
//	/**
//	 * Reaction time for CSTNU. NOT IMPLEMENTED YET!
//	 */
//	@Option(required = false, name = "-r", usage = "Reaction time. It must be >= 0.")
//	private int reactionTime = 0;
//
//	/**
//	 * Constructor for CSTNU.
//	 */
//	@SuppressWarnings("unused")
//	private CSTNPSU() {
//	}
//
//	/**
//	 * Constructor for CSTNU
//	 *
//	 * @param reactionTime a non negative int representing the time after the setting of a proposition the system can react.
//	 * @param useΩ if Ω node has to be add and used if it is missing.
//	 * @param labeledIntValueMap the type for representing labeled value sets.
//	 */
//	public CSTNPSU(final int reactionTime, final boolean useΩ, Class<? extends LabeledIntMap> labeledIntValueMap) {
//		if (reactionTime < 0)
//			throw new IllegalArgumentException("Reaction time must be >= 0.");
//		this.reactionTime = reactionTime;
//		if (labeledIntValueMap != null) {
//			this.labeledIntValueMap = labeledIntValueMap;
//		}
//	}
//
//	/**
//	 * Apply Morris Label Removal Reduction (see page 1196 of the article MM2005).
//	 *
//	 * <pre>
//	 *     l_1, b, x    l_2, B, z                    	l_2, z
//	 * B  &lt;---------A &lt;----------C and z&ge;-x adds A &lt;-----------C
//	 * </pre>
//	 *
//	 * @param currentGraph the originating graph.
//	 * @param C
//	 * @param A
//	 * @param CA
//	 * @param status CSTNUCheckStatus object representing the status of a checking algorithm run.
//	 * @return true if a reduction is applied at least
//	 */
//	@SuppressWarnings("static-method")
//	boolean caseLabelRemovalRule(final LabeledIntGraph currentGraph, final LabeledNode C, final LabeledNode A, final LabeledIntEdge CA,
//			CSTNUCheckStatus status) {
//
//		LOG.finer("Label Removal Rule: start.");
//		final Collection<LabeledIntEdge> lowerCaseEdge = currentGraph.getOutEdges(A);
//		/*
//		 * I use the same node/edge ids of the Morris paper: A, B, C for nodes, and CA, AB for edges.
//		 */
//		LabeledNode B;
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, String>>> ABMap;
//
//		for (final LabeledIntEdge AB : lowerCaseEdge) {
//			ABMap = AB.getLowerLabelSet();
//			if (ABMap.size() == 0)
//				continue;
//			B = currentGraph.getDest(AB);
//
//			LabeledIntEdge BA = currentGraph.findEdge(B, A);// to manage guarded link
//
//			for (final Object2IntMap.Entry<Entry<Label, String>> upperCaseEntryOfCA : CA.getUpperLabelSet()) {
//				final String upperCaseNodeName = upperCaseEntryOfCA.getKey().getValue();
//				if (!upperCaseNodeName.equals(B.getName()))
//					continue;// Rule condition!
//				final Label l2 = upperCaseEntryOfCA.getKey().getKey();
//				final int z = upperCaseEntryOfCA.getValue();
//
//				for (final Object2IntMap.Entry<Entry<Label, String>> lowerCaseEntryOfAB : ABMap) {
//					final Label l1 = lowerCaseEntryOfAB.getKey().getKey();
//
//					int x = lowerCaseEntryOfAB.getValue();
//					if (x == Constants.INT_NULL || -z > x)
//						continue;
//
//					// With guarded link it is not sufficient to be lower than the lower case value (that represents the guard).
//					int x1 = (BA != null) ? BA.getMinValueConsistentWith(l1) : Constants.INT_NULL;
//					if (x1 != Constants.INT_NULL && z < x1) {
//						if (LOG.isLoggable(Level.FINEST)) {
//							LOG.log(Level.FINEST, "Case Label Removal not applied to edge " + CA + ": lower bound of " + AB + " is greater than " + z);
//						}
//						continue;
//					}
//					if (!l1.isConsistentWith(l2)) {
//						if (LOG.isLoggable(Level.FINEST)) {
//							LOG.log(Level.FINEST,
//									"Case Label Removal not applied to edge " + CA + ": label l1 '" + l1 + "' is not consitent with l2 '" + l2 + "'");
//						}
//						continue;
//					}
//					final int oldZ = CA.getValue(l2);
//					final String oldCA = CA.toString();
//
//					if (CA.mergeLabeledValue(l2, z)) {
//						reductionApplied = true;
//						status.caseLabelRemovalRuleCalls++;
//						LOG.finer("Case Label Removal applied to edge " + oldCA + ":\n" + "partic: "
//								+ B.getName() + " <---" + tripleAsString(l1, x, lowerCaseEntryOfAB.getKey().getValue(), true) + "--- " + A.getName()
//								+ " <---" + tripleAsString(l2, z, upperCaseNodeName, false) + "--- " + C.getName()
//								+ "\nresult: " + A.getName() + " <---" + pairAsString(l2, z) + "--- " + C.getName()
//								+ "; oldValue: " + Constants.formatInt(oldZ));
//					}
//				}
//			}
//		}
//		LOG.finer("Label Removal Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * checkWellDefinitionProperties.
//	 *
//	 * @param g a {@link LabeledIntGraph} object.
//	 * @return true if the g is a CSTN well defined.
//	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
//	 */
//	public boolean checkWellDefinitionProperties(final LabeledIntGraph g) throws WellDefinitionException {
//		boolean flag = false;
//		if (LOG.isLoggable(Level.FINER)) {
//			LOG.log(Level.FINER, "Checking if graph is well defined...");
//		}
//		for (final LabeledIntEdge e : g.getEdges()) {
//			flag = checkWellDefinition1Property(g.getSource(e), g.getDest(e), e, false);
//			flag = flag && checkWellDefinition3Property(g, e);
//		}
//		for (final LabeledNode node : g.getNodes()) {
//			flag = flag && checkWellDefinition2Property(g, node, this.reactionTime);
//		}
//		if (LOG.isLoggable(Level.FINER)) {
//			LOG.log(Level.FINER, ((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well-defined graph!\n"));
//		}
//		return flag;
//	}
//
//	/**
//	 * Apply Morris cross case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *      l1, B:x        l2, c:y                          
//	 * A  &lt;-----------C &lt;----------D and x&le;0, B!=C, A!=C
//	 * adds 
//	 *      l1l2, B:x+y
//	 * A &lt;-------------D
//	 * </pre>
//	 *
//	 * @param currentGraph the initial graph. CANNOT BE NULL
//	 * @param D
//	 * @param C
//	 * @param A
//	 * @param DC CANNOT BE NULL
//	 * @param CA CANNOT BE NULL
//	 * @param DA CANNOT BE NULL
//	 * @param status CSTNUCheckStatus object representing the status of a checking algorithm run.
//	 * @return true if a reduction is applied at least
//	 */
//	@SuppressWarnings("static-method")
//	boolean crossCaseRule(final LabeledIntGraph currentGraph, final LabeledNode D, final LabeledNode C, final LabeledNode A, final LabeledIntEdge DC,
//			final LabeledIntEdge CA, final LabeledIntEdge DA, CSTNUCheckStatus status) {
//
//		/*
//		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, String>>> DCMap = DC.getLowerLabelSet();
//		Set<Object2IntMap.Entry<Entry<Label, String>>> CAMap = CA.getUpperLabelSet();
//		if (DCMap.isEmpty() || CAMap.isEmpty() || A.equalsByName(C))
//			return false;
//
//		LOG.finer("Cross Case Rule: start.");
//
//		for (final Object2IntMap.Entry<Entry<Label, String>> entryLabelUpperCaseEdge : CAMap) {
//			final String upperCaseNodeName = entryLabelUpperCaseEdge.getKey().getValue();
//			if (upperCaseNodeName.equalsIgnoreCase(C.getName()))
//				continue;// Rule condition!
//
//			final int x = entryLabelUpperCaseEdge.getValue();
//			if (x > 0)
//				continue; // Rule condition!
//			final Label l1 = entryLabelUpperCaseEdge.getKey().getKey();
//
//			for (final Object2IntMap.Entry<Entry<Label, String>> entryLowerCaseEdgeToC : DCMap) {
//				final int y = entryLowerCaseEdgeToC.getValue();
//				final Label l2 = entryLowerCaseEdgeToC.getKey().getKey();
//				final Label l1l2 = l1.conjunction(l2);
//				if (l1l2 == null)
//					continue;
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//				final int oldZ = DA.getUpperLabelValue(l1l2, upperCaseNodeName);
//				final String oldDA = DA.toString();
//
//				if (DA.mergeUpperLabelValue(l1l2, upperCaseNodeName, z)) {
//					reductionApplied = true;
//					status.crossCaseRuleCalls++;
//					LOG.finer("Cross Case applied to edge " + oldDA + ":\n partic: " + A.getName() + " <---" + tripleAsString(l1, x, upperCaseNodeName, false)
//							+ "--- " + C.getName() + " <---" + tripleAsString(l2, y, entryLowerCaseEdgeToC.getKey().getValue(), true) + "--- " + D.getName()
//							+ "\nresult: " + A.getName()
//							+ " <---" + tripleAsString(l1l2, z, upperCaseNodeName, false) + "--- " + D.getName() + "; oldValue: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		LOG.finer("Cross Case Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
//	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
//	 *
//	 * @param g
//	 *            the original graph that has to be checked. If the check is successful, g is modified and it contains all minimized constraints;
//	 *            otherwise, it is not modified.
//	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
//	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException
//	 *             if the nextGraph is not well defined (does not observe all well definition properties).
//	 */
//	public CSTNUCheckStatus dynamicControllabilityCheck(final LabeledIntGraph g) throws WellDefinitionException {
//		CSTNUCheckStatus status = new CSTNUCheckStatus();
//		if (g == null)
//			return status;
//
//		final String originalName = g.getName();
//		LabeledIntGraph nextGraph = new LabeledIntGraph(g, labeledIntValueMap);
//		nextGraph.setName("Next graph");
//		try {
//			initUpperLowerLabelDataStructure(nextGraph);
//		} catch (final IllegalArgumentException e) {
//			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
//		}
//
//		Collection<LabeledIntEdge> edgesToCheck = nextGraph.getEdges();
//
//		final int n = nextGraph.getVertexCount();
//		int k = nextGraph.getUpperLabeledEdges().size();
//		if (k == 0) {
//			k = 1;
//		}
//		int p = nextGraph.getPropositions().size();
//		if (p == 0) {
//			p = 1;
//		}
//
//		int maxCycles = p * ((n * n) + (n * k) + k);// cstnu
//		// int maxCycles = (int) (Math.pow(2,p) * Math.pow(n, 3) *
//		// -getSumOfNegativeEdgeValues(g) );//cstn
//		// int maxCycles = -CSTNU.getSumOfNegativeEdgeValues(g) * n;// cstn
//		if (maxCycles == 0) {
//			maxCycles = 2;
//		}
//		LOG.info("The maximum number of possible cycles is " + maxCycles);
//
//		int i;
//		long startTime = System.nanoTime();
//
//		for (i = 1; i <= maxCycles && status.controllable && !status.finished; i++) {
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
//			}
//
//			status = this.oneStepDynamicControllability(nextGraph, edgesToCheck, status);
//
//			if (status.controllable && !status.finished) {
//				if (LOG.isLoggable(Level.FINE)) {
//					StringBuilder log = new StringBuilder();
//					log.append("During the check n. " + i + ", " + edgesToCheck.size()
//							+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
//					for (LabeledIntEdge e : edgesToCheck) {
//						if (e.size() == 0)
//							continue;
//						log.append("Edge " + e + "\n");
//					}
//					LOG.log(Level.FINE, log.toString());
//				}
//			}
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
//			}
//		}
//		// FIXME Experimental check
//		LOG.info("AllMax Projection check starts...");
//		LabeledIntGraph allMaxCSTN = makeAllMaxProjection(nextGraph);
//		CSTN cstnChecker = new CSTN(null);
//		CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck(allMaxCSTN);
//		LOG.info("AllMax Projection check done.\n");
//		if (!cstnStatus.consistency) {
//			LOG.info("The AllMax Projection graph has at leat one negative loop at the start of cycle " + status.cycles + ": stop!");
//			status.controllable = false;
//			status.finished = true;
//		}
//		// FIXME Experimental check
//
//		status.executionTimeNS = (System.nanoTime() - startTime);
//
//		if (!status.controllable) {
//			if (LOG.isLoggable(Level.INFO)) {
//				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an UNCONTROLLABILITY.\nStatus: " + status);
//				LOG.log(Level.FINER, "Final un-controllable graph: " + nextGraph);
//			}
//			g.takeIn(nextGraph);
//			g.setName(originalName);
//			return status;
//		}
//
//		if (i > maxCycles && !status.finished) {
//			if (LOG.isLoggable(Level.INFO)) {
//				LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + status);
//				LOG.log(Level.FINER, "Last determined graph: " + nextGraph);
//			}
//			status.controllable = status.finished;
//			g.takeIn(nextGraph);
//			g.setName(originalName);
//			return status;
//		}
//
//		// controllable && finished
//		if (LOG.isLoggable(Level.INFO)) {
//			LOG.log(Level.INFO, "Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles
//					+ ".\nStatus: " + status);
//		}
//		// Put all data structures of currentGraph in g
//		g.copyCleaningRedundantLabels(nextGraph);
//		g.setName(originalName);
//		return status;
//	}
//
//	/**
//	 * Initializes all data structures before the execution of controllability checking algorithm. This method has to be called before the application of
//	 * any propagation/reduction rule. If the given instance does not contain a node named 'Z' (assumed to be the first node to be executed), the method
//	 * adds it and all the necessary temporal for assuring that Z will be the first executed node.
//	 * 
//	 * @param g a cstnu instance to check.
//	 * @return true if the check is successful. The input g results to be modified by the method.
//	 * @throws WellDefinitionException if the graph is null or it is not well formed.
//	 */
//	public boolean initUpperLowerLabelDataStructure(final LabeledIntGraph g) throws WellDefinitionException {
//		if (g == null)
//			throw new WellDefinitionException("The graph is null!");
//
//		if (LOG.isLoggable(Level.FINER)) {
//			LOG.log(Level.FINER, "Initial Checking.\nReaction time: " + this.reactionTime);
//		}
//		g.clearCache();
//
//		for (final LabeledIntEdge e : g.getEdges()) {
//
//			if (LOG.isLoggable(Level.FINEST)) {
//				LOG.log(Level.FINEST, "Initial Checking edge e: " + e);
//			}
//
//			final LabeledNode s = g.getSource(e);
//			final LabeledNode d = g.getDest(e);
//			
//			// WD1 is checked and adjusted here
//			try {
//				checkWellDefinition1Property(s, d, e, true);
//			} catch (final WellDefinitionException ex) {
//				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
//			}
//
//			if (e.size() == 0 && e.lowerLabelSize() == 0 && e.upperLabelSize() == 0) {
//				// The merge removed labels...
//				g.removeEdge(e);
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, "Labels fixing on edge " + e + " removed all labels. Edge " + e + " has been removed.");
//				}
//				continue;
//			}
//
//			// WD3 property
//			try {
//				checkWellDefinition3Property(g, e);
//			} catch (final WellDefinitionException ex) {
//				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
//			}
//
//			if (!e.isContingentEdge()) {
//				continue;
//			}
//			/***
//			 * Manage contingent link.
//			 */
//			/**
//			 * From svn version 74 I try to introduce the management of guarded link (contingent link with different paired requirement
//			 * constraints). The idea is to set the upper/lower case label if and only if there is NO upper/lower case label.
//			 */
//			final Label conjunctedLabel = s.getLabel().conjunction(d.getLabel());
//			final int initialValue = e.getMinValueConsistentWith(conjunctedLabel);
//			if (initialValue == Constants.INT_NULL) {
//				if (e.lowerLabelSize() == 0 && e.upperLabelSize() == 0)
//					throw new IllegalArgumentException("Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
//			}
//			if (initialValue == 0)
//				throw new IllegalArgumentException("Contingent edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0<x<y<∞.");
//
//			// e.clearLabels(); This cannot be done when there are guarded links!
//
//			// if (caseLabel != null) {
//			// // manage a wait contingent link
//			// if (caseLabel.matches(Constants.upperCaseLabel)) {
//			// e.mergeUpperLabelValue(l, caseLabel, initialValue);
//			// } else {
//			// LOG.warning("Contingent edge with a wait default label that is lower case!");
//			// e.mergeLowerLabelValue(l, caseLabel, initialValue);
//			// }
//			// continue;
//			// }
//
//			boolean insertUpperLowerCaseValue = false;
//			LabeledIntEdge eInverted = g.findEdge(d, s);
//			LOG.finer("Edge " + e + " is contingent. Found its companion: " + eInverted);
//			if (eInverted == null) {
//				// Since
//				throw new IllegalArgumentException("Contingent edge " + e + " is alone. The companion contingent edge between " + d.getName()
//						+ " and " + s.getName() + " does not exist. It must!");
//				// eInverted = newEdgeInCSTNU(d.getName() + "_" + s.getName()+, LabeledIntEdge.Type.derived, );
//				// g.addEdge(eInverted, d, s);
//				// LOG.warning("Edge " + e + " is contingent. Its companion is null, so a new one has been created: " + eInverted);
//				// // in this case we can add the case label
//				// insertUpperLowerCaseValue = true;
//			} // else {
//				// I have discovered that some users can set one edge 'contingent' while set 'normal' the companion!
//				// I fix!
//			if (!eInverted.isContingentEdge()) {
//				throw new IllegalArgumentException("Edge " + e + " is contingent while the companion edge " + eInverted + " is not contingent!\nIt must be!");
//			}
//			// if ((initialValue == eInverted.getMinValueConsistentWith(conjunctLabel))) { //this is not necessary anymore.
//			// A contingent link can have only UPPER and lower case values.
//			// A guarded link can have also ordinary values that can be different from the corresponding upper/lower case values.
//			// throw new IllegalArgumentException(
//			// "Contingent edge " + e + " cannot have a bound equals to the bound of its companion. The two bounds [x,y] have to be 0<x<y<∞.");
//			// }
//			if (initialValue != Constants.INT_NULL && initialValue <= 0 && (eInverted.getLowerLabelValue(conjunctedLabel, s) == Constants.INT_NULL)) {// the
//																																					// current
//																																					// edge is
//																																					// C--->A
//				LOG.warning("Edge " + e + " is contingent with a negative value but the inverted " + eInverted + " does not contain a lower case value: "
//						+ tripleAsString(conjunctedLabel, Constants.INT_NULL, s.getName(), true) + ".");
//				insertUpperLowerCaseValue = true;
//			}
//			if (initialValue != Constants.INT_NULL && initialValue > 0 && (eInverted.getUpperLabelValue(conjunctedLabel, d) == Constants.INT_NULL)) {// the
//																																					// current
//																																					// edge is
//																																					// A--->C
//				LOG.warning("Edge " + e + " is contingent with a positive value but the inverted " + eInverted + " does not contain an UPPER case value: "
//						+ tripleAsString(conjunctedLabel, Constants.INT_NULL, d.getName(), false) + ".");
//				insertUpperLowerCaseValue = true;
//			}
//			// }
//			if (insertUpperLowerCaseValue) {
//				if (initialValue <= 0) {
//					eInverted.mergeLowerLabelValue(conjunctedLabel, s, -initialValue);
//					LOG.warning("Insert the lower label value: " + tripleAsString(conjunctedLabel, -initialValue, s.getName(), true) + " to edge "
//							+ eInverted.getName());
//				} else {
//					eInverted.mergeUpperLabelValue(conjunctedLabel, d, -initialValue);
//					LOG.warning("Insert the upper label value: " + tripleAsString(conjunctedLabel, -initialValue, d.getName(), false) + ") to edge "
//							+ eInverted.getName());
//
//				}
//			}
//		} // end edges for cycle
//
//		// init two useful structures
//		g.getLowerLabeledEdges();
//		g.getPropositions();
//
//		// Start of well definition and properties about nodes (w.r.t. the Z node)!
//		LabeledNode Z = g.getZ();
//		if (Z == null) {
//			Z = g.getNode(CSTNPSU.ZeroNodeName);
//			if (Z == null) {
//				// We add by authority!
//				Z = new LabeledNode(CSTNPSU.ZeroNodeName);
//				Z.setX(0.0);
//				Z.setY(0.0);
//				g.addVertex(Z);
//				if (LOG.isLoggable(Level.WARNING))
//					LOG.log(Level.WARNING, "No " + CSTNPSU.ZeroNodeName + " node found: added!");
//			}
//			g.setZ(Z);
//		}
//
//		final Collection<LabeledNode> nodeSet = g.getVertices();
//		for (final LabeledNode node : nodeSet) {
//			// Check that obs-node has no in its label the proposition observed!
//			final char obs = node.getPropositionObserved();
//			final Label label = node.getLabel();
//			if (obs != Constants.UNKNOWN) {
//				if (label.contains(obs)) {
//					if (LOG.isLoggable(Level.WARNING))
//						LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label
//								+ "' of the observation node '" + node.getName() + "'. Removed!");
//				}
//				label.remove(obs);
//			}
//
//			// WD2 is checked and adjusted here
//			try {
//				checkWellDefinition2Property(g, node, this.reactionTime);
//			} catch (final WellDefinitionException ex) {
//				if (ex.getType() == Type.ObservationNodeDoesNotOccurBefore) {
//					for (final char l1 : label.getPropositions()) {
//						final LabeledNode obsl1 = g.getObservator(l1);
//						LabeledIntEdge e = g.findEdge(node, obsl1);
//						if (e == null) {
//							e = CSTN.makeNewEdge(node.getName() + "_" + obsl1.getName(), LabeledIntEdge.ConstraintType.derived);
//							g.addEdge(e, node, obsl1);
//							LOG.warning("It is necessary to add a preceding constraint between node '" + node.getName()
//									+ "' and node '" + obsl1.getName() + "' to satisfy WD2.");
//						}
//						e.mergeLabeledValue(label, -this.reactionTime);
//						if (LOG.isLoggable(Level.WARNING)) {
//							LOG.log(Level.WARNING, "WD2.2 requires the following update: " + e);
//						}
//					}
//				} else
//					throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
//			}
//		}
//
//		if (!Z.getLabel().isEmpty()) {
//			if (LOG.isLoggable(Level.WARNING))
//				LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
//			Z.setLabel(Label.emptyLabel);
//		}
//
//		// Now I assuring that each node has a edge to Z.
//		for (final LabeledNode node : nodeSet) {
//			if (node != Z) {
//				LabeledIntEdge e = g.findEdge(node, Z);
//				if (e == null) {
//					e = CSTN.makeNewEdge(node.getName() + "_" + Z.getName(), LabeledIntEdge.ConstraintType.derived);
//					g.addEdge(e, node, Z);
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING,
//								"It is necessary to add a constraint to guarantee that node '" + node.getName() + "' occurs after node '" + Z.getName());
//					}
//				}
//				e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
//			}
//		}
//		// It is better to normalize with respect to the label modification rules before starting the DC check.
//		// Such normalization assures only that redundant labels are removed (w.r.t. R0, R2)
//		// Qstar are not solved by this normalization!
//		// Moreover, we calculate the sum of all negative values and the sum of all positive values to set an upper bound between Z and Ω
//		final CSTNUCheckStatus status = new CSTNUCheckStatus();
//		try {
//			for (final LabeledIntEdge e : g.getEdges()) {
//				final LabeledNode s = g.getSource(e);
//				final LabeledNode d = g.getDest(e);
//
//				// Normalize with respect to R0--R3
//				if (s.isObservator()) {
//					labelModificationR0(g, s, d, Z, e, status);
//				}
//				// if (!this.excludeR1R2) {
//				// if (d.isObservator()) {
//				// this.labelModificationR2(g, d, s, e, status);
//				// }
//				// this.labelModificationR1(g, s, d, e, status);
//				// if (d.isObservator()) {
//				// // again because R1 could have add a new value;
//				// this.labelModificationR2(g, d, s, e, status);
//				// }
//				// }
//				this.labelModificationR3(g, s, d, Z, e, status);
//				if (s.isObservator()) {
//					// again because R3 could have add a new value;
//					labelModificationR0(g, s, d, Z, e, status);
//				}
//			}
//		} catch (IllegalStateException ex) {
//			String logMsg = "Graph is not well defined:\n" + ex.getMessage();
//			LOG.severe(logMsg);
//			throw new WellDefinitionException(logMsg);
//		}
//
//		// Set an upper bound between Z and Ω
//		if (LOG.isLoggable(Level.INFO)) {
//			LOG.log(Level.INFO, "A preliminary application of label modification rules has been done: " + status.toString());
//		}
//		return true;
//	}
//
//	/**
//	 * Applies rule R0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
//	 *
//	 * <pre>
//	 * R0:
//	 * P? --[w, C, α p,]--&gt; X changes in P? --[w, C α']--&gt; X when w < ε
//	 * where:
//	 * p is the positive or the negative literal associated to proposition observed in P?,
//	 * α is a label,
//	 * C can be ◇ or an upper letter.
//	 * α' is α without 'p', P? children, and any children of possible q-literals.
//	 * ε>0 is the reaction time.
//	 * </pre>
//	 *
//	 * @param currentGraph
//	 * @param P the observation node
//	 * @param X the other node
//	 * @param PX the edge connecting P? ---&gt; X
//	 * @param Z
//	 * @param status
//	 * @return true if the rule has been applied one time at least.
//	 */
//	boolean labelModificationR0(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledNode Z, final LabeledIntEdge PX,
//			final CSTNUCheckStatus status) {
//		// Visibility is package because there is Junit Class test that checks this method.
//
//		boolean ruleApplied = false, mergeStatus;
//		final char p = P.getPropositionObserved();
//		if (p == Constants.UNKNOWN) {
//			if (LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
//			}
//			return false;
//		}
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R0: start.");
//		}
//		if (X.getLabel().contains(p)) {// Table 2 ICAPS
////			if (LOG.isLoggable(Level.FINER)) {
////				LOG.log(Level.FINER, "R0: Proposition '" + p + "' is present in the X label '" + X.getLabel() + "'. R0 cannot be applied.");
////			}
//			return false;
//		}
//
//		final ObjectSet<Label> obsXLabelSet = PX.getLabeledValueMap().keySet();
//
//		for (final Label l : obsXLabelSet) {
//			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
//				continue;
//			}
//
//			final int w = PX.getValue(l);
//			if (w == Constants.INT_NULL) {
//				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//				continue;
//			}
//
//			if (w >= this.reactionTime) {// Table 2 ICAPS paper
//				continue;
//			}
//
//			final Label alphaPrime = CSTN.makeAlphaPrime(X, P, Z, p, l);
//			if (alphaPrime == null) {
//				continue;
//			}
//			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
//			String logMessage = null;
//			if (LOG.isLoggable(Level.FINER)) {
//				logMessage = "R0 simplifies a label of edge " + PX.getName()
//						+ ":\nsource: " + P.getName() + " ---" + pairAsString(l, w) + "---> " + X.getName()
//						+ "\nresult: " + P.getName() + " ---" + pairAsString(alphaPrime, w) + "---> " + X.getName();
//			}
//
//			PX.putLabeledValueToRemovedList(l, w);
//			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
//			status.r0calls++;
//			ruleApplied = true;
//			mergeStatus = PX.mergeLabeledValue(alphaPrime, w);
//			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, logMessage);
//			}
//			if (CSTN.isNewLabeledValueANegativeLoop(alphaPrime, w, P, X, PX)) {
//				status.controllable = false;
//				status.finished = true;
//				return ruleApplied;
//			}
//		}
//
//		// upper-case labels
//		final Set<Object2IntMap.Entry<Entry<Label, String>>> edgeUpperLabeledValueSet = new ObjectArraySet<>(PX.getUpperLabelSet());
//		for (final Object2IntMap.Entry<Entry<Label, String>> entryObs : edgeUpperLabeledValueSet) {
//			final Label l = entryObs.getKey().getKey();
//			if (l == null || l.getStateLiteralWithSameName(p) == State.absent) {// l can be nullified in a previous cycle.
//				continue;
//			}
//
//			final String nodeName = entryObs.getKey().getValue();
//			final int w = PX.getUpperLabelValue(l, nodeName);
//			if (w == Constants.INT_NULL) {
//				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//				continue;
//			}
//
//			if (w >= this.reactionTime) {// Table 2 ICAPS paper
//				continue;
//			}
//
//			final Label alphaPrime = CSTN.makeAlphaPrime(X, P, Z, p, l);
//			if (alphaPrime == null) {
//				continue;
//			}
//
//			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
//			String logMessage = null;
//			if (LOG.isLoggable(Level.FINER)) {
//				logMessage = "R0 simplifies a label of edge " + PX.getName()
//						+ ":\nsource: " + P.getName() + " ---" + tripleAsString(l, w, nodeName, false) + "---> " + X.getName()
//						+ "\nresult: " + P.getName() + " ---" + tripleAsString(alphaPrime, w, nodeName, false) + "---> " + X.getName();
//			}
//
//			PX.putUpperLabeledValueToRemovedList(l, nodeName, w);
//			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
//			status.r0calls++;
//			ruleApplied = true;
//			mergeStatus = PX.mergeUpperLabelValue(alphaPrime, nodeName, w);
//			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, logMessage);
//			}
//		}
//
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R0: end.");
//		}
//
//		return ruleApplied;
//	}
//
//	/**
//	 * Rule R3 applies the following labels modification:
//	 *
//	 * <pre>
//	 * if P? --[α, U, w]--&gt; nD &lt;--[βθp', U', v]-- nS  and w &le; ε
//	 * then the constraint between Y and X is modified adding the following label:
//	 * nD &lt;--[(α*β)', U'', max{w-ε,v}]-- nS
//	 * where:
//	 * α does not contain any literal of p and P? children.
//	 * U=◇, U' can be upper-case letter or ◇ when nD!= Z. They can be any but not both upper-case letter when nD = Z.
//	 * U'' is ◇ if U = U' = ◇, U if U' = ◇, U' if  U = ◇
//	 * β can contain q-literal but not literals of p and its children
//	 * θ contains children of p.
//	 * p' is any literal (¿p included) of p.
//	 * (α*β)' is the extended conjunction without any children of possible q-literals in it.
//	 * ε>0 is the reaction time.
//	 * </pre>
//	 *
//	 * @param currentGraph
//	 * @param nS node
//	 * @param nD node
//	 * @param Z
//	 * @param eSD LabeledIntEdge containing the constrain to modify
//	 * @param status
//	 * @return true if a rule has been applied.
//	 */
//	// Visibility is package because there is Junit Class test that checks this method.
//	boolean labelModificationR3(final LabeledIntGraph currentGraph, final LabeledNode nS, final LabeledNode nD, final LabeledNode Z, final LabeledIntEdge eSD,
//			final CSTNUCheckStatus status) {
//
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R3: start.");
//		}
//		boolean ruleApplied = false;
//
//		ObjectArraySet<LabeledIntEdge> Obs2nDEdges = CSTN.getEdgeFromObservatorsToNode(nD);
//		if (Obs2nDEdges.isEmpty())
//			return false;
//
//		final ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> SDLabeledValueSet = eSD.getAllUpperCaseAndOrdinaryLabeledValuesSet();
//
//		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
//			final LabeledNode nObs = currentGraph.getSource(eObsD);
//
//			if (nObs.equalsByName(nS))
//				continue;
//			final char p = nObs.getPropositionObserved();
//
//			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// Table 2 ICAPS
////				if (LOG.isLoggable(Level.FINEST)) {
////					LOG.log(Level.FINEST, "R3: Proposition '" + p + "' is present in the nS label '" + nS.getLabel() + "' or nD label '" + nD.getLabel()
////							+ "'. R3 cannot be applied.");
////				}
//				continue;
//			}
//			// all labels from Obs
//			for (final Object2IntMap.Entry<Entry<Label, String>> entryObsD : eObsD.getAllUpperCaseAndOrdinaryLabeledValuesSet()) {
//				final int w = entryObsD.getIntValue();
//				if (w > this.reactionTime || (this.reactionTime == 0 && w == 0)) {// Table 2 ICAPS
//					continue;
//				}
//
//				final Label ObsDLabel = entryObsD.getKey().getKey();
//				if (ObsDLabel.containsUnknown() || ObsDLabel.contains(p)) {// p should be never present... R0 rids the edge of it
//					continue;
//				}
//
//				final String ObsDUpperCaseLetter = entryObsD.getKey().getValue();
//
//				Label SDLabel;
//				String newUpperCaseLetter;
//				for (final Object2IntMap.Entry<Entry<Label, String>> SDLabelEntry : SDLabeledValueSet) {
//					if (SDLabelEntry == null || !(SDLabel = SDLabelEntry.getKey().getKey()).contains(p)) {
//						continue;
//					}
//
//					String SDUpperCaseLetter = SDLabelEntry.getKey().getValue();
//
//					if (ObsDUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
//						// ObsDUpperCaseLetter == ◇
//						newUpperCaseLetter = SDUpperCaseLetter;
//					} else {
//						if (!SDUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
//							continue;
//						}
//						if (!nD.equalsByName(Z)) {
//							continue;
//						}
//						newUpperCaseLetter = ObsDUpperCaseLetter;
//					}
//					final int v = (SDUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) ? eSD.getValue(SDLabel)
//							: eSD.getUpperLabelValue(SDLabel, SDUpperCaseLetter);
//					if (v == Constants.INT_NULL) {
//						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//						continue;
//					}
//
//					final int max = Math.max(w - this.reactionTime, v);
//
//					final Label ab1 = CSTN.makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel);//FIXME 
//					if (ab1 == null) {
//						if (LOG.isLoggable(Level.FINER)) {
//							LOG.log(Level.FINER, "R3: Label " + ab1 + " is not suitable.");
//						}
//						continue;
//					}
//
//					if (newUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
//						eSD.putLabeledValueToRemovedList(SDLabel, v);
//						ruleApplied = eSD.mergeLabeledValue(ab1, max);
//					} else {
//						eSD.putUpperLabeledValueToRemovedList(SDLabel, SDUpperCaseLetter, v);
//						ruleApplied = eSD.mergeUpperLabelValue(ab1, newUpperCaseLetter, max);
//					}
//
//					if (ruleApplied) {
//						if (LOG.isLoggable(Level.FINER)) {
//							if (!newUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring)) {
//								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
//										+ "source: " + nObs.getName() + " ---" + tripleAsString(ObsDLabel, w, ObsDUpperCaseLetter, false) + "---> "
//										+ nD.getName()
//										+ " <---" + tripleAsString(SDLabel, v, SDUpperCaseLetter, false) + "--- " + nS.getName()
//										+ "\nresult: add " + nD.getName() + " <---" + tripleAsString(ab1, max, newUpperCaseLetter, false) + "--- "
//										+ nS.getName());
//							} else {
//								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
//										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "---> " + nD.getName()
//										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
//										+ "\nresult: add " + nD.getName() + " <---" + pairAsString(ab1, max) + "--- " + nS.getName());
//
//							}
//						}
//						status.r3calls++;
//					}
//
//					if (newUpperCaseLetter.equals(Constants.EMPTY_UPPER_CASE_LABELstring) && CSTN.isNewLabeledValueANegativeLoop(ab1, w, nS, nD, eSD)) {
//						status.controllable = false;
//						status.finished = true;
//						return ruleApplied;
//					}
//				}
//			}
//		}
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R3: end.");
//		}
//		return ruleApplied;
//	}
//
//	/**
//	 * Apply Morris no case reduction to all constraints but the Upper or Lower Label ones.
//	 *
//	 * <pre>
//	 *     l1, x       l2, y                    l1l2, x+y
//	 * C  &lt;-------B &lt;--------A and x&le;0 adds C &lt;----------A
//	 * </pre>
//	 *
//	 * @param currentGraph the originating graph.
//	 * @param A
//	 * @param B
//	 * @param C
//	 * @param AB CANNOT BE NULL
//	 * @param BC CANNOT BE NULL
//	 * @param AC CANNOT BE NULL
//	 * @param Z
//	 * @param status CSTNUCheckStatus object representing the status of a checking algorithm run.
//	 * @return true if a reduction is applied at least
//	 */
//	boolean labelPropagationRule(final LabeledIntGraph currentGraph, final LabeledNode A, final LabeledNode B, final LabeledNode C, final LabeledNode Z,
//			final LabeledIntEdge AB, final LabeledIntEdge BC, final LabeledIntEdge AC, CSTNUCheckStatus status) {
//
//		// if (AC == null) {
//		// AC = newEdgeInCSTNU(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived, currentGraph);
//		// currentGraph.addEdge(AC, A, C);
//		// }
//
//		boolean ruleApplied = false;
//		for (final Object2IntMap.Entry<Label> ABEntry : AB.labeledValueSet()) {
//			final Label labelAB = ABEntry.getKey();
//
//			if (labelAB.containsUnknown())
//				continue;// TABLE 3 ICAPS paper
//
//			final int x = ABEntry.getIntValue();
//			for (final Object2IntMap.Entry<Label> BCEntry : BC.labeledValueSet()) {
//				final Label labelBC = BCEntry.getKey();
//				final int y = BCEntry.getIntValue();
//				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//
//				final boolean isNegativePath = (x < reactionTime) && (y < 0);
//				final Label newLabelAC = (isNegativePath) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
//				if (newLabelAC == null) {
//					continue;
//				}
//				if (isNegativePath) {
//					// newLabelAC can contain ¿ literals. It is necessary to remove all children of unknown literals (TIME15)
//					CSTN.removeChildrenOfUnknown(newLabelAC);
//				}
//
//				int oldValue = AC.getValue(newLabelAC);
//				if (A == C) {
//					if (sum >= 0) {
//						// it would be a redundant edge
//						continue;
//					}
//					// sum is negative!
//					if (!newLabelAC.containsUnknown()) {
//						AC.mergeLabeledValue(newLabelAC, sum);
//						if (LOG.isLoggable(Level.FINER)) {
//							LOG.log(Level.FINER, "***\nFound a negative loop " + pairAsString(newLabelAC, sum) + " in the edge  " + AC + "\n***");
//						}
//						status.controllable = false;
//						status.finished = true;
//						status.labelPropagationRuleCalls++;
//						return true;
//					}
//					sum = Constants.INT_NEG_INFINITE;
//					// A node-loop with -infty value has to be propagated immediately to Z
//					LabeledIntEdge eAZ = currentGraph.findEdge(A, Z);// it cannot be null in a CSTN!
//					eAZ.mergeLabeledValue(newLabelAC, sum);
//					if (LOG.isLoggable(Level.FINER)) {
//						LOG.log(Level.FINER, "Found a negative qLoop on node " + A.getName() + ". Constraint to Z update: " + eAZ);
//					}
//				} else {
//					// in the case of A != C, a value is stored only if it is more negative than the current one.
//					if ((oldValue != Constants.INT_NULL) && (sum > oldValue)) {
//						continue;
//					}
//				}
//				// here sum has to be insert!
//				// I have to prepare the log before the execution of the merge!
//				String log = null;
//				if (LOG.isLoggable(Level.FINER)) {
//					log = "Label Propagation Rule applied to edge " + AC.getName()
//							+ ":\nsource: "
//							+ A.getName() + " ---" + pairAsString(labelAB, x) + "---> " + B.getName() + " ---" + pairAsString(labelBC, y) + "---> " + C.getName()
//							+ "\nresult: "
//							+ A.getName() + " ---" + pairAsString(newLabelAC, sum) + "---> " + C.getName()
//							+ "; old value: " + Constants.formatInt(oldValue);
//				}
//
//				if (AC.mergeLabeledValue(newLabelAC, sum)) {
//					ruleApplied = true;
//					status.labelPropagationRuleCalls++;
//					LOG.log(Level.FINER, log);
//				}
//			}
//		}
//		// if (AC.labeledValueSet().isEmpty()) {
//		// // This occurs only when a new edge is added at the start of this method but, then, no value can be added.
//		// currentGraph.removeEdge(AC);
//		// AC = null;
//		// }
//		return ruleApplied;
//	}
//
//	/**
//	 * Apply Morris lower case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *     l1, x     l2, c:y                    l1l2, x+y
//	 * A  &lt;-------C &lt;--------D and x&le;0 adds A &lt;-----------D
//	 * </pre>
//	 *
//	 * @param currentGraph the originating graph.
//	 * @param D
//	 * @param C
//	 * @param A
//	 * @param DC CANNOT BE NULL
//	 * @param CA CANNOT BE NULL
//	 * @param DA CANNOT BE NULL
//	 * @param Z
//	 * @param status CSTNUCheckStatus object representing the status of a checking algorithm run.
//	 * @return true if a reduction is applied at least
//	 */
//	@SuppressWarnings("static-method")
//	boolean lowerCaseRule(final LabeledIntGraph currentGraph, final LabeledNode D, final LabeledNode C, final LabeledNode A, final LabeledNode Z,
//			final LabeledIntEdge DC, final LabeledIntEdge CA, final LabeledIntEdge DA, CSTNUCheckStatus status) {
//
//		/*
//		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, String>>> DCMap = DC.getLowerLabelSet();
//		if (DCMap.size() == 0)
//			return false;
//		if (CA.getMinValue() == Constants.INT_NULL || CA.getMinValue() > 0 || A.equalsByName(C) || A.equalsByName(D))// rule conditions
//			return false;
//
//		LOG.finer("Lower Case Rule: start.");
//
//		final Set<Object2IntMap.Entry<Label>> CAMap = CA.labeledValueSet();
//
//		for (final Object2IntMap.Entry<Entry<Label, String>> entryDC : DCMap) {
//			final Label l2 = entryDC.getKey().getKey();
//			final int y = entryDC.getValue();
//			for (final Object2IntMap.Entry<Label> entryCA : CAMap) {
//				final int x = entryCA.getValue();
//				if (x > 0)// rule condition
//					continue;
//				final Label l1 = entryCA.getKey();
//				final Label l1l2 = (A == Z && x < 0) ? l1.conjunctionExtended(l2) : l1.conjunction(l2);
//				if (l1l2 == null) {
//					continue;
//				}
//				final int oldZ = DA.getValue(l1l2);
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(y, x);
//				final String oldDA = DA.toString();
//
//				if (DA.mergeLabeledValue(l1l2, z)) {
//					reductionApplied = true;
//					status.lowerCaseRuleCalls++;
//					LOG.finer("Lower Case applied to edge " + oldDA + ":\n"
//							+ "partic: " + A.getName() + " <---" + pairAsString(l1, x) + "--- " + C.getName()
//							+ " <---" + tripleAsString(l2, y, entryDC.getKey().getValue(), true) + "--- " + D.getName()
//							+ "\nresult: " + A.getName() + " <---" + pairAsString(l1l2, z) + "--- " + D.getName()
//							+ "; old value: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		LOG.finer("Lower Case Rule: end.");
//		return reductionApplied;
//	}
//
//	// /**
//	// * Determines the minimal distance between all pairs of vertexes of the given graph if the graph does not contain any negative cycles. If the graph
//	// * contains a negative cycle, the method stops and returns false (the graph is anyway modified).
//	// *
//	// * @param g
//	// * the graph
//	// * @return true if the input graph does not contain any negative cycle, false otherwise.
//	// */
//	// boolean minimalDistanceGraphFast(final LabeledIntGraph g) {
//	// final int n = g.getVertexCount();
//	// final LabeledNode[] node = g.getVerticesArray();
//	// LabeledNode iV, jV, kV;
//	// LabeledIntEdge ik, kj, ij;
//	// int v;
//	// Label l;
//	// LabeledIntMap ijMap = null;
//	// for (int k = 0; k < n; k++) {
//	// kV = node[k];
//	// for (int i = 0; i < n; i++) {
//	// iV = node[i];
//	// for (int j = 0; j < n; j++) {
//	// if ((k == i) && (i == j)) {
//	// continue;
//	// }
//	// jV = node[j];
//	// Label nodeLabelConjunction = iV.getLabel().conjunction(jV.getLabel());
//	// if (nodeLabelConjunction == null)
//	// continue;
//	//
//	// ik = g.findEdge(iV, kV);
//	// kj = g.findEdge(kV, jV);
//	// if ((ik == null) || (kj == null)) {
//	// continue;
//	// }
//	// ij = g.findEdge(iV, jV);
//	//
//	// final Set<Object2IntMap.Entry<Label>> ikMap = ik.labeledValueSet();
//	// final Set<Object2IntMap.Entry<Label>> kjMap = kj.labeledValueSet();
//	// if ((k == i) || (k == j)) {
//	// ijMap = labeledIntMapFactory.create(ij.getLabeledValueMap());// this is necessary to avoid concurrent access to the same map by the
//	// // iterator.
//	// } else {
//	// ijMap = null;
//	// }
//	// for (final Object2IntMap.Entry<Label> ikL : ikMap) {
//	// for (final Object2IntMap.Entry<Label> kjL : kjMap) {
//	// l = ikL.getKey().conjunction(kjL.getKey());
//	// if (l == null) {
//	// continue;
//	// }
//	// l = l.conjunction(nodeLabelConjunction);// It is necessary to propagate with node labels!
//	// if (l == null) {
//	// continue;
//	// }
//	// if (ij == null) {
//	// ij = CSTN.makeNewEdge(node[i].getName() + "_" + node[j].getName(), LabeledIntEdge.ConstraintType.derived, g);
//	// g.addEdge(ij, iV, jV);
//	// }
//	// v = ikL.getValue() + kjL.getValue();
//	// if (ijMap != null) {
//	// ijMap.put(l, v);
//	// } else {
//	// ij.mergeLabeledValue(l, v);
//	// }
//	// if (i == j) // check negative cycles
//	// if (v < 0 || ij.getMinValue() < 0) {
//	// LOG.finer("Found a negative cycle on node " + iV.getName() + ": "
//	// + ((ijMap != null) ? ijMap : ij) + "\nIn details, ik=" + ik + ", kj="
//	// + kj + ", v=" + v + ", ij.getValue(" + l + ")=" + ij.getValue(l));
//	// return false;
//	// }
//	// }
//	// }
//	// if (ijMap != null) {
//	// ij.setLabeledValue(ijMap);
//	// }
//	// }
//	// }
//	// }
//	// return true;
//	// }
//
//	/**
//	 * Adds 'edgesToAdd' to 'g' in all-max-projection way.<br>
//	 * Adds 'edgesToAdd' to 'g' considering, for each edge, only the ordinary labeled value and upper-case labeled values.
//	 * Upper-case labeled values are added as ordinary labeled values.<br>
//	 *
//	 * @param g a CSTN instance. It cannot be null! It is not modified.
//	 * @return the all-max projection of the graph g (CSTN graph).
//	 */
//	LabeledIntGraph makeAllMaxProjection(final LabeledIntGraph g) {
//		if (g == null)
//			return null;
//		LabeledIntGraph allMax = new LabeledIntGraph(labeledIntValueMap);
//		// clone all nodes
//		LabeledNode vNew;
//		for (final LabeledNode v : g.getVertices()) {
//			vNew = new LabeledNode(v);
//			allMax.addVertex(vNew);
//		}
//		allMax.setZ(allMax.getNode(g.getZ().getName()));
//
//		// clone all edges giving the right new endpoints corresponding the old ones.
//		AbstractLabeledIntEdge eNew;
//		LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeFactory<>(labeledIntValueMap);
//		for (final LabeledIntEdge e : g.getEdges()) {
//			eNew = edgeFactory.create(e);
//			eNew.setConstraintType(ConstraintType.normal);
//			eNew.getLowerLabelMap().clear();
//			for (final Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
//				eNew.mergeLabeledValue(entry.getKey().getKey(), entry.getValue());
//			}
//			eNew.getUpperLabelMap().clear();
//			allMax.addEdge(eNew, g.getSource(e).getName(), g.getDest(e).getName());
//		}
//		return allMax;
//	}
//
//	/**
//	 * Executes one step of the dynamic controllability check.<br>
//	 * Before the first execution of this method, it is necessary to execute {@link #initUpperLowerLabelDataStructure(LabeledIntGraph)}.
//	 * 
//	 * @param currentGraph the current graph. At the end of the procedure, it will contain the results of reductions.
//	 * @param status the record where to store statistics and exit status of the execution. BE CAREFULL, this procedure cannot verified if the DC is finished or
//	 *            not. So, the status.finished field is not update by this procedure.
//	 * @param edgesToCheck set of edges that have to be checked.
//	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
//	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
//	 *             there is a problem in the rules coding.
//	 */
//	public CSTNUCheckStatus oneStepDynamicControllability(final LabeledIntGraph currentGraph, final Collection<LabeledIntEdge> edgesToCheck,
//			final CSTNUCheckStatus status) throws WellDefinitionException {
//
//		LabeledNode A, B, C;
//		LabeledIntEdge AC, CB, edgeCopy;
//
//		final LabeledNode Z = currentGraph.getZ();
//
//		status.cycles++;
//
//		LOG.log(Level.FINER, "\nStart application labeled constraint generation and label removal rules.");
//
//		ObjectArraySet<LabeledIntEdge> newEdgesToCheck = new ObjectArraySet<>();// HAS TO BE A SET!
//		int i = 1, n = edgesToCheck.size();
//		for (LabeledIntEdge AB : edgesToCheck) {
//			if (LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, "Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
//			}
//			A = currentGraph.getSource(AB);
//			B = currentGraph.getDest(AB);
//			// initAndCheck does not resolve completely a qStar.
//			// It is necessary to check here the edge before to consider the second edge.
//			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
//			edgeCopy = currentGraph.getEdgeFactory().create(AB);
//			if (A.isObservator()) {
//				// R0 on the resulting new values
//				labelModificationR0(currentGraph, A, B, Z, AB, status);
//			}
//			labelModificationR3(currentGraph, A, B, Z, AB, status);
//			if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
//				// R0 on the resulting new values
//				labelModificationR0(currentGraph, A, B, Z, AB, status);
//			}
//
//			// LLR is put here because it works like R0 and R3
//			if (AB.getUpperLabelMap().size() > 0) {
//				caseLabelRemovalRule(currentGraph, A, B, AB, status);
//			}
//
//			if (!AB.equalsLabeledValues(edgeCopy)) {
//				newEdgesToCheck.add(AB);
//			}
//
//			/**
//			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
//			 * A-->B-->C
//			 */
//			boolean llm, ulm;
//			LOG.info("Rules, phase 1/2: edge " + AB.getName() + " as first component.");
//			for (LabeledIntEdge BC : currentGraph.getOutEdges(B)) {
//				C = currentGraph.getDest(BC);
//				if (B.equalsByName(C)) {
//					continue;// self loop on the second pair in not useful.
//				}
//
//				AC = currentGraph.findEdge(A, C);
//				// I need to preserve the old edge to compare below
//				edgeCopy = (AC != null) ? edgeCopy = currentGraph.getEdgeFactory().create(AC) : null;
//
//				if (AC == null) {
//					AC = CSTN.makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
//				}
//
//				this.labelPropagationRule(currentGraph, A, B, C, Z, AB, BC, AC, status);
//
//				/**
//				 * The following rule are called if there are condition (avoid to call for nothing)
//				 */
//				llm = AB.getLowerLabelMap().size() > 0;
//				if (llm) {
//					lowerCaseRule(currentGraph, A, B, C, Z, AB, BC, AC, status);
//				}
//				ulm = BC.getUpperLabelMap().size() > 0;
//				if (ulm) {
//					upperCaseRule(currentGraph, A, B, C, AB, BC, AC, status);
//				}
//				if (llm && ulm) {
//					crossCaseRule(currentGraph, A, B, C, AB, BC, AC, status);
//				}
//
//				if (!status.controllable)
//					return status;
//
//				if (edgeCopy == null) {
//					if (!AC.isEmpty()) {
//						currentGraph.addEdge(AC, A, C);
//						newEdgesToCheck.add(AC);
//					}
//				} else {
//					if (!AC.equalsLabeledValues(edgeCopy)) {
//						newEdgesToCheck.add(AC);
//					}
//				}
//			}
//			LOG.info("Rules, phase 1/2 done.");
//
//			/**
//			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
//			 * C-->A-->B
//			 */
//			LOG.info("Rules, phase 2/2: edge " + AB.getName() + " as second component.");
//			for (LabeledIntEdge CA : currentGraph.getInEdges(A)) {
//				C = currentGraph.getSource(CA);
//				if (A.equalsByName(B)) {
//					continue;// self loop on the second pair in not useful.
//				}
//
//				CB = currentGraph.findEdge(C, B);
//				edgeCopy = (CB != null) ? edgeCopy = currentGraph.getEdgeFactory().create(CB) : null;
//				if (CB == null) {
//					CB = CSTN.makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
//				}
//
//				labelPropagationRule(currentGraph, C, A, B, Z, CA, AB, CB, status);
//
//				llm = CA.getLowerLabelMap().size() > 0;
//				if (llm) {
//					lowerCaseRule(currentGraph, C, A, B, Z, CA, AB, CB, status);
//				}
//				ulm = AB.getUpperLabelMap().size() > 0;
//				if (ulm) {
//					upperCaseRule(currentGraph, C, A, B, CA, AB, CB, status);
//				}
//				if (llm && ulm) {
//					crossCaseRule(currentGraph, C, A, B, CA, AB, CB, status);
//				}
//
//				if (!status.controllable)
//					return status;
//
//				if (edgeCopy == null) {
//					if (!CB.isEmpty()) {
//						currentGraph.addEdge(CB, C, B);
//						newEdgesToCheck.add(CB);
//					}
//				} else {
//					if (!CB.equalsLabeledValues(edgeCopy)) {
//						newEdgesToCheck.add(CB);
//					}
//				}
//			}
//			LOG.info("Rules phase 2/2 done.\n");
//		}
//		LOG.log(Level.FINER, "End application all rules.");
//		edgesToCheck.clear();// in any case, this set has been elaborated. It is better to clear it out.
//		status.finished = newEdgesToCheck.size() == 0;
//		if (!status.finished) {
//			edgesToCheck.addAll(newEdgesToCheck);
//		}
//		// LOG.info("AllMax Projection check starts...");
//		// LabeledIntGraph allMaxCSTN = makeAllMaxProjection(currentGraph);
//		// CSTN cstnChecker = new CSTN(this.reactionTime, this.useΩ, labeledIntValueMap);
//		// CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck(allMaxCSTN);
//		// LOG.info("AllMax Projection check done.\n");
//		// if (!cstnStatus.consistency) {
//		// LOG.info("The AllMax Projection graph has at leat one negative loop at the start of cycle " + status.cycles + ": stop!");
//		// status.controllable = false;
//		// status.finished = true;
//		// }
//		return status;
//	}
//
//	/**
//	 * Apply Morris upper case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *     l1, B:x     l2, y             l1l2, B:x+y
//	 * A &lt;----------C &lt;--------D adds A &lt;-------------D
//	 * </pre>
//	 *
//	 * @param currentGraph the originating graph.
//	 * @param D
//	 * @param C
//	 * @param A
//	 * @param DC CANNOT BE NULL
//	 * @param CA CANNOT BE NULL
//	 * @param DA CANNOT BE NULL
//	 * @param status CSTNUCheckStatus object representing the status of a checking algorithm run.
//	 * @return true if a reduction is applied at least
//	 */
//	@SuppressWarnings("static-method")
//	boolean upperCaseRule(final LabeledIntGraph currentGraph, final LabeledNode D, final LabeledNode C, final LabeledNode A, final LabeledIntEdge DC,
//			final LabeledIntEdge CA, final LabeledIntEdge DA, CSTNUCheckStatus status) {
//
//		/*
//		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, String>>> CAMap = CA.getUpperLabelSet();
//		if (CAMap.size() == 0)
//			return false;
//		// if (C.equalsByName(D) || A.equalsByName(D)) continue;// it is useless to consider self loop. NO! it is necessary with guarded links!
//		LOG.finest("Upper Case Rule: start.");
//
//		final Set<Object2IntMap.Entry<Label>> DCMap = DC.labeledValueSet();
//
//		for (final Object2IntMap.Entry<Entry<Label, String>> entryCA : CAMap) {
//			final Label l1 = entryCA.getKey().getKey();
//			final String upperCaseNode = entryCA.getKey().getValue();
//			final int x = entryCA.getValue();
//
//			for (final Object2IntMap.Entry<Label> entryDC : DCMap) {
//				final Label l2 = entryDC.getKey();
//				final Label l1l2 = l1.conjunction(l2);
//				if (l1l2 == null)
//					continue;
//				final int y = entryDC.getValue();
//				final int oldZ = DA.getUpperLabelValue(l1l2, upperCaseNode);
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//				final String oldDA = DA.toString();
//				// if ((oldZ != null) && (oldZ <= z)) continue;
//
//				if (DA.mergeUpperLabelValue(l1l2, upperCaseNode, z)) {
//					reductionApplied = true;
//					status.upperCaseRuleCalls++;
//					LOG.finer("Upper Case applied to edge " + oldDA + ":\n" + "partic: " 
//							+ A.getName() + " <---" + tripleAsString(l1, x, upperCaseNode, false) +"--- " + C.getName() + " <---" + pairAsString(l2, y) +"--- " + D.getName() 
//							+ "\nresult: " 
//							+ A.getName() + " <---" + tripleAsString(l1l2, z, upperCaseNode, false) + "--- " + D.getName()
//							+ "; old value: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		LOG.finest("Upper Case Rule: end.");
//		return reductionApplied;
//	}
//
//}
