package it.univr.di.wf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimenti con il caso T1|T2-T3-T4|T5. Nel AND-Join si sperimentano link di ammortizzamento
 * interni.
 * 
 * @author posenato
 */
public class pathC1_C2 {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(pathC1_C2.class.getName());

	/**
	 * Setta i valori dei range del grafo.
	 * 
	 * @param g distance graph
	 */
	@SuppressWarnings("unused")
	static void definizioneRange(DistanceGraph g) {
		final int infty = DistanceGraph.defaultUB;

		g.setConnector(0, "C1", 1, 5);
		g.setConnector(2, "C2", 4, 5);

		g.setEdge(1, 2, "C1C2", 1, 1);

		g.setConstraint(0, 3, "S_C1-S_C2", 5, 6);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 4;
		DistanceGraph graph = new DistanceGraph(n);

		// Per modificare il livello di log a livello programmatico,
		// prima modificare il livello del parente (anche dell'handler console)
		// e poi, in cascata, tutte i livelli delle classi di interesse.
		LOG.getParent().setLevel(Level.ALL);
		LOG.getParent().getHandlers()[0].setLevel(Level.ALL);
		DistanceGraph.LOG.setLevel(Level.FINE);

		definizioneRange(graph);

		System.out.print("Range iniziali:\n" + graph.printRange(true));

		if (graph.checkDCPath(true))
			System.out.print("Range finali:\n" + graph.printRange(true));

	}
}
