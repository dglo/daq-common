package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class DeployedDOMTest
	
{
    @Test
    public void testCreate()
	throws Exception
    {	
	final String mainboardId = "1234";
	
	DeployedDOM dDOM = new DeployedDOM();

	dDOM.mainboardId = mainboardId;

	DeployedDOM dDOM1 = new DeployedDOM(dDOM);
	
	assertEquals("Get ChannelID", (short)0, dDOM1.getChannelId());
	assertEquals("Get MainboardId", mainboardId, dDOM1.getMainboardId());
	assertEquals("Get DomId", null, dDOM1.getDomId());
	assertEquals("Get Name", null, dDOM1.getName());
	assertEquals("Get String Major", 0, dDOM1.getStringMajor());
	assertEquals("Get String Minor", 0, dDOM1.getStringMinor());
	assertEquals("Get X", (double)0, dDOM1.getX());
	assertEquals("Get Y", (double)0, dDOM1.getY());
	assertEquals("Get Z", (double)0, dDOM1.getZ());
	assertEquals("Is real DOM", false, dDOM1.isRealDOM());
	assertNotNull("Get hash code", dDOM1.hashCode());
	assertEquals("Equals", true, dDOM1.equals(dDOM1));	
	assertNotNull("String", dDOM1.toString());
	
    }
   
}
