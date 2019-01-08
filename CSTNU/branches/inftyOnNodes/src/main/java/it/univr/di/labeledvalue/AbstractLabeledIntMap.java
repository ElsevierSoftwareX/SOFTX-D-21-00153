package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.Debug;

/**
 * Abstract class for {@link it.univr.di.labeledvalue.LabeledIntMap} interface.
 *
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
public abstract class AbstractLabeledIntMap implements LabeledIntMap, Serializable {

	/**
	 * Admissible values as regular expression.
	 */
	static final String valueRE = "[ ,0-9∞" + Pattern.quote("-") + "]+";
	/**
	 * format of a labeled value as regular expression.
	 */
	static final String labeledValueRE = "(" + Label.LABEL_RE + valueRE + "|" + valueRE + Label.LABEL_RE + ")";

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(AbstractLabeledIntMap.class.getName());

	/**
	 * Matcher for a set of labeled values.
	 */
	static final Pattern patternLabelCharsRE = Pattern
			.compile(Pattern.quote("{")
					+ "("
					+ Pattern.quote(Constants.OPEN_PAIR) + labeledValueRE + Pattern.quote(Constants.CLOSE_PAIR)
					+ "[ ]*)*"
					+ Pattern.quote("}"));

	/**
	 *
	 */
	static final long serialVersionUID = 1L;

	/**
	 * RE for splitting a list of labeled values.
	 */
	static final Pattern splitterEntry = Pattern
			.compile(Pattern.quote("{") + Pattern.quote("}") + "|[{" + Pattern.quote(Constants.OPEN_PAIR) + "]+|" + Pattern.quote(Constants.CLOSE_PAIR) + " ["
					+ Constants.OPEN_PAIR + "} ]*");

	/**
	 *
	 */
	static final Pattern splitterPair = Pattern.compile(", ");

	/**
	 * @param entry (label, value)
	 * @return string representing the labeled value, i.e., "(value, label)"
	 */
	static final String entryAsString(final Entry<Label> entry) {
		if (entry == null)
			return "";
		return entryAsString(entry.getKey(), entry.getIntValue());
	}

	/**
	 * @param value
	 * @param label
	 * @return string representing the labeled value, i.e., "(value, label)"
	 */
	static final public String entryAsString(Label label, int value) {
		if (label == null)
			return "";
		final StringBuilder sb = new StringBuilder();
		sb.append(Constants.OPEN_PAIR);
		sb.append(Constants.formatInt(value));
		sb.append(", ");
		sb.append(label.toString());
		sb.append(Constants.CLOSE_PAIR);
		return sb.toString();
	}

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link #toString()}: {\[(&lt;value&gt;, &lt;key&gt;) \]*}
	 * This method is also capable to parse the old format: {\[(&lt;key&gt;, &lt;value&gt;) \]*}
	 *
	 * @param inputMap a {@link java.lang.String} object.
	 * @return a LabeledValueTreeMap object if <code>inputMap</code> represents a valid map, null otherwise.
	 */
	static public LabeledIntMap parse(final String inputMap) {
		if (inputMap == null)
			return null;

		if (!AbstractLabeledIntMap.patternLabelCharsRE.matcher(inputMap).matches()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					AbstractLabeledIntMap.LOG.warning("Input string is not well formed for representing a set of labeled values: " + patternLabelCharsRE);
				}
			}
			return null;
		}

		LabeledIntMapFactory<LabeledIntTreeMap> factory = new LabeledIntMapFactory<>(LabeledIntTreeMap.class);
		final LabeledIntMap newMap = factory.get();

		final String[] entryPair = AbstractLabeledIntMap.splitterEntry.split(inputMap);
		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
		Label l;
		int value;
		for (final String s : entryPair) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					AbstractLabeledIntMap.LOG.finest("s: " + s);
				}
			}
			if (s.length() != 0) {
				final String[] labInt = AbstractLabeledIntMap.splitterPair.split(s);
				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
				l = Label.parse(labInt[0]);
				// Manage old and new format!
				if (l == null) {
					if (labInt[0].equals("-" + Constants.INFINITY_SYMBOL))
						value = Constants.INT_NEG_INFINITE;
					else
						value = Integer.parseInt(labInt[0]);
					l = Label.parse(labInt[1]);
				} else {
					if (labInt[1].equals("-" + Constants.INFINITY_SYMBOL))
						value = Constants.INT_NEG_INFINITE;
					else
						value = Integer.parseInt(labInt[1]);
				}
				newMap.put(l, value);
			}
		}
		return newMap;
	}

	/**
	 * @return true if they contain the same set of values.
	 */
	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LabeledIntMap))
			return false;
		LabeledIntMap lvm = (LabeledIntMap) o;
		if (this.size() != lvm.size())
			return false;
		return this.entrySet().equals(lvm.entrySet());// The internal representation is not important!.
	}

	@Override
	public int hashCode() {
		return this.entrySet().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("{");
		final ObjectList<Entry<Label>> sorted = new ObjectArrayList<>(this.entrySet());
		sorted.sort(LabeledIntMap.entryComparator);
		for (final Entry<Label> entry : sorted) {
			sb.append(AbstractLabeledIntMap.entryAsString(entry) + " ");
		}
		sb.append("}");
		return sb.toString();
	}

}
