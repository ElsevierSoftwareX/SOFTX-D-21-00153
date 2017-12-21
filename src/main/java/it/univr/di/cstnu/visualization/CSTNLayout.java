package it.univr.di.cstnu.visualization;

/**
 * [07/02/2012] Made serializable by Posenato
 */

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Function;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.Graph;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;

/**
 * Allow to layout CSTN(U) determined by transforming workflows generated by Atapis Random generator tool.
 * Such class is highly depended on the name conventions used in Atapis Random generator tool.
 * So, it is not general and it cannot be used in other contexts.
 *
 * @param <E> edge type
 * @version $Id: $Id
 */
public class CSTNLayout<E> extends edu.uci.ics.jung.algorithms.layout.StaticLayout<LabeledNode, E> implements IterativeContext {

	/**
	 * half of the yShiftA length
	 */
	private static int halfLength;

	/**
	 * 
	 */
	private static Logger LOG = Logger.getLogger(CSTNLayout.class.getName());

	/**
	 * It is used for getting the coordinates of node stored inside LabelNode object.
	 */
	static public Function<LabeledNode, Point2D> positionInitializer = new Function<LabeledNode, Point2D>() {
		@Override
		public Point2D apply(final LabeledNode v) {
			final Point2D p = new Point2D.Double(v.getX(), v.getY());
			return p;
		}
	};

	/**
	 *
	 */
	@SuppressWarnings("unused")
	private static final long serialVersionUID = 1L;

	/**
	 * Version
	 */
	static final public String VERSIONandDATE = CSTNLayout.class.getName() + ". Version  1.0 - October, 20 2017";

	/**
	 * half of y shift for next node if not particular condition applies.
	 */
	private double halfYShift;

	/**
	 * X position first node.
	 */
	private double initialX = 25;

	/**
	 * Y position first node.
	 */
	private double initialY = 200;

	/**
	 * max X
	 */
	private double maxX;

	/**
	 * max Y
	 */
	private double maxY;

	/**
	 * x shift for next node
	 */
	private double xShift = 100;

	/**
	 * y shift for next node if not particular condition applies.
	 */
	private double yShift = 100;

	/**
	 * array of possible y shifts.
	 */
	private double[] yShiftA;

	/**
	 * Current layout from which take current node positions.
	 */
	private AbstractLayout<LabeledNode, E> currentLayout = null;

	/**
	 * Creates an instance for the specified graph and default size; vertex locations are determined by {@link #positionInitializer}.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 */
	public CSTNLayout(final Graph<LabeledNode, E> graph) {
		super(graph);
	}

	/**
	 * Creates an instance for the specified graph and size.
	 *
	 * @param graph a {@link edu.uci.ics.jung.graph.Graph} object.
	 * @param size a {@link java.awt.Dimension} object.
	 */
	public CSTNLayout(final Graph<LabeledNode, E> graph, final Dimension size) {
		super(graph, positionInitializer, size);
	}

