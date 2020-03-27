package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to view a project that does not exist, mainly in their browser
 */
public class ProjectDoesNotExistException extends Exception {
    public ProjectDoesNotExistException(String description) {
        super(description);
    }
}
