package it.univr.di.wf;


import java.util.logging.Logger;



/**
 * @author posenato
 */
public class Consistency {

	/**
	 * Valore di default per l'upper bound alle durate e ai ritardi.
	 */
	final static float defaultUB = Float.POSITIVE_INFINITY;

	/**
	 * Valore di default per il lower bound alle durate e ai ritardi.
	 */
	final static float defaultLB = 1;

	/**
	 * Valore di default per il lower bound alle durate e ai ritardi.
	 */
	final static float notAllowed = Float.NEGATIVE_INFINITY;

	/**
	 * Costante carattere che rappresenta il simbolo di infinito: ∞.
	 */
	final static char INFINITY = '∞';

	/**
	 * Logger della classe
	 */
	static Logger LOG = Logger.getLogger(Consistency.class.getName());

	/**
	 * Determina la chiusura transitiva della matrice G.
	 * 
	 * @param adjacency matrice delle distanze
	 * @return chiusura transitiva
	 */
	static float[][] Floyd(float[][] adjacency) {

		float[][] distance = adjacency.clone();

		for (int k = 0; k < distance.length; k++) {
			for (int i = 0; i < distance.length; i++) {
				// if (M[i][k] == notAllowed) continue;
				for (int j = 0; j < distance.length; j++) {
					// if (M[i][j] == notAllowed || M[k][j] == notAllowed)
					// continue;
					distance[i][j] = Math.min(distance[i][j], distance[i][k] + distance[k][j]);
				}
			}
		}

		return distance;
	}

	/**
	 * Determina la chiusura transitiva della matrice G calcolando i valori a partire dalla riga di
	 * indice maggiore.
	 * 
	 * @param adjacency matrice delle distanze
	 * @return chiusura transitiva
	 */
	static float[][] FloydI(float[][] adjacency) {

		float[][] distance = adjacency.clone();

		final int n = distance.length - 1;

		for (int k = n; k >= 0; k--) {
			for (int i = n; i >= 0; i--) {
				// if (M[i][k] == notAllowed) continue;
				for (int j = n; j >= 0; j--) {
					// if (M[i][j] == notAllowed || M[k][j] == notAllowed)
					// continue;
					distance[i][j] = Math.min(distance[i][j], distance[i][k] + distance[k][j]);
				}
			}
		}
		return distance;
	}

