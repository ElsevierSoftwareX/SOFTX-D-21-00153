/**
 *
 */
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
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.xml.parsers.ParserConfigurationException;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.xml.sax.SAXException;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
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
import it.univr.di.cstnu.algorithms.CSTN;
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.CSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.CSTN.EdgesToCheck;
import it.univr.di.cstnu.algorithms.CSTNEpsilon;
import it.univr.di.cstnu.algorithms.CSTNIR;
import it.univr.di.cstnu.algorithms.CSTNIR3R;
import it.univr.di.cstnu.algorithms.CSTNPSU;
import it.univr.di.cstnu.algorithms.CSTNU;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.algorithms.CSTNUPotential;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * A simple graphical application for creating/loading/modifying/saving/checking CSTNs.
 * It is based on LabeledIntEdgePluggable.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNEditor extends JFrame implements Cloneable {

	/**
	 * @author posenato
	 */
	@SuppressWarnings("javadoc")
	private class CSTNCheckListener implements ActionListener {

		public CSTNCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			switch (CSTNEditor.this.dcCurrentSem) {
			case ε:
				CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.checkedGraph);
				break;
			case IR:
				CSTNEditor.this.cstn = new CSTNIR(CSTNEditor.this.checkedGraph);
				break;
			default:
				CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.checkedGraph);
				break;
			}
			CSTNEditor.this.cstn.withUnknown = CSTNEditor.this.withUknown;

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
				if (CSTNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.checkedGraph);
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
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
	@SuppressWarnings("javadoc")
	private class CSTNRestrictedCheckListener implements ActionListener {

		public CSTNRestrictedCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			CSTNEditor.this.cstn = new CSTNIR3R(CSTNEditor.this.checkedGraph);
			CSTNEditor.this.cstn.withUnknown = CSTNEditor.this.withUknown;

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
				if (CSTNEditor.this.cstnStatus.consistency) {

					jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.checkedGraph);
						}
					}
				} else {
					// The distance graph is not consistent
					jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
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
	@SuppressWarnings("javadoc")
	private class CSTNAPSPListener implements ActionListener {

		public CSTNAPSPListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());

			jl.setBackground(Color.orange);
			boolean consistent = false;
			CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.checkedGraph);
			consistent = CSTN.getMinimalDistanceGraph(CSTNEditor.this.cstn.getG());
			if (consistent) {

				jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is All-Pair Shortest Paths.");
				jl.setBackground(Color.green);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						CSTNEditor.LOG.finer("All-Pair Shortest Paths graph: " + CSTNEditor.this.checkedGraph);
					}
				}
			} else {
				// The distance graph is not consistent
				jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is not CSTN consistent.</b>");
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
	@SuppressWarnings("javadoc")
	private class CSTNInitListener implements ActionListener {

		public CSTNInitListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
			switch (CSTNEditor.this.dcCurrentSem) {
			case ε:
				CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.checkedGraph);
				break;
			case IR:
				CSTNEditor.this.cstn = new CSTNIR(CSTNEditor.this.checkedGraph);
				break;
			default:
				CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.checkedGraph);
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
				jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>" + msg + "</b>");
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
	@SuppressWarnings("javadoc")
	private class CSTNOneStepListener implements ActionListener {

		public CSTNOneStepListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;

			if (CSTNEditor.this.cycle == -1)
				return;
			if (CSTNEditor.this.cycle == 0) {
				CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.inputGraph.getEdgeFactory().toString());
				switch (CSTNEditor.this.dcCurrentSem) {
				case ε:
					CSTNEditor.this.cstn = new CSTNEpsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.checkedGraph);
					break;
				case IR:
					CSTNEditor.this.cstn = new CSTNIR(CSTNEditor.this.checkedGraph);
					break;
				default:
					CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.checkedGraph);
					break;
				}
				CSTNEditor.this.cstn.withUnknown = CSTNEditor.this.withUknown;

				try {
					CSTNEditor.this.cstn.initAndCheck();
				} catch (final Exception ex) {
					jl.setText("There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}
				CSTNEditor.this.oneStepBackGraph = new LabeledIntGraph(CSTNEditor.this.checkedGraph, CSTNEditor.labeledIntValueMap);
				CSTNEditor.this.cstnStatus = new CSTNCheckStatus();
			} else {
				CSTNEditor.this.oneStepBackGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.checkedGraph, CSTNEditor.labeledIntValueMap));
				CSTNEditor.this.cstn.withUnknown = CSTNEditor.this.withUknown;
			}
			CSTNEditor.this.cycle++;

			jl.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.oneStepDynamicConsistencyByNode();
				CSTNEditor.this.cstnStatus.finished = CSTNEditor.this.checkedGraph.hasSameEdgesOf(CSTNEditor.this.oneStepBackGraph);
				final boolean reductionsApplied = !CSTNEditor.this.cstnStatus.finished;
				final boolean inconsistency = !CSTNEditor.this.cstnStatus.consistency;
				if (inconsistency) {
					jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is inconsistent.<b>");
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
					jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTN consistent. The number of executed cycles is "
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
	@SuppressWarnings("javadoc")
	private class CSTNSaveListener implements ActionListener {

		public CSTNSaveListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JFileChooser chooser = new JFileChooser();
			// CSTNEditor.LOG.finest("Current dir:" + f);

			// CSTNEditor.LOG.finest("Path wanted:" + path);
			chooser = new JFileChooser(CSTNEditor.defaultDir);
			final int option = chooser.showSaveDialog(CSTNEditor.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				CSTNEditor.this.saveGraphToFile(CSTNEditor.this.checkedGraph, file);
			}
		}
	}

	/**
	 * @author posenato
	 */
	@SuppressWarnings("javadoc")
	private class CSTNUCheckListener implements ActionListener {

		public CSTNUCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The CSTNU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.checkedGraph);
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The CSTNU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
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
	@SuppressWarnings({ "javadoc", "unused" })
	private class CSTNUPotentialCheckListener implements ActionListener {

		public CSTNUPotentialCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.cstnu = new CSTNUPotential(CSTNEditor.this.checkedGraph);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTNU controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.checkedGraph);
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is not CSTNU controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
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
	@SuppressWarnings("javadoc")
	private class CSTNPSUCheckListener implements ActionListener {

		public CSTNPSUCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
			CSTNEditor.this.cstnu = new CSTNPSU(CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The CSTNPSU is dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.checkedGraph);
						}
					}
				} else {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The CSTNPSU is not dynamically controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
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
	@SuppressWarnings("javadoc")
	private class CSTNUInitListener implements ActionListener {

		public CSTNUInitListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));

			CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
			try {
				CSTNEditor.this.cstnu.initAndCheck();
			} catch (final IllegalArgumentException | WellDefinitionException ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						CSTNEditor.LOG.warning(msg);
					}
				}
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNUEditor.warnIcon);
				jl1.setOpaque(true);
				jl1.setBackground(Color.orange);
				CSTNEditor.this.validate();
				CSTNEditor.this.repaint();
				return;
			}
			jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;LabeledIntGraph with Lower and Upper Case Labels.");
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
	@SuppressWarnings("javadoc")
	private class CSTNUOneStepListener implements ActionListener {

		public CSTNUOneStepListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.viewerMessageArea;
			if (CSTNEditor.this.cycle == -1)
				return;
			if (CSTNEditor.this.cycle == 0) {
				CSTNEditor.this.checkedGraph.takeIn(new LabeledIntGraph(CSTNEditor.this.inputGraph, CSTNEditor.labeledIntValueMap));
				CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.checkedGraph, 30 * 60, CSTNEditor.this.onlyToZ);
				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.checkedGraph.getEdgeFactory().toString());
				try {
					CSTNEditor.this.cstnu.initAndCheck();
				} catch (final Exception ex) {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the graph: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}
				CSTNEditor.this.cstnuStatus = new CSTNUCheckStatus();
				CSTNEditor.this.edgesToCheck = new EdgesToCheck(CSTNEditor.this.checkedGraph.getEdges());
			}
			CSTNEditor.this.cycle++;

			jl1.setBackground(Color.orange);
			try {
				Instant timeOut = Instant.now().plusSeconds(2700);
				CSTNEditor.this.cstnuStatus = (CSTNEditor.this.onlyToZ)
						? CSTNEditor.this.cstnu.oneStepDynamicControllabilityLimitedToZ(CSTNEditor.this.edgesToCheck, timeOut)
						: CSTNEditor.this.cstnu.oneStepDynamicControllability(CSTNEditor.this.edgesToCheck, timeOut);
				CSTNEditor.this.cstnuStatus.finished = CSTNEditor.this.edgesToCheck.size() == 0;
				final boolean reductionsApplied = !CSTNEditor.this.cstnuStatus.finished;
				final boolean notControllable = !CSTNEditor.this.cstnuStatus.consistency;
				if (notControllable) {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is inconsistent.</b>");
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
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
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
	@SuppressWarnings("javadoc")
	private class DCSemanticsListener implements ActionListener {
		JComboBox<DCSemantics> comboBox;

		public DCSemanticsListener(JComboBox<DCSemantics> comboBox) {
			this.comboBox = comboBox;
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
	private class HelpListener implements ActionListener {
		@SuppressWarnings("javadoc")
		private static final String instructions = "<html>"
				+ "<h2>Simple CSTNU Editor " + CSTNEditor.version + "</h2><h3>All Modes:</h3>"
				+ "<ul>"
				+ "<li>Right-click an empty area for <b>Create Vertex</b> popup"
				+ "<li>Right-click on a Vertex for <b>Delete Vertex</b> popup"
				+ "<li>Right-click on a Vertex for <b>Add LabeledIntEdge</b> menus <br>(if there are selected Vertices)"
				+ "<li>Right-click on an LabeledIntEdge for <b>Delete LabeledIntEdge</b> popup"
				+ "<li>Mousewheel scales with a crossover value of 1.0.<br>"
				+ "     - scales the graph layout when the combined scale is greater than 1<br>"
				+ "     - scales the graph view when the combined scale is less than 1"
				+ "</ul>"
				+ "<h3>Editing Mode:</h3>"
				+ "<ul>"
				+ "<li>Left-click an empty area to create a new Vertex"
				+ "<li>Left-click on a Vertex and drag to another Vertex to create an Undirected LabeledIntEdge"
				+ "<li>Shift+Left-click on a Vertex and drag to another Vertex to create a Directed LabeledIntEdge"
				+ "</ul>"
				+ "<h3>Picking Mode:</h3>"
				+ "<ul>"
				+ "<li>Mouse1 on a Vertex selects the vertex"
				+ "<li>Mouse1 elsewhere unselects all Vertices"
				+ "<li>Mouse1+Shift on a Vertex adds/removes Vertex selection"
				+ "<li>Mouse1+drag on a Vertex moves all selected Vertices"
				+ "<li>Mouse1+drag elsewhere selects Vertices in a region"
				+ "<li>Mouse1+Shift+drag adds selection of Vertices in a new region"
				+ "<li>Mouse1+CTRL on a Vertex selects the vertex and centers the display on it"
				+ "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
				+ "</ul>"
				+ "<h3>Transforming Mode:</h3>"
				+ "<ul>"
				+ "<li>Mouse1+drag pans the graph"
				+ "<li>Mouse1+Shift+drag rotates the graph"
				+ "<li>Mouse1+CTRL(or Command)+drag shears the graph"
				+ "<li>Mouse1 double-click on a vertex or edge allows you to edit the label"
				+ "</ul>"
				+ "<h3>Annotation Mode:</h3>"
				+ "<ul>"
				+ "<li>Mouse1 begins drawing of a Rectangle"
				+ "<li>Mouse1+drag defines the Rectangle shape"
				+ "<li>Mouse1 release adds the Rectangle as an annotation"
				+ "<li>Mouse1+Shift begins drawing of an Ellipse"
				+ "<li>Mouse1+Shift+drag defines the Ellipse shape"
				+ "<li>Mouse1+Shift release adds the Ellipse as an annotation"
				+ "<li>Mouse3 shows a popup to input text, which will become"
				+ "<li>a text annotation on the graph at the mouse location"
				+ "</ul>"
				+ "</html>";

		@SuppressWarnings("javadoc")
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
	@SuppressWarnings("javadoc")
	private class OpenFileListener implements ActionListener {

		public OpenFileListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = new JFileChooser(CSTNEditor.defaultDir);
			final int option = chooser.showOpenDialog(CSTNEditor.this);
			final JEditorPane jl = CSTNEditor.this.viewerMessageArea;
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				CSTNEditor.defaultDir = file.getParent();
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
	@SuppressWarnings("javadoc")
	private class SaveFileListener implements ActionListener {

		public SaveFileListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JFileChooser chooser = new JFileChooser(CSTNEditor.defaultDir);
			// CSTNEditor.LOG.finest("Path wanted:" + path);
			final int option = chooser.showSaveDialog(CSTNEditor.this);
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				CSTNEditor.defaultDir = file.getParent();
				CSTNEditor.this.saveGraphToFile(CSTNEditor.this.inputGraph, file);
			}
		}

	}

	/**
	 * @author posenato
	 */
	@SuppressWarnings("javadoc")
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
	@SuppressWarnings("javadoc")
	private class BigViewerListener implements ActionListener {

		boolean isInputGraphLayoutToShow;

		public BigViewerListener(boolean isInputGraphLayoutToShow) {
			this.isInputGraphLayoutToShow = isInputGraphLayoutToShow;
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			JDialog frame = new JDialog(CSTNEditor.this,
					(this.isInputGraphLayoutToShow) ? CSTNEditor.inputGraphBigViewerName : CSTNEditor.derivedGraphBigViewerName);
			frame.setBounds(CSTNEditor.this.getBounds());
			Dimension bvvDim = new Dimension(frame.getWidth(), frame.getHeight() - 100);
			VisualizationViewer<LabeledNode, LabeledIntEdge> bvv = new VisualizationViewer<>(
					(this.isInputGraphLayoutToShow) ? CSTNEditor.this.layoutEditor : CSTNEditor.this.layoutViewer, bvvDim);
			bvv.setName(CSTNEditor.inputGraphBigViewerName);
			buildRenderContext(bvv, true);
			((ModalGraphMouse) bvv.getGraphMouse()).setMode(ModalGraphMouse.Mode.TRANSFORMING);
			final JPanel rowForAppButtons = new JPanel();
			@SuppressWarnings("unchecked")
			final JComboBox<Mode> modeBox = ((EditingModalGraphMouse<LabeledNode, LabeledIntEdge>) bvv.getGraphMouse()).getModeComboBox();
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
			frame.add(new JLabel(getGraphLabelDescription(((LabeledIntGraph) CSTNEditor.this.layoutEditor.getGraph()))));
			frame.add(bvv);
			frame.add(rowForAppButtons);
			frame.setVisible(true);
			frame.validate();
		}
	}

	/**
	 * @author posenato
	 */
	private class LayoutListener implements ActionListener {

		/**
		 * Original StaticLayout
		 */
		AbstractLayout<LabeledNode, LabeledIntEdge> originalLayout;

		/**
		 * 
		 */
		@SuppressWarnings("unused")
		CSTNLayout<LabeledIntEdge> cstnLayout;

		/**
		 * 
		 */
		LayoutListener() {
			this.originalLayout = null;
			this.cstnLayout = null;
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

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			try {
				AbstractLayout<LabeledNode, LabeledIntEdge> nextLayout;
				if (CSTNEditor.this.layoutToggleButton.isSelected()) {
					this.originalLayout = CSTNEditor.this.layoutEditor;
					// if (this.cstnLayout == null) {
					nextLayout = new CSTNLayout<>(CSTNEditor.this.inputGraph, CSTNEditor.this.vvEditor.getSize());
					((CSTNLayout<LabeledIntEdge>) nextLayout).setInitialY(CSTNEditor.this.vvEditor.getSize().height / 2);
					((CSTNLayout<LabeledIntEdge>) nextLayout).setCurrentLayout(this.originalLayout);
					nextLayout.initialize();
					this.cstnLayout = (CSTNLayout<LabeledIntEdge>) nextLayout;
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
				// LayoutTransition<LabeledNode, LabeledIntEdge> lt = new LayoutTransition<>(CSTNEditor.this.vv1, CSTNEditor.this.layout1, nextLayout);
				// Animator animator = new Animator(lt);
				// animator.start();
				// ELSE one step transition
				CSTNEditor.this.vvEditor.setGraphLayout(nextLayout);
				CSTNEditor.this.vvEditor.repaint();
				// END iF
				CSTNEditor.this.layoutEditor = nextLayout;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Default load/save directory
	 */
	static String defaultDir = "/Dropbox/";

	/**
	 * Name of the editor panel
	 */
	public static final String editorName = "Editor";

	/**
	 * Name of the distance viewer panel
	 */
	public static final String distanceViewerName = "DistanceViewer";

	/**
	 * Name of the input graph big viewer
	 */
	public static final String inputGraphBigViewerName = "Input Graph Big Viewer";

	/**
	 * Name of the derived graph big viewer
	 */
	public static final String derivedGraphBigViewerName = "Derived Graph Big Viewer";

	/**
	 *
	 */
	static final URL infoIconFile = Class.class.getResource("/images/metal-info.png");

	/**
	 * Class for representing edge labeled values.
	 */
	final static Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;// LabeledIntHierarchyMap.class;

	/**
	 * class logger
	 */
	static Logger LOG = Logger.getLogger(CSTNEditor.class.getName());

	/**
	 * the preferred sizes for the two views
	 */
	static Dimension preferredSize = new Dimension(780, 768);

	/**
	 * Standard serial number
	 */
	private static final long serialVersionUID = 647420826043015775L;

	/**
	 * Version
	 */
	private static final String version = "Version  $Rev$";
	/**
	 *
	 */
	static final URL warnIconFile = Class.class.getResource("/images/metal-warning.png");

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		@SuppressWarnings("unused")
		CSTNEditor editor = new CSTNEditor();
	}

	/**
	 * LabeledIntGraph structures necessary to represent derived graph.
	 */
	final LabeledIntGraph checkedGraph;

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
	 * CSTNU check status
	 */
	CSTNUCheckStatus cstnuStatus;

	/**
	 * Number of cycles of CSTN(U) check step-by-step
	 */
	int cycle;

	/**
	 * The current wanted semantics
	 */
	public DCSemantics dcCurrentSem = DCSemantics.Std;

	/**
	 * Message area above the derived (no input) graph.
	 */
	JEditorPane viewerMessageArea;

	/**
	 * Edges to check in CSTN(U) check step-by-step
	 */
	CSTN.EdgesToCheck edgesToCheck;

	/**
	 * The epsilon panel
	 */
	JPanel epsilonPanel;

	/**
	 * The graph info label
	 */
	JLabel graphInfoLabel;

	/**
	 * LabeledIntGraph structures necessary to represent input graph.
	 */
	final LabeledIntGraph inputGraph;

	/**
	 * Layout for input graph.
	 */
	AbstractLayout<LabeledNode, LabeledIntEdge> layoutEditor;

	/**
	 * Layout for derived graph.
	 */
	AbstractLayout<LabeledNode, LabeledIntEdge> layoutViewer;

	/**
	 * 
	 */
	JLabel mapInfoLabel;

	/**
	 * Button for re-layout input graph
	 */
	JToggleButton layoutToggleButton;

	/**
	 * OnlyToZ says if the DC checking has to be made propagating constraints only to time-point Z
	 */
	boolean onlyToZ = true;

	/**
	 * with unkwown literal
	 */
	boolean withUknown = true;

	/**
	 * LabeledIntGraph structures necessary to represent an axuliary graph.
	 */
	LabeledIntGraph oneStepBackGraph;

	/**
	 * Reaction time for CSTN
	 */
	int reactionTime = 1;

	/**
	 * Result Save Button
	 */
	JButton saveCSTNResultButton;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for input graph.
	 */
	VisualizationViewer<LabeledNode, LabeledIntEdge> vvEditor;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for derived graph.
	 */
	VisualizationViewer<LabeledNode, LabeledIntEdge> vvViewer;

	/**
	 * Default constructor
	 */
	public CSTNEditor() {
		super("Simple CSTNU Editor. CSTN " + CSTN.VERSIONandDATE + ". CSTNU " + CSTNU.VERSIONandDATE);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle bounds = env.getMaximumWindowBounds();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Screen Bounds: " + bounds);
			}
		}
		CSTNEditor.preferredSize = new Dimension((bounds.width - 30) / 2, bounds.height - 260);

		this.inputGraph = new LabeledIntGraph(CSTNEditor.labeledIntValueMap);
		this.checkedGraph = new LabeledIntGraph(CSTNEditor.labeledIntValueMap);
		this.layoutEditor = new StaticLayout<>(this.inputGraph, CSTNEditor.preferredSize);
		this.layoutViewer = new StaticLayout<>(this.checkedGraph, CSTNEditor.preferredSize);
		this.vvEditor = new VisualizationViewer<>(this.layoutEditor, CSTNEditor.preferredSize);
		this.vvEditor.setName(CSTNEditor.editorName);
		this.vvViewer = new VisualizationViewer<>(this.layoutViewer, CSTNEditor.preferredSize);
		this.vvViewer.setName(CSTNEditor.distanceViewerName);

		buildRenderContext(this.vvEditor, true);
		buildRenderContext(this.vvViewer, false);

		// CONTENT
		// content is the canvas of the application.
		final Container contentPane = this.getContentPane();

		// NORTH
		// I put a row for messages: since there will 2 graphs, the row contains two columns,
		// corresponding to the two following graphs.
		JPanel messagePanel = new JPanel(new GridLayout(1, 2));
		// Info for first graph
		JPanel message1Graph = new JPanel();
		message1Graph.setBackground(new Color(253, 253, 253));
		this.graphInfoLabel = new JLabel("");
		message1Graph.add(this.graphInfoLabel);
		this.mapInfoLabel = new JLabel("");
		// this.mapInfoLabel.setSize(200, 18);
		message1Graph.add(this.mapInfoLabel);
		messagePanel.add(message1Graph);

		message1Graph = new JPanel(new GridLayout(1, 1));// even if in the second cell there is only one element, derivedGraphMessageArea, a JPanel is necessary
															// to have the same
		// padding of first cell.

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
		centralPanel.add(new GraphZoomScrollPane(this.vvEditor));// GraphZoomScroll is necessary to show border!
		centralPanel.add(new GraphZoomScrollPane(this.vvViewer));
		contentPane.add(centralPanel, BorderLayout.CENTER);

		// SOUTH
		final JPanel controls = new JPanel(new GridLayout(3, 1)), rowForAppButtons = new JPanel(), rowForCSTNButtons = new JPanel(),
				rowForCSTNUButtons = new JPanel();// , rowForCSTNPSUButtons = new JPanel();
		final ValidationPanel validationPanelRowForCSTNButtons = new ValidationPanel();
		final ValidationGroup validationGroupCSTN = validationPanelRowForCSTNButtons.getValidationGroup();
		validationPanelRowForCSTNButtons.setInnerComponent(rowForCSTNButtons);
		validationPanelRowForCSTNButtons.setBorder(BorderFactory.createLineBorder(getForeground(), 1));
		controls.add(rowForAppButtons, 0);// for tuning application
		controls.add(validationPanelRowForCSTNButtons, 1);// for button regarding CSTN
		controls.add(rowForCSTNUButtons, 2);// for button regarding CSTNU
		// controls.add(rowForCSTNPSUButtons, 3);// for button regarding CSTNPSU
		contentPane.add(controls, BorderLayout.SOUTH);

		JButton buttonCheck;

		// FIRST ROW OF COMMANDS
		this.layoutToggleButton = new JToggleButton("Layout input graph");
		this.layoutToggleButton.addActionListener(new LayoutListener());
		rowForAppButtons.add(this.layoutToggleButton);

		buttonCheck = new JButton("Input Graph big viewer");
		buttonCheck.addActionListener(new BigViewerListener(true));
		rowForAppButtons.add(buttonCheck);

		@SuppressWarnings("unchecked")
		final JComboBox<Mode> modeBox = ((EditingModalGraphMouse<LabeledNode, LabeledIntEdge>) this.vvEditor.getGraphMouse()).getModeComboBox();
		rowForAppButtons.add(modeBox);
		// AnnotationControls<LabeledNode,LabeledIntEdge> annotationControls =
		// new AnnotationControls<LabeledNode,LabeledIntEdge>(gm.getAnnotatingPlugin());
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

		// SECOND ROW OF COMMANDS

		// rowForCSTNButtons.add(new JLabel("Labeled value mode"));
		// @SuppressWarnings("rawtypes")
		// JComboBox<Class> jcb = new JComboBox<>(new Class[] { LabeledIntTreeMap.class, LabeledIntHierarchyMap.class});

		// RWO FOR CSTNs
		
		JCheckBox withUnkwon = new JCheckBox("With unknown literals");
		withUnkwon.setSelected(this.withUknown);
		withUnkwon.addItemListener(new 	ItemListener() {
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

		buttonCheck = new JButton("CSTN Init Graph");
		buttonCheck.addActionListener(new CSTNInitListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN 6-Rule Check");
		buttonCheck.addActionListener(new CSTNCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new CSTNOneStepListener());
		rowForCSTNButtons.add(buttonCheck);

		this.saveCSTNResultButton = new JButton("Save CSTN");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new CSTNSaveListener());
		rowForCSTNButtons.add(this.saveCSTNResultButton);

		buttonCheck = new JButton("CSTN 3-Rule Check");
		buttonCheck.addActionListener(new CSTNRestrictedCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN All-Pair Shortest Paths");
		buttonCheck.addActionListener(new CSTNAPSPListener());
		rowForCSTNButtons.add(buttonCheck);

		// ROW FOR CSTNU
		JCheckBox onlyToZCB = new JCheckBox("Propagate only to Z");
		onlyToZCB.setSelected(this.onlyToZ);
		onlyToZCB.addItemListener(new OnlyToZListener());
		rowForCSTNUButtons.add(onlyToZCB);

		buttonCheck = new JButton("CSTNU Init Graph");
		buttonCheck.addActionListener(new CSTNUInitListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new CSTNUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new CSTNUOneStepListener());
		rowForCSTNUButtons.add(buttonCheck);

		// buttonCheck = new JButton("CSTNU Check by Potential");
		// buttonCheck.addActionListener(new CSTNUPotentialCheckListener());
		// rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNPSU Check");
		buttonCheck.addActionListener(new CSTNPSUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		// MENU
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

		final JFileChooser chooser = new JFileChooser();
		CSTNEditor.defaultDir = chooser.getCurrentDirectory() + CSTNEditor.defaultDir;

		this.pack();
		this.setVisible(true);
	}

	/**
	 * sets up vertex and edges renders
	 * 
	 * @param viewer
	 * @param firstViewer
	 */
	public static void setNodeEdgeRenders(BasicVisualizationServer<LabeledNode, LabeledIntEdge> viewer, boolean firstViewer) {
		RenderContext<LabeledNode, LabeledIntEdge> renderCon = viewer.getRenderContext();
		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		viewer.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		renderCon.setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);

		// EDGE setting
		renderCon.setEdgeDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		renderCon.setEdgeLabelTransformer(AbstractLabeledIntEdge.edgeLabelFunction);
		renderCon.setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		renderCon.setEdgeStrokeTransformer(AbstractLabeledIntEdge.edgeStrokeTransformer);
		renderCon.setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<LabeledNode, LabeledIntEdge>(0.65, 0.5));
		renderCon.setArrowDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		renderCon.setArrowFillPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(viewer.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		if (firstViewer) {
			renderCon.setEdgeFontTransformer(AbstractLabeledIntEdge.edgeFontFunction);
		}
		renderCon.setLabelOffset((firstViewer) ? 6 : 3);
	}

	/**
	 * Adds vertex and edges renders, tooltips and mouse behaviour to a viewer.
	 * 
	 * @param viewer
	 * @param firstViewer
	 */
	void buildRenderContext(VisualizationViewer<LabeledNode, LabeledIntEdge> viewer, boolean firstViewer) {
		RenderContext<LabeledNode, LabeledIntEdge> renderCon = viewer.getRenderContext();

		// vertex and edge renders
		setNodeEdgeRenders(viewer, firstViewer);

		// TOOLTIPS setting
		viewer.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		Supplier<LabeledIntEdge> edgeFactory = new LabeledIntEdgeSupplier<>(CSTNEditor.labeledIntValueMap);
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> graphMouse = new EditingModalGraphMouse<>(renderCon, LabeledNode.getFactory(),
				edgeFactory,
				CSTNEditor.this);
		graphMouse.setMode(ModalGraphMouse.Mode.PICKING);
		viewer.setGraphMouse(graphMouse);
		viewer.addKeyListener(graphMouse.getModeKeyListener());
	}

	/**
	 * Load graph stored in file 'fileName' into attribute this.g. Moreover, it create this.layout1 using this.g.
	 * 
	 * @param fileName
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	void loadGraphG(final File fileName) throws IOException, ParserConfigurationException, SAXException {
		final CSTNUGraphMLReader graphReader = new CSTNUGraphMLReader(fileName, CSTNEditor.labeledIntValueMap);
		CSTNEditor.this.inputGraph.takeIn(graphReader.readGraph());
		CSTNEditor.this.inputGraph.setFileName(fileName);
	}

	/**
	 * @param graphToSave graph to save
	 * @param file
	 */
	void saveGraphToFile(final LabeledIntGraph graphToSave, final File file) {
		final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(this.layoutEditor);
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

	/**
	 * @param g
	 * @return a string describing essential characteristics of the graph.
	 */
	public static String getGraphLabelDescription(LabeledIntGraph g) {
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
	 * 
	 */
	public void resetDerivedGraphStatus() {
		this.vvViewer.setVisible(false);
		this.viewerMessageArea.setText("");
		this.cycle = 0;
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
