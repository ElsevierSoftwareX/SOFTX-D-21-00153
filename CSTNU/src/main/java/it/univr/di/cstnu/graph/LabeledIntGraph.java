/**
 *
 */
package it.univr.di.cstnu.graph;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Simple extensions of Jung DirectedSparseGraph<LabeledNode, LabeledIntEdge> that allows an easy access to the details of an edge or of
 * a node given the name.<br>
 * In this graph, node name and edge name are key.<br>
 *
 * @author posenato
 */
public class LabeledIntGraph extends DirectedSparseGraph<LabeledNode, LabeledIntEdge> {

	/**
	 *
	 */
	private static Logger LOG = Logger.getLogger(LabeledIntGraph.class.getName());

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * To activate all optimization code in order to remove the redundant label in the set.
	 * No final because in the simulator we want to reuse graph internal data structures.
	 */
	boolean optimize;

	/**
	 * Set of edges with lower case label set not empty
	 */
	private ObjectArraySet<LabeledIntEdge> lowerCaseEdgesSet;

	/**
	 * Name
	 */
	private String name;

	/**
	 * Set of propositions observed in the graph.
	 */
	private Set<Literal> observedProposition;

	/**
	 * Children of observation nodes
	 */
	private Map<LabeledNode, Set<Literal>> childrenOfObservator;

	/**
	 * Zero node. In temporal constraint network such node is the first node to execute.
	 */
	private LabeledNode Z = null;

	/**
	 * The structure of this object comes from the super class:<br>
	 * protected Map<LabeledNode, Pair<Map<LabeledNode,LabeledIntEdge>>> vertices; // Map of vertices to Pair of adjacency maps {incoming, outgoing} of neighbor
	 * vertices to incident edges.<br>
	 * protected Map<LabeledIntEdge, Pair<LabeledNode>> edges; // Map of edges to incident vertex pairs
	 *
	 * @param optimize To activate all optimization code in order to remove the redundant label in the set.
	 */
	public LabeledIntGraph(final boolean optimize) {
		super();
		this.optimize = optimize;
	}

	/**
	 * A constructor that clones a given graph g. If g is null, this new graph will be empty.
	 *
	 * @param g the graph to be cloned
	 * @param forceOptimization TODO
	 */
	public LabeledIntGraph(final LabeledIntGraph g, final boolean forceOptimization) {
		this(forceOptimization);
		if (g == null) return;

		this.name = g.name;
		this.optimize = forceOptimization;

		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVerticesArray()) {
			vNew = new LabeledNode(v);
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		LabeledIntEdge eNew;
		for (final LabeledIntEdge e : g.getEdges()) {
			eNew = new LabeledIntEdge(e, forceOptimization);
			this.addEdge(eNew, this.getNode(g.getSource(e).getName()), this.getNode(g.getDest(e).getName()));
		}

		/*
		 * for (LabeledIntEdge e : g.lowerLabeledEdges) { this.lowerLabeledEdges.add(this.getEdge(e.getName())); } for (LabeledIntEdge e :
		 * g.upperLabeledEdges) {
		 * this.upperLabeledEdges.add(this.getEdge(e.getName())); } this.proposition.addAll(g.proposition);
		 */
	}

	/**
	 * @param name a name to the graph
	 * @param optimize
	 */
	public LabeledIntGraph(final String name, final boolean optimize) {
		super();
		this.name = name;
		this.optimize = optimize;
	}

	/*
	 * It is necessary to copy the general method here because otherwise it calls the general addEdge(e, v1, v2,
	 * edge_type)!!!
	 */
	@Override
	public boolean addEdge(LabeledIntEdge e, final LabeledNode v1, final LabeledNode v2) {
		if (e.optimize != this.optimize) {
			e = new LabeledIntEdge(e, this.optimize);
		}
		return this.addEdge(e, v1, v2, this.getDefaultEdgeType());
	}

