/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * <p>Component class.</p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class Component implements Serializable, Comparable<Object> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(LabeledIntEdge.class.getName());

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static protected int idSeq = 0;

	/**
	 * Possible name
	 */
	private String name;

	/**
	 * Minimal constructor. the name will be 'c&lt;id&gt;'.
	 */
	Component() {
		this("");
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param c the component to clone.
	 */
	Component(final Component c) {
		if (c == null) {
			this.name = "";
			return;
		}
		this.name = c.name;
	}

	/**
	 * Simplified constructor
	 *
	 * @param n
	 */
	Component(final String n) {
		this.name = ((n == null) || (n.length() == 0)) ? "c" + Component.idSeq++ : n;
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.Component} object.
	 * @return &lt;0 if this has a name coming prior to c.name, 0 if they are equal, &gt;0 otherwise.
	 */
	public int compareTo(final Component c) {
		return this.name.compareToIgnoreCase(c.name);
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Object o) {
		if (o == null) return 1;
		if (this == o) return 0;
		return this.compareTo((Component) o);
	}

	/**
	 * {@inheritDoc}
	 *
	 * A component is assumed equal to another if it has the same name.<br>
	 * This is weaker than the original {@link #equals(Object)} semantics.<br>
	 * Since {@link Component} is memorized into {@link LabeledIntGraph} using {@link HashMap} and we allow an user to change the name of a component,
	 * {@link #hashCode()} cannot be coherent with {@link #equals(Object)} (otherwise after a renaming a component is not more retrievable).
	 */
	@Override
	@Deprecated
	public boolean equals(final Object o) {
		return super.equals(o);
		// if (o == null || (o.getClass() != this.getClass())) return false;
		// return name.equals(((Component) o).name);
	}

	/**
	 * <p>equalsByName.</p>
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.Component} object.
	 * @return true if they are the same name.
	 */
	public boolean equalsByName(final Component c) {
		if (c == null) return false;
		return this.name.equals(c.name);
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return the name of the component
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Is is necessary to guarantee that hahsCode agrees with {@link #equals(Object)}<br>
	 * This yields a big issue about the renaming of node or edges. The reason is that LabeledIntGraph class uses hash table to memorize nodes/edges. If an user
	 * changes a
	 * name to an already defined node/edge, with an hashcode coherent to equals such node/edge cannot be retrieved any more.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * Set the name of the component. Cannot be null or empty.
	 *
	 * @param name the not-null not-empty new name
	 * @return the old name
	 */
	public String setName(final String name) {
		final String old = this.name;
		if ((name != null) && (name.length() > 0)) {
			this.name = name;
		}
		return old;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString() {
		return "〖" + (this.name.length() == 0 ? "<empty>" : this.name) + "〗";
	}

}
