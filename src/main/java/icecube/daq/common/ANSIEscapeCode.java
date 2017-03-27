package icecube.daq.common;

/**
 * Utilities to manage ANSI escape codes
 */
public class ANSIEscapeCode
{
    public static final String OFF = escapeString(0);

    public static final String BOLD_ON = escapeString(1);
    public static final String ITALIC_ON = escapeString(3);
    public static final String UNDERLINE_ON = escapeString(4);
    public static final String INVERTED_ON = escapeString(7);
    public static final String BOLD_OFF = escapeString(21);
    public static final String BOLD_FAINT_OFF = escapeString(22);
    public static final String ITALIC_OFF = escapeString(23);
    public static final String UNDERLINE_OFF = escapeString(24);
    public static final String INVERTED_OFF = escapeString(27);

    public static final String FG_BLACK = foregroundColor(ANSIColor.BLACK);
    public static final String FG_RED = foregroundColor(ANSIColor.RED);
    public static final String FG_GREEN = foregroundColor(ANSIColor.GREEN);
    public static final String FG_YELLOW = foregroundColor(ANSIColor.YELLOW);
    public static final String FG_BLUE = foregroundColor(ANSIColor.BLUE);
    public static final String FG_MAGENTA = foregroundColor(ANSIColor.MAGENTA);
    public static final String FG_CYAN = foregroundColor(ANSIColor.CYAN);
    public static final String FG_WHITE = foregroundColor(ANSIColor.WHITE);
    public static final String FG_DEFAULT = foregroundColor(ANSIColor.DEFAULT);

    public static final String BG_BLACK = backgroundColor(ANSIColor.BLACK);
    public static final String BG_RED = backgroundColor(ANSIColor.RED);
    public static final String BG_GREEN = backgroundColor(ANSIColor.GREEN);
    public static final String BG_YELLOW = backgroundColor(ANSIColor.YELLOW);
    public static final String BG_BLUE = backgroundColor(ANSIColor.BLUE);
    public static final String BG_MAGENTA = backgroundColor(ANSIColor.MAGENTA);
    public static final String BG_CYAN = backgroundColor(ANSIColor.CYAN);
    public static final String BG_WHITE = backgroundColor(ANSIColor.WHITE);
    public static final String BG_DEFAULT = backgroundColor(ANSIColor.DEFAULT);

    /**
     * Return the ANSI escape string to set the background color
     *
     * @param color ANSI color
     *
     * @return escape code string which will set the background color
     */
    public static final String backgroundColor(ANSIColor color)
    {
        if (color == null) {
            throw new NullPointerException("Color cannot be null");
        }

        return escapeString(color.getCode() + 40);
    }

    /**
     * Return the ANSI escape string to set the foreground color
     *
     * @param color ANSI color
     *
     * @return escape code string which will set the foreground color
     */
    public static final String foregroundColor(ANSIColor color)
    {
        if (color == null) {
            throw new NullPointerException("Color cannot be null");
        }

        return escapeString(color.getCode() + 30);
    }

    /**
     * Build an ANSI escape code string
     *
     * @param code value to encode
     *
     * @return escape code string
     */
    private static String escapeString(int code)
    {
        String substr;
        if (code <= 0) {
            substr = "";
        } else {
            substr = Integer.toString(code);
        }

        return "\033[" + substr + "m";
    }

    public static final void main(String[] args)
    {
        int color = 0;

        String space = "";
        for (int i = 0; i < args.length; i++) {
            String style, fgColor, bgColor;
            switch (color) {
            case 0:
                style = BOLD_ON;
                fgColor = FG_BLACK;
                bgColor = BG_BLUE;
                break;
            case 1:
                style = ITALIC_ON;
                fgColor = FG_RED;
                bgColor = BG_MAGENTA;
                break;
            case 2:
                style = UNDERLINE_ON;
                fgColor = FG_GREEN;
                bgColor = BG_CYAN;
                break;
            case 3:
                style = INVERTED_ON;
                fgColor = FG_YELLOW;
                bgColor = BG_GREEN;
                break;
            case 4:
                style = BOLD_OFF;
                fgColor = FG_BLUE;
                bgColor = BG_BLACK;
                break;
            case 5:
                style = ITALIC_OFF;
                fgColor = FG_MAGENTA;
                bgColor = BG_RED;
                break;
            case 6:
                style = UNDERLINE_OFF;
                fgColor = FG_CYAN;
                bgColor = BG_GREEN;
                break;
            default:
                style = INVERTED_OFF;
                fgColor = FG_GREEN;
                bgColor = BG_YELLOW;
                break;
            }

            System.out.print(BG_WHITE + space + style + fgColor + bgColor +
                             args[i]);
            color = (color + 1) % 8;
            space = " ";
        }

        System.out.println(OFF);
    }
}