	/*
	 * It is necessary to copy the general method here because otherwise it calls the general addEdge(e, new
	 * Pair<LabeledNode>(v1, v2), edge_type)!!!
	 * @see edu.uci.ics.jung.graph.AbstractGraph#addEdge(java.lang.Object, java.lang.Object, java.lang.Object,
	 * edu.uci.ics.jung.graph.util.EdgeType)
	 */
	@Override
	public boolean addEdge(LabeledIntEdge e, final LabeledNode v1, final LabeledNode v2, final EdgeType edge_type1) {
		if (e.optimize != this.optimize) {
			e = new LabeledIntEdge(e, this.optimize);
		}
		return this.addEdge(e, new Pair<>(v1, v2), edge_type1);
	}

	/**
	 *
	 */
	public void clearCache() {
		this.lowerCaseEdgesSet = null;
		this.observedProposition = null;
		this.childrenOfObservator = null;
	}

	/**
	 * Clone (defensive copy) all internal structures of g into this.<br>
	 * This method is useful to copy a graph into the current without modifying the reference to the current.
	 *
	 * @param g the graph to copy.
	 */
	public void clone(final LabeledIntGraph g) {
		this.name = g.name;
		this.optimize = g.optimize;
		this.edges.clear();
		this.vertices.clear();
		this.clearCache();
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVerticesArray()) {
			vNew = new LabeledNode(v);
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		LabeledIntEdge eNew;
		for (final LabeledIntEdge e : g.getEdges()) {
			eNew = new LabeledIntEdge(e, this.optimize);
			this.addEdge(eNew, this.getNode(g.getSource(e).getName()), this.getNode(g.getDest(e).getName()));
		}
	}

	
	/**
	 * @param g
	 */
	public void cloneCleaningRedundantLabels(LabeledIntGraph g) {
		this.name = g.name;
		this.optimize = g.optimize;
		this.edges.clear();
		this.vertices.clear();
		this.clearCache();
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVerticesArray()) {
			vNew = new LabeledNode(v);
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		LabeledIntEdge eNew;
		int value;
		Label label;
		for (final LabeledIntEdge e : g.getEdges()) {
			eNew = new LabeledIntEdge(e.getName(), e.getType(), this.optimize);
			for( Object2IntMap.Entry<Label> entry : e.labeledValueSet()) {
				value = entry.getIntValue();
				if (value == Constants.INT_NEG_INFINITE) continue;
				label = entry.getKey();
				if (label.containsUnknown()) continue;
				eNew.mergeLabeledValue(entry.getKey(), value);
			}
			for( Object2IntMap.Entry<Entry<Label, String>> entry : e.getUpperLabelSet()) {
				eNew.mergeUpperLabelValue(entry.getKey().getKey(), entry.getKey().getValue(), entry.getIntValue());
			}
			this.addEdge(eNew, this.getNode(g.getSource(e).getName()), this.getNode(g.getDest(e).getName()));
		}
	}
	
