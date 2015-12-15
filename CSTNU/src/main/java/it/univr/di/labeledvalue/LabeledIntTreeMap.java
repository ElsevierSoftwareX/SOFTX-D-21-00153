package it.univr.di.labeledvalue;

import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntMap} interface.
 * <p>
 * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
 *
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
public class LabeledIntTreeMap extends AbstractLabeledIntMap {

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(LabeledIntTreeMap.class.getName());

	/**
	 *
	 */
	static private final long serialVersionUID = 2L;

	/**
	 * Main.
	 *
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	static public void main(final String[] args) {

		final int nTest = (int) 1E3;
		final double msNorm = 1.0E6 * nTest;

		final LabeledIntMap map = new LabeledIntTreeMap(true);

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
		System.out.println("LABELED VALUE SET-TREE MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map + ".\nTime: (ms): "
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
			min = map.get(l);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		map.put(Label.parse("c"), 11);
		map.put(Label.parse("¬c"), 11);
		System.out.println("After the insertion of (c,11) and (¬c,11) the map becomes: " + map);
	}

	/**
	 * Label forming a base for the labels of the map. All literal are straight.
	 */
	private Label base;

	/**
	 * Design choice: the set of labeled values of this map is organized as a collection of sets each containing labels of the same length. This allows the
	 * label minimization task to be performed in a more systematic and, possibly, efficient way. The efficiency has been proved comparing this implementation
	 * with one in which the map has been realized with a standard map and the minimization task determines the same length labels every time it needs it.
	 */
	private Int2ObjectAVLTreeMap<Object2IntRBTreeMap<Label>> mainInt2SetMap;

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 */
	private final boolean optimize;

	/**
	 * Simple constructor. Label are optimized.
	 */
	LabeledIntTreeMap() {
		this(true);
	}

	/**
	 * Simple constructor. The internal structure is built and empty.
	 *
	 * @param withOptimization
	 *            true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
	 */
	LabeledIntTreeMap(final boolean withOptimization) {
		this.mainInt2SetMap = new Int2ObjectAVLTreeMap<>();
		this.base = new Label();
		this.optimize = withOptimization;
	}

