package logist.task;

import logist.topology.Topology.City;

/**
 * The probability, reward and weight distributions of {@link logist.task.Task}s.
 * Each round a new batch of tasks is generated according to a TaskDistribution.
 * 
 * @author Robin Steiger
 */
public interface TaskDistribution {

	/**
	 * Returns the probability that a task to <tt>to</tt> is available in city
	 * <tt>from</tt>. If <tt>to</tt> is <tt>null</tt> then the probability that
	 * there is no task available in city <tt>from</tt> is returned.
	 * 
	 * @param from
	 *            the source of the task
	 * @param to
	 *            the destination of the task, or <tt>null</tt>
	 */
	double probability(City from, City to);

	/**
	 * Returns the (expected) reward for a task between two cities.
	 * 
	 * @param from
	 *            the source of the task
	 * @param to
	 *            the destination of the task
	 */
	int reward(City from, City to);

	/**
	 * Returns the (expected) weight for a task between two cities.
	 * 
	 * @param from
	 *            the source of the task
	 * @param to
	 *            the destination of the task
	 */
	int weight(City from, City to);

}