	/**
	 * Equals based on equals of edges and vertices.
	 *
	 * @param obj
	 * @return true if the obj is a graph with equals vertices and edges.
	 */
	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof LabeledIntGraph)) return false;
		final LabeledIntGraph g1 = (LabeledIntGraph) obj;
		return g1.edges.equals(this.edges) && g1.vertices.equals(this.vertices);
	}

	/**
	 * Returns the edge associated to the name.
	 *
	 * @param s
	 * @return the edge associated to the name.
	 */
	public LabeledIntEdge getEdge(final String s) {
		if ((s == null) || s.isEmpty()) return null;
		for (final LabeledIntEdge e : this.edges.keySet())
			if (e.getName().equals(s)) return e;
		return null;
	}

	/**
	 * In order to compare saved files more easily, we save nodes and edges in lexicographical order.
	 */
	@Override
	public Collection<LabeledIntEdge> getEdges() {
		final ObjectRBTreeSet<LabeledIntEdge> ordered = new ObjectRBTreeSet<>(this.edges.keySet());
		return ordered;
	}

	/**
	 * @return the set of edges as an array ordered w.r.t the name of edge in ascending order.
	 */
	public LabeledIntEdge[] getEdgesArray() {
		final LabeledIntEdge[] edgesA = this.getEdges().toArray(new LabeledIntEdge[this.getEdgeCount()]);
		return edgesA;
	}

	/**
	 * @return the set of edges containing Lower Case Labels
	 */
	public Set<LabeledIntEdge> getLowerLabeledEdges() {
		if (this.lowerCaseEdgesSet == null) {
			this.lowerCaseEdgesSet = new ObjectArraySet<>();
			for (final LabeledIntEdge e : this.edges.keySet())
				if (e.lowerLabelSize() > 0) {
					this.lowerCaseEdgesSet.add(e);
				}
		}
		return this.lowerCaseEdgesSet;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the node associated to the name.
	 *
	 * @param s
	 * @return the node associated to the name.
	 */
	public LabeledNode getNode(final String s) {
		if ((s == null) || s.isEmpty()) return null;
		final LabeledNode[] a = this.getVerticesArray();
		for (final LabeledNode n : a)
			if (n.getName().equals(s)) return n;
		return null;
	}

	/**
	 * @return return the set of node ordered w.r.t. the lexicographical order of their names.
	 */
	public Collection<LabeledNode> getNodes() {
		return this.getVertices();
	}

	/**
	 * @param l the request proposition
	 * @return the node that observes the proposition l if it exists, null otherwise.
	 */
	public LabeledNode getObservator(final Literal l) {
		final Map<Literal, LabeledNode> observer = this.getObservedAndObservator();
		if ((observer == null) || (l == null)) return null;

		LabeledIntGraph.LOG.finest("Literal=" + l + "; observer=" + observer);
		return observer.get(l.isNegated() ? l.getComplement() : l);
	}

	/**
	 * @return the map of propositions and their observator nodes. If there is no observator node, it returns null. The key is the literal observed.
	 */
	public Map<Literal, LabeledNode> getObservedAndObservator() {
		final Set<LabeledNode> vertexSet = this.vertices.keySet();
		final Object2ObjectArrayMap<Literal, LabeledNode> obs = new Object2ObjectArrayMap<>();

		Literal l;
		for (final LabeledNode n : vertexSet)
			if ((l = n.getPropositionObserved()) != null) obs.put(l, n);
		return (obs.size() > 0) ? obs : null;
	}

	/**
	 * @return the set of proposition of the graph.
	 */
	public Set<Literal> getPropositions() {
		if (this.observedProposition == null) {
			this.observedProposition = new ObjectArraySet<>();
			Literal l;
			for (final LabeledNode n : this.vertices.keySet())
				if ((l = n.getPropositionObserved()) != null) {
					this.observedProposition.add(l);
				}
		}
		return this.observedProposition;
	}

	/**
	 * Given a observation node P that observes the proposition 'p', its 'children' are all observation nodes, Q, for which 'p' appears in the label of node Q.
	 * <p>
	 * This method returns the set of children of a given node as a set of the straight literals associated to the children instead of the children nodes.
	 * 
	 * @param obs
	 * @return the set of children of observation node obs, null if there is no children.
	 */
	public Set<Literal> getChildrenOf(LabeledNode obs) {
		if (obs == null) return null;
		// The soundness of this method is based on the properties that the observed proposition of an observation node
		// is represented as a straight literal.
		if (this.childrenOfObservator == null) {
			this.childrenOfObservator = new Object2ObjectRBTreeMap<>();
			// LabeledNode, ObjectRBTreeSet<Literal>>();
			for (Entry<Literal, LabeledNode> entryObservedObservatorNode : this.getObservedAndObservator().entrySet()) {
				Literal litObserved = entryObservedObservatorNode.getKey();
				LabeledNode observator = entryObservedObservatorNode.getValue();
				Label observatorLabel = observator.getLabel();
				for (Literal litInObsLabel : observatorLabel.getAllAsStraight()) {
					LabeledNode father = this.getObservator(litInObsLabel);// for the well property, father must exist!
					Set<Literal> setChildrenOfFather = childrenOfObservator.get(father);
					if (setChildrenOfFather == null) {
						setChildrenOfFather = new ObjectRBTreeSet<>();
						childrenOfObservator.put(father, setChildrenOfFather);
					}
					setChildrenOfFather.add(litObserved);
				}
			}
		}
		return childrenOfObservator.get(obs);
	}

	/**
	 * @return the set of edges containing Upper Case Label.
	 */
	public Set<LabeledIntEdge> getUpperLabeledEdges() {
		final ObjectArraySet<LabeledIntEdge> es1 = new ObjectArraySet<>();
		for (final LabeledIntEdge e : this.edges.keySet())
			if (e.upperLabelSize() > 0) {
				es1.add(e);
			}
		return es1;
	}

	/**
	 * In order to compare saved files more easily, we save nodes and edges in lexicographical order.
	 */
	@Override
	public Collection<LabeledNode> getVertices() {
		final ObjectRBTreeSet<LabeledNode> ordered = new ObjectRBTreeSet<>(this.vertices.keySet());
		return ordered;
	}

	/**
	 * @return the set of vertices as an array ordered w.r.t the name of node in ascending order.
	 */
	public LabeledNode[] getVerticesArray() {
		final LabeledNode[] nodes = this.getVertices().toArray(new LabeledNode[this.getVertexCount()]);
		return nodes;
	}

	/**
	 * @return the Z node
	 */
	public LabeledNode getZ() {
		return this.Z;
	}

	/**
	 * Since equals has been specialized, hashCode too.
	 *
	 * @return hash code associated to this graph.
	 */
	@Override
	public int hashCode() {
		return this.edges.hashCode() + this.vertices.hashCode();
	}

	/**
	 * @param g1
	 * @return true if this graph has the same set of edges (by equals()) of g1. false otherwise.
	 */
	public boolean hasSameEdgesOf(final LabeledIntGraph g1) {
		final StringBuffer sb = new StringBuffer("Different edges:");
		if (g1 == null) return false;

		LabeledIntEdge e;
		boolean sameEdges = true;
		for (final LabeledIntEdge e1 : g1.edges.keySet()) {
			e = this.getEdge(e1.getName());
			if ((e == null) || !e.equalsLabeledValues(e1)) {
				sb.append("\nCurrent  edge: " + e + "\n"
						+ "Modified edge: " + e1);
				sameEdges = false;// i want to log all differences!!!
			}
		}
		if (LOG.isLoggable(Level.INFO)) LabeledIntGraph.LOG.log(Level.INFO, sb.toString());
		return sameEdges;
	}

	/**
	 * @return the optimize
	 */
	public boolean isOptimize() {
		return this.optimize;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @param z the z to set
	 */
	public void setZ(final LabeledNode z) {
		this.Z = z;
	}

	/**
	 * Takes in all internal structures of g.<br>
	 * This method is useful to copy the references of internal data structure of the given graph 'g' into the current.
	 * It is not a clone because even if g!=this, all internal data structures are the same.
	 *
	 * @param g the graph to copy.
	 */
	public void takeIn(final LabeledIntGraph g) {
		this.name = g.name;
		this.optimize = g.optimize;
		this.vertices = g.vertices;
		this.edges = g.edges;
		this.lowerCaseEdgesSet = g.lowerCaseEdgesSet;
		this.observedProposition = g.observedProposition;
		this.Z = g.Z;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(
				"%LabeledIntGraph: "
						+ this.name
						+ "\n%LabeledIntGraph Syntax\n"
						+ "%LabeledNode: <name, label, proposition observed>\n"
						+ "%LabeledIntEdge: <name, type, source node, dest. node, L:{labeled values}, LL:{lower case labeled values}, UL:{upper case labeled values}>\n");
		sb.append("Nodes:\n");

		for (final LabeledNode n : this.getVertices()) {
			sb.append("<" + n.getName() + ",\t" + n.getLabel() + ",\t" + n.getPropositionObserved() + ">\n");
		}
		sb.append("Edges:\n");
		for (final LabeledIntEdge e : this.getEdges()) {
			sb.append("<" + e.getName() + ",\t" + e.getType() + ",\t" + this.getSource(e).getName() + ",\t"
					+ this.getDest(e).getName() + ",\tL:" + e.getLabeledValueMap().toString() + ", LL:"
					+ e.lowerLabelsToString()
					+ ", UL:" + e.upperLabelsToString() + ">\n");
		}
		return sb.toString();
	}
}
