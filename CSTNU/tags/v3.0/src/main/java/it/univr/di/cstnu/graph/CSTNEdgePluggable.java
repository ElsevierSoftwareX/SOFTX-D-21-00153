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
	 * Clear (remove) all labeled values associated to this edge.
	 */
	@Override
	public void clear() {
		super.clear();
		this.labeledValue.clear();
		this.consideredLabeledValue.clear();
	}

	@Override
	public Class<? extends LabeledIntMap> getLabeledIntMapImplClass() {
		return labeledValueMapImpl;
	}

	/**
	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
	 */
	@Override
	public LabeledIntMap getLabeledValueMap() {
		return this.labeledValue;
	}

	/**
	 * @return the labeled values as a set
	 */
	@Override
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet() {
		return this.labeledValue.entrySet();
	}

	/**
	 * @return the labeled values as a set
	 */
	@Override
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet(ObjectSet<Object2IntMap.Entry<Label>> setToReuse) {
		return this.labeledValue.entrySet(setToReuse);
	}

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link Constants#INT_NULL} otherwise.
	 */
	@Override
	public Entry<Label> getMinLabeledValue() {
		return this.labeledValue.getMinLabeledValue();
	}

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link Constants#INT_NULL} otherwise.
	 */
	@Override
	public int getMinValue() {
		return this.labeledValue.getMinValue();
	}

	/**
	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some; {@link Constants#INT_NULL}
	 *         otherwise.
	 */
	@Override
	public int getMinValueAmongLabelsWOUnknown() {
		return this.labeledValue.getMinValueAmongLabelsWOUnknown();
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
	 */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		return this.labeledValue.getMinValueConsistentWith(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueSubsumedBy(Label l) {
		return this.labeledValue.getMinValueSubsumedBy(l);
	}

	/**
	 * @param label label
	 * @return the value associated to label it it exists, {@link Constants#INT_NULL} otherwise.
	 */
	@Override
	public int getValue(final Label label) {
		return this.labeledValue.get(label);
	}

	@Override
	public boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof CSTNEdge))
			return false;
		if (e == this)
			return true;
		// Use getLabeledValueMap instead of labeledValueSet() to have a better control.
		return (this.getLabeledValueMap().equals(((CSTNEdge) e).getLabeledValueMap()));
	}

	@Override
	public boolean isCSTNEdge() {
		return true;
	}

	/**
	 * 
	 */
	@Override
	public boolean isEmpty() {
		return this.labeledValue.isEmpty();
	}

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a integer.
	 * @return true if the operation was successful, false otherwise.
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

	@Override
	public void mergeLabeledValue(LabeledIntMap map) {
		for (Object2IntMap.Entry<Label> entry : map.entrySet())
			this.mergeLabeledValue(entry.getKey(), entry.getIntValue());
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
	public CSTNEdgePluggable newInstance() {
		return new CSTNEdgePluggable();
	}

	@Override
	public CSTNEdgePluggable newInstance(Edge edge) {
		return new CSTNEdgePluggable(edge);
	}

	@Override
	public CSTNEdgePluggable newInstance(String name1) {
		return new CSTNEdgePluggable(name1);
	}

	@Override
	public boolean putLabeledValue(final Label l, final int i) {
		this.consideredLabeledValue.put(l, i); // once a value has been inserted, it is useless to insert it again in the future.
		return this.labeledValue.put(l, i);
	}

	/**
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the old value if it exists, null otherwise.
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

	@Override
	public void setLabeledValueMap(LabeledIntMap inputLabeledValue) {
		if (inputLabeledValue == null) {
			this.labeledValue.clear();
		} else {
			this.labeledValue = inputLabeledValue;
		}
	}

	@Override
	public int size() {
		return this.labeledValue.size();
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
		if (e instanceof CSTNEdgePluggable) {
			CSTNEdgePluggable e1 = (CSTNEdgePluggable) e;
			this.constraintType = e1.constraintType;
			this.labeledValue = e1.labeledValue;
			this.consideredLabeledValue = e1.consideredLabeledValue;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return Constants.OPEN_TUPLE + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getConstraintType() + "; "
				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
				+ Constants.CLOSE_TUPLE;
	}
}
