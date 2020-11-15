package it.univr.di.cstnu.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.AbstractObject2IntMap.BasicEntry;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.univr.di.Debug;
import it.univr.di.cstnu.algorithms.AbstractCSTN.NodesToCheck;
import it.univr.di.cstnu.graph.Component.Color;
import it.univr.di.cstnu.graph.Edge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.STNEdge;
import it.univr.di.cstnu.graph.STNEdgeInt;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;

/**
 * Simple class to represent and consistency-check Simple Temporal Network (STN) where the edge weight are signed integer.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class STN {

	public static enum CheckAlgorithm {
		/**
		 * 
		 */
		BannisterEppstein,
		/**
		 * Bellman-Ford
		 */
		BellmanFord,
		/**
		 * Bellman-Ford
		 */
		BellmanFordSingleSink,
		/**
		 * Dijkstra
		 */
		Dijkstra,
		/**
		 * Floyd-Warshall
		 */
		FloydWarshall,
		/**
		 * Johnson
		 */
		Johnson,
		/**
		 * Yen
		 */
		Yen,
		/**
		 * Yen
		 */
		YenSingleSink
	}

	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class STNCheckStatus {
		/**
		 * True if the network is consistent so far.
		 */
		public boolean consistency = true;

		/**
		 * Counters about the # of application of different rules.
		 */
		public int cycles = 0, propagationCalls = 0;

		/**
		 * Execution time in nanoseconds.
		 */
		public long executionTimeNS = Constants.INT_NULL;

		/**
		 * True if no rule can be applied anymore.
		 */
		public boolean finished = false;

		/**
		 * Standard Deviation of Execution time if this last one is a mean. In nanoseconds.
		 */
		public long stdDevExecutionTimeNS = Constants.INT_NULL;

		/**
		 * True if check has been interrupted because a give time-out has occurred.
		 */
		public boolean timeout = false;

		/**
		 * True if all data structured have been initialized.
		 */
		boolean initialized = false;

		/**
		 * Reset all indexes.
		 */
		public void reset() {
			this.consistency = true;
			this.cycles = 0;
			this.propagationCalls = 0;
			this.executionTimeNS = this.stdDevExecutionTimeNS = Constants.INT_NULL;
			this.finished = this.timeout = false;
			this.initialized = false;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("The check is");
			if (!this.finished)
				sb.append(" NOT");
			sb.append(" finished after ").append(this.cycles).append(" cycle(s).\n");
			if (this.finished) {
				sb.append("The consistency check has determined that given network is ");
				if (!this.consistency)
					sb.append("NOT ");
				sb.append("consistent.\n");
			}
			sb.append("Propagation has been applied ").append(this.propagationCalls).append(" times.\n");
			if (this.timeout)
				sb.append("Checking has been interrupted because execution time exceeds the given time limit.\n");

			if (this.executionTimeNS != Constants.INT_NULL)
				sb.append("The global execution time has been ").append(this.executionTimeNS).append(" ns (~").append((this.executionTimeNS / 1E9))
						.append(" s.)");
			return sb.toString();
		}
	}

	/**
	 * Suffix for file name
	 */
	public static String FILE_NAME_SUFFIX = ".stn";

	/**
	 * The name for the initial node.
	 */
	public static String ZeroNodeName = "Z";
	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger("it.univr.di.cstnu.algorithms.STN");

	/**
	 * Version of the class
	 */
	static final String VERSIONandDATE = "Version 1.0 - July, 15 2019";

	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		STN stn = new STN();
		System.out.println(stn.getVersionAndCopyright());
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		if (!stn.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");
		if (stn.versionReq) {
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loading graph...");
			}
		}
		TNGraphMLReader<STNEdge> graphMLReader = new TNGraphMLReader<>(stn.fInput, STNEdgeInt.class);
		stn.setG(graphMLReader.readGraph());

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("STN Graph loaded!\nNow, it is time to check it...");
			}
		}
		// STNCheckStatus status;
		// try {
		// status = stn.consistencyCheck();
		// } catch (final WellDefinitionException e) {
		// System.out.print("An error has been occured during the checking: " + e.getMessage());
		// return;
		// }
		// if (status.finished) {
		// System.out.println("Checking finished!");
		// if (status.consistency) {
		// System.out.println("The given STN is consistent!");
		// } else {
		// System.out.println("The given STN is not consistent!");
		// }
		// System.out.println("Details: " + status);
		// System.out.println("Graph checked: " + stn.getGChecked());
		// } else {
		// System.out.println("Checking has not been finished!");
		// System.out.println("Details: " + status);
		// }
		ObjectList<ObjectList<LabeledNode>> rcSet = stn.getSTNRigidComponents();
		System.out.println("List of rigid components: ");
		int i = 1;
		for (ObjectList<LabeledNode> rc : rcSet) {
			System.out.println("RC #" + (i++));
			System.out.println(rc.toString());
		}
	}

	/**
	 * Determines the minimal distance between source node and any node (or any node and the sink (called source) if backward) using the BellmanFord algorithm.
	 * The minimal distance is stored as potential value in each node.
	 * If the graph contains a negative cycle, it returns false.
	 * 
	 * @param g1 input graph
	 * @param source the source node for the algorithm
	 * @param backward true if the search has to be backward.
	 * @param checkStatus1 status to update with statistics of algorithm. It can be null.
	 * @return true if the graph is consistent, false otherwise.
	 */
	static boolean bellmanFord(TNGraph<STNEdge> g1, LabeledNode source, boolean backward, STNCheckStatus checkStatus1) {
		if (g1 == null)
			return false;
		final Collection<LabeledNode> nodes = g1.getVertices();
		final int n = nodes.size();
		if (source == null || !nodes.contains(source))
			return false;
		final Collection<STNEdge> edges = g1.getEdges();
		int v;

		for (LabeledNode node : nodes) {
			node.setPotential(Constants.INT_POS_INFINITE);
		}
		source.setPotential(0);
		LabeledNode s, d;
		for (int i = 1; i < n; i++) {// n-1 rounds
			for (STNEdge e : edges) {
				if (backward) {// for single sink, each edge is reversed
					d = g1.getSource(e);
					s = g1.getDest(e);
				} else {
					s = g1.getSource(e);
					d = g1.getDest(e);
				}
				v = Constants.sumWithOverflowCheck(s.getPotential(), e.getValue());
				if (d.getPotential() > v) {
					if (Debug.ON) {
						STN.LOG.finer("BF " + d.getName() + " potential: " + Constants.formatInt(d.getPotential()) + " --> " + Constants.formatInt(v));
					}
					d.setPotential(v);
					if (checkStatus1 != null)
						checkStatus1.propagationCalls++;
				}
			}
		}
		// check if a negative cycle is present
		for (STNEdge e : edges) {
			if (backward) {// for single sink, each edge is reversed
				d = g1.getSource(e);
				s = g1.getDest(e);
			} else {
				s = g1.getSource(e);
				d = g1.getDest(e);
			}
			v = Constants.sumWithOverflowCheck(s.getPotential(), e.getValue());
			if (d.getPotential() > v) {
				if (Debug.ON) {
					STN.LOG.finer("BF inconsitency:" + d.getName() + " potential: " + Constants.formatInt(d.getPotential()) + "-->" + Constants.formatInt(v));
				}
				if (checkStatus1 != null) {
					checkStatus1.consistency = false;
					checkStatus1.finished = true;
				}
				return false;
			}
		}
		if (checkStatus1 != null) {
			checkStatus1.cycles = n;
			checkStatus1.consistency = true;
			checkStatus1.finished = true;
		}
		return true;
	}

	/**
	 * Stops a computation if current instant is after the <code>timeoutInstant</code>
	 * setting <code>status.timeout=true</code>.<br>
	 * As courtesy, it sets also <code>status.consistency=status.finished=false</code>.
	 * 
	 * @param timeoutInstant
	 * @param status
	 * @return true if timeOut has been reached.
	 */
	static final boolean checkTimeOutAndAdjustStatus(Instant timeoutInstant, STNCheckStatus status) {
		if (Instant.now().isAfter(timeoutInstant)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Time out occurred!");
				}
			}
			status.timeout = true;
			status.consistency = false;
			status.finished = false;
			return true;
		}
		return false;
	}

	/**
	 * Determines the minimal distance between source node and any node using the Dijkstra algorithm.
	 * Each minimal distance is stored as potential value of the node.
	 * If the graph contains a negative edge beyond the source outgoing edges or the source is not in the graph, it returns false.
	 * 
	 * @param g1 input graph
	 * @param source the source node. It must belong to g1.
	 * @param checkStatus1 status to update with statistics of algorithm. It can be null.
	 * @return true if the node distances have been determined, false otherwise.
	 */
	static boolean dijkstra(TNGraph<STNEdge> g1, LabeledNode source, STNCheckStatus checkStatus1) {
		Object2IntMap<LabeledNode> nodeDistances = dijkstraReadOnly(g1, source, checkStatus1);
		if (nodeDistances.size() > 0) {
			for (LabeledNode node : g1.getVertices()) {
				node.setPotential(nodeDistances.getInt(node));
			}
			return true;
		}
		return false;
	}

	/**
	 * This method differs from {@link #dijkstra(TNGraph, LabeledNode, STNCheckStatus)} in the fact that distances are store on
	 * a map <node, value> that is returned. Node potentials are not modified.
	 * 
	 * @see #dijkstra(TNGraph, LabeledNode, STNCheckStatus)
	 * @param g1 input graph
	 * @param source the source node for the algorithm
	 * @param checkStatus1 status to update with statistics of algorithm. It can be null.
	 * @return a non empty map (node,integer) representing the distances of all nodes from the given source.
	 *         Null if any error occurs like source not in graph, graph is empty, negative edge beyond source edges has been found, etc.
	 */
	static Object2IntMap<LabeledNode> dijkstraReadOnly(TNGraph<STNEdge> g1, LabeledNode source, STNCheckStatus checkStatus1) {
		if (g1 == null)
			return null;
		final Collection<LabeledNode> nodes = g1.getVertices();
		final int n = nodes.size();
		if (source == null || !nodes.contains(source))
			return null;
		int v;

		NodePriorityHeap nodeQueue = new NodePriorityHeap();
		for (LabeledNode node : nodes) {
			nodeQueue.insertOrDecrease(node, Constants.INT_POS_INFINITE);
		}
		nodeQueue.insertOrDecrease(source, 0);

		LabeledNode s, d;
		BasicEntry<LabeledNode> entry;
		int sValue, eValue;

		while (nodeQueue.size() > 0) {
			entry = nodeQueue.extractMinEntry();
			s = entry.getKey();
			sValue = entry.getIntValue();

			if (Debug.ON) {
				STN.LOG.finer("Dijkstra node " + s.getName() + " with distance " + sValue);
			}
			for (STNEdge e : g1.getOutEdges(s)) {
				d = g1.getDest(e);
				eValue = e.getValue();
				if (s != source && eValue < 0)
					return null;
				v = Constants.sumWithOverflowCheck(sValue, eValue);
				if (nodeQueue.value(d) > v) {
					if (Debug.ON) {
						STN.LOG.finer(
								"Dijkstra updates " + d.getName() + " potential adding edge value " + eValue + ": " + Constants.formatInt(nodeQueue.value(d))
										+ " --> " + Constants.formatInt(v));
					}
					nodeQueue.insertOrDecrease(d, v);
					if (checkStatus1 != null)
						checkStatus1.propagationCalls++;
				}
			}
		}
		if (checkStatus1 != null) {
			checkStatus1.cycles = n;
			checkStatus1.consistency = true;
			checkStatus1.finished = true;
		}
		if (Debug.ON) {
			LOG.finest("Determined node distances from the heap: " + nodeQueue.getPriorities().toString());
		}
		return nodeQueue.getPriorities();
	}

	/**
	 * Determines the minimal distance between all pair of vertexes modifying the given graph.
	 * If the graph contains a negative cycle, it returns false and the graph contains the edges that
	 * have determined the negative cycle.
	 * 
	 * @param g1 the graph to complete
	 * @param checkStatus1 possible status to fill during the computation. It can be null.
	 * @return true if the graph is consistent, false otherwise.
	 *         If the response is false, the edges do not represent the minimal distance between nodes.
	 */
	static boolean floydWarshall(TNGraph<STNEdge> g1, STNCheckStatus checkStatus1) {
		final int n = g1.getVertexCount();
		final EdgeSupplier<STNEdge> edgeFactory = g1.getEdgeFactory();
		final LabeledNode[] node = g1.getVerticesArray();
		LabeledNode iV, jV, kV;
		STNEdge ik, kj, ij;
		int v;

		for (int k = 0; k < n; k++) {
			kV = node[k];
			for (int i = 0; i < n; i++) {
				iV = node[i];
				for (int j = 0; j < n; j++) {
					if ((k == i) || (k == j)) {
						continue;
					}
					jV = node[j];

					ik = g1.findEdge(iV, kV);
					kj = g1.findEdge(kV, jV);
					if ((ik == null) || (kj == null)) {
						continue;
					}
					v = Constants.sumWithOverflowCheck(ik.getValue(), kj.getValue());

					if (i == j) {
						if (v < 0) {
							// check negative cycles
							STN.LOG.info("Found a negative cycle on node " + iV.getName()
									+ "\nDetails: ik=" + ik + ", kj=" + kj + ",  v=" + v);
							if (checkStatus1 != null) {
								checkStatus1.consistency = false;
								checkStatus1.finished = true;
							}
							return false;
						}
						continue;
					}

					ij = g1.findEdge(iV, jV);
					int old = Constants.INT_POS_INFINITE;
					if (ij == null) {
						ij = edgeFactory.get("e" + node[i].getName() + node[j].getName());
						ij.setConstraintType(Edge.ConstraintType.derived);
						g1.addEdge(ij, iV, jV);
					} else {
						old = ij.getValue();
					}
					if (old > v) {
						ij.setValue(v);
					}
					if (old != v) {
						if (checkStatus1 != null) {
							checkStatus1.propagationCalls++;
						}
						if (Debug.ON) {
							STN.LOG.fine("Edge " + ij.getName() + ": " + Constants.formatInt(old) + " --> " + Constants.formatInt(v));
						}
					}
				}
			}
			if (checkStatus1 != null) {
				checkStatus1.cycles++;
			}
		}
		if (checkStatus1 != null) {
			checkStatus1.consistency = true;
			checkStatus1.finished = true;
		}
		return true;
	}

	/**
	 * Determines the minimal distance between all pairs of nodes using the Johnson algorithm.
	 * The minimal distance is stored as potential value in each node.
	 * If the graph contains a negative cycle, it returns false.
	 * 
	 * @param g1 input graph
	 * @param horizon the maximum edge value present in the graph. It is necessary for guaranteeing that Z reaches any nodes.
	 * @param checkStatus1 status to update with statistics of algorithm. It can be null.
	 * @return true if the graph is consistent, false otherwise.
	 */
	static boolean johnson(TNGraph<STNEdge> g1, int horizon, STNCheckStatus checkStatus1) {
		// I cannot trust that Z can reach any node
		// I add an edge Z-->node for each node with horizon value.
		EdgeSupplier<STNEdge> edgeFactory = g1.getEdgeFactory();
		if (Debug.ON) {
			LOG.finer("Horizon value: " + horizon + "\nAdding edges for guaranteeing that Z reaches each node.");
		}
		LabeledNode Z = g1.getZ();
		makeNodesReachableBy(g1, Z, horizon);
		if (Debug.ON) {
			LOG.finer("Determining shortest-paths from Z by Bellman-Ford.");
		}
		if (!bellmanFord(g1, Z, false, checkStatus1))
			return false;

		// g1 is completed with BellmanFord potential values.
		if (Debug.ON) {
			LOG.finer("Re-weighting all edges.");
		}
		reweight(g1);

		TNGraph<STNEdge> finalG = new TNGraph<>(g1, g1.getEdgeImplClass());

		// Determine the distances from each node updating the edge in the finalG
		for (LabeledNode source : g1.getVertices()) {

			// Dijkstra determines distances from source
			Object2IntMap<LabeledNode> nodeDistanceFromSource = dijkstraReadOnly(g1, source, checkStatus1);
			// for each other node, adjust the distance from source in finalG
			for (LabeledNode d : g1.getVertices()) {
				if (d == source)
					continue;
				STNEdge finalE = finalG.findEdge(source.getName(), d.getName());

				if (finalE == null) {
					finalE = edgeFactory.get(source.getName() + "__" + d.getName());
					finalE.setConstraintType(ConstraintType.internal);
					finalG.addEdge(finalE, source.getName(), d.getName());
				}

				// new value is the value of the edge in Dijkstra + the original potential difference between destination and source: DijkstraDistance + (d - s)
				int newEdgeValue = Constants.sumWithOverflowCheck(nodeDistanceFromSource.getInt(d),
						Constants.sumWithOverflowCheck(d.getPotential(), -source.getPotential()));
				if (Debug.ON) {
					LOG.finer("Adjusting value of edge " + finalE + " in final graph --> " + newEdgeValue);
				}
				finalE.setValue(newEdgeValue);
			}
		}
		g1.takeIn(finalG);
		return true;
	}

	/**
	 * Determine the reverse-post-order of reachable nodes from the given root node in graph g1.
	 * All node colors are assumed to be null.
	 * At the end, all reachable nodes have also color {@link Color#gray}.
	 * 
	 * @param g1
	 * @param root
	 * @return the array of node in reverse-post-order if g1 is not null and root is a node of g1, null otherwise.
	 */
	static LabeledNode[] reversePostOrderVisit(TNGraph<STNEdge> g1, LabeledNode root) {
		if (g1 == null || !g1.containsVertex(root))
			return null;

		ObjectArrayList<LabeledNode> order = new ObjectArrayList<>();
		depthFirstOrder(g1, root, order, false);

		Collections.reverse(order);

		return order.toArray(new LabeledNode[0]);
	}

	/**
	 * Re-weights all edge weights using potentials of nodes. If any node potential is undefined or infinite, it returns false.
	 * This method is usually called after Bellman-Ford algorithm execution in order to make all edge values positive.
	 * 
	 * @param g1 input graph
	 * @throws IllegalStateException it is not possible to reweighting because potential value are not corrects.
	 */
	static void reweight(TNGraph<STNEdge> g1) {
		final Collection<STNEdge> edges = g1.getEdges();
		LabeledNode s, d;
		int sV, dV, eV, newV;
		for (STNEdge e : edges) {
			s = g1.getSource(e);
			d = g1.getDest(e);
			sV = s.getPotential();
			dV = d.getPotential();
			eV = e.getValue();
			if (sV == Constants.INT_NULL || dV == Constants.INT_NULL || sV == Constants.INT_POS_INFINITE || dV == Constants.INT_POS_INFINITE) {
				throw new IllegalStateException("At least one of the following nodes contains an illegal value: " + s + " or " + d);
			}
			// new value is the value - the potential difference between destination and source: e - (d - s) = e + s -d
			newV = Constants.sumWithOverflowCheck(eV, Constants.sumWithOverflowCheck(sV, -dV));
			String log = "Re-weighting " + e.getName() + ": Source potential: " + Constants.formatInt(sV) + ", Dest potential: "
					+ Constants.formatInt(dV) + ". Edge value: " + Constants.formatInt(eV) + ". New value: " + Constants.formatInt(newV);

			if (newV < 0) {
				throw new IllegalStateException("Error in re-weighting. " + log);
			}
			e.setValue(newV);
			if (Debug.ON) {
				STN.LOG.finer(log);
			}
		}
	}

	/**
	 * Given the unweighed parent graph (assumed to be a tree) as parent vector, returns true if such a graph contains a cycle.
	 * It is assumed that the root of tree is the node with index 0 and parent[0] == 0, i.e., root ha itself as parent.
	 * 
	 * @param parent
	 * @return true if there is a cycle.
	 */
	private static boolean checkNegativeCycle(int[] parent) {
		if (parent[0] != 0)
			// Z cannot have a parent!
			return true;
		int n = parent.length;
		int[] visitLevel = new int[n];// Initialized to 0

		int level = n;
		for (int i = n - 1; i >= 0; i--) {
			if (visitLevel[i] > level) {
				// node already visited
				continue;
			}
			visitLevel[i] = level;
			int p = parent[i];
			while (p != 0) {
				if (visitLevel[p] == level) {
					// found a cycle
					return true;
				}
				if (visitLevel[p] > level) {
					// node already visited and ok
					break;
				}
				visitLevel[p] = level;
				p = parent[p];
			}
			level--;
		}
		return false;
	}

	/**
	 * Recursive method for determining a depth-first order of nodes.
	 * It not visits nodes having {@link Color#gray} color.
	 * 
	 * @param g1 the graph in which making the visit
	 * @param node node to visit after his descendants
	 * @param finalOrder the resulting order
	 * @param translate true if the order has to be determined in the translated graph.
	 */
	private static void depthFirstOrder(TNGraph<STNEdge> g1, LabeledNode node, ObjectList<LabeledNode> finalOrder, boolean translate) {
		node.setColor(Color.gray);
		Collection<LabeledNode> adjNodes = (translate) ? g1.getPredecessors(node) : g1.getSuccessors(node);
		for (LabeledNode adjNode : adjNodes) {
			if (adjNode.getColor() == Color.gray)
				continue;
			depthFirstOrder(g1, adjNode, finalOrder, translate);
		}
		finalOrder.add(node);
	}

	/**
	 * Make each node reachable by source.
	 * 
	 * @param g1
	 * @param source
	 * @param horizon
	 */
	private static void makeNodesReachableBy(TNGraph<STNEdge> g1, LabeledNode source, int horizon) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Making all nodes reachable from " + source.getName());
			}
		}
		EdgeSupplier<STNEdge> edgeFact = g1.getEdgeFactory();
		for (final LabeledNode node : g1.getVertices()) {
			// 3. Checks that each node different from Z has an edge to Z
			if (node == source)
				continue;
			STNEdge edge = g1.findEdge(source, node);
			if (edge == null) {
				edge = edgeFact.get(source.getName() + "__" + node.getName());
				g1.addEdge(edge, source, node);
				edge.setConstraintType(ConstraintType.internal);
				edge.setValue(horizon);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER,
								"Added " + edge.getName() + ": " + source.getName() + "--(" + horizon + ")-->" + node.getName());
					}
				}
			}
		}
	}

	/**
	 * Check status
	 */
	STNCheckStatus checkStatus = new STNCheckStatus();

	/**
	 */
	@Option(required = false, name = "-cleaned", usage = "Output a cleaned result. A result cleaned graph does not contain empty edges or labeled values containing unknown literals.")
	boolean cleanCheckedInstance = true;

	/**
	 * Which algorithm to use for consistency check. Default is Flowyd-Warshall
	 */
	CheckAlgorithm defaultConsistencyCheckAlg = CheckAlgorithm.FloydWarshall;

	/**
	 * The input file containing the STN graph in GraphML format.
	 */
	@Argument(required = false, index = 0, usage = "file_name must be the input STN graph in GraphML format.", metaVar = "file_name")
	File fInput;

	/**
	 * Output file where to write the XML representing the minimal STN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "Output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "output_file_name")
	File fOutput = null;

	/**
	 * Input TNGraph.
	 */
	TNGraph<STNEdge> g = null;

	/**
	 * TNGraph on which to operate.
	 */
	TNGraph<STNEdge> gCheckedCleaned = null;

	/**
	 * Horizon value. A node that has to be executed after such time means that it has not to be executed!
	 */
	int horizon = Constants.INT_NULL;

	/**
	 * Absolute value of the max negative weight determined during initialization phase.
	 */
	int maxWeight = Constants.INT_NULL;

	/**
	 * Output stream to fOutput
	 */
	PrintStream output = null;

	/**
	 * Timeout in seconds for the check.
	 */
	@Option(required = false, name = "-t", aliases = "--timeOut", usage = "Timeout in seconds for the check", metaVar = "seconds")
	int timeOut = 2700;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	boolean versionReq = false;

	/**
	 * Z node of the graph.
	 * Utility reference for many method. #initAndCheck sets this value.
	 */
	LabeledNode Z = null;

	/**
	 * @param graph TNGraph to check
	 */
	public STN(TNGraph<STNEdge> graph) {
		this();
		this.setG(graph);// sets also checkStatus!
	}

	/**
	 * @param graph TNGraph to check
	 * @param giveTimeOut timeout for the check
	 */
	public STN(TNGraph<STNEdge> graph, int giveTimeOut) {
		this(graph);
		this.timeOut = giveTimeOut;
	}

	/**
	 * Default constructor.
	 */
	STN() {
	}

	/**
	 * Determines the minimal distance in this STN between any node and the sink one (node Z) using the BellmanFord algorithm.
	 * The minimal distance is stored as potential value in each node.
	 * If the graph contains a negative cycle, it returns false.
	 * 
	 * @param backward true if the search has to be backward.
	 * @return true if the graph is consistent, false otherwise.
	 */
	public boolean bellmanFord(boolean backward) {
		return STN.bellmanFord(this.g, this.g.getZ(), backward, this.checkStatus);
	}

	/**
	 * Checks the consistency of a STN instance within timeout seconds.
	 * During the execution of this method, the given graph is modified. <br>
	 * If the check is successful, all constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * <br>
	 * After a check, {@link #getGChecked} returns the graph resulting after the check.
	 * 
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if the nextGraph<E> is not well defined (does not observe all well definition
	 *             properties).
	 */
	public STNCheckStatus consistencyCheck() throws WellDefinitionException {
		return consistencyCheck(this.defaultConsistencyCheckAlg);
	}

	public STNCheckStatus consistencyCheck(CheckAlgorithm alg) throws WellDefinitionException {
		try {
			initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The STN graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("The STN has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}
		Instant startInstant = Instant.now();

		switch (alg) {
		case FloydWarshall:
			this.checkStatus.consistency = floydWarshall();
			break;
		case Johnson:
			this.checkStatus.consistency = johnson();
			break;
		case Dijkstra:
			this.checkStatus.consistency = dijkstra();
			break;
		case BellmanFord:
			this.checkStatus.consistency = bellmanFord(false);
			break;
		case BellmanFordSingleSink:
			this.checkStatus.consistency = bellmanFord(true);
			break;
		case Yen:
			this.checkStatus.consistency = yenAlgorithm(false, false);
			break;
		case YenSingleSink:
			this.checkStatus.consistency = yenAlgorithm(false, true);
			break;
		case BannisterEppstein:
			this.checkStatus.consistency = yenAlgorithm(true, false);
			break;
		default:
			break;
		}
		Instant endInstant = Instant.now();
		this.checkStatus.finished = true;
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "Found an inconsistency.\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Final inconsistent graph: " + this.g);
					}
				}
			}
			this.saveGraphToFile();
			return this.checkStatus;
		}
		// consistent && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Stable state reached. Status: " + this.checkStatus);
			}
		}
		if (this.cleanCheckedInstance) {
			this.gCheckedCleaned = this.g;
		}
		this.saveGraphToFile();
		return this.checkStatus;
	}

	/**
	 * Determines the minimal distance in this STN between source node and any node using the Dijkstra algorithm.
	 * The minimal distance is stored as potential value in each node.
	 * If the graph contains a negative edge, it returns false.
	 * 
	 * @return true if the graph contains only positive edges, false otherwise.
	 */
	public boolean dijkstra() {
		return STN.dijkstra(this.g, this.g.getZ(), this.checkStatus);
	}

	/**
	 * Determines the minimal distance between all pair of vertexes modifying the current STN.
	 * 
	 * @return true if the STN is consistent, false otherwise.
	 *         If the response is false, the edges do not represent the minimal distance between nodes.
	 */
	public boolean floydWarshall() {
		return STN.floydWarshall(this.g, this.checkStatus);
	}

	/**
	 * @return the checkStatus
	 */
	public STNCheckStatus getCheckStatus() {
		return this.checkStatus;
	}

	/**
	 * @return the defaultConsistencyCheckAlg
	 */
	public CheckAlgorithm getDefaultConsistencyCheckAlg() {
		return this.defaultConsistencyCheckAlg;
	}

	/**
	 * @return the fOutput
	 */
	public File getfOutput() {
		return this.fOutput;
	}

	/**
	 * @return the g
	 */
	final public TNGraph<STNEdge> getG() {
		return this.g;
	}

	/**
	 * @return the resulting graph of a check. It is up to the called to be sure the the returned graph is the result of a check.
	 *         It can be used also by subclasses with a proper cast.
	 * @see #setOutputCleaned(boolean)
	 */
	public TNGraph<STNEdge> getGChecked() {
		if (this.cleanCheckedInstance && this.getCheckStatus().finished && this.getCheckStatus().consistency)
			return this.gCheckedCleaned;
		return this.g;
	}

	/**
	 * @return the maxWeight
	 */
	final public int getMaxWeight() {
		return this.maxWeight;
	}

	/**
	 * Returns the Muscettola predecessor graph of source in the current STN.
	 * Be aware that Muscettola predecessor graph is made by ALL shortest paths having source as origin node.
	 * The returned graph is an independent graph with respect to the graph of this object.
	 * 
	 * @param source a node of this STN
	 * @return the predecessor graph of node X, if X belongs (object identity) to this STN, null otherwise.
	 */
	public TNGraph<STNEdge> getSTNPredecessorGraph(LabeledNode source) {
		if (!this.g.containsVertex(source)) {
			STN.LOG.fine("This STN does not contain " + source.getName());
			return null;
		}
		for (STNEdge e : this.g.getEdges()) {
			e.setColor(null);
		}

		TNGraph<STNEdge> pGraph = new TNGraph<>(this.g, this.g.getEdgeImplClass());
		source = pGraph.getNode(source.getName());
		if (!STN.bellmanFord(pGraph, source, false, null)) {
			STN.LOG.fine("This pGraph does not contain " + source.getName());
			return null;
		}

		if (Debug.ON) {
			STN.LOG.fine("Start the determination of predecessor graph of node " + source.getName());
		}

		ObjectArrayFIFOSetQueue<LabeledNode> nodes = new ObjectArrayFIFOSetQueue<>();
		nodes.add(source);
		while (!nodes.isEmpty()) {
			LabeledNode node = nodes.dequeue();
			int nodeValue = node.getPotential();
			for (STNEdge e : pGraph.getOutEdges(node)) {
				LabeledNode d = pGraph.getDest(e);
				if (nodeValue + e.getValue() == d.getPotential() && e.getColor() != Color.white) {
					// e is in a shortest path
					e.setColor(Color.white);
					if (d != source)
						nodes.add(d);
					if (Debug.ON) {
						STN.LOG.finer("Edge " + e.getName() + " added to predecessor graph.");
					}
					STNEdge e1 = this.g.findEdge(source, d);
					if (e1 != null) {
						if (e1.getValue() < d.getPotential())
							throw new IllegalStateException("Something wrong occured: Bellman-Ford determined a non-minimal distance.");
						if (e1.getValue() == d.getPotential()) {
							e1.setColor(Color.white);
							if (Debug.ON) {
								STN.LOG.finer("Edge " + e1.getName() + " added to predecessor graph.");
							}
						}
					}
				}
			}
			nodes.remove(node);
		}

		for (STNEdge e : pGraph.getEdges()) {
			if (Debug.ON) {
				STN.LOG.finest("Edge " + e.getName() + " color: " + e.getColor());
			}
			if (e.getColor() != Color.white)
				pGraph.removeEdge(e);
		}
		return pGraph;
	}

	/**
	 * Determines all the Rigid Components (RC) using the linear time algorithm (w.r.t. the |edges|) proposed by Cormen et al. for Strongly Connected Components
	 * of Z predecessor graphs.<br>
	 * Be aware that Z must reach all nodes before calling this method.
	 * 
	 * @return the list of possible RCs as a list of original nodes. If there is no RCs, the list is empty.
	 */
	public ObjectList<ObjectList<LabeledNode>> getSTNRigidComponents() {
		if (this.checkStatus == null || !this.checkStatus.initialized) {
			try {
				initAndCheck();
			} catch (WellDefinitionException e) {
				e.printStackTrace();
			}
			makeNodesReachableBy(this.g, this.Z, this.horizon);
		}

		TNGraph<STNEdge> g1 = getSTNPredecessorGraph(this.Z);

		// such nodes are different object w.r.t. the nodes of this object.
		LabeledNode[] nodes = reversePostOrderVisit(g1, g1.getZ());

		for (int i = 0; i < nodes.length; i++) {
			nodes[i].setColor(null);
		}
		g1.reverse();

		ObjectList<ObjectList<LabeledNode>> rc = new ObjectArrayList<>();

		for (LabeledNode root : nodes) {
			if (root.getColor() != null)
				continue;
			LabeledNode[] revPOV = reversePostOrderVisit(g1, root);
			if (revPOV.length > 1) {
				ObjectList<LabeledNode> lrc = new ObjectArrayList<>();
				for (LabeledNode node : revPOV) {
					LabeledNode n1 = this.g.getNode(node.getName());
					n1.setPotential(node.getPotential());
					lrc.add(n1);
				}
				rc.add(lrc);
			}
		}
		return rc;
	}

	/**
	 * @return version and copyright string
	 */
	public String getVersionAndCopyright() {
		// I use a non-static method for having a general method that prints the right name for each derived class.
		String s = "\nAcademic and non-commercial use only.\n"
				+ "Copyright © 2017-2019, Roberto Posenato.\n";
		try {
			s = this.getClass().getName() + " " + this.getClass().getDeclaredField("VERSIONandDATE").get(this)
					+ s;
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			//
		}
		return s;
	}

	/**
	 * Initializes the STN instance represented by graph g.
	 * It calls {@link #coreSTNInitAndCheck()}.
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	public boolean initAndCheck() throws WellDefinitionException {
		return coreSTNInitAndCheck();
	}

	/**
	 * @return the fOutputCleaned
	 */
	public boolean isOutputCleaned() {
		return this.cleanCheckedInstance;
	}

	/**
	 * Determines the minimal distance between all pair of vertexes modifying the current STN using Johnson's algorithm.
	 * 
	 * @return true if the STN is consistent, false otherwise.
	 *         If the response is false, the edges do not represent the minimal distance between nodes.
	 */
	public boolean johnson() {
		if (this.horizon == Constants.INT_NULL) {
			try {
				this.initAndCheck();
			} catch (WellDefinitionException e) {
				e.printStackTrace();
			}
		}
		return STN.johnson(this.g, this.horizon, this.checkStatus);
	}

	/**
	 * Makes the graph dispatchable applying Muscettola et al. 1998 algorithm.
	 * 
	 * @return true if it was possible to make the graph dispatchable (i.e., the graph was consistent).
	 */
	public boolean makeDispatchable() {

		if (!this.floydWarshall())
			return false;
		for (STNEdge edge : this.g.getEdges()) {
			edge.setColor(null);
		}

		for (LabeledNode node3 : this.g.getVertices()) {
			// upper dominant
			STNEdge[] incomingEdge = this.g.getInEdges(node3).toArray(new STNEdgeInt[0]);
			for (int i = 0; i < incomingEdge.length - 1; i++) {
				STNEdge edge13 = incomingEdge[i];
				LabeledNode node1 = this.g.getSource(edge13);
				int v13 = edge13.getValue();
				if (v13 < 0)
					continue;
				for (int j = i + 1; j < incomingEdge.length; j++) {
					STNEdge edge23 = incomingEdge[j];
					LabeledNode node2 = this.g.getSource(edge23);
					int v23 = edge23.getValue();
					if (v23 < 0)
						continue;
					STNEdge edge12 = this.g.findEdge(node1, node2);
					int v12 = edge12.getValue();
					boolean edge13NotDominated = true;
					if (edge13.getColor() != Color.gray && v13 == v12 + v23) {
						// edge23 dominates
						edge13.setColor(Color.gray);
						edge13NotDominated = false;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "Edge " + edge13 + " dominated by " + edge23 + " and " + edge12);
							}
						}
					}
					if (edge13NotDominated) {
						STNEdge edge21 = this.g.findEdge(node2, node1);
						int v21 = edge21.getValue();
						if (edge23.getColor() != Color.gray && v23 == v21 + v13) {
							// edge13 dominates
							edge23.setColor(Color.gray);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, "Edge " + edge23 + " dominated by " + edge13 + " and " + edge21);
								}
							}

						}
					}
				}
			}

			// lower dominant
			STNEdge[] outgoingEdge = this.g.getOutEdges(node3).toArray(new STNEdgeInt[0]);
			for (int i = 0; i < outgoingEdge.length - 1; i++) {
				STNEdge edge31 = outgoingEdge[i];
				LabeledNode node1 = this.g.getDest(edge31);
				int v31 = edge31.getValue();
				if (v31 >= 0)
					continue;
				for (int j = i + 1; j < outgoingEdge.length; j++) {
					STNEdge edge32 = outgoingEdge[j];
					LabeledNode node2 = this.g.getDest(edge32);
					int v32 = edge32.getValue();
					if (v32 >= 0)
						continue;
					STNEdge edge12 = this.g.findEdge(node1, node2);
					int v12 = edge12.getValue();
					boolean edge32NotDominated = true;
					if (edge32.getColor() != Color.gray && v32 == v31 + v12) {
						// edge31 dominates
						edge32.setColor(Color.gray);
						edge32NotDominated = false;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "Edge " + edge32 + " dominated by " + edge31 + " and " + edge12);
							}
						}

					}
					if (edge32NotDominated) {
						STNEdge edge21 = this.g.findEdge(node2, node1);
						int v21 = edge21.getValue();
						if (edge31.getColor() != Color.gray && v31 == v32 + v21) {
							// edge13 dominates
							edge31.setColor(Color.gray);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, "Edge " + edge31 + " dominated by " + edge32 + " and " + edge21);
								}
							}

						}
					}
				}
			}
		}
		// remove all gray edges
		for (STNEdge edge : this.g.getEdges()) {
			if (edge.getColor() == Color.gray) {
				this.g.removeEdge(edge);
			}
		}
		return true;
	}

	/**
	 * Stores the graph after a check to the file {@link #fOutput}.
	 * 
	 * @see #getGChecked()
	 */
	public void saveGraphToFile() {
		if (this.fOutput == null) {
			if (this.fInput == null) {
				LOG.info("Input file and output file are null. It is not possible to save the result in automatic way.");
				return;
			}
			String outputName;
			try {
				outputName = this.fInput.getCanonicalPath().replaceFirst(FILE_NAME_SUFFIX + "$", "");
			} catch (IOException e) {
				System.err.println(
						"It is not possible to save the result. Field fOutput is null and no the standard output file can be created: " + e.getMessage());
				return;
			}
			if (!this.getCheckStatus().finished) {
				outputName += "_notFinishedCheck";
				if (this.getCheckStatus().timeout) {
					outputName += "_timeout_" + this.timeOut;
				}
			} else {
				outputName += "_checked_" + ((this.getCheckStatus().consistency) ? "DC" : "NOTDC");
			}
			outputName += FILE_NAME_SUFFIX;
			this.fOutput = new File(outputName);
			LOG.info("Output file name is " + this.fOutput.getAbsolutePath());
		}

		TNGraph<STNEdge> g1 = this.getGChecked();
		g1.setInputFile(this.fOutput);
		g1.setName(this.fOutput.getName());
		g1.removeEmptyEdges();

		StaticLayout<STNEdge> layout = new StaticLayout<>(g1);
		final TNGraphMLWriter graphWriter = new TNGraphMLWriter(layout);
		try (Writer out = new BufferedWriter(new FileWriter(this.fOutput))) {
			graphWriter.save(g1, out);
		} catch (final Exception e) {
			System.err.println("Something is wrong and it is not possible to save the result. The program does not stop. Error: " + e.getMessage());
		}
		LOG.info("Checked instance saved in file " + this.fOutput.getAbsolutePath());
	}

	/**
	 * @param defaultConsistencyCheckAlg1 the defaultConsistencyCheckAlg to set
	 */
	public void setDefaultConsistencyCheckAlg(CheckAlgorithm defaultConsistencyCheckAlg1) {
		this.defaultConsistencyCheckAlg = defaultConsistencyCheckAlg1;
	}

	/**
	 * @param fileOutput the file where to save the result.
	 */
	public void setfOutput(File fileOutput) {
		this.fOutput = fileOutput;
	}

	/**
	 * Considers the given graph as the graph to check (graph will be modified).
	 * Clear all {@link #maxWeight}, {@link #horizon} and {@link #checkStatus}.
	 * 
	 * @param graph set internal TNGraph to g. It cannot be null.
	 */
	public void setG(TNGraph<STNEdge> graph) {
		if (graph == null)
			throw new IllegalArgumentException("Input graph is null!");
		reset();
		this.g = graph;
		this.Z = graph.getZ();// Don't remove this assignment!
	}

	/**
	 * Set to true for having the result graph cleaned of empty edges and labeled values having unknown literals.
	 * 
	 * @param clean the resulting graph
	 */
	public void setOutputCleaned(boolean clean) {
		this.cleanCheckedInstance = clean;
	}

	/**
	 * Algorithm 4 Yen's algorithm (adaptive version with early termination)<br>
	 * Implements Yen's algorithm presented in<br>
	 * J. Y. Yen, “An algorithm for finding shortest routes from all source nodes to a given destination in general networks,”<br>
	 * Q. Appl. Math., vol. 27, no. 4, pp. 526–530, Jan. 1970.<br>
	 * 
	 * @param randomOrder true if nodes have to be ordered randomly. If false, nodes are ordered w.r.t. their name.
	 *            In the case true, the algorithm is also known as Bannister and Eppstein
	 * @param backward true if the search has to be done in backward way.
	 * @return true if the STN is consistent, false otherwise.
	 *         It also fills {@link #checkStatus}.
	 */
	public boolean yenAlgorithm(boolean randomOrder, boolean backward) {
		/**
		 * make a random order of nodes, putting Z at the first position.
		 * The random order is decided setting a random potential and, then, ordering the nodes w.r.t. the potential.
		 * The order is saved in an array and in a hash map. Potential is then reset.
		 */
		int n = this.g.getVertexCount();

		LabeledNode[] orderedNodes;
		if (randomOrder) {
			orderedNodes = this.g.getVertices().toArray(new LabeledNode[n]);
			Random rnd = new Random(System.currentTimeMillis());
			for (int i = 0; i < n; i++) {
				orderedNodes[i].setPotential(rnd.nextInt());
			}
			java.util.Arrays.sort(orderedNodes, 1, n, new Comparator<LabeledNode>() {
				@Override
				public int compare(LabeledNode o1, LabeledNode o2) {
					return o1.getPotential() - o2.getPotential();
				}
			});
		} else {
			orderedNodes = this.g.getVerticesArray();// already ordered but Z can be in the last positions
			int i = n;
			this.Z = this.g.getZ();
			for (i = n; --i >= 0;) {
				if (orderedNodes[i] == this.Z) {
					break;
				}
			}
			if (i > 0) {
				for (int j = i; --j >= 0;) {
					orderedNodes[j + 1] = orderedNodes[j];
				}
				orderedNodes[0] = this.Z;
			}
		}
		Object2IntMap<LabeledNode> nodeRdnIndex = new Object2IntLinkedOpenHashMap<>();
		/**
		 * Filling nodeRdnIndex and reset the potential
		 */
		for (int i = 0; i < n; i++) {
			nodeRdnIndex.put(orderedNodes[i], i);
			orderedNodes[i].setPotential(Constants.INT_POS_INFINITE);
		}
		this.Z.setPotential(0);

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Node ordering for Yen algorithm: " + Arrays.deepToString(orderedNodes));
			}
		}

		NodesToCheck nodesToCheck = new NodesToCheck();
		nodesToCheck.add(this.Z);
		NodesToCheck nodesModified = new NodesToCheck();
		LabeledNode s, d;
		int sIndex, dIndex, dOldValue, value;
		int negativeCheckThreshold = n / 3 + 2;
		int[] parent = new int[n];
		while (!nodesToCheck.isEmpty()) {
			// G-
			for (int i = n - 1; i >= 0; i--) {
				s = orderedNodes[i];
				sIndex = nodeRdnIndex.getInt(s);
				if (sIndex == 0)
					continue;
				ObjectList<STNEdge> edges = (backward) ? this.g.getIncidentEdges(s) : this.g.getOutEdges(s);
				for (STNEdge e : edges) {
					d = (backward) ? this.g.getSource(e) : this.g.getDest(e);
					dOldValue = d.getPotential();
					dIndex = nodeRdnIndex.getInt(d);
					if (dIndex < sIndex && (nodesToCheck.contains(s) || nodesModified.contains(s))) {
						value = Constants.sumWithOverflowCheck(s.getPotential(), e.getValue());
						if (value < dOldValue) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Subgraph G-. Edge: " + e.toString() + ((backward) ? "(reverse it)" : "") + ". " + d.getName() + " value "
													+ Constants.formatInt(dOldValue)
													+ " new value: " + Constants.formatInt(value));
								}
							}
							d.setPotential(value);
							nodesModified.add(d);
							this.checkStatus.propagationCalls++;
							parent[dIndex] = sIndex;
						}
					}
				}
			}
			// G+
			for (int i = 0; i < n; i++) {
				s = orderedNodes[i];
				sIndex = nodeRdnIndex.getInt(s);// i
				if (sIndex == n - 1)
					continue;
				ObjectList<STNEdge> edges = (backward) ? this.g.getIncidentEdges(s) : this.g.getOutEdges(s);
				for (STNEdge e : edges) {
					d = (backward) ? this.g.getSource(e) : this.g.getDest(e);
					dOldValue = d.getPotential();
					dIndex = nodeRdnIndex.getInt(d);
					if (dIndex > sIndex && (nodesToCheck.contains(s) || nodesModified.contains(s))) {
						value = Constants.sumWithOverflowCheck(s.getPotential(), e.getValue());
						if (value < dOldValue) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Subgraph G+. Edge: " + e.toString() + ((backward) ? "(reverse it)" : "") + ". " + d.getName() + " value "
													+ Constants.formatInt(dOldValue)
													+ " new value: " + Constants.formatInt(value));
								}
							}
							d.setPotential(value);
							nodesModified.add(d);
							this.checkStatus.propagationCalls++;
							parent[dIndex] = sIndex;
						}
					}
				}
			}
			nodesToCheck.clear();
			nodesToCheck.addAll(nodesModified);
			nodesModified.clear();
			this.checkStatus.cycles++;

			if (this.checkStatus.cycles > negativeCheckThreshold) {
				if (checkNegativeCycle(parent)) {
					this.checkStatus.consistency = false;
					this.checkStatus.finished = true;
					return false;
				}
			}
		}
		// We maintain negative distance because they are equal to the values presented in distance matrix
		// for (LabeledNode node : orderedNodes) {
		// node.setPotential(-node.getPotential());
		// }
		this.checkStatus.consistency = true;
		this.checkStatus.finished = true;
		return true;
	}

	/**
	 * Makes the STN check and initialization. The STN instance is represented by graph g.
	 * If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level. If the method cannot fix such not-well-defined constraints, it raises a
	 * {@link WellDefinitionException}.
	 * <br>
	 * Since the current DC checking algorithm is complete only if the STN instance contains an upper bound to the distance between Z (the first node) and
	 * each node, this procedure add such upper bound (= #nodes * max weight value) to each node.<br>
	 * <b>Note</b>
	 * This method is necessary for allowing the building of special subclass initAndCheck (in subclasses of subclasses).
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	final boolean coreSTNInitAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Starting initial well definition check.");
			}
		}
		this.g.clearCache();
		this.gCheckedCleaned = null;
		this.Z = this.g.getZ();

		// Checks the presence of Z node!
		// this.Z = this.g.getZ(); already done in setG()
		if (this.Z == null) {
			this.Z = this.g.getNode(STN.ZeroNodeName);
			if (this.Z == null) {
				// We add by authority!
				this.Z = this.g.getNodeFactory().get(STN.ZeroNodeName);
				this.Z.setX(10);
				this.Z.setY(10);
				this.g.addVertex(this.Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + STN.ZeroNodeName + " node found: added!");
				}
			}
			this.g.setZ(this.Z);
		} else {
			if (!this.Z.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
				}
				this.Z.setLabel(Label.emptyLabel);
			}
		}

		// Checks well definiteness of edges and determine maxWeight
		int minNegWeight = 0;
		for (final STNEdge e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Initial Checking edge e: " + e);
				}
			}
			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);

			if (s == d) {
				// loop are not admissible
				this.g.removeEdge(e);
				continue;
			}
			if (e.isEmpty()) {
				this.g.removeEdge(e);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Empty edge " + e + " has been removed.");
					}
				}
				continue;
			}
			int ev = e.getValue();
			if (ev != Constants.INT_NULL && ev < minNegWeight)
				minNegWeight = ev;
		}

		// manage maxWeight value
		this.maxWeight = -minNegWeight;
		// Determine horizon value
		long product = ((long) this.maxWeight) * (this.g.getVertexCount() - 1);// Z doesn't count!
		// if (product >= Constants.INT_POS_INFINITE) {
		// throw new ArithmeticException(
		// "Horizon value is not representable by an integer. maxWeight = " + this.maxWeight + ", #vertices = " + this.g.getVertexCount());
		// }
		this.horizon = (int) product;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "The horizon value is " + String.format("%6d", product));
		}

		/*
		 * Checks well definiteness of nodes
		 */
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {
			// 3. Checks that each node different from Z has an edge to Z
			if (node == this.Z)
				continue;
			boolean added = false;
			STNEdge edge = this.g.findEdge(node, this.Z);
			if (edge == null) {
				edge = makeNewEdge(node.getName() + "_" + this.Z.getName(), ConstraintType.internal);
				this.g.addEdge(edge, node, this.Z);
				edge.setValue(0);
				added = true;
			}
			if (edge.getValue() == Constants.INT_NULL || edge.getValue() > 0) {
				edge.setValue(0);
				added = true;
			}
			if (Debug.ON) {
				if (added) {
					if (LOG.isLoggable(Level.FINE)) {
						LOG.log(Level.FINE,
								"Added " + edge.getName() + ": " + node.getName() + "--(0)-->" + this.Z.getName());
					}
				}
			}
		}

		this.checkStatus.reset();
		this.checkStatus.initialized = true;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Initial well definition check done!");
			}
		}
		return true;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 *
	 * @param name the proposed name. If an edge with name already exists, then name is modified adding an suitable integer such that the name becomes unique
	 *            in 'g'.
	 * @param type the type of edge to create.
	 * @return an edge with a unique name.
	 */
	STNEdge makeNewEdge(final String name, final Edge.ConstraintType type) {
		int i = this.g.getEdgeCount();
		String name1 = name;
		while (this.g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final STNEdge e = this.g.getEdgeFactory().get(name1);
		e.setConstraintType(type);
		return e;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	@SuppressWarnings("deprecation")
	boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			if (this.fInput == null) {
				try (Scanner consoleScanner = new Scanner(System.in)) {
					System.out.print("Insert STN file name (absolute file name): ");
					String fileName = consoleScanner.next();
					this.fInput = new File(fileName);
				}
			}
			if (!this.fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (this.fOutput != null) {
				if (this.fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!this.fOutput.getName().endsWith(".cstn")) {
					this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".cstn"));
				}
				if (this.fOutput.exists()) {
					this.fOutput.delete();
				}
				try {
					this.fOutput.createNewFile();
					this.output = new PrintStream(this.fOutput);
				} catch (final IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				this.output = System.out;
			}
		} catch (final CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass().getName() + " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-*.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " file_name");
			return false;
		}
		return true;
	}

	/**
	 * Resets all internal structures
	 */
	void reset() {
		this.g = null;
		this.Z = null;
		this.maxWeight = 0;
		this.horizon = 0;
		this.checkStatus.reset();
	}
}
