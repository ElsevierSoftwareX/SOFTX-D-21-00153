// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;

/**
 * <p>STNUEdgeInt class.</p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class STNUEdgeInt extends STNEdgeInt implements STNUEdge {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * the labeled value associated to the edge
	 */
	int labeledValue;

	/**
	 * The label. The first component is the node label, the second specifies the nature: true for upper-case, false for lower-case.
	 */
	Pair nodeLabel;

	/**
	 * <p>Constructor for STNUEdgeInt.</p>
	 */
	public STNUEdgeInt() {
		super();
		clear();
	}

	/**
	 * Helper constructor
	 * 
	 * @param labeledIntMapImpl useless. This method is only here for convenience.
	 *            public STNUEdgeInt(Class<? extends LabeledIntMap> labeledIntMapImpl) {
	 *            this();
	 *            }
	 */

	/**
	 * <p>Constructor for STNUEdgeInt.</p>
	 *
	 * @param e a {@link it.univr.di.cstnu.graph.Edge} object.
	 */
	public STNUEdgeInt(Edge e) {
		super(e);
		if (e instanceof STNUEdge) {
			STNUEdge e1 = (STNUEdge) e;
			this.nodeLabel = e1.getNodeLabel();// pair is read-only
			this.labeledValue = e1.getLabeledValue();
		}
	}

	/**
	 * <p>Constructor for STNUEdgeInt.</p>
	 *
	 * @param n a {@link java.lang.String} object.
	 */
	public STNUEdgeInt(String n) {
		super(n);
		clear();
	}

	/**
	 * <p>Constructor for STNUEdgeInt.</p>
	 *
	 * @param n a {@link java.lang.String} object.
	 * @param v a int.
	 */
	public STNUEdgeInt(String n, int v) {
		super(n);
		clear();
		this.setValue(v);
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		super.clear();
		this.labeledValue = Constants.INT_NULL;
		this.nodeLabel = null;
	}

	/** {@inheritDoc} */
	@Override
	public int getLabeledValue() {
		return this.labeledValue;
	}

	/** {@inheritDoc} */
	@Override
	public Pair getNodeLabel() {
		return this.nodeLabel;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasSameValues(Edge e) {
		if (e == null || !(e instanceof STNUEdge))
			return false;
		STNUEdge e1 = (STNUEdge) e;
		if (this.value != e1.getValue())
			return false;
		if (this.labeledValue != e1.getLabeledValue() || this.nodeLabel != e1.getNodeLabel()) {
			return false;
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public STNUEdgeInt newInstance() {
		return new STNUEdgeInt();
	}

	/** {@inheritDoc} */
	@Override
	public STNUEdgeInt newInstance(Edge edge) {
		return new STNUEdgeInt(edge);
	}

	/** {@inheritDoc} */
	@Override
	public STNUEdgeInt newInstance(String name1) {
		return new STNUEdgeInt(name1);
	}

	/** {@inheritDoc} */
	@Override
	public int setLabeledValue(ALetter nodeALetter, int w, boolean upperCase) {
		int old = this.labeledValue;
		if (nodeALetter == null || w == Constants.INT_NULL) {
			this.nodeLabel = null;
			this.labeledValue = Constants.INT_NULL;
			return old;
		}
		if (!upperCase && w < 0) {
			throw new IllegalArgumentException("A lower-case value cannot be negative. Details: " + nodeALetter + ": " + w + ".");
		}
		this.labeledValue = w;
		this.nodeLabel = new Pair(nodeALetter, Boolean.valueOf(upperCase));
		return old;
	}

	/** {@inheritDoc} */
	@Override
	public void takeIn(Edge e) {
		if (e == null)
			return;
		super.takeIn(e);
		if ((e instanceof STNUEdge)) {
			STNUEdge e1 = (STNUEdge) e;
			this.labeledValue = e1.getLabeledValue();
			this.nodeLabel = e1.getNodeLabel();
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return this.getValue() == Constants.INT_NULL && this.getLabeledValue() == Constants.INT_NULL;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSTNUEdge() {
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSTNEdge() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return Constants.OPEN_TUPLE
				+ (this.getName().length() == 0 ? "<empty>" : this.getName())
				+ "; "
				+ this.getConstraintType()
				+ "; "
				+ ((this.getValue() != Constants.INT_NULL) ? Constants.formatInt(this.getValue()) + "; " : "")
				+ ((this.getLabeledValueFormatted().isEmpty()) ? "" : this.getLabeledValueFormatted())
				+ Constants.CLOSE_TUPLE;
	}
}
