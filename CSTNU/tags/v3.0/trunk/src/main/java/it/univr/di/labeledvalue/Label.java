// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Represents a immutable propositional <em>label</em> in the CSTN/CSTNU framework.<br>
 * A label is a (logic) conjunction of zero or more <em>literals</em> ({@link it.univr.di.labeledvalue.Literal}).<br>
 * A label without literals is called <em>empty label</em> and it is represented graphically as {@link it.univr.di.labeledvalue.Constants#EMPTY_LABEL}.<br>
 * A labels is <em>consistent</em> when it does not contains opposite literals.
 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> (<code>⊨ L' ⇒ L</code>).<br>
 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.
 * <p>
 * Design assumptions
 * Since in CSTN(U) project the memory footprint of a label is an important aspect, after some experiments, I have found that the best way
 * to represent a label is to limit the possible propositions to the range [a-z,A-F] and to use two <code>int</code> for representing the state of literals
 * composing a label:
 * the two <code>int</code> are used in pair; each position of them is associated to a possible literal (position 0 to 'a',...,position 32 to 'F'); given a
 * position,
 * the two corresponding bits in the two long can represent all possible four states ({@link Literal#ABSENT},
 * {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW}) of the literal associated to the position.<br>
 * Using only 32 possible propositions, it is possible to cache the two <code>int</code> of a label as a long and, therefore, to cache the label for reusing it.
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
 * <th>two int Immutable</th>
 * </tr>
 * <tr>
 * <td>Simple conjunction of '¬abcd' with '¬adefjs'='¬abcdefjs' (ms)</td>
 * <td>0.076961</td>
 * <td>0.066116</td>
 * <td>0.068798</td>
 * <td>0.001309</td>
 * <td>0.000299</td>
 * <td>0.000317</td>
 * <td>0.000529</td>
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
 * <td>0.000229</td>
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
 * <td>0.000122</td>
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
 * <td>0.000089</td>
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
 * <td>4.46E-4</td>
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
 * <td>2.14E-4</td>
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
 * <td>7.43E-5</td>
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
 * <td>5.25E-5</td>
 * </tr>
 * </table>
 * <b>All code for performance tests is in LabelTest class .</b>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class Label implements Comparable<Label> {

	/**
	 * Label object cache
	 * This declaration must stay here, before any other!
	 */
	private static final Long2ObjectMap<Label> CREATED_LABEL = new Long2ObjectOpenHashMap<>();

	/**
	 * A constant empty label to represent an empty label that cannot be modified.
	 */
	public static final Label emptyLabel = Label.valueOf(0L);

	/**
	 * Regular expression representing a Label.
	 * The re checks only that label chars are allowed.
	 */
	public static final String LABEL_RE = "(("
			+ "(" + Constants.NOTstring + "|" + Constants.UNKNOWN + "|)"
			+ Literal.PROPOSITION_RANGE
			+ ")+|"
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
	 * <pre>
	 * Possible status of a literal
	 * 				bit1[i] bit0[i]
	 * not present 		0 		0
	 * straight 		0		1
	 * negated 			1		0
	 * unknown 			1		1
	 * </pre>
	 */
	private static final char[] LITERAL_STATE = { Literal.ABSENT, Literal.STRAIGHT, Literal.NEGATED, Literal.UNKNONW };

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger("Label");

	/**
	 * Maximal number of possible proposition in a network.<br>
	 * This limit cannot be change without revising all this class code.
	 */
	public static final int NUMBER_OF_POSSIBLE_PROPOSITIONS = 32;// 64;

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
	 * @param b1 a positive integer
	 * @param b0 a positive integer
	 * @return the index associated to the two index b0 and b1.
	 */
	private static long cacheIndex(int b1, int b0) {
		return (((long) b1) << 32) + b0;// << must be within ()
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
	private static final Label complementGenerator(final char[] proposition, final int mask) {
		int n = proposition.length;
		if (n == 0)
			return null;
		int j = 1;
		Label newLabel = emptyLabel;
		for (int i = 0; i < n; i++, j <<= 1) {
			char state = ((j & mask) != 0) ? Literal.NEGATED : Literal.STRAIGHT;
			newLabel = newLabel.conjunction(proposition[i], state);
		}
		return newLabel;
	}

	/**
	 * @param index the input index
	 * @return the lower part of the given index as unsigned int
	 */
	private static int getB0(long index) {
		return (int) (index & 0xFFFFFFFFL);
	}

	/**
	 * @param index the input index
	 * @return the upper part of the given index as unsigned int
	 */
	private static int getB1(long index) {
		return (int) (index >>> 32);
	}

	/**
	 * Parse a string representing a label and return an equivalent Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link #LABEL_RE}.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a Label object corresponding to the label string representation, null if the input string does not represent a label or is null.
	 */
	public static final Label parse(@Nullable String s) {
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

		Label label = Label.emptyLabel;
		// byte literalIndex;
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
				// literalIndex = Literal.index(c);
				literalStatus = (sign == Constants.NOT) ? Literal.NEGATED : Literal.UNKNONW;
			} else {
				if (!Literal.check(c))
					return null;
				// literalIndex = Literal.index(c);
				literalStatus = Literal.STRAIGHT;
			}
			// if (label.label[literalIndex] != 0)
			if (label.contains(c))
				return null;
			label = label.conjunctionExtended(c, literalStatus);
			i++;
		}
		return label;
	}

	/**
	 * Each label is represented by two ints.
	 * In order to modify a label, it is possible to pass its two ints, the literal index to modify and the new state for such index.
	 * The method returns the index of the label representing the given parameters.
	 * 
	 * @param b0 seed for the first state int.
	 * @param b1 seed for the second state int.
	 * @param literalIndex the index of the literal to update.
	 * @param literalStatus the new state.
	 * @return the index of the label (composition of b1 and b0)
	 */
	private static final long set(int b0, int b1, final byte literalIndex, final char literalStatus) {
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
			b0 |= mask;
			mask = ~mask;
			b1 &= mask;
			break;
		case Literal.NEGATED:
			b1 |= mask;
			mask = ~mask;
			b0 &= mask;
			break;
		case Literal.UNKNONW:
			b1 |= mask;
			b0 |= mask;
			break;
		case Literal.ABSENT:
		default:
			mask = ~mask;
			b1 &= mask;
			b0 &= mask;
		}
		return cacheIndex(b1, b0);
	}

	/**
	 * @param proposition the input proposition as chat
	 * @param state its state
	 * @return the label initialized with literal represented by the proposition and its state.
	 *         If proposition is a char not allowed, a IllegalArgumentException is raised.
	 */
	static public Label valueOf(final char proposition, final char state) {
		if (state == Literal.ABSENT)
			return emptyLabel;
		byte literalIndex = Literal.index(proposition);
		if (literalIndex < 0)
			throw new IllegalArgumentException("Proposition is not allowed!");
		long index = set(0, 0, literalIndex, state);
		return valueOf(index);
	}

	/**
	 * @param index the input index
	 * @return the label represented by the two state ints.
	 */
	static private Label valueOf(long index) {
		Label cached = CREATED_LABEL.get(index);
		if (cached == null) {
			cached = new Label(getB1(index), getB0(index));
			CREATED_LABEL.put(index, cached);
		}
		return cached;
	}

	/**
	 * Using two ints, it is possible to represent 4 states for each position.<br>
	 * Each position is associated to a proposition.<br>
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
	private final int bit1, bit0;

	/**
	 * Index of the highest-order ("leftmost") literal of label w.r.t. lexicographical order.
	 * On 2016-03-30 I showed by SizeofUtilTest.java that using byte it is possible to define also 'size' field without incrementing the memory footprint of the
	 * object.
	 */
	private final byte maxIndex;

	/**
	 * Number of literals in the label
	 * Value -1 means that the size has to be calculated!
	 */
	private final byte count;

	/**
	 * Create a label from state integers b1 and b0.
	 * 
	 * @param b1 one input index
	 * @param b0 the other input index
	 */
	private Label(final int b1, final int b0) {
		this.bit0 = b0;
		this.bit1 = b1;
		this.count = (byte) Long.bitCount(b0 | b1);
		int mask = 1 << 31;
		byte mi = -1;
		for (byte i = 32; ((--i) >= 0);) {
			if (((b1 & mask) != 0) || ((b0 & mask) != 0)) {
				mi = i;
				break;
			}
			mask = mask >>> 1;
		}
		this.maxIndex = mi;
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
		int sizeC = Integer.compare(this.size(), label.size());
		if (sizeC != 0)
			return sizeC;

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
	 * It returns a new label conjunction of <code>proposition</code> and <code>this</code> if <code>this</code> is consistent with <code>proposition</code> and
	 * its
	 * <code>propositionState</code>.
	 * If propositionState is {@link Literal#ABSENT}, the effect is reset the <code>proposition</code> in the new label.
	 * 
	 * @param proposition the proposition to conjunct.
	 * @param propositionState a possible state of the proposition: {@link Literal#STRAIGHT}, {@link Literal#NEGATED} or {@link Literal#ABSENT}.
	 * @return the new label if proposition can be conjuncted, null otherwise.
	 */
	public Label conjunction(final char proposition, final char propositionState) {
		if (propositionState == Literal.UNKNONW)
			return null;
		byte propIndex = Literal.index(proposition);
		char st = get(propIndex);
		if (st == propositionState)
			return this;
		if (Literal.areComplement(st, propositionState))
			return null;
		long index = Label.set(this.bit0, this.bit1, propIndex, propositionState);
		return Label.valueOf(index);
	}

	/**
	 * Conjuncts <code>label</code> to <code>this</code> if <code>this</code> is consistent with <code>label</code> and returns the result without modifying
	 * <code>this</code>.
	 *
	 * @param label the label to conjunct
	 * @return a new label with the conjunction of <code>this</code> and <code>label</code> if they are consistent, null otherwise.<br>
	 *         null also if <code>this</code> or <code>label</code> contains unknown literals.
	 */
	public Label conjunction(final Label label) {
		if (label == null)
			return null;
		int unionB0 = this.bit0 | label.bit0;
		int unionB1 = this.bit1 | label.bit1;
		if ((unionB0 & unionB1) != 0) {
			// there is at least one unknown or a pair of opposite literals
			return null;
		}
		return valueOf(cacheIndex(unionB1, unionB0));
	}

	/**
	 * Helper method for {@link it.univr.di.labeledvalue.Label#conjunction(char, char)}
	 * 
	 * @param literal a literal
	 * @return the new Label if literal has been added, null otherwise.
	 */
	public Label conjunction(final Literal literal) {
		if (literal == null)
			return null;
		return conjunction(literal.getName(), literal.getState());
	}

	/**
	 * It returns the conjunction of <code>proposition</code> to <code>this</code>. If <code>proposition</code> state <code>literalState</code> is opposite to
	 * the corresponding literal in <code>this</code>, the opposite literal in <code>this</code> is substituted with <code>proposition</code> but with unknown
	 * state. If <code>proposition</code> has unknown state, it is add to <code>this</code> as unknown.
	 * If propositionState is {@link Literal#ABSENT}, the effect is reset the proposition in the label.
	 *
	 * @param proposition the literal to conjunct.
	 * @param propositionState a possible state of the proposition: {@link Literal#STRAIGHT}, {@link Literal#NEGATED}, {@link Literal#UNKNONW} or
	 *            {@link Literal#ABSENT}.
	 * @return Label if proposition is added, false otherwise.
	 */
	public Label conjunctionExtended(final char proposition, char propositionState) {
		byte propIndex = Literal.index(proposition);
		char st = get(propIndex);
		if (Literal.areComplement(st, propositionState)) {
			propositionState = Literal.UNKNONW;
		}
		long index = Label.set(this.bit0, this.bit1, propIndex, propositionState);
		return Label.valueOf(index);
	}

	/**
	 * Create a new label that represents the conjunction of <code>this</code> and <code>label</code> using also {@link Literal#UNKNONW} literals.
	 * A {@link Literal#UNKNONW} literal represent the fact that in the two input labels a proposition letter is present as straight state in
	 * one label and in negated state in the other.<br>
	 * For a detail about the conjunction of unknown literals, see {@link #conjunctionExtended(char, char)}.
	 * 
	 * @param label the input label.
	 * @return a new label with the conjunction of <code>this</code> and <code>label</code>.<br>
	 *         <code>this</code> is not altered by this method.
	 */
	public Label conjunctionExtended(final Label label) {
		if (label == null)
			return null;
		int unionB0 = this.bit0 | label.bit0;
		int unionB1 = this.bit1 | label.bit1;
		return valueOf(cacheIndex(unionB1, unionB0));
	}

	/**
	 * Helper method {@link #conjunctionExtended(char, char)}
	 * 
	 * @param literal a literal
	 * @return Label where literal has been added.
	 */
	public Label conjunctionExtended(final Literal literal) {
		if (literal == null)
			return null;
		return conjunctionExtended(literal.getName(), literal.getState());
	}

	/**
	 * @param proposition the proposition to check.
	 * @return true if this contains proposition in any state: straight, negated or unknown.
	 */
	public boolean contains(final char proposition) {
		return get(Literal.index(proposition)) != Literal.ABSENT;
	}

	/**
	 * @param proposition the proposition to check.
	 * @return the state of the proposition in this label: straight, negated, unknown or absent.
	 */
	public char getState(final char proposition) {
		return get(Literal.index(proposition));
	}

	/**
	 * @param l the input literal
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
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Label))
			return false;
		Label l = (Label) o;
		return this.bit1 == l.bit1 && this.bit0 == l.bit0;
	}

	/**
	 * @param literalIndex the index of the literal to retrieve.
	 * @return the status of literal with index literalIndex. If the literal is not present, it returns {@link Literal#ABSENT}.
	 */
	private final char get(final byte literalIndex) {
		int mask = 1 << literalIndex;
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
	 * @return An array containing a copy of literals in this label. The array may be empty.
	 */
	public Literal[] getLiterals() {
		Literal[] indexes = new Literal[size()];
		int j = 0;
		char state;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			if ((state = get(i)) != Literal.ABSENT) {
				indexes[j++] = Literal.valueOf(Literal.charValue(i), state);
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
		Label sub = emptyLabel;
		if (this.isEmpty())
			return sub;
		if ((label == null) || label.isEmpty())
			return (inCommon) ? sub : this;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			char thisState = get(i);
			if (thisState == Literal.ABSENT)
				continue;
			char labelState = label.get(i);
			if (inCommon) {
				if (labelState == Literal.ABSENT)
					continue;
				if (thisState == labelState) {
					sub = sub.conjunctionExtended(Literal.charValue(i), labelState);
					continue;
				}
			} else {
				if (labelState != thisState) {
					sub = sub.conjunctionExtended(Literal.charValue(i), thisState);
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
		Label sub = emptyLabel;
		if (this.isEmpty())
			return sub;
		if ((label == null) || label.isEmpty())
			return (inCommon) ? sub : this;
		for (byte i = (byte) (this.maxIndex + 1); (--i) >= 0;) {
			char thisState = get(i);
			if (thisState == Literal.ABSENT)
				continue;
			char labelState = label.get(i);
			if (inCommon) {
				if (labelState == Literal.ABSENT)
					continue;
				if (thisState == labelState) {
					sub = sub.conjunctionExtended(Literal.charValue(i), labelState);
					continue;
				}
				if (!strict) {
					sub = sub.conjunctionExtended(Literal.charValue(i), Literal.UNKNONW);
					continue;
				}
			} else {
				if (labelState == Literal.ABSENT) {
					sub = sub.conjunctionExtended(Literal.charValue(i), thisState);
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
			return Literal.valueOf(Literal.charValue(theDistinguished), get(theDistinguished));
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		// It is impossible to guarantee a unique hashCode for each possible label.
		return this.bit1 << this.maxIndex | this.bit0;
	}

	/**
	 * A literal l is consistent with a label if the last one does not contain ¬l.
	 * 
	 * @param literalIndex the index of the literal
	 * @param literalState its state
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
	 * @param lit the input literal
	 * @return true if lit is consistent with this label.
	 */
	public boolean isConsistentWith(Literal lit) {
		return isConsistentWith(Literal.index(lit.getName()), lit.getState());
	}

	/**
	 * @return true if the label contains no literal.
	 */
	public boolean isEmpty() {
		return this.size() == 0;
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
			literals[j++] = Literal.valueOf(Literal.charValue(i), thisState == Literal.NEGATED ? Literal.STRAIGHT : Literal.NEGATED);
		}
		return literals;
	}

	/**
	 * Returns a new label that is a copy of <code>this</code> without <code>proposition</code> if it is present.
	 * Removing a proposition means to remove all literal of the given proposition.
	 *
	 * @param proposition the proposition to remove.
	 * @return the new label.
	 */
	public Label remove(final char proposition) {
		return this.conjunction(proposition, Literal.ABSENT);
	}

	/**
	 * Returns a new label copy of <code>this</code> where all literals with names in <b>inputLabel</b> are removed.
	 *
	 * @param inputLabel names of literals to remove.
	 * @return the new label.
	 */
	public Label remove(Label inputLabel) {
		if (inputLabel == null)
			return this;

		int inputPropositions = inputLabel.bit0 | inputLabel.bit1;
		inputPropositions = ~inputPropositions;

		return valueOf(cacheIndex(this.bit1 & inputPropositions, this.bit0 & inputPropositions));
	}

	/**
	 * Returns a new label that is a copy of <code>this</code> where <code>literal</code> is removed if it is present.
	 * If label contains '¬p' and input literal is 'p', then the method returns a copy of <code>this</code>.
	 *
	 * @param literal the literal to remove
	 * @return the new label.
	 */
	public Label remove(final Literal literal) {
		byte index = Literal.index(literal.getName());
		if (get(index) == literal.getState()) {
			return this.conjunction(literal.getName(), Literal.ABSENT);
		}
		return this;
	}

	/**
	 * @return the number of literals of the label.
	 */
	public int size() {
		return this.count;
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
