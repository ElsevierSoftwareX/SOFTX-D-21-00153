package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.cstnu.graph.TNGraphMLWriter;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.ALabel;
import it.univr.di.labeledvalue.ALabelAlphabet.ALetter;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMapSupplier;
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Simple class to represent and check Conditional Simple Temporal Network with Partial Shrinkable Uncertainty (CSTNPSU).
 * In this class, contingent link are, in real, guarded ones.
 * Therefore, the input graph HAS to have defined guarded links explicitly: upper bound edge must contain also the lower guard as lower case contingent value,
 * and lower bound edge must contain also the upper case negative value.
 * <br>
 * This class is an extension of {@link CSTNU} class.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNPSU extends CSTNU {
	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger("CSTNPSU");

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final String VERSIONandDATE = "Version 1.0 - Feb, 21 20178";

	/**
	 * Reads a CSTNPSU file and checks it.
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

		final CSTNPSU cstnpsu = new CSTNPSU();

		if (!cstnpsu.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Parameters ok!");
		}
		if (cstnpsu.versionReq) {
			System.out.println("CSTNPSU " + CSTNPSU.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017,2018, Roberto Posenato");
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "Loading graph...");
		}
		TNGraphMLReader<CSTNUEdge> graphMLReader = new TNGraphMLReader<>(cstnpsu.fInput, EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS,
				LabeledIntMapSupplier.DEFAULT_LABELEDINTMAP_CLASS);
		cstnpsu.setG(graphMLReader.readGraph());
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
			final TNGraphMLWriter graphWriter = new TNGraphMLWriter(new StaticLayout<>(cstnpsu.g));
			try {
				graphWriter.save(cstnpsu.g, new PrintWriter(cstnpsu.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * Default constructor, package use only!
	 */
	CSTNPSU() {
		super();
	}

	/**
	 * Constructor for CSTNPSU
	 * 
	 * @param graph TNGraph to check
	 */
	public CSTNPSU(TNGraph<CSTNUEdge> graph) {
		super(graph);// Remember that super(g) calls CSTNU.setG(g)!
	}

	/**
	 * Constructor for CSTNPSU
	 * 
	 * @param graph TNGraph to check
	 * @param givenTimeOut timeout for the check in seconds
	 */
	public CSTNPSU(TNGraph<CSTNUEdge> graph, int givenTimeOut) {
		super(graph, givenTimeOut);
	}

	/**
	 * CSTNPSU
	 * Constructor for CSTNU
	 * 
	 * @param graph TNGraph to check
	 * @param givenTimeOut timeout for the check in seconds
	 * @param givenPropagationOnlyToZ
	 */
	public CSTNPSU(TNGraph<CSTNUEdge> graph, int givenTimeOut, boolean givenPropagationOnlyToZ) {
		super(graph, givenTimeOut, givenPropagationOnlyToZ);
	}

	/**
	 * Calls {@link CSTN#initAndCheck()} and, then, check all guarded links.
	 * So, it is override {@link CSTNU#initAndCheck()} allowing guarded links!
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
		for (final CSTNUEdge e : this.g.getEdges()) {
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

			CSTNUEdge eInverted = this.g.findEdge(d, s);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "Edge " + e + " is contingent. Found its companion: " + eInverted);
			}
			if (eInverted == null) {
				throw new IllegalArgumentException("Guarded edge " + e + " is alone. The companion Guarded edge between " + d.getName()
						+ " and " + s.getName() + " does not exist. It has to!");
			}
			if (!eInverted.isContingentEdge()) {
				throw new IllegalArgumentException("Edge " + e + " is guarded while the companion edge " + eInverted + " is not guarded!\nIt has be!");
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
					if (lowerCaseValue != Constants.INT_NULL && -initialValue > lowerCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is guarded with a negative value and the inverted " + eInverted + " has a guard that is smaller: "
										+ lowerCaseValueAsString(sourceALabel, lowerCaseValue, conjunctedLabel) + ".");
					}
					if (lowerCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue <= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is guarded with a negative value but the inverted " + eInverted
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
						// e.removeLabel(conjunctedLabel);

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
					if (upperCaseValue != Constants.INT_NULL && initialValue < -upperCaseValue) {
						throw new IllegalArgumentException(
								"Edge " + e + " is guarded with a positive value and the inverted " + eInverted
										+ " already contains a upper guard that is smaller: "
										+ upperCaseValueAsString(destALabel, upperCaseValue, conjunctedLabel) + ".");
					}
					if (upperCaseValue == Constants.INT_NULL && (eInvertedInitialValue == Constants.INT_NULL || eInvertedInitialValue >= 0)) {
						throw new IllegalArgumentException("Edge " + e + " is guarded with a positive value but the inverted " + eInverted
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
							// eInverted.removeLabel(conjunctedLabel);

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
				CSTNUEdge e = this.g.findEdge(this.Z, node);
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
	 * Labeled Letter Removal (zLr).<br>
	 * Overrides {@link CSTNU#labeledLetterRemovalRule(LabeledNode, LabeledNode, CSTNUEdge)}
	 * considering guarded links instead of contingent ones.<br>
	 * 
	 * <pre>
	 * X ---(v,ℵ,β)---&gt; A ---(x',c,α)---&gt; C 
	 *                    &lt;---(-x,α)-----
	 * adds 
	 *         
	 * X ---(v,ℵ',β)---&gt; A
	 * 	
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
	@Override
	boolean labeledLetterRemovalRule(final LabeledNode nX, final LabeledNode nA, final CSTNUEdge eXA) {

		if (!this.isActivationNode(nA))
			return false;

		boolean ruleApplied = false;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLr: start.");
		}

		for (CSTNUEdge eAC : this.g.getOutEdges(nA)) {
			if (eAC.getLowerCaseValue().isEmpty()) {
				continue;
			}
			// found a contingent link A===>C
			LabeledNode nC = this.g.getDest(eAC);
			CSTNUEdge eCA = this.g.findEdge(nC, nA);

			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER))
					LOG.log(Level.FINER, "zLr: found guarded link " + eAC);
			}
			for (final ALabel aleph : eXA.getUpperCaseValueMap().keySet()) {
				if (!aleph.contains(nC.getAlabel()))
					continue;
				LabeledIntTreeMap valuesMap = eXA.getUpperCaseValueMap().get(aleph);
				if (valuesMap == null)// a previous cycle could have removed it
					continue;
				for (Label beta : valuesMap.keySet()) {
					int v = eXA.getUpperCaseValue(beta, aleph);
					if (v == Constants.INT_NULL)
						continue;
					LabeledLowerCaseValue ACLowerCaseValueObj = eAC.getLowerCaseValue();
					final Label alpha = ACLowerCaseValueObj.getLabel();
					int x = eCA.getValue(alpha); // in the guarded, the lower bound is considered, no the guard ACLowerCaseValueObj.getValue();
					if (x == Constants.INT_NULL || v < x || !beta.subsumes(alpha))// x is already negative
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
									LOG.log(Level.FINER, "zLr applied to edge " + oldXA + ":\n" + "partic: "
											+ nC + " ---" + lowerCaseValueAsString(ALabel.emptyLabel, x, alpha) + "--> " + nA.getName()
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
				LOG.log(Level.FINER, "zLr: end.");
		}
		return ruleApplied;
	}

	/**
	 * Labeled LetterRemoval* (zLr)<br>
	 * Overrides {@link CSTNU#zLabeledLetterRemovalRule(LabeledNode, CSTNUEdge)}
	 * considering guarded links instead of contingent ones.<br>
	 * 
	 * <pre>
	 * Y ---(v,ℵ,β)---&gt; Z &lt;---(w,ℵ1,αl)--- A ---(x',c,l)---&gt; C 
	 *                                             &lt;---(-x,◇,l)---
	 * adds 
	 *         
	 * Y ---(v,ℵℵ1,β*(αl))---&gt; Z
	 *  
	 * if v ≥ w-x
	 * </pre>
	 * 
	 * @param nY
	 * @param eYZ
	 * @return true if the reduction has been applied.
	 */
	@Override
	boolean zLabeledLetterRemovalRule(final LabeledNode nY, final CSTNUEdge eYZ) {
		boolean ruleApplied = false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER))
				LOG.log(Level.FINER, "zLR: start.");
		}

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
					if (nA == this.Z)
						continue;
					CSTNUEdge AC = this.getLowerContingentLink(nC);

					LabeledLowerCaseValue lowerCaseEntry = AC.getLowerCaseValue();
					if (lowerCaseEntry.isEmpty())
						continue;
					Label l = lowerCaseEntry.getLabel();
					CSTNUEdge CA = this.g.findEdge(nC, nA);
					int x = CA.getValue(l);// guarded link, x must be the lower bound, not the lower guard lowerCaseEntry.getValue();
					if (x == Constants.INT_NULL)
						continue;
					CSTNUEdge AZ = this.g.findEdge(nA, this.Z);

					for (ALabel aleph1 : AZ.getAllUpperCaseAndLabeledValuesMaps().keySet()) {
						if (aleph1.contains(nodeLetter))
							continue;
						LabeledIntTreeMap AZAlephMap = AZ.getAllUpperCaseAndLabeledValuesMaps().get(aleph1);
						if (AZAlephMap == null)
							continue;
						for (Entry<Label> entryAZ : AZAlephMap.entrySet()) {
							final Label alpha = entryAZ.getKey();
							final int w = entryAZ.getIntValue();

							if (!alpha.subsumes(l))
								continue;// rule condition

							/**
							 * The following check is alternative.
							 * (v<w-x) would be more restrictive but the completeness proof is based on Math.max(v, w - x).
							 * So, we maintain Math.max(v, w - x) for now.
							 */
							// if (v < w - x) continue;
							v = Math.max(v, w + x);// guarded link x is already negative!

							ALabel alephAleph1 = aleph.conjunction(aleph1);
							alephAleph1.remove(nodeLetter);

							Label alphaBeta = alpha.conjunctionExtended(beta);

							final int oldValue = (Debug.ON) ? eYZ.getUpperCaseValue(alphaBeta, alephAleph1) : -1;
							final String oldYZ = (Debug.ON) ? eYZ.toString() : "";

							boolean mergeStatus = (alephAleph1.isEmpty()) ? eYZ.mergeLabeledValue(alphaBeta, v)
									: eYZ.mergeUpperCaseValue(alphaBeta, alephAleph1, v);

							if (mergeStatus) {
								ruleApplied = true;
								getCheckStatus().letterRemovalRuleCalls++;
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										if (LOG.isLoggable(Level.FINER))
											LOG.log(Level.FINER, "zLR applied to edge " + oldYZ + ":\n" + "partic: "
													+ nY.getName() + "---" + upperCaseValueAsString(aleph, v, beta) + "---> Z <---"
													+ upperCaseValueAsString(aleph1, w, alpha) + "--- " + nA.getName()
													+ "---" + lowerCaseValueAsString(nC.getAlabel(), x, l) + "---> " + nodeLetter
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

}
