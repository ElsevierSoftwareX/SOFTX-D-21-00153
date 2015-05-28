package it.univr.di.labeledvalue;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Declare all methods necessary to manage a set of values (of type int) each labeled by label of type {@link it.univr.di.labeledvalue.Label}.
 * Each value is also associated to a set of nodes that represent the internal nodes of any path that has determined the value.
 * <p>
 * The semantics of a set of labeled value is defined in the paper “The Dynamic Controllability of Conditional STNs with Uncertainty.” by "Hunsberger, Luke,
 * Roberto Posenato, and Carlo Combi. 2012. http://arxiv.org/abs/1212.2005. //FIXME Mettere ultima referenza.
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
public interface LabeledIntNodeSetMap {// I do not extend Object2IntMap<Label> because I want to avoid a lot of nonsensical declarations.


	/**
	 * Default value to represent a no valid int value. It is necessary in the type oriented implementation of Map(Label,int).
	 * It has to be different to the value {@link Constants#INT_POS_INFINITE}, used to represent an edge with a no bound labeled value.
	 */
	static public final int INT_NULL = Constants.INT_NULL;

	/**
	 * Remove all entries of the map.
	 *
	 * @see Map#clear()
	 */
	public void clear();

	/**
	 * It has the same specification of {@link java.util.Map#entrySet()}.
	 * Therefore, the set is backed by the map: changes to the map are reflected in the set, and vice-versa.
	 * <p>
	 * The only different thing from {@link java.util.Map#entrySet()} is the type of returned entries.
	 *
	 * @see java.util.Map#entrySet()
	 * @return a set representation of this map.
	 */
	public Set<Entry<Label>> entrySet();

	/**
	 * <p>get.</p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the pair value and node set associated to label l if it exists, null otherwise.
	 */
	public ValueNodeSetPair get(Label l);

	/**
	 * <p>getMinValue.</p>
	 *
	 * @return the minimum int value present in the set if the set is not empty; {@link #INT_NULL} otherwise.
	 */
	public int getMinValue();

	/**
	 * <p>getMinValueAmongLabelsWOUnknown.</p>
	 *
	 * @return the min value among all labeled value having label without unknown literals.
	 */
	public int getMinValueAmongLabelsWOUnknown();

	/**
	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels
	 * consistent with <code>l</code>.
	 *
	 * @param l If it is null, {@link #INT_NULL} is returned.
	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to
	 *         labels consistent with <code>l</code>. If no labels are consistent by <code>l</code>, {@link #INT_NULL} is returned.
	 */
	public int getMinValueConsistentWith(final Label l);

	/**
	 * <p>getNodeSet.</p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the node set associated to label L if it exists; null otherwise.
	 */
	public SortedSet<String> getNodeSet(Label l);

	/**
	 * <p>getValue.</p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value associated to label L if it exists; {@link #INT_NULL} otherwise.
	 * @see Object2IntMap#getInt(Object)
	 */
	public int getValue(Label l);

	/**
	 * It has the same specification of {@link java.util.Map#entrySet()}.
	 * Therefore, the set is backed by the map: changes to the map are reflected in the set, and vice-versa.
	 * <p>
	 * The only different thing from {@link java.util.Map#entrySet()} is the type of returned entries.
	 *
	 * @see java.util.Map#entrySet()
	 * @return a set representation of this map.
	 */
	public Set<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, ValueNodeSetPair>> object2ObjectEntrySet();

	/**
	 * Put a label with value <code>i</code> only if label <code>l</code> is not null.
	 * <p>
	 * The labeled value is inserted if there is not a labeled value in the set with label <code>l</code> or it is present but with a value higher than
	 * <code>l</code>.<br>
	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
	 * is lost.
	 *
	 * @param l a not null label.
	 * @param i a not {@link #INT_NULL} value.
	 * @return true if <code>(l,i)</code> has been inserted. Since an insertion can be removed more than one redundant labeled ints, it is nonsensical to return
	 *         "the old value" as expected from a standard put method.
	 */
	public boolean put(Label l, int i);

	/**
	 * Put a label with value <code>i</code> only if label <code>l</code> is not null.
	 * <p>
	 * The labeled value is inserted if there is not a labeled value in the set with label <code>l</code> or it is present but with a value higher than
	 * <code>l</code>.<br>
	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
	 * is lost.
	 *
	 * @param l a not null label.
	 * @param i a not {@link #INT_NULL} value.
	 * @param nodeNameSet a {@link java.util.Set} object.
	 * @return true if <code>(l,i)</code> has been inserted. Since an insertion can be removed more than one redundant labeled ints, it is nonsensical to return
	 *         "the old value" as expected from a standard put method.
	 */
	public boolean put(Label l, int i, Set<String> nodeNameSet);

	/**
	 * Put all elements of inputMap into the current one without making a defensive copy.
	 *
	 * @param inputMap a {@link it.univr.di.labeledvalue.LabeledIntNodeSetMap} object.
	 * @see Object2IntMap#putAll(Map)
	 */
	public void putAll(final LabeledIntNodeSetMap inputMap);

	/**
	 * Put a label <code>l</code> with value <code>i</code> only if <code>l</code> is not null, overwriting a possibly already present value with the same
	 * label.
	 * Be careful! This method can destroy the minimality of represented labeled values. If you are in doubt, use {@link #put(Label, int)}.
	 * This method is useful only when there is no label optimization and one wants to collect all labels without any sanity check, that is always made in
	 * {@link #put(Label, int)}.
	 *
	 * @param l a not null label.
	 * @param i a not {@link #INT_NULL} value.
	 * @return the previous int associated to l if it exists, {@link #INT_NULL} otherwise.
	 */
	public int putForcibly(Label l, int i);

	/**
	 * Put a label <code>l</code> with value <code>i</code> only if <code>l</code> is not null, overwriting a possibly already present value with the same
	 * label.
	 * Be careful! This method can destroy the minimality of represented labeled values. If you are in doubt, use {@link #putForcibly(Label, int, Set)}.
	 * This method is useful only when there is no label optimization and one wants to collect all labels without any sanity check, that is always made in
	 * {@link #put(Label, int, Set)}.
	 *
	 * @param l a not null label.
	 * @param i a not {@link #INT_NULL} value.
	 * @param nodeNameSet a {@link java.util.Set} object.
	 * @return the previous int associated to l if it exists, {@link #INT_NULL} otherwise.
	 */
	public int putForcibly(Label l, int i, Set<String> nodeNameSet);

	/**
	 * Remove the label <code>l</code> from the map. If the <code>l</code> is not present, it does nothing.
	 *
	 * @param l a not null label.
	 * @return the previous value associated with <code>l</code>, or {@link #INT_NULL} if there was no mapping for <code>l</code>.
	 * @see Map#remove(Object)
	 */
	public int remove(Label l);

	/**
	 * <p>size.</p>
	 *
	 * @return the number of labeled value (value with empty label included).
	 * @see Map#size()
	 */
	public int size();

	/**
	 * <p>values.</p>
	 *
	 * @return the set of all integer present in the map.
	 */
	public IntCollection values();
	
	/**
	 * <p>keys.</p>
	 *
	 * @return the set view of all labels in the map.
	 */
	public Set<Label> keys();
}
