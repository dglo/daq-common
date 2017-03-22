package icecube.daq.common;

import java.util.HashMap;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;

/**
 * log4j appender which writes color-coded log messages to STDOUT.
 *
 * To use, make these the first lines in your `main()` method:
 * <code>
 *  BasicConfigurator.resetConfiguration();
 *  BasicConfigurator.configure(new ColoredAppender());
 * </code>
 */
public class ColoredAppender
    implements Appender
{
    /**
     * Utility class for tying together background and foreground colors
     */
    class Colors
    {
        private ANSIColor background;
        private ANSIColor foreground;

        Colors(ANSIColor background, ANSIColor foreground)
        {
            this.background = background;
            this.foreground = foreground;
        }

        /**
         * Get the background color
         * @return background color
         */
        ANSIColor getBackground()
        {
            return background;
        }

        /**
         * Get the ANSI string representing the background color
         * @return background color as a piece of an ANSI string
         */
        String getBackgroundColor()
        {
            switch (background) {
            case BLACK:
                return ANSIEscapeCode.BG_BLACK;
            case RED:
                return ANSIEscapeCode.BG_RED;
            case GREEN:
                return ANSIEscapeCode.BG_GREEN;
            case YELLOW:
                return ANSIEscapeCode.BG_YELLOW;
            case BLUE:
                return ANSIEscapeCode.BG_BLUE;
            case MAGENTA:
                return ANSIEscapeCode.BG_MAGENTA;
            case CYAN:
                return ANSIEscapeCode.BG_CYAN;
            case WHITE:
                return ANSIEscapeCode.BG_WHITE;
            case DEFAULT:
                break;
            }

            return ANSIEscapeCode.BG_DEFAULT;
        }

        /**
         * Get the foreground color
         * @return foreground color
         */
        ANSIColor getForeground()
        {
            return foreground;
        }

        /**
         * Get the ANSI string representing the foreground color
         * @return foreground color as a piece of an ANSI string
         */
        String getForegroundColor()
        {
            switch (foreground) {
            case BLACK:
                return ANSIEscapeCode.FG_BLACK;
            case RED:
                return ANSIEscapeCode.FG_RED;
            case GREEN:
                return ANSIEscapeCode.FG_GREEN;
            case YELLOW:
                return ANSIEscapeCode.FG_YELLOW;
            case BLUE:
                return ANSIEscapeCode.FG_BLUE;
            case MAGENTA:
                return ANSIEscapeCode.FG_MAGENTA;
            case CYAN:
                return ANSIEscapeCode.FG_CYAN;
            case WHITE:
                return ANSIEscapeCode.FG_WHITE;
            case DEFAULT:
                break;
            }

            return ANSIEscapeCode.FG_DEFAULT;
        }

        /**
         * Set the background color
         * @param color background color
         */
        void setBackground(ANSIColor color)
        {
            background = color;
        }

        /**
         * Set the foreground color
         * @param color foreground color
         */
        void setForeground(ANSIColor color)
        {
            foreground = color;
        }
    }

    /** minimum level of log messages which will be print. */
    private Level minLevel;

    /** default foreground/background colors for each log level */
    private HashMap<Level, Colors> colorMap = new HashMap<Level, Colors>() {
            {
                put(Level.ALL,   new Colors(ANSIColor.BLUE, ANSIColor.WHITE));
                put(Level.DEBUG, new Colors(ANSIColor.WHITE, ANSIColor.BLACK));
                put(Level.INFO,  new Colors(ANSIColor.GREEN, ANSIColor.WHITE));
                put(Level.WARN,  new Colors(ANSIColor.YELLOW, ANSIColor.RED));
                put(Level.ERROR, new Colors(ANSIColor.RED, ANSIColor.YELLOW));
                put(Level.FATAL, new Colors(ANSIColor.BLACK, ANSIColor.RED));
            }
        };

    /**
     * Create a ColoredAppender which ignores everything below the WARN level.
     */
    public ColoredAppender()
    {
        this(Logger.getRootLogger().getLevel());
    }

    /**
     * Create a ColoredAppender which ignores everything
     * below the specified level.
     *
     * @param minLevel minimum level
     */
    public ColoredAppender(Level minLevel)
    {
        this.minLevel = minLevel;
    }

    /**
     * Unimplemented.
     *
     * @param x0 ???
     */
    public void addFilter(Filter x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     */
    public void clearFilters()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Nothing needs to be done here.
     */
    public void close()
    {
        // don't need to do anything
    }

    /**
     * Handle a logging event.
     *
     * @param evt logging event
     */
    public void doAppend(LoggingEvent evt)
    {
        if (evt.getLevel().toInt() >= minLevel.toInt()) {
            dumpEvent(evt);
        }
    }

    /**
     * Dump a logging event to System.out
     *
     * @param evt logging event
     */
    private void dumpEvent(LoggingEvent evt)
    {
        LocationInfo loc = evt.getLocationInformation();

        Colors colors = colorMap.get(evt.getLevel());
        if (colors == null) {
            colors = colorMap.get(Level.ALL);
        }

        System.out.println(colors.getBackgroundColor() +
                           colors.getForegroundColor() +
                           ANSIEscapeCode.ITALIC_ON + evt.getLoggerName() +
                           " " + evt.getLevel() + ANSIEscapeCode.ITALIC_OFF +
                           " [" + loc.fullInfo + "] " + evt.getMessage() +
                           ANSIEscapeCode.OFF);

        String[] stack = evt.getThrowableStrRep();
        for (int i = 0; stack != null && i < stack.length; i++) {
            System.out.println(colors.getBackgroundColor() +
                               colors.getForegroundColor() + "> " +
                               stack[i] + ANSIEscapeCode.OFF);
        }
    }

    /**
     * Get the background color for the specified log level
     * @param level log level
     * @return background color
     */
    public ANSIColor getBackground(Level level)
    {
        return colorMap.get(level).getBackground();
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public ErrorHandler getErrorHandler()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public Filter getFilter()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the foreground color for the specified log level
     * @param level log level
     * @return foreground color
     */
    public ANSIColor getForeground(Level level)
    {
        return colorMap.get(level).getForeground();
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public Layout getLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get logging level.
     *
     * @return logging level
     */
    public Level getLevel()
    {
        return minLevel;
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public String getName()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Is this appender sending log messages?
     *
     * @return <tt>true</tt> if this appender is connected
     */
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
    public boolean isConnected(String logHost, int logPort, String liveHost,
                               int livePort)
    {
        return true;
    }

    /**
     * Reconnect to the remote socket.
     */
    public void reconnect()
    {
        // do nothing
    }

    /**
     * Unimplemented.
     *
     * @return ???
     */
    public boolean requiresLayout()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set the background color for the specified log level
     * @param level log level
     * @param color background color
     */
    public void setBackground(Level level, ANSIColor color)
    {
        colorMap.get(level).setBackground(color);
    }

    /**
     * Unimplemented.
     *
     * @param x0 error handler
     */
    public void setErrorHandler(ErrorHandler x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set the foreground color for the specified log level
     * @param level log level
     * @param color foreground color
     */
    public void setForeground(Level level, ANSIColor color)
    {
        colorMap.get(level).setForeground(color);
    }

    /**
     * Unimplemented.
     *
     * @param x0 ???
     */
    public void setLayout(Layout x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set logging level.
     *
     * @param lvl logging level
     *
     * @return this object (so commands can be chained)
     */
    public ColoredAppender setLevel(Level lvl)
    {
        minLevel = lvl;

        return this;
    }

    /**
     * Unimplemented.
     *
     * @param s0 ???
     */
    public void setName(String s0)
    {
        throw new Error("Unimplemented");
    }
}
