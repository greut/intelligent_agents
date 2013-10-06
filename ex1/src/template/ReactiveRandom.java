package template;

import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;

public class ReactiveRandom implements ReactiveBehavior {

    private static double DEFAULT_DISCOUNT = 0.95;
    private Random random;
    private double pPickup;

    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        double discount = (double) agent.readProperty("discount-factor",
                Double.class, DEFAULT_DISCOUNT);

        random = new Random();
        pPickup = discount;
    }

    public Action act(Vehicle vehicle, Task availableTask) {
        System.err.println("[Random] " + vehicle.getCurrentCity() + " -> " +
                (availableTask != null ? availableTask : ":-("));

        Action action;
        if (availableTask == null || random.nextDouble() > pPickup) {
            City currentCity = vehicle.getCurrentCity();
            action = new Move(currentCity.randomNeighbor(random));
        } else {
            action = new Pickup(availableTask);
        }
        return action;
    }
}
