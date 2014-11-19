/**
 * Simple class to represent a label of a node or an edge. A label is not a name, it is the conjunction of one or more
 * literals specifying
 * the environment where the node/edge has to be considered.
 */
package it.univr.di.cstnu;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

/**
 * Simple class to represent and to manage a Label (conjunction of literals).
 * 
 * @author posenato
 */
public class Label implements Comparable<Label>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * logger
	 */
	private static Logger LOG = Logger.getLogger(Label.class.getName());

	/**
	 * The empty literal
	 */
	public static final Literal emptyLiteral = new Literal(Constants.EMPTY_LABEL);

	/**
	 * The empty label
	 */
	public static final Label emptyLabel = new Label(Label.emptyLiteral);

	/**
	 * Validator for graphical interface
	 */
	public static Validator<String> labelValidator = new Validator<String>() {
		@Override
		public void validate(Problems problems, String compName,
				String model) {
			if ((model == null) || (model.length() == 0))
				return;
			final Label l = Label.parse(model);
			if (l == null) {
				problems.append("Highlighted label is inconsistent.");
				return;
			}
		}

		@Override
		public Class<String> modelType() {
			return String.class;
		}
	};

	/**
	 * Returns a label containing a copy of 'label' but with negate literals having indexes corresponding to bits 1 in
	 * the parameter
	 * 'index'.
	 * 
	 * @param l a proper label: composed by different and not opposite literals
	 * @param index
	 * @return a label copy of label but with negate literals having indexes corresponding to bits 1 in the parameter
	 *         'index'. If label is
	 *         null or empty or non proper, returns null;
	 */
	public static Label complementGenerator(Literal[] l, int index) {
		int n;
		if ((l == null) || ((n = l.length) == 0) || (index < 0) || (index > Math.pow(2, n))) return null;
		int j = 1;
		final Label newLabel = new Label();
		final Set<Literal> lit = new HashSet<>();
		Literal labelStraight;
		for (int i = 0; i < n; i++, j <<= 1) {
			labelStraight = l[i].isNegative() ? l[i].negation() : l[i];
			if (lit.contains(labelStraight))
				return null;
			lit.add(labelStraight);
			newLabel.label.add(((j & index) != 0) ? l[i].negation() : l[i]);
		}
		return newLabel;
	}

	/**
	 * @see #complementGenerator(Literal[], int)
	 * @param l
	 * @param index
	 * @return a label copy of label but with negate literals having indexes corresponding to bits 1 in the parameter
	 *         'index'. If label is
	 *         null or empty or non proper, returns null;
	 */
	public static Label complementGenerator(Collection<Literal> l, int index) {
		return complementGenerator(l.toArray(new Literal[0]), index);
	}

	/**
	 * A base is a set of same-length labels that are can be used to build any other greater-length label of the
	 * universe.<br>
	 * The label components of a base can be built from a set of literals making all possible same-length combinations
	 * of such literals and
	 * their negations.
	 * 
	 * @param baseElements
	 * @return return all the components of the base built using literals of baseElements.
	 */
	public static Label[] allComponentsOfBaseGenerator(Collection<Literal> baseElements) {
		if (baseElements == null || baseElements.isEmpty()) return null;
		int n = (int) Math.pow(2, baseElements.size());
		Label[] components = new Label[n];
		Literal[] lit = baseElements.toArray(new Literal[0]);
		for (int i = 0; i < n; i++) {
			components[i] = Label.complementGenerator(lit, i);
			if (components[i] == null) return null;
		}
		return components;
	}

	@SuppressWarnings({ "unused", "javadoc" })
	public static void main(String[] args) {
		final Literal a = new Literal('A'), b = new Literal('B'), c = new Literal('c', true), d = new Literal('d');

		final Label l1 = Label.parse(Constants.NOT + "A");
		l1.conjunct(b.negation());
		System.out.printf("Label l1 = %s\n", l1);

		final Label l2 = new Label(a);
		l2.conjunct(Label.parse(Constants.NOT + "B"));
		System.out.printf("Label l2 = %s\n", l2);

		final Label l3 = new Label(a);
		// l3.conjunct(new Literal('b', false));
		// System.out.printf("Label l3 = %s\n", l3);

		// System.out.printf(l1+".compareTo("+l2+") = %d, %s\n", l1.compareTo(l2), l1.equals(l2));
		// System.out.printf(l1+".compareTo("+l3+") = %d, %s\n", l1.compareTo(l3), l1.equals(l3));
		// System.out.printf(l3+".compareTo("+l1+") = %d, %s\n", l3.compareTo(l1), l1.equals(l3));
		// System.out.printf(l3 + ".compareTo(" + l2 + ") = %d, %s\n", l3.compareTo(l2), l1.equals(l2));
		// System.out.printf("Label l1 conjunction l2 = %s\n", l1.conjunction(l2));
		// System.out.printf("Label l1 conjunction l3 = %s\n", l1.conjunction(l3));
		// System.out.printf("Label l2 conjunction l3 = %s\n", l2.conjunction(l3));
		// System.out.printf("Label l1 = %s\n", l1);
		// l1.conjunct(l3);
		// System.out.printf("Label l1.conjunct(l3) = %s\n", l1);
		//
		// System.out.printf("l1.compareTo(l2) = %d\n", l1.compareTo(l2));
		// l3.conjunct(d);
		// l3.conjunct(b);
		// System.out.printf("Label l3 = %s\n", l3);
		// System.out.printf("l1.compareTo(l3) = %d, %s\n", l1.compareTo(l3), l1.equals(l3));
		//
		// l3 = new Label();
		// System.out.printf("Label l3 = %s\n", l3);
		// System.out.printf("l1.compareTo(l3) = %d, %s\n", l1.compareTo(l3), l1.equals(l3));
		// l1 = new Label();
		// System.out.printf("Label l1 = %s\n", l1);
		// System.out.printf("l1.compareTo(l3) = %d, %s\n", l1.compareTo(l3), l1.equals(l3));
		//
		// System.out.printf("Label corresponding to string '¬a': %s\n", Label.parse("¬a"));
		// System.out.printf("Label corresponding to string '¬acbbb': %s\n", Label.parse("¬acbbb"));
		// System.out.printf("Label corresponding to string '¬ab b': %s\n", Label.parse("¬ab b"));
		// System.out.printf("Label corresponding to string '¬ab ': %s\n", Label.parse("¬ab "));
		// System.out.printf("Label corresponding to string '¬a¬b¬c': %s\n", Label.parse("¬a¬b¬c"));
		// System.out.printf("Label corresponding to string '¬aab': %s\n", Label.parse("¬aab"));
		// System.out.printf("Label corresponding to string '¬a¬b¬b': %s\n", Label.parse("¬a¬b¬b"));
		// System.out.printf("Label corresponding to string '': %s\n", Label.parse(""));
		// System.out.printf("Label corresponding to string '⊡': %s\n", Label.parse("" + Constants.EMPTY_LABEL));
		//
		final Label l4 = new Label(l2);
		System.out.printf("Label l4 (l2 cloned): %s\n", l4);
		System.out.printf("Label l2 without '¬B' : %s %s\n", l2.remove(new Literal('B', true)), l2);
		System.out.printf("Label l4 : %s\n", l4);
		System.out.printf(l2 + ".subsumes(" + l4 + "): %s\n", l2.subsumes(l4));
		System.out.printf(l4 + ".subsumes(" + l2 + "): %s\n", l4.subsumes(l2));
		System.out.printf("⊡.subsumes(" + l2 + "): %s\n", Label.parse("" + Constants.EMPTY_LABEL).subsumes(l4));
		System.out.printf(l2 + ".subsumes(⊡): %s\n", l2.subsumes(Label.parse("⊡")));

		// l2 = new Label(Label.emptyLabel);
		// l4 = new Label(Label.parse(Constants.EMPTY_LABEL + " "));
		// System.out.printf("Label l2 empty: %s, isEmpty: %s, size: %d\n", l2, l2.isEmpty(), l2.size());
		// System.out.printf("Label l4 empty: %s\n", l4);
		// System.out.printf("l2.equals(l4) : %s\n", l2.equals(l4));

		final Literal[] l1a = l1.toArray();
		for (int i = 0; i < (1 << l1a.length); i++)
			System.out.printf("%d complement of %s: %s\n", i, l1, Label.complementGenerator(l1a, i));
		System.out.printf("%s.getUniqueDifferentLiteral(%s): %s\n", l2, l1, l2.getUniqueDifferentLiteral(l1));
		System.out.printf("%s.getUniqueDifferentLiteral(%s): %s\n", l4, l1, l4.getUniqueDifferentLiteral(l1));
		System.out.printf("%s.getUniqueDifferentLiteral(%s): %s\n", l4, Label.complementGenerator(l1a, 2),
				l4.getUniqueDifferentLiteral(Label.complementGenerator(l1a, 2)));
		System.out.printf("%s.getUniqueDifferentLiteral(%s): %s\n", Label.parse("a¬b¬c"), Label.parse("ab¬c"), Label
				.parse("a¬b¬c").getUniqueDifferentLiteral(Label.parse("ab¬c")));
		System.out.printf("%s.getUniqueDifferentLiteral(%s): %s\n", Label.parse("¬c"), Label.parse("c"),
				Label.parse("¬c").getUniqueDifferentLiteral(Label.parse("c")));
	}

	/**
	 * Parse a string representing a label and return an equivalent Label object if no errors are found, null otherwise.<br>
	 * The regular expression syntax for a label is: {@link Constants#labelRE}
	 * 
	 * @param s
	 * @return a Label object corresponding to the label string representation.
	 */
	public static Label parse(String s) {
		String s2 = s;
		int i = 0, n = s2.length();
		char c;
		final Label label = new Label();
		if (n == 0) return label;

		// trim all internal spaces
		final StringBuffer sb = new StringBuffer(s2.length());
		while (i < n) {
			c = s2.charAt(i++);
			if (c != ' ') sb.append(c);
		}
		s2 = sb.toString();
		LOG.finest("String trimmed: " + s2);
		if (!s2.matches(Constants.labelRE)) {
			LOG.finest("Input '"+s2+"' does not satisfy Label format: " + Constants.labelRE);
			return null;
		}
		
		i = 0;
		n = s2.length();
		Literal l;
		while (i < n) {
			c = s2.charAt(i);
			if (c == Constants.NOT) {
				c = s2.charAt(++i);
				l = new Literal(c, true);
			} else {
				if (c == Constants.EMPTY_LABEL) break;
				l = new Literal(c);
			}
			if (label.isConsistentWith(l))
				label.conjunct(l);
			else
				return null;
			i++;
		}
		return label;
	}

	/**
	 * The label info
	 */
	private TreeSet<Literal> label = new TreeSet<>();

	/**
	 * Default constructor.
	 */
	public Label() {}

	/**
	 * Constructs a label cloning the given label l. This object shares the literals of l.
	 * 
	 * @param l the label to clone. If null, this will be an empty label.
	 */
	public Label(Label l) {
		if (l != null) label.addAll(l.getAll());
	}

	/**
	 * Constructs a label with literal l.
	 * 
	 * FIXME Be careful!
	 * Literal with uppercase letter assumes a different meaning!
	 * 
	 * @param l
	 */
	public Label(Literal l) {
		if ((l == null) || l.equals(Label.emptyLiteral)) return;
		label.add(l);
	}

	/**
	 * FIXME Be careful!
	 * Literal with uppercase letter assumes a different meaning!
	 * 
	 * @param emptyAlternative
	 */
	public Label(Set<Literal> emptyAlternative) {
		if (!(emptyAlternative == null)) for (final Literal l : emptyAlternative)
			label.add(l);
	}

	/**
	 * Determines a lexicographical order between labels based on the natural order of type {@link Literal}.
	 */
	@Override
	public int compareTo(Label l) {
		Literal l1, l2;
		int cmp;
		final int s1 = size(), s2 = l.size();
		// case where one of two is empty
		if ((s1 == 0) || (s2 == 0)) return s1 - s2;

		final Iterator<Literal> l1I = label.iterator();
		final Iterator<Literal> l2I = l.label.iterator();
		while (l1I.hasNext()) {
			l1 = l1I.next();
			if (l2I.hasNext()) {
				l2 = l2I.next();
				if ((cmp = l1.compareTo(l2)) != 0) return cmp;
			} else
				return 1;
		}
		// All the compared literal are equal.
		return (s1 == s2) ? 0 : -1;
	}

	/**
	 * Conjuncts l to this if l does not contain any opposite literal of those contained in 'this'.
	 * 
	 * @param l label to add to this label
	 * @return true if l is added, false otherwise.
	 */
	public boolean conjunct(Label l) {
		final Label newLabel = this.conjunction(l);
		if ((newLabel != null) && (newLabel.label.size() != label.size())) {
			label = newLabel.label;
			return true;
		}
		return false;
	}

	/**
	 * It conjuncts l to this if 'this' does not contain l or ¬l.
	 * 
	 * @param l the literal to conjunct.
	 * @return true if l is added, false otherwise.
	 */
	public boolean conjunct(Literal l) {
		if ((l == null) || l.equals(Label.emptyLiteral) || label.contains(l.negation())) return false;
		return label.add(l);// ListOrderedSet.add does not add a literal already present.
	}

	/**
	 * @param l the label to check
	 * @return a new label with the conjunction of 'this' and 'label' if they are consistent, null otherwise.<br>
	 *         'this' is not altered by this method.
	 */
	public Label conjunction(Label l) {
		final Label newLabel = new Label();
		newLabel.label.addAll(label);

		if ((l == null) || l.isEmpty()) return newLabel;

		if (this.isConsistentWith(l)) {
			newLabel.label.addAll(l.label);
			return newLabel;
		}
		return null;
	}

	/**
	 * @param literal the label to check
	 * @return a new label with the conjunction of 'this' and 'literal' if they are consistent, null otherwise.<br>
	 *         'this' is not altered by this method.
	 */
	public Label conjunction(Literal literal) {
		final Label newLabel = new Label();
		newLabel.label.addAll(label);

		if (literal == null) return newLabel;

		if (this.isConsistentWith(literal)) {
			newLabel.label.add(literal);
			return newLabel;
		}
		return null;
	}

	/**
	 * @param l the literal to check
	 * @return true if this contains l.
	 */
	public boolean contains(Literal l) {
		if (l == null) return false;
		if (l.equals(Label.emptyLiteral)) return true;
		return label.contains(l);
	}

	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Label)) return false;
		final Label l1 = (Label) obj;
		if (isEmpty() && l1.isEmpty()) return true; // label is a TreeSet and two empty TreeSet
													// are always considered not equal while
													// here we want them equal!
		return label.equals(l1.label);
	}

	/**
	 * @return all literals of the label as a Collection.
	 */
	public Collection<Literal> getAll() {
		return label;
	}

	/**
	 * @return all literals of the label as straight literals in a Collection.
	 */
	public Collection<Literal> getAllStraight() {
		final Set<Literal> result = new HashSet<>();
		for (final Literal l : getAll())
			result.add((l.isNegative()) ? l.negation() : l);
		return result;
	}

	/**
	 * @param label1 a not null or empty label.
	 * @return the unique literal of 'this' that has its opposite in label <code>label1</code>.<br>
	 *         null, if there is no literal of such kind or there are two or more literals of this kind or this/label1 is empty or null.
	 */
	public Literal getUniqueDifferentLiteral(Label label1) {
		if ((label1 == null) || (label1.size() == 0) || isEmpty()) return null;
		Literal theDistinguished = null;
		for (final Literal l : toArray())
			if (!label1.contains(l)) {
				if (theDistinguished == null) {
					if (label1.contains(l.negation()))
						theDistinguished = l;
					else
						return null;
				} else
					return null;
			}
		return theDistinguished;
	}

	@Override
	public int hashCode() {
		return label.hashCode();
	}

	
	/**
	 * A label <code>L</code> is consistent with a label <code>L1</code> if the <code>L1</code> does not contain any
	 * negative literal of <code>L</code>.<br>
	 * L.subsumes(L1) implies L.isConsistentWith(L1) but not vice-versa.
	 * 
	 * @param l the label to check
	 * @return true if the label is consistent with this label.
	 */
	public boolean isConsistentWith(Label l) {
		if ((l == null) || l.isEmpty() || this.isEmpty()) return true;
		// FIXME test di label speciali
		if ( Character.isUpperCase(l.toString().codePointAt(0)) && Character.isUpperCase(this.toString().codePointAt(0)) ) return false;
		Label shorterLabel, longestLabel;
		if (size() > l.size()) {// It is better to cycle on the shorter label.
			shorterLabel = l;
			longestLabel = this;
		} else {
			shorterLabel = this;
			longestLabel = l;
		}
		for (final Literal lit : shorterLabel.label)
			if (longestLabel.label.contains(lit.negation())) return false;
		return true;
	}

	/**
	 * A literal l is consistent with a label if the last one does not contain ¬l.
	 * 
	 * @param l the literal to check
	 * @return true if the literal is consistent with this label.
	 */
	public boolean isConsistentWith(Literal l) {
		if ((l == null) || this.isEmpty()) return true;
		// FIXME test di label speciali
		if ( Character.isUpperCase(l.toString().codePointAt(0)) && Character.isUpperCase(this.toString().codePointAt(0)) ) return false;

		return (!label.contains(l.negation()));
	}

	/**
	 * @return true if the label contains no literal.
	 */
	public boolean isEmpty() {
		return label.isEmpty();
	}

	/**
	 * @param l
	 * @return the number of opposite literals between this and label (they are the same but for such opposite
	 *         literals). If they contain
	 *         different literals, it returns null.
	 */
	public Integer isOneComplementOf(Label l) {
		int n;
		if ((l == null) || ((n = l.size()) == 0) || (n != size())) return null;
		int j = 0;
		for (final Literal l1 : getAll()) {
			if (l.contains(l1)) continue;
			if (l.contains(l1.negation())) {
				j++;
				continue;
			}
			return null;
		}
		return j;
	}

	/**
	 * Since a label is a conjunction of literals, its negation is a disjunction of negative literals (i.e., not a
	 * label). The negation
	 * operator returns a set of all negative literals of this label.
	 * 
	 * @return the set of all negative literal of this. If this is empty, returns null;
	 */
	public Literal[] negation() {
		if (isEmpty()) return null;

		final ArrayList<Literal> literals = new ArrayList<>();
		for (final Literal l : label)
			literals.add(l.negation());
		return literals.toArray(new Literal[0]);
	}

	/**
	 * Makes the label empty.
	 */
	public void clear() {
		this.label.clear();
	}

	/**
	 * It removes l if it is present, otherwise it does nothing.
	 * 
	 * @param l the literal to check
	 * @param all if true it removes also ¬l
	 * @return true if the literal is removed
	 */
	public boolean remove(Literal l, boolean all) {
		boolean rem = label.remove(l);
		if (all) {
			rem = label.remove(l.negation()) || rem;
		}
		return rem;
	}

	/**
	 * It removes l if it is present, otherwise it does nothing.
	 * 
	 * @param l the literal to check
	 * @return true if the literal is removed
	 */
	public boolean remove(Literal l) {
		return remove(l, false);
	}

	/**
	 * @return Return the number of literals of the label
	 */
	public int size() {
		return label.size();
	}

	/**
	 * @param from the label from which determine the common/uncommon sub-part.
	 * @param inCommon true if the common sub-label is wanted, false if the sub-label present in this and not in from is
	 *            wanted.
	 * @return the sub label of this that is in common/not in common (if inCommon is true/false) with from. The label
	 *         returned is a new
	 *         object that shares only literals with this or with from. If there is no common part, an empty label is
	 *         returned.
	 */
	public Label subLabelFrom(Label from, boolean inCommon) {
		final Label sub = new Label();
		if (isEmpty()) return sub;
		if ((from == null) || from.isEmpty()) return (inCommon) ? sub : new Label(this);
		for (final Literal l : label)
			if (from.label.contains(l)) {
				if (inCommon) sub.conjunct(l);
			} else if (!inCommon) sub.conjunct(l);
		return sub;
	}

	/**
	 * A label <code>L'</code> subsumes a label <code>L</code> iff <code>L'</code> implies <code>L</code> ( <code>⊨ L' ⇒ L</code>).<br>
	 * In other words, if <code>L</code> is a sub-label of <code>L'</code>.
	 * 
	 * @param l the label to check
	 * @return true if this subsumes l.
	 */
	public boolean subsumes(Label l) {
		if ((l == null) || l.isEmpty()) return true;
		return label.containsAll(l.label);
	}

	/**
	 * @return the representation of this label as an array of literals.
	 */
	public Literal[] toArray() {
		return label.toArray(new Literal[size()]);
	}

	@Override
	public String toString() {
		if (isEmpty()) return String.valueOf(Constants.EMPTY_LABEL);
		final String l = label.toString();
		return l.replaceAll("[ ,\\[\\]]", "");
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
	public String toLogicalExpr(boolean negate, String not, String and, String or) {
		if (this.isEmpty()) return "";
		if (not == null) not = "!";
		if (and == null) and = " & ";
		if (or == null) or = " | ";
		Literal[] lit = (negate) ? negation() : toArray();
		StringBuffer s = new StringBuffer();

		for (Literal l : lit) {
			s.append(((l.isNegative()) ? not : "") + l.getName() + ((negate) ? or : and));
		}

		return s.substring(0, s.length() - ((negate) ? or.length() : and.length()));
	}

}
