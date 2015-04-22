/**
 *
 */
package it.univr.di.cstnu.graph;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetMap;
import it.univr.di.labeledvalue.LabeledIntNodeSetTreeMap;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedInfo;

/**
 * It contains all information of a CSTPU edge.
 *
 * @author posenato
 */
public class LabeledIntEdge extends Component {
	/**
	 * Possible types
	 *
	 * @author posenato
	 */
	public static enum Type {
		/**
		 * The edge represents a generic constraint. In the CNSTU is equivalent to normal edge, but it is graphically
		 * represented in a
		 * different way.
		 */
		constraint,

		/** The edge represents a contingent (not shrinkable) constraint. */
		contingent,

		/**
		 * The edge represents a constraint derived by the controllability check algorithm.
		 */
		derived,

		/**
		 * The edge represents an internal one used to represent high level construct as a WORKFLOW OR JOIN.
		 */
		internal,

		/**
		 * The edge represents an execution precedence between two nodes one.
		 */
		normal
	}

	/**
	 * A transformer to return a label for the edge
	 */
	public static final ToStringLabeller<LabeledIntEdge> edgeLabelTransformer = new ToStringLabeller<LabeledIntEdge>() {
		/**
		 * Returns a label for the edge
		 */
		@Override
		public String transform(final LabeledIntEdge e) {
			final StringBuffer sb = new StringBuffer();
			sb.append((e.getName().length() == 0 ? "''" : e.getName()));
			sb.append("; ");
			if (e.getLabeledValueMap().size() > 0) {
				sb.append(e.getLabeledValueMap().toString());
			}
			if (e.getUpperLabelSet().size() > 0) {
				sb.append("; UL: ");
				sb.append(e.upperLabelsToString());
			}
			if (e.getLowerLabelSet().size() > 0) {
				sb.append("; LL:");
				sb.append(e.lowerLabelsToString());
			}

			return sb.toString();
		}
	};
	
	/**
	 * A transformer to return a font for edge label.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Transformer<LabeledIntEdge,Font> edgeFontTransformer = new ConstantTransformer(new Font("Helvetica", Font.PLAIN, 14));
	

	/**
	 * Select how to draw an edge given its type.
	 */
	public final static Transformer<LabeledIntEdge, Stroke> edgeStrokeTransformer = new Transformer<LabeledIntEdge, Stroke>() {
		@Override
		public Stroke transform(final LabeledIntEdge s) {
			switch (s.getType()) {
				case normal:
					return LabeledIntEdge.normalEdgeStroke;
				case contingent:
					return LabeledIntEdge.contingentEdgeStroke;
				case constraint:
					return LabeledIntEdge.constraintEdgeStroke;
				default:
					return LabeledIntEdge.derivedEdgeStroke;
			}
		}
	};

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke constraintEdgeStroke = RenderContext.DASHED;

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke contingentEdgeStroke = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	// GRAPHICS & VISUALIZATION STUFF
	// Set up a new stroke Transformer for the edges
	/**
	 * Simple stroke object to draw a 'derived' type edge.
	 */
	static final Stroke derivedEdgeStroke = RenderContext.DOTTED;
	// new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static int idSeq = 0;

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(LabeledIntEdge.class.getName());

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke normalEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Returns a transformer to select the color that is used to draw the edge. This transformer uses the type and the
	 * state of the edge to
	 * select the color.
	 *
	 * @param pi
	 * @param pickedPaint
	 * @param normalPaint
	 * @param contingentPaint
	 * @param derivedPaint
	 * @return a transformer object to draw an edge with a different color when it is picked.
	 */
	public static final Transformer<LabeledIntEdge, Paint> edgeDrawPaintTransformer(final PickedInfo<LabeledIntEdge> pi, final Paint pickedPaint,
			final Paint normalPaint,
			final Paint contingentPaint, final Paint derivedPaint) {

		final Paint[] paintMap = new Paint[] { normalPaint, contingentPaint, derivedPaint, derivedPaint, normalPaint };

		return new Transformer<LabeledIntEdge, Paint>() {
			@Override
			public Paint transform(final LabeledIntEdge e) {
				if (e == null) return normalPaint;
				// LabeledIntEdge.LOG.finer("LabeledIntEdge: " + e + ", picked: " + pi.isPicked(e));
				if (pi.isPicked(e)) return pickedPaint;
				return paintMap[e.getType().ordinal()];
			}
		};
	}

