package template;

import java.util.LinkedList;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.topology.Topology.City;


/**
 * A vehicle schedule of tasks action interleaving.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Schedule {
    public final LinkedList<Step> steps;
    public final Vehicle vehicle;

    /**
     * @param v the vehicle link with the schedule
     */
    public Schedule (Vehicle v) {
        steps = new LinkedList<Step>();
        vehicle = v;
    }

    /**
     * Add the given task at the beginning of the schedule.
     *
     * @param t the task to be added
     */
    public void add(Task t) {
        steps.add(Step.newPickup(t, this));
        steps.add(Step.newDelivery(t, this));
    }

    /**
     * Remove the task from the schedule alltogether.
     *
     * @param t the task to be removed
     */
    public void remove(Task t) {
        // TODO
        // remove the two steps of the tasks from the list.
    }

    /**
     * Remove the first task it finds and returns it.
     *
     * @return the task removed
     */
    public Task shift() {
        // TODO
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.name());
        for (Step s : steps) {
            sb.append(" -> ");
            sb.append(s);
        }
        sb.append(".");
        return sb.toString();
    }

    /**
     * Converts the schedule into a plan.
     *
     * @return a well built plan
     */
    public Plan toPlan() {
        City current = vehicle.getCurrentCity();
        Plan plan = new Plan(current);
        for (Step s : steps) {
            current = s.applyTo(plan, current);
        }
        return plan;
    }
}
