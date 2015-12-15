package it.univr.di.labeledvalue;

import java.util.logging.Logger;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

/**
 * Simple implementation of {@link it.univr.di.labeledvalue.LabeledIntHierarchyMap} interface.
 * <p>
 * When creating a object it is possible to specify if the labeled values represented into the map should be maintained to the minimal equivalent ones.
 *
 * @author Roberto Posenato
 * @see LabeledIntHierarchyMap
 * @version $Id: $Id
 */
public class LabeledIntHierarchyMap extends AbstractLabeledIntMap {

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
		boolean foundTwin = false;

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
			return "valueGreaterThanCurrentvalueGreaterThanCurrent: " + valueGreaterThanCurrent
					+ "\nvalueGreaterThanCurrent: " + valueGreaterThanCurrent
					+ "\nupdateAPreviousValue: " + updateAPreviousValue
					+ "\nfoundPerfectMatch: " + foundPerfectMatch;
		}
	}

	/**
	 * Simple class to represent a labeled value in the hierarchy.
	 * 
	 * @author posenato
	 */
	static class HierarchyNode implements Object2IntMap.Entry<Label> {
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
		 * @param l
		 * @param v
		 */
		public HierarchyNode(Label l, int v) {
			this();
			label = new Label(l);
			value = v;
		}

		/**
		 * 
		 */
		private HierarchyNode() {
			label = null;
			value = INT_NULL;
			visit = 0;
			father = null;
			son = null;
		}

		/**
		 * Clear all internal objects nullyfing them.
		 */
		public void clear() {
			if (son != null) {
				son.clear();
				son = null;
			}
			if (father != null) {
				father.clear();
				father = null;
			}
			if (label != null) {
				if (label.equals(Label.emptyLabel)) {
					value = Constants.INT_POS_INFINITE;
					return;
				}
				label.clear();
				label = null;
			}
			value = INT_NULL;
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
			return label.equals(o1.getKey()) && value == o1.getIntValue();
		}

		@Override
		public int hashCode() {
			return label.hashCode() + value;
		}

		@Override
		public String toString() {
			if (label == null)
				return "";
			return entryAsString(this);
		}

		@Override
		public Label getKey() {
			return label;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public Integer setValue(Integer value) {
			return setValue(value.intValue());
		}

		@Override
		public int setValue(int value) {
			int old = this.value;
			this.value = value;
			return old;
		}

		@Override
		public int getIntValue() {
			return value;
		}
	}

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger(LabeledIntHierarchyMap.class.getName());

	/**
	 *
	 */
	static private final long serialVersionUID = 2L;

	/**
	 * <p>
	 * main.
	 * </p>
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
		System.out.println("LABELED VALUE SET-HIERARCHY MANAGED\nExecution time for some merge operations (mean over " + nTest + " tests).\nFinal map: " + map + ".\nTime: (ms): "
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
	 * Replace currentNode with newNode.
	 * 
	 * @param currentNode
	 * @param newNode
	 */
	private static void replace(HierarchyNode currentNode, HierarchyNode newNode) {
		for (HierarchyNode father : currentNode.father) {
			father.son.remove(currentNode);
		}
		if (currentNode.son != null) {
			if (newNode.son == null)
				newNode.son = new ObjectArraySet<>();
			for (HierarchyNode son : currentNode.son) {
				son.father.remove(currentNode);
				son.father.add(newNode);
				newNode.son.add(son);
			}
		}
	}

	/**
	 * Root of hierarchy Design choice: the set of labeled values of this map is organized as a double linked hierarchy of labeled values. A labeled value
	 * (label, value) is father of another labeled value (label1, value1) if label1 subsumes label and value1 &lt; value.
	 */
	private HierarchyNode root;

	/**
	 * Simple constructor. The internal structure is built and empty.
	 */
	LabeledIntHierarchyMap() {
		root = new HierarchyNode(Label.emptyLabel, Constants.INT_POS_INFINITE);
	}

	/**
	 * Simple constructor. The internal structure is built and empty.
	 * 
	 * @param withOptimization
	 *            it is ignored!
	 */
	LabeledIntHierarchyMap(boolean withOptimization) {
		this();
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

		for (Entry<Label> e : this.entrySet()) {
			put(e.getKey(), e.getIntValue());
		}
	}

	/**
	 * Constructor to clone the structure.
	 *
	 * @param lvm
	 *            the LabeledValueTreeMap to clone. If lvm is null, this will be a empty map.
	 * @param withOptimization
	 *            it is ignored!
	 */
	LabeledIntHierarchyMap(final LabeledIntMap lvm, boolean withOptimization) {
		this(lvm);
	}

	/** {@inheritDoc} */
	@Override
	public void clear() {
		recursiveClear(this.root, this.root.visit + 1);
		this.root.visit++;
	}

	/** {@inheritDoc} */
	@Override
	public ObjectSet<Entry<Label>> entrySet() {
		final ObjectSet<Entry<Label>> coll = new ObjectArraySet<>();
		recursiveBuildingSet(this.root, coll, (this.root.visit + 1));
		this.root.visit++;
		return coll;
	}

	/** {@inheritDoc} */
	@Override
	public int get(final Label l) {
		if (l == null)
			return INT_NULL;
		return recursiveGet(this.root, l);
	}

	/** {@inheritDoc} */
	@Override
	public int getMinValueConsistentWith(final Label l) {
		if (l == null)
			return INT_NULL;
		RecursionStatus status = new RecursionStatus();
		int min = recursiveGetMinConsistentWith(this.root, l, Constants.INT_POS_INFINITE, status);
		return (min == Constants.INT_POS_INFINITE) ? LabeledIntMap.INT_NULL : min;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return this.entrySet().hashCode();
	}

	/**
	 * @return a set view of all labels present into this map.
	 */
	public ObjectSet<Label> keySet() {
		ObjectSet<Label> coll = new ObjectArraySet<>();
		for (final Entry<Label> entry : this.entrySet()) {
			if (entry.getKey() == Label.emptyLabel && entry.getIntValue() == Constants.INT_POS_INFINITE)
				continue;
			coll.add(entry.getKey());
		}
		return coll;
	}

	/** {@inheritDoc} */
	@Override
	public boolean put(Label newLabel, int newValue) {
		if ((newLabel == null) || (newValue == INT_NULL))
			return false;

		HierarchyNode newNode = new HierarchyNode(newLabel, newValue);
		RecursionStatus status = new RecursionStatus();
		return recursivePut(root, newNode, status);
	}

	/** {@inheritDoc} */
	@Override
	public int remove(final Label l) {
		if (l == null)
			return INT_NULL;
		return recursiveRemove(root, l);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		if (root == null)
			return 0;
		int sum = recursiveCount(root, root.visit + 1);
		root.visit++;
		return sum;
	}

	/** {@inheritDoc} */
	@Override
	public IntSet values() {
		final IntArraySet coll = new IntArraySet(this.size());
		for (final Entry<Label> entry : this.entrySet()) {
			if (entry.getKey() == Label.emptyLabel && entry.getIntValue() == Constants.INT_POS_INFINITE)
				continue;
			coll.add(entry.getIntValue());
		}
		return coll;
	}

	/**
	 * @param node
	 * @param coll
	 * @param visit
	 */
	private void recursiveBuildingSet(HierarchyNode node, ObjectSet<Entry<Label>> coll, int visit) {
		if (node.visit == visit)
			return;
		if (node.son != null) {
			for (HierarchyNode n : node.son) {
				recursiveBuildingSet(n, coll, visit);
			}
		}
		if (node.label != null && node.value != INT_NULL && node.label != Label.emptyLabel && node.value != Constants.INT_POS_INFINITE)
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
		if (node.son != null) {
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
		if (node.son != null) {
			for (HierarchyNode n : node.son) {
				sum += recursiveCount(n, visit);
			}
		}
		node.visit = visit;
		if (node != root) {
			return sum + 1;
		}
		if (node.label != Label.emptyLabel && node.value != Constants.INT_POS_INFINITE) {
			return sum + 1;
		}
		return sum;
	}

	/**
	 * @param node
	 * @param l
	 * @return the value associated to label l if it exists, {@link LabeledIntMap#INT_NULL} otherwise.
	 */
	private int recursiveGet(final HierarchyNode node, final Label l) {
		if (node.label.equals(l))
			return node.value;
		if (node.son == null)
			return INT_NULL;
		for (HierarchyNode n : node.son) {
			if (!l.subsumes(n.label))
				continue;
			int v = recursiveGet(n, l);
			if (v != INT_NULL)
				return v;
		}
		return INT_NULL;
	}

	/**
	 * @param node
	 * @param l
	 * @param min
	 * @param status
	 * @return the min value associated or consistent to label l if it exists, {@link LabeledIntMap#INT_NULL} otherwise.
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
		if (node.son == null) {
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
	 * @param current
	 * @param newNode
	 * @param recursionStatus
	 * @return true if the newNode has been inserted; false otherwise.
	 */
	private boolean recursivePut(HierarchyNode current, HierarchyNode newNode, RecursionStatus recursionStatus) {
		boolean status = false;

		// 1. Manage possible spin label
		status = manageSpin(current, newNode, recursionStatus);
		if (recursionStatus.foundTwin) {
			return status;
		}

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
			recursiveUpdateSons(current);
			recursionStatus.updateAPreviousValue = true;
			return true;
		}

		// 5. Descent the sub hierarchy checking if new node has to be insert as an update or as new node.
		status = false;
		if (current.son != null) {
			for (HierarchyNode son : current.son) {
				status = recursivePut(son, newNode, recursionStatus) || status;
				if (recursionStatus.foundTwin) {
					return status;
				}
				if (status && recursionStatus.valueGreaterThanCurrent) {
					// If newNode has been already inserted but, then, it has been discovered that it shouldn't be inserted because there is a smaller value,
					// then newNode has to be removed!
					recursiveRemove(current, newNode.label);
					return false;
				}
				if (recursionStatus.valueGreaterThanCurrent) {
					// It is already present
					return false;
				}
			}
		}

		// 6. If it has been insert in the sub-hierarchy, everything has been done!
		if (status)
			return status;

		// Before inserting newNode, it is better to check if among brothers there is one with a single opposite literal and same value.
		// If yes, newNode has to be insert, but the brother removed, and the newNode without the common proposition insert from the
		// boolean st1 = manageTwinBrother(current, newNode, recursionStatus);
		// if (recursionStatus.foundTwin) {
		// return st1;
		// }

		// Element has to be insert as son.
		if (newNode.father == null) {
			newNode.father = new ObjectArraySet<>();
		}
		newNode.father.add(current);
		if (current.son == null) {
			current.son = new ObjectArraySet<>();
			current.son.add(newNode);
			return true;
		}
		// Add the node!
		current.son.add(newNode);

		// It could occur that some brothers have to be become sons of newNode!
		// Since currentNode.son will be possibly shorten, I need a copy to be sure to check all components.
		HierarchyNode[] sonsReferenceCopy = current.son.toArray(new HierarchyNode[1]);
		for (HierarchyNode son : sonsReferenceCopy) {
			if (son == null || son == newNode)
				continue;
			if (son.label.subsumes(newNode.label)) {
				if (son.value >= newNode.value) {
					// newNode has a shorter label and a better value, son can be deleted.
					replace(son, newNode);
				} else {
					current.son.remove(son);
					son.father.remove(current);
					son.father.add(newNode);
					if (newNode.son == null)
						newNode.son = new ObjectArraySet<>();
					newNode.son.add(son);
				}
			} else {
				// // It could that a node can be a twin,
				// boolean st2 = false;
				// st2 = manageTwinBrother(son, newNode, recursionStatus);
				// if (recursionStatus.foundTwin) {
				// return st2;
				// }
				recursiveUpdateSiblings(son.son, newNode, recursionStatus);
			}
		}
		return true;
	}

	/**
	 * Check and manage the case that currentNode has a label that differs from newNode one by only one literal.
	 * 
	 * @param currentNode
	 * @param newNode
	 * @param status
	 * @return true if a labeled value with only one opposite literal w.r.t. literals in newNode label and with same value has been found; false otherwise.
	 */
	private boolean manageSpin(HierarchyNode currentNode, HierarchyNode newNode, RecursionStatus status) {
		if (currentNode == null || newNode == null)
			return false;

		Literal p = newNode.label.getUniqueDifferentLiteral(currentNode.label);
		if (p != null) {
			int max = newNode.value > currentNode.value ? newNode.value : currentNode.value;

			if (max == newNode.value && max == currentNode.value) {
				// Both nodes have to be replaced!
				recursiveRemove(currentNode, currentNode.label);
				removeNewNode(newNode);
			} else {
				if (max == newNode.value) {
					// Only the newNode have to be adjusted!
					removeNewNode(newNode);
				} else {
					// currentNode has to be replaced by the shorten one
					recursiveRemove(currentNode, currentNode.label);
					// newNode has to be inserted!
					// I prefer to insert again from root!
					put(newNode.label, newNode.value);
				}
			}
			Label labelWOp = new Label(newNode.label);
			labelWOp.remove(p);
			if (labelWOp.isEmpty())
				labelWOp = Label.emptyLabel;
			status.foundTwin = true;
			return put(labelWOp, max);
		}
		return false;
	}

	/**
	 * @param newNode
	 */
	private static void removeNewNode(HierarchyNode newNode) {
		if (newNode.father != null && newNode.father.size() > 0) {
			// newNode has been already inserted.
			// It is necessary to remove it.
			for (HierarchyNode fatherNewNode : newNode.father) {
				if (fatherNewNode.son != null) {
					fatherNewNode.son.remove(newNode);
				}
				if (newNode.son != null && newNode.son.size() > 0) {
					for (HierarchyNode son1 : newNode.son) {
						fatherNewNode.son.add(son1);
					}
				}
			}
		}
	}

	/**
	 * @param currentNode
	 * @param l
	 * @return the int value associated to a node with label l if it exists, INT_NULL otherwise.
	 */
	private int recursiveRemove(HierarchyNode currentNode, Label l) {
		if (currentNode.label.equals(l)) {
			int oldValue = currentNode.value;

			if (currentNode.label.equals(Label.emptyLabel)) {
				currentNode.value = Constants.INT_POS_INFINITE;
				return oldValue;
			}
			for (HierarchyNode father : currentNode.father) {
				if (father.son != null) {
					father.son.remove(currentNode);
				}
				if (currentNode.son != null) {
					for (HierarchyNode n : currentNode.son) {
						n.father.remove(currentNode);
						n.father.add(father);
						father.son.add(n);
					}
				}
			}
			currentNode.clear();
			return oldValue;
		}

		int oldV = INT_NULL;
		if (currentNode.son != null) {
			// Since currentNode.son will be possibly shorten, I need a copy to be sure to check all components.
			HierarchyNode[] sonsReferenceCopy = currentNode.son.toArray(new HierarchyNode[1]);
			for (HierarchyNode n : sonsReferenceCopy) {
				if (n == null)
					continue;
				if (l.subsumes(n.label)) {
					oldV = recursiveRemove(n, l);
					if (oldV != INT_NULL)
						return oldV;
				}
			}
		}
		return oldV;
	}

	/**
	 * @param siblings
	 * @param newNode
	 * @param recursionStatus
	 */
	private void recursiveUpdateSiblings(ObjectArraySet<HierarchyNode> siblings, HierarchyNode newNode, RecursionStatus recursionStatus) {
		if (siblings != null) {
			for (HierarchyNode currentNode : siblings) {
				manageSpin(currentNode, newNode, recursionStatus);
				if (recursionStatus.foundTwin) {
					return;
				}
				if (currentNode.label.subsumes(newNode.label)) {
					if (currentNode.value >= newNode.value) {
						// It could that a label with ? can be nullified by a label with a smaller value and without ?
						// LOG.warning("CurrentNode: " + currentNode + "; NewNode: " + newNode);
						replace(currentNode, newNode);
						return;
					}
					currentNode.father.add(newNode);
					if (newNode.son == null)
						newNode.son = new ObjectArraySet<>();
					newNode.son.add(currentNode);
				} else {
					recursiveUpdateSiblings(currentNode.son, newNode, recursionStatus);
				}
			}
		}
	}

	/**
	 * @param currentNode
	 */
	private void recursiveUpdateSons(HierarchyNode currentNode) {
		if (currentNode.son != null) {
			// Since currentNode.son will be possibly shorten, I need a copy to be sure to check all components.
			HierarchyNode[] sonsReferenceCopy = currentNode.son.toArray(new HierarchyNode[1]);
			for (HierarchyNode son : sonsReferenceCopy) {
				if (son == null)
					continue;
				if (son.value >= currentNode.value) {
					recursiveRemove(son, son.label);
				}
			}
		}
	}

	// /**
	// * @param current
	// * @param newNode
	// * @param status
	// * @return true if a labeled value with only one opposite literal w.r.t. literals in newNode label and with same value has been found; false otherwise.
	// */
	// private boolean manageTwinBrother(HierarchyNode current, HierarchyNode newNode, RecursionStatus status) {
	// // Before inserting newNode, it is better to check if among brothers there is one with a single opposite literal and same value.
	// // If yes, newNode has to be insert, but the brother removed, and the newNode without the common proposition insert from the
	// if (current.son == null)
	// return false;
	// Literal p = null;
	// // Since currentNode.son will be possibly shorten, I need a copy to be sure to check all components.
	// HierarchyNode[] sonsReferenceCopy = current.son.toArray(new HierarchyNode[1]);
	// for (HierarchyNode son : sonsReferenceCopy) {
	// if (son == null)
	// continue;
	// p = newNode.label.getUniqueDifferentLiteral(son.label);
	// if (p != null) {
	// int max = newNode.value > son.value ? newNode.value : son.value;
	//
	// if (max == newNode.value && max == son.value) {
	// // Both twin have to be replaced!
	// recursiveRemove(son, son.label);
	// if (newNode.father != null && newNode.father.size() > 0) {
	// // newNode has been already inserted.
	// // It is necessary to remove it.
	// for (HierarchyNode fatherNewNode : newNode.father) {
	// if (fatherNewNode.son != null) {
	// fatherNewNode.son.remove(newNode);
	// }
	// }
	// }
	// } else {
	// if (max == newNode.value) {
	// // Only the newNode have to be adjusted!
	// if (newNode.father != null && newNode.father.size() > 0) {
	// // newNode has been already inserted.
	// // It is necessary to remove it.
	// for (HierarchyNode fatherNewNode : newNode.father) {
	// if (fatherNewNode.son != null) {
	// fatherNewNode.son.remove(newNode);
	// }
	// if (newNode.son != null && newNode.son.size() > 0) {
	// for (HierarchyNode son1 : newNode.son) {
	// fatherNewNode.son.add(son1);
	// }
	// }
	// }
	// }
	// } else {
	// // The already present node has to be replaced by the shorten one
	// recursiveRemove(son, son.label);
	// // newNode has to be inserted!
	// // I prefer to insert again from root!
	// // Element has to be insert as son.
	// // if (newNode.father == null) {
	// // newNode.father = new ObjectArraySet<>();
	// // }
	// // newNode.father.add(current);
	// // current.son.add(newNode);
	// put(newNode.label, newNode.value);
	// }
	// }
	// Label labelWOp = new Label(newNode.label);
	// labelWOp.remove(p);
	// if (labelWOp.isEmpty())
	// labelWOp = Label.emptyLabel;
	// status.foundTwin = true;
	// return put(labelWOp, max);
	// }
	// }
	// return false;
	// }

}
