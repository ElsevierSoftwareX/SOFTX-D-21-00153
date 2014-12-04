/**
 *
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;
import it.univr.di.labeledvalue.Literal.State;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

/**
 * @author posenato
 */
public class LabeledNode extends Component {

	/**
	 * @return a factory to build empty nodes.
	 */
	public static Factory<LabeledNode> getFactory() {
		return new Factory<LabeledNode>() {
			@Override
			public LabeledNode create() {
				return new LabeledNode("n" + LabeledNode.idSeq++, Label.emptyLabel);// if you change this default, change also in GraphMLReader
			}
		};
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To provide a unique id for the default creation of component.
	 */
	static int idSeq = 0;

	/**
	 * Used to show the node name.
	 */
	public static Transformer<LabeledNode, String> vertexLabelTransformer = new ToStringLabeller<LabeledNode>() {
		@Override
		public String transform(final LabeledNode v) {
			return v.getName();
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
	Literal propositionObserved;

	/**
	 * @param string
	 */
	public LabeledNode(final String string) {
		super(string);
		this.label = Label.emptyLabel;
		this.x = this.y = new Double(0);
		this.propositionObserved = null;
	}

	/**
	 * Standard constructor for an observation node
	 *
	 * @param n name of the node.
	 * @param proposition proposition observed by this node.
	 */
	public LabeledNode(final String n, final Literal proposition) {
		super(n);
		this.label = Label.emptyLabel;
		this.propositionObserved = (proposition != null) ? new Literal(proposition, State.straight) : null;
		this.x = this.y = new Double(0);
	}

	/**
	 * Constructor for cloning.
	 *
	 * @param n the node to copy.
	 */
	LabeledNode(final LabeledNode n) {
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
		this.propositionObserved = null;
		this.x = this.y = new Double(0);
	}

	/**
	 * @return the label
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * @return the proposition under the control of this node. null, if no observation is made.
	 */
	public Literal getPropositionObserved() {
		return this.propositionObserved;
	}

	/**
	 * @return the x
	 */
	public Double getX() {
		return this.x;
	}

	/**
	 * @return the y
	 */
	public Double getY() {
		return this.y;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(final Label label) {
		this.label = (label == null) ? Label.emptyLabel : label;
	}

	/**
	 * @param s the label to set
	 */
	public void setLabel(final String s) {
		this.label = ((s != null) && !s.isEmpty()) ? Label.parse(s) : Label.emptyLabel;
	}

	/**
	 * Set the proposition to be observed.
	 *
	 * @param l the proposition to observe. If null, the node became not observable node.
	 */
	public void setObservable(final Literal l) {
		this.propositionObserved = (l != null) ? new Literal(l, State.straight) : null;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(final Double x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(final Double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("〈");
		sb.append(this.getName());
		sb.append("; ");
		sb.append(this.getLabel());
		if (this.propositionObserved != null) {
			sb.append("; Obs: ");
			sb.append(this.propositionObserved);
		}
		sb.append("〉");
		return sb.toString();
	}
}
