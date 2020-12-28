// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

package it.univr.di.cstnu.algorithms;

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
 * In this class, an input CSTN graph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNIRwoNodeLabels extends CSTNIR {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNIRwoNodeLabels.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.0 - November, 07 2017";
	// static final public String VERSIONandDATE = "Version 1.1 - November, 11 2017";// Replace Ω node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 2.0 - November, 16 2017";// Now the super class is CSTNwoNodeLabel
	static final public String VERSIONandDATE = "Version 2.1 - November, 22 2017";// Now the super class is CSTNIR

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNIRwoNodeLabels(), "Instantaneous Reaction  DC without node labels");
	}

	/**
	 * Default constructor.
	 */
	CSTNIRwoNodeLabels() {
		super();
		this.withNodeLabels = false;
	}

	/**
	 * Constructor for
	 * 
	 * @param g1 graph to check
	 */
	public CSTNIRwoNodeLabels(TNGraph<CSTNEdge> g1) {
		super(g1);
		this.withNodeLabels = false;
	}

	/**
	 * @param g1
	 * @param timeOut1
	 */
	public CSTNIRwoNodeLabels(TNGraph<CSTNEdge> g1, int timeOut1) {
		super(g1, timeOut1);
		this.withNodeLabels = false;
	}
}
