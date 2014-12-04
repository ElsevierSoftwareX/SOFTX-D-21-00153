/**
 * 
 */
package it.univr.di.labeledvalue;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;

import java.util.Set;

/**
 * To each label is associated a pair, value e node set.
 * 
 * @author Roberto Posenato
 */
public class ValueNodeSetPair {
	/**
	 * 
	 */
	public int defaultReturnValue = LabeledIntMap.INT_NULL;

	/**
	 * Contains internal node names of any negative path starting from Z. <br>
	 * It is necessary to use node name (String) instead of LabeledNode (that would be the right representation) because here it is necessary to build
	 * set with different nodes in a common sense (i.e., with different names) but the method equals of LabeledNode (method used by Set class to
	 * decide if two nodes are equal/different) does not compare the name but the memory address (see {@link it.univr.di.cstnu.graph.Component#equals(Object)}
	 * for explanation).
	 */
	@SuppressWarnings("javadoc")
	private Set<String> nodeNameSet;

	/**
	 * The value
	 */
	private int value;

	/**
	 * 
	 */
	public ValueNodeSetPair() {
		value = LabeledIntMap.INT_NULL;
		nodeNameSet = null;
	}

	/**
	 * @param newValue
	 * @param newNodeSet
	 */
	public ValueNodeSetPair(int newValue, Set<String> newNodeSet) {
		value = newValue;
		nodeNameSet = (newNodeSet == null) ? null : new ObjectArraySet<>(newNodeSet);
	}

	/**
	 * Add node to the node set associated to the label.
	 * 
	 * @param nodeName not null node
	 * @return true if the node has been added (the set didn't contain it before), false otherwise.
	 */
	public boolean addInNodeSet(String nodeName) {
		if (nodeName == null) return false;
		if (nodeNameSet == null) nodeNameSet = new ObjectArraySet<>();
		return nodeNameSet.add(nodeName);
	}

	/**
	 * 
	 * @param nodeName
	 * @return true if 'node' is in the set of nodes associated to the label 'qPath' that represents a potential negative path.
	 */
	public boolean containsInNodeSet(String nodeName) {
		if (nodeName == null || this.isNodeSetNullOrEmpty()) return false;
		return nodeNameSet.contains(nodeName);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ValueNodeSetPair)) return false;
		ValueNodeSetPair o1 = (ValueNodeSetPair) o;
		if (value != o1.value) return false;
		if (isNodeSetNullOrEmpty() && o1.isNodeSetNullOrEmpty()) return value == o1.value;
		if (isNodeSetNullOrEmpty() || o1.isNodeSetNullOrEmpty()) return false;
		return value == o1.value && nodeNameSet.equals(o1.nodeNameSet);
	}

	/**
	 * @return the node set associated to the label. Null if such node set does not exists or it is empty.
	 */
	public Set<String> getNodeSet() {
		if (this.isNodeSetNullOrEmpty()) return null;
		return new ObjectArraySet<>(nodeNameSet);
	}

	/**
	 * @param set 
	 */
	public void setNodeSet(Set<String> set) {
		if (set == null || set.isEmpty()) {
			nodeNameSet = null;
			return;
		}
		nodeNameSet = new ObjectArraySet<>(set);
	}

	/**
	 * @param set
	 */
	public void addNodeSet(Set<String> set) {
		if (set == null || set.isEmpty()) return;
		if (nodeNameSet==null) new ObjectArraySet<>();
		nodeNameSet.addAll(set);
	}
	
	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}
	
	/**
	 * @param i the new value
	 */
	public void setValue(int i) {
		value = i;
	}
	
	@Override
	public int hashCode() {
		return value * ((nodeNameSet != null) ? nodeNameSet.hashCode() : 1);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
			if (value < 0) {
				sb.append('-');
			}
			sb.append(Constants.INFINITY_SYMBOL);
		} else {
			sb.append(value);
		}
		if (nodeNameSet != null && !nodeNameSet.isEmpty()) {
			sb.append(", ");
			sb.append(nodeNameSet.toString());
		}
		return sb.toString();
	}

	@SuppressWarnings({ "javadoc" })
	public final boolean isNodeSetNullOrEmpty() {
		return nodeNameSet == null || nodeNameSet.isEmpty();
	}

}
