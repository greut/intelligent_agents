package logist.history;

import java.io.IOException;

import logist.LogistException;

/**
 * An exception that is thrown by an {@link logist.history.XMLWriter}
 * 
 * @author Robin Steiger
 */
public class XMLWritingException extends LogistException {
	private static final long serialVersionUID = 6062694012148411235L;

	XMLWritingException(String message) {
		super(message);
	}

	XMLWritingException(IOException cause) {
		super("I/O problem", cause);
	}

}