	/**
	 * @param firstNode
	 * @param firstNodeX
	 * @param firstNodeY
	 * @param g
	 * @param nSplits
	 * @return the set of nodes that have been laid out.
	 */
	public ObjectSet<LabeledNode> draw(LabeledNode firstNode, double firstNodeX, double firstNodeY, LabeledIntGraph g, int nSplits) {
		if (firstNode == null)
			return null;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finest("Node root " + firstNode.getName() + ": (" + firstNodeX + ", " + firstNodeY + ")");
			}
		}
		firstNode.setX(firstNodeX);
		firstNode.setY(firstNodeY);

		// Breadth-first traversal
		ObjectArrayFIFOQueue<LabeledNode> queue = new ObjectArrayFIFOQueue<>();
		ObjectSet<LabeledNode> marked = new ObjectOpenHashSet<>(g.getVertexCount());
		queue.enqueue(firstNode);
		marked.add(firstNode);
		while (!queue.isEmpty()) {
			LabeledNode node = queue.dequeue();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("DRAW. Queue length: %4d. Marked size: %4d.", queue.size(), marked.size()));
				}
			}
			double xNode = node.getX();
			double yNode = node.getY();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finest("Node father " + node.getName() + ": (" + xNode + ", " + yNode + ")");
				}
			}

			ObjectList<LabeledIntEdge> inEdge;
			String nodeName = node.getName();
			if (nodeName.endsWith("S")) {
				// Consider only the corresponding node
				String adjName = nodeName.substring(0, nodeName.length() - 1) + "E";
				LabeledIntEdge e = g.findEdge(adjName, nodeName);
				if (e == null && nodeName.equals("1S"))// in some graph, 1E has been replaced by Ω
					e = g.findEdge("Ω", nodeName);
				inEdge = new ObjectArrayList<>();
				if (e != null) {
					inEdge.add(e);
				} else {
					adjName = "X" + adjName + "?";
					e = g.findEdge(adjName, nodeName);
					if (e != null) {
						inEdge.add(e);
					} else {
						throw new RuntimeException("Node name unplanned");
					}
				}
			} else {
				inEdge = (ObjectList<LabeledIntEdge>) g.getInEdges(node);
			}
			ObjectIterator<LabeledIntEdge> eIte = inEdge.iterator();
			while (eIte.hasNext()) {
				LabeledIntEdge e = eIte.next();
				if (e.getMinValue() > 0
						|| (e.getConstraintType() != LabeledIntEdge.ConstraintType.contingent && e.getConstraintType() != LabeledIntEdge.ConstraintType.normal)
						|| (e.getConstraintType() == LabeledIntEdge.ConstraintType.contingent && e.lowerCaseValueSize() > 0)) {
					eIte.remove();
				}
			}
			int i = halfLength - inEdge.size() / 2;
			for (LabeledIntEdge e : inEdge) {
				LabeledNode adjacent = g.getSource(e);
				if (marked.contains(adjacent)) {
					// the adjacent has been already laid out.
					if (adjacent.getX() <= xNode || adjacent.getX() >= xNode + this.xShift) {
						if (nodeName.endsWith("w1") || nodeName.endsWith("w0")
								|| node.getLabel().equals(adjacent.getLabel())
								|| (node.getLabel().subsumes(adjacent.getLabel()) && !adjacent.getLabel().subsumes(node.getLabel()))) {
							double newY;
							if (adjacent.getY() > yNode + this.halfYShift) {
								newY = (yNode + (adjacent.getY() - this.halfYShift - yNode) / 2);
								if (newY > this.maxY)
									this.maxY = newY;
							} else {
								if (adjacent.getY() < yNode - this.halfYShift) {
									newY = (yNode - (yNode - adjacent.getY() + this.halfYShift) / 2);
									if (newY > this.maxY)
										this.maxY = newY;
								} else {
									newY = adjacent.getY();
								}
							}
							double newX = ((adjacent.getX() <= xNode) ? xNode + this.xShift : adjacent.getX());
							if (adjacent.getY() == newY && adjacent.getX() == newX)
								continue;
							if (newX > this.maxX)
								this.maxX = newX;
							redraw(adjacent, newX, newY, g, marked, nSplits);
						}
					}
					continue;
				}
				double x = xNode + this.xShift;
				double y;
				if (node.isObserver() || nodeName.endsWith("E AND SPLIT")) {
					if (i == halfLength)
						i++; // no child straight under the father.
					y = yNode + this.yShiftA[i++] * nSplits;
				} else {
					if (nodeName.endsWith("w1") || nodeName.endsWith("w0")
							|| (node.getLabel().subsumes(adjacent.getLabel()) && !adjacent.getLabel().subsumes(node.getLabel()))) {
						y = yNode + this.halfYShift;
					} else {
						y = yNode + this.yShiftA[i++];// if there is only one adjacent, this.yShiftA[i++] is 0.
					}
				}

				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finest("Node adjacent " + adjacent.getName() + ": (" + x + ", " + y + ")");
					}
				}
				adjacent.setX(x);
				adjacent.setY(y);
				if (y > this.maxY)
					this.maxY = y;
				if (x > this.maxX)
					this.maxX = x;
				marked.add(adjacent);
				queue.enqueue(adjacent);
			}
			if (node.isObserver() || nodeName.endsWith("E AND SPLIT"))
				nSplits--;
		}
		return marked;
	}

	/**
	 * Relocate some marked adjacent nodes of given node because node has been relocated.
	 * The relocation is realized by shifting nodes as much as the given node has been shifted.
	 * 
	 * @param firstNode
	 * @param nodeX
	 * @param nodeY
	 * @param g
	 * @param marked
	 * @param nObs
	 */
	private void redraw(LabeledNode firstNode, double nodeX, double nodeY, LabeledIntGraph g, ObjectSet<LabeledNode> marked, int nObs) {
		if (firstNode == null)
			return;
		double shiftX = nodeX - firstNode.getX();
		double shiftY = nodeY - firstNode.getY();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finest("Shift to apply: (" + shiftX + ", " + shiftY + ")");
			}
		}
		ObjectArrayFIFOQueue<LabeledNode> queue = new ObjectArrayFIFOQueue<>();
		ObjectSet<LabeledNode> markedInternal = new ObjectOpenHashSet<>();
		queue.enqueue(firstNode);
		markedInternal.add(firstNode);
		while (!queue.isEmpty()) {
			LabeledNode node = queue.dequeue();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finer(String.format("REDRAW. Queue length: %4d. Marked size: %4d.", queue.size(), marked.size()));
				}
			}
			if (!marked.contains(node)) {
				continue;
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("Relocated node " + node.getName() + ": (" + node.getX() + ", " + node.getY() + ")-->" + "(" + (node.getX() + shiftX) + ", "
							+ (node.getY() + shiftY) + ")");
				}
			}
			node.setX(node.getX() + shiftX);
			node.setY(node.getY() + shiftY);
			ObjectList<LabeledIntEdge> inEdge = (ObjectList<LabeledIntEdge>) g.getInEdges(node);
			ObjectIterator<LabeledIntEdge> eIte = inEdge.iterator();
			while (eIte.hasNext()) {
				LabeledIntEdge e = eIte.next();
				if ((e.getConstraintType() != LabeledIntEdge.ConstraintType.contingent && e.getConstraintType() != LabeledIntEdge.ConstraintType.normal)
						|| e.getMinValue() > 0 || (e.getConstraintType() == LabeledIntEdge.ConstraintType.contingent && e.lowerCaseValueSize() > 0)) {
					eIte.remove();
				}
			}
			double minDistanceAdjNode = Constants.INT_POS_INFINITE;
			for (LabeledIntEdge e : inEdge) {
				LabeledNode adjacent = g.getSource(e);
				if (!marked.contains(adjacent))
					continue;
				double distanceAdjNode = (adjacent.getX() + shiftX) - node.getX();
				if (distanceAdjNode < minDistanceAdjNode)
					minDistanceAdjNode = distanceAdjNode;
				queue.enqueue(adjacent);
			}
			if (minDistanceAdjNode > this.xShift)
				shiftX = shiftX - minDistanceAdjNode + this.xShift;
			if (shiftX < this.xShift)
				shiftX = this.xShift;
		}
	}

	/**
	 * @return the initialX
	 */
	public double getInitialX() {
		return this.initialX;
	}

	/**
	 * @return the initialY
	 */
	public double getInitialY() {
		return this.initialY;
	}

	/**
	 * @return the xShift
	 */
	public double getxShift() {
		return this.xShift;
	}

	/**
	 * @return the yShift
	 */
	public double getyShift() {
		return this.yShift;
	}

	@Override
	public void initialize() {
		LabeledIntGraph g = (LabeledIntGraph) this.graph;
		LabeledNode Z = g.getZ();
		// Approximate number of AND split = (n-6*obs -5)/6;
		int nAnd = (g.getVertexCount() - 6 * g.getObserverCount() - 5) / 6;
		int nSplit = g.getObserverCount() + nAnd;
		this.yShiftA = new double[9];
		halfLength = this.yShiftA.length / 2;
		double y = -this.yShift / 2 * 4;
		for (int i = 0; i < 9; i++) {
			this.yShiftA[i] = y;
			y += this.yShift / 2;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finest("yShiftA = " + Arrays.toString(this.yShiftA));
			}
		}

		this.halfYShift = this.yShift / 2;
		this.maxX = this.initialX;
		this.maxY = this.initialY;

		Collection<LabeledNode> allNodes = g.getVertices();
		if (this.currentLayout!=null) {
			for (LabeledNode node : allNodes) {
				node.setX(this.currentLayout.getX(node));
				node.setY(this.currentLayout.getY(node));
			}
		}
		/*
		 * Draw the graph
		 */
		ObjectSet<LabeledNode> marked = draw(Z, this.initialX, this.initialY, g, nSplit);
		while (!marked.containsAll(allNodes)) {
			LabeledNode otherZ = Z;
			for (LabeledNode node : allNodes) {
				if (!marked.contains(node)) {
					otherZ = node;
					break;
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.finest("A node has not laid out because has not negative out outgoing edges: " + otherZ
							+ "\nStart a new layout phase for such nodes and its successors.");
				}
			}
			marked.addAll(draw(otherZ, otherZ.getX(), otherZ.getY(), g, nSplit));
		}

		// check if some node has a negative y
		double negativeY = this.halfYShift;
		for (LabeledNode node : g.getVertices()) {
			double lY = node.getY();
			if (lY < this.halfYShift) {
				if (lY < negativeY) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.finest("A negative value for y coordinate found: " + lY + ". It belongs to node " + node.getName());
						}
					}
					negativeY = lY;
				}
			}
		}
		if (negativeY < this.halfYShift) {
			// it is necessary to shift every thing
			negativeY = (negativeY < 0) ? negativeY = -negativeY + this.yShift / 2 : this.yShift / 2;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.info("It is necessary to translate the graph of " + negativeY + " pixels.");
				}
			}
			for (LabeledNode node : g.getVertices()) {
				double lY = node.getY();
				node.setY(lY + negativeY);
			}
			this.maxY = this.maxY + negativeY;
		}

		this.size = new Dimension((int) (this.maxX + this.xShift / 4), (int) (this.maxY + this.yShift / 4));
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finest("This size is " + this.size + " pixels.");
			}
		}
	}

	/**
	 * @param initialX the initialX to set
	 */
	public void setInitialX(int initialX) {
		this.initialX = initialX;
	}

	/**
	 * @param initialY the initialY to set
	 */
	public void setInitialY(int initialY) {
		this.initialY = initialY;
	}

	/**
	 * @param xShift the xShift to set
	 */
	public void setxShift(int xShift) {
		this.xShift = xShift;
	}

	/**
	 * @param yShift the yShift to set
	 */
	public void setyShift(int yShift) {
		this.yShift = yShift;
		this.halfYShift = yShift / 2;
	}

	/**
	 * @param currentLayout the currentLayout to set
	 */
	public void setCurrentLayout(AbstractLayout<LabeledNode, E> currentLayout) {
		this.currentLayout = currentLayout;
	}

	@Override
	public String toString() {
		return CSTNLayout.VERSIONandDATE;
	}

	@Override
	public void step() {
		// no action
	}

	@Override
	public boolean done() {
		return false;
	}

}