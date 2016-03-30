package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class RealTimeRateMeterTest
{
    private double computeRate(double sum, long interval)
    {
        return sum / interval * 1.0E10;
    }

    private long computeTime(long utc, long interval)
    {
        return (utc / interval * interval) - interval;
    }

    @Test
    public void testCreate()
	throws Exception
    {
        final long interval = 3L;

	RealTimeRateMeter rtm = new RealTimeRateMeter(interval);

	assertEquals("Get rate0", 0.00, rtm.getRate(), 0.01);
	assertEquals("Get Time0", 0L - interval, rtm.getTime());

	final long utc = 2468L;
	final double wt = 50.00;

	rtm.recordEvent(utc, wt);

	assertEquals("Get rate1", computeRate(0.00, interval), rtm.getRate(), 0.01);
	assertEquals("Get Time1", computeTime(utc, interval), rtm.getTime());

        final long utc2 = utc + 1000L;

	rtm.recordEvent(utc2);

	assertEquals("Get rate2", computeRate(wt, interval), rtm.getRate(), 0.01);
	assertEquals("Get Time2", computeTime(utc2, interval), rtm.getTime());
    }
}
