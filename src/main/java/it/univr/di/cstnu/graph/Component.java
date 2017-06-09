/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Component interface.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface Component extends Serializable, Comparable<Object> {

	/**
	 * {@inheritDoc}
	 *
	 * A component is assumed equal to another if it has the same name.<br>
	 * This is weaker than the original {@link #equals(Object)} semantics.<br>
	 * Since {@link Component} can be memorized in a {@link LabeledIntGraph} using {@link HashMap} and we allow an user to change the name of a component,
	 * {@link #hashCode()} cannot be coherent with {@link #equals(Object)} (otherwise after a renaming a component is not more retrievable).
	 */
	@Override
	@Deprecated
	public boolean equals(final Object o);

	/**
	 * <p>
	 * equalsByName.
	 * </p>
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.Component} object.
	 * @return true if they are the same name.
	 */
	public boolean equalsByName(final Component c);

	/**
	 * <p>
	 * Getter for the field <code>name</code>.
	 * </p>
	 *
	 * @return the name of the component
	 */
	public String getName();

	/**
	 * {@inheritDoc}
	 *
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode();

	/**
	 * Set the name of the component. Cannot be null or empty.
	 *
	 * @param name the not-null not-empty new name
	 * @return the old name
	 */
	public String setName(final String name);

	/**
	 * {@inheritDoc}
	 *
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString();

}