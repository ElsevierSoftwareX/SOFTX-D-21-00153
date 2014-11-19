package it.univr.di.wf;

import java.util.logging.Logger;

/**
 * In questa classe ci sono i metodi che calcolano la controllabilità di un path in cui possono
 * essere presenti gli edge interni agli AND-Join.
 * 
 * @author posenato
 */
public class ExtendedControllabilityNonFunzionante {

	/**
	 * Logger della classe.
	 */
	static Logger LOG = Logger.getLogger(ExtendedControllabilityNonFunzionante.class.getName());

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
	static void printIntervalliParallelo(Edge[][] m, boolean withEdge) {
		System.out.printf("Durata C1:\t[%3d, %3d]\n", -m[1][0].valore, m[0][1]);
		if (withEdge) {
			System.out.printf("Durata C1-T1:\t[%3d, %3d]\n", -m[2][1].valore, m[1][2]);
			System.out.printf("Durata C1-T2:\t[%3d, %3d]\n", -m[4][1].valore, m[1][4]);
		}
		System.out.printf("Durata T1:\t[%3d, %3d]\n", -m[3][2].valore, m[2][3]);
		System.out.printf("Durata T1-C2INT:[%3d, %3d]\n", -m[8][6].valore, m[6][8]);
		if (withEdge) {
			System.out.printf("Durata T1-C2:\t[%3d, %3d]\n", -m[6][3].valore, m[3][6]);
		}
		System.out.printf("Durata T2:\t[%3d, %3d]\n", -m[5][4].valore, m[4][5]);
		System.out.printf("Durata T2-C2INT:[%3d, %3d]\n", -m[8][7].valore, m[7][8]);
		if (withEdge) {
			System.out.printf("Durata T2-C2:\t[%3d, %3d]\n", -m[7][5].valore, m[5][7]);
		}

		System.out.printf("Durata C2:\t[%3d, %3d]\n", -m[9][8].valore, m[8][9]);
		if (withEdge) {
			System.out.printf("Durata C2-T3:\t[%3d, %3d]\n", -m[10][9].valore, m[9][10]);
		}

		System.out.printf("Durata T3:\t[%3d, %3d]\n", -m[11][10].valore, m[10][11]);
		if (withEdge) {
			System.out.printf("Durata T3-C3:\t[%3d, %3d]\n", -m[12][11].valore, m[11][12]);
		}
		System.out.printf("Durata C3:\t[%3d, %3d]\n", -m[13][12].valore, m[12][13]);
		if (withEdge) {
			System.out.printf("Durata C3-T4:\t[%3d, %3d]\n", -m[14][13].valore, m[13][14]);
			System.out.printf("Durata C3-T5:\t[%3d, %3d]\n", -m[16][13].valore, m[13][16]);
		}
		System.out.printf("Durata T4:\t[%3d, %3d]\n", -m[15][14].valore, m[14][15]);
		System.out.printf("Durata T5:\t[%3d, %3d]\n", -m[17][16].valore, m[16][17]);
		if (withEdge) {
			System.out.printf("Durata T4-C4:\t[%3d, %3d]\n", -m[18][15].valore, m[15][18]);
			System.out.printf("Durata T5-C4:\t[%3d, %3d]\n", -m[19][17].valore, m[17][19]);
		}
		System.out.printf("Durata C4:\t[%3d, %3d]\n", -m[21][20].valore, m[20][21]);

		System.out.printf("Durata T1-T4:\t[%3d, %3d]\n", -m[15][2].valore, m[2][15]);
		System.out.printf("Durata T2-T4:\t[%3d, %3d]\n", -m[15][4].valore, m[4][15]);
		System.out.printf("Durata C1-C4:\t[%3d, %3d]\n", -m[21][0].valore, m[0][21]);

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
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		final int n = 22;
		Edge[][] m = new Edge[n][n], m1 = new Edge[n][n];

		int[] taskList = new int[] { 2, 4, 10, 14, 16 };

		// controllableParallelPathT1T2T3T4T5(m);

		// checkDCPath(m, true);

		System.out.println("Intervalli prima calcolo controllabilità:");
		// printIntervalli(m, false);
		printIntervalliParallelo(m, true);

		// if (checkDCPath(m, true)) {
		System.out.println("Intervalli DOPO calcolo controllabilità:");
		printIntervalliParallelo(m, true);
		// }
	}
}
