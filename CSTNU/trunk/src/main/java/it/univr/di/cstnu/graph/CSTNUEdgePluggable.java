/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * An implementation of CSTNUEdge where the labeled value set can be plugged during the creation.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNUEdgePluggable extends BasicCSTNUEdgePluggable implements CSTNUEdge {

	/**
	 *
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * Morris Lower case value augmented by a propositional label.Br>
	 * The name of node has to be equal to the original name. No case modifications are necessary!
	 */
	LabeledLowerCaseValue lowerCaseValue;

	/**
	 * class initializer
	 */
	{
		/**
		 * logger
		 */
		LOG = Logger.getLogger(CSTNUEdgePluggable.class.getName());
	}

	/**
	 * <C extends LabeledIntMap> CSTNUEdgePluggable(Class<C> labeledIntMapImpl) {
	 * this((String) null, labeledIntMapImpl);
	 * }
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable() {
		this((String) null);
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param e the edge to clone.
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable(Edge e) {
		super(e);
		if (e != null && CSTNUEdge.class.isAssignableFrom(e.getClass())) {
			CSTNUEdge e1 = (CSTNUEdge) e;
			this.lowerCaseValue = LabeledLowerCaseValue.create(e1.getLowerCaseValue());
		} else {
			this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
		}
	}

	/**
	 * @param n
	 */
	<C extends LabeledIntMap> CSTNUEdgePluggable(final String n) {
		super(n);
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
	}

	@Override
	public void clear() {
		super.clear();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
	}

	@Override
	public void clearLowerCaseValues() {
		this.removeLowerCaseValue();
	}

	@Override
	public LabeledLowerCaseValue getLowerCaseValue() {
		return this.lowerCaseValue;
	}

	@Override
	public boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof CSTNUEdge))
			return false;
		if (e == this)
			return true;
		if (!super.hasSameValues(e))
			return false;
		CSTNUEdge e1 = (CSTNUEdge) e;
		return (this.getLowerCaseValue().equals(e1.getLowerCaseValue()));
	}

	@Override
	public boolean isCSTNUEdge() {
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
	public int lowerCaseValueSize() {
		return this.lowerCaseValue.isEmpty() ? 0 : 1;
	}

	@Override
	public CSTNUEdgePluggable newInstance() {
		return new CSTNUEdgePluggable();
	}

	@Override
	public CSTNUEdgePluggable newInstance(Edge edge) {
		return new CSTNUEdgePluggable(edge);
	}

	@Override
	public CSTNUEdgePluggable newInstance(String name1) {
		return new CSTNUEdgePluggable(name1);
	}

	@Override
	public int removeLowerCaseValue() {
		if (this.lowerCaseValue.isEmpty())
			return Constants.INT_NULL;

		int i = this.lowerCaseValue.getValue();
		this.lowerCaseValue = LabeledLowerCaseValue.emptyLabeledLowerCaseValue;
		this.setConstraintType(ConstraintType.normal);
		this.setChanged();
		notifyObservers("LowerLabel:remove");
		return i;
	}

	@Override
	public void setLowerCaseValue(final Label l, final ALabel nodeName, final int i) {
		this.setLowerCaseValue(LabeledLowerCaseValue.create(nodeName, i, l));
	}

	@Override
	public boolean mergeLowerCaseValue(Label l, ALabel nodeName, int i) {
		this.setLowerCaseValue(l, nodeName, i);
		return true;
	}

	@Override
	public void setLowerCaseValue(final LabeledLowerCaseValue inputLabeledValue) {
		this.lowerCaseValue = inputLabeledValue;
		if (!this.lowerCaseValue.isEmpty()) {
			this.setConstraintType(ConstraintType.contingent);
			this.setChanged();
			notifyObservers("LowerLabel:add");
		}
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
			this.lowerCaseValue = e1.lowerCaseValue;
		}
	}

	@Override
	public final boolean putLowerCaseValue(final Label l, ALabel nodeName, final int i) {
		return this.mergeLowerCaseValue(l, nodeName, i);
	}

}
