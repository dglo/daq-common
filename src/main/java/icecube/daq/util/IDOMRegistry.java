package icecube.daq.util;

import java.util.Set;

public interface IDOMRegistry
{
    /** Name of file containing all DOM data */
    String DEFAULT_DOM_GEOMETRY = "default-dom-geometry.xml";
    /** Name of SQLite file containing all DOM data */
    String DEFAULT_DOM_DATABASE = ".default-dom-geometry.db";

    /**
     * Return information for all DOMs
     * @return iterable for all DOMs
     */
    Iterable<DOMInfo> allDOMs()
        throws DOMRegistryException;
    /**
     * get distance in meters between pair of DOMs
     */
    double distanceBetweenDOMs(DOMInfo dom0, DOMInfo dom1);
    /**
     * get distance in meters between pair of DOMs
     */
    double distanceBetweenDOMs(short chan0, short chan1);
    /**
     * Look up channel ID given mainboard ID
     * @param mbid DOM mainboard ID
     * @return channel Id (or <tt>-1</tt> if mainboard ID was not found)
     */
    short getChannelId(long mbid);
    /**
     * Lookup DOM given mainboard Id
     * @param mbId DOM mainboard id
     * @return DOM information (<tt>null</tt> if not found)
     */
    DOMInfo getDom(long mbId);
    /**
     * Lookup DOM given string and position.
     * @param major string number
     * @param minor dom position (1-64)
     * @return DOM information (<tt>null</tt> if not found)
     */
    DOMInfo getDom(int major, int minor);
    /**
     * Lookup DOM based on channelID
     * @param channelId - 64*string + (module-1)
     * @return DOM information (<tt>null</tt> if not found)
     */
    DOMInfo getDom(short channelId);
    /**
     * Return the set of all DOMs on a hub.
     * @param hubId hub ID
     * @return set of DOMs
     * @throws DOMRegistryException if there is a problem
     */
    Set<DOMInfo> getDomsOnHub(int hubId)
        throws DOMRegistryException;
    /**
     * Return the set of all DOMs (including icetop DOMs) associated with a
     * string.
     * @param string string number
     * @return set of DOMs
     * @throws DOMRegistryException if there is a problem
     */
    Set<DOMInfo> getDomsOnString(int string)
        throws DOMRegistryException;
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
     * get the number of known mainboard IDs
     * @return number of known mainboard IDs
     * @throws DOMRegistryException if there is a problem
     */
    int size()
        throws DOMRegistryException;
}
