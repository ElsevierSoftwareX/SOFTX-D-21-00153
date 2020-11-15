/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * An implementation of CSTNUEdge where the labeled value set can be plugged during the creation.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNPSUEdgePluggable extends BasicCSTNUEdgePluggable implements CSTNPSUEdge {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The CSTNU controllability check algorithm needs to know if a labeled value has been already considered
	 * in the past in order to avoid to add it a second time.
	 */
	Object2IntMap<Entry<Label, ALabel>> consideredLowerCaseValue;

	/**
	 * Morris Lower case value augmented by a propositional label.<br>
	 * The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledALabelIntTreeMap lowerCaseValue;

	/**
	 * class initializer
	 */
	{
		/**
		 * logger
		 */
		LOG = Logger.getLogger(CSTNPSUEdgePluggable.class.getName());
	}

	/**
	 * <C extends LabeledIntMap> CSTNUEdgePluggable(Class<C> labeledIntMapImpl) {
	 * this((String) null, labeledIntMapImpl);
	 * }
	 */
	<C extends LabeledIntMap> CSTNPSUEdgePluggable() {
		this((String) null);
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param e the edge to clone.
	 */
	<C extends LabeledIntMap> CSTNPSUEdgePluggable(Edge e) {
		super(e);
		if (e != null && CSTNPSUEdge.class.isAssignableFrom(e.getClass())) {
			CSTNPSUEdge e1 = (CSTNPSUEdge) e;
			this.lowerCaseValue = new LabeledALabelIntTreeMap(e1.getLowerCaseValueMap());
		} else {
			if (e != null && CSTNUEdge.class.isAssignableFrom(e.getClass())) {
				CSTNUEdge e1 = (CSTNUEdge) e;
				this.lowerCaseValue = new LabeledALabelIntTreeMap();
				LabeledLowerCaseValue lcv = e1.getLowerCaseValue();
				if (!lcv.isEmpty()) {
					this.lowerCaseValue.mergeTriple(lcv.getLabel(), lcv.getNodeName(), lcv.getValue());
				}
			} else {
				this.lowerCaseValue = new LabeledALabelIntTreeMap();
			}
		}
		this.consideredLowerCaseValue = new Object2IntArrayMap<>();
		this.consideredLowerCaseValue.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * @param n
	 */
	<C extends LabeledIntMap> CSTNPSUEdgePluggable(final String n) {
		super(n);
		this.lowerCaseValue = new LabeledALabelIntTreeMap();
		this.consideredLowerCaseValue = new Object2IntArrayMap<>();
		this.consideredLowerCaseValue.defaultReturnValue(Constants.INT_NULL);

	}

	@Override
	public void clear() {
		super.clear();
		this.lowerCaseValue.clear();
		this.consideredLowerCaseValue.clear();
	}

	@Override
	public void clearLowerCaseValues() {
		this.lowerCaseValue.clear();
	}

	@Override
	public final LabeledALabelIntTreeMap getAllLowerCaseAndLabeledValuesMaps() {
		LabeledALabelIntTreeMap union = new LabeledALabelIntTreeMap();

		for (final ALabel alabel : this.lowerCaseValue.keySet()) {
			union.put(alabel, this.lowerCaseValue.get(alabel));
		}
		union.put(ALabel.emptyLabel, this.getLabeledValueMap());
		return union;
	}

	@Override
	public LabeledLowerCaseValue getLowerCaseValue() {
		Entry<Label, Object2IntMap.Entry<ALabel>> entry = this.lowerCaseValue.getMinValue();
		return (entry != null) ? LabeledLowerCaseValue.create(entry.getValue().getKey(), entry.getValue().getIntValue(), entry.getKey())
				: LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
	}

	@Override
	public int getLowerCaseValue(Label l, ALabel name1) {
		return this.lowerCaseValue.getValue(l, name1);
	}

	@Override
	public LabeledALabelIntTreeMap getLowerCaseValueMap() {
		return this.lowerCaseValue;
	}

	@Override
	public final boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof CSTNPSUEdge))
			return false;
		if (e == this)
			return true;
		if (!super.hasSameValues(e))
			return false;
		CSTNPSUEdge e1 = (CSTNPSUEdge) e;
		return (this.getLowerCaseValueMap().equals(e1.getLowerCaseValueMap()));
	}

	@Override
	public boolean isCSTNPSUEdge() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty() && this.lowerCaseValue.isEmpty();
	}

	@Override
	public String lowerCaseValuesAsString() {
		return this.lowerCaseValue.toString();
	}

	@Override
	public final int lowerCaseValueSize() {
		return this.lowerCaseValue.size();
	}

	@Override
	public boolean mergeLabeledValue(final Label l, final int i) {
		boolean added = super.mergeLabeledValue(l, i);
		// If a value is positive, and there are more lower-case values, some of them may be simplified.
		if (added && i >= 0 && this.lowerCaseValue.size() > 0) {
			for (ALabel LCALabel : this.lowerCaseValue.keySet()) {
				LabeledIntMap labeledLCValues = this.lowerCaseValue.get(LCALabel);
				for (Label LCLabel : labeledLCValues.keySet()) {
					int LCValue = labeledLCValues.get(LCLabel);
					if (i <= LCValue && LCLabel.subsumes(l)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST))
								LOG.log(Level.FINEST,
										"The value (" + l + ", " + i + ") makes redundant lower case value (" + LCLabel + ", "
												+ LCALabel + ":" + LCValue + ").  Last one is removed.");
						}
						this.setLowerCaseValueAsConsidered(LCLabel, LCALabel, LCValue);
						this.removeLowerCaseValue(LCLabel, LCALabel);
					}
				}
			}
		}
		return added;
	}

	@Override
	public final boolean mergeLowerCaseValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + nodeName + ", " + Constants.formatInt(i) + ").");
		InternalEntry se = new InternalEntry(l, nodeName);
		final int oldValue = this.consideredLowerCaseValue.getInt(se);
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
		this.setLowerCaseValueAsConsidered(l, nodeName, i);// once it has been added, it is useless to add it again!
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
		boolean added = this.lowerCaseValue.mergeTriple(l, nodeName, i, false);
		// if (added && !this.lowerCaseValue.isEmpty()) {
		// this.setConstraintType(ConstraintType.contingent);
		// this.setChanged();
		// notifyObservers("LowerLabel:add");
		// }
		return added;
	}

	@Override
	public final boolean putLowerCaseValue(final Label l, ALabel nodeName, final int i) {
		if ((l == null) || (nodeName == null) || (i == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + nodeName + ", " + Constants.formatInt(i) + ").");
		InternalEntry se = new InternalEntry(l, nodeName);
		final int oldValue = this.consideredLowerCaseValue.getInt(se);
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
		this.setLowerCaseValueAsConsidered(l, nodeName, i);// once it has been added, it is useless to add it again!
		return this.lowerCaseValue.mergeTriple(l, nodeName, i, true);
	}

	@Override
	public CSTNPSUEdgePluggable newInstance() {
		return new CSTNPSUEdgePluggable();
	}

	@Override
	public CSTNPSUEdgePluggable newInstance(Edge edge) {
		return new CSTNPSUEdgePluggable(edge);
	}

	@Override
	public CSTNPSUEdgePluggable newInstance(String name1) {
		return new CSTNPSUEdgePluggable(name1);
	}

	@Override
	public final int removeLowerCaseValue(final Label l, final ALabel n) {
		// this.consideredLowerCaseValue.removeInt(new InternalEntry(l, n));
		return this.lowerCaseValue.remove(l, n);
	}

	@Override
	public void setLowerCaseValue(LabeledALabelIntTreeMap lowerCaseValue1) {
		this.lowerCaseValue = (lowerCaseValue1 == null) ? new LabeledALabelIntTreeMap() : lowerCaseValue1;
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
		super.takeIn(e);
		if (e instanceof CSTNPSUEdgePluggable) {
			CSTNPSUEdgePluggable e1 = (CSTNPSUEdgePluggable) e;
			this.lowerCaseValue = e1.lowerCaseValue;
			this.consideredLowerCaseValue = e1.consideredLowerCaseValue;
		}
	}

	/**
	 * Set the triple as already considered in order to avoid to consider it again in the future.
	 * 
	 * @param l
	 * @param n
	 * @param i
	 * @return the old value associated to (l,n), or the {@link Constants#INT_NULL} if no value was present.
	 */
	int setLowerCaseValueAsConsidered(final Label l, final ALabel n, final int i) {
		return this.consideredLowerCaseValue.put(new InternalEntry(l, n), i);
	}
}