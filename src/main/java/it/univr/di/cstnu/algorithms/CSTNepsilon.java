package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Option;

import edu.uci.ics.jung.io.GraphIOException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * In this class, the dynamic consistency check is done assuming ε-reaction-time DC semantics (cf. ICAPS 2016 paper, table 2).
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNepsilon extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNepsilon.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
//	static public final String VERSIONandDATE = "Version  1.0 - April, 03 2017";// first release
	static public final String VERSIONandDATE = "Version  1.1 - October, 11 2017";//removed qLabel

	/**
	 * Reaction time for CSTN
	 */
	@Option(required = false, name = "-r", usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(final String[] args) throws FileNotFoundException, GraphIOException {
		LOG.finest("Start...");
		final CSTNepsilon cstn = new CSTNepsilon();

		if (!cstn.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTNepsilon.class.getName() + " " + CSTNepsilon.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		LOG.finest("Loading graph...");
		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstn.fInput,LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());

		LOG.finest("LabeledIntGraph loaded!");

		LOG.finest("ε-reaction-time DC Checking...");
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		LOG.finest("LabeledIntGraph minimized!");
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

		if (cstn.fOutput != null) {
			final GraphMLWriter graphWriter = new GraphMLWriter(new StaticLayout<>(cstn.getG()));
			try {
				graphWriter.save(cstn.getG(), new PrintWriter(cstn.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Default constructor. Label optimization.
	 */
	private CSTNepsilon() {
		super();
	}

	/**
	 * Constructor for CSTN.
	 * 
	 * @param reactionTime reaction time. It must be strictly positive.
	 * @param g graph to check
	 */
	public CSTNepsilon(int reactionTime, LabeledIntGraph g) {
		super(g);
		if (reactionTime <= 0)
			throw new IllegalArgumentException("Reaction time must be > 0.");
		this.reactionTime = reactionTime;
		this.wd2epsilon = reactionTime;
	}

	/**
	 * Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>ε-reaction-time DC semantics is assumed.</b>
	 * 
	 * <pre>
	 * R0:
	 * P? --[α p, w]--&gt; X 
	 * changes in 
	 * P? --[α', w]--&gt; X when w &lt; ε
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * ε>0 is the reaction time.
	 * </pre>
	 * 
	 * Rule qR0 has X==Z.
	 * @param nObs the observation node
	 * @param nX the other node
	 * @param nZ
	 * @param ePX the edge connecting P? ---&gt; X
	 * 
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge ePX) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false, mergeStatus;
		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
			}
			return false;
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: start.");
		}
		if (nX.getLabel().contains(p)) {
			// Table 1 ICAPS
			// if (LOG.isLoggable(Level.FINER)) {
			// LOG.log(Level.FINER, "R0: Proposition " + p + " is present in the X label '" + X.getLabel() + ". R0 cannot be applied.");
			// }
			return false;
		}

		final ObjectSet<Label> obsXLabelSet = ePX.getLabeledValueMap().keySet();

		for (final Label l : obsXLabelSet) {
			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = ePX.getValue(l);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w >= this.reactionTime) {// Table 2 ICAPS paper for IR semantics!
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
			if (alphaPrime == null) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (LOG.isLoggable(Level.FINER)) {
				logMessage = "R0 simplifies a label of edge " + ePX.getName()
						+ ":\nsource: " + nObs.getName() + " ---" + CSTN.pairAsString(l, w) + "---> " + nX.getName()
						+ "\nresult: " + nObs.getName() + " ---" + CSTN.pairAsString(alphaPrime, w) + "---> " + nX.getName();
			}

			ePX.putLabeledValueToRemovedList(l, w);
			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
			this.checkStatus.r0calls++;
			ruleApplied = true;
			mergeStatus = ePX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, logMessage);
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: end.");
		}
		return ruleApplied;
	}
	
	/**
	 * Rule R3* applies the following labels modification:<br>
	 * <b>ε-reaction-time DC semantics is assumed.</b>
	 * 
	 * <pre>
	 * if P? --[αβ, w]--&gt; nD &lt;--[βγp, v]-- nS  and w &le; ε
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[αβγ', max{w-ε,v}]-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * ε>0 is the reaction time.
	 * </pre>
	 *
	 * Rule qR3* applies:
	 * 
	 * <pre>
	 * if P? --[γ, w]--&gt; Z &lt;--[βθp', v]-- nS  and w &le; 0 (because there is no positive value)
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[(γ★β)†, max{w-ε,v}]-- nS
	 * where:
	 * β, θ and γ are in Q*.
	 * p' is p or ¬p or ¿p
	 * γ does not contain p' and any of its children.
	 * β does not contain any children of p'.
	 * θ contains only children of p'.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * (γ★β)† is the extended conjunction without any children of unknown literals.
	 * ε>0 is the reaction time.
	 * </pre>
	 * @param nS node
	 * @param nD node
	 * @param nZ
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * 
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSD) {

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R3: start.");
		}
		boolean ruleApplied = false;

		ObjectArraySet<LabeledIntEdge> Obs2nDEdges = this.getEdgeFromObservatorsToNode(nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();
		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);

			if (nObs.equalsByName(nS))
				continue;
			final char p = nObs.getPropositionObserved();

			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// Table 1 and 2 ICAPS
				// if (LOG.isLoggable(Level.FINEST)) {
				// LOG.log(Level.FINEST, "R3: Proposition " + p + " is present in the nS label '" + nS.getLabel() + " or nD label " + nD.getLabel()
				// + ". R3 cannot be applied.");
				// }
				continue;
			}
			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.getLabeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (w > this.reactionTime) { // Table 2 ICAPS
					// (w == 0 && nD==Z), it means that P? is executed at 0. So, even if v==0 (it cannot be v>0),
					// the constraint does not imply an implicit constraint (stripping p). So, we don't touch the constraint.
					continue;
				}

				final Label ObsDLabel = entryObsD.getKey();

				for (final Label SDLabel : SDLabelSet) {
					if (SDLabel == null || !SDLabel.contains(p)) {
						continue;
					}

					final int v = eSD.getValue(SDLabel);
					if (v == Constants.INT_NULL) {
						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
						continue;
					}

					final int max = Math.max(w-this.reactionTime, v);

					Label newLabel = (nD != nZ) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					eSD.putLabeledValueToRemovedList(SDLabel, v);
					ruleApplied = eSD.mergeLabeledValue(newLabel, max);
					if (ruleApplied) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
									+ "source: " + nObs.getName() + " ---" + CSTN.pairAsString(ObsDLabel, w) + "---> " + nD.getName()
									+ " <---" + CSTN.pairAsString(SDLabel, v) + "--- " + nS.getName()
									+ "\nresult: add " + nD.getName() + " <---" + CSTN.pairAsString(newLabel, max) + "--- " + nS.getName());
						}
						this.checkStatus.r3calls++;
					}
				}
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R3: end.");
		}
		return ruleApplied;
	}


}
