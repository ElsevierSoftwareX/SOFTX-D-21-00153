package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1).
 * In this class is supposed that instances have only negative edges (minimal distance required).
 * Then, it tries to solve using an extended Bellman-Ford algorithm and R0, and R3 rules.<br>
 * 20190226 Found problems:
 * 1) negative q-loop cannot be solved (avoiding positive edges does it solve?) There is an instance for which algorithm does not stop.
 * 2) Is it possible to avoid to reset the counter when R3 is applied? It would eliminate pseudo-polynomiality
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNPotential extends CSTN {

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final public String VERSIONandDATE = "Version 0.1 - February, 20 2019";// Infty value management moved to nodes!

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNPotential.class.getName());

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNPotential(), "Potential DC");
	}

	/**
	 * Graph order
	 */
	int numberOfNodes;

	/**
	 * @param g graph to check
	 */
	public CSTNPotential(LabeledIntGraph g) {
		this();
		this.setG(g);// sets also checkStatus!
	}

	/**
	 * @param g graph to check
	 * @param timeOut timeout for the check
	 */
	public CSTNPotential(LabeledIntGraph g, int timeOut) {
		this(g);
		this.timeOut = timeOut;
	}

	/**
	 * Default constructor.
	 */
	CSTNPotential() {
		super();
	}

	/**
	 * Calls {@link CSTN#initAndCheck()} and, if everything is ok, it tranposes the graph.
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	@Override
	public boolean initAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Starting CSTN potential checking. Init data structures...");
			}
		}
		if (!super.initAndCheck())
			return false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Transpose the distance graph.");
			}
		}
		// To find the minimal distance of all nodes from Z, MultipleSourcesSingleDestination (MSSD),
		// the BellmanFord algorithm is applied to the transposed/reverted graph.
		this.g.transpose();

		this.numberOfNodes = this.g.getVertexCount();

		// LabeledIntGraph g1 = new LabeledIntGraph(this.g, LabeledIntNotMinMap.class);
		// this.g = g1;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Number of nodes: " + this.numberOfNodes);
			}
		}
		this.checkStatus.reset();
		this.checkStatus.initialized = true;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Init done!");
			}
		}
		return true;
	}

	@Override
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {
		throw new RuntimeException("Not applicable.");
	}

	/**
	 * Executes one step of the BellmanFord algorithm.
	 * 
	 * <pre>
	 * A[u,α]---(v,β)-->B
	 * adds
	 * B[(u+v),γ]
	 * where γ=α*β if v<0, γ=αβ otherwise and (u+v) < possibly previous value.
	 * </pre>
	 * 
	 * If (u+v) is the n+1 update of labeled value associated to γ=αβ, then a negative circuit is present and the check is over.
	 * If (u+v) is the n+1 update of labeled value associated to γ=α*β, the a negative q-loop is present and the value (u+v) is set to -∞.
	 * 
	 * @param nodesToCheck input set of nodes
	 * @param obsNodesToCheck modified observation nodes during the call
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	CSTNCheckStatus bellmanFord(final NodesToCheck nodesToCheck, final NodesToCheck obsNodesToCheck, Instant timeoutInstant) {

		LabeledNode B;
		this.checkStatus.cycles++;
		/**
		 * When a new labeled value is added in a potential, then it has to verified w.r.t. all obs potentials to verify if
		 * it can be simplified. obsModified maintains footprint of obs involved in the new added values.
		 */
		Label obsModified = Label.emptyLabel;

		while (!nodesToCheck.isEmpty()) {
			LabeledNode A = nodesToCheck.dequeue();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Considering node " + A.getName());
				}
			}
			// cache
			LabeledIntTreeMap APotential = new LabeledIntTreeMap(A.getPotentialGrounded());
			ObjectSet<Label> APotentialLabel = APotential.keySet();

			NodesToCheck Bsons = new NodesToCheck();
			for (LabeledIntEdge AB : this.g.getOutEdges(A)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "*** Considering edge " + (AB.getName()) + " related to node " + A.getName());
					}
				}
				B = this.g.getDest(AB);
				boolean isBModified = false;
				Bsons.clear();

				for (Entry<Label> ABEntry : AB.getLabeledValueSet()) {
					int v = ABEntry.getIntValue();
					Label beta = ABEntry.getKey();
					for (Label ALabel : APotentialLabel) {
						Label newLabel = (v < 0) ? ALabel.conjunctionExtended(beta) : ALabel.conjunction(beta);
						int newValue = Constants.sumWithOverflowCheck(APotential.get(ALabel), v);
						if (newValue == Constants.INT_NEG_INFINITE && v >= 0)
							continue;
						if (newLabel != null) {
							if (updatePotential(B, newLabel, newValue, false, "")) {
								isBModified = true;
								obsModified = obsModified.conjunctionExtended(newLabel);
								if (!this.checkStatus.consistency) {
									return this.checkStatus;
								}
							}
						}
						// FIXME I try to extend the Bellman-Ford in order to overcome the limitation of v<0 for unknown labeled values
						if (newLabel == null || newLabel.containsUnknown())
							continue;
						for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
							LabeledNode C = this.g.getDest(BC);
							boolean isCModified = false;
							for (Entry<Label> BCEntry : BC.getLabeledValueSet()) {
								int w = BCEntry.getIntValue();
								Label gamma = BCEntry.getKey();
								if (w >= 0)
									continue;
								Label newLabel1 = newLabel.conjunctionExtended(gamma);
								int newValue1 = Constants.sumWithOverflowCheck(newValue, w);
								if (A == C && newValue1 < 0 && newLabel1.containsUnknown()) {
									newValue1 = Constants.INT_NEG_INFINITE;
								}
								// if (newValue1 > 0)
								// continue;
								if (updatePotential(C, newLabel1, newValue1, false, "")) {
									isCModified = true;
									obsModified = obsModified.conjunctionExtended(newLabel1);
									if (!this.checkStatus.consistency) {
										return this.checkStatus;
									}
								}
							}
							if (isCModified) {
								Bsons.enqueue(C);
								if (C.isObserver())
									obsNodesToCheck.enqueue(C);
							}
						}
					}
				}
				if (isBModified) {
					nodesToCheck.enqueue(B);
					if (B.isObserver())
						obsNodesToCheck.enqueue(B);
				}
				while (Bsons.size() != 0) {
					nodesToCheck.enqueue(Bsons.dequeue());
				}
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			} // end for edges outgoing from A
		}

		// add all obs node that have to be checked.
		for (char p : obsModified.getPropositions()) {
			obsNodesToCheck.enqueue(this.g.getObserver(p));
		}

		return this.checkStatus;
	}

	@Override
	boolean potentialR1_2(LabeledNode node, Label label, String log) {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	boolean potentialR3(LabeledNode nS, LabeledNode nD, LabeledIntEdge eSD, ObjectSet<Label> nDPotentialLabel) {
		throw new RuntimeException("Not applicable.");
	}

	/**
	 * Apply R3 to potential of each nodes of the graph.
	 * 
	 * <pre>
	 * if P?[u,α]  and X[v,βp]
	 * then adds
	 * X[max(u,v), α*β]
	 * </pre>
	 * 
	 * @param nodesToCheck the set of nodes that has to be checked w.r.t. the obs node in obsNodesToCheck.
	 * @param obsNodesToCheck the input set of observation time point to consider
	 * @param newNodesToCheck it is fill with nodes modified by this method.
	 * @param timeoutInstant
	 * @return true if at least one node was updated.
	 */
	boolean potentialR3(final LabeledNode[] nodesToCheck, final NodesToCheck obsNodesToCheck, final NodesToCheck newNodesToCheck,
			Instant timeoutInstant) {
		boolean ruleApplied = false;
		Label beta;
		String log = "";
		while (!obsNodesToCheck.isEmpty()) {
			LabeledNode obs = obsNodesToCheck.dequeue();
			char p = obs.getPropositionObserved();
			ObjectSet<Entry<Label>> obsEntrySet = obs.getPotentialGrounded().entrySet();
			for (LabeledNode node : nodesToCheck) {
				if (node == obs)
					continue;

				for (Entry<Label> entryNode : node.getPotentialGrounded().entrySet()) {
					Label betap = entryNode.getKey();
					if (!betap.contains(p))
						continue;
					beta = betap.remove(p);
					int v = entryNode.getIntValue();

					for (Entry<Label> obsEntry : obsEntrySet) {
						Label alpha = obsEntry.getKey();
						int u = obsEntry.getIntValue();
						int max = Math.max(u, v);
						Label alphaBeta = alpha.conjunctionExtended(beta);

						if (this.withNodeLabels) {
							alphaBeta = removeChildrenOfUnknown(alphaBeta);
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								log = "Potential R3 applied to " + obs.getName() + " and " + node.getName()
										+ ":\nsource: "
										+ obs.getName() + "[" + pairAsString(alpha, u) + "] " + node.getName() + "[" + pairAsString(betap, v) + "]\n";
							}
						}
						if (updatePotential(node, alphaBeta, max, true, log)) {
							ruleApplied = true;
							this.checkStatus.potentialCalls[2]++;
							newNodesToCheck.enqueue(node);
							if (!this.checkStatus.consistency)
								return true;
						}
					}
				}
			}
			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return ruleApplied;
			}
		} // for all obs
		return ruleApplied;
	}

	/**
	 * {@link #potentialR3(LabeledNode[], NodesToCheck, NodesToCheck, Instant)} applied only to one node.
	 * 
	 * @see #potentialR3(LabeledNode[], NodesToCheck, NodesToCheck, Instant)
	 * 		@SuppressWarnings("javadoc")
	 *      boolean potentialR3(LabeledNode node, final NodesToCheck obsNodesToCheck, Instant timeoutInstant) {
	 *      return potentialR3(new LabeledNode[] { node }, obsNodesToCheck, obsNodesToCheck, timeoutInstant);
	 *      }
	 */

	@Override
	boolean potentialR3_4_5_6(LabeledNode nY, NodesToCheck newNodesToCheck, EdgesToCheck newEdgesToCheck) {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	boolean potentialR6(LabeledNode nS, LabeledNode nD, Label alpha, int value, LabeledIntEdge eSD, String preLog) {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	CSTNCheckStatus dynamicConsistencyCheckWOInit() {
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("Graph has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}

		LabeledNode[] allNodes = this.g.getVerticesArray();

		NodesToCheck nodesToCheck = new NodesToCheck();
		nodesToCheck.enqueue(this.Z);
		this.Z.putPotential(Label.emptyLabel, 0);

		NodesToCheck obsNodesToCheck = new NodesToCheck();

		int i = 1;
		Instant startInstant = Instant.now();
		Instant timeoutInstant = startInstant.plusSeconds(this.timeOut);

		while (this.checkStatus.consistency && !this.checkStatus.finished && !this.checkStatus.timeout) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** Start Main Cycle " + i + " ***");
				}
			}

			bellmanFord(nodesToCheck, obsNodesToCheck, timeoutInstant);
			// ASSERTIONS
			// nodesToCheck is empty
			// obsNodesToCheck may be not empty

			if (!this.checkStatus.consistency || this.checkStatus.timeout) {
				break;
			}

			potentialR3(allNodes, obsNodesToCheck, nodesToCheck, timeoutInstant);

			if (!this.checkStatus.finished) {
				this.checkStatus.finished = nodesToCheck.size() == 0;
			}

			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** End Main Cycle " + i + " ***\n\n");
				}
			}
			i++;
		} // end checking

		if (this.checkStatus.timeout) {
			if (Debug.ON) {
				String msg = "During the check # " + i + ", " + this.timeOut + " seconds timeout occured. ";
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, msg);
				}
			}
			this.saveGraphToFile();
			this.checkStatus.executionTimeNS = ChronoUnit.NANOS.between(startInstant, Instant.now());
			return this.checkStatus;
		}

		Instant endInstant = Instant.now();
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an inconsistency.\nStatus: " + this.checkStatus);
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
				LOG.log(Level.INFO,
						"Stable state reached. Number of cycles: " + (i - 1) + ".\nStatus: " + this.checkStatus);
			}
		}
		this.g.reverse();
		this.gCheckedCleaned = new LabeledIntGraph(this.g.getName(), this.g.getInternalLabeledValueMapImplementationClass());
		this.gCheckedCleaned.copyCleaningRedundantLabels(this.g);
		this.saveGraphToFile();
		return this.checkStatus;
	}

	@Override
	boolean labelModificationR0qR0(final LabeledNode nObs, final LabeledNode nX, final LabeledIntEdge eObsX) {
		throw new UnsupportedOperationException("labelModificationR0qR0");
	}

	@Override
	Label labelModificationR0qR0Light(final LabeledNode nP, final LabeledNode nX, final Label alpha, int w) {
		throw new UnsupportedOperationException("labelModificationR0qR0Light");
	}

	@Override
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final LabeledIntEdge eSD) {
		throw new UnsupportedOperationException("labelModificationR3qR3");
	}

	@Override
	boolean labelPropagation(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB, final LabeledIntEdge eBC,
			LabeledIntEdge eAC) {
		throw new UnsupportedOperationException("labelPropagation");
	}

	@Override
	CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck edgesToCheck, final NodesToCheck nodesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdges");
	}

	@Override
	CSTNCheckStatus oneStepDynamicConsistencyByEdgesLimitedToZ(EdgesToCheck edgesToCheck, NodesToCheck nodesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdgesLimitedToZ");
	}

	/**
	 * Updates the labeled value <code>(value, label)</code> in the potential of <code>node</code>.<br>
	 * Rule R0: If <code>node</code> is an observation t.p., then <code>label</code> is cleaned removing a possible observed literal.<br>
	 * If <code>(value, label)</code> has already updated for more than #nodes times and label does <b>not</b> contain unknown literals,
	 * it means that a negative circuit is present and the check is stopped.<br>
	 * If <code>(value, label)</code> has already updated for more than #nodes times and label does contain unknown literals,
	 * then value is set to -∞.
	 * 
	 * @param node
	 * @param newLabel
	 * @param newValue it is assumed that it is != {@link Constants#INT_NULL}
	 * @param reset true if the count has to be reset.
	 * @param log
	 * @return true if the value was added.
	 */
	boolean updatePotential(LabeledNode node, Label newLabel, int newValue, boolean reset, String log) {
		// R0
		if (node.isObserver()) {
			newLabel = newLabel.remove(node.getPropositionObserved());
		}

		int currentValue = node.getPotentialGrounded(newLabel);
		if (node.putPotential(newLabel, newValue)) {
			// the value was added
			int count = node.updatePotentialCount(newLabel, reset || currentValue == Constants.INT_NULL);//
			if (count == Constants.INT_NULL)
				count = 0;
			count++;
			if (count > this.numberOfNodes) {
				newValue = Constants.INT_NEG_INFINITE;
				node.putPotential(newLabel, newValue);
			}
			if (Debug.ON) {
				log += "Update potential on " + node.getName()
						+ ": " + pairAsString(newLabel, currentValue) + " replaced by " + pairAsString(newLabel, newValue) + ". Update #" + count + "\n";
			}
			if (!newLabel.containsUnknown()) {
				if (newValue == Constants.INT_NEG_INFINITE
						|| (newValue < 0 && node == this.Z)) {
					// found a negative cycle!
					log += "\n***\nFound a negative loop in node " + node + "\n***";
					this.checkStatus.consistency = false;
					this.checkStatus.finished = true;
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, log);
				}
			}
			this.checkStatus.potentialCalls[0]++;
			return true;
		}
		return false;
	}

}
