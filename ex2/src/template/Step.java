package template;

import logist.plan.Action;
import logist.topology.Topology.City;
import logist.task.Task;


/**
 * Encapsulating the step with their direct reward.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Step implements Comparable<Step> {
    public final City destination;
    public final Task task;
    public final double reward;
    public final Step.Actions type;

    public static enum Actions {
        MOVE,
        PICKUP,
        DELIVERY
    };

    public Step(Task task) {
        this.destination = null;
        this.task = task;
        this.reward = 0;
        type = Actions.PICKUP;
    }

    public Step(Task task, double reward) {
        this.destination = null;
        this.task = task;
        this.reward = reward;
        type = Actions.DELIVERY;
    }

    public Step(City destination, double reward) {
        this.destination = destination;
        this.task = null;
        this.reward = reward;
        type = Actions.MOVE;
    }

    public String toString() {
        return String.format("<Step \"" + toAction().toLongString()  + ", € " + reward +"\">");
    }

    /**
     * Turn the step into an action.
     *
     * @return action for the action handler
     */
    public Action toAction() {
        switch (type) {
            case MOVE:
                return new Action.Move(destination);
            case PICKUP:
                return new Action.Pickup(task);
            case DELIVERY:
            default:
                return new Action.Delivery(task);
        }
    }

    public int compareTo(Step o) {
        return Double.compare(reward, o.reward);
    }
}
