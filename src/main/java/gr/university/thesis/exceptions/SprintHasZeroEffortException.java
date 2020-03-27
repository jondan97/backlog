package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to start a sprint with 0 effort, or better: it has no tasks/bugs
 * filling the 'ready' backlog
 */
public class SprintHasZeroEffortException extends Exception {
    public SprintHasZeroEffortException(String description) {
        super(description);
    }
}
