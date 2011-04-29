package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import junit.framework.*;

public class DOMRegistryTest
	
{
    @Test
    public void testCreate()
	throws Exception
    {	
	final String mbid ="1234";

	DOMRegistry dom = new DOMRegistry();
	
	assertEquals("Pair ID", 5567, dom.pairId( 1, 2));	
	//assertEquals("Pair ID", 1, dom.pairId( mbid, "2345"));	
	//assertNotNull("DOM Registry", dom.loadRegistry( null));

	//dom.tabulateDistances();

	assertNull("Deployed DOM", dom.getDom( mbid));
	assertNull("Deployed DOM", dom.getDom((short)1));
	//assertEquals("get Dom Id", "1234", dom.getDomId(mbid));
	//assertEquals("get Channel Id", (short)0, dom.getChannelId(mbid));
	//assertEquals("Get Name", null, dom.getName(mbid));
	//assertEquals("Get String Major", 0, dom.getStringMajor(mbid));
	//assertEquals("Get String Minor", 0, dom.getStringMinor(mbid));
	//assertEquals("Get DeploymentLocation", 0, dom.getDeploymentLocation(mbid));
    }
   
}
