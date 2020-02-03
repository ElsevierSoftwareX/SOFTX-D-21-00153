/**
 *
 */
package it.univr.di.cstnu.graph;

import java.io.IOException;
import java.io.Writer;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Hypergraph;
import it.univr.di.cstnu.graph.TNGraph.NetworkType;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Literal;

/**
 * Allows the writing of a Temporal Network graph to a file in GraphML format.<br>
 * GraphML format allows the definition of different attributes for the TNGraph, vertices and edges.<br>
 * All attributes are defined in the first part of a GraphML file. Examples of GraphML file that can read by this class are given in the Instances directory
 * under CstnuTool one.<br>
 * It assumes that nodes are {@linkplain LabeledNode} and edges are {@linkplain Edge}.
 * 
 * @author posenato
 * @version $Id: $Id
 */
public class TNGraphMLWriter extends edu.uci.ics.jung.io.GraphMLWriter<LabeledNode, Edge> {

	/**
	 * 
	 */
	static final public String NETWORK_TYPE_KEY = "NetworkType";

	/**
	 * 
	 */
	static final public String EDGE_LABELED_LC_VALUE_KEY = "LowerCaseLabeledValues";

	/**
	 * 
	 */
	static final public String EDGE_LABELED_UC_VALUE_KEY = "UpperCaseLabeledValues";

	/**
	 * 
	 */
	static final public String EDGE_LABELED_VALUE_KEY = "LabeledValues";

	/**
	 * 
	 */
	static final public String EDGE_VALUE_KEY = "Value";

	/**
	 * 
	 */
	static final public String EDGE_TYPE_KEY = "Type";

	/**
	 * 
	 */
	static final public String GRAPH_NAME_KEY = "Name";

	/**
	 * 
	 */
	static final public String GRAPH_nCTG_KEY = "nContingent";
	/**
	 * 
	 */
	static final public String GRAPH_nEDGES_KEY = "nEdges";

	/**
	 * 
	 */
	static final public String GRAPH_nOBS_KEY = "nObservedProposition";
	/**
	 * 
	 */
	static final public String GRAPH_nVERTICES_KEY = "nVertices";

	/**
	 * 
	 */
	static final public String NODE_LABEL_KEY = "Label";

	/**
	 * 
	 */
	static final public String NODE_OBSERVED_KEY = "Obs";

	/**
	 * 
	 */
	static final public String NODE_POTENTIAL_KEY = "Potential";

	/**
	 * 
	 */
	static final public String NODE_X_KEY = "x";

	/**
	 * 
	 */
	static final public String NODE_Y_KEY = "y";

	/**
	 *
	 */
	AbstractLayout<LabeledNode, ? extends Edge> layout;

	/**
	 * Graph type
	 */
	TNGraph.NetworkType networkType;

