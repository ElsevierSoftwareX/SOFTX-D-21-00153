package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.Edge.ConstraintType;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and qR3*
 * rules.<br>
 * This class is the base class for some other specialized in which DC semantics is defined in a different way.
 * The rule implemented in this class are the LP, R0, R3* rules where LP can create -∞ values but such values are stored in nodes potentials.
 * Then, there are other 6 rules that manage such -∞ values.<br>
 * This version, 9Rules, is not efficient when there is no negative q-loops.
 * So, we store it but we do not maintain it.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN extends AbstractCSTN<CSTNEdge> {
	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.3 - October, 4 2016";
	// static final String VERSIONandDATE = "Version 4.0 - October, 25 2016";// added management not all negative edges in a negative qLoop
	// static final public String VERSIONandDATE = "Version 5.0 - April, 03 2017";// re-factored
	// static final public String VERSIONandDATE = "Version 5.2 - October, 16 2017";// better log management. This version uses LP,R0,R3*,qLP,qR0,qR3* and
	// horizon!
	// static final public String VERSIONandDATE = "Version 5.3 - November, 9 2017";// Replaced Omega node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 5.4 - November, 17 2017";// Adjusted LP
	// static final public String VERSIONandDATE = "Version 5.5 - November, 23 2017";// Adjusted skipping condition in LP
	// static final public String VERSIONandDATE = "Version 5.6 - November, 23 2017";// Horizon tweaking
	// static final public String VERSIONandDATE = "Version 5.7 - December, 13 2017";// Code tweaking
	// static final public String VERSIONandDATE = "Version 5.8 - January, 17 2019";// Code tweaking
	// static final public String VERSIONandDATE = "Version 6 - January, 31 2019";// Infty value management moved to nodes!
	// static final public String VERSIONandDATE = "Version 6.1 - June, 9 2019";// Refactoring Edge
	// static final public String VERSIONandDATE = "Version 6.2 - June, 12 2019";// Refactoring CSTN class
	@SuppressWarnings("hiding")
	static final public String VERSIONandDATE = "Version 6.5 - November, 07 2019";// 9Rule version. SVN version 363

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTN(), "Standard DC");
	}

	/**
	 * @param args
	 * @param cstn
	 * @param kindOfChecking
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	static void defaultMain(final String[] args, final CSTN cstn, String kindOfChecking)
			throws IOException, ParserConfigurationException, SAXException {
		System.out.println(cstn.getVersionAndCopyright());
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		System.out.println("Starting execution...");
		if (cstn.versionReq) {
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loading graph...");
			}
		}
		TNGraphMLReader<CSTNEdge> graphMLReader = new TNGraphMLReader<>(cstn.fInput, EdgeSupplier.DEFAULT_CSTN_EDGE_CLASS,
				LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS);
		cstn.setG(graphMLReader.readGraph());

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("TNGraph<E> loaded!\n" + kindOfChecking + " Checking...");
			}
		}
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given CSTN is Dynamic consistent!");
			} else {
				System.out.println("The given CSTN is not Dynamic consistent!");
			}
			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}
	}

	/**
	 * @param graph TNGraph to check
	 */
	public CSTN(TNGraph<CSTNEdge> graph) {
		this();
		this.setG(graph);// sets also checkStatus!
	}

	/**
	 * @param graph TNGraph to check
	 * @param giveTimeOut timeout for the check
	 */
	public CSTN(TNGraph<CSTNEdge> graph, int giveTimeOut) {
		this(graph);
		this.timeOut = giveTimeOut;
	}

	/**
	 * Default constructor.
	 */
	CSTN() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CSTNCheckStatus dynamicConsistencyCheck() throws WellDefinitionException {
		try {
			initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		return dynamicConsistencyCheckWOInit();
	}

	/**
	 * Executes one step of the dynamic consistency check.<br>
	 * For each possible triangle of the network, label propagation rule is applied and, on the resulting
	 * edge, all other rules, R0, R3 and potential ones, are also applied.<br>
	 * <em>This method is offered for studying the propagation node by node. It is not efficient!<br>
	 * {@link #dynamicConsistencyCheck()} uses a different propagation technique!</em>
	 * 
	 * @return the update status (for convenience. The status is also stored in {@link #checkStatus}).
	 * @throws WellDefinitionException if the nextGraph<E> is not well defined (does not observe all well definition properties). If this exception
	 *             occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {
		LabeledNode B, C;
		CSTNEdge AC;// AB, BC
		boolean createEdge = false;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "\nStart application labeled propagation rule+R0+R3.");
			}
		}
		/**
		 * March, 03 2016 I try to apply the rules on all edges making a by-row-visit to the adjacency matrix.
		 */
		NodesToCheck newNodesToCheck = new NodesToCheck();
		EdgesToCheck<CSTNEdge> newEdgesToCheck = new EdgesToCheck<>();
		for (LabeledNode A : this.g.getVertices()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering node " + A + ".\nFirst: potential propagation.");
				}
			}
			potentialR3_4_5_6(A, false, newNodesToCheck, newEdgesToCheck);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return this.checkStatus;
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Done.\nSecond: label propagation considering edges outgoing from " + A);
				}
			}
			for (CSTNEdge AB : this.g.getOutEdges(A)) {
				B = this.g.getDest(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				if (B == this.Z || !this.propagationOnlyToZ) {
					// Since in some graphs it is possible that there is not BC, we apply R0 and R3 to AB
					if (A.isObserver()) {
						// R0 on the resulting new values
						labelModificationR0qR0(A, B, AB);
					}
					this.labelModificationR3qR3(A, B, AB);
					if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						labelModificationR0qR0(A, B, AB);
					}
				}
				for (CSTNEdge BC : this.g.getOutEdges(B)) {
					C = this.g.getDest(BC);
					// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to possibly create -∞

					if (C == this.Z || !this.propagationOnlyToZ) {
						if (B.isObserver()) {
							// R0 on the resulting new values
							labelModificationR0qR0(B, C, BC);
						}
						this.labelModificationR3qR3(B, C, BC);
						if (B.isObserver()) {// R3 can add new values that have to be minimized.
							// R0 on the resulting new values
							labelModificationR0qR0(B, C, BC);
						}
					}
					// Now it is possible to propagate the labels with the standard rules
					AC = this.g.findEdge(A, C);
					// I need to preserve the old edge to compare below
					createEdge = (AC == null);
					if (createEdge) {
						AC = makeNewEdge(A.getName() + "_" + C.getName(), ConstraintType.derived);
					}

					labelPropagation(A, B, C, AB, BC, AC);

					@SuppressWarnings("null")
					boolean empty = AC.isEmpty();
					if (createEdge && !empty) {
						// the new CB has to be added to the graph!
						this.g.addEdge(AC, A, C);
					} else {
						if (empty)
							continue;
					}
					if (!this.checkStatus.consistency) {
						this.checkStatus.finished = true;
						return this.checkStatus;
					}

					if (C == this.Z || !this.propagationOnlyToZ) {
						if (A.isObserver()) {
							// R0 on the resulting new values
							labelModificationR0qR0(A, C, AC);
						}
						// R3 on the resulting new values
						this.labelModificationR3qR3(A, C, AC);

						if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
							// R0 on the resulting new values
							labelModificationR0qR0(A, C, AC);
						}
					}
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Finished label propagation considering edges outgoing from " + A);
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3."
						+ "\nSituation after the labeled propagation rule+R0+R3.\n");
			}
		}
		return this.checkStatus;
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance without initialize the network.<br>
	 * This method can be used ONLY when it is guaranteed that the network is already initialize by method {@link #initAndCheck()}.
	 * 
	 * @return the final status of the checking with some statistics.
	 */
	CSTNCheckStatus dynamicConsistencyCheckWOInit() {
		if (!this.checkStatus.initialized) {
			throw new IllegalStateException("TNGraph<E> has not been initialized! Please, consider dynamicConsistencyCheck() method!");
		}
		EdgesToCheck<CSTNEdge> edgesToCheck = new EdgesToCheck<>(this.g.getEdges());
		NodesToCheck nodesToCheck = new NodesToCheck();
		final int propositionN = this.g.getObserverCount();
		final int nodeN = this.g.getVertexCount();
		int m = (this.getMaxWeight() != 0) ? this.getMaxWeight() : 1;
		// From CSTNU TIME 2018: m |T|^2 3^|P|
		int maxCycles = m * nodeN * nodeN * (int) Math.pow(propositionN, 3);
		if (maxCycles < 0)
			maxCycles = Integer.MAX_VALUE;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "The maximum number of possible cycles is " + maxCycles);
			}
		}

		int i;
		Instant startInstant = Instant.now();
		Instant timeoutInstant = startInstant.plusSeconds(this.timeOut);
		for (i = 1; (i <= maxCycles) && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}

			if (this.propagationOnlyToZ) {
				oneStepDynamicConsistencyByEdgesLimitedToZ(edgesToCheck, nodesToCheck, timeoutInstant);
			} else {
				oneStepDynamicConsistencyByEdges(edgesToCheck, nodesToCheck, timeoutInstant);// Don't use 'this.' because such method is overrode!
			}

			if (!this.checkStatus.finished) {
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
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
				if (this.checkStatus.consistency) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINE)) {
							StringBuilder log = new StringBuilder("During the check # " + i + ", " + edgesToCheck.size()
									+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
							for (CSTNEdge e : edgesToCheck) {
								if (e.size() == 0)
									continue;
								log.append("Edge " + e + "\n");
							}
							LOG.log(Level.FINE, log.toString());
						}
					}
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
				}
			}
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

		if ((i > maxCycles) && !this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Last determined graph: " + this.g);
					}
				}
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			this.saveGraphToFile();
			return this.checkStatus;
		}

		// consistent && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO,
						"Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: " + this.checkStatus);
			}
		}
		if (this.cleanCheckedInstance) {
			this.gCheckedCleaned = new TNGraph<>(this.g.getName(), this.g.getEdgeImplClass(), this.g.getLabeledValueMapImplClass());
			this.gCheckedCleaned.copyCleaningRedundantLabels(this.g);
		}
		this.saveGraphToFile();
		return this.checkStatus;
	}

	/**
	 * Returns the label to use for storing the new value considering rule R0qR0.
	 * <b>Standard DC semantics is assumed.</b>
	 * Derived classes can modify rule conditions of this method overriding {@link #mainConditionForSkippingInR0qR0(int)}.
	 * 
	 * <pre>
	 * R0:
	 * nP? --[w, α p]--&gt; nX 
	 * changes in 
	 * nP? --[w, α']--&gt; nX when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * It is assumed that P? != X.<br>
	 * Rule qR0 has X==Z.
	 * 
	 * @param nP the observation node. Per efficiency reason, there is no a security check!
	 * @param nX the other node
	 * @param alpha
	 * @param w
	 * @return the newLabel adjusted if the rule has been applied, original label otherwise.
	 */
	Label labelModificationR0qR0Light(final LabeledNode nP, final LabeledNode nX, final Label alpha, int w) {
		final char p = nP.getPropositionObserved();
		if (this.withNodeLabels) {
			if (nX.getLabel().contains(p)) {
				// It is a strange case because only with IR it is possible to manage such case.
				// In all other case is the premise of a negative loop.
				// We let this possibility
				return alpha;
			}
		}

		if (w == Constants.INT_NULL || mainConditionForSkippingInR0qR0(w)) {
			return alpha;
		}

		final Label alphaPrime = makeAlphaPrime(nX, nP, p, alpha);
		if (alphaPrime == null || alphaPrime.equals(alpha)) {
			return alpha;
		}
		this.checkStatus.r0calls++;
		return alphaPrime;
	}


	/**
	 * <h1>Rule R3*</h1>
	 * <b>Standard DC semantics is assumed.</b><br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics.</b>
	 * 
	 * <pre>
	 * if P? --[w, αβ]--&gt; nD &lt;--[v, βγp]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[max{w,v}, αβγ']-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * </pre>
	 *
	 * <h2>Rule qR3*</h2>
	 * 
	 * <pre>
	 * if P? --[w, γ]--&gt; Z &lt;--[v, βθp']-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[max{w,v}, (γ★β)†]-- nS
	 * where:
	 * β, θ and γ are in Q*.
	 * p' is p or ¬p or ¿p
	 * γ does not contain p' and any of its children.
	 * β does not contain any children of p'.
	 * θ contains only children of p'.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * (γ★β)† is the extended conjunction without any children of unknown literals.
	 * </pre>
	 * 
	 * Moreover, it calls {@link #potentialR3(LabeledNode, LabeledNode, CSTNEdge, ObjectSet)} to determine if
	 * potentials present in node nD have to be propagated to nS.
	 * 
	 * @param nS node must be different from nD
	 * @param nD node must be different from nS
	 * @param eSD E containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final CSTNEdge eSD) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: start.");
			}
		}
		boolean ruleApplied = false;
		boolean nSisObs = nS.isObserver();

		ObjectList<CSTNEdge> Obs2nDEdges = this.getEdgeFromObserversToNode(nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();

		/*
		 * allLiteralsSD is a label that contains all propositions (each represented with any its literals)
		 * that compares in labels of labeled values in the edge nS-->nD.
		 */
		Label allLiteralsSD = Label.emptyLabel;
		for (Label l : SDLabelSet) {
			allLiteralsSD = allLiteralsSD.conjunctionExtended(l);
		}

		ObjectSet<Label> nDPotentialLabel = nD.getLabeledPotential().keySet();

		for (final CSTNEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);
			if (nObs == nS)
				continue;

			final char p = nObs.getPropositionObserved();

			if (!allLiteralsSD.contains(p)) {
				// no label in nS-->nD contain any literal of p.
				continue;
			}
			if (this.withNodeLabels) {
				if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// WD1 must be preserved!
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINEST)) {
							LOG.log(Level.FINEST, "R3: Proposition " + p + " is present in the nS label '" + nS.getLabel() + " or nD label " + nD.getLabel()
									+ ". WD1 must be preserved, so R3 cannot be applied.");
						}
					}
					continue;
				}
			}

			String firstLog = "R3 considers edge " + eSD.getName() + " and observation t.p. " + nObs.getName();
			// all labels from current Obs
			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.getLabeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (mainConditionForSkippingInR3qR3(w, nD)) {
					continue;
				}

				final Label ObsDLabel = entryObsD.getKey();

				// all labels from nS-->nD
				boolean ruleAppliedOnSnD = false;
				for (final Label SDLabel : SDLabelSet) {
					if (SDLabel == null || !SDLabel.contains(p)) {
						continue;
					}

					final int v = eSD.getValue(SDLabel);
					if (v == Constants.INT_NULL) {
						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
						continue;
					}

					Label newLabel = (nD != this.Z) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					final int max = newValueInR3qR3(v, w);

					String log = "";
					if (Debug.ON) {
						log = firstLog
								+ "\nsource: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "⟶ " + nD.getName()
								+ " ⟵" + pairAsString(SDLabel, v) + "---" + nS.getName();
					}

					// If nS is an obs, apply R0 to the new label.
					if (nSisObs) {
						newLabel = labelModificationR0qR0Light(nS, nD, newLabel, max);
					}
					// Before storing the new value, checks that it can be stored!
					if (!this.propagationOnlyToZ) {
						if (potentialR6(nS, nD, newLabel, max, eSD, log))
							continue;
						if (checkAndApplyUnknownAfterObs(nD, newLabel, max, log))
							continue;
					}
					ruleApplied = eSD.mergeLabeledValue(newLabel, max);

					if (ruleApplied) {
						ruleAppliedOnSnD = true;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, log
										+ "\nresult: add " + nD.getName() + " ⟵" + pairAsString(newLabel, max) + "--- " + nS.getName() + "\n");
								firstLog = "";
							}
						}
						this.checkStatus.r3calls++;

					}
				} // all labeled value in nS-->nD has been checked.
				if (ruleAppliedOnSnD && !this.propagationOnlyToZ) {
					potentialR3(nS, nD, eSD, nDPotentialLabel);
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Applies the labeled propagation rule:<br>
	 * <b>Standard DC semantics is assumed.</b><br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics or epsilon-reaction time.</b><br>
	 * The rule implements 2018-11 qLP+, submitted to ICAPS19.
	 * 
	 * <pre>
	 * if A ---(u,α)⟶ B ---(v,β)⟶ C and (u+v < 0 and u < 0) or (u+v < 0 and αβ in P*)
	 * then A ---[(α★β)†, u+v]⟶ C 
	 * 
	 * α,β in Q*
	 * (α★β)† is the label without children of unknown.
	 *
	 * If A==C and u+v < 0, then
	 * - if (α★β)† does not contain ¿ literals, the network is not DC
	 * - if (α★β)† contains ¿ literals, the u+v becomes -∞ and IT IS STORED as labeled value in node A!
	 * </pre>
	 * 
	 * Before storing a new value, it checks the value considering potential rules and R0 one to guarantee to store a necessary value with the shortest label.
	 * 
	 * @param nA first node.
	 * @param nB second node.
	 * @param nC third node.
	 * @param eAB edge nA⟶nB
	 * @param eBC edge nB⟶nC
	 * @param eAC edge nA⟶nC
	 * @return true if a reduction has been applied.
	 */
	boolean labelPropagation(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final CSTNEdge eAB, final CSTNEdge eBC, CSTNEdge eAC) {
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
				if (this.propagationOnlyToZ || mainConditionForRestrictedLP(u, v)) {// Even if we published that when nC == Z, the label must be consistent, we
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

				if (!this.propagationOnlyToZ) {
					// Checks the new labeled value w.r.t. some rules before storing it.
					if (potentialR6(nA, nC, newLabelAC, oldValue, eAC, log))
						continue;
					if (checkAndApplyUnknownAfterObs(nC, newLabelAC, sum, log))
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

	/**
	 * Executes one step of the dynamic consistency check.
	 * For each edge in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule is applied two times:
	 * one time having the edge as first edge, one time having the edge as second edge.<br>
	 * All modified or new edges are returned in the set 'edgesToCheck'.<br>
	 * It is assumed that {@link #propagationOnlyToZ} is false!
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param nodesToCheck set of nodes having at least one (-∞, l) that must be propagated.
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck<CSTNEdge> edgesToCheck, final NodesToCheck nodesToCheck, Instant timeoutInstant) {

		if (this.propagationOnlyToZ)
			throw new IllegalStateException("oneStepDynamicConsistencyByEdges can be called only when propagationOnlyToZ is false.");

		LabeledNode A, B, C;
		LabeledIntMap sourceNodeOriginalPotential;
		CSTNEdge AC, CB, edgeCopy;
		boolean nodeToAdd = false;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "*** Starting of application labeled propagation rule+R0+R3+node potential propagation.");
			}
		}
		/**
		 * March, 06 2016 I try to apply the rules on all edges that have been modified in the previous cycle.
		 */
		EdgesToCheck<CSTNEdge> newEdgesToCheck = new EdgesToCheck<>(edgesToCheck.edgesToCheck);
		EdgesToCheck<CSTNEdge> newEdgesToCheckR0R3 = new EdgesToCheck<>();
		NodesToCheck newNodesToCheck = new NodesToCheck();
		int i = 1, j = 1, n;
		// Find a stable state using R0 e R3.
		while (edgesToCheck.size() != 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "*** R3R0 CYCLE " + (j++));
				}
			}
			n = edgesToCheck.size();
			for (CSTNEdge AB : edgesToCheck) {
				nodeToAdd = true;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "*** R3R0: considering edge " + (i++) + "/" + n + ": " + AB.getName());
					}
				}
				A = this.g.getSource(AB);
				B = this.g.getDest(AB);
				sourceNodeOriginalPotential = new LabeledIntTreeMap(A.getLabeledPotential());
				// initAndCheck does not resolve completely a qStar.
				// It is necessary to check here the edge before to consider the second edge.
				// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
				// It is assumed that {@link #propagationOnlyToZ} is false!
				edgeCopy = this.g.getEdgeFactory().get(AB);
				if (A.isObserver()) {
					// // R0 on the resulting new values
					labelModificationR0qR0(A, B, AB);
				}
				labelModificationR3qR3(A, B, AB);
				// if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// // R0 on the resulting new values
				// labelModificationR0qR0(A, B, AB);
				// }
				if (!AB.hasSameValues(edgeCopy)) {
					newEdgesToCheckR0R3.add(AB, A, B, this.Z, this.g, this.propagationOnlyToZ);
					newEdgesToCheck.add(AB, A, B, this.Z, this.g, this.propagationOnlyToZ);
				}
				if (nodeToAdd && !A.getLabeledPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to A
					newNodesToCheck.enqueue(A);
					nodeToAdd = false;
				}
				//
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
			edgesToCheck.clear();
			edgesToCheck.addAll(newEdgesToCheckR0R3.edgesToCheck);
			newEdgesToCheckR0R3.clear();
			i = 1;
		}

		edgesToCheck.addAll(newEdgesToCheck.edgesToCheck);
		newEdgesToCheck.clear();
		n = edgesToCheck.size();
		// now it is time to propagate a stable configuration
		for (CSTNEdge AB : edgesToCheck) {
			nodeToAdd = true;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "*** LP: considering edge " + (i++) + "/" + n + ": " + AB.getName());
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			sourceNodeOriginalPotential = new LabeledIntTreeMap(A.getLabeledPotential());
			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			for (CSTNEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);
				// It is necessary to consider also self loop to store first negative loop.
				// In this class, (-∞,q), where q\in Q* are not more propagate on edges, but on nodes!

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().get(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), CSTNEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(A, B, C, AB, BC, AC);

				/**
				 * 2016-03-08: It has been experimented that it is not necessary to clean AC values using R0 and R3
				 */
				boolean edgeModified = false;
				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					edgeModified = true;
				} else {
					// CB was already present and it has been changed!
					edgeModified = (edgeCopy != null && !edgeCopy.hasSameValues(AC));
				}

				if (edgeModified) {
					newEdgesToCheck.add(AC, A, C, this.Z, this.g, this.propagationOnlyToZ);
					potentialR3(A, C, AC, null);
				}

				if (nodeToAdd && !A.getLabeledPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to A
					newNodesToCheck.enqueue(A);
					nodeToAdd = false;
				}
				if (!this.checkStatus.consistency) {
					// it must be here because a potential can be modified!
					this.checkStatus.finished = true;
					return this.checkStatus;
				}
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			sourceNodeOriginalPotential = new LabeledIntTreeMap(B.getLabeledPotential());
			nodeToAdd = true;
			for (CSTNEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);
				if (C == B)// it has been checked in the Step 1/2
					continue;

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().get(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), CSTNEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(C, A, B, CA, AB, CB);

				boolean edgeModified = false;
				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
				} else {
					// CB was already present and it has been changed!
					edgeModified = edgeCopy != null && !edgeCopy.hasSameValues(CB);
				}

				if (edgeModified) {
					newEdgesToCheck.add(CB, C, B, this.Z, this.g, this.propagationOnlyToZ);
					potentialR3(C, B, CB, null);
				}

				if (nodeToAdd && !B.getLabeledPotential().equals(sourceNodeOriginalPotential)) {
					// A new -∞ value has been added to B
					newNodesToCheck.enqueue(B);
					nodeToAdd = false;
				}
				if (!this.checkStatus.consistency) {
					// it must be here because a potential can be modified!
					this.checkStatus.finished = true;
					return this.checkStatus;
				}
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.\n"
						+ "Starts execution of propagation -∞ from nodes.");
			}
		}

		// Manage -∞ on nodes!
		while (newNodesToCheck.size() != 0) {
			nodesToCheck.enqueue(newNodesToCheck.dequeue());
		}
		while (nodesToCheck.size() != 0) {
			LabeledNode node = nodesToCheck.dequeue();
			potentialR3_4_5_6(node, false, newNodesToCheck, newEdgesToCheck);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return this.checkStatus;
			}
			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return this.checkStatus;
			}
		}
		if (!this.checkStatus.consistency) {
			this.checkStatus.finished = true;
			return this.checkStatus;
		}
		// check the halt conditions
		edgesToCheck.clear();
		nodesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0 && newNodesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
			nodesToCheck.takeIn(newNodesToCheck);
		}
		return this.checkStatus;
	}

	/**
	 * Executes one step of the dynamic consistency check.
	 * For each edge B-->Z in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule
	 * is applied to A-->B-->Z for all A-->B.
	 * All modified or new edges are returned in the set 'edgesToCheck'.
	 * This method does not manage –∞ as node potential.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param nodesToCheck
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	CSTNCheckStatus oneStepDynamicConsistencyByEdgesLimitedToZ(final EdgesToCheck<CSTNEdge> edgesToCheck, final NodesToCheck nodesToCheck,
			Instant timeoutInstant) {
		// This version consider only pair of edges going to Z, i.e., in the form A-->B-->Z,
		// 2018-01-25: with this method, performances worsen.
		LabeledNode B, A;
		CSTNEdge AZ, edgeCopy;

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "\nStart application labeled propagation rule+R0+R3.");
			}
		}

		EdgesToCheck<CSTNEdge> newEdgesToCheck = new EdgesToCheck<>(edgesToCheck.edgesToCheck);
		EdgesToCheck<CSTNEdge> newEdgesToCheckR0R3 = new EdgesToCheck<>();
		int i = 1, j = 1, n;
		// Find a stable state using R0 and R3.
		while (edgesToCheck.size() != 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n\n*** R3R0 CYCLE " + (j++));
				}
			}
			n = edgesToCheck.size();
			for (CSTNEdge BZ : edgesToCheck) {
				if (this.g.getDest(BZ) != this.Z)
					continue;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "\n\n*** R3R0: considering edge " + (i++) + "/" + n + ": " + BZ.getName());
					}
				}
				B = this.g.getSource(BZ);
				// initAndCheck does not resolve completely a qStar.
				// It is necessary to check here the edge before to consider the second edge.
				// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
				edgeCopy = this.g.getEdgeFactory().get(BZ);
				if (B.isObserver()) {
					// R0 on the resulting new values
					labelModificationR0qR0(B, this.Z, BZ);
				}

				labelModificationR3qR3(B, this.Z, BZ);

				if (!BZ.hasSameValues(edgeCopy)) {
					newEdgesToCheckR0R3.add(BZ, B, this.Z, this.Z, this.g, this.propagationOnlyToZ);
					newEdgesToCheck.add(BZ, B, this.Z, this.Z, this.g, this.propagationOnlyToZ);
				}

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
			edgesToCheck.clear();
			edgesToCheck.addAll(newEdgesToCheckR0R3.edgesToCheck);
			newEdgesToCheckR0R3.clear();
			i = 1;
		}

		edgesToCheck.addAll(newEdgesToCheck.edgesToCheck);
		newEdgesToCheck.clear();
		n = edgesToCheck.size();

		// now it is time to propagate a stable configuration
		for (CSTNEdge BZ : edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n\n*** LP: considering edge " + (i++) + "/" + n + ": " + BZ.getName());
				}
			}
			B = this.g.getSource(BZ);
			/**
			 * Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->Z
			 */
			for (CSTNEdge AB : this.g.getInEdges(B)) {
				A = this.g.getSource(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				AZ = this.g.findEdge(A, this.Z);
				// I need to preserve the old edge to compare below
				if (AZ != null) {
					edgeCopy = this.g.getEdgeFactory().get(AZ);
				} else {
					AZ = makeNewEdge(A.getName() + "_" + this.Z.getName(), CSTNEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labelPropagation(A, B, this.Z, AB, BZ, AZ);

				boolean edgeModified = false;
				if (edgeCopy == null && !AZ.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AZ, A, this.Z);
				} else {
					// CB was already present and it has been changed!
					edgeModified = edgeCopy != null && !edgeCopy.hasSameValues(AZ);
				}

				if (edgeModified) {
					newEdgesToCheck.add(AZ, A, this.Z, this.Z, this.g, this.propagationOnlyToZ);
					// potentialR3(A, this.Z, AZ, null);
				}

				if (!this.checkStatus.consistency) {
					this.checkStatus.finished = true;
					return this.checkStatus;
				}

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return this.checkStatus;
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.\n" +
						"Starts execution of propagation -∞ from nodes.");
			}
		}

		if (!this.checkStatus.consistency) {
			this.checkStatus.finished = true;
			return this.checkStatus;
		}

		edgesToCheck.clear();
		nodesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
		}
		return this.checkStatus;
	}

	/**
	 * Propagates (-∞, q) with q ∈ Q* from node potential to other node potentials.
	 * Potential Rules 1 and 2, 3, and 6 have been implemented as methods that must be used ALSO inside labelPropagation and Q3 methods for speeding up the
	 * algorithm.
	 * In this method, rules 3, 4, 5, and 6 are rechecked only for node nY.
	 * 
	 * <pre>
	 * 4) if Y?[(-∞,β)] and A ---(u,α y')⟶ B u < 0
	 *    then add A ---(u,(α★β)†)⟶ B
	 *  
	 * 5) if P? ---(u,α)⟶ A  and  B[(-∞,β p')] and u ≤ 0
	 *    then B ---(u,(α★β)†)⟶ A
	 * 
	 * α in Q*
	 * β in Q* / P* 
	 * (α★β)† is the label without children of unknown.
	 * y' = y or ¬y or ¿y
	 * p' = p or ¬p or ¿p
	 * </pre>
	 * 
	 * @param nY
	 * @param limitToZ true if method has to check only edge going to Z.
	 * @param newNodesToCheck
	 * @param newEdgesToCheck
	 * @return true if the rule has been applied, false otherwise.
	 */
	boolean potentialR3_4_5_6(LabeledNode nY, boolean limitToZ, NodesToCheck newNodesToCheck, EdgesToCheck<CSTNEdge> newEdgesToCheck) {
		boolean ruleApplied = false;
		String log = "", firstLog = "";
		ObjectSet<Label> nYPotentialLabel = nY.getLabeledPotential().keySet();

		if (!nYPotentialLabel.isEmpty()) {
			/**
			 * Case 3
			 * if X ---(u,α)⟶ Y[(-∞,β)] and u ≤ 0,
			 * then add X[(α★β)†]
			 * Again the rule is applied to guarantee that there is no unpropagated potential.
			 * 
			 * @see {@link checkAndApplyPotentialRule3}
			 */
			for (CSTNEdge yInEdge : this.g.getInEdges(nY)) {
				LabeledNode nS = this.g.getSource(yInEdge);
				if (potentialR3(nS, nY, yInEdge, nYPotentialLabel)) {
					newNodesToCheck.enqueue(nS);
					ruleApplied = true;
					if (!this.checkStatus.consistency) {
						this.checkStatus.finished = true;
						return true;
					}
				}
			}
			/**
			 * Case 6
			 * if Y[(-∞,β)] ---(u,α)⟶ X and u ≤ 0 and α subsumes β,
			 * then DELETE (u,α) from the edge.
			 *** Applied also in #labelPropagation() and R3qR3***
			 */
			if (nY != this.Z) {
				for (CSTNEdge yOutEdge : this.g.getOutEdges(nY)) {
					LabeledNode nD = this.g.getDest(yOutEdge);
					for (Entry<Label> entry : yOutEdge.getLabeledValueSet()) {
						int u = entry.getIntValue();
						Label alpha = entry.getKey();
						ruleApplied |= potentialR6(nY, nD, alpha, u, yOutEdge, "");
					}
				}
			}
			/**
			 * Case 4 (very expensive!)
			 * if Y?[(-∞,β)] and A ---(u,α y')⟶ B and u ≤ 0
			 * then add A ---(u,(α★β)†)⟶ B
			 */
			if (nY.isObserver()) {
				char proposition = nY.getPropositionObserved();
				if (!nYPotentialLabel.isEmpty()) {
					Collection<CSTNEdge> edgeSet = (limitToZ) ? this.g.getInEdges(this.Z) : this.g.getEdges();
					for (CSTNEdge e : edgeSet) {
						if (Debug.ON) {
							firstLog = "Labeled Potential Propagation Rule 4 considers edge " + e.getName() + "\nand observation t.p.: " + nY.getName();
						}
						LabeledNode A = this.g.getSource(e), B = this.g.getDest(e);
						boolean aIsObs = A.isObserver();
						boolean modified = false;
						for (Entry<Label> entry : e.getLabeledValueSet()) {
							int u = entry.getIntValue();
							if (u >= 0)
								continue;
							Label alpha = entry.getKey();
							if (!alpha.contains(proposition))
								continue;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									log = firstLog + "\nsource: " + A.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + B.getName();
								}
							}
							alpha = alpha.remove(proposition);
							for (Label beta : nYPotentialLabel) {
								Label alphaBeta = alpha.conjunctionExtended(beta);
								if (this.withNodeLabels) {
									alphaBeta = removeChildrenOfUnknown(alphaBeta);
								}
								if (alpha.equals(alphaBeta))
									continue;
								if (potentialR6(A, B, alphaBeta, u, e, log))
									continue;
								if (checkAndApplyUnknownAfterObs(B, alphaBeta, u, log))
									continue;
								if (aIsObs) {
									alphaBeta = labelModificationR0qR0Light(A, B, alphaBeta, u);
								}

								if (e.mergeLabeledValue(alphaBeta, u)) {
									modified = true;
									this.checkStatus.potentialCalls[3]++;
									if (Debug.ON) {
										if (LOG.isLoggable(Level.FINER)) {
											LOG.log(Level.FINER, log
													+ " and " + nY.getName() + "[" + pairAsString(beta, Constants.INT_NEG_INFINITE) + "]"
													+ "\nresult: "
													+ A.getName() + " ---" + pairAsString(alphaBeta, u) + "⟶ " + B.getName());
											firstLog = "";
										}
									}
								}
							}
						}
						if (modified) {
							ruleApplied = true;
							newEdgesToCheck.add(e, this.g.getSource(e), this.g.getDest(e), this.Z, this.g, this.propagationOnlyToZ);
						}
					}
					// Second part of Rule 4 that considers node's potentials.
					for (LabeledNode node : this.g.getNodes()) {
						boolean modified = false;
						for (Label alpha : node.getLabeledPotential().keySet()) {
							if (node.getLabeledPotential(alpha) == Constants.INT_NULL) {
								continue; // a previous cycle removed it!
							}
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									firstLog = "Labeled Potential Propagation Rule 4 considers node " + node.getName()
											+ "\nand observation t.p.: " + nY.getName();
								}
							}
							if (!alpha.contains(proposition))
								continue;
							alpha = alpha.remove(proposition);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									log = firstLog + "\nsource: " + node.getName() + node.getLabeledPotential();
								}
							}
							for (Label beta : nYPotentialLabel) {
								Label alphaBeta = alpha.conjunctionExtended(beta);
								if (this.withNodeLabels) {
									alphaBeta = removeChildrenOfUnknown(alphaBeta);
								}
								if (potentialR1_2(node, alphaBeta, log)) {
									modified = true;
									this.checkStatus.potentialCalls[3]++;
									if (!this.checkStatus.consistency) {
										this.checkStatus.finished = true;
										return true;
									}
									if (Debug.ON) {
										if (LOG.isLoggable(Level.FINER)) {
											LOG.log(Level.FINER, log
													+ " and " + nY.getName() + "[" + pairAsString(beta, Constants.INT_NEG_INFINITE) + "]"
													+ "\nresult: "
													+ node.getName() + node.getLabeledPotential());
											firstLog = "";
										}
									}
								}
							}
						}
						if (modified) {
							ruleApplied = true;
							newNodesToCheck.enqueue(node);
						}
					}
				}
			} // nY is an observer
		} // nY potential is not empty
		/**
		 * Case 5
		 * if P? ---(u,α)⟶ A and B[(-∞,β p')] and u ≤ 0
		 * then B ---(u,(α★β)†)⟶ A
		 * From here Y = P?
		 */
		// First of all, determines all nodes and its potential labels having label containing the proposition.
		if (!nY.isObserver())
			return false;
		char proposition = nY.getPropositionObserved();
		Object2ObjectArrayMap<LabeledNode, ObjectSet<Label>> cleanedNodeLabelMapWithPropositionInPotential = new Object2ObjectArrayMap<>();
		for (LabeledNode node : this.g.getVertices()) {
			for (Label beta : node.getLabeledPotential().keySet()) {
				if (beta.contains(proposition)) {
					ObjectSet<Label> labelSet = cleanedNodeLabelMapWithPropositionInPotential.get(node);
					if (labelSet == null) {
						labelSet = new ObjectArraySet<>();
						cleanedNodeLabelMapWithPropositionInPotential.put(node, labelSet);
					}
					labelSet.add(beta.remove(proposition));
				}
			}
		}
		if (cleanedNodeLabelMapWithPropositionInPotential.isEmpty()) {
			return ruleApplied;
		}
		// Then, for each edge outgoing from P to A having negative values, add an edge from the B to A.
		Collection<CSTNEdge> edgeSet;
		if (limitToZ) {
			edgeSet = new ObjectArrayList<>();
			edgeSet.add(this.g.findEdge(nY, this.Z));
		} else {
			edgeSet = this.g.getOutEdges(nY);
		}
		for (CSTNEdge YA : edgeSet) {
			LabeledNode A = this.g.getDest(YA);

			for (Entry<Label> entry : YA.getLabeledValueSet()) {
				int u = entry.getIntValue();
				if (u > 0)
					continue;
				Label alpha = entry.getKey();
				for (java.util.Map.Entry<LabeledNode, ObjectSet<Label>> nodeAndPotential : cleanedNodeLabelMapWithPropositionInPotential.entrySet()) {
					LabeledNode B = nodeAndPotential.getKey();
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							firstLog = "Labeled Potential Propagation Rule 5 applied to edge " + YA.getName()
									+ "\nand node " + B.getName();
						}
					}
					if (A == B)
						continue;
					CSTNEdge e = this.g.findEdge(B, A);
					if (e == null) {
						e = makeNewEdge(B.getName() + "_" + A.getName(), CSTNEdge.ConstraintType.derived);
						this.g.addEdge(e, B, A);
					}
					boolean modified = false;
					for (Label beta : nodeAndPotential.getValue()) {
						Label alphaBeta = alpha.conjunctionExtended(beta);
						if (this.withNodeLabels) {
							alphaBeta = removeChildrenOfUnknown(alphaBeta);
						}
						if (checkAndApplyUnknownAfterObs(A, alphaBeta, u, log))
							continue;

						if (e.mergeLabeledValue(alphaBeta, u)) {
							modified = true;
							this.checkStatus.potentialCalls[4]++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, firstLog
											+ "\nSource: " + nY.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + A.getName() + " and node "
											+ B.getName() + "[" + beta + "]"
											+ "\nResult: " + B.getName() + " ---" + pairAsString(alphaBeta, u) + "⟶ " + A.getName());
									firstLog = "";
								}
							}
						}
					}
					if (modified) {
						ruleApplied = true;
						newEdgesToCheck.add(e, B, A, this.Z, this.g, this.propagationOnlyToZ);
					}
				}
			}
		}
		return ruleApplied;
	}

	/**
	 * Checks and applies Labeled Potential Rule 6 to edge <code>eSD</code>.
	 * 
	 * <pre>
	 * Potential Rule 6
	 * if nS[(-∞,β)] ---(value, α)⟶ nD and value ≤ 0 and α subsumes β,  
	 * then DELETE (u,α) from the edge.
	 * </pre>
	 * 
	 * @param nS the origin node of the edge
	 * @param nD
	 * @param alpha the new label to store
	 * @param value the present value
	 * @param eSD the edge
	 * @param preLog possible log string to prefix the log of this method.
	 * @return true if the rule was applied, false otherwise.
	 */
	boolean potentialR6(LabeledNode nS, LabeledNode nD, Label alpha, int value, CSTNEdge eSD, String preLog) {
		if (nS == nD || nS.getLabeledPotential().isEmpty() || value > 0)
			return false;
		for (Label beta : nS.getLabeledPotential().keySet()) {
			if (alpha.subsumes(beta)) {
				if (value != Constants.INT_NULL) {
					// it may be possible that in the edge there is already a value associated to newLabel.
					// in such a case, it must be removed.
					eSD.removeLabeledValue(alpha);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, preLog
									+ "\nLabeled Potential Propagation Rule 6 is applicable:\n"
									+ nS.getName() + nS.getLabeledPotential() + " removes value " + pairAsString(alpha, value) + " in "
									+ eSD.getName()
									+ "\nResults: " + eSD);
						}
					}
				}
				// the newLabel (and its value) must not be stored.
				this.checkStatus.potentialCalls[5]++;
				return true;
			}
		}
		return false;
	}

	/**
	 * Simple method to determine the label αβγ' for rule {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, CSTNEdge)}.<br>
	 * See Table 1 and Table 2 ICAPS 2016 paper.
	 * 
	 * @param nS
	 * @param nD
	 * @param nObs
	 * @param observed the proposition observed by observer (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observer
	 * @param labelToClean
	 * @return alphaBetaGamma' if all conditions are satisfied. null otherwise.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeAlphaBetaGammaPrime4R3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nObs, final char observed,
			final Label labelFromObs, Label labelToClean) {
		StringBuilder slog;
		if (Debug.ON) {
			slog = new StringBuilder();
			if (LOG.isLoggable(Level.FINEST))
				slog.append("labelEdgeFromObs = " + labelFromObs);
		}
		if (this.withNodeLabels) {
			if (labelFromObs.contains(observed) || nS.getLabel().contains(observed) || nD.getLabel().contains(observed)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								slog.toString() + " αβγ' cannot be calculated because labelFromObs or lables of nodes contain the prop " + observed
										+ " that has to be removed.");
					}
				}
				return null;
			}
		}
		Label labelToCleanWOp = labelToClean.remove(observed);
		final Label alpha = labelFromObs.getSubLabelIn(labelToCleanWOp, false);
		if (alpha.containsUnknown()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + " α contains unknow: " + alpha);
				}
			}
			return null;
		}
		final Label beta = labelFromObs.getSubLabelIn(labelToCleanWOp, true);
		if (beta.containsUnknown()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + " β contains unknow " + beta);
				}
			}
			return null;
		}
		Label gamma = labelToCleanWOp.getSubLabelIn(labelFromObs, false);

		if (this.withNodeLabels) {
			gamma = gamma.remove(this.g.getChildrenOf(nObs));
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + " γ: " + gamma + "\n.");
			}
		}
		Label alphaBetaGamma = alpha.conjunction(beta).conjunction(gamma);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append(", αβγ'=" + alphaBetaGamma);
		}

		if (this.withNodeLabels) {
			if (alphaBetaGamma == null)
				return null;
			if (!alphaBetaGamma.subsumes(nD.getLabel().conjunction(nS.getLabel()))) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, slog.toString() + " αβγ' does not subsume labels from nodes:" + nD.getLabel().conjunction(nS.getLabel()));
					}
				}
				return null;
			}
		}
		return alphaBetaGamma;
	}

	/**
	 * Returns true if label propagation rule (for example, {@link CSTN#labelPropagation} method) has to apply only for consistent labels.<br>
	 * Overriding this method it is possible implement the different semantics in the {@link CSTN#labelPropagation} method.
	 * 
	 * @param u
	 * @param v
	 * @return true if the rule has to not apply.
	 */
	@SuppressWarnings("static-method")
	boolean mainConditionForRestrictedLP(final int u, final int v) {
		// Table 1 2016 ICAPS paper for standard DC extended with rules on page 6 of file noteAboutLP.tex
		// Moreover, Luke and I verified on 2018-11-22 that with u≤0, qLP+ can be applied.
		return u > 0;
	}

	/**
	 * Checks the labeled -∞ before putting in the potential of a node.
	 * If the label does not contain unknowns, then it stores the value, logs that a negative loop has been found,
	 * sets the consistency of the <code>this.status=false</code> and returns.
	 * Otherwise, it stores the value and returns.
	 * In any case, if the <code>node</code> is an observation time-point, the method removes from <code>label</code>
	 * possible literal associated to the proposition observed by <code>node</code> before storing the <code>(-∞, label)</code>.
	 * 
	 * @param node the node where to add the <code>(-∞, label)</code>.
	 * @param label the label of new value
	 * @param log possible text to log as prefix to the log of this method.
	 * @return true if the value has been added, false otherwise.
	 */
	boolean potentialR1_2(LabeledNode node, Label label, String log) {
		if (node.isObserver())
			// Potential Rule 2 and RO
			label = label.remove(node.getPropositionObserved());
		if (Debug.ON) {
			log += "\nputAndCheckNegativeInfty on " + node.toString()
					+ " added: " + pairAsString(label, Constants.INT_NEG_INFINITE);
		}
		if (!label.containsUnknown()) {
			node.putLabeledPotential(label, Constants.INT_NEG_INFINITE);
			// Negative loop!
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, log + "\n***\nFound a negative loop in node " + node + "\n***");
				}
			}
			this.checkStatus.consistency = false;
			this.checkStatus.finished = true;
			this.checkStatus.potentialCalls[0]++;
			return true;
		}
		if (node.putLabeledPotential(label, Constants.INT_NEG_INFINITE)) {
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

	/**
	 * Checks and applies Labeled Potential Rule 3 to node <code>nS</code>.
	 * 
	 * <pre>
	 * Potential Rule 3
	 * if nS ---(u,α)⟶ nD[(-∞,β)] and u < 0,  
	 * then add nS[(α★β)†]
	 * </pre>
	 * 
	 * @param nS
	 * @param nD
	 * @param eSD
	 * @param nDPotentialLabel if null, method checks nD potential. This parameter is useful to speed-up method calls with the same nD
	 * @return true if the rule has been applied.
	 */
	boolean potentialR3(LabeledNode nS, LabeledNode nD, CSTNEdge eSD, ObjectSet<Label> nDPotentialLabel) {
		if (nS == nD || nD.getLabeledPotential().isEmpty())
			return false;
		if (nDPotentialLabel == null)
			nDPotentialLabel = nD.getLabeledPotential().keySet();
		String log = "";
		boolean ruleApplied = false;

		for (Entry<Label> entry : eSD.getLabeledValueSet()) {
			int u = entry.getIntValue();
			if (u >= 0)
				continue;
			Label alpha = entry.getKey();
			for (Label beta : nDPotentialLabel) {
				Label alphaBeta = alpha.conjunctionExtended(beta);
				if (this.withNodeLabels) {
					alphaBeta = removeChildrenOfUnknown(alphaBeta);
				}
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = "Labeled Potential Propagation Rule 3 applied to edge " + eSD.getName()
								+ ":\nsource: "
								+ nS.getName() + " ---" + pairAsString(alpha, u) + "⟶ " + nD.getName() + "{"
								+ pairAsString(beta, Constants.INT_NEG_INFINITE) + "}";
					}
				}
				if (potentialR1_2(nS, alphaBeta, log)) {
					if (!this.checkStatus.consistency)
						return true;
					ruleApplied = true;
					this.checkStatus.potentialCalls[2]++;
				}
			}
		}
		return ruleApplied;
	}

}