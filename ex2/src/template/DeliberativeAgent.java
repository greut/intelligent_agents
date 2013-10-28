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
        synchronized(System.out) {
            System.out.println("--------------");
            System.out.println("Vehicle:\t\t" + vehicle.name());
            System.out.println("Tasks:\t\t" + tasks);
            System.out.println("Current Tasks:\t" + vehicle.getCurrentTasks());
            System.out.println("--------------");
        }
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        Heuristic g = new DistanceHeuristic();
        State initial = new State(current, capacity, costPerKm, tasks, vehicle.getCurrentTasks(), g);
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
        //System.err.println("Tasks:");
        //System.err.println(new String(new char[80]).replace('\0', '-'));
        for (Task t : tasks) {
        //    System.err.println(" " + t);
        }
        //System.err.println("Best plan for: " + algo + " + " + g + ":");
        //System.err.println(new String(new char[80]).replace('\0', '-'));

        State goal = algo.search(initial);

        // Build plan
        Iterator<Action> iter = goal.planIterator();
        //System.err.println(" " + current);
        while (iter.hasNext()) {
            Action action = iter.next();
        //    System.err.println(" " + action);
            plan.append(action);
        }

        return plan;
    }

    @Override
    public void planCancelled(TaskSet cancelledTasks) {
        // pass
    }
}
