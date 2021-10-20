// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;

import com.google.common.base.Supplier;

/**
 * This supplier <b>requires</b> as E a class that implements {@link it.univr.di.cstnu.graph.Edge} interface and that contains the following 3 constructors:
 * <ol>
 * <li>E(Class&lt;? extends LabeledIntMap&gt;)
 * <li>E(E, Class&lt;? extends LabeledIntMap&gt;)
 * <li>E(String, Classv&lt;? extends LabeledIntMap&gt;)
 * </ol>
 *
 * @author posenato
 * @param <E> type of edge
 * @version $Id: $Id
 */
public class EdgeSupplier<E extends Edge> implements Supplier<E> {

	/**
	 * 
	 */
	static final public Class<? extends STNEdge> DEFAULT_STN_EDGE_CLASS = STNEdgeInt.class;
	/**
	 * 
	 */
	static final public Class<? extends STNUEdge> DEFAULT_STNU_EDGE_CLASS = STNUEdgeInt.class;
	/**
	 * 
	 */
	static final public Class<? extends CSTNEdge> DEFAULT_CSTN_EDGE_CLASS = CSTNEdgePluggable.class;
	/**
	 * 
	 */
	static final public Class<? extends CSTNUEdge> DEFAULT_CSTNU_EDGE_CLASS = CSTNUEdgePluggable.class;
	/**
	 * 
	 */
	static final public Class<? extends CSTNPSUEdge> DEFAULT_CSTNPSU_EDGE_CLASS = CSTNPSUEdgePluggable.class;
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
	 * <p>
	 * Constructor for EdgeSupplier.
	 * </p>
	 *
	 * @param defaultStnEdgeClass vg
	 */
	public EdgeSupplier(Class<? extends E> defaultStnEdgeClass) {// , Class<? extends LabeledIntMap> labeledIntMapImplClass
		super();
		this.generatorClass = defaultStnEdgeClass;
		// this.labeledIntValueMapImpl = labeledIntMapImplClass;
		try {
			// if (labeledIntMapImplClass != null) {
			// this.generator = edgeImplClass.getDeclaredConstructor(new Class[] { this.labeledIntValueMapImpl.getClass() })
			// .newInstance(new Object[] { this.labeledIntValueMapImpl });
			// } else {
			this.generator = defaultStnEdgeClass.getDeclaredConstructor((Class<?>[]) null).newInstance((Object[]) null);
			// }
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Problem constructing an edge supplier: " + e.getMessage());
		}
	}

	/** {@inheritDoc} */
	@Override
	public E get() {
		return this.generatorClass.cast(this.generator.newInstance());// this.labeledIntValueMapImpl
	}

	/**
	 * <p>
	 * get.
	 * </p>
	 *
	 * @param edge the edge to clone.
	 * @return a new edge
	 */
	public E get(Edge edge) {
		return this.generatorClass.cast(this.generator.newInstance(edge));// , this.labeledIntValueMapImpl
	}

	/**
	 * <p>
	 * get.
	 * </p>
	 *
	 * @param name a name for the new edge
	 * @return a new edge
	 */
	public E get(String name) {
		return this.generatorClass.cast(this.generator.newInstance(name));// , this.labeledIntValueMapImpl
	}

	/**
	 * @param <K> type of edge
	 *
	 * @param edgeClass dd
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "Edge type: " + this.generatorClass.toString();// + ". Labeled Value Set type: " + this.labeledIntValueMapImpl.toString();
	}

}
