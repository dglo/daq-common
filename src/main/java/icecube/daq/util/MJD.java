package icecube.daq.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

class MJD
    implements Comparable<MJD>
{
    /** Number of significant digits kept when converting to a <tt>long</tt> */
    private static final int PRECISION = 1000000;

    /** Cached formatting object used by <tt>toDateString()</tt> */
    private SimpleDateFormat dateFmt;

    /** Actual MJD value */
    private double value;

    /**
     * Create a dummy object to be filled in later
     */
    private MJD()
    {
    }

    /**
     * Convert the date/time to a Modified Julian Date value.
     *
     * The algorithm for the calculation came from:
     * <i>Practical Astronomy With Your Calculator Third Ed</i>
     * One assumption, that we would not be using years before
     * 1582 was made.
     *
     * @param year integer year (ie 2012 )
     * @param month integer month ( jan = 1 )
     * @param day day of month
     * @return modified julian date
     */
    MJD(int year, int month, int day)
    {
        this(year, month, day, 0, 0, 0);
    }

    /**
     * Convert the date/time to a Modified Julian Date value.
     *
     * The algorithm for the calculation came from:
     * <i>Practical Astronomy With Your Calculator Third Ed</i>
     * One assumption, that we would not be using years before
     * 1582 was made.
     *
     * @param year integer year (ie 2012 )
     * @param month integer month ( jan = 1 )
     * @param day day of month
     * @param hour hour
     * @param minute minute
     * @param second second
     *
     * @return Modified Julian Date
     */
    MJD(int year, int month, int day, int hour, int minute, int second)
    {
        if (month == 1 || month == 2) {
            year = year - 1;
            month = month + 12;
        }

        // assume that we will never
        // be calculating mjd's before
        // oct 15 1582
        int a = year / 100;

        // the a/4 is supposed to be integer division
        int b = 2 - a + ((int) a / 4);

        // continuing with the same assumption
        // year will not be negative
        int c = (int) (365.25 * year);

        int d = (int) (30.600 * (month + 1));

        double jd = b + c + d + day + 1720994.5;

        // to go from julian date to modified jd
        // subtract 2400000.5
        double mjd = jd - 2400000.5;

        // add in hour/minute/second as a fractional value
        value = mjd + ((second / 60. + minute) / 60. + hour) / 24.;
    }

    /**
     * Convert an NTP timestamp (seconds since 1900) into a Modified Julian
     * Date.
     * The calculation was taken from the NIST supplied leap-seconds file.
     *
     * @param timestamp seconds since 1900
     */
    MJD(long timestamp)
    {
        value = timestamp / 86400.0 + 15020;
    }

    /**
     * Add a value to this date/time (or subtract, for negative values)
     */
    void add(double days)
    {
        value += days;
    }

    /**
     * Compare this object to <tt>other</tt>
     *
     * @return the usual values (-1, 0, 1)
     */
    @Override
    public int compareTo(MJD other)
    {
        long val = longValue() - other.longValue();
        if (val < 0) {
            return -1;
        } else if (val > 0) {
            return 1;
        }

        return 0;
    }

    /**
     * Return a copy of this date/time
     *
     * @return new object
     */
    MJD copy()
    {
        MJD mjd = new MJD();
        mjd.value = value;
        return mjd;
    }

    /**
     * Return the current year
     *
     * @return current year
     */
    static int currentYear()
    {
        return nowCalendar().get(Calendar.YEAR);
    }

    /**
     * Is this object the same as the specified object?
     *
     * @return <tt>true</tt> if both objects represent the same date/time
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MJD)) {
            return false;
        }

        return compareTo((MJD) obj) == 0;
    }

    /**
     * Hash code for this MJD value
     *
     * @return smooshed MJD value
     */
    @Override
    public int hashCode()
    {
        return (int) (longValue() % (long) Integer.MAX_VALUE);
    }

    /**
     * Is 'other' later than this date/time?
     *
     * @return <tt>true</tt> if this date is after the specified date
     */
    boolean isAfter(MJD other)
    {
        return this.value > other.value;
    }

    /**
     * Convert MJD value to a long
     *
     * @return long value
     */
    private long longValue()
    {
        return (long) (value * PRECISION);
    }

    /**
     * Get the Modified Julian Date value for today.
     *
     * @return Modified Julian Date
     */
    static MJD now()
    {
        Calendar now = nowCalendar();

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);

        return new MJD(year, month, day, hour, minute, sec);
    }

    /**
     * Get the current date/time as a Calendar object
     *
     * @return current date/time
     */
    static Calendar nowCalendar()
    {
        TimeZone zone = TimeZone.getTimeZone("GMT");
        return Calendar.getInstance(zone);
    }

    /*
     * Take a modified julian date and convert it to a
     * java calendar object.  The algorithm to make this calculate
     * comes from <i>Practical Astronomy With Your Calculator</i>
     * third edition.
     *
     * @param mjd - double modified julian date
     * @return Calendar object ( assumes gmt timezone ) representing mjd
     */
    Calendar toCalendar()
    {
        // step 1
        double jd = value + 2400001;
        int i = (int) jd;
        double f = jd % 1;

        // step 2
        int b;
        if (i <= 2299160) {
            b = i;
        } else {
            int a = (int) ((i - 1867216.25) / 36524.25);
            b = i + 1 + a - (int) (a / 4.0);
        }

        // step 3
        double c = b + 1524.;

        // step 4
        int d = (int) ((c - 122.1) / 365.25);

        // step 5
        int e = (int) (365.25 * d);

        // step 6
        int g = (int) ((c - e) / 30.6001);

        int day = (int) (c - e + f - (int) (30.6001 * g));

        double month;
        if (g < 13.5) {
            month = g - 1;
        } else {
            month = g - 13;
        }

        int year;
        if (month > 2.5) {
            year = d - 4716;
        } else {
            year = d - 4715;
        }

        Calendar cal = nowCalendar();

        cal.set(year, (int) (month - 1), day, 0, 0, 0);

        return cal;
    }

    /**
     * Return string representation of this date/time.
     *
     * @return date string
     */
    String toDateString()
    {
        if (dateFmt == null) {
            dateFmt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            dateFmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        return dateFmt.format(toCalendar().getTime());
    }

    /**
     * Base MJD value
     *
     * @return MJD value
     */
    double value()
    {
        return value;
    }

    /**
     * Return debugging string
     *
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "MJD(" + value + ")";
    }

    public static final void main(String[] args)
    {
        for (int i = 0; i < args.length; i++) {
        }
    }
}
