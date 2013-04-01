package icecube.daq.util;

/**
 * This class' sole purpose is to hold information about DOMs
 * that are <i>permanently</i> installed in the ice (or in IceTop).
 * @author krokodil
 */

public class DeployedDOM
{
	short channelId;
	String mainboardId;
	long numericMainboardId;
	String domId;
	String name;
	/** component ID of the hub to which this channel is connected */
	int hubId;     
	/** Logical string ID - note can be > 86 for test system and sim DOMs */ 
	int string;
	/** Modules' location along the string */
	int location;
	double x;
	double y;
	double z;

	/** Constructor only for package peers */
	DeployedDOM() { }

	/**
	 * Copy construtor.
	 */
	DeployedDOM(DeployedDOM dom)
	{
		channelId  	= dom.channelId;
		mainboardId	= dom.mainboardId;
		numericMainboardId = dom.numericMainboardId;
		domId 		= dom.domId;
		name  		= dom.name;
		hubId       = dom.hubId;
		string 		= dom.string;
		location 	= dom.location;
		x			= dom.x;
		y			= dom.y;
		z 			= dom.z;
	}

	public short getChannelId() { return channelId; }

	public String getMainboardId() { return mainboardId; }
	
	public long getNumericMainboardId() { return numericMainboardId; }

	public String getDomId() { return domId; }

	public String getName() { return name; }

	public int getHubId() { return hubId; }
	
	public int getStringMajor() { return string; }

	public int getStringMinor() { return location; }

	public double getX() { return x; }
	public double getY() { return y; }
	public double getZ() { return z; }

	public boolean isRealDOM()
	{
	    return (string >= 1 && string <= 86);
	}

	@Override
	public String toString()
	{
	    return domId + "[" + mainboardId + "]" + channelId + " '" + name +
                "' at " + String.format("%02d-%02d", string, location);
	}

	@Override
	public boolean equals(Object obj)
	{
	    return (obj instanceof DeployedDOM &&
	            ((DeployedDOM) obj).mainboardId.equals(mainboardId));
	}

	@Override
	public int hashCode()
	{
	     return mainboardId.hashCode();
	}
}
