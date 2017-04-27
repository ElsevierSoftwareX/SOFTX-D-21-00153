package it.univr.di.labeledvalue;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.ALabelAlphabet.State;

/**
 * <p>
 * Simple class to represent a <em>A-label</em> in the CSTNU framework.<br>
 * A A-label is a conjunction of zero or more <em>A-Letters</em> in the alphabet ({@link it.univr.di.labeledvalue.ALabelAlphabet}).<br>
 * A label without letters is called <em>empty label</em> and it is represented graphically as
 * {@link it.univr.di.labeledvalue.Constants#EMPTY_UPPER_CASE_LABEL}.<br>
 * <p>
 * Design assumptions
 * Since in CSTNU project the memory footprint of a label is an important aspect, after some experiments, I have found that the best way
 * to represent a A-label is to limit the possible A-letters to 64 distinct strings and to use one int for representing the state of A-letters composing a
 * A-label: present/absent.
 * <p>
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class ALabel implements Comparable<ALabel>, Iterable<ALetter> {

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

		public boolean hasNext() {
			return this.cursor < size();
		}

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
		@SuppressWarnings({ "synthetic-access", "javadoc" })
		public EmptyLabel() {
			super();
		}

		@Override
		public boolean conjunct(ALetter aLetter) {
			throw new IllegalStateException("Empty label cannot be modified");
		}

		@Override
		public int compareTo(ALabel l) {
			if (l.isEmpty())
				return 0;
			return -1;
		}

		@Override
		public ALabel conjunction(final ALabel label) {
			return new ALabel(label);
		}

		@Override
		public boolean contains(final ALabel label) {
			if (label.isEmpty())
				return true;
			return false;
		}

		@Override
		public boolean contains(final ALetter letter) {
			return false;
		}

		@Override
		public ALetter[] getAllLetter() {
			return new ALetter[0];
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
			return String.valueOf(Constants.EMPTY_UPPER_CASE_LABEL);
		}
	}

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

			if (!Pattern.matches(Constants.ALABEL_RE, model)) {
				problems.append("Highlighted label is not well-formed.");
				return;
			}
		}
	};

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ALabel.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ALabelAlphabet alpha = new ALabelAlphabet();
		ALabel a = new ALabel(new ALetter("A"), alpha);
		System.out.println("ALabel A: " + a);

		ALabel b = new ALabel(new ALetter("b"), a.getAlphabet());
		System.out.println("ALabel b: " + b);

		ALabel c = new ALabel(new ALetter("C"), b.getAlphabet());
		System.out.println("ALabel C: " + c);

		System.out.println("ALabel b: " + b);

		a = a.conjunction(b);
		System.out.println("ALabel dopo conjunction con b [Ab]: " + a);

		ALabel e = ALabel.emptyLabel;
		System.out.println("Empty label: " + e);

		a = a.conjunction(e);
		System.out.println("ALabel dopo conjunction con empty [Ab]: " + a);

		System.out.println("Empty conjunction con Ab [Ab]: " + e.conjunction(a));

		a = a.conjunction(a);
		System.out.println("ALabel dopo conjunction con a [Ab]: " + a);

		a = a.conjunction(c);
		System.out.println("ALabel dopo conjunction con C [AbC]: " + a);

		a.remove(new ALetter("C"));
		System.out.println("ALabel dopo rimozione C [Ab]: " + a);

		System.out.println("ALabel contiene 'b' [true]: " + a.contains(new ALetter("b")));
		System.out.println("ALabel contiene 'C' [false]: " + a.contains(new ALetter("C")));

		System.out.println("ALabel confronto con 'C' [<0]: " + a.compareTo(c));
		System.out.println("ALabel confronto a [0]: " + a.compareTo(a));
		System.out.println("C confronto con 'A∙b' [>0]: " + c.compareTo(a));
		a.conjunct(new ALetter("C"));
		System.out.println("ALabel A: " + a);
		for (ALetter l : a) {
			System.out.println("Letter: " + l);
		}

		System.out.println("A intersect with empty: " + a.intersect(emptyLabel));
		System.out.println("A intersect with a: " + a.intersect(a));
		System.out.println("A intersect with b: " + a.intersect(b));
		System.out.println("A intersect with c: " + a.intersect(c));
		System.out.println("A intersect with 'A': " + a.intersect(new ALabel("A", a.alphabet)));

	}

	/**
	 * Parse a string representing a A-label and return an equivalent A-Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link it.univr.di.labeledvalue.Constants#LABEL_RE}.
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
		if (!Pattern.matches(Constants.ALABEL_RE, s))
			return null;
		// split all possible letters
		String[] letters = s.split(Constants.ALABEL_SEPARATOR);
		int size = letters.length;
		if (size == 0)
			return ALabel.emptyLabel;

		// build alphabet
		if (alphabet == null) {
			alphabet = new ALabelAlphabet(size);
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
	 * Index of the last significant A-Letter of label w.r.t. lexicographical order.
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
	 * @param label the label to clone. If null, this will be an empty label.
	 */
	public ALabel(final ALabel label) {
		this();
		if (label == null || label == emptyLabel) {
			return;
		}
		this.alphabet = label.alphabet;// alphabet has to be shared!
		this.bit0 = label.bit0;
		this.maxIndex = label.maxIndex;
		this.cacheOfSize = label.cacheOfSize;
	}

	/**
	 * Default constructor.
	 * 
	 * @param alphab
	 */
	public ALabel(ALabelAlphabet alphab) {
		this();
		if (alphab == null)
			throw new IllegalArgumentException("Alphabet cannot be null!");
		this.alphabet = alphab;
	}

	/**
	 * Default constructor.
	 * 
	 * @param l firs a-letter of label
	 * @param alphab alphabet of a-letters. It could be empty!
	 */
	public ALabel(ALetter l, ALabelAlphabet alphab) {
		this(alphab);
		this.conjunct(l);
	}

	/**
	 * Helper constructor. It calls {@link #ALabel(ALetter, ALabelAlphabet)}
	 * 
	 * @param s
	 * @param alphabet
	 */
	public ALabel(String s, ALabelAlphabet alphabet) {
		this(new ALetter(s), alphabet);
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
	 * {@inheritDoc} Determines a lexicographical order between labels based on the natural order of type {@link Literal}.
	 */
	@Override
	public int compareTo(final ALabel label) {
		if (label == null)
			return 1;
		if (this.alphabet != label.alphabet)
			throw new IllegalArgumentException("Comparison is not possible because the input label has a different alphabet!");
		byte i = 0, j = 0, cmp = 0;
		State thisState, labelState;
		
		while (i <= this.maxIndex && j <= label.maxIndex) {
			while ((thisState = getState(i)) == State.absent && i <= this.maxIndex) {
				i++;
			}
			while ((labelState = label.getState(j)) == State.absent && j <= label.maxIndex) {
				j++;
			}
			if (i != j)
				return i - j;
			cmp = (byte) thisState.compareTo(labelState);
			if (cmp != 0)
				return cmp;
			i++;
			j++;
		}
		if (i > this.maxIndex) {
			if (j <= label.maxIndex)
				return -1;
			return 0;
		}
		// i<=maxIndex
		if (j > label.maxIndex)
			return 1;
		return 0;// impossible
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
	 * Conjuncts <code>label</code> to this if <code>this</code> is consistent with <code>label</code> and returns the results without modifying
	 * <code>this</code>.
	 *
	 * @param label the label to conjunct
	 * @return a new label with the conjunction of 'this' and 'label' if they are consistent, null otherwise.<br>
	 *         null also if this or label contains unknown literals. 'this' is not altered by this method.
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

		long unionB0 = this.bit0 | label.bit0;

		final ALabel newLabel = new ALabel(this.alphabet);
		newLabel.bit0 = unionB0;
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
			throw new IllegalArgumentException("label is not defined using the same alphabet!");
		int max = (this.maxIndex > label.maxIndex) ? this.maxIndex : label.maxIndex;
		for (byte i = (byte) (max + 1); (--i) >= 0;) {
			State thisState = getState(i);
			State labelState = label.getState(i);
			if (thisState == labelState || labelState == State.absent)
				continue;
			if (thisState == State.absent)
				return false;
		}
		return true;
	}

	/**
	 * @param label
	 * @return the label containing common ALetter between this and label.
	 */
	public ALabel intersect(final ALabel label) {
		if ((label == null) || this.isEmpty() || label.isEmpty())
			return emptyLabel;
		if (this.alphabet != label.alphabet)
			throw new IllegalArgumentException("label is not defined using the same alphabet!");
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
	 * @param name the proposition to check.
	 * @return true if this contains proposition in any state: straight, negated or unknown.
	 */
	public boolean contains(final ALetter name) {
		return this.getState(getIndex(name)) != State.absent;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof ALabel))
			throw new IllegalArgumentException("Argument must be an ALabel object");
		final ALabel l1 = (ALabel) obj;
		return this.alphabet == l1.alphabet && this.bit0 == l1.bit0;
	}

	/**
	 * @return The array of a-letters of present literals in this label in alphabetic order.
	 */
	public ALetter[] getAllLetter() {
		ALetter[] letters = new ALetter[size()];
		int j = 0;
		for (byte i = 0; i <= this.maxIndex; i++) {
			if (getState(i) != State.absent) {
				letters[j++] = this.getLetter(i);
			}
		}
		return letters;
	}

	/**
	 * @return the alphabet
	 */
	public ALabelAlphabet getAlphabet() {
		return this.alphabet;
	}

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
	private final ALetter getLetter(final byte letterIndex) {
		return this.alphabet.get(letterIndex);
	}

	/**
	 * @param letterIndex the index of the literal to retrieve.
	 * @return the status of literal with index literalIndex. If the literal is not present, it returns {@link State#absent}, otherwise .{@link State#present}.
	 */
	private final State getState(final byte letterIndex) {
		long mask = 1L << letterIndex;
		return ((this.bit0 & mask) != 0) ? State.present : State.absent;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// It is impossible to guarantee a unique hashCode for each possible label.
		return (int) (this.bit0);
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
	private final void remove(final byte index) {
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
		byte _cacheOfSize = 0;
		long or = this.bit0;
		for (int i = this.maxIndex + 1; (--i) >= 0;) {
			_cacheOfSize += (or & 1);
			or = or >>> 1;
		}
		this.cacheOfSize = _cacheOfSize;
		return _cacheOfSize;
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
			return emptyLabel.toString();
		final StringBuilder s = new StringBuilder();
		State st;
		for (byte i = 0, j = 0; i <= this.maxIndex; i++) {
			st = getState(i);
			if (st == State.present) {
				s.append(getLetter(i));
				if (++j < this.size()) {
					s.append(Constants.ALABEL_SEPARATOR);
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
