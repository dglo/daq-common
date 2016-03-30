package icecube.daq.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

/**
 * The DOM registry is a utility class for looking up DOM information.
 * @author krokodil
 */
public class DOMRegistry
    implements IDOMRegistry
{
    /** Name of file containing all DOM data */
    public static final String DEFAULT_DOM_GEOMETRY =
        "default-dom-geometry.xml";
    /** Total number of in-ice and icetop DOMs */
    private static final int NCH = 87*64;
    /** Maximum channel ID */
    private static final int MAX_CHANNEL_ID = 6171;

    private static final Log LOG = LogFactory.getLog(DOMRegistry.class);

    private static File cachedPath;
    private static DOMRegistry cachedRegistry;
    private HashMap<Long, DeployedDOM> doms;
    private DeployedDOM[] domsByChannelId;
    private double[] distanceTable;

    protected DOMRegistry(HashMap<Long, DeployedDOM> doms,
                          DeployedDOM[] domsByChannelId)
    {
        this.doms = doms;
        this.domsByChannelId = domsByChannelId;

        /* Only need about 15M pairs */
        distanceTable = new double[NCH*(NCH-1)/2];
    }

    public double distanceBetweenDOMs(long mbid0, long mbid1)
    {
        if (mbid0 == mbid1) return 0.0;
        return distanceTable[tableIndex(doms.get(mbid0), doms.get(mbid1))];
    }

    /**
     * Lookup channel ID given mainboard ID
     * @param mbid DOM mainboard ID
     * @return channel Id (or <tt>-1</tt> if mainboard ID was not found)
     */
    public short getChannelId(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot find channel for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return -1;
        }

        return dom.channelId;
    }

    public String getDeploymentLocation(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot find location for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return null;
        }

        return String.format("%2d-%2d", dom.string, dom.location);
    }

    /**
     * Lookup DOM given mainboard Id
     * @param mbid input DOM mainboard id - the 12-char hex
     * @return deployed DOM information
     */
    public DeployedDOM getDom(long mbid)
    {
        return doms.get(mbid);
    }

    /**
     * Lookup DOM given string and position.
     * @param major string number
     * @param minor dom position (1-64)
     * @return deployed DOM information
     */
    public DeployedDOM getDom(int major, int minor)
    {
        return getDom(DeployedDOM.computeChannelId(major, minor));
    }

    /**
     * Lookup DOM based on channelID
     * @param ch channel ID - 64*string + (module-1)
     * @return DeployedDOM object
     */
    public DeployedDOM getDom(short ch)
    {
        if (ch < 0 || ch >= domsByChannelId.length) {
            LOG.error("Cannot fetch DOM entry for channel " + ch +
                      " (domsByChannelId*" + domsByChannelId.length + ")");
            return null;
        }
        return domsByChannelId[ch];
    }

    /**
     * Lookup DOM Id given mainboard Id
     * @param mbid DOM mainboard id
     * @return 8-char DOM Id - like TP5Y0515
     */
    public String getDomId(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot fetch DOM entry for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return null;
        }

        return dom.domId;
    }

    /**
     * Return the set of all DOMs on a hub.  Note that IceTop DOMs are on
     * an icetop hub and will not be returned with the DOMS on an in-ice hub.
     * @param hubId hub ID
     * @return set of DOMs
     */
    public Set<DeployedDOM> getDomsOnHub(int hubId)
    {
        HashSet<DeployedDOM> rlist = new HashSet<DeployedDOM>(60);
        for (DeployedDOM dom : doms.values()) {
            if (hubId == dom.hubId) {
                rlist.add(dom);
            }
        }

        return rlist;
    }

    /**
     * Return the set of all DOMs associated with a string.
     * @param string string number
     * @return set of DOMs
     */
    public Set<DeployedDOM> getDomsOnString(int string)
    {
        HashSet<DeployedDOM> rlist = new HashSet<DeployedDOM>(66);
        for (DeployedDOM dom : doms.values()) {
            if (string == dom.string) {
                rlist.add(dom);
            }
        }

        return rlist;
    }

    /**
     * Lookup name of DOM given mainboard Id.
     * @param mbid DOM mainboard id.
     * @return DOM name
     */
    public String getName(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot find name for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return null;
        }

        return dom.name;
    }

    public int getStringMajor(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot find string major for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return -1;
        }

        return dom.getStringMajor();
    }

    public int getStringMinor(long mbid)
    {
        DeployedDOM dom = doms.get(mbid);
        if (dom == null) {
            final String errmsg =
                String.format("Cannot find string minor for %012x (doms=%d)",
                              mbid, doms.size());
            LOG.error(errmsg);
            return -1;
        }

        return dom.getStringMinor();
    }

    public Set<Long> keys()
    {
        return doms.keySet();
    }

    public static DOMRegistry loadRegistry()
        throws ParserConfigurationException, SAXException, IOException
    {
        return loadRegistry(LocatePDAQ.findConfigDirectory());
    }

    public static DOMRegistry loadRegistry(String path)
        throws ParserConfigurationException, SAXException, IOException
    {
        return loadRegistry(new File(path));
    }

    public static synchronized DOMRegistry loadRegistry(File path)
        throws ParserConfigurationException, SAXException, IOException
    {
        if (cachedRegistry == null || cachedPath == null ||
            !path.equals(cachedPath))
        {
            DOMRegistryParser parser =
                new DOMRegistryParser(path, MAX_CHANNEL_ID + 1);

            DOMRegistry reg = parser.getRegistry();
            reg.tabulateDistances();

            cachedPath = path;
            cachedRegistry = reg;
        }

        return cachedRegistry;
    }

    /**
     * get the number of known mainboard IDs
     * @return number of known mainboard IDs
     */
    public int size()
    {
        return doms.size();
    }

    /**
     * Return the tightly packed pair for the two DOMs
     *
     * @param d1 first DOM
     * @param d2 second DOM
     *
     * @return index into DOM arrays
     */
    private static int tableIndex(DeployedDOM d1, DeployedDOM d2)
    {
        int ch1 = d1.channelId;
        int ch2 = d2.channelId;

        if (ch1 < ch2) {
            // swap ch1 and ch2
            int tmp = ch2;
            ch2 = ch1;
            ch1 = tmp;
        }

        return ch2 * NCH + ch1 - (ch2+1)*(ch2+2)/2;
    }

    private void tabulateDistances()
    {
        DeployedDOM[] mlist = doms.values().toArray(new DeployedDOM[0]);
        for (int ch0 = 0; ch0 < mlist.length; ch0++)
        {
            DeployedDOM d0 = mlist[ch0];
            if (d0.isRealDOM() && !d0.isScintillator())
            {
                for (int ch1 = 0; ch1 < ch0; ch1++)
                {
                    DeployedDOM d1 = mlist[ch1];
                    if (d1.isRealDOM() && !d1.isScintillator())
                    {
                        int pid = tableIndex(d0, d1);
                        if (pid >= NCH*(NCH-1)/2)
                        {
                            System.err.println("ERROR - d0/d1: " + ch0 + "/" +
                                               ch1);
                        }
                        double dx = d0.x - d1.x;
                        double dy = d0.y - d1.y;
                        double dz = d0.z - d1.z;
                        double rho2 = dx * dx + dy * dy;
                        double dist = Math.sqrt(dz * dz + rho2);
                        distanceTable[pid] = dist;
                    }
                }
            }
        }
    }
}
