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
public interface CSTNUEdge extends CSTNEdge {
	/**
	 * Clears the labeled lower case value.
	 */
	public void clearLowerCaseValue();

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
	 * @return the representation of all Lower Case Labels as a string.
	 */
	public String lowerCaseValueAsString();

	/**
	 * @return the number of Upper Case Labels of the edge.
	 */
	public int lowerCaseValueSize();

	/**
	 * Set a upper label constraint with delay i for the node name n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node.
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperCaseValue(final Label l, ALabel nodeName, final int i);

	/**
	 * @return the value of the removed labeled value
	 */
	public int removeLowerCaseValue();

	/**
	 * Remove the upper label for node name n with label l.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link ALabel} node name
	 * @return the old value
	 */
	public int removeUpperCaseValue(final Label l, final ALabel n);

	/**
	 * Set a lower label constraint with delay i for the node n with label l.<br>
	 * If a lower label with label l for node n is already present, it is overwritten.
	 * 
	 * @param l It cannot be null or empty.
	 * @param nodeName the node. It cannot be null.
	 * @param i It cannot be null.
	 */
	public void setLowerCaseValue(final Label l, final ALabel nodeName, final int i);

	/**
	 * @param lowerCaseValue the labeled lower case value to use for initializing the current one.
	 */
	public void setLowerCaseValue(final LabeledLowerCaseValue lowerCaseValue);

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
