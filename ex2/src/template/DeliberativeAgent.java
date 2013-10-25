package template;

import java.util.HashMap;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * An optimal planner for one vehicle.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class DeliberativeAgent implements DeliberativeBehavior {

    enum Algorithm { BFS, ASTAR }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;
    int costPerKm;

    /* the planning class */
    Algorithm algorithm;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        capacity = agent.vehicles().get(0).capacity();
        costPerKm = agent.vehicles().get(0).costPerKm();
        String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");

        // Throws IllegalArgumentException if algorithm is unknown
        algorithm = Algorithm.valueOf(algorithmName.toUpperCase());

        // ...
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        State initial = new State(current, capacity, costPerKm, tasks);
        State goal;

        switch (algorithm) {
            case ASTAR:
                goal = doAStarSearch(initial);
                break;
            case BFS:
            default:
                goal = doBreadthFirstSearch(initial);
                break;
        }

        // Debug information
        System.err.println("Tasks:");
        System.err.println("------");
        for (Task t : tasks) {
            System.err.println(" " + t);
        }
        System.err.println("Best plan:");
        System.err.println("----------");
        System.err.println(" " + current);
        // Build plan
        Iterator<Action> iter = goal.planIterator();
        while (iter.hasNext()) {
            Action action = iter.next();
            System.err.println(" " + action);
            plan.append(action);
        }

        return plan;
    }

    /**
     * Breadth First Search algorithm.
     *
     * @param initial the initial state of the agent.
     * @return the best final state found.
     */
    private State doBreadthFirstSearch(State initial) {
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
                Queue<Step> steps = curr.steps();
                for (Step s = steps.poll(); s != null; s = steps.poll()) {
                    State next = curr.apply(s);
                    boolean found = false;
                    if (!next.hasLoop()) {
                        q.add(next);
                    } else {
                        statesDiscarded++;
                    }
                }
            }
            statesExplored++;
        }

        State best = finals.peek();
        // stats
        System.out.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + maxDepth);
        System.out.println(best);
        return best;
    }

    /**
     * A-star algorithm.
     *
     * @param initial the initial state of the agent.
     * @return the best final state found.
     */
    private State doAStarSearch(State initial) {
        PriorityQueue<State> q = new PriorityQueue<State>();
        HashMap<State,Double> c = new HashMap<State,Double>();
        State curr = null;
        // stats
        int statesExplored = 0;
        int statesDiscarded = 0;
        q.add(initial);
        while (!q.isEmpty()) {
            curr = q.poll();
            if (!q.isEmpty())
                System.err.println(curr + " ||||| " + q.peek());
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
        System.out.println("states: " + statesDiscarded + "/" + statesExplored + " max-depth:" + curr.getDepth());
        System.out.println(curr);
        return curr;
    }

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }
}
