package template.algorithm;

import java.util.Queue;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.LinkedList;

import template.State;
import template.Step;

/**
 * Breadth First Search (BFS) implementation
 *
 * @see https://en.wikipedia.org/wiki/Breadth-first_search
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
 */
public class BreadthFirstSearch implements Search {
    @Override
    public String toString() {
        return "BFS";
    }

    public State search(State initial) {
        Deque<State> q = new LinkedList<State>();
        Queue<State> finals = new PriorityQueue<State>();
        int maxDepth = Integer.MAX_VALUE;
        // stats
        int statesExplored = 0;
        int statesDiscarded = 0;
        q.add(initial);
        while (!q.isEmpty()) {
            State curr = q.removeFirst();
            if (curr.isFinal()) {
                maxDepth = curr.getDepth();
                finals.add(curr);
            }
            if (curr.getDepth() < maxDepth) {
                for (State next : curr.nextStates()) {
                    if (!next.hasLoop()) {
                        q.add(next);
                    } else {
                        statesDiscarded++;
                    }
                }
            }
            statesExplored++;
        }

        // Debug
        //System.err.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + maxDepth);
        //System.err.println(finals.peek());
        return finals.peek();
    }
}
