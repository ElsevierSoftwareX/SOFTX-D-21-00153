package it.univr.di.wf;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Semplice classe per rappresenatare grafi delle distanze di un workflow. Ci sono sia metodi che
 * gestiscono la matrice di adiacenza costituita solo da interi, sia matrici di adiacenza costituite
 * da oggetti di tipo {@link Edge}.
 * 
 * @author posenato
 */
public class DistanceGraph {

	/**
	 * Valore di default per l'upper bound alle durate e ai ritardi.
	 */
	final static int defaultUB = Integer.MAX_VALUE;

	/**
	 * Valore di default per il lower bound alle durate e ai ritardi.
	 */
	final static int defaultLB = 1;

	/**
	 * Costante carattere che rappresenta il simbolo di infinito: ∞.
	 */
	final static char INFINITY = '∞';

	/**
	 * logger della classe
	 */
	static Logger LOG = Logger.getLogger(DistanceGraph.class.getName());

	/**
	 * Determina la chiusura transitiva della matrice G.
	 * 
	 * @param distance matrice delle distanze
	 */
	static void FloydWarshall(int[][] distance) {
		final int n = distance.length;
		long val = 0;
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (distance[i][k] == Integer.MAX_VALUE || distance[k][j] == Integer.MAX_VALUE
							|| (val = (long) distance[i][k] + distance[k][j]) >= Integer.MAX_VALUE)
						continue;
					if (val < distance[i][j])
						distance[i][j] = (int) val;
				}
			}
		}
	}

	/**
	 * Determina la chiusura transitiva della matrice G.
	 * 
	 * @param distance matrice delle distanze
	 */
	static void FloydWarshall(Edge[][] distance) {
		final int n = distance.length;
		long val = 0;
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (distance[i][k].valore == Integer.MAX_VALUE || distance[k][j].valore == Integer.MAX_VALUE
							|| (val = (long) distance[i][k].valore + distance[k][j].valore) >= Integer.MAX_VALUE)
						continue;
					if (val < distance[i][j].valore)
						distance[i][j].valore = (int) val;
				}
			}
		}
	}

	/**
	 * Determina la chiusura transitiva della matrice G calcolando i valori a partire dalla riga di
	 * indice maggiore.
	 * 
	 * @param distance matrice delle distanze
	 */
	static void FloydWarshallI(int[][] distance) {
		final int n = distance.length;
		long val;
		for (int k = n; k-- != 0;) {
			for (int i = n; i-- != 0;) {
				for (int j = n; j-- != 0;) {
					if (distance[i][k] == Integer.MAX_VALUE || distance[k][j] == Integer.MAX_VALUE
							|| (val = (long) distance[i][k] + distance[k][j]) >= Integer.MAX_VALUE)
						continue;
					if (val < distance[i][j])
						distance[i][j] = (int) val;
				}
			}
		}
	}

	/**
	 * Verifica se la matrice delle distanze contiene cicli negativi. In caso positivo, stampa un
	 * messaggio e restituisce vero.
	 * 
	 * @param m matrice delle distanze
	 * @return vero se la matrice contiene cicli negativi, falso altrimenti.
	 */
	static boolean areNegativeCycles(int[][] m) {
		for (int i = 0; i < m.length; i++) {
			if (m[i][i] < 0) {
				System.out.print("La matrice contiene cicli negativi!\n");
				return true;
			}
		}
		return false;
	}

	/**
	 * Verifica se la matrice delle distanze contiene cicli negativi. In caso positivo, stampa un
	 * messaggio e restituisce vero.
	 * 
	 * @param m matrice delle distanze
	 * @return vero se la matrice contiene cicli negativi, falso altrimenti.
	 */
	static boolean areNegativeCycles(Edge[][] m) {
		for (int i = m.length; i-- != 0;) {
			if (m[i][i].valore != 0) {
				if (m[i][i].valore < 0)
					LOG.info("La matrice contiene cicli negativi!\n");
				else
					LOG.info("La matrice contiene nodi con loop positivo!\n");
				return true;
			}
		}
		return false;
	}

	/**
	 * Costruisce la stringa che rappresenta il contenuto della matrice.
	 * 
	 * @param distance
	 * @return stringa che rappresenta la matrice
	 */
	static String print(int[][] distance) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				if (distance[i][j] == defaultUB)
					sb.append(String.format("%5s", INFINITY));
				else {
					sb.append(String.format("%5d", distance[i][j]));
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * Costruisce la stringa che rappresenta il contenuto della matrice.
	 * 
	 * @param distance
	 * @return stringa che rappresenta la matrice
	 */
	static String print(Edge[][] distance) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				if (distance[i][j].valore == defaultUB)
					sb.append(String.format("%5s", INFINITY));
				else {
					sb.append(String.format("%5d", distance[i][j].valore));
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * Simple bidimensional array cloner.
	 * 
	 * @param source
	 * @param destination
	 */
	static void deepClone(int[][] source, int[][] destination) {
		for (int i = source.length; i-- != 0;) {
			System.arraycopy(source[i], 0, destination[i], 0, source[i].length);
		}
	}

	/**
	 * Simple bidimensional array cloner.
	 * 
	 * @param source
	 * @param destination
	 */
	static void deepClone(Edge[][] source, Edge[][] destination) {
		if (source == null || destination == null)
			return;
		final int n = source.length;
		for (int i = n; i-- != 0;) {
			for (int j = n; j-- != 0;) {
				destination[i][j].nome = source[i][j].nome;
				destination[i][j].valore = source[i][j].valore;
				destination[i][j].tipo = source[i][j].tipo;
				destination[i][j].isLowerBound = source[i][j].isLowerBound;
				if (source[i][j].wait != null) {
					destination[i][j].wait = new HashMap<Integer, Integer>(source[i][j].wait);
				}
			}
		}
	}

	/**
	 * Simple check whether two matrices are different or not.
	 * 
	 * @param a
	 * @param b
	 * @return true se sono different
	 */
	static boolean isDifferent(int[][] a, int[][] b) {
		for (int i = a.length; i-- != 0;) {
			for (int j = a.length; j-- != 0;) {
				if (a[i][j] != b[i][j])
					return true;
			}
		}
		return false;
	}

	/**
	 * Simple check whether two matrices are different or not.
	 * 
	 * @param a
	 * @param b
	 * @return true se sono different
	 */
	static boolean isDifferent(Edge[][] a, Edge[][] b) {
		for (int i = a.length; i-- != 0;) {
			for (int j = a.length; j-- != 0;) {
				if (a[i][j].valore != b[i][j].valore)
					return true;
			}
		}
		return false;
	}

	// ///////////////////////////////////////////////////////////////////////////////
	// Parte non statica

	/**
	 * Matrice delle distanze del grafo
	 */
	Edge[][] distance;

	/**
	 * Costruttore di default. Tutte le distanze sono messe a {@link #defaultUB}, tranne per i loop
	 * che sono messi a 0.
	 * 
	 * @param n numero di nodi
	 */
	public DistanceGraph(int n) {
		distance = new Edge[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < i; j++) {
				distance[i][j] = new Edge();
				distance[j][i] = new Edge();
			}
			distance[i][i] = new Edge(0, false);
		}
	}

	/**
	 * Costruttore per copia. L'oggetto creato è distinto dall'oggetto passato.
	 * 
	 * @param source distance graph da copiare.
	 */
	public DistanceGraph(DistanceGraph source) {
		if (source == null)
			return;
		final int n = source.size();
		distance = new Edge[n][n];
		Edge sourceE;
		for (int i = n; i-- != 0;) {
			for (int j = n; j-- != 0;) {
				sourceE = source.distance[i][j];
				distance[i][j] = new Edge(sourceE.nome, sourceE.tipo, sourceE.valore, sourceE.isLowerBound);
				if (sourceE.wait != null) {
					distance[i][j].wait = new HashMap<Integer, Integer>(sourceE.wait);
				}
			}
		}
	}

	/**
	 * Ritorna la dimensione del grafo.
	 * 
	 * @return la dimensione del grafo
	 */
	public int size() {
		return distance.length;
	}

	/**
	 * @return true se ci sono cicli negativi, falso altrimenti.
	 * @see DistanceGraph#areNegativeCycles(Edge[][])
	 */
	public boolean areNegativeCycles() {
		return DistanceGraph.areNegativeCycles(this.distance);
	}

	/**
	 * Determina la chiusura transitiva della matrice {@link #distance}.
	 * 
	 * @see DistanceGraph#FloydWarshall(Edge[][])
	 */
	public void FloydWarshall() {
		DistanceGraph.FloydWarshall(this.distance);
	}

	/**
	 * Per ciascuno triangolo del grafo, determina quale riduzione e regressione applicare e la
	 * applica.
	 */
	public void applyReductionsRegressions() {
		final int n = distance.length;
		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (k == i || k == j || i == j)
						continue;
					// caso follow
					if ((distance[k][i].tipo == Edge.Type.edge || distance[k][i].tipo == Edge.Type.derived)
							&& distance[i][j].tipo == Edge.Type.task
							&& (distance[k][j].tipo == Edge.Type.constraint || distance[k][j].tipo == Edge.Type.derived)) {
						follow(k, i, j);
					}
					// // caso precede
					// if ((distance[k][i].tipo == Edge.Type.task)
					// && (distance[i][j].tipo == Edge.Type.edge || distance[i][j].tipo ==
					// Edge.Type.derived)
					// && (distance[k][j].tipo == Edge.Type.constraint || distance[k][j].tipo ==
					// Edge.Type.derived)) {
					// precede(k, i, j);
					// }

					// caso wait semplice
					if ((distance[k][i].tipo == Edge.Type.task)
							&& (distance[j][i].tipo == Edge.Type.edge || distance[j][i].tipo == Edge.Type.derived || distance[j][i].tipo == Edge.Type.constraint)
							&& (distance[k][j].tipo == Edge.Type.edge || distance[k][j].tipo == Edge.Type.derived || distance[k][j].tipo == Edge.Type.constraint)) {
						wait(k, i, j);
					}
					// caso task da k a i e da k a j
					// Si rompe in due casi, ciascuno dei quali considera solo 1 task e l'altro come
					// edge requirement.
					// Questo caso non dovrebbe mai accadere nei nostri workflow!
					if ((distance[k][i].tipo == Edge.Type.task) && (distance[k][j].tipo == Edge.Type.task)
							&& (distance[j][i].tipo == Edge.Type.edge || distance[j][i].tipo == Edge.Type.derived)) {
						LOG.info("Caso di due task che partono dallo stesso nodo. Nodo: " + k + ". Nome task:"
								+ distance[k][i].nome + ". Nome altro task:" + distance[k][j].nome);
						// considero solo il task (k,i)
						// wait(k, i, j);
						// considero solo il task (k,j)
						// wait(k, j, i);
					}
					// regressione di un eventuale wait
					waitRegression(k, i, j);
				}
			}
		}
	}

	/**
	 * Dato un task e un edge che si assume incidere sul nodo fine del task (quindi non è caso
	 * follow o precede), si determina il valore dell'intervallo dell'edge tra il nodo inizio task e
	 * il nodo dove parte l'edge dato.<br>
	 * I casi possibili per determinare i nuovi valori sono 3: precede, follow e wait. Questo metodo
	 * li gestisce tutti.<br>
	 * Se l'intervallo presente tra inizio task e fine task non è un intervallo con lower bound
	 * positivo, allora significa che l'arco incide sull'inizio del task, quindi non si esegue nulla
	 * (caso gestito dal metodo precede).<br>
	 * 
	 * @param startT indice nodo inizio task.
	 * @param endT indice nodo fine task.
	 * @param startE indice nodo inizio edge. Si assume che l'indice del nodo di fine edge sia
	 *            uguale a ednT.
	 */
	public void wait(int startT, int endT, int startE) {

		int x = -distance[endT][startT].valore; // l.b Task
		int y = distance[startT][endT].valore;// u.b. Task
		String nomeT = (distance[startT][endT].nome.length() == 0) ? "{" + startT + ", " + endT + "}"
				: distance[startT][endT].nome;
		int p = -distance[endT][startE].valore;// l.b. edge
		int q = distance[startE][endT].valore;// u.b. edge
		String nomeE = (distance[startE][endT].nome.length() == 0) ? "(" + startE + ", " + endT + ")"
				: distance[startE][endT].nome;
		int u = -distance[startE][startT].valore;
		int v = distance[startT][startE].valore;
		String nomeEW = (distance[startT][startE].nome.length() == 0) ? "(" + startT + ", " + startE + ")"
				: distance[startT][startE].nome;
		int t;

		// Se gli estremi non costituiscono un intervallo corretto, non si fa nulla
		if (x < 0 || y < x) {
			return;
		}

		LOG.finest("Probabile caso wait con task " + nomeT + " e arco " + nomeE + " su arco " + nomeEW);
		LOG.finest("Valori esistenti su " + nomeE + "=> [p,q]: [" + p + ", " + q + "]");
		LOG.finest("Valori esistenti su " + nomeEW + "=> [u,v]: [" + u + ", " + v + "]");
		if (q < 0) {
			// caso precede
			LOG.finest("Caso precede");
			u = x - q;
			v = y - p;
		} else {
			if (p >= 0) {
				// caso follow
				LOG.finest("Caso follow");
				u = y - q;
				v = x - p;
			} else {
				// caso wait
				t = y - q;
				LOG.finest("Caso wait. Valore di t=" + t);
				if (t >= 0) {
					// solo se il ritardo è significativo, viene valutato
					if (t < x) {
						u = t;
					}
					// memorizza wait <endT, t>
					if (distance[startT][startE].wait == null)
						distance[startT][startE].wait = new HashMap<Integer, Integer>();
					LOG.finest("Definizione wait <E_" + nomeT + ", " + t + "> su edge " + nomeEW);
					distance[startT][startE].wait.put(endT, t);
				}
			}
		}
		boolean restringimento = false;
		LOG.finest("Valori calcolati [u,v]: [" + u + ", " + v + "]");
		if (u > (-distance[startE][startT].valore)) {
			distance[startE][startT].valore = -u;
			restringimento = true;
		}

		if (v < (distance[startT][startE].valore)) {
			distance[startT][startE].valore = v;
			restringimento = true;
		}

		if (!restringimento)
			LOG.finest("Nuovi [u,v] NON registrati.");
		else
			LOG.finest("Nuovi [u,v] registrati.");
	}

	/**
	 * Propaga l'eventuale vincolo wait <startT+1, t> presente sull'edge (startT,endEconWait)
	 * sull'edge (startT,endEDestionation).<br>
	 * La propagazione avviene:
	 * <ol>
	 * <li>calcolando il nuovo valore t' in funzione del tipo di edge (endEDestionation,
	 * endEconWait)
	 * <li>se t'>0, aggiornando il wait (creandolo se non esiste) sull'edge
	 * (starT,endEDestionation).
	 * </ol>
	 * Il metodo esegue tutti i controlli necessari sulla correttezza del tipo di edge e sulla
	 * presenza dei wait. Si può quindi chiamare la procedura passando una terna di indici senza
	 * verificare a priori la correttezza. Nel caso in cui la terna non rappresenti un caso di
	 * regressione, il metodo ritorna facendo nulla.
	 * 
	 * @param startT origine del task che ha determinato il wait. Il wait deve contenere l'indice
	 *            startT+1 come fine task da aspettare.
	 * @param endEconWait (startT, endEconWait) contiene il wait da regredire <E_{startT+1},t>
	 * @param endEDestination edge (startT, endEDestination) presso qui regredire il wait.
	 */
	public void waitRegression(int startT, int endEconWait, int endEDestination) {
		// Controlli per verificare se c'è un wait da regredire

		// Se non c'è un task che inizia in startT, non si applica
		if (startT + 1 >= distance.length)
			return;
		if (distance[startT][startT + 1].tipo != Edge.Type.task)
			return;

		// Se l'edge (startT, endEDestination) è un constraint, non si applica. Non ne sono sicuro!
		// Ricordare che (startT, endEDestination) può essere solo un constraint o un derived o un
		// edge
		// if (distance[startT][endEDestination].tipo == Edge.Type.constraint)
		// return;

		// Se l'edge (endEDestination, endEconWait) ha orientamento opposto, non si applica
		// L'orientamento è opposto quando il lower bound < 0
		if (distance[endEconWait][endEDestination].valore > 0) // nella matrice delle distanze il
			// lower è memorizzato invertito di
			// segno
			return;

		// Se l'edge (endEDestination, endEconWait) è un internal, non si applica
		Edge.Type regressionType = distance[endEDestination][endEconWait].tipo;
		if (regressionType == Edge.Type.internal) {
			return;
		}

		String nomeT = (distance[startT][startT + 1].nome.length() == 0) ? "{" + startT + ", " + (startT + 1) + "}"
				: distance[startT][startT + 1].nome;
		String nomeEconWait = (distance[startT][endEconWait].nome.length() == 0) ? "(" + startT + ", " + (endEconWait)
				+ ")" : distance[startT][endEconWait].nome;
		String nomeEdgeDest = (distance[startT][endEDestination].nome.length() == 0) ? "(" + startT + ", "
				+ (endEDestination) + ")" : distance[startT][endEDestination].nome;

		// Se non c'è un wait su (startT, endEconWait) riferito al task, non si applica
		Map<Integer, Integer> waitMap = distance[startT][endEconWait].wait;
		Integer tObj = (waitMap != null) ? waitMap.get(startT + 1) : null;
		if (tObj == null) {
			return;
		}
		LOG.finest("Probabile caso wait regression di wait su arco " + nomeEconWait + " relativi al task " + nomeT
				+ " verso edge " + nomeEdgeDest);
		// valore di wait
		int t = tObj.intValue();
		// valori del task principale
		int x1 = distance[startT][startT + 1].valore;
		// valori del task/connettore/edge attraverso il quale fare la regressione
		int x2 = -distance[endEconWait][endEDestination].valore;
		int y2 = distance[endEDestination][endEconWait].valore;
		// nuovo valore di wait
		int t1 = (regressionType == Edge.Type.task) ? t - x2 : t - y2;
		// lower bound dell'edge che avrà il nuovo wait
		int u = -distance[endEDestination][startT].valore;

		if (t1 >= 0) {
			LOG.finest("Il wait <E_" + nomeT + ", " + t + "> si regredisce con nuovo t'=" + t1);
			if (t1 < x1) {
				// il wait regredito diventa unconditional
				LOG.finest("t' (=" + (t1) + ") è inferiore a x1 (=" + (x1) + ")");
				u = t1;
			}
			// memorizza wait <endT, t1>
			if (distance[startT][endEDestination].wait == null)
				distance[startT][endEDestination].wait = new HashMap<Integer, Integer>();
			LOG.finest("Wait <E_" + nomeT + ", " + t + "> propagato da " + nomeEconWait + " a " + nomeEdgeDest
					+ ": <E_" + nomeT + ", " + t1 + ">");
			distance[startT][endEDestination].wait.put(startT + 1, t1);
			// se il wait regredito implica un migliore lower bound... si aggiorna il lower bound
			if (u > -distance[endEDestination][startT].valore) {
				LOG.finer("Lower bound su " + nomeEdgeDest + " (=" + -distance[endEDestination][startT].valore
						+ ") viene aggiornato a " + u);
				distance[endEDestination][startT].valore = -u;
			}
		} else {
			LOG.finest("Nessuna regressione in quanto t (=" + t + ") regredisce a t' (=" + t1 + "), negativo.");
		}
	}

	/**
	 * Restituisce vero se i range dei task sono stati ristretti nella matrice b rispetto alla
	 * matrice a.
	 * 
	 * @param a
	 * @param b
	 * @return vero se i range dei task sono stati ristretti nella matrice b rispetto alla matrice
	 *         a, falso altrimenti.
	 */
	static boolean shrikingCheck(Edge[][] a, Edge[][] b) {
		int n = a.length;
		int upperBound;
		int lowerBound;

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (a[i][j].tipo == Edge.Type.task && !a[i][j].isLowerBound) {
					// Controlliamo solo una volta ogni task. Controlliamo quando incontriamo
					// l'upper bound.
					upperBound = a[i][j].valore;
					lowerBound = -a[j][i].valore;
					if (upperBound != b[i][j].valore || lowerBound != -b[j][i].valore) {
						LOG.info("\nIl task " + a[i][j].nome + " che inizia al nodo " + i
								+ " ha subito una modifica del range: [" + lowerBound + ", " + upperBound + "] ==> ["
								+ (-b[j][i].valore) + ", " + b[i][j].valore + "]");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Esegue il check della controllabilità dinamica su un path rappresentato dall'oggetto
	 * corrente.
	 * 
	 * @param stampaMatrice stampa valori matrice all'inizio e alla fine.
	 * @return true se il path è dinamicamente controllabile, falso altrimenti.
	 */
	public boolean checkDCPath(boolean stampaMatrice) {
		// tengo copia della matrice

		DistanceGraph graph1 = new DistanceGraph(this);

		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Matrice delle distanze inizio:\n" + DistanceGraph.print(distance));
			LOG.finer("Range iniziali:\n" + graph1.printRange(true));
		}

		int ciclo = 1;
		boolean isDifferent = false, shrinked = false;
		do {
			LOG.info("*** Ciclo " + (ciclo++) + " ***");
			LOG.finer("Minimizzo matrice con Floyd & Warshall");
			graph1.FloydWarshall();
			if (graph1.areNegativeCycles()) {
				LOG.info("Matrice con cicli negativi!\n");
				LOG.finer("Matrice:\n" + print(graph1.distance));
				return false;
			}
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Matrice dopo minimizzazione:\n" + print(graph1.distance));
				LOG.finer("Range:\n" + graph1.printRange(true));
			}

			LOG.finer("Verifico se ci sono dei restringimenti di range di task");
			shrinked = shrikingCheck(distance, graph1.distance);
			if (shrinked) {
				LOG.info("Matrice con restringimenti dei task.\n");
				return false;
			}

			LOG.finer("Applico riduzioni e regressioni");
			graph1.applyReductionsRegressions();
			if (LOG.isLoggable(Level.FINER)) {
				LOG.finer("Matrice dopo riduzioni e regressioni:\n" + print(graph1.distance));
				LOG.finer("Range:\n" + graph1.printRange(true));
			}

			LOG.finer("Verifico se le distanze sono cambiate");
			isDifferent = DistanceGraph.isDifferent(distance, graph1.distance);

			// System.out.println("Aggiorno la matrice originale");
			DistanceGraph.deepClone(graph1.distance, distance);

		} while (isDifferent);
		LOG.fine("Punto fisso raggiunto. Sono occorsi " + (ciclo - 1) + " cicli.");
		LOG.finer("Range finali:\n" + graph1.printRange(true));
		return true;
	}

	/**
	 * Restringe il valore di arco che precede un task (contingent edge follows a requirment one) ed
	 * è sottoposto a un vincolo con la fine del task. L'edge incide sul nodo di inizio task.<br>
	 * Se l'intervallo presente tra inizio task e fine task non è un intervallo con lower bound
	 * positivo e upper bound maggiore, allora significa che l'arco incide sulla fine del task,
	 * quindi non si esegue nulla.
	 * 
	 * @param startE indice nodo inizio arco
	 * @param startT indice nodo inizio task
	 * @param endT indice nodo fine task
	 */
	public void follow(int startE, int startT, int endT) {
		int u, u1, v, v1, x, y, p, q;
		// Estremi del range del task
		x = -distance[endT][startT].valore;
		y = distance[startT][endT].valore;
		// estremi dell'edge da aggiustare
		u = -distance[startT][startE].valore;
		v = distance[startE][startT].valore;
		// Se (starT, endT) non sono un task o gli estremi non costituiscono un intervallo corretto
		// o gli estremi dell'edge non costituiscono un range corretto, non si fa nulla
		if ((distance[startT][endT].tipo != Edge.Type.task) || (x < 0 || y < x) || (u < 0 || v < u)) {
			return;
		}

		String edgeName = (distance[startE][startT].nome.length() == 0) ? "(" + startE + ", " + startT + ")"
				: distance[startE][startT].nome;
		// estremi vincolo
		p = -distance[endT][startE].valore;
		q = distance[startE][endT].valore;

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Probabile 'follow' arco " + edgeName + " e task " + distance[startT][endT].nome + ".");
			LOG.finest("Valori esistenti [u,v]: [" + u + ", " + v + "]");
		}
		// nuovi valori per l'edge
		u1 = p - x;
		v1 = q - y;
		LOG.finest("Valori calcolati [u,v]: [" + u1 + ", " + v1 + "]");

		boolean restringimento = false;
		if (u1 > u) {
			distance[startT][startE].valore = -u1;
			restringimento = true;
		}

		if (v1 < v) {
			distance[startE][startT].valore = v1;
			restringimento = true;
		}

		if (!restringimento)
			LOG.finest("Nuovi [u,v] NON registrati.");
		else
			LOG.finest("Nuovi [u,v] registrati.");
	}

	/**
	 * 
	 * @param v
	 * @return valore formattato
	 */
	private String valore2string(int v) {
		return (Math.abs(v) == defaultUB) ? String.format("%2s%1s", (v<0)?"-":"", INFINITY) : String.format("%3d", v);
	}

	/**
	 * Restiuisce la stringa con tutti gli intervalli dei task task e i nuovi valori per i vincoli
	 * intertask. Se 'withEdge' è vero, stampa anche i valori degli edge con eventuali wait.
	 * 
	 * @param withEdge se true, stampa anche i valori degli edge
	 * @return string con i valori degli intervalli.
	 */
	public String printRange(boolean withEdge) {
		StringBuffer sbT = new StringBuffer(), sbC = new StringBuffer(), sbE = new StringBuffer();
		Edge e;
		Map<Integer, Integer> wait;
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < distance.length; j++) {
				e = distance[i][j];
				if (e.tipo == Edge.Type.task && !e.isLowerBound) {
					// è un u.b. di un task. L'edge contiene tutte le info
					sbT.append(String.format("Task %s:\t\t[%s, %s]\n", e.nome, valore2string(-distance[j][i].valore),
							valore2string(e.valore)));
					continue;
				}
				if (e.tipo == Edge.Type.connector && !e.isLowerBound) {
					// è un u.b. di un connettore. L'edge contiene tutte le info
					sbT.append(String.format("Connector %s:\t[%s, %s]\n", e.nome,
							valore2string(-distance[j][i].valore), valore2string(e.valore)));
					continue;
				}
				if (e.tipo == Edge.Type.internal && !e.isLowerBound) {
					// è un u.b. di un internal. L'edge contiene tutte le info
					sbT.append(String.format("Internal %s:\t[%s, %s]\n", e.nome,
							valore2string(-distance[j][i].valore), valore2string(e.valore)));
					continue;
				}
				if (e.tipo == Edge.Type.constraint && !e.isLowerBound) {
					// è un u.b. di un connettore. L'edge contiene tutte le info
					sbC.append(String.format("Vincolo %s:\t[%s, %s]\n", e.nome, valore2string(-distance[j][i].valore),
							valore2string(e.valore)));
				}
				if (withEdge && e.tipo == Edge.Type.edge && !e.isLowerBound) {
					// è un u.b. di un task. L'edge contiene tutte le info
					sbE.append(String.format("Edge %s:\t[%s, %s]\n", e.nome, valore2string(-distance[j][i].valore),
							valore2string(e.valore)));
				}
				wait = e.wait;
				String nomeE = (e.nome.length() == 0) ? "(" + i + ", " + j + ")" : e.nome;
				if (wait != null) {
					int nodo, t;
					Set<Entry<Integer, Integer>> waitList = wait.entrySet();
					for (Entry<Integer, Integer> k : waitList) {
						nodo = k.getKey();
						t = k.getValue();
						// Non si stampano wait che terminano alla fine di un task
						if ((j - 1 == 0) || (j - 1 > 0 && distance[j - 1][j].tipo != Edge.Type.task)) {
							sbE.append(String.format("Wait edge %s:\t<E_%s, %3s>\n", nomeE,
									distance[nodo - 1][nodo].nome, valore2string(t)));
						}
					}
				}
			}
		}
		return sbT.toString() + "\n" + sbE.toString() + "\n" + sbC.toString() + "\n";
	}

	/**
	 * Costruisce la stringa che rappresenta il contenuto della matrice.
	 * 
	 * @return stringa che rappresenta la matrice
	 */
	@Override
	public String toString() {
		return DistanceGraph.print(distance);
	}

	/**
	 * Permette di settare una coppia di link di distanza per rappresentare un task nel grafo delle
	 * distanze. Si assume che nel grafo delle distanze i due nodi (nodo che rappresenta inizio task
	 * e nodo che rappresenta fine task) siano sempre consecutivi.
	 * 
	 * @param nodoInizioTask nodo dove inizia il task
	 * @param nome del task
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setTask(int nodoInizioTask, String nome, int lowerBound, int upperBound) {
		// Setto link upper bound
		distance[nodoInizioTask][nodoInizioTask + 1].set(nome, Edge.Type.task, upperBound, false);
		// Setto link lower bound
		distance[nodoInizioTask + 1][nodoInizioTask].set(nome + "(l.b.)", Edge.Type.task, -lowerBound, true);
	}

	/**
	 * Permette di settare una coppia di link di distanza per rappresentare un connettore nel grafo
	 * delle distanze. Si assume che nel grafo delle distanze i due nodi (nodo che rappresenta
	 * inizio connettore e nodo che rappresenta fine connettore) siano sempre consecutivi.
	 * 
	 * @param nodoInizioConnettore nodo dove inizia il connettore
	 * @param nome del task
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setConnector(int nodoInizioConnettore, String nome, int lowerBound, int upperBound) {
		// Setto link upper bound
		distance[nodoInizioConnettore][nodoInizioConnettore + 1].set(nome, Edge.Type.connector, upperBound, false);
		// Setto link lower bound
		distance[nodoInizioConnettore + 1][nodoInizioConnettore].set(nome, Edge.Type.connector, -lowerBound, true);
	}

	/**
	 * Permette di settare una coppia di link di distanza per rappresentare un edge del workflow nel
	 * grafo delle distanze.
	 * 
	 * @param nodoInizioEdge nodo dove inizia l'edge
	 * @param nodoFineEdge nodo dove finisce l'edge
	 * @param nome dell'edge
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setEdge(int nodoInizioEdge, int nodoFineEdge, String nome, int lowerBound, int upperBound) {
		// Setto link upper bound
		distance[nodoInizioEdge][nodoFineEdge].set(nome, Edge.Type.edge, upperBound, false);
		// Setto link lower bound
		distance[nodoFineEdge][nodoInizioEdge].set(nome, Edge.Type.edge, -lowerBound, true);
	}

	/**
	 * Permette di settare una coppia di link di distanza per rappresentare un constraint del
	 * workflow nel grafo delle distanze.
	 * 
	 * @param nodoInizio nodo dove inizia il constraint
	 * @param nodoFine nodo dove finisce il constraint
	 * @param nome dell'edge
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setConstraint(int nodoInizio, int nodoFine, String nome, int lowerBound, int upperBound) {
		// Setto link upper bound
		distance[nodoInizio][nodoFine].set(nome, Edge.Type.constraint, upperBound, false);
		// Setto link lower bound
		distance[nodoFine][nodoInizio].set(nome + "(l.b.)", Edge.Type.constraint, -lowerBound, true);
	}

	/**
	 * Permette di settare una coppia di link di distanza per rappresentare un vincolo (internal)
	 * per rappresentare la sincronizzazione sui AND-join.
	 * 
	 * @param nodoInizio nodo dove inizia il constraint
	 * @param nodoFine nodo dove finisce il constraint
	 * @param nome dell'edge
	 * @param lowerBound
	 * @param upperBound
	 */
	public void setInternal(int nodoInizio, int nodoFine, String nome, int lowerBound, int upperBound) {
		// Setto link upper bound
		distance[nodoInizio][nodoFine].set(nome, Edge.Type.internal, upperBound, false);
		// Setto link lower bound
		distance[nodoFine][nodoInizio].set(nome + "(l.b.)", Edge.Type.internal, -lowerBound, true);
	}

	/**
	 * Restringe il valore di arco che segue un task (contingent edge precedes a requirment one) ed
	 * è sottoposto a un vincolo con la fine del'edge. L'edge inizia sul nodo di fine task.<br>
	 * Se l'intervallo presente tra inizio task e fine task non è un intervallo con lower bound
	 * positivo e upper bound maggiore, allora significa che l'arco incide sull'inizio del task,
	 * quindi non si esegue nulla (caso gestito dal metodo follow). <br>
	 * Questo caso è gestito anche dall'algoritmo di Floyd&Warshall. Qui si riporta per completezza
	 * e per poter anche analizzare, tramite i log, i casi in cui viene applicato.
	 * 
	 * @param startT indice nodo inizio task
	 * @param endT indice nodo fine task
	 * @param endE indice nodo fine arco
	 */
	public void precede(int startT, int endT, int endE) {
		int u, u1, v, v1, x, y, p, q;

		// Estremi del range del task
		x = -distance[endT][startT].valore;
		y = distance[startT][endT].valore;
		// estremi dell'edge
		u = -distance[endE][endT].valore;
		v = distance[endT][endE].valore;
		// Se (starT, endT) non sono un task o gli estremi non costituiscono un intervallo corretto
		// o gli estremi dell'edge non costituiscono un range corretto, non si fa nulla
		if ((distance[startT][endT].tipo != Edge.Type.task) || (x < 0 || y < x) || (u < 0 || v < u)) {
			return;
		}

		LOG.finest("Probabile caso precede task [" + startT + ", " + endT + "] e arco (" + endT + ", " + (endE) + ")");

		// estremi vincolo
		p = -distance[endE][startT].valore;
		q = distance[startT][endE].valore;

		LOG.finest("Valori esistenti [u,v]: [" + u + ", " + v + "]");

		// nuovi valori per l'edge
		u1 = p - y;
		v1 = q - x;

		LOG.finest("Valori calcolati [u,v]: [" + u1 + ", " + v1 + "]");

		boolean restringimento = false;
		if (u1 > u) {
			distance[endE][endT].valore = -u1;
			restringimento = true;
		}

		if (v1 < v) {
			distance[endT][endE].valore = v1;
			restringimento = true;
		}

		if (!restringimento)
			LOG.finest("Nuovi [u,v] NON registrati.");
		else
			LOG.finest("Nuovi [u,v] registrati.");
	}

	/**
	 * Esegue il check della consistenza su un path rappresentato dall'oggetto
	 * corrente.
	 * 
	 * @param stampaMatrice stampa valori matrice all'inizio e alla fine.
	 * @return true se il path è dinamicamente controllabile, falso altrimenti.
	 */
	public boolean checkConsistencyPath(boolean stampaMatrice) {
	
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Matrice delle distanze inizio:\n" + DistanceGraph.print(distance));
			LOG.finer("Range iniziali:\n" + this.printRange(true));
		}
	
		LOG.finer("Minimizzo matrice con Floyd & Warshall");
		this.FloydWarshall();
		if (this.areNegativeCycles()) {
			LOG.info("Matrice con cicli negativi!\n");
			LOG.finer("Matrice:\n" + print(this.distance));
			return false;
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.finer("Matrice dopo minimizzazione:\n" + print(this.distance));
			LOG.finer("Range:\n" + this.printRange(true));
		}
		return true;
	}
}