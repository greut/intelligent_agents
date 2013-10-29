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

import template.heuristic.*;

/**
 * An optimal planner for one vehicle.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
 */
public class DeliberativeAgent implements DeliberativeBehavior {

    enum Algorithm { BFS, ASTAR }
    enum Heuristic { BALANCE, DISTANCE, POTENTIAL }

    /* Environment */
    Topology topology;
    TaskDistribution td;

    /* the properties of the agent */
    Agent agent;
    int capacity;
    int costPerKm;

    /* the planning class */
    Algorithm algorithm;
    Heuristic heuristic;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        this.topology = topology;
        this.td = td;
        this.agent = agent;

        // initialize the planner
        capacity = agent.vehicles().get(0).capacity();
        costPerKm = agent.vehicles().get(0).costPerKm();
        String algorithmName = agent.readProperty("algorithm", String.class, "bfs");
        String heuristicName = agent.readProperty("heuristic", String.class, "distance");

        // Throws IllegalArgumentException if they are unknown
        algorithm = Algorithm.valueOf(algorithmName.trim().replace("-", "").toUpperCase());
        heuristic = Heuristic.valueOf(heuristicName.trim().replace('-', '_').toUpperCase());
    }

    @Override
    public Plan plan(Vehicle vehicle, TaskSet tasks) {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        StateComparator g;
        SearchAlgorithm algo;

        switch(heuristic) {
            case BALANCE:
                // Very fast but not optimal in the A* case
                g = new Balance();
                break;
            case POTENTIAL:
                g = new Potential();
                break;
            case DISTANCE:
            default:
                // Optimal but too slow (behaves like BFS)
                g = new Distance();
                break;
        }

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
        System.err.println(vehicle.name() + " (" + algo + "+" + g + ")");
        System.err.println("Tasks:");
        System.err.println(new String(new char[80]).replace('\0', '-'));
        for (Task t : tasks) {
            System.err.println(" " + t);
        }

        long startTime = System.nanoTime();
        State initial = new State(current, capacity, costPerKm, tasks, vehicle.getCurrentTasks(), g);
        State best = algo.search(initial);
        long duration = System.nanoTime() - startTime;

        // Build plan
        Iterator<Action> iter = best.planIterator();
        while (iter.hasNext()) {
            plan.append(iter.next());
        }

        System.err.println(String.format("Time spent: %.3fs", duration / 1000000000.));

        return plan;
    }

    @Override
    public void planCancelled(TaskSet cancelledTasks) {
        // pass
    }
}
