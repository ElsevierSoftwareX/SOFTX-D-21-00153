package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.Literal;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * In this class, a input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * The dynamic consistency check is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1).
 * This class uses LP, R0, qR0, R3* and qR3* rules.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNwoNodeLabel extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNwoNodeLabel.class.getName());

	/**
	 * Version of the class
	 */
	// static public final String VERSIONandDATE = "Version 1.2 - April, 25 2017";
	// static public final String VERSIONandDATE = "Version 1.3 - October, 10 2017";// removed qLabels from LP
	@SuppressWarnings("hiding")
	static public final String VERSIONandDATE = "Version  1.4 - November, 07 2017";// restored original LP

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		if (Debug.ON)
			LOG.finest("Start...");
		final CSTNwoNodeLabel cstn = new CSTNwoNodeLabel();

		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON)
			LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTNwoNodeLabel.class.getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		if (Debug.ON)
			LOG.finest("Loading graph...");
		CSTNUGraphMLReader graphMLReader = new CSTNUGraphMLReader(cstn.fInput, LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());

		if (Debug.ON)
			LOG.finest("LabeledIntGraph loaded!");

		if (Debug.ON)
			LOG.finest("Standard DC Checking...");
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (Debug.ON)
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
			final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(new StaticLayout<>(cstn.g));
			try {
				graphWriter.save(cstn.g, new PrintWriter(cstn.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Auxiliary constraints switch
	 */
	boolean addAuxiliaryConstraints = false;

	/**
	 * Default constructor.
	 */
	private CSTNwoNodeLabel() {
		super();
	}

	/**
	 * Constructor for
	 * 
	 * @param g graph to check
	 */
	public CSTNwoNodeLabel(LabeledIntGraph g) {
		super(g);
	}

	/**
	 * Help method to initialize and check the CSTN represented by graph g. The {@link #dynamicConsistencyCheck()} calls this method before
	 * to execute the check. If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level.
	 * This version of method also transform the graph removing all node labels and adding the corresponding constraints as shown in paper TIME17.
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	@Override
	public boolean initAndCheck() throws WellDefinitionException {

		super.initAndCheck();

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Starts the conversion to a CSTN without labels on nodes.");
			}
		}

		// Substitutes node labels with corresponding edges with horizon value
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		LabeledNode Z = this.g.getZ();
		for (final LabeledNode node : nodeSet) {
			if (this.addAuxiliaryConstraints) {
				Label nodeLabel = node.getLabel();
				LabeledIntEdge edgeToZ = this.g.findEdge(node, Z);
				// Now, it removes label from node and adds equivalent constraints as shown in TIME17
				// Add lower bounds
				for (Literal l : nodeLabel.negation()) {
					/**
					 * [2017-04-25] After an experimentation, it has been proved that the lower bound must be -∞ to avoid a lot of useless labeled value
					 * propagations.
					 * The theoretical bound is (-this.horizon - 1).
					 */
					edgeToZ.mergeLabeledValue(new Label(l.getName(), l.getState()), Constants.INT_NEG_INFINITE);
				}
				// Add the upper bound: since Ω has been already add, this is not more necessary
				// LabeledIntEdge edgeFromZ = this.g.findEdge(Z, node);
				// if (edgeFromZ == null) {
				// edgeFromZ = makeNewEdge(this.ZeroNodeName + "_" + node.getName(), ConstraintType.internal);
				// this.g.addEdge(edgeFromZ, Z, node);
				// }
				// edgeFromZ.mergeLabeledValue(nodeLabel, this.horizon);
			}
			node.setLabel(Label.emptyLabel);
		}

		if (this.addAuxiliaryConstraints) {
			// It is better to normalize with respect to the label modification rules before starting the DC check.
			// Such normalization assures only that redundant labels are removed (w.r.t. R0)
			// Q* are not solved by this normalization!
			this.checkStatus.reset();
			this.g.clearCache();
			try {
				for (final LabeledIntEdge e : this.g.getEdges()) {
					//
					final LabeledNode s = this.g.getSource(e);
					final LabeledNode d = this.g.getDest(e);

					// Normalize with respect to R0--R3
					if (s.isObserver()) {
						this.labelModificationR0qR0(s, d, Z, e);
					}
					this.labelModificationR3qR3(s, d, Z, e);
					if (s.isObserver()) {
						// again because R3 could have add a new value;
						this.labelModificationR0qR0(s, d, Z, e);
					}
				}
			} catch (IllegalStateException ex) {
				String logMsg = "Graph is not well defined: " + ex.getMessage();
				if (Debug.ON)
					LOG.severe(logMsg);
				throw new WellDefinitionException(logMsg);
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "The conversion to a CSTN without labels on nodes has been done. ");
			}
		}
		this.checkStatus.initialized = true;
		return true;
	}

	/**
	 * @return the addAuxiliaryConstraints
	 */
	public boolean isAddAuxiliaryConstraints() {
		return this.addAuxiliaryConstraints;
	}

	/**
	 * As {@link CSTN#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge)}
	 * but without the check that the new label subsumes the conjunction of node labels and children management.
	 * The rule is the EqLp+ not yet published.
	 *
	 * @param nA CANNOT BE NULL!
	 * @param nB CANNOT BE NULL!
	 * @param nC CANNOT BE NULL!
	 * @param eAB CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eBC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eAC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @return true if a reduction has been applied.
	 * @see CSTN#labeledPropagationqLP(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge)
	 */
	@Override
	boolean labeledPropagationqLP(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB,
			final LabeledIntEdge eBC, LabeledIntEdge eAC) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false;

		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			/**
			 * If there is a self loop containing a (-∞, q*), it must be propagated! NO MORE USED!
			 */
			final int u = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet()) {
				final Label labelBC = BCEntry.getKey();
				final Label newLabelAC = labelAB.conjunctionExtended(labelBC);
				final boolean qLabel = newLabelAC.containsUnknown();
				if (qLabel) {
					if (u >= 0) // rule condition!
						continue;
				}
				final int v = BCEntry.getIntValue();
				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(u, v);
				int oldValue = eAC.getValue(newLabelAC);
				if (nA == nC) {
					if (sum >= 0) {
						// it would be a redundant edge
						continue;
					}
					// here sum is negative
					if (!qLabel) {
						// sum is negative!
						eAC.mergeLabeledValue(newLabelAC, sum);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "***\nFound a negative loop " + pairAsString(newLabelAC, sum) + " in the edge  " + eAC + "\n***");
							}
						}
						this.checkStatus.consistency = false;
						this.checkStatus.finished = true;
						this.checkStatus.labeledValuePropagationcalls++;
						return true;
					}
					sum = Constants.INT_NEG_INFINITE;
				} else {
					// in the case of A != C, a value is stored only if it is more negative than the current one.
					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						continue;
					}
				}
				// here sum has to be insert!
				// I have to prepare the log before the execution of the merge!
				String log = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						log = "Label Propagation Rule applied to edge " + eAC.getName()
								+ ":\nsource: "
								+ nA.getName() + " ---" + pairAsString(labelAB, u) + "---> " + nB.getName() + " ---" + pairAsString(labelBC, v)
								+ "---> "
								+ nC.getName()
								+ "\nresult: "
								+ nA.getName() + " ---" + pairAsString(newLabelAC, sum) + "---> " + nC.getName()
								+ "; old value: " + Constants.formatInt(oldValue);
					}
				}

				if (eAC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					this.checkStatus.labeledValuePropagationcalls++;
					if (Debug.ON)
						LOG.log(Level.FINER, log);
				}
			}
		}
		return ruleApplied;
	}

	/**
	 * As {@link CSTN#labelModificationR0qR0(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge)} but without check of node labels!
	 * 
	 * @param nObs the observation node
	 * @param nX the other node
	 * @param nZ
	 * @param eObsX the edge connecting nObs? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 * @see CSTN#labelModificationR0qR0(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge)
	 */
	@Override
	boolean labelModificationR0qR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge eObsX) {
		// Visibility is package because there is Junit Class test that checks this method.
		boolean ruleApplied = false;
		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R0: start.");
			}
		}

		final ObjectSet<Label> obsXLabelSet = eObsX.getLabeledValueMap().keySet();

		for (final Label edgeLabel : obsXLabelSet) {
			if (edgeLabel == null || !edgeLabel.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = eObsX.getValue(edgeLabel);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w > 0) {// Table 1 ICAPS paper.
				// When X==Z, w must be < 0 to apply rule. w==0 is not considered because it doesn't occur since each node is at least 0 distance from Z.
				continue;
			}

			Label alphaPrime = new Label(edgeLabel);
			alphaPrime.remove(p);

			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					logMessage = "R0 simplifies a label of edge " + eObsX.getName()
							+ ":\nsource: " + nObs.getName() + " ---" + pairAsString(edgeLabel, w) + "---> " + nX.getName()
							+ "\nresult: " + nObs.getName() + " ---" + pairAsString(alphaPrime, w) + "---> " + nX.getName();
				}
			}

			this.checkStatus.r0calls++;
			ruleApplied = true;
			boolean merged = eObsX.mergeLabeledValue(alphaPrime, w);
			if (Debug.ON) {
				if (merged && LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, logMessage);
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R0: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * As {@link CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge)} but without check of node labels.
	 * 
	 * @param nS node
	 * @param nD node
	 * @param nZ
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 * @see CSTN#labelModificationR3qR3(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge)
	 */
	@Override
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3qR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSD) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Label Modification R3: start.");
			}
		}
		boolean ruleApplied = false;

		ObjectList<LabeledIntEdge> Obs2nDEdges = this.getEdgeFromObserversToNode(nD);
		if (Obs2nDEdges == null || Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();
		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);

			if (nObs.equalsByName(nS))
				continue;
			final char p = nObs.getPropositionObserved();

			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.getLabeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (w > 0 || (w == 0 && nD == nZ)) { // Table 1 ICAPS
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

					final int max = Math.max(w, v);

					Label newLabel = (nD != nZ) ? this.makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					ruleApplied = eSD.mergeLabeledValue(newLabel, max);
					if (ruleApplied) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "---> " + nD.getName()
										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
										+ "\nresult: add " + nD.getName() + " <---" + pairAsString(newLabel, max) + "--- " + nS.getName());
							}
						}
						this.checkStatus.r3calls++;
					}
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
	 * It makes the same action of {@link CSTN#makeAlphaBetaGammaPrime4R3(LabeledNode, LabeledNode, LabeledNode, char, Label, Label)}
	 * without the check of node labels because for streamlined CSTN is not necessary.
	 * 
	 * @param nS
	 * @param nD
	 * @param nObs
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observator
	 * @param labelToClean
	 * @return alphaBetaGamma' if all conditions are satisfied. null otherwise.
	 */
	@Override
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeAlphaBetaGammaPrime4R3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nObs, final char observed,
			final Label labelFromObs, Label labelToClean) {

		final StringBuilder slog = new StringBuilder();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append("labelEdgeFromObs = " + labelFromObs);
		}

		Label labelToCleanWOp = new Label(labelToClean);
		labelToCleanWOp.remove(observed);

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
		final Label gamma = labelToCleanWOp.getSubLabelIn(labelFromObs, false);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + " γ: " + gamma + "\n.");
			}
			// gamma.remove(this.g.getChildrenOf(nObs));
		}

		Label alphaBetaGamma = alpha.conjunction(beta).conjunction(gamma);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append(", αβγ'=" + alphaBetaGamma);
		}

		if (alphaBetaGamma == null)
			return null;

		return alphaBetaGamma;
	}

	/**
	 * As {@link CSTN#makeBetaGammaDagger4qR3(LabeledNode, LabeledNode, LabeledNode, char, Label, Label)} without the check of node labels because for
	 * streamlined CSTN it is no necessary.
	 * 
	 * @param nS
	 * @param nZ
	 * @param nObs
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observator
	 * @param labelToClean
	 * @return αβγ'
	 */
	@Override
	// Visibility is package because there is Junit Class test that checks this method.
	Label makeBetaGammaDagger4qR3(final LabeledNode nS, final LabeledNode nZ, final LabeledNode nObs, final char observed,
			final Label labelFromObs, Label labelToClean) {

		final Label beta = new Label(labelToClean);
		beta.remove(observed);
		Label betaGamma = labelFromObs.conjunctionExtended(beta);
		return betaGamma;
	}

	/**
	 * @param addAuxiliaryConstraints the addAuxiliaryConstraints to set
	 */
	public void setAddAuxiliaryConstraints(boolean addAuxiliaryConstraints) {
		this.addAuxiliaryConstraints = addAuxiliaryConstraints;
	}
}
