package logist.plan;

import logist.task.Task;
import logist.topology.Topology.City;

/**
 * An action of a vehicle during the simulation.
 * There are 3 possibles actions:
 * <ul>
 * <li>Move to another city ({@link logist.plan.Action.Move})</li>
 * <li>Pick up a task ({@link logist.plan.Action.Pickup})</li>
 * <li>Deliver a task ({@link logist.plan.Action.Delivery} )</li>
 * </ul>
 * 
 * @see PlanVerifier
 * @see Plan
 * @author Robin Steiger
 */
public abstract class Action {

    private Action() {}
    
    /**
     * Shows tasks with full details rather than just the id.
     */
    public abstract String toLongString();
    
    /**
     * Performs a pattern matching on the type of task.
     * 
     * @param <T> the type of the value produced by the handler
     * @param handler the handler for each pattern
     * @return the result of the handler
     */
    public abstract <T> T accept(ActionHandler<T> handler);
    
    /** A move to another city */
    public static final class Move extends Action {
        private final City destination;
        
        /**
         * Creates a move action
         * @param destination The city to move to
         */
        public Move(City destination) {
            this.destination = destination;
        }

        @Override
        public <T> T accept(ActionHandler<T> handler) {
            return handler.moveTo(destination);
        }
        
        @Override
        public String toString() {
            return "Move (" + destination + ")";
        }
        
        @Override
        public String toLongString() {
            return toString();
        }
    }
    
    /** A pickup of a task */
    public static final class Pickup extends Action {
        private final Task task;
    
        /**
         * Creates a pickup action
         * @param task The task to pickup
         */
        public Pickup(Task task) {
            this.task = task;
        }

        @Override
        public <T> T accept(ActionHandler<T> handler) {
            return handler.pickup(task);
        }
        
        @Override
        public String toString() {
            return "Pickup (Task " + task.id + ")";
        }
        
        @Override
        public String toLongString() {
            return "Pickup " + task;
        }
    }
    
    /** A delivery of a task */
    public static final class Delivery extends Action {
        private final Task task;
        
        /**
         * Creates a delivery action
         * @param task The task to deliver
         */
        public Delivery(Task task) {
            this.task = task;
        }

        @Override
        public <T> T accept(ActionHandler<T> handler) {
            return handler.deliver(task);
        }

        @Override
        public String toString() {
            return "Deliver (Task " + task.id + ")";
        }
        
        @Override
        public String toLongString() {
            return "Deliver " + task;
        }
    }
    
}
