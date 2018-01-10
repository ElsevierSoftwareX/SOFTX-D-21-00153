package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
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
import it.univr.di.labeledvalue.LabeledIntTreeMap;
import it.univr.di.labeledvalue.LabeledLowerCaseValue;

/**
 * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU)
 * exploit Bellman-Ford algorithms.
 * It is based on instantaneous reaction and uses only rules qR0, and qR3 as label modification rules.
 * It is assumed that CSTN are streamlined!
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNUPotential extends CSTNU {
	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static final Logger LOG = Logger.getLogger(CSTNUPotential.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final public String VERSIONandDATE = "Version 1.0 - Dec, 25 2017";

	/**
	 * @author posenato
	 */
	@SuppressWarnings("javadoc")
	private static enum BellmanCheckExit {
		TimeOut, NotControllable, NoPotentialModified, PotentialModified, ObservationPotentialModified
	}

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

		final CSTNUPotential cstnu = new CSTNUPotential();

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
			System.out.println("Final graph: " + cstnu.g.toString());
			System.out.println("Final graph potentials: ");
			for (LabeledNode node : cstnu.g.getVertices()) {
				System.out.println(node.toString());
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
	 * 
	 */
	EdgesToCheck edgesToCheck = new EdgesToCheck();

	/**
	 * 
	 */
	ObjectSet<LabeledNode> updatedNodes = new ObjectRBTreeSet<>();

	/**
	 * 
	 */
	Object2ObjectMap<LabeledNode, LabeledIntEdge> contingentNode2Edge = new Object2ObjectOpenHashMap<>();

	/**
	 * Default constructor, package use only!
	 */
	CSTNUPotential() {
		super();
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 */
	public CSTNUPotential(LabeledIntGraph g) {
		super(g);
	}

	/**
	 * Constructor for CSTNU
	 * 
	 * @param g graph to check
	 * @param timeOut timeout for the check
	 */
	public CSTNUPotential(LabeledIntGraph g, int timeOut) {
		super(g, timeOut);
	}

	/**
	 * Checks the controllability of a CSTNU instance and, if the instance is controllable, determines all the minimal ranges for the constraints. <br>
	 * All propositions that are redundant at run time are removed: therefore, all labels contains only the necessary and sufficient propositions.
	 *
	 * @return status an {@link it.univr.di.cstnu.algorithms.CSTNU.CSTNUCheckStatus} object containing the final status and some statistics about the executed
	 *         checking.
	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException
	 *             if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	@Override
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

		this.checkStatus.finished = false;
		Instant startInstant = Instant.now();
		Instant timeoutInstant = startInstant.plusSeconds(this.timeOut);

		this.g.reverse();
		potentialInit();

		final int n = this.g.getVertexCount();
		final int maxCycles = (int) (Math.pow(n, 3));
		int i;
		for (i = 1; i < maxCycles && this.checkStatus.consistency && !this.checkStatus.finished; i++) {// && this.updatedNodes.size() > 0 is made by
																										// oneStepDynamicControllability
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
				}
			}
			updateEdgesToCheck();
			this.checkStatus = this.oneStepDynamicControllability(timeoutInstant);

			if (!this.checkStatus.finished) {
				if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
					if (Debug.ON) {
						String msg = "During the check # " + i + " time out of " + this.timeOut + " seconds occured. ";
						if (LOG.isLoggable(Level.INFO)) {
							LOG.log(Level.INFO, msg);
						}
					}
					this.checkStatus.executionTimeNS = ChronoUnit.NANOS.between(Instant.now(), startInstant);
					return getCheckStatus();
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
		this.g.reverse();
		return getCheckStatus();
	}

	/**
	 * <h1>Labeled Cross-Lower Case (LCC*)</h1>
	 * 
	 * <pre>
	 *      u,c,α            
	 * C ------------&gt; A 
	 * v,ℵ,β   
	 * adds 
	 * A
	 * u+v,ℵ,αβ
	 * 
	 * if αβ∈P*, C ∉ ℵ, and v<0.
	 * </pre>
	 * 
	 * Since it is assumed that L(C)=L(A)=α, there is only ONE lower-case labeled value u,c,α!
	 * 
	 * @param nC
	 * @param nA
	 * @param eCA CANNOT BE NULL
	 * @return true if the rule has been applied.
	 */
	boolean labeledCrossLowerCaseRule(final LabeledNode nC, final LabeledNode nA, final LabeledIntEdge eCA) {

		boolean ruleApplied = false;
		final LabeledLowerCaseValue lowerCaseValue = eCA.getLowerCaseValue();
		if (lowerCaseValue.isEmpty())
			return false;

		// Since it is assumed that L(C)=L(A)=α, there is only ONE lower-case labeled value u,c,α!
		ALabel c = lowerCaseValue.getNodeName();
		Label alpha = lowerCaseValue.getLabel();
		int u = lowerCaseValue.getValue();

		final ObjectSet<ALabel> CPotentialALabelSet = nC.getALabelsOfPotential();
		if (CPotentialALabelSet.isEmpty())
			return false;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "LCC*: start.");
			}
		}

		for (ALabel aleph : CPotentialALabelSet) {
			final boolean alephNOTEmpty = !aleph.isEmpty();
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryCPotential : nC.getPotentialEntrySet(aleph)) {
				final int v = entryCPotential.getIntValue();
				if (v >= 0)
					continue; // Rule condition!

				// Rule condition: upper case label cannot be equal or contain c name
				if (alephNOTEmpty && aleph.contains(c)) {
					continue;// rule condition
				}

				final Label beta = entryCPotential.getKey();
				final Label alphaBeta = beta.conjunction(alpha);
				if (alphaBeta == null)
					continue;
				final int sum = Constants.sumWithOverflowCheck(v, u);

				if (sum >= 0) {
					continue;// positive upper-case values are super-seeded by 0 value that is implicit in a loop
				}

				// Management of labeledLetterRemoval!
				if (alephNOTEmpty) {// FIXME
					aleph = labeledLetterRemoval(aleph, sum);
				}

				final int oldValue = nA.getPotential(aleph, alphaBeta);

				if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
					// value is stored only if it is more negative than the current one.
					continue;
				}

				String logMsg = null;
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER,
								"LCLC applied to  " + nA.toString()
										+ ":\npartic: " + nC.getName() + "(" + upperCaseValueAsString(aleph, v, beta) + ")---"
										+ lowerCaseValueAsString(c, u, alpha) + "--> " + nA.getName()
										+ "\nresult: " + upperCaseValueAsString(aleph, sum, alphaBeta)
										+ "\nold value:" + oldValue);
					}
				}

				boolean localApp = nA.potentialPut(aleph, alphaBeta, sum);

				if (localApp) {
					ruleApplied = true;
					if (alephNOTEmpty)
						getCheckStatus().crossCaseRuleCalls++;
					else
						getCheckStatus().lowerCaseRuleCalls++;
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, logMsg);
						}
					}
				}

				if (isThereANegativePotentialInZ(nA, sum)) {
					return true;
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
	 * Apply 'labeled no case' and 'labeled upper case' and 'forward labeled upper case' and 'labeled conjuncted upper case' rules.<br>
	 * 
	 * <pre>
	 * 1) CASE LNC+LUC*
	 *            u,◇,α        
	 * Y &lt;------------ X 
	 *                 v,ℵ,β 
	 * adds                                 
	 * Y
	 *  u+v,ℵ,αβ
	 * 
	 * ℵ can be empty.
	 * 
	 * 2) CASE FLUC+LCUC
	 *            u,C,α        
	 * C &lt;------------ A 
	 *                  v,ℵ,β    
	 * adds 
	 * C
	 * u+v,Cℵ,αβ
	
	 * ℵ can be empty.
	 * </pre>
	 * 
	 * Moreover, in case LNC+LUC*, each time it adds a potential generated by a contingent t.p. X, it manages the letter removal possibility.
	 * 
	 * @param nX
	 * @param nY
	 * @param eXY CANNOT BE NULL
	 * @return true if a reduction is applied at least
	 */
	// Don't rename such method because it has to overwrite the CSTN one!
	boolean labeledPropagationqLP(final LabeledNode nX, final LabeledNode nY, final LabeledIntEdge eXY) {

		boolean ruleApplied = false;
		ObjectSet<ALabel> nXPotentialALabelSet = nX.getALabelsOfPotential();
		if (nXPotentialALabelSet.size() == 0)
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

			for (ALabel aleph : nXPotentialALabelSet) {

				boolean emptyUpperCase = aleph.isEmpty();

				String letterRemoval = "";
				for (Object2IntMap.Entry<Label> entryXPotential : nX.getPotentialEntrySet(aleph)) {
					boolean letterRemovalRuleCall = false;

					final Label beta = entryXPotential.getKey();
					Label alphaBeta;
					alphaBeta = alpha.conjunction(beta);
					if (alphaBeta == null)
						continue;

					final int v = entryXPotential.getIntValue();
					int sum = Constants.sumWithOverflowCheck(u, v);
					if (sum >= 0) // New condition that works well for big instances!
						continue;

					// Management of labeledLetterRemoval!
					if (u > 0 && !emptyUpperCase && nY.getAlabel() != null && !aleph.contains(nY.getAlabel())) {
						aleph = labeledLetterRemoval(aleph, sum);
						letterRemovalRuleCall = true;
					}

					final int oldValue = nY.getPotential(aleph, alphaBeta);

					if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
						// value is stored only if it is more negative than the current one.
						continue;
					}

					String oldnY = nY.toString();
					boolean mergeStatus = nY.potentialPut(aleph, alphaBeta, sum);

					if (mergeStatus) {
						ruleApplied = true;
						if (letterRemovalRuleCall)
							getCheckStatus().letterRemovalRuleCalls++;
						else {
							if (emptyUpperCase) {
								this.checkStatus.labeledValuePropagationCalls++;
							} else {
								getCheckStatus().upperCaseRuleCalls++;
							}
						}
						if (Debug.ON) {
							if (LOG.isLoggable(Level.FINER)) {
								LOG.log(Level.FINER, "LNC+LUC* applied to potential of node " + oldnY + ":\n" + "partic: "
										+ nX.getName() + "(" + upperCaseValueAsString(aleph, v, beta) + ")--- "
										+ upperCaseValueAsString(ALabel.emptyLabel, u, alpha) + "---> " + nY.getName()
										+ "\nresult: "
										+ nY.toString()
										+ "; old value: " + Constants.formatInt(oldValue)
										+ ((!letterRemoval.isEmpty()) ? "\n" + letterRemoval : ""));
							}
						}

						if (isThereANegativePotentialInZ(nY, sum)) {
							return true;
						}
					}
				}
			}
		}

		final ObjectSet<ALabel> XYUpperCaseALabels = eXY.getUpperCaseValueMap().keySet();

		// 2) CASE FLUC + LCUC
		ALabel nYasALabel = nY.getAlabel();
		for (final ALabel upperCaseLabel : XYUpperCaseALabels) {
			if (upperCaseLabel.size() != 1 || !upperCaseLabel.equals(nYasALabel)) {
				continue;// only UC label corresponding to original contingent upper case value is considered.
			}
			for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryXY : eXY.getUpperCaseValueMap().get(upperCaseLabel).entrySet()) {
				final Label alpha = entryXY.getKey();
				final int u = entryXY.getIntValue();

				for (final ALabel aleph : nXPotentialALabelSet) {
					for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryXPotential : nX.getPotentialEntrySet(aleph)) {
						final Label beta = entryXPotential.getKey();

						Label alphaBeta = alpha.conjunction(beta);
						if (alphaBeta == null)
							continue;

						final ALabel upperCaseLetterAleph = upperCaseLabel.conjunction(aleph);
						final int v = entryXPotential.getIntValue();

						int sum = Constants.sumWithOverflowCheck(u, v);
						// Here sum is always non positive!

						if (sum >= 0) {
							// it would be a redundant edge
							continue;
						}

						final int oldValue = nY.getPotential(upperCaseLetterAleph, alphaBeta);

						if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
							// in the case of A != C, a value is stored only if it is more negative than the current one.
							continue;
						}

						String oldnY = nY.toString();

						boolean mergeStatus = nY.potentialPut(upperCaseLetterAleph, alphaBeta, sum);

						if (mergeStatus) {
							ruleApplied = true;
							getCheckStatus().upperCaseRuleCalls++;
							if (Debug.ON) {
								if (LOG.isLoggable(Level.FINER)) {
									LOG.log(Level.FINER,
											"FLUC+LCUC applied to node " + oldnY + ":\n" + "partic: "
													+ nX.getName() + "(" + upperCaseValueAsString(aleph, v, beta) + ")--- " +
													upperCaseValueAsString(upperCaseLabel, u, alpha) + "---> " + nY.getName()
													+ "\nresult: "
													+ nY.toString()
													+ "; old value: " + Constants.formatInt(oldValue));
								}
							}

							if (isThereANegativePotentialInZ(nY, sum)) {
								return true;
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
	 * Implements the qR3* rule assuming instantaneous reaction and a streamlined network.<br>
	 * <b>This differs from {@link CSTNIR3RwoNodeLabels#labelModificationR3qR3(LabeledNode, LabeledNode, LabeledIntEdge)}
	 * in the checking also upper case value.</b>
	 * 
	 * @param node node
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationqR3(final LabeledNode node) {

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Label Modification qR3*: start.");
			}
		}
		boolean ruleApplied = false;

		LabeledALabelIntTreeMap allNodePotential = node.clonePotential();
		if (allNodePotential.isEmpty())
			return false;

		final ObjectSet<Label> NodePotentialLabelSet = new ObjectRBTreeSet<>();
		for (ALabel alabel : allNodePotential.keySet()) {
			NodePotentialLabelSet.addAll(allNodePotential.get(alabel).keySet());
		}
		final Label allLiteralsInPotentials = new Label();
		for (Label l : NodePotentialLabelSet) {
			allLiteralsInPotentials.conjunctExtended(l);
		}

		for (final LabeledNode nObs : this.g.getObservers()) {
			if (nObs == node)
				continue;

			final char p = nObs.getPropositionObserved();
			if (!allLiteralsInPotentials.contains(p)) {
				continue;
			}

			// all potentials of nObs
			LabeledALabelIntTreeMap obsPotential = nObs.clonePotential();
			for (final ALabel aleph1 : obsPotential.keySet()) {
				for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<Label> entryObsPotential : obsPotential.get(aleph1).entrySet()) {
					final int w = entryObsPotential.getIntValue();
					if (w == 0) { // Table 1 ICAPS
						continue;
					}

					final Label gamma = entryObsPotential.getKey();

					for (final ALabel aleph : allNodePotential.keySet()) {
						for (Object2IntMap.Entry<Label> entryNodePotential : allNodePotential.get(aleph).entrySet()) {

							Label nodePotentialLabel = entryNodePotential.getKey();
							final int v = node.getPotential(aleph, nodePotentialLabel);
							if (v == Constants.INT_NULL) {
								// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
								continue;
							}

							Label newLabel = makeBetaGammaDagger4qR3(node, nObs, p, gamma, nodePotentialLabel);
							if (newLabel == null) {
								continue;
							}
							final int max = R3qR3NewValue(v, w);
							ALabel newUpperCaseLetter = aleph.conjunction(aleph1);

							ruleApplied = node.potentialPut(newUpperCaseLetter, newLabel, max);

							if (ruleApplied) {
								if (Debug.ON) {
									if (LOG.isLoggable(Level.FINER)) {
										LOG.log(Level.FINER, "qR3* adds a labeled value to potential of " + node.getName() + ":\n"
												+ "source: " + nObs.getName() + " (" + upperCaseValueAsString(aleph1, w, gamma) + ")--->"
												+ this.Z.getName()
												+ " <---" + node.getName() + "(" + upperCaseValueAsString(aleph, v, nodePotentialLabel) + ")"
												+ "\nresult: add " + node.getName() + " (" + upperCaseValueAsString(newUpperCaseLetter, max, newLabel)
												+ ")");
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
	 * Executes one step of the dynamic controllability check.<br>
	 * 
	 * @param timeoutInstant time instant limit allowed to the computation.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNUCheckStatus oneStepDynamicControllability(Instant timeoutInstant) throws WellDefinitionException {

		this.checkStatus.cycles++;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE))
				LOG.log(Level.FINE, "Start application labeled constraint generation and label removal rules.");
		}

		int nEdges = this.edgesToCheck.size(), n = this.g.getVertexCount();
		boolean oneObservationPotentialChanged = false;
		// int maxNumberOfValueInAnEdge = 0, maxNumberOfUpperCaseValuesInAnEdge = 0;
		// LabeledIntEdge fatEdgeInLabeledValues = null, fatEdgeInUpperCaseValues = null;// for sure they will be initialized!
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO))
				LOG.log(Level.INFO, "Number of edges to analyze: " + nEdges);
		}

		BellmanCheckExit statusOneBellmanStep = BellmanCheckExit.PotentialModified;
		for (int j = 1; j < n && statusOneBellmanStep != BellmanCheckExit.NoPotentialModified; j++) {
			statusOneBellmanStep = oneBellmanStep(timeoutInstant, j, n, false);
			if (statusOneBellmanStep == BellmanCheckExit.TimeOut || statusOneBellmanStep == BellmanCheckExit.NotControllable) {
				return getCheckStatus();
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				if (statusOneBellmanStep == BellmanCheckExit.NoPotentialModified) {
					LOG.log(Level.FINE, "Phase check ends early because no potential has been modified in the last phase.");
				}
			}
		}
		// Check negative cycle presence
		statusOneBellmanStep = oneBellmanStep(timeoutInstant, 1, n, true);
		if (statusOneBellmanStep == BellmanCheckExit.TimeOut || statusOneBellmanStep == BellmanCheckExit.NotControllable) {
			return getCheckStatus();
		}
		if (statusOneBellmanStep == BellmanCheckExit.PotentialModified || statusOneBellmanStep == BellmanCheckExit.ObservationPotentialModified) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "Negative cycle found! Check finished!");
				}
			}
			this.checkStatus.consistency = false;
			this.checkStatus.finished = true;
			return getCheckStatus();
		}

		this.checkStatus.finished = this.updatedNodes.isEmpty();
		if (this.checkStatus.finished) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINE)) {
					LOG.log(Level.FINE, "All rule application finished. Stable state reached.");
				}
			}
			return getCheckStatus();
		}

		// Check potentials

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Check all potentials with respect to qR3*.");
			}
		}
		Collection<LabeledNode> potentialToCheck = (oneObservationPotentialChanged) ? this.g.getVertices() : this.updatedNodes;

		for (LabeledNode node : potentialToCheck) {
			labelModificationqR3(node);
			// labeledLetterRemovalRule(node);

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return getCheckStatus();
			}
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "All rule application finished.");
			}
		}
		return getCheckStatus();
	}

	/**
	 * @param timeoutInstant
	 * @param phase
	 * @param nPhase
	 * @param onlyCheck
	 * @return -1 = timeout o not controllability, 0 = no potential modified, 1 = at least one potential modified, 2 = one observation potential modified
	 */
	private BellmanCheckExit oneBellmanStep(Instant timeoutInstant, int phase, int nPhase, boolean onlyCheck) {
		LabeledNode A, B;
		LabeledALabelIntTreeMap BPotentialCopy;
		int edgeIndex = 1;
		int nEdges = this.edgesToCheck.size();
		BellmanCheckExit potentialModified = BellmanCheckExit.NoPotentialModified;
		for (LabeledIntEdge AB : this.edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, ((onlyCheck) ? "\nCheck Phase " : "\nPhase ") + phase + "/" + nPhase + ": Considering edge " + (edgeIndex++) + "/"
							+ nEdges + ": " + AB + "\n");
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);

			BPotentialCopy = B.clonePotential();

			this.labeledPropagationqLP(A, B, AB);

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus)) {
				return BellmanCheckExit.TimeOut;
			}

			/**
			 * The following rule are called if there are condition (avoid to call for nothing)
			 */
			if (!AB.getLowerCaseValue().isEmpty()) {
				labeledCrossLowerCaseRule(A, B, AB);
			}

			if (!B.isPotentialEqual(BPotentialCopy)) {
				this.updatedNodes.add(B);
				if (potentialModified == BellmanCheckExit.NoPotentialModified)
					potentialModified = BellmanCheckExit.PotentialModified;
				if (onlyCheck)
					return BellmanCheckExit.PotentialModified;
				if (B.isObserver()) {
					potentialModified = BellmanCheckExit.ObservationPotentialModified;
				}
			}

			if (!this.checkStatus.consistency) {
				this.checkStatus.finished = true;
				return BellmanCheckExit.NotControllable;
			}

			if (checkTimeOutAndAdjustStatus(timeoutInstant, this.checkStatus))
				return BellmanCheckExit.TimeOut;
		}
		return potentialModified;
	}

	/**
	 * Initializes the potential of each node, the set of node to update, and the map contingent node-->lower case edge.
	 * The graph is assumed already reversed.
	 */
	void potentialInit() {
		this.updatedNodes.clear();
		this.Z.potentialPut(Label.emptyLabel, 0);
		this.updatedNodes.add(this.Z);
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Potential initialization.");
			}
		}

		for (LabeledIntEdge e : this.g.getOutEdges(this.Z)) {
			LabeledNode node = this.g.getDest(e);
			this.updatedNodes.add(node);

			node.potentialPut(ALabel.emptyLabel, e.getLabeledValueMap());

			for (ALabel alabel : e.getUpperCaseValueMap().keySet()) {
				node.potentialPut(alabel, e.getUpperCaseValueMap().get(alabel));
			}
		}

		for (LabeledIntEdge e : this.g.getLowerLabeledEdges()) {
			this.contingentNode2Edge.put(this.g.getSource(e), e);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Contingent nodes: " + this.contingentNode2Edge);
			}
		}
	}

	/**
	 * Updates edgesToCheck considering current updatedNodes: edgesToCheck will contain all out edges of updatedNodes.
	 * UpdatedNodes will be cleared.
	 */
	void updateEdgesToCheck() {
		this.edgesToCheck.clear();
		for (LabeledNode node : this.updatedNodes) {
			this.edgesToCheck.addAll(this.g.getOutEdges(node));
		}
		this.updatedNodes.clear();
	}

	/**
	 * Checks if there is a negative potential in Z and, if so, adjusts this.checkStatus and return true.
	 * 
	 * @param node
	 * @param value
	 * @return true if node is Z and value is < 0. This is a condition of not controllability!
	 */
	private boolean isThereANegativePotentialInZ(LabeledNode node, int value) {
		if (this.Z == node && value < 0) {
			this.checkStatus.consistency = false;
			this.checkStatus.finished = true;
			return true;
		}
		return false;
	}

	/**
	 * Checks if the upper case value (aleph: value) can be simplified w.r.t. the upper case letter.
	 * 
	 * @param aleph
	 * @param alephPotential
	 * @return the new aleph
	 */
	private ALabel labeledLetterRemoval(ALabel aleph, int alephPotential) {
		ALabel aleph1 = null;
		for (ALetter aletter : aleph) {
			LabeledNode node = this.g.getNode(aletter.name);
			int nodePotential = node.getPotential(node.getAlabel()).getMinValue();
			LabeledIntEdge e = this.contingentNode2Edge.get(node);
			int activationPotential = nodePotential + e.getLowerCaseValue().getValue();
			if (alephPotential >= activationPotential) {// they are negative values!
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST, "Label letter applied to " + aleph + ":" + alephPotential
								+ "\nNode removed:" + aletter + " because its potential is " + nodePotential + " and lower case value is "
								+ e.getLowerCaseValue().getValue());
					}
				}
				if (aleph1 == null)
					aleph1 = ALabel.clone(aleph);
				aleph1.remove(aletter);
			}
		}
		if (aleph1 == null)
			return aleph;
		return aleph1;
	}
}
