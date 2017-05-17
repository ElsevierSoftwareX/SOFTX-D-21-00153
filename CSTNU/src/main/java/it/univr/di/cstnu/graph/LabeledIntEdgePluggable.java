/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.NotImplementedException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * It contains all information of a CSTPU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
@SuppressWarnings("unused")
public class LabeledIntEdgePluggable extends AbstractLabeledIntEdge implements LabeledIntEdge {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static final Logger LOG = Logger.getLogger(LabeledIntEdgePluggable.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Labeled value.
	 */
	private LabeledIntMap labeledValue;

	/**
	 * Factory for labeled value set.
	 */
	LabeledIntMapFactory<? extends LabeledIntMap> labeledValueMapFactory;

	/**
	 * @param labeledIntMapImplementation
	 */
	public <C extends LabeledIntMap> LabeledIntEdgePluggable(Class<C> labeledIntMapImplementation) {
		super();
		this.labeledValueMapFactory = new LabeledIntMapFactory<>(labeledIntMapImplementation);
		this.labeledValue = this.labeledValueMapFactory.create();
	}

	/**
	 * A simple constructor cloner. The internal labeled int value map is implemented as in e if it is not null, LabeledIntTreeMap otherwise.
	 *
	 * @param e edge to clone. If null, an empty edge is created with type = normal.
	 */
	@SuppressWarnings("unchecked")
	public <C extends LabeledIntMap> LabeledIntEdgePluggable(final LabeledIntEdge e) {
		this(e, ((Class<C>) ((e == null) ? LabeledIntTreeMap.class : e.getLabeledIntValueMapFactory().getReturnedObjectClass())));
	}

	/**
	 * A simple constructor cloner.
	 *
	 * @param e edge to clone. If null, an empty edge is created with type = normal.
	 * @param labeledIntMapImplementation
	 */
	public <C extends LabeledIntMap> LabeledIntEdgePluggable(final LabeledIntEdge e, Class<C> labeledIntMapImplementation) {
		super(e);
		this.labeledValueMapFactory = new LabeledIntMapFactory<>(labeledIntMapImplementation);
		if (e != null) {
			this.labeledValue = this.labeledValueMapFactory.create(e.getLabeledValueMap());
		} else {
			this.labeledValue = this.labeledValueMapFactory.create();
		}
	}

	/**
	 * Constructor for LabeledIntEdge.
	 *
	 * @param n a {@link java.lang.String} object.
	 * @param labeledIntMapImplementation
	 */
	public <C extends LabeledIntMap> LabeledIntEdgePluggable(final String n, Class<C> labeledIntMapImplementation) {
		super(n);
		this.labeledValueMapFactory = new LabeledIntMapFactory<>(labeledIntMapImplementation);
		this.labeledValue = this.labeledValueMapFactory.create();
	}

	/**
	 * Default constructor: empty name, derived type and no labeled value.
	 *
	 * @param name a {@link java.lang.String} object.
	 * @param t the type of the edge
	 * @param labeledIntMapImplementation
	 */
	public <C extends LabeledIntMap> LabeledIntEdgePluggable(final String name, final ConstraintType t, Class<C> labeledIntMapImplementation) {
		this(name, labeledIntMapImplementation);
		this.setConstraintType(t);
	}

	/**
	 * Clear (remove) all labeled values associated to this edge.
	 */
	public void clear() {
		super.clear();
		this.labeledValue.clear();
	}

	/**
	 * Clears all ordinary labeled values.
	 */
	public void clearLabels() {
		this.removedLabeledValue.clear();
		this.labeledValue.clear();
	}

	@Override
	public LabeledIntEdge createLabeledIntEdge() {
		throw new NotImplementedException("This class needs to know which labeled int map implementation to use.");
	}

	/**
	 * @param labeledIntMapImplementation
	 * @return a new edge
	 * @see LabeledIntEdgePluggable#LabeledIntEdgePluggable(LabeledIntEdge)
	 */
	@SuppressWarnings("static-method")
	public <C extends LabeledIntMap> LabeledIntEdge createLabeledIntEdge(Class<C> labeledIntMapImplementation) {
		return new LabeledIntEdgePluggable(labeledIntMapImplementation);
	}

	/**
	 * @see LabeledIntEdgePluggable#LabeledIntEdgePluggable(LabeledIntEdge)
	 */
	@Override
	public LabeledIntEdge createLabeledIntEdge(LabeledIntEdge e) {
		return new LabeledIntEdgePluggable(e);
	}

	/**
	 * @param e
	 * @param labeledIntMapImplementation
	 * @return a new edge copy of input edge e.
	 */
	@SuppressWarnings("static-method")
	public <C extends LabeledIntMap> LabeledIntEdge createLabeledIntEdge(LabeledIntEdge e, Class<C> labeledIntMapImplementation) {
		return new LabeledIntEdgePluggable(e, labeledIntMapImplementation);
	}

	/**
	 * @param name1
	 * @param labeledIntMapImplementation
	 * @return a new empty edge with name name.
	 */
	@SuppressWarnings("static-method")
	public <C extends LabeledIntMap> LabeledIntEdge createLabeledIntEdge(String name1, Class<C> labeledIntMapImplementation) {
		return new LabeledIntEdgePluggable(name1, labeledIntMapImplementation);
	}

	/**
	 * <p>
	 * getLabeledValueMap.
	 * </p>
	 *
	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
	 */
	public LabeledIntMap getLabeledValueMap() {
		return this.labeledValue;
	}

	/**
	 * <p>
	 * getMinValue.
	 * </p>
	 *
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link Constants#INT_NULL} otherwise.
	 */
	public int getMinValue() {
		return this.labeledValue.getMinValue();
	}

	/**
	 * <p>
	 * getMinValueAmongLabelsWOUnknown.
	 * </p>
	 *
	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some; {@link Constants#INT_NULL}
	 *         otherwise.
	 */
	public int getMinValueAmongLabelsWOUnknown() {
		return this.labeledValue.getMinValueAmongLabelsWOUnknown();
	}

	/**
	 * <p>
	 * getMinValueConsistentWith.
	 * </p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l) {
		return this.labeledValue.getMinValueConsistentWith(l);
	}

	/**
	 * <p>
	 * getMinValueConsistentWith.
	 * </p>
	 *
	 * @param l the scenario label
	 * @param upperL the Upper Label
	 * @return the value associated to the upper label of the occurrence of node n if it exists or the minimal values among the labels subsumed by l if one
	 *         exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l, final ALabel upperL) {
		return this.upperLabel.getMinValueConsistentWith(l, upperL);
	}

	/**
	 * @param label label
	 * @return the value associated to label it it exists, {@link Constants#INT_NULL} otherwise.
	 */
	public int getValue(final Label label) {
		return this.labeledValue.get(label);
	}

	/**
	 * 
	 */
	@Override
	public boolean isEmpty() {
		return this.labeledValue.size() == 0 && this.lowerLabel.size() == 0 && this.upperLabel.size() == 0;
	}

	/**
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet() {
		return this.labeledValue.entrySet();
	}

	/**
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet(ObjectSet<Object2IntMap.Entry<Label>> setToReuse) {
		return this.labeledValue.entrySet(setToReuse);
	}

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a integer.
	 * @return true if the operation was successful, false otherwise.
	 */
	public boolean mergeLabeledValue(final Label l, final int i) {
		if ((l == null) || (i == Constants.INT_NULL))
			return false;
		final int oldValue = this.removedLabeledValue.getInt(l);
		if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
			// The new value is greater or equal the old one, the new value can be ignored.
			// the labeled value (l,i) was already removed by label modification rule. So, it will be not stored.
			if (LOG.isLoggable(Level.FINEST))
				LOG.log(Level.FINEST, "The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", "
						+ oldValue + ") is in the removed list");
			return false;
		}
		this.removedLabeledValue.put(l, i); // once a value has been inserted, it is useless to insert it again
		boolean added = this.labeledValue.put(l, i);
		if (added) {// I try to clean UPPER values
			ObjectSet<Entry<java.util.Map.Entry<Label, ALabel>>> upperLabelValueSet = this.getUpperLabelSet();
			if (upperLabelValueSet.size() > 0) {
				for (Entry<java.util.Map.Entry<Label, ALabel>> entry : upperLabelValueSet) {
					Label label = entry.getKey().getKey();
					int value = entry.getIntValue();
					if (label.subsumes(l) && value >= i) {
						ALabel alabel = entry.getKey().getValue();
						this.putUpperLabeledValueToRemovedList(label, alabel, value);
						this.removeUpperLabel(label, alabel);
					}
				}
			}
		}
		return added;
	}

