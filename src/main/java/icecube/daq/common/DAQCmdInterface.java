/*
 * class: CmdInterface
 *
 * Version $Id: DAQCmdInterface.java 2230 2007-11-02 16:13:07Z dglo $
 *
 * Date: March 8 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.common;

/**
 * This class defines constants that are used through the DAQ software.
 *
 * @version $Id: DAQCmdInterface.java 2230 2007-11-02 16:13:07Z dglo $
 * @author mcp
 */
public interface DAQCmdInterface
{
    public static final int DAQ_MAX_NUM_STRINGS = 80;
    public static final int DAQ_MAX_NUM_IDH = 16;

    public static final String DAQ_PAYLOAD_INVALID_SOURCE_ID =
            "payloadInvalidSourceId";

    // names for all component types
    public static final String DAQ_DOMHUB =
            "domHub";
    public static final String DAQ_STRINGPROCESSOR =
            "stringProcessor";
    public static final String DAQ_ICETOP_DATA_HANDLER =
            "iceTopDataHandler";
    public static final String DAQ_INICE_TRIGGER =
            "inIceTrigger";
    public static final String DAQ_ICETOP_TRIGGER =
            "iceTopTrigger";
    public static final String DAQ_AMANDA_TRIGGER =
            "amandaTrigger";
    public static final String DAQ_GLOBAL_TRIGGER =
            "globalTrigger";
    public static final String DAQ_EVENTBUILDER =
            "eventBuilder";
    public static final String DAQ_MONITORBUILDER =
            "monitorBuilder";
    public static final String DAQ_SNBUILDER =
            "snBuilder";
    public static final String DAQ_TCALBUILDER =
            "tcalBuilder";
    public static final String DAQ_SECONDARY_BUILDERS =
            "secondaryBuilders";
    public static final String DAQ_STRING_HUB =
            "stringHub";
    
    public static final String DAQ_ONLINE_RUNSTART_FLAG =
            "RunStart:";
    public static final String DAQ_ONLINE_RUNSTOP_FLAG =
            "RunStop:";

    public static final String SOURCE = "source";
    public static final String SINK = "sink";
}
