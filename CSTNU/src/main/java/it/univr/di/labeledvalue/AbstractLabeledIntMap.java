package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Abstract class for {@link it.univr.di.labeledvalue.LabeledIntMap} interface.
 *
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
public abstract class AbstractLabeledIntMap implements LabeledIntMap, Serializable {

	/**
	 * Admissible chars in a label and associated value.
	 */
	static final String valueRE = "[ ,0-9∞" + Pattern.quote("-") + "]+";
	@SuppressWarnings("javadoc")
	static final String labeledValueRE = "(" + Constants.LABEL_RE + valueRE + "|" + valueRE + Constants.LABEL_RE + ")";

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(AbstractLabeledIntMap.class.getName());

	/**
	 * Matcher for labeled values.
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
	 * A natural comparator for Entry&lt;Label&gt;.
	 * It orders considering the alphabetical order of Label.
	 */
	static Comparator<Entry<Label>> entryComparator = new Comparator<Entry<Label>>() {
		// I wanted a sorted print!
		@Override
		public int compare(final Entry<Label> o1, final Entry<Label> o2) {
			if (o1 == o2)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			return o1.getKey().compareTo(o2.getKey());
		}
	};

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
			AbstractLabeledIntMap.LOG.warning("Input string is not well formed for representing a set of labeled values: " + patternLabelCharsRE);
			return null;
		}

		LabeledIntMapFactory<LabeledIntTreeMap> factory = new LabeledIntMapFactory<>(LabeledIntTreeMap.class);
		final LabeledIntMap newMap = factory.create();

		final String[] entryPair = AbstractLabeledIntMap.splitterEntry.split(inputMap);
		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
		Label l;
		int value;
		for (final String s : entryPair) {
			AbstractLabeledIntMap.LOG.finest("s: " + s);
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
	 * Determines the sum of 'a' and 'b'. If any of them is already INFINITY, returns INFINITY. If the sum is greater/lesser than the maximum/minimum integer
	 * representable by a int, it throw an IllegalStateException because the overflow. I don't use Overflow exception because it requires to use a try{} catch
	 * section.
	 *
	 * @param a an integer.
	 * @param b an integer.
	 * @return the controlled sum.
	 * @throws java.lang.ArithmeticException
	 *             if any.
	 */
	static public final int sumWithOverflowCheck(final int a, final int b) throws ArithmeticException {
		if ((a == Constants.INT_NEG_INFINITE) || (b == Constants.INT_NEG_INFINITE))
			return Constants.INT_NEG_INFINITE;
		if ((a == Constants.INT_POS_INFINITE) || (b == Constants.INT_POS_INFINITE))
			return Constants.INT_POS_INFINITE;

		final long sum = (long) a + (long) b;// CAST IS NECESSARY!
		if ((sum >= Constants.INT_POS_INFINITE) || (sum <= Constants.INT_NEG_INFINITE))
			throw new ArithmeticException("Integer overflow in a sum of labeled values: " + a + " + " + b);
		return (int) sum;
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof LabeledIntMap))
			return false;
		final LabeledIntMap lvm = ((LabeledIntMap) o);
		if (this.size() != lvm.size())
			return false;
		return this.entrySet().equals(lvm.entrySet());// Two maps are equals if they contain the same set of values.
		// The internal representation is not important!.
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValue() {
		int min = Constants.INT_POS_INFINITE;
		for (int value : this.values()) {
			if (min > value)
				min = value;
		}
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueAmongLabelsWOUnknown() {
		int v = Constants.INT_POS_INFINITE, i;
		Label l;
		for (final Entry<Label> entry : this.entrySet()) {
			l = entry.getKey();
			if (l.containsUnknown()) {
				continue;
			}
			i = entry.getIntValue();
			if (v > i) {
				v = i;
			}
		}
		return (v == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : v;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		int min = this.get(l);
		if (min == Constants.INT_NULL) {
			// the label does not exits, try all consistent labels
			min = Constants.INT_POS_INFINITE;
			int v1;
			Label l1 = null;
			for (final Entry<Label> e : this.entrySet()) {
				l1 = e.getKey();
				if (l.isConsistentWith(l1)) {
					v1 = e.getIntValue();
					if (min > v1) {
						min = v1;
					}
				}
			}
		}
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(final LabeledIntMap inputMap) {
		if (inputMap == null)
			return;
		for (final Entry<Label> entry : inputMap.entrySet()) {
			this.put(entry.getKey(), entry.getIntValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("{");
		final ObjectList<Entry<Label>> sorted = new ObjectArrayList<>(this.entrySet());
		sorted.sort(AbstractLabeledIntMap.entryComparator);
		for (final Entry<Label> entry : sorted) {
			sb.append(AbstractLabeledIntMap.entryAsString(entry) + " ");
		}
		sb.append("}");
		return sb.toString();
	}

}
