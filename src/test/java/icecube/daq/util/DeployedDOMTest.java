package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class DeployedDOMTest
{
    @Test
    public void testCreate()
	throws Exception
    {
	DeployedDOM dDOM = new DeployedDOM();

	assertEquals("Get ChannelID", (short)0, dDOM.getChannelId());
	assertEquals("Get MainboardId", "000000000000",
                     dDOM.getMainboardId());
	assertEquals("Get DomId", null, dDOM.getDomId());
	assertEquals("Get Name", null, dDOM.getName());
	assertEquals("Get String Major", 0, dDOM.getStringMajor());
	assertEquals("Get String Minor", 0, dDOM.getStringMinor());
	assertEquals("Get X", (double)0, dDOM.getX(), 0.01);
	assertEquals("Get Y", (double)0, dDOM.getY(), 0.01);
	assertEquals("Get Z", (double)0, dDOM.getZ(), 0.01);
	assertFalse("Not real DOM", dDOM.isRealDOM());

        final short channelId = 1234;
	final String mainboardId = "1234";
        final String domId = "012345678901";
        final String name = "foo";
        final int string = 11;
        final int location = 22;
        final double x = 1.23;
        final double y = 4.56;
        final double z = 7.89;

	dDOM.channelId = channelId;
        dDOM.mainboardId = mainboardId;
        dDOM.domId = domId;
        dDOM.name = name;
        dDOM.string = string;
        dDOM.location = location;
        dDOM.x = x;
        dDOM.y = y;
        dDOM.z = z;

	assertEquals("Get ChannelID", channelId, dDOM.getChannelId());
	assertEquals("Get MainboardId", mainboardId, dDOM.getMainboardId());
	assertEquals("Get DomId", domId, dDOM.getDomId());
	assertEquals("Get Name", name, dDOM.getName());
	assertEquals("Get String Major", string, dDOM.getStringMajor());
	assertEquals("Get String Minor", location, dDOM.getStringMinor());
	assertEquals("Get X", x, dDOM.getX(), 0.01);
	assertEquals("Get Y", y, dDOM.getY(), 0.01);
	assertEquals("Get Z", z, dDOM.getZ(), 0.01);
	assertTrue("Is real DOM", dDOM.isRealDOM());

	DeployedDOM dDOM1 = new DeployedDOM(dDOM);

	assertEquals("Get ChannelID", channelId, dDOM1.getChannelId());
	assertEquals("Get MainboardId", mainboardId, dDOM1.getMainboardId());
	assertEquals("Get DomId", domId, dDOM1.getDomId());
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
