package logist.behavior;

import logist.agent.Agent;
import logist.plan.Action;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;

/**
 * The behavior of a state-based reactive agent.
 * 
 * @author Robin Steiger
 */
public interface ReactiveBehavior {

    /**
     * The setup method is called exactly once, before the simulation starts and
     * before any other method is called.
     * <p>
     * The <tt>agent</tt> argument allows you to access important information
     * about your agent, most notably:
     * <ul>
     * <li>{@link agent.Agent#vehicles()} the list of vehicles controlled
     * by the agent</li>
     * <li>{@link agent.Agent#getTasks()} the set of tasks that the agent
     * has accepted but not yet delivered</li>
     * </ul>
     * 
     * @param topology
     *            The topology of the simulation
     * @param distribution
     *            The task distribution of the simulation
     * @param agent
     *            The properties of the agent
     */
    void setup(Topology topology, TaskDistribution distribution, Agent agent);

    /**
     * This method is called whenever the agent arrives in a new city and is not
     * carrying a task. <br>
     * The agent can see one available task in the city and decide whether or
     * not to accept the task.
     * <ul>
     * <li>If the agent decides to pick up the task, the platform will take over
     * the control of the vehicle and deliver the task on the shortest path. The
     * next time this method is called the vehicle will have dropped the task at
     * its destination.
     * <li>
     * <li>If the agent decides to refuse the task, it chooses a neighboring
     * city to move to. A refused task disappears and will no longer be
     * available the next time the agent visits this city.
     * <li>
     * </ul>
     * 
     * @param vehicle
     *            The vehicle that the agent is controlling
     * @param availableTask
     *            the proposed task or <code>null</code> if there is no task
     * @return A {@link logist.plan.Action.Pickup} action if the task was
     *         accepted, or a {@link logist.plan.Action.Move} action if the task
     *         was refused.
     */
    Action act(Vehicle vehicle, Task availableTask);

}
