package logist.task;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Private implementation class for TaskSet, for "regular sized" task batches
 * (i.e., those with 64 or fewer tasks).
 * 
 * @author Robin Steiger
 * @author (based on RegularEnumSet by) Joshua Bloch
 */
class RegularTaskSet extends TaskSet {

    private long elements = 0L;

    RegularTaskSet(Task[] universe) {
        super(universe);
    }

    @Override
    void addAll() {
        if (universe.length != 0)
            elements = -1L >>> -universe.length;
    }

    @Override
    void complement() {
        if (universe.length != 0) {
            elements = ~elements;
            elements &= -1L >>> -universe.length; // Mask unused bits
        }
    }

    @Override
    public Iterator<Task> iterator() {
        return new TaskIterator();
    }

    private class TaskIterator implements Iterator<Task> {
        /**
         * A bit vector representing the elements in the set not yet returned by
         * this iterator.
         */
        long unseen;

        /**
         * The bit representing the last element returned by this iterator but
         * not removed, or zero if no such element exists.
         */
        long lastReturned = 0;

        TaskIterator() {
            unseen = elements;
        }

        public boolean hasNext() {
            return unseen != 0;
        }

        public Task next() {
            if (unseen == 0)
                throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            unseen -= lastReturned;
            return universe[Long.numberOfTrailingZeros(lastReturned)];
        }

        public void remove() {
            if (lastReturned == 0)
                throw new IllegalStateException();
            elements -= lastReturned;
            lastReturned = 0;
        }
    }

    /**
     * Returns the number of elements in this set.
     * 
     * @return the number of elements in this set
     */
    @Override
    public int size() {
        return Long.bitCount(elements);
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     * 
     * @return <tt>true</tt> if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return elements == 0;
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * 
     * @param e
     *            element to be checked for containment in this collection
     * @return <tt>true</tt> if this set contains the specified element
     */
    @Override
    public boolean contains(Object e) {
        if (!(e instanceof Task))
            return false;

        Task task = (Task) e;
        check(task);

        return (elements & (1L << task.id)) != 0;
    }

    // Modification Operations

    /**
     * Adds the specified element to this set if it is not already present.
     * 
     * @param e
     *            element to be added to this set
     * @return <tt>true</tt> if the set changed as a result of the call
     * 
     * @throws NullPointerException
     *             if <tt>e</tt> is null
     */
    @Override
    public boolean add(Task e) {
        check(e);

        long oldElements = elements;
        elements |= (1L << e.id);
        return elements != oldElements;
    }

    /**
     * Removes the specified element from this set if it is present.
     * 
     * @param e
     *            element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    public boolean remove(Object e) {
        if (!(e instanceof Task))
            return false;

        Task task = (Task) e;
        check(task);

        long oldElements = elements;
        elements &= ~(1L << task.id);
        return elements != oldElements;
    }

    // Bulk Operations

    /**
     * Returns <tt>true</tt> if this set contains all of the elements in the
     * specified collection.
     * 
     * @param c
     *            collection to be checked for containment in this set
     * @return <tt>true</tt> if this set contains all of the elements in the
     *         specified collection
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof RegularTaskSet))
            return super.containsAll(c);

        RegularTaskSet taskset = (RegularTaskSet) c;
        check(taskset);

        return (taskset.elements & ~elements) == 0;
    }

    /**
     * Adds all of the elements in the specified collection to this set.
     * 
     * @param c
     *            collection whose elements are to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException
     *             if the specified collection or any of its elements are null
     */
    @Override
    public boolean addAll(Collection<? extends Task> c) {
        if (!(c instanceof RegularTaskSet))
            return super.addAll(c);

        RegularTaskSet taskset = (RegularTaskSet) c;
        check(taskset);

        long oldElements = elements;
        elements |= taskset.elements;
        return elements != oldElements;
    }

    /**
     * Removes from this set all of its elements that are contained in the
     * specified collection.
     * 
     * @param c
     *            elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        if (!(c instanceof RegularTaskSet))
            return super.removeAll(c);

        RegularTaskSet taskset = (RegularTaskSet) c;
        check(taskset);

        long oldElements = elements;
        elements &= ~taskset.elements;
        return elements != oldElements;
    }

    /**
     * Retains only the elements in this set that are contained in the specified
     * collection.
     * 
     * @param c
     *            elements to be retained in this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof RegularTaskSet))
            return super.retainAll(c);

        RegularTaskSet taskset = (RegularTaskSet) c;
        check(taskset);

        long oldElements = elements;
        elements &= taskset.elements;
        return elements != oldElements;
    }

    /**
     * Removes all of the elements from this set.
     */
    @Override
    public void clear() {
        elements = 0L;
    }

    /**
     * Compares the specified object with this set for equality. Returns
     * <tt>true</tt> if the given object is also a set, the two sets have the
     * same size, and every member of the given set is contained in this set.
     * 
     * @param o
     *            object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegularTaskSet))
            return super.equals(o);

        RegularTaskSet taskset = (RegularTaskSet) o;
        check(taskset);

        return taskset.elements == elements;
    }

    @Override
    public int hashCode() {
        return (int) elements + (int) (elements >>> 32);
    }

}
