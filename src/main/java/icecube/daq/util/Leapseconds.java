package icecube.daq.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides knowledge of leap second insertions, both historical
 * and pending. Information is obtained from the NIST leap second
 * file provided by the configuration.
 *
 * The primary function of this class is getLeapOffset() which provides an
 * offset value that can be used to translate a UTC point-in-time (which does
 * not enumerate leap seconds) to an ICL point in time which is based on the
 * number of seconds since the beginning of a year.
 *
 * In general use, the singleton instance of this class will be initialized
 * with the default year equal to the current year at initialization. This
 * allows for unqualified use for time values related to the current year.
 * Clients which desire a lookup from a different year can specify the year.
 *
 * Use of this class should be restricted to times occurring from 1972
 * through to the expiration of the configured NIST file. Usage beyond the
 * expiration date is not prohibited, but will produce inaccurate offsets
 * for times occurring after a published leap second.
 *
 * <b>NOTE</b>: The information provided by this class is only as good as the
 *       information provided by the configured NIST file. Specifically,
 *       if this class is used to look up offset data or duration data
 *       beyond the expiration date of the NIST file, there is a potential
 *       for inaccurate data to be returned. Maintenance of the NIST file
 *       is a system responsibility and is not enforced directly by this
 *       class.
 */
public class Leapseconds
{
    /** First year covered by NIST file */
    public static final int NIST_EPOCH_YEAR = 1972;

    /** Singleton instance */
    private static Leapseconds instance;

    /** Directory where NIST files can be found */
    private static File configDir;

    /** The date when the file expires */
    private MJD expiry;

    /**
     * Path to file used to populate this instance
     */
    private File file;
    /**
     * Default year for lookups that do not specify the year, in normal
     * usage this will be the current year.
     */
    private int defaultYear;

    /** Starting year for internal offset array */
    private int baseOffsetYear;
    /** Internal year-based leapsecond data */
    private LeapOffsets[] leapOffsets;

    /**
     * Load NIST leapsecond file and cache important values for every
     * year of interest
     *
     * @param filename path to the NIST leapsecond file
     *
     * @throws IllegalArgumentException if the file cannot be found
     */
    private Leapseconds(File file)
        throws IllegalArgumentException
    {
        this(file, MJD.currentYear());
    }

    /**
     * Load NIST leapsecond file and cache important values for every
     * year of interest
     *
     * @param filename path to the NIST leapsecond file
     * @param year the default year (usually the current year)
     *
     * @throws IllegalArgumentException if the file cannot be found
     */
    protected Leapseconds(File file, int year)
        throws IllegalArgumentException
    {
        if (year < NIST_EPOCH_YEAR) {
            // nist does not provide information prior to 1972
            final String err = "Nist does not provide leap second info" +
                " prior to " + NIST_EPOCH_YEAR;
            throw new ExceptionInInitializerError(err);
        }

        this.file = file;
        this.defaultYear = year;

        // parse the leapseconds file to initialize this object
        new NISTParser(this).parse(file, defaultYear);

        // fill in total seconds for each leapOffsets entry
        computeAndSetSecondsInYear();
    }

    /**
     * Precompute the total seconds for every year
     */
    private void computeAndSetSecondsInYear()
    {
        MJD mjd1 = new MJD(baseOffsetYear, 1, 1);
        for (int idx = 0; idx < leapOffsets.length; idx++) {
            MJD mjd2 = new MJD(baseOffsetYear + idx + 1, 1, 1);

            final int daysInYear = (int) (mjd2.value() - mjd1.value());
            leapOffsets[idx].setTotalSeconds(daysInYear * 3600 * 24);
            mjd1 = mjd2;
        }
    }

    /**
     * Return number of days until this leapsecond file expires
     *
     * @return decimal days till leap second file expires
     */
    public double daysTillExpiry()
    {
        return expiry.value() - MJD.now().value();
    }

    /**
     * Calculate the number of days in the given year
     *
     * @param year the year of interest with century (i.e. 2012)
     * @return days in the given year (i.e. 365 or 366)
     */
    public static int getDaysInYear(int year)
    {
        final int daysInYear = 365;

        if (year % 400 == 0) {
            return daysInYear + 1;
        } else if (year % 100 == 0) {
            return daysInYear;
        } else if (year % 4 == 0) {
            return daysInYear + 1;
        } else {
            return daysInYear;
        }
    }

