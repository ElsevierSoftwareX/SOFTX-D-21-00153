/**
 * 
 */
package it.univr.di.cstnu;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Simple implementation of {@link LabeledValueMapAccess} interface.
 * It does not manage overflow cases when summing edge values.
 * 
 * @author posenato
 * @param <T> type for value field
 * @see LabeledValueMapAccess
 */
public class LabeledValueMap<T extends Number & Comparable<T>> implements LabeledValueMapAccess<T>, Serializable {

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(LabeledValueMap.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 * It is not yet shown that it is correct.
	 * // FIXME 2014/11/04 We discovered that the optimization made by mergeLabeledValue() does not work with some instances of CSTN.
	 */
	private static final boolean Optimize = true;

	/**
	 * A comparator of label to sort the label in size not increasing order.
	 * public static final Comparator<Label> sizeCmp = new Comparator<Label>() {
	 * 
	 * @Override
	 *           public int compare(Label o1, Label o2) {
	 *           if (o1 == null) {
	 *           if (o2 == null) return 0;
	 *           return 1;
	 *           }
	 *           if (o2 == null) return -1;
	 *           return o2.size() - o1.size();
	 *           }
	 *           };
	 */

	/**
	 * Parse a string representing a LabeledValueMap and return an object containing the labeled values represented by
	 * the string.<br>
	 * The format of the string is given by the method {@link #toString()}:<br>
	 * {\[(<key>, <value>) \]*}
	 * 
	 * @param arg
	 * @return a LabeledValueMap object if args represents a valid map, null otherwise.
	 */
	public static LabeledValueMap<Integer> parse(String arg) {
		if (arg == null) {
			LabeledValueMap.LOG.finest("Arg not valid: " + arg);
			return null;
		}
		final String labelCharsRE = "a-zA-Z0-9, \\-" + Constants.NOT + Constants.EMPTY_LABEL;
		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", arg)) {
			LabeledValueMap.LOG.finest("Arg not valid: " + arg);
			return null;
		}

		final LabeledValueMap<Integer> newMap = new LabeledValueMap<>();
		final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
		final Pattern splitterPair = Pattern.compile(", ");

		final String[] entryPair = splitterEntry.split(arg);
		LabeledValueMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
		for (final String s : entryPair) {
			LabeledValueMap.LOG.finest("s: " + s);
			if (s.length() != 0) {
				final String[] labInt = splitterPair.split(s);
				LabeledValueMap.LOG.finest("labInt: " + Arrays.toString(labInt));
				final Label l = Label.parse(labInt[0]);
				final Integer j = Integer.parseInt(labInt[1]);
				newMap.mergeLabeledValue(l, j);
			}
		}
		LabeledValueMap.LOG.finest("newMap: " + newMap);
		return newMap;
	}

	/**
	 * 
	 * @param lvm
	 * @return lvm as new labeled value map minimized.
	 *         static public LabeledValueMap<Integer> getLabeledValueMapOptimized(LabeledValueMap<Integer> lvm) {
	 *         if (lvm == null) return null;
	 *         LabeledValueMap<Integer> newMap = new LabeledValueMap<>();
	 *         for (final Entry<Label, Integer> entry : lvm.labeledValueSet())
	 *         newMap.mergeLabeledValueOptimized(entry.getKey(), entry.getValue());
	 *         return newMap;
	 *         }
	 */

	/**
	 * Labeled value semantics:
	 * <ol>
	 * <li>Each labeled value is a labeled T value.</li>
	 * <li>Each label is a conjunction of literals, i.e., of type {@link Label}.</li>
	 * <li>The set of values of this map is organized as a collection of set each containing labels of the same length.</li>
	 * <li>If a labeled value is put in the map and its label is already present in the map, the labeled value in the map is updated to the minimal 'int' value.
	 * <li>If a labeled value is put in the map and its label subsumes other already present labels in the map, the new value is insert if only it is less than
	 * all other subsumed values.
	 * <li>When a new value is insert, all labeled values having a label that subsumes the new one and a value greater than the new one are removed.
	 * </ol>
	 */
	private TreeMap<Integer, TreeMap<Label, T>> map;

	/**
	 * Label forming a base for the labels of the map.
	 * All literal are straight.
	 */
	private Label base;

	/**
	 * Simple constructor. The internal structure is built and empty.
	 */
	public LabeledValueMap() {
		map = new TreeMap<>();
		base = new Label();
	}

