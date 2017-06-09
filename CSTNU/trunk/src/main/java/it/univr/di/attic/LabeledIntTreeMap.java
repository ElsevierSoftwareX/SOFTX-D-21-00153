
//package it.univr.di.labeledvalue;
//
//import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
//import it.unimi.dsi.fastutil.ints.IntArraySet;
//import it.unimi.dsi.fastutil.ints.IntSet;
//import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
//import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//
//import java.io.Serializable;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Simple implementation of {@link LabeledIntMap} interface using int (primitive type) values.
// * <p>
// * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
// *
// * @author Roberto Posenato
// * @see LabeledIntMap
// */
//public class LabeledIntTreeMap implements LabeledIntMap, Serializable {
//
//	/**
//	 * @param args
//	 */
//	static public void main(final String[] args) {
//
//		final int nTest = 1000;
//		final double msNorm = 1.0 / (1000000.0 * nTest);
//
//		final LabeledIntMap map = new LabeledIntTreeMap(true);
//
//		final Label l2 = Label.parse("abdfghji");
//		final Label l3 = Label.parse("adji");
//		final Label l4 = Label.parse("¬bdfghji");
//		final Label l5 = Label.parse("b¬dfji");
//		final Label l6 = Label.parse("a¬bdfi");
//		final Label l7 = Label.parse("abdfg¬hji");
//		final Label l8 = Label.parse("aews¬fg");
//		final Label l9 = Label.parse("¬arffss");
//		final Label l11 = Label.parse("abdfghi");
//		final Label l12 = Label.parse("adj¬fi");
//		final Label l13 = Label.parse("¬bdfghji");
//		final Label l14 = Label.parse("bdfjigde");
//		final Label l15 = Label.parse("a¬bdfg¬ihj");
//		final Label l16 = Label.parse("abd");
//
//		final long startTime = System.nanoTime();
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
//		final long endTime = System.nanoTime();
//		System.out.println("Execution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map + ".\nTime: (ms): "
//				+ ((endTime - startTime) * msNorm));
//	}
//
//	/**
//	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by
//	 * the string.<br>
//	 * The format of the string is given by the method {@link #toString()}:<br>
//	 * {\[(<key>, <value>) \]*}
//	 *
//	 * @param inputMap
//	 * @param withOptimization true if the redundant labeled values have to be removed.
//	 * @return a LabeledValueTreeMap object if <code>inputMap<code> represents a valid map, null otherwise.
//	 */
//	static public LabeledIntTreeMap parse(final String inputMap, final boolean withOptimization) {
//		if (inputMap == null) return null;
//		final String labelCharsRE = Constants.propositionLetterRanges+", \\-" + Constants.NOT + Constants.UNKNOWN + Constants.EMPTY_LABEL;
//		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", inputMap)) // LabeledIntTreeMap.LOG.finest("Arg not valid: " + inputMap);
//			return null;
//
//		final LabeledIntTreeMap newMap = new LabeledIntTreeMap(withOptimization);
//		final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
//		final Pattern splitterPair = Pattern.compile(", ");
//
//		final String[] entryPair = splitterEntry.split(inputMap);
//		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
//		for (final String s : entryPair) {
//			LabeledIntTreeMap.LOG.finest("s: " + s);
//			if (s.length() != 0) {
//				final String[] labInt = splitterPair.split(s);
//				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
//				newMap.put(Label.parse(labInt[0]), Integer.parseInt(labInt[1]));
//			}
//		}
//		// LabeledIntTreeMap.LOG.finest("newMap: " + newMap);
//		return newMap;
//	}
//
//	/**
//	 * Determines the sum of 'a' and 'b'.
//	 * If any of them is already INFINITY, returns INFINITY.
//	 *
//	 * If the sum is greater/lesser than the maximum/minimum integer representable by a int, it throw an IllegalStateException because the overflow.
//	 * I don't use Overflow exception because it requires to use a try{} catch section.
//	 *
//	 * @param a
//	 * @param b
//	 * @return the controlled sum.
//	 * @throws ArithmeticException
//	 */
//	static public final int sumWithOverflowCheck(final int a, final int b) throws ArithmeticException {
//		if ((a == Constants.INT_NEG_INFINITE) || (b == Constants.INT_NEG_INFINITE)) return Constants.INT_NEG_INFINITE;
//		if ((a == Constants.INT_POS_INFINITE) || (b == Constants.INT_POS_INFINITE)) return Constants.INT_POS_INFINITE;
//
//		final long sum = (long) a + (long) b;// CAST IS NECESSARY!
//		if ((sum >= Constants.INT_POS_INFINITE) || (sum <= Constants.INT_NEG_INFINITE))
//			throw new ArithmeticException("Integer overflow in a sum of labeled values: " + a + " + " + b);
//		return (int) sum;
//	}
//
//	/**
//	 * logger
//	 */
//	static private Logger LOG = Logger.getLogger(LabeledIntTreeMap.class.getName());
//
//	/**
//	 *
//	 */
//	static private final long serialVersionUID = 1L;
//
//	/**
//	 * Label forming a base for the labels of the map.
//	 * All literal are straight.
//	 */
//	private Label base;
//
//	/**
//	 * Design choice: the set of labeled values of this map is organized as a collection of sets each containing labels of the same length.
//	 * This allows the label minimization task to be performed in a more systematic and, possibly, efficient way.
//	 * The efficiency has been proved comparing this implementation with one in which the map has been realized with a standard map and the minimization task
//	 * determines the same length labels every time it needs it.
//	 */
//	private Int2ObjectAVLTreeMap<Object2IntRBTreeMap<Label>> mainInt2SetMap;
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
//	public LabeledIntTreeMap(final boolean withOptimization) {
//		this.mainInt2SetMap = new Int2ObjectAVLTreeMap<>();
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
//	public LabeledIntTreeMap(final LabeledIntMap lvm, final boolean withOptimization) {
//		this(withOptimization);
//		if (lvm == null) return;
//		if (withOptimization) {
//			for (final Entry<Label> entry : lvm.entrySet()) {
//				this.put(entry.getKey(), entry.getValue());
//			}
//		} else {
//			for (final Entry<Label> entry : lvm.entrySet()) {
//				this.putForcibly(entry.getKey(), entry.getValue());
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
//	public Set<Entry<Label>> entrySet() {
//		final Object2IntRBTreeMap<Label> coll = new Object2IntRBTreeMap<>();
//		coll.defaultReturnValue(LabeledIntMap.INT_NULL);
//		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
//			coll.putAll(mapI);
//		}
//		return coll.object2IntEntrySet();
//	}
//
//	/**
//	 *
//	 * @return true if the input is a {@link LabeledIntMap} and it has an equal set of labeled values.
//	 */
//	@Override
//	public boolean equals(final Object o) {
//		if ((o == null) || !(o instanceof LabeledIntMap)) return false;
//		final LabeledIntMap lvm = ((LabeledIntMap) o);
//		if (this.size() != lvm.size()) return false;
//		return this.entrySet().equals(lvm.entrySet());// Two maps are equals if they contain the same set of values. The internal representation of
//		// optimization is not important!.
//	}
//
//	@Override
//	public int getMinValue() {
//		int v = Constants.INT_POS_INFINITE;
//		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values()) {
//			for (final int i : map1.values())
//				if (v > i) {
//					v = i;
//				}
//		}
//		return (v == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : v;
//	}
//
//	@Override
//	public int getMinValueAmongLabelsWOUnknown() {
//		int v = Constants.INT_POS_INFINITE, i;
//		Label l;
//		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values()) {
//			for (final Entry<Label> entry : map1.object2IntEntrySet()) {
//				l = entry.getKey();
//				if (l.containsUnknown()) continue;
//				i = entry.getIntValue();
//				if (v > i) v = i;
//			}
//		}
//		return (v == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : v;
//	}
//
//	@Override
//	public int getMinValueConsistentWith(final Label l) {
//		if (l == null) return LabeledIntMap.INT_NULL;
//		int min = this.getValue(l);
//		if (min == LabeledIntMap.INT_NULL) {
//			// the label does not exits, try all consistent labels
//			min = Constants.INT_POS_INFINITE;
//			int v1;
//			Label l1 = null;
//			for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
//				for (final Entry<Label> e : mapI.object2IntEntrySet()) {
//					l1 = e.getKey();
//					if (l.isConsistentWith(l1)) {
//						v1 = e.getIntValue();
//						if (min > v1) {
//							min = v1;
//						}
//					}
//				}
//			}
//		}
//		// if (v == INT_NULL) {
//		// LabeledIntTreeMapNew.LOG.finest("There is no consistent labeled value for the given label " + l.toString());
//		// }
//		return (min == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : min;
//	}
//
//	@Override
//	public int getValue(final Label l) {
//		if (l == null) return LabeledIntMap.INT_NULL;
//		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return LabeledIntMap.INT_NULL;
//		return map1.getInt(l);
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
//		for (final int i : this.values())
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
//		if ((newLabel == null) || (newValue == LabeledIntMap.INT_NULL)) return false;
//		if (!this.optimize) {
//			this.putForcibly(newLabel, newValue);
//			return true;
//		}
//		// First version: very redundant but simple to check!
//		/*
//		 * 1. Check if there is already a value in the map that represents the new value (in this case, return false)
//		 * or if one or more value in the map can be removed by the insertion of the new value (in this case remove such values).
//		 */
//		Label l1;
//		int v1;
//
//		final int newValueSize = newLabel.size();
//		int l1Size = 0;
//		for (final Object2IntRBTreeMap<Label> mapAllSameLenghLabels : this.mainInt2SetMap.values()) {
//			for (final ObjectIterator<Entry<Label>> ite = mapAllSameLenghLabels.object2IntEntrySet().iterator(); ite.hasNext();) {
//				final Entry<Label> entry = ite.next();
//				l1 = entry.getKey();
//				v1 = entry.getIntValue();
//				if ((newValueSize >= (l1Size = l1.size())) && newLabel.subsumes(l1) && (newValue >= v1)) return false;
//				if ((newValueSize <= l1Size) && l1.subsumes(newLabel) && (newValue < v1)) {
//					if (LOG.isLoggable(Level.FINER)) {
//						LOG.log(Level.FINER,
//								"Labeled value (" + l1 + ", " + Constants.formatInt(v1) + ") removed by (" + newLabel + ", " + Constants.formatInt(newValue)
//										+ ").");
//					}
//					ite.remove();
//					this.checkValidityOfTheBaseAfterRemoving(l1);
//				}
//			}
//		}
//
//		/*
//		 * 2. If optimization is request,
//		 * Insert the new value and check if it possible to simplify with some other label with same value and only one different literals.
//		 */
//		if (this.optimize) {
//			final Object2IntRBTreeMap<Label> a = new Object2IntRBTreeMap<>();
//			a.defaultReturnValue(LabeledIntMap.INT_NULL);
//			a.put(newLabel, newValue);
//			return this.insertAndSimplify(a);
//		}
//
//		/*
//		 * 2.1 Otherwise, put the new value and return if there was an old one.
//		 */
//		this.putForcibly(newLabel, newValue);
//		return true;
//	}
//
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
//	 * A class specific version of {@link #putAll(LabeledIntMap)}
//	 *
//	 * @param inputMap
//	 */
//	public void putAll(final LabeledIntTreeMap inputMap) {
//		if (inputMap == null) return;
//		for (final Object2IntRBTreeMap<Label> map1 : inputMap.mainInt2SetMap.values()) {
//			for (final Entry<Label> entry : map1.object2IntEntrySet()) {
//				final Label l = entry.getKey();
//				final int value = entry.getIntValue();
//				this.put(l, value);
//			}
//		}
//	}
//
//	@Override
//	public int putForcibly(final Label l, final int i) {
//		if ((l == null) || (i == LabeledIntMap.INT_NULL)) return LabeledIntMap.INT_NULL;
//		Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) {
//			map1 = new Object2IntRBTreeMap<>();
//			map1.defaultReturnValue(LabeledIntMap.INT_NULL);
//			this.mainInt2SetMap.put(l.size(), map1);
//		}
//		return map1.put(new Label(l), i);
//	}
//
//	@Override
//	public int remove(final Label l) {
//		if (l == null) return LabeledIntMap.INT_NULL;
//
//		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return LabeledIntMap.INT_NULL;
//		final int oldValue = map1.removeInt(l);
//		if ((oldValue != LabeledIntMap.INT_NULL) && this.optimize) {
//			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
//			// base.
//			if (this.checkValidityOfTheBaseAfterRemoving(l)) {
//				final LabeledIntTreeMap newMap = new LabeledIntTreeMap(this, this.optimize);
//				this.mainInt2SetMap = newMap.mainInt2SetMap;
//				this.base = newMap.base;
//			}
//		}
//		return oldValue;
//	}
//
//	@Override
//	public int size() {
//		int n = 0;
//		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values()) {
//			n += map1.size();
//		}
//		return n;
//	}
//
//	/**
//	 * @param inputMap
//	 * @return Sums the labeled values of inputMap to the values of this map and returns the result as a new labeled
//	 *         value map. If the labels of the two maps are all not consistent or the input map is null, then an empty map is returned.
//	 * @see #summedTo(LabeledIntTreeMap)
//	 */
//	public LabeledIntMap summedTo(final LabeledIntMap inputMap) {
//		return this.summedTo(((LabeledIntTreeMap) inputMap));
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
//	public LabeledIntTreeMap summedTo(final LabeledIntTreeMap inputMap) {
//		final LabeledIntTreeMap resultMap = new LabeledIntTreeMap(this.optimize);
//		if (inputMap == null) return resultMap;
//
//		int l1Value, l2Value;
//		Label newLabel, l1, l2;
//		for (final Object2IntRBTreeMap<Label> currentMap1 : this.mainInt2SetMap.values()) {
//			for (final Entry<Label> currentEntry : currentMap1.object2IntEntrySet()) {
//				for (final Object2IntRBTreeMap<Label> inputMap1 : inputMap.mainInt2SetMap.values()) {
//					for (final Entry<Label> inputEntry : inputMap1.object2IntEntrySet()) {
//						l1 = currentEntry.getKey();
//						l1Value = currentEntry.getIntValue();// the value is significative
//						l2 = inputEntry.getKey();
//						l2Value = inputEntry.getIntValue();
//
//						newLabel = l1.conjunctionExtended(l2);
//						if (newLabel != null) {
//							resultMap.put(newLabel, LabeledIntTreeMap.sumWithOverflowCheck(l1Value, l2Value));
//						}
//					}
//				}
//			}
//		}
//		return resultMap;
//	}
//
//	@Override
//	public String toString() {
//		final StringBuffer sb = new StringBuffer("{");
//		for (final Entry<Label> e : this.entrySet()) {// I wanted a sorted print!
//			sb.append("(");
//			sb.append(e.getKey().toString());
//			sb.append(", ");
//			final int value = e.getIntValue();
//			if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
//				if (value < 0) {
//					sb.append('-');
//				}
//				sb.append(Constants.INFINITY_SYMBOL);
//			} else {
//				sb.append(value);
//			}
//			sb.append(") ");
//		}
//		sb.append("}");
//		return sb.toString();
//	}
//
//	@Override
//	public IntSet values() {
//		final IntArraySet coll = new IntArraySet(this.size());
//		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
//			coll.addAll(mapI.values());
//		}
//		return coll;
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
//		boolean add = false;
//		// All entries of inputMap should have label of same size.
//		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
//		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
//		final Object2IntRBTreeMap<Label> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(n);
//		final Object2IntRBTreeMap<Label> toAdd = new Object2IntRBTreeMap<>();
//		toAdd.defaultReturnValue(LabeledIntMap.INT_NULL);
//
//		if (currentMapLimitedToLabelOfNSize != null) {
//			for (final ObjectIterator<Entry<Label>> ite = inputMap.object2IntEntrySet().iterator(); ite.hasNext();) {
//				final Entry<Label> inputEntry = ite.next();
//				boolean iteRemoved = false;
//				final Label newLabel = inputEntry.getKey();
//				final int newValue = inputEntry.getIntValue();
//				// check is there is any labeled value with same value and only one opposite literal
//				for (final Entry<Label> entry : currentMapLimitedToLabelOfNSize.object2IntEntrySet()) {
//					final Label l1 = new Label(entry.getKey());
//					final int v1 = entry.getIntValue();
//					Literal lit = null;
//					if ((newValue == v1) && ((lit = l1.getUniqueDifferentLiteral(newLabel)) != null)) {
//						// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
//						if (!iteRemoved) {
//							if (LOG.isLoggable(Level.FINER)) {
//								LOG.log(Level.FINER, "Labeled value (" + newLabel + ", " + Constants.formatInt(newValue)
//										+ ") removed by the label with opposite (" + l1 + ", " + Constants.formatInt(v1) + ") in order to introduce a smaller label.");
//							}
//							ite.remove();
//							iteRemoved = true;
//						}
//						l1.removeAllLiteralsWithSameName(lit);
//						if (l1.size() < 0) throw new IllegalStateException("There is no literal to remove, there is a problem in the code!");
//						toAdd.put(l1, v1);
//					}
//				}
//			}
//		}
//		// inputMap has been updated. Now it contains all the elements that have to be insert.
//		for (final Entry<Label> entry : inputMap.object2IntEntrySet()) {
//			final Label l1 = entry.getKey();
//			final int v1 = entry.getIntValue();
//			this.removeAllValuesGreaterThan(l1, v1);
//			if (this.isBaseAbleToRepresent(l1, v1)) continue;
//			if (LOG.isLoggable(Level.FINEST)) LOG.log(Level.FINER, "Labeled value (" + l1 + ", " + Constants.formatInt(v1) + ") added by insertAndSimplify.");
//			this.putForcibly(new Label(l1), v1);
//			add = true;
//			if (this.makeABetterBase(l1, v1)) this.removeAllValuesGreaterThanBase();
//		}
//		if (toAdd.size() > 0) add = this.insertAndSimplify(toAdd) || add;
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
//		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(this.base.size());
//		for (final Label l1 : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
//			final int v1 = map1.getInt(l1);
//			if (v1 == LabeledIntMap.INT_NULL) {
//				LabeledIntTreeMap.LOG.severe("The base is not sound: base=" + this.base + ". Map1=" + map1);
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
//		if ((label == null) || (v == LabeledIntMap.INT_NULL)) return false;
//
//		final int n = label.size();
//
//		if (n == 0) {
//			// The new labeled value (l,v) has universal label, the base is not more necessary!
//			this.base.clear();
//			return true;
//		}
//		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(n);
//		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
//			return false;
//		final Literal[] baseCandidateColl = label.getAllAsStraight();
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
//	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
//	 *
//	 * @param l
//	 * @param i
//	 * @return true if one element at least has been removed, false otherwise.
//	 */
//	private boolean removeAllValuesGreaterThan(final Label l, final int i) {
//		if ((l == null) || (i == LabeledIntMap.INT_NULL)) return false;
//
//		final int n = l.size();
//		boolean removed = false;
//		for (Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.tailMap(n).values()) {
//			for (final ObjectIterator<Entry<Label>> currentMapIte = map1.object2IntEntrySet().iterator(); currentMapIte.hasNext();) {
//				final Entry<Label> entry = currentMapIte.next();
//				final Label l1 = entry.getKey();
//				final int value = entry.getIntValue();
//				if (l1.subsumes(l) && (value >= i)) {
//					if (LOG.isLoggable(Level.FINER)) {
//						LOG.log(Level.FINER, "Labeled value (" + l1 + ", " + Constants.formatInt(value) + ") removed by (" + l + ", " + Constants.formatInt(i)
//								+ ").");
//					}
//					currentMapIte.remove();
//					this.checkValidityOfTheBaseAfterRemoving(l1);
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
//		final LabeledIntTreeMap newMap = new LabeledIntTreeMap(this.optimize);
//		// build the list of labeled values that form the base
//		final ObjectArraySet<Entry<Label>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
//		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllAsStraight())) {
//			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.getValue(l)));
//		}
//		Label l1, lb;
//		int v1, vb;
//		boolean toInsert = true;
//		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values()) {
//			for (final Entry<Label> entry : map1.object2IntEntrySet()) {
//				l1 = entry.getKey();
//				v1 = entry.getIntValue();
//				toInsert = false;
//				for (final Entry<Label> baseEntry : baseComponent) {
//					lb = baseEntry.getKey();
//					vb = baseEntry.getIntValue();
//					if (l1.equals(lb)) {
//						toInsert = true; // a base component has to be always insert!
//						break;
//					}
//					if (l1.isConsistentWith(lb) && (v1 < vb)) {
//						toInsert = true;
//						break;
//					}
//				}
//				if (toInsert) {
//					newMap.putForcibly(l1, v1);
//				}
//			}
//		}
//		if (!newMap.equals(this)) {
//			this.mainInt2SetMap = newMap.mainInt2SetMap;
//			return true;
//		}
//		return false;
//	}
//
//}
