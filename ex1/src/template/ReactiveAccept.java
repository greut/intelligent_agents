package template;

import java.util.Random;

import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

/**
 * Sample agent that always accept the task given to its.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class ReactiveAccept implements ReactiveBehavior {

    private Random random;

    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        random = new Random();
    }

    public Action act(Vehicle vehicle, Task availableTask) {
        System.err.println("[Accept] " + (availableTask != null ? availableTask : ":-("));
        return availableTask == null ?
            new Action.Move(vehicle.getCurrentCity().randomNeighbor(random)) :
            new Action.Pickup(availableTask);
    }
}
