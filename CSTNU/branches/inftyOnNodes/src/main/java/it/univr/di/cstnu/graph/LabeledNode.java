package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import com.google.common.base.Function;
import com.google.common.base.Supplier;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap.LabeledALabelIntTreeMapView;
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
	 * Counts how many times a labeled value has been updated.
	 */
	Object2IntMap<Label> count;

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
		this.count = new Object2IntLinkedOpenHashMap<>(n.count);
		this.count.defaultReturnValue(Constants.INT_NULL);
	}

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
		this.count = new Object2IntLinkedOpenHashMap<>();
		this.count.defaultReturnValue(Constants.INT_NULL);
	}

	/**
	 * Standard constructor for an observation node
	 *
	 * @param n name of the node.
	 * @param proposition proposition observed by this node.
	 */
	public LabeledNode(final String n, final char proposition) {
		this(n);
		this.propositionObserved = (Literal.check(proposition)) ? proposition : Constants.UNKNOWN;
	}

	/**
	 * Standard constructor
	 *
	 * @param n
	 * @param l
	 */
	LabeledNode(final String n, final Label l) {
		this(n);
		this.label = l;
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
		this.potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
	}

	/**
	 * @return a copy of the potential of this node.
	 */
	public LabeledALabelIntTreeMap clonePotential() {
		return new LabeledALabelIntTreeMap(this.potential);
	}

	/**
	 * @return the alabel
	 */
	public ALabel getAlabel() {
		return this.alabel;
	}

	/**
	 * @return the set of alabels in the potential.
	 */
	public ObjectSet<ALabel> getALabelsOfPotential() {
		return this.potential.keySet();
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
	 * @return a read-only view of potential.
	 */
	public LabeledALabelIntTreeMapView getPotentialAll() {
		return this.potential.unmodifiable();
	}

	/**
	 * @param aLabel
	 * @return the labeled values associated to <code>alabel</code> in the node potential.
	 */
	public LabeledIntTreeMap getPotentialOfUC(ALabel aLabel) {
		return this.potential.get(aLabel);
	}


	/**
	 * @param aLabel
	 * @param l
	 * @return the value associated to aLabel and l in the potential
	 */
	public int getPotentialOfUC(ALabel aLabel, Label l) {
		return this.potential.getValue(l, aLabel);
	}

	
	/**
	 * @return a read-only copy of the potential counters.
	 */
	public Object2IntMap<Label> getPotentialCount() {
		return Object2IntMaps.unmodifiable(this.count);
	}

	/**
	 * @param l a not null label
	 * @return the counter value associate to label l. If the value does not exists, returns {@link Constants#INT_NULL}
	 */
	public int getPotentialCount(Label l) {
		return this.count.getInt(l);
	}

	/**
	 * @param aLabel
	 * @return the entry set of labeled values associated to alabel in the potential of the node.
	 */
	public ObjectSet<Entry<Label>> getPotentialEntrySetOfUC(ALabel aLabel) {
		return this.potential.get(aLabel).entrySet();
	}

	/**
	 * Shortcut for {@link #getPotentialOfUC(ALabel)} with argument {@link ALabel#emptyLabel}.
	 * 
	 * @return the labeled values getPotential(ALabel.emptyLabel).
	 * @see #getPotentialOfUC(ALabel)
	 */
	public LabeledIntTreeMap getPotential() {
		return this.getPotentialOfUC(ALabel.emptyLabel);
	}

	/**
	 * Shortcut for {@link #getPotentialOfUC(ALabel, Label)} with argument {@link ALabel#emptyLabel}.
	 * 
	 * @param l
	 * @return the labeled value getPotential(ALabel.emptyLabel, Label).
	 * @see #getPotentialOfUC(ALabel,Label)
	 */
	public int getPotential(Label l) {
		return this.getPotentialOfUC(ALabel.emptyLabel, l);
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
	public boolean isPotentialEqual(LabeledALabelIntTreeMap inputPotential) {
		return this.potential.equals(inputPotential);
	}

	/**
	 * @param aLabel
	 * @param l
	 * @param value
	 * @return true if the triple has been merged.
	 */
	final public boolean putPotential(ALabel aLabel, Label l, int value) {
		// if (value < 0 && this.getPropositionObserved() != Constants.UNKNOWN) {// Rule qR0 It is better to solve it in a proper rule for avoiding collateral
		// effects.
		// l = l.remove(getPropositionObserved());
		// }
		return this.potential.mergeTriple(l, aLabel, value, false);
	}

	/**
	 * @param aLabel
	 * @param map
	 * @return true if the triple has been merged.
	 */
	public boolean putPotential(ALabel aLabel, LabeledIntMap map) {
		if (map == null)
			return false;
		boolean added = false;
		for (Entry<Label> entry : map.entrySet()) {
			added |= this.putPotential(aLabel, entry.getKey(), entry.getIntValue());
		}
		return added;
	}

	/**
	 * Puts the labeled value (value, l) into the potential maps setting the a-letter empty.
	 * This method is a shorthand for {@link #putPotential(ALabel.emptyLabel, Label, int)}.
	 * 
	 * @param l
	 * @param value
	 * @return true if the triple has been merged.
	 */
	@SuppressWarnings("javadoc")
	final public boolean putPotential(Label l, int value) {
		return this.putPotential(ALabel.emptyLabel, l, value);
	}

	/**
	 * @param l a not null label
	 * @param reset true if the count has to be reset
	 * @return the old value associate to to label l. If the old value does not exists, returns {@link Constants#INT_NULL}
	 */
	public int updatePotentialCount(Label l, boolean reset) {
		if (l == null)
			return Constants.INT_NULL;
		int i = this.count.getInt(l);
		i = (i == Constants.INT_NULL || reset) ? 1 : i + 1;
		return this.count.put(l, i);
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
	 * If potential does not contain the map associated to {@link ALabel#emptyLabel}, an empty map is added.
	 * If potential is null, it does nothing.
	 * 
	 * @param potential
	 */
	public void setPotential(LabeledALabelIntTreeMap potential) {
		if (potential == null)
			return;
		if (potential.get(ALabel.emptyLabel) == null) {
			potential.put(ALabel.emptyLabel, new LabeledIntTreeMap());
		}
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
}
