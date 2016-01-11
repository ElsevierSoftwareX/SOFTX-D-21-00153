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
			value = Constants.INT_NULL;
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
				label = null;// It is fundamental for checking if a HirearchyNode has been deleted.
			}
			value = Constants.INT_NULL;
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
	static private Logger LOG = Logger.getLogger(LabeledIntHierarchyMap.class.getName());

	/**
	 *
	 */
	static private final long serialVersionUID = 3L;

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
	 * Simple constructor. The internal structure is built and empty.
	 */
	LabeledIntHierarchyMap() {
		root = new HierarchyNode(Label.emptyLabel, Constants.INT_POS_INFINITE);
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
		putHistory = "";
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
		recursiveBuildingSet(this.root, coll, (this.root.visit + 1));
		this.root.visit++;
		return coll;
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
		if ((newLabel == null) || (newValue == Constants.INT_NULL))
			return false;

		HierarchyNode newNode = new HierarchyNode(newLabel, newValue);
		RecursionStatus status = new RecursionStatus();
		if (wellFormatCheck) {
			putHistory += newNode + " ";
		}
		boolean st = recursivePut(root, newNode, status);
		if (wellFormatCheck && root != null && root.son != null) {
			for (HierarchyNode son : root.son) {
				for (HierarchyNode son1 : root.son) {
					if (son != null && son1 != null && son != son1 && (son.label.subsumes(son1.label) || son1.label.subsumes(son.label))) {
						LOG.severe("Hierarchy: " + this.toString());
						LOG.severe("Son: " + root.son);
						LOG.severe("Put history: " + putHistory);
						throw new IllegalStateException("Hirearchy is not in a right format.");
					}
				}
			}
		}
		return st;
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
		if (node.son != null && node.son.size() > 0) {
			for (HierarchyNode n : node.son) {
				recursiveBuildingSet(n, coll, visit);
			}
		}
		if (node.label != null && node.value != Constants.INT_NULL && node.label != Label.emptyLabel && node.value != Constants.INT_POS_INFINITE)
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
	 * @return the value associated to label l if it exists, {@link Constants#INT_NULL} otherwise.
	 */
	private HierarchyNode recursiveGet(final HierarchyNode node, final Label l) {
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
	 * @param current
	 * @param newNode
	 * @param recursionStatus
	 * @return true if the newNode has been inserted; false otherwise.
	 */
	private boolean recursivePut(HierarchyNode current, HierarchyNode newNode, RecursionStatus recursionStatus) {
		if (current == null || newNode == null || newNode.label == null) {
			String msg = "Something wrong. newNode: " + newNode + ", current:" + current;
			LOG.severe(msg);
			throw new IllegalArgumentException(msg);
		}

		boolean status = false;

		// 1. Manage possible spin label
		// status = manageSpin(current, newNode, recursionStatus);
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
				status = recursivePut(son, newNode, recursionStatus) || status;
				if (recursionStatus.foundTwin || recursionStatus.updateAPreviousValue) {
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
					recursiveUpdateSiblingSons(son.son.toArray(new HierarchyNode[1]), 0, newNode, recursionStatus);
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
	 * Check and manage the case that currentNode has a label that differs from newNode one by only one literal. This procedure can determine a removing cascade
	 * of element that cannot be controlled in a safe way during a recursion (this procedure is called during a recursion insert).
	 * 
	 * Therefore, for now, it is disabled.
	 * 
	 * @param currentNode
	 * @param newNode
	 * @param status
	 * @return true if a labeled value with only one opposite literal w.r.t. literals in newNode label and with same value has been found; false otherwise.
	 */
	@SuppressWarnings({ "static-method", "unused" })
	private boolean manageSpin(HierarchyNode currentNode, HierarchyNode newNode, RecursionStatus status) {
		// if (currentNode == null || newNode == null)
		// return false;
		//
		// Literal p = newNode.label.getUniqueDifferentLiteral(currentNode.label);
		// if (p != null) {
		// int max = newNode.value > currentNode.value ? newNode.value : currentNode.value;
		//
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
		// Label labelWOp = new Label(newNode.label);
		// labelWOp.remove(p);
		// if (labelWOp.isEmpty())
		// labelWOp = Label.emptyLabel;
		// status.foundTwin = true;
		// return put(labelWOp, max);
		// }
		return false;
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
		// for each father gFather of nodeToRemove,
		// 1. remove nodeToRemove as son
		// 2. check if each nodeToRemove's son X has to become son of gFather
		for (HierarchyNode gFather : nodeToRemove.father) {
			if (gFather.son != null && gFather.son.size() > 0) {// It should be always != null
				gFather.son.remove(nodeToRemove);
			}
			if (nodeToRemove.son != null && nodeToRemove.son.size() > 0) {
				for (HierarchyNode sonOfNodeToRemove : nodeToRemove.son) {
					// Each nodeToRemove's son X becomes son of gFather if and only if no other X's father subsumes gFather.
					sonOfNodeToRemove.father.remove(nodeToRemove);
					if (sonOfNodeToRemove.value >= gFather.value)// sanity check
						continue;
					if (sonOfNodeToRemove.father.size() > 0) {
						boolean add = true;
						for (HierarchyNode fatherOfsonOfNodeToRemove : sonOfNodeToRemove.father) {
							if (fatherOfsonOfNodeToRemove.label.subsumes(gFather.label)) {
								add = false;
								break;
							}
						}
						if (add) {
							addFatherTo(gFather, sonOfNodeToRemove);// sonOfNodeToRemove.father.add(gFather);
							addSonTo(sonOfNodeToRemove, gFather);
						}
					} else {
						addFatherTo(gFather, sonOfNodeToRemove);// sonOfNodeToRemove.father.add(gFather);
						addSonTo(sonOfNodeToRemove, gFather);
					}
				}
			}
		}
		nodeToRemove.clear();
	}

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
		
		if (nodeWhereToAddSon.son==null) {
			nodeWhereToAddSon.son = new ObjectArraySet<>();
		}
		nodeWhereToAddSon.son.add(newSon);
	}
	


	/**
	 * After the insertion of newNode, it can occur that siblings' sons has to be update with respect to newNode.
	 *
	 * @param siblingsSons
	 * @param newNode
	 * @param recursionStatus
	 */
	private void recursiveUpdateSiblingSons(HierarchyNode[] siblingsSons, int index, HierarchyNode newNode, RecursionStatus recursionStatus) {
		if (siblingsSons == null || siblingsSons.length == 0 || index == siblingsSons.length)
			return;
		// go to the last son
		if (index < siblingsSons.length) {
			recursiveUpdateSiblingSons(siblingsSons, index + 1, newNode, recursionStatus);
		}
		HierarchyNode currentSiblingSon = siblingsSons[index];
		if (currentSiblingSon == null)
			return;
		// goto the last grand-son
		if (currentSiblingSon.son != null && currentSiblingSon.son.size() > 0) {
			recursiveUpdateSiblingSons(currentSiblingSon.son.toArray(new HierarchyNode[1]), 0, newNode, recursionStatus);
		}
		// last son unchecked.
		// manageSpin(currentSiblingSon, newNode, recursionStatus);// possible case: ¬ab-->¬abc and new node is ¬a¬bc
		// if (recursionStatus.foundTwin) {
		// return;
		// }
		if (currentSiblingSon.label.subsumes(newNode.label)) {
			if (currentSiblingSon.value >= newNode.value) {
				// It could that a label with ? can be nullified by a label with a smaller value and without ?
				// LOG.warning("CurrentNode: " + currentNode + "; NewNode: " + newNode);
				// replace(currentSiblingSon, newNode); ????
				removeNode(currentSiblingSon);
				return;
			}
			addFatherTo(newNode, currentSiblingSon);// currentSiblingSon.father.add(newNode);
			addSonTo(currentSiblingSon, newNode);
		}
	}

	/**
	 * Recursive removing all sons having a value greater than the given one.
	 * 
	 * @param currentNode
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
}
