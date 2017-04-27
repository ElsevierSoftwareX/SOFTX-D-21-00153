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
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;

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
	private static final String labelCharsRE = Constants.ALETTER + Constants.ALABEL_SEPARATOR + ",\\- " + Constants.NOT + Constants.EMPTY_LABEL
			+ Constants.UNKNOWNstring + Constants.PROPOSITION_RANGES + Constants.INFINITY_SYMBOL;

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
	@SuppressWarnings("unused")
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

		ALabelAlphabet alpha = new ALabelAlphabet(3);
		ALabel a = new ALabel(new ALetter("A"), alpha);
		ALabel b = new ALabel(new ALetter("B"), alpha);
		ALabel c = new ALabel(new ALetter("C"), alpha);

		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			map.clear();
			map.putTriple(Label.emptyLabel, a, 109);
			map.putTriple(l1, a, 10);
			map.putTriple(l2, a, 20);
			map.putTriple(l3, a, 25);
			map.putTriple(l4, a, 23);
			map.putTriple(l5, a, 22);
			map.putTriple(l6, a, 23);
			map.putTriple(l7, a, 20);
			map.putTriple(l8, a, 20);
			map.putTriple(l9, a, 21);
			map.putTriple(l10, a, 11);
			map.putTriple(l11, a, 11);
			map.putTriple(l12, a, 11);
			map.putTriple(l13, a, 24);
			map.putTriple(l14, a, 22);
			map.putTriple(l15, a, 23);
			map.putTriple(l16, a, 20);
			map.putTriple(l17, a, 23);
			map.putTriple(l18, a, 23);
			map.putTriple(l19, a, 23);
			map.putTriple(l20, a, 23);
			map.putTriple(l1, b, 10);
			map.putTriple(l2, b, 20);
			map.putTriple(l3, b, 25);
			map.putTriple(l4, b, 23);
			map.putTriple(l5, b, 22);
			map.putTriple(l6, b, 23);
			map.putTriple(l7, b, 20);
			map.putTriple(l8, b, 20);
			map.putTriple(l9, b, 21);
			map.putTriple(l10, b, 11);
			map.putTriple(l11, b, 11);
			map.putTriple(l12, b, 11);
			map.putTriple(l13, b, 24);
			map.putTriple(l14, b, 22);
			map.putTriple(l15, b, 23);
			map.putTriple(l16, b, 20);
			map.putTriple(l17, b, 23);
			map.putTriple(l18, b, 23);
			map.putTriple(l19, b, 23);
			map.putTriple(l20, b, 23);
			map.putTriple(l1, c, 10);
			map.putTriple(l2, c, 20);
			map.putTriple(l3, c, 25);
			map.putTriple(l4, c, 23);
			map.putTriple(l5, c, 22);
			map.putTriple(l6, c, 23);
			map.putTriple(l7, c, 20);
			map.putTriple(l8, c, 20);
			map.putTriple(l9, c, 21);
			map.putTriple(l10, c, 11);
			map.putTriple(l11, c, 11);
			map.putTriple(l12, c, 11);
			map.putTriple(l13, c, 24);
			map.putTriple(l14, c, 22);
			map.putTriple(l15, c, 23);
			map.putTriple(l16, c, 20);
			map.putTriple(l17, c, 23);
			map.putTriple(l18, c, 23);
			map.putTriple(l19, c, 23);
			map.putTriple(l20, c, 23);
		}
		long endTime = System.nanoTime();
		// System.out.println("LABELED VALUE SET-TREE MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFirst map: " + map
		// + ".\nTime: (ms): "
		// + ((endTime - startTime) / msNorm));
		// String rightAnswer = "{(⊡, A, 23) (abc¬f, A, 10) (abd¿f, A, 11) (b¬d¿ef, A, 22) (abcdef, A, 20) (ae¬f, A, 20) (¬a¿bf, A, 20) (a¿d¬f, A, 11) (c¬e, A,
		// 11) (c, A, 22) }";
		// System.out.println("The right final set is " + parse(rightAnswer,alpha) + ".");
		// System.out.println("Is equal? " + parse(rightAnswer,alpha).equals(map));

		startTime = System.nanoTime();
		int min = 1000;
		for (int i = 0; i < nTest; i++) {
			min = map.getMinValue();
		}
		endTime = System.nanoTime();
		// System.out.println("Execution time for determining the min value (" + min + ") (mean over " + nTest + " tests). (ms): "
		// + ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		Label l = Label.parse("abd¿f");
		for (int i = 0; i < nTest; i++) {
			min = map.getValue(l, a);
		}
		endTime = System.nanoTime();
		// System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
		// + ((endTime - startTime) / msNorm));

		// startTime = System.nanoTime();
		// map.putTriple(Label.parse("c"), a, 11);
		// map.putTriple(Label.parse("¬c"), a, 11);
		// endTime = System.nanoTime();
		// System.out.println("After the insertion of (c,A,11) and (¬c,A,11) the map becomes: " + map);
		// System.out.println("Execution time for simplification (ms): "
		// + ((endTime - startTime) / 1.0E6));

		map.clear();
		map.putTriple(Label.parse("c"), a, 11);
		map.putTriple(Label.parse("c"), a.conjunction(b), 11);
		map.putTriple(Label.parse("c"), a.conjunction(c), 11);
		map.putTriple(Label.parse("c"), a.conjunction(c), 11);
		map.putTriple(Label.parse("c"), a.conjunction(c), 11);
		System.out.println("After the insertion of conjuncted ALabel: " + map);
	}

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
	 * The format of the string is given by the method {@link #toString()}:<code>\{{(&lang;label&rang;, &lang;Alabel&rang;, &lang;value&rang;) }*\}</code>
	 * It also parse the old format: <code>\{{(&lang;Alabel&rang;, &lang;value&rang;, &lang;label&rang;) }*\}</code>
	 * 
	 * @param arg a {@link java.lang.String} object.
	 * @param alphabet
	 * @return a LabeledPairMap object if args represents a valid map, null otherwise.
	 */
	public static LabeledContingentIntTreeMap parse(String arg, ALabelAlphabet alphabet) {
		// final Pattern splitterNode = Pattern.compile("〈|; ");
		LabeledContingentIntTreeMap.LOG.finest("Begin parse: " + arg);
		if ((arg == null) || (arg.length() < 3))
			return null;

		if (!patternlabelCharsRE.matcher(arg).matches())
			return null;
		final LabeledContingentIntTreeMap newMap = new LabeledContingentIntTreeMap();

		arg = arg.replaceAll("[{}]", "");
		// arg = arg.substring(1, arg.length() - 2);
		LabeledContingentIntTreeMap.LOG.finest("Before split: '" + arg + "'");
		final Pattern splitterEntry = Pattern.compile("\\)|\\(");
		final String[] entryThreesome = splitterEntry.split(arg);
		LabeledContingentIntTreeMap.LOG.finest("EntryThreesome: " + Arrays.toString(entryThreesome));

		final Pattern splitterTriple = Pattern.compile(", ");
		if (alphabet == null)
			alphabet = new ALabelAlphabet();
		int j;
		String labelStr, aLabelStr, valueStr;
		for (final String s : entryThreesome) {
			LabeledContingentIntTreeMap.LOG.finest("s: '" + s + "'");
			if (s.length() > 1) {// s can be empty or a space.
				final String[] triple = splitterTriple.split(s);
				LabeledContingentIntTreeMap.LOG.finest("triple: " + Arrays.toString(triple));
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
				LabeledContingentIntTreeMap.LOG.finest("Label: " + l);
				if (valueStr.equals("-" + Constants.INFINITY_SYMBOLstring))
					j = Constants.INT_NEG_INFINITE;
				else
					j = Integer.parseInt(valueStr);
				LabeledContingentIntTreeMap.LOG.finest("Value: " + j);
				// LabeledNode is represented as " 〈<id>; {}; Obs: null〉 "
				// final String nodePart = labLitInt[1];//splitterNode.split(labLitInt[1]);
				final ALabel node = new ALabel(new ALetter(aLabelStr), alphabet);
				LabeledContingentIntTreeMap.LOG.finest("LabeledNode: " + node);

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
	private final Object2ObjectArrayMap<ALabel, LabeledIntTreeMap> map;

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
		for (final Entry<ALabel, LabeledIntTreeMap> entry : lvm.entrySet()) {
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
	public ObjectSet<Entry<ALabel, LabeledIntTreeMap>> entrySet() {
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
		for (final Entry<ALabel, LabeledIntTreeMap> entry : this.entrySet()) {
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
	public int getMinValueConsistentWith(final Label l, final ALabel p) {
		if ((l == null) || (p == null) || p.isEmpty())
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
	public ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> labeledTripleSet() {
		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> set = new ObjectArraySet<>();

		for (final Entry<ALabel, LabeledIntTreeMap> entryI : this.entrySet()) {
			for (final Object2IntMap.Entry<Label> entryI1 : entryI.getValue().entrySet()) {
				final Entry<Label, ALabel> e1 = new SimpleEntry<>(entryI1.getKey(), entryI.getKey());
				final Object2IntMap.Entry<Entry<Label, ALabel>> e2 = new AbstractObject2IntMap.BasicEntry<>(e1, entryI1.getIntValue());
				set.add(e2);
			}
		}
		return set;
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
	 * Merges a label case value <code>(p,l,i)</code>.<br>
	 * The value is insert if there is not a labeled value in the set with label &lt;l,p&gt; or
	 * it is present with a value higher than i.<br>
	 * The method can remove or modify other labeled values of the set in order to minimize
	 * the labeled values present guaranteeing that no info is lost.
	 *
	 * @param label a {@link it.univr.di.labeledvalue.Label} object.
	 * @param alabel a case name.
	 * @param i a int.
	 * @param force true if the value has to be stored without label optimization.
	 * @return true if the triple is stored, false otherwise.
	 */
	public boolean mergeTriple(final Label label, final ALabel alabel, final int i, final boolean force) {
		if ((label == null) || (alabel == null) || alabel.isEmpty() || (i == Constants.INT_NULL))
			return false;
		ALabel alabelToStore = new ALabel(alabel);
		LabeledIntTreeMap map1 = this.map.get(alabelToStore);
		if (map1 == null) {
			map1 = new LabeledIntTreeMap();
			this.map.put(alabelToStore, map1);
		}

		// CHECK that there is no a more general label
		boolean isEqual = map1.get(label)==i;
		boolean hasToBeAdded = true;
		//even if it is already present, the following cycle can clean some redundant entry
		//it also remove the same entry (if present).
		for (Object2IntMap.Entry<Entry<Label, ALabel>> entry : this.labeledTripleSet()) {
			Label otherLabel = entry.getKey().getKey();
			ALabel otherALabel = entry.getKey().getValue();
			int otherValue = entry.getIntValue();
			// FIXME: I try to optimize stored label
			// (ab,CP,-2) vs. (a,C,-3)/
			if (otherLabel.subsumes(label) && otherALabel.contains(alabel) && otherValue >= i) {
				this.remove(otherLabel, otherALabel);
			}
			if (label.subsumes(otherLabel) && alabel.contains(otherALabel) && i >= otherValue) {
				// it is not necessary to add this value!
				hasToBeAdded = false;
			}
		}
		if (!hasToBeAdded && !isEqual)
			return false;
		return ((force) ? map1.putForcibly(label, i) != Constants.INT_NULL : map1.put(label, i));
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
		if ((label == null) || (p == null) || p.isEmpty() || (i == Constants.INT_NULL))
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
	static public String entryAsString(Label label, int value, ALabel nodeName, boolean lower) {
		StringBuffer s = new StringBuffer();
		s.append(Constants.OPEN_PAIR);
		s.append((lower) ? nodeName.toLowerCase() : nodeName.toUpperCase());
		s.append(", ");
		s.append(Constants.formatInt(value));
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
		for (final Entry<ALabel, LabeledIntTreeMap> entry : this.entrySet()) {
			for (final Object2IntMap.Entry<Label> entry1 : entry.getValue().entrySet()) {
				s.append(entryAsString(entry1.getKey(), entry1.getIntValue(), entry.getKey(), lower));
				s.append(' ');
			}
		}
		s.append("}");
		return s.toString();
	}

}
