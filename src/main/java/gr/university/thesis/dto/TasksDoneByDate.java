package gr.university.thesis.dto;

import gr.university.thesis.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * this class serves as a Data Transfer Object, in order to transfer multiple data from the methods used, to the
 * user interface. This data is needed in order to create a history table of all the tasks done in the sprint history
 * page
 */
@Getter
@Setter
@AllArgsConstructor
public class TasksDoneByDate {

    /**
     * the date String that owns all the items
     */
    Date date;

    /**
     * the list of items that belong to that certain date(day)
     */
    List<Item> items;

    /**
     * custom constructor to initialize the array
     */
    public TasksDoneByDate() {
        items = new ArrayList<>();
    }
}
