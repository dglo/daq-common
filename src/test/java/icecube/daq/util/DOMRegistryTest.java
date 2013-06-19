package icecube.daq.util;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class DOMRegistryTest
{
    private static DOMRegistry registry;

    @Before
    public void setUp()
        throws Exception
    {
        if (registry == null) {
            //File configDir = LocatePDAQ.findConfigDirectory();
            File configDir =
                new File(getClass().getResource("/config").getPath());
            if (!configDir.exists()) {
                final String msg = "Cannot find config directory";
                throw new IllegalArgumentException(msg);
            }

            registry = DOMRegistry.loadRegistry(configDir);
        }
    }

    @Test
    public void testGetDom()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // Get "Cicero's" record
        final String idStr = "a18ce1e5b29c";
        DeployedDOM dom = registry.getDom(idStr);
        assertNotNull("Could not find " + idStr, dom);

        assertEquals("Cicero", dom.getName());
        assertEquals(0xa18ce1e5b29cL, dom.getNumericMainboardId());
    }

    @Test
    public void testGetChannelId()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // Let's try "Douglas Mawson"
        short chan = registry.getChannelId("5fa9ebf82828");
        assertEquals(701, (int) chan);
    }

    @Test
    public void testGetStringMajor()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // for "Sakigake"
        assertEquals(51, registry.getStringMajor("7ce3bc68a2d6"));
    }

    @Test
    public void testGetStringMinor()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // for "Sakigake"
        assertEquals(63, registry.getStringMinor("7ce3bc68a2d6"));
    }

    @Test
    public void testGetHubId() throws Exception
    {
        assertNotNull("Registry is null", registry);

        final String idStr = "7ce3bc68a2d6";
        DeployedDOM dom = registry.getDom(idStr);
        assertNotNull("Could not find " + idStr, dom);

        assertEquals(210, dom.getHubId());
    }

    @Test
    public void testDistanceBetweenDOMs()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

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
