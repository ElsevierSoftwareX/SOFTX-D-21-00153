package it.univr.di.labeledvalue;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import it.univr.di.labeledvalue.Literal.State;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

/**
 * Simple class to represent a <em>label</em> in the CSTN/CSTNU framework.<br>
 * A label is the conjunction (and logic) of zero or more <em>literals</em> ({@link Literal}).<br>
 * A label without literals is called <em>empty label</em> and it is represented graphically as {@link Constants#EMPTY_LABEL}.<br>
 * A labels is <em>consistent</em> when it does not contains opposite literals.
 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> (<code>⊨ L' ⇒ L</code>).<br>
 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.
 * <p>
 * Design assumptions
 * <ol>
 * <li>Since it is more frequent to search/inspect the components of a label instead of change it, I decided to implement a label as a sorted set of literals.
 * If for some reason, the sorting property has to be avoid, it is necessary to review the following methods: {@link #getLiteralWithSameName(Literal)}.
 * </ol>
 * <p>
 * Execution time for some operations w.r.t the core data structure of the class:<br>
 * <table border="1">
 * <tr>
 * <th>Method</th>
 * <th>TreeSet</th>
 * <th>ObjectAVLTreeSet</th>
 * <th>ObjectRBTreeSet</th>
 * </tr>
 * <tr>
 * <td>Simple conjunction of '¬abcd' with '¬adefjs'='¬abcdefjs' (ms)</td>
 * <td>0.076961</td>
 * <td>0.066116</td>
 * <td>0.068798</td>
 * </tr>
 * <tr>
 * <td>Execution time for an extended conjunction of '¬abcd' with 'a¬c¬defjs'='¿ab¿c¿defjs' (ms)</td>
 * <td>0.07583</td>
 * <td>0.024099</td>
 * <td>0.014627</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (inconsistent) labels are consistent. Details '¬abcd' with 'a¬c¬defjs' (ms)</td>
 * <td>0.004016</td>
 * <td>0.001666</td>
 * <td>0.00166</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if two (consistent) labels are consistent. Details '¬abcd' with '¬abcd' (ms)</td>
 * <td>0.01946</td>
 * <td>0.004457</td>
 * <td>0.004099</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the literal is the last inserted) (ms)</td>
 * <td>5.48E-4</td>
 * <td>4.96E-4</td>
 * <td>5.01E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for checking if a literal is present in a label (the literal is not present) (ms)</td>
 * <td>6.48E-4</td>
 * <td>7.71E-4</td>
 * <td>5.96E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition letter (the literal is present) (ms)</td>
 * <td>0.003272</td>
 * <td>1.83E-4</td>
 * <td>1.09E-4</td>
 * </tr>
 * <tr>
 * <td>Execution time for get the literal in the label with the same proposition letter (the literal is not present) (ms)</td>
 * <td>0.002569</td>
 * <td>1.68E-4</td>
 * <td>1.0E-4</td>
 * </tr>
 * </table>
 *
 * @author Roberto Posenato
 */
public class Label implements Comparable<Label>, Serializable {
	/**
	 * A base is a set of same-length labels that are can be used to build any other greater-length label of the universe.<br>
	 * The label components of a base can be built from a set of literals making all possible same-length combinations of such literals and their negations.
	 *
	 * @param baseElements It cannot contain literal having unknown state.
	 * @return return all the components of the base built using literals of baseElements. Null if baseElements is null or empty or contains at least a literal
	 *         with unknown state.
	 */
	public static final Label[] allComponentsOfBaseGenerator(final Literal[] baseElements) {
		if ((baseElements == null) || (baseElements.length == 0)) return null;
		for (final Literal l : baseElements) {
			if (l.isUnknown()) return null;
		}
		final int baseSize = baseElements.length;
		final int n = (int) Math.pow(2, baseSize);
		final Label[] components = new Label[n];
		for (int i = 0; i < n; i++) {
			components[i] = Label.complementGenerator(baseElements, i);
		}
		return components;
	}

