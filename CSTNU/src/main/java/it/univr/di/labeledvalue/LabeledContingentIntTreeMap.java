package it.univr.di.labeledvalue;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Allows to manage an augmented Upper/Lower-case constraint that uses also a PLabel to characterize the constraint.
 *
 * @author Roberto Posenato
 */
public class LabeledContingentIntTreeMap implements Serializable {

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link HashMap#toString()}:<br>
	 *
	 * <pre>
	 * \{{(&langle;label&rangle;, &langle;T&rangle;, &langle;value&rangle;) }*\}
	 * </pre>
	 *
	 * @param arg
	 * @param optimized
	 * @return a LabeledPairMap object if args represents a valid map, null otherwise.
	 */
	public static LabeledContingentIntTreeMap parse(String arg, final boolean optimized) {
		// final Pattern splitterNode = Pattern.compile("〈|; ");
		LabeledContingentIntTreeMap.LOG.finest("Begin parse: " + arg);
		if ((arg == null) || (arg.length() < 3)) return null;
		final String labelCharsRE = "a-zA-Z0-9,\\-" + Constants.NOT + Constants.EMPTY_LABEL;
		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", arg)) return null;
		final LabeledContingentIntTreeMap newMap = new LabeledContingentIntTreeMap(optimized);

		arg = arg.substring(1, arg.length() - 2);
		LabeledContingentIntTreeMap.LOG.finest("Before split: '" + arg + "'");
		final Pattern splitterEntry = Pattern.compile("\\)|\\(");
		final String[] entryThreesome = splitterEntry.split(arg);
		LabeledContingentIntTreeMap.LOG.finest("EntryThreesome: " + Arrays.toString(entryThreesome));

		final Pattern splitterTriple = Pattern.compile(", ");
		for (final String s : entryThreesome) {
			LabeledContingentIntTreeMap.LOG.finest("s: '" + s + "'");
			if (s.length() > 1) {// s can be empty or a space.
				final String[] triple = splitterTriple.split(s);
				LabeledContingentIntTreeMap.LOG.finest("triple: " + Arrays.toString(triple));
				final Label l = Label.parse(triple[0]);
				LabeledContingentIntTreeMap.LOG.finest("Label: " + l);
				// LabeledNode is represented as " 〈<id>; {}; Obs: null〉 "
				// final String nodePart = labLitInt[1];//splitterNode.split(labLitInt[1]);
				final String node = triple[1];
				LabeledContingentIntTreeMap.LOG.finest("LabeledNode: " + node);
				final Integer j = Integer.parseInt(triple[2]);
				LabeledContingentIntTreeMap.LOG.finest("Value: " + j);

				newMap.mergeTriple(l, node, j, false);
			}
		}
		return newMap;
	}

