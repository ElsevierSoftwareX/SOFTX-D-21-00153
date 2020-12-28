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

import org.freehep.graphicsbase.util.export.ExportDialog;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * 2017-10-23 I added a menu item to manage the export of a graph.
 * 
 * @author Roberto Posenato
 * @param <V>
 * @param <E>
 */
public class EditingPopupGraphMousePlugin<V extends LabeledNode, E extends Edge> extends AbstractPopupGraphMousePlugin {

	Supplier<V> vertexFactory;
	Supplier<E> edgeFactory;
	public EditingPopupGraphMousePlugin(Supplier<V> vertexFactory1, Supplier<E> edgeFactory1) {
		this.vertexFactory = vertexFactory1;
		this.edgeFactory = edgeFactory1;
	}

	public void setEdgeFactory(Supplier<E> edgeFactory1) {
		this.edgeFactory = edgeFactory1;
	}

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
					JMenu directedMenu = new JMenu("Create Edge");
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

				popup.add(new AbstractAction("Delete Vertex") {
					@Override
					public void actionPerformed(ActionEvent a) {
						pickedVertexState.pick(vertex, false);
						graph.removeVertex(vertex);
						vv.repaint();
					}
				});
			} else if (edge != null) {
				popup.add(new AbstractAction("Delete Edge") {
					@Override
					public void actionPerformed(ActionEvent a) {
						pickedEdgeState.pick(edge, false);
						graph.removeEdge(edge);
						vv.repaint();
					}
				});
			} else {
				popup.add(new AbstractAction("Create Vertex") {
					@Override
					public void actionPerformed(ActionEvent a) {
						V newVertex = EditingPopupGraphMousePlugin.this.vertexFactory.get();
						graph.addVertex(newVertex);
						layout.setLocation(newVertex, vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p));
						vv.repaint();
					}
				});
				popup.add(new AbstractAction("Export graph to vector image...") {
					@Override
					public void actionPerformed(ActionEvent a) {
						ExportDialog export = new ExportDialog("Roberto Posenato");
						VisualizationImageServer<V, E> vis = new VisualizationImageServer<>(vv.getGraphLayout(), vv.getGraphLayout().getSize());
						CSTNEditor.setNodeEdgeRenders((BasicVisualizationServer<LabeledNode, Edge>) vis, false);
						export.showExportDialog(vv.getParent(), "Export view as ...", vis, "cstnExported.pdf");
					}
				});
			}
			if (popup.getComponentCount() > 0) {
				popup.show(vv, e.getX(), e.getY());
			}
		}
	}
}
