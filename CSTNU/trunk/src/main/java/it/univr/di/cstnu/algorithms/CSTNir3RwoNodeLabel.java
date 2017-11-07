package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.io.PrintWriter;
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

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * In this class, a input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * The dynamic consistency check is done assuming Instantaneous Reaction semantics (cf. ICAPS 2016 paper, table 1).
 * This class uses only LP, qR0, and qR3* rules.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNir3RwoNodeLabel extends CSTNir3R {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNir3RwoNodeLabel.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static public final String VERSIONandDATE = "Version 1.0 - November, 07 2017";

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
		final CSTNir3RwoNodeLabel cstn = new CSTNir3RwoNodeLabel();

		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON)
			LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTNir3RwoNodeLabel.class.getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
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
	 * Default constructor.
	 */
	private CSTNir3RwoNodeLabel() {
		super();
	}

	/**
	 * Constructor for
	 * 
	 * @param g graph to check
	 */
	public CSTNir3RwoNodeLabel(LabeledIntGraph g) {
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
		for (final LabeledNode node : this.g.getVertices()) {
			node.setLabel(Label.emptyLabel);
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
	 * This method differs from {@link CSTNir3R#labeledPropagationLP(LabeledNode, LabeledNode, LabeledNode, LabeledIntEdge, LabeledIntEdge, LabeledIntEdge)}
	 * only in the fact that it does not check node labels.
	 * 
	 * @param nA CANNOT BE NULL!
	 * @param nB CANNOT BE NULL!
	 * @param nC CANNOT BE NULL!
	 * @param eAB CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eBC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @param eAC CANNOT BE NULL! This parameter is necessary only to speed up the method!
	 * @return true if a reduction has been applied.
	 */
	@Override
	boolean labeledPropagationLP(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB,
			final LabeledIntEdge eBC, LabeledIntEdge eAC) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false;
		String log = null;
		Label nAnCLabel = nA.getLabel().conjunction(nC.getLabel());
		if (nAnCLabel == null)
			return false;

		for (final Object2IntMap.Entry<Label> ABEntry : eAB.getLabeledValueSet()) {
			final Label labelAB = ABEntry.getKey();
			final int u = ABEntry.getIntValue();

			for (final Object2IntMap.Entry<Label> BCEntry : eBC.getLabeledValueSet()) {
				final Label labelBC = BCEntry.getKey();
				Label newLabelAC = labelAB.conjunction(labelBC);
				if (newLabelAC == null) {
					continue; // rule condition
				}
				final int v = BCEntry.getIntValue();
				/**
				 * 2017-05-04 Roberto verified that it is faster to propagate all values (positive and negative).
				 */
				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(u, v);
				int oldValue = eAC.getValue(newLabelAC);

				if (nA == nC) {
					if (sum >= 0) {
						// it would be a redundant edge
						continue;
					}
					// sum is negative!
					eAC.mergeLabeledValue(newLabelAC, sum);
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
							LOG.log(Level.FINER, log + "\n***\nFound a negative loop " + pairAsString(newLabelAC, sum) + " in the edge  " + eAC + "\n***");
						}
					}
					this.checkStatus.consistency = false;
					this.checkStatus.finished = true;
					this.checkStatus.labeledValuePropagationcalls++;
					return true;
				}
				if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
					continue;
				}
				// here sum has to be add!
				// I have to prepare the log before the execution of the merge!
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
	 * This method differs from {@link CSTNir3R#labelModificationqR0(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * only in the fact that it does not check node labels.
	 * 
	 * 
	 * @param nObs the observation node
	 * @param nZ
	 * @param eObsX the edge connecting nObs? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 */
	@Override
	boolean labelModificationqR0(final LabeledNode nObs, final LabeledNode nZ, final LabeledIntEdge eObsX) {// nX is assumed == Z!
		// Visibility is package because there is Junit Class test that checks this method.
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR0: start.");
			}
		}
		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Method labelModification qR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
		boolean ruleApplied = false, mergeStatus;

		final ObjectSet<Label> obsXLabelSet = eObsX.getLabeledValueMap().keySet();

		for (final Label l : obsXLabelSet) {
			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = eObsX.getValue(l);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w >= 0) {// Table 1 ICAPS paper.
				// When X==Z, w must be < 0 to apply rule. Remember, IR assumed!
				continue;
			}

			Label alphaPrime = new Label(l);
			alphaPrime.remove(p);

			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					logMessage = "qR0 simplifies a label of edge " + eObsX.getName()
							+ ":\nsource: " + nObs.getName() + " ---" + pairAsString(l, w) + "---> " + nZ.getName()// nX.getName()
							+ "\nresult: " + nObs.getName() + " ---" + pairAsString(alphaPrime, w) + "---> " + nZ.getName();// nX.getName();
				}
			}

			mergeStatus = eObsX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus) {
				ruleApplied = true;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, logMessage);
					}
				}
			}
			this.checkStatus.r0calls++;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR0: end.");
			}
		}
		return ruleApplied;
	}
	
	/**
	 * This method differs from {@link CSTNir3R#labelModificationqR3(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * only in the fact that it does not check node labels.
	 * 
	 * @param nS node
	 * @param nZ node IT MUST BE THE Z node of the graph. NO check is made!
	 * @param eSZ LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	@Override
	// Visibility is package because there is Junit Class test that checks this method.
	// boolean labelModificationR3(final LabeledNode nS, final LabeledNode nZ, final LabeledNode Z, final LabeledIntEdge eSD) {
	boolean labelModificationqR3(final LabeledNode nS, final LabeledNode nZ, final LabeledIntEdge eSZ) {// it assumed that nZ is the Z node!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR3*: start.");
			}
		}
		boolean ruleApplied = false;

		ObjectList<LabeledIntEdge> Obs2ZEdges = this.getEdgeFromObserversToNode(nZ);
		if (Obs2ZEdges.isEmpty())
			return false;

		final ObjectSet<Label> SZLabelSet = eSZ.getLabeledValueMap().keySet();

		final Label allLiteralsSZ = new Label();
		for (Label l : SZLabelSet) {
			allLiteralsSZ.conjunctExtended(l);
		}
		// check each edge from an observator to Z.
		for (final LabeledIntEdge eObsZ : Obs2ZEdges) {// usually the number of observator is lesser than the number of labels in SZ.

			final LabeledNode nObs = this.g.getSource(eObsZ);
			if (nObs == nS)
				continue;

			final char p = nObs.getPropositionObserved();

			if (!allLiteralsSZ.contains(p)) {
				// no label in nS-->Z contain any literal of p.
				continue;
			}
			// all labels from current Obs
			for (final Object2IntMap.Entry<Label> entryObsZ : eObsZ.getLabeledValueSet()) {
				final int w = entryObsZ.getIntValue();
				// if (w > 0 || (w == 0 && nD == nZ)) { // Table 1 ICAPS
				if (w >= 0) { // 2017-10-17 nD==nZ
					// (w == 0 && nD==Z), it means that P? is executed at 0. For IR, it cannot be modified.
					continue;
				}

				final Label ObsZLabel = entryObsZ.getKey();

				for (final Label SDLabel : SZLabelSet) {
					if (SDLabel == null || !SDLabel.contains(p)) {
						continue;
					}

					final int v = eSZ.getValue(SDLabel);
					if (v == Constants.INT_NULL) {
						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
						continue;
					}

					Label newLabel = makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsZLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}
					final int max = Math.max(w, v);

					ruleApplied = eSZ.mergeLabeledValue(newLabel, max);
					if (ruleApplied) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "qR3* adds a labeled value to edge " + eSZ.getName() + ":\n"
										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsZLabel, w) + "---> " + nZ.getName()
										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
										+ "\nresult: add " + nZ.getName() + " <---" + pairAsString(newLabel, max) + "--- " + nS.getName());
							}
						}
						this.checkStatus.r3calls++;
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR3*: end.");
			}
		}
		return ruleApplied;
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
	 * @param g the g to set
	 */
	@Override
	void setG(LabeledIntGraph g) {
		super.setG(g);
		this.horizon = Constants.INT_POS_INFINITE;
	}
}
