package gr.university.thesis.exceptions;

/**
 * this exception is thrown when the user tries to create or update an item, and leaves its name blank
 */
public class ItemHasEmptyTitleException extends Exception {
    public ItemHasEmptyTitleException(String description) {
        super(description);
    }

}
