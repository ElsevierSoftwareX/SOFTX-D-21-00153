// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * 
 */
package it.univr.di.cstnu.graph;

import java.util.logging.Logger;

import com.google.common.base.Supplier;

/**
 * LabeledIntEdgePluggabble supplier.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class LabeledNodeSupplier implements Supplier<LabeledNode> {

	/**
	 * class logger
	 */
	@SuppressWarnings("unused")
	static private Logger LOG = Logger.getLogger(LabeledNodeSupplier.class.getName());

	/**
	 * 
	 */
	// private Class<C> labeledIntValueMapImpl;

	/**
	 * <p>Constructor for LabeledNodeSupplier.</p>
	 */
	public LabeledNodeSupplier() {// Class<C> labeledIntMapImplementation
		super();
		// this.labeledIntValueMapImpl = labeledIntMapImplementation;
	}

	/** {@inheritDoc} */
	@Override
	public LabeledNode get() {
		return get("");
	}

	/**
	 * <p>get.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a new LabeledIntMap concrete object.
	 */
	@SuppressWarnings("static-method")
	public LabeledNode get(String name) {
		return new LabeledNode(name);// , this.labeledIntValueMapImpl
	}

	/**
	 * <p>get.</p>
	 *
	 * @param node the edge to clone.
	 * @return a new edge
	 */
	@SuppressWarnings("static-method")
	public LabeledNode get(LabeledNode node) {
		return new LabeledNode(node);// , this.labeledIntValueMapImpl
	}

	/**
	 * <p>get.</p>
	 *
	 * @param n a {@link java.lang.String} object.
	 * @param proposition a char.
	 * @return a new edge
	 */
	@SuppressWarnings("static-method")
	public LabeledNode get(final String n, final char proposition) {
		return new LabeledNode(n, proposition);// , this.labeledIntValueMapImpl
	}

	/**
	 * @return the class chosen for creating new object.
	 *         public Class<C> getInternalObjectClass() {
	 *         return this.labeledIntValueMapImpl;
	 *         }
	 */

	// @Override
	// public String toString() {
	// return "Labeled value set managed as " + this.labeledIntValueMapImpl.toString() + ".";
	// }

}
