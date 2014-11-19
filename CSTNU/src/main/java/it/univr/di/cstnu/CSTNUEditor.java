/**
 * 
 */
package it.univr.di.cstnu;

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
		// Literal a = new Literal('a'), b = new Literal('b');
		// Label A = new Label(a);
		// Label AB = new Label(b);
		// AB.add(b);
		// Label AnB = new Label(b.getTheNegate());
		// AnB.add(a);
		//
		// Node n1 = new Node("1", A);
		// n1.putLabeledValue(AnB, new Integer(100));
		// Node n2 = new Node("2", AB);
		// Node n3 = new Node("3", new Label(b));
		// Node n4 = new Node("4", AnB);
		// Node n5 = new Node("5", A.union(new Label(b)));
		//
		// Edge e1 = new Edge("e1", Edge.Type.derived, A, 1);
		// Edge e2 = new Edge("e2", new Label(b), 2);
		// Edge e3 = new Edge("e3", A, 2);
		// Edge e4 = new Edge("e4", A, 3);
		// Edge e5 = new Edge("e5", A, 3);
		//
		// graph.addVertex(n1);
		// graph.addEdge(e1, n1, n2);
		// graph.addEdge(e2, n2, n3);
		// graph.addEdge(e3, n2, n4);
		// graph.addEdge(e4, n4, n1);
		// graph.addEdge(e5, n1, n5);
		//
		// System.out.print("Il Grafo è: " + graph.toString());
		//
		// DijkstraShortestPath<Node, Edge> alg = new DijkstraShortestPath<Node, Edge>(graph,
		// Edge.getTransformer(AB));
		// List<Edge> l = alg.getPath(n4, n3);
		// System.out.println("\n\n\n\nThe shortest unweighted path from with label AB from " + n4 +
		// " to " + n3 + " is: "
		// + l.toString() + "\n");
	}

	/**
	 * Graph structures necessary to represent all graphs.
	 */
	@SuppressWarnings("javadoc")
	Graph g, g1, g2, distanceGraph;

	/**
	 * Layout algorithm for graph.
	 */
	@SuppressWarnings("javadoc")
	StaticLayout<Node, Edge> layout1, layout2;

	/**
	 * The model for the viewer. Useful if there are more than one Viewer.
	 */
	@SuppressWarnings("javadoc")
	VisualizationModel<Node, Edge> vm1, vm2;

	/**
	 * The BasicVisualizationServer<V,E> is parameterized by the edge types
	 */
	@SuppressWarnings("javadoc")
	VisualizationViewer<Node, Edge> vv1, vv2;

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
	 * CSTNU save button
	 */
	JButton saveCSTNUButton;

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
	// g1 = new Graph(g);
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
	// Node gV;
	// CSTNUEditor.LOG.finer("Original graph: " + g);
	// CSTNUEditor.LOG.finer("Distance graph: " + g1);
	// for (final Node v : g1.getVertices()) {
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
	// g1 = new Graph(g);
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
	// if (!CSTNU.minimalDistanceGraphFast(g1)) {
	// // The distance graph is not consistent
	// jl.setText("The minimal all max projection is inconsistent.");
	// jl.setIcon(CSTNUEditor.warnIcon);
	// } else {// FIXME
	// jl.setText("The minimal all max projection.");
	// jl.setIcon(CSTNUEditor.infoIcon);
	// }
	//
	// jl.setOpaque(true);
	// jl.setBackground(Color.orange);
	// layout2 = new StaticLayout<>(g1);
	// Node gV;
	// CSTNUEditor.LOG.finer("Original graph: " + g);
	// CSTNUEditor.LOG.finer("Distance graph: " + g1);
	// for (final Node v : g1.getVertices()) {
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

		g = new Graph();
		g1 = new Graph();
		layout1 = new StaticLayout<>(g, CSTNUEditor.preferredSize);
		layout2 = new StaticLayout<>(g1, CSTNUEditor.preferredSize);
		vm1 = new DefaultVisualizationModel<>(layout1, CSTNUEditor.preferredSize);
		vm2 = new DefaultVisualizationModel<>(layout2, CSTNUEditor.preferredSize);
		vv1 = new VisualizationViewer<>(vm1, CSTNUEditor.preferredSize);
		vv1.setName("Editor");
		vv2 = new VisualizationViewer<>(vm2, CSTNUEditor.preferredSize);
		vv2.setName(Constants.distanceViewerName);

		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv1.getRenderContext().setVertexLabelTransformer(Node.vertexLabelTransformer);
		vv1.setVertexToolTipTransformer(Node.vertexToolTipTransformer);
		vv1.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv2.getRenderContext().setVertexLabelTransformer(Node.vertexLabelTransformer);
		vv2.setVertexToolTipTransformer(Node.vertexToolTipTransformer);
		vv2.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);

		// EDGE setting
		vv1.getRenderContext().setEdgeLabelTransformer(Edge.edgeLabelTransformer);
		vv1.getRenderContext().setEdgeStrokeTransformer(Edge.edgeStrokeTransformer);
		vv1.getRenderContext().setEdgeDrawPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));
		vv1.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		vv1.getRenderContext().setArrowDrawPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));
		vv1.getRenderContext().setArrowFillPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));

		vv2.getRenderContext().setEdgeLabelTransformer(Edge.edgeLabelTransformer);
		vv2.getRenderContext().setEdgeStrokeTransformer(Edge.edgeStrokeTransformer);
		vv2.getRenderContext().setEdgeDrawPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));
		vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		vv2.getRenderContext().setArrowDrawPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));
		vv2.getRenderContext().setArrowFillPaintTransformer(
				Edge.edgeDrawPaintTransformer(vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange,
						Color.gray));

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		final EditingModalGraphMouse<Node, Edge> gm = new EditingModalGraphMouse<>(vv1.getRenderContext(),
				Node.getFactory(), Edge.getFactory());
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		vv1.setGraphMouse(gm);
		vv1.addKeyListener(gm.getModeKeyListener());
		// EditingModalGraphMouse<Node, Edge> gm2 = new EditingModalGraphMouse<Node, Edge>(vv2.getRenderContext(),
		// Node.getFactory(), Edge.getFactory());
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

		final JPanel controls = new JPanel(new BorderLayout()), controls1 = new JPanel(), controls2 = new JPanel();
		controls.add(controls1, BorderLayout.NORTH);
		controls.add(controls2, BorderLayout.CENTER);
		baseContainer.add(controls, BorderLayout.SOUTH);
		
		JButton buttonCheck;
		//FIRST ROW OF COMMANDS
		
		@SuppressWarnings("rawtypes")
		final JComboBox modeBox = gm.getModeComboBox();
		controls1.add(modeBox);
		// AnnotationControls<Node,Edge> annotationControls =
		// new AnnotationControls<Node,Edge>(gm.getAnnotatingPlugin());
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
		controls1.add(instantaneousAct);

