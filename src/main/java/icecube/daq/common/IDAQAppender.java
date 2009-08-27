package icecube.daq.common;

import java.net.SocketException;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;

public interface IDAQAppender
    extends Appender
{
    /**
     * Get the logging level.
     *
     * @return lowest leel of messages which will be logged
     */
    Level getLevel();

    /**
     * Is this appender connected to a remote socket?
     *
     * @return <tt>true</tt> if this appender is connected to a remote socket
     */
    boolean isConnected();

    /**
     * Is this appender connected to the specified socket?
     *
     * @return <tt>true</tt> if this appender is connected to
     *                       the specified socket
     */
    boolean isConnected(String logHost, int logPort, String liveHost,
                        int livePort);

    /**
     * Reconnect to logging socket.
     *
     * @throws SocketException if the connection could not be made
     */
    void reconnect()
        throws SocketException;
}
