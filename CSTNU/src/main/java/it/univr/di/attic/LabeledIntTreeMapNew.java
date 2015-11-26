import it.univr.di.labeledvalue.Constants;

//package it.univr.di.attic;
//
//import it.unimi.dsi.fastutil.ints.IntCollection;
//import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
//import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//import it.unimi.dsi.fastutil.objects.ObjectSet;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.Literal;
//
//import java.io.Serializable;
//import java.util.Set;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Simple implementation of {@link LabeledValueMap} interface using Integer values.
// * <p>
// * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
// * <p>
// * It does not manage overflow cases when summing edge values.
// *  QUESTA IMPLEMENTAZIONE PIATTA DELLA MAPPA E` MENO EFFICIENTE DELL'IMPLEMENTAZIONE A DUE LIVELLI: PRIMO LIVELLO ARRAY DELLE MAPPE DELLE LABEL DI PARI LUNGHEZZA, 
//  SECONDO LIVELLO: LE MAPPE.
//
// * @author Roberto Posenato
// * @see LabeledValueMap
// */
//public class LabeledIntTreeMapNew implements LabeledIntMap, Serializable {
//
//	/**
//	 * @param args
//	 */
//	static public void main(final String[] args) {
//
//		final int nTest = 1000;
//		final double msNorm = 1.0 / (1000000.0 * nTest);
//
//		LabeledIntMap map = new LabeledIntTreeMapNew(true);
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
//			map.put(Label.emptyLabel, 109);
//			map.put(l2, 10);
//			map.put(l3, 25);
//			map.put(l4, 23);
//			map.put(l5, 22);
//			map.put(l6, 23);
//			map.put(l7, 20);
//			map.put(l8, 20);
//			map.put(l9, 21);
//			map.put(Label.emptyLabel, 100);
//			map.put(l11, 11);
//			map.put(l12, 24);
//			map.put(l13, 23);
//			map.put(l14, 22);
//			map.put(l15, 23);
//			map.put(l16, 20);
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
//	static public LabeledIntTreeMapNew parse(final String arg, final boolean withOptimization) {
//		if (arg == null) {
//			LabeledIntTreeMapNew.LOG.finest("Arg not valid: " + arg);
//			return null;
//		}
//		final String labelCharsRE = ""+Constants.propositionLetterRanges+"0-9, \\-" + Constants.NOT + Constants.EMPTY_LABEL;
//		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", arg)) {
//			LabeledIntTreeMapNew.LOG.finest("Arg not valid: " + arg);
//			return null;
//		}
//
//		final LabeledIntTreeMapNew newMap = new LabeledIntTreeMapNew(withOptimization);
//		final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
//		final Pattern splitterPair = Pattern.compile(", ");
//
//		final String[] entryPair = splitterEntry.split(arg);
//		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
//		for (final String s : entryPair) {
//			LabeledIntTreeMapNew.LOG.finest("s: " + s);
//			if (s.length() != 0) {
//				final String[] labInt = splitterPair.split(s);
//				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
//				newMap.put(Label.parse(labInt[0]), Integer.parseInt(labInt[1]));
//			}
//		}
//		LabeledIntTreeMapNew.LOG.finest("newMap: " + newMap);
//		return newMap;
//	}
//
//	/**
//	 * logger
//	 */
//	static private Logger LOG = Logger.getLogger(LabeledIntTreeMapNew.class.getName());
//
//	/**
//	 *
//	 */
//	static private final long serialVersionUID = 1L;
//
//	/**
//	 *
//	 * @param lvm
//	 * @return lvm as new labeled value map minimized.
//	 *         static public LabeledValueTreeMap getLabeledValueMapOptimized(LabeledValueTreeMap lvm) {
//	 *         if (lvm == null) return null;
//	 *         LabeledValueTreeMap newMap = new LabeledValueTreeMap();
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
//	 * The map is a sorted map!
//	 */
//	private Object2IntRBTreeMap<Label> map;
//
//	/**
//	 * To activate all optimization code in order to remove the redundant label in the set.
//	 */
//	private final boolean optimize;
//
//	/**
//	 * Simple constructor.
//	 * The internal structure is built and empty.
//	 *
//	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
//	 */
//	public LabeledIntTreeMapNew(final boolean withOptimization) {
//		this.map = new Object2IntRBTreeMap<>();
//		this.base = new Label();
//		this.optimize = withOptimization;
//		this.map.defaultReturnValue(INT_NULL);
//	}
//
//	/**
//	 * Constructor to clone the structure.
//	 *
//	 * @param lvm the LabeledValueTreeMap to clone. If it is null, the new object will be a empty map.
//	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
//	 */
//	public LabeledIntTreeMapNew(final LabeledIntTreeMapNew lvm, final boolean withOptimization) {
//		this(withOptimization);
//		if (lvm == null) return;
//		if (withOptimization) {
//			for (final Entry<Label> entry : lvm.map.object2IntEntrySet()) {
//				this.put(entry.getKey(), entry.getValue());
//			}
//		} else {
//			for (final Entry<Label> entry : lvm.map.object2IntEntrySet()) {
//				this.put(entry.getKey(), entry.getValue());
//			}
//		}
//	}
//
//	@Override
//	public void clear() {
//		this.map.clear();
//		this.base.clear();
//	}
//
//	@Override
//	public Set<Entry<Label>> entrySet() {
//		return map.object2IntEntrySet();
//	}
//
//	@Override
//	public boolean equals(final Object o) {
//		if ((o == null) || !(o instanceof LabeledIntTreeMapNew)) return false;
//		final LabeledIntTreeMapNew lvm = ((LabeledIntTreeMapNew) o);
//		return map.equals(lvm.map);
//		// optimization is not important!.
//	}
//
//	/**
//	 * @return the minimal value in this map. Null if the map is empty.
//	 */
//	public int getMinValue() {
//		int v = INT_NULL;
//
//		for (final int i : map.values())
//			if (v > i) v = i;
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
//	@Override
//	public int getMinValueConsistentWith(final Label l) {
//		if (l == null) return INT_NULL;
//		int v = this.getValue(l);
//		if (v == INT_NULL) {
//			// the label does not exits, try all consistent labels
//			int v1;
//			Label l1 = null;
//			for (final Entry<Label> e : map.object2IntEntrySet()) {
//				l1 = e.getKey();
//				if (l.isConsistentWith(l1)) {
//					v1 = e.getIntValue();
//					if (v > v1) v = v1;
//				}
//			}
//		}
//		// if (v == INT_NULL) {
//		// LabeledIntTreeMapNew.LOG.finest("There is no consistent labeled value for the given label " + l.toString());
//		// }
//		return v;
//	}
//
//	@Override
//	public int getValue(final Label l) {
//		return map.getInt(l);
//	}
//
//	@Override
//	public int hashCode() {
//		return this.map.hashCode();
//	}
//
//	/**
//	 * @return true if this map contains a negative value at least.
//	 */
//	public boolean isThereNegativeValues() {
//		for (final int i : map.values())
//			if (i < 0) return true;
//		return false;
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
//	public boolean put(final Label newLabel, final int newValue) {
//		if ((newLabel == null) || (newValue == INT_NULL)) return false;
//
//		// First version: very redundant but simple to check!
//		Label l1;
//		int v1;
//
//		final int newValueSize = newLabel.size();
//		int l1Size = 0;
//		for (final ObjectIterator<Entry<Label>> ite = map.object2IntEntrySet().iterator(); ite.hasNext();) {
//			final Entry<Label> entry = ite.next();
//			l1 = entry.getKey();
//			v1 = entry.getIntValue();
//			if ((newValueSize >= (l1Size = l1.size())) && newLabel.subsumes(l1) && (newValue >= v1))
//				return false;
//			if ((newValueSize <= l1Size) && l1.subsumes(newLabel) && (newValue < v1)) {
//				ite.remove();
//				if (this.optimize) {
//					this.checkValidityOfTheBaseAfterRemoving(l1);
//				}
//			}
//		}
//
//		/*
//		 * 2. Insert the value and check if it possible to simplify with some other label with same value and only one different literals.
//		 */
//		if (this.optimize) {
//			final Object2IntRBTreeMap<Label> a = new Object2IntRBTreeMap<>();
//			a.put(newLabel, newValue);
//			return this.insertAndSimplify(a);
//		}
//
//		return map.put(new Label(newLabel), newValue) != INT_NULL;
//	}
//
//	/**
//	 * Merge all the labeled value of map into this.
//	 * 
//	 * @param inputMap
//	 */
//	public void putAll(final LabeledIntTreeMapNew inputMap) {
//		if (inputMap == null) return;
//		for (final Entry<Label> entry : inputMap.map.object2IntEntrySet()) {
//			final Label l = entry.getKey();
//			final int value = entry.getIntValue();
//			this.put(l, value);
//		}
//	}
//
//	/**
//	 * Merge all the labeled value of map into this.
//	 * 
//	 * @param inputMap
//	 */
//	@Override
//	public void putAll(final LabeledIntMap inputMap) {
//		if (inputMap == null) return;
//		for (final Entry<Label> entry : inputMap.entrySet()) {
//			final Label l = entry.getKey();
//			final int value = entry.getIntValue();
//			this.put(l, value);
//		}
//	}
//
//	/**
//	 * Put the pair in the map. If force is true, the method insert the pair without any check about the label minimization.
//	 * 
//	 * @param l
//	 * @param i
//	 * @return the old value associated to l if there was, {@link LabeledIntMap#nullInt} otherwise.
//	 */
//	@Override
//	public int putForcibly(final Label l, final int i) {
//		if (l == null || i == INT_NULL) return INT_NULL;
//		return map.put(new Label(l), i);
//	}
//
//	@Override
//	public int remove(final Label l) {
//		if (l == null) return INT_NULL;
//
//		final int oldValue = map.removeInt(l);
//		if ((oldValue != INT_NULL) && this.optimize) {
//			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
//			// base.
//			if (checkValidityOfTheBaseAfterRemoving(l)) {
//				final LabeledIntTreeMapNew newMap = new LabeledIntTreeMapNew(this, this.optimize);
//				this.map = newMap.map;
//				this.base = newMap.base;
//			}
//		}
//		return oldValue;
//	}
//
//	@Override
//	public int size() {
//		return map.size();
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
//	public LabeledIntTreeMapNew summedTo(final LabeledIntTreeMapNew inputMap) {
//		final LabeledIntTreeMapNew resultMap = new LabeledIntTreeMapNew(this.optimize);
//		if (inputMap == null) return resultMap;
//
//		int l1Value, l2Value;
//		Label newLabel, l1, l2;
//		for (final Entry<Label> currentEntry : map.object2IntEntrySet()) {
//			for (final Entry<Label> inputEntry : inputMap.map.object2IntEntrySet()) {
//				l1 = currentEntry.getKey();
//				l1Value = currentEntry.getIntValue();
//				l2 = inputEntry.getKey();
//				l2Value = inputEntry.getIntValue();
//
//				newLabel = l1.conjunctionExtended(l2);
//				if (newLabel != null) {
//					long sum = l1Value + l2Value;
//					if (sum < INT_NULL)
//						resultMap.put(newLabel, (int) sum);
//					else
//						new IllegalArgumentException("Overflow in the labeled value (" + newLabel + ", " + sum + "). It cannot be represented as int.");
//				}
//			}
//		}
//		return resultMap;
//	}
//
//	@Override
//	public String toString() {
//		final StringBuffer sb = new StringBuffer("{");
//		for (final Entry<Label> e : map.object2IntEntrySet()) {
//			sb.append("(");
//			sb.append(e.getKey().toString());
//			sb.append(", ");
//			final int value = e.getIntValue();
//			if (value == INT_NULL) {
//				sb.append("" + Constants.INFINITY_SYMBOL);
//			} else {
//				sb.append(value);
//			}
//			sb.append(") ");
//		}
//		sb.append("}");
//		return sb.toString();
//	}
//
//	/**
//	 * @see LabeledIntMap#values()
//	 */
//	@Override
//	public IntCollection values() {
//		return map.values();
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
//		if ((l == null) || ((ln = l.size()) == 0) || ((bn = this.base.size()) == 0) || (ln != bn)) return false;
//
//		if (l.conjunctionExtended(this.base).size() > ln) return false;
//		this.base.clear();
//		return true;
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
//	private boolean insertAndSimplify(final Object2IntRBTreeMap<Label> inputMap) {
//		if ((inputMap == null) || (inputMap.size() == 0)) return false;// recursion basement!
//
//		// All entries of inputMap should have label of same size.
//		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
//		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
//		final ObjectSet<Entry<Label>> currentMapLimitedToLabelOfNSize = new ObjectArraySet<>();
//		for (Entry<Label> entry : map.object2IntEntrySet()) {
//			if (entry.getKey().size() == n) currentMapLimitedToLabelOfNSize.add(entry);
//		}
//		final Object2IntRBTreeMap<Label> toAdd = new Object2IntRBTreeMap<>();
//		// toRemove = new TreeMap(),
//
//		
//		if (currentMapLimitedToLabelOfNSize.size() > 0) {
//			for (final ObjectIterator<Entry<Label>> ite = inputMap.object2IntEntrySet().iterator(); ite.hasNext();) {
//				final Entry<Label> inputEntry = ite.next();
//				boolean iteRemoved = false;
//				final Label newLabel = inputEntry.getKey();
//				final int newValue = inputEntry.getIntValue();
//				// check is there is any labeled value with same value and only one opposite literal
//				for (final Entry<Label> entry : currentMapLimitedToLabelOfNSize) {
//					final Label l1 = new Label(entry.getKey());
//					final int v1 = entry.getIntValue();
//					Literal lit = null;
//					if ((newValue == v1) && ((lit = l1.getUniqueDifferentLiteral(newLabel)) != null)) {
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
//		boolean add = false;
//		for (final Entry<Label> entry : inputMap.object2IntEntrySet()) {
//			final Label l1 = entry.getKey();
//			final int v1 = entry.getIntValue();
//			this.removeAllValuesGreaterThan(l1, v1);
//			if (this.isBaseAbleToRepresent(l1, v1)) {
//				continue;
//			}
//			add = map.put(new Label(l1), v1)!= INT_NULL || add;
//			if (this.makeABetterBase(l1, v1)) {
//				this.removeAllValuesGreaterThanBase();
//			}
//		}
//		if (toAdd.size() > 0) {
//			add = this.insertAndSimplify(toAdd) || add;
//		}
//		return add;
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
//	private boolean isBaseAbleToRepresent(final Label l, final int v) {
//		if ((l == null) || (this.base == null) || this.base.isEmpty()) return false;
//
//		int n = this.base.size();
//		final Object2IntRBTreeMap<Label> currentMapLimitedToLabelOfNSize = new Object2IntRBTreeMap<>();
//		for (Entry<Label> entry : map.object2IntEntrySet()) {
//			if (entry.getKey().size() == n) currentMapLimitedToLabelOfNSize.put(entry.getKey(), entry.getIntValue());
//		}
//		for (final Label l1 : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
//			final int v1 = currentMapLimitedToLabelOfNSize.getInt(l1);
//			if (v1 == INT_NULL) {
//				LabeledIntTreeMapNew.LOG.severe("The base is not sound: base=" + this.base + ". currentMapLimitedToLabelOfNSize="
//						+ currentMapLimitedToLabelOfNSize);
//				this.base = new Label();
//				return false;
//				// throw new IllegalStateException("A base component has a null value. It is not possible.");
//			}
//			if (l.isConsistentWith(l1) && (v1 < v)) return true;
//		}
//		return false;
//	}
//
//	/**
//	 * @param label
//	 * @param v
//	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
//	 */
//	private boolean makeABetterBase(final Label label, final int v) {
//		if ((label == null) || (v == INT_NULL)) return false;
//
//		final int n = label.size();
//
//		if (n == 0) {
//			// The new labeled value (l,v) has universal label, the base is not more necessary!
//			this.base.clear();
//			return true;
//		}
//		final Object2IntRBTreeMap<Label> currentMapLimitedToLabelOfNSize = new Object2IntRBTreeMap<>();
//		for (Entry<Label> entry : map.object2IntEntrySet()) {
//			if (entry.getKey().size() == n) currentMapLimitedToLabelOfNSize.put(entry.getKey(), entry.getIntValue());
//		}
//
//		if (currentMapLimitedToLabelOfNSize.size() < Math.pow(2.0, n)) // there are no sufficient elements!
//			return false;
//		final Literal[] baseCandidateColl = label.getAllStraight();
//		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
//			if (currentMapLimitedToLabelOfNSize.get(label1) == null) return false;
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
//	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
//	 *
//	 * @param l
//	 * @param i
//	 * @return true if one element at least has been removed, false otherwise.
//	 */
//	private boolean removeAllValuesGreaterThan(final Label l, final int i) {
//		if (l == null || i == INT_NULL) return false;
//
//		final int n = l.size();
//		boolean removed = false;
//		for (final ObjectIterator<Entry<Label>> mapIte = this.map.object2IntEntrySet().iterator(); mapIte.hasNext();) {
//			final Entry<Label> entry = mapIte.next();
//			final Label l1 = entry.getKey();
//			if (l1.size() < n) continue;
//			final int value = entry.getIntValue();
//			if (l1.subsumes(l) && (value >= i)) {
//				mapIte.remove();
//				this.checkValidityOfTheBaseAfterRemoving(l1);
//				removed = true;
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
//
//		final LabeledIntTreeMapNew newMap = new LabeledIntTreeMapNew(this.optimize);
//
//		// build the list of labeled values that form the base
//		final ObjectArraySet<Entry<Label>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
//		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllStraight())) {
//			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.getValue(l)));
//		}
//		Label l1, lb;
//		int v1, vb;
//		boolean toInsert = true;
//		for (final Entry<Label> entry : this.map.object2IntEntrySet()) {
//			l1 = entry.getKey();
//			v1 = entry.getIntValue();
//			toInsert = false;
//			for (final Entry<Label> baseEntry : baseComponent) {
//				lb = baseEntry.getKey();
//				vb = baseEntry.getIntValue();
//				if (l1.equals(lb)) {
//					toInsert = true; // a base component has to be always insert!
//					break;
//				}
//				// if (l1 == null || lb == null || v1 == null || vb == null) {
//				// System.out.println("Trovato");
//				// }
//				if (l1.isConsistentWith(lb) && (v1 < vb)) {
//					toInsert = true;
//					break;
//				}
//			}
//			if (toInsert) {
//				newMap.putForcibly(l1, v1);
//			}
//		}
//		if (!newMap.equals(this.map)) {
//			this.map = newMap.map;
//			return true;
//		}
//		return false;
//	}
//
//	@Override
//	public LabeledIntMap summedTo(LabeledIntMap inputMap) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
