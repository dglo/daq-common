/*
 * class: CmdInterface
 *
 * Version $Id: DAQCmdInterface.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: March 8 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.common;

/**
 * This class defines constants that are used through the DAQ software.
 *
 * @version $Id: DAQCmdInterface.java 2125 2007-10-12 18:27:05Z ksb $
 * @author mcp
 */
public interface DAQCmdInterface
{

    // public static final member data

    // protected static final member data

    // static final member data
    // names for all component types
    public static final int DAQ_MAX_NUM_STRINGS = 80;
    public static final int DAQ_MAX_NUM_IDH = 16;
    public static final String DAQ_PAYLOAD_INVALID_SOURCE_ID =
            "payloadInvalidSourceId";
    public static final String DAQ_DOMSET =
            "domSet";
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
    public static final String DAQ_DATACACHE_MANAGER =
            "dataCacheManager";
    public static final String DAQ_CONTROL =
            "daqControl";
    public static final String DAQ_SYSTEM =
            "daqSystem";
    // names for mbean property categories
    public static final String DAQ_MBEAN_TYPE =
            "type";
    public static final String DAQ_MBEAN_ACME_ASPECT =
            "acme-aspect";
    public static final String DAQ_MBEAN_ASPECT =
            "aspect";
    public static final String DAQ_MBEAN_ID =
            "id";
    public static final String DAQ_MBEAN_FCN =
            "fcn";
    public static final String DAQ_MBEAN_NAME =
            "name";
    public static final String DAQ_MBEAN_CLASS_NAME = 
            "class";
    // names for mbean property values
    public static final String DAQ_MBEAN_ASPECT_CONTROL =
            "control";
    public static final String DAQ_MBEAN_ASPECT_SPLICER =
            "splicer";
    public static final String DAQ_MBEAN_ASPECT_MONITOR =
            "monitor";
    public static final String DAQ_MBEAN_FCN_CMD =
            "cmd";
    public static final String DAQ_MBEAN_FCN_CONFIG =
            "config";
    public static final String DAQ_MBEAN_FCN_STATUS =
            "status";
    public static final String DAQ_MBEAN_ACME_ASPECT_CONTROL =
            "control";
    public static final String DAQ_MBEAN_ACME_ASPECT_MONITOR =
            "monitor";
    public static final String DAQ_MBEAN_ACME_ASPECT_CONFIGURE =
            "configure";
    public static final String DAQ_MBEAN_ACME_ASPECT_SERVICE =
            "service";
    public static final String DAQ_MBEAN_ACME_ASPECT_NONE =
            "none";
    // names for channel contents
    public static final String DAQ_PHYSICS_CONTENT =
            "physics";
    public static final String DAQ_MONITOR_CONTENT =
            "monitor";
    public static final String DAQ_SN_CONTENT =
            "supernova";
    public static final String DAQ_TIMECAL_CONTENT =
            "timeCal";
    public static final String DAQ_TRIGGER_CONTENT =
            "trigger";
    public static final String DAQ_EVENT_SPEC_CONTENT =
            "eventSpec";
    public static final String DAQ_REQUEST_PHYSICS_CONTENT =
            "requestPhysics";
    public static final String DAQ_CLEAR_PHYSICS_CONTENT =
            "clearPhysics";
    // names for DAQ system partitions
    public static final String DAQ_NETWORK_TEST_PARTITIION =
            "network_test";
    public static final String DAQ_MAIN_DATA_COLLECTION_PARTITION =
            "main";
    public static final String DAQ_PAYLOAD_SYSTEM_TEST_PARTITION =
            "payload_system_test";
    public static final String DAQ_SIMULATION_PARTITION =
            "simulation";
    
    public static final String DAQ_ONLINE_RUNSTART_FLAG =
            "RunStart:";
    public static final String DAQ_ONLINE_RUNSTOP_FLAG =
            "RunStop:";
    public static final String DAQ_TEST_FRAME_PARTITION =
            "testFrame";

    public static final String SOURCE = "source";

    public static final String SINK = "sink";
    
    // public methods
    boolean processCmd(String command);

    String getLastCmdStatus();

    LocalStateInfoData getStateInfo();


}
