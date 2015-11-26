import it.univr.di.labeledvalue.Constants;

///**
// * 
// */
//package it.univr.di.attic;
//
//import it.univr.di.attic.LabeledValueTreeMap;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.Literal;
//
//import java.io.Serializable;
//import java.util.AbstractMap.SimpleEntry;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Allows to manage an augmented Upper/Lower-case constraint that uses also a PLabel to characterize the constraint.
// * 
// * @author posenato
// * @param <T> the type of value
// */
//public class LabeledCaseCValueMap<T extends Number & Comparable<T>> implements Serializable {
//
//	/**
//	 * logger
//	 */
//	private static final Logger LOG = Logger.getLogger(LabeledCaseCValueMap.class.getName());
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 2L;
//
//	@SuppressWarnings({ "javadoc" })
//	public static void main(String[] args) {
//
//		final Literal a = new Literal('a'), b = new Literal('b');
//
//		final Label l1 = new Label(a);
//		l1.conjunct(b);
//	
//		System.out.println("Parsing:" );
//		LabeledCaseCValueMap.parse("{(⊡, c, 100) (⊡, c, 101) }");
//	}
//
//	/**
//	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by the string.<br>
//	 * The format of the string is given by the method {@link HashMap#toString()}:<br>
//	 * 
//	 * <pre>
//	 * \{{(&langle;label&rangle;, &langle;T&rangle;, &langle;value&rangle;) }*\}
//	 * </pre>
//	 * 
//	 * @param arg
//	 * @return a LabeledPairMap object if args represents a valid map, null otherwise.
//	 */
//	public static LabeledCaseCValueMap<Integer> parse(String arg) {
////		final Pattern splitterNode = Pattern.compile("〈|; ");
//		LOG.finest("Begin parse: "+arg);
//		if (arg == null || arg.length()<3) return null;
//		final String labelCharsRE = "+Constants.propositionLetterRanges+"0-9,\\-" + Constants.NOT + Constants.EMPTY_LABEL;
//		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", arg)) return null;
//		final LabeledCaseCValueMap<Integer> newMap = new LabeledCaseCValueMap<>();
//
//		arg = arg.substring(1, arg.length()-2);
//		LOG.finest("Before split: '"+arg+"'");
//		final Pattern splitterEntry = Pattern.compile("\\)|\\(");
//		final String[] entryThreesome = splitterEntry.split(arg);
//		LOG.finest("EntryThreesome: " + Arrays.toString(entryThreesome));
//
//		
//		final Pattern splitterTriple = Pattern.compile(", ");
//		for (final String s : entryThreesome) {
//			LOG.finest("s: '" + s+"'");
//			if (s.length() > 1) {//s can be empty or a space.
//				final String[] triple = splitterTriple.split(s);
//				LOG.finest("triple: " + Arrays.toString(triple));
//				final Label l = Label.parse(triple[0]);
//				LOG.finest("Label: " + l);
//				// LabeledNode is represented as " 〈<id>; {}; Obs: null〉 "
////				final String nodePart = labLitInt[1];//splitterNode.split(labLitInt[1]);
//				final String node = triple[1];
//				LOG.finest("LabeledNode: " + node);
//				final Integer j = Integer.parseInt(triple[2]);
//				LOG.finest("Value: " + j);
//				
//				newMap.mergeTriple(l, node, j);
//			}
//		}
//		return newMap;
//	}
//
//	/**
//	 * Data structure.
//	 * <ol>
//	 * <li>A Upper/Lower Case constraint is a pair (name, value) where name is a name of a node and can be written either in all UPPER case or in all lower
//	 * case. Such kind of constraint has been introduced by Morris Muscettola 2005.
//	 * <li>A labeled Upper/Lower Case constraint is a pair (name, (label, value)), where label represents scenario where value holds. Such kind of constraint
//	 * has been introduced by Hunsbergher, Combi Posenato.
//	 * <li>Each label is a conjunction of literals, i.e., of type {@link Label}.</li>
//	 * <li>Since there may be more pairs with the same 'name', I represent a labeled Upper/Lower Case constraint as a map of (name, LabeledValueTreeMap). See
//	 * {@link LabeledValueTreeMap}.
//	 * <li>The name of a node is represented as String.
//	 * </ol>
//	 */
//	private HashMap<String, LabeledValueTreeMap<T>> map;
//	
//	/**
//	 * To activate all optimization code in order to remove the redundant label in the set.
//	 */
//	private boolean optimize =false;
//
//
//
//	/**
//	 * Simple constructor. The internal structure is built and empty.
//	 */
//	public LabeledCaseCValueMap() {
//		map = new HashMap<>();
//	}
//
//	/**
//	 * Constructor to clone the structure.
//	 * 
//	 * @param lvm the map to clone. If null, 'this' will be a empty map.
//	 */
//	public LabeledCaseCValueMap(LabeledCaseCValueMap<T> lvm) {
//		this();
//		if (lvm == null) return;
//		for (final Entry<String, LabeledValueTreeMap<T>> entry : lvm.labeledPairSet()) {
//			LabeledValueTreeMap<T> map1 = new LabeledValueTreeMap<>(entry.getValue(),this.optimize);
//			map.put(entry.getKey(), map1);
//		}
//	}
//
//	@Override
//	public boolean equals(Object o) {
//		if ((o == null) || !(o instanceof LabeledCaseCValueMap)) return false;
//		@SuppressWarnings("unchecked")
//		final LabeledCaseCValueMap<T> lvm = (LabeledCaseCValueMap<T>) o;
//		return map.equals(lvm.map);
//	}
//
//	/**
//	 * @return the minimal value of this map. Null if the map is empty.
//	 */
//	public T getMinValue() {
//		T value = null, v = null;
//		for (final Entry<String, LabeledValueTreeMap<T>> entry : labeledPairSet()) {
//			LabeledValueTreeMap<T> map1 = entry.getValue();
//			if (map1 != null && (v = map1.getMinValue()) != null) {
//				if ((value == null) || (value.compareTo(v) > 0)) value = v;
//			}
//		}
//		return value;
//	}
//
//	/**
//	 * @param l
//	 * @param p
//	 * @return the value associate to the key (label, p) if it exits, null otherwise.
//	 */
//	public T getValue(Label l, String p) {
//		if (l == null || p == null || p.isEmpty()) return null;
//		LabeledValueTreeMap<T> map1 = map.get(p);
//		if (map1 == null) return null;
//		return map1.getValue(l);
//	}
//
//	/**
//	 * Returns the value associated to <code>(l, p)</code> if it exists, otherwise the minimal value among all labels consistent with <code>(l, p)</code>.
//	 * 
//	 * @param l if it is null, null is returned.
//	 * @param p if it is null or empty, null is returned.
//	 * @return the value associated to the <code>(l, p)</code> if it exists or the minimal value among values associated to labels consistent by <code>l</code>.
//	 *         If no labels are subsumed by <code>l</code>, null is returned.
//	 */
//	public T getMinValueConsistentWith(Label l, String p) {
//		if ((l == null) || (p == null) || p.isEmpty()) return null;
//		LabeledValueTreeMap<T> map1 = map.get(p);
//		return map1.getMinValueConsistentWith(l);
//	}
//
//	@Override
//	public int hashCode() {
//		return map.hashCode();
//	}
//
//	/**
//	 * @return the map as a set of (nodeName, LabeledValueTreeMap).
//	 *         Be careful: the LabeledValueTreeMap(s) returned are not a copy but maps inside a such maps!
//	 */
//	public Set<Entry<String, LabeledValueTreeMap<T>>> labeledPairSet() {
//		return map.entrySet();
//	}
//
//	/**
//	 * @return the map as ((label,node),value) triples.
//	 */
//	public Set<Entry<Entry<Label, String>, T>> labeledTripleSet() {
//		Set<Entry<Entry<Label, String>, T>> set = new HashSet<>();
//		for (final Entry<String, LabeledValueTreeMap<T>> entryI : this.labeledPairSet()) {
//			for (final Entry<Label, T> entryI1 : entryI.getValue().entrySet()) {
//				Entry<Label, String> e1 = new SimpleEntry<>(entryI1.getKey(), entryI.getKey());
//				Entry<Entry<Label, String>, T> e2 = new SimpleEntry<>(e1, entryI1.getValue());
//				set.add(e2);
//			}
//		}
//		return set;
//	}
//
//	/**
//	 * Merges a label case value
//	 * <p,l,i>
//	 * .<br>
//	 * The value is insert if there is not a labeled value in the set with label <l,p> or it is present with a value higher than i.<br>
//	 * The method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info is lost.
//	 * 
//	 * @param l
//	 * @param p
//	 * @param i
//	 * @return true if the triple is stored, false otherwise.
//	 */
//	public boolean mergeTriple(Label l, String p, T i) {
//		if ((l == null) || (p == null) || p.isEmpty() || i == null) return false;
//		LabeledValueTreeMap<T> map1 = map.get(p);
//		if (map1 == null) {
//			map1 = new LabeledValueTreeMap<>(this.optimize);
//			map1.putLabeledValue(l, i);
//			map.put(p, map1);
//			return true;
//		}
//		return map1.mergeLabeledValue(l, i);
//	}
//
//	/**
//	 * Wrapper method to {@link #mergeTriple(Label, String, Number)}.
//	 * 'label' parameter is converted to a Label before calling {@link #mergeTriple(Label, String, Number)}.
//	 * 
//	 * @param label
//	 * @param p
//	 * @param i
//	 * @return true if the triple is stored, false otherwise.
//	 */
//	public boolean mergeTriple(String label, String p, T i) {
//		if ((label == null) || (p == null) || p.isEmpty() || i == null) return false;
//		Label l = Label.parse(label);
//		return mergeTriple(l, p, i);
//	}
//
//	
//	
//	
//	/**
//	 * Put the triple <code>(p,l,i)</code> into the map. If the triple is already present, it is overwritten.
//	 * 
//	 * @param l
//	 * @param p
//	 * @param i
//	 * @return the value overwritten.
//	 */
//	public T putTriple(Label l, String p, T i) {
//		if ((l == null) || (p == null) || p.isEmpty() || i == null) return null;
//		LabeledValueTreeMap<T> map1 = map.get(p);
//		if (map1 == null) {
//			map1 = new LabeledValueTreeMap<>(this.optimize);
//			map1.putLabeledValue(l, i);
//			map.put(p, map1);
//			return null;
//		}
//		return map1.putLabeledValue(l, i);
//	}
//
//	/**
//	 * 
//	 */
//	public void clear() {
//		map.clear();
//	}
//
//	/**
//	 * @param l
//	 * @param p
//	 * @return the old value if it exists, null otherwise.
//	 */
//	public T remove(Label l, String p) {
//		if ((l == null) || (p == null) || p.isEmpty()) return null;
//		LabeledValueTreeMap<T> map1 = map.get(p);
//		if (map1 == null) return null;
//		return map1.remove(l);
//	}
//
//	/**
//	 * @return the number of elements of the map.
//	 */
//	public int size() {
//		int n = 0;
//		for (LabeledValueTreeMap<T> map1 : map.values()) {
//			n += map1.size();
//		}
//		return n;
//	}
//
//	@Override
//	public String toString() {
//		return this.toString(false);
//	}
//
//	/**
//	 * @param lower if true, the first element of the pair is rendered as lower case.
//	 * @return a string representing the content of the map. The format is <label, node, value>
//	 */
//	public String toString(boolean lower) {
//		final StringBuffer s = new StringBuffer("{");
//		// tricky
//
//		for (final Entry<String, LabeledValueTreeMap<T>> entry : labeledPairSet()) {
//			for (final Entry<Label, T> entry1 : entry.getValue().entrySet()) {
//				s.append("(");
//				s.append(entry1.getKey());
//				s.append(", ");
//				String s1 = entry.getKey();
//				s1 = (lower) ? s1.toLowerCase() : s1.toUpperCase();
//				s.append(s1);
//				s.append(", ");
//				T value = entry1.getValue();
//				if (value != null) {
//					if (value.equals(Constants.INT_POS_INFINITE)) {
//						s.append("" + Constants.INFINITY_SYMBOL);
//					} else {
//						s.append(value);
//					}
//				}
//				s.append(") ");
//			}
//		}
//		s.append("}");
//		return s.toString();
//	}
//
//}
