/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import com.google.common.base.Supplier;

/**
 * This supplier <b>requires</b> as E a class that implements {@link Edge} interface and that contains the following 3 constructors:
 * 
 * <pre>
 * 1. E(Class<? extends LabeledIntMap>)
 * 2. E(E, Class<? extends LabeledIntMap>)
 * 3. E(String, Class<? extends LabeledIntMap>)
 * </pre>
 * 
 * @author posenato
 * @param <E> type of edge
 */
public class EdgeSupplier<E extends Edge> implements Supplier<E> {

	static final public Class<? extends STNEdge> DEFAULT_STN_EDGE_CLASS = STNEdgeInt.class;
	static final public Class<? extends CSTNEdge> DEFAULT_CSTN_EDGE_CLASS = CSTNEdgePluggable.class;
	static final public Class<? extends CSTNUEdge> DEFAULT_CSTNU_EDGE_CLASS = CSTNUEdgePluggable.class;
	/**
	 * 
	 */
	// private Class<? extends LabeledIntMap> labeledIntValueMapImpl;
	/**
	 * 
	 */
	private E generator;

	/**
	 * 
	 */
	private Class<? extends E> generatorClass;

	/**
	 * @param defaultStnEdgeClass
	 */
	public EdgeSupplier(Class<? extends E> defaultStnEdgeClass) {// , Class<? extends LabeledIntMap> labeledIntMapImplClass
		super();
		this.generatorClass = defaultStnEdgeClass;
//		this.labeledIntValueMapImpl = labeledIntMapImplClass;
		try {
			// if (labeledIntMapImplClass != null) {
			// this.generator = edgeImplClass.getDeclaredConstructor(new Class[] { this.labeledIntValueMapImpl.getClass() })
			// .newInstance(new Object[] { this.labeledIntValueMapImpl });
			// } else {
			this.generator = defaultStnEdgeClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
			// }
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Problem construccting an edge supplier: " + e.getMessage());
		}
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	@Override
	public E get() {
		return this.generatorClass.cast(this.generator.newInstance());// this.labeledIntValueMapImpl
	}

	/**
	 * @param edge the edge to clone.
	 * @return a new edge
	 */
	public E get(Edge edge) {
		return this.generatorClass.cast(this.generator.newInstance(edge));// , this.labeledIntValueMapImpl
	}

	/**
	 * @param name a name for the new edge
	 * @return a new edge
	 */
	public E get(String name) {
		return this.generatorClass.cast(this.generator.newInstance(name));// , this.labeledIntValueMapImpl
	}

	/**
	 * @param edgeClass
	 * @param n dimension
	 * @return a new LabeledIntEdge array of size n.
	 */
	@SuppressWarnings("unchecked")
	public static <K extends Edge> K[] get(Class<K> edgeClass, int n) {
		return (K[]) Array.newInstance(edgeClass, n);
	}

	/**
	 * @return the class chosen for creating new labeled value map.
	 *         public Class<? extends LabeledIntMap> getLabeledIntValueMapImplClass() {
	 *         return this.labeledIntValueMapImpl;
	 *         }
	 */

	/**
	 * @return the class chosen for creating new edge.
	 */
	public Class<? extends E> getEdgeImplClass() {
		return this.generatorClass;
	}

	@Override
	public String toString() {
		return "Edge type: " + this.generatorClass.toString();// + ". Labeled Value Set type: " + this.labeledIntValueMapImpl.toString();
	}

}
