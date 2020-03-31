/**
 *
 */
package it.univr.di.cstnu.graph;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Represents the behavior of a CSTNU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface BasicCSTNUEdge extends CSTNEdge {
	/**
	 * Clears the labeled lower case values.
	 * For CSTNU there is always only one value, but for some extensions can be more.
	 */
	public void clearLowerCaseValues();

	/**
	 * Clears all upper case labeled values.
	 */
	public void clearUpperCaseValues();

	/**
	 * @return the set of maps of labeled values and labeled upper-case ones.
	 *         The maps of labeled values has ALabel empty.
	 */
	public LabeledALabelIntTreeMap getAllUpperCaseAndLabeledValuesMaps();

	/**
	 * @return the labeled lower-Case value object. Use {@link LabeledLowerCaseValue#isEmpty()} to check if it contains or not a significant value.
	 *         In case that there is more lower-case values (extensions of CSTNU), it returns the minimal one. In there is no lower-case value, it returns an
	 *         empty LabeledLowerCaseValue object.
	 */
	public LabeledLowerCaseValue getLowerCaseValue();

	/**
	 * @return the minimal value (with the ALabel) among all Upper Case Label if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL}
	 *         otherwise.
	 */
	public Object2ObjectMap.Entry<Label, Entry<ALabel>> getMinUpperCaseValue();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param name a {@link ALabel} node name.
	 * @return the value associated to the upper label of the occurrence of node n if it exists, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getUpperCaseValue(final Label l, final ALabel name);

	/**
	 * @return the Upper-Case labeled Value
	 */
	public LabeledALabelIntTreeMap getUpperCaseValueMap();

	/**
	 * @return the representation of all Lower-Case Labels of the edge.
	 */
	public String lowerCaseValuesAsString();

	/**
	 * @return the number of Lower-Case Labels of the edge.
	 */
	public int lowerCaseValueSize();

	/**
	 * Merge a lower label constraint with value <code>i</code> for the node name <code>n</code> with label <code>l</code>.<br>
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node.
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeLowerCaseValue(final Label l, ALabel nodeName, final int i);

	/**
	 * Put a lower label constraint with value <code>i</code> for the node name <code>n</code> with label <code>l</code>.<br>
	 * Putting does not make any label optimization.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node.
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean putLowerCaseValue(final Label l, ALabel nodeName, final int i);

	/**
	 * Merge a upper label constraint with delay i for the node name n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 * If the new value makes other already present values redundant, such values are removed.
	 * If the new value is redundant, it is ignored.
	 * 
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node.
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperCaseValue(final Label l, ALabel nodeName, final int i);

	/**
	 * Put a upper label constraint with delay i for the node name n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.<br>
	 * There is no optimization of the labeled values present after the insertion of this one.
	 * 
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node.
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean putUpperCaseValue(final Label l, ALabel nodeName, final int i);

	/**
	 * Remove the upper label for node name n with label l.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link ALabel} node name
	 * @return the old value
	 */
	public int removeUpperCaseValue(final Label l, final ALabel n);


	/**
	 * @param labeledValue the upper case labeled value map to use for initializing the set
	 */
	public void setUpperCaseValueMap(final LabeledALabelIntTreeMap labeledValue);

	/**
	 * @return the representation of all Upper Case Labels of the edge.
	 */
	public String upperCaseValuesAsString();

	/**
	 * @return the number of Upper Case Labels of the edge.
	 */
	public int upperCaseValueSize();
}
