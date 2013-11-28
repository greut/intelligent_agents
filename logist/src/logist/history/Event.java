package logist.history;

import logist.plan.Action;
import logist.plan.ActionHandler;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;

import static logist.history.Event.Type.*;

/**
 * An simulation event is an action carried out by a vehicle at a particular
 * time.
 * 
 * @author Robin Steiger
 */
public class Event {
    enum Type {
        MOVE, DELIVER, PICKUP;
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    // final int id;
    final Type type;
    final long time;
    final Vehicle vehicle;
    final City city;
    final Task task;

    private Event(Type type, long time, Vehicle vehicle, City city,
            Task task) {
        // this.id = id;
        this.type = type;
        this.time = time;
        this.vehicle = vehicle;
        this.city = city;
        this.task = task;
        // this.agent = agent;
    }

    public static Event arrive(long time, Vehicle vehicle, City city) {
        return new Event(MOVE, time, vehicle, city, null);
    }

    public static Event deliver(long time, Vehicle vehicle, Task task) {
        return new Event(DELIVER, time, vehicle, null, task);
    }

    public static Event pickup(long time, Vehicle vehicle, Task task) {
        return new Event(PICKUP, time, vehicle, null, task);
    }

    public static Event fromAction(final long time, final Vehicle vehicle,
            Action action) {
        return action.accept(new ActionHandler<Event>() {

            @Override
            public Event deliver(Task task) {
                return Event.deliver(time, vehicle, task);
            }

            @Override
            public Event moveTo(City city) {
                return Event.arrive(time, vehicle, city);
            }

            @Override
            public Event pickup(Task task) {
                return Event.pickup(time, vehicle, task);
            }
        });
    }

    public String action() {
        return (type == MOVE) ? city.toString() : Integer.toString(task.id);
    }

    // @Override
    // public int compareTo(Event that) {
    //		
    // if (this.time < that.time)
    // return -1;
    // if (this.time > that.time)
    // return 1;
    //		
    // int diff = this.type.compareTo(that.type);
    // if (diff != 0)
    // return diff;
    //		
    // return this.vehicle.getName().compareTo(that.vehicle.getName());
    // }
}
