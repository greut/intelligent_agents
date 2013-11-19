package template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Arrays;
import java.util.List;

import logist.behavior.CentralizedBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;


/**
 * A Centralized Agent that uses list, coz lists are the shit ;-)
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class CentralizedAgent2 implements CentralizedBehavior {

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
        int i = 1000;
        double cost = planning.getCost();
        System.err.println(planning);
        try {
            serieToCsv(planning.toTimeSerie(), "plan0.csv");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        while (i-- > 0) {
            List<Planning> neighbors = planning.chooseNeighbors();
            Planning best = localChoice(planning, neighbors);
            // TODO: probability thingy
            planning = best;
            if (cost > planning.getCost()) {
                cost = planning.getCost();
                System.err.println(i + "> " + cost);
            }
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
     * @param old   the old plan
     * @param plans set of new plans
     */
    private Planning localChoice(Planning old, List<Planning> plans) {
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
