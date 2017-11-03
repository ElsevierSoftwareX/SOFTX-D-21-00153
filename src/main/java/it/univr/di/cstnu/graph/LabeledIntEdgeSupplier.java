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
 * @param <C>
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
	private LabeledIntEdgePluggable internal;
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
		this.internal = new LabeledIntEdgePluggable(labeledIntMapImplementation);
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	public LabeledIntEdgePluggable get() {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(this.labeledIntValueMapImpl);
	}

	/**
	 * @param edge the edge to clone.
	 * @return a new edge
	 */
	public LabeledIntEdgePluggable get(LabeledIntEdge edge) {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(edge, this.labeledIntValueMapImpl);
	}

	/**
	 * @param name a name for the new edge
	 * @return  a new edge
	 */
	public LabeledIntEdgePluggable get(String name) {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(name, this.labeledIntValueMapImpl);
	}

	/**
	 * @param n dimension
	 * @return a new LabeledIntEdge array of size n.
	 */
	public LabeledIntEdgePluggable[] get(int n) {
		return (LabeledIntEdgePluggable[]) Array.newInstance(this.internal.getClass(), n);
	}

	/**
	 * @return the class chosen for creating new object.
	 */
	public Class<C> getInternalObjectClass() {
		return this.labeledIntValueMapImpl;
	}

	public String toString() {
		return "Labeled value set managed as " + this.internal.labeledValueMapFactory.toString() + ".";
	}

}
