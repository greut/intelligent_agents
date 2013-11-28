package logist.task;

import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A specialized Set implementation for use with {@link Task} objects. All
 * elements in a TaskSet must come from the same task batch that is generated
 * before each simulation round. TaskSet are represented internally as bit
 * vectors.
 * 
 * <p>
 * The iterator returned by the <tt>iterator</tt>method traverses the elements
 * in order of increasing task id.
 * 
 * <p>
 * Note that TaskSets or Tasks from different round are not compatible and
 * cannot be combined.
 * 
 * <p>
 * The implementation of this class is based on Joshua Bloch's EnumSet from the
 * Java Collections Framework.
 * 
 * @author Robin Steiger
 * @author (largely based on EnumSet implementation by Josh Bloch)
 */
public abstract class TaskSet extends AbstractSet<Task> implements Cloneable {

    final Task[] universe;

    TaskSet(Task[] universe) {
        this.universe = universe;

        for (int i = 0; i < universe.length; i++)
            if (universe[i].id != i)
                throw new IllegalArgumentException(
                        "Task ids don't match indices");
    }

    /**
     * For system use only. <br>
     * Creates a new task set for a given task batch. The returned set contains
     * all tasks.
     * 
     * @param universe
     * @throws IllegalArgumentException
     *             if some task id does not match with the index in the table
     */
    public static TaskSet create(Task[] universe) {
        TaskSet result;

//		universe = universe.clone();
        if (universe.length <= 64)
            result = new RegularTaskSet(universe);
        else
            result = new JumboTaskSet(universe);

        result.addAll();
        return result;
    }

    /**
     * Creates a new task set containing the same elements as the specified task
     * set.
     * 
     * @param s
     *            the collection from which to initialize this task set
     */
    public static TaskSet copyOf(TaskSet s) {
        return s.clone();
    }

    /**
     * Creates an empty task set ranging over the same universe as the specified
     * task set.
     * 
     * @param s
     *            the collection from which to initialize this task set
     */
    public static TaskSet noneOf(TaskSet s) {
        TaskSet result = s.clone();
        result.clear();
        return result;
    }

    /**
     * Creates a new task set containing the union of two task sets.
     * 
     * @param s1
     * @param s2
     * @return the set of elements contained by either s1 or s2
     * @throws IllegalArgumentException
     *             if s1 and s2 are task sets from different rounds
     */
    public static TaskSet union(TaskSet s1, TaskSet s2) {
        TaskSet result = s1.clone();
        result.addAll(s2);
        return result;
    }

    /**
     * Creates a new task set containing the intersection of two task sets.
     * 
     * @param s1
     * @param s2
     * @return the set of elements contained by both s1 and s2
     * @throws IllegalArgumentException
     *             if s1 and s2 are task sets from different rounds
     */
    public static TaskSet intersect(TaskSet s1, TaskSet s2) {
        TaskSet result = s1.clone();
        result.retainAll(s2);
        return result;
    }

    /**
     * Creates a new task set containing the intersection of a task set with the
     * complement of another task set. The complement is taken with respect to
     * all tasks in the universe (i.e. in the current round).
     * 
     * @param s1
     * @param s2
     * @return the set of elements contained by s1, but not by s2
     * @throws IllegalArgumentException
     *             if s1 and s2 are task sets from different rounds
     */
    public static TaskSet intersectComplement(TaskSet s1, TaskSet s2) {
        TaskSet result = s1.clone();
        result.removeAll(s2);
        return result;
    }

    abstract void addAll();

    abstract void complement();

    /**
     * Sums the weights of all tasks.
     * @return The sum of weights of all tasks in the set.
     */
    public int weightSum() {
        int sum = 0;
        for (Task task : this)
            sum += task.weight;
        return sum;
    }
    
    /**
     * Sums the rewards of all tasks.
     * @return The sum of rewards of all tasks in the set.
     */
    public int rewardSum() {
        int sum = 0;
        for (Task task : this)
            sum += task.reward;
        return sum;
    }
    
    /**
     * Returns an iterator over the elements contained in this set. The iterator
     * traverses the elements in the order of increasing task ids.
     * 
     * @return an iterator over the elements contained in this set
     */
    @Override
    public abstract Iterator<Task> iterator();
    

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }

    /**
     * Returns a copy of this set.
     * 
     * @return a copy of this set.
     */
    public TaskSet clone() {
        try {
            return (TaskSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /* Integrity checks */
    
    final void check(TaskSet taskset) {
        if (taskset.universe != universe)
            throw new IllegalArgumentException(
                    "You cannot combine Tasksets from different rounds !");
    }
    
    final void check(Task task) {
        if (0 > task.id || task.id >= universe.length
                || universe[task.id] != task)
            throw new IllegalArgumentException(
                    "You cannot use a task from a different round !");
    }

}
