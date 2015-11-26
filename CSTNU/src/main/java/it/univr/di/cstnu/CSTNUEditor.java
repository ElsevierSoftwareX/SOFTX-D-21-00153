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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.GraphMLWriter;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import it.univr.di.cstnu.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.EditingModalGraphMouse;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;
import it.univr.di.labeledvalue.Constants;

/**
 * Simple facade of Jung graph in order to guarantee the CSTPU semantics.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNUEditor extends JFrame implements Cloneable {

	/**
	 * Standard serial number
	 */
	private static final long serialVersionUID = 647420826043015775L;

	/**
	 * Version
	 */
	private static final String version = "Version  $Rev$";

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
	 * <p>
	 * main.
	 * </p>
	 *
	 * @param args
	 *                an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {

		new CSTNUEditor();
	}

	/**
	 * LabeledIntGraph structures necessary to represent all graphs.
	 */
	@SuppressWarnings("javadoc")
	LabeledIntGraph g, g1, g2, distanceGraph;

	/**
	 *
	 */
	CSTNCheckStatus status = null;
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
	 * The BasicVisualizationServer&lt;V,E&gt; is parameterized by the edge types
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
	 * Reaction time for CSTN
	 */
	int cstnReactionTime = 1;

	/**
	 * Reaction time for CSTN
	 */
	int cstnUReactionTime = 0;

	/**
	 * Flag to activate optimization of labeled values.
	 */
	boolean labelOptimization = true;

	/**
	 * Flag for excluding the application of R1 and R2 rules.
	boolean excludeR1R2 = true;
	 */

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
		super("Simple CSTNU Editor " + CSTNUEditor.version);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.g = new LabeledIntGraph(this.labelOptimization);
		this.g1 = new LabeledIntGraph(this.labelOptimization);
		this.layout1 = new StaticLayout<>(this.g, CSTNUEditor.preferredSize);
		this.layout2 = new StaticLayout<>(this.g1, CSTNUEditor.preferredSize);
		this.vm1 = new DefaultVisualizationModel<>(this.layout1, CSTNUEditor.preferredSize);
		this.vm2 = new DefaultVisualizationModel<>(this.layout2, CSTNUEditor.preferredSize);
		this.vv1 = new VisualizationViewer<>(this.vm1, CSTNUEditor.preferredSize);
		this.vv1.setName("Editor");
		// vv1.getRenderContext().setLabelOffset(20);
		this.vv2 = new VisualizationViewer<>(this.vm2, CSTNUEditor.preferredSize);
		this.vv2.setName(CSTNUEditor.distanceViewerName);

		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		this.vv1.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		this.vv1.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		this.vv1.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		this.vv2.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		this.vv2.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		this.vv2.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		// EDGE setting
		this.vv1.getRenderContext().setEdgeDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setEdgeFontTransformer(LabeledIntEdge.edgeFontTransformer);
		this.vv1.getRenderContext().setEdgeLabelTransformer(LabeledIntEdge.edgeLabelTransformer);
		this.vv1.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv1.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<LabeledNode, LabeledIntEdge>());
		this.vv1.getRenderContext().setEdgeStrokeTransformer(LabeledIntEdge.edgeStrokeTransformer);

		this.vv1.getRenderContext().setArrowDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setArrowFillPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		this.vv2.getRenderContext().setEdgeLabelTransformer(LabeledIntEdge.edgeLabelTransformer);
		this.vv2.getRenderContext().setEdgeDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv2.getRenderContext().setEdgeStrokeTransformer(LabeledIntEdge.edgeStrokeTransformer);
		this.vv2.getRenderContext().setArrowDrawPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setArrowFillPaintTransformer(
				LabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm = new EditingModalGraphMouse<>(this.vv1.getRenderContext(),
				LabeledNode.getFactory(),
				LabeledIntEdge.getFactory());
		gm.setMode(ModalGraphMouse.Mode.PICKING);
		this.vv1.setGraphMouse(gm);
		this.vv1.addKeyListener(gm.getModeKeyListener());
		// EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm2 = new EditingModalGraphMouse<LabeledNode, LabeledIntEdge>(vv2.getRenderContext(),
		// LabeledNode.getFactory(), LabeledIntEdge.getFactory());
		// gm2.setMode(ModalGraphMouse.Mode.PICKING);
		this.vv2.setGraphMouse(gm);
		this.vv2.addKeyListener(gm.getModeKeyListener());

		// content is the canvas of the application.
		final Container baseContainer = this.getContentPane();

		// I put a row for messages: since there will 2 graphs, the row contains two columns,
		// corresponding to the two following graphs.
		this.messagesPanel = new JPanel(new GridLayout(1, 1));
//		this.messagesPanel.add(new JLabel(" "));
		JTextArea jl = new JTextArea("");
		jl.setEditable(false);
		this.messagesPanel.add(jl);

		baseContainer.add(this.messagesPanel, BorderLayout.NORTH);

		// I put a row for graphs.
		this.graphPanel = new JPanel(new GridLayout(1, 2));
		this.graphPanel.add(new GraphZoomScrollPane(this.vv1));
		final GraphZoomScrollPane graphZoomSP = new GraphZoomScrollPane(this.vv2);
		this.graphPanel.add(graphZoomSP, 1);

		baseContainer.add(this.graphPanel, BorderLayout.CENTER);

		// I put a row for commands buttons

		final JPanel controls = new JPanel(new BorderLayout()), rowForAppButtons = new JPanel(), rowForCSTNButtons = new JPanel(),
				rowForCSTNUButtons = new JPanel();
		final ValidationPanel validationPanelRowForCSTNButtons = new ValidationPanel();
		final ValidationGroup validationGroupCSTN = validationPanelRowForCSTNButtons.getValidationGroup();
		validationPanelRowForCSTNButtons.setInnerComponent(rowForCSTNButtons);
		controls.add(rowForAppButtons, BorderLayout.NORTH);// for tuning application
		controls.add(validationPanelRowForCSTNButtons, BorderLayout.CENTER);// for button regarding CSTN
		controls.add(rowForCSTNUButtons, BorderLayout.SOUTH);// for button regarding CSTNU
		baseContainer.add(controls, BorderLayout.SOUTH);
		validationPanelRowForCSTNButtons.setBorder(BorderFactory.createLineBorder(getForeground(), 1));

		JButton buttonCheck;
		// FIRST ROW OF COMMANDS

		@SuppressWarnings("rawtypes")
		final JComboBox modeBox = gm.getModeComboBox();
		rowForAppButtons.add(modeBox);
		// AnnotationControls<LabeledNode,LabeledIntEdge> annotationControls =
		// new AnnotationControls<LabeledNode,LabeledIntEdge>(gm.getAnnotatingPlugin());
		// controls.add(annotationControls.getAnnotationsToolBar());

		// final JRadioButton excludeR1R2Button = new JRadioButton("R1 and R2 rule disabled", this.excludeR1R2);
		// excludeR1R2Button.addItemListener(new ItemListener() {
		// @Override
		// public void itemStateChanged(final ItemEvent ev) {
		// if (ev.getStateChange() == ItemEvent.SELECTED) {
		// CSTNUEditor.this.excludeR1R2 = true;
		// CSTNUEditor.LOG.fine("excludeR1R2 flag set to true");
		// } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
		// CSTNUEditor.this.excludeR1R2 = false;
		// CSTNUEditor.LOG.fine("excludeR1R2 flag set to false");
		// }
		// }
		// });
		// rowForAppButtons.add(excludeR1R2Button);

		final JRadioButton labelOptimizationRadio = new JRadioButton("Label minimization", this.labelOptimization);
		labelOptimizationRadio.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent ev) {
				if (ev.getStateChange() == ItemEvent.SELECTED) {
					CSTNUEditor.this.labelOptimization = true;
					CSTNUEditor.LOG.fine("LabelOptimization flag set to true");
				} else if (ev.getStateChange() == ItemEvent.DESELECTED) {
					CSTNUEditor.this.labelOptimization = false;
					CSTNUEditor.LOG.fine("LabelOptimization flag set to false");
				}
			}
		});
		rowForAppButtons.add(labelOptimizationRadio);

		this.saveCSTNResultButton = new JButton("Save CSTN(U)");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new AbstractAction("Save result") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				// CSTNUEditor.LOG.finest("Current dir:" + f);

				// CSTNUEditor.LOG.finest("Path wanted:" + path);
				chooser = new JFileChooser(CSTNUEditor.defaultDir);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNUEditor.this.saveXML(CSTNUEditor.this.g1, file);
				}
			}
		});
		rowForAppButtons.add(this.saveCSTNResultButton);

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
					+ "<h2>Simple CSTNU Editor " + CSTNUEditor.version + "</h2><h3>All Modes:</h3>"
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
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(CSTNUEditor.this.vv1, instructions);
			}
		});
		rowForAppButtons.add(buttonCheck);

		// SECOND ROW OF COMMANDS
		rowForCSTNButtons.add(new JLabel("System reacts "));

		final JFormattedTextField reactionTime = new JFormattedTextField();
		reactionTime.setValue(this.cstnReactionTime);
		reactionTime.setColumns(3);
		reactionTime.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				CSTNUEditor.LOG.finest("Property: " + evt.getPropertyName());
				CSTNUEditor.this.cstnReactionTime = ((Number) reactionTime.getValue()).intValue();
				CSTNUEditor.LOG.info("New reaction time: " + CSTNUEditor.this.cstnReactionTime);
			}
		});
		validationGroupCSTN.add(reactionTime, StringValidators.regexp(Constants.strictlyPositiveIntValueRE, "A positive and not 0 integer!", false));
		rowForCSTNButtons.add(reactionTime);

		rowForCSTNButtons.add(new JLabel("time units after (≥) an observation."));

		buttonCheck = new JButton("CSTN Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTN Init LabeledIntGraph") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);

				CSTN cstn = new CSTN(CSTNUEditor.this.labelOptimization, CSTNUEditor.this.cstnReactionTime);
				try {
					cstn.initAndCheck(CSTNUEditor.this.g1);
				} catch (final WellDefinitionException ec) {
					String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
					CSTNUEditor.LOG.warning(msg);
					jl.setText(msg);
//					jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
//					CSTNUEditor.this.vv2.validate();
//					CSTNUEditor.this.vv2.repaint();
					CSTNUEditor.this.graphPanel.validate();
					CSTNUEditor.this.graphPanel.repaint();
					return;
				}
				jl.setText("CSTN initialized.");
