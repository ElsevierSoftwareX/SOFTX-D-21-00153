/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * An implementation of CSTNUEdge where the labeled value set can be plugged during the creation.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNUEdgePluggable extends CSTNEdgePluggable implements CSTNUEdge {

	/**
	 * Represents a pair (Label, String).
	 * 
	 * @author posenato
	 */
	final static class InternalEntry implements Object2ObjectMap.Entry<Label, ALabel>, Comparable<Object2ObjectMap.Entry<Label, ALabel>> {

		ALabel aLabel;
		Label label;

		/**
		 * @param inputLabel
		 * @param inputALabel
		 */
		public InternalEntry(Label inputLabel, ALabel inputALabel) {
			this.label = inputLabel;
			this.aLabel = inputALabel;
		}

		@Override
		public int compareTo(Object2ObjectMap.Entry<Label, ALabel> o) {
			if (o == null)
				return 1;
			if (this == o)
				return 0;
			int i = this.label.compareTo(o.getKey());
			if (i != 0)
				return i;
			return this.aLabel.compareTo(o.getValue());
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof InternalEntry))
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
			return this.label.hashCode() + 31 * this.aLabel.hashCode();
		}

		@Override
		public ALabel setValue(ALabel value) {
			ALabel old = ALabel.clone(this.aLabel);
			this.aLabel = value;
			return old;
		}

		@Override
		public String toString() {
			return "(" + this.aLabel + ", " + this.label + ")";
		}

	}

	/**
	 *
	 */
	private static final long serialVersionUID = 3L;
	/**
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been already considered
	 * in the past in order to avoid to add it a second time.
	 */
	Object2IntMap<Entry<Label, ALabel>> consideredUpperCaseValue;

	/**
	 * Morris Lower case value augmented by a propositional label. The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledLowerCaseValue lowerCaseValue;

	/**
	 * Morris Upper case value augmented by a propositional label. The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledALabelIntTreeMap upperCaseValue;

	/**
	 * class initializer
	 */
	{
		/**
		 * logger
		 */
		LOG = Logger.getLogger("CSTNUEdgePluggable");
	}

	/**
	 * @param labeledIntMapImpl
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable(Class<C> labeledIntMapImpl) {
		this((String) null, labeledIntMapImpl);
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param e the edge to clone.
	 * @param labeledIntMapImplementation
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable(Edge e, Class<C> labeledIntMapImplementation) {
		super(e, labeledIntMapImplementation);
		if (e != null && CSTNUEdge.class.isAssignableFrom(e.getClass())) {
			CSTNUEdge e1 = (CSTNUEdge) e;
			this.upperCaseValue = new LabeledALabelIntTreeMap(e1.getUpperCaseValueMap());
			this.lowerCaseValue = LabeledLowerCaseValue.create(e1.getLowerCaseValue());
			this.consideredUpperCaseValue = new Object2IntArrayMap<>();
			this.consideredUpperCaseValue.defaultReturnValue(Constants.INT_NULL);
		} else {
			this.upperCaseValue = new LabeledALabelIntTreeMap();
			this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
			this.consideredUpperCaseValue = new Object2IntArrayMap<>();
			this.consideredUpperCaseValue.defaultReturnValue(Constants.INT_NULL);
		}
	}

	/**
	 * @param n
	 * @param labeledIntMapImplementation
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable(final String n, Class<C> labeledIntMapImplementation) {
		super(n, labeledIntMapImplementation);
		this.upperCaseValue = new LabeledALabelIntTreeMap();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
		this.consideredUpperCaseValue = new Object2IntArrayMap<>();
		this.consideredUpperCaseValue.defaultReturnValue(Constants.INT_NULL);
	}

	@Override
	public void clear() {
		super.clear();
		this.upperCaseValue.clear();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
		this.consideredUpperCaseValue.clear();
	}

	@Override
	public final void clearLowerCaseValue() {
		this.removeLowerCaseValue();
	}

	@Override
	public final void clearUpperCaseValues() {
		this.upperCaseValue.clear();
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
	public LabeledLowerCaseValue getLowerCaseValue() {
		return this.lowerCaseValue;
	}

	@Override
	public final int getMinUpperCaseValue() {
		return this.upperCaseValue.getMinValue();
	}

	@Override
	public final int getUpperCaseValue(final Label l, final ALabel name1) {
		return this.upperCaseValue.getValue(l, name1);
	}

	@Override
	public final LabeledALabelIntTreeMap getUpperCaseValueMap() {
		return this.upperCaseValue;
	}

	@Override
	public final boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof CSTNUEdge))
			return false;
		if (e == this)
			return true;
		if (!super.hasSameValues(e))
			return false;
		CSTNUEdge e1 = (CSTNUEdge) e;
		return (this.getLowerCaseValue().equals(e1.getLowerCaseValue())
				&& this.getUpperCaseValueMap().equals(e1.getUpperCaseValueMap()));
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && this.lowerCaseValue.isEmpty() && this.upperCaseValue.isEmpty();
	}

	@Override
	public boolean isCSTNUEdge() {
		return true;
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
	public boolean mergeLabeledValue(final Label l, final int i) {
		boolean added = super.mergeLabeledValue(l, i);
		if (added && this.upperCaseValue.size() > 0) {
			// I try to clean UPPER values
			// Since this.labeledValue.put(l, i) can simplify labeled value set,
			// it is necessary to check every UPPER CASE with any labeled value, not only the last one inserted!
			// 2017-10-31 I verified that it is necessary to improve the performance!
			// 2018-12-24 I re-verified that it is necessary to improve the performance!
			// int maxValueWOUpperCase = this.labeledValue.getMaxValue();
			// if (this.labeledValue.size() >= nBeforeAdd && (this.labeledValue.get(l) == i)) {
			// the added element did not simplify the set, we compare UC values only with it.
			for (ALabel UCALabel : this.upperCaseValue.keySet()) {
				LabeledIntMap labeledUCValues = this.upperCaseValue.get(UCALabel);
				for (Label UCLabel : labeledUCValues.keySet()) {
					int UCValue = labeledUCValues.get(UCLabel);
					// if (UCaseValue >= maxValueWOUpperCase) {
					// //this wait is useless because a normal constraint
					// this.putUpperCaseValueToRemovedList(UCLabel, UCALabel, UCaseValue);
					// this.removeUpperCaseValue(UCLabel, UCALabel);
					// continue;
					// }
					if (i <= UCValue && UCLabel.subsumes(l)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST))
								LOG.log(Level.FINEST,
										"The value (" + l + ", " + i + ") makes redundant upper case value (" + UCLabel + ", "
												+ UCALabel + ":" + UCValue + ").  Last one is removed.");
						}
						this.setUpperCaseValueAsConsidered(UCLabel, UCALabel, UCValue);
						this.removeUpperCaseValue(UCLabel, UCALabel);
					}
				}
			}
			// } else {
			// // this.labeledvalue set has been simplified. We consider all values of this.labeledvalue
			// 2018-12-17 Too much expensive, we check only the new inserted value.
			// for (ALabel UCALabel : upperCaseValueValueMap.keySet()) {
			// LabeledIntTreeMap labeledUCValues = upperCaseValueValueMap.get(UCALabel);
			// for (Label UCLabel : labeledUCValues.keySet()) {
			// int UCaseValue = labeledUCValues.get(UCLabel);
			// int min = this.labeledValue.getMinValueSubsumedBy(UCLabel);
			// if (min == Constants.INT_NULL)
			// continue;
			// if (min <= UCaseValue) {
			// this.putUpperCaseValueToRemovedList(UCLabel, UCALabel, UCaseValue);
			// this.removeUpperCaseValue(UCLabel, UCALabel);
			// }
			// }
			// }
			// }
		}
		return added;
	}

	@Override
	public final boolean mergeUpperCaseValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + nodeName + ", " + Constants.formatInt(i) + ").");
		InternalEntry se = new InternalEntry(l, nodeName);
		final int oldValue = this.consideredLabeledValue.getInt(se);
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
		this.setUpperCaseValueAsConsidered(l, nodeName, i);// once it has been added, it is useless to add it again!
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
	public final int removeLowerCaseValue() {
		if (this.lowerCaseValue.isEmpty())
			return Constants.INT_NULL;
		this.setChanged();
		notifyObservers("LowerLabel");
		int i = this.lowerCaseValue.getValue();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
		this.setConstraintType(ConstraintType.normal);
		return i;
	}

	@Override
	public final int removeUpperCaseValue(final Label l, final ALabel n) {
		this.consideredUpperCaseValue.removeInt(new InternalEntry(l, n));
		return this.upperCaseValue.remove(l, n);
	}

	@Override
	public void setLowerCaseValue(final Label l, final ALabel nodeName, final int i) {
		this.setLowerCaseValue(LabeledLowerCaseValue.create(nodeName, i, l));
	}

	@Override
	public final void setLowerCaseValue(final LabeledLowerCaseValue inputLabeledValue) {
		this.lowerCaseValue = inputLabeledValue;
		if (!this.lowerCaseValue.isEmpty())
			this.setConstraintType(ConstraintType.contingent);

	}

	@Override
	public final void setUpperCaseValueMap(final LabeledALabelIntTreeMap inputLabeledValue) {
		this.upperCaseValue = (inputLabeledValue == null) ? new LabeledALabelIntTreeMap() : inputLabeledValue;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param e
	 */
	@Override
	public void takeIn(Edge e) {
		if (e == null)
			return;
		if (e instanceof CSTNUEdgePluggable) {
			super.takeIn(e);
			CSTNUEdgePluggable e1 = (CSTNUEdgePluggable) e;
			this.upperCaseValue = e1.upperCaseValue;
			this.lowerCaseValue = e1.lowerCaseValue;
			this.consideredUpperCaseValue = e1.consideredUpperCaseValue;
		}
	}

	/**
	 * {@inheritDoc} Return a string representation of labeled values.h
	 */
	@Override
	public String toString() {
		StringBuilder superS = new StringBuilder(super.toString());
		superS.delete(superS.length() - Constants.CLOSE_TUPLE.length(), superS.length());
		if (this.upperCaseValueSize() > 0)
			superS.append("UL: " + this.upperCaseValuesAsString() + "; ");
		if (this.lowerCaseValueSize() > 0)
			superS.append("LL: " + this.lowerCaseValueAsString() + "; ");
		superS.append(Constants.CLOSE_TUPLE);
		return superS.toString();
	}

	@Override
	public final String upperCaseValuesAsString() {
		return this.upperCaseValue.toString();
	}

	@Override
	public final int upperCaseValueSize() {
		return this.upperCaseValue.size();
	}

	/**
	 * Set the triple as already considered in order to avoid to consider it again in the future.
	 * 
	 * @param l
	 * @param n
	 * @param i
	 * @return the old value associated to (l,n), or the {@link Constants#INT_NULL} if no value was present.
	 */
	int setUpperCaseValueAsConsidered(final Label l, final ALabel n, final int i) {
		return this.consideredUpperCaseValue.put(new InternalEntry(l, n), i);
	}

	@Override
	public CSTNUEdgePluggable newInstance() {
		return newInstance(LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS);
	}

	@Override
	public CSTNUEdgePluggable newInstance(Class<? extends LabeledIntMap> labeledIntMapImpl) {
		return new CSTNUEdgePluggable(labeledIntMapImpl);
	}

	@Override
	public CSTNUEdgePluggable newInstance(Edge edge) {
		return new CSTNUEdgePluggable(edge, LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS);
	}

	@Override
	public CSTNUEdgePluggable newInstance(Edge edge, Class<? extends LabeledIntMap> labeledIntMapImpl) {
		return new CSTNUEdgePluggable(edge, labeledIntMapImpl);
	}

	@Override
	public CSTNUEdgePluggable newInstance(String name1) {
		return new CSTNUEdgePluggable(name1, LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS);
	}

	@Override
	public CSTNUEdgePluggable newInstance(String name1, Class<? extends LabeledIntMap> labeledIntMapImpl) {
		return new CSTNUEdgePluggable(name1, labeledIntMapImpl);
	}

	@Override
	public boolean putLabeledValue(final Label l, final int i) {
		this.consideredLabeledValue.put(l, i); // once a value has been inserted, it is useless to insert it again in the future.
		return this.labeledValue.put(l, i);
	}
}
