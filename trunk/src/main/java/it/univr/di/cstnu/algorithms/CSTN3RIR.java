package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.LabeledIntGraph;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming instantaneous reaction semantics (cf. ICAPS 2016 paper, table 1) and using LP, qR0, and
 * qR3* rules.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN3RIR extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTN3RIR.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final public String VERSIONandDATE = "Version 6.0 - October, 16 2017";// SVN 202. Removed qLabels from LP. Removed R0 and R3. Now, rules are LP,
	// qR0 and qR3*
	// static final public String VERSIONandDATE = "Version 6.1 - October, 25 2017";// SVN 203. Code optimization.
	// static final public String VERSIONandDATE = "Version 6.2 - November, 11 2017";// Replace Ω node with equivalent constraints.
	static final public String VERSIONandDATE = "Version  6.3 - November, 21 2017";// This is the principal #R class.

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTN3RIR(), "Instantaneous Reaction DC based on 3 Rules");
	}

	/**
	 * 
	 */
	public CSTN3RIR() {
		super();
		this.applyReducedSetOfRules = true;
		this.wd2epsilon = 0;
	}

	/**
	 * @param g
	 */
	public CSTN3RIR(LabeledIntGraph g) {
		super(g);
		this.applyReducedSetOfRules = true;
		this.wd2epsilon = 0;
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean R0qR0MainConditionForSkipping(final int w) {
		// Table 1 ICAPS2016 paper for IR semantics
		return w >= 0;
	}

}
