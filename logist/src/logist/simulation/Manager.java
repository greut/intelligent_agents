package logist.simulation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import logist.agent.Agent;
import logist.agent.AgentImpl;

import logist.LogistException;
import logist.LogistSettings;
import logist.LogistSettings.FileKey;
import logist.LogistSettings.FlagKey;
import logist.LogistSettings.SizeKey;
import logist.history.History;
import logist.task.DefaultTaskDistribution;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;

/**
 * Does every part of the simulation except the simulation of actions. Assigns
 * agents to companies, generates tasks and does the auctioning.
 * 
 * @author Robin Steiger
 */
public class Manager implements Context {

	private final LogistSettings settings;
	private final Topology topology;
	private final List<AgentImpl> agents;
	private final List<Company> companies;
	private final DefaultTaskDistribution taskDistribution;

	private History history;
	private Simulation simulation;
	private int round;
	private TaskSet toBeDelivered;
	private TaskSet toBePickedUp;

	public Manager(LogistSettings settings, Topology topology,
			List<AgentImpl> agents, List<Company> companies,
			DefaultTaskDistribution taskDistribution) {

		this.settings = settings;
		this.topology = topology;
		this.agents = agents;
		this.companies = companies;
		this.taskDistribution = taskDistribution;
	}

	public void run() {
		File configFile = settings.get(FileKey.CONFIGURATION);
		File histFile = settings.get(FileKey.HISTORY);
		boolean showUI = settings.get(FlagKey.SHOW_UI);
		history = new History(histFile, configFile.toString());

		setup();
		beginRound();
		simulation.run(showUI);
	}

	public void close() {
		List<Agent> ranking = new ArrayList<Agent>();
		for (AgentImpl agent : agents)
			ranking.add(agent.getInfo());
		Collections.sort(ranking);

		for (int rank = 0; rank < ranking.size(); rank++)
			history.addStat(rank + 1, ranking.get(rank));

		history.close();
		System.out.println("Wrote " + settings.get(FileKey.HISTORY));
	}

	private void setup() {
		// assign vehicles to agents (each agent controls a company)
		if (companies.size() < agents.size())
			throw new LogistException("There are more agents than companies !");

		for (int i = 0; i < agents.size(); i++) {
			AgentImpl agent = agents.get(i);
			Company company = companies.get(i);

			agent.setup(i, this, company);

			history.addAgent(agent.getInfo().name());
			for (VehicleImpl vehicle : agent.getVehicles())
				history.addVehicle(vehicle.getInfo().name());
		}
		history.flush();

		simulation = new Simulation(this, agents, history);
		round = 0;
	}

	private void beginRound() {
		history.addRound(++round);

		int numTasks = settings.get(SizeKey.NUMBER_OF_TASKS);
		Task[] taskArray = new Task[numTasks];
		TaskSet tasks = taskDistribution.createTaskSet(taskArray);
		
		this.toBeDelivered = TaskSet.copyOf(tasks);
		this.toBePickedUp = TaskSet.copyOf(tasks);
		
		
		TaskSet sharedTasks = TaskSet.copyOf(tasks);
		for (AgentImpl agent : agents) {
			switch (agent.type()) {
			case REACTIVE:
			case AUCTION:
				agent.beginRound(TaskSet.noneOf(tasks));
				break;
			case DELIBERATIVE:
				agent.beginRound(sharedTasks);
				break;				
			case CENTRALIZED:
				agent.beginRound(TaskSet.copyOf(tasks));
				break;
			}
		}

		for (Task task : tasks) {
			history.addTask(task);

			int min = -1;
			Long[] bids = new Long[agents.size()];
			for (int i = 0; i < bids.length; i++) {
				AgentImpl agent = agents.get(i);
				Long bid = bids[i] = agent.askBid(task);

				if (bid != null) {
					history.addBid(agent.getInfo().name(), bid);

					if (min == -1 || bid < bids[min])
						min = i;
				}
			}

//			if (min == -1)
//				throw new LogistException("There are no bidders !");

			// replace task
			if (min >= 0) {
				task = new Task(task.id, task.pickupCity, task.deliveryCity,
						bids[min].longValue(), task.weight);
				taskArray[task.id] = task;
			}

			for (AgentImpl agent : agents)
				agent.notifyResult(task, min, bids);
		}
	}

	@Override
	public TaskDistribution getTaskDistribution() {
		return taskDistribution;
	}

	@Override
	public Topology getTopology() {
		return topology;
	}

	@Override
	public LogistSettings getSettings() {
		return settings;
	}

	@Override
	public void notifyDelivery(Task task) {
		toBeDelivered.remove(task);
	}

	@Override
	public void notifyPickup(Task task) {
		toBePickedUp.remove(task);
	}

	@Override
	public int[] countDelivery() {
		int[] count = new int[topology.size()];
		for (Task task : toBeDelivered)
			count[task.deliveryCity.id]++;
		return count;
	}

	@Override
	public int[] countPickup() {
		int[] count = new int[topology.size()];
		for (Task task : toBePickedUp)
			count[task.pickupCity.id]++;
		return count;
	}
}