	/**
	 * Constructor to clone the structure.
	 * 
	 * @param lvm the LabeledValueMap to clone. If lvm is null, this will be a empty map.
	 * @param withOptimization if true, it merge all values removing the redundant, otherwise it makes a base copy of the values.
	 */
	LabeledValueMap(LabeledValueMap<T> lvm, boolean withOptimization) {
		this();
		if (lvm == null) return;
		if (withOptimization) {
			for (final Entry<Label, T> entry : lvm.labeledValueSet())
				this.mergeLabeledValue(entry.getKey(), entry.getValue());
		} else {
			for (final Entry<Label, T> entry : lvm.labeledValueSet())
				this.putLabeledValue(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Constructor to clone the structure.
	 * 
	 * @param lvm the LabeledValueMap to clone. If lvm is null, this will be a empty map.
	 */
	public LabeledValueMap(LabeledValueMap<T> lvm) {
		this(lvm, Optimize);
	}

	/**
	 * @param l
	 * @param value
	 * @return true is 'value' is less than any values of the labeled values forming the base. False otherwise.
	 */
	private boolean isAnyBaseLabeledValueLessThan(Label l, T value) {
		if (l == null || base == null || base.isEmpty()) return true;

		TreeMap<Label, T> map1 = map.get(base.size());
		for (Label l1 : Label.allComponentsOfBaseGenerator(base.getAll())) {
			T v1 = map1.get(l1);
			if (v1 == null) {
				LOG.finest("Base non corretta:\nbase=" + base + "\nmap1=" + map1);
				base = new Label();
				return true;
				// throw new IllegalStateException("A base component has a null value. It is not possible.");
			}
			if (l.isConsistentWith(l1) && value.compareTo(v1) >= 0) return false;
		}
		return true;
	}

	/**
	 * @return the first label of this map if it exists, null otherwise;
	 *         public Label getFirstLabel() {
	 *         if (map == null) return null;
	 *         try {
	 *         return map.firstKey();
	 *         }
	 *         catch (final NoSuchElementException e) {
	 *         return null;
	 *         }
	 *         }
	 */

	/**
	 * @return the number of components of the base.
	 *         private int baseSize() {
	 *         if (base.size() == 0) return 0;
	 *         return (int) Math.pow(2, base.size());
	 *         }
	 */

	@Override
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof LabeledValueMap)) return false;
		@SuppressWarnings("unchecked")
		final LabeledValueMap<T> lvm = ((LabeledValueMap<T>) o);
		Map<Label, T> map1 = labeledValueMap();
		return map1.equals(lvm.labeledValueMap());
	}

	/**
	 * @return the minimal value in this map. Null if the map is empty.
	 */
	public T getMinValue() {
		T v = null;
		if (map.values() == null || map.firstEntry() == null || map.firstEntry().getValue() == null || map.firstEntry().getValue().firstEntry() == null
				|| (v = map.firstEntry().getValue().firstEntry().getValue()) == null)//Trick for having v initialized and avoid a comparison in the loop.
			return null;
		for (TreeMap<Label, T> map1 : map.values())
			for (final T i : map1.values())
				if (v.compareTo(i) > 0) v = i;
		return v;
	}

	/**
	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels
	 * consistent with <code>l</code>.
	 * 
	 * @param l If it is null, null is returned.
	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to
	 *         labels consistent with <code>l</code>. If no labels are consistent by <code>l</code>, null is returned.
	 */
	public T getMinValueConsistentWith(Label l) {
		if (l == null) return null;
		T v = getValue(l);
		if (v == null) {
			// the label does not exits, try all consistent labels
			T v1 = null;
			Label l1 = null;
			for (final Entry<Label, T> entry : labeledValueSet()) {
				l1 = entry.getKey();
				if (l.isConsistentWith(l1)) {
					v1 = entry.getValue();
					if (v == null) {
						v = v1;
						continue;
					}
					if (v.compareTo(v1) > 0) v = v1;
				}
			}
		}
		if (v == null)
			LabeledValueMap.LOG.finest("There is no consistent labeled value for the given label " + l.toString());
		return v;
	}

	@Override
	public T getValue(Label l) {
		if (l == null) return null;
		TreeMap<Label, T> map1 = map.get(l.size());
		if (map1 == null) return null;
		return map1.get(l);
	}

