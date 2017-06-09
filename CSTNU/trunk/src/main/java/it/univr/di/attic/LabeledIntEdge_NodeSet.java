///**
// *
// */
//package it.univr.di.cstnu.graph;
//
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.SortedSet;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.LabeledIntMapFactory;
//import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
//import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;
//
///**
// * It contains all information of a CST edge including the information about node sets associated to each labeled value.
// *
// * @author posenato
// * @version $Id: $Id
// */
//public class LabeledIntEdge_NodeSet extends AbstractLabeledIntEdge {
//
//	/**
//	 * logger
//	 */
//	static final Logger LOG = Logger.getLogger(LabeledIntEdge_NodeSet.class.getName());
//
//	/**
//	 *
//	 */
//	private static final long serialVersionUID = 2L;
//
////	/**
////	 * A transformer to return a font for edge label.
////	 *
////	 * @Param <K>
////	 */
////	@SuppressWarnings({ "unchecked", "rawtypes" })
////	public static Transformer<LabeledIntEdge_NodeSet, Font> edgeFontTransformer = new ConstantTransformer(new Font("Helvetica", Font.PLAIN, 14));
////	
////	/**
////	 * A transformer to return a label for the edge
////	 */
////	public static final ToStringLabeller<LabeledIntEdge_NodeSet> edgeLabelTransformer = new ToStringLabeller<LabeledIntEdge_NodeSet>() {
////		/**
////		 * Returns a label for the edge
////		 */
////		@Override
////		public String transform(final LabeledIntEdge_NodeSet e) {
////			final StringBuffer sb = new StringBuffer();
////			sb.append((e.getName().length() == 0 ? "''" : e.getName()));
////			sb.append("; ");
////			if (e.getLabeledValueMap().size() > 0) {
////				sb.append(e.getLabeledValueMap().toString());
////			}
////			if (e.getUpperLabelSet().size() > 0) {
////				sb.append("; UL: ");
////				sb.append(e.upperLabelsToString());
////			}
////			if (e.getLowerLabelSet().size() > 0) {
////				sb.append("; LL:");
////				sb.append(e.lowerLabelsToString());
////			}
////
////			return sb.toString();
////		}
////	};
////	
////	
////	/**
////	 * Select how to draw an edge given its type.
////	 */
////	public final static Transformer<LabeledIntEdge_NodeSet, Stroke> edgeStrokeTransformer = new Transformer<LabeledIntEdge_NodeSet, Stroke>() {
////		@Override
////		public Stroke transform(final LabeledIntEdge_NodeSet s) {
////			switch (s.getType()) {
////				case normal:
////					return AbstractLabeledIntEdge.normalEdgeStroke;
////				case contingent:
////					return AbstractLabeledIntEdge.contingentEdgeStroke;
////				case constraint:
////					return AbstractLabeledIntEdge.constraintEdgeStroke;
////				default:
////					return AbstractLabeledIntEdge.derivedEdgeStroke;
////			}
////		}
////	};
//
//
//	/**
//	 * Factory for LabeledIntMap
//	 */
//	LabeledIntMapFactory labeledIntMapFactory = new LabeledIntMapFactory(LabeledIntMapFactory.Type.LabeledIntTreeMap);
//
//	/**
//	 * Labeled value.
//	 */
//	private LabeledIntNodeSetMap labeledValue;
//
//	/**
//	 * Default constructor: empty name, derived type and no labeled value.
//	 */
//	public LabeledIntEdge_NodeSet() {
//		super();
//		this.labeledValue = new LabeledIntNodeSetTreeMap();
//	}
//
//	/**
//	 * A simple constructor cloner.
//	 *
//	 * @param e
//	 *            edge to clone. If null, an empty edge is created with type = normal.
//	 */
//	public LabeledIntEdge_NodeSet(final LabeledIntEdge e) {
//		super(e);
//		if (e != null && (e instanceof LabeledIntEdge_NodeSet)) {
//			LabeledIntEdge_NodeSet e1 = (LabeledIntEdge_NodeSet) e;
//			this.labeledValue = new LabeledIntNodeSetTreeMap(e1.labeledValue);
//		}
//	}
//
//	/**
//	 * <p>
//	 * Constructor for LabeledIntEdge.
//	 * </p>
//	 *
//	 * @param n
//	 *            a {@link java.lang.String} object.
//	 */
//	public LabeledIntEdge_NodeSet(final String n) {
//		super(n);
//		this.labeledValue = new LabeledIntNodeSetTreeMap();
//	}
//
//	/**
//	 * Default constructor: empty name, derived type and no labeled value.
//	 *
//	 * @param name
//	 *            a {@link java.lang.String} object.
//	 * @param t
//	 *            the type of the edge
//	 */
//	public LabeledIntEdge_NodeSet(final String name, final ConstraintType t) {
//		this(name);
//		this.setConstraintType(t);
//	}
//
//	/**
//	 * Default constructor
//	 *
//	 * @param name
//	 *            a {@link java.lang.String} object.
//	 * @param t
//	 *            a {@link it.univr.di.cstnu.graph.LabeledIntEdge.Type} object.
//	 * @param l
//	 *            a {@link it.univr.di.labeledvalue.Label} object.
//	 * @param i
//	 *            an int.
//	 */
//	public LabeledIntEdge_NodeSet(final String name, final ConstraintType t, final Label l, final int i) {
//		this(name, t);
//		this.labeledValue.put(l, i);
//	}
//
//	/**
//	 * Clear (remove) all labeled values associated to this edge.
//	 */
//	public void clear() {
//		super.clear();
//		this.labeledValue.clear();
//	}
//
//	/**
//	 * Clears all ordinary labeled values.
//	 */
//	public void clearLabels() {
//		this.removedLabeledValue.clear();
//		this.labeledValue.clear();
//	}
//
//	@Override
//	public LabeledIntEdge createLabeledIntEdge() {
//		return new LabeledIntEdge_NodeSet();
//	}
//
//	@Override
//	public LabeledIntEdge createLabeledIntEdge(LabeledIntEdge e) {
//		return new LabeledIntEdge_NodeSet(e);
//	}
//
//	@Override
//	public LabeledIntMap getLabeledValueMap() {
//		LabeledIntNodeSetMap map = this.getLabeledValueNodeSetMap();
//		LabeledIntMap map1 = labeledIntMapFactory.create();
//		for (Entry<Label, Integer> entry : map.entrySet()) {
//			map1.put(entry.getKey(), entry.getValue());
//		}
//		return map1;
//	}
//
//	/**
//	 * <p>
//	 * getLabeledValueMap.
//	 * </p>
//	 *
//	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
//	 */
//	public LabeledIntNodeSetMap getLabeledValueNodeSetMap() {
//		return this.labeledValue;
//	}
//
//	/**
//	 * <p>
//	 * getMinValue.
//	 * </p>
//	 *
//	 * @return the minimal value among all ordinary labeled values if there are some values, {@link Constants#INT_NULL}
//	 *         otherwise.
//	 */
//	public int getMinValue() {
//		return this.labeledValue.getMinValue();
//	}
//
//	/**
//	 * <p>
//	 * getMinValueAmongLabelsWOUnknown.
//	 * </p>
//	 *
//	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some;
//	 *         {@link Constants#INT_NULL} otherwise.
//	 */
//	public int getMinValueAmongLabelsWOUnknown() {
//		return this.labeledValue.getMinValueAmongLabelsWOUnknown();
//	}
//
//	/**
//	 * <p>
//	 * getMinValueConsistentWith.
//	 * </p>
//	 *
//	 * @param l
//	 *            a {@link it.univr.di.labeledvalue.Label} object.
//	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
//	 */
//	public int getMinValueConsistentWith(final Label l) {
//		return this.labeledValue.getMinValueConsistentWith(l);
//	}
//
//	/**
//	 * <p>
//	 * getMinValueConsistentWith.
//	 * </p>
//	 *
//	 * @param l
//	 *            the scenario label
//	 * @param upperL
//	 *            the Upper Label
//	 * @return the value associated to the upper label of the occurrence of node n if it exists or the minimal values among the labels subsumed by l if one
//	 *         exists, null otherwise.
//	 */
//	public int getMinValueConsistentWith(final Label l, final String upperL) {
//		return this.upperLabel.getMinValueConsistentWith(l, upperL);
//	}
//
//	/**
//	 * <p>
//	 * getNodeSet.
//	 * </p>
//	 *
//	 * @param label
//	 *            label
//	 * @return the node set associated to label it it exists, null otherwise.
//	 */
//	public SortedSet<String> getNodeSet(final Label label) {
//		return this.labeledValue.getNodeSet(label);
//	}
//
//	/**
//	 * <p>
//	 * getValue.
//	 * </p>
//	 *
//	 * @param label
//	 *            label
//	 * @return the value associated to label it it exists, {@link Constants#INT_NULL} otherwise.
//	 */
//	public int getValue(final Label label) {
//		return this.labeledValue.get(label);
//	}
//
//	/**
//	 * <p>
//	 * labeledValueSet.
//	 * </p>
//	 *
//	 * @return the labeled values as a set
//	 */
//	public Set<Object2IntMap.Entry<Label>> labeledValueSet() {
//		return this.labeledValue.entrySet();
//	}
//
//	/**
//	 * Merges the labeled value i to the set of labeled values of this edge.
//	 *
//	 * @param l
//	 *            a {@link it.univr.di.labeledvalue.Label} object.
//	 * @param i
//	 *            a integer.
//	 * @return true if the operation was successful, false otherwise.
//	 */
//	public boolean mergeLabeledValue(final Label l, final int i) {
//		if ((l == null) || (i == Constants.INT_NULL))
//			return false;
//		return mergeLabeledValue(l, i, null);
//	}
//
//	/**
//	 * Merges the labeled value i to the set of labeled values of this edge.
//	 *
//	 * @param l
//	 *            a {@link it.univr.di.labeledvalue.Label} object.
//	 * @param i
//	 *            an integer.
//	 * @param s
//	 *            the node set to add.
//	 * @return true if the operation was successful, false otherwise.
//	 */
//	public boolean mergeLabeledValue(final Label l, final int i, Set<String> s) {
//		if ((l == null) || (i == Constants.INT_NULL))
//			return false;
//		final int oldValue = this.removedLabeledValue.getInt(l);
//		if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
//			// The new value is greater or equal the old one, the new value can be ignored.
//			// the labeled value (l,i) was already removed by label modification rule. So, it will be not stored.
//			if (LOG.isLoggable(Level.FINEST))
//				LabeledIntEdge_NodeSet.LOG.log(Level.FINEST,
//						"The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", "
//								+ oldValue + ") is in the removed list");
//			return false;
//		}
//		boolean exit = this.labeledValue.put(l, i, s);
//		return exit;
//	}
//
//	@Override
//	public void mergeLabeledValue(final LabeledIntMap map) {
//		for (Object2IntMap.Entry<Label> entry : map.entrySet())
//			this.labeledValue.put(entry.getKey(), entry.getIntValue());
//	}
//
//	@Override
//	public boolean putLabeledValue(final Label l, final int i) {
//		return this.labeledValue.putForcibly(l, i) != Constants.INT_NULL;
//	}
//
//	@Override
//	public int removeLabel(final Label l) {
//		LabeledIntEdge_NodeSet.LOG.finer("Removing label '" + l + "' from the edge " + this.toString());
//		return this.labeledValue.remove(l);
//	}
//
//	@Override
//	public void setLabeledValue(final LabeledIntMap labeledValue) {
//		LabeledIntNodeSetMap map = new LabeledIntNodeSetTreeMap();
//		for (Object2IntMap.Entry<Label> entry : labeledValue.entrySet()) {
//			map.put(entry.getKey(), entry.getIntValue());
//		}
//		this.labeledValue = map;
//	}
//
//	@Override
//	public int size() {
//		return this.labeledValue.size();
//	}
//
//	/**
//	 * A copy by reference of internal structure of edge e. Only optimize field cannot be update because it is read-only.
//	 *
//	 * @param e
//	 *            edge to clone. If null, it returns doing nothing.
//	 */
//	public void takeIn(final LabeledIntEdge_NodeSet e) {
//		if (e == null)
//			return;
//		this.constraintType = e.constraintType;
//		this.labeledValue = e.labeledValue;
//		this.upperLabel = e.upperLabel;
//		this.lowerLabel = e.lowerLabel;
//		this.removedLabeledValue = e.removedLabeledValue;
//		this.removedUpperLabel = e.removedUpperLabel;
//	}
//
//	/**
//	 * {@inheritDoc} Return a string representation of labeled values.
//	 */
//	@Override
//	public String toString() {
//		return "❮" + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getConstraintType() + "; "
//				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
//				+ ((this.upperLabel.size() > 0) ? "UL: " + this.upperLabel.toString() + "; " : "")
//				+ ((this.lowerLabel.size() > 0) ? "LL: " + this.lowerLabel.toString(true) + ";" : "")
//				// + ((this.nodeSetOfLabel.size() > 0) ? "NodeSet: " + this.nodeSetOfLabel.toString() + ";" : "")
//				+ "❯";
//	}
//
//}