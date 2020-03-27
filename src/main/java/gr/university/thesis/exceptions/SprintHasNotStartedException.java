package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to access a sprint that has not started yet
 */
public class SprintHasNotStartedException extends Exception {
    public SprintHasNotStartedException(String description) {
        super(description);
    }
}
