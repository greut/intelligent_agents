package logist.behavior;

import java.util.List;

import logist.agent.Agent;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * The behavior of a decentralized agent that buys tasks through auction.
 * 
 * @author Robin Steiger
 */
public interface AuctionBehavior {

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
     * Asks an agent to offer a price for a task.
     * <p>
     * This method is called for each task that is auctioned. The agent should
     * return the amount of money it would like to receive for that task. If the
     * agent wins the auction the reward of the task will be set to the agent's
     * price and the agent receives the task.
     * 
     * @param task
     *            the task being auctioned
     * @return A bid for the task, or <tt>null</tt>
     * @see agent.Agent#id()
     */
    Long askPrice(Task task);

    /**
     * <tt>lastTask</tt> and <tt>lastWinner</tt> contain the results of the
     * previous auction.
     * 
     * @param lastTask
     *            the task that was auctioned
     * @param lastWinner
     *            the id of the agent who has won the previous auction
     * @param lastOffers
     *            the price offers of all agents from the previous auction,
     *            indexed by agent id. May contain <tt>null</tt> bids for agents
     *            that did not participate.
     * @see agent.Agent#id()
     */
    void auctionResult(Task lastTask, int lastWinner, Long[] lastOffers);

    /**
     * Computes the joint plan for several vehicles. <br>
     * The plans for each vehicle are returned as a list, in the same order than
     * the vehicles. The agent can assume that no vehicle is carrying a task.
     * 
     * @param vehicles
     *            The list of vehicles
     * @param tasks
     *            The list of tasks to be handled
     * @return The plans for each vehicle
     */
    List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks);

}
