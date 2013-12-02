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
 * A learning agent that will observe how the others are playing and adjust its
 * price to it. It's a pure learner, it does not even look at the marginal cost
 * and throws away its initial estimation after round 0.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @see g16.AuctionGreedy
 */
public class AuctionLouie implements AuctionBehavior {

    private Topology topology;
    private TaskDistribution distribution;
    private Agent agent;
    private Logger log;

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

    private double costPerKm;
    private double costs[][];

    int round = 0;

    @Override
    public void setup(Topology t, TaskDistribution td, Agent a) {
        log = Logger.getLogger(AuctionLouie.class.getName());

        topology = t;
        distribution = td;
        agent = a;

        current = new Planning(agent.vehicles());
        candidate = null;
        bid = 0;
        reward = 0;

        init();
    }

    private void init() {
        costs = new double[topology.size()][topology.size()];

        costPerKm = Integer.MAX_VALUE;
        for (Vehicle vehicle : agent.vehicles()) {
            costPerKm = Math.min(costPerKm, vehicle.costPerKm());
        }

        for (City from : topology.cities()) {
            for (City to : topology.cities()) {
                if (from.equals(to)) {
                    continue;
                }
                // cost per unit
                costs[from.id][to.id] = -1;
            }
        }
    }

    private void learn(Task task, long bid) {
        double distance = task.pickupCity.distanceTo(task.deliveryCity);
        double value = bid / distance;
        City from = task.pickupCity;
        for (City to : from.pathTo(task.deliveryCity)) {
            double cost = costs[from.id][to.id];
            cost = (cost * round) + value;
            cost /= (round + 1);
            costs[from.id][to.id] = cost;
            from = to;
        }
        costPerKm = (costPerKm * round) + value;
        costPerKm /= round + 1;
    }

    private double estimate(Task task) {
        City from = task.pickupCity;
        double price = 0;
        for (City to : from.pathTo(task.deliveryCity)) {
            double distance = from.distanceTo(to);
            double cpk = costs[from.id][to.id] < 0 ?
                    costPerKm :
                    costs[from.id][to.id];
            double p = cpk * distance;
            price += p;
            from = to;
        }
        return price;
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
        for (long b : bids) {
            if (b != bid) {
                learn(previous, b);
            }
        }

        log.info("[" +agent.id() + "] " + status + "\t" + bid + " (" + Math.round(bid - marginalCost) + ")");
        bid = 0;
        candidate = null;
        round += 1;
    }

    @Override
    public Long askPrice(Task task) {
        candidate = Planning.addAndSimulate(current, task);
        marginalCost = candidate.getCost() - current.getCost();

        double estimation = estimate(task);

        // The tax
        bid = Math.round(estimation) + 1;
        return bid;
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planning solution = new Planning(current, vehicles, tasks);
        log.info("["+ agent.id() + "] €" + reward);
        return solution.toList();
    }
}
