package icecube.daq.util;

/**
 * This class' sole purpose is to hold information about DOMs
 * that are <i>permanently</i> installed in the ice (or in IceTop).
 * @author krokodil
 */

public class DOMInfo
    implements Comparable<DOMInfo>
{
    public static final int NO_VALUE = Integer.MIN_VALUE;

    short channelId = Short.MIN_VALUE;
    String mainboardId;
    long numericMainboardId;
    String prodId;
    String name;
    /** component ID of the hub to which this channel is connected */
    int hubId;
    /** Logical string ID - note can be > 86 for test system and sim DOMs */
    int string = NO_VALUE;
    /** Modules' location along the string */
    int location = NO_VALUE;
    double x;
    double y;
    double z;

    /** Cached deployment location string (e.g. "1-1", "83-60", etc.) */
    private String deployLoc;

    /** Public constructor */
    public DOMInfo(long mbId, int string, int location)
    {
        this(mbId, string, location, string);
    }

    /** Public constructor */
    public DOMInfo(long mbId, int string, int location, int hubId)
    {
        numericMainboardId = mbId;
        this.string = string;
        this.location = location;
        this.hubId = hubId;

        channelId = computeChannelId(string, location);
    }

    /** Constructor only for package peers */
    DOMInfo() { }

    /**
     * Copy construtor.
     */
    DOMInfo(DOMInfo dom)
    {
        channelId = dom.channelId;
        mainboardId = dom.mainboardId;
        numericMainboardId = dom.numericMainboardId;
        prodId = dom.prodId;
        name = dom.name;
        hubId = dom.hubId;
        string = dom.string;
        location = dom.location;
        x = dom.x;
        y = dom.y;
        z = dom.z;
    }

    @Override
    public int compareTo(DOMInfo dom)
    {
        int diff = string - dom.string;
        if (diff == 0) {
            diff = location - dom.location;
            if (diff == 0) {
                long ldiff = numericMainboardId - dom.numericMainboardId;
                if (ldiff < 0) {
                    diff = -1;
                } else if (ldiff > 0) {
                    diff = 1;
                }
            }
        }
        return diff;
    }

    /**
     * Use DOM's string number and position to compute the channel ID.
     *
     * @return channel ID
     */
    public short computeChannelId()
    {
        return computeChannelId(string, location);
    }

    /**
     * Use string number and position to compute the channel ID.
     *
     * @param string string number (1-86, 201-211)
     * @param position string position (1-66)
     *
     * @return channel ID
     */
    public static final short computeChannelId(int string, int position)
    {
        final int kstring = string % 1000;

        if (kstring < 0 || kstring > 86) {
            // "string" 0 is for things like AMANDA and IceACT
            throw new Error("Impossible string " + kstring);
        }

        if (position < 1 || position > 66) {
            if (kstring == 0 && (position == 91 || position == 92)) {
                // grandfather in ancient AMANDA "DOM" positions
                return -1;
            }

            throw new Error("Impossible position " + position +
                            " for string " + string);
        }

        if (position > 64) {
            return (short) (6000 + ((kstring - 1) * 2) + (position - 65));
        }

        return (short) (kstring * 64 + (position - 1));
    }

    /**
     * Use channel ID to compute string number.
     *
     * @param chanId channel ID
     *
     * @return source ID
     */
    public static final int computeSourceId(short chanId)
    {
        if (chanId < 5998) {
            return (((int) chanId) / 64);
        }

        return ((((int) chanId) - 5998) / 2);
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof DOMInfo &&
            ((DOMInfo) obj).numericMainboardId == numericMainboardId;
    }

    public short getChannelId() {
        return channelId;
    }

    public String getDeploymentLocation()
    {
        if (deployLoc == null) {
            deployLoc = String.format("%d-%d", string, location);
        }

        return deployLoc;
    }

    public String getProductionId() {
        return prodId;
    }

    public int getHubId() {
        return hubId;
    }

    public String getMainboardId()
    {
        if (mainboardId == null) {
            mainboardId = String.format("%012x", numericMainboardId);
        }

        return mainboardId;
    }

    public String getName() {
        return name;
    }

    public long getNumericMainboardId() {
        return numericMainboardId;
    }

    public int getStringMajor() {
        return string;
    }

    public int getStringMinor() {
        return location;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getZ() {
        return z;
    }

    @Override
    public int hashCode()
    {
        return (int) (numericMainboardId ^ (numericMainboardId >> 32));
    }

    public boolean isInIce()
    {
        return (location >= 1 && location <= 60);
    }

    public boolean isIceACT()
    {
        return (string == 0 && location == 1);
    }

    public boolean isIceTop()
    {
        return (location >= 61 && location <= 64);
    }

    public boolean isRealDOM()
    {
        return (string >= 1 && string <= 86 &&
                location >= 1 && location <= 64);
    }

    public boolean isScintillator()
    {
        return (string >= 1 && string <= 86 &&
                location >= 65 && location <= 66);
    }

    @Override
    public String toString()
    {
        final String prodStr = (prodId == null ? "" : prodId);
        final String chanStr = (channelId == Short.MIN_VALUE ? "" :
                                "ch#" + channelId);
        final String nameStr = (name == null ? "" : " '" + name + "'");
        final String hubStr = (hubId == string ? "" : " hub " + hubId);

        final String majorStr;
        if (string == NO_VALUE) {
            majorStr = "??";
        } else {
            majorStr = Integer.toString(string);
        }

        final String minorStr;
        if (location == NO_VALUE) {
            minorStr = "??";
        } else {
            minorStr = Integer.toString(location);
        }

        final String omId = "(" + majorStr + ", " + minorStr + ")";
        return prodStr + "[" + getMainboardId() + "]" + chanStr + nameStr +
            " at " + omId + hubStr;
    }
}
