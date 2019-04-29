/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.lang.reflect.Array;
import java.util.logging.Logger;

import com.google.common.base.Supplier;

import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * LabeledIntEdgePluggabble supplier.
 * 
 * @author posenato
 * @param <C> type for managing labeled values.
 */
public class LabeledIntEdgeSupplier<C extends LabeledIntMap> implements Supplier<LabeledIntEdge> {

	/**
	 * class logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger(LabeledIntEdgeSupplier.class.getName());

	/**
	 * 
	 */
	private Class<C> labeledIntValueMapImpl;

	/**
	 * @param labeledIntMapImplementation
	 */
	public LabeledIntEdgeSupplier(Class<C> labeledIntMapImplementation) {
		super();
		this.labeledIntValueMapImpl = labeledIntMapImplementation;
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	@Override
	public LabeledIntEdgePluggable get() {
		return new LabeledIntEdgePluggable(this.labeledIntValueMapImpl);
	}

	/**
	 * @param edge the edge to clone.
	 * @return a new edge
	 */
	public LabeledIntEdgePluggable get(LabeledIntEdge edge) {
		return new LabeledIntEdgePluggable(edge, this.labeledIntValueMapImpl);
	}

	/**
	 * @param name a name for the new edge
	 * @return  a new edge
	 */
	public LabeledIntEdgePluggable get(String name) {
		return new LabeledIntEdgePluggable(name, this.labeledIntValueMapImpl);
	}

	/**
	 * @param n dimension
	 * @return a new LabeledIntEdge array of size n.
	 */
	public static LabeledIntEdgePluggable[] get(int n) {
		return (LabeledIntEdgePluggable[]) Array.newInstance(LabeledIntEdgePluggable.class, n);
	}

	/**
	 * @return the class chosen for creating new object.
	 */
	public Class<C> getInternalObjectClass() {
		return this.labeledIntValueMapImpl;
	}

	@Override
	public String toString() {
		return "Labeled value set managed as " + this.labeledIntValueMapImpl.toString() + ".";
	}

}
