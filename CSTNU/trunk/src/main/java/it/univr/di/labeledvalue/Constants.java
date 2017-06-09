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
	 * A-Letter
	 */
	public static final String ALETTER = "A-Za-z0-9α-μ_ ωΩ?";

	/**
	 * A-Letter range
	 */
	public static final String ALETTER_RANGE = "["+ALETTER+"]+";

	/**
	 * ALabel separator '∙'.
	 */
	public static final String ALABEL_SEPARATOR = "∙";

	/**
	 * Regular expression representing an A-Label.
	 * The re checks only that label chars are allowed.
	 */
	public static final String ALABEL_RE = "["+ALETTER + ALABEL_SEPARATOR + "]+|" + Constants.EMPTY_UPPER_CASE_LABELstring;

	/**
	 * Char representing labeled-value closing ")"
	 */
	public static final String CLOSE_PAIR = ")";// '⟩';

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
	 * First proposition
	 */
	public static final char[] FIRST_PROPOSITION = { 'A', 'a', 'α' };
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
	 * THe integer value representing the -∞.
	 */
	public static final int INT_NEG_INFINITE = INT_NULL + 1;

	/**
	 * THe integer value representing the +∞.
	 */
	public static final int INT_POS_INFINITE = Integer.MAX_VALUE;


	/**
	 * Regular expression for an acceptable value in a LabeledValue
	 */
	public static final String LabeledValueRE = "[-[0-9]|[0-9]]*";

	/**
	 * Last proposition
	 */
	public static final char[] LAST_PROPOSITION = { 'Z', 'z', 'μ' };

	/**
	 * Regular expression for an acceptable positive integer
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
	 * Maximal number of possible proposition in a network.
	 */
	public static final int NUMBER_OF_POSSIBLE_PROPOSITION = 64;// LastPossibleProposition - FirstPossibleProposition + 1;

	/**
	 * Char representing labeled-value opening
	 */
	public static final String OPEN_PAIR = "(";// '⟨';//It is not possible... to much saved file with (
	
	/**
	 * Set of char ranges allowed for a proposition name in RE range format.
	 * For memory and performance issue, we limit the number of possible propositions to be less or equal to 64.
	 */
	public static final String PROPOSITION_RANGES = FIRST_PROPOSITION[0] + "-" + LAST_PROPOSITION[0]
			+ FIRST_PROPOSITION[1] + "-" + LAST_PROPOSITION[1]
			+ FIRST_PROPOSITION[2] + "-" + LAST_PROPOSITION[2];
	/**
	 * 
	 */
	public static final int PROPOSITIONS_BLOCKS = 3;
	
	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Regular expression for positive and not 0 integer
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
	 * Regular expression representing a Label.
	 * The re checks only that label chars are allowed.
	 */
	public static final String LABEL_RE = "(("
			+ Constants.NOTstring + "[" + PROPOSITION_RANGES + "]|"
			+ Constants.UNKNOWN + "[" + PROPOSITION_RANGES + "]|"
			+ "[" + PROPOSITION_RANGES + "])+|"
			+ Constants.EMPTY_LABELstring+")";

	/**
	 * @param n a int.
	 * @return the value of n as String using ∞ for infinitive number and null for not valid int.
	 */
	static public String formatInt(int n) {
		switch (n) {
		case Constants.INT_NEG_INFINITE:
			return "-" + Constants.INFINITY_SYMBOLstring;
		case Constants.INT_POS_INFINITE:
			return Constants.INFINITY_SYMBOLstring;
		case Constants.INT_NULL:
			return "null";
		default:
			return String.valueOf(n);
		}
	}

}
