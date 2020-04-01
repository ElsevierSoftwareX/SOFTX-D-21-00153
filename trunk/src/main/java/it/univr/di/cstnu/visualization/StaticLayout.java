package it.univr.di.cstnu.visualization;

/**
 * 
 * [07/02/2012] Made serializable by Posenato
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.Serializable;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * Extends StaticLayout retrieving initial node positions from node attributes.
 * 
 * @param <E> edge type
 * @version $Id: $Id
 */
public class StaticLayout<E> extends edu.uci.ics.jung.algorithms.layout.StaticLayout<LabeledNode, E> implements Serializable, IterativeContext {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Version
	 */
	// static final public String VERSIONandDATE = "1.0 - October, 20 2017";
	static final public String VERSIONandDATE = "1.1, June, 9 2019";// Refactoring Edge

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
	 * Creates an instance for the specified graph and default size; vertex locations are determined by {@link #positionInitializer}.
	 *
	 * @param graph1 a {@link edu.uci.ics.jung.graph.Graph} object.
	 */
	public StaticLayout(final Graph<LabeledNode, E> graph1) {
		super(graph1, positionInitializer);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 *
	 * @param graph1 a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param size1 a {@link java.awt.Dimension} object.
	 */
	public StaticLayout(final Graph<LabeledNode, E> graph1, final Dimension size1) {
		super(graph1, positionInitializer, size1);
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
	
	@Override
	public String toString() {
		return StaticLayout.VERSIONandDATE;
	}

	@Override
	public void step() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}

}
