package g16;

import java.util.Arrays;
import java.util.List;

import logist.Measures;
import logist.behavior.AuctionBehavior;
import logist.agent.Agent;
import logist.simulation.Vehicle;
import logist.plan.Plan;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.task.TaskSet;
import logist.topology.Topology;
import logist.topology.Topology.City;

import g16.plan.Planning;
import g16.plan.Schedule;

/**
 * Super greedy agent that will never take risk and bid its marginal cost or
 * 1 (to avoid working for free). Its goal is to pick all the tasks from its
 * peers and never let them do any work.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class AuctionGreedy implements AuctionBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;

    /**
     * Current best plan.
     */
    private Planning current;
    /**
     * Candidate plan for the bid in progress.
     */
    private Planning candidate;
    /**
     * Current marginal cost.
     */
    double marginalCost;
    /**
     * Current bid made.
     */
    private long bid;
    /**
     * Current reward.
     */
    private long reward;

    @Override
    public void setup(Topology t, TaskDistribution td, Agent a) {

        topology = t;
        distribution = td;
        agent = a;

        current = new Planning(agent.vehicles());
        candidate = null;
        bid = 0;
        reward = 0;
    }

    @Override
    public void auctionResult(Task previous, int winner, Long[] bids) {
        String status;
        if (winner == agent.id()) {
            reward += bid - marginalCost;
            current = candidate;
            status = "win " + bid + " (" + Math.round(bid - marginalCost) + ")";
        } else {
            status = "lost " + bid;
            bid = 0;
            candidate = null;
        }
        System.err.println(this.agent.id() + " => " + status);
    }

    @Override
    public Long askPrice(Task task) {
        candidate = Planning.addAndSimulate(current, task, 1000);
        // Never work for free.
        marginalCost = candidate.getCost() - current.getCost();
        bid = Math.max(1, Math.round(marginalCost + 1));
        return bid;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        // Will rebuild the planning using the one from the taskset.
        Planning solution = new Planning(current, vehicles, tasks);
        //System.err.println(solution);

        System.err.println("Greedy(" + agent.id() + ")> " + reward + "$ | #" + tasks.size());
        return solution.toList();
    }
}
