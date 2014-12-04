//package it.univr.di.attic;
//
//import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
//import it.unimi.dsi.fastutil.objects.AbstractObject2ObjectMap;
//import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
//import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.Literal;
//
//import java.io.Serializable;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Simple implementation of {@link LabeledValueMap} interface.
// * <p>
// * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
// * <p>
// * It does not manage overflow cases when summing edge values.
// * <p>
// * 2014-11-23: I decided to switch to LabeledIntTreeMapNew because it is more common to have CSTN(U) with integer weights. An int typed map can speed up the
// * execution time of the implementation.
// * <p>
// * 
// * @author Roberto Posenato
// * @param <T> type for value field
// * @see LabeledValueMap
// */
//public class LabeledValueTreeMap<T extends Number & Comparable<T>> implements LabeledValueMap<T>, Serializable {
//
//	/**
//	 * logger
//	 */
//	static private Logger LOG = Logger.getLogger(LabeledValueTreeMap.class.getName());
//
//	/**
//	 *
//	 */
//	private static final long serialVersionUID = 1L;
//
//	/**
//	 * @param args
//	 */
//	public static void main(final String[] args) {
//		final int nTest = 1000;
//		final double msNorm = 1.0 / (1000000.0 * nTest);
//
//		LabeledValueMap<Integer> map = new LabeledValueTreeMap<>(true);
//
//		Label l2 = Label.parse("abdfghji");
//		Label l3 = Label.parse("adji");
//		Label l4 = Label.parse("¬bdfghji");
//		Label l5 = Label.parse("b¬dfji");
//		Label l6 = Label.parse("a¬bdfi");
//		Label l7 = Label.parse("abdfg¬hji");
//		Label l8 = Label.parse("aews¬fg");
//		Label l9 = Label.parse("¬arffss");
//		Label l11 = Label.parse("abdfghi");
//		Label l12 = Label.parse("adj¬fi");
//		Label l13 = Label.parse("¬bdfghji");
//		Label l14 = Label.parse("bdfjigde");
//		Label l15 = Label.parse("a¬bdfg¬ihj");
//		Label l16 = Label.parse("abd");
//		
//		long startTime = System.nanoTime();
//		for (int i = 0; i < nTest; i++) {
//			map.clear();
//			map.mergeLabeledValue(Label.emptyLabel, 109);
//			map.mergeLabeledValue(l2, 10);
//			map.mergeLabeledValue(l3, 25);
//			map.mergeLabeledValue(l4, 23);
//			map.mergeLabeledValue(l5, 22);
//			map.mergeLabeledValue(l6, 23);
//			map.mergeLabeledValue(l7, 20);
//			map.mergeLabeledValue(l8, 20);
//			map.mergeLabeledValue(l9, 21);
//			map.mergeLabeledValue(Label.emptyLabel, 100);
//			map.mergeLabeledValue(l11, 11);
//			map.mergeLabeledValue(l12, 24);
//			map.mergeLabeledValue(l13, 23);
//			map.mergeLabeledValue(l14, 22);
//			map.mergeLabeledValue(l15, 23);
//			map.mergeLabeledValue(l16, 20);
//		}
//		long endTime = System.nanoTime();
//		System.out.println("Execution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map + ".\nTime: (ms): "
//				+ (endTime - startTime) * msNorm);
//	}
//
//	/**
//	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by
//	 * the string.<br>
//	 * The format of the string is given by the method {@link #toString()}:<br>
//	 * {\[(<key>, <value>) \]*}
//	 *
//	 * @param arg
//	 * @param withOptimization true if the redundant labeled values have to be removed.
//	 * @return a LabeledValueTreeMap object if arg represents a valid map, null otherwise.
//	 */
//	public static LabeledValueTreeMap<Integer> parse(final String arg, boolean withOptimization) {
//		if (arg == null) {
//			LabeledValueTreeMap.LOG.finest("Arg not valid: " + arg);
//			return null;
//		}
//		final String labelCharsRE = "a-zA-Z0-9, \\-" + Constants.NOT + Constants.EMPTY_LABEL;
//		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", arg)) {
//			LabeledValueTreeMap.LOG.finest("Arg not valid: " + arg);
//			return null;
//		}
//
//		final LabeledValueTreeMap<Integer> newMap = new LabeledValueTreeMap<>(withOptimization);
//		final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
//		final Pattern splitterPair = Pattern.compile(", ");
//
//		final String[] entryPair = splitterEntry.split(arg);
//		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
//		for (final String s : entryPair) {
//			LabeledValueTreeMap.LOG.finest("s: " + s);
//			if (s.length() != 0) {
//				final String[] labInt = splitterPair.split(s);
//				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
//				final Label l = Label.parse(labInt[0]);
//				final Integer j = Integer.parseInt(labInt[1]);
//				newMap.mergeLabeledValue(l, j);
//			}
//		}
//		LabeledValueTreeMap.LOG.finest("newMap: " + newMap);
//		return newMap;
//	}
//
//	/**
//	 *
//	 * @param lvm
//	 * @return lvm as new labeled value map minimized.
//	 *         static public LabeledValueTreeMap<Integer> getLabeledValueMapOptimized(LabeledValueTreeMap<Integer> lvm) {
//	 *         if (lvm == null) return null;
//	 *         LabeledValueTreeMap<Integer> newMap = new LabeledValueTreeMap<>();
//	 *         for (final Entry<Label, Integer> entry : lvm.labeledValueSet())
//	 *         newMap.mergeLabeledValueOptimized(entry.getKey(), entry.getValue());
//	 *         return newMap;
//	 *         }
//	 */
//
//	/**
//	 * Label forming a base for the labels of the map.
//	 * All literal are straight.
//	 */
//	private Label base;
//
//	/**
//	 * Design specification: The set of labeled values of this map is organized as a collection of sets each containing labels of the same length.
//	 * This allows the minimization task to be performed in a more systematic and, possibly, efficient way.
//	 */
//	private Int2ObjectArrayMap<Object2ObjectRBTreeMap<Label, T>> mainInt2SetMap;
//
//	/**
//	 * To activate all optimization code in order to remove the redundant label in the set.
//	 */
//	private boolean optimize;
//
//	/**
//	 * Simple constructor.
//	 * The internal structure is built and empty.
//	 * 
//	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
//	 */
//	public LabeledValueTreeMap(boolean withOptimization) {
//		this.mainInt2SetMap = new Int2ObjectArrayMap<>();
//		this.base = new Label();
//		this.optimize = withOptimization;
//	}
//
//	/**
//	 * Constructor to clone the structure.
//	 *
//	 * @param lvm the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
//	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
//	 */
//	public LabeledValueTreeMap(final LabeledValueTreeMap<T> lvm, final boolean withOptimization) {
//		this(withOptimization);
//		if (lvm == null) return;
//		if (withOptimization) {
//			for (final Entry<Label,T> entry : lvm.entrySet()) {
//				this.mergeLabeledValue(entry.getKey(), entry.getValue());
//			}
//		} else {
//			for (final Entry<Label,T> entry : lvm.entrySet()) {
//				this.putLabeledValue(entry.getKey(), entry.getValue());
//			}
//		}
//	}
//
//	@Override
//	public void clear() {
//		this.mainInt2SetMap.clear();
//		this.base.clear();
//	}
//
//	@Override
//	public boolean equals(final Object o) {
//		if ((o == null) || !(o instanceof LabeledValueTreeMap<?>)) return false;
//		@SuppressWarnings("unchecked")
//		final LabeledValueTreeMap<T> lvm = ((LabeledValueTreeMap<T>) o);
//		if (this.size() != lvm.size()) return false;
//		return this.entrySet().equals(lvm.entrySet());// Two maps are equals if they contain the same set of values. The internal representation of
//														// optimization is not important!.
//	}
//
//	/**
//	 * @return the minimal value in this map. Null if the map is empty.
//	 */
//	public T getMinValue() {
//		final Integer M = new Integer(Integer.MAX_VALUE);
//		@SuppressWarnings("unchecked")
//		T v = ((T) M);
//		for (final Object2ObjectSortedMap<Label, T> map1 : this.mainInt2SetMap.values()) {
//			for (final T i : map1.values())
//				if (v.compareTo(i) > 0) {
//					v = i;
//				}
//		}
//		if (v == M) return null;// even if there is a MAX_VALUE labeled value, it cannot be the same of the guard!
//		return v;
//	}
//
//	/**
//	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels
//	 * consistent with <code>l</code>.
//	 *
//	 * @param l If it is null, null is returned.
//	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to
//	 *         labels consistent with <code>l</code>. If no labels are consistent by <code>l</code>, null is returned.
//	 */
//	@SuppressWarnings("unchecked")
//	public T getMinValueConsistentWith(final Label l) {
//		if (l == null) return null;
//		final Integer M = new Integer(Integer.MAX_VALUE);
//		T v = this.getValue(l);
//		if (v == null) {
//			// the label does not exits, try all consistent labels
//			v = ((T) M);
//			T v1 = null;
//			Label l1 = null;
//			for (final Entry<Label,T> entry : this.entrySet()) {
//				l1 = entry.getKey();
//				if (l.isConsistentWith(l1)) {
//					v1 = entry.getValue();
//					if (v.compareTo(v1) > 0) {
//						v = v1;
//					}
//				}
//			}
//		}
//		if (v == null || v == M) {// even if there is a MAX_VALUE labeled value, it cannot be the same of the guard!
//			LabeledValueTreeMap.LOG.finest("There is no consistent labeled value for the given label " + l.toString());
//			return null;
//		}
//		return v;
//	}
//
//	@Override
//	public T getValue(final Label l) {
//		if (l == null) return null;
//		final Object2ObjectSortedMap<Label, T> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return null;
//		return map1.get(l);
//	}
//
//	@Override
//	public int hashCode() {
//		return this.mainInt2SetMap.hashCode();
//	}
//
//	/**
//	 * @return true if this map contains a negative value at least.
//	 */
//	public boolean isThereNegativeValues() {
//		@SuppressWarnings("unchecked")
//		final T z = (T) new Integer(0);
//
//		for (final T i : this.values())
//			if (i.compareTo(z) < 0) return true;
//		return false;
//	}
//
//	@Override
//	public Set<Entry<Label, T>> entrySet() {
//		Set<Entry<Label, T>> coll = new ObjectRBTreeSet<>();
//		for (final Object2ObjectSortedMap<Label, T> mapI : this.mainInt2SetMap.values()) {
//			for (Entry<Label, T> e : mapI.object2ObjectEntrySet()) {
//				coll.add(e);
//			}
//		}
//		return coll;
//	}
//
//	/**
//	 * Adds the pair &langle;l,i&rangle;.<br>
//	 * Moreover, tries to eliminate all labels that are redundant.
//	 *
//	 * @param newLabel
//	 * @param newValue
//	 * @return true if the pair is stored, false otherwise.
//	 */
//	@Override
//	public boolean mergeLabeledValue(final Label newLabel, final T newValue) {
//		if ((newLabel == null) || (newValue == null)) return false;
//
//		// First version: very redundant but simple to check!
//		// Object2ObjectRBTreeMap<Label, Integer> oldMap = new TreeMap<>(map);
//		Label l1;
//		T v1;
//
//		int newValueSize = newLabel.size(), l1Size = 0;
//		for (final Object2ObjectRBTreeMap<Label, T> mapAllSameLenghLabels : this.mainInt2SetMap.values()) {
//			for (final ObjectIterator<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, T>> ite = mapAllSameLenghLabels.object2ObjectEntrySet()
//					.iterator(); ite.hasNext();) {
//				final it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, T> entry = ite.next();
//				l1 = entry.getKey();
//				v1 = entry.getValue();
//				if (newValueSize >= (l1Size = l1.size()) && newLabel.subsumes(l1) && (newValue.compareTo(v1) >= 0))
//					return false;
//				if (newValueSize <= l1Size && l1.subsumes(newLabel) && (newValue.compareTo(v1) < 0)) {
//					ite.remove();
//					if (this.optimize) checkValidityOfTheBaseAfterRemoving(l1);
//				}
//			}
//		}
//
//		/*
//		 * 2. Insert the value and check if it possible to simplify with some other label with same value and only one different literals.
//		 */
//		if (this.optimize) {
//			final Object2ObjectRBTreeMap<Label, T> a = new Object2ObjectRBTreeMap<>();
//			a.put(newLabel, newValue);
//			return this.insertAndSimplify(a);
//		}
//
//		this.putLabeledValue(newLabel, newValue);
//		return true;
//	}
//
//	/**
//	 * Merge all the labeled value of map into this.
//	 *
//	 * @see LabeledValueTreeMap#mergeLabeledValue(Label, T)
//	 * @param map1
//	 */
//	@SuppressWarnings("javadoc")
//	public void mergeLabeledValue(final LabeledValueTreeMap<T> map1) {
//		if (map1 == null) return;
//		for (final Entry<Label,T> entry : map1.entrySet()) {
//			final Label l = entry.getKey();
//			final T value = entry.getValue();
//			this.mergeLabeledValue(l, value);
//		}
//	}
//
//	@Override
//	public T putLabeledValue(final Label l, final T i) {
//		if (l == null) return null;
//		Object2ObjectRBTreeMap<Label, T> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) {
//			map1 = new Object2ObjectRBTreeMap<>();
//			map1.put(new Label(l), i);
//			this.mainInt2SetMap.put(l.size(), map1);
//			return null;
//		}
//		T old = map1.put(new Label(l), i);
//		return old;
//	}
//
//	@Override
//	public T remove(final Label l) {
//		if (l == null) return null;
//
//		final Object2ObjectRBTreeMap<Label, T> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return null;
//		final T oldValue = map1.remove(l);
//		if ((oldValue != null) && this.optimize) {
//			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
//			// base.
//
//			final LabeledValueTreeMap<T> newMap = new LabeledValueTreeMap<>(this, this.optimize);
//			this.mainInt2SetMap = newMap.mainInt2SetMap;
//			this.base = newMap.base;
//		}
//		return oldValue;
//	}
//
//	@Override
//	public int size() {
//		int n = 0;
//		for (final Map<Label, T> map1 : this.mainInt2SetMap.values()) {
//			n += map1.size();
//		}
//		return n;
//	}
//
//	/**
//	 * Sums the labeled values of inputMap to the values of this map and returns the result as a new labeled value map.<br>
//	 * This map is not modified.
//	 *
//	 * @param inputMap
//	 * @return Sums the labeled values of inputMap to the values of this map and returns the result as a new labeled
//	 *         value map. If the labels of the two maps are all not consistent or the input map is null, then an empty map is returned.
//	 */
//	public LabeledValueTreeMap<Integer> summedTo(final LabeledValueTreeMap<Integer> inputMap) {
//		final LabeledValueTreeMap<Integer> resultMap = new LabeledValueTreeMap<>(optimize);
//		if (inputMap == null) return resultMap;
//
//		Integer l1Value, l2Value;
//		Label newLabel, l1, l2;
//		for (final Entry<Label,T> currentEntry : this.entrySet()) {
//			for (final Entry<Label, Integer> inputEntry : inputMap.entrySet()) {
//				l1 = currentEntry.getKey();
//				l1Value = (Integer) currentEntry.getValue();
//				l2 = inputEntry.getKey();
//				l2Value = inputEntry.getValue();
//
//				newLabel = l1.conjunctionExtended(l2);
//				if (newLabel != null) {
//					resultMap.mergeLabeledValue(newLabel, l1Value + l2Value);
//				}
//			}
//		}
//		return resultMap;
//	}
//
//	@Override
//	public String toString() {
//		final StringBuffer sb = new StringBuffer("{");
//		for (final Entry<Label,T> e : this.entrySet()) {
//			sb.append("(");
//			sb.append(e.getKey().toString());
//			sb.append(", ");
//			final T value = e.getValue();
//			if (value != null) {
//				if (value.equals(Constants.INT_POS_INFINITE)) {
//					sb.append("" + Constants.INFINITY_SYMBOL);
//				} else {
//					sb.append(value);
//				}
//			}
//			sb.append(") ");
//		}
//		sb.append("}");
//		return sb.toString();
//	}
//
//	@Override
//	public Set<T> values() {
//		ObjectArraySet<T> coll = new ObjectArraySet<>(size());
//		for (final Object2ObjectSortedMap<Label, T> mapI : this.mainInt2SetMap.values()) {
//			coll.addAll(mapI.values());
//		}
//		return coll;
//	}
//
//	/**
//	 * Tries to add all given labeled values into the current map.<br>
//	 * Recursive procedure:
//	 * 0. Given a set of labeled values to insert (all label have the same lenght)
//	 * 1. For each of them, compares all same-length labels already in the map with the current one looking for if there is one with same value and only one
//	 * opposite literal
//	 * (this allows the simplification of the two labels with a shorter one). In case of a positive search, shorten the current label to insert.
//	 * 2. For each of the labeled values to insert (possibly updated), removes all labeled values in the map greater than it.
//	 * Ad each round it tries to add labeled values of the same size.
//	 *
//	 * @param inputMap contains all the elements that have to be inserted.
//	 * @return true if any element of inputMap has been inserted into the map.
//	 */
//	private boolean insertAndSimplify(final Object2ObjectRBTreeMap<Label, T> inputMap) {
//		if ((inputMap == null) || (inputMap.size() == 0)) return false;// recursion basement!
//
//		boolean inserted = false;
//		// All entries of inputMap should have label of same size.
//		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
//		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
//		final Object2ObjectRBTreeMap<Label, T> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(n);
//		final Object2ObjectRBTreeMap<Label, T> toAdd = new Object2ObjectRBTreeMap<>();
//		// toRemove = new TreeMap<>(),
//
//		if (currentMapLimitedToLabelOfNSize != null) {
//			for (final ObjectIterator<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, T>> ite = inputMap.object2ObjectEntrySet().iterator(); ite
//					.hasNext();) {
//				final it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, T> inputEntry = ite.next();
//				boolean iteRemoved = false;
//				final Label newLabel = inputEntry.getKey();
//				final T newValue = inputEntry.getValue();
//				// check is there is any labeled value with same value and only one opposite literal
//				for (final Entry<Label, T> entry : currentMapLimitedToLabelOfNSize.object2ObjectEntrySet()) {
//					final Label l1 = new Label(entry.getKey());
//					final T v1 = entry.getValue();
//					Literal lit = null;
//					if ((newValue.compareTo(v1) == 0) && ((lit = l1.getUniqueDifferentLiteral(newLabel)) != null)) {
//						// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
//						// toRemove.put(newLabel, newValue);
//						// toRemove.put(l1, v1);
//						if (!iteRemoved) {
//							ite.remove();
//							iteRemoved = true;
//						}
//						l1.remove(lit, true);
//						if (l1.size() < 0) throw new IllegalStateException("There is no literal to remove, there is a problem in the code!");
//						toAdd.put(l1, v1);
//					}
//				}
//			}
//		}
//		// inputMap has been updated. Now it contains all the elements that have to be insert.
//		for (final Entry<Label, T> entry : inputMap.object2ObjectEntrySet()) {
//			final Label l1 = new Label(entry.getKey());
//			final T v1 = entry.getValue();
//			this.removeAllValuesGreaterThan(l1, v1);
//			if (this.isBaseAbleToRepresent(l1, v1)) continue;
//			this.putLabeledValue(l1, v1);
//			inserted = true;
//			if (this.makeABetterBase(l1, v1)) {
//				this.removeAllValuesGreaterThanBase();
//			}
//		}
//		if (toAdd.size() > 0) {
//			inserted = this.insertAndSimplify(toAdd) || inserted;
//		}
//		return inserted;
//	}
//
//	/**
//	 * Determines whether the value can be represented by any component of the base.
//	 * A component of the base can represent the labeled value <code>(l,v)</code> if <code>l</code> subsumes the component label
//	 * and the <code>v</code> is greater or equal the component value.
//	 *
//	 * @param l
//	 * @param v
//	 * @return true if <code>v</code> is greater or equal than a base component value that is subsumed by <code>l</code>. False otherwise.
//	 */
//	private boolean isBaseAbleToRepresent(final Label l, final T v) {
//		if ((l == null) || (this.base == null) || this.base.isEmpty()) return false;
//
//		final Object2ObjectRBTreeMap<Label, T> map1 = this.mainInt2SetMap.get(this.base.size());
//		for (final Label l1 : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
//			final T v1 = map1.get(l1);
//			if (v1 == null) {
//				LabeledValueTreeMap.LOG.finest("The base is not sound: base=" + this.base + ". Map1=" + map1);
//				this.base = new Label();
//				return false;
//				// throw new IllegalStateException("A base component has a null value. It is not possible.");
//			}
//			if (l.isConsistentWith(l1) && (v.compareTo(v1) >= 0)) return true;
//		}
//		return false;
//	}
//
//	/**
//	 * @param label
//	 * @param v
//	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
//	 */
//	private boolean makeABetterBase(final Label label, final T v) {
//		if ((label == null) || (v == null)) return false;
//
//		final int n = label.size();
//
//		if (n == 0) {
//			// The new labeled value (l,v) has universal label, the base is not more necessary!
//			this.base.clear();
//			return true;
//		}
//		final Object2ObjectRBTreeMap<Label, T> map1 = this.mainInt2SetMap.get(n);
//		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
//			return false;
//		final Literal[] baseCandidateColl = label.getAllStraight();
//		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
//			if (map1.get(label1) == null) return false;
//		}
//		final Label baseCandidate = new Label();
//		for (final Literal l : baseCandidateColl) {
//			baseCandidate.conjunct(l);
//		}
//		this.base = baseCandidate;
//		return true;
//	}
//
//	/**
//	 * If the removed label <code>l</code> is a component of the base, then reset the base.
//	 * 
//	 * @param l
//	 * @return true if <code>l</code> is a component of the base, false otherwise.
//	 */
//	private boolean checkValidityOfTheBaseAfterRemoving(final Label l) {
//		int bn, ln;
//		if (l == null || (ln = l.size()) == 0 || (bn = base.size()) == 0 || ln != bn) return false;
//
//		if (l.conjunctionExtended(base).size() > ln) return false;
//		base.clear();
//		return true;
//	}
//
//	/**
//	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
//	 * 
//	 * @param l
//	 * @param i
//	 * @return true if one element at least has been removed, false otherwise.
//	 */
//	private boolean removeAllValuesGreaterThan(final Label l, final T i) {
//		if ((l == null) || (i == null)) return false;
//
//		final int n = l.size();
//		boolean removed = false;
//		for (int j : this.mainInt2SetMap.keySet()) {
//			if (j < n) continue;
//			for (final ObjectIterator<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, T>> currentMapIte = this.mainInt2SetMap.get(j)
//					.object2ObjectEntrySet().iterator(); currentMapIte.hasNext();) {
//				final Entry<Label, T> entry = currentMapIte.next();
//				final Label l1 = entry.getKey();
//				final T value = entry.getValue();
//				if (l1.subsumes(l) && (value.compareTo(i) >= 0)) {
//					currentMapIte.remove();
//					checkValidityOfTheBaseAfterRemoving(l1);
//					removed = true;
//				}
//			}
//		}
//		return removed;
//	}
//
//	/**
//	 * Remove all labeled values having each value greater than all values of base components consistent with it.
//	 *
//	 * @return true if one element at least has been removed, false otherwise.
//	 */
//	private boolean removeAllValuesGreaterThanBase() {
//		if ((this.base == null) || (this.base.size() == 0)) return false;
//		final LabeledValueTreeMap<T> newMap = new LabeledValueTreeMap<>(optimize);
//		// build the list of labeled values that form the base
//		final ObjectArraySet<Entry<Label, T>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
//		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllStraight())) {
//			baseComponent.add(new AbstractObject2ObjectMap.BasicEntry<>(l, this.getValue(l)));
//		}
//		Label l1, lb;
//		T v1, vb;
//		boolean toInsert = true;
//		for (final java.util.Map.Entry<Label, T> entry : this.entrySet()) {
//			l1 = entry.getKey();
//			v1 = entry.getValue();
//			toInsert = false;
//			for (final Entry<Label, T> baseEntry : baseComponent) {
//				lb = baseEntry.getKey();
//				vb = baseEntry.getValue();
//				if (l1.equals(lb)) {
//					toInsert = true; // a base component has to be always insert!
//					break;
//				}
//				// if (l1 == null || lb == null || v1 == null || vb == null) {
//				// System.out.println("Trovato");
//				// }
//				if (l1.isConsistentWith(lb) && (v1.compareTo(vb) < 0)) {
//					toInsert = true;
//					break;
//				}
//			}
//			if (toInsert) {
//				newMap.putLabeledValue(l1, v1);
//			}
//		}
//		if (!newMap.equals(this.mainInt2SetMap)) {
//			this.mainInt2SetMap = newMap.mainInt2SetMap;
//			return true;
//		}
//		return false;
//	}
//
//}
