/**
 * 
 */
package it.univr.di.wf;

import it.univr.di.wf.Node.Type;

import java.util.Vector;

/**
 * @author posenato
 *
 */
public class WPath {
	
	/**
	 * Node building path
	 */
	Vector<Node> node;
	
	
	/**
	 * 
	 */
	WPath() {
		node = new Vector<Node>();
	}
	/**
	 * Aggiunge un nodo al path controllando che non sia già inserito e se occorre aggiungere archi di raccordo.
	 * Se il nodo è già inserito, il vecchio viene sostituito dal corrente.
	 * 
	 * @param n
	 * @return true se ha aggiunto, false se ha sostituito.
	 */
	boolean add(Node n) {
		int i = node.indexOf(n);
		if (i == -1) {
			node.add(n);
			if (n.type != Type.edge) {
				//È una attività
				//Devo verificare se c'è l'arco che la connette con quella precedente.
				//Se non c'è l'arco, inserisco uno di default.
				
			}
			return true;
		}
		node.setElementAt(n, i);
		return false;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
