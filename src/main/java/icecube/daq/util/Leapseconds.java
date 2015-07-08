package icecube.daq.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * <pre>
 * Provides knowledge of leap second insertions, both historical
 * and pending. Information is obtained from the NIST leap second
 * file provided by the configuration.
 *
 * The primary function of this class is get_leap_offset() which provides an
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
 * NOTE: The information provided by this class is only as good as the
 *       information provided by the configured NIST file. Specifically,
 *       if this class is used to look up offset data or duration data
 *       beyond the expiration date of the NIST file, there is a potential
 *       for inaccurate data to be returned. Maintenance of the NIST file
 *       is a system responsibility and is not enforced directly by this
 *       class.
 *</pre>
 */
public class Leapseconds {

    // a hash map from mjd to tai offset
    private HashMap<Double, Long> mjd_to_tai_map;

    // date current leapseconds file expires
    private double mjd_expiry;

    // pre-calculated array of day to leap second offset
    // for supported years. Generally this will include 1972
    // through the present if the NIST file is maintained.
    private Map<Integer,int[]> precalculatedOffsets;

    // default year for lookups that do not specify the year,
    // in normal usage this will be the current year.
    public int defaultYear;

    // data from the nist file
    private double[] nist_offset_array;

    private static Leapseconds instance;

    private static File CONFIG_DIR;

    private static final String NIST_CONFIG_FILE = "nist/leapseconds-latest";

    private static final int LEAP_YEAR=366;
    private static final int YEAR=365;

    /** A reasonable bound on the span of years that can be precalculated */
    private static final int MAX_PRECALCULATE_SPAN = 100;

    public static void setConfigDirectory(File dir)
    {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Bad config directory " + dir);
        }

