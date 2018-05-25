package it.univr.di.labeledvalue;

import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntMap} interface.
 * <p>
 * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
 *
 * @author Roberto Posenato
 * @see LabeledIntMap
 * @version $Id: $Id
 */
public class LabeledIntHierarchyMap extends AbstractLabeledIntMap {

	/**
	 * Simple class to represent a labeled value in the hierarchy.
	 * 
	 * @author posenato
	 */
	static class HierarchyNode implements Object2IntMap.Entry<Label>, Comparable<HierarchyNode> {
		/**
		 * Labeled values subsumed by this.
		 */
		ObjectArraySet<HierarchyNode> father;
		/**
		 * 
		 */
		Label label;
		/**
		 * Labeled values that subsume this.
		 */
		ObjectArraySet<HierarchyNode> son;
		/**
		 * 
		 */
		int value;

		/**
		 * To manage visit
		 */
		int visit;

		/**
		 * 
		 */
		private HierarchyNode() {
			this.label = null;
			this.value = Constants.INT_NULL;
			this.visit = 0;
			this.father = null;
			this.son = null;
		}

		/**
		 * @param l
		 * @param v
		 */
		public HierarchyNode(Label l, int v) {
			this();
			this.label = Label.clone(l);
			this.value = v;
		}

		/**
		 * Clear all internal objects making them empty.
		 */
		public void clear() {
			if (this.son != null) {
				this.son.clear();
				this.son = null;
			}
			if (this.father != null) {
				this.father.clear();
				this.father = null;
			}
			if (this.label != null) {
				if (this.label.equals(Label.emptyLabel)) {
					this.label = Label.emptyLabel; //this is necessary to restore the original empty root node 
					this.value = Constants.INT_POS_INFINITE;
					return;
				}
				this.label.clear();
				this.label = null;// It is fundamental for checking if a HirearchyNode has been deleted.
			}
			this.value = Constants.INT_NULL;
		}

