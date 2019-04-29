package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard IR semantics (cf. ICAPS 2016 paper, table 1).
 * and that the instance is streamlined (cf TIME 2018 paper).
 * In this class the DC checking is solved using an extended Shortest Paths Faster Algorithm (SPFA)*, and R0-R3 rules.<br>
 * *Moore, Edward F. (1959).
 * "The shortest path through a maze".
 * Proceedings of the International Symposium on the Theory of Switching. Harvard University Press. pp. 285–292
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNSPFA extends CSTNIR {

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final public String VERSIONandDATE = "Version 0.1 - February, 20 2019";
	// static final public String VERSIONandDATE = "Version 0.2 - March, 31 2019";// It extends CSTNIR. I proved that pseudo-polynomiality cannot be avoid.
	static final public String VERSIONandDATE = "Version 2.0 - April, 26 2019";// During the init, all nodes in the negative subpath of a negative q-loop will
																				// be identified by putting -∞ in their potential.
	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNSPFA.class.getName());

	/**
	 * Checks the extended graph
	 */
	static final boolean PREPROCESSING_GRAPH_FINDING_POTENTIALS_OF_QLOOP_NODES = true;

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNSPFA(), "Potential DC");
	}

	/**
	 * Graph order
	 */
	int numberOfNodes;

	/**
	 * @param g graph to check
	 */
	public CSTNSPFA(LabeledIntGraph g) {
		super(g);
		// this.withNodeLabels = false;
	}

	/**
	 * @param g graph to check
	 * @param timeOut timeout for the check
	 */
	public CSTNSPFA(LabeledIntGraph g, int timeOut) {
		super(g, timeOut);
		// this.withNodeLabels = false;
	}

	/**
	 * Default constructor.
	 */
	CSTNSPFA() {
		super();
		// this.withNodeLabels = false;
	}

	/**
	 * Calls {@link CSTN#initAndCheck()} and, if everything is ok, it transposes the graph.
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

		if (PREPROCESSING_GRAPH_FINDING_POTENTIALS_OF_QLOOP_NODES) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Starting completition of graph with edges having negative weights...");
				}
			}
			// Completes the graph adding only new negative edges for n round.
			// In this way it should find negative cycles.
			ObjectArrayList<LabeledIntEdge> edgesToCheck = new ObjectArrayList<>(this.g.getEdges());
			ObjectArrayList<LabeledIntEdge> newEdgesToCheck = new ObjectArrayList<>();

			int n = this.g.getVertexCount();
			boolean noFirstRound = false;
			int negInftyPotentialCount = 0;
			while (edgesToCheck.size() > 0 && n-- > 0) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "***Cycle countdown: " + n);
					}
				}
				for (LabeledIntEdge edgeAB : edgesToCheck) {
					LabeledNode A = this.g.getSource(edgeAB);
					LabeledNode B = this.g.getDest(edgeAB);
					for (LabeledIntEdge edgeBC : this.g.getOutEdges(B)) {
						LabeledNode C = this.g.getDest(edgeBC);
						LabeledIntEdge edgeAC = this.g.findEdge(A, C);

						// Propagate only to new edges.
						// At first round, even an already defined edge has to be considered new.
						if (edgeAC == null) {
							edgeAC = makeNewEdge("∞", LabeledIntEdge.ConstraintType.derived);
						} else {
							if (noFirstRound)
								continue;
						}

						if (labelPropagation(A, B, C, edgeAB, edgeBC, edgeAC)) {
							if (!this.checkStatus.consistency) {
								this.checkStatus.initialized = true;
								this.checkStatus.finished = true;
								return true;
							}

							if (A != C && !edgeAC.isEmpty()) {
								newEdgesToCheck.add(edgeAC);
							}
							if (Debug.ON) {
								if (A == C) {
									negInftyPotentialCount++;
								}
							}
						}
					}
				}
				edgesToCheck = newEdgesToCheck;
				newEdgesToCheck = new ObjectArrayList<>();
				noFirstRound = true;
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "All possible -∞ potential found. They are " + negInftyPotentialCount + ".");
				}
			}

			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Done.");
				}
			}
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Transpose the distance graph.");
			}
		}
		// To find the minimal distance of all nodes from Z, MultipleSourcesSingleDestination (MSSD),
		// the BellmanFord algorithm is applied to the transposed/reverted graph.
		this.g.transpose();

		this.numberOfNodes = this.g.getVertexCount();

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
	 * Instantaneous reaction semantics is assumed.
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
		/**
		 * When a new labeled value is added in a potential, then it has to checked w.r.t. all observation potentials for verifying whether
		 * it can be simplified. obsModified maintains footprint of observation t.p. involved in the new added values.
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
			LabeledIntMap APotential = A.getPotential();
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
					for (Label alpha : APotentialLabel) {
						int u = APotential.get(alpha);
						if (u == Constants.INT_NULL) {
							continue; // It could occur that APotential is modified during the cycle (look ahead).
							// So, it is necessary to check if the value is still present
						}
						Label newLabel = (v < 0) ? alpha.conjunctionExtended(beta) : alpha.conjunction(beta);// IR assumed.
						int newValue = Constants.sumWithOverflowCheck(u, v);
						if (newValue == Constants.INT_NEG_INFINITE && v > 0)// do not propagate -∞ through positive edges
							continue;
						String log = "";
						if (newLabel != null) {
							log = A.getName() + "[" + pairAsString(alpha, u) + "]--" + pairAsString(beta, v) + "-->" + B.getName();
							if (updatePotential(B, newLabel, newValue, false, log + "\n")) {
								isBModified = true;
								obsModified = obsModified.conjunctionExtended(newLabel);
								this.checkStatus.labeledValuePropagationCalls++;
								if (!this.checkStatus.consistency) {
									return this.checkStatus;
								}
							}
						}
						if (!PREPROCESSING_GRAPH_FINDING_POTENTIALS_OF_QLOOP_NODES) {
							// Here Bellman-Ford is extended in order to overcome the limitation of v < 0 for unknown labeled values
							// when PREPROCESSING_GRAPH_FINDING_POTENTIALS_OF_QLOOP_NODES, all possible -∞ for qloops are already present in the node
							// potentials.
							// so, the following extension is useless.
							if (newLabel == null || newLabel.containsUnknown())
								continue;
							for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
								LabeledNode C = this.g.getDest(BC);
								boolean isCModified = false;
								for (Entry<Label> BCEntry : BC.getLabeledValueSet()) {
									int w = BCEntry.getIntValue();
									if (w >= 0)
										continue;
									Label gamma = BCEntry.getKey();
									int vw = Constants.sumWithOverflowCheck(v, w);
									if (u == Constants.INT_NEG_INFINITE && vw > 0)
										continue;
									Label newLabel1 = newLabel.conjunctionExtended(gamma);
									int newValue1 = Constants.sumWithOverflowCheck(u, vw);
									if (A == C && newValue1 < 0 && newLabel1.containsUnknown()) {
										newValue1 = Constants.INT_NEG_INFINITE;
									}
									String log1 = log + "--" + pairAsString(gamma, w) + "-->" + C.getName() + "\n";
									if (updatePotential(C, newLabel1, newValue1, false, log1)) {
										isCModified = true;
										obsModified = obsModified.conjunctionExtended(newLabel1);
										this.checkStatus.labeledValuePropagationCalls++;
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
			this.checkStatus.cycles++;
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
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final LabeledIntEdge eSD) {
		throw new UnsupportedOperationException("labelModificationR3qR3");
	}

	/**
	 * This version is a restrict version of
	 * {@link CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge)}.
	 * Here no call to {@link CSTN#potentialR6(LabeledNode, LabeledNode, Label, int, LabeledIntEdge, String)} and to CSTN#checkAndApplyUnknownAfterObs(nC,
	 * newLabelAC, sum, log) is made.
	 */
	@Override
	boolean labelPropagation(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB, final LabeledIntEdge eBC,
			LabeledIntEdge eAC) {
		// * Be careful, in order to propagate correctly possibly -∞ self-loop, it is necessary call this method also for triple like with nodes A == B or B==C!
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false;
		Label nAnCLabel = null;
		if (this.withNodeLabels) {
			nAnCLabel = nA.getLabel().conjunction(nC.getLabel());
			if (nAnCLabel == null)
				return false;
		}

		boolean nAisAnObserver = nA.isObserver();
		char proposition = nA.getPropositionObserved();

		ObjectSet<Object2IntMap.Entry<Label>> setToReuse = new ObjectArraySet<>();
		String firstLog = "Labeled Propagation Rule considers edges " + eAB.getName() + ", " + eBC.getName() + " for " + eAC.getName();
		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			final int u = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet(setToReuse)) {
				final int v = BCEntry.getIntValue();
				int sum = Constants.sumWithOverflowCheck(u, v);
				if (sum > 0) {
					// // It is not necessary to propagate positive values.
					// // Less propagations, less useless labeled values.
					// 2018-01-25: I verified that for some negative instances, avoiding the positive propagations can increase the execution time.
					// For positive instances, surely avoiding the positive propagations shorten the execution time.
					continue;
				}
				final Label labelBC = BCEntry.getKey();
				boolean qLabel = false;
				Label newLabelAC = null;
				if (mainConditionForRestrictedLP(u, v)) {// Even if we published that when nC == Z, the label must be consistent, we
					// also showed (but not published that if u<0, then label can contains unknown even when nC==Z.
					newLabelAC = labelAB.conjunction(labelBC);
					if (newLabelAC == null) {
						continue;
					}
				} else {
					newLabelAC = (this.withUnknown) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
					if (newLabelAC == null)
						continue;
					qLabel = newLabelAC.containsUnknown();
					if (qLabel && this.withNodeLabels) {
						newLabelAC = removeChildrenOfUnknown(newLabelAC);
						qLabel = newLabelAC.containsUnknown();
					}
				}
				if (this.withNodeLabels) {
					if (!newLabelAC.subsumes(nAnCLabel)) {
						if (Debug.ON)
							LOG.log(Level.FINEST,
									"New alphaBeta label " + newLabelAC + " does not subsume node labels " + nAnCLabel + ". New value cannot be added.");
						continue;
					}
				}

				int oldValue = eAC.getValue(newLabelAC);

				// Prepare the log in advance in order to avoid repetition
				String log = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = firstLog
								+ "\nsource: " + nA.getName() + " ---" + pairAsString(labelAB, u) + "⟶ "
								+ nB.getName() + " ---" + pairAsString(labelBC, v) + "⟶ " + nC.getName();
					}
				}
				if (nA == nC) {
					if (sum < 0 && potentialR1_2(nA, newLabelAC, log)) {// sum can be 0!
						// The labeled value is negative and label is in Q*.
						// The -∞ value is now stored on node A (==C) as potential value if label is in Q*/P*, otherwise, a negative loop has been found!
						ruleApplied = true;
						this.checkStatus.labeledValuePropagationCalls++;
						if (!this.checkStatus.consistency)
							return true;
					}
					continue;
				} // end if nA==nC

				// in the case of A != C
				if (oldValue != Constants.INT_NULL && sum >= oldValue) {
					// a value is stored only if it is more negative than the current one.
					continue;
				}

				// here sum has to be add!
				if (eAC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					this.checkStatus.labeledValuePropagationCalls++;
					// // R0qR0 rule
					if (nAisAnObserver && newLabelAC.contains(proposition)) {
						Label newLabelAC1 = labelModificationR0qR0Light(nA, nC, newLabelAC, sum);
						if (!newLabelAC1.equals(newLabelAC)) {
							newLabelAC = newLabelAC1;
							eAC.mergeLabeledValue(newLabelAC, sum);
						}
					}
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							log += "\nresult: "
									+ nA.getName() + " ---" + pairAsString(newLabelAC, sum) + "⟶ " + nC.getName()
									+ "; old value: " + Constants.formatInt(oldValue) + "\n";
							LOG.log(Level.FINER, log);
							firstLog = "";
						}
					}
				}
			}
		}
		return ruleApplied;
	}

	@Override
	CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck edgesToCheck, final NodesToCheck nodesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdges");
	}

	@Override
	CSTNCheckStatus oneStepDynamicConsistencyByEdgesLimitedToZ(EdgesToCheck edgesToCheck, NodesToCheck nodesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdgesLimitedToZ");
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

		// First of all, check all obs node among them in order to have the minimum common potential among obs t.p.
		ruleApplied = potentialR3internalCycle(obsNodesToCheck.toArray(), new NodesToCheck(obsNodesToCheck), newNodesToCheck, true, timeoutInstant);
		if (!this.checkStatus.consistency)
			return true;
		if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
			return ruleApplied;
		}

		// Secondly, check the nodesToCheck w.r.t. obs nodes
		ruleApplied |= potentialR3internalCycle(nodesToCheck, obsNodesToCheck, newNodesToCheck, false, timeoutInstant);

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
	boolean potentialR3_4_5_6(LabeledNode nY, boolean limitToZ, NodesToCheck newNodesToCheck, EdgesToCheck newEdgesToCheck) {
		throw new RuntimeException("Not applicable.");
	}

	@Override
	boolean potentialR6(LabeledNode nS, LabeledNode nD, Label alpha, int value, LabeledIntEdge eSD, String preLog) {
		throw new RuntimeException("Not applicable.");
	}

	/**
	 * @param nodesToCheck
	 * @param obsNodes
	 * @param newNodesToCheck
	 * @param obsAlignment
	 * @param timeoutInstant
	 * @return true if at least a value has been modified.
	 */
	private boolean potentialR3internalCycle(final LabeledNode[] nodesToCheck, final NodesToCheck obsNodes, final NodesToCheck newNodesToCheck,
			boolean obsAlignment, Instant timeoutInstant) {
		boolean ruleApplied = false;
		String log = "";
		while (!obsNodes.isEmpty()) {
			LabeledNode obs = obsNodes.dequeue();
			char p = obs.getPropositionObserved();
			ObjectSet<Entry<Label>> obsEntrySet = obs.getPotential().entrySet();
			for (LabeledNode node : nodesToCheck) {
				if (node == obs)
					continue;
				int minNodeValue = node.getPotential(Label.emptyLabel);
				if (minNodeValue == Constants.INT_NULL)
					minNodeValue = Constants.INT_POS_INFINITE;
				for (Label betap : node.getPotential().keySet()) {
					int v = node.getPotential(betap);
					if (v == Constants.INT_NULL || !betap.contains(p))
						continue;
					Label beta = betap.remove(p);
					for (Entry<Label> obsEntry : obsEntrySet) {
						Label alpha = obsEntry.getKey();
						int u = obsEntry.getIntValue();
						if (u >= minNodeValue)
							continue;
						int max = Math.max(u, v);
						Label alphaBeta = alpha.conjunctionExtended(beta);

						// if (this.withNodeLabels) {
						// alphaBeta = removeChildrenOfUnknown(alphaBeta);
						// }
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
							if (obsAlignment)
								obsNodes.enqueue(node);
							newNodesToCheck.enqueue(node);// in any case a modified obsNode has to be rechecked using bellmanFord method.
							if (!this.checkStatus.consistency)
								return true;
						}
					}

				}
			}
		}
		if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
			return ruleApplied;
		}
		return ruleApplied;
	}

	/**
	 * Updates the labeled value <code>(value, label)</code> in the potential of <code>node</code>.<br>
	 * Rule R0: If <code>node</code> is an observation t.p., then <code>label</code> is cleaned removing a possible observed literal.<br>
	 * If <code>(value, label)</code> has already updated for more than #nodes times and label does <b>not</b> contain unknown literals,
	 * it means that a negative circuit is present and the check is stopped.<br>
	 * If <code>(value, label)</code> has already updated for more than #nodes times and label does contain unknown literals,
	 * then value is set to -∞.
	 * If <code>(value, label)</code> has been set by rule R3, then a secondary counter is incremented for counting how many time R3 adjusts
	 * such value. If such second counter is greater than #nodes, then the value is update to obsValue.
	 * 
	 * @param node
	 * @param newLabel
	 * @param newValue it is assumed that it is != {@link Constants#INT_NULL}
	 * @param fromR3 true if the count has to be reset.
	 * @param log
	 * @return true if the value was added.
	 */
	private boolean updatePotential(LabeledNode node, Label newLabel, int newValue, boolean fromR3, String log) {
		// R0
		if (node.isObserver()) {
			newLabel = newLabel.remove(node.getPropositionObserved());
		}

		int currentValue = node.getPotential(newLabel);
		if (node.putPotential(newLabel, newValue)) {
			// the value was added
			int count = node.updatePotentialCount(newLabel, currentValue == Constants.INT_NULL || fromR3) + 1;//
			if (count > this.numberOfNodes) {
				newValue = Constants.INT_NEG_INFINITE;
				node.putPotential(newLabel, newValue);
			}
			if (!newLabel.containsUnknown() && (newValue == Constants.INT_NEG_INFINITE || (newValue < 0 && node == this.Z))) {
				// found a negative cycle!
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
			}
			/**
			 * It is not possible to speed up the update of a node when the value of an observation is very late w.r.t. the node value
			 * because in some cases this can generate a wrong update.
			 * See test 088_1negQloop1posQloop1Shared.cstn as test case where the update of each node has to be done
			 * following the update of each obs node and each obs node is update by 1 at each cycle.
			 */
			if (Debug.ON) {
				if (fromR3) {
					log += "R3 ";
				}
				log += "Update potential on " + node.getName()
						+ ": " + pairAsString(newLabel, currentValue) + " replaced by " + pairAsString(newLabel, newValue) + ". Update #" + count;
				if (!this.checkStatus.consistency) {
					log += "\n***\nFound a negative loop in node " + node + "\n***";
				}
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