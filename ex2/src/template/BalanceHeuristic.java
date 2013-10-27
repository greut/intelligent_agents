package template;

public class BalanceHeuristic implements Heuristic {
    public int compare(State a, State b) {
        return Double.compare(b.getBalance(), a.getBalance());
    }
}
