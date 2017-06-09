package it.univr.di.labeledvalue;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.ValueNodeSetPair;

/**
 * Declare all methods necessary to manage a set of values (of type int) each labeled by label of type {@link it.univr.di.labeledvalue.Label}.
 * Each value is also associated to a set of nodes that represent the internal nodes of any path that has determined the value.
 * <p>
 * The semantics of a set of labeled value is defined in the paper “The Dynamic Controllability of Conditional STNs with Uncertainty.” by "Hunsberger, Luke,
 * Roberto Posenato, and Carlo Combi. 2012. http://arxiv.org/abs/1212.2005. //FIXME Mettere ultima referenza.
 * <p>
 * All methods managing single labeled value make a defensive copy of the label in order to guarantee that the label insert/get is a copy of the label
 * given/requested.<br>
 * All methods managing bundle of labeled values do not make defensive copy.
 * <p>
 * This interface is specialized for labeled integer.
 * </p>
 *
 * @author Robert Posenato
 * @version $Id: $Id
 */
public interface LabeledIntNodeSetMap extends LabeledIntMap {

	/**
	 * <p>get.</p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the pair value and node set associated to label l if it exists, null otherwise.
	 */
	public ValueNodeSetPair getNodeSetPair(Label l);

	/**
	 * <p>getNodeSet.</p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the node set associated to label L if it exists; null otherwise.
	 */
	public SortedSet<String> getNodeSet(Label l);

	/**
	 * It has the same specification of {@link java.util.Map#entrySet()}.
	 * Therefore, the set is backed by the map: changes to the map are reflected in the set, and vice-versa.
	 * <p>
	 * The only different thing from {@link java.util.Map#entrySet()} is the type of returned entries.
	 *
	 * @see java.util.Map#entrySet()
	 * @return a set representation of this map.
	 */
	public Set<it.unimi.dsi.fastutil.objects.Object2ObjectMap.Entry<Label, ValueNodeSetPair>> object2ObjectEntrySet();

	/**
	 * Put a label with value <code>i</code> only if label <code>l</code> is not null.
	 * <p>
	 * The labeled value is inserted if there is not a labeled value in the set with label <code>l</code> or it is present but with a value higher than
	 * <code>l</code>.<br>
	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
	 * is lost.
	 *
	 * @param l a not null label.
	 * @param i a not {@link Constants#INT_NULL} value.
	 * @param nodeNameSet a {@link java.util.Set} object.
	 * @return true if <code>(l,i)</code> has been inserted. Since an insertion can be removed more than one redundant labeled ints, it is nonsensical to return
	 *         "the old value" as expected from a standard put method.
	 */
	public boolean put(Label l, int i, Set<String> nodeNameSet);

	/**
	 * Put all elements of inputMap into the current one without making a defensive copy.
	 *
	 * @param inputMap a {@link it.univr.di.attic.LabeledIntNodeSetMap} object.
	 * @see Object2IntMap#putAll(Map)
	 */
	public void putAll(final LabeledIntNodeSetMap inputMap);

	/**
	 * Put a label <code>l</code> with value <code>i</code> only if <code>l</code> is not null, overwriting a possibly already present value with the same
	 * label.
	 * Be careful! This method can destroy the minimality of represented labeled values. If you are in doubt, use {@link #put(Label, int)}.
	 * This method is useful only when there is no label optimization and one wants to collect all labels without any sanity check, that is always made in
	 * {@link #put(Label, int)}.
	 *
	 * @param l a not null label.
	 * @param i a not {@link Constants#INT_NULL} value.
	 * @return the previous int associated to l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	public int putForcibly(Label l, int i);

	/**
	 * Put a label <code>l</code> with value <code>i</code> only if <code>l</code> is not null, overwriting a possibly already present value with the same
	 * label.
	 * Be careful! This method can destroy the minimality of represented labeled values. If you are in doubt, use {@link #putForcibly(Label, int, Set)}.
	 * This method is useful only when there is no label optimization and one wants to collect all labels without any sanity check, that is always made in
	 * {@link #put(Label, int, Set)}.
	 *
	 * @param l a not null label.
	 * @param i a not {@link Constants#INT_NULL} value.
	 * @param nodeNameSet a {@link java.util.Set} object.
	 * @return the previous int associated to l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	public int putForcibly(Label l, int i, Set<String> nodeNameSet);
}