	// /**
	// * <b>This method is defined for making easier the management of nodeSet for some kinds of internal labeled value maps.</b>
	// * Merges the labeled value i to the set of labeled values of this edge.
	// * [2017-04-07] Posenato<br>
	// * NodeSet are not more necessary thanks to EqLP+ rule!<br>
	// *
	// * @param l a {@link it.univr.di.labeledvalue.Label} object.
	// * @param i an integer.
	// * @param s the node set to add.
	// * @return true if the operation was successful, false otherwise.
	// */
	// @Override
	// public boolean mergeLabeledValue(final Label l, final int i, ObjectSet<String> s) {
	// if ((l == null) || (i == Constants.INT_NULL)) || this.labeledValueMapFactory.getReturnedObjectClass() != LabeledIntNodeSetTreeMap.class)
	// return false;
	// final int oldValue = this.removedLabeledValue.getInt(l);
	// if ((oldValue != Constants.INT_NULL) && (i >= oldValue)) {
	// // The new value is greater or equal the old one, the new value can be ignored.
	// // the labeled value (l,i) was already removed by label modification rule. So, it will be not stored.
	// if (LOG.isLoggable(Level.FINEST))
	// LOG.log(Level.FINEST, "The labeled value (" + l + ", " + i + ") will be not stored because the labeled value (" + l + ", "
	// + oldValue + ") is in the removed list");
	// return false;
	// }
	// boolean exit = ((LabeledIntNodeSetTreeMap) this.labeledValue).put(l, i, s);
	// return exit;
	// }

