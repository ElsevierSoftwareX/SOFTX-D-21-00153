// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

import java.io.IOException;
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
import it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus;
import it.univr.di.cstnu.graph.CSTNPSUEdge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.cstnu.visualization.CSTNUStaticLayout;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledALabelIntTreeMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check streamlined Conditional Simple Temporal Network with Partial Shrinkable Uncertainty (CSTNPSU).
 * In this class, contingent link are, in real, guarded ones.
 * Therefore, the input graph HAS to have defined guarded links explicitly: upper bound edge must contain also the lower guard as lower case contingent value,
 * and lower bound edge must contain also the upper case negative value.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNPSU extends AbstractCSTN<CSTNPSUEdge> {
	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNPSU.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final String VERSIONandDATE = "Version 1.0 - Feb, 21 2017";
	static final String VERSIONandDATE = "Version 2.0 - Feb, 13 2020";

	/**
	 * Reads a CSTNPSU file and checks it.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws org.xml.sax.SAXException if any.
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Start...");
		}

		final CSTNPSU cstnpsu = new CSTNPSU();

		if (!cstnpsu.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Parameters ok!");
		}
		if (cstnpsu.versionReq) {
			System.out.println("CSTNPSU " + CSTNPSU.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017--2020, Roberto Posenato");
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Loading graph...");
		}
		TNGraphMLReader<CSTNPSUEdge> graphMLReader = new TNGraphMLReader<>();
		cstnpsu.setG(graphMLReader.readGraph(cstnpsu.fInput, EdgeSupplier.DEFAULT_CSTNPSU_EDGE_CLASS));
		cstnpsu.g.setInputFile(cstnpsu.fInput);

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "TNGraph loaded!\nDC Checking...");
			}
		}
		System.out.println("Checking started...");
		CSTNUCheckStatus status;
		try {
			status = cstnpsu.dynamicControllabilityCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "TNGraph minimized!");
		}
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given network is Dynamic controllable!");
			} else {
				System.out.println("The given network is NOT Dynamic controllable!");
			}
			System.out.println("Final graph: " + cstnpsu.g.toString());

			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}

		if (cstnpsu.fOutput != null) {
			final TNGraphMLWriter graphWriter = new TNGraphMLWriter(new CSTNUStaticLayout<>(cstnpsu.g));
			graphWriter.save(cstnpsu.g, cstnpsu.fOutput);
		}
	}

	/**
	 * Utility map that returns the activation time point (node) associated to a contingent link given the contingent time point,
	 * i.e., contingent link A===&gt;C determines the entry (C,A) in this map.
	 */
	Object2ObjectMap<LabeledNode, LabeledNode> activationNode;

	/**
	 * Utility map that return the edge containing the lower case constraint of a contingent link given the contingent time point.
	 */
	Object2ObjectMap<LabeledNode, CSTNPSUEdge> lowerContingentLink;

	/**
	 * Constructor for CSTNPSU
	 *
	 * @param graph TNGraph to check
	 */
	public CSTNPSU(TNGraph<CSTNPSUEdge> graph) {
		this();
		this.setG(graph);
	}

	/**
	 * Constructor for CSTNPSU
	 *
	 * @param graph TNGraph to check
	 * @param givenTimeOut timeout for the check in seconds
	 */
	public CSTNPSU(TNGraph<CSTNPSUEdge> graph, int givenTimeOut) {
		this(graph);
		this.timeOut = givenTimeOut;
	}

	/**
	 * Constructor for CSTNPSU.
	 * 
	 * @param graph TNGraph to check
	 * @param givenTimeOut timeout for the check in seconds
	 * @param givenPropagationOnlyToZ IS IGNORED FOR NOW!
	 *            public CSTNPSU(TNGraph<CSTNPSUEdge> graph, int givenTimeOut, boolean givenPropagationOnlyToZ) {
	 *            super(graph, givenTimeOut);
	 *            this.propagationOnlyToZ = givenPropagationOnlyToZ;
	 *            this.contingentAlsoAsOrdinary = true;
	 *            }
	 */

	/**
	 * Default constructor, package use only!
	 */
	CSTNPSU() {
		super();
		this.checkStatus = new CSTNUCheckStatus();
		this.activationNode = new Object2ObjectOpenHashMap<>();
		this.lowerContingentLink = new Object2ObjectOpenHashMap<>();
		this.propagationOnlyToZ = true;
		this.reactionTime = 0;// IR semantics
	}

	/**
	 * {@inheritDoc}
	 * Wrapper method for {@link #dynamicControllabilityCheck()}
	 */
	@Override
	public CSTNUCheckStatus dynamicConsistencyCheck() throws WellDefinitionException {
		return dynamicControllabilityCheck();
	}

	/**
	 * Checks the controllability of a CSTNPSU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
	 *
	 * @return an {@link it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus} object containing the final status and some statistics about the executed
	 *         checking.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if any.
	 */
	public CSTNUCheckStatus dynamicControllabilityCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "\nStarting checking CSTNU dynamic controllability...\n");
			}
		}

		try {
			this.initAndCheck();
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
		}

		EdgesToCheck<CSTNPSUEdge> edgesToCheck = new EdgesToCheck<>(this.g.getEdges());

		final int n = this.g.getVertexCount();
		int k = this.g.getContingentNodeCount();
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

			this.checkStatus = oneStepDynamicControllabilityLimitedToZ(edgesToCheck, timeoutInstant);
			// (this.propagationOnlyToZ) ? oneStepDynamicControllabilityLimitedToZ(edgesToCheck, timeoutInstant)
			// : oneStepDynamicControllability(edgesToCheck, timeoutInstant);

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
							for (CSTNPSUEdge e : edgesToCheck) {
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
		} // fine DC check
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
		if (this.cleanCheckedInstance) {
			this.gCheckedCleaned = new TNGraph<>(this.g.getName(), this.g.getEdgeImplClass());
			this.gCheckedCleaned.copyCleaningRedundantLabels(this.g);
		}
		return getCheckStatus();
	}

	/** {@inheritDoc} */
	@Override
	public final CSTNUCheckStatus getCheckStatus() {
		return ((CSTNUCheckStatus) this.checkStatus);
	}

	/**
	 * {@inheritDoc}
	 * Calls {@link CSTN#initAndCheck()} and, then, check all guarded links.
	 * This method works only with streamlined instances!
	 */
	@Override
	public boolean initAndCheck() throws WellDefinitionException {
		if (this.checkStatus.initialized) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Initialization of a CSTNPSU can be done only one time! Reload the graph if a new init is necessary!");
				}
			}
			return true;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Starting checking graph as CSTNPSU well-defined instance...");
			}
		}

		// check underneath CSTN
		coreCSTNInitAndCheck();
		this.checkStatus.initialized = false;

		// Contingent link have to be checked AFTER WD1 and WD3 have been checked and fixed!
		int maxWeightContingent = Constants.INT_POS_INFINITE;
		for (final CSTNPSUEdge e : this.g.getEdges()) {
			if (!e.isContingentEdge()) {
				continue;
			}
			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);
			/***
			 * Manage guarded link.
			 */
			Entry<Label> minLabeledValue = e.getMinLabeledValue(); // we assume that instance was streamlined! Moreover, we consider only one value, the one
																	// with label == conjunctedLabel in the original network.
			int initialValue = minLabeledValue.getIntValue();
			Label conjunctedLabel = minLabeledValue.getKey();// s.getLabel().conjunction(d.getLabel());

			if (initialValue == Constants.INT_NULL) {
				if (e.lowerCaseValueSize() == 0 && e.upperCaseValueSize() == 0) {
					throw new IllegalArgumentException(
							"Guarded edge " + e + " cannot be inizialized because it hasn't an initial value neither a lower/upper case value.");
				}
			}
			if (initialValue == 0) {
				if (d.isObserver() && e.lowerCaseValueSize() > 0) {
					e.removeLabeledValue(conjunctedLabel);
					initialValue = Constants.INT_NULL;
				} else {
					throw new IllegalArgumentException(
							"Guarded edge " + e + " cannot have a bound equals to 0. The two bounds [x,y] have to be 0 < x < y < ∞.");
				}
			}

			CSTNPSUEdge eInverted = this.g.findEdge(d, s);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "Edge " + e + " is contingent. Found its companion: " + eInverted);
			}
			if (eInverted == null) {
				throw new IllegalArgumentException("Guarded edge " + e + " is alone. The companion Guarded edge between " + d.getName()
						+ " and " + s.getName() + " does not exist. It has to!");
			}
			if (!eInverted.isContingentEdge()) {
				throw new IllegalArgumentException("Edge " + e + " is guarded while the companion edge " + eInverted + " is not guarded!\nIt has to be!");
			}
			/**
			 * Memo.
			 * If current initialValue is < 0, current edge is the lower bound A<---C.
			 * The lower case labeled value has to be put in the inverted edge if it is not already present.
			 * <br>
			 * If current initialValue is >=0, current edge is the upper bound A--->C.
			 * The upper case labeled value has to be put in the inverted edge if it is not already present.
			 * <br>
			 * If current initialValue is undefined, then we assume that the contingent link is already set and contains only upper/lower values!
			 */
			if (initialValue != Constants.INT_NULL) {
				int eInvertedInitialValue;
				int lowerCaseValueInEInverted = Constants.INT_NULL;
				int ucValue = Constants.INT_NULL;
				eInvertedInitialValue = eInverted.getValue(conjunctedLabel);

				if (initialValue < 0) {
					// current edge is the lower bound.
					ALabel contingentALabel = new ALabel(s.getName(), this.g.getALabelAlphabet());
					if (!contingentALabel.equals(s.getALabel()))
						s.setALabel(contingentALabel);// to speed up DC checking!
					lowerCaseValueInEInverted = eInverted.getLowerCaseValue(conjunctedLabel, contingentALabel);
					if (lowerCaseValueInEInverted != Constants.INT_NULL && -initialValue > lowerCaseValueInEInverted) {
						throw new IllegalArgumentException(
								"Edge " + e + " is guarded with a negative value and the inverted " + eInverted + " has a guard that is smaller: "
										+ CSTNU.lowerCaseValueAsString(contingentALabel, lowerCaseValueInEInverted, conjunctedLabel) + ".");
					}
					if (lowerCaseValueInEInverted == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is guarded with a negative value but the inverted " + eInverted
								+ " does not contain a lower case value neither a proper initial value. ");
					}

					if (lowerCaseValueInEInverted == Constants.INT_NULL) {
						lowerCaseValueInEInverted = -initialValue;
						eInverted.mergeLowerCaseValue(conjunctedLabel, contingentALabel, lowerCaseValueInEInverted);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER,
										"Inserted the lower label value: "
												+ CSTNU.lowerCaseValueAsString(contingentALabel, lowerCaseValueInEInverted, conjunctedLabel)
												+ " to edge " + eInverted);
							}
						}
					}
					Object2ObjectMap.Entry<Label, Entry<ALabel>> minUC = e.getMinUpperCaseValue();
					Label ucLabel = minUC.getKey();
					ALabel ucALabel = minUC.getValue().getKey();
					ucValue = minUC.getValue().getIntValue();
					if (ucValue == Constants.INT_NULL || !ucALabel.equals(contingentALabel)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER,
										"The upper-case value is missing or it has a wrong ALabel: " + minUC + "\n Fixing it!");
							}
						}
					}
					if (ucValue == Constants.INT_NULL) {
						if (eInvertedInitialValue != Constants.INT_NULL) {
							ucValue = -eInvertedInitialValue;
							e.mergeUpperCaseValue(conjunctedLabel, contingentALabel, ucValue);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Inserted the upper label value: " + CSTNU.upperCaseValueAsString(contingentALabel, ucValue, conjunctedLabel)
													+ " to edge " + e);
								}
							}
						} else {
							throw new IllegalArgumentException("Edge " + e + " is guarded without an upper-case value and the inverted " + eInverted
									+ " does not contain a initial value. It is not possible to set current edge correctly.");
						}
					} else {
						if (-ucValue > eInvertedInitialValue) {
							throw new IllegalArgumentException(
									"Edge " + e + " is guarded with an upper-case value greater that the upper value in the companion edge " + eInverted
											+ ". It is not possible to set current edge correctly.");
						}
						if (!ucALabel.equals(contingentALabel) || !ucLabel.equals(conjunctedLabel)) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Edge " + e + " is guarded with an upper-case value " + minUC + " havin a wrong ALabel w.r.t. the correct label "
													+ contingentALabel + ". Fixing it");
								}
							}
							e.removeUpperCaseValue(ucLabel, contingentALabel);
							e.mergeUpperCaseValue(conjunctedLabel, contingentALabel, ucValue);
						}
					}
					// In order to speed up the checking, prepare some auxiliary data structure
					s.setALabel(contingentALabel);// s is the contingent node.
					this.activationNode.put(s, d);
					this.lowerContingentLink.put(s, eInverted);

				} else {
					// e : A--->C
					// eInverted : C--->A
					ALabel contingentALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
					if (!contingentALabel.equals(d.getALabel()))
						d.setALabel(contingentALabel);// to speed up DC checking!
					Object2ObjectMap.Entry<Label, Entry<ALabel>> minUC = eInverted.getMinUpperCaseValue();
					Label ucLabel = minUC.getKey();
					ALabel ucALabel = minUC.getValue().getKey();
					ucValue = minUC.getValue().getIntValue();

					if (ucValue != Constants.INT_NULL) {
						if (initialValue < -ucValue) {
							throw new IllegalArgumentException(
									"Edge " + e + " is guarded with a positive value and the inverted " + eInverted
											+ " already contains a upper guard that is smaller: "
											+ CSTNU.upperCaseValueAsString(contingentALabel, ucValue, conjunctedLabel) + ".");
						}
						if (!ucALabel.equals(contingentALabel) || !ucLabel.equals(conjunctedLabel)) {
							throw new IllegalArgumentException(
									"Edge " + e + " is lower guard edge and the inverted " + eInverted
											+ " has a wrong upper guard because the node name is wrong or the label is wrong. Corrent upper guard: "
											+ CSTNU.upperCaseValueAsString(ucALabel, ucValue, ucLabel) + ".");
						}
					}
					if (ucValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is guarded with a positive value but the inverted " + eInverted
								+ " does not contain a upper case value neither a proper initial value. ");
					}
					if (ucValue == Constants.INT_NULL) {
						ucValue = -initialValue;
						eInverted.mergeUpperCaseValue(conjunctedLabel, contingentALabel, ucValue);
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER,
										"Inserted the upper label value: " + CSTNU.upperCaseValueAsString(contingentALabel, ucValue, conjunctedLabel)
												+ " to edge " + eInverted);
							}
						}
						if (eInvertedInitialValue != Constants.INT_NULL) {
							lowerCaseValueInEInverted = -eInvertedInitialValue;
							e.mergeLowerCaseValue(conjunctedLabel, contingentALabel, lowerCaseValueInEInverted);
							// In order to speed up the checking, prepare some auxiliary data structure
							this.activationNode.put(d, s);
							this.lowerContingentLink.put(d, e);
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"Inserted the lower label value: "
													+ CSTNU.lowerCaseValueAsString(contingentALabel, lowerCaseValueInEInverted, conjunctedLabel)
													+ " to edge " + e);
								}
							}
						}
					}
				}
			} else {
				// here initial value is indefinite... UC and LC values are already present.
				if (!e.getLowerCaseValueMap().isEmpty()) {
					this.activationNode.put(d, s);
					this.lowerContingentLink.put(d, e);
				}
				if (e.upperCaseValueSize() > 0) {
					ALabel sourceALabel = new ALabel(s.getName(), this.g.getALabelAlphabet());
					if (!sourceALabel.equals(s.getALabel()))
						s.setALabel(sourceALabel);// to speed up DC checking!
				}
				if (eInverted.upperCaseValueSize() > 0) {
					ALabel destALabel = new ALabel(d.getName(), this.g.getALabelAlphabet());
					if (!destALabel.equals(d.getALabel()))
						d.setALabel(destALabel);// to speed up DC checking!
				}
			}
			// it is necessary to check max value
			int m = e.getMinUpperCaseValue().getValue().getIntValue();
			// LOG.warning("m value: " + m);
			if (m != Constants.INT_NULL && m < maxWeightContingent)
				maxWeightContingent = m;
			m = eInverted.getMinUpperCaseValue().getValue().getIntValue();
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
					LOG.warning("-" + maxWeightContingent
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
			this.horizon = (int) product;
		}
		addUpperBounds();

		// init CSTNPSU structures.
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
	 * Executes one step of the dynamic controllability check.<br>
	 * Before the first execution of this method, it is necessary to execute {@link #initAndCheck()}.
	 *
	 * @param edgesToCheck set of edges that have to be checked.
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 */
	public CSTNUCheckStatus oneStepDynamicControllabilityLimitedToZ(final EdgesToCheck<CSTNPSUEdge> edgesToCheck, Instant timeoutInstant) {
		// This version consider only pair of edges going to Z, i.e., in the form A-->B-->Z,
		LabeledNode B, A;
		CSTNPSUEdge AZorZA, edgeCopy;

		this.checkStatus.cycles++;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules limited to Z.");
		}

		EdgesToCheck<CSTNPSUEdge> newEdgesToCheck = new EdgesToCheck<>();
		int i = 1, n = edgesToCheck.size();
		// int maxNumberOfValueInAnEdge = 0, maxNumberOfUpperCaseValuesInAnEdge = 0;
		// CSTNPSUEdge fatEdgeInLabeledValues = null, fatEdgeInUpperCaseValues = null;// for sure they will be initialized!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.log(Level.INFO, "Number of edges to analyze: " + n);
		}

		boolean BZ;
		LabeledNode Z = this.g.getZ();
		for (CSTNPSUEdge currentEdge : edgesToCheck) {
			if (this.g.getDest(currentEdge) == Z) {
				BZ = true;
			} else {
				if (this.g.getSource(currentEdge) == Z) {
					BZ = false;
				} else {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Ignoring edge " + (i++) + "/" + n + ": " + currentEdge + " because no one of its endopoints is Z.\n");
					}
					continue;
				}
			}
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering edge " + (i++) + "/" + n + ": " + currentEdge + "\n");
				}
			}
			B = (BZ) ? this.g.getSource(currentEdge) : this.g.getDest(currentEdge);

			edgeCopy = this.g.getEdgeFactory().get(currentEdge);

			// initAndCheck does not resolve completely a possible qStar. So, it is necessary to check here the edge before to consider the second edge.
			// The check has to be done in case B==Z and it consists in applying R0, R3 and zLabeledLetterRemovalRule!
			if (BZ && B.isObserver()) {
				// R0 on the resulting new values
				rM1(B, currentEdge);
			}
			if (BZ)
				rM2(B, currentEdge);
			if (BZ && B.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// R0 on the resulting new values
				rM1(B, currentEdge);
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}

			// LLR is put here because it works like R0 and R3
			if (BZ)
				rG4(B, currentEdge);

			if (BZ && !currentEdge.hasSameValues(edgeCopy)) {
				newEdgesToCheck.add(currentEdge, B, Z, Z, this.g, this.propagationOnlyToZ);
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}

			/**
			 * Make all propagation considering current edge as follows:<br>
			 * if (BZ) ==&gt; A--&gt;B--&gt;Z
			 * else ==&gt; Z--&gt;B--&gt;A
			 */
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Apply propagation rules to " + currentEdge.getName());
				}
			}

			for (CSTNPSUEdge ABorBA : (BZ) ? this.g.getInEdges(B) : this.g.getOutEdges(B)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "Considering other edge " + ABorBA.getName());
					}
				}
				A = (BZ) ? this.g.getSource(ABorBA) : this.g.getDest(ABorBA);

				AZorZA = (BZ) ? this.g.findEdge(A, Z) : this.g.findEdge(Z, A);

				// I need to preserve the old edge to compare below
				if (AZorZA != null) {
					edgeCopy = this.g.getEdgeFactory().get(AZorZA);
				} else {
					AZorZA = makeNewEdge((BZ) ? (A.getName() + "_" + Z.getName()) : (Z.getName() + "_" + A.getName()),
							CSTNPSUEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				if (BZ)
					rG1G3(A, B, Z, ABorBA, currentEdge, AZorZA);
				else
					rG5rG6rG7(Z, B, A, currentEdge, ABorBA, AZorZA);

				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					return getCheckStatus();
				}

				if (BZ && B.isContingent()) {
					rG2(A, B, Z, ABorBA, currentEdge, AZorZA);
				}

				boolean add = false;
				if (edgeCopy == null && !AZorZA.isEmpty()) {
					// the new CB has to be added to the graph!
					if (BZ) {
						this.g.addEdge(AZorZA, A, Z);
					} else {
						this.g.addEdge(AZorZA, Z, A);
					}
					add = true;
				} else if (edgeCopy != null && !edgeCopy.hasSameValues(AZorZA)) {
					// CB was already present and it has been changed!
					add = true;
				}
				if (add) {
					if (BZ) {
						newEdgesToCheck.add(AZorZA, A, Z, Z, this.g, this.propagationOnlyToZ);
					} else {
						newEdgesToCheck.add(AZorZA, Z, A, Z, this.g, this.propagationOnlyToZ);
					}
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

		// check guarded bounds
		for (LabeledNode nC : this.activationNode.keySet()) {
			LabeledNode nA = this.getActivationNode(nC);
			if (nA == null)
				continue;
			CSTNPSUEdge eAC = this.g.findEdge(nA, nC),
					eCA = this.g.findEdge(nC, nA),
					eAZ = this.g.findEdge(nA, Z),
					eZA = this.g.findEdge(Z, nA),
					eCZ = this.g.findEdge(nC, Z),
					eZC = this.g.findEdge(Z, nC);

			if (rG8(nA, Z, nC, eAZ, eZC, eAC, eCA)) {
				// eAC has been modified, eAZ and eZC must be added
				newEdgesToCheck.add(eAZ, nA, Z, Z, this.g, this.propagationOnlyToZ);
				newEdgesToCheck.add(eZC, Z, nC, Z, this.g, this.propagationOnlyToZ);
			}
			if (rG9(nC, Z, nA, eCZ, eZA, eAC, eCA)) {
				// eCA has been modified, eCZ and eZA must be added
				newEdgesToCheck.add(eCZ, nC, Z, Z, this.g, this.propagationOnlyToZ);
				newEdgesToCheck.add(eZA, Z, nA, Z, this.g, this.propagationOnlyToZ);
			}
			checkBoundGuarded(nA, nC, eAC, eCA, this.checkStatus);
			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return getCheckStatus();
			}
		}

		if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
			return getCheckStatus();
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
	 * @param nC node 
	 * @return the activation node associated to the contingent link having nC as contingent time point.
	 */
	LabeledNode getActivationNode(LabeledNode nC) {
		return this.activationNode.get(nC);
	}

	/**
	 * @param nC node 
	 * @return the edge containing the lower case value associated to the contingent link having nC as contingent time point.
	 */
	CSTNPSUEdge getLowerContingentLink(LabeledNode nC) {
		return this.lowerContingentLink.get(nC);
	}

	/**
	 * @param nA node
	 * @return true if nA is an activation time point
	 */
	boolean isActivationNode(LabeledNode nA) {
		return this.activationNode.containsValue(nA);
	}

	/**
	 * Resets all internal structures
	 */
	@Override
	void reset() {
		super.reset();
		if (this.activationNode == null) {
			this.activationNode = new Object2ObjectOpenHashMap<>();
			this.lowerContingentLink = new Object2ObjectOpenHashMap<>();
			return;
		}
		this.activationNode.clear();
		this.lowerContingentLink.clear();
	}

	/**
	 * Apply rules rG1 and rG3 of Table 1 in 'Extending CSTN with partially shrinkable uncertainty' (TIME18).<br>
	 *
	 * <pre>
	 * 1) rG1
	 *        v,ℵ,β           u,◇,α        
	 * Z &lt;------------ Y &lt;------------ X 
	 * adds 
	 *     u+v,ℵ,α★β
	 * Z &lt;------------------------------X
	 * when u+v &lt; 0 and u &lt; 0.
	 * If u &ge; 0, α★β must be αβ
	 * ℵ can be empty. 
	 * 
	 * 2) rG3
	 *     v,ℵ,β           u,X,α        
	 * Z &lt;------------ Y &lt;------------ X 
	 * adds 
	 *     u+v,Xℵ,α★β
	 * Z &lt;------------------------------X
	 * when u+v &lt; 0 and X ∉ ℵ
	 * ℵ can be empty.
	 * </pre>
	 * 
	 * @param nX node
	 * @param nY node
	 * @param nZ node
	 * @param eXY CANNOT BE NULL
	 * @param eYW CANNOT BE NULL
	 * @param eXZ CANNOT BE NULL
	 * @return true if a reduction is applied at least
	 */
	boolean rG1G3(final LabeledNode nX, final LabeledNode nY, final LabeledNode nZ, final CSTNPSUEdge eXY, final CSTNPSUEdge eYW,
			final CSTNPSUEdge eXZ) {

		boolean ruleApplied = false;

		final LabeledALabelIntTreeMap YZAllLabeledValueMap = eYW.getAllUpperCaseAndLabeledValuesMaps();
		if (YZAllLabeledValueMap.size() == 0)
			return false;

		final Set<Object2IntMap.Entry<Label>> XYLabeledValueMap = eXY.getLabeledValueSet();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG1G3: start.");
		}

		// 1) rG1
		for (final Object2IntMap.Entry<Label> entryXY : XYLabeledValueMap) {
			final Label alpha = entryXY.getKey();
			final int u = entryXY.getIntValue();

			for (ALabel aleph : YZAllLabeledValueMap.keySet()) {
				for (Object2IntMap.Entry<Label> entryYW : YZAllLabeledValueMap.get(aleph).entrySet()) {// entrySet read-only
					final Label beta = entryYW.getKey();
					Label alphaBeta;
					alphaBeta = (u < 0) ? alpha.conjunctionExtended(beta) : alpha.conjunction(beta);
					if (alphaBeta == null)
						continue;
					final int v = entryYW.getIntValue();
					int sum = Constants.sumWithOverflowCheck(u, v);
					/**
					 * 2018-07-18. With the sound-and-complete algorithm, positive values are not necessary any more.
					 */
					if (sum > 0 || (sum == 0 && nZ == nX))
						continue;

					final int oldValue = (aleph.isEmpty()) ? eXZ.getValue(alphaBeta) : eXZ.getUpperCaseValue(alphaBeta, aleph);

					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						// value is stored only if it is more negative than the current one.
						continue;
					}

					String logMsg = null;
					if (Debug.ON) {
						final String oldXW = eXZ.toString();
						logMsg = "CSTNPSU rG1 applied to edge " + oldXW + ":\n" + "partic: "
								+ nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
								+ CSTNU.upperCaseValueAsString(ALabel.emptyLabel, u, alpha) + "--- " + nX.getName()
								+ "\nresult: "
								+ nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(aleph, sum, alphaBeta) + "--- " + nX.getName()
								+ "; old value: " + Constants.formatInt(oldValue);
					}

					boolean mergeStatus = (aleph.isEmpty()) ? eXZ.mergeLabeledValue(alphaBeta, sum) : eXZ.mergeUpperCaseValue(alphaBeta, aleph, sum);

					if (mergeStatus) {
						ruleApplied = true;
						if (aleph.isEmpty()) {
							this.checkStatus.labeledValuePropagationCalls++;
						} else {
							getCheckStatus().zEsclamationRuleCalls++;
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, logMsg);
							}
						}

						if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nX, nZ, eXZ, this.checkStatus)) {
							if (Debug.ON) {
								if (LOG.isLoggable(Level.INFO)) {
									LOG.log(Level.INFO, logMsg);
								}
							}
							return true;
						}
					}
				}
			}
		}

		// 2) rG3
		if (!nX.isContingent() || (nX.isContingent() && this.getActivationNode(nX) != nY))
			return ruleApplied;

		LabeledIntTreeMap eXYUpperCaseValues = eXY.getUpperCaseValueMap().get(nX.getALabel());
		if (eXYUpperCaseValues != null) {

			for (Object2IntMap.Entry<Label> entryXY : eXYUpperCaseValues.entrySet()) {// entrySet read-only
				final Label alpha = entryXY.getKey();
				final int u = entryXY.getIntValue();
				for (final ALabel aleph : YZAllLabeledValueMap.keySet()) {
					if (aleph.contains(nX.getALabel()))
						continue;
					for (Object2IntMap.Entry<Label> entryYW : YZAllLabeledValueMap.get(aleph).entrySet()) {// entrySet read-only
						final Label beta = entryYW.getKey();
						Label alphaBeta = alpha.conjunctionExtended(beta);
						if (alphaBeta == null)
							continue;
						final ALabel newXAleph = nX.getALabel().conjunction(aleph);
						final int v = entryYW.getIntValue();

						int sum = Constants.sumWithOverflowCheck(u, v);
						if (sum > 0) {
							continue;
						}

						final int oldValue = eXZ.getUpperCaseValue(alphaBeta, newXAleph);
						if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
							continue;
						}

						String logMsg = null;
						if (Debug.ON) {
							final String oldXW = eXZ.toString();
							logMsg = "CSTNPSU rG3 applied to edge " + oldXW + ":\n" + "partic: "
									+ nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(aleph, v, beta) + "--- " + nY.getName() + " <---"
									+ CSTNU.upperCaseValueAsString(nX.getALabel(), u, alpha) + "--- " + nX.getName()
									+ "\nresult: "
									+ nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(newXAleph, sum, alphaBeta) + "--- " + nX.getName()
									+ "; old value: " + Constants.formatInt(oldValue);
						}

						boolean mergeStatus = eXZ.mergeUpperCaseValue(alphaBeta, newXAleph, sum);
						if (mergeStatus) {
							ruleApplied = true;
							getCheckStatus().zEsclamationRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER, logMsg);
								}
							}

							if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nX, nZ, eXZ, this.checkStatus)) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.INFO)) {
										LOG.log(Level.INFO, logMsg);
									}
								}
								return true;
							}

							// clean redundant aleph
							// FIXME
							// if (newXAleph.size() > 1) {
							// ALabel[] alephSet = eXZ.getUpperCaseValueMap().keySet().toArray(new ALabel[0]);
							// if (alephSet.length == 0)
							// continue;
							// for (ALabel aleph1 : alephSet) {
							// if (aleph1.contains(nX.getAlabel()))
							// continue;
							// if (newXAleph.contains(aleph1)) {
							// eXZ.getUpperCaseValueMap().remove(aleph1);
							// continue;
							// }
							// }
							// }
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "rG1rG3: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * <b>rG2</b>
	 * 
	 * <pre>
	 *     v,ℵ,β           u,c,α            
	 * Z &lt;------------ C &lt;------------ A 
	 * adds 
	 *             u+v,ℵ,α★β
	 * Z &lt;----------------------------A
	 * 
	 * if C ∉ ℵ and v+u &lt; 0.
	 * </pre>
	 * 
	 * @param nA node 
	 * @param nC none
	 * @param nZ none
	 * @param eAC CANNOT BE NULL
	 * @param eCZ CANNOT BE NULL
	 * @param eAZ CANNOT BE NULL
	 * @return true if the rule has been applied.
	 */
	boolean rG2(final LabeledNode nA, final LabeledNode nC, final LabeledNode nZ, final CSTNPSUEdge eAC, final CSTNPSUEdge eCZ,
			final CSTNPSUEdge eAZ) {

		boolean ruleApplied = false;
		if (this.activationNode.get(nC) != nA)
			return false;

		final LabeledIntTreeMap lowerCaseValueMap = eAC.getLowerCaseValueMap().get(nC.getALabel());
		if (lowerCaseValueMap == null || lowerCaseValueMap.isEmpty())
			return false;

		final LabeledALabelIntTreeMap CZAllValueMap = eCZ.getAllUpperCaseAndLabeledValuesMaps();
		if (CZAllValueMap.isEmpty())
			return false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "rG2: start.");
			}
		}

		// for (ALabel c : lowerCaseValueMap.keySet()) {
		for (Entry<Label> entryLowerCase : lowerCaseValueMap.entrySet()) {
			Label alpha = entryLowerCase.getKey();
			int u = entryLowerCase.getIntValue();
			for (final ALabel aleph : CZAllValueMap.keySet()) {
				LabeledIntTreeMap czValuesMap = CZAllValueMap.get(aleph);
				if (czValuesMap == null)
					continue;
				if (aleph.contains(nC.getALabel())) {
					continue;// Rule condition: upper case label cannot be equal or contain c name
				}
				boolean emptyAleph = aleph.isEmpty();
				for (Object2IntMap.Entry<Label> entryCZ : czValuesMap.entrySet()) {// entrySet read-only
					final int v = entryCZ.getIntValue();
					final int sum = Constants.sumWithOverflowCheck(v, u);
					if (sum >= 0)
						continue;

					final Label beta = entryCZ.getKey();
					final Label alphaBeta = beta.conjunctionExtended(alpha);
					if (alphaBeta == null)
						continue;

					final int oldValue = (emptyAleph) ? eAZ.getValue(alphaBeta) : eAZ.getUpperCaseValue(alphaBeta, aleph);

					if (oldValue != Constants.INT_NULL && oldValue <= sum) {
						continue;
					}
					String logMsg = null;
					if (Debug.ON) {
						final String oldAX = eAZ.toString();
						logMsg = "rG2 applied to edge " + oldAX + ":\npartic: " + nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(aleph, v, beta)
								+ "--- " + nC.getName() + " <---" + CSTNU.lowerCaseValueAsString(nC.getALabel(), u, alpha) + "--- "
								+ nA.getName()
								+ "\nresult: " + nZ.getName()
								+ " <---" + CSTNU.upperCaseValueAsString(aleph, sum, alphaBeta) + "--- " + nA.getName() + "; oldValue: "
								+ Constants.formatInt(oldValue);
					}

					boolean localApp = (emptyAleph) ? eAZ.mergeLabeledValue(alphaBeta, sum) : eAZ.mergeUpperCaseValue(alphaBeta, aleph, sum);

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

					if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nA, nZ, eAZ, this.checkStatus)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.INFO)) {
								LOG.log(Level.INFO, logMsg);
							}
						}
						return true;
					}
				}
			}
		}
		// }
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "rG2: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * rG4<br>
	 * Overrides {@link CSTNU#zLabeledLetterRemovalRule(LabeledNode, it.univr.di.cstnu.graph.CSTNUEdge)}
	 * considering guarded links instead of contingent ones.<br>
	 * 
	 * <pre>
	 * Y ---(v,ℵ,β)---&gt; Z &lt;---(w,ℵ1,αl)--- A ---(x',c,l)---&gt; C 
	 *                                             &lt;---(-x,◇,l)---
	 * adds 
	 *         
	 * Y ---(m,ℵℵ1,β*(αl))---&gt; Z
	 *  
	 * if m = max(v, w + (-x))
	 * </pre>
	 * 
	 * @param nY none
	 * @param eYZ  none
	 * @return true if the reduction has been applied.
	 */
	boolean rG4(final LabeledNode nY, final CSTNPSUEdge eYZ) {
		boolean ruleApplied = false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLR: start.");
		}
		LabeledNode Z = this.g.getZ();
		for (final ALabel aleph : eYZ.getUpperCaseValueMap().keySet()) {
			LabeledIntTreeMap YZvaluesMap = eYZ.getUpperCaseValueMap().get(aleph);
			if (YZvaluesMap == null)
				continue;
			for (Label beta : YZvaluesMap.keySet()) {
				int v = eYZ.getUpperCaseValue(beta, aleph);
				if (v == Constants.INT_NULL)
					continue;
				for (ALetter nodeLetter : aleph) {
					LabeledNode nC = this.g.getNode(nodeLetter.name);
					if (nY == nC) // Z is the activation time point!
						continue;
					LabeledNode nA = this.getActivationNode(nC);
					if (nA == Z)
						continue;
					CSTNPSUEdge AC = this.getLowerContingentLink(nC);
					Label guardedLinkLabel = nC.getLabel().conjunction(nA.getLabel());
					int lowerCaseEntry = AC.getLowerCaseValue(guardedLinkLabel, nC.getALabel());
					if (lowerCaseEntry == Constants.INT_NULL)
						continue;
					CSTNPSUEdge CA = this.g.findEdge(nC, nA);
					int x = CA.getValue(guardedLinkLabel);// guarded link, x must be the lower bound, not the lower guard lowerCaseEntry.getValue();
					if (x == Constants.INT_NULL)
						continue;
					CSTNPSUEdge AZ = this.g.findEdge(nA, Z);

					for (ALabel aleph1 : AZ.getAllUpperCaseAndLabeledValuesMaps().keySet()) {
						if (aleph1.contains(nodeLetter))
							continue;
						LabeledIntTreeMap AZAlephMap = AZ.getAllUpperCaseAndLabeledValuesMaps().get(aleph1);
						if (AZAlephMap == null)
							continue;
						for (Entry<Label> entryAZ : AZAlephMap.entrySet()) {
							final Label alpha = entryAZ.getKey();
							final int w = entryAZ.getIntValue();

							if (!alpha.subsumes(guardedLinkLabel))
								continue;// rule condition

							int m = Math.max(v, w + x);// lower bound x of a guarded link is already negative!

							ALabel alephAleph1 = aleph.conjunction(aleph1);
							alephAleph1.remove(nodeLetter);

							Label alphaBeta = alpha.conjunctionExtended(beta);

							final int oldValue = (Debug.ON) ? eYZ.getUpperCaseValue(alphaBeta, alephAleph1) : -1;
							final String oldYZ = (Debug.ON) ? eYZ.toString() : "";

							boolean mergeStatus = (alephAleph1.isEmpty()) ? eYZ.mergeLabeledValue(alphaBeta, m)
									: eYZ.mergeUpperCaseValue(alphaBeta, alephAleph1, m);

							if (mergeStatus) {
								ruleApplied = true;
								getCheckStatus().letterRemovalRuleCalls++;
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										LOG.log(Level.FINER, "CSTNPSU rG4 applied to edge " + oldYZ + ":\n" + "partic: "
												+ nY.getName() + "---" + CSTNU.upperCaseValueAsString(aleph, v, beta) + "---> Z <---"
												+ CSTNU.upperCaseValueAsString(aleph1, w, alpha) + "--- " + nA.getName()
												+ "---" + CSTNU.lowerCaseValueAsString(nC.getALabel(), x, guardedLinkLabel) + "---> " + nodeLetter
												+ "\nresult: " + nY.getName() + "---" + CSTNU.upperCaseValueAsString(alephAleph1, m, alphaBeta) + "---> Z"
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
				LOG.log(Level.FINER, "rG4: end.");
		}
		return ruleApplied;
	}

	/**
	 * Implements the CSTNU zqR0 rule assuming instantaneous reaction and a streamlined network.<br>
	 * <b>This differs from {@link CSTN#labelModificationR0qR0(LabeledNode, LabeledNode, it.univr.di.cstnu.graph.CSTNEdge)}
	 * in the checking also upper case value</b>
	 * 
	 * @param nObs the observation node
	 * @param ePZ the edge connecting P? ---&gt; Z
	 * @return true if the rule has been applied one time at least.
	 */
	boolean rM1(final LabeledNode nObs, final CSTNPSUEdge ePZ) {

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
		LabeledNode Z = this.g.getZ();
		for (final ALabel aleph : mapOfAllValues.keySet()) {
			boolean alephNOTEmpty = !aleph.isEmpty();
			for (Label alpha : mapOfAllValues.get(aleph).keySet()) {
				if (alpha == null || !alpha.contains(p)) {
					continue;
				}
				final int w = (alephNOTEmpty) ? ePZ.getUpperCaseValue(alpha, aleph) : ePZ.getValue(alpha);
				// It is necessary to re-check if the value is still present. Verified that it is necessary on Nov, 26 2015
				if (w == Constants.INT_NULL || mainConditionForSkippingInR0qR0(w)) {// Table 1 ICAPS paper
					continue;
				}

				final Label alphaPrime = makeAlphaPrime(Z, nObs, p, alpha);
				if (alphaPrime == null) {
					continue;
				}

				// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
				String logMessage = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						logMessage = "zqR0 simplifies a label of edge " + ePZ.getName()
								+ ":\nsource: " + nObs.getName() + " ---" + CSTNU.upperCaseValueAsString(aleph, w, alpha) + "---> " + Z.getName()
								+ "\nresult: " + nObs.getName() + " ---" + CSTNU.upperCaseValueAsString(aleph, w, alphaPrime) + "---> " + Z.getName();
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
	 * Implements the CSTNU qR3* rule assuming instantaneous reaction and a streamlined network.<br>
	 * <b>This differs from {@link CSTNIR3RwoNodeLabels#labelModificationR3qR3(LabeledNode, LabeledNode, it.univr.di.cstnu.graph.CSTNEdge)}
	 * in the checking also upper case value.</b>
	 * 
	 * @param nS node
	 * @param eSZ CSTNPSUEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	boolean rM2(final LabeledNode nS, final CSTNPSUEdge eSZ) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "rM2: start.");
			}
		}
		boolean ruleApplied = false;
		LabeledNode Z = this.g.getZ();
		ObjectList<CSTNPSUEdge> Obs2ZEdges = this.getEdgeFromObserversToNode(Z);

		LabeledALabelIntTreeMap allValueMapSZ = eSZ.getAllUpperCaseAndLabeledValuesMaps();
		if (allValueMapSZ.isEmpty())
			return false;

		final ObjectSet<Label> SZLabelSet = eSZ.getLabeledValueMap().keySet();
		SZLabelSet.addAll(eSZ.getUpperCaseValueMap().labelSet());// it adds all the labels from upper-case values!

		Label allLiteralsSZ = Label.emptyLabel;
		for (Label l : SZLabelSet) {
			allLiteralsSZ = allLiteralsSZ.conjunctionExtended(l);
		}

		// check each edge from an observator to Z.
		for (final CSTNPSUEdge eObsZ : Obs2ZEdges) {
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
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryObsZ : allValueMapObsZ.get(aleph1).entrySet()) {// entrySet read-only
					final int w = entryObsZ.getIntValue();
					if (mainConditionForSkippingInR3qR3(w, Z)) { // Table 1 ICAPS
						continue;
					}

					final Label gamma = entryObsZ.getKey();
					for (final ALabel aleph : allValueMapSZ.keySet()) {
						for (Label SZLabel : allValueMapSZ.get(aleph).keySet()) {
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
							final int max = newValueInR3qR3(v, w);
							ALabel newUpperCaseLetter = aleph.conjunction(aleph1);

							ruleApplied = (newUpperCaseLetter.isEmpty()) ? ruleApplied = eSZ.mergeLabeledValue(newLabel, max)
									: eSZ.mergeUpperCaseValue(newLabel, newUpperCaseLetter, max);

							if (ruleApplied) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										LOG.log(Level.FINER, "rM2 adds a labeled value to edge " + eSZ.getName() + ":\n"
												+ "source: " + nObs.getName() + " ---" + CSTNU.upperCaseValueAsString(aleph1, w, gamma) + "---> "
												+ Z.getName()
												+ " <---" + CSTNU.upperCaseValueAsString(aleph, v, SZLabel) + "--- " + nS.getName()
												+ "\nresult: add " + Z.getName() + " <---"
												+ CSTNU.upperCaseValueAsString(newUpperCaseLetter, max, newLabel)
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
				LOG.log(Level.FINER, "rM2: end.");
			}
		}
		return ruleApplied;
	}

	/**
	 * Apply rules rG5 and rG6 and rG7 of Table 2 paper submitted to CP20.<br>
	 *
	 * <pre>
	 * 1) rG5
	 *      v,ד,β              u,◇,α
	 * Z ------------&gt; X ------------&gt; Y
	 * adds
	 *      u+v,ד,α★β
	 * Z ------------------------------&gt; Y
	 * when u+v &ge; 0. If (u &lt; 0), α★β must be αβ
	 * ד can be empty.
	 * 
	 * 2) rG6
	 *     v,ד,β               u,y,α
	 * Z ------------&gt; X ------------&gt; Y
	 * adds
	 *     u+v,yד,α★β
	 * Z ------------------------------&gt; Y
	 * when u+v &ge; 0.
	 * ד can be empty. If not empty, y not in ד.
	 * 
	 * 2) rG7
	 *     v,ד,β               -u,X,α
	 * Z ------------&gt; X ------------&gt; Y
	 * adds
	 *     -u+v,xד,α★β
	 * Z ------------------------------&gt; Y
	 * when u+v &ge; 0, ד can be empty and X not in ד
	 * </pre>
	 * 
	 * @param nX node 
	 * @param nY node 
	 * @param nZ node
	 * @param eZX CANNOT BE NULL
	 * @param eXY CANNOT BE NULL
	 * @param eZY CANNOT BE NULL
	 * @return true if a reduction is applied at least
	 */
	boolean rG5rG6rG7(final LabeledNode nZ, final LabeledNode nX, final LabeledNode nY, final CSTNPSUEdge eZX, final CSTNPSUEdge eXY, final CSTNPSUEdge eZY) {
		boolean ruleApplied = false;
		String logMsg = null;
		LabeledALabelIntTreeMap ZXLowerCAndLabeledValueMap = eZX.getAllLowerCaseAndLabeledValuesMaps();
		if (ZXLowerCAndLabeledValueMap.isEmpty())
			return false;

		final Set<Object2IntMap.Entry<Label>> XYLabeledValueMap = eXY.getLabeledValueSet();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG5rG6rG7: start.");
		}

		// 1) rG5
		for (final Object2IntMap.Entry<Label> entryXY : XYLabeledValueMap) {
			final Label alpha = entryXY.getKey();
			final int u = entryXY.getIntValue();

			for (ALabel dalet : ZXLowerCAndLabeledValueMap.keySet()) {
				for (Object2IntMap.Entry<Label> entryZX : ZXLowerCAndLabeledValueMap.get(dalet).entrySet()) {// entrySet read-only
					final Label beta = entryZX.getKey();
					Label alphaBeta;
					alphaBeta = (u >= 0) ? alpha.conjunctionExtended(beta) : alpha.conjunction(beta);
					if (alphaBeta == null)
						continue;
					final int v = entryZX.getIntValue();
					int sum = Constants.sumWithOverflowCheck(u, v);
					final int oldValue = (dalet.isEmpty()) ? eZY.getValue(alphaBeta) : eZY.getLowerCaseValue(alphaBeta, dalet);
					if (Debug.ON) {
						final String oldZY = eZY.toString();
						logMsg = "CSTNPSU rG5 applied to edge " + oldZY + ":\n" + "partic: "
								+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(dalet, v, beta) + "---> " + nX.getName() + " ---"
								+ CSTNU.lowerCaseValueAsString(ALabel.emptyLabel, u, alpha) + "---> " + nY.getName()
								+ "\nresult: "
								+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(dalet, sum, alphaBeta) + "---> " + nY.getName()
								+ "; old value: " + Constants.formatInt(oldValue);
					}
					if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nZ, nY, eZY, this.checkStatus)) {
						if (Debug.ON) {
							if (LOG.isLoggable(Level.INFO)) {
								LOG.log(Level.INFO, logMsg);
							}
						}
						return true;
					}

					if ((sum >= 0 && nZ == nY) || ((oldValue != Constants.INT_NULL) && (sum >= oldValue))) {
						// value is stored only if it is smaller than the current one.
						continue;
					}

					boolean mergeStatus = (dalet.isEmpty()) ? eZY.mergeLabeledValue(alphaBeta, sum) : eZY.mergeLowerCaseValue(alphaBeta, dalet, sum);

					if (mergeStatus) {
						ruleApplied = true;
						// if (aleph.isEmpty()) {
						// this.checkStatus.labeledValuePropagationCalls++;
						// } else {
						getCheckStatus().zEsclamationRuleCalls++;
						// }
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, logMsg);
							}
						}
					}
				}
			}
		}

		// 2) rG6
		if (nY.isContingent() && this.getActivationNode(nY) == nX) {

			LabeledIntTreeMap eXYLowerCaseValues = eXY.getLowerCaseValueMap().get(nY.getALabel());
			if (eXYLowerCaseValues != null) {
				for (Object2IntMap.Entry<Label> entryXY : eXYLowerCaseValues.entrySet()) {
					final Label alpha = entryXY.getKey();
					final int u = entryXY.getIntValue();
					for (final ALabel dalet : ZXLowerCAndLabeledValueMap.keySet()) {
						if (dalet.contains(nY.getALabel()))
							continue;
						for (Object2IntMap.Entry<Label> entryZX : ZXLowerCAndLabeledValueMap.get(dalet).entrySet()) {// It should be one!
							final Label beta = entryZX.getKey();
							Label alphaBeta = alpha.conjunctionExtended(beta);
							if (alpha.isEmpty())
								alphaBeta = beta;
							if (alphaBeta == null)
								continue;
							final int v = entryZX.getIntValue();
							int sum = Constants.sumWithOverflowCheck(u, v);
							final ALabel newXAleph = nY.getALabel().conjunction(dalet);
							final int oldValue = eZY.getLowerCaseValue(alphaBeta, newXAleph);

							if (Debug.ON) {
								final String oldZY = eZY.toString();
								logMsg = "CSTNPSU rG6 applied to edge " + oldZY + ":\n" + "partic: "
										+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(dalet, v, beta) + "---> " + nX.getName() + " ---"
										+ CSTNU.lowerCaseValueAsString(nY.getALabel(), u, alpha) + "---> " + nY.getName()
										+ "\nresult: "
										+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(newXAleph, sum, alphaBeta) + "---> " + nY.getName()
										+ "; old value: " + Constants.formatInt(oldValue);
							}
							if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nZ, nY, eZY, this.checkStatus)) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.INFO)) {
										LOG.log(Level.INFO, logMsg);
									}
								}
								return true;
							}

							if ((sum >= 0 && nZ == nY) || (oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
								continue;
							}

							boolean mergeStatus = eZY.mergeLowerCaseValue(alphaBeta, newXAleph, sum);
							if (mergeStatus) {
								ruleApplied = true;
								getCheckStatus().zEsclamationRuleCalls++;
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										LOG.log(Level.FINER, logMsg);
									}
								}

								// FIXME
								// clean redundant aleph
								// if (newXAleph.size() > 1) {
								// ALabel[] alephSet = eZY.getLowerCaseValueMap().keySet().toArray(new ALabel[0]);
								// if (alephSet.length == 0)
								// continue;
								// for (ALabel aleph1 : alephSet) {
								// if (aleph1.contains(nY.getAlabel()))
								// continue;
								// if (newXAleph.contains(aleph1)) {
								// if (Debug.ON) {
								// final String oldZY = eZY.toString();
								// logMsg = "CSTNPSU rG7 removal applied to edge " + oldZY + ":\n" + "partic: "
								// + "Removing " + aleph1;
								// }
								// eZY.getLowerCaseValueMap().remove(aleph1);
								// if (Debug.ON) {
								// logMsg += "\nNew edge " + eZY;
								// if (LOG.isLoggable(Level.FINER)) {
								// LOG.log(Level.FINER, logMsg);
								// }
								// }
								// continue;
								// }
								// }
								// }
							}
						}
					}
				}
			}
		}

		// 2) rG7
		if (nX.isContingent() && this.getActivationNode(nX) == nY) {
			LabeledIntTreeMap eXYUpperCaseValue = eXY.getUpperCaseValueMap().get(nX.getALabel());
			if (eXYUpperCaseValue != null) {
				for (Object2IntMap.Entry<Label> entryXY : eXYUpperCaseValue.entrySet()) {// entrySet read-only
					final Label alpha = entryXY.getKey();
					final int u = entryXY.getIntValue();
					for (final ALabel dalet : ZXLowerCAndLabeledValueMap.keySet()) {
						if (dalet.contains(nX.getALabel()))
							continue;
						for (Object2IntMap.Entry<Label> entryZX : ZXLowerCAndLabeledValueMap.get(dalet).entrySet()) {// it should be only one!
							final Label beta = entryZX.getKey();
							Label alphaBeta = alpha.conjunctionExtended(beta);
							if (alphaBeta == null)
								continue;
							final int v = entryZX.getIntValue();
							int sum = Constants.sumWithOverflowCheck(u, v);
							// ALabel newDalet = dalet.conjunction(nX.getAlabel());
							final int oldValue = eZY.getLowerCaseValue(alphaBeta, dalet);
							if (Debug.ON) {
								final String oldZY = eZY.toString();
								logMsg = "CSTNPSU rG7 applied to edge " + oldZY + ":\n" + "partic: "
										+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(dalet, v, beta) + "---> " + nX.getName() + " ---"
										+ CSTNU.upperCaseValueAsString(nX.getALabel(), u, alpha) + "---> " + nY.getName()
										+ "\nresult: "
										+ nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(dalet, sum, alphaBeta) + "---> " + nY.getName()
										+ "; old value: " + Constants.formatInt(oldValue);
							}

							if (CSTNU.checkAndManageIfNewLabeledValueIsANegativeLoop(sum, nZ, nY, eZY, this.checkStatus)) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.INFO)) {
										LOG.log(Level.INFO, logMsg);
									}
								}
								return true;
							}

							if ((sum >= 0 && nZ == nY) || (oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
								continue;
							}

							boolean mergeStatus = (dalet.isEmpty()) ? eZY.mergeLabeledValue(alphaBeta, sum)
									: eZY.mergeLowerCaseValue(alphaBeta, dalet, sum);

							if (mergeStatus) {
								ruleApplied = true;
								getCheckStatus().zEsclamationRuleCalls++;
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
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "rG6rG7rG8: end.");
			}
		}
		return ruleApplied;
	}

	// /**
	// * rGSimplify
	// *
	// * <pre>
	// * v,ℵ,β
	// * Z &lt;------------ C
	// * adds
	// * v,◇,β
	// * Z &lt;------------ C
	// * when C != ℵ
	// * </pre>
	// *
	// * @param nC must be a contingent time point
	// * @param nZ
	// * @param eCZ CANNOT BE NULL
	// * @return true if a reduction is applied at least
	// */
	// boolean rGSimplify(final LabeledNode nC, final LabeledNode nZ, final CSTNPSUEdge eCZ) {
	// boolean ruleApplied = false;
	// ALabel nCALabel = nC.getAlabel();
	// ALabel[] allAleph = eCZ.getUpperCaseValueMap().keySet().toArray(new ALabel[0]);
	// if (nCALabel == null || nCALabel.isEmpty() || allAleph.length == 0)
	// return false;
	//
	// for (ALabel aleph : allAleph) {
	// if (aleph.contains(nCALabel))
	// continue;
	// for (Object2IntMap.Entry<Label> entryCZ : eCZ.getUpperCaseValueMap().get(aleph).entrySet().toArray(new Object2IntMap.Entry[1])) {
	// final Label alphap = entryCZ.getKey();
	// final int u = entryCZ.getIntValue();
	// boolean added = eCZ.mergeLabeledValue(alphap, u);
	// if (added) {
	// ruleApplied = added;
	// if (Debug.ON) {
	// final String oldCZ = eCZ.toString();
	// String logMsg = "CSTNPSU rG5 applied to edge " + oldCZ + ":\n" + "partic: "
	// + nZ.getName() + " <---" + CSTNU.upperCaseValueAsString(aleph, u, alphap) + "--- "
	// + nC.getName()
	// + "\nresult: "
	// + nZ.getName() + " <---" + pairAsString(alphap, u) + "--- " + nC.getName();
	// if (LOG.isLoggable(Level.FINER)) {
	// LOG.log(Level.FINER, logMsg);
	// }
	// }
	// }
	// }
	// }
	// // It is always done because labeled value can be present form the original network
	// LabeledNode nA = this.activationNode.get(nC);
	// updateGuardedBoundsUsingZ(nC, nZ, nA);
	// return ruleApplied;
	// }
	//
	// /**
	// * rG8
	// *
	// * <pre>
	// * v,ℵ,β
	// * Z ------------&gt; C
	// * adds
	// * v,◇,β
	// * Z ------------&gt; C
	// * when c not in ℵ
	// * </pre>
	// *
	// * @param nC must be a contingent time point
	// * @param nZ
	// * @param eZC CANNOT BE NULL
	// * @return true if a reduction is applied at least
	// */
	// boolean rGSimplify1(final LabeledNode nC, final LabeledNode nZ, final CSTNPSUEdge eZC) {
	// boolean ruleApplied = false;
	// ALabel nCALabel = nC.getAlabel();
	// ALabel[] allAleph = eZC.getLowerCaseValueMap().keySet().toArray(new ALabel[0]);
	// if (nCALabel == null || nCALabel.isEmpty() || allAleph.length == 0)
	// return false;
	//
	// for (ALabel aleph : allAleph) {
	// if (aleph.contains(nCALabel))
	// continue;
	// for (Object2IntMap.Entry<Label> entryCZ : eZC.getLowerCaseValueMap().get(aleph).entrySet().toArray(new Object2IntMap.Entry[1])) {
	// final Label alphap = entryCZ.getKey();
	// final int u = entryCZ.getIntValue();
	// boolean added = eZC.mergeLabeledValue(alphap, u);
	// if (added) {
	// ruleApplied = added;
	// if (Debug.ON) {
	// final String oldZC = eZC.toString();
	// String logMsg = "CSTNPSU rG8 applied to edge " + oldZC + ":\n" + "partic: "
	// + nZ.getName() + " ---" + CSTNU.lowerCaseValueAsString(aleph, u, alphap) + "---> "
	// + nC.getName()
	// + "\nresult: "
	// + nZ.getName() + " ---" + pairAsString(alphap, u) + "---> " + nC.getName();
	// if (LOG.isLoggable(Level.FINER)) {
	// LOG.log(Level.FINER, logMsg);
	// }
	// }
	// }
	// }
	// }
	// // It is always done because labeled value can be present form the original network
	// LabeledNode nA = this.activationNode.get(nC);
	// updateGuardedBoundsUsingZ(nA, nZ, nC);
	// return ruleApplied;
	// }

	// /**
	// * Applies the propagation rule only on labeled values in the triangle
	// *
	// * <pre>
	// * nX ---(u, alpha)---> Z ---(v, beta)---> nY
	// * adds
	// * nX ---(u+v, alphaBeta)---> nY
	// * </pre>
	// *
	// * @param nX
	// * @param nZ
	// * @param nY
	// */
	// void updateGuardedBoundsUsingZ(LabeledNode nX, LabeledNode nZ, LabeledNode nY) {
	// CSTNPSUEdge eXZ = this.g.findEdge(nX, nZ),
	// eXY = this.g.findEdge(nX, nY),
	// eZY = this.g.findEdge(nZ, nY);
	// for (Entry<Label> entryAZ : eXZ.getLabeledValueSet()) {
	// Label alpha = entryAZ.getKey();
	// int u = entryAZ.getIntValue();
	// for (Entry<Label> entryZY : eZY.getLabeledValueSet()) {
	// Label beta = entryZY.getKey();
	// int v = entryZY.getIntValue();
	//
	// Label alphaBeta = alpha.conjunction(beta);
	// if (alphaBeta == null)
	// continue;
	// int sum = Constants.sumWithOverflowCheck(v, u);
	// boolean added = eXY.mergeLabeledValue(alphaBeta, sum);
	// if (Debug.ON && added) {
	// final String oldXY = eXY.toString();
	// String logMsg = "CSTNPSU update guarded bounds of " + oldXY + ":\n" + "partic: "
	// + nX.getName() + " ---" + pairAsString(alpha, u) + "---> " + nZ.getName() + " ---" + pairAsString(beta, v) + "---> "
	// + nY.getName()
	// + "\nresult: "
	// + nX.getName() + " ---" + pairAsString(alphaBeta, sum) + "---> " + nY.getName();
	// if (LOG.isLoggable(Level.FINER)) {
	// LOG.log(Level.FINER, logMsg);
	// }
	// }
	// }
	// }
	// }

	/**
	 * Update lower bound of a guarded range.
	 * 
	 * <pre>
	 * nC ---(u, ℵ, alpha)---&gt; Z ---(v, ד, beta)---&gt; nA
	 * adds
	 * nC ---(u+v, alphaBeta)---&gt; nA
	 * 
	 * ℵ and/or ד can be empty and cannot contain common names or nC.
	 * Alpha and beta must be consistent or, if one has unknown literals, the other must be empty.
	 * </pre>
	 * 
	 * @param nC contingent node 
	 * @param nZ zero node a
	 * @param nA activation node
	 * @param eCZ edge 
	 * @param eZA edge
 	 * @param eAC edge
	 * @param eCA edge
	 * @return true if the rule has been applied.
	 */
	static boolean rG9(LabeledNode nC, LabeledNode nZ, LabeledNode nA, CSTNPSUEdge eCZ, CSTNPSUEdge eZA, CSTNPSUEdge eAC, CSTNPSUEdge eCA) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG9: start.");
		}
		String logMsg;
		boolean ruleApplied = false;
		int lowerGuard = eAC.getLowerCaseValue(Label.emptyLabel, nC.getALabel());
		int upperGuard = eCA.getUpperCaseValue(Label.emptyLabel, nC.getALabel());
		if (lowerGuard > -upperGuard) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "Guarded link " + nA.getName() + "--" + nC.getName() + " is a partial shrinkable guarded link. Rule does not apply.");
			}
			return false;
		}

		LabeledALabelIntTreeMap eCZMaps = eCZ.getAllUpperCaseAndLabeledValuesMaps();
		LabeledALabelIntTreeMap eZAMaps = eZA.getAllLowerCaseAndLabeledValuesMaps();
		for (ALabel aleph : eCZMaps.keySet()) {
			if (aleph.contains(nC.getALabel()))
				continue;
			for (Entry<Label> entryCZ : eCZMaps.get(aleph).entrySet()) {
				Label alpha = entryCZ.getKey();
				int u = entryCZ.getIntValue();
				for (ALabel dalet : eZAMaps.keySet()) {
					if (dalet.contains(nC.getALabel()) || !aleph.intersect(dalet).isEmpty())
						continue;
					for (Entry<Label> entryZA : eZAMaps.get(dalet).entrySet()) {
						Label beta = entryZA.getKey();
						int v = entryZA.getIntValue();
						Label alphaBeta = alpha.conjunction(beta);
						if (alphaBeta == null && (!alpha.isEmpty() && !beta.isEmpty()))
							continue;
						int sum = Constants.sumWithOverflowCheck(v, u);
						if (Debug.ON) {
							final String oldCA = eCA.toString();
							logMsg = "CSTNPSU rG9 on " + oldCA + ":\n" + "partic: "
									+ nC.getName() + " ---" + CSTNU.upperCaseValueAsString(aleph, u, alpha) + "---> " + nZ.getName() + " ---"
									+ CSTNU.lowerCaseValueAsString(dalet, v, beta) + "---> " + nA.getName()
									+ "\nresult: "
									+ nC.getName() + " ---" + pairAsString(Label.emptyLabel, sum) + "---> " + nA.getName();
						}
						boolean added = eCA.mergeLabeledValue(Label.emptyLabel, sum);

						if (added) {
							ruleApplied = added;
							if (Debug.ON) {
								LOG.log(Level.FINER, logMsg);
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG9: end.");
		}
		return ruleApplied;
	}

	/**
	 * Update upper bound of a guarded range.
	 * 
	 * <pre>
	 * nA ---(u, ℵ, alpha)---&gt; Z ---(v, ד, beta)---&gt; nC
	 * adds
	 * nA ---(u+v, ⊡)---&gt; nC
	 * 
	 * ℵ and/or ד can be empty and cannot contain common names or nC.
	 * Alpha and beta must be consistent or, if one has unknown literals, the other must be empty.
	 * </pre>
	 * 
	 * @param nA activation node
	 * @param nZ zero node 
	 * @param nC contingent node
	 * @param eAZ edge 
	 * @param eZC edge 
	 * @param eAC edge 
	 * @param eCA edge
	 * @return true if the rule has been applied.
	 */
	static boolean rG8(LabeledNode nA, LabeledNode nZ, LabeledNode nC, CSTNPSUEdge eAZ, CSTNPSUEdge eZC, CSTNPSUEdge eAC, CSTNPSUEdge eCA) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG8: start.");
		}
		String logMsg = "";
		boolean ruleApplied = false;
		int lowerGuard = eAC.getLowerCaseValue(Label.emptyLabel, nC.getALabel());
		int upperGuard = eCA.getUpperCaseValue(Label.emptyLabel, nC.getALabel());
		if (lowerGuard > -upperGuard) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "Guarded link " + nA.getName() + "--" + nC.getName() + " is a partial shrinkable guarded link. Rule does not apply.");
			}
			return false;
		}
		LabeledALabelIntTreeMap eZCMaps = eZC.getAllLowerCaseAndLabeledValuesMaps();
		LabeledALabelIntTreeMap eAZMaps = eAZ.getAllUpperCaseAndLabeledValuesMaps();
		for (ALabel dalet : eZCMaps.keySet()) {
			if (dalet.contains(nC.getALabel()))
				continue;
			for (Entry<Label> entryCZ : eZCMaps.get(dalet).entrySet()) {
				Label beta = entryCZ.getKey();
				int v = entryCZ.getIntValue();
				for (ALabel aleph : eAZMaps.keySet()) {
					if (aleph.contains(nC.getALabel()) || !aleph.intersect(dalet).isEmpty())
						continue;
					for (Entry<Label> entryZA : eAZMaps.get(aleph).entrySet()) {
						Label alpha = entryZA.getKey();
						int u = entryZA.getIntValue();
						Label alphaBeta = alpha.conjunction(beta);
						if (alphaBeta == null && (!alpha.isEmpty() && !beta.isEmpty()))
							continue;
						int sum = Constants.sumWithOverflowCheck(v, u);
						if (Debug.ON) {
							final String oldAC = eAC.toString();
							logMsg = "CSTNPSU rG8 on " + oldAC + ":\n" + "partic: "
									+ nA.getName() + " ---" + CSTNU.upperCaseValueAsString(aleph, u, alpha) + "---> " + nZ.getName() + " ---"
									+ CSTNU.lowerCaseValueAsString(dalet, v, beta) + "---> " + nC.getName()
									+ "\nresult: "
									+ nA.getName() + " ---" + pairAsString(Label.emptyLabel, sum) + "---> " + nC.getName();
						}
						boolean added = eAC.mergeLabeledValue(Label.emptyLabel, sum);

						if (added) {
							ruleApplied = added;
							if (Debug.ON) {
								LOG.log(Level.FINER, logMsg);
							}
						}
					}
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "rG8: end.");
		}
		return ruleApplied;
	}

	/**
	 * <pre>
	 * A ---(u, alpha)---&gt; C ---(v, beta)---&gt; A
	 * 
	 * If u+v &lt; 0, raise uncontrollability of the guarded link.
	 * </pre>
	 * 
	 * @param nA activation node 
	 * @param nC contingent node 
	 * @param eAC edge 
	 * @param eCA edge
	 * @param checkStatus1 status of the checking
	 * @return true if the guarded link is uncontrollable.
	 */
	static boolean checkBoundGuarded(LabeledNode nA, LabeledNode nC, CSTNPSUEdge eAC, CSTNPSUEdge eCA, CSTNCheckStatus checkStatus1) {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "checkBoundGuarded: start.");
		}
		String logMsg = "";

		for (Entry<Label> entryAC : eAC.getLabeledValueSet()) {
			Label alpha = entryAC.getKey();
			int u = entryAC.getIntValue();
			for (Entry<Label> entryCA : eCA.getLabeledValueSet()) {
				Label beta = entryCA.getKey();
				int v = entryCA.getIntValue();
				Label alphaBeta = alpha.conjunction(beta);
				int sum = Constants.sumWithOverflowCheck(v, u);
				if (Debug.ON) {
					logMsg = "CSTNPSU checkBoundGuarded:\n" + "partic: "
							+ nA.getName() + " ---" + pairAsString(alpha, u) + "---> " + nC.getName() + " ---"
							+ pairAsString(beta, v) + "---> " + nA.getName()
							+ "\nresult: "
							+ nC.getName() + " ---" + pairAsString(alphaBeta, sum) + "---> " + nC.getName();
				}
				boolean found = sum < 0;

				if (found) {
					if (Debug.ON) {
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, logMsg);
						}
					}
					checkStatus1.consistency = false;
					checkStatus1.finished = true;
					checkStatus1.negativeLoopNode = nC;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER))
							LOG.log(Level.FINER, "checkBoundGuarded: end.");
					}
					return true;
				}
			}
		}
		if (LOG.isLoggable(Level.FINER))
			LOG.log(Level.FINER, "checkBoundGuarded: end.");
		return false;
	}
}
