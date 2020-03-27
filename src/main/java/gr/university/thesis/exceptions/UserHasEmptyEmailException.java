package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the admin tries to create a user and leaves their email blank
 */
public class UserHasEmptyEmailException extends Exception {
    public UserHasEmptyEmailException(String description) {
        super(description);
    }
}
