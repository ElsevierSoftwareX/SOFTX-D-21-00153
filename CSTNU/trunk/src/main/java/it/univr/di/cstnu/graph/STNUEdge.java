// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.regex.Pattern;

import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;

/**
 * Represents the behavior of a STNU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface STNUEdge extends STNEdge {

	public final String SEP_CASE = ":";
	public final String UC_LABEL = "UC";
	public final String LC_LABEL = "LC";

	/**
	 * A lower/upper-case label is represented as pair: node-name and the flag for saying if it is an upper (true) o a lower (false) case.
	 * 
	 * @author posenato
	 */
	public class Pair extends org.apache.commons.math3.util.Pair<ALetter, Boolean> {
		/**
		 * A lower/upper-case label. k must be not null.
		 * 
		 * @param k
		 * @param v
		 */
		public Pair(ALetter k, boolean v) {
			super(k, v);
			if (k == null) {
				throw new IllegalArgumentException("Node name cannot be null");
			}
		}

		public Pair(Pair pair) {
			this(pair.getFirst(), pair.getSecond());
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append((this.isUpper()) ? UC_LABEL : LC_LABEL);
			sb.append(Constants.OPEN_PAIR);
			sb.append(this.getFirst().toString());
			sb.append(Constants.CLOSE_PAIR);
			return sb.toString();
		}

		boolean isUpper() {
			return this.getSecond();
		}

		boolean isLower() {
			return !this.getSecond();
		}
	}

	/**
	 * @return the labeled weight if it is a contingent edge, {@link Constants#INT_NULL} otherwise.
	 */
	public int getLabeledValue();

	/**
	 * @return the node label associated to this edge when it is a contingent one and its nature (true for upper-case, false for lower-case), null otherwise.
	 */
	public Pair getNodeLabel();

	/**
	 * @return true if the edge contains an ordinary value; false otherwise
	 */
	public default boolean isOrdinaryEdge() {
		return this.getValue() != Constants.INT_NULL;
	}

	/**
	 * @return true if the edge is a lower-case one; false otherwise
	 */
	public default boolean isLowerCase() {
		final Pair p = this.getNodeLabel();
		if (p == null || p.isUpper())
			return false;
		return true;
	}

	/**
	 * @return true if the edge is a lower-case one; false otherwise
	 */
	public default boolean isUpperCase() {
		final Pair p = this.getNodeLabel();
		if (p == null || p.isLower())
			return false;
		return true;
	}

	/**
	 * Representation of the labeled value as a string.
	 * It is important to have a reference method because in many parts the string representation is used.
	 * 
	 * @return a string representing the labeled value. If there is no labeled value, returns an empty string.
	 */
	public default String getLabeledValueFormatted() {
		if (this.getNodeLabel() == null) {
			return "";
		}
		return this.getNodeLabel().toString() + SEP_CASE + Constants.formatInt(this.getLabeledValue());
	}

	/**
	 * Sets the labeled weight to w. It must set the type of this edge to {@link it.univr.di.cstnu.graph.Edge.ConstraintType#contingent}.
	 * If nodeLabel == null or w = {@link Constants#INT_NULL}, the possible labeled value is removed.
	 * 
	 * @param nodeLabel the name of the contingent node as ALetter.
	 * @param w the new weight value
	 * @param upperCase true if the edge is a upper-case edge, false it it is a lower-case edge. In case of lower-case edge, w must be positive.
	 * @return the old weight associated to the edge.
	 *         If the weight was not set, it returns {@link Constants#INT_NULL}.
	 */
	public int setLabeledValue(ALetter nodeLabel, int w, boolean upperCase);

	/**
	 * Parse a upper/case labeled value for determining the new value for this.
	 * This method must be aligned with {@link #getLabeledValueFormatted()}.
	 * 
	 * @param labeledValueAsString
	 * @return true if the labeled value was set, false otherwise.
	 */
	public default boolean setLabeledValue(String labeledValueAsString) {
		// System.err.print("\\b(" + UC_LABEL + "|" + LC_LABEL + ")\\b" + Pattern.quote(Constants.OPEN_PAIR) + ".*"
		// + Pattern.quote(Constants.CLOSE_PAIR) + SEP_CASE + ".*");
		if (labeledValueAsString == null || labeledValueAsString.isEmpty()
				|| !labeledValueAsString.matches("\\b(" + UC_LABEL + "|" + LC_LABEL + ")\\b" + Pattern.quote(Constants.OPEN_PAIR) + ".*"
						+ Pattern.quote(Constants.CLOSE_PAIR) + SEP_CASE + ".*"))
			return false;
		final String[] entryPair = labeledValueAsString.split(SEP_CASE);
		// System.err.println(Arrays.toString(entryPair));
		boolean upperCase = true;
		if (entryPair[0].startsWith(LC_LABEL)) {
			upperCase = false;
			entryPair[0] = entryPair[0].substring(LC_LABEL.length() + Constants.OPEN_PAIR.length());
		} else if (!entryPair[0].startsWith(UC_LABEL)) {
			return false;
		} else {
			entryPair[0] = entryPair[0].substring(LC_LABEL.length() + Constants.OPEN_PAIR.length());
		}

		// System.err.println(Arrays.toString(entryPair));
		ALetter nodeLabel = new ALetter(entryPair[0].substring(0, entryPair[0].length() - Constants.CLOSE_PAIR.length()));

		this.setLabeledValue(nodeLabel, Integer.parseInt(entryPair[1]), upperCase);
		return true;
	}

}
