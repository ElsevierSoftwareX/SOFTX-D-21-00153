package it.univr.di.labeledvalue;

/**
 * Represents a immutable literal.
 * <p>
 * A literal is a char that can be preceded by the symbol {@link it.univr.di.labeledvalue.Constants#NOT}, negated literal, or by the symbol
 * {@link it.univr.di.labeledvalue.Constants#UNKNOWN}, 'unknown' literal.
 * <p>
 * While the semantics of a literal and its negation is the standard one, the semantics for unknown literal is particular of the CSTN/CSTNU application.<br>
 * An unknown literal, as '¿p' for example, is true if the value of proposition letter 'p' is not assigned yet. False otherwise.
 * <p>
 * Therefore, if a state is characterized by the proposition '¿p', it means that the state is valid till the value of proposition letter 'p' is unknown.
 * In the instant the value of 'p' is set, '¿p' became false and the associated state is not more valid.
 * <p>
 * A literal object is immutable and must have a propositional letter.
 * <p>
 * Lastly, for efficiency reasons, this class allows to represent literal using at most 64 propositions in the range {@link #PROPOSITION_ARRAY}.
 * 64 is given by the fact that {@link Label} represents labels using integer (64 bits), so label with at most 64 different propositions.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class Literal implements Comparable<Literal> {

	/**
	 * On 20171027 using VisualVM it has been shown that representing the state values of a literal using an enum consumes a lot of memory
	 * in this kind of application.<br>
	 * Therefore I decided to simplify the representation using 4 constants value: {@link #ABSENT}, {@link #STRAIGHT}, {@link #NEGATED}, and {@link #UNKNONW}.
	 * The char corresponding to each such constant is exploit in the method (to make them more efficient).
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
	 * Such list is made concatenating 3 blocks: a-z, A-Z, α-μ.<br>
	 * If such blocks are changed, please revise {@link #check(char)} and {@link #index(char)} methods because it exploits the bounds of such blocks.
	 * The length of this array cannot be modified without revising all this class code and {@link Label} class.
	 * 
	 * @see #PROPOSITIONS
	 */
	public static final char[] PROPOSITION_ARRAY = {
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'α', 'β', 'γ', 'δ', 'ε', 'ζ', 'η', 'θ', 'ι', 'κ', 'λ', 'μ' };

	/**
	 * Number of blocks of consecutive proposition in {@link #PROPOSITION_ARRAY}.
	 */
	public static final int PROPOSITION_BLOCKS = 3;

	/**
	 * R.E. representation of {@link #PROPOSITION_ARRAY}
	 */
	public static final String PROPOSITION_RANGE = "[A-Za-z0-9α-μ]";

	/**
	 * R.E. representation of allowed propositions.
	 */
	public static final String PROPOSITIONS = "A-Za-z0-9α-μ";


	/**
	 * @param state1 a possible state of a literal. No integrity check is done.
	 * @param state2 a possible state of a literal. No integrity check is done.
	 * @return true if state1 and state2 are complement. False otherwise.
	 * @see #STRAIGHT
	 * @see #NEGATED
	 */
	static final boolean areComplement(char state1, char state2) {
		return (state1 == STRAIGHT && state2 == NEGATED) || (state1 == NEGATED && state2 == STRAIGHT);
	}

	/**
	 * @param i
	 * @return char at position i in {@link Literal#PROPOSITION_ARRAY}.
	 */
	static final char charValue(final int i) {
		if (i < 0 || i > Label.NUMBER_OF_POSSIBLE_PROPOSITIONS)
			throw new IllegalArgumentException("Index '" + i + "' is not valid.");
		return PROPOSITION_ARRAY[i];
	}

	/**
	 * @param c
	 * @return true if the char represents a valid literal identifier
	 */
	public static final boolean check(final char c) {
		return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || ('α' <= c && c <= 'μ');
	}

	/**
	 * Simple constructor of a positive literal.
	 *
	 * @param v a char.
	 * @return the straight literal of proposition v
	 */
	public static Literal create(final char v) {
		return create(v, STRAIGHT);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}.
	 * @return a literal with name v and state state.
	 */
	public static Literal create(final char v, char state) {
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
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}.
	 * @return a copy of literal v but with state state.
	 */
	public static Literal create(final Literal v, char state) {
		if (state == ABSENT)
			throw new IllegalArgumentException("The state is not valid!");
		int hc = -1;
		Literal l = CREATED_LITERAL[hc = hashCode(v.name, state)];
		if (l == null) {
			l = new Literal(v, state);
			CREATED_LITERAL[hc] = l;
		}
		return l;
	}

	/**
	 * Since it is necessary to order literals, it is necessary to fix an order among possible state of a literal.
	 * The order implemented by this method allows to be independent by the alphabetic order implied by the corresponding char of the state.
	 * 
	 * @param state
	 * @return the ordinal associated to a proper state, -1 if the state is not recognized.
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
	 * Hash code for a literal given as char and state.
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
	 * @return the index of the given proposition in {@link #PROPOSITION_ARRAY} if it is a proposition, -1 otherwise.
	 */
	static final byte index(final char c) {
		if ('a' <= c && c <= 'z')
			return (byte) (c - 'a');
		if ('A' <= c && c <= 'Z')
			return (byte) ((c - 'A') + 26);// 26 is 'A' position in PROPOSITION_ARRAY
		if ('α' <= c && c <= 'μ')
			return (byte) ((c - 'α') + 52);// 26 is 'α' position in PROPOSITION_ARRAY
		return -1;
	}

	/**
	 * Parse a string returning the literal represented.
	 *
	 * @param s It can be a single char ([a-z]) or the character ¬ [\u00ac] followed by a char. If the argument is null or not valid, it returns null.
	 * @return the literal represented by 's' if 's' is a valid representation of a literal, null otherwise.
	 */
	public static final Literal parse(final String s) {
		if (s == null || s.length() > 2)
			return null;
		if (s.length() == 1) {
			if (!Literal.check(s.charAt(0)))
				return null;
			final Literal l = Literal.create(s.charAt(0), STRAIGHT);
			return l;
		}
		if (s.charAt(0) == Constants.NOT) {
			if (!Literal.check(s.charAt(1)))
				return null;
			final Literal l = Literal.create(s.charAt(1), NEGATED);
			return l;
		}
		return null;
	}

	/**
	 * @param state
	 * @return the char representing the state.
	 */
	static final String stateAsString(char state) {
		if (state <= STRAIGHT)
			return "";
		return String.valueOf(state);
	}

	/**
	 * @param propositionIndex
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is
	 *            done.
	 * @return the string representation of a literal identified by its index and state parameter.
	 */
	static final char[] toChars(int propositionIndex, char state) {
		// optimized!
		if (state > STRAIGHT)
			return new char[] { state, Literal.charValue(propositionIndex) };
		if (state == STRAIGHT)
			return new char[] { Literal.charValue(propositionIndex) };
		return new char[0];
	}

	/**
	 * @param proposition
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is
	 *            done.
	 * @return the string representation of a literal identified by proposition and state parameter.
	 */
	static final String toString(char proposition, char state) {
		return stateAsString(state) + proposition;
	}

	/**
	 * @param propositionIndex
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is
	 *            done.
	 * @return the string representation of a literal identified by its index and state parameter.
	 */
	static final String toString(int propositionIndex, char state) {
		return stateAsString(state) + Literal.charValue(propositionIndex);
	}

	/**
	 * hashcode cache
	 */
	private int hashCode;

	/**
	 * Propositional letter. It cannot be changed!
	 */
	private char name;

	/**
	 * It is true if the literal is the negation of the variable.
	 */
	private char state;

	/**
	 * Simple constructor of a positive literal.
	 *
	 * @param v a char.
	 */
	private Literal(final char v) {
		this(v, STRAIGHT);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is
	 *            done.
	 */
	private Literal(final char v, final char state) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not an admissible proposition!");
		this.name = v;
		this.state = state;
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal {@link #NEGATED} or {@link #STRAIGHT} o {@link #UNKNONW}. No integrity check is
	 *            done.
	 */
	private Literal(final Literal v, final char state) {
		this.name = v.name;
		this.state = state;
	}

	/**
	 * {@inheritDoc} Implements the lexical order.
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

	/** {@inheritDoc} */
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
	 * The complement of a straight literal is the negated one.<br>
	 * The complement of a negated literal is the straight one.<br>
	 * The complement of a unknown literal is null (an empty literal is not possible).<br>
	 *
	 * @return a new literal that it is the negate of this. null if it is request the complement of unknown literal.
	 */
	public final Literal getComplement() {
		if (this.state == UNKNONW)
			return null;
		return create(this, (this.isNegated()) ? STRAIGHT : NEGATED);
	}

	/**
	 * Getter for the field <code>name</code>.
	 *
	 * @return the propositional letter associated to this literal.
	 */
	public final char getName() {
		return this.name;
	}

	/**
	 * getNegated.
	 *
	 * @return a new literal with the same name and state negated.
	 */
	public final Literal getNegated() {
		return create(this, NEGATED);
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
	 * getStraight.
	 *
	 * @return a new literal with the same name and state straight.
	 */
	public final Literal getStraight() {
		return create(this, STRAIGHT);
	}

	/**
	 * getUnknown.
	 *
	 * @return a new literal with the same name and state unknown.
	 */
	public final Literal getUnknown() {
		return create(this, UNKNONW);
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		if (this.hashCode == 0) {
			this.hashCode = Literal.hashCode(this.name, this.state);
		}
		return this.hashCode();
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
	 * isNegated.
	 *
	 * @return true if it is a negated literal.
	 */
	public final boolean isNegated() {
		return this.state == NEGATED;
	}

	/**
	 * isStraight.
	 *
	 * @return true if it is a straight literal.
	 */
	public final boolean isStraight() {
		return this.state == STRAIGHT;
	}

	/**
	 * isUnknown.
	 *
	 * @return true if it is a literal in the unknown state.
	 */
	public final boolean isUnknown() {
		return this.state == UNKNONW;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return stateAsString(this.state) + this.name;
	}

}
