/**
 *
 */
package it.univr.di.cstnu.graph;

/**
 * Root class for representing edges in it.univr.di.cstnu package.
 *
 * @author posenato
 * @version $Id: $Id
 */
public interface Edge extends Component {

	/**
	 * Possible types of an edge.
	 *
	 * @author posenato
	 */
	public static enum ConstraintType {
		/**
		 * The edge represents a user constraint
		 */
		constraint,

		/**
		 * The edge represent a contingent constraint.
		 */
		contingent,

		/**
		 * The edge represents a constraint derived by the controllability check algorithm.
		 */
		derived,

		/**
		 * The edge represents an internal one used to represent high level construct as a WORKFLOW OR JOIN.
		 */
		internal,
		/**
		 * The edge represents an execution precedence between two nodes one.
		 */
		normal,
		/**
		 * The edge represents an internal one used by qloop finder
		 */
		qloopFinder
	}

	/**
	 * @return the type
	 */
	public ConstraintType getConstraintType();

	/**
	 * @param e the other edge
	 * @return true if it has the same values.
	 */
	public boolean hasSameValues(Edge e);

	/**
	 * @return true if the constraint is a contingent one.
	 */
	public default boolean isContingentEdge() {
		return this.getConstraintType() == ConstraintType.contingent;
	}

	/**
	 * This method is inappropriate here, but it helps to speed up the code.
	 * 
	 * @return true if the edge is CSTN edge
	 */
	public boolean isCSTNEdge();

	/**
	 * This method is inappropriate here, but it helps to speed up the code.
	 * 
	 * @return true if the edge is CSTNPSU edge
	 */
	public boolean isCSTNPSUEdge();

	/**
	 * This method is inappropriate here, but it helps to speed up the code.
	 * 
	 * @return true if the edge is CSTNU edge
	 */
	public boolean isCSTNUEdge();

	/**
	 * @return true is it does not contain any values
	 */
	public boolean isEmpty();

	/**
	 * @return true if the edge is a normal edge or similar (it is not contingent).
	 */
	public default boolean isRequirementEdge() {
		return !isContingentEdge();
	}

	/**
	 * This method is inappropriate here, but it helps to speed up the code.
	 * 
	 * @return true if the edge is STN edge
	 */
	public boolean isSTNEdge();

	/**
	 * This method is inappropriate here, but it helps to speed up the code.
	 * 
	 * @return true if the edge is STNU edge
	 */
	public boolean isSTNUEdge();

	/**
	 * Factory
	 * 
	 * @return an object of type Edge.
	 */
	public Edge newInstance();

	/**
	 * Any super-interfaces/implementing classes should assure that such method has Edge edge as argument!
	 * 
	 * @param edge an object to clone.
	 * @return an object of type Edge.
	 */
	public Edge newInstance(Edge edge);

	/**
	 * Factory
	 * 
	 * @param name of the edge
	 * @return an object of type Edge.
	 */
	public Edge newInstance(String name);

	/**
	 * Setter for the field <code>type</code>.
	 *
	 * @param type the type to set
	 */
	public void setConstraintType(final ConstraintType type);

	/**
	 * A copy by reference of internal structure of edge e.
	 *
	 * @param e edge to clone. If null, it does nothing.
	 */
	public void takeIn(Edge e);


}
