/**
 *
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Represents the behavior of a CSTNPSU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface CSTNPSUEdge extends BasicCSTNUEdge {
	/**
	 * @return the set of maps of labeled values and labeled lower-case ones.
	 *         The maps of labeled values has ALabel empty.
	 */
	public LabeledALabelIntTreeMap getAllLowerCaseAndLabeledValuesMaps();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param name a {@link ALabel} node name.
	 * @return the labeled lower-Case value object. Use {@link LabeledLowerCaseValue#isEmpty()} to check if it contains or not a significant value.
	 */
	public int getLowerCaseValue(final Label l, final ALabel name);

	/**
	 * @return the Lower-Case labeled Value Map.
	 */
	public LabeledALabelIntTreeMap getLowerCaseValueMap();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link ALabel} node name
	 * @return the value of the removed labeled value
	 */
	public int removeLowerCaseValue(final Label l, final ALabel n);

	/**
	 * @param lowerCaseValue the labeled lower case value to use for initializing the current one.
	 */
	public void setLowerCaseValue(final LabeledALabelIntTreeMap lowerCaseValue);
}
