package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;

import org.junit.Test;
import junit.framework.*;

class MockDOMRegistry
    implements IDOMRegistry
{
    private HashMap<String, DeployedDOM> doms = new HashMap<String, DeployedDOM>();

    public void addEntry(long domId, int chanId)
    {
	doms.put("1234", dDOM);
        doms.put("2345", dDOM1);
	
    }

    public double distanceBetweenDOMs(String mbid0, String mbid1)
    {
        throw new Error("Unimplemented");
    }

    public short getChannelId(String mbid)
    {
        if (!map.containsKey(mbid)) {
            return -1;
        }

        return map.get(mbid).shortValue();
    }

    public int getStringMajor(String mbid)
    {
        throw new Error("Unimplemented");
    }

    public Set<String> keys()
    {
        throw new Error("Unimplemented");
    }

    public static String makeDOMString(long domId)
    {
        String domStr = Long.toHexString(domId);
        while (domStr.length() < 12) {
            domStr = "0" + domStr;
        }

        return domStr;
    }
}

public class DOMRegistryTest
	
{
    @Test
    public void testCreate()
	throws Exception
    {	
	final String mbid ="1234";
	final short chanId = 1;

	final DeployedDOM dDOM = new DeployedDOM();
	final DeployedDOM dDOM1 = new DeployedDOM();
	dDOM.channelId = chanId;
	dDOM1.channelId = chanId;

	HashMap<String, DeployedDOM> doms = new HashMap<String, DeployedDOM>()
	    {
		{
		    put("1234", dDOM);
		    put("2345", dDOM1);
		}
            };

	DOMRegistry dom = new DOMRegistry();

	//dom.doms = doms;
	
	assertEquals("Pair ID", 5567, dom.pairId( 1, 2));	
	assertEquals("Pair ID", 1, dom.pairId( null, null));	
	assertNotNull("DOM Registry", dom.loadRegistry("/home/pavithra/pdaq/config"));

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
