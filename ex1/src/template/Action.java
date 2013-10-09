package template;

import logist.topology.Topology.City;

/**
 * The action an agent can take.
 *
 * The action is either to accept the task (DELIVERY) or to move to the task's
 * city.
 */
public class Action {
    private City destination;
    public static Action DELIVERY = new Action(null);

    public Action(City to) {
        destination = to;
    }

    public String toString() {
        return String.format("<Action: \"%s\">",
                isDelivery() ? "DELIVERY" : "-> " + destination);
    }

    public City getCity() {
        return destination;
    }

    public boolean equals(Object other) {
        Action o = (Action) other;
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
