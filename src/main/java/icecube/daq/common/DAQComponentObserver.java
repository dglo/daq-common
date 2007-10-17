/**
 * DAQComponentObserver
 * Date: Nov 13, 2006 3:32:14 PM
 * 
 * (c) 2005 IceCube Collaboration
 */
package icecube.daq.common;

/**
 * This interface represent the interaction between the input/output engines and the high level
 * component manager
 * @author artur
 * @version $Id: DAQComponentObserver.java 2125 2007-10-12 18:27:05Z ksb $
 */
public interface DAQComponentObserver {

    /**
     * The observable object notifies this class that its state type has changed. The class that
     * implements this interface should synchronize this method if more than one objects are
     * using it.
     * @param object
     */
    public void update(Object object, String notificationID);
}
