package template;

import logist.plan.Plan;
import logist.task.Task;
import logist.topology.Topology.City;

/**
 * Task's step which can be pickup or delivery.
 *
 * @author Yoan Blanc
 */
public class Step {
    public final Types type;
    public final Task task;
    public final Schedule schedule;
    public final City city;

    public static enum Types {
        PICKUP {
            @Override
            public String toString() {
                return "p";
            }
        },
        DELIVERY {
            @Override
            public String toString() {
                return "d";
            }
        }
    }

    private Step(Types ty, Task ta, Schedule sched) {
        type = ty;
        task = ta;
        schedule = sched;
        switch (ty) {
            case PICKUP:
                city = ta.pickupCity;
                break;
            case DELIVERY:
            default:
                city = ta.deliveryCity;
        }
    }

    public static Step newPickup(Task t, Schedule s) {
        return new Step(Types.PICKUP, t, s);
    }

    public static Step newDelivery(Task t, Schedule s) {
        return new Step(Types.DELIVERY, t, s);
    }

    /**
     * Move and do.
     *
     * @param plan the plan to act on
     * @param position the starting point
     */
    public void applyTo(Plan plan, City position) {
        for (City c : position.pathTo(city)) {
            plan.appendMove(c);
        }
        // Do
        switch (type) {
            case PICKUP:
                plan.appendPickup(task);
                break;
            case DELIVERY:
            default:
                plan.appendDelivery(task);
                break;
        }
    }

    @Override
    public String toString() {
        return type.toString() + task.id;
    }
}
