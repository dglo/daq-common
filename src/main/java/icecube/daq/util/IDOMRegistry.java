package icecube.daq.util;

import java.util.Set;

public interface IDOMRegistry
{
    /**
     * Look up channel ID given mainboard ID
     * @param mbid DOM 12-char hexadecimal mainboard ID
     * @return channel Id (or <tt>-1</tt> if mainboard ID was not found)
     */
    short getChannelId(String mbid);
    /**
     * Look up string number given mainboard ID
     * @param mbid DOM 12-char hexadecimal mainboard ID
     * @return string number (or <tt>-1</tt> if mainboard ID was not found)
     */
    int getStringMajor(String mbid);
    /**
     * get the set of all known mainboard IDs
     * @return all known mainboard IDs
     */
    Set<String> keys();
    /**
     * get distance in meters between pair of DOMs
     */
    double distanceBetweenDOMs(String mbid0, String mbid1);
}
