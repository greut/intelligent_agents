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
 * Abstract auction agent with some boilerplates
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public abstract class AuctionBentina implements AuctionBehavior {

    protected Topology topology;
    protected TaskDistribution distribution;
    protected Agent agent;
    protected Logger log;

    /**
     * Current best plan.
     */
    protected Planning current;
    /**
     * Candidate plan for the bid in progress.
     */
    protected Planning candidate;
    /**
     * Current marginal cost.
     */
    protected double marginalCost;
    /**
     * Current bid made.
     */
    protected long bid;
    /**
     * Current reward.
     */
    protected long reward;

    @Override
    public void setup(Topology t, TaskDistribution td, Agent a) {
        log = Logger.getLogger(this.getClass().getName());

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
            status = "win";
        } else {
            status = "lost";
        }

        log.info("[" +agent.id() + "] " + status + "\t" + bid + " (" + Math.round(bid - marginalCost) + ")");
    }

    @Override
    public Long askPrice(Task task) {
        candidate = Planning.addAndSimulate(current, task);
        marginalCost = candidate.getCost() - current.getCost();
        return Math.round(marginalCost);
    }

    @Override
    public List<Plan> plan(List<Vehicle> vehicles, TaskSet tasks) {
        Planning solution = new Planning(current, vehicles, tasks);
        log.info("["+ agent.id() + "] €" + reward);
        return solution.toList();
    }

    /**
     * x! = x(x-1)(x-2)(x-3)...1
     *
     * @param x
     * @return x!
     */
    static protected long fact(int x) {
        long fact = 1;
        for (long i = 1; i <= x; i++) {
            fact *= i;
        }
        return fact;
    }
}
