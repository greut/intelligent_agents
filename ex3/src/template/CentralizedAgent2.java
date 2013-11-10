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
        int i = 100;
        System.err.println("=> " + planning.getCost());
        while (i-- > 0) {
            List<Planning> neighbors = planning.chooseNeighbors();
            Planning best = localChoice(planning, neighbors);
            // TODO: probability thingy
            planning = best;
            //System.err.println("-> " + planning.getCost());
        }
        System.err.println(planning);
        System.err.println("=> " + planning.getCost());
        return planning.toList();
    }

    private Planning localChoice(Planning old, List<Planning> plans) {
        double cost = old.getCost(), c = cost;
        Planning best = old;
        for (Planning plan : plans) {
            c = plan.getCost();
            if (c < cost) {
                cost = c;
                best = plan;
            }
        }
        return best;
    }
}
