/**
 * 
 */
package it.univr.di.cstnu;

import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author posenato
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
	private static Logger LOG = Logger.getLogger(Edge.class.getName());

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static protected int idSeq = 0;

	/**
	 * Possible name
	 */
	private String name;

	/**
	 * Minimal constructor. the name will be 'c<id>'.
	 */
	Component() {
		this("");
	}

	/**
	 * Constructor to clone the component.
	 * 
	 * @param c the component to clone.
	 */
	Component(Component c) {
		if (c == null) {
			final Component c1 = new Component("");
			name = c1.name;
			return;
		}
		name = c.name;
	}

	/**
	 * Simplified constructor
	 * 
	 * @param n
	 */
	Component(String n) {
		name = ((n == null) || (n.length() == 0)) ? "c" + Component.idSeq++ : n;
	}

	/**
	 * A component is assumed equal to another if it has the same name.<br>
	 * This is weaker than the original {@link #equals(Object)} semantics.<br>
	 * Since {@link Component} is memorized into {@link Graph} using {@link HashMap} and we allow an user to change the name of a component, {@link #hashCode()}
	 * cannot be coherent with {@link #equals(Object)} (otherwise after a renaming a component is not more retrievable).
	 * 
	 * @param o the object to compare
	 * @return true if they are the same name.
	 */
	@Override
	@Deprecated
	public boolean equals(Object o) {
		return super.equals(o);
		// if (o == null || (o.getClass() != this.getClass())) return false;
		// return name.equals(((Component) o).name);
	}

	/**
	 * @param c
	 * @return true if they are the same name.
	 */
	public boolean equalsByName(Component c) {
		if (c == null) return false;
		return name.equals(c.name);
	}

	/**
	 * @return the name of the component
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is is necessary to guarantee that hahsCode agrees with {@link #equals(Object)}<br>
	 * This yields a big issue about the renaming of node or edges. The reason is that Graph class uses hash table to memorize nodes/edges. If an user changes a
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
	public String setName(String name) {
		final String old = this.name;
		if ((name != null) && (name.length() > 0)) this.name = name;
		return old;
	}

	/**
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString() {
		return "〖" + (name.length() == 0 ? "<empty>" : name) + "〗";
	}

	@Override
	public int compareTo(Object o) {
		if (o==null) return 1;
		if (this == o) return 0;
		return this.compareTo((Component) o);
	}
	
	/**
	 * @param c
	 * @return <0 if this has a name coming prior to c.name, 0 if they are equal, >0 otherwise. 
	 */
	public int compareTo(Component c) {
		return this.name.compareToIgnoreCase(c.name);
	}

}
