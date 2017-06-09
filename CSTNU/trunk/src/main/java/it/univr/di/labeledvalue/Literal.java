package it.univr.di.labeledvalue;

import java.util.logging.Logger;

/**
 * Simple class to represent a literal.
 * <p>
 * A literal is a char that can be preceded by the symbol {@link it.univr.di.labeledvalue.Constants#NOT}, negated literal, or by the symbol
 * {@link it.univr.di.labeledvalue.Constants#UNKNOWN}, 'unknown' literal.
 * <p>
 * While the semantics of a literal and its negation is the standard one, the semantics for unknown literal is particular of the CSTN/CSTNU application.<br>
 * An unknown literal, as '¿p' for example, is true if the value of proposition letter 'p' is not assigned yet. False otherwise.
 * <p>
 * Therefore, if a state is characterized by the proposition '¿p', it means that the state is valid till the value of proposition letter 'p' is unknown. In the
 * instant the value of 'p' is set, '¿p' became false and the associated event is not more valid.
 * <p>
 * A literal object is immutable and must have a propositional letter.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class Literal implements Comparable<Literal> {

	/**
	 * A literal can be: negated or straight or unknown.<br>
	 * The meaning of 'unknown' is exemplified in the documentation of the class {@link Literal}.
	 * <p>
	 * Do not change the order of values! Some methods in the package it.univr.di.labeledvalue.* exploit such order!!!
	 */
	public static enum State {
		/**
		 * Not present. Useful only for BitLabel class.
		 */
		absent,
		/**
		 * A negated literal is true if the truth value assigned to its proposition letter is false, false otherwise.
		 */
		negated,
		/**
		 * A straight literal is true if the truth value assigned to its proposition letter is true, false otherwise.
		 */
		straight,
		/**
		 * An unknown literal, as '¿p' for example, is true if the value of proposition letter 'p' is not assigned yet. False otherwise.
		 */
		unknown;

		/**
		 * @param st
		 * @return true if this is opposite to state st. False, otherwise.
		 */
		public boolean isComplement(State st) {
			// return (this == State.straight && st == State.negated) || (this == State.negated && st == State.straight);
			return (this.ordinal() * st.ordinal()) == 2;
		}

		@Override
		public String toString() {
			switch (this) {
			case negated:
				return Constants.NOTstring;
			case unknown:
				return Constants.UNKNOWNstring;
			default:
				return "";
			}
		}
	}

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Literal.class.getName());

	/**
	 * Literal object cache
	 */
	private static final Literal[] literalCreated = new Literal[Constants.NUMBER_OF_POSSIBLE_PROPOSITION * 3];

	/**
	 * 
	 */
	private static int[] propositionBlockUpperBound;

	static {
		propositionBlockUpperBound = new int[Constants.PROPOSITIONS_BLOCKS];
		int n = 0;
		for (int i = 0; i < Constants.PROPOSITIONS_BLOCKS; i++) {
			n = Constants.LAST_PROPOSITION[i] - Constants.FIRST_PROPOSITION[i];
			// LOG.finest("n: "+n);
			if (i == 0)
				propositionBlockUpperBound[i] = n;
			else
				propositionBlockUpperBound[i] = propositionBlockUpperBound[i - 1] + n + 1;
		}
	}

	/**
	 * @param i
	 * @return char at position i w.r.t. the block of possible chars as defined in {@link Constants#PROPOSITION_RANGES}.
	 */
	static public final char charValue(final int i) {
		if (i < 0 || i > Constants.NUMBER_OF_POSSIBLE_PROPOSITION)
			throw new IllegalArgumentException("Index '" + i + "' is not valid.");
		int k = 0;
		for (int j = 0; j < Constants.PROPOSITIONS_BLOCKS; j++) {
			if (i <= propositionBlockUpperBound[j]) {
				// LOG.warning("Constants.FIRST_PROPOSITION[j]:" + Constants.FIRST_PROPOSITION[j] + " + " + i + "- " + k);
				return (char) (Constants.FIRST_PROPOSITION[j] + i - k);
			}
			k = propositionBlockUpperBound[j] + 1;
		}
		// return (char) (Constants.FirstPossibleProposition + i);
		return Constants.UNKNOWN;
	}

	/**
	 * @param c
	 * @return true if the char represents a valid literal identifier
	 */
	static public final boolean check(final char c) {
		boolean ok = false;
		for (int i = 0; i < Constants.PROPOSITIONS_BLOCKS; i++) {
			ok = c >= Constants.FIRST_PROPOSITION[i] && c <= Constants.LAST_PROPOSITION[i];
			if (ok)
				return true;
		}
		return false;
	}

	/**
	 * Simple constructor of a positive literal.
	 *
	 * @param v a char.
	 * @return the straight literal of proposition v
	 */
	public static Literal create(final char v) {
		return create(v, State.straight);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 * @return a literal with name v and state state.
	 */
	public static Literal create(final char v, State state) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not a letter!");
		if (state == null || state == State.absent)
			throw new IllegalArgumentException("The state is not valid!");
		int hc = -1;
		Literal l = literalCreated[hc = hashCode(v, state)];
		if (l == null) {
			l = new Literal(v, state);
			literalCreated[hc] = l;
		}
		return l;
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 * @return a copy of literal v but with state state.
	 */
	public static Literal create(final Literal v, State state) {
		if (state == null || state == State.absent)
			throw new IllegalArgumentException("The state is not valid!");
		int hc = -1;
		Literal l = literalCreated[hc = hashCode(v.name, state)];
		if (l == null) {
			l = new Literal(v, state);
			literalCreated[hc] = l;
		}
		return l;
	}

	/**
	 * Hash code for a literal given as char and state.
	 * 
	 * @param c
	 * @param cState
	 * @return an integer that is surely unique when 'a'<=c<='z'.
	 */
	public static final int hashCode(char c, State cState) {
		// byte i = index(c);
		// int j = cState.ordinal();
		// int n = i*3;
		// n+=j-1;
		// return n;
		return index(c) * 3 + (cState.ordinal() - 1);
	}

	/**
	 * @param c
	 * @return the index of a proposition c w.r.t. the base index 0 associated to the proposition.
	 */
	static public final byte index(final char c) {
		if (!check(c))
			throw new IllegalArgumentException("Proposition '" + c + "' is not valid.");

		for (int j = 0; j < Constants.PROPOSITIONS_BLOCKS; j++) {
			if (c <= Constants.LAST_PROPOSITION[j]) {
				return (byte) (propositionBlockUpperBound[j] - (Constants.LAST_PROPOSITION[j] - c));
			}
		}
		return (byte) (-1);
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
			final Literal l = Literal.create(s.charAt(0), State.straight);
			return l;
		}
		if (s.charAt(0) == Constants.NOT) {
			if (!Literal.check(s.charAt(1)))
				return null;
			final Literal l = Literal.create(s.charAt(1), State.negated);
			return l;
		}
		return null;
	}

	/**
	 * @param proposition
	 * @param state
	 * @return the string representation of a literal identified by proposition and state parameter.
	 */
	static final String toString(char proposition, State state) {
		return state.toString() + proposition;
	}

	/**
	 * @param propositionIndex
	 * @param state
	 * @return the string representation of a literal identified by its index and state parameter.
	 */
	static final String toString(int propositionIndex, State state) {
		return state.toString() + Literal.charValue(propositionIndex);
	}

	/**
	 * Propositional letter. It cannot be changed!
	 */
	private char name;

	/**
	 * It is true if the literal is the negation of the variable.
	 */
	private State state;

	/**
	 * Simple constructor of a positive literal.
	 *
	 * @param v a char.
	 */
	private Literal(final char v) {
		this(v, State.straight);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 */
	private Literal(final char v, final State state) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not a letter!");
		this.name = v;
		this.state = state;
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v the proposition letter.
	 * @param state one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 */
	private Literal(final Literal v, final State state) {
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
		return this.state.compareTo(o.state);
	}

	/** {@inheritDoc} */
	@Override
	public final boolean equals(final Object o) {
		if ((o == null) || !(o instanceof Literal))
			return false;
		final Literal l = (Literal) o;
		return (this.name == l.name) && (this.state == l.state);
	}

	/**
	 * The complement of a straight literal is the negated one.<br>
	 * The complement of a negated literal is the straight one.<br>
	 * The complement of a unknown literal is null (an empty literal is not possible).<br>
	 *
	 * @return a new literal that it is the negate of this. null if it is request the complement of unknown literal.
	 */
	public final Literal getComplement() {
		if (this.state == State.unknown)
			return null;
		return create(this, (this.isNegated()) ? State.straight : State.negated);
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
		return create(this, State.negated);
	}

	/**
	 * Getter for the field <code>state</code>.
	 *
	 * @return the state
	 */
	public final State getState() {
		return this.state;
	}

	/**
	 * getStraight.
	 *
	 * @return a new literal with the same name and state straight.
	 */
	public final Literal getStraight() {
		return create(this, State.straight);
	}

	/**
	 * getUnknown.
	 *
	 * @return a new literal with the same name and state unknown.
	 */
	public final Literal getUnknown() {
		return create(this, State.unknown);
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		return Literal.hashCode(this.name, this.state);
	}

	/**
	 * @param l
	 * @return true if it is a complement literal of the given one
	 */
	public final boolean isComplement(Literal l) {
		if (l == null)
			return false;
		return this.state.isComplement(l.state);
	}

	/**
	 * isNegated.
	 *
	 * @return true if it is a negated literal.
	 */
	public final boolean isNegated() {
		return this.state == State.negated;
	}

	/**
	 * isStraight.
	 *
	 * @return true if it is a straight literal.
	 */
	public final boolean isStraight() {
		return this.state == State.straight;
	}

	/**
	 * isUnknown.
	 *
	 * @return true if it is a literal in the unknown state.
	 */
	public final boolean isUnknown() {
		return this.state == State.unknown;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return this.state.toString() + this.name;
	}

	/**
	 * Proposes only some execution time estimates about some class methods.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		// final int nTest = 1000;
		// final double msNorm = 1.0 / (1000000.0 * nTest);

		final Literal A = Literal.create('A'), Z = Literal.create('Z'), B = Literal.create('B', State.straight), b = Literal.create('b'),
				a = Literal.create('a'), z = Literal.create('z'),
				α = Literal.create('α'), μ = Literal.create('μ');

		Label empty = Label.emptyLabel;
		System.out.println("Empty: " + empty);
		System.out.println("A: " + A);
		System.out.println("Z: " + Z);
		System.out.println("a: " + a);
		System.out.println("z: " + z);
		System.out.println("α: " + α);
		System.out.println("μ; " + μ);

		System.out.println("Hashcode A: " + A.hashCode());
		System.out.println("Hashcode B: " + B.hashCode());
		System.out.println("Hashcode Z: " + Z.hashCode());
		System.out.println("Hashcode a: " + a.hashCode());
		System.out.println("Hashcode b: " + b.hashCode());
		System.out.println("Hashcode z: " + z.hashCode());
		System.out.println("Hashcode α: " + α.hashCode());
		System.out.println("Hashcode μ; " + μ.hashCode());

		System.out.println("Index of 'A': " + Literal.index('A'));
		System.out.println("Index of 'B': " + Literal.index('B'));
		System.out.println("Index of 'Z': " + Literal.index('Z'));
		System.out.println("Index of 'a': " + Literal.index('a'));
		System.out.println("Index of 'b': " + Literal.index('b'));
		System.out.println("Index of 'z': " + Literal.index('z'));
		System.out.println("Index of 'α': " + Literal.index('α'));
		System.out.println("Index of 'μ': " + Literal.index('μ'));

		System.out.println("CharValue of 0: " + Literal.charValue(0));
		System.out.println("CharValue of 1: " + Literal.charValue(1));
		System.out.println("CharValue of 25: " + Literal.charValue(25));
		System.out.println("CharValue of 26: " + Literal.charValue(26));
		System.out.println("CharValue of 27: " + Literal.charValue(27));
		System.out.println("CharValue of 51: " + Literal.charValue(51));
		System.out.println("CharValue of 52: " + Literal.charValue(52));
		System.out.println("CharValue of 63: " + Literal.charValue(63));
	}

}
