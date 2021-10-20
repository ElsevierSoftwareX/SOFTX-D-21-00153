// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.labeledvalue;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import com.google.common.base.Supplier;

/**
 * Basic factory of LabeledIntMap objects.
 * A implementation C must provide
 *
 * @author posenato
 * @param <C> implementation class of LabeledIntMap interface.
 * @version $Id: $Id
 */
public final class LabeledIntMapSupplier<C extends LabeledIntMap> implements Supplier<C> {

	/**
	 * class logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger("LabeledIntMapSupplier");

	/**
	 * 
	 */
	static final public Class<? extends LabeledIntMap> DEFAULT_LABELEDINTMAP_CLASS = LabeledIntTreeMap.class;

	/**
	 * 
	 */
	private C generator;

	/**
	 * 
	 */
	private Class<C> generatorClass;

	/**
	 * <p>Constructor for LabeledIntMapSupplier.</p>
	 *
	 * @param implementationClass a {@link java.lang.Class} object.
	 */
	public LabeledIntMapSupplier(Class<C> implementationClass) {
		super();
		this.generatorClass = implementationClass;
		try {
			this.generator = implementationClass.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			e.printStackTrace();
		}
	}

	/** {@inheritDoc} */
	@Override
	public C get() {
		return this.generatorClass.cast(this.generator.newInstance());
	}

	/**
	 * <p>get.</p>
	 *
	 * @param lim a {@link it.univr.di.labeledvalue.LabeledIntMap} object.
	 * @return a new LabeledIntMap concrete object.
	 */
	public C get(LabeledIntMap lim) {
		return this.generatorClass.cast(this.generator.newInstance(lim));
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return this.generatorClass.getSimpleName();
	}
	
	/**
	 * <p>getReturnedObjectClass.</p>
	 *
	 * @return a {@link java.lang.Class} object.
	 */
	public Class<C> getReturnedObjectClass() {
		return this.generatorClass;
	}
}

