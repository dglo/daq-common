package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class RealTimeRateMeterTest
	
{
    @Test
    public void testCreate()
	throws Exception
    {
        final long interval = 1234L;
	final long utc = 2468L;
	final double wt = 50.00;
	
	RealTimeRateMeter rtm = new RealTimeRateMeter(interval);

	rtm.recordEvent( utc, wt);
	rtm.recordEvent( utc);

	assertEquals("Get rate", 0.00, rtm.getRate());
	assertEquals("Get Time", interval, rtm.getTime());
	
    }
   
}
