package template;

//the list of imports
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.Collections;

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

/**
 * A very simple auction agent that assigns all tasks to its first vehicle and
 * handles them sequentially.
 * 
 */
@SuppressWarnings("unused")
public class CentralizedAgent implements CentralizedBehavior {

	private Topology topology;
	private TaskDistribution distribution;
	private Agent agent;

	private double stepProbability;
	private int iterations;

	@Override
	public void setup(Topology topology, TaskDistribution distribution,
			Agent agent) {

		this.topology = topology;
		this.distribution = distribution;
		this.agent = agent;
		this.stepProbability = agent.readProperty("step-probability", Double.class, 0.5);
		this.iterations = agent.readProperty("iterations", Integer.class, 10000);
	}

	@Override
	public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {

		if(vehicles.size() == 0) {
			System.err.println("No vehicles found!");
			return null;
		}

		List<Task> taskList = new ArrayList<Task>();
		Iterator<Task> it = tasks.iterator();
		while(it.hasNext()) taskList.add(it.next());

		CSPDefinition centralPlan = new CSPDefinition(vehicles, taskList);

		selectInitialSolution(centralPlan);
		while(!centralPlan.isValid())
			selectInitialSolution(centralPlan);

		for(int i = 0; i < iterations; i++) {
			List<CSPDefinition> neighbors = chooseNeighbors(centralPlan);
			centralPlan = localChoice(centralPlan, neighbors);
		}

		// solution = centralPlan
		return centralPlan.getPlan();
	}

	/**
	 * Set initial solution
	 */
	private void selectInitialSolution(CSPDefinition plan) {
		plan.clear();
		List<Vehicle> vehicles = plan.getVehicles();
		List<Task> tasks = plan.getTasks();

		// Get biggest vehicle
		Vehicle biggest = vehicles.get(0);
		for(int k = 1; k < vehicles.size(); k++) {
			if(vehicles.get(k).capacity() > biggest.capacity())
				biggest = vehicles.get(k);
		}

		Collections.shuffle(tasks);
		// Set all tasks to the biggest vehicle
		plan.setNextTask(biggest, tasks.get(0));
		plan.setTime(tasks.get(0), 1);
		plan.setVehicle(tasks.get(0), biggest);
		for(int i = 1; i < tasks.size(); i++) {
			plan.setNextTask(tasks.get(i-1), tasks.get(i));
			plan.setTime(tasks.get(i), i+1);
			plan.setVehicle(tasks.get(i), biggest);
		}
		// all the rest will be automatically null
	}

	/**
	 * Create the set of all neighbours 1 step distant from the current plan
	 */
	private List<CSPDefinition> chooseNeighbors(CSPDefinition plan) {
		List<Vehicle> vehicles = plan.getVehicles();
		List<Task> tasks = plan.getTasks();

		List<CSPDefinition> solutions = new ArrayList<CSPDefinition>();

		Random rand = new Random();
		int r = rand.nextInt(vehicles.size());
		while(plan.nextTask(vehicles.get(r)) == null)
			r = rand.nextInt(vehicles.size());
		Vehicle rv = vehicles.get(r);

		// change vehicles
		for(int k = 0; k < vehicles.size(); k++) {
			CSPDefinition step = plan.changeVehicle(rv, vehicles.get(k));
			if(step != null)
				solutions.add(step);
		}

		// swap task order
		int length = plan.taskPerVehicle(rv);
		if(length > 1) {
			for(int i = 0; i < length-1; i++) {
				for(int j = i+1; j < length; j++) {
					CSPDefinition step = plan.changeTaskOrder(rv, i+1, j+1);
					if(step != null)
						solutions.add(step);
				}
			}
		}

		return solutions;
	}

	public CSPDefinition localChoice(CSPDefinition oldPlan, List<CSPDefinition> neighbours) {
		if(neighbours.size() == 0)
			return oldPlan; // shouldn't happen

		ArrayList<CSPDefinition> best = new ArrayList<CSPDefinition>();
		best.add(neighbours.get(0));
		double bestcost = neighbours.get(0).totalCost();

		for(CSPDefinition c : neighbours) {
			double ccost = c.totalCost();
			if(ccost < bestcost) {
				best.clear();
				best.add(c);
				bestcost = ccost;
			} else if(ccost == bestcost) {
				best.add(c);
			}
		}

		Random rand = new Random();
		int randombest = rand.nextInt(best.size());
		return (rand.nextDouble() < this.stepProbability) ? best.get(randombest) : oldPlan;
	}
}
