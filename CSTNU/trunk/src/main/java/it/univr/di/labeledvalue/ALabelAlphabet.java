/**
 * 
 */
package it.univr.di.labeledvalue;

import java.util.regex.Pattern;

/**
 * In 2017-4-13 turns out that an A-Label is not just a name of a node representing an upper case letter in Morris's rules, but it must be a set of such upper
 * case letters.
 * Moreover, in real, such upper case letters represent node name and, therefore, they should be strings instead of letters.
 * Class {@link ALabel} represents an A-Label. Such class needs an alphabet for building labels. This class implements a customizable alphabet mapping strings
 * to integer.
 * A user can build a proper alphabet that can be used by {@link ALabel} for building A-Label(s).
 * 
 * @author posenato
 */
public class ALabelAlphabet {

	/**
	 * @author posenato
	 */
	public static enum State {
		/**
		 * Not present. Useful only for BitLabel class.
		 */
		absent,
		/**
		 * A negated literal is true if the truth value assigned to its proposition letter is false, false otherwise.
		 */
		present;

		/**
		 * @param st
		 * @return true if this is opposite to state st. False, otherwise.
		 */
		public boolean isComplement(State st) {
			// return (this == State.straight && st == State.negated) || (this == State.negated && st == State.straight);
			return (this.ordinal() ^ st.ordinal()) == 1;
		}

		@Override
		public String toString() {
			return "";
		}
	}

	/**
	 * @author posenato
	 */
	public static class ALetter implements Comparable<ALetter> {
		/**
		 * 
		 */
		public final String name;

		/**
		 * @param s
		 */
		public ALetter(String s) {
			if (s == null || s.isEmpty())
				throw new IllegalArgumentException("A ALetter cannot be null or empty");
			if (!Pattern.matches(Constants.ALETTER_RANGE, s))
				throw new IllegalArgumentException("The argoment " + s + " must be in the regular-expression range: " + Constants.ALETTER_RANGE);
			this.name = s;
		}

		public boolean equals(Object o) {
			if (o == null || !(o instanceof ALetter))
				return false;
			return this.name.equals(((ALetter) o).name);
		}

		@Override
		public int compareTo(ALetter o) {
			return this.name.compareTo(o.name);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return this.name;
		}

		/**
		 * @return upper case version
		 */
		public String toUpperCase() {
			return this.name.toUpperCase();
		}

		/**
		 * @return lower case version
		 */
		public String toLowerCase() {
			return this.name.toLowerCase();
		}

	}

	/**
	 * Default value for not found name
	 */
	public static ALetter DEFAULT_RET_VALUE = null;

	/**
	 * Maximum size for the alphabet.
	 * Such limitation is dictated by the ALabel class implementation.
	 */
	public static final byte MAX_ALABELALPHABET_SIZE = 64;

	/**
	 * 
	 */
	private static ALetter[] EMPTY_ALFABET = {};

	@SuppressWarnings({ "unused", "javadoc" })
	private static final long serialVersionUID = 1L;

	/**
	 * The number of valid entries in {@link #value}.
	 */
	private byte size;
	/**
	 * The values.
	 */
	private ALetter[] value;

	/**
	 * 
	 */
	public ALabelAlphabet() {
		this.size = 0;
		this.value = EMPTY_ALFABET;
	}

	/**
	 * @param size initial size of alphabet
	 */
	public ALabelAlphabet(int size) {
		this();
		if (size > MAX_ALABELALPHABET_SIZE)
			throw new IllegalArgumentException("Dimension exceeds the maximum capacity!");
		this.value = new ALetter[size];
	}

	/**
	 * @param alphabet to clone.
	 */
	public ALabelAlphabet(ALabelAlphabet alphabet) {
		this();
		if (alphabet == null || alphabet.size == 0)
			return;
		this.size = alphabet.size;
		this.value = new ALetter[this.size];
		for (byte i = this.size; i-- != 0;) {
			this.value[i] = alphabet.get(i);
		}
	}

	/**
	 * Clean the map
	 */
	public void clear() {
		for (int i = this.size; i-- != 0;) {
			this.value[i] = null;
		}
		this.size = 0;
	}

	/**
	 * @param k
	 * @return true if index k is present, false otherwise
	 */
	// public boolean containsKey(final int k) {
	// return findKey(k) >= 0;
	// }

	/**
	 * @param v
	 * @return true if v is present, false otherwise.
	 */
	public boolean containsValue(ALetter v) {
		return index(v) >= 0;
	}

	/**
	 * @param k
	 * @return the string associated to index k
	 */
	public ALetter get(final byte k) {
		if (k < 0 || k >= this.size)
			return DEFAULT_RET_VALUE;
		return this.value[k];
	}

	/**
	 * @param name
	 * @return the index associated to name if it exists, < 0 otherwise.
	 */
	public byte index(final ALetter name) {
		if (name == null)
			return -1;
		// return (byte) Arrays.binarySearch(this.value, 0, this.size, name);
		for (byte i = this.size; i-- != 0;)
			if ((this.value[i]).equals(name))
				return i;
		return -1;
	}

	/**
	 * @return the current size of alphabet
	 */
	public boolean isEmpty() {
		return this.size == 0;
	}

	/**
	 * Put the element v in the map if not present.
	 * 
	 * @param v
	 * @return the index associate to the element v.
	 */
	public byte put(ALetter v) {
		if (v == null)
			return -1;

		byte k = this.index(v);
		if (k >= 0) {
			return k;
		}
		if (this.size == this.value.length) {
			if (this.size == MAX_ALABELALPHABET_SIZE) {
				throw new IllegalArgumentException("It is not possible to add new elements!");
			}
			final ALetter[] newValue = new ALetter[this.size == 0 ? 2 : this.size * 2];
			for (int i = this.size; i-- != 0;) {
				newValue[i] = this.value[i];
			}
			this.value = newValue;
		}
		k = this.size;
		this.value[k] = v;
		this.size++;
		// Arrays.sort(this.value, 0, this.size); NON SI POSSONO ordinare!
		// return this.index(v);
		return k;
	}

	/**
	 * Remove the name if present.
	 * It is important to make it append only!
	 * 
	 * @param v
	 * @return v if removed, {@link #defRetValue} otherwise.
	 *         public ALetter remove(final ALetter v) {
	 *         final int oldPos = index(v);
	 *         if (oldPos < 0)
	 *         return defRetValue;
	 *         final ALetter oldValue = this.value[oldPos];
	 *         final int tail = this.size - oldPos - 1;
	 *         for (int i = 0; i < tail; i++) {
	 *         this.value[oldPos + i] = this.value[oldPos + i + 1];
	 *         }
	 *         this.size--;
	 *         this.value[this.size] = null;
	 *         return oldValue;
	 *         }
	 */

	/**
	 * @return the current size of alphabet
	 */
	public int size() {
		return this.size;
	}

}