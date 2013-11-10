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
        boolean terminationCriterion = false;
        while (!terminationCriterion) {
            // todo
            terminationCriterion = true;
        }
        System.err.println(planning);
        return planning.toList();
    }
}
