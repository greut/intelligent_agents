package logist.behavior;

import java.util.List;

import logist.agent.Agent;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * The behavior of a centralized agent that creates plans for all vehicles.
 * 
 * @author Robin Steiger
 */
public interface CentralizedBehavior {

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
	 * Computes the joint plan for several vehicles.
	 * <br>
	 * The plans for each vehicle are returned as a list, in the same order than the vehicles.
	 * The agent can assume that no vehicle is carrying a task.
	 * 
	 * @param vehicles The list of vehicles
	 * @param tasks The list of tasks to be handled
	 * @return The plans for each vehicle
	 */
	List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks);

}
