/* -*- mode: java; indent-tabs-mode:t; tab-width:4 -*- */

package icecube.daq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The DOM registry is a utility class for looking up DOM information.
 * @author krokodil
 *
 */
public class DOMRegistry
	extends DefaultHandler
	implements IDOMRegistry
{
	private static final Log LOG = LogFactory.getLog(DOMRegistry.class);

	private static File cachedPath;
	private static DOMRegistry cachedRegistry;
	private StringBuilder xmlChars;
	private HashMap<Long, DeployedDOM> doms;
	private DeployedDOM[] domsByChannelId;
	private DeployedDOM currentDOM;
	private int currentHubId;
	private int originalString;
	private static final String DEFAULT_DOM_GEOMETRY =
		"default-dom-geometry.xml";
	private static final int NCH = 87*64;
	private double[] distanceTable;
	private double[] distanceTabXY;

	protected DOMRegistry()
	{
		xmlChars = new StringBuilder();
		currentDOM = new DeployedDOM();
		doms = new HashMap<Long, DeployedDOM>();
		domsByChannelId = new DeployedDOM[NCH];
		/* Only need about 15M pairs */
		distanceTable = new double[NCH*(NCH-1)/2];
		distanceTabXY = new double[NCH*(NCH-1)/2];
	}

	public static DOMRegistry loadRegistry(String path)
		throws ParserConfigurationException, SAXException, IOException
	{
		File f = new File(path);
		if (!f.exists()) {
			throw new FileNotFoundException("Registry does not exist in \"" +
											path + "\"");
		}

		return loadRegistry(f);
	}

	public static synchronized DOMRegistry loadRegistry(File path)
		throws ParserConfigurationException, SAXException, IOException
	{
		if (cachedRegistry != null && cachedPath != null &&
			path.equals(cachedPath))
		{
			return cachedRegistry;
		}

		File file = new File(path, DEFAULT_DOM_GEOMETRY);
		FileInputStream is = new FileInputStream(file);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			SAXParser parser = factory.newSAXParser();
			DOMRegistry reg = new DOMRegistry();
			parser.parse(is, reg);
			reg.tabulateDistances();

			cachedPath = path;
			cachedRegistry = reg;

			return reg;
		} finally {
			is.close();
		}
	}

	/**
	 * Return the tightly packed pair given reduced channel ID as input
	 * @param ch1 - reduced channel ID of DOM #1
	 * @param ch2 - reduced channel ID of DOM #2
	 */
	private int pairId(int ch1, int ch2)
	{
		if (ch1 < ch2) {
			// swap ch1 and ch2
			int tmp = ch2;
			ch2 = ch1;
			ch1 = tmp;
		}
		return ch2 * NCH + ch1 - (ch2+1)*(ch2+2)/2;
	}

	private int pairId(long mbid1, long mbid2)
	{
		int ch1 = doms.get(mbid1).channelId;
		int ch2 = doms.get(mbid2).channelId;
		return pairId(ch1, ch2);
	}

	private void tabulateDistances()
	{
		DeployedDOM[] mlist = doms.values().toArray(new DeployedDOM[0]);
		for (int ch0 = 0; ch0 < mlist.length; ch0++)
		{
			DeployedDOM d0 = mlist[ch0];
			if (d0.isRealDOM())
			{
				for (int ch1 = 0; ch1 < ch0; ch1++)
				{
					DeployedDOM d1 = mlist[ch1];
					if (d1.isRealDOM())
					{
						int pid = pairId(d0.channelId, d1.channelId);
						if (pid >= NCH*(NCH-1)/2)
						{
							System.err.println("ERROR - d0/d1: " + ch0 + "/" + ch1);
						}
						double dx = d0.x - d1.x;
						double dy = d0.y - d1.y;
						double dz = d0.z - d1.z;
						double rho2 = dx * dx + dy * dy;
						double dist = Math.sqrt(dz * dz + rho2);
						distanceTabXY[pid] = Math.sqrt(rho2);
						distanceTable[pid] = dist;
					}
				}
			}
		}
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
		return getDom((short) ((major % 1000) * 64 + (minor - 1)));
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

	public Set<Long> keys()
	{
		return doms.keySet();
	}

	/**
     * get the number of known mainboard IDs
     * @return number of known mainboard IDs
     */
	public int size()
	{
		return doms.size();
	}

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

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		super.characters(ch, start, length);
		xmlChars.append(ch, start, length);
	}

	@Override
	public void startElement(String uri, String localName, String qName,
							 Attributes attributes) throws SAXException
	{
		super.startElement(uri, localName, qName, attributes);
		xmlChars.setLength(0);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
		throws SAXException
	{
		super.endElement(uri, localName, qName);
		String txt = xmlChars.toString().trim();
		if (localName.equalsIgnoreCase("dom"))
		{
			currentDOM.hubId = currentHubId;

			if (originalString > 0)
				currentDOM.string = originalString;
			else
				currentDOM.string = currentHubId;

			doms.put(currentDOM.numericMainboardId, currentDOM);
			if (currentDOM.isRealDOM())
				domsByChannelId[currentDOM.channelId] = currentDOM;

			currentDOM = new DeployedDOM();
			originalString = 0;
		}
		else if (localName.equalsIgnoreCase("position"))
		{
			currentDOM.location   = Integer.parseInt(txt);
		}
		else if (localName.equalsIgnoreCase("channelId"))
			currentDOM.channelId = Short.parseShort(txt);
		else if (localName.equalsIgnoreCase("mainBoardId"))
		{
			currentDOM.mainboardId = txt;
			currentDOM.numericMainboardId =
				Long.parseLong(currentDOM.mainboardId, 16);
		}
		else if (localName.equalsIgnoreCase("name"))
			currentDOM.name = txt;
		else if (localName.equalsIgnoreCase("productionId"))
			currentDOM.domId = txt;
		else if (localName.equalsIgnoreCase("xCoordinate"))
			currentDOM.x = Double.parseDouble(txt);
		else if (localName.equalsIgnoreCase("yCoordinate"))
			currentDOM.y = Double.parseDouble(txt);
		else if (localName.equalsIgnoreCase("zCoordinate"))
			currentDOM.z = Double.parseDouble(txt);
		else if (localName.equalsIgnoreCase("number"))
			currentHubId = Integer.parseInt(txt);
		else if (localName.equals("originalString"))
			originalString = Integer.parseInt(txt);
	}

	public double distanceBetweenDOMs(long mbid0, long mbid1)
	{
		if (mbid0 == mbid1) return 0.0;
		return distanceTable[pairId(mbid0, mbid1)];
	}

	private double distanceXY(long mbid0, long mbid1)
	{
		if (mbid0 == mbid1) return 0.0;
		return distanceTabXY[pairId(mbid0, mbid1)];
	}

	private double verticalDistance(long mbid0, long mbid1)
	{
		if (mbid0 == mbid1) return 0.0;

		final DeployedDOM d0 = doms.get(mbid0);
		final DeployedDOM d1 = doms.get(mbid1);
		if (d0 == null || d1 == null) {
			LOG.error("Cannot compute DOM vertical distance for " +
					  String.format("%012x and %012x", mbid0, mbid1));
			return Long.MAX_VALUE;
		}
		return d1.z - d0.z;
	}
}
