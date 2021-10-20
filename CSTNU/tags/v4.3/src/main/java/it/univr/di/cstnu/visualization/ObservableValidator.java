// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 *
 */
package it.univr.di.cstnu.visualization;

import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;

/**
 * <p>
 * ObservableValidator class.
 * </p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class  ObservableValidator implements Validator<String> {

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger("ObservableValidator");

	/**
	 * Version
	 */
	static final public String VERSIONandDATE = "1.0, June, 9 2019";// Refactoring Edge

	/**
	 *
	 */
	TNGraph<?> tnGraph;
	/**
	 *
	 */
	LabeledNode node;

	/**
	 * <p>Constructor for ObservableValidator.</p>
	 *
	 * @param g a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 * @param n a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 */
	public ObservableValidator(final TNGraph<?> g, final LabeledNode n) {
		if (g == null)
			throw new NullPointerException("TNGraph cannot be null!");
		this.tnGraph = g;
		if (n == null)
			throw new NullPointerException("LabeledNode cannot be null!");
		this.node = n;
	}

	/** {@inheritDoc} */
	@Override
	public Class<String> modelType() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public void validate(final Problems problems, final String compName, final String model) {
		if ((model == null) || (model.length() == 0))
			return;
		final LabeledNode currentNodeForProposition = this.tnGraph.getObserver(model.charAt(0));

		// LOG.finest("Validate: p=" + p + "; currentNodeForProposition=" + currentNodeForProposition + "; editedNode="
		// + node);

		if (currentNodeForProposition == null)
			return;
		if (currentNodeForProposition != this.node) {
			problems.append("An observer for '" + model.charAt(0) + "' already exists: " + currentNodeForProposition);
		}
	}

}
