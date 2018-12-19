package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;

/**
 * Allows to manage upper-case values that are also associated to propositional labels.
 * Labeled (by {@link Label}) values are grouped by alphabetic labels {@link ALabel}.
 * Each labeled value group is represented as LabeledIntTreeMap.
 * Be careful!<br>
 * Since lower-case value are singular for each edge, it not convenient to represent it as TreeMap.
 * A specialized class has been developed to represent such values: {@link LabeledLowerCaseValue}.
 * <p>
 * At first time, I made some experiments for evaluating if it is better to use a Object2ObjectRBTreeMap or a ObjectArrayMap for representing the internal map.
 * The below table shows that for very small network, the two implementation are almost equivalent. So, ObjectArrayMap was chosen.
 * *
 * <table border="1">
 * <caption>Execution time (ms) for some operations w.r.t the core data structure of the class.</caption>
 * <tr>
 * <th>Operation</th>
 * <th>Using Object2ObjectRBTreeMap (ms)</th>
 * <th>Using ObjectArrayMap (ms)</th>
 * </tr>
 * <tr>
 * <td>Create 1st map</td>
 * <td>0.370336085</td>
 * <td>0.314329559</td>
 * </tr>
 * <tr>
 * <td>min value</td>
 * <td>0.017957532</td>
 * <td>0.014711536</td>
 * </tr>
 * <tr>
 * <td>Retrieve value</td>
 * <td>0.001397098</td>
 * <td>0.000600641</td>
 * </tr>
 * <tr>
 * <td>Simplification</td>
 * <td>~0.183388</td>
 * <td>~0.120013</td>
 * </tr>
 * </table>
 * <p>
 * In October 2017, I verified that even with medium size network, some edges can contain around 5000 labeled UC values.
 * So, I tested the two implementation again considering up to 10000 labeled UC values.
 * It resulted that up to 1000 values, the two implementation show still almost equivalent performance, BUT when the keys are 5000, using ObjectArrayMap
 * retrieve the keys requires more than ONE hour, while using Object2ObjectRBTreeMap it requires almost 96. ms!!!
 * Details using Object2ObjectRBTreeMap:
 * 
 * <pre>
 * Time to retrieve 50 elements using entrySet(): ---
 * Time to retrieve 50 elements using keySet(): 0.012000000000000004ms
 * 
 * Time to retrieve 100 elements using entrySet(): ---
 * Time to retrieve 100 elements using keySet(): 0.006000000000000003ms
 *
 * Time to retrieve 1000 elements using entrySet(): 0.045ms
 * Time to retrieve 1000 elements using keySet(): 0.034ms
 * The difference is 0.025000000000000015 ms. It is better to use: keySet() approach.
 * 
 * Time to retrieve 5000 elements using entrySet(): 0.9623700000000001ms
 * Time to retrieve 5000 elements using keySet(): 0.352ms
 * The difference is 0.6139400000000002 ms. It is better to use: keySet() approach.
 *
 * Time to retrieve 10000 elements using entrySet(): --
 * Time to retrieve 10000 elements using keySet(): 1.292ms
 * </pre>
 * 
 * Considering then, RB Tree instead of RB Tree:
 * 
 * <pre>
 * Time to retrieve 50 elements using keySet(): 0.012000000000000002ms
 * 
 * Time to retrieve 100 elements using keySet(): 0.007ms
 *
 * Time to retrieve 1000 elements using entrySet(): ---
 * Time to retrieve 1000 elements using keySet(): 0.038ms
 * The difference is 0.025000000000000015 ms. It is better to use: keySet() approach.
 * 
 * Time to retrieve 5000 elements using entrySet(): ---
 * Time to retrieve 5000 elements using keySet(): 0.388ms
 * The difference is 0.6139400000000002 ms. It is better to use: keySet() approach.
 *
 * Time to retrieve 10000 elements using entrySet(): --
 * Time to retrieve 10000 elements using keySet(): 1.314ms
 * </pre>
 * 
 * <pre>
 * <b>All code for testing is in LabeledALabelIntTreeMapTest class (not public available).</b>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class LabeledALabelIntTreeMap implements Serializable {

	/**
	 * Keyword \w because it is necessary to accept also node names!
	 */
	static final String labelCharsRE = ALabelAlphabet.ALETTER + ALabel.ALABEL_SEPARATORstring + ",\\- " + Constants.NOT + Constants.EMPTY_LABEL
			+ Constants.UNKNOWNstring + Literal.PROPOSITIONS + Constants.INFINITY_SYMBOL;

	/**
	 * logger
	 */
	private static final Logger LOG = Logger.getLogger(LabeledALabelIntTreeMap.class.getName());

	/**
	 * Matcher for RE
	 */
	static final Pattern patternlabelCharsRE = Pattern.compile("\\{[\\(" + labelCharsRE + "\\) ]*\\}");

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link #toString()}.
	 * For historical reasons, the method is capable to parse two different map
	 * format:<code>"{[(&lang;label&rang;, &lang;Alabel&rang;, &lang;value&rang;) ]*}</code>
	 * or <code>"{[(&lang;Alabel&rang;, &lang;value&rang;, &lang;label&rang;) ]*}"</code>, where [a]* is a meta constructor for saying zero o more 'a'.
	 * 
	 * @param arg a {@link java.lang.String} object.
	 * @param alphabet
	 * @return a LabeledPairMap object if args represents a valid map, null otherwise.
	 */
	public static LabeledALabelIntTreeMap parse(String arg, ALabelAlphabet alphabet) {
		// final Pattern splitterNode = Pattern.compile("〈|; ");
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LabeledALabelIntTreeMap.LOG.finest("Begin parse: " + arg);
			}
		}
		if ((arg == null) || (arg.length() < 3))
			return null;

		if (!patternlabelCharsRE.matcher(arg).matches())
			return null;
		final LabeledALabelIntTreeMap newMap = new LabeledALabelIntTreeMap();

		arg = arg.replaceAll("[{}]", "");
		// arg = arg.substring(1, arg.length() - 2);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LabeledALabelIntTreeMap.LOG.finest("Before split: '" + arg + "'");
			}
		}
		final Pattern splitterEntry = Pattern.compile("\\)|\\(");
		final String[] entryThreesome = splitterEntry.split(arg);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LabeledALabelIntTreeMap.LOG.finest("EntryThreesome: " + Arrays.toString(entryThreesome));
			}
		}

		final Pattern splitterTriple = Pattern.compile(", ");
		if (alphabet == null)
			alphabet = new ALabelAlphabet();
		int j;
		String labelStr, aLabelStr, valueStr;
		for (final String s : entryThreesome) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LabeledALabelIntTreeMap.LOG.finest("s: '" + s + "'");
				}
			}
			if (s.length() > 1) {// s can be empty or a space.
				final String[] triple = splitterTriple.split(s);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LabeledALabelIntTreeMap.LOG.finest("triple: " + Arrays.toString(triple));
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
						LabeledALabelIntTreeMap.LOG.finest("Label: " + l);
					}
				}
				if (valueStr.equals("-" + Constants.INFINITY_SYMBOLstring))
					j = Constants.INT_NEG_INFINITE;
				else
					j = Integer.parseInt(valueStr);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LabeledALabelIntTreeMap.LOG.finest("Value: " + j);
					}
				}
				// LabeledNode is represented as " 〈<id>; {}; Obs: null〉 "
				// final String nodePart = labLitInt[1];//splitterNode.split(labLitInt[1]);
				// System.out.println(aLabelStr);
				final ALabel node = ALabel.parse(aLabelStr, alphabet);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LabeledALabelIntTreeMap.LOG.finest("LabeledNode: " + node);
					}
				}

				newMap.mergeTriple(l, node, j, false);
			}
		}
		return newMap;
	}

	/**
	 * Data structure.
	 * <ol>
	 * <li>A Upper/Lower Case value is a pair (nodeName, value) where nodeName is a name of a node
	 * and can be written either in all UPPER case or in all lower case.
	 * Such kind of constraint has been introduced by Morris Muscettola 2005.
	 * <li>A labeled Upper/Lower Case value is a pair (nodeName, (plabel, value)), where plabel
	 * represents scenario where value holds.
	 * Such kind of constraint has been introduced by Hunsbergher, Combi, and Posenato in 2012.
	 * <li>Each plabel is a conjunction of literals, i.e., of type {@link Label}.</li>
	 * <li>Since there may be more pairs with the same 'nodeName', a labeled Upper/Lower Case value is as a map of (nodeName, LabeledIntMap). See
	 * {@link LabeledIntMap}.
	 * <li>In 2017-10, nodeName has been substituted by Alabel. ALabel represent the name of a node or a conjunction of node names. Such modification has been
	 * introduced because CSTNU DC checking algorithm requires such kind of values.
	 * <li>Since the introduction of ALabel, we suggest to use two @{link {@link LabeledALabelIntTreeMap}. One to represent the upper-case values.
	 * The other for lower-case ones.
	 * </ol>
	 */
	private final Object2ObjectRBTreeMap<ALabel, LabeledIntTreeMap> map;// ObjectArrayMap is not suitable when the map is greater than 1000 values!

	/**
	 * Simple constructor. The internal structure is built and empty.
	 */
	public LabeledALabelIntTreeMap() {
		this.map = new Object2ObjectRBTreeMap<>();
	}

	/**
	 * Constructor to clone the structure.
	 * All internal maps will be independent from lvm ones while elements of maps will be shared.
	 * The motivation is that usually a Label inside a map is managed as read-only.
	 *
	 * @param lvm the map to clone. If null, 'this' will be a empty map.
	 */
	public LabeledALabelIntTreeMap(final LabeledALabelIntTreeMap lvm) {
		this();
		if (lvm == null)
			return;
		for (final ALabel alabel : lvm.keySet()) {
			final LabeledIntTreeMap map1 = new LabeledIntTreeMap(lvm.get(alabel));
			this.map.put(alabel, map1);
		}
	}

	/**
	 * 
	 */
	public void clear() {
		this.map.clear();
	}

	/**
	 * @return a set view of this map. In particular, it returns a set of (ALabel, LabeledIntTreeMap) objects.<br>
	 *         Be careful: returned LabeledIntTreeMap(s) are not a copy but the maps inside this object.
	 *         THIS METHOD HAS NOT A GOOD PERFORMANCE
	 *         public ObjectSet<Entry<ALabel, LabeledIntTreeMap>> entrySet() {
	 *         return this.map.entrySet();
	 *         }
	 */

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if (o == this)
			return true;
		if (!(o instanceof LabeledALabelIntTreeMap))
			return false;
		final LabeledALabelIntTreeMap lvm = (LabeledALabelIntTreeMap) o;
		return this.map.equals(lvm.map);// this equals checks the size... so NO empty pair (key, {}) cannot be stored!
	}

	/**
	 * @return the minimal value of this map not considering upper/lower case label (node label), {@link Constants#INT_NULL} if the map is empty.
	 */
	public int getMinValue() {
		if (this.size() == 0)
			return Constants.INT_NULL;
		int min = Integer.MAX_VALUE, v = Constants.INT_NULL;
		for (final ALabel alabel : this.keySet()) {
			final LabeledIntTreeMap map1 = this.get(alabel);
			if ((map1 != null) && ((v = map1.getMinValue()) != Constants.INT_NULL)) {
				if (min > v) {
					min = v;
				}
			}
		}
		return min;
	}

	/**
	 * Returns the value associated to <code>(l, p)</code> if it exists,
	 * otherwise the minimal value among all labels consistent with <code>(l, p)</code>.
	 *
	 * @param l if it is null, {@link Constants#INT_NULL} is returned.
	 * @param p if it is null or empty, {@link Constants#INT_NULL} is returned.
	 * @return the value associated to the <code>(l, p)</code> if it exists or the minimal value among values associated to labels consistent by <code>l</code>.
	 *         If no labels are subsumed by <code>l</code>, {@link Constants#INT_NULL} is returned.
	 */
	public int getMinValueConsistentWith(final Label l, final ALabel p) {
		if ((l == null) || (p == null))// || p.isEmpty())
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
		return map1.getMinValueConsistentWith(l);
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link ALabel} representing the upper/lower case label (node label).
	 * @return the value associate to the key (label, p) if it exits, {@link Constants#INT_NULL} otherwise.
	 */
	public int getValue(final Label l, final ALabel p) {
		if ((l == null) || (p == null))// || p.isEmpty())
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
		return map1.get(l);
	}

	/**
	 * @param alabel
	 * @return the value to which the specified key is mapped, or null if this map contains no mapping for the key
	 */
	public LabeledIntTreeMap get(final ALabel alabel) {
		return this.map.get(alabel);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	/**
	 * @return a set view of all a-labels present into this map.
	 */
	public ObjectSet<ALabel> keySet() {
		return this.map.keySet();
	}

	/**
	 * @return a set of all labels present into this map.
	 */
	public ObjectSet<Label> labelSet() {
		ObjectSet<Label> labelSet = new ObjectRBTreeSet<>();
		for (LabeledIntTreeMap lset : this.map.values())
			labelSet.addAll(lset.keySet());
		return labelSet;
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @return see {@link #mergeTriple(Label, ALabel, int, boolean)}
	 * @see #mergeTriple(Label, ALabel, int, boolean)
	 */
	public boolean mergeTriple(final Label l, final ALabel p, final int i) {
		return this.mergeTriple(l, p, i, false);
	}

	/**
	 * @param newLabel
	 * @param newAlabel
	 * @param newValue
	 * @return true if the current map can represent the value. In positive case, an add of the element does not change the map.
	 *         If returns false, then the adding of the value to the map would modify the map.
	 */
	public boolean alreadyRepresents(final Label newLabel, final ALabel newAlabel, final int newValue) {
		int valuePresented = getValue(newLabel, newAlabel);
		if (valuePresented > newValue)
			return false;// the newValue would simplify the map.
		if (valuePresented != Constants.INT_NULL && valuePresented < newValue)
			return true;
		/**
		 * Check if there is already a value in the map that represents the new value.
		 */
		for (ALabel otherALabel : this.keySet()) {
			if (newAlabel.contains(otherALabel)) {
				LabeledIntTreeMap labeledValuesOfOtherALabel = this.get(otherALabel);
				if (labeledValuesOfOtherALabel.alreadyRepresents(newLabel, newValue)) {
					// a smaller conjuncted upper case value map already contains the input value
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Merges a label case value <code>(p,l,i)</code>.<br>
	 * The value is insert if there is not a labeled value in the set with label &lt;l,p&gt; or
	 * it is present with a value higher than i.<br>
	 * The method can remove or modify other labeled values of the set in order to minimize
	 * the labeled values present guaranteeing that no info is lost.
	 *
	 * @param newLabel a {@link it.univr.di.labeledvalue.Label} object.
	 * @param newAlabel a case name.
	 * @param newValue a int.
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final Label newLabel, final ALabel newAlabel, final int newValue, final boolean force) {

		if (this.alreadyRepresents(newLabel, newAlabel, newValue))
			return false;

		/**
		 * The input alabeled value must be insert!
		 * 2017-10-31
		 * Algorithm removes all alabeled values that will become redundant aftee the insertion of the input alabeled value.
		 * I verified that following optimization reduces global computation time.
		 */
		final int newAlabelSize = newAlabel.size();
		for (ALabel otherALabel : this.keySet()) {
			if (otherALabel.size() > newAlabelSize) { // Check only alabels that contain newALabel strictly.
				for (Object2IntMap.Entry<Label> entry : this.get(otherALabel).entrySet()) {
					Label otherLabel = entry.getKey();
					int otherValue = entry.getIntValue();
					// For example, in case that otherValue is (ab,CP,-3) and input label is (a,C,-3), it is possible to avoid the check!
					if (otherALabel.contains(newAlabel) && otherLabel.subsumes(newLabel) && otherValue >= newValue) {
						this.remove(otherLabel, otherALabel);
					}
				}
			}
		}

		// int mapALabelSize;
		LabeledIntTreeMap map1 = this.map.get(newAlabel);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap();
			this.map.put(ALabel.clone(newAlabel), map1);
			// mapALabelSize = 0;
			// } else {
			// mapALabelSize = map1.size();
		}
		boolean added = ((force) ? map1.putForcibly(newLabel, newValue) != Constants.INT_NULL : map1.put(newLabel, newValue));

		/*
		 * FIXME 2018-12-17 I try to avoid to re-check all inserted values...
		 */
		// if (added && mapALabelSize == map1.size()) {
		// // the insertion determined a simplification of the map, we re-check all values.
		// for (ALabel otherALabel : this.keySet()) {
		// LabeledIntTreeMap labeledValues = this.get(otherALabel);
		// if (labeledValues == map1)
		// continue;
		// for (Object2IntMap.Entry<Label> entry : labeledValues.entrySet()) {
		// Label otherLabel = entry.getKey();
		// int otherValue = entry.getIntValue();
		// // in case that label is (ab,CP,-3) and it is already present (a,C,-3), it is possible to avoid the check!
		// for (Object2IntMap.Entry<Label> entry1 : map1.entrySet()) {
		// Label label1 = entry1.getKey();
		// int value1 = entry1.getIntValue();
		// if (otherALabel.contains(newAlabel) && otherLabel.subsumes(label1) && otherValue >= value1) {
		// this.remove(otherLabel, otherALabel);
		// }
		// }
		// }
		// }
		// }
		return added;
	}

	/**
	 * Put the triple <code>(p,l,i)</code> into the map. If the triple is already present, it is overwritten.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i the new value to add.
	 * @return true if the valued has been added.
	 */
	public boolean putTriple(final Label l, final ALabel p, final int i) {
		return this.mergeTriple(l, p, i, false);
	}

	/**
	 * Put a map associate to key alabel.
	 * Possible previous map will be replaced.
	 * 
	 * @param alabel
	 * @param labeledValueMap
	 * @return the old map if one was associated to alabel, null otherwise
	 */
	public LabeledIntTreeMap put(ALabel alabel, LabeledIntMap labeledValueMap) {
		return this.map.put(alabel, (LabeledIntTreeMap) labeledValueMap);
	}

	/**
	 * Wrapper method. It calls mergeTriple(label, p, i, false);
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @return see {@link #mergeTriple(String, ALabel, int, boolean)}
	 * @see #mergeTriple(String, ALabel, int, boolean)
	 */
	public boolean mergeTriple(final String label, final ALabel p, final int i) {
		return this.mergeTriple(label, p, i, false);
	}

	/**
	 * Wrapper method to {@link #mergeTriple(Label, ALabel, int, boolean)}. 'label' parameter is converted to a Label before calling
	 * {@link #mergeTriple(Label, ALabel, int, boolean)}.
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final String label, final ALabel p, final int i, final boolean force) {
		if ((label == null) || (p == null) || (i == Constants.INT_NULL))// p.isEmpty() ||
			return false;
		final Label l = Label.parse(label);
		return this.mergeTriple(l, p, i, force);
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @return the old value if it exists, null otherwise.
	 */
	public int remove(final Label l, final ALabel p) {
		if ((l == null) || (p == null))
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
		int old = map1.remove(l);
		if (map1.size() == 0) {
			this.map.remove(p);// it is necessary for making equals working.
		}
		return old;
	}

	/**
	 * @return the number of elements of the map.
	 */
	public int size() {
		int n = 0;
		for (final LabeledIntTreeMap map1 : this.map.values()) {
			n += map1.size();
		}
		return n;
	}

	/**
	 * @return true if the map does not contain any labeled value.
	 */
	public boolean isEmpty() {
		return this.size() == 0;
	}

	/**
	 * @param label
	 * @param value
	 * @param nodeName this name is printed as it is. This method is necessary for saving the values of the map in a file.
	 * @return the canonical representation of the triple (as stated in ICAPS/ICAART papers), i.e.
	 *         {@link Constants#OPEN_PAIR}Alabel, value, label{@link Constants#CLOSE_PAIR}
	 */
	static final public String entryAsString(Label label, int value, ALabel nodeName) {
		StringBuffer s = new StringBuffer();
		s.append(Constants.OPEN_PAIR);
		s.append(nodeName);
		s.append(", ");
		s.append(Constants.formatInt(value));
		s.append(", ");
		s.append(label);
		s.append(Constants.CLOSE_PAIR);
		return s.toString();
	}

	/**
	 * Returns a string representing the content of the map, i.e., "{[&langle;entry&rangle; ]*}", where each &langle;entry&rangle; is written by
	 * {@link #entryAsString(Label, int, ALabel)}.
	 * 
	 * @return a string.
	 */
	@Override
	public String toString() {
		final StringBuffer s = new StringBuffer("{");
		for (final ALabel entryE : this.keySet()) {
			LabeledIntTreeMap entry = this.get(entryE);
			if (entry.size() == 0)
				continue;
			final ObjectList<Entry<Label>> sorted = new ObjectArrayList<>(entry.entrySet());
			sorted.sort(LabeledIntMap.entryComparator);

			for (final Object2IntMap.Entry<Label> entry1 : sorted) {
				s.append(entryAsString(entry1.getKey(), entry1.getIntValue(), entryE));
				s.append(' ');
			}
		}
		s.append("}");
		return s.toString();
	}

}
