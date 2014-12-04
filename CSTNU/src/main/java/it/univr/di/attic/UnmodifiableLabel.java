//package it.univr.di.attic;
//
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.Literal;
//
//
///**
// * Simple class to represent an unmodifiable <em>label</em> in the CSTN/CSTNU framework.<br>
// * It extends class {@link Label}.<br>
// *
// * @author Roberto Posenato
// */
//public class UnmodifiableLabel extends Label {
//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//
//	public static UnmodifiableLabel parse(final String s) {
//		return new UnmodifiableLabel( Label.parse(s) );
//	}
//	
//	/**
//	 * Constructs a label cloning the given label l. This object shares the literals of l.
//	 *
//	 * @param l the label to clone. If null, this will be an empty label.
//	 */
//	public UnmodifiableLabel(final Label l) {
//		super(l);	}
//
//	/**
//	 * Constructs a label with literal l.
//	 *
//	 * @param l
//	 */
//	public UnmodifiableLabel(final Literal l) {
//		super(l);
//	}
//
//	public final void clear() { throw new UnsupportedOperationException(); }
//
//	public boolean conjunct(final Literal l) { throw new UnsupportedOperationException(); }
//
//	public boolean conjunctExtended(final Literal l) { throw new UnsupportedOperationException(); }
//
//	/**
//	 * Determines the sub label of <code>this</code> that is also present (not present) in label <code>lab</code>.
//	 *
//	 * @param lab the label in which to find the common/uncommon sub-part.
//	 * @param inCommon true if the common sub-label is wanted, false if the sub-label present in <code>this</code> and not in <code>lab</code> is wanted.
//	 * @return the sub label of <code>this</code> that is in common/not in common (if inCommon is true/false) with <code>lab</code>.
//	 *         The label returned is a new object that shares only literals with this or with from.
//	 *         If there is no common part, an empty label is returned.
//	 */
//	public UnmodifiableLabel getSubLabelIn(final Label lab, final boolean inCommon) {
//		return new UnmodifiableLabel(super.getSubLabelIn(lab, inCommon));
//	}
//
//	public boolean remove(final Literal l) { throw new UnsupportedOperationException(); }
//
//	public boolean remove(final Literal l, final boolean all) {throw new UnsupportedOperationException(); }
//}