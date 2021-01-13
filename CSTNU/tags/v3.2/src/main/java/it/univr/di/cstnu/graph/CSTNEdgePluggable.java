// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;

/**
 * An implementation of CSTNEdge where the labeled value set can be configured at source level.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNEdgePluggable extends AbstractEdge implements CSTNEdge {

	/**
	 * Labeled value class used in the class.
	 */
	public static final Class<? extends LabeledIntMap> labeledValueMapImpl = LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS;

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNEdgePluggable.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 4L;

	/**
	 * Maintains log of labeled values that have been already inserted and, therefore, cannot be reinserted.
	 * This is used for speed-up the label management.
	 */
	protected Object2IntMap<Label> consideredLabeledValue;

	/**
	 * Labeled value.
	 */
	protected LabeledIntMap labeledValue;

	/**
	 * 
	 */
	CSTNEdgePluggable() {
		this((String) null);
	}

	/**
	 * A simple constructor cloner.
	 *
	 * @param e edge to clone. If null, an empty edge is created with type = normal.
	 */
	CSTNEdgePluggable(Edge e) {
		super(e);
		if (e != null && CSTNEdge.class.isAssignableFrom(e.getClass())) {
			this.labeledValue = (new LabeledIntMapSupplier<>(labeledValueMapImpl)).get(((CSTNEdge) e).getLabeledValueMap());
		} else {
			this.labeledValue = (new LabeledIntMapSupplier<>(labeledValueMapImpl)).get();
		}
		this.consideredLabeledValue = new Object2IntArrayMap<>();
		this.consideredLabeledValue.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Constructor for LabeledIntEdge.
	 *
	 * @param n a {@link java.lang.String} object.
	 */
	CSTNEdgePluggable(final String n) {
		super(n);
		this.labeledValue = (new LabeledIntMapSupplier<>(labeledValueMapImpl)).get();
		this.consideredLabeledValue = new Object2IntArrayMap<>();
		this.consideredLabeledValue.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Clear (remove) all labeled values associated to this edge.
	 */
	@Override
	public void clear() {
		super.clear();
		this.labeledValue.clear();
		this.consideredLabeledValue.clear();
	}

	/** {@inheritDoc} */
	@Override
	public Class<? extends LabeledIntMap> getLabeledIntMapImplClass() {
		return labeledValueMapImpl;
	}

	/** {@inheritDoc} */
	@Override
	public LabeledIntMap getLabeledValueMap() {
		return this.labeledValue;
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet() {
		return this.labeledValue.entrySet();
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet(ObjectSet<Object2IntMap.Entry<Label>> setToReuse) {
		return this.labeledValue.entrySet(setToReuse);
	}

	/** {@inheritDoc} */
	@Override
	public Entry<Label> getMinLabeledValue() {
		return this.labeledValue.getMinLabeledValue();
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValue() {
		return this.labeledValue.getMinValue();
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueAmongLabelsWOUnknown() {
		return this.labeledValue.getMinValueAmongLabelsWOUnknown();
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		return this.labeledValue.getMinValueConsistentWith(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueSubsumedBy(Label l) {
		return this.labeledValue.getMinValueSubsumedBy(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getValue(final Label label) {
		return this.labeledValue.get(label);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof CSTNEdge))
			return false;
		if (e == this)
			return true;
		// Use getLabeledValueMap instead of labeledValueSet() to have a better control.
		return (this.getLabeledValueMap().equals(((CSTNEdge) e).getLabeledValueMap()));
	}

	/** {@inheritDoc} */
	@Override
	public boolean isCSTNEdge() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.labeledValue.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 *
	 * Merges the labeled value i to the set of labeled values of this edge.
	 */
	@Override
	public boolean mergeLabeledValue(final Label l, final int i) {
		if ((l == null) || (i == Constants.INT_NULL))
			return false;
		final int oldValue = this.consideredLabeledValue.getInt(l);
		if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
			// The new value is greater or equal the old one, the new value can be ignored.
			// the labeled value (l,i) was already removed in the past, it will be not stored.
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST))
					LOG.log(Level.FINEST, "The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", "
							+ oldValue + ") is in the removed list");
			}
			return false;
		}
		this.consideredLabeledValue.put(l, i); // once a value has been inserted, it is useless to insert it again in the future.
		return this.labeledValue.put(l, i);
	}

	/** {@inheritDoc} */
	@Override
	public void mergeLabeledValue(LabeledIntMap map) {
		for (Object2IntMap.Entry<Label> entry : map.entrySet())
			this.mergeLabeledValue(entry.getKey(), entry.getIntValue());
	}

	/**
	 * {@inheritDoc}
	 *
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 * @see #mergeLabeledValue(Label, int)
	 */
	@Override
	public boolean mergeLabeledValue(final String ls, final int i) {
		// just a wrapper!
		return this.mergeLabeledValue(Label.parse(ls), i);
	}

	/** {@inheritDoc} */
	@Override
	public CSTNEdgePluggable newInstance() {
		return new CSTNEdgePluggable();
	}

	/** {@inheritDoc} */
	@Override
	public CSTNEdgePluggable newInstance(Edge edge) {
		return new CSTNEdgePluggable(edge);
	}

	/** {@inheritDoc} */
	@Override
	public CSTNEdgePluggable newInstance(String name1) {
		return new CSTNEdgePluggable(name1);
	}

	/** {@inheritDoc} */
	@Override
	public boolean putLabeledValue(final Label l, final int i) {
		this.consideredLabeledValue.put(l, i); // once a value has been inserted, it is useless to insert it again in the future.
		return this.labeledValue.put(l, i);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 */
	@Override
	public int removeLabeledValue(final Label l) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				CSTNEdgePluggable.LOG.finer("Removing label '" + l + "' from the edge " + this.toString());
			}
		}
		this.consideredLabeledValue.removeInt(l); // If it is removed, we assume that it can be reconsidered to be added.
		return this.labeledValue.remove(l);
	}

	/** {@inheritDoc} */
	@Override
	public void setLabeledValueMap(LabeledIntMap inputLabeledValue) {
		if (inputLabeledValue == null) {
			this.labeledValue.clear();
		} else {
			this.labeledValue = inputLabeledValue;
		}
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return this.labeledValue.size();
	}

	/** {@inheritDoc} */
	@Override
	public void takeIn(Edge e) {
		if (e == null)
			return;
		super.takeIn(e);
		if (e instanceof CSTNEdgePluggable) {
			CSTNEdgePluggable e1 = (CSTNEdgePluggable) e;
			this.constraintType = e1.constraintType;
			this.labeledValue = e1.labeledValue;
			this.consideredLabeledValue = e1.consideredLabeledValue;
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Constants.OPEN_TUPLE + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getConstraintType() + "; "
				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
				+ Constants.CLOSE_TUPLE;
	}
}
