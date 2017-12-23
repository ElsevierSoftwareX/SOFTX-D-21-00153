/**
 *
 */
package it.univr.di.cstnu.graph;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;

import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

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
	final static class InternalEntry implements Object2ObjectMap.Entry<Label, ALabel>, Comparable<Object2ObjectMap.Entry<Label, ALabel>> {

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
	 * Font for edge label rendering
	 */
	public final static Function<LabeledIntEdge, Font> edgeFontFunction = new Function<LabeledIntEdge, Font>() {
		/**
		 * 
		 */
		Font b = new Font("Helvetica", Font.BOLD, 14);
		/**
		 * 
		 */
		protected boolean bold = false;
		/**
		 * 
		 */
		Font f = new Font("Helvetica", Font.PLAIN, 14);

		@Override
		public Font apply(LabeledIntEdge e) {
			if (this.bold)
				return this.b;
			return this.f;
		}

		/**
		 * @param bold
		 */
		@SuppressWarnings("unused")
		public void setBold(boolean bold) {
			this.bold = bold;
		}
	};

	/**
	 * Font for edge label rendering
	 */
	public final static Function<LabeledIntEdge, String> edgeLabelFunction = new Function<LabeledIntEdge, String>() {
		/**
		 * Returns a label for the edge
		 */
		@Override
		public String apply(final LabeledIntEdge e) {
			final StringBuffer sb = new StringBuffer();
			sb.append((e.getName().length() == 0 ? "''" : e.getName()));
			sb.append("; ");
			if (e.getLabeledValueMap().size() > 0) {
				sb.append(e.getLabeledValueMap().toString());
			}
			if (e.getUpperCaseValueMap().size() > 0) {
				sb.append("; UL: ");
				sb.append(e.upperCaseValuesAsString());
			}
			if (!e.getLowerCaseValue().isEmpty()) {
				sb.append("; LL:");
				sb.append(e.lowerCaseValueAsString());
			}

			return sb.toString();
		}
	};

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
	 * Morris Lower case value augmented by a propositional label. The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledLowerCaseValue lowerCaseValue;

	/**
	 * Removed Labeled value.<br>
	 * Only methods inside AbstractLabeledIntEdge or implementing class can modified such field.
	 */
	Object2IntMap<Label> removedLabeledValue;

	/**
	 * Removed Upper Case Labeled value.<br>
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been removed in the past in order to avoid to add it a second time.
	 */
	Object2IntMap<Entry<Label, ALabel>> removedUpperCaseValue;

	/**
	 * Morris Upper case value augmented by a propositional label. The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledALabelIntTreeMap upperCaseValue;

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
			this.upperCaseValue = new LabeledALabelIntTreeMap(e.getUpperCaseValueMap());
			this.lowerCaseValue = LabeledLowerCaseValue.create(e.getLowerCaseValue());
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
		this.upperCaseValue = new LabeledALabelIntTreeMap();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;

		this.removedLabeledValue = new Object2IntArrayMap<>();
		this.removedLabeledValue.defaultReturnValue(Constants.INT_NULL);
		this.removedUpperCaseValue = new Object2IntArrayMap<>();
		this.removedUpperCaseValue.defaultReturnValue(Constants.INT_NULL);
	}

	@Override
	public void clear() {
		this.upperCaseValue.clear();
		this.lowerCaseValue.clear();
		this.removedLabeledValue.clear();
		this.removedUpperCaseValue.clear();
	}

	@Override
	public void clearLabels() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final void clearLowerCaseValue() {
		this.lowerCaseValue.clear();
	}

	@Override
	public final void clearUpperCaseValues() {
		this.upperCaseValue.clear();
	}

	@Override
	public void copyLabeledValueMap(LabeledIntMap labeledValue) {
		throw new UnsupportedOperationException("Core class.");
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
	public final boolean equalsAllLabeledValues(final LabeledIntEdge e) {
		if (e == null || e == this)
			return false;

		// Use getLabeledValueMap instead of labeledValueSet() to have a better control.
		return (this.getLabeledValueMap().equals(e.getLabeledValueMap())
				&& this.getLowerCaseValue().equals(e.getLowerCaseValue())
				&& this.getUpperCaseValueMap().equals(e.getUpperCaseValueMap()));
	}

	@Override
	public final LabeledALabelIntTreeMap getAllUpperCaseAndLabeledValuesMaps() {
		LabeledALabelIntTreeMap union = new LabeledALabelIntTreeMap();

		for (final ALabel alabel : this.upperCaseValue.keySet()) {
			union.put(alabel, this.upperCaseValue.get(alabel));
		}
		union.put(ALabel.emptyLabel, this.getLabeledValueMap());
		return union;
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
	public LabeledLowerCaseValue getLowerCaseValue() {
		return this.lowerCaseValue;
	}

	@Override
	public final int getMinUpperCaseValue() {
		return this.upperCaseValue.getMinValue();
	}

	@Override
	public int getMinValue() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public Object2IntMap.Entry<Label> getMinLabeledValue() {
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
	public int getMinValueSubsumedBy(Label l) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final Object2IntMap<Label> getRemovedLabeledValuesMap() {
		return this.removedLabeledValue;
	}

	@Override
	public int getUpperCaseMinValueConsistentWith(Label l, ALabel upperL) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final int getUpperCaseValue(final Label l, final ALabel name1) {
		return this.upperCaseValue.getValue(l, name1);
	}

	@Override
	public final LabeledALabelIntTreeMap getUpperCaseValueMap() {
		return this.upperCaseValue;
	}

	// @Override
	// public final ObjectSet<Entry<ALabel, LabeledIntTreeMap>> getUpperCaseValueSet() {
	// return this.upperCaseValue.entrySet();
	// }

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
	public final String lowerCaseValueAsString() {
		return this.lowerCaseValue.toString();
	}

	@Override
	public final int lowerCaseValueSize() {
		return this.lowerCaseValue.isEmpty() ? 0 : 1;
	}

	@Override
	public boolean mergeLabeledValue(Label l, int i) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public void mergeLabeledValue(LabeledIntMap map) {
		throw new UnsupportedOperationException("Core class.");
	}

	/**
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 *
	 * @param i an integer.
	 * @return true if the operation was successful, false otherwise.
	 * @see #mergeLabeledValue(Label, int)
	 * @param ls a {@link java.lang.String} object.
	 */
	@Override
	public boolean mergeLabeledValue(final String ls, final int i) {
		// just a wrapper!
		return this.mergeLabeledValue(Label.parse(ls), i);
	}

	@Override
	public final boolean mergeUpperCaseValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + nodeName + ", " + Constants.formatInt(i) + ").");
		InternalEntry se = new InternalEntry(l, nodeName);
		final int oldValue = this.removedUpperCaseValue.getInt(se);
		if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
			// the labeled value (l,i) was already removed by label modification rule.
			// A labeled value with a value equal or smaller will be modified again.
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i + ") has not been stored because the previous (" + l + ", " + nodeName
							+ ", " + oldValue + ") is in the removed list");
				}
			}
			return false;
		}
		this.putUpperCaseValueToRemovedList(l, nodeName, i);// once it has been added, it is useless to add it again!
		// Check if a standard labeled value is more restrictive of the one to put.
		final int minNormalValueSubSumedByL = this.getLabeledValueMap().getMinValueSubsumedBy(l);
		if ((minNormalValueSubSumedByL != Constants.INT_NULL) && (minNormalValueSubSumedByL <= i)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("The labeled value (" + l + ", " + nodeName + ", " + i
							+ ") has not been stored because the value is greater than the labeled minimal value subsume by " + l + ".");
				}
			}
			return false;
		}
		return this.upperCaseValue.mergeTriple(l, nodeName, i, false);
	}

	@Override
	public boolean putLabeledValue(Label l, int i) {
		throw new UnsupportedOperationException("Core class.");
	}

	/**
	 * Put the triple in the removedUpperLabel list in order to avoid to consider it again in the future.
	 * 
	 * @param l
	 * @param n
	 * @param i
	 * @return the old value, or the {@link Constants#INT_NULL} if no value was present for the given key.
	 */
	int putUpperCaseValueToRemovedList(final Label l, final ALabel n, final int i) {
		return this.removedUpperCaseValue.put(new InternalEntry(l, n), i);
	}

	@Override
	public int removeLabel(Label l) {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final int removeLowerCaseValue() {
		if (this.lowerCaseValue.isEmpty())
			return Constants.INT_NULL;
		this.setChanged();
		notifyObservers("LowerLabel");
		int i = this.lowerCaseValue.getValue();
		this.lowerCaseValue.clear();
		return i;
	}

	@Override
	public final int removeUpperCaseValue(final Label l, final ALabel n) {
		this.removedUpperCaseValue.remove(new InternalEntry(l, n));
		return this.upperCaseValue.remove(l, n);
	}

	@Override
	public void setConstraintType(final ConstraintType type) {
		this.constraintType = type;
	}

	@Override
	public void setLowerCaseValue(final Label l, final ALabel nodeName, final int i) {
		this.lowerCaseValue = LabeledLowerCaseValue.create(nodeName, i, l);
	}

	@Override
	public final void setLowerCaseValue(final LabeledLowerCaseValue labeledValue) {
		this.lowerCaseValue = labeledValue;
	}

	@Override
	public final void setUpperCaseValueMap(final LabeledALabelIntTreeMap labeledValue) {
		this.upperCaseValue = (labeledValue == null) ? new LabeledALabelIntTreeMap() : labeledValue;
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Core class.");
	}

	@Override
	public final String upperCaseValuesAsString() {
		return this.upperCaseValue.toString();
	}

	@Override
	public final int upperCaseValueSize() {
		return this.upperCaseValue.size();
	}
}
