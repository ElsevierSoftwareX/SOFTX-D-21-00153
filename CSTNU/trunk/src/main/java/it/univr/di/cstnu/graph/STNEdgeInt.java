// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.Constants;

/**
 * <p>STNEdgeInt class.</p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class STNEdgeInt extends AbstractEdge implements STNEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the value associated to the edge
	 */
	int value;

	/**
	 * <p>Constructor for STNEdgeInt.</p>
	 */
	public STNEdgeInt() {
		super();
		clear();
	}

	/**
	 * helper constructor
	 * 
	 * @param labeledIntMapImpl useless. This method is only here for convenience.
	 *            public STNEdgeInt(Class<? extends LabeledIntMap> labeledIntMapImpl) {
	 *            this();
	 *            }
	 */

	/**
	 * <p>Constructor for STNEdgeInt.</p>
	 *
	 * @param e a {@link it.univr.di.cstnu.graph.Edge} object.
	 */
	public STNEdgeInt(Edge e) {
		super(e);
		if (e instanceof STNEdge)
			this.value = ((STNEdge) e).getValue();
	}

	/**
	 * <p>Constructor for STNEdgeInt.</p>
	 *
	 * @param n a {@link java.lang.String} object.
	 */
	public STNEdgeInt(String n) {
		super(n);
		clear();
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		super.clear();
		this.value = Constants.INT_NULL;
	}

	/** {@inheritDoc} */
	@Override
	public int getValue() {
		return this.value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof STNEdge))
			return false;
		return this.value == ((STNEdge) e).getValue();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.value == Constants.INT_NULL;
	}

	/** {@inheritDoc} */
	@Override
	public STNEdgeInt newInstance() {
		return new STNEdgeInt();
	}

	// @Override
	// public STNEdgeInt newInstance(Class<? extends LabeledIntMap> labeledIntMapImpl) {
	// return newInstance();
	// }

	/** {@inheritDoc} */
	@Override
	public STNEdgeInt newInstance(Edge edge) {
		return new STNEdgeInt(edge);
	}

	// @Override
	// public STNEdgeInt newInstance(Edge edge, Class<? extends LabeledIntMap> labeledIntMapImpl) {
	// return new STNEdgeInt(edge);
	// }

	/** {@inheritDoc} */
	@Override
	public STNEdgeInt newInstance(String name1) {
		return new STNEdgeInt(name1);
	}

	// @Override
	// public STNEdgeInt newInstance(String name1, Class<? extends LabeledIntMap> labeledIntMapImpl) {
	// return new STNEdgeInt(name1);
	// }

	/** {@inheritDoc} */
	@Override
	public int setValue(int w) {
		int old = this.value;
		this.value = w;
		return old;
	}

	/** {@inheritDoc} */
	@Override
	public void takeIn(Edge e) {
		if (e == null || !(e instanceof STNEdge))
			return;
		super.takeIn(e);
		this.value = ((STNEdge) e).getValue();
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSTNEdge() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Constants.OPEN_TUPLE + (this.getName().length() == 0 ? "<empty>" : this.getName()) + "; " + this.getConstraintType() + "; "
				+ Constants.formatInt(this.value) + "; " + Constants.CLOSE_TUPLE;
	}

}
