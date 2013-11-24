package logist.task;

import java.util.Random;

import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A task distribution and a task generator that are based on task frequencies.
 * 
 * @author Robin Steiger
 */
public class DefaultTaskDistribution implements TaskDistribution {

	private final Topology topology;
	private final Random random;

	private final double[][] frequence;
	private final double[][] reward;
	private final double[][] weight;
	private final double[] notask;

	private final double[][] probability;
	private final double[] frequenceSum;
	private final double frequenceSumGlobal;

	public DefaultTaskDistribution(Topology topology, Random random,
			double[][] f, double[][] r, double[][] w, double[] n) {
		this.topology = topology;
		this.random = random;
		this.frequence = f;
		this.notask = n;
		this.reward = r;
		this.weight = w;

		// normalize probabilities
		int numC = f.length;
		this.probability = new double[numC][numC];
		this.frequenceSum = new double[numC];

		double sum = 0;
		for (int i = 0; i < numC; i++) {
			for (int j = 0; j < numC; j++)
				frequenceSum[i] += frequence[i][j];

			sum += frequenceSum[i];

			double factor = (1.0 - notask[i]) / frequenceSum[i];
			for (int j = 0; j < numC; j++)
				probability[i][j] = frequence[i][j] * factor;
		}
		this.frequenceSumGlobal = sum;
	}

	@Override
	public double probability(City from, City to) {
		return (to == null) ? notask[from.id] : probability[from.id][to.id];
	}

	@Override
	public int reward(City from, City to) {
		return (to == null) ? 0 : (int) reward[from.id][to.id];
	}

	@Override
	public int weight(City from, City to) {
		return (to == null) ? 0 : (int) weight[from.id][to.id];
	}

	public Task createTask(City from) {
		double prob = random.nextDouble();

		for (City to : topology) {
			prob -= probability[from.id][to.id];
			if (prob <= 0.0) {
				double rew = reward[from.id][to.id];
				double wgt = weight[from.id][to.id];

				return new Task(0, from, to, (int) rew, (int) wgt);
			}
		}
		return null;
	}

	public TaskSet createTaskSet(int size) {
		return createTaskSet(new Task[size]);
	}
	
	public TaskSet createTaskSet(Task[] tasks) {
		
		for (int i = 0; i < tasks.length; i++)
			tasks[i] = createTask(i);

		return TaskSet.create(tasks);
	}

	public Task createTask() {
		return createTask(0);
	}

	private Task createTask(int id) {
		double pivot = random.nextDouble() * frequenceSumGlobal;

		for (City from : topology) {
			pivot -= frequenceSum[from.id];
			if (pivot < 0) {
				for (City to : topology) {
					pivot += frequence[from.id][to.id];
					if (pivot >= 0) {
//						@SuppressWarnings("unused")
						double rew = reward[from.id][to.id];
						double wgt = weight[from.id][to.id];

//						return new Task(id, from, to, (long) 0, (int) wgt);
						return new Task(id, from, to, (long) rew, (int) wgt);
					}
				}
			}
		}
		throw new AssertionError("no task was created");
	}

	public Random getRandom() {
		return random;
	}
}
