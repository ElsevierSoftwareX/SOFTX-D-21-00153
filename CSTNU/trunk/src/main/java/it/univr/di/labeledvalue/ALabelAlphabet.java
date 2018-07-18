/**
 * 
 */
package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * A customizable alphabet, where elements are strings. Each element has an index (integer) that can be used to retrieve the element.
 * <p>
 * On 2017-4-13 it turns out that an A-Label introduced in the previous papers is not just a name of a node representing an upper-case letter (used in Morris's
 * rules), but it must be a set of upper-case letters.<br>
 * Moreover, such upper-case letters represent node name and, therefore, they should be strings instead of letters.
 * <p>
 * Class {@link ALabel} represents an A-Label. Such class needs an alphabet for building labels, {@link ALabelAlphabet}.
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

		@Override
		public int hashCode() {
			return this.name.hashCode();
		}

		@Override
		public String toString() {
			return this.name;
		}
	}


	/**
	 * A-Letter.
	 */
	public static final String ALETTER = "A-Za-z0-9α-μ_ ωΩ?";

	/**
	 * A-Letter range.
	 */
	public static final String ALETTER_RANGE = "[" + ALETTER + "]+";

	/**
	 * Default value for not found index.
	 */
	public static ALetter DEFAULT_ALETTER_RET_VALUE = null;

	/**
	 * Default value for not found name.
	 */
	public static byte DEFAULT_BYTE_RET_VALUE = -1;

	/**
	 * The empty a-letter.
	 */
	private static ALetter[] EMPTY_ALPHABET = {};

	/**
	 * Maximum size for the alphabet.
	 * Such limitation is dictated by the ALabel class implementation.
	 */
	public static final byte MAX_ALABELALPHABET_SIZE = ALabel.MAX_ALABELALPHABET_SIZE;

	@SuppressWarnings({ "unused", "javadoc" })
	private static final long serialVersionUID = 1L;

	/**
	 * The number of valid entries in {@link #value}.
	 */
	private byte size;

	/**
	 * The aletters of this alphabet.
	 * <br>
	 * Such array does not contain holes.
	 */
	private ALetter[] value;

	/**
	 * In order to speed up the #index method, a map int2Aletter is maintained.
	 */
	private Object2IntOpenHashMap<ALetter> value2int;

	/**
	 * Default constructor.
	 */
	public ALabelAlphabet() {
		this.size = 0;
		this.value = EMPTY_ALPHABET;
		this.value2int = new Object2IntOpenHashMap<>();
		this.value2int.defaultReturnValue(DEFAULT_BYTE_RET_VALUE);
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
	 * Cleans the map.
	 */
	public void clear() {
		for (int i = this.size; i-- != 0;) {
			this.value[i] = null;
		}
		this.value2int.clear();
		this.size = 0;
	}

	/**
	 * @param v
	 * @return true if v is present, false otherwise
	 */
	public boolean containsValue(ALetter v) {
		return index(v) >= 0;
	}

	/**
	 * @param k the index of the wanted a-letter
	 * @return the a-letter associated to index k, {@link #DEFAULT_ALETTER_RET_VALUE} it it does not exist
	 */
	public ALetter get(final byte k) {
		if (k < 0 || k >= this.size)
			return DEFAULT_ALETTER_RET_VALUE;
		return this.value[k];
	}

	/**
	 * @param name
	 * @return the index associated to name if it exists, {@link #DEFAULT_BYTE_RET_VALUE} otherwise
	 */
	public byte index(final ALetter name) {
		return (byte) this.value2int.getInt(name);
	}

	/**
	 * @return true is this does not contain a-letter
	 */
	public boolean isEmpty() {
		return this.size == 0;
	}

	/**
	 * Puts the element v in the map if not present.
	 * 
	 * @param v a non-null a-letter
	 * @return the index associate to the element v if {@code v} is present, {@value #DEFAULT_BYTE_RET_VALUE} if {@code v} is null
	 * @throws IllegalArgumentException if there are already {@link #MAX_ALABELALPHABET_SIZE} elements in the map
	 */
	public byte put(ALetter v) {
		if (v == null)
			return DEFAULT_BYTE_RET_VALUE;

		byte k = this.index(v);
		if (k >= 0) {
			return k;
		}
		if (this.size == this.value.length) {
			if (this.size == MAX_ALABELALPHABET_SIZE) {
				throw new IllegalArgumentException("It is not possible to add new elements!");
			}
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
	 * @return the current size of this alphabet
	 */
	public int size() {
		return this.size;
	}

	@Override
	public String toString() {
		return Arrays.toString(this.value);
	}
}