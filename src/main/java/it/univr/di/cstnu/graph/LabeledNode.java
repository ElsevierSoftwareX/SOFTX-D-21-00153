package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Function;

import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
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
	static final Logger LOG = Logger.getLogger("LabeledNode");

	/**
	 * Labeled value class used in the class.
	 */
	public static final Class<? extends LabeledIntMap> labeledValueMapImpl = LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS;

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * First counter of labeled value updating.
	 * 20191031 I haven't found a case in which it can help.
	 * Object2IntMap<Label> labeledPotentialCount;
	 */

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
	 * Labeled potential values.
	 */
	private LabeledIntMap labeledPotential;

	/**
	 * Potential value.
	 */
	private int potential;

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
	 */
	LabeledNode(final LabeledNode n) {// , Class<? extends LabeledIntMap> labeledIntMapImplementation
		super(n);
		this.label = n.label;
		this.propositionObserved = n.getPropositionObserved();
		this.x = n.x;
		this.y = n.y;
		this.alabel = n.alabel;
		this.potential = n.potential;

		this.labeledPotential = (new LabeledIntMapSupplier<>(labeledValueMapImpl)).get();
		// this.labeledPotentialCount = new Object2IntLinkedOpenHashMap<>(n.labeledPotentialCount);
		// this.labeledPotentialCount.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Constructor for LabeledNode.
	 *
	 * @param string a {@link java.lang.String} object.
	 */
	LabeledNode(final String string) {// , Class<? extends LabeledIntMap> labeledIntMapImplementation
		super(string);
		this.label = Label.emptyLabel;
		this.x = this.y = 0;
		this.propositionObserved = Constants.UNKNOWN;
		this.potential = Constants.INT_NULL;
		this.alabel = null;
		this.labeledPotential = (new LabeledIntMapSupplier<>(labeledValueMapImpl)).get();
		// this.labeledPotentialCount = new Object2IntLinkedOpenHashMap<>();
		// this.labeledPotentialCount.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Standard constructor for an observation node
	 *
	 * @param n name of the node.
	 * @param proposition proposition observed by this node.
	 */
	<C extends LabeledIntMap> LabeledNode(final String n, final char proposition) {// , Class<C> labeledIntMapImplementation
		this(n);
		this.propositionObserved = (Literal.check(proposition)) ? proposition : Constants.UNKNOWN;
		this.potential = Constants.INT_NULL;
	}

	/**
	 * Clears all fields but name of <code>this</code>.
	 */
	@Override
	public void clear() {
		super.clear();
		this.label = Label.emptyLabel;
		this.propositionObserved = Constants.UNKNOWN;
		this.x = this.y = 0;
		this.alabel = null;
		this.potential = Constants.INT_NULL;
		this.labeledPotential.clear();
		// this.labeledPotentialCount.clear();
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
	 *         public int getPotentialCount(Label l) {
	 *         int i = this.labeledPotentialCount.getInt(l);
	 *         return (i == Constants.INT_NULL) ? 0 : i;
	 *         }
	 *         //
	 */

	/**
	 * @return an unmodifiable view of the labeled potential values
	 */
	public LabeledIntMap getLabeledPotential() {
		return this.labeledPotential.unmodifiable();
	}

	/**
	 * @param l the label to remove
	 * @return the old value
	 */
	public int removeLabeledPotential(Label l) {
		return this.labeledPotential.remove(l);
	}

	/**
	 * @param l
	 * @return the labeled value getPotential(ALabel.emptyLabel, Label).
	 */
	public int getLabeledPotential(Label l) {
		return this.labeledPotential.get(l);
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
		return this.labeledPotential.equals(inputPotential);
	}

	/**
	 * Puts the labeled value (value, l) into the potential map.
	 * 
	 * @param l
	 * @param value
	 * @return true if the pair has been merged.
	 */
	final public boolean putLabeledPotential(Label l, int value) {
		return this.labeledPotential.put(l, value);
	}

	/**
	 * @param l a not null label
	 * @param reset true if the count has to be reset and, therefore, this becomes the first update.
	 * @return the old value associate to to label l. If the label does not exists, returns {@link Constants#INT_NULL}. In case of reset, returns 0.
	 *         public int updatePotentialCount(Label l, boolean reset) {
	 *         if (l == null)
	 *         return Constants.INT_NULL;
	 *         int i = this.labeledPotentialCount.getInt(l);
	 *         i = (i == Constants.INT_NULL || reset) ? 1 : i + 1;
	 *         this.labeledPotentialCount.put(l, i);
	 *         return i - 1;
	 *         }
	 */

	/**
	 * It is responsibility of programmer to maintain the correspondence between name and alabel.
	 * 
	 * @param inputAlabel the alabel to set
	 */
	public void setAlabel(ALabel inputAlabel) {
		this.alabel = inputAlabel;
	}

	/**
	 * Setter for the field <code>label</code>.
	 *
	 * @param inputLabel the label to set. If it is null, this.label is set to {@value Label#emptyLabel}.
	 */
	public void setLabel(@Nullable final Label inputLabel) {
		String old = this.label.toString();
		this.label = (inputLabel == null || inputLabel.isEmpty()) ? Label.emptyLabel : inputLabel;
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
	 * @param nodeName the not-null not-empty new name
	 * @return the old name
	 */
	@Override
	public String setName(final String nodeName) {
		final String old = this.name;
		if ((nodeName != null) && (nodeName.length() > 0)) {
			this.name = nodeName;
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
	 * @param potentialMap
	 */
	public void setLabeledPotential(LabeledIntMap potentialMap) {
		if (potentialMap == null)
			return;
		this.labeledPotential = potentialMap;
	}

	/**
	 * Setter for the field <code>x</code>.
	 *
	 * @param x1 the x to set
	 */
	public void setX(final double x1) {
		this.x = x1;
	}

	/**
	 * Setter for the field <code>y</code>.
	 *
	 * @param y1 the y to set
	 */
	public void setY(final double y1) {
		this.y = y1;
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
		if (!this.labeledPotential.isEmpty()) {
			sb.append("; Labeled Potential: ");
			sb.append(this.labeledPotential.toString());
		}
		if (this.potential != Constants.INT_NULL) {
			sb.append("; Potential: ");
			sb.append(this.potential);
		}

		sb.append(Constants.CLOSE_TUPLE);
		return sb.toString();
	}

	/**
	 * @return the potential. If {@link Constants#INT_NULL}, it means that it was not determined.
	 */
	public int getPotential() {
		return this.potential;
	}

	/**
	 * @param potential1 the potential to set
	 */
	public void setPotential(int potential1) {
		this.potential = potential1;
	}
}
