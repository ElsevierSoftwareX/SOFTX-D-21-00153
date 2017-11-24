/*
 * Copyright (c) 2005, the JUNG Project and the Regents of the University of California All rights reserved. This
 * software is open-source under the BSD license;
 * see either "license.txt" or http://jung.sourceforge.net/license.txt for a description. Created on Mar 8, 2005
 */
package it.univr.di.cstnu.visualization;

import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;
import it.univr.di.labeledvalue.Literal;

/**
 * Allows to edit vertex or edge attributes.
 *
 * @author Roberto Posenato.
 * @param <V> vertex type
 * @param <E> edge type
 * @version $Id: $Id
 */
public class LabelEditingGraphMousePlugin<V extends LabeledNode, E extends LabeledIntEdge>
		extends edu.uci.ics.jung.visualization.control.LabelEditingGraphMousePlugin<V, E> {

	/**
	 * General method to setup a dialog to edit the attributes of a vertex or of an edge.
	 *
	 * @param e
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings("unchecked")
	private static boolean edgeAttributesEditor(final LabeledIntEdge e, final String viewerName, final LabeledIntGraph g) {

		// LabeledIntEdge has a name, a default value (label for this value is determined by the conjunction of labels of its end-points and a type.

		final boolean editorPanel = viewerName.equals(CSTNEditor.editorName);
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
		LabelEditingGraphMousePlugin.setConditionToEnable(name, viewerName, false);
		jp.add(new JLabel("Syntax: [" + Literal.PROPOSITIONS + "0-9_]"));
		group.add(name, StringValidators.REQUIRE_NON_EMPTY_STRING);

		// Endpoints
		jp.add(new JLabel("Endpoints:"));
		jp.add(new JLabel(g.getSource(e) + "→"));
		jp.add(new JLabel("→" + g.getDest(e)));

		// Default Value
		Integer v = null;
		// Integer v = e.getInitialValue();
		// final JTextField value = new JTextField((v == null || v.equals(Constants.INT_POS_INFINITE)) ? "" : v.toString());
		// jl = new JLabel("Initial Value:");
		// jl.setLabelFor(value);
		// jp.add(jl);
		// jp.add(value);
		// setConditionToEnable(value, viewerName, false);
		// jp.add(new JLabel("Syntax: " + Constants.labeledValueRE));
		// group.add(value, StringValidators.regexp(Constants.labeledValueRE, "Check the syntax!", false));

		// Type
		jp.add(new JLabel("LabeledIntEdge type: "));
		final JRadioButton normalButton = new JRadioButton(LabeledIntEdge.ConstraintType.normal.toString());
		normalButton.setActionCommand(LabeledIntEdge.ConstraintType.normal.toString());
		normalButton.setSelected(e.getConstraintType() == LabeledIntEdge.ConstraintType.normal);
		LabelEditingGraphMousePlugin.setConditionToEnable(normalButton, viewerName, false);
		final JRadioButton contingentButton = new JRadioButton(LabeledIntEdge.ConstraintType.contingent.toString());
		contingentButton.setActionCommand(LabeledIntEdge.ConstraintType.contingent.toString());
		contingentButton.setSelected(e.isContingentEdge());
		LabelEditingGraphMousePlugin.setConditionToEnable(contingentButton, viewerName, false);
		final JRadioButton constraintButton = new JRadioButton(LabeledIntEdge.ConstraintType.constraint.toString());
		constraintButton.setActionCommand(LabeledIntEdge.ConstraintType.constraint.toString());
		constraintButton.setSelected(e.getConstraintType() == LabeledIntEdge.ConstraintType.constraint);
		LabelEditingGraphMousePlugin.setConditionToEnable(constraintButton, viewerName, false);
		final JRadioButton derivedButton = new JRadioButton(LabeledIntEdge.ConstraintType.derived.toString());
		derivedButton.setActionCommand(LabeledIntEdge.ConstraintType.derived.toString());
		derivedButton
				.setSelected(e.getConstraintType() == LabeledIntEdge.ConstraintType.derived || e.getConstraintType() == LabeledIntEdge.ConstraintType.internal);
		derivedButton.setEnabled(false);

		// Group the radio buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalButton);
		buttonGroup.add(contingentButton);
		buttonGroup.add(constraintButton);
		buttonGroup.add(derivedButton);

		jp.add(normalButton);
		jp.add(contingentButton);
		jp.add(new JLabel(""));// in order to jumb a cell
		jp.add(constraintButton);
		jp.add(derivedButton);
		// LabelEditingGraphMousePlugin.setConditionToEnable(jp, viewerName, false);

		// Show possible labeled values
		final Set<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label>> labeledValueSet = e.getLabeledValueSet();
		JTextField jt;
		jp.add(new JLabel("Labeled value syntax:"));
		jt = new JTextField(Label.LABEL_RE);
		LabelEditingGraphMousePlugin.setConditionToEnable(jt, viewerName, false);
		jp.add(jt);
		jt = new JTextField(Constants.LabeledValueRE);
		LabelEditingGraphMousePlugin.setConditionToEnable(jt, viewerName, false);
		jp.add(jt);

		final int inputsN = labeledValueSet.size() + 1;
		int i = 0;
		final JTextField[] labelInputs = new JTextField[inputsN];
		final JTextField[] newIntInputs = new JTextField[inputsN];
		final Integer[] oldIntInputs = new Integer[inputsN];

		JTextField jtLabel, jtValue;
		if (labeledValueSet.size() > 0) {
			for (final Entry<Label> entry : labeledValueSet) {
				jl = new JLabel("Assigned Label " + i + ":");
				jtLabel = new JTextField(entry.getKey().toString());
				labelInputs[i] = jtLabel;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, false);
				jl.setLabelFor(jtLabel);
				jp.add(jl);
				jp.add(jtLabel);
				group.add(jtLabel, StringValidators.regexp(Label.LABEL_RE + "|", "Check the syntax!", false), Label.labelValidator);

				oldIntInputs[i] = entry.getIntValue();
				jtValue = new JTextField(Constants.formatInt(oldIntInputs[i]));
				newIntInputs[i] = jtValue;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtValue, viewerName, false);
				jp.add(jtValue);
				group.add(jtValue, StringValidators.regexp(Constants.LabeledValueRE + "|", "Integer please or let it empty!", false));

				i++;
			}
		}
		// Show a row where it is possible to specify a new labeled value
		if (editorPanel) {
			jl = new JLabel("Labeled value " + i + ":");
			jtLabel = new JTextField();
			labelInputs[i] = jtLabel;
			LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, false);
			jl.setLabelFor(jtLabel);
			jp.add(jl);
			jp.add(jtLabel);
			group.add(jtLabel, StringValidators.regexp(Label.LABEL_RE + "|", "Check the syntax!", false), Label.labelValidator);

			jtValue = new JTextField();
			newIntInputs[i] = jtValue;
			oldIntInputs[i] = null;
			LabelEditingGraphMousePlugin.setConditionToEnable(jtValue, viewerName, false);
			jp.add(jtValue);
			group.add(jtValue, StringValidators.regexp(Constants.LabeledValueRE + "|", "Integer please or let it empty!", false));
		}

		// Show all upper and lower case values allowing also the possibility of insertion.
		final int nUpperLabels = e.upperCaseValueSize();
		final JTextField[] labelUpperInputs = new JTextField[nUpperLabels + 1];
		final JTextField[] newUpperValueInputs = new JTextField[nUpperLabels + 1];

		LabeledLowerCaseValue lowerValue = e.getLowerCaseValue();
		final int nLowerLabels = (lowerValue.isEmpty()) ? 0 : 1;
		final JTextField[] labelLowerInputs = new JTextField[1];
		final JTextField[] newLowerValueInputs = new JTextField[1];

		// If the edge type is contingent, then we allow the modification of the single possible lower/upper case value.
		// Show additional label

		jp.add(new JLabel("Syntax:"));
		jt = new JTextField("Label (read-only)");
		LabelEditingGraphMousePlugin.setConditionToEnable(jt, viewerName, true);
		jp.add(jt);
		jt = new JTextField("<node Name>: <value>");
		LabelEditingGraphMousePlugin.setConditionToEnable(jt, viewerName, true);
		jp.add(jt);
		i = 0;
		if (nUpperLabels > 0) {
			for (ALabel alabel : e.getUpperCaseValueMap().keySet()) {
				LabeledIntTreeMap labeledValues = e.getUpperCaseValueMap().get(alabel);
				for (Object2IntMap.Entry<Label> entry1 : labeledValues.entrySet()) {
					// It should be only one! I put a cycle in order to verify

					jp.add(new JLabel("Upper Label"));
					jtLabel = new JTextField(entry1.getKey().toString());
					labelUpperInputs[i] = jtLabel;
					LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, true);
					jp.add(jtLabel);
					jtLabel = new JTextField(alabel.toString() + ": " + Constants.formatInt(entry1.getIntValue()));
					newUpperValueInputs[i] = jtLabel;
					LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, editorPanel);
					jp.add(jtLabel);
				}
			}
		} else {
			if (editorPanel) {
				jp.add(new JLabel("Upper Label"));
				jtLabel = new JTextField("");
				labelUpperInputs[i] = jtLabel;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField("");
				newUpperValueInputs[i] = jtLabel;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, editorPanel);
				jp.add(jtLabel);
			}
		}
		i = 0;
		if (nLowerLabels == 1) {
			// for (java.util.Map.Entry<ALabel, LabeledIntTreeMap> entry : e.getLowerLabelSet()) {
			// LabeledIntTreeMap labeledValues = entry.getValue();
			// for (Object2IntMap.Entry<Label> entry1 : labeledValues.entrySet()) {
			// It should be only one! I put a cycle in order to verify
			jp.add(new JLabel("Lower Label"));
			jtLabel = new JTextField(lowerValue.getLabel().toString());// entry1.getKey().toString());
			labelLowerInputs[i] = jtLabel;
			LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, true);
			jp.add(jtLabel);
			jtLabel = new JTextField(lowerValue.getNodeName().toString() + ": " + Constants.formatInt(lowerValue.getValue()));
			newLowerValueInputs[i] = jtLabel;
			LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, false);
			jp.add(jtLabel);
			// }
			// }
		} else {
			if (editorPanel) {
				jp.add(new JLabel("Lower Label"));
				jtLabel = new JTextField("");
				labelLowerInputs[i] = jtLabel;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, true);
				jp.add(jtLabel);
				jtLabel = new JTextField("");
				newLowerValueInputs[i] = jtLabel;
				LabelEditingGraphMousePlugin.setConditionToEnable(jtLabel, viewerName, false);
				jp.add(jtLabel);
			}
		}

		// Build the new object from the return values.
		boolean modified = false;
		if (panel.showOkCancelDialog("Attributes editor") && editorPanel) {
			String newValue = null;

			// Name
			newValue = name.getText();
			if (!e.getName().equals(newValue)) {
				e.setName(newValue);
				modified = true;
			}

			// Default value
			// newValue = value.getText().trim();
			// v = e.getInitialValue();
			// if ((v == null) || !newValue.equals(v.toString())) {
			// v = (newValue.isEmpty()) ? null : Integer.valueOf(newValue);
			// LabelEditingGraphMousePlugin.LOG.finest("New default value: " + v);
			// e.clear();
			// e.setInitialValue(v);
			// modified = true;
			// }

			final LabeledIntEdge.ConstraintType t = (normalButton.isSelected()) ? LabeledIntEdge.ConstraintType.normal
					: (contingentButton.isSelected()) ? LabeledIntEdge.ConstraintType.contingent : LabeledIntEdge.ConstraintType.constraint;

			// manage edge type
			if (e.getConstraintType() != t) {
				e.setConstraintType(t);
				modified = true;
			}

			LabeledIntMapFactory<LabeledIntMap> mapFactory = new LabeledIntMapFactory<>();
			final LabeledIntMap comp = mapFactory.get();
			Label l;
			String s, is;
			// It is more safe to build a new Label set and put substitute the old one with the present.
			for (i = 0; i < (inputsN - 1); i++) {
				s = labelInputs[i].getText();
				is = newIntInputs[i].getText();
				v = (is.length() > 0) ? Integer.valueOf(is) : null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finest("Label value" + i + ": (" + s + ", " + is + " [old:" + oldIntInputs[i] + "])");
					}
				}
				if (v == null)
					continue; // if label is null or empty, the value is the default value!
				l = ((s == null) || (s.length() == 0)) ? Label.emptyLabel : Label.parse(s);
				comp.put(l, v);
			}
			// the row representing possible new value can have the two fields null!
			is = (newIntInputs[i] != null) ? newIntInputs[i].getText() : "";
			if (is.length() != 0) {
				l = (labelInputs[i] != null) ? Label.parse(labelInputs[i].getText()) : Label.emptyLabel;
				is = newIntInputs[i].getText();
				v = Integer.valueOf(is);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finest("New label value: (" + l + ", " + v + ")");
					}
				}
				comp.put(l, v);
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
					if (splitted.length < 2) {
						v = null;
					} else {
						// nodeName = splitted[0].toUpperCase();
						nodeName = splitted[0];
						if (nodeName != null && nodeName.isEmpty())
							nodeName = null;
						else {
							if (g.getNode(nodeName) == null) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.SEVERE)) {
										LabelEditingGraphMousePlugin.LOG
												.severe("ALabel " + nodeName + " does not correspond to a node name. Abort!" + caseValue);
									}
								}
								nodeName = null;
							}
						}
						v = Integer.valueOf(splitted[1]);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST)) {
								LabelEditingGraphMousePlugin.LOG.finest("New Upper value input: " + nodeName + ": " + v + ".");
							}
						}
					}
				} else {
					e.clearUpperCaseValues();
				}
				if (nodeName == null || v == null) {
					e.clearUpperCaseValues();
				} else {
					final LabeledNode source = g.getSource(e);
					final LabeledNode dest = g.getDest(e);
					final Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());
					ALabel alabel = new ALabel(new ALetter(source.getName()), g.getALabelAlphabet());
					if (alabel.toString().equals(nodeName)) {
						e.clearUpperCaseValues();
						e.mergeUpperCaseValue(endpointsLabel, alabel, v);// Temporally I ignore the label specified by user because an upper/lower case
						// value of a contingent must have the label of its endpoints.
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINEST)) {
								LabelEditingGraphMousePlugin.LOG.finest("Merged Upper value input: " + endpointsLabel + ", " + alabel + ": " + v + ".");
							}
						}
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
						if (splitted.length < 2) {
							nodeName = null;
							v = null;
						} else {
							// nodeName = splitted[0].toLowerCase();
							nodeName = splitted[0];
							if (nodeName != null && nodeName.isEmpty())
								nodeName = null;
							else {
								if (g.getNode(nodeName) == null) {
									if (Debug.ON) {
										if (LOG.isLoggable(Level.SEVERE)) {
											LabelEditingGraphMousePlugin.LOG.severe("ALabel " + nodeName + " does not correspond to a node name. Abort!");
										}
									}
									nodeName = null;
								}
							}
							v = Integer.valueOf(splitted[1]);
						}
					} else {
						nodeName = null;
						v = null;
					}
					if ((nodeName == null) || (v == null)) {
						e.clearLowerCaseValue();
					} else {
						final LabeledNode source = g.getSource(e);
						final LabeledNode dest = g.getDest(e);
						final Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());

						if (dest.getName().equals(nodeName)) {
							e.clearLowerCaseValue();
							ALabel destALabel = (dest.getAlabel() != null) ? new ALabel(dest.getAlabel()) : new ALabel(dest.getName(), null);
							dest.setAlabel(destALabel);
							e.setLowerCaseValue(endpointsLabel, destALabel, v);// Temporally I ignore the label specified by user because an upper/lower case
							// value of a contingent must have the label of its endpoints.
						}
					}
				} else {
					e.clearLowerCaseValue();
				}
			} else {
				e.clearLowerCaseValue();
				e.clearUpperCaseValues();
			}
			if (!e.getLabeledValueMap().equals(comp)) {
				modified = true;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finer("Original label set of the component: " + e.getLabeledValueMap());
						LabelEditingGraphMousePlugin.LOG.finer("New label set for the component: " + comp);
					}
				}
				e.clearLabels();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finer("Label set of the component after the clear: " + e.getLabeledValueMap());
					}
				}
				e.mergeLabeledValue(comp);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finer("New label set assigned to the component: " + e.getLabeledValueMap());
					}
				}
			}
		}
		return modified;
	}

	/**
	 * General method to setup a dialog to edit the attributes of a node.
	 *
	 * @param node
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings({ "unchecked", "static-method" })
	private boolean nodeAttributesEditor(final LabeledNode node, final String viewerName, final LabeledIntGraph g) {

		// Planning a possible extension, a node could contains more labels with associated integers.
		// For now, we use only one entry and only the label part.

		final boolean editorPanel = viewerName.equals(CSTNEditor.editorName);
		// Create a ValidationPanel - this is a panel that will show
		// any problem with the input at the bottom with an icon
		final ValidationPanel panel = new ValidationPanel();

		// LabeledNode contains only the possible observed proposition and the possible associated label.
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
		LabelEditingGraphMousePlugin.setConditionToEnable(name, viewerName, false);
		jp.add(new JLabel("Syntax: [" + ALabelAlphabet.ALETTER + "?]+"));
		group.add(name, StringValidators.regexp("[" + ALabelAlphabet.ALETTER + "?]+", "Must be a well format name", false), new ObservableValidator(g, node));

		// Observed proposition
		JTextField observedProposition = new JTextField(1);
		char p = node.getPropositionObserved();
		observedProposition = new JTextField((p == Constants.UNKNOWN) ? "" : "" + p);
		jl = new JLabel("Observed proposition:");
		jl.setLabelFor(observedProposition);
		jp.add(jl);
		jp.add(observedProposition);
		LabelEditingGraphMousePlugin.setConditionToEnable(observedProposition, viewerName, false);
		jp.add(new JLabel("Syntax: " + Literal.PROPOSITION_RANGE + "| "));
		group.add(observedProposition, StringValidators.regexp(Literal.PROPOSITION_RANGE + "|", "Must be a single char in the range!", false),
				new ObservableValidator(g, node));

		// Label
		final Label l = node.getLabel();
		final JTextField label = new JTextField(l.toString());
		jl = new JLabel("Label:");
		jl.setLabelFor(label);
		jp.add(jl);
		jp.add(label);
		LabelEditingGraphMousePlugin.setConditionToEnable(label, viewerName, false);
		final JTextField jtf = new JTextField("Syntax: " + Label.LABEL_RE);
		jp.add(jtf);
		group.add(label, StringValidators.regexp(Label.LABEL_RE, "Check the syntax!", false), Label.labelValidator);

		// Build the new object from the return values.
		boolean modified = false;
		if (panel.showOkCancelDialog("Attributes editor") && editorPanel) {
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
				final char oldP = node.getPropositionObserved();
				if (newValue.length() > 0) {
					p = newValue.charAt(0);
					if ((oldP == Constants.UNKNOWN) || oldP != p) {
						node.setObservable(p);
						modified = true;
					}
				} else if (oldP != Constants.UNKNOWN) {
					node.setObservable(Constants.UNKNOWN);
					modified = true;
				}
			}
			// Label
			newValue = label.getText();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LabelEditingGraphMousePlugin.LOG.finest("New label for node " + node.getName() + ": " + newValue + ". Old: " + l.toString());
				}
			}
			if (!l.toString().equals(newValue)) {
				// syntax check allows a fast assignment!
				node.setLabel(Label.parse(newValue));
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Simple method to disable the editing of the property jc if forceDisable is true or if the viewerName is not equals to CSTNEditor.editorName.
	 *
	 * @param jc
	 * @param viewerName
	 * @param forceDisable
	 */
	private static void setConditionToEnable(final JComponent jc, final String viewerName, final boolean forceDisable) {
		if (forceDisable) {
			jc.setEnabled(false);
			return;
		}
		jc.setEnabled(viewerName.equals(CSTNEditor.editorName));
	}

	/**
	 * logger della classe
	 */
	static Logger LOG = Logger.getLogger(LabelEditingGraphMousePlugin.class.getName());

	/**
	 * The editor in which this plugin works.
	 */
	CSTNEditor cstnEditor;

	/**
	 * create an instance with default settings
	 * 
	 * @param cstnEditor
	 */
	public LabelEditingGraphMousePlugin(CSTNEditor cstnEditor) {
		super(InputEvent.BUTTON1_MASK);
		this.cstnEditor = cstnEditor;
	}

	/**
	 * Create an instance with overrides.
	 *
	 * @param selectionModifiers for primary selection
	 * @param cstnEditor
	 */
	public LabelEditingGraphMousePlugin(final int selectionModifiers, CSTNEditor cstnEditor) {
		super(selectionModifiers);
		this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		this.cstnEditor = cstnEditor;
	}

	/**
	 * {@inheritDoc}
	 * For primary modifiers (default, MouseButton1):
	 * <ol>
	 * <li>Pick a single Vertex or LabeledIntEdge that is under the mouse pointer.<br>
	 * <li>If no Vertex or LabeledIntEdge is under the pointer, unselect all picked Vertices and Edges, and set up to draw a rectangle for multiple selection of
	 * contained Vertices.
	 * </ol>
	 * For additional selection (default Shift+MouseButton1):
	 * <ol>
	 * <li>Add to the selection, a single Vertex or LabeledIntEdge that is under the mouse pointer.
	 * <li>If a previously picked Vertex or LabeledIntEdge is under the pointer, it is un-picked.
	 * <li>If no vertex or LabeledIntEdge is under the pointer, set up to draw a multiple selection rectangle (as above) but do not unpick previously picked
	 * elements.
	 * </ol>
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void mouseClicked(final MouseEvent e) {
		if ((e.getModifiers() == this.modifiers) && (e.getClickCount() == 2)) {
			final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
			JPanel jp2;
			final JEditorPane mesg2 = this.cstnEditor.derivedGraphMessageArea;

			if (vv.getName().equals(CSTNEditor.editorName)) {
				jp2 = this.cstnEditor.vv2;
			} else {
				jp2 = vv;
			}
			final GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
			final String viewerName = vv.getName();
			if (pickSupport != null) {
				final Layout<V, E> layout = vv.getGraphLayout();
				final LabeledIntGraph g = (LabeledIntGraph) layout.getGraph();
				final Point2D p = e.getPoint(); // p is the screen point for the mouse event

				this.vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
				if (this.vertex != null) {
					if (nodeAttributesEditor(this.vertex, viewerName, g)) {
						jp2.setVisible(false);
						mesg2.setText("");
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LabelEditingGraphMousePlugin.LOG.finer("The graph has been modified. Disable the distance viewer: " + jp2);
							}
						}
						g.clearCache();
					}
					vv.repaint();
					return;
				}

				// p is the screen point for the mouse event take away the view transform
				// Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
				this.edge = pickSupport.getEdge(layout, p.getX(), p.getY());
				if (this.edge != null) {
					if (edgeAttributesEditor(this.edge, viewerName, g)) {
						jp2.setVisible(false);
						mesg2.setText("");
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LabelEditingGraphMousePlugin.LOG.finer("The graph has been modified. Disable the distance viewer: " + jp2);
							}
						}
						g.clearCache();
					}
					vv.repaint();
					return;
				}
			}
			e.consume();
		}
	}
}
