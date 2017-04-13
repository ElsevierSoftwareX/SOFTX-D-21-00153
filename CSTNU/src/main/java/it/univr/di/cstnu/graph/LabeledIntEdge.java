/**
 *
 */
package it.univr.di.cstnu.graph;

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Map.Entry;

import edu.uci.ics.jung.visualization.RenderContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;

/**
 * It contains all information of a CSTPU edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface LabeledIntEdge extends Component {

	/**
	 * Possible types
	 *
	 * @author posenato
	 */
	public static enum ConstraintType {
		/**
		 * The edge represents a generic constraint. In the CNSTU is equivalent to normal edge, but it is graphically represented in a different way.
		 */
		constraint,

		/** The edge represents a contingent (not shrinkable) constraint. */
		contingent,

		/**
		 * The edge represents a constraint derived by the controllability check algorithm.
		 */
		derived,

		/**
		 * The edge represents an internal one used to represent high level construct as a WORKFLOW OR JOIN.
		 */
		internal,

		/**
		 * The edge represents an execution precedence between two nodes one.
		 */
		normal
	}

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke constraintEdgeStroke = RenderContext.DASHED;

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke contingentEdgeStroke = new BasicStroke(1.2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	// GRAPHICS & VISUALIZATION STUFF
	// Set up a new stroke Transformer for the edges
	/**
	 * Simple stroke object to draw a 'derived' type edge.
	 */
	static final Stroke derivedEdgeStroke = RenderContext.DOTTED;
	// new BasicStroke(0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 * Simple stroke object to draw a 'standard' type edge.
	 */
	static final Stroke normalEdgeStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f);

	/**
	 * Public method to enable a Factory class.
	 * 
	 * @return an object of type LabeledIntEdge.
	 */
	public LabeledIntEdge createLabeledIntEdge();

	/**
	 * Public method to enable a Factory class.
	 * 
	 * @param e an object to clone.
	 * @return an object of type LabeledIntEdge.
	 */
	public LabeledIntEdge createLabeledIntEdge(LabeledIntEdge e);

	/**
	 * Clear (remove) all labeled values associated to this edge.
	 */
	public void clear();

	/**
	 * Clears all ordinary labeled values.
	 */
	public void clearLabels();

	/**
	 * Clears all lower case labeled values.
	 */
	public void clearLowerLabels();

	/**
	 * Clears all upper case labeled values.
	 */
	public void clearUpperLabels();

	/**
	 * A different kind of equals. It allows one to compare two edges with respect to ALL their labeled values (ordinary, upper- and lower-case ones).
	 *
	 * @param e a not null edge
	 * @return true if edge e contains labeled values equal to the current edge ones.
	 */
	public boolean equalsLabeledValues(final LabeledIntEdge e);

	/**
	 * The ordinary labeled values have Constants.EMPTY_UPPER_CASE_LABELstring as Upper-case letter.
	 * @return the set of all ordinary labeled values and upper case ones.
	 */
	public ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> getAllUpperCaseAndOrdinaryLabeledValuesSet();

	/**
	 * @return the labeledValueMap. If there is no labeled values, return an empty map.
	 */
	public LabeledIntMap getLabeledValueMap();

	/**
	 * @return the lower-Case labeled Value map
	 */
	public LabeledContingentIntTreeMap getLowerLabelMap();

	/**
	 * @return the map of Lower Case Labels of the edge.
	 */
	public ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> getLowerLabelSet();

	/**
	 * Returns the value associated to the lower label of the occurrence of node n if it exists, null otherwise;
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @return the value associated to the labeled wait if it exists.
	 */
	public int getLowerLabelValue(final Label l, final LabeledNode n);

	/**
	 * @return the minimal value among all Lower Case Label if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinLowerLabeledValue();

	/**
	 * @return the minimal value among all Upper Case Label if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinUpperLabeledValue();

	/**
	 * @return the minimal value among all ordinary labeled values if there are some values, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinValue();

	/**
	 * @return the minimal value among all ordinary labeled values having label without unknown literals, if there are some;
	 *         {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getMinValueAmongLabelsWOUnknown();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the value of label l or the minimal value of labels consistent with l if it exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l);

	/**
	 * @param l the scenario label
	 * @param upperL the Upper Label
	 * @return the value associated to the upper label of the occurrence of node n if it exists or the minimal values
	 *         among the labels
	 *         subsumed by l if one exists, null otherwise.
	 */
	public int getMinValueConsistentWith(final Label l, final String upperL);

	/**
	 * @return the labeled values that cannot be further added to the labeled value set of this edge
	 */
	public Object2IntMap<Label> getRemovedLabeledValuesMap();

	/**
	 * @return the type
	 */
	public ConstraintType getConstraintType();

	/**
	 * @return the factory for building the internal labeled value map.
	 */
	public LabeledIntMapFactory<? extends LabeledIntMap> getLabeledIntValueMapFactory();

	/**
	 * @return the Upper-Case labeled Value
	 */
	public LabeledContingentIntTreeMap getUpperLabelMap();

	/**
	 * @return The map of Upper Case Labels of the edge.
	 */
	public ObjectSet<Object2IntMap.Entry<Entry<Label, String>>> getUpperLabelSet();

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @return the value associated to the upper label of the occurrence of node n if it exists, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getUpperLabelValue(final Label l, final LabeledNode n);

	/**
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param name a {@link java.lang.String} object.
	 * @return the value associated to the upper label of the occurrence of node n if it exists, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getUpperLabelValue(final Label l, final String name);

	/**
	 * @param label label
	 * @return the value associated to label it it exists, {@link it.univr.di.labeledvalue.Constants#INT_NULL} otherwise.
	 */
	public int getValue(final Label label);

	/**
	 * @return true if the edge represent a contingent edge.
	 */
	public boolean isContingentEdge();

	/**
	 * @return true if the edge represent a normal edge or a derived one or a constraint one.
	 */
	public boolean isRequirementEdge();

	/**
	 * @return true is it does not contain any values
	 */
	public boolean isEmpty();
	
	/**
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> labeledValueSet();

	/**
	 * @param setToReuse
	 * @return the labeled values as a set
	 */
	public ObjectSet<Object2IntMap.Entry<Label>> labeledValueSet(ObjectSet<Object2IntMap.Entry<Label>> setToReuse);

	/**
	 * @return the number of Lower Case Labels of the edge.
	 */
	public int lowerLabelSize();

	/**
	 * @return the representation of all Lower Case Labels as a string.
	 */
	public String lowerLabelsToString();

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i an integer.
	 * @param s the node set to add.
	 * @return true if the operation was successful, false otherwise.
	 */
	public boolean mergeLabeledValue(final Label l, final int i, ObjectSet<String> s);

	/**
	 * Merges the labeled value i to the set of labeled values of this edge.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a integer.
	 * @return true if the operation was successful, false otherwise.
	 */
	public boolean mergeLabeledValue(final Label l, final int i);

	/**
	 * <p>
	 * mergeLabeledValue.
	 * </p>
	 *
	 * @param map a {@link it.univr.di.labeledvalue.LabeledIntMap} object.
	 */
	public void mergeLabeledValue(final LabeledIntMap map);

	/**
	 * Wrapper method for {@link #mergeLabeledValue(Label, int)}.
	 *
	 * @param i an integer.
	 * @return true if the operation was successful, false otherwise.
	 * @see #mergeLabeledValue(Label, int)
	 * @param ls a {@link java.lang.String} object.
	 */
	public boolean mergeLabeledValue(final String ls, final int i);

	/**
	 * Set a lower label constraint with delay i for the node n with label l.<br>
	 * If a lower label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param n the node. It cannot be null.
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeLowerLabelValue(final Label l, final LabeledNode n, final int i);

	/**
	 * Set a lower label constraint with delay i for the node n with label l.<br>
	 * If a lower label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node. It cannot be null. It must be the unmodified name of the node. 
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeLowerLabelValue(final Label l, String nodeName, final int i);

	/**
	 * Set a upper label constraint with delay i for the node n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param n the node. It cannot be null.
	 * @param i It cannot be null.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperLabelValue(final Label l, final LabeledNode n, final int i);

	/**
	 * Set a upper label constraint with delay i for the node name n with label l.<br>
	 * If a upper label with label l for node n is already present, it is overwritten.
	 *
	 * @param l It cannot be null or empty.
	 * @param nodeName the node name. It cannot be null. It must be the unmodified name of the node. 
	 * @param i It cannot be nullInt.
	 * @return true if the merge has been successful.
	 */
	public boolean mergeUpperLabelValue(final Label l, String nodeName, final int i);

	/**
	 * <p>
	 * putLabeledValue.
	 * </p>
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a int.
	 * @return true if the value has been inserted, false otherwise.
	 */
	public boolean putLabeledValue(final Label l, final int i);

	/**
	 * Put the labeled value (l, i) to the list of removed labeled values in order to avoid possible future put/merge of
	 * the same pair.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param i a int.
	 * @return the previous value if there exists one, null otherwise.
	 */
	public int putLabeledValueToRemovedList(final Label l, final int i);

	/**
	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
	 * of the same pair.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @param i an int.
	 * @return the old value
	 */
	public int putUpperLabeledValueToRemovedList(final Label l, final LabeledNode n, final int i);

	/**
	 * Put the labeled value (l, n, i) to the list of removed labeled values in order to avoid possible future put/merge
	 * of the same pair.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n node name
	 * @param i a int.
	 * @return the previous value.
	 */
	public int putUpperLabeledValueToRemovedList(final Label l, final String n, final int i);

	/**
	 * Remove the label l from the map. If the label is not present, it does nothing.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @return the old value if it exists, null otherwise.
	 */
	public int removeLabel(final Label l);

	/**
	 * Remove the lower label for node n with label l.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @return the value of the removed labeled value
	 */
	public int removeLowerLabel(final Label l, final LabeledNode n);

	/**
	 * Remove the upper label for node n with label l.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @return the value of the removed element.
	 */
	public int removeUpperLabel(final Label l, final LabeledNode n);

	/**
	 * Remove the upper label for node name n with label l.
	 *
	 * @param l a {@link it.univr.di.labeledvalue.Label} object.
	 * @param n a {@link java.lang.String} object.
	 * @return the old value
	 */
	public int removeUpperLabel(final Label l, final String n);

	/**
	 * <p>
	 * setLabeledLowerCaseValue.
	 * </p>
	 *
	 * @param labeledValue the lower case labeled value map to use for initializing the set
	 */
	public void setLabeledLowerCaseValue(final LabeledContingentIntTreeMap labeledValue);

	/**
	 * setLabeledUpperCaseValue.
	 *
	 * @param labeledValue the upper case labeled value map to use for initializing the set
	 */
	public void setLabeledUpperCaseValue(final LabeledContingentIntTreeMap labeledValue);

	/**
	 * Setter for the field <code>labeledValue</code>.
	 *
	 * @param labeledValue the labeledValue to set
	 */
	public void setLabeledValue(final LabeledIntMap labeledValue);

	/**
	 * Setter for the field <code>type</code>.
	 *
	 * @param type the type to set
	 */
	public void setConstraintType(final ConstraintType type);

	/**
	 * @return the number of labeled values associated to this edge.
	 */
	public int size();

	/**
	 * @return the number of Upper Case Labels of the edge.
	 */
	public int upperLabelSize();

	/**
	 * @return the representation of all Upper Case Labels of the edge.
	 */
	public String upperLabelsToString();

}
