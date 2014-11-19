package it.univr.di.cstnu;

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
 * StaticLayout places the vertices in the locations specified by its Transformer<V,Point2D> initializer. Vertex locations can be placed in a Map<V,Point2D> and
 * then supplied to this layout as follows: <code>
            Transformer<V,Point2D> vertexLocations =
        	TransformerUtils.mapTransformer(map);
 * </code>
 * 
 * @author Tom Nelson - tomnelson@dev.java.net
 * @param <V>
 * @param <E>
 */
public class StaticLayout<V, E> extends AbstractLayout<V, E> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	static public Transformer<Node, Point2D> positionInitializer = new Transformer<Node, Point2D>() {
		@Override
		public Point2D transform(Node v) {
			final Point2D p = new Point2D.Double(v.getX(), v.getY());
			return p;
		}
	};

	/**
	 * 
	 */
	@SuppressWarnings("unchecked")
	public StaticLayout() {
		this((Graph<V, E>) new it.univr.di.cstnu.Graph());
	}

	/**
	 * Creates an instance for the specified graph and default size; vertex locations are randomly assigned.
	 * 
	 * @param graph
	 */
	public StaticLayout(Graph<V, E> graph) {
		super(graph);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 * 
	 * @param graph
	 * @param size
	 */
	public StaticLayout(Graph<V, E> graph, Dimension size) {
		super(graph, size);
	}

	/**
	 * Creates an instance for the specified graph and locations, with default size.
	 * 
	 * @param graph
	 * @param initializer
	 */
	public StaticLayout(Graph<V, E> graph, Transformer<V, Point2D> initializer) {
		super(graph, initializer);
	}

	/**
	 * Creates an instance for the specified graph, locations, and size.
	 * 
	 * @param graph
	 * @param initializer
	 * @param size
	 */
	public StaticLayout(Graph<V, E> graph, Transformer<V, Point2D> initializer, Dimension size) {
		super(graph, initializer, size);
	}

	/**
	 * @return the position of all vertices.
	 */
	public Map<V, Point2D> getLocations() {
		return locations;
	}

	@Override
	public void initialize() {
		// empty
	}

	@Override
	public void reset() {
		// empty
	}

}
