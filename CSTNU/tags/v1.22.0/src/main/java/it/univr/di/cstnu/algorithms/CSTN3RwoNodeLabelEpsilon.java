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
 * The dynamic consistency check (DC check) is done assuming epsilon DC semantics (cf. ICAPS 2016 paper, table 2) and using LP, qR0, and qR3*
 * rules.<br>
 * In this class, an input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN3RwoNodeLabelEpsilon extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTN3RwoNodeLabelEpsilon.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final public String VERSIONandDATE = "Version 1.0 - November, 17 2017";

	/**
	 * Reaction time for CSTN
	 */
	@Option(required = false, name = "-r", usage = "Reaction time. It must be > 0.")
	private int reactionTime = 1;

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTN3RwoNodeLabelEpsilon(), "Epsilon DC based on 3 Rules and without node labels");
	}

	/**
	 * Default constructor.
	 */
	CSTN3RwoNodeLabelEpsilon() {
		super();
		this.applyReducedSetOfRules = true;
		this.withNodeLabels = false;
	}

	/**
	 * Constructor for CSTN with reaction time at least epsilon and without node labels.
	 * 
	 * @param reactionTime reaction time. It must be strictly positive.
	 * @param g graph to check
	 */
	public CSTN3RwoNodeLabelEpsilon(int reactionTime, LabeledIntGraph g) {
		super(g);
		if (reactionTime <= 0)
			throw new IllegalArgumentException("Reaction time must be > 0.");
		this.reactionTime = reactionTime;
		this.wd2epsilon = reactionTime;
		this.applyReducedSetOfRules = true;
		this.withNodeLabels = false;
	}

	/**
	 * @return the reactionTime
	 */
	public final int getReactionTime() {
		return this.reactionTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean R0qR0MainConditionForSkipping(final int w) {
		// Table 2 ICAPS2016 paper for epsilon semantics
		return w >= this.reactionTime;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean R3qR3MainConditionForSkipping(final int w, final LabeledNode nD) {
		// Table 2 ICAPS for epsilon semantics
		// (w > 0 && nD==Z) is not added because w is always <=0 when nD==Z.
		return w > this.reactionTime;
	}

	@Override
	final int R3qR3NewValue(final int v, final int w) {
		// Table 2 ICAPS2016.
		int w1 = w - this.reactionTime;
		return (v >= w1) ? v : w1;
	}

}
