package it.univr.di.wf;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Esperimenti con il caso di Or Receive a due canali.
 * 
 * @author posenato
 */
public class pathOrReceive {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(pathOrReceive.class.getName());

	/**
	 * Setta i valori dei range del grafo. 
	 * Si assume che i canali di comunicazione sono realizzati usando un nodo buffer per poter permettere la sincronizzazione con un receive.
	 * 
	 * Quindi ogni nodo buffer dovrebbe essere connesso al nodo End_Receive con un intervallo [0,\infty].
	 * In questo modo però si richiederebbe che tutti i nodi buffer siano attivi per poter attivare il nodo End_Receive.
	 * La semantica dell'OR Receive richiede invece che il Receive scatti (e quindi End_Receive sia acceso) quando almeno un messaggio arriva (quindi almeno un nodo buffer sia attivo).
	 * In IHI2012 abbiamo detto che si devono quindi attivare due scenari: uno in cui un buffer è posto in relazione di precedenza rispetto sia all'End_receive sia all'altro nodo buffer (che non viene connesso)
	 * l'altro scenario è l'analogo considerando l'altro nodo buffer come nodo di riferimento.
	 * 
	 * 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
		final int infty = DistanceGraph.defaultUB;

		g.setConnector(1, "Rs-Re", 3, 4);//coppia nodi per Receive; nodo[2] è End_Receive
		//nodo 3 è il buffer b1
		//nodo 4 è il buffer b2
		//nodo 5 è il dopo Receive
		g.setInternal(3, 2, "B1-Re", -infty, infty);
		g.setInternal(4, 2, "B2-Re", -infty, infty);
		g.setInternal(3, 2, "B1-Re", -2, 0);
		g.setInternal(4, 2, "B2-Re", -2, 0);
		
		g.setInternal(3, 4, "B1-B2", -infty, infty);//serve solo per poterlo avere in stampa
		g.setInternal(3, 5, "B1-E", 0, infty);
		g.setInternal(4, 5, "B2-E", 0, infty);
		
		g.setEdge(0, 1, "S-Rs", 1, 1);
		g.setEdge(0, 3, "S-B1", 3, 8);
		g.setEdge(0, 4, "S-B2", 3, 8);
		g.setEdge(2, 5, "Re-E", 1, 1);
		
		
		g.setConstraint(0, 5, "Start-End",	5, 5);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)  {
		

		LOG.getParent().setLevel(Level.ALL);
		LOG.getParent().getHandlers()[0].setLevel(Level.ALL);

		final int n = 6;
		DistanceGraph graph = new DistanceGraph(n);
		DistanceGraph.LOG.setLevel(Level.INFO);
		
		definizioneRange(graph);

		System.out.print("Range iniziali:\n"+graph.printRange(true));

		if (graph.checkDCPath(true)) System.out.print("Range finali:\n"+graph.printRange(true));

	}
}
