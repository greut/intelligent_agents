package template;


public interface SearchAlgorithm {
    /**
     * Search for the best final state from the start state.
     *
     * @param start initial state.
     * @return final state where all the task has been delivered.
     */
    public State search(State start);
}