	/**
	 * @return a simple factory!
	 */
	public static Factory<LabeledIntEdge> getFactory() {
		return new Factory<LabeledIntEdge>() {
			@Override
			public LabeledIntEdge create() {
				return this.create(true);
			}

			public LabeledIntEdge create(final boolean optimized) {
				final LabeledIntEdge e = new LabeledIntEdge(optimized);
				return e;
			}
		};
	}

	/**
	 * An edge is usually draw as an arc between two points. The area delimited by the arc and the straight line
	 * connecting the two edge
	 * points can be filled by a color.
	 *
	 * @param normalPaint
	 * @param contingentPaint
	 * @param derivedPaint
	 * @return a transformer object to fill an edge 'area' with a color depending on edge type.
	 */
	static final Transformer<LabeledIntEdge, Paint> edgeFillPaintTransformer(final Paint normalPaint,
			final Paint contingentPaint, final Paint derivedPaint) {
		return new Transformer<LabeledIntEdge, Paint>() {
			/**
			 * logger
			 */
			final Paint[] paintMap = { normalPaint, contingentPaint, derivedPaint };

			@Override
			public Paint transform(final LabeledIntEdge e) {
				if (e == null) return normalPaint;
				return this.paintMap[e.getType().ordinal()];
			}
		};
	}

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 */
	final boolean optimize;

	// /**
	// * @param label
	// * @return a Transformer useful to select the minimal values associated to a label
	// */
	// public static Transformer<LabeledIntEdge, Integer> getTransformer(final Label label) {
	// return new Transformer<LabeledIntEdge, Integer>() {
	// /**
	// * logger
	// */
	// Logger LOG1 = Logger.getLogger(Transformer.class.getName());
	//
	// @Override
	// public Integer transform(LabeledIntEdge link) {
	// if ((label == null) || (link == null)) {
	// this.LOG1.finer("At least, one is null. Label: " + label + ". Link: " + link);
	// return null;
	// }
	// this.LOG1.finer("Label " + label.toString() + " tranform(" + link.toString() + "):" + link.getValue(label));
	// return link.getValue(label);
	// }
	// };
	// }

	// @SuppressWarnings({ "unused", "javadoc" })
	// public static void main(String[] args) {
	// final Literal a = new Literal('a'), b = new Literal('b'), c = new Literal('c', State.negated), d = new Literal('d');
	//
	// final Label l1 = new Label();
	// l1.conjunct(d);
	// l1.conjunct(b);
	// System.out.printf("Label l1 = %s\n", l1);
	//
	// final Label l2 = new Label(b);
	// l2.conjunct(d.getComplement());
	// System.out.printf("Label l2 = %s\n", l2);
	//
	// // LabeledIntEdge edge = new LabeledIntEdge(l1, 2), edge1 = new LabeledIntEdge("e2", Type.normal, l2, 3);
	//
	// // System.out.printf("LabeledIntEdge with no name and label l1: %s\n", edge);
	// // System.out.printf("LabeledIntEdge e2 with label l2: %s\n", edge1);
	// }

	/**
	 * Labeled value.
	 */
	private LabeledIntNodeSetMap labeledValue;

	/**
	 * Lower case Morris Labels
	 */
	private LabeledContingentIntTreeMap lowerLabel;

	/**
	 * The node set associated to a label. It contains internal nodes of any negative qPath (path formed considering ¿literals) from Z.
	 * This set is not null only for edges outgoing from Z.<br>
	 * With the adoption of LabeledIntNodeSetMap, such information is present in the labeledValue member.
	 * 
	 */
//	private Map<Label, SortedSet<String>> nodeSetOfLabel;

