package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import com.google.common.base.Function;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapFactory;
import it.univr.di.labeledvalue.Literal;

/**
 * LabeledNode class.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class LabeledNode extends AbstractComponent {

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
	 * 
	 */
	static final Logger LOG = Logger.getLogger(LabeledNode.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * First counter of labeled value updating
	 */
	Object2IntMap<Label> potentialCount;

	/**
	 * Possible proposition observed.
	 */
	char propositionObserved;

	/**
	 * ALabel associated to this node.
	 * This field has the scope to speed up the DC checking.
	 * It is used to represent the name of a contingent time point as ALabel, instead of to calculate it every time.
	 */
	private ALabel alabel;

	/**
	 * Label associated to this node.
	 */
	private Label label;

	/**
	 * Potential labeled values.
	 */
	private LabeledIntMap potential;

	/**
	 * Position Coordinates. It must be double even if it is not necessary for Jung library compatibility.
	 */
	private double x;

	/**
	 * Position Coordinates. It must be double even if it is not necessary for Jung library compatibility.
	 */
	private double y;


	/**
	 * Constructor for cloning.
	 *
	 * @param n the node to copy.
	 * @param labeledIntMapImplementation
	 */
	<C extends LabeledIntMap> LabeledNode(final LabeledNode n, Class<C> labeledIntMapImplementation) {
		super(n);
		this.label = n.label;
		this.propositionObserved = n.getPropositionObserved();
		this.x = n.x;
		this.y = n.y;
		this.alabel = n.alabel;
		this.potential = (new LabeledIntMapFactory<>(labeledIntMapImplementation)).get();
		this.potentialCount = new Object2IntLinkedOpenHashMap<>(n.potentialCount);
		this.potentialCount.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Constructor for LabeledNode.
	 *
	 * @param string a {@link java.lang.String} object.
	 * @param labeledIntMapImplementation
	 */
	<C extends LabeledIntMap> LabeledNode(final String string, Class<C> labeledIntMapImplementation) {
		super(string);
		this.label = Label.emptyLabel;
		this.x = this.y = 0;
		this.propositionObserved = Constants.UNKNOWN;
		this.alabel = null;
		this.potential = (new LabeledIntMapFactory<>(labeledIntMapImplementation)).get();
		this.potentialCount = new Object2IntLinkedOpenHashMap<>();
		this.potentialCount.defaultReturnValue(Constants.INT_NULL);

	}

	/**
	 * Standard constructor for an observation node
	 *
	 * @param n name of the node.
	 * @param proposition proposition observed by this node.
	 * @param labeledIntMapImplementation
	 */
	<C extends LabeledIntMap> LabeledNode(final String n, final char proposition, Class<C> labeledIntMapImplementation) {
		this(n, labeledIntMapImplementation);
		this.propositionObserved = (Literal.check(proposition)) ? proposition : Constants.UNKNOWN;
	}

	/**
	 * Clears all fields but name of <code>this</code>.
	 */
	public void clear() {
		this.label = Label.emptyLabel;
		this.propositionObserved = Constants.UNKNOWN;
		this.x = this.y = 0;
		this.alabel = null;
		this.potential.clear();
		this.potentialCount.clear();
	}

	/**
	 * @return the alabel
	 */
	public ALabel getAlabel() {
		return this.alabel;
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
	 * @param l a not null label
	 * @return the counter value associate to label l. If the value does not exists, returns 0;
	 */
	public int getPotentialCount(Label l) {
		int i = this.potentialCount.getInt(l);
		return (i == Constants.INT_NULL) ? 0 : i;
	}

	/**
	 * @return an unmodifiable view of the labeled potential values
	 */
	public LabeledIntMap getPotential() {
		return this.potential.unmodifiable();
	}

	/**
	 * @param l the label to remove
	 * @return the old value
	 */
	public int removePotential(Label l) {
		return this.potential.remove(l);
	}

	/**
	 * @param l
	 * @return the labeled value getPotential(ALabel.emptyLabel, Label).
	 */
	public int getPotential(Label l) {
		return this.potential.get(l);
	}

	/**
	 * @return the proposition under the control of this node. {@link Constants#UNKNOWN}, if no observation is made.
	 */
	public char getPropositionObserved() {
		return this.propositionObserved;
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
	 * @return true if this point represents a contingent time point.
	 *         It is assumed that a node representing a contingent time point has its field 'alabel' not null.
	 */
	public boolean isContingent() {
		return this.alabel != null;
	}

	/**
	 * @return true if this node is an observator one (it is associated to a proposition letter), false otherwise;
	 */
	public boolean isObserver() {
		return this.propositionObserved != Constants.UNKNOWN;
	}

	/**
	 * @param inputPotential
	 * @return true if node potential is equal to inputPotential
	 */
	public boolean isPotentialEqual(LabeledIntMap inputPotential) {
		return this.potential.equals(inputPotential);
	}

	/**
	 * Puts the labeled value (value, l) into the potential map.
	 * 
	 * @param l
	 * @param value
	 * @return true if the pair has been merged.
	 */
	final public boolean putPotential(Label l, int value) {
		return this.potential.put(l, value);
	}

	/**
	 * @param l a not null label
	 * @param reset true if the count has to be reset
	 * @return the old value associate to to label l. If the old value does not exists, returns 0.
	 */
	public int updatePotentialCount(Label l, boolean reset) {
		if (l == null)
			return Constants.INT_NULL;
		int i = this.potentialCount.getInt(l);
		i = (i == Constants.INT_NULL || reset) ? 1 : i + 1;
		this.potentialCount.put(l, i);
		return i - 1;
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
	 * Setter for the potential.
	 * If potential is not null, it is used (not copied) as new potential of the node.
	 * If potential is null, it does nothing.
	 * 
	 * @param potential
	 */
	public void setPotential(LabeledIntMap potential) {
		if (potential == null)
			return;
		this.potential = potential;
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
		final StringBuilder sb = new StringBuilder(Constants.OPEN_TUPLE);
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
		sb.append(Constants.CLOSE_TUPLE);
		return sb.toString();
	}
}
