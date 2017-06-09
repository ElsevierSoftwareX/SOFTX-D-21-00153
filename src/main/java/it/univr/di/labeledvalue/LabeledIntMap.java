package it.univr.di.labeledvalue;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Declare all methods necessary to manage a set of values (of type int) each labeled by label of type {@link it.univr.di.labeledvalue.Label}.
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
	 * <p>
	 * getMinValue.
	 * </p>
	 *
	 * @return the minimum int value present in the set if the set is not empty; {@link Constants#INT_NULL} otherwise.
	 */
	public int getMinValue();

	/**
	 * <p>
	 * getMinValueAmongLabelsWOUnknown.
	 * </p>
	 *
	 * @return the min value among all labeled value having label without unknown literals.
	 */
	public int getMinValueAmongLabelsWOUnknown();

	/**
	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels consistent with <code>l</code>.
	 *
	 * @param l If it is null, {@link Constants#INT_NULL} is returned.
	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to labels consistent with <code>l</code>. If
	 *         no labels are consistent by <code>l</code>, {@link Constants#INT_NULL} is returned.
	 */
	public int getMinValueConsistentWith(final Label l);

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
	 * Put a label with value <code>i</code> only if label <code>l</code> is not null.
	 * <p>
	 * The labeled value is inserted if there is not a labeled value in the set with label <code>l</code> or it is present but with a value higher than
	 * <code>l</code>.<br>
	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
	 * is lost.
	 *
	 * @param l a not null label.
	 * @param i a not {@link Constants#INT_NULL} value.
	 * @return true if <code>(l,i)</code> has been inserted. Since an insertion can be removed more than one redundant labeled ints, it is nonsensical to return
	 *         "the old value" as expected from a standard put method.
	 */
	public boolean put(Label l, int i);

	/**
	 * Put all elements of inputMap into the current one without making a defensive copy.
	 *
	 * @param inputMap a {@link it.univr.di.labeledvalue.LabeledIntMap} object.
	 * @see Object2IntMap#putAll(Map)
	 */
	public void putAll(final LabeledIntMap inputMap);

	/**
	 * Remove the label <code>l</code> from the map. If the <code>l</code> is not present, it does nothing.
	 *
	 * @param l a not null label.
	 * @return the previous value associated with <code>l</code>, or {@link Constants#INT_NULL} if there was no mapping for <code>l</code>.
	 * @see Map#remove(Object)
	 */
	public int remove(Label l);

	/**
	 * <p>
	 * size.
	 * </p>
	 *
	 * @return the number of labeled value (value with empty label included).
	 * @see Map#size()
	 */
	public int size();

	/**
	 * <p>
	 * values.
	 * </p>
	 *
	 * @return the set of all integer present in the map as an ordered list.
	 */
	public IntSet values();

}
