package template.heuristic;

import template.State;


/**
 * The best state is the one with makes the most money.
 *
 * Note: this heuristic is not optimal because there is no `g`. It is not
 *       monotone. This what is used for BFS though.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Balance implements StateComparator {
    @Override
    public String toString() {
        return "Balance";
    }

    public int compare(State a, State b) {
        // g -> 0
        // h -> money
        return Double.compare(b.getBalance(), a.getBalance());
    }
}
