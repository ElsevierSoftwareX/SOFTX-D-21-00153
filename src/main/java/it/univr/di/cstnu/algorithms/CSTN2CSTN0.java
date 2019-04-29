package it.univr.di.cstnu.algorithms;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import it.univr.di.Debug;
import it.univr.di.cstnu.graph.LabeledIntEdge;
import it.univr.di.cstnu.graph.LabeledIntGraph;
import it.univr.di.cstnu.graph.LabeledNode;
import it.univr.di.labeledvalue.Constants;
import it.univr.di.labeledvalue.Label;
import it.univr.di.labeledvalue.LabeledIntMap;
import it.univr.di.labeledvalue.LabeledIntTreeMap;

/**
 * Simple class to represent and check Conditional Simple Temporal Network assuming epsilon semantics and reducing an instance to an appropriate CSTN where DC
 * checking is
 * made assuming instantaneous reaction semantics.
 * 
 * @author Roberto Posenato
 * @version $Id: $Id
 */
public class CSTN2CSTN0 extends CSTNEpsilonwoNodeLabels {

	/**
	 * Default labeledIntValueMap
	 */
	static final Class<? extends LabeledIntMap> labeledIntValueMap = LabeledIntTreeMap.class;

	/**
	 * logger
	 */
	@SuppressWarnings("hiding")
	static Logger LOG = Logger.getLogger(CSTN2CSTN0.class.getName());

	/**
	 * Version of the class
	 */
	@SuppressWarnings("hiding")
	// static final String VERSIONandDATE = "Version 1.0 - November, 15 2017";
	static final String VERSIONandDATE = "Version  1.1 - November, 20 2017";// It derives from CSTNEpsilonwoNodeLabels

	/**
	 * @param args an array of {@link java.lang.String} objects.
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException {
		defaultMain(args, new CSTN2CSTN0(), "Reduction to CSTN IR DC");
	}
	/**
	 * 
	 */
	private CSTN2CSTN0() {
		super();
	}

	/**
	 * @param reactionTime
	 * @param g
	 */
	public CSTN2CSTN0(int reactionTime, LabeledIntGraph g) {
		super(reactionTime, g);
	}

	/**
	 * @param reactionTime
	 * @param g
	 * @param timeOut
	 */
	public CSTN2CSTN0(int reactionTime, LabeledIntGraph g, int timeOut) {
		super(reactionTime, g, timeOut);
	}

	/**
	 * Checks the controllability of a CSTNU instance.
	 * This method transform the given CSTNU instance into a corresponding CSTN instance such that
	 * the original instance is dynamic <em>controllable</em> iff the corresponding CSTN is dynamic <em>consistent</em>.
	 */
	@Override
	public CSTNCheckStatus dynamicConsistencyCheck() throws WellDefinitionException {
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "Starting checking CSTN2CSTN0 DC...\n");
			}
		}

		initAndCheck();

		LabeledIntGraph nextGraph = new LabeledIntGraph(this.g, labeledIntValueMap);
		nextGraph.setName("Next graph");
		CSTNCheckStatus status = new CSTNCheckStatus();

		Instant startInstant = Instant.now();

		LOG.info("Conversion to the corresponding CSTN instance...");
		LabeledIntGraph cstnGraph = transform();
		LOG.info("Conversion to the corresponding CSTN instance done.");

		LOG.info("CSTN DC-checking...");
		CSTNIR3RwoNodeLabels cstnChecker = new CSTNIR3RwoNodeLabels(cstnGraph, this.timeOut);
		CSTNCheckStatus cstnStatus = cstnChecker.dynamicConsistencyCheck();
		LOG.info("CSTN DC-checking done.");

		status.finished = cstnStatus.finished;
		status.consistency = cstnStatus.finished;
		status.cycles = cstnStatus.cycles;
		status.r0calls = cstnStatus.r0calls;
		status.r3calls = cstnStatus.r3calls;
		status.labeledValuePropagationCalls = cstnStatus.labeledValuePropagationCalls;

		Instant endInstant = Instant.now();
		status.executionTimeNS = Duration.between(startInstant, endInstant).toNanos();

		if (!status.consistency) {
			if (Debug.ON) {
				if (LOG.isLoggable(Level.INFO)) {
					LOG.log(Level.INFO, "The CSTN instance is not DC controllable.\nStatus: " + status);
				}
			}
			return status;
		}

		// controllable && finished
		if (Debug.ON) {
			if (LOG.isLoggable(Level.INFO)) {
				LOG.log(Level.INFO, "The CSTNU instance is DC controllable.\nStatus: " + status);
			}
		}
		// Put all data structures of currentGraph in g
		// g.copyCleaningRedundantLabels(cstnGraph);
		// g.setName(originalName);
		return status;
	}

	/**
	 * Returns the corresponding CSTN having each observation node P? is replaced with a pair of nodes P? and P?0. P? is standard node while P?0 is a new
	 * observation node that observes 'p'.<br>
	 * P?0 is set to be at distance epsilon after P?, exactly.<br>
	 * 
	 * @return g represented as a CSTN0.
	 *         In order to minimize name conflicts, the new name associated to P? is P?^0.
	 */
	LabeledIntGraph transform() {
		LabeledIntGraph cstn = new LabeledIntGraph(this.g, labeledIntValueMap);

		int nOfObservers = this.g.getObserverCount();
		if (nOfObservers == 0) {
			return cstn;
		}

		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Input graph: " + this.g);
			}
		}
		Collection<LabeledNode> observers = new ArrayList<LabeledNode>(this.g.getObservers());// this.g.getObservers() will change at each
																								// oldObs.setObservable(Constants.UNKNOWN);

		for (final LabeledNode oldObs : observers) {
			LabeledNode newObs = this.g.getNodeFactory().get(oldObs);
			newObs.setName(newObs.getName() + "^0");
			newObs.setX(newObs.getX() + 30);
			newObs.setY(newObs.getY() + 30);
			oldObs.setObservable(Constants.UNKNOWN);
			while (!cstn.addVertex(newObs)) {
				newObs.setName(newObs.getName() + "0");// in case the name has been already used!
			}
			// add the two constraints for fixing the distance between the two nodes at epsilon.
			// To oldObs
			LabeledIntEdge newE = cstn.getEdgeFactory().get(newObs.getName() + "_" + oldObs.getName());
			newE.mergeLabeledValue(Label.emptyLabel, -this.getReactionTime());
			cstn.addEdge(newE, newObs, oldObs);
			// To newObs
			newE = cstn.getEdgeFactory().get(oldObs.getName() + "-" + newObs.getName());
			newE.mergeLabeledValue(Label.emptyLabel, this.getReactionTime());
			cstn.addEdge(newE, oldObs, newObs);
		}
		if (Debug.ON) {
			if (LOG.isLoggable(Level.FINEST)) {
				LOG.finest("Transformed graph: " + cstn);
			}
		}
		return cstn;
	}

}
