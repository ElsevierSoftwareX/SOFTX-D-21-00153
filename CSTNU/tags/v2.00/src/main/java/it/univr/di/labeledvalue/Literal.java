package it.univr.di.labeledvalue;

/**
 * An immutable literal.
 * <p>
 * A literal is a char that <b>can</b> be preceded by the symbol {@value it.univr.di.labeledvalue.Constants#NOT}, negated literal, or by the symbol
 * {@value it.univr.di.labeledvalue.Constants#UNKNOWN}, 'unknown' literal.
 * <p>
 * While the semantics of a literal and its negation is the standard one, the semantics for unknown literal is particular of the CSTN/CSTNU application.<br>
 * An unknown literal, as '¿p' for example, is true if the value of proposition letter 'p' is not assigned yet. False otherwise.
 * <p>
 * Therefore, if a state is characterized by the proposition '¿p', it means that the state is valid till the value of proposition letter 'p' is unknown.
 * In the instant the value of 'p' is set, '¿p' became false and the associated state is not more valid.
 * <p>
 * A literal object is immutable and must have a propositional letter.
 * <p>
 * Lastly, for efficiency reasons, this class allows to represent literal using at most {@link Label#NUMBER_OF_POSSIBLE_PROPOSITIONS} propositions in the range
 * {@link #PROPOSITION_ARRAY}.
 * {@link Label#NUMBER_OF_POSSIBLE_PROPOSITIONS} is given by the fact that {@link Label} represents propositional labels using integer (32 bits), so labels with
 * at most 32 different propositions.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class Literal implements Comparable<Literal> {

	/**
	 * On 20171027 using VisualVM it has been shown that representing the state values of a literal using an enum consumes a lot of memory
	 * in this kind of application.<br>
	 * Therefore I decided to simplify the representation using 4 constants value: {@link #ABSENT}, {@link #STRAIGHT}, {@link #NEGATED}, and {@link #UNKNONW}.
	 * The char corresponding to each such constant is exploit in the class (to make them more efficient).
	 * So, don't change them without revising all the class.<br>
	 * {@link #ABSENT} is useful only for internal methods. It is not admitted for defining a literal.
	 */
	@SuppressWarnings("javadoc")
	public static final char ABSENT = '\u0000',
			STRAIGHT = '\u0001',
			NEGATED = Constants.NOT,
			UNKNONW = Constants.UNKNOWN;

	/**
	 * Literal object cache
	 */
	private static final Literal[] CREATED_LITERAL = new Literal[Label.NUMBER_OF_POSSIBLE_PROPOSITIONS * 3];

	/**
	 * List of possible proposition managed by this class.<br>
	 * Such list is made concatenating 2 blocks: a-z, and A-F.
	 * If such blocks are changed, please revise {@link #check(char)} and {@link #index(char)} methods because it exploits the bounds of such blocks.
	 * The length of this array cannot be modified without revising all this class code and {@link Label} class.
	 * 
	 * @see #PROPOSITIONS
	 */
	// 3 blocks: a-z, A-Z, α-μ.<br>
	public static final char[] PROPOSITION_ARRAY = {
			// 0 1 2 3 4 5 6 7 8 9
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
			'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
			'E', 'F'
			// 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			// 'α', 'β', 'γ', 'δ', 'ε', 'ζ', 'η', 'θ', 'ι', 'κ', 'λ', 'μ'
	};

	/**
	 * R.E. representation of allowed propositions.
	 */
	public static final String PROPOSITIONS = "a-zA-F";// "A-Za-z0-9α-μ";

	/**
	 * R.E. representation of {@link #PROPOSITION_ARRAY}
	 */
	public static final String PROPOSITION_RANGE = "[" + PROPOSITIONS + "]";

	/**
	 * @param state1 a possible state of a literal. No integrity check is done
	 * @param state2 a possible state of a literal. No integrity check is done
	 * @return true if state1 and state2 are complement. False otherwise
	 * @see #STRAIGHT
	 * @see #NEGATED
	 */
	static final boolean areComplement(char state1, char state2) {
		return (state1 == STRAIGHT && state2 == NEGATED) || (state1 == NEGATED && state2 == STRAIGHT);
	}

	/**
	 * @param i a positive value smaller than {@value Label#NUMBER_OF_POSSIBLE_PROPOSITIONS}.
	 * @return char at position i in {@link Literal#PROPOSITION_ARRAY}
	 * @impleSpec No parameter integrity-check is done
	 */
	static final char charValue(final int i) {
		return PROPOSITION_ARRAY[i];
	}

	/**
	 * @param c the char to check
	 * @return true if the char represents a valid literal identifier
	 */
	public static final boolean check(final char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'F');// 'Z') || ('α' <= c && c <= 'μ');
	}

	/**
	 * Returns the positive literal of {@code v}.
	 *
	 * @param v a char in the range {@link #PROPOSITION_RANGE}
	 * @return the straight literal of proposition v
	 */
	public static Literal valueOf(final char v) {
		return valueOf(v, STRAIGHT);
	}

	/**
	 * Return the literal having the given {@code state} of {@code v}.
	 *
	 * @param v the proposition letter
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}
	 * @return a literal with name {@code v} and state {@code state}
	 */
	public static Literal valueOf(final char v, char state) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not a letter!");
		if (state == ABSENT)
			throw new IllegalArgumentException("The state is not valid!");
		int hc = hashCode(v, state);
		Literal l = CREATED_LITERAL[hc];
		if (l == null) {
			l = new Literal(v, state);
			CREATED_LITERAL[hc] = l;
		}
		return l;
	}

	/**
	 * Returns a new literal having same proposition of {@code v} but with state given by {@code state}.
	 *
	 * @param v a non null literal v
	 * @param state one of possible state of a literal: {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}
	 * @return a new literal having same proposition of {@code v} but with state given by {@code state}.
	 */
	public static Literal valueOf(final Literal v, char state) {
		if (state == ABSENT || v == null)
			throw new IllegalArgumentException("The state or the input literal is not valid!");
		int hc = -1;
		Literal l = CREATED_LITERAL[hc = hashCode(v.name, state)];
		if (l == null) {
			l = new Literal(v.name, state);
			CREATED_LITERAL[hc] = l;
		}
		return l;
	}

	/**
	 * Returns the ordinal associate to {@code state}.
	 * 
	 * @implNote Since it is necessary to order literals, it is necessary to fix an order among possible state of a literal.
	 *           The order implemented by this method allows to be independent by the alphabetic order implied by the corresponding char of the state.}
	 * @param state One of the following value: {@value #NEGATED}, {@value #STRAIGHT}, {@value #UNKNONW}
	 * @return the ordinal associated to a proper state, a negative integer if the state is not recognized
	 */
	static final byte getStateOrdinal(char state) {
		switch (state) {
		case STRAIGHT:
			return 1;
		case NEGATED:
			return 2;
		case UNKNONW:
			return 3;
		case ABSENT:
			return 0;
		default:
			return -1;
		}
	}

	/**
	 * Hash code for a literal given as char {@code c} and state {@code state}.
	 * 
	 * @param c
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is done.
	 * @return an integer that is surely unique when 'a'<=c<='z'.
	 */
	static final int hashCode(char c, char state) {
		return index(c) * 3 + getStateOrdinal(state) - 1;// -1 because ABSENT is not admissible.
	}

	/**
	 * @param c
	 * @return the index of the given proposition {@code c} in {@link #PROPOSITION_ARRAY} if it is a proposition, a negative integer otherwise.
	 */
	static final byte index(final char c) {
		if ('a' <= c && c <= 'z')
			return (byte) (c - 'a');
		if ('A' <= c && c <= 'F') // if ('A' <= c && c <= 'Z')
			return (byte) ((c - 'A') + 26);// 26 is 'A' position in PROPOSITION_ARRAY
		// if ('α' <= c && c <= 'μ')
		// return (byte) ((c - 'α') + 52);// 26 is 'α' position in PROPOSITION_ARRAY
		return -1;
	}

	/**
	 * Parses the string {@code s} returning the literal represented.
	 *
	 * @param s It can be a single char ({@value #PROPOSITION_ARRAY}) or one of characters [{@value Constants#NOT} {@value Constants#UNKNOWN}]
	 *            followed by a char of {@value #PROPOSITION_ARRAY}. No spaces are allowed
	 * @return the literal represented by {@code s} if {@code s} is a valid representation of a literal, null otherwise
	 */
	public static final Literal parse(final String s) {
		int len;
		char p, state;
		if (s == null || (len = s.length()) > 2)
			return null;
		if (len == 1) {
			p = s.charAt(0);
			if (!Literal.check(p))
				return null;
			final Literal l = Literal.valueOf(p, STRAIGHT);
			return l;
		}
		state = s.charAt(0);
		p = s.charAt(1);
		if (!Literal.check(p))
			return null;
		if (state == Constants.NOT) {
			final Literal l = Literal.valueOf(p, NEGATED);
			return l;
		}
		if (state == Constants.UNKNOWN) {
			final Literal l = Literal.valueOf(p, UNKNONW);
			return l;
		}

		return null;
	}

	/**
	 * @param state
	 * @return the string representation of {@code state}
	 */
	static final String stateAsString(char state) {
		if (state <= STRAIGHT)
			return "";
		return String.valueOf(state);
	}

	/**
	 * @param propositionIndex
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}
	 * @return the char-array representation of a literal identified by its index and state parameter. If state is not correct, an empty array is returned
	 * @implSpec No parameter integrity-check is done
	 */
	static final char[] toChars(int propositionIndex, char state) {
		// Exploits the state fixed order.
		if (state > STRAIGHT)
			return new char[] { state, Literal.charValue(propositionIndex) };
		if (state == STRAIGHT)
			return new char[] { Literal.charValue(propositionIndex) };
		return new char[0];
	}

	/**
	 * Hash code cache.
	 */
	private int hashCodeCached;

	/**
	 * Immutable propositional letter.
	 */
	private char name;

	/**
	 * Immutable state.
	 */
	private char state;

	/**
	 * Makes the positive literal of {@code v}.
	 * This class is immutable. Use {@link #valueOf(char)}.
	 * 
	 * @param v a char
	 */
	private Literal(final char v) {
		this(v, STRAIGHT);
	}

	/**
	 * Makes a literal using {@code v} and {@code state}.
	 * This class is immutable, use {@link #valueOf(char, char)}
	 * 
	 * @param v the proposition letter
	 * @param state1 one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}
	 */
	private Literal(final char v, final char state1) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not an admissible proposition!");
		if (getStateOrdinal(state1) < 0)
			throw new IllegalArgumentException("The state is not an admissible one!");
		this.name = v;
		this.state = state1;
	}

	/**
	 * @implSpec When the two literals have the same name, it returns a different value than 0 if this and {@code o} have different state.
	 */
	@Override
	public int compareTo(final Literal o) {
		if (this.name < o.name)
			return -1;
		else if (this.name > o.name)
			return 1;
		// Since compareTo has to be consistent with equals, when the two names are equal,
		// it returns a different value than 0 if the two literals have different state.
		return getStateOrdinal(this.state) - getStateOrdinal(o.state);
	}

	@Override
	public final boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof Literal))
			return false;
		final Literal l = (Literal) o;
		return this.name == l.name && this.state == l.state;
	}

	/**
	 * Returns the complement of this.
	 * <p>
	 * The complement of a straight literal is the negated one.<br>
	 * The complement of a negated literal is the straight one.<br>
	 * The complement of a unknown literal is null (an empty literal is not possible).<br>
	 *
	 * @return a new literal that it is the negate of this. null if it is request the complement of unknown literal
	 */
	public final Literal getComplement() {
		if (this.state == UNKNONW || this.state == ABSENT)
			return null;
		return valueOf(this, (this.isNegated()) ? STRAIGHT : NEGATED);
	}

	/**
	 * Getter for the field <code>name</code>.
	 *
	 * @return the propositional letter associated to this
	 */
	public final char getName() {
		return this.name;
	}

	/**
	 * Returns a literal that is the negated of this.
	 *
	 * @return a new literal with the same name and state negated
	 */
	public final Literal getNegated() {
		return valueOf(this, NEGATED);
	}

	/**
	 * Getter for the field <code>state</code>.
	 *
	 * @return the state
	 */
	public final char getState() {
		return this.state;
	}

	/**
	 * Returns a literal that is the straight literal of this.
	 *
	 * @return a new literal with the same name and state straight
	 */
	public final Literal getStraight() {
		return valueOf(this, STRAIGHT);
	}

	/**
	 * Returns a literal that is the unknown literal of this.
	 *
	 * @return a new literal with the same name and state unknown
	 */
	public final Literal getUnknown() {
		return valueOf(this, UNKNONW);
	}

	@Override
	public final int hashCode() {
		if (this.hashCodeCached == 0) {
			this.hashCodeCached = Literal.hashCode(this.name, this.state);
		}
		return this.hashCodeCached;
	}

	/**
	 * @param l
	 * @return true if it is a complement literal of the given one
	 */
	public final boolean isComplement(Literal l) {
		if (l == null)
			return false;
		return areComplement(this.state, l.state);
	}

	/**
	 * @return true if it is a negated literal
	 */
	public final boolean isNegated() {
		return this.state == NEGATED;
	}

	/**
	 * @return true if it is a straight literal
	 */
	public final boolean isStraight() {
		return this.state == STRAIGHT;
	}

	/**
	 * @return true if it is a literal in the unknown state
	 */
	public final boolean isUnknown() {
		return this.state == UNKNONW;
	}

	/**
	 * @return the string representation of this. If the literal is a negated one, the propositional letter is prefixed by {@link Constants#NOT}.
	 *         If the literal is an unknown one, the propositional letter is prefixed by {@link Constants#UNKNOWN}
	 */
	@Override
	public final String toString() {
		return stateAsString(this.state) + this.name;
	}

}
