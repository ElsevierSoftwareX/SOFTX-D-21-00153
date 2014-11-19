/**
 * 
 */
package it.univr.di.cstnu;

/**
 * @author posenato
 *
 */
public class WellDefinitionException extends Exception {

	/**
	 * @author posenato
	 *
	 */
	@SuppressWarnings("javadoc")
	enum Type {
		LabelInconsistent,
		LabelNotSubsumes,
		ObservationNodeDoesNotExist,
		ObservationNodeDoesNotOccurBefore
	};
	
	/**
	 * Type of exception.
	 */
	private Type type;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public WellDefinitionException() {
		super();
		type = null;
	}

	/**
	 * @param message
	 */
	public WellDefinitionException(String message) {
		super(message);
		type = null;
	}

	/**
	 * @param message
	 * @param t
	 */
	public WellDefinitionException(String message, Type t) {
		super(message);
		type = t;
	}

	/**
	 * @param cause
	 */
	public WellDefinitionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public WellDefinitionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public WellDefinitionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the subtype of exception.
	 */
	Type getType() {
		return type;
	}
}
