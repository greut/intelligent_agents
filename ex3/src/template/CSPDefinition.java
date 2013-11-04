package template;

import java.util.List;
import java.util.HashMap;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;


public class CSPDefinition {
	private List<Task> tasks;
	private List<Vehicle> vehicles;

	private HashMap<Task,Task> tnextTask;
	private HashMap<Vehicle,Task> vnextTask;
	private HashMap<Task,Integer> time;
	private HashMap<Task,Vehicle> vehicle;

	public CSPDefinition(List<Task> tasks, List<Vehicle> vehicles) {
		this.tasks = tasks;
		this.vehicles = vehicles;

		tnextTask = new HashMap<Task,Task>();
		vnextTask = new HashMap<Vehicle,Task>();
		time = new HashMap<Task,Integer>();
		vehicle = new HashMap<Task,Vehicle>();
	}


	/**
	 * Set next task of a task
	 * Guarantees validity of values
	 */
	public void setNextTask(Task t, Task t1) {
		if(t != null) tnextTask.put(t,t1);
	}

	/**
	 * Set first task of a vehicle
	 * Guarantees validity of values
	 */
	public void setNextTask(Vehicle v, Task t) {
		if(v != null) vnextTask.put(v,t);
	}

	/**
	 * Set time of a task
	 * Guarantees validity of values
	 */
	public void setTime(Task t, Integer t1) {
		if(t != null && t1 > 0 && t1 <= tasks.size()) time.put(t,t1);
	}

	/**
	 * Set vehicle that transport a task
	 * Guarantees validity of values
	 */
	public void setVehicle(Task t, Vehicle v) {
		if(t != null && v != null) vehicle.put(t,v);
	}

	/**
	 * Return the next task of a task (or null)
	 */
	public Task nextTask(Task t) {
		return tnextTask.get(t);
	}

	/**
	 * Return the first task for a vehicle (or null)
	 */
	public Task nextTask(Vehicle v) {
		return vnextTask.get(v);
	}

	/**
	 * Return the next task of a task (or null)
	 */
	public Task time(Task t) {
		return time.get(t);
	}
	/**
	 * Return the next task of a task (or null)
	 */
	public Task vehicle(Task t) {
		return vehicle.get(t);
	}


	/**
	 * Return true or false whether the variable instances fall within the constraints or not
	 */
	public boolean isValid() {
		int delivered = 0; //number of value in nextTask
		int endpath = 0; //number of nulls in nextTask

		for(Task t : tasks) {
			Task t1 = nextTask(t);
			if(t.equals(t1)) return false;
			if(time(t1) != time(t) + 1) return false;
			if(vehicle(t) != vehicle(t1)) return false;
			if(t.weight > vehicle(t).capacity()) return false;

			if(t1 == null) endpath++;
			else delivered++;
		}
		for(Vehicle v : vehicles) {
			Task t = nextTask(v);
			if(t != null && time(t) != 1) return false;
			if(vehicle(t) != v) return false;

			if(t == null) endpath++;
			else delivered++;
		}

		if(delivered != tasks.size() || endpath != vehicles.size()) return false;

		return true; // valid istancing of variables
	}





	public double dist(Task t1, Task t2) {
		return (t2 == NULL) ? 0 : t1.deliveryCity.distanceTo(t2.pickupCity);
	}

	public double dist(Vehicle v, Task t) {
		return (t == NULL) ? 0 : v.getCurrentCity().distanceTo(t.pickupCity);
	}

	public double length(Task t) {
		return (t == NULL) ? 0 : t.pickupCity.distanceTo(t.deliveryCity);
	}

	public double cost(Vehicle v) {
		return v.costPerKm();
	}

	public double objectiveFunction() {
		double total = 0;
		double tkm;
		for(Task t : tasks) {
			total += (dist(t, nextTask(t)) + length(nextTask(t))) * cost(vehicle(t));
		}
		for(Vehicle v : vehicles) {
			total += (dist(v, nextTask(v)) + length(nextTask(v))) * cost(v);
		}
		return total;
	}

}