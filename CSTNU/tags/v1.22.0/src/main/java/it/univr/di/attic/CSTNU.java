//package it.univr.di.cstnu;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.Collection;
//import java.util.Map.Entry;
//import java.util.Set;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import edu.uci.ics.jung.io.GraphIOException;
//import it.unimi.dsi.fastutil.objects.Object2IntMap;
//import it.unimi.dsi.fastutil.objects.ObjectArraySet;
//import it.unimi.dsi.fastutil.objects.ObjectSet;
//import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
//import it.univr.di.cstnu.graph.GraphMLReader;
//import it.univr.di.cstnu.graph.GraphMLWriter;
//import it.univr.di.cstnu.graph.LabeledIntEdge;
//import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
//import it.univr.di.cstnu.graph.LabeledIntEdgeFactory;
//import it.univr.di.cstnu.graph.LabeledIntGraph;
//import it.univr.di.cstnu.graph.LabeledNode;
//import it.univr.di.cstnu.visualization.StaticLayout;
//import it.univr.di.labeledvalue.ALabel;
//import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
//import it.univr.di.labeledvalue.AbstractLabeledIntMap;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.LabeledIntTreeMap;
//import it.univr.di.labeledvalue.Literal.State;
//
///**
// * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU).
// * It doesn't manage guarded link!
// * 
// * @author Roberto Posenato
// * @version $Id: $Id
// */
//public class CSTNU extends CSTN {
//	/**
//	 * Simple class to represent the status of the checking algorithm during an execution.
//	 *
//	 * @author Roberto Posenato
//	 */
//	public static class CSTNUCheckStatus extends CSTNCheckStatus {
//
//		// controllability = super.consistency!
//
//		/**
//		 * Counters about the # of application of different rules.
//		 */
//		@SuppressWarnings("javadoc")
//		public int upperCaseRuleCalls = 0, lowerCaseRuleCalls = 0, crossCaseRuleCalls = 0, caseLabelRemovalRuleCalls = 0;
//
//		@Override
//		public String toString() {
//			return ("The check is" + (this.finished ? " " : " NOT") + " finished after " + this.cycles + " cycle(s).\n"
//					+ ((this.finished) ? "the controllability check has determined that given network is" + (this.consistency ? " " : " NOT ")
//							+ "dynamic controllable.\n" : "")
//					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
//					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
//					+ "Label Propagation Rule has been applied " + this.labeledValuePropagationcalls + " times.\n"
//					+ "Upper Case Rule has been applied " + this.upperCaseRuleCalls + " times.\n"
//					+ "Lower Case Rule has been applied " + this.lowerCaseRuleCalls + " times.\n"
//					+ "Cross Case Rule has been applied " + this.crossCaseRuleCalls + " times.\n"
//					+ "Case Removal Rule has been applied " + this.caseLabelRemovalRuleCalls + " times.\n"
//					+ "Negative qLoops: " + this.qAllNegLoop + "\n"
//					+ "Negative qLoops with positive edge: " + this.qSemiNegLoop + "\n"
//					+ "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)");
//		}
//
//		public void reset() {
//			super.reset();
//			this.upperCaseRuleCalls = 0;
//			this.lowerCaseRuleCalls = 0;
//			this.crossCaseRuleCalls = 0;
//			this.caseLabelRemovalRuleCalls = 0;
//		}
//	}
//
//	/**
//	 * logger
//	 */
//	@SuppressWarnings("hiding")
//	static Logger LOG = Logger.getLogger(CSTNU.class.getName());
//
//	/**
//	 * Version of the class
//	 */
//	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
//	// static final String VERSIONandDATE = "Version 3.2 - June, 14 2016";
//	@SuppressWarnings("hiding")
//	static final String VERSIONandDATE = "Version  4 - April, 5 2017";
//
//	/**
//	 * Returns the sum of all negative values (ignoring their labels) present in the edges of a graph. If an edge has more than one negative values, only
//	 * the minimum among them is considered. For contingent link, also the lower case value is considered.
//	 *
//	 * @param g
//	 * @return the sum of all negative value (negative value)
//	 */
//	static int getSumOfNegativeEdgeValues(final LabeledIntGraph g) {
//		int sum = 0;
//		if ((g == null) || (g.getEdgeCount() == 0))
//			return sum;
//
//		for (final LabeledIntEdge e : g.getEdges()) {
//			final int min = e.getMinValue();
//			// int minLC = e.getMinLowerLabeledValue();
//			if ((min != Constants.INT_NULL) && (min < 0)) {
//				// if (minLC != null) {
//				// if (min.compareTo(minLC) > 0 ) min = minLC;
//				// }
//				sum += min;
//			}
//		}
//		LOG.finer("The sum of all negative values is " + sum);
//		return sum;
//	}
//
//	/**
//	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
//	 *
//	 * @param args an array of {@link java.lang.String} objects.
//	 * @throws FileNotFoundException
//	 * @throws GraphIOException
//	 */
//	public static void main(final String[] args) throws FileNotFoundException, GraphIOException {
//		LOG.finest("Start...");
//		final CSTNU cstnu = new CSTNU();
//
//		if (!cstnu.manageParameters(args))
//			return;
//		LOG.finest("Parameters ok!");
//		if (cstnu.versionReq) {
//			System.out.println(CSTNU.class.getName() + " " + CSTNU.VERSIONandDATE + ". Academic and non-commercial use only.\n"
//					+ "Copyright © 2017, Roberto Posenato");
//			return;
//		}
//
//		LOG.finest("Loading graph...");
//		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstnu.fInput, LabeledIntTreeMap.class);
//		cstnu.setG(graphMLReader.readGraph());
//
//		LOG.finest("LabeledIntGraph loaded!");
//
//		LOG.finest("DC Checking...");
//		CSTNUCheckStatus status;
//		try {
//			status = cstnu.dynamicControllabilityCheck();
//		} catch (final WellDefinitionException e) {
//			System.out.print("An error has been occured during the checking: " + e.getMessage());
//			return;
//		}
//		LOG.finest("LabeledIntGraph minimized!");
//		if (status.finished) {
//			System.out.println("Checking finished!");
//			if (status.consistency) {
//				System.out.println("The given CSTNU is Dynamic controllable!");
//			} else {
//				System.out.println("The given CSTNU is NOT Dynamic controllable!");
//			}
//			System.out.println("Details: " + status);
//		} else {
//			System.out.println("Checking has not been finished!");
//			System.out.println("Details: " + status);
//		}
//
//		if (cstnu.fOutput != null) {
//			final GraphMLWriter graphWriter = new GraphMLWriter(new StaticLayout<>(cstnu.g));
//			try {
//				graphWriter.save(cstnu.g, new PrintWriter(cstnu.output));
//			} catch (final IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	/**
//	 * @param label
//	 * @param value
//	 * @param nodeName
//	 * @param lower
//	 * @return the conventional representation of a labeled value
//	 */
//	static final String tripleAsString(Label label, int value, ALabel nodeName, boolean lower) {
//		return LabeledContingentIntTreeMap.entryAsString(label, value, nodeName, lower);
//	}
//
//	/**
//	 * Constructor for CSTNU.
//	 */
//	private CSTNU() {
//	}
//
//	/**
//	 * Constructor for CSTNU
//	 * 
//	 * @param g graph to check
//	 */
//	public CSTNU(LabeledIntGraph g) {
//		super(g);
//	}
//
//	/**
//	 * Checks whether the constraint represented by an edge 'e' satisfies the well definition 1 property:<br>
//	 * any labeled valued of the edge is consistent and subsumes both labels of two endpoints.
//	 *
//	 * @param source the source node of the edge.
//	 * @param destination the destination node of the edge.
//	 * @param e edge representing a labeled constraint.
//	 * @param hasToBeFixed true for fixing well-definition errors that can be fixed!
//	 * @return false if the check fails, true otherwise
//	 * @throws WellDefinitionException
//	 */
//	boolean checkWellDefinitionProperty1and3(final LabeledNode source, final LabeledNode destination, final LabeledIntEdge e, boolean hasToBeFixed)
//			throws WellDefinitionException {
//
//		super.checkWellDefinitionProperty1and3(source, destination, e, hasToBeFixed);
//		final Label conjunctedLabel = source.getLabel().conjunction(destination.getLabel());
//
//		// check the upper case labeled values
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getUpperLabelSet()) {
//			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
//				final String msg = "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//		}
//		// check the lower case labeled values
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getLowerLabelSet()) {
//			if (!entry.getKey().getKey().subsumes(conjunctedLabel)) {
//				final String msg = "Lower case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
//				if (LOG.isLoggable(Level.WARNING)) {
//					LOG.log(Level.WARNING, msg);
//				}
//				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Checks whether each labeled value of an edge 'e' satisfies the third well definition property:<br>
//	 * <blockquote>For each literal present in any label of 'e', the label of the observation node of the considered literal is subsumed by the label of the
//	 * edge.</blockquote>
//	 * 
//	 * @param e the current edge to check.
//	 * @return false if the check fails, true otherwise
//	 * @throws WellDefinitionException
//	 */
//	// FIXME
//	boolean checkWellDefinition3Property(final LabeledIntEdge e) throws WellDefinitionException {
//		if (e == null) {
//			if (LOG.isLoggable(Level.WARNING)) {
//				LOG.log(Level.WARNING, "Edge is null. Please, check parameter.");
//			}
//			return false;
//		}
//
//		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> allLabeledValuesSet = e.getAllUpperCaseAndOrdinaryLabeledValuesSet();
//		allLabeledValuesSet.addAll(e.getLowerLabelSet());
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : allLabeledValuesSet) {
//			final Label edgeLabel = entry.getKey().getKey();
//			if (edgeLabel.isEmpty()) {
//				continue;
//			}
//
//			// check the observation node
//			for (final char l : edgeLabel.getPropositions()) {
//				final LabeledNode obs = this.g.getObservator(l);
//				if (obs == null) {
//					final String msg = "Observation node of proposition '" + l + "' present in label '" + edgeLabel + "' of edge " + e
//							+ " does not exist.";
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, msg);
//					}
//					throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
//				}
//
//				final Label obsLabel = obs.getLabel();
//				if (!edgeLabel.subsumes(obsLabel)) {
//					final String msg = "Label " + edgeLabel + " of edge " + e + " does not subsume label of obs node " + obs;
//					if (LOG.isLoggable(Level.WARNING)) {
//						LOG.log(Level.WARNING, msg);
//					}
//					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
//				}
//			}
//		}
//		return true;
//	}
//
//	/**
//	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
//	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
//	 *
//	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
//	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException
//	 *             if the nextGraph is not well defined (does not observe all well definition properties).
//	 */
//	public CSTNUCheckStatus dynamicControllabilityCheck() throws WellDefinitionException {
//
//		try {
//			initUpperLowerLabelDataStructure();
//		} catch (final IllegalArgumentException e) {
//			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
//		}
//
//		Collection<LabeledIntEdge> edgesToCheck = this.g.getEdges();
//
//		final int n = this.g.getVertexCount();
//		int k = this.g.getUpperLabeledEdges().size();
//		if (k == 0) {
//			k = 1;
//		}
//		int p = this.g.getPropositions().size();
//		if (p == 0) {
//			p = 1;
//		}
//		int maxCycles = p * ((n * n) + (n * k) + k);// cstnu
//		if (maxCycles == 0) {
//			maxCycles = 2;
//		}
//		LOG.info("The maximum number of possible cycles is " + maxCycles);
//
//		int i;
//		this.checkStatus.finished = false;
//		Instant startInstant = Instant.now();
//		for (i = 1; i <= maxCycles && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
//			}
//			this.checkStatus = this.oneStepDynamicControllability(edgesToCheck);
//
//			if (this.checkStatus.consistency && !this.checkStatus.finished) {
//				if (LOG.isLoggable(Level.FINER)) {
//					StringBuilder log = new StringBuilder();
//					log.append("During the check n. " + i + ", " + edgesToCheck.size()
//							+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
//					for (LabeledIntEdge e : edgesToCheck) {
//						if (e.size() == 0)
//							continue;
//						log.append("Edge " + e + "\n");
//					}
//					LOG.log(Level.FINER, log.toString());
//				}
//			}
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
//			}
//		}
//		if (this.checkStatus.consistency) {
//			if (LOG.isLoggable(Level.INFO)) {
//				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, all possible propagations have done."
//						+ "\nNow it is necessary to check that AllMax Projection network is consistent.");
//				LOG.info("AllMax Projection check starts...");
//			}
//			LabeledIntGraph allMaxCSTN = makeAllMaxProjection();
//			CSTN cstnChecker = new CSTN(allMaxCSTN);
//			CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck();
//			LOG.info("AllMax Projection network check done.\n");
//			if (!cstnStatus.consistency) {
//				LOG.info("The AllMax Projection network has at least one negative loop. The original network cannot be DC!");
//				this.checkStatus.consistency = false;
//				this.checkStatus.finished = true;
//			}
//		}
//		Instant endInstant = Instant.now();
//		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();
//
//		if (!this.checkStatus.consistency) {
//			if (LOG.isLoggable(Level.INFO)) {
//				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, it has been stated that the network is NOT DC controllable.\nStatus: " + this.checkStatus);
//				LOG.log(Level.FINE, "Final NOT DC controllable network: " + this.g);
//			}
//			return ((CSTNUCheckStatus) this.checkStatus);
//		}
//
//		if (i > maxCycles && !this.checkStatus.finished) {
//			if (LOG.isLoggable(Level.INFO)) {
//				LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
//				LOG.log(Level.FINE, "Last NOT DC controllable network determined: " + this.g);
//			}
//			this.checkStatus.consistency = this.checkStatus.finished;
//			return ((CSTNUCheckStatus) this.checkStatus);
//		}
//
//		// controllable && finished
//		if (LOG.isLoggable(Level.INFO)) {
//			LOG.log(Level.INFO, "Stable state reached. The newtork is DC controllable.\nNumber of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles
//					+ ".\nStatus: " + this.checkStatus);
//		}
//		return ((CSTNUCheckStatus) this.checkStatus);
//	}
//
//	/**
//	 * Call {@link CSTN#initAndCheck()} and, then, check all contingent links.
//	 * 
//	 * @return true if the check is successful. The input g results to be modified by the method.
//	 * @throws WellDefinitionException if the graph is null or it is not well formed.
//	 */
//	public boolean initUpperLowerLabelDataStructure() throws WellDefinitionException {
//
//		if (LOG.isLoggable(Level.FINER)) {
//			LOG.log(Level.FINER, "Starting checking graph as CSTNU well-defined instance...");
//		}
//
//		// check underneath CSTN
//		super.initAndCheck();
//		this.checkStatus.initialized = false;
//
//		// Contingent link have to be checked AFTER WD1 and WD3 have been checked and fixed!
//		for (final LabeledIntEdge e : this.g.getEdges()) {
//			if (!e.isContingentEdge()) {
//				continue;
//			}
//			final LabeledNode s = this.g.getSource(e);
//			final LabeledNode d = this.g.getDest(e);
//			/***
//			 * Manage contingent link.
//			 */
//			final Label conjunctedLabel = s.getLabel().conjunction(d.getLabel());
//			final int initialValue = e.getValue(conjunctedLabel); // we consider only one value, the one with label == conjunctedLabel
//			if (initialValue == Constants.INT_NULL) {
//				if (e.lowerLabelSize() == 0 && e.upperLabelSize() == 0) {
//					throw new IllegalArgumentException(
//							"Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
//				}
//			}
//			if (initialValue == 0)
//				throw new IllegalArgumentException("Contingent edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0<x<y<∞.");
//
//			LabeledIntEdge eInverted = this.g.findEdge(d, s);
//			LOG.finer("Edge " + e + " is contingent. Found its companion: " + eInverted);
//			if (eInverted == null) {
//				throw new IllegalArgumentException("Contingent edge " + e + " is alone. The companion contingent edge between " + d.getName()
//						+ " and " + s.getName() + " does not exist. It must!");
//			}
//			if (!eInverted.isContingentEdge()) {
//				throw new IllegalArgumentException("Edge " + e + " is contingent while the companion edge " + eInverted + " is not contingent!\nIt must be!");
//			}
//			/**
//			 * Memo.
//			 * If current initialValue is negative, current edge is the lower bound C--->A. The lower case labeled value has to be put in the inverted edge.
//			 * If current initialValue is positive, current edge is the upper bound A--->C. The upper case labeled value has to be put in the inverted edge.
//			 * if current initialValue is undefined, then we assume that the contingent link is already set.
//			 */
//			if (initialValue != Constants.INT_NULL) {
//				int eInvertedInitialValue;
//				int lowerCaseValue = Constants.INT_NULL;
//				int upperCaseValue = Constants.INT_NULL;
//				eInvertedInitialValue = eInverted.getValue(conjunctedLabel);
//
//				if (initialValue < 0) {
//					// current edge is the lower bound.
//					ALabel sourceALabel = new ALabel(s.getName(), this.g.getALabelAlphabet());
//					lowerCaseValue = eInverted.getLowerLabelValue(conjunctedLabel, sourceALabel);
//					if (lowerCaseValue != Constants.INT_NULL && -initialValue != lowerCaseValue) {
//						throw new IllegalArgumentException(
//								"Edge " + e + " is contingent with a negative value and the inverted " + eInverted + " already contains a lower case value: "
//										+ tripleAsString(conjunctedLabel, lowerCaseValue, sourceALabel, true) + ".");
//					}
//					if (lowerCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
//						throw new IllegalArgumentException("Edge " + e + " is contingent with a negative value but the inverted " + eInverted
//								+ " does not contain a lower case value neither a proper initial value. ");
//					}
//
//					if (lowerCaseValue == Constants.INT_NULL) {
//						lowerCaseValue = -initialValue;
//						eInverted.mergeLowerLabelValue(conjunctedLabel, sourceALabel, lowerCaseValue);
//						// e.removeLabel(conjunctedLabel); 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
//						LOG.warning("Insert the lower label value: " + tripleAsString(conjunctedLabel, lowerCaseValue, sourceALabel, true) + " to edge "
//								+ eInverted);
//						if (eInvertedInitialValue != Constants.INT_NULL) {
//							upperCaseValue = -eInvertedInitialValue;
//							e.mergeUpperLabelValue(conjunctedLabel, sourceALabel, upperCaseValue);
//							// eInverted.removeLabel(conjunctedLabel);2017-04-10 It is preferable to maintain the value for AllMaxProjection check
//							LOG.warning("Insert the upper label value: " + tripleAsString(conjunctedLabel, upperCaseValue, sourceALabel, false) + " to edge "
//									+ e);
//						}
//					}
//				} else {
//					// e : A--->C
//					// eInverted : C--->A
//					ALabel destALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
//					upperCaseValue = eInverted.getUpperLabelValue(conjunctedLabel, destALabel);
//					if (upperCaseValue != Constants.INT_NULL && -initialValue != upperCaseValue) {
//						throw new IllegalArgumentException(
//								"Edge " + e + " is contingent with a positive value and the inverted " + eInverted + " already contains a upper case value: "
//										+ tripleAsString(conjunctedLabel, upperCaseValue, destALabel, true) + ".");
//					}
//					if (upperCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
//						throw new IllegalArgumentException("Edge " + e + " is contingent with a positive value but the inverted " + eInverted
//								+ " does not contain a upper case value neither a proper initial value. ");
//					}
//					if (upperCaseValue == Constants.INT_NULL) {
//						upperCaseValue = -initialValue;
//						eInverted.mergeUpperLabelValue(conjunctedLabel, destALabel, upperCaseValue);
//						LOG.warning("Insert the upper label value: " + tripleAsString(conjunctedLabel, upperCaseValue, destALabel, false) + " to edge "
//								+ eInverted);
//						// e.removeLabel(conjunctedLabel); 2017-04-10 It is preferable to maintain the value for AllMaxProjection check
//						if (eInvertedInitialValue != Constants.INT_NULL) {
//							lowerCaseValue = -eInvertedInitialValue;
//							e.mergeLowerLabelValue(conjunctedLabel, destALabel, lowerCaseValue);
//							// eInverted.removeLabel(conjunctedLabel);2017-04-10 It is preferable to maintain the value for AllMaxProjection check
//							LOG.warning("Insert the lower label value: " + tripleAsString(conjunctedLabel, lowerCaseValue, destALabel, true) + " to edge "
//									+ e);
//						}
//					}
//
//				}
//			}
//		} // end contingent edges cycle
//
//		// init CSTNU structures.
//		// CSTN structures have already been initialized.
//		this.g.getLowerLabeledEdges();
//		this.checkStatus.initialized = true;
//
//		return true;
//	}
//
//	/**
//	 * Apply Morris cross case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *      l1, B:x        l2, c:y                          
//	 * A  &lt;-----------C &lt;----------D and B!=C and (x<0 or (x=0 and A!=C))
//	 * adds 
//	 *      l1l2, B:x+y
//	 * A &lt;-------------D
//	 * </pre>
//	 * 
//	 * @param nD
//	 * @param nC
//	 * @param nA
//	 * @param eDC CANNOT BE NULL
//	 * @param eCA CANNOT BE NULL
//	 * @param eDA CANNOT BE NULL
//	 * @return true if a reduction is applied at least
//	 */
//	boolean labeledCrossCaseRule(final LabeledNode nD, final LabeledNode nC, final LabeledNode nA, final LabeledIntEdge eDC, final LabeledIntEdge eCA,
//			final LabeledIntEdge eDA) {
//
//		/*
//		 * I use the same node/edge names of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> DCMap = eDC.getLowerLabelSet();
//		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> CAMap = eCA.getUpperLabelSet();
//		if (DCMap.isEmpty() || CAMap.isEmpty() || nA.equalsByName(nC))
//			return false;
//
//		LOG.finer("Cross Case Rule: start.");
//
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryLabelUpperCaseEdge : CAMap) {
//			final ALabel upperCaseNodeName = entryLabelUpperCaseEdge.getKey().getValue();
//			//Rule condition: upper case label cannot be equal or contain nC name
//			if (upperCaseNodeName.contains(new ALetter(nC.getName())))
//				continue;
//
//			final int x = entryLabelUpperCaseEdge.getValue();
//			if (x > 0 || (x == 0 && nA == nC))
//				continue; // Rule condition!
//			final Label l1 = entryLabelUpperCaseEdge.getKey().getKey();
//
//			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryLowerCaseEdgeToC : DCMap) {
//				final int y = entryLowerCaseEdgeToC.getValue();
//				final Label l2 = entryLowerCaseEdgeToC.getKey().getKey();
//				final Label l1l2 = l1.conjunction(l2);
//				if (l1l2 == null)
//					continue;
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//				// if (l1l2.containsUnknown() && z >= 0) {
//				// continue;
//				// }
//				final int oldZ = eDA.getUpperLabelValue(l1l2, upperCaseNodeName);
//				final String oldDA = eDA.toString();
//
//				if (eDA.mergeUpperLabelValue(l1l2, upperCaseNodeName, z)) {
//					reductionApplied = true;
//					((CSTNUCheckStatus) this.checkStatus).crossCaseRuleCalls++;
//					LOG.finer("Cross Case applied to edge " + oldDA + ":\npartic: " + nA.getName() + " <---" + tripleAsString(l1, x, upperCaseNodeName, false)
//							+ "--- " + nC.getName() + " <---" + tripleAsString(l2, y, entryLowerCaseEdgeToC.getKey().getValue(), true) + "--- " + nD.getName()
//							+ "\nresult: " + nA.getName()
//							+ " <---" + tripleAsString(l1l2, z, upperCaseNodeName, false) + "--- " + nD.getName() + "; oldValue: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		LOG.finer("Cross Case Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * Apply Morris Label Removal Reduction (see page 1196 of the article MM2005).
//	 *
//	 * <pre>
//	 *       l_1, b, x      l_2, B, z                        l_2, z
//	 * B &lt;-------------A &lt;------------ C and z&ge;-x adds A &lt;-----------C
//	 * </pre>
//	 * 
//	 * @param nC
//	 * @param nA
//	 * @param eCA
//	 * @return true if a reduction is applied at least
//	 */
//	boolean labeledLetterRemovalRule(final LabeledNode nC, final LabeledNode nA, final LabeledIntEdge eCA) {
//		/*
//		 * I use the same node/edge names of the Morris paper: A, B, C for nodes, and CA, AB for edges.
//		 */
//		boolean reductionApplied = false;
//		LabeledNode nB;
//		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> ABMap;
//		LOG.finer("Label Removal Rule: start.");
//
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> upperCaseEntryOfCA : eCA.getUpperLabelSet()) {
//			final ALabel nodeName = upperCaseEntryOfCA.getKey().getValue();
//			nB = this.g.getNode(nodeName.toString());
//			LabeledIntEdge AB = this.g.findEdge(nA, nB);
//			if (AB == null || (ABMap = AB.getLowerLabelSet()).size() == 0)
//				continue;
//			final Label l2 = upperCaseEntryOfCA.getKey().getKey();
//			final int z = upperCaseEntryOfCA.getValue();
//			for (final Object2IntMap.Entry<Entry<Label, ALabel>> lowerCaseEntryOfAB : ABMap) {
//				final Label l1 = lowerCaseEntryOfAB.getKey().getKey();
//
//				int x = lowerCaseEntryOfAB.getValue();
//				if (x == Constants.INT_NULL || z < -x)
//					continue;
//				if (!l1.isConsistentWith(l2)) {
//					if (LOG.isLoggable(Level.FINEST)) {
//						LOG.log(Level.FINEST,
//								"Case Label Removal not applied to edge " + eCA + ": label l1 '" + l1 + "' is not consistent with l2 '" + l2 + "'");
//					}
//					continue;
//				}
//				final int oldZ = eCA.getValue(l2);
//				final String oldCA = eCA.toString();
//
//				if (eCA.mergeLabeledValue(l2, z)) {
//					reductionApplied = true;
//					((CSTNUCheckStatus) this.checkStatus).caseLabelRemovalRuleCalls++;
//					LOG.finer("Case Label Removal applied to edge " + oldCA + ":\n" + "partic: "
//							+ nodeName + " <---" + tripleAsString(l1, x, lowerCaseEntryOfAB.getKey().getValue(), true) + "--- " + nA.getName()
//							+ " <---" + tripleAsString(l2, z, nodeName, false) + "--- " + nC.getName()
//							+ "\nresult: " + nA.getName() + " <---" + pairAsString(l2, z) + "--- " + nC.getName()
//							+ "; oldValue: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		// I have not a clear idea if the above is more efficient of the below code!
//		// for (final LabeledIntEdge AB : lowerCaseEdge) {
//		// ABMap = AB.getLowerLabelSet();
//		// if (ABMap.size() == 0)
//		// continue;
//		// nB = this.g.getDest(AB);
//		//
//		// for (final Object2IntMap.Entry<Entry<Label, String>> upperCaseEntryOfCA : eCA.getUpperLabelSet()) {
//		// final String upperCaseNodeName = upperCaseEntryOfCA.getKey().getValue();
//		// if (!upperCaseNodeName.equals(nB.getName()))
//		// continue;// Rule condition!
//		// final Label l2 = upperCaseEntryOfCA.getKey().getKey();
//		// final int z = upperCaseEntryOfCA.getValue();
//		//
//		// for (final Object2IntMap.Entry<Entry<Label, String>> lowerCaseEntryOfAB : ABMap) {
//		// final Label l1 = lowerCaseEntryOfAB.getKey().getKey();
//		//
//		// int x = lowerCaseEntryOfAB.getValue();
//		// if (x == Constants.INT_NULL || z < -x)
//		// continue;
//		// if (!l1.isConsistentWith(l2)) {
//		// if (LOG.isLoggable(Level.FINEST)) {
//		// LOG.log(Level.FINEST,
//		// "Case Label Removal not applied to edge " + eCA + ": label l1 '" + l1 + "' is not consistent with l2 '" + l2 + "'");
//		// }
//		// continue;
//		// }
//		// final int oldZ = eCA.getValue(l2);
//		// final String oldCA = eCA.toString();
//		//
//		// if (eCA.mergeLabeledValue(l2, z)) {
//		// reductionApplied = true;
//		// ((CSTNUCheckStatus) this.checkStatus).caseLabelRemovalRuleCalls++;
//		// LOG.finer("Case Label Removal applied to edge " + oldCA + ":\n" + "partic: "
//		// + nB.getName() + " <---" + tripleAsString(l1, x, lowerCaseEntryOfAB.getKey().getValue(), true) + "--- " + nA.getName()
//		// + " <---" + tripleAsString(l2, z, upperCaseNodeName, false) + "--- " + nC.getName()
//		// + "\nresult: " + nA.getName() + " <---" + pairAsString(l2, z) + "--- " + nC.getName()
//		// + "; oldValue: " + Constants.formatInt(oldZ));
//		// }
//		// }
//		// }
//		// }
//		LOG.finer("Label Removal Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * Apply Morris lower case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *     l1, x     l2, c:y                    l1l2, x+y
//	 * A  &lt;-------C &lt;--------D and x&le;0 adds A &lt;-----------D
//	 * </pre>
//	 * 
//	 * @param nD
//	 * @param nC
//	 * @param nA
//	 * @param nZ
//	 * @param eDC CANNOT BE NULL
//	 * @param eCA CANNOT BE NULL
//	 * @param eDA CANNOT BE NULL
//	 * @return true if a reduction is applied at least
//	 */
//	boolean labeledLowerCaseRule(final LabeledNode nD, final LabeledNode nC, final LabeledNode nA, final LabeledNode nZ, final LabeledIntEdge eDC,
//			final LabeledIntEdge eCA, final LabeledIntEdge eDA) {
//
//		/*
//		 * I use the same node/edge names of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> DCMap = eDC.getLowerLabelSet();
//		if (DCMap.size() == 0)
//			return false;
//		if (eCA.getMinValue() == Constants.INT_NULL || nA.equalsByName(nC) || nA.equalsByName(nD))
//			return false;
//
//		LOG.finer("Lower Case Rule: start.");
//
//		final Set<Object2IntMap.Entry<Label>> CAMap = eCA.labeledValueSet();
//
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryDC : DCMap) {
//			final Label l2 = entryDC.getKey().getKey();
//			final int y = entryDC.getValue();
//			for (final Object2IntMap.Entry<Label> entryCA : CAMap) {
//				final int x = entryCA.getValue();
//				if (x > 0)// rule condition
//					continue;
//				final Label l1 = entryCA.getKey();
//				final Label l1l2 = (nA == nZ && x < 0) ? l1.conjunctionExtended(l2) : l1.conjunction(l2);
//				if (l1l2 == null) {
//					continue;
//				}
//				final int oldZ = eDA.getValue(l1l2);
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(y, x);
//				final String oldDA = eDA.toString();
//
//				if (eDA.mergeLabeledValue(l1l2, z)) {
//					reductionApplied = true;
//					((CSTNUCheckStatus) this.checkStatus).lowerCaseRuleCalls++;
//					LOG.finer("Lower Case applied to edge " + oldDA + ":\n"
//							+ "partic: " + nA.getName() + " <---" + pairAsString(l1, x) + "--- " + nC.getName()
//							+ " <---" + tripleAsString(l2, y, entryDC.getKey().getValue(), true) + "--- " + nD.getName()
//							+ "\nresult: " + nA.getName() + " <---" + pairAsString(l1l2, z) + "--- " + nD.getName()
//							+ "; old value: " + Constants.formatInt(oldZ));
//				}
//			}
//		}
//		LOG.finer("Lower Case Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * Apply Morris upper case reduction (see page 1196 of the article).
//	 *
//	 * <pre>
//	 *     l1, B:x     l2, y             l1l2, B:x+y
//	 * A &lt;----------C &lt;--------D adds A &lt;-------------D
//	 * </pre>
//	 * 
//	 * @param nD
//	 * @param nC
//	 * @param nA
//	 * @param eDC CANNOT BE NULL
//	 * @param eCA CANNOT BE NULL
//	 * @param eDA CANNOT BE NULL
//	 * @return true if a reduction is applied at least
//	 */
//	boolean labeledUpperCaseRule(final LabeledNode nD, final LabeledNode nC, final LabeledNode nA, final LabeledIntEdge eDC, final LabeledIntEdge eCA,
//			final LabeledIntEdge eDA) {
//		/*
//		 * I use the same node/edge ids of the Morris paper: A, C, D for nodes, and CA, DC, and DA for edges.
//		 */
//		boolean reductionApplied = false;
//		Set<Object2IntMap.Entry<Entry<Label, ALabel>>> CAMap = eCA.getUpperLabelSet();
//		if (CAMap.size() == 0)
//			return false;
//		// if (C.equalsByName(D) || A.equalsByName(D)) continue;// it is useless to consider self loop. NO! it is necessary with guarded links!
//		LOG.finest("Upper Case Rule: start.");
//
//		final Set<Object2IntMap.Entry<Label>> DCMap = eDC.labeledValueSet();
//
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryCA : CAMap) {
//			final Label l1 = entryCA.getKey().getKey();
//			final ALabel upperCaseNodeCA = entryCA.getKey().getValue();
//			final int x = entryCA.getValue();
//
//			for (final Object2IntMap.Entry<Label> entryDC : DCMap) {
//				final Label l2 = entryDC.getKey();
//				final int y = entryDC.getValue();
//				final Label l1l2 = (y < 0) ? l1.conjunctionExtended(l2) : l1.conjunction(l2);
//				if (l1l2 == null)
//					continue;
//				final int oldZ = eDA.getUpperLabelValue(l1l2, upperCaseNodeCA);
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//				final String oldDA = eDA.toString();
//				// if ((oldZ != null) && (oldZ <= z)) continue;
//
//				if (z == 0 && nA == nD)
//					continue;// FIXME
//				if (eDA.mergeUpperLabelValue(l1l2, upperCaseNodeCA, z)) {
//					reductionApplied = true;
//					((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
//					LOG.finer("Upper Case applied to edge " + oldDA + ":\n" + "partic: "
//							+ nA.getName() + " <---" + tripleAsString(l1, x, upperCaseNodeCA, false) + "--- " + nC.getName() + " <---" + pairAsString(l2, y)
//							+ "--- " + nD.getName()
//							+ "\nresult: "
//							+ nA.getName() + " <---" + tripleAsString(l1l2, z, upperCaseNodeCA, false) + "--- " + nD.getName()
//							+ "; old value: " + Constants.formatInt(oldZ));
//				}
//			}
//
//			// FIXME
//			// Manca la propagazione in avanti di UPPER CASE value che sono negativi!
//			// Prova con gli upper case value
//			for (final it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Entry<Label, ALabel>> entryDC : eDC.getUpperLabelMap().labeledTripleSet()) {
//				final Label l2 = entryDC.getKey().getKey();
//				ALabel upperCaseNodeDC = entryDC.getKey().getValue();
//				ALabel conjunctUpperCase = upperCaseNodeCA.conjunction(upperCaseNodeDC);
//				final int y = entryDC.getIntValue();
//				final Label l1l2 = (y < 0) ? l1.conjunctionExtended(l2) : l1.conjunction(l2);
//				if (l1l2 == null)
//					continue;
//				final int oldZ = eDA.getUpperLabelValue(l1l2, conjunctUpperCase);
//				final int z = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);
//				final String oldDA = eDA.toString();
//				// if ((oldZ != null) && (oldZ <= z)) continue;
//				if (z == 0 && nA == nD)
//					continue;// FIXME
//
//				if (eDA.mergeUpperLabelValue(l1l2, conjunctUpperCase, z)) {
//					reductionApplied = true;
//					((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
//					LOG.finer("Upper Case* applied to edge " + oldDA + ":\n" + "partic: "
//							+ nA.getName() + " <---" + tripleAsString(l1, x, upperCaseNodeCA, false) + "--- " + upperCaseNodeCA + " <---"
//							+ tripleAsString(l2, y, upperCaseNodeCA, false)
//							+ "--- " + nD.getName()
//							+ "\nresult: "
//							+ nA.getName() + " <---" + tripleAsString(l1l2, z, conjunctUpperCase, false) + "--- "
//							+ nD.getName()
//							+ "; old value: " + Constants.formatInt(oldZ));
//				}
//			}
//
//		}
//		LOG.finest("Upper Case Rule: end.");
//		return reductionApplied;
//	}
//
//	/**
//	 * ( Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
//	 * <b>Standard DC semantics is assumed.</b>
//	 * 
//	 * <pre>
//	 * R0:
//	 * P? --[α, C p, w]--&gt; X 
//	 * changes in 
//	 * P? --[α', C, w]--&gt; X when w &le; 0
//	 * where:
//	 * p is the positive or the negative literal associated to proposition observed in P?,
//	 * α is a label,
//	 * C can be ◇ or an upper letter.
//	 * α' is α without 'p', P? children, and any children of possible q-literals.
//	 * </pre>
//	 * 
//	 * Rule qR0 has X==Z.
//	 * 
//	 * @param nObs the observation node
//	 * @param nX the other node
//	 * @param nZ
//	 * @param ePX the edge connecting P? ---&gt; X
//	 * @return true if the rule has been applied one time at least.
//	 */
//	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge ePX) {
//		// Visibility is package because there is Junit Class test that checks this method.
//
//		boolean ruleApplied = false, mergeStatus;
//		final char p = nObs.getPropositionObserved();
//		if (p == Constants.UNKNOWN) {
//			if (LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
//			}
//			return false;
//		}
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R0: start.");
//		}
//		if (nX.getLabel().contains(p)) {// Table 2 ICAPS
//			// if (LOG.isLoggable(Level.FINER)) {
//			// LOG.log(Level.FINER, "R0: Proposition '" + p + "' is present in the X label '" + X.getLabel() + "'. R0 cannot be applied.");
//			// }
//			return false;
//		}
//
//		final ObjectSet<Label> obsXLabelSet = ePX.getLabeledValueMap().keySet();
//
//		for (final Label l : obsXLabelSet) {
//			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
//				continue;
//			}
//
//			final int w = ePX.getValue(l);
//			if (w == Constants.INT_NULL) {
//				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//				continue;
//			}
//
//			if (w > 0) {// Table 1 ICAPS paper.
//				continue;
//			}
//
//			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
//			if (alphaPrime == null) {
//				continue;
//			}
//			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
//			String logMessage = null;
//			if (LOG.isLoggable(Level.FINER)) {
//				logMessage = "R0 simplifies a label of edge " + ePX.getName()
//						+ ":\nsource: " + nObs.getName() + " ---" + pairAsString(l, w) + "---> " + nX.getName()
//						+ "\nresult: " + nObs.getName() + " ---" + pairAsString(alphaPrime, w) + "---> " + nX.getName();
//			}
//
//			ePX.putLabeledValueToRemovedList(l, w);
//			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
//			this.checkStatus.r0calls++;
//			ruleApplied = true;
//			mergeStatus = ePX.mergeLabeledValue(alphaPrime, w);
//			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, logMessage);
//			}
//			if (CSTN.isNewLabeledValueANegativeLoop(alphaPrime, w, nObs, nX, ePX)) {
//				this.checkStatus.consistency = false;
//				this.checkStatus.finished = true;
//				return ruleApplied;
//			}
//		}
//
//		// upper-case labels
//		final Set<Object2IntMap.Entry<Entry<Label, ALabel>>> edgeUpperLabeledValueSet = new ObjectArraySet<>(ePX.getUpperLabelSet());
//		for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryObs : edgeUpperLabeledValueSet) {
//			final Label l = entryObs.getKey().getKey();
//			if (l == null || l.getStateLiteralWithSameName(p) == State.absent) {// l can be nullified in a previous cycle.
//				continue;
//			}
//
//			final ALabel nodeName = entryObs.getKey().getValue();
//			final int w = ePX.getUpperLabelValue(l, nodeName);
//			if (w == Constants.INT_NULL) {
//				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//				continue;
//			}
//
//			if (w > 0) {// Table 1 ICAPS paper.
//				continue;
//			}
//
//			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
//			if (alphaPrime == null) {
//				continue;
//			}
//
//			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
//			String logMessage = null;
//			if (LOG.isLoggable(Level.FINER)) {
//				logMessage = "R0 simplifies a label of edge " + ePX.getName()
//						+ ":\nsource: " + nObs.getName() + " ---" + tripleAsString(l, w, nodeName, false) + "---> " + nX.getName()
//						+ "\nresult: " + nObs.getName() + " ---" + tripleAsString(alphaPrime, w, nodeName, false) + "---> " + nX.getName();
//			}
//
//			ePX.putUpperLabeledValueToRemovedList(l, nodeName, w);
//			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
//			this.checkStatus.r0calls++;
//			ruleApplied = true;
//			mergeStatus = ePX.mergeUpperLabelValue(alphaPrime, nodeName, w);
//			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, logMessage);
//			}
//		}
//
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R0: end.");
//		}
//
//		return ruleApplied;
//	}
//
//	/**
//	 * <h1>Rule R3*</h1>
//	 * <b>Standard DC semantics is assumed.</b><br>
//	 * <b>This method is also valid assuming Instantaneous Reaction semantics.</b>
//	 *
//	 * <pre>
//	 * if P? --[w, U, αβ]--&gt; nD &lt;--[v, U', βγp]-- nS  and w &le; 0
//	 * then the constraint between Y and X is modified adding the following label:
//	 * nD &lt;--[max{w,v}, U'', αβγ']-- nS
//	 * where:
//	 * α, β and γ do not share any literals.
//	 * α, β do not contain any literal of p.
//	 * p cannot compare also in label of nodes nD and nS.
//	 * γ' is obtained by removing children of p from γ.
//	 * U=◇, U' can be upper-case letter or ◇ when nD!= Z. They can be any but not both upper-case letter when nD = Z.
//	 * U'' is ◇ if U = U' = ◇, U if U' = ◇, U' if  U = ◇
//	 * </pre>
//	 *
//	 * <h2>Rule qR3*</h2>
//	 * 
//	 * <pre>
//	 * if P? --[w, U, γ]--&gt; Z &lt;--[v, U', βθp']-- nS  and w &le; 0
//	 * then the constraint between Y and X is modified adding the following label:
//	 * Z &lt;--[max{w,v}, U'', (γ★β)†]-- nS
//	 * where:
//	 * β, θ and γ are in Q*.
//	 * p' is p or ¬p or ¿p
//	 * γ does not contain p' and any of its children.
//	 * β does not contain any children of p'.
//	 * θ contains only children of p'.
//	 * p cannot compare also in label of nodes nD and nS.
//	 * γ' is obtained by removing children of p from γ.
//	 * (γ★β)† is the extended conjunction without any children of unknown literals.
//	 * U=◇, U' can be upper-case letter or ◇ when nD!= Z. They can be any but not both upper-case letter when nD = Z.
//	 * U'' is ◇ if U = U' = ◇, U if U' = ◇, U' if  U = ◇
//	 * </pre>
//	 * 
//	 * @param nS node
//	 * @param nD node
//	 * @param nZ
//	 * @param eSD LabeledIntEdge containing the constrain to modify
//	 * @return true if a rule has been applied.
//	 */
//	// Visibility is package because there is Junit Class test that checks this method.
//	boolean labelModificationR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSD) {
//
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R3: start.");
//		}
//		boolean ruleApplied = false;
//
//		ObjectArraySet<LabeledIntEdge> Obs2nDEdges = getEdgeFromObservatorsToNode(nD);
//		if (Obs2nDEdges.isEmpty())
//			return false;
//
//		final ObjectSet<Object2IntMap.Entry<Entry<Label, ALabel>>> SDLabeledValueSet = eSD.getAllUpperCaseAndOrdinaryLabeledValuesSet();
//
//		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
//			final LabeledNode nObs = this.g.getSource(eObsD);
//
//			if (nObs.equalsByName(nS))
//				continue;
//			final char p = nObs.getPropositionObserved();
//
//			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// Table 2 ICAPS
//				// if (LOG.isLoggable(Level.FINEST)) {
//				// LOG.log(Level.FINEST, "R3: Proposition '" + p + "' is present in the nS label '" + nS.getLabel() + "' or nD label '" + nD.getLabel()
//				// + "'. R3 cannot be applied.");
//				// }
//				continue;
//			}
//			// all labels from Obs
//			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entryObsD : eObsD.getAllUpperCaseAndOrdinaryLabeledValuesSet()) {
//				final int w = entryObsD.getIntValue();
//				if (w > 0 || (w == 0 && nD == nZ)) { // Table 1 ICAPS
//					// (w == 0 && nD==Z), it means that P? is executed at 0. So, even if v==0 (it cannot be v>0),
//					// the constraint does not imply an implicit constraint (stripping p). So, we don't touch the constraint.
//					continue;
//				}
//
//				final Label ObsDLabel = entryObsD.getKey().getKey();
//				final ALabel ObsDUpperCaseLetter = entryObsD.getKey().getValue();
//
//				Label SDLabel;
//				ALabel newUpperCaseLetter;
//				for (final Object2IntMap.Entry<Entry<Label, ALabel>> SDLabelEntry : SDLabeledValueSet) {
//					if (SDLabelEntry == null || !(SDLabel = SDLabelEntry.getKey().getKey()).contains(p)) {
//						continue;
//					}
//
//					ALabel SDUpperCaseLetter = SDLabelEntry.getKey().getValue();
//
//					if (ObsDUpperCaseLetter.equals(ALabel.emptyLabel)) {
//						// ObsDUpperCaseLetter == ◇
//						newUpperCaseLetter = SDUpperCaseLetter;
//					} else {
//						if (!SDUpperCaseLetter.equals(ALabel.emptyLabel)) {
//							continue;
//						}
//						if (!nD.equalsByName(nZ)) {
//							continue;
//						}
//						newUpperCaseLetter = ObsDUpperCaseLetter;
//					}
//					final int v = (SDUpperCaseLetter.equals(ALabel.emptyLabel)) ? eSD.getValue(SDLabel)
//							: eSD.getUpperLabelValue(SDLabel, SDUpperCaseLetter);
//					if (v == Constants.INT_NULL) {
//						// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
//						continue;
//					}
//
//					final int max = Math.max(w, v);
//
//					Label newLabel = (nD != nZ) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel)
//							: makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsDLabel, SDLabel);
//					if (newLabel == null) {
//						continue;
//					}
//
//					if (newUpperCaseLetter.equals(ALabel.emptyLabel)) {
//						eSD.putLabeledValueToRemovedList(SDLabel, v);
//						ruleApplied = eSD.mergeLabeledValue(newLabel, max);
//					} else {
//						eSD.putUpperLabeledValueToRemovedList(SDLabel, SDUpperCaseLetter, v);
//						ruleApplied = eSD.mergeUpperLabelValue(newLabel, newUpperCaseLetter, max);
//					}
//
//					if (ruleApplied) {
//						if (LOG.isLoggable(Level.FINER)) {
//							if (!newUpperCaseLetter.equals(ALabel.emptyLabel)) {
//								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
//										+ "source: " + nObs.getName() + " ---" + tripleAsString(ObsDLabel, w, ObsDUpperCaseLetter, false) + "---> "
//										+ nD.getName()
//										+ " <---" + tripleAsString(SDLabel, v, SDUpperCaseLetter, false) + "--- " + nS.getName()
//										+ "\nresult: add " + nD.getName() + " <---" + tripleAsString(newLabel, max, newUpperCaseLetter, false) + "--- "
//										+ nS.getName());
//							} else {
//								LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
//										+ "source: " + nObs.getName() + " ---" + pairAsString(ObsDLabel, w) + "---> " + nD.getName()
//										+ " <---" + pairAsString(SDLabel, v) + "--- " + nS.getName()
//										+ "\nresult: add " + nD.getName() + " <---" + pairAsString(newLabel, max) + "--- " + nS.getName());
//
//							}
//						}
//						this.checkStatus.r3calls++;
//					}
//
//					if (newUpperCaseLetter.equals(ALabel.emptyLabel) && CSTN.isNewLabeledValueANegativeLoop(newLabel, w, nS, nD, eSD)) {
//						this.checkStatus.consistency = false;
//						this.checkStatus.finished = true;
//						return ruleApplied;
//					}
//				}
//			}
//		}
//		if (LOG.isLoggable(Level.FINEST)) {
//			LOG.log(Level.FINEST, "Label Modification R3: end.");
//		}
//		return ruleApplied;
//	}
//
//	/**
//	 * Adds 'edgesToAdd' to 'g' in all-max-projection way.<br>
//	 * Adds 'edgesToAdd' to 'g' considering, for each edge, only the ordinary labeled value and upper-case labeled values.
//	 * Upper-case labeled values are added as ordinary labeled values.<br>
//	 *
//	 * @return the all-max projection of the graph g (CSTN graph) without edges connecting nodes with non consistent labels.
//	 */
//	LabeledIntGraph makeAllMaxProjection() {
//		LabeledIntGraph allMax = new LabeledIntGraph(this.g.getInternalLabeledValueMapImplementationClass());
//		// clone all nodes
//		LabeledNode vNew;
//		for (final LabeledNode v : this.g.getVertices()) {
//			vNew = new LabeledNode(v);
//			allMax.addVertex(vNew);
//		}
//		allMax.setZ(allMax.getNode(this.g.getZ().getName()));
//
//		// clone all edges giving the right new endpoints corresponding the old ones.
//		// we do not add edges connecting nodes in not consistent scenarios (such edges have only unknown labels).
//		AbstractLabeledIntEdge eNew;
//		LabeledIntEdgeFactory<? extends LabeledIntMap> edgeFactory = allMax.getEdgeFactory();
//		for (final LabeledIntEdge e : this.g.getEdges()) {
//			if (!this.g.getSource(e).getLabel().isConsistentWith(this.g.getDest(e).getLabel()))
//				continue;
//			eNew = edgeFactory.create(e);
//			eNew.setConstraintType(ConstraintType.normal);
//			eNew.getLowerLabelMap().clear();
//			for (final Object2IntMap.Entry<Entry<Label, ALabel>> entry : e.getUpperLabelSet()) {
//				eNew.mergeLabeledValue(entry.getKey().getKey(), entry.getValue());
//			}
//			eNew.getLowerLabelMap().clear();
//			eNew.getUpperLabelMap().clear();
//			allMax.addEdge(eNew, this.g.getSource(e).getName(), this.g.getDest(e).getName());
//		}
//		return allMax;
//	}
//
//	/**
//	 * Executes one step of the dynamic controllability check.<br>
//	 * Before the first execution of this method, it is necessary to execute {@link #initUpperLowerLabelDataStructure()}.
//	 * 
//	 * @param edgesToCheck set of edges that have to be checked.
//	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
//	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
//	 *             there is a problem in the rules coding.
//	 */
//	public CSTNUCheckStatus oneStepDynamicControllability(final Collection<LabeledIntEdge> edgesToCheck) throws WellDefinitionException {
//
//		LabeledNode A, B, C;
//		LabeledIntEdge AC, CB, edgeCopy;
//
//		final LabeledNode Z = this.g.getZ();
//
//		this.checkStatus.cycles++;
//
//		LOG.log(Level.FINER, "\nStart application labeled constraint generation and label removal rules.");
//
//		ObjectArraySet<LabeledIntEdge> newEdgesToCheck = new ObjectArraySet<>();// HAS TO BE A SET!
//		int i = 1, n = edgesToCheck.size();
//		for (LabeledIntEdge AB : edgesToCheck) {
//			if (LOG.isLoggable(Level.FINER)) {
//				LOG.log(Level.FINER, "Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
//			}
//			A = this.g.getSource(AB);
//			B = this.g.getDest(AB);
//			// initAndCheck does not resolve completely a qStar.
//			// It is necessary to check here the edge before to consider the second edge.
//			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
//			edgeCopy = this.g.getEdgeFactory().create(AB);
//			if (A.isObservator()) {
//				// R0 on the resulting new values
//				labelModificationR0(A, B, Z, AB);
//			}
//			labelModificationR3(A, B, Z, AB);
//			if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
//				// R0 on the resulting new values
//				labelModificationR0(A, B, Z, AB);
//			}
//
//			// LLR is put here because it works like R0 and R3
//			if (AB.getUpperLabelMap().size() > 0) {
//				labeledLetterRemovalRule(A, B, AB);
//			}
//
//			if (!AB.equalsLabeledValues(edgeCopy)) {
//				newEdgesToCheck.add(AB);
//			}
//
//			/**
//			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
//			 * A-->B-->C
//			 */
//			boolean llm, ulm;
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "Rules, phase 1/2: edge " + AB.getName() + " as first component.");
//			}
//			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
//				C = this.g.getDest(BC);
//				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞
//
//				AC = this.g.findEdge(A, C);
//				// I need to preserve the old edge to compare below
//				if (AC != null) {
//					edgeCopy = this.g.getEdgeFactory().create(AC);
//				} else {
//					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
//					edgeCopy = null;
//				}
//
//				this.labeledPropagationRule(A, B, C, AB, BC, AC, Z);
//
//				/**
//				 * The following rule are called if there are condition (avoid to call for nothing)
//				 */
//				llm = AB.getLowerLabelMap().size() > 0;
//				if (llm) {
//					labeledLowerCaseRule(A, B, C, Z, AB, BC, AC);
//				}
//				ulm = BC.getUpperLabelMap().size() > 0;
//				if (ulm) {
//					labeledUpperCaseRule(A, B, C, AB, BC, AC);
//				}
//				if (llm && ulm) {
//					labeledCrossCaseRule(A, B, C, AB, BC, AC);
//				}
//
//				if (!this.checkStatus.consistency)
//					return ((CSTNUCheckStatus) this.checkStatus);
//
//				if (edgeCopy == null && !AC.isEmpty()) {
//					// the new CB has to be added to the graph!
//					this.g.addEdge(AC, A, C);
//					newEdgesToCheck.add(AC);
//				} else if (edgeCopy != null && !edgeCopy.equalsLabeledValues(AC)) {
//					// CB was already present and it has been changed!
//					newEdgesToCheck.add(AC);
//				}
//			}
//
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "Rules, phase 1/2 done.");
//			}
//
//			/**
//			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
//			 * C-->A-->B
//			 */
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "Rules, phase 2/2: edge " + AB.getName() + " as second component.");
//			}
//			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
//				C = this.g.getSource(CA);
//				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞
//
//				CB = this.g.findEdge(C, B);
//				// I need to preserve the old edge to compare below
//				if (CB != null) {
//					edgeCopy = this.g.getEdgeFactory().create(CB);
//				} else {
//					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
//					edgeCopy = null;
//				}
//
//				labeledPropagationRule(C, A, B, CA, AB, CB, Z);
//
//				llm = CA.getLowerLabelMap().size() > 0;
//				if (llm) {
//					labeledLowerCaseRule(C, A, B, Z, CA, AB, CB);
//				}
//				ulm = AB.getUpperLabelMap().size() > 0;
//				if (ulm) {
//					labeledUpperCaseRule(C, A, B, CA, AB, CB);
//				}
//				if (llm && ulm) {
//					labeledCrossCaseRule(C, A, B, CA, AB, CB);
//				}
//
//				if (!this.checkStatus.consistency)
//					return ((CSTNUCheckStatus) this.checkStatus);
//
//				if (edgeCopy == null && !CB.isEmpty()) {
//					// the new CB has to be added to the graph!
//					this.g.addEdge(CB, C, B);
//					newEdgesToCheck.add(CB);
//				} else if (edgeCopy != null && !edgeCopy.equalsLabeledValues(CB)) {
//					// CB was already present and it has been changed!
//					newEdgesToCheck.add(CB);
//				}
//			}
//			if (LOG.isLoggable(Level.FINE)) {
//				LOG.log(Level.FINE, "Rules phase 2/2 done.\n");
//			}
//		}
//		if (LOG.isLoggable(Level.FINER)) {
//			LOG.log(Level.FINER, "End application all rules.");
//		}
//		edgesToCheck.clear();// in any case, this set has been elaborated. It is better to clear it out.
//		this.checkStatus.finished = newEdgesToCheck.size() == 0;
//		if (!this.checkStatus.finished) {
//			edgesToCheck.addAll(newEdgesToCheck);
//		}
//		// AllMax Projection check is done at the end of the all propagations.
//		if (!this.checkStatus.consistency)
//			this.checkStatus.finished = true;
//		return ((CSTNUCheckStatus) this.checkStatus);
//	}
//
//	/**
//	 * @return the checkStatus
//	 */
//	public CSTNUCheckStatus getCheckStatus() {
//		return ((CSTNUCheckStatus) this.checkStatus);
//	}
//
//	/**
//	 * @param g the g to set
//	 */
//	void setG(LabeledIntGraph g) {
//		if (g == null)
//			throw new IllegalArgumentException("Input graph is null!");
//		this.g = g;
//		this.checkStatus = new CSTNUCheckStatus();
//	}
//
//	// /**
//	// * Determines the minimal distance between all pairs of vertexes of the given graph if the graph does not contain any negative cycles. If the graph
//	// * contains a negative cycle, the method stops and returns false (the graph is anyway modified).
//	// *
//	// * @param g
//	// * the graph
//	// * @return true if the input graph does not contain any negative cycle, false otherwise.
//	// */
//	// boolean minimalDistanceGraphFast(final LabeledIntGraph g) {
//	// final int n = g.getVertexCount();
//	// final LabeledNode[] node = g.getVerticesArray();
//	// LabeledNode iV, jV, kV;
//	// LabeledIntEdge ik, kj, ij;
//	// int v;
//	// Label l;
//	// LabeledIntMap ijMap = null;
//	// for (int k = 0; k < n; k++) {
//	// kV = node[k];
//	// for (int i = 0; i < n; i++) {
//	// iV = node[i];
//	// for (int j = 0; j < n; j++) {
//	// if ((k == i) && (i == j)) {
//	// continue;
//	// }
//	// jV = node[j];
//	// Label nodeLabelConjunction = iV.getLabel().conjunction(jV.getLabel());
//	// if (nodeLabelConjunction == null)
//	// continue;
//	//
//	// ik = g.findEdge(iV, kV);
//	// kj = g.findEdge(kV, jV);
//	// if ((ik == null) || (kj == null)) {
//	// continue;
//	// }
//	// ij = g.findEdge(iV, jV);
//	//
//	// final Set<Object2IntMap.Entry<Label>> ikMap = ik.labeledValueSet();
//	// final Set<Object2IntMap.Entry<Label>> kjMap = kj.labeledValueSet();
//	// if ((k == i) || (k == j)) {
//	// ijMap = labeledIntMapFactory.create(ij.getLabeledValueMap());// this is necessary to avoid concurrent access to the same map by the
//	// // iterator.
//	// } else {
//	// ijMap = null;
//	// }
//	// for (final Object2IntMap.Entry<Label> ikL : ikMap) {
//	// for (final Object2IntMap.Entry<Label> kjL : kjMap) {
//	// l = ikL.getKey().conjunction(kjL.getKey());
//	// if (l == null) {
//	// continue;
//	// }
//	// l = l.conjunction(nodeLabelConjunction);// It is necessary to propagate with node labels!
//	// if (l == null) {
//	// continue;
//	// }
//	// if (ij == null) {
//	// ij = CSTN.makeNewEdge(node[i].getName() + "_" + node[j].getName(), LabeledIntEdge.ConstraintType.derived, g);
//	// g.addEdge(ij, iV, jV);
//	// }
//	// v = ikL.getValue() + kjL.getValue();
//	// if (ijMap != null) {
//	// ijMap.put(l, v);
//	// } else {
//	// ij.mergeLabeledValue(l, v);
//	// }
//	// if (i == j) // check negative cycles
//	// if (v < 0 || ij.getMinValue() < 0) {
//	// LOG.finer("Found a negative cycle on node " + iV.getName() + ": "
//	// + ((ijMap != null) ? ijMap : ij) + "\nIn details, ik=" + ik + ", kj="
//	// + kj + ", v=" + v + ", ij.getValue(" + l + ")=" + ij.getValue(l));
//	// return false;
//	// }
//	// }
//	// }
//	// if (ijMap != null) {
//	// ij.setLabeledValue(ijMap);
//	// }
//	// }
//	// }
//	// }
//	// return true;
//	// }
//
//}
