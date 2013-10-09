package template;

import logist.topology.Topology.City;

/**
 * The state the agent can be in.
 *
 * A state is represented as: (current city, future city)
 *
 * The current city represents where the agent sits while future city tells
 * where the available task is willing to lead it. When there are no tasks,
 * then the state only has a current city.
 */
public class State {
    /**
     * Where the agent is.
     */
    private City current;

    /**
     * Where the task available in the cgturrent city will lead the agent to.
     */
    private City future;

    public State(City from) {
        this(from, null);
    }

    public State(City from, City to) {
        current = from;
        future = to;
    }

    public City getCurrentCity() {
        return current;
    }

    public City getFutureCity() {
        return future;
    }

    public double getDistance() {
        return current.distanceTo(future);
    }

    public double getDistance(Action action) {
        return current.distanceTo(action.getCity());
    }

    public boolean hasTask() {
        return future != null && !current.equals(future);
    }

    public String toString() {
        return String.format("<State: %s %s>", current, hasTask() ?
                "-> " + future : "no tasks");
    }

    public boolean equals(Object other) {
        State o = (State) other;
        return current.equals(o.current) && (future != null ? future.equals(o.future) : o.future == null);
    }
}
