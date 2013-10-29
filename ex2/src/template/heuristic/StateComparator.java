package template.heuristic;

import template.State;


/**
 * Heuristic interface.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public interface StateComparator {
    /**
     * How two states are compared.
     *
     * @param a first state
     * @param b second state
     * @see java.util.Comparable
     */
    public int compare(State a, State b);
}
