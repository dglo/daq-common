package icecube.daq.util;

import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LeapsecondsTest
{
    private static File nistDir;

    private static final String DEFAULT_FILENAME = "leap-seconds.3629577600";

    @Before
    public void setUp()
    {
        File configDir =
            new File(getClass().getResource("/config").getPath());
        if (!configDir.exists()) {
            throw new IllegalArgumentException("Cannot find config" +
                                               " directory under " +
                                               getClass().getResource("/"));
        }

        nistDir = new File(configDir, "nist");
        if (!nistDir.exists()) {
            throw new IllegalArgumentException("Cannot find config/nist" +
                                               " directory under " +
                                               getClass().getResource("/"));
        }

        System.setProperty(LocatePDAQ.CONFIG_DIR_PROPERTY,
                           configDir.getAbsolutePath());
    }

    public Leapseconds load(String fname, int year)
        throws IllegalArgumentException
    {
        File configFile = new File(nistDir, fname);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Cannot find file \"" + fname +
                                               "\"");
        }

        return new Leapseconds(configFile, year);
    }

    /* test that you get an exception if the leapseconds file is missing
     */
    @Test
    public void testMissingFile()
    {
        boolean pass=false;
        try {
            Leapseconds test = load("junk1234156", 1972);
            fail("Should not be able to load bad file");
        } catch (IllegalArgumentException e) {
            // ignore failures
        }
    }

    @Test
    public void testDaysInYear()
    {
        for (int year = 1972; year <= 2017; year++) {
            int expDays;
            if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) {
                expDays = 366;
            } else {
                expDays = 365;
            }

            assertEquals("For year " + year, expDays,
                         Leapseconds.getDaysInYear(year));
        }
    }

    @Test
    public void testOldYear()
    {
        try {
            Leapseconds test = load(DEFAULT_FILENAME, 1960);
            fail("Default year preceded 1972");
        } catch (ExceptionInInitializerError e) {
            //desired
        }
    }


    /* This USED to thrown an exception for a future year
     * It's supposed to assume no leapseconds have occurred in time
     * for which we have no information.  make sure that we
     * do NOT get an exception
     *
     */
    @Test
    public void testFutureYear()
    {
        try {
            Leapseconds test = load("leap-seconds.3535228800", 3020);
            assertNotNull("Future year was not supported.", test);
        } catch (IllegalArgumentException e) {
           fail("Future year was not supported.");
        }
    }


    /* test for years that should have an all zero
     * offset list
     *
     * years with no leap seconds
     * 1984
     * 1986
     * 1987
     * 1989
     * 1995
     * 1998
     * 2000 - 2005 ( inclusive )
     * 2007
     * 2008
     * 2010
     * 2011
     */
    @Test
    public void testZeroLeapOffsetYears()
    {
        /* years with no leap seconds
         */
        int years[] = {
            1984, 1986, 1987, 1989, 1995, 1998, 2000, 2001, 2002, 2003,
            2004, 2005, 2007, 2008, 2010, 2011
        };

        for(int yindex = 0; yindex < years.length; yindex++) {
            int year = years[yindex];

            Leapseconds test = load(DEFAULT_FILENAME, year);
            if (test == null) {
                fail("Could not load " + DEFAULT_FILENAME);
            }

            int maxDays = test.getDaysInYear(year)+1;
            for(int day = 0; day < maxDays; day++) {
                final int offset = test.getLeapOffset(day, year);
                assertEquals("Bad offset " + offset + " for " + year +
                             " day#" + day, offset, 0L);
            }
        }
    }

    /* the given test file expires on dec 28, 2012
     * test that the number of leap seconds is continuous after
     * june 30
     */
    @Test
    public void test2012()
    {
        final int year2012 = 2012;

        Leapseconds test = load(DEFAULT_FILENAME, year2012);
        if (test == null) {
            fail("Could not load " + DEFAULT_FILENAME);
        }

        MJD jan1mjd = new MJD(year2012, 1, 1);
        MJD jul1mjd = new MJD(year2012, 7, 1);
        MJD jan1mjd2013 = new MJD(year2012 + 1, 1, 1);

        /* should be 0 from jan 1 to one day before jul1 */
        // +1 because the offset array is 1 based
        // as the gps clock starts the first day of year at 1
        int dayJul1 = (int) (jul1mjd.value() - jan1mjd.value()) + 1;
        for(int day = 0; day < dayJul1; day++) {
            final int leapSecs = test.getLeapOffset(day);
            assertEquals("For day " + day, 0, leapSecs);
        }

        /* should go from 0 to 1 from june 30 to jul 1
         * and stay there until jan 1 of 2013
         */
        int dayJan1 = (int) (jan1mjd2013.value() - jan1mjd.value()) + 1;
        for(int day = dayJul1; day <= dayJan1; day++) {
            final int leapSecs = test.getLeapOffset(day);
            assertEquals("For day " + day, 1, leapSecs);
        }
    }

    @Test
    public void testNextYear()
    {
        Leapseconds test = Leapseconds.getInstance();

        final int lsecs = test.getLeapOffset(366);
        assertEquals("Bad number of leap seconds for next year", lsecs,
                     test.getLeapOffset(366, test.getDefaultYear() + 1));
    }

    @Test
    public void testSecondsInYear()
    {
        Leapseconds test = load(DEFAULT_FILENAME, 1972);
        if (test == null) {
            fail("Could not load " + DEFAULT_FILENAME);
        }

        assertEquals(test.getTotalSeconds(2008) , 31622401L);
        assertEquals(test.getTotalSeconds(2011) , 31536000L);
        assertEquals(test.getTotalSeconds(1997) , 31536001L);
        assertEquals(test.getTotalSeconds(2007) , 31536000L);
        assertEquals(test.getTotalSeconds(1972) , 31622402L);

        try {
            test.getTotalSeconds(1970);
            fail("This should not succeed");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expect this to fail
        }

        try {
            test.getTotalSeconds(2020);
            fail("This should not succeed");
        } catch (ArrayIndexOutOfBoundsException e) {
            // expect this to fail
        }
    }

    /**
     * 1972 is the only year with 2 leap seconds
     */
    @Test
    public void test1972()
    {
        Leapseconds test = load(DEFAULT_FILENAME, 1972);
        if (test == null) {
            fail("Could not load " + DEFAULT_FILENAME);
        }

        /* calculate the interesting day of year numbers */
        MJD jan1 = new MJD(1972, 1, 1);
        MJD jul1 = new MJD(1972, 7, 1);
        MJD jan11973 = new MJD(1973, 1, 1);

        /* should be 0 from jan 1 to one day before jul1 */
        // +1 because the offset array is 1 based
        // as the gps clock starts the first day of year at 1
        int limit = (int) (jul1.value() - jan1.value()) + 1;
        for(int day = 0; day < limit; day++) {
            assertEquals("For day " + day + " (limit " + limit + ")",
                         0L, test.getLeapOffset(day));
        }

        /* should go from 0 to 1 from june 30 to jul 1
         * and stay there until jan 1 of 1973
         */
        int limit_jan1 = (int)(jan11973.value() - jan1.value()) + 1;
        for(int day = limit; day < limit_jan1; day++) {
            assertEquals("For day " + day + " (limit " + limit_jan1 + ")",
                         1, test.getLeapOffset(day));
        }

        /* should go from 1 to 2 at jan 1 offset
         */
        assertEquals("For day " + limit_jan1,
                     2, test.getLeapOffset(limit_jan1));
    }

    @Test
    public void testRangeOfYearOffsets()
    {
        //
        // In 2015, the leapsecond class was extended to provide
        // access to pre-calculated offsets for all years covered in
        // the NIST file in a single instantiation
        //
        final int defaultYear = 1992;
        Leapseconds subject = load(DEFAULT_FILENAME, defaultYear);
        if (subject == null) {
            fail("Could not load " + DEFAULT_FILENAME);
        }

        // hardcoded know leap second data as of July 2015, omitting
        // 1972 because there were 2 leap seconds
        Map<Integer, Integer> leaps = new HashMap<Integer, Integer>(30);

        leaps.put(1973, 366); // leap second at end of year
        leaps.put(1974, 366); // leap second at end of year
        leaps.put(1975, 366); // leap second at end of year
        leaps.put(1976, 367); // leap second at end of year.  Also a leap year.
        leaps.put(1977, 366); // leap second at end of year
        leaps.put(1978, 366); // leap second at end of year
        leaps.put(1979, 366); // leap second at end of year
        leaps.put(1980, 999); // no leap second.  Also a leap year.
        leaps.put(1981, 182); // leap second June 30th
        leaps.put(1982, 182); // leap second June 30th
        leaps.put(1983, 182); // leap second June 30th
        leaps.put(1984, 999); // no leap second.  Also a leap year.
        leaps.put(1985, 182); // leap second June 30th
        leaps.put(1986, 999); // no leap second
        leaps.put(1987, 366); // leap second at end of year
        leaps.put(1988, 999); // no leap second.  Also a leap year.
        leaps.put(1989, 366); // leap second at end of year
        leaps.put(1990, 366); // leap second at end of year
        leaps.put(1991, 999); // no leap second
        leaps.put(1992, 183); // leap second June 30th.  Also a leap year.
        leaps.put(1993, 182); // leap second June 30th
        leaps.put(1994, 182); // leap second June 30th
        leaps.put(1995, 366); // leap second at end of year
        leaps.put(1996, 999); // no leap second.  Also a leap year.
        leaps.put(1997, 182); // leap second June 30th
        leaps.put(1998, 366); // leap second at end of year
        leaps.put(1999, 999); // no leap second
        leaps.put(2000, 999); // no leap second.  Also a leap year.
        leaps.put(2001, 999); // no leap second
        leaps.put(2002, 999); // no leap second
        leaps.put(2003, 999); // no leap second
        leaps.put(2004, 999); // no leap second.  Also a leap year.
        leaps.put(2005, 366); // leap second at end of year
        leaps.put(2006, 999); // no leap second
        leaps.put(2007, 999); // no leap second
        leaps.put(2008, 367); // leap second at end of year.  Also a leap year.
        leaps.put(2009, 999); // no leap second
        leaps.put(2010, 999); // no leap second
        leaps.put(2011, 999); // no leap second
        leaps.put(2012, 183); // leap second June 30th. Also a leap year.
        leaps.put(2013, 999); // no leap second
        leaps.put(2014, 999); // no leap second
        leaps.put(2015, 182); // leap second June 30th.

        for (int year = 1973; year <= 2015; year++) {
            //unusual year-plus-two-day implementation in leapsecond class
            for(int day = 1; day <= subject.getDaysInYear(year) + 2; day++) {
                int predicted = (day >= leaps.get(year)) ? 1 : 0;
                int actual = subject.getLeapOffset(day, year);
                String msg = String.format("Bad leap offset for day %d of " +
                                           " year %d", day, year);
                assertEquals(msg, predicted, actual);
            }
        }

        // test the default year of this instantiation
        for (int day = 1; day <= subject.getDaysInYear(defaultYear) + 2;
             day++)
        {
            int predicted = (day>=leaps.get(defaultYear)) ? 1 : 0;
            int actual = subject.getLeapOffset(day);
            String msg = String.format("Bad leap offset for day %d of " +
                                       " year [%d]", day, defaultYear);
            assertEquals(msg, predicted, actual);
        }
    }

    public static final void main(String[] args)
    {
        org.junit.runner.JUnitCore.main("LeapsecondsTest");
    }
}
