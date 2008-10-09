package icecube.daq.common;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;

public interface IDAQAppender
    extends Appender
{
    Level getLevel();
    boolean isConnected(String host, int port);
}
