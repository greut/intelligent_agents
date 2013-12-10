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

    private double minCost;
    private Planning otherCandidate;
    private Planning otherCurrent;
    private int otherReward;
    private double otherMarginalCost;

    // the prediction difference
    private double diff;

    // The learning phase size
    private int rounds = 9;
    private City[] cities;
    private double[][] segments;
    private double[][] distances;
    private double[][] expectations;

    // Learning structures
    private double capacity;
    private double costPerKm;
    private double minCostPerKm;
    private int counters[][];
    private double costs[][];

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

        cities = new City[topology.size()];
        segments = new double[topology.size()][topology.size()];
        distances = new double[topology.size()][topology.size()];
        expectations = new double[topology.size()][topology.size()];
        counters = new int[topology.size()][topology.size()];
        costs = new double[topology.size()][topology.size()];

        init();
    }

    private void init() {
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

                // Deform the reality about the map, so the less accessible
                // parts of the graph are reflected in our estimation. The
                // rational behind this is that it's very likely that we will
                // have to travel back from such locations.
                //
                // /!\ 3 and 1.5 are magic numbers.
                double distance = from.distanceTo(to);
                switch (to.neighbors().size()) {
                    case 1:
                        distance *= 3;
                        break;
                    case 2:
                        distance *= 1.5;
                        break;
                    default:
                        break;
                }
                distances[from.id][to.id] = distance;
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
            double distance = distances[from.id][to.id];
            double e = expectations[from.id][to.id];
            double cost = Math.ceil((e * task.weight) / capacity) / e;
            cost *= distance;
            price += cost * costPerKm;
            from = to;
        }
        return price;
    }

    private void learn(Task task, long bid) {
        double distance = task.pickupCity.distanceTo(task.deliveryCity);
        double value = bid / distance;
        City from = task.pickupCity;
        for (City to : from.pathTo(task.deliveryCity)) {
            double cost = costs[from.id][to.id];
            int r = counters[from.id][to.id];
            cost = (cost * r) + value;
            cost /= (r + 1);
            costs[from.id][to.id] = cost;
            counters[from.id][to.id] += 1;

            from = to;
        }
        costPerKm = (costPerKm * round) + value;
        costPerKm /= round + 1;
    }

    private double getEstimateOtherCost(Task task) {
        City from = task.pickupCity;
        double price = 0;
        for (City to : from.pathTo(task.deliveryCity)) {
            double distance = from.distanceTo(to);
            double cpk = counters[from.id][to.id] == 0 ?
                    costPerKm :
                    costs[from.id][to.id];
            double p = cpk * distance;
            price += p;
            from = to;
        }
        return price;
    }

    @Override
    public Long askPrice(Task task) {
        super.askPrice(task);

        // the assuming no movements
        minCost = task.pickupCity.distanceTo(task.deliveryCity) * minCostPerKm;

        //otherCandidate = Planning.addAndSimulate(otherCurrent, task);
        //otherMarginalCost = otherCandidate.getCost() - otherCurrent.getCost();

        double cost = getEstimateCost(task);
        double otherCost = getEstimateOtherCost(task);

        // Those estimation are not usually bad.
        /*
        System.err.println(task);
        System.err.println("Bid: " + Math.round(cost));
        System.err.println("Marginal cost: " + Math.round(marginalCost));
        System.err.println("Reward: " + reward);
        System.err.println("Other bid: " + Math.round(otherCost));
        System.err.println("Other marginal cost: " + Math.round(otherMarginalCost));
        System.err.println("Other reward: " + otherReward);
        System.err.println("");
        */

        // Huey
        cost = Math.round(Math.min(cost, Math.max(marginalCost, minCost)));
        // Louie
        cost = (otherCost + cost) / 2.;

        // 3 cases:
        //   we are winning by a fair margin, try to improve our profit
        //   we are losing by a big bit, try to mimic our opponent
        //   else, keep our current strategy
        /*
        double diff;
        if (reward > (otherReward * 1.2)) {
            diff = Math.max(otherCost, cost) - cost;
            // The other guy is acting rationally
            if (diff > 0) {
                // Let's bid between our value and his.
                cost += diff / 2.;
            }
            // The tax
            cost += 1;
        } else if (reward < (otherReward * .8)) {
            // Steal the others' strategy if we are loosing too much.
            diff = Math.min(otherCost, cost) - otherCost;
            cost -= diff / 2.;
        }
        // else: stay agressive
        */
        // The tax
        bid = Math.round(cost + 1);
        return bid;
    }

    @Override
    public void auctionResult(Task previous, int winner, Long[] bids) {
        super.auctionResult(previous, winner, bids);

        // The other
        for (long b : bids) {
            if (b != bid) {
                learn(previous, b);
                round += 1;

                /*
                double d = b - otherMarginalCost;
                if (winner != agent.id()) {
                    otherReward += (b - otherMarginalCost);
                    otherCurrent = otherCandidate;
                    otherCandidate = null;
                }
                // AVG difference of guess
                diff = ((round * diff) + d) / (double) (round + 1);
                log.info("guess made: " + d + " " + diff);
                */
            }
        }
    }
}
