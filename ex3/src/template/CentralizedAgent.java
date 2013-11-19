package template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;


/**
 * A Centralized Agent that uses linked list everywhere.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class CentralizedAgent implements CentralizedBehavior {

    private static int MAX_ROUND = 1000;

    // Not used
    //private Topology topology;
    //private TaskDistribution distribution;
    //private Agent agent;

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        // Not used anyway
        //this.topology = topology;
        //this.distribution = distribution;
        //this.agent = agent;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planning planning = new Planning(vehicles);
        planning.selectInitialSolution(tasks);
        //planning.selectInitialSolutionRoundRobin(tasks);
        int i = MAX_ROUND;
        double cost = planning.getCost();
        System.err.println(planning);
        try {
            serieToCsv(planning.toTimeSerie(), "plan0.csv");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        while (i-- > 0) {
            List<Planning> neighbors = planning.chooseNeighbors();
            Planning best = localChoiceSimulatedAnnealing(planning, neighbors, i);
            if (cost > best.getCost()) {
                System.err.println(i + "> " + cost);
            }
            cost = best.getCost();
            planning = best;
        }
        System.err.println(planning);
        try {
            serieToCsv(planning.toTimeSerie(), "plan.csv");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return planning.toList();
    }

    /**
     * Choose to replace the old plan with the best one from the plan.
     *
     * Greedy algorithm.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param round current round
     * @return best new planning
     */
    private Planning localChoice(Planning old, List<Planning> plans, int round) {
        double cost = old.getCost(), c = cost;
        boolean valid;
        Planning best = old;
        for (Planning plan : plans) {
            // Constraints check
            valid = plan.isValid();
            // Money check
            c = plan.getCost();
            if (valid && c < cost) {
                cost = c;
                best = plan;
            }
        }
        return best;
    }

    /**
     * Choose the new plan using a simulated annealing process.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param temperature energy left
     * @return the next planning to be used.
     */
    private Planning localChoiceSimulatedAnnealing(Planning old, List<Planning> plans, int temperature) {
        double cost = old.getCost(), c = cost;
        Random rand = new Random(temperature); // pseudo random
        // we accept that percentage of difference between neighbor and
        // solution.
        double tolerance = Math.pow(temperature / (double) MAX_ROUND, 4);
        Planning best = old;
        for (Planning plan : plans) {
            // Constraints check
            if (!plan.isValid()) {
                continue;
            }
            // Money check
            c = plan.getCost();
            if (c < cost)  {
                cost = c;
                best = plan;
            } else {
                // Simulated annealing
                // -------------------
                // tolerance is the % of extra we agree to tolerate
                // max is the maximum cost we are considering
                // p is the where the value sits from cost to max.
                double max = cost * (1 + tolerance);
                if (c > max) {
                    continue; // p = 0
                }
                double p = (max - c) / (max - cost);
                if (p > rand.nextDouble()) {
                    best = plan;
                }
            }
        }
        return best;
    }

    /**
     * Output timeseries to CSV
     */
    private void serieToCsv(int[][][] serie, String filename) throws IOException {
        File f = new File(filename);
        FileWriter fp = new FileWriter(f);

        for (int s=0; s < serie.length; s++) {
            int[][] time = serie[s];
            for (int t=0; t < time.length; t++) {
                fp.write(s + "," + time[t][0] + "," + time[t][1] + "\n");
            }
        }

        fp.close();
        System.err.println(filename + " has been written with timeserie.");
    }
}
