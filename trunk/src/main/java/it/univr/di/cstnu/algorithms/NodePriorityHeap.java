// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap.Entry;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;

/**
 * Simple implementation of the Fibonacci Heap data structure, where elements are {@link LabeledNode} and priorities are integers.
 * 
 * @author posenato
 */
public class NodePriorityHeap {
	@SuppressWarnings("javadoc")
	public enum NodeStatus {
		isPresent, notPresent, wasPresent
	}

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger("it.univr.di.cstnu.algorithms.NodePriorityHeap");

	@SuppressWarnings("javadoc")
	private static NodeStatus getStatus(Entry<Integer, LabeledNode> entry) {
		if (entry == null)
			return NodeStatus.notPresent;
		if (entry.getValue() == null)
			return NodeStatus.wasPresent;
		return NodeStatus.isPresent;
	}

	/**
	 * The heap organized as FibonacciHeap
	 */
	// private FibonacciHeap<Integer, LabeledNode> heap = new FibonacciHeap<>();
	private BinaryHeap<Integer, LabeledNode> heap = new BinaryHeap<>();

	/**
	 * For each node, it returns the heap entry associated to the node, i.e., node-->(int, node)
	 * If the returned entry (int, node) has node == null, it means that node was in the queue and it was removed.
	 */
	private Object2ObjectMap<LabeledNode, Entry<Integer, LabeledNode>> map = new Object2ObjectAVLTreeMap<>();

	@SuppressWarnings("javadoc")
	public void clear() {
		this.heap.clear();
		this.map.clear();
	}

	/**
	 * Decrease the key of the given element.
	 * This class can always cheaply determine if item is not a member of this heap (in O(1) time, thanks to reference magic).
	 * 
	 * @param item the node
	 * @param priority the new value
	 * @throws IllegalArgumentException - If priority is larger than item's current key or item is not a member of this heap.
	 * @throws ClassCastException - If the new key is not mutually comparable with other keys in the heap.
	 *             private void decreasePriority(LabeledNode item, int priority) {
	 *             Entry<Integer, LabeledNode> entry = this.map.get(item);
	 *             this.heap.decreaseKey(entry, priority);
	 *             }
	 */

	/**
	 * Remove and return the item with minimum priority in this heap.
	 * 
	 * @return the LabeledNode with minimum priority.
	 * @throws NoSuchElementException - If this heap is empty.
	 */
	public LabeledNode extractMin() {
		Entry<Integer, LabeledNode> entry = this.heap.extractMinimum();
		LabeledNode min = entry.getValue();
		setWasPresent(entry);
		return min;
	}

	/**
	 * Remove and return the minimum entry in this heap.
	 * 
	 * @return the entry <LabeledNode, int> with minimum priority.
	 * @throws NoSuchElementException - If this heap is empty.
	 */
	public BasicEntry<LabeledNode> extractMinEntry() {
		Entry<Integer, LabeledNode> entry = this.heap.extractMinimum();
		BasicEntry<LabeledNode> basicEntry = new BasicEntry<>(entry.getValue(), entry.getKey().intValue());
		setWasPresent(entry);
		return basicEntry;
	}

	/**
	 * @return the map (LabeledNode, priority) of all the elements that have been added in the queue.
	 */
	public Object2IntMap<LabeledNode> getPriorities() {
		Object2IntMap<LabeledNode> nodeDistance = new Object2IntLinkedOpenHashMap<>();
		for (Entry<Integer, LabeledNode> entry : this.map.values()) {
			if (entry.getValue() == null)
				continue;
			nodeDistance.put(entry.getValue(), entry.getKey().intValue());
		}
		return nodeDistance;
	}

	/**
	 * @param node must be present.
	 * @return the priority of the node if it is present.
	 */
	public int getPriority(LabeledNode node) {
		if (getStatus(node) != NodeStatus.isPresent)
			throw new IllegalArgumentException("Node " + node + " is not present in the queue.");
		return this.map.get(node).getKey().intValue();
	}

	/**
	 * @param node must be present.
	 * @return the priority of the node if it is present.
	 */
	public int getPriorityRemovedElement(LabeledNode node) {
		if (getStatus(node) != NodeStatus.wasPresent)
			throw new IllegalArgumentException("Node " + node + " was never removed.");
		return this.map.get(node).getKey().intValue();
	}

	@SuppressWarnings("javadoc")
	public NodeStatus getStatus(LabeledNode node) {
		Entry<Integer, LabeledNode> entry = this.map.get(node);
		return getStatus(entry);
	}

	/**
	 * Insert or update the given node and its priority into this heap.
	 * 
	 * @param node the labeled node
	 * @param priority the value of priority
	 * @return true if the operation was successful; false if a previous value was already removed and the new priority is lower than the previous value.
	 */
	public boolean insertOrDecrease(LabeledNode node, int priority) {
		Entry<Integer, LabeledNode> entry = this.map.get(node);

		final NodeStatus status = getStatus(entry);
		switch (status) {
		case isPresent:
			if (entry.getKey() > priority)
				this.heap.decreaseKey(entry, priority);
			break;
		case wasPresent:
			if (entry.getKey() > priority) {
				return false;
			}
			break;
		case notPresent:
		default:
			entry = this.heap.insert(priority, node);
			this.map.put(node, entry);
			break;
		}
		return true;
	}

	@SuppressWarnings("javadoc")
	public boolean isEmpty() {
		return this.heap.isEmpty();
	}

	@SuppressWarnings("javadoc")
	public int size() {
		return this.heap.getSize();
	}

	/**
	 * Return the ordered queue w.r.t. the priority
	 */
	@Override
	public String toString() {
		ObjectList<Entry<Integer, LabeledNode>> h = new ObjectArrayList<>(this.heap.getEntries());
		h.sort(new Comparator<Entry<Integer, LabeledNode>>() {
			@Override
			public int compare(Entry<Integer, LabeledNode> o1, Entry<Integer, LabeledNode> o2) {
				int c = o1.getKey() - o2.getKey();
				if (c == 0)
					return o1.getValue().compareTo(o2.getValue());
				return c;
			}
		});
		return h.toString();
	}

	/**
	 * @param item the element
	 * @return the priority associated to item in the queue, {@link Constants#INT_NULL} if the item is not in the queue.
	 */
	public int value(LabeledNode item) {
		return this.getPriority(item);
	}

	@SuppressWarnings("javadoc")
	private void setWasPresent(Entry<Integer, LabeledNode> entry) {
		LabeledNode node = entry.getValue();
		entry.setValue(null);
		this.map.put(node, entry);
	}
}
