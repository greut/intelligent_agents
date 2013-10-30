package template.heuristic;

import template.State;


/**
 * The best state is the one that travels the less.
 *
 * Assuming that rewards are constant.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Distance implements StateComparator {
    @Override
    public String toString() {
        return "Distance";
    }

    public int compare(State a, State b) {
        // g -> distance
        // h -> 0
        return Double.compare(a.getDistance(), b.getDistance());
    }
}
