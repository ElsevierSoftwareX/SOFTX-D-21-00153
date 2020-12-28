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
 * The dynamic consistency check (DC check) is done assuming instantaneous reaction semantics (cf. ICAPS 2016 paper, table 1) and using LP, qR0, and
 * qR3* rules.<br>
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNIR3R extends CSTNIR {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger("CSTNIR3R");

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final public String VERSIONandDATE = "Version 6.0 - October, 16 2017";// SVN 202. Removed qLabels from LP. Removed R0 and R3. Now, rules are LP,
	// qR0 and qR3*
	// static final public String VERSIONandDATE = "Version 6.1 - October, 25 2017";// SVN 203. Code optimization.
	// static final public String VERSIONandDATE = "Version 6.2 - November, 11 2017";// Replace Ω node with equivalent constraints.
	// static final public String VERSIONandDATE = "Version 6.3 - November, 21 2017";// This is the principal #R class.
	static final public String VERSIONandDATE = "Version  6.4 - November, 22 2017";// Now super class is CSTNIR

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTNIR3R(), "Instantaneous Reaction DC based on 3 Rules");
	}

	/**
	 * 
	 */
	public CSTNIR3R() {
		super();
		this.propagationOnlyToZ = true;
	}

	/**
	 * @param g1
	 */
	public CSTNIR3R(TNGraph<CSTNEdge> g1) {
		super(g1);
		this.propagationOnlyToZ = true;
	}

	/**
	 * @param g1
	 * @param timeOut1
	 */
	public CSTNIR3R(TNGraph<CSTNEdge> g1, int timeOut1) {
		super(g1, timeOut1);
		this.propagationOnlyToZ = true;
	}
}
