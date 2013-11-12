package template;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import logist.plan.Plan;
import logist.simulation.Vehicle;
import logist.task.Task;
import logist.task.TaskSet;


/**
 * Aggregation of all the plans.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Planning {
    private Schedule[] schedules;
    private double cost;
    private boolean isValid;

    /**
     * All the vehicles and tasks.
     *
     * @param cars set of vehicles
     * @param ts   set of tasks
     */
    public Planning(List<Vehicle> cars) {
        cost = 0;
        schedules = new Schedule[cars.size()];
        int i = 0;
        isValid = true;
        for (Vehicle car : cars) {
            schedules[i] = new Schedule(i, car);
            i += 1;
        }
    }

    /**
     * Private constructor for `.clone()`.
     *
     * <b>NB:</b> It does a shallow copy.
     *
     * @param p other planning
     */
    private Planning(Planning p) {
        cost = p.cost;
        isValid = p.isValid;
        schedules = new Schedule[p.schedules.length];
        System.arraycopy(p.schedules, 0, schedules, 0, schedules.length);
    }

    /**
     * Assign all the tasks in a round-robin fashion.
     *
     * @param tasks initial set of tasks
     */
    public void selectInitialSolution(TaskSet tasks) {
        Iterator<Task> iter = tasks.iterator();
        int rr = -1;
        while (iter.hasNext()) {
            Task t = iter.next();
            do {
                rr = (rr + 1) % schedules.length;
            } while (schedules[rr].vehicle.capacity() < t.weight);
            schedules[rr].add(t);
        }
        for (Schedule s : schedules) {
            cost += s.getCost();
        }
    }

    public List<Planning> chooseNeighbors() {
        ArrayList<Planning> neighbors = new ArrayList<Planning>();
        Schedule from = randomNonEmptySchedule();
        Task task = from.randomTask();
        for (Schedule to : schedules) {
            for (Planning p : changingTask(task, from, to)) {
                neighbors.add(p);
            }
        }
        return neighbors;
    }

    /**
     * Compute the cost.
     *
     * @return the total cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * Tells if the planning is valid.
     *
     * @return false if some schedule are violating the constraint
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Pick a random non empty schedule from the list.
     *
     * @return non empty schedule
     */
    private Schedule randomNonEmptySchedule() {
        Schedule pick;
        Random r = new Random();
        do {
            pick = schedules[r.nextInt(schedules.length)];
        } while (pick.isEmpty());
        return pick;
    }

     /**
     * Change the task schedule and order.
     *
     * It computes all the possible task distribution for the given task by
     * moving the pickup and delivery positions.
     *
     * @param task the t to be moved.
     * @param from the schedule to pick the task from
     * @param to the schedule to put the task into
     */
    private Planning[] changingTask(Task task, Schedule from, Schedule to) {
        /*
         * j i := positions
         * | |
         * v v
         * p d - - - - -
         * p - d - - - -
         * - p d - - - -
         * p - - d - - -
         * - p - d - - -
         * - - p d - - -
         * p - - - d - -
         * - p - - d - -
         * - - p - d - -
         * - - - p d - -
         * p - - - - d -
         * ...
         * - - - - - p d
         */
        int k = 0, n = to.steps.size();
        // If the destination is different from the source, it's gonna be
        // bigger with a factor of 2 afterwards.
        if (from != to) {
            n += 2;
        }
        Planning[] plans = new Planning[(n - 1) * (n / 2)];
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < i; j++) {
                Planning p = (Planning) clone();
                Schedule newFrom = (Schedule) from.clone();
                Schedule newTo = newFrom;
                if (from != to) {
                    newTo = (Schedule) to.clone();
                }

                // Move task
                newFrom.remove(task);
                newTo.insertAt(j, Step.newPickup(task));
                newTo.insertAt(i, Step.newDelivery(task));
                p.schedules[from.id] = newFrom;
                p.schedules[to.id] = newTo;

                // Update cost
                p.cost -= from.getCost();
                p.cost += newFrom.getCost();
                p.isValid &= newFrom.isValid();
                if (from != to) {
                    p.cost -= to.getCost();
                    p.cost += newTo.getCost();
                    p.isValid &= newTo.isValid();
                }
                plans[k] = p;
                k++;
            }
        }
        return plans;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=> ");
        sb.append(Math.round(cost));
        for (Schedule s : schedules) {
            sb.append("\n  ");
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Shallow copy
     *
     * @return dolly
     */
    @Override
    public Object clone() {
        return (Object) new Planning(this);
    }

    /**
     * Generate the plan to feed back the behaviour.
     *
     * @return list of plan to from the schedules.
     */
    public List<Plan> toList() {
        ArrayList<Plan> list = new ArrayList<Plan>(schedules.length);
        for (Schedule s : schedules) {
            list.add(s.toPlan());
        }
        return list;
    }
}