        CONFIG_DIR = dir;
    }

    public static File getConfigDirectory()
    {
        if (CONFIG_DIR == null) {
            CONFIG_DIR = LocatePDAQ.findConfigDirectory();
        }

        return CONFIG_DIR;
    }

    /*
     * singleton pattern
     * get an instance of the leapseconds class
     * assumes that the caller will only be interested in
     * the year the first time getInstance was called.
     * this is an assumption dave g. approbed
     */
    public static synchronized Leapseconds getInstance() {
        if (instance == null) {
            // combine the config dir and the config file
            String joinedPath =
                new File(getConfigDirectory(), NIST_CONFIG_FILE).toString();
            instance = new Leapseconds(joinedPath);
        }
        return instance;
    }

    /*
     * calculate the number of days in the given year
     *
     * @param int - year the year of interest with century ( ie 2012 )
     * @returns int - days in the given year ( ie 365 or 366 )
     */
    public int get_days_in_year(int year) {
        if (year%400 == 0) {
            return LEAP_YEAR;
        } else if (year%100 == 0) {
            return YEAR;
        } else if (year%4 == 0 ) {
            return LEAP_YEAR;
        } else {
            return YEAR;
        }
    }


    /**
     * Get the leap second offset for a specific day of the default year.
     * Generally this will be the year in which the application was
     * initialized.
     *
     * It is assumed that the year has not changed during the lifetime of
     * an instance.
     *
     * @param day_of_year A day in the year, one-based.
     * @returns long - number of leap seconds that have occurred since the
     *                 beginning of the year.
     */
    public int get_leap_offset(int day_of_year) {
        return get_leap_offset(defaultYear, day_of_year);
    }

    /**
     *  Get the leap second offset for a specific day of a specific
     * year. Callers should be aware of the general limitations of
     * this class for years prior to 1972 and years beyond the expiration
     * of the NIST file.
     *
     * @param year The operative year.
     * @param day_of_year A day in the specified year, one-based.
     * @returns The number of leap seconds that have occurred since the
     *          beginning of the operative year, or zero for years outside
     *          the capabilities of this class.
     */
    public int get_leap_offset(final int year, final int day_of_year) {
        if(precalculatedOffsets.containsKey(year))
        {
            final int[] precalculatedOffsets = this.precalculatedOffsets.get(year);
            if (day_of_year > 0 && day_of_year < precalculatedOffsets.length) {
                return precalculatedOffsets[day_of_year];
            } else {
                // if there is an array index error just return 0
                return 0;
            }
        }
        else
        {
            return 0;
        }
    }



    /* figure out how many days until this leapsecond file expires
     *
     * @returns double - decimal days till leap second file expires
     */
    public double daysTillExpiry() {
        return mjd_expiry - mjd_today();
    }


    /* Take a date given in year, month, day format
     * and return the modified julian date for that day.
     * The algorithm for the calculation came from:
     * <i>Practical Astronomy With Your Calculator Third Ed</i>
     * One assumption, that we would not be using years before
     * 1582 was made.
     *
     * @param year - integer year (ie 2012 )
     * @param month - integer month ( jan = 1 )
     * @param day - day of month + fraction of day ( 1.5 = noon on first day )
     * @return modified julian date
     */
    public double mjd(int year, int month, double day) {

        if (month==1 || month==2) {
            year = year - 1;
            month = month + 12;
        }

        // assume that we will never
        // be calculating mjd's before
        // oct 15 1582
        int a = year/100;
        // the a/4 is supposed to be integer division
        // hopefully this works
        int b = 2 - a + ((int)a/4);

        // continuing with the same assumption
        // year will not be negative
        int c = (int)(365.25 * year);

        int d = (int)(30.600 * (month + 1));

        double jd = b + c + d + day + 1720994.5;

        // to go from julian date to modified jd
        // subtract 2400000.5

        double mjd = jd - 2400000.5;

        return mjd;
    }


    /*
     * Calculates the number of seconds in a year including the
     * number of leap seconds
     *
     * @param year - year with century
     * @returns Long - number of seconds which makeup the year specified in the argument
     * @throws IllegalArgumentException  if the year is before 1972 or after the nist data expires
     */

    public Long seconds_in_year(int year) {
        double mjd1 = mjd(year, 1, 1);
        double mjd2 = mjd(year+1, 1, 1);

        if(year<1972) {
            throw new IllegalArgumentException("Cannot calculate before 1972, not enough information");
        }


        // The behaviour of this class is changing.
        // If the leapseconds file has expired we are to assume that there have been no leapseconds
        // ----------------------------------------------------------------------------------------
        //if(mjd2>mjd_expiry) {
        //    throw new IllegalArgumentException("leap second information expires before jan 1 of "+(year+1));
        //}

        return (long)((mjd2-mjd1)*3600*24 +
                      (get_tai_offset(nist_offset_array, mjd2) -
                       get_tai_offset(nist_offset_array, mjd1)));

    }



    /*
     * Converts an ntp timestamp into an modified julian date
     * An ntp timestamp is in seconds since 1900.  The calculation
     * comes from the NIST supplied leap-seconds file.
     *
     * @param ntp_timestamp - long a timestamp in seconds since 1900
     * @return mjd - double modified julian date
     */
    private double ntp_to_mjd(long ntp_timestamp) {
        double mjd = ntp_timestamp / 86400. + 15020;

        return mjd;
    }


    /*
     * Using the GMT timezone get the current day/month/year and calculate
     * the modified julian date for today.  Note that java uses 0=jan for months
     *
     * @return double modified julian date
     */
    private double mjd_today() {

        TimeZone utc_zone = TimeZone.getTimeZone("GMT");
        Calendar now = Calendar.getInstance(utc_zone);

        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH)+1;
        int day = now.get(Calendar.DAY_OF_MONTH);
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);
        int sec = now.get(Calendar.SECOND);

        double frac_day = sec / 60.;
        frac_day = (frac_day + minute)/60. + hour;
        frac_day = frac_day / 24. + day;

        return mjd(year, month, frac_day);
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
    private Calendar mjd_to_cal(double mjd) {
        // convert mjd to jd
        double jd = mjd +2400000.5;

        // step 1
        jd = jd + 0.5;
        int i = (int)jd;
        double f = jd % 1;

        // step 2
        int b;
        if (i>2299160) {
            int a = (int)( (i-1867216.25) / 36524.25);
            b = i + 1 + a - (int)(a/4.0);
        } else {
            b = i;
        }

        // step 3
        double c = b + 1524.;

        // step 4
        int d = (int)( (c-122.1)/365.25);

        // step 5
        int e = (int)(365.25 * d);

        // step 6
        int g = (int)( (c-e)/30.6001);

        double day = c - e + f - (int)(30.6001 * g);

        double m;
        if(g<13.5) {
            m = g - 1;
        } else {
            m = g -13;
        }

        double year;
        if(m>2.5) {
            year = d - 4716;
        } else {
            year = d - 4715;
        }

        TimeZone utc_zone = TimeZone.getTimeZone("GMT");
        Calendar mjd_cal = Calendar.getInstance(utc_zone);

        mjd_cal.set((int)year, (int)(m-1), (int)day, 0, 0, 0);

        return mjd_cal;
    }


    /*
     * Parse the NIST leap seconds file for both the tai offset information
     * and for the expiry date on the file. The files are self documenting

     * The files are self documenting
     *
     * @param the file name and path to the nist file to parse
     * @throws FileNotFoundException throws exception if the file name argument cannot be found
     */
    private void parse_nist_leapseconds(String leapsecond_name,
                                        ArrayList<Double> mjd_list)
        throws FileNotFoundException
    {

        // expiry regexp
        // '#@  3565641600'
        Pattern expiry_pat = Pattern.compile("^#[@]\\s+(\\d+)");

        // comment line, not expiry
        Pattern comment_pat = Pattern.compile("(^#$)|(^#[^@].*$)");

        // data pattern
        Pattern data_pat = Pattern.compile("^(\\d+)\\s+(\\d+)");
        File f = new File(leapsecond_name);
        Scanner scanner = new Scanner(new FileReader(f));
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (comment_pat.matcher(line).find()) {
                continue;
            } else {
                Matcher expiry_matcher = expiry_pat.matcher(line);
                if(expiry_matcher.find()) {
                    // found the expiration info
                    long ntp_expiry = Long.parseLong(expiry_matcher.group(1));
                    mjd_expiry = ntp_to_mjd(ntp_expiry);
                } else {
                    Matcher data_match = data_pat.matcher(line);
                    if(data_match.find()) {
                        // found some data
                        double pt_mjd = ntp_to_mjd(Long.parseLong(data_match.group(1)));
                        long tai_offset = Long.parseLong(data_match.group(2));

                        mjd_to_tai_map.put(pt_mjd, tai_offset);
                        mjd_list.add(pt_mjd);
                    }
                }
            }
        }
    }


    /*
     * A utility function used with the parse_nist_leapseconds
     * file above.  Given a modifid julian date, search through
     * the dates of leap seconds in the file, find out where
     * the mjd lands, and get the tai offset at that point.
     *
     * TAI = Temps Atomique International or international atomic time
     * UTC and TAI where syned in ~1958 and have drifted apart since
     * NO leapseconds in TAI
     *
     * @param mjd_array
     * @param mjd
     * @returns tai offset at the given mjd
     */
    private long get_tai_offset(double[] mjd_array, double mjd) {
        int offset_index;
        offset_index = Arrays.binarySearch(mjd_array, mjd);
        // binary search returns >=0 iff the key is found
        // in the array, otherwise (-(insertion point) -1)

        double mjd_pt;
        if(offset_index>=0) {
            mjd_pt = mjd_array[offset_index];
        } else {
            // the insertion point is the first point
            // GREATER than the mjd given, we want one less
            int index_pt = -1 * (offset_index+1);

            // assume that the icecube detector will not
            // be operating in the past
            mjd_pt = mjd_array[index_pt-1];
        }

        return mjd_to_tai_map.get(mjd_pt);
    }


    /**
     * Initialize the pre-calculated map of offsets.
     *
     * For each year, an array of leap second offsets, indexed by day of year
     * will be generated.
     *
     * If the NIST file expires within a year, the offset at expiration will
     * be propagated to the end of the year.
     *
     * If the NIST file expires prior to a year, an offset array of zeros will
     * be generated.
     *
     * @param firstYear The earliest year to auto-generate.
     * @param finalYear The latest year to auto-generate.
     * @param defaultYear This year will always be populated.
     */
    private void init_offset_array(final int firstYear, final int finalYear,
                                   final int defaultYear, ArrayList<Double> mjd_list) {

        List<Integer> targetYears = new ArrayList<Integer>(finalYear-firstYear);

        // NOTE: The contract of this class allows usage for years beyond the
        // NIST expiry.
        if(defaultYear > finalYear)
        {
            targetYears.add(defaultYear);
        }

        for (int year = firstYear; year <= finalYear; year++)
        {
            targetYears.add(year);
        }

        // Prevent memory utilization errors by respecting a bound on
        // the number of pre-calulated years.
        if(targetYears.size() > MAX_PRECALCULATE_SPAN)
        {
            String msg =
                    String.format("Nist file spans too many years:[%d-%d]",
                            firstYear, finalYear);
            throw new Error(msg);
        }

        // pre-calculate offsets for the requested year as well as
        // all years available in the NIST file. By convention, the offset
        // for years not covered in the NIST file will be reported as zero.
        precalculatedOffsets = init_offset_array(targetYears, mjd_list);
    }


    /**
     * Generate a pre-calculated leap second offset array for a list
     * of years.
     *
     * @param years The list of years to pre-calculate.
     * @param mjd_list A list of times at which leap seconds insertions
     *                 have been or will be inserted.
     * @return A map of offsets indexed by the operative year.
     */
    private Map<Integer, int[]> init_offset_array(final List<Integer> years,
                                                  ArrayList<Double> mjd_list) {


        // NOTE: side-effect, the nist_offset_array is populated
        //       for later use by seconds_in_year()
        // get an array of the mjd data from the list
        // config file.  This is used for the binary search
        nist_offset_array = new double[mjd_list.size()];
        for (int i = 0; i < mjd_list.size(); i++) {
            nist_offset_array[i] = mjd_list.get(i).doubleValue();
        }

        // generate a list of offsets for every year
        Map<Integer, int[]> offsetMap =
                new HashMap<Integer, int[]>(years.size());
        for (int year : years)
        {
            // jan 2'nd next year
            double mjd_this_year = mjd(year, 1, 1);
            double mjd_next_year = mjd(year+1, 1, 2);

            // calculate how far into the future our data extends
            // NOTE: the behaviour of this class has changed.
            // DO NOT complain about the file expiring, simply assume
            // that there have been no leapseconds past the end of the time
            // where we have information
            // ------------------------------------------------------------
            double mjd_limit = mjd_next_year;
            int mjd_data_limit = (int)Math.ceil(mjd_limit - mjd_this_year + 2.);

            // get initial TAI offset
            long initial_offset;
            initial_offset = get_tai_offset(nist_offset_array, mjd_this_year);

            int currentOffsetsSize = (int)Math.ceil(mjd_next_year - mjd_this_year + 2.0);
            int[] currentOffsets = new int[currentOffsetsSize];
            // initialize the offset array to zero
            for(int index=0; index<currentOffsets.length; index++) {
                currentOffsets[index] = 0;
            }


            double mjd_day = mjd_this_year;
            for(int index=1; index<mjd_data_limit; index++) {
                long tmp_offset = get_tai_offset(nist_offset_array, mjd_day);
                currentOffsets[index] = (int) (tmp_offset - initial_offset);
                mjd_day +=1.0;
            }

            if(mjd_data_limit!=currentOffsetsSize) {
                // we don't have data to the end of the year
                // however, make the detector consistent
                int value_to_copy = currentOffsets[mjd_data_limit-1];

                for(int index = mjd_data_limit; index<currentOffsetsSize; index++) {
                    currentOffsets[index] = value_to_copy;
                }
            }

            offsetMap.put(year, currentOffsets);
        }


        return offsetMap;
    }


    /*
     * toString
     * @returns string A string representation of this class
     */
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append(this.getClass().getName() + NEW_LINE);
        result.append("Year: "+ defaultYear +NEW_LINE);

        double jan1_mjd = mjd(defaultYear, 1, 1.);
        double curr_mjd = jan1_mjd;
        int[] defaultYearOffsets = precalculatedOffsets.get(defaultYear);
        for(int index=1; index< defaultYearOffsets.length; index++) {
            Calendar cal_day = mjd_to_cal(curr_mjd);
            curr_mjd += 1.0;

            int year = cal_day.get(Calendar.YEAR);
            int month = cal_day.get(Calendar.MONTH)+1;
            int day = cal_day.get(Calendar.DAY_OF_MONTH);

            result.append(month+"/"+day+"/"+year+" offset: "+ defaultYearOffsets[index]+NEW_LINE);
        }

        return result.toString();

    }
    /*
     * Constructor - only for testing
     * Note that this constructor is mainly for testing as it lets you specify a year
     * we know that there where two leap seconds in 1975, no leap second in 1984
     *
     * @param leapsecond_name - path to the nist leapsecond file
     * @param year - year for which to generate the offset list
     * @throws IllegalArgumentException - if the file cannot be found
     *
     */
    protected Leapseconds(String leapsecond_name, int year) throws IllegalArgumentException {
        init(leapsecond_name, year);
    }

    /*
     * Constructor
     * Note that this constructor assumes it is generating an offset array for the current
     * calendar year
     *
     * @param leapsecond_name - path to the nist leapsecond file
     * @throws IllegalArgumentException if the file cannot be found
     */
    public Leapseconds(String leapsecond_name) throws IllegalArgumentException {
        TimeZone utc_zone = TimeZone.getTimeZone("GMT");
        Calendar now = Calendar.getInstance(utc_zone);
        int year = now.get(Calendar.YEAR);

        init(leapsecond_name, year);
    }


    /*
     * returned true if the nist leapsecond file has expired
     *
     * @returns boolean - true if the leapsecond file is expired for the year of interest
     */
    public boolean has_expired() {

        Calendar expiry_cal = mjd_to_cal(mjd_expiry);
        int expiry_year = expiry_cal.get(Calendar.YEAR);

        if(expiry_year== defaultYear) {
            double mjd_now = mjd_today();
            if (mjd_now>mjd_expiry) {
                return true;
            }
            return false;
        } else if(expiry_year> defaultYear) {
            return false;
        }

        return true;
    }

    /*
     * actually the main body of the constructor, just seperated out
     * to allow for multiple constructors
     *
     * @param leapsecond_name - filename and path to the nist leapsecond file
     * @param year - year for which to generate the offset array
     * @throws IllegalArgumentException - if the year is before 1972, the leapsecond file is not found
     */
    private void init(String leapsecond_name, int year) throws IllegalArgumentException {

        if (year< 1972) {
            // nist does not provide information prior to 1972
            final String err =
                "Nist does not provide leap second info prior to 1972";
            throw new ExceptionInInitializerError(err);
        }

        defaultYear = year;


        mjd_expiry = 0.0;

        // a hash map from mjd to tai offset
        mjd_to_tai_map = new HashMap<Double, Long>();
        // array list of mjd's with offset information
        ArrayList<Double> mjd_list = new ArrayList<Double>();

        // parse the leapseconds file filling in the above
        try {
            parse_nist_leapseconds(leapsecond_name, mjd_list);
        } catch(FileNotFoundException e) {
            throw new ExceptionInInitializerError("leapsecond file '" +
                                                  leapsecond_name +
                                                  "' not found");
        }


        // The desired behavior of this class is changing
        // Do NOT complain about an expired leapseconds file, simply assume that there
        // has been no leapsecond in the time range for which we have no information
        // This is per a discussion with Dave G.
        // The pdaq dash code will complain about an expired and/or expiring file
        // ------------------------------------------------------------------
        //if (this.has_expired()) {
        //    final String err = "leapsecond file not valid for " + year;
        //    throw new ExceptionInInitializerError(err);
        //}

        // For each year covered in the NIST file, as well as for the default
        // year, generate an array of offsets from the beginning of
        // the operative year to jan 2 of next year.
        final int firstYear = 1972;
        int finalYear = mjd_to_cal(mjd_expiry).get(Calendar.YEAR);
        init_offset_array(firstYear, finalYear, defaultYear, mjd_list);
    }
}
