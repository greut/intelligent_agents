package template;

import logist.plan.Action;
import logist.topology.Topology.City;
import logist.task.Task;


/**
 * Encapsulating the step with their direct reward.
 *
 * @author Yoan Blanc <yoan.blanc@epfl.ch>
 */
public class Step {
    public final City destination;
    public final Task task;
    public final Step.Actions type;

    public static enum Actions {
        MOVE,
        PICKUP,
        DELIVERY
    };

    public Step(Task task, Actions type) {
        this.destination = null;
        this.task = task;
        this.type = type;
    }

    public Step(City destination) {
        this.destination = destination;
        this.task = null;
        type = Actions.MOVE;
    }

    public String toString() {
        return String.format("<Step \"" + toAction().toLongString()  + "\">");
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
}
