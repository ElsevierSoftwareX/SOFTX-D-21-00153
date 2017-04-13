package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Allows to manage an augmented Upper/Lower-case constraint that uses also a PLabel to characterize
 * the constraint.
 * This is a simple implementation: labeled value are grouped by node label (Upper/Lower-case label).
 * Each laveled value group is represented as LabeledIntTreeMap.
 * <p>
 * <table border="1">
 * <caption>Execution time (ms) for some operations w.r.t the core data structure of the class.</caption>
 * <tr>
 * <th>Operation</th>
 * <th>Using all RB Tree (ms)</th>
 * <th>Using fastUtil.ObjectArrayMap (ms)</th>
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
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class LabeledContingentIntTreeMap implements Serializable {

	/**
	 * \w because it is necessary to accept also node names!
	 */
	private static final String labelCharsRE = "\\w,\\- " + Constants.NOT + Constants.EMPTY_LABEL + Constants.UNKNOWNstring;

	/**
	 * logger
	 */
	private static final Logger LOG = Logger.getLogger(LabeledContingentIntTreeMap.class.getName());

	/**
	 * Matcher for RE
	 */
	private static final Pattern patternlabelCharsRE = Pattern.compile("\\{[\\(" + labelCharsRE + "\\) ]*\\}");

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * Main.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 */
	static public void main(final String[] args) {

		final int nTest = (int) 1E3;
		final double msNorm = 1.0E6 * nTest;

		final LabeledContingentIntTreeMap map = new LabeledContingentIntTreeMap();

		final Label l1 = Label.parse("abc¬f");
		final Label l2 = Label.parse("abcdef");
		final Label l3 = Label.parse("a¬bc¬de¬f");
		final Label l4 = Label.parse("¬b¬d¬f");
		final Label l5 = Label.parse("ec");
		final Label l6 = Label.parse("¬fedcba");
		final Label l7 = Label.parse("ae¬f");
		final Label l8 = Label.parse("¬af¿b");
		final Label l9 = Label.parse("¬af¿b");
		final Label l10 = Label.parse("¬ec");
		final Label l11 = Label.parse("abd¿f");
		final Label l12 = Label.parse("a¿d¬f");
		final Label l13 = Label.parse("¬b¿d¿f");
		final Label l14 = Label.parse("b¬df¿e");
		final Label l15 = Label.parse("e¬c");
		final Label l16 = Label.parse("ab¿d¿f");
		final Label l17 = Label.parse("ad¬f");
		final Label l18 = Label.parse("b¿d¿f");
		final Label l19 = Label.parse("¬b¬df¿e");
		final Label l20 = Label.parse("¬e¬c");

		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			map.clear();
			map.putTriple(Label.emptyLabel, "A", 109);
			map.putTriple(l1, "A", 10);
			map.putTriple(l2, "A", 20);
			map.putTriple(l3, "A", 25);
			map.putTriple(l4, "A", 23);
			map.putTriple(l5, "A", 22);
			map.putTriple(l6, "A", 23);
			map.putTriple(l7, "A", 20);
			map.putTriple(l8, "A", 20);
			map.putTriple(l9, "A", 21);
			map.putTriple(l10, "A", 11);
			map.putTriple(l11, "A", 11);
			map.putTriple(l12, "A", 11);
			map.putTriple(l13, "A", 24);
			map.putTriple(l14, "A", 22);
			map.putTriple(l15, "A", 23);
			map.putTriple(l16, "A", 20);
			map.putTriple(l17, "A", 23);
			map.putTriple(l18, "A", 23);
			map.putTriple(l19, "A", 23);
			map.putTriple(l20, "A", 23);
			map.putTriple(l1, "B", 10);
			map.putTriple(l2, "B", 20);
			map.putTriple(l3, "B", 25);
			map.putTriple(l4, "B", 23);
			map.putTriple(l5, "B", 22);
			map.putTriple(l6, "B", 23);
			map.putTriple(l7, "B", 20);
			map.putTriple(l8, "B", 20);
			map.putTriple(l9, "B", 21);
			map.putTriple(l10, "B", 11);
			map.putTriple(l11, "B", 11);
			map.putTriple(l12, "B", 11);
			map.putTriple(l13, "B", 24);
			map.putTriple(l14, "B", 22);
			map.putTriple(l15, "B", 23);
			map.putTriple(l16, "B", 20);
			map.putTriple(l17, "B", 23);
			map.putTriple(l18, "B", 23);
			map.putTriple(l19, "B", 23);
			map.putTriple(l20, "B", 23);
			map.putTriple(l1, "C", 10);
			map.putTriple(l2, "C", 20);
			map.putTriple(l3, "C", 25);
			map.putTriple(l4, "C", 23);
			map.putTriple(l5, "C", 22);
			map.putTriple(l6, "C", 23);
			map.putTriple(l7, "C", 20);
			map.putTriple(l8, "C", 20);
			map.putTriple(l9, "C", 21);
			map.putTriple(l10, "C", 11);
			map.putTriple(l11, "C", 11);
			map.putTriple(l12, "C", 11);
			map.putTriple(l13, "C", 24);
			map.putTriple(l14, "C", 22);
			map.putTriple(l15, "C", 23);
			map.putTriple(l16, "C", 20);
			map.putTriple(l17, "C", 23);
			map.putTriple(l18, "C", 23);
			map.putTriple(l19, "C", 23);
			map.putTriple(l20, "C", 23);
		}
		long endTime = System.nanoTime();
		System.out.println("LABELED VALUE SET-TREE MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFirst map: " + map
				+ ".\nTime: (ms): "
				+ ((endTime - startTime) / msNorm));
		String rightAnswer = "{(⊡, A, 23) (abc¬f, A, 10) (abd¿f, A, 11) (b¬d¿ef, A, 22) (abcdef, A, 20) (ae¬f, A, 20) (¬a¿bf, A, 20) (a¿d¬f, A, 11) (c¬e, A, 11) (c, A, 22) }";
		System.out.println("The right final set is " + parse(rightAnswer) + ".");
		System.out.println("Is equal? " + parse(rightAnswer).equals(map));

		startTime = System.nanoTime();
		int min = 1000;
		for (int i = 0; i < nTest; i++) {
			min = map.getMinValue();
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for determining the min value (" + min + ") (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		Label l = Label.parse("abd¿f");
		for (int i = 0; i < nTest; i++) {
			min = map.getValue(l, "A");
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		map.putTriple(Label.parse("c"), "A", 11);
		map.putTriple(Label.parse("¬c"), "A", 11);
		endTime = System.nanoTime();
		System.out.println("After the insertion of (c,11) and (¬c,11) the map becomes: " + map);
		System.out.println("Execution time for simplification (ms): "
				+ ((endTime - startTime) / 1.0E6));
	}

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link java.util.HashMap#toString()}:<br>
	 *
	 * <pre>
	 * \{{(&lang;label&rang;, &lang;T&rang;, &lang;value&rang;) }*\}
	 * </pre>
	 *
	 * @param arg
	 *            a {@link java.lang.String} object.
	 * @return a LabeledPairMap object if args represents a valid map, null otherwise.
	 */
	public static LabeledContingentIntTreeMap parse(String arg) {
		// final Pattern splitterNode = Pattern.compile("〈|; ");
		LabeledContingentIntTreeMap.LOG.finest("Begin parse: " + arg);
		if ((arg == null) || (arg.length() < 3))
			return null;

		if (!patternlabelCharsRE.matcher(arg).matches())
			return null;
		final LabeledContingentIntTreeMap newMap = new LabeledContingentIntTreeMap();

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
	 * Data structure.
	 * <ol>
	 * <li>A Upper/Lower Case constraint is a pair (nodeName, value) where nodeName is a name of a node
	 * and can be written either in all UPPER case or in all lower case.
	 * Such kind of constraint has been introduced by Morris Muscettola 2005.
	 * <li>A labeled Upper/Lower Case constraint is a pair (nodeName, (label, value)), where label
	 * represents scenario where value holds.
	 * Such kind of constraint has been introduced by Hunsbergher, Combi Posenato in 2012.
	 * <li>Each label is a conjunction of literals, i.e., of type {@link Label}.</li>
	 * <li>Since there may be more pairs with the same 'nodeName', a labeled Upper/Lower Case constraint
	 * is as a map of (nodeName, LabeledIntMap). See {@link LabeledIntMap}.
	 * <li>The name of a node is represented as String.
	 * </ol>
	 */
	private final Object2ObjectArrayMap<String, LabeledIntTreeMap> map;

	/**
	 * Simple constructor. The internal structure is built and empty.
	 */
	public LabeledContingentIntTreeMap() {
		this.map = new Object2ObjectArrayMap<>();
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm the map to clone. If null, 'this' will be a empty map.
	 */
	public LabeledContingentIntTreeMap(final LabeledContingentIntTreeMap lvm) {
		this();
		if (lvm == null)
			return;
		for (final Entry<String, LabeledIntTreeMap> entry : lvm.entrySet()) {
			final LabeledIntTreeMap map1 = new LabeledIntTreeMap(entry.getValue());
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
	 * @return the map as a set of (nodeName, LabeledIntTreeMap).<br>
	 *         Be careful: returned LabeledIntTreeMap(s) are not a copy but the maps inside this object.
	 */
	public ObjectSet<Entry<String, LabeledIntTreeMap>> entrySet() {
		return this.map.entrySet();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof LabeledContingentIntTreeMap))
			return false;
		final LabeledContingentIntTreeMap lvm = (LabeledContingentIntTreeMap) o;
		return this.map.equals(lvm.map);
	}

	/**
	 * @return the minimal value of this map not considering upper/lower case label (node label), {@link Constants#INT_NULL} if the map is empty.
	 */
	public int getMinValue() {
		if (this.size() == 0)
			return Constants.INT_NULL;
		int min = Integer.MAX_VALUE, v = Constants.INT_NULL;
		for (final Entry<String, LabeledIntTreeMap> entry : this.entrySet()) {
			final LabeledIntTreeMap map1 = entry.getValue();
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
	public int getMinValueConsistentWith(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty())
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
		return map1.getMinValueConsistentWith(l);
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} representing the upper/lower case label (node label).
	 * @return the value associate to the key (label, p) if it exits, {@link Constants#INT_NULL} otherwise.
	 */
	public int getValue(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty())
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
		return map1.get(l);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.map.hashCode();
	}

	/**
	 * @return the map as ((label,node),value) triples.
	 */
	public ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> labeledTripleSet() {
		final ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> set = new ObjectArraySet<>();

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
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @return see {@link #mergeTriple(Label, String, int, boolean)}
	 * @see #mergeTriple(Label, String, int, boolean)
	 */
	public boolean mergeTriple(final Label l, final String p, final int i) {
		return this.mergeTriple(l, p, i, false);
	}

	/**
	 * Merges a label case value &lt;p,l,i&gt;.<br>
	 * The value is insert if there is not a labeled value in the set with label &lt;l,p&gt; or
	 * it is present with a value higher than i.<br>
	 * The method can remove or modify other labeled values of the set in order to minimize
	 * the labeled values present guaranteeing that no info is lost.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final Label l, final String p, final int i, final boolean force) {
		if ((l == null) || (p == null) || p.isEmpty() || (i == Constants.INT_NULL))
			return false;
		LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap();
			map1.putForcibly(l, i);
			this.map.put(p, map1);
			return true;
		}
		return ((force) ? map1.putForcibly(l, i) != Constants.INT_NULL : map1.put(l, i));
	}

	/**
	 * Wrapper method. It calls mergeTriple(label, p, i, false);
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @return see {@link #mergeTriple(String, String, int, boolean)}
	 * @see #mergeTriple(String, String, int, boolean)
	 */
	public boolean mergeTriple(final String label, final String p, final int i) {
		return this.mergeTriple(label, p, i, false);
	}

	/**
	 * Wrapper method to {@link #mergeTriple(Label, String, int, boolean)}. 'label' parameter is converted to a Label before calling
	 * {@link #mergeTriple(Label, String, int, boolean)}.
	 *
	 * @param label a {@link java.lang.String} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i a int.
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final String label, final String p, final int i, final boolean force) {
		if ((label == null) || (p == null) || p.isEmpty() || (i == Constants.INT_NULL))
			return false;
		final Label l = Label.parse(label);
		return this.mergeTriple(l, p, i, force);
	}

	/**
	 * Put the triple <code>(p,l,i)</code> into the map. If the triple is already present, it is overwritten.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @param i the new value to add.
	 * @return true if the valued has been added.
	 */
	public boolean putTriple(final Label l, final String p, final int i) {
		if ((l == null) || (p == null) || p.isEmpty() || (i == Constants.INT_NULL))
			return false;
		LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap();
			this.map.put(p, map1);
		}
		return map1.put(l, i);
	}

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param p a {@link java.lang.String} object.
	 * @return the old value if it exists, null otherwise.
	 */
	public int remove(final Label l, final String p) {
		if ((l == null) || (p == null) || p.isEmpty())
			return Constants.INT_NULL;
		final LabeledIntTreeMap map1 = this.map.get(p);
		if (map1 == null)
			return Constants.INT_NULL;
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.toString(false);
	}

	/**
	 * @param label
	 * @param value
	 * @param nodeName
	 * @param lower
	 * @return the canonical representation of the triple (as stated in ICAPS/ICAART papers)
	 */
	static public String entryAsString(Label label, int value, String nodeName, boolean lower) {
		StringBuffer s = new StringBuffer();
		s.append(Constants.OPEN_PAIR);
		s.append(Constants.formatInt(value));
		s.append(", ");
		s.append( (lower) ? nodeName.toLowerCase() : nodeName.toUpperCase() );
		s.append(", ");
		s.append(label);
		s.append(Constants.CLOSE_PAIR);
		return s.toString();
	}
	/**
	 * @param lower if true, the alphabetic label (the name of contingent t.p.) is rendered as lower case.
	 * @return a string representing the content of the map. The format is &lt;value, nodeName, label&gt;
	 */
	public String toString(final boolean lower) {
		final StringBuffer s = new StringBuffer("{");
		for (final Entry<String, LabeledIntTreeMap> entry : this.entrySet()) {
			for (final Object2IntMap.Entry<Label> entry1 : entry.getValue().entrySet()) {
				s.append(entryAsString(entry1.getKey(), entry1.getIntValue(), entry.getKey(), lower));
				s.append(' '); 
			}
		}
		s.append("}");
		return s.toString();
	}

}
