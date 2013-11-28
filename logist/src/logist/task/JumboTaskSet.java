package logist.task;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Private implementation class for TaskSet, for "jumbo" task batches (i.e.,
 * those with more than 64 elements).
 *
 * @author Robin Steiger
 * @author (based on JumboEnumSet by) Joshua Bloch
 */
class JumboTaskSet extends TaskSet {
    /**
     * Bit vector representation of this set. The ith bit of the jth element of
     * this array represents the presence of universe[64*j +i] in this set.
     */
    private long elements[];

    // Redundant - maintained for performance
    private int size = 0;

    JumboTaskSet(Task[] universe) {
        super(universe);
        elements = new long[(universe.length + 63) >>> 6];
    }

    @Override
    void addAll() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = -1;
        elements[elements.length - 1] >>>= -universe.length;
        size = universe.length;
    }

    @Override
    void complement() {
        for (int i = 0; i < elements.length; i++)
            elements[i] = ~elements[i];
        elements[elements.length - 1] &= (-1L >>> -universe.length);
        size = universe.length - size;
    }

    /**
     * Returns an iterator over the elements contained in this set. The iterator
     * traverses the elements in their <i>natural order</i> (which is the order
     * in which the enum constants are declared). The returned Iterator is a
     * "weakly consistent" iterator that will never throw
     * {@link ConcurrentModificationException}.
     *
     * @return an iterator over the elements contained in this set
     */
    @Override
    public Iterator<Task> iterator() {
        return new TaskSetIterator();
    }

    private class TaskSetIterator implements Iterator<Task> {
        /**
         * A bit vector representing the elements in the current "word" of the
         * set not yet returned by this iterator.
         */
        long unseen;

        /**
         * The index corresponding to unseen in the elements array.
         */
        int unseenIndex = 0;
        /**
         * The bit representing the last element returned by this iterator but
         * not removed, or zero if no such element exists.
         */
        long lastReturned = 0;
        /**
         * The index corresponding to lastReturned in the elements array.
         */
        int lastReturnedIndex = 0;

        TaskSetIterator() {
            unseen = elements[0];
        }

        @Override
        public boolean hasNext() {
            while (unseen == 0 && unseenIndex < elements.length - 1)
                unseen = elements[++unseenIndex];
            return unseen != 0;
        }

        @Override
        public Task next() {
            if (!hasNext())
                throw new NoSuchElementException();
            lastReturned = unseen & -unseen;
            lastReturnedIndex = unseenIndex;
            unseen -= lastReturned;
            return universe[(lastReturnedIndex << 6)
                    + Long.numberOfTrailingZeros(lastReturned)];
        }

        @Override
        public void remove() {
            if (lastReturned == 0)
                throw new IllegalStateException();
            elements[lastReturnedIndex] -= lastReturned;
            size--;
            lastReturned = 0;
        }
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
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
        if (e == null)
            return false;

        Task task = (Task) e;
        check(task);

        int eOrdinal = task.id;
        return (elements[eOrdinal >>> 6] & (1L << eOrdinal)) != 0;
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

        int eOrdinal = e.id;
        int eWordNum = eOrdinal >>> 6;

        long oldElements = elements[eWordNum];
        elements[eWordNum] |= (1L << eOrdinal);
        boolean result = (elements[eWordNum] != oldElements);
        if (result)
            size++;
        return result;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param e
     *            element to be removed from this set, if present
     * @return <tt>true</tt> if the set contained the specified element
     */
    @Override
    public boolean remove(Object e) {
        if (e == null)
            return false;

        Task task = (Task) e;
        check(task);

        int eOrdinal = task.id;
        int eWordNum = eOrdinal >>> 6;

        long oldElements = elements[eWordNum];
        elements[eWordNum] &= ~(1L << eOrdinal);
        boolean result = (elements[eWordNum] != oldElements);
        if (result)
            size--;
        return result;
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
    public boolean containsAll(Collection<?> c) {
        if (!(c instanceof JumboTaskSet))
            return super.containsAll(c);

        JumboTaskSet ts = (JumboTaskSet) c;
        check(ts);

        for (int i = 0; i < elements.length; i++)
            if ((ts.elements[i] & ~elements[i]) != 0)
                return false;
        return true;
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
    public boolean addAll(Collection<? extends Task> c) {
        if (!(c instanceof JumboTaskSet))
            return super.addAll(c);

        JumboTaskSet ts = (JumboTaskSet) c;
        check(ts);

        for (int i = 0; i < elements.length; i++)
            elements[i] |= ts.elements[i];
        return recalculateSize();
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
        if (!(c instanceof JumboTaskSet))
            return super.removeAll(c);

        JumboTaskSet ts = (JumboTaskSet) c;
        check(ts);

        for (int i = 0; i < elements.length; i++)
            elements[i] &= ~ts.elements[i];
        return recalculateSize();
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
    public boolean retainAll(Collection<?> c) {
        if (!(c instanceof JumboTaskSet))
            return super.retainAll(c);

        JumboTaskSet ts = (JumboTaskSet) c;
        check(ts);

        for (int i = 0; i < elements.length; i++)
            elements[i] &= ts.elements[i];
        return recalculateSize();
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        Arrays.fill(elements, 0);
        size = 0;
    }

    /**
     * Compares the specified object with this set for equality. Returns
     * <tt>true</tt> if the given object is also a set, the two sets have 7 *
     * the same size, and every member of the given set is contained in this
     * set.
     *
     * @param e
     *            object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public boolean equals(Object o) {
        if (!(o instanceof JumboTaskSet))
            return super.equals(o);

        JumboTaskSet ts = (JumboTaskSet) o;
//		if (ts.isEmpty() && isEmpty())
//			return true;
        check(ts);

        return Arrays.equals(ts.elements, elements);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (long element : elements)
            hash += (int) element + (int) (element >>> 32);
        return hash;
    }

    /**
     * Recalculates the size of the set. Returns true if it's changed.
     */
    private boolean recalculateSize() {
        int oldSize = size;
        size = 0;
        for (long elt : elements)
            size += Long.bitCount(elt);

        return size != oldSize;
    }

    @Override
    public TaskSet clone() {
        JumboTaskSet result = (JumboTaskSet) super.clone();
        result.elements = result.elements.clone();
        return result;
    }
}
