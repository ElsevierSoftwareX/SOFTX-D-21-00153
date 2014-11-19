package it.univr.di.wf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimenti con il caso T1|T2-T3-T4|T5. 
 * 
 * @author posenato
 */
public class pathT1_T3_T4 {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(pathT1_T3_T4.class.getName());

	/**
	 * Setta i valori dei range del grafo.
	 * 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
//		final int infty = DistanceGraph.defaultUB;

		g.setConnector(0, "C1", 1, 3);
		g.setConnector(4, "C2", 1, 2);
		g.setConnector(8, "C3", 1, 10);
		g.setConnector(12, "C4", 1, 2);

		g.setTask(2, "T1", 2, 6);
		g.setTask(6, "T3", 2, 7);
		g.setTask(10, "T4", 3, 8);

		g.setEdge(1, 2, "C1T1", 2, 2);
		g.setEdge(3, 4, "T1C2", 1, 1);
		g.setEdge(5, 6, "C2T3", 1, 1);
		g.setEdge(7, 8, "T3C3", 1, 1);
		g.setEdge(9, 10, "C3T4", 1, 1);
		g.setEdge(11, 12, "T4C4", 1, 1);
//		g.setEdge(7, 4, "T5O1", 1, 1);
//		g.setEdge(9, 12, "C2O2", 1, 1);
		

		g.setConstraint(0, 13, "S_C1-E_C4", 15, 34);
		g.setConstraint(2, 11, "S_T1-E_T4", 22, 27);
//		g.setConstraint(6, 11, "S_T5-E_T6", 40, 80);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 14;
		DistanceGraph graph = new DistanceGraph(n);

		// Per modificare il livello di log a livello programmatico,
		// prima modificare il livello del parente (anche dell'handler console)
		// e poi, in cascata, tutte i livelli delle classi di interesse.
		LOG.getParent().setLevel(Level.ALL);
//		LOG.getParent().getHandlers()[0].setLevel(Level.ALL);
		DistanceGraph.LOG.setLevel(Level.FINE);

		definizioneRange(graph);

		System.out.print("Range iniziali:\n" + graph.printRange(true));

		if (graph.checkDCPath(true))
			System.out.print("Range finali:\n" + graph.printRange(true));

	}
}
