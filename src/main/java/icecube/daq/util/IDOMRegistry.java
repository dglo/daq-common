package icecube.daq.util;

import java.util.Set;

public interface IDOMRegistry
{
    /**
     * Look up channel ID given mainboard ID
     * @param mbid DOM mainboard ID
     * @return channel Id (or <tt>-1</tt> if mainboard ID was not found)
     */
    short getChannelId(long mbid);
    /**
     * Lookup DOM given mainboard Id
     * @param mbId DOM mainboard id
     * @return deployed DOM information
     */
    DeployedDOM getDom(long mbId);
    /**
     * Lookup DOM based on channelID
     * @param channelId - 64*string + (module-1)
     * @return DeployedDOM object
     */
    DeployedDOM getDom(short channelId);
    /**
     * Return the set of all DOMs on a hub.
     * @param hubId hub ID
     * @return set of DOMs
     */
    Set<DeployedDOM> getDomsOnHub(int hubId);
    /**
     * Return the set of all DOMs (including icetop DOMs) associated with a
     * string.
     * @param string string number
     * @return set of DOMs
     */
    Set<DeployedDOM> getDomsOnString(int string);
    /**
     * Lookup name of DOM given mainboard Id.
     * @param mbid DOM mainboard id.
     * @return DOM name
     */
    String getName(long mbid);
    /**
     * Lookup production Id given mainboard Id
     * @param mbid DOM mainboard id
     * @return 8-char production Id (e.g. TP5Y0515)
     */
    String getProductionId(long mbid);
    /**
     * Look up string number given mainboard ID
     * @param mbid DOM mainboard ID
     * @return string number (or <tt>-1</tt> if mainboard ID was not found)
     */
    int getStringMajor(long mbid);
    /**
     * Look up position number given mainboard ID
     * @param mbid DOM mainboard ID
     * @return position number (or <tt>-1</tt> if mainboard ID was not found)
     */
    int getStringMinor(long mbid);
    /**
     * get the set of all known mainboard IDs
     * @return all known mainboard IDs
     */
    Set<Long> keys();
    /**
     * get the number of known mainboard IDs
     * @return number of known mainboard IDs
     */
    int size();
    /**
     * get distance in meters between pair of DOMs
     */
    double distanceBetweenDOMs(long mbid0, long mbid1);
}