    /**
     * Get the default year
     *
     * @return default year
     */
    public int getDefaultYear()
    {
        return defaultYear;
    }

    /**
     * Return the singleton instance of the Leapseconds class.
     *
     * @return shared leapsecond data object
     */
    public static synchronized Leapseconds getInstance()
    {
        if (configDir == null) {
            // look for the pDAQ configuration directory
            configDir = LocatePDAQ.findConfigDirectory();
        }

        if (instance == null || !instance.isConfigDirectory(configDir)) {
            instance = new Leapseconds(new File(configDir,
                                                "nist/leapseconds-latest"));
        }

        return instance;
    }

    /**
     * Get the leap second offset for a specific day of the default year.
     * Generally this will be the year in which the application was
     * initialized.
     *
     * It is assumed that the year has not changed during the lifetime of
     * an instance.
     *
     * @param dayOfYear A day in the year, one-based.
     * @return number of leap seconds that have occurred since the
     *         beginning of the year.
     */
    public int getLeapOffset(int dayOfYear)
    {
        return getLeapOffset(dayOfYear, defaultYear);
    }

    /**
     * Get the leap second offset for a specific day of a specific
     * year. Days past the end of the year are ignored, so for a non-leap
     * year, day 365 and day 999 return the same value.
     *
     * Callers should be aware of the general limitations of
     * this class for years prior to 1972 and years beyond the expiration
     * of the NIST file.
     *
     * @param dayOfYear A day in the specified year, one-based.
     * @param year The operative year.
     *
     * @return The number of leap seconds that have occurred since the
     *         beginning of the operative year, or zero for years outside
     *         the capabilities of this class.
     */
    public int getLeapOffset(int dayOfYear, final int year)
    {
        // get the index into the leapOffsets array for the specified year
        int yrIndex = year - baseOffsetYear;
        if (yrIndex >= leapOffsets.length) {
            yrIndex = leapOffsets.length - 1;
            // use all leap seconds declared for the previous year
            dayOfYear = 366;
        }

        // get the leapsecond day array for the requested year
        return leapOffsets[yrIndex].getLeapSeconds(dayOfYear);
    }

    /**
     * Get the number of seconds in the year including leap seconds
     *
     * @param year year
     *
     * @return number of seconds in the year including leap seconds
     */
    public int getTotalSeconds(int year)
    {
        int yrIndex = year - baseOffsetYear;
        if (yrIndex < 0 || yrIndex > leapOffsets.length) {
            final String errmsg =
                String.format("Year %s is unknown (valid range [%d-%d])",
                              year, baseOffsetYear,
                              baseOffsetYear + leapOffsets.length);
            throw new ArrayIndexOutOfBoundsException(errmsg);
        }

        // get the leapsecond day array for the requested year
        return leapOffsets[yrIndex].getTotalSeconds();
    }

    /**
     * Return <tt>true</tt> if the nist leapsecond file has expired
     *
     * @return true if the leapsecond file does not cover the year of interest
     */
    public boolean hasExpired()
    {
        if (expiry == null) {
            return true;
        }

        final int year = expiry.toCalendar().get(Calendar.YEAR);
        if (year == defaultYear) {
            return MJD.now().isAfter(expiry);
        }

        return year < defaultYear;
    }

    private boolean isConfigDirectory(File configDir)
    {
        return file != null && configDir != null &&
            file.toString().startsWith(configDir.toString());
    }

    /**
     * Calculates the number of seconds in a year including the
     * number of leap seconds
     *
     * @param year year with century
     * @return number of seconds in the specified year
     *
     * @throws IllegalArgumentException if the year is before 1972 or
     *                                  after the NIST file's expiration date
     */
    public static void setConfigDirectory(File dir)
    {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Bad config directory " + dir);
        }

