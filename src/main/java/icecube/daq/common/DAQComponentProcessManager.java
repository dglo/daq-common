/*
 * class: DAQComponentProcessManager
 *
 * Version $Id: DAQComponentProcessManager.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: March 24 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.common;

/**
 * This class ...does what?
 *
 * @version $Id: DAQComponentProcessManager.java 2125 2007-10-12 18:27:05Z ksb $
 * @author mcp
 */
public interface DAQComponentProcessManager
{

    // public static final member data

    // protected static final member data

    // static final member data

    // private static final member data

    // private static member data

    // private instance member data

    // constructors


    // instance member method (alphabetic)
    public void DAQComponentProcessStopNotification(String notificationTag,
                                                    boolean status);

    public void DAQComponentProcessErrorNotification(String notificationTag,
                                                     String errorInfo,
                                                     Exception e);
    public void DAQConfigurationNotification(String completionInfo,
                                             boolean status);
    // static member methods (alphabetic)

    // Description of this object.
    // public String toString() {}

    // public static void main(String args[]) {}
}