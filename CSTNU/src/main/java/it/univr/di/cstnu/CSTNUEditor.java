/**
 * 
 */
package it.univr.di.cstnu;

import it.univr.di.cstnu.CSTN.CheckStatus;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.EditingModalGraphMouse;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

/**
 * Simple facade of Jung graph in order to guarantee the CSTPU semantics.
 * 
 * @author posenato
 */
public class CSTNUEditor extends JFrame implements Cloneable {

	/**
	 * Standard serial number
	 */
	private static final long serialVersionUID = 647420826043015774L;
	/**
	 * class logger
	 */
	static Logger LOG = Logger.getLogger(CSTNUEditor.class.getName());

	/**
	 * the preferred sizes for the two views
	 */
	static final Dimension preferredSize = new Dimension(780, 768);

	/**
	 * Name of the distance viewer
	 */
	public static final String distanceViewerName = "DistanceViewer";

	/**
	 * 
	 */
	static final ImageIcon infoIcon = new ImageIcon("images/metal-info.png");

	/**
	 * 
	 */
	static final ImageIcon warnIcon = new ImageIcon("images/metal-warning.png");

	/**
	 * Default load/save directory
	 */
	static String defaultDir = "/Dropbox/_CSTNU";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		@SuppressWarnings("unused")
		final CSTNUEditor cSTNUEditor = new CSTNUEditor();
	}

	/**
	 * LabeledIntGraph structures necessary to represent all graphs.
	 */
	@SuppressWarnings("javadoc")
	LabeledIntGraph g, g1, g2, distanceGraph;

	/**
	 * Layout algorithm for graph.
	 */
	@SuppressWarnings("javadoc")
	StaticLayout<LabeledNode, LabeledIntEdge> layout1, layout2;

	/**
	 * The model for the viewer. Useful if there are more than one Viewer.
	 */
	@SuppressWarnings("javadoc")
	VisualizationModel<LabeledNode, LabeledIntEdge> vm1, vm2;

	/**
	 * The BasicVisualizationServer<V,E> is parameterized by the edge types
	 */
	@SuppressWarnings("javadoc")
	VisualizationViewer<LabeledNode, LabeledIntEdge> vv1, vv2;

	/**
	 * The second panel
	 */
	JPanel messagesPanel;

	/**
	 * The graph panel
	 */
	JPanel graphPanel;

	/**
	 * To count the number of cycle of CSTNU check step-by-step
	 */
	int cycle;

	/**
	 * To activate instantaneous activation
	 */
	boolean instantaneousReaction = true;

	/**
	 * Flag to activate optimization of labeled values.
	 */
	boolean labelOptimization = true;

	
	/**
	 * Flag for applying R0 and R3 only on Z node.
	 */
	boolean onlyOnZ = false;
	
	/**
	 * Flag for excluding the application of R1 and R2 rules.
	 */
	boolean excludeR1R2 = true;
	/**
	 * CSTNU save button
	 */
	JButton saveCSTNResultButton;

	// /**
	// *
	// */
	// private final AbstractAction allMaxProjection = new AbstractAction("AllMax Projection") {
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// final JLabel jl = (JLabel) messagesPanel
	// .getComponent(1);
	// g1 = new LabeledIntGraph(g);
	// try {
	// CSTNU.initUpperLowerLabelDataStructure(g1);
	// }
	// catch (IllegalArgumentException ec) {
	// jl.setIcon(CSTNUEditor.warnIcon);
	// jl.setOpaque(true);
	// jl.setBackground(Color.orange);
	// jl.setText("The graph has a problem and it cannot be initialize: " + ec.getMessage());
	// graphPanel.validate();
	// graphPanel.repaint();
	// return;
	// }
	// g1 = CSTNU.makeAllMaxProjection(g1);
	// jl.setText("The All Max Projection");
	// jl.setIcon(CSTNUEditor.warnIcon);
	// jl.setOpaque(true);
	// jl.setBackground(Color.orange);
	// layout2 = new StaticLayout<>(g1);
	// LabeledNode gV;
	// CSTNUEditor.LOG.finer("Original graph: " + g);
	// CSTNUEditor.LOG.finer("Distance graph: " + g1);
	// for (final LabeledNode v : g1.getVertices()) {
	// // LOG.finest("Vertex of distance graph: " + v);
	// gV = g.getNode(v.getName());
	// // LOG.finest("Vertex of original graph: " + gV);
	// // LOG.finest("Original position (" +
	// // layout1.getX(gV) + ";" + layout1.getY(gV) + ")");
	// layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
	// }
	// vv2.setGraphLayout(layout2);
	// vv2.setVisible(true);
	// saveCSTNUButton.setVisible(true);
	//
	// graphPanel.validate();
	// graphPanel.repaint();
	// cycle = 0;
	// }
	// };

	// /**
	// *
	// */
	// private final AbstractAction minAllMaxProjection = new AbstractAction("Min AllMax Projection") {
	// /**
	// *
	// */
	// private static final long serialVersionUID = 1L;
	//
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// final JLabel jl = (JLabel) messagesPanel
	// .getComponent(1);
	// g1 = new LabeledIntGraph(g);
	// if (!CSTNU.initUpperLowerLabelDataStructure(g1)) {
	// jl.setIcon(CSTNUEditor.warnIcon);
	// jl.setOpaque(true);
	// jl.setBackground(Color.orange);
	// jl.setText("The graph has a problem and it cannot be initialize: or it is null or it has a pair of nodes with labels inconsistent.");
	// graphPanel.validate();
	// graphPanel.repaint();
	// return;
	// }
	// g1 = CSTNU.makeAllMaxProjection(g1);
	//
	// if (!CSTNU.malDistanceGraphFast(g1)) {
	// // The distance graph is not consistent
	// jl.setText("The minimal all max projection is inconsistent.");
	// jl.setIcon(CSTNUEditor.warnIcon);
	// } else {
	// jl.setText("The minimal all max projection.");
	// jl.setIcon(CSTNUEditor.infoIcon);
	// }
	//
	// jl.setOpaque(true);
	// jl.setBackground(Color.orange);
	// layout2 = new StaticLayout<>(g1);
	// LabeledNode gV;
	// CSTNUEditor.LOG.finer("Original graph: " + g);
	// CSTNUEditor.LOG.finer("Distance graph: " + g1);
	// for (final LabeledNode v : g1.getVertices()) {
	// CSTNUEditor.LOG.finest("Vertex of all max graph: " + v);
	// gV = g.getNode(v.getName());
	// CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
	// CSTNUEditor.LOG.finest("Original position (" + layout1.getX(gV) + ";" + layout1.getY(gV) + ")");
	// layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
	// }
	// vv2.setGraphLayout(layout2);
	// vv2.setVisible(true);
	// saveCSTNUButton.setVisible(true);
	//
	// graphPanel.validate();
	// graphPanel.repaint();
	// cycle = 0;
	// }
	// };

	/**
	 * Default constructor
	 */
	public CSTNUEditor() {
		super("Simple CSTNU Editor");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		g = new LabeledIntGraph(labelOptimization);
		g1 = new LabeledIntGraph(labelOptimization);
		layout1 = new StaticLayout<>(g, CSTNUEditor.preferredSize);
		layout2 = new StaticLayout<>(g1, CSTNUEditor.preferredSize);
		vm1 = new DefaultVisualizationModel<>(layout1, CSTNUEditor.preferredSize);
		vm2 = new DefaultVisualizationModel<>(layout2, CSTNUEditor.preferredSize);
		vv1 = new VisualizationViewer<>(vm1, CSTNUEditor.preferredSize);
		vv1.setName("Editor");
		vv2 = new VisualizationViewer<>(vm2, CSTNUEditor.preferredSize);
		vv2.setName(distanceViewerName);

		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv1.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv1.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		vv1.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		vv2.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv2.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		vv2.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		// EDGE setting
		vv1.getRenderContext().setEdgeLabelTransformer(LabeledIntEdge.edgeLabelTransformer);
		vv1.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		vv1.getRenderContext().setEdgeStrokeTransformer(LabeledIntEdge.edgeStrokeTransformer);
		vv1.getRenderContext().setEdgeDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		vv1.getRenderContext().setArrowDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		vv1.getRenderContext().setArrowFillPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		vv2.getRenderContext().setEdgeLabelTransformer(LabeledIntEdge.edgeLabelTransformer);
		vv2.getRenderContext().setEdgeDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		vv2.getRenderContext().setEdgeStrokeTransformer(LabeledIntEdge.edgeStrokeTransformer);
		vv2.getRenderContext().setArrowDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		vv2.getRenderContext().setArrowFillPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm = new EditingModalGraphMouse<>(vv1.getRenderContext(), LabeledNode.getFactory(),
				LabeledIntEdge.getFactory());
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv1.setGraphMouse(gm);
		vv1.addKeyListener(gm.getModeKeyListener());
		// EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm2 = new EditingModalGraphMouse<LabeledNode, LabeledIntEdge>(vv2.getRenderContext(),
		// LabeledNode.getFactory(), LabeledIntEdge.getFactory());
		// gm2.setMode(ModalGraphMouse.Mode.PICKING);
		vv2.setGraphMouse(gm);
		vv2.addKeyListener(gm.getModeKeyListener());

		// content is the canvas of the application.
		final Container baseContainer = getContentPane();

		// I put a row for messages: since there will 2 graphs, the row contains two columns,
		// corresponding to the two following graphs.
		messagesPanel = new JPanel(new GridLayout(1, 2));
		messagesPanel.add(new JLabel(" "));
		messagesPanel.add(new JLabel(" "));

		baseContainer.add(messagesPanel, BorderLayout.NORTH);

		// I put a row for graphs.
		graphPanel = new JPanel(new GridLayout(1, 2));
		graphPanel.add(new GraphZoomScrollPane(vv1));
		GraphZoomScrollPane graphZoomSP = new GraphZoomScrollPane(vv2);
		graphPanel.add(graphZoomSP, 1);

		baseContainer.add(graphPanel, BorderLayout.CENTER);

		// I put a row for commands buttons

		final JPanel controls = new JPanel(new BorderLayout()), rowForAppButtons = new JPanel(), rowForCSTNButtons = new JPanel(), rowForCSTNUButtons = new JPanel();
		controls.add(rowForAppButtons, BorderLayout.NORTH);// for tuning application
		controls.add(rowForCSTNButtons, BorderLayout.CENTER);// for button regarding CSTN
		controls.add(rowForCSTNUButtons, BorderLayout.SOUTH);// for button regarding CSTNU
		baseContainer.add(controls, BorderLayout.SOUTH);

		JButton buttonCheck;
		// FIRST ROW OF COMMANDS

		@SuppressWarnings("rawtypes")
		final JComboBox modeBox = gm.getModeComboBox();
		rowForAppButtons.add(modeBox);
		// AnnotationControls<LabeledNode,LabeledIntEdge> annotationControls =
		// new AnnotationControls<LabeledNode,LabeledIntEdge>(gm.getAnnotatingPlugin());
		// controls.add(annotationControls.getAnnotationsToolBar());

		JRadioButton instantaneousAct = new JRadioButton("Instant. Reaction", instantaneousReaction);
		instantaneousAct.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					instantaneousReaction = true;
					LOG.fine("Instantaneous activations flag set to true");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					instantaneousReaction = false;
					LOG.fine("Instantaneous activations flag set to false");
				}
			}
		});
		rowForAppButtons.add(instantaneousAct);

		JRadioButton excludeR1R2Button = new JRadioButton("R1 and R2 rule disabled", excludeR1R2);
		excludeR1R2Button.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					excludeR1R2 = true;
					LOG.fine("excludeR1R2 flag set to true");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					excludeR1R2 = false;
					LOG.fine("excludeR1R2 flag set to false");
				}
			}
		});
		rowForAppButtons.add(excludeR1R2Button);

		JRadioButton onlyOnZButton = new JRadioButton("R0 and R3 applied only in Z", onlyOnZ);
		onlyOnZButton.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					onlyOnZ = true;
					LOG.fine("onlyOnZ flag set to true");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					onlyOnZ = false;
					LOG.fine("onlyOnZ flag set to false");
				}
			}
		});
		rowForAppButtons.add(onlyOnZButton);
		
		JRadioButton labelOptimizationRadio = new JRadioButton("Label minimization", labelOptimization);
		labelOptimizationRadio.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					labelOptimization = true;
					LOG.fine("LabelOptimization flag set to true");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					labelOptimization = false;
					LOG.fine("LabelOptimization flag set to false");
				}
			}
		});
		rowForAppButtons.add(labelOptimizationRadio);

		saveCSTNResultButton = new JButton("Save CSTN(U)");
		saveCSTNResultButton.setEnabled(false);
		saveCSTNResultButton.addActionListener(new AbstractAction("Save result") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				// CSTNUEditor.LOG.finest("Current dir:" + f);

				// CSTNUEditor.LOG.finest("Path wanted:" + path);
				chooser = new JFileChooser(defaultDir);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					saveXML(g1, file);
				}
			}
		});
		rowForAppButtons.add(saveCSTNResultButton);

		buttonCheck = new JButton("Help");
		buttonCheck.addActionListener(new AbstractAction("Help") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			/**
			 * 
			 */
			private static final String instructions = "<html>"
					+ "<h3>All Modes:</h3>"
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

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(vv1, instructions);
			}
		});
		rowForAppButtons.add(buttonCheck);

		// SECOND ROW OF COMMANDS
		buttonCheck = new JButton("CSTN Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTN Init LabeledIntGraph") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				g1 = new LabeledIntGraph(g, labelOptimization);
				CSTN cstn = new CSTN(labelOptimization);

				try {
					cstn.initAndCheckCSTN(g1);
				}
				catch (IllegalArgumentException ec) {
					jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
					jl.setText("The graph has a problem and it cannot be initialize: " + ec.getMessage());
					graphPanel.validate();
					graphPanel.repaint();
					return;
				}
				jl.setText("CSTN initialized.");
				jl.setIcon(CSTNUEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				LabeledNode gV;
				CSTNUEditor.LOG.finer("Original graph: " + g);
				CSTNUEditor.LOG.finer("Complete graph: " + g1);
				for (final LabeledNode v : g1.getVertices()) {
					CSTNUEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = g.getNode(v.getName());
					CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
					CSTNUEditor.LOG.finest("Original position (" + layout1.getX(gV) + ";" + layout1.getY(gV) + ")");
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("CSTN") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				saveCSTNResultButton.setEnabled(false);
				g1 = new LabeledIntGraph(g, labelOptimization);
				CSTN cstn = new CSTN(labelOptimization);
				try {
					cstn.initAndCheckCSTN(g1);
				}
				catch (IllegalArgumentException ec) {
					throw new IllegalArgumentException("The graph has a problem and it cannot be initialize:\n " + ec.getMessage());
				}
				CSTNUEditor.LOG.finer("Original graph initialized: " + g1);

				try {
					CheckStatus status;
					status = cstn.dynamicConsistencyCheck(g1, instantaneousReaction, onlyOnZ, excludeR1R2);
					if (status.consistency) {
						jl.setText("The graph is CSTN consistent.");
						jl.setIcon(CSTNUEditor.infoIcon);
						CSTNUEditor.LOG.finer("Final controllable graph: " + g1);
					} else {
						// The distance graph is not consistent
						jl.setText("The graph is not CSTN consistent.");
						jl.setIcon(CSTNUEditor.warnIcon);
					}
				}
				catch (WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				LabeledNode gV;
				for (final LabeledNode v : g1.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				vv2.validate();
				vv2.repaint();

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTN") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				CSTN cstn = new CSTN(labelOptimization);
				if (cycle == -1) return;
				if (cycle == 0) {
					g1 = new LabeledIntGraph(g, labelOptimization);
					cstn.initAndCheckCSTN(g1);
					g2 = new LabeledIntGraph(g1, labelOptimization);
					distanceGraph = new LabeledIntGraph(labelOptimization);
				} else {
					g1 = new LabeledIntGraph(g2, labelOptimization);
				}
				cycle++;

				try {
					final CheckStatus status = cstn.oneStepDynamicConsistency(g1, g2, instantaneousReaction,  onlyOnZ, excludeR1R2, new CheckStatus());
					final boolean reductionsApplied = !status.finished;
					final boolean inconsistency = !status.consistency;
					if (inconsistency) {
						jl.setText("The graph is inconsistent.");
						jl.setIcon(CSTNUEditor.warnIcon);
						cycle = -1;
						CSTNUEditor.LOG.info("INCONSISTENT GRAPH: " + g2);
					} else if (reductionsApplied) {
						jl.setText("Step " + cycle + " of consistency check is done.");
						jl.setIcon(CSTNUEditor.warnIcon);
					} else {
						jl.setText("The graph is CSTN consistent. The number of executed cycles is " + cycle);
						jl.setIcon(CSTNUEditor.infoIcon);
						cycle = -1;
						CSTNUEditor.LOG.info("CONSISTENT GRAPH: " + g2);
					}
				}
				catch (WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					jl.setIcon(CSTNUEditor.warnIcon);
					cycle = -1;
				}

				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g2);
				LabeledNode gV;
				for (final LabeledNode v : g2.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				graphPanel.validate();
				graphPanel.repaint();
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		// THIRD row
		buttonCheck = new JButton("CSTNU Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTNU Init LabeledIntGraph") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				g1 = new LabeledIntGraph(g, true);

				try {
					CSTNU.initUpperLowerLabelDataStructure(g1);
				}
				catch (IllegalArgumentException ec) {
					jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
					jl.setText("The graph has a problem and it cannot be initialize: " + ec.getMessage());
					graphPanel.validate();
					graphPanel.repaint();
					return;
				}
				jl.setText("LabeledIntGraph with Lower and Upper Case Labels.");
				jl.setIcon(CSTNUEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				LabeledNode gV;
				CSTNUEditor.LOG.finer("Original graph: " + g);
				CSTNUEditor.LOG.finer("Complete graph: " + g1);
				for (final LabeledNode v : g1.getVertices()) {
					CSTNUEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = g.getNode(v.getName());
					CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
					CSTNUEditor.LOG.finest("Original position (" + layout1.getX(gV) + ";" + layout1.getY(gV) + ")");
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

//		buttonCheck = new JButton("STNU Check");
//		buttonCheck.addActionListener(new AbstractAction("STNU") {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
//				g1 = new LabeledIntGraph(g, true);
//
//				if (CSTNU.stnuRules(g1)) {
//					jl.setText("The graph is stnu controllable.");
//					jl.setIcon(CSTNUEditor.infoIcon);
//				} else {
//					// The distance graph is not consistent
//					jl.setText("The graph is not stnu controllable.");
//					jl.setIcon(CSTNUEditor.warnIcon);
//				}
//				jl.setOpaque(true);
//				jl.setBackground(Color.orange);
//				layout2 = new StaticLayout<>(g1);
//				LabeledNode gV;
//				CSTNUEditor.LOG.finer("Original graph: " + g);
//				CSTNUEditor.LOG.finer("After graph: " + g1);
//				for (final LabeledNode v : g1.getVertices()) {
//					gV = g.getNode(v.getName());
//					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
//				}
//				vv2.setGraphLayout(layout2);
//				vv2.setVisible(true);
//				saveCSTNResultButton.setEnabled(true);
//
//				graphPanel.validate();
//				graphPanel.repaint();
//				cycle = 0;
//			}
//		});
//		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("CSTNU") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				saveCSTNResultButton.setEnabled(false);
				g1 = new LabeledIntGraph(g, true);
				try {
					CSTNU.initUpperLowerLabelDataStructure(g1);
				}
				catch (IllegalArgumentException ec) {
					throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + ec.getMessage());
				}
				CSTNUEditor.LOG.finer("Original graph initialized: " + g1);

				try {
					if (CSTNU.dynamicControllabilityCheck(g1, instantaneousReaction)) {
						jl.setText("The graph is CSTNU controllable.");
						jl.setIcon(CSTNUEditor.infoIcon);
						CSTNUEditor.LOG.finer("Final controllable graph: " + g1);
					} else {
						// The distance graph is not consistent
						jl.setText("The graph is not CSTNU controllable.");
						jl.setIcon(CSTNUEditor.warnIcon);
					}
				}
				catch (WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				LabeledNode gV;
				for (final LabeledNode v : g1.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				vv2.validate();
				vv2.repaint();

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTNU") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				if (cycle == -1) return;
				if (cycle == 0) {
					g1 = new LabeledIntGraph(g, true);
					CSTNU.initUpperLowerLabelDataStructure(g1);
					g2 = new LabeledIntGraph(g1, true);
					distanceGraph = new LabeledIntGraph(labelOptimization);
				} else {
					g1 = new LabeledIntGraph(g2, true);
				}
				cycle++;

				try {
					final Entry<Boolean, Boolean> status = CSTNU.oneStepDynamicControllability(cycle, g1, g2, distanceGraph, instantaneousReaction);
					final boolean reductionsApplied = status.getKey();
					final boolean inconsistency = status.getValue();
					if (inconsistency) {
						jl.setText("The graph is inconsistent.");
						jl.setIcon(CSTNUEditor.warnIcon);
						cycle = -1;
						CSTNUEditor.LOG.info("INCONSISTENT GRAPH: " + g2);
					} else if (reductionsApplied) {
						jl.setText("Step " + cycle + " of controllability check is done.");
						jl.setIcon(CSTNUEditor.warnIcon);
					} else {
						jl.setText("The graph is CSTNU controllable.");
						jl.setIcon(CSTNUEditor.infoIcon);
						cycle = -1;
						CSTNUEditor.LOG.info("CONTROLLABLE GRAPH: " + g2);
					}
				}
				catch (WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					jl.setIcon(CSTNUEditor.warnIcon);
					cycle = -1;
				}

				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g2);
				LabeledNode gV;
				for (final LabeledNode v : g2.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV),
							layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNResultButton.setEnabled(true);

				graphPanel.validate();
				graphPanel.repaint();
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("Translation to UPPAAL TIGA");
		buttonCheck.addActionListener(new AbstractAction("UPPAAL Tiga Tranlsation") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@SuppressWarnings("resource")
			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				PrintStream output;
				JFileChooser chooser = new JFileChooser(defaultDir);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					defaultDir = file.getParent();
					if (!file.getName().endsWith("xml")) {
						file = new File(file.getAbsolutePath() + ".xml");
					}
					try {
						output = new PrintStream(file);
					}
					catch (FileNotFoundException e1) {
						e1.printStackTrace();
						output = System.out;
					}
					CSTNU2UppaalTiga translator = null;

					jl.setOpaque(true);
					jl.setBackground(Color.orange);

					try {
						translator = new CSTNU2UppaalTiga(g, output);
						if (!translator.translate()) throw new IllegalArgumentException();
					}
					catch (IllegalArgumentException e1) {
						e1.printStackTrace();
						jl.setIcon(CSTNUEditor.warnIcon);
						jl.setText("The graph has a problem and it cannot be translated to an UPPAAL Tiga automaton.");
						graphPanel.repaint();
						graphPanel.validate();
						return;
					}
					finally {
						output.close();
					}
					jl.setText("The graph has been translated and saved into file '" + file.getName() + "'.");
					try {
						output = new PrintStream(file.getAbsolutePath().replace(".xml", ".q"));
					}
					catch (FileNotFoundException e1) {
						e1.printStackTrace();
						output = System.out;
					}
					output.println("control: A[] not _processMain.goal");

					jl.setIcon(CSTNUEditor.infoIcon);
					graphPanel.validate();
					graphPanel.repaint();
					output.close();
				}
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		final JMenu menu = new JMenu("File");
		menu.add(new AbstractAction("Open...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(defaultDir);
				final int option = chooser.showOpenDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					defaultDir = file.getParent();
					try {
						openXML(file);
						vv1.setGraphLayout(layout1);
						vv2.setVisible(false);
						saveCSTNResultButton.setEnabled(false);
						((JLabel) messagesPanel.getComponent(1)).setIcon(null);
						((JLabel) messagesPanel.getComponent(1)).setText("");
						((JLabel) messagesPanel.getComponent(1)).setOpaque(false);
						CSTNUEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + g.getName());
						CSTNUEditor.this.getRootPane().repaint();
						cycle = 0;
					}
					catch (final ClassNotFoundException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
		menu.add(new AbstractAction("Save...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser(defaultDir);
				// CSTNUEditor.LOG.finest("Path wanted:" + path);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					defaultDir = file.getParent();
					saveXML(g, file);
				}
			}
		});
		// JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		setJMenuBar(menuBar);

		JFileChooser chooser = new JFileChooser();
		defaultDir = chooser.getCurrentDirectory() + defaultDir;

		pack();
		setVisible(true);
	}

	/**
	 * @param fileName
	 * @throws ClassNotFoundException
	 */
	void openXML(File fileName) throws ClassNotFoundException {
		@SuppressWarnings("resource")
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileName);
			final GraphMLReader2<LabeledIntGraph, LabeledNode, LabeledIntEdge> graphReader = new GraphMLReader(fileReader);
			g = graphReader.readGraph();
			layout1 = new StaticLayout<>(g, StaticLayout.positionInitializer);
		}
		catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (final GraphIOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (fileReader != null) fileReader.close();
			}
			catch (Exception ee) {}
		}
	}

	/**
	 * The following elements are saved in the order specified:<br>
	 * 1. LabeledIntGraph g<br>
	 * For each node i:<br>
	 * i: LabeledNode i <Br>
	 * i+1: Point2D position
	 * 
	 * @param g graph to save
	 * @param file
	 */
	void saveXML(LabeledIntGraph g, File file) {
		final GraphMLWriter<LabeledNode, LabeledIntEdge> graphWriter = new it.univr.di.cstnu.graph.GraphMLWriter(layout1);
		g.setName(file.getName());
		try {
			@SuppressWarnings("resource")
			final Writer out = new BufferedWriter(new FileWriter(file));
			graphWriter.save(g, out);
		}
		catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		}
		catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
