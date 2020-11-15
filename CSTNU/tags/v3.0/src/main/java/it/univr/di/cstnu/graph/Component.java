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
	 * Color of the component. Used in some algorithms for fixing the state of an edge/node.
	 */
	static enum Color {
		black, gray, white
	}

	/**
	 * In general, we assume that a component is equal to another if it has the same name and that a user can modify a name even after the creation of the
	 * component.
	 * Clearly, if there is a set of components, it is responsibility of the user/software to allow a change only if there is no conflict with the names of
	 * other components.
	 * <br>
	 * On the other hand, we assume also that a component can be can be memorized in a structure like {@link HashMap}, where {@link #hashCode()} is
	 * used for addressing elements. {@link #hashCode()} needs to identify an object even after its renaming and it must be coherent with
	 * {@link #equals(Object)}.
	 * Therefore, this method and {@link #hashCode()} must not be modified.
	 */
	@Override
	@Deprecated
	public boolean equals(final Object o);

	/**
	 * A component is assumed to be equal to another if it has the same name.
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.Component} object.
	 * @return true if this component has the same name of c.
	 * @see #equals(Object)
	 */
	public boolean equalsByName(final Component c);

	/**
	 * @return the color of the edge
	 */
	public Color getColor();

	/**
	 * Getter for the field <code>name</code>.
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
	 * set color
	 * 
	 * @param c the new color
	 */
	public void setColor(Color c);

	/**
	 * Set the name of the component. Cannot be null or empty.
	 *
	 * @param name the not-null not-empty new name
	 * @return the old name
	 */
	public String setName(final String name);

	/**
	 * {@inheritDoc}
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString();

	/**
	 * Clear all component but name.
	 */
	public void clear();
}