/**
 * 
 */
package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * In 2017-4-13 turns out that an A-Label introduced in the papers is not just a name of a node representing an upper case letter in Morris's rules, but it must
 * be a set of such upper case letters.<br>
 * Moreover, such upper case letters represent node name and, therefore, they should be strings instead of letters.
 * Class {@link ALabel} represents an A-Label. Such class needs an alphabet for building labels, {@link ALabelAlphabet}.
 * This class implements a customizable alphabet mapping strings to integer for speeding up matching.
 * A user can build a proper alphabet that can be used by {@link ALabel} for building A-Label(s).
 * 
 * @author posenato
 */
public class ALabelAlphabet {

	/**
	 * ALetter makes simpler to check if a node name is appropriate.<br>
	 * ALabelAlphabet is built over ALetter.
	 * 
	 * @author posenato
	 */
	public static class ALetter implements Comparable<ALetter> {
		/**
		 * 
		 */
		public final String name;

		/**
		 * Constructor by copy
		 * 
		 * @param a
		 */
		public ALetter(ALetter a) {
			this.name = a.name;
		}

		/**
		 * @param s
		 */
		public ALetter(String s) {
			if (s == null || s.isEmpty())
				throw new IllegalArgumentException("A ALetter cannot be null or empty");
			if (!Pattern.matches(ALabelAlphabet.ALETTER_RANGE, s))
				throw new IllegalArgumentException("The argument " + s + " must be in the regular-expression range: " + ALabelAlphabet.ALETTER_RANGE);
			this.name = s;
		}

		@Override
		public int compareTo(ALetter o) {
			return this.name.compareTo(o.name);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (!(o instanceof ALetter))
				return false;
			return this.name.equals(((ALetter) o).name);
		}

		/**
		 * @param o
		 * @return true if String is equal to this letter.
		 */
		public boolean equals(String o) {
			if (o == null)
				return false;
			return this.name.equals(o);
		}

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		/**
		 * @return lower case version
		 */
		public String toLowerCase() {
			return this.name.toLowerCase();
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

	}

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

		@Override
		public String toString() {
			return "";
		}
	}

	/**
	 * A-Letter
	 */
	public static final String ALETTER = "A-Za-z0-9α-μ_ ωΩ?";

	/**
	 * A-Letter range
	 */
	public static final String ALETTER_RANGE = "[" + ALETTER + "]+";

	/**
	 * Default value for not found index
	 */
	public static ALetter DEFAULT_ALETTER_RET_VALUE = null;

	/**
	 * Default value for not found name
	 */
	public static byte DEFAULT_BYTE_RET_VALUE = -1;

	/**
	 * 
	 */
	private static ALetter[] EMPTY_ALPHABET = {};

	/**
	 * Maximum size for the alphabet.
	 * Such limitation is dictated by the ALabel class implementation.
	 */
	public static final byte MAX_ALABELALPHABET_SIZE = 64;

	@SuppressWarnings({ "unused", "javadoc" })
	private static final long serialVersionUID = 1L;

	/**
	 * The number of valid entries in {@link #value}.
	 */
	private byte size;

	/**
	 * The aletters of this alphabet. Such array does not contain holes.
	 */
	private ALetter[] value;

	/**
	 * In order to speed up the #index method, a map int2Aletter is maintained.
	 */
	private Object2IntOpenHashMap<ALetter> value2int;

	/**
	 * 
	 */
	public ALabelAlphabet() {
		this.size = 0;
		this.value = EMPTY_ALPHABET;
		this.value2int = new Object2IntOpenHashMap<>();
		this.value2int.defaultReturnValue(DEFAULT_BYTE_RET_VALUE);
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
		this.value2int = new Object2IntOpenHashMap<>(MAX_ALABELALPHABET_SIZE);
		this.value2int.defaultReturnValue(DEFAULT_BYTE_RET_VALUE);
		for (byte i = this.size; i-- != 0;) {
			this.value[i] = alphabet.get(i);
			this.value2int.put(this.value[i], i);
		}
	}

	/**
	 * @param size initial size of alphabet
	 */
	public ALabelAlphabet(int size) {
		this();
		if (size > MAX_ALABELALPHABET_SIZE)
			throw new IllegalArgumentException("Dimension exceeds the maximum capacity!");
		this.value = new ALetter[size];
		this.value2int = new Object2IntOpenHashMap<>(size);
		this.value2int.defaultReturnValue(DEFAULT_BYTE_RET_VALUE);
	}

	/**
	 * Clean the map
	 */
	public void clear() {
		for (int i = this.size; i-- != 0;) {
			this.value[i] = null;
		}
		this.value2int.clear();
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
	 * @return the aletter associated to index k
	 */
	public ALetter get(final byte k) {
		if (k < 0 || k >= this.size)
			return DEFAULT_ALETTER_RET_VALUE;
		return this.value[k];
	}

	/**
	 * @param name
	 * @return the index associated to name if it exists, < 0 otherwise.
	 */
	public byte index(final ALetter name) {
		// if (name == null)
		// return -1;
		// // return (byte) Arrays.binarySearch(this.value, 0, this.size, name);
		// for (byte i = this.size; i-- != 0;)
		// if ((this.value[i]).equals(name))
		// return i;
		// return -1;
		return (byte) this.value2int.getInt(name);
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
			// final ALetter[] newValue = new ALetter[this.size + 16];
			// for (int i = this.size; i-- != 0;) {
			// newValue[i] = this.value[i];
			// }
			// this.value = newValue;
			this.value = Arrays.copyOf(this.value, this.size + MAX_ALABELALPHABET_SIZE / 4);
		}
		k = this.size;
		this.value[k] = v;
		this.value2int.put(v, k);
		this.size++;
		// Arrays.sort(this.value, 0, this.size); NON SI POSSONO ordinare perché gli indici possono essere già stati usati!
		// return this.index(v);
		return k;
	}

	/**
	 * Remove the name if present.
	 * Since indexes associate to others names can be already used, it is not possible to modify them.
	 * Therefore, this alphabet is append only!
	 * 
	 * @param v
	 * @return v if removed, null otherwise.
	 */
	// public ALetter remove(final ALetter v) {
	// final int oldPos = index(v);
	// if (oldPos < 0)
	// return null;
	// final ALetter oldValue = this.value[oldPos];
	// final int tail = this.size - oldPos - 1;
	// for (int i = 0; i < tail; i++) {
	// this.value[oldPos + i] = this.value[oldPos + i + 1];
	// }
	// this.size--;
	// this.value[this.size] = null;
	// return oldValue;
	// }

	/**
	 * @return the current size of alphabet
	 */
	public int size() {
		return this.size;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.value);
	}
}