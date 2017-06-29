package it.univr.di.cstnu.visualization;

/**
 * 
 * [07/02/2012] Made seriazable by
 * Posenato
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.Serializable;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.graph.Graph;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * Extends StaticLayout setting the following initializer:
 * static public Function<LabeledNode, Point2D> positionInitializer = new Function<LabeledNode, Point2D>() {
 * 
 * @Override
 * 			public Point2D apply(final LabeledNode v) {
 *           final Point2D p = new Point2D.Double(v.getX(), v.getY());
 *           return p;
 *           }
 *           };
 * @param <E> edge type
 * @version $Id: $Id
 */
public class StaticLayout<E> extends edu.uci.ics.jung.algorithms.layout.StaticLayout<LabeledNode, E> implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * It is used for getting the coordinates of node stored inside LabelNode object.
	 */
	static public Function<LabeledNode, Point2D> positionInitializer = new Function<LabeledNode, Point2D>() {
		@Override
		public Point2D apply(final LabeledNode v) {
			final Point2D p = new Point2D.Double(v.getX(), v.getY());
			return p;
		}
	};

	/**
	 * Creates an instance for the specified graph and default size; vertex locations are randomly assigned.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 */
	public StaticLayout(final Graph<LabeledNode, E> graph) {
		super(graph, positionInitializer);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public StaticLayout(final Graph<LabeledNode, E> graph, final Dimension size) {
		super(graph, positionInitializer, size);
	}

	/**
	 * @return the position of all vertices.
	 */
	public LoadingCache<LabeledNode, Point2D> getLocations() {
		return this.locations;
	}

	/** It has been erased. */
	@Override
	public void initialize() {
		// empty
	}

	/** It has been erased. */
	@Override
	public void reset() {
		// empty
	}

}
