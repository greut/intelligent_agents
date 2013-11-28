package logist;

/**
 * An exception that is thrown when an unrecoverable error occurs that forces
 * the platform to terminate.
 * 
 * @author Robin Steiger
 */
public class LogistException extends RuntimeException {
    private static final long serialVersionUID = 7882864513870769951L;

    /**
     * A platform exception that is caused by another exception.
     * 
     * @param message
     *            the error message
     * @param cause
     *            the cause
     */
    public LogistException(String message, Throwable cause) {
        super(message + ": " + cause.getMessage(), cause);
    }

    /**
     * A platform exception that describes the encountered problem.
     * 
     * @param message
     *            the error message
     */
    public LogistException(String message) {
        super(message);
    }
}
