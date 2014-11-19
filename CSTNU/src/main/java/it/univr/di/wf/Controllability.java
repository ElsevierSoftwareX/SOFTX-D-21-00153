package it.univr.di.wf;

import java.util.logging.Logger;

/**
 * @author posenato
 */
public class Controllability {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(Controllability.class.getName());

	/**
	 * Stampa intervalli per i task e i nuovi valori per i vincoli intertask nel caso in cui il
	 * grafo è quello per TIME2010
	 * 
	 * @param m matrice di adiacenza del grafo
	 * @param withEdge se true, stampa anche i valori degli edge
	 */
	static void printIntervalli(int[][] m, boolean withEdge) {
		System.out.printf("Durata C1:\t[%3d, %3d]\n", -m[1][0], m[0][1]);
		if (withEdge)
			System.out.printf("Durata C1-T:\t[%3d, %3d]\n", -m[2][1], m[1][2]);
		System.out.printf("Durata T1|T2:\t[%3d, %3d]\n", -m[3][2], m[2][3]);
		if (withEdge)
			System.out.printf("Durata T-C2:\t[%3d, %3d]\n", -m[4][3], m[3][4]);
		System.out.printf("Durata C2:\t[%3d, %3d]\n", -m[5][4], m[4][5]);
		if (withEdge)
			System.out.printf("Durata C2-T:\t[%3d, %3d]\n", -m[6][5], m[5][6]);
		System.out.printf("Durata T3:\t[%3d, %3d]\n", -m[7][6], m[6][7]);
		if (withEdge)
			System.out.printf("Durata T-C3:\t[%3d, %3d]\n", -m[8][7], m[7][8]);
		System.out.printf("Durata C3:\t[%3d, %3d]\n", -m[9][8], m[8][9]);
		if (withEdge)
			System.out.printf("Durata C3-T:\t[%3d, %3d]\n", -m[10][9], m[9][10]);
		System.out.printf("Durata T4|T5:\t[%3d, %3d]\n", -m[11][10], m[10][11]);
		if (withEdge)
			System.out.printf("Durata T-C4:\t[%3d, %3d]\n", -m[12][11], m[11][12]);
		System.out.printf("Durata C4:\t[%3d, %3d]\n", -m[13][12], m[12][13]);

		System.out.printf("Durata intertask C1-C4:\t\t\t\t[%3d, %3d]\n", -m[13][0], m[0][13]);
		System.out.printf("Durata intertask C3-T4|5:\t\t\t\t[%3d, %3d]\n", -m[11][2], m[2][11]);
	}