	/**
	 * logger
	 */
	private static final Logger LOG = Logger.getLogger(LabeledContingentIntTreeMap.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Data structure.
	 * <ol>
	 * <li>A Upper/Lower Case constraint is a pair (nodeName, value) where nodeName is a name of a node and can be written either in all UPPER case or in all
	 * lower case. Such kind of constraint has been introduced by Morris Muscettola 2005.
	 * <li>A labeled Upper/Lower Case constraint is a pair (nodeName, (label, value)), where label represents scenario where value holds. Such kind of
	 * constraint has been introduced by Hunsbergher, Combi Posenato in 2012.
	 * <li>Each label is a conjunction of literals, i.e., of type {@link Label}.</li>
	 * <li>Since there may be more pairs with the same 'nodeName', a labeled Upper/Lower Case constraint is as a map of (nodeName, LabeledIntMap). See
	 * {@link LabeledIntMap}.
	 * <li>The name of a node is represented as String.
	 * </ol>
	 */
	private final Object2ObjectRBTreeMap<String, LabeledIntTreeMap> map;

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 */
	private final boolean optimize;

	/**
	 * Simple constructor. The internal structure is built and empty.
	 *
	 * @param optimized true if the labeled values have to be minimized.
	 */
	public LabeledContingentIntTreeMap(final boolean optimized) {
		this.map = new Object2ObjectRBTreeMap<>();
		this.optimize = optimized;
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm the map to clone. If null, 'this' will be a empty map.
	 * @param optimize
	 */
	public LabeledContingentIntTreeMap(final LabeledContingentIntTreeMap lvm, final boolean optimize) {
		this(optimize);
		if (lvm == null) return;
		for (final Entry<String, LabeledIntTreeMap> entry : lvm.entrySet()) {
			final LabeledIntTreeMap map1 = new LabeledIntTreeMap(entry.getValue(), optimize);
			this.map.put(entry.getKey(), map1);
		}
	}

	/**
	 *
	 */
	public void clear() {
		this.map.clear();
	}

	/**
	 * @return the map as a set of (nodeName, LabeledIntTreeMap).
	 *         Be careful: returned LabeledIntTreeMap(s) are not a copy but the maps inside this object.
	 */
	public ObjectSortedSet<Entry<String, LabeledIntTreeMap>> entrySet() {
		return this.map.entrySet();
	}

	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof LabeledContingentIntTreeMap)) return false;
		final LabeledContingentIntTreeMap lvm = (LabeledContingentIntTreeMap) o;
		return this.map.equals(lvm.map);
	}

	/**
	 * @return the minimal value of this map, {@link LabeledIntMap#INT_NULL} if the map is empty,
	 */
	public int getMinValue() {
		if (this.size() == 0) return LabeledIntMap.INT_NULL;
		int min = Integer.MAX_VALUE, v = LabeledIntMap.INT_NULL;
		for (final Entry<String, LabeledIntTreeMap> entry : this.entrySet()) {
			final LabeledIntTreeMap map1 = entry.getValue();
			if ((map1 != null) && ((v = map1.getMinValue()) != LabeledIntMap.INT_NULL)) {
				if (min > v) {
					min = v;
				}
			}
		}
		return min;
	}

	/**
	 * Returns the value associated to <code>(l, p)</code> if it exists, otherwise the minimal value among all labels consistent with <code>(l, p)</code>.
	 *
	 * @param l if it is null, {@link LabeledIntMap#INT_NULL} is returned.
	 * @param p if it is null or empty, {@link LabeledIntMap#INT_NULL} is returned.
	 * @return the value associated to the <code>(l, p)</code> if it exists or the minimal value among values associated to labels consistent by <code>l</code>.
	 *         If no labels are subsumed by <code>l</code>, {@link LabeledIntMap#INT_NULL} is returned.
	 */
	public int getMinValueConsistentWith(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty()) return LabeledIntMap.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) return LabeledIntMap.INT_NULL;
		return map1.getMinValueConsistentWith(l);
	}

	/**
	 * @param l
	 * @param p
	 * @return the value associate to the key (label, p) if it exits, {@link LabeledIntMap#INT_NULL} otherwise.
	 */
	public int getValue(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty()) return LabeledIntMap.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) return LabeledIntMap.INT_NULL;
		return map1.getValue(l);
	}

	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	/**
	 * @return the map as ((label,node),value) triples.
	 */
	public Set<Object2IntMap.Entry<Entry<Label, String>>> labeledTripleSet() {
		final Set<Object2IntMap.Entry<Entry<Label, String>>> set = new ObjectArraySet<>();

		for (final Entry<String, LabeledIntTreeMap> entryI : this.entrySet()) {
			for (final Object2IntMap.Entry<Label> entryI1 : entryI.getValue().entrySet()) {
				final Entry<Label, String> e1 = new SimpleEntry<>(entryI1.getKey(), entryI.getKey());
				final Object2IntMap.Entry<Entry<Label, String>> e2 = new AbstractObject2IntMap.BasicEntry<>(e1, entryI1.getIntValue());
				set.add(e2);
			}
		}
		return set;
	}

	/**
	 * @param l
	 * @param p
	 * @param i
	 * @return see {@link #mergeTriple(Label, String, int, boolean)}
	 * @see #mergeTriple(Label, String, int, boolean)
	 */
	public boolean mergeTriple(final Label l, final String p, final int i) {
		return this.mergeTriple(l, p, i, false);
	}

	/**
	 * Merges a label case value
	 * <p,l,i>
	 * .<br>
	 * The value is insert if there is not a labeled value in the set with label <l,p> or it is present with a value higher than i.<br>
	 * The method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info is lost.
	 *
	 * @param l
	 * @param p
	 * @param i
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final Label l, final String p, final int i, final boolean force) {
		if ((l == null) || (p == null) || p.isEmpty() || (i == LabeledIntMap.INT_NULL)) return false;
		LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap(this.optimize);
			map1.putForcibly(l, i);
			this.map.put(p, map1);
			return true;
		}
		return ((force) ? map1.putForcibly(l, i) != LabeledIntMap.INT_NULL : map1.put(l, i));
	}

	/**
	 * Wrapper method. It calls mergeTriple(label, p, i, true);
	 *
	 * @param label
	 * @param p
	 * @param i
	 * @return see {@link #mergeTriple(String, String, int, boolean)}
	 * @see #mergeTriple(String, String, int, boolean)
	 */
	public boolean mergeTriple(final String label, final String p, final int i) {
		return this.mergeTriple(label, p, i, false);
	}

	/**
	 * Wrapper method to {@link #mergeTriple(Label, String, int, boolean)}.
	 * 'label' parameter is converted to a Label before calling {@link #mergeTriple(Label, String, int, boolean)}.
	 *
	 * @param label
	 * @param p
	 * @param i
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final String label, final String p, final int i, final boolean force) {
		if ((label == null) || (p == null) || p.isEmpty() || (i == LabeledIntMap.INT_NULL)) return false;
		final Label l = Label.parse(label);
		return this.mergeTriple(l, p, i, force);
	}

	/**
	 * Put the triple <code>(p,l,i)</code> into the map. If the triple is already present, it is overwritten.
	 *
	 * @param l
	 * @param p
	 * @param i
	 * @return the value overwritten.
	 */
	public int putTriple(final Label l, final String p, final int i) {
		if ((l == null) || (p == null) || p.isEmpty() || (i == LabeledIntMap.INT_NULL)) return LabeledIntMap.INT_NULL;
		LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap(this.optimize);
			this.map.put(p, map1);
		}
		return map1.putForcibly(l, i);
	}

	/**
	 * @param l
	 * @param p
	 * @return the old value if it exists, null otherwise.
	 */
	public int remove(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty()) return LabeledIntMap.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) return LabeledIntMap.INT_NULL;
		return map1.remove(l);
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

	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * @param lower if true, the first element of the pair is rendered as lower case.
	 * @return a string representing the content of the map. The format is <label, node, value>
	 */
	public String toString(final boolean lower) {
		final StringBuffer s = new StringBuffer("{");
		// tricky

		for (final Entry<String, LabeledIntTreeMap> entry : this.entrySet()) {
			for (final Object2IntMap.Entry<Label> entry1 : entry.getValue().entrySet()) {
				s.append("(");
				s.append(entry1.getKey());
				s.append(", ");
				String s1 = entry.getKey();
				s1 = (lower) ? s1.toLowerCase() : s1.toUpperCase();
				s.append(s1);
				s.append(", ");
				final int value = entry1.getValue();
				if (value == Constants.INT_POS_INFINITE) {
					s.append(Constants.INFINITY_SYMBOL);
				} else {
					s.append(value);
				}
				s.append(") ");
			}
		}
		s.append("}");
		return s.toString();
	}

}