	/**
	 * Stampa la matrice G in System.out.
	 * 
	 * @param distance
	 */
	static void print(float[][] distance) {
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				if (distance[i][j] == defaultUB)
					System.out.printf("%5s", INFINITY);
				else {
					if (distance[i][j] == notAllowed)
						System.out.printf("%5s", "n.a.");
					else
						System.out.printf("%5.0f", distance[i][j]);
				}
			}
			System.out.println();
		}
	}

	/**
	 * Stampa intervalli per i task e i nuovi valori per i vincoli intertask nel caso in cui il
	 * grafo è la figura 13 dell'articolo per TAAS.
	 * 
	 * @param G
	 */
	static void printIntervalli(float[][] G) {
		System.out.printf("Durata T1:\t\t\t[%.0f, %.0f]\n", -G[2][1], G[1][2]);
		System.out.printf("Durata T1-C1:\t\t[%.0f, %.0f]\n", -G[3][2], G[2][3]);
		System.out.printf("Durata C1:\t\t\t[%.0f, %.0f]\n", -G[4][3], G[3][4]);
		System.out.printf("Durata C1-T2|T3:\t[%.0f, %.0f]\n", -G[5][4], G[4][5]);
		System.out.printf("Durata T2|T3:\t\t[%.0f, %.0f]\n", -G[6][5], G[5][6]);
		System.out.printf("Durata T2|T3-C2:\t[%.0f, %.0f]\n", -G[7][6], G[6][7]);
		System.out.printf("Durata C2:\t\t\t[%.0f, %.0f]\n", -G[8][7], G[7][8]);
		System.out.printf("Durata C2-T4:\t\t[%.0f, %.0f]\n", -G[9][8], G[8][9]);
		System.out.printf("Durata T4:\t\t\t[%.0f, %.0f]\n", -G[10][9], G[9][10]);
		System.out.printf("Durata T4-C3:\t\t[%.0f, %.0f]\n", -G[11][10], G[10][11]);
		System.out.printf("Durata C3:\t\t\t[%.0f, %.0f]\n", -G[12][11], G[11][12]);
		System.out.printf("Durata C3-T5|T6:\t[%.0f, %.0f]\n", -G[13][12], G[12][13]);
		System.out.printf("Durata T5|T6:\t\t[%.0f, %.0f]\n", -G[14][13], G[13][14]);
		System.out.printf("Durata T5|T6-C4:\t[%.0f, %.0f]\n", -G[15][14], G[14][15]);
		System.out.printf("Durata C4:\t\t\t[%.0f, %.0f]\n", -G[16][15], G[15][16]);
		System.out.printf("Durata C4-T7:\t\t[%.0f, %.0f]\n", -G[17][16], G[16][17]);
		System.out.printf("Durata T7:\t\t\t[%.0f, %.0f]\n", -G[18][17], G[17][18]);

		System.out.printf("Durata intertask T1-T7:\t\t\t\t\t\t[%.0f, %.0f]\n", -G[18][1], G[1][18]);
		System.out.printf("Durata intertask T2-T5 (o T3-T6 o T2-T6):\t[%.0f, %.0f]\n", -G[14][5],
				G[5][14]);

	}

	/**
	 * Stampa intervalli per i task e i nuovi valori per i vincoli intertask nel caso in cui il
	 * grafo è quello per TIME2010
	 * 
	 * @param G
	 */
	static void printIntervalli2(float[][] G) {
		System.out.printf("Durata C1:\t[%.0f, %.0f]\n", -G[1][0], G[0][1]);
		System.out.printf("Durata T1|T2:\t[%.0f, %.0f]\n", -G[3][2], G[2][3]);
		System.out.printf("Durata C2:\t[%.0f, %.0f]\n", -G[5][4], G[4][5]);
		System.out.printf("Durata T3:\t[%.0f, %.0f]\n", -G[7][6], G[6][7]);
		System.out.printf("Durata C3:\t[%.0f, %.0f]\n", -G[9][8], G[8][9]);
		System.out.printf("Durata T4|T5:\t[%.0f, %.0f]\n", -G[11][10], G[10][11]);
		System.out.printf("Durata C4:\t[%.0f, %.0f]\n", -G[13][12], G[12][13]);

		System.out.printf("Durata intertask C1-C4:\t\t\t\t[%.0f, %.0f]\n", -G[13][0], G[0][13]);

		System.out.printf("Durata intertask T1-T4 (o T1-T5 o T2-T5):\t[%.0f, %.0f]\n", -G[11][2],
				G[2][11]);
	}

	/**
	 * Stampa intervalli per i delay,task e connettori del workflow completo di Figura 13 del TAAS.
	 * 
	 * @param G the graph
	 */
	static void printIntervalliFullWF(float[][] G) {

		System.out.printf("Durata T1:\t\t[%.0f, %.0f]\n", -G[2][1], G[1][2]);
		System.out.printf("Durata T1-C1:\t[%.0f, %.0f]\n", -G[3][2], G[2][3]);
		System.out.printf("Durata C1:\t\t[%.0f, %.0f]\n", -G[4][3], G[3][4]);
		System.out.printf("Durata C1-T2:\t[%.0f, %.0f]\n", -G[5][4], G[4][5]);
		System.out.printf("Durata C1-T3:\t[%.0f, %.0f]\n", -G[7][4], G[4][7]);
		System.out.printf("Durata T2:\t\t[%.0f, %.0f]\n", -G[6][5], G[5][6]);
		System.out.printf("Durata T3:\t\t[%.0f, %.0f]\n", -G[8][7], G[7][8]);
		System.out.printf("Durata T2-C2:\t[%.0f, %.0f]\n", -G[9][6], G[6][9]);
		System.out.printf("Durata T3-C2:\t[%.0f, %.0f]\n", -G[9][8], G[8][9]);
		System.out.printf("Durata C2:\t\t[%.0f, %.0f]\n", -G[10][9], G[9][10]);
		System.out.printf("Durata C2-T4:\t[%.0f, %.0f]\n", -G[11][10], G[10][11]);
		System.out.printf("Durata T4:\t\t[%.0f, %.0f]\n", -G[12][11], G[11][12]);
		System.out.printf("Durata T4-C3:\t[%.0f, %.0f]\n", -G[13][12], G[12][13]);
		System.out.printf("Durata C3:\t\t[%.0f, %.0f]\n", -G[14][13], G[13][14]);
		System.out.printf("Durata C3-T5:\t[%.0f, %.0f]\n", -G[15][14], G[14][15]);
		System.out.printf("Durata C3-T6:\t[%.0f, %.0f]\n", -G[17][14], G[14][17]);
		System.out.printf("Durata T5:\t\t[%.0f, %.0f]\n", -G[16][15], G[15][16]);
		System.out.printf("Durata T6:\t\t[%.0f, %.0f]\n", -G[18][17], G[17][18]);
		System.out.printf("Durata T5-C4:\t[%.0f, %.0f]\n", -G[19][16], G[16][19]);
		System.out.printf("Durata T6-C4:\t[%.0f, %.0f]\n", -G[19][18], G[18][19]);
		System.out.printf("Durata C4:\t\t[%.0f, %.0f]\n", -G[20][19], G[19][20]);
		System.out.printf("Durata C4-T7:\t[%.0f, %.0f]\n", -G[21][20], G[20][21]);
		System.out.printf("Durata T7:\t\t[%.0f, %.0f]\n", -G[22][21], G[21][22]);

		System.out.printf("Durata intertask T1-T7: [%.0f, %.0f]\n", -G[22][1], G[1][22]);
		System.out.printf("Durata intertask T2-T5: [%.0f, %.0f]\n", -G[16][5], G[5][16]);
		System.out.printf("Durata intertask T2-T6: [%.0f, %.0f]\n", -G[18][5], G[5][18]);
		System.out.printf("Durata intertask T3-T6: [%.0f, %.0f]\n", -G[18][7], G[7][18]);
	}

	/**
	 * @return matrice delle distanze di un grafo apparentemente inconsistente.
	 */
	static float[][] grafoApparentementeInconsistente() {
		float[][] m = new float[7][7];
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = defaultLB;
		}

		m[0][1] = 10;
		m[1][0] = -2;
		m[0][6] = 15;
		m[6][0] = -6;
		m[1][2] = 7;
		m[2][1] = -6;
		m[1][4] = 1;
		m[4][1] = -1;
		m[3][6] = 3;
		m[6][3] = -3;
		m[6][5] = m[5][6] = 0;
		m[2][3] = 3;
		m[3][2] = -1;
		m[4][5] = 2;
		m[5][4] = -2;

		return m;
	}

	/**
	 * Inizializza la matrice delle distanza con +defaultUB e aggiusta i tempi per i link e i
	 * connettori come da figura 13 dell'articolo TAAS.
	 * 
	 * @param m matrice delle distanze
	 */
	static void initMatrix(float[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = defaultUB;
				m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}
		// inizializzo con [1,+∞] i connettori esterni
		m[1][0] = m[3][2] = m[4][3] = m[5][4] = m[7][6] = m[8][7] = m[9][8] = m[11][10] = m[12][11] = m[13][12] = m[15][14] = m[16][15] = m[17][16] = m[19][18] = -defaultLB;
		// Inizializzo con tempo [1,1] i connettori e gli edge della zona
		// centrale del wf.
		m[6][7] = m[7][8] = m[8][9] = m[10][11] = m[11][12] = m[12][13] = 1;

		m[1][18] = 70;
		m[18][1] = -45;// vincolo INTERTASK tra T1 e T7
	}

	/**
	 * Inizializza la matrice delle distanza con +defaultUB e aggiusta i tempi per i link e i
	 * connettori per l'esempio articolo TIME2010
	 * 
	 * @param m matrice delle distanze
	 */
	static void initMatrix2(float[][] m) {
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = defaultUB;
				m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}
		// inizializzo il L.B. di tutti gli edge e i connettori a -defaultLB
		m[1][0] = m[2][1] = m[4][3] = m[5][4] = m[6][5] = m[8][7] = m[9][8] = m[10][9] = m[12][11] = m[13][12] = m[13][12] = -defaultLB;
		// inizializzo l'U.B. di tutti gli edge a 1
		m[1][2] = m[3][4] = m[5][6] = m[7][8] = m[9][10] = m[11][12] = 1;

		m[0][13] = 45;
		m[13][0] = -40;// vincolo INTERTASK tra S_C1 e E_C4

	}

	/**
	 * Inizializza la matrice delle distanze per il workflow completo di figura 13 dell'articolo
	 * TAAS.
	 * 
	 * @param m matrice delle distanze
	 */
	static void initFullWF(float[][] m) {

		// Inizializzo tutto a +infty e la diagonale a 0.
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}

		// inizializzo i singoli elementi: delay, TASK e i CONNETTORI
		m[1][0] = -defaultLB; // Lower per Start-T1
		m[1][2] = 4; // T1
		m[2][1] = -2;
		m[3][2] = -defaultLB; // T1-C1
		m[4][3] = -defaultLB; // C1
		m[5][4] = -defaultLB;// C1-T2
		m[7][4] = -defaultLB;// C1-T3
		m[5][6] = 4; // T2
		m[6][5] = -2;
		m[7][8] = 4; // T3
		m[8][7] = -1;
		m[9][6] = -defaultLB; // T2-C2
		m[6][9] = 1;
		m[9][8] = -defaultLB;// T3-C2
		m[8][9] = 1;
		m[9][10] = 1; // C2
		m[10][9] = -defaultLB;
		m[10][11] = 1;// C2-T4
		m[11][10] = -defaultLB;
		m[11][12] = 9; // T4
		m[12][11] = -2;
		m[12][13] = 1;// T4-C3
		m[13][12] = -defaultLB;
		m[13][14] = 1;// C3
		m[14][13] = -defaultLB;
		m[14][15] = 1; // C3-T5
		m[15][14] = -defaultLB;
		m[14][17] = 1; // C3-T6
		m[17][14] = -defaultLB;
		m[15][16] = 35; // T5
		m[16][15] = -25;
		m[17][18] = 45; // T6
		m[18][17] = -35;
		// m[15][17] = m[17][15] = notAllowed;
		// m[16][18] = m[18][16] = notAllowed;
		// m[15][18] = m[18][15] = notAllowed;
		// m[16][17] = m[17][16] = notAllowed;
		m[19][16] = -defaultLB; // T5-C4
		// m[16][19] = 6;
		m[19][18] = -defaultLB;// T6-C4
		// m[18][19] = 1;
		m[20][19] = -defaultLB;// C4
		// m[19][20] = 1;// C4
		m[21][20] = -defaultLB;// C4-T7
		m[21][22] = 7;// T7
		m[22][21] = -4;
		m[23][22] = -defaultLB;

		// Vicoli intertask
		m[1][22] = 70;// Start-T7
		m[22][1] = -45;

		m[5][16] = 40;// T2-T5
		m[16][5] = -30;

		m[5][18] = 60;// T2-T6
		m[18][5] = -40;

		m[7][18] = 60;// T3-T6
		m[18][7] = -42;
	}

	/**
	 * Inizializza la matrice delle distanze per il prefisso T1T2 di figura 13 dell'articolo TAAS.
	 * 
	 * @param m matrice delle distanze
	 */
	static void initFullWFT1T2(float[][] m) {

		// Inizializzo tutto a +infty e la diagonale a 0.
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}

		// inizializzo i singoli elementi: delay, TASK e i CONNETTORI
		m[1][0] = -defaultLB; // Lower per Start-T1
		m[1][2] = 4; // T1
		m[2][1] = -2;
		m[3][2] = -defaultLB; // T1-C1
		m[4][3] = -defaultLB; // C1
		m[5][4] = -defaultLB;// C1-T2
		m[7][4] = -defaultLB;// C1-T3
		m[5][6] = 4; // T2
		m[6][5] = -2;
		// m[7][8] = 4; // T3
		// m[8][7] = -1;
		m[9][6] = -defaultLB; // T2-C2
		m[6][9] = 1;
		m[9][8] = -defaultLB;// T3-C2
		m[8][9] = 1;
		m[9][10] = 1; // C2
		m[10][9] = -defaultLB;
		m[10][11] = 1;// C2-T4
		m[11][10] = -defaultLB;
		m[11][12] = 9; // T4
		m[12][11] = -2;
		m[12][13] = 1;// T4-C3
		m[13][12] = -defaultLB;
		m[13][14] = 1;// C3
		m[14][13] = -defaultLB;
		m[14][15] = 1; // C3-T5
		m[15][14] = -defaultLB;
		m[14][17] = 1; // C3-T6
		m[17][14] = -defaultLB;
		m[15][16] = 35; // T5
		m[16][15] = -25;
		m[17][18] = 45; // T6
		m[18][17] = -35;
		// m[15][17] = m[17][15] = notAllowed;
		// m[16][18] = m[18][16] = notAllowed;
		// m[15][18] = m[18][15] = notAllowed;
		// m[16][17] = m[17][16] = notAllowed;
		m[19][16] = -defaultLB; // T5-C4
		// m[16][19] = 7;
		m[19][18] = -defaultLB;// T6-C4
		// m[18][19] = 1;
		m[20][19] = -defaultLB;// C4
		m[21][20] = -defaultLB;// C4-T7
		m[21][22] = 7;// T7
		m[22][21] = -4;
		m[23][22] = -defaultLB;

		// Vicoli intertask
		m[1][22] = 70;// Start-T7
		m[22][1] = -45;

		m[5][16] = 35;// T2-T5
		m[16][5] = -30;

		m[5][18] = 60;// T2-T6
		m[18][5] = -40;

		// m[7][18] = 60;// T3-T6
		// m[18][7] = -58;

	}

	/**
	 * Inizializza la matrice delle distanze per il prefisso T1T3 di figura 13 dell'articolo TAAS.
	 * 
	 * @param m matrice delle distanze
	 */
	static void initFullWFT1T3(float[][] m) {

		// Inizializzo tutto a +infty e la diagonale a 0.
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}

		// inizializzo i singoli elementi: delay, TASK e i CONNETTORI
		m[1][0] = -defaultLB; // Lower per Start-T1
		m[1][2] = 6; // T1
		m[2][1] = -3;
		m[3][2] = -defaultLB; // T1-C1
		m[4][3] = -defaultLB; // C1
		// m[5][4] = -defaultLB;// C1-T2
		m[7][4] = -defaultLB;// C1-T3
		// m[5][6] = 4; // T2
		// m[6][5] = -2;
		m[7][8] = 4; // T3
		m[8][7] = -1;
		// m[9][6] = -defaultLB; // T2-C2
		// m[6][9] = 1;
		m[9][8] = -defaultLB;// T3-C2
		m[8][9] = 1;
		m[9][10] = 1; // C2
		m[10][9] = -defaultLB;
		m[10][11] = 1;// C2-T4
		m[11][10] = -defaultLB;
		m[11][12] = 9; // T4
		m[12][11] = -2;
		m[12][13] = 1;// T4-C3
		m[13][12] = -defaultLB;
		m[13][14] = 1;// C3
		m[14][13] = -defaultLB;
		m[14][15] = 1; // C3-T5
		m[15][14] = -defaultLB;
		m[14][17] = 1; // C3-T6
		m[17][14] = -defaultLB;
		m[15][16] = 5; // T5
		m[16][15] = -3;
		m[17][18] = 9; // T6
		m[18][17] = -7;
		m[15][17] = m[17][15] = notAllowed;
		m[16][18] = m[18][16] = notAllowed;
		m[15][18] = m[18][15] = notAllowed;
		m[16][17] = m[17][16] = notAllowed;
		m[19][16] = -defaultLB; // T5-C4
		m[16][19] = 6;
		m[19][18] = -defaultLB;// T6-C4
		m[18][19] = 1;
		m[20][19] = -defaultLB;// C4
		m[21][20] = -defaultLB;// C4-T7
		m[21][22] = 7;// T7
		m[22][21] = -4;
		m[23][22] = -defaultLB;

		// Vicoli intertask
		m[1][22] = 35;// Start-T7
		m[22][1] = -14;

		// m[5][16] = 15;// T2-T5
		// m[16][5] = -9;

		// m[5][18] = 25;// T2-T6
		// m[18][5] = -10;

		m[7][18] = 16;// T3-T6
		m[18][7] = -11;

	}

	/**
	 * Inizializza la matrice delle distanze per il workflow completo di figura 13 dell'articolo
	 * TAAS con i vincoli inter-task che portano alla controllabilità time-dependent e con i primi
	 * due TASK al loro massimo mentre il connettore C1 e i ritardi fra questi al minimo.
	 * 
	 * @param m matrice delle distanze
	 */
	static void esecuzionFullWFT1T2ForTimeDependent(float[][] m) {

		// Inizializzo tutto a +infty e la diagonale a 0.
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}

		m[1][0] = -defaultLB; // Lower per Start-T1
		m[1][2] = 3; // T1
		m[2][1] = -3;
		m[3][2] = -defaultLB; // T1-C1
		m[2][3] = 1;
		m[4][3] = -defaultLB; // C1
		m[3][4] = 1;
		m[5][4] = -defaultLB;// C1-T2
		m[4][5] = 1;
		// m[7][4] = -defaultLB;// C1-T3
		m[5][6] = 4; // T2
		m[6][5] = -4;
		// m[7][8] = 4; // T3
		// m[8][7] = -1;
		m[9][6] = -defaultLB; // T2-C2
		m[6][9] = 1;
		// m[9][8] = -defaultLB;// T3-C2
		// m[8][9] = 1;
		m[9][10] = 1; // C2
		m[10][9] = -defaultLB;
		m[10][11] = 1;// C2-T4
		m[11][10] = -defaultLB;
		m[11][12] = 9; // T4
		m[12][11] = -2;
		m[12][13] = 1;// T4-C3
		m[13][12] = -defaultLB;
		m[13][14] = 1;// C3
		m[14][13] = -defaultLB;
		m[14][15] = 1; // C3-T5
		m[15][14] = -defaultLB;
		m[14][17] = 1; // C3-T6
		m[17][14] = -defaultLB;
		m[15][16] = 5; // T5
		m[16][15] = -3;
		m[17][18] = 9; // T6
		m[18][17] = -7;
		// m[15][17] = m[17][15] = notAllowed;
		// m[16][18] = m[18][16] = notAllowed;
		// m[15][18] = m[18][15] = notAllowed;
		// m[16][17] = m[17][16] = notAllowed;
		m[19][16] = -defaultLB; // T5-C4
		m[16][19] = 7;
		m[19][18] = -defaultLB;// T6-C4
		m[18][19] = 1;
		m[20][19] = -defaultLB;// C4
		// m[19][20] =1;
		m[21][20] = -defaultLB;// C4-T7
		// m[20][21] = 1;
		m[21][22] = 7;// T7
		m[22][21] = -4;
		m[23][22] = -defaultLB;

		// Vicoli intertask
		m[1][22] = 35;// Start-T7
		m[22][1] = -14;

		m[5][16] = 16;// T2-T5
		m[16][5] = -13;

		m[5][18] = 25;// T2-T6
		m[18][5] = -22;

		// m[7][18] = 16;// T3-T6
		// m[18][7] = -11;
	}

	/**
	 * Inizializza la matrice delle distanze per il workflow completo di figura 13 dell'articolo
	 * TAAS con i vincoli inter-task che portano alla controllabilità strong-dependent e con i primi
	 * due TASK al loro massimo mentre il connettore C1 e i ritardi fra questi al minimo.
	 * 
	 * @param m matrice delle distanze
	 */
	static void esecuzionFullWFT1T2(float[][] m) {

		// Inizializzo tutto a +infty e la diagonale a 0.
		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++) {
				m[i][j] = m[j][i] = defaultUB;
			}
			m[i][i] = 0;
		}

		m[1][0] = -defaultLB; // Lower per Start-T1
		m[1][2] = 6; // T1
		m[2][1] = -6;
		m[3][2] = -defaultLB; // T1-C1
		m[2][3] = 1;
		m[4][3] = -defaultLB; // C1
		m[3][4] = 1;
		m[5][4] = -defaultLB;// C1-T2
		m[4][5] = 1;
		// m[7][4] = -defaultLB;// C1-T3
		m[5][6] = 4; // T2
		m[6][5] = -4;
		// m[7][8] = 4; // T3
		// m[8][7] = -1;
		m[9][6] = -defaultLB; // T2-C2
		m[6][9] = 1;
		// m[9][8] = -defaultLB;// T3-C2
		// m[8][9] = 1;
		m[9][10] = 1; // C2
		m[10][9] = -defaultLB;
		m[10][11] = 1;// C2-T4
		m[11][10] = -defaultLB;
		m[11][12] = 9; // T4
		m[12][11] = -2;
		m[12][13] = 1;// T4-C3
		m[13][12] = -defaultLB;
		m[13][14] = 1;// C3
		m[14][13] = -defaultLB;
		m[14][15] = 1; // C3-T5
		m[15][14] = -defaultLB;
		m[14][17] = 1; // C3-T6
		m[17][14] = -defaultLB;
		m[15][16] = 5; // T5
		m[16][15] = -3;
		m[17][18] = 9; // T6
		m[18][17] = -7;
		m[15][17] = m[17][15] = notAllowed;
		m[16][18] = m[18][16] = notAllowed;
		m[15][18] = m[18][15] = notAllowed;
		m[16][17] = m[17][16] = notAllowed;
		m[19][16] = -defaultLB; // T5-C4
		m[16][19] = 6;
		m[19][18] = -defaultLB;// T6-C4
		m[18][19] = 1;
		m[20][19] = -defaultLB;// C4
		m[21][20] = -defaultLB;// C4-T7
		m[21][22] = 7;// T7
		m[22][21] = -4;
		m[23][22] = -defaultLB;

		// Vicoli intertask
		m[1][22] = 35;// Start-T7
		m[22][1] = -14;

		m[5][16] = 15;// T2-T5
		m[16][5] = -9;

		m[5][18] = 25;// T2-T6
		m[18][5] = -10;

		// m[7][18] = 16;// T3-T6
		// m[18][7] = -11;
	}

	/**
	 * Verifica se la matrice delle distanze contiene cicli negativi. In caso positivo, stampa un
	 * messaggio e restituisce vero.
	 * 
	 * @param m matrice delle distanze
	 * @return vero se la matrice contiene cicli negativi, falso altrimenti.
	 */
	static boolean checkCicliNegativi(float[][] m) {
		for (int i = 0; i < m.length; i++) {
			if (m[i][i] < 0) {
				System.out.print("La matrice contiene cicli negativi!\n");
				return true;
			}
		}
		return false;
	}

	/**
	 * Inizializza il wf-path T1T2T4T5T7 di fig. 13 dell'articolo. I connettori e gli edge sono
	 * inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathT1T2T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 4; // UPPER nodo T1 6
		m[2][1] = -2; // LOWER nodo T1 -3
		m[5][6] = 4; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 9; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 35; // nodo T5
		m[14][13] = -25; // nodo T5
		// m[14][15] = 6;// T5-C4
		// m[15][14] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 35;
		m[14][5] = -30;// INTERTASK tra T2 e T5
	}

	/**
	 * Controlla consistenza wf-path T1T3T4. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix2(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix2(float[][])
	 */
	static void controllablePathT1T3T4(float[][] m) {
		initMatrix2(m);
		m[0][1] = 5;// UB x C1 imposto
		m[1][2] = 2;// Alterazione del edge C1-T1
		m[2][1] = -2;
		m[2][3] = 4; // UPPER nodo T1
		m[3][2] = -2; // LOWER nodo T1
		m[4][5] = 3;// UB imposto x C2

		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3
		m[8][9] = 8;// UB imposto x C3
		m[10][11] = 5; // nodo T4
		m[11][10] = -3; // nodo T4
		m[12][13] = 9;// UB x C4 imposto

		m[11][2] = -13;// INTERTASK tra S_T1 e E_T4
		m[2][11] = 22;
	}

	/**
	 * Controlla consistenza wf-path T1T3T5. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix2(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix2(float[][])
	 */
	static void controllablePathT1T3T5(float[][] m) {
		initMatrix2(m);
		m[0][1] = 5;// UB imposto per C1
		m[1][2] = 2;// Alterazione del edge C1-T1
		m[2][1] = -2;
		m[2][3] = 4; // UPPER nodo T1
		m[3][2] = -2; // LOWER nodo T1
		m[5][4] = -3;m[4][5] = 3;// UB imposto x C2
		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3
		m[8][9] = 6;// UB imposto x C3
		m[10][11] = 9; // nodo T5
		m[11][10] = -7; // nodo T5
		m[12][13] = 9;// UB x C4 imposto

		m[11][2] = -22;// INTERTASK tra S_T1 e E_T4
		m[2][11] = 27;
	}

	/**
	 * Controlla consistenza wf-path T2T3T5. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix2(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix2(float[][])
	 */
	static void controllablePathT2T3T5(float[][] m) {
		initMatrix2(m);
		m[0][1] = 1;// UB x C1 imposto
		m[2][3] = 4; // UPPER nodo T2
		m[3][2] = -1; // LOWER nodo T2
		m[5][4]=-3; m[4][5] = 6;// UB x C2 imposto
		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3
		m[8][9] =6;// UB x C3 imposto
		m[10][11] = 9; // nodo T5
		m[11][10] = -7; // nodo T5
		//m[12][13] = 9;// UB x C4 imposto

		m[11][2] = -26;// INTERTASK tra S_T2 e E_T5
		m[2][11] = 28;
	}

	/**
	 * Controlla consistenza wf-path T2T3T4. I connettori e gli edge sono inizializzati in
	 * {@link #initMatrix2(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix2(float[][])
	 */
	static void controllablePathT2T3T4(float[][] m) {
		initMatrix2(m);
		//m[0][1] = 5;// UB x C1 imposto
		m[2][3] = 4; // UPPER nodo T2
		m[3][2] = -1; // LOWER nodo T2
		m[5][4]=-3; m[4][5] = 6;// UB x C2 imposto
		m[6][7] = 7; // UPPER nodo T3
		m[7][6] = -2; // LOWER nodo T3
		m[8][9] = 6;// UB x C3 imposto
		m[10][11] = 5; // nodo T4
		m[11][10] = -3; // nodo T4
		m[12][13] = 9;// UB x C4 imposto

		// m[11][2] = -13;// INTERTASK tra S_T1 e E_T4
		// m[2][11] = 22;
	}

	/**
	 * Inizializza il wf-path T1T2T4T5T7 di fig. 13 dell'articolo assumendo che i task e i
	 * connettori siano riinizializzati con i range dell'intersezione ottenuta dalla prima analisi.
	 * I connettori e gli edge sono inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathIntersezioneT1T2T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1 6
		m[2][1] = -2; // LOWER nodo T1 -3
		m[3][2] = -1;// T1-C1
		m[2][3] = 1;
		m[4][3] = -1;// C1
		m[3][4] = 1;
		m[5][4] = -1;// C1-T2
		m[4][5] = 14;
		m[5][6] = 2; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 2; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 25; // nodo T5
		m[14][13] = -25; // nodo T5
		m[15][14] = -1;// T5-C4
		m[14][15] = 24;
		m[16][15] = -1;// C4
		m[15][16] = 24;
		m[17][16] = -1;// C4-T7
		m[16][17] = 24;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 35;
		m[14][5] = -30;// INTERTASK tra T2 e T5
	}

	/**
	 * Inizializza il wf-path T1T2T4T6T7 di fig. 13 dell'articolo assumendo che i task e i
	 * connettori siano riinizializzati con i range dell'intersezione ottenuta dalla prima analisi.
	 * I connettori e gli edge sono inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathIntersezioneT1T2T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[3][2] = -1;// T1-C1
		m[2][3] = 1;
		m[4][3] = -1;// C1
		m[3][4] = 1;
		m[5][4] = -1;// C1-T2
		m[4][5] = 14;
		m[5][6] = 2; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 2; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -35; // nodo T6
		m[15][14] = -1;// T6-C4
		m[14][15] = 14;
		m[16][15] = -1;// C4
		m[15][16] = 14;
		m[17][16] = -1;// C4-T7
		m[16][17] = 14;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 60;
		m[14][5] = -40;// INTERTASK tra T2 e T6
	}

	/**
	 * Inizializza il wf-path T1T3T4T5T7 di fig. 13 dell'articolo assumendo che i task e i
	 * connettori siano riinizializzati con i range dell'intersezione ottenuta dalla prima analisi.
	 * I connettori e gli edge sono inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathIntersezioneT1T3T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1 6
		m[2][1] = -2; // LOWER nodo T1 -3
		m[3][2] = -1;// T1-C1
		m[2][3] = 1;
		m[4][3] = -1;// C1
		m[3][4] = 1;
		m[5][4] = -1;// C1-T3
		m[4][5] = 1;
		m[5][6] = 4; // UPPER nodo T3
		m[6][5] = -1; // LOWER nodo T3
		m[9][10] = 9; // nodo T4
		m[10][9] = -3; // nodo T4
		m[13][14] = 35; // nodo T5
		m[14][13] = -25; // nodo T5
		m[15][14] = -1;// T5-C4
		m[14][15] = 24;
		m[16][15] = -1;// C4
		m[15][16] = 25;
		m[17][16] = -1;// C4-T7
		m[16][17] = 25;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7
	}

	/**
	 * Inizializza il wf-path T1T3T4T6T7 di fig. 13 dell'articolo assumendo che i task e i
	 * connettori siano riinizializzati con i range dell'intersezione ottenuta dalla prima analisi.
	 * I connettori e gli edge sono inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathIntersezioneT1T3T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[3][2] = -1;// T1-C1
		m[2][3] = 1;
		m[4][3] = -1;// C1
		m[3][4] = 1;
		m[5][4] = -1;// C1-T3
		m[4][5] = 1;
		m[5][6] = 4; // T3
		m[6][5] = -1;
		m[9][10] = 9; // nodo T4
		m[10][9] = -3; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -39; // nodo T6
		m[15][14] = -1;// T6-C4
		m[14][15] = 1;
		m[16][15] = -1;// C4
		m[15][16] = 1;
		m[17][16] = -1;// C4-T7
		m[16][17] = 1;
		m[17][18] = 4; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 60;
		m[14][5] = -58;// INTERTASK tra T3 e T6
	}

	/**
	 * Inizializza il wf-path T1T2T4T6T7 di fig. 13 dell'articolo. I connettori e gli edge sono
	 * inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathT1T2T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 4; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[5][6] = 4; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 9; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -35; // nodo T6
		// m[14][15] = 1;// T6-C4
		// m[15][14] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 60;
		m[14][5] = -40;// INTERTASK tra T2 e T6

	}

	/**
	 * Inizializza il wf-path T1T3T4T5T7 di fig. 13 dell'articolo. I connettori e gli edge sono
	 * inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathT1T3T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 4; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[5][6] = 4; // UPPER nodo T3
		m[6][5] = -1; // LOWER nodo T3
		m[9][10] = 9; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 35; // nodo T5
		m[14][13] = -25; // nodo T5
		// m[14][15] = 6;// T5-C4
		// m[15][14] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7
	}

	/**
	 * Inizializza il wf-path T1T3T4T6T7 di fig. 13 dell'articolo. I connettori e gli edge sono
	 * inizializzati in {@link #initMatrix(float[][])}.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void initPathT1T3T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 4; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[5][6] = 4; // UPPER nodo T3
		m[6][5] = -1; // LOWER nodo T3
		m[9][10] = 9; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -35; // nodo T6
		// m[14][15] = 1;// T6-C4
		// m[15][14] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 60;
		m[14][5] = -58;// INTERTASK tra T3.b e T6.e

	}

	/**
	 * Semplice procedura per costruire una matrice delle distanze di un w-f con 3 task in serie.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void treTaskinSerie(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}

		m[0][1] = 5;
		m[1][0] = -3;
		m[2][1] = m[3][2] = m[4][3] = -defaultLB;
		m[4][5] = 3;
		m[5][4] = -2;
		m[0][5] = 12;
		m[5][0] = -7;
	}

	/**
	 * Semplice procedura per verificare una matrice delle distanze di un w-f con 1 task, punto
	 * esterno e 1 vincolo intertask.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void triangolo(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}

		m[0][1] = 6;// Task
		m[1][0] = -2;
		m[2][1] = 2;// vincolo intertask puntoEsterno-FineTask
		m[1][2] = 1;
		// m[0][2] = 8;
		// m[2][0] = -2;
		stampaTriangolo(m);
	}

	/**
	 * Stampa gli intervalli determinati su {@link #triangolo(float[][])}
	 * 
	 * @param m matrice delle distanze.
	 */
	static void stampaTriangolo(float[][] m) {
		System.out.printf("[x,y] = [%3.0f,%3.0f]\n", (m[1][0] != 0 ? -m[1][0] : m[1][0]), m[0][1]);
		System.out.printf("[p,q] = [%3.0f,%3.0f]\n", (m[2][1] != 0 ? -m[2][1] : m[2][1]), m[1][2]);
		System.out.printf("[u,v] = [%3.0f,%3.0f]\n", (m[2][0] != 0 ? -m[2][0] : m[2][0]), m[0][2]);
	}

	/**
	 * Semplice procedura per costruire una matrice delle distanze di un w-f con 2 task in serie.
	 * Del primo task rappresentiamo sia il punto di partenza sia il punto di arrivo, del secondo
	 * solo il punto di partenza.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void dueTaskinSerie(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}

		m[0][1] = 18;
		m[1][0] = -9;
		m[1][2] = 1;
		m[2][1] = -1;
		m[2][3] = 9;
		m[3][2] = -7;
		m[0][3] = 25;
		m[3][0] = -22;

		m[1][4] = 1;
		m[4][1] = -1;
		m[4][5] = 5;
		m[5][4] = -3;
		m[0][5] = 16;
		m[5][0] = -13;
		// m[2][4]=m[4][2]=notAllowed;
		// m[3][5]=m[5][3]=notAllowed;
		// m[2][5]=m[5][2]=notAllowed;
		// m[3][4]=m[4][3]=notAllowed;

		m[6][3] = m[6][5] = -defaultLB;
	}

	/**
	 * Semplice procedura per costruire una matrice delle distanze di un w-f con 2 task in
	 * parallelo.<br>
	 * Ci sono 4 nodi: 1 parallel split, 2 task e il Total Join.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void dueTaskinParallelo(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}
		m[0][1] = m[0][3] = 11;
		m[1][0] = m[3][0] = -1;
		m[1][2] = 7;
		m[2][1] = -7;
		m[3][4] = 4;
		m[4][3] = -1;
		m[2][5] = 11;
		m[5][2] = -1;
		m[4][5] = 11;
		m[5][4] = -2;
		// vincolo intertask
		m[0][5] = 11;
		m[5][0] = -7;
	}

	/**
	 * Semplice procedura per costruire una matrice delle distanze di un w-f con 2 task in parallelo
	 * che hanno un vincolo del tipo E[0,2]E.<br>
	 * Ci sono 4 nodi: T1_b, T1_e, T2_b, T2_e.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void dueTaskinParalleloConVincoloEE(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}
		m[0][1] = 3;
		m[1][0] = -2;
		m[1][3] = 2;
		m[3][1] = 0;
		m[2][3] = 3;
		m[3][2] = -2;

	}

	/**
	 * Costruisce una matrice delle distanze di un w-f con 2 task in parallelo che hanno un vincolo
	 * del tipo E[0,2]E dopo che sono stati ridotti e regressi gli altri vincoli.<br>
	 * Ci sono 4 nodi: T1_b, T1_e, T2_b, T2_e.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void dueTaskinParalleloConVincoloEE2Giro(float[][] m) {

		for (int i = 0; i < m.length; i++) {
			for (int j = 0; j < i; j++)
				m[i][j] = m[j][i] = defaultUB;
			m[i][i] = 0;
		}
		m[0][1] = 6;// T1
		m[1][0] = -4;
		m[3][1] = 1;// UB E_T2--E_T1
		m[1][3] = 2;// LB E_T2--E_T1
		m[2][3] = 3;// T2
		m[3][2] = -2;
		// nuovi vincoli
		m[0][2] = 3;// UB S_T1--S_T2
		m[2][0] = -3;// LB S_T1--S_T2
		m[0][3] = 8;// UB S_T1--E_T2
		m[3][0] = -4;// LB S_T1--E_T2

		// m[2][1] = 8;
		// m[1][2] = -1;
	}

	/**
	 * Stampa i valori dei link della matrice dueTaskinParalleloConVincoloEE.
	 * 
	 * @param m matrice delle distanze.
	 */
	static void stampaLinkDueTaskinParalleloConVincoloEE(float[][] m) {
		System.out.printf("Fissato: T1 = [%3.0f,%3.0f]\n", -m[1][0], m[0][1]);
		System.out.printf("Fissato: T2 = [%3.0f,%3.0f]\n", -m[3][2], m[2][3]);
		System.out.printf("Fissato: E_T2-E_T1 = [%3.0f,%3.0f]\n", -m[1][3], m[3][1]);
		System.out.printf("S_T1-S_T2 = [%3.0f,%3.0f]\n", (m[2][0] != 0 ? -m[2][0] : m[2][0]),
				m[0][2]);
		System.out.printf("S_T1-E_T2 = [%3.0f,%3.0f]\n", (m[3][0] != 0 ? -m[3][0] : m[3][0]),
				m[0][3]);
		System.out.printf("S_T2-E_T1 = [%3.0f,%3.0f]\n", (m[1][2] != 0 ? -m[1][2] : m[1][2]),
				m[2][1]);
	}

	/**
	 * Esecuzione del wf-path T1T3T4T5T7 di fig. 13 dell'articolo in cui T1=6 e T3=4 e i connettori
	 * e gli edge sono a 1.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void esecuzioneT1T3T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[2][3] = 1; // delay T1-C1
		m[3][2] = -1;
		m[3][4] = 1; // nodo C1
		m[4][3] = -1;
		m[4][5] = 1;// delay C1-T3
		m[5][4] = -1;
		m[5][6] = 4; // UPPER nodo T3
		m[6][5] = -4; // LOWER nodo T3
		m[9][10] = 9; // nodo T4
		m[10][9] = -3; // nodo T4
		m[13][14] = 35; // nodo T5
		m[14][13] = -25; // nodo T5
		m[14][15] = 1;// arco T5-C4
		m[15][14] = -1;
		m[15][16] = 1;// nodo C4
		m[16][15] = -1;
		m[16][17] = 1;// delay C4-T7
		m[17][16] = -1;
		m[17][18] = 4; // nodo T7
		m[18][17] = -4; // nodo T7
	}

	/**
	 * Esecuzione del wf-path T1T3T4T6T7 di fig. 13 dell'articolo in cui T1=6 e T3=4 e i connettori
	 * e gli edge sono a 1.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void esecuzioneT1T3T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 4; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		// m[2][3] = m[3][4] = m[4][5] = 1;
		m[3][2] = m[4][3] = m[5][4] = -1;
		m[5][6] = 1; // UPPER nodo T3
		m[6][5] = -1; // LOWER nodo T3
		m[9][10] = 9; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -35; // nodo T6
		// m[14][15] = 1;//T6-C4
		m[15][14] = -1;
		// m[15][16] = 1;// nodo C4
		m[16][15] = -1;
		// m[16][17] = 1;// delay C4-T7
		m[17][16] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4; // nodo T7

		m[5][14] = 60;
		m[14][5] = -58;// INTERTASK tra T3.b e T6.e
	}

	/**
	 * Esecuzione del wf-path T1T2T4T5T7 di fig. 13 dell'articolo in cui T1=6 e T2=4 e i connettori
	 * e gli edge sono a 1.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void esecuzioneT1T2T4T5T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[2][3] = 1;// T1-C1
		m[3][2] = -1;// T1-C1
		m[3][4] = 1;// C1
		m[4][3] = -1;// C1
		m[4][5] = 14;// C1-T2
		m[5][4] = -1;// C1-T2
		m[5][6] = 2; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 2; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 25; // nodo T5
		m[14][13] = -25; // nodo T5
		m[14][15] = 24;// T5-C4
		m[15][14] = -1;
		m[15][16] = 14;// C4
		m[16][15] = -1;
		m[16][17] = 14;// C4-T7
		m[17][16] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4;

		m[5][14] = 35;
		m[14][5] = -30;// INTERTASK tra T2 e T5
	}

	/**
	 * Esecuzione del wf-path T1T2T4T6T7 di fig. 13 dell'articolo in cui T1=6 e T2=4 e i connettori
	 * e gli edge sono a 1.
	 * 
	 * @param m matrice delle distanze de wf-path trasformato in un STP.
	 * @see #initMatrix(float[][])
	 */
	static void esecuzioneT1T2T4T6T7(float[][] m) {
		initMatrix(m);
		m[1][2] = 2; // UPPER nodo T1
		m[2][1] = -2; // LOWER nodo T1
		m[2][3] = 1;// T1-C1
		m[3][2] = -1;// T1-C1
		m[3][4] = 1;// C1
		m[4][3] = -1;// C1
		m[4][5] = 14;// C1-T2
		m[5][4] = -1;// C1-T2
		m[5][6] = 2; // UPPER nodo T2
		m[6][5] = -2; // LOWER nodo T2
		m[9][10] = 2; // nodo T4
		m[10][9] = -2; // nodo T4
		m[13][14] = 45; // nodo T6
		m[14][13] = -35; // nodo T6
		m[14][15] = 14;// T6-C4
		m[15][14] = -1;
		m[15][16] = 14;// C4
		m[16][15] = -1;
		m[16][17] = 14;// C4-T7
		m[17][16] = -1;
		m[17][18] = 7; // nodo T7
		m[18][17] = -4;

		m[5][14] = 60;
		m[14][5] = 40;// INTERTASK tra T2 e T6

	}

	/**
	 * Restringe il valore di arco che precede un task (contigent follows a requirment edge) ed è
	 * sottoposto a un vincolo con la fine del task.<br>
	 * 
	 * Per ora, si assume che l'arco sia sempre incidente al task.
	 * 
	 * @param m matrice delle distanze
	 * @param startE indice inizio arco
	 * @param endE indice fine arco
	 * @param endT indice fine task
	 */
	static void follow(float[][] m, int startE, int endE, int endT) {
		float u, v;

		u = -m[endT][startE] + m[endT][endE];// p-x_2;
		v = m[startE][endT] - m[endE][endT];// q-y_2

		// if (u < (-m[endE][startE])) {
		// System.out
		// .print("Il lower bound calcolato per il caso follow non restringe!\n");
		// }
		m[endE][startE] = -u;

		// if (v > (m[startE][endE])) {
		// System.out
		// .print("L'upper bound calcolato per il caso follow non restringe!\n");
		// }
		m[startE][endE] = v;
	}

	/**
	 * Simple bidimensional array cloner.
	 * 
	 * @param source
	 * @param destination
	 */
	static void multiArrayCopy(float[][] source, float[][] destination) {
		for (int a = 0; a < source.length; a++) {
			System.arraycopy(source[a], 0, destination[a], 0, source[a].length);
		}
	}

	/**
	 * Simple check whether two matrices are different or not.
	 * 
	 * @param a
	 * @param b
	 * @return true se le due matrici sono differenti
	 */
	static boolean isDifferent(float[][] a, float[][] b) {
		for (int i = a.length; i-- != 0;) {
			for (int j = a.length; j-- != 0;) {
				if (a[i][j] != b[i][j])
					return true;
			}
		}
		return false;
	}

	/**
	 * Restituisce vero se i range dei task elencati in taskIndex sono stati ristretti nella matrice
	 * b rispetto alla matrice a.
	 * 
	 * @param a
	 * @param b
	 * @param taskIndex
	 * @return true se i range dei task sono stati ristretti.
	 */
	static boolean shrikingCheck(float[][] a, float[][] b, int[] taskIndex) {
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
				multiArrayCopy(b, a);
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param m matrice di adiacenza.
	 */
	static void checkDCPath(float[][] m) {

		// Assumo che la matrice sia già minimizzata

		float[][] m1 = new float[m.length][m.length];
		multiArrayCopy(m, m1);

		/**
		 * Indici dei nodi che rappresentano lo start dei task del path
		 */
		int[] sTask = { 2, 6, 10 };

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
			m1 = Floyd(m1);
			if (checkCicliNegativi(m1)) {
				print(m1);
				return;
			}

			shrinked = shrikingCheck(m, m1, sTask);
			if (shrinked)
				return;

			// System.out.println("Verifica se le distanze sono cambiate");
			isDifferent = isDifferent(m, m1);

			// System.out.println("Aggiorno la matrice originale");
			multiArrayCopy(m1, m);

		} while (isDifferent);
		System.out.println("");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final int n = 14;
		float[][] m = new float[n][n], m1 = new float[n][n];

		// float[][] m = { {0,3,2}, {3,0,-2}, {-2,2,0} };
		// float[][] m = { { 0, 3, 2 }, { -1, 0, 2 }, { -1, -1, 0 } };

		// inizializzazione FloydW

		// initPathT1T3T4T6T7(m);
		// controllablePathT1T3T5(m);
		// controllablePathT2T3T4(m);
		// controllablePathT2T3T5(m);
		// initPathIntersezioneT1T3T4T6T7(m);
		// initPathIntersezioneT1T3T4T6T7(m);
		// initFullWF(m);
		// initFullWFT1T2(m);

		// vincolo INTERTASK tra T1 e T7
		// esecuzionFullWFT1T2ForTimeDependent(m);
		// esecuzionFullWFT1T2(m);
		// esecuzioneT1T3T4T6T7(m);
		// esecuzioneT1T2T4T5T7(m);

		// m = new float[7][7];
		// dueTaskinSerie(m);

		// m = new float[6][6];
		// treTaskinSerie(m);

		// dueTaskinParalleloConVincoloEE2Giro(m);
		// triangolo(m);
		controllablePathT2T3T4(m);

		multiArrayCopy(m, m1);

		System.out.println("Matrice delle distanze prima Floyd-W:");
		print(m);
		m = Floyd(m);
		// System.out.println("Matrice delle distanze dopo Floyd-W:");
		// print(m);
		checkCicliNegativi(m);
		if (shrikingCheck(m1, m, new int[] { 2, 6, 10 }))
			return;

		System.out.println("Intervalli prima calcolo controllabilità:");
		printIntervalli2(m);

		checkDCPath(m);
		System.out.println("Intervalli DOPO calcolo controllabilità:");
		printIntervalli2(m);
		// stampaLinkDueTaskinParalleloConVincoloEE(m);
		// printIntervalli(m);
		// printIntervalliFullWF(m);
		// stampaTriangolo(m);
	}
}
