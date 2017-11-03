package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.xml.sax.SAXException;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.Debug;
import it.univr.di.cstnu.graph.CSTNUGraphMLReader;
import it.univr.di.cstnu.graph.CSTNUGraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.visualization.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * In this class, the dynamic consistency check is done assuming instantaneous reaction DC semantics (cf. ICAPS 2016 paper, table 1).
 * This class uses only LP, qR0 and qR3* rules.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNirRestricted extends CSTNir {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNirRestricted.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final public String VERSIONandDATE = "Version 6.0 - October, 16 2017";// SVN 202. Removed qLabels from LP. Removed R0 and R3. Now, rules are LP,
	// qR0 and qR3*
	static final public String VERSIONandDATE = "Version  6.1 - October, 25 2017";// SVN 203. Code optimization.

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Start...");
			}
		}
		final CSTNirRestricted cstn = new CSTNirRestricted();

		if (!cstn.manageParameters(args))
			return;
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Parameters ok!");
			}
		}
		if (cstn.versionReq) {
			System.out.println(CSTNirRestricted.class.getName() + " " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Loading graph...");
			}
		}
		CSTNUGraphMLReader graphMLReader = new CSTNUGraphMLReader(cstn.fInput, LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("LabeledIntGraph loaded!\nStandard DC Checking...");
			}
		}
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("LabeledIntGraph minimized!");
			}
		}
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
	 * 
	 */
	public CSTNirRestricted() {
		super();
	}

	/**
	 * @param g
	 */
	public CSTNirRestricted(LabeledIntGraph g) {
		this();
		this.setG(g);// sets also checkStatus!
	}

	/**
	 * Help method to initialize and check the CSTN represented by graph g. The {@link #dynamicConsistencyCheck()} calls this method before
	 * to execute the check. If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level.
	 * Since the current DC checking algorithm is complete only the CSTN instance contains an upper bound to the distance between Z (the first node) and Ω (the
	 * last node), this procedure add such upper bound (= #nodes * max weight value).
	 * This method differs from {@link CSTN#initAndCheck()} because it does not apply R0 and R3*.
	 * 
	 * @return true if the graph is a well formed
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	@Override
	public boolean initAndCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Starting initial well definition check.");
			}
		}
		this.g.clearCache();

		// Checks the presence of Z node!
		LabeledNode Z = this.g.getZ();
		if (Z == null) {
			Z = this.g.getNode(CSTN.ZeroNodeName);
			if (Z == null) {
				// We add by authority!
				Z = new LabeledNode(CSTN.ZeroNodeName);
				Z.setX(5);
				Z.setY(5);
				this.g.addVertex(Z);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + CSTN.ZeroNodeName + " node found: added!");
				}
			}
			this.g.setZ(Z);
		} else {
			if (!Z.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
				}
				Z.setLabel(Label.emptyLabel);
			}
			CSTN.ZeroNodeName = Z.getName();
		}
		// Checks the presence of Ω node!
		LabeledNode Ω = this.g.getΩ();
		if (Ω == null) {
			Ω = this.g.getNode(CSTN.OmegaNodeName);
			if (Ω == null) {
				// We add by authority!
				Ω = new LabeledNode(CSTN.OmegaNodeName);
				Ω.setX(5);
				Ω.setY(50);
				this.g.addVertex(Ω);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "No " + CSTN.OmegaNodeName + " node found: added!");
				}
			}
			this.g.setΩ(Ω);
		} else {
			if (!Ω.getLabel().isEmpty()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING))
						LOG.log(Level.WARNING, "In the graph, Ω node has not empty label. Label removed!");
				}
				Ω.setLabel(Label.emptyLabel);
			}
			CSTN.OmegaNodeName = Ω.getName();
		}

		// Checks well definiteness of edges and determine maxWeight
		this.maxWeight = 0;
		for (final LabeledIntEdge e : this.g.getEdges()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Initial Checking edge e: " + e);
				}
			}
			// Determines the absolute max weight value
			for (Object2IntMap.Entry<Label> entry : e.getLabeledValueSet()) {
				int v = entry.getIntValue();
				if (v < this.maxWeight)
					this.maxWeight = v;
				// else if (v > this.maxWeight)
				// this.maxWeight = v;
			}

			final LabeledNode s = this.g.getSource(e);
			final LabeledNode d = this.g.getDest(e);

			if (s == d) {
				// loop are not admissible
				this.g.removeEdge(e);
				continue;
			}
			// WD1 is checked and adjusted here
			try {
				checkWellDefinitionProperty1and3(s, d, e, true);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (e.isEmpty()) {
				// The merge removed labels...
				this.g.removeEdge(e);
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Labels fixing on edge " + e + " removed all labels. Edge " + e + " has been removed.");
					}
				}
				continue;
			}

			if (e.isContingentEdge()) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER,
								"Found a contingent edge: " + e + ". The consistency check does not difference between ordinary and contingent edges.");
					}
				}
			}
		} // end maxWeight search
		this.maxWeight = -this.maxWeight;
		// Determine horizon value
		long product = ((long) this.maxWeight) * this.g.getVertexCount();
		if (product >= Constants.INT_POS_INFINITE) {
			throw new ArithmeticException("Horizon value is not representable by an integer.");
		}
		this.horizon = (int) product;

		// Init two useful structures
		this.g.getPropositions();

		// Checks well definiteness of nodes
		final Collection<LabeledNode> nodeSet = this.g.getVertices();
		for (final LabeledNode node : nodeSet) {

			// 1. Checks that observation node doesn't have the observed proposition in its label!
			final char obs = node.getPropositionObserved();
			final Label label = node.getLabel();
			if (obs != Constants.UNKNOWN && label.contains(obs)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName()
								+ "'. Removed!");
					}
				}
				label.remove(obs);
			}

			// WD2 is checked and adjusted here
			try {
				checkWellDefinitionProperty2(node, true);
			} catch (final WellDefinitionException ex) {
				throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
			}

			// 3. Checks that each node has an edge to Z.
			if (node != Z) {
				LabeledIntEdge edgeToZ = this.g.findEdge(node, Z);
				if (edgeToZ == null) {
					edgeToZ = makeNewEdge(node.getName() + "_" + CSTN.ZeroNodeName, ConstraintType.internal);
					this.g.addEdge(edgeToZ, node, Z);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs after '" + CSTN.ZeroNodeName + "'.");
						}
					}
				}
				edgeToZ.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
			}

			// 4. Checks that each node has an edge from Ω
			if (node != Ω) {
				LabeledIntEdge edgeFromΩ = this.g.findEdge(Ω, node);
				if (edgeFromΩ == null) {
					edgeFromΩ = makeNewEdge(CSTN.OmegaNodeName + "_" + node.getName(), ConstraintType.internal);
					this.g.addEdge(edgeFromΩ, Ω, node);
					if (Debug.ON) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER,
									"It is necessary to add a constraint to guarantee that '" + node.getName() + "' occurs before '" + CSTN.OmegaNodeName
											+ "'.");
						}
					}
				}
				/**
				 * The following edges are necessary to make comeplete the algorithm and to make it faster even if Ω is already present
				 * and partially connected to other nodes.
				 */
				edgeFromΩ.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be before Ω!
			}
		}
		// Edge from Z to Ω
		LabeledIntEdge eZΩ = this.g.findEdge(Z, Ω);
		if (eZΩ == null) {
			eZΩ = makeNewEdge(CSTN.ZeroNodeName + "_" + CSTN.OmegaNodeName, ConstraintType.internal);
			this.g.addEdge(eZΩ, Z, Ω);
			if (Debug.ON) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							"It is necessary to add a constraint to guarantee that '" + Ω.getName() + "' occurs at most " + this.horizon + " after '"
									+ CSTN.ZeroNodeName + "'.");
				}
			}
		}
		eZΩ.mergeLabeledValue(Label.emptyLabel, this.horizon);

		// It is better to normalize with respect to the label modification rules before starting the DC check.
		// Such normalization assures only that redundant labels are removed (w.r.t. qR0)
		// Q* are not solved by this normalization!
		this.checkStatus.reset();
		for (final LabeledIntEdge e : this.g.getInEdges(Z)) {
			final LabeledNode s = this.g.getSource(e);
			// Normalize with respect to R0--R3
			if (s.isObserver()) {
				this.labelModificationR0(s, Z, e);
			}
			this.labelModificationR3(s, Z, e);
			if (s.isObserver()) {
				// again because R3 could have add a new value;
				this.labelModificationR0(s, Z, e);
			}
		}
		// if (Debug.ON && LOG.isLoggable(Level.FINER)) {
		// LOG.log(Level.FINER, "A preliminary application of label modification rules has been done: " + this.checkStatus.toString());
		// }
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Initial well definition check done!");
			}
		}
		this.checkStatus.initialized = true;
		return true;
	}

	/**
	 * On 2017-10-16 it has been shown that R0 is not more necessary for IR DC checking.<br>
	 * For now, I comment out old code.<br>
	 * Applies rule qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Instantaneous reaction semantics is assumed.</b>
	 * 
	 * <pre>
	 * qR0:
	 * P? --[w, α p]--&gt; Z 
	 * changes in 
	 * P? --[w, α']--&gt; Z when w &le; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * It is assumed that P? != Z.<br>
	 * 
	 * @param nObs the observation node
	 * @param nZ
	 * @param eObsX the edge connecting nObs? ---&gt; X
	 * @return true if the rule has been applied one time at least.
	 */
	// boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge eObsX) {
	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nZ, final LabeledIntEdge eObsX) {// nX is assumed == Z!
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
		// Z cannot have a label
		// if (nX.getLabel().contains(p)) {
		// // Table 1 ICAPS
		// if (Debug.ON) {
		// if (LOG.isLoggable(Level.FINER)) {
		// LOG.log(Level.FINER,
		// "R0: Proposition " + p + " is present in the X label '" + nX.getLabel() + ". WD1 must be preserved, so R0 cannot be applied.");
		// }
		// }
		// return false;
		// }

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

			// final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
			final Label alphaPrime = makeAlphaPrime(nObs, nZ, p, l);
			if (alphaPrime == null) {
				continue;
			}
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

	@Override
	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge ePZ) {
		throw new IllegalAccessError("R0 cannot be used in a CSTNirRestricted. Only qR0!");
	}

	/**
	 * <h1>Rule qR3*</h1>
	 * On 2017-10-16 it has been shown that R3 is not more necessary.<br>
	 * For now, I comment out old code.<br>
	 * <b>This method assumes Instantaneous Reaction semantics.</b>
	 * 
	 * <pre>
	 *  NO MORE IMPLEMENTED!
	 * if P? --[w, αβ]--&gt; nD &lt;--[v, βγp]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[max{w,v}, αβγ']-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * </pre>
	 *
	 * <h2>Rule qR3*</h2>
	 * 
	 * <pre>
	 * if P? --[w, γ]--&gt; Z &lt;--[v, βθp']-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[max{w,v}, (γ★β)†]-- nS
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
	 * @param nZ node IT MUST BE THE Z node of the graph. NO check is made!
	 * @param eSZ LabeledIntEdge containing the constrain to modify
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	// boolean labelModificationR3(final LabeledNode nS, final LabeledNode nZ, final LabeledNode Z, final LabeledIntEdge eSD) {
	boolean labelModificationR3(final LabeledNode nS, final LabeledNode nZ, final LabeledIntEdge eSZ) {// it assumed that nZ is the Z node!
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
			allLiteralsSZ.conjunctionExtended(l);
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
			if (nS.getLabel().contains(p)) { // || nD.getLabel().contains(p)) {// WD1 must be preserved!
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINER)) {
						LOG.log(Level.FINER, "qR3*: Proposition " + p + " is present in the nS label '" + nS.getLabel()
								+ ". WD1 must be preserved, so R3 cannot be applied.");
					}
				}
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

					Label newLabel = // (nD != nZ) ? makeAlphaBetaGammaPrime4R3(nS, nD, nObs, p, ObsDLabel, SDLabel) :
							makeBetaGammaDagger4qR3(nS, nZ, nObs, p, ObsZLabel, SDLabel);
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
	 * Applies the labeled propagation rule:<br>
	 * <b>This method is also valid assuming Instantaneous Reaction semantics.</b>
	 * QLabels are not more considered because it has been shown that are not necessary (2017-10-10).
	 * 
	 * <pre>
	 * if A --[α, u]--&gt; B --[β, v]--&gt; C, 
	 * then A --[αβ, u+v]--&gt; C 
	 * 
	 * If A==C and u+c<0, then the network is not DC.
	 * </pre>
	 * 
	 * In order to speed up the method, NO checks of null object are done.
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
	boolean labeledPropagationRule(final LabeledNode nA, final LabeledNode nB, final LabeledNode nC, final LabeledIntEdge eAB,
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
				if (!newLabelAC.subsumes(nAnCLabel)) {
					if (Debug.ON)
						LOG.log(Level.FINER,
								"AlphaBeta '" + newLabelAC + "' does not subsume conjunction of node labels '" + nAnCLabel + "'. New value cannot be added.");
					continue;
				}

				// boolean qLabel = false;
				// if ((u >= 0) && (v < 0)) {
				// newLabelAC = labelAB.conjunction(labelBC);
				// if (newLabelAC == null) {
				// continue;
				// }
				// } else {
				// newLabelAC = labelAB.conjunctionExtended(labelBC);
				// qLabel = newLabelAC.containsUnknown();
				// if (qLabel) {
				// removeChildrenOfUnknown(newLabelAC);
				// }
				// }
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
					// if (!qLabel) {
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
					// }
					// sum = Constants.INT_NEG_INFINITE;
				}
				// else {
				// in the case of A != C, a value is stored only if it is more negative than the current one.
				if ((oldValue != Constants.INT_NULL) && (sum >= oldValue)) {
					continue;
				}
				// }
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
					// if (sum == Constants.INT_NEG_INFINITE && nA == nC && u != Constants.INT_NEG_INFINITE && v != Constants.INT_NEG_INFINITE) {
					// if (v >= 0)
					// this.checkStatus.qSemiNegLoop++;
					// else
					// this.checkStatus.qAllNegLoop++;
					// }
				}
			}
		}
		return ruleApplied;
	}

	@Override
	boolean labelModificationR3(final LabeledNode nS, final LabeledNode nD, final LabeledNode nZ, final LabeledIntEdge eSZ) {
		throw new IllegalAccessError("R3* cannot be used in a CSTNirRestricted");
	}

	/**
	 * On 2017-10-16 it has been simplified for working only with qR0. The previous code has been comment out!<br>
	 * Simple method to determine the α' to use in rule qR0.
	 * Check paper TIME15 and ICAPS 2016 about CSTN sound&amp;complete DC check.
	 * α' is obtained by α removing all children of the observed proposition.
	 * If X==Z, then it is necessary also to remove all children of unknown from α'.
	 * 
	 * @param nObs observator node
	 * @param nZ
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up.)
	 * @param labelFromObs label of the edge from observator
	 * @return α'
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	// Label makeAlphaPrime(final LabeledNode nX, final LabeledNode nObs, final LabeledNode nZ, final char observed, final Label labelFromObs) {
	Label makeAlphaPrime(final LabeledNode nObs, final LabeledNode nZ, final char observed, final Label labelFromObs) {

		// if (nX.getLabel().contains(observed))
		// return null;

		StringBuilder logStr;
		if (Debug.ON) {
			logStr = new StringBuilder();
			if (LOG.isLoggable(Level.FINEST))
				logStr.append("labelEdgeFromObs = " + labelFromObs);
		}

		Label alphaPrime = new Label(labelFromObs);
		alphaPrime.remove(observed);
		alphaPrime.remove(this.g.getChildrenOf(nObs));
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				logStr.append(", labelWithoutPandChildren=" + alphaPrime);
		}

		if (// nX == nZ &&
		alphaPrime.containsUnknown()) {
			removeChildrenOfUnknown(alphaPrime);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				logStr.append(", α'=" + alphaPrime);
			}
		}
		if (// !alphaPrime.subsumes(nX.getLabel().conjunction(nObs.getLabel()))
		!alphaPrime.subsumes(nObs.getLabel())) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, logStr.toString() + " α' does not subsume labels from nodes:" // + nX.getLabel().conjunction(nObs.getLabel()));
							+ nObs.getLabel());
				}
			}
			return null;
		}
		return alphaPrime;
	}

	/**
	 * Simple method to determine the label (β*γ)† to use in rules qR3*.
	 * See Table 1 and Table 2 ICAPS 2016.
	 * 
	 * <pre>
	 * if P? --[γ, w]--&gt; Z &lt;--[βp'θ, v]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[(β*γ)†, max{w',v}]-- nS
	 * where:
	 * p' is any literal (¿p included) of p.
	 * γ does not contain p' or P? children.
	 * β cannot contain children of P?
	 * θ contains only children of P?.
	 * (β*γ)† is the q-label obtained by removing children of any q-literals that appear in β*γ
	 * ε>0 is the reaction time.
	 * w' is w or w-ε according with the kind of semantics.
	 * </pre>
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

		final StringBuilder slog = new StringBuilder();
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST))
				slog.append("labelEdgeFromObs = " + labelFromObs);
		}
		if (labelFromObs.contains(observed)) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST,
							slog.toString() + " (β*γ)† cannot be calculated because labelFromObs contains the prop " + observed + " that has to be removed.");
				}
			}
			return null;
		}

		final Label beta = new Label(labelToClean);
		beta.remove(observed);

		Label childrenOfP = this.g.getChildrenOf(nObs);
		if (childrenOfP != null && !childrenOfP.isEmpty()) {
			Label test = new Label(labelFromObs);
			test.remove(childrenOfP);
			if (!labelFromObs.equals(test)) {
				if (Debug.ON) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								slog.toString() + " (β*γ)† cannot exist because labelFromObs contains a child of " + observed
										+ " that has to be removed. The children are " + childrenOfP);
					}
				}
				return null;
			}
			beta.remove(childrenOfP);
		}

		Label betaGamma = labelFromObs.conjunctionExtended(beta);

		// remove all children of unknowns.
		removeChildrenOfUnknown(betaGamma);

		if (!betaGamma.subsumes(nS.getLabel())) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.log(Level.FINEST, slog.toString() + "(β*γ)† does not subsume labels from nodes:" + nS.getLabel());
				}
			}
			return null;
		}
		return betaGamma;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 *
	 * @param name the proposed name. If an edge with name already exists, then name is modified adding an suitable integer such that the name becomes unique
	 *            in 'g'.
	 * @param type the type of edge to create.
	 * @return an edge with a unique name.
	 */
	@Override
	LabeledIntEdgePluggable makeNewEdge(final String name, final LabeledIntEdge.ConstraintType type) {
		int i = this.g.getEdgeCount();
		String name1 = name;
		while (this.g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final LabeledIntEdgePluggable e = this.g.getEdgeFactory().get(name1);
		e.setConstraintType(type);
		return e;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	@Override
	@SuppressWarnings("deprecation")
	boolean manageParameters(final String[] args) {
		final CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			if (!this.fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (this.fOutput != null) {
				if (this.fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!this.fOutput.getName().endsWith(".cstn")) {
					this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".cstn"));
				}
				if (this.fOutput.exists()) {
					this.fOutput.delete();
				}
				try {
					this.fOutput.createNewFile();
					this.output = new PrintStream(this.fOutput);
				} catch (final IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				this.output = System.out;
			}
		} catch (final CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will report an error message.
			System.err.println(e.getMessage());
			System.err.println("java " + this.getClass().getName() + " [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-*.*.*-SNAPSHOT.jar " + this.getClass().getName() + " "
					+ parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " <graph_file_name>");
			return false;
		}
		return true;
	}

	/**
	 * On 2017-10-16 it has been simplified for working only with LP,qR0,qR3*.<br>
	 * Executes one step of the dynamic consistency check: for each edge in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule is
	 * applied
	 * two times: one time having the edge as first edge, one time having the edge as second edge.
	 * All modified or new edges are returned in the set 'edgesToCheck'.
	 * 
	 * @param edgesToCheck set of edges that have to be checked.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	@Override
	public CSTNCheckStatus oneStepDynamicConsistencyByEdges(final EdgesToCheck edgesToCheck) {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;
		final LabeledNode Z = this.g.getZ();

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "\nStart application LP+qR0+qR3*.");
			}
		}
		/**
		 * March, 06 2016 I try to apply the rules on all edges that have been modified in the previous cycle.
		 */
		EdgesToCheck newEdgesToCheck = new EdgesToCheck();
		int i = 1, n = edgesToCheck.size();
		for (LabeledIntEdge AB : edgesToCheck) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "\n***Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
				}
			}
			A = this.g.getSource(AB);
			B = this.g.getDest(AB);
			// initAndCheck does not resolve completely a qStar.
			// It is necessary to check here the edge before to consider the second edge.
			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
			if (B == Z) {
				edgeCopy = this.g.getEdgeFactory().get(AB);
				if (A.isObserver()) {
					// R0 on the resulting new values
					labelModificationR0(A, Z, AB);
				}
				labelModificationR3(A, Z, AB);
				if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
					// R0 on the resulting new values
					this.labelModificationR0(A, Z, AB);
				}
				if (!AB.equalsAllLabeledValues(edgeCopy)) {
					newEdgesToCheck.addIfZ(AB, A, B, Z, this.g);
				}
			}

			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
				C = this.g.getDest(BC);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				AC = this.g.findEdge(A, C);
				// I need to preserve the old edge to compare below
				if (AC != null) {
					edgeCopy = this.g.getEdgeFactory().get(AC);
				} else {
					AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationRule(A, B, C, AB, BC, AC);

				/**
				 * I need to clean values on AC
				 * March, 8 2016. By an experimental results, it seems that it is not necessary to clean out the new determined values. Making a qR0 qR3*
				 * cleaning, the final number of rule
				 * applications does not change!
				 */
				if (edgeCopy == null && !AC.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(AC, A, C);
					newEdgesToCheck.addIfZ(AC, A, C, Z, this.g);
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(AC)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.addIfZ(AC, A, C, Z, this.g);
				}

				if (!this.checkStatus.consistency)
					return this.checkStatus;
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			for (LabeledIntEdge CA : this.g.getInEdges(A)) {
				C = this.g.getSource(CA);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				CB = this.g.findEdge(C, B);
				// I need to preserve the old edge to compare below
				if (CB != null) {
					edgeCopy = this.g.getEdgeFactory().get(CB);
				} else {
					CB = makeNewEdge(C.getName() + "_" + B.getName(), LabeledIntEdge.ConstraintType.derived);
					edgeCopy = null;
				}

				this.labeledPropagationRule(C, A, B, CA, AB, CB);

				if (edgeCopy == null && !CB.isEmpty()) {
					// the new CB has to be added to the graph!
					this.g.addEdge(CB, C, B);
					newEdgesToCheck.addIfZ(CB, C, B, Z, this.g);
				} else if (edgeCopy != null && !edgeCopy.equalsAllLabeledValues(CB)) {
					// CB was already present and it has been changed!
					newEdgesToCheck.addIfZ(CB, C, B, Z, this.g);
				}

				if (!this.checkStatus.consistency)
					return this.checkStatus;
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.");
			}
		}
		edgesToCheck.clear();
		this.checkStatus.finished = newEdgesToCheck.size() == 0;
		if (!this.checkStatus.finished) {
			edgesToCheck.takeIn(newEdgesToCheck);
		}
		if (!this.checkStatus.consistency)
			this.checkStatus.finished = true;
		return this.checkStatus;
	}

	/**
	 * On 2017-10-16 it has been simplified for working only with LP,qR0,qR3*.<br>
	 * Executes one step of the dynamic consistency check: for each possible triangle of the network, label propagation rule is applied and, on the resulting
	 * edge, all other rules qR0--qR3* are also applied.
	 *
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	@Override
	public CSTNCheckStatus oneStepDynamicConsistencyByNode() throws WellDefinitionException {

		LabeledNode B, C;
		LabeledIntEdge AC;// AB, BC
		boolean createEdge = false;
		final LabeledNode Z = this.g.getZ();

		this.checkStatus.cycles++;

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "Start application labeled propagation rule+R0+R3.");
			}
		}
		/**
		 * March, 03 2016 I try to apply the rules on all edges making a by-row-visit to the adjacency matrix.
		 */
		for (LabeledNode A : this.g.getVertices()) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.FINER)) {
					LOG.log(Level.FINER, "Considering edges outgoing from " + A);
				}
			}
			for (LabeledIntEdge AB : this.g.getOutEdges(A)) {
				B = this.g.getDest(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				if (B == Z) {
					// Since in some graphs it is possible that there is not BC, we apply R0 and R3 to AB
					if (A.isObserver()) {
						// R0 on the resulting new values
						this.labelModificationR0(A, B, Z, AB);
					}
					this.labelModificationR3(A, B, Z, AB);
					if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						this.labelModificationR0(A, B, Z, AB);
					}
				}
				for (LabeledIntEdge BC : this.g.getOutEdges(B)) {
					C = this.g.getDest(BC);
					// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

					if (C == Z) {
						if (B.isObserver()) {
							// R0 on the resulting new values
							this.labelModificationR0(B, C, Z, BC);
						}
						this.labelModificationR3(B, C, Z, BC);
						if (B.isObserver()) {// R3 can add new values that have to be minimized.
							// R0 on the resulting new values
							this.labelModificationR0(B, C, Z, BC);
						}
					}
					// Now it is possible to propagate the labels with the standard rules
					AC = this.g.findEdge(A, C);
					// I need to preserve the old edge to compare below
					createEdge = (AC == null);
					if (createEdge) {
						AC = makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived);
					}

					this.labeledPropagationRule(A, B, C, AB, BC, AC);

					if (!this.checkStatus.consistency)
						return this.checkStatus;

					@SuppressWarnings("null")
					boolean empty = AC.isEmpty();
					if (createEdge && !empty) {
						// the new CB has to be added to the graph!
						this.g.addEdge(AC, A, C);
					} else {
						if (empty)
							continue;
					}

					if (C == Z) {
						if (A.isObserver()) {
							// R0 on the resulting new values
							this.labelModificationR0(A, C, Z, AC);
						}

						// if (!this.excludeR1R2 && C.isObservator()) {
						// // R2 on the resulting new values.
						// this.labelModificationR2(currentGraph, C, A, AC, status);
						// }

						// R3 on the resulting new values
						this.labelModificationR3(A, C, Z, AC);

						if (A.isObserver()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
							// R0 on the resulting new values
							this.labelModificationR0(A, C, Z, AC);
						}
					}
					// if (!this.excludeR1R2) {
					// // R1 on the resulting new values.
					// this.labelModificationR1(currentGraph, A, C, AC, status);
					// if (C.isObservator()) {
					// this.labelModificationR2(currentGraph, C, A, AC, status);// It should be like R0! To verify
					// // experimentally.
					// }
					// }
				}
			}
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "End application labeled propagation rule+R0+R3.");
			}
		}
		return this.checkStatus;
	}

}
