/**
 *
 */
package it.univr.di.cstnu.graph;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * <p>GraphMLWriter class.</p>
 *
 * @author posenato
 * @version $Id: $Id
 */
public class GraphMLWriter extends edu.uci.ics.jung.io.GraphMLWriter<LabeledNode, LabeledIntEdge> {

	/**
	 *
	 */
	StaticLayout<LabeledNode, LabeledIntEdge> layout;

	/**
	 * <p>Constructor for GraphMLWriter.</p>
	 *
	 * @param lay a {@link it.univr.di.cstnu.graph.StaticLayout} object.
	 */
	public GraphMLWriter(final StaticLayout<LabeledNode, LabeledIntEdge> lay) {
		super();
		this.layout = lay;

		this.addGraphData("Name", "LabeledIntGraph Name", "", new Transformer<Hypergraph<LabeledNode, LabeledIntEdge>, String>() {
			@Override
			public String transform(final Hypergraph<LabeledNode, LabeledIntEdge> g) {
				return ((LabeledIntGraph) (g)).getName();
			}
		});

		this.addGraphData("Optimized", "Label optimization", "", new Transformer<Hypergraph<LabeledNode, LabeledIntEdge>, String>() {
			@Override
			public String transform(final Hypergraph<LabeledNode, LabeledIntEdge> g) {
				return String.valueOf(((LabeledIntGraph) (g)).isOptimize());
			}
		});

		// LabeledNode data manipulation
		// to store layout position for each node
		this.setVertexIDs(new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return v.getName();
			}
		});
		this.addVertexData("x", "The x coordinate for the visualitation. A positive value.", "0", new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return Double.toString(GraphMLWriter.this.layout.getX(v));
			}
		});
		this.addVertexData("y", "The y coordinate for the visualitation. A positive value.", "0", new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return Double.toString(GraphMLWriter.this.layout.getY(v));
			}
		});
		this.addVertexData("Obs", "Proposition Observed. Format: [a-zA-Z]", "", new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return (v.propositionObserved != null) ? v.propositionObserved.toString() : "";
			}
		});
		this.addVertexData("Label", "Label. Format: [¬[a-zA-Z]|[a-zA-Z]]+|⊡", "", new Transformer<LabeledNode, String>() {
			@Override
			public String transform(final LabeledNode v) {
				return v.getLabel().toString();
			}
		});

		// LabeledIntEdge data manipulation
		this.setEdgeIDs(new Transformer<LabeledIntEdge, String>() {
			@Override
			public String transform(final LabeledIntEdge e) {
				return e.getName();
			}
		});
		this.addEdgeData("Type", "Type: Possible values: normal|contingent|constraint.", "normal", new Transformer<LabeledIntEdge, String>() {
			@Override
			public String transform(final LabeledIntEdge e) {
				return e.getType().toString();
			}
		});
		this.addEdgeData("Optimized", "If the labeled values must be optimized. Format: a boolean.", "true", new Transformer<LabeledIntEdge, String>() {
			@Override
			public String transform(final LabeledIntEdge e) {
				final boolean v = e.optimize;
				return String.valueOf(v);
			}
		});

		this.addEdgeData("LabeledValues", "Labeled Values. Format: {[[\\('label', 'integer'\\) ]+}|{}", "", new Transformer<LabeledIntEdge, String>() {
			@Override
			public String transform(final LabeledIntEdge e) {
				return e.getLabeledValueMap().toString();
			}
		});
		//
		this.addEdgeData("UpperCaseLabeledValues", "Upper-Case Labeled Values. Format: {[[\\('label', 'UPPER CASE NAME', 'integer'\\) ]+}|{}", "",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						return e.getUpperLabelMap().toString();
					}
				});
		this.addEdgeData("LowerCaseLabeledValues", "Lower-Case Labeled Values. Format: {[[\\('label', 'lower case name', 'integer'\\) ]+}|{}", "",
				new Transformer<LabeledIntEdge, String>() {
					@Override
					public String transform(final LabeledIntEdge e) {
						return e.getLowerLabelMap().toString(true);
					}
				});

	}

}
