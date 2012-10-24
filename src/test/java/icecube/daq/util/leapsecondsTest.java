package icecube.daq.util;

import java.io.File;

import static org.junit.Assert.*;

import org.junit.Test;


public class leapsecondsTest {
    private static String homeDir = System.getenv("PDAQ_HOME");

    public leapseconds load(String fname, int year)
        throws IllegalArgumentException
    {
        if (homeDir == null || homeDir.equals("")) {
            System.err.println("PDAQ_HOME has not been set");
            return null;
        }

        File config_file =
            new File(new File(new File(homeDir, "config"), "nist"), fname);
        if (!config_file.exists()) {
            throw new IllegalArgumentException("Cannot find file \"" + fname +
                                               "\"");
        }

        return new leapseconds(config_file.getPath(), year);
    }

    public leapsecondsTest() {

    }

    /* test that you get an exception if the leapseconds file is missing
     */
    @Test
    public void testMissingFile() {
        boolean pass=false;
        try {
            leapseconds test = load("junk1234156", 1972);
            if (test == null) {
                return;
            }
        } catch (IllegalArgumentException e) {
            pass=true;
        }
        assertTrue(pass);
    }

    /* test that the get_days_in_year method works
     */
    @Test
    public void testDaysInYear() {
        int leap_years[] = { 2016, 2012, 2008, 2004, 2000, 1996, 1992, 1988,
                             1984, 1980, 1976, 1972 };

        int years[] = { 2015, 2014, 2013, 2011, 2010, 2009, 2007, 2006, 2005,
                        2003, 2002, 2001, 1999, 1998, 1997, 1995, 1994, 1993 };

        leapseconds test = load("leap-seconds.3535228800", 1972);
        if (test == null) {
            return;
        }

        for(int index=0; index< leap_years.length; index++) {
            assertTrue(test.get_days_in_year(leap_years[index])==366);
        }

        for(int index=0; index<years.length; index++) {
            assertTrue(test.get_days_in_year(years[index])==365);
        }
    }


    /* test that you get an exception for an old year
     */
    @Test
    public void testOldYear() {
        boolean pass=false;
        try {
            leapseconds test = load("leap-seconds.3535228800", 1960);
            if (test == null) {
                return;
            }
        } catch (IllegalArgumentException e) {
            pass=true;
        }
        assertTrue(pass);
    }


    /* test that you get an exception for a future year
     */
    @Test
    public void testFutureYear() {
        boolean pass=false;
        try {
            leapseconds test = load("leap-seconds.3535228800", 3020);
            if (test == null) {
                return;
            }
        } catch (IllegalArgumentException e) {
            pass=true;
        }
        assertTrue(pass);
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
    public void testZeroLeapOffsetYears() {

        /* years with no leap seconds
         */
        int years[] = { 1984, 1986, 1987, 1989, 1995,
                        1998, 2000, 2001, 2002, 2003,
                        2004, 2005, 2007, 2008, 2010,
                        2011 };

        for(int index=0; index< years.length; index++) {
            int year = years[index];

            leapseconds test = load("leap-seconds.3535228800", year);
            if (test == null) {
                return;
            }

            int limit = test.get_days_in_year(year)+1;
            for(int index2=0; index2<limit; index2++) {
                assertTrue(test.get_leap_offset(index)==0L);
            }
        }
    }

    /* the given test file expires on dec 28, 2012
     * test that the number of leap seconds is continuous after
     * june 30
     */
    @Test
    public void test2012() {
        leapseconds test = load("leap-seconds.3535228800", 2012);
        if (test == null) {
            return;
        }

        double jan1mjd = test.mjd(2012, 1, 1.0);
        double jul1mjd = test.mjd(2012, 7, 1.);
        double jan1mjd2013 = test.mjd(2013, 1, 1.);

        /* should be 0 from jan 1 to one day before jul1 */
        // +1 because the offset array is 1 based
        // as the gps clock starts the first day of year at 1
        int limit = (int)(jul1mjd-jan1mjd)+1;
        for(int index=0; index<limit; index++) {
            assertTrue(test.get_leap_offset(index)==0L);
        }

        /* should go from 0 to 1 from june 30 to jul 1
         * and stay there until jan 1 of 1973
         */
        int limit_jan1 = (int)(jan1mjd2013 - jan1mjd)+1;
        for(int index=limit; index<limit_jan1; index++) {
            assertTrue(test.get_leap_offset(index)==1L);
        }
    }


    /* test seconds-seconds_in_year method
     */

    @Test
    public void test_seconds_in_year() {
        leapseconds test = load("leap-seconds.3535228800", 1972);
        if (test == null) {
            return;
        }

        assertTrue(test.seconds_in_year(2008)==31622401L);
        assertTrue(test.seconds_in_year(2011)==31536000L);
        assertTrue(test.seconds_in_year(1997)==31536001L);
        assertTrue(test.seconds_in_year(2007)==31536000L);
        assertTrue(test.seconds_in_year(1972)==31622402L);

        boolean caught_exception = false;
        try {
            test.seconds_in_year(1970);
        } catch (IllegalArgumentException e) {
            caught_exception=true;
        }

        assertTrue(caught_exception);

        caught_exception=false;
        try {
            test.seconds_in_year(2020);
        } catch (IllegalArgumentException e) {
            caught_exception=true;
        }

        assertTrue(caught_exception);
    }


    /* that 1972 worked correctly
     * only year with 2 leap seconds
     */
    @Test
    public void test1972() {
        leapseconds test = load("leap-seconds.3535228800", 1972);
        if (test == null) {
            return;
        }

        /* calculate the interesting day of year numbers */
        double jan1mjd = test.mjd(1972, 1, 1.);
        double jul1mjd = test.mjd(1972, 7, 1.);
        double jan1mjd1973 = test.mjd(1973, 1, 1.);

        /* should be 0 from jan 1 to one day before jul1 */
        // +1 because the offset array is 1 based
        // as the gps clock starts the first day of year at 1
        int limit = (int)(jul1mjd-jan1mjd)+1;
        for(int index=0; index<limit; index++) {
            assertTrue(test.get_leap_offset(index)==0L);
        }

        /* should go from 0 to 1 from june 30 to jul 1
         * and stay there until jan 1 of 1973
         */
        int limit_jan1 = (int)(jan1mjd1973 - jan1mjd)+1;
        for(int index=limit; index<limit_jan1; index++) {
            assertTrue(test.get_leap_offset(index)==1L);
        }

        /* should go from 1 to 2 at jan 1 offset
         */
        assertTrue(test.get_leap_offset(limit_jan1)==2L);

    }
}