	/**
	 * Stampa intervalli per i task e i nuovi valori per i vincoli intertask.
	 * 
	 * @param m matrice di adiacenza del grafo
	 * @param withEdge se true, stampa anche i valori degli edge.
	 */
	static void printIntervalliParallelo(int[][] m, boolean withEdge) {
		System.out.printf("Durata C1:\t[%3d, %3d]\n", -m[1][0], m[0][1]);
		if (withEdge) {
			System.out.printf("Durata C1-T1:\t[%3d, %3d]\n", -m[2][1], m[1][2]);
			System.out.printf("Durata C1-T2:\t[%3d, %3d]\n", -m[4][1], m[1][4]);
		}
		System.out.printf("Durata T1:\t[%3d, %3d]\n", -m[3][2], m[2][3]);
		System.out.printf("Durata T1-C2INT:[%3d, %3d]\n", -m[8][6], m[6][8]);
		if (withEdge) {
			System.out.printf("Durata T1-C2:\t[%3d, %3d]\n", -m[6][3], m[3][6]);
		}
		System.out.printf("Durata T2:\t[%3d, %3d]\n", -m[5][4], m[4][5]);
		System.out.printf("Durata T2-C2INT:[%3d, %3d]\n", -m[8][7], m[7][8]);
		if (withEdge) {
			System.out.printf("Durata T2-C2:\t[%3d, %3d]\n", -m[7][5], m[5][7]);
		}

		System.out.printf("Durata C2:\t[%3d, %3d]\n", -m[9][8], m[8][9]);
		if (withEdge) {
			System.out.printf("Durata C2-T3:\t[%3d, %3d]\n", -m[10][9], m[9][10]);
		}

		System.out.printf("Durata T3:\t[%3d, %3d]\n", -m[11][10], m[10][11]);
		if (withEdge) {
			System.out.printf("Durata T3-C3:\t[%3d, %3d]\n", -m[12][11], m[11][12]);
		}
		System.out.printf("Durata C3:\t[%3d, %3d]\n", -m[13][12], m[12][13]);
		if (withEdge) {
			System.out.printf("Durata C3-T4:\t[%3d, %3d]\n", -m[14][13], m[13][14]);
			System.out.printf("Durata C3-T5:\t[%3d, %3d]\n", -m[16][13], m[13][16]);
		}
		System.out.printf("Durata T4:\t[%3d, %3d]\n", -m[15][14], m[14][15]);
		System.out.printf("Durata T5:\t[%3d, %3d]\n", -m[17][16], m[16][17]);
		if (withEdge) {
			System.out.printf("Durata T4-C4:\t[%3d, %3d]\n", -m[18][15], m[15][18]);
			System.out.printf("Durata T5-C4:\t[%3d, %3d]\n", -m[19][17], m[17][19]);
		}
		System.out.printf("Durata C4:\t[%3d, %3d]\n", -m[21][20], m[20][21]);

		System.out.printf("Durata T1-T4:\t[%3d, %3d]\n", -m[15][2], m[2][15]);
		System.out.printf("Durata T2-T4:\t[%3d, %3d]\n", -m[15][4], m[4][15]);
		System.out.printf("Durata C1-C4:\t[%3d, %3d]\n", -m[21][0], m[0][21]);

	}

	/**
	 * Restringe il valore di arco che precede un task (contigent edge follows a requirment one) ed
	 * è sottoposto a un vincolo con la fine del task.<br>
	 * 
	 * Per ora, si assume che l'arco sia sempre incidente al task.
	 * 
	 * @param m matrice delle distanze
	 * @param startE indice inizio arco
	 * @param startT indice inizio task
	 * @param endT indice fine task
	 */
	static void follow(int[][] m, int startE, int startT, int endT) {
		int u, v;

		LOG.finest("Caso follow arco ("+startE+", "+(startT)+") e task ["+startT+", "+endT+"].");
		u = -m[endT][startE] + m[endT][startT];// p-x_2;
		v = m[startE][endT] - m[startT][endT];// q-y_2

		if (u > (-m[startT][startE])) {
			LOG.fine("Restringimento lower bound caso follow. u = " + u + "; -m[" + startT + "][" + startE + "] = " + (-m[startT][startE]));
			m[startT][startE] = -u;
		}

		if (v < (m[startE][startT])) {
			LOG.fine("Restringimento upper bound caso follow. v = " + v + "; m[" + startE + "][" + startT + "] = " + (m[startE][startT]));
			m[startE][startT] = v;
		}
	}

