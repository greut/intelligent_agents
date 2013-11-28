package logist.simulation;

import logist.plan.Action;
import logist.task.Task;

/**
 * A vehicle controller decides what actions each vehicle performs.
 * @author Robin Steiger
 */
public interface VehicleController {

    Action nextAction(int vid);

    void stuckAction(int vid, Action action);
    
    void notifyPickup(Task task);
    void notifyDelivery(Task task);

}
