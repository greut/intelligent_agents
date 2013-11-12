package template;

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
        int i = 1000;
        System.err.println(planning);
        while (i-- > 0) {
            List<Planning> neighbors = planning.chooseNeighbors();
            Planning best = localChoice(planning, neighbors);
            // TODO: probability thingy
            planning = best;
            System.err.println(i + "> " + planning.getCost());
        }
        System.err.println(planning);
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
}