//				jl.setIcon(CSTNUEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g1);
				LabeledNode gV;
				CSTNUEditor.LOG.finer("Original graph: " + CSTNUEditor.this.g);
				CSTNUEditor.LOG.finer("Complete graph: " + CSTNUEditor.this.g1);
				for (final LabeledNode v : CSTNUEditor.this.g1.getVertices()) {
					CSTNUEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNUEditor.LOG.finest("Original position (" + CSTNUEditor.this.layout1.getX(gV) + ";"
								+ CSTNUEditor.this.layout1.getY(gV) + ")");
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
				CSTNUEditor.this.cycle = 0;
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("CSTN Check") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(false);
				CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);
				final CSTN cstn = new CSTN(CSTNUEditor.this.labelOptimization, CSTNUEditor.this.cstnReactionTime);
				jl.setBackground(Color.orange);
				try {
					CSTNCheckStatus status;
					status = cstn.dynamicConsistencyCheck(CSTNUEditor.this.g1);
					if (status.consistency) {
						jl.setText("The graph is CSTN consistent.");
						jl.setBackground(Color.green);
//						jl.setIcon(CSTNUEditor.infoIcon);
						CSTNUEditor.LOG.finer("Final controllable graph: " + CSTNUEditor.this.g1);
					} else {
						// The distance graph is not consistent
						jl.setText("The graph is not CSTN consistent.");
//						jl.setIcon(CSTNUEditor.warnIcon);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
//					jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl.setOpaque(true);
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g1);
				LabeledNode gV;
				for (final LabeledNode v : CSTNUEditor.this.g1.getVertices()) {
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNUEditor.LOG.finest("Original position (" + CSTNUEditor.this.layout1.getX(gV) + ";"
								+ CSTNUEditor.this.layout1.getY(gV) + ")");
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.vv2.validate();
				CSTNUEditor.this.vv2.repaint();

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
				CSTNUEditor.this.cycle = 0;
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTN") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				CSTN cstn = new CSTN(CSTNUEditor.this.labelOptimization, CSTNUEditor.this.cstnReactionTime);
				if (CSTNUEditor.this.cycle == -1)
					return;
				if (CSTNUEditor.this.cycle == 0) {
					CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);
					try {
						cstn.initAndCheck(CSTNUEditor.this.g1);
					} catch (final WellDefinitionException ex) {
						jl.setText("There is a problem in the graph: " + ex.getMessage());
//						jl.setIcon(CSTNUEditor.warnIcon);
						CSTNUEditor.this.cycle = -1;
						return;
					}
					CSTNUEditor.this.g2 = new LabeledIntGraph(CSTNUEditor.this.g1, CSTNUEditor.this.labelOptimization);
					CSTNUEditor.this.distanceGraph = new LabeledIntGraph(CSTNUEditor.this.labelOptimization);
					CSTNUEditor.this.status = new CSTNCheckStatus();
				} else {
					CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g2, CSTNUEditor.this.labelOptimization);
				}
				CSTNUEditor.this.cycle++;

				jl.setBackground(Color.orange);
				try {
					CSTNUEditor.this.status = cstn.oneStepDynamicConsistency(CSTNUEditor.this.g2, CSTNUEditor.this.status);
					CSTNUEditor.this.status.finished = CSTNUEditor.this.g1.hasSameEdgesOf(CSTNUEditor.this.g2);
					final boolean reductionsApplied = !CSTNUEditor.this.status.finished;
					final boolean inconsistency = !CSTNUEditor.this.status.consistency;
					if (inconsistency) {
						jl.setText("The graph is inconsistent.");
//						jl.setIcon(CSTNUEditor.warnIcon);
						CSTNUEditor.this.cycle = -1;
						CSTNUEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNUEditor.this.g2);
						CSTNUEditor.LOG.info("Status stats: " + CSTNUEditor.this.status);
					} else if (reductionsApplied) {
						jl.setText("Step " + CSTNUEditor.this.cycle + " of consistency check is done.");
//						jl.setIcon(CSTNUEditor.warnIcon);
					} else {
						jl.setText("The graph is CSTN consistent. The number of executed cycles is " + CSTNUEditor.this.cycle);
//						jl.setIcon(CSTNUEditor.infoIcon);
						CSTNUEditor.this.cycle = -1;
						jl.setBackground(Color.green);
						CSTNUEditor.LOG.info("Status stats: " + CSTNUEditor.this.status);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
//					jl.setIcon(CSTNUEditor.warnIcon);
					CSTNUEditor.this.cycle = -1;
					return;
				}

				jl.setOpaque(true);
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g2);
				LabeledNode gV;
				for (final LabeledNode v : CSTNUEditor.this.g2.getVertices()) {
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNUEditor.LOG.finest("Original position (" + CSTNUEditor.this.layout1.getX(gV) + ";"
								+ CSTNUEditor.this.layout1.getY(gV) + ")");
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTNU Init LabeledIntGraph") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);

				final CSTNU cstn = new CSTNU(CSTNUEditor.this.labelOptimization);
				try {
					cstn.initUpperLowerLabelDataStructure(CSTNUEditor.this.g1);
				} catch (final IllegalArgumentException ec) {
					String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
					CSTNUEditor.LOG.warning(msg);
					jl.setText(msg);
//					jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
//					CSTNUEditor.this.vv2.validate();
//					CSTNUEditor.this.vv2.repaint();
					CSTNUEditor.this.graphPanel.validate();
					CSTNUEditor.this.graphPanel.repaint();
					return;
				}
				jl.setText("LabeledIntGraph with Lower and Upper Case Labels.");
//				jl.setIcon(CSTNUEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g1);
				LabeledNode gV;
				CSTNUEditor.LOG.finer("Original graph: " + CSTNUEditor.this.g);
				CSTNUEditor.LOG.finer("Complete graph: " + CSTNUEditor.this.g1);
				for (final LabeledNode v : CSTNUEditor.this.g1.getVertices()) {
					CSTNUEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNUEditor.LOG.finest("Original position (" + CSTNUEditor.this.layout1.getX(gV) + ";"
								+ CSTNUEditor.this.layout1.getY(gV) + ")");
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
				CSTNUEditor.this.cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		// buttonCheck = new JButton("STNU Check");
		// buttonCheck.addActionListener(new AbstractAction("STNU") {
		// /**
		// *
		// */
		// private static final long serialVersionUID = 1L;
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// final JLabel jl = (JLabel) messagesPanel.getComponent(1);
		// g1 = new LabeledIntGraph(g, true);
		//
		// if (CSTNU.stnuRules(g1)) {
		// jl.setText("The graph is stnu controllable.");
		// jl.setIcon(CSTNUEditor.infoIcon);
		// } else {
		// // The distance graph is not consistent
		// jl.setText("The graph is not stnu controllable.");
		// jl.setIcon(CSTNUEditor.warnIcon);
		// }
		// jl.setOpaque(true);
		// jl.setBackground(Color.orange);
		// layout2 = new StaticLayout<>(g1);
		// LabeledNode gV;
		// CSTNUEditor.LOG.finer("Original graph: " + g);
		// CSTNUEditor.LOG.finer("After graph: " + g1);
		// for (final LabeledNode v : g1.getVertices()) {
		// gV = g.getNode(v.getName());
		// layout2.setLocation(v, layout1.getX(gV), layout1.getY(gV));
		// }
		// vv2.setGraphLayout(layout2);
		// vv2.setVisible(true);
		// saveCSTNResultButton.setEnabled(true);
		//
		// graphPanel.validate();
		// graphPanel.repaint();
		// cycle = 0;
		// }
		// });
		// rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("CSTNU") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
//				final JLabel jl = (JLabel) CSTNUEditor.this.messagesPanel.getComponent(1);
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(false);
				CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);
				final CSTNU cstnu = new CSTNU(CSTNUEditor.this.labelOptimization);
				CSTNUCheckStatus status = new CSTNUCheckStatus();
				try {
					cstnu.initUpperLowerLabelDataStructure(CSTNUEditor.this.g1);
				} catch (final IllegalStateException ec) {
					String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
					CSTNUEditor.LOG.warning(msg);
					jl.setText(msg);
//					jl.setIcon(CSTNUEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
//					CSTNUEditor.this.vv2.validate();
//					CSTNUEditor.this.vv2.repaint();
					CSTNUEditor.this.graphPanel.validate();
					CSTNUEditor.this.graphPanel.repaint();
					return;
				}
				CSTNUEditor.LOG.finer("Original graph initialized: " + CSTNUEditor.this.g1);

				jl.setBackground(Color.orange);
				try {
					status = cstnu.dynamicControllabilityCheck(CSTNUEditor.this.g1);
					if (status.controllable) {
						jl.setText("The graph is CSTNU controllable.");
//						jl.setIcon(CSTNUEditor.infoIcon);
						jl.setBackground(Color.green);
						CSTNUEditor.LOG.finer("Final controllable graph: " + CSTNUEditor.this.g1);
					} else {
						jl.setText("The graph is not CSTNU controllable.");
//						jl.setIcon(CSTNUEditor.warnIcon);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
//					jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl.setOpaque(true);
				
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g1);
				LabeledNode gV;
				for (final LabeledNode v : CSTNUEditor.this.g1.getVertices()) {
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.vv2.validate();
				CSTNUEditor.this.vv2.repaint();

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
				CSTNUEditor.this.cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTNU") {
			private static final long serialVersionUID = 1L;
			private CSTNUCheckStatus status = null;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				if (CSTNUEditor.this.cycle == -1)
					return;
				final CSTNU cstnu = new CSTNU(CSTNUEditor.this.labelOptimization);
				if (CSTNUEditor.this.cycle == 0) {
					status = new CSTNUCheckStatus();
					CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g, CSTNUEditor.this.labelOptimization);
					cstnu.initUpperLowerLabelDataStructure(CSTNUEditor.this.g1);
					CSTNUEditor.this.g2 = new LabeledIntGraph(CSTNUEditor.this.g1, CSTNUEditor.this.labelOptimization);
					CSTNUEditor.this.distanceGraph = new LabeledIntGraph(CSTNUEditor.this.labelOptimization);
				} else {
					CSTNUEditor.this.g1 = new LabeledIntGraph(CSTNUEditor.this.g2, CSTNUEditor.this.labelOptimization);
				}
				CSTNUEditor.this.cycle++;

				try {
					cstnu.oneStepDynamicControllability(CSTNUEditor.this.g1, CSTNUEditor.this.g2, CSTNUEditor.this.distanceGraph, status);
					if (status.finished && !status.controllable) {
						jl.setText("The graph is not controllable.");
//						jl.setIcon(CSTNUEditor.warnIcon);
						CSTNUEditor.this.cycle = -1;
						CSTNUEditor.LOG.info("Uncontrollable GRAPH: " + CSTNUEditor.this.g2);
					} else if (!status.finished) {
						jl.setText("Step " + CSTNUEditor.this.cycle + " of controllability check is done.");
//						jl.setIcon(CSTNUEditor.warnIcon);
					} else {
						jl.setText("The graph is CSTNU controllable. The number of executed cycles is " + CSTNUEditor.this.cycle);
//						jl.setIcon(CSTNUEditor.infoIcon);
						CSTNUEditor.this.cycle = -1;
						CSTNUEditor.LOG.info("CONTROLLABLE GRAPH: " + CSTNUEditor.this.g2);
						CSTNUEditor.LOG.info("Status stats: " + status);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
//					jl.setIcon(CSTNUEditor.warnIcon);
					CSTNUEditor.this.cycle = -1;
				}

				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				CSTNUEditor.this.layout2 = new StaticLayout<>(CSTNUEditor.this.g2);
				LabeledNode gV;
				for (final LabeledNode v : CSTNUEditor.this.g2.getVertices()) {
					gV = CSTNUEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNUEditor.this.layout2.setLocation(v, CSTNUEditor.this.layout1.getX(gV), CSTNUEditor.this.layout1.getY(gV));
					} else {
						CSTNUEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNUEditor.this.vv2.setGraphLayout(CSTNUEditor.this.layout2);
				CSTNUEditor.this.vv2.setVisible(true);
				CSTNUEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNUEditor.this.graphPanel.validate();
				CSTNUEditor.this.graphPanel.repaint();
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
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
				PrintStream output;
				final JFileChooser chooser = new JFileChooser(CSTNUEditor.defaultDir);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					CSTNUEditor.defaultDir = file.getParent();
					if (!file.getName().endsWith("xml")) {
						file = new File(file.getAbsolutePath() + ".xml");
					}
					try {
						output = new PrintStream(file);
					} catch (final FileNotFoundException e1) {
						e1.printStackTrace();
						output = System.out;
					}
					CSTNU2UppaalTiga translator = null;

					jl.setOpaque(true);
					jl.setBackground(Color.orange);

					try {
						translator = new CSTNU2UppaalTiga(CSTNUEditor.this.g, output);
						if (!translator.translate())
							throw new IllegalArgumentException();
					} catch (final IllegalArgumentException e1) {
						String msg = "The graph has a problem and it cannot be translated to an UPPAAL Tiga automaton:" + e1.getMessage();
						CSTNUEditor.LOG.warning(msg);
						jl.setText(msg);
//						jl.setIcon(CSTNUEditor.warnIcon);
						jl.setOpaque(true);
						jl.setBackground(Color.orange);
						CSTNUEditor.this.graphPanel.validate();
						CSTNUEditor.this.graphPanel.repaint();
						return;
					} finally {
						output.close();
					}
					jl.setText("The graph has been translated and saved into file '" + file.getName() + "'.");
					try {
						output = new PrintStream(file.getAbsolutePath().replace(".xml", ".q"));
					} catch (final FileNotFoundException e1) {
						e1.printStackTrace();
						output = System.out;
					}
					output.println("control: A[] not _processMain.goal");

//					jl.setIcon(CSTNUEditor.infoIcon);
					CSTNUEditor.this.graphPanel.validate();
					CSTNUEditor.this.graphPanel.repaint();
					output.close();
				}
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		final JMenu menu = new JMenu("File");
		menu.add(new AbstractAction("Open...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser chooser = new JFileChooser(CSTNUEditor.defaultDir);
				final int option = chooser.showOpenDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNUEditor.defaultDir = file.getParent();
					try {
						CSTNUEditor.this.openXML(file);
						CSTNUEditor.this.vv1.setGraphLayout(CSTNUEditor.this.layout1);
						CSTNUEditor.this.vv2.setVisible(false);
						CSTNUEditor.this.saveCSTNResultButton.setEnabled(false);
						final JTextArea jl = (JTextArea) CSTNUEditor.this.messagesPanel.getComponent(0);
//						((JTextArea) CSTNUEditor.this.messagesPanel.getComponent(1)).setIcon(null);
						jl.setText("");
						jl.setOpaque(false);
						CSTNUEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + CSTNUEditor.this.g.getName());
						CSTNUEditor.this.getRootPane().repaint();
						CSTNUEditor.this.cycle = 0;
					} catch (final ClassNotFoundException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
		menu.add(new AbstractAction("Save...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser chooser = new JFileChooser(CSTNUEditor.defaultDir);
				// CSTNUEditor.LOG.finest("Path wanted:" + path);
				final int option = chooser.showSaveDialog(CSTNUEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNUEditor.defaultDir = file.getParent();
					CSTNUEditor.this.saveXML(CSTNUEditor.this.g, file);
				}
			}
		});
		// JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		final JFileChooser chooser = new JFileChooser();
		CSTNUEditor.defaultDir = chooser.getCurrentDirectory() + CSTNUEditor.defaultDir;

		this.pack();
		this.setVisible(true);
	}

	/**
	 * @param fileName
	 * @throws ClassNotFoundException
	 */
	void openXML(final File fileName) throws ClassNotFoundException {
		@SuppressWarnings("resource")
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(fileName);
			final GraphMLReader2<LabeledIntGraph, LabeledNode, LabeledIntEdge> graphReader = new GraphMLReader(fileReader);
			this.g = graphReader.readGraph();
			this.layout1 = new StaticLayout<>(this.g, StaticLayout.positionInitializer);
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final GraphIOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fileReader != null) {
					fileReader.close();
				}
			} catch (final Exception ee) {
			}
		}
	}

	/**
	 * The following elements are saved in the order specified:<br>
	 * 1. LabeledIntGraph g<br>
	 * For each node i:<br>
	 * i: LabeledNode i <Br>
	 * i+1: Point2D position
	 *
	 * @param g
	 *                graph to save
	 * @param file
	 */
	void saveXML(final LabeledIntGraph g, final File file) {
		final GraphMLWriter<LabeledNode, LabeledIntEdge> graphWriter = new it.univr.di.cstnu.graph.GraphMLWriter(this.layout1);
		g.setName(file.getName());
		try {
			@SuppressWarnings("resource")
			final Writer out = new BufferedWriter(new FileWriter(file));
			graphWriter.save(g, out);
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
