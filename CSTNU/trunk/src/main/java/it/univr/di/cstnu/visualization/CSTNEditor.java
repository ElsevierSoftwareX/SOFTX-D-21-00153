package it.univr.di.cstnu.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.xml.sax.SAXException;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.AbstractCSTN;
import it.univr.di.cstnu.algorithms.AbstractCSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.AbstractCSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.AbstractCSTN.EdgesToCheck;
import it.univr.di.cstnu.algorithms.CSTN;
import it.univr.di.cstnu.algorithms.CSTNEpsilon;
import it.univr.di.cstnu.algorithms.CSTNEpsilon3R;
import it.univr.di.cstnu.algorithms.CSTNIR;
import it.univr.di.cstnu.algorithms.CSTNIR3R;
import it.univr.di.cstnu.algorithms.CSTNPSU;
import it.univr.di.cstnu.algorithms.CSTNPotential;
import it.univr.di.cstnu.algorithms.CSTNU;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.algorithms.CSTNU2CSTN;
import it.univr.di.cstnu.algorithms.STN;
import it.univr.di.cstnu.algorithms.STN.STNCheckStatus;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.LabeledNodeSupplier;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraph.NetworkType;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.labeledvalue.Constants;

/**
 * A simple graphical application for creating/loading/modifying/saving/checking CSTNs.
 * It is based on EdgePluggable.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNEditor extends JFrame implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @author posenato
	 */
	private class ContingentAlsoAsOrdinaryListener implements ItemListener {

		public ContingentAlsoAsOrdinaryListener() {
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			CSTNEditor.this.contingentAlsoAsOrdinary = e.getStateChange() == ItemEvent.SELECTED;
		}
	}

	/**
	 * @author posenato
	 */
	private class BigViewerListener implements ActionListener {

		boolean isInputGraphLayoutToShow;

		public BigViewerListener(boolean isInputGraphLayoutToShow1) {
			this.isInputGraphLayoutToShow = isInputGraphLayoutToShow1;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JDialog frame = new JDialog(CSTNEditor.this,
					(this.isInputGraphLayoutToShow) ? CSTNEditor.INPUT_GRAPH_BIG_VIEWER_NAME : CSTNEditor.DERIVED_GRAPH_BIG_VIEWER_NAME);
			frame.setBounds(CSTNEditor.this.getBounds());
			Dimension bvvDim = new Dimension(frame.getWidth(), frame.getHeight() - 100);

			AbstractLayout<LabeledNode, ? extends Edge> layout = (this.isInputGraphLayoutToShow) ? CSTNEditor.this.layoutEditor : CSTNEditor.this.layoutViewer;
			VisualizationViewer<LabeledNode, ? extends Edge> bvv = new VisualizationViewer<>(layout, bvvDim);
			bvv.setName(CSTNEditor.INPUT_GRAPH_BIG_VIEWER_NAME);
			buildRenderContext(bvv, true);
			((ModalGraphMouse) bvv.getGraphMouse()).setMode(ModalGraphMouse.Mode.TRANSFORMING);
			final JPanel rowForAppButtons = new JPanel();
			@SuppressWarnings("unchecked")
			final JComboBox<Mode> modeBox = ((EditingModalGraphMouse<LabeledNode, Edge>) bvv.getGraphMouse()).getModeComboBox();
			rowForAppButtons.add(modeBox);
			JButton close = new JButton(new AbstractAction("Close") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent event) {
					frame.dispose();
				}
			});
			rowForAppButtons.add(close);
			frame.setLayout(new FlowLayout(FlowLayout.CENTER));
			frame.add(new JLabel(getGraphLabelDescription(((TNGraph<?>) CSTNEditor.this.layoutEditor.getGraph()))));
			frame.add(bvv);
			frame.add(rowForAppButtons);
			frame.setVisible(true);
			frame.validate();
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNAPSPListener implements ActionListener {

		public CSTNAPSPListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());

			jl.setBackground(Color.orange);
			boolean consistent = false;
			CSTNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
			consistent = AbstractCSTN.getMinimalDistanceGraph(CSTNEditor.this.cstn.getG());
			if (consistent) {

				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is All-Pair Shortest Paths.");
				jl.setBackground(Color.green);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						CSTNEditor.LOG.finer("All-Pair Shortest Paths graph: " + CSTNEditor.this.cstn.getGChecked());
					}
				}
			} else {
				// The distance graph is not consistent
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
				// jl.setIcon(CSTNEditor.warnIcon);
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNCheckListener implements ActionListener {

		public CSTNCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			switch (CSTNEditor.this.dcCurrentSem) {
			case ε:
				CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, (TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			case IR:
				CSTNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			default:
				CSTNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			}
			CSTNEditor.this.cstn.setOutputCleaned(CSTNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
				if (CSTNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstn.getGChecked());
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
					// jl.setIcon(CSTNEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class STNConsistencyCheckListener implements ActionListener {

		public STNConsistencyCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			CSTNEditor.this.stn = new STN((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph);
			CSTNEditor.this.stn.setDefaultConsistencyCheckAlg(CSTNEditor.this.stnCheckAlg);
			CSTNEditor.this.stn.setOutputCleaned(CSTNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.stnStatus = CSTNEditor.this.stn.consistencyCheck();
				if (CSTNEditor.this.stnStatus.consistency) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is STN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.stn.getGChecked());
						}
					}
				} else {
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent.</b>");
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class STNDispatchableListener implements ActionListener {

		public STNDispatchableListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			CSTNEditor.this.stn = new STN((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph);
			CSTNEditor.this.stn.setDefaultConsistencyCheckAlg(CSTNEditor.this.stnCheckAlg);
			CSTNEditor.this.stn.setOutputCleaned(CSTNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			boolean status = CSTNEditor.this.stn.makeDispatchable();
			if (status) {

				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is dispatchable.");
				jl.setBackground(Color.green);
			} else {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent.</b>");
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class STNPredecessorGraphListener implements ActionListener {

		public STNPredecessorGraphListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			CSTNEditor.this.stn = new STN((TNGraph<STNEdge>) CSTNEditor.this.inputGraph);
			CSTNEditor.this.stn.setOutputCleaned(CSTNEditor.this.cleanResult);

			LabeledNode node = null;
			while (node == null) {
				LabeledNode[] nodes = CSTNEditor.this.stn.getG().getVerticesArray();
				node = (LabeledNode) JOptionPane.showInputDialog(
						CSTNEditor.this.rowForSTNButtons,
						"Chose the source node:",
						"Customized Dialog",
						JOptionPane.PLAIN_MESSAGE,
						null,
						nodes,
						"Z");
			}
			jl.setBackground(Color.orange);
			TNGraph<STNEdge> g1 = CSTNEditor.this.stn.getSTNPredecessorGraph(node);
			if (g1 != null) {
				((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);
				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>Predecessor graphs of " + node.getName() + ".");
				jl.setBackground(Color.green);
			} else {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent.</b>");
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNInitListener implements ActionListener {

		public CSTNInitListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			switch (CSTNEditor.this.dcCurrentSem) {
			case ε:
				CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, (TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			case IR:
				CSTNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			default:
				CSTNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			}
			try {
				CSTNEditor.this.cstn.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						CSTNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// CSTNEditor.this.vv2.validate();
				// CSTNEditor.this.vv2.repaint();
				CSTNEditor.this.validate();
				CSTNEditor.this.repaint();
				return;
			}
			jl.setText("CSTN initialized.");
			// jl.setIcon(CSTNEditor.infoIcon);
			jl.setOpaque(true);
			jl.setBackground(Color.orange);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class STNInitListener implements ActionListener {

		public STNInitListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			CSTNEditor.this.stn = new STN((TNGraph<STNEdge>) CSTNEditor.this.checkedGraph);
			try {
				CSTNEditor.this.stn.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						CSTNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// CSTNEditor.this.vv2.validate();
				// CSTNEditor.this.vv2.repaint();
				CSTNEditor.this.validate();
				CSTNEditor.this.repaint();
				return;
			}
			jl.setText("STN initialized.");
			// jl.setIcon(CSTNEditor.infoIcon);
			jl.setOpaque(true);
			jl.setBackground(Color.orange);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNOneStepListener implements ActionListener {

		public CSTNOneStepListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;

			if (CSTNEditor.this.cycle == -1)
				return;
			if (CSTNEditor.this.cycle == 0) {
				TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
				switch (CSTNEditor.this.dcCurrentSem) {
				case ε:
					CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, (TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
					break;
				case IR:
					CSTNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
					break;
				default:
					CSTNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
					break;
				}
				CSTNEditor.this.cstn.setOutputCleaned(CSTNEditor.this.cleanResult);

				try {
					CSTNEditor.this.cstn.initAndCheck();
				} catch (final Exception ex) {
					jl.setText("There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}
				CSTNEditor.this.oneStepBackGraph = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				CSTNEditor.this.cstnStatus = new CSTNCheckStatus();
			} else {
				TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNEdge>) CSTNEditor.this.oneStepBackGraph).takeIn(g1);
				// CSTNEditor.this.cstn.setWithUnknown(CSTNEditor.this.withUknown);

			}
			CSTNEditor.this.cycle++;

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.oneStepDynamicConsistencyByNode();
				CSTNEditor.this.cstnStatus.finished = ((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph)
						.hasSameEdgesOf((TNGraph<CSTNEdge>) CSTNEditor.this.oneStepBackGraph);
				final boolean reductionsApplied = !CSTNEditor.this.cstnStatus.finished;
				final boolean inconsistency = !CSTNEditor.this.cstnStatus.consistency;
				if (inconsistency) {
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is inconsistent.<b>");
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINE)) {
							CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.oneStepBackGraph);
						}
						if (LOG.isLoggable(Level.INFO)) {
							CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnStatus);
						}
					}
				} else if (reductionsApplied) {
					jl.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
					// jl.setIcon(CSTNEditor.warnIcon);
				} else {
					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent. The number of executed cycles is "
							+ CSTNEditor.this.cycle);
					// jl.setIcon(CSTNEditor.infoIcon);
					CSTNEditor.this.cycle = -1;
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.INFO)) {
							CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnStatus);
						}
					}
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
				CSTNEditor.this.cycle = -1;
				return;
			}

			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNPSUCheckListener implements ActionListener {

		public CSTNPSUCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.onlyToZCB.setSelected(false);
			CSTNEditor.this.cstnu = new CSTNPSU((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNPSU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstnu.getGChecked());
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The CSTNPSU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNUEditor.warnIcon);
			}
			jl1.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNRestrictedCheckListener implements ActionListener {

		public CSTNRestrictedCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());

			jl.setBackground(Color.orange);
			switch (CSTNEditor.this.dcCurrentSem) {
			case ε:
				CSTNEditor.this.cstn = new CSTNEpsilon3R(CSTNEditor.this.reactionTime, (TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			case IR:
				CSTNEditor.this.cstn = new CSTNIR3R((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
				break;
			default:
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE
						+ "'>&nbsp;<b>There is no DC checking algorithm for std semantics and rules restricted to Z.</b>");
				jl.setOpaque(true);
				CSTNEditor.this.cycle = 0;
				return;
			}
			CSTNEditor.this.cstn.setOutputCleaned(CSTNEditor.this.cleanResult);

			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
				if (CSTNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstn.getGChecked());
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
					// jl.setIcon(CSTNEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNPotentialCheckListener implements ActionListener {

		public CSTNPotentialCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());

			jl.setBackground(Color.orange);
			CSTNEditor.this.cstn = new CSTNPotential((TNGraph<CSTNEdge>) CSTNEditor.this.checkedGraph);
			CSTNEditor.this.cstn.setOutputCleaned(CSTNEditor.this.cleanResult);

			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
				if (CSTNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstn.getGChecked());
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
					// jl.setIcon(CSTNEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
			}
			jl.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNSaveListener implements ActionListener {

		public CSTNSaveListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			// CSTNEditor.LOG.finest("Current dir:" + f);

			// CSTNEditor.LOG.finest("Path wanted:" + path);
			chooser = new JFileChooser(CSTNEditor.default_dir);
			final int option = chooser.showSaveDialog(CSTNEditor.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				if (CSTNEditor.this.cstn != null) {
					CSTNEditor.this.cstn.setfOutput(file);
					CSTNEditor.this.cstn.saveGraphToFile();
				}
				// aveGraphToFile(CSTNEditor.this.checkedGraph, file);
			}
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNU2CSTNCheckListener implements ActionListener {

		public CSTNU2CSTNCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.cstnu2cstn = new CSTNU2CSTN((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph, 30 * 60);

			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu2cstn.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstnu2cstn.getGChecked());
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The CSTNU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNUEditor.warnIcon);
			}
			jl1.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNUCheckListener implements ActionListener {

		public CSTNUCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			CSTNEditor.this.cstnu.setContingentAlsoAsOrdinary(CSTNEditor.this.contingentAlsoAsOrdinary);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.cstnu.getGChecked());
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The CSTNU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNUEditor.warnIcon);
			}
			jl1.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vvViewer.validate();
			CSTNEditor.this.vvViewer.repaint();

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNUInitListener implements ActionListener {

		public CSTNUInitListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) CSTNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

			CSTNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			CSTNEditor.this.cstnu.setContingentAlsoAsOrdinary(CSTNEditor.this.contingentAlsoAsOrdinary);
			try {
				CSTNEditor.this.cstnu.initAndCheck();
			} catch (final IllegalArgumentException | WellDefinitionException ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						CSTNEditor.LOG.warning(msg);
					}
				}
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNUEditor.warnIcon);
				jl1.setOpaque(true);
				jl1.setBackground(Color.orange);
				CSTNEditor.this.validate();
				CSTNEditor.this.repaint();
				return;
			}
			jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;TNGraph with Lower and Upper Case Labels.");
			// jl.setIcon(CSTNUEditor.infoIcon);
			jl1.setOpaque(true);
			jl1.setBackground(Color.orange);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
			CSTNEditor.this.cycle = 0;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNUOneStepListener implements ActionListener {

		public CSTNUOneStepListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			if (CSTNEditor.this.cycle == -1)
				return;
			if (CSTNEditor.this.cycle == 0) {
				TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) CSTNEditor.this.inputGraph,
						(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph).takeIn(g1);

				CSTNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
				CSTNEditor.this.cstnu.setContingentAlsoAsOrdinary(CSTNEditor.this.contingentAlsoAsOrdinary);
				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.checkedGraph.getEdgeFactory().toString());
				try {
					CSTNEditor.this.cstnu.initAndCheck();
				} catch (final Exception ex) {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}
				CSTNEditor.this.cstnuStatus = new CSTNUCheckStatus();
				CSTNEditor.this.edgesToCheck = new it.univr.di.cstnu.algorithms.AbstractCSTN.EdgesToCheck<>(CSTNEditor.this.checkedGraph.getEdges());
			}
			CSTNEditor.this.cycle++;

			jl1.setBackground(Color.orange);
			try {
				Instant timeOut = Instant.now().plusSeconds(2700);
				CSTNEditor.this.cstnuStatus = (CSTNEditor.this.onlyToZ)
						? CSTNEditor.this.cstnu.oneStepDynamicControllabilityLimitedToZ((EdgesToCheck<CSTNUEdge>) CSTNEditor.this.edgesToCheck, timeOut)
						: CSTNEditor.this.cstnu.oneStepDynamicControllability((EdgesToCheck<CSTNUEdge>) CSTNEditor.this.edgesToCheck, timeOut);
				CSTNEditor.this.cstnuStatus.finished = CSTNEditor.this.edgesToCheck.size() == 0;
				final boolean reductionsApplied = !CSTNEditor.this.cstnuStatus.finished;
				final boolean notControllable = !CSTNEditor.this.cstnuStatus.consistency;
				if (notControllable) {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is inconsistent.</b>");
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINE)) {
							CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.checkedGraph);
						}
						if (LOG.isLoggable(Level.INFO)) {
							CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
						}
					}
				} else if (reductionsApplied) {
					jl1.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
					// jl.setIcon(CSTNEditor.warnIcon);
				} else {
					jl1.setText("<b>The graph is CSTNU consistent. The number of executed cycles is " + CSTNEditor.this.cycle);
					// jl.setIcon(CSTNEditor.infoIcon);
					CSTNEditor.this.cycle = -1;
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.INFO)) {
							CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
						}
					}
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
				CSTNEditor.this.cycle = -1;
				return;
			}

			jl1.setOpaque(true);
			updateNodePositions();
			CSTNEditor.this.vvViewer.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
		}
	}

	/**
	 * @author posenato
	 */
	private class DCSemanticsListener implements ActionListener {
		JComboBox<DCSemantics> comboBox;

		public DCSemanticsListener(JComboBox<DCSemantics> comboBox1) {
			this.comboBox = comboBox1;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CSTNEditor.this.dcCurrentSem = (DCSemantics) this.comboBox.getSelectedItem();
			CSTNEditor.this.epsilonPanel.setVisible(CSTNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
		}
	}

	/**
	 * @author posenato
	 */
	private class STNCheckAlgListener implements ActionListener {
		JComboBox<STN.CheckAlgorithm> comboBox;

		public STNCheckAlgListener(JComboBox<STN.CheckAlgorithm> comboBox1) {
			this.comboBox = comboBox1;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			CSTNEditor.this.stnCheckAlg = (STN.CheckAlgorithm) this.comboBox.getSelectedItem();
		}
	}

	/**
	 * @author posenato
	 */
	private class HelpListener implements ActionListener {
		private static final String instructions = "<html>"
				+ "<h2>Simple CSTNU Editor " + CSTNEditor.VERSION + "</h2><h3>All Modes:</h3>"
				+ "<h3>Editing Mode:</h3>"
				+ "<ul>"
				+ "<li>Right-click an empty area to create a new Vertex o to export the graph"
				+ "<li>Left-click+Shift on a Vertex adds/removes Vertex selection"
				+ "<li>Left-click an empty area unselects all Vertices"
				+ "<li>Left+drag on a Vertex moves all selected Vertices"
				+ "<li>Left+drag elsewhere selects Vertices in a region"
				+ "<li>Left+Shift+drag adds selection of Vertices in a new region"
				+ "<li>Left+CTRL on a Vertex selects the vertex and centers the display on it"
				+ "<li>Left double-click on a vertex or edge allows you to edit the label"
				+ "<li>Right-click on a Vertex for <b>Delete Vertex</b> popup"
				+ "<li>Right-click on a Vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"
				+ "<li>Right-click on an Edge for <b>Delete Edge</b> popup"
				+ "<li>Mousewheel scales with a crossover value of 1.0.<br>"
				+ "     - scales the graph layout when the combined scale is greater than 1<br>"
				+ "     - scales the graph view when the combined scale is less than 1"
				+ "</ul>"
				+ "<h3>Transforming Mode:</h3>"
				+ "<ul>"
				+ "<li>Left+drag pans the graph"
				+ "<li>Left+Shift+drag rotates the graph"
				+ "<li>Left+Command+drag shears the graph"
				+ "<li>Left double-click on a vertex or edge allows you to edit the label"
				+ "</ul>"
				// + "<h3>Annotation Mode:</h3>"
				// + "<ul>"
				// + "<li>Mouse1 begins drawing of a Rectangle"
				// + "<li>Mouse1+drag defines the Rectangle shape"
				// + "<li>Mouse1 release adds the Rectangle as an annotation"
				// + "<li>Mouse1+Shift begins drawing of an Ellipse"
				// + "<li>Mouse1+Shift+drag defines the Ellipse shape"
				// + "<li>Mouse1+Shift release adds the Ellipse as an annotation"
				// + "<li>Mouse3 shows a popup to input text, which will become"
				// + "<li>a text annotation on the graph at the mouse location"
				// + "</ul>"
				+ "</html>";

		public HelpListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JOptionPane.showMessageDialog(CSTNEditor.this.vvEditor, instructions);
		}
	}

	/**
	 * @author posenato
	 */
	private class LayoutListener implements ActionListener {

		/**
		 * 
		 */
		@SuppressWarnings("unused")
		CSTNLayout cstnLayout;

		/**
		 * Original StaticLayout
		 */
		AbstractLayout<LabeledNode, ? extends Edge> originalLayout;

		/**
		 * 
		 */
		LayoutListener() {
			this.originalLayout = null;
			this.cstnLayout = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			try {
				AbstractLayout<LabeledNode, ? extends Edge> nextLayout;
				if (CSTNEditor.this.layoutToggleButton.isSelected()) {
					this.originalLayout = CSTNEditor.this.layoutEditor;
					// if (this.cstnLayout == null) {
					nextLayout = new CSTNLayout((TNGraph<CSTNEdge>) CSTNEditor.this.inputGraph, CSTNEditor.this.vvEditor.getSize());
					((CSTNLayout) nextLayout).setInitialY(CSTNEditor.this.vvEditor.getSize().height / 2);
					((CSTNLayout) nextLayout).setCurrentLayout(this.originalLayout);
					nextLayout.initialize();
					this.cstnLayout = (CSTNLayout) nextLayout;
					// } else {
					// nextLayout = this.cstnLayout;
					// }
				} else {
					nextLayout = this.originalLayout;
				}

				// IF one wants animation
				// Relaxer relaxer = new VisRunner((IterativeContext) this.currentLayout);
				// relaxer.stop();
				// relaxer.prerelax();
				// LayoutTransition<LabeledNode, Edge> lt = new LayoutTransition<>(CSTNEditor.this.vv1, CSTNEditor.this.layout1, nextLayout);
				// Animator animator = new Animator(lt);
				// animator.start();
				// ELSE one step transition
				((VisualizationViewer<LabeledNode, Edge>) CSTNEditor.this.vvEditor).setGraphLayout((Layout<LabeledNode, Edge>) nextLayout);
				CSTNEditor.this.vvEditor.repaint();
				// END iF
				CSTNEditor.this.layoutEditor = nextLayout;
			} catch (Exception e) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						CSTNEditor.LOG.warning("The instance cannot be layout with the workflow algorithm.");
					}
				}
			}
		}

		/**
		 * 
		 */
		public void reset() {
			CSTNEditor.this.layoutToggleButton.setSelected(false);
			this.originalLayout = null;
			this.cstnLayout = null;
			return;
		}
	}

	/**
	 * @author posenato
	 */
	private class OnlyToZListener implements ItemListener {

		public OnlyToZListener() {
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			CSTNEditor.this.onlyToZ = e.getStateChange() == ItemEvent.SELECTED;
		}
	}

	/**
	 * @author posenato
	 */
	private class OpenFileListener implements ActionListener {
		private JFileChooser chooser = null;

		public OpenFileListener() {
			this.chooser = new JFileChooser(CSTNEditor.default_dir);
			this.chooser.setDragEnabled(true);
			String msg = "The extension of the selected file determines the kind of network. Use *.stn, *.cstn, *.cstnu, *.cstpsu";
			this.chooser.setToolTipText(msg);
			this.chooser.setApproveButtonToolTipText(msg);

			FileFilter stnE = new FileNameExtensionFilter("(C)STN(U) file (.stn/.cstn/.cstnu)", "stn", "cstn", "cstnu", "cstnpsu");
			this.chooser.addChoosableFileFilter(stnE);
			this.chooser.setFileFilter(stnE);
			this.chooser.setAcceptAllFileFilterUsed(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final int option = this.chooser.showOpenDialog(CSTNEditor.this);
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = this.chooser.getSelectedFile();
				CSTNEditor.default_dir = file.getParent();
				try {
					CSTNEditor.this.loadGraphG(file);
					CSTNEditor.this.vvViewer.setVisible(false);
					((LayoutListener) CSTNEditor.this.layoutToggleButton.getActionListeners()[0]).reset();
					CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
					jl.setText("");
					jl.setOpaque(false);
					// CSTNEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + CSTNEditor.this.g.getName());
					CSTNEditor.this.graphInfoLabel.setText(CSTNEditor.getGraphLabelDescription(CSTNEditor.this.inputGraph));
					CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());

				} catch (IOException | ParserConfigurationException | SAXException e1) {
					CSTNEditor.this.inputGraph.clear();
					CSTNEditor.this.vvViewer.setVisible(false);
					CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
					String msg = "The graph has a problem in the definition:" + e1.getMessage();
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							CSTNEditor.LOG.warning(msg);
						}
					}
					jl.setText("<b>" + msg + "</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
				} finally {
					CSTNEditor.this.validate();
					CSTNEditor.this.repaint();
					CSTNEditor.this.cycle = 0;
				}
			}
		}
	}

	/**
	 * @author posenato
	 */
	private class SaveFileListener implements ActionListener {

		public SaveFileListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = new JFileChooser(CSTNEditor.default_dir);
			// CSTNEditor.LOG.finest("Path wanted:" + path);
			final int option = chooser.showSaveDialog(CSTNEditor.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				CSTNEditor.default_dir = file.getParent();
				CSTNEditor.this.saveGraphToFile(CSTNEditor.this.inputGraph, file);
			}
		}

	}

	/**
	 * Name of the derived graph big viewer
	 */
	public static final String DERIVED_GRAPH_BIG_VIEWER_NAME = "Derived Graph Big Viewer";

	/**
	 * Name of the distance viewer panel
	 */
	public static final String DISTANCE_VIEWER_NAME = "DistanceViewer";

	/**
	 * Name of the editor panel
	 */
	public static final String EDITOR_NAME = "Editor";

	/**
	 * Name of the input graph big viewer
	 */
	public static final String INPUT_GRAPH_BIG_VIEWER_NAME = "Input Graph Big Viewer";

	/**
	 * Default load/save directory
	 */
	static String default_dir = "/Dropbox/_CSTNU";

	/**
	 *
	 */
	static final URL INFO_ICON_FILE = Class.class.getResource("/images/metal-info.png");

	// EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS,

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger(CSTNEditor.class.getName());

	/**
	 * the preferred sizes for the two views
	 */
	static Dimension preferred_size = new Dimension(780, 768);

	/**
	 *
	 */
	static final URL WARN_ICON_FILE = Class.class.getResource("/images/metal-warning.png");

	/**
	 * Standard serial number
	 */
	@SuppressWarnings("unused")
	private static final long SERIAL_VERSION_UID = 647420826043015776L;
	/**
	 * Version
	 */
	private static final String VERSION = "Version  $Rev$";

	/**
	 * @param g
	 * @return a string describing essential characteristics of the graph.
	 */
	@SuppressWarnings("rawtypes")
	static String getGraphLabelDescription(TNGraph g) {
		if (g == null)
			return "";
		StringBuilder sb = new StringBuilder();
		if (g.getFileName() != null) {
			sb.append("File ");
			sb.append(g.getFileName().getName());
		}
		sb.append(": #nodes: ");
		sb.append(g.getVertexCount());
		sb.append(", #edges: ");
		sb.append(g.getEdgeCount());
		sb.append(", #obs: ");
		sb.append(g.getObserverCount());
		if (g.getContingentCount() > 0) {
			sb.append(", #contingent: ");
			sb.append(g.getContingentCount());
		}
		return sb.toString();
	}

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		@SuppressWarnings("unused")
		CSTNEditor editor = new CSTNEditor();
	}

	/**
	 * Sets up vertex and edges renders.
	 * 
	 * @param viewer
	 * @param firstViewer
	 */
	static <E extends Edge> void setNodeEdgeRenders(BasicVisualizationServer<LabeledNode, E> viewer, boolean firstViewer) {
		RenderContext<LabeledNode, E> renderCon = viewer.getRenderContext();
		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		renderCon.setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		// EDGE setting
		renderCon.setEdgeDrawPaintTransformer(
				EdgeRendering.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		renderCon.setEdgeLabelTransformer(EdgeRendering.edgeLabelFunction);
		renderCon.setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		renderCon.setEdgeStrokeTransformer(EdgeRendering.edgeStrokeTransformer);
		renderCon.setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<LabeledNode, E>(0.65, 0.5));
		renderCon.setArrowDrawPaintTransformer(
				EdgeRendering.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		renderCon.setArrowFillPaintTransformer(
				EdgeRendering.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		if (firstViewer) {
			renderCon.setEdgeFontTransformer(EdgeRendering.edgeFontFunction);
		}
		renderCon.setLabelOffset((firstViewer) ? 6 : 3);
	}

	/**
	 * The current wanted semantics
	 */
	public DCSemantics dcCurrentSem = DCSemantics.IR;

	/**
	 * TNGraph structures necessary to represent derived graph.
	 */
	final TNGraph<? extends Edge> checkedGraph;

	/**
	 * Cleaned result. True to store a cleaned result
	 */
	final boolean cleanResult = false;

	/**
	 * STN checker
	 */
	STN stn;

	/**
	 * STN check status
	 */
	STNCheckStatus stnStatus;

	/**
	 * CSTN checker
	 */
	CSTN cstn;

	/**
	 * CSTN check status
	 */
	CSTNCheckStatus cstnStatus;

	/**
	 * CSTNU checker
	 */
	CSTNU cstnu;

	/**
	 * CSTNU2CSTN checker
	 */
	CSTNU2CSTN cstnu2cstn;

	/**
	 * CSTNU check status
	 */
	CSTNUCheckStatus cstnuStatus;

	/**
	 * Number of cycles of CSTN(U) check step-by-step
	 */
	int cycle;

	/**
	 * Edges to check in CSTN(U) check step-by-step
	 */
	CSTN.EdgesToCheck<? extends Edge> edgesToCheck;

	/**
	 * The epsilon panel
	 */
	JPanel epsilonPanel;

	/**
	 * The graph info label
	 */
	JLabel graphInfoLabel;

	/**
	 * TNGraph structures necessary to represent input graph.
	 */
	final TNGraph<? extends Edge> inputGraph;

	/**
	 * Layout for input graph.
	 */
	AbstractLayout<LabeledNode, ? extends Edge> layoutEditor;

	/**
	 * Button for re-layout input graph
	 */
	JToggleButton layoutToggleButton;

	/**
	 * Layout for derived graph.
	 */
	AbstractLayout<LabeledNode, ? extends Edge> layoutViewer;

	/**
	 * 
	 */
	JLabel mapInfoLabel;

	/**
	 * TNGraph structures necessary to represent an axuliary graph.
	 */
	TNGraph<? extends Edge> oneStepBackGraph;

	/**
	 * OnlyToZ says if the DC checking has to be made propagating constraints only to time-point Z
	 */
	boolean onlyToZ = true;

	/**
	 * 
	 */
	JCheckBox onlyToZCB;

	/**
	 * True if contingent link as to be represented also as ordinary constraints.
	 */
	boolean contingentAlsoAsOrdinary = false;

	/**
	 * Reaction time for CSTN
	 */
	int reactionTime = 1;

	/**
	 * Which check alg to use for STN
	 */
	STN.CheckAlgorithm stnCheckAlg = STN.CheckAlgorithm.FloydWarshall;

	/**
	 * Result Save Button
	 */
	JButton saveCSTNResultButton;

	/**
	 * Message area above the derived (no input) graph.
	 */
	JEditorPane viewerMessageArea;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for input graph.
	 */
	VisualizationViewer<LabeledNode, ? extends Edge> vvEditor;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for derived graph.
	 */
	VisualizationViewer<LabeledNode, ? extends Edge> vvViewer;

	ValidationPanel validationPanelCSTN, validationPanelCSTNU;

	JPanel controlSouthPanel;

	JPanel rowForSTNButtons;

	/**
	 * with unknown literal
	 */
	boolean withUknown = true;

	/**
	 * The kind of network the system is currently showing
	 */
	TNGraph.NetworkType currentTNGraphType;

	/**
	 * Current edge implementation class
	 */
	Class<? extends Edge> currentEdgeImpl;

	/**
	 * Default constructor
	 */
	@SuppressWarnings("unchecked")
	public CSTNEditor() {
		super("Simple (C)STN(U) Editor. CSTN " + CSTN.VERSIONandDATE + ". CSTNU " + CSTNU.VERSIONandDATE);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Screen Bounds: " + bounds);
			}
		}
		CSTNEditor.preferred_size = new Dimension((bounds.width - 30) / 2, bounds.height - 260);

		// Using a null input TNGraph for setting all graphical aspects.
		// When the graph will be load, inputGraph will be updated copying all the graph inside it (takeIn method).
		this.inputGraph = new TNGraph<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
		this.checkedGraph = new TNGraph<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
		this.layoutEditor = new StaticLayout<>(this.inputGraph, CSTNEditor.preferred_size);
		this.layoutViewer = new StaticLayout<>(this.checkedGraph, CSTNEditor.preferred_size);
		this.vvEditor = new VisualizationViewer<>(this.layoutEditor, CSTNEditor.preferred_size);
		this.vvEditor.setName(CSTNEditor.EDITOR_NAME);
		this.vvViewer = new VisualizationViewer<>(this.layoutViewer, CSTNEditor.preferred_size);
		this.vvViewer.setName(CSTNEditor.DISTANCE_VIEWER_NAME);

		buildRenderContext(this.vvEditor, true);
		buildRenderContext(this.vvViewer, false);
		// CONTENT
		// content is the canvas of the application.
		final Container contentPane = this.getContentPane();

		// NORTH
		// I put a row for messages: since there will 2 graphs, the row contains two columns,
		// corresponding to the two graphs.
		JPanel messagePanel = new JPanel(new GridLayout(1, 2));
		this.graphInfoLabel = new JLabel("  ");
		this.graphInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.mapInfoLabel = new JLabel("  ");
		this.mapInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		// Info for first graph
		JPanel message1Graph = new JPanel(new GridLayout(2, 1));
		message1Graph.setBackground(new Color(253, 253, 253));
		message1Graph.add(this.graphInfoLabel);
		message1Graph.add(this.mapInfoLabel);
		messagePanel.add(message1Graph);

		message1Graph = new JPanel(new GridLayout(1, 1));// even if in the second cell there is only one element, derivedGraphMessageArea, a JPanel is necessary
															// to have the same padding of first cell.
		this.viewerMessageArea = new JEditorPane("text/html", "");
		this.viewerMessageArea.setBorder(new EmptyBorder(2, 2, 2, 2));
		this.viewerMessageArea.setEditable(false);
		this.viewerMessageArea.setVisible(true);
		message1Graph.add(this.viewerMessageArea);
		messagePanel.add(message1Graph);

		contentPane.add(messagePanel, BorderLayout.NORTH);

		// USING WEST AND EAST zone results that, after a resize, the central panel may be displayed and it is ugly.
		// LEFT contentPane.add(new GraphZoomScrollPane(this.vv1), BorderLayout.WEST);
		// RIGHT contentPane.add(new GraphZoomScrollPane(this.vv2), BorderLayout.EAST);
		// USIC CENTER is better even if it require a new layout
		JPanel centralPanel = new JPanel(new GridLayout(1, 2));
		centralPanel.add(new GraphZoomScrollPane(this.vvEditor));// GraphZoomScrollPane is necessary to show border!
		centralPanel.add(new GraphZoomScrollPane(this.vvViewer));
		contentPane.add(centralPanel, BorderLayout.CENTER);

		// SOUTH
		this.controlSouthPanel = new JPanel(new GridLayout(4, 1));// one row for AppButtons, one for validationPanelCSTN, one for validationPanelCSTNU
		JPanel rowForAppButtons = new JPanel();

		this.rowForSTNButtons = new JPanel();

		JPanel rowForCSTNButtons = new JPanel();
		this.validationPanelCSTN = new ValidationPanel();
		final ValidationGroup validationGroupCSTN = this.validationPanelCSTN.getValidationGroup();
		this.validationPanelCSTN.setInnerComponent(rowForCSTNButtons);
		this.validationPanelCSTN.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		JPanel rowForCSTNUButtons = new JPanel();// , rowForCSTNPSUButtons = new JPanel();
		this.validationPanelCSTNU = new ValidationPanel();
		@SuppressWarnings("unused")
		final ValidationGroup validationGroupCSTNU = this.validationPanelCSTN.getValidationGroup();
		this.validationPanelCSTNU.setInnerComponent(rowForCSTNUButtons);
		this.validationPanelCSTNU.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		this.controlSouthPanel.add(rowForAppButtons, 0);// for tuning application
		this.controlSouthPanel.add(this.rowForSTNButtons, 1);// for button regarding STN
		this.controlSouthPanel.add(this.validationPanelCSTN, 2);// for button regarding CSTN
		this.controlSouthPanel.add(this.validationPanelCSTNU, 3);// for button regarding CSTNU
		contentPane.add(this.controlSouthPanel, BorderLayout.SOUTH);

		JButton buttonCheck;

		// FIRST ROW OF COMMANDS
		// mode box for the editor
		JComboBox<Mode> modeBox = ((EditingModalGraphMouse<LabeledNode, Edge>) this.vvEditor.getGraphMouse()).getModeComboBox();
		rowForAppButtons.add(modeBox);

		this.layoutToggleButton = new JToggleButton("Layout input graph");
		this.layoutToggleButton.addActionListener(new LayoutListener());
		rowForAppButtons.add(this.layoutToggleButton);

		buttonCheck = new JButton("Input Graph big viewer");
		buttonCheck.addActionListener(new BigViewerListener(true));
		rowForAppButtons.add(buttonCheck);

		// AnnotationControls<LabeledNode,Edge> annotationControls =
		// new AnnotationControls<LabeledNode,Edge>(gm.getAnnotatingPlugin());
		// controls.add(annotationControls.getAnnotationsToolBar());

		// final JRadioButton excludeR1R2Button = new JRadioButton("R1 and R2 rule disabled", this.excludeR1R2);
		// excludeR1R2Button.addItemListener(new ItemListener() {
		// @Override
		// public void itemStateChanged(final ItemEvent ev) {
		// if (ev.getStateChange() == ItemEvent.SELECTED) {
		// CSTNEditor.this.excludeR1R2 = true;
		// CSTNEditor.LOG.fine("excludeR1R2 flag set to true");
		// } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
		// CSTNEditor.this.excludeR1R2 = false;
		// CSTNEditor.LOG.fine("excludeR1R2 flag set to false");
		// }
		// }
		// });
		// rowForAppButtons.add(excludeR1R2Button);

		buttonCheck = new JButton("Derived Graph big viewer");
		buttonCheck.addActionListener(new BigViewerListener(false));
		rowForAppButtons.add(buttonCheck);

		buttonCheck = new JButton("Help");
		buttonCheck.addActionListener(new HelpListener());
		rowForAppButtons.add(buttonCheck);

		// mode box for the distance viewer
		JComboBox<Mode> modeBoxViewer = ((EditingModalGraphMouse<LabeledNode, Edge>) this.vvViewer.getGraphMouse()).getModeComboBox();
		rowForAppButtons.add(modeBoxViewer);

		// SECOND ROW OF COMMANDS

		// ROW FOR STNs
		this.rowForSTNButtons.add(new JLabel("Check Alg: "));
		JComboBox<STN.CheckAlgorithm> cAlgCombo = new JComboBox<>(STN.CheckAlgorithm.values());
		cAlgCombo.setSelectedItem(this.stnCheckAlg);
		cAlgCombo.addActionListener(new STNCheckAlgListener(cAlgCombo));
		this.rowForSTNButtons.add(cAlgCombo);

		buttonCheck = new JButton("Init");
		buttonCheck.addActionListener(new STNInitListener());
		this.rowForSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("Consistency");
		buttonCheck.addActionListener(new STNConsistencyCheckListener());
		this.rowForSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("Dispatchable");
		buttonCheck.addActionListener(new STNDispatchableListener());
		this.rowForSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("PredecessorGraph");
		buttonCheck.addActionListener(new STNPredecessorGraphListener());
		this.rowForSTNButtons.add(buttonCheck);

		// ROW FOR CSTNs
		JCheckBox withUnkwon = new JCheckBox("With unknown literals");
		withUnkwon.setSelected(this.withUknown);
		withUnkwon.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				CSTNEditor.this.withUknown = e.getStateChange() == ItemEvent.SELECTED;
			}
		});
		rowForCSTNButtons.add(withUnkwon);

		rowForCSTNButtons.add(new JLabel("DC Semantics: "));
		JComboBox<DCSemantics> dcSemCombo = new JComboBox<>(DCSemantics.values());
		dcSemCombo.setSelectedItem(DCSemantics.Std);
		dcSemCombo.addActionListener(new DCSemanticsListener(dcSemCombo));
		rowForCSTNButtons.add(dcSemCombo);

		// epsilon panel
		CSTNEditor.this.epsilonPanel = new JPanel(new FlowLayout());

		CSTNEditor.this.epsilonPanel.add(new JLabel("System reacts "));
		final JFormattedTextField jreactionTime = new JFormattedTextField();
		jreactionTime.setValue(this.reactionTime);
		jreactionTime.setColumns(3);
		jreactionTime.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				CSTNEditor.this.reactionTime = ((Number) jreactionTime.getValue()).intValue();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						CSTNEditor.LOG.finest("Property: " + evt.getPropertyName());
					}
					if (LOG.isLoggable(Level.INFO)) {
						CSTNEditor.LOG.info("New reaction time: " + CSTNEditor.this.reactionTime);
					}
				}
			}
		});
		validationGroupCSTN.add(jreactionTime, StringValidators.regexp(Constants.NonNegIntValueRE, "A > 0 integer!", false));
		CSTNEditor.this.epsilonPanel.add(jreactionTime);
		CSTNEditor.this.epsilonPanel.add(new JLabel("time units after (≥) an observation."));
		CSTNEditor.this.epsilonPanel.setVisible(CSTNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
		rowForCSTNButtons.add(CSTNEditor.this.epsilonPanel);

		buttonCheck = new JButton("CSTN Init");
		buttonCheck.addActionListener(new CSTNInitListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("DC Check HP_18 (only with IR or ε)");
		buttonCheck.addActionListener(new CSTNRestrictedCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Check HP_19");
		buttonCheck.addActionListener(new CSTNCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Check HP_20");
		buttonCheck.addActionListener(new CSTNPotentialCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new CSTNOneStepListener());
		rowForCSTNButtons.add(buttonCheck);

		this.saveCSTNResultButton = new JButton("Save CSTN");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new CSTNSaveListener());
		rowForCSTNButtons.add(this.saveCSTNResultButton);

		buttonCheck = new JButton("CSTN All-Pair Shortest Paths");
		buttonCheck.addActionListener(new CSTNAPSPListener());
		rowForCSTNButtons.add(buttonCheck);

		// ROW FOR CSTNU
		this.onlyToZCB = new JCheckBox("Propagate only to Z");
		this.onlyToZCB.setSelected(this.onlyToZ);
		this.onlyToZCB.addItemListener(new OnlyToZListener());
		rowForCSTNUButtons.add(this.onlyToZCB);

		JCheckBox contingentAlsoAsOrdinaryCB = new JCheckBox("Propagate contingents also as std constraints");
		contingentAlsoAsOrdinaryCB.setSelected(this.contingentAlsoAsOrdinary);
		contingentAlsoAsOrdinaryCB.addItemListener(new ContingentAlsoAsOrdinaryListener());
		rowForCSTNUButtons.add(contingentAlsoAsOrdinaryCB);

		buttonCheck = new JButton("CSTNU Init");
		buttonCheck.addActionListener(new CSTNUInitListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new CSTNUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new CSTNUOneStepListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU2CSTN Check");
		buttonCheck.addActionListener(new CSTNU2CSTNCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNPSU Check");
		buttonCheck.addActionListener(new CSTNPSUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		// MENU
		CSTNEditor.default_dir = (new JFileChooser()).getCurrentDirectory() + CSTNEditor.default_dir;
		final JMenu menu = new JMenu("File");
		final JMenuItem openItem = new JMenuItem("Open...");
		openItem.addActionListener(new OpenFileListener());
		menu.add(openItem);
		final JMenuItem saveItem = new JMenuItem("Save...");
		saveItem.addActionListener(new SaveFileListener());
		menu.add(saveItem);

		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		this.pack();
		this.setVisible(true);
	}

	/**
	 * In the command panel, only one row of commands is visible.
	 * This method makes visible one row, hiding the others.
	 * 
	 * @param rowName Admissible value 'STN', 'CSTN', 'CSTNU'.
	 */
	void showCommandRow(String rowName) {
		switch (rowName) {
		case "CSTNU":
			this.validationPanelCSTNU.setVisible(true);
			this.validationPanelCSTN.setVisible(false);
			this.rowForSTNButtons.setVisible(false);
			break;
		case "CSTN":
			this.validationPanelCSTNU.setVisible(false);
			this.validationPanelCSTN.setVisible(true);
			this.rowForSTNButtons.setVisible(false);
			break;
		default:
			this.validationPanelCSTNU.setVisible(false);
			this.validationPanelCSTN.setVisible(false);
			this.rowForSTNButtons.setVisible(true);
		}
	}

	/**
	 * 
	 */
	void resetDerivedGraphStatus() {
		this.vvViewer.setVisible(false);
		this.viewerMessageArea.setText("");
		this.cycle = 0;
	}

	/**
	 * Adds vertex and edges renders, tooltips and mouse behavior to a viewer.
	 * 
	 * @param viewer
	 * @param firstViewer
	 */
	<E extends Edge> void buildRenderContext(VisualizationViewer<LabeledNode, E> viewer, boolean firstViewer) {
		LOG.finest("buildRenderContext: " + viewer + ", firstViewer:" + firstViewer);

		// vertex and edge renders
		setNodeEdgeRenders(viewer, firstViewer);

		// mouse action
		EditingModalGraphMouse<LabeledNode, E> graphMouse = new EditingModalGraphMouse<>(
				viewer.getRenderContext(),
				new LabeledNodeSupplier(),
				null, // only after graph load it is possible to set edge supplier.
				CSTNEditor.this,
				firstViewer);
		LOG.finest("buildRenderContext.graphMouse " + graphMouse);
//		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		viewer.setGraphMouse(graphMouse);
		viewer.addKeyListener(graphMouse.getModeKeyListener());

		// TOOLTIPS setting
		viewer.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);
	}

	/**
	 * Updates Edge Supplier in viewer considering the current type of loaded graph.
	 * 
	 * @param viewer
	 */
	@SuppressWarnings("unchecked")
	<E extends Edge> void updateEdgeSupplierInViewer(VisualizationViewer<LabeledNode, E> viewer) {
		// MOUSE setting
		// Create a mouse and add it to the visualization component
		// The following edgeSupp has to be update after graph load!
		EdgeSupplier<E> edgeSupp = null;

		if (this.currentTNGraphType == NetworkType.STN) {
			edgeSupp = (EdgeSupplier<E>) new EdgeSupplier<>(EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
		} else {
			if (this.currentTNGraphType == NetworkType.CSTN) {
				edgeSupp = (EdgeSupplier<E>) new EdgeSupplier<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			} else {
				edgeSupp = (EdgeSupplier<E>) new EdgeSupplier<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			}
		}
		//
		((EditingModalGraphMouse<LabeledNode, E>) viewer.getGraphMouse()).setEdgeEditingPlugin(edgeSupp);
	}

	/**
	 * Loads TNGraph stored in file 'fileName' into attribute this.g.<br>
	 * <b>Be careful!</b>
	 * The extension of the file name determines the kind of TNGraph.
	 * 
	 * <pre>
	 * .stn ===> STN 
	 * .cstn ===> CSTN
	 * .cstnu ===> CSTNU
	 * </pre>
	 * 
	 * It creates call {@link #buildRenderContext(VisualizationViewer, boolean)}
	 * 
	 * @param fileName
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "null" })
	void loadGraphG(final File fileName) throws IOException, ParserConfigurationException, SAXException {
		@SuppressWarnings("rawtypes")
		TNGraphMLReader graphReader = null;
		String name = fileName.getName();
		if (name.endsWith(".stn")) {
			this.currentTNGraphType = NetworkType.STN;
			this.currentEdgeImpl = EdgeSupplier.DEFAULT_STN_EDGE_CLASS;
			graphReader = new TNGraphMLReader<STNEdge>(fileName, (Class<? extends STNEdge>) this.currentEdgeImpl);
			showCommandRow("STN");
		} else {
			if (name.endsWith(".cstn")) {
				this.currentTNGraphType = NetworkType.CSTN;
				this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS;
				graphReader = new TNGraphMLReader<CSTNEdge>(fileName, (Class<? extends CSTNEdge>) this.currentEdgeImpl);
				showCommandRow("CSTN");
			} else {
				if (name.endsWith(".cstnu")) {
					this.currentTNGraphType = NetworkType.CSTNU;
					this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS;
					graphReader = new TNGraphMLReader<CSTNUEdge>(fileName, (Class<? extends CSTNUEdge>) this.currentEdgeImpl);
					showCommandRow("CSTNU");
				} else {
					if (name.endsWith(".cstnpsu")) {
						this.currentTNGraphType = NetworkType.CSTNPSU;
						this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS;
						graphReader = new TNGraphMLReader<CSTNUEdge>(fileName, (Class<? extends CSTNUEdge>) this.currentEdgeImpl);
						showCommandRow("CSTNU");
					}
				}
			}
		}
		CSTNEditor.this.inputGraph.takeIn(graphReader.readGraph());
		CSTNEditor.this.inputGraph.setInputFile(fileName);
		CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
		// LOG.severe(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
		updateEdgeSupplierInViewer(this.vvEditor);
		updateEdgeSupplierInViewer(this.vvViewer);

		CSTNEditor.this.validate();
		CSTNEditor.this.repaint();
	}

	/**
	 * @param graphToSave graph to save
	 * @param file
	 */
	void saveGraphToFile(final TNGraph<? extends Edge> graphToSave, final File file) {
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(this.layoutEditor);
		graphToSave.setName(file.getName());
		try (Writer out = new BufferedWriter(new FileWriter(file))) {
			graphWriter.save(graphToSave, out);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update node positions in derived graph.
	 */
	void updateNodePositions() {
		LabeledNode gV;
		for (final LabeledNode v : this.checkedGraph.getVertices()) {
			gV = this.inputGraph.getNode(v.getName());
			if (gV != null) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV +
								"\nOriginal position (" + CSTNEditor.this.layoutEditor.getX(gV) + ";" + CSTNEditor.this.layoutEditor.getY(gV) + ")");
					}
				}
				CSTNEditor.this.layoutViewer.setLocation(v, CSTNEditor.this.layoutEditor.getX(gV), CSTNEditor.this.layoutEditor.getY(gV));
			} else {
				CSTNEditor.this.layoutViewer.setLocation(v, v.getX(), v.getY());
			}
		}
	}

}// end_of_file

//
// buttonCheck = new JButton("Translation to UPPAAL TIGA");
// buttonCheck.addActionListener(new AbstractAction("UPPAAL Tiga Tranlsation") {
// /**
// *
// */
// private static final long serialVersionUID = 1L;
//
// @Override
// public void actionPerformed(final ActionEvent e) {
// final JTextArea jl = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
// PrintStream output;
// final JFileChooser chooser = new JFileChooser(CSTNEditor.defaultDir);
// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
// final int option = chooser.showSaveDialog(CSTNEditor.this);
// if (option == JFileChooser.APPROVE_OPTION) {
// File file = chooser.getSelectedFile();
// CSTNEditor.defaultDir = file.getParent();
// if (!file.getName().endsWith("xml")) {
// file = new File(file.getAbsolutePath() + ".xml");
// }
// try {
// output = new PrintStream(file);
// } catch (final FileNotFoundException e1) {
// e1.printStackTrace();
// output = System.out;
// }
// CSTNU2UppaalTiga translator = null;
//
// jl.setOpaque(true);
// jl.setBackground(Color.orange);
//
// try {
// translator = new CSTNU2UppaalTiga(CSTNEditor.this.g, output);
// if (!translator.translate())
// throw new IllegalArgumentException();
// } catch (final IllegalArgumentException e1) {
// String msg = "The graph has a problem and it cannot be translated to an UPPAAL Tiga automaton:" + e1.getMessage();
// CSTNEditor.LOG.warning(msg);
// jl.setText(msg);
// // jl.setIcon(CSTNUEditor.warnIcon);
// jl.setOpaque(true);
// jl.setBackground(Color.orange);
// CSTNEditor.this.graphPanel.validate();
// CSTNEditor.this.graphPanel.repaint();
// return;
// } finally {
// output.close();
// }
// jl.setText("The graph has been translated and saved into file '" + file.getName() + "'.");
// try {
// output = new PrintStream(file.getAbsolutePath().replace(".xml", ".q"));
// } catch (final FileNotFoundException e1) {
// e1.printStackTrace();
// output = System.out;
// }
// output.println("control: A[] not _processMain.goal");
//
// // jl.setIcon(CSTNUEditor.infoIcon);
// CSTNEditor.this.graphPanel.validate();
// CSTNEditor.this.graphPanel.repaint();
// output.close();
// }
// }
// });
// rowForCSTNUButtons.add(buttonCheck);
