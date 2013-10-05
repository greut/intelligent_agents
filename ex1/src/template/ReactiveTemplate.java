package template;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

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

public class ReactiveTemplate implements ReactiveBehavior {

    private ArrayList<State> states;
    private ArrayList<City> actions;
    private double[][] rewards;
    private double[][][] transitions;

    // define best action structure and relative value
    private HashMap<State, City> best;
    private double[] v;

    @Override
    public void setup(Topology topology, TaskDistribution td, Agent agent) {

        // Reads the discount factor from the agents.xml file.
        // If the property is not present it defaults to 0.95
        Double discount = agent.readProperty("discount-factor", Double.class,
                0.95);

        // from the probability and rewards of every task,
        // p = td.probability(from,to);
        // r = td.reward(from, to);
        if(agent.vehicles().size() == 0)
            return;

        // define states
        states = new ArrayList<State>();
        for(City from : topology.cities()) {
            for(City to : topology.cities()) {
                states.add(new State(from, to));
            }
        }

        // define actions
        actions = new ArrayList<City>();
        for(City c : topology.cities())
            actions.add(c);

        // define rewards
        rewards = new double[states.size()][actions.size()];
        for(int s = 0; s < states.size(); s++) {
            for(int a = 0; a < actions.size(); a++) {
                State objs = states.get(s);
                City obja = actions.get(a);
                if(objs.notask()) {
                    // cannot pick delivery, set reward as "-infinite"
                    rewards[s][a] = Double.MIN_VALUE;

                } else if(obja == objs.getFutureCity()) {
                    // delivery -> reward = reward - cost
                    rewards[s][a] = td.reward(objs.getCurrentCity(), objs.getFutureCity());
                    rewards[s][a] -= objs.getCurrentCity().distanceTo(objs.getFutureCity()) * agent.vehicles().get(0).costPerKm();
                } else {
                    // simple move -> reward = cost only
                    rewards[s][a] = -objs.getCurrentCity().distanceTo(obja) * agent.vehicles().get(0).costPerKm();
                }
            }
        }

        // define transitions
        transitions = new double[states.size()][actions.size()][states.size()];
        for(int s = 0; s < states.size(); s++) {
            for(int a = 0; a < actions.size(); a++) {
                for(int s1 =  0; s1 < states.size(); s1++) {
                    State objs = states.get(s);
                    City obja = actions.get(a);
                    State objs1 = states.get(s1);

                    // TODO: can't stay in the same place nor pickup if there is no task

                    // if delivery
                    if(obja == objs1.getCurrentCity() && obja != objs.getCurrentCity()) {
                        // if a == s.dest && s.dest != s.current -> delivery
                        // if a != s.dest && s1.current == a -> simple movement
                        transitions[s][a][s1] = td.probability(objs1.getCurrentCity(), objs1.getFutureCity());
                    }
                    else {
                        transitions[s][a][s1] = 0;
                    }
                }
            }
        }

        best = new HashMap<State, City>(states.size());
        v = new double[states.size()];
        // run reinforcement learning algorithm
        for(int i = 0; i < states.size(); i++) v[i] = 1; // initialize v
        double epsilon = 0.001; // error
        double max_error = 1;
        while(max_error > epsilon) {
            
            max_error = 0;
            double[][] q = new double[states.size()][actions.size()];
            
            for(int s = 0; s < states.size(); s++) {
                for(int a = 0; a < actions.size(); a++) {
                    double vs1 = 0;
                    for(int s1 = 0; s1 < states.size(); s1++) {
                        vs1 += transitions[s][a][s1] * v[s1];
                    }
                    q[s][a] = rewards[s][a] + discount * vs1;
                }

                // best action initialized to first valid action for that state
                int max;
                for(int a = 0; a < actions.size(); a++) {
                    if(states.get(s).getCurrentCity().hasNeighbor(actions.get(a)))
                        max = a;
                }
                // check the best action in all possible actions for that state
                for(int a = max + 1; a < actions.size(); a++) {
                    if(states.get(s).getCurrentCity().hasNeighbor(actions.get(a)) && q[s][a] > q[s][max])
                        max = a;
                    
                }
                best.put(states.get(s), actions.get(max));
                // check the error and updates v
                double serror = Math.abs(q[s][max] - v[s]);
                v[s] = q[s][max];

                // update and check maximum error
                if(serror > max_error)
                    max_error = serror;
            }
        }
    }

    @Override
    public Action act(Vehicle vehicle, Task availableTask) {
        Action action;

        State currentState = null;
        if(availableTask == null) {
            // no task -> state (current = dest)
            for(State s : states)
                if(s.getCurrentCity() == vehicle.getCurrentCity() && s.notask())
                    currentState = s;
        } else {
            // task -> state(current, task_destination)
            for(State s : states)
                if(s.getCurrentCity() == vehicle.getCurrentCity() && s.getFutureCity() == availableTask.deliveryCity)
                    currentState = s;
        }

        City next = best.get(currentState);
        if(next == currentState.getFutureCity())
            action = new Pickup(availableTask);
        else
            action = new Move(next);

        return action;
    }
}
