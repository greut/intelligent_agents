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
     * @return the final position
     */
    public City applyTo(Plan plan, City position) {
        City destination;
        // Move
        switch (type) {
            case PICKUP:
                destination = task.pickupCity;
                break;
            case DELIVERY:
            default:
                destination = task.deliveryCity;
                break;
        }
        for (City c : position.pathTo(destination)) {
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
        return destination;
    }

    @Override
    public String toString() {
        return type.toString() + task.id;
    }
}
