package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * A type-specific array-based FIFO queue, supporting also dequeue operations.
 * <em>An element is added only if not already present. Moreover, it is possible to check if an element if present.</em>
 * <p>
 * Instances of this class represent a FIFO queue using a backing array in a
 * circular way. The array is enlarged and shrunk as needed. You can use the
 * {@link #trim()} method to reduce its memory usage, if necessary.
 * <p>
 * This class provides additional methods that implement a <em>dequeue</em>
 * (double-ended queue).
 * 
 * @param <K>
 */
public class ObjectArrayFIFOSetQueue<K> implements PriorityQueue<K>, ObjectSet<K>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The standard initial capacity of a queue. */
	public static final int INITIAL_CAPACITY = 4;
	/** The backing array. */
	protected transient K array[];
	/** The current (cached) length of {@link #array}. */
	protected transient int length;
	/**
	 * The start position in {@link #array}. It is always strictly smaller than
	 * {@link #length}.
	 */
	protected transient int start;
	/**
	 * The end position in {@link #array}. It is always strictly smaller than
	 * {@link #length}. Might be actually smaller than {@link #start} because
	 * {@link #array} is used cyclically.
	 */
	protected transient int end;

	/**
	 * Creates a new empty queue with given capacity.
	 *
	 * @param capacity
	 *            the initial capacity of this queue.
	 */
	@SuppressWarnings("unchecked")
	public ObjectArrayFIFOSetQueue(final int capacity) {
		if (capacity < 0)
			throw new IllegalArgumentException("Initial capacity (" + capacity + ") is negative");
		this.array = (K[]) new Object[Math.max(1, capacity)]; // Never build a queue with zero-sized backing array.
		this.length = this.array.length;
	}

	/**
	 * Creates a new empty queue with standard {@linkplain #INITIAL_CAPACITY initial
	 * capacity}.
	 */
	public ObjectArrayFIFOSetQueue() {
		this(INITIAL_CAPACITY);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns {@code null} (FIFO queues have no comparator).
	 */
	@Override
	public Comparator<? super K> comparator() {
		return null;
	}

	@Override
	public K dequeue() {
		if (this.start == this.end)
			throw new NoSuchElementException();
		final K t = this.array[this.start];
		this.array[this.start] = null; // Clean-up for the garbage collector.
		if (++this.start == this.length)
			this.start = 0;
		reduce();
		return t;
	}

	/**
	 * Dequeues the {@linkplain PriorityQueue#last() last} element from the queue.
	 *
	 * @return the dequeued element.
	 * @throws NoSuchElementException
	 *             if the queue is empty.
	 */
	public K dequeueLast() {
		if (this.start == this.end)
			throw new NoSuchElementException();
		if (this.end == 0)
			this.end = this.length;
		final K t = this.array[--this.end];
		this.array[this.end] = null; // Clean-up for the garbage collector.
		reduce();
		return t;
	}

	/**
	 * @param size
	 * @param newLength
	 */
	@SuppressWarnings("unchecked")
	private final void resize(final int size, final int newLength) {
		final K[] newArray = (K[]) new Object[newLength];
		if (this.start >= this.end) {
			if (size != 0) {
				System.arraycopy(this.array, this.start, newArray, 0, this.length - this.start);
				System.arraycopy(this.array, 0, newArray, this.length - this.start, this.end);
			}
		} else
			System.arraycopy(this.array, this.start, newArray, 0, this.end - this.start);
		this.start = 0;
		this.end = size;
		this.array = newArray;
		this.length = newLength;
	}

	/**
	 * 
	 */
	private final void expand() {
		resize(this.length, (int) Math.min(it.unimi.dsi.fastutil.Arrays.MAX_ARRAY_SIZE, 2L * this.length));
	}

	/**
	 * 
	 */
	private final void reduce() {
		final int size = size();
		if (this.length > INITIAL_CAPACITY && size <= this.length / 4)
			resize(size, this.length / 2);
	}

	@Override
	public void enqueue(K x) {
		if (this.contains(x))
			return;
		this.array[this.end++] = x;
		if (this.end == this.length)
			this.end = 0;
		if (this.end == this.start)
			expand();
	}

	/**
	 * Enqueues a new element as the first element (in dequeuing order) of the
	 * queue.
	 * 
	 * @param x the element to enqueue.
	 */
	public void enqueueFirst(K x) {
		if (this.contains(x))
			return;
		if (this.start == 0)
			this.start = this.length;
		this.array[--this.start] = x;
		if (this.end == this.start)
			expand();
	}

	@Override
	public K first() {
		if (this.start == this.end)
			throw new NoSuchElementException();
		return this.array[this.start];
	}

	@Override
	public K last() {
		if (this.start == this.end)
			throw new NoSuchElementException();
		return this.array[(this.end == 0 ? this.length : this.end) - 1];
	}

	@Override
	public void clear() {
		if (this.start <= this.end)
			Arrays.fill(this.array, this.start, this.end, null);
		else {
			Arrays.fill(this.array, this.start, this.length, null);
			Arrays.fill(this.array, 0, this.end, null);
		}
		this.start = this.end = 0;
	}

	/** Trims the queue to the smallest possible size. */
	@SuppressWarnings("unchecked")
	public void trim() {
		final int size = size();
		final K[] newArray = (K[]) new Object[size + 1];
		if (this.start <= this.end)
			System.arraycopy(this.array, this.start, newArray, 0, this.end - this.start);
		else {
			System.arraycopy(this.array, this.start, newArray, 0, this.length - this.start);
			System.arraycopy(this.array, 0, newArray, this.length - this.start, this.end);
		}
		this.start = 0;
		this.length = (this.end = size) + 1;
		this.array = newArray;
	}

	@Override
	public int size() {
		final int apparentLength = this.end - this.start;
		return apparentLength >= 0 ? apparentLength : this.length + apparentLength;
	}

	/**
	 * @param o
	 * @return true if the queue contains o.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		if (o == null || (this.start == this.end))
			return false;
		return (this.getIndex((K) o) != -1);
	}

	/**
	 * @param k
	 * @return the index of k if present, -1 otherwise
	 */
	private int getIndex(K k) {
		int size = size();
		for (int i = this.start; size-- != 0;) {
			if (this.array[i++].equals(k))
				return i - 1;
			if (i == this.length)
				i = 0;
		}
		return -1;
	}

	/**
	 * @return a copy of the queue as an array.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public K[] toArray() {
		final int size = size();
		final K[] newArray = (K[]) new Object[size + 1];
		if (this.start <= this.end)
			System.arraycopy(this.array, this.start, newArray, 0, this.end - this.start);
		else {
			System.arraycopy(this.array, this.start, newArray, 0, this.length - this.start);
			System.arraycopy(this.array, 0, newArray, this.length - this.start, this.end);
		}
		return newArray;
	}


	/**
	 * @return an iterator on the current queue. If the queue is modified during the use of the iterator, then the iterator behavior is undefined.
	 */
	@Override
	public ObjectIterator<K> iterator() {
		return new ObjectIterator<K>() {
			int pos = ObjectArrayFIFOSetQueue.this.start, max = ObjectArrayFIFOSetQueue.this.length, last = ObjectArrayFIFOSetQueue.this.end;

			@Override
			public boolean hasNext() {
				if (this.pos==this.last)
					return false;
				return true;
			}

			@Override
			public K next() {
				if (!hasNext())
					throw new NoSuchElementException();
				final K t = ObjectArrayFIFOSetQueue.this.array[this.pos];
				if (++this.pos == this.max)
					this.pos = 0;
				return t;
			}
		};
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException("toArray");
	}

	@Override
	public boolean add(K e) {
		if (this.contains(e))
			return false;
		enqueue(e);
		return true;
	}

	@Override
	public boolean remove(Object o) {
		if (o == null || this.start == this.end)
			return false;
		@SuppressWarnings("unchecked")
		K ok = (K) o;
		int i = getIndex(ok);
		if (i==-1) 
			return false;
		if (i <= this.end) {
			for (; i < this.end;) {
				this.array[i] = this.array[++i];
			}
			this.end--;
		} else {
			for (; i > this.start;) {
				this.array[i] = this.array[--i];
			}
			this.start++;
		}
		return true;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!this.contains(o))
				return false;
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends K> c) {
		for (K o : c) {
			this.enqueue(o);
		}
		return true;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c) {
			this.remove(o);
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("retainAll");
	}

	/**
	 * @param s
	 * @throws IOException
	 */
	private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException {
		s.defaultWriteObject();
		int size = size();
		s.writeInt(size);
		for (int i = this.start; size-- != 0;) {
			s.writeObject(this.array[i++]);
			if (i == this.length)
				i = 0;
		}
	}

	@Override
	public String toString() {
		int size = size();
		StringBuffer sb = new StringBuffer("[");
		for (int i = this.start; size-- != 0;) {
			sb.append(this.array[i++].toString());
			sb.append(", ");
			if (i == this.length)
				i = 0;
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("]");
		return sb.toString();
	}
	/**
	 * @param s
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
		s.defaultReadObject();
		this.end = s.readInt();
		this.array = (K[]) new Object[this.length = HashCommon.nextPowerOfTwo(this.end + 1)];
		for (int i = 0; i < this.end; i++)
			this.array[i] = (K) s.readObject();
	}

	@Override
	public boolean isEmpty() {
		return this.start == this.end;
	}
}
