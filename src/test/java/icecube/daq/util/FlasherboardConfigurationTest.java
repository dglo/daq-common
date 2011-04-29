package icecube.daq.util;

import static org.junit.Assert.assertEquals;
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
	FlasherboardConfiguration fbc1 = new FlasherboardConfiguration( mbid, 
		brightness, width, delay, mask, rate);

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
	assertEquals("String ", "FlasherboardConfiguration[" + mbid + ",bri=" +
		brightness + ",width=" + width + ",delay=" + delay + ",mask=" + 
		mask + ",rate=" + rate + "]", fbc.toString());
	
    }
   
}
