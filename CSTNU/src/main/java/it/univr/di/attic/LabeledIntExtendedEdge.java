///**
// * 
// */
//package it.univr.di.attic;
//
//import it.univr.di.cstnu.graph.Component;
//import it.univr.di.cstnu.graph.LabeledNode;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
//
//import java.awt.BasicStroke;
//import java.awt.Paint;
//import java.awt.Stroke;
//import java.util.AbstractMap.SimpleEntry;
//import java.util.HashSet;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.logging.Logger;
//
//import org.apache.commons.collections15.Factory;
//import org.apache.commons.collections15.Transformer;
//
//import edu.uci.ics.jung.visualization.RenderContext;
//import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
//import edu.uci.ics.jung.visualization.picking.PickedInfo;
//
///**
// * It contains all information of a CSTPU edge. 
//  VERSIONE ORIGINALE CON LABELEDVALUEMAP<INTEGER>
//
// * 
// * @author posenato
// */
//public class LabeledIntExtendedEdge extends Component {
//	/**
//	 * Possible types
//	 * 
//	 * @author posenato
//	 */
//	public static enum Type {
//		/**
//		 * The edge represents an execution precedence between two nodes one.
//		 */
//		normal,
//
//		/** The edge represents a contingent (not shrinkable) constraint. */
//		contingent,
//
//		/**
//		 * The edge represents a generic constraint. In the CNSTU is equivalent to normal edge, but it is graphically
//		 * represented in a
//		 * different way.
//		 */
//		constraint,
//
//		/**
//		 * The edge represents a constraint derived by the controllability check algorithm.
//		 */
//		derived,
//
//		/**
//		 * The edge represents an internal one used to represent high level construct as a WORKFLOW OR JOIN.
//		 */
//		internal
//	}
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//
//	/**
//	 * To provide a unique id for the default creation of component.
//	 */
//	static int idSeq = 0;
//
//	/**
//	 * logger
//	 */
//	static Logger LOG = Logger.getLogger(LabeledIntExtendedEdge.class.getName());
//
//	// GRAPHICS & VISUALIZATION STUFF
//	// Set up a new stroke Transformer for the edges
//	/**
//	 * Simple stroke object to draw a 'derived' type edge.
//	 */
//	static final Stroke derivedEdgeStroke = RenderContext.DOTTED;
//	// new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);
//
//	/**
//	 * Simple stroke object to draw a 'standard' type edge.
//	 */
//	static final Stroke normalEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);
//
//	/**
//	 * Simple stroke object to draw a 'standard' type edge.
//	 */
//	static final Stroke contingentEdgeStroke = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);
//
//	/**
//	 * Simple stroke object to draw a 'standard' type edge.
//	 */
//	static final Stroke constraintEdgeStroke = RenderContext.DASHED;
//
//	/**
//	 * Select how to draw an edge given its type.
//	 */
//	public static Transformer<LabeledIntExtendedEdge, Stroke> edgeStrokeTransformer = new Transformer<LabeledIntExtendedEdge, Stroke>() {
//		@Override
//		public Stroke transform(LabeledIntExtendedEdge s) {
//			switch (s.getType()) {
//				case normal:
//					return LabeledIntExtendedEdge.normalEdgeStroke;
//				case contingent:
//					return LabeledIntExtendedEdge.contingentEdgeStroke;
//				case constraint:
//					return LabeledIntExtendedEdge.constraintEdgeStroke;
//				default:
//					return LabeledIntExtendedEdge.derivedEdgeStroke;
//			}
//		}
//	};
//
//	/**
//	 * A transformer to return a label for the edge
//	 */
//	public static final ToStringLabeller<LabeledIntExtendedEdge> edgeLabelTransformer = new ToStringLabeller<LabeledIntExtendedEdge>() {
//		/**
//		 * Returns a label for the edge
//		 */
//		@Override
//		public String transform(LabeledIntExtendedEdge e) {
//			final StringBuffer sb = new StringBuffer();
//			final Integer value = e.getInitialValue();
//			sb.append((e.getName().length() == 0 ? "''" : e
//					.getName()));
//			sb.append("; ");
//			if (value != null) {
//				if (value.equals(Constants.INT_POS_INFINITE)) {
//					sb.append("" + Constants.INFINITY_SYMBOL);
//				} else {
//					sb.append(value);
//				}
//			}
//			// LOG.finest("LabeledIntEdge: "+ e.name);
//			if (e.getLabeledValueMap().size() > 0) {
//				sb.append("; ");
//				sb.append(e.getLabeledValueMap().toString());
//			}
//			if (e.getUpperLabelSet().size() > 0) {
//				sb.append("; UL: ");
//				sb.append(e.upperLabelsToString());
//			}
//			if (e.getLowerLabelSet().size() > 0) {
//				sb.append("; LL:");
//				sb.append(e.lowerLabelsToString());
//			}
//
//			return sb.toString();
//		}
//	};
//
//	/**
//	 * A different kind of equals. It allows one to compare two edges with respect to their labeled values.
//	 *
//	 * @param e a not null edge
//	 * @return true if edge e contains labeled values equal to the current edge ones.
//	 */
//	public boolean equalsLabeledValues(LabeledIntExtendedEdge e) {
//		if (e == null) return false;
//		if (e == this) return true;
//
//		return (this.getLabeledValueMap().equals(e.getLabeledValueMap())
//				&& this.getLowerLabelSet().equals(e.getLowerLabelSet())
//				&& this.getUpperLabelSet().equals(e.getUpperLabelSet()));
//	}
//
//	/**
//	 * Returns a transformer to select the color that is used to draw the edge. This transformer uses the type and the
//	 * state of the edge to
//	 * select the color.
//	 * 
//	 * @param pi
//	 * @param pickedPaint
//	 * @param normalPaint
//	 * @param contingentPaint
//	 * @param derivedPaint
//	 * @return a transformer object to draw an edge with a different color when it is picked.
//	 */
//	public static final Transformer<LabeledIntExtendedEdge, Paint> edgeDrawPaintTransformer(final PickedInfo<LabeledIntExtendedEdge> pi, final Paint pickedPaint, final Paint normalPaint,
//			final Paint contingentPaint, final Paint derivedPaint) {
//
//		final Paint[] paintMap = new Paint[] { normalPaint, contingentPaint, normalPaint, derivedPaint };
//
//		return new Transformer<LabeledIntExtendedEdge, Paint>() {
//			@Override
//			public Paint transform(LabeledIntExtendedEdge e) {
//				if (e == null) return normalPaint;
//				// LabeledIntEdge.LOG.finer("LabeledIntEdge: " + e + ", picked: " + pi.isPicked(e));
//				if (pi.isPicked(e)) return pickedPaint;
//				return paintMap[e.getType().ordinal()];
//			}
//		};
//	}
//
//	/**
//	 * An edge is usually draw as an arc between two points. The area delimited by the arc and the straight line
//	 * connecting the two edge
//	 * points can be filled by a color.
//	 * 
//	 * @param normalPaint
//	 * @param contingentPaint
//	 * @param derivedPaint
//	 * @return a transformer object to fill an edge 'area' with a color depending on edge type.
//	 */
//	static final Transformer<LabeledIntExtendedEdge, Paint> edgeFillPaintTransformer(final Paint normalPaint,
//			final Paint contingentPaint, final Paint derivedPaint) {
//		return new Transformer<LabeledIntExtendedEdge, Paint>() {
//			/**
//			 * logger
//			 */
//			final Paint[] paintMap = { normalPaint, contingentPaint, derivedPaint };
//
//			@Override
//			public Paint transform(LabeledIntExtendedEdge e) {
//				if (e == null) return normalPaint;
//				return this.paintMap[e.getType().ordinal()];
//			}
//		};
//	}
//
//	/**
//	 * @return a simple factory!
//	 */
//	public static Factory<LabeledIntExtendedEdge> getFactory() {
//		return new Factory<LabeledIntExtendedEdge>() {
//			@Override
//			public LabeledIntExtendedEdge create() {
//				final LabeledIntExtendedEdge e = new LabeledIntExtendedEdge();
//				return e;
//			}
//		};
//	}
//
//	/**
//	 * @param label
//	 * @return a Transformer useful to select the minimal values associated to a label
//	 */
//	public static Transformer<LabeledIntExtendedEdge, Integer> getTransformer(final Label label) {
//		return new Transformer<LabeledIntExtendedEdge, Integer>() {
//			/**
//			 * logger
//			 */
//			Logger LOG1 = Logger.getLogger(Transformer.class.getName());
//
//			@Override
//			public Integer transform(LabeledIntExtendedEdge link) {
//				if ((label == null) || (link == null)) {
//					this.LOG1.finer("At least, one is null. Label: " + label + ". Link: " + link);
//					return null;
//				}
//				this.LOG1.finer("Label " + label.toString() + " tranform(" + link.toString() + "):"
//						+ link.getValue(label));
//				return link.getValue(label);
//			}
//		};
//	}
//
//	// @SuppressWarnings({ "unused", "javadoc" })
//	// public static void main(String[] args) {
//	// final Literal a = new Literal('a'), b = new Literal('b'), c = new Literal('c', State.negated), d = new Literal('d');
//	//
//	// final Label l1 = new Label();
//	// l1.conjunct(d);
//	// l1.conjunct(b);
//	// System.out.printf("Label l1 = %s\n", l1);
//	//
//	// final Label l2 = new Label(b);
//	// l2.conjunct(d.getComplement());
//	// System.out.printf("Label l2 = %s\n", l2);
//	//
//	// // LabeledIntEdge edge = new LabeledIntEdge(l1, 2), edge1 = new LabeledIntEdge("e2", Type.normal, l2, 3);
//	//
//	// // System.out.printf("LabeledIntEdge with no name and label l1: %s\n", edge);
//	// // System.out.printf("LabeledIntEdge e2 with label l2: %s\n", edge1);
//	// }
//
//	/**
//	 * The type of the edge.
//	 */
//	private Type type;
//
//	/**
//	 * Original value of the represented constraint
//	 */
//	private Integer initialValue;
//
//	/**
//	 * Original value of the upper/lower case
//	 */
//	// private String initialCaseLabel;
//
//	/**
//	 * Labeled value.
//	 */
//	private LabeledValueTreeMap<Integer> labeledValue;
//
//	/**
//	 * Removed Labeled value.<br>
//	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order
//	 * to avoid to add it a
//	 * second time.
//	 */
//	private LabeledValueTreeMap<Integer> removedLabeledValue;
//
//	/**
//	 * Upper case Morris Labels
//	 */
//	private LabeledContingentIntTreeMap<Integer> upperLabel;
//
//	/**
//	 * Removed Upper Case Labeled value.<br>
//	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order
//	 * to avoid to add it a
//	 * second time.
//	 */
//	private LabeledContingentIntTreeMap<Integer> removedUpperLabel;
//
//	/**
//	 * Upper case Morris Labels
//	 */
//	private LabeledContingentIntTreeMap<Integer> lowerLabel;
//
//	/**
//	 * To activate all optimization code in order to remove the redundant label in the set.
//	 */
//	private boolean optimize;
//
//	/**
//	 * Default constructor: empty name, derived type and no labeled value.
//	 */
//	public LabeledIntExtendedEdge() {
//		super("e" + LabeledIntExtendedEdge.idSeq++);// if you change this default, change also in GraphMLReader
//		setType(Type.normal);
//		this.initialValue = null;
//		// this.initialCaseLabel = null;
//		this.labeledValue = new LabeledValueTreeMap<>(optimize);
//		this.upperLabel = new LabeledContingentIntTreeMap<>();
//		this.lowerLabel = new LabeledContingentIntTreeMap<>();
//		this.removedLabeledValue = new LabeledValueTreeMap<>(optimize);
//		this.removedUpperLabel = new LabeledContingentIntTreeMap<>();
//	}
//
//	/**
//	 * A simple constructor cloner.
//	 * 
//	 * @param e edge to clone. If null, an empty edge is created with type = normal.
//	 */
//	public LabeledIntExtendedEdge(LabeledIntExtendedEdge e) {
//		super(e);
//		if (e == null) {
//			setType(Type.normal);
//			this.initialValue = null;
//			// this.initialCaseLabel = null;
//			this.labeledValue = new LabeledValueTreeMap<>(optimize);
//			this.upperLabel = new LabeledContingentIntTreeMap<>();
//			this.lowerLabel = new LabeledContingentIntTreeMap<>();
//			this.removedLabeledValue = new LabeledValueTreeMap<>(optimize);
//			this.removedUpperLabel = new LabeledContingentIntTreeMap<>();
//		} else {
//			this.type = e.type;
//			this.initialValue = e.initialValue;
//			this.labeledValue = new LabeledValueTreeMap<>(e.labeledValue, optimize);
//			this.upperLabel = new LabeledContingentIntTreeMap<>(e.upperLabel);
//			this.lowerLabel = new LabeledContingentIntTreeMap<>(e.lowerLabel);
//			this.removedLabeledValue = new LabeledValueTreeMap<>(e.removedLabeledValue, false);
//			this.removedUpperLabel = new LabeledContingentIntTreeMap<>(e.removedUpperLabel);
//		}
//	}
//
//	/**
//	 * @param n
//	 */
//	public LabeledIntExtendedEdge(String n) {
//		super(n);
//		setType(Type.normal);
//		this.initialValue = null;
//		// this.initialCaseLabel = null;
//		this.labeledValue = new LabeledValueTreeMap<>(optimize);
//		this.upperLabel = new LabeledContingentIntTreeMap<>();
//		this.lowerLabel = new LabeledContingentIntTreeMap<>();
//		this.removedLabeledValue = new LabeledValueTreeMap<>(false);
//		this.removedUpperLabel = new LabeledContingentIntTreeMap<>();
//	}
//
//	/**
//	 * Default constructor: empty name, derived type and no labeled value.
//	 * 
//	 * @param name
//	 * @param t the type of the edge
//	 */
//	public LabeledIntExtendedEdge(String name, Type t) {
//		this(name);
//		setType(t);
//	}
//
//	/**
//	 * Default constructor
//	 * 
//	 * @param name
//	 * @param t
//	 * @param l
//	 * @param i
//	 */
//	public LabeledIntExtendedEdge(String name, Type t, Label l, int i) {
//		this(name, t);
//		this.labeledValue.putLabeledValue(l, i);
//		this.initialValue = i;
//	}
//
//	/**
//	 * Clears all ordinary labeled values.
//	 */
//	public void clearLabels() {
//		this.labeledValue.clear();
//	}
//
//	/**
//	 * Clears all lower case labeled values.
//	 */
//	public void clearLowerLabels() {
//		this.lowerLabel.clear();
//	}
//
//	/**
//	 * Clears all upper case labeled values.
//	 */
//	public void clearUpperLabels() {
//		this.upperLabel.clear();
//	}
//
//	/**
//	 * @return the initialValue
//	 */
//	public Integer getInitialValue() {
//		return this.initialValue;
//	}
//
//	/**
//	 * @return the initialCaseLabel
//	 *         public String getInitialCaseLabel() {
//	 *         return initialCaseLabel;
//	 *         }
//	 */
//
//	/**
//	 * @param initialCaseLabel the initialCaseLabel to set
//	 *            public void setInitialCaseLabel(String initialCaseLabel) {
//	 *            this.initialCaseLabel = initialCaseLabel;
//	 *            }
//	 */
//
//	/**
//	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
//	 */
//	public LabeledValueTreeMap<Integer> getLabeledValueMap() {
//		return this.labeledValue;
//	}
//
//	/**
//	 * @return the labeled values that cannot be further added to the labeled value set of this edge
//	 */
//	public LabeledValueTreeMap<Integer> getRemovedLabeledValuesMap() {
//		return this.removedLabeledValue;
//	}
//
//	/**
//	 * @return the map of Lower Case Labels of the edge.
//	 */
//	public Set<Entry<Entry<Label, String>, Integer>> getLowerLabelSet() {
//		return this.lowerLabel.labeledTripleSet();
//	}
//
//	/**
//	 * Returns the value associated to the lower label of the occurrence of node n if it exists, null otherwise;
//	 * 
//	 * @param l
//	 * @param n
//	 * @return the value associated to the labeled wait if it exists.
//	 */
//	public Integer getLowerLabelValue(Label l, LabeledNode n) {
//		return this.lowerLabel.getValue(l, n.getName());
//	}
//
//	/**
//	 * @return the minimal value among all Lower Case Label
//	 */
//	public Integer getMinLowerLabeledValue() {
//		return this.lowerLabel.getMinValue();
//	}
//
//	/**
//	 * @return the minimal value among all Upper Case Label
//	 */
//	public Integer getMinUpperLabeledValue() {
//		return this.upperLabel.getMinValue();
//	}
//
//	/**
//	 * @return the minimal value among all ordinary labeled values. Null if there are no values.
//	 */
//	public Integer getMinValue() {
//		return this.labeledValue.getMinValue();
//	}
//
//	/**
//	 * @return the type
//	 */
//	public Type getType() {
//		return this.type;
//	}
//
//	/**
//	 * @return the Upper-Case labeled Value
//	 */
//	public LabeledContingentIntTreeMap<Integer> getUpperLabelMap() {
//		return this.upperLabel;
//	}
//
//	/**
//	 * @return the Upper-Case labeled Value
//	 */
//	public LabeledContingentIntTreeMap<Integer> getLowerLabelMap() {
//		return this.lowerLabel;
//	}
//
//	/**
//	 * @return he map of Upper Case Labels of the edge.
//	 */
//	public Set<Entry<Entry<Label, String>, Integer>> getUpperLabelSet() {
//		return this.upperLabel.labeledTripleSet();
//	}
//
//	/**
//	 * @return the set of all ordinary labeled values and upper case ones.
//	 */
//	public Set<Entry<Entry<Label, String>, Integer>> getAllUpperCaseAndOrdinaryLabeledValuesSet() {
//		// Merge all possible labeled values and Upper Case labeled values of edges between Y and X in a single set.
//		Set<Entry<Entry<Label, String>, Integer>> globalLabeledValueSet = new HashSet<>(this.getUpperLabelSet());
//		for (Entry<Label, Integer> entry : this.labeledValueSet()) {
//			Entry<Label, String> e = new SimpleEntry<>(entry.getKey(), Constants.EMPTY_UPPER_CASE_LABELstring);
//			globalLabeledValueSet.add(new SimpleEntry<>(e, entry.getValue()));
//		}
//		return globalLabeledValueSet;
//	}
//
//	/**
//	 * @param l
//	 * @param n
//	 * @return the value associated to the upper label of the occurrence of node n if it exists, null otherwise.
//	 */
//	public Integer getUpperLabelValue(Label l, LabeledNode n) {
//		return this.upperLabel.getValue(l, n.getName());
//	}
//
//	/**
//	 * @param l
//	 * @param name
//	 * @return the value associated to the upper label of the occurrence of node n if it exists, null otherwise.
//	 */
//	public Integer getUpperLabelValue(Label l, String name) {
//		return this.upperLabel.getValue(l, name);
//	}
//
//	/**
//	 * @param l the scenario label
//	 * @param upperL the Upper Label
//	 * @return the value associated to the upper label of the occurrence of node n if it exists or the minimal values
//	 *         among the labels
//	 *         subsumed by l if one exists, null otherwise.
//	 */
//	public Integer getMinValueConsistentWith(Label l, String upperL) {
//		return this.upperLabel.getMinValueConsistentWith(l, upperL);
//	}
//
//	/**
//	 * @param label label
//	 * @return the value associated to label it it exists, null otherwise.
//	 */
//	public Integer getValue(Label label) {
//		return this.labeledValue.getValue(label);
//	}
//
//	/**
//	 * @param l
//	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
//	 */
//	public Integer getMinValueConsistentWith(Label l) {
//		return this.labeledValue.getMinValueConsistentWith(l);
//	}
//
//	/**
//	 * @return true if the edge represent a contingent edge.
//	 */
//	public boolean isContingentEdge() {
//		return (getType() == LabeledIntExtendedEdge.Type.contingent);
//	}
//
//	/**
//	 * @return true if the edge represent a normal edge or a derived one or a constraint one.
//	 */
//	public boolean isRequirementEdge() {
//		return ((getType() == LabeledIntExtendedEdge.Type.normal) || (getType() == LabeledIntExtendedEdge.Type.derived) || (getType() == LabeledIntExtendedEdge.Type.constraint));
//	}
//
//	/**
//	 * @return the labeled values as a set
//	 */
//	public Set<Entry<Label, Integer>> labeledValueSet() {
//		return this.labeledValue.entrySet();
//	}
//
//	/**
//	 * @return the number of Lower Case Labels of the edge.
//	 */
//	public int lowerLabelSize() {
//		return this.lowerLabel.size();
//	}
//
//	/**
//	 * @return the representation of all Lower Case Labels as a string.
//	 */
//	public String lowerLabelsToString() {
//		return this.lowerLabel.toString(true);
//	}
//
//	/**
//	 * Merges the labeled value i to the set of labeled values of this edge.
//	 * 
//	 * @param l
//	 * @param i
//	 * @return true if the operation was successful, false otherwise.
//	 * @see LabeledValueTreeMap#mergeLabeledValue(Label, Integer)
//	 */
//	@SuppressWarnings("javadoc")
//	public boolean mergeLabeledValue(Label l, Integer i) {
//		// if (l==null || i == null) return false;// I comment to speed up the operation.
//		final Integer oldValue = this.removedLabeledValue.getValue(l);
//		if ((oldValue != null) && (i >= oldValue)) {
//			// the labeled value (l,i) was already removed by label modification rule.
//			// A labeled value with a value equal or smaller will be modified again.
//			LabeledIntExtendedEdge.LOG.finest("The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", " + oldValue
//					+ ") is in the removed list");
//			return false;
//		}
//		return this.labeledValue.mergeLabeledValue(l, i);
//	}
//
//	/**
//	 * Wrapper method for {@link #mergeLabeledValue(Label, Integer)}.
//	 * 
//	 * @param l
//	 * @param i
//	 * @return true if the operation was successful, false otherwise.
//	 * @see #mergeLabeledValue(Label, Integer)
//	 */
//	@SuppressWarnings("javadoc")
//	public boolean mergeLabeledValue(String ls, Integer i) {
//		// if (l==null || i == null) return false;// I comment to speed up the operation.
//		return mergeLabeledValue(Label.parse(ls), i);
//	}
//
//	/**
//	 * @see LabeledValueTreeMap#mergeLabeledValue(LabeledValueTreeMap)
//	 */
//	@SuppressWarnings("javadoc")
//	public void mergeLabeledValue(LabeledValueTreeMap<Integer> map) {
//		this.labeledValue.mergeLabeledValue(map);
//	}
//
//	/**
//	 * Set a lower label constraint with delay i for the node n with label l.<br>
//	 * If a lower label with label l for node n is already present, it is overwritten.
//	 * 
//	 * @param l It cannot be null or empty.
//	 * @param n the node. It cannot be null.
//	 * @param i It cannot be null.
//	 * @return true if the merge has been successful.
//	 */
//	public boolean mergeLowerLabelValue(Label l, LabeledNode n, Integer i) {
//		if ((l == null) || (i == null))
//			throw new IllegalArgumentException("The label or the value has a not admitted value");
//		return this.lowerLabel.mergeTriple(l, n.getName(), i);
//	}
//
//	/**
//	 * Set a lower label constraint with delay i for the node n with label l.<br>
//	 * If a lower label with label l for node n is already present, it is overwritten.
//	 * 
//	 * @param l It cannot be null or empty.
//	 * @param nodeName the node. It cannot be null.
//	 * @param i It cannot be null.
//	 * @return true if the merge has been successful.
//	 */
//	public boolean mergeLowerLabelValue(Label l, String nodeName, Integer i) {
//		if ((l == null) || (i == null))
//			throw new IllegalArgumentException("The label or the value has a not admitted value. Label=" + l + ", value=" + i);
//		return this.lowerLabel.mergeTriple(l, nodeName, i);
//	}
//
//	/**
//	 * Set a upper label constraint with delay i for the node n with label l.<br>
//	 * If a upper label with label l for node n is already present, it is overwritten.
//	 * 
//	 * @param l It cannot be null or empty.
//	 * @param n the node. It cannot be null.
//	 * @param i It cannot be null.
//	 * @return true if the merge has been successful.
//	 */
//	public boolean mergeUpperLabelValue(Label l, LabeledNode n, Integer i) {
//		return this.mergeUpperLabelValue(l, n.getName(), i);
//	}
//
//	/**
//	 * Set a upper label constraint with delay i for the node name n with label l.<br>
//	 * If a upper label with label l for node n is already present, it is overwritten.
//	 * 
//	 * @param l It cannot be null or empty.
//	 * @param n the node name. It cannot be null.
//	 * @param i It cannot be null.
//	 * @return true if the merge has been successful.
//	 */
//	public boolean mergeUpperLabelValue(Label l, String n, Integer i) {
//		// if (l == null || i == null)//I comment out to speed up the operation!
//		// throw new IllegalArgumentException("The label or the value has a not admitted value");
//		final Integer oldValue = this.removedUpperLabel.getValue(l, n);
//		if ((oldValue != null) && (i >= oldValue)) {
//			// the labeled value (l,i) was already removed by label modification rule.
//			// A labeled value with a value equal or smaller will be modified again.
//			LabeledIntExtendedEdge.LOG.finest("The labeled value (" + l + ", " + n + ", " + i
//					+ ") has not been stored because the previous ("
//					+ l + ", " + n + ", " + oldValue + ") is in the removed list");
//			return false;
//		}
//		Integer value = getValue(l);
//		if (value != null && value <= i) {
//			LabeledIntExtendedEdge.LOG.finest("The labeled value (" + l + ", " + n + ", " + i
//					+ ") has not been stored because the constraint contains ("
//					+ l + ", " + value + ").");
//			return false;
//		}
//		return this.upperLabel.mergeTriple(l, n, i);
//	}
//
//	/**
//	 * @param l
//	 * @param i
//	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also
//	 *         indicate that the
//	 *         map previously associated null with key.)
//	 */
//	public Integer putLabeledValue(Label l, Integer i) {
//		return this.labeledValue.putLabeledValue(l, i);
//	}
//
//	/**
//	 * Put the labeled value (l, i) to the list of removed labeled values in order to avoid possible future put/merge of
//	 * the same pair.
//	 * 
//	 * @param l
//	 * @param i
//	 * @return the previous value if there exists one, null otherwise.
//	 */
//	public Integer putLabeledValueToRemovedList(Label l, Integer i) {
//		return this.removedLabeledValue.putLabeledValue(l, i);
//	}
//
//	/**
//	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
//	 * of the same pair.
//	 * 
//	 * @param l
//	 * @param n
//	 * @param i
//	 * @return true the triple is insert, false otherwise.
//	 */
//	public boolean putUpperLabeledValueToRemovedList(Label l, LabeledNode n, Integer i) {
//		return this.removedUpperLabel.mergeTriple(l, n.getName(), i);
//	}
//
//	/**
//	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
//	 * of the same pair.
//	 * 
//	 * @param l
//	 * @param n node name
//	 * @param i
//	 * @return true the triple is insert, false otherwise.
//	 */
//	public boolean putUpperLabeledValueToRemovedList(Label l, String n, Integer i) {
//		return this.removedUpperLabel.mergeTriple(l, n, i);
//	}
//
//	/**
//	 * Remove the label l from the map. If the label is not present, it does nothing.
//	 * 
//	 * @param l
//	 * @return the old value if it exists, null otherwise.
//	 */
//	public Integer removeLabel(Label l) {
//		LOG.finer("Removing label '" + l + "' from the edge " + this.toString());
//		return this.labeledValue.remove(l);
//	}
//
//	/**
//	 * Clear (remove) all labeled values associated to this edge.
//	 */
//	public void clear() {
//		this.labeledValue.clear();
//	}
//
//	/**
//	 * Remove the lower label for node n with label l.
//	 * 
//	 * @param l
//	 * @param n
//	 */
//	public void removeLowerLabel(Label l, LabeledNode n) {
//		if ((l == null) || (n == null)) return;
//		this.lowerLabel.remove(l, n.getName());
//	}
//
//	/**
//	 * Remove the upper label for node n with label l.
//	 * 
//	 * @param l
//	 * @param n
//	 */
//	public void removeUpperLabel(Label l, LabeledNode n) {
//		if ((l == null) || (n == null)) return;
//		this.upperLabel.remove(l, n.getName());
//	}
//
//	/**
//	 * Remove the upper label for node name n with label l.
//	 * 
//	 * @param l
//	 * @param n
//	 */
//	public void removeUpperLabel(Label l, String n) {
//		if ((l == null) || (n == null)) return;
//		this.upperLabel.remove(l, n);
//	}
//
//	/**
//	 * @param initialValue the initialValue to set
//	 */
//	public void setInitialValue(Integer initialValue) {
//		this.initialValue = initialValue;
//	}
//
//	/**
//	 * @param labeledValue the labeledValue to set
//	 */
//	public void setLabeledValue(LabeledValueTreeMap<Integer> labeledValue) {
//		this.labeledValue = (labeledValue == null) ? new LabeledValueTreeMap<Integer>(optimize) : labeledValue;
//	}
//
//	/**
//	 * @param labeledValue the lower case labeled value map to use for initializing the set
//	 */
//	public void setLabeledLowerCaseValue(LabeledContingentIntTreeMap<Integer> labeledValue) {
//		this.lowerLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap<Integer>() : labeledValue;
//	}
//
//	/**
//	 * @param labeledValue the upper case labeled value map to use for initializing the set
//	 */
//	public void setLabeledUpperCaseValue(LabeledContingentIntTreeMap<Integer> labeledValue) {
//		this.upperLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap<Integer>() : labeledValue;
//	}
//
//	/**
//	 * @param type the type to set
//	 */
//	public void setType(Type type) {
//		this.type = type;
//	}
//
//	/**
//	 * @return the number of labeled values associated to this edge.
//	 */
//	public int size() {
//		return this.labeledValue.size();
//	}
//
//	/**
//	 * Return a string representation of labeled values.
//	 */
//	@Override
//	public String toString() {
//		return "❮" + (getName().length() == 0 ? "<empty>" : getName()) + "; " + getType() + "; "
//				+ ((this.getInitialValue() != null) ? this.labeledValue.toString() + "; " : "")
//				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
//				+ ((this.upperLabel.size() > 0) ? "UL: " + this.upperLabel.toString() + "; " : "")
//				+ ((this.lowerLabel.size() > 0) ? "LL: " + this.lowerLabel.toString() + ";" : "")
//				+ "❯";
//	}
//
//	/**
//	 * @return the number of Upper Case Labels of the edge.
//	 */
//	public int upperLabelSize() {
//		return this.upperLabel.size();
//	}
//
//	/**
//	 * @return the representation of all Upper Case Labels of the edge.
//	 */
//	public String upperLabelsToString() {
//		return this.upperLabel.toString();
//	}
//
//}
