package template;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArraySet;

import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;


/**
 * State of the deliberative agent.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class State implements Comparable<State> {
    /**
     * Position of the agent.
     */
    private City position;
    /**
     * What we can carry in total.
     */
    private int capacity;
    /**
     * What travelling costs.
     */
    private int cost;
    /**
     * Total distance travelled.
     */
    private int distance;
    /**
     * Tasks that are ready to be taken.
     */
    private AbstractSet<Task> ready;
    /**
     * Tasks that where picked up by the agent.
     */
    private AbstractSet<Task> loaded;
    /**
     * Money!! Tons of money (hence `double`)
     */
    private double balance;
    /**
     * What actions has been taken on in the past.
     */
    private Action seed;
    /**
     * Who's your daddy
     */
    private State parent;

    /**
     * The initial state.
     *
     * @param currentCity     initial city
     * @param storageCapacity storage capacity
     * @param costPerKm       cost per km
     * @param readyTasks      inial tasks
     */
    public State(City currentCity, int storageCapacity, int costPerKm, TaskSet readyTasks) {
        this(storageCapacity, costPerKm);
        position = currentCity;
        distance = 0;
        ready = new CopyOnWriteArraySet<Task>(readyTasks);
        loaded = new CopyOnWriteArraySet<Task>();
        balance = 0;
        seed = null;
        parent = null;
    }

    private State(int storageCapacity, int costPerKm) {
        capacity = storageCapacity;
        cost = costPerKm;
    }

    private State(State s) {
        this(s.capacity, s.cost);
        position = s.position;
        distance = s.distance;
        ready = s.ready;
        loaded = s.loaded;
        balance = s.balance;
    }

    public City getPosition() {
        return position;
    }

    public int capacityLeft() {
        int left = capacity;
        for(Task t : loaded) {
            left -= t.weight;
        }
        return left;
    }

    public double getBalance() {
        return balance;
    }

    public int getDepth() {
        int depth;
        State p;
        for (depth=0, p=parent; p != null; p=p.parent, depth++);
        return depth;
    }

    /**
     * Return all possible actions from this state.
     */
    public PriorityQueue<Step> steps() {
        PriorityQueue<Step> q = new PriorityQueue<Step>();
        for (City neighbor : position.neighbors()) {
            q.add(new Step(neighbor, - position.distanceTo(neighbor) * cost));
        }
        for (Task task : ready) {
            if (task.pickupCity.equals(position) && task.weight <= capacityLeft()) {
                q.add(new Step(task));
            }
        }
        for (Task task : loaded) {
            if (task.deliveryCity.equals(position)) {
                q.add(new Step(task, task.reward));
            }
        }
        return q;
    }

    /**
     * Generate the next state obtained from this one after the given step has
     * been taken.
     *
     * @param step the step to take.
     * @return     a new state.
     */
    public State apply(Step step) {
        State s = new State(this);
        s.seed = step.toAction();
        s.parent = this;
        int i;
        switch (step.type) {
            case MOVE:
                s.distance += position.distanceTo(step.destination);
                s.position = step.destination;
                s.balance -= position.distanceTo(step.destination) * cost;
                break;
            case PICKUP:
                // clone only when required
                s.ready = new CopyOnWriteArraySet<Task>(s.ready);
                s.loaded = new CopyOnWriteArraySet<Task>(s.loaded);
                s.ready.remove(step.task);
                s.loaded.add(step.task);
                break;
            case DELIVERY:
                s.loaded = new CopyOnWriteArraySet<Task>(s.loaded);
                s.loaded.remove(step.task);
                s.balance += step.task.reward;
                break;
        }
        return s;
    }

    /**
     * Tell if this state is a goal state or not.
     *
     * @return true if nothing to be done remain.
     */
    public boolean isFinal() {
        return ready.isEmpty() && loaded.isEmpty();
    }

    /**
     * Search for a loop in the exploration graph.
     *
     * @return true is this state already existed in this history.
     */
    public boolean hasLoop() {
        State s = parent;
        while (s != null && s.ready.size() == ready.size()) {
            if (s.equals(this)) {
                return true;
            }
            s = s.parent;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("<State \"" + position + "\" " +
                " € " + balance + " (" +
                loaded.size() + "/" + ready.size() +
                ")>");
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof State)) {
            return false;
        }
        if (other == this) {
            return true;
        }
        State o = (State) other;
        if (!position.equals(o.position)) {
            return false;
        }
        if (!ready.equals(o.ready)) {
            return false;
        }
        if (!loaded.equals(o.loaded)) {
            return false;
        }
        return true;
    }

    public int compareTo(State o) {
        return Double.compare(o.balance, balance);
    }

    /**
     * Builds the plan that reaches this state.
     *
     * @return iterator for each action.
     */
    public Iterator<Action> planIterator() {
        LinkedList<Action> actions = new LinkedList<Action>();
        State s = this;
        while (s.parent != null) {
            actions.add(s.seed);
            s = s.parent;
        }
        return actions.descendingIterator();
    }
}
