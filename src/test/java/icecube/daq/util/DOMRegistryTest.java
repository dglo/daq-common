package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
	DeployedDOM dDOM ;
	DeployedDOM dDOM1 = new DeployedDOM(dDOM);
	
	//assertEquals("Get hash code", 0, dDOM1.hashCode());
	assertEquals("Pair ID", 1, dom.pairId( 1, 1));	
	assertEquals("Pair ID", 1, dom.pairId( null, null));	
	assertNotNull("DOM Registry", dom.loadRegistry( null));

	//dom.tabulateDistances();

	dDOM = dom.getDom( "1234");
	assertNotNull("Deployed DOM", dom.getDom((short)1));
	assertEquals("get Dom Id", "1234", dom.getDomId(mbid));
	assertEquals("get Channel Id", (short)0, dom.getChannelId(mbid));
	assertEquals("Get Name", null, dom.getName(mbid));
	assertEquals("Get String Major", 0, dom.getStringMajor(mbid));
	assertEquals("Get String Minor", 0, dom.getStringMinor(mbid));
	assertEquals("Get DeploymentLocation", 0, dom.getDeploymentLocation(mbid));
    }
   
}
