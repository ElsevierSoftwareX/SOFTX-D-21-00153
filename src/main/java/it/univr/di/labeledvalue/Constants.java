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
	 * Char representing labeled-value closing ")".
	 */
	public static final String CLOSE_PAIR = ")";// '⟩';

	/**
	 * A tupla closing char
	 */
	public static final String CLOSE_TUPLE = "❯";

	/**
	 * A tupla opening char
	 */
	public static final String OPEN_TUPLE = "❮";

	/**
	 * Char representing empty label: ⊡.
	 */
	public static final char EMPTY_LABEL = '\u22A1';
	/**
	 * @see #EMPTY_LABEL
	 */
	public static final String EMPTY_LABELstring = String.valueOf(Constants.EMPTY_LABEL);

	/**
	 * Char representing empty upper case label: ◇.
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
	 * Negative infinitive.
	 * 
	 * @see #INFINITY_SYMBOL
	 */
	public static final String NEGATIVE_INFINITY_SYMBOLstring = "-" + INFINITY_SYMBOLstring;

	/**
	 * Default value to represent a no valid integer value. It is necessary in the type oriented implementation of {@code Map(Label,int)}.
	 * It has to be different to the value {@link Constants#INT_POS_INFINITE}, used to represent an edge with a no bound labeled value.
	 */
	static public final int INT_NULL = Integer.MIN_VALUE;

	/**
	 * THe integer value representing the -∞.
	 */
	public static final int INT_NEG_INFINITE = INT_NULL + 1;

	/**
	 * THe integer value representing the +∞.
	 */
	public static final int INT_POS_INFINITE = Integer.MAX_VALUE;


	/**
	 * Regular expression for an acceptable value in a LabeledValue.
	 */
	public static final String LabeledValueRE = "[-[0-9]|[0-9]]*";


	/**
	 * Regular expression for an acceptable positive integer.
	 */
	public static final String NonNegIntValueRE = "[0-9]+";

	/**
	 * Char representing logic not symbol: ¬.
	 */
	public static final char NOT = '\u00AC';

	/**
	 * @see #NOT
	 */
	public static final String NOTstring = String.valueOf(Constants.NOT);

	/**
	 * String representing labeled-value opening.
	 */
	public static final String OPEN_PAIR = "(";// '⟨';//It is not possible... to much saved file with (

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Regular expression for positive and not 0 integer.
	 */
	public static final String StrictlyPositiveIntValueRE = "[1-9]+";

	/**
	 * Char representing logic not know symbol: ¿.
	 */
	public static final char UNKNOWN = '\u00BF';

	/**
	 * @see #UNKNOWN
	 */
	public static final String UNKNOWNstring = String.valueOf(Constants.UNKNOWN);

	/**
	 * @param n a integer
	 * @return the value of {@code n} as a String using {@link Constants#INFINITY_SYMBOL} for the infinitive and {@code NaN} for not valid integer
	 */
	static public final String formatInt(int n) {
		switch (n) {
		case Constants.INT_NEG_INFINITE:
			return NEGATIVE_INFINITY_SYMBOLstring;
		case Constants.INT_POS_INFINITE:
			return Constants.INFINITY_SYMBOLstring;
		case Constants.INT_NULL:
			return "NaN";
		default:
			return Integer.toString(n);
		}
	}

	/**
	 * Determines the sum of {@code a} and {@code b}. If any of them is already INFINITY, returns INFINITY.
	 *
	 * @param a an integer
	 * @param b an integer
	 * @return the controlled sum
	 * @throws java.lang.ArithmeticException if the sum of two inputs is greater/lesser than the maximum/minimum integer representable by a {@code int}
	 */
	static public final int sumWithOverflowCheck(final int a, final int b) throws ArithmeticException {
		int max, min;
		if (a >= b) {
			max = a;
			min = b;
		} else {
			min = a;
			max = b;
		}
		if (min == Constants.INT_NEG_INFINITE) {
			if (max == Constants.INT_POS_INFINITE)
				throw new ArithmeticException("Integer overflow in a sum of labeled values: " + Constants.formatInt(a) + " + " + Constants.formatInt(b));
			return Constants.INT_NEG_INFINITE;
		}
		if (max == Constants.INT_POS_INFINITE) {
			if (min == Constants.INT_NEG_INFINITE)
				throw new ArithmeticException("Integer overflow in a sum of labeled values: " + Constants.formatInt(a) + " + " + Constants.formatInt(b));
			return Constants.INT_POS_INFINITE;
		}

		final long sum = (long) a + (long) b;
		if ((sum >= Constants.INT_POS_INFINITE) || (sum <= Constants.INT_NEG_INFINITE))
			throw new ArithmeticException("Integer overflow in a sum of labeled values: " + Constants.formatInt(a) + " + " + Constants.formatInt(b));
		return (int) sum;
	}
}
