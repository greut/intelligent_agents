package logist.simulation;

import logist.LogistSettings;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

/**
 * The context of a simulation.
 * 
 * @author Robin Steiger
 */
public interface Context {

    Topology getTopology();
    TaskDistribution getTaskDistribution();
    LogistSettings getSettings();

    void notifyPickup(Task task);
    void notifyDelivery(Task task);
    int[] countPickup();
    int[] countDelivery();

    void close();
}
