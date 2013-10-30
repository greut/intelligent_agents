package template;

import java.util.AbstractSet;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArraySet;

import logist.plan.Action;
import logist.task.Task;
import logist.task.TaskSet;
import logist.topology.Topology.City;

import template.heuristic.StateComparator;


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
     * Value of the loaded tasks.
     */
    private double loadedValue;
    /**
     * Value of the delivered tasks;
     */
    private double deliveredValue;
    /**
     * What actions has been taken on in the past.
     */
    private Action seed;
    /**
     * Who's your daddy
     */
    private State parent;
    /**
     * comparator function to sort good from bad states.
     */
    private StateComparator comparator;

    // Memoization
    private int maxDistance = -1;
    private int minDistance = -1;

    /**
     * The initial state.
     *
     * @param currentCity     initial city
     * @param storageCapacity storage capacity
     * @param costPerKm       cost per km
     * @param readyTasks      available tasks
     * @param loadedTasks     loaded tasks
     * @param g               the comparator to be sorted with
     */
    public State(City currentCity, int storageCapacity, int costPerKm,
            TaskSet readyTasks, TaskSet loadedTasks, StateComparator g) {
        this(storageCapacity, costPerKm);
        position = currentCity;
        distance = 0;
        ready = new CopyOnWriteArraySet<Task>(readyTasks);
        loaded = new CopyOnWriteArraySet<Task>(loadedTasks);
        seed = null;
        parent = null;
        comparator = g;
        loadedValue = 0;
        deliveredValue = 0;
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
        comparator = s.comparator;
        loadedValue = s.loadedValue;
        deliveredValue = s.deliveredValue;
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

    /**
     * The total travelled distance.
     *
     * @return the traveled distance.
     */
    public int getDistance() {
        return distance;
    }

    /**
     * The upper-bound of what need to be travelled.
     *
     * @return maximum travelling distance from the current state.
     */
    public int getMaxDistanceToGoal() {
        if (maxDistance == -1) {
            int upper = 0;
            for (Task t: ready) {
                upper += position.distanceTo(t.pickupCity);
                upper += t.pickupCity.distanceTo(t.deliveryCity);
            }
            for (Task t: loaded) {
                upper += position.distanceTo(t.deliveryCity);
            }
            maxDistance = upper;
        }
        return maxDistance;
    }

    /**
     * The lower-bound of what moves are needed to finish.
     *
     * @return minimum travelling distance from the current state.
     */
    public int getMinDistanceToGoal() {
        if (minDistance == -1) {
            int longest = 0;
            for (Task t: ready) {
                longest = Math.max(longest,
                        (int) (position.distanceTo(t.pickupCity) +
                        t.pickupCity.distanceTo(t.deliveryCity)));
            }
            for (Task t: loaded) {
                longest = Math.max(longest, (int) position.distanceTo(t.deliveryCity));
            }
            minDistance = longest;
        }
        return minDistance;
    }

    /**
     * The rewards minus the costs.
     *
     * @return the remaining money so far.
     */
    public double getBalance() {
        return deliveredValue - (distance * cost);
    }

    /**
     * Value of what's carried.
     *
     * @return potential money we have.
     */
    public double getLoadedReward() {
        return loadedValue;
    }

    /**
     * Value of what's been delivered.
     *
     * @return money made.
     */
    public double getDeliveredReward() {
        return deliveredValue;
    }

    /**
     * How deep in the exploration tree are we.
     *
     * @return total number of actions till here.
     */
    public int getDepth() {
        int depth;
        State p;
        for (depth=0, p=parent; p != null; p=p.parent, depth++);
        return depth;
    }

    /**
     * Return all possible actions from this state.
     */
    public Deque<Step> steps() {
        Deque<Step> q = new LinkedList<Step>();
        for (Task task : loaded) {
            if (task.deliveryCity.equals(position)) {
                q.add(new Step(task, Step.Actions.DELIVERY));
            }
        }
        if (!q.isEmpty()) {
            return q;
        }
        for (Task task : ready) {
            if (task.pickupCity.equals(position) && task.weight <= capacityLeft()) {
                q.add(new Step(task, Step.Actions.PICKUP));
            }
        }
        if (!q.isEmpty()) {
            return q;
        }
        for (City neighbor : position.neighbors()) {
            if (parent == null || !neighbor.equals(parent.position)) {
                q.add(new Step(neighbor));
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
                break;
            case PICKUP:
                // clone only when required
                s.ready = new CopyOnWriteArraySet<Task>(s.ready);
                s.loaded = new CopyOnWriteArraySet<Task>(s.loaded);
                s.ready.remove(step.task);
                s.loaded.add(step.task);
                s.loadedValue += step.task.reward;
                break;
            case DELIVERY:
                s.loaded = new CopyOnWriteArraySet<Task>(s.loaded);
                s.loaded.remove(step.task);
                s.loadedValue -= step.task.reward;
                s.deliveredValue += step.task.reward;
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
                " € " + getBalance() + " (" +
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
        return comparator.compare(this, o);
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
