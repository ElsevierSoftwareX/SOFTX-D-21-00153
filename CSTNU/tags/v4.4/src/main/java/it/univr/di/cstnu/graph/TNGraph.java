// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * Bear in mind that ObjectRBTreeSet is usually a little bit more faster than ObjectAVLTreeSet on set of medium size. In very large set size, ObjectAVLTreeSet
 * shine!
 * On small set, ArraySet is more efficient in time and space.
 */
package it.univr.di.cstnu.graph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.jung.graph.AbstractTypedGraph;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap;
import it.unimi.dsi.fastutil.chars.Char2ObjectMap;
import it.unimi.dsi.fastutil.chars.CharSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

/**
 * Represents (dense) temporal network graphs where nodes are {@link it.univr.di.cstnu.graph.LabeledNode} and edges are (an extension of)
 * {@link it.univr.di.cstnu.graph.Edge}.
 * This class implements the interface {@link edu.uci.ics.jung.graph.DirectedGraph} in order to allow the representation of Graph by Jung library.
 *
 * @author posenato
 * @version $Id: $Id
 * @param <E> type of edge
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SE_BAD_FIELD", justification = "I know what I'm doing")
public class TNGraph<E extends Edge> extends AbstractTypedGraph<LabeledNode, E> implements DirectedGraph<LabeledNode, E>, PropertyChangeListener {

	/**
	 * Types of network that can be represented by this class.
	 * The type is determined in the constructor checking the implemented interface by the given edge class.<br>
	 * On 2019-06-13 the considered interfaces are:
	 * 
	 * <pre>
	 * Edge Interface      Network Type
	 * STNEdge             STN
	 * STNUEdge            STNU
	 * CSTNEdge            CSTN
	 * CSTNUEdge           CSTNU
	 * CSTNPSUEdge		   CSTPSU
	 * </pre>
	 * 
	 * <b>This is not a correct design-choice but it allows the written of classes that can use TNGraph&lt;Edge&gt; objects and make only different operations
	 * according with the type of the network.</b>
	 * 
	 * @author posenato
	 */
	public enum NetworkType {
		/**
		 * 
		 */
		STN,

		/**
		 * 
		 */
		STNU,

		/**
		 * 
		 */
		CSTN,

		/**
		 * 
		 */
		CSTNU,

		/**
		 * 
		 */
		CSTNPSU
	}

	/**
	 * Represents the association of an edge with its position in the adjacency matrix of the graph.
	 * index with its corresponding node.
	 * 
	 * @author posenato
	 */
	private class EdgeIndex {
		/**
		 * It is not possible to use the technique used for Node (extending LabeledIntNode class) because if I extended
		 * E, then edges are viewed as E and parameterized type T cannot be used as base class for extending.
		 */

		/**
		 * 
		 */
		int colAdj = -1;
		/**
		 * 
		 */
		E edge = null;
		/**
		 * 
		 */
		int rowAdj = -1;

		/**
		 * @param e edge
		 * @param row row 
		 * @param col col
		 */
		EdgeIndex(E e, int row, int col) {
			this.edge = e;
			this.rowAdj = row;
			this.colAdj = col;
		}

		@Override
		public String toString() {
			return String.format("%s->(%dX%d)", this.edge.getName(), this.rowAdj, this.colAdj);
		}
	}

	/**
	 * Adjacency grow factor; It represents the multiplication factor to use for increasing the dimension of adjacency matrix. It has to be at least 1.5.
	 */
	static final float growFactor = 1.8f;

	/**
	 *
	 */
	private static Logger LOG = Logger.getLogger("it.univr.di.cstnu.graph.TNGraph");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @return an instance for childrenOfObserver field.
	 */
	private final static Object2ObjectMap<LabeledNode, Label> newChildrenObserverInstance() {
		return new Object2ObjectArrayMap<>();// in Label I showed that for small map, ArrayMap is faster than Object2ObjectRBTreeMap and
												// Object2ObjectAVLTreeMap.
	}

	/**
	 * @return an instance for propositionToNode field.
	 */
	private final static Char2ObjectMap<LabeledNode> newProposition2NodeInstance() {
		Char2ObjectMap<LabeledNode> map = new Char2ObjectArrayMap<>();
		map.defaultReturnValue(null);
		return map;// I verified that Char2ObjectArrayMap is faster than Openhash when proposition are limited, as in this application.
	}

	/**
	 * The graph is represented by its adjacency matrix.
	 */
	private E[][] adjacency;

	/**
	 * Alphabet for A-Label
	 */
	private ALabelAlphabet aLabelAlphabet;

	/**
	 * Children of observation nodes
	 */
	private Map<LabeledNode, Label> childrenOfObserver;

	/**
	 * Map (edge--&gt;adjacency position)
	 */
	private Object2ObjectMap<String, EdgeIndex> edge2index;

	/**
	 * Edge factory
	 */
	private EdgeSupplier<E> edgeFactory;

	/**
	 * Map (adjacency row--&gt;node)
	 */
	private Int2ObjectMap<LabeledNode> index2node;

	/**
	 * A possible input file containing this graph.
	 */
	private File inputFile;

	/**
	 * List of edges with lower case label set not empty
	 */
	private ObjectList<BasicCSTNUEdge> lowerCaseEdges;

	/**
	 * Name
	 */
	private String name;

	/**
	 * Node factory
	 */
	private LabeledNodeSupplier nodeFactory;

	/**
	 * Map (node--&gt;adjacency row)
	 */
	private Object2IntMap<String> nodeName2index;

	/**
	 * List of edges from observers to Z
	 */
	private ObjectList<E> observer2Z;

	/**
	 * Current number of nodes;
	 */
	private int order;

	/**
	 * Map of (proposition--&gt;Observer node).
	 */
	private Char2ObjectMap<LabeledNode> proposition2Observer;

	/**
	 * Type of network
	 */
	private NetworkType type;

	/**
	 * Zero node. In temporal constraint network such node is the first node to execute.
	 */
	private LabeledNode Z;

	/**
	 * Creates a new object using inputEdgeImplClass class for representing the edges of the graph.
	 * @param <E1> type of edge
	 *
	 * @param inputEdgeImplClass a {@link java.lang.Class} object.
	 */
	public <E1 extends E> TNGraph(Class<E1> inputEdgeImplClass) {// , Class<M1> inputLabeledValueMapImplClass
		super(EdgeType.DIRECTED);
		if (CSTNPSUEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.type = NetworkType.CSTNPSU;
		else if (CSTNUEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.type = NetworkType.CSTNU;
		else if (CSTNEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.type = NetworkType.CSTN;
		else if (STNUEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.type = NetworkType.STNU;
		else if (STNEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.type = NetworkType.STN;

		this.edgeFactory = new EdgeSupplier<>(inputEdgeImplClass);// , inputLabeledValueMapImplClass
		this.nodeFactory = new LabeledNodeSupplier();// inputLabeledValueMapImplClass
		this.order = 0;
		this.adjacency = createAdjacency(10);
		this.nodeName2index = new Object2IntOpenHashMap<>();
		this.nodeName2index.defaultReturnValue(Constants.INT_NULL);
		this.index2node = new Int2ObjectOpenHashMap<>();
		this.edge2index = new Object2ObjectAVLTreeMap<>(); // From fastutil javadoc:
		// In general, AVL trees have slightly slower updates but faster searches; however, on very large collections the smaller height may lead in fact to
		// faster updates, too.
		if (BasicCSTNUEdge.class.isAssignableFrom(inputEdgeImplClass) || STNUEdge.class.isAssignableFrom(inputEdgeImplClass))
			this.aLabelAlphabet = new ALabelAlphabet();
	}

	/**
	 * Creates a new object using inputEdgeImplClass class for representing the edges of the graph.
	 * @param <E1> type of edge
	 *
	 * @param edgeImplClass a {@link java.lang.Class} object.
	 * @param alphabet Alphabet to use for naming Upper Case label
	 */
	public <E1 extends E> TNGraph(Class<E1> edgeImplClass, ALabelAlphabet alphabet) {
		this(edgeImplClass);
		if (alphabet != null) {
			this.aLabelAlphabet = alphabet;
		}
	}

	/**
	 * A constructor that copy a given graph g using copy constructor even for internal structures. If g is null, this new graph will be empty.
	 * @param <E1> type of edge
	 *
	 * @param g the graph to be cloned
	 * @param edgeImplClass class
	 */
	public <E1 extends E> TNGraph(final TNGraph<E> g, Class<E1> edgeImplClass) {
		this(edgeImplClass);
		this.name = g.name;
		this.aLabelAlphabet = g.aLabelAlphabet;
		this.inputFile = g.inputFile;
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVertices()) {
			vNew = this.nodeFactory.get(v);
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}
		vNew = g.getZ();
		if (vNew != null) {
			this.setZ(this.getNode(vNew.getName()));
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		E eNew;
		for (final E e : g.getEdges()) {
			eNew = this.edgeFactory.get(e);
			addEdge(eNew, g.getSource(e.getName()).name, g.getDest(e.getName()).name);
		}
	}

	/**
	 * Constructor for TNGraph.
	 * @param <E1> type of edge
	 *
	 * @param graphName a name for the graph
	 * @param inputEdgeImplClass type of edges
	 */
	public <E1 extends E> TNGraph(final String graphName, Class<E1> inputEdgeImplClass) {
		this(inputEdgeImplClass);
		this.name = graphName;
	}

	/**
	 * Constructor for TNGraph.
	 * @param <E1> type of edge
	 *
	 * @param graphName a name for the graph
	 * @param inputEdgeImplClass type of edges
	 * @param alphabet alphabet for upper case letter used to label values in the edges.
	 */
	public <E1 extends E> TNGraph(final String graphName, Class<E1> inputEdgeImplClass, ALabelAlphabet alphabet) {
		this(inputEdgeImplClass);
		this.name = graphName;
		this.aLabelAlphabet = alphabet;
	}

	/**
	 * Add child to obs.
	 * It is user responsibility to assure that 'child' is a children in CSTN sense of 'obs'.
	 * No validity check is made by the method.
	 *
	 * @param obs a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @param child a char.
	 */
	public void addChildToObserverNode(LabeledNode obs, char child) {
		if (this.childrenOfObserver == null)
			this.childrenOfObserver = newChildrenObserverInstance();
		Label children = this.childrenOfObserver.get(obs);
		if (children == null) {
			children = Label.emptyLabel;
		}
		children = children.conjunction(child, Literal.STRAIGHT);
		this.childrenOfObserver.put(obs, children);
	}

	/*
	 * It is necessary to copy the general method here because of generics T
	 */
	/** {@inheritDoc} */
	@Override
	public boolean addEdge(E e, final LabeledNode v1, final LabeledNode v2) {
		if (e == null || v1 == null || v2 == null)
			return false;
		if (!this.nodeName2index.containsKey(v1.getName())) {
			addVertex(v1);
		}
		if (!this.nodeName2index.containsKey(v2.getName())) {
			addVertex(v2);
		}
		return addEdge(e, v1.name, v2.name);
	}

	/*
	 * It is necessary to copy the general method here because otherwise it calls the general addEdge(e, new Pair<LabeledNode>(v1, v2), edge_type)!!!
	 * @see edu.uci.ics.jung.graph.AbstractGraph#addEdge(java.lang.Object, java.lang.Object, java.lang.Object, edu.uci.ics.jung.graph.util.EdgeType)
	 */
	/** {@inheritDoc} */
	@Override
	public boolean addEdge(E e, final LabeledNode v1, final LabeledNode v2, final EdgeType edge_type1) {
		if (e == null || v1 == null || v2 == null)
			return false;

		return addEdge(e, v1.name, v2.name);
	}

	/** {@inheritDoc} */
	@Override
	public boolean addEdge(E edge, Pair<? extends LabeledNode> endpoints, EdgeType edgeType) {
		if (edge == null || endpoints == null)
			return false;
		return this.addEdge(edge, endpoints.getFirst(), endpoints.getSecond());
	}

	/**
	 * Optimized method for adding edge. It exploits internal structure of the class.
	 *
	 * @param e not null
	 * @param v1Name not null
	 * @param v2Name not null
	 * @return true if edge has been added, false otherwise.
	 */
	public boolean addEdge(E e, final String v1Name, final String v2Name) {
		if (e == null || v1Name == null || v2Name == null) {
			LOG.severe("A parameter is null: " + e + ", " + v1Name + ", " + v2Name);
			return false;
		}
		if (this.edge2index.containsKey(e.getName())) {
			LOG.severe("An edge with name " + e.getName() + " already exists. The new edge cannot be added.");
			return false;
		}
		int sourceIndex = this.nodeName2index.getInt(v1Name);
		if (sourceIndex == Constants.INT_NULL) {
			LOG.severe("Source node during adding edge with name " + e.getName() + " is null. The new edge cannot be added.");
			return false;
		}

		int destIndex = this.nodeName2index.getInt(v2Name);
		if (destIndex == Constants.INT_NULL) {
			LOG.severe("Destination node during adding edge with name " + e.getName() + " is null. The new edge cannot be added.");
			return false;
		}

		E old = this.adjacency[sourceIndex][destIndex];
		if (old != null) {
			LOG.severe("Between node " + v1Name + " and node " + v2Name + " there exists the edge " + old + ". Remove it before adding a new one.");
			return false;
		}
		// removeEdgeFromIndex(old);
		this.adjacency[sourceIndex][destIndex] = e;

		this.edge2index.put(e.getName(), new EdgeIndex(e, sourceIndex, destIndex));
		this.lowerCaseEdges = null;
		((AbstractEdge) e).addObserver("edgeType", this);
		((AbstractEdge) e).addObserver("edgeName", this);
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public boolean addVertex(LabeledNode vertex) {
		if (vertex == null
				|| this.nodeName2index.containsKey(vertex.name)
				|| (vertex.getPropositionObserved() != Constants.UNKNOWN && this.getObserver(vertex.getPropositionObserved()) != null)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.info("The new vertex for adding is null or is already present or contains a proposition already present.");
				}
			}
			return false;
		}

		int currentSize = this.adjacency.length;
		if (currentSize == this.order) {
			int newSize = (int) (currentSize * growFactor);
			E[][] newAdjancency = createAdjacency(newSize);
			for (int i = currentSize; i-- != 0;) {
				for (int j = currentSize; j-- != 0;)
					newAdjancency[i][j] = this.adjacency[i][j];
			}
			this.adjacency = newAdjancency;
		}
		// now it is possible to add node in position 'order'
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Adding node " + vertex);
			}
		}
		this.nodeName2index.put(vertex.name, this.order);
		this.index2node.put(this.order, vertex);
		this.order++;
		clearCache();
		vertex.addObserver("nodeName", this);
		vertex.addObserver("nodeLabel", this);
		vertex.addObserver("nodeProposition", this);
		return true;
	}

	/**
	 * clear all internal structures.
	 * The graph will be empty.
	 */
	public void clear() {
		clear(10);
	}

	/**
	 * Clear all internal structures.
	 * The graph will be erased.
	 *
	 * @param initialAdjSize initial number of vertices.
	 */
	public void clear(int initialAdjSize) {
		this.order = 0;// addVertex adjusts the value
		this.adjacency = createAdjacency(initialAdjSize);
		this.nodeName2index.clear();
		this.index2node.clear();
		this.edge2index.clear();
		this.clearCache();
	}

	/**
	 * Clear all internal caches.
	 * <p>
	 * Caches are automatically created during any modification or query about the graph structure.
	 */
	public void clearCache() {
		this.lowerCaseEdges = null;
		this.proposition2Observer = null;
		this.childrenOfObserver = null;
		this.observer2Z = null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsEdge(E edge) {
		if (edge == null || edge.getName() == null)
			return false;
		return this.edge2index.containsKey(edge.getName());
	}

	/** {@inheritDoc} */
	@Override
	public boolean containsVertex(LabeledNode vertex) {
		if (vertex == null || vertex.name == null)
			return false;
		return this.nodeName2index.containsKey(vertex.name);
	}

	/**
	 * Defensive copy of all internal structures of g into this.<br>
	 * This method is useful to copy a graph into the current without modifying the reference to the current. 'g' internal structures are copied as they are.
	 *
	 * @param g the graph to copy.
	 */
	public void copy(final TNGraph<E> g) {
		this.name = g.name;
		this.order = 0;// addVertex adjusts the value
		this.adjacency = createAdjacency(g.getVertexCount());
		this.nodeName2index.clear();
		this.index2node.clear();
		this.edge2index.clear();
		this.clearCache();
		// Clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVertices()) {
			vNew = this.nodeFactory.get(v);
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}

		// Clone all edges giving the right new endpoints corresponding the old ones.
		for (final E e : g.getEdges()) {
			addEdge(this.edgeFactory.get(e), g.getSource(e).name, g.getDest(e).name);
		}
	}

	/**
	 * Makes a copy as {@link #copy(TNGraph)} removing all labeled values having unknown literal(s) or -∞ value.
	 *
	 * @param g a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 */
	public void copyCleaningRedundantLabels(TNGraph<E> g) {
		this.name = g.name;
		this.order = 0;// addVertex adjusts the value
		this.adjacency = createAdjacency(g.getVertexCount());
		this.nodeName2index.clear();
		this.index2node.clear();
		this.edge2index.clear();
		this.clearCache();
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : g.getVertices()) {
			vNew = this.nodeFactory.get(v);
			for (Label label : vNew.getLabeledPotential().keySet()) {
				if (label.containsUnknown())
					vNew.removeLabeledPotential(label);
			}
			this.addVertex(vNew);
			if (v.equalsByName(g.Z)) {
				this.Z = vNew;
			}
		}

		// clone all edges giving the right new endpoints corresponding the old ones.
		E eNew;
		int value;
		Label label;
		for (final E e : g.getEdges()) {
			if (e.isEmpty())
				continue;
			eNew = this.edgeFactory.get(e.getName());// I don't copy values!
			eNew.setConstraintType(e.getConstraintType());
			if (e.isSTNEdge()) {
				((STNEdge) eNew).setValue(((STNEdge) e).getValue());
				addEdge(eNew, g.getSource(e).getName(), g.getDest(e).getName());
				continue;
			}
			if (e.isCSTNEdge()) {
				for (Object2IntMap.Entry<Label> entry : ((CSTNEdge) e).getLabeledValueSet()) {
					value = entry.getIntValue();
					if (value == Constants.INT_NEG_INFINITE)
						continue;
					label = entry.getKey();
					if (label.containsUnknown())
						continue;
					((CSTNEdge) eNew).mergeLabeledValue(entry.getKey(), value);
				}
			}
			if (BasicCSTNUEdge.class.isAssignableFrom(e.getClass())) {
				for (ALabel alabel : ((BasicCSTNUEdge) e).getUpperCaseValueMap().keySet()) {
					for (Object2IntMap.Entry<Label> entry1 : ((BasicCSTNUEdge) e).getUpperCaseValueMap().get(alabel).entrySet()) {// entrySet read-only
						value = entry1.getIntValue();
						if (value == Constants.INT_NEG_INFINITE)
							continue;
						label = entry1.getKey();
						if (label.containsUnknown())
							continue;
						((BasicCSTNUEdge) eNew).mergeUpperCaseValue(entry1.getKey(), alabel, entry1.getIntValue());
					}
				}
				if (e.isCSTNUEdge()) {
					// lower case value
					((CSTNUEdge) eNew).setLowerCaseValue(((CSTNUEdge) e).getLowerCaseValue());
				}
				if (e.isCSTNPSUEdge()) {
					// lower case value
					((CSTNPSUEdge) eNew).setLowerCaseValue(((CSTNPSUEdge) e).getLowerCaseValueMap());
				}
			}
			if (eNew.isEmpty())
				continue;
			addEdge(eNew, g.getSource(e).getName(), g.getDest(e).getName());
		}
	}

	/**
	 * {@inheritDoc} Equals based on equals of edges and vertices.
	 */
	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TNGraph))
			return false;
		@SuppressWarnings("unchecked")
		final TNGraph<E> g1 = (TNGraph<E>) o;
		return this.hasSameEdgesOf(g1) && this.hasSameVerticesOf(g1);
	}

	/** {@inheritDoc} */
	@Override
	public E findEdge(LabeledNode s, LabeledNode d) {
		if (s == null || s.getName() == null || d == null || d.getName() == null)
			return null;
		return findEdge(s.getName(), d.getName());
	}

	/**
	 * Find the edge given the name of source node and destination one.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @param d a {@link java.lang.String} object.
	 * @return null if any parameter is null or there not exists at least one of two nodes or the edge does not exist.
	 */
	public E findEdge(String s, String d) {
		if (s == null || d == null)
			return null;
		int sourceNI = this.nodeName2index.getInt(s);
		if (sourceNI == Constants.INT_NULL)
			return null;
		int destNI = this.nodeName2index.getInt(d);
		if (destNI == Constants.INT_NULL)
			return null;
		return this.adjacency[sourceNI][destNI];
	}

	/**
	 * <p>
	 * Getter for the field <code>aLabelAlphabet</code>.
	 * </p>
	 *
	 * @return the aLabelAlphabet
	 */
	public ALabelAlphabet getALabelAlphabet() {
		return this.aLabelAlphabet;
	}

	/**
	 * Given a observation node <code>obs</code> that observes the proposition 'p', its 'children' are all observation nodes, Q, for which 'p' appears in the
	 * label of node Q.
	 * <p>
	 * This method returns the set of children of a given node as a <b>label of straight propositions</b> associated to the children instead of a <b>set of
	 * children nodes</b>.
	 *
	 * @param obs a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 * @return the set of children of observation node <code>obs</code>, null if there is no children.
	 */
	public Label getChildrenOf(LabeledNode obs) {
		if (obs == null)
			return null;

		// The soundness of this method is based on the property that the observed proposition of an observation node is represented as a straight literal.
		if (this.childrenOfObserver == null) {
			// Build the cache map of childrenOfObserver
			this.childrenOfObserver = newChildrenObserverInstance();

			for (Char2ObjectMap.Entry<LabeledNode> entryObservedObserverNode : this.getObservedAndObserver().char2ObjectEntrySet()) {
				char observedProposition = entryObservedObserverNode.getCharKey();
				LabeledNode observator = entryObservedObserverNode.getValue();
				Label observatorLabel = observator.getLabel();
				for (char propInObsLabel : observatorLabel.getPropositions()) {
					LabeledNode father = this.getObserver(propInObsLabel);// for the well property, father must exist!
					Label children = this.childrenOfObserver.get(father);
					if (children == null) {
						children = Label.emptyLabel;
					}
					children = children.conjunction(observedProposition, Literal.STRAIGHT);
					this.childrenOfObserver.put(father, children);
				}
			}
		}
		return this.childrenOfObserver.get(obs);
	}

	/**
	 * <p>
	 * getContingentNodeCount.
	 * </p>
	 *
	 * @return the number of contingent nodes.
	 */
	public int getContingentNodeCount() {
		int c = 0;
		for (E e : this.getEdges()) {
			if (e.isContingentEdge())
				c++;
		}
		return c / 2;
	}

	/** {@inheritDoc} */
	@Override
	public LabeledNode getDest(E directedEdge) {
		EdgeIndex ei = this.edge2index.get(directedEdge.getName());
		if (ei == null)
			return null;

		return this.index2node.get(ei.colAdj);
	}

	/**
	 * Wrapper {@link #getDest(Edge)}
	 *
	 * @param edgeName a {@link java.lang.String} object.
	 * @return a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 */
	public LabeledNode getDest(String edgeName) {
		if (edgeName == null)
			return null;

		EdgeIndex ei = this.edge2index.get(edgeName);
		if (ei == null)
			return null;

		return this.index2node.get(ei.colAdj);
	}

	/**
	 * Returns the edge associated to the name.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return the edge associated to the name.
	 */
	public E getEdge(final String s) {
		if ((s == null) || s.isEmpty())
			return null;
		EdgeIndex ei = this.edge2index.get(s);
		if (ei == null)
			return null;
		return ei.edge;
	}

	/** {@inheritDoc} */
	@Override
	public int getEdgeCount() {
		return this.edge2index.size();
	}

	/**
	 * <p>
	 * Getter for the field <code>edgeFactory</code>.
	 * </p>
	 *
	 * @return the edgeFactory
	 */
	public EdgeSupplier<E> getEdgeFactory() {
		return this.edgeFactory;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<E> getEdges() {
		ObjectArrayList<E> coll = new ObjectArrayList<>();
		for (EdgeIndex ei : this.edge2index.values()) {
			coll.add(ei.edge);
		}
		return coll;
		// ObjectCollection<?> tmp = edge2index.values();
		// return (Collection<T>) tmp;
	}

	/**
	 * {@inheritDoc}
	 * getEdgesArray.
	 */

	@Override
	public Pair<LabeledNode> getEndpoints(E edge) {
		if (edge == null)
			return null;
		EdgeIndex ei = this.edge2index.get(edge.getName());
		if (ei == null)
			return null;

		return new Pair<>(this.index2node.get(ei.rowAdj), this.index2node.get(ei.colAdj));
	}

	/**
	 * <p>
	 * getFileName.
	 * </p>
	 *
	 * @return the name of the file that contains this graph.
	 */
	public File getFileName() {
		return this.inputFile;
	}

	/** {@inheritDoc} */
	@Override
	public ObjectList<E> getIncidentEdges(LabeledNode vertex) {
		int index;
		ObjectArrayList<E> coll = new ObjectArrayList<>();
		if (vertex == null || (index = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return coll;

		E e;
		for (int i = 0; i < this.order; i++) {
			e = this.adjacency[index][i];
			if (e != null)
				coll.add(e);
			if (i == index)
				continue;
			e = this.adjacency[i][index];
			if (e != null)
				coll.add(e);
		}
		return coll;

	}

	/** {@inheritDoc} */
	@Override
	public Collection<E> getInEdges(LabeledNode vertex) {
		int nodeIndex;
		ObjectArrayList<E> inEdges = new ObjectArrayList<>();
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return inEdges;
		E e;
		for (int i = 0; i < this.order; i++) {
			e = this.adjacency[i][nodeIndex];
			if (e != null)
				inEdges.add(e);
		}
		return inEdges;
	}

	/**
	 * @return the edgeImplementationClass
	 *         public Class<? extends LabeledIntMap> getLabeledValueMapImplClass() {
	 *         return this.edgeFactory.getLabeledIntValueMapImplClass();
	 *         }
	 */

	/**
	 * @return the edgeImplementationClass
	 */
	public Class<? extends E> getEdgeImplClass() {
		return this.edgeFactory.getEdgeImplClass();
	}

	/**
	 * <p>
	 * getLowerLabeledEdges.
	 * </p>
	 *
	 * @return the set of edges containing Lower Case Labels (when type of network is CSTNU/CSTPSU). If there is no such edges, it returns an empty list.
	 */
	public ObjectList<BasicCSTNUEdge> getLowerLabeledEdges() {
		if (this.lowerCaseEdges == null) {
			this.lowerCaseEdges = new ObjectArrayList<>();
			if (getType() == NetworkType.CSTNU || getType() == NetworkType.CSTNPSU) {
				BasicCSTNUEdge edge;
				for (int i = 0; i < this.order; i++) {
					for (int j = 0; j < this.order; j++) {
						if ((edge = (BasicCSTNUEdge) this.adjacency[i][j]) != null && edge.lowerCaseValueSize() == 1 && edge.isContingentEdge()) {
							this.lowerCaseEdges.add(edge);
						}
					}
				}
			}
		}
		return this.lowerCaseEdges;
	}

	/**
	 * Getter for the field <code>name</code>.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<LabeledNode> getNeighbors(LabeledNode vertex) {
		int nodeIndex;
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return null;
		ObjectArraySet<LabeledNode> neighbors = new ObjectArraySet<>();
		for (int i = 0; i < this.order; i++) {
			if (this.adjacency[nodeIndex][i] != null)
				neighbors.add(this.index2node.get(i));
			if (this.adjacency[i][nodeIndex] != null)
				neighbors.add(this.index2node.get(i));
		}
		return neighbors;
	}

	/**
	 * Returns the node associated to the name.
	 *
	 * @param s a {@link java.lang.String} object.
	 * @return the node associated to the name if present, null otherwise.
	 */
	public LabeledNode getNode(final String s) {
		if ((s == null) || s.isEmpty())
			return null;
		return this.index2node.get(this.nodeName2index.getInt(s));
	}

	/**
	 * <p>
	 * Getter for the field <code>nodeFactory</code>.
	 * </p>
	 *
	 * @return the nodeFactory
	 */
	public LabeledNodeSupplier getNodeFactory() {
		return this.nodeFactory;
	}

	/**
	 * getNodes.
	 *
	 * @return return the set of node ordered w.r.t. the lexicographical order of their names.
	 */
	public Collection<LabeledNode> getNodes() {
		return this.getVertices();
	}

	/**
	 * <p>getObservedAndObserver.</p>
	 *
	 * @return the map of propositions and their observers (nodes). If there is no observer node, it returns an empty map.
	 */
	public Char2ObjectMap<LabeledNode> getObservedAndObserver() {
		if (this.proposition2Observer == null) {
			this.proposition2Observer = newProposition2NodeInstance();
			char proposition;
			for (final LabeledNode n : getVertices()) {
				if ((proposition = n.getPropositionObserved()) != Constants.UNKNOWN) {
					if (this.proposition2Observer.put(proposition, n) != null) {
						throw new IllegalStateException("There is two observer nodes for the same proposition " + proposition);
					}
				}
			}
		}
		return this.proposition2Observer;
	}

	/**
	 * <p>getObserver.</p>
	 *
	 * @param c the proposition
	 * @return the node that observes the proposition if it exists, null otherwise.
	 */
	public LabeledNode getObserver(final char c) {
		final Char2ObjectMap<LabeledNode> observer = this.getObservedAndObserver();
		if (observer == null)
			return null;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				TNGraph.LOG.finest("Propositione=" + c + "; observer=" + observer);
		}
		return observer.get(c);

	}

	/**
	 * Be careful! The returned value is not a copy as the edges contained!
	 *
	 * @return the set of edges from observers to Z if Z is defined, empty set otherwise.
	 */
	public ObjectList<E> getObserver2ZEdges() {
		if (this.observer2Z == null) {
			this.buildObserver2ZEdgesSet();
		}
		return this.observer2Z;
	}

	/**
	 * <p>
	 * getObserverCount.
	 * </p>
	 *
	 * @return the number of observers.
	 */
	public int getObserverCount() {
		return this.getObservers().size();
	}

	/**
	 * <p>
	 * getObservers.
	 * </p>
	 *
	 * @return the set of observator time-points.
	 */
	public Collection<LabeledNode> getObservers() {
		if (this.proposition2Observer == null) {
			getObservedAndObserver();
		}
		return this.proposition2Observer.values();
	}

	/** {@inheritDoc} */
	@Override
	public ObjectList<E> getOutEdges(LabeledNode vertex) {
		int nodeIndex;
		ObjectArrayList<E> outEdges = new ObjectArrayList<>();
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return outEdges;
		E e;
		for (int i = this.order; --i >= 0;) {
			e = this.adjacency[nodeIndex][i];
			if (e != null)
				outEdges.add(e);
		}
		return outEdges;
	}

	/** {@inheritDoc} */
	@Override
	public int outDegree(LabeledNode vertex) {
		int nodeIndex;
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return Constants.INT_NULL;
		int count = 0;
		for (int i = this.order; --i >= 0;) {
			if (this.adjacency[nodeIndex][i] != null)
				count++;
		}
		return count;
	}

	/** {@inheritDoc} */
	@Override
	public int inDegree(LabeledNode vertex) {
		int nodeIndex;
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return Constants.INT_NULL;
		int count = 0;
		for (int i = this.order; --i >= 0;) {
			if (this.adjacency[i][nodeIndex] != null)
				count++;
		}
		return count;
	}

	/** {@inheritDoc} */
	@Override
	public int degree(LabeledNode vertex) {
		int nodeIndex;
		if (vertex == null || (nodeIndex = this.nodeName2index.getInt(vertex.name)) == Constants.INT_NULL)
			return Constants.INT_NULL;
		int count = 0;
		for (int i = this.order; --i >= 0;) {
			if (this.adjacency[nodeIndex][i] != null)
				count++;
			if (this.adjacency[i][nodeIndex] != null)
				count++;
		}
		return count;
	}

	/** {@inheritDoc} */
	@Override
	public Collection<LabeledNode> getPredecessors(LabeledNode vertex) {
		ObjectArrayList<LabeledNode> predecessor = new ObjectArrayList<>();
		for (E e : this.getInEdges(vertex)) {
			predecessor.add(this.getSource(e));
		}
		return predecessor;
	}

	/**
	 * getPropositions.
	 *
	 * @return the set of propositions of the graph.
	 */
	public CharSet getPropositions() {
		if (this.proposition2Observer == null) {
			this.getObservedAndObserver();
		}
		return this.proposition2Observer.keySet();
	}

	/** {@inheritDoc} */
	@Override
	public LabeledNode getSource(E edge) {
		if (edge == null)
			return null;
		return getSource(edge.getName());
	}

	/**
	 * Wrapper of {@link #getSource(Edge)}
	 *
	 * @param edgeName a {@link java.lang.String} object.
	 * @return a {@link it.univr.di.cstnu.graph.LabeledNode} object.
	 */
	public LabeledNode getSource(String edgeName) {
		if (edgeName == null)
			return null;

		EdgeIndex ei = this.edge2index.get(edgeName);
		if (ei == null)
			return null;

		return this.index2node.get(ei.rowAdj);
	}

	/** {@inheritDoc} */
	@Override
	public Collection<LabeledNode> getSuccessors(LabeledNode vertex) {
		ObjectArrayList<LabeledNode> successors = new ObjectArrayList<>();
		for (E e : this.getOutEdges(vertex)) {
			successors.add(this.getDest(e));
		}
		return successors;
	}

	/**
	 * <p>
	 * Getter for the field <code>type</code>.
	 * </p>
	 *
	 * @return the type of network
	 */
	public NetworkType getType() {
		return this.type;
	}

	/**
	 * <p>
	 * getUpperLabeledEdges.
	 * </p>
	 *
	 * @return the set of edges containing Upper Case Label only for TNGraphs that contain BasicCSTNEdge or derived.
	 */
	public Set<BasicCSTNUEdge> getUpperLabeledEdges() {
		final ObjectArraySet<BasicCSTNUEdge> es1 = new ObjectArraySet<>();
		for (final E e : this.getEdges()) {
			BasicCSTNUEdge e1 = ((BasicCSTNUEdge) e);
			if (e1.upperCaseValueSize() > 0) {
				es1.add(e1);
			}
		}
		return es1;
	}

	/** {@inheritDoc} */
	@Override
	public int getVertexCount() {
		return this.nodeName2index.size();
	}

	/** {@inheritDoc} */
	@Override
	public Collection<LabeledNode> getVertices() {
		return this.index2node.values();
	}

	/**
	 * getVerticesArray.
	 *
	 * @return the set of vertices as an array ordered w.r.t the name of node in ascending order.
	 */
	public LabeledNode[] getVerticesArray() {
		final LabeledNode[] nodes = this.getVertices().toArray(new LabeledNode[0]);
		Arrays.sort(nodes);
		return nodes;
	}

	/**
	 * <p>
	 * getZ.
	 * </p>
	 *
	 * @return the Z node
	 */
	public LabeledNode getZ() {
		return this.Z;
	}

	/**
	 * {@inheritDoc} Since equals has been specialized, hashCode too.
	 */
	@Override
	public int hashCode() {
		return this.getEdges().hashCode() + 31 * this.getVertices().hashCode();
	}

	/**
	 * hasSameEdgesOf.
	 *
	 * @param g1 a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 * @return true if this graph contains edges equal to g1 edges w.r.t. their name and their values. The check DOES NOT USE edge {@link #equals(Object)}.
	 *         False, otherwise.
	 */
	public boolean hasSameEdgesOf(final TNGraph<E> g1) {
		if (g1 == null)
			return false;
		final StringBuffer sb = new StringBuffer("Different edges:");
		final String currentName = this.name;
		final String g1name = g1.name;

		boolean sameEdges = true;
		ObjectSet<E> allEdges = new ObjectAVLTreeSet<>(getEdges());
		allEdges.addAll(g1.getEdges());
		E eg, eg1;
		for (E e : allEdges) {
			eg = getEdge(e.getName());
			eg1 = g1.getEdge(e.getName());
			if (eg == null || eg1 == null || !eg.hasSameValues(eg1)) {
				sb.append('\n').append(currentName).append(":\t").append(eg).append("\n").append(g1name).append(":\t").append(eg1);
				sameEdges = false;// i want to log all differences!!!
			}
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				TNGraph.LOG.log(Level.FINE, sb.toString());
		}
		return sameEdges;
	}

	/**
	 * <p>
	 * hasSameVerticesOf.
	 * </p>
	 *
	 * @param g1 a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 * @return true if this graph contains vertices equal to g1 vertices w.r.t. their name and their values. The check DOES NOT USE edge
	 *         {@link #equals(Object)}.
	 *         False, otherwise.
	 */
	public boolean hasSameVerticesOf(final TNGraph<E> g1) {
		if (g1 == null)
			return false;
		boolean sameNodes = true;
		ObjectSet<LabeledNode> allNodes = new ObjectAVLTreeSet<>(getVertices());
		allNodes.addAll(g1.getVertices());
		LabeledNode nodeG, nodeG1;
		for (LabeledNode n : allNodes) {
			nodeG = getNode(n.getName());
			nodeG1 = g1.getNode(n.getName());
			if (nodeG == null || nodeG1 == null || nodeG.propositionObserved != nodeG1.propositionObserved || !nodeG.getLabel().equals(nodeG1.getLabel())) {
				sameNodes = false;
			}
		}
		return sameNodes;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isDest(LabeledNode vertex, E edge) {
		if (vertex == null || edge == null)
			return false;
		int nodeIndex = this.nodeName2index.getInt(vertex.name);
		if (nodeIndex == Constants.INT_NULL)
			return false;

		for (int i = 0; i < this.order; i++) {
			if (edge.equalsByName(this.adjacency[i][nodeIndex]))
				return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isSource(LabeledNode vertex, E edge) {
		if (vertex == null || edge == null)
			return false;
		int nodeIndex = this.nodeName2index.getInt(vertex.name);
		if (nodeIndex == Constants.INT_NULL)
			return false;

		for (int i = 0; i < this.order; i++) {
			if (edge.equalsByName(this.adjacency[nodeIndex][i]))
				return true;
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeEdge(E edge) {
		if (edge == null)
			return false;
		return removeEdge(edge.getName());
	}

	/**
	 * <p>
	 * removeEdge.
	 * </p>
	 *
	 * @param edgeName a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean removeEdge(String edgeName) {
		EdgeIndex ei;
		if (edgeName == null || (ei = this.edge2index.get(edgeName)) == null)
			return false;

		this.adjacency[ei.rowAdj][ei.colAdj] = null;
		removeEdgeFromIndex(this.getEdge(edgeName));
		this.lowerCaseEdges = null;
		return true;
	}

	/**
	 * Removes all empty edges in the graph.
	 *
	 * @return true if at least one edge was removed.
	 */
	public boolean removeEmptyEdges() {
		boolean removed = false;
		for (final E e : this.getEdges()) {
			if (e.isEmpty()) {
				this.removeEdge(e);
				removed = true;
			}
		}
		return removed;
	}

	/** {@inheritDoc} */
	@Override
	public boolean removeVertex(LabeledNode removingNode) {
		/**
		 * I don't touch the adjacency size. I just move last col and row to the col and row that have to be removed.
		 */
		int removingNodeIndex;
		if (removingNode == null || (removingNodeIndex = this.nodeName2index.getInt(removingNode.name)) == Constants.INT_NULL) {
			LOG.fine("I cannot remove vertex " + removingNode + " because it is null or its index is null.");
			return false;
		}

		removingNode.removeObserver("nodeLabel", this);
		removingNode.removeObserver("nodeName", this);
		removingNode.removeObserver("nodeProposition", this);
		int last = this.order - 1;
		// Start to move node to remove at the end of adjacency matrix and to remove all its edges.
		if (removingNodeIndex == last) {
			for (int i = 0; i < last; i++) {
				// I just nullify last col and last row
				removeEdgeFromIndex(this.adjacency[i][last]);
				removeEdgeFromIndex(this.adjacency[last][i]);
				this.adjacency[i][last] = this.adjacency[last][i] = null;
			}
			removeEdgeFromIndex(this.adjacency[last][last]);
			this.adjacency[last][last] = null;
		} else {
			for (int i = 0; i < last; i++) {
				if (i == removingNodeIndex) {
					removeEdgeFromIndex(this.adjacency[i][i]);
					this.adjacency[i][i] = this.adjacency[last][last];
					updateEdgeInIndex(this.adjacency[i][i], i, i);
					removeEdgeFromIndex(this.adjacency[i][last]);
					removeEdgeFromIndex(this.adjacency[last][i]);
					this.adjacency[last][last] = this.adjacency[i][last] = this.adjacency[last][i] = null;
					continue;
				}
				removeEdgeFromIndex(this.adjacency[i][removingNodeIndex]);
				this.adjacency[i][removingNodeIndex] = this.adjacency[i][last];
				updateEdgeInIndex(this.adjacency[i][removingNodeIndex], i, removingNodeIndex);
				this.adjacency[i][last] = null;

				removeEdgeFromIndex(this.adjacency[removingNodeIndex][i]);
				this.adjacency[removingNodeIndex][i] = this.adjacency[last][i];
				updateEdgeInIndex(this.adjacency[removingNodeIndex][i], removingNodeIndex, i);
				this.adjacency[last][i] = null;
			}
		}
		// End to move node to remove at the end of adjacency matrix and to remove all its edges.
		this.index2node.remove(removingNodeIndex);
		this.nodeName2index.removeInt(removingNode.name);
		if (removingNodeIndex != last) {
			LabeledNode nodeMovedToRemovedNodePosition = this.index2node.get(last);
			this.index2node.remove(last);
			this.index2node.put(removingNodeIndex, nodeMovedToRemovedNodePosition);
			this.nodeName2index.put(nodeMovedToRemovedNodePosition.name, removingNodeIndex);
		}
		this.order = last;
		clearCache();

		return true;
	}

	/**
	 * Reverse (transpose) the current graph.
	 */
	public void reverse() {
		this.transpose();
	}

	/**
	 * Transposes <code>this</code> inverting only the source/destination of each edge.
	 * All other attributes of an edge are not modified.
	 */
	public void transpose() {
		int n = this.getVertexCount();
		for (int i = 1; i < n; i++) {
			for (int j = 0; j < i; j++) {
				E eIJ = this.adjacency[i][j];
				E eJI = this.adjacency[j][i];
				this.adjacency[i][j] = eJI;
				this.adjacency[j][i] = eIJ;
				updateEdgeInIndex(eIJ, j, i);
				updateEdgeInIndex(eJI, i, j);
			}
		}
	}

	/**
	 * <p>Setter for the field <code>inputFile</code>.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 */
	public void setInputFile(File file) {
		this.inputFile = file;
	}

	/**
	 * Setter for the field <code>name</code>.
	 *
	 * @param graphName the name to set
	 */
	public void setName(final String graphName) {
		this.name = graphName;
	}

	/**
	 * <p>setZ.</p>
	 *
	 * @param z the node to be set as Z node of the graph.
	 */
	public void setZ(final LabeledNode z) {
		if (z == null) {
			this.Z = null;
			return;
		}
		if (getNode(z.getName()) == null)
			addVertex(z);
		this.Z = z;
	}

	/**
	 * Takes in all internal structures of g.<br>
	 * This method is useful to copy the references of internal data structure of the given graph 'g' into the current. It is not a clone because even if
	 * g!=this, all internal data structures are the same.
	 *
	 * @param g the graph to copy.
	 */
	@SuppressWarnings("unchecked")
	public void takeIn(final TNGraph<? extends E> g) {
		this.adjacency = g.adjacency;
		this.aLabelAlphabet = g.aLabelAlphabet;
		this.childrenOfObserver = g.childrenOfObserver;
		this.edge2index = ((TNGraph<E>) g).edge2index;
		this.edgeFactory = ((TNGraph<E>) g).edgeFactory;
		this.index2node = g.index2node;
		this.inputFile = g.inputFile;
		this.lowerCaseEdges = g.lowerCaseEdges;
		this.name = g.name;
		this.nodeName2index = g.nodeName2index;
		this.observer2Z = ((TNGraph<E>) g).observer2Z;
		this.order = g.order;
		this.proposition2Observer = g.proposition2Observer;
		this.Z = g.Z;
		this.type = g.type;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer(
				"%TNGraph: "
						+ ((this.name != null) ? this.name : (this.inputFile != null) ? this.inputFile.toString() : "no name")
						// + "\n%TNGraph<E> Syntax\n"
						// + "%LabeledNode: <name, label, [observed proposition], [>\n"
						// + "%T: <name, type, source node, dest. node, L:{labeled values}, LL:{labeled lower-case values}, UL:{labeled upper-case values}>"
						+ "\n");
		sb.append("%Nodes:\n");

		for (final LabeledNode n : this.getVertices()) {
			sb.append(n.toString());
			sb.append("\n");
		}
		sb.append("%Edges:\n");
		for (final E e : this.getEdges()) {
			sb.append(this.getSource(e).toString() + "--" + e.toString() + "-->" + this.getDest(e).toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/** {@inheritDoc} */
	@Override
	public void propertyChange(PropertyChangeEvent pce) {
		if (pce == null)
			return;
		Object o = pce.getSource();
		String property = pce.getPropertyName();
		Object old = pce.getOldValue();
		if (o instanceof LabeledNode) {
			LabeledNode node = (LabeledNode) o;
			if (property.equals("nodeName")) {
				String oldValue = (String) old;
				int oldI = this.nodeName2index.getInt(oldValue);
				int newI = this.nodeName2index.getInt(node.getName());
				if (newI != Constants.INT_NULL) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER))
							LOG.finer("Values in nodeName2index: " + this.nodeName2index.toString());
					}
					if (Debug.ON)
						LOG.severe("It is not possible to rename node " + oldValue + " with " + node.getName()
								+ " because there is already a node with name " + node.getName());
					node.name = oldValue;
					return;
				}
				if (this.nodeName2index.removeInt(oldValue) != oldI) {
					LOG.severe("It is not possible to remove the node " + oldValue);
				}
				this.nodeName2index.put(node.getName(), oldI);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer("The nodeName2Index is updated. Removed old name " + oldValue + ". Add the new one: " + node + " at position " + oldI
								+ "\nValues in nodeName2index: " + this.nodeName2index.toString());
					}
				}
				node.setALabel(null);
				if (this.getZ() != null && oldValue.equals(this.getZ().name)) {
					this.setZ(null);
				}
				return;
			}
			if (property.equals("nodeProposition")) {
				char newP = node.propositionObserved;
				// it is complicated to rely on proposition2Observer because it can be erased for some reason.
				// So, the check is made checking all nodes.
				if (newP != Constants.UNKNOWN) {
					for (final LabeledNode n : getVertices()) {
						if (n != node && n.getPropositionObserved() == newP) {
							if (Debug.ON) {
								LOG.severe("It is not possible to assign proposition " + newP + " to node " + node.getName()
										+ " because there is already a node that observes the proposition: node " + n);
								node.propositionObserved = ((Character) old).charValue();
							}
							return;
						}
					}
				}
				this.proposition2Observer = null;
				this.childrenOfObserver = null;
				this.observer2Z = null;
				return;
			}
			if (property.equals("nodeLabel")) {
				this.childrenOfObserver = null;
			}
		} // LabeledNode

		if (o instanceof AbstractEdge) {
			AbstractEdge edge = (AbstractEdge) o;
			if (property.equals("edgeName")) {
				String oldName = (String) old;
				EdgeIndex oldI = this.edge2index.get(oldName);
				EdgeIndex newI = this.edge2index.get(edge.getName());
				if (newI != null) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER))
							LOG.finer("Values in edge2index: " + this.edge2index.toString());
					}
					if (Debug.ON)
						LOG.severe("It is not possible to rename edge " + (oldName) + " with " + edge.getName()
								+ " because there is already a edge with name " + edge.getName());
					edge.name = (String) old;
					return;
				}
				this.edge2index.remove(oldName);
				this.edge2index.put(edge.getName(), oldI);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer("The edge2index is updated. Removed old name " + oldName + ". Add the new one: " + edge + " at position " + oldI +
								"\nValues in edge2index: " + this.edge2index.toString());
					}
				}
				return;
			}
			if (property.equals("lowerLabel:remove")) {
				BasicCSTNUEdge e1 = (BasicCSTNUEdge) edge;
				this.lowerCaseEdges.remove(e1);
				return;
			}
			if (property.equals("lowerLabel:add")) {
				BasicCSTNUEdge e1 = (BasicCSTNUEdge) edge;
				if (this.lowerCaseEdges == null) {
					this.lowerCaseEdges = new ObjectArrayList<>();
				}
				this.lowerCaseEdges.add(e1);
			}
			return;
		}
		if (property.equals("edgeType")) {
			this.lowerCaseEdges = null;
		}
	}

	/**
	 * builds this.observer2Z;
	 */
	private void buildObserver2ZEdgesSet() {
		this.observer2Z = new ObjectArrayList<>();
		if (this.Z == null)
			return;
		Char2ObjectMap<LabeledNode> observers = getObservedAndObserver();
		for (final LabeledNode node : observers.values()) {
			E e = this.findEdge(node, this.Z);
			if (e != null)
				this.observer2Z.add(e);
		}
	}

	/**
	 * @param size
	 * @return a bi-dimensional size x size vector for containing T elements.
	 */
	@SuppressWarnings("unchecked")
	private E[][] createAdjacency(int size) {
		return (E[][]) Array.newInstance(this.edgeFactory.getEdgeImplClass(), size, size);
	}

	/**
	 * @param e
	 */
	private void removeEdgeFromIndex(E e) {
		if (e == null || e.getName() == null)
			return;
		((AbstractEdge) e).removeObserver("edgeType", this);
		((AbstractEdge) e).removeObserver("edgeName", this);
		this.edge2index.remove(e.getName());
	}

	/**
	 * @param e
	 * @param row
	 * @param col
	 */
	private void updateEdgeInIndex(E e, int row, int col) {
		if (e == null || e.getName() == null)
			return;
		EdgeIndex ei = this.edge2index.get(e.getName());
		ei.rowAdj = row;
		ei.colAdj = col;
	}
}
