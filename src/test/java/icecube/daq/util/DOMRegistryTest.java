package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;

import org.junit.Test;
import junit.framework.*;
import java.util.Set;

public class DOMRegistryTest
	
{
    @Test
    public void testCreate()
	throws Exception,IllegalAccessException
    {	
	final String mbid = "1234";
	final String mbid1 = "2345";
	final short chanId = 1;
	final char[] ch = {'1','2'};
	Attributes attributes ;

	final DeployedDOM dDOM = new DeployedDOM();
	final DeployedDOM dDOM1 = new DeployedDOM();
	DeployedDOM[] dDOMs = new DeployedDOM[2];
	dDOM.channelId = chanId;
	dDOM1.channelId = chanId;
	dDOMs[1] = dDOM; 

	HashMap<String, DeployedDOM> map = new HashMap<String, DeployedDOM>();
	map.put(mbid, dDOM);
        map.put(mbid1, dDOM1);

	DOMRegistry dom = new DOMRegistry();
	Field field = DOMRegistry.class.getDeclaredField("doms");
	field.setAccessible(true);
	field.set(dom,map);

	Field field1 = DOMRegistry.class.getDeclaredField("domsByChannelId");
	field1.setAccessible(true);
	field1.set(dom,dDOMs);

	Method method = DOMRegistry.class.getDeclaredMethod("tabulateDistances");
	method.setAccessible(true);
	method.invoke(dom);
	
	assertEquals("Pair ID", 5567, dom.pairId( 1, 2));	
	assertEquals("Pair ID", 5566, dom.pairId( mbid, mbid1));	
	//assertNotNull("DOM Registry", dom.loadRegistry("/home/pavithra/pdaq/config"));

	assertNotNull("Deployed DOM", dom.getDom( mbid));
	assertNotNull("Deployed DOM", dom.getDom((short)chanId));
	assertEquals("get Dom Id", null, dom.getDomId(mbid));
	assertEquals("get Channel Id", (short)1, dom.getChannelId(mbid));
	assertEquals("Get Name", null, dom.getName(mbid));
	assertEquals("Get String Major", 0, dom.getStringMajor(mbid));
	assertEquals("Get String Minor", 0, dom.getStringMinor(mbid));
	//assertNotNull("Get DeploymentLocation", dom.getDeploymentLocation(mbid));
	assertNotNull("Get Keys", dom.keys());
	assertNotNull("Get Keys", dom.getDomsOnString(1234));
	assertEquals("distance Between DOMs", 0.0 , dom.distanceBetweenDOMs( mbid, mbid1));
	assertEquals("distance Between DOMs", 0.0 , dom.distanceXY( mbid, mbid1));
	assertEquals("distance Between DOMs", 0.0 , dom.verticalDistance( mbid, mbid1));
	dom.characters(ch, 0, 1);
	dom.endElement(null,"dom","dom");	
	dom.endElement(null,"position","dom");
	dom.endElement(null,"channelId","dom");
	dom.endElement(null,"mainBoardId","dom");
	dom.endElement(null,"name","dom");
	dom.endElement(null,"productionId","dom");
	dom.endElement(null,"xCoordinate","dom");
	dom.endElement(null,"yCoordinate","dom");
	dom.endElement(null,"zCoordinate","dom");
	dom.endElement(null,"number","dom");
	dom.startElement(null, "dom", "dom", null);
    }
   
}
