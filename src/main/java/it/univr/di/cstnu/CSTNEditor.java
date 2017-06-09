/**
 *
 */
package it.univr.di.cstnu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.univr.di.cstnu.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.CSTN.DCSemantics;
import it.univr.di.cstnu.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
import it.univr.di.cstnu.graph.EditingModalGraphMouse;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;
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
	static Logger LOG = Logger.getLogger(CSTNEditor.class.getName());

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
	static String defaultDir = "/Dropbox/";

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 */
	public static void main(final String[] args) {
		@SuppressWarnings("unused")
		CSTNEditor editor = new CSTNEditor();
	}

	/**
	 * LabeledIntGraph structures necessary to represent all graphs.
	 */
	@SuppressWarnings("javadoc")
	LabeledIntGraph g, g1, g2, distanceGraph;
	
	/**
	 * CSTN checker
	 */
	CSTN  cstn = null;
	
	/**
	 * CSTNU checker
	 */
	CSTNU cstnu = null;
	
	/**
	 * 
	 */
	CSTNCheckStatus cstnStatus = null;

	@SuppressWarnings("javadoc")
	CSTNUCheckStatus cstnuStatus = null;

	@SuppressWarnings("javadoc")
	StaticLayout<LabeledNode, LabeledIntEdge> layout1;

	/**
	 * Layout algorithm for graph.
	 */
	StaticLayout<LabeledNode, LabeledIntEdge> layout2;

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
	 * The epsilon panel
	 */
	JPanel epsilonPanel;


	/**
	 * The graph info label
	 */
	JLabel graphInfoLabel;

	/**
	 * 
	 */
	JEditorPane mapInfoLabel;

	/**
	 * The graph panel
	 */
	JPanel graphPanel;

	/**
	 * To count the number of cycle of CSTNU check step-by-step
	 */
	int cycle;

	/**
	 * To store edges to check in CSTNU check step-by-step
	 */
	ObjectArraySet<LabeledIntEdge> edgesToCheck;

	/**
	 * Reaction time for CSTN
	 */
	int reactionTime = 1;

	/**
	 * The current wanted semantics
	 */
	public DCSemantics dcCurrentSem = DCSemantics.Std;

	/**
	 * CSTNU save button
	 */
	JButton saveCSTNResultButton;

	/**
	 * Class for representing edge labeled values.
	 */
	final Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;// LabeledIntHierarchyMap.class;

	/**
	 * Default constructor
	 */
	public CSTNEditor() {
		super("Simple CSTNU Editor " + CSTNEditor.version);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.g = new LabeledIntGraph(this.labeledIntValueMap);
		this.g1 = new LabeledIntGraph(this.labeledIntValueMap);
		this.layout1 = new StaticLayout<>(this.g, CSTNEditor.preferredSize);
		this.layout2 = new StaticLayout<>(this.g1, CSTNEditor.preferredSize);
		this.vm1 = new DefaultVisualizationModel<>(this.layout1, CSTNEditor.preferredSize);
		this.vm2 = new DefaultVisualizationModel<>(this.layout2, CSTNEditor.preferredSize);
		this.vv1 = new VisualizationViewer<>(this.vm1, CSTNEditor.preferredSize);
		this.vv1.setName("Editor");
		// vv1.getRenderContext().setLabelOffset(20);
		this.vv2 = new VisualizationViewer<>(this.vm2, CSTNEditor.preferredSize);
		this.vv2.setName(CSTNEditor.distanceViewerName);

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
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setEdgeFontTransformer(AbstractLabeledIntEdge.edgeFontTransformer);
		this.vv1.getRenderContext().setEdgeLabelTransformer(AbstractLabeledIntEdge.edgeLabelTransformer);
		this.vv1.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv1.getRenderContext().setEdgeShapeTransformer(new EdgeShape.QuadCurve<LabeledNode, LabeledIntEdge>());
		this.vv1.getRenderContext().setEdgeStrokeTransformer(AbstractLabeledIntEdge.edgeStrokeTransformer);

		this.vv1.getRenderContext().setArrowDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setArrowFillPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		this.vv2.getRenderContext().setEdgeLabelTransformer(AbstractLabeledIntEdge.edgeLabelTransformer);
		this.vv2.getRenderContext().setEdgeDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv2.getRenderContext().setEdgeStrokeTransformer(AbstractLabeledIntEdge.edgeStrokeTransformer);
		this.vv2.getRenderContext().setArrowDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setArrowFillPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = new LabeledIntEdgeFactory<>(this.labeledIntValueMap);
		@SuppressWarnings({ "rawtypes", "unchecked" })
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm = new EditingModalGraphMouse(this.vv1.getRenderContext(), LabeledNode.getFactory(),
				edgeFactory);
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
		// this.messagesPanel.add(new JLabel(" "));
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
		this.graphInfoLabel = new JLabel("");
		rowForAppButtons.add(this.graphInfoLabel);
		this.mapInfoLabel = new JEditorPane("text/html", "");
		this.mapInfoLabel.setEditable(false);
		this.mapInfoLabel.setSize(200, 20);
		this.mapInfoLabel.setVisible(true);
		rowForAppButtons.add(this.mapInfoLabel);

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
		// CSTNEditor.this.excludeR1R2 = true;
		// CSTNEditor.LOG.fine("excludeR1R2 flag set to true");
		// } else if (ev.getStateChange() == ItemEvent.DESELECTED) {
		// CSTNEditor.this.excludeR1R2 = false;
		// CSTNEditor.LOG.fine("excludeR1R2 flag set to false");
		// }
		// }
		// });
		// rowForAppButtons.add(excludeR1R2Button);

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

			@Override
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(CSTNEditor.this.vv1, instructions);
			}
		});
		rowForAppButtons.add(buttonCheck);

		buttonCheck = new JButton("Interrupt");
		buttonCheck.addActionListener(new AbstractAction("Interrupt") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				Thread.currentThread().interrupt();
			}
		});
		// rowForAppButtons.add(buttonCheck);

		// SECOND ROW OF COMMANDS
		// rowForCSTNButtons.add(new JLabel("Labeled value mode"));
		// @SuppressWarnings("rawtypes")
		// JComboBox<Class> jcb = new JComboBox<>(new Class[] { LabeledIntTreeMap.class, LabeledIntHierarchyMap.class});

		rowForCSTNButtons.add(new JLabel("DC Semantics: "));
		final JComboBox<DCSemantics> dcSemCombo = new JComboBox<DCSemantics>(DCSemantics.values());
		dcSemCombo.setSelectedItem(DCSemantics.Std);
		dcSemCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CSTNEditor.this.dcCurrentSem = (DCSemantics) dcSemCombo.getSelectedItem();
				CSTNEditor.this.epsilonPanel.setVisible(CSTNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
			}
		});
		rowForCSTNButtons.add(dcSemCombo);

		//epsilon panel
		CSTNEditor.this.epsilonPanel = new JPanel(new FlowLayout());
		
		CSTNEditor.this.epsilonPanel.add(new JLabel("System reacts "));
		final JFormattedTextField jreactionTime = new JFormattedTextField();
		jreactionTime.setValue(this.reactionTime);
		jreactionTime.setColumns(3);
		jreactionTime.addPropertyChangeListener("value", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				CSTNEditor.LOG.finest("Property: " + evt.getPropertyName());
				CSTNEditor.this.reactionTime = ((Number) jreactionTime.getValue()).intValue();
				CSTNEditor.LOG.info("New reaction time: " + CSTNEditor.this.reactionTime);
			}
		});
		validationGroupCSTN.add(jreactionTime, StringValidators.regexp(Constants.NonNegIntValueRE, "A > 0 integer!", false));
		CSTNEditor.this.epsilonPanel.add(jreactionTime);
		CSTNEditor.this.epsilonPanel.add(new JLabel("time units after (≥) an observation."));
		CSTNEditor.this.epsilonPanel.setVisible(CSTNEditor.this.dcCurrentSem.equals(DCSemantics.ε));
		rowForCSTNButtons.add(CSTNEditor.this.epsilonPanel);

		buttonCheck = new JButton("CSTN Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTN Init LabeledIntGraph") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				@SuppressWarnings("hiding")
				final JTextArea jl = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
				CSTNEditor.this.mapInfoLabel.setText("<b>" + CSTNEditor.this.g1.getEdgeFactory().toString());
				switch (CSTNEditor.this.dcCurrentSem) {
				case ε:
					CSTNEditor.this.cstn = new CSTNepsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.g1);
					break;
				case IR:
					CSTNEditor.this.cstn = new CSTNir(CSTNEditor.this.g1);
					break;
				default:
					CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.g1);
					break;
				}
				try {
					CSTNEditor.this.cstn.initAndCheck();
				} catch (final Exception ec) {
					String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
					CSTNEditor.LOG.warning(msg);
					jl.setText(msg);
					// jl.setIcon(CSTNEditor.warnIcon);
					jl.setOpaque(true);
					jl.setBackground(Color.orange);
					// CSTNEditor.this.vv2.validate();
					// CSTNEditor.this.vv2.repaint();
					CSTNEditor.this.graphPanel.validate();
					CSTNEditor.this.graphPanel.repaint();
					return;
				}
				jl.setText("CSTN initialized.");
				// jl.setIcon(CSTNEditor.infoIcon);
				jl.setOpaque(true);
				jl.setBackground(Color.orange);
				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g1);
				LabeledNode gV;
				CSTNEditor.LOG.finer("Original graph: " + CSTNEditor.this.g);
				CSTNEditor.LOG.finer("Complete graph: " + CSTNEditor.this.g1);
				for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
					CSTNEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";"
								+ CSTNEditor.this.layout1.getY(gV) + ")");
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
				CSTNEditor.this.cycle = 0;
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
				@SuppressWarnings("hiding")
				final JTextArea jl = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
				CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
				CSTNEditor.this.mapInfoLabel.setText("<b>" + CSTNEditor.this.g1.getEdgeFactory().toString());
				switch (CSTNEditor.this.dcCurrentSem) {
				case ε:
					CSTNEditor.this.cstn = new CSTNepsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.g1);
					break;
				case IR:
					CSTNEditor.this.cstn = new CSTNir(CSTNEditor.this.g1);
					break;
				default:
					CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.g1);
					break;
				}

				jl.setBackground(Color.orange);
				try {
					CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.dynamicConsistencyCheck();
					if (CSTNEditor.this.cstnStatus.consistency) {
						jl.setText("The graph is CSTN consistent.");
						jl.setBackground(Color.green);
						// jl.setIcon(CSTNEditor.infoIcon);
						CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.g1);
					} else {
						// The distance graph is not consistent
						jl.setText("The graph is not CSTN consistent.");
						// jl.setIcon(CSTNEditor.warnIcon);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
				}
				jl.setOpaque(true);
				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g1);
				LabeledNode gV;
				for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";"
								+ CSTNEditor.this.layout1.getY(gV) + ")");
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.vv2.validate();
				CSTNEditor.this.vv2.repaint();

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
				CSTNEditor.this.cycle = 0;
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTN") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				@SuppressWarnings("hiding")
				final JTextArea jl = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				switch (CSTNEditor.this.dcCurrentSem) {
				case ε:
					CSTNEditor.this.cstn = new CSTNepsilon(CSTNEditor.this.reactionTime, CSTNEditor.this.g1);
					break;
				case IR:
					CSTNEditor.this.cstn = new CSTNir(CSTNEditor.this.g1);
					break;
				default:
					CSTNEditor.this.cstn = new CSTN(CSTNEditor.this.g1);
					break;
				}

				if (CSTNEditor.this.cycle == -1)
					return;
				if (CSTNEditor.this.cycle == 0) {
					CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
					CSTNEditor.this.mapInfoLabel.setText("<b>" + CSTNEditor.this.g1.getEdgeFactory().toString());
					try {
						CSTNEditor.this.cstn.initAndCheck();
					} catch (final Exception ex) {
						jl.setText("There is a problem in the graph: " + ex.getMessage());
						// jl.setIcon(CSTNEditor.warnIcon);
						CSTNEditor.this.cycle = -1;
						return;
					}
					CSTNEditor.this.g2 = new LabeledIntGraph(CSTNEditor.this.g1, CSTNEditor.this.labeledIntValueMap);
					CSTNEditor.this.cstnStatus = new CSTNCheckStatus();
				} else {
					CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g2, CSTNEditor.this.labeledIntValueMap);
				}
				CSTNEditor.this.cycle++;

				jl.setBackground(Color.orange);
				try {
					CSTNEditor.this.cstnStatus = CSTNEditor.this.cstn.oneStepDynamicConsistencyByNode();
					CSTNEditor.this.cstnStatus.finished = CSTNEditor.this.g1.hasSameEdgesOf(CSTNEditor.this.g2);
					final boolean reductionsApplied = !CSTNEditor.this.cstnStatus.finished;
					final boolean inconsistency = !CSTNEditor.this.cstnStatus.consistency;
					if (inconsistency) {
						jl.setText("The graph is inconsistent.");
						// jl.setIcon(CSTNEditor.warnIcon);
						CSTNEditor.this.cycle = -1;
						CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.g2);
						CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnStatus);
					} else if (reductionsApplied) {
						jl.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
						// jl.setIcon(CSTNEditor.warnIcon);
					} else {
						jl.setText("The graph is CSTN consistent. The number of executed cycles is " + CSTNEditor.this.cycle);
						// jl.setIcon(CSTNEditor.infoIcon);
						CSTNEditor.this.cycle = -1;
						jl.setBackground(Color.green);
						CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnStatus);
					}
				} catch (final WellDefinitionException ex) {
					jl.setText("There is a problem in the code: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}

				jl.setOpaque(true);
				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g2);
				LabeledNode gV;
				for (final LabeledNode v : CSTNEditor.this.g2.getVertices()) {
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";"
								+ CSTNEditor.this.layout1.getY(gV) + ")");
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
			}
		});
		rowForCSTNButtons.add(buttonCheck);

		this.saveCSTNResultButton = new JButton("Save CSTN");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new AbstractAction("Save result") {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				// CSTNEditor.LOG.finest("Current dir:" + f);

				// CSTNEditor.LOG.finest("Path wanted:" + path);
				chooser = new JFileChooser(CSTNEditor.defaultDir);
				final int option = chooser.showSaveDialog(CSTNEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNEditor.this.saveXML(CSTNEditor.this.g1, file);
				}
			}
		});
		rowForCSTNButtons.add(this.saveCSTNResultButton);

		buttonCheck = new JButton("CSTNU Init Graph");
		buttonCheck.addActionListener(new AbstractAction("CSTNU Init LabeledIntGraph") {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl1 = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);

				CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
				try {
					CSTNEditor.this.cstnu.initUpperLowerLabelDataStructure();
				} catch (final IllegalArgumentException | WellDefinitionException ec) {
					String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
					CSTNEditor.LOG.warning(msg);
					jl1.setText(msg);
					// jl.setIcon(CSTNUEditor.warnIcon);
					jl1.setOpaque(true);
					jl1.setBackground(Color.orange);
					// CSTNUEditor.this.vv2.validate();
					// CSTNUEditor.this.vv2.repaint();
					CSTNEditor.this.graphPanel.validate();
					CSTNEditor.this.graphPanel.repaint();
					return;
				}
				jl1.setText("LabeledIntGraph with Lower and Upper Case Labels.");
				// jl.setIcon(CSTNUEditor.infoIcon);
				jl1.setOpaque(true);
				jl1.setBackground(Color.orange);
				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g1);
				LabeledNode gV;
				CSTNEditor.LOG.finer("Original graph: " + CSTNEditor.this.g);
				CSTNEditor.LOG.finer("Complete graph: " + CSTNEditor.this.g1);
				for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
					CSTNEditor.LOG.finest("Vertex of complete graph: " + v);
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";"
								+ CSTNEditor.this.layout1.getY(gV) + ")");
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
				CSTNEditor.this.cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);
	
		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("CSTNU") {
			/**
			*
			*/
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				// final JLabel jl = (JLabel) CSTNUEditor.this.messagesPanel.getComponent(1);
				final JTextArea jl1 = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
				CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
				CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
				jl1.setBackground(Color.orange);
				try {
					CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
					if (CSTNEditor.this.cstnuStatus.consistency) {
						jl1.setText("The graph is CSTNU controllable.");
						// jl.setIcon(CSTNUEditor.infoIcon);
						jl1.setBackground(Color.green);
						CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.g1);
					} else {
						jl1.setText("The graph is not CSTNU controllable.");
						// jl.setIcon(CSTNUEditor.warnIcon);
					}
				} catch (final WellDefinitionException ex) {
					jl1.setText("There is a problem in the code: " + ex.getMessage());
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
				jl1.setOpaque(true);

				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g1);
				LabeledNode gV;
				for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.vv2.validate();
				CSTNEditor.this.vv2.repaint();

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
				CSTNEditor.this.cycle = 0;
			}
		});
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new AbstractAction("One Step CSTNU") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JTextArea jl1 = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				if (CSTNEditor.this.cycle == -1)
					return;
				if (CSTNEditor.this.cycle == 0) {
					CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
					CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
					CSTNEditor.this.mapInfoLabel.setText("<b>" + CSTNEditor.this.g1.getEdgeFactory().toString());
					try {
						CSTNEditor.this.cstnu.initUpperLowerLabelDataStructure();
					} catch (final Exception ex) {
						jl1.setText("There is a problem in the graph: " + ex.getMessage());
						// jl.setIcon(CSTNEditor.warnIcon);
						CSTNEditor.this.cycle = -1;
						return;
					}
					CSTNEditor.this.cstnuStatus = new CSTNUCheckStatus();
					CSTNEditor.this.edgesToCheck = new ObjectArraySet<>(CSTNEditor.this.g1.getEdges());
				}
				CSTNEditor.this.cycle++;

				jl1.setBackground(Color.orange);
				try {
					CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.oneStepDynamicControllability(CSTNEditor.this.edgesToCheck);
					CSTNEditor.this.cstnuStatus.finished = CSTNEditor.this.edgesToCheck.size() == 0;
					final boolean reductionsApplied = !CSTNEditor.this.cstnuStatus.finished;
					final boolean notControllable = !CSTNEditor.this.cstnuStatus.consistency;
					if (notControllable) {
						jl1.setText("The graph is inconsistent.");
						// jl.setIcon(CSTNEditor.warnIcon);
						CSTNEditor.this.cycle = -1;
						CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.g1);
						CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
					} else if (reductionsApplied) {
						jl1.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
						// jl.setIcon(CSTNEditor.warnIcon);
					} else {
						jl1.setText("The graph is CSTNU consistent. The number of executed cycles is " + CSTNEditor.this.cycle);
						// jl.setIcon(CSTNEditor.infoIcon);
						CSTNEditor.this.cycle = -1;
						jl1.setBackground(Color.green);
						CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
					}
				} catch (final WellDefinitionException ex) {
					jl1.setText("There is a problem in the code: " + ex.getMessage());
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					return;
				}

				jl1.setOpaque(true);
				CSTNEditor.this.layout2 = new StaticLayout<>(CSTNEditor.this.g1);
				LabeledNode gV;
				for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
					gV = CSTNEditor.this.g.getNode(v.getName());
					if (gV != null) {
						CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
						CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";"
								+ CSTNEditor.this.layout1.getY(gV) + ")");
						CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
					} else {
						CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
					}
				}
				CSTNEditor.this.vv2.setGraphLayout(CSTNEditor.this.layout2);
				CSTNEditor.this.vv2.setVisible(true);
				CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

				CSTNEditor.this.graphPanel.validate();
				CSTNEditor.this.graphPanel.repaint();
			}
		});
		rowForCSTNUButtons.add(buttonCheck);
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

		final JMenu menu = new JMenu("File");
		menu.add(new AbstractAction("Open...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser chooser = new JFileChooser(CSTNEditor.defaultDir);
				final int option = chooser.showOpenDialog(CSTNEditor.this);
				@SuppressWarnings("hiding")
				final JTextArea jl = (JTextArea) CSTNEditor.this.messagesPanel.getComponent(0);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNEditor.defaultDir = file.getParent();
					try {
						CSTNEditor.this.openXML(file);
						CSTNEditor.this.vv1.setGraphLayout(CSTNEditor.this.layout1);
						CSTNEditor.this.vv2.setVisible(false);
						CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
						// ((JTextArea) CSTNEditor.this.messagesPanel.getComponent(1)).setIcon(null);
						jl.setText("");
						jl.setOpaque(false);
						// CSTNEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + CSTNEditor.this.g.getName());
						CSTNEditor.this.graphInfoLabel.setText("File: " + file.getName()
								+ ", #nodes: " + CSTNEditor.this.g.getVertexCount()
								+ ", #edges: " + CSTNEditor.this.g.getEdgeCount()
								+ ", #obs: " + CSTNEditor.this.g.getObservators().size());
						CSTNEditor.this.mapInfoLabel.setText("<b>" + CSTNEditor.this.g.getEdgeFactory().toString());
						CSTNEditor.this.getRootPane().repaint();
						CSTNEditor.this.cycle = 0;
					} catch (ClassNotFoundException | FileNotFoundException | GraphIOException e1) {
						CSTNEditor.this.g = new LabeledIntGraph(CSTNEditor.this.labeledIntValueMap);
						CSTNEditor.this.vv1.setGraphLayout(CSTNEditor.this.layout1);
						CSTNEditor.this.vv2.setVisible(false);
						CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
						String msg = "The graph has a problem in the definition:" + e1.getMessage();
						CSTNEditor.LOG.warning(msg);
						jl.setText(msg);
						// jl.setIcon(CSTNUEditor.warnIcon);
						jl.setOpaque(true);
						jl.setBackground(Color.orange);
						CSTNEditor.this.graphPanel.validate();
						CSTNEditor.this.graphPanel.repaint();
						return;
					}

				}
			}
		});
		menu.add(new AbstractAction("Save...") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser chooser = new JFileChooser(CSTNEditor.defaultDir);
				// CSTNEditor.LOG.finest("Path wanted:" + path);
				final int option = chooser.showSaveDialog(CSTNEditor.this);
				if (option == JFileChooser.APPROVE_OPTION) {
					final File file = chooser.getSelectedFile();
					CSTNEditor.defaultDir = file.getParent();
					CSTNEditor.this.saveXML(CSTNEditor.this.g, file);
				}
			}
		});
		// JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		final JMenuBar menuBar = new JMenuBar();
		menuBar.add(menu);
		this.setJMenuBar(menuBar);

		final JFileChooser chooser = new JFileChooser();
		CSTNEditor.defaultDir = chooser.getCurrentDirectory() + CSTNEditor.defaultDir;

		this.pack();
		this.setVisible(true);
	}

	/**
	 * Load graph stored in file 'fileName' into attribute this.g. Moreover, it create this.layout1 using this.g.
	 * 
	 * @param fileName
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	void openXML(final File fileName) throws ClassNotFoundException, FileNotFoundException, GraphIOException {
		try (FileReader fileReader = new FileReader(fileName)) {
			final GraphMLReader<LabeledIntGraph> graphReader = new GraphMLReader<>(fileReader, this.labeledIntValueMap);
			this.g = graphReader.readGraph();
			this.layout1 = new StaticLayout<>(this.g, StaticLayout.positionInitializer);
			fileReader.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * The following elements are saved in the order specified:<br>
	 * 1. LabeledIntGraph g<br>
	 * For each node i:<br>
	 * i: LabeledNode i <Br>
	 * i+1: Point2D position
	 *
	 * @param graphToSave graph to save
	 * @param file
	 */
	void saveXML(final LabeledIntGraph graphToSave, final File file) {
		final GraphMLWriter graphWriter = new GraphMLWriter(this.layout1);
		graphToSave.setName(file.getName());
		try (Writer out = new BufferedWriter(new FileWriter(file))) {
			graphWriter.save(graphToSave, out);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
