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
	 */
	public LabeledNodeSupplier() {// Class<C> labeledIntMapImplementation
		super();
		// this.labeledIntValueMapImpl = labeledIntMapImplementation;
	}

	/**
	 * @return a new LabeledIntMap concrete object.
	 */
	@Override
	public LabeledNode get() {
		return get("");
	}

	/**
	 * @param name
	 * @return a new LabeledIntMap concrete object.
	 */
	@SuppressWarnings("static-method")
	public LabeledNode get(String name) {
		return new LabeledNode(name);// , this.labeledIntValueMapImpl
	}

	/**
	 * @param node the edge to clone.
	 * @return a new edge
	 */
	@SuppressWarnings("static-method")
	public LabeledNode get(LabeledNode node) {
		return new LabeledNode(node);// , this.labeledIntValueMapImpl
	}

	/**
	 * @param n
	 * @param proposition
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
