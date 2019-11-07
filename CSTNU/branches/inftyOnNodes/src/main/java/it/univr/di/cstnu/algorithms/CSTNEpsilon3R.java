package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.cstnu.graph.CSTNEdge;
import it.univr.di.cstnu.graph.TNGraph;

/**
 * Simple class to represent and DC check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * The dynamic consistency check (DC check) is done assuming epsilon DC semantics (cf. ICAPS 2016 paper, table 2) and using LP, qR0, and qR3*
 * rules.<br>
 * In this class, an input CSTN TNGraph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNEpsilon3R extends CSTNEpsilon {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger("CSTNEpsilon3R");

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final public String VERSIONandDATE = "Version 1.0 - November, 22 2017";

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNEpsilon3R(), "Epsilon DC based on 3 Rules");
	}

	/**
	 * Default constructor.
	 */
	CSTNEpsilon3R() {
		super();
		this.propagationOnlyToZ = true;
	}

	/**
	 * Constructor for CSTN with reaction time at least epsilon and without node labels.
	 * 
	 * @param reactionTime1 reaction time. It must be strictly positive.
	 * @param g1 tNGraph to check
	 */
	public CSTNEpsilon3R(int reactionTime1, TNGraph<CSTNEdge> g1) {
		super(reactionTime1, g1);
		this.propagationOnlyToZ = true;
	}

	/**
	 * @param reactionTime1
	 * @param g1
	 * @param timeOut1
	 */
	public CSTNEpsilon3R(int reactionTime1, TNGraph<CSTNEdge> g1, int timeOut1) {
		super(reactionTime1, g1, timeOut1);
		this.propagationOnlyToZ = true;
	}
}
