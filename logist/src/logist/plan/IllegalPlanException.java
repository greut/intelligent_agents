package logist.plan;

/**
 * An exception that is thrown when a plan violates a constraint.
 * 
 * <p>
 * A detailed description of the problem can be obtained from the
 * <tt>toString</tt> method.
 * 
 * @author Robin Steiger
 */
public class IllegalPlanException extends RuntimeException {
	private static final long serialVersionUID = 5688061061983363127L;

	private final String[] message;

	/**
	 * Creates an IllegalPlanException
	 * 
	 * @param message
	 *            A detailed description of the problem
	 */
	public IllegalPlanException(String[] message) {
		super(message[0]);
		this.message = message;
	}

	/**
	 * A string that describes the problem. The description may span multiple
	 * lines.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(message[0]);

		for (int i = 1; i < message.length; i++) {
			builder.append('\n');
			builder.append(message[i]);
		}
		return builder.toString();
	}

}
