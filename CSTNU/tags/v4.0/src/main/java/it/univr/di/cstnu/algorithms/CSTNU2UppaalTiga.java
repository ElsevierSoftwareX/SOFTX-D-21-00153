// SPDX-FileCopyrightText: 2020 Roberto Posenato <roberto.posenato@univr.it>
//
// SPDX-License-Identifier: LGPL-3.0-or-later

/**
 * Translator to the Time Game Automata (TIGA) model.
 */
package it.univr.di.cstnu.algorithms;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.OptionHandlerFilter;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.bpodgursky.jbool_expressions.Expression;
import com.bpodgursky.jbool_expressions.parsers.ExprParser;
import com.bpodgursky.jbool_expressions.rules.RuleSet;

import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import it.univr.di.cstnu.graph.CSTNUEdge;
import it.univr.di.cstnu.graph.EdgeSupplier;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.TNGraph;
import it.univr.di.cstnu.graph.TNGraphMLReader;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.Literal;

/**
 * Actor class that transforms a CSTNU instance into a UppaalTiga Time Game Automa schema.<br>
 * It is sufficient to build an CSTNU2UppaalTiga object giving a TNGraph instance that represents the network of a CSTNU instance and an output stream where the
 * result must be sent (see {@link it.univr.di.cstnu.algorithms.CSTNU2UppaalTiga#CSTNU2UppaalTiga(TNGraph, PrintStream)}.<br>
 * Then, invoking {@link #translate()}, the result is sent to the specificied output stream.
 *
 * @author posenato
 * @version $Id: $Id
 */
public class CSTNU2UppaalTiga {
	/**
	 * Version
	 */
	// static final String VERSIONandDATE = "1.6.1, April, 30 2014";
	// static final String VERSIONandDATE = "1.6.2, April, 30 2015";
	// static final String VERSIONandDATE = "1.6.3, December, 30 2015";
	// static final String VERSIONandDATE = "1.64, June, 09 2019";// Edge refactoring
	static final String VERSIONandDATE = "1.65, January, 13 2021";// Fixed file encoding

	/**
	 * Utility class to represent a contingent link parameters.
	 */
	private static class Contingent implements Comparable<Contingent> {
		int lower, upper;
		LabeledNode source, dest;

		/**
		 * @param s  none
		 * @param l  none
		 * @param u  none
		 * @param d  none
		 */
		Contingent(LabeledNode s, int l, int u, LabeledNode d) {
			if (l == Constants.INT_NULL || u == Constants.INT_NULL)
				throw new IllegalArgumentException("Integer values cannot be null!");
			this.source = s;
			this.dest = d;
			this.lower = l;
			this.upper = u;
		}

		@Override
		public int compareTo(Contingent o) {
			int v = this.source.compareTo(o.source);
			if (v != 0)
				return v;
			v = this.dest.compareTo(o.dest);
			if (v != 0)
				return v;
			v = this.lower - o.lower;
			if (v != 0)
				return v;
			v = this.upper - o.upper;
			return v;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Contingent))
				return false;
			Contingent c = (Contingent) o;
			return this.source.equalsByName(c.source)
					&& this.dest.equalsByName(c.dest)
					&& this.lower == c.lower
					&& this.upper == c.upper;
		}

		@Override
		public int hashCode() {
			String s = this.source.getName() + '-' + this.dest.getName() + this.lower + '-' + this.upper;
			return s.hashCode();
		}
	}

	/**
	 * Utility class to represent a constraint.
	 */
	private static class Constraint implements Comparable<Constraint> {
		int value;
		LabeledNode source, dest;

		Constraint(LabeledNode d, LabeledNode s, int l) {
			if (l == Constants.INT_NULL)
				throw new IllegalArgumentException("Integer values cannot be null!");
			this.source = s;
			this.dest = d;
			this.value = l;
		}

		@Override
		public int compareTo(Constraint o) {
			if (o == null)
				return 1;
			if (this.equals(o))
				return 0;
			int b = 0;
			if ((b = this.dest.compareTo(o.dest)) != 0)
				return b;
			if ((b = this.source.compareTo(o.source)) != 0)
				return b;
			return this.value - o.value;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Constraint))
				return false;
			Constraint cons = (Constraint) o;
			return cons.source != null && cons.source.equalsByName(this.source)
					&& cons.dest != null && cons.dest.equalsByName(this.dest)
					&& cons.value != Constants.INT_NULL && cons.value == this.value;
		}

		@Override
		public String toString() {
			// Be careful!
			// A constraint (D-S≤v) has to be translated as (tS-tD≤v) when tS and tD are clock names, because node clock will be reset when node is executed!
			return "(" + getClockName(this.dest) + " - " + getClockName(this.source) + " <= " + this.value + ")";
		}

		@Override
		public int hashCode() {
			return ((this.dest != null ? this.dest.hashCode() : 0) + 31 * (this.source != null ? this.source.hashCode() : 0)) * 31
					+ (this.value != Constants.INT_NULL ? this.value : 0);
		}
	}

	/**
	 * class logger
	 */
	static Logger LOG = Logger.getLogger("CSTNU2UppaalTiga");

	/**
	 * Token to represent logic AND in Tiga expression.
	 */
	static final String AND = " && ";

	/**
	 * Token to represent logic NOT in Tiga expression.
	 */
	static final String NOT = "!";

	/**
	 * Token to represent logic OR in Tiga expression.
	 */
	static final String OR = " || ";

	/**
	 * Jbool_expression represents Expression using identifiers escaped by `, logical NOT as '!', logical AND as ' &amp;' and logical OR as '|'.
	 * For example, "!`a` &amp; `(D-A &lt;=)` | `a`".
	 * 
	 * @param expr jbool_expression
	 * @param not string representing not. If null, it is assumed {@link #NOT}
	 * @param and representing not. If null, it is assumed {@link #AND}
	 * @param or representing not. If null, it is assumed {@link #OR} @return Tiga representation of orbitalFormulaText.
	 * @return Jbool_expression represents Expression using identifiers escaped by `, not as '!', and as '&amp;' and or as '!'.
	 */
	private static String jbool2TigaExpr(String expr, String not, String and, String or) {
		if (expr == null || expr.isEmpty())
			return "";

		if (not == null)
			not = NOT;
		if (and == null)
			and = AND;
		if (or == null)
			or = OR;

		expr = expr.replaceAll("!", not);
		expr = expr.replaceAll(" & ", and);
		expr = expr.replaceAll(" \\| ", or);
		expr = expr.replaceAll("`", "");

		return expr;
		// String allowedTokenRE = "-\\w\\s\\.";
		// =<\\(([" + allowedTokenRE + "]+),([" + allowedTokenRE + "]+)\\)", "( ($1) <= ($2) )");
	}

	/**
	 * Build a clock name: "t" followed by node name cleaned off not allowed chars.
	 * 
	 * @param n node
	 * @return clock name associated to the node
	 */
	static String getClockName(LabeledNode n) {
		return "t" + removeCharNotAllowed(n.getName());
	}

	/**
	 * Build a executed boolean variable name: "x" followed by node name cleaned off not allowed chars.
	 * 
	 * @param n
	 * @return executed var name associated to the node
	 */
	private static String getExecutedName(LabeledNode n) {
		return "x" + removeCharNotAllowed(n.getName());
	}

	/**
	 * Build a proposition name.
	 * 
	 * @param n node
	 * @return the proposition associated to the l.
	 */
	private static String getPropositionName(LabeledNode n) {
		if (n == null || n.getPropositionObserved() == Constants.UNKNOWN)
			return "";
		return "" + n.getPropositionObserved();
	}

	// /**
	// * Build a obs node name for a proposition.
	// *
	// * @param n node
	// * @return the obs node name associated to the l.
	// */
	// private static String getObsNodeName(LabeledNode n) {
	// if (n == null || n.getObservable() == null) return "";
	// return "n" + n.getObservable();
	// }

	/**
	 * Return an id for the template id using the name of the tNGraph.
	 * 
	 * @param g the tNGraph
	 * @return a cleaned Tga name
	 */
	private static String getTgaName(TNGraph<CSTNUEdge> g) {
		String name = removeCharNotAllowed(g.getName());
		if (name.matches("[0-9]+.+")) {
			name = "g" + name;
		}
		return name;
	}

	/**
	 * Reads a CSTNU file and converts it into <a href="http://people.cs.aau.dk/~adavid/tiga/index.html">UPPAAL TIGA</a> format.
	 *
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws java.io.IOException if any.
	 * @throws javax.xml.parsers.ParserConfigurationException if any.
	 * @throws org.xml.sax.SAXException if any.
	 */
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
		LOG.finest("Start...");
		CSTNU2UppaalTiga translator = new CSTNU2UppaalTiga();

		if (!translator.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (translator.versionReq) {
			System.out.print("CSTNU2UppaalTiga " + VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2014, Roberto Posenato");
			return;
		}

		LOG.finest("Loading tNGraph...");
		if (!translator.loadCSTNU(translator.fInput))
			return;
		LOG.finest("TNGraph loaded!");

		LOG.finest("Translating tNGraph...");
		translator.translate();
		LOG.finest("TNGraph translated and saved!");
	}

	/**
	 * Remove all character that cannot be part of a TIGA identifier.
	 * 
	 * @param s
	 * @return a cleaned string
	 */
	private static String removeCharNotAllowed(String s) {
		return s.replaceAll("[-?. ]", "_");
	}

	/**
	 * Name of controller state/node
	 */
	@Option(required = false, name = "--controller", usage = "Name of controller node", metaVar = "agnes")
	private String AGNES = "agnes";

	private SortedSet<Contingent> contingentEdge = null;

	/**
	 * Contains all CSTNU contingent nodes: a CSTNU node is contingent if it is destination of a contingent constraint.
	 */
	private SortedSet<LabeledNode> contingentNode = null;

	/**
	 * CSTNU tNGraph to translate
	 */
	private TNGraph<CSTNUEdge> cstnu = null;

	/**
	 * Document representing DOM of TIGA
	 */
	private Document doc = null;

	/**
	 * The input file containing the CSTNU tNGraph in GraphML format.
	 */
	@Argument(required = true, index = 0, usage = "input file. Input file has to be a CSTNU tNGraph in GraphML format.", metaVar = "CSTNU_file_name")
	private File fInput;

	/**
	 * Output file where to write the XML representing UPPAAL TIGA automata.
	 */
	@Option(required = false, name = "-o", aliases = "--output", usage = "output to this file. If file is already present, it is overwritten. If this parameter is not present, then the output is send to the std output.", metaVar = "UPPAALTIGA_file_name")
	private File fOutput = null;

	/**
	 * Contains all CSTNU free nodes: a CSTNU node is free if it does not observe a proposition and it is source of any edge or it is destination of a
	 * non-contingent constraint.
	 */
	private SortedSet<LabeledNode> freeNode = null;

	/**
	 * Name of goal state/node.
	 */
	@Option(required = false, name = "--goal", usage = "Name of Goal node", metaVar = "GOAL")
	private String GOAL = "goal";

	/**
	 * Name of go state/node.
	 */
	@Option(required = false, name = "--go", usage = "Name of Go node", metaVar = "GO")
	private String GO = "go";

	/**
	 * Contains all CSTNU observation nodes: a CSTNU node is an observation if its execution determines the value of a boolean proposition associated to the
	 * node.
	 */
	private SortedSet<LabeledNode> obsNode = null;

	/**
	 * Contains all labeled constraint present into CSTNU constraints organized by label.
	 */
	private TreeMap<Label, HashSet<Constraint>> allConstraintsByLabel = null;

	/**
	 * Output stream to fOutput
	 */
	private PrintStream output = null;

	/**
	 * Name of loop clock.
	 */
	@Option(required = false, name = "--loop", usage = "Name of loop clock", metaVar = "t__Delta")
	private String tDelta = "t__Delta";

	/**
	 * Name of global clock.
	 */
	@Option(required = false, name = "--global", usage = "Name of global clock", metaVar = "t__G")
	private String tG = "t__G";

	/**
	 * Name of environment state/node
	 */
	@Option(required = false, name = "--environment", usage = "Name of environment node", metaVar = "vera")
	private String VERA = "vera";

	/**
	 * Software Version.
	 */
	@Option(required = false, name = "-v", aliases = "--version", usage = "Version")
	private boolean versionReq = false;

	/**
	 * Parameter for asking to determine a compact version (with less state) of the automata.
	 */
	@Option(required = false, name = "-compact", usage = "Translate using the minimal number of states")
	private boolean compact = false;

	/**
	 * parameter ignore
	 * 
	 * @Option(required = false, name = "-ignore", usage = "")
	 *                  private String ignore = "";
	 */

	/**
	 * To verify if a cstn has syntax ok!
	 */
	private CSTNU cstnuCheck;

	/**
	 * In a TGA, a location element contains the declaration of a node.
	 * In the CSTNU translation, there are three constants location: controller (id={@link #VERA}), environment (id={@link #AGNES}) and goal (id={@link #GOAL}).
	 * Then, there are one location for each CSTNU observation node, (id=nodeName without?
	 * 
	 * @param template
	 */
	private void addLocationElements(Element template) {
		Document localDoc = template.getOwnerDocument();

		template.appendChild(buildLocationElement(this.AGNES, true));
		template.appendChild(buildLocationElement(this.VERA, false));
		template.appendChild(buildLocationElement(this.GOAL, false));
		template.appendChild(buildLocationElement(this.GO, true));

		// Add a urgent location for each proposition
		// 13/06/2014: No more!
		// for (final LabeledNode node : obsNode) {
		// template.appendChild(buildLocationElement(getObsNodeName(node), true));
		// }

		if (!this.compact) {
			// we add as many intermediate node GO_i as the number of significant label is.
			int n = this.allConstraintsByLabel.size() - 1;// the empty label does not count!
			if (n > 0) {
				for (int i = 1; i < n; i++) {
					template.appendChild(buildLocationElement(this.GO + i, true));
				}
			}
		}
		// The declaration of the initial node
		Element init = localDoc.createElement("init");
		init.setAttribute("ref", this.AGNES);
		template.appendChild(init);
	}

	/**
	 * In a TGA, a transition element contains the declaration of a transition between two nodes of the automaton.
	 * In the CSTNU translation, there are 8 kinds of transition.
	 * 
	 * @param template
	 */
	private void addTransitionElements(Element template) {
		// first of all, the always present transitions
		template.appendChild(this.doc.createComment("GAIN transition"));
		// c1) The transition to guarantee to Agnes to gain the control (GAIN)
		// It is only one: (vera, tDelta > 0, gain, "", agnes)
		template.appendChild(buildTransitionElement(this.VERA, this.tDelta + " > 0", "gain", "", this.AGNES, false));

		template.appendChild(this.doc.createComment("PASS transition"));
		// c2) The transition to return the control to Vera
		// It is only one: (vera, tDelta > 0, pass, "", agnes)
		template.appendChild(buildTransitionElement(this.AGNES, "", "pass", this.tDelta + " := 0", this.VERA, false));

		template.appendChild(this.doc.createComment("Transitions for clock setting"));
		// 1) Set of transaction
		// For each free time-point X there is a non controllable transaction (agnes, !xX, set_X, xX:=true, tX:=0, agnes)
		for (final LabeledNode n : this.freeNode) {
			String exec = getExecutedName(n);
			template.appendChild(
					buildTransitionElement(this.AGNES, "!" + exec, "set_" + exec, exec + " := true," + getClockName(n) + " := 0", this.AGNES, false));
		}

		template.appendChild(this.doc.createComment("Transitions for proposition setting"));
		// 2) Set of transaction
		// For each Observation node P, there are four transactions
		// 2.1) (agnes, !xP, set_P, xP:=true, tP:=0, agnes) not controllable, for the clock
		// 2.2) (vera, xP && P == 0, set_P_false, P := -1, vera) controllable, to allow ENV to decide the value
		// 2.3) (vera, xP && P == 0, set_P_true, P := 1, agnes) controllable, to allow ENV to decide the value
		// 2.4) (agnes, xP && P == 0 && tDelta > 0, P_not_set, "", goal) not controllable, to force ENV to decide the value
		for (final LabeledNode n : this.obsNode) {
			String exec = getExecutedName(n);
			String prop = getPropositionName(n);
			template.appendChild(buildTransitionElement(this.AGNES, "!" + exec + AND + prop + " == 0", "set_" + prop, exec + " := true, " + getClockName(n)
					+ " := 0", this.AGNES, false));
			template.appendChild(
					buildTransitionElement(this.VERA, exec + AND + prop + " == 0", "set_" + prop + "_false", prop + " := -1, " + this.tDelta + " := 0",
							this.VERA,
							true));
			template.appendChild(
					buildTransitionElement(this.VERA, exec + AND + prop + " == 0", "set_" + prop + "_true", prop + " := 1, " + this.tDelta + " := 0", this.VERA,
							true));
			template.appendChild(
					buildTransitionElement(this.AGNES, exec + AND + prop + " == 0" + AND + this.tDelta + " > 0", prop + "not_set", "", this.GOAL, false));
		}

		template.appendChild(this.doc.createComment("Transitions for contingent-constraint setting"));
		// 3) Set of transaction
		// For each contingent link (A,l,u,C) there is
		// 3.1) a transaction (vera, Sigma(tC,tA,tG), set_C, "xC:=true, tC:=0, tDelta:=0", vera) where Sigma(tC,tA,tG) := xA && !xC && (tA >= l) && (tA <=
		// u)//assign a right value to contingent point.
		// 3.2) a transition (agnes, Phi(tA,tC,tG), cvC, "", goal) where Phi(tA,tC,tG) := xA && !xC && tA>u //the contingent has violated its upper bound!
		// Remember that in TGA contingent point are chosen by a controllable transaction that can be 'blocked' by tDelta. So, if a controllable transaction for
		// tC cannot be executed, then the system verifies later with 3.2 the violation.
		for (final Contingent c : this.contingentEdge) {
			String tA = getClockName(c.source);
			String tC = getClockName(c.dest);
			String xA = getExecutedName(c.source);
			String xC = getExecutedName(c.dest);

			String sigma = xA + AND + "!" + xC + AND + "(" + tA + " >= " + c.lower + ")" + AND + "(" + tA + " <= " + c.upper + ")";
			template.appendChild(
					buildTransitionElement(this.VERA, sigma, "set_" + xC, xC + " := true, " + tC + " := 0, " + this.tDelta + " := 0", this.VERA, true));

			String phi = xA + AND + "!" + xC + AND + "(" + tA + " > " + c.upper + ")";
			template.appendChild(buildTransitionElement(this.AGNES, phi, "cv_" + tC, "", this.GOAL, false));
		}

		template.appendChild(this.doc.createComment("WIN transitions"));
		/**
		 * 4) The transition for the end of the game.
		 * We split the win transition into two sets:
		 * 1) one set is made of only one uncontrollable transition (VERA, Psi(t, tB), win_unlabelled, "", go)
		 * where Psi1 = AND_{X timepoint} xX && AND_{unlabeled non-contingent constraint Y-X<k} (tY-tX <= k)
		 * It represents the event that all timepoints are executed and all unlabeled constraints are satisfied.
		 * Such constraint is always present.
		 * 2) other set is made considering all labeled constraints.
		 * There are two possible set constructions according to the value of 'compact' value.
		 * Before showing how to build them, remember that allLabeledConstraint is a Map that, for each label, returns all constraints with the given label.
		 * If compact is false, then for each label l (assume l is the i-th one in the lexicographical order),
		 * we put a transition for each of its literal with guard the negated literal and one transition with guard the conjunction of all associated
		 * constraints
		 * between state GO_i and GO_{i+1}, where GO_0 = GO and GO_{m} = GOAL (m=#labels)
		 * If compact is true, for each label l, the implicant "l => conjunction of all associated constraints" is built.
		 * Then, we define \Psi2 = conjunction of all obtained implicants.
		 * Then, we determine \Psi2DNF := the DNF of \Psi2
		 * Then, for each disjunct d_i of \Phi2DNF, we add the following controllable transition (go, d_i, win_labelled, "", goal)
		 * If there is no labeled constraints, we add only (go, "", win_labelled, "", goal).
		 * All this transitions have to be uncontrollable.
		 */

		// First set
		StringBuffer psi1dirty = new StringBuffer();
		for (final LabeledNode n : this.freeNode) {
			String exec = getExecutedName(n);
			psi1dirty.append(exec + AND);
		}
		for (final LabeledNode n : this.contingentNode) {
			String exec = getExecutedName(n);
			psi1dirty.append(exec + AND);
		}
		for (final LabeledNode n : this.obsNode) {
			String exec = getExecutedName(n);
			psi1dirty.append(exec + AND);
		}

		StringBuffer psi2dirty = new StringBuffer();
		final String jboolAnd = " & ";
		final String jboolOr = " | ";
		final String jboolNot = " !";

		int labelOrdinal = 0;
		String sourceState = null;
		String destState = null;
		for (Entry<Label, HashSet<Constraint>> entry : this.allConstraintsByLabel.entrySet()) {// entrySet read-only
			Label label = entry.getKey();
			HashSet<Constraint> constSet = entry.getValue();

			if (!this.compact && !label.isEmpty()) {
				sourceState = (labelOrdinal == 0) ? this.GO : this.GO + labelOrdinal;
				labelOrdinal++;
				destState = (labelOrdinal == this.allConstraintsByLabel.size() - 1) ? this.GOAL : this.GO + labelOrdinal;
				for (Literal lit : label.negation()) {
					template.appendChild(buildTransitionElement(sourceState, "(" + lit.getName() + " == " + ((lit.isNegated()) ? "-1" : "1") + ")",
							"win_labelled" + labelOrdinal + lit.getName(), "", destState, false));
					LOG.finest("Transition added= (" + sourceState + ", (" + lit.getName() + " == " + ((lit.isNegated()) ? "-1" : "1") + "), "
							+ "win_labelled" + labelOrdinal + lit.getName() + ", '', " + destState + ")");
				}
				psi2dirty.delete(0, psi2dirty.length());
			}
			for (Constraint constr : constSet) {
				String sourceClock = getClockName(constr.source);
				String destClock = getClockName(constr.dest);
				Integer value = constr.value;
				if (label.isEmpty()) {
					// First set
					// Be careful!
					// A constraint (D-S≤v) has to be translated as (tS-tD≤v) because node clock will be reset when node is executed!
					psi1dirty.append("(" + sourceClock + " - " + destClock + " <= " + value + ")" + AND);
				} else {
					if (!this.compact) {
						// Be careful!
						// A constraint (D-S≤v) has to be translated as (tS-tD≤v) because node clock will be reset when node is executed!
						psi2dirty.append("(" + sourceClock + " - " + destClock + " <= " + value + ")" + AND);
					} else {
						// psi2 will be transformed into DNF.
						// To convert into DNF I will use an external library: com.bpodgursky.jbool_expressions
						// Therefore it is convenient to write the expression into jbool_expressions format
						// and = &
						// or = |
						// not = !
						// identificator name = `name`
						// true = true
						// false = false
						// no other operator.
						// So, (a¬b => (A-B <= t)) has to be represented as (!`a` | 'b' | `(A-B <= t)`)
						String labelNegatedandEscaped = label.toLogicalExpr(true, jboolNot, jboolAnd, jboolOr);

						labelNegatedandEscaped = labelNegatedandEscaped.replaceAll("(" + Literal.PROPOSITION_RANGE + ")", "`$1`");// tutto a 1
						// LOG.finest("labelNegatedandEscaped= " + labelNegatedandEscaped);
						psi2dirty.append("(" + labelNegatedandEscaped + jboolOr + "`((" + sourceClock + " - " + destClock + ") <= " + value + ")`)" + jboolAnd);
					}
				}
			}
			if (!this.compact && !label.isEmpty()) {
				final String psi2clean = psi2dirty.substring(0, psi2dirty.length() - AND.length());
				// LOG.finest("psi2" + labelOrdinal + "= " + psi2clean);
				template.appendChild(buildTransitionElement(sourceState, psi2clean, "win_labelled" + labelOrdinal, "", destState, false));
				LOG.finest("Transition added= (" + sourceState + ", " + psi2clean + ", win_labelled" + labelOrdinal + ", '', " + destState + ")");

			}
		}

		// First set
		String psi1 = psi1dirty.substring(0, psi1dirty.length() - AND.length());
		template.appendChild(buildTransitionElement(this.VERA, psi1, "win_unlabelled", "", this.GO, false));

		// Second set when !useStatesForScenarios
		LOG.finest("psi2dirty= " + psi2dirty);
		if (this.allConstraintsByLabel.size() == 1) {
			// there are no labeled constraints!
			template.appendChild(buildTransitionElement(this.GO, "", "win_labelled", "", this.GOAL, false));
		} else {
			if (this.compact) {
				String psi2 = psi2dirty.substring(0, psi2dirty.length() - jboolAnd.length());
				String psi2DNF = "";
				LOG.finest("psi2= " + psi2);

				// I use jbool_expression to obtain the DNF of psi2
				Expression<String> psi2Jbool = ExprParser.parse(psi2);

				String psi2DnfJbool = RuleSet.toSop(psi2Jbool).toString();
				psi2DnfJbool = psi2DnfJbool.substring(1, psi2DnfJbool.length() - 1);// there are an initial and a final () !
				LOG.finest("psi2DnfJbool= " + psi2DnfJbool);

				// A literal "`l`" has to be transformed to "(l == 1)"
				// while "!`l`" to "(l == -1)"
				psi2DnfJbool = psi2DnfJbool.replaceAll("`(" + Literal.PROPOSITION_RANGE + ")`", "\\($1 == 1\\)");// tutto a 1
				psi2DnfJbool = psi2DnfJbool.replaceAll("!\\((" + Literal.PROPOSITION_RANGE + ") == 1", "\\($1 == -1");// mentre si mette a -1 chi è
																														// negato!

				psi2DNF = jbool2TigaExpr(psi2DnfJbool, null, null, " | ");
				LOG.finest("psi2DNF= " + psi2DNF);

				int i = 0;
				for (String psi2Disjunct : psi2DNF.split(" \\| ")) {
					template.appendChild(buildTransitionElement(this.GO, psi2Disjunct, "win_labelled_" + i++, "", this.GOAL, false));
				}
			}
		}
	}

	/**
	 * In a TGA, declaration element contains the declaration of all clocks and variables.
	 * There are:
	 * <ol>
	 * <li>For each CSTNU node X, one clock 'tX'; a global clock and a loop clock, called delta clock.
	 * <li>For each CSTNU node X, one bool var 'xX' that says if X has been executed or not.
	 * <li>For each observed proposition P, one integer var 'pP' assuming only value 0, for no set, -1, for false and 1 for true. .
	 * </ol>
	 * 
	 * @return the element representing the clock declaration.
	 */
	private Element buildDeclarationElement() {
		Element declaration = this.doc.createElement("declaration");

		StringBuffer clocks = new StringBuffer("clock " + this.tG + ", " + this.tDelta);
		StringBuffer executed = new StringBuffer();
		StringBuffer obs = new StringBuffer();
		for (LabeledNode node : this.freeNode) {
			clocks.append(", " + getClockName(node));
			executed.append(", " + getExecutedName(node));
		}
		for (LabeledNode node : this.contingentNode) {
			clocks.append(", " + getClockName(node));
			executed.append(", " + getExecutedName(node));
		}
		for (LabeledNode node : this.obsNode) {
			clocks.append(", " + getClockName(node));
			executed.append(", " + getExecutedName(node));
			obs.append(", " + getPropositionName(node) + " = 0");
		}
		clocks.append(';');
		if (executed.length() > 0) {
			executed.replace(0, 1, "bool"); // remove first , and put "bool " declaration
			executed.append(';');
		}
		if (obs.length() > 0) {
			obs.replace(0, 1, "int [-1,1]"); // remove first , and put "int [-1,1] " declaration
			obs.append(';');
		}

		declaration.appendChild(this.doc.createTextNode(clocks + "\n" + executed + "\n" + obs));
		return declaration;
	}

	/**
	 * Build a location element with id and name = id and urgent child if urgent is true.
	 * 
	 * @param id
	 * @param urgent
	 * @return the location element
	 */
	private Element buildLocationElement(String id, Boolean urgent) {
		Element location = this.doc.createElement("location");
		location.setAttribute("id", id);
		Element name = this.doc.createElement("name");
		name.appendChild(this.doc.createTextNode(id));
		location.appendChild(name);
		if (urgent) {
			location.appendChild(this.doc.createElement("urgent"));
		}
		return location;
	}

	/**
	 * In a TGA, template element contains the declaration of all nodes and transitions.
	 * 
	 * @return the element representing all clock, node, and transition declaration.
	 */
	private Element buildTemplateElement() {
		Element template = this.doc.createElement("template");

		// name
		Element name = this.doc.createElement("name");
		name.appendChild(this.doc.createTextNode(getTgaName(this.cstnu)));
		template.appendChild(name);

		// local declaration
		template.appendChild(this.doc.createComment("Clock and proposition declarations"));
		template.appendChild(buildDeclarationElement());

		template.appendChild(this.doc.createComment("LabeledNode declarations"));
		addLocationElements(template);

		template.appendChild(this.doc.createComment("Transition declarations"));
		addTransitionElements(template);

		Element system = this.doc.createElement("system");
		system.appendChild(this.doc.createTextNode("_processMain = " + getTgaName(this.cstnu) + "();\n\t\tsystem _processMain;"));
		template.appendChild(system);
		return template;
	}

	/**
	 * Build a transition element with source, target, guard, assignment, controllable attributes.
	 * 
	 * @param source
	 * @param guard
	 * @param action
	 * @param assignment
	 * @param target
	 * @param controllable
	 * @return the location element
	 */
	private Element buildTransitionElement(String source, String guard, String action, String assignment, String target, boolean controllable) {
		Element transition = this.doc.createElement("transition");
		transition.setAttribute("controllable", controllable ? "true" : "false");
		transition.setAttribute("action", action);

		Element sourceE = this.doc.createElement("source");
		sourceE.setAttribute("ref", source);
		transition.appendChild(sourceE);

		Element targetE = this.doc.createElement("target");
		targetE.setAttribute("ref", target);
		transition.appendChild(targetE);

		Element label = this.doc.createElement("label");
		label.setAttribute("kind", "guard");
		label.appendChild(this.doc.createTextNode(guard));
		transition.appendChild(label);

		label = this.doc.createElement("label");
		label.setAttribute("kind", "assignment");
		label.appendChild(this.doc.createTextNode(assignment));
		transition.appendChild(label);

		return transition;
	}

	/**
	 * Default constructor not accessible
	 */
	private CSTNU2UppaalTiga() {
		this.cstnuCheck = new CSTNU();
	}

	/**
	 * Constructor for CSTNU2UppaalTiga.
	 *
	 * @param g a {@link it.univr.di.cstnu.graph.TNGraph} object.
	 * @param o a {@link java.io.PrintStream} object.
	 */
	public CSTNU2UppaalTiga(TNGraph<CSTNUEdge> g, PrintStream o) {
		this();
		if (o == null || g == null)
			throw new IllegalArgumentException("One parameter is null!");
		this.output = o;
		this.cstnu = g;
		if (!checkCSTNUSyntax())
			throw new IllegalArgumentException("CSTNU is not well formed!");
	}

	/**
	 * Load CSTNU file and create a TNGraph g.
	 * 
	 * @param fileName
	 * @return tNGraph if the file was load successfully; null otherwise.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	private boolean loadCSTNU(File fileName) throws IOException, ParserConfigurationException, SAXException {
		TNGraphMLReader<CSTNUEdge> graphMLReader = new TNGraphMLReader<>();
		this.cstnu = graphMLReader.readGraph(fileName, EdgeSupplier.DEFAULT_CSTNU_EDGE_CLASS);
		return checkCSTNUSyntax();
	}

	/**
	 * @return true if the CSTNU is well written, false otherwise.
	 */
	private boolean checkCSTNUSyntax() {
		LOG.finest("Checking tNGraph...");
		try {
			this.cstnuCheck.initAndCheck();
		} catch (IllegalArgumentException | WellDefinitionException e) {
			System.err.println(e.getMessage());
			return false;
		}
		prepareAxuliaryCSTNUData();
		LOG.finest("TNGraph checked!");
		return true;
	}

	/**
	 * Simple method to manage command line parameters using args4j library.
	 * 
	 * @param args
	 * @return false if a parameter is missing or it is wrong. True if every parameters are given in a right format.
	 */
	@SuppressWarnings("deprecation")
	private boolean manageParameters(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			if (!this.fInput.exists())
				throw new CmdLineException(parser, "Input file does not exist.");

			if (this.fOutput != null) {
				if (this.fOutput.isDirectory())
					throw new CmdLineException(parser, "Output file is a directory.");
				if (!this.fOutput.getName().endsWith(".xml")) {
					String name = this.fOutput.getAbsolutePath() + ".xml";
					this.fOutput = new File(name);
				}
				if (this.fOutput.exists()) {
					if (!this.fOutput.delete()) {
						String m = "File " + this.fOutput.getAbsolutePath() + " cannot be deleted.";
						LOG.severe(m);
						throw new RuntimeException(m);
					}
				}
				try {
					if (!this.fOutput.createNewFile()) {
						LOG.warning("Cannot create " + this.fOutput.getName());
					}
					this.output = new PrintStream(this.fOutput, "UTF-8");
				} catch (IOException e) {
					throw new CmdLineException(parser, "Output file cannot be created.");
				}
			} else {
				this.output = System.out;
			}
		} catch (CmdLineException e) {
			// if there's a problem in the command line, you'll get this exception. this will reportan error message.
			System.err.println(e.getMessage());
			System.err.println("java CSTNU2UppaalTiga [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("Example: java -jar CSTNU2UppaalTiga.jar" + parser.printExample(OptionHandlerFilter.REQUIRED) + " <CSTNU_file_name>");
			return false;
		}
		return true;
	}

	/**
	 * Classifies nodes of the cstnu tNGraph
	 */
	private void prepareAxuliaryCSTNUData() {

		this.freeNode = new ObjectRBTreeSet<>();
		this.contingentNode = new ObjectRBTreeSet<>();
		this.contingentEdge = new ObjectRBTreeSet<>();
		this.obsNode = new ObjectRBTreeSet<>();
		this.allConstraintsByLabel = new TreeMap<>();
		// I put the entry for all constraints not labeled
		HashSet<Constraint> constr = new HashSet<>();
		this.allConstraintsByLabel.put(Label.emptyLabel, constr);

		// This cycle is redundant, but to avoid to consider an already considered node is more expensive that consider it more times
		SortedSet<CSTNUEdge> edges = new ObjectRBTreeSet<>(this.cstnu.getEdges());
		for (CSTNUEdge e : edges) {
			LabeledNode s = this.cstnu.getSource(e);
			LabeledNode d = this.cstnu.getDest(e);
			if (e.isContingentEdge()) {
				LOG.fine("Found a contingent link: " + e);
				int lower = e.getLowerCaseValue().getValue(), upper;// TODO This works only for CSTNU without guarded links.
				if (lower != Constants.INT_NULL) {
					LOG.fine("Add contingent node: " + d);
					this.contingentNode.add(d);
					upper = -this.cstnu.findEdge(d, s).getMinUpperCaseValue().getValue().getIntValue();
					if (upper == Constants.INT_NULL)
						throw new IllegalArgumentException(
								"There is no a companion upper case value in edge " + this.cstnu.findEdge(d, s) + " w.r.t. the edge" + e);
					LOG.fine("Add contingent edge: (" + s.getName() + ", " + lower + ", " + upper + ", " + d.getName() + ").");
					this.contingentEdge.add(new Contingent(s, lower, upper, d));
				}
			} else {
				// normal or constraint edge
				if (s.getPropositionObserved() != Constants.UNKNOWN)
					this.obsNode.add(s);
				else
					this.freeNode.add(s);

				if (d.getPropositionObserved() != Constants.UNKNOWN)
					this.obsNode.add(d);
				else
					this.freeNode.add(d);
				for (Entry<Label, Integer> entry : e.getLabeledValueMap().entrySet()) {
					// Since CSTNU has been initialized, the default value is represented as labeled value with ⊡ label.
					Label label = entry.getKey().conjunction(s.getLabel()).conjunction(d.getLabel());// IT IS NECESSSARY for guaranteeing all possible scenario
																										// are represented.
					Integer value = entry.getValue();
					constr = this.allConstraintsByLabel.get(label);
					if (constr == null) {
						constr = new HashSet<>();
						this.allConstraintsByLabel.put(label, constr);
					}
					constr.add(new Constraint(d, s, value));
				}
			}
		}
		// Free node contains contingent nodes if these last ones are destination of normal edge.
		// It is necessary to remove them.
		this.freeNode.removeAll(this.contingentNode);
		LOG.finest("freeNode set: " + this.freeNode.toString());
		LOG.finest("contingentNode set: " + this.contingentNode.toString());
		LOG.finest("obsNode set: " + this.obsNode.toString());
		LOG.finest("all Label set: " + this.allConstraintsByLabel.toString());
	}

	/**
	 * Convert a CSTNU TNGraph g into a Timed Game Automata in the UPPAAL TIGA format.
	 *
	 * @return true if the translation has been done and saved.
	 */
	public boolean translate() {

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			docFactory.setNamespaceAware(true);
			docFactory.setValidating(true);
			docFactory.setExpandEntityReferences(false);
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			DocumentType docType = docBuilder.getDOMImplementation().createDocumentType(
					"nta",
					"-//Uppaal Team//DTD Flat System 1.1//EN",
					"http://www.it.uu.se/research/group/darts/uppaal/flat-1_1.dtd");
			this.doc = docBuilder.getDOMImplementation().createDocument(null, "nta", docType);

			Element rootElement = this.doc.getDocumentElement();

			// global declaration element
			// rootElement.appendChild(buildDeclarationElement(doc, g));

			// template
			rootElement.appendChild(buildTemplateElement());

			// Get the implementations
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

			DOMImplementationLS implLS = (DOMImplementationLS) registry.getDOMImplementation("LS");
			// Prepare the output
			LSOutput domOutput = implLS.createLSOutput();
			domOutput.setEncoding(StandardCharsets.UTF_8.name());
			domOutput.setByteStream(this.output);
			// Prepare the serialization
			LSSerializer domWriter = implLS.createLSSerializer();
			domWriter.setNewLine("\r\n");
			DOMConfiguration domConfig = domWriter.getDomConfig();
			domConfig.setParameter("format-pretty-print", true);
			domConfig.setParameter("element-content-whitespace", true);
			domConfig.setParameter("cdata-sections", Boolean.TRUE);
			// And finally, write
			domWriter.write(this.doc, domOutput);

			if (this.fOutput != null) {
				this.output.close();
				String name = this.fOutput.getAbsolutePath().replace(".xml", ".q");
				this.output = new PrintStream(name, "UTF-8");
			}
			this.output.println("control: A[] not _processMain." + this.GOAL);
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (InstantiationException e) {
			e.printStackTrace();
			return false;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return false;
		} catch (ClassCastException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
