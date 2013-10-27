package template;

public class DistanceHeuristic implements Heuristic {
    @Override
    public String toString() {
        return "Distance";
    }

    public int compare(State a, State b) {
        return Double.compare(a.getDistance(), b.getDistance());
    }
}