	/**
	 * Removed Labeled value.<br>
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order
	 * to avoid to add it a
	 * second time.
	 */
	private LabeledIntNodeSetMap removedLabeledValue;

	/**
	 * Removed Upper Case Labeled value.<br>
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order
	 * to avoid to add it a
	 * second time.
	 */
	private LabeledContingentIntTreeMap removedUpperLabel;

	/**
	 * The type of the edge.
	 */
	private Type type;

	/**
	 * Upper case Morris Labels
	 */
	private LabeledContingentIntTreeMap upperLabel;

	/**
	 * Default constructor: empty name, derived type and no labeled value.
	 *
	 * @param optimized true if the labeled values have to be simplified every time is possible.
	 */
	public LabeledIntEdge(final boolean optimized) {
		super("e" + LabeledIntEdge.idSeq++);// if you change this default, change also in GraphMLReader
		this.setType(Type.normal);
		this.optimize = optimized;
		this.labeledValue = new LabeledIntNodeSetTreeMap(this.optimize);
//		this.nodeSetOfLabel = new Object2ObjectAVLTreeMap<>();
		this.upperLabel = new LabeledContingentIntTreeMap(this.optimize);
		this.lowerLabel = new LabeledContingentIntTreeMap(this.optimize);
		this.removedLabeledValue = new LabeledIntNodeSetTreeMap(false);
		this.removedUpperLabel = new LabeledContingentIntTreeMap(false);
	}

	/**
	 * A simple constructor cloner.
	 *
	 * @param e edge to clone. If null, an empty edge is created with type = normal.
	 * @param forceOptimization true if one wants a copy of the edge but forcing the label optimization.
	 */
	public LabeledIntEdge(final LabeledIntEdge e, final boolean forceOptimization) {
		super(e);
		if (e == null) {
			this.setType(Type.normal);
			this.optimize = forceOptimization;
			this.labeledValue = new LabeledIntNodeSetTreeMap(this.optimize);
			this.upperLabel = new LabeledContingentIntTreeMap(this.optimize);
			this.lowerLabel = new LabeledContingentIntTreeMap(this.optimize);
			this.removedLabeledValue = new LabeledIntNodeSetTreeMap(false);
			this.removedUpperLabel = new LabeledContingentIntTreeMap(false);
		} else {
			this.type = e.type;
			this.optimize = forceOptimization;
			this.labeledValue = new LabeledIntNodeSetTreeMap(e.labeledValue, this.optimize);
			this.upperLabel = new LabeledContingentIntTreeMap(e.upperLabel, this.optimize);
			this.lowerLabel = new LabeledContingentIntTreeMap(e.lowerLabel, this.optimize);
			this.removedLabeledValue = new LabeledIntNodeSetTreeMap(e.removedLabeledValue, false);
			this.removedUpperLabel = new LabeledContingentIntTreeMap(e.removedUpperLabel, false);
		}
	}

	/**
	 * @param n
	 * @param optimized
	 */
	public LabeledIntEdge(final String n, final boolean optimized) {
		super(n);
		this.setType(Type.normal);
		this.optimize = optimized;
		this.labeledValue = new LabeledIntNodeSetTreeMap(this.optimize);
//		this.nodeSetOfLabel = new Object2ObjectAVLTreeMap<>();
		this.upperLabel = new LabeledContingentIntTreeMap(this.optimize);
		this.lowerLabel = new LabeledContingentIntTreeMap(this.optimize);
		this.removedLabeledValue = new LabeledIntNodeSetTreeMap(false);
		this.removedUpperLabel = new LabeledContingentIntTreeMap(false);
	}

	/**
	 * Default constructor: empty name, derived type and no labeled value.
	 *
	 * @param name
	 * @param t the type of the edge
	 * @param optimized
	 */
	public LabeledIntEdge(final String name, final Type t, final boolean optimized) {
		this(name, optimized);
		this.setType(t);
	}

