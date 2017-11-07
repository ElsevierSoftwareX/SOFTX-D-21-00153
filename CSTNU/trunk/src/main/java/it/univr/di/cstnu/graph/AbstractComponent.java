/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.Observable;

/**
 *
 * @author posenato
 * @version $Id: $Id
 */
public abstract class AbstractComponent extends Observable implements Component  {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static protected int idSeq = 0;

	/**
	 * Possible name
	 */
	String name;

	/**
	 * Minimal constructor. the name will be 'c&lt;id&gt;'.
	 */
	AbstractComponent() {
		this("");
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param c the component to clone.
	 */
	AbstractComponent(final Component c) {
		if (c == null) {
			this.name = "";
			return;
		}
		this.name = c.getName();
	}

	/**
	 * Simplified constructor
	 *
	 * @param n
	 */
	AbstractComponent(final String n) {
		this.name = ((n == null) || (n.length() == 0)) ? "c" + AbstractComponent.idSeq++ : n;
	}

	/**
	 * <p>compareTo.</p>
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.AbstractComponent} object.
	 * @return &lt;0 if this has a name coming prior to c.name, 0 if they are equal, &gt;0 otherwise.
	 */
	public int compareTo(final Component c) {
		return this.name.compareToIgnoreCase(c.getName());
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
	 */
	@Override
	@Deprecated
	public boolean equals(final Object o) {
		return super.equals(o);
		// if (o == null || (o.getClass() != this.getClass())) return false;
		// return name.equals(((AbstractComponent) o).name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equalsByName(final Component c) {
		if (c == null) return false;
		return this.name.equals(c.getName());
	}

	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return the name of the component
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
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
