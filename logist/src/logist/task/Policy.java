package logist.task;

import java.util.Arrays;
import java.util.Random;

import logist.topology.Topology;
import logist.topology.Topology.City;

/**
 * A policy allows to create a probability distribution that depends on the
 * distance of the tasks.
 * 
 * @author Robin Steiger
 */
public abstract class Policy {

	final Topology topology;
	final double minDistance;
	final double maxDistance;
	final double avgDistance;
	final double lenDistance;

	Policy(Topology topology) {
		this.topology = topology;

		double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
		for (City from : topology) {
			for (City to : topology) {
				if (from == to)
					continue;

				double dist = from.distanceTo(to);

				if (min > dist)
					min = dist;
				if (max < dist)
					max = dist;
			}
		}
		this.minDistance = min;
		this.maxDistance = max;
		this.avgDistance = (max + min) / 2.0;
		this.lenDistance = (max - min) / 2.0;
	}

	abstract double constant(double dist);

	abstract double uniform(double dist, Random rnd);

	public double[][] constant(double min, double max) {

		int numC = topology.size();
		double[][] distribution = new double[numC][numC];

		double diff = max - min;
		for (City from : topology) {
			for (City to : topology) {
				if (from == to) {
					distribution[from.id][to.id] = 0.0;
					continue;
				}

				double distance = from.distanceTo(to);

				double p = constant(distance);

				distribution[from.id][to.id] = min + diff * p;
			}
		}

		return distribution;
	}

	public double[][] uniform(double min, double max, Random rnd) {

		int numC = topology.size();
		double[][] distribution = new double[numC][numC];

		double diff = max - min;
		for (City from : topology) {
			for (City to : topology) {
				if (from == to) {
					distribution[from.id][to.id] = 0.0;
					continue;
				}

				double distance = from.distanceTo(to);

				double p = uniform(distance, rnd);

				distribution[from.id][to.id] = min + diff * p;
			}
		}

		return distribution;
	}

	/**
	 * A policy that gives a proportionally larger probability for long-distance
	 * tasks.
	 * 
	 * @author Robin Steiger
	 */
	public static class LongDistances extends Policy {
		public LongDistances(Topology topology) {
			super(topology);
		}

		@Override
		double constant(double distance) {
			return (distance - minDistance) / (2.0 * lenDistance);
		}

		@Override
		double uniform(double distance, Random rnd) {
			double low = (distance - minDistance) / (4.0 * lenDistance);
			return low + 0.5 * rnd.nextDouble();
		}
	}

	/**
	 * A policy that gives a proportionally larger probability for
	 * short-distance tasks.
	 * 
	 * @author Robin Steiger
	 */
	public static class ShortDistances extends Policy {
		public ShortDistances(Topology topology) {
			super(topology);
		}

		@Override
		double constant(double distance) {
			return (maxDistance - distance) / (2.0 * lenDistance);
		}

		@Override
		double uniform(double distance, Random rnd) {
			double low = (maxDistance - distance) / (4.0 * lenDistance);
			return low + 0.5 * rnd.nextDouble();
		}
	}

	/**
	 * A policy that gives a proportionally larger probability for
	 * medium-distance tasks.
	 * 
	 * @author Robin Steiger
	 */
	public static class MediumDistances extends Policy {
		public MediumDistances(Topology topology) {
			super(topology);
		}

		@Override
		double constant(double distance) {
			double diff = Math.abs(distance - avgDistance);
			return (lenDistance - diff) / lenDistance;
		}

		@Override
		double uniform(double distance, Random rnd) {
			double diff = Math.abs(distance - avgDistance);
			double low = (lenDistance - diff) / (2.0 * lenDistance);
			return low + 0.5 * rnd.nextDouble();
		}
	}

	/**
	 * A policy that gives a proportionally equal probability for all tasks.
	 * 
	 * @author Robin Steiger
	 */
	public static class Uniform extends Policy {
		public Uniform(Topology topology) {
			super(topology);
		}

		@Override
		double constant(double distance) {
			return 0.5;
		}

		@Override
		double uniform(double distance, Random rnd) {
			return rnd.nextDouble();
		}
	}

	public static double[] constant(int numC, double value) {

		double[] distribution = new double[numC];
		Arrays.fill(distribution, value);

		return distribution;
	}

	public static double[] uniform(int numC, double min, double max, Random rnd) {

		double[] distribution = new double[numC];

		double diff = max - min;
		for (int i = 0; i < numC; i++)
			distribution[i] = min + diff * rnd.nextDouble();

		return distribution;
	}

}
