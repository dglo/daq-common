package icecube.daq.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class DOMRegistryTest
{
    private DOMRegistry registry;
    
    @Before
    public void setUp() throws Exception
    {
        registry = DOMRegistry.loadRegistry(System.getenv("PDAQ_HOME") + "/config");
    }

    @Test
    public void testGetDom()
    {
        // Get "Cicero's" record
        DeployedDOM dom = registry.getDom("a18ce1e5b29c");
        assertEquals("Cicero", dom.getName());
    }

    @Test
    public void testGetChannelId()
    {   
        // Let's try "Douglas Mawson"
        short chan = registry.getChannelId("5fa9ebf82828");
        assertEquals(701, (int) chan);
    }

    @Test
    public void testGetStringMajor()
    {
        // for "Sagigake"
        assertEquals(210, registry.getStringMajor("7ce3bc68a2d6"));
        
    }

    @Test
    public void testGetStringMinor()
    {
        // for "Sagigake"
        assertEquals(63, registry.getStringMinor("7ce3bc68a2d6"));
    }

    @Test
    public void testPairID()
    {
    }
    
    @Test
    public void testDistanceBetweenDOMs()
    {
        
        for (int ich = 65; ich < 64*87; ich++)
        {
            
            DeployedDOM d1 = registry.getDom((short) ich);
            if (d1 != null) 
            {
            for (int jch = 64; jch < ich; jch++)
                {
                    DeployedDOM d2 = registry.getDom((short) jch);
                    if (d2 != null)
                    {
                        double dx = d2.x - d1.x;
                        double dy = d2.y - d1.y;
                        double dz = d2.z - d1.z;
                        double dist = Math.sqrt(dx*dx+dy*dy+dz*dz);
                        assertEquals(dist, registry.distanceBetweenDOMs(d1.mainboardId, d2.mainboardId), 0.001);
                    }
                }
            }
        }
    }

}
