package gr.university.thesis.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * this class is used as a util class to handle everything related to time in general, calculations between dates etc.
 * will be done here and used 'globally'
 */
public class Time implements Comparator<Calendar> {

    /**
     * this method adds a number of weeks to a date given, and returns the result of that addition (the end date)
     *
     * @param startDate: the starting date
     * @param weeks:     the number of weeks that need to be added to the starting date in order to calculate the end date
     * @return : returns the calculated end date
     */
    public static Date calculateEndDate(Date startDate, int weeks) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.WEEK_OF_MONTH, weeks);
        //we set the end date to the final day and hour of 23:59
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    /**
     * this method takes two dates and calculates how many days are in between
     *
     * @param startDate: the starting date
     * @param endDate:   the ending date
     * @return : the number of days between these two
     */
    public static int calculateDaysInBetween(Date startDate, Date endDate) {
        long differenceInMs = endDate.getTime() - startDate.getTime();
        return (int) TimeUnit.DAYS.convert(differenceInMs, TimeUnit.MILLISECONDS);
    }

    /**
     * this method takes two dates and calculates the difference in MS between the two
     *
     * @param startDate : the starting date
     * @param endDate   :   the ending date
     * @return :the difference in MS between the two
     */
    public static long calculateDifferenceInMs(Date startDate, Date endDate) {
        return endDate.getTime() - startDate.getTime();
    }

    /**
     * this method takes as input a date and returns a String that represents the calendar day of that date
     * for example date: 18/3/20 20:00:00 would returns 'Wed'
     *
     * @param date: the date that the user requested to find the String of
     * @return : returns the String of the given date which could be something like 'Mon' for Monday
     */
    public static String findDay(Date date) {
        return new SimpleDateFormat("EE").format(date);
    }

    /**
     * this method takes as input a date, and returns the day after the input date
     *
     * @param date: date that the user requested to increment by 1 day
     * @return : returns the incremented date
     */
    public static Date incrementDateByOne(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //number of days to add
        return calendar.getTime();
    }

    /**
     * this method was created to make things easier for the developer, instead of instantiating a Time class,
     * the developer can instantly use this method from a static view point, outside this class, it compares two dates
     * and returns which one is earlier or later of the two
     *
     * @param date1: first date that we want to compare
     * @param date2: second date that we want to compare
     * @return : returns the result of the overidden compare(): 1 if date1 is later than date2, 0 if date1 is equal to
     * date2 and -1 if date1 is earlier than date2
     */
    public static int compare(Date date1, Date date2) {
        Time time = new Time();
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        return time.compare(calendar1, calendar2);
    }

    /**
     * this method sets the clock of a date to zero, mainly used to group dates together
     *
     * @param date: the date that the user requested to set the clock to zero
     * @return : returns a date with its clock set to 00:00:00
     */
    public static Date setClockToZero(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        //we set the end date's clock to  00:00:00
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * this method is implemented by the calendar comparator, mainly used to compare two calendars and to check
     * if they are equal or not
     *
     * @param c1: first calendar that we want to compare
     * @param c2: second calendar that we want to compare
     * @return : returns 1 if calendar1 is later than calendar2, returns 0 if same date and returns -1 if calendar1
     * is earlier than calendar 2
     */
    @Override
    public int compare(Calendar c1, Calendar c2) {
        if (c1.get(Calendar.YEAR) != c2.get(Calendar.YEAR))
            return c1.get(Calendar.YEAR) - c2.get(Calendar.YEAR);
        if (c1.get(Calendar.MONTH) != c2.get(Calendar.MONTH))
            return c1.get(Calendar.MONTH) - c2.get(Calendar.MONTH);
        return c1.get(Calendar.DAY_OF_MONTH) - c2.get(Calendar.DAY_OF_MONTH);
    }
}
