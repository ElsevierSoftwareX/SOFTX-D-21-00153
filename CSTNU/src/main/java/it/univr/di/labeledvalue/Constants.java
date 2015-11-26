package it.univr.di.labeledvalue;

import java.io.Serializable;

/**
 * Some useful constants for the package.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public final class Constants implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Char representing logic not symbol: ¬.
	 */
	public static final char NOT = '\u00AC';
	/**
	 * @see #NOT
	 */
	public static final String NOTstring = String.valueOf(Constants.NOT);

	/**
	 * Char representing logic not know symbol: ¿.
	 */
	public static final char UNKNOWN = '\u00BF';
	/**
	 * @see #UNKNOWN
	 */
	public static final String UNKNOWNstring = String.valueOf(Constants.UNKNOWN);

	/**
	 * Char representing empty label: ⊡.
	 */
	public static final char EMPTY_LABEL = '\u22A1';
	/**
	 * @see #EMPTY_LABEL
	 */
	public static final String EMPTY_LABELstring = String.valueOf(Constants.EMPTY_LABEL);

	/**
	 * Char representing empty upper case label: ◇
	 */
	public static final char EMPTY_UPPER_CASE_LABEL = '\u25C7';
	/**
	 * @see Constants#EMPTY_UPPER_CASE_LABEL
	 */
	public static final String EMPTY_UPPER_CASE_LABELstring = String.valueOf(Constants.EMPTY_UPPER_CASE_LABEL);

	/**
	 * Char representing infinity symbol: ∞.
	 */
	public static final char INFINITY_SYMBOL = '\u221E';
	/**
	 * @see #INFINITY_SYMBOL
	 */
	public static final String INFINITY_SYMBOLstring = String.valueOf(Constants.INFINITY_SYMBOL);

	/**
	 * Default value to represent a no valid int value. It is necessary in the type oriented implementation of Map(Label,int). It has to be different to the
	 * value {@link Constants#INT_POS_INFINITE}, used to represent an edge with a no bound labeled value.
	 */
	static public final int INT_NULL = Integer.MIN_VALUE;

	/**
	 * THe integer value representing the +∞.
	 */
	public static final int INT_POS_INFINITE = Integer.MAX_VALUE;

	/**
	 * THe integer value representing the -∞.
	 */
	public static final int INT_NEG_INFINITE = INT_NULL + 1;

	/**
	 * Set of chars allowed for proposition name in RE reange format.
	 */
	public static final String extraCharRanges = "\u00C0-\u00FF";
	/**
	 * Set of char ranges allowed for a proposition name in RE range format.
	 */
	public static final String propositionLetterRanges = "A-Za-z"+extraCharRanges;

	/**
	 * Regular expression representing a Label.
	 */
	public static final String labelRE = "(([" + Constants.NOTstring + "|" + Constants.UNKNOWN + "]?[" + propositionLetterRanges + "])*|([" + propositionLetterRanges + "])*)+|"
			+ Constants.EMPTY_LABELstring;

	/**
	 * Regular expression for an acceptable value in a LabeledValue
	 */
	public static final String labeledValueRE = "[-][0-9]+|[0-9]*";

	/**
	 * Regular expression for an acceptable positive integer
	 */
	public static final String nonNegIntValueRE = "[0-9]+";

	/**
	 * Regular expression for positive and not 0 integer 
	 */
	public static final String strictlyPositiveIntValueRE = "[1-9]+";

	/**
	 * <p>
	 * formatInt.
	 * </p>
	 *
	 * @param n
	 *            a int.
	 * @return the value of n as String using ∞ for infinitive number and null for not valid int.
	 */
	static public String formatInt(int n) {
		if (n == Constants.INT_NEG_INFINITE)
			return "-" + Constants.INFINITY_SYMBOLstring;
		if (n == Constants.INT_POS_INFINITE)
			return Constants.INFINITY_SYMBOLstring;
		if (n == Constants.INT_NULL)
			return "null";
		return String.valueOf(n);
	}

}
