package template;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.topology.Topology;
import logist.topology.Topology.City;

@SuppressWarnings("unchecked")
public class CSPDefinition {
	private ArrayList<Vehicle> vehicles;
	private ArrayList<Task> tasks;

	private HashMap<Task,Task> tnextTask;
	private HashMap<Vehicle,Task> vnextTask;
	private HashMap<Task,Integer> time;
	private HashMap<Task,Vehicle> vehicle;

	public CSPDefinition(List<Vehicle> vehicles, List<Task> tasks) {
		this.vehicles = new ArrayList<Vehicle>(vehicles);
		this.tasks = new ArrayList<Task>(tasks);

		tnextTask = new HashMap<Task,Task>();
		vnextTask = new HashMap<Vehicle,Task>();
		time = new HashMap<Task,Integer>();
		vehicle = new HashMap<Task,Vehicle>();
	}

	/**
	 * Cloning constructor
	 */
	public CSPDefinition(CSPDefinition original) {
		this.vehicles = (ArrayList<Vehicle>) original.getVehicles().clone();
		this.tasks = (ArrayList<Task>) original.getTasks().clone();

		this.tnextTask = (HashMap<Task,Task>) original.getTNextTask().clone();
		this.vnextTask = (HashMap<Vehicle,Task>) original.getVNextTask().clone();
		this.time = (HashMap<Task,Integer>) original.getTime().clone();
		this.vehicle = (HashMap<Task,Vehicle>) original.getVehicle().clone();
	}

	public void clear() {
		this.tnextTask.clear();
		this.vnextTask.clear();
		this.time.clear();
		this.vehicle.clear();
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
	public Integer time(Task t) {
		return time.get(t);
	}
	/**
	 * Return the next task of a task (or null)
	 */
	public Vehicle vehicle(Task t) {
		return vehicle.get(t);
	}

	/**
	 * Get number of tasks per vehicle
	 */
	public int taskPerVehicle(Vehicle v) {
		int cnt = 0;
		Task t = nextTask(v);
		while(t != null) {
			t = nextTask(t);
			cnt++;
		}
		return cnt;
	}


	/**
	 * Return true or false whether the variable instances fall within the constraints or not
	 */
	public boolean isValid() {
		int delivered = 0; //number of tasks in nextTask
		int endpath = 0; //number of nulls in nextTask

		for(Task t : tasks) {
			Task t1 = nextTask(t);
			if(t1 == null) {
				endpath++;
			} else {
				delivered++;
				if(t.equals(t1)) return false;
				if(time(t1) != time(t) + 1) return false;
				if(vehicle(t) != vehicle(t1)) return false;
			}
			if(t.weight > vehicle(t).capacity()) return false;
		}
		for(Vehicle v : vehicles) {
			Task t = nextTask(v);
			if(t == null) {
				endpath++;
			} else {
				delivered++;
				if(time(t) != 1) return false;
				if(vehicle(t) != v) return false;
			}
		}

		if(delivered != tasks.size() || endpath != vehicles.size()) return false;

		return true; // valid istancing of variables
	}





	public double dist(Task t1, Task t2) {
		return (t2 == null) ? 0 : t1.deliveryCity.distanceTo(t2.pickupCity);
	}

	public double dist(Vehicle v, Task t) {
		return (t == null) ? 0 : v.getCurrentCity().distanceTo(t.pickupCity);
	}

	public double length(Task t) {
		return (t == null) ? 0 : t.pickupCity.distanceTo(t.deliveryCity);
	}

	public double cost(Vehicle v) {
		return v.costPerKm();
	}

	/**
	 * Objective function
	 */
	public double objectiveFunction() { return totalCost(); }
	public double totalCost() {
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

	public ArrayList<Vehicle> getVehicles() { return vehicles; }
	public ArrayList<Task> getTasks() { return tasks; }
	public HashMap<Task,Task> getTNextTask() { return tnextTask; }
	public HashMap<Vehicle,Task> getVNextTask() { return vnextTask; }
	public HashMap<Task,Integer> getTime() { return time; }
	public HashMap<Task,Vehicle> getVehicle() { return vehicle; }


	public CSPDefinition changeVehicle(Vehicle v1, Vehicle v2) {
		CSPDefinition step = new CSPDefinition(this);
		if(v1 != v2 && step.getVehicles().contains(v1) && step.getVehicles().contains(v2) && step.nextTask(v1) != null) {
			Task t = step.nextTask(v1);
			step.setNextTask(v1, step.nextTask(t));
			step.setNextTask(t, step.nextTask(v2));
			step.setNextTask(v2, t);
			
			step.setVehicle(t, v2);

			int cnt = 1;
			Task t1 = step.nextTask(v1);
			while(t1 != null) {
				step.setTime(t1, cnt);
				t1 = step.nextTask(t1);
				cnt++;
			}
			cnt = 1;
			t1 = step.nextTask(v2);
			while(t1 != null) {
				step.setTime(t1, cnt);
				t1 = step.nextTask(t1);
				cnt++;
			}
			return step;
		}
		return null;
	}

	/**
	 * Change the task order of task with id id1 and id2 of vehicle v
	 * 1-based indexing used (like variable time)
	 */
	public CSPDefinition changeTaskOrder(Vehicle v, int id1, int id2) {
		CSPDefinition step = new CSPDefinition(this);
		if(id1 == id2) return step;

		Task t1 = null;
		Task t2 = null;
		Task tmp = null;

		int cnt = 1;
		tmp = nextTask(v);
		while(tmp != null) {
			if(cnt == id1) t1 = tmp;
			if(cnt == id2) t2 = tmp;
			tmp = nextTask(tmp);
			cnt++;
		}

		if(t1 != null && t2 != null && t1 != t2) {
			cnt = 1;
			if(id1 == 1) step.setNextTask(v, t2);
			if(id2 == 1) step.setNextTask(v, t1);
			tmp = nextTask(v);
			while(tmp != null) {
				if(cnt == id1-1) step.setNextTask(tmp, t2);
				if(cnt == id2-1) step.setNextTask(tmp, t1);
				tmp = nextTask(tmp);
				cnt++;
			}

			tmp = step.nextTask(t1);
			step.setNextTask(t1, step.nextTask(t2));
			step.setNextTask(t2, tmp);
			return step;
		}

		return null;
	}


	/**
	 * Return the Solution as a plan definition
	 */
	public List<Plan> getPlan() {
		List<Plan> plans = new ArrayList<Plan>();
		for(Vehicle v : vehicles) {
			City current = v.getCurrentCity();
			Plan plan = new Plan(current);

			Task task = nextTask(v);
			while(task != null) {
				// move: current city => pickup location
				for (City city : current.pathTo(task.pickupCity))
					plan.appendMove(city);

				plan.appendPickup(task);

				// move: pickup location => delivery location
				for (City city : task.path())
					plan.appendMove(city);

				plan.appendDelivery(task);

				// set current city
				current = task.deliveryCity;
				task = nextTask(task);
			}
			
			plans.add(plan);
		}

		return plans;
	}
}