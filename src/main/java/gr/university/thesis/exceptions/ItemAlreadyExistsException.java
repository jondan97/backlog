package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the item the user is trying to create or update, already exists in the repository
 */
public class ItemAlreadyExistsException extends Exception {
    public ItemAlreadyExistsException(String description) {
        super(description);
    }
}
