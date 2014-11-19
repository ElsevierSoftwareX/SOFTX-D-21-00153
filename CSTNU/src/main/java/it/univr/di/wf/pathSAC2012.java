package it.univr.di.wf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimenti con il caso T1|T2-T3-T4|T5. 
 * 
 * @author posenato
 */
public class pathSAC2012 {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(pathSAC2012.class.getName());

	/**
	 * Setta i valori dei range del grafo.
	 * 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
//		final int infty = DistanceGraph.defaultUB;

		g.setTask(0, "T1", 2, 4);
		g.setTask(2, "T2", 5, 20);
		g.setConnector(4, "C1", 1, 1);
		g.setTask(6, "T4", 25, 45);

		g.setEdge(1, 2, "T1T2", 3, 5);
		g.setEdge(3, 4, "T2C1", 1, 1);
//		g.setEdge(5, 6, "C1T4", 1, 1);
		g.setEdge(5, 6, "C1T4", 1, 40);

//		g.setConstraint(0, 6, "S_T1-S_T4", 15, 30);
		g.setConstraint(0, 7, "S_T1-S_T4", 15, 90);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 8;
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
