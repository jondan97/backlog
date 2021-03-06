package gr.university.thesis.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * this class serves as a Data Transfer Object, in order to transfer multiple data from the methods used, to the
 * user interface. This data is needed in order to have the burn down chart found in the history page, properly work
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BurnDownChartData {

    /**
     * categories is the array that contains the set of Sprints in the project in order, for example :
     * [Sprint 1, Sprint 2, ...]
     */
    String[] categories;
    /**
     * ideal_burn is the array that contains the ideal burn of effort based on the project total effort divided by
     * the estimated sprints needed for that project to finish, for example [100(total project effort), 86, ...]
     * it is represented as a double, as the division can result in a double, and if it not properly handled, it can
     * be inaccurate
     */
    double[] ideal_burn;

    /**
     * actual_burn is the array that contains the actual burn of effort (velocity) that was done for each finished
     * sprint in the project, for example [100(total project effort), 92, ...]
     */
    int[] actual_burn;

    /**
     * possibleDelay is the boolean that warns the user whether the project will be delayed or not,
     * the ideal remaining is compared with the actual remaining and if the actual remaining is more than the ideal
     * remaining, then the user is notified that something is wrong
     */
    boolean possibleDelay;

    /**
     * earlierFinish is the boolean that notifies the user that they have finished the items in the sprint earlier
     * than expected
     * the ideal remaining is compared with the actual remaining, and if the latter reaches 0 before the ideal remaining
     * then the application informs the user for any possibly solutions (for example add more items in the next sprint
     * etc.)
     */
    boolean earlierFinish;
}
