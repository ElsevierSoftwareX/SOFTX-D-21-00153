/**
 * 
 */
package it.univr.di.cstnu;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Simple extensions of Jung DirectedSparseGraph<Node, Edge> that allows an easy access to the details of an edge or of
 * a node given the name.<br>
 * In this graph, node name and edge name are key.<br>
 * 
 * @author posenato
 */
public class Graph extends DirectedSparseGraph<Node, Edge> {

	/**
	 * 
	 */
	private static Logger LOG = Logger.getLogger(Graph.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Set of edges with lower case label set not empty
	 */
	private HashSet<Edge> lowerCaseEdgesSet;

	/**
	 * Set of propositions observed in the graph.
	 */
	private Set<Literal> observedProposition;

	/**
	 * Name
	 */
	private String name;
	
	/**
	 * Zero node. In temporal constraint network such node is the first node to execute.
	 */
	Node Z = null;
	
	/**
	 * The structure of this object comes from the super class:<br>
	 * protected Map<Node, Pair<Map<Node,Edge>>> vertices; // Map of vertices to Pair of adjacency maps {incoming, outgoing} of neighbor vertices to incident edges.<br>
	 * protected Map<Edge, Pair<Node>> edges; // Map of edges to incident vertex pairs
	 */
	public Graph() {
		super();
	}

	/**
	 * A constructor that clones a given graph g. If g is null, this new graph will be empty.
	 * 
	 * @param g the graph to be cloned
	 */
	public Graph(Graph g) {
		this();
		if (g == null) return;

		name = g.name;
		
		// clone all nodes
		Node vNew;
		for (final Node v : g.getVerticesArray()) {
			vNew = new Node(v);
			addVertex(vNew);
			if (v.equalsByName(g.Z)) this.Z = vNew;
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		Edge eNew;
		for (final Edge e : g.getEdges()) {
			eNew = new Edge(e);
			this.addEdge(eNew, getNode(g.getSource(e).getName()), getNode(g.getDest(e).getName()));
		}

		/*
		 * for (Edge e : g.lowerLabeledEdges) { this.lowerLabeledEdges.add(this.getEdge(e.getName())); } for (Edge e :
		 * g.upperLabeledEdges) {
		 * this.upperLabeledEdges.add(this.getEdge(e.getName())); } this.proposition.addAll(g.proposition);
		 */
	}

	/**
	 * @param name a name to the graph
	 */
	public Graph(String name) {
		super();
		this.name = name;
	}

	/**
	 * @param e
	 * @return true if the addition has been successful.
	 */
	// public boolean addLowerLabeledEdge(Edge e) {
	// return lowerLabeledEdges.add(e);
	// }

	/**
	 * @param p
	 * @return true if the addition has been successful.
	 */
	// public boolean addProposition(Literal p) {
	// return proposition.add(p);
	// }

	/**
	 * @param e
	 * @return true if the addition has been successful.
	 */
	// public boolean addUpperLabeledEdge(Edge e) {
	// return upperLabeledEdges.add(e);
	// }

	/**
	 * 
	 */
	// public void clearLowerLabeledEdges() {
	// lowerLabeledEdges.clear();
	// }

	/**
	 * 
	 */
	// public void clearPropositions() {
	// proposition.clear();
	// }

	/**
	 * 
	 */
	// public void clearUpperLabeledEdges() {
	// upperLabeledEdges.clear();
	// }

	/*
	 * It is necessary to copy the general method here because otherwise it calls the general addEdge(e, v1, v2,
	 * edge_type)!!!
	 */
	@Override
	public boolean addEdge(Edge e, Node v1, Node v2) {
		return addEdge(e, v1, v2, getDefaultEdgeType());
	}

	/*
	 * It is necessary to copy the general method here because otherwise it calls the general addEdge(e, new
	 * Pair<Node>(v1, v2), edge_type)!!!
	 * @see edu.uci.ics.jung.graph.AbstractGraph#addEdge(java.lang.Object, java.lang.Object, java.lang.Object,
	 * edu.uci.ics.jung.graph.util.EdgeType)
	 */
	@Override
	public boolean addEdge(Edge e, Node v1, Node v2, EdgeType edge_type1) {
		return addEdge(e, new Pair<>(v1, v2), edge_type1);
	}

	/**
	 * @param g1
	 * @return the string containing all edges that are different in the two graphs.
	 */
	public String calculateDifferentEdges(Graph g1) {
		final StringBuffer sb = new StringBuffer("Different edges:\n");
		if (g1 == null) return sb.toString();
		Edge e;

		for (final Edge e1 : g1.edges.keySet()) {
			e = getEdge(e1.getName());
			if ((e == null) || !e.getLabeledValueMap().equals(e1.getLabeledValueMap())
					|| !e.getUpperLabelSet().equals(e1.getUpperLabelSet())
					|| !e.getLowerLabelSet().equals(e1.getLowerLabelSet()))
				sb.append("this.e: " + e + "\n" + "othe.e: " + e1 + "\n");
		}
		return sb.append("end different edges\n").toString();
	}

	/**
	 * 
	 */
	public void cleanLowerEdgeCache() {
		lowerCaseEdgesSet = null;
	}

	/**
	 * 
	 */
	public void cleanPropositionCache() {
		observedProposition = null;
	}

	/**
	 * Takes in all internal structures of g.<br>
	 * This method is useful to copy a graph into the current without modifying the reference to the current.
	 * 
	 * @param g the graph to copy.
	 */
	public void takeIn(Graph g) {
		this.name = g.name;
		this.vertices = g.vertices;
		this.edges = g.edges;
		this.lowerCaseEdgesSet = g.lowerCaseEdgesSet;
		this.observedProposition = g.observedProposition;
		this.Z = g.Z;
	}

	/**
	 * Clone all internal structures of g into this.<br>
	 * This method is useful to copy a graph into the current without modifying the reference to the current.
	 * 
	 * @param g the graph to copy.
	 */
	public void clone(Graph g) {
		name = g.name;

		edges.clear();
		vertices.clear();
		cleanLowerEdgeCache();
		cleanPropositionCache();
		// clone all nodes
		Node vNew;
		for (final Node v : g.getVerticesArray()) {
			vNew = new Node(v);
			addVertex(vNew);
			if (v.equalsByName(g.Z)) this.Z = vNew;
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		Edge eNew;
		for (final Edge e : g.getEdges()) {
			eNew = new Edge(e);
			this.addEdge(eNew, getNode(g.getSource(e).getName()), getNode(g.getDest(e).getName()));
		}
	}

	/**
	 * Equals based on equals of edges and vertices.
	 * 
	 * @param obj
	 * @return true if the obj is a graph with equals vertices and edges.
	 */
	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof Graph)) return false;
		final Graph g1 = (Graph) obj;
		return g1.edges.equals(edges) && g1.vertices.equals(vertices);
	}

