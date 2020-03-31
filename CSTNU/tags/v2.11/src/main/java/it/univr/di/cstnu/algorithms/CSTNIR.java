package it.univr.di.cstnu.algorithms;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.TNGraph;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming instantaneous reaction DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and
 * qR3* rules.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNIR extends CSTN {


	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.0 - April, 03 2017";// first release i.r.
	static public final String VERSIONandDATE = "Version  1.1 - October, 10 2017";// removed qLables

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNIR.class.getName());

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNIR3R(), "Instantaneous Reaction DC");
	}

	/**
	 * Constructor for CSTN.
	 * 
	 * @param g1 the labeled int valued tNGraph to check
	 */
	public CSTNIR(TNGraph<CSTNEdge> g1) {
		super(g1);
		this.reactionTime = 0;
	}

	/**
	 * @param g1
	 * @param timeOut1
	 */
	public CSTNIR(TNGraph<CSTNEdge> g1, int timeOut1) {
		super(g1, timeOut1);
		this.reactionTime = 0;
	}

	/**
	 * Default constructor.
	 */
	CSTNIR() {
		super();
		this.reactionTime = 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean lpMustRestricted2ConsistentLabel(final int u, final int v) {
		// Table 1 ICAPS paper for standard DC
		// u must be < 0
		return u >= 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final boolean mainConditionForSkippingInR0qR0(final int w) {
		// Table 1 ICAPS2016 paper for IR semantics
		// w must be < 0.
		return w >= 0;
	}

}
