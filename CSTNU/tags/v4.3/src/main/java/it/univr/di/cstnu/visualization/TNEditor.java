// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.visualization;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
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
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalMenuBarUI;
import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
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
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.AbstractCSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.AbstractCSTN.CheckAlgorithm;
import it.univr.di.cstnu.algorithms.AbstractCSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.AbstractCSTN.EdgesToCheck;
import it.univr.di.cstnu.algorithms.AbstractCSTN;
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
import it.univr.di.cstnu.algorithms.STNU;
import it.univr.di.cstnu.algorithms.STNU.STNUCheckStatus;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.CSTNPSUEdge;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.LabeledNodeSupplier;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNUEdge;
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
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SE_BAD_FIELD_STORE", justification = "I know what I'm doing")
public class TNEditor extends JFrame {

	/**
	 * Open a bigger viewer for showing the input or derived network.
	 * 
	 * @author posenato
	 */
	private class BigViewerListener implements ActionListener {

		boolean isInputGraphLayoutToShow;
		JDialog frame;
		AbstractLayout<LabeledNode, ? extends Edge> layout;
		VisualizationViewer<LabeledNode, ? extends Edge> bvv;

		public BigViewerListener(boolean isInputGraphLayoutToShow1) {
			this.isInputGraphLayoutToShow = isInputGraphLayoutToShow1;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {

			this.frame = new JDialog(TNEditor.this,
					(this.isInputGraphLayoutToShow) ? TNEditor.INPUT_GRAPH_BIG_VIEWER_NAME : TNEditor.DERIVED_GRAPH_BIG_VIEWER_NAME);
			this.frame.setBounds(TNEditor.this.getBounds());
			Dimension bvvDim = new Dimension(this.frame.getWidth(), this.frame.getHeight() - 100);

			this.layout = (this.isInputGraphLayoutToShow) ? TNEditor.this.layoutEditor : TNEditor.this.layoutViewer;
			this.bvv = new VisualizationViewer<>(this.layout);
			this.bvv.setPreferredSize(bvvDim);
			this.bvv.setName((this.isInputGraphLayoutToShow) ? TNEditor.INPUT_GRAPH_BIG_VIEWER_NAME : TNEditor.DERIVED_GRAPH_BIG_VIEWER_NAME);

			// vertex and edge renders
			setNodeEdgeRenders(this.bvv, this.isInputGraphLayoutToShow);

			// mouse action
			@SuppressWarnings("unchecked")
			EditingModalGraphMouse<LabeledNode, Edge> graphMouse = new EditingModalGraphMouse<>(
					(RenderContext<LabeledNode, Edge>) this.bvv.getRenderContext(),
					new LabeledNodeSupplier(),
					(EdgeSupplier<Edge>) new EdgeSupplier<>(TNEditor.this.currentEdgeImpl), // only after graph load it is possible to set edge supplier.
					TNEditor.this,
					this.isInputGraphLayoutToShow);
			// graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
			this.bvv.setGraphMouse(graphMouse);
			this.bvv.addKeyListener(graphMouse.getModeKeyListener());
			((ModalGraphMouse) this.bvv.getGraphMouse()).setMode(ModalGraphMouse.Mode.EDITING);
			final JPanel rowForAppButtons1 = new JPanel();
			@SuppressWarnings("unchecked")
			final JComboBox<Mode> modeBox1 = ((EditingModalGraphMouse<LabeledNode, Edge>) this.bvv.getGraphMouse()).getModeComboBox();
			rowForAppButtons1.add(modeBox1);
			JButton close = new JButton(new AbstractAction("Close") {
				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed(ActionEvent event) {
					BigViewerListener.this.frame.dispose();
				}
			});
			rowForAppButtons1.add(close);
			this.frame.setLayout(new FlowLayout(FlowLayout.CENTER));
			this.frame.add(new JLabel(getGraphLabelDescription(((TNGraph<?>) TNEditor.this.layoutEditor.getGraph()))));
			this.frame.add(this.bvv);
			this.frame.add(rowForAppButtons1);

			this.frame.setVisible(true);
			this.frame.validate();
		}
	}

	/**
	 * @author posenato
	 */
	private class CheckAlgListener<T> implements ActionListener {
		JComboBox<T> comboBox;

		public CheckAlgListener(JComboBox<T> comboBox1) {
			this.comboBox = comboBox1;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object selected = this.comboBox.getSelectedItem();
			// JOptionPane.showMessageDialog(TNEditor.this.vvEditor, "selezionato "+selected);

			if (selected instanceof STN.CheckAlgorithm) {
				TNEditor.this.stnCheckAlg = (STN.CheckAlgorithm) this.comboBox.getSelectedItem();
				return;
			}
			if (selected instanceof STNU.CheckAlgorithm) {
				TNEditor.this.stnuCheckAlg = (STNU.CheckAlgorithm) this.comboBox.getSelectedItem();
				return;
			}
			if (selected instanceof CSTN.CheckAlgorithm) {
				TNEditor.this.cstnCheckAlg = (CSTN.CheckAlgorithm) this.comboBox.getSelectedItem();
				if (TNEditor.this.cstnCheckAlg == CheckAlgorithm.HunsbergerPosenato18) {
					if (TNEditor.this.cstnDCSemmanticsComboBox.getSelectedItem()==DCSemantics.Std) {
						TNEditor.this.cstnDCSemmanticsComboBox.setSelectedItem(DCSemantics.IR);
					}
				}
				if (TNEditor.this.cstnCheckAlg == CheckAlgorithm.HunsbergerPosenato20) {
					TNEditor.this.cstnDCSemmanticsComboBox.setSelectedItem(DCSemantics.IR);
				}
				TNEditor.this.cstnDCSemmanticsComboBox.validate();
				TNEditor.this.cstnDCSemmanticsComboBox.repaint();
				return;
			}
		}
	}

