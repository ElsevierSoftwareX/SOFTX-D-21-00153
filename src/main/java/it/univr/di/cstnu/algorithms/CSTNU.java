package it.univr.di.cstnu.algorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.AbstractLabeledIntEdge;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledIntEdgeSupplier;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU).
 * It is based on instantaneous reaction and uses only rules LP, qR0, and qR3.
 * It is NOT assumed that CSTN are streamlined!
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNU extends CSTN3RIR {
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
					+ "Labeled Propagation Rule has been applied " + this.labeledValuePropagationCalls + " times.\n"
					+ "Labeled Upper Case Rule has been applied " + this.upperCaseRuleCalls + " times.\n"
					+ "Labeled Lower Case Rule has been applied " + this.lowerCaseRuleCalls + " times.\n"
					+ "Labeled Cross-Lower Case Rule has been applied " + this.crossCaseRuleCalls + " times.\n"
					+ "Labeled Letter Removal (LLR) Rule has been applied " + this.letterRemovalRuleCalls + " times.\n"
					// + "Negative qLoops: " + this.qAllNegLoop + "\n"
					// + "Negative qLoops with positive edge: " + this.qSemiNegLoop + "\n"
					+ "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)");
		}

		@Override
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
	static final Logger LOG = Logger.getLogger(CSTNU.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.2 - June, 14 2016";
	// static final public String VERSIONandDATE = "Version 5.0 - September, 8 2017";// introduced new rules
	// static final public String VERSIONandDATE = "Version 5.0 - September, 8 2017";// removed qLabels
	static final public String VERSIONandDATE = "Version 5.1 - November, 9 2017";// Replace Ω node with equivalent constraints.

	/**
	 * Returns the sum of all negative values (ignoring their labels) present in the edges of a graph. If an edge has more than one negative values, only
	 * the minimum among them is considered. For contingent link, also the lower case value is considered.
	 *
	 * @param g
	 * @return the sum of all negative value (negative value)
	 */
	// static final int getSumOfNegativeEdgeValues(final LabeledIntGraph g) {
	// int sum = 0;
	// if ((g == null) || (g.getEdgeCount() == 0))
	// return sum;
	//
	// for (final LabeledIntEdge e : g.getEdges()) {
	// final int min = e.getMinValue();
	// // int minLC = e.getMinLowerLabeledValue();
	// if ((min != Constants.INT_NULL) && (min < 0)) {
	// // if (minLC != null) {
	// // if (min.compareTo(minLC) > 0 ) min = minLC;
	// // }
	// sum += min;
	// }
	// }
	// LOG.log(Level.FINER, "The sum of all negative values is " + sum);
	// return sum;
	// }

	/**
	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Start...");
		}
		final CSTNU cstnu = new CSTNU();

		if (!cstnu.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Parameters ok!");
		}
		if (cstnu.versionReq) {
			System.out.println(CSTNU.class.getName() + " " + CSTNU.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Loading graph...");
		}
		CSTNUGraphMLReader graphMLReader = new CSTNUGraphMLReader(cstnu.fInput, LabeledIntTreeMap.class);
		cstnu.setG(graphMLReader.readGraph());
		cstnu.g.setFileName(cstnu.fInput);

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "LabeledIntGraph loaded!\nDC Checking...");
			}
		}
		CSTNUCheckStatus status;
		try {
			status = cstnu.dynamicControllabilityCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "LabeledIntGraph minimized!");
		}
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
			final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(new StaticLayout<>(cstnu.g));
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
	 * @return the conventional representation of a labeled value
	 */
	static final String upperCaseValueAsString(ALabel nodeName, int value, Label label) {
		return LabeledALabelIntTreeMap.entryAsString(label, value, nodeName);
	}

	/**
	 * @param value
	 * @param nodeName
	 * @param label
	 * @return the conventional representation of a labeled value
	 */
	static final String lowerCaseValueAsString(ALabel nodeName, int value, Label label) {
		return LabeledLowerCaseValue.entryAsString(nodeName, value, label, true);
	}

	/**
	 * Just to check if a new labeled value is negative, its label has not unknown literals and it is in a self loop.
	 *
	 * @param newLabel
	 * @param value
	 * @param source
	 * @param dest
	 * @param newEdge
	 * @return true if the value represent a negative loop!
	 */
	static boolean isNewLabeledValueANegativeLoop(final Label newLabel, final int value, final LabeledNode source, final LabeledNode dest,
			final LabeledIntEdge newEdge) {
		if (source.equalsByName(dest) && value < 0 && !newLabel.containsUnknown()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Found a negative loop in the edge " + newEdge);
				}
			}
			return true;
		}
		return false;
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
	@Override
	boolean checkWellDefinitionProperty1and3(final LabeledNode source, final LabeledNode destination, final LabeledIntEdge e, boolean hasToBeFixed)
			throws WellDefinitionException {

		super.checkWellDefinitionProperty1and3(source, destination, e, hasToBeFixed);
		final Label conjunctedLabel = source.getLabel().conjunction(destination.getLabel());

		// check the upper case labeled values
		for (final ALabel alabel : e.getUpperCaseValueMap().keySet()) {
			LabeledIntMap labeledValues = e.getUpperCaseValueMap().get(alabel);
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entry : labeledValues.entrySet()) {
				if (!entry.getKey().subsumes(conjunctedLabel)) {
					final String msg = "Upper case Labeled value " + entry + " of edge " + e.getName() + " does not subsume the endpoint labels.";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.SEVERE)) {
							LOG.log(Level.SEVERE, msg);
						}
					}
					throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
				}
			}
		}
		// check the lower case labeled values
		// there is only one value

		// OLD CODE for (final Entry<ALabel, LabeledIntTreeMap> entryE : e.getLowerLabelSet()) {
		// LabeledIntMap labeledValues = entryE.getValue();
		// for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entry : labeledValues.entrySet()) {
		LabeledLowerCaseValue lowerValue = e.getLowerCaseValue();
		if (!lowerValue.isEmpty()) {
			if (!lowerValue.getLabel().subsumes(conjunctedLabel)) {
				final String msg = "Labeled lower-case value " + lowerValue.toString() + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				if (Debug.ON) {
					if (LOG.isLoggable(Level.SEVERE)) {
						LOG.log(Level.SEVERE, msg);
					}
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
			return dynamicControllabilityCheck(214748364);
	}

	/**
	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
	 *
	 * @param timeout maximum number of seconds allowed to the computation. Pass value 214748364 (=2485 days) to give the maximum time.
	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException
	 *             if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	public CSTNUCheckStatus dynamicControllabilityCheck(int timeout) throws WellDefinitionException {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "\nStarting checking CSTNU dynamic controllability...\n");
			}
		}

		try {
			initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		EdgesToCheck edgesToCheck = new EdgesToCheck(this.g.getEdges());

		final int n = this.g.getVertexCount();
		int k = this.g.getContingentCount();
		if (k == 0) {
			k = 1;
		}
		int p = this.g.getObserverCount();
		if (p == 0) {
			p = 1;
		}
		int maxCycles = p * ((n * n) + (n * k) + k);// cstnu
		if (maxCycles == 0) {
			maxCycles = 2;
		}
		// TODO Find the right number of cycles.
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.info("The maximum number of possible cycles is " + maxCycles);
		}

		int i;
		this.checkStatus.finished = false;
		Instant startInstant = Instant.now();
		Instant timeOutInstant = startInstant.plusSeconds(timeout);
		for (i = 1; i <= maxCycles && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}
			this.checkStatus = this.oneStepDynamicControllability(edgesToCheck);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");

					// TODO
					String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm"));
					String fInputWOSuffix = this.g.getFileName().getPath().substring(0, this.g.getFileName().getPath().length() - 6);
					String newFileName = String.format("%s_%02d_%s.cstnu", fInputWOSuffix, i, now);
					File newFile = new File(newFileName);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, "Save the current CSTNU as " + newFileName);
						}
					}
					final CSTNUGraphMLWriter graphWriter = new CSTNUGraphMLWriter(new StaticLayout<>(this.g));
					try (Writer out = new PrintWriter(new BufferedWriter(new FileWriter(newFile)))) {
						graphWriter.save(this.g, out);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					// TODO
				}
			}
			if (!this.checkStatus.finished) {
				if (Instant.now().isAfter(timeOutInstant)) {
					String msg = "During the check # " + i + " time out of " + timeout + " seconds occured. ";
					if (Debug.ON) {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, msg);
						}
					}
					this.checkStatus.timeout = true;
					this.checkStatus.consistency = false;
					this.checkStatus.executionTimeNS = (long) (timeout * 10E9);
					return ((CSTNUCheckStatus) this.checkStatus);
				}
				if (this.checkStatus.consistency) {
					if (Debug.ON) {
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
			}
		}
		if (this.checkStatus.consistency && this.checkStatus.finished && i <= maxCycles) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "After " + (i - 1) + " cycle, all possible propagations have done."
							+ "\nNow it is necessary to check that AllMax Projection network is consistent.");
					LOG.info("AllMax Projection check starts...");
				}
			}
			LabeledIntGraph allMaxCSTN = makeAllMaxProjection();
			CSTN3RIR cstnChecker = new CSTN3RIR(allMaxCSTN);
			CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck();
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO))
					LOG.info("AllMax Projection network check done.\n");
			}
			if (!cstnStatus.consistency) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.INFO))
						LOG.info("The AllMax Projection network has at least one negative loop. The original network cannot be DC! Filename is "
								+ this.g.getName());
				}
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
			}
		}
		Instant endInstant = Instant.now();
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO,
							"After " + (i - 1) + " cycle, it has been stated that the network is NOT DC controllable.\nStatus: " + this.checkStatus);
				}
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Final NOT DC controllable network: " + this.g);
				}
			}
			return ((CSTNUCheckStatus) this.checkStatus);
		}

		if (i > maxCycles && !this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
				}
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Last NOT DC controllable network determined: " + this.g);
				}
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			return ((CSTNUCheckStatus) this.checkStatus);
		}

		// controllable && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Stable state reached. The network is DC controllable.\nNumber of cycles: " + (i - 1) + " over the maximum allowed "
						+ maxCycles + ".\nStatus: " + this.checkStatus);
			}
		}
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * Call {@link CSTN#initAndCheck()} and, then, check all contingent links.
	 * 
	 * @return true if the check is successful. The input g results to be modified by the method.
	 * @throws WellDefinitionException if the graph is null or it is not well formed.
	 */
	@Override
	public boolean initAndCheck() throws WellDefinitionException {
		if (this.checkStatus.initialized) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Initialization of a CSTNU can be done only one time! Reload the graph if a new init is necessary!");
				}
			}
			return true;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Starting checking graph as CSTNU well-defined instance...");
			}
		}

		// check underneath CSTN
		super.initAndCheck();
		this.checkStatus.initialized = false;

		// Contingent link have to be checked AFTER WD1 and WD3 have been checked and fixed!
		int maxWeightContingent = Constants.INT_POS_INFINITE;
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
			int initialValue = e.getValue(conjunctedLabel); // we consider only one value, the one with label == conjunctedLabel
			if (initialValue == Constants.INT_NULL) {
				if (e.lowerCaseValueSize() == 0 && e.upperCaseValueSize() == 0) {
					throw new IllegalArgumentException(
							"Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
				}
			}
			if (initialValue == 0) {
				if (d.isObserver() && e.lowerCaseValueSize() > 0) {
					e.removeLabel(conjunctedLabel);
					initialValue = Constants.INT_NULL;
				} else {
					throw new IllegalArgumentException(
							"Contingent edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0 < x < y < ∞.");
				}
			}

			LabeledIntEdge eInverted = this.g.findEdge(d, s);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "Edge " + e + " is contingent. Found its companion: " + eInverted);
			}
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
					lowerCaseValue = eInverted.getLowerCaseValue().getValue();
					if (lowerCaseValue != Constants.INT_NULL && -initialValue != lowerCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is contingent with a negative value and the inverted " + eInverted + " already contains a lower case value: "
										+ lowerCaseValueAsString(sourceALabel, lowerCaseValue, conjunctedLabel) + ".");
					}
					if (lowerCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is contingent with a negative value but the inverted " + eInverted
								+ " does not contain a lower case value neither a proper initial value. ");
					}

					if (lowerCaseValue == Constants.INT_NULL) {
						lowerCaseValue = -initialValue;
						eInverted.setLowerCaseValue(conjunctedLabel, sourceALabel, lowerCaseValue);
						s.setAlabel(sourceALabel);// to speed up DC checking!
						e.removeLabel(conjunctedLabel); // 2017-10-11 such value is not necessary for the check, but only for AllMax. AllMax building method
														// cares of it.
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "Inserted the lower label value: " + lowerCaseValueAsString(sourceALabel, lowerCaseValue, conjunctedLabel)
										+ " to edge " + eInverted);
							}
						}
						if (eInvertedInitialValue != Constants.INT_NULL) {
							upperCaseValue = -eInvertedInitialValue;
							e.mergeUpperCaseValue(conjunctedLabel, sourceALabel, upperCaseValue);
							eInverted.removeLabel(conjunctedLabel);// 2017-10-11 such value is not necessary for the check, but only for AllMax. AllMax building
																	// method cares of it.
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Inserted the upper label value: " + upperCaseValueAsString(sourceALabel, upperCaseValue, conjunctedLabel)
													+ " to edge " + e);
								}
							}
						}
					}
				} else {
					// e : A--->C
					// eInverted : C--->A
					ALabel destALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
					if (!destALabel.equals(d.getAlabel()))
						d.setAlabel(destALabel);// to speed up DC checking!
					upperCaseValue = eInverted.getUpperCaseValue(conjunctedLabel, destALabel);
					if (upperCaseValue != Constants.INT_NULL && -initialValue != upperCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is contingent with a positive value and the inverted " + eInverted + " already contains a upper case value: "
										+ upperCaseValueAsString(destALabel, upperCaseValue, conjunctedLabel) + ".");
					}
					if (upperCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is contingent with a positive value but the inverted " + eInverted
								+ " does not contain a upper case value neither a proper initial value. ");
					}
					if (upperCaseValue == Constants.INT_NULL) {
						upperCaseValue = -initialValue;
						eInverted.mergeUpperCaseValue(conjunctedLabel, destALabel, upperCaseValue);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "Inserted the upper label value: " + upperCaseValueAsString(destALabel, upperCaseValue, conjunctedLabel)
										+ " to edge " + eInverted);
							}
						}
						e.removeLabel(conjunctedLabel);// 2017-10-11 such value is not necessary for the check, but only for AllMax. AllMax building method
														// cares of it.
						if (eInvertedInitialValue != Constants.INT_NULL) {
							lowerCaseValue = -eInvertedInitialValue;
							e.setLowerCaseValue(conjunctedLabel, destALabel, lowerCaseValue);
							eInverted.removeLabel(conjunctedLabel);// 2017-10-11 such value is not necessary for the check, but only for AllMax. AllMax building
																	// method cares of it.
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Inserted the lower label value: " + lowerCaseValueAsString(destALabel, lowerCaseValue, conjunctedLabel)
													+ " to edge " + e);
								}
							}
						}
					}
				}
			} else {
				// here initialvalue is undefinite... UC and LC values are already present.
				if (e.upperCaseValueSize() > 0) {
					ALabel sourceALabel = new ALabel(s.getName(), this.g.getALabelAlphabet());
					if (!sourceALabel.equals(s.getAlabel()))
						s.setAlabel(sourceALabel);// to speed up DC checking!
				}
				if (eInverted.upperCaseValueSize() > 0) {
					ALabel destALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
					if (!destALabel.equals(d.getAlabel()))
						d.setAlabel(destALabel);// to speed up DC checking!
				}
			}
			// it is necessary to check max value
			int m = e.getMinUpperCaseValue();
			// LOG.warning("m value: " + m);
			if (m != Constants.INT_NULL && m < maxWeightContingent)
				maxWeightContingent = m;
			m = eInverted.getMinUpperCaseValue();
			if (m != Constants.INT_NULL && m < maxWeightContingent)
				maxWeightContingent = m;
		} // end contingent edges cycle

		maxWeightContingent = -maxWeightContingent;
		// LOG.warning("maxWeightContingent value: " + maxWeightContingent);
		// LOG.warning("this.maxWeight value: " + this.maxWeight);
		if (maxWeightContingent > this.maxWeight) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("It is necessary to update the horizon of the graph since -" + maxWeightContingent
							+ " is the new most negative found in contingent "
							+ "while -" + this.maxWeight + " is the most negative found in normal constraint.");
				}
			}
			// it is necessary to recalculate horizon
			this.maxWeight = maxWeightContingent;
			// Determine horizon value
			long product = ((long) this.maxWeight) * this.g.getVertexCount();
			if (product >= Constants.INT_POS_INFINITE) {
				throw new ArithmeticException("Horizon value is not representable by an integer.");
			}
			int oldHorizon = this.horizon;
			this.horizon = (int) product;
			// replace old horizon with the new one
			for (LabeledNode node : this.g.getVertices()) {
				if (node == this.Z)
					continue;
				LabeledIntEdge e = this.g.findEdge(this.Z, node);
				if (e.getValue(node.getLabel()) == oldHorizon) {
					e.removeLabel(node.getLabel());
					e.mergeLabeledValue(node.getLabel(), this.horizon);
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.warning("For each node, a new bound from Z has set to " + this.horizon + " instead of " + oldHorizon);
				}
			}
		}
		// init CSTNU structures.
		this.g.getLowerLabeledEdges();
		this.checkStatus.initialized = true;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Checking graph as CSTNU well-defined instance finished!\n");
			}
		}

		return true;
	}

	/**
	 * <h1>Labeled Cross-Lower Case (LCC*)</h1>
	 * See ICAPS 2018 paper.
	 * 
	 * <pre>
	 *     v,ℵ,β           u,c,α            
	 * X &lt;------------ C &lt;------------ A 
	 * adds 
	 *             u+v,ℵ,αβ
	 * X &lt;----------------------------A
	 * 
	 * if αβ∈P*, C ∉ ℵ, and v<0. If |ℵ|>1, then X must be Z.
	 * </pre>
	 * 
	 * Since it is assumed that L(C)=L(A)=α, there is only ONE lower-case labeled value u,c,α!
	 * 
	 * @param nA
	 * @param nC
	 * @param nX
	 * @param eAC CANNOT BE NULL
	 * @param eCX CANNOT BE NULL
	 * @param eAX CANNOT BE NULL
	 * @param nZ Z node of the CSTNU
	 * @return true if the rule has been applied.
	 */
	boolean labeledCrossLowerCaseRule(final LabeledNode nA, final LabeledNode nC, final LabeledNode nX, final LabeledIntEdge eAC, final LabeledIntEdge eCX,
			final LabeledIntEdge eAX, LabeledNode nZ) {

		boolean ruleApplied = false;
		final LabeledLowerCaseValue lowerCaseValue = eAC.getLowerCaseValue();
		if (lowerCaseValue.isEmpty())
			return false;

		// Since it is assumed that L(C)=L(A)=α, there is only ONE lower-case labeled value u,c,α!
		ALabel c = lowerCaseValue.getNodeName();
		Label alpha = lowerCaseValue.getLabel();
		int u = lowerCaseValue.getValue();

		final LabeledALabelIntTreeMap CXAllValueMap = eCX.getAllUpperCaseAndLabeledValuesMaps();
		if (CXAllValueMap.isEmpty())
			return false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "LCC*: start.");
			}
		}

		/*
		 * After some test, I verified that analyzing labeled value map and labeled upper-case map separately is not more effecient than
		 * to make a union of them and analyzing then.
		 */
		for (final ALabel aleph : CXAllValueMap.keySet()) {
			LabeledIntTreeMap valuesMap = CXAllValueMap.get(aleph);
			if (valuesMap == null)
				continue;
			final boolean alephNOTEmpty = !aleph.isEmpty();
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryCX : valuesMap.entrySet()) {
				final int v = entryCX.getIntValue();
				if (v >= 0)// the following condition is not applicable because we are considering instantaneous reaction: || (v == 0 && nX == nC))
					continue; // Rule condition!

				// Rule condition: upper case label cannot be equal or contain c name
				if (alephNOTEmpty && aleph.contains(c))
					continue;
				if (alephNOTEmpty && aleph.size() > 1 && nX != nZ)
					continue;// rule condition

				final Label beta = entryCX.getKey();
				final Label alphaBeta = beta.conjunction(alpha);
				if (alphaBeta == null)
					continue;
				final int z = Constants.sumWithOverflowCheck(v, u);

				if (nA == nX && z >= 0) { // Remember that nA can be equal to nX!!!
					continue;// positive upper-case values are super-seeded by 0 value that is implicit in a loop
				}
				final int oldZ = (alephNOTEmpty) ? eAX.getUpperCaseValue(alphaBeta, aleph) : eAX.getValue(alphaBeta);
				final String oldAX = eAX.toString();

				boolean localApp = (alephNOTEmpty) ? eAX.mergeUpperCaseValue(alphaBeta, aleph, z) : eAX.mergeLabeledValue(alphaBeta, z);

				if (localApp) {
					ruleApplied = true;
					if (alephNOTEmpty)
						((CSTNUCheckStatus) this.checkStatus).crossCaseRuleCalls++;
					else
						((CSTNUCheckStatus) this.checkStatus).lowerCaseRuleCalls++;
					if (Debug.ON)
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER,
									"LCLC applied to edge " + oldAX + ":\npartic: " + nX.getName() + " <---" + upperCaseValueAsString(aleph, v, beta)
											+ "--- " + nC.getName() + " <---" + lowerCaseValueAsString(c, u, alpha) + "--- "
											+ nA.getName()
											+ "\nresult: " + nX.getName()
											+ " <---" + upperCaseValueAsString(aleph, z, alphaBeta) + "--- " + nA.getName() + "; oldValue: "
											+ Constants.formatInt(oldZ));
						}
				}
				if (isNewLabeledValueANegativeLoop(alphaBeta, z, nA, nX, eAX)) {
					this.checkStatus.consistency = false;
					this.checkStatus.finished = true;
					return ruleApplied;
				}
			}
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "LCLC: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Labeled LetterRemoval* (LLR*)<br>
	 * See ICAPS 18
	 *
	 * <pre>
	 *       x,c,α          v,ℵ,β        
	 * C &lt;-------------A &lt;------------ X 
	 * adds 
	 *     v,ℵ',β
	 * A &lt;-----------X
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

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "LLR*: start.");
		}

		LabeledLowerCaseValue ACLowerCaseValueObj;

		for (final ALabel aleph : eXA.getUpperCaseValueMap().keySet()) {
			LabeledIntTreeMap valuesMap = eXA.getUpperCaseValueMap().get(aleph);
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> upperCaseEntryOfXA : valuesMap.entrySet()) {
				final Label beta = upperCaseEntryOfXA.getKey();
				final int v = upperCaseEntryOfXA.getIntValue();

				for (ALetter nodeLetter : aleph) {
					LabeledNode nC = this.g.getNode(nodeLetter.toString());
					LabeledIntEdge AC = this.g.findEdge(nA, nC);
					if (AC == null || (ACLowerCaseValueObj = AC.getLowerCaseValue()).isEmpty())
						continue;
					final Label alpha = ACLowerCaseValueObj.getLabel();
					final ALabel lowerCaseLetter = ACLowerCaseValueObj.getNodeName();
					int x = ACLowerCaseValueObj.getValue();
					if (x == Constants.INT_NULL || v < -x || !beta.subsumes(alpha))
						continue;
					final int oldZ = eXA.getUpperCaseValue(beta, aleph);
					final String oldXA = eXA.toString();

					ALabel aleph1 = new ALabel(aleph);
					aleph1.remove(nodeLetter);

					boolean mergeStatus = (aleph1.isEmpty()) ? eXA.mergeLabeledValue(beta, v) : eXA.mergeUpperCaseValue(beta, aleph1, v);
					if (mergeStatus) {
						ruleApplied = true;
						((CSTNUCheckStatus) this.checkStatus).letterRemovalRuleCalls++;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								if (LOG.isLoggable(Level.FINER))
									LOG.log(Level.FINER, "LLR* applied to edge " + oldXA + ":\n" + "partic: "
											+ nC + " <---" + lowerCaseValueAsString(lowerCaseLetter, x, alpha) + "--- " + nA.getName()
											+ " <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nX.getName()
											+ "\nresult: " + nA.getName() + " <---" + upperCaseValueAsString(aleph1, v, beta) + "--- " + nX.getName()
											+ "; oldValue: " + Constants.formatInt(oldZ));
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "LLR*: end.");
		}
		return ruleApplied;
	}

	/**
	 * Apply 'labeled no case' and 'labeled upper case' and 'forward labeled upper case' and 'labeled conjuncted upper case' rules.<br>
	 * See ICAPS 18 paper.
	 * 
	 * <pre>
	 * 1) CASE LNC+LUC*
	 *     v,ℵ,β           u,◇,α        
	 * W &lt;------------ Y &lt;------------ X 
	 * adds 
	 *     u+v,ℵ,αβ
	 * W &lt;------------------------------X
	 * 
	 * ℵ can be empty. If |ℵ|>1, then W must be Z.
	 * 
	 * 2) CASE FLUC+LCUC
	 *     v,ℵ,β           u,C,α        
	 * Z &lt;------------ Y &lt;------------ C 
	 * adds 
	 *     u+v,Cℵ,αβ
	 * Z &lt;------------------------------C
	 * 
	 * ℵ can be empty.
	 * </pre>
	 * 
	 * @param nX
	 * @param nY
	 * @param nW
	 * @param eXY CANNOT BE NULL
	 * @param eYW CANNOT BE NULL
	 * @param eXW CANNOT BE NULL
	 * @return true if a reduction is applied at least
	 */
	@Override
	// Don't rename such method because it has to overwrite the CSTN one!
	boolean labeledPropagationqLP(final LabeledNode nX, final LabeledNode nY, final LabeledNode nW, final LabeledIntEdge eXY, final LabeledIntEdge eYW,
			final LabeledIntEdge eXW) {

		Label nXnWLabel = nX.getLabel().conjunction(nW.getLabel());
		if (nXnWLabel == null)
			return false;

		boolean ruleApplied = false;
		boolean nWisNotZ = nW != this.Z;
		final LabeledALabelIntTreeMap YWAllLabeledValueMap = eYW.getAllUpperCaseAndLabeledValuesMaps();
		if (YWAllLabeledValueMap.size() == 0)
			return false;

		final Set<Object2IntMap.Entry<Label>> XYLabeledValueMap = eXY.getLabeledValueSet();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "LNC+LUC+FLUC+LCUC: start.");
		}

		// 1) CASE LNC + LUC*
		for (final Object2IntMap.Entry<Label> entryXY : XYLabeledValueMap) {
			final Label alpha = entryXY.getKey();
			final int u = entryXY.getIntValue();

			for (final ALabel aleph : YWAllLabeledValueMap.keySet()) {
				if (nWisNotZ && aleph.size() > 1)
					continue;// rule condition
				boolean emptyUpperCase = aleph.isEmpty();
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryYW : YWAllLabeledValueMap.get(aleph).entrySet()) {
					final Label beta = entryYW.getKey();

					Label alphaBeta;
					alphaBeta = alpha.conjunction(beta);
					if (alphaBeta == null)
						continue;

					if (!alphaBeta.subsumes(nXnWLabel)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER))
								LOG.log(Level.FINER,
										"New alphaBeta label " + alphaBeta + " does not subsume node labels " + nXnWLabel + ". New value cannot be added!");
						}
						continue;
					}

					final int v = entryYW.getIntValue();
					/**
					 * 2017-05-04 Roberto verifies that it is faster to propagate all values (positive and negative).
					 */
					int sum = Constants.sumWithOverflowCheck(u, v);

					// FIXME
					// It is true that propagates all values make convergence faster... but what's happen when values are HUGE?
					// I try...
					if (sum > 0)
						continue;

					final String oldXW;
					final int oldValue = (emptyUpperCase) ? eXW.getValue(alphaBeta) : eXW.getUpperCaseValue(alphaBeta, aleph);
					String logMsg = null;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							oldXW = eXW.toString();
							logMsg = "LNC+LUC* applied to edge " + oldXW + ":\n" + "partic: "
									+ nW.getName() + " <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
									+ upperCaseValueAsString(ALabel.emptyLabel, u, alpha) + "--- " + nX.getName()
									+ "\nresult: "
									+ nW.getName() + " <---" + upperCaseValueAsString(aleph, sum, alphaBeta) + "--- " + nX.getName()
									+ "; old value: " + Constants.formatInt(oldValue);
						}
					}

					if (nX == nW) {
						if (sum >= 0) {
							// it would be a redundant edge
							continue;
						}
						if (emptyUpperCase) {
							eXW.mergeLabeledValue(alphaBeta, sum);
							this.checkStatus.labeledValuePropagationCalls++;
						} else {
							eXW.mergeUpperCaseValue(alphaBeta, aleph, sum);
							((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.INFO)) {
								LOG.log(Level.INFO,
										logMsg + "\n***\nFound a negative loop " + upperCaseValueAsString(aleph, sum, alphaBeta) + " in the edge  " + eXW
												+ "\n***");
							}
						}
						this.checkStatus.consistency = false;
						this.checkStatus.finished = true;
						return true;
					}
					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						// value is stored only if it is more negative than the current one.
						continue;
					}
					boolean mergeStatus = (emptyUpperCase) ? eXW.mergeLabeledValue(alphaBeta, sum) : eXW.mergeUpperCaseValue(alphaBeta, aleph, sum);

					if (mergeStatus) {
						ruleApplied = true;
						if (emptyUpperCase) {
							this.checkStatus.labeledValuePropagationCalls++;
						} else {
							((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, logMsg);
							}
						}
					}
				}
			}
		}

		if (nWisNotZ) {
			// it is possible to return here, because the second part is applicable only when nW==Z.
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "LNC+LUC+FLUC+LCUC: end.");
			}
			return ruleApplied;
		}

		final ObjectSet<ALabel> XYUpperCaseALabels = eXY.getUpperCaseValueMap().keySet();

		// 2) CASE FLUC + LCUC
		ALabel nXasALabel = nX.getAlabel();
		for (final ALabel upperCaseLabel : XYUpperCaseALabels) {
			if (upperCaseLabel.size() != 1 || !upperCaseLabel.equals(nXasALabel)) {
				continue;// only UC label corresponding to original contingent upper case value is considered.
			}
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryXY : eXY.getUpperCaseValueMap().get(upperCaseLabel).entrySet()) {
				final Label alpha = entryXY.getKey();
				final int u = entryXY.getIntValue();

				for (final ALabel aleph : YWAllLabeledValueMap.keySet()) {
					for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryYW : YWAllLabeledValueMap.get(aleph).entrySet()) {
						final Label beta = entryYW.getKey();

						Label alphaBeta = alpha.conjunction(beta);
						if (alphaBeta == null)
							continue;
						if (!alphaBeta.subsumes(nXnWLabel)) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER))
									LOG.log(Level.FINER,
											"New alphaBeta label " + alphaBeta + " does not subsume node labels " + nXnWLabel + ". New value cannot be added!");
							}
							continue;
						}

						final ALabel upperCaseLetterAleph = upperCaseLabel.conjunction(aleph);
						final int v = entryYW.getIntValue();
						/**
						 * 2017-05-04 Roberto verifies that it is faster to propagate all values (positive and negative).
						 */
						int sum = Constants.sumWithOverflowCheck(u, v);

						final int oldValue = eXW.getUpperCaseValue(alphaBeta, upperCaseLetterAleph);

						final String oldXW;
						String logMsg = null;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								oldXW = eXW.toString();
								logMsg = "FLUC+LCUC applied to edge " + oldXW + ":\n" + "partic: "
										+ nW.getName() + " <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
										+ upperCaseValueAsString(upperCaseLabel, u, alpha) + "--- " + nX.getName()
										+ "\nresult: "
										+ nW.getName() + " <---" + upperCaseValueAsString(upperCaseLetterAleph, sum, alphaBeta) + "--- " + nX.getName()
										+ "; old value: " + Constants.formatInt(oldValue);
							}
						}

						if (nX == nW) {
							if (sum >= 0) {
								// it would be a redundant edge
								continue;
							}
							// sum is negative!
							eXW.mergeUpperCaseValue(alphaBeta, upperCaseLetterAleph, sum);
							((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.INFO)) {
									LOG.log(Level.INFO,
											logMsg + "\n***\nFound a negative loop " + upperCaseValueAsString(upperCaseLetterAleph, sum, alphaBeta)
													+ " in the edge  "
													+ eXW
													+ "\n***");
								}
							}
							this.checkStatus.consistency = false;
							this.checkStatus.finished = true;
							return true;
						}
						if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
							// in the case of A != C, a value is stored only if it is more negative than the current one.
							continue;
						}
						boolean mergeStatus = eXW.mergeUpperCaseValue(alphaBeta, upperCaseLetterAleph, sum);

						if (mergeStatus) {
							ruleApplied = true;
							((CSTNUCheckStatus) this.checkStatus).upperCaseRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, logMsg);
								}
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "LNC+LUC+FLUC+LCUC: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * On 2017-10-16 it has been shown that R0 is not more necessary for IR DC checking.<br>
	 * Applies rule qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Instantaneous reaction semantics is assumed.</b>
	 * <b>This differs from CSTN.labelModificationR0 in the checking also upper case value</b>
	 * 
	 * <pre>
	 * qR0:
	 * P? --[w, ℵ, α p]--&gt; Z 
	 * changes in 
	 * P? --[w, ℵ, α']--&gt; Z when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * ℵ is an aLabel or empty.
	 * </pre>
	 * 
	 * It is assumed that P? != Z.<br>
	 * 
	 * @param nObs the observation node
	 * @param ePZ the edge connecting P? ---&gt; Z
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationqR0(final LabeledNode nObs, final LabeledIntEdge ePZ) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR0: start.");
			}
		}

		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Method qR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
		boolean ruleApplied = false, mergeStatus = false;

		/*
		 * After some test, I verified that analyzing labeled value map and labeled upper-case map separately is not more effecient than
		 * to make a union of them and analyzing then.
		 */
		LabeledALabelIntTreeMap mapOfAllValues = ePZ.getAllUpperCaseAndLabeledValuesMaps();
		for (final ALabel aleph : mapOfAllValues.keySet()) {
			boolean alephNOTEmpty = !aleph.isEmpty();
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryPZ : mapOfAllValues.get(aleph).entrySet()) {
				Label alpha = entryPZ.getKey();
				if (alpha == null || !alpha.contains(p)) {// l can be nullified in a previous cycle.
					continue;
				}
				final int w = (alephNOTEmpty) ? ePZ.getUpperCaseValue(alpha, aleph) : ePZ.getValue(alpha);
				// It is necessary to re-check if the value is still present. Verified that it is necessary on Nov, 26 2015
				if (w == Constants.INT_NULL || R0qR0MainConditionForSkipping(w)) {// Table 1 ICAPS paper
					continue;
				}

				final Label alphaPrime = makeAlphaPrime(this.Z, nObs, p, alpha);
				if (alphaPrime == null) {
					continue;
				}

				// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
				String logMessage = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						logMessage = "qR0 simplifies a label of edge " + ePZ.getName()
								+ ":\nsource: " + nObs.getName() + " ---" + upperCaseValueAsString(aleph, w, alpha) + "---> " + this.Z.getName()
								+ "\nresult: " + nObs.getName() + " ---" + upperCaseValueAsString(aleph, w, alphaPrime) + "---> " + this.Z.getName();
					}
				}

				mergeStatus = (alephNOTEmpty) ? ePZ.mergeUpperCaseValue(alphaPrime, aleph, w) : ePZ.mergeLabeledValue(alphaPrime, w);
				if (mergeStatus) {
					ruleApplied = true;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER))
							LOG.log(Level.FINER, logMessage);
					}
					this.checkStatus.r0calls++;
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR0: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * <h1>Rule qR3*</h1>
	 * <b>Instantaneous reaction semantics is assumed.</b><br>
	 * On 2017-10-16 it has been shown that R3 is not more necessary.<br>
	 * 
	 * <pre>
	 * if P? --[w, ℵ1, γ]--&gt; Z &lt;--[v, ℵ, βθp']-- nS  and w < 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[max{w,v}, ℵℵ1, (γ★β)†]-- nS
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
	 * It is assumed that nS!=nD.
	 * 
	 * @param nS node
	 * @param eSZ LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationqR3(final LabeledNode nS, final LabeledIntEdge eSZ) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR3*: start.");
			}
		}
		boolean ruleApplied = false;

		ObjectList<LabeledIntEdge> Obs2ZEdges = getEdgeFromObserversToNode(this.Z);

		LabeledALabelIntTreeMap allValueMapSZ = eSZ.getAllUpperCaseAndLabeledValuesMaps();
		if (allValueMapSZ.isEmpty())
			return false;

		// check each edge from an observator to Z.
		for (final LabeledIntEdge eObsZ : Obs2ZEdges) {
			final LabeledNode nObs = this.g.getSource(eObsZ);
			if (nObs.equalsByName(nS))
				continue;

			final char p = nObs.getPropositionObserved();

			if (nS.getLabel().contains(p)) {// Table 2 ICAPS16
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "qR3*: Proposition '" + p + "' is present in the nS label '" + nS.getLabel()
								+ "'. WD1 must be preserved, so R3 cannot be applied.");
					}
				}
				continue;
			}

			// all labels from current Obs
			LabeledALabelIntTreeMap allValueMapObsZ = eObsZ.getAllUpperCaseAndLabeledValuesMaps();
			for (final ALabel aleph1 : allValueMapObsZ.keySet()) {
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryObsZ : allValueMapObsZ.get(aleph1).entrySet()) {
					final Label gamma = entryObsZ.getKey();
					final int w = entryObsZ.getIntValue();
					if (R3qR3MainConditionForSkipping(w, this.Z)) { // Table 1 ICAPS
						// (w == 0 && nD==Z), it means that P? is executed at 0. For IR, it cannot be modified.
						continue;
					}

					Label SZLabel;
					for (final ALabel aleph : allValueMapSZ.keySet()) {

						for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entrySZ : allValueMapSZ.get(aleph).entrySet()) {

							if (!(SZLabel = entrySZ.getKey()).contains(p)) {
								continue;
							}

							final int v = (aleph.isEmpty()) ? eSZ.getValue(SZLabel) : eSZ.getUpperCaseValue(SZLabel, aleph);
							if (v == Constants.INT_NULL) {
								// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
								continue;
							}

							Label newLabel = makeBetaGammaDagger4qR3(nS, nObs, p, gamma, SZLabel);
							if (newLabel == null) {
								continue;
							}
							final int max = R3qR3NewValue(v, w);
							ALabel newUpperCaseLetter = aleph.conjunction(aleph1);

							ruleApplied = (newUpperCaseLetter.isEmpty()) ? ruleApplied = eSZ.mergeLabeledValue(newLabel, max)
									: eSZ.mergeUpperCaseValue(newLabel, newUpperCaseLetter, max);

							if (ruleApplied) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										LOG.log(Level.FINER, "qR3* adds a labeled value to edge " + eSZ.getName() + ":\n"
												+ "source: " + nObs.getName() + " ---" + upperCaseValueAsString(aleph1, w, gamma) + "---> "
												+ this.Z.getName()
												+ " <---" + upperCaseValueAsString(aleph, v, SZLabel) + "--- " + nS.getName()
												+ "\nresult: add " + this.Z.getName() + " <---" + upperCaseValueAsString(newUpperCaseLetter, max, newLabel)
												+ "--- "
												+ nS.getName());
									}
								}
								this.checkStatus.r3calls++;
							}
						}
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
	 * Create a copy of this.g merging, for each each of g, all ordinary and upper case values.
	 * Moreover, for each edge representing lower bound of a contingent, sets its ordinary value to the maximum of the contingent.
	 *
	 * @return the all-max projection of the graph g (CSTN graph) without edges connecting nodes with non consistent labels.
	 */
	@SuppressWarnings("null")
	LabeledIntGraph makeAllMaxProjection() {
		LabeledIntGraph allMax = new LabeledIntGraph(this.g.getInternalLabeledValueMapImplementationClass(), this.g.getALabelAlphabet());
		// clone all nodes
		LabeledNode vNew;
		for (final LabeledNode v : this.g.getVertices()) {
			vNew = new LabeledNode(v);
			allMax.addVertex(vNew);
		}
		allMax.setZ(allMax.getNode(this.g.getZ().getName()));

		// clone all edges giving the right new endpoints corresponding the old ones.
		// we do not add edges connecting nodes in not consistent scenarios (such edges have only unknown labels).
		LabeledIntEdge eNew;
		LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = allMax.getEdgeFactory();
		for (final LabeledIntEdge e : this.g.getEdges()) {
			boolean toAdd = false;
			LabeledNode s = this.g.getSource(e);
			LabeledNode d = this.g.getDest(e);
			String sName = s.getName();
			String dName = d.getName();
			Label sdLabel = s.getLabel().conjunction(d.getLabel());
			if (sdLabel == null)
				continue;
			eNew = allMax.findEdge(sName, dName);
			toAdd = (eNew == null);
			if (toAdd)
				eNew = edgeFactory.get(e); // to preserve the name
			eNew.setConstraintType(ConstraintType.normal);
			LabeledALabelIntTreeMap allValueMapE = e.getAllUpperCaseAndLabeledValuesMaps();
			for (final ALabel alabel : allValueMapE.keySet()) {
				eNew.mergeLabeledValue(allValueMapE.get(alabel));
			}
			if (e.isContingentEdge()) {
				LabeledALabelIntTreeMap map = e.getUpperCaseValueMap();
				if (map != null && map.size() > 0) {
					LabeledIntEdge eNewInverted = allMax.findEdge(dName, sName);
					if (eNewInverted == null) {
						// this is the lower bound
						eNewInverted = edgeFactory.get(this.g.findEdge(d, s));
						allMax.addEdge((AbstractLabeledIntEdge) eNewInverted, dName, sName);
					}
					// eNewInverted.clearLowerCaseValue();
					// eNewInverted.clearUpperCaseValues();
					int ub = map.getValue(sdLabel, new ALabel(dName, this.g.getALabelAlphabet()));
					if (ub != Constants.INT_NULL) {
						eNewInverted.mergeLabeledValue(sdLabel, -ub);
					}
				}
			}
			if (toAdd)
				allMax.addEdge((AbstractLabeledIntEdge) eNew, sName, dName);
		}
		return allMax;
	}

	/**
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initAndCheck()}.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllability(final EdgesToCheck edgesToCheck) throws WellDefinitionException {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;

		this.checkStatus.cycles++;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules.");
		}

		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		int i = 1, n = edgesToCheck.size();
		int maxNumberOfValueInAnEdge = 0, maxNumberOfUpperCaseValuesInAnEdge = 0;
		LabeledIntEdge fatEdgeInLabeledValues = null, fatEdgeInUpperCaseValues = null;// for sure they will be initialized!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.log(Level.INFO, "Number of edges to analyze: " + n);
		}
		for (LabeledIntEdge AB : edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					if (i % 1000 == 0) {
						LOG.log(Level.INFO, "Considering edge " + (i++) + "/" + n);
					}
					if (LOG.isLoggable(Level.FINE)) {
						LOG.log(Level.FINE, "Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
					}
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);

			// initAndCheck does not resolve completely a qStar.
			// It is necessary to check here the edge before to consider the second edge.
			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
			edgeCopy = this.g.getEdgeFactory().get(AB);
			if (B == this.Z) {
				if (A.isObserver()) {
					// R0 on the resulting new values
					labelModificationqR0(A, AB);
				}
				labelModificationqR3(A, AB);
				if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
					// R0 on the resulting new values
					labelModificationqR0(A, AB);
				}

			} // end if B==Z
				// LLR is put here because it works like R0 and R3
			if (AB.getUpperCaseValueMap().size() > 0) {
				labeledLetterRemovalRule(A, B, AB);
			}

			if (!AB.equalsAllLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(AB, A, B, this.Z, this.g);
				if (Debug.ON) {
					int ne = AB.getLabeledValueMap().size();
					if (LOG.isLoggable(Level.INFO)) {
						if (ne > maxNumberOfValueInAnEdge) {
							maxNumberOfValueInAnEdge = ne;
							fatEdgeInLabeledValues = AB;
						}
						ne = AB.getUpperCaseValueMap().size();
						if (ne > maxNumberOfUpperCaseValuesInAnEdge) {
							maxNumberOfUpperCaseValuesInAnEdge = ne;
							fatEdgeInUpperCaseValues = AB;
						}
					}
				}
			}

			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules, phase 1/2: edge " + AB.getName() + " as first component.");
				}
			}
			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().get(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationqLP(A, B, C, AB, BC, AC);

				/**
				 * The following rule are called if there are condition (avoid to call for nothing)
				 */
				if (!AB.getLowerCaseValue().isEmpty()) {
					labeledCrossLowerCaseRule(A, B, C, AB, BC, AC, this.Z);
				}

				boolean add = false;
				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					add = true;
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AC)) {
					// CB was already present and it has been changed!
					add = true;
				}
				if (add) {
					newEdgesToCheck.add(AC, A, C, this.Z, this.g);
					if (Debug.ON) {
						int ne = AC.getLabeledValueMap().size();
						if (LOG.isLoggable(Level.INFO)) {
							if (ne > maxNumberOfValueInAnEdge) {
								maxNumberOfValueInAnEdge = ne;
								fatEdgeInLabeledValues = AC;
							}
							ne = AC.getUpperCaseValueMap().size();
							if (ne > maxNumberOfUpperCaseValuesInAnEdge) {
								maxNumberOfUpperCaseValuesInAnEdge = ne;
								fatEdgeInUpperCaseValues = AC;
							}
						}
					}
				}
				if (!this.checkStatus.consistency)
					return ((CSTNUCheckStatus) this.checkStatus);

			}

			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules, phase 1/2 done.");
				}
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules, phase 2/2: edge " + AB.getName() + " as second component.");
				}
			}
			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().get(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				labeledPropagationqLP(C, A, B, CA, AB, CB);

				if (!CA.getLowerCaseValue().isEmpty()) {
					labeledCrossLowerCaseRule(C, A, B, CA, AB, CB, this.Z);
				}

				boolean add = false;
				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
					add = true;
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(CB)) {
					// CB was already present and it has been changed!
					add = true;
				}
				if (add) {
					newEdgesToCheck.add(CB, C, B, this.Z, this.g);
				}
				if (Debug.ON) {
					int ne = CB.getLabeledValueMap().size();
					if (LOG.isLoggable(Level.INFO)) {
						if (ne > maxNumberOfValueInAnEdge) {
							maxNumberOfValueInAnEdge = ne;
							fatEdgeInLabeledValues = CB;
						}
						ne = CB.getUpperCaseValueMap().size();
						if (ne > maxNumberOfUpperCaseValuesInAnEdge) {
							maxNumberOfUpperCaseValuesInAnEdge = ne;
							fatEdgeInUpperCaseValues = CB;
						}
					}
				}

				if (!this.checkStatus.consistency)
					return ((CSTNUCheckStatus) this.checkStatus);

			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules phase 2/2 done.\n");
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application all rules.");
			}
		}
		edgesToCheck.clear();// in any case, this set has been elaborated. It is better to clear it out.
		this.checkStatus.finished = newEdgesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				// FIXME
				if (fatEdgeInLabeledValues != null) {
					LOG.log(Level.INFO,
							"At the end of cycle\n" + "Edge " + fatEdgeInLabeledValues.toString() + "\n contains the maximum number of labeled values: "
									+ maxNumberOfValueInAnEdge);
				}
				if (fatEdgeInUpperCaseValues != null) {
					LOG.log(Level.INFO, "Edge " + fatEdgeInUpperCaseValues.toString() + "\n contains the maximum number of labeled upper case values: "
							+ maxNumberOfUpperCaseValuesInAnEdge);
				}
				LOG.log(Level.INFO,
						"\nBe aware! If the number of values does not correspond to the printed one, it means that some following propagations reduced the number of values");
				if (edgesToCheck.size() < 200) {
					LOG.log(Level.INFO, "\n\nNumber of edge to check next main cycle are " + edgesToCheck.size() + ". Here they are in alphabetic order!");
					int ii = 1;
					for (LabeledIntEdge e : edgesToCheck) {
						LOG.log(Level.INFO, String.format("%3d: %s", ii++, e.toString()));
					}
				}
			}
		}
		if (!this.checkStatus.consistency)
			this.checkStatus.finished = true;
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * @return the checkStatus
	 */
	@Override
	public CSTNUCheckStatus getCheckStatus() {
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * @param g the g to set
	 */
	@Override
	public void setG(LabeledIntGraph g) {
		if (g == null)
			throw new IllegalArgumentException("Input graph is null!");
		this.g = g;
		this.Z = g.getZ();
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
	// LOG.log(Level.FINER, "Found a negative cycle on node " + iV.getName() + ": "
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