	/**
	 * @return true if adding l to this, there is a new base having literals of l.
	 *         private boolean isNewBase(Label l) {
	 *         final Set<Literal> literals = (Set<Literal>) l.getAllStraight();
	 *         literals.addAll(base);
	 *         final Label[] component = Label.allComponentsOfBaseGenerator(literals);
	 *         for (final Label l1 : component)
	 *         if (!l1.equals(l) && (getValue(l1) == null)) return false;
	 *         return true;
	 *         }
	 */
	@Override
	public int hashCode() {
		return map.hashCode();
	}

	/**
	 * Tries to add all given labeled values into the current map.<br>
	 * Recursive procedure.<br>
	 * Ad each round it tries to add labeled values of the same size.
	 * 
	 * @param inputMap
	 * @return true if any element of inputMap has been inserted into the map.
	 */
	private boolean insertAndSimplify(TreeMap<Label, T> inputMap) {
		if (inputMap == null || inputMap.size() == 0) return false;

		boolean inserted = false;
		// All entries of inputMap should have label of same size.
		int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
		TreeMap<Label, T> currentMapLimitedToLabelOfNSize = map.get(n);// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
		TreeMap<Label, T> toAdd = new TreeMap<>();
		// toRemove = new TreeMap<>(),

		if (currentMapLimitedToLabelOfNSize != null) {
			for (Iterator<Entry<Label, T>> ite = inputMap.entrySet().iterator(); ite.hasNext();) {
				Entry<Label, T> inputEntry = ite.next();
				boolean iteRemoved = false;
				Label newLabel = inputEntry.getKey();
				T newValue = inputEntry.getValue();
				// check is there is any labeled value with same value and only one opposite literal
				for (Entry<Label, T> entry : currentMapLimitedToLabelOfNSize.entrySet()) {
					Label l1 = new Label(entry.getKey());
					T v1 = entry.getValue();
					Literal lit = null;
					if (newValue.compareTo(v1) == 0 && ((lit = l1.getUniqueDifferentLiteral(newLabel)) != null)) {
						// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
						// toRemove.put(newLabel, newValue);
						// toRemove.put(l1, v1);
						if (!iteRemoved) {
							ite.remove();
							iteRemoved = true;
						}
						l1.remove(lit, true);
						if (l1.size() < 0) throw new IllegalStateException("Il calcolo della base della ricorsione è errato!");
						toAdd.put(l1, v1);
					}
				}
			}
		}
		// inputMap contains all the elements that have to be insert
		for (Entry<Label, T> entry : inputMap.entrySet()) {
			Label l1 = new Label(entry.getKey());
			T v1 = entry.getValue();
			removeAllValuesGreaterThan(l1, v1);
			if (!isAnyBaseLabeledValueLessThan(l1, v1))
				continue;
			putLabeledValue(l1, v1);
			inserted = true;
			if (makeABetterBase(l1, v1)) {
				removeAllValuesGreaterThanBase();
			}
		}
		if (toAdd.size() > 0) {
			inserted = insertAndSimplify(toAdd) || inserted;
		}
		return inserted;
	}

	/**
	 * @return true if this map contains a negative value at least.
	 */
	public boolean isThereNegativeValues() {
		@SuppressWarnings("unchecked")
		T z = (T) new Integer(0);

		for (final T i : labeledValueMap().values())
			if (i.compareTo(z) < 0) return true;
		return false;
	}

	@Override
	public Map<Label, T> labeledValueMap() {
		TreeMap<Label, T> map1 = new TreeMap<>();
		for (TreeMap<Label, T> mapI : map.values()) {
			map1.putAll(mapI);
		}
		return map1;
	}

	@Override
	public Set<Entry<Label, T>> labeledValueSet() {
		Map<Label, T> map1 = labeledValueMap();
		Set<Entry<Label, T>> set = map1.entrySet();
		return set;
	}

	/**
	 * @param label
	 * @param value
	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
	 */
	private boolean makeABetterBase(Label label, T value) {
		if (label == null || value == null) return false;

		int n = label.size();

		if (n == 0) {
			// it is empty!
			base.clear();
			return true;
		}
		TreeMap<Label, T> map1 = map.get(n);
		if (map1.size() < Math.pow(2.0, n)) {
			// there are no sufficient elements!
			return false;
		}
		Collection<Literal> baseCandidateColl = label.getAllStraight();
		for (Label l1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
			if (map1.get(l1) == null) return false;
		}
		Label baseCandidate = new Label();
		for (Literal l : baseCandidateColl)
			baseCandidate.conjunct(l);
		base = baseCandidate;
		return true;
	}

