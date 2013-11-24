package logist.task;

import java.util.List;

import logist.topology.Topology.City;

/**
 * A task represents the unit of work in the pick up and delivery problem.
 * 
 * <p>
 * Tasks are created in batches at the beginning of each round according to a
 * {@link logist.task.TaskDistribution}.
 * 
 * @author Robin Steiger
 */
public final class Task {

	/**
	 * A unique id of the task in the current round, in the range
	 * <tt>[0,numTasks)</tt>.
	 */
	public final int id;
	
	/** The pickup location */
	public final City pickupCity;
	
	/** The delivery location */
	public final City deliveryCity;
	
	/** The reward for this task */
	public final long reward;
	
	/** The weight of this task */
	public final int weight;

	/**
	 * For system use only. Creates a new task.
	 */
	public Task(int id, City source, City destination, long reward, int weight) {
		this.id = id;
		this.pickupCity = source;
		this.deliveryCity = destination;
		this.reward = reward;
		this.weight = weight;
	}

	// Reference equality should be enough

	// @Override
	// public boolean equals(Object that) {
	// return (that instanceof Task) && (this.id == ((Task) that).id);
	// }
	//
	// @Override
	// public int hashCode() {
	// return id;
	// }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("(Task ");
		builder.append(id);
		builder.append(", ");
		builder.append(weight);
		builder.append(" kg, ");
		builder.append(reward);
		builder.append(" CHF, ");
		builder.append(pickupCity);
		builder.append(" -> ");
		builder.append(deliveryCity);
		builder.append(")");
		return builder.toString();
	}

	/**
	 * The cities on the shortest path from the source of this task (excluding)
	 * to the destination (including).
	 * 
	 * @return The cities on the shortest path to deliver this task.
	 */
	public List<City> path() {
		return pickupCity.pathTo(deliveryCity);
	}

	/**
	 * The length of the shortest path from the source of this task
	 * to the destination.
	 */
	public double pathLength() {
		return pickupCity.distanceTo(deliveryCity);
	}
}