	/**
	 * Dati due task, controlla se tra la fine del secondo e quella del primo c'è un vincolo con
	 * lower bound negativo e upper positivo.<br>
	 * In caso positivo, attiva un vincolo wait tra l'inizio del primo task e la fine del secondo e
	 * propaga tale wait tra l'inizio del primo e l'inizio del secondo.
	 * 
	 * @param m matrice di adiacenza
	 * @param startT1 indice nodo inizio del primo task. Indice nodo fine task è startT1+1
	 * @param startT2 indice nodo inizio del secondo task. Indice nodo fine task è startT2+1
	 */
	static void waitCheck(int[][] m, int startT1, int startT2) {
		
		int x1 = -m[startT1+1][startT1]; //l.b T1
		int y1 = m[startT1][startT1+1];//u.b. T1
		int x2 = -m[startT2+1][startT2];//l.b. T2
//		int y2 = m[startT2][startT2+1];
		int p = -m[startT1+1][startT2+1];//l.b. E_t2 --> E_t1
		int q = m[startT2+1][startT1+1];// u.b. E_t2 --> E_t1
		int u = -m[startT2+1][startT1];//l.b. S_t1-->E_T2
		int v = m[startT1][startT2+1];// u.b. S_t1-->E_t2
		
		//if (q<0) è il caso già gestito da Floyd-Warshall
		
		if (p>=0) {
			//caso follow però applicato al termine del task.
			//È il caso 2 del pattern (e) di TIME2010
			LOG.finer("Caso follow su end del task che termina: "+(startT1+1));
			LOG.finer("Valori esistenti: ["+u+", "+ v+"]");
			u = y1-q;
			v = x1-p;
			LOG.finer("Valori calcolati: ["+u+", "+v+"]."); 
		} else {
			if (p<0 && q>=0) {
				int t = y1-q;
				LOG.finer("Caso wait su end del task che termina in "+(startT1+1)+". [p,q]=["+p+", "+q+"]. Valore t:"+t);
				if (t<x1) {
					LOG.finer("Valore t (="+t+") inferiore a x_1 (="+x1+"). Aggiorno u (="+u+") a t (="+t+").");
					u = t;
				}
				int t2 = t-x2;
				int u2 = -m[startT2][startT1];
				if (t2<x1) {
					LOG.finer("Caso regressione wait attraverso T2 che inizia in "+startT2+". Valore t2:"+t2+". Valore lower bound S_T1-S_T2:"+u2);
					if (t2<u2) m[startT2][startT1]=t2;
				}
			}
		}
	}

	/**
	 * Restituisce vero se i range dei task elencati in taskIndex sono stati ristretti nella matrice
	 * b rispetto alla matrice a.
	 * 
	 * @param a
	 * @param b
	 * @param taskIndex array che indica, per ciascun task, il nodo iniziale.
	 * @return vero se i range dei task elencati in taskIndex sono stati ristretti nella matrice b
	 *         rispetto alla matrice a, falso altrimenti.
	 */
	static boolean shrikingCheck(int[][] a, int[][] b, int[] taskIndex) {
		// Verifico che nessun task ha avuto il range modificato
		// System.out.println("Verifica range dei task");
		for (int j = 0; j < taskIndex.length; j++) {
			if (a[taskIndex[j]][taskIndex[j] + 1] != b[taskIndex[j]][taskIndex[j] + 1]
					|| a[taskIndex[j] + 1][taskIndex[j]] != b[taskIndex[j] + 1][taskIndex[j]]) {
				System.out.println("\nIl task che inizia al nodo " + taskIndex[j]
						+ " ha subito una modifica del range: ["
						+ (-a[taskIndex[j] + 1][taskIndex[j]]) + ", "
						+ a[taskIndex[j]][taskIndex[j] + 1] + "] ==> ["
						+ (-b[taskIndex[j] + 1][taskIndex[j]]) + ", "
						+ b[taskIndex[j]][taskIndex[j] + 1] + "]");
				DistanceGraph.deepClone(b, a);
				return true;
			}
		}
		return false;
	}

