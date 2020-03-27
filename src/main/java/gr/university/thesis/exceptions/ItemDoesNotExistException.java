package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user is trying to access an item that does not exist, mainly from trying to view
 * it in the browser
 */
public class ItemDoesNotExistException extends Exception {
    public ItemDoesNotExistException(String description) {
        super(description);
    }
}
