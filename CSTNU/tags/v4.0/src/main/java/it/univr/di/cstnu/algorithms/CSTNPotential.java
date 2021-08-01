// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

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
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.Literal;

/**
 * Represents a Conditional Simple Temporal Network (CSTN) and it contains a method to check the dynamic consistency of the instance.<br>
 * Edge weights are signed integer.<br>
 * The dynamic consistency check (DC check) is done assuming the instantaneous reaction semantics (cf. ICAPS 2016 paper, table 1).
 * and that the instance is streamlined (cf TIME 2018 paper).
 * In this class the DC checking is solved using the Single-Sink Shortest-Paths algorithm (SSSP), and R0-R3 rules.
 * This DC checking algorithm was presented at ICAPS 2020.<br>
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNPotential extends CSTNIR {

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final public String VERSIONandDATE = "Version 0.1 - February, 20 2019";
	// static final public String VERSIONandDATE = "Version 0.2 - March, 31 2019";// It extends CSTNIR. I proved that pseudo-polynomiality cannot be avoid.
	// static final public String VERSIONandDATE = "Version 2.0 - April, 26 2019";// During the init, all nodes in the negative subpath of a negative q-loop
	// will be identified by putting -∞ in their potential.
	// static final public String VERSIONandDATE = "Version 3.0 - May, 01 2019";// It apply SPFA approach directly on instance adjusting the SPFA.
	// static final public String VERSIONandDATE = "Version 3.1 - June, 09 2019";// Edge refactoring
	// static final public String VERSIONandDATE = "Version 3.5 - November, 07 2019";// Version working with CSTN 9R v. 6.5 (SVN 363)
	// static final public String VERSIONandDATE = "Version 3.6 - November, 25 2019";// Renamed
	static final public String VERSIONandDATE = "Version 4.0 - January, 31 2020";// Implements HP_20 algorithm presented at ICAPS 2020
	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNPotential.class.getName());

	/**
	 * Just for using this class also from a terminal.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws org.xml.sax.SAXException if any.
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNPotential(), "Potential DC");
	}

	/**
	 * TNGraph&lt;CSTNEdge&gt; order
	 */
	int numberOfNodes;

	/**
	 * <p>Constructor for CSTNPotential.</p>
	 *
	 * @param graph TNGraph to check
	 */
	public CSTNPotential(TNGraph<CSTNEdge> graph) {
		super(graph);
		// this.withNodeLabels = false;
	}

	/**
	 * <p>Constructor for CSTNPotential.</p>
	 *
	 * @param graph TNGraph to check
	 * @param givenTimeOut timeout for the check
	 */
	public CSTNPotential(TNGraph<CSTNEdge> graph, int givenTimeOut) {
		super(graph, givenTimeOut);
		// this.withNodeLabels = false;
	}

	/**
	 * Default constructor.
	 */
	CSTNPotential() {
		super();
		// this.withNodeLabels = false;
	}

	/**
	 * {@inheritDoc}
	 * Calls {@link CSTN#initAndCheck()} and, if everything is ok, it determined all possible -∞ potentials in negative q-loops.
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

		if (!qLoopFinder())
			return false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Done.");
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

	/** {@inheritDoc} */
	@Override
	@Deprecated
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {
		throw new RuntimeException("Not applicable.");
	}

	/**
	 * Executes one step of the single-sink BellmanFord algorithm.
	 * 
	 * <pre>
	 * A[u,α] &lt;---(v,β)---B
	 * adds
	 * B[(u+v),γ]
	 * where γ=α*β if v&lt;0, γ=αβ otherwise and (u+v) &lt; possibly previous value.
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
	CSTNCheckStatus singleSinkShortestPathStep(final NodesToCheck nodesToCheck, final NodesToCheck obsNodesToCheck, Instant timeoutInstant) {
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
			LabeledIntMap APotential = A.getLabeledPotential();
			ObjectSet<Label> APotentialLabel = APotential.keySet();

			NodesToCheck Bsons = new NodesToCheck();
			for (CSTNEdge AB : this.g.getInEdges(A)) {
				B = this.g.getSource(AB);
				ObjectSet<Entry<Label>> ABEntrySet = AB.getLabeledValueSet();
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "*** Considering " + A.getName() + " <--" + ABEntrySet + "-- " + B.getName());
					}
				}
				boolean isBModified = false;
				Bsons.clear();

				for (Entry<Label> ABEntry : ABEntrySet) {
					int v = ABEntry.getIntValue();
					Label beta = ABEntry.getKey();
					for (Label alpha : APotentialLabel) {
						int u = APotential.get(alpha);
						if (u == Constants.INT_NULL) {
							continue; // It could occur that APotential is modified during the cycle (look ahead).
							// So, it is necessary to check if the value is still present
						}
						int newValue = Constants.sumWithOverflowCheck(u, v);
						if (newValue >= 0 || (newValue == Constants.INT_NEG_INFINITE && v > 0))
							continue;// only non-positive values are interesting and -∞ through positive edges cannot be propagated
						Label newLabel = (v < 0) ? alpha.conjunctionExtended(beta) : alpha.conjunction(beta);// IR assumed.
						// Label newLabel = alpha.conjunction(beta);// FIXME maybe unknown literals must not to be propagated
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
			throw new IllegalStateException("TNGraph<CSTNEdge> has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}

		LabeledNode[] allNodes = this.g.getVerticesArray();

		NodesToCheck nodesToCheck = new NodesToCheck();
		LabeledNode Z = this.g.getZ();
		nodesToCheck.enqueue(Z);
		Z.putLabeledPotential(Label.emptyLabel, 0);

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
			singleSinkShortestPathStep(nodesToCheck, obsNodesToCheck, timeoutInstant);
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
		this.gCheckedCleaned = new TNGraph<>(this.g.getName(), this.g.getEdgeImplClass());
		if (this.cleanCheckedInstance) {
			this.gCheckedCleaned.copyCleaningRedundantLabels(this.g);
		}
		this.saveGraphToFile();
		return this.checkStatus;
	}

	@Override
	@Deprecated
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final CSTNEdge eSD) {
		throw new UnsupportedOperationException("labelModificationR3qR3");
	}

	/**
	 * This version is a restrict version of
	 * {@link CSTN#labelPropagation(LabeledNode, LabeledNode, LabeledNode, CSTNEdge, CSTNEdge, CSTNEdge)}.
	 * This method is used by {@link #initAndCheck()} for determining all negative qloops!
	 */
	@Override
	boolean labelPropagation(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final CSTNEdge eAB, final CSTNEdge eBC,
			CSTNEdge eAC) {
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
		boolean nCisAnObserver = nC.isObserver();
		Literal unkPropositionC = Literal.valueOf(nC.getPropositionObserved(), Literal.UNKNONW);

		ObjectSet<Object2IntMap.Entry<Label>> setToReuse = new ObjectArraySet<>();
		String firstLog = "Potential Labeled Propagation Rule considers edges " + eAB.getName() + ", " + eBC.getName() + " for " + eAC.getName();
		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			final int u = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet(setToReuse)) {
				final int v = BCEntry.getIntValue();
				int sum = Constants.sumWithOverflowCheck(u, v);
				if (sum > 0) {
					continue;
				}
				final Label labelBC = BCEntry.getKey();
				boolean qLabel = false;
				Label newLabelAC = null;
				if (lpMustRestricted2ConsistentLabel(u, v)) {
					// Even if we published that when nC == Z, the label must be consistent, we
					// also showed (but not published that if u<0, then label can contains unknown even when nC==Z.
					newLabelAC = labelAB.conjunction(labelBC);
					if (newLabelAC == null) {
						continue;
					}
				} else {
					newLabelAC = labelAB.conjunctionExtended(labelBC);
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
				if (oldValue != Constants.INT_NULL && sum >= oldValue) {
					continue;
				}

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
					if (sum == 0) {
						continue;
					}
					sum = Constants.INT_NEG_INFINITE;
					if (updatePotential(nA, newLabelAC, sum, false, log)) {
						// The labeled value is negative and label is in Q*.
						// The -∞ value is now stored on node A (==C) as potential value if label is in Q*/P*, otherwise, a negative loop has been found!
						ruleApplied = true;
						this.checkStatus.labeledValuePropagationCalls++;
						if (!this.checkStatus.consistency)
							return true;
					}
					continue;
				} // end if nA==nC

				// here sum has to be add!
				if (eAC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					this.checkStatus.labeledValuePropagationCalls++;
					// // R0qR0 rule
					if (nAisAnObserver && newLabelAC.contains(proposition)) {
						Label newLabelAC1 = labelModificationR0qR0Core(nA, nC, newLabelAC, sum);
						if (!newLabelAC1.equals(newLabelAC)) {
							newLabelAC = newLabelAC1;
							eAC.mergeLabeledValue(newLabelAC, sum);
						}
					}
					// The following is equivalent to R3qR33 when nD==Obs and newLabel contains the unknown literal of obs prop.
					if (nCisAnObserver && newLabelAC.contains(unkPropositionC)) {
						Label newLabelAC1 = newLabelAC.remove(unkPropositionC);
						if (!newLabelAC1.equals(newLabelAC)) {
							newLabelAC = newLabelAC1;
							sum = 0;
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
	@Deprecated
	CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck<CSTNEdge> edgesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdges");
	}

	@Override
	@Deprecated
	CSTNCheckStatus oneStepDynamicConsistencyByEdgesLimitedToZ(EdgesToCheck<CSTNEdge> edgesToCheck, Instant timeoutInstant) {
		throw new UnsupportedOperationException("oneStepDynamicConsistencyByEdgesLimitedToZ");
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
	 * @param timeoutInstant time out instant to not overlap
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
	 * Checks the potential of each node X in <code>nodesToCheck</code> w.r.t. each node in <code>obsNodes</code>.<br>
	 * If the potential of X is modified, X is added to <code>newNodesToCheck</code>.<br>
	 * If <code>obsAlignment</code> is true, then it assumed that <code>nodesToCheck</code> contains obs nodes only and such nodes has to be aligned among them.
	 * Therefore, the rule is applied considering only obs nodes until no potential is modified.
	 * 
	 * @param nodesToCheck the set of nodes to check
	 * @param obsNodes the observation node to which all nodes have to be checked
	 * @param newNodesToCheck If the potential of X is modified, X is added to <code>newNodesToCheck</code>
	 * @param obsAlignment if true, then it assumed that <code>nodesToCheck</code> contains obs nodes only and such nodes has to be aligned among them.
	 * @param timeoutInstant maximum allowed time for the check
	 * @return true if at least a value has been modified.
	 */
	private boolean potentialR3internalCycle(final LabeledNode[] nodesToCheck, final NodesToCheck obsNodes, final NodesToCheck newNodesToCheck,
			boolean obsAlignment, Instant timeoutInstant) {
		boolean ruleApplied = false;
		String log = "";
		while (!obsNodes.isEmpty()) {
			LabeledNode obs = obsNodes.dequeue();
			char p = obs.getPropositionObserved();
			ObjectSet<Entry<Label>> obsEntrySet = obs.getLabeledPotential().entrySet();
			for (LabeledNode node : nodesToCheck) {
				if (node == obs || (!obsAlignment && node.isObserver()))
					continue;
				int minNodeValue = node.getLabeledPotential(Label.emptyLabel);
				if (minNodeValue == Constants.INT_NULL)
					minNodeValue = Constants.INT_POS_INFINITE;
				for (Label betap : node.getLabeledPotential().keySet()) {
					int v = node.getLabeledPotential(betap);
					if (v == Constants.INT_NULL || !betap.contains(p))
						continue;
					Label beta = betap.remove(p);
					for (Entry<Label> obsEntry : obsEntrySet) {
						Label alpha = obsEntry.getKey();
						int u = obsEntry.getIntValue();
						if (u >= minNodeValue)
							continue;
						int max = newValueInR3qR3(u, v);
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
							this.checkStatus.r3calls++;
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
	 * Completes the graph adding only new negative edges for n round. In this way it finds some negative q-loops.
	 * Once possibly negative q-loops have been found (and store as (-∞, <qLabel>) in node potentials), the added edges are removed from the graph.
	 * 
	 * @return if during the computation, it founds a negative loop, it updates this.checkStatus and return false, true otherwise.
	 */
	@SuppressWarnings("null")
	private boolean qLoopFinder() {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Starting completition of graph with edges having negative weights...");
			}
		}

		this.numberOfNodes = this.g.getVertexCount();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Number of nodes: " + this.numberOfNodes);
			}
		}

		ObjectArrayList<CSTNEdge> edgesToCheck = new ObjectArrayList<>(this.g.getEdges());
		ObjectArrayList<CSTNEdge> newEdgesToCheck = new ObjectArrayList<>();
		ObjectArrayList<CSTNEdge> edgesToRemove = new ObjectArrayList<>();

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Number of edges: " + edgesToCheck.size());
			}
		}

		boolean noFirstRound = false;
		int negInftyPotentialCount = 0;
		int n = this.numberOfNodes;

		while (edgesToCheck.size() > 0 && n-- > 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "***Cycle countdown: " + n);
				}
			}
			for (CSTNEdge edgeAB : edgesToCheck) {
				LabeledNode A = this.g.getSource(edgeAB);
				LabeledNode B = this.g.getDest(edgeAB);
				boolean newEdge = false;
				for (CSTNEdge edgeBC : this.g.getOutEdges(B)) {
					LabeledNode C = this.g.getDest(edgeBC);
					CSTNEdge edgeAC = this.g.findEdge(A, C);

					// Propagate only to new edges.
					// At first round, even an already defined edge has to be considered new.
					newEdge = edgeAC == null;
					if (newEdge) {
						edgeAC = makeNewEdge(A.getName() + C.getName() + "∞", CSTNEdge.ConstraintType.qloopFinder);
					} else {
						if (noFirstRound)
							continue;
					}
					assert edgeAC != null : "Errore impossibile!";
					
					boolean newValue = labelPropagation(A, B, C, edgeAB, edgeBC, edgeAC); 
					if (newValue) {
						if (!this.checkStatus.consistency) {
							this.checkStatus.initialized = true;
							this.checkStatus.finished = true;
							return false;
						}

						if (A != C) {
							newEdgesToCheck.add(edgeAC);
							if (newEdge && edgeAC.getConstraintType() == ConstraintType.qloopFinder) {
								this.g.addEdge(edgeAC, A, C);
								edgesToRemove.add(edgeAC);
							}
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
		for (CSTNEdge e : edgesToRemove) {
			this.g.removeEdge(e);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "All possible -∞ potentials found. They are " + negInftyPotentialCount + "."
						+ "\nAll added edges for such a checking removed."
						+ "\nTo be sure, number of edges: " + this.g.getEdgeCount());
			}
		}
		return true;
	}

	/**
	 * Updates the labeled value <code>(value, label)</code> in the potential of <code>node</code>.<br>
	 * It is assumed that newValue < 0!
	 * If <code>node</code> is an observation t.p., then <code>label</code> is cleaned removing a possible observed literal.<br>
	 * If <code>(value, label)</code> is <0 and label does not contain unknown literals,
	 * a negative circuit is present and this method sets #status#consistency to false and #status#finished to true.
	 * 
	 * @param node
	 * @param newLabel
	 * @param newValue it is assumed that it is != {@link Constants#INT_NULL}
	 * @param fromR3 true if the value comes from R3 rule
	 * @param log
	 * @return true if the value was added.
	 */
	private boolean updatePotential(LabeledNode node, Label newLabel, int newValue, boolean fromR3, String log) {

		if (newValue >= 0)
			throw new IllegalArgumentException(
					"Potential value cannot be non-negative: " + node + ", new potential to add " + AbstractLabeledIntMap.entryAsString(newLabel, newValue));

		if (node.isObserver()) {
			newLabel = newLabel.remove(node.getPropositionObserved());
		}

		int currentValue;
		if (Debug.ON) {
			currentValue = node.getLabeledPotential(newLabel);
		}
		if (node.putLabeledPotential(newLabel, newValue)) {
			// the value was added
			// It seems that it is useless because it is not used on test cases
			// int count = node.updatePotentialCount(newLabel, currentValue == Constants.INT_NULL || fromR3) + 1;//
			// if (count > this.numberOfNodes) {
			// newValue = Constants.INT_NEG_INFINITE;
			// node.putLabeledPotential(newLabel, newValue);
			// if (Debug.ON) {
			// if (LOG.isLoggable(Level.FINER)) {
			// LOG.log(Level.FINER, "###Update potential has been called more than " + this.numberOfNodes + " times on " + node.getName());
			// }
			// }
			// }
			if (!newLabel.containsUnknown() && (newValue == Constants.INT_NEG_INFINITE || (newValue < 0 && node == this.g.getZ()))) {
				// found a negative cycle!
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
				this.checkStatus.negativeLoopNode = node;
			}
			/**
			 * The value of a potential is updated to Constants.INT_NEG_INFINITE only when there is more than n updates made by potential propagation only.
			 * If PotentialR3 modifies the value, it is necessary to restart again.
			 * See test 088_1negQloop1posQloop1Shared.cstn as test case where the update of each node has to be done
			 * following the update of each obs node and each obs node is update by 1 at each cycle.
			 */
			if (Debug.ON) {

				if (fromR3) {
					log += "R3 ";
				}
				log += "Update potential on " + node.getName()
						+ ": " + pairAsString(newLabel, currentValue) + " replaced by " + pairAsString(newLabel, newValue) + "\n";// + ". Update #" + count;
				if (!this.checkStatus.consistency) {
					log += "***\nFound a negative loop in node " + node + "\n***\n\n";
				}
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, log);
				}
			}
			this.checkStatus.potentialUpdate++;
			return true;
		}
		return false;
	}

}
