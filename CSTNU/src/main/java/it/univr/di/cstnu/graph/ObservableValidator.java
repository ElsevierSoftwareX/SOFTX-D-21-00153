/**
 *
 */
package it.univr.di.cstnu.graph;

import it.univr.di.labeledvalue.Literal;

import java.util.logging.Logger;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

/**
 * @author posenato
 */
public class ObservableValidator implements Validator<String> {

	/**
	 * logger
	 */
	@SuppressWarnings("unused")
	private static Logger LOG = Logger.getLogger(ObservableValidator.class.getName());

	/**
	 *
	 */
	LabeledIntGraph graph;
	/**
	 *
	 */
	LabeledNode node;

	/**
	 * @param g
	 * @param n
	 */
	public ObservableValidator(final LabeledIntGraph g, final LabeledNode n) {
		if (g == null) throw new NullPointerException("LabeledIntGraph cannot be null!");
		this.graph = g;
		if (n == null) throw new NullPointerException("LabeledNode cannot be null!");
		this.node = n;
	}

	@Override
	public Class<String> modelType() {
		return String.class;
	}

	@Override
	public void validate(final Problems problems, final String compName, final String model) {
		if ((model == null) || (model.length() == 0)) return;
		final Literal p = new Literal(model.charAt(0));
		final LabeledNode currentNodeForProposition = this.graph.getObservator(p);

		// LOG.finest("Validate: p=" + p + "; currentNodeForProposition=" + currentNodeForProposition + "; editedNode="
		// + node);

		if (currentNodeForProposition == null) return;
		if (currentNodeForProposition != this.node) {
			problems.append("A " + p + " observer node already exists: " + currentNodeForProposition);
		}
	}

}
