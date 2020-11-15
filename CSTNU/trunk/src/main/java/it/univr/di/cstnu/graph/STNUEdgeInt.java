/**
 * 
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;

/**
 * @author posenato
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
	 * 
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
	 * @param e
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
	 * @param n
	 */
	public STNUEdgeInt(String n) {
		super(n);
		clear();
	}

	/**
	 * @param n
	 * @param v
	 */
	public STNUEdgeInt(String n, int v) {
		super(n);
		clear();
		this.setValue(v);
	}

	@Override
	public void clear() {
		super.clear();
		this.labeledValue = Constants.INT_NULL;
		this.nodeLabel = null;
	}

	@Override
	public int getLabeledValue() {
		return this.labeledValue;
	}

	@Override
	public Pair getNodeLabel() {
		return this.nodeLabel;
	}

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

	@Override
	public STNUEdgeInt newInstance() {
		return new STNUEdgeInt();
	}

	@Override
	public STNUEdgeInt newInstance(Edge edge) {
		return new STNUEdgeInt(edge);
	}

	@Override
	public STNUEdgeInt newInstance(String name1) {
		return new STNUEdgeInt(name1);
	}

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

	@Override
	public boolean isEmpty() {
		return this.getValue() == Constants.INT_NULL && this.getLabeledValue() == Constants.INT_NULL;
	}

	@Override
	public boolean isSTNUEdge() {
		return true;
	}

	@Override
	public boolean isSTNEdge() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
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
