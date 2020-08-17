package icecube.daq.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;

import org.apache.log4j.Category;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Mock log4j appender.
 */
public class MockAppender
    implements IDAQAppender
{
    private static final Logger LOG = Logger.getLogger(MockAppender.class);

    /** minimum level of log messages which will be print. */
    private Level minLevel;
    /** <tt>true</tt> if messages should be printed as well as cached. */
    private boolean verbose;
    /** <tt>true</tt> if messages are not kept. */
    private boolean flushMsgs;

    private ArrayList<LoggingEvent> eventList;

    /**
     * Create a MockAppender which ignores everything below the WARN level.
     */
    public MockAppender()
    {
        this(Level.WARN);
    }

    /**
     * Create a MockAppender which ignores everything
     * below the specified level.
     *
     * @param minLevel minimum level
     */
    public MockAppender(Level minLevel)
    {
        this.minLevel = minLevel;
        eventList = new ArrayList<LoggingEvent>();
    }

    /**
     * Unimplemented.
     *
     * @param x0 ???
     */
    @Override
    public void addFilter(Filter x0)
    {
        throw new Error("Unimplemented");
    }

    public void assertLogMessage(String message)
    {
        if (getNumberOfMessages() < 1) {
            throw new AssertionError("No log messages found, expected " +
                                     message);
        } else {
            final String logMsg = (String) removeEvent(0).getMessage();
            if (!logMsg.startsWith(message)) {
                throw new AssertionError("Expected log message \"" + message +
                                         "\" not \"" + logMsg + "\"");
            }
        }
    }

    public void assertNoLogMessages()
    {
        assertNoLogMessages(null);
    }

    public void assertNoLogMessages(String description)
    {
        if (getNumberOfMessages() != 0) {
            String foundStr;
            if (description == null) {
                foundStr = "Found ";
            } else {
                foundStr = "For [" + description + "], found ";
            }

            try {
                if (getNumberOfMessages() == 1) {
                    throw new AssertionError(foundStr +
                                             "unexpected log message: " +
                                             getMessage(0));
                } else {
                    throw new AssertionError(foundStr + getNumberOfMessages() +
                                    " unexpected log messages, first" +
                                    " message: " + getMessage(0));
                }
            } finally {
                clear();
            }
        }
    }

    /**
     * Clear the cached logging events.
     */
    public void clear()
    {
        eventList.clear();
    }

    /**
     * Unimplemented.
     */
    @Override
    public void clearFilters()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Nothing needs to be done here.
     */
    @Override
    public void close()
    {
        // don't need to do anything
    }

    /**
     * Handle a logging event.
     *
     * @param evt logging event
     */
    @Override
    public void doAppend(LoggingEvent evt)
    {
        if (evt.getLevel().toInt() >= minLevel.toInt()) {
            if (!flushMsgs) {
                eventList.add(evt);
            }

            if (verbose) {
                dumpEvent(System.err, evt);
            }
        }
    }

    /**
     * Dump a logging event to System.out
     *
     * @param evt logging event
     */
    public void dumpEvent(int i)
    {
        dumpEvent(System.err, getEvent(i));
    }

    /**
     * Dump a logging event to the specified output destination
     *
     * @param out output destination
     * @param evt logging event
     */
    private void dumpEvent(PrintStream out, LoggingEvent evt)
    {
        LocationInfo loc = evt.getLocationInformation();

        out.println(evt.getLoggerName() + " " + evt.getLevel() + " [" +
                    loc.fullInfo + "] " + evt.getMessage());

        String[] stack = evt.getThrowableStrRep();
        for (int i = 0; stack != null && i < stack.length; i++) {
            out.println("> " + stack[i]);
        }
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public ErrorHandler getErrorHandler()
    {
        throw new Error("Unimplemented");
    }

    private LoggingEvent getEvent(int idx)
    {
        if (idx < 0 || idx > eventList.size()) {
            throw new IllegalArgumentException("Bad index " + idx);
        }

        return eventList.get(idx);
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public Filter getFilter()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public Layout getLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get logging level.
     *
     * @return logging level
     */
    @Override
    public Level getLevel()
    {
        return minLevel;
    }

    /**
     * Get error message from the specified logging event
     *
     * @return error message
     */
    public Object getMessage(int idx)
    {
        return getEvent(idx).getMessage();
    }

    /**
     * Get log level from the specified logging event
     *
     * @return error message
     */
    public Level getMessageLevel(int idx)
    {
        return getEvent(idx).getLevel();
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public String getName()
    {
        throw new Error("Unimplemented");
    }

    public int getNumberOfMessages()
    {
        return eventList.size();
    }

    /**
     * Is this appender sending log messages?
     *
     * @return <tt>true</tt> if this appender is connected
     */
    @Override
    public boolean isConnected()
    {
        return true;
    }

    /**
     * Is this appender sending log messages to the specified host and port.
     *
     * @param logHost DAQ host name/IP address
     * @param logPort DAQ port number
     * @param liveHost I3Live host name/IP address
     * @param livePort I3Live port number
     *
     * @return <tt>true</tt> if this appender uses the host:port
     */
    @Override
    public boolean isConnected(String logHost, int logPort, String liveHost,
                               int livePort)
    {
        return true;
    }

    /**
     * Reconnect to the remote socket.
     */
    @Override
    public void reconnect()
    {
        // do nothing
    }

    private LoggingEvent removeEvent(int idx)
    {
        if (idx < 0 || idx > eventList.size()) {
            throw new IllegalArgumentException("Bad index " + idx);
        }

        return eventList.remove(idx);
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    @Override
    public boolean requiresLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @param x0 ???
     */
    @Override
    public void setErrorHandler(ErrorHandler x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Should log messages be flushed?
     *
     * @param val <tt>false</tt> if log messages should be saved
     */
    public void setFlushMessages(boolean val)
    {
        flushMsgs = val;
    }

    /**
     * Unimplemented.
     *
     * @param x0 ???
     */
    @Override
    public void setLayout(Layout x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set logging level.
     *
     * @param lvl logging level
     */
    public MockAppender setLevel(Level lvl)
    {
        minLevel = lvl;

        // set log level in all active loggers
        Enumeration current = LOG.getLoggerRepository().getCurrentLoggers();
        while (current.hasMoreElements()) {
            Category logger = (Category) current.nextElement();
            logger.setLevel(lvl);
        }

        return this;
    }

    /**
     * Unimplemented.
     *
     * @param s0 ???
     */
    @Override
    public void setName(String s0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set verbosity.
     *
     * @param val <tt>true</tt> if log messages should be printed
     */
    public MockAppender setVerbose(boolean val)
    {
        verbose = val;

        return this;
    }
}
