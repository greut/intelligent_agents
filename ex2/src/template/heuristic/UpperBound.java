package template.heuristic;

import template.State;


/**
 * A combination of the distance so far with the upper bound of remaining
 * distance.
 *
 * Note: This heuristic is not optimal because the `h` function over estimate
 *       the remaining cost. And it is not monotone.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class UpperBound implements StateComparator {
    @Override
    public String toString() {
        return "UpperBound";
    }

    public int compare(State a, State b) {
        // g -> distance
        double aValue = a.getDistance();
        double bValue = b.getDistance();
        // h -> upper bound of the remaining distance
        aValue += a.getMaxDistanceToGoal();
        bValue += b.getMaxDistanceToGoal();
        return Double.compare(aValue, bValue);
    }
}
