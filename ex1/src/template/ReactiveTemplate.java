package template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
import logist.plan.Action;
import logist.plan.Action.Move;
import logist.plan.Action.Pickup;
import logist.task.Task;
import logist.task.TaskDistribution;
import logist.topology.Topology;
import logist.topology.Topology.City;


/**
 * The reactive agent.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 * @author Tiziano Signo <tiziano.signo@epfl.ch>
 */
public class ReactiveTemplate implements ReactiveBehavior {

    /**
     * Disount factor.
     */
    private static double DEFAULT_DISCOUNT = 0.95;
    /**
     * When to stop computing the optimal road.
     */
    private static double DEFAULT_EPSILON = 0.01;

    // Set of states: S
    private State[] states;
    // Set of actions: A
    private Act[] actions;
    // Set of rewards: R(s, a)
    private double[][] rewards;
    // Set of transistions: T(s, a, s')
    private double[][][] transitions;

    // define best action structure and relative value: B(s) -> a
    private int[] best;
    // V(s) -> N
    private double[] values;

    /**
     * Initiate the states.
     *
     * @param cities the list of all the cities from the topology.
     * @return a list of states
     * @see template.State
     */
    private static List<State> buildStateList(List<City> cities) {
        List<State> list = new ArrayList<State>();
        for (City current: cities) {
            list.add(new State(current));
            for (City future: cities) {
                if (!current.equals(future)) {
                    list.add(new State(current, future));
                }
            }
        }
        return list;
    }

    /**
     * Initiate the actions.
     *
     * @param cities the list of all cities from the topology.
     * @return a list of actions
     */
    private static List<Act> buildActList(List<City> cities) {
        List<Act> list = new ArrayList<Act>();
        list.add(Act.DELIVERY);
        for (City city : cities) {
            list.add(new Act(city));
        }
        return list;
    }

    /**
     * Compute the reward table.
     *
     * @param td the task distribution object for the `reward` function.
     * @param costPerKm the fuel cost
     * @see logist.task.TaskDistribution.reward()
     */
    protected void computeRewards(TaskDistribution td, double costPerKm) {
        State state;
        Act action;
        double distance;
        double reward;
        for (int s=0; s < states.length; s++) {
            state = states[s];
            for (int a=0; a < actions.length; a++) {
                action = actions[a];
                reward = 0;
                distance = 0;

                if (action.isDelivery()) { // Deliver
                    if (state.hasTask()) {
                        reward = (double) td.reward(state.getCurrentCity(),
                                state.getFutureCity());
                        distance = state.getDistance();
                    } else {
                        // We shall not use this combination, ever since there
                        // is nothing to deliver from here.
                        reward = Double.MIN_VALUE;
                    }
                } else { // Move
                    if (state.getCurrentCity().hasNeighbor(action.getCity())) {
                        distance = state.getDistance(action);
                    } else {
                        // Do no try to jump directly to non-adjacent cities.
                        reward = Double.MIN_VALUE;
                    }
                }
                rewards[s][a] = reward - distance * costPerKm;
            }
        }
    }

    /**
     * Compute the transition table.
     *
     * @param td the task distribution object the `probability` function.
     * @see logist.task.TaskDistribution.probability()
     */
    protected void computeTransitions(TaskDistribution td) {
        State state;
        Act action;
        State statePrime;
        double p;
        for (int s=0; s < states.length; s++) {
            state = states[s];
            for (int a=0; a < actions.length; a++) {
                action = actions[a];
                for (int s2=0; s2 < states.length; s2++) {
                    statePrime = states[s2];
                    p = 0;

                    // Keep only valid states:
                    //  - if there is a task, then the action must be the
                    //    delivery one and the s' current city the destination
                    //    city of the current state
                    //  - if there is no tasks, then the next city must be a
                    //    neighbor of the current one. It's illegal to jump to
                    //    other cities (and it makes sense).
                    if ((state.hasTask() && action.isDelivery() &&
                            state.getFutureCity().equals(statePrime.getCurrentCity())) ||
                        (!state.hasTask() && action.isMove() &&
                            action.getCity().equals(statePrime.getCurrentCity()) &&
                            state.getCurrentCity().hasNeighbor(action.getCity()))
                    ) {
                        p = td.probability(statePrime.getCurrentCity(),
                            statePrime.getFutureCity());
                    }

                    transitions[s][a][s2] = p;
                }
            }
        }
    }

    /**
     * Run the reinforcement learning algorithm.
     *
     * @param discount the discount factor
     * @param epsilon the error threshold
     */
    protected void reinforcementLearning(double discount, double epsilon) {
        int max;
        double vs, error;
        double max_error = 2 * epsilon;
        double[][] q = new double[states.length][actions.length];

        while (max_error > epsilon) {
            max_error = 0;
            for (int s=0; s < states.length; s++) {
                for (int a=0; a < actions.length; a++) {
                    vs = 0;
                    for (int s2 = 0; s2 < states.length; s2++) {
                        vs += transitions[s][a][s2] * values[s2];
                    }
                    q[s][a] = rewards[s][a] + discount * vs;
                }

                max = 0;
                for (int a=0; a < q[s].length; a++) {
                    if (q[s][a] >= q[s][max]) {
                        max = a;
                    }
                }

                best[s] = max;
                error = Math.abs(q[s][max] - values[s]);
                values[s] = q[s][max];
                max_error = Math.max(error, max_error);
            }
        }
    }

    public void setup(Topology topology, TaskDistribution td, Agent agent) {
        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        double discount = (double) agent.readProperty("discount-factor",
                Double.class, DEFAULT_DISCOUNT);
        double epsilon = DEFAULT_EPSILON;

        // define states
        List<State> ts = buildStateList(topology.cities());
        states = new State[ts.size()];
        states = ts.toArray(states);

        // define actions
        List<Act> ta = buildActList(topology.cities());
        actions = new Act[ta.size()];
        actions = ta.toArray(actions);

        // define rewards
        rewards = new double[states.length][actions.length];
        computeRewards(td, agent.vehicles().get(0).costPerKm());

        // define transitions
        transitions = new double[states.length][actions.length][states.length];
        computeTransitions(td);

        best = new int[states.length];
        values = new double[states.length];

        reinforcementLearning(discount, epsilon);
    }

    public Action act(Vehicle vehicle, Task availableTask) {
        State state = new State(vehicle.getCurrentCity(),
                availableTask != null ? availableTask.deliveryCity : null);
        int s = Arrays.asList(states).indexOf(state);


        int next = best[s];
        Action nextAction = actions[next].isDelivery() ?
            new Pickup(availableTask) :
            new Move(actions[next].getCity());

        System.err.println("[Reactive] " + state + " >> " + nextAction);
        return nextAction;
    }
}
