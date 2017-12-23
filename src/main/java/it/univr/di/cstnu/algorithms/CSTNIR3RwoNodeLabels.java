package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.LabeledIntGraph;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming instantaneous reaction DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, qR0, and qR3*
 * rules.<br>
 * In this class, an input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNIR3RwoNodeLabels extends CSTNIR3R {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNIR3RwoNodeLabels.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.0 - November, 07 2017";
	// static final public String VERSIONandDATE = "Version 1.1 - November, 11 2017";// Replace Ω node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 1.2 - November, 21 2017";// Now it is derived from CSTNIR3R
	static final public String VERSIONandDATE = "Version 1.3 - November, 22 2017";// Now it is derived from CSTNIR3R

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNIR3RwoNodeLabels(), "Instantaneous Reaction DC based on 3 Rules and without node labels");
	}

	/**
	 * Default constructor.
	 */
	CSTNIR3RwoNodeLabels() {
		super();
		this.withNodeLabels = false;
	}

	/**
	 * Constructor for
	 * 
	 * @param g graph to check
	 */
	public CSTNIR3RwoNodeLabels(LabeledIntGraph g) {
		super(g);
		this.withNodeLabels = false;
	}

	/**
	 * @param g
	 * @param timeOut
	 */
	public CSTNIR3RwoNodeLabels(LabeledIntGraph g, int timeOut) {
		super(g, timeOut);
		this.withNodeLabels = false;

	}
}
