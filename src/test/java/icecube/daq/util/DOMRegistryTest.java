package icecube.daq.util;

import icecube.daq.common.test.MockAppender;

import java.io.File;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

class DOMData
{
    final long mbid;
    final int hub;
    final int string;
    final int position;
    final String name;
    final String productionID;

    DOMData(long mbid, int string, int position, String name,
            String productionID)
    {
        this(mbid, string, string, position, name, productionID);
    }

    DOMData(long mbid, int hub, int string, int position, String name,
            String productionID)
    {
        this.mbid = mbid;
        this.hub = hub;
        this.string = string;
        this.position = position;
        this.name = name;
        this.productionID = productionID;
    }

    short getChannelId()
    {
        int kstring = string % 1000;

        if (position > 64) {
            return (short) ((6000 + ((kstring - 1) * 2) + (position - 65)) &
                            0xffff);
        }

        return (short) (((kstring * 64) + (position - 1)) & 0xffff);
    }

    String getDeploymentLocation()
    {
        return String.format("%2d-%2d", string, position);
    }
}

public class DOMRegistryTest
{
    private static final MockAppender appender =
        new MockAppender(/*org.apache.log4j.Level.ALL*/)/*.setVerbose(true)*/;

    private static DOMRegistry registry;

    private DOMData[] domData = new DOMData[] {
        new DOMData(0x98b7b6b98e9fL, 10, 1, "Banshee", "UP7P2542"),
        new DOMData(0x4696f5e99e5dL, 10, 60, "Ballet", "TP7P2487"),
        new DOMData(0xa18ce1e5b29cL, 37, 60, "Cicero", "TP8P3409"),
        new DOMData(0x5fa9ebf82828L, 204, 10, 62, "Douglas_Mawson",
                    "AP7P2096"),
        new DOMData(0x7ce3bc68a2d6L, 210, 51, 63, "Sakigake", "AP8P3026"),
    };

    private void assertLogMessage(String message)
    {
        try {
            assertEquals("Bad number of log messages", 1,
                         appender.getNumberOfMessages());
            String logMsg = (String) appender.getMessage(0);
            assertTrue("Unexpected log message " + logMsg,
                       logMsg.startsWith(message));
        } finally {
            appender.clear();
        }
    }

    private void assertNoLogMessage()
    {
        try {
            assertEquals("Bad number of log messages", 0,
                         appender.getNumberOfMessages());
        } finally {
            appender.clear();
        }
    }

    @Before
    public void setUp()
        throws Exception
    {
        appender.clear();

        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(appender);

        if (registry == null) {
            File configDir =
                new File(getClass().getResource("/config").getPath());
            if (!configDir.exists()) {
                final String msg = "Cannot find config directory";
                throw new IllegalArgumentException(msg);
            }

            registry = DOMRegistry.loadRegistry(configDir);
        }
    }

    @After
    public void tearDown()
        throws Exception
    {
        assertEquals("Bad number of log messages",
                     0, appender.getNumberOfMessages());
    }

    @Test
    public void testUnknownMBID()
        throws Exception
    {
        final long badMBID = 0L;

        assertEquals("Found channel ID for bad MBID " + badMBID,
                     (short) -1, registry.getChannelId(badMBID));
        assertLogMessage("Cannot find channel for 000000000000");

        assertNull("Found location for bad MBID " + badMBID,
                   registry.getDeploymentLocation(badMBID));
        assertLogMessage("Cannot find location for 000000000000");

        assertNull("Found name for bad MBID " + badMBID,
                   registry.getDom(badMBID));
        assertNoLogMessage();

        assertNull("Found DOM ID for bad MBID " + badMBID,
                   registry.getDomId(badMBID));
        assertLogMessage("Cannot fetch DOM entry for 000000000000");

        assertNull("Found name for bad MBID " + badMBID,
                   registry.getName(badMBID));
        assertLogMessage("Cannot find name for 000000000000");

        assertEquals("Found major number for bad MBID " + badMBID,
                     -1, registry.getStringMajor(badMBID));
        assertLogMessage("Cannot find string major for 000000000000");

        assertEquals("Found minor number for bad MBID " + badMBID,
                     -1, registry.getStringMinor(badMBID));
        assertLogMessage("Cannot find string minor for 000000000000");
    }

    @Test
    public void testBasicFunctions()
        throws Exception
    {
        for (DOMData dom : domData) {
            DeployedDOM ddom = registry.getDom(dom.mbid);
            assertNotNull("Could not find " + dom.name, ddom);
            assertEquals(dom.name, ddom.getName());
            assertEquals(dom.name, dom.mbid, ddom.getNumericMainboardId());
            assertEquals(dom.name, dom.hub, ddom.getHubId());
            assertEquals(dom.name, dom.string, ddom.getStringMajor());
            assertEquals(dom.name, dom.position, ddom.getStringMinor());
            assertEquals(dom.name, dom.getChannelId(), ddom.getChannelId());

            assertEquals(dom.name, registry.getName(dom.mbid));
            assertEquals(dom.name, dom.string,
                         registry.getStringMajor(dom.mbid));
            assertEquals(dom.name, dom.position,
                         registry.getStringMinor(dom.mbid));
            assertEquals(dom.name, dom.getChannelId(),
                         registry.getChannelId(dom.mbid));
            assertEquals(dom.name, dom.getDeploymentLocation(),
                         registry.getDeploymentLocation(dom.mbid));
        }
    }

    @Test
    public void testDistanceBetweenDOMs()
        throws Exception
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
                        assertEquals(dist, registry.distanceBetweenDOMs(d1.numericMainboardId, d2.numericMainboardId), 0.001);
                    }
                }
            }
        }
    }

    @Test
    public void testGetDomsOnHub()
        throws Exception
    {
        final int hubId = 10;
        Set<DeployedDOM> doms = registry.getDomsOnHub(hubId);

        int num = 0;
        for (DOMData dom : domData) {
            if (dom.hub == hubId) {
                DeployedDOM ddom = registry.getDom(dom.mbid);

                assertTrue("Cannot find " + dom.name + " on hub " + hubId,
                           doms.contains(ddom));
                num++;
            }
        }
        assertEquals("Bad number of DOMs for hub " + hubId,
                     num, doms.size());
    }

    @Test
    public void testGetDomsOnString()
        throws Exception
    {
        final int string = 10;
        Set<DeployedDOM> doms = registry.getDomsOnString(string);

        int num = 0;
        for (DOMData dom : domData) {
            if (dom.string == string) {
                DeployedDOM ddom = registry.getDom(dom.mbid);

                assertTrue("Cannot find " + dom.name + " on string " + string,
                           doms.contains(ddom));
                num++;
            }
        }
        assertEquals("Bad number of DOMs for string " + string,
                     num, doms.size());
    }
}
