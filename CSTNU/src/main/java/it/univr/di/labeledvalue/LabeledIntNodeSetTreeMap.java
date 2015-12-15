package it.univr.di.labeledvalue;

import java.io.Serializable;
import java.util.Set;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntNodeSetMap} interface.
 * <p>
 * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
 *
 * @author Roberto Posenato
 * @see LabeledIntNodeSetMap
 * @version $Id: $Id
 */
public class LabeledIntNodeSetTreeMap implements LabeledIntNodeSetMap, Serializable {

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(LabeledIntNodeSetTreeMap.class.getName());

	
	/**
	 * 
	 */
	private static final String labelCharsRE = Constants.propositionLetterRanges+"0-9,\\-" + Constants.NOT + Constants.UNKNOWN + Constants.EMPTY_LABEL;
	/**
	 * Matcher for RE
	 */
	private static final Pattern patternlabelCharsRE = Pattern.compile("\\{[\\(" + labelCharsRE + "\\) ]*\\}");

	/**
	 * 
	 */
	private static final Pattern splitterEntry = Pattern.compile("\\{\\}|[{(]+|\\) [(}]*");
	/**
	 * 
	 */
	private static final Pattern splitterPair = Pattern.compile(", ");

	/**
	 *
	 */
	static private final long serialVersionUID = 1L;

	/**
	 * Main.
	 *
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	static public void main(final String[] args) {

		final int nTest = (int) 1E3;
		final double msNorm = 1.0E6 * nTest;

		final LabeledIntNodeSetMap map = new LabeledIntNodeSetTreeMap(true);

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
			map.put(Label.emptyLabel, 109);
			map.put(l1, 10);
			map.put(l2, 20);
			map.put(l3, 25);
			map.put(l4, 23);
			map.put(l5, 22);
			map.put(l6, 23);
			map.put(l7, 20);
			map.put(l8, 20);
			map.put(l9, 21);
			map.put(l10, 11);
			map.put(l11, 11);
			map.put(l12, 11);
			map.put(l13, 24);
			map.put(l14, 22);
			map.put(l15, 23);
			map.put(l16, 20);
			map.put(l17, 23);
			map.put(l18, 23);
			map.put(l19, 23);
			map.put(l20, 23);
		}
		long endTime = System.nanoTime();
		System.out.println("LABELED VALUE SET WITH NODE SET\nExecution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map + ".\nTime: (ms): "
				+ ((endTime - startTime) / msNorm));
		String rightAnswer = "{(⊡, 23) (¬a¿bf, 20) (abcdef, 20) (abc¬f, 10) (abd¿f, 11) (a¿d¬f, 11) (ae¬f, 20) (b¬d¿ef, 22) (c, 22) (c¬e, 11) }";
		System.out.println("The right final set is " + rightAnswer + ".");
		System.out.println("Is equal? " + AbstractLabeledIntMap.parse(rightAnswer).equals(map));

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
			min = map.getValue(l);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		map.put(Label.parse("c"), 11);
		map.put(Label.parse("¬c"), 11);
		System.out.println("After the insertion of (c,11) and (¬c,11) the map becomes: " + map);
	}

	/**
	 * Parse a string representing a LabeledValueTreeMap and return an object containing the labeled values represented by
	 * the string.<br>
	 * The format of the string is given by the method {@link #toString()}:<br>
	 * {\[(&lt;key&gt;, &lt;value&gt;) \]*}
	 *
	 * @param inputMap a {@link java.lang.String} object.
	 * @param withOptimization true if the redundant labeled values have to be removed.
	 * @return a LabeledValueTreeMap object if <code>inputMap</code> represents a valid map, null otherwise.
	 */
	static public LabeledIntNodeSetTreeMap parse(final String inputMap, final boolean withOptimization) {
		if (inputMap == null) return null;

		if (!patternlabelCharsRE.matcher(inputMap).matches()) {
			LOG.warning("Input string is not well formed for representing a proposition");
			return null;
		}

		final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(withOptimization);

		final String[] entryPair = splitterEntry.split(inputMap);
		// LabeledValueTreeMap.LOG.finest("EntryPairs: " + Arrays.toString(entryPair));
		for (final String s : entryPair) {
			LabeledIntNodeSetTreeMap.LOG.finest("s: " + s);
			if (s.length() != 0) {
				final String[] labInt = splitterPair.split(s);
				// LabeledValueTreeMap.LOG.finest("labInt: " + Arrays.toString(labInt));
				newMap.put(Label.parse(labInt[0]), Integer.parseInt(labInt[1]), null);
			}
		}
		return newMap;
	}

