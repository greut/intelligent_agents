package logist.agent;

import logist.Measures;

public interface AgentStatistics extends Comparable<AgentStatistics> {

    /** The name of the agent */
    public abstract String name();

    /**
     * The total distance (in units) traveled by all vehicles.
     * 
     * @see Measures
     */
    public abstract long getTotalDistanceUnits();

    /** The total distance (in km) traveled by all vehicles */
    public abstract double getTotalDistance();

    /** The total cost of all vehicles */
    public abstract long getTotalCost();

    /** The total reward for all delivered tasks */
    public abstract long getTotalReward();

    /** The total profit of the agent, calculated as (total reward - total cost) */
    public abstract long getTotalProfit();

    /** The total number of tasks that are handled by this agent */
    public abstract int getTotalTasks();

}