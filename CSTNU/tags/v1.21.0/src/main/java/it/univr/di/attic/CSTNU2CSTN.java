//package it.univr.di.cstnu;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.PrintStream;
//import java.io.PrintWriter;
//import java.time.Duration;
//import java.time.Instant;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//import org.kohsuke.args4j.CmdLineException;
//import org.kohsuke.args4j.CmdLineParser;
//import org.kohsuke.args4j.OptionHandlerFilter;
//
//import edu.uci.ics.jung.io.GraphIOException;
//import it.unimi.dsi.fastutil.chars.CharSet;
//import it.univr.di.attic.CSTNPSU;
//import it.univr.di.cstnu.algorithms.CSTN;
//import it.univr.di.cstnu.algorithms.CSTNU;
//import it.univr.di.cstnu.algorithms.WellDefinitionException;
//import it.univr.di.cstnu.algorithms.CSTN.CSTNCheckStatus;
//import it.univr.di.cstnu.graph.GraphMLReader;
//import it.univr.di.cstnu.graph.GraphMLWriter;
//import it.univr.di.cstnu.graph.LabeledIntEdge;
//import it.univr.di.cstnu.graph.LabeledIntEdge.ConstraintType;
//import it.univr.di.cstnu.graph.LabeledIntEdgePluggable;
//import it.univr.di.cstnu.graph.LabeledIntGraph;
//import it.univr.di.cstnu.graph.LabeledNode;
//import it.univr.di.labeledvalue.Constants;
//import it.univr.di.labeledvalue.Label;
//import it.univr.di.labeledvalue.LabeledContingentIntTreeMap;
//import it.univr.di.labeledvalue.LabeledIntMap;
//import it.univr.di.labeledvalue.LabeledIntTreeMap;
//import it.univr.di.labeledvalue.Literal.State;
//
///**
// * Simple class to represent and check Conditional Simple Temporal Network with Uncertainty (CSTNU) reducing it to a CSTN.
// * It doesn't manage guarded link! For such feature, look at {@link CSTNPSU} class.
// * 
// * @author Roberto Posenato
// * @version $Id: $Id
// */
//public class CSTNU2CSTN extends CSTNU {
//
//	/**
//	 * Simple class to represent the status of the checking algorithm during an execution.
//	 *
//	 * @author Roberto Posenato
//	 */
//	public static class CSTNUCheckStatus {
//		/**
//		 * True if the network is controllable so far.
//		 */
//		public boolean controllable = true;
//
//		/**
//		 * Execution time in milliseconds.
//		 */
//		public long executionTimeNS = Constants.INT_NULL;
//
//		/**
//		 * True if no rule can be applied anymore.
//		 */
//		public boolean finished = false;
//
//		@Override
//		public String toString() {
//			return ("The check is" + (this.finished ? " " : " NOT") + " finished.\n"
//					+ ((this.finished) ? "the consistency check has determined that given network is" + (this.controllable ? " " : " NOT ")
//							+ " dynamic controllable.\n" : "")
//					+ "The global execution time has been " + this.executionTimeNS + " ns (~" + (this.executionTimeNS / 1E9) + " s.)");
//		}
//	}
//
//	/**
//	 * logger
//	 */
//	static Logger LOG1 = Logger.getLogger(CSTNU2CSTN.class.getName());
//
//	/**
//	 * Version of the class
//	 */
//	// static final String VERSIONandDATE = "Version 3.1 - Apr, 20 2016";
//	static final String VERSIONandDATE = "Version  1.0 - September, 25 2016";
//
//	/**
//	 * Constructor for CSTNU2CSTN.
//	 */
//	private CSTNU2CSTN() {
//		super(null);// useless but necessary to compile
//	}
//
//	/**
//	 * Constructor for CSTNU
//	 *
//	 * @param reactionTime a non negative int representing the time after the setting of a proposition the system can react.
//	 * @param useΩ if Ω node has to be add and used if it is missing.
//	 * @param labeledIntValueMap the type for representing labeled value sets.
//	 */
//	public CSTNU2CSTN(final int reactionTime, final boolean useΩ, Class<? extends LabeledIntMap> labeledIntValueMap) {
//		super(null);
//	}
//
//	/**
//	 * Checks the controllability of a CSTNU instance.
//	 * This method transform the given CSTNU instance into a corresponding CSTN instance such that
//	 * the original instance is dynamic <em>controllable</em> iff the corresponding CSTN is dynamic <em>consistent</em>.
//	 *
//	 * @param g the original CSTNU that has to be checked.
//	 * @param cstnGraph the resulting CSTN. It is necessary to pass an empty CSTN object. It will be modified such that it contains, at the end, the
//	 *            corresponding
//	 *            (minimized in case of positive check) CSTN instance.
//	 * @return status an {@link CSTNUCheckStatus} object containing the final status and some statistics about the executed checking.
//	 * @throws it.univr.di.cstnu.algorithms.WellDefinitionException if the nextGraph is not well defined (does not observe all well definition properties).
//	 */
//	public CSTNU2CSTN.CSTNUCheckStatus dynamicControllabilityCheck(final LabeledIntGraph g, LabeledIntGraph cstnGraph) throws WellDefinitionException {
//		CSTNUCheckStatus status = new CSTNUCheckStatus();
//		if (g == null || cstnGraph == null)
//			return status;
//
//		LabeledIntGraph nextGraph = new LabeledIntGraph(g, this.labeledIntValueMap);
//		nextGraph.setName("Next graph");
//		try {
//			initUpperLowerLabelDataStructure();
//		} catch (final IllegalArgumentException e) {
//			throw new IllegalArgumentException("The graph has a problem and it cannot be initialize: " + e.getMessage());
//		}
//
//		Instant startInstant = Instant.now();
//		
//		LOG1.info("Conversion to the corresponding CSTN instance starts...");
//		transform(nextGraph, cstnGraph);
//		LOG1.info("Conversion to the corresponding CSTN instance done.");
//
//		LOG1.info("CSTN DC-checking starts...");
//		CSTN cstnChecker = new CSTN(null);
//		CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck(cstnGraph);
//		LOG1.info("CSTN DC-checking done.");
//		status.finished = cstnStatus.finished;
//		if (!cstnStatus.consistency) {
//			LOG1.info("The corresponding CSTN graph has at least one negative loop: stop!");
//			status.controllable = false;
//			status.finished = true;
//		}
//
//		Instant endInstant = Instant.now();
//		status.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();
//
//		if (!status.controllable) {
//			if (LOG1.isLoggable(Level.INFO)) {
//				LOG1.log(Level.INFO, "The CSTNU instance is not DC controllable.\nStatus: " + status);
//			}
//			return status;
//		}
//
//		// controllable && finished
//		if (LOG1.isLoggable(Level.INFO)) {
//			LOG1.log(Level.INFO, "The CSTNU instance is DC controllable."
//					+ "\nStatus: " + status);
//		}
//		// Put all data structures of currentGraph in g
//		// g.copyCleaningRedundantLabels(cstnGraph);
//		// g.setName(originalName);
//		return status;
//	}
//
//	/**
//	 * Returns the corresponding CSTN of the given CSTNU g.
//	 * The transformation consists in replacing each contingent link with a pattern that use a proper new observation time point (associated to the contingent
//	 * link).
//	 * 
//	 * <pre>
//	 * A ===[x,y]===> C is transformed to A ---[x,x]---> K? ---[0, k]---> C
//	 *                                                      <--[x-y,¬k]--
//	 * </pre>
//	 * 
//	 * @param g an CSTNU instance. It is assumed that contingent link are represented using only upper/lower case values.
//	 * @param cstn the graph that has to contain the g transformed. It must be a empty graph.
//	 * @return g represented as a CSTN
//	 */
//	boolean transform(LabeledIntGraph g, LabeledIntGraph cstn) {
//		if (g == null || cstn == null)
//			return false;
//
//		int k = g.getUpperLabeledEdges().size();
//		if (k == 0) {
//			cstn.copyCleaningRedundantLabels(g);
//			return true;
//		}
//
//		CharSet usedProposition = g.getPropositions();
//		int p = usedProposition.size();
//		int n = g.getVertexCount();
//
//		// Current implementation of Label allows only Constants.NUMBER_OF_POSSIBLE_PROPOSITION propositions at maximum
//		if (k + p > Constants.NUMBER_OF_POSSIBLE_PROPOSITION) {
//			throw new IllegalArgumentException(
//					"The network cannot be checked by this method because the sum of the number of contingent links and the number of observation time points is greater than 32, "
//							+ "the maximum capacity of this implementation.");
//		}
//		// cleaning cstn
//		cstn.clear(n + k);
//
//		// build the vector of proposition that can be used.
//		char[] availableProposition = new char[k];
//		for (int block = 0, j = 0; block < Constants.PROPOSITIONS_BLOCKS && j < k; block++) {
//			char c = Constants.FIRST_PROPOSITION[block];
//			for (; c<= Constants.LAST_PROPOSITION[block] && j < k;) {
//				if (usedProposition.contains(c)) {
//					c++;
//					continue;
//				}
//				availableProposition[j++] = c++;
//			}
//		}
//		// Clone all nodes
//		LabeledNode newV, Z = g.getZ(), Ω = g.getΩ();
//		for (final LabeledNode v : g.getVertices()) {
//			newV = new LabeledNode(v);
//			cstn.addVertex(newV);
//			if (v.equalsByName(Z)) {
//				cstn.setZ(newV);
//				continue;
//			}
//			if (v.equalsByName(Ω)) {
//				cstn.setΩ(newV);
//			}
//			// LOG1.finer("Node added: "+ newV);
//		}
//
//		// clone all edges, transforming the contingent ones
//		LabeledIntEdgePluggable newE;
//		LabeledIntEdge eInverted;
//		int sizeU, sizeL, lowerCaseValue, upperCaseValue;
//		LabeledContingentIntTreeMap lowerCaseValueMap;
//		int firstPropAvailable = 0;
//		for (final LabeledIntEdge e : g.getEdges()) {
//			LabeledNode sInG = g.getSource(e);
//			LabeledNode dInG = g.getDest(e);
//			if (!e.isContingentEdge()) {
//				newE = cstn.getEdgeFactory().create(e);
//				cstn.addEdge(newE, sInG.getName(), dInG.getName());
//				continue;
//			}
//			// e is contingent!
//			// Since for each contingent link, there is 2 contingent edges, we consider them only when we meet the
//			// contingent edge with positive value (lower case value);
//			// We assume that contingent edges contains only lower case value or upper case value.
//			lowerCaseValueMap = e.getLowerLabelMap();
//			if ((sizeL = lowerCaseValueMap.size()) == 0) {
//				if ((sizeU = e.getUpperLabelMap().size()) == 0 || sizeU > 1) {
//					throw new IllegalStateException("Edge " + e + " is contingent but it doesn't containt upper case value neither lower case one.");
//				}
//				continue;
//			}
//			if (sizeL > 1) {
//				throw new IllegalStateException("Edge " + e + " is contingent and contains more lower case values.");
//			}
//			LOG1.finer("Considering e: " + e);
//			eInverted = g.findEdge(dInG.getName(), sInG.getName());
//			LOG1.finer("and its companion eInverted: " + eInverted);
//
//			lowerCaseValue = e.getMinLowerLabeledValue();
//			upperCaseValue = eInverted.getMinUpperLabeledValue();
//			if (lowerCaseValue == Constants.INT_NULL || upperCaseValue == Constants.INT_NULL) {
//				throw new IllegalStateException("Something is wrong with the two contingent edges " + e + " and " + eInverted);
//			}
//			// new observation time point K
//			LabeledNode newK = new LabeledNode(availableProposition[firstPropAvailable] + "?", availableProposition[firstPropAvailable++]);
//			newK.setLabel(cstn.getNode(dInG.getName()).getLabel());
//			newK.setX(sInG.getX() + 10);
//			newK.setY(sInG.getY());
//			cstn.addVertex(newK);
//			LOG1.finer("Node added: " + newK);
//
//			// two edges between X and K
//			newE = cstn.getEdgeFactory().create(sInG.getName() + "-" + newK.getName());
//			newE.setConstraintType(ConstraintType.internal);
//			Label l = cstn.getNode(sInG.getName()).getLabel().conjunction(newK.getLabel());
//			newE.mergeLabeledValue(l, lowerCaseValue);
//			cstn.addEdge(newE, sInG.getName(), newK.getName());
//			LOG1.finer("New edge added: " + newE);
//
//			newE = cstn.getEdgeFactory().create(newK.getName() + "-" + sInG.getName());
//			newE.setConstraintType(ConstraintType.internal);
//			newE.mergeLabeledValue(l, -lowerCaseValue);
//			cstn.addEdge(newE, newK.getName(), sInG.getName());
//			LOG1.finer("New edge added: " + newE);
//
//			// two edges between K and C
//			newE = cstn.getEdgeFactory().create(dInG.getName() + "-" + newK.getName());
//			newE.setConstraintType(ConstraintType.internal);
//			l = cstn.getNode(dInG.getName()).getLabel().conjunction(newK.getLabel());
//			newE.mergeLabeledValue(l.conjunction(new Label(newK.getPropositionObserved(), State.negated)), lowerCaseValue + upperCaseValue);// it is x-y.
//																																			// upperCaseValue=-y
//			cstn.addEdge(newE, dInG.getName(), newK.getName());
//			LOG1.finer("New edge added: " + newE);
//
//			newE = cstn.getEdgeFactory().create(newK.getName() + "-" + dInG.getName());
//			newE.setConstraintType(ConstraintType.internal);
//			newE.mergeLabeledValue(l.conjunction(new Label(newK.getPropositionObserved(), State.straight)), 0);
//			cstn.addEdge(newE, newK.getName(), dInG.getName());
//			LOG1.finer("New edge added: " + newE);
//		}
//
//		return true;
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
//		final CSTNU2CSTN cstnu2cstn = new CSTNU2CSTN(0, false, LabeledIntTreeMap.class);
//
//		if (!cstnu2cstn.manageParameters(args))
//			return;
//		LOG.finest("Parameters ok!");
//
//		LOG.finest("Loading graph...");
//		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstnu2cstn.fInput, cstnu2cstn.labeledIntValueMap);
//		LabeledIntGraph g = graphMLReader.readGraph();
//
//		LOG.finest("LabeledIntGraph loaded!");
//
//		LOG.finest("DC Checking...");
//		CSTNUCheckStatus status;
//		LabeledIntGraph cstn = new LabeledIntGraph(LabeledIntTreeMap.class);
//		try {
//			status = cstnu2cstn.dynamicControllabilityCheck(g, cstn);
//		} catch (final WellDefinitionException e) {
//			System.out.print("An error has been occured during the checking: " + e.getMessage());
//			return;
//		}
//		if (status.finished) {
//			System.out.println("Checking finished!");
//			if (status.controllable) {
//				System.out.println("The given cstnu is Dynamic controllable!");
//			} else {
//				System.out.println("The given cstnu is NOT DC!");
//			}
//			System.out.println("Details: " + status);
//		} else {
//			System.out.println("Checking has not been finished!");
//			System.out.println("Details: " + status);
//		}
//
//		if (cstnu2cstn.fOutput != null) {
//			final GraphMLWriter graphWriter = new GraphMLWriter(null);
//			try {
//				graphWriter.save(cstn, new PrintWriter(cstnu2cstn.output));
//			} catch (final IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
//
//	/**
//	 * Simple method to manage command line parameters using args4j library.
//	 *
//	 * @param args
//	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
//	 */
//	private boolean manageParameters(final String[] args) {
//		final CmdLineParser parser = new CmdLineParser(this);
//		try {
//			// parse the arguments.
//			parser.parseArgument(args);
//
//			if (!this.fInput.exists())
//				throw new CmdLineException(parser, "Input file does not exist.");
//
//			if (this.fOutput != null) {
//				if (this.fOutput.isDirectory())
//					throw new CmdLineException(parser, "Output file is a directory.");
//				if (!this.fOutput.getName().endsWith(".cstnu")) {
//					this.fOutput.renameTo(new File(this.fOutput.getAbsolutePath() + ".cstnu2cstn.2cstn"));
//				}
//				if (this.fOutput.exists()) {
//					this.fOutput.delete();
//				}
//				try {
//					this.fOutput.createNewFile();
//					this.output = new PrintStream(this.fOutput);
//				} catch (final IOException e) {
//					throw new CmdLineException(parser, "Output file cannot be created.");
//				}
//			} else {
//				this.output = System.out;
//			}
//		} catch (final CmdLineException e) {
//			// if there's a problem in the command line, you'll get this exception. this will report an error message.
//			System.err.println(e.getMessage());
//			System.err.println("java CSTNU2CSTN [options...] arguments...");
//			// print the list of available options
//			parser.printUsage(System.err);
//			System.err.println();
//
//			// print option sample. This is useful some time
//			System.err.println("Example: java -jar CSTNU.jar" + parser.printExample(OptionHandlerFilter.REQUIRED)
//					+ " <CSTN_file_name>");
//			return false;
//		}
//		return true;
//	}
//
//}
