package gr.university.thesis.util;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * this class is used as a util class to handle everything related to time in general, calculations between dates etc.
 * will be done here and used 'globally'
 */
public class Time {

    /**
     * this method adds a number of weeks to a date given, and returns the result of that addition (the end date)
     *
     * @param startDate: the starting date
     * @param weeks:     the number of weeks that need to be added to the starting date in order to calculate the end date
     * @return: returns the calculated end date
     */
    public static Date calculateEndDate(Date startDate, int weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.WEEK_OF_MONTH, weeks);
        return calendar.getTime();
    }

    /**
     * this method takes two dates and calculates how many days are in between
     *
     * @param startDate: the starting date
     * @param endDate:   the ending date
     * @return: the number of days between these two
     */
    public static int calculateDaysRemaining(Date startDate, Date endDate) {
        long differenceInMs = endDate.getTime() - startDate.getTime();
        return (int) TimeUnit.DAYS.convert(differenceInMs, TimeUnit.MILLISECONDS);
    }
}
