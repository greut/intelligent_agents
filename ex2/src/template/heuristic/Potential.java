package template.heuristic;

import template.State;


/**
 * A combination of the distance so far with the money loaded.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Potential implements StateComparator {
    @Override
    public String toString() {
        return "Potential";
    }

    public int compare(State a, State b) {
        double aValue = a.getDistance() + a.getDistanceToGoal();
        double bValue = b.getDistance() + b.getDistanceToGoal();
        return Double.compare(aValue, bValue);
    }
}
