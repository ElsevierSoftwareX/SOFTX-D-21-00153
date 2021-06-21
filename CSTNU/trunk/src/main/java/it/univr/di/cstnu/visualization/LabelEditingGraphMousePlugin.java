// Copyright (c) 2005, the JUNG Project and the Regents of the University of California All rights reserved. This
// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.visualization;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.BasicCSTNUEdge;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.CSTNPSUEdge;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNUEdge;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraph.NetworkType;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
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
public class LabelEditingGraphMousePlugin<V extends LabeledNode, E extends Edge>
		extends edu.uci.ics.jung.visualization.control.LabelEditingGraphMousePlugin<V, E> {

	/**
	 * logger della classe
	 */
	static Logger LOG = Logger.getLogger(LabelEditingGraphMousePlugin.class.getName());

	/**
	 * General method to setup a dialog to edit the attributes of a vertex or of an edge.
	 *
	 * @param e
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings({ "unchecked", "null" })
	private static <E extends Edge> boolean edgeAttributesEditor(final E e, final String viewerName, final TNGraph<E> g) {

		// Edge has a name, a default value (label for this value is determined by the conjunction of labels of its end-points and a type.

		final boolean editorPanel = viewerName.equals(CSTNEditor.EDITOR_NAME);
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
		jp.add(new JLabel("RE: [" + Literal.PROPOSITIONS + "0-9_]"));
		group.add(name, StringValidators.REQUIRE_NON_EMPTY_STRING);

		// Endpoints
		LabeledNode sourceNode = g.getSource(e), destNode = g.getDest(e);

		jp.add(new JLabel("Endpoints:"));
		jp.add(new JLabel(sourceNode + "→"));
		jp.add(new JLabel("→" + destNode));

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
		// Group the radio buttons.
		final ButtonGroup buttonGroup = new ButtonGroup();
		jp.add(new JLabel("Edge type: "));
		final JRadioButton normalButton = new JRadioButton(Edge.ConstraintType.normal.toString());
		normalButton.setActionCommand(Edge.ConstraintType.normal.toString());
		normalButton.setSelected(e.getConstraintType() == Edge.ConstraintType.normal);
		setConditionToEnable(normalButton, viewerName, false);
		jp.add(normalButton);
		buttonGroup.add(normalButton);

		final JRadioButton contingentButton = new JRadioButton(Edge.ConstraintType.contingent.toString());
		if (g.getType() == NetworkType.CSTNU || g.getType() == NetworkType.CSTNPSU || g.getType() == NetworkType.STNU) {
			contingentButton.setActionCommand(Edge.ConstraintType.contingent.toString());
			contingentButton.setSelected(e.isContingentEdge());
			setConditionToEnable(contingentButton, viewerName, false);
			jp.add(contingentButton);
			jp.add(new JLabel(""));// in order to jump a cell
			buttonGroup.add(contingentButton);
		}

		final JRadioButton constraintButton = new JRadioButton(Edge.ConstraintType.constraint.toString());
		constraintButton.setActionCommand(Edge.ConstraintType.constraint.toString());
		constraintButton.setSelected(e.getConstraintType() == Edge.ConstraintType.constraint);
		setConditionToEnable(constraintButton, viewerName, false);
		jp.add(constraintButton);
		if (g.getType() == NetworkType.STN) {
			jp.add(new JLabel(""));// in order to jump a cell
		}
		buttonGroup.add(constraintButton);

		final JRadioButton derivedButton = new JRadioButton(Edge.ConstraintType.derived.toString());
		derivedButton.setActionCommand(Edge.ConstraintType.derived.toString());
		derivedButton
				.setSelected(e.getConstraintType() == Edge.ConstraintType.derived || e.getConstraintType() == Edge.ConstraintType.internal);
		derivedButton.setEnabled(false);
		jp.add(derivedButton);
		buttonGroup.add(derivedButton);

		JTextField jt;
		int i = 0;
		JTextField jtLabel, jtValue;

		int inputsN = -1;
		JTextField[] labelInputs = null;
		JTextField[] newIntInputs = null;
		Integer[] oldIntInputs = null;

		if (e.isSTNEdge() || e.isSTNUEdge()) {
			inputsN = 1;
			newIntInputs = new JTextField[inputsN];
			oldIntInputs = new Integer[inputsN];

			// Show value label
			// jp.add(new JLabel(""));// in order to jump a cell
			jp.add(new JLabel("Value: "));// in order to jump a cell
			// Show value
			oldIntInputs[0] = ((STNEdge) e).getValue();
			jtValue = new JTextField(Constants.formatInt(oldIntInputs[0]));
			newIntInputs[0] = jtValue;
			setConditionToEnable(jtValue, viewerName, false);
			jp.add(jtValue);
			group.add(jtValue, StringValidators.regexp(Constants.LabeledValueRE + "|", "Integer please or let it empty!", false));
			// Show syntax
			jt = new JTextField("RE: " + Constants.LabeledValueRE);
			setConditionToEnable(jt, viewerName, true);
			jp.add(jt);

			// if (editorPanel) {
			// jtValue = new JTextField();
			// newIntInputs[i] = jtValue;
			// oldIntInputs[i] = null;
			// setConditionToEnable(jtValue, viewerName, false);
			// jp.add(jtValue);
			// group.add(jtValue, StringValidators.regexp(Constants.LabeledValueRE + "|", "Integer please or let it empty!", false));
			// }
		}

		if (e.isSTNUEdge()) {
			STNUEdge e1 = (STNUEdge) e;
			String labeledValue = e1.getLabeledValueFormatted();
			if (!labeledValue.isEmpty()) {
				// jp.add(new JLabel(""));// in order to jump a cell
				jp.add(new JLabel("Case value: "));// in order to jump a cell
				jtValue = new JTextField(labeledValue);
				setConditionToEnable(jtValue, viewerName, true);
				jp.add(jtValue);
			}
		}

		if (e.isCSTNEdge()) {
			// Show possible labeled values
			final Set<it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label>> labeledValueSet = ((CSTNEdge) e).getLabeledValueSet();
			jp.add(new JLabel("Labeled value syntax:"));
			jt = new JTextField(Label.LABEL_RE);
			setConditionToEnable(jt, viewerName, false);
			jp.add(jt);
			jt = new JTextField(Constants.LabeledValueRE);
			setConditionToEnable(jt, viewerName, false);
			jp.add(jt);

			inputsN = labeledValueSet.size() + 1;
			labelInputs = new JTextField[inputsN];
			newIntInputs = new JTextField[inputsN];
			oldIntInputs = new Integer[inputsN];

			if (labeledValueSet.size() > 0) {
				for (final Entry<Label> entry : labeledValueSet) {
					jl = new JLabel("Assigned Label " + i + ":");
					jtLabel = new JTextField(entry.getKey().toString());
					labelInputs[i] = jtLabel;
					setConditionToEnable(jtLabel, viewerName, false);
					jl.setLabelFor(jtLabel);
					jp.add(jl);
					jp.add(jtLabel);
					group.add(jtLabel, StringValidators.regexp(Label.LABEL_RE + "|", "Check the syntax!", false), Label.labelValidator);

					oldIntInputs[i] = entry.getIntValue();
					jtValue = new JTextField(Constants.formatInt(oldIntInputs[i]));
					newIntInputs[i] = jtValue;
					setConditionToEnable(jtValue, viewerName, false);
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
				setConditionToEnable(jtLabel, viewerName, false);
				jl.setLabelFor(jtLabel);
				jp.add(jl);
				jp.add(jtLabel);
				group.add(jtLabel, StringValidators.regexp(Label.LABEL_RE + "|", "Check the syntax!", false), Label.labelValidator);

				jtValue = new JTextField();
				newIntInputs[i] = jtValue;
				oldIntInputs[i] = null;
				setConditionToEnable(jtValue, viewerName, false);
				jp.add(jtValue);
				group.add(jtValue, StringValidators.regexp(Constants.LabeledValueRE + "|", "Integer please or let it empty!", false));
			}
		}

		int nUpperLabels = 0, nLowerLabels = 0;
		JTextField[] labelUpperInputs = null;
		JTextField[] newUpperValueInputs = null;
		JTextField[] labelLowerInputs = null;
		JTextField[] newLowerValueInputs = null;

		if (e.isCSTNUEdge() || e.isCSTNPSUEdge()) {
			nUpperLabels = ((BasicCSTNUEdge) e).upperCaseValueSize();

			LabeledLowerCaseValue lowerValue = null;
			nLowerLabels = ((BasicCSTNUEdge) e).lowerCaseValueSize();

			if (e.isContingentEdge() || nUpperLabels > 0 || nLowerLabels > 0) {
				// Show all upper and lower case values allowing also the possibility of insertion.
				BasicCSTNUEdge e1 = (BasicCSTNUEdge) e;
				labelUpperInputs = new JTextField[(nUpperLabels == 0) ? 1 : nUpperLabels];
				newUpperValueInputs = new JTextField[(nUpperLabels == 0) ? 1 : nUpperLabels];

				// lowerValue = e1.getLowerCaseValue();
				labelLowerInputs = new JTextField[(nLowerLabels == 0) ? 1 : nLowerLabels];
				newLowerValueInputs = new JTextField[(nLowerLabels == 0) ? 1 : nLowerLabels];

				// If the edge type is contingent, then we allow the modification of the single possible lower/upper case value.
				// Show additional label

				jp.add(new JLabel("Syntax:"));
				jt = new JTextField("Label (read-only)");
				setConditionToEnable(jt, viewerName, true);
				jp.add(jt);
				jt = new JTextField("<node Name>: <value>");
				setConditionToEnable(jt, viewerName, true);
				jp.add(jt);
				i = 0;
				if (nUpperLabels > 0) {
					for (ALabel alabel : e1.getUpperCaseValueMap().keySet()) {
						LabeledIntTreeMap labeledValues = e1.getUpperCaseValueMap().get(alabel);
						for (Object2IntMap.Entry<Label> entry1 : labeledValues.entrySet()) {
							// It should be only one! I put a cycle in order to verify
							jp.add(new JLabel("Upper Label"));
							jtLabel = new JTextField(entry1.getKey().toString());
							labelUpperInputs[i] = jtLabel;
							setConditionToEnable(jtLabel, viewerName, true);
							jp.add(jtLabel);
							jtLabel = new JTextField(alabel.toString() + ": " + Constants.formatInt(entry1.getIntValue()));
							newUpperValueInputs[i] = jtLabel;
							setConditionToEnable(jtLabel, viewerName, (nUpperLabels > 1) ? true : false);
							jp.add(jtLabel);
							group.add(jtLabel, StringValidators.regexp("^" + sourceNode.getName() + "\\s*:.*" + "|", "Contingent name is wrong!", false));
							i++;
						}
					}
				} else {
					jp.add(new JLabel("Upper Label"));
					jtLabel = new JTextField("");
					labelUpperInputs[i] = jtLabel;
					setConditionToEnable(jtLabel, viewerName, true);
					jp.add(jtLabel);
					jtLabel = new JTextField("");
					newUpperValueInputs[i] = jtLabel;
					setConditionToEnable(jtLabel, viewerName, !editorPanel);
					jp.add(jtLabel);
					group.add(jtLabel, StringValidators.regexp("^" + sourceNode.getName() + ":.*" + "|",
							"Contingent name is wrong or it is not followed by : without spaces!", false));
				}
				i = 0;
				if (nLowerLabels > 0) {
					if (e1.isCSTNUEdge()) {
						lowerValue = e1.getLowerCaseValue();
						jp.add(new JLabel("Lower Label"));
						jtLabel = new JTextField(lowerValue.getLabel().toString());// entry1.getKey().toString());
						labelLowerInputs[i] = jtLabel;
						setConditionToEnable(jtLabel, viewerName, true);
						jp.add(jtLabel);
						jtLabel = new JTextField(lowerValue.getNodeName().toString() + ": " + Constants.formatInt(lowerValue.getValue()));
						newLowerValueInputs[i] = jtLabel;
						setConditionToEnable(jtLabel, viewerName, false);
						group.add(jtLabel, StringValidators.regexp("^" + destNode.getName() + "\\s*:.*" + "|", "Contingent name is wrong!", false));
						jp.add(jtLabel);
					} else {
						// CSTNPSU edge
						CSTNPSUEdge e2 = (CSTNPSUEdge) e1;
						for (ALabel alabel : e2.getLowerCaseValueMap().keySet()) {
							LabeledIntTreeMap labeledValues = e2.getLowerCaseValueMap().get(alabel);
							for (Object2IntMap.Entry<Label> entry1 : labeledValues.entrySet()) {
								// It should be only one! I put a cycle in order to verify
								jp.add(new JLabel("Lower Label"));
								jtLabel = new JTextField(entry1.getKey().toString());
								labelLowerInputs[i] = jtLabel;
								setConditionToEnable(jtLabel, viewerName, true);
								jp.add(jtLabel);
								jtLabel = new JTextField(alabel.toString() + ": " + Constants.formatInt(entry1.getIntValue()));
								newLowerValueInputs[i] = jtLabel;
								setConditionToEnable(jtLabel, viewerName, (nLowerLabels > 1) ? true : false);
								jp.add(jtLabel);
								group.add(jtLabel, StringValidators.regexp("^" + destNode.getName() + "\\s*:.*" + "|", "Contingent name is wrong!", false));
								i++;
							}
						}
					}
				} else {
					if (editorPanel) {
						jp.add(new JLabel("Lower Label"));
						jtLabel = new JTextField("");
						labelLowerInputs[i] = jtLabel;
						setConditionToEnable(jtLabel, viewerName, true);
						jp.add(jtLabel);
						jtLabel = new JTextField("");
						newLowerValueInputs[i] = jtLabel;
						setConditionToEnable(jtLabel, viewerName, false);
						group.add(jtLabel, StringValidators.regexp("^" + destNode.getName() + "\\s*:.*" + "|", "Contingent name is wrong!", false));
						jp.add(jtLabel);
					}
				}
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

			final Edge.ConstraintType t = (normalButton.isSelected()) ? Edge.ConstraintType.normal
					: (contingentButton.isSelected()) ? Edge.ConstraintType.contingent : Edge.ConstraintType.constraint;

			// manage edge type
			if (e.getConstraintType() != t) {
				e.setConstraintType(t);
				modified = true;
			}

			if (e.isSTNEdge()) {
				STNEdge e1 = (STNEdge) e;
				String is = newIntInputs[0].getText();
				v = (is.length() > 0) ? Integer.valueOf(is) : null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finest("Value: " + is + " [old:" + oldIntInputs[0] + "])");
					}
				}
				if (v == null) {
					v = Constants.INT_POS_INFINITE;
				} else {
					if (e1.getValue() != v) {
						modified = true;
						e1.setValue(v.intValue());
					}
				}
				modified = true;
			}

			if (e.isSTNUEdge()) {
				STNUEdge e1 = (STNUEdge) e;
				String is = newIntInputs[0].getText();
				v = (is.length() > 0) ? Integer.valueOf(is) : null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LabelEditingGraphMousePlugin.LOG.finest("Value: " + is + " [old:" + oldIntInputs[0] + "])");
					}
				}
				if (v != null) {
					if (e1.getValue() != v) {
						modified = true;
						e1.setValue(v.intValue());
						e1.setLabeledValue(null, -1, false);
					}
				}
				modified = true;
			}

			if (e.isCSTNEdge()) {
				CSTNEdge e1 = (CSTNEdge) e;

				LabeledIntMapSupplier<LabeledIntMap> mapFactory = new LabeledIntMapSupplier<>((Class<LabeledIntMap>) e1.getLabeledValueMap().getClass());
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
					modified = true;
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
					modified = true;
				}
				if (!e1.getLabeledValueMap().equals(comp)) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LabelEditingGraphMousePlugin.LOG.finer("Original label set of the component: " + e1.getLabeledValueMap());
							LabelEditingGraphMousePlugin.LOG.finer("New label set for the component: " + comp);
						}
					}
					e1.clear();
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LabelEditingGraphMousePlugin.LOG.finer("Label set of the component after the clear: " + e1.getLabeledValueMap());
						}
					}
					e1.mergeLabeledValue(comp);
					modified = true;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LabelEditingGraphMousePlugin.LOG.finer("New label set assigned to the component: " + e1.getLabeledValueMap());
						}
					}
				}
			}

			if ((e.isCSTNUEdge() || e.isCSTNPSUEdge()) && (newUpperValueInputs != null && newLowerValueInputs != null)) {

				// I manage possible specification of lower/upper case new values
				LOG.info("It is a modified CSTNU (" + e.isCSTNUEdge() + ") or CSTPSU (" + e.isCSTNPSUEdge() + ") edge. nUpperValues: " + nUpperLabels
						+ ", nLowerValues:" + nLowerLabels);
				String caseValue = null, nodeName = null;
				String[] splitted = null;
				if (BasicCSTNUEdge.class.isAssignableFrom(e.getClass())) {
					BasicCSTNUEdge e1 = (BasicCSTNUEdge) e;

					// UPPER CASE VALUE
					// we consider only the first row
					// We don't use label because it has to be only one s = (labelUpperInputs[0] != null) ? labelUpperInputs[0].getText() : "";
					JTextField s = newUpperValueInputs[0];
					caseValue = (s != null) ? s.getText() : "";
					if (caseValue.length() != 0) {
						// the value is in the form "<node name>: <int>"
						splitted = caseValue.split(":[ ]*");
						if (splitted.length < 2) {
							v = null;
						} else {
							// nodeName = splitted[0].toUpperCase();
							nodeName = splitted[0];
							v = Integer.valueOf(splitted[1]);
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

							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LabelEditingGraphMousePlugin.LOG.finest("New Upper value input: " + nodeName + ": " + v + ".");
								}
							}
						}
					} else {
						nodeName = null;
						v = null;
					}
					if ((v == null || nodeName == null) && nUpperLabels > 0) {
						// The upper case label was removed.
						e1.clearUpperCaseValues();
						modified = true;
					}
					if (nodeName != null && v != null) {
						e1.clearUpperCaseValues();
						final LabeledNode source = g.getSource(e);
						final LabeledNode dest = g.getDest(e);
						final Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());
						ALabel alabel = new ALabel(new ALetter(source.getName()), g.getALabelAlphabet());
						if (alabel.toString().equals(nodeName)) {
							e1.clearUpperCaseValues();
							e1.putUpperCaseValue(endpointsLabel, alabel, v);// Temporally I ignore the label specified by user because an upper/lower case
							// value of a contingent must have the label of its endpoints.
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINEST)) {
									LabelEditingGraphMousePlugin.LOG.finest("Merged Upper value input: " + endpointsLabel + ", " + alabel + ": " + v + ".");
								}
							}
							modified = true;
						}
					}
					// LOWER CASE
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
								v = Integer.valueOf(splitted[1]);
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
							}
						} else {
							nodeName = null;
							v = null;
						}
					}
					if ((v == null || nodeName == null) && nLowerLabels > 0) {
						// The upper case label was removed.
						e1.clearLowerCaseValues();
						modified = true;
					}
					if ((nodeName != null) && (v != null)) {
						e1.clearLowerCaseValues();
						final LabeledNode source = g.getSource(e);
						final LabeledNode dest = g.getDest(e);
						final Label endpointsLabel = dest.getLabel().conjunction(source.getLabel());

						if (dest.getName().equals(nodeName)) {
							ALabel destALabel = (dest.getALabel() != null) ? ALabel.clone(dest.getALabel())
									: new ALabel(dest.getName(), g.getALabelAlphabet());
							dest.setALabel(destALabel);
							e1.putLowerCaseValue(endpointsLabel, destALabel, v);// Temporally I ignore the label specified by user because an upper/lower
																				// case value of a contingent must have the label of its endpoints.
							modified = true;
						}
					}
				}
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
		jc.setEnabled(viewerName.equals(CSTNEditor.EDITOR_NAME));
	}

	/**
	 * The editor in which this plugin works.
	 */
	CSTNEditor cstnEditor;

	/**
	 * Create an instance with default settings
	 *
	 * @param cstnEditor1 reference to the editor object (useful for finding some part of its panels).
	 */
	public LabelEditingGraphMousePlugin(CSTNEditor cstnEditor1) {
		super();
		this.cstnEditor = cstnEditor1;
	}

	/**
	 * {@inheritDoc}
	 * For primary modifiers (default, MouseButton1):
	 * <ol>
	 * <li>Pick a single Vertex or Edge that is under the mouse pointer.<br>
	 * <li>If no Vertex or Edge is under the pointer, unselect all picked Vertices and Edges, and set up to draw a rectangle for multiple selection of
	 * contained Vertices.
	 * </ol>
	 * For additional selection (default Shift+MouseButton1):
	 * <ol>
	 * <li>Add to the selection, a single Vertex or Edge that is under the mouse pointer.
	 * <li>If a previously picked Vertex or Edge is under the pointer, it is un-picked.
	 * <li>If no vertex or Edge is under the pointer, set up to draw a multiple selection rectangle (as above) but do not unpick previously picked
	 * elements.
	 * </ol>
	 */
	@Override
	@SuppressWarnings({ "unqualified-field-access", "unchecked" })
	public void mouseClicked(final MouseEvent e) {
		// DON'T USE e.getModifiersex()
		// LOG.severe("e.getModifiers():" + e.getModifiers() + "\tthis.modifiers: " + this.modifiers + "\te.getClickCount(): " + e.getClickCount());
		if ((e.getModifiers() == modifiers) && (e.getClickCount() == 2)) {
			// LOG.severe("LabelEditingGraphMousePlugin.mouseClicked.cstnEditor " + this.cstnEditor);
			final VisualizationViewer<V, E> vv = (VisualizationViewer<V, E>) e.getSource();
			final GraphElementAccessor<V, E> pickSupport = vv.getPickSupport();
			final String viewerName = vv.getName();
			if (pickSupport != null) {
				final Layout<V, E> layout = vv.getGraphLayout();
				final TNGraph<E> g = (TNGraph<E>) layout.getGraph();
				final Point2D p = e.getPoint(); // p is the screen point for the mouse event

				this.vertex = pickSupport.getVertex(layout, p.getX(), p.getY());
				if (this.vertex != null) {
					if (nodeAttributesEditor(this.vertex, viewerName, g)) {
						this.cstnEditor.resetDerivedGraphStatus();
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LabelEditingGraphMousePlugin.LOG.finer("The graph has been modified. Disable the distance viewer.");
							}
						}
						g.clearCache();
					}
					e.consume();
					vv.validate();
					vv.repaint();
					return;
				}

				// p is the screen point for the mouse event take away the view transform
				// Point2D ip = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.VIEW, p);
				this.edge = pickSupport.getEdge(layout, p.getX(), p.getY());
				if (this.edge != null) {
					if (edgeAttributesEditor(this.edge, viewerName, g)) {
						this.cstnEditor.resetDerivedGraphStatus();
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LabelEditingGraphMousePlugin.LOG.finer("The graph has been modified. Disable the distance viewer.");
							}
						}
						g.clearCache();
					}
					e.consume();
					vv.validate();
					vv.repaint();
					return;
				}
			}
			e.consume();
		}
	}

	/**
	 * Create an instance with overrides.
	 *
	 * @param selectionModifiers for primary selection
	 * @param cstnEditor1 reference to the editor object (useful for finding some part of its panels).
	 */
	public LabelEditingGraphMousePlugin(final int selectionModifiers, CSTNEditor cstnEditor1) {
		super(selectionModifiers);
		this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		this.cstnEditor = cstnEditor1;
	}

	/**
	 * General method to setup a dialog to edit the attributes of a node.
	 *
	 * @param node
	 * @param viewerName
	 * @param g graph
	 * @return true if one attribute at least has been modified
	 */
	@SuppressWarnings({  "static-method", "null" })
	private boolean nodeAttributesEditor(final LabeledNode node, final String viewerName, final TNGraph<? extends Edge> g) {

		// Planning a possible extension, a node could contains more labels with associated integers.
		// For now, we use only one entry and only the label part.

		final boolean editorPanel = viewerName.equals(CSTNEditor.EDITOR_NAME);
		// Create a ValidationPanel - this is a panel that will show
		// any problem with the input at the bottom with an icon
		final ValidationPanel panel = new ValidationPanel();

		// LabeledNode contains only the possible observed proposition and the possible associated label.
		/*
		 * The layout is a grid of 3 columns.
		 */
		final JPanel jp = new JPanel(new GridLayout(0, (editorPanel) ? 3 : 2));
		panel.setInnerComponent(jp);
		final ValidationGroup group = panel.getValidationGroup();

		// Name
		final JTextField name = new JTextField(node.getName());
		JLabel jl = new JLabel("Name:");
		jl.setLabelFor(name);
		jp.add(jl);
		jp.add(name);
		setConditionToEnable(name, viewerName, false);
		if (editorPanel) {
			jp.add(new JLabel("Syntax: [" + ALabelAlphabet.ALETTER + "?]+"));
			group.add(name, StringValidators.regexp("[" + ALabelAlphabet.ALETTER + "?]+", "Must be a well format name", false),
					new ObservableValidator(g, node));
		}

		// Potential
		int potential = node.getPotential();
		if (potential != Constants.INT_NULL) {
			JTextField potentialValue = new JTextField(Constants.formatInt(potential));
			jl = new JLabel("Potential: ");
			jl.setLabelFor(potentialValue);
			jp.add(jl);
			jp.add(potentialValue);
			setConditionToEnable(potentialValue, viewerName, true);
		}

		JTextField observedProposition = null;
		char p;
		Label l = null;
		JTextField label = null;
		if (BasicCSTNUEdge.class.isAssignableFrom(g.getEdgeImplClass())) {
			// Observed proposition
			p = node.getPropositionObserved();
			observedProposition = new JTextField((p == Constants.UNKNOWN) ? "" : "" + p);
			jl = new JLabel("Observed proposition:");
			jl.setLabelFor(observedProposition);
			jp.add(jl);
			jp.add(observedProposition);
			setConditionToEnable(observedProposition, viewerName, false);
			if (editorPanel) {
				jp.add(new JLabel("Syntax: " + Literal.PROPOSITION_RANGE + "| "));
				group.add(observedProposition, StringValidators.regexp(Literal.PROPOSITION_RANGE + "|", "Must be a single char in the range!", false),
						new ObservableValidator(g, node));
			}

			// Label
			l = node.getLabel();
			label = new JTextField(l.toString());
			jl = new JLabel("Label:");
			jl.setLabelFor(label);
			jp.add(jl);
			jp.add(label);
			setConditionToEnable(label, viewerName, false);
			if (editorPanel) {
				final JTextField jtf = new JTextField("Syntax: " + Label.LABEL_RE);
				jp.add(jtf);
				group.add(label, StringValidators.regexp(Label.LABEL_RE, "Check the syntax!", false), Label.labelValidator);
			}
			// Labeled Potential
			LabeledIntMap potentialMap = node.getLabeledPotential();
			if (!potentialMap.isEmpty()) {
				jl = new JLabel("Labeled Potential: ");
				jp.add(jl);
				JLabel potentialValues = new JLabel(
						"<html>" + potentialMap.toString().replace("{", "").replace("}", "").replaceAll("\\) \\(", ")<br />(") + "</html>",
						SwingConstants.LEFT);
				potentialValues.setBackground(Color.white);
				potentialValues.setOpaque(true);
				jp.add(potentialValues);
			}
		}

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

			if (BasicCSTNUEdge.class.isAssignableFrom(g.getEdgeImplClass())) {
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
		}
		return modified;
	}

	/**
	 * <p>
	 * Getter for the field <code>cstnEditor</code>.
	 * </p>
	 *
	 * @return the cstnEditor
	 */
	public CSTNEditor getCstnEditor() {
		return this.cstnEditor;
	}

	/**
	 * <p>
	 * Setter for the field <code>cstnEditor</code>.
	 * </p>
	 *
	 * @param cstnEditor1 the cstnEditor to set
	 */
	public void setCstnEditor(CSTNEditor cstnEditor1) {
		this.cstnEditor = cstnEditor1;
	}
}
