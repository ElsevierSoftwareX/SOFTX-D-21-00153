// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

/**
 * Base class for implementing LabeledIntEdge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public abstract class AbstractEdge extends AbstractComponent implements Edge {
	/**
	 * To provide a unique id for the default creation of component.
	 */
	@SuppressWarnings("hiding")
	static int idSeq = 0;

	/**
	 * logger
	 */
	static final Logger LOG = Logger.getLogger("AbstractEdge");

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The type of the edge.
	 */
	ConstraintType constraintType;

	/**
	 * Minimal constructor. the name will be 'e&lt;id&gt;'.
	 */
	public AbstractEdge() {
		this((String) null);
	}

	/**
	 * Constructor to clone the component.
	 *
	 * @param e the component to clone.
	 */
	AbstractEdge(final Edge e) {
		this((e != null) ? e.getName() : (String) null);
		if (e != null) {
			this.setConstraintType(e.getConstraintType());
		}
	}

	/**
	 * Simplified constructor
	 *
	 * @param n
	 */
	AbstractEdge(final String n) {
		super(n);
		this.name = ((n == null) || (n.length() == 0)) ? "e" + idSeq++ : n;
		this.setConstraintType(ConstraintType.normal);
	}

	@Override
	public final ConstraintType getConstraintType() {
		return this.constraintType;
	}

	@Override
	public boolean isCSTNEdge() {
		return false;
	}

	@Override
	public boolean isCSTNPSUEdge() {
		return false;
	}

	@Override
	public boolean isCSTNUEdge() {
		return false;
	}

	@Override
	public boolean isSTNEdge() {
		return false;
	}

	@Override
	public boolean isSTNUEdge() {
		return false;
	}

	@Override
	public void setConstraintType(final ConstraintType type) {
		this.constraintType = type;
		notifyObservers("Type:" + type);
	}

	@Override
	public void takeIn(Edge e) {
		super.takeIn(e);
		this.constraintType = e.getConstraintType();
	}

	/**
	 * Set the name of the edge. Cannot be null or empty.
	 *
	 * @param edgeName the not-null not-empty new name
	 * @return the old name
	 */
	@Override
	public String setName(final String edgeName) {
		final String old = this.name;
		if ((edgeName != null) && (edgeName.length() > 0)) {
			this.name = edgeName;
			this.setChanged();
			notifyObservers("Name:" + old);
		}
		return old;
	}
}
