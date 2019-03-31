package it.univr.di.labeledvalue;

import java.util.Comparator;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Declares all methods necessary to manage a set of values (of type int) each labeled by label of type {@link it.univr.di.labeledvalue.Label}.
 * <p>
 * The semantics of a set of labeled value is defined in the paper “The Dynamic Controllability of Conditional STNs with Uncertainty.” by "Hunsberger, Luke,
 * Roberto Posenato, and Carlo Combi. 2012. http://arxiv.org/abs/1212.2005.
 * <p>
 * All methods managing single labeled value make a defensive copy of the label in order to guarantee that the label insert/get is a copy of the label
 * given/requested.<br>
 * All methods managing bundle of labeled values do not make defensive copy.
 * <p>
 * This interface is specialized for labeled integer.
 * </p>
 *
 * @author Robert Posenato
 * @version $Id: $Id
 */
public interface LabeledIntMap {
	// I do not extend Object2IntMap<Label> because I want to avoid a lot of nonsensical declarations.

	/**
	 * A natural comparator for Entry&lt;Label&gt;.
	 * It orders considering the alphabetical order of Label.
	 */
	static public Comparator<Entry<Label>> entryComparator = (o1, o2) -> {
		if (o1 == o2)
			return 0;
		if (o1 == null)
			return -1;
		if (o2 == null)
			return 1;
		return o1.getKey().compareTo(o2.getKey());
	};

	/**
	 * A read-only view of an object
	 * 
	 * @author posenato
	 */
	public interface LabeledIntMapView extends LabeledIntMap {

		@Override
		default public void clear() {
			return;
		}

		/**
		 * Object Read-only. It does nothing.
		 */
		@Override
		default public boolean put(Label l, int i) {
			return false;
		}

		@Override
		default public void putAll(LabeledIntMap inputMap) {
			return;
		}

		@Override
		default public int remove(Label l) {
			return Constants.INT_NULL;
		}
	}

	/**
	 * @param newLabel
	 * @param newValue
	 * @return true if the current map can represent the value. In positive case, an add of the element does not change the map.
	 *         If returns false, then the adding of the value to the map would modify the map.
	 */
	public boolean alreadyRepresents(Label newLabel, int newValue);

	/**
	 * Remove all entries of the map.
	 *
	 * @see Map#clear()
	 */
	public void clear();

	/**
	 * Public method to enable a Factory class.
	 * 
	 * @return an object of type LabeledIntMap.
	 */
	public LabeledIntMap createLabeledIntMap();

	/**
	 * Public method to enable a Factory class. *
	 * 
	 * @param lim an object to clone.
	 * @return an object of type LabeledIntMap.
	 */
	public LabeledIntMap createLabeledIntMap(LabeledIntMap lim);

	/**
	 * It has the same specification of {@link java.util.Map#entrySet()}.
	 * 
	 * @see java.util.Map#entrySet()
	 * @see ObjectSet
	 * @see it.unimi.dsi.fastutil.objects.Object2IntMap.Entry
	 * @return a set representation of this map.
	 */
	public ObjectSet<Entry<Label>> entrySet();


