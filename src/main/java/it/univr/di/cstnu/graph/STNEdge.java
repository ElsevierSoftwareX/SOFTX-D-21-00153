// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.Constants;

/**
 * Represents the behavior of a STN edge.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface STNEdge extends Edge {

	/**
	 * @return the weight associated to the edge
	 */
	public int getValue();

	/**
	 * Sets the weight to w.
	 * 
	 * @param w the new weight value
	 * @return the old weight associated to the edge.
	 *         If the weight was not set, it returns {@link Constants#INT_NULL}.
	 */
	public int setValue(int w);
}
