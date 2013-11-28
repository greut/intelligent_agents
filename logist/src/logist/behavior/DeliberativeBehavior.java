package logist.behavior;

import logist.agent.Agent;
import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * The behavior of a deliberative agent.
 * 
 * @author Robin Steiger
 */
public interface DeliberativeBehavior {

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
     * Computes the transportation plan for a vehicle. <br>
     * In a single agent system, the agent can assume that the vehicle is
     * carrying no tasks. In a multi-agent system this method might be called
     * again during the execution of a plan (see
     * {@link behavior.DeliberativeBehavior#planCancelled(logist.task.TaskSet)}
     * ).
     * 
     * @param vehicle
     *            The vehicle that the agent is controlling
     * @param tasks
     *            The list of tasks to be handled
     * @return The plan for the vehicle
     */
    Plan plan(Vehicle vehicle, TaskSet tasks);

    /**
     * In a multi-agent system the plan of an agent might get 'stuck' in which
     * case this method is called followed by a call to
     * {@link behavior.DeliberativeBehavior#plan(logist.simulation.Vehicle, logist.task.TaskSet)}
     * . The argument carriedTasks is the set of task that the vehicle has
     * picked up but not yet delivered. These tasks have to be considered the
     * next time a plan is computed.
     * 
     * You can also use {@link logist.simulation.Vehicle#getCurrentTasks()} to
     * obtain the set of tasks that the vehicle is holding.
     * 
     * @param carriedTasks
     *            the tasks that the vehicle is holding
     */
    void planCancelled(TaskSet carriedTasks);
}
