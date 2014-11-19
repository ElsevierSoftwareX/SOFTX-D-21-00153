package it.univr.di.wf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimenti con il caso T1|T2-T3-T4|T5. Nel AND-Join si sperimentano link di ammortizzamento
 * interni.
 * 
 * @author posenato
 */
public class PathT1T10T11T12 {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(PathT1T10T11T12.class.getName());

	/**
	 * Setta i valori dei range del grafo.
	 * 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
		final int infty = DistanceGraph.defaultUB;

		g.setConnector(2, "C4", 1, 1);
		g.setConnector(6, "X3", 1, 1);
		g.setConnector(10, "C5", 1, 1);
		g.setConnector(14, "X4", 1, infty);

		g.setTask(0, "T8", 2, 2);
		g.setTask(4, "T10", 1, 4);
		g.setTask(8, "T11", 3, 9);
		g.setTask(12, "T12", 25, 35);
		g.setTask(16, "T14", 4, 7);

		g.setEdge(1, 2, "T8C4", 1, 1);
		g.setEdge(3, 4, "C4T10", 1, 1);
		g.setEdge(5, 6, "T10X3", 1, 1);
		g.setEdge(7, 8, "X3T11", 1, 1);
		g.setEdge(9, 10, "T11C5", 1, 1);
		g.setEdge(11, 12, "C5T12", 1, 1);
		g.setEdge(13, 14, "T12X4", 1, infty);
		g.setEdge(15, 16, "X4T14", 1, infty);

		g.setConstraint(0, 17, "S_T8-E_T14", 45, 70);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 18;
		DistanceGraph graph = new DistanceGraph(n);

		// Per modificare il livello di log a livello programmatico,
		// prima modificare il livello del parente (anche dell'handler console)
		// e poi, in cascata, tutte i livelli delle classi di interesse.
		LOG.getParent().setLevel(Level.ALL);
		LOG.getParent().getHandlers()[0].setLevel(Level.ALL);
		DistanceGraph.LOG.setLevel(Level.FINE);

		definizioneRange(graph);

		System.out.print("Range iniziali:\n" + graph.printRange(true));

		if (graph.checkConsistencyPath(true))
			System.out.print("Range finali:\n" + graph.printRange(true));

	}
}