		// @Override
		// public int compareTo(HierarchyNode o) {
		// int v = label.compareTo(o.label);
		// if (v == 0) {
		// if (value == o.value)
		// return 0;
		// if (value == Constants.INT_NEG_INFINITE || o.value == Constants.INT_POS_INFINITE)
		// return -1;
		// if (o.value == Constants.INT_NEG_INFINITE || value == Constants.INT_POS_INFINITE)
		// return 1;
		// return value - o.value;
		// }
		// return v;
		// }

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Object2IntMap.Entry<?>))
				return false;
			@SuppressWarnings("unchecked")
			Entry<Label> o1 = (Entry<Label>) o;
			return this.label.equals(o1.getKey()) && this.value == o1.getIntValue();
		}

		@Override
		public int getIntValue() {
			return this.value;
		}

		@Override
		public Label getKey() {
			return this.label;
		}

		@Override
		@Deprecated
		public Integer getValue() {
			return this.value;
		}

		@Override
		public int hashCode() {
			return this.label.hashCode() + this.value;
		}

		@Override
		public int setValue(int value) {
			int old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public Integer setValue(Integer value) {
			return setValue(value.intValue());
		}

		@Override
		public String toString() {
			if (this.label == null)
				return "";
			return entryAsString(this);
		}

		/**
		 * * In order to minimize the number of insertions, it is better to have sons ordered in inverted lexicographical order.
		 */
		@Override
		public int compareTo(HierarchyNode o) {
			if (o == null || o.label == null)
				return -1;

			return o.label.compareTo(this.label);
		}
	}

	/**
	 * Simple class to store some found conditions during a recursion.
	 * 
	 * @author posenato
	 */
	private static class RecursionStatus {
		/**
		 * True if a new labeled value is greater that one already present.
		 */
		boolean valueGreaterThanCurrent = false;
		/**
		 * True if the new labeled value substitutes one already present.
		 */
		boolean updateAPreviousValue = false;
		/**
		 * true if a labeled value has to be inserted and there is another equal labeled valued with a label that differs only for one opposite literal.
		 */
		// boolean foundTwin = false;

		/**
		 * true if a perfect match has been found
		 */
		boolean foundPerfectMatch = false;

		/**
		 * 
		 */
		public RecursionStatus() {
		}

		@Override
		public String toString() {
			return "valueGreaterThanCurrentvalueGreaterThanCurrent: " + this.valueGreaterThanCurrent
					+ "\nvalueGreaterThanCurrent: " + this.valueGreaterThanCurrent
					+ "\nupdateAPreviousValue: " + this.updateAPreviousValue
					+ "\nfoundPerfectMatch: " + this.foundPerfectMatch;
		}
	}

	/**
	 * logger
	 */
	static private Logger LOG = Logger.getLogger(LabeledIntHierarchyMap.class.getName());

	/**
	 *
	 */
	static private final long serialVersionUID = 3L;

	/**
	 * When a father has to be add a set of father, it is necessary to check that there is no other father that subsumes the new one.
	 * 
	 * @param newFather
	 * @param nodeWhereToAddFather
	 */
	private static void addFatherTo(HierarchyNode newFather, HierarchyNode nodeWhereToAddFather) {
		if (newFather == null || nodeWhereToAddFather == null)
			return;

		if (nodeWhereToAddFather.father == null) {
			nodeWhereToAddFather.father = new ObjectArraySet<>();
		}
		if (nodeWhereToAddFather.father.size() == 0) {
			nodeWhereToAddFather.father.add(newFather);
			return;
		}
		// iterator remove is not implemented in iterator of ObjectArraySet.
		HierarchyNode[] fatherA = nodeWhereToAddFather.father.toArray(new HierarchyNode[1]);
		for (HierarchyNode fatherInSet : fatherA) {
			if (fatherInSet == null)
				continue;
			if (fatherInSet.label.subsumes(newFather.label))
				return;
			if (newFather.label.subsumes(fatherInSet.label)) {
				nodeWhereToAddFather.father.remove(fatherInSet);
				addFatherTo(fatherInSet, newFather);
			}
		}
		nodeWhereToAddFather.father.add(newFather);
	}

	/**
	 * This helper method checks if newSon has nodeWhereToAddSon as father and, in positive case, adds newSon as so to nodeWhereToAddSon.
	 * 
	 * @param newSon
	 * @param nodeWhereToAddSon
	 */
	private static void addSonTo(HierarchyNode newSon, HierarchyNode nodeWhereToAddSon) {
		if (newSon == null || nodeWhereToAddSon == null || newSon.father == null || !newSon.father.contains(nodeWhereToAddSon))
			return;

		if (nodeWhereToAddSon.son == null) {
			nodeWhereToAddSon.son = new ObjectArraySet<>();
		}
		nodeWhereToAddSon.son.add(newSon);
	}

	/**
	 * main.
	 *
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 */
	static public void main(final String[] args) {

		final int nTest = (int) 1E3;
		final double msNorm = 1.0E6 * nTest;

		final LabeledIntMap map = new LabeledIntHierarchyMap();

		final Label l1 = Label.parse("abc¬f");
		final Label l2 = Label.parse("abcdef");
		final Label l3 = Label.parse("a¬bc¬de¬f");
		final Label l4 = Label.parse("¬b¬d¬f");
		final Label l5 = Label.parse("ec");
		final Label l6 = Label.parse("¬fedcba");
		final Label l7 = Label.parse("ae¬f");
		final Label l8 = Label.parse("¬af¿b");
		final Label l9 = Label.parse("¬af¿b");
		final Label l10 = Label.parse("¬ec");
		final Label l11 = Label.parse("abd¿f");
		final Label l12 = Label.parse("a¿d¬f");
		final Label l13 = Label.parse("¬b¿d¿f");
		final Label l14 = Label.parse("b¬df¿e");
		final Label l15 = Label.parse("e¬c");
		final Label l16 = Label.parse("ab¿d¿f");
		final Label l17 = Label.parse("ad¬f");
		final Label l18 = Label.parse("b¿d¿f");
		final Label l19 = Label.parse("¬b¬df¿e");
		final Label l20 = Label.parse("¬e¬c");

		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			map.clear();
			map.put(Label.emptyLabel, 109);
			map.put(l1, 10);
			map.put(l2, 20);
			map.put(l3, 25);
			map.put(l4, 23);
			map.put(l5, 22);
			map.put(l6, 23);
			map.put(l7, 20);
			map.put(l8, 20);
			map.put(l9, 21);
			map.put(l10, 11);
			map.put(l11, 11);
			map.put(l12, 11);
			map.put(l13, 24);
			map.put(l14, 22);
			map.put(l15, 23);
			map.put(l16, 20);
			map.put(l17, 23);
			map.put(l18, 23);
			map.put(l19, 23);
			map.put(l20, 23);
		}
		long endTime = System.nanoTime();
		System.out.println("LABELED VALUE SET-HIERARCHY MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map
				+ ".\nTime: (ms): "
				+ ((endTime - startTime) / msNorm));
		String rightAnswer = "{(⊡, 23) (¬a¿bf, 20) (abcdef, 20) (abc¬f, 10) (abd¿f, 11) (a¿d¬f, 11) (ae¬f, 20) (b¬d¿ef, 22) (c, 22) (c¬e, 11) }";
		System.out.println("The right final set is " + rightAnswer + ".");
		System.out.println("Is equal? " + AbstractLabeledIntMap.parse(rightAnswer).equals(map));

		startTime = System.nanoTime();
		int min = 1000;
		for (int i = 0; i < nTest; i++) {
			min = map.getMinValue();
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for determining the min value (" + min + ") (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		startTime = System.nanoTime();
		Label l = Label.parse("abd¿f");
		for (int i = 0; i < nTest; i++) {
			min = map.get(l);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for retrieving value of label " + l + " (mean over " + nTest + " tests). (ms): "
				+ ((endTime - startTime) / msNorm));

		map.put(Label.parse("c"), 11);
		map.put(Label.parse("¬c"), 11);
		System.out.println("After the insertion of (c,11) and (¬c,11) the map becomes: " + map);
	}

	/**
	 * Remove the given node from the hierarchy adjusting possible its sons w.r.t. its father(s).
	 * 
	 * @param nodeToRemove
	 *            node to remove
	 */
	private static void removeNode(HierarchyNode nodeToRemove) {
		if (nodeToRemove == null || nodeToRemove.label == null || nodeToRemove.father == null || nodeToRemove.father.size() == 0)
			return;

		// It is efficient to remove nodeToRemove as father in each son as first action!
		if (nodeToRemove.son != null && nodeToRemove.son.size() > 0) {
			for (HierarchyNode sonOfNodeToRemove : nodeToRemove.son) {
				sonOfNodeToRemove.father.remove(nodeToRemove);
			}
		}

		// for each father gFather of nodeToRemove,
		// 1. remove nodeToRemove as son
		// 2. check if each nodeToRemove's son X has to become son of gFather
		for (HierarchyNode gFather : nodeToRemove.father) {
			assert (gFather.son != null && gFather.son.size() > 0);
			gFather.son.remove(nodeToRemove);

			if (nodeToRemove.son == null || nodeToRemove.son.size() == 0)
				continue;

			for (HierarchyNode sonOfNodeToRemove : nodeToRemove.son) {
				// Each nodeToRemove's son X becomes son of gFather if and only if no other X's father subsumes gFather.
				if (sonOfNodeToRemove.value >= gFather.value)// sanity check
					continue;
				addFatherTo(gFather, sonOfNodeToRemove);// sonOfNodeToRemove.father.add(gFather);
				addSonTo(sonOfNodeToRemove, gFather);
			}
		}
		nodeToRemove.clear();
	}

	/**
	 * Removes 'nodeToRemove' because it has been made redundant by 'newNode' and adjusts nodeToRemove's sons w.r.t. newNode as a possible father.
	 * 
	 * @param nodeToRemove
	 * @param newNode
	 */
	private static void removeNodeAndAddFather(HierarchyNode nodeToRemove, HierarchyNode newNode) {
		if (nodeToRemove == null || nodeToRemove.label == null || nodeToRemove.father == null || nodeToRemove.father.size() == 0 || newNode == null)
			return;

		// Maintain a copy of son set of nodeToRemove
		ObjectArraySet<HierarchyNode> grandChildren = (nodeToRemove.son != null) ? new ObjectArraySet<>(nodeToRemove.son) : null;

		removeNode(nodeToRemove);

		if (grandChildren == null || grandChildren.size() == 0)
			return;

		for (HierarchyNode son : grandChildren) {
			addFatherTo(newNode, son);
			addSonTo(son, newNode);
		}
	}

	/**
	 * Replace currentNode with newNode.
	 * 
	 * @param currentNode
	 * @param newNode
	 */
	private static void replace(HierarchyNode currentNode, HierarchyNode newNode) {
		for (HierarchyNode father : currentNode.father) {
			father.son.remove(currentNode);
		}
		if (currentNode.son != null && currentNode.son.size() > 0) {
			if (newNode.son == null)
				newNode.son = new ObjectArraySet<>();
			for (HierarchyNode son : currentNode.son) {
				son.father.remove(currentNode);
				addFatherTo(newNode, son);// son.father.add(newNode);
				addSonTo(son, newNode);
			}
		}
	}

	/**
	 * @param label 
	 * @param value 
	 * @return true if the node is the original emptyRoot node.
	 */
	private static final boolean isEmptyRootNode(Label label, int value) {
		if (label == null || value == Constants.INT_NULL)
			return false;
		return (label == Label.emptyLabel && value == Constants.INT_POS_INFINITE);
	}

	/**
	 * EmptyRoot necessary to initialize the tree. If the definition is changed, then {@link #isEmptyRootNode(Label, int)} and
	 * {@link LabeledIntHierarchyMap.HierarchyNode#clear()} have to be updated!
	 */
	private HierarchyNode emptyRootNode = new HierarchyNode(Label.emptyLabel, Constants.INT_POS_INFINITE);

	/**
	 * Root of hierarchy Design choice: the set of labeled values of this map is organized as a double linked hierarchy of labeled values. A labeled value
	 * (label, value) is father of another labeled value (label1, value1) if label1 subsumes label and value1 &lt; value.
	 */
	private HierarchyNode root;

	/**
	 * Just to force the control that, after each put, the format of hierarchy is still valid. Don't set true in a production program!
	 */
	public boolean wellFormatCheck = false;

	/**
	 * Just for debugging
	 */
	private String putHistory = "";

	/**
	 * Necessary constructor for the factory.
	 * The internal structure is built and empty.
	 */
	LabeledIntHierarchyMap() {
		// Root has to be set. I choose to use (+infty, emptyLabel) as root of an empty hierarchy
		this.root = this.emptyRootNode;
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm
	 *            the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 */
	LabeledIntHierarchyMap(final LabeledIntMap lvm) {
		this();
		if (lvm == null)
			return;

		for (Entry<Label> e : lvm.entrySet()) {
			put(e.getKey(), e.getIntValue());
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		recursiveClear(this.root, this.root.visit + 1);
		this.root.visit++;
		this.putHistory = "";
	}

	@Override
	public LabeledIntHierarchyMap createLabeledIntMap() {
		return new LabeledIntHierarchyMap();
	}

	@Override
	public LabeledIntHierarchyMap createLabeledIntMap(LabeledIntMap lim) {
		return new LabeledIntHierarchyMap(lim);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Entry<Label>> entrySet() {
		final ObjectSet<Entry<Label>> coll = new ObjectArraySet<>();
		return entrySet(coll);
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Entry<Label>> entrySet(ObjectSet<Entry<Label>> setToReuse) {
		setToReuse.clear();
		recursiveBuildingSet(this.root, setToReuse, (this.root.visit + 1));
		this.root.visit++;
		return setToReuse;
	}

	/** {@inheritDoc} */
	@Override
	public int get(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		HierarchyNode node = recursiveGet(this.root, l);
		return (node == null || node.label == null) ? Constants.INT_NULL : node.value;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		RecursionStatus status = new RecursionStatus();
		int min = recursiveGetMinConsistentWith(this.root, l, Constants.INT_POS_INFINITE, status);
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueSubsumedBy(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		RecursionStatus status = new RecursionStatus();
		int min = recursiveGetMinSubsumedBy(this.root, l, Constants.INT_POS_INFINITE, status);
		return (min == Constants.INT_POS_INFINITE) ? Constants.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.entrySet().hashCode();
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet() {
		ObjectSet<Label> coll = new ObjectArraySet<>();
		return keySet(coll);
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	@Override
	public ObjectSet<Label> keySet(ObjectSet<Label> setToReuse) {
		setToReuse.clear();
		for (final Entry<Label> entry : this.entrySet()) {
			if (isEmptyRootNode(entry.getKey(),entry.getIntValue()))
				continue;
			setToReuse.add(entry.getKey());
		}
		return setToReuse;
	}

	/**
	 * Check and manage the case that currentNode has a label that differs from newNode one by only one literal. This procedure can determine a removing cascade
	 * of element that cannot be controlled in a safe way during a recursion (this procedure is called during a recursion insert).
	 * Therefore, for now, it is disabled.
	 * 
	 * @param currentNode
	 * @param newNode
	 * @param sonOfSpin
	 * @param status
	 * @return true if a labeled value with only one opposite literal w.r.t. literals in newNode label and with same value has been found; false otherwise.
	 */
	@SuppressWarnings("static-method")
	private boolean manageSpin(HierarchyNode currentNode, HierarchyNode newNode, ObjectArraySet<HierarchyNode> sonOfSpin, RecursionStatus status) {
		if (currentNode == null || newNode == null)
			return false;

		Literal p = newNode.label.getUniqueDifferentLiteral(currentNode.label);
		if (p == null)
			return false;
		int max = newNode.value > currentNode.value ? newNode.value : currentNode.value;

		// if (max == newNode.value && max == currentNode.value) {
		// // Both nodes have to be replaced!
		// removeNode(currentNode);
		// removeNode(newNode);
		// } else {
		// if (max == newNode.value) {
		// // Only the newNode have to be adjusted!
		// removeNode(newNode);
		// } else {
		// // currentNode has to be replaced by the shorten one, but it cannot be removed because its sons could be lost.
		// removeNode(currentNode);
		// // newNode has to be inserted!
		// // I prefer to insert again from root!
		// if (newNode.father == null)// newNode has not yet inserted!
		// put(newNode.label, newNode.value);
		// }
		// }
		Label labelWOp = Label.clone(newNode.label);
		labelWOp.remove(p.getName());
		if (labelWOp.isEmpty())
			labelWOp = Label.emptyLabel;
		// status.foundTwin = true;
		HierarchyNode spin = new HierarchyNode(labelWOp, max);
		sonOfSpin.add(spin);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean put(Label newLabel, int newValue) {
		if ((newLabel == null) || (newValue == Constants.INT_NULL))
			return false;

		HierarchyNode newNode = new HierarchyNode(newLabel, newValue);
		RecursionStatus status = new RecursionStatus();
		if (this.wellFormatCheck) {
			this.putHistory += newNode + " ";
		}
		ObjectArraySet<HierarchyNode> sonOfSpin = new ObjectArraySet<>();
		boolean st = recursivePut(this.root, newNode, sonOfSpin, status);
		// TODO
		if (sonOfSpin.size() > 0) {
			// there is new element determined by simplification of labels with one opposite literal (e.g., (¬ab,3) (ab,4) --> (b,4) has to be added).
			for (HierarchyNode newNodeSpin : sonOfSpin) {
				if (newNodeSpin.label != null)
					st = put(newNodeSpin.label, newNodeSpin.value) || st;
			}
		}
		// TODO
		if (this.wellFormatCheck && this.root != null && this.root.son != null) {
			for (HierarchyNode son : this.root.son) {
				for (HierarchyNode son1 : this.root.son) {
					if (son != null && son1 != null && son != son1 && (son.label.subsumes(son1.label) || son1.label.subsumes(son.label))) {
						LOG.severe("Hierarchy: " + this.toString());
						LOG.severe("Son: " + this.root.son);
						LOG.severe("Put history: " + this.putHistory);
						throw new IllegalStateException("Hirearchy is not in a right format.");
					}
				}
			}
		}
		return st;
	}

	/**
	 * @param node
	 * @param coll
	 * @param visit
	 */
	private void recursiveBuildingSet(HierarchyNode node, ObjectSet<Entry<Label>> coll, int visit) {
		if (node.visit == visit)
			return;
		if (node.son != null && node.son.size() > 0) {
			for (HierarchyNode n : node.son) {
				recursiveBuildingSet(n, coll, visit);
			}
		}
		if (node.label != null && node.value != Constants.INT_NULL && !isEmptyRootNode(node.label, node.value))
			coll.add(node);
		node.visit = visit;
	}

	/**
	 * Clear the hierarchy in a recursive way.
	 * 
	 * @param node
	 * @param visit
	 */
	private void recursiveClear(HierarchyNode node, int visit) {
		if (node.visit == visit)
			return;
		if (node.son != null && node.son.size() > 0) {
			for (HierarchyNode n : node.son) {
				recursiveClear(n, visit);
			}
		}
		node.clear();
		node.visit = visit;
	}

	/**
	 * Counts elements of the hierarchy in a recursive way.
	 * 
	 * @param node
	 * @param visit
	 * @return number of nodes below node
	 */
	private int recursiveCount(HierarchyNode node, int visit) {
		if (node.visit == visit)
			return 0;
		int sum = 0;
		if (node.son != null && node.son.size() > 0) {
			for (HierarchyNode n : node.son) {
				sum += recursiveCount(n, visit);
			}
		}
		node.visit = visit;
		if (node != this.root) {
			return sum + 1;
		}
		if (!isEmptyRootNode(node.label, node.value)) {
			return sum + 1;
		}
		return sum;
	}

	/**
	 * @param node
	 * @param l
	 * @return the value associated to label l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	private HierarchyNode recursiveGet(final HierarchyNode node, final Label l) {
		if (node.label == null) {
			LOG.severe("A removed node is still present as son!");
			return null;
		}
		if (node.label.equals(l))
			return node;
		if (node.son == null || node.son.size() == 0)
			return null;
		for (HierarchyNode n : node.son) {
			if (!l.subsumes(n.label))
				continue;
			HierarchyNode v = recursiveGet(n, l);
			if (v != null)
				return v;
		}
		return null;
	}

	/**
	 * @param node
	 * @param l
	 * @param min
	 * @param status
	 * @return the min value associated or consistent to label l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	private int recursiveGetMinConsistentWith(final HierarchyNode node, final Label l, int min, RecursionStatus status) {
		if (status.foundPerfectMatch)
			return min;
		if (node.label.equals(l)) {
			status.foundPerfectMatch = true;
			return node.value;
		}
		if (node.label.isConsistentWith(l)) {
			if (node.value < min)
				min = node.value;
		} else {
			return min;
		}
		if (node.son == null || node.son.size() == 0) {
			return min;
		}
		for (HierarchyNode n : node.son) {
			if (!l.subsumes(n.label))
				continue;
			int v = recursiveGetMinConsistentWith(n, l, min, status);
			if (status.foundPerfectMatch)
				return v;
			if (v < min)
				min = v;
		}
		return min;
	}

	
	
	/**
	 * @param node
	 * @param l
	 * @param min
	 * @param status
	 * @return the min value subsumed by label l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	private int recursiveGetMinSubsumedBy(final HierarchyNode node, final Label l, int min, RecursionStatus status) {
		if (status.foundPerfectMatch)
			return min;
		if (node.label.equals(l)) {
			status.foundPerfectMatch = true;
			return node.value;
		}
		if (node.label.subsumes(l)) {
			if (node.value < min)
				min = node.value;
		} else {
			return min;
		}
		if (node.son == null || node.son.size() == 0) {
			return min;
		}
		for (HierarchyNode n : node.son) {
			if (!l.subsumes(n.label))
				continue;
			int v = recursiveGetMinSubsumedBy(n, l, min, status);
			if (status.foundPerfectMatch)
				return v;
			if (v < min)
				min = v;
		}
		return min;
	}
	
	
	/**
	 * @param current
	 * @param newNode
	 * @param sonOfSpin
	 *            TODO
	 * @param recursionStatus
	 * @return true if the newNode has been inserted; false otherwise.
	 */
	private boolean recursivePut(HierarchyNode current, HierarchyNode newNode, ObjectArraySet<HierarchyNode> sonOfSpin, RecursionStatus recursionStatus) {
		if (current == null || newNode == null || newNode.label == null) {
			String msg = "Something wrong. newNode: " + newNode + ", current:" + current;
			LOG.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		boolean status = false;

		// 1. Manage possible spin label
		manageSpin(current, newNode, sonOfSpin, recursionStatus);
		// if (recursionStatus.foundTwin) {
		// return status;
		// }

		// 2. If new label does not subsume current label, it cannot stay in this sub-hierarchy.
		if (!newNode.label.subsumes(current.label))
			return false;

		// 3. If new value is greater, it cannot stay in this sub-hierarchy.
		if (newNode.value >= current.value) {
			recursionStatus.valueGreaterThanCurrent = true;
			return false;
		}

		// 4. Manage case current element has same label. (A value is already present)
		if (current.label.equals(newNode.label)) {
			current.value = newNode.value;
			if (current.son != null && current.son.size() > 0)
				recursiveUpdateSons(current.son.toArray(new HierarchyNode[1]), 0, newNode.value);
			recursionStatus.updateAPreviousValue = true;
			return true;
		}

		// 5. Descent the sub hierarchy checking if new node has to be insert as an update or as new node.
		status = false;
		if (current.son != null && current.son.size() > 0) {
			// LOG.severe("Current.son: " + current.son);
			HierarchyNode[] sonsReferenceCopy = current.son.toArray(new HierarchyNode[1]);
			for (HierarchyNode son : sonsReferenceCopy) {
				if (son.label == null) // === !currentNode.son.contains(son))
					continue;
				status = recursivePut(son, newNode, sonOfSpin, recursionStatus) || status;
				if (recursionStatus.updateAPreviousValue) {
					return status;
				}
				if (recursionStatus.valueGreaterThanCurrent) {
					if (status) {
						// If newNode has been already inserted but, then, it has been discovered that it shouldn't be inserted because there is a smaller
						// value, then newNode has to be removed!
						removeNode(newNode);
					}
					return false;
				}
			}
		}

		// 6. If it has been insert in the sub-hierarchy, everything has been done!
		if (status)
			return status;

		// Element has to be insert as son.
		addFatherTo(current, newNode);// newNode.father.add(current);
		addSonTo(newNode, current);

		// It could occur that some brothers have to be become sons of newNode!
		// Since currentNode.son will be possibly shorten, I need a copy to be sure to check all components.
		HierarchyNode[] sonsReferenceCopy = current.son.toArray(new HierarchyNode[1]);
		for (HierarchyNode son : sonsReferenceCopy) {
			if (son == null || son.label == null || son == newNode)
				continue;
			if (son.label.subsumes(newNode.label)) {
				if (son.value >= newNode.value) {
					// newNode has a shorter label and a better value, 'son' can be deleted.
					if (son.son != null && son.son.size() > 0) {
						recursiveUpdateSons(son.son.toArray(new HierarchyNode[1]), 0, newNode.value);
					}
					replace(son, newNode);
				} else {
					// son label subsumes newNode one and son value is smaller ==> son becomes son of newNode.
					current.son.remove(son);
					son.father.remove(current);
					addFatherTo(newNode, son);// son.father.add(newNode);
					addSonTo(son, newNode);
				}
			} else {
				if (son.son != null && son.son.size() > 0) {
					recursiveUpdateSiblingSons(son.son.toArray(new HierarchyNode[1]), 0, newNode, sonOfSpin, recursionStatus);
				}
				if (newNode.father != null && newNode.father.size() > 0) {// newNode is still valid
					if (newNode.label.subsumes(son.label)) {
						LOG.warning("It is strange that progam is here. newNode: " + newNode + ", son: " + son + ", global:" + this.toString());
						addFatherTo(son, newNode);
						addSonTo(son, newNode);
					}
				}
			}
		}
		return true;
	}

	/**
	 * After the insertion of newNode, it can occur that siblings' sons has to be update with respect to newNode.
	 *
	 * @param siblingsSons
	 * @param index
	 * @param newNode
	 * @param sonOfSpin
	 * @param recursionStatus
	 */
	private void recursiveUpdateSiblingSons(HierarchyNode[] siblingsSons, int index, HierarchyNode newNode, ObjectArraySet<HierarchyNode> sonOfSpin,
			RecursionStatus recursionStatus) {
		if (siblingsSons == null || siblingsSons.length == 0 || index == siblingsSons.length)
			return;
		// go to the last son
		if (index < siblingsSons.length) {
			recursiveUpdateSiblingSons(siblingsSons, index + 1, newNode, sonOfSpin, recursionStatus);
		}
		HierarchyNode currentSiblingSon = siblingsSons[index];
		if (currentSiblingSon == null)
			return;
		// goto the last grand-son
		// boolean consistent = newNode.label.isConsistentWith(currentSiblingSon.label); NOOO because there could be ¿ literal!!!
		if (currentSiblingSon.son != null && currentSiblingSon.son.size() > 0) {
			// Remember that it is necessary to go down even if newNode label is not consistent with currenSiblingSon one
			// because some sons can contain ¿ literals
			recursiveUpdateSiblingSons(currentSiblingSon.son.toArray(new HierarchyNode[1]), 0, newNode, sonOfSpin, recursionStatus);
		}
		// last son unchecked.
		manageSpin(currentSiblingSon, newNode, sonOfSpin, recursionStatus);// possible case: ¬ab-->¬abc and new node is ¬a¬bc
		// if (recursionStatus.foundTwin) {
		// return;
		// }
		if (currentSiblingSon.label.subsumes(newNode.label)) {
			if (currentSiblingSon.value >= newNode.value) {
				// It could that a label with ? can be nullified by a label with a smaller value and without ?
				// LOG.warning("CurrentNode: " + currentNode + "; NewNode: " + newNode);
				removeNodeAndAddFather(currentSiblingSon, newNode);
				return;
			}
			addFatherTo(newNode, currentSiblingSon);// currentSiblingSon.father.add(newNode);
			addSonTo(currentSiblingSon, newNode);
		}
	}

	/**
	 * Recursive removing all sons having a value greater than the given one.
	 * 
	 * @param son
	 * @param index
	 * @param newValue
	 */
	private void recursiveUpdateSons(HierarchyNode[] son, int index, int newValue) {
		if (son == null || son.length == 0 || index == son.length)
			return;
		// go to the last son
		if (index < son.length) {
			recursiveUpdateSons(son, index + 1, newValue);
		}
		HierarchyNode current = son[index];
		if (current == null)
			return;
		// goto the last grand-son
		if (current.son != null && current.son.size() > 0) {
			recursiveUpdateSons(current.son.toArray(new HierarchyNode[1]), 0, newValue);
		}
		// last son unchecked.
		if (current.value >= newValue) {
			removeNode(current);
		}
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		if (l == null)
			return Constants.INT_NULL;
		HierarchyNode node = recursiveGet(this.root, l);
		if (node == null || node.label == null)
			return Constants.INT_NULL;
		int v = node.value;
		removeNode(node);
		return v;
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (this.root == null)
			return 0;
		int sum = recursiveCount(this.root, this.root.visit + 1);
		this.root.visit++;
		return sum;
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		final IntArraySet coll = new IntArraySet(this.size());
		for (final Entry<Label> entry : this.entrySet()) {
			if (isEmptyRootNode(entry.getKey(), entry.getIntValue())) 
				continue;
			coll.add(entry.getIntValue());
		}
		return coll;
	}

	@Override
	public boolean alreadyRepresents(Label newLabel, int newValue) {
		// TODO Auto-generated method stub
		return false;
	}
}