	/**
	 * Determines the sum of 'a' and 'b'.
	 * If any of them is already INFINITY, returns INFINITY.
	 *
	 * If the sum is greater/lesser than the maximum/minimum integer representable by a int, it throw an IllegalStateException because the overflow.
	 * I don't use Overflow exception because it requires to use a try{} catch section.
	 *
	 * @param a a int.
	 * @param b a int.
	 * @return the controlled sum.
	 * @throws java.lang.ArithmeticException if any.
	 */
	static public final int sumWithOverflowCheck(final int a, final int b) throws ArithmeticException {
		if ((a == Constants.INT_NEG_INFINITE) || (b == Constants.INT_NEG_INFINITE)) return Constants.INT_NEG_INFINITE;
		if ((a == Constants.INT_POS_INFINITE) || (b == Constants.INT_POS_INFINITE)) return Constants.INT_POS_INFINITE;

		final long sum = (long) a + (long) b;// CAST IS NECESSARY!
		if ((sum >= Constants.INT_POS_INFINITE) || (sum <= Constants.INT_NEG_INFINITE))
			throw new ArithmeticException("Integer overflow in a sum of labeled values: " + a + " + " + b);
		return (int) sum;
	}

	@SuppressWarnings("javadoc")
	private static final boolean isNullOrEmpty(Set<String> nodeSet) {
		return nodeSet == null || nodeSet.isEmpty();
	}

	/**
	 * Label forming a base for the labels of the map.
	 * All literal are straight.
	 */
	private Label base;

	/**
	 * Design choice: the set of labeled values of this map is organized as a collection of sets each containing labels of the same length.
	 * This allows the label minimization task to be performed in a more systematic and, possibly, efficient way.
	 * The efficiency has been proved comparing this implementation with one in which the map has been realized with a standard map and the minimization task
	 * determines the same length labels every time it needs it.
	 */
	private Int2ObjectAVLTreeMap<Object2ObjectRBTreeMap<Label, ValueNodeSetPair>> mainInt2SetMap;
//	private SortedMap<Integer,Object2ObjectRBTreeMap<Label, ValueNodeSetPair>> mainInt2SetMap;

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 */
	private final boolean optimize;

