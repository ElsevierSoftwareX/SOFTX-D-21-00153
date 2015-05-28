/**
 * 
 */
package it.univr.di.cstnu;

/**
 * <p>WellDefinitionException class.</p>
 *
 * @author posenato
 * @version $Id: $Id
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
	 * <p>Constructor for WellDefinitionException.</p>
	 */
	public WellDefinitionException() {
		super();
		type = null;
	}

	/**
	 * <p>Constructor for WellDefinitionException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 */
	public WellDefinitionException(String message) {
		super(message);
		type = null;
	}

	/**
	 * <p>Constructor for WellDefinitionException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param t a {@link it.univr.di.cstnu.WellDefinitionException.Type} object.
	 */
	public WellDefinitionException(String message, Type t) {
		super(message);
		type = t;
	}

	/**
	 * <p>Constructor for WellDefinitionException.</p>
	 *
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public WellDefinitionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Constructor for WellDefinitionException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 */
	public WellDefinitionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>Constructor for WellDefinitionException.</p>
	 *
	 * @param message a {@link java.lang.String} object.
	 * @param cause a {@link java.lang.Throwable} object.
	 * @param enableSuppression a boolean.
	 * @param writableStackTrace a boolean.
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
