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
	 * @param implementationClass
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

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	@Override
	public C get() {
		return this.generatorClass.cast(this.generator.newInstance());
	}

	/**
	 * @param lim
	 * @return a new LabeledIntMap concrete object.
	 */
	public C get(LabeledIntMap lim) {
		return this.generatorClass.cast(this.generator.newInstance(lim));
	}

	@Override
	public String toString() {
		return this.generatorClass.getSimpleName();
	}
	
	public Class<C> getReturnedObjectClass() {
		return this.generatorClass;
	}
}

