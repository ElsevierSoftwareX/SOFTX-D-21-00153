/**
 * 
 */
package it.univr.di.labeledvalue;

import java.util.logging.Logger;

import com.google.common.base.Supplier;

/**
 * @author posenato <C> implementation class of LabeledIntMap interface.
 * @param <C> 
 */
public final class LabeledIntMapFactory<C extends LabeledIntMap> implements Supplier<C> {

	/**
	 * class logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger(LabeledIntMapFactory.class.getName());

	/**
	 * 
	 */
	static final public Class<LabeledIntTreeMap> DEFAULT_LABELEDINTMAP_CLASS = LabeledIntTreeMap.class;

	/**
	 * 
	 */
	private C internal;

	/**
	 * @param implementationClass
	 */
	public LabeledIntMapFactory(Class<C> implementationClass) {
		super();
		try {
			this.internal = implementationClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use {@link #DEFAULT_LABELEDINTMAP_CLASS} as implementing class.
	 */
	@SuppressWarnings("unchecked")
	public LabeledIntMapFactory() {
		super();
		try {
			this.internal = (C) DEFAULT_LABELEDINTMAP_CLASS.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public C get() {
		return (C) this.internal.createLabeledIntMap();
	}

	/**
	 * @param lim
	 * @return a new LabeledIntMap concrete object.
	 */
	@SuppressWarnings("unchecked")
	public C get(LabeledIntMap lim) {
		return (C) this.internal.createLabeledIntMap(lim);
	}

	@Override
	public String toString() {
		return this.internal.getClass().getSimpleName();
	}
	
	@SuppressWarnings({ "unchecked", "javadoc" })
	public Class<C> getReturnedObjectClass() {
		return (Class<C>) this.internal.getClass();
	}
}

