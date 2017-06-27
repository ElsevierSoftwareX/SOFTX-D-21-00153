//package it.univr.di.attic;
//
//import it.univr.di.labeledvalue.Label;
//
//import java.util.Map.Entry;
//import java.util.Set;
//
///**
// * Declare all methods necessary to manage a set of values (of type I) labeled by label of type {@link Label}.<br>
// * All methods managing single labeled value make a defensive copy of the label in order to guarantee that the label insert/get is a copy of the label
// * given/requested.<br>
// * All methods managing bundle of labeled values do not make defensive copy.
// * <p>
// * 2014-11-23: I decided to switch to LabeledIntMap because it is more common to have CSTN(U) with integer weights. An int typed map can speed up the execution
// * time of the implementation.
// * <p>
// * 
// * @author Robert Posenato
// * @param <I>
// */
//public interface LabeledValueMap<I extends Number & Comparable<I>> {
//
//	/**
//	 * Remove all entries of the map.
//	 */
//	public void clear();
//
//	/**
//	 * @param l
//	 * @return the value associated to label L, if it exists, null otherwise.
//	 */
//	public I getValue(Label l);
//
//	/**
//	 * @return the set of all values.
//	 */
//	public Set<I> values();
//
//	/**
//	 * @return a set representation of this map.
//	 */
//	public Set<Entry<Label,I>> entrySet();
//
//	/**
//	 * Merges a label with value <code>i</code> only if label <code>l</code> is not null.
//	 * <p>
//	 * The labeled value is inserted if there is not a labeled value in the set with label <code>l</code> or it is present with a value higher than
//	 * <code>l</code>. <br>
//	 * Not mandatory: the method can remove or modify other labeled values of the set in order to minimize the labeled values present guaranteeing that no info
//	 * is lost.
//	 *
//	 * @param l a not null label.
//	 * @param i a not null value.
//	 * @return true if the labeled value has been insert. False if it is already present.
//	 */
//	public boolean mergeLabeledValue(Label l, I i);
//
//	/**
//	 * Put a label <code>l</code> with value <code>i</code> only if <code>l</code> is not null, overwriting a possibly already present value with the same
//	 * label.
//	 * Be careful! This method can destroy the minimality of represented labeled values. If you are in doubt, use {@link #mergeLabeledValue(Label, Number)}.
//	 * This method is useful only when there is no label optimization and one wants to collect all labels without any sanity check, that is always made in
//	 * {@link #mergeLabeledValue(Label, Number)}.
//	 *
//	 * @param l a not null label.
//	 * @param i a not null value.
//	 * @return the previous value associated with <code>l</code>, or null if there was no mapping for <code>l</code>.
//	 */
//	public I putLabeledValue(Label l, I i);
//
//	/**
//	 * Remove the label <code>l</code> from the map. If the <code>l</code> is not present, it does nothing.
//	 *
//	 * @param l a not null label.
//	 * @return the previous value associated with <code>l</code>, or null if there was no mapping for <code>l</code>.
//	 */
//	public I remove(Label l);
//
//	/**
//	 * @return the number of labeled value (value with empty label included).
//	 */
//	public int size();
//
//	/**
//	 * Returns the value associated to the <code>l</code> if it exists, otherwise the minimal value among all labels
//	 * consistent with <code>l</code>.
//	 *
//	 * @param l If it is null, null is returned.
//	 * @return the value associated to the <code>l</code> if it exists or the minimal value among values associated to
//	 *         labels consistent with <code>l</code>. If no labels are consistent by <code>l</code>, null is returned.
//	 */
//	public I getMinValueConsistentWith(final Label l);
//
//}
