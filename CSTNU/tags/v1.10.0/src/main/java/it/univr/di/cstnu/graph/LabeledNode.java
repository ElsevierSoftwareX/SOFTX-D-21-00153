/**
 *
 */
package it.univr.di.cstnu.graph;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

/**
 * LabeledNode class.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class LabeledNode extends AbstractComponent {

	/**
	 * <p>
	 * getFactory.
	 * </p>
	 *
	 * @return a factory to build empty nodes.
	 */
	public static Factory<LabeledNode> getFactory() {
		return new Factory<LabeledNode>() {
			@Override
			public LabeledNode create() {
				return new LabeledNode("n" + idSeq++, Label.emptyLabel);// if you change this default, change also in GraphMLReader
			}
		};
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Used to show the node name.
	 */
	public static Transformer<LabeledNode, String> vertexLabelTransformer = new ToStringLabeller<LabeledNode>() {
		@Override
		public String transform(final LabeledNode v) {
			return v.getName() + (v.getLabel().isEmpty() ? "" : "_[" + v.getLabel() + "]");
		}
	};

	/**
	 * Transformer object to show the tooltip of node: the label is print.
	 */
	public static Transformer<LabeledNode, String> vertexToolTipTransformer = new ToStringLabeller<LabeledNode>() {
		@Override
		public String transform(final LabeledNode v) {
			return "Label: " + v.getLabel().toString();
		}
	};

	/**
	 * Label associated to this node.
	 */
	private Label label = null;

	/**
	 * Position Coordinates
	 */
	private Double x;

	/**
	 * Position Coordinates
	 */
	private Double y;

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
		this.x = this.y = new Double(0);
		this.propositionObserved = Constants.UNKNOWN;
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
		this.x = this.y = new Double(0);
	}

	/**
	 * Constructor for cloning.
	 *
	 * @param n the node to copy.
	 */
	public LabeledNode(final LabeledNode n) {
		super(n);
		this.label = new Label(n.label);
		this.propositionObserved = n.getPropositionObserved();
		this.x = n.x;
		this.y = n.y;
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
		this.x = this.y = new Double(0);
	}

	/**
	 * Getter for the field <code>label</code>.
	 *
	 * @return the label
	 */
	public Label getLabel() {
		return new Label(this.label);
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
	public boolean isObservator() {
		return this.propositionObserved != Constants.UNKNOWN;
	}

	/**
	 * Getter for the field <code>x</code>.
	 *
	 * @return the x
	 */
	public Double getX() {
		return this.x;
	}

	/**
	 * Getter for the field <code>y</code>.
	 *
	 * @return the y
	 */
	public Double getY() {
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
	 * @param c the proposition to observe. If null, the node became not observable node.
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
	public void setX(final Double x) {
		this.x = x;
	}

	/**
	 * Setter for the field <code>y</code>.
	 *
	 * @param y the y to set
	 */
	public void setY(final Double y) {
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

}
