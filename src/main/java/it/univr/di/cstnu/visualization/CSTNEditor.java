/**
 *
 */
package it.univr.di.cstnu.visualization;

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
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.border.EmptyBorder;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import com.google.common.base.Supplier;

import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.univr.di.cstnu.algorithms.CSTN;
import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
import it.univr.di.cstnu.algorithms.CSTN.DCSemantics;
import it.univr.di.cstnu.algorithms.CSTNU;
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.algorithms.CSTNepsilon;
import it.univr.di.cstnu.algorithms.CSTNir;
import it.univr.di.cstnu.algorithms.WellDefinitionException;
import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
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
	static final URL infoIconFile = Class.class.getResource("/images/metal-info.png");

	/**
	 *
	 */
	static final URL warnIconFile = Class.class.getResource("/images/metal-warning.png");

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
	 * derivedGraphMessageArea
	 */
	JEditorPane derivedGraphMessageArea;

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
	JLabel mapInfoLabel;

	/**
	 * Result Save Button
	 */
	JButton saveCSTNResultButton;

	/**
	 * Layout for input graph.
	 */
	StaticLayout<LabeledIntEdge> layout1;

	/**
	 * Layout for derived graph.
	 */
	StaticLayout<LabeledIntEdge> layout2;

	/**
	 * The model for the viewer of input graph. Useful if there are more than one Viewer.
	 */
//	VisualizationModel<LabeledNode, LabeledIntEdge> vm1;

	/**
	 * The model for the viewer of the derived graph.
	 */
//	VisualizationModel<LabeledNode, LabeledIntEdge> vm2;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for input graph.
	 */
	VisualizationViewer<LabeledNode, LabeledIntEdge> vv1;

	/**
	 * The BasicVisualizationServer&lt;V,E&gt; for derived graph.
	 */
	VisualizationViewer<LabeledNode, LabeledIntEdge> vv2;

	/**
	 * LabeledIntGraph structures necessary to represent all graphs.
	 */
	@SuppressWarnings("javadoc")
	LabeledIntGraph g, g1, g2, distanceGraph;

	/**
	 * CSTN checker
	 */
	CSTN cstn;

	/**
	 * CSTNU checker
	 */
	CSTNU cstnu;

	/**
	 * CSTN check status
	 */
	CSTNCheckStatus cstnStatus;

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
		this.vv1 = new VisualizationViewer<>(this.layout1, CSTNEditor.preferredSize);
		this.vv1.setName("Editor");
		this.vv2 = new VisualizationViewer<>(this.layout2, CSTNEditor.preferredSize);
		this.vv2.setName(CSTNEditor.distanceViewerName);

		// VERTEX setting
		// vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		this.vv1.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		this.vv1.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		this.vv1.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);
