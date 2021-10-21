// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.util.Comparator;
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

/**
 * Simple implementation a priority queue where elements are T objects and priorities are integers.<br>
 * It is based on {@link BinaryHeap} and it maintains memory of elements that have been inserted and removed (see {@link #getStatus(Object)} 
 *
 * @author posenato
 * @version $Id: $Id
 * @param <T> tyep pf object in the queue. Usually are T or Edge
 * 
 */
public class PriorityQueue<T> {
	/**
	 * The possible state of an element with respect to a {@link PriorityQueue}.
	 * 
	 * @author posenato
	 */
	public enum Status {
		/**
		 * the element is currently in the queue.
		 */
		isPresent,
		/**
		 * the element has been never added in the queue.
		 */
		notPresent,
		/**
		 * the element has been added and removed from this queue.
		 */
		wasPresent
	}

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger("it.univr.di.cstnu.algorithms.PriorityQueue");

	/**
	 * @param entry
	 * @return the status of the object
	 */
	private Status getEntryStatus(Entry<Integer, T> entry) {
		if (entry == null)
			return Status.notPresent;
		if (entry.getValue() == null)
			return Status.wasPresent;
		return Status.isPresent;
	}

	/**
	 * The heap organized as binary heap.
	 */
	// private FibonacciHeap<Integer, T> heap = new FibonacciHeap<>();
	private BinaryHeap<Integer, T> heap = new BinaryHeap<>();

	/**
	 * For each object of type T, it returns the heap entry associated to the object, i.e., T-->(int, node)
	 * If the returned entry (int, o) has o == null, it means that o was in the queue and it was removed.
	 */
	private Object2ObjectMap<T, Entry<Integer, T>> map = new Object2ObjectAVLTreeMap<>();

	/**
	 * Makes the queue empty.
	 */
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
	 *
	 *             private void decreasePriority(T item, int priority) {
	 *             Entry<Integer, T> entry = this.map.get(item);
	 *             this.heap.decreaseKey(entry, priority);
	 *             }
	 */

	/**
	 * Remove and return the item with minimum priority in this heap.
	 *
	 * @return the T with minimum priority.
	 */
	public T extractMin() {
		Entry<Integer, T> entry = this.heap.extractMinimum();
		T min = entry.getValue();
		setWasPresent(entry);
		return min;
	}

	/**
	 * Remove and return the minimum entry in this heap.
	 *
	 * @return the entry &lt;T, int&gt; with minimum priority.
	 */
	public BasicEntry<T> extractMinEntry() {
		Entry<Integer, T> entry = this.heap.extractMinimum();
		BasicEntry<T> basicEntry = new BasicEntry<>(entry.getValue(), entry.getKey().intValue());
		setWasPresent(entry);
		return basicEntry;
	}

	/**
	 * @return the map (T, priority) of all the elements that have been in the queue.
	 */
	public Object2IntMap<T> getAllDeterminedPriorities(){
		Object2IntMap<T> nodeDistance = new Object2IntLinkedOpenHashMap<>();
		for (T node : this.map.keySet()) {
			Entry<Integer, T> entry =  this.map.get(node);
			nodeDistance.put(node, entry.getKey().intValue());
		}
		return nodeDistance;
	}
	
	/**
	 * @param obj must be present.
	 * @return the priority of the object if it is present.
	 */
	public int getPriority(T obj) {
		if (getStatus(obj) != Status.isPresent)
			throw new IllegalArgumentException("Node " + obj + " is not present in the queue.");
		return this.map.get(obj).getKey().intValue();
	}

	/**
	 * Usually, in a priority queue an object is firtsly added and, possibly, removed.<br>
	 * This class remembers all objects that have been added in the queue.<br> 
	 * Therefore, this method returns the possible state of an element (see {@link Status}).
	 * 
	 * @param obj an object.
	 * @return a {@link it.univr.di.cstnu.algorithms.PriorityQueue.Status} object.
	 */
	public Status getStatus(T obj) {
		Entry<Integer, T> entry = this.map.get(obj);
		return getEntryStatus(entry);
	}

	/**
	 * Insert or update the given object and its priority into this heap.
	 *
	 * @param obj an object
	 * @param priority the value of priority
	 * @return true if the operation was successful; false if a previous value was already removed and the new priority is lower than the previous value.
	 */
	public boolean insertOrDecrease(T obj, int priority) {
		Entry<Integer, T> entry = this.map.get(obj);
		final Status status = getEntryStatus(entry);
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
			entry = this.heap.insert(priority, obj);
			this.map.put(obj, entry);
			break;
		}
		return true;
	}

	/**
	 * @return true if the queue is empty, false otherwise.
	 */
	public boolean isEmpty() {
		return this.heap.isEmpty();
	}

	/**
	 * @return the number of elements in the queue.
	 */
	public int size() {
		return this.heap.getSize();
	}

	/**
	 * @return a string representing the ordered queue w.r.t. the priority.
	 */
	@Override
	public String toString() {
		ObjectList<Entry<Integer, T>> h = new ObjectArrayList<>(this.heap.getEntries());
		h.sort(new Comparator<Entry<Integer, T>>() {
			@Override
			public int compare(Entry<Integer, T> o1, Entry<Integer, T> o2) {
				int c = o1.getKey() - o2.getKey();
				return c;
			}
		});
		return h.toString();
	}

	/**
	 * @param item the element
	 * @return the priority associated to item in the queue, {@link it.univr.di.labeledvalue.Constants#INT_NULL} if the item is not in the queue.
	 */
	public int value(T item) {
		return this.getPriority(item);
	}
	
	/**
	 * Delete an entry.
	 * @param entry to delete.
	 */
	private void setWasPresent(Entry<Integer, T> entry) {
		T o = entry.getValue();
		entry.setValue(null);
		this.map.put(o, entry);
	}
}