	// /**
	// * @author posenato
	// */
	// private class CSTNAPSPListener implements ActionListener {
	//
	// public CSTNAPSPListener() {
	// }
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public void actionPerformed(final ActionEvent e) {
	// final JEditorPane jl = TNEditor.this.viewerMessageArea;
	// TNEditor.this.saveCSTNResultButton.setEnabled(false);
	// TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.inputGraph,
	// (Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
	// ((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph).takeIn(g1);
	// TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
	//
	// jl.setBackground(Color.orange);
	// boolean consistent = false;
	// TNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
	// consistent = AbstractCSTN.getMinimalDistanceGraph(TNEditor.this.cstn.getG());
	// if (consistent) {
	//
	// jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is All-Pair Shortest Paths.");
	// jl.setBackground(Color.green);
	// if (Debug.ON) {
	// if (LOG.isLoggable(Level.FINER)) {
	// TNEditor.LOG.finer("All-Pair Shortest Paths graph: " + TNEditor.this.cstn.getGChecked());
	// }
	// }
	// } else {
	// // The distance graph is not consistent
	// jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
	// // jl.setIcon(TNEditor.warnIcon);
	// }
	// jl.setOpaque(true);
	// updateNodePositions();
	// TNEditor.this.vvViewer.setVisible(true);
	// TNEditor.this.saveCSTNResultButton.setEnabled(false);
	//
	// TNEditor.this.vvViewer.validate();
	// TNEditor.this.vvViewer.repaint();
	//
	// TNEditor.this.validate();
	// TNEditor.this.repaint();
	// TNEditor.this.cycle = 0;
	// }
	// }

	/**
	 * @author posenato
	 */
	private class ContingentAlsoAsOrdinaryListener implements ItemListener {

		public ContingentAlsoAsOrdinaryListener() {
		}

		@Override
		public void itemStateChanged(ItemEvent e) {
			TNEditor.this.contingentAlsoAsOrdinary = e.getStateChange() == ItemEvent.SELECTED;
		}
	}

	/**
	 * @author posenato
	 */
	private class CSTNControllabilityCheckListener implements ActionListener {

		public CSTNControllabilityCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = TNEditor.this.viewerMessageArea;

			TNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNEditor.this.vvViewer.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());

			switch (TNEditor.this.cstnCheckAlg) {
			case HunsbergerPosenato18:
				switch (TNEditor.this.dcCurrentSem) {
				case ε:
					TNEditor.this.cstn = new CSTNEpsilon3R(TNEditor.this.reactionTime, (TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case IR:
					TNEditor.this.cstn = new CSTNIR3R((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case Std:
				default:
					jl.setBackground(Color.orange);
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE
							+ "'>&nbsp;<b>There is no DC checking algorithm for STD semantics and rules restricted to Z.</b>");
					jl.setOpaque(true);
					TNEditor.this.cycle = 0;
					return;
				}
				break;
			case HunsbergerPosenato19:
				switch (TNEditor.this.dcCurrentSem) {
				case ε:
					TNEditor.this.cstn = new CSTNEpsilon(TNEditor.this.reactionTime, (TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case IR:
					TNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case Std:
				default:
					TNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				}
				break;

			default:
			case HunsbergerPosenato20:
				TNEditor.this.cstn = new CSTNPotential((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
				TNEditor.this.cstnDCSemmanticsComboBox.setSelectedItem(DCSemantics.IR);
				break;
			}

			TNEditor.this.cstn.setOutputCleaned(TNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			try {
				TNEditor.this.cstnStatus = TNEditor.this.cstn.dynamicConsistencyCheck();
				if (TNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							TNEditor.LOG.finer("Final controllable graph: " + TNEditor.this.cstn.getGChecked());
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
					// jl.setIcon(TNEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(TNEditor.warnIcon);
			}

			TNEditor.this.cycle = 0;
			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			switch (TNEditor.this.dcCurrentSem) {
			case ε:
				TNEditor.this.cstn = new CSTNEpsilon(TNEditor.this.reactionTime, (TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
				break;
			case IR:
				TNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
				break;
			case Std:
			default:
				TNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
				break;
			}
			try {
				TNEditor.this.cstn.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(TNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// TNEditor.this.vv2.validate();
				// TNEditor.this.vv2.repaint();
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
			jl.setText("CSTN initialized.");
			// jl.setIcon(TNEditor.infoIcon);

			jl.setBackground(Color.orange);
			TNEditor.this.cycle = 0;
			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;

			TNEditor.this.checkingAlgCSTNComboBox.setSelectedItem(AbstractCSTN.CheckAlgorithm.HunsbergerPosenato19);
			if (TNEditor.this.cycle == -1)
				return;
			if (TNEditor.this.cycle == 0) {
				TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.inputGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

				TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
				switch (TNEditor.this.dcCurrentSem) {
				case ε:
					TNEditor.this.cstn = new CSTNEpsilon(TNEditor.this.reactionTime, (TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case IR:
					TNEditor.this.cstn = new CSTNIR((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				case Std:
				default:
					TNEditor.this.cstn = new CSTN((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph);
					break;
				}
				TNEditor.this.cstn.setOutputCleaned(TNEditor.this.cleanResult);

				try {
					TNEditor.this.cstn.initAndCheck();
				} catch (final Exception ex) {
					jl.setText("There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(TNEditor.warnIcon);
					TNEditor.this.cycle = -1;
					return;
				}
				TNEditor.this.oneStepBackGraph = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				TNEditor.this.cstnStatus = new CSTNCheckStatus();
			} else {
				TNGraph<CSTNEdge> g1 = new TNGraph<>((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph,
						(Class<? extends CSTNEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNEdge>) TNEditor.this.oneStepBackGraph).takeIn(g1);
				// TNEditor.this.cstn.setWithUnknown(TNEditor.this.withUknown);

			}
			TNEditor.this.cycle++;

			jl.setBackground(Color.orange);
			TNEditor.this.cstnStatus = TNEditor.this.cstn.oneStepDynamicConsistencyByNode();
			TNEditor.this.cstnStatus.finished = ((TNGraph<CSTNEdge>) TNEditor.this.checkedGraph)
					.hasSameEdgesOf((TNGraph<CSTNEdge>) TNEditor.this.oneStepBackGraph);
			final boolean reductionsApplied = !TNEditor.this.cstnStatus.finished;
			final boolean inconsistency = !TNEditor.this.cstnStatus.consistency;
			if (inconsistency) {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is inconsistent.<b>");
				// jl.setIcon(TNEditor.warnIcon);
				TNEditor.this.cycle = -1;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINE)) {
						TNEditor.LOG.fine("INCONSISTENT GRAPH: " + TNEditor.this.oneStepBackGraph);
					}
					if (LOG.isLoggable(Level.INFO)) {
						TNEditor.LOG.info("Status stats: " + TNEditor.this.cstnStatus);
					}
				}
			} else if (reductionsApplied) {
				jl.setText("Step " + TNEditor.this.cycle + " of consistency check is done.");
				// jl.setIcon(TNEditor.warnIcon);
			} else {
				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is CSTN consistent. The number of executed cycles is "
						+ TNEditor.this.cycle);
				// jl.setIcon(TNEditor.infoIcon);
				TNEditor.this.cycle = -1;
				jl.setBackground(Color.green);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.INFO)) {
						TNEditor.LOG.info("Status stats: " + TNEditor.this.cstnStatus);
					}
				}
			}

			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl1 = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNPSUEdge> g1 = new TNGraph<>((TNGraph<CSTNPSUEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNPSUEdge>) EdgeSupplier.DEFAULT_CSTNPSU_EDGE_CLASS);
			((TNGraph<CSTNPSUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			// TNEditor.this.onlyToZCB.setSelected(false);
			TNEditor.this.cstnpsu = new CSTNPSU((TNGraph<CSTNPSUEdge>) TNEditor.this.checkedGraph, 30 * 60);
			jl1.setBackground(Color.orange);
			try {
				TNEditor.this.cstnuStatus = TNEditor.this.cstnpsu.dynamicControllabilityCheck();
				if (TNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNPSU/FTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							TNEditor.LOG.finer("Final controllable graph: " + TNEditor.this.cstnpsu.getGChecked());
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The CSTNPSU/FTNU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNUEditor.warnIcon);
			}

			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
		}
	}

	/**
	 * @author posenato
	 */
	private class TNSaveListener implements ActionListener {

		public TNSaveListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JFileChooser chooser = new JFileChooser(TNEditor.this.defaultDir);
			final int option = chooser.showSaveDialog(TNEditor.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				if (TNEditor.this.cstn != null) {
					TNEditor.this.cstn.setfOutput(file);
					TNEditor.this.cstn.saveGraphToFile();
				}
				// saveGraphToFile(TNEditor.this.checkedGraph, file);
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
			final JEditorPane jl1 = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.cstnu2cstn = new CSTNU2CSTN((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph, 30 * 60);

			jl1.setBackground(Color.orange);
			try {
				TNEditor.this.cstnuStatus = TNEditor.this.cstnu2cstn.dynamicControllabilityCheck();
				if (TNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							TNEditor.LOG.finer("Final controllable graph: " + TNEditor.this.cstnu2cstn.getGChecked());
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

			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl1 = TNEditor.this.viewerMessageArea;
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph, 30 * 60, TNEditor.this.onlyToZ);
			TNEditor.this.cstnu.setContingentAlsoAsOrdinary(TNEditor.this.contingentAlsoAsOrdinary);
			try {
				TNEditor.this.cstnu.initAndCheck();
			} catch (final IllegalArgumentException | WellDefinitionException ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning(msg);
					}
				}
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNUEditor.warnIcon);
				jl1.setOpaque(true);
				jl1.setBackground(Color.orange);
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
			jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;TNGraph with Lower and Upper Case Labels.");
			// jl.setIcon(CSTNUEditor.infoIcon);
			jl1.setBackground(Color.orange);
			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl1 = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);
			TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) TNEditor.this.inputGraph,
					(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
			((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph, 30 * 60, TNEditor.this.onlyToZ);
			TNEditor.this.cstnu.setContingentAlsoAsOrdinary(TNEditor.this.contingentAlsoAsOrdinary);
			jl1.setBackground(Color.orange);
			try {
				TNEditor.this.cstnuStatus = TNEditor.this.cstnu.dynamicControllabilityCheck();
				if (TNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The CSTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							TNEditor.LOG.finer("Final controllable graph: " + TNEditor.this.cstnu.getGChecked());
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

			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl1 = TNEditor.this.viewerMessageArea;
			if (TNEditor.this.cycle == -1)
				return;
			if (TNEditor.this.cycle == 0) {
				TNGraph<CSTNUEdge> g1 = new TNGraph<>((TNGraph<CSTNUEdge>) TNEditor.this.inputGraph,
						(Class<? extends CSTNUEdge>) EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
				((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

				TNEditor.this.cstnu = new CSTNU((TNGraph<CSTNUEdge>) TNEditor.this.checkedGraph, 30 * 60, TNEditor.this.onlyToZ);
				TNEditor.this.cstnu.setContingentAlsoAsOrdinary(TNEditor.this.contingentAlsoAsOrdinary);
				TNEditor.this.mapInfoLabel.setText(TNEditor.this.checkedGraph.getEdgeFactory().toString());
				try {
					TNEditor.this.cstnu.initAndCheck();
				} catch (final Exception ex) {
					jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(TNEditor.warnIcon);
					TNEditor.this.cycle = -1;
					return;
				}
				TNEditor.this.cstnuStatus = new CSTNUCheckStatus();
				TNEditor.this.edgesToCheck = new it.univr.di.cstnu.algorithms.AbstractCSTN.EdgesToCheck<>(TNEditor.this.checkedGraph.getEdges());
			}
			TNEditor.this.cycle++;

			jl1.setBackground(Color.orange);
			Instant timeOut = Instant.now().plusSeconds(2700);
			TNEditor.this.cstnuStatus = (TNEditor.this.onlyToZ)
					? TNEditor.this.cstnu.oneStepDynamicControllabilityLimitedToZ((EdgesToCheck<CSTNUEdge>) TNEditor.this.edgesToCheck, timeOut)
					: TNEditor.this.cstnu.oneStepDynamicControllability((EdgesToCheck<CSTNUEdge>) TNEditor.this.edgesToCheck, timeOut);
			TNEditor.this.cstnuStatus.finished = TNEditor.this.edgesToCheck.size() == 0;
			final boolean reductionsApplied = !TNEditor.this.cstnuStatus.finished;
			final boolean notControllable = !TNEditor.this.cstnuStatus.consistency;
			if (notControllable) {
				jl1.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is inconsistent.</b>");
				// jl.setIcon(TNEditor.warnIcon);
				TNEditor.this.cycle = -1;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINE)) {
						TNEditor.LOG.fine("INCONSISTENT GRAPH: " + TNEditor.this.checkedGraph);
					}
					if (LOG.isLoggable(Level.INFO)) {
						TNEditor.LOG.info("Status stats: " + TNEditor.this.cstnuStatus);
					}
				}
			} else if (reductionsApplied) {
				jl1.setText("Step " + TNEditor.this.cycle + " of consistency check is done.");
				// jl.setIcon(TNEditor.warnIcon);
			} else {
				jl1.setText("<b>The graph is CSTNU consistent. The number of executed cycles is " + TNEditor.this.cycle);
				// jl.setIcon(TNEditor.infoIcon);
				TNEditor.this.cycle = -1;
				jl1.setBackground(Color.green);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.INFO)) {
						TNEditor.LOG.info("Status stats: " + TNEditor.this.cstnuStatus);
					}
				}
			}

			TNEditor.this.saveCSTNResultButton.setEnabled(true);
			TNEditor.this.updatevvViewer();
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
			TNEditor.this.dcCurrentSem = (DCSemantics) this.comboBox.getSelectedItem();
			TNEditor.this.epsilonPanel.setVisible(TNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
		}
	}

	/**
	 * @author posenato
	 */
	private static class HelpListener implements ActionListener {
		static enum AvailableHelp {
			CSTNHelp, CSTNUHelp, GenericHelp, STNHelp, STNUHelp,
		}

		private static final String cstnHelp = "<html>"
				+ "<h2>Conditional Simple Temporal Network</h2>"
				+ "<h4>Checking algorithm</h4>"
				+ "<p>Select which algorithm will be used for checking the consistency.<br>Hunsberger-Posenato 20 works only with instantaneous reaction semantics,<br>"
				+ "Hunsberger-Posenato 19 works with any kind of semantics,<br>while Hunsberger-Posenato 18 works only with instantaneous or ε semantics.</p>"
				+ "<h4>Reaction time</h4>"
				+ "<p>In case that \u03B5 semantics is selected, the system asks to set the minimum delay by which the system can react to an observation. Only integer values are admitted.</p>"
				+ "<h4>Init</h4>"
				+ "<p>Checks that node <b>Z</b> is present and adds all necessary constraints to make it as the first node to execute.<br>"
				+ "Moreover, it checks that all labeled values satisfy the well-definedness rules.</p>"
				+ "<h4>Controllability</h4>"
				+ "<p>Executes the controllability check using the selected algorithm in the drop-down list.<br>"
				+ "The resulting graph is presented on the right window.</p>"
				+ "<h4>One Step CSTN Check</h4>"
				+ "<p>Executes only one pass of HunsbergerPosenato19 algorithm at each buttom press. Useful for viewing the execution of the algorihm step-by-step.</p>"
				+ "<h4>Saved Checked CSTN</h4>"
				+ "<p>Saves the network obtained by a check.</p>"
				+ "</html>";

		private static final String cstnuHelp = "<html>"
				+ "<h2>Condition Simple Temporal Network with Uncertainty</h2>"
				+ "<h4>Propagate only to Z</h4>"
				+ "<p>If checked, the DC checking algorithm checks the network propagating only constraints ending to Z.<br>"
				+ "For a DC network it saves a lot of time, for not DC network, it slows the checking phase.</p>"
				+ "<h4>Propagate consingents also as std constraints</h4>"
				+ "The DC checking algorithm does not require to propagate the contingent constraints as ordinary constraints. The resulting network can not contain proper ranges for all timepoints.<br>"
				+ "Propagating contingent constraints as ordinary constraints allows the determination of more proper ranges for all time-points at a cost of a small increment of computation time."
				+ "<h4>CSTNU Init</h4>"
				+ "<p>Checks that node <b>Z</b> is present and adds all necessary constraints to make it as the first node to execute.<br>"
				+ "Moreover, it checks that all lableled values satisfy the well-definedness rules and that all contingent links are in the right form.</p>"
				+ "<h4>CSTNU Check</h4>"
				+ "<p>Executes the controllability check.<br>"
				+ "The resulting graph is presented on the right window.</p>"
				+ "<h4>One Step CSTNU Check</h4>"
				+ "<p>Executes only one pass of DC checking algorithm at each buttom press. Useful for viewing the execution of the algorihm step-by-step.</p>"
				+ "<h4>CSTNU2CSTN Check</h4>"
				+ "<p>Checks the instance transforming it to an equivalent CSTN instance and using the CSTN DC checking algorithm.</p>"
				+ "<h4>CSTNPSU/FTNU Check</h4>"
				+ "<p>Executes the controllability check for CSTPSU/FTNU instances. Such instances are an extension of CSTNU ones where each contingent link can have an extended range.<br>"
				+ "The resulting graph is presented on the right window.</p>"
				+ "</html>";

		private String genericHelp;
		private static final String stnHelp = "<html>"
				+ "<h2>Simple Temporal Network</h2>"
				+ "<h4>Checking algorithm</h4>"
				+ "<p>Select which algorithm will be used for checking the consistency.<br>"
				+ "Single-source-shortest-paths algoirithms like Bellman-Ford consider the node <b>Z</b> as source node."
				+ "<h4>Init</h4>"
				+ "<p>Checks that node <b>Z</b> is present and adds all necessary constraints to make it as the first node to execute.</p>"
				+ "<h4>Consistency</h4>"
				+ "<p>Executes the consistency check using the selected algorithm in the drop-down list.<br>"
				+ "The resulting graph is presented on the right window.</p>"
				+ "<h4>Dispatchable</h4>"
				+ "<p>Transforms a <b>consistent</b> network in a dispatchable form using the Muscettola et al. 1998 algorithm.</p>"
				+ "<h4>PredecessorSubGraph</h4>"
				+ "<p>Determine a predecessor graph of a given node in a <b>consistent</b> network using Muscettola algorithm. The predecessor graph consists in all shortest paths from the given node to all other nodes.</p>"
				+ "</html>";

		private static final String stnuHelp = "<html>"
				+ "<h2>Simple Temporal Network with Uncertainty</h2>"
				+ "<h4>Checking algorithm</h4>"
				+ "<p>Select which algorithm will be used for checking the dynamic controllability of the network. "
				+ "<h4>Init</h4>"
				+ "<p>Checks that node <b>Z</b> is present and adds all necessary constraints to make it as the first node to execute.<br>"
				+ "Moreover, it verifies that all contingent links are in the right format.</p>"
				+ "<h4>Controllability</h4>"
				+ "<p>Executes the dynamic controllability check using the selected algorithm in the drop-down list.<br>"
				+ "The resulting graph is presented on the right window.</p>"
				+ "</html>";

		TNEditor editor;

		public HelpListener(TNEditor ed) {
			this.editor = ed;

			// Generic help depends on this.editor.extraButtons;
			this.genericHelp = "<html>"
					+ "<h2>TNEditor Help</h2>"
					+ "<p>It is possibile to create (File->New), load (File->Open), and save the following kind of temporal networks: STN, CSTN, CSTNU, CSTPSU/FTNU.</p>"
					+ "<p>The application contains two main windows. The left window is the <i>editor window</i> where it is possible to build/edit a network.<br>"
					+ "The right window is the <i>view window</i> where the result of an operation (like consistency check) is shown. In the view window it is not possible to edit the shown graph.</p>"
					+ "<p>Once a network is created, the tool bar is exended to offer all possible operations on the network.<br>"
					+ "A network can be modified or inspected when the window is in <i>EDITING</i> mode.<br>"
					+ "The <i>TRANSFORMING</i> mode is for moving and zooming the network.</p>"
					+ ((this.editor.extraButtons)
							? "<p>Button 'Layout input graph' redraws the network in the editor. It works only when the network represents a business schema transformation with a proper grammar.<br>"
							: "")
					+ "Button 'Input/Derived Graph big viewer' opens a wider window for showing the input/derived graph.</p>"
					+ "<h3>Editing Mode:</h3>"
					+ "<ul>"
					+ "<li>Right-click on an empty area for <b>Add node</b> menu."
					+ "<li>Right-click on a node for <b>Delete node</b> popup."
					+ "<li>Right-click on a node for <b>Add edge</b> menu (if there are selected nodes)."
					+ "<li>Right-click on an edge for <b>Delete edge</b> popup"
					+ "<li>Left-click+Shift on a node adds/removes node selection."
					+ "<li>Left-click an empty area unselects all nodes."
					+ "<li>Left+drag on a node moves all selected nodes."
					+ "<li>Left+drag elsewhere selects nodes in a region."
					+ "<li>Left+Shift+drag adds selection of nodes in a new region."
					+ "<li>Left-click+drag on a selected node to another node, add an edge from the first node to the second."
					+ "<li>Left+Ctrl on a node selects the node and centers the display on it."
					+ "<li>Left double-click on a node or edge allows you to edit it."
					+ "<li>Mousewheel scales with a crossover value of 1.0.<br>"
					+ "     - scales the network layout when the combined scale is greater than 1<br>"
					+ "     - scales the network view when the combined scale is less than 1"
					+ "</ul>"
					+ "<h3>Transforming Mode:</h3>"
					+ "<ul>"
					+ "<li>Left+drag for moving the network."
					+ "<li>Left+Shift+drag for rotating the network."
					+ "<li>Left+Command+drag shears the network."
					+ "<li>Left double-click on a vertex or edge allows you to edit the label."
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
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			String message = this.genericHelp;
			switch (e.getActionCommand()) {
			case "STNHelp":
				message = stnHelp;
				break;
			case "STNUHelp":
				message = stnuHelp;
				break;
			case "CSTNHelp":
				message = cstnHelp;
				break;
			case "CSTNUHelp":
				message = cstnuHelp;
				break;
			case "GenericHelp":
			default:
				break;
			}
			JOptionPane.showMessageDialog(this.editor.vvEditor, message);
		}
	}

	/**
	 * @author posenato
	 */
	private class LayoutListener implements ActionListener {

		// @SuppressWarnings("unused")
		// CSTNLayout cstnLayout;

		/**
		 * Original CSTNUStaticLayout
		 */
		AbstractLayout<LabeledNode, ? extends Edge> originalLayout;

		/**
		 * 
		 */
		LayoutListener() {
			this.originalLayout = null;
			// this.cstnLayout = null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			try {
				AbstractLayout<LabeledNode, ? extends Edge> nextLayout;
				if (TNEditor.this.layoutToggleButton.isSelected()) {
					this.originalLayout = TNEditor.this.layoutEditor;
					// if (this.cstnLayout == null) {
					nextLayout = new CSTNLayout((TNGraph<CSTNEdge>) TNEditor.this.inputGraph, TNEditor.this.vvEditor.getSize());
					((CSTNLayout) nextLayout).setInitialY(TNEditor.this.vvEditor.getSize().height / 2);
					((CSTNLayout) nextLayout).setCurrentLayout(this.originalLayout);
					nextLayout.initialize();
					// this.cstnLayout = (CSTNLayout) nextLayout;
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
				// LayoutTransition<LabeledNode, Edge> lt = new LayoutTransition<>(TNEditor.this.vv1, TNEditor.this.layout1, nextLayout);
				// Animator animator = new Animator(lt);
				// animator.start();
				// ELSE one step transition
				((VisualizationViewer<LabeledNode, Edge>) TNEditor.this.vvEditor).setGraphLayout((Layout<LabeledNode, Edge>) nextLayout);
				TNEditor.this.vvEditor.repaint();
				// END iF
				TNEditor.this.layoutEditor = nextLayout;
			} catch (Exception e) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning("The instance cannot be layout with the workflow algorithm.");
					}
				}
			}
		}

		/**
		 * 
		 */
		public void reset() {
			TNEditor.this.layoutToggleButton.setSelected(false);
			this.originalLayout = null;
			// this.cstnLayout = null;
			return;
		}
	}

	/**
	 * @author posenato
	 */
	private class NewNetworkActivation implements ActionListener {

		public NewNetworkActivation() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (!askBeforeOverwriteCurrentNetwork())
				return;
			switch (e.getActionCommand()) {
			case "STNU":
				setDefaultParametersForNetwork(NetworkType.STNU);
				break;
			case "CSTN":
				setDefaultParametersForNetwork(NetworkType.CSTN);
				break;
			case "CSTNU":
				setDefaultParametersForNetwork(NetworkType.CSTNU);
				break;
			case "CSTNPSU":
				setDefaultParametersForNetwork(NetworkType.CSTNPSU);
				break;
			case "STN":
			default:
				setDefaultParametersForNetwork(NetworkType.STN);
				break;
			}
			TNEditor.this.inputGraphBiggerViewer.setEnabled(true);
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
			TNEditor.this.onlyToZ = e.getStateChange() == ItemEvent.SELECTED;
		}
	}

	/**
	 * @author posenato
	 */
	private class OpenFileListener implements ActionListener {
		private JFileChooser chooser;

		public OpenFileListener() {
			this.chooser = new JFileChooser(TNEditor.this.defaultDir);
			this.chooser.setDragEnabled(true);
			String msg = "The extension of the selected file determines the kind of network. Use *.stn, *.stnu, *.cstn, *.cstnu, *.stpsu, *.cstpsu";
			this.chooser.setToolTipText(msg);
			this.chooser.setApproveButtonToolTipText(msg);

			FileNameExtensionFilter stnE = new FileNameExtensionFilter("(C)STN(PS)(U) file (.(c)stn(ps)(u))", "stn", "stnu", "cstn", "cstnu", "cstnpsu",
					"stnpsu");
			this.chooser.addChoosableFileFilter(stnE);
			this.chooser.setFileFilter(stnE);
			this.chooser.setAcceptAllFileFilterUsed(false);
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			if (!askBeforeOverwriteCurrentNetwork())
				return;
			final int option = this.chooser.showOpenDialog(TNEditor.this);
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = this.chooser.getSelectedFile();
				TNEditor.this.defaultDir = file.getParent();
				try {
					TNEditor.this.loadGraphG(file);
					TNEditor.this.vvViewer.setVisible(false);
					((LayoutListener) TNEditor.this.layoutToggleButton.getActionListeners()[0]).reset();
					TNEditor.this.saveCSTNResultButton.setEnabled(false);
					jl.setText("");
					jl.setOpaque(false);
					// TNEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + TNEditor.this.g.getName());
					TNEditor.this.graphInfoLabel.setText(TNEditor.getGraphLabelDescription(TNEditor.this.inputGraph));
					TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());

				} catch (IOException | ParserConfigurationException | SAXException e1) {
					TNEditor.this.inputGraph.clear();
					TNEditor.this.vvViewer.setVisible(false);
					TNEditor.this.saveCSTNResultButton.setEnabled(false);
					String msg = "The graph has a problem in the definition:" + e1.getMessage();
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							TNEditor.LOG.warning(msg);
						}
					}
					jl.setText("<b>" + msg + "</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
				} finally {
					TNEditor.this.validate();
					TNEditor.this.repaint();
					TNEditor.this.cycle = 0;
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
			final JFileChooser chooser = new JFileChooser(TNEditor.this.defaultDir) {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public void approveSelection() {
					File f = getSelectedFile();
					if (f.exists() && getDialogType() == SAVE_DIALOG) {
						int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_OPTION);
						switch (result) {
						case JOptionPane.YES_OPTION:
							super.approveSelection();
							return;
						case JOptionPane.NO_OPTION:
							return;
						default:
						case JOptionPane.CLOSED_OPTION:
							return;
						}
					}
					super.approveSelection();
				}
			};
			// TNEditor.LOG.finest("Path wanted:" + path);
			boolean saved = false;
			while (!saved) {
				final int option = chooser.showSaveDialog(TNEditor.this);
				if (option == JFileChooser.CANCEL_OPTION)
					break;
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					TNEditor.this.defaultDir = file.getParent();
					try {
						TNEditor.this.saveGraphToFile(TNEditor.this.inputGraph, file);
						saved = true;
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(TNEditor.this.vvViewer, "The selected file cannot be used for saving the graph");
						saved = false;
					}
				}
			}
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) TNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stn = new STN((TNGraph<STNEdge>) TNEditor.this.checkedGraph);
			TNEditor.this.stn.setDefaultConsistencyCheckAlg(TNEditor.this.stnCheckAlg);
			TNEditor.this.stn.setOutputCleaned(TNEditor.this.cleanResult);

			
			try {
				TNEditor.this.stn.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The network has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(TNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// TNEditor.this.vv2.validate();
				// TNEditor.this.vv2.repaint();
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
			
			if (TNEditor.this.stnCheckAlg== STN.CheckAlgorithm.Dijkstra && TNEditor.this.stn.getMinNegativeWeight()<0) {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>Dijkstra algorithm cannot be applied to a network having edges with negative values.</b>");
				// jl.setIcon(TNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// TNEditor.this.vv2.validate();
				// TNEditor.this.vv2.repaint();
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
				
			jl.setBackground(Color.orange);
			TNEditor.this.stnStatus = TNEditor.this.stn.consistencyCheck();
			if (TNEditor.this.stnStatus.consistency) {

				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is STN consistent.");
				jl.setBackground(Color.green);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						TNEditor.LOG.finest("Final controllable graph: " + TNEditor.this.stn.getGChecked());
					}
				}
			} else {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent.</b>");
				ObjectList<LabeledNode> negativeCycle = TNEditor.this.stnStatus.negativeCycle;
				if (negativeCycle != null) {
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE
							+ "'>&nbsp;<b>The graph is not consistent. The found negative cycle is in red color.</b>");
					// draw edge in negative cycle by red
					for (int i = 0; i < negativeCycle.size() - 1; i++) {
						LabeledNode s = negativeCycle.get(i);
						LabeledNode d = negativeCycle.get(i + 1);
						s.setInNegativeCycle(true);
						d.setInNegativeCycle(true);

						STNEdge edge = (STNEdge) TNEditor.this.checkedGraph.findEdge(s, d);
						edge.setInNegativeCycle(true);
					}
				}
				if (TNEditor.this.stnStatus.negativeLoopNode != null) {
					TNEditor.this.stnStatus.negativeLoopNode.setInNegativeCycle(true);
				}
			}
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) TNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stn = new STN((TNGraph<STNEdge>) TNEditor.this.checkedGraph);
			TNEditor.this.stn.setDefaultConsistencyCheckAlg(TNEditor.this.stnCheckAlg);
			TNEditor.this.stn.setOutputCleaned(TNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			boolean status = TNEditor.this.stn.makeDispatchable();
			if (status) {
				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is dispatchable.");
				jl.setBackground(Color.green);
			} else {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent and, therefore, it cannot be made dispatchable.</b>");
			}
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNGraph<STNEdge> g1 = new TNGraph<>((TNGraph<STNEdge>) TNEditor.this.inputGraph,
					(Class<? extends STNEdge>) EdgeSupplier.DEFAULT_STN_EDGE_CLASS);
			((TNGraph<STNEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stn = new STN((TNGraph<STNEdge>) TNEditor.this.checkedGraph);
			try {
				TNEditor.this.stn.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(TNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// TNEditor.this.vv2.validate();
				// TNEditor.this.vv2.repaint();
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
			jl.setText("STN initialized.");
			// jl.setIcon(TNEditor.infoIcon);
			jl.setBackground(Color.orange);
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
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
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stn = new STN((TNGraph<STNEdge>) TNEditor.this.inputGraph);
			TNEditor.this.stn.setOutputCleaned(TNEditor.this.cleanResult);

			LabeledNode node = null;
			while (node == null) {
				LabeledNode[] nodes = TNEditor.this.stn.getG().getVerticesArray();
				node = (LabeledNode) JOptionPane.showInputDialog(
						TNEditor.this.rowForSTNButtons,
						"Chose the source node:",
						"Customized Dialog",
						JOptionPane.PLAIN_MESSAGE,
						null,
						nodes,
						"Z");
			}
			jl.setBackground(Color.orange);
			TNGraph<STNEdge> g1 = TNEditor.this.stn.getSTNPredecessorSubGraph(node);
			if (g1 != null) {
				((TNGraph<STNEdge>) TNEditor.this.checkedGraph).takeIn(g1);
				jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>Predecessor subgraph of " + node.getName() + ".");
				jl.setBackground(Color.green);
			} else {
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not consistent and, therefore, it is not possible to find the predecessor subgraph.</b>");
			}
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
		}
	}

	/**
	 * @author posenato
	 */
	private class STNUControllabilityCheckListener implements ActionListener {

		public STNUControllabilityCheckListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNEditor.this.saveCSTNResultButton.setEnabled(false);

			TNGraph<STNUEdge> g1 = new TNGraph<>((TNGraph<STNUEdge>) TNEditor.this.inputGraph,
					(Class<? extends STNUEdge>) EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
			((TNGraph<STNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stnu = new STNU((TNGraph<STNUEdge>) TNEditor.this.checkedGraph);
			TNEditor.this.stnu.setDefaultConsistencyCheckAlg(TNEditor.this.stnuCheckAlg);
			TNEditor.this.stnu.setOutputCleaned(TNEditor.this.cleanResult);

			jl.setBackground(Color.orange);
			try {
				TNEditor.this.stnuStatus = TNEditor.this.stnu.dynamicControllabilityCheck();
				if (TNEditor.this.stnuStatus.isControllability()) {

					jl.setText("<img align='middle' src='" + INFO_ICON_FILE + "'>&nbsp;<b>The graph is STNU consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							TNEditor.LOG.finer("Final controllable graph: " + TNEditor.this.stnu.getGChecked());
						}
					}
				} else {
					jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>The graph is not controllable.</b>");
				}
			} catch (final WellDefinitionException ex) {
				jl.setText("There is a problem in the code: " + ex.getMessage());
			}
			TNEditor.this.cycle = 0;
			TNEditor.this.updatevvViewer();
		}
	}

	/**
	 * @author posenato
	 */
	private class STNUInitListener implements ActionListener {

		public STNUInitListener() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = TNEditor.this.viewerMessageArea;
			TNGraph<STNUEdge> g1 = new TNGraph<>((TNGraph<STNUEdge>) TNEditor.this.inputGraph,
					(Class<? extends STNUEdge>) EdgeSupplier.DEFAULT_STNU_EDGE_CLASS);
			((TNGraph<STNUEdge>) TNEditor.this.checkedGraph).takeIn(g1);

			TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
			TNEditor.this.stnu = new STNU((TNGraph<STNUEdge>) TNEditor.this.checkedGraph);
			try {
				TNEditor.this.stnu.initAndCheck();
			} catch (final Exception ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						TNEditor.LOG.warning(msg);
					}
				}
				jl.setText("<img align='middle' src='" + WARN_ICON_FILE + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(TNEditor.warnIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				// TNEditor.this.vv2.validate();
				// TNEditor.this.vv2.repaint();
				TNEditor.this.validate();
				TNEditor.this.repaint();
				return;
			}
			jl.setText("STNU initialized.");
			// jl.setIcon(TNEditor.infoIcon);
			TNEditor.this.cycle = 0;
			jl.setBackground(Color.orange);
			TNEditor.this.updatevvViewer();
		}
	}

	/**
	 * Name of the derived graph big viewer
	 */
	public static final String DERIVED_GRAPH_BIG_VIEWER_NAME = "Resulting network bigger viewer";

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
	public static final String INPUT_GRAPH_BIG_VIEWER_NAME = "Bigger viewer";

	/**
	 *
	 */
	static final URL INFO_ICON_FILE = TNEditor.class.getClassLoader().getResource("images/metal-info.png");

	/**
	 * class logger
	 */
	static final Logger LOG = Logger.getLogger(TNEditor.class.getName());

	/**
	 *
	 */
	static final URL WARN_ICON_FILE = TNEditor.class.getClassLoader().getResource("images/metal-warning.png");

	/**
	 * Standard serial number
	 */
	@SuppressWarnings("unused")
	private static final long SERIAL_VERSION_UID = 647420826043015778L;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * Version
	 */
	// Till version is update by SVN ($REV$), I put replace to avoid $Rev: string$
	private static final String VERSION = "Version  $Rev$".replace("$Rev: ", "").replace("$", "");

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		TNEditor editor = new TNEditor();
		if (!editor.manageParameters(args))
			return;
		editor.init();
	}

	/**
	 * @param g graph
	 * @return a string describing essential characteristics of the graph.
	 */
	static String getGraphLabelDescription(TNGraph<?> g) {
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
		if (g.getContingentNodeCount() > 0) {
			sb.append(", #contingent: ");
			sb.append(g.getContingentNodeCount());
		}
		return sb.toString();
	}

	/**
	 * Sets up vertex and edges renders.
	 * 
	 * @param <E> type of edge
	 * @param viewer viewer
	 * @param firstViewer true if viewer is in the first position
	 */
	static <E extends Edge> void setNodeEdgeRenders(BasicVisualizationServer<LabeledNode, E> viewer, boolean firstViewer) {
		RenderContext<LabeledNode, E> renderCon = viewer.getRenderContext();
		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		renderCon.setVertexLabelTransformer(NodeRendering.vertexLabelFunction);
		renderCon.setVertexDrawPaintTransformer(NodeRendering.nodeDrawPaintTransformer(viewer.getPickedVertexState(), Color.yellow, Color.green, Color.red));
		renderCon.setVertexFillPaintTransformer(NodeRendering.nodeDrawPaintTransformer(viewer.getPickedVertexState(), Color.yellow, Color.green, Color.red));
		((VisualizationViewer<LabeledNode, E>) viewer).setVertexToolTipTransformer(NodeRendering.vertexToolTipFunction);

		// EDGE setting
		renderCon.setEdgeDrawPaintTransformer(
				EdgeRendering.edgeDrawPaintFunction(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray, Color.red));
		renderCon.setEdgeLabelTransformer(EdgeRendering.edgeLabelFunction);
		renderCon.setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		renderCon.setEdgeStrokeTransformer(EdgeRendering.edgeStrokeFunction);
		renderCon.setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<LabeledNode, E>(0.65, 0.5));
		renderCon.setArrowDrawPaintTransformer(
				EdgeRendering.edgeDrawPaintFunction(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray, Color.red));
		renderCon.setArrowFillPaintTransformer(
				EdgeRendering.edgeDrawPaintFunction(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray, Color.red));
		if (firstViewer) {
			renderCon.setEdgeFontTransformer(EdgeRendering.edgeFontFunction);
		}
		renderCon.setLabelOffset((firstViewer) ? 6 : 3);
	}

	/**
	 * The current wanted semantics
	 */
	public DCSemantics dcCurrentSem = DCSemantics.Std;

	/**
	 * TNGraph structures necessary to represent derived graph.
	 */
	final TNGraph<? extends Edge> checkedGraph;

	/**
	 * Cleaned result. True to store a cleaned result
	 */
	@Option(required = false, name = "-cleaned", usage = "Show a cleaned result. A cleaned graph does not contain empty edges or labeled values containing unknown literals.")
	boolean cleanResult = false;

	/**
	 * True if contingent link as to be represented also as ordinary constraints.
	 */
	@Option(required = false, name = "-ctgAsOrdinary", usage = "Manage contingent link also as ordinary constraints. It is not necessary but it helps to find some stricter upper bounds.")
	boolean contingentAlsoAsOrdinary = true;

	/**
	 * 
	 */
	JPanel controlSouthPanel;

	/**
	 * CSTN checker
	 */
	CSTN cstn;

	/**
	 * Which check alg for CSTN
	 */
	CSTN.CheckAlgorithm cstnCheckAlg = CSTN.CheckAlgorithm.HunsbergerPosenato20;

	
	/**
	 * Drop-down list for selecting CSTN CheckingAlgorithm
	 */
	JComboBox<CSTN.CheckAlgorithm> checkingAlgCSTNComboBox;
	
	/**
	 * semantic combo for CSTN
	 */
	JComboBox<DCSemantics> cstnDCSemmanticsComboBox;

	/**
	 * CSTNPSU checker
	 */
	CSTNPSU cstnpsu;

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
	 * Current edge implementation class
	 */
	Class<? extends Edge> currentEdgeImpl;

	/**
	 * The kind of network the system is currently showing
	 */
	TNGraph.NetworkType currentTNGraphType;

	/**
	 * Number of cycles of CSTN(U) check step-by-step
	 */
	int cycle;

	/**
	 * Default load/save directory
	 */
	String defaultDir = "";

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
	 * Button for input network bigger viewer
	 */
	JButton inputGraphBiggerViewer;

	/**
	 * Button for derived network bigger viewer
	 */
	JButton derivedGraphBiggerViewer;

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
	 * Position of the mode box for the main editor
	 */
	int modeBoxIndex;

	/**
	 * Position of the mode box for the the viewer
	 */
	int modeBoxViewerIndex;

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
	 * the preferred sizes for the two views
	 */
	Dimension preferredSize = new Dimension(780, 768);

	/**
	 * Reaction time for CSTN
	 */
	int reactionTime = 1;

	/**
	 * 
	 */
	JPanel rowForAppButtons;

	/**
	 * 
	 */
	JPanel rowForSTNButtons;

	/**
	 * 
	 */
	JPanel rowForSTNUButtons;

	/**
	 * Result Save Button
	 */
	JButton saveCSTNResultButton;

	/**
	 * STN checker
	 */
	STN stn;

	/**
	 * Which check alg to use for STN
	 */
	STN.CheckAlgorithm stnCheckAlg = STN.CheckAlgorithm.AllPairsShortestPaths;

	/**
	 * STN check status
	 */
	STNCheckStatus stnStatus;

	/**
	 * STNU checker
	 */
	STNU stnu;

	/**
	 * Which check alg to use for STNU
	 */
	STNU.CheckAlgorithm stnuCheckAlg = STNU.CheckAlgorithm.Morris2014;

	/**
	 * STNU check status
	 */
	STNUCheckStatus stnuStatus;

	/** Validation ppanel for CSTN row */
	ValidationPanel validationPanelCSTN;
	/** Validation ppanel for CSTNU row */
	ValidationPanel validationPanelCSTNU;

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

	/**
	 * with unknown literal
	 * boolean withUknown = true;
	 */

	/**
	 * Some buttons have meaning only for some contexts.
	 * The default is not to show.
	 */
	@Option(required = false, name = "-extraButtons", usage = "To see some extra buttons for some special feature (development mode).")
	boolean extraButtons = false;

	/**
	 * Initiliazes the fundamental fields.
	 * The initilization of the rest of fields and the starting of GUI is made by {@link #init()} method, after that possible input parameter are read.
	 */
	public TNEditor() {
		super("TNEditor " + VERSION);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Screen Bounds: " + bounds);
			}
		}

		// Using a null input TNGraph for setting all graphical aspects.
		// When the graph will be load, inputGraph will be updated copying all the graph inside it (takeIn method).
		this.inputGraph = new TNGraph<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
		this.checkedGraph = new TNGraph<>(EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);

		this.preferredSize = new Dimension((bounds.width - 30) / 2, bounds.height - 260);
		this.layoutEditor = new CSTNUStaticLayout<>(this.inputGraph, this.preferredSize);
		this.layoutViewer = new CSTNUStaticLayout<>(this.checkedGraph, this.preferredSize);
		this.vvEditor = new VisualizationViewer<>(this.layoutEditor, this.preferredSize);
		this.vvEditor.setName(TNEditor.EDITOR_NAME);
		this.vvViewer = new VisualizationViewer<>(this.layoutViewer, this.preferredSize);
		this.vvViewer.setName(TNEditor.DISTANCE_VIEWER_NAME);
	}

	/**
	 * Initialize all others component of the GUI using the parameter values passed by
	 */
	public void init() {
		// buildRenderContext(this.vvEditor, true);
		// buildRenderContext(this.vvViewer, false);
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
		this.controlSouthPanel = new JPanel(new GridLayout(2, 1));// one row for AppButtons, one for STN, one for STNU, one for validationPanelCSTN, one for
																	// validationPanelCSTNU
		this.rowForAppButtons = new JPanel();
		this.controlSouthPanel.add(this.rowForAppButtons, 0);// for tuning application
		contentPane.add(this.controlSouthPanel, BorderLayout.SOUTH);

		this.rowForSTNButtons = new JPanel();
		this.rowForSTNButtons.setBorder(BorderFactory.createLineBorder(getForeground(), 1));
		this.rowForSTNUButtons = new JPanel();
		this.rowForSTNUButtons.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		JPanel rowForCSTNButtons = new JPanel();
		this.validationPanelCSTN = new ValidationPanel();
		final ValidationGroup validationGroupCSTN = this.validationPanelCSTN.getValidationGroup();
		this.validationPanelCSTN.setInnerComponent(rowForCSTNButtons);
		this.validationPanelCSTN.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		JPanel rowForCSTNUButtons = new JPanel();// , rowForCSTNPSUButtons = new JPanel();
		this.validationPanelCSTNU = new ValidationPanel();
		this.validationPanelCSTNU.setInnerComponent(rowForCSTNUButtons);
		this.validationPanelCSTNU.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		// FIRST ROW OF COMMANDS
		// mode box for the editor
		this.rowForAppButtons.add(new JComboBox<>());// the real ComboBox is added after the initialization.
		this.modeBoxIndex = 0;

		this.layoutToggleButton = new JToggleButton("Layout input graph");
		this.layoutToggleButton.setEnabled(false);
		this.layoutToggleButton.addActionListener(new LayoutListener());
		if (this.extraButtons) {
			// I create this.layoutToggleButton in any case because it is manipulated in many places...
			this.rowForAppButtons.add(this.layoutToggleButton);
		}
		this.inputGraphBiggerViewer = new JButton(INPUT_GRAPH_BIG_VIEWER_NAME);
		this.inputGraphBiggerViewer.setEnabled(false);
		this.inputGraphBiggerViewer.addActionListener(new BigViewerListener(true));
		this.rowForAppButtons.add(this.inputGraphBiggerViewer);

		// AnnotationControls<LabeledNode,Edge> annotationControls =
		// new AnnotationControls<LabeledNode,Edge>(gm.getAnnotatingPlugin());
		// controls.add(annotationControls.getAnnotationsToolBar());

		// final JRadioButton excludeR1R2Button = new JRadioButton("R1 and R2 rule disabled", this.excludeR1R2);
		// excludeR1R2Button.addItemListener(new ItemListener() {
		// @Override
		// public void itemStateChanged(final ItemEvent ev) {
		// if (ev.getStateChange() == ItemEvent.SELECTED) {
		// TNEditor.this.excludeR1R2 = true;
		// TNEditor.LOG.fine("excludeR1R2 flag set to true");
		// } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
		// TNEditor.this.excludeR1R2 = false;
		// TNEditor.LOG.fine("excludeR1R2 flag set to false");
		// }
		// }
		// });
		// rowForAppButtons.add(excludeR1R2Button);

		this.derivedGraphBiggerViewer = new JButton(DERIVED_GRAPH_BIG_VIEWER_NAME);
		this.derivedGraphBiggerViewer.setEnabled(false);
		this.derivedGraphBiggerViewer.addActionListener(new BigViewerListener(false));
		this.rowForAppButtons.add(this.derivedGraphBiggerViewer);

		// mode box for the distance viewer
		this.rowForAppButtons.add(new JComboBox<>());
		this.modeBoxViewerIndex = this.rowForAppButtons.getComponentCount() - 1;

		HelpListener help = new HelpListener(this);

		JButton buttonCheck = new JButton("Help");
		buttonCheck.addActionListener(help);
		buttonCheck.setActionCommand(HelpListener.AvailableHelp.GenericHelp.toString());
		this.rowForAppButtons.add(buttonCheck);

		// SECOND ROW OF COMMANDS
		// ROW FOR STNs
		this.rowForSTNButtons.add(new JLabel("Checking Algorithm: "));
		JComboBox<STN.CheckAlgorithm> cAlgCombo = new JComboBox<>(STN.CheckAlgorithm.values());
		cAlgCombo.setSelectedItem(this.stnCheckAlg);
		cAlgCombo.addActionListener(new CheckAlgListener<>(cAlgCombo));
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

		buttonCheck = new JButton("PredecessorSubGraph");
		buttonCheck.addActionListener(new STNPredecessorGraphListener());
		this.rowForSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("STN Help");
		buttonCheck.addActionListener(help);
		buttonCheck.setActionCommand(HelpListener.AvailableHelp.STNHelp.toString());
		this.rowForSTNButtons.add(buttonCheck);

		// ROW FOR STNUs
		this.rowForSTNUButtons.add(new JLabel("Checking Algorithm: "));
		JComboBox<STNU.CheckAlgorithm> cAlgComboSTNU = new JComboBox<>(STNU.CheckAlgorithm.values());
		cAlgComboSTNU.setSelectedItem(this.stnuCheckAlg);
		cAlgComboSTNU.addActionListener(new CheckAlgListener<>(cAlgComboSTNU));
		this.rowForSTNUButtons.add(cAlgComboSTNU);

		buttonCheck = new JButton("Init");
		buttonCheck.addActionListener(new STNUInitListener());
		this.rowForSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("Controllability");
		buttonCheck.addActionListener(new STNUControllabilityCheckListener());
		this.rowForSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("STNU Help");
		buttonCheck.addActionListener(help);
		buttonCheck.setActionCommand(HelpListener.AvailableHelp.STNUHelp.toString());
		this.rowForSTNUButtons.add(buttonCheck);

		// ROW FOR CSTNs
		// JCheckBox withUnkwon = new JCheckBox("With unknown literals");
		// withUnkwon.setSelected(this.withUknown);
		// withUnkwon.addItemListener(new ItemListener() {
		// @Override
		// public void itemStateChanged(ItemEvent e) {
		// TNEditor.this.withUknown = e.getStateChange() == ItemEvent.SELECTED;
		// }
		// });
		// rowForCSTNButtons.add(withUnkwon);
		this.cstnDCSemmanticsComboBox = new JComboBox<>(DCSemantics.values());// this panel must be declared here because used by checkingAlgCSTNComboBox
		this.epsilonPanel = new JPanel(new FlowLayout());// this panel must be declared here because is used inside DCSemanticsListener

		rowForCSTNButtons.add(new JLabel("Checking Algorithm: "));
		this.checkingAlgCSTNComboBox = new JComboBox<>(CSTN.CheckAlgorithm.values());
		this.checkingAlgCSTNComboBox.addActionListener(new CheckAlgListener<>(this.checkingAlgCSTNComboBox));
		this.checkingAlgCSTNComboBox.setSelectedItem(this.cstnCheckAlg);
		rowForCSTNButtons.add(this.checkingAlgCSTNComboBox);

		rowForCSTNButtons.add(new JLabel("DC Semantics: "));
		this.cstnDCSemmanticsComboBox.addActionListener(new DCSemanticsListener(this.cstnDCSemmanticsComboBox));
		this.cstnDCSemmanticsComboBox.setSelectedItem(this.dcCurrentSem); //set by cascade from checkingAlgCSTNComboBox.setSelectedItem(this.cstnCheckAlg);
		rowForCSTNButtons.add(this.cstnDCSemmanticsComboBox);

		//
		// epsilon panel
		//
		// TNEditor.this.epsilonPanel = new JPanel(new FlowLayout());declared before because needed by this.cstnDCSemmanticsCombo
		this.epsilonPanel.add(new JLabel("System reacts "));
		final JFormattedTextField jreactionTime = new JFormattedTextField();
		jreactionTime.setValue(this.reactionTime);
		jreactionTime.setColumns(3);
		jreactionTime.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				TNEditor.this.reactionTime = ((Number) jreactionTime.getValue()).intValue();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						TNEditor.LOG.finest("Property: " + evt.getPropertyName());
					}
					if (LOG.isLoggable(Level.INFO)) {
						TNEditor.LOG.info("New reaction time: " + TNEditor.this.reactionTime);
					}
				}
			}
		});
		validationGroupCSTN.add(jreactionTime, StringValidators.regexp(Constants.NonNegIntValueRE, "A > 0 integer!", false));
		TNEditor.this.epsilonPanel.add(jreactionTime);
		TNEditor.this.epsilonPanel.add(new JLabel("time units after (≥) an observation."));
		TNEditor.this.epsilonPanel.setVisible(TNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
		rowForCSTNButtons.add(TNEditor.this.epsilonPanel);

		buttonCheck = new JButton("Init");
		buttonCheck.addActionListener(new CSTNInitListener());
		rowForCSTNButtons.add(buttonCheck);

		// buttonCheck = new JButton("DC Check HP_18 (only with IR or ε)");
		// buttonCheck.addActionListener(new CSTNRestrictedCheckListener());
		// rowForCSTNButtons.add(buttonCheck);
		//
		buttonCheck = new JButton("Controllability");
		buttonCheck.addActionListener(new CSTNControllabilityCheckListener());
		rowForCSTNButtons.add(buttonCheck);
		//
		// buttonCheck = new JButton("CSTN Check HP_20");
		// buttonCheck.addActionListener(new CSTNPotentialCheckListener());
		// rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new CSTNOneStepListener());
		rowForCSTNButtons.add(buttonCheck);

		this.saveCSTNResultButton = new JButton("Save checked CSTN");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new TNSaveListener());
		rowForCSTNButtons.add(this.saveCSTNResultButton);

		// buttonCheck = new JButton("CSTN All-Pair Shortest Paths");
		// buttonCheck.addActionListener(new CSTNAPSPListener());
		// rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Help");
		buttonCheck.addActionListener(help);
		buttonCheck.setActionCommand(HelpListener.AvailableHelp.CSTNHelp.toString());
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

		buttonCheck = new JButton("CSTNPSU/FTNU Check");
		buttonCheck.addActionListener(new CSTNPSUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Help");
		buttonCheck.addActionListener(help);
		buttonCheck.setActionCommand(HelpListener.AvailableHelp.CSTNUHelp.toString());
		rowForCSTNUButtons.add(buttonCheck);

		NewNetworkActivation newNetAct = new NewNetworkActivation();

		// MENU
		final JMenuBar menuBar = new JMenuBar();
		menuBar.setBackground(Color.LIGHT_GRAY);
		menuBar.setOpaque(true);
		menuBar.setUI(new MetalMenuBarUI());

		this.defaultDir = (new JFileChooser()).getCurrentDirectory() + this.defaultDir;
		final JMenu menu = new JMenu("File");
		menu.setOpaque(false);

		JMenuItem aboutItem = new JMenuItem("About TNEditor");
		aboutItem.setMnemonic('A');
		aboutItem.addActionListener(event -> {
			JOptionPane.showMessageDialog(this,
					"TNEditor: Temporal Network Editor\n" + VERSION + "\nby Roberto Posenato (roberto.posenato@univr.it)",
					"About",
					JOptionPane.INFORMATION_MESSAGE);
		} // end method actionPerformed
		); // end call to addActionListener
		menu.add(aboutItem); // add about item to file menu
		menu.addSeparator();

		final JMenu newFile = new JMenu("New network");
		final JMenuItem newStn = new JMenuItem("STN");
		newStn.setActionCommand("STN");
		newStn.addActionListener(newNetAct);
		final JMenuItem newStnu = new JMenuItem("STNU");
		newStnu.setActionCommand("STNU");
		newStnu.addActionListener(newNetAct);
		final JMenuItem newCstn = new JMenuItem("CSTN");
		newCstn.setActionCommand("CSTN");
		newCstn.addActionListener(newNetAct);
		final JMenuItem newCstnu = new JMenuItem("CSTNU");
		newCstnu.setActionCommand("CSTNU");
		newCstnu.addActionListener(newNetAct);
		final JMenuItem newCstnpsu = new JMenuItem("CSTNPSU/FTNU");
		newCstnpsu.setActionCommand("CSTNPSU");
		newCstnpsu.addActionListener(newNetAct);

		newFile.add(newStn);
		newFile.add(newStnu);
		newFile.add(newCstn);
		newFile.add(newCstnu);
		newFile.add(newCstnpsu);
		menu.add(newFile);

		final JMenuItem openItem = new JMenuItem("Open...");
		openItem.addActionListener(new OpenFileListener());
		menu.add(openItem);
		final JMenuItem saveItem = new JMenuItem("Save...");
		saveItem.addActionListener(new SaveFileListener());
		menu.add(saveItem);

		final JMenuItem quitItem = new JMenuItem("Quit TNEditor");
		quitItem.setMnemonic(KeyEvent.VK_Q);
		quitItem.setAccelerator(KeyStroke.getKeyStroke(
				KeyEvent.VK_Q, ActionEvent.META_MASK));
		quitItem.setToolTipText("Exit application");
		quitItem.addActionListener(event -> System.exit(0));
		menu.add(quitItem);

		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		this.pack();
		this.setVisible(true);
	}

	/**
	 * Adds vertex and edges renders, tooltips and mouse behavior to a viewer.
	 * 
	 * @param <E> type of edge
	 * @param viewer viewer
	 * @param firstViewer true if viewer is in the first position
	 */
	<E extends Edge> void buildRenderContext(VisualizationViewer<LabeledNode, E> viewer, boolean firstViewer) {
		LOG.finest("buildRenderContext: " + viewer + ", firstViewer:" + firstViewer);

		// vertex and edge renders
		setNodeEdgeRenders(viewer, firstViewer);

		// mouse action
		@SuppressWarnings("unchecked")
		EditingModalGraphMouse<LabeledNode, E> graphMouse = new EditingModalGraphMouse<>(
				viewer.getRenderContext(),
				new LabeledNodeSupplier(),
				(EdgeSupplier<E>) new EdgeSupplier<>(TNEditor.this.currentEdgeImpl), // only after graph load it is possible to set edge supplier.
				TNEditor.this,
				firstViewer);
		LOG.finest("buildRenderContext.graphMouse " + graphMouse);
		// graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		viewer.setGraphMouse(graphMouse);
		viewer.addKeyListener(graphMouse.getModeKeyListener());

		// set the operation mode
		if (firstViewer) {
			TNEditor.this.rowForAppButtons.remove(this.modeBoxIndex);
			TNEditor.this.rowForAppButtons.add(graphMouse.getModeComboBox(), this.modeBoxIndex);
		} else {
			TNEditor.this.rowForAppButtons.remove(this.modeBoxViewerIndex);
			TNEditor.this.rowForAppButtons.add(graphMouse.getModeComboBox(), this.modeBoxViewerIndex);
		}
	}

	/**
	 * Loads TNGraph stored in file 'fileName' into attribute this.g.<br>
	 * <b>Be careful!</b>
	 * The extension of the file name determines the kind of TNGraph.
	 * 
	 * <pre>
	 * .stn ===&gt; STN 
	 * .cstn ===&gt; CSTN
	 * .stnu ===&gt; STNU
	 * .cstnu ===&gt; CSTNU
	 * .cstpsu ===&gt; CSTNPSU
	 * </pre>
	 * 
	 * @param fileName file name
	 * @throws SAXException none
	 * @throws ParserConfigurationException none
	 * @throws IOException none
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	void loadGraphG(final File fileName) throws IOException, ParserConfigurationException, SAXException {

		String name = fileName.getName();
		if (name.endsWith(".stn")) {
			setDefaultParametersForNetwork(NetworkType.STN);
		} else {
			if (name.endsWith(".stnu")) {
				setDefaultParametersForNetwork(NetworkType.STNU);
			} else {
				if (name.endsWith(".cstn")) {
					setDefaultParametersForNetwork(NetworkType.CSTN);
				} else {
					if (name.endsWith(".cstnu")) {
						setDefaultParametersForNetwork(NetworkType.CSTNU);
					} else {
						if (name.endsWith(".stnpsu") || name.endsWith(".cstnpsu")) {
							setDefaultParametersForNetwork(NetworkType.CSTNPSU);
						}
					}
				}
			}
		}
		TNEditor.this.inputGraph.takeIn((new TNGraphMLReader()).readGraph(fileName, this.currentEdgeImpl));
		TNEditor.this.inputGraph.setInputFile(fileName);
		TNEditor.this.mapInfoLabel.setText(TNEditor.this.inputGraph.getEdgeFactory().toString());
		TNEditor.this.inputGraphBiggerViewer.setEnabled(true);
		// LOG.severe(TNEditor.this.inputGraph.getEdgeFactory().toString());
		TNEditor.this.validate();
		TNEditor.this.repaint();
	}

	/**
	 * 
	 */
	void resetDerivedGraphStatus() {
		this.vvViewer.setVisible(false);
		this.viewerMessageArea.setText("");
		this.viewerMessageArea.setBackground(Color.lightGray);
		this.cycle = 0;
	}

	/**
	 * @param graphToSave graph to save
	 * @param file file where to save
	 * @throws IOException if file cannot be used for saving the graph.
	 */
	void saveGraphToFile(final TNGraph<? extends Edge> graphToSave, final File file) throws IOException {
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(this.layoutEditor);
		graphToSave.setName(file.getName());
		graphWriter.save(graphToSave, file);
	}

	/**
	 * Set all default parameter about the editor according with the input type.
	 * 
	 * @param networkType network type
	 */
	@SuppressWarnings("unchecked")
	void setDefaultParametersForNetwork(TNGraph.NetworkType networkType) {
		switch (networkType) {
		case STNU:
			TNEditor.this.currentTNGraphType = NetworkType.STNU;
			TNEditor.this.currentEdgeImpl = EdgeSupplier.DEFAULT_STNU_EDGE_CLASS;
			break;
		case CSTN:
			TNEditor.this.currentTNGraphType = NetworkType.CSTN;
			TNEditor.this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTN_EDGE_CLASS;
			break;
		case CSTNU:
			TNEditor.this.currentTNGraphType = NetworkType.CSTNU;
			TNEditor.this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS;
			break;
		case CSTNPSU:
			TNEditor.this.currentTNGraphType = NetworkType.CSTNPSU;
			TNEditor.this.currentEdgeImpl = EdgeSupplier.DEFAULT_CSTNPSU_EDGE_CLASS;
			break;
		default:
		case STN:
			TNEditor.this.currentTNGraphType = NetworkType.STN;
			TNEditor.this.currentEdgeImpl = EdgeSupplier.DEFAULT_STN_EDGE_CLASS;
			break;
		}

		@SuppressWarnings("rawtypes")
		TNGraph g = new TNGraph<>(this.currentEdgeImpl);
		TNEditor.this.inputGraph.takeIn(g);
		g = new TNGraph<>(this.currentEdgeImpl);
		TNEditor.this.checkedGraph.takeIn(g);

		showCommandRow(networkType);
		buildRenderContext(TNEditor.this.vvEditor, true);
		buildRenderContext(TNEditor.this.vvViewer, false);

		// updateEdgeSupplierInViewer(this.vvEditor);
		// updateEdgeSupplierInViewer(this.vvViewer);
		TNEditor.this.validate();
		TNEditor.this.repaint();
	}

	/**
	 * In the command panel, only one row of commands is visible.
	 * This method makes visible one row, hiding the others.
	 * 
	 * @param networkType network type
	 */
	void showCommandRow(TNGraph.NetworkType networkType) {
		switch (networkType) {
		case CSTNU:
		case CSTNPSU:
			this.validationPanelCSTNU.setVisible(true);
			this.validationPanelCSTN.setVisible(false);
			this.rowForSTNUButtons.setVisible(false);
			this.rowForSTNButtons.setVisible(false);
			if (this.controlSouthPanel.getComponentCount() == 2)
				this.controlSouthPanel.remove(1);
			this.controlSouthPanel.add(this.validationPanelCSTNU, 1);// for button regarding CSTNU
			break;
		case CSTN:
			this.validationPanelCSTNU.setVisible(false);
			this.validationPanelCSTN.setVisible(true);
			this.rowForSTNUButtons.setVisible(false);
			this.rowForSTNButtons.setVisible(false);
			if (this.controlSouthPanel.getComponentCount() == 2)
				this.controlSouthPanel.remove(1);
			this.controlSouthPanel.add(this.validationPanelCSTN, 1);// for button regarding CSTN
			break;
		case STNU:
			this.validationPanelCSTNU.setVisible(false);
			this.validationPanelCSTN.setVisible(false);
			this.rowForSTNButtons.setVisible(false);
			this.rowForSTNUButtons.setVisible(true);
			if (this.controlSouthPanel.getComponentCount() == 2)
				this.controlSouthPanel.remove(1);
			this.controlSouthPanel.add(this.rowForSTNUButtons, 1);// for button regarding STN
			break;
		case STN:
		default:
			this.validationPanelCSTNU.setVisible(false);
			this.validationPanelCSTN.setVisible(false);
			this.rowForSTNUButtons.setVisible(false);
			this.rowForSTNButtons.setVisible(true);
			if (this.controlSouthPanel.getComponentCount() == 2)
				this.controlSouthPanel.remove(1);
			this.controlSouthPanel.add(this.rowForSTNButtons, 1);// for button regarding STN
		}
	}

	/**
	 * Updates Edge Supplier in viewer considering the current type of loaded graph.
	 * 
	 * @param viewer
	 *            @SuppressWarnings("unchecked")
	 *            <E extends Edge> void updateEdgeSupplierInViewer(VisualizationViewer<LabeledNode, E> viewer) {
	 *            ((EditingModalGraphMouse<LabeledNode, E>) viewer.getGraphMouse()).setEdgeEditingPlugin((EdgeSupplier<E>) new
	 *            EdgeSupplier<>(this.currentEdgeImpl));
	 *            }
	 */

	/**
	 * Update the vvViewer after a check making some common operations.
	 */
	void updatevvViewer() {
		this.viewerMessageArea.setOpaque(true);
		this.updateNodePositions();
		this.vvViewer.setCursor(Cursor.getDefaultCursor());
		this.vvViewer.setVisible(true);
		this.vvViewer.validate();
		this.vvViewer.repaint();
		this.derivedGraphBiggerViewer.setEnabled(true);
		this.validate();
		this.repaint();
	}

	/**
	 * Shows a ConfirmDialog to ask the permission to overwrite the current input network.
	 * 
	 * @return true is the user clicked YES, false for any other action
	 */
	boolean askBeforeOverwriteCurrentNetwork() {
		if (this.inputGraph != null && this.inputGraph.getVertexCount() > 0) {
			int result = JOptionPane.showConfirmDialog(this, "Overwrite current input network?", "Consent request", JOptionPane.YES_NO_OPTION);
			return result == JOptionPane.YES_OPTION;
		}
		return true;
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
						TNEditor.LOG.finest("Vertex of original graph: " + gV +
								"\nOriginal position (" + TNEditor.this.layoutEditor.getX(gV) + ";" + TNEditor.this.layoutEditor.getY(gV) + ")");
					}
				}
				TNEditor.this.layoutViewer.setLocation(v, TNEditor.this.layoutEditor.getX(gV), TNEditor.this.layoutEditor.getY(gV));
			} else {
				TNEditor.this.layoutViewer.setLocation(v, v.getX(), v.getY());
			}
		}
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args none
	 * @return false if a parameter is missing or it is wrong. True if every
	 *         parameters are given in a right format.
	 */
	boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (final CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this
			// will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass().getName() + " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-*.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED) + " file_name");
			return false;
		}
		return true;
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
// final JTextArea jl = (JTextArea) TNEditor.this.messagesPanel.getComponent(0);
// PrintStream output;
// final JFileChooser chooser = new JFileChooser(TNEditor.defaultDir);
// chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
// final int option = chooser.showSaveDialog(TNEditor.this);
// if (option == JFileChooser.APPROVE_OPTION) {
// File file = chooser.getSelectedFile();
// TNEditor.defaultDir = file.getParent();
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
// translator = new CSTNU2UppaalTiga(TNEditor.this.g, output);
// if (!translator.translate())
// throw new IllegalArgumentException();
// } catch (final IllegalArgumentException e1) {
// String msg = "The graph has a problem and it cannot be translated to an UPPAAL Tiga automaton:" + e1.getMessage();
// TNEditor.LOG.warning(msg);
// jl.setText(msg);
// // jl.setIcon(CSTNUEditor.warnIcon);
// jl.setOpaque(true);
// jl.setBackground(Color.orange);
// TNEditor.this.graphPanel.validate();
// TNEditor.this.graphPanel.repaint();
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
// TNEditor.this.graphPanel.validate();
// TNEditor.this.graphPanel.repaint();
// output.close();
// }
// }
// });
// rowForCSTNUButtons.add(buttonCheck);