	/**
	 * Proposes only some execution time estimates about some class methods.
	 *
	 * @param args
	 */
	public static void main(final String[] args) {
		final int nTest = 1000;
		final double msNorm = 1.0 / (1000000.0 * nTest);

		final Literal d = new Literal('d'), z = new Literal('z');

		Label l1 = Label.parse(Constants.NOT + "abcd");
		Label l2 = Label.parse(Constants.NOT + "aejfsd");
		Label result = new Label();
		long startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunction(l2);
		}
		long endTime = System.nanoTime();
		System.out.println("Execution time for a simple conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime)
				* msNorm));

		l1 = Label.parse(Constants.NOT + "abcd");
		l2 = Label.parse("a¬d¬cejfs");
		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			result = l1.conjunctionExtended(l2);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for an extended conjunction of '" + l1 + "' with '" + l2 + "'='" + result + "' (ms): " + ((endTime - startTime)
				* msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l2);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (inconsistent) labels are consistent. Details '" + l1 + "' with '" + l2 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.isConsistentWith(l1);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if two (consistent) labels are consistent. Details '" + l1 + "' with '" + l1 + "' (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(d);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if a literal is present in a label (the literal is the last inserted) (ms): " + ((endTime - startTime)
				* msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.contains(z);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for checking if a literal is present in a label (the literal is not present) (ms): " + ((endTime - startTime)
				* msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.getLiteralWithSameName(d);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is present) (ms): "
				+ ((endTime - startTime) * msNorm));

		startTime = System.nanoTime();
		for (int i = 0; i < nTest; i++) {
			l1.getLiteralWithSameName(z);
		}
		endTime = System.nanoTime();
		System.out.println("Execution time for get the literal in the label with the same proposition letter (the literal is not present) (ms): "
				+ ((endTime - startTime) * msNorm));
	}

	/**
	 * Parse a string representing a label and return an equivalent Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is specified in {@link Constants#labelRE}.
	 *
	 * @param s
	 * @return a Label object corresponding to the label string representation.
	 */
	public static Label parse(final String s) {
		if (s == null) return null;
		int n = s.length();
		final Label label = new Label();
		if (n == 0) return label;
		char c;

		// trim all internal spaces
		final StringBuffer sb = new StringBuffer(n);
		int i = 0;
		while (i < n) {
			c = s.charAt(i++);
			if ((c != ' ') && (c != '\t')) {
				sb.append(c);
			}
		}
		final String s2 = sb.toString();
		// LOG.finest("String trimmed: " + s2);
		if (!s2.matches(Constants.labelRE)) // LOG.finest("Input '" + s2 + "' does not satisfy Label format: " + Constants.labelRE);
			return null;

		n = s2.length();
		if ((n == 1) && (s2.charAt(0) == Constants.EMPTY_LABEL)) return Label.emptyLabel; // Check only one time the special case made with the empty symbol.
		Literal l;
		i = 0;
		Literal oldL;
		while (i < n) {
			c = s2.charAt(i);
			if (c == Constants.NOT || c == Constants.UNKNOWN) {
				char sign = c;
				c = s2.charAt(++i);
				l = new Literal(c, (sign == Constants.NOT) ? State.negated : State.unknown);
			} else {
				l = new Literal(c);
			}
			oldL = label.getLiteralWithSameName(l);
			if (oldL == null || oldL.equals(oldL)) label.conjunctExtended(l);
			else return null;
			i++;
		}
		return label;
	}

	/**
	 * Returns a label containing a copy of 'label' but with literals having indexes corresponding to bits 1 in the parameter 'index' set to negative state.
	 *
	 * @param l a consistent label represented as an array of literals. l must not contain any literal havng unknown state.
	 * @param index
	 * @return a label copy of label but with literals having indexes corresponding to bits 1 in the parameter 'index' set to negative state.
	 *         If label is null or empty or contains UNKNOWN literals, returns null;
	 */
	private static final Label complementGenerator(final Literal[] l, final int index) {
		int n;
		if ((l == null) || ((n = l.length) == 0) || (index < 0) || (index > Math.pow(2, n))) return null;
		int j = 1;
		final Label newLabel = new Label();
		final Set<Literal> lit = new ObjectOpenHashSet<>(n);
		Literal labelStraight;
		for (int i = 0; i < n; i++, j <<= 1) {
			if (l[i].isUnknown()) return null;
			labelStraight = l[i].isNegated() ? l[i].getComplement() : l[i];
			if (lit.contains(labelStraight)) return null;// l contains two times at least the same proposition letter. It is not proper.
			lit.add(labelStraight);
			newLabel.label.add(((j & index) != 0) ? l[i].getComplement() : l[i]);
		}
		return newLabel;
	}

	/**
	 * A constant empty label to represent an empty label that will never change.
	 */
	public static final Label emptyLabel = new Label();

	/**
	 * Validator for graphical interface
	 */
	public static final Validator<String> labelValidator = new Validator<String>() {
		@Override
		public Class<String> modelType() {
			return String.class;
		}

		@Override
		public void validate(final Problems problems, final String compName, final String model) {
			if ((model == null) || (model.length() == 0)) return;
			final Label l = Label.parse(model);
			if (l == null) {
				problems.append("Highlighted label is inconsistent.");
				return;
			}
		}
	};

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Label.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 2L;

	/**
	 * The label info
	 */
	// private final TreeSet<Literal> label = new TreeSet<>();
	private ObjectSortedSet<Literal> label = new ObjectRBTreeSet<>();

	/**
	 * Default constructor.
	 */
	public Label() {}

	/**
	 * Constructs a label cloning the given label l. This object shares the literals of l.
	 *
	 * @param l the label to clone. If null, this will be an empty label.
	 */
	public Label(final Label l) {
		if (l == null) return;
		this.label.addAll(l.label);
	}

	/**
	 * Constructs a label with literal l.
	 *
	 * @param l
	 */
	public Label(final Literal l) {
		if (l == null) return;
		this.label.add(l);
	}

	/**
	 * Makes the label empty.
	 */
	public void clear() {
		this.label.clear();
	}

	/**
	 * Determines a lexicographical order between labels based on the natural order of type {@link Literal}.
	 */
	@Override
	public int compareTo(final Label l) {
		int cmp;
		final int s1 = this.size(), s2 = l.size();
		// case where one of two is empty
		if ((s1 == 0) || (s2 == 0)) return s1 - s2;

		final Iterator<Literal> l2I = l.label.iterator();
		Literal l2;
		for (final Literal l1 : this.label) {
			if (l2I.hasNext()) {
				l2 = l2I.next();
				if ((cmp = l1.compareTo(l2)) != 0) return cmp;
			} else return 1;
		}
		// All the compared literal are equal.
		return (s1 == s2) ? 0 : -1;
	}

	/**
	 * It conjuncts <code>l</code> to this if <code>this</code> does not contain <code>l</code> or ¬<code>l</code>.
	 *
	 * @param l the literal to conjunct. It cannot be an UNKNOWN literal.
	 * @return true if l is added, false otherwise.
	 */
	public boolean conjunct(final Literal l) {
		// if l is unknown state, getComplement returns false!
		if ((l == null) || l.isUnknown() || this.label.contains(l.getComplement())) return false;
		return this.label.add(l);
	}

	/**
	 * It conjuncts <code>l</code> to <code>this</code>.
	 *
	 * If <code>l</code> is opposite to any literal in <code>this</code>, the opposite literal in <code>this</code> is substituted with <code>l</code> but with
	 * unknown state.
	 * If <code>l</code> has unknown state, it is add to <code>this</code>, removing possible literal with the same propositional letter.
	 *
	 * @param l the literal to conjunct.
	 * @return true if l is the size of <code>this</code> is augmented, false otherwise.
	 */
	public boolean conjunctExtended(final Literal l) {
		if (l == null) return false;
		final Literal inLabel = this.getLiteralWithSameName(l);
		if (inLabel == null) return this.label.add(l);
		if (inLabel.equals(l)) return false;

		this.label.remove(inLabel);
		return this.label.add(l.getUnknown());
	}

	/**
	 * @param l the label to check
	 * @return a new label with the conjunction of 'this' and 'label' if they are consistent, null otherwise.<br>
	 * 			null also if this label or l contains unknown literals.
	 *         'this' is not altered by this method.
	 */
	public Label conjunction(final Label l) {
		if (l == null || this.containsUnknown() || l.containsUnknown()) return null;
		final Label newLabel = new Label();
		newLabel.label.addAll(this.label);
		for (Literal l1 : l.label) {
			if (newLabel.label.contains(l1)) continue; // it is necessary otherwise the conjunct return false adding one already present element.
			if (!newLabel.conjunct(l1)) return null;
		}
		return newLabel;
	}

	/**
	 * Create a new label that represents the conjunction of <code>this<code> and <code>label</code> using also {@link Literal.State#unknown} literals.
	 * A {@link Literal.State#unknown} literal represent the fact that in the two input labels a proposition letter is present as straight state in one label
	 * and in negated state in the other.
	 *
	 * @param l the input label.
	 * @return a new label with the conjunction of <code>this<code> and <code>label</code>.<br>
	 *         <code>this<code> is not altered by this method.
	 */
	public Label conjunctionExtended(final Label l) {
		final Label newLabel = new Label();
		newLabel.label.addAll(this.label);

		if ((l == null) || l.isEmpty()) return newLabel;

		for (final Literal lit : l.label) {
			newLabel.conjunctExtended(lit);
		}
		return newLabel;
	}

	/**
	 * @param l the literal to check
	 * @return true if this contains l.
	 */
	public boolean contains(final Literal l) {
		if (l == null) return false;
		return this.label.contains(l);
	}

	/**
	 * @return true if the label contains one unknown literal at least.
	 */
	public boolean containsUnknown() {
		for (final Literal l : this.label) {
			if (l.isUnknown()) return true;
		}
		return false;
	}

	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof Label)) return false;
		final Label l1 = (Label) obj;
		if (this.isEmpty() && l1.isEmpty()) return true; // label is a TreeSet and two empty TreeSet are always considered not equal while here we want them
		// equal!
		return this.label.equals(l1.label);
	}

	/**
	 * @return An iterator over literals composing the label.
	 */
	public Literal[] getAll() {
		return this.label.toArray(new Literal[this.label.size()]);
	}

	/**
	 * @return a copy of all literals of the label as straight literals in a Collection.
	 */
	public Literal[] getAllAsStraight() {
		int n = this.label.size();
		final Literal[] result = new Literal[n], orig = this.toArray();
		Literal l;
		while (n-- != 0) {
			l = orig[n];
			result[n] = ((l.isStraight()) ? l : l.getStraight());
		}
		return result;
	}

	/**
	 * @return a copy of all literals of the label having {@link Literal.State#unknown} state.
	 */
	public Literal[] getAllUnknown() {
		final ObjectArraySet<Literal> result = new ObjectArraySet<>();
		for (final Literal l : this.label) {
			if (l.isUnknown()) result.add(l);
		}
		return result.toArray(new Literal[result.size()]);
	}

	/**
	 * @param l the literal to check
	 * @return l or ¬l or ¿l if it is present, null otherwise. It returns a copy of the literal.
	 */
	public Literal getLiteralWithSameName(final Literal l) {
		if (l == null) return null;
		final char lName = l.getName();
		// This method exploits the order of literals in the label!
		for (final Literal l1 : this.label.subSet(l.getNegated(), new Literal((char) (l.getName() + 1), State.negated))) {
			if (l1.getName() == lName) return l1;
		}
		return null;
	}

	/**
	 * Determines the sub label of <code>this</code> that is also present (not present) in label <code>lab</code>.
	 * <p>
	 * When the common sub label is required (<code>inCommon</code> true), if <code>strict</code> is true, then the common sub label is as expected: it contains
	 * only the literals of <code>this</code> also present into <code>lab</code>. <br>
	 * Otherwise, when <code>strict</code> is false, the common part contains also every literal that has the opposite or the unknown counterpart in
	 * <code>lab</code>. <br>
	 * For example, is this='a¬b¿cd' and lab='bc¿d', the common part is '¿b¿c¿d'
	 * 
	 * @param lab the label in which to find the common/uncommon sub-part.
	 * @param inCommon true if the common sub-label is wanted, false if the sub-label present in <code>this</code> and not in <code>lab</code> is wanted.
	 * @param strict if the common part should contain only the same literals in both labels.
	 * @return the sub label of <code>this</code> that is in common/not in common (if inCommon is true/false) with <code>lab</code>.
	 *         The label returned is a new object that shares only literals with this or with from.
	 *         If there is no common part, an empty label is returned.
	 */
	public Label getSubLabelIn(final Label lab, final boolean inCommon, boolean strict) {
		final Label sub = new Label();
		if (this.isEmpty()) return sub;
		if ((lab == null) || lab.isEmpty()) return (inCommon) ? sub : new Label(this);
		for (final Literal l : this.label) {
			Literal found = lab.getLiteralWithSameName(l);
			if (inCommon) {
				if (found == null) continue;
				if (l.equals(found)) {
					sub.label.add(l);
					continue;
				}
				if (!strict && (l.isUnknown() || found.isUnknown() || (l.equals(found.getComplement())))) {
					sub.label.add(l.getUnknown());
				}
			} else {
				if (found == null) sub.label.add(l);
			}
		}
		return sub;
	}

	/**
	 * Finds and returns the unique different literal between <code>this</code> and <code>lab</code> if it exists.
	 * If label <code>this</code> and <code>lab</code> differs in more than one literals, it returns null.
	 * <p>
	 * If <code>this</code> and <code>lab</code> contain a common propositional label but in one the literal is unknown and in other is straight or negated, it
	 * returns null;
	 *
	 * @param lab a nor null neither empty label.
	 * @return the unique literal of 'this' that has its opposite in <code>lab</code>.<br>
	 *         null, if there is no literal of such kind or there are two or more literals of this kind or this/label is empty or null.
	 */
	public Literal getUniqueDifferentLiteral(final Label lab) {
		if ((lab == null) || (lab.size() == 0) || this.isEmpty()) return null;
		Literal theDistinguished = null;
		for (final Literal l : this.label) {
			final Literal lInLabel = lab.getLiteralWithSameName(l);
			if (lInLabel == null) return null;
			if (lInLabel.getState() == l.getState()) {
				continue;
			}
			if (l.isUnknown() || lInLabel.isUnknown()) return null;
			if (theDistinguished == null) {
				if (lab.contains(l.getComplement())) {
					theDistinguished = l;
				} else return null;
			} else return null;
		}
		return theDistinguished;
	}

	@Override
	public int hashCode() {
		return this.label.hashCode();
	}

	/**
	 * A label <code>L</code> is consistent with a label <code>L1</code> if <code>L1</code> does not contain any negative literal of <code>L</code>.<br>
	 * L.subsumes(L1) implies L.isConsistentWith(L1) but not vice-versa.
	 *
	 * @param l the label to check
	 * @return true if the label is consistent with this label.
	 */
	public boolean isConsistentWith(final Label l) {
		if ((l == null) || l.isEmpty() || this.isEmpty()) return true;
		Label shorterLabel, longestLabel;
		if (this.size() > l.size()) {// It is better to cycle on the shorter label.
			shorterLabel = l;
			longestLabel = this;
		} else {
			shorterLabel = this;
			longestLabel = l;
		}
		for (final Literal lit : shorterLabel.label)
			if (!longestLabel.isConsistentWith(lit)) return false;
		return true;
	}

	/**
	 * A literal l is consistent with a label if the last one does not contain ¬l.
	 *
	 * @param l the literal to check
	 * @return true if the literal is consistent with this label.
	 */
	public boolean isConsistentWith(final Literal l) {
		if ((l == null) || this.isEmpty()) return true;

		final Literal lInLabel = this.getLiteralWithSameName(l);
		if (lInLabel == null) return true;
		if ((lInLabel.isStraight() && l.isNegated()) || (lInLabel.isNegated() && l.isStraight())) return false;
		return true;
	}

	/**
	 * @return true if the label contains no literal.
	 */
	public boolean isEmpty() {
		return this.label.isEmpty();
	}

	/**
	 * Since a label is a conjunction of literals, its negation is a disjunction of negative literals (i.e., not a label).
	 * <p>
	 * The negation operator returns a set of all negative literals of this label. Bear in mind that the complement of a literal with unknown state is a null
	 * object.
	 *
	 * @return the set of all negative literal of this. If this is empty, returns null;
	 */
	public Literal[] negation() {
		if (this.isEmpty()) return null;

		final Literal[] literals = new Literal[this.size()];
		int i = 0;
		for (final Literal l : this.label) {
			literals[i++] = l.getComplement();
		}
		return literals;
	}

	/**
	 * It removes l if it is present, otherwise it does nothing.
	 *
	 * @param l the literal to check
	 * @return true if the literal is removed
	 */
	public boolean remove(final Literal l) {
		return this.label.remove(l);
	}

	/**
	 * It removes l if it is present, otherwise it does nothing.
	 *
	 * @param l the literal to check
	 * @return true if the literal is removed
	 */
	public boolean removeAllLiteralsWithSameName(Literal l) {
		if (l == null) return false;
		if (l.isUnknown()) l = new Literal(l, State.straight);
		return this.label.remove(l) || this.label.remove(l.getComplement()) || this.label.remove(l.getUnknown());
	}

	/**
	 * It removes all literal in inputSet from the current label.
	 *
	 * @param inputSet the literal to check
	 * @return true if any literal is removed
	 */
	public boolean removeAllLiteralsWithSameName(Set<Literal> inputSet) {
		if (inputSet == null) return false;
		boolean rem = false;
		for (Literal l : inputSet) {
			rem = this.removeAllLiteralsWithSameName(l) || rem;
		}
		return rem;
	}

	/**
	 * @return Return the number of literals of the label
	 */
	public int size() {
		return this.label.size();
	}

	/**
	 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> ( <code>⊨ L' ⇒ L</code>).<br>
	 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.
	 *
	 * @param inputLabel the label to check
	 * @return true if this subsumes 'inputLabel'.
	 */
	public boolean subsumes(final Label inputLabel) {
		if ((inputLabel == null) || inputLabel.isEmpty()) return true;
		for (Literal l1 : inputLabel.label) {
			Literal l2 = this.getLiteralWithSameName(l1);
			if (l2 == null) return false;
			if (l2.equals(l1)) continue;
			// before to say that it is false, we have to check if there is a ¿l1 in this.label
			// ¿p subsumes p, ¿p subsumes ¬p, ¿p subsumes ¿p
			// p NOT subsumes ¿p, ¬p NOT subsumes ¿p.
			if (!l2.isUnknown()) return false;
		}
		return true;
	}

	/**
	 * @return the representation of this label as an array of literals.
	 */
	public Literal[] toArray() {
		return this.label.toArray(new Literal[this.size()]);
	}

	/**
	 * Return a string representing the the label as logical expression using logical 'not', 'and', and 'or'.
	 * String representations of operators can be given as parameters.
	 *
	 * A label 'P¬A' is represented as "P and not A"
	 * If negate is true, then 'P¬A' is represented as negated: "not P or A".
	 *
	 * @param negate negate the label before the conversion. Be careful!
	 * @param not string representing not. If null, it is assumed "!"
	 * @param and representing not. If null, it is assumed " & "
	 * @param or representing not. If null, it is assumed " | "
	 * @return empty string if label is null or empty, the string representation as logical expression otherwise.
	 */
	public String toLogicalExpr(final boolean negate, String not, String and, String or) {
		if (this.isEmpty()) return "";
		if (not == null) not = "!";
		if (and == null) and = " & ";
		if (or == null) or = " | ";
		final Literal[] lit = (negate) ? this.negation() : this.toArray();
		final StringBuffer s = new StringBuffer();

		for (final Literal l : lit) {
			s.append(((l.isNegated()) ? not : "") + l.getName() + ((negate) ? or : and));
		}

		return s.substring(0, s.length() - ((negate) ? or.length() : and.length()));
	}

	@Override
	public String toString() {
		if (this.isEmpty()) return String.valueOf(Constants.EMPTY_LABEL);
		final StringBuffer s = new StringBuffer();
		for (final Literal l : this.label)
			s.append(l);
		return s.toString();
	}
}