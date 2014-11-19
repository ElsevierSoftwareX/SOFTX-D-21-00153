/**
 * Class to collect all the constants used in the package
 */
package it.univr.di.cstnu;

import java.io.Serializable;

/**
 * @author posenato
 */
public final class Constants implements Serializable {

	/**
	 * Default value for edge lower bounds.
	 */
	public static final int defaultLB = 1;

	/**
	 * Upper bound for the weight of an edge.
	 */
	public static final int defaultUB = Integer.MAX_VALUE;

	/**
	 * Name of the distance viewer
	 */
	public static final String distanceViewerName = "DistanceViewer";

	/**
	 * Char representing empty label: ⊡.
	 */
	public static final char EMPTY_LABEL = '\u22A1';

	/**
	 * Char representing empty upper case label: ◇
	 */
	public static final char EMPTY_UPPER_CASE_LABEL = '\u25C7';

	/**
	 * String representing empty upper case label: ◇
	 */
	public static final String EMPTY_UPPER_CASE_LABEL_STRING = "" + EMPTY_UPPER_CASE_LABEL;

	/**
	 * Char representing infinity symbol: ∞.
	 */
	public static final char INFINITY = '\u221E';

	/**
	 * Upper bound for the weight of an edge.
	 */
	public static final Integer INFINITY_VALUE = Integer.MAX_VALUE;

	/**
	 * Regular expression representing a LabeledValue
	 */
	public static final String labeledValueRE = "[-][0-9]+|[0-9]*";

	/**
	 * Regular expression representing an Lower or Upper case label
	 */
	public static final String upperLowerCaseLabel = "|[0-9a-z_]+|[0-9A-Z_]+";

	/**
	 * Regular expression representing an Upper case label
	 */
	public static final String upperCaseLabel = "[0-9A-Z_]+";

	/**
	 * Regular expression representing a Label.
	 */
	public static final String labelRE = "((" + Constants.NOT + "[A-Za-z])*|([A-Za-z])*)+|"+ Constants.EMPTY_LABEL;

	/**
	 * Char representing logic not symbol: ¬.
	 */
	public static final char NOT = '\u00AC';

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