        configDir = dir;
    }

    /**
     * Used by NISTParser to initialize the core data
     *
     * @param expiry expiration date
     * @param baseOffsetYear the first year of data included in leapOffsets
     * @param leapOffsets cached leapsecond data for many years
     */
    protected void setData(MJD expiry, int baseOffsetYear,
                           LeapOffsets[] leapOffsets)
    {
        this.expiry = expiry;
        this.baseOffsetYear = baseOffsetYear;
        this.leapOffsets = leapOffsets;
    }

    /**
     * String respresentation
     *
     * @return A string representation of this class
     */
    public String toString()
    {
        StringBuilder result = new StringBuilder();

        result.append(getClass().getName()).append('(');
        result.append(defaultYear).append(')');

        return result.toString();
    }
}

/**
 * Parse the NIST leapseconds file
 */
class NISTParser
{
    /** Maximum number of years to precalculate */
    private static final int MAX_PRECALCULATE_SPAN = 100;

    /** pattern used to extract values from the NIST leapseconds file */
    private static final Pattern NIST_DATA_PAT =
        Pattern.compile("^(\\d+)\\s+(\\d+)");

    private Leapseconds lsObject;

    NISTParser(Leapseconds lsObject)
    {
        this.lsObject = lsObject;
    }

    private void initObject(int defaultYear, MJD expiry,
                            Map<MJD, Integer> taiMap)
    {
        final int expireYear = expiry.toCalendar().get(Calendar.YEAR);

        int finalYear;
        if (defaultYear > expireYear) {
            // NOTE: The contract of this class allows usage for years
            // beyond the NIST expiration year.
            finalYear = defaultYear;
        } else {
            finalYear = expireYear;
        }

        int firstYear;
        if (finalYear - Leapseconds.NIST_EPOCH_YEAR <
            MAX_PRECALCULATE_SPAN)
        {
            firstYear = Leapseconds.NIST_EPOCH_YEAR;
        } else {
            // it's been a REALLY LONG TIME since
            //  NIST started tracking leap seconds!
            firstYear = finalYear - MAX_PRECALCULATE_SPAN;
        }

        int baseOffsetYear = firstYear;

        LeapOffsets[] leapOffsets =
            new LeapOffsets[finalYear - firstYear + 1];

        // get sorted list of all leap seconds
        MJD[] leapSeconds = taiMap.keySet().toArray(new MJD[0]);
        Arrays.sort(leapSeconds);

        double jan1 = new MJD(firstYear, 1, 1).value();

        int index = 0;
        for (int year = firstYear; year <= finalYear; year++) {
            double nextJan1 = new MJD(year + 1, 1, 1).value();

            // find current offset
            while (index < leapSeconds.length - 2 &&
                   jan1 > leapSeconds[index].value())
            {
                index++;
            }
            if (index >= leapSeconds.length) {
                index = leapSeconds.length - 1;
            }

            MJD firstMJD = leapSeconds[index];

            // if the first leap second is on Jan 1, skip it
            int firstLeapDay = (int) (leapSeconds[index].value() - jan1);
            if (firstLeapDay == 0) {
                index++;
            }

            int nextIndex = index;
            int offDayLen = 0;
            while (nextIndex < leapSeconds.length &&
                   nextJan1 >= leapSeconds[nextIndex].value())
            {
                final int day =
                    (int) (leapSeconds[nextIndex++].value() - jan1);
                if (day > 0) {
                    offDayLen++;
                }
            }

            int[] yearOffsets = new int[offDayLen];

            int yrIndex = 0;
            while (index < nextIndex &&
                   nextJan1 >= leapSeconds[index].value())
            {
                final int day =
                    (int) (leapSeconds[index].value() - jan1);
                if (day > 0) {
                    yearOffsets[yrIndex++] = day;
                }

                index++;
            }

            leapOffsets[year - baseOffsetYear] =
                new LeapOffsets(taiMap.get(firstMJD), yearOffsets);

            jan1 = nextJan1;
            if (nextIndex == 0 ||
                leapSeconds[nextIndex - 1].value() > nextJan1)
            {
                index = nextIndex;
            } else {
                index = nextIndex - 1;
            }
        }

        lsObject.setData(expiry, baseOffsetYear, leapOffsets);
    }

