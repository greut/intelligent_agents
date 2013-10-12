package template;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import logist.simulation.Vehicle;
import logist.agent.Agent;
import logist.behavior.ReactiveBehavior;
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
    private Action[] actions;
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
    private static List<Action> buildActionList(List<City> cities) {
        List<Action> list = new ArrayList<Action>();
        list.add(Action.DELIVERY);
        for (City city : cities) {
            list.add(new Action(city));
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
        Action action;
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
                        reward = Double.NEGATIVE_INFINITY;
                    }
                } else { // Move
                    if (state.getCurrentCity().hasNeighbor(action.getCity())) {
                        distance = state.getDistance(action);
                    } else {
                        // Do no try to jump directly to non-adjacent cities.
                        reward = Double.NEGATIVE_INFINITY;
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
        Action action;
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
        int max, steps = 0;
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
                for (int a=1; a < q[s].length; a++) {
                    if (q[s][a] >= q[s][max]) {
                        max = a;
                    }
                }

                best[s] = max;
                error = Math.abs(q[s][max] - values[s]);
                values[s] = q[s][max];
                max_error = Math.max(error, max_error);
            }
            steps++;
        }
        System.err.println(steps + " steps to converge.");
    }

    protected void outputGraphviz(String path) throws IOException {
        File f = new File(path);
        FileWriter fp = new FileWriter(f);
        // Building the data structure
        // ---------------------------
        // City -> City -> counter
        // -1: route that does not exist
        // 0: no arc
        // 1+: default
        HashMap<City,Map<City,Integer>> graph = new HashMap<City,Map<City,Integer>>();
        Map<City,Integer> counter;

        for(int s=0; s < best.length; s++) {
            State state = states[s];
            City city = state.getCurrentCity();
            City next;
            Action action = actions[best[s]];
            int value = 0;
            if(!graph.containsKey(city)) {
                graph.put(city, new HashMap<City, Integer>());
            }
            counter = graph.get(city);
            next = action.getCity();
            if (state.hasTask() && !action.isDelivery()) {
                value = -1;
            } else if (!state.hasTask()) {
                value = 1;
            }

            if (next != null) {
                if(!counter.containsKey(next)) {
                    counter.put(next, value);
                } else {
                    counter.put(next, value);
                }
            }
        }

        // Output the graph
        // ----------------
        // Display: the preferred route (when there is no tasks)
        //          the routes are discarded (even if a task exist)
        fp.write("digraph G {\n");
        for (Map.Entry<City,Map<City,Integer>> curr: graph.entrySet()) {
            for (Map.Entry<City,Integer> next: curr.getValue().entrySet()) {
                if (next.getValue() != 0) {
                    fp.write("\"" + curr.getKey() + "\" -> \"" +
                        next.getKey() + "\" [style=" +
                        ((next.getValue() == 1) ?
                            "solid" :
                            ((next.getValue() == -1) ?
                                "dotted" : "invisible")) + "]\n");
                }
            }
        }
        fp.write("}\n");
        fp.close();
        System.err.println(path + " has been written with graph.");
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
        List<Action> ta = buildActionList(topology.cities());
        actions = new Action[ta.size()];
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
        try {
            outputGraphviz("graph.dot");
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public logist.plan.Action act(Vehicle vehicle, Task availableTask) {
        State state = new State(vehicle.getCurrentCity(),
                availableTask != null ? availableTask.deliveryCity : null);
        int s = Arrays.asList(states).indexOf(state);
        int next = best[s];

        logist.plan.Action nextAction = actions[next].isDelivery() ?
            new Pickup(availableTask) :
            new Move(actions[next].getCity());

        System.err.println("[Reactive] " + state + " >> " + nextAction);
        return nextAction;
    }
}
