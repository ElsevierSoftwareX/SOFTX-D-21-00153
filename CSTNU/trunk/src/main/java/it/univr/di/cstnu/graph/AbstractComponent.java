// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.Observable;

/**
 * @author posenato
 * @version $Id: $Id
 */
public abstract class AbstractComponent extends Observable implements Component {

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static protected int idSeq = 0;

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Possible name
	 */
	protected String name;

	/**
	 * Possible color
	 */
	Color color;

	/**
	 * Minimal constructor. the name will be 'c&lt;id&gt;'.
	 */
	protected AbstractComponent() {
		this("");
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param c the component to clone.
	 */
	protected AbstractComponent(final Component c) {
		if (c == null) {
			this.name = "";
			return;
		}
		this.name = c.getName();
		this.color = c.getColor();
	}

	/**
	 * Simplified constructor
	 *
	 * @param n
	 */
	protected AbstractComponent(final String n) {
		this.name = ((n == null) || (n.length() == 0)) ? "c" + AbstractComponent.idSeq++ : n;
		this.color = null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @return &lt;0 if this has a name coming prior to c.name, 0 if they are equal, &gt;0 otherwise.
	 */
	@Override
	public int compareTo(final Object o) {
		if (o == null)
			return 1;
		if (this == o)
			return 0;
		return this.name.compareToIgnoreCase(((Component) o).getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equalsByName(final Component c) {
		if (c == null)
			return false;
		return this.name.equals(c.getName());
	}

	/**
	 * <p>
	 * Getter for the field <code>name</code>.
	 * </p>
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
	 * Set the name of the edge. Cannot be null or empty.
	 *
	 * @param inputName the not-null not-empty new name
	 * @return the old name
	 */
	@Override
	public String setName(final String inputName) {
		final String old = this.name;
		if ((inputName != null) && (inputName.length() > 0)) {
			this.name = inputName;
			this.setChanged();
			notifyObservers("Name:" + old);
		}
		return old;
	}

	/**
	 * {@inheritDoc}
	 * Return a string representation of labeled values.
	 */
	@Override
	public String toString() {
		return "〖" + (this.name.length() == 0 ? "<empty>" : this.name) + "〗";
	}

	@Override
	public void setColor(Color c) {
		this.color = c;
	}

	@Override
	public Color getColor() {
		return this.color;
	}

	@Override
	public void clear() {
		this.color = null;
	}

	public void takeIn(Component c) {
		this.color = c.getColor();
		this.setName(c.getName());
	}
}
