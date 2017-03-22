package icecube.daq.common;

/**
 * Enumeration of all ANSI color codes
 */
public enum ANSIColor
{
    BLACK(0), RED(1), GREEN(2), YELLOW(3),
    BLUE(4), MAGENTA(5), CYAN(6), WHITE(7),
    DEFAULT(9);

    private final int code;

    /**
     * Create an ANSI color code
     * @param code color code (a value between 0 and 9)
     */
    ANSIColor(final int code)
    {
        this.code = code;
    }

    /**
     * Return the ANSI color code
     * @return color code (a value between 0 and 9)
     */
    public int getCode()
    {
        return code;
    }
}
