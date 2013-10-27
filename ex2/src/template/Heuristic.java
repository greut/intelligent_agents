package template;


/**
 * Heuristic interface.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public interface Heuristic {
    /**
     * How two states are compared.
     *
     * @param a first state
     * @param b second state
     * @see java.util.Comparable
     */
    public int compare(State a, State b);
}
