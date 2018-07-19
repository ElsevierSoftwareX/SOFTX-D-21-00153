/**
 *
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.Literal;

/**
 * LabeledNode class.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class LabeledNode extends AbstractComponent {

	/**
	 * Factory as Supplier type.
	 * 
	 * @return a supplier to get empty nodes.
	 */
	public static Supplier<LabeledNode> getFactory() {
		return new Supplier<LabeledNode>() {
			@Override
			public LabeledNode get() {
				return new LabeledNode("n" + idSeq++, Label.emptyLabel);// if you change this default, change also in CSTNUGraphMLReader
			}
		};
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	static final Logger LOG = Logger.getLogger(LabeledNode.class.getName());

	/**
	 * Used to show the node name.
	 */
	public final static Function<LabeledNode, String> vertexLabelTransformer = new Function<LabeledNode, String>() {
		/**
		 * Returns a label for the node
		 */
		@Override
		public String apply(final LabeledNode v) {
			return v.getName() + (v.getLabel().isEmpty() ? "" : "_[" + v.getLabel() + "]");
		}
	};

	/**
	 * Transformer object to show the tooltip of node: the label is print.
	 */
	public static Function<LabeledNode, String> vertexToolTipTransformer = new Function<LabeledNode, String>() {
		@Override
		public String apply(final LabeledNode v) {
			return "Label: " + v.getLabel().toString();
		}
	};

	/**
	 * Label associated to this node.
	 */
	private Label label;

	/**
	 * ALabel associated to this node.
	 * This field has the scope to speed up the DC checking.
	 * It is used to represent the name of a contingent time point as ALabel, instead of to calculate it every time.
	 */
	private ALabel alabel;

	/**
	 * Potential labeled values.
	 */
	private LabeledALabelIntTreeMap potential;

	/**
	 * Position Coordinates. It must be double even if it is not necessary for Jung library compatibility.
	 */
	private double x;

	/**
	 * Position Coordinates. It must be double even if it is not necessary for Jung library compatibility.
	 */
	private double y;

	/**
	 * Possible proposition observed.
	 */
	char propositionObserved;

	/**
	 * Constructor for LabeledNode.
	 *
	 * @param string a {@link java.lang.String} object.
	 */
	public LabeledNode(final String string) {
		super(string);
		this.label = Label.emptyLabel;
		this.x = this.y = 0;
		this.propositionObserved = Constants.UNKNOWN;
		this.alabel = null;
		this.potential = new LabeledALabelIntTreeMap();
		this.potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
	}

	/**
	 * Standard constructor for an observation node
	 *
	 * @param n name of the node.
	 * @param proposition proposition observed by this node.
	 */
	public LabeledNode(final String n, final char proposition) {
		super(n);
		this.label = Label.emptyLabel;
		this.propositionObserved = (Literal.check(proposition)) ? proposition : Constants.UNKNOWN;
		this.x = this.y = 0;
		this.alabel = null;
		this.potential = new LabeledALabelIntTreeMap();
		this.potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
	}

	/**
	 * Constructor for cloning.
	 *
	 * @param n the node to copy.
	 */
	public LabeledNode(final LabeledNode n) {
		super(n);
		this.label = n.label;
		this.propositionObserved = n.getPropositionObserved();
		this.x = n.x;
		this.y = n.y;
		this.alabel = n.alabel;
		this.potential = new LabeledALabelIntTreeMap(n.potential);
		this.potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
	}

	/**
	 * Standard constructor
	 *
	 * @param n
	 * @param l
	 */
	LabeledNode(final String n, final Label l) {
		super(n);
		this.label = l;
		this.propositionObserved = Constants.UNKNOWN;
		this.x = this.y = 0;
		this.alabel = null;
		this.potential = new LabeledALabelIntTreeMap();
		this.potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
	}

	/**
	 * Getter for the field <code>label</code>.
	 *
	 * @return the label
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * @return the set of alabels in the potential.
	 */
	public ObjectSet<ALabel> getALabelsOfPotential() {
		return this.potential.keySet();
	}

	/**
	 * @param aLabel
	 * @return the entry set of labeled values associated to alabel in the potential of the node.
	 */
	public ObjectSet<Entry<Label>> getPotentialEntrySet(ALabel aLabel) {
		return this.potential.get(aLabel).entrySet();
	}

	/**
	 * @return the potential map
	 */
	public LabeledALabelIntTreeMap getPotential() {
		return this.potential;
	}

	/**
	 * @param aLabel
	 * @return the labeled values associated to alabel in the potential of the node.
	 */
	public LabeledIntTreeMap getPotential(ALabel aLabel) {
		return this.potential.get(aLabel);
	}

	/**
	 * @param aLabel
	 * @param l
	 * @return the value associated to aLabel and l in the potential
	 */
	public int getPotential(ALabel aLabel, Label l) {
		return this.potential.getValue(l, aLabel);
	}

	/**
	 * @return a copy of the potential of this node.
	 */
	public LabeledALabelIntTreeMap clonePotential() {
		return new LabeledALabelIntTreeMap(this.potential);
	}

	/**
	 * @param inputPotential
	 * @return true if node potential is equal to inputPotential
	 */
	public boolean isPotentialEqual(LabeledALabelIntTreeMap inputPotential) {
		return this.potential.equals(inputPotential);
	}
	/**
	 * @param aLabel
	 * @param l
	 * @param value
	 * @return true if the triple has been merged.
	 */
	public boolean potentialPut(ALabel aLabel, Label l, int value) {
		if ((l == null) || (aLabel == null) || (value == Constants.INT_NULL))
			throw new IllegalArgumentException(
					"The label or the value has a not admitted value: (" + l + ", " + aLabel + ", " + Constants.formatInt(value) + ").");
		if (value < 0 && this.getPropositionObserved() != Constants.UNKNOWN) {// Rule qR0
			l = l.remove(getPropositionObserved());
		}
		// Check if a standard labeled value is more restrictive of the one to put.
		// FIXME
		// final int minNormalValueSubSumedByL = this.potential.get(ALabel.emptyLabel).getMinValueSubsumedBy(l);
		// if ((minNormalValueSubSumedByL != Constants.INT_NULL) && (minNormalValueSubSumedByL <= value)) {
		// if (Debug.ON) {
		// if (LOG.isLoggable(Level.FINEST)) {
		// LOG.finest("The labeled value (" + l + ", " + aLabel + ", " + value
		// + ") has not been stored because the value is greater than the labeled minimal value subsumed by " + l + ".");
		// }
		// }
		// return false;
		// }
		return this.potential.mergeTriple(l, aLabel, value, false);
	}

	/**
	 * @param aLabel
	 * @param map
	 * @return true if the triple has been merged.
	 */
	public boolean potentialPut(ALabel aLabel, LabeledIntMap map) {
		if (map == null)
			return false;
		boolean added = false;
		for (Entry<Label> entry : map.entrySet()) {
			added |= this.potentialPut(aLabel, entry.getKey(), entry.getIntValue());
		}
		return added;
	}

	/**
	 * @param l
	 * @param value
	 * @return true if the triple has been merged.
	 */
	public boolean potentialPut(Label l, int value) {
		return this.potentialPut(ALabel.emptyLabel, l, value);
	}

	/**
	 * @return the proposition under the control of this node. {@link Constants#UNKNOWN}, if no observation is made.
	 */
	public char getPropositionObserved() {
		return this.propositionObserved;
	}

	/**
	 * @return true if this node is an observator one (it is associated to a proposition letter), false otherwise;
	 */
	public boolean isObserver() {
		return this.propositionObserved != Constants.UNKNOWN;
	}

	/**
	 * Getter for the field <code>x</code>.
	 *
	 * @return the x
	 */
	public double getX() {
		return this.x;
	}

	/**
	 * Getter for the field <code>y</code>.
	 *
	 * @return the y
	 */
	public double getY() {
		return this.y;
	}

	/**
	 * Setter for the field <code>label</code>.
	 *
	 * @param label the label to set
	 */
	public void setLabel(final Label label) {
		String old = this.label.toString();
		this.label = (label == null || label.isEmpty()) ? Label.emptyLabel : label;
		this.setChanged();
		notifyObservers("Label:" + old);
	}

	/**
	 * Setter for the field <code>label</code>.
	 *
	 * @param s the label to set
	 */
	public void setLabel(final String s) {
		this.setLabel(Label.parse(s));
	}

	/**
	 * Set the proposition to be observed.
	 *
	 * @param c the proposition to observe. If {@link Constants#UNKNOWN}, the node became not observable node.
	 */
	public void setObservable(final char c) {
		char old = this.propositionObserved;
		this.propositionObserved = (Literal.check(c)) ? c : Constants.UNKNOWN;
		notifyObservers("Proposition:" + old);
		this.setChanged();
	}

	/**
	 * Setter for the field <code>x</code>.
	 *
	 * @param x the x to set
	 */
	public void setX(final double x) {
		this.x = x;
	}

	/**
	 * Setter for the field <code>y</code>.
	 *
	 * @param y the y to set
	 */
	public void setY(final double y) {
		this.y = y;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("〈");
		sb.append(this.getName());
		sb.append("; ");
		sb.append(this.getLabel());
		if (this.propositionObserved != Constants.UNKNOWN) {
			sb.append("; Obs: ");
			sb.append(this.propositionObserved);
		}
		if (!this.potential.isEmpty()) {
			sb.append("; Potential: ");
			sb.append(this.potential.toString());
		}
		sb.append("〉");
		return sb.toString();
	}

	/**
	 * Set the name of the node. Cannot be null or empty.
	 *
	 * @param name the not-null not-empty new name
	 * @return the old name
	 */
	@Override
	public String setName(final String name) {
		final String old = this.name;
		if ((name != null) && (name.length() > 0)) {
			this.name = name;
			this.setChanged();
			notifyObservers("Name:" + old);
		}
		return old;
	}

	/**
	 * @return the alabel
	 */
	public ALabel getAlabel() {
		return this.alabel;
	}

	/**
	 * It is responsibility of programmer to maintain the correspondence between name and alabel.
	 * 
	 * @param alabel the alabel to set
	 */
	public void setAlabel(ALabel alabel) {
		this.alabel = alabel;
	}

	/**
	 * @return true if this point represents a contingent time point.
	 *         It is assumed that a node representing a contingent time point has its field 'alabel' not null.
	 */
	public boolean isContingent() {
		return this.alabel != null;
	}
}