	/**
	 * Default constructor
	 *
	 * @param name
	 * @param t
	 * @param l
	 * @param i
	 * @param optimized
	 */
	public LabeledIntEdge(final String name, final Type t, final Label l, final int i, final boolean optimized) {
		this(name, t, optimized);
		this.labeledValue.putForcibly(l, i);
	}

//	/**
//	 * Add node name set to the set of node associated to label 'label'.
//	 * 
//	 * @param label label
//	 * @param nodeNameSet
//	 */
//	public void addNodeSet(final Label label, Set<String> nodeNameSet) {
//		if (label == null || nodeNameSet == null || nodeNameSet.isEmpty()) return;
//		ValueNodeSetPair vnsp = labeledValue.get(label);
//		if (vnsp == null) return;
//		vnsp.add(nodeNameSet);
//	}

	/**
	 * Clear (remove) all labeled values associated to this edge.
	 */
	public void clear() {
		this.labeledValue.clear();
		this.upperLabel.clear();
//		this.nodeSetOfLabel.clear();
		this.lowerLabel.clear();
		this.removedLabeledValue.clear();
		this.removedUpperLabel.clear();
	}

	/**
	 * Clears all ordinary labeled values.
	 */
	public void clearLabels() {
		this.labeledValue.clear();
	}

	/**
	 * Clears all lower case labeled values.
	 */
	public void clearLowerLabels() {
		this.lowerLabel.clear();
	}

	/**
	 * Clears all upper case labeled values.
	 */
	public void clearUpperLabels() {
		this.upperLabel.clear();
	}

	/**
	 * A different kind of equals. It allows one to compare two edges with respect to their labeled values.
	 *
	 * @param e a not null edge
	 * @return true if edge e contains labeled values equal to the current edge ones.
	 */
	public boolean equalsLabeledValues(final LabeledIntEdge e) {
		if (e == null) return false;
		if (e == this) return true;

		return (this.getLabeledValueMap().equals(e.getLabeledValueMap())
				&& this.getLowerLabelSet().equals(e.getLowerLabelSet())
				&& this.getUpperLabelSet().equals(e.getUpperLabelSet()));
	}

	/**
	 * @return the set of all ordinary labeled values and upper case ones.
	 */
	public Set<Object2IntMap.Entry<Entry<Label, String>>> getAllUpperCaseAndOrdinaryLabeledValuesSet() {
		// Merge all possible labeled values and Upper Case labeled values of edges between Y and X in a single set.
		final Set<Object2IntMap.Entry<Entry<Label, String>>> globalLabeledValueSet = new ObjectArraySet<>(this.getUpperLabelSet());
		for (final Object2IntMap.Entry<Label> entry : this.labeledValueSet()) {
			final Entry<Label, String> e = new AbstractObject2ObjectMap.BasicEntry<>(entry.getKey(), Constants.EMPTY_UPPER_CASE_LABELstring);
			globalLabeledValueSet.add(new AbstractObject2IntMap.BasicEntry<>(e, entry.getIntValue()));
		}
		return globalLabeledValueSet;
	}

	/**
	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
	 */
	public LabeledIntNodeSetMap getLabeledValueMap() {
		return this.labeledValue;
	}

	/**
	 * @return the Upper-Case labeled Value
	 */
	public LabeledContingentIntTreeMap getLowerLabelMap() {
		return this.lowerLabel;
	}

	/**
	 * @return the map of Lower Case Labels of the edge.
	 */
	public Set<Object2IntMap.Entry<Entry<Label, String>>> getLowerLabelSet() {
		return this.lowerLabel.labeledTripleSet();
	}

	/**
	 * Returns the value associated to the lower label of the occurrence of node n if it exists, null otherwise;
	 *
	 * @param l
	 * @param n
	 * @return the value associated to the labeled wait if it exists.
	 */
	public int getLowerLabelValue(final Label l, final LabeledNode n) {
		return this.lowerLabel.getValue(l, n.getName().toLowerCase());
	}

