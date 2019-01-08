package it.univr.di.labeledvalue;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntMap} interface.
 * <p>
 * An experimental result on 2016-01-13 showed that using base there is a small improvement in the performance.
 * <table border="1">
 * <caption>Execution time (ms) for some operations w.r.t the core data structure of the class (Object2IntMap).</caption>
 * <tr>
 * <th>Operation</th>
 * <th>Using AVL or RB Tree (ms)</th>
 * <th>Using OpenHash (ms)</th>
 * <th>Using fastUtil.Array (ms)</th>
 * </tr>
 * <tr>
 * <td>Create 1st map</td>
 * <td>0.236499088</td>
 * <td>0.15073823</td>
 * <td>0.140981928</td>
 * </tr>
 * <tr>
 * <td>min value</td>
 * <td>0.004523044</td>
 * <td>0.005419635</td>
 * <td>0.007725364</td>
 * </tr>
 * <tr>
 * <td>Retrieve value</td>
 * <td>0.000697368</td>
 * <td>0.000216167E</td>
 * <td>0.000172576</td>
 * </tr>
 * <tr>
 * <td>Simplification</td>
 * <td>~1.275382</td>
 * <td>~1.221648</td>
 * <td>~0.328194</td>
 * </tr>
 * </table>
 * <b>All code for performance tests is in LabeledIntTreeMapTest class (not public available).</b>
 * 
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
public class LabeledIntTreeMap extends AbstractLabeledIntMap {

	/**
	 * empty base;
	 */
	static private final char[] emptyBase = new char[0];

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(LabeledIntTreeMap.class.getName());

	/**
	 *
	 */
	static private final long serialVersionUID = 2L;

	/**
	 * @return an Object2IntMap<Label> object
	 */
	private static final Int2ObjectMap<Object2IntMap<Label>> makeInt2ObjectMap() {
		return new Int2ObjectArrayMap<>();
	}

	/**
	 * @return an Object2IntMap<Label> object
	 */
	private static final Object2IntMap<Label> makeObject2IntMap() {
		return new Object2IntArrayMap<>();// Object2IntRBTreeMap is better than Object2IntArrayMap when the set is larger than 5000 elements!
	}

	/**
	 * Set of propositions forming a base for the labels of the map.
	 */
	private char[] base;

	/**
	 * Design choice: the set of labeled values of this map is organized as a collection of sets each containing labels of the same length. This allows the
	 * label minimization task to be performed in a more systematic and efficient way. The efficiency has been proved comparing this implementation
	 * with one in which the map has been realized with a standard map and the minimization task determines the same length labels every time it needs it.
	 */
	private Int2ObjectMap<Object2IntMap<Label>> mainInt2SetMap;

	/**
	 * Necessary constructor for the factory. The internal structure is built and empty.
	 */
	public LabeledIntTreeMap() {
		this.mainInt2SetMap = makeInt2ObjectMap();
		this.base = emptyBase;
	}

	/**
	 * Constructor to clone the structure. For optimization issue, this method clone only LabeledIntTreeMap object.
	 *
	 * @param lvm the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 */
	LabeledIntTreeMap(final LabeledIntMap lvm) {
		this();
		if (lvm == null)
			return;
		this.base = ((LabeledIntTreeMap) lvm).base;
		for (final Entry<Label> entry : lvm.entrySet()) {
			this.putForcibly(entry.getKey(), entry.getIntValue());
		}
	}

	/**
	 * @param newLabel
	 * @param newValue
	 * @return true if the current map can represent the value. In positive case, an addition of the element does not change the map.
	 *         If returns false, then an addition of the value to the map would modify the map.
	 */
	@Override
	public boolean alreadyRepresents(Label newLabel, int newValue) {
		int valuePresented = get(newLabel);
		if (valuePresented > newValue)
			return false;// the newValue would simplify the map.
		if (valuePresented != Constants.INT_NULL && valuePresented < newValue)
			return true;
		/**
		 * Check if there is already a value in the map that represents the new value.
		 */
		final int newLabelSize = newLabel.size();

		for (int labelLenght : this.mainInt2SetMap.keySet()) {
			if (labelLenght > newLabelSize)
				continue;
			for (Entry<Label> entry : this.mainInt2SetMap.get(labelLenght).object2IntEntrySet()) {
				final Label l1 = entry.getKey();
				final int v1 = entry.getIntValue();

				if (newLabel.subsumes(l1) && newValue >= v1) {
					return true;
				}
			}
		}
		return (isBaseAbleToRepresent(newLabel, newValue));
	}

