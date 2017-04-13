/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.lang.reflect.Array;
import java.util.logging.Logger;

import org.apache.commons.collections15.Factory;

import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * @author posenato
 * @param <C>
 */
public class LabeledIntEdgeFactory<C extends LabeledIntMap> implements Factory<LabeledIntEdgePluggable> {

	/**
	 * class logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger(LabeledIntEdgeFactory.class.getName());

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
	public LabeledIntEdgeFactory(Class<C> labeledIntMapImplementation) {
		super();
		this.labeledIntValueMapImpl = labeledIntMapImplementation;
		this.internal = new LabeledIntEdgePluggable(labeledIntMapImplementation);
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	public LabeledIntEdgePluggable create() {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(this.labeledIntValueMapImpl);
	}

	/**
	 * @param lim
	 * @return a new LabeledIntMap concrete object.
	 */
	public LabeledIntEdgePluggable create(LabeledIntEdge lim) {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(lim, this.labeledIntValueMapImpl);
	}

	
	/**
	 * @param name a name for the new edge
	 * @return a new LabeledIntMap concrete object.
	 */
	public LabeledIntEdgePluggable create(String name) {
		return (LabeledIntEdgePluggable) this.internal.createLabeledIntEdge(name, this.labeledIntValueMapImpl);
	}

	
	
	/**
	 * @param n dimension
	 * @return a new LabeledIntEdge array of size n.
	 */
	public LabeledIntEdgePluggable[] create(int n) {
		return (LabeledIntEdgePluggable[]) Array.newInstance(this.internal.getClass(), n);
	}

	
	/**
	 * @return the class chosen for creating new object. 
	 */
	public Class<C> getInternalObjectClass() {
		return this.labeledIntValueMapImpl;
	}
	
	public String toString() {
		return "Labeled value set managed as " + this.internal.labeledValueMapFactory.toString();
	}

}
