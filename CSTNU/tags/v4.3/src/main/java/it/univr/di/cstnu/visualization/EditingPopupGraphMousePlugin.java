// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.visualization;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * 2017-10-23 I added a menu item to manage the export of a graph.
 *
 * @author Roberto Posenato
 * @param <V> generic type extending for Node
 * @param <E> generic type extending for Edge
 * @version $Id: $Id
 */
public class EditingPopupGraphMousePlugin<V extends LabeledNode, E extends Edge> extends AbstractPopupGraphMousePlugin {

	/**
	 * 
	 */
	Supplier<V> vertexFactory;
	/**
	 * 
	 */
	Supplier<E> edgeFactory;

	/**
	 * <p>
	 * Constructor for EditingPopupGraphMousePlugin.
	 * </p>
	 *
	 * @param vertexFactory1 a {@link com.google.common.base.Supplier} object.
	 * @param edgeFactory1 a {@link com.google.common.base.Supplier} object.
	 */
	public EditingPopupGraphMousePlugin(Supplier<V> vertexFactory1, Supplier<E> edgeFactory1) {
		this.vertexFactory = vertexFactory1;
		this.edgeFactory = edgeFactory1;
	}

	/**
	 * <p>
	 * Setter for the field <code>edgeFactory</code>.
	 * </p>
	 *
	 * @param edgeFactory1 a {@link com.google.common.base.Supplier} object.
	 */
	public void setEdgeFactory(Supplier<E> edgeFactory1) {
		this.edgeFactory = edgeFactory1;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings({ "unchecked", "serial" })
	protected void handlePopup(MouseEvent e) {
		final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
		final Layout<V, E> layout = vv.getGraphLayout();
		final Graph<V, E> graph = layout.getGraph();
		final Point2D p = e.getPoint();

		GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
		if (pickSupport != null) {

			final V vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
			final E edge = pickSupport.getEdge(layout, p.getX(), p.getY());
			final PickedState<V> pickedVertexState = vv.getPickedVertexState();
			final PickedState<E> pickedEdgeState = vv.getPickedEdgeState();

			JPopupMenu popup = new JPopupMenu();
			if (vertex != null) {
				Set<V> picked = pickedVertexState.getPicked();
				if (picked.size() > 0) {
					JMenu directedMenu = new JMenu("Add edge");
					popup.add(directedMenu);
					for (final V other : picked) {
						if (other.equalsByName(vertex))
							continue;
						directedMenu.add(new AbstractAction("[" + other + "→" + vertex + "]") {
							@Override
							public void actionPerformed(ActionEvent e1) {
								E newEdge = EditingPopupGraphMousePlugin.this.edgeFactory.get();
								graph.addEdge(newEdge, other, vertex, EdgeType.DIRECTED);
								vv.repaint();
							}
						});
						directedMenu.add(new AbstractAction("[" + vertex + "→" + other + "]") {
							@Override
							public void actionPerformed(ActionEvent e1) {
								E newEdge = EditingPopupGraphMousePlugin.this.edgeFactory.get();
								graph.addEdge(newEdge, vertex, other, EdgeType.DIRECTED);
								vv.repaint();
							}
						});
					}
				}

				popup.add(new AbstractAction("Delete node") {
					@Override
					public void actionPerformed(ActionEvent a) {
						pickedVertexState.pick(vertex, false);
						graph.removeVertex(vertex);
						vv.repaint();
					}
				});
			} else if (edge != null) {
				popup.add(new AbstractAction("Delete edge") {
					@Override
					public void actionPerformed(ActionEvent a) {
						pickedEdgeState.pick(edge, false);
						graph.removeEdge(edge);
						vv.repaint();
					}
				});
			} else {
				popup.add(new AbstractAction("Add node") {
					@Override
					public void actionPerformed(ActionEvent a) {
						V newVertex = EditingPopupGraphMousePlugin.this.vertexFactory.get();
						graph.addVertex(newVertex);
						layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p));
						vv.repaint();
					}
				});
				//Unfortunately, FREEHEP 2.4 library is not more JDK11 compatible... I have to disable till a new version or workaround is found! 
//				popup.add(new AbstractAction("Export graph to vector image...") {
//					@Override
//					public void actionPerformed(ActionEvent a) {
//						ExportDialog export = new ExportDialog("Roberto Posenato");
//						VisualizationImageServer<V, E> vis = new VisualizationImageServer<>(vv.getGraphLayout(), vv.getGraphLayout().getSize());
//						TNEditor.setNodeEdgeRenders((BasicVisualizationServer<LabeledNode, Edge>) vis, false);
//						export.showExportDialog(vv.getParent(), "Export view as ...", vis, "cstnExported.pdf");
//					}
//				});
			}
			if (popup.getComponentCount() > 0) {
				popup.show(vv, e.getX(), e.getY());
			}
		}
	}
}
