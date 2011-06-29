package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class FlasherboardConfigurationTest
{
    @Test
    public void testCreate()
	throws Exception
    {
        final String mbid = "1234";
	final int brightness = 50;
	final int width = 50;
	final int delay = 1;
	final int mask = 31;
	final int rate = 1;

	FlasherboardConfiguration fbc = new FlasherboardConfiguration();

	assertEquals("Get main board ID", null, fbc.getMainboardID());
	assertEquals("Get Brightness", 0, fbc.getBrightness());
	assertEquals("Get Width", 0, fbc.getWidth());
	assertEquals("Get Delay", 0, fbc.getDelay());
	assertEquals("Get Mask", 0, fbc.getMask());
	assertEquals("Get Rate", 0, fbc.getRate());
	assertNotNull("String", fbc.toString());

	fbc.setMainboardID(mbid);
	fbc.setBrightness(brightness);
	fbc.setWidth(width);
	fbc.setDelay(delay);
	fbc.setMask(mask);
	fbc.setRate(rate);

	assertEquals("Get main board ID", mbid, fbc.getMainboardID());
	assertEquals("Get Brightness", brightness, fbc.getBrightness());
	assertEquals("Get Width", width, fbc.getWidth());
	assertEquals("Get Delay", delay, fbc.getDelay());
	assertEquals("Get Mask", mask, fbc.getMask());
	assertEquals("Get Rate", rate, fbc.getRate());
	assertNotNull("String", fbc.toString());

	FlasherboardConfiguration fbc1 =
            new FlasherboardConfiguration(mbid,  brightness, width, delay,
                                          mask, rate);

	assertEquals("Get main board ID", mbid, fbc1.getMainboardID());
	assertEquals("Get Brightness", brightness, fbc1.getBrightness());
	assertEquals("Get Width", width, fbc1.getWidth());
	assertEquals("Get Delay", delay, fbc1.getDelay());
	assertEquals("Get Mask", mask, fbc1.getMask());
	assertEquals("Get Rate", rate, fbc1.getRate());
	assertNotNull("String", fbc1.toString());
    }
}
