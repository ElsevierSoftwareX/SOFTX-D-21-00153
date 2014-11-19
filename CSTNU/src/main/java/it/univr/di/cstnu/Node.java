/**
 * 
 */
package it.univr.di.cstnu;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;

/**
 * @author posenato
 */
public class Node extends Component {

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
	public static Transformer<Node, String> vertexLabelTransformer = new ToStringLabeller<Node>() {
		@Override
		public String transform(Node v) {
			return v.getName();
		}
	};

	/**
	 * Transformer object to show the tooltip of node: the label is print.
	 */
	public static Transformer<Node, String> vertexToolTipTransformer = new ToStringLabeller<Node>() {
		@Override
		public String transform(Node v) {
			return "Label: " + v.getLabel().toString();
		}
	};

	/**
	 * @return a factory to build empty nodes.
	 */
	public static Factory<Node> getFactory() {
		return new Factory<Node>() {
			@Override
			public Node create() {
				return new Node("n" + Node.idSeq++, new Label());// if you change this default, change also in GraphMLReader
			}
		};
	}

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
	 * Constructor for cloning.
	 * 
	 * @param n the node to copy.
	 */
	Node(Node n) {
		super(n);
		label = new Label(n.label);
		propositionObserved = (n.getObservable() == null) ? null : new Literal(n.getObservable());
		x = n.x;
		y = n.y;
	}

	/**
	 * @param string
	 */
	public Node(String string) {
		super(string);
		label = Label.emptyLabel;
		x = y = new Double(0);
	}

	/**
	 * Standard constructor
	 * 
	 * @param n
	 * @param l
	 */
	Node(String n, Label l) {
		super(n);
		label = l;
		propositionObserved = null;
		x = y = new Double(0);
	}

	/**
	 * Standard constructor for an observation node
	 * 
	 * @param n
	 * @param proposition proposition observed by this node.
	 */
	Node(String n, Literal proposition) {
		super(n);
		label = Label.emptyLabel;
		if (proposition != null) proposition.setNegative(false);
		propositionObserved = proposition;
		x = y = new Double(0);
	}

	/**
	 * @return the label
	 */
	public Label getLabel() {
		return label;
	}

	/**
	 * @return the proposition under the control of this node. In null, no observation is made.
	 */
	public Literal getObservable() {
		return propositionObserved;
	}

	/**
	 * @return the x
	 */
	public Double getX() {
		return x;
	}

	/**
	 * @return the y
	 */
	public Double getY() {
		return y;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(Label label) {
		this.label = (label == null) ? Label.emptyLabel : label;
	}

	/**
	 * @param s the label to set
	 */
	public void setLabel(String s) {
		Label l = Label.emptyLabel;
		if ((s != null) && !s.isEmpty()) l = Label.parse(s);
		label = l;
	}

	/**
	 * Set the proposition to be observed.
	 * 
	 * @param l the proposition to observe. If the literal is negated, it is made straight. If null, the node became not observable node.
	 */
	public void setObservable(Literal l) {
		if (l != null) l.setNegative(false);
		propositionObserved = l;
	}

	/**
	 * @param x the x to set
	 */
	public void setX(Double x) {
		this.x = x;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(Double y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "〈" + getName() + "; " + getLabel().toString() + "; Obs: " + propositionObserved + "〉";
	}
}
