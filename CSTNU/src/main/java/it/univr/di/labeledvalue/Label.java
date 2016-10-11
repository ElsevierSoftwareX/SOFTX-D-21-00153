package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import it.univr.di.labeledvalue.Literal.State;

/**
 * <p>
 * Simple class to represent a <em>label</em> in the CSTN/CSTNU framework.<br>
 * A label is a (logic) conjunction of zero or more <em>literals</em> ({@link it.univr.di.labeledvalue.Literal}).<br>
 * A label without literals is called <em>empty label</em> and it is represented graphically as {@link it.univr.di.labeledvalue.Constants#EMPTY_LABEL}.<br>
 * A labels is <em>consistent</em> when it does not contains opposite literals.
 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> (<code>⊨ L' ⇒ L</code>).<br>
 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.</p>
 * <p>
 * Design assumptions
 * Since in CSTN(U) project the memory footprint of a label is an important aspect, after some experiments, I have found that the best way
 * to represent a label is to limit the possible propositions to the range [a...z] and to use two ints for representing the state of literals composing a label:
 * the two int are used in pair; each position of them is associated to a possible literal (position 0 to 'a',...,position 25 to 'z'); given a position,
 * the two corresponding bits in the two ints can represent all possible four states ({@link Literal.State} of the literal associated to the position.</p>
 * <p>
 * The following table represent execution times of some Label operations determined using different implementation of this class.</p>
 * <table border="1">
 * <caption>Execution time for some operations w.r.t the core data structure of
 * the class.</caption>
 * <tr>
 * <th>Method</th>
 * <th>TreeSet</th>
 * <th>ObjectAVLTreeSet</th>
 * <th>ObjectRBTreeSet</th>
 * <th>byte array</th>
 * <th>two int (Label)</th>
 * </tr>
 * <tr>
 * <td>Simple conjunction of '¬abcd' with '¬adefjs'='¬abcdefjs' (ms)</td>
 * <td>0.076961</td>
 * <td>0.066116</td>
 * <td>0.068798</td>
 * <td>0.001309</td>
 * <td>0.000299</td>
 * </tr>
 * <tr>
 * <td>Execution time for an extended conjunction of '¬abcd' with
 * 'a¬c¬defjs'='¿ab¿c¿defjs' (ms)</td>
 * <td>0.07583</td>
 * <td>0.024099</td>
 * <td>0.014627</td>
 * <td>0.000843</td>
 * <td>0.000203</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (inconsistent) labels are consistent.
 * Details '¬abcd' with 'a¬c¬defjs' (ms)</td>
 * <td>0.004016</td>
 * <td>0.001666</td>
 * <td>0.00166</td>
 * <td>0.00121</td>
 * <td>0.00075</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (consistent) labels are consistent.
 * Details '¬abcd' with '¬abcd' (ms)</td>
 * <td>0.01946</td>
 * <td>0.004457</td>
 * <td>0.004099</td>
 * <td>0.000392</td>
 * <td>0.000558</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the
 * literal is the last inserted) (ms)</td>
 * <td>5.48E-4</td>
 * <td>4.96E-4</td>
 * <td>5.01E-4</td>
 * <td>2.69E-4</td>
 * <td>5.03E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the
 * literal is not present) (ms)</td>
 * <td>6.48E-4</td>
 * <td>7.71E-4</td>
 * <td>5.96E-4</td>
 * <td>1.84E-4</td>
 * <td>3.07E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition
 * letter (the literal is present) (ms)</td>
 * <td>0.003272</td>
 * <td>1.83E-4</td>
 * <td>1.09E-4</td>
 * <td>1.27E-4</td>
 * <td>1.60E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition
 * letter (the literal is not present) (ms)</td>
 * <td>0.002569</td>
 * <td>1.68E-4</td>
 * <td>1.0E-4</td>
 * <td>1.04E-4</td>
 * <td>1.60E-4</td>
 * </tr>
 * </table>
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
	public static final class EmptyLabel extends Label {
		/**
		 * 
		 */
		public EmptyLabel() {
			super();
		};

		public final boolean conjunct(char c, State s) {
			throw new IllegalAccessError("Read only object!");
		}

		public final boolean conjunct(Literal l) {
			throw new IllegalAccessError("Read only object!");
		}

		public final boolean conjunctExtended(char c, State s) {
			throw new IllegalAccessError("Read only object!");
		}

		public final boolean conjunctExtended(Literal l) {
			throw new IllegalAccessError("Read only object!");
		}
	}

	/**
	 * A constant empty label to represent an empty label that cannot be modified.
	 */
	public static final Label emptyLabel = new EmptyLabel();

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
		if ((baseElements == null) || (baseElements.length == 0))
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
	 * Returns a label containing the propositions specified by c. The propositions with index in correspondence to bit = 1 in the parameter 'index' are set to
	 * negative state.
	 *
	 * @param proposition an array of propositions.
	 * @param index
	 * @return a label copy of label but with literals having indexes corresponding to bits 1 in the parameter 'index' set to negative state.
	 *         If label is null or empty or contains UNKNOWN literals, returns null;
	 */
	private static final Label complementGenerator(final char[] proposition, final int index) {
		int n;
		if (proposition == null || (n = proposition.length) == 0 || index < 0 || index > Math.pow(2, n))
			return null;
		int j = 1;
		final Label newLabel = new Label();
		for (int i = 0; i < n; i++, j <<= 1) {
			// if (lit.contains(labelStraight)) // return null;// l contains two times at least the same proposition letter. Ignore! lit is a set, it manage it!
			State state = ((j & index) != 0) ? State.negated : State.straight;
			newLabel.set(Literal.index(proposition[i]), state);
		}
		return newLabel;
	}

	/**
	 * Proposes only some execution time estimates about some class methods.
	 *
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		final int nTest = 1000;
		final double msNorm = 1.0 / (1000000.0 * nTest);

		final Literal d = Literal.create('d'), z = Literal.create('x');

		Label empty = Label.emptyLabel;
		System.out.println("Empty: " + empty);
		Label result = new Label();
		// System.out.println("Empty: " + result);
		result = new Label('a', State.straight);
		System.out.println("a: " + result);
		result = new Label('b', State.negated);
		System.out.println("¬b: " + result);
		result = new Label('a', State.absent);
		System.out.println("Null: " + result);

		Label l1 = Label.parse(Constants.NOT + "abcd");
		Label l2 = Label.parse(Constants.NOT + "aejfsd");
		System.out.println("l1: " + l1 + "\nl2: " + l2);
		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunction(l2);
		}
		long endTime = System.nanoTime();
		System.out.println(
				"Execution time for a simple conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime) * msNorm));

		l1 = Label.parse(Constants.NOT + "abcd");
		l2 = Label.parse("a¬d¬cejfs");
		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunctionExtended(l2);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for an extended conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l2);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (inconsistent) labels are consistent. Details '" + l1 + "' with '" + l2 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l1);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (consistent) labels are consistent. Details '" + l1 + "' with '" + l1 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(d);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (the literal is the last inserted) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('d');
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (given the name) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(z);
		}
		endTime = System.nanoTime();
		System.out.println(
				"Execution time for checking if a literal is present in a label (the literal is not present) (ms): " + ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('d');
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is present) (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains('z');
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is not present) (ms): "
				+ ((endTime - startTime) * msNorm));
	}

	/**
	 * Parse a string representing a label and return an equivalent Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link it.univr.di.labeledvalue.Constants#LabelRE}.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return a Label object corresponding to the label string representation.
	 */
	public static final Label parse(String s) {
		if (s == null)
			return null;
		
		//FIX for past label in mixed case
		s = s.toLowerCase();
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
		State literalStatus;
		i = 0;
		while (i < n) {
			c = s.charAt(i);
			if (c == Constants.NOT || c == Constants.UNKNOWN) {
				char sign = c;
				if (++i>=n) return null;
				c = s.charAt(i);
				if (!Literal.check(c))
					return null;
				// l = Literal.create(c, (sign == Constants.NOT) ? State.negated
				// : State.unknown);
				literalIndex = Literal.index(c);
				literalStatus = (sign == Constants.NOT) ? State.negated : State.unknown;
			} else {
				if (!Literal.check(c))
					return null;
				literalIndex = Literal.index(c);
				literalStatus = State.straight;
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
	 * One integer has 32 bits.<br>
	 * Using two integers, it is possible to represent 4 states for each position 1..32.<br>
	 * Each position is associated to a proposition.<br>
	 * Since {@link Constants#LabelRE} declares 26 propositions, only 26 position of the two integer are used.<br>
	 * 
	 * <pre>
	 * Status of i-th literal
	 *              bit1[i] bit0[i]
	 * not present          0   0
	 * negated              0   1
	 * straight             1   0
	 * unknown              1   1
	 * </pre>
	 */
	@SuppressWarnings("javadoc")
	private int bit1, bit0;

	/**
	 * Index of the last significant literal of label w.r.t. lexicographical order.
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
		bit0 = bit1 = 0;
		maxIndex = -1;
		cacheOfSize = 0;
	}

	/**
	 * Constructs a label with a proposition having a state.
	 *
	 * @param proposition
	 * @param state
	 * @see Literal.State
	 */
	public Label(final char proposition, final State state) {
		this();
		if (state != State.absent) {
			byte index = Literal.index(proposition);
			this.set(index, state);
			maxIndex = index;
		}
	}

	/**
	 * Constructs a label cloning the given label l.
	 * 
	 * @param label the label to clone. If null, this will be an empty label.
	 */
	public Label(final Label label) {
		this();
		if (label == null || label == emptyLabel) {
			return;
		}
		bit0 = label.bit0;
		bit1 = label.bit1;
		maxIndex = label.maxIndex;
		cacheOfSize = label.cacheOfSize;
	}

	/**
	 * Makes the label empty.
	 */
	public void clear() {
		bit1 = bit0 = 0;
		maxIndex = -1;
		cacheOfSize = 0;
	}

	/**
	 * {@inheritDoc} Determines a lexicographical order between labels based on the natural order of type {@link Literal}.
	 */
	@Override
	public int compareTo(final Label label) {
		if (label == null)
			return 1;
		byte i = 0, j = 0, cmp = 0;
		State thisState, labelState;
		while (i <= maxIndex && j <= label.maxIndex) {
			while ((thisState = get(i)) == State.absent && i <= maxIndex) {
				i++;
			}
			while ((labelState = label.get(j)) == State.absent && j <= label.maxIndex) {
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
		if (i > maxIndex) {
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
	 * It conjuncts <code>proposition</code> to this if <code>this</code> is consistent with <code>proposition</code> and its <code>propositionState</code>.
	 *
	 * @param proposition the proposition to conjunct. It cannot have an UNKNOWN state.
	 * @param propositionState Literal.State of proposition
	 * @return true if proposition is added, false otherwise.
	 */
	public boolean conjunct(final char proposition, final State propositionState) {
		if (propositionState == null || propositionState == State.absent || propositionState == State.unknown)
			return false;
		byte propIndex = Literal.index(proposition);
		State st = get(propIndex);
		if (st.isComplement(propositionState))
			return false;
		if (st == propositionState)
			return true;
		set(propIndex, propositionState);
		if (propIndex > maxIndex)
			maxIndex = propIndex;
		return true;
	}

	/**
	 * Helper method for {@link it.univr.di.labeledvalue.Label#conjunct(char, State)}
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
	 * @param literalState state of the proposition {@link Literal.State}.
	 * @return true if proposition is added, false otherwise.
	 */
	public boolean conjunctExtended(final char proposition, State literalState) {
		if (literalState == null || literalState == State.absent)
			return false;
		byte index = Literal.index(proposition);
		State st = get(index);
		if (literalState == State.unknown || st.isComplement(literalState)) {
			literalState = State.unknown;
		}
		if (index > maxIndex)
			maxIndex = index;
		set(index, literalState);
		return true;
	}

	/**
	 * Helper method {@link #conjunctExtended(char, State)}
	 * 
	 * @param literal a literal 
	 * @return true if literal has been added.
	 */
	public boolean conjunctExtended(final Literal literal) {
		return conjunctExtended(literal.getName(), literal.getState());
	}

	/**
	 * Conjuncts <code>label</code> to this if <code>this</code> is consistent with <code>label</code> and returns the results without modifying
	 * <code>this</code>.
	 *
	 * @param label the label to conjunct
	 * @return a new label with the conjunction of 'this' and 'label' if they are consistent, null otherwise.<br>
	 *         null also if this or label contains unknown literals. 'this' is not altered by this method.
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

		final Label newLabel = new Label();
		newLabel.bit0 = unionB0;
		newLabel.bit1 = unionB1;
		newLabel.maxIndex = (label.maxIndex > maxIndex) ? label.maxIndex : maxIndex;
		newLabel.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return newLabel;
	}

	/**
	 * Create a new label that represents the conjunction of <code>this</code> and <code>label</code> using also {@link Literal.State#unknown} literals.
	 * A {@link Literal.State#unknown} literal represent the fact that in the two input labels a proposition letter is present as straight state in
	 * one label and in negated state in the other.<br>
	 * For a detail about the conjunction of unknown literals, see {@link #conjunctExtended(char, State)}.
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

		final Label newLabel = new Label();
		newLabel.bit0 = unionB0;
		newLabel.bit1 = unionB1;
		newLabel.maxIndex = (label.maxIndex > maxIndex) ? label.maxIndex : maxIndex;
		newLabel.cacheOfSize = -1;// it has to be calculated... delay the stuff.
		return newLabel;
	}

	/**
	 * @param proposition the proposition to check.
	 * @return true if this contains proposition in any state: straight, negated or unknown.
	 */
	public boolean contains(final char proposition) {
		return get(Literal.index(proposition)) != State.absent;
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
		return (bit0 & bit1) != 0;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof Label))
			return false;
		final Label l1 = (Label) obj;
		return this.bit1 == l1.bit1 && this.bit0 == l1.bit0;
	}

	/**
	 * @param literalIndex the index of the literal to retrieve.
	 * @return the status of literal with index literalIndex. If the literal is not present, it returns {@link Literal.State#absent}.
	 */
	private final State get(final byte literalIndex) {
		int mask = 1 << literalIndex;
		int b1 = ((bit1 & mask) != 0) ? 2 : 0;
		int b0 = ((bit0 & mask) != 0) ? 1 : 0;
		return State.values()[b1 + b0];
	}

	/**
	 * @return The array of literal with unknown status in this label.
	 */
	public char[] getAllUnknown() {
		if (maxIndex <= 0)
			return new char[0];
		char[] indexes = new char[size()];
		int j = 0;
		for (byte i = 0; i <= maxIndex; i++) {
			if (get(i) == State.unknown) {
				indexes[j++] = Literal.charValue(i);
			}
		}
		return Arrays.copyOf(indexes, j);
	}

	/**
	 * @return The array of proposition of present literals in this label.
	 */
	public Literal[] getLiterals() {
		Literal[] indexes = new Literal[size()];
		int j = 0;
		State state;
		for (byte i = 0; i <= maxIndex; i++) {
			if ((state = get(i)) != State.absent) {
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
		for (byte i = 0; i <= maxIndex; i++) {
			if (get(i) != State.absent) {
				indexes[j++] = Literal.charValue(i);
			}
		}
		return indexes;
	}

	/**
	 * @param c the name of literal
	 * @return the state of literal with name c if it is present, {@link Literal.State#absent} otherwise.
	 */
	public final State getStateLiteralWithSameName(final char c) {
		return get(Literal.index(c));
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
		for (byte i = (byte) (maxIndex + 1); (--i) >= 0;) {
			State thisState = get(i);
			if (thisState == State.absent)
				continue;
			State labelState = label.get(i);
			if (inCommon) {
				if (labelState == State.absent)
					continue;
				if (thisState == labelState) {
					sub.set(i, labelState);
					continue;
				}
				if (!strict) {
					sub.set(i, State.unknown);
					continue;
				}
			} else {
				if (labelState == State.absent) {
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
		for (byte i = (byte) (maxIndex + 1); (--i) >= 0;) {
			State thisState = get(i);
			if (thisState == State.absent)
				continue;
			State labelState = label.get(i);
			if (inCommon) {
				if (labelState == State.absent)
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
		State thisState = State.absent;
		for (byte i = (byte) (maxIndex + 1); (--i) >= 0;) {
			thisState = get(i);
			State labelState = label.get(i);
			if (thisState == labelState)
				continue;
			if (thisState == State.unknown || labelState == State.unknown || thisState == State.absent || labelState == State.absent)
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

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return bit1 << maxIndex | bit0;
	}

	/**
	 * A literal l is consistent with a label if the last one does not contain ¬l.
	 * 
	 * @param literalIndex
	 * @param literalState
	 * @return true if the literal is consistent with this label.
	 */
	boolean isConsistentWith(final byte literalIndex, State literalState) {
		if (literalState == null || literalState == State.absent)
			return true;
		State thisState = get(literalIndex);
		if (thisState == State.unknown && literalState != State.unknown)
			return false;
		if (thisState != State.unknown && literalState == State.unknown)
			return false;
		return (!thisState.isComplement(literalState));
	}

	/**
	 * <p>A label L<sub>1</sub> is consistent with a label L<sub>2</sub> if L<sub>1</sub> &wedge; L<sub>2</sub> is satisfiable.<br>
	 * L<sub>1</sub> subsumes L<sub>2</sub> implies L<sub>1</sub> is consistent with L<sub>2</sub> but not vice-versa.</p>
	 * <p>
	 * If L<sub>1</sub> contains an unknown literal ¿p, and L<sub>2</sub> contains p/¬p, then the conjunction L<sub>1</sub> &wedge; L<sub>2</sub>
	 * is not defined while ¿p subsumes p/¬p is true.</p>
	 * <p>
	 * In order to considering also labels with unknown literals, this method considers the {@link #conjunctionExtended(Label)}:
	 * L<sub>1</sub> &#x2605; L<sub>2</sub>, where ¿p are used for represent the conjunction of opposite literals p and ¬p or literals like ¿p and p/¬p.<br>
	 * Now, it holds ¿p &#x2605; p = ¿p is satisfiable.</p>
	 * 
	 * @param label the label to check
	 * @return true if the label is consistent with this label.
	 */
	public boolean isConsistentWith(final Label label) {
		if (label == null || label.isEmpty() || this.isEmpty())
			return true;
		/**
		 * Method 1.
		 * We consider L<sub>1</sub> &#x2605; L<sub>2</sub>,
		 */
		// both this and label have bit0 | bit1 != 0
		int xBit0 = this.bit0 ^ label.bit0;// each bit=1 corresponds to 1) a negative literal present only in one label
		// or 2) a unknown literal present only in one label
		// or 3) a negative literal present in one label and the opposite present in the other
		int xBit1 = this.bit1 ^ label.bit1;// each bit=1 corresponds to 1) a positive literal present only in one label
		// or 2) a unknown literal present only in one label
		// or 3) a positive literal present in one label and the opposite present in the other
		int aBit = xBit0 & xBit1;
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
	 * @see Label#isConsistentWith(byte, State)
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
		return (bit1 == 0 && bit0 == 0);
	}

	/**
	 * Since a label is a conjunction of literals, its negation is a disjunction of negative literals (i.e., not a label).
	 * <p>
	 * The negation operator returns a set of all negative literals of this label.<br>
	 * Bear in mind that the complement of a literal with unknown state is a null object.
	 *
	 * @return the set of all negative literal of this. If this is empty, returns null;
	 */
	public Literal[] negation() {
		if (this.isEmpty())
			return null;

		final Literal[] literals = new Literal[size()];
		int j = 0;
		for (byte i = 0; i <= maxIndex; i++) {
			State thisState = get(i);
			if (thisState == State.absent || thisState == State.unknown)
				continue;
			literals[j++] = Literal.create(Literal.charValue(i), thisState == State.negated ? State.straight : State.negated);
		}
		return literals;
	}

	/**
	 * It removes proposition if it is present, otherwise it does nothing.
	 * Removing a proposition means to remove all literal of the given proposition.
	 *
	 * @param proposition the proposition to remove.
	 */
	public void remove(final char proposition) {
		set(Literal.index(proposition), State.absent);
	}

	/**
	 * It removes all literals with names in <b>inputSet</b> from the current label.
	 *
	 * @param inputSet names of literals to remove.
	 */
	public void remove(char[] inputSet) {
		if (inputSet == null)
			return;
		for (int i = inputSet.length; (--i) >= 0;) {
			set(Literal.index(inputSet[i]), State.absent);
		}
	}

	/**
	 * It removes literal (proposition with a Literal.State) if it is present, otherwise it does nothing.
	 * If label contains '¬p' and input literal is 'p', then the method does nothing.
	 *
	 * @param literal the literal to remove
	 * @return true if the literal is removed.
	 */
	public boolean remove(final Literal literal) {
		byte index = Literal.index(literal.getName());
		if (get(index) == literal.getState()) {
			set(index, State.absent);
			return true;
		}
		return false;
	}

	/**
	 * @param literalIndex the index of the literal to update.
	 * @param literalStatus the new state.
	 */
	private final void set(final byte literalIndex, final State literalStatus) {
		/**
		 * <pre>
		 * Status of i-th literal
		 *             bit1[i] bit0[i]
		 * not present       0 0
		 * negated           0 1
		 * straight          1 0
		 * unknown           1 1
		 * </pre>
		 */
		int mask = 1 << literalIndex;
		switch (literalStatus) {
		case straight:
			if (((bit1 | bit0) & mask) == 0)
				cacheOfSize++;
			bit1 |= mask;
			mask = ~mask;
			bit0 &= mask;
			if (maxIndex < literalIndex)
				maxIndex = literalIndex;
			return;
		case negated:
			if (((bit1 | bit0) & mask) == 0)
				cacheOfSize++;
			bit0 |= mask;
			mask = ~mask;
			bit1 &= mask;
			if (maxIndex < literalIndex)
				maxIndex = literalIndex;
			return;
		case unknown:
			if (((bit1 | bit0) & mask) == 0)
				cacheOfSize++;
			bit1 |= mask;
			bit0 |= mask;
			if (maxIndex < literalIndex)
				maxIndex = literalIndex;
			return;
		case absent:
		default:
			if (((bit1 | bit0) & mask) != 0)
				cacheOfSize--;
			mask = ~mask;
			bit1 &= mask;
			bit0 &= mask;
			if (maxIndex == literalIndex) {
				int u = bit1 | bit0;
				mask = ~mask;
				do {
					maxIndex--;
					mask = mask >>> 1;
				} while ((u & mask) == 0 && maxIndex >= 0);
			}
			return;
		}
	}

	/**
	 * @return Return the number of literals of the label
	 */
	public int size() {
		if (cacheOfSize >= 0) {
			return cacheOfSize;
		}
		byte _cacheOfSize = 0;
		int or = bit0 | bit1;
		for (int i = maxIndex + 1; (--i) >= 0;) {
			_cacheOfSize += (or & 1);
			or = or >>> 1;
		}
		cacheOfSize = _cacheOfSize;
		return _cacheOfSize;
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
		int max = (maxIndex > label.maxIndex) ? maxIndex : label.maxIndex;
		for (byte i = (byte) (max + 1); (--i) >= 0;) {
			State thisState = get(i);
			State labelState = label.get(i);
			if (thisState == labelState || labelState == State.absent)
				continue;
			/**
			 * When labelState[i] != thisState[i], before saying that it is false, it must be checked if thisState[i] is a ¿.
			 * ¿p subsumes p, ¿p subsumes ¬p, ¿p subsumes ¿p
			 * p NOT subsumes ¿p, ¬p NOT subsumes ¿p.
			 */
			if (thisState != State.unknown)
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
			return String.valueOf(Constants.EMPTY_LABEL);
		final StringBuilder s = new StringBuilder();
		State st;
		for (byte i = 0; i <= maxIndex; i++) {
			st = get(i);
			if (st != State.absent)
				s.append(Literal.toString(i, st));
		}
		return s.toString();
	}
}
