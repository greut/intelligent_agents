package template;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * All the vehicles and tasks.
     *
     * @param cars set of vehicles
     * @param ts   set of tasks
     */
    public Planning(List<Vehicle> cars) {
        schedules = new Schedule[cars.size()];
        int i = 0;
        for (Vehicle car : cars) {
            schedules[i] = new Schedule(car);
            i += 1;
        }
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
