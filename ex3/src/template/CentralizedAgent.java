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
            //Planning best = localChoiceGreedy(planning, neighbors);
            Planning best = localChoiceStochastic(planning, neighbors);
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
    private Planning localChoiceGreedy(Planning old, List<Planning> plans) {
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
     * Choose to replace the old plan with the best one from the plan.
     *
     * Stochastic algorithm.
     *
     * @param old   the old plan
     * @param plans set of new plans
     * @param round current round
     * @return best new planning
     */
    private Planning localChoiceStochastic(Planning old, List<Planning> plans) {
        double cost = old.getCost(), c = cost;
        boolean valid;
        ArrayList<Planning> bests = new ArrayList<Planning>();
        for (Planning plan : plans) {
            // Constraints check
            if (!plan.isValid()) {
                continue;
            }
            // Money check
            c = plan.getCost();
            if (c < cost) {
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
