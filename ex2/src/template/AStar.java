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
        State best = null;
        // stats
        int statesExplored = 0;
        int statesDiscarded = 0;
        q.add(initial);
        while (!q.isEmpty()) {
            State curr = q.poll();
            if (curr.isFinal()) {
                best = curr;
                break;
            }

            Queue<Step> steps = curr.steps();
            for (Step s = steps.poll(); s != null; s = steps.poll()) {
                State next = curr.apply(s);
                q.add(next);
            }
            statesExplored++;
        }

        statesDiscarded = q.size() - 1;

        // stats
        System.err.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + best.getDepth());
        System.err.println(best);
        return best;
    }
}
