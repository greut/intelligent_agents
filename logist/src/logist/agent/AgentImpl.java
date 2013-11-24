package logist.agent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import logist.behavior.AuctionBehavior;
import logist.behavior.CentralizedBehavior;
import logist.behavior.DeliberativeBehavior;
import logist.behavior.ReactiveBehavior;

import logist.Measures;
import logist.config.ParserException;
import logist.config.XMLTag;
import logist.simulation.Company;
import logist.simulation.Context;
import logist.simulation.VehicleImpl;
import logist.simulation.VehicleController;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;

/**
 * An agent in the pickup and delivery problem.
 * <p>
 * The concrete behavior of an agent is defined by a {@link behavior}
 * class.
 * 
 * 
 * @author Robin Steiger
 */
public abstract class AgentImpl implements VehicleController {

	public enum Type {
		REACTIVE, DELIBERATIVE, CENTRALIZED, AUCTION
	}

	final String name;
	final Agent agentInfo;
	// String companyName;

	private int id;
	Context sim;
	List<VehicleImpl> vehicles;

	TaskSet tasks;

	AgentImpl(String name) {
		this.name = name;
		this.agentInfo = new Info();
	}
	
	@Override
	public String toString() {
		return "Agent " + name;
	}

	public void setup(int id, Context sim, Company company) {
		this.id = id;
		this.sim = sim;
		this.vehicles = company.vehicles;
	}

	public void beginRound(TaskSet empty) {
		this.tasks = empty;
		for (VehicleImpl vehicle : vehicles)
			vehicle.beginRound(empty);
	}

	public abstract Long askBid(Task task);

	public void notifyResult(Task previous, int winner, Long[] offers) {
		// System.out.println("Notify " + name + " " + id + " ");
		if (winner == id)
			tasks.add(previous);
	}

	// public abstract void begin(TaskSet tasks);
	public List<VehicleImpl> getVehicles() {
		return vehicles;
	}

	public Agent getInfo() {
		return agentInfo;
	}

	public static AgentImpl forClass(String agentName, Map<String, String> map,
			Class<?> behavior) {

		AgentImpl agent;

		if (ReactiveBehavior.class.isAssignableFrom(behavior)) {
			Class<? extends ReactiveBehavior> clazz = behavior
					.asSubclass(ReactiveBehavior.class);
			agent = new ReactiveAgent(agentName, clazz);
		} else if (DeliberativeBehavior.class.isAssignableFrom(behavior)) {
			Class<? extends DeliberativeBehavior> clazz = behavior
					.asSubclass(DeliberativeBehavior.class);
			agent = new DeliberativeAgent(agentName, clazz);
		} else if (CentralizedBehavior.class.isAssignableFrom(behavior)) {
			Class<? extends CentralizedBehavior> clazz = behavior
					.asSubclass(CentralizedBehavior.class);
			agent = new CentralizedAgent(agentName, clazz);
		} else if (AuctionBehavior.class.isAssignableFrom(behavior)) {
			Class<? extends AuctionBehavior> clazz = behavior
					.asSubclass(AuctionBehavior.class);
			agent = new AuctionAgent(agentName, clazz);
		} else {
			return null;
		}

		((Info) agent.agentInfo).properties = map;

		// try {
		// Class<? extends ReactiveBehavior> clazz = behavior
		// .asSubclass(ReactiveBehavior.class);
		// return new ReactiveAgent(agentName, clazz);
		// } catch (ClassCastException ccEx) {
		// }
		//
		// try {
		// Class<? extends DeliberativeBehavior> clazz = behavior
		// .asSubclass(DeliberativeBehavior.class);
		// return new DeliberativeAgent(agentName, clazz);
		// } catch (ClassCastException ccEx) {
		// }
		//
		// try {
		// Class<? extends AuctionBehavior> clazz = behavior
		// .asSubclass(AuctionBehavior.class);
		// return new AuctionAgent(agentName, clazz);
		// } catch (ClassCastException ccEx) {
		// }

		// interface with legacy API
		// try {
		// Class<? extends Behavior> clazz =
		// behavior.asSubclass(Behavior.class);
		// return new AuctionAgent(clazz, agentName);
		// } catch (ClassCastException ccEx) {}

		return agent;
	}

	private class Info implements Agent {

		List<Vehicle> infosCache;
		Map<String, String> properties;

		@Override
		public String name() {
			return name;
		}

		@Override
		public int id() {
			return id;
		}

		@Override
		public TaskSet getTasks() {
			return (tasks == null) ? null : TaskSet.copyOf(tasks);
		}

		@Override
		public List<Vehicle> vehicles() {

			if (infosCache == null) {
				List<Vehicle> infos = new ArrayList<Vehicle>(vehicles
						.size());
				for (VehicleImpl vehicle : vehicles)
					infos.add(vehicle.getInfo());

				infosCache = Collections.unmodifiableList(infos);
			}
			return infosCache;
		}

		@Override
		public long getTotalDistanceUnits() {
			long total = 0;
			for (Vehicle vehicle : vehicles())
				total += vehicle.getDistanceUnits();

			return total;
		}

		@Override
		public double getTotalDistance() {
			return Measures.unitsToKM(getTotalDistanceUnits());
		}

		@Override
		public long getTotalCost() {
			long total = 0;
			for (Vehicle vehicle : vehicles())
				total += vehicle.getDistanceUnits() * vehicle.costPerKm();
			return (long) Math.round(Measures.unitsToKM(total));
		}

		@Override
		public long getTotalReward() {
			long total = 0;
			for (Vehicle vehicle : vehicles())
				total += vehicle.getReward();
			return total;
		}

		@Override
		public long getTotalProfit() {
			return getTotalReward() - getTotalCost();
		}

		@Override
		public int compareTo(AgentStatistics that) {
			long myProfit = this.getTotalProfit();
			long hisProfit = that.getTotalProfit();

			if (myProfit > hisProfit)
				return -1;
			if (myProfit < hisProfit)
				return 1;
			return 0;
		}

		@Override
		public int getTotalTasks() {
			int total = tasks.size();
			for (VehicleImpl vehicle : getVehicles())
				total += vehicle.numTasks();
			return total;
		}

		@Override
		public <T> T readProperty(String paramName, Class<T> clazz, T default_) {
			try {
				return XMLTag.convert(properties, paramName, "agent", clazz,
						default_);
			} catch (ParserException pEx) {
				throw new RuntimeException("parameter conversion failed", pEx);
			}
		}
	}

	public abstract Type type();

	@Override
	public void notifyDelivery(Task task) {
		sim.notifyDelivery(task);
	}

	@Override
	public void notifyPickup(Task task) {
		sim.notifyPickup(task);
	}
}
