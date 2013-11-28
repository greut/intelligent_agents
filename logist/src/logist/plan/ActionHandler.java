package logist.plan;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * Computes a result after a pattern matching on actions. 
 *  
 * @author Robin Steiger
 *
 * @param <T> The type of the computation of the handler
 */
public interface ActionHandler<T> {

    /**
     * A move pattern
     * @param city The target city
     */
    public abstract T moveTo(City city);
    
    /**
     * A pickup pattern
     * @param task The task being picked up
     */
    public abstract T pickup(Task task);

    /**
     * A delivery pattern
     * @param task The task being delivered
     */
    public abstract T deliver(Task task);
    
}
