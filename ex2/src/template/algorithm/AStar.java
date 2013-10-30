package template.algorithm;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

import template.State;
import template.Step;


/**
 * A* (A-star) implementation.
 *
 * @see https://en.wikipedia.org/wiki/A*_search_algorithm
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
 */
public class AStar implements Search {
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

            q.addAll(curr.nextStates());
            statesExplored++;
        }

        // stats
        //statesDiscarded = q.size() - 1;
        //System.err.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + best.getDepth());
        //System.err.println(best);
        return best;
    }
}