	/**
	 * Simple constructor.
	 * The internal structure is built and empty.
	 *
	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
	 */
	public LabeledIntNodeSetTreeMap(final boolean withOptimization) {
		this.mainInt2SetMap = new Int2ObjectAVLTreeMap<>();
//		this.mainInt2SetMap = new TreeMap<>();
		this.base = new Label();
		this.optimize = withOptimization;
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 * @param withOptimization true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
	 */
	public LabeledIntNodeSetTreeMap(final LabeledIntNodeSetMap lvm, final boolean withOptimization) {
		this(withOptimization);
		if (lvm == null) return;
		SortedSet<String> s, inputS;
		if (withOptimization) {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : lvm.object2ObjectEntrySet()) {
				if ((inputS = entry.getValue().getNodeSet()) != null) {
					s = ValueNodeSetPair.newSetInstance();
					s.addAll(inputS);
				} else {
					s = null;
				}
				// ANNULLATO: Ci sono dei problemi nel clonare LabeledIntNodeSetMap non ottime. Per ora copio in modo identico
				this.putForcibly(new Label(entry.getKey()), entry.getValue().getValue(), s);
			}
			base = ((LabeledIntNodeSetTreeMap) lvm).base;
		} else {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : lvm.object2ObjectEntrySet()) {
				if ((inputS = entry.getValue().getNodeSet()) != null) {
					s = ValueNodeSetPair.newSetInstance();
					s.addAll(inputS);
				} else {
					s = null;
				}
				this.putForcibly(new Label(entry.getKey()), entry.getValue().getValue(), s);
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.mainInt2SetMap.clear();
		this.base.clear();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Entry<Label>> entrySet() {
		final Object2IntRBTreeMap<Label> coll = new Object2IntRBTreeMap<>();
		coll.defaultReturnValue(LabeledIntNodeSetMap.INT_NULL);
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e1 : mapI.object2ObjectEntrySet())
				coll.put(e1.getKey(), e1.getValue().getValue());
		}
		return coll.object2IntEntrySet();
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(final Object o) {
		if ((o == null) || !(o instanceof LabeledIntNodeSetMap)) return false;
		final LabeledIntNodeSetMap lvm = ((LabeledIntNodeSetMap) o);
		if (this.size() != lvm.size()) return false;
		return this.object2ObjectEntrySet().equals(lvm.object2ObjectEntrySet());// Two maps are equals if they contain the same set of values. The internal
																				// representation of optimization is not important!.
	}

	/** {@inheritDoc} */
	@Override
	public ValueNodeSetPair get(final Label l) {
		if (l == null) return null;
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) return null;
		return map1.get(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValue() {
		int v = Constants.INT_POS_INFINITE;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
			for (final ValueNodeSetPair i : map1.values())
				if (v > i.getValue()) v = i.getValue();
		}
		return (v == Constants.INT_POS_INFINITE) ? LabeledIntNodeSetMap.INT_NULL : v;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueAmongLabelsWOUnknown() {
		int v = Constants.INT_POS_INFINITE, i;
		Label l;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : map1.object2ObjectEntrySet()) {
				l = entry.getKey();
				if (l.containsUnknown()) continue;
				i = entry.getValue().getValue();
				if (v > i) v = i;
			}
		}
		return (v == Constants.INT_POS_INFINITE) ? LabeledIntNodeSetMap.INT_NULL : v;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		if (l == null) return LabeledIntNodeSetMap.INT_NULL;
		int min = this.getValue(l);
		if (min == LabeledIntNodeSetMap.INT_NULL) {
			// the label does not exits, try all consistent labels
			min = Constants.INT_POS_INFINITE;
			int v1;
			Label l1 = null;
			for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
				for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e : mapI.object2ObjectEntrySet()) {
					l1 = e.getKey();
					if (l.isConsistentWith(l1)) {
						v1 = e.getValue().getValue();
						if (min > v1) min = v1;
					}
				}
			}
		}
		// if (v == INT_NULL) {
		// LOG.finest("There is no consistent labeled value for the given label " + l.toString());
		// }
		return (min == Constants.INT_POS_INFINITE) ? LabeledIntNodeSetMap.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public SortedSet<String> getNodeSet(final Label l) {
		if (l == null) return null;
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) return null;
		ValueNodeSetPair vnsp = map1.get(l);
		if (vnsp == null) return null;
		return vnsp.getNodeSet();
	}

	/** {@inheritDoc} */
	@Override
	public int getValue(final Label l) {
		if (l == null) return LabeledIntNodeSetMap.INT_NULL;
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
		ValueNodeSetPair e = null;
		if (map1 == null || ((e = map1.get(l)) == null)) return LabeledIntNodeSetMap.INT_NULL;
		return e.getValue();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.mainInt2SetMap.hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public Set<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> object2ObjectEntrySet() {
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> coll = new Object2ObjectRBTreeMap<>();
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
			coll.putAll(mapI);
		}
		return coll.object2ObjectEntrySet();
	}

	/** {@inheritDoc} */
	@Override
	public boolean put(Label l, int i) {
		return this.put(l, i, null);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Adds the pair &lang;l,i&rang;.<br>
	 * Moreover, tries to eliminate all labels that are redundant.
	 */
	@SuppressWarnings("null")
	@Override
	public boolean put(final Label newLabel, int newValue, Set<String> newNodeSet) {
		/*
		 * This version of the method is very redundant but simple to check!
		 */
		if ((newLabel == null) || (newValue == LabeledIntNodeSetMap.INT_NULL)) return false;
		/*
		 * 1. Check if there is already a value in the map that represents the new value and the node set (in this case, return false)
		 * or if one or more value in the map can be removed by the insertion of the new value (in this case remove such values).
		 */
		Label l1;
		int v1;
		SortedSet<String> nodeSet1;

		final int newLabelSize = newLabel.size();
		int l1Size = 0;
		ValueNodeSetPair vnspair;
		Set<Label> labelToRemove = new ObjectArraySet<>();
		Set<Label> labelToAdjust = new ObjectArraySet<>();
		boolean hasNewLabelToBeInserted = true;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapAllSameLenghLabels : this.mainInt2SetMap.values()) {
			for (Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : mapAllSameLenghLabels.object2ObjectEntrySet()) {
				l1 = entry.getKey();
				vnspair = entry.getValue();
				v1 = vnspair.getValue();
				nodeSet1 = vnspair.getNodeSet();
				if ((newLabelSize >= (l1Size = l1.size())) && newLabel.subsumes(l1) && newValue >= v1) {
					if (isNullOrEmpty(newNodeSet) || (newLabel.equals(l1) && !isNullOrEmpty(nodeSet1) && nodeSet1.containsAll(newNodeSet))) {
						// the input value is simple (no associated node set) and it is already contained in the set.
						// or it has a node set but that it is already represented by the current value
						return false;
					}
					if (v1 == Constants.INT_NEG_INFINITE) {
						// the input value is not simple because has a node set but the subsume an -infinity labeled value, we can continue to
						// check with others but making it simple. It will then not insert. (we continue in order to make a sanity check in the rest of the set)
						newNodeSet = null;
						hasNewLabelToBeInserted = false;
					}
					newValue = v1;// the subsumed value is better than the new one but, since the new one has a node set, it has to be inserted.
					// we insert it but with the value v1 that it is more negative.
				}
				if ((newLabelSize <= l1Size) && l1.subsumes(newLabel) && (newValue <= v1)) {
					if (newValue == v1 && newLabel.equals(l1) && newValue == Constants.INT_NEG_INFINITE) {
						// the value is already in the set
						hasNewLabelToBeInserted = false;
						continue;
					}
					if (isNullOrEmpty(nodeSet1) || (newLabel.equals(l1) && !isNullOrEmpty(newNodeSet) && newNodeSet.equals(nodeSet1))) {
						labelToRemove.add(l1);// case 1, 2
						// l1 is more generic and simple, we remove it
						// this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(l1, nodeSet1);
					} else {
						if (newLabel.equals(l1)) {
							// The case of the same label when one of two labels has a node set is managed here
							if (newValue == v1 && nodeSet1.equals(newNodeSet)) {
								hasNewLabelToBeInserted = false;
								continue;
							}
							if (newValue != Constants.INT_NEG_INFINITE) {
								// Negative Infinity can be propagated without node set
								if (newNodeSet == null) newNodeSet = ValueNodeSetPair.newSetInstance();
								newNodeSet.addAll(nodeSet1);
							}
							labelToRemove.add(l1);
						} else {
							// The already labeled value (that it is not simple) has to be adjusted if it has a greater value.
							if (newValue < v1) labelToAdjust.add(l1);
						}
					}
				}
			}
		}

		// The label to remove and to adjust are now ready to be managed.
		for (Label lr : labelToRemove) {
			Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map = this.mainInt2SetMap.get(lr.size());
			if (LOG.isLoggable(Level.FINEST)) LOG.log(Level.FINEST,
					"The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") forces the removal of (" + lr + ", " + map.get(lr) + ").");
			map.remove(lr);
			this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(lr);
		}
		for (Label lr : labelToAdjust) {
			Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map = this.mainInt2SetMap.get(lr.size());
			ValueNodeSetPair vnspair1 = map.get(lr);
			if (LOG.isLoggable(Level.FINEST)) LOG.log(Level.FINEST,
					"The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") forces the modification of (" + lr + ", " + vnspair1 + ") to ("
							+ lr + ", " + newValue + ", " + ((newValue == Constants.INT_NEG_INFINITE) ? "null" : vnspair1.getNodeSet()));
			vnspair1.setValue(newValue);
			if (newValue == Constants.INT_NEG_INFINITE) vnspair1.setNodeSet(null);
		}

		if (!hasNewLabelToBeInserted) {
			if (LOG.isLoggable(Level.FINEST))
				LOG.log(Level.FINEST, "The new value (" + newLabel + ", " + newValue + ", " + newNodeSet + ") has not to be inserted.");
			return false;
		}
		/*
		 * 2. If optimization is request,
		 * Insert the new value and check if it possible to simplify with some other label with same value and only one different literals.
		 */
		if (this.optimize) {
			final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> a = new Object2ObjectRBTreeMap<>();
			a.put(newLabel, new ValueNodeSetPair(newValue, newNodeSet));
			return this.insertAndSimplify(a);
		}

		/*
		 * 2.1 Otherwise, put the new value and return if there was an old one.
		 */
		this.putForcibly(newLabel, newValue, newNodeSet);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void putAll(final LabeledIntNodeSetMap inputMap) {
		if (inputMap == null) return;
		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : inputMap.object2ObjectEntrySet()) {
			this.put(entry.getKey(), entry.getValue().getValue(), entry.getValue().getNodeSet());
		}
	}

	/** {@inheritDoc} */
	@Override
	public int putForcibly(Label l, int i) {
		return this.putForcibly(l, i, null);
	}

	/** {@inheritDoc} */
	@Override
	public int putForcibly(final Label l, final int i, final Set<String> nodeSet) {
		if ((l == null) || (i == LabeledIntNodeSetMap.INT_NULL)) return LabeledIntNodeSetMap.INT_NULL;
		Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) {
			map1 = new Object2ObjectRBTreeMap<>();
			this.mainInt2SetMap.put(l.size(), map1);
		}
		ValueNodeSetPair old = map1.put(l, new ValueNodeSetPair(i, nodeSet));
		return (old == null) ? INT_NULL : old.getValue();
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		if (l == null) return LabeledIntNodeSetMap.INT_NULL;

		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) return LabeledIntNodeSetMap.INT_NULL;
		final ValueNodeSetPair oldValue = map1.remove(l);
		if ((oldValue != null && oldValue.getValue() != LabeledIntNodeSetMap.INT_NULL) && this.optimize) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.
			if (this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(l)) {
				final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(this, this.optimize);
				this.mainInt2SetMap = newMap.mainInt2SetMap;
				this.base = newMap.base;
			}
		}
		return (oldValue == null) ? INT_NULL : oldValue.getValue();
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		int n = 0;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values())
			n += map1.size();
		return n;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("{");
		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> e : this.object2ObjectEntrySet()) {// I wanted a sorted print!
			sb.append("(");
			sb.append(e.getKey().toString());
			sb.append(", ");
			final int value = e.getValue().getValue();
			if ((value == Constants.INT_NEG_INFINITE) || (value == Constants.INT_POS_INFINITE)) {
				if (value < 0) sb.append('-');
				sb.append(Constants.INFINITY_SYMBOL);
			} else {
				sb.append(value);
			}
			final SortedSet<String> ns = e.getValue().getNodeSet();
			if (ns != null && !ns.isEmpty()) {
				sb.append(", ");
				sb.append(ns.toString());
			}
			sb.append(") ");
		}
		sb.append("}");
		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		final IntArraySet coll = new IntArraySet(this.size());
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
			for (ValueNodeSetPair vnsp : mapI.values())
				coll.add(vnsp.getValue());
		}
		return coll;
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	public ObjectArraySet<Label> keys() {
		ObjectArraySet<Label> coll = new ObjectArraySet<>();
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> mapI : this.mainInt2SetMap.values()) {
			coll.addAll(mapI.keySet());
		}
		return coll;
	}
	/**
	 * If the removed label <code>l</code> is a component of the base, then reset the base.
	 *
	 * @param l
	 * @return true if <code>l</code> is a component of the base, false otherwise.
	 */
	private boolean checkValidityOfTheBaseAfterRemovingOrAddANodeSet(final Label l) {
		int bn, ln;
		if ((l == null) || ((ln = l.size()) == 0) || ((bn = this.base.size()) == 0) || (ln != bn)) return false;

		if (l.containsUnknown()) return false;// removing a label with unknown does not affect a possible base.

		int dimConj = l.conjunctionExtended(this.base).size();
		if (dimConj > ln) return false;
		this.base.clear();
		return true;
	}

	/**
	 * Tries to add all given labeled values into the current map.<br>
	 * Recursive procedure:
	 * 0. Given a set of labeled values to insert (all labels have the same length)
	 * 1. For each of them, compares all same-length labels already in the map with the current one looking for if there is one with same value and only one
	 * opposite literal
	 * (this allows the simplification of the two labels with a shorter one). In case of a positive search, shorten the current label to insert.
	 * 2. For each of the labeled values to insert (possibly updated), removes all labeled values in the map greater than it.
	 * Ad each round it tries to add labeled values of the same size.
	 *
	 * @param inputMap contains all the elements that have to be inserted.
	 * @return true if any element of inputMap has been inserted into the map.
	 */
	private boolean insertAndSimplify(final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> inputMap) {
		if ((inputMap == null) || (inputMap.size() == 0)) return false;// recursion basement!

		boolean add = false;
		// All entries of inputMap should have label of same size.
		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(n);
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> toAdd = new Object2ObjectRBTreeMap<>();
		ObjectArraySet<Label> toRemove = new ObjectArraySet<>();

		if (currentMapLimitedToLabelOfNSize != null) {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> inputEntry : inputMap.object2ObjectEntrySet()) {
				final Label inputLabel = inputEntry.getKey();
				final int inputValue = inputEntry.getValue().getValue();
				SortedSet<String> inputNodeSet = inputEntry.getValue().getNodeSet();

				if (!isNullOrEmpty(inputNodeSet)) continue; // we simplify only with labeled value not containing node set.
				// check is there is any labeled value with same value and only one opposite literal
				for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : currentMapLimitedToLabelOfNSize.object2ObjectEntrySet()) {
					final Label l1 = new Label(entry.getKey());
					final int v1 = entry.getValue().getValue();
					final SortedSet<String> newNodeSet1 = inputEntry.getValue().getNodeSet();

					if (!isNullOrEmpty(newNodeSet1)) continue; // we simplify only with labeled value not containing node set.
					Literal lit = null;
					if ((inputValue == v1) && ((lit = l1.getUniqueDifferentLiteral(inputLabel)) != null)) {
						// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
						toRemove.add(inputLabel);
						toRemove.add(entry.getKey());
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "Label " + l1 + ", combined with label " + inputLabel + " induces a simplification. "
									+ "Firstly, it removes (" + inputLabel + ", " + inputValue + ", " + inputNodeSet + ")");
						}
						l1.removeAllLiteralsWithSameName(lit);
						if (l1.size() < 0) throw new IllegalStateException("There is no literal to remove, there is a problem in the code!");
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "Then, it adds toAdd (" + l1 + ", " + v1 + ", " + newNodeSet1 + ")");
						}
						toAdd.put(l1, new ValueNodeSetPair(v1, inputNodeSet));
					}
				}
			}
		}
		for (Label l : toRemove) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label " + l + " is removed from inputMap.");
			}
			inputMap.remove(l);			// FIXME dobbiamo togliere anche da currentMap?
		}
		// inputMap has been updated. Now it contains all the elements that have to be insert.
		for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : inputMap.object2ObjectEntrySet()) {
			final Label l1 = entry.getKey();
			this.removeAllValuesGreaterThan(l1, entry.getValue());
			if (this.isBaseAbleToRepresent(l1, entry.getValue())) continue;
			this.putForcibly(l1, entry.getValue().getValue(), entry.getValue().getNodeSet());
			add = true;
			if (this.makeABetterBase(l1, entry.getValue())) this.removeAllValuesGreaterThanBase();
		}
		if (toAdd.size() > 0) add = this.insertAndSimplify(toAdd) || add;
		return add;
	}

	/**
	 * Determines whether the value can be represented by any component of the base.
	 * A component of the base can represent the labeled value <code>(l,v)</code> if <code>l</code> subsumes the component label
	 * and the <code>v</code> is greater or equal the component value.
	 *
	 * @param inputLabel
	 * @param entry
	 * @return true if <code>v</code> is greater or equal than a base component value that is subsumed by <code>l</code>. False otherwise.
	 */
	private boolean isBaseAbleToRepresent(final Label inputLabel, ValueNodeSetPair entry) {
		if ((inputLabel == null) || (this.base == null) || this.base.isEmpty()) return false;

		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(this.base.size());
		for (final Label baseLabel : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
			final int baseValue = map1.get(baseLabel).getValue();
//			SortedSet<String> nodeSet1 = map1.get(l1).nodeSet;

			if (baseValue == LabeledIntNodeSetMap.INT_NULL) {
				LabeledIntNodeSetTreeMap.LOG.severe("The base is not sound: base=" + this.base + ". Map1=" + map1);
				this.base = new Label();
				return false;
				// throw new IllegalStateException("A base component has a null value. It is not possible.");
			}
			if (inputLabel.isConsistentWith(baseLabel) && (baseValue < entry.getValue())) {
				if (entry.isNodeSetNullOrEmpty()) return true;// case 5, 7
				entry.setValue(baseValue);// case 6
			}
		}
		return false;
	}

	/**
	 * @param label
	 * @param entry
	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
	 */
	private boolean makeABetterBase(final Label label, ValueNodeSetPair entry) {
		if ((label == null) || (entry.getValue() == LabeledIntNodeSetMap.INT_NULL) || !entry.isNodeSetNullOrEmpty()) return false;

		final int n = label.size();

		if (n == 0) {
			// The new labeled value (l,v) has universal label, the base is not more necessary!
			this.base.clear();
			return true;
		}
		final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 = this.mainInt2SetMap.get(n);
		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
			return false;
		final Literal[] baseCandidateColl = label.getAllAsStraight();
		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
			ValueNodeSetPair pair = map1.get(label1);
			if (pair == null || !pair.isNodeSetNullOrEmpty()) return false;
		}
		final Label baseCandidate = new Label();
		for (final Literal l : baseCandidateColl) {
			baseCandidate.conjunct(l);
		}
		this.base = baseCandidate;
		return true;
	}

	/**
	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
	 *
	 * @param inputLabel
	 * @param inputEntry
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThan(final Label inputLabel, ValueNodeSetPair inputEntry) {
		if ((inputLabel == null) || (inputEntry.getValue() == LabeledIntNodeSetMap.INT_NULL)) return false;

		int inputValue = inputEntry.getValue();

		final int n = inputLabel.size();
		boolean removed = false;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> internalMap : this.mainInt2SetMap.tailMap(n).values()) {
			for (final ObjectIterator<Object2ObjectMap.Entry<Label, ValueNodeSetPair>> currentMapIte = internalMap.object2ObjectEntrySet().iterator(); currentMapIte
					.hasNext();) {
				final Object2ObjectMap.Entry<Label, ValueNodeSetPair> currentEntry = currentMapIte.next();
				final Label currentLabel = currentEntry.getKey();
				final int currentValue = currentEntry.getValue().getValue();
				SortedSet<String> currentSet = currentEntry.getValue().getNodeSet();
				if (currentLabel.subsumes(inputLabel) && (currentValue >= inputValue)) {
					if (isNullOrEmpty(currentSet)) {
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "New label " + inputLabel + " induces a remove of (" + currentLabel + ", " + currentEntry.getValue() + ")");
						}
						currentMapIte.remove();
						this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(currentLabel);
						removed = true;
					} else {
						// Maybe a shorten of an inputLabel in insertAndSimplify requires to adjust further the value.
						if (currentValue > inputValue) {
							if (LOG.isLoggable(Level.FINEST))
								LOG.log(Level.FINEST, "Label (" + inputLabel + ", " + inputEntry + ") requires to adjust label (" + currentLabel + ", "
										+ currentEntry.getValue() + ").");
							if (inputValue == Constants.INT_NEG_INFINITE) {
								currentMapIte.remove();
								this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(currentLabel);
								removed = true;
							} else {
								currentEntry.getValue().setValue(inputValue);
							}
						}
					}
				}
			}
		}
		return removed;
	}

	/**
	 * Remove all labeled values having each value greater than all values of base components consistent with it.
	 *
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThanBase() {
		if ((this.base == null) || (this.base.size() == 0)) return false;
		final LabeledIntNodeSetTreeMap newMap = new LabeledIntNodeSetTreeMap(this.optimize);
		// build the list of labeled values that form the base
		final ObjectArraySet<Entry<Label>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllAsStraight())) {
			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.getValue(l)));
		}
		Label l1, lb;
		int v1, vb;
		SortedSet<String> ns1, ns2;
		boolean toInsert = true;
		for (final Object2ObjectRBTreeMap<Label, ValueNodeSetPair> map1 : this.mainInt2SetMap.values()) {
			for (final Object2ObjectMap.Entry<Label, ValueNodeSetPair> entry : map1.object2ObjectEntrySet()) {
				l1 = entry.getKey();
				v1 = entry.getValue().getValue();
				ns1 = entry.getValue().getNodeSet();
				toInsert = false;
				for (final Entry<Label> baseEntry : baseComponent) {
					lb = baseEntry.getKey();
					vb = baseEntry.getIntValue();
					if (l1.equals(lb)) {
						toInsert = true; // a base component has to be always insert!
						break;
					}
					if (l1.isConsistentWith(lb) && ((v1 < vb) || ((v1 == vb) && !isNullOrEmpty(ns1)))) {
						toInsert = true;
						break;
					}
				}
				if (toInsert) {
					if (ns1 == null) {
						ns2 = null;
					} else {
						ns2 = ValueNodeSetPair.newSetInstance();
						ns2.addAll(ns1);
					}
					newMap.putForcibly(new Label(l1), v1, ns2);
				}
			}
		}
		if (!newMap.equals(this)) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Base changed: the old map " + this.toString() + " is subsituted by " + newMap.toString());
			}
			this.mainInt2SetMap = newMap.mainInt2SetMap;
			return true;
		}
		return false;
	}

}
