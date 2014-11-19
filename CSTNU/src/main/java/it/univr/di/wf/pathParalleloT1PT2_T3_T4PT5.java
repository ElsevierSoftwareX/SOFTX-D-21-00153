package it.univr.di.wf;

import java.util.logging.Logger;

/**
 * Esperimenti con il caso T1|T2-T3-T4|T5. Nel AND-Join si sperimentano link di ammortizzamento
 * interni.
 * 
 * @author posenato
 */
public class pathParalleloT1PT2_T3_T4PT5 {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(pathParalleloT1PT2_T3_T4PT5.class.getName());

	/**
	 * Setta i valori dei range del grafo. 
	 * @param g distance graph
	 */
	static void definizioneRange(DistanceGraph g) {
		final int infty = DistanceGraph.defaultUB;

		g.setConnector(0, "C1", 1, infty);
		g.setConnector(8, "C2", 1, 2);
		g.setInternal(6, 8, "C2L", 0, infty);
		g.setInternal(7, 8, "C2R", 0, infty);
		g.setConnector(12, "C3", 1, infty);
		g.setConnector(20, "C4", 1, 2);
		g.setInternal(18, 20, "C4L", 0, infty);
		g.setInternal(19, 20, "C4R", 0, infty);

		g.setTask(2, "T1", 2, 6);
		g.setTask(4, "T2", 4, 6);
		g.setTask(10, "T3", 2, 7);
		g.setTask(14, "T4", 3, 8);
		g.setTask(16, "T5", 5, 7);
		
		g.setEdge(1, 2, "C1T1", 1, infty);//2
		g.setEdge(1, 4, "C1T2", 1, infty);//2
		g.setEdge(3, 6, "T1C2", 1, 1);
		g.setEdge(5, 7, "T2C2", 1, 1);
		g.setEdge(9, 10, "C2T3", 1, 1);
		g.setEdge(11, 12, "T3C3", 1, 1);
		g.setEdge(13, 14, "C3T4", 1, 2);
		g.setEdge(13, 16, "C3T5", 1, 2);
		g.setEdge(15, 18, "C4T4", 1, 1);
		g.setEdge(17, 19, "T5C4", 1, 1);
		
		g.setConstraint(3, 5, "E_T1-E_T2",	-3, 3);
		
		g.setConstraint(0, 21, "S_C1-E_C4",	15, 34);
		g.setConstraint(2, 15, "S_T1-E_T4",	22, 28);
		g.setConstraint(4, 15, "S_T2-E_T4",	17, 28);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 22;
		DistanceGraph graph = new DistanceGraph(n);
		
		definizioneRange(graph);

		System.out.print("Range iniziali:\n"+graph.printRange(true));

		if (graph.checkDCPath(true)) System.out.print("Range finali:\n"+graph.printRange(true));

	}
}
