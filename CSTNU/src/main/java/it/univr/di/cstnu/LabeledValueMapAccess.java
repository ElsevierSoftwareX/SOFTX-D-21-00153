/**
 * 
 */
package it.univr.di.cstnu;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Declare all methods necessary to manage a set of values (of type I) labeled by label of type Label.<br>
 * All methods managing single labeled value make a defensive copy of the label in order to guarantee that the label insert/get is a copy of the label
 * given/requested.<br>
 * All methods managing bundle of labeled values do not make defensive copy.
 * 
 * @author posenato
 * @param <I>
 */
public interface LabeledValueMapAccess<I extends Number & Comparable<I>> {

	/**
	 * @param l
	 * @return the value associated to label L, if it exists, null otherwise.
	 */
	public I getValue(Label l);

	/**
	 * @return the map itself.
	 */
	public Map<Label, I> labeledValueMap();

	/**
	 * @return the map as a ordered set.
	 */
	public Set<Entry<Label, I>> labeledValueSet();

	/**
	 * Merge a label with value i only if label l is not null. The labeled value is insert if there is not a labeled value in the set with label l or it is
	 * present with a value higher than i. The method can remove or modify other labeled values of the set in order to minimize the labeled values present
	 * guaranteeing that no info is lost.
	 * 
	 * @param l
	 * @param i
	 * @return true if the labeled value has been insert. False if it is already present.
	 */
	public boolean mergeLabeledValue(Label l, I i);

	/**
	 * Put a label with value i only if label is not null overwriting possibly value with the same value already present.
	 * 
	 * @param l
	 * @param i
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously
	 *         associated null with key.)
	 */
	public I putLabeledValue(Label l, I i);

	/**
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 * 
	 * @param l
	 * @return the previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously
	 *         associated null with key.)
	 */
	public I remove(Label l);

	/**
	 * Clear (remove) all entries of the map.
	 */
	public void clear();

	/**
	 * @return the number of labeled value (value with empty label included).
	 */
	public int size();

	/**
	 * @return all the values of the map as an array of Integer.
	public Integer[] valueSet();
	 */

}
