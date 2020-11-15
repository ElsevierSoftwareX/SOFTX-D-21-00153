/**
 *
 */
package it.univr.di.cstnu.graph;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * Represents the behavior of a CSTN edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface CSTNEdge extends Edge {
	/**
	 * @return the implementing class to represent labeled values
	 */
	public Class<? extends LabeledIntMap> getLabeledIntMapImplClass();

	/**
	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
	 */
	public LabeledIntMap getLabeledValueMap();

	/**
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet();

	/**
	 * @param setToReuse
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> getLabeledValueSet(ObjectSet<Object2IntMap.Entry<Label>> setToReuse);

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public Entry<Label> getMinLabeledValue();

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinValue();

	/**
	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some;
	 *         {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinValueAmongLabelsWOUnknown();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l);

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value of label l or the minimal value of labels subsumed by <code>l</code> if it exists, null otherwise.
	 */
	public int getMinValueSubsumedBy(final Label l);

	/**
	 * @param label label
	 * @return the value associated to label it it exists, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getValue(final Label label);

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a integer.
	 * @return true if the operation was successful, false otherwise.
	 */
	public boolean mergeLabeledValue(final Label l, final int i);

	/**
	 * @param map a {@link it.univr.di.labeledvalue.LabeledIntMap} object.
	 */
	public void mergeLabeledValue(final LabeledIntMap map);

	/**
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 *
	 * @param i an integer.
	 * @return true if the operation was successful, false otherwise.
	 * @see #mergeLabeledValue(Label, int)
	 * @param ls a {@link java.lang.String} object.
	 */
	public boolean mergeLabeledValue(final String ls, final int i);

	/**
	 * <p>
	 * putLabeledValue.
	 * </p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a int.
	 * @return true if the value has been inserted, false otherwise.
	 */
	public boolean putLabeledValue(final Label l, final int i);

	/**
	 * Remove the value labeled by l from the map. If the 'l' is not present, it does nothing.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the old value if it exists, null otherwise.
	 */
	public int removeLabeledValue(final Label l);

	/**
	 * Uses inputLabeledValue as internal labeled value map.
	 *
	 * @param inputLabeledValue the labeledValue to use
	 */
	public void setLabeledValueMap(final LabeledIntMap inputLabeledValue);

	/**
	 * @return the number of labeled values associated to this edge.
	 */
	public int size();
}
