package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.jung.io.GraphIOException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU).
 * It doesn't manage guarded link!
 * It is based on instantaneous reaction!
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNU extends CSTNir {
	/**
	 * Simple class to represent the status of the checking algorithm during an execution.<br>
	 * controllability = super.consistency!
	 * 
	 * @author Roberto Posenato
	 */
	public static class CSTNUCheckStatus extends CSTNCheckStatus {

		// controllability = super.consistency!

		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings("javadoc")
		public int upperCaseRuleCalls = 0, lowerCaseRuleCalls = 0, crossCaseRuleCalls = 0, letterRemovalRuleCalls = 0;

		@Override
		public String toString() {
			return ("The check is" + (this.finished ? " " : " NOT") + " finished after " + this.cycles + " cycle(s).\n"
					+ ((this.finished) ? "the controllability check has determined that given network is" + (this.consistency ? " " : " NOT ")
							+ "dynamic controllable.\n" : "")
					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
					+ "Labeled Propagation Rule has been applied " + this.labeledValuePropagationcalls + " times.\n"
					+ "Labeled Upper Case Rule has been applied " + this.upperCaseRuleCalls + " times.\n"
					+ "Labeled Lower Case Rule has been applied " + this.lowerCaseRuleCalls + " times.\n"
					+ "Labeled Cross-Lower Case Rule has been applied " + this.crossCaseRuleCalls + " times.\n"
					+ "Labeled Letter Removal (LLR) Rule has been applied " + this.letterRemovalRuleCalls + " times.\n"
					+ "Negative qLoops: " + this.qAllNegLoop + "\n"
					+ "Negative qLoops with positive edge: " + this.qSemiNegLoop + "\n"
					+ "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)");
		}

		public void reset() {
			super.reset();
			this.upperCaseRuleCalls = 0;
			this.lowerCaseRuleCalls = 0;
			this.crossCaseRuleCalls = 0;
			this.letterRemovalRuleCalls = 0;
		}

		/**
		 * @return the value of controllability
		 */
		public boolean getControllability() {
			return this.consistency;
		}

		/**
		 * Set the controllability value!
		 * 
		 * @param controllability
		 */
		public void setControllability(boolean controllability) {
			this.consistency = controllability;
		}
	}

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNU.class.getName());

	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.2 - June, 14 2016";
	@SuppressWarnings("hiding")
	static final String VERSIONandDATE = "Version  4 - April, 5 2017";

	/**
	 * Returns the sum of all negative values (ignoring their labels) present in the edges of a graph. If an edge has more than one negative values, only
	 * the minimum among them is considered. For contingent link, also the lower case value is considered.
	 *
	 * @param g
	 * @return the sum of all negative value (negative value)
	 */
	static int getSumOfNegativeEdgeValues(final LabeledIntGraph g) {
		int sum = 0;
		if ((g == null) || (g.getEdgeCount() == 0))
			return sum;

		for (final LabeledIntEdge e : g.getEdges()) {
			final int min = e.getMinValue();
			// int minLC = e.getMinLowerLabeledValue();
			if ((min != Constants.INT_NULL) && (min < 0)) {
				// if (minLC != null) {
				// if (min.compareTo(minLC) > 0 ) min = minLC;
				// }
				sum += min;
			}
		}
		LOG.finer("The sum of all negative values is " + sum);
		return sum;
	}

	/**
	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(final String[] args) throws FileNotFoundException, GraphIOException {
		LOG.finest("Start...");
		final CSTNU cstnu = new CSTNU();

		if (!cstnu.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (cstnu.versionReq) {
			System.out.println(CSTNU.class.getName() + " " + CSTNU.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		LOG.finest("Loading graph...");
		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstnu.fInput, LabeledIntTreeMap.class);
		cstnu.setG(graphMLReader.readGraph());

		LOG.finest("LabeledIntGraph loaded!");

		LOG.finest("DC Checking...");
		CSTNUCheckStatus status;
		try {
			status = cstnu.dynamicControllabilityCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		LOG.finest("LabeledIntGraph minimized!");
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given CSTNU is Dynamic controllable!");
			} else {
				System.out.println("The given CSTNU is NOT Dynamic controllable!");
			}
			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}

		if (cstnu.fOutput != null) {
			final GraphMLWriter graphWriter = new GraphMLWriter(new StaticLayout<>(cstnu.g));
			try {
				graphWriter.save(cstnu.g, new PrintWriter(cstnu.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @param value
	 * @param nodeName
	 * @param label
	 * @param lower
	 * @return the conventional representation of a labeled value
	 */
	static final String tripleAsString(int value, ALabel nodeName, Label label, boolean lower) {
		return LabeledContingentIntTreeMap.entryAsString(label, value, nodeName, lower);
	}

	/**
	 * Default constructor, package use only!
	 */
	CSTNU() {
		super();
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 */
	public CSTNU(LabeledIntGraph g) {
		super(g);
	}

	/**
	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property:<br>
	 * any labeled valued of the edge is consistent and subsumes both labels of two endpoints.
	 *
	 * @param source the source node of the edge.
	 * @param destination the destination node of the edge.
	 * @param e edge representing a labeled constraint.
	 * @param hasToBeFixed true for fixing well-definition errors that can be fixed!
	 * @return false if the check fails, true otherwise
	 * @throws WellDefinitionException
	 */
	boolean checkWellDefinitionProperty1and3(final LabeledNode source, final LabeledNode destination, final LabeledIntEdge e, boolean hasToBeFixed)
			throws WellDefinitionException {

		super.checkWellDefinitionProperty1and3(source, destination, e, hasToBeFixed);
		final Label conjunctedLabel = source.getLabel().conjunction(destination.getLabel());

		// check the upper case labeled values
		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getUpperLabelSet()) {
			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
				final String msg = "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, msg);
				}
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		// check the lower case labeled values
		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getLowerLabelSet()) {
			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
				final String msg = "Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, msg);
				}
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		return true;
	}

	/**
	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
	 *
	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException
	 *             if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	public CSTNUCheckStatus dynamicControllabilityCheck() throws WellDefinitionException {

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "\nStarting checking CSTNU dynamic controllability...\n");
		}

		try {
			initUpperLowerLabelDataStructure();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		Collection<LabeledIntEdge> edgesToCheck = this.g.getEdges();

		final int n = this.g.getVertexCount();
		int k = this.g.getUpperLabeledEdges().size();
		if (k == 0) {
			k = 1;
		}
		int p = this.g.getPropositions().size();
		if (p == 0) {
			p = 1;
		}
		int maxCycles = p * ((n * n) + (n * k) + k);// cstnu
		if (maxCycles == 0) {
			maxCycles = 2;
		}
		LOG.info("The maximum number of possible cycles is " + maxCycles);

		int i;
		this.checkStatus.finished = false;
		Instant startInstant = Instant.now();
		for (i = 1; i <= maxCycles && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
			}
			this.checkStatus = this.oneStepDynamicControllability(edgesToCheck);
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
			}
			if (this.checkStatus.consistency && !this.checkStatus.finished) {
				if (LOG.isLoggable(Level.FINER)) {
					StringBuilder log = new StringBuilder();
					log.append("During the check n. " + i + ", " + edgesToCheck.size()
							+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
					for (LabeledIntEdge e : edgesToCheck) {
						if (e.size() == 0)
							continue;
						log.append("Edge " + e + "\n");
					}
					LOG.log(Level.FINER, log.toString());
				}
			}
		}
		if (this.checkStatus.consistency && this.checkStatus.finished && i <= maxCycles) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, all possible propagations have done."
						+ "\nNow it is necessary to check that AllMax Projection network is consistent.");
				LOG.info("AllMax Projection check starts...");
			}
			LabeledIntGraph allMaxCSTN = makeAllMaxProjection();
			CSTN cstnChecker = new CSTN(allMaxCSTN);
			CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck();
			LOG.info("AllMax Projection network check done.\n");
			if (!cstnStatus.consistency) {
				LOG.info("The AllMax Projection network has at least one negative loop. The original network cannot be DC!");
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
			}
		}
		Instant endInstant = Instant.now();
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, it has been stated that the network is NOT DC controllable.\nStatus: " + this.checkStatus);
				LOG.log(Level.FINE, "Final NOT DC controllable network: " + this.g);
			}
			return ((CSTNUCheckStatus) this.checkStatus);
		}

		if (i > maxCycles && !this.checkStatus.finished) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
				LOG.log(Level.FINE, "Last NOT DC controllable network determined: " + this.g);
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			return ((CSTNUCheckStatus) this.checkStatus);
		}

		// controllable && finished
		if (LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "Stable state reached. The newtork is DC controllable.\nNumber of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles
					+ ".\nStatus: " + this.checkStatus);
		}
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * Call {@link CSTN#initAndCheck()} and, then, check all contingent links.
	 * 
	 * @return true if the check is successful. The input g results to be modified by the method.
	 * @throws WellDefinitionException if the graph is null or it is not well formed.
	 */
	public boolean initUpperLowerLabelDataStructure() throws WellDefinitionException {

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "Starting checking graph as CSTNU well-defined instance...");
		}

		// check underneath CSTN
		super.initAndCheck();
		this.checkStatus.initialized = false;

		// Contingent link have to be checked AFTER WD1 and WD3 have been checked and fixed!
		for (final LabeledIntEdge e : this.g.getEdges()) {
			if (!e.isContingentEdge()) {
				continue;
			}
			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);
			/***
			 * Manage contingent link.
			 */
			final Label conjunctedLabel = s.getLabel().conjunction(d.getLabel());
			final int initialValue = e.getValue(conjunctedLabel); // we consider only one value, the one with label == conjunctedLabel
			if (initialValue == Constants.INT_NULL) {
				if (e.lowerLabelSize() == 0 && e.upperLabelSize() == 0) {
					throw new IllegalArgumentException(
							"Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
				}
			}
			if (initialValue == 0)
				throw new IllegalArgumentException("Contingent edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0<x<y<∞.");

			LabeledIntEdge eInverted = this.g.findEdge(d, s);
			LOG.finer("Edge " + e + " is contingent. Found its companion: " + eInverted);
			if (eInverted == null) {
				throw new IllegalArgumentException("Contingent edge " + e + " is alone. The companion contingent edge between " + d.getName()
						+ " and " + s.getName() + " does not exist. It must!");
			}
			if (!eInverted.isContingentEdge()) {
				throw new IllegalArgumentException("Edge " + e + " is contingent while the companion edge " + eInverted + " is not contingent!\nIt must be!");
			}
			/**
			 * Memo.
			 * If current initialValue is negative, current edge is the lower bound C--->A. The lower case labeled value has to be put in the inverted edge.
			 * If current initialValue is positive, current edge is the upper bound A--->C. The upper case labeled value has to be put in the inverted edge.
			 * if current initialValue is undefined, then we assume that the contingent link is already set.
			 */
			if (initialValue != Constants.INT_NULL) {
				int eInvertedInitialValue;
				int lowerCaseValue = Constants.INT_NULL;
				int upperCaseValue = Constants.INT_NULL;
				eInvertedInitialValue = eInverted.getValue(conjunctedLabel);

				if (initialValue < 0) {
					// current edge is the lower bound.
					ALabel sourceALabel = new ALabel(s.getName(), this.g.getALabelAlphabet());
					lowerCaseValue = eInverted.getLowerLabelValue(conjunctedLabel, sourceALabel);
					if (lowerCaseValue != Constants.INT_NULL && -initialValue != lowerCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is contingent with a negative value and the inverted " + eInverted + " already contains a lower case value: "
										+ tripleAsString(lowerCaseValue, sourceALabel, conjunctedLabel, true) + ".");
					}
					if (lowerCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is contingent with a negative value but the inverted " + eInverted
								+ " does not contain a lower case value neither a proper initial value. ");
					}

					if (lowerCaseValue == Constants.INT_NULL) {
						lowerCaseValue = -initialValue;
						eInverted.mergeLowerLabelValue(conjunctedLabel, sourceALabel, lowerCaseValue);
						e.removeLabel(conjunctedLabel); // 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
						LOG.warning("Insert the lower label value: " + tripleAsString(lowerCaseValue, sourceALabel, conjunctedLabel, true) + " to edge "
								+ eInverted);
						if (eInvertedInitialValue != Constants.INT_NULL) {
							upperCaseValue = -eInvertedInitialValue;
							e.mergeUpperLabelValue(conjunctedLabel, sourceALabel, upperCaseValue);
							eInverted.removeLabel(conjunctedLabel);// 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
							LOG.warning("Insert the upper label value: " + tripleAsString(upperCaseValue, sourceALabel, conjunctedLabel, false) + " to edge "
									+ e);
						}
					}
				} else {
					// e : A--->C
					// eInverted : C--->A
					ALabel destALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
					upperCaseValue = eInverted.getUpperLabelValue(conjunctedLabel, destALabel);
					if (upperCaseValue != Constants.INT_NULL && -initialValue != upperCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is contingent with a positive value and the inverted " + eInverted + " already contains a upper case value: "
										+ tripleAsString(upperCaseValue, destALabel, conjunctedLabel, true) + ".");
					}
					if (upperCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is contingent with a positive value but the inverted " + eInverted
								+ " does not contain a upper case value neither a proper initial value. ");
					}
					if (upperCaseValue == Constants.INT_NULL) {
						upperCaseValue = -initialValue;
						eInverted.mergeUpperLabelValue(conjunctedLabel, destALabel, upperCaseValue);
						LOG.warning("Insert the upper label value: " + tripleAsString(upperCaseValue, destALabel, conjunctedLabel, false) + " to edge "
								+ eInverted);
						e.removeLabel(conjunctedLabel); // 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
						if (eInvertedInitialValue != Constants.INT_NULL) {
							lowerCaseValue = -eInvertedInitialValue;
							e.mergeLowerLabelValue(conjunctedLabel, destALabel, lowerCaseValue);
							eInverted.removeLabel(conjunctedLabel);// 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
							LOG.warning("Insert the lower label value: " + tripleAsString(lowerCaseValue, destALabel, conjunctedLabel, true) + " to edge "
									+ e);
						}
					}

				}
			}
		} // end contingent edges cycle

		// init CSTNU structures.
		this.g.getLowerLabeledEdges();
		this.checkStatus.initialized = true;

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "Checking graph as CSTNU well-defined instance finished!\n");
		}

		return true;
	}

	/**
	 * <h1>Labeled Cross-Lower Case (LCLC)</h1>
	 * See TIME 17 paper.
	 * 
	 * <pre>
	 *     v,ℵ,β           u,c,α            
	 * X &lt;------------ C &lt;------------ A 
	 * adds 
	 *             u+v,ℵ,αβ
	 * X &lt;----------------------------A
	 * 
	 * if αβ∈P*, C ∉ ℵ, (v<0 or (v=0 and C!=X)
	 * </pre>
	 * 
	 * @param nA
	 * @param nC
	 * @param nX
	 * @param eAC CANNOT BE NULL
	 * @param eCX CANNOT BE NULL
	 * @param eAX CANNOT BE NULL
	 * @return true if the rule has been applied.
	 */
	boolean labeledCrossLowerCaseRule(final LabeledNode nA, final LabeledNode nC, final LabeledNode nX, final LabeledIntEdge eAC, final LabeledIntEdge eCX,
			final LabeledIntEdge eAX) {

		boolean ruleApplied = false;
		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> ACMap = eAC.getLowerLabelSet();
		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> CXMap = eCX.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		if (ACMap.isEmpty() || CXMap.isEmpty())
			return false;

		LOG.finer("LCLC: start.");

		ALetter aLnC = new ALetter(nC.getName());

		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryCX : CXMap) {
			final ALabel aleph = entryCX.getKey().getValue();

			// Rule condition: upper case label cannot be equal or contain nC name
			if (aleph.contains(aLnC))
				continue;

			final int v = entryCX.getIntValue();
			if (v > 0 || (v == 0 && nX == nC))
				continue; // Rule condition!

			final Label beta = entryCX.getKey().getKey();

			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryAC : ACMap) {
				final int u = entryAC.getIntValue();
				final Label alpha = entryAC.getKey().getKey();
				final Label alphaBeta = beta.conjunction(alpha);
				if (alphaBeta == null)
					continue;

				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(v, u);

				final int oldZ = (aleph.isEmpty()) ? eAX.getValue(alphaBeta) : eAX.getUpperLabelValue(alphaBeta, aleph);
				final String oldAX = eAX.toString();

				boolean localApp = (aleph.isEmpty()) ? eAX.mergeLabeledValue(alphaBeta, z) : eAX.mergeUpperLabelValue(alphaBeta, aleph, z);

				if (localApp) {
					ruleApplied = true;
					if (aleph.isEmpty()) {
						((CSTNUCheckStatus) this.checkStatus).lowerCaseRuleCalls++;
					} else {
						((CSTNUCheckStatus) this.checkStatus).crossCaseRuleCalls++;
					}
					LOG.finer("LCLC applied to edge " + oldAX + ":\npartic: " + nX.getName() + " <---" + tripleAsString(v, aleph, beta, false)
							+ "--- " + nC.getName() + " <---" + tripleAsString(u, entryAC.getKey().getValue(), alpha, true) + "--- " + nA.getName()
							+ "\nresult: " + nX.getName()
							+ " <---" + tripleAsString(z, aleph, alphaBeta, false) + "--- " + nA.getName() + "; oldValue: " + Constants.formatInt(oldZ));
				}
				if (isNewLabeledValueANegativeLoop(alphaBeta, z, nA, nX, eAX)) {
					this.checkStatus.consistency = false;
					this.checkStatus.finished = true;
					return ruleApplied;
				}
			}
		}
		LOG.finer("LCLC: end.");
		return ruleApplied;
	}

	/**
	 * <h1>Labeled LetterRemoval* (LLR*)</h1>
	 * See TIME 17.
	 *
	 * <pre>
	 *       x,c,α          v,ℵ,β        
	 * C &lt;-------------A &lt;------------ X 
	 * adds 
	 *     v,ℵ',β
	 * A &lt;-----------C
	 *  
	 * if C ∈ ℵ, v≥−x, β entails α.
	 * ℵ'=ℵ'/C
	 * </pre>
	 * 
	 * @param nX
	 * @param nA
	 * @param eXA
	 * @return true if the reduction has been applied.
	 */
	boolean labeledLetterRemovalRule(final LabeledNode nX, final LabeledNode nA, final LabeledIntEdge eXA) {
		boolean ruleApplied = false;
		LOG.finer("LLR*: start.");

		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> ABMap;

		for (final Object2IntMap.Entry<Entry<Label, ALabel>> upperCaseEntryOfXA : eXA.getUpperLabelSet()) {
			final ALabel aleph = upperCaseEntryOfXA.getKey().getValue();
			final Label beta = upperCaseEntryOfXA.getKey().getKey();
			final int v = upperCaseEntryOfXA.getIntValue();
			for (ALetter nodeLetter : aleph) {
				LabeledNode nC = this.g.getNode(nodeLetter.toString());
				LabeledIntEdge AC = this.g.findEdge(nA, nC);
				if (AC == null || (ABMap = AC.getLowerLabelSet()).size() == 0)
					continue;
				for (final Object2IntMap.Entry<Entry<Label, ALabel>> lowerCaseEntryOfAC : ABMap) {
					final Label alpha = lowerCaseEntryOfAC.getKey().getKey();
					final ALabel lowerCaseLetter = lowerCaseEntryOfAC.getKey().getValue();
					int x = lowerCaseEntryOfAC.getIntValue();
					if (x == Constants.INT_NULL || v < -x || !beta.subsumes(alpha))
						continue;
					final int oldZ = eXA.getUpperLabelValue(beta, aleph);
					final String oldXA = eXA.toString();

					ALabel aleph1 = new ALabel(aleph);
					aleph1.remove(nodeLetter);

					boolean mergeStatus = (aleph1.isEmpty()) ? eXA.mergeLabeledValue(beta, v) : eXA.mergeUpperLabelValue(beta, aleph1, v);
					if (mergeStatus) {
						// it is necessary to block further add of the same values
						eXA.putUpperLabeledValueToRemovedList(beta, aleph, v);

						ruleApplied = true;
						((CSTNUCheckStatus) this.checkStatus).letterRemovalRuleCalls++;
						LOG.finer("LLR* applied to edge " + oldXA + ":\n" + "partic: "
								+ nC + " <---" + tripleAsString(x, lowerCaseLetter, alpha, true) + "--- " + nA.getName()
								+ " <---" + tripleAsString(v, aleph, beta, false) + "--- " + nX.getName()
								+ "\nresult: " + nA.getName() + " <---" + tripleAsString(v, aleph1, beta, false) + "--- " + nX.getName()
								+ "; oldValue: " + Constants.formatInt(oldZ));
					}
				}
			}
		}
		// I have not a clear idea if the above is more efficient of the below code!
		// for (final LabeledIntEdge AB : lowerCaseEdge) {
		// ABMap = AB.getLowerLabelSet();
		// if (ABMap.size() == 0)
		// continue;
		// nB = this.g.getDest(AB);
		//
		// for (final Object2IntMap.Entry<Entry<Label, String>> upperCaseEntryOfCA : eCA.getUpperLabelSet()) {
		// final String upperCaseNodeName = upperCaseEntryOfCA.getKey().getValue();
		// if (!upperCaseNodeName.equals(nB.getName()))
		// continue;// Rule condition!
		// final Label l2 = upperCaseEntryOfCA.getKey().getKey();
		// final int z = upperCaseEntryOfCA.getValue();
		//
		// for (final Object2IntMap.Entry<Entry<Label, String>> lowerCaseEntryOfAB : ABMap) {
		// final Label l1 = lowerCaseEntryOfAB.getKey().getKey();
		//
		// int x = lowerCaseEntryOfAB.getValue();
		// if (x == Constants.INT_NULL || z < -x)
		// continue;
		// if (!l1.isConsistentWith(l2)) {
		// if (LOG.isLoggable(Level.FINEST)) {
		// LOG.log(Level.FINEST,
		// "Case Label Removal not applied to edge " + eCA + ": label l1 '" + l1 + "' is not consistent with l2 '" + l2 + "'");
		// }
		// continue;
		// }
		// final int oldZ = eCA.getValue(l2);
		// final String oldCA = eCA.toString();
		//
		// if (eCA.mergeLabeledValue(l2, z)) {
		// reductionApplied = true;
		// ((CSTNUCheckStatus) this.checkStatus).caseLabelRemovalRuleCalls++;
		// LOG.finer("Case Label Removal applied to edge " + oldCA + ":\n" + "partic: "
		// + nB.getName() + " <---" + tripleAsString(l1, x, lowerCaseEntryOfAB.getKey().getValue(), true) + "--- " + nA.getName()
		// + " <---" + tripleAsString(l2, z, upperCaseNodeName, false) + "--- " + nC.getName()
		// + "\nresult: " + nA.getName() + " <---" + pairAsString(l2, z) + "--- " + nC.getName()
		// + "; oldValue: " + Constants.formatInt(oldZ));
		// }
		// }
		// }
		// }
		LOG.finer("LLR*: end.");
		return ruleApplied;
	}

	/**
	 * <h1>LabeledPropagation (LP)</h1>
	 * Apply new "no case + upper case" rule.
	 * See TIME17 paper.
	 * 
	 * <pre>
	 *     v,ℵ_2,β           u,ℵ_1,α        
	 * Y &lt;------------ W &lt;------------ X 
	 * adds 
	 *     v+u,ℵ_1ℵ_2,γ
	 * Y &lt;------------------------------X
	 * 
	 * if
	 * 
	 * 1) γ=αβ∈P* ℵ_1∩ℵ_2=∅ x+y≤0 or
	 * 2) γ=(α*β)†∈Q, u<0, ℵ_1∩ℵ_2=∅ x+y≤0
	 * </pre>
	 * 
	 * @param nX
	 * @param nW
	 * @param nY
	 * @param eXW CANNOT BE NULL
	 * @param eWY CANNOT BE NULL
	 * @param eXY CANNOT BE NULL
	 * @return true if a reduction is applied at least
	 */
	boolean labeledPropagationRule(final LabeledNode nX, final LabeledNode nW, final LabeledNode nY, final LabeledIntEdge eXW, final LabeledIntEdge eWY,
			final LabeledIntEdge eXY) {

		// if (C.equalsByName(D) || A.equalsByName(D)) continue;// it is useless to consider self loop. NO! it is necessary with guarded links!
		boolean ruleApplied = false;

		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> XWMap = eXW.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> WYMap = eWY.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		if (XWMap.size() == 0 || WYMap.size() == 0)
			return false;
		LOG.finest("LP: start.");

		Label nXnYLabel = nX.getLabel().conjunctionExtended(nY.getLabel());

		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryXW : XWMap) {
			final Label alpha = entryXW.getKey().getKey();
			final ALabel upperCaseNodeXW = entryXW.getKey().getValue();
			final int u = entryXW.getIntValue();

			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryWY : WYMap) {
				final Label beta = entryWY.getKey().getKey();
				final ALabel upperCaseNodeWY = entryWY.getKey().getValue();
				final int v = entryWY.getIntValue();

				final ALabel aleph1 = upperCaseNodeXW.conjunction(upperCaseNodeWY);

				// TODO: Verify the following conditions!
				if (!upperCaseNodeXW.intersect(upperCaseNodeWY).isEmpty() // this case would make a cycle on upper case values!
						|| (upperCaseNodeXW.size() == 1 && upperCaseNodeWY.size() == 0)// this case would forward propagate a simple upper case value through a
																						// standard edge!
				) {
					continue;
				}

				Label alphaBeta = (u < 0) ? alpha.conjunctionExtended(beta) : alpha.conjunction(beta);
				if (alphaBeta == null)
					continue;

				if (alphaBeta.containsUnknown()) {
					this.removeChildrenOfUnknown(alphaBeta);
				}

				if (!alphaBeta.subsumes(nXnYLabel)) {
					LOG.log(Level.FINEST, "New alphaBeta label " + alphaBeta + " does not subsume node labels " + nXnYLabel + ". New value cannot be added!");
					continue;
				}

				/**
				 * 2017-05-04 Roberto verifies that it is faster to propagate all values (positive and negative).
				 */
				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(u, v);

				boolean emptyUpperCase = aleph1.isEmpty();
				final int oldValue = (emptyUpperCase) ? eXY.getValue(alphaBeta) : eXY.getUpperLabelValue(alphaBeta, aleph1);
				final String oldXY = eXY.toString();

				String logMsg = "LP applied to edge " + oldXY + ":\n" + "partic: "
						+ nY.getName() + " <---" + tripleAsString(v, upperCaseNodeWY, beta, false) + "--- " + nW.getName() + " <---"
						+ tripleAsString(u, upperCaseNodeXW, alpha, false) + "--- " + nX.getName()
						+ "\nresult: "
						+ nY.getName() + " <---" + tripleAsString(sum, aleph1, alphaBeta, false) + "--- " + nX.getName()
						+ "; old value: " + Constants.formatInt(oldValue);

				if (nX == nY) {
					if (sum >= 0) {
						// it would be a redundant edge
						continue;
					}
					// sum is negative!
					if (!alphaBeta.containsUnknown()) {
						if (emptyUpperCase) {
							eXY.mergeLabeledValue(alphaBeta, sum);
						} else {
							eXY.mergeUpperLabelValue(alphaBeta, aleph1, sum);
						}
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, logMsg + "\n***\nFound a negative loop " + tripleAsString(sum, aleph1, alphaBeta, false) + " in the edge  "
									+ eXY + "\n***");
						}
						this.checkStatus.consistency = false;
						this.checkStatus.finished = true;
						if (emptyUpperCase) {
							this.checkStatus.labeledValuePropagationcalls++;
						} else {
							((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
						}
						return true;
					}
					sum = Constants.INT_NEG_INFINITE;
				} else {
					// in the case of A != C, a value is stored only if it is more negative than the current one.
					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						continue;
					}
				}

				boolean mergeStatus = (emptyUpperCase) ? eXY.mergeLabeledValue(alphaBeta, sum) : eXY.mergeUpperLabelValue(alphaBeta, aleph1, sum);

				if (mergeStatus) {
					ruleApplied = true;
					if (emptyUpperCase) {
						this.checkStatus.labeledValuePropagationcalls++;
					} else {
						((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
					}
					if (LOG.isLoggable(Level.FINER)) {
						LOG.finer("LP applied to edge " + oldXY + ":\n" + "partic: "
								+ nY.getName() + " <---" + tripleAsString(v, upperCaseNodeWY, beta, false) + "--- " + nW.getName() + " <---"
								+ tripleAsString(u, upperCaseNodeXW, alpha, false) + "--- " + nX.getName()
								+ "\nresult: "
								+ nY.getName() + " <---" + tripleAsString(sum, aleph1, alphaBeta, false) + "--- " + nX.getName()
								+ "; old value: " + Constants.formatInt(oldValue));
					}
				}
			}
		}
		LOG.finest("LP: end.");
		return ruleApplied;
	}

	/**
	 * Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Instantaneous reaction semantics is assumed.</b>
	 * <b>This differs from CSTN.labelModificationR0 in the checking also upper case value</b>
	 * 
	 * <pre>
	 * R0:
	 * P? --[α, C p, w]--&gt; X 
	 * changes in 
	 * P? --[α', C, w]--&gt; X when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * C can be ◇ or an upper letter.
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * Rule qR0 has X==Z.
	 * 
	 * @param nObs the observation node
	 * @param nX the other node
	 * @param nZ
	 * @param ePX the edge connecting P? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge ePX) {

		boolean ruleApplied = false, mergeStatus = false;
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
		if (nX.getLabel().contains(p)) {// Table 2 ICAPS
			// if (LOG.isLoggable(Level.FINER)) {
			// LOG.log(Level.FINER, "R0: Proposition '" + p + "' is present in the X label '" + X.getLabel() + "'. R0 cannot be applied.");
			// }
			return false;
		}

		// normal and upper-case values
		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> allLabeledValueSet = ePX.getAllUpperCaseAndOrdinaryLabeledValuesSet();
		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryObs : allLabeledValueSet) {
			Label alpha = entryObs.getKey().getKey();
			if (alpha == null || !alpha.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}
			final ALabel aLabel = entryObs.getKey().getValue();
			// It is necessary to re-check if the value is still present.Verified that it is necessary on Nov, 26 2015
			final int w = (aLabel.isEmpty()) ? ePX.getValue(alpha) : ePX.getUpperLabelValue(alpha, aLabel);
			if (w == Constants.INT_NULL) {
				continue;
			}

			if (w >= 0) {// Table 1 ICAPS paper.
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, alpha);
			if (alphaPrime == null) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (LOG.isLoggable(Level.FINER)) {
				logMessage = "R0 simplifies a label of edge " + ePX.getName()
						+ ":\nsource: " + nObs.getName() + " ---" + tripleAsString(w, aLabel, alpha, false) + "---> " + nX.getName()
						+ "\nresult: " + nObs.getName() + " ---" + tripleAsString(w, aLabel, alphaPrime, false) + "---> " + nX.getName();
			}

			if (aLabel.isEmpty()) {
				ePX.putLabeledValueToRemovedList(alpha, w);
				mergeStatus = ePX.mergeLabeledValue(alphaPrime, w);
			} else {
				ePX.putUpperLabeledValueToRemovedList(alpha, aLabel, w);
				mergeStatus = ePX.mergeUpperLabelValue(alphaPrime, aLabel, w);
			}
			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
				ruleApplied = true;
				LOG.log(Level.FINER, logMessage);
			}
			this.checkStatus.r0calls++;
			if (CSTN.isNewLabeledValueANegativeLoop(alphaPrime, w, nObs, nX, ePX)) {
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
				return ruleApplied;
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: end.");
		}
		return ruleApplied;
	}

	/**
	 * <h1>Rule R3*</h1>
	 * <b>Instantaneous reaction semantics is assumed.</b><br>
	 *
	 * <pre>
	 * if P? --[w, U, αβ]--&gt; nD &lt;--[v, U', βγp]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[max{w,v}, U'', αβγ']-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * U=◇, U' can be upper-case letter or ◇ when nD!= Z. They can be any but not both upper-case letter when nD = Z.
	 * U'' is ◇ if U = U' = ◇, U if U' = ◇, U' if  U = ◇
	 * </pre>
	 *
	 * <h2>Rule qR3*</h2>
	 * 
	 * <pre>
	 * if P? --[w, U, γ]--&gt; Z &lt;--[v, U', βθp']-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[max{w,v}, U'', (γ★β)†]-- nS
	 * where:
	 * β, θ and γ are in Q*.
	 * p' is p or ¬p or ¿p
	 * γ does not contain p' and any of its children.
	 * β does not contain any children of p'.
	 * θ contains only children of p'.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * (γ★β)† is the extended conjunction without any children of unknown literals.
	 * U=◇, U' can be upper-case letter or ◇ when nD!= Z. They can be any but not both upper-case letter when nD = Z.
	 * U'' is ◇ if U = U' = ◇, U if U' = ◇, U' if  U = ◇
	 * </pre>
	 * 
	 * @param nS node
	 * @param nD node
	 * @param nZ
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSD) {

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R3: start.");
		}
		boolean ruleApplied = false;

		ObjectArraySet<LabeledIntEdge> Obs2nDEdges = getEdgeFromObservatorsToNode(nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> SDLabeledValueSet = eSD.getAllUpperCaseAndOrdinaryLabeledValuesSet();

		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = this.g.getSource(eObsD);

			if (nObs.equalsByName(nS))
				continue;
			final char p = nObs.getPropositionObserved();

			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// Table 2 ICAPS
				// if (LOG.isLoggable(Level.FINEST)) {
				// LOG.log(Level.FINEST, "R3: Proposition '" + p + "' is present in the nS label '" + nS.getLabel() + "' or nD label '" + nD.getLabel()
				// + "'. R3 cannot be applied.");
				// }
				continue;
			}
			// all labels from Obs
			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryObsD : eObsD.getAllUpperCaseAndOrdinaryLabeledValuesSet()) {
				final int w = entryObsD.getIntValue();
				if (w > 0 || (w == 0 && nD == nZ)) { // Table 1 ICAPS
					// (w == 0 && nD==Z), it means that P? is executed at 0. So, even if v==0 (it cannot be v>0),
					// the constraint does not imply an implicit constraint (stripping p). So, we don't touch the constraint.
					continue;
				}

				final Label ObsDLabel = entryObsD.getKey().getKey();
				final ALabel ObsDUpperCaseLetter = entryObsD.getKey().getValue();

				Label SDLabel;
				ALabel newUpperCaseLetter;
				for (final Object2IntMap.Entry<Entry<Label, ALabel>> SDLabelEntry : SDLabeledValueSet) {
					if (SDLabelEntry == null || !(SDLabel = SDLabelEntry.getKey().getKey()).contains(p)) {
						continue;
					}

					ALabel SDUpperCaseLetter = SDLabelEntry.getKey().getValue();

					if (ObsDUpperCaseLetter.equals(ALabel.emptyLabel)) {
						// ObsDUpperCaseLetter == ◇
						newUpperCaseLetter = SDUpperCaseLetter;
					} else {
						if (!SDUpperCaseLetter.equals(ALabel.emptyLabel)) {
							continue;
						}
						if (!nD.equalsByName(nZ)) {
							continue;
						}
						newUpperCaseLetter = ObsDUpperCaseLetter;
					}
					final int v = (SDUpperCaseLetter.equals(ALabel.emptyLabel)) ? eSD.getValue(SDLabel)
							: eSD.getUpperLabelValue(SDLabel, SDUpperCaseLetter);
					if (v == Constants.INT_NULL) {
						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
						continue;
					}

					final int max = Math.max(w, v);

					Label newLabel = (nD != nZ) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					// if (eSD.getName().equals("n8_Z") && newLabel.toString().equals("¿b")) {
					// @SuppressWarnings("unused")
					// String ok = "trovato";
					// }

					if (newUpperCaseLetter.equals(ALabel.emptyLabel)) {
						eSD.putLabeledValueToRemovedList(SDLabel, v);
						ruleApplied = eSD.mergeLabeledValue(newLabel, max);
					} else {
						eSD.putUpperLabeledValueToRemovedList(SDLabel, SDUpperCaseLetter, v);
						ruleApplied = eSD.mergeUpperLabelValue(newLabel, newUpperCaseLetter, max);
					}

					if (ruleApplied) {
						if (LOG.isLoggable(Level.FINER)) {
							if (!newUpperCaseLetter.equals(ALabel.emptyLabel)) {
								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
										+ "source: " + nObs.getName() + " ---" + tripleAsString(w, ObsDUpperCaseLetter, ObsDLabel, false) + "---> "
										+ nD.getName()
										+ " <---" + tripleAsString(v, SDUpperCaseLetter, SDLabel, false) + "--- " + nS.getName()
										+ "\nresult: add " + nD.getName() + " <---" + tripleAsString(max, newUpperCaseLetter, newLabel, false) + "--- "
										+ nS.getName());
							} else {
								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "---> " + nD.getName()
										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
										+ "\nresult: add " + nD.getName() + " <---" + pairAsString(newLabel, max) + "--- " + nS.getName());

							}
						}
						this.checkStatus.r3calls++;
					}

					if (newUpperCaseLetter.equals(ALabel.emptyLabel) && CSTN.isNewLabeledValueANegativeLoop(newLabel, w, nS, nD, eSD)) {
						this.checkStatus.consistency = false;
						this.checkStatus.finished = true;
						return ruleApplied;
					}
				}
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R3: end.");
		}
		return ruleApplied;
	}

	/**
	 * Adds 'edgesToAdd' to 'g' in all-max-projection way.<br>
	 * Adds 'edgesToAdd' to 'g' considering, for each edge, only the ordinary labeled value and upper-case labeled values.
	 * Upper-case labeled values are added as ordinary labeled values.<br>
	 *
	 * @return the all-max projection of the graph g (CSTN graph) without edges connecting nodes with non consistent labels.
	 */
	LabeledIntGraph makeAllMaxProjection() {
		LabeledIntGraph allMax = new LabeledIntGraph(this.g.getInternalLabeledValueMapImplementationClass());
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : this.g.getVertices()) {
			vNew = new LabeledNode(v);
			allMax.addVertex(vNew);
		}
		allMax.setZ(allMax.getNode(this.g.getZ().getName()));

		// clone all edges giving the right new endpoints corresponding the old ones.
		// we do not add edges connecting nodes in not consistent scenarios (such edges have only unknown labels).
		AbstractLabeledIntEdge eNew;
		LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = allMax.getEdgeFactory();
		for (final LabeledIntEdge e : this.g.getEdges()) {
			if (!this.g.getSource(e).getLabel().isConsistentWith(this.g.getDest(e).getLabel()))
				continue;
			eNew = edgeFactory.create(e);
			eNew.setConstraintType(ConstraintType.normal);
			eNew.getLowerLabelMap().clear();
			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getUpperLabelSet()) {
				eNew.mergeLabeledValue(entry.getKey().getKey(), entry.getIntValue());
			}
			eNew.getLowerLabelMap().clear();
			eNew.getUpperLabelMap().clear();
			allMax.addEdge(eNew, this.g.getSource(e).getName(), this.g.getDest(e).getName());
		}
		return allMax;
	}

	/**
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initUpperLowerLabelDataStructure()}.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllability(final Collection<LabeledIntEdge> edgesToCheck) throws WellDefinitionException {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;

		final LabeledNode Z = this.g.getZ();

		this.checkStatus.cycles++;

		LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules.");

		ObjectArraySet<LabeledIntEdge> newEdgesToCheck = new ObjectArraySet<>();// HAS TO BE A SET!
		int i = 1, n = edgesToCheck.size();
		for (LabeledIntEdge AB : edgesToCheck) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			// initAndCheck does not resolve completely a qStar.
			// It is necessary to check here the edge before to consider the second edge.
			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!

			edgeCopy = this.g.getEdgeFactory().create(AB);
			if (A.isObservator()) {
				// R0 on the resulting new values
				labelModificationR0(A, B, Z, AB);
			}
			labelModificationR3(A, B, Z, AB);
			if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// R0 on the resulting new values
				labelModificationR0(A, B, Z, AB);
			}

			// LLR is put here because it works like R0 and R3
			if (AB.getUpperLabelMap().size() > 0) {
				labeledLetterRemovalRule(A, B, AB);
			}

			if (!AB.equalsLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(AB);
			}

			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Rules, phase 1/2: edge " + AB.getName() + " as first component.");
			}
			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().create(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationRule(A, B, C, AB, BC, AC);

				/**
				 * The following rule are called if there are condition (avoid to call for nothing)
				 */
				if (AB.getLowerLabelMap().size() > 0) {
					labeledCrossLowerCaseRule(A, B, C, AB, BC, AC);
				}

				if (!this.checkStatus.consistency)
					return ((CSTNUCheckStatus) this.checkStatus);

				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					newEdgesToCheck.add(AC);
				} else if (edgeCopy != null && !edgeCopy.equalsLabeledValues(AC)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.add(AC);
				}
			}

			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Rules, phase 1/2 done.");
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Rules, phase 2/2: edge " + AB.getName() + " as second component.");
			}
			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().create(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				labeledPropagationRule(C, A, B, CA, AB, CB);

				if (CA.getLowerLabelMap().size() > 0) {
					labeledCrossLowerCaseRule(C, A, B, CA, AB, CB);
				}

				if (!this.checkStatus.consistency)
					return ((CSTNUCheckStatus) this.checkStatus);

				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
					newEdgesToCheck.add(CB);
				} else if (edgeCopy != null && !edgeCopy.equalsLabeledValues(CB)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.add(CB);
				}
			}
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Rules phase 2/2 done.\n");
			}
		}
		if (LOG.isLoggable(Level.FINE)) {
			LOG.log(Level.FINE, "End application all rules.");
		}
		edgesToCheck.clear();// in any case, this set has been elaborated. It is better to clear it out.
		this.checkStatus.finished = newEdgesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.addAll(newEdgesToCheck);
		}
		if (!this.checkStatus.consistency)
			this.checkStatus.finished = true;
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * @return the checkStatus
	 */
	public CSTNUCheckStatus getCheckStatus() {
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * @param g the g to set
	 */
	void setG(LabeledIntGraph g) {
		if (g == null)
			throw new IllegalArgumentException("Input graph is null!");
		this.g = g;
		this.checkStatus = new CSTNUCheckStatus();
	}

	// /**
	// * Determines the minimal distance between all pairs of vertexes of the given graph if the graph does not contain any negative cycles. If the graph
	// * contains a negative cycle, the method stops and returns false (the graph is anyway modified).
	// *
	// * @param g
	// * the graph
	// * @return true if the input graph does not contain any negative cycle, false otherwise.
	// */
	// boolean minimalDistanceGraphFast(final LabeledIntGraph g) {
	// final int n = g.getVertexCount();
	// final LabeledNode[] node = g.getVerticesArray();
	// LabeledNode iV, jV, kV;
	// LabeledIntEdge ik, kj, ij;
	// int v;
	// Label l;
	// LabeledIntMap ijMap = null;
	// for (int k = 0; k < n; k++) {
	// kV = node[k];
	// for (int i = 0; i < n; i++) {
	// iV = node[i];
	// for (int j = 0; j < n; j++) {
	// if ((k == i) && (i == j)) {
	// continue;
	// }
	// jV = node[j];
	// Label nodeLabelConjunction = iV.getLabel().conjunction(jV.getLabel());
	// if (nodeLabelConjunction == null)
	// continue;
	//
	// ik = g.findEdge(iV, kV);
	// kj = g.findEdge(kV, jV);
	// if ((ik == null) || (kj == null)) {
	// continue;
	// }
	// ij = g.findEdge(iV, jV);
	//
	// final Set<Object2IntMap.Entry<Label>> ikMap = ik.labeledValueSet();
	// final Set<Object2IntMap.Entry<Label>> kjMap = kj.labeledValueSet();
	// if ((k == i) || (k == j)) {
	// ijMap = labeledIntMapFactory.create(ij.getLabeledValueMap());// this is necessary to avoid concurrent access to the same map by the
	// // iterator.
	// } else {
	// ijMap = null;
	// }
	// for (final Object2IntMap.Entry<Label> ikL : ikMap) {
	// for (final Object2IntMap.Entry<Label> kjL : kjMap) {
	// l = ikL.getKey().conjunction(kjL.getKey());
	// if (l == null) {
	// continue;
	// }
	// l = l.conjunction(nodeLabelConjunction);// It is necessary to propagate with node labels!
	// if (l == null) {
	// continue;
	// }
	// if (ij == null) {
	// ij = CSTN.makeNewEdge(node[i].getName() + "_" + node[j].getName(), LabeledIntEdge.ConstraintType.derived, g);
	// g.addEdge(ij, iV, jV);
	// }
	// v = ikL.getValue() + kjL.getValue();
	// if (ijMap != null) {
	// ijMap.put(l, v);
	// } else {
	// ij.mergeLabeledValue(l, v);
	// }
	// if (i == j) // check negative cycles
	// if (v < 0 || ij.getMinValue() < 0) {
	// LOG.finer("Found a negative cycle on node " + iV.getName() + ": "
	// + ((ijMap != null) ? ijMap : ij) + "\nIn details, ik=" + ik + ", kj="
	// + kj + ", v=" + v + ", ij.getValue(" + l + ")=" + ij.getValue(l));
	// return false;
	// }
	// }
	// }
	// if (ijMap != null) {
	// ij.setLabeledValue(ijMap);
	// }
	// }
	// }
	// }
	// return true;
	// }

}
