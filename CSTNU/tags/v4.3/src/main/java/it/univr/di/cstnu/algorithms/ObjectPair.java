// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of <code>Collection</code> interface that stores exactly
 * 2 objects and is not mutable. They respect <code>equals</code>
 * and may be used as indices or map keys.
 * 
 * @param <T> type of the two elements.
 */
class ObjectPair<T> implements Collection<T>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * @author posenato
	 */
	private class PairIterator implements Iterator<T> {
		/**
		 * 
		 */
		int position;

		/**
		 * 
		 */
		PairIterator() {
			this.position = 0;
		}

		@Override
		public boolean hasNext() {
			return this.position < 2;
		}

		@Override
		public T next() {
			this.position++;
			if (this.position == 1)
				return ObjectPair.this.first;
			else if (this.position == 2)
				return ObjectPair.this.second;
			else
				throw new NoSuchElementException("No more elements");
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("Pairs cannot be mutated");
		}
	}

	/**
	 * 
	 */
	private T first;

	/**
	 * 
	 */
	private T second;

	/**
	 * Creates a Pair from the passed Collection.
	 * The size of the Collection must be 2.
	 *
	 * @param values the elements of the new <code>Pair</code>
	 */
	public ObjectPair(Collection<? extends T> values) {
		if (values.size() == 2) {
			Iterator<? extends T> iter = values.iterator();
			this.first = iter.next();
			this.second = iter.next();
		} else
			throw new IllegalArgumentException("Pair may only be created from a Collection of exactly 2 elements");

	}

	/**
	 * Creates a <code>Pair</code> from the specified elements.
	 *
	 * @param value1 the first value in the new <code>Pair</code>
	 * @param value2 the second value in the new <code>Pair</code>
	 */
	public ObjectPair(T value1, T value2) {
		this.first = value1;
		this.second = value2;
	}

	/**
	 * Creates a <code>Pair</code> from the passed array.
	 * The size of the array must be 2.
	 *
	 * @param values the values to be used to construct this Pair
	 * @throws java.lang.IllegalArgumentException if the input array is null,
	 *             contains null values, or has != 2 elements.
	 */
	public ObjectPair(T[] values) {
		if (values.length == 2) {
			this.first = values[0];
			this.second = values[1];
		} else
			throw new IllegalArgumentException("Pair may only be created from an array of 2 elements");
	}

	/** {@inheritDoc} */
	@Override
	public boolean add(T o) {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(Object o) {
		return (this.first == o || this.first.equals(o) || this.second == o || this.second.equals(o));
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsAll(Collection<?> c) {
		if (c.size() > 2)
			return false;
		Iterator<?> iter = c.iterator();
		Object c_first = iter.next();
		Object c_second = iter.next();
		return this.contains(c_first) && this.contains(c_second);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;

		if (o instanceof ObjectPair) {
			ObjectPair<?> otherPair = (ObjectPair<?>) o;
			Object otherFirst = otherPair.getFirst();
			Object otherSecond = otherPair.getSecond();
			return (this.first == otherFirst ||
					(this.first != null && this.first.equals(otherFirst)))
					&&
					(this.second == otherSecond ||
							(this.second != null && this.second.equals(otherSecond)));
		}
		return false;
	}

	/**
	 * <p>
	 * Getter for the field <code>first</code>.
	 * </p>
	 *
	 * @return the first element.
	 */
	public T getFirst() {
		return this.first;
	}

	/**
	 * <p>
	 * Getter for the field <code>second</code>.
	 * </p>
	 *
	 * @return the second element.
	 */
	public T getSecond() {
		return this.second;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int hashCode = 1;
		hashCode = 31 * hashCode + (this.first == null ? 0 : this.first.hashCode());
		hashCode = 31 * hashCode + (this.second == null ? 0 : this.second.hashCode());
		return hashCode;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isEmpty() {
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public Iterator<T> iterator() {
		return new PairIterator();
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Pairs cannot be mutated");
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return 2;
	}

	/** {@inheritDoc} */
	@Override
	public Object[] toArray() {
		Object[] to_return = new Object[2];
		to_return[0] = this.first;
		to_return[1] = this.second;
		return to_return;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public <S> S[] toArray(S[] a) {
		S[] to_return = a;
		Class<?> type = a.getClass().getComponentType();
		if (a.length < 2)
			to_return = (S[]) java.lang.reflect.Array.newInstance(type, 2);
		to_return[0] = (S) this.first;
		to_return[1] = (S) this.second;

		if (to_return.length > 2)
			to_return[2] = null;
		return to_return;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "<" + this.first.toString() + ", " + this.second.toString() + ">";
	}
}
