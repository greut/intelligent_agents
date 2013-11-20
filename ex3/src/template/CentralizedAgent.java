package template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.ArrayList;
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

    private int iterations;
    private String init;
    private String search;

    @Override
    public void setup(Topology topology, TaskDistribution distribution,
            Agent agent) {
        iterations = agent.readProperty("iterations", Integer.class, 10000);
        init = agent.readProperty("init", String.class, "one").toLowerCase();
        search = agent.readProperty("local-search", String.class, "greedy").toLowerCase();

        System.err.println("Iterations: " + iterations);
        System.err.println("Initialization: " + init);
        System.err.println("Local search: " + search);
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planning planning = new Planning(vehicles);
        Planning best = planning;

        if ("one".equals(init)) {
            // Assign all the tasks to the biggest vehicle
            planning.selectInitialSolution(tasks);
        } else {
            // Assign all the tasks in a round-robin fashion
            planning.selectInitialSolutionRoundRobin(tasks);
        }

        // Debug
        System.err.println(planning);
        try {
            serieToCsv(planning.toTimeSerie(), "plan0.csv");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        int i = iterations;
        while (i-- > 0) {
            List<Planning> neighbors = planning.chooseNeighbors();
            Planning next;

            if ("greedy".equals(search)) {
                // Bist the best
                next = localChoiceGreedy(planning, neighbors);
            } else if ("stochastic".equals(search)) {
                // Random pick among the better ones
                next = localChoiceStochastic(planning, neighbors);
            } else {
                // Consider any valid solutions and cool it down
                next = localChoiceSimulatedAnnealing(planning, neighbors, i);
            }

            if (best.getCost() > next.getCost()) {
                best = next;

                // Debug
                System.err.println(i + "> " + next.getCost());
            }

            planning = next;
        }
        planning = best;

        // Debug
        System.err.println(planning);
        try {
            serieToCsv(planning.toTimeSerie(), "plan.csv");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        return planning.toList();
    }

    /**
     * Choose to replace the old plan with one random good one from the plans.
     *
     * Greedy algorithm.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param round current round
     * @return best new planning
     */
    private Planning localChoiceGreedy(Planning old, List<Planning> plans) {
        double cost = old.getCost(), c = cost;
        boolean valid;
        Planning best = old;
        for (Planning plan : plans) {
            if (!plan.isValid()) {
                continue;
            }

            c = plan.getCost();
            if (c < cost) {
                cost = c;
                best = plan;
            }
        }
        return best;
    }

    /**
     * Choose to replace the old plan with a good one (always) or a bad one
     * sometimes (simulated annealing).
     *
     * Stochastic algorithm.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param round current round
     * @return best new planning
     */
    private Planning localChoiceStochastic(Planning old, List<Planning> plans) {
        ArrayList<Planning> bests = new ArrayList<Planning>();
        for (Planning plan : plans) {
            if (!plan.isValid()) {
                continue;
            }

            if (plan.getCost() < old.getCost()) {
                bests.add(plan);
            }
        }

        if (bests.size() > 0) {
            Random rand = new Random();
            return bests.get(rand.nextInt(bests.size()));
        } else {
            return old;
        }
    }

    /**
     * Choose to replace the old plan with the best one from the plan.
     *
     * Simulated annealing.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param round current round
     * @return best new planning
     */
    private Planning localChoiceSimulatedAnnealing(Planning old, List<Planning> plans, int temperature) {
        ArrayList<Planning> valids = new ArrayList<Planning>();
        for (Planning plan : plans) {
            if (!plan.isValid()) {
                continue;
            }
            valids.add(plan);
        }

        if (valids.size() > 0) {
            double cost = old.getCost();
            Random rand = new Random();
            Planning best;

            int tries = valids.size();
            while (tries-- > 0) {
                best = valids.get(rand.nextInt(valids.size()));
                double badness = cost - best.getCost();
                if (badness > 0) {
                    return best;
                } else if (rand.nextDouble() < (Math.exp(badness / (iterations / (double) temperature)))) {
                    return best;
                }
            }
        }
        return old;
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
