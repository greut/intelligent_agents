package g16;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
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
 * A mix between Paperino (for our predictions) and Picsou to take into account
 * what it does cost to the other player (by having a same environment) as ours.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @see g16.AuctionPaperino
 * @see g16.AuctionPicsou
 */
public class AuctionDewey extends AuctionBentina {

    private double costPerKm;
    private double minCostPerKm;
    private double capacity;

    private Planning otherCandidate;
    private Planning otherCurrent;
    private int otherReward;
    private double otherMinCost;
    private double otherMarginalCost;

    // the prediction difference
    private double diff;

    // Magic number
    private int rounds = 9;
    private City[] cities;
    private double[][] segments;
    private double[][] expectations;

    // Current round
    private int round = 0;

    @Override
    public void setup(Topology t, TaskDistribution td, Agent a) {
        super.setup(t, td, a);

        Vehicle big = a.vehicles().get(0);
        minCostPerKm = big.costPerKm();
        for (Vehicle v : a.vehicles()) {
            if (big.capacity() < v.capacity()) {
                big = v;
            }
            minCostPerKm = Math.min(minCostPerKm, v.costPerKm());
        }

        capacity = big.capacity();
        costPerKm = big.costPerKm();

        // Smartly estimating what the other is doing.
        otherCurrent = new Planning(agent.vehicles());
        otherCandidate = null;
        otherReward = 0;
        diff = 0;

        init();
    }

    private void init() {
        cities = new City[topology.size()];
        segments = new double[topology.size()][topology.size()];
        expectations = new double[topology.size()][topology.size()];
        double total = 0;

        for (City from : topology.cities()) {
            cities[from.id] = from;
            for (City to : topology.cities()) {
                if (from.equals(to)) {
                    continue;
                }
                double p = distribution.probability(from, to);
                City f = from;
                for (City t : from.pathTo(to)) {
                    segments[f.id][t.id] += p;
                    f = t;
                    total += p;
                }
            }
        }

        // Normalizing to 1 and computing expectation
        int n = rounds;
        for (int i=0; i < segments.length; i++) {
            for (int j=0; j < segments[i].length; j++) {
                segments[i][j] /= total;
                // Expectation
                double exp = 0;
                double p = segments[i][j];
                for (int k=1; p > 0 && k <= n; k++) {
                    double x = fact(n) / (fact(k) * fact(n - k));
                    x *= Math.pow(p, k) * Math.pow(1 - p, n - k);
                    exp += k * p;
                }
                expectations[i][j] = exp;
            }
        }
    }

    /**
     * Give a price prediction for the given task based on the expected
     * occurence of the path.
     *
     * @param task task to estimate
     * @return a fair price
     */
    private double getEstimateCost(Task task) {
        double price = 0;
        City from = task.pickupCity;
        for(City to : from.pathTo(task.deliveryCity)) {
            double distance = from.distanceTo(to);
            double e = expectations[from.id][to.id];
            double cost = Math.ceil((e * task.weight) / capacity) / e;
            cost *= distance;
            price += cost * costPerKm;
            from = to;
        }
        return price;
    }

    @Override
    public Long askPrice(Task task) {
        super.askPrice(task);

        // Other player, predicting the future bits ;-)
        otherMinCost = task.pickupCity.distanceTo(task.deliveryCity) * minCostPerKm;

        otherCandidate = Planning.addAndSimulate(otherCurrent, task);
        otherMarginalCost = otherCandidate.getCost() - otherCurrent.getCost();

        // The other starts winning money as long as he bids above:
        //  otherMarginalCost - otherReward.
        //
        // We are starting winning money when we bid is above:
        //  marginalCost - reward
        //
        // but our agressive "Paperino" strategy is to go with our estimated
        // cost if possible. Especially at the beginning when the state of the
        // game is relatively unknown.
        //
        //  min(cost, marginalCost - reward)
        //
        // Let the magic happen.


        // The other's most agressive (yet sane) move.
        otherMinCost = Math.min(otherMinCost, otherMarginalCost - otherReward);
        // Our agressive move
        double cost = getEstimateCost(task);

        // Let's do business
        if (otherMinCost > cost) {
            if (reward > 0) {
                // We iare on the good side, let's take risks
                cost += diff / 10;
            } else {
                // the tax
                cost -= diff / 10;
            }
        } else {
            // Go to the other's bid
            // No idea!
        }

        bid = Math.round(cost);
        System.err.println(task + " " + bid + " " + otherMarginalCost);
        return bid;
    }

    @Override
    public void auctionResult(Task previous, int winner, Long[] bids) {
        super.auctionResult(previous, winner, bids);
        double d = 0;

        // The other
        for (long b : bids) {
            if (b != bid) {
                d = b - otherMarginalCost;
                if (winner == agent.id()) {
                    otherReward += (b - otherMarginalCost);
                    otherCurrent = otherCandidate;
                    otherCandidate = null;
                }
                // AVG difference of guess
                diff = ((round * diff) + d) / (double) (round + 1);
                round += 1;
                log.info("guess made: " + d + " " + diff);
            }
        }
    }
}
