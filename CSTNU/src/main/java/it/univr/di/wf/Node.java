/**
 * 
 */
package it.univr.di.wf;

/**
 * @author posenato
 * 
 */
public class Node {

	/**
	 * Tipo enumerativo del nodo
	 * @author posenato
	 */
	static enum Type {
		/**
		 * 
		 */
		edge, /**
		 * 
		 */
		connector, /**
		 * 
		 */
		task
	};

	/**
	 * Nome del nodo
	 */
	String key;
	/**
	 * Tipo del nodo
	 */
	Type type;
	/**
	 * Design lower bound
	 */
	int lowerBound;
	/**
	 * Design upper bound
	 */
	int upperBound;
	/**
	 * Actual lower bound determined by the context
	 */
	int actualLower;
	/**
	 * Actual upper bound determined by the context
	 */
	int actualUpper;

	/**
	 * A default constructor is not available
	 */
	@SuppressWarnings("unused")
	private Node() {
	};

	/**
	 * 
	 * @param k
	 * @param t
	 * @param lower
	 * @param upper
	 */
	Node(String k, Type t, int lower, int upper) {
		if (k == null)
			throw new NullPointerException("La chiave non può essere nulla.");

		key = k;
		type = t;
		lowerBound = lower;
		upperBound = upper;
	}

	/**
	 * Costruisce un nodo con i valori di default per il lower e l'upper bound.
	 *
	 * @param k
	 * @param t
	 */
	Node(String k, Type t) {
		this(k, t, DistanceGraph.defaultLB, DistanceGraph.defaultUB);
	}

	/**
	 * Due elementi sono uguali se hanno key uguale.
	 * @param n nodo da confrontare
	 * @return vero se hanno key uguale.
	 */
	boolean equals(Node n) {
		return key.equals(n.key);
	}
}
