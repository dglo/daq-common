package icecube.daq.common;

public class ANSIEscapeCode
{
    public static final String OFF = escapeString(0);

    private static final int BLACK = 0;
    private static final int RED = 1;
    private static final int GREEN = 2;
    private static final int YELLOW = 3;
    private static final int BLUE = 4;
    private static final int MAGENTA = 5;
    private static final int CYAN = 6;
    private static final int WHITE = 7;
    private static final int DEFAULT = 9;

    public static final String BOLD_ON = escapeString(1);
    public static final String ITALIC_ON = escapeString(3);
    public static final String UNDERLINE_ON = escapeString(4);
    public static final String INVERTED_ON = escapeString(7);
    public static final String BOLD_OFF = escapeString(21);
    public static final String BOLD_FAINT_OFF = escapeString(22);
    public static final String ITALIC_OFF = escapeString(23);
    public static final String UNDERLINE_OFF = escapeString(24);
    public static final String INVERTED_OFF = escapeString(27);

    public static final String FG_BLACK = foregroundColor(BLACK);
    public static final String FG_RED = foregroundColor(RED);
    public static final String FG_GREEN = foregroundColor(GREEN);
    public static final String FG_YELLOW = foregroundColor(YELLOW);
    public static final String FG_BLUE = foregroundColor(BLUE);
    public static final String FG_MAGENTA = foregroundColor(MAGENTA);
    public static final String FG_CYAN = foregroundColor(CYAN);
    public static final String FG_WHITE = foregroundColor(WHITE);
    public static final String FG_DEFAULT = foregroundColor(DEFAULT);

    public static final String BG_BLACK = backgroundColor(BLACK);
    public static final String BG_RED = backgroundColor(RED);
    public static final String BG_GREEN = backgroundColor(GREEN);
    public static final String BG_YELLOW = backgroundColor(YELLOW);
    public static final String BG_BLUE = backgroundColor(BLUE);
    public static final String BG_MAGENTA = backgroundColor(MAGENTA);
    public static final String BG_CYAN = backgroundColor(CYAN);
    public static final String BG_WHITE = backgroundColor(WHITE);
    public static final String BG_DEFAULT = backgroundColor(DEFAULT);

    public static final String backgroundColor(int color)
    {
        if (color < 0 || color > 9) {
            throw new IllegalArgumentException("Color must be between" +
                                               " 0 and 9, not " + color);
        }

        return escapeString(color + 40);
    }

    public static final String foregroundColor(int color)
    {
        if (color < 0 || color > 9) {
            throw new IllegalArgumentException("Color must be between" +
                                               " 0 and 9, not " + color);
        }

        return escapeString(color + 30);
    }

    private static String escapeString(int code)
    {
        String substr;
        if (code <= 0) {
            substr = "";
        } else {
            //substr = "0;" + code;
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
