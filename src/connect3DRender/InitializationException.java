package connect3DRender;

/**
 * Thrown to indicate that some aspect of the renderer failed to initialize.
 * For example if any libraries needed could not be found.
 * @author Benjamin
 *
 */
public final class InitializationException extends Exception {

	/**
	 * An InitializationException with the reason for the exception.
	 * @param msg
	 * 	The reason the exception occurred.
	 */
	public InitializationException(String msg) {super(msg);}
	/**
	 * An initialization Exception with no reason specified.
	 */
	public InitializationException() {}
	/**
	 * 
	 */
	private static final long serialVersionUID = 165489651L;

}
