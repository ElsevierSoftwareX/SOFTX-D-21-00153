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
 * The dynamic consistency check (DC check) is done assuming standard DC semantics (cf. ICAPS 2016 paper, table 1) and using LP, R0, qR0, R3*, and qR3*
 * rules.<br>
 * In this class, an input CSTN tNGraph is transformed into an equivalent CSTN instance where node labels are empty.<br>
 *
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNwoNodeLabel extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger("CSTNwoNodeLabel");

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static public final String VERSIONandDATE = "Version 1.2 - April, 25 2017";
	// static public final String VERSIONandDATE = "Version 1.3 - October, 10 2017";// removed qLabels from LP
	// static public final String VERSIONandDATE = "Version 1.4 - November, 07 2017";// restored original LP
	// static public final String VERSIONandDATE = "Version 1.5 - November, 15 2017";// Removed the possibility of auxiliary constraints.
	static public final String VERSIONandDATE = "Version  1.6 - November, 17 2017";// Adjusted LP

	/**
	 * Just for using this class also from a terminal.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws org.xml.sax.SAXException
	 * @throws javax.xml.parsers.ParserConfigurationException
	 * @throws java.io.IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNwoNodeLabel(), "Standard DC without node labels");
	}

	/**
	 * Default constructor.
	 */
	CSTNwoNodeLabel() {
		super();
		this.withNodeLabels = false;
	}

	/**
	 * Constructor for
	 *
	 * @param graph TNGraph to check
	 */
	public CSTNwoNodeLabel(TNGraph<CSTNEdge> graph) {
		super(graph);
		this.withNodeLabels = false;
	}

	/**
	 * <p>Constructor for CSTNwoNodeLabel.</p>
	 *
	 * @param graph a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 * @param givenTimeOut a int.
	 */
	public CSTNwoNodeLabel(TNGraph<CSTNEdge> graph, int givenTimeOut) {
		super(graph, givenTimeOut);
		this.withNodeLabels = false;
	}
}
