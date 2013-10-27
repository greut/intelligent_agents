package template;

public class BalanceHeuristic implements Heuristic {
    @Override
    public String toString() {
        return "Balance";
    }

    public int compare(State a, State b) {
        return Double.compare(a.getBalance(), b.getBalance());
    }
}
