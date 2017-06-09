package it.univr.di.labeledvalue;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.univr.di.labeledvalue.Constants;

import java.util.Set;
import java.util.SortedSet;

/**
 * Simple class to associate an integer value and a set of strings (representing a set of node names) to a CSTN label.<br>
 *
 * The node name set contains internal node names of any negative path starting from Z. <br>
 * It is necessary to use node name (String) instead of LabeledNode (that would be the right representation) because here it is necessary to build
 * set with different nodes in a common sense (i.e., with different names) but the method equals of LabeledNode (method used by Set class to
 * decide whether two nodes are equal/different) does not compare the name but the memory address (see {@link it.univr.di.cstnu.graph.AbstractComponent#equals(Object)}
 * for
 * explanation).
 *
 * This class does not make defensive copy of any given string set.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
@SuppressWarnings("javadoc")
public class ValueNodeSetPair {
	/**
	 * Factory for set of strings.
	 *
	 * @return a new set instance.
	 */
	public final static SortedSet<String> newSetInstance() {
		return new ObjectRBTreeSet<>();
	}

	/** 
	 */
	public static int defaultReturnValue = Constants.INT_NULL;

	/**
	 * Contains internal node names of any negative path starting from Z. <br>
	 * It is necessary to use node name (String) instead of LabeledNode (that would be the right representation) because here it is necessary to build
	 * set with different nodes in a common sense (i.e., with different names) but the method equals of LabeledNode (method used by Set class to
	 * decide if two nodes are equal/different) does not compare the name but the memory address (see {@link it.univr.di.cstnu.graph.AbstractComponent#equals(Object)}
	 * for explanation).
	 */
	private SortedSet<String> nodeNameSet;

	/**
	 * The value
	 */
	private int value;

	/**
	 * <p>Constructor for ValueNodeSetPair.</p>
	 */
	public ValueNodeSetPair() {
		value = defaultReturnValue;
		nodeNameSet = null;

	}

	/**
	 * <p>Constructor for ValueNodeSetPair.</p>
	 *
	 * @param newValue a int.
	 * @param newNodeSet a {@link java.util.Set} object.
	 */
	public ValueNodeSetPair(int newValue, Set<String> newNodeSet) {
		value = newValue;
		if (newNodeSet == null || newNodeSet.isEmpty()) nodeNameSet = null;
		else {
			nodeNameSet =newSetInstance();
			nodeNameSet.addAll(newNodeSet);
		}
	}

	/**
	 * Add node to the node set associated to the label.
	 *
	 * @param nodeName not null node
	 * @return true if the node has been added (the set didn't contain it before), false otherwise.
	 */
	public boolean add(String nodeName) {
		if (nodeName == null) return false;
		if (nodeNameSet == null) nodeNameSet = newSetInstance();
		return nodeNameSet.add(nodeName);
	}

	/**
	 * <p>add.</p>
	 *
	 * @param inputSet set of node names to add to the current internal node name set.
	 */
	public void add(Set<String> inputSet) {
		if (inputSet == null || inputSet.isEmpty()) return;
		if (this.nodeNameSet == null ) this.nodeNameSet = newSetInstance();
		this.nodeNameSet.addAll(inputSet);
	}

	/**
	 * <p>containsInNodeSet.</p>
	 *
	 * @param nodeName a {@link java.lang.String} object.
	 * @return true if 'node' is in the set of nodes associated to the label 'qPath' that represents a potential negative path.
	 */
	public boolean containsInNodeSet(String nodeName) {
		if (nodeName == null || this.isNodeSetNullOrEmpty()) return false;
		return nodeNameSet.contains(nodeName);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Two ValueNodeSetPair are equals if they have the same value and the same set of node names.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ValueNodeSetPair)) return false;
		ValueNodeSetPair o1 = (ValueNodeSetPair) o;
		if (value != o1.value) return false;
		if (isNodeSetNullOrEmpty() && o1.isNodeSetNullOrEmpty()) return value == o1.value;
		if (isNodeSetNullOrEmpty() || o1.isNodeSetNullOrEmpty()) return false;
		return value == o1.value && nodeNameSet.equals(o1.nodeNameSet);
	}

	/**
	 * <p>getNodeSet.</p>
	 *
	 * @return the node set associated to the label as a independent copy (new equal set). Null if such node set does not exists or it is empty.
	 */
	public SortedSet<String> getNodeSet() {
		if (this.isNodeSetNullOrEmpty()) return null;
		return nodeNameSet;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return value * ((nodeNameSet != null) ? nodeNameSet.hashCode() : 1);
	}

	/**
	 * <p>isNodeSetNullOrEmpty.</p>
	 *
	 * @return a boolean.
	 */
	public final boolean isNodeSetNullOrEmpty() {
		return nodeNameSet == null || nodeNameSet.isEmpty();
	}

	/**
	 * <p>setNodeSet.</p>
	 *
	 * @param set the node name set to use. This method does not make a defensive copy of the set.
	 */
	public void setNodeSet(SortedSet<String> set) {
		if (set == null || set.isEmpty()) nodeNameSet = null;
		else nodeNameSet = set;
	}

	/**
	 * <p>Setter for the field <code>value</code>.</p>
	 *
	 * @param i the new value
	 */
	public void setValue(int i) {
		value = i;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
			if (value < 0) sb.append('-');
			sb.append(Constants.INFINITY_SYMBOL);
		} else {
			sb.append(value);
		}
		if (nodeNameSet != null){// && !nodeNameSet.isEmpty()) {
			sb.append(", ");
			sb.append(nodeNameSet.toString());
		}
		return sb.toString();
	}

}
