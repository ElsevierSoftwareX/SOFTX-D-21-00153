/**
 * Simple class to represent literal.
 */
package it.univr.di.cstnu;

import java.io.Serializable;

/**
 * @author posenato
 */
public class Literal implements Comparable<Literal>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param c
	 * @return true if the char represents a valid literal identifier
	 */
	private static final boolean check(char c) {
		return (((c >= 'a') && (c <= 'z')) || ((c >= 'A') || (c <= 'Z')));
	}

	/**
	 * Parse a string returning the literal represented.
	 * 
	 * @param s It can be a single char or the character ¬ [\u00ac] followed by a char. If it is null, it throw an NullParameter exception
	 * @return the literal represented by s.
	 */
	public static Literal parse(String s) {
		if (s == null) throw new NullPointerException("The parameter cannot be null!");
		if (s.length() == 1) {
			if (!Literal.check(s.charAt(0)))
				throw new IllegalArgumentException("The string does not contain a letter!");
			final Literal l = new Literal(s.charAt(0), false);
			return l;
		}
		if (s.charAt(0) == Constants.NOT) {
			if (!Literal.check(s.charAt(1)))
				throw new IllegalArgumentException("The string does not contain a letter!");
			final Literal l = new Literal(s.charAt(1), true);
			return l;
		}
		throw new IllegalArgumentException("The string contains more than 1 char and the first is not the negation!");
	}

	/**
	 * Boolean variable name
	 */
	private char name;

	/**
	 * It is true if the literal is the negation of the variable.
	 */
	private boolean negative;

	/**
	 * 
	 */
	@SuppressWarnings({ "unused" })
	private Literal() {
	}

	/**
	 * Simple constructor of a positive literal.
	 * 
	 * @param v
	 */
	public Literal(char v) {
		this(v, false);
	}

	/**
	 * Simple constructor allowing to specify if the literal is negate or not
	 * 
	 * @param v
	 * @param negative
	 */
	public Literal(char v, boolean negative) {
		if (!Literal.check(v)) throw new IllegalArgumentException("The char is not a letter!");
		name = v;
		this.negative = negative;
	}

	/**
	 * Simple constructor of a copy of a literal.
	 * 
	 * @param l
	 */
	public Literal(Literal l) {
		this(l.name, l.negative);
	}

	/**
	 * Simple constructor.
	 * 
	 * @param s It can be a single char or the character ¬ [\u00ac] followed by a char. If it is null, it throw an NullParameter exception
	 */
	public Literal(String s) {
		final Literal l = Literal.parse(s);
		name = l.name;
		negative = l.negative;
	}

	/**
	 * It has to be used only to have the lexical order.
	 */
	@Override
	public int compareTo(Literal o) {
		if (name < o.name)
			return -1;
		else if (name > o.name) return 1;
		// Since compareTo has to be consistent with equals, when the two names are equal, we return
		// a different value than 0 if the literal are one the negate of the other.
		if (negative == o.negative) return 0;
		if (negative) return -1;
		return 1;
	}

	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof Literal)) return false;
		final Literal l1 = (Literal) o;
		return (name == l1.name) && (negative == l1.negative);
	}

	/**
	 * @return the name without possible negation.
	 */
	public char getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name * (negative ? 1 : 256);
	}

	/**
	 * @return true if it is a negative literal.
	 */
	public boolean isNegative() {
		return negative;
	}

	/**
	 * @return a new literal that it is the negate of this.
	 */
	public Literal negation() {
		return new Literal(name, !negative);
	}

	/**
	 * @param name the name to set
	 */
	public void setName(char name) {
		this.name = name;
	}

	/**
	 * @param negative the negate to set
	 */
	public void setNegative(boolean negative) {
		this.negative = negative;
	}

	@Override
	public String toString() {
		return (negative ? Constants.NOT : "") + String.valueOf(name);
	}

}