	/**
	 * Constructor for TNGraphMLWriter.
	 *
	 * @param lay a {@link AbstractLayout} object. If it is null, vertex coordinates are determined from the property of the vertex.
	 */
	public TNGraphMLWriter(final AbstractLayout<LabeledNode, ? extends Edge> lay) {
		super();
		this.layout = lay;

		/*
		 * TNGraph attributes
		 */
		this.addGraphData(NETWORK_TYPE_KEY, "Network Type", "CSTNU",
				new Function<Hypergraph<LabeledNode, Edge>, String>() {
					@Override
					public String apply(final Hypergraph<LabeledNode, Edge> g) {
						TNGraph<Edge> g1 = (TNGraph<Edge>) (g);
						return g1.getType().toString();
					}
				});

		this.addGraphData(GRAPH_NAME_KEY, "Graph Name", "",
				new Function<Hypergraph<LabeledNode, Edge>, String>() {
					@Override
					public String apply(final Hypergraph<LabeledNode, Edge> g) {
						TNGraph<Edge> g1 = (TNGraph<Edge>) (g);
						return g1.getName();
					}
				});

		this.addGraphData(GRAPH_nCTG_KEY, "Number of contingents in the graph", "0",
				new Function<Hypergraph<LabeledNode, Edge>, String>() {
					@Override
					public String apply(final Hypergraph<LabeledNode, Edge> g) {
						if (TNGraphMLWriter.this.networkType == NetworkType.CSTNU)
							return String.valueOf(((TNGraph<Edge>) (g)).getContingentCount());
						return null;
					}
				});
		this.addGraphData(GRAPH_nEDGES_KEY, "Number of edges in the graph", "0",
				(Hypergraph<LabeledNode, Edge> g) -> String.valueOf(((TNGraph<Edge>) (g)).getEdgeCount()));

		this.addGraphData(GRAPH_nOBS_KEY, "Number of observed propositions in the graph", "0",
				new Function<Hypergraph<LabeledNode, Edge>, String>() {
					@Override
					public String apply(final Hypergraph<LabeledNode, Edge> g) {
						if (TNGraphMLWriter.this.networkType == NetworkType.CSTNU || TNGraphMLWriter.this.networkType == NetworkType.CSTN)
							return String.valueOf(((TNGraph<Edge>) (g)).getObserverCount());
						return null;
					}
				});
		this.addGraphData(GRAPH_nVERTICES_KEY, "Number of vertices in the graph", "0",
				new Function<Hypergraph<LabeledNode, Edge>, String>() {
					@Override
					public String apply(final Hypergraph<LabeledNode, Edge> g) {
						return String.valueOf(((TNGraph<Edge>) (g)).getVertexCount());
					}
				});

		/*
		 * Node attributes
		 */
		this.setVertexIDs(new Function<LabeledNode, String>() {
			@Override
			public String apply(final LabeledNode v) {
				return v.getName();
			}
		});

		this.addVertexData(NODE_X_KEY, "The x coordinate for the visualitation. A positive value.", "0",
				new Function<LabeledNode, String>() {
					@Override
					public String apply(final LabeledNode v) {
						return Double.toString((TNGraphMLWriter.this.layout != null) ? TNGraphMLWriter.this.layout.getX(v) : v.getX());
					}
				});
		this.addVertexData(NODE_Y_KEY, "The y coordinate for the visualitation. A positive value.", "0",
				new Function<LabeledNode, String>() {
					@Override
					public String apply(final LabeledNode v) {
						return Double.toString((TNGraphMLWriter.this.layout != null) ? TNGraphMLWriter.this.layout.getY(v) : v.getY());
					}
				});
		this.addVertexData(NODE_OBSERVED_KEY, "Proposition Observed. Value specification: " + Literal.PROPOSITION_RANGE, "",
				new Function<LabeledNode, String>() {
					@Override
					public String apply(final LabeledNode v) {
						return (v.propositionObserved != Constants.UNKNOWN) ? "" + v.propositionObserved : null;
					}
				});
		this.addVertexData(NODE_POTENTIAL_KEY, "Labeled Potential Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}",
				"",
				new Function<LabeledNode, String>() {
					@Override
					public String apply(final LabeledNode v) {
						String s = v.getLabeledPotential().toString();
						return (s.startsWith("{}")) ? null : s;
					}
				});
		this.addVertexData(NODE_LABEL_KEY, "Label. Format: [¬" + Literal.PROPOSITION_RANGE + "|" + Literal.PROPOSITION_RANGE + "]+|⊡", "⊡",
				new Function<LabeledNode, String>() {
					@Override
					public String apply(final LabeledNode v) {
						if (TNGraphMLWriter.this.networkType == NetworkType.CSTNU || TNGraphMLWriter.this.networkType == NetworkType.CSTN)
							return v.getLabel().toString();
						return null;
					}
				});

		/*
		 * Edge attributes
		 */
		this.setEdgeIDs(new Function<Edge, String>() {
			@Override
			public String apply(final Edge e) {
				return e.getName();
			}
		});
		this.addEdgeData(EDGE_TYPE_KEY, "Type: Possible values: normal|contingent|constraint|derived|internal.", "normal",
				new Function<Edge, String>() {
					@Override
					public String apply(final Edge e) {
						return e.getConstraintType().toString();
					}
				});

		this.addEdgeData(EDGE_LABELED_VALUE_KEY, "Labeled Values. Format: {[('integer', 'label') ]+}|{}", "",
				new Function<Edge, String>() {
					@Override
					public String apply(final Edge e) {
						if (e.isCSTNEdge() || e.isCSTNUEdge()) {
							return ((CSTNEdge) e).getLabeledValueMap().toString();
						}
						return null;
					}
				});
		this.addEdgeData(EDGE_VALUE_KEY, "Value for STN edge. Format: 'integer'", "",
				new Function<Edge, String>() {
					@Override
					public String apply(final Edge e) {
						if (e.isSTNEdge()) {
							return String.valueOf(((STNEdge) e).getValue());
						}
						return null;
					}
				});
		this.addEdgeData(EDGE_LABELED_UC_VALUE_KEY, "Labeled Upper-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}",
				"",
				new Function<Edge, String>() {
					@Override
					public String apply(final Edge e) {
						if (e.isCSTNUEdge()) {
							String s = ((CSTNUEdge) e).getUpperCaseValueMap().toString();
							return (s.startsWith("{}")) ? null : s;
						}
						return null;
					}
				});
		this.addEdgeData(EDGE_LABELED_LC_VALUE_KEY, "Labeled Lower-Case Values. Format: {[('node name (no case modification)', 'integer', 'label') ]+}|{}",
				"",
				new Function<Edge, String>() {
					@Override
					public String apply(final Edge e) {
						if (e.isCSTNUEdge()) {
							String s = ((CSTNUEdge) e).getLowerCaseValue().toString();
							return (s.startsWith("{}")) ? null : s;
						}
						return null;
					}
				});
	}

	/**
	 * @param graph
	 * @param w
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public void save(TNGraph<? extends Edge> graph, Writer w) throws IOException {
		this.networkType = graph.getType();
		super.save((TNGraph<Edge>) graph, w);
	}
}
