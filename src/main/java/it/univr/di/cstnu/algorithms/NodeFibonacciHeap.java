package it.univr.di.cstnu.algorithms;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.teneighty.heap.FibonacciHeap;
import org.teneighty.heap.Heap.Entry;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;

public class NodeFibonacciHeap {

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger("it.univr.di.cstnu.algorithms.NodeFibonacciHeap");

	FibonacciHeap<Integer, LabeledNode> heap = new FibonacciHeap<>();

	Map<LabeledNode, Entry<Integer, LabeledNode>> map = new HashMap<>();

	/**
	 * Insert the given item into this heap with specified priority, returning the entry in which the new pair is stored.
	 * 
	 * @param item the labeled node
	 * @param priority the value of priority
	 * @throws NullPointerException - If node is null.
	 */
	public void add(LabeledNode item, int priority) {
		Entry<Integer, LabeledNode> entry = this.heap.insert(priority, item);
		this.map.put(item, entry);
	}

	/**
	 * Decrease the key of the given element.
	 * This class can always cheaply determine if item is not a member of this heap (in O(1) time, thanks to reference magic).
	 * 
	 * @param item the node
	 * @param priority the new value
	 * @throws IllegalArgumentException - If priority is larger than item's current key or item is not a member of this heap.
	 * @throws ClassCastException - If the new key is not mutually comparable with other keys in the heap.
	 */
	public void decreasePriority(LabeledNode item, int priority) {
		// try {
			this.heap.decreaseKey(this.map.get(item), priority);
		// } catch (IllegalArgumentException e) {
		// String log = "Problem with " + item + " and new priority " + priority + ". The map says that item is in it: " + this.map.containsKey(item)
		// + " with priority " + this.map.get(item).getKey();
		// LOG.severe(log);
		// throw new IllegalArgumentException(log);
		// }
	}

	/**
	 * Remove and return the item with minimum priority in this heap.
	 * 
	 * @return the LabeledNode with minimum priority.
	 * @throws NoSuchElementException - If this heap is empty.
	 */
	public LabeledNode extractMin() {
		return this.heap.extractMinimum().getValue();
	}

	/**
	 * Remove and return the minimum entry in this heap.
	 * 
	 * @return the entry <LabeledNode, int> with minimum priority.
	 * @throws NoSuchElementException - If this heap is empty.
	 */
	public BasicEntry<LabeledNode> extractMinEntry() {
		Entry<Integer, LabeledNode> entry = this.heap.extractMinimum();
		return new BasicEntry<>(entry.getValue(), entry.getKey().intValue());
	}

	public void clear() {
		this.heap.clear();
	}

	public int size() {
		return this.heap.getSize();
	}

	/**
	 * @param item
	 * @return the priority associated to item in the queue, {@link Constants#INT_NULL} if the item is not in the queue.
	 */
	public int value(LabeledNode item) {
		Entry<Integer, LabeledNode> entry = this.map.get(item);
		if (entry == null)
			return Constants.INT_NULL;
		return entry.getKey().intValue();
	}

	/**
	 * @return the map (LabeledNode, priority) of all the elements that have been added in the queue.
	 */
	public Object2IntMap<LabeledNode> getPriorities() {
		Object2IntMap<LabeledNode> nodeDistance = new Object2IntLinkedOpenHashMap<>();
		for (Entry<Integer, LabeledNode> entry : this.map.values()) {
			nodeDistance.put(entry.getValue(), entry.getKey().intValue());
		}
		return nodeDistance;
	}
}
