package it.univr.di.wf;


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimento di un CTP strong consistent ma non overcostraint strong consistent.
 * Questo esperimento serve per produrre la prova dell'errore della dimostrazione della
 * strong consistency dell'articolo
 * CTP:  A new constraint-based formalism for conditional
         temporal planning

   by Ioannis Tsamardinos, Thierry Vidal, Martha Pollack
   published in 2002
   
 * Per poter sfruttare la classe DistanceGraph, il ctp è rappresentanto con connettori di durata nulla.
 * @author posenato
 */
public class PathT1_T2OT3Luke {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(PathT1_T2OT3Luke.class.getName());

	/**
	 * Setta i valori dei range del grafo.
	 * 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
		@SuppressWarnings("unused")
		final int infty = DistanceGraph.defaultUB;

//		g.setConnector(1, "C1", 0, 0);
//		g.setConnector(3, "A", 0, 0);
//		g.setConnector(5, "nA", 0, 0);
//		g.setConnector(7, "O1", 0, 0);

		g.setEdge(0, 1, "StC1", 1, 2);
		
		//ramo A
		g.setEdge(1, 2, "C1 A", 1, 1);
		g.setEdge(2, 4, " AO1", 2, 2);

		//ramo nA
		g.setEdge(1, 3, "C1nA", 1, 1);
		g.setEdge(3, 4, "nAO1", 3, 4);
		
		
		g.setConstraint(0, 4, "St-E_O1", 4, 6);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 9;
		DistanceGraph graph = new DistanceGraph(n);

		// Per modificare il livello di log a livello programmatico,
		// prima modificare il livello del parente (anche dell'handler console)
		// e poi, in cascata, tutte i livelli delle classi di interesse.
		LOG.getParent().setLevel(Level.INFO);
		LOG.getParent().getHandlers()[0].setLevel(Level.INFO);
		DistanceGraph.LOG.setLevel(Level.INFO);

		definizioneRange(graph);

		System.out.print("Range iniziali:\n" + graph.printRange(true));

		if (graph.checkConsistencyPath(true))
			System.out.print("Range finali:\n" + graph.printRange(true));

	}
}