	/**
	 * It has the same specification of {@link java.util.Map#entrySet()}.
	 * <p>
	 * It accepts setToReuse in order to reuse it (it is an attempt to save memory because on March, 01 2016 I verified that with some instances there occurs
	 * "GC overhead limit exceeded").
	 * 
	 * @param setToReuse
	 * @see java.util.Map#entrySet()
	 * @see ObjectSet
	 * @see it.unimi.dsi.fastutil.objects.Object2IntMap.Entry
	 * @return a set representation of this map.
	 */
	public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse);

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value associated to <code>l</code> if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	public int get(final Label l);

	/**
	 * @return the maximum int value present in the set if the set is not empty; {@link Constants#INT_NULL} otherwise.
	 */
	default public int getMaxValue() {
		if (this.size() == 0)
			return Constants.INT_NULL;
		int max = Constants.INT_NEG_INFINITE;
		for (int value : this.values()) {
			if (max < value)
				max = value;
		}
		return max;
	}

	/**
	 * Returns the value associated to the <code>l</code> if it exists, otherwise the maximal value among all labels consistent with <code>l</code>.
	 *
	 * @param l If it is null, {@link Constants#INT_NULL} is returned.
	 * @return the value associated to the <code>l</code> if it exists or the maximal value among values associated to labels consistent with <code>l</code>. If
	 *         no labels are consistent by <code>l</code>, {@link Constants#INT_NULL} is returned.
	 */
	default public int getMaxValueSubsumedBy(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		int max = this.get(l);
		if (max == Constants.INT_NULL) {
			// the label does not exits, try all consistent labels
			max = Constants.INT_NEG_INFINITE;
			int v1;
			Label l1 = null;
			for (final Entry<Label> e : this.entrySet()) {
				l1 = e.getKey();
				if (l.subsumes(l1)) {
					v1 = e.getIntValue();
					if (max < v1) {
						max = v1;
					}
				}
			}
		}
		return (max == Constants.INT_NEG_INFINITE) ? Constants.INT_NULL : max;
	}


	/**
	 * @return the minimum int value present in the set if the set is not empty; {@link Constants#INT_NULL} otherwise.
	 */
	default public Entry<Label> getMinLabeledValue() {
		if (this.size() == 0)
			return new AbstractObject2IntMap.BasicEntry<>(Label.emptyLabel, Constants.INT_NULL);
		int min = Constants.INT_POS_INFINITE;
		Label label = null;
		for (Entry<Label> entry : this.entrySet()) {
			int value = entry.getIntValue();
			if (min > value) {
				min = value;
				label = entry.getKey();
			}
		}
		return new AbstractObject2IntMap.BasicEntry<>(label, min);
	}

	/**
	 * @return the minimum int value present in the set if the set is not empty; {@link Constants#INT_NULL} otherwise.
	 */
	default public int getMinValue() {
		if (this.size() == 0)
			return Constants.INT_NULL;
		int min = Constants.INT_POS_INFINITE;

		for (int value : this.values()) {
			if (min > value)
				min = value;
		}
		return min;
	}

	/**
	 * @return the min value among all labeled value having label without unknown literals.
	 */
	default public int getMinValueAmongLabelsWOUnknown() {
		int v = Constants.INT_POS_INFINITE, i;
		Label l;
		for (final Entry<Label> entry : this.entrySet()) {
			l = entry.getKey();
			if (l.containsUnknown()) {
				continue;
			}
			i = entry.getIntValue();
			if (v > i) {
				v = i;
			}
		}
		return (v == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : v;
	}

	/**
	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels consistent with <code>l</code>.
	 *
	 * @param l If it is null, {@link Constants#INT_NULL} is returned.
	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to labels consistent with <code>l</code>. If
	 *         no labels are consistent by <code>l</code>, {@link Constants#INT_NULL} is returned.
	 */
	default public int getMinValueConsistentWith(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		int min = this.get(l);
		if (min == Constants.INT_NULL) {
			// the label does not exits, try all consistent labels
			min = Constants.INT_POS_INFINITE;
			int v1;
			Label l1 = null;
			for (final Entry<Label> e : this.entrySet()) {
				l1 = e.getKey();
				if (l.isConsistentWith(l1)) {
					v1 = e.getIntValue();
					if (min > v1) {
						min = v1;
					}
				}
			}
		}
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	
	/**
	 * Returns the minimal value among those associated to labels subsumed by <code>l</code> if it exists, {@link Constants#INT_NULL} otherwise. 
	 *
	 * @param l If it is null, {@link Constants#INT_NULL} is returned.
	 * @return minimal value among those associated to labels subsumed by <code>l</code> if it exists, {@link Constants#INT_NULL} otherwise. 
	 */
	public int getMinValueSubsumedBy(final Label l);
		
	/**
	 * @return true if the map has no elements.
	 */
	public boolean isEmpty();

	/**
	 * @return the set view of all labels in the map.
	 */
	public ObjectSet<Label> keySet();

	/**
	 * <p>
	 * It accepts setToReuse in order to reuse it (it is an attempt to save memory because on March, 01 2016 I verified that with some instances there occurs
	 * "GC overhead limit exceeded").
	 * 
	 * @param setToReuse
	 * @return the set view of all labels in the map.
	 */
	public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse);

	/**
	 * Put a label with value <code>i</code> if label <code>l</code> is not null and there is not a labeled value in the set with label <code>l</code> or it is
	 * present but with a value higher than <code>l</code>.
	 * <p>
	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
	 * is lost.
	 *
	 * @param l a not null label.
	 * @param i a not {@link Constants#INT_NULL} value.
	 * @return true if <code>(l,i)</code> has been inserted. Since an insertion can remove more than one redundant labeled values, it is nonsensical to return
	 *         "the old value" as expected from a classical put method.
	 */
	public boolean put(Label l, int i);

	/**
	 * Put all elements of inputMap into the current one without making a defensive copy.
	 *
	 * @param inputMap a {@link it.univr.di.labeledvalue.LabeledIntMap} object.
	 * @see Object2IntMap#putAll(Map)
	 */
	default public void putAll(final LabeledIntMap inputMap) {
		if (inputMap == null)
			return;
		for (final Entry<Label> entry : inputMap.entrySet()) {
			this.put(entry.getKey(), entry.getIntValue());
		}
	}

	/**
	 * Remove the label <code>l</code> from the map. If the <code>l</code> is not present, it does nothing.
	 *
	 * @param l a not null label.
	 * @return the previous value associated with <code>l</code>, or {@link Constants#INT_NULL} if there was no mapping for <code>l</code>.
	 * @see Map#remove(Object)
	 */
	public int remove(Label l);

	/**
	 * @return the number of labeled value (value with empty label included).
	 * @see Map#size()
	 */
	public int size();

	/**
	 * @return the set of all integer present in the map as an ordered list.
	 */
	public IntSet values();

	/**
	 * @return a read-only view of this.
	 */
	public LabeledIntMapView unmodifiable();

}
