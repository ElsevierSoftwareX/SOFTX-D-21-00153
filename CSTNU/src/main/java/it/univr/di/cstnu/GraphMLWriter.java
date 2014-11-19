/**
 * 
 */
package it.univr.di.cstnu;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.Hypergraph;

/**
 * @author posenato
 */
public class GraphMLWriter extends edu.uci.ics.jung.io.GraphMLWriter<Node, Edge> {

	/**
	 * 
	 */
	StaticLayout<Node, Edge> layout;

	/**
	 * @param lay
	 */
	public GraphMLWriter(StaticLayout<Node, Edge> lay) {
		super();
		layout = lay;

		addGraphData("Name", "Graph Name", "", new Transformer<Hypergraph<Node, Edge>, String>() {
			@Override
			public String transform(Hypergraph<Node, Edge> g) {
				return ((Graph) (g)).getName();
			}
		});

		// Node data manipulation
		// to store layout position for each node
		setVertexIDs(new Transformer<Node, String>() {
			@Override
			public String transform(Node v) {
				return v.getName();
			}
		});
		addVertexData("x", "The x coordinate for the visualitation. A positive value.", "0", new Transformer<Node, String>() {
			@Override
			public String transform(Node v) {
				return Double.toString(layout.getX(v));
			}
		});
		addVertexData("y", "The y coordinate for the visualitation. A positive value.", "0", new Transformer<Node, String>() {
			@Override
			public String transform(Node v) {
				return Double.toString(layout.getY(v));
			}
		});
		addVertexData("Obs", "Proposition Observed. Format: [a-zA-Z]", "", new Transformer<Node, String>() {
			@Override
			public String transform(Node v) {
				return (v.propositionObserved != null) ? v.propositionObserved.toString() : "";
			}
		});
		addVertexData("Label", "Label. Format: [¬[a-zA-Z]|[a-zA-Z]]+|⊡", "", new Transformer<Node, String>() {
			@Override
			public String transform(Node v) {
				return v.getLabel().toString();
			}
		});

		// Edge data manipulation
		setEdgeIDs(new Transformer<Edge, String>() {
			@Override
			public String transform(Edge e) {
				return e.getName();
			}
		});
		addEdgeData("Type", "Type: Possible values: normal|contingent|constraint.", "normal", new Transformer<Edge, String>() {
			@Override
			public String transform(Edge e) {
				return e.getType().toString();
			}
		});
		addEdgeData("Value", "Value. Format: an integer.", "1", new Transformer<Edge, String>() {
			@Override
			public String transform(Edge e) {
				Integer v = e.getInitialValue();
				return (v == null) ? "" : v.toString();
			}
		});

		addEdgeData("LabeledValues", "Labeled Values. Format: {[[\\('label', 'integer'\\) ]+}|{}", "", new Transformer<Edge, String>() {
			@Override
			public String transform(Edge e) {
				return e.getLabeledValueMap().toString();
			}
		});
		//
		addEdgeData("UpperCaseLabeledValues", "Upper-Case Labeled Values. Format: {[[\\('label', 'UPPER CASE NAME', 'integer'\\) ]+}|{}", "", new Transformer<Edge, String>() {
			public String transform(Edge e) {
				return e.getUpperLabelMap().toString();
			}
		});
		addEdgeData("LowerCaseLabeledValues", "Lower-Case Labeled Values. Format: {[[\\('label', 'lower case name', 'integer'\\) ]+}|{}", "", new Transformer<Edge, String>() {
			public String transform(Edge e) {
				return e.getLowerLabelMap().toString(true);
			}
		});

	}

}
