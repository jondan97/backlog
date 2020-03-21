package gr.university.thesis.Exceptions;

/**
 * this exception is thrown when the admin tries to create a user that share a common email with an existing user
 */
public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String description) {
        super(description);
    }
}
