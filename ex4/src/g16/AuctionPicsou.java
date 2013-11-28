package g16;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

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
 * Built upon `AuctionGreedy` but tries to:
 *  1) block the other guys from getting tasks
 *  2) earn as much money as it can.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @see g16.AuctionGreedy
 */
public class AuctionPicsou implements AuctionBehavior {

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

    private double minCostPerKm;

    private Logger log;

    @Override
    public void setup(Topology t, TaskDistribution td, Agent a) {
        log = Logger.getLogger(AuctionPicsou.class.getName());

        topology = t;
        distribution = td;
        agent = a;

        current = new Planning(agent.vehicles());
        candidate = null;
        bid = 0;
        reward = 0;

        minCostPerKm = Integer.MAX_VALUE;
        for (Vehicle v : agent.vehicles()) {
            minCostPerKm = Math.min(minCostPerKm, v.costPerKm());
        }
    }

    @Override
    public void auctionResult(Task previous, int winner, Long[] bids) {
        String status;
        if (winner == agent.id()) {
            reward += bid - marginalCost;
            current = candidate;
            status = "win";
        } else {
            status = "lost";
        }
        log.info("[" + agent.id() + "] " + status + "\t" + bid + " (" + Math.round(bid - marginalCost) + ")");
        bid = 0;
        candidate = null;
    }

    @Override
    public Long askPrice(Task task) {
        candidate = Planning.addAndSimulate(current, task, 1000);
        marginalCost = candidate.getCost() - current.getCost();

        double minCost = task.pickupCity.distanceTo(task.deliveryCity) * minCostPerKm;

        // Our best vs the others' best
        bid = Math.round(Math.min(marginalCost, minCost - reward));
        // To to win back what we've lost
        bid = Math.max(bid, -reward);
        // But never work for less than the others may, we are making an
        // educated guess here. Nothing more.
        bid = Math.max(bid, Math.round(minCost - 1));
        // The tax
        bid += 1;
        return bid;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planning solution = new Planning(current, vehicles, tasks);
        log.info("["+ agent.id() + "] €" + reward);
        return solution.toList();
    }
}
