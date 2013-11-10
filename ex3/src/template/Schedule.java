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
    public final int id;
    public final LinkedList<Step> steps;
    public final Vehicle vehicle;
    public double cost;

    /**
     * @param index id
     * @param v the vehicle link with the schedule
     */
    public Schedule (int index, Vehicle v) {
        this(index, 0, v, new LinkedList<Step>());
    }

    private Schedule(int index, double c, Vehicle v, LinkedList<Step> s) {
        cost = c;
        id = index;
        vehicle = v;
        steps = s;
    }

    @Override
    public Object clone() {
        return (Object) new Schedule(id, cost, vehicle, new LinkedList<Step>(steps));
    }

    public double getCost() {
        return cost;
    }

    /**
     * Compute the total cost.
     */
    public double computeCost() {
        double cost = 0;
        City c = vehicle.getCurrentCity();
        for (Step s : steps) {
            cost += c.distanceTo(s.city);
            c = s.city;
        }
        return cost;
    }

    /**
     * Add the given task at the beginning of the schedule.
     *
     * @param t the task to be added
     */
    public void add(Task t) {
        Step p, d;
        p = Step.newPickup(t, this);
        d = Step.newDelivery(t, this);

        // Update the cost.
        if (!steps.isEmpty()) {
            cost -= vehicle.getCurrentCity().distanceTo(steps.get(0).city);
            cost += d.city.distanceTo(steps.get(0).city);
        }
        cost += p.city.distanceTo(d.city);
        cost += vehicle.getCurrentCity().distanceTo(p.city);

        steps.addFirst(d);
        steps.addFirst(p);
    }

    /**
     * Remove the task from the schedule alltogether.
     *
     * @param t the task to be removed
     */
    public boolean remove(Task t) {
        Step p = null, d = null;
        int i = 0, j = 1, k = 0;
        for (Step s : steps) {
            // Using .equals should not be necessary here.
            if (s.task == t) {
                if (p == null) {
                    i = k;
                    p = s;
                } else {
                    j = k;
                    d = s;
                    break;
                }
            }
            k++;
        }

        // Update the cost.
        // ================
        // Where are doing incremental steps here since it's easy. It doesn't
        // mean we won't need a way to recompute everything from scratch.
        //
        // a -> (P) -> b -> (D) -> c
        City a, b = null, c = null;
        if (i == 0) {
            a = vehicle.getCurrentCity();
        } else {
            a = steps.get(i-1).city;
        }
        if (i < j - 1) {
            b = steps.get(j-1).city;
        }
        if (j < steps.size() - 2) {
            c = steps.get(j+1).city;
        }

        // Remove
        // ------
        // a -> P
        cost -= a.distanceTo(p.city);
        if (b != null) {
            // P -> b
            cost -= p.city.distanceTo(b);
            // b -> D
            cost -= b.distanceTo(d.city);
        } else {
            // P -[b]-> D
            cost -= p.city.distanceTo(d.city);
        }
        // D -> c
        if (c != null) {
            cost -= d.city.distanceTo(c);
        }

        boolean ret = steps.remove(p) && steps.remove(d);

        // Add back (a -> b -> c)
        // --------
        //
        // a -> b
        if (b != null) {
            cost += a.distanceTo(b);
        } else {
            // a -[b]-> c
            b = a;
        }
        // b -> c
        if (c != null) {
            cost += b.distanceTo(c);
        }

        return ret;
    }

    /**
     * Remove the first task it finds and returns it.
     *
     * @return the task removed
     */
    public Task removeFirst() {
        if (isEmpty())
            return null;

        Task t = steps.get(0).task;
        return remove(t) ? t : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(vehicle.name());
        sb.append(" (");
        sb.append(cost);
        sb.append(")");
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
            s.applyTo(plan, current);
            current = s.city;
        }
        return plan;
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }
}
