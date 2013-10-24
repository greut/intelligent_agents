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
        Plan plan;

        // Compute the plan with the selected algorithm.
        switch (algorithm) {
        case ASTAR:
            plan = doPlanAstar(vehicle, tasks);
            break;
        case BFS:
            plan = doPlanBFS(vehicle, tasks);
            break;
        default:
            throw new AssertionError("Should not happen.");
        }
        return plan;
    }

    private Plan doPlanBFS(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        State initial = new State(current, capacity, costPerKm, tasks);

        for (Task t : tasks) {
            System.err.println(t);
        }

        // Dirty BFS
        Deque<State> q = new LinkedList<State>();
        Queue<State> finals = new PriorityQueue<State>();
        int maxDepth = Integer.MAX_VALUE;
        q.add(initial);
        while(!q.isEmpty()) {
            State curr = q.removeFirst();
            if (curr.isFinal()) {
                maxDepth = curr.getDepth();
                finals.add(curr);
            }
            if (curr.getDepth() < maxDepth) {
                Queue<Step> steps = curr.steps();
                for(Step s = steps.poll(); s != null; s = steps.poll()) {
                    State next = curr.apply(s);
                    boolean found = false;
                    if (!next.hasLoop()) {
                        q.add(next);
                    }
                }
            }
        }

        // Build plan
        Plan plan = new Plan(current);
        State best = finals.peek();
        System.out.println(best.getBalance());
        Iterator<Action> iter = best.planIterator();
        while(iter.hasNext()) {
            plan.append(iter.next());
        }

        return plan;
    }

    private Plan doPlanAstar(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        State initial = new State(current, capacity, costPerKm, tasks);

        for(Task t: tasks) {
            System.err.println(t);
        }

        // A-star
        PriorityQueue<State> q = new PriorityQueue<State>();
        HashMap<State,Double> c = new HashMap<State,Double>();
        State curr = null;
        q.add(initial);
        while(!q.isEmpty()) {
            curr = q.poll();
            if(!q.isEmpty())
                System.err.println(curr + " ||||| " + q.peek());
            if(curr.isFinal()) {
                break;
            }

            Double prevbalance = c.get(curr); // if state already in c compare balance
            if(prevbalance == null || curr.getBalance() >= prevbalance) {
                c.put(curr, curr.getBalance());
                Queue<Step> steps = curr.steps();
                for(Step s = steps.poll(); s != null; s = steps.poll()) {
                    State next = curr.apply(s);
                    q.add(next);
                }
            }
        }

        // Build plan
        Plan plan = new Plan(current);
        Iterator<Action> iter = curr.planIterator();
        while(iter.hasNext()) {
            plan.append(iter.next());
        }

        return plan;
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
