/**
 * 
 */
package it.univr.di.cstnu;

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
	Graph graph;
	/**
	 * 
	 */
	Node node;

	/**
	 * @param g
	 * @param n
	 */
	public ObservableValidator(Graph g, Node n) {
		if (g == null) throw new NullPointerException("Graph cannot be null!");
		graph = g;
		if (n == null) throw new NullPointerException("Node cannot be null!");
		node = n;
	}

	@Override
	public void validate(Problems problems, String compName, String model) {
		if ((model == null) || (model.length() == 0)) return;
		final Literal p = new Literal(model.charAt(0));
		final Node currentNodeForProposition = graph.getObservable(p);

		// LOG.finest("Validate: p=" + p + "; currentNodeForProposition=" + currentNodeForProposition + "; editedNode="
		// + node);

		if (currentNodeForProposition == null) return;
		if (currentNodeForProposition != node) {
			problems.append("A " + p + " observer node already exists: " + currentNodeForProposition);
		}
	}

	@Override
	public Class<String> modelType() {
		return String.class;
	}

}
