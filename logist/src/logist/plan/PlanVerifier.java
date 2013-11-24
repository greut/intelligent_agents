package logist.plan;

//import logist.agent.VehicleInfo;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A verifier that can check various constraints on {@link Plan}s.
 * <p>
 * The set of constraints that are actually checked depends on the exercise
 * (i.e. type of agent).
 * 
 * @author Robin Steiger
 */
public class PlanVerifier implements ActionHandler<String[]> {

	private final Topology topology;
	private final TaskSet availableTasks;
	private final TaskSet finishedTasks;

	private int capacity;
	private City currentCity;
	private TaskSet currentTasks;

	/**
	 * Creates a plan verifier for a given topology and task set.
	 * 
	 * @param topology
	 *            The topology
	 * @param availableTasks
	 *            The set of tasks available for pickup
	 */
	public PlanVerifier(Topology topology, TaskSet availableTasks) {
		this.topology = topology;
		this.availableTasks = availableTasks.clone();
		this.finishedTasks = TaskSet.noneOf(availableTasks);
	}

	/**
	 * Verifies that a sequence of actions is valid for a vehicle.
	 * 
	 * @param vehicle
	 *            The vehicle executing the plan
	 * @param plan
	 *            The plan
	 */
	public void verifyPlan(Vehicle vehicle, Plan plan) {
		this.capacity = vehicle.capacity();
		this.currentCity = vehicle.getCurrentCity();
		this.currentTasks = vehicle.getCurrentTasks();

		for (Action action : plan) {
			String[] message = action.accept(this);
			if (message != null)
				throw new IllegalPlanException(message);
		}
	}

	/**
	 * Verifies that the plan picks up all available tasks.
	 * 
	 * @throws IllegalPlanException
	 *             If this constraint is violated
	 */
	public void verifyPickup() {
		// check availableTasks
		if (!availableTasks.isEmpty())
			throw new IllegalPlanException(new String[] {
					"Illegal plan: Not all available tasks were picked up",
					"\tMissing tasks : " + availableTasks, });
	}

	/**
	 * Verifies that the plan delivers all tasks that were picked up.
	 * 
	 * @throws IllegalPlanException
	 *             If this constraint is violated
	 */
	public void verifyDelivery() {
		// check currentTasks
		if (!currentTasks.isEmpty())
			throw new IllegalPlanException(
					new String[] {
							"Illegal plan: Not all tasks that have been picked up were also delivered",
							"\tMissing tasks : " + currentTasks, });
	}

	@Override
	public String[] moveTo(City targetCity) {
		// check city
		if (!topology.contains(targetCity))
			return new String[] { "Illegal move: City does not exist",
					"\tTopology    : " + topology,
					"\tTarget city : " + targetCity };
		// check neighbor
		if (!currentCity.hasNeighbor(targetCity))
			return new String[] {
					"Illegal move: Target city is not a neighbor",
					"\tCurrent city : " + currentCity,
					"\tTarget city  : " + targetCity };

		currentCity = targetCity;
		return null;
	}

	@Override
	public String[] pickup(Task task) {
		// check task
		if (!availableTasks.contains(task))
			return new String[] { "Illegal pickup: Task does not exist",
					"\tAvailable tasks : " + availableTasks,
					"\tPickup task     : " + task };
		// check capacity
		int load = currentTasks.weightSum();
		if (!availableTasks.contains(task) || capacity < load + task.weight)
			return new String[] {
					"Illegal pickup: Task weight exceeds remaining vehicle capacity",
					"\tCurrent tasks : " + currentTasks,
					"\tCurrent load  : " + load + " / " + capacity + " kg",
					"\tPickup task   : " + task };

		availableTasks.remove(task);
		currentTasks.add(task);
		return null;
	}

	@Override
	public String[] deliver(Task task) {
		// check task
		if (!currentTasks.contains(task))
			return new String[] { "Illegal delivery: Task does not exist",
					"\tCarried tasks : " + currentTasks,
					"\tDelivery task : " + task };

		currentTasks.remove(task);
		finishedTasks.add(task);
		return null;
	}
}
