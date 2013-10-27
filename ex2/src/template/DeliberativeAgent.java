package template;

import java.util.Iterator;

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
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
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
        State initial = new State(current, capacity, costPerKm, tasks, new BalanceHeuristic());
        SearchAlgorithm algo;

        switch (algorithm) {
            case ASTAR:
                algo = new AStar();
                break;
            case BFS:
            default:
                algo = new BreadthFirstSearch();
                break;
        }

        // Debug information
        System.err.println("Tasks:");
        System.err.println(new String(new char[80]).replace('\0', '-'));
        for (Task t : tasks) {
            System.err.println(" " + t);
        }

        State goal = algo.search(initial);

        // Debug
        System.err.println("Best plan for " + algo + ":");
        System.err.println(new String(new char[80]).replace('\0', '-'));
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

    @Override
    public void planCancelled(TaskSet carriedTasks) {

        if (!carriedTasks.isEmpty()) {
            // This cannot happen for this simple agent, but typically
            // you will need to consider the carriedTasks when the next
            // plan is computed.
        }
    }
}