	/**
	 * Esegue il check della controllabilità su path sequenziale, applicando la riduzione 'follow'.
	 * 
	 * @param m matrice di adiacenza
	 * @return true se il path è dinamicamente controllabile, falso altrimenti.
	 */
	static boolean checkDCPath(int[][] m) {
		// Assumo che la matrice sia già minimizzata
		int[][] m1 = new int[m.length][m.length];
		DistanceGraph.deepClone(m, m1);

		/**
		 * Indici dei nodi che rappresentano lo start dei task del path
		 */
		int[] sTask = { 2, 4, 10, 14, 16 };

		int ciclo = 1;
		boolean isDifferent = false, shrinked = false;
		System.out.print("Cicli: ");
		do {
			System.out.print((ciclo++) + ", ");
			// Applico le riduzioni "follow"
			// System.out.println("Applico riduzione");
			for (int i = 0; i < 9; i++) {
				for (int j = 0; j < sTask.length; j++) {
					if (i < sTask[j]) // si potrebbe mettere sTask[j] - 1 visto che l'edge che porta
						// al task è [1,1], ma non complichiamo le cose
						follow(m1, i, sTask[j], sTask[j] + 1);
				}
			}
			// Minimizzo
			// System.out.println("Minimizzo con FW");
			DistanceGraph.FloydWarshallI(m1);
			if (DistanceGraph.areNegativeCycles(m1)) {
				DistanceGraph.print(m1);
				return false;
			}

			shrinked = shrikingCheck(m, m1, sTask);
			if (shrinked)
				return false;

			// System.out.println("Verifica se le distanze sono cambiate");
			isDifferent = DistanceGraph.isDifferent(m, m1);

			// System.out.println("Aggiorno la matrice originale");
			DistanceGraph.deepClone(m1, m);

		} while (isDifferent);
		System.out.println("");
		return true;
	}

	/**
	 * Esegue il check della controllabilità su path parallelo T1|T2-T3-T4|T5, applicando la
	 * riduzione 'follow'.
	 * 
	 * @param m matrice dei nodi del grafo
	 * @return true se il path è dinamicamente controllabile, falso altrimenti.
	 */
	static boolean checkDCPathParallelo(int[][] m) {
		// Assumo che la matrice sia già minimizzata
		int[][] m1 = new int[m.length][m.length];
		DistanceGraph.deepClone(m, m1);

		/**
		 * Indici dei nodi che rappresentano lo start dei task del path
		 */
		int[] sTask = { 2, 4, 10, 14, 16 };

		/**
		 * Indice nodo inizio edge prima di un task
		 */
		int[] edges = { 1, 9, 13 };

		int ciclo = 1;
		boolean isDifferent = false, shrinked = false;
		System.out.print("Cicli: ");
		do {
			System.out.print((ciclo++) + ", ");
			// Applico le riduzioni "follow"
			// System.out.println("Applico riduzione");
			for (int i = 0; i < edges.length; i++) {
				for (int j = 0; j < sTask.length; j++) {
					if (edges[i] < sTask[j])
						follow(m1, edges[i], sTask[j], sTask[j] + 1);
				}
			}
			// Applico il wait check all'interno dei cammini paralleli
			waitCheck(m1, 2, 4);
			waitCheck(m1, 4, 2);
			waitCheck(m1, 14, 16);
			waitCheck(m1, 16, 14);
			
			// Minimizzo
			// System.out.println("Minimizzo con FW");
			DistanceGraph.FloydWarshallI(m1);
			if (DistanceGraph.areNegativeCycles(m1)) {
				System.out.println("Matrice delle distanze dopo Floyd-W:\n" + DistanceGraph.print(m1));
				return false;
			}

			shrinked = shrikingCheck(m, m1, sTask);
			if (shrinked)
				return false;

			// System.out.println("Verifica se le distanze sono cambiate");
			isDifferent = DistanceGraph.isDifferent(m, m1);

			// System.out.println("Aggiorno la matrice originale");
			DistanceGraph.deepClone(m1, m);

		} while (isDifferent);
		System.out.println("");
		return true;
	}

	/**
	 * Inizializza la matrice delle distanza con +defaultUB e aggiusta i tempi per i link e i
	 * connettori per l'esempio articolo TIME2010
	 * 
	 * @param m matrice delle distanze
	 */
	static void initMatrix(int[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = DistanceGraph.defaultUB;
			}
			m[i][i] = 0;
		}
		// inizializzo il L.B. di tutti gli edge e i connettori a -defaultLB
		m[1][0] = m[2][1] = m[4][3] = m[5][4] = m[6][5] = m[8][7] = m[9][8] = m[10][9] = m[12][11] = m[13][12] = m[13][12] = -DistanceGraph.defaultLB;
		// inizializzo l'U.B. di tutti gli edge a 1
		m[1][2] = m[3][4] = m[5][6] = m[7][8] = m[9][10] = m[11][12] = 1;