//
		this.vv2.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		this.vv2.getRenderContext().setVertexLabelTransformer(LabeledNode.vertexLabelTransformer);
		this.vv2.setVertexToolTipTransformer(LabeledNode.vertexToolTipTransformer);

		// EDGE setting
		this.vv1.getRenderContext().setEdgeDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setEdgeFontTransformer(AbstractLabeledIntEdge.edgeFontFunction);
		this.vv1.getRenderContext().setEdgeLabelTransformer(AbstractLabeledIntEdge.edgeLabelFunction);
		this.vv1.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv1.getRenderContext().setEdgeStrokeTransformer(AbstractLabeledIntEdge.edgeStrokeTransformer);
		this.vv1.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<LabeledNode,LabeledIntEdge>(0.65, 0.5));
		this.vv1.getRenderContext().setLabelOffset(13);
		this.vv1.getRenderContext().setArrowDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv1.getRenderContext().setArrowFillPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv1.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		this.vv2.getRenderContext().setEdgeLabelTransformer(AbstractLabeledIntEdge.edgeLabelFunction);
		this.vv2.getRenderContext().setEdgeDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setEdgeLabelRenderer(new DefaultEdgeLabelRenderer(Color.blue));
		this.vv2.getRenderContext().setEdgeStrokeTransformer(AbstractLabeledIntEdge.edgeStrokeTransformer);
		this.vv2.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<LabeledNode,LabeledIntEdge>(0.65, 0.5));
		this.vv2.getRenderContext().setArrowDrawPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));
		this.vv2.getRenderContext().setArrowFillPaintTransformer(
				AbstractLabeledIntEdge.edgeDrawPaintTransformer(this.vv2.getPickedEdgeState(), Color.blue, Color.black, Color.orange, Color.gray));

		// MOUSE setting
		// Create a graph mouse and add it to the visualization component
		Supplier<LabeledIntEdge> edgeFactory = new LabeledIntEdgeSupplier<>(this.labeledIntValueMap);
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm1 = new EditingModalGraphMouse<>(this.vv1.getRenderContext(), LabeledNode.getFactory(),
				edgeFactory, this);
		gm1.setMode(ModalGraphMouse.Mode.PICKING);
		this.vv1.setGraphMouse(gm1);
		this.vv1.addKeyListener(gm1.getModeKeyListener());
		final EditingModalGraphMouse<LabeledNode, LabeledIntEdge> gm2 = new EditingModalGraphMouse<>(this.vv2.getRenderContext(), LabeledNode.getFactory(), edgeFactory, this);
		gm2.setMode(ModalGraphMouse.Mode.PICKING);
		this.vv2.setGraphMouse(gm2);
		this.vv2.addKeyListener(gm2.getModeKeyListener());

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

		message1Graph = new JPanel(new GridLayout(1, 1));// even if in the second cell there is only one element, derivedGraphMessageArea, a JPanel is necessary to have the same
										// padding of first cell.
		
		this.derivedGraphMessageArea = new JEditorPane("text/html", "");
		this.derivedGraphMessageArea.setBorder(new EmptyBorder(2, 2, 2, 2));
		this.derivedGraphMessageArea.setEditable(false);
		this.derivedGraphMessageArea.setVisible(true);
		message1Graph.add(this.derivedGraphMessageArea);
		messagePanel.add(message1Graph);

		contentPane.add(messagePanel, BorderLayout.NORTH);

		// LEFT
		contentPane.add(new GraphZoomScrollPane(this.vv1), BorderLayout.WEST);

		// RIGHT
		contentPane.add(new GraphZoomScrollPane(this.vv2), BorderLayout.EAST);

		// SOUTH
		final JPanel controls = new JPanel(new GridLayout(3, 1)), rowForAppButtons = new JPanel(), rowForCSTNButtons = new JPanel(),
				rowForCSTNUButtons = new JPanel();
		final ValidationPanel validationPanelRowForCSTNButtons = new ValidationPanel();
		final ValidationGroup validationGroupCSTN = validationPanelRowForCSTNButtons.getValidationGroup();
		validationPanelRowForCSTNButtons.setInnerComponent(rowForCSTNButtons);
		validationPanelRowForCSTNButtons.setBorder(BorderFactory.createLineBorder(getForeground(), 1));
		controls.add(rowForAppButtons, 0);// for tuning application
		controls.add(validationPanelRowForCSTNButtons, 1);// for button regarding CSTN
		controls.add(rowForCSTNUButtons, 2);// for button regarding CSTNU
		contentPane.add(controls, BorderLayout.SOUTH);

		JButton buttonCheck;

		// FIRST ROW OF COMMANDS

		@SuppressWarnings("rawtypes")
		final JComboBox modeBox = gm1.getModeComboBox();
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
		buttonCheck.addActionListener(new HelpListener());
		rowForAppButtons.add(buttonCheck);

		// SECOND ROW OF COMMANDS

		// rowForCSTNButtons.add(new JLabel("Labeled value mode"));
		// @SuppressWarnings("rawtypes")
		// JComboBox<Class> jcb = new JComboBox<>(new Class[] { LabeledIntTreeMap.class, LabeledIntHierarchyMap.class});

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
		buttonCheck.addActionListener(new CSTNInitListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTN Check");
		buttonCheck.addActionListener(new CSTNCheckListener());
		rowForCSTNButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTN Check");
		buttonCheck.addActionListener(new CSTNOneStepListener());
		rowForCSTNButtons.add(buttonCheck);

		this.saveCSTNResultButton = new JButton("Save CSTN");
		this.saveCSTNResultButton.setEnabled(false);
		this.saveCSTNResultButton.addActionListener(new CSTNSaveListener());
		rowForCSTNButtons.add(this.saveCSTNResultButton);

		buttonCheck = new JButton("CSTNU Init Graph");
		buttonCheck.addActionListener(new CSTNUInitListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("CSTNU Check");
		buttonCheck.addActionListener(new CSTNUCheckListener());
		rowForCSTNUButtons.add(buttonCheck);

		buttonCheck = new JButton("One Step CSTNU Check");
		buttonCheck.addActionListener(new CSTNUOneStepListener());
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
	 * Load graph stored in file 'fileName' into attribute this.g. Moreover, it create this.layout1 using this.g.
	 * 
	 * @param fileName
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	void loadGraphG(final File fileName) throws ClassNotFoundException, FileNotFoundException, GraphIOException {
		try (FileReader fileReader = new FileReader(fileName)) {
			final GraphMLReader<LabeledIntGraph> graphReader = new GraphMLReader<>(fileReader, this.labeledIntValueMap);
			this.g = graphReader.readGraph();
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
	void saveGraphToFile(final LabeledIntGraph graphToSave, final File file) {
		final GraphMLWriter graphWriter = new GraphMLWriter(this.layout1);
		graphToSave.setName(file.getName());
		try (Writer out = new BufferedWriter(new FileWriter(file))) {
			graphWriter.save(graphToSave, out);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Every time the graph associated to a viewer is replaced by another one, the layout and the visualization viewer render context has to be update.
	 * 
	 * @param firstViewer
	 */
	void updateVisualizationViewer(boolean firstViewer) {
		if (firstViewer) {
			CSTNEditor.this.layout1.setGraph(CSTNEditor.this.g);
			CSTNEditor.this.vv1.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve(CSTNEditor.this.g));
		} else {
			CSTNEditor.this.layout2.setGraph(CSTNEditor.this.g1);
			CSTNEditor.this.vv2.getRenderContext().setEdgeShapeTransformer(EdgeShape.quadCurve(CSTNEditor.this.g1));
		}
	}
	
	/**
	 * Update node positions in derived graph.
	 */
	void updateNodePositions() {
		LabeledNode gV;
		for (final LabeledNode v : CSTNEditor.this.g1.getVertices()) {
			gV = CSTNEditor.this.g.getNode(v.getName());
			if (gV != null) {
				CSTNEditor.LOG.finest("Vertex of original graph: " + gV);
				CSTNEditor.LOG.finest("Original position (" + CSTNEditor.this.layout1.getX(gV) + ";" + CSTNEditor.this.layout1.getY(gV) + ")");
				CSTNEditor.this.layout2.setLocation(v, CSTNEditor.this.layout1.getX(gV), CSTNEditor.this.layout1.getY(gV));
			} else {
				CSTNEditor.this.layout2.setLocation(v, v.getX(), v.getY());
			}
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
			JOptionPane.showMessageDialog(CSTNEditor.this.vv1, instructions);
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
	@SuppressWarnings("javadoc")
	private class CSTNInitListener implements ActionListener {

		public CSTNInitListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.derivedGraphMessageArea;
			CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.g1.getEdgeFactory().toString());
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
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
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
	private class CSTNCheckListener implements ActionListener {

		public CSTNCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl = CSTNEditor.this.derivedGraphMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
			CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.g1.getEdgeFactory().toString());
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

					jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTN consistent.");
					jl.setBackground(Color.green);
					CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.g1);
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
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vv2.validate();
			CSTNEditor.this.vv2.repaint();

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
			final JEditorPane jl = CSTNEditor.this.derivedGraphMessageArea;
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
				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.g1.getEdgeFactory().toString());
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
					jl.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is inconsistent.<b>");
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.g2);
					CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnStatus);
				} else if (reductionsApplied) {
					jl.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
					// jl.setIcon(CSTNEditor.warnIcon);
				} else {
					jl.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTN consistent. The number of executed cycles is "
							+ CSTNEditor.this.cycle);
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
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
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
				CSTNEditor.this.saveGraphToFile(CSTNEditor.this.g1, file);
			}
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
			final JEditorPane jl1 = CSTNEditor.this.derivedGraphMessageArea;
			CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);

			CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
			try {
				CSTNEditor.this.cstnu.initUpperLowerLabelDataStructure();
			} catch (final IllegalArgumentException | WellDefinitionException ec) {
				String msg = "The graph has a problem and it cannot be initialize: " + ec.getMessage();
				CSTNEditor.LOG.warning(msg);
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>" + msg + "</b>");
				// jl.setIcon(CSTNUEditor.warnIcon);
				jl1.setOpaque(true);
				jl1.setBackground(Color.orange);
				// CSTNUEditor.this.vv2.validate();
				// CSTNUEditor.this.vv2.repaint();
				CSTNEditor.this.validate();
				CSTNEditor.this.repaint();
				return;
			}
			jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;LabeledIntGraph with Lower and Upper Case Labels.");
			// jl.setIcon(CSTNUEditor.infoIcon);
			jl1.setOpaque(true);
			jl1.setBackground(Color.orange);
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
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
	private class CSTNUCheckListener implements ActionListener {

		public CSTNUCheckListener() {
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final JEditorPane jl1 = CSTNEditor.this.derivedGraphMessageArea;
			CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
			CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
			CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
			jl1.setBackground(Color.orange);
			try {
				CSTNEditor.this.cstnuStatus = CSTNEditor.this.cstnu.dynamicControllabilityCheck();
				if (CSTNEditor.this.cstnuStatus.consistency) {
					jl1.setText("<img align='middle' src='" + infoIconFile + "'>&nbsp;<b>The graph is CSTNU controllable.</b>");
					// jl.setIcon(CSTNUEditor.infoIcon);
					jl1.setBackground(Color.green);
					CSTNEditor.LOG.finer("Final controllable graph: " + CSTNEditor.this.g1);
				} else {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is not CSTNU controllable.</b>");
					// jl.setIcon(CSTNUEditor.warnIcon);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNUEditor.warnIcon);
			}
			jl1.setOpaque(true);
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.vv2.validate();
			CSTNEditor.this.vv2.repaint();

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
			final JEditorPane jl1 = CSTNEditor.this.derivedGraphMessageArea;
			if (CSTNEditor.this.cycle == -1)
				return;
			if (CSTNEditor.this.cycle == 0) {
				CSTNEditor.this.g1 = new LabeledIntGraph(CSTNEditor.this.g, CSTNEditor.this.labeledIntValueMap);
				CSTNEditor.this.cstnu = new CSTNU(CSTNEditor.this.g1);
				CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.g1.getEdgeFactory().toString());
				try {
					CSTNEditor.this.cstnu.initUpperLowerLabelDataStructure();
				} catch (final Exception ex) {
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the graph: " + ex.getMessage());
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
					jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;<b>The graph is inconsistent.</b>");
					// jl.setIcon(CSTNEditor.warnIcon);
					CSTNEditor.this.cycle = -1;
					CSTNEditor.LOG.fine("INCONSISTENT GRAPH: " + CSTNEditor.this.g1);
					CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
				} else if (reductionsApplied) {
					jl1.setText("Step " + CSTNEditor.this.cycle + " of consistency check is done.");
					// jl.setIcon(CSTNEditor.warnIcon);
				} else {
					jl1.setText("<b>The graph is CSTNU consistent. The number of executed cycles is " + CSTNEditor.this.cycle);
					// jl.setIcon(CSTNEditor.infoIcon);
					CSTNEditor.this.cycle = -1;
					jl1.setBackground(Color.green);
					CSTNEditor.LOG.info("Status stats: " + CSTNEditor.this.cstnuStatus);
				}
			} catch (final WellDefinitionException ex) {
				jl1.setText("<img align='middle' src='" + warnIconFile + "'>&nbsp;There is a problem in the code: " + ex.getMessage());
				// jl.setIcon(CSTNEditor.warnIcon);
				CSTNEditor.this.cycle = -1;
				return;
			}

			jl1.setOpaque(true);
			updateVisualizationViewer(false);
			updateNodePositions();
			CSTNEditor.this.vv2.setVisible(true);
			CSTNEditor.this.saveCSTNResultButton.setEnabled(true);

			CSTNEditor.this.validate();
			CSTNEditor.this.repaint();
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
			final JEditorPane jl = CSTNEditor.this.derivedGraphMessageArea;
			if (option == JFileChooser.APPROVE_OPTION) {
				final File file = chooser.getSelectedFile();
				CSTNEditor.defaultDir = file.getParent();
				try {
					CSTNEditor.this.loadGraphG(file);
					updateVisualizationViewer(true);
					CSTNEditor.this.vv2.setVisible(false);
					
					CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
					jl.setText("");
					jl.setOpaque(false);
					// CSTNEditor.this.setTitle("CSTNU Editor and Checker: " + file.getName() + "-" + CSTNEditor.this.g.getName());
					CSTNEditor.this.graphInfoLabel.setText("File " + file.getName()
							+ ": #nodes: " + CSTNEditor.this.g.getVertexCount()
							+ ", #edges: " + CSTNEditor.this.g.getEdgeCount()
							+ ", #obs: " + CSTNEditor.this.g.getObservators().size()
							+ ".");
					CSTNEditor.this.mapInfoLabel.setText(CSTNEditor.this.g.getEdgeFactory().toString());

				} catch (ClassNotFoundException | FileNotFoundException | GraphIOException e1) {
					CSTNEditor.this.g = new LabeledIntGraph(CSTNEditor.this.labeledIntValueMap);
					CSTNEditor.this.layout1 = new StaticLayout<>(CSTNEditor.this.g);
					CSTNEditor.this.vv1.setGraphLayout(CSTNEditor.this.layout1);
					CSTNEditor.this.vv2.setVisible(false);
					CSTNEditor.this.saveCSTNResultButton.setEnabled(false);
					String msg = "The graph has a problem in the definition:" + e1.getMessage();
					CSTNEditor.LOG.warning(msg);
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
				CSTNEditor.this.saveGraphToFile(CSTNEditor.this.g, file);
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