//		JRadioButton newRuleR6Radio = new JRadioButton("New Rule R6", newRuleR6);
//		instantaneousAct.addItemListener(new ItemListener() {
//			public void itemStateChanged(ItemEvent ev) {
//				if (ev.getStateChange() == ItemEvent.SELECTED) {
//					newRuleR6 = true;
//					LOG.fine("Instantaneous activations flag set to true");
//				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
//					newRuleR6 = false;
//					LOG.fine("Instantaneous activations flag set to false");
//				}
//			}
//		});
//		controls1.add(newRuleR6Radio);

		
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
					+ "<li>Right-click on a Vertex for <b>Add Edge</b> menus <br>(if there are selected Vertices)"
					+ "<li>Right-click on an Edge for <b>Delete Edge</b> popup"
					+ "<li>Mousewheel scales with a crossover value of 1.0.<br>"
					+ "     - scales the graph layout when the combined scale is greater than 1<br>"
					+ "     - scales the graph view when the combined scale is less than 1"
					+ "</ul>"
					+ "<h3>Editing Mode:</h3>"
					+ "<ul>"
					+ "<li>Left-click an empty area to create a new Vertex"
					+ "<li>Left-click on a Vertex and drag to another Vertex to create an Undirected Edge"
					+ "<li>Shift+Left-click on a Vertex and drag to another Vertex to create a Directed Edge"
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
		controls1.add(buttonCheck);

		
		
		//SECOND ROW OF COMMANDS
		buttonCheck = new JButton("Init Graph");
		buttonCheck.addActionListener(new AbstractAction("Init Graph") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				g1 = new Graph(g);

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
				jl.setText("Graph with Lower and Upper Case Labels.");
				jl.setIcon(CSTNUEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				Node gV;
				CSTNUEditor.LOG.finer("Original graph: " + g);
				CSTNUEditor.LOG.finer("Complete graph: " + g1);
				for (final Node v : g1.getVertices()) {
					CSTNUEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = g.getNode(v.getName());
					CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
					CSTNUEditor.LOG.finest("Original position (" + layout1.getX(gV) + ";" + layout1.getY(gV) + ")");
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		controls2.add(buttonCheck);

		// buttonCheck = new JButton("All Max Projection");
		// buttonCheck.addActionListener(allMaxProjection);
		// controls.add(buttonCheck);


		buttonCheck = new JButton("CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("CSTN") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				saveCSTNUButton.setVisible(false);
				g1 = new Graph(g);
				try {
					CSTNU.initUpperLowerLabelDataStructure(g1);
				}
				catch (IllegalArgumentException ec) {
					throw new IllegalArgumentException("The graph has a problem and it cannot be initialize:\n " + ec.getMessage());
				}
				CSTNUEditor.LOG.finer("Original graph initialized: " + g1);

				try {
					if (CSTNU.dynamicConsistencyCheck(g1,instantaneousReaction)) {
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
				Node gV;
				for (final Node v : g1.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				vv2.validate();
				vv2.repaint();

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		controls2.add(buttonCheck);

		
		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTN") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				if (cycle == -1) return;
				if (cycle == 0) {
					g1 = new Graph(g);
					CSTNU.initUpperLowerLabelDataStructure(g1);
					g2 = new Graph(g1);
					distanceGraph = new Graph();
				} else {
					g1 = new Graph(g2);
				}
				cycle++;

				try {
					final Entry<Boolean, Boolean> status = CSTNU.oneStepDynamicConsistency(cycle, g1, g2, distanceGraph,instantaneousReaction);
					final boolean reductionsApplied = status.getKey();
					final boolean inconsistency = status.getValue();
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
				Node gV;
				for (final Node v : g2.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV),
							layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				graphPanel.validate();
				graphPanel.repaint();
			}
		});
		controls2.add(buttonCheck);


		
		buttonCheck = new JButton("STNU Check");
		buttonCheck.addActionListener(new AbstractAction("STNU") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				g1 = new Graph(g);

				if (CSTNU.stnuRules(g1)) {
					jl.setText("The graph is stnu controllable.");
					jl.setIcon(CSTNUEditor.infoIcon);
				} else {
					// The distance graph is not consistent
					jl.setText("The graph is not stnu controllable.");
					jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				layout2 = new StaticLayout<>(g1);
				Node gV;
				CSTNUEditor.LOG.finer("Original graph: " + g);
				CSTNUEditor.LOG.finer("After graph: " + g1);
				for (final Node v : g1.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		controls2.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("CSTNU") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				saveCSTNUButton.setVisible(false);
				g1 = new Graph(g);
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
				Node gV;
				for (final Node v : g1.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				vv2.validate();
				vv2.repaint();

				graphPanel.validate();
				graphPanel.repaint();
				cycle = 0;
			}
		});
		controls2.add(buttonCheck);

		saveCSTNUButton = new JButton("Save CSTNU");
		saveCSTNUButton.setVisible(false);
		saveCSTNUButton.addActionListener(new AbstractAction("Save result") {
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
		controls2.add(saveCSTNUButton);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTNU") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				final JLabel jl = (JLabel) messagesPanel.getComponent(1);
				if (cycle == -1) return;
				if (cycle == 0) {
					g1 = new Graph(g);
					CSTNU.initUpperLowerLabelDataStructure(g1);
					g2 = new Graph(g1);
					distanceGraph = new Graph();
				} else {
					g1 = new Graph(g2);
				}
				cycle++;

				try {
					final Entry<Boolean, Boolean> status = CSTNU.oneStepDynamicControllability(cycle, g1, g2, distanceGraph,instantaneousReaction);
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
				Node gV;
				for (final Node v : g2.getVertices()) {
					gV = g.getNode(v.getName());
					layout2.setLocation(v, layout1.getX(gV),
							layout1.getY(gV));
				}
				vv2.setGraphLayout(layout2);
				vv2.setVisible(true);
				saveCSTNUButton.setVisible(true);

				graphPanel.validate();
				graphPanel.repaint();
			}
		});
		controls2.add(buttonCheck);

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
		controls2.add(buttonCheck);


		

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
						saveCSTNUButton.setVisible(false);
						((JLabel) messagesPanel.getComponent(1)).setIcon(null);
						((JLabel) messagesPanel.getComponent(1)).setText("");
						((JLabel) messagesPanel.getComponent(1)).setOpaque(false);
						CSTNUEditor.this.setTitle("CSTNU Editor and Checker: " + g.getName());
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
			final GraphMLReader2<Graph, Node, Edge> graphReader = new GraphMLReader(fileReader);
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
	 * 1. Graph g<br>
	 * For each node i:<br>
	 * i: Node i <Br>
	 * i+1: Point2D position
	 * 
	 * @param g graph to save
	 * @param file
	 */
	void saveXML(Graph g, File file) {
		final GraphMLWriter<Node, Edge> graphWriter = new it.univr.di.cstnu.GraphMLWriter(layout1);
		if (g.getName() == null || g.getName().isEmpty()) g.setName(file.getName());
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
