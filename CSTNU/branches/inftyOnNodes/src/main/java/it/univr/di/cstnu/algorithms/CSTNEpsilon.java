package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Option;
import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming epsilon DC semantics (cf. ICAPS 2016 paper, table 2) and using LP, R0, qR0, R3*, and qR3*
 * rules.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNEpsilon extends CSTN {

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.0 - April, 03 2017";// first release
	static public final String VERSIONandDATE = "Version  1.1 - October, 11 2017";

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNEpsilon.class.getName());

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNEpsilon(), "Epsilon DC");
	}

	/**
	 * Reaction time for CSTN
	 */
	@Option(required = false, name = "-e", usage = "Forced Reaction Time. It must be > 0.")
	int epsilon = 1;

	/**
	 * Constructor for CSTN.
	 * 
	 * @param reactionTime reaction time. It must be strictly positive.
	 * @param g graph to check
	 */
	public CSTNEpsilon(int reactionTime, LabeledIntGraph g) {
		super(g);
		if (reactionTime <= 0)
			throw new IllegalArgumentException("Reaction time must be > 0.");
		this.epsilon = reactionTime;
		this.reactionTime = reactionTime;
	}

	/**
	 * Constructor for CSTN.
	 * 
	 * @param reactionTime reaction time. It must be strictly positive.
	 * @param g graph to check
	 * @param timeOut timeout for the check
	 */
	public CSTNEpsilon(int reactionTime, LabeledIntGraph g, int timeOut) {
		this(reactionTime, g);
		this.timeOut = timeOut;
	}

	/**
	 * Default constructor. Label optimization.
	 */
	CSTNEpsilon() {
		super();
	}

	/**
	 * @return the reactionTime
	 */
	public int getEspsilonReactionTime() {
		return this.epsilon;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean mainConditionForRestrictedLP(final int u, final int v) {
		// Table 1 ICAPS paper for standard DC
		return u >= this.epsilon && v < 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean mainConditionForSkippingInR0qR0(final int w) {
		// Table 2 ICAPS2016 paper for epsilon semantics
		return w >= this.epsilon;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean mainConditionForSkippingInR3qR3(final int w, final LabeledNode nD) {
		// Table 2 ICAPS for epsilon semantics
		// (w > 0 && nD==Z) is not added because w is always <=0 when nD==Z.
		return w > this.epsilon;
	}

	@Override
	final int newValueInR3qR3(final int v, final int w) {
		// Table 2 ICAPS2016.
		final int w1 = w - this.epsilon;
		return (v > w1) ? v : w1;
	}

}
