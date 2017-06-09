package it.univr.di.cstnu.graph;

/**
 * Created on Jul 21, 2005 Copyright (c) 2005, the JUNG Project and the Regents of the University of California All rights reserved. This software is
 * open-source under the BSD license; see either "license.txt" or http://jung.sourceforge.net/license.txt for a description. [07/02/2012] Made seriazable by
 * Posenato
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;

/**
 * StaticLayout places the vertices in the locations specified by its Transformer&lt;V,Point2D&gt; initializer. 
 * Vertex locations can be placed in a Map&lt;V,Point2D&gt; and then supplied to this layout as follows: <pre>
 *            Transformer&lt;V,Point2D&gt; vertexLocations =
 *        	TransformerUtils.mapTransformer(map);
 * </pre>
 *
 * @author Tom Nelson - tomnelson@dev.java.net
 * @param <V> vertex type
 * @param <E> edge type
 * @version $Id: $Id
 */
public class StaticLayout<V, E> extends AbstractLayout<V, E> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	static public Transformer<LabeledNode, Point2D> positionInitializer = new Transformer<LabeledNode, Point2D>() {
		@Override
		public Point2D transform(final LabeledNode v) {
			final Point2D p = new Point2D.Double(v.getX(), v.getY());
			return p;
		}
	};

	/**
	 * Creates an instance for the specified graph and default size; vertex locations are randomly assigned.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 */
	public StaticLayout(final Graph<V, E> graph) {
		super(graph);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public StaticLayout(final Graph<V, E> graph, final Dimension size) {
		super(graph, size);
	}

	/**
	 * Creates an instance for the specified graph and locations, with default size.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param initializer a {@link org.apache.commons.collections15.Transformer} object.
	 */
	public StaticLayout(final Graph<V, E> graph, final Transformer<V, Point2D> initializer) {
		super(graph, initializer);
	}

	/**
	 * Creates an instance for the specified graph, locations, and size.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param initializer a {@link org.apache.commons.collections15.Transformer} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public StaticLayout(final Graph<V, E> graph, final Transformer<V, Point2D> initializer, final Dimension size) {
		super(graph, initializer, size);
	}

	/**
	 * <p>getLocations.</p>
	 *
	 * @return the position of all vertices.
	 */
	public Map<V, Point2D> getLocations() {
		return this.locations;
	}

	/** {@inheritDoc} */
	@Override
	public void initialize() {
		// empty
	}

	/** {@inheritDoc} */
	@Override
	public void reset() {
		// empty
	}

}
