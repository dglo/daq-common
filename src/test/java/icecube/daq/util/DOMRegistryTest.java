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
        final long mbid = 0xa18ce1e5b29cL;
        DeployedDOM dom = registry.getDom(mbid);
        assertNotNull("Could not find " + mbid, dom);

        assertEquals("Cicero", dom.getName());
        assertEquals(mbid, dom.getNumericMainboardId());
    }

    @Test
    public void testGetChannelId()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // Let's try "Douglas Mawson"
        short chan = registry.getChannelId(0x5fa9ebf82828L);
        assertEquals(701, (int) chan);
    }

    @Test
    public void testGetStringMajor()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // for "Sakigake"
        assertEquals(51, registry.getStringMajor(0x7ce3bc68a2d6L));
    }

    @Test
    public void testGetStringMinor()
        throws Exception
    {
        assertNotNull("Registry is null", registry);

        // for "Sakigake"
        assertEquals(63, registry.getStringMinor(0x7ce3bc68a2d6L));
    }

    @Test
    public void testGetHubId() throws Exception
    {
        assertNotNull("Registry is null", registry);

        final long mbid = 0x7ce3bc68a2d6L;
        DeployedDOM dom = registry.getDom(mbid);
        assertNotNull("Could not find " + mbid, dom);

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
                        assertEquals(dist, registry.distanceBetweenDOMs(d1.numericMainboardId, d2.numericMainboardId), 0.001);
                    }
                }
            }
        }
    }
}
