package it.univr.di.labeledvalue;

import java.io.Serializable;

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
public class Literal implements Comparable<Literal>, Serializable {

	/**
	 * A literal can be: negated or straight or unknown.<br>
	 * The meaning of 'unknown' is exemplified in the documentation of the class {@link Literal}.
	 * <p>
	 * Do not change the order of values! Some methods in the package it.univr.di.labeledvalue.* exploit such order.
	 */
	public static enum State {
		/**
		 * A negated literal is true if the truth value assigned to its proposition letter is false, false otherwise.
		 */
		negated(Constants.NOTstring), /**
						 * A straight literal is true if the truth value assigned to its proposition letter is true, false otherwise.
						 */
		straight(""), /**
				 * An unknown literal, as '¿p' for example, is true if the value of proposition letter 'p' is not assigned yet. False otherwise.
				 */
		unknown(Constants.UNKNOWNstring);

		/**
		 * String representation of the state
		 */
		String stringRep;

		/**
		 * Default constructor.
		 *
		 * @param s
		 *                the string representation of the state.
		 */
		State(final String s) {
			this.stringRep = s;
		}

		@Override
		public String toString() {
			return this.stringRep;
		}
	}

	/**
	 * Parse a string returning the literal represented.
	 *
	 * @param s
	 *                It can be a single char ([a..zA..Z]) or the character ¬ [\u00ac] followed by a char. If the argument is null or not valid, it returns
	 *                null.
	 * @return the literal represented by 's' if 's' is a valid representation of a literal, null otherwise.
	 */
	public static final Literal parse(final String s) {
		if (s == null)
			return null;
		if (s.length() == 1) {
			if (!Literal.check(s.charAt(0)))
				return null;
			final Literal l = new Literal(s.charAt(0), State.straight);
			return l;
		}
		if (s.charAt(0) == Constants.NOT) {
			if (!Literal.check(s.charAt(1)))
				return null;
			final Literal l = new Literal(s.charAt(1), State.negated);
			return l;
		}
		return null;
	}

	/**
	 * @param c
	 * @return true if the char represents a valid literal identifier
	 */
	private static final boolean check(final char c) {
		return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') || (c <= 'Z')));
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

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
	 * @param v
	 *                a char.
	 */
	public Literal(final char v) {
		this(v, State.straight);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v
	 *                the proposition letter.
	 * @param state
	 *                one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 */
	public Literal(final char v, final State state) {
		if (!Literal.check(v))
			throw new IllegalArgumentException("The char is not a letter!");
		this.name = v;
		this.state = state;
	}

	/**
	 * Simple constructor allowing to specify if the literal is negated or not.
	 *
	 * @param v
	 *                the proposition letter.
	 * @param state
	 *                one of possible state of a literal: {@link it.univr.di.labeledvalue.Literal.State}.
	 */
	public Literal(final Literal v, final State state) {
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
		return new Literal(this, (this.isNegated()) ? State.straight : State.negated);
	}

	/**
	 * <p>
	 * getUnknown.
	 * </p>
	 *
	 * @return a new literal with the same name and state unknown.
	 */
	public final Literal getUnknown() {
		return new Literal(this, State.unknown);
	}

	/**
	 * <p>
	 * getStraight.
	 * </p>
	 *
	 * @return a new literal with the same name and state straight.
	 */
	public final Literal getStraight() {
		return new Literal(this, State.straight);
	}

	/**
	 * <p>
	 * getNegated.
	 * </p>
	 *
	 * @return a new literal with the same name and state negated.
	 */
	public final Literal getNegated() {
		return new Literal(this, State.negated);
	}

	/**
	 * <p>
	 * Getter for the field <code>name</code>.
	 * </p>
	 *
	 * @return the propositional letter associated to this literal.
	 */
	public final char getName() {
		return this.name;
	}

	/**
	 * <p>
	 * Getter for the field <code>state</code>.
	 * </p>
	 *
	 * @return the state
	 */
	public final State getState() {
		return this.state;
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		return this.name * (this.state.ordinal() * 256);
	}

	/**
	 * <p>
	 * isNegated.
	 * </p>
	 *
	 * @return true if it is a negated literal.
	 */
	public final boolean isNegated() {
		return this.state == State.negated;
	}

	/**
	 * <p>
	 * isStraight.
	 * </p>
	 *
	 * @return true if it is a straight literal.
	 */
	public final boolean isStraight() {
		return this.state == State.straight;
	}

	/**
	 * <p>
	 * isUnknown.
	 * </p>
	 *
	 * @return true if it is a literal in the unknown state.
	 */
	public final boolean isUnknown() {
		return this.state == State.unknown;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return (new StringBuffer()).append(this.state.stringRep).append(this.name).toString();
	}
}
