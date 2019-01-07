package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
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
 * It is based on instantaneous reaction and uses only rules qR0, and qR3 as label modification rules.
 * <br>
 * The input network is transformed into its streamlined version and, then, checked.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNU extends CSTNIR3RwoNodeLabels {
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
					+ "Labeled Propagation Rule (zLp/Nc/Uc/zCore) has been applied " + this.labeledValuePropagationCalls + " times.\n"
					+ "Labeled Upper Case Rule (z!) has been applied " + this.upperCaseRuleCalls + " times.\n"
					+ "Labeled Lower Case Rule (zLc) has been applied " + this.lowerCaseRuleCalls + " times.\n"
					+ "Labeled Cross-Lower Case Rule (Cc) has been applied " + this.crossCaseRuleCalls + " times.\n"
					+ "Labeled Letter Removal (zLR) Rule has been applied " + this.letterRemovalRuleCalls + " times.\n"
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
	// static final public String VERSIONandDATE = "Version 5.1 - November, 9 2017";// Replace Ω node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 5.2 - December, 13 2017";// Adjusted after CSTN consolidation
	static final public String VERSIONandDATE = "Version 6.0 - February, 21 2018";// zUCore added

	/**
	 * Reads a CSTNU file and checks it.
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
					+ "Copyright © 2017,2018, Roberto Posenato");
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
		System.out.println("Checking started...");
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
				System.out.println("The given network is Dynamic controllable!");
			} else {
				System.out.println("The given network is NOT Dynamic controllable!");
			}
			System.out.println("Final graph: " + cstnu.g.toString());

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
	 * @param value
	 * @param source
	 * @param dest
	 * @param newEdge
	 * @param status
	 * @return true if the value represent a negative loop!
	 */
	static final boolean checkAndManageIfNewLabeledValueIsANegativeLoop(final int value, final LabeledNode source, final LabeledNode dest,
			final LabeledIntEdge newEdge, CSTNCheckStatus status) {
		if (source == dest && value < 0) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Found a negative loop in the edge " + newEdge);
				}
			}
			status.consistency = false;
			status.finished = true;
			return true;
		}
		return false;
	}

	/**
	 * Utility map that returns the activation time point (node) associated to a contingent link given the contingent time point,
	 * i.e., contingent link A===>C determines the entry (C,A) in this map.
	 */
	Object2ObjectMap<LabeledNode, LabeledNode> activationNode;

	/**
	 * Utility map that return the edge containing the lower case constraint of a contingent link given the contingent time point.
	 */
	Object2ObjectMap<LabeledNode, LabeledIntEdge> lowerContingentLink;

	/**
	 * Default constructor, package use only!
	 */
	CSTNU() {
		super();
		this.checkStatus = new CSTNUCheckStatus();
		this.activationNode = new Object2ObjectOpenHashMap<>();
		this.lowerContingentLink = new Object2ObjectOpenHashMap<>();
		this.propagationOnlyToZ = false;
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 */
	public CSTNU(LabeledIntGraph g) {
		super(g);// Remember that super(g) calls CSTNU.setG(g)!
		this.checkStatus = new CSTNUCheckStatus();
		this.propagationOnlyToZ = false;
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 * @param timeOut timeout for the check in seconds
	 */
	public CSTNU(LabeledIntGraph g, int timeOut) {
		this(g);
		this.timeOut = timeOut;
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 * @param timeOut timeout for the check in seconds
	 * @param propagationOnlyToZ
	 */
	public CSTNU(LabeledIntGraph g, int timeOut, boolean propagationOnlyToZ) {
		this(g);
		this.timeOut = timeOut;
		this.propagationOnlyToZ = propagationOnlyToZ;
	}

	/**
	 * Calls {@link CSTN#checkWellDefinitionProperty1and3(LabeledNode, LabeledNode, LabeledIntEdge, boolean)}
	 * and, then, checks upper and lower case values.
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
		int value;
		for (final ALabel alabel : e.getUpperCaseValueMap().keySet()) {
			LabeledIntMap labeledValues = e.getUpperCaseValueMap().get(alabel);
			for (Label currentLabel : labeledValues.keySet()) {
				value = e.getUpperCaseValue(currentLabel, alabel);
				if (value == Constants.INT_NULL)
					continue;
				if (!currentLabel.subsumes(conjunctedLabel)) {
					final String msg = "Upper case Labeled value " + upperCaseValueAsString(alabel, value, currentLabel) + " of edge "
							+ e.getName() + " does not subsume the conjuncted endpoint labels " + conjunctedLabel;
					if (hasToBeFixed) {
						e.removeUpperCaseValue(currentLabel, alabel);
						e.mergeUpperCaseValue(conjunctedLabel, alabel, value);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING,
										msg + " Labeled value " + upperCaseValueAsString(alabel, value, currentLabel) + " removed. Labeled value "
												+ upperCaseValueAsString(alabel, value, conjunctedLabel) + " added.");
							}
						}
						currentLabel = conjunctedLabel;
					} else {
						throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
					}
				}

				// Checks if label subsumes all observer-t.p. labels of observer t.p. whose proposition is present into the label.
				// WD3 property.
				Label currentLabelModified = currentLabel;
				for (final char l : currentLabel.getPropositions()) {
					LabeledNode obs = this.g.getObserver(l);
					if (obs == null) {
						final String msg = "Observation node of literal " + l + " of upper case label " + currentLabel + " in edge " + e + " does not exist.";
						if (Debug.ON) {
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING, msg);
							}
						}
						throw new WellDefinitionException(msg, WellDefinitionException.Type.ObservationNodeDoesNotExist);
					}
					// Checks WD3 and adjusts
					final Label obsLabel = obs.getLabel();
					if (!currentLabel.subsumes(obsLabel)) {
						final String msg = "Label " + currentLabel + " of edge " + e + " does not subsume label of obs node " + obs + ". It has been fixed.";
						if (Debug.ON) {
							if (LOG.isLoggable(Level.WARNING)) {
								LOG.log(Level.WARNING, msg);
							}
						}
						currentLabelModified = currentLabelModified.conjunction(obsLabel);
						if (currentLabelModified == null) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.WARNING)) {
									LOG.log(Level.WARNING, "Label " + currentLabel + " of edge " + e + " does not subsume label of obs node " + obs
											+ " and cannot be expanded because it becomes inconsistent.");
								}
							}
							throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelInconsistent);
						}
					}
				}
				if (!currentLabelModified.equals(currentLabel)) {
					e.removeUpperCaseValue(currentLabel, alabel);
					e.mergeUpperCaseValue(currentLabelModified, alabel, value);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.WARNING)) {
							LOG.log(Level.WARNING,
									"Labeled value " + upperCaseValueAsString(alabel, value, currentLabelModified) + " replace dishonest labeled value "
											+ upperCaseValueAsString(alabel, value, currentLabel) + " in edge " + e + ".");
						}
					}
				}
			}
		}
		// lower case
		LabeledLowerCaseValue lowerValue = e.getLowerCaseValue();
		if (!lowerValue.isEmpty()) {
			if (!lowerValue.getLabel().subsumes(conjunctedLabel)) {
				final String msg = "Labeled lower-case value " + lowerValue.toString() + " of edge " + e.getName() + " does not subsume the endpoint labels.";
				throw new WellDefinitionException(msg, WellDefinitionException.Type.LabelNotSubsumes);
			}
		}
		return true;
	}

	@Override
	public CSTNCheckStatus dynamicConsistencyCheck() throws WellDefinitionException {
		return dynamicControllabilityCheck();
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
		// FROM TIME 2018: horizon * |T|^2 3^|P| 2^|L|
		int maxCycles = this.horizon * n * n * p * p * p * k * k;
		if (maxCycles < 0) {
			maxCycles = Integer.MAX_VALUE;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.info("The maximum number of possible cycles is " + maxCycles);
		}

		int i;
		this.checkStatus.finished = false;
		Instant startInstant = Instant.now();
		Instant timeoutInstant = startInstant.plusSeconds(this.timeOut);
		for (i = 1; i <= maxCycles && this.checkStatus.consistency && !this.checkStatus.finished; i++) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}

			this.checkStatus = (this.propagationOnlyToZ) ? oneStepDynamicControllabilityLimitedToZ(edgesToCheck, timeoutInstant)
					: oneStepDynamicControllability(edgesToCheck, timeoutInstant);

			if (!this.checkStatus.finished) {
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					if (Debug.ON) {
						String msg = "During the check # " + i + " time out of " + this.timeOut + " seconds occured. ";
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, msg);
						}
					}
					this.checkStatus.executionTimeNS = ChronoUnit.NANOS.between(startInstant, Instant.now());
					return getCheckStatus();
				}
				if (this.checkStatus.consistency) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							StringBuilder log = new StringBuilder("During the check n. " + i + ", " + edgesToCheck.size()
									+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
							for (LabeledIntEdge e : edgesToCheck) {
								log.append("Edge " + e + "\n");
							}
							LOG.log(Level.FINER, log.toString());
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
		//
		// FOR HARD DEBUG
		// if (this.checkStatus.consistency && this.checkStatus.finished && i <= maxCycles) {
		// if (Debug.ON) {
		// if (LOG.isLoggable(Level.INFO)) {
		// LOG.log(Level.INFO, "After " + (i - 1) + " cycle, all possible propagations have done."
		// + "\nNow it is necessary to check that AllMax Projection network is consistent.");
		// LOG.info("AllMax Projection check starts...");
		// }
		// }
		// LabeledIntGraph allMaxCSTN = makeAllMaxProjection();
		// CSTNIR3R cstnChecker = new CSTNIR3R(allMaxCSTN);
		// CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck();
		// if (Debug.ON) {
		// if (LOG.isLoggable(Level.INFO))
		// LOG.info("AllMax Projection network check done.\n");
		// }
		// if (!cstnStatus.consistency) {
		// if (Debug.ON) {
		// if (LOG.isLoggable(Level.INFO))
		// LOG.info("The AllMax Projection network has at least one negative loop. The original network cannot be DC! Filename is "
		// + this.g.getName());
		// }
		// this.checkStatus.consistency = false;
		// this.checkStatus.finished = true;
		// }
		// }
		Instant endInstant = Instant.now();
		this.checkStatus.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!this.checkStatus.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO,
							"After " + (i - 1) + " cycle, it has been stated that the network is NOT DC controllable.\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Final NOT DC controllable network: " + this.g);
					}
				}
			}
			return getCheckStatus();
		}

		if (i > maxCycles && !this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + this.checkStatus);
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Last NOT DC controllable network determined: " + this.g);
					}
				}
			}
			this.checkStatus.consistency = this.checkStatus.finished;
			return getCheckStatus();
		}

		// controllable && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Stable state reached. The network is DC controllable.\nNumber of cycles: " + (i - 1) + " over the maximum allowed "
						+ maxCycles + ".\nStatus: " + this.checkStatus);
			}
		}
		LabeledIntGraph optimizedGraph = new LabeledIntGraph(this.g.getName(), this.g.getInternalLabeledValueMapImplementationClass());
		optimizedGraph.copyCleaningRedundantLabels(this.g);
		this.g = optimizedGraph;
		return getCheckStatus();
	}

	/**
	 * Provides the initAndCheck only the CSTN part to subclasses!
	 * 
	 * @return true if the CSTN part of the graph is correct and initialized.
	 * @throws WellDefinitionException
	 */
	boolean cstnInitAndCheck() throws WellDefinitionException {
		return super.initAndCheck();
	}

	/**
	 * Calls {@link CSTN#initAndCheck()} and, then, check all contingent links.
	 * This method works only with streamlined instances!
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
		cstnInitAndCheck();
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
			Entry<Label> minLabeledValue = e.getMinLabeledValue(); // we assume that instance was streamlined! Moreover, we consider only one value, the one
																	// with label == conjunctedLabel in the original network.
			int initialValue = minLabeledValue.getIntValue();
			Label conjunctedLabel = minLabeledValue.getKey();// s.getLabel().conjunction(d.getLabel());

			if (initialValue == Constants.INT_NULL) {
				if (e.lowerCaseValueSize() == 0 && e.upperCaseValueSize() == 0) {
					throw new IllegalArgumentException(
							"Contingent edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
				}
			}
			if (initialValue == 0) {
				if (d.isObserver() && e.lowerCaseValueSize() > 0) {
					e.removeLabeledValue(conjunctedLabel);
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

						/**
						 * History for lower bound.
						 * 2017-10-11 initialValue = minLabeledValue.getIntValue() is not necessary for the check, but only for AllMax. AllMax building
						 * method cares of it.
						 * 2017-12-22 If activation t.p. is Z, then removing initial value the contingent t.p. has not a right lower bound w.r.t. Z!
						 * 2018-02-21 initialValue = minLabeledValue.getIntValue() allows the reduction of # propagations.
						 */
						e.removeLabeledValue(conjunctedLabel);

						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "Inserted the lower label value: " + lowerCaseValueAsString(sourceALabel, lowerCaseValue, conjunctedLabel)
										+ " to edge " + eInverted);
							}
						}
						if (eInvertedInitialValue != Constants.INT_NULL) {
							upperCaseValue = -eInvertedInitialValue;
							e.mergeUpperCaseValue(conjunctedLabel, sourceALabel, upperCaseValue);
							/**
							 * History for upper bound.
							 * 2017-10-11 such value is not necessary for the check, but only for AllMax. AllMax building method cares of it.
							 * 2017-12-22 If activation t.p. is Z, then removing initial value the contingent t.p. has not a right upper bound w.r.t. Z!
							 * 2018-02-21 Upper bound are not necessary for the completeness, we ignore it.
							 */
							eInverted.removeLabeledValue(conjunctedLabel);

							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Inserted the upper label value: " + upperCaseValueAsString(sourceALabel, upperCaseValue, conjunctedLabel)
													+ " to edge " + e);
								}
							}
						}
					}
					// In order to speed up the checking, prepare some auxiliary data structure
					s.setAlabel(sourceALabel);// s is the contingent node.
					this.activationNode.put(s, d);
					this.lowerContingentLink.put(s, eInverted);

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

						/**
						 * @see comment "History for upper bound." above.
						 */
						e.removeLabeledValue(conjunctedLabel);

						if (eInvertedInitialValue != Constants.INT_NULL) {
							lowerCaseValue = -eInvertedInitialValue;
							e.setLowerCaseValue(conjunctedLabel, destALabel, lowerCaseValue);
							// In order to speed up the checking, prepare some auxiliary data structure
							this.activationNode.put(d, s);
							this.lowerContingentLink.put(d, e);

							/**
							 * @see comment "History for lower bound." above.
							 */
							eInverted.removeLabeledValue(conjunctedLabel);

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
				// here initialvalue is indefinite... UC and LC values are already present.
				if (!e.getLowerCaseValue().isEmpty()) {
					this.activationNode.put(d, s);
					this.lowerContingentLink.put(d, e);
				}
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
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("maxWeightContingent value found: " + maxWeightContingent + ". MaxWeight not contingent: " + this.maxWeight);
			}
		}
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
			long product = ((long) this.maxWeight) * (this.g.getVertexCount() - 1);// Z doesn't count!
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
					e.removeLabeledValue(node.getLabel());
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
	 * <h1>Labeled Cross-Lower Case (zLc/CC)</h1>
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
	 * @return true if the rule has been applied.
	 */
	boolean labeledCrossLowerCaseRule(final LabeledNode nA, final LabeledNode nC, final LabeledNode nX, final LabeledIntEdge eAC, final LabeledIntEdge eCX,
			final LabeledIntEdge eAX) {

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
				LOG.log(Level.FINER, "zLc/CC: start.");
			}
		}

		for (final ALabel aleph : CXAllValueMap.keySet()) {
			LabeledIntTreeMap valuesMap = CXAllValueMap.get(aleph);
			if (valuesMap == null)
				continue;
			boolean emptyAleph = aleph.isEmpty();

			final boolean alephNOTEmpty = !aleph.isEmpty();
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryCX : valuesMap.entrySet()) {
				final int v = entryCX.getIntValue();
				if (v >= 0)// the following condition is not applicable because we are considering instantaneous reaction: || (v == 0 && nX == nC))
					continue; // Rule condition!

				// Rule condition: upper case label cannot be equal or contain c name
				if (alephNOTEmpty && aleph.contains(c)) {
					continue;// rule condition
				}

				final Label beta = entryCX.getKey();
				final Label alphaBeta = beta.conjunction(alpha);
				if (alphaBeta == null)
					continue;
				final int sum = Constants.sumWithOverflowCheck(v, u);

				if (sum > 0)// && aleph.isEmpty()) // (sum > 0) works well for big POSITIVE instances! (sum > 0 && aleph.isEmpty()) works well for NEGATIVE
							// ones!
					continue;

				if (nA == nX && sum >= 0) { // Remember that nA can be equal to nX!!!
					continue;// positive upper-case values are super-seeded by 0 value that is implicit in a loop
				}

				final int oldValue = (emptyAleph) ? eAX.getValue(alphaBeta) : eAX.getUpperCaseValue(alphaBeta, aleph);

				if (oldValue != Constants.INT_NULL && oldValue <= sum) {
					continue;
				}
				String logMsg = null;
				if (Debug.ON) {
					final String oldAX = eAX.toString();
					if (LOG.isLoggable(Level.FINER)) {
						logMsg = "zLc/CC applied to edge " + oldAX + ":\npartic: " + nX.getName() + " <---" + upperCaseValueAsString(aleph, v, beta)
								+ "--- " + nC.getName() + " <---" + lowerCaseValueAsString(c, u, alpha) + "--- "
								+ nA.getName()
								+ "\nresult: " + nX.getName()
								+ " <---" + upperCaseValueAsString(aleph, sum, alphaBeta) + "--- " + nA.getName() + "; oldValue: "
								+ Constants.formatInt(oldValue);
					}
				}

				boolean localApp = (emptyAleph) ? eAX.mergeLabeledValue(alphaBeta, sum) : eAX.mergeUpperCaseValue(alphaBeta, aleph, sum);

				if (localApp) {
					ruleApplied = true;
					if (emptyAleph)
						getCheckStatus().crossCaseRuleCalls++;
					else
						getCheckStatus().lowerCaseRuleCalls++;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, logMsg);
						}
					}
				}

				if (checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nA, nX, eAX, this.checkStatus)) {
					return true;
				}
			}
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "zLc/CC: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Labeled LetterRemoval (zLr) and (zLr*)<br>
	 *
	 * <pre>
	 * Y ---(v,Cℵ,β)---&gt; Z &lt;---(w,ℵ1,α)--- A ---(x,c,⊡)---&gt; C 
	 *
	 * adds 
	 *         
	 * Y ---(v,ℵℵ1,β*α)---&gt; Z
	 *  
	 * if v ≥ w-x
	 * </pre>
	 * 
	 * zLr*
	 *
	 * <pre>
	 * C &lt;---(x,c,⊡)--- Y ---(v,Cℵ,β)---&gt; Z  
	 *
	 * adds 
	 *         
	 * Y ---(v,ℵ,β)---&gt; Z
	 *  
	 * if v < 0
	 * </pre>
	 * 
	 * @param nY
	 * @param eYZ
	 * @return true if the reduction has been applied.
	 */
	boolean zLabeledLetterRemovalRule(final LabeledNode nY, final LabeledIntEdge eYZ) {
		boolean ruleApplied = false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLR: start.");
		}

		for (final ALabel aleph : eYZ.getUpperCaseValueMap().keySet()) {
			if (aleph.isEmpty())
				continue;
			LabeledIntTreeMap YZvaluesMap = eYZ.getUpperCaseValueMap().get(aleph);
			if (YZvaluesMap == null)
				continue;
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> upperCaseEntryOfYA : YZvaluesMap.entrySet()) {
				final Label beta = upperCaseEntryOfYA.getKey();
				int v = upperCaseEntryOfYA.getIntValue();

				for (ALetter nodeLetter : aleph) {
					LabeledNode nC = this.g.getNode(nodeLetter.name);
					if (nY == nC) // Z is the activation time point!
						continue;
					LabeledNode nA = this.getActivationNode(nC);
					if (nA == this.Z)
						continue;

					if (nA == nY) {
						// zLr* special case
						if (v >= 0)
							continue;
						ALabel alephAleph1 = ALabel.clone(aleph);
						alephAleph1.remove(nodeLetter);

						final int oldValue = (Debug.ON) ? eYZ.getUpperCaseValue(beta, alephAleph1) : -1;
						final String oldYZ = (Debug.ON) ? eYZ.toString() : "";

						boolean mergeStatus = (alephAleph1.isEmpty()) ? eYZ.mergeLabeledValue(beta, v)
								: eYZ.mergeUpperCaseValue(beta, alephAleph1, v);

						if (mergeStatus) {
							ruleApplied = true;
							getCheckStatus().letterRemovalRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									if (LOG.isLoggable(Level.FINER))
										LOG.log(Level.FINER, "zLR* applied to edge " + oldYZ + ":\n" + "partic: "
												+ "Z <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName()
												+ "---(" + nC.getAlabel().toLowerCase() + ",...," + Label.emptyLabel + ")---> " + nodeLetter
												+ "\nresult: " + "Z <---" + upperCaseValueAsString(alephAleph1, v, beta) + nY.getName()
												+ "; oldValue: " + Constants.formatInt(oldValue));
								}
							}
						}
						continue;
					}
					LabeledIntEdge AC = this.getLowerContingentLink(nC);

					LabeledLowerCaseValue lowerCaseEntry = AC.getLowerCaseValue();
					if (lowerCaseEntry.isEmpty())
						continue;
					// Label l = lowerCaseEntry.getLabel();IT SHOULD BE empty!
					int x = lowerCaseEntry.getValue();

					LabeledIntEdge AZ = this.g.findEdge(nA, this.Z);

					for (ALabel aleph1 : AZ.getAllUpperCaseAndLabeledValuesMaps().keySet()) {
						if (aleph1.contains(nodeLetter))
							continue;
						LabeledIntTreeMap AZAlephMap = AZ.getAllUpperCaseAndLabeledValuesMaps().get(aleph1);
						if (AZAlephMap == null)
							continue;
						for (Entry<Label> entryAZ : AZAlephMap.entrySet()) {
							final Label alpha = entryAZ.getKey();
							final int w = entryAZ.getIntValue();

							// if (!alpha.subsumes(l));l SHOULD BE empty!
							// continue;// rule condition

							int newV = Math.max(v, w - x);

							ALabel alephAleph1 = aleph.conjunction(aleph1);
							alephAleph1.remove(nodeLetter);

							Label alphaBeta = alpha.conjunctionExtended(beta);

							final int oldValue = (Debug.ON) ? eYZ.getUpperCaseValue(alphaBeta, alephAleph1) : -1;
							final String oldYZ = (Debug.ON) ? eYZ.toString() : "";

							boolean mergeStatus = (alephAleph1.isEmpty()) ? eYZ.mergeLabeledValue(alphaBeta, newV)
									: eYZ.mergeUpperCaseValue(alphaBeta, alephAleph1, newV);

							if (mergeStatus) {
								ruleApplied = true;
								getCheckStatus().letterRemovalRuleCalls++;
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										if (LOG.isLoggable(Level.FINER))
											LOG.log(Level.FINER, "zLR applied to edge " + oldYZ + ":\n" + "partic: "
													+ nY.getName() + "---" + upperCaseValueAsString(aleph, v, beta) + "---> Z <---"
													+ upperCaseValueAsString(aleph1, w, alpha) + "--- " + nA.getName()
													+ "---" + lowerCaseValueAsString(nC.getAlabel(), x, Label.emptyLabel) + "---> " + nodeLetter
													+ "\nresult: " + nY.getName() + "---" + upperCaseValueAsString(alephAleph1, v, alphaBeta) + "---> Z"
													+ "; oldValue: " + Constants.formatInt(oldValue));
									}
								}
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLR: end.");
		}
		return ruleApplied;
	}

	/**
	 * Labeled Letter Removal (Lr)<br>
	 * 
	 * <pre>
	 * X ---(v,ℵ,β)---&gt; A ---(x,c,α)---&gt; C 
	 *                    
	 * adds 
	 *         
	 * X ---(v,ℵ',β)---&gt; A
	 * 
	 * if C ∈ ℵ, v ≥ −x, β entails α.
	 * ℵ'=ℵ'/C
	 * </pre>
	 * 
	 * @param nX
	 * @param nA
	 * @param eXA
	 * @return true if the reduction has been applied.
	 */
	boolean labeledLetterRemovalRule(final LabeledNode nX, final LabeledNode nA, final LabeledIntEdge eXA) {

		if (!this.isActivationNode(nA))
			return false;

		boolean ruleApplied = false;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLr: start.");
		}

		for (LabeledIntEdge eAC : this.g.getOutEdges(nA)) {
			if (eAC.getLowerCaseValue().isEmpty()) {
				continue;
			}
			// found a contingent link A===>C
			LabeledNode nC = this.g.getDest(eAC);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "zLr: found contingent link " + eAC);
			}
			for (final ALabel aleph : eXA.getUpperCaseValueMap().keySet()) {
				if (!aleph.contains(nC.getAlabel()))
					continue;
				LabeledIntTreeMap valuesMap = eXA.getUpperCaseValueMap().get(aleph);
				if (valuesMap == null)
					continue;
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> upperCaseEntryOfXA : valuesMap.entrySet()) {
					final Label beta = upperCaseEntryOfXA.getKey();
					int v = upperCaseEntryOfXA.getIntValue();

					LabeledLowerCaseValue ACLowerCaseValueObj = eAC.getLowerCaseValue();
					final Label alpha = ACLowerCaseValueObj.getLabel();
					int x = ACLowerCaseValueObj.getValue();
					if (v < -x || !beta.subsumes(alpha))
						continue;
					final int oldZ = (Debug.ON) ? eXA.getUpperCaseValue(beta, aleph) : -1;
					final String oldXA = (Debug.ON) ? eXA.toString() : "";

					ALabel aleph1 = ALabel.clone(aleph);
					aleph1.remove(nC.getAlabel());

					boolean mergeStatus = (aleph1.isEmpty()) ? eXA.mergeLabeledValue(beta, v) : eXA.mergeUpperCaseValue(beta, aleph1, v);
					if (mergeStatus) {
						ruleApplied = true;
						getCheckStatus().letterRemovalRuleCalls++;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								if (LOG.isLoggable(Level.FINER))
									LOG.log(Level.FINER, "Lr applied to edge " + oldXA + ":\n" + "partic: "
											+ nC + " <---" + lowerCaseValueAsString(ACLowerCaseValueObj.getNodeName(), x, alpha) + "--- " + nA.getName()
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
				LOG.log(Level.FINER, "Lr: end.");
		}
		return ruleApplied;
	}

	/**
	 * Apply 'labeled no case' and 'labeled upper case' and 'forward labeled upper case' and 'labeled conjuncted upper case' rules.<br>
	 * 
	 * <pre>
	 * 1) CASE zLP/Nc/Uc
	 *        v,ℵ,β           u,◇,α        
	 * W &lt;------------ Y &lt;------------ X 
	 * adds 
	 *     u+v,ℵ,αβ
	 * W &lt;------------------------------X
	 * 
	 * ℵ can be empty. If |ℵ|>1, then W must be Z.
	 * 
	 * 2) CASE z!
	 * Also known as z!
	 *     v,ℵ,β           u,C,α        
	 * Z &lt;------------ Y &lt;------------ C 
	 * adds 
	 *     u+v,Cℵ,αβ
	 * Z &lt;------------------------------C
	 * 
	 * ℵ can be empty.
	 * </pre>
	 * 
	 * This method implements also zUCore rule:
	 * 
	 * <pre>
	 * Z &lt;---(v, ℵ, β)---X &lt;---(u, ◇, α)----C
	 * adds
	 * Z &lt;---------(u+v,ℵ/C,αβ)---------------- C
	 * only when C is in ℵ and u<0
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

		// Label nXnWLabel = nX.getLabel().conjunction(nW.getLabel());
		// if (nXnWLabel == null)
		// return false;

		boolean ruleApplied = false;
		boolean nWisNotZ = nW != this.Z;
		final LabeledALabelIntTreeMap YWAllLabeledValueMap = eYW.getAllUpperCaseAndLabeledValuesMaps();
		if (YWAllLabeledValueMap.size() == 0)
			return false;

		final Set<Object2IntMap.Entry<Label>> XYLabeledValueMap = eXY.getLabeledValueSet();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLP/Nc/Uc/zUCore + z!: start.");
		}

		// 1) CASE LNC + LUC*
		for (final Object2IntMap.Entry<Label> entryXY : XYLabeledValueMap) {
			final Label alpha = entryXY.getKey();
			final int u = entryXY.getIntValue();

			for (ALabel aleph : YWAllLabeledValueMap.keySet()) {
				if (nWisNotZ && aleph.size() > 1)
					continue;// rule condition

				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryYW : YWAllLabeledValueMap.get(aleph).entrySet()) {
					final Label beta = entryYW.getKey();
					Label alphaBeta;
					alphaBeta = alpha.conjunction(beta);
					if (alphaBeta == null)
						continue;

					final int v = entryYW.getIntValue();
					int sum = Constants.sumWithOverflowCheck(u, v);
					/**
					 * 2018-07-18. With the sound-and-complete algorithm, positive values are not necessary any more.
					 * 2018-01-25. We discovered that it is necessary to propagate positive UPPER CASE values!
					 * normal positive values may be not propagate for saving computation time!
					 * aleph.isEmpty() is necessary!
					 */
					if (sum > 0)// && aleph.isEmpty()) // New condition that works well for big instances!
						continue;

					if (nX == nW && sum >= 0) {
						// it would be a redundant edge
						continue;
					}

					// zUCore rule:
					// aleph cannot be changed because we cannot assume yet that u is always < 0 in the set YWAllLabeledValueMap.get(aleph).entrySet()
					ALabel alephCore = ALabel.clone(aleph);
					boolean alephCoreEmpty = alephCore.isEmpty();
					if (!alephCoreEmpty && u < 0 && aleph.contains(nX.getAlabel())) {
						alephCore.remove(nX.getAlabel());
						alephCoreEmpty = alephCore.isEmpty();
					}

					final int oldValue = (alephCoreEmpty) ? eXW.getValue(alphaBeta) : eXW.getUpperCaseValue(alphaBeta, alephCore);

					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						// value is stored only if it is more negative than the current one.
						continue;
					}

					String logMsg = null;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							final String oldXW = eXW.toString();
							logMsg = "zLP/Nc/Uc/zUCore applied to edge " + oldXW + ":\n" + "partic: "
									+ nW.getName() + " <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
									+ upperCaseValueAsString(ALabel.emptyLabel, u, alpha) + "--- " + nX.getName()
									+ "\nresult: "
									+ nW.getName() + " <---" + upperCaseValueAsString(alephCore, sum, alphaBeta) + "--- " + nX.getName()
									+ "; old value: " + Constants.formatInt(oldValue);
						}
					}

					boolean mergeStatus = (alephCoreEmpty) ? eXW.mergeLabeledValue(alphaBeta, sum) : eXW.mergeUpperCaseValue(alphaBeta, alephCore, sum);

					if (mergeStatus) {
						ruleApplied = true;
						if (alephCoreEmpty) {
							this.checkStatus.labeledValuePropagationCalls++;
						} else {
							getCheckStatus().upperCaseRuleCalls++;
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, logMsg);
							}
						}

						if (checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nX, nW, eXW, this.checkStatus)) {
							return true;
						}
					}
				}
			}
		}

		if (nWisNotZ) {
			// it is possible to stop here, because the second part is applicable only when nW==Z.
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "zLP/Nc/Uc/zUCore + z!: end.");
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
						// if (!alphaBeta.subsumes(nXnWLabel)) {
						// if (Debug.ON) {
						// if (LOG.isLoggable(Level.FINER))
						// LOG.log(Level.FINER,
						// "New alphaBeta label " + alphaBeta + " does not subsume node labels " + nXnWLabel + ". New value cannot be added!");
						// }
						// continue;
						// }

						final ALabel upperCaseLetterAleph = upperCaseLabel.conjunction(aleph);
						final int v = entryYW.getIntValue();

						int sum = Constants.sumWithOverflowCheck(u, v);
						if (sum > 0)// && upperCaseLetterAleph.isEmpty()) // upperCaseLetterAleph is never empty!
							continue;

						if (nX == nW && sum >= 0) {
							// it would be a redundant edge
							continue;
						}

						final int oldValue = eXW.getUpperCaseValue(alphaBeta, upperCaseLetterAleph);

						if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
							// in the case of A != C, a value is stored only if it is more negative than the current one.
							continue;
						}

						String logMsg = null;
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								final String oldXW = eXW.toString();
								logMsg = "z! applied to edge " + oldXW + ":\n" + "partic: "
										+ nW.getName() + " <---" + upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
										+ upperCaseValueAsString(upperCaseLabel, u, alpha) + "--- " + nX.getName()
										+ "\nresult: "
										+ nW.getName() + " <---" + upperCaseValueAsString(upperCaseLetterAleph, sum, alphaBeta) + "--- " + nX.getName()
										+ "; old value: " + Constants.formatInt(oldValue);
							}
						}

						boolean mergeStatus = eXW.mergeUpperCaseValue(alphaBeta, upperCaseLetterAleph, sum);

						if (mergeStatus) {
							ruleApplied = true;
							getCheckStatus().upperCaseRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, logMsg);
								}
							}

							if (checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nX, nW, eXW, this.checkStatus)) {
								return true;
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "zLP/Nc/Uc/zUCore + z!: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Implements the zqR0 rule assuming instantaneous reaction and a streamlined network.<br>
	 * <b>This differs from {@link CSTNIR3RwoNodeLabels#labelModificationR0qR0(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * in the checking also upper case value</b>
	 * 
	 * @param nObs the observation node
	 * @param ePZ the edge connecting P? ---&gt; Z
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationqR0(final LabeledNode nObs, final LabeledIntEdge ePZ) {

		boolean ruleApplied = false, mergeStatus = false;

		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Method zqR0 called passing a non observation node as first parameter!");
				}
			}
			return false;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification zqR0: start.");
			}
		}

		/*
		 * After some test, I verified that analyzing labeled value map and labeled upper-case map separately is not more efficient than
		 * making an union of them and analyzing then.
		 */
		LabeledALabelIntTreeMap mapOfAllValues = ePZ.getAllUpperCaseAndLabeledValuesMaps();
		for (final ALabel aleph : mapOfAllValues.keySet()) {
			boolean alephNOTEmpty = !aleph.isEmpty();
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryPZ : mapOfAllValues.get(aleph).entrySet()) {
				Label alpha = entryPZ.getKey();
				if (alpha == null || !alpha.contains(p)) {
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
						logMessage = "zqR0 simplifies a label of edge " + ePZ.getName()
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
				LOG.log(Level.FINER, "Label Modification zqR0: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Implements the qR3* rule assuming instantaneous reaction and a streamlined network.<br>
	 * <b>This differs from {@link CSTNIR3RwoNodeLabels#labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * in the checking also upper case value.</b>
	 * 
	 * @param nS node
	 * @param eSZ LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationqR3(final LabeledNode nS, final LabeledIntEdge eSZ) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification zqR3*: start.");
			}
		}
		boolean ruleApplied = false;

		ObjectList<LabeledIntEdge> Obs2ZEdges = getEdgeFromObserversToNode(this.Z);

		LabeledALabelIntTreeMap allValueMapSZ = eSZ.getAllUpperCaseAndLabeledValuesMaps();
		if (allValueMapSZ.isEmpty())
			return false;

		final ObjectSet<Label> SZLabelSet = eSZ.getLabeledValueMap().keySet();
		SZLabelSet.addAll(eSZ.getUpperCaseValueMap().labelSet());

		Label allLiteralsSZ = Label.emptyLabel;
		for (Label l : SZLabelSet) {
			allLiteralsSZ = allLiteralsSZ.conjunctionExtended(l);
		}

		// check each edge from an observator to Z.
		for (final LabeledIntEdge eObsZ : Obs2ZEdges) {
			final LabeledNode nObs = this.g.getSource(eObsZ);
			if (nObs == nS)
				continue;

			final char p = nObs.getPropositionObserved();

			if (!allLiteralsSZ.contains(p)) {
				// no label in nS-->Z contain any literal of p.
				continue;
			}

			// all labels from current Obs
			LabeledALabelIntTreeMap allValueMapObsZ = eObsZ.getAllUpperCaseAndLabeledValuesMaps();
			for (final ALabel aleph1 : allValueMapObsZ.keySet()) {
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryObsZ : allValueMapObsZ.get(aleph1).entrySet()) {
					final int w = entryObsZ.getIntValue();
					if (R3qR3MainConditionForSkipping(w, this.Z)) { // Table 1 ICAPS
						continue;
					}

					final Label gamma = entryObsZ.getKey();

					Label SZLabel;
					for (final ALabel aleph : allValueMapSZ.keySet()) {

						for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entrySZ : allValueMapSZ.get(aleph).entrySet()) {
							SZLabel = entrySZ.getKey();

							if (SZLabel == null || !SZLabel.contains(p)) {
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
										LOG.log(Level.FINER, "zqR3* adds a labeled value to edge " + eSZ.getName() + ":\n"
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
				LOG.log(Level.FINER, "Label Modification zqR3*: end.");
			}
		}
		return ruleApplied;
	}


	@Override
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {
		throw new UnsupportedOperationException("Not implemented.");
	}

	/**
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initAndCheck()}.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllability(final EdgesToCheck edgesToCheck, Instant timeoutInstant) throws WellDefinitionException {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;

		this.checkStatus.cycles++;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules.");
		}

		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		int i = 1, n = edgesToCheck.size();
		// int maxNumberOfValueInAnEdge = 0, maxNumberOfUpperCaseValuesInAnEdge = 0;
		// LabeledIntEdge fatEdgeInLabeledValues = null, fatEdgeInUpperCaseValues = null;// for sure they will be initialized!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.log(Level.INFO, "Number of edges to analyze: " + n);
		}
		for (LabeledIntEdge AB : edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			// initAndCheck does not resolve completely a possible qStar. So, it is necessary to check here the edge before to consider the second edge.
			// The check has to be done in case B==Z and it consists in applying R0, R3 and zLabeledLetterRemovalRule!
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
				// zLLR is put here because it works like R0 and R3
				zLabeledLetterRemovalRule(A, AB);
			} else {
				// labeledLetterRemovalRule clean possible redundant a-letters.
				labeledLetterRemovalRule(A, B, AB);
			}
			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}

			if (!AB.equalsAllLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(AB, A, B, this.Z, this.g, this.propagationOnlyToZ);
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
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

				this.labeledLetterRemovalRule(B, C, BC);

				this.labeledPropagationqLP(A, B, C, AB, BC, AC);

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return getCheckStatus();
				}

				/**
				 * The following rule are called if there are condition (avoid to call for nothing)
				 */
				if (!AB.getLowerCaseValue().isEmpty()) {
					labeledCrossLowerCaseRule(A, B, C, AB, BC, AC);
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
					newEdgesToCheck.add(AC, A, C, this.Z, this.g, this.propagationOnlyToZ);
				}

				if (!this.checkStatus.consistency) {
					this.checkStatus.finished = true;
					return getCheckStatus();
				}

			}

			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules, phase 1/2 done.");
				}
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
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

				this.labeledLetterRemovalRule(C, A, CA);

				labeledPropagationqLP(C, A, B, CA, AB, CB);

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return getCheckStatus();
				}

				if (!CA.getLowerCaseValue().isEmpty()) {
					labeledCrossLowerCaseRule(C, A, B, CA, AB, CB);
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
					newEdgesToCheck.add(CB, C, B, this.Z, this.g, this.propagationOnlyToZ);
				}

				if (!this.checkStatus.consistency) {
					this.checkStatus.finished = true;
					return getCheckStatus();
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules phase 2/2 done.\n");
				}
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
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
		return getCheckStatus();
	}

	/**
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initAndCheck()}.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllabilityLimitedToZ(final EdgesToCheck edgesToCheck, Instant timeoutInstant) throws WellDefinitionException {
		//
		// This version consider only pair of edges going to Z, i.e., in the form A-->B-->Z,
		// 2018-01-25: with this method, performances worsen.
		LabeledNode B, A;
		LabeledIntEdge AZ, edgeCopy;

		this.checkStatus.cycles++;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules limited to Z.");
		}

		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		int i = 1, n = edgesToCheck.size();
		// int maxNumberOfValueInAnEdge = 0, maxNumberOfUpperCaseValuesInAnEdge = 0;
		// LabeledIntEdge fatEdgeInLabeledValues = null, fatEdgeInUpperCaseValues = null;// for sure they will be initialized!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.log(Level.INFO, "Number of edges to analyze: " + n);
		}
		for (LabeledIntEdge BZ : edgesToCheck) {
			if (this.g.getDest(BZ) != this.Z)
				continue;
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering edge " + (i++) + "/" + n + ": " + BZ + "\n");
				}
				if (BZ.getName().equals("e2"))
					LOG.log(Level.FINER, "Found : " + BZ + "\n");
			}
			B = this.g.getSource(BZ);
			// initAndCheck does not resolve completely a possible qStar. So, it is necessary to check here the edge before to consider the second edge.
			// The check has to be done in case B==Z and it consists in applying R0, R3 and zLabeledLetterRemovalRule!
			edgeCopy = this.g.getEdgeFactory().get(BZ);
			if (B.isObserver()) {
				// R0 on the resulting new values
				labelModificationqR0(B, BZ);
			}
			labelModificationqR3(B, BZ);
			if (B.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// R0 on the resulting new values
				labelModificationqR0(B, BZ);
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}

			// LLR is put here because it works like R0 and R3
			zLabeledLetterRemovalRule(B, BZ);

			if (!BZ.equalsAllLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(BZ, B, this.Z, this.Z, this.g, this.propagationOnlyToZ);
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}

			/**
			 * Make all propagation considering edge AB as first edge in the chain.<br>
			 * A-->B-->Z
			 */
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Apply rules to " + BZ.getName() + " as second component.");
				}
			}

			for (LabeledIntEdge AB : this.g.getInEdges(B)) {
				A = this.g.getSource(AB);

				AZ = this.g.findEdge(A, this.Z);
				// I need to preserve the old edge to compare below
				if (AZ != null) {
					edgeCopy = this.g.getEdgeFactory().get(AZ);
				} else {
					AZ = makeNewEdge(A.getName() + "_" + this.Z.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				labeledPropagationqLP(A, B, this.Z, AB, BZ, AZ);

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return getCheckStatus();
				}

				if (!AB.getLowerCaseValue().isEmpty()) {
					labeledCrossLowerCaseRule(A, B, this.Z, AB, BZ, AZ);
				}

				boolean add = false;
				if (edgeCopy == null && !AZ.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AZ, A, this.Z);
					add = true;
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AZ)) {
					// CB was already present and it has been changed!
					add = true;
				}
				if (add) {
					newEdgesToCheck.add(AZ, A, this.Z, this.Z, this.g, this.propagationOnlyToZ);
				}

				if (!this.checkStatus.consistency) {
					this.checkStatus.finished = true;
					return getCheckStatus();
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Rules phase done.\n");
				}
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
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
		return getCheckStatus();
	}

	/**
	 * @return the checkStatus
	 */
	@Override
	public final CSTNUCheckStatus getCheckStatus() {
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * @param g the g to set
	 */
	@Override
	public void setG(LabeledIntGraph g) {
		super.setG(g);
		if (this.activationNode == null) {
			this.activationNode = new Object2ObjectOpenHashMap<>();
			this.lowerContingentLink = new Object2ObjectOpenHashMap<>();
			return;
		}
		this.activationNode.clear();
		this.lowerContingentLink.clear();
	}

	/**
	 * @param nC
	 * @return the activation node associated to the contingent link having nC as contingent time point.
	 */
	LabeledNode getActivationNode(LabeledNode nC) {
		return this.activationNode.get(nC);
	}

	/**
	 * @param nC
	 * @return the edge containing the lower case value associated to the contingent link having nC as contingent time point.
	 */
	LabeledIntEdge getLowerContingentLink(LabeledNode nC) {
		return this.lowerContingentLink.get(nC);
	}

	/**
	 * @param nA
	 * @return true if nA is an activation time point
	 */
	boolean isActivationNode(LabeledNode nA) {
		return this.activationNode.containsValue(nA);
	}

	// /**
	// * Create a copy of this.g merging, for each each of g, all ordinary and upper case values.
	// * Moreover, for each edge representing lower bound of a contingent, sets its ordinary value to the maximum of the contingent.
	// *
	// * @return the all-max projection of the graph g (CSTN graph) without edges connecting nodes with non consistent labels.
	// */
	// @SuppressWarnings("null")
	// LabeledIntGraph makeAllMaxProjection() {
	// LabeledIntGraph allMax = new LabeledIntGraph(this.g.getInternalLabeledValueMapImplementationClass(), this.g.getALabelAlphabet());
	// // clone all nodes
	// LabeledNode vNew;
	// for (final LabeledNode v : this.g.getVertices()) {
	// vNew = new LabeledNode(v);
	// allMax.addVertex(vNew);
	// }
	// allMax.setZ(allMax.getNode(this.g.getZ().getName()));
	//
	// // clone all edges giving the right new endpoints corresponding the old ones.
	// // we do not add edges connecting nodes in not consistent scenarios (such edges have only unknown labels).
	// LabeledIntEdge eNew;
	// LabeledIntEdgeSupplier<? extends LabeledIntMap> edgeFactory = allMax.getEdgeFactory();
	// for (final LabeledIntEdge e : this.g.getEdges()) {
	// boolean toAdd = false;
	// LabeledNode s = this.g.getSource(e);
	// LabeledNode d = this.g.getDest(e);
	// String sName = s.getName();
	// String dName = d.getName();
	// Label sdLabel = s.getLabel().conjunction(d.getLabel());
	// if (sdLabel == null)
	// continue;
	// eNew = allMax.findEdge(sName, dName);
	// toAdd = (eNew == null);
	// if (toAdd)
	// eNew = edgeFactory.get(e); // to preserve the name
	// eNew.setConstraintType(ConstraintType.normal);
	// LabeledALabelIntTreeMap allValueMapE = e.getAllUpperCaseAndLabeledValuesMaps();
	// for (final ALabel alabel : allValueMapE.keySet()) {
	// eNew.mergeLabeledValue(allValueMapE.get(alabel));
	// }
	// if (e.isContingentEdge()) {
	// LabeledALabelIntTreeMap map = e.getUpperCaseValueMap();
	// if (map != null && map.size() > 0) {
	// LabeledIntEdge eNewInverted = allMax.findEdge(dName, sName);
	// if (eNewInverted == null) {
	// // this is the lower bound
	// eNewInverted = edgeFactory.get(this.g.findEdge(d, s));
	// allMax.addEdge((AbstractLabeledIntEdge) eNewInverted, dName, sName);
	// }
	// // eNewInverted.clearLowerCaseValue();
	// // eNewInverted.clearUpperCaseValues();
	// int ub = map.getValue(sdLabel, new ALabel(dName, this.g.getALabelAlphabet()));
	// if (ub != Constants.INT_NULL) {
	// eNewInverted.mergeLabeledValue(sdLabel, -ub);
	// }
	// }
	// }
	// if (toAdd)
	// allMax.addEdge((AbstractLabeledIntEdge) eNew, sName, dName);
	// }
	// return allMax;
	// }

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
