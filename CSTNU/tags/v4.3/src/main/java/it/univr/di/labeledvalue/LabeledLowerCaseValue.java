// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.univr.di.Debug;

/**
 * Represents an immutable Labeled Lower Case value.<br>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class LabeledLowerCaseValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Logger.
	 */
	private static final Logger LOG = Logger.getLogger("LabeledLowerCaseValue");

	/**
	 * A constant empty label to represent an empty label that cannot be modified.
	 */
	public static final LabeledLowerCaseValue emptyLabeledLowerCaseValue = new LabeledLowerCaseValue();

	/**
	 * Parses a string representing a labeled lower-case value and returns an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link #toString()}:<code>\{{(&lang;label&rang;, &lang;Alabel&rang;, &lang;value&rang;) }*\}</code><br>
	 * It also parse the old format: <code>\{{(&lang;Alabel&rang;, &lang;value&rang;, &lang;label&rang;) }*\}</code>
	 *
	 * @param arg a {@link java.lang.String} object.
	 * @param alphabet the alphabet to use for building a new labeled lower-case value. If null, a new alphabet is generated and insert into the created labeled
	 *            value.
	 * @return a LabeledLowerCaseValue object if arg represents a valid labeled value, null otherwise.
	 */
	public static LabeledLowerCaseValue parse(String arg, ALabelAlphabet alphabet) {
		// final Pattern splitterNode = Pattern.compile("〈|; ");
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Begin parse: " + arg);
			}
		}
		if ((arg == null) || (arg.length() < 2))
			return null;
		if (arg.equals("{}"))
			return emptyLabeledLowerCaseValue;

		if (!LabeledALabelIntTreeMap.patternlabelCharsRE.matcher(arg).matches())
			return null;

		arg = arg.replaceAll("[{}]", "");
		// arg = arg.substring(1, arg.length() - 2);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Before split: '" + arg + "'");
			}
		}
		final Pattern splitterEntry = Pattern.compile("\\)|\\(");
		final String[] entryThreesome = splitterEntry.split(arg);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("EntryThreesome: " + Arrays.toString(entryThreesome));
			}
		}
		final Pattern splitterTriple = Pattern.compile(", ");
		int j;
		String labelStr, aLabelStr, valueStr;
		// THERE IS ONLY ONE ENTRY
		if (alphabet == null) {
			alphabet = new ALabelAlphabet();
		}
		for (final String s : entryThreesome) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("s: '" + s + "'");
				}
			}
			if (s.length() > 1) {// s can be empty or a space.
				final String[] triple = splitterTriple.split(s);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.finest("triple: " + Arrays.toString(triple));
					}
				}
				Label l = Label.parse(triple[2]);
				if (l == null) {
					// probably it is the old format
					labelStr = triple[0];
					aLabelStr = triple[1];
					valueStr = triple[2];
				} else {
					// new format
					aLabelStr = triple[0];
					valueStr = triple[1];
					labelStr = triple[2];
				}
				if (l == null)
					l = Label.parse(labelStr);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.finest("Label: " + l);
					}
				}
				if (valueStr.equals("-" + Constants.INFINITY_SYMBOLstring))
					j = Constants.INT_NEG_INFINITE;
				else
					j = Integer.parseInt(valueStr);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.finest("Value: " + j);
					}
				} // LabeledNode is represented as " 〈<id>; {}; Obs: null〉 "
					// final String nodePart = labLitInt[1];//splitterNode.split(labLitInt[1]);
				final ALabel node = new ALabel(aLabelStr, alphabet);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.finest("LabeledNode: " + node);
					}
				}
				return new LabeledLowerCaseValue(node, j, l);
			}
		}
		return emptyLabeledLowerCaseValue;
	}

	/**
	 * Creates a lower-case value.
	 *
	 * @param nodeName not null node name
	 * @param value not null value
	 * @param label not null label
	 * @return a new LabeledLowerCaseValue object
	 */
	static public LabeledLowerCaseValue create(ALabel nodeName, int value, Label label) {
		if (nodeName == null || value == Constants.INT_NULL || label == null)
			return emptyLabeledLowerCaseValue;
		if (nodeName.size() > 1)
			throw new IllegalArgumentException("Node name label must contain only one name!");
		return new LabeledLowerCaseValue(nodeName, value, label);
	}

	/**
	 * Copy constructor.
	 * The new object is distinct from input.<br>
	 * No null check is done!
	 *
	 * @param input a {@link it.univr.di.labeledvalue.LabeledLowerCaseValue} object.
	 * @return a new LabeledLowerCaseValue object with equals fields of input
	 */
	static public LabeledLowerCaseValue create(LabeledLowerCaseValue input) {
		if (input == null || input.isEmpty())
			return emptyLabeledLowerCaseValue;
		return new LabeledLowerCaseValue(ALabel.clone(input.nodeName), input.value, input.label);
	}

	/**
	 * 
	 */
	private Label label;

	/**
	 * Even if this field could be just a ALetter, it is an ALabel because the comparison between ALabels is faster than ALetter and ALabel.
	 */
	private ALabel nodeName;

	/**
	 * 
	 */
	private int value;

	/**
	 * cached hash code
	 */
	private int hashCode;

	/**
	 * Creates an empty lower-case value.
	 * 
	 * @implSpec
	 * 			Externally, users have to use {@link #emptyLabeledLowerCaseValue} for having an empty object.
	 */
	private LabeledLowerCaseValue() {
		this.label = null;
		this.nodeName = null;
		this.value = Constants.INT_NULL;
	}

	/**
	 * @param nodeName1 a not null node name
	 * @param value1 a value different from {@link Constants#INT_NULL}
	 * @param label1 a non null label
	 */
	private LabeledLowerCaseValue(ALabel nodeName1, int value1, Label label1) {
		if (nodeName1 == null || value1 == Constants.INT_NULL || label1 == null)
			return;
		if (nodeName1.size() > 1)
			throw new IllegalArgumentException("Node name label must contain only one name!");
		this.label = label1;
		this.nodeName = nodeName1;
		this.value = value1;
	}

	/**
	 * <p>Getter for the field <code>label</code>.</p>
	 *
	 * @return the label
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * <p>Getter for the field <code>nodeName</code>.</p>
	 *
	 * @return the node name
	 */
	public ALabel getNodeName() {
		return this.nodeName;
	}

	/**
	 * <p>Getter for the field <code>value</code>.</p>
	 *
	 * @return the value
	 */
	public int getValue() {
		return this.value;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LabeledLowerCaseValue))
			return false;
		LabeledLowerCaseValue v = (LabeledLowerCaseValue) o;
		return this.value == v.value && this.label.equals(v.label) && this.nodeName.equals(v.nodeName);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		int result = this.hashCode;
		if (result == 0) {
			result = (isEmpty()) ? 0 : (this.value * 31 + this.label.hashCode()) * 31 + this.nodeName.hashCode();
			this.hashCode = result;
		}
		return result;
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @return true if the object is empty
	 * @implSpec it is empty when at least one of its fields is null
	 */
	public boolean isEmpty() {
		return (this.nodeName == null || this.value == Constants.INT_NULL || this.label == null);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * <p>entryAsString.</p>
	 *
	 * @param nodeN a {@link it.univr.di.labeledvalue.ALabel} object.
	 * @param v a int.
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param lower true if the node name has to be written lower case
	 * @return the string representation of this lower-case value.
	 */
	public static String entryAsString(ALabel nodeN, int v, Label l, boolean lower) {
		final StringBuffer s = new StringBuffer("{");// this is necessary for saving the value in a file in the old format
		s.append(Constants.OPEN_PAIR);
		s.append((lower) ? nodeN.toLowerCase() : nodeN);
		s.append(", ");
		s.append(Constants.formatInt(v));
		s.append(", ");
		s.append(l);
		s.append(Constants.CLOSE_PAIR);
		s.append(' ');
		s.append("}");
		return s.toString();
	}

	/**
	 * <p>toString.</p>
	 *
	 * @param lower true if the node name has to be written lower case
	 * @return the string representation of this lower-case value
	 */
	public String toString(boolean lower) {
		if (this.isEmpty())
			return "{}";
		return entryAsString(this.nodeName, this.value, this.label, lower);
	}
}
