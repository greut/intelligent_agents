package template;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 * A* (A-star) implementation.
 *
 * @see https://en.wikipedia.org/wiki/A*_search_algorithm
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
 */
public class AStar implements SearchAlgorithm {
    @Override
    public String toString() {
        return "A*";
    }

    public State search(State initial) {
        PriorityQueue<State> q = new PriorityQueue<State>();
        HashMap<State,Double> c = new HashMap<State,Double>();
        State curr = null;
        // stats
        int statesExplored = 0;
        int statesDiscarded = 0;
        q.add(initial);
        while (!q.isEmpty()) {
            curr = q.poll();
            //if (!q.isEmpty())
            //    System.err.println(curr + " ||||| " + q.peek());
            if (curr.isFinal()) {
                break;
            }

            // if state already in c compare balance
            Double prevbalance = c.get(curr);
            if (prevbalance == null || curr.getBalance() >= prevbalance) {
                c.put(curr, curr.getBalance());
                Queue<Step> steps = curr.steps();
                for (Step s = steps.poll(); s != null; s = steps.poll()) {
                    State next = curr.apply(s);
                    q.add(next);
                }
            } else {
                statesDiscarded++;
            }
            statesExplored++;
        }

        // stats
        System.err.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + curr.getDepth());
        System.err.println(curr);
        return curr;
    }
}
