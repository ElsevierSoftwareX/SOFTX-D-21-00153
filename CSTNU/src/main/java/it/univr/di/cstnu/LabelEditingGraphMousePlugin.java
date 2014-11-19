/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of California All rights reserved. This
 * software is open-source under the BSD license;
 * see either "license.txt" or http://jung.sourceforge.net/license.txt for a description. Created on Mar 8, 2005
 */
package it.univr.di.cstnu;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.*;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;

/**
 * Allows to edit vertex or edge attributes.
 * 
 * @author Tom Nelson. Adapted by Roberto Posenato.
 * @param <N>
 * @param <E>
 */
public class LabelEditingGraphMousePlugin<N extends Node, E extends Edge> extends AbstractGraphMousePlugin implements
		MouseListener {

	/**
	 * logger della classe
	 */
	static Logger LOG = Logger.getLogger(LabelEditingGraphMousePlugin.class.getName());

	/**
	 * the picked Vertex, if any
	 */
	protected N vertex;

	/**
	 * the picked Edge, if any
	 */
	protected E edge;

	/**
	 * create an instance with default settings
	 */
	public LabelEditingGraphMousePlugin() {
		this(InputEvent.BUTTON1_MASK);
	}

	/**
	 * Create an instance with overrides.
	 * 
	 * @param selectionModifiers for primary selection
	 */
	public LabelEditingGraphMousePlugin(int selectionModifiers) {
		super(selectionModifiers);
		cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	}

	/**
	 * General method to setup a dialog to edit the attributes of a vertex or of an edge.
	 * 
	 * @param e
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings("unchecked")
	private static boolean edgeAttributesEditor(Edge e, String viewerName, Graph g) {

		// Edge has a name, a default value (label for this value is determined by the conjunction of labels of its end-points and a type.

		final boolean distanceViewer = viewerName.equals("DistanceViewer");
		// Create a ValidationPanel - this is a panel that will show
		// any problem with the input at the bottom with an icon
		final ValidationPanel panel = new ValidationPanel();

		/*
		 * The layout is a grid of 3 columns.
		 */
		final JPanel jp = new JPanel(new GridLayout(0, 3));
		panel.setInnerComponent(jp);
		final ValidationGroup group = panel.getValidationGroup();

		// Name
		final JTextField name = new JTextField(e.getName());
		JLabel jl = new JLabel("Name:");
		jl.setLabelFor(name);
		jp.add(jl);
		jp.add(name);
		setConditionToEnable(name, viewerName, false);
		jp.add(new JLabel("Syntax: [A-Za-z0-9_]"));
		group.add(name, StringValidators.REQUIRE_NON_EMPTY_STRING);

		// Default Value
		Integer v = e.getInitialValue();
		final JTextField value = new JTextField((v == null || v.equals(Constants.INFINITY_VALUE)) ? "" : v.toString());
		jl = new JLabel("Initial Value:");
		jl.setLabelFor(value);
		jp.add(jl);
		jp.add(value);
		setConditionToEnable(value, viewerName, false);
		jp.add(new JLabel("Syntax: " + Constants.labeledValueRE));
		group.add(value, StringValidators.regexp(Constants.labeledValueRE, "Check the syntax!", false));

		// Type
		final JRadioButton normalButton = new JRadioButton(Edge.Type.normal.toString());
		normalButton.setSelected(false);
		final JRadioButton contingentButton = new JRadioButton(Edge.Type.contingent.toString());
		contingentButton.setSelected(false);
		final JRadioButton constraintButton = new JRadioButton(Edge.Type.constraint.toString());
		constraintButton.setSelected(false);
		jp.add(new JLabel("Edge type: "));
		normalButton.setActionCommand(Edge.Type.normal.toString());
		normalButton.setSelected(e.getType() == Edge.Type.normal);
		jp.add(normalButton);
		setConditionToEnable(jp, viewerName, false);

		contingentButton.setActionCommand(Edge.Type.contingent.toString());
		contingentButton.setSelected(e.isContingentEdge());
		jp.add(contingentButton);
		setConditionToEnable(jp, viewerName, false);

		jp.add(new JLabel(""));
		constraintButton.setActionCommand(Edge.Type.constraint.toString());
		constraintButton.setSelected(e.getType() == Edge.Type.constraint);
		jp.add(constraintButton);
		setConditionToEnable(jp, viewerName, false);
		jp.add(new JLabel(""));

		// Group the radio buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalButton);
		buttonGroup.add(contingentButton);
		buttonGroup.add(constraintButton);

		// Show possible labeled values
		final Set<Entry<Label, Integer>> labeledValueSet = e.labeledValueSet();
		JTextField jt;
		jp.add(new JLabel("Syntax:"));
		jt = new JTextField(Constants.labelRE);
		setConditionToEnable(jt, viewerName, false);
		jp.add(jt);
		jt = new JTextField(Constants.labeledValueRE);
		setConditionToEnable(jt, viewerName, false);
		jp.add(jt);

		final int inputsN = labeledValueSet.size() + 1;
		int i = 0;
		final JTextField[] labelInputs = new JTextField[inputsN];
		final JTextField[] newIntInputs = new JTextField[inputsN];
		final Integer[] oldIntInputs = new Integer[inputsN];

		JTextField jtLabel, jtValue;
		if (labeledValueSet.size() > 0) {
			for (final Entry<Label, Integer> entry : labeledValueSet) {
				jl = new JLabel("Assigned Label " + i + ":");
				jtLabel = new JTextField(entry.getKey().toString());
				labelInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, false);
				jl.setLabelFor(jtLabel);
				jp.add(jl);
				jp.add(jtLabel);
				group.add(jtLabel, StringValidators.regexp(Constants.labelRE + "|", "Check the syntax!", false), Label.labelValidator);

				oldIntInputs[i] = entry.getValue();
				jtValue = new JTextField(oldIntInputs[i].toString());
				newIntInputs[i] = jtValue;
				setConditionToEnable(jtValue, viewerName, false);
				jp.add(jtValue);
				group.add(jtValue, StringValidators.regexp(Constants.labeledValueRE + "|", "Integer please or let it empty!", false));

				i++;
			}
		}
		// Show a row where it is possible to specify a new labeled value
		if (!distanceViewer) {
			jl = new JLabel("Labeled value " + i + ":");
			jtLabel = new JTextField();
			labelInputs[i] = jtLabel;
			setConditionToEnable(jtLabel, viewerName, false);
			jl.setLabelFor(jtLabel);
			jp.add(jl);
			jp.add(jtLabel);
			group.add(jtLabel, StringValidators.regexp(Constants.labelRE + "|", "Check the syntax!", false), Label.labelValidator);

			jtValue = new JTextField();
			newIntInputs[i] = jtValue;
			oldIntInputs[i] = null;
			setConditionToEnable(jtValue, viewerName, false);
			jp.add(jtValue);
			group.add(jtValue, StringValidators.regexp(Constants.labeledValueRE + "|", "Integer please or let it empty!", false));
		}

		// Show all upper and lower case values allowing also the possibility of insertion.
		final int nUpperLabels = e.upperLabelSize();
		final JTextField[] labelUpperInputs = new JTextField[nUpperLabels + 1];
		final JTextField[] newUpperValueInputs = new JTextField[nUpperLabels + 1];

		final int nLowerLabels = e.lowerLabelSize();
		final JTextField[] labelLowerInputs = new JTextField[nLowerLabels + 1];
		final JTextField[] newLowerValueInputs = new JTextField[nLowerLabels + 1];

		// If the edge type is contingent, then we allow the modification of the single possible lower/upper case value.
		// Show additional label

		jp.add(new JLabel("Syntax:"));
		jt = new JTextField("");
		setConditionToEnable(jt, viewerName, false);
		jp.add(jt);
		jt = new JTextField("<node Name>: <value>");
		setConditionToEnable(jt, viewerName, true);
		jp.add(jt);
		i = 0;
		if (nUpperLabels > 0) {
			for (final Entry<Entry<Label, String>, Integer> pair : e.getUpperLabelSet()) {
				// It should be only one! I put a cycle in order to verify
				jp.add(new JLabel("Upper Label"));
				jtLabel = new JTextField(pair.getKey().getKey().toString());
				labelUpperInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField(pair.getKey().getValue().toUpperCase() + ": " + pair.getValue());
				newUpperValueInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, distanceViewer);
				jp.add(jtLabel);
			}
		} else {
			if (!distanceViewer) {
				jp.add(new JLabel("Upper Label"));
				jtLabel = new JTextField("");
				labelUpperInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField("");
				newUpperValueInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, distanceViewer);
				jp.add(jtLabel);
			}
		}
		i = 0;
		if (nLowerLabels > 0) {
			for (final Entry<Entry<Label, String>, Integer> pair : e.getLowerLabelSet()) {
				// It should be only one! I put a cycle in order to verify
				jp.add(new JLabel("Lower Label"));
				jtLabel = new JTextField(pair.getKey().getKey().toString());
				labelLowerInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField(pair.getKey().getValue().toLowerCase() + ": " + pair.getValue());
				newLowerValueInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, false);
				jp.add(jtLabel);
			}
		} else {
			if (!distanceViewer) {
				jp.add(new JLabel("Lower Label"));
				jtLabel = new JTextField("");
				labelLowerInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField("");
				newLowerValueInputs[i] = jtLabel;
				setConditionToEnable(jtLabel, viewerName, false);
				jp.add(jtLabel);
			}
		}

		// Build the new object from the return values.
		boolean modified = false;
		if (panel.showOkCancelDialog("Attributes editor") && !distanceViewer) {
			String newValue = null;

			// Name
			newValue = name.getText();
			if (!e.getName().equals(newValue)) {
				e.setName(newValue);
				modified = true;
			}

			// Default value
			newValue = value.getText().trim();
			v = e.getInitialValue();
			if ((v == null) || !newValue.equals(v.toString())) {
				v = (newValue.isEmpty()) ? null : Integer.valueOf(newValue);
				LabelEditingGraphMousePlugin.LOG.finest("New default value: " + v);
				e.clear();
				e.setInitialValue(v);
				modified = true;
			}

			final Edge.Type t = (normalButton.isSelected()) ? Edge.Type.normal : (contingentButton.isSelected()) ? Edge.Type.contingent : Edge.Type.constraint;

			// manage edge type
			if (e.getType() != t) {
				e.setType(t);
				modified = true;
			}

			final LabeledValueMap<Integer> comp = new LabeledValueMap<>();
			Label l;
			String s, is;
			// It is more safe to build a new Label set and put it into the vertex
			// instead of substitute each different label: the mergeLabel method check the coherence of all labels
			for (i = 0; i < (inputsN - 1); i++) {
				s = labelInputs[i].getText();
				is = newIntInputs[i].getText();
				v = (is.length() > 0) ? Integer.valueOf(is) : null;
				LabelEditingGraphMousePlugin.LOG.finest("Label value" + i + ": (" + s + ", " + is + " [old:" + oldIntInputs[i] + "])");
				if ((s.length() == 0) || (v == null)) continue;
				l = Label.parse(s);
				comp.mergeLabeledValue(l, v);
			}
			s = (labelInputs[i] != null) ? labelInputs[i].getText() : "";
			if (s.length() != 0) {
				l = Label.parse(s);
				is = newIntInputs[i].getText();
				v = (is.length() > 0) ? Integer.valueOf(is) : null;
				LabelEditingGraphMousePlugin.LOG.finest("New label value: (" + l + ", " + v + ")");
				comp.mergeLabeledValue(l, v);
			}

			// I manage possible specification of lower/upper case new values
			String caseValue = null, nodeName = null;
			String[] splitted = null;
			if (e.isContingentEdge()) {
				e.clearLabels();
				// we consider only the first row
				// We don't use label because it has to be only one s = (labelUpperInputs[0] != null) ? labelUpperInputs[0].getText() : "";
				caseValue = newUpperValueInputs[0].getText();
				if (caseValue.length() != 0) {
					// the value is in the form "<node name>: <int>"
					splitted = caseValue.split(":[ ]*");
					nodeName = splitted[0].toUpperCase();
					v = Integer.valueOf(splitted[1]);
				} else {
					v = null;
				}
				if (nodeName == null) {
					e.clearUpperLabels();
				} else {
					Node source = g.getSource(e);
					Node dest = g.getDest(e);
					Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());
					if (source.getName().equalsIgnoreCase(nodeName)) {
						e.clearUpperLabels();
						e.mergeUpperLabelValue(endpointsLabel, source, v);// Temporally I ignore the label specified by user because an upper/lower case
																			// value of a contingent must have the label of its endpoints.
					}
				}
				// lower case
				// we consider only the first row
				// s = (labelLowerInputs[0] != null) ? labelLowerInputs[0].getText() : "";
				caseValue = newLowerValueInputs[0].getText();
				if (caseValue.length() != 0) {
					// the value is in the form "<node name>: <int>"
					if (caseValue.length() > 0) {
						splitted = caseValue.split(":[ ]*");
						nodeName = splitted[0].toLowerCase();
						v = Integer.valueOf(splitted[1]);
					} else {
						nodeName = null;
						v = null;
					}
					if (nodeName == null) {
						e.clearLowerLabels();
					} else {
						Node source = g.getSource(e);
						Node dest = g.getDest(e);
						Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());
						if (dest.getName().equalsIgnoreCase(nodeName)) {
							e.clearLowerLabels();
							e.mergeLowerLabelValue(endpointsLabel, dest, v);// Temporally I ignore the label specified by user because an upper/lower case
																			// value of a contingent must have the label of its endpoints.
						}
					}
				}
			} else {
				e.clearLowerLabels();
				e.clearUpperLabels();
			}
			if (!e.getLabeledValueMap().equals(comp)) {
				modified = true;
				LabelEditingGraphMousePlugin.LOG.finer("Original label set of the component: " + e.getLabeledValueMap());
				LabelEditingGraphMousePlugin.LOG.finer("New label set for the component: " + comp);
				e.clear();
				LabelEditingGraphMousePlugin.LOG.finer("Label set of the component after the clear: " + e.getLabeledValueMap());
				e.mergeLabeledValue(comp);
				LabelEditingGraphMousePlugin.LOG.finer("New label set assigned to the component: " + e.getLabeledValueMap());
			}
		}
		return modified;
	}

	/**
	 * For primary modifiers (default, MouseButton1):
	 * <ol>
	 * <li>Pick a single Vertex or Edge that is under the mouse pointer.<br>
	 * <li>If no Vertex or Edge is under the pointer, unselect all picked Vertices and Edges, and set up to draw a rectangle for multiple selection of contained
	 * Vertices.
	 * </ol>
	 * For additional selection (default Shift+MouseButton1):
	 * <ol>
	 * <li>Add to the selection, a single Vertex or Edge that is under the mouse pointer.
	 * <li>If a previously picked Vertex or Edge is under the pointer, it is un-picked.
	 * <li>If no vertex or Edge is under the pointer, set up to draw a multiple selection rectangle (as above) but do not unpick previously picked elements.
	 * </ol>
	 * 
	 * @param e the event
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void mouseClicked(MouseEvent e) {
		if ((e.getModifiers() == modifiers) && (e.getClickCount() == 2)) {
			final VisualizationViewer<N, E> vv = (VisualizationViewer<N, E>) e.getSource();
			JPanel jp2;
			final JLabel mesg2 = (JLabel) ((JPanel) vv.getParent().getParent().getParent().getComponent(0))
					.getComponent(1);
			mesg2.setIcon(null);
			mesg2.setText("");
			mesg2.setOpaque(false);

			if (!vv.getName().equals(Constants.distanceViewerName))
				// I didn't find a better way to determine the VisualizationViewer of distance
				// graphs inside this method.
				jp2 = (JPanel) (((JPanel) vv.getParent().getParent().getComponent(1)).getComponent(0));
			else
				jp2 = vv;
			final GraphElementAccessor<N, E> pickSupport = vv.getPickSupport();
			final String viewerName = vv.getName();
			if (pickSupport != null) {
				final Layout<N, E> layout = vv.getGraphLayout();
				final Graph g = (Graph) layout.getGraph();
				final Point2D p = e.getPoint(); // p is the screen point for the mouse event

				vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
				if (vertex != null) {
					if (nodeAttributesEditor(vertex, viewerName, g)) {
						jp2.setVisible(false);
						LabelEditingGraphMousePlugin.LOG
								.finer("The graph has been modified. Disable the distance viewer: " + jp2);
						g.cleanLowerEdgeCache();
						g.cleanPropositionCache();
					}
					vv.repaint();
					return;
				}

				// p is the screen point for the mouse event take away the view transform
				// Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
				edge = pickSupport.getEdge(layout, p.getX(), p.getY());
				if (edge != null) {
					if (edgeAttributesEditor(edge, viewerName, g)) {
						jp2.setVisible(false);
						LabelEditingGraphMousePlugin.LOG
								.finer("The graph has been modified. Disable the distance viewer: " + jp2);
						g.cleanLowerEdgeCache();
						g.cleanPropositionCache();
					}
					// clean everything
					vv.repaint();
					return;
				}
			}
			e.consume();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// empty
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// empty
	}

	/**
	 * If the mouse is over a picked vertex, drag all picked vertices with the mouse. If the mouse is not over a Vertex,
	 * draw the rectangle to select multiple
	 * Vertices
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// empty
	}

	/**
	 * If the mouse is dragging a rectangle, pick the Vertices contained in that rectangle clean up settings from
	 * mousePressed
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// empty
	}

	/**
	 * General method to setup a dialog to edit the attributes of a node.
	 * 
	 * @param node
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings("unchecked")
	private static boolean nodeAttributesEditor(Node node, String viewerName, Graph g) {

		// Planning a possible extension, a node could contains more labels with associated integers.
		// For now, we use only one entry and only the label part.

		final boolean distanceViewer = viewerName.equals("DistanceViewer");
		// Create a ValidationPanel - this is a panel that will show
		// any problem with the input at the bottom with an icon
		final ValidationPanel panel = new ValidationPanel();

		// Node contains only the possible observed proposition and the possible associated label.
		/*
		 * The layout is a grid of 3 columns.
		 */
		final JPanel jp = new JPanel(new GridLayout(0, 3));
		panel.setInnerComponent(jp);
		final ValidationGroup group = panel.getValidationGroup();

		// Name
		final JTextField name = new JTextField(node.getName());
		JLabel jl = new JLabel("Name:");
		jl.setLabelFor(name);
		jp.add(jl);
		jp.add(name);
		setConditionToEnable(name, viewerName, false);
		jp.add(new JLabel("Syntax: [A-Za-z0-9_]"));
		group.add(name, StringValidators.REQUIRE_NON_EMPTY_STRING);

		// Observed proposition
		JTextField observedProposition = new JTextField(1);
		Literal p = node.getObservable();
		observedProposition = new JTextField((p == null) ? "" : p.toString());
		jl = new JLabel("Observed proposition:");
		jl.setLabelFor(observedProposition);
		jp.add(jl);
		jp.add(observedProposition);
		setConditionToEnable(observedProposition, viewerName, false);
		jp.add(new JLabel("Syntax: [A-Za-z]| "));
		group.add(observedProposition, StringValidators.regexp("[A-Za-z]|", "Only One Char!", false),
				new ObservableValidator(g, node));

		// Label
		final Label l = node.getLabel();
		final JTextField label = new JTextField(l.toString());
		jl = new JLabel("Label:");
		jl.setLabelFor(label);
		jp.add(jl);
		jp.add(label);
		setConditionToEnable(label, viewerName, false);
		final JTextField jtf = new JTextField("Syntax: " + Constants.labelRE);
		jp.add(jtf);
		group.add(label, StringValidators.regexp(Constants.labelRE, "Check the syntax!", false), Label.labelValidator);

		// Build the new object from the return values.
		boolean modified = false;
		if (panel.showOkCancelDialog("Attributes editor") && !distanceViewer) {
			String newValue = null;

			// Name
			newValue = name.getText();
			if (!node.getName().equals(newValue)) {
				node.setName(newValue);
				modified = true;
			}

			// Observable
			newValue = observedProposition.getText();
			if (newValue != null) {
				final Literal oldP = node.getObservable();
				if (newValue.length() > 0) {
					p = new Literal(newValue.charAt(0));
					if ((oldP == null) || !oldP.equals(p)) {
						node.setObservable(p);
						modified = true;
					}
				} else if (oldP != null) {
					node.setObservable(null);
					modified = true;
				}
			}
			// Label
			newValue = label.getText();
			LabelEditingGraphMousePlugin.LOG.finest("New label for node " + node.getName() + ": " + newValue + ". Old: " + l.toString());
			if (!l.toString().equals(newValue)) {
				// syntax check allows a fast assignment!
				node.setLabel(Label.parse(newValue));
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Simple method to disable the editing of the property jc if forceDisable is true or if the viewerName is equals to "DistanceViewer".
	 * 
	 * @param jc
	 * @param viewerName
	 * @param forceDisable
	 */
	private static void setConditionToEnable(JComponent jc, String viewerName, boolean forceDisable) {
		if (forceDisable) {
			jc.setEnabled(false);
			return;
		}
		jc.setEnabled(!viewerName.equals("DistanceViewer"));
	}

}
