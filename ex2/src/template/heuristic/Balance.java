package template.heuristic;

import template.State;


/**
 * The best state is the one with makes the most money.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Balance implements StateComparator {
    @Override
    public String toString() {
        return "Balance";
    }

    public int compare(State a, State b) {
        return Double.compare(b.getBalance(), a.getBalance());
    }
}
