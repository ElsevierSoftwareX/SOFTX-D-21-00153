package it.univr.di.cstnu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;

import edu.uci.ics.jung.io.GraphIOException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;
import it.univr.di.labeledvalue.AbstractLabeledIntMap;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN {

	/**
	 * Simple class to represent the status of the checking algorithm during an execution.
	 *
	 * @author Roberto Posenato
	 */
	public static class CSTNCheckStatus {
		/**
		 * True if the network is consistent so far.
		 */
		public boolean consistency = true;
		/**
		 * Counters about the # of application of different rules.
		 */
		@SuppressWarnings("javadoc")
		public int cycles = 0, r0calls = 0, r3calls = 0, labeledValuePropagationcalls = 0, qAllNegLoop = 0, qSemiNegLoop = 0;
		// r1calls = 0, r2calls = 0,

		/**
		 * Execution time in nanoseconds.
		 */
		public long executionTimeNS = Constants.INT_NULL;

		/**
		 * True if no rule can be applied anymore.
		 */
		public boolean finished = false;

		@Override
		public String toString() {
			return ("The check is "
					+ (this.finished ? "" : "NOT")
					+ " finished after "
					+ this.cycles
					+ " cycle(s).\n"
					+ ((this.finished)
							? "the consistency check has determined that given network is " + (this.consistency ? "" : "NOT ") + "consistent.\n"
							: "")
					+ "Some statistics:\nRule R0 has been applied " + this.r0calls + " times.\n"
					// + "Rule R1 has been applied " + this.r1calls + " times.\n"
					// + "Rule R2 has been applied " + this.r2calls + " times.\n"
					+ "Rule R3 has been applied " + this.r3calls + " times.\n"
					+ "Rule Labeled Propagation has been applied " + this.labeledValuePropagationcalls + " times.\n"
					+ "Negative qLoops: " + this.qAllNegLoop + "\n"
					+ "Negative qLoops with positive edge: " + this.qSemiNegLoop + "\n"
					+ ((executionTimeNS != Constants.INT_NULL)
							? "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)"
							: ""));
		}
	}

	/**
	 * logger
	 */
	static Logger LOG = Logger.getLogger(CSTN.class.getName());

	/**
	 * The name for the last node.
	 */
	private static final String OmegaNodeName = "Ω";

	/**
	 * Version of the class
	 */
	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
	// static final String VERSIONandDATE = "Version 3.3 - October, 4 2016";
	static final String VERSIONandDATE = "Version  4.0 - October, 25 2016";// added management not all negative edges in a negative qLoop

	/**
	 * The name for the initial node.
	 */
	private static final String ZeroNodeName = "Z";

	/**
	 * Determine the set of edges P?-->nX where P? is an observator node and nX is the given node.
	 *
	 * @param currentGraph
	 * @param nX
	 *            the given node.
	 * @return the set of edges P?-->nX, an empty set if nX is empty o there is no observator.
	 */
	public static ObjectArraySet<LabeledIntEdge> getEdgeFromObservators(final LabeledIntGraph currentGraph, final LabeledNode nX) {
		final ObjectArraySet<LabeledIntEdge> fromObs = new ObjectArraySet<>();

		Collection<LabeledNode> obsSet = currentGraph.getObservators();
		if (obsSet.size() == 0)
			return fromObs;

		LabeledIntEdge e;
		for (final LabeledNode n : obsSet) {
			if ((e = currentGraph.findEdge(n, nX)) != null) {
				fromObs.add(e);
			}
		}
		return fromObs;
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
	static public boolean isNewLabeledValueANegativeLoop(final Label newLabel, final int value, final LabeledNode source, final LabeledNode dest,
			final LabeledIntEdge newEdge) {
		if (source.equalsByName(dest) && value < 0 && !newLabel.containsUnknown()) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Found a negative loop in the edge " + newEdge);
			}
			return true;
		}
		return false;
	}

	/**
	 * Reads a CSTN file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args
	 *            an array of {@link java.lang.String} objects.
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(final String[] args) throws FileNotFoundException, GraphIOException {
		LOG.finest("Start...");
		final CSTN cstn = new CSTN();

		if (!cstn.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTN.class.getName() + " " + CSTN.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2015, Roberto Posenato");
			return;
		}

		LOG.finest("Loading graph...");
		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstn.fInput, cstn.labeledIntValueMap);
		LabeledIntGraph g = graphMLReader.readGraph();

		LOG.finest("LabeledIntGraph loaded!");

		LOG.finest("DC Checking...");
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck(g);
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
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
			final GraphMLWriter graphWriter = new GraphMLWriter(new StaticLayout<>(g));
			try {
				graphWriter.save(g, new PrintWriter(cstn.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Simple method to determine the label betagamma' to use in rules R3.<br>
	 * See Table 1 and Table 2 ICAPS 2016 paper.
	 * 
	 * <pre>
	 * if P? --[αβ, w]--&gt; nD &lt;--[βγp, v]-- nS  and w &le; ε
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[αβγ', max{w-ε,v}]-- nS
	 * where:
	 * α, β and γ do not share any literals.
	 * α, β do not contain any literal of p.
	 * p cannot compare also in label of nodes nD and nS.
	 * γ' is obtained by removing children of p from γ.
	 * ε>0 is the reaction time.
	 * </pre>
	 * 
	 * @param currentGraph the current graph
	 * @param nS
	 * @param nD
	 * @param P
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observator
	 * @param labelToClean
	 * @return alphaBeta' if all conditions are satisfied. null otherwise.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	static public Label makeAlphaBetaGammaPrime4R3(final LabeledIntGraph currentGraph, final LabeledNode nS, final LabeledNode nD, final LabeledNode P,
			final char observed, final Label labelFromObs, Label labelToClean) {

		final StringBuilder slog = new StringBuilder();
		if (LOG.isLoggable(Level.FINEST))
			slog.append("labelEdgeFromObs = " + labelFromObs);
		if (labelFromObs.contains(observed) || nS.getLabel().contains(observed) || nD.getLabel().contains(observed)) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST,
						slog.toString() + " alphabetagamma' cannot be calculated because labelFromObs or nodes' labels contains the prop " + observed
								+ " that has to be removed.");
			}
			return null;
		}

		Label labelToCleanWOp = new Label(labelToClean);
		labelToCleanWOp.remove(observed);

		final Label alpha = labelFromObs.getSubLabelIn(labelToCleanWOp, false);
		if (alpha.containsUnknown()) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST,
						slog.toString() + " alpha contains unknow: " + alpha);
			}
			return null;
		}
		final Label beta = labelFromObs.getSubLabelIn(labelToCleanWOp, true);
		if (beta.containsUnknown()) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST,
						slog.toString() + " beta contains unknow " + beta);
			}
			return null;
		}
		final Label gamma = labelToCleanWOp.getSubLabelIn(labelFromObs, false);
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST,
					slog.toString() + " gamma: " + gamma + "\n.");
		}
		gamma.remove(currentGraph.getChildrenOf(P));

		Label alphaBetaGamma = alpha.conjunction(beta).conjunction(gamma);
		if (LOG.isLoggable(Level.FINEST))
			slog.append(", alphaBetaGamma'=" + alphaBetaGamma);

		if (alphaBetaGamma == null)
			return null;

		if (!alphaBetaGamma.subsumes(nD.getLabel().conjunction(nS.getLabel()))) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + " alphabeta' does not subsume labels from nodes:" + nD.getLabel().conjunction(nS.getLabel()));
			}
			return null;
		}
		return alphaBetaGamma;
	}

	/**
	 * Simple method to determine the label (β*γ)† to use in rules qR3.
	 * See Table 1 and Table 2 ICAPS 2016.
	 * 
	 * <pre>
	 * if P? --[γ, w]--&gt; Z &lt;--[βp'θ, v]-- nS  and w &le; 0
	 * then the constraint between Y and X is modified adding the following label:
	 * Z &lt;--[(β*γ)†, max{w-ε,v}]-- nS
	 * where:
	 * p' is any literal (¿p included) of p.
	 * γ does not contain p' or P? children.
	 * β cannot contain children of P?
	 * θ contains only children of P?.
	 * (β*γ)† is the q-label obtained by removing children of any q-literals that appear in β*γ
	 * ε>0 is the reaction time.
	 * </pre>
	 * 
	 * @param currentGraph the current graph
	 * @param nS
	 * @param Z
	 * @param P
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up).
	 * @param labelFromObs label of the edge from observator
	 * @param labelToClean
	 * @return alphaBeta1
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	static public Label makeBetaGammaDagger4qR3(final LabeledIntGraph currentGraph, final LabeledNode nS, final LabeledNode Z, final LabeledNode P,
			final char observed, final Label labelFromObs, Label labelToClean) {

		final StringBuilder slog = new StringBuilder();
		if (LOG.isLoggable(Level.FINEST))
			slog.append("labelEdgeFromObs = " + labelFromObs);
		if (labelFromObs.contains(observed)) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST,
						slog.toString() + " alphabeta1 cannot be calculated because labelFromObs contains the prop " + observed + " that has to be removed.");
			}
			return null;
		}
		char[] childrenOfP = currentGraph.getChildrenOf(P);
		if (childrenOfP != null) {
			for (char c : childrenOfP) {
				if (labelFromObs.contains(c)) {
					if (LOG.isLoggable(Level.FINEST)) {
						LOG.log(Level.FINEST,
								slog.toString() + " alphabeta1 cannot be calculated because labelFromObs contains a child of " + observed
										+ " that has to be removed: " + c);
					}
					return null;
				}
			}
		}
		final Label beta = new Label(labelToClean);
		beta.remove(observed);
		beta.remove(childrenOfP);

		Label betaGamma = labelFromObs.conjunctionExtended(beta);

		// remove all children of unknowns.
		removeChildrenOfUnknown(currentGraph, betaGamma);

		if (!betaGamma.subsumes(nS.getLabel())) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, slog.toString() + "betagamma does not subsume labels from nodes:" + nS.getLabel());
			}
			return null;
		}
		return betaGamma;
	}

	/**
	 * Simple method to determine the &alpha;' to use in rules R0.
	 * Check paper TIME15 and ICAPS 2016 about CSTN sound&amp;complete DC check.
	 *
	 * @param currentGraph the current graph
	 * @param X the destination node
	 * @param P observator node
	 * @param observed the proposition observed by observator (since this value usually is already determined before calling this method, this parameter is just
	 *            for speeding up.)
	 * @param labelFromObs label of the edge from observator
	 * @param Z
	 * @return alphaBeta1
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	static public Label makeAlphaPrime(final LabeledIntGraph currentGraph, final LabeledNode X, final LabeledNode P, final LabeledNode Z, final char observed,
			final Label labelFromObs) {

		if (X.getLabel().contains(observed))
			return null;

		final StringBuilder logStr = new StringBuilder();
		if (LOG.isLoggable(Level.FINEST))
			logStr.append("labelEdgeFromObs = " + labelFromObs);

		Label alphaPrime = new Label(labelFromObs);
		alphaPrime.remove(observed);
		alphaPrime.remove(currentGraph.getChildrenOf(currentGraph.getObservator(observed)));
		if (LOG.isLoggable(Level.FINEST))
			logStr.append(", labelWithoutPandChildren=" + alphaPrime);

		if (X == Z) {
			removeChildrenOfUnknown(currentGraph, alphaPrime);
		}
		if (LOG.isLoggable(Level.FINEST)) {
			logStr.append(", alpha'=" + alphaPrime);
		}
		if (!alphaPrime.subsumes(X.getLabel().conjunction(P.getLabel()))) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, logStr.toString() + " alphabeta1 does not subsume labels from nodes:" + X.getLabel().conjunction(P.getLabel()));
			}
			return null;
		}
		return alphaPrime;
	}

	/**
	 * Create an edge assuring that its name is unique in the graph 'g'.
	 *
	 * @param name the proposed name. If an edge with name already exists, then name is modified adding an suitable integer suche that the name becomes unique
	 *            in 'g'.
	 * @param type the type of edge to create.
	 * @param g the graph in which edge has to be added. This method cannot add the edge!
	 * @return an edge with a unique name.
	 */
	static public LabeledIntEdgePluggable makeNewEdge(final String name, final LabeledIntEdge.ConstraintType type, final LabeledIntGraph g) {
		int i = g.getEdgeCount();
		String name1 = new String(name);
		while (g.getEdge(name1) != null) {
			name1 = name + "_" + i++;
		}
		final LabeledIntEdgePluggable e = g.getEdgeFactory().create();
		e.setName(name1);
		e.setConstraintType(type);
		return e;
	}

	/**
	 * Modifies label removing all children of possibly present unknown literals in label.
	 * 
	 * @param currentGraph
	 * @param label
	 * @return the label modified.
	 */
	static public Label removeChildrenOfUnknown(LabeledIntGraph currentGraph, Label label) {
		for (final char unknownLit : label.getAllUnknown()) {
			label.remove(currentGraph.getChildrenOf(currentGraph.getObservator(unknownLit)));
		}
		return label;
	}

	/**
	 * Exclude rule R1 and R2 in the DC check. This parameter should be temporary!!!
	 */
	private boolean excludeR1R2 = true;

	/**
	 * The input file containing the CSTN graph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "input file. Input file has to be a CSTN graph in GraphML format.", metaVar = "CSTNU_file_name")
	private File fInput;

	/**
	 * Output file where to write the XML representing the minimal CSTN graph.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "CSTN_file_name")
	private final File fOutput = null;

	/**
	 * Class for representing edge labeled values.
	 */
	Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Reaction time for CSTN
	 */
	@Option(required = false, name = "-r", usage = "Reaction time. It must be >= 0.")
	private int reactionTime = 0;

	/**
	 * use Ω node
	 */
	private boolean useΩ = true;

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private final boolean versionReq = false;

	/**
	 * Default constructor. Label optimization, Reaction time is 1.
	 */
	private CSTN() {
	}

	/**
	 * Constructor for CSTN.
	 *
	 * @param reactionTime a non negative int representing the time after the setting of a proposition the system can react.
	 * @param useΩ if Ω node has to be add and used if it is missing.
	 * @param labeledIntValueMapClass the type for representing labeled value sets.
	 */
	public CSTN(final int reactionTime, final boolean useΩ, Class<? extends LabeledIntMap> labeledIntValueMapClass) {
		if (reactionTime < 0)
			throw new IllegalArgumentException("Reaction time must be >= 0.");
		this.reactionTime = reactionTime;
		this.useΩ = useΩ;
		if (labeledIntValueMapClass != null) {
			this.labeledIntValueMap = labeledIntValueMapClass;
		}
	}

	/**
	 * checkWellDefinitionProperties.
	 *
	 * @param g a {@link LabeledIntGraph} object.
	 * @return true if the g is a CSTN well defined.
	 * @throws it.univr.di.cstnu.WellDefinitionException if any.
	 */
	public boolean checkWellDefinitionProperties(final LabeledIntGraph g) throws WellDefinitionException {
		// DON'T use CSTNU.checkWellDefinitionProperties method because it is necessary to pass local reaction time to checkWellDefinition2Property
		boolean flag = false;
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "Checking if graph is well defined...");
		}
		for (final LabeledIntEdge e : g.getEdges()) {
			flag = CSTNU.checkWellDefinition1Property(g.getSource(e), g.getDest(e), e, false);
			flag = flag && CSTNU.checkWellDefinition3Property(g, e);
		}
		for (final LabeledNode node : g.getNodes()) {
			flag = flag && CSTNU.checkWellDefinition2Property(g, node, reactionTime, false);
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, ((flag) ? "done: all is well defined.\n" : "done: something is wrong. Not well-defined graph!\n"));
		}
		return flag;
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance.
	 *
	 * @param g the original graph that has to be checked. In any case, the graph is modified by the minimization process. If the check is successful, all
	 *            constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * @return the final status of the checking with some statistics.
	 * @throws it.univr.di.cstnu.WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties).
	 */
	public CSTNCheckStatus dynamicConsistencyCheck(final LabeledIntGraph g) throws WellDefinitionException {
		if (g == null)
			throw new IllegalArgumentException("Input CSTN is null");

		try {
			this.initAndCheck(g);
		} catch (final IllegalArgumentException e) {
			throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
		}
		return dynamicConsistencyCheckWOInit(g);
	}

	/**
	 * Checks the dynamic consistency of a CSTN instance without initialize the network.<br>
	 * This method can be used ONLY when it is guaranteed that the network is already initialize by method {@link #initAndCheck(LabeledIntGraph)}.
	 * In case of doubts, use {@link #dynamicConsistencyCheck(LabeledIntGraph)}.
	 *
	 * @param g the original graph that has to be checked. In any case, the graph is modified by the minimization process. If the check is successful, all
	 *            constraints to node Z in g are minimized; otherwise, g contains a negative cycle at least.
	 * @return the final status of the checking with some statistics.
	 */
	public CSTNCheckStatus dynamicConsistencyCheckWOInit(final LabeledIntGraph g) {
		final CSTNCheckStatus status = new CSTNCheckStatus();

		final String originalName = g.getName();
		LabeledIntGraph nextGraph = new LabeledIntGraph(g, labeledIntValueMap);
		nextGraph.setName("Next graph");

		ObjectArraySet<LabeledIntEdge> edgesToCheck = new ObjectArraySet<>(nextGraph.getEdges());

		final int propositionN = nextGraph.getPropositions().size();
		final int nodeN = nextGraph.getVertexCount();
		// TODO: trovare il numero giusto di iterazioni
		// final int maxCycles = propositionN * 10;
		final int maxCycles = (int) (Math.pow(nodeN, 3) * Math.pow(2, propositionN));
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "The maximum number of possible cycles is " + maxCycles);
		}

		int i;
		// final boolean hierarchyMap = LabeledIntEdge.labeledValueMapFactory.createLabeledIntMap().getClass().equals(LabeledIntHierarchyMap.class);

		Instant startInstant = Instant.now();
		for (i = 1; (i <= maxCycles) && status.consistency && !status.finished; i++) {
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
			}
			this.oneStepDynamicConsistencyByEdges(nextGraph, edgesToCheck, status);

			if (status.consistency && !status.finished) {
				if (LOG.isLoggable(Level.FINE)) {
					StringBuilder log = new StringBuilder();
					log.append("During the check n. " + i + ", " + edgesToCheck.size()
							+ " edges have been add/modified. Check has to continue.\nDetails of only modified edges having values:\n");
					for (LabeledIntEdge e : edgesToCheck) {
						if (e.size() == 0)
							continue;
						log.append("Edge " + e + "\n");
					}
					LOG.log(Level.FINE, log.toString());
				}
			}
			if (LOG.isLoggable(Level.FINE)) {
				LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
			}
		}

		Instant endInstant = Instant.now();
		status.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!status.consistency) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an inconsistency.\nStatus: " + status);
				LOG.log(Level.FINER, "Final inconsistent graph: " + nextGraph);
			}
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		if ((i > maxCycles) && !status.finished) {
			if (LOG.isLoggable(Level.WARNING)) {
				LOG.log(Level.WARNING, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + status);
				LOG.log(Level.FINER, "Last determined graph: " + nextGraph);
			}
			status.consistency = status.finished;
			g.takeIn(nextGraph);
			g.setName(originalName);
			return status;
		}

		// consistent && finished
		if (LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: " + status);
		}
		// Just an experiment to find the most heavy edge
		// LabeledIntEdge max = new LabeledIntEdge("guard",LabeledIntEdge.ConstraintType.internal, Label.emptyLabel, 0, false);
		// for (LabeledIntEdge e : currentGraph.getEdgesArray()) {
		// if (e.labeledValueSet().size() > max.labeledValueSet().size()) {
		// max = e;
		// }
		// }
		// System.out.println("Edge with maximum set of labels: "+max);
		g.copyCleaningRedundantLabels(nextGraph);
		g.setName(originalName);
		return status;
	}

	// /**
	// * Checks the dynamic consistency of a CSTN instance and, if the instance is consistent, determines all the minimal ranges for the constraints. <br>
	// * All label containing proposition that cannot be evaluated at run time are removed.<br>
	// * This method tries to execute only LP with R0 and R3 for at most |P| cycles.
	// *
	// * @param g
	// * the original graph that has to be checked. If the check is successful, g is modified and it contains all minimized constraints; otherwise, it
	// * is
	// * not modified.
	// * @return the final status of the checking with some statistics.
	// * @throws it.univr.di.cstnu.WellDefinitionException
	// * if the nextGraph is not well defined (does not observe all well definition properties). If this
	// * exception occurs, then there is a problem in the rules coding.
	// */
	// public CSTNCheckStatus dynamicConsistencyCheckByNode(final LabeledIntGraph g) throws WellDefinitionException {
	//
	// final CSTNCheckStatus status = new CSTNCheckStatus();
	// if (g == null)
	// return status;
	//
	// final String originalName = g.getName();
	// LabeledIntGraph currentGraph, nextGraph;
	// currentGraph = new LabeledIntGraph(g, labeledIntValueMap);
	// currentGraph.setName("Current graph");
	// try {
	// this.initAndCheck(currentGraph);
	// } catch (final IllegalArgumentException e) {
	// throw new IllegalArgumentException("The CSTN graph has a problem and it cannot be initialize: " + e.getMessage());
	// } catch (Exception e) {
	// LOG.log(Level.INFO, "An interrupt requested catched during the initAndCheck phase.");
	// }
	//
	// nextGraph = new LabeledIntGraph(currentGraph, labeledIntValueMap);
	// nextGraph.setName("New graph");
	//
	// final int propositionN = currentGraph.getPropositions().size();
	// final int nodeN = currentGraph.getVertexCount();
	// // final int maxCycles = propositionN * 10;
	// final int maxCycles = (int) (Math.pow(nodeN, 3) * Math.pow(2, propositionN));
	// if (LOG.isLoggable(Level.FINER)) {
	// LOG.log(Level.FINER, "The maximum number of possible cycles is " + maxCycles);
	// }
	//
	// int i;
	// // final boolean hierarchyMap = LabeledIntEdge.labeledValueMapFactory.createLabeledIntMap().getClass().equals(LabeledIntHierarchyMap.class);
	//
	// final long startTime = System.nanoTime();
	// for (i = 1; (i <= maxCycles) && status.consistency && !status.finished; i++) {
	// if (LOG.isLoggable(Level.FINE)) {
	// LOG.log(Level.FINE, "*** Start Main Cycle " + i + "/" + maxCycles + " ***");
	// }
	// this.oneStepDynamicConsistencyByNodeOpt(nextGraph, status);
	// status.finished = currentGraph.hasSameEdgesOf(nextGraph);
	//
	// if (status.consistency && !status.finished) {
	// currentGraph.copy(nextGraph);
	// // HierarchyMap has a small bug (that I have no time to find!!!) and it is possible that some values are dangling. Graph re-copy remove such
	// // values;
	// // With modest confidence, fixed!
	// // if (hierarchyMap) nextGraph.copy(currentGraph);
	// nextGraph.setName("nextGraph");
	// currentGraph.setName("currentGraph");
	// }
	// if (LOG.isLoggable(Level.FINE)) {
	// LOG.log(Level.FINE, "*** End Main Cycle " + i + "/" + maxCycles + " ***\n\n");
	// }
	// }
	// status.executionTimeNS = (System.nanoTime() - startTime);
	//
	// if (!status.consistency) {
	// if (LOG.isLoggable(Level.INFO)) {
	// LOG.log(Level.INFO, "After " + (i - 1) + " cycle, found an inconsistency.\nStatus: " + status);
	// LOG.log(Level.FINER, "Final inconsistent graph: " + nextGraph);
	// }
	// g.takeIn(nextGraph);
	// g.setName(originalName);
	// return status;
	// }
	//
	// if ((i > maxCycles) && !status.finished) {
	// if (LOG.isLoggable(Level.INFO)) {
	// LOG.log(Level.INFO, "The maximum number of cycle (+" + maxCycles + ") has been reached!\nStatus: " + status);
	// LOG.log(Level.FINER, "Last determined graph: " + nextGraph);
	// }
	// status.consistency = status.finished;
	// g.takeIn(nextGraph);
	// g.setName(originalName);
	// return status;
	// }
	//
	// // consistent && finished
	// if (LOG.isLoggable(Level.INFO)) {
	// LOG.log(Level.INFO, "Stable state reached. Number of cycles: " + (i - 1) + " over the maximum allowed " + maxCycles + ".\nStatus: "
	// + status);
	// }
	// // Just an experiment to find the most heavy edge
	// // LabeledIntEdge max = new LabeledIntEdge("guard",LabeledIntEdge.ConstraintType.internal, Label.emptyLabel, 0, false);
	// // for (LabeledIntEdge e : currentGraph.getEdgesArray()) {
	// // if (e.labeledValueSet().size() > max.labeledValueSet().size()) {
	// // max = e;
	// // }
	// // }
	// // System.out.println("Edge with maximum set of labels: "+max);
	// nextGraph.copyCleaningRedundantLabels(currentGraph);
	// // Put all data structures of currentGraph in g
	// g.takeIn(nextGraph);
	// g.setName(originalName);
	// return status;
	// }

	/**
	 * @param set
	 * @return old value
	 */
	public boolean excludeR1R2(boolean set) {
		boolean old = this.excludeR1R2;
		excludeR1R2 = set;
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "New value for excludeR1R2: " + old + " becomes " + set);
		}
		return old;
	}

	/**
	 * Help method to initialize and check the CSTN represented by graph g. The {@link #dynamicConsistencyCheck(LabeledIntGraph)} calls this method before
	 * to execute the check. If some constraints of the network does not observe well-definition properties AND they can be adjusted, then the method fixes them
	 * and logs such fixes in log system at WARNING level.
	 * 
	 * @param g a {@link it.univr.di.cstnu.graph.LabeledIntGraph} object.
	 * @return true if the graph is a well formed CSTN.
	 * @throws WellDefinitionException if the initial graph is not well defined.
	 */
	public boolean initAndCheck(final LabeledIntGraph g) throws WellDefinitionException {
		if (g == null)
			throw new WellDefinitionException("The graph is null!");

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "Starting initial well definition check.\nReaction time: " + this.reactionTime);
		}
		g.clearCache();

		for (final LabeledIntEdge e : g.getEdges()) {

			if (LOG.isLoggable(Level.FINEST)) {
				LOG.log(Level.FINEST, "Initial Checking edge e: " + e);
			}

			final LabeledNode s = g.getSource(e);
			final LabeledNode d = g.getDest(e);

			// WD1 is checked and adjusted here
			try {
				CSTNU.checkWellDefinition1Property(s, d, e, true);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (e.size() == 0 && e.lowerLabelSize() == 0 && e.upperLabelSize() == 0) {
				// The merge removed labels...
				g.removeEdge(e);
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "Labels fixing on edge " + e + " removed all labels. Edge " + e + " has been removed.");
				}
				continue;
			}

			// WD3 property
			try {
				CSTNU.checkWellDefinition3Property(g, e);
			} catch (final WellDefinitionException ex) {
				throw new IllegalArgumentException("Edge " + e + " has the following problem: " + ex.getMessage());
			}

			if (e.isContingentEdge()) {
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							"Found a contingent edge: " + e + ". The consistency check does not difference between ordinary and contingent edges.");
				}
			}
		}

		// Init two useful structures
		g.getPropositions();

		final Collection<LabeledNode> nodeSet = g.getVertices();
		for (final LabeledNode node : nodeSet) {
			// Check that observation node has no in the proposition observed its label!
			final char obs = node.getPropositionObserved();
			final Label label = node.getLabel();
			if (obs != Constants.UNKNOWN) {
				if (label.contains(obs)) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "Literal '" + obs + "' cannot be part of the label '" + label + "' of the observation node '" + node.getName()
								+ "'. Removed!");
					}
				}
				label.remove(obs);
			}

			// WD2 is checked and adjusted here
			try {
				CSTNU.checkWellDefinition2Property(g, node, reactionTime, true);
			} catch (final WellDefinitionException ex) {
				throw new WellDefinitionException("WellDefinition 2 problem found at node " + node + ": " + ex.getMessage());
			}
		}

		// Start of well definition and properties about nodes (w.r.t. the Z node)!
		LabeledNode Z = g.getZ();
		boolean zOrΩAdded = false;
		if (Z == null) {
			Z = g.getNode(CSTN.ZeroNodeName);
			if (Z == null) {
				// We add by authority!
				Z = new LabeledNode(CSTN.ZeroNodeName);
				Z.setX(0.0);
				Z.setY(0.0);
				g.addVertex(Z);
				zOrΩAdded = true;
				if (LOG.isLoggable(Level.WARNING))
					LOG.log(Level.WARNING, "No " + CSTN.ZeroNodeName + " node found: added!");
			}
			g.setZ(Z);
		} else {
			if (!Z.getLabel().isEmpty()) {
				if (LOG.isLoggable(Level.WARNING))
					LOG.log(Level.WARNING, "In the graph, Z node has not empty label. Label removed!");
				Z.setLabel(Label.emptyLabel);
			}
		}

		LabeledNode Ω = g.getΩ();
		if (this.useΩ) {
			Ω = g.getNode(OmegaNodeName);
			if (Ω == null) {
				// We add by authority!
				Ω = new LabeledNode(OmegaNodeName);
				Ω.setX(700.0);
				Ω.setY(700.0);
				g.addVertex(Ω);
				zOrΩAdded = true;
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING, "No " + OmegaNodeName + " node found: added!");
				}
				g.setΩ(Ω);
			} else {
				if (!Ω.getLabel().isEmpty()) {
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING, "In the graph, Ω node has not empty label. Label removed!");
					}
					Ω.setLabel(Label.emptyLabel);
				}
			}
		}

		// Now I assuring that each node has a edge to Z and Ω has an edge to the considered node.
		for (final LabeledNode node : nodeSet) {
			if (node != Z) {
				LabeledIntEdge e = g.findEdge(node, Z);
				if (e == null) {
					e = makeNewEdge(node.getName() + "_" + Z.getName(), LabeledIntEdge.ConstraintType.derived, g);
					g.addEdge(e, node, Z);
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING,
								"It is necessary to add a constraint to guarantee that node '" + node.getName() + "' occurs after node '" + Z.getName() + "'.");
					}
				}
				e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be after Z!
			}
			if (this.useΩ && node != Ω) {
				LabeledIntEdge e = g.findEdge(Ω, node);
				if (e == null) {
					e = CSTN.makeNewEdge(Ω.getName() + "_" + node.getName(), LabeledIntEdge.ConstraintType.derived, g);
					g.addEdge(e, Ω, node);
					if (LOG.isLoggable(Level.WARNING)) {
						LOG.log(Level.WARNING,
								"It is necessary to add a constraint to guarantee that node 'Ω' occurs after '" + node.getName() + "' node.");
					}
				}
				e.mergeLabeledValue(node.getLabel(), 0);// in any case, all nodes must be before Ω!
			}
		}

		// It is better to normalize with respect to the label modification rules before starting the DC check.
		// Such normalization assures only that redundant labels are removed (w.r.t. R0, R2)
		// Qstar are not solved by this normalization!
		// Moreover, we calculate the sum of all negative values and the sum of all positive values to set an upper bound between Z and Ω
		final CSTNCheckStatus status = new CSTNCheckStatus();
		int maxPosSum = 0, maxNegSum = 0;
		try {
			for (final LabeledIntEdge e : g.getEdges()) {
				//
				int min = 0, max = 0;
				for (Object2IntMap.Entry<Label> entry : e.labeledValueSet()) {
					int v = entry.getIntValue();
					if (v < min)
						min = v;
					if (v > max)
						max = v;
				}
				maxPosSum += max;
				maxNegSum += min;

				final LabeledNode s = g.getSource(e);
				final LabeledNode d = g.getDest(e);

				// Normalize with respect to R0--R3
				if (s.isObservator()) {
					this.labelModificationR0(g, s, d, Z, e, status);
				}
				// if (!this.excludeR1R2) {
				// if (d.isObservator()) {
				// this.labelModificationR2(g, d, s, e, status);
				// }
				// this.labelModificationR1(g, s, d, e, status);
				// if (d.isObservator()) {
				// // again because R1 could have add a new value;
				// this.labelModificationR2(g, d, s, e, status);
				// }
				// }
				this.labelModificationR3(g, s, d, Z, e, status);
				if (s.isObservator()) {
					// again because R3 could have add a new value;
					this.labelModificationR0(g, s, d, Z, e, status);
				}
			}
		} catch (IllegalStateException ex) {
			String logMsg = "Graph is not well defined:\n" + ex.getMessage();
			LOG.severe(logMsg);
			throw new WellDefinitionException(logMsg);
		}
		if (LOG.isLoggable(Level.INFO)) {
			LOG.log(Level.INFO, "A preliminary application of label modification rules has been done: " + status.toString());
		}

		// Set an upper bound between Z and Ω if it is not present
		if (this.useΩ && zOrΩAdded) {
			LabeledIntEdge e = g.findEdge(Z, Ω);
			if (e == null) {
				e = CSTN.makeNewEdge(Z.getName() + "_" + Ω.getName(), LabeledIntEdge.ConstraintType.derived, g);
				g.addEdge(e, Z, Ω);
				if (LOG.isLoggable(Level.WARNING)) {
					LOG.log(Level.WARNING,
							"It is necessary to add an upper bound constraint between Z and Ω to guarantee the algorithm termination.");
				}
			}
			maxNegSum = -maxNegSum;
			maxPosSum = (maxPosSum < maxNegSum) ? maxNegSum : maxPosSum;
			e.mergeLabeledValue(Label.emptyLabel, maxPosSum);
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Added the upper bound value " + maxPosSum + " between Z and Ω");
			}
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "Initial well definition check done!");
		}
		return true;
	}

	/**
	 * Applies rule R0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 *
	 * <pre>
	 * R0:
	 * P? --[α p, w]--&gt; X changes in P? --[α', w]--&gt; X when w < ε
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * ε>0 is the reaction time.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param P the observation node
	 * @param X the other node
	 * @param Z
	 * @param PX the edge connecting P? ---&gt; X
	 * @param status
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0(final LabeledIntGraph currentGraph, final LabeledNode P, final LabeledNode X, final LabeledNode Z, final LabeledIntEdge PX,
			final CSTNCheckStatus status) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false, mergeStatus;
		final char p = P.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
			}
			return false;
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: start.");
		}
		if (X.getLabel().contains(p)) {// Table 2 ICAPS
			// if (LOG.isLoggable(Level.FINER)) {
			// LOG.log(Level.FINER, "R0: Proposition " + p + " is present in the X label '" + X.getLabel() + ". R0 cannot be applied.");
			// }
			return false;
		}

		final ObjectSet<Label> obsXLabelSet = PX.getLabeledValueMap().keySet();

		for (final Label l : obsXLabelSet) {
			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = PX.getValue(l);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w >= this.reactionTime) {// Table 1 and 2 ICAPS paper
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(currentGraph, X, P, Z, p, l);
			if (alphaPrime == null) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (LOG.isLoggable(Level.FINER)) {
				logMessage = "R0 simplifies a label of edge " + PX.getName()
						+ ":\nsource: " + P.getName() + " ---" + CSTNU.pairAsString(l, w) + "---> " + X.getName()
						+ "\nresult: " + P.getName() + " ---" + CSTNU.pairAsString(alphaPrime, w) + "---> " + X.getName();
			}

			PX.putLabeledValueToRemovedList(l, w);
			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
			status.r0calls++;
			ruleApplied = true;
			mergeStatus = PX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, logMessage);
			}
			if (CSTN.isNewLabeledValueANegativeLoop(alphaPrime, w, P, X, PX)) {
				status.consistency = false;
				status.finished = true;
				return ruleApplied;
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: end.");
		}

		return ruleApplied;
	}

	/**
	 * Rule R3 applies the following labels modification:
	 *
	 * <pre>
	 * if P? --[α, w]--&gt; nD &lt;--[βθp', v]-- nS  and w &le; ε
	 * then the constraint between Y and X is modified adding the following label:
	 * nD &lt;--[(α*β)', max{w-ε,v}]-- nS
	 * where:
	 * α does not contain any literal of p and P? children.
	 * β can contain q-literal but not literals of p and its children
	 * θ contains children of p.
	 * p' is any literal (¿p included) of p.
	 * (α*β)' is the extended conjunction without any children of possible q-literals in it.
	 * ε>0 is the reaction time.
	 * </pre>
	 *
	 * @param currentGraph
	 * @param nS node
	 * @param nD node
	 * @param eSD LabeledIntEdge containing the constrain to modify
	 * @param Z
	 * @param status
	 * @return true if a rule has been applied.
	 */
	// Visibility is package because there is Junit Class test that checks this method.
	boolean labelModificationR3(final LabeledIntGraph currentGraph, final LabeledNode nS, final LabeledNode nD, final LabeledNode Z, final LabeledIntEdge eSD,
			final CSTNCheckStatus status) {

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R3: start.");
		}
		boolean ruleApplied = false;

		ObjectArraySet<LabeledIntEdge> Obs2nDEdges = CSTN.getEdgeFromObservators(currentGraph, nD);
		if (Obs2nDEdges.isEmpty())
			return false;

		final ObjectSet<Label> SDLabelSet = eSD.getLabeledValueMap().keySet();
		for (final LabeledIntEdge eObsD : Obs2nDEdges) {
			final LabeledNode nObs = currentGraph.getSource(eObsD);

			if (nObs.equalsByName(nS))
				continue;
			final char p = nObs.getPropositionObserved();

			if (nS.getLabel().contains(p) || nD.getLabel().contains(p)) {// Table 2 ICAPS
				// if (LOG.isLoggable(Level.FINEST)) {
				// LOG.log(Level.FINEST, "R3: Proposition " + p + " is present in the nS label '" + nS.getLabel() + " or nD label " + nD.getLabel()
				// + ". R3 cannot be applied.");
				// }
				continue;
			}
			for (final Object2IntMap.Entry<Label> entryObsD : eObsD.labeledValueSet()) {
				final int w = entryObsD.getIntValue();
				if (w > this.reactionTime || (this.reactionTime == 0 && w == 0 && nD == Z)) {// If reactionTime==0, then it has to be considered instantaneous
					// reaction, so Table 1 ICAPS (qR*3) has to be also considered.
					// in case (this.reactionTime == 0 && w == 0 && nD==Z), it means that P? is executed at 0. So, even if v==0 (it cannot be v>0),
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

					final int max = Math.max(w - this.reactionTime, v);

					Label newLabel = (nD != Z) ? makeAlphaBetaGammaPrime4R3(currentGraph, nS, nD, nObs, p, ObsDLabel, SDLabel)
							: makeBetaGammaDagger4qR3(currentGraph, nS, Z, nObs, p, ObsDLabel, SDLabel);
					if (newLabel == null) {
						continue;
					}

					eSD.putLabeledValueToRemovedList(SDLabel, v);
					ruleApplied = eSD.mergeLabeledValue(newLabel, max);
					if (ruleApplied) {
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, "R3 adds a labeled value to edge " + eSD.getName() + ":\n"
									+ "source: " + nObs.getName() + " ---" + CSTNU.pairAsString(ObsDLabel, w) + "---> " + nD.getName()
									+ " <---" + CSTNU.pairAsString(SDLabel, v) + "--- " + nS.getName()
									+ "\nresult: add " + nD.getName() + " <---" + CSTNU.pairAsString(newLabel, max) + "--- " + nS.getName());
						}
						status.r3calls++;
					}

					if (CSTN.isNewLabeledValueANegativeLoop(newLabel, w, nS, nD, eSD)) {
						status.consistency = false;
						status.finished = true;
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
	 * Applies the labeled propagation rule:
	 *
	 * <pre>
	 * if A --[l1, x]--&gt; B --[l2, y]--&gt; C, then A --[l1l2, x+y]--&gt; C
	 * 
	 * l1l2 is the extended conjunction when 
	 * <ol> 
	 * <li> x &ge; 0, y &ge; 0
	 * <li> x &lt; 0, y anything
	 * </ol>
	 * standard conjunction otherwise.
	 * In case l1l2 contains 'unknown literals' (¿p for example), then l1l2 has to not contain any children of such unknown literals.
	 * 
	 * If A==C and x+y &lt; ε, then
	 * - if l1l2 does not contain ¿ literals, the network is not DC
	 * - if l1l2 contains ¿ literals, the x+y becomes -∞
	 * 
	 * Be careful, in order to propagate correctly possibly -∞ self-loop, it is necessary call this method also for triple like with nodes A == B or B==C!
	 * </pre>
	 *
	 * @param currentGraph the originating graph.
	 * @param A
	 * @param B
	 * @param C
	 * @param AB
	 * @param BC
	 * @param Z the Z node in currentGraph (only to speed-up)
	 * @param status
	 * @return true if a reduction has been applied.
	 */
	boolean labelPropagationRule(final LabeledIntGraph currentGraph, final LabeledNode A, final LabeledNode B, final LabeledNode C,
			final LabeledIntEdge AB, final LabeledIntEdge BC, final LabeledNode Z, final CSTNCheckStatus status) {
		// Visibility is package because there is Junit Class test that checks this method.

		LabeledIntEdge AC = currentGraph.findEdge(A, C);
		if (AC == null) {
			AC = CSTN.makeNewEdge(A.getName() + "_" + C.getName(), LabeledIntEdge.ConstraintType.derived, currentGraph);
			currentGraph.addEdge(AC, A, C);
		}

		boolean ruleApplied = false;
		for (final Object2IntMap.Entry<Label> ABEntry : AB.labeledValueSet()) {
			final Label labelAB = ABEntry.getKey();

			/**
			 * If there is a self loop containing a (-∞, q*), it must be propagated!
			 */
			final int x = ABEntry.getIntValue();
			for (final Object2IntMap.Entry<Label> BCEntry : BC.labeledValueSet()) {
				final Label labelBC = BCEntry.getKey();
				final int y = BCEntry.getIntValue();
				int sum = AbstractLabeledIntMap.sumWithOverflowCheck(x, y);

				//The following check allows the propagation of -∞ only through negative edges.
//				final boolean isQLabelAdmitted = (x < reactionTime && y < 0 && (!labelAB.containsUnknown() || x == Constants.INT_NEG_INFINITE))
//						|| (sum < 0 && y >= reactionTime && x != Constants.INT_NEG_INFINITE && !labelBC.containsUnknown());
				final boolean isQLabelAdmitted = (x < reactionTime) && (sum<=0) ;//|| (x >= reactionTime && y >= reactionTime);
				
				final Label newLabelAC = (isQLabelAdmitted) ? labelAB.conjunctionExtended(labelBC) : labelAB.conjunction(labelBC);
				if (newLabelAC == null) {
					continue;
				}
				if (isQLabelAdmitted) {
					// newLabelAC can contain ¿ literals
					// It is necessary to remove all children of unknown literals (TIME15)
					removeChildrenOfUnknown(currentGraph, newLabelAC);
				}

				int oldValue = AC.getValue(newLabelAC);
				if (A == C) {
					if (sum >= this.reactionTime) {
						// it would be a redundant edge
						continue;
					}
					// sum is negative!
					if (!newLabelAC.containsUnknown()) {
						AC.mergeLabeledValue(newLabelAC, sum);
						if (LOG.isLoggable(Level.FINER)) {
							LOG.log(Level.FINER, "***\nFound a negative loop " + CSTNU.pairAsString(newLabelAC, sum) + " in the edge  " + AC + "\n***");
						}
						status.consistency = false;
						status.finished = true;
						status.labeledValuePropagationcalls++;
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
				if (LOG.isLoggable(Level.FINER)) {
					log = "Label Propagation Rule applied to edge " + AC.getName()
							+ ":\nsource: "
							+ A.getName() + " ---" + CSTNU.pairAsString(labelAB, x) + "---> " + B.getName() + " ---" + CSTNU.pairAsString(labelBC, y) + "---> "
							+ C.getName()
							+ "\nresult: "
							+ A.getName() + " ---" + CSTNU.pairAsString(newLabelAC, sum) + "---> " + C.getName()
							+ "; old value: " + Constants.formatInt(oldValue);
				}

				if (AC.mergeLabeledValue(newLabelAC, sum)) {
					ruleApplied = true;
					status.labeledValuePropagationcalls++;
					LOG.log(Level.FINER, log);
					if (sum == Constants.INT_NEG_INFINITE && A == C && x != Constants.INT_NEG_INFINITE && y != Constants.INT_NEG_INFINITE) {
						if (y >= reactionTime)
							status.qSemiNegLoop++;
						else
							status.qAllNegLoop++;
					}
				}
			}
		}
		if (AC.labeledValueSet().isEmpty()) {
			// This occurs only when a new edge is added at the start of this method but, then, no value can be added.
			currentGraph.removeEdge(AC);
			AC = null;
		}
		return ruleApplied;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 *
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	private boolean manageParameters(final String[] args) {
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
			System.err.println("java CSTN [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU-1.7.1-SNAPSHOT.jar" + parser.printExample(OptionHandlerFilter.REQUIRED)
					+ " <CSTN_file_name>");
			return false;
		}
		return true;
	}

	/**
	 * Executes one step of the dynamic consistency check: for each edge in edgesToCheck, rules R0--R3 are applied on it and, then, label propagation rule is
	 * applied
	 * two times: one time having the edge as first edge, one time having the edge as second edge.
	 * All modified or new edges are returned in the set 'edgesToCheck'.
	 *
	 * @param currentGraph the current graph. At the end of the procedure, it will contain the results of reductions.
	 * @param status the record where to store statistics and exit status of the execution. BE CAREFULL, this procedure cannot verified if the DC is finished or
	 *            not. So, the status.finished field is not update by this procedure.
	 * @param edgesToCheck set of edges that have to be checked.
	 * @return the update status (it is for convenience. It is not necessary because return the same parameter status).
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByEdges(final LabeledIntGraph currentGraph, final ObjectArraySet<LabeledIntEdge> edgesToCheck,
			final CSTNCheckStatus status) {

		LabeledNode A, B, C;
		LabeledIntEdge AC, CB, edgeCopy;
		final LabeledNode Z = currentGraph.getZ();

		status.cycles++;

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "\nStart application labeled propagation rule+R0+R3.");
		}
		/**
		 * March, 06 2016 I try to apply the rules on all edges that have been modified in the previous cycle.
		 */
		ObjectArraySet<LabeledIntEdge> newEdgesToCheck = new ObjectArraySet<>();
		int i = 1, n = edgesToCheck.size();
		for (LabeledIntEdge AB : edgesToCheck) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "\n***Considering edge " + (i++) + "/" + n + ": " + AB + "\n");
			}
			A = currentGraph.getSource(AB);
			B = currentGraph.getDest(AB);
			// initAndCheck does not resolve completely a qStar.
			// It is necessary to check here the edge before to consider the second edge.
			// If the second edge is not present, in any case the current edge has been analyzed by R0 and R3 (qStar can be solved)!
			edgeCopy = currentGraph.getEdgeFactory().create(AB);
			if (A.isObservator()) {
				// R0 on the resulting new values
				labelModificationR0(currentGraph, A, B, Z, AB, status);
			}
			labelModificationR3(currentGraph, A, B, Z, AB, status);
			if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// R0 on the resulting new values
				this.labelModificationR0(currentGraph, A, B, Z, AB, status);
			}
			if (!AB.equalsLabeledValues(edgeCopy)) {
				newEdgesToCheck.add(AB);
			}

			/**
			 * Step 1/2: Make all propagation considering edge AB as first edge.<br>
			 * A-->B-->C
			 */
			for (LabeledIntEdge BC : currentGraph.getOutEdges(B)) {
				C = currentGraph.getDest(BC);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				edgeCopy = currentGraph.findEdge(A, C);
				if (edgeCopy != null) {
					// I need to preserve the old edge to compare below
					edgeCopy = currentGraph.getEdgeFactory().create(edgeCopy);
				}

				this.labelPropagationRule(currentGraph, A, B, C, AB, BC, Z, status);

				if (!status.consistency)
					return status;
				AC = currentGraph.findEdge(A, C);
				if (AC == null) {
					continue;
				}

				/**
				 * I need to clean values on AC
				 * March, 8 2016 By an experimental results, it seems that the following clean code is not necessary. Without it, the final number of rule
				 * applications does not change!
				 */
				// if (A.isObservator()) {
				// // R0 on the resulting new values
				// this.labelModificationR0(currentGraph, A, C, AC, status);
				// }
				//
				// // if (!this.excludeR1R2 && C.isObservator()) {
				// // // R2 on the resulting new values.
				// // this.labelModificationR2(currentGraph, C, A, AC, status);
				// // }
				//
				// // R3 on the resulting new values
				// this.labelModificationR3(currentGraph, A, C, AC, status);
				// if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
				// // R0 on the resulting new values
				// this.labelModificationR0(currentGraph, A, C, AC, status);
				// }
				//
				// // if (!this.excludeR1R2) {
				// // // R1 on the resulting new values.
				// // this.labelModificationR1(currentGraph, A, C, AC, status);
				// // if (C.isObservator()) {
				// // this.labelModificationR2(currentGraph, C, A, AC, status);// It should be like R0! To verify
				// // // experimentally.
				// // }
				// // }
				if (edgeCopy == null || !AC.equalsLabeledValues(edgeCopy)) {
					newEdgesToCheck.add(AC);
				}
			}

			/**
			 * Step 2/2: Make all propagation considering edge AB as second edge.<br>
			 * C-->A-->B
			 */
			for (LabeledIntEdge CA : currentGraph.getInEdges(A)) {
				C = currentGraph.getSource(CA);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞
				edgeCopy = currentGraph.findEdge(C, B);
				if (edgeCopy != null) {
					// I need to preserve the old edge to compare below
					edgeCopy = currentGraph.getEdgeFactory().create(edgeCopy);
				}

				this.labelPropagationRule(currentGraph, C, A, B, CA, AB, Z, status);
				if (!status.consistency)
					return status;
				CB = currentGraph.findEdge(C, B);
				if (CB == null) {
					continue;
				}
				if (edgeCopy == null || !CB.equalsLabeledValues(edgeCopy)) {
					newEdgesToCheck.add(CB);
				}
			}
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3.");
		}
		edgesToCheck.clear();
		status.finished = newEdgesToCheck.size() == 0;
		if (!status.finished) {
			edgesToCheck.addAll(newEdgesToCheck);
		}
		if (!status.consistency)
			status.finished = true;
		return status;
	}

	/**
	 * Executes one step of the dynamic consistency check: for each possible triangle of the network, label propagation rule is applied and, on the resulting
	 * edge, all other rules R0--R3 are also applied.
	 *
	 * @param currentGraph the current graph. At the end of the procedure, it will contain the results of reductions.
	 * @param status the record where to store statistics and exit status of the execution. BE CAREFULL, this procedure cannot verified if the DC is finished
	 *            or
	 *            not. So, the status.finished field is not update by this procedure.
	 * @return the update status (for convenience. It is not necessary because return the same parameter status).
	 * @throws WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties). If this exception occurs, then
	 *             there is a problem in the rules coding.
	 */
	public CSTNCheckStatus oneStepDynamicConsistencyByNode(final LabeledIntGraph currentGraph, final CSTNCheckStatus status) throws WellDefinitionException {

		LabeledNode B, C;
		LabeledIntEdge AC;// AB, BC

		final LabeledNode Z = currentGraph.getZ();

		status.cycles++;

		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "");
			LOG.log(Level.FINER, "Start application labeled propagation rule+R0+R3.");
		}
		/**
		 * March, 03 2016 I try to apply the rules on all edges making a by-row-visit to the adjacency matrix.
		 */
		for (LabeledNode A : currentGraph.getVertices()) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Considering edges outgoing from " + A);
			}
			for (LabeledIntEdge AB : currentGraph.getOutEdges(A)) {
				B = currentGraph.getDest(AB);
				// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

				// Since in some graphs it is possible that there is not BC, we apply R0 and R3 to AB
				if (A.isObservator()) {
					// R0 on the resulting new values
					this.labelModificationR0(currentGraph, A, B, Z, AB, status);
				}
				this.labelModificationR3(currentGraph, A, B, Z, AB, status);
				if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
					// R0 on the resulting new values
					this.labelModificationR0(currentGraph, A, B, Z, AB, status);
				}
				for (LabeledIntEdge BC : currentGraph.getOutEdges(B)) {
					C = currentGraph.getDest(BC);
					// Attention! It is necessary to consider also self loop, e.g. A==B and B==C to propagate rightly -∞

					if (B.isObservator()) {
						// R0 on the resulting new values
						this.labelModificationR0(currentGraph, B, C, Z, BC, status);
					}
					this.labelModificationR3(currentGraph, B, C, Z, BC, status);
					if (B.isObservator()) {// R3 can add new values that have to be minimized.
						// R0 on the resulting new values
						this.labelModificationR0(currentGraph, B, C, Z, BC, status);
					}
					// Now it is possible to propagate the labels with the standard rules
					this.labelPropagationRule(currentGraph, A, B, C, AB, BC, Z, status);
					if (!status.consistency)
						return status;
					AC = currentGraph.findEdge(A, C);
					if (AC == null) {
						continue;
					}

					if (A.isObservator()) {
						// R0 on the resulting new values
						this.labelModificationR0(currentGraph, A, C, Z, AC, status);
					}

					// if (!this.excludeR1R2 && C.isObservator()) {
					// // R2 on the resulting new values.
					// this.labelModificationR2(currentGraph, C, A, AC, status);
					// }

					// R3 on the resulting new values
					this.labelModificationR3(currentGraph, A, C, Z, AC, status);

					if (A.isObservator()) {// R3 can add new values that have to be minimized. Experimentally VERIFIED on June, 28 2015
						// R0 on the resulting new values
						this.labelModificationR0(currentGraph, A, C, Z, AC, status);
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
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "End application labeled propagation rule+R0+R3."
					+ "\nSituation after the labeled propagation rule+R0+R3.");
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.log(Level.FINER, "\n");
		}
		return status;
	}

}
