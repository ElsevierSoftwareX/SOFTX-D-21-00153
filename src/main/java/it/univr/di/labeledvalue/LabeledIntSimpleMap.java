// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.labeledvalue;

import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntMap} interface without minimization.
 * This class is provided only to give an evidence that without minimization of labeled value sets, any CSTN algorithm
 * can be very slowly.
 * 
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
@Deprecated
public class LabeledIntSimpleMap extends AbstractLabeledIntMap {

	/**
	 * A read-only view of an object
	 * 
	 * @author posenato
	 */
	public static class LabeledIntNotMinMapView extends LabeledIntSimpleMap implements LabeledIntMapView {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * @param inputMap
		 */
		public LabeledIntNotMinMapView(LabeledIntSimpleMap inputMap) {
			this.mainMap = inputMap.mainMap;
		}
	}

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger("LabeledIntSimpleMap");

	/**
	 *
	 */
	static private final long serialVersionUID = 2L;

	/**
	 * @return an Object2IntMap<Label> object
	 */
	private static final Object2IntMap<Label> makeLabel2IntMap() {
		return new Object2IntOpenHashMap<>();// Object2IntRBTreeMap is better than Object2IntArrayMap when the set is larger than 5000 elements!
	}

	/**
	 * Map of label
	 */
	Object2IntMap<Label> mainMap;

	/**
	 * Counter of labeled value updates.
	 */
	Object2IntMap<Label> updateCount;

	/**
	 * Necessary constructor for the factory. The internal structure is built and empty.
	 */
	public LabeledIntSimpleMap() {
		this.mainMap = makeLabel2IntMap();
		this.mainMap.defaultReturnValue(Constants.INT_NULL);
		this.updateCount = makeLabel2IntMap();
		this.updateCount.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Constructor to clone the structure. For optimization issue, this method clone only LabeledIntTreeMap object.
	 *
	 * @param lvm the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 */
	public LabeledIntSimpleMap(final LabeledIntMap lvm) {
		this();
		if (lvm == null)
			return;
		for (final Entry<Label> entry : lvm.entrySet()) {
			this.put(entry.getKey(), entry.getIntValue());
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
		for ( Entry<Label> entry:this.mainMap.object2IntEntrySet()) {
			if (newLabel.subsumes(entry.getKey()) && newValue >= entry.getIntValue())
				return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		this.mainMap.clear();
		this.updateCount.clear();
	}

	@Override
	public LabeledIntSimpleMap newInstance() {
		return new LabeledIntSimpleMap();
	}

	@Override
	public LabeledIntSimpleMap newInstance(LabeledIntMap lim) {
		return new LabeledIntSimpleMap(lim);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ObjectSet<Entry<Label>> entrySet() {
		ObjectSet<Entry<Label>> coll = new ObjectArraySet<>();
		// for( Entry<Label> entry : this.mainMap.object2IntEntrySet()) {
		// coll.add(new AbstractObject2IntMap.BasicEntry<>(entry.getKey(), entry.getIntValue()));
		// }
		return entrySet(coll);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse) {
		setToReuse.clear();
		setToReuse.addAll(this.mainMap.object2IntEntrySet());
		return setToReuse;
	}

	/** {@inheritDoc} */
	@Override
	public int get(final Label l) {
		return this.mainMap.getInt(l);
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.mainMap.hashCode();
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet() {
		ObjectSet<Label> coll = new ObjectArraySet<>();
		return this.keySet(coll);
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse) {
		setToReuse.clear();
		setToReuse.addAll(this.mainMap.keySet());
		return setToReuse;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean put(final Label newLabel, int newValue) {
		if ((newLabel == null) || (newValue == Constants.INT_NULL))
			return false;
		int updated = this.updateCount.getInt(newLabel);
		updated = (updated == Constants.INT_NULL) ? 1 : updated + 1;
		if (!this.alreadyRepresents(newLabel, newValue)) {
			this.mainMap.put(newLabel, newValue);
			this.updateCount.put(newLabel, updated);
			return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		return this.mainMap.removeInt(l);
	}

	@Override
	public int size() {
		return this.mainMap.size();
	}

	@Override
	public LabeledIntMapView unmodifiable() {
		return new LabeledIntNotMinMapView(this);
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		return (IntSet) this.mainMap.values();
	}

}
