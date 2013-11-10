package template;

import java.util.ArrayList;
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
        schedules = new Schedule[p.schedules.length];
        System.arraycopy(p.schedules, 0, schedules, 0, schedules.length);
    }

    /**
     * Assign all the task to the vehicle with the biggest capacity.
     *
     * @param tasks initial set of tasks
     */
    public void selectInitialSolution(TaskSet tasks) {
        Schedule schedule = schedules[0];
        int capacity = 0;
        for (Schedule s : schedules) {
            if (capacity < s.vehicle.capacity()) {
                schedule = s;
            }
        }

        for (Task task : tasks) {
            schedule.add(task);
        }
        cost = schedule.getCost();
    }

    public List<Planning> chooseNeighbors() {
        ArrayList<Planning> neighbors = new ArrayList<Planning>();
        Schedule schedule = randomNonEmptySchedule();
        for (Schedule s : schedules) {
            if (s != schedule) {
                neighbors.add(changingVehicle(schedule, s));
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
     * Change the first task's vehicle
     *
     * @param from vehicle to take the task from.
     * @param to new vehicle handling this task.
     * @return new planning with the change applied
     */
    private Planning changingVehicle(Schedule from, Schedule to) {
        Planning p = (Planning) clone();
        // Clone schedules to be modified
        Schedule newFrom = (Schedule) from.clone();
        Schedule newTo = (Schedule) to.clone();
        // Move task
        Task t = newFrom.removeFirst();
        newTo.add(t);
        // Replace schedules
        p.schedules[to.id] = newTo;
        p.schedules[from.id] = newFrom;
        // Update cost
        p.cost -= from.getCost() + to.getCost();
        p.cost += newFrom.getCost() + newTo.getCost();
        return p;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Schedule s : schedules) {
            sb.append(s);
            sb.append("\n");
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
