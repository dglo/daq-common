/*
 * class: CmdInterface
 *
 * Version $Id: DAQCmdInterface.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: March 8 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.common;

/**
 * This class defines constants that are used through the DAQ software.
 *
 * @version $Id: DAQCmdInterface.java 4574 2009-08-28 21:32:32Z dglo $
 * @author mcp
 */
public interface DAQCmdInterface
{
    int DAQ_MAX_NUM_STRINGS = 86;
    int DAQ_MAX_NUM_IDH = 16;

    String DAQ_PAYLOAD_INVALID_SOURCE_ID =
            "payloadInvalidSourceId";

    // names for all component types
    String DAQ_DOMHUB =
            "domHub";
    String DAQ_STRINGPROCESSOR =
            "stringProcessor";
    String DAQ_ICETOP_DATA_HANDLER =
            "iceTopDataHandler";
    String DAQ_INICE_TRIGGER =
            "inIceTrigger";
    String DAQ_ICETOP_TRIGGER =
            "iceTopTrigger";
    String DAQ_AMANDA_TRIGGER =
            "amandaTrigger";
    String DAQ_GLOBAL_TRIGGER =
            "globalTrigger";
    String DAQ_EVENTBUILDER =
            "eventBuilder";
    String DAQ_MONITORBUILDER =
            "monitorBuilder";
    String DAQ_SNBUILDER =
            "snBuilder";
    String DAQ_TCALBUILDER =
            "tcalBuilder";
    String DAQ_SECONDARY_BUILDERS =
            "secondaryBuilders";
    String DAQ_STRING_HUB =
            "stringHub";
    String DAQ_REPLAY_HUB =
            "replayHub";
    String DAQ_SIMULATION_HUB =
            "simHub";

    String DAQ_ONLINE_RUNSTART_FLAG =
            "RunStart:";
    String DAQ_ONLINE_RUNSTOP_FLAG =
            "RunStop:";
    String DAQ_ONLINE_SUBRUNSTART_FLAG =
            "SubrunStart:";

    String SOURCE = "source";
    String SINK = "sink";
}