		m[0][13] = 34;
		m[13][0] = -25;// vincolo INTERTASK tra S_C1 e E_C4

	}

	/**
	 * Inizializza la matrice delle distanza con +defaultUB e aggiusta i tempi per i link e i
	 * connettori per l'esempio di verifica controllabilità con connettori AND estesi.
	 * 
	 * @param m matrice delle distanze
	 */
	static void initMatrixParallelo(int[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = DistanceGraph.defaultUB;
			}
			m[i][i] = 0;
		}
		// inizializzo il L.B. di tutti gli edge e i connettori a -defaultLB
		m[1][0] = m[4][1] = m[6][3] = m[7][5] = m[9][8] = m[10][9] = m[12][11] = m[13][12] = m[14][13] = m[16][13] = m[18][15] = m[19][17] = m[21][20] = -DistanceGraph.defaultLB;
		// inizializzo l'U.B. di tutti gli edge a 1
		m[1][4] = m[3][6] = m[5][7] = m[9][10] = m[11][12] = m[13][14] = m[13][16] = m[15][18] = m[17][19] = DistanceGraph.defaultLB;

		m[0][21] = 34;
		m[21][0] = -15;// vincolo INTERTASK tra S_C1 e E_C4

		// I vincoli artificiali per il sincronismo dei AND Join sono posti a [0,0] inizialmente per
		// compatibilità con il vecchio sistema di calcolo
		m[6][8] = m[8][6] = m[7][8] = m[8][7] = m[18][20] = m[20][18] = m[19][20] = m[20][19] = 0;

	}

	/**
	 * Controlla consistenza wf-path T1T3T4. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix(int[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(int[][])
	 */
	static void controllablePathT1T3T4(int[][] m) {
		initMatrix(m);
		// m[0][1] = 3;// UB x C1 imposto
		m[1][2] = 2;// Alterazione del edge C1-T1
		m[2][1] = -2;
		m[2][3] = 6; // UPPER nodo T1
		m[3][2] = -2; // LOWER nodo T1
		m[4][5] = 2;// UB imposto x C2

		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3

		// m[8][9] = 10;// UB imposto x C3

		m[10][11] = 8; // nodo T4
		m[11][10] = -3; // nodo T4
		m[12][13] = 2;// UB x C4 imposto

		m[11][2] = -22;// INTERTASK tra S_C3 e E_T4
		m[2][11] = 27;
	}

	/**
	 * Controlla consistenza wf-path T1T3T5. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix(int[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(int[][])
	 */
	static void controllablePathT1T3T5(int[][] m) {
		initMatrix(m);
		m[0][1] = 3;// UB imposto per C1
		m[1][2] = 2;// Alterazione del edge C1-T1
		m[2][1] = -2;
		m[2][3] = 6; // UPPER nodo T1
		m[3][2] = -2; // LOWER nodo T1

		m[4][5] = 2;// UB imposto x C2

		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3

		m[8][9] = 10;// UB imposto x C3
		// m[9][8] = -3;

		m[10][11] = 7; // nodo T5
		m[11][10] = -5; // nodo T5

		m[12][13] = 2;// UB x C4 imposto

		// m[11][8] = -7;// INTERTASK tra S_C3 e E_T5
		// m[8][11] = 14;
	}

	/**
	 * Controlla consistenza wf-path T2T3T5. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix(int[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(int[][])
	 */
	static void controllablePathT2T3T5(int[][] m) {
		initMatrix(m);
		m[0][1] = 3;// UB x C1 imposto

		m[2][3] = 6; // UPPER nodo T2
		m[3][2] = -4; // LOWER nodo T2

		m[4][5] = 2;// UB x C2 imposto

		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3

		// m[8][9] = 8;// UB x C3 imposto
		// m[9][8] = -3;

		m[10][11] = 7; // nodo T5
		m[11][10] = -5; // nodo T5

		m[12][13] = 2;// UB x C4 imposto

		// m[11][8] = -7;// INTERTASK tra S_C3 e E_T5
		// m[8][11] = 14;
	}

	/**
	 * Controlla consistenza wf-path T2T3T4. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix(int[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(int[][])
	 */
	static void controllablePathT2T3T4(int[][] m) {
		initMatrix(m);
		m[0][1] = 3;// UB x C1 imposto

		m[2][3] = 6; // UPPER nodo T2
		m[3][2] = -4; // LOWER nodo T2

		m[4][5] = 2;// UB x C2 imposto
		// m[5][4] = -3;

		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3

		// m[8][9] = 5;// UB x C3 imposto

		m[10][11] = 8; // nodo T4
		m[11][10] = -3; // nodo T4

		m[12][13] = 2;// UB x C4 imposto

		m[11][2] = -17;// INTERTASK tra S_C3 e E_T4
		m[2][11] = 27;

	}

	/**
	 * Controlla consistenza wf-path T1|T2-T3-T4|T5.<br>
	 * I connettori e gli edge sono inizializzati in {@link #initMatrix(int[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(int[][])
	 */
	static void controllableParallelPathT1T2T3T4T5(int[][] m) {
		initMatrixParallelo(m);

		// m[0][1] = 3;// UB x C1 imposto

		m[1][2] = 2;// C1-T1
		m[2][1] = -1;

		m[2][3] = 6; // UPPER nodo T1
		m[3][2] = -2; // LOWER nodo T1

		m[4][5] = 6; // UPPER nodo T2
		m[5][4] = -4; // LOWER nodo T2

		m[6][8] = m[7][8] = Integer.MAX_VALUE;// Delay interno al C2.

		m[8][9] = 2;// C2
		m[9][8] = -1;

		m[10][11] = 7; // UPPER nodo T3
		m[11][10] = -2; // LOWER nodo T3

		// m[8][9] = 5;// UB x C3 imposto

		m[14][15] = 8; // nodo T4
		m[15][14] = -3; // nodo T4

		m[16][17] = 7; // nodo T5
		m[17][16] = -5; // nodo T5

		m[18][20] = m[19][20] = Integer.MAX_VALUE;// Delay interno al C4.

		m[20][21] = 2;// UB x C4 imposto

		m[2][15] = 27;// INTERTASK tra S_T1 e E_T4
		m[15][2] = -22;

		m[4][15] = 27;// INTERTASK tra S_T2 e E_T4
		m[15][4] = -17;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 22;
		int[][] m = new int[n][n], m1 = new int[n][n];

		int[] taskList = new int[] { 2, 4, 10, 14, 16 };

		controllableParallelPathT1T2T3T4T5(m);

		DistanceGraph.deepClone(m, m1);

		System.out.println("Matrice delle distanze prima Floyd-W:\n" + DistanceGraph.print(m));

		DistanceGraph.FloydWarshall(m);
		System.out.println("Matrice delle distanze dopo Floyd-W:\n" + DistanceGraph.print(m));

		if (DistanceGraph.areNegativeCycles(m)) {
			// System.out.println(DistanceGraph.print(m));
			return;
		}
		if (shrikingCheck(m1, m, taskList))
			return;

		System.out.println("Intervalli prima calcolo controllabilità:");
		// printIntervalli(m, false);
		printIntervalliParallelo(m, true);

		if (checkDCPathParallelo(m)) {
			System.out.println("Intervalli DOPO calcolo controllabilità:");
			printIntervalliParallelo(m, true);
		}
	}
}
