package logist.topology;

/**
 * An exception that is thrown if the topology is not valid.
 * 
 * @author Robin Steiger
 */
public class TopologyException extends RuntimeException {
    private static final long serialVersionUID = 85291480456085519L;

    public TopologyException(String message) {
        super(message);
    }
    
}