	/**
	 * @param lvm
	 */
	LabeledIntTreeMap(final LabeledIntMap lvm) {
		this(lvm, true);
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm
	 *            the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 * @param withOptimization
	 *            true if the redundant labeled values must be always removed. It checks the redundancy at every operation.
	 */
	LabeledIntTreeMap(final LabeledIntMap lvm, final boolean withOptimization) {
		this(withOptimization);
		if (lvm == null)
			return;
		if (withOptimization) {
			for (final Entry<Label> entry : lvm.entrySet()) {
				this.putForcibly(entry.getKey(), entry.getIntValue());
			}
			base = ((LabeledIntTreeMap) lvm).base;
		} else {
			for (final Entry<Label> entry : lvm.entrySet()) {
				this.putForcibly(entry.getKey(), entry.getIntValue());
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
	public ObjectSet<Entry<Label>> entrySet() {
		final ObjectSet<Entry<Label>> coll = new ObjectArraySet<>();
		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
			coll.addAll(mapI.object2IntEntrySet());
		}
		return coll;
	}

	/** {@inheritDoc} */
	@Override
	public int get(final Label l) {
		if (l == null)
			return INT_NULL;
		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null)
			return INT_NULL;
		return map1.getInt(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValue() {
		int min = Constants.INT_POS_INFINITE;
		int i;
		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
			for (IntIterator j = (mapI.values()).iterator(); j.hasNext();)
				if (min > (i = j.nextInt()))
					min = i;
		}
		return (min == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.mainInt2SetMap.hashCode();
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	public ObjectSet<Label> keySet() {
		ObjectSet<Label> coll = new ObjectArraySet<>();
		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
			coll.addAll(mapI.keySet());
		}
		return coll;
	}

	/**
	 * {@inheritDoc} Adds the pair &lang;l,i&rang;.<br>
	 * Moreover, tries to eliminate all labels that are redundant.
	 */
	@Override
	public boolean put(final Label newLabel, int newValue) {
		/*
		 * This version of the method is very redundant but simple to check!
		 */
		if ((newLabel == null) || (newValue == LabeledIntMap.INT_NULL))
			return false;
		/*
		 * 1. Check if there is already a value in the map that represents the new value and the node set (in this case, return false) or if one or more value
		 * in the map can be removed by the insertion of the new value (in this case remove such values).
		 */
		Label l1;
		int v1;

		final int newLabelSize = newLabel.size();
		int l1Size = 0;
		ObjectSet<Label> labelToRemove = new ObjectArraySet<>();
		ObjectSet<Label> labelToAdjust = new ObjectArraySet<>();
		boolean hasNewLabelToBeInserted = true;
		for (final Object2IntRBTreeMap<Label> mapAllSameLenghLabels : this.mainInt2SetMap.values()) {
			for (Entry<Label> entry : mapAllSameLenghLabels.object2IntEntrySet()) {
				l1 = entry.getKey();
				v1 = entry.getIntValue();
				if ((newLabelSize >= (l1Size = l1.size())) && newLabel.subsumes(l1) && newValue >= v1) {
					// if (v1 == Constants.INT_NEG_INFINITE) {
					// // the input value is not simple because has a node set but the subsume an -infinity labeled value, we can continue to
					// // check with others but making it simple. It will then not insert. (we continue in order to make a sanity check in the rest of the set)
					// newNodeSet = null;
					// hasNewLabelToBeInserted = false;
					// }
					return false;
				}
				if ((newLabelSize <= l1Size) && l1.subsumes(newLabel) && (newValue <= v1)) {
					if (newValue == v1 && newLabel.equals(l1) && newValue == Constants.INT_NEG_INFINITE) {
						// the value is already in the set
						hasNewLabelToBeInserted = false;
						continue;
					}
					if (newLabel.equals(l1)) {
						labelToRemove.add(l1);
					} else {
						// The already labeled value (that it is not simple) has to be adjusted if it has a greater value.
						if (newValue < v1)
							labelToAdjust.add(l1);
					}
				}
			}
		}

		// The label to remove and to adjust are now ready to be managed.
		for (Label lr : labelToRemove) {
			Object2IntRBTreeMap<Label> map = this.mainInt2SetMap.get(lr.size());
			if (LOG.isLoggable(Level.FINEST))
				LOG.log(Level.FINEST, "The new value (" + newLabel + ", " + newValue + ") forces the removal of (" + lr + ", " + map.get(lr) + ").");
			map.remove(lr);
			this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(lr);
		}
		for (Label lr : labelToAdjust) {
			Object2IntRBTreeMap<Label> map = this.mainInt2SetMap.get(lr.size());
			int value = map.getInt(lr);
			if (LOG.isLoggable(Level.FINEST))
				LOG.log(Level.FINEST, "The new value (" + newLabel + ", " + newValue + ") forces the modification of (" + lr + ", " + value + ") to ("
						+ lr + ", " + newValue + ")");
			map.put(lr, newValue);
		}

		if (!hasNewLabelToBeInserted) {
			if (LOG.isLoggable(Level.FINEST))
				LOG.log(Level.FINEST, "The new value (" + newLabel + ", " + newValue + ") has not to be inserted.");
			return false;
		}
		/*
		 * 2. If optimization is request, Insert the new value and check if it possible to simplify with some other label with same value and only one different
		 * literals.
		 */
		if (this.optimize) {
			final Object2IntRBTreeMap<Label> a = new Object2IntRBTreeMap<>();
			a.defaultReturnValue(INT_NULL);
			a.put(newLabel, newValue);
			return this.insertAndSimplify(a);
		}

		/*
		 * 2.1 Otherwise, put the new value and return if there was an old one.
		 */
		this.putForcibly(newLabel, newValue);
		return true;
	}

	/**
	 * @param l
	 * @param i
	 * @return previous value if present, INT_NULL otherwise;
	 */
	public int putForcibly(final Label l, final int i) {
		if ((l == null) || (i == LabeledIntMap.INT_NULL))
			return LabeledIntMap.INT_NULL;
		Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) {
			map1 = new Object2IntRBTreeMap<>();
			map1.defaultReturnValue(INT_NULL);
			this.mainInt2SetMap.put(l.size(), map1);
		}
		int old = map1.put(l, i);
		return old;
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		if (l == null)
			return LabeledIntMap.INT_NULL;

		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null)
			return LabeledIntMap.INT_NULL;
		final int oldValue = map1.removeInt(l);
		if (oldValue != INT_NULL && this.optimize) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.
			if (this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(l)) {
				final LabeledIntTreeMap newMap = new LabeledIntTreeMap(this, this.optimize);
				this.mainInt2SetMap = newMap.mainInt2SetMap;
				this.base = newMap.base;
			}
		}
		return oldValue;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		int n = 0;
		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values())
			n += map1.size();
		return n;
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		final IntArraySet coll = new IntArraySet();
		for (final Object2IntRBTreeMap<Label> mapI : this.mainInt2SetMap.values()) {
			for (Entry<Label> i : mapI.object2IntEntrySet())
				coll.add(i.getIntValue());
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
		if ((l == null) || ((ln = l.size()) == 0) || ((bn = this.base.size()) == 0) || (ln != bn))
			return false;

		if (l.containsUnknown())
			return false;// removing a label with unknown does not affect a possible base.

		int dimConj = l.conjunctionExtended(this.base).size();
		if (dimConj > ln)
			return false;
		this.base.clear();
		return true;
	}

	/**
	 * Tries to add all given labeled values into the current map.<br>
	 * Recursive procedure: 0. Given a set of labeled values to insert (all labels have the same length) 1. For each of them, compares all same-length labels
	 * already in the map with the current one looking for if there is one with same value and only one opposite literal (this allows the simplification of the
	 * two labels with a shorter one). In case of a positive search, shorten the current label to insert. 2. For each of the labeled values to insert (possibly
	 * updated), removes all labeled values in the map greater than it. Ad each round it tries to add labeled values of the same size.
	 *
	 * @param inputMap
	 *            contains all the elements that have to be inserted.
	 * @return true if any element of inputMap has been inserted into the map.
	 */
	private boolean insertAndSimplify(final Object2IntRBTreeMap<Label> inputMap) {
		if ((inputMap == null) || (inputMap.size() == 0))
			return false;// recursion basement!

		boolean add = false;
		// All entries of inputMap should have label of same size.
		final int n = inputMap.firstKey().size();// a dirty trick to get the size of the label.
		// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = n;
		final Object2IntRBTreeMap<Label> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(n);
		final Object2IntRBTreeMap<Label> toAdd = new Object2IntRBTreeMap<>();
		toAdd.defaultReturnValue(INT_NULL);
		ObjectArraySet<Label> toRemove = new ObjectArraySet<>();

		if (currentMapLimitedToLabelOfNSize != null) {
			for (final Entry<Label> inputEntry : inputMap.object2IntEntrySet()) {
				final Label inputLabel = inputEntry.getKey();
				final int inputValue = inputEntry.getIntValue();

				// check is there is any labeled value with same value and only one opposite literal
				for (final Entry<Label> entry : currentMapLimitedToLabelOfNSize.object2IntEntrySet()) {
					final Label l1 = new Label(entry.getKey());
					final int v1 = entry.getIntValue();

					Literal lit = null;
					if ((lit = l1.getUniqueDifferentLiteral(inputLabel)) != null) {
						int max = (inputValue > v1) ? inputValue : v1;
						// we can simplify (newLabel, newValue) and (v1,l1)
						// we maintain the pair with lower value
						// while we insert the one with greater value removing from its label 'lit'
						Label labelWOlit = new Label(l1);
						labelWOlit.removeAllLiteralsWithSameName(lit);

						if (max == inputValue && max == v1) {
							toRemove.add(inputLabel);
							toRemove.add(l1);
						} else {
							if (max == inputValue) {
								toRemove.add(inputLabel);
							} else {
								toRemove.add(l1);
							}
						}
						toAdd.put(labelWOlit, max);
					}
				}
			}
		}
		for (Label l : toRemove) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label " + l + " is removed from inputMap.");
			}
			inputMap.remove(l);
		}
		// inputMap has been updated. Now it contains all the elements that have to be insert.
		for (final Entry<Label> entry : inputMap.object2IntEntrySet()) {
			this.removeAllValuesGreaterThan(entry);
			if (this.isBaseAbleToRepresent(entry))
				continue;
			this.putForcibly(entry.getKey(), entry.getIntValue());
			add = true;
			if (this.makeABetterBase(entry))
				this.removeAllValuesGreaterThanBase();
		}
		if (toAdd.size() > 0)
			add = this.insertAndSimplify(toAdd) || add;
		return add;
	}

	/**
	 * Determines whether the value can be represented by any component of the base. A component of the base can represent the labeled value <code>(l,v)</code>
	 * if <code>l</code> subsumes the component label and the <code>v</code> is greater or equal the component value.
	 *
	 * @param entry
	 * @return true if <code>v</code> is greater or equal than a base component value that is subsumed by <code>l</code>. False otherwise.
	 */
	private boolean isBaseAbleToRepresent(final Entry<Label> entry) {
		Label inputLabel = entry.getKey();
		if ((inputLabel == null) || (this.base == null) || this.base.isEmpty())
			return false;

		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(this.base.size());
		for (final Label baseLabel : Label.allComponentsOfBaseGenerator(this.base.toArray())) {
			final int baseValue = map1.getInt(baseLabel);
			// SortedSet<String> nodeSet1 = map1.get(l1).nodeSet;

			if (baseValue == LabeledIntMap.INT_NULL) {
				LabeledIntTreeMap.LOG.severe("The base is not sound: base=" + this.base + ". Map1=" + map1);
				this.base = new Label();
				return false;
				// throw new IllegalStateException("A base component has a null value. It is not possible.");
			}
			if (inputLabel.isConsistentWith(baseLabel) && (baseValue < entry.getIntValue())) {
				// entry.setValue(baseValue);// case 6
				return true;
			}
		}
		return false;
	}

	/**
	 * @param entry
	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
	 */
	private boolean makeABetterBase(final Entry<Label> entry) {
		if ((entry.getKey() == null) || (entry.getIntValue() == LabeledIntMap.INT_NULL))
			return false;

		final int n = entry.getKey().size();

		if (n == 0) {
			// The new labeled value (l,v) has universal label, the base is not more necessary!
			this.base.clear();
			return true;
		}
		final Object2IntRBTreeMap<Label> map1 = this.mainInt2SetMap.get(n);
		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
			return false;
		final Literal[] baseCandidateColl = entry.getKey().getAllAsStraight();
		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
			int value = map1.getInt(label1);
			if (value == INT_NULL)
				return false;
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
	 * @param entry
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThan(Entry<Label> entry) {
		if ((entry.getKey() == null) || (entry.getIntValue() == LabeledIntMap.INT_NULL))
			return false;
		Label inputLabel = entry.getKey();
		int inputValue = entry.getIntValue();

		final int n = inputLabel.size();
		boolean removed = false;
		for (final Object2IntRBTreeMap<Label> internalMap : this.mainInt2SetMap.tailMap(n).values()) {
			for (final ObjectIterator<Entry<Label>> currentMapIte = internalMap.object2IntEntrySet().iterator(); currentMapIte.hasNext();) {
				final Entry<Label> currentEntry = currentMapIte.next();
				final Label currentLabel = currentEntry.getKey();
				final int currentValue = currentEntry.getIntValue();
				if (currentLabel.subsumes(inputLabel) && (currentValue >= inputValue)) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "New label " + inputLabel + " induces a remove of (" + currentLabel + ", " + currentEntry.getIntValue() + ")");
					}
					currentMapIte.remove();
					this.checkValidityOfTheBaseAfterRemovingOrAddANodeSet(currentLabel);
					removed = true;
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
		if ((this.base == null) || (this.base.size() == 0))
			return false;
		final LabeledIntTreeMap newMap = new LabeledIntTreeMap(this.optimize);
		// build the list of labeled values that form the base
		final ObjectArraySet<Entry<Label>> baseComponent = new ObjectArraySet<>((int) Math.pow(2, this.base.size()));
		for (final Label l : Label.allComponentsOfBaseGenerator(this.base.getAllAsStraight())) {
			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.get(l)));
		}
		Label l1, lb;
		int v1, vb;
		boolean toInsert = true;
		for (final Object2IntRBTreeMap<Label> map1 : this.mainInt2SetMap.values()) {
			for (final Entry<Label> entry : map1.object2IntEntrySet()) {
				l1 = entry.getKey();
				v1 = entry.getIntValue();
				toInsert = false;
				for (final Entry<Label> baseEntry : baseComponent) {
					lb = baseEntry.getKey();
					vb = baseEntry.getIntValue();
					if (l1.equals(lb)) {
						toInsert = true; // a base component has to be always insert!
						break;
					}
					if (l1.isConsistentWith(lb) && ((v1 < vb))) {
						toInsert = true;
						break;
					}
				}
				if (toInsert) {
					newMap.putForcibly(new Label(l1), v1);
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
