package template.heuristic;

import template.State;


/**
 * A combination of the distance so far with the money loaded.
 *
 * Note: This heuristic is not optimal because the `h` function over estimate
 *       the remaining cost. And it is not monotone.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class LowerBound implements StateComparator {
    @Override
    public String toString() {
        return "LowerBound";
    }

    public int compare(State a, State b) {
        // g -> distance
        double aValue = a.getDistance();
        double bValue = b.getDistance();
        // h -> minimal distance to reach the final goal
        aValue += a.getMinDistanceToGoal();
        bValue += b.getMinDistanceToGoal();
        return Double.compare(aValue, bValue);
    }
}
