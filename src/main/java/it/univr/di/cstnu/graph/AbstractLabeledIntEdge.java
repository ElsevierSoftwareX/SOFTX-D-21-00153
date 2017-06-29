/**
 *
 */
package it.univr.di.cstnu.graph;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * It contains all information of a CSTPU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public abstract class AbstractLabeledIntEdge extends AbstractComponent implements LabeledIntEdge {

	/**
	 * Represents a pair (Label, String).
	 * 
	 * @author posenato
	 */
	static class InternalEntry implements Object2ObjectMap.Entry<Label, ALabel>, Comparable<Object2ObjectMap.Entry<Label, ALabel>> {

		@SuppressWarnings("javadoc")
		ALabel aLabel;
		@SuppressWarnings("javadoc")
		Label label;

		/**
		 * @param label
		 * @param aLabel
		 */
		public InternalEntry(Label label, ALabel aLabel) {
			this.label = label;
			this.aLabel = aLabel;
		}

		@Override
		public int compareTo(Object2ObjectMap.Entry<Label, ALabel> o) {
			if (o == null)
				return 1;
			int i = this.label.compareTo(o.getKey());
			if (i != 0)
				return i;
			return this.aLabel.compareTo(o.getValue());
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof InternalEntry))
				return false;
			InternalEntry e = (InternalEntry) o;
			return this.label.equals(e.label) && this.aLabel.equals(e.aLabel);
		}

		@Override
		public Label getKey() {
			return this.label;
		}

		@Override
		public ALabel getValue() {
			return this.aLabel;
		}

		@Override
		public int hashCode() {
			return this.label.hashCode() + 1000 * this.aLabel.hashCode();
		}

		@Override
		public ALabel setValue(ALabel value) {
			ALabel old = new ALabel(this.aLabel);
			this.aLabel = value;
			return old;
		}

		@Override
		public String toString() {
			return "(" + this.aLabel + ", " + this.label + ")";
		}

	}

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
	 * Select how to draw an edge given its type.
	 */
	public final static Function<LabeledIntEdge, Stroke> edgeStrokeTransformer = new Function<LabeledIntEdge, Stroke>() {
		@Override
		public Stroke apply(final LabeledIntEdge s) {
			switch (s.getConstraintType()) {
			case normal:
				return AbstractLabeledIntEdge.normalEdgeStroke;
			case contingent:
				return AbstractLabeledIntEdge.contingentEdgeStroke;
			case constraint:
				return AbstractLabeledIntEdge.constraintEdgeStroke;
			default:
				return AbstractLabeledIntEdge.derivedEdgeStroke;
			}
		}
	};

	/**
	 * Font for edge label rendering
	 */
	public final static Function<LabeledIntEdge, Font> edgeFontFunction = new Function<LabeledIntEdge, Font>() {
		/**
		 * 
		 */
		protected boolean bold = false;
		/**
		 * 
		 */
		Font f = new Font("Helvetica", Font.PLAIN, 14);
		/**
		 * 
		 */
		Font b = new Font("Helvetica", Font.BOLD, 14);

		/**
		 * @param bold
		 */
		@SuppressWarnings("unused")
		public void setBold(boolean bold) {
			this.bold = bold;
		}

		@Override
		public Font apply(LabeledIntEdge e) {
			if (this.bold)
				return this.b;
			return this.f;
		}
	};
	
	
	/**
	 * Font for edge label rendering
	 */
	public final static Function<LabeledIntEdge, String> edgeLabelFunction = new Function<LabeledIntEdge, String>() {
		/**
		 * Returns a label for the edge
		 */
		public String apply(final LabeledIntEdge e) {
			final StringBuffer sb = new StringBuffer();
			sb.append((e.getName().length() == 0 ? "''" : e.getName()));
			sb.append("; ");
			if (e.getLabeledValueMap().size() > 0) {
				sb.append(e.getLabeledValueMap().toString());
			}
			if (e.getUpperLabelSet().size() > 0) {
				sb.append("; UL: ");
				sb.append(e.upperLabelsAsString());
			}
			if (e.getLowerLabelSet().size() > 0) {
				sb.append("; LL:");
				sb.append(e.lowerLabelsAsString());
			}

			return sb.toString();
		}
	};

		
	
	/**
	 * To provide a unique id for the default creation of component.
	 */
	@SuppressWarnings("hiding")
	static int idSeq = 0;

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger(AbstractLabeledIntEdge.class.getName());

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke normalEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Returns a transformer to select the color that is used to draw the edge. This transformer uses the type and the state of the edge to select the color.
	 * 
	 * @param <K>
	 * @param pi a {@link PickedInfo} object.
	 * @param pickedPaint a {@link java.awt.Paint} object.
	 * @param normalPaint a {@link java.awt.Paint} object.
	 * @param contingentPaint a {@link java.awt.Paint} object.
	 * @param derivedPaint a {@link java.awt.Paint} object.
	 * @return a transformer object to draw an edge with a different color when it is picked.
	 */
	public static final <K extends LabeledIntEdge> Function<K, Paint> edgeDrawPaintTransformer(final PickedInfo<K> pi,
			final Paint pickedPaint,
			final Paint normalPaint,
			final Paint contingentPaint, final Paint derivedPaint) {

		final Paint[] paintMap = new Paint[] { normalPaint, contingentPaint, derivedPaint, derivedPaint, normalPaint };

		return new Function<K, Paint>() {
			@Override
			public Paint apply(final K e) {
				if (e == null)
					return normalPaint;
				// LabeledIntEdge.LOG.finer("LabeledIntEdge: " + e + ", picked: " + pi.isPicked(e));
				if (pi.isPicked(e))
					return pickedPaint;
				return paintMap[e.getConstraintType().ordinal()];
			}
		};
	}

	/**
	 * An edge is usually draw as an arc between two points. The area delimited by the arc and the straight line connecting the two edge points can be filled by
	 * a color.
	 *
	 * @param normalPaint
	 * @param contingentPaint
	 * @param derivedPaint
	 * @return a transformer object to fill an edge 'area' with a color depending on edge type.
	 */
	static final Function<LabeledIntEdge, Paint> edgeFillPaintTransformer(final Paint normalPaint,
			final Paint contingentPaint, final Paint derivedPaint) {
		return new Function<LabeledIntEdge, Paint>() {
			/**
			 * logger
			 */
			final Paint[] paintMap = { normalPaint, contingentPaint, derivedPaint };

			@Override
			public Paint apply(final LabeledIntEdge e) {
				if (e == null)
					return normalPaint;
				return this.paintMap[e.getConstraintType().ordinal()];
			}
		};
	}

	/**
	 * The type of the edge.
	 */
	ConstraintType constraintType;

	/**
	 * Lower case Morris Labels. The name of node HAS TO be preserved as the original. The name of this map says that has to be considered as lower-case letter.
	 */
	LabeledContingentIntTreeMap lowerLabel;

	/**
	 * Removed Labeled value.<br>
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order to avoid to add it a second time.
	 */
	Object2IntMap<Label> removedLabeledValue;

	/**
	 * Removed Upper Case Labeled value.<br>
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order to avoid to add it a second time.
	 */
	Object2IntMap<Entry<Label, ALabel>> removedUpperLabel;

	/**
	 * Upper case Morris Labels. The name of node HAS TO be preserved as the original. The name of this map says that has to be considered as upper-case letter.
	 */
	LabeledContingentIntTreeMap upperLabel;

	/**
	 * Minimal constructor. the name will be 'e&lt;id&gt;'.
	 */
	public AbstractLabeledIntEdge() {
		this((String) null);
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param e
	 *            the component to clone.
	 */
	AbstractLabeledIntEdge(final LabeledIntEdge e) {
		this((e != null) ? e.getName() : (String) null);
		if (e != null) {
			this.setConstraintType(e.getConstraintType());
			this.upperLabel = new LabeledContingentIntTreeMap(e.getUpperLabelMap());
			this.lowerLabel = new LabeledContingentIntTreeMap(e.getLowerLabelMap());
		}
	}

	/**
	 * Simplified constructor
	 *
	 * @param n
	 */
	AbstractLabeledIntEdge(final String n) {
		this.name = ((n == null) || (n.length() == 0)) ? "e" + idSeq++ : n;
		this.setConstraintType(ConstraintType.normal);
		this.upperLabel = new LabeledContingentIntTreeMap();
		this.lowerLabel = new LabeledContingentIntTreeMap();

		this.removedLabeledValue = new Object2IntArrayMap<>();
		this.removedLabeledValue.defaultReturnValue(Constants.INT_NULL);
		this.removedUpperLabel = new Object2IntArrayMap<>();
		this.removedUpperLabel.defaultReturnValue(Constants.INT_NULL);
	}

	@Override
	public void clear() {
		this.upperLabel.clear();
		this.lowerLabel.clear();
		this.removedLabeledValue.clear();
		this.removedUpperLabel.clear();
	}

	@Override
	public void clearLabels() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final void clearLowerLabels() {
		this.lowerLabel.clear();
	}

	@Override
	public final void clearUpperLabels() {
		this.upperLabel.clear();
	}

	@Override
	public LabeledIntEdge createLabeledIntEdge() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public LabeledIntEdge createLabeledIntEdge(LabeledIntEdge e) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final boolean equalsLabeledValues(final LabeledIntEdge e) {
		if (e == null || e == this)
			return false;

		// Use getLabeledValueMap instead of labeledValueSet() to have a better control.
		return (this.getLabeledValueMap().equals(e.getLabeledValueMap())
				&& this.getLowerLabelSet().equals(e.getLowerLabelSet())
				&& this.getUpperLabelSet().equals(e.getUpperLabelSet()));
	}

	@Override
	public final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> getAllUpperCaseAndOrdinaryLabeledValuesSet() {
		// Merge all possible labeled values and Upper Case labeled values of edges between Y and X in a single set.
		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> globalLabeledValueSet = new ObjectArraySet<>(this.getUpperLabelSet());
		for (final Object2IntMap.Entry<Label> entry : this.getLabeledValueSet()) {
			final Entry<Label, ALabel> e = new AbstractObject2ObjectMap.BasicEntry<>(entry.getKey(), ALabel.emptyLabel);
			globalLabeledValueSet.add(new AbstractObject2IntMap.BasicEntry<>(e, entry.getIntValue()));
		}
		return globalLabeledValueSet;
	}

	@Override
	public final ConstraintType getConstraintType() {
		return this.constraintType;
	}

	@Override
	public LabeledIntMap getLabeledValueMap() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final LabeledContingentIntTreeMap getLowerLabelMap() {
		return this.lowerLabel;
	}

	@Override
	public final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> getLowerLabelSet() {
		return this.lowerLabel.labeledTripleSet();
	}

	@Override
	public final int getLowerLabelValue(final Label l, final ALabel nodeName) {
		return this.lowerLabel.getValue(l, nodeName);
	}

	@Override
	public final int getMinLowerLabeledValue() {
		return this.lowerLabel.getMinValue();
	}

	@Override
	public final int getMinUpperLabeledValue() {
		return this.upperLabel.getMinValue();
	}

	@Override
	public int getMinValue() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public int getMinValueAmongLabelsWOUnknown() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public int getMinValueConsistentWith(Label l) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public int getMinValueConsistentWith(Label l, ALabel upperL) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final Object2IntMap<Label> getRemovedLabeledValuesMap() {
		return this.removedLabeledValue;
	}

	@Override
	public final LabeledContingentIntTreeMap getUpperLabelMap() {
		return this.upperLabel;
	}

	@Override
	public final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> getUpperLabelSet() {
		return this.upperLabel.labeledTripleSet();
	}

	@Override
	public final int getUpperLabelValue(final Label l, final ALabel name1) {
		return this.upperLabel.getValue(l, name1);
	}

	@Override
	public int getValue(Label label) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final boolean isContingentEdge() {
		return (this.getConstraintType() == LabeledIntEdge.ConstraintType.contingent);
	}

	@Override
	public final boolean isRequirementEdge() {
		return ((this.getConstraintType() == LabeledIntEdge.ConstraintType.normal) || (this.getConstraintType() == LabeledIntEdge.ConstraintType.derived)
				|| (this.getConstraintType() == LabeledIntEdge.ConstraintType.constraint));
	}

	@Override
	public final String lowerLabelsAsString() {
		return this.lowerLabel.toString(true);
	}

	@Override
	public final int lowerLabelSize() {
		return this.lowerLabel.size();
	}

	@Override
	public boolean mergeLabeledValue(Label l, int i) {
		throw new UnsupportedOperationException("Core class.");
	}

	// @Override
	// public boolean mergeLabeledValue(Label l, int i, ObjectSet<ALabel> s) {
	// throw new UnsupportedOperationException("Core class.");
	// }

	@Override
	public void mergeLabeledValue(LabeledIntMap map) {
		throw new UnsupportedOperationException("Core class.");
	}

	/**
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 *
	 * @param i
	 *            an integer.
	 * @return true if the operation was successful, false otherwise.
	 * @see #mergeLabeledValue(Label, int)
	 * @param ls
	 *            a {@link java.lang.String} object.
	 */
	@Override
	public boolean mergeLabeledValue(final String ls, final int i) {
		// just a wrapper!
		return this.mergeLabeledValue(Label.parse(ls), i);
	}

	@Override
	public final boolean mergeLowerLabelValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException("The label or the value has a not admitted value");
		final int value = this.getValue(l);
		if ((value != Constants.INT_NULL) && (value <= i)) {
			if (Debug.ON)
				LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the constraint contains ("
						+ l + ", " + value + ").");
			return false;
		}
		return this.lowerLabel.mergeTriple(l, nodeName, i, false);
	}

	@Override
	public final boolean mergeUpperLabelValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + nodeName + ", " + Constants.formatInt(i) + ").");
		InternalEntry se = new InternalEntry(l, nodeName);
		final int oldValue = this.removedUpperLabel.getInt(se);
		if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
			// the labeled value (l,i) was already removed by label modification rule.
			// A labeled value with a value equal or smaller will be modified again.
			if (Debug.ON)
				LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the previous (" + l + ", " + nodeName + ", "
						+ oldValue + ") is in the removed list");
			return false;
		}
		this.putUpperLabeledValueToRemovedList(l, nodeName, i);// once it has been added, it is useless to add it again!
		final int value = getValue(l);
		if ((value != Constants.INT_NULL) && (value <= i)) {
			if (Debug.ON)
				LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the constraint contains (" + l + ", " + value
						+ ").");
			return false;
		}
		return this.upperLabel.mergeTriple(l, nodeName, i, false);
	}

	public boolean putLabeledValue(Label l, int i) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final int putLabeledValueToRemovedList(final Label l, final int i) {
		return this.removedLabeledValue.put(l, i);
	}

	@Override
	public final int putUpperLabeledValueToRemovedList(final Label l, final ALabel n, final int i) {
		return this.removedUpperLabel.put(new InternalEntry(l, n), i);
	}

	@Override
	public int removeLabel(Label l) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final int removeLowerLabel(final Label l, final ALabel n) {
		this.setChanged();
		notifyObservers("LowerLabel:" + l.toString());
		return this.lowerLabel.remove(l, n);
	}

	@Override
	public final int removeUpperLabel(final Label l, final ALabel n) {
		return this.upperLabel.remove(l, n);
	}

	@Override
	public void setConstraintType(final ConstraintType type) {
		this.constraintType = type;
	}

	@Override
	public final void setLabeledLowerCaseValue(final LabeledContingentIntTreeMap labeledValue) {
		this.lowerLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap() : labeledValue;
	}

	@Override
	public final void setLabeledUpperCaseValue(final LabeledContingentIntTreeMap labeledValue) {
		this.upperLabel = (labeledValue == null) ? new LabeledContingentIntTreeMap() : labeledValue;
	}

	@Override
	public void setLabeledValue(LabeledIntMap labeledValue) {
		throw new UnsupportedOperationException("Core class.");
	}

	// /**
	// * A copy by reference of internal structure of edge e. Only optimize field cannot be update because it is read-only.
	// *
	// * @param e edge to clone. If null, it returns doing nothing.
	// */
	// public abstract void takeIn(final LabeledIntEdge e);

	@Override
	public int size() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final String upperLabelsAsString() {
		return this.upperLabel.toString();
	}

	@Override
	public final int upperLabelSize() {
		return this.upperLabel.size();
	}
}