	/**
	 * Returns the edge associated to the name.
	 * 
	 * @param s
	 * @return the edge associated to the name.
	 */
	public Edge getEdge(String s) {
		if ((s == null) || s.isEmpty()) return null;
		for (final Edge e : edges.keySet())
			if (e.getName().equals(s)) return e;
		return null;
	}

	/**
	 * @return the set of edges as an array ordered w.r.t the name of edge in ascending order.
	 */
	public Edge[] getEdgesArray() {
		final Edge[] edgesA = edges.keySet().toArray(new Edge[0]);
		return edgesA;
	}

	/**
	 * In order to compare saved files more easily, we save nodes and edges in lexicographical order.
	 */
	@Override
	public Collection<Edge> getEdges() {
		return Collections.unmodifiableCollection(new TreeSet<>(edges.keySet()));
	}

	/**
	 * @return the set of edges containing Lower Case Labels
	 */
	public Set<Edge> getLowerLabeledEdges() {
		if (lowerCaseEdgesSet == null) {
			lowerCaseEdgesSet = new HashSet<>();
			for (final Edge e : edges.keySet())
				if (e.lowerLabelSize() > 0) lowerCaseEdgesSet.add(e);
		}
		return lowerCaseEdgesSet;
	}

	
	/**
	 * @return the Z node
	 */
	public Node getZ() {
		return Z;
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the node associated to the name.
	 * 
	 * @param s
	 * @return the node associated to the name.
	 */
	public Node getNode(String s) {
		if ((s == null) || s.isEmpty()) return null;
		final Node[] a = getVerticesArray();
		for (final Node n : a)
			if (n.getName().equals(s)) return n;
		return null;
	}

	/**
	 * @param l the request proposition
	 * @return the node that observes the proposition l if it exists, null otherwise.
	 */
	public Node getObservable(Literal l) {
		final Map<Literal, Node> observer = getObservables();
		if (observer == null || l == null) return null;

		Graph.LOG.finest("Literal=" + l + "; observer=" + observer);
		return observer.get(l.isNegative() ? l.negation() : l);
	}

	/**
	 * @return the map of propositions and their observer nodes. If there is no observable node, it returns null. The
	 *         key is the literal observed.
	 */
	public Map<Literal, Node> getObservables() {
		final Set<Node> verticiesSet = vertices.keySet();
		final Map<Literal, Node> obs = new HashMap<>();

		Literal l;
		for (final Node n : verticiesSet)
			if ((l = n.getObservable()) != null) obs.put(l, n);
		return (obs.size() > 0) ? obs : null;
	}

	/**
	 * @return the set of proposition of the graph.
	 */
	public Set<Literal> getPropositions() {
		if (observedProposition == null) {
			observedProposition = new HashSet<>();
			Literal l;
			for (final Node n : vertices.keySet())
				if ((l = n.getObservable()) != null) observedProposition.add(l);
		}
		return observedProposition;
	}

	/**
	 * @return the set of edges containing Upper Case Label.
	 */
	public Set<Edge> getUpperLabeledEdges() {
		final HashSet<Edge> es1 = new HashSet<>();
		for (final Edge e : edges.keySet())
			if (e.upperLabelSize() > 0) es1.add(e);
		return es1;
	}

	/**
	 * @return the set of vertices as an array ordered w.r.t the name of node in ascending order.
	 */
	public Node[] getVerticesArray() {
		final Node[] nodes = vertices.keySet().toArray(new Node[0]);
		return nodes;
	}

	/**
	 * In order to compare saved files more easily, we save nodes and edges in lexicographical order.
	 */
	@Override
	public Collection<Node> getVertices() {
		return Collections.unmodifiableCollection(new TreeSet<>(vertices.keySet()));
	}

	/**
	 * @return return the set of node ordered w.r.t. the lexicographical order of their names.
	 */
	public Collection<Node> getNodes() {
		return getVertices();
	}
	
	/**
	 * Since equals has been specialized, hashCode too.
	 * 
	 * @return hash code associated to this graph.
	 */
	@Override
	public int hashCode() {
		return edges.hashCode() + vertices.hashCode();
	}

	/**
	 * @param g1
	 * @return true if this graph has the same set of edges (by equals()) of g1. false otherwise.
	 */
	public boolean hasSameEdgesOf(Graph g1) {
		final StringBuffer sb = new StringBuffer("Different edges:");
		if (g1 == null) return false;

		Edge e;
		boolean sameEdges = true;
		for (final Edge e1 : g1.edges.keySet()) {
			e = this.getEdge(e1.getName());
			if ((e == null) || !e.equalsLabeledValues(e1)) {
				sb.append("\nCurrent  edge: " + e + "\n"
						+ "Modified edge: " + e1);
				sameEdges = false;// i want to log all differences!!!
			}
		}
		Graph.LOG.info(sb.toString());
		return sameEdges;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(
				"%Graph: "
						+ name
						+ "\n%Graph Syntax\n"
						+ "%Node: <name, label, proposition observed>\n"
						+ "%Edge: <name, type, source node, dest. node, L:{labeled values}, LL:{lower case labeled values}, UL:{upper case labeled values}>\n");
		sb.append("Nodes:\n");
		for (final Node n : getVerticesArray())
			sb.append("<" + n.getName() + ",\t" + n.getLabel() + ",\t" + n.getObservable() + ">\n");
		sb.append("Edges:\n");
		for (final Edge e : getEdgesArray())
			sb.append("<" + e.getName() + ",\t" + e.getType() + ",\t" + getSource(e).getName() + ",\t"
					+ getDest(e).getName() + ",\tL:" + e.getLabeledValueMap().toString() + ", LL:"
					+ e.lowerLabelsToString()
					+ ", UL:" + e.upperLabelsToString() + ">\n");
		return sb.toString();
	}
}
