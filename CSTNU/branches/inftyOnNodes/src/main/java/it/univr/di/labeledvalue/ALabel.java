package it.univr.di.labeledvalue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

/**
 * Simple class to represent a <em>A-label</em> in the CSTNU framework.<br>
 * A A-label is a conjunction of zero or more <em>A-Letters</em> in the alphabet ({@link it.univr.di.labeledvalue.ALabelAlphabet}).<br>
 * A label without letters is called <em>empty label</em> and it is represented graphically as
 * {@link it.univr.di.labeledvalue.Constants#EMPTY_UPPER_CASE_LABEL}.<br>
 * <p>
 * <h2>Design assumptions</h2>
 * Since in CSTNU project the memory footprint of a label is an important aspect, after some experiments, I have found that the best way
 * to represent a A-label is to limit the possible A-letters to 64 distinct strings and to use one int for representing the state of A-letters composing a
 * A-label: present/absent.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class ALabel implements Comparable<ALabel>, Iterable<ALetter> {

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
	 * @author posenato
	 */
	private class ALabelItr implements Iterator<ALetter> {
		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		byte cursor = 0;

		/**
		 * The modCount value that the iterator believes that the backing
		 * List should have. If this expectation is violated, the iterator
		 * has detected concurrent modification.
		 */
		int expectedModCount = ALabel.this.modCount;

		/**
		 * Index of element returned by most recent call to next or
		 * previous. Reset to -1 if this element is deleted by a call
		 * to remove.
		 */
		byte lastRet = -1;

		/**
		 * 
		 */
		public ALabelItr() {
		}

		/**
		 * 
		 */
		final void checkForComodification() {
			if (ALabel.this.modCount != this.expectedModCount)
				throw new ConcurrentModificationException();
		}

		@Override
		public boolean hasNext() {
			return this.cursor < size();
		}

		@Override
		public ALetter next() {
			checkForComodification();
			byte i = this.cursor;
			while (getState(i) == State.absent)
				i++;
			ALetter next = getLetter(i);
			this.lastRet = i;
			this.cursor = (byte) (i + 1);
			return next;
		}

		@Override
		public void remove() {
			if (this.lastRet < 0)
				throw new IllegalStateException();
			checkForComodification();

			try {
				ALabel.this.remove(this.lastRet);
				if (this.lastRet < this.cursor)
					this.cursor--;
				this.lastRet = -1;
				this.expectedModCount = ALabel.this.modCount;
			} catch (IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}
	}

	/**
	 * An unmodifiable empty label.
	 * 
	 * @author posenato
	 */
	public static final class EmptyLabel extends ALabel {
		@SuppressWarnings({ "synthetic-access" })
		public EmptyLabel() {
			super();
		}

		@Override
		public int compareTo(ALabel l) {
			if (l == null)
				return 1;
			if (l.isEmpty())
				return 0;
			return -1;
		}

		@Override
		public boolean conjunct(ALetter aLetter) {
			throw new IllegalStateException("Empty label cannot be modified");
		}

		@Override
		public ALabel conjunction(final ALabel label) {
			if (label.isEmpty())
				return this;
			return ALabel.clone(label);
		}

		@Override
		public boolean contains(final ALabel label) {
			if (label == null || label.isEmpty())
				return true;
			return false;
		}

		@Override
		public boolean contains(final ALetter letter) {
			if (letter == null)
				return true;
			return false;
		}

		// @Override
		// public ALetter[] getAllLetter() {
		// return new ALetter[0];
		// }

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public Iterator<ALetter> iterator() {
			return null;
		}

		@Override
		public void remove(final ALetter letter) {
			return;
		}

		@Override
		public void remove(final ALetter[] letter) {
			return;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public String toString() {
			return Constants.EMPTY_UPPER_CASE_LABELstring;
		}
	}

	/**
	 * ALabel separator '∙'.
	 */
	public static final char ALABEL_SEPARATOR = '∙';

	/**
	 * ALabel separator '∙'.
	 */
	public static final String ALABEL_SEPARATORstring = String.valueOf(ALABEL_SEPARATOR);

	/**
	 * Regular expression representing an A-Label.
	 * The re checks only that label chars are allowed.
	 */
	public static final String ALABEL_RE = "[" + ALabelAlphabet.ALETTER + ALABEL_SEPARATORstring + "]+|" + Constants.EMPTY_UPPER_CASE_LABELstring;

	/**
	 * Maximum size for the alphabet.
	 * Such limitation is dictated by the ALabel class implementation.
	 */
	public static final byte MAX_ALABELALPHABET_SIZE = 64;

	/**
	 * A constant empty label to represent an empty label that cannot be modified.
	 */
	public static final ALabel emptyLabel = new EmptyLabel();

	/**
	 * Validator for graphical interface
	 */
	public static final Validator<String> labelValidator = new Validator<String>() {

		@Override
		public Class<String> modelType() {
			return String.class;
		}

		@Override
		public void validate(final Problems problems, final String compName, final String model) {
			if ((model == null) || (model.length() == 0))
				return;

			if (!Pattern.matches(ALABEL_RE, model)) {
				problems.append("Highlighted label is not well-formed.");
				return;
			}
		}
	};

	/**
	 * logger
	 */
	private static final Logger LOG = Logger.getLogger("ALabel");

	/**
	 * In order to have a correct copy of a a-label.
	 * 
	 * @param label
	 * @return a distinct equal copy of label
	 */
	static final public ALabel clone(final ALabel label) {
		if (label == null || label.isEmpty())
			return ALabel.emptyLabel;
		return new ALabel(label);
	}

	/**
	 * Parse a string representing a A-label and return an equivalent A-Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link it.univr.di.labeledvalue.Label#LABEL_RE}.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param alphabet A {@link ALabelAlphabet} to use. If null, it will be generated and added to the return label.
	 * @return a Label object corresponding to the label string representation.
	 */
	public static final ALabel parse(String s, ALabelAlphabet alphabet) {
		if (s == null)
			return null;
		int n = s.length();
		if (n == 0 || Pattern.matches(Constants.EMPTY_UPPER_CASE_LABELstring, s))
			return ALabel.emptyLabel;
		if (!Pattern.matches(ALABEL_RE, s))
			return null;
		// split all possible letters
		String[] letters = s.split(ALABEL_SEPARATORstring);
		int size = letters.length;
		if (size == 0)
			return ALabel.emptyLabel;

		// build alphabet
		if (alphabet == null) {
			alphabet = new ALabelAlphabet();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer("Created a new ALabelAlphabet: " + alphabet);
				}
			}
		}

		// build alabel
		ALabel alabel = new ALabel(alphabet);
		for (byte i = 0; i < size; i++) {
			alabel.conjunct(new ALetter(letters[i]));
		}

		return alabel;
	}

	/**
	 * Alphabet to map the name into A-letter
	 */
	private ALabelAlphabet alphabet;

	/**
	 * One long has 64 bits.<br>
	 * Each position is associated to a A-Letter.<br>
	 * 
	 * <pre>
	 * Status of i-th A-Letter
	 *              bit0[i]
	 * not present          0
	 * present              1
	 * </pre>
	 */
	private long bit0;

	/**
	 * Number of A-letters in the label
	 * Value -1 means that the size has to be calculated!
	 */
	private byte cacheOfSize;

	/**
	 * Index of the last significant A-Letter of label.
	 * On 2016-03-30 I showed by SizeofUtilTest.java that using byte it is possible to define also 'size' field without incrementing the memory footprint of the
	 * object.
	 */
	private byte maxIndex;

	/**
	 * The number of times this ALabel has been <i>structurally modified</i>.
	 * Structural modifications are those that change the size, or otherwise perturb it in such a fashion that iterations in
	 * progress may yield incorrect results.
	 */
	protected transient int modCount;

	/**
	 * Just for internal use
	 */
	private ALabel() {
		this.bit0 = 0;
		this.maxIndex = -1;
		this.cacheOfSize = 0;
		this.modCount = 0;
	}

	/**
	 * Constructs a label cloning the given label l.
	 * 
	 * @param label the label to clone. It cannot be null or empty!
	 */
	private ALabel(final ALabel label) {
		this();
		if (label == null || label.isEmpty())
			throw new IllegalArgumentException("Label cannot be null or empty!");
		this.alphabet = label.alphabet;// alphabet has to be shared!
		this.bit0 = label.bit0;
		this.maxIndex = label.maxIndex;
		this.cacheOfSize = label.cacheOfSize;
	}

	/**
	 * Default constructor using a given alphabet.
	 * 
	 * @param alphabet1
	 */
	public ALabel(ALabelAlphabet alphabet1) {
		this();
		if (alphabet1 == null)
			throw new IllegalArgumentException("Alphabet cannot be null!");
		this.alphabet = alphabet1;
	}

	/**
	 * Builds an a-label using the a-letter 'l' and 'alphabet'.
	 * Be aware that if 'l' is not present into alphabet, it will be added.
	 * 
	 * @param l first a-letter of label
	 * @param alphabet1 alphabet of a-letters. It may be empty!
	 */
	public ALabel(ALetter l, ALabelAlphabet alphabet1) {
		this(alphabet1);
		this.conjunct(l);
	}

	/**
	 * Helper constructor. It calls ALabel(ALetter, ALabelAlphabet).
	 * Be aware that if 's' is not present into alphabet as a-letter, it will be added as a-letter.
	 * 
	 * @param s the string to add.
	 * @param alphabet1 alphabet of a-letters. It may be empty!
	 */
	public ALabel(String s, ALabelAlphabet alphabet1) {
		this(new ALetter(s), alphabet1);
	}

	/**
	 * Makes the label empty.
	 */
	public void clear() {
		this.bit0 = 0;
		this.maxIndex = -1;
		this.cacheOfSize = 0;
	}

	/**
	 * In order to speed up this method and considering that the {@link ALabelAlphabet} order may be not the expected alphabetic one,
	 * (first letter in an {@link ALabelAlphabet} can be 'nodeZ' and the last one 'aNode'), the order of labels is given w.r.t. their indexes in
	 * the their {@link ALabelAlphabet}.
	 */
	@Override
	public int compareTo(final ALabel label) {
		if (label == null)
			return 1;
		if (label.isEmpty()) {
			if (this.isEmpty())
				return 0;
			return 1;
		}
		if (this.alphabet != label.alphabet)
			throw new IllegalArgumentException("Comparison is not possible because the given label has a different alphabet from the current one!");
		return Long.compareUnsigned(this.bit0, label.bit0);
	}

	/**
	 * It conjuncts <code>a-letter</code> to this.
	 *
	 * @param aLetter the a-letter to conjunct.
	 * @return true if a-letter is added, false otherwise.
	 */
	public boolean conjunct(final ALetter aLetter) {
		if (aLetter == null)
			return false;
		byte propIndex = getIndex(aLetter);
		if (propIndex < 0)
			propIndex = this.alphabet.put(aLetter);
		this.set(propIndex, State.present);
		return true;
	}

	/**
	 * Conjuncts <code>a-label</code> to <code>this</code> and returns the result without modifying <code>this</code>.
	 *
	 * @param label the label to conjunct
	 * @return a new label with the conjunction of 'this' and 'label'.
	 */
	public ALabel conjunction(final ALabel label) {
		if (label == null || (!this.isEmpty() && !label.isEmpty() && this.alphabet != label.alphabet))
			return null;
		if (this.isEmpty()) {
			return new ALabel(label);
		}
		if (label.isEmpty()) {
			return new ALabel(this);
		}

		final ALabel newLabel = new ALabel(this.alphabet);
		newLabel.bit0 = this.bit0 | label.bit0;
		newLabel.maxIndex = (label.maxIndex > this.maxIndex) ? label.maxIndex : this.maxIndex;
		newLabel.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return newLabel;
	}

	/**
	 * L<sub>1</sub> contains L<sub>2</sub> if L<sub>1</sub> contains all a-letters of L<sub>2</sub>.
	 *
	 * @param label the label to check
	 * @return true if this contains label.
	 */
	public boolean contains(final ALabel label) {
		if ((label == null) || label.isEmpty())
			return true;
		if (this.alphabet != label.alphabet)
			throw new IllegalArgumentException(
					"label is not defined using the same alphabet: " + this.alphabet.toString() + " vs " + label.alphabet.toString());
		// int max = (this.maxIndex > label.maxIndex) ? this.maxIndex : label.maxIndex;
		// for (byte i = (byte) (max + 1); (--i) >= 0;) {
		// State thisState = getState(i);
		// State labelState = label.getState(i);
		// if (thisState == labelState || labelState == State.absent)
		// continue;
		// if (thisState == State.absent)
		// return false;
		// }
		// return true;
		// 1st xor shows different bits. Masking them with the complement of this, shows the bits 1 in label.bit0 that are not present in this.bit0.
		return (((this.bit0 ^ label.bit0) & (~this.bit0)) == 0);
	}

	/**
	 * @param name the proposition to check.
	 * @return true if this contains proposition in any state: straight, negated or unknown.
	 */
	public boolean contains(final ALetter name) {
		if (name == null)
			return true;
		return this.getState(getIndex(name)) != State.absent;
	}

	/**
	 * Compare the letter with an a-letter name.
	 * 
	 * @param name
	 * @return true if the label is equal to the a-letter name.
	 */
	public boolean equals(final ALetter name) {
		if (name == null)
			return false;
		return this.size() == 1 && this.getState(getIndex(name)) == State.present;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ALabel))
			return false;
		ALabel alabel = (ALabel) obj;
		if (this.isEmpty() && alabel.isEmpty())
			return true;
		return this.alphabet == alabel.alphabet && this.bit0 == alabel.bit0;
	}

	/**
	 * @return the alphabet
	 */
	public ALabelAlphabet getAlphabet() {
		return this.alphabet;
	}

	/**
	 * @return The array of a-letters of present literals in this label.
	 *         public ALetter[] getAllLetter() {
	 *         ALetter[] letters = new ALetter[size()];
	 *         int j = 0;
	 *         for (byte i = 0; i <= this.maxIndex; i++) {
	 *         if (getState(i) != State.absent) {
	 *         letters[j++] = this.getLetter(i);
	 *         }
	 *         }
	 *         return letters;
	 *         }
	 */

	/**
	 * @param letter
	 * @return the index of the letter in the alphabet.
	 */
	private final byte getIndex(final ALetter letter) {
		return this.alphabet.index(letter);
	}

	/**
	 * @param letterIndex the index of the literal to retrieve.
	 * @return the letter with index literalIndex.
	 */
	final ALetter getLetter(final byte letterIndex) {
		return this.alphabet.get(letterIndex);
	}

	/**
	 * @param letterIndex the index of the literal to retrieve.
	 * @return the status of literal with index literalIndex. If the literal is not present, it returns {@link State#absent}, otherwise .{@link State#present}.
	 */
	final State getState(final byte letterIndex) {
		long mask = 1L << letterIndex;
		return ((this.bit0 & mask) != 0) ? State.present : State.absent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// It is impossible to guarantee a unique hashCode for each possible label.
		return (int) (31 * this.maxIndex + this.bit0);
	}

	/**
	 * @param label
	 * @return the label containing common ALetter between this and label.
	 */
	public ALabel intersect(final ALabel label) {
		if ((label == null) || this.isEmpty() || label.isEmpty())
			return emptyLabel;
		if (this.alphabet != label.alphabet)
			throw new IllegalArgumentException(
					"alabel is not defined using the same alphabet: " + this.alphabet.toString() + " vs " + label.alphabet.toString());
		ALabel newl = new ALabel(this.alphabet);
		newl.bit0 = this.bit0 & label.bit0;
		if (newl.bit0 == 0)
			return newl;
		newl.maxIndex = (this.maxIndex > label.maxIndex) ? this.maxIndex : label.maxIndex;
		while (newl.maxIndex >= 0 && (newl.getState(newl.maxIndex) == State.absent))
			newl.maxIndex--;
		return newl;
	}

	/**
	 * @return true if the label contains no literal.
	 */
	public boolean isEmpty() {
		return this.bit0 == 0;
	}

	@Override
	public Iterator<ALetter> iterator() {
		return new ALabelItr();
	}

	/**
	 * @param l
	 * @return true if a-letter is added, false otherwise.
	 */
	public boolean put(final ALetter l) {
		return conjunct(l);
	}

	/**
	 * It removes all a-letters in <b>aLabel</b> from the current label.
	 *
	 * @param aLabel a-Label to remove.
	 */
	public void remove(final ALabel aLabel) {
		if (aLabel == null)
			return;
		for (ALetter aletter : aLabel) {
			set(getIndex(aletter), State.absent);
		}
	}

	/**
	 * It removes a-letter if it is present, otherwise it does nothing.
	 *
	 * @param letter the letter to remove.
	 */
	public void remove(final ALetter letter) {
		if (letter == null)
			return;
		set(getIndex(letter), State.absent);
	}

	/**
	 * It removes all a-letters in <b>inputSet</b> from the current label.
	 *
	 * @param inputSet a-letters to remove.
	 */
	public void remove(final ALetter[] inputSet) {
		if (inputSet == null)
			return;
		for (int i = inputSet.length; (--i) >= 0;) {
			set(getIndex(inputSet[i]), State.absent);
		}
	}

	/**
	 * @param index the index of the letter to remove.
	 */
	final void remove(final byte index) {
		set(index, State.absent);
	}

	/**
	 * @param aLetterIndex the index of the literal to update.
	 * @param letterStatus the new state.
	 */
	private final void set(final byte aLetterIndex, final State letterStatus) {
		/**
		 * <pre>
		 * Status of i-th literal
		 *             bit0[i]
		 * absent       0 
		 * present      1
		 * </pre>
		 */
		if (aLetterIndex < 0 || aLetterIndex > MAX_ALABELALPHABET_SIZE)
			return;
		long mask = 1L << aLetterIndex;
		switch (letterStatus) {
		case present:
			if (((this.bit0) & mask) == 0)
				this.cacheOfSize++;
			this.bit0 |= mask;
			if (this.maxIndex < aLetterIndex)
				this.maxIndex = aLetterIndex;
			return;
		case absent:
		default:
			if (((this.bit0) & mask) != 0)
				this.cacheOfSize--;
			mask = ~mask;
			this.bit0 &= mask;
			if (this.maxIndex == aLetterIndex) {
				long u = this.bit0;
				mask = ~mask;
				do {
					this.maxIndex--;
					mask = mask >>> 1;
				} while ((u & mask) == 0 && this.maxIndex >= 0);
			}
			return;
		}
	}

	/**
	 * @return Return the number of literals of the label
	 */
	public int size() {
		if (this.cacheOfSize >= 0) {
			return this.cacheOfSize;
		}
		// byte _cacheOfSize = 0;
		// long or = this.bit0;
		// for (int i = this.maxIndex + 1; (--i) >= 0;) {
		// _cacheOfSize += (or & 1);
		// or = or >>> 1;
		// }
		this.cacheOfSize = (byte) Long.bitCount(this.bit0);
		return this.cacheOfSize;
	}

	/**
	 * @return lower case version
	 */
	public String toLowerCase() {
		return this.toString().toLowerCase();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.isEmpty())
			return Constants.EMPTY_UPPER_CASE_LABELstring;
		final StringBuilder s = new StringBuilder();
		State st;
		for (byte i = 0, j = 0; i <= this.maxIndex; i++) {
			st = getState(i);
			if (st == State.present) {
				s.append(getLetter(i));
				if (++j < this.size()) {
					s.append(ALABEL_SEPARATOR);
				}
			}
		}
		return s.toString();
	}

	/**
	 * @return upper case version
	 */
	public String toUpperCase() {
		return this.toString().toUpperCase();
	}
}
