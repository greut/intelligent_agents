package template;

import logist.topology.Topology.City;

/**
 * The action an agent can take.
 *
 * The action is either to accept the task (DELIVERY) or to move to the task's
 * city.
 */
public class Act {
    private City destination;
    public static Act DELIVERY = new Act(null);

    public Act(City to) {
        destination = to;
    }

    public String toString() {
        return String.format("<Act: %s>",
                isDelivery() ? "DELIVERY" : "-> " + destination);
    }

    public City getCity() {
        return destination;
    }

    public boolean equals(Object other) {
        Act o = (Act) other;
        return destination != null ?
                destination.equals(o.destination) :
                destination == o.destination;
    }

    public boolean isDelivery() {
        return destination == null;
    }

    public boolean isMove() {
        return destination != null;
    }
}
