/**
 *
 */
package it.univr.di.cstnu.graph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Hypergraph;
import it.univr.di.labeledvalue.Constants;

/**
 * <p>
 * GraphMLWriter class.
 * </p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class GraphMLWriter extends edu.uci.ics.jung.io.GraphMLWriter<LabeledNode, LabeledIntEdge> {

	/**
	 *
	 */
	AbstractLayout<LabeledNode, LabeledIntEdge> layout;

	/**
	 * Constructor for GraphMLWriter.
	 *
	 * @param lay a {@link AbstractLayout} object.
	 */
	public GraphMLWriter(final AbstractLayout<LabeledNode, LabeledIntEdge> lay) {
		super();
		this.layout = lay;

		this.addGraphData("Name", "LabeledIntGraph Name", "",
				new Transformer<Hypergraph<LabeledNode, LabeledIntEdge>, String>() {
					@Override
					public String transform(final Hypergraph<LabeledNode, LabeledIntEdge> g) {
						return ((LabeledIntGraph) (g)).getName();
					}
				});

		// this.addGraphData("Optimized", "Label optimization", "", new
		// Transformer<Hypergraph<LabeledNode, K>, String>() {
		// @Override
		// public String transform(final Hypergraph<LabeledNode, K> g) {
		// return String.valueOf(((LabeledIntGraph) (g)).isOptimize());
		// }
		// });

		// LabeledNode data manipulation
		// to store layout position for each node
		this.setVertexIDs(new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return v.getName();
			}
		});
		this.addVertexData("x", "The x coordinate for the visualitation. A positive value.", "0",
				new Transformer<LabeledNode, String>() {
					@Override
					public String transform(final LabeledNode v) {
						return Double.toString((GraphMLWriter.this.layout != null) ? GraphMLWriter.this.layout.getX(v) : v.getX());
					}
				});
		this.addVertexData("y", "The y coordinate for the visualitation. A positive value.", "0",
				new Transformer<LabeledNode, String>() {
					@Override
					public String transform(final LabeledNode v) {
						return Double.toString((GraphMLWriter.this.layout != null) ? GraphMLWriter.this.layout.getY(v) : v.getY());
					}
				});
		this.addVertexData("Obs", "Proposition Observed. Format: [" + Constants.PROPOSITION_RANGES + "]", "",
				new Transformer<LabeledNode, String>() {
					@Override
					public String transform(final LabeledNode v) {
						return (v.propositionObserved != Constants.UNKNOWN) ? "" + v.propositionObserved : null;
					}
				});
		this.addVertexData("Label", "Label. Format: [¬[" + Constants.PROPOSITION_RANGES + "]|[" + Constants.PROPOSITION_RANGES + "]]+|⊡", "",
				new Transformer<LabeledNode, String>() {
					@Override
					public String transform(final LabeledNode v) {
						return v.getLabel().toString();
					}
				});

		// K data manipulation
		this.setEdgeIDs(new Transformer<LabeledIntEdge, String>() {
			@Override
			public String transform(final LabeledIntEdge e) {
				return e.getName();
			}
		});
		this.addEdgeData("Type", "Type: Possible values: normal|contingent|constraint.", "normal",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						return e.getConstraintType().toString();
					}
				});
		// this.addEdgeData("Optimized", "If the labeled values must be
		// optimized. Format: a boolean.", "true", new Transformer<K, String>()
		// {
		// @Override
		// public String transform(final K e) {
		// final boolean v = e.optimize;
		// return String.valueOf(v);
		// }
		// });

		this.addEdgeData("LabeledValues", "Labeled Values. Format: {[[\\('integer', 'label'\\) ]+}|{}", "",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						return e.getLabeledValueMap().toString();
					}
				});
		//
		this.addEdgeData("UpperCaseLabeledValues", "Upper-Case Labeled Values. Format: {[[\\('integer', 'node name (no case modification)', 'label'\\) ]+}|{}",
				"",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						String s = e.getUpperLabelMap().toString();
						return (s.startsWith("{}")) ? null : s;
					}
				});
		this.addEdgeData("LowerCaseLabeledValues", "Lower-Case Labeled Values. Format: {[[\\('integer', 'node name (no case modification)', 'label'\\) ]+}|{}",
				"",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						String s = e.getLowerLabelMap().toString();
						return (s.startsWith("{}")) ? null : s;
					}
				});

	}

}