    public void parse(File file, int defaultYear)
    {
        // try to open the file
        BufferedReader rdr;
        try {
            rdr = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException fnf) {
            throw new ExceptionInInitializerError("leapsecond file '" +
                                                  file + "' not found");
        }

        // create a mapping from MJD to TAI offset
        Map<MJD, Integer> taiMap = new HashMap<MJD, Integer>();

        MJD expiry;
        try {
            expiry = parseLines(rdr, taiMap);
        } catch (LeapsecondException lsex) {
            throw new ExceptionInInitializerError(lsex);
        } finally {
            try {
                rdr.close();
            } catch (IOException ioex) {
                // ignore errors on close
            }
        }

        initObject(defaultYear, expiry, taiMap);
    }

    /*
     * Parse the NIST leap seconds file for both the tai offset information
     * and for the expiry date on the file. The files are self documenting
     *
     * @param file path to the nist file being parsed
     *
     * @return a binary-searchable list
     *
     * @throws LeapsecondException if there is a problem with the file
     */
    private MJD parseLines(BufferedReader rdr, Map<MJD, Integer> taiMap)
        throws LeapsecondException
    {
        MJD expiry = null;

        while (true) {
            String line;
            try {
                line = rdr.readLine();
            } catch (IOException ioe) {
                throw new LeapsecondException("Cannot read NIST file", ioe);
            }

            if (line == null) {
                break;
            }

            if (line.length() == 0) {
                // skip blank lines
                continue;
            }

            if (line.charAt(0) == '#') {
                // found a comment line
                if (line.length() > 4 && line.charAt(1) == '@') {
                    // but it's really the expiration date
                    long val = Long.parseLong(line.substring(3).trim());
                    expiry = new MJD(val);
                }

                continue;
            }

            Matcher match = NIST_DATA_PAT.matcher(line);
            if (match.find()) {
                // found some data
                MJD pt = new MJD(Long.parseLong(match.group(1)));
                int offset = Integer.parseInt(match.group(2));

                taiMap.put(pt, offset);

                continue;
            }
        }

        // complain if no expiry line was found
        if (expiry == null) {
            throw new LeapsecondException("No expiration line found");
        } else if (taiMap.size() == 0) {
            throw new LeapsecondException("No leapsecond data found");
        }

        return expiry;
    }
}

/**
 * Track all leapsecond-related quantities for a single year
 */
class LeapOffsets
{
    /** Number of leap seconds at the start of the year */
    private int initialOffset;
    /** Total seconds in this year, including leap seconds */
    private int totalSeconds;
    /** Day(s) when another leapsecond occurs */
    private int[] days;

    /**
     * Create a object with all the data for a year
     *
     * @param initialOffset number of leapseconds at the start of the year
     * @param days array of days when another leapsecond occurs
     */
    LeapOffsets(int initialOffset, int[] days)
    {
        this.initialOffset = initialOffset;
        this.days = days;
    }

    /**
     * Return the total number of leap seconds prior to Jan 1
     *
     * @return number of leap seconds at the start of this year
     */
    int getInitialOffset()
    {
        return initialOffset;
    }

    /**
     * Get the number of leapseconds for this day
     *
     * @param dayOfYear the day to check
     *
     * @return number of new leapseconds since this year began
     */
    int getLeapSeconds(int dayOfYear)
    {
        int numLeapSecs = 0;

        for (int idx = 0; idx < days.length; idx++) {
            if (dayOfYear <= days[idx]) {
                // we haven't reached this leap second yet so we're done
                break;
            }

            numLeapSecs++;
        }

        return numLeapSecs;
    }

    /**
     * Get the total number of seconds in this year, including leap seconds
     *
     * @return total seconds
     */
    int getTotalSeconds()
    {
        return totalSeconds;
    }

    /**
     * Set the total number of seconds in this year
     *
     * @param value total seconds, <b>NOT</b> including leap seconds
     */
    void setTotalSeconds(int value)
    {
        // use 999 days so we find all the leap seconds
        totalSeconds = value + getLeapSeconds(999);
    }
}

/**
 * Exceptions for the Leapseconds class
 */
class LeapsecondException
    extends Exception
{
    /**
     * Create an exception
     *
     * @param msg error message
     */
    LeapsecondException(String msg)
    {
        super(msg);
    }

    /**
     * Wrap an existing exception
     *
     * @param msg error message
     * @param thr wrapped exception/throwable
     */
    LeapsecondException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
