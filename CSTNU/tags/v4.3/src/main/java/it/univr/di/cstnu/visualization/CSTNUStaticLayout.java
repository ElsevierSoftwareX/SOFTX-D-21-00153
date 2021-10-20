// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.visualization;

/**
 * [07/02/2012] Made serializable by Posenato
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;

import com.google.common.base.Function;
import com.google.common.cache.LoadingCache;

import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * Extends CSTNUStaticLayout retrieving initial node positions from node attributes.
 *
 * @param <E> edge type
 * @version $Id: $Id
 * @author posenato
 */
public class CSTNUStaticLayout<E> extends edu.uci.ics.jung.algorithms.layout.StaticLayout<LabeledNode, E> implements IterativeContext {

	/**
	 * Version
	 */
	// static final public String VERSIONandDATE = "1.0 - October, 20 2017";
	static final public String VERSIONandDATE = "1.1, June, 9 2019";// Refactoring Edge

	/**
	 * It is used for getting the coordinates of node stored inside LabelNode object.
	 */
	static public final Function<LabeledNode, Point2D> positionInitializer = new Function<>() {
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
	public CSTNUStaticLayout(final Graph<LabeledNode, E> graph1) {
		super(graph1, positionInitializer);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 *
	 * @param graph1 a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param size1 a {@link java.awt.Dimension} object.
	 */
	public CSTNUStaticLayout(final Graph<LabeledNode, E> graph1, final Dimension size1) {
		super(graph1, positionInitializer, size1);
	}

	/**
	 * <p>getLocations.</p>
	 *
	 * @return the position of all vertices.
	 */
	public LoadingCache<LabeledNode, Point2D> getLocations() {
		return this.locations;
	}

	/**
	 * {@inheritDoc}
	 * It has been erased.
	 */
	@Override
	public void initialize() {
		// empty
	}

	/**
	 * {@inheritDoc}
	 * It has been erased.
	 */
	@Override
	public void reset() {
		// empty
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return CSTNUStaticLayout.VERSIONandDATE;
	}

	/** {@inheritDoc} */
	@Override
	public void step() {
		// TODO Auto-generated method stub
	}

	/** {@inheritDoc} */
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}

}
