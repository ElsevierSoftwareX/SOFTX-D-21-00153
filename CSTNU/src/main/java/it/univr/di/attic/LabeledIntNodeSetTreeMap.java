//package it.univr.di.labeledvalue;
//
//import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
//import it.unimi.dsi.fastutil.ints.IntArraySet;
//import it.unimi.dsi.fastutil.ints.IntSet;
//import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
//import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
//import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
//import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
//import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectIterator;
//import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.Literal;
//import it.univr.di.labeledvalue.ValueNodeSetPair;
//
//import java.io.Serializable;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.regex.Pattern;
//
///**
// * Simple implementation of {@link LabeledIntNodeSetMap} interface.
// * <p>
// * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
// *
// * @author Roberto Posenato
// * @see LabeledIntNodeSetMap
// */
//public class LabeledIntNodeSetTreeMap implements LabeledIntNodeSetMap, Serializable {
//
//	/**
//	 * logger
//	 */
//	static private Logger LOG = Logger.getLogger(LabeledIntNodeSetTreeMap.class.getName());
//
//	/**
//	 *
//	 */
//	static private final long serialVersionUID = 1L;
//
//	/**
//	 * @param args
//	 */
//	static public void main(final String[] args) {
//
//		final int nTest = 1000;
//		final double msNorm = 1.0 / (1000000.0 * nTest);
//
//		final LabeledIntNodeSetMap map = new LabeledIntNodeSetTreeMap(true);
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
//			map.put(Label.emptyLabel, 109, null);
//			map.put(l2, 10, null);
//			map.put(l3, 25, null);
//			map.put(l4, 23, null);
//			map.put(l5, 22, null);
//			map.put(l6, 23, null);
//			map.put(l7, 20, null);
//			map.put(l8, 20, null);
//			map.put(l9, 21, null);
//			map.put(Label.emptyLabel, 100, null);
//			map.put(l11, 11, null);
//			map.put(l12, 24, null);
//			map.put(l13, 23, null);
//			map.put(l14, 22, null);
//			map.put(l15, 23, null);
//			map.put(l16, 20, null);
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
//	static public LabeledIntNodeSetTreeMap parse(final String inputMap, final boolean withOptimization) {
//		if (inputMap == null) return null;
//		final String labelCharsRE = "a-zA-Z0-9, \\-" + Constants.NOT + Constants.UNKNOWN + Constants.EMPTY_LABEL;
//		if (!Pattern.matches("\\{[\\(" + labelCharsRE + "\\) ]*\\}", inputMap)) return null;
//
//		final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(withOptimization);
//		final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
//		final Pattern splitterPair = Pattern.compile(", ");
//
//		final String[] entryPair = splitterEntry.split(inputMap);
//		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
//		for (final String s : entryPair) {
//			LabeledIntNodeSetTreeMap.LOG.finest("s: " + s);
//			if (s.length() != 0) {
//				final String[] labInt = splitterPair.split(s);
//				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
//				newMap.put(Label.parse(labInt[0]), Integer.parseInt(labInt[1]), null);
//			}
//		}
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
//	@SuppressWarnings("javadoc")
//	private static final boolean isNullOrEmpty(Set<String> nodeSet) {
//		return nodeSet == null || nodeSet.isEmpty();
//	}
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
//	private Int2ObjectAVLTreeMap<Object2ObjectRBTreeMap<Label, ValueNodeSetPair>> mainInt2SetMap;
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
//	public LabeledIntNodeSetTreeMap(final boolean withOptimization) {
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
//	public LabeledIntNodeSetTreeMap(final LabeledIntNodeSetMap lvm, final boolean withOptimization) {
//		this(withOptimization);
//		if (lvm == null) return;
//		Set<String> s, inputS;
//		if (withOptimization) {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : lvm.object2ObjectEntrySet()) {
//				if ((inputS = entry.getValue().getNodeSet()) != null) {
//					s = ValueNodeSetPair.newSetInstance();
//					s.addAll(inputS);
//				} else {
//					s = null;
//				}
//				this.put(new Label(entry.getKey()), entry.getValue().getValue(), s);
//			}
//		} else {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : lvm.object2ObjectEntrySet()) {
//				if ((inputS = entry.getValue().getNodeSet()) != null) {
//					s = ValueNodeSetPair.newSetInstance();
//					s.addAll(inputS);
//				} else {
//					s = null;
//				}
//				this.putForcibly(new Label(entry.getKey()), entry.getValue().getValue(), s);
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
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e1 : mapI.object2ObjectEntrySet())
//				coll.put(e1.getKey(), e1.getValue().getValue());
//		}
//		return coll.object2IntEntrySet();
//	}
//
//	/**
//	 *
//	 * @return true if the input is a {@link LabeledIntNodeSetMap} and it has an equal set of labeled values.
//	 */
//	@Override
//	public boolean equals(final Object o) {
//		if ((o == null) || !(o instanceof LabeledIntNodeSetMap)) return false;
//		final LabeledIntNodeSetMap lvm = ((LabeledIntNodeSetMap) o);
//		if (this.size() != lvm.size()) return false;
//		return this.object2ObjectEntrySet().equals(lvm.object2ObjectEntrySet());// Two maps are equals if they contain the same set of values. The internal
//																				// representation of optimization is not important!.
//	}
//
//	/**
//	 * @param l
//	 * @return the pair value e node set associate to label if it exists, null otherwise.
//	 */
//	public ValueNodeSetPair get(final Label l) {
//		if (l == null) return null;
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return null;
//		return map1.get(l);
//	}
//
//	@Override
//	public int getMinValue() {
//		int v = Constants.INT_POS_INFINITE;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
//			for (final ValueNodeSetPair i : map1.values())
//				if (v > i.getValue()) v = i.getValue();
//		}
//		return (v == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : v;
//	}
//
//	@Override
//	public int getMinValueAmongLabelsWOUnknown() {
//		int v = Constants.INT_POS_INFINITE, i;
//		Label l;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : map1.object2ObjectEntrySet()) {
//				l = entry.getKey();
//				if (l.containsUnknown()) continue;
//				i = entry.getValue().getValue();
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
//			for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
//				for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e : mapI.object2ObjectEntrySet()) {
//					l1 = e.getKey();
//					if (l.isConsistentWith(l1)) {
//						v1 = e.getValue().getValue();
//						if (min > v1) min = v1;
//					}
//				}
//			}
//		}
//		// if (v == INT_NULL) {
//		// LOG.finest("There is no consistent labeled value for the given label " + l.toString());
//		// }
//		return (min == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : min;
//	}
//
//	@Override
//	public int getValue(final Label l) {
//		if (l == null) return LabeledIntMap.INT_NULL;
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
//		ValueNodeSetPair e = null;
//		if (map1 == null || ((e = map1.get(l)) == null)) return LabeledIntMap.INT_NULL;
//		return e.getValue();
//	}
//
//	@Override
//	public int hashCode() {
//		return this.mainInt2SetMap.hashCode();
//	}
//
//	@Override
//	public Set<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> object2ObjectEntrySet() {
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> coll = new Object2ObjectRBTreeMap<>();
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
//			coll.putAll(mapI);
//		}
//		return coll.object2ObjectEntrySet();
//	}
//
//	@Override
//	public boolean put(Label l, int i) {
//		return this.put(l, i, null);
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
//	public boolean put(final Label newLabel, int newValue, Set<String> newNodeSet) {
//		/*
//		 * This version of the method is very redundant but simple to check!
//		 */
//		if ((newLabel == null) || (newValue == LabeledIntMap.INT_NULL)) return false;
//		/*
//		 * 1. Check if there is already a value in the map that represents the new value and the node set (in this case, return false)
//		 * or if one or more value in the map can be removed by the insertion of the new value (in this case remove such values).
//		 */
//		Label l1;
//		int v1;
//		Set<String> nodeSet1;
//
//		final int newValueSize = newLabel.size();
//		int l1Size = 0;
//		ValueNodeSetPair vnspair;
//		Set<Label> labelToRemove = new ObjectArraySet<>();
//		Set<Label> labelToAdjust = new ObjectArraySet<>();
//		boolean hasNewLabelToBeInserted = true;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapAllSameLenghLabels : this.mainInt2SetMap.values()) {
//			for (Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : mapAllSameLenghLabels.object2ObjectEntrySet()) {
//				l1 = entry.getKey();
//				vnspair = entry.getValue();
//				v1 = vnspair.getValue();
//				nodeSet1 = vnspair.getNodeSet();
//				if ((newValueSize >= (l1Size = l1.size())) && newLabel.subsumes(l1) && newValue >= v1) {
//					if (isNullOrEmpty(newNodeSet)) {
//						// the input value is simple (no associated node set) and it is already contained in the set.
//						return false;
//					}
//					if (v1 == Constants.INT_NEG_INFINITE) {
//						// the input value is not simple because has a node set but the subsume an -infinity labeled value, we can continue to
//						// check with other but making it simple. It will then not insert. (we continue in order to make a sanity check in the rest of the set)
//						newNodeSet = null;
//						hasNewLabelToBeInserted = false;
//					}
//					newValue = v1;// the subsumed value is better than the new one but, since the new one has a node set, it has to be inserted.
//					// we insert it but with the value v1 that it is more negative.
//				}
//				if ((newValueSize <= l1Size) && l1.subsumes(newLabel) && (newValue <= v1)) {
//					if (newValue == v1 && newLabel.equals(l1) && newValue == Constants.INT_NEG_INFINITE) {
//						// the value is already in the set
//						hasNewLabelToBeInserted = false;
//						continue;
//					}
//					if (isNullOrEmpty(nodeSet1)) {
//						labelToRemove.add(l1);// ite.remove();// case 1, 2
//						// l1 is more generic and simple, we remove it
//						// this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(l1, nodeSet1);
//					} else {
//						if (newLabel.equals(l1)) {
//							// The case of the same label when one of two labels has a node set is managed here
//							if (newValue == v1 && nodeSet1.equals(newNodeSet)) {
//								hasNewLabelToBeInserted = false;
//								continue;
//							}
//							if (newValue != Constants.INT_NEG_INFINITE) {
//								// Negative Infinity can be propagated without node set
//								if (newNodeSet == null) newNodeSet = ValueNodeSetPair.newSetInstance();
//								newNodeSet.addAll(nodeSet1);
//							}
//							labelToRemove.add(l1);
//						} else {
//							// The already labeled value (that it is not simple) has to be adjusted if it has a greater value.
//							if (newValue < v1) labelToAdjust.add(l1);
//						}
//					}
//				}
//			}
//		}
//
//		// The label to remove and to adjust are now ready to be managed.
//		for (Label lr : labelToRemove) {
//			Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map = this.mainInt2SetMap.get(lr.size());
//			if (LOG.isLoggable(Level.FINEST)) LOG.log(Level.FINEST,
//					"The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") forces the removal of (" + lr + ", " + map.get(lr) + ").");
//			map.remove(lr);
//			this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(lr);
//		}
//		for (Label lr : labelToAdjust) {
//			Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map = this.mainInt2SetMap.get(lr.size());
//			ValueNodeSetPair vnspair1 = map.get(lr);
//			if (LOG.isLoggable(Level.FINEST)) LOG.log(Level.FINEST,
//					"The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") forces the modification of (" + lr + ", " + vnspair1 + ") to ("
//							+ lr + ", " + newValue + ", " + ((newValue == Constants.INT_NEG_INFINITE) ? "null" : vnspair1.getNodeSet()));
//			vnspair1.setValue(newValue);
//			if (newValue == Constants.INT_NEG_INFINITE) vnspair1.setNodeSet(null);
//		}
//
//		if (!hasNewLabelToBeInserted) {
//			if (LOG.isLoggable(Level.FINEST))
//				LOG.log(Level.FINEST, "The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") has not to be inserted.");
//			return false;
//		}
//		/*
//		 * 2. If optimization is request,
//		 * Insert the new value and check if it possible to simplify with some other label with same value and only one different literals.
//		 */
//		if (this.optimize) {
//			final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> a = new Object2ObjectRBTreeMap<>();
//			a.put(newLabel, new ValueNodeSetPair(newValue, newNodeSet));
//			return this.insertAndSimplify(a);
//		}
//
//		/*
//		 * 2.1 Otherwise, put the new value and return if there was an old one.
//		 */
//		this.putForcibly(newLabel, newValue, newNodeSet);
//		return true;
//	}
//
//	@Override
//	public void putAll(LabeledIntMap inputMap) {
//		if (inputMap == null) return;
//		for (final Entry<Label> entry : inputMap.entrySet())
//			this.put(entry.getKey(), entry.getIntValue(), null);
//	}
//
//	@Override
//	public void putAll(final LabeledIntNodeSetMap inputMap) {
//		if (inputMap == null) return;
//		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : inputMap.object2ObjectEntrySet()) {
//			this.put(entry.getKey(), entry.getValue().getValue(), entry.getValue().getNodeSet());
//		}
//	}
//
//	@Override
//	public int putForcibly(Label l, int i) {
//		return this.putForcibly(l, i, null);
//	}
//
//	@Override
//	public int putForcibly(final Label l, final int i, final Set<String> nodeSet) {
//		if ((l == null) || (i == LabeledIntMap.INT_NULL)) return LabeledIntMap.INT_NULL;
//		Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) {
//			map1 = new Object2ObjectRBTreeMap<>();
//			this.mainInt2SetMap.put(l.size(), map1);
//		}
//		ValueNodeSetPair old = map1.put(l, new ValueNodeSetPair(i, nodeSet));
//		return (old == null) ? INT_NULL : old.getValue();
//	}
//
//	@Override
//	public int remove(final Label l) {
//		if (l == null) return LabeledIntMap.INT_NULL;
//
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
//		if (map1 == null) return LabeledIntMap.INT_NULL;
//		final ValueNodeSetPair oldValue = map1.remove(l);
//		if ((oldValue != null && oldValue.getValue() != LabeledIntMap.INT_NULL) && this.optimize) {
//			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
//			// base.
//			if (this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(l)) {
//				final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(this, this.optimize);
//				this.mainInt2SetMap = newMap.mainInt2SetMap;
//				this.base = newMap.base;
//			}
//		}
//		return (oldValue == null) ? INT_NULL : oldValue.getValue();
//	}
//
//	@Override
//	public int size() {
//		int n = 0;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values())
//			n += map1.size();
//		return n;
//	}
//
//	@Override
//	public String toString() {
//		final StringBuffer sb = new StringBuffer("{");
//		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e : this.object2ObjectEntrySet()) {// I wanted a sorted print!
//			sb.append("(");
//			sb.append(e.getKey().toString());
//			sb.append(", ");
//			final int value = e.getValue().getValue();
//			if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
//				if (value < 0) sb.append('-');
//				sb.append(Constants.INFINITY_SYMBOL);
//			} else {
//				sb.append(value);
//			}
//			final Set<String> ns = e.getValue().getNodeSet();
//			if (ns != null && !ns.isEmpty()) {
//				sb.append(", ");
//				sb.append(ns.toString());
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
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
//			for (ValueNodeSetPair vnsp : mapI.values())
//				coll.add(vnsp.getValue());
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
//	private boolean checkValidityOfTheBaseAfterRemovingOrAddANodeSet(final Label l) {
//		int bn, ln;
//		if ((l == null) || ((ln = l.size()) == 0) || ((bn = this.base.size()) == 0) || (ln != bn)) return false;
//
//		int dimConj = l.conjunctionExtended(this.base).size();
//		if (dimConj > ln) return false;
//		this.base.clear();
//		return true;
//	}
//
//	/**
//	 * Tries to add all given labeled values into the current map.<br>
//	 * Recursive procedure:
//	 * 0. Given a set of labeled values to insert (all labels have the same length)
//	 * 1. For each of them, compares all same-length labels already in the map with the current one looking for if there is one with same value and only one
//	 * opposite literal
//	 * (this allows the simplification of the two labels with a shorter one). In case of a positive search, shorten the current label to insert.
//	 * 2. For each of the labeled values to insert (possibly updated), removes all labeled values in the map greater than it.
//	 * Ad each round it tries to add labeled values of the same size.
//	 *
//	 * @param inputMap contains all the elements that have to be inserted.
//	 * @return true if any element of inputMap has been inserted into the map.
//	 */
//	private boolean insertAndSimplify(final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> inputMap) {
//		if ((inputMap == null) || (inputMap.size() == 0)) return false;// recursion basement!
//
//		boolean add = false;
//		// All entries of inputMap should have label of same size.
//		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
//		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(n);
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> toAdd = new Object2ObjectRBTreeMap<>();
//		ObjectArraySet<Label> toRemove = new ObjectArraySet<>();
//
//		if (currentMapLimitedToLabelOfNSize != null) {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> inputEntry : inputMap.object2ObjectEntrySet()) {
//				final Label inputLabel = inputEntry.getKey();
//				final int inputValue = inputEntry.getValue().getValue();
//				Set<String> inputNodeSet = inputEntry.getValue().getNodeSet();
//
//				if (!isNullOrEmpty(inputNodeSet)) continue; // we simplify only with labeled value not containing node set.
//				// check is there is any labeled value with same value and only one opposite literal
//				for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : currentMapLimitedToLabelOfNSize.object2ObjectEntrySet()) {
//					final Label l1 = new Label(entry.getKey());
//					final int v1 = entry.getValue().getValue();
//					final Set<String> newNodeSet1 = inputEntry.getValue().getNodeSet();
//
//					if (!isNullOrEmpty(newNodeSet1)) continue; // we simplify only with labeled value not containing node set.
//					Literal lit = null;
//					if ((inputValue == v1) && ((lit = l1.getUniqueDifferentLiteral(inputLabel)) != null)) {
//						// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
//						toRemove.add(inputLabel);
//						toRemove.add(entry.getKey());
//						if (LOG.isLoggable(Level.FINEST)) {
//							LOG.log(Level.FINEST, "Label " + l1 + ", combined with label " + inputLabel + " induces a simplification. "
//									+ "Firstly, it removes (" + inputLabel + ", " + inputValue + ", "+inputNodeSet+")");
//						}
//						l1.removeAllLiteralsWithSameName(lit);
//						if (l1.size() < 0) throw new IllegalStateException("There is no literal to remove, there is a problem in the code!");
//						if (LOG.isLoggable(Level.FINEST)) {
//							LOG.log(Level.FINEST, "Then, it adds toAdd (" + l1 + ", " + v1 + ", "+newNodeSet1+")");
//						}
//						toAdd.put(l1, new ValueNodeSetPair(v1, inputNodeSet));
//					}
//				}
//			}
//		}
//		for (Label l : toRemove) {
//			if (LOG.isLoggable(Level.FINEST)) {
//				LOG.log(Level.FINEST, "Label " + l + " is removed from inputMap.");
//			}
//			inputMap.remove(l);
//		}
//		// inputMap has been updated. Now it contains all the elements that have to be insert.
//		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : inputMap.object2ObjectEntrySet()) {
//			final Label l1 = entry.getKey();
//			this.removeAllValuesGreaterThan(l1, entry.getValue());
//			if (this.isBaseAbleToRepresent(l1, entry.getValue())) continue;
//			this.putForcibly(l1, entry.getValue().getValue(), entry.getValue().getNodeSet());
//			add = true;
//			if (this.makeABetterBase(l1, entry.getValue())) this.removeAllValuesGreaterThanBase();
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
//	 * @param inputLabel
//	 * @param entry
//	 * @return true if <code>v</code> is greater or equal than a base component value that is subsumed by <code>l</code>. False otherwise.
//	 */
//	private boolean isBaseAbleToRepresent(final Label inputLabel, ValueNodeSetPair entry) {
//		if ((inputLabel == null) || (this.base == null) || this.base.isEmpty()) return false;
//
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(this.base.size());
//		for (final Label baseLabel : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
//			final int baseValue = map1.get(baseLabel).getValue();
////			Set<String> nodeSet1 = map1.get(l1).nodeSet;
//
//			if (baseValue == LabeledIntMap.INT_NULL) {
//				LabeledIntNodeSetTreeMap.LOG.severe("The base is not sound: base=" + this.base + ". Map1=" + map1);
//				this.base = new Label();
//				return false;
//				// throw new IllegalStateException("A base component has a null value. It is not possible.");
//			}
//			if (inputLabel.isConsistentWith(baseLabel) && (baseValue < entry.getValue())) {
//				if (entry.isNodeSetNullOrEmpty()) return true;// case 5, 7
//				entry.setValue(baseValue);// case 6
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * @param label
//	 * @param entry
//	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
//	 */
//	private boolean makeABetterBase(final Label label, ValueNodeSetPair entry) {
//		if ((label == null) || (entry.getValue() == LabeledIntMap.INT_NULL) || !entry.isNodeSetNullOrEmpty()) return false;
//
//		final int n = label.size();
//
//		if (n == 0) {
//			// The new labeled value (l,v) has universal label, the base is not more necessary!
//			this.base.clear();
//			return true;
//		}
//		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(n);
//		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
//			return false;
//		final Literal[] baseCandidateColl = label.getAllAsStraight();
//		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
//			ValueNodeSetPair pair = map1.get(label1);
//			if (pair == null || !pair.isNodeSetNullOrEmpty()) return false;
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
//	 * @param inputLabel
//	 * @param inputEntry
//	 * @return true if one element at least has been removed, false otherwise.
//	 */
//	private boolean removeAllValuesGreaterThan(final Label inputLabel, ValueNodeSetPair inputEntry) {
//		if ((inputLabel == null) || (inputEntry.getValue() == LabeledIntMap.INT_NULL)) return false;
//
//		int inputValue = inputEntry.getValue();
//
//		final int n = inputLabel.size();
//		boolean removed = false;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> internalMap : this.mainInt2SetMap.tailMap(n).values()) {
//			for (final ObjectIterator<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> currentMapIte = internalMap.object2ObjectEntrySet().iterator(); currentMapIte
//					.hasNext();) {
//				final Object2ObjectMap.Entry<Label, ValueNodeSetPair> currentEntry = currentMapIte.next();
//				final Label currentLabel = currentEntry.getKey();
//				final int currentValue = currentEntry.getValue().getValue();
//				Set<String> currentSet = currentEntry.getValue().getNodeSet();
//				if (currentLabel.subsumes(inputLabel) && (currentValue >= inputValue)) {
//					if (isNullOrEmpty(currentSet)) {
//						if (LOG.isLoggable(Level.FINEST)) {
//							LOG.log(Level.FINEST, "New label " + inputLabel + " induces a remove of (" + currentLabel + ", " + currentEntry.getValue() + ")");
//						}
//						currentMapIte.remove();
//						this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(currentLabel);
//						removed = true;
//					} else {
//						// Maybe a shorten of an inputLabel in insertAndSimplify requires to adjust further the value.
//						if (currentValue > inputValue) {
//							if (LOG.isLoggable(Level.FINEST)) {
//								LOG.log(Level.FINEST, "Label (" + inputLabel + ", " + inputEntry + ") requires to adjust label (" + currentLabel + ", "
//										+ currentEntry.getValue() + ").");
//								currentEntry.getValue().setValue(inputValue);
//							}
//						}
//					}
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
//		final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(this.optimize);
//		// build the list of labeled values that form the base
//		final ObjectArraySet<Entry<Label>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
//		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllAsStraight())) {
//			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.getValue(l)));
//		}
//		Label l1, lb;
//		int v1, vb;
//		Set<String> ns1, ns2;
//		boolean toInsert = true;
//		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
//			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : map1.object2ObjectEntrySet()) {
//				l1 = entry.getKey();
//				v1 = entry.getValue().getValue();
//				ns1 = entry.getValue().getNodeSet();
//				toInsert = false;
//				for (final Entry<Label> baseEntry : baseComponent) {
//					lb = baseEntry.getKey();
//					vb = baseEntry.getIntValue();
//					if (l1.equals(lb)) {
//						toInsert = true; // a base component has to be always insert!
//						break;
//					}
//					if (l1.isConsistentWith(lb) && ((v1 < vb) || ((v1 == vb) && !isNullOrEmpty(ns1)))) {
//						toInsert = true;
//						break;
//					}
//				}
//				if (toInsert) {
//					if (ns1 == null) {
//						ns2 = null;
//					} else {
//						ns2 = ValueNodeSetPair.newSetInstance();
//						ns2.addAll(ns1);
//					}
//					newMap.putForcibly(new Label(l1), v1, ns2);
//				}
//			}
//		}
//		if (!newMap.equals(this)) {
//			if (LOG.isLoggable(Level.FINEST)) {
//				LOG.finest("Base changed: the old map " + this.toString() + " is subsituted by " + newMap.toString());
//			}
//			this.mainInt2SetMap = newMap.mainInt2SetMap;
//			return true;
//		}
//		return false;
//	}
//
//}
