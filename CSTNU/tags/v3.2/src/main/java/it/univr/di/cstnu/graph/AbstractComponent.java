// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.Observable;

/**
 * <p>
 * Abstract AbstractComponent class.
 * </p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public abstract class AbstractComponent extends Observable implements Component {

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static int idSeq = 0;

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
	 * @param n a {@link java.lang.String} object.
	 */
	protected AbstractComponent(final String n) {
		this.name = ((n == null) || (n.length() == 0)) ? "c" + AbstractComponent.idSeq++ : n;
		this.color = null;
	}

	/** {@inheritDoc} */
	@Override
	public int compareTo(final Object o) {
		if (o == null)
			return 1;
		if (this == o)
			return 0;
		return this.name.compareToIgnoreCase(((Component) o).getName());
	}

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public boolean equals(final Object o) {
		return super.equals(o);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equalsByName(final Component c) {
		if (c == null)
			return false;
		return this.name.equals(c.getName());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Getter for the field <code>name</code>.
	 * </p>
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/**
	 * {@inheritDoc}
	 * Set the name of the edge. Cannot be null or empty.
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

	/** {@inheritDoc} */
	@Override
	public void setColor(Color c) {
		this.color = c;
	}

	/** {@inheritDoc} */
	@Override
	public Color getColor() {
		return this.color;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.color = null;
	}

	/**
	 * <p>
	 * takeIn.
	 * </p>
	 *
	 * @param c a {@link it.univr.di.cstnu.graph.Component} object.
	 */
	public void takeIn(Component c) {
		this.color = c.getColor();
		this.setName(c.getName());
	}
}