	/**
	 * If the removed label <code>l</code> is a component of the base, then the base is reset.
	 * <p>
	 * An experiment result on 2016-01-13 showed that using base there is a small improvement in the performance.
	 * 
	 * @param l
	 * @return true if <code>l</code> is a component of the base, false otherwise.
	 */
	private boolean checkValidityOfTheBaseAfterRemoving(final Label l) {
		if ((this.base.length == 0) || (l.size() == 0) || (l.size() != this.base.length) || l.containsUnknown())
			return false;

		// l and base have same length.
		for (char c : this.base) {
			if (!l.contains(c))
				return false;
		}
		// l is a component of the base and it was removed.
		this.base = emptyBase;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.mainInt2SetMap.clear();
		this.base = emptyBase;
	}

	@Override
	public LabeledIntTreeMap createLabeledIntMap() {
		return new LabeledIntTreeMap();
	}

	@Override
	public LabeledIntTreeMap createLabeledIntMap(LabeledIntMap lim) {
		return new LabeledIntTreeMap(lim);
	}

	/**
	 * {@inheritDoc}
	 * Up to 1000 items in the map it is better to use {@link #entrySet()} instead of {@link #keySet()} and, then, {@link #get(Label)}. With 1000 or more items,
	 * it is better to use {@link #keySet()} approach.
	 */
	@Override
	public ObjectSet<Entry<Label>> entrySet() {
		final ObjectSet<Entry<Label>> coll = new ObjectArraySet<>();
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			coll.addAll(mapI.object2IntEntrySet());
		}
		return coll;
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse) {
		setToReuse.clear();
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			setToReuse.addAll(mapI.object2IntEntrySet());
		}
		return setToReuse;
	}

	/** {@inheritDoc} */
	@Override
	public int get(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		final Object2IntMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null)
			return Constants.INT_NULL;
		return map1.getInt(l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMaxValue() {
		int max = Constants.INT_NEG_INFINITE;
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			for (int j : mapI.values())
				if (max < j)
					max = j;
		}
		return (max == Constants.INT_NEG_INFINITE) ? Constants.INT_NULL : max;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValue() {
		int min = Constants.INT_POS_INFINITE;
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			for (int j : mapI.values())
				if (min > j)
					min = j;
		}
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueSubsumedBy(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		int min = this.get(l);
		if (min == Constants.INT_NULL) {
			// the label does not exits, try all subsumed labels
			min = this.get(Label.emptyLabel);
			if (min == Constants.INT_NULL) {
				min = Constants.INT_POS_INFINITE;
			}
			int v1;
			Label l1 = null;
			int n = l.size();
			for (int i = 0; i < n; i++) {
				Object2IntMap<Label> map = this.mainInt2SetMap.get(i);
				if (map == null)
					continue;
				for (final Entry<Label> e : map.object2IntEntrySet()) {
					l1 = e.getKey();
					if (l.subsumes(l1)) {
						v1 = e.getIntValue();
						if (min > v1) {
							min = v1;
						}
					}
				}
			}
		}
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.mainInt2SetMap.hashCode();
	}

	/**
	 * Tries to add all given labeled values into the current map:
	 * <ol>
	 * <li>Given a set of labeled values to insert (all labels have the same length)
	 * <li>For each of them, compares all same-length labels already in the map with the current one looking for if there is one with same value and only one
	 * opposite literal (this allows the simplification of the two labels with a shorter one). In case of a positive search, shorten the current label to
	 * insert.
	 * <li>For each of the labeled values to insert (possibly updated), removes all labeled values in the map greater than it. Ad each round it tries to add
	 * labeled values of the same size.
	 *
	 * @param inputMap contains all the elements that have to be inserted.
	 * @param inputMapLabelLength length of labels contained into inputMap
	 * @return true if any element of inputMap has been inserted into the map.
	 */
	private boolean insertAndSimplify(Object2IntMap<Label> inputMap, int inputMapLabelLength) {

		ObjectArraySet<Label> toRemove = new ObjectArraySet<>();
		boolean add = false;

		while (inputMapLabelLength >= 0) {
			// All entries of inputMap should have label of same size.
			// currentMapLimitedToLabelOfNSize contains all the labeled values with label size = inputMapSize;
			final Object2IntMap<Label> currentMapLimitedToLabelOfNSize = this.mainInt2SetMap.get(inputMapLabelLength);
			final Object2IntMap<Label> toAdd = makeObject2IntMap();
			toAdd.defaultReturnValue(Constants.INT_NULL);
			toRemove.clear();

			if (currentMapLimitedToLabelOfNSize != null) {
				for (final Entry<Label> inputEntry : inputMap.object2IntEntrySet()) {
					final Label inputLabel = inputEntry.getKey();
					final int inputValue = inputEntry.getIntValue();

					// check is there is any labeled value with same value and only one opposite literal
					for (final Entry<Label> entry : currentMapLimitedToLabelOfNSize.object2IntEntrySet()) {
						Label l1 = entry.getKey();
						final int v1 = entry.getIntValue();

						Literal lit = null;
						// Management of two labels that differ for only one literal (one contains the straight one while the other contains the negate).
						// Such labels can be reduced in two different way.
						// 1) If they have the same value, then they can be replaced with one only labeled value (same value) where label does not contain the
						// different literal.
						// If they haven't the same value, they are ignored and, in the following, they will constitute a base for the set.
						// 2) The label with the maximum value is always replaced with a labeled value where value is the same but the label does not contain
						// the different literal.
						// If both have the same value, the management is equivalent to 1) first part.
						// The disadvantage of this management is that is quite difficult to build base.
						//
						// An experimental test showed that is 2) management makes the algorithm ~30% faster.
						// On 2016-03-30 I discovered that with Management 2) there is a potential problem in the representation of situations like:
						// Current set={ (b,-1), (¬b,-2) }. Request to insert (¿b,-3).
						// Even value (¿b,-3) should not be insert because the base is able to represent it, since ¿b is consistent with b/¬b (extended
						// consistency)
						// the value (¿b,-3) is insert in both the two management.
						/**
						 * Management 1)
						 */
						if (inputValue == v1 && (lit = l1.getUniqueDifferentLiteral(inputLabel)) != null) {
							// we can simplify (newLabel, newValue) and (v1,l1) removing them and putting in map (v1/lit,l1)
							toRemove.add(inputLabel);
							toRemove.add(entry.getKey());
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LOG.log(Level.FINEST, "Label " + l1 + ", combined with label " + inputLabel + " induces a simplification. "
											+ "Firstly, (" + inputLabel + ", " + inputValue + ") in removed.");
								}
							}
							l1 = l1.remove(lit.getName());
							if (l1.size() < 0)
								throw new IllegalStateException("There is no literal to remove, there is a problem in the code!");
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LOG.log(Level.FINEST, "Then, (" + l1 + ", " + v1 + ") is considering for adding at the end.");
								}
							}
							toAdd.put(l1, v1);
						}
						/**
						 * Management 2)
						 */
						// if ((lit = l1.getUniqueDifferentLiteral(inputLabel)) != null) {
						// int max = (inputValue > v1) ? inputValue : v1;
						// // we can simplify (newLabel, newValue) and (v1,l1)
						// // we maintain the pair with lower value
						// // while we insert the one with greater value removing from its label 'lit'
						// Label labelWOlit = new Label(l1);
						// labelWOlit.remove(lit.getName());
						//
						// if (max == inputValue && max == v1) {
						// toRemove.add(inputLabel);
						// toRemove.add(l1);
						// } else {
						// if (max == inputValue) {
						// toRemove.add(inputLabel);
						// } else {
						// toRemove.add(l1);
						// }
						// }
						// toAdd.put(labelWOlit, max);
						// }
					}
				}
			}
			for (Label l : toRemove) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Label " + l + " is removed from inputMap.");
					}
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
			if (toAdd.size() > 0) {
				inputMap = toAdd;
				inputMapLabelLength--;
			} else {
				inputMapLabelLength = -1;
			}
		}
		return add;
	}

	/**
	 * @see #isBaseAbleToRepresent(Label, int)
	 * @param entry
	 * @return true if <code>inputValue</code> is greater or equal than a base component value that is subsumed by <code>inputLabel</code>. False otherwise.
	 */
	private final boolean isBaseAbleToRepresent(final Entry<Label> entry) {
		return isBaseAbleToRepresent(entry.getKey(), entry.getIntValue());
	}

	/**
	 * Determines whether the value can be represented by any component of the base.<br>
	 * There are the following cases:
	 * <ol>
	 * <li>A component of the base can represent the labeled value <code>(l,v)</code> if <code>l</code> subsumes the component label and the <code>v</code> is
	 * greater or equal the component value.
	 * <li>If the entry has value that is greater that any value of the base, then the base can represent it.
	 * </ol>
	 * <p>
	 * An experiment result on 2016-01-13 showed that using base there is a small improvement in the performance.
	 * 
	 * @param inputLabel
	 * @param inputValue
	 * @return true if <code>inputValue</code> is greater or equal than a base component value that is subsumed by <code>inputLabel</code>. False otherwise.
	 */
	private boolean isBaseAbleToRepresent(final Label inputLabel, final int inputValue) {
		if (this.base == emptyBase)
			return false;

		final Object2IntMap<Label> map1 = this.mainInt2SetMap.get(this.base.length);
		for (final Label baseLabel : Label.allComponentsOfBaseGenerator(this.base)) {
			final int baseValue = map1.getInt(baseLabel);

			if (baseValue == Constants.INT_NULL) {
				if (Debug.ON)
					if (LOG.isLoggable(Level.SEVERE)) {
						LabeledIntTreeMap.LOG.severe("The base is not sound: base=" + Arrays.toString(this.base) + ". Map1=" + map1);
					}
				this.base = emptyBase;
				return false;
				// throw new IllegalStateException("A base component has a null value. It is not possible.");
			}
			if (inputLabel.subsumes(baseLabel)) {
				if (inputValue >= baseValue) {
					// entry.setValue(baseValue);// case 6
					return true;
				}
				return false;
			}
			if (inputLabel.isConsistentWith(baseLabel)) {
				if (inputValue < baseValue) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet() {
		ObjectSet<Label> coll = new ObjectArraySet<>();
		return keySet(coll);
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse) {
		setToReuse.clear();
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			setToReuse.addAll(mapI.keySet());
		}
		return setToReuse;
	}

	/**
	 * If entry (label, value) determines a new (better) base, the base is updated.<br>
	 * An experiment result on 2016-01-13 showed that using base there is a small improvement in the performance.
	 * 
	 * @param entry
	 * @return true if (label, value) determine a new (better) base. If true, the base is update. False otherwise.
	 */
	private boolean makeABetterBase(final Entry<Label> entry) {
		if (entry.getIntValue() == Constants.INT_NULL)
			return false;

		final int n = entry.getKey().size();

		if (n == 0) {
			// The new labeled value (l,v) has universal label, the base is not more necessary!
			this.base = emptyBase;
			return true;
		}
		final Object2IntMap<Label> map1 = this.mainInt2SetMap.get(n);
		if (map1.size() < Math.pow(2.0, n)) // there are no sufficient elements!
			return false;
		final char[] baseCandidateColl = entry.getKey().getPropositions();
		for (final Label label1 : Label.allComponentsOfBaseGenerator(baseCandidateColl)) {
			int value = map1.getInt(label1);
			if (value == Constants.INT_NULL)
				return false;
		}
		this.base = baseCandidateColl;
		return true;
	}

	/**
	 * {@inheritDoc} Adds the pair &lang;l,i&rang;.<br>
	 * Moreover, tries to eliminate all labels that are redundant.<br>
	 * <b>IMPORTANT!</b><br>
	 * This version of the method is very redundant but simple to check!
	 */
	@Override
	public boolean put(final Label newLabel, int newValue) {
		if ((newLabel == null) || (newValue == Constants.INT_NULL) || alreadyRepresents(newLabel, newValue))
			return false;
		/**
		 * Step 1.
		 * The value is not already represented.
		 * It must be add.
		 * In the following, all values already present and implied by the new one are removed before adding it.
		 * Then, the new value is add in step 2.
		 */
		removeAllValuesGreaterThan(newLabel, newValue);

		/**
		 * Step 2.
		 * Insert the new value and check if it possible to simplify with some other labels with same value and only one different literals.
		 */
		final Object2IntMap<Label> a = makeObject2IntMap();
		a.defaultReturnValue(Constants.INT_NULL);
		a.put(newLabel, newValue);
		return this.insertAndSimplify(a, newLabel.size());
	}

	/**
	 * @param l
	 * @param i
	 * @return previous value if present, Constants.INT_NULL otherwise;
	 */
	public int putForcibly(final Label l, final int i) {
		if ((l == null) || (i == Constants.INT_NULL))
			return Constants.INT_NULL;
		Object2IntMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null) {
			map1 = makeObject2IntMap();
			map1.defaultReturnValue(Constants.INT_NULL);
			this.mainInt2SetMap.put(l.size(), map1);
		}
		return map1.put(l, i);
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		if (l == null)
			return Constants.INT_NULL;

		final Object2IntMap<Label> map1 = this.mainInt2SetMap.get(l.size());
		if (map1 == null)
			return Constants.INT_NULL;
		final int oldValue = map1.removeInt(l);
		if (oldValue != Constants.INT_NULL) {// && this.optimize) {
			// The base could have been changed. To keep things simple, for now it is better to rebuild all instead of to try to rebuild a possible damaged
			// base.
			if (this.checkValidityOfTheBaseAfterRemoving(l)) {
				final LabeledIntTreeMap newMap = new LabeledIntTreeMap(this);
				this.mainInt2SetMap = newMap.mainInt2SetMap;
				this.base = newMap.base;
			}
		}
		return oldValue;
	}

	/**
	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
	 * 
	 * @param entry
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThan(Entry<Label> entry) {
		if (entry == null)
			return false;
		return removeAllValuesGreaterThan(entry.getKey(), entry.getIntValue());

	}

	/**
	 * Remove all labeled values that subsume <code>l</code> and have values greater or equal to <code>i</code>.
	 * 
	 * @param inputLabel
	 * @param inputValue
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThan(final Label inputLabel, final int inputValue) {
		if (inputLabel == null || inputValue == Constants.INT_NULL)
			return false;
		boolean removed = false;
		final int inputLabelSize = inputLabel.size();
		for (int labelLenght : this.mainInt2SetMap.keySet()) {
			if (labelLenght < inputLabelSize)
				continue;
			final Object2IntMap<Label> internalMap = this.mainInt2SetMap.get(labelLenght);
			// BE CAREFUL! Since it is necessary to remove, it is not possible to use internalMap.keySet() directly
			// because removing an element in the map changes the keyset and it is possible to loose the checking of some label (the following
			// one a deleted element).
			// Iterator are not supported!
			// The last resource is to copy the labeled value set using object2IntEntrySet! :-(
			for (Entry<Label> entry1 : internalMap.object2IntEntrySet()) {
				final Label currentLabel = entry1.getKey();
				final int currentValue = entry1.getIntValue();
				if (currentLabel.subsumes(inputLabel) && (currentValue >= inputValue)) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "New label " + inputLabel + " induces a remove of (" + currentLabel + ", " + currentValue + ")");
						}
					}
					internalMap.remove(currentLabel);
					this.checkValidityOfTheBaseAfterRemoving(currentLabel);
					removed = true;
				}
			}
		}
		return removed;
	}

	/**
	 * Remove all labeled values having each value greater than all values of base components consistent with it.
	 * An experiment result on 2016-01-13 showed that using base there is a small improvement in the performance.
	 * 
	 * @return true if one element at least has been removed, false otherwise.
	 */
	private boolean removeAllValuesGreaterThanBase() {
		if ((this.base == null) || (this.base.length == 0))
			return false;
		final LabeledIntTreeMap newMap = new LabeledIntTreeMap();
		// build the list of labeled values that form the base
		final ObjectArrayList<Entry<Label>> baseComponent = new ObjectArrayList<>((int) Math.pow(2, this.base.length));
		for (final Label l : Label.allComponentsOfBaseGenerator(this.base)) {
			baseComponent.add(new AbstractObject2IntMap.BasicEntry<>(l, this.get(l)));
		}
		Label l1, lb;
		int v1, vb;
		boolean toInsert = true;
		for (final Object2IntMap<Label> map1 : this.mainInt2SetMap.values()) {
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
					if (l1.isConsistentWith(lb) && v1 < vb) {// isConsistent is necessary to manage cases like base = {(b,3)(¬b,4)} l1={(a,1)}
						toInsert = true;
						break;
					}
				}
				if (toInsert) {
					newMap.putForcibly(l1, v1);
				}
			}
		}
		if (!newMap.equals(this)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("Base changed: the old map " + this.toString() + " is subsituted by " + newMap.toString());
				}
			}
			this.mainInt2SetMap = newMap.mainInt2SetMap;
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		int n = 0;
		for (final Object2IntMap<Label> map1 : this.mainInt2SetMap.values())
			n += map1.size();
		return n;
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		final IntArraySet coll = new IntArraySet();
		for (final Object2IntMap<Label> mapI : this.mainInt2SetMap.values()) {
			for (Entry<Label> i : mapI.object2IntEntrySet())
				coll.add(i.getIntValue());
		}
		return coll;
	}

}
