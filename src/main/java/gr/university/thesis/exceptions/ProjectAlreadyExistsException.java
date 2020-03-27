package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to create a project that has a common title with another project
 */
public class ProjectAlreadyExistsException extends Exception {
    public ProjectAlreadyExistsException(String description) {
        super(description);
    }
}