	// /**
	// * This method is defined for making easier the management of nodeSet for some kinds of internal labeled value maps.
	// *
	// * [2017-04-07] Posenato<br>
	// * NodeSet are not more necessary thanks to EqLP+ rule!<br>
	// *
	// * param label label
	// * return the node set associated to label it it exists, null otherwise.
	// */
	// // public SortedSet<String> getNodeSet(final Label label) {
	// // return ((LabeledIntNodeSetTreeMap) this.labeledValue).getNodeSet(label);
	// // }

	@Override
	public void mergeLabeledValue(LabeledIntMap map) {
		for (Object2IntMap.Entry<Label> entry : map.entrySet())
			this.labeledValue.put(entry.getKey(), entry.getIntValue());
	}

	@Override
	public boolean putLabeledValue(final Label l, final int i) {
		return this.labeledValue.put(l, i);
	}

	/**
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the old value if it exists, null otherwise.
	 */
	public int removeLabel(final Label l) {
		LabeledIntEdgePluggable.LOG.finer("Removing label '" + l + "' from the edge " + this.toString());
		return this.labeledValue.remove(l);
	}

	@Override
	public void setLabeledValue(final LabeledIntMap labeledValue) {
		LabeledIntMap map = this.labeledValueMapFactory.create();
		for (Object2IntMap.Entry<Label> entry : labeledValue.entrySet()) {
			map.put(entry.getKey(), entry.getIntValue());
		}
		this.labeledValue = map;
	}

	@Override
	public int size() {
		return this.labeledValue.size();
	}

	/**
	 * A copy by reference of internal structure of edge e.
	 *
	 * @param e edge to clone. If null, it returns doing nothing.
	 */
	public void takeIn(final LabeledIntEdgePluggable e) {
		if (e == null)
			return;
		this.constraintType = e.constraintType;
		this.labeledValue = e.labeledValue;
		this.upperLabel = e.upperLabel;
		this.lowerLabel = e.lowerLabel;
		this.removedLabeledValue = e.removedLabeledValue;
		this.removedUpperLabel = e.removedUpperLabel;
	}

	/**
	 * {@inheritDoc} Return a string representation of labeled values.
	 */
	@Override
	public String toString() {
		return "❮" + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getConstraintType() + "; "
				+ ((this.labeledValue.size() > 0) ? this.labeledValue.toString() + "; " : "")
				+ ((this.upperLabel.size() > 0) ? "UL: " + this.upperLabel.toString() + "; " : "")
				+ ((this.lowerLabel.size() > 0) ? "LL: " + this.lowerLabel.toString(true) + ";" : "")
				+ "❯";
	}

	@Override
	public LabeledIntMapFactory<? extends LabeledIntMap> getLabeledIntValueMapFactory() {
		return this.labeledValueMapFactory;
	}

	/**
	 * Set the name of the edge. Cannot be null or empty.
	 *
	 * @param name the not-null not-empty new name
	 * @return the old name
	 */
	@Override
	public String setName(final String name) {
		final String old = this.name;
		if ((name != null) && (name.length() > 0)) {
			this.name = name;
			this.setChanged();
			notifyObservers("Name:" + old);
		}
		return old;
	}

}