	/**
	 * @return the minimal value among all Lower Case Label if there are some values, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getMinLowerLabeledValue() {
		return this.lowerLabel.getMinValue();
	}

	/**
	 * @return the minimal value among all Upper Case Label if there are some values, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getMinUpperLabeledValue() {
		return this.upperLabel.getMinValue();
	}

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getMinValue() {
		return this.labeledValue.getMinValue();
	}

	/**
	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some;
	 *         {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getMinValueAmongLabelsWOUnknown() {
		return this.labeledValue.getMinValueAmongLabelsWOUnknown();
	}

	/**
	 * @param l
	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l) {
		return this.labeledValue.getMinValueConsistentWith(l);
	}

	/**
	 * @param l the scenario label
	 * @param upperL the Upper Label
	 * @return the value associated to the upper label of the occurrence of node n if it exists or the minimal values
	 *         among the labels
	 *         subsumed by l if one exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l, final String upperL) {
		return this.upperLabel.getMinValueConsistentWith(l, upperL);
	}

	/**
	 * @param label label
	 * @return the node set associated to label it it exists, null otherwise.
	 */
	public SortedSet<String> getNodeSet(final Label label) {
		return this.labeledValue.getNodeSet(label);
	}

	/**
	 * @return the labeled values that cannot be further added to the labeled value set of this edge
	 */
	public LabeledIntNodeSetMap getRemovedLabeledValuesMap() {
		return this.removedLabeledValue;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * @return the Upper-Case labeled Value
	 */
	public LabeledContingentIntTreeMap getUpperLabelMap() {
		return this.upperLabel;
	}

	/**
	 * @return he map of Upper Case Labels of the edge.
	 */
	public Set<Object2IntMap.Entry<Entry<Label, String>>> getUpperLabelSet() {
		return this.upperLabel.labeledTripleSet();
	}

	/**
	 * @param l
	 * @param n
	 * @return the value associated to the upper label of the occurrence of node n if it exists, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getUpperLabelValue(final Label l, final LabeledNode n) {
		return this.upperLabel.getValue(l, n.getName().toUpperCase());
	}

	/**
	 * @param l
	 * @param name
	 * @return the value associated to the upper label of the occurrence of node n if it exists, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getUpperLabelValue(final Label l, final String name) {
		return this.upperLabel.getValue(l, name.toUpperCase());
	}

	/**
	 * @param label label
	 * @return the value associated to label it it exists, {@link LabeledIntNodeSetMap#INT_NULL} otherwise.
	 */
	public int getValue(final Label label) {
		return this.labeledValue.getValue(label);
	}

	/**
	 * @return true if the edge represent a contingent edge.
	 */
	public boolean isContingentEdge() {
		return (this.getType() == LabeledIntEdge.Type.contingent);
	}

	/**
	 * @return the optimize
	 */
	public boolean isOptimized() {
		return this.optimize;
	}

	/**
	 * @return true if the edge represent a normal edge or a derived one or a constraint one.
	 */
	public boolean isRequirementEdge() {
		return ((this.getType() == LabeledIntEdge.Type.normal) || (this.getType() == LabeledIntEdge.Type.derived) || (this.getType() == LabeledIntEdge.Type.constraint));
	}

	/**
	 * @return the labeled values as a set
	 */
	public Set<Object2IntMap.Entry<Label>> labeledValueSet() {
		return this.labeledValue.entrySet();
	}

	/**
	 * @return the number of Lower Case Labels of the edge.
	 */
	public int lowerLabelSize() {
		return this.lowerLabel.size();
	}

	/**
	 * @return the representation of all Lower Case Labels as a string.
	 */
	public String lowerLabelsToString() {
		return this.lowerLabel.toString(true);
	}

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l
	 * @param i
	 * @param s the node set to add.
	 * @return true if the operation was successful, false otherwise.
	 * @see LabeledValueTreeMap#put(Label, int)
	 */
	@SuppressWarnings("javadoc")
	public boolean mergeLabeledValue(final Label l, final int i, Set<String> s) {
		if ((l == null) || (i == LabeledIntNodeSetMap.INT_NULL)) return false;
		final int oldValue = this.removedLabeledValue.getValue(l);
		if ((oldValue != LabeledIntNodeSetMap.INT_NULL) && (i >= oldValue)) {
			// The new value is greater or equal the old one, the new value can be ignored.
			// the labeled value (l,i) was already removed by label modification rule. So, it will be not stored.
			if (LOG.isLoggable(Level.FINEST))
				LabeledIntEdge.LOG.log(Level.FINEST, "The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", "
						+ oldValue + ") is in the removed list");
			return false;
		}
		boolean exit = this.labeledValue.put(l, i, s);
		return exit;
	}

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l
	 * @param i
	 * @return true if the operation was successful, false otherwise.
	 * @see LabeledValueTreeMap#put(Label, int)
	 */
	@SuppressWarnings("javadoc")
	public boolean mergeLabeledValue(final Label l, final int i) {
		if ((l == null) || (i == LabeledIntNodeSetMap.INT_NULL)) return false;
		return mergeLabeledValue(l, i, null);
	}

	/**
	 */
	@SuppressWarnings("javadoc")
	public void mergeLabeledValue(final LabeledIntNodeSetMap map) {
		for (Object2IntMap.Entry<Label> entry : map.entrySet())
			this.labeledValue.put(entry.getKey(), entry.getIntValue());
	}

	/**
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 *
	 * @param l
	 * @param i
	 * @return true if the operation was successful, false otherwise.
	 * @see #mergeLabeledValue(Label, int)
	 */
	@SuppressWarnings("javadoc")
	public boolean mergeLabeledValue(final String ls, final int i) {
		return this.mergeLabeledValue(Label.parse(ls), i);
	}

	/**
	 * Set a lower label constraint with delay i for the node n with label l.<br>
	 * If a lower label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param n the node. It cannot be null.
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeLowerLabelValue(final Label l, final LabeledNode n, final int i) {
		return this.lowerLabel.mergeTriple(l, n.getName().toLowerCase(), i, false);
	}

	/**
	 * Set a lower label constraint with delay i for the node n with label l.<br>
	 * If a lower label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node. It cannot be null.
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeLowerLabelValue(final Label l, final String nodeName, final int i) {
		return this.lowerLabel.mergeTriple(l, nodeName.toLowerCase(), i, false);
	}

	/**
	 * Set a upper label constraint with delay i for the node n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param n the node. It cannot be null.
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperLabelValue(final Label l, final LabeledNode n, final int i) {
		return this.mergeUpperLabelValue(l, n.getName().toUpperCase(), i);
	}

	/**
	 * Set a upper label constraint with delay i for the node name n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null.
	 * @param i It cannot be LabeledIntNodeSetMap.nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperLabelValue(final Label l, String nodeName, final int i) {
		nodeName = nodeName.toUpperCase();
		if ((l == null) || (nodeName == null) || (i == LabeledIntNodeSetMap.INT_NULL))
			throw new IllegalArgumentException("The label or the value has a not admitted value");
		final int oldValue = this.removedUpperLabel.getValue(l, nodeName);
		if ((oldValue != LabeledIntNodeSetMap.INT_NULL) && (i >= oldValue)) {
			// the labeled value (l,i) was already removed by label modification rule.
			// A labeled value with a value equal or smaller will be modified again.
			LabeledIntEdge.LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the previous ("
					+ l + ", " + nodeName + ", " + oldValue + ") is in the removed list");
			return false;
		}
		final int value = this.getValue(l);
		if ((value != LabeledIntNodeSetMap.INT_NULL) && (value <= i)) {
			LabeledIntEdge.LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the constraint contains ("
					+ l + ", " + value + ").");
			return false;
		}
		return this.upperLabel.mergeTriple(l, nodeName, i, false);
	}

	/**
	 * @param l
	 * @param i
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also
	 *         indicate that the
	 *         map previously associated null with key.)
	 */
	public int putLabeledValue(final Label l, final int i) {
		return this.labeledValue.putForcibly(l, i);
	}

	/**
	 * Put the labeled value (l, i) to the list of removed labeled values in order to avoid possible future put/merge of
	 * the same pair.
	 *
	 * @param l
	 * @param i
	 * @return the previous value if there exists one, null otherwise.
	 */
	public int putLabeledValueToRemovedList(final Label l, final int i) {
		return this.removedLabeledValue.putForcibly(l, i);
	}

	/**
	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
	 * of the same pair.
	 *
	 * @param l
	 * @param n
	 * @param i
	 * @return true the triple is insert, false otherwise.
	 */
	public boolean putUpperLabeledValueToRemovedList(final Label l, final LabeledNode n, final int i) {
		return this.removedUpperLabel.mergeTriple(l, n.getName(), i, true);
	}

	/**
	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
	 * of the same pair.
	 *
	 * @param l
	 * @param n node name
	 * @param i
	 * @return true the triple is insert, false otherwise.
	 */
	public boolean putUpperLabeledValueToRemovedList(final Label l, final String n, final int i) {
		return this.removedUpperLabel.mergeTriple(l, n, i, true);
	}

	/**
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 *
	 * @param l
	 * @return the old value if it exists, null otherwise.
	 */
	public int removeLabel(final Label l) {
		LabeledIntEdge.LOG.finer("Removing label '" + l + "' from the edge " + this.toString());
		return this.labeledValue.remove(l);
	}

	/**
	 * Remove the lower label for node n with label l.
	 *
	 * @param l
	 * @param n
	 * @return the value of the removed labeled value
	 */
	public int removeLowerLabel(final Label l, final LabeledNode n) {
		return this.lowerLabel.remove(l, n.getName().toLowerCase());
	}

	/**
	 * Remove the upper label for node n with label l.
	 *
	 * @param l
	 * @param n
	 * @return the value of the removed element.
	 */
	public int removeUpperLabel(final Label l, final LabeledNode n) {
		return this.upperLabel.remove(l, n.getName().toUpperCase());
	}

	/**
	 * Remove the upper label for node name n with label l.
	 *
	 * @param l
	 * @param n
	 * @return the old value
	 */
	public int removeUpperLabel(final Label l, final String n) {
		return this.upperLabel.remove(l, n.toUpperCase());
	}

	/**
	 * @param labeledValue the lower case labeled value map to use for initializing the set
	 */
	public void setLabeledLowerCaseValue(final LabeledContingentIntTreeMap labeledValue) {
		this.lowerLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap(this.optimize) : labeledValue;
	}

	/**
	 * @param labeledValue the upper case labeled value map to use for initializing the set
	 */
	public void setLabeledUpperCaseValue(final LabeledContingentIntTreeMap labeledValue) {
		this.upperLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap(this.optimize) : labeledValue;
	}

	/**
	 * @param labeledValue the labeledValue to set
	 */
	public void setLabeledValue(final LabeledIntNodeSetMap labeledValue) {
		LabeledIntNodeSetMap map = new LabeledIntNodeSetTreeMap(this.optimize);
		for (Object2IntMap.Entry<Label> entry : labeledValue.entrySet()) {
			map.put(entry.getKey(), entry.getIntValue());
		}
		this.labeledValue = map;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(final Type type) {
		this.type = type;
	}

	/**
	 * @return the number of labeled values associated to this edge.
	 */
	public int size() {
		return this.labeledValue.size();
	}

	/**
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString() {
		return "❮" + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getType() + "; "
				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
				+ ((this.upperLabel.size() > 0) ? "UL: " + this.upperLabel.toString() + "; " : "")
				+ ((this.lowerLabel.size() > 0) ? "LL: " + this.lowerLabel.toString(true) + ";" : "")
				// + ((this.nodeSetOfLabel.size() > 0) ? "NodeSet: " + this.nodeSetOfLabel.toString() + ";" : "")
				+ "❯";
	}

	/**
	 * @return the number of Upper Case Labels of the edge.
	 */
	public int upperLabelSize() {
		return this.upperLabel.size();
	}

	/**
	 * @return the representation of all Upper Case Labels of the edge.
	 */
	public String upperLabelsToString() {
		return this.upperLabel.toString();
	}
}
