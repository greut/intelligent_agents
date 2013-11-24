package logist.simulation;

import java.awt.Color;

import logist.Measures;
import logist.plan.Action;
import logist.plan.ActionHandler;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

/**
 * A vehicle in the pickup and delivery problem.
 * 
 * @author Robin Steiger
 */
public class VehicleImpl extends MovingObject implements ActionHandler<Boolean> {
	private final long PICKUP_DELIVER_TIME = 300 * 1000000;
	
	private final Vehicle info = new Info();

	private final String name;
	private final int id;
	private final int capacity;
	private final int costPerKm;
	private final City homeCity;
	private VehicleController controller;
//	private Agent controller;

	// History
//	private final List<Action> pastActions = new ArrayList<Action>();
	private long totalReward;
	private long totalDistance;

	private TaskSet availableTasks;
	private TaskSet currentTasks;
	private TaskSet finishedTasks;

	public VehicleImpl(int id, String name, int capacity, int costPerKm, City home, long speed, Color color) {
		super(speed, color);

		this.name = name;
		this.id = id;
		this.capacity = capacity;
		this.costPerKm = costPerKm;
		this.homeCity = home;
	}
	
	public void setController(VehicleController controller) {
		if (controller == null)
			throw new IllegalStateException("Controller was already set");
		
		this.controller = controller;
	}
	
	/**
	 * 
	 * @param tasks
	 */
	public void beginRound(TaskSet tasks){
		setTasks(tasks);
		totalReward = 0;
		totalDistance = 0;
		reset(homeCity);
	}
	
	public void setTasks(TaskSet tasks){
		availableTasks = tasks;
		currentTasks = TaskSet.noneOf(tasks);
		finishedTasks = TaskSet.noneOf(tasks);
	}

	public City getCurrentCity() {
		return nextCity;
	}

	public boolean hasNextAction() {
		return hasArrived();
	}

	public Action executeNextAction() {

		while (true) {
			Action action = controller.nextAction(id);	
			if (action == null)
				return null;

			if (action.accept(this))
				return action;
			
			controller.stuckAction(id, action);
		}
	}	
	
	public double getRewardRatio() {
		if (totalDistance == 0) return 0;
		return totalReward / Measures.unitsToKM(totalDistance) - costPerKm;
	}
	
	public int numTasks() {
		return currentTasks.size() + finishedTasks.size();
	}
	
	/* ActionHandler<Boolean> */

	@Override
	public Boolean deliver(Task task) {
		if (!currentTasks.contains(task))
			return false;

//		System.out.println("[" + name + "] \tDelivers " + task);
		controller.notifyDelivery(task);
		
		totalReward += task.reward;
		currentTasks.remove(task);
		finishedTasks.add(task);
		setWait(PICKUP_DELIVER_TIME);
		return true;
	}

	@Override
	public Boolean moveTo(City target) {
		if (!getCurrentCity().hasNeighbor(target))
			return false;

		totalDistance += setNextCity(target);
		return true;
	}

	@Override
	public Boolean pickup(Task task) {
		int load = currentTasks.weightSum() + task.weight;
		if (!availableTasks.contains(task) || capacity < load)
			return false;

//		System.out.println("[" + name + "] \tPicks up " + task);
		controller.notifyPickup(task);

		availableTasks.remove(task);
		currentTasks.add(task);
		setWait(PICKUP_DELIVER_TIME);
		return true;
	}

	/* VehicleInfo */
	
	public Vehicle getInfo() {
		return info;
	}

	private class Info implements Vehicle {

		@Override
		public int capacity() {
			return capacity;
		}

		@Override
		public int id() {
			return id;
		}

		@Override
		public TaskSet getCurrentTasks() {
			return currentTasks.clone();
		}

		@Override
		public City getCurrentCity() {
			return nextCity;
		}

		@Override
		public City homeCity() {
			return homeCity;
		}

		@Override
		public String name() {
			return name;
		}
		
//		@Override
//		public Double getDiscountFactor() {
//			return null;
//		}

		@Override
		public double speed() {
			return Measures.unitsToKM(VehicleImpl.this.getSpeed());
		}

		@Override
		public long getReward() {
			return totalReward;
		}
		
		@Override
		public long getDistanceUnits() {
			return totalDistance;
		}
		
		@Override
		public double getDistance() {
			return Measures.unitsToKM(totalDistance);
		}
		
		@Override
		public int costPerKm() {
			return costPerKm;
		}

		@Override
		public Color color() {
			return getColor();
		}
		
	}
}
