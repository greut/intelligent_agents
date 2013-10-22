package template;

import java.util.AbstractSet;
import java.util.Deque;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.DeliberativeBehavior;
import logist.plan.Action;
import logist.plan.Plan;
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
            // ...
            plan = doPlan(vehicle, tasks);
            break;
        case BFS:
            // ...
            plan = doPlan(vehicle, tasks);
            break;
        default:
            throw new AssertionError("Should not happen.");
        }
        return plan;
    }

    private Plan doPlan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        State initial = new State(current, capacity, costPerKm, tasks);

        // Dirty BFS
        LinkedList<State> all = new LinkedList<State>();
        Deque<State> q = new LinkedList<State>();
        State curr = null;
        all.add(initial);
        q.add(initial);
        while(!q.isEmpty()) {
            curr = q.removeFirst();
            if (curr.isFinal()) {
                break;
            }
            Queue<Step> steps = curr.steps();
            for(Step s = steps.poll(); s != null; s = steps.poll()) {
                State next = curr.apply(s);
                boolean found = false;
                for (Iterator<State> i = all.iterator(); i.hasNext() && !found;) {
                    if (i.next().equals(next)) {
                        found = true;
                    }
                }

                if (!found) {
                    all.add(next);
                    q.add(next);
                }
            }
        }
        // Tracing back the trail of actions that made this result possible.
        LinkedList<Action> actions = new LinkedList<Action>();
        while (!curr.equals(initial)) {
            actions.add(curr.getPreviousAction());
            curr = curr.getParent();
        }
        // Build plan
        Plan plan = new Plan(current);
        Iterator<Action> iter = actions.descendingIterator();
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
