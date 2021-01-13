// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.visualization;

import java.awt.Cursor;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.logging.Logger;

import javax.swing.JComponent;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EdgeSupport;
import edu.uci.ics.jung.visualization.control.SimpleEdgeSupport;
import edu.uci.ics.jung.visualization.control.SimpleVertexSupport;
import edu.uci.ics.jung.visualization.control.VertexSupport;

/**
 * A plugin that can create vertices, undirected edges, and directed edges
 * using mouse gestures.
 * vertexSupport and edgeSupport member classes are responsible for actually
 * creating the new graph elements, and for repainting the view when changes
 * were made.
 *
 * @author Tom Nelson, Roberto Posenato
 * @param <V> generic type extending for Node
 * @param <E> generic type extending for Edge
 * @version $Id: $Id
 */
public class EditingGraphMousePlugin<V, E> extends edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin implements
		MouseListener, MouseMotionListener {

	/**
	 * logger della classe
	 */
	static Logger LOG = Logger.getLogger(EditingGraphMousePlugin.class.getName());

	@SuppressWarnings("javadoc")
	protected VertexSupport<V, E> vertexSupport;
	@SuppressWarnings("javadoc")
	protected EdgeSupport<V, E> edgeSupport;
	private Creating createMode = Creating.UNDETERMINED;

	private enum Creating {
		EDGE, VERTEX, UNDETERMINED
	}

	/**
	 * Mask for button
	 */
	static int shiftButtonDownMask = InputEvent.BUTTON1_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;

	/**
	 * Creates an instance and prepares shapes for visual effects,
	 * using the default modifiers of designed button (button 1).
	 *
	 * @param vertexFactory for creating vertices
	 * @param edgeFactory for creating edges
	 */
	public EditingGraphMousePlugin(Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
		this(shiftButtonDownMask, vertexFactory, edgeFactory);
	}

	/**
	 * Creates an instance and prepares shapes for visual effects.
	 * 
	 * @param modifiers1 the mouse event modifiers to use
	 * @param vertexFactory for creating vertices
	 * @param edgeFactory for creating edges
	 */
	private EditingGraphMousePlugin(int modifiers1, Supplier<V> vertexFactory, Supplier<E> edgeFactory) {
		super(modifiers1);
		this.cursor = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		this.vertexSupport = new SimpleVertexSupport<>(vertexFactory);
		this.edgeSupport = new SimpleEdgeSupport<>(edgeFactory);
	}

	/**
	 * {@inheritDoc}
	 * Overridden to be more flexible, and pass events with
	 * key combinations. The default responds to both ButtonOne
	 * and ButtonOne+Shift
	 */
	@Override
	public boolean checkModifiers(MouseEvent e) {
		// LOG.severe("checkModifiers: " + e.getModifiersEx() + " this.modifiers: " + this.modifiers);
		return (e.getModifiersEx() & this.modifiers) == this.modifiers;
	}

	/**
	 * {@inheritDoc}
	 * If the mouse is pressed in an empty area, create a new vertex there.
	 * If the mouse is pressed on an existing vertex, prepare to create
	 * an edge from that vertex to another
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// LOG.severe("mousePressed: " + e);
		if (checkModifiers(e)) {
			@SuppressWarnings("unchecked")
			final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
			final Point2D p = e.getPoint();
			GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
			if (pickSupport != null) {
				final V vertex = pickSupport.getVertex(vv.getModel().getGraphLayout(), p.getX(), p.getY());
				if (vertex != null) { // get ready to make an edge
					this.createMode = Creating.EDGE;
					// Graph<V,E> graph = vv.getModel().getGraphLayout().getGraph();
					// set default edge type
					EdgeType edgeType = EdgeType.DIRECTED;// (graph instanceof DirectedGraph) ? EdgeType.DIRECTED : EdgeType.UNDIRECTED;
					// if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0) {// && graph instanceof UndirectedGraph == false) {
					// edgeType = EdgeType.DIRECTED;
					// }
					this.edgeSupport.startEdgeCreate(vv, vertex, e.getPoint(), edgeType);
				} else { // make a new vertex
					// this.createMode = Creating.VERTEX;
					// this.vertexSupport.startVertexCreate(vv, e.getPoint());
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * If startVertex is non-null, and the mouse is released over an
	 * existing vertex, create an undirected edge from startVertex to
	 * the vertex under the mouse pointer. If shift was also pressed,
	 * create a directed edge instead.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void mouseReleased(MouseEvent e) {
		// LOG.severe("mouseReleased: " + e);
		// if(checkModifiers(e)) {
		final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		final Point2D p = e.getPoint();
		Layout<V, E> layout = vv.getGraphLayout();
		if (this.createMode == Creating.EDGE) {
			GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
			V vertex = null;
			if (pickSupport != null) {
				vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
			}
			this.edgeSupport.endEdgeCreate(vv, vertex);
		} else if (this.createMode == Creating.VERTEX) {
			this.vertexSupport.endVertexCreate(vv, e.getPoint());
		}
		// }
		this.createMode = Creating.UNDETERMINED;
	}

	/**
	 * {@inheritDoc}
	 * If startVertex is non-null, stretch an edge shape between
	 * startVertex and the mouse pointer to simulate edge creation
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void mouseDragged(MouseEvent e) {
		if (checkModifiers(e)) {
			VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
			if (this.createMode == Creating.EDGE) {
				this.edgeSupport.midEdgeCreate(vv, e.getPoint());
			} else if (this.createMode == Creating.VERTEX) {
				this.vertexSupport.midVertexCreate(vv, e.getPoint());
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public void mouseClicked(MouseEvent e) {
		// empty block
	}

	/** {@inheritDoc} */
	@Override
	public void mouseEntered(MouseEvent e) {
		JComponent c = (JComponent) e.getSource();
		c.setCursor(this.cursor);
	}

	/** {@inheritDoc} */
	@Override
	public void mouseExited(MouseEvent e) {
		JComponent c = (JComponent) e.getSource();
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/** {@inheritDoc} */
	@Override
	public void mouseMoved(MouseEvent e) {
		// empty block
	}

	/**
	 * <p>
	 * Getter for the field <code>vertexSupport</code>.
	 * </p>
	 *
	 * @return a {@link edu.uci.ics.jung.visualization.control.VertexSupport} object.
	 */
	public VertexSupport<V, E> getVertexSupport() {
		return this.vertexSupport;
	}

	/**
	 * <p>
	 * Setter for the field <code>vertexSupport</code>.
	 * </p>
	 *
	 * @param vertexSupport1 a {@link edu.uci.ics.jung.visualization.control.VertexSupport} object.
	 */
	public void setVertexSupport(VertexSupport<V, E> vertexSupport1) {
		this.vertexSupport = vertexSupport1;
	}

	/**
	 * <p>
	 * Getter for the field <code>edgeSupport</code>.
	 * </p>
	 *
	 * @return a {@link edu.uci.ics.jung.visualization.control.EdgeSupport} object.
	 */
	public EdgeSupport<V, E> getEdgeSupport() {
		return this.edgeSupport;
	}

	/**
	 * <p>
	 * Setter for the field <code>edgeSupport</code>.
	 * </p>
	 *
	 * @param edgeSupport1 a {@link edu.uci.ics.jung.visualization.control.EdgeSupport} object.
	 */
	public void setEdgeSupport(EdgeSupport<V, E> edgeSupport1) {
		this.edgeSupport = edgeSupport1;
	}

	/**
	 * <p>
	 * setEdgeFactory.
	 * </p>
	 *
	 * @param edgeFactory1 a {@link com.google.common.base.Supplier} object.
	 */
	public void setEdgeFactory(Supplier<E> edgeFactory1) {
		this.edgeSupport = new SimpleEdgeSupport<>(edgeFactory1);
	}

}
