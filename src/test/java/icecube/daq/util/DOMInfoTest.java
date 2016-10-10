package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class DOMInfoTest
{
    @Test
    public void testCreate()
	throws Exception
    {
	DOMInfo dDOM = new DOMInfo();

	assertEquals("Get ChannelID", Short.MIN_VALUE, dDOM.getChannelId());
	assertEquals("Get MainboardId", "000000000000",
                     dDOM.getMainboardId());
	assertEquals("Get DomId", null, dDOM.getProductionId());
	assertEquals("Get Name", null, dDOM.getName());
	assertEquals("Get String Major",
				 DOMInfo.NO_VALUE, dDOM.getStringMajor());
	assertEquals("Get String Minor",
				 DOMInfo.NO_VALUE, dDOM.getStringMinor());
	assertEquals("Get X", (double)0, dDOM.getX(), 0.01);
	assertEquals("Get Y", (double)0, dDOM.getY(), 0.01);
	assertEquals("Get Z", (double)0, dDOM.getZ(), 0.01);
	assertFalse("Not real DOM", dDOM.isRealDOM());

        final short channelId = 1234;
	final String mainboardId = "1234";
        final String prodId = "ABC1234Z";
        final String name = "foo";
        final int string = 11;
        final int location = 22;
        final double x = 1.23;
        final double y = 4.56;
        final double z = 7.89;

	dDOM.channelId = channelId;
        dDOM.mainboardId = mainboardId;
        dDOM.prodId = prodId;
        dDOM.name = name;
        dDOM.string = string;
        dDOM.location = location;
        dDOM.x = x;
        dDOM.y = y;
        dDOM.z = z;

	assertEquals("Get ChannelID", channelId, dDOM.getChannelId());
	assertEquals("Get MainboardId", mainboardId, dDOM.getMainboardId());
	assertEquals("Get DomId", prodId, dDOM.getProductionId());
	assertEquals("Get Name", name, dDOM.getName());
	assertEquals("Get String Major", string, dDOM.getStringMajor());
	assertEquals("Get String Minor", location, dDOM.getStringMinor());
	assertEquals("Get X", x, dDOM.getX(), 0.01);
	assertEquals("Get Y", y, dDOM.getY(), 0.01);
	assertEquals("Get Z", z, dDOM.getZ(), 0.01);
	assertTrue("Is real DOM", dDOM.isRealDOM());

	DOMInfo dDOM1 = new DOMInfo(dDOM);

	assertEquals("Get ChannelID", channelId, dDOM1.getChannelId());
	assertEquals("Get MainboardId", mainboardId, dDOM1.getMainboardId());
	assertEquals("Get DomId", prodId, dDOM1.getProductionId());
	assertEquals("Get Name", name, dDOM1.getName());
	assertEquals("Get String Major", string, dDOM1.getStringMajor());
	assertEquals("Get String Minor", location, dDOM1.getStringMinor());
	assertEquals("Get X", x, dDOM1.getX(), 0.01);
	assertEquals("Get Y", y, dDOM1.getY(), 0.01);
	assertEquals("Get Z", z, dDOM1.getZ(), 0.01);
	assertTrue("Is real DOM", dDOM1.isRealDOM());

	assertNotNull("Get hash code", dDOM1.hashCode());
	assertEquals("Equals", true, dDOM1.equals(dDOM));
	assertNotNull("String", dDOM1.toString());
    }
}
