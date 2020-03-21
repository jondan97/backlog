package gr.university.thesis.Exceptions;

/**
 * this exception is thrown when the user tries to create or update a project and leaves its name blank
 */
public class ProjectHasEmptyTitleException extends Exception {
    public ProjectHasEmptyTitleException(String description) {
        super(description);
    }
}
