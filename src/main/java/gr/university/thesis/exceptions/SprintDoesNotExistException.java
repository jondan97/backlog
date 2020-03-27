package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to view a sprint that does not exist, mainly from their browser
 */
public class SprintDoesNotExistException extends Exception {
    public SprintDoesNotExistException(String description) {
        super(description);
    }

}
