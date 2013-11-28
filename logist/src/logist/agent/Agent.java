package logist.agent;

import java.util.List;

import logist.simulation.Vehicle;
import logist.task.TaskSet;

/**
 * Collection of useful information about an agent.
 * <p>
 * A <tt>get</tt>-prefix indicates that the return value of the method may
 * change over time. All other methods return constant values.
 * 
 * @author Robin Steiger
 */
public interface Agent extends AgentStatistics {

    /**
     * A unique id in the range <tt>[0,numAgents)</tt>. The id of each agent
     * corresponds to the index of its bid in the table received by
     * {@link behavior.AuctionBehavior#auctionResult(Task,int,Long[])}
     */
    public abstract int id();

    /** The vehicles controlled by the agent */
    public abstract List<Vehicle> vehicles();

    /**
     * The tasks that the agent has accepted, but not yet picked up and
     * delivered.
     */
    public abstract TaskSet getTasks();

    /**
     * Reads an agent property from the agents.xml file and converts it to the
     * requested type. Supported types are
     * <tt>String, Boolean, Integer, Long, Color, Double, File, ClassLoader</tt>
     * .
     * 
     * If the property does not exist, the default value is returned (if
     * non-null) or a RuntimeException is thrown (if default is null).
     * 
     * @param <T>
     *            Type of the property
     * @param paramName
     *            Name of the property
     * @param clazz
     *            Type of the property
     * @param default_
     *            A default value, or null
     * 
     * @throws RuntimeException
     *             (1) if the property does not have the correct type or (2) if
     *             the property does not exist and the default value is null.
     * 
     * @return The agent property
     */
    public <T> T readProperty(String paramName, Class<T> clazz, T default_);
}
