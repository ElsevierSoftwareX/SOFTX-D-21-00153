package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.LabeledIntGraph;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming instantaneous reaction DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and
 * qR3* rules.<br>
 * In this class, an input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNwoNodeLabelIR extends CSTNwoNodeLabel {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNwoNodeLabelIR.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.0 - November, 07 2017";
	// static final public String VERSIONandDATE = "Version 1.1 - November, 11 2017";// Replace Ω node with equivalent constraints.
	static final public String VERSIONandDATE = "Version 2.0 - November, 16 2017";// Now the super class is CSTNwoNodeLabel

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNwoNodeLabelIR(), "Instantaneous Reaction  DC without node labels");
	}

	/**
	 * Default constructor.
	 */
	CSTNwoNodeLabelIR() {
		super();
		this.wd2epsilon = 0;
	}

	/**
	 * Constructor for
	 * 
	 * @param g graph to check
	 */
	public CSTNwoNodeLabelIR(LabeledIntGraph g) {
		super(g);
		this.wd2epsilon = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean R0qR0MainConditionForSkipping(final int w) {
		// Table 1 ICAPS2016 paper for IR semantics
		return w >= 0;
	}
}
