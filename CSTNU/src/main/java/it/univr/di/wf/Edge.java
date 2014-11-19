/**
 * 
 */
package it.univr.di.wf;

import java.util.Map;

/**
 * Struttura per memorizzare il tipo di un edge ed eventuali vincoli wait presenti.
 * 
 * @author posenato
 */
public class Edge {
	/**
	 * Tipi possibili di edge
	 * 
	 * @author posenato
	 */
	enum Type {
		/**
		 * è un edge interno al connettore AND
		 */
		internal,
		/**
		 * è un edge derivato da altri calcoli
		 */
		derived,
		/**
		 * è un edge con delay definito dall'utente
		 */
		edge,
		/**
		 * è un edge che connette il nodo inizio e nodo fine di un connettore
		 */
		connector,
		/**
		 * è un vincolo intertask/connector
		 */
		constraint,
		/**
		 * è un edge che connette il nodo inizio e nodo fine di un task.
		 */
		task
	}

	/**
	 * Eventuale nome
	 */
	String nome;

	/**
	 * tipo dell'edge
	 */
	Type tipo;

	/**
	 * Valore dell'edge.
	 */
	int valore;
	
	/**
	 * Indica se il {@link #valore} rappresenta il lower bound o l'upper bound tra il nodo di partenza e il nodo destinazione.
	 * 
	 * Quando è true, {@link #valore} è il lower bound invertito di segno della distanza temporale tra (nodoDestinazione,nodoPartenza). 
	 * Altrimenti, {@link #valore} rappresenta upper bound per la distanza temporale tra (nodoPartenza, nodoDestinazione).
	 */
	boolean isLowerBound;
	
	/**
	 * Mappa che mantiene eventuali vincoli di wait sull'edge.<br>
	 * La struttura deve essere: <indiceNodoFineTask, valore di wait>
	 */
	Map<Integer, Integer> wait;

	/**
	 * Default constructor.
	 * Il nome è stringa vuota, il tipo è 'derived', il valore è {@link DistanceGraph#defaultUB} e il wait è nullo.
	 */
	public Edge() {
		this("", Type.derived, DistanceGraph.defaultUB, false);
	}

	/**
	 * Il nome è stringa vuota, il tipo è 'derived' e il wait è nullo.
	 * @param v valore del edge
	 * @param isLowerBound true if v is a lower bound
	 */
	public Edge(int v, boolean isLowerBound) {
		this("", Type.derived, v, isLowerBound);
	}

	
	/**
	 * @param n
	 * @param t
	 * @param v
	 * @param isLowerBound true if v is a lower bound
	 */
	public Edge(String n, Type t, int v, boolean isLowerBound) {
		nome = n;
		tipo = t;
		valore = v;
		wait = null;
		this.isLowerBound = isLowerBound;
	}

	
	/**
	 * Assegna i valori all'edge
	 * @param n nome dell'edge
	 * @param t tipo
	 * @param v valore
	 * @param isLowerBound true if v is a lower bound
	 */
	public void set(String n, Type t, int v, boolean isLowerBound) {
		nome = n;
		tipo = t;
		valore = v;
		this.isLowerBound = isLowerBound;
	}

	
	
	/**
	 * Stampa il valore.
	 */
	public String toString() {
		return Integer.toString(valore);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Edge e = (Edge) super.clone();
		return e;
	}
	
	/**
	 * 
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Edge)) return false;
		Edge e = (Edge) o;
		return nome.equals(e.nome) && valore == e.valore && tipo == e.tipo && isLowerBound==e.isLowerBound;
	}
	
}
