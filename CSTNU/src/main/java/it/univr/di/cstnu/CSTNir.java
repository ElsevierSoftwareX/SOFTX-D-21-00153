package it.univr.di.cstnu;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.uci.ics.jung.io.GraphIOException;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.univr.di.cstnu.graph.GraphMLReader;
import it.univr.di.cstnu.graph.GraphMLWriter;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.cstnu.graph.StaticLayout;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network (CSTN) where the edge weight are signed integer.
 * In this class, the dynamic consistency check is done assuming instantaneous reaction DC semantics (cf. ICAPS 2016 paper, table 1).
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTNir extends CSTN {

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTNir.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	static final String VERSIONandDATE = "Version  1.0 - April, 03 2017";// first release i.r.

	/**
	 * Just for using this class also from a terminal.
	 * 
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws FileNotFoundException
	 * @throws GraphIOException
	 */
	public static void main(final String[] args) throws FileNotFoundException, GraphIOException {
		LOG.finest("Start...");
		final CSTNir cstn = new CSTNir();

		if (!cstn.manageParameters(args))
			return;
		LOG.finest("Parameters ok!");
		if (cstn.versionReq) {
			System.out.println(CSTNir.class.getName() + " " + CSTNir.VERSIONandDATE + ". Academic and non-commercial use only.\n"
					+ "Copyright © 2017, Roberto Posenato");
			return;
		}

		LOG.finest("Loading graph...");
		GraphMLReader<LabeledIntGraph> graphMLReader = new GraphMLReader<>(cstn.fInput,LabeledIntTreeMap.class);
		cstn.setG(graphMLReader.readGraph());

		LOG.finest("LabeledIntGraph loaded!");

		LOG.finest("Instantaneous reaction DC Checking...");
		CSTNCheckStatus status;
		try {
			status = cstn.dynamicConsistencyCheck();
		} catch (final WellDefinitionException e) {
			System.out.print("An error has been occured during the checking: " + e.getMessage());
			return;
		}
		LOG.finest("LabeledIntGraph minimized!");
		if (status.finished) {
			System.out.println("Checking finished!");
			if (status.consistency) {
				System.out.println("The given CSTN is Dynamic consistent!");
			} else {
				System.out.println("The given CSTN is not Dynamic consistent!");
			}
			System.out.println("Details: " + status);
		} else {
			System.out.println("Checking has not been finished!");
			System.out.println("Details: " + status);
		}

		if (cstn.fOutput != null) {
			final GraphMLWriter graphWriter = new GraphMLWriter(new StaticLayout<>(cstn.getG()));
			try {
				graphWriter.save(cstn.getG(), new PrintWriter(cstn.output));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Default constructor. Label optimization.
	 */
	private CSTNir() {
		super();
		this.wd2epsilon = 0;
	}
	

	/**
	 * Constructor for CSTN.
	 * @param g the labeled int valued graph to check 
	 */
	public CSTNir(LabeledIntGraph g) {
		super(g);
		this.wd2epsilon = 0;
	}

	/**
	 * Applies rule R0/qR0: label containing a proposition that can be decided only in the future is simplified removing such proposition.
	 * <b>Instantaneous reaction DC semantics is assumed.</b>
	 * 
	 * <pre>
	 * R0:
	 * P? --[α p, w]--&gt; X 
	 * changes in 
	 * P? --[α', w]--&gt; X when w &lt; 0
	 * where:
	 * p is the positive or the negative literal associated to proposition observed in P?,
	 * α is a label,
	 * α' is α without 'p', P? children, and any children of possible q-literals.
	 * </pre>
	 * 
	 * Rule qR0 has X==Z.
	 * @param nObs the observation node
	 * @param nX the other node
	 * @param nZ
	 * @param ePX the edge connecting P? ---&gt; X
	 * 
	 * @return true if the rule has been applied one time at least.
	 */
	boolean labelModificationR0(final LabeledNode nObs, final LabeledNode nX, final LabeledNode nZ, final LabeledIntEdge ePX) {
		// Visibility is package because there is Junit Class test that checks this method.

		boolean ruleApplied = false, mergeStatus;
		final char p = nObs.getPropositionObserved();
		if (p == Constants.UNKNOWN) {
			if (LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, "Method labelModificationR0 called passing a non observation node as first parameter!");
			}
			return false;
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: start.");
		}
		if (nX.getLabel().contains(p)) {
			// Table 1 ICAPS
			// if (LOG.isLoggable(Level.FINER)) {
			// LOG.log(Level.FINER, "R0: Proposition " + p + " is present in the X label '" + X.getLabel() + ". R0 cannot be applied.");
			// }
			return false;
		}

		final ObjectSet<Label> obsXLabelSet = ePX.getLabeledValueMap().keySet();

		for (final Label l : obsXLabelSet) {
			if (l == null || !l.contains(p)) {// l can be nullified in a previous cycle.
				continue;
			}

			final int w = ePX.getValue(l);
			if (w == Constants.INT_NULL) {
				// the value has been removed in a previous merge! Verified that it is necessary on Nov, 26 2015
				continue;
			}

			if (w >= 0) {// Table 1 ICAPS paper for IR semantics!
				continue;
			}

			final Label alphaPrime = makeAlphaPrime(nX, nObs, nZ, p, l);
			if (alphaPrime == null) {
				continue;
			}
			// Prepare the log message now with old values of the edge. If R0 modifies, then we can log it correctly.
			String logMessage = null;
			if (LOG.isLoggable(Level.FINER)) {
				logMessage = "R0 simplifies a label of edge " + ePX.getName()
						+ ":\nsource: " + nObs.getName() + " ---" + CSTN.pairAsString(l, w) + "---> " + nX.getName()
						+ "\nresult: " + nObs.getName() + " ---" + CSTN.pairAsString(alphaPrime, w) + "---> " + nX.getName();
			}

			ePX.putLabeledValueToRemovedList(l, w);
			// PXinNextGraph.removeLabel(l); It is not necessary, the introduction of new label remove it!
			this.checkStatus.r0calls++;
			ruleApplied = true;
			mergeStatus = ePX.mergeLabeledValue(alphaPrime, w);
			if (mergeStatus && LOG.isLoggable(Level.FINER)) {
				LOG.log(Level.FINER, logMessage);
			}
			if (CSTN.isNewLabeledValueANegativeLoop(alphaPrime, w, nObs, nX, ePX)) {
				this.checkStatus.consistency = false;
				this.checkStatus.finished = true;
				return ruleApplied;
			}
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.log(Level.FINEST, "Label Modification R0: end.");
		}
		return ruleApplied;
	}

}
