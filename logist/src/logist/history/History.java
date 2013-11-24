package logist.history;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import logist.agent.AgentStatistics;

import logist.task.Task;

import static logist.history.History.State.*;

/**
 * Records the history of a simulation and writes it to an XML file.
 * 
 * @author Robin Steiger
 */
public class History {
	enum State {
		INIT, AGENTS, ROUNDS, TASKS, EVENTS, STATS
	}

	private XMLWriter writer;
	private State state = INIT;

	public History(File file, String config) {
		try {
			this.writer = new XMLWriter(
					new BufferedWriter(new FileWriter(file)));
			writer.writeTag("history");
			writer.writeAttribute("configuration", config);
		} catch (IOException ioEx) {
			throw new XMLWritingException(ioEx);
		}
	}

	public void addAgent(String name) {
		if (state == INIT) {
			writer.writeTag("agents");
			state = AGENTS;
		} else if (state == AGENTS) {
			writer.endTag(); // close agent tag
		} else
			expectState(AGENTS); // will fail

		writer.writeTag("agent");
		writer.writeAttribute("name", name);
	}

	public void addVehicle(String name) {
		expectState(AGENTS);

		writer.writeTag("vehicle");
		writer.writeAttribute("name", name);
		writer.endTag();
	}

	public void addRound(int round) {
		if (state == AGENTS) {
			writer.endTag(); // close agent tag
			writer.endTag(); // close agents tag
			writer.writeTag("rounds");
			state = ROUNDS;
		} else if (state == EVENTS) {
			writer.endTag(); // close events tag
			writer.endTag(); // close round tag
			state = ROUNDS;
		} else
			expectState(AGENTS); // will fail

		writer.writeTag("round");
		writer.writeAttribute("id", round);
	}

	public void addTask(Task task) {
		if (state == ROUNDS) {
			writer.writeTag("tasks");
			state = TASKS;
		} else if (state == TASKS) {
			writer.endTag(); // close tasks tag
		} else
			expectState(TASKS);

		writer.writeTag("task");
		writer.writeAttribute("id", task.id);
		writer.writeAttribute("pickup", task.pickupCity);
		writer.writeAttribute("delivery", task.deliveryCity);
		writer.writeAttribute("weight", task.weight);
		writer.writeAttribute("reward", task.reward);
	}

	public void addBid(String agent, long bid) {
		expectState(TASKS);

		writer.writeTag("bid");
		writer.writeAttribute("agent", agent);
		writer.writeAttribute("bid", bid);
		writer.endTag();
	}

	public void addEvent(int id, Event event) {
		if (state == TASKS) {
			writer.endTag(); // close task tag
			writer.endTag(); // close tasks tag
			writer.writeTag("events");
			state = EVENTS;
		} else
			expectState(EVENTS);

		writer.writeTag("event");
		writer.writeAttribute("id", id);
		writer.writeAttribute("time", event.time);
		writer.writeAttribute("vehicle", event.vehicle.name());
		writer.writeAttribute(event.type.toString(), event.action());
		writer.endTag();
	}

	public void addStat(int rank, AgentStatistics agent) {
		if (state == EVENTS) {
			writer.endTag(); // close events tag
			writer.endTag(); // close round tag
			writer.endTag(); // close rounds tag
			writer.writeTag("statistics");
			state = STATS;
		} else if (state == TASKS) {
			writer.endTag(); // close task tag
			writer.endTag(); // close tasks tag
			writer.endTag(); // close round tag
			writer.endTag(); // close rounds tag
			writer.writeTag("statistics");
			state = STATS;
		} else
			expectState(STATS);

		writer.writeTag("stat");
		writer.writeAttribute("rank", rank);
		writer.writeAttribute("agent", agent.name());

		writer.writeTag("total-tasks");
		writer.writeAttribute("value", agent.getTotalTasks());
		writer.endTag();
		writer.writeTag("total-distance");
		writer.writeAttribute("value", agent.getTotalDistance());
		writer.endTag();
		writer.writeTag("total-cost");
		writer.writeAttribute("value", agent.getTotalCost());
		writer.endTag();
		writer.writeTag("total-reward");
		writer.writeAttribute("value", agent.getTotalReward());
		writer.endTag();
		writer.writeTag("total-profit");
		writer.writeAttribute("value", agent.getTotalProfit());
		writer.endTag();

		writer.endTag();
	}

	public void flush() {
		writer.flush();
	}

	public void close() {
		expectState(STATS);

		writer.endTag(); // close statistics tag
		writer.endTag(); // close history tag
		writer.close();
	}

	private void expectState(State expected) {
		if (state != expected) {
			flush();
			throw new IllegalStateException("In state " + state
					+ ", but expected " + expected);
		}
	}
}
