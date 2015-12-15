/**
 * 
 */
package it.univr.di.labeledvalue;

/**
 * @author posenato
 */
public final class LabeledIntMapFactory {

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	static public LabeledIntMap createLabeledIntMap() {
		return createLabeledIntMap(true);
	}

	/**
	 * @param withOptimization
	 *            true if labels have to be minimized.
	 * @return a new LabeledIntMap concrete object.
	 */
	static public LabeledIntMap createLabeledIntMap(boolean withOptimization) {
		return new LabeledIntTreeMap(withOptimization);
	}

	/**
	 * @param lim
	 * @return a new LabeledIntMap concrete object.
	 */
	static public LabeledIntMap createLabeledIntMap(LabeledIntMap lim) {
		return new LabeledIntTreeMap(lim, true);
	}

	/**
	 * @param lim
	 * @param withOptimization
	 *            true if labels have to be minimized.
	 * @return a new LabeledIntMap concrete object.
	 */
	static public LabeledIntMap createLabeledIntMap(LabeledIntMap lim, boolean withOptimization) {
		return new LabeledIntTreeMap(lim, withOptimization);
	}

}