	/**
	 * Adds the pair &langle;l,i&rangle;.<br>
	 * Moreover, tries to eliminate all labels that are redundant.
	 * 
	 * @param newLabel
	 * @param newValue
	 * @param withOptimization if true, the redundant labels are removed from the set.
	 * @return true if the pair is stored, false otherwise.
	 */
	private boolean mergeLabeledValue(Label newLabel, T newValue, boolean withOptimization) {
		// First version: very redundant but simple to check!
		if ((newLabel == null) || (newValue == null)) return false;

		// TreeMap<Label, Integer> oldMap = new TreeMap<>(map);
		Label l1;
		T v1;

		/*
		 * 1.Check if (l,i) has to be insert.
		 * If (l,i) is greater than any subsumed value, return
		 * otherwise, remove all possible subsuming value that are greater than i.
		 */
		boolean removed = false;
		for (Map<Label, T> map1 : map.values()) {
			for (Iterator<Entry<Label, T>> ite = map1.entrySet().iterator(); ite.hasNext();) {
				Entry<Label, T> entry = ite.next();
				l1 = entry.getKey();
				v1 = entry.getValue();
				if (newLabel.subsumes(l1) && (newValue.compareTo(v1) >= 0))
					return false;
				if (l1.subsumes(newLabel) && newValue.compareTo(v1) < 0) {
					ite.remove();
					removed = true;
				}
			}
		}

		if (removed) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.
			final LabeledValueMap<T> newMap = new LabeledValueMap<>(this);
			map = newMap.map;
			base = newMap.base;
		}
		/*
		 * 2. Insert the value and check if it possible to simplify with some other label with same value and only one different literals.
		 */
		if (withOptimization) {
			TreeMap<Label, T> a = new TreeMap<>();
			a.put(newLabel, newValue);
			return insertAndSimplify(a);
		}
		putLabeledValue(newLabel, newValue);
		return true;
	}

	/**
	 * Adds the pair &langle;l,i&rangle;.<br>
	 * Moreover, tries to eliminate all labels that are redundant.
	 * 
	 * @param newLabel
	 * @param newValue
	 * @return true if the pair is stored, false otherwise.
	 */
	@Override
	public boolean mergeLabeledValue(Label newLabel, T newValue) {
		return this.mergeLabeledValue(newLabel, newValue, Optimize);
	}

	/**
	 * Merge all the labeled value of map into this.
	 * 
	 * @see LabeledValueMap#mergeLabeledValue(Label, T)
	 * @param map1
	 */
	@SuppressWarnings("javadoc")
	public void mergeLabeledValue(LabeledValueMap<T> map1) {
		if (map1 == null) return;
		for (final Entry<Label, T> entry : map1.labeledValueSet()) {
			final Label l = entry.getKey();
			final T value = entry.getValue();
			this.mergeLabeledValue(l, value);
		}
	}

	@Override
	public T putLabeledValue(Label l, T i) {
		if (l == null) return null;
		TreeMap<Label, T> map1 = map.get(l.size());
		if (map1 == null) {
			map1 = new TreeMap<>();
			map1.put(new Label(l), i);
			map.put(l.size(), map1);
			return null;
		}
		return map1.put(new Label(l), i);
	}

	/**
	 * Remove all labeled values that subsume l and have values greater than i.
	 * The labeled value (l,i) is also removed!
	 * 
	 * @param l
	 * @param i
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThan(Label l, T i) {
		if (l == null || i == null) return false;

		int n = l.size();
		boolean removed = false;
		for (Map<Label, T> map1 : map.tailMap(n, true).values()) {
			for (Iterator<Entry<Label, T>> ite = map1.entrySet().iterator(); ite.hasNext();) {
				Entry<Label, T> entry = ite.next();
				Label l1 = entry.getKey();
				T value = entry.getValue();
				if (l1.subsumes(l) && (value.compareTo(i) >= 0)) {
					ite.remove();
					removed = true;
				}
			}
		}
		if (removed) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.

			final LabeledValueMap<T> newMap = new LabeledValueMap<>(this);
			map = newMap.map;
			base = newMap.base;
		}
		return removed;
	}

	/**
	 * Remove all labeled values having value greater than all values of the base component.
	 * 
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThanBase() {
		if (base == null || base.size() == 0) return false;
		final LabeledValueMap<T> newMap = new LabeledValueMap<>();
		// build the list of labeled values that form the base
		final ArrayList<SimpleEntry<Label, T>> baseComponent = new ArrayList<>();
		for (final Label l : Label.allComponentsOfBaseGenerator(base.getAllStraight())) {
			baseComponent.add(new SimpleEntry<>(l, getValue(l)));
		}
		Label l1, lb;
		T v1, vb;
		boolean toInsert = true;
		for (final Entry<Label, T> entry : labeledValueSet()) {
			l1 = entry.getKey();
			v1 = entry.getValue();
			toInsert = false;
			for (final Entry<Label, T> baseEntry : baseComponent) {
				lb = baseEntry.getKey();
				vb = baseEntry.getValue();
				if (l1.equals(lb)) {
					toInsert = true; // a base component has to be always insert!
					break;
				}
				// if (l1 == null || lb == null || v1 == null || vb == null) {
				// System.out.println("Trovato");
				// }
				if (l1.isConsistentWith(lb) && (v1.compareTo(vb) < 0)) {
					toInsert = true;
					break;
				}
			}
			if (toInsert)
				newMap.putLabeledValue(l1, v1);
		}
		if (!newMap.equals(map)) {
			map = newMap.map;
			return true;
		}
		return false;
	}

	@Override
	public T remove(Label l) {
		return remove(l, Optimize);
	}

	/**
	 * Removes value with label l.
	 * 
	 * @param l
	 * @param withOptimization if true, it tries also to remove all redundant labels that can be
	 * @return the old value associate to l if it existed before the deletion, null otherwise.
	 */
	private T remove(Label l, boolean withOptimization) {
		if (l == null) return null;

		TreeMap<Label, T> map1 = map.get(l.size());
		if (map1 == null) return null;
		final T oldValue = map1.remove(l);
		if (oldValue != null && withOptimization) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.

			final LabeledValueMap<T> newMap = new LabeledValueMap<>(this);
			map = newMap.map;
			base = newMap.base;
		}
		return oldValue;
	}

	@Override
	public void clear() {
		map.clear();
		base.clear();
	}

	@Override
	public int size() {
		int n = 0;
		for (Map<Label, T> map1 : map.values()) {
			n += map1.size();
		}
		return n;
	}

	/**
	 * Sums the labeled values of inputMap to the values of this map and returns the result as a new labeled value map.<br>
	 * This map is not modified.
	 * 
	 * @param inputMap
	 * @return Sums the labeled values of inputMap to the values of this map and returns the result as a new labeled
	 *         value map. If the labels of the two maps are all not consistent or the input map is null, then an empty map is returned.
	 */
	public LabeledValueMap<Integer> summedTo(LabeledValueMap<Integer> inputMap) {
		final LabeledValueMap<Integer> resultMap = new LabeledValueMap<>();
		if (inputMap == null) return resultMap;

		Integer l1Value, l2Value;
		Label newLabel, l1, l2;
		for (final Entry<Label, T> currentEntry : this.labeledValueSet())
			for (final Entry<Label, Integer> inputEntry : inputMap.labeledValueSet()) {
				l1 = currentEntry.getKey();
				l1Value = (Integer) currentEntry.getValue();
				l2 = inputEntry.getKey();
				l2Value = inputEntry.getValue();

				newLabel = l1.conjunction(l2);
				if (newLabel != null) {
					resultMap.mergeLabeledValue(newLabel, l1Value + l2Value);
				}
			}
		return resultMap;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("{");
		for (final Entry<Label, T> e : labeledValueSet()) {
			sb.append("(");
			sb.append(e.getKey().toString());
			sb.append(", ");
			T value = e.getValue();
			if (value != null) {
				if (value.equals(Constants.INFINITY_VALUE)) {
					sb.append("" + Constants.INFINITY);
				} else {
					sb.append(value);
				}
			}
			sb.append(") ");
		}
		sb.append("}");
		return sb.toString();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		LabeledValueMap<Integer> map = new LabeledValueMap<>();
//
//		map.mergeLabeledValue(Label.parse("¬q"), -1);
//		map.mergeLabeledValue(Label.parse("q"), -12);
//		map.mergeLabeledValue(Label.parse("p"), -10);
//
//		// map.remove(Label.parse("p"));
//
//		map.mergeLabeledValue(Label.emptyLabel, -10);
//
//		System.out.println("Map: " + map);
	}

}
