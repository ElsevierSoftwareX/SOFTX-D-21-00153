package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

/**
 * <p>
 * Simple class to represent a <em>label</em> in the CSTN/CSTNU framework.<br>
 * A label is a (logic) conjunction of zero or more <em>literals</em> ({@link it.univr.di.labeledvalue.Literal}).<br>
 * A label without literals is called <em>empty label</em> and it is represented graphically as {@link it.univr.di.labeledvalue.Constants#EMPTY_LABEL}.<br>
 * A labels is <em>consistent</em> when it does not contains opposite literals.
 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> (<code>⊨ L' ⇒ L</code>).<br>
 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.
 * </p>
 * <p>
 * Design assumptions
 * Since in CSTN(U) project the memory footprint of a label is an important aspect, after some experiments, I have found that the best way
 * to represent a label is to limit the possible propositions to the range [A-Z,a-z,α-μ] and to use two <code>long</code> for representing the state of literals
 * composing a label:
 * the two long are used in pair; each position of them is associated to a possible literal (position 0 to 'A',...,position 63 to 'μ'); given a position,
 * the two corresponding bits in the two long can represent all possible four states ({@link Literal#ABSENT},
 * {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW}) of the literal associated to the position.
 * </p>
 * <p>
 * The following table represent execution times of some Label operations determined using different implementation of this class.
 * </p>
 * <table border="1">
 * <caption>Execution time for some operations w.r.t the core data structure of the class.</caption>
 * <tr>
 * <th>Method</th>
 * <th>TreeSet</th>
 * <th>ObjectAVLTreeSet</th>
 * <th>ObjectRBTreeSet</th>
 * <th>byte array</th>
 * <th>two int</th>
 * <th>two long</th>
 * </tr>
 * <tr>
 * <td>Simple conjunction of '¬abcd' with '¬adefjs'='¬abcdefjs' (ms)</td>
 * <td>0.076961</td>
 * <td>0.066116</td>
 * <td>0.068798</td>
 * <td>0.001309</td>
 * <td>0.000299</td>
 * <td>0.000317</td>
 * </tr>
 * <tr>
 * <td>Execution time for an extended conjunction of '¬abcd' with
 * 'a¬c¬defjs'='¿ab¿c¿defjs' (ms)</td>
 * <td>0.07583</td>
 * <td>0.024099</td>
 * <td>0.014627</td>
 * <td>0.000843</td>
 * <td>0.000203</td>
 * <td>0.000235</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (inconsistent) labels are consistent.
 * Details '¬abcd' with 'a¬c¬defjs' (ms)</td>
 * <td>0.004016</td>
 * <td>0.001666</td>
 * <td>0.00166</td>
 * <td>0.00121</td>
 * <td>0.00075</td>
 * <td>0.00040</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (consistent) labels are consistent.
 * Details '¬abcd' with '¬abcd' (ms)</td>
 * <td>0.01946</td>
 * <td>0.004457</td>
 * <td>0.004099</td>
 * <td>0.000392</td>
 * <td>0.000558</td>
 * <td>0.000225</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the
 * literal is the last inserted) (ms)</td>
 * <td>5.48E-4</td>
 * <td>4.96E-4</td>
 * <td>5.01E-4</td>
 * <td>2.69E-4</td>
 * <td>5.03E-4</td>
 * <td>7.47E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the
 * literal is not present) (ms)</td>
 * <td>6.48E-4</td>
 * <td>7.71E-4</td>
 * <td>5.96E-4</td>
 * <td>1.84E-4</td>
 * <td>3.07E-4</td>
 * <td>2.33E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition
 * letter (the literal is present) (ms)</td>
 * <td>0.003272</td>
 * <td>1.83E-4</td>
 * <td>1.09E-4</td>
 * <td>1.27E-4</td>
 * <td>1.60E-4</td>
 * <td>1.32E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition
 * letter (the literal is not present) (ms)</td>
 * <td>0.002569</td>
 * <td>1.68E-4</td>
 * <td>1.0E-4</td>
 * <td>1.04E-4</td>
 * <td>1.60E-4</td>
 * <td>1.30E-4</td>
 * </tr>
 * </table>
 * <b>All code for performance tests is in LabelTest class .</b>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class Label implements Comparable<Label> {

	/**
	 * An unmodifiable empty label.
	 * 
	 * @author posenato
	 */
	static final class EmptyLabel extends Label {
		/**
		 * 
		 */
		public EmptyLabel() {
			super();
		}

		@Override
		public void clear() {
			return;
		}

		@Override
		public int compareTo(Label label) {
			if (label == null)
				return 1;
			if (label.isEmpty())
				return 0;
			return -1;
		}

		@Override
		public final boolean conjunct(char c, char s) {
			throw new IllegalAccessError("Read only object!");
		}

		@Override
		public final boolean conjunct(Literal l) {
			throw new IllegalAccessError("Read only object!");
		}

		@Override
		public final boolean conjunctExtended(char c, char s) {
			throw new IllegalAccessError("Read only object!");
		}

		@Override
		public final boolean conjunctExtended(Literal l) {
			throw new IllegalAccessError("Read only object!");
		}

		@Override
		public Label conjunction(Label l) {
			if (l.isEmpty())
				return this;
			return Label.clone(l);
		}

		@Override
		public Label conjunctionExtended(Label l) {
			if (l.isEmpty())
				return this;
			return Label.clone(l);
		}

		@Override
		public boolean contains(char proposition) {
			return false;
		}

		@Override
		public boolean contains(Literal l) {
			return false;
		}

		@Override
		public boolean containsUnknown() {
			return false;
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean isEmpty() {
			return true;
		}

		@Override
		public final Label remove(char c) {
			return this;
		}

		@Override
		public final Label remove(Label l) {
			return this;
		}

		@Override
		public final boolean remove(Literal l) {
			return false;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public String toString() {
			return Constants.EMPTY_LABELstring;
		}

	}

	/**
	 * A constant empty label to represent an empty label that cannot be modified.
	 */
	public static final Label emptyLabel = new EmptyLabel();

	/**
	 * Maximal number of possible proposition in a network.<br>
	 * This limit cannot be change without revising all this class code.
	 */
	public static final int NUMBER_OF_POSSIBLE_PROPOSITIONS = 64;

	/**
	 * <pre>
	 * Possible status of a literal
	 * 				bit1[i] bit0[i]
	 * not present 		0 		0
	 * straight 			0		1
	 * negated 			1		0
	 * unknown 			1		1
	 * </pre>
	 */
	private static final char[] LITERAL_STATE = { Literal.ABSENT, Literal.STRAIGHT, Literal.NEGATED, Literal.UNKNONW };

	/**
	 * Regular expression representing a Label.
	 * The re checks only that label chars are allowed.
	 */
	public static final String LABEL_RE = "(("
			+ Constants.NOTstring + "[" + Literal.PROPOSITION_RANGE + "]|"
			+ Constants.UNKNOWN + "[" + Literal.PROPOSITION_RANGE + "]|"
			+ "[" + Literal.PROPOSITION_RANGE + "])+|"
			+ Constants.EMPTY_LABELstring + ")";

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
			final Label l = Label.parse(model);

			if (l == null) {
				problems.append("Highlighted label is not well-formed.");
				return;
			}
		}
	};

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Label.class.getName());

	/**
	 * A base is a set of same-length labels that are can be used to build any other greater-length label of the universe.<br>
	 * The label components of a base can be built from a set of literals making all possible same-length combinations of such literals and their negations.
	 *
	 * @param baseElements an array of propositions.
	 * @return return all the components of the base built using literals of baseElements. Null if baseElements is null or empty.
	 */
	public static final Label[] allComponentsOfBaseGenerator(final char[] baseElements) {
		if (baseElements.length == 0)
			return null;
		final int baseSize = baseElements.length;
		final int n = (int) Math.pow(2, baseSize);
		final Label[] components = new Label[n];
		for (int i = 0; i < n; i++) {
			components[i] = Label.complementGenerator(baseElements, i);
		}
		return components;
	}

	/**
	 * Returns a label containing the propositions specified by c. The i-th proposition of resulting label has negative state if
	 * i-th bit of mask is 1.
	 *
	 * @param proposition an array of propositions.
	 * @param mask It has to be > 0 and <= 2^proposition.length. No range check is made!
	 * @return a label copy of label but with literals having indexes corresponding to bits 1 in the parameter 'index' set to negative state.
	 *         If label is null or empty or contains UNKNOWN literals, returns null;
	 */
	private static final Label complementGenerator(final char[] proposition, final long mask) {
		int n = proposition.length;
		if (n == 0)
			return null;
		long j = 1L;
		final Label newLabel = new Label();
		for (int i = 0; i < n; i++, j <<= 1) {
			char state = ((j & mask) != 0) ? Literal.NEGATED : Literal.STRAIGHT;
			newLabel.set(Literal.index(proposition[i]), state);
		}
		return newLabel;
	}

	/**
	 * Parse a string representing a label and return an equivalent Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link #LABEL_RE}.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a Label object corresponding to the label string representation.
	 */
	public static final Label parse(String s) {
		if (s == null)
			return null;

		// FIX for past label in mixed case
		// s = s.toLowerCase(); From version > 130 proposition can be also upper case and first eleven greek letters
		int n = s.length();
		if (n == 0)
			return Label.emptyLabel;
		char c;

		// trim all internal spaces or other chars
		final StringBuilder sb = new StringBuilder(n);
		int i = 0;
		while (i < n) {
			c = s.charAt(i++);
			if (Literal.check(c) || (c == Constants.NOT) || (c == Constants.UNKNOWN) || (c == Constants.EMPTY_LABEL)) {
				sb.append(c);
			} else {
				if (!(c == ' ' || c == '\t' || c == '\n' || c == '\f' || c == '\r')) {
					return null;
				}
			}
		}
		s = sb.toString();
		n = s.length();
		// LOG.finest("String trimmed: " + s2);
		// if (!patterLabelRE.matcher(s2).matches()) // LOG.finest("Input '" +
		// s2 + "' does not satisfy Label format: " + Constants.labelRE);
		// return null;

		if ((n == 1) && (s.charAt(0) == Constants.EMPTY_LABEL))
			return Label.emptyLabel; // Check only one time the special case made with the empty symbol.

		Label label = new Label();
		byte literalIndex;
		char literalStatus;
		i = 0;
		while (i < n) {
			c = s.charAt(i);
			if (c == Constants.NOT || c == Constants.UNKNOWN) {
				char sign = c;
				if (++i >= n)
					return null;
				c = s.charAt(i);
				if (!Literal.check(c))
					return null;
				// l = Literal.create(c, (sign == Constants.NOT) ? Literal.NEGATED
				// : Literal.UNKNONW);
				literalIndex = Literal.index(c);
				literalStatus = (sign == Constants.NOT) ? Literal.NEGATED : Literal.UNKNONW;
			} else {
				if (!Literal.check(c))
					return null;
				literalIndex = Literal.index(c);
				literalStatus = Literal.STRAIGHT;
			}
			// if (label.label[literalIndex] != 0)
			if (label.contains(c))
				return null;
			label.set(literalIndex, literalStatus);
			i++;
		}
		return label;
	}

	/**
	 * One long has 64 bits.<br>
	 * Using two longs, it is possible to represent 4 states for each position.<br>
	 * Each position is associated to a proposition.<br>
	 * Since {@link Constants#LabelRE} declares 64 propositions.
	 * 
	 * <pre>
	 * Status of i-th literal
	 *              bit1[i] bit0[i]
	 * not present          0   0
	 * straight             0   1
	 * negated              1   0
	 * unknown              1   1
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	private long bit1, bit0;

	/**
	 * Index of the highest-order ("leftmost") literal of label w.r.t. lexicographical order.
	 * On 2016-03-30 I showed by SizeofUtilTest.java that using byte it is possible to define also 'size' field without incrementing the memory footprint of the
	 * object.
	 */
	private byte maxIndex;

	/**
	 * Number of literals in the label
	 * Value -1 means that the size has to be calculated!
	 */
	private byte cacheOfSize = -1;

	/**
	 * Default constructor.
	 */
	public Label() {
		this.bit0 = this.bit1 = 0;
		this.maxIndex = -1;
		this.cacheOfSize = 0;
	}

	/**
	 * Constructs a label with a proposition having a state.
	 *
	 * @param proposition
	 * @param state a possible state of the proposition: {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW}
	 */
	public Label(final char proposition, final char state) {
		this();
		if (state != Literal.ABSENT) {
			byte index = Literal.index(proposition);
			this.set(index, state);
			this.maxIndex = index;
		}
	}

	/**
	 * Constructs a label cloning the given label l.
	 * 
	 * @param label the label to clone. It cannot be null or empty!
	 */
	private Label(final Label label) {
		this();
		if (label == null || label.isEmpty())
			throw new IllegalArgumentException("Label cannot be null or empty!");
		this.bit0 = label.bit0;
		this.bit1 = label.bit1;
		this.maxIndex = label.maxIndex;
		this.cacheOfSize = label.cacheOfSize;
	}

	/**
	 * In order to have a correct copy of a label.
	 * 
	 * @param label
	 * @return a distinct equal copy of label
	 */
	static final public Label clone(final Label label) {
		if (label == null || label.isEmpty())
			return Label.emptyLabel;
		return new Label(label);
	}

	/**
	 * Makes the label empty.
	 */
	public void clear() {
		this.bit1 = this.bit0 = 0;
		this.maxIndex = -1;
		this.cacheOfSize = 0;
	}

	/**
	 * {@inheritDoc} Determines an order based on length of label. Same length labels are in lexicographical order based on the natural order of type
	 * {@link Literal}.
	 */
	@Override
	public int compareTo(final Label label) {
		if (label == null)
			return 1;
		if (this.equals(label))
			return 0;// fast comparison!
		if (this.size() < label.size())
			return -1;
		if (this.size() > label.size())
			return 1;

		// they have same length and they are different
		int i = 0, j = 0, cmp = 0;
		int thisState, labelState;
		long maskI = 1L, maskJ = 1L;
		while (i <= this.maxIndex && j <= label.maxIndex) {
			while ((thisState = ((((this.bit1 & maskI) != 0) ? 2 : 0) + (((this.bit0 & maskI) != 0) ? 1 : 0))) == 0 && i <= this.maxIndex) {
				i++;
				maskI <<= 1;
			}
			while ((labelState = ((((label.bit1 & maskJ) != 0) ? 2 : 0) + (((label.bit0 & maskJ) != 0) ? 1 : 0))) == 0 && j <= label.maxIndex) {
				j++;
				maskJ <<= 1;
			}
			if (i != j)
				return i - j;
			cmp = thisState - labelState;
			if (cmp != 0)
				return cmp;
			i++;
			maskI <<= 1;
			j++;
			maskJ <<= 1;
		}
		return 0;// impossible but necessary for avoiding the warning!
	}

	/**
	 * It conjuncts <code>proposition</code> to this if <code>this</code> is consistent with <code>proposition</code> and its <code>propositionState</code>.
	 *
	 * @param proposition the proposition to conjunct. It cannot have an UNKNOWN state.
	 * @param propositionState a possible state of the proposition: {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW}
	 * @return true if proposition is added, false otherwise.
	 */
	public boolean conjunct(final char proposition, final char propositionState) {
		if (propositionState == Literal.ABSENT || propositionState == Literal.UNKNONW)
			return false;
		byte propIndex = Literal.index(proposition);
		char st = get(propIndex);
		if (Literal.areComplement(st, propositionState))
			return false;
		if (st == propositionState)
			return true;
		set(propIndex, propositionState);
		if (propIndex > this.maxIndex)
			this.maxIndex = propIndex;
		return true;
	}

	/**
	 * Helper method for {@link it.univr.di.labeledvalue.Label#conjunct(char, char)}
	 * 
	 * @param literal a literal
	 * @return true if literal has been added.
	 */
	public boolean conjunct(final Literal literal) {
		return conjunct(literal.getName(), literal.getState());
	}

	/**
	 * It conjuncts <code>proposition</code> to <code>this</code>. If <code>proposition</code> state <code>literalState</code> is opposite to the
	 * corresponding literal in <code>this</code>, the opposite literal in <code>this</code> is substituted with <code>proposition</code> but with unknown
	 * state. If <code>proposition</code> has unknown state, it is add to <code>this</code> as unknown.
	 *
	 * @param proposition the literal to conjunct.
	 * @param propositionState a possible state of the proposition: {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW}
	 * @return true if proposition is added, false otherwise.
	 */
	public boolean conjunctExtended(final char proposition, char propositionState) {
		if (propositionState == Literal.ABSENT)
			return false;
		byte index = Literal.index(proposition);
		char st = get(index);
		if (propositionState == Literal.UNKNONW || Literal.areComplement(st, propositionState)) {
			propositionState = Literal.UNKNONW;
		}
		if (index > this.maxIndex)
			this.maxIndex = index;
		set(index, propositionState);
		return true;
	}

	/**
	 * Helper method {@link #conjunctExtended(char, char)}
	 * 
	 * @param literal a literal
	 * @return true if literal has been added.
	 */
	public boolean conjunctExtended(final Literal literal) {
		return conjunctExtended(literal.getName(), literal.getState());
	}

	/**
	 * It conjuncts the given label to this using also {@link Literal#UNKNONW} literals.
	 * A {@link Literal#UNKNONW} literal represent the fact that in the two input labels a proposition letter is present as straight state in
	 * one label and in negated state in the other.<br>
	 * For a detail about the conjunction of unknown literals, see {@link #conjunctExtended(char, char)}.
	 * 
	 * @param label the input label.
	 * @return boolean true if the conjunction changed this.
	 */
	public boolean conjunctExtended(final Label label) {
		if (label == null)
			return false;
		long unionB0 = this.bit0 | label.bit0;
		long unionB1 = this.bit1 | label.bit1;
		boolean changed = (this.bit0 != unionB0) || (this.bit1 != unionB1);
		if (!changed)
			return false;
		this.bit0 = unionB0;
		this.bit1 = unionB1;
		this.maxIndex = (label.maxIndex > this.maxIndex) ? label.maxIndex : this.maxIndex;
		this.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return true;
	}

	/**
	 * Conjuncts <code>label</code> to this if <code>this</code> is consistent with <code>label</code> and returns the result without modifying
	 * <code>this</code>.
	 *
	 * @param label the label to conjunct
	 * @return a new label with the conjunction of <code>this</code> and <code>label</code> if they are consistent, null otherwise.<br>
	 *         null also if <code>this</code> or <code>label</code> contains unknown literals.
	 */
	public Label conjunction(final Label label) {
		if (label == null)
			return null;
		long unionB0 = this.bit0 | label.bit0;
		long unionB1 = this.bit1 | label.bit1;
		if ((unionB0 & unionB1) != 0) {
			// there is at least one unknown or a pair of opposite literals
			return null;
		}

		final Label newLabel = new Label();
		newLabel.bit0 = unionB0;
		newLabel.bit1 = unionB1;
		newLabel.maxIndex = (label.maxIndex > this.maxIndex) ? label.maxIndex : this.maxIndex;
		newLabel.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return newLabel;
	}

	/**
	 * Create a new label that represents the conjunction of <code>this</code> and <code>label</code> using also {@link Literal#UNKNONW} literals.
	 * A {@link Literal#UNKNONW} literal represent the fact that in the two input labels a proposition letter is present as straight state in
	 * one label and in negated state in the other.<br>
	 * For a detail about the conjunction of unknown literals, see {@link #conjunctExtended(char, char)}.
	 * 
	 * @param label the input label.
	 * @return a new label with the conjunction of <code>this</code> and <code>label</code>.<br>
	 *         <code>this</code> is not altered by this method.
	 */
	public Label conjunctionExtended(final Label label) {
		if (label == null)
			return null;
		long unionB0 = this.bit0 | label.bit0;
		long unionB1 = this.bit1 | label.bit1;

		final Label newLabel = new Label();
		newLabel.bit0 = unionB0;
		newLabel.bit1 = unionB1;
		newLabel.maxIndex = (label.maxIndex > this.maxIndex) ? label.maxIndex : this.maxIndex;
		newLabel.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return newLabel;
	}

	/**
	 * @param proposition the proposition to check.
	 * @return true if this contains proposition in any state: straight, negated or unknown.
	 */
	public boolean contains(final char proposition) {
		return get(Literal.index(proposition)) != Literal.ABSENT;
	}

	/**
	 * @param l
	 * @return true if the literal <code>l</code> is present into the label.
	 */
	public boolean contains(final Literal l) {
		if (l == null)
			return false;
		return l.getState() == get(Literal.index(l.getName()));
	}

	/**
	 * @return true if the label contains one unknown literal at least.
	 */
	public boolean containsUnknown() {
		// optimized version!
		return (this.bit0 & this.bit1) != 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof Label))
			return false;
		return this.equals((Label) obj);
	}

	/**
	 * This method is for a faster check. Not null check is not done!
	 * 
	 * @param label a not null label!
	 * @return true if the two labels are equal!
	 */
	public boolean equals(final Label label) {
		return this.bit1 == label.bit1 && this.bit0 == label.bit0;
	}

	/**
	 * @param literalIndex the index of the literal to retrieve.
	 * @return the status of literal with index literalIndex. If the literal is not present, it returns {@link Literal#ABSENT}.
	 */
	private final char get(final byte literalIndex) {
		long mask = 1L << literalIndex;
		int b1 = ((this.bit1 & mask) != 0) ? 2 : 0;
		int b0 = ((this.bit0 & mask) != 0) ? 1 : 0;
		return LITERAL_STATE[b1 + b0];
	}

	/**
	 * @return The array of propositions (char) that have unknown status in this label.
	 */
	public char[] getAllUnknown() {
		if (this.maxIndex <= 0)
			return new char[0];
		char[] indexes = new char[size()];
		int j = 0;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			if (get(i) == Literal.UNKNONW) {
				indexes[j++] = Literal.charValue(i);
			}
		}
		return Arrays.copyOf(indexes, j);
	}

	/**
	 * @return An array containing a copy of literals in this label.
	 */
	public Literal[] getLiterals() {
		Literal[] indexes = new Literal[size()];
		int j = 0;
		char state;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			if ((state = get(i)) != Literal.ABSENT) {
				indexes[j++] = Literal.create(Literal.charValue(i), state);
			}
		}
		return indexes;
	}

	/**
	 * @return The array of proposition of present literals in this label in alphabetic order.
	 */
	public char[] getPropositions() {
		char[] indexes = new char[size()];
		int j = 0;
		for (byte i = 0; i <= this.maxIndex; i++) {
			if (get(i) != Literal.ABSENT) {
				indexes[j++] = Literal.charValue(i);
			}
		}
		return indexes;
	}

	/**
	 * @param c the name of literal
	 * @return the state of literal with name c if it is present, {@link Literal#ABSENT} otherwise.
	 */
	public final char getStateLiteralWithSameName(final char c) {
		return get(Literal.index(c));
	}

	/**
	 * Determines the sub label of <code>this</code> that is also present (not present) in label <code>lab</code>.
	 * <p>
	 *
	 * @param label the label in which to find the common/uncommon sub-part.
	 * @param inCommon true if the common sub-label is wanted, false if the sub-label present in <code>this</code> and not in <code>lab</code> is wanted.
	 * @return the sub label of <code>this</code> that is in common/not in common (if inCommon is true/false) with <code>lab</code>. The label returned is a new
	 *         object that shares only literals with this or with from. If there is no common part, an empty label is returned.
	 */
	public Label getSubLabelIn(final Label label, final boolean inCommon) {
		final Label sub = new Label();
		if (this.isEmpty())
			return sub;
		if ((label == null) || label.isEmpty())
			return (inCommon) ? sub : new Label(this);
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			char thisState = get(i);
			if (thisState == Literal.ABSENT)
				continue;
			char labelState = label.get(i);
			if (inCommon) {
				if (labelState == Literal.ABSENT)
					continue;
				if (thisState == labelState) {
					sub.set(i, labelState);
					continue;
				}
			} else {
				if (labelState != thisState) {
					sub.set(i, thisState);
					continue;
				}
			}
		}
		return sub;
	}

	/**
	 * Determines the sub label of <code>this</code> that is also present (not present) in label <code>lab</code>.
	 * <p>
	 * When the common sub label is required (<code>inCommon</code> true), if <code>strict</code> is true, then the common sub label is as expected:
	 * it contains only the literals of <code>this</code> also present into <code>lab</code>. <br>
	 * Otherwise, when <code>strict</code> is false, the common part contains also each literal that has the opposite or the unknown counterpart in
	 * <code>lab</code>. For example, is this='a¬b¿cd' and lab='bc¿d', the not strict common part is '¿b¿c¿d'.
	 *
	 * @param label the label in which to find the common/uncommon sub-part.
	 * @param inCommon true if the common sub-label is wanted, false if the sub-label present in <code>this</code> and not in <code>lab</code> is wanted.
	 * @param strict if the common part should contain only the same literals in both labels.
	 * @return the sub label of <code>this</code> that is in common/not in common (if inCommon is true/false) with <code>lab</code>. The label returned is a new
	 *         object that shares only literals with this or with from. If there is no common part, an empty label is returned.
	 */
	public Label getSubLabelIn(final Label label, final boolean inCommon, boolean strict) {
		final Label sub = new Label();
		if (this.isEmpty())
			return sub;
		if ((label == null) || label.isEmpty())
			return (inCommon) ? sub : new Label(this);
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			char thisState = get(i);
			if (thisState == Literal.ABSENT)
				continue;
			char labelState = label.get(i);
			if (inCommon) {
				if (labelState == Literal.ABSENT)
					continue;
				if (thisState == labelState) {
					sub.set(i, labelState);
					continue;
				}
				if (!strict) {
					sub.set(i, Literal.UNKNONW);
					continue;
				}
			} else {
				if (labelState == Literal.ABSENT) {
					sub.set(i, thisState);
					continue;
				}
			}
		}
		return sub;
	}

	/**
	 * Finds and returns the unique different literal between <code>this</code> and <code>lab</code> if it exists. If label <code>this</code> and
	 * <code>lab</code> differs in more than one literals, it returns null.
	 * <p>
	 * If <code>this</code> and <code>lab</code> contain a common proposition but in one its state is unknown and in other is straight or negated, it
	 * returns null;
	 *
	 * @param label a nor null neither empty label.
	 * @return the unique literal of 'this' that has its opposite in <code>lab</code>.<br>
	 *         null, if there is no literal of such kind or there are two or more literals of this kind or this/label is empty or null.
	 */
	public Literal getUniqueDifferentLiteral(final Label label) {
		if (label == null || label.isEmpty() || this.size() != label.size())
			return null;
		byte theDistinguished = -1;
		char thisState = Literal.ABSENT;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			thisState = get(i);
			char labelState = label.get(i);
			if (thisState == labelState)
				continue;
			if (thisState == Literal.UNKNONW || labelState == Literal.UNKNONW || thisState == Literal.ABSENT
					|| labelState == Literal.ABSENT)
				return null;
			if (theDistinguished == -1) {
				theDistinguished = i;
			} else {
				return null;
			}
		}
		if (theDistinguished != -1)
			return Literal.create(Literal.charValue(theDistinguished), get(theDistinguished));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// It is impossible to guarantee a unique hashCode for each possible label.
		return (int) (this.bit1 << this.maxIndex | this.bit0);
	}

	/**
	 * A literal l is consistent with a label if the last one does not contain ¬l.
	 * 
	 * @param literalIndex
	 * @param literalState
	 * @return true if the literal is consistent with this label.
	 */
	boolean isConsistentWith(final byte literalIndex, char literalState) {
		if (literalState == Literal.ABSENT)
			return true;
		char thisState = get(literalIndex);
		if (thisState == Literal.UNKNONW && literalState != Literal.UNKNONW)
			return false;
		if (thisState != Literal.UNKNONW && literalState == Literal.UNKNONW)
			return false;
		return (!Literal.areComplement(thisState, literalState));
	}

	/**
	 * <p>
	 * A label L<sub>1</sub> is consistent with a label L<sub>2</sub> if L<sub>1</sub> &wedge; L<sub>2</sub> is satisfiable.<br>
	 * L<sub>1</sub> subsumes L<sub>2</sub> implies L<sub>1</sub> is consistent with L<sub>2</sub> but not vice-versa.
	 * </p>
	 * <p>
	 * If L<sub>1</sub> contains an unknown literal ¿p, and L<sub>2</sub> contains p/¬p, then the conjunction L<sub>1</sub> &wedge; L<sub>2</sub>
	 * is not defined while ¿p subsumes p/¬p is true.
	 * </p>
	 * <p>
	 * In order to considering also labels with unknown literals, this method considers the {@link #conjunctionExtended(Label)}:
	 * L<sub>1</sub> &#x2605; L<sub>2</sub>, where ¿p are used for represent the conjunction of opposite literals p and ¬p or literals like ¿p and p/¬p.<br>
	 * Now, it holds ¿p &#x2605; p = ¿p is satisfiable.
	 * </p>
	 * 
	 * @param label the label to check
	 * @return true if the label is consistent with this label.
	 */
	public boolean isConsistentWith(final Label label) {
		if (label == null)// || (label.isEmpty() && !this.containsUnknown()) || (this.isEmpty()&& !label.containsUnknown()))
			return true;
		/**
		 * Method 1.
		 * We consider L<sub>1</sub> &#x2605; L<sub>2</sub>,
		 */
		// both this and label have bit0 | bit1 != 0
		long xBit0 = this.bit0 ^ label.bit0;// each bit=1 corresponds to 1) a positive literal present only in one label
		// or 2) a unknown literal present only in one label
		// or 3) a positive literal present in one label and the opposite present in the other
		long xBit1 = this.bit1 ^ label.bit1;// each bit=1 corresponds to 1) a negative literal present only in one label
		// or 2) a unknown literal present only in one label
		// or 3) a negative literal present in one label and the opposite present in the other
		long aBit = xBit0 & xBit1;
		aBit = aBit & ~(this.bit0 & this.bit1) & ~(label.bit0 & label.bit1);// ~(this.bit0 & this.bit1) contains 0 in correspondence of possible ¿p in this
		return aBit == 0;
		/**
		 * Method 2.
		 * The following code manages the case in which ¿p can be present but only if there is not a corresponding 'p' or '¬p' in the other label.
		 */
		// int uBit0 = this.bit0 | label.bit0;
		// int uBit1 = this.bit1 | label.bit1;
		// int aBit01 = uBit0 & uBit1;
		// if (aBit01 == 0)
		// return true; // no opposite literals, no unknown literals.
		// uBit0 = this.bit0 & this.bit1;
		// uBit1 = label.bit0 & label.bit1;
		// if (uBit0 != 0 || uBit1 != 0) {
		// // There is a unknown literal at least
		// if ((uBit0 & (label.bit0 ^ label.bit1)) != 0) {
		// // this label has an unknown literal at least
		// return false;// unknown on first label and straight/negated on the second label.
		// }
		// if ((uBit1 & (this.bit0 ^ this.bit1)) != 0) {
		// // this label has an unknown literal at least
		// return false;// straight/negated on first label and unknown on the second label.
		// }
		// return true;
		// }
		// return false;
	}

	/**
	 * @see Label#isConsistentWith(byte, char)
	 * @param lit
	 * @return true if lit is consistent with this label.
	 */
	public boolean isConsistentWith(Literal lit) {
		return isConsistentWith(Literal.index(lit.getName()), lit.getState());
	}

	/**
	 * @return true if the label contains no literal.
	 */
	public boolean isEmpty() {
		return (this.bit1 == 0 && this.bit0 == 0);
	}

	/**
	 * Since a label is a conjunction of literals, its negation is a disjunction of negative literals (i.e., not a label).
	 * <p>
	 * The negation operator returns a set of all negative literals of this label.<br>
	 * Bear in mind that the complement of a literal with unknown state is a null object.
	 *
	 * @return the set of all negative literal of this as an array. If this is empty, returns an empty array.;
	 */
	public Literal[] negation() {
		if (this.isEmpty())
			return new Literal[0];

		final Literal[] literals = new Literal[size()];
		int j = 0;
		for (byte i = 0; i <= this.maxIndex; i++) {
			char thisState = get(i);
			if (thisState == Literal.ABSENT || thisState == Literal.UNKNONW)
				continue;
			literals[j++] = Literal.create(Literal.charValue(i), thisState == Literal.NEGATED ? Literal.STRAIGHT : Literal.NEGATED);
		}
		return literals;
	}

	/**
	 * It removes proposition if it is present, otherwise it does nothing.
	 * Removing a proposition means to remove all literal of the given proposition.
	 *
	 * @param proposition the proposition to remove.
	 * @return return reference a this for allowing concatenation
	 */
	public Label remove(final char proposition) {
		set(Literal.index(proposition), Literal.ABSENT);
		return this;
	}

	/**
	 * It removes all literals with names in <b>inputLabel</b> from the current label.
	 *
	 * @param inputLabel names of literals to remove.
	 * @return this as reference
	 */
	public Label remove(Label inputLabel) {
		if (inputLabel == null)
			return this;

		long inputPropositions = inputLabel.bit0 | inputLabel.bit1;
		inputPropositions = ~inputPropositions;
		this.bit0 = this.bit0 & inputPropositions;
		this.bit1 = this.bit1 & inputPropositions;

		long mask = 1L << this.maxIndex;
		long u = this.bit1 | this.bit0;
		while ((u & mask) == 0 && this.maxIndex >= 0) {
			this.maxIndex--;
			mask = mask >>> 1;
		}
		this.cacheOfSize = -1;
		return this;
	}

	/**
	 * It removes literal (proposition with a Literal.char) if it is present, otherwise it does nothing.
	 * If label contains '¬p' and input literal is 'p', then the method does nothing.
	 *
	 * @param literal the literal to remove
	 * @return true if the literal is removed.
	 */
	public boolean remove(final Literal literal) {
		byte index = Literal.index(literal.getName());
		if (get(index) == literal.getState()) {
			set(index, Literal.ABSENT);
			return true;
		}
		return false;
	}

	/**
	 * @param literalIndex the index of the literal to update.
	 * @param literalStatus the new state.
	 */
	private final void set(final byte literalIndex, final char literalStatus) {
		/**
		 * <pre>
		 * Status of i-th literal
		 *             bit1[i] bit0[i]
		 * not present       0 0
		 * straight          0 1
		 * negated           1 0
		 * unknown           1 1
		 * </pre>
		 */
		long mask = 1L << literalIndex;
		switch (literalStatus) {
		case Literal.STRAIGHT:
			if (((this.bit1 | this.bit0) & mask) == 0)
				this.cacheOfSize++;
			this.bit0 |= mask;
			mask = ~mask;
			this.bit1 &= mask;
			if (this.maxIndex < literalIndex)
				this.maxIndex = literalIndex;
			return;
		case Literal.NEGATED:
			if (((this.bit1 | this.bit0) & mask) == 0)
				this.cacheOfSize++;
			this.bit1 |= mask;
			mask = ~mask;
			this.bit0 &= mask;
			if (this.maxIndex < literalIndex)
				this.maxIndex = literalIndex;
			return;
		case Literal.UNKNONW:
			if (((this.bit1 | this.bit0) & mask) == 0)
				this.cacheOfSize++;
			this.bit1 |= mask;
			this.bit0 |= mask;
			if (this.maxIndex < literalIndex)
				this.maxIndex = literalIndex;
			return;
		case Literal.ABSENT:
		default:
			if (((this.bit1 | this.bit0) & mask) != 0)
				this.cacheOfSize--;
			mask = ~mask;
			this.bit1 &= mask;
			this.bit0 &= mask;
			if (this.maxIndex == literalIndex) {
				long u = this.bit1 | this.bit0;
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
		// long or = this.bit0 | this.bit1;
		// for (int i = this.maxIndex + 1; (--i) >= 0;) {
		// _cacheOfSize += (or & 1);
		// or = or >>> 1;
		// }
		// this.cacheOfSize = _cacheOfSize;
		// return _cacheOfSize;
		this.cacheOfSize = (byte) Long.bitCount(this.bit0 | this.bit1);
		return this.cacheOfSize;
	}

	/**
	 * A label L<sub>1</sub> subsumes (entails) a label L<sub>2</sub> if <code>L<sub>1</sub> ⊨ L<sub>2</sub></code>.<br>
	 * In other words, L<sub>1</sub> subsumes L<sub>2</sub> if L<sub>1</sub> contains all literals of L<sub>2</sub> when both they have no unknown literals.
	 * <p>
	 * An unknown literal ¿p represents the fact that the value of p is not known. Therefore, if ¿p is true, then p can be true. The same for ¬p.<br>
	 * If p is true or false, then ¿p is false. The same for ¬p.<br>
	 * Therefore, it holds that ¿p subsumes p, ¿p subsumes ¬p, and ¿p subsumes ¿p, while p NOT subsumes ¿p and ¬p NOT subsumes ¿p.
	 *
	 * @param label the label to check
	 * @return true if this subsumes label.
	 */
	public boolean subsumes(final Label label) {
		if ((label == null) || label.isEmpty())
			return true;
		int max = (this.maxIndex > label.maxIndex) ? this.maxIndex : label.maxIndex;
		for (byte i = (byte) (max + 1); (--i) >= 0;) {
			char thisState = get(i);
			char labelState = label.get(i);
			if (thisState == labelState || labelState == Literal.ABSENT)
				continue;
			/**
			 * When labelState[i] != thisState[i], before saying that it is false, it must be checked if thisState[i] is a ¿.
			 * ¿p subsumes p, ¿p subsumes ¬p, ¿p subsumes ¿p
			 * p NOT subsumes ¿p, ¬p NOT subsumes ¿p.
			 */
			if (thisState != Literal.UNKNONW)
				return false;
		}
		return true;
	}

	/**
	 * Return a string representing the the label as logical expression using logical 'not', 'and', and 'or'. String representations of operators can be given
	 * as parameters. A label 'P¬A' is represented as "P and not A". If negate is true, then 'P¬A' is represented as negated: "not P or A".
	 *
	 * @param negate negate the label before the conversion. Be careful!
	 * @param not string representing not. If null, it is assumed "!"
	 * @param and representing not. If null, it is assumed "&amp;"
	 * @param or representing not. If null, it is assumed " | "
	 * @return empty string if label is null or empty, the string representation as logical expression otherwise.
	 */
	public String toLogicalExpr(final boolean negate, String not, String and, String or) {
		if (this.isEmpty())
			return "";
		if (not == null)
			not = "!";
		if (and == null)
			and = " & ";
		if (or == null)
			or = " | ";
		final Literal[] lit = (negate) ? this.negation() : this.getLiterals();
		final StringBuilder s = new StringBuilder();

		for (int i = 0; i < lit.length; i++) {
			s.append(((lit[i].isUnknown()) ? not : "") + lit[i].getName() + ((negate) ? or : and));
		}
		return s.substring(0, s.length() - ((negate) ? or.length() : and.length()));
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		if (this.isEmpty())
			return Constants.EMPTY_LABELstring;
		final StringBuilder s = new StringBuilder();
		char st;
		for (byte i = 0; i <= this.maxIndex; i++) {
			st = get(i);
			if (st != Literal.ABSENT)
				s.append(Literal.toChars(i, st));
		}
		return s.toString();
	}
}
