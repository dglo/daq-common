package icecube.daq.util;

import java.io.File;

/**
 * Utility methods used to locate important pDAQ directories
 */
public final class LocatePDAQ
{
    /** Cached configuration directory */
    private static File CONFIG_DIR;
    /** Cached pDAQ trunk directory */
    private static File META_DIR;

    /** Name of property holding path for pDAQ configuration directory */
    private static final String CONFIG_DIR_PROPERTY =
        "icecube.daq.component.configDir";

    /**
     * Clear all cached paths.
     *
     * NOTE: This is intended for use in unit tests and should probably not
     * be used in normal operation.
     */
    public static final void clearCache()
    {
        CONFIG_DIR = null;
        META_DIR = null;
    }

    /**
     * Find the pDAQ run configuration directory.
     *
     * @return configuration directory
     *
     * @throws IllegalArgumentException if config directory is not found
     */
    public static File findConfigDirectory()
        throws IllegalArgumentException
    {
        if (CONFIG_DIR != null) {
            return CONFIG_DIR;
        }

        boolean done = false;
        for (int i = 0; !done; i++) {
            File dir = null;
            switch (i) {
            case 0:
                // check command-line property
                String cfgProp = System.getProperty(CONFIG_DIR_PROPERTY, null);
                if (cfgProp != null && !cfgProp.equals("")) {
                    dir = new File(cfgProp);
                }
                break;
            case 1:
                // check $PDAQ_CONFIG
                String cfgEnvDir = System.getenv("PDAQ_CONFIG");
                if (cfgEnvDir != null && !cfgEnvDir.equals("")) {
                    dir = new File(cfgEnvDir);
                }
                break;
            case 2:
                // check home directory
                final String homeDir = System.getenv("HOME");
                if (homeDir != null && homeDir.length() > 0) {
                    dir = new File(homeDir, "config");
                }
                break;
            case 3:
                // check user-specified pDAQ distribution directory
                File tmpFile = findTrunk("config");
                if (tmpFile != null) {
                    dir = tmpFile;
                }
                break;
            case 4:
                // give up
                done = true;
                break;
            }

            // quit if we've gone through all the possibilities
            if (done) {
                break;
            }

            // if directory exists...
            if (dir != null && dir.isDirectory() &&
                new File(dir, "trigger").isDirectory() &&
                new File(dir, "domconfigs").isDirectory())
            {
                CONFIG_DIR = dir;
                return dir;
            }
        }

        throw new IllegalArgumentException("Cannot find config directory");
    }

    /**
     * Find the top of the pDAQ directory.
     *
     * @return top-level directory
     */
    public static File findTrunk()
    {
        if (META_DIR != null) {
            return META_DIR;
        }

        File dir = findTrunk(null);

        if (dir != null) {
            META_DIR = dir;
            return META_DIR;
        }

        throw new IllegalArgumentException("Cannot find pDAQ trunk directory");
    }

    /**
     * Find the subdirectory of a pDAQ directory (or just the pDAQ trunk if
     * <tt>subdir</tt> is <tt>null</tt>)
     *
     * @param subdir subdirectory to look for
     *
     * @return pDAQ subdirectory (or <tt>null</tt> if trunk or subdirectory
     *                            is not found)
     */
    public static File findTrunk(String subdir)
    {
        boolean done = false;
        for (int i = 0; !done; i++) {
            File dir = null;
            switch (i) {
            case 0:
                // check $PDAQ_HOME
                String pdaqHome = System.getenv("PDAQ_HOME");
                if (pdaqHome != null && !pdaqHome.equals("")) {
                    dir = new File(pdaqHome);
                }
                break;
            case 1:
                // check "$HOME/pDAQ_current"
                String homeDir = System.getenv("HOME");
                if (homeDir != null && !homeDir.equals("")) {
                    dir = new File(homeDir, "pDAQ_current");
                }
                break;
            case 2:
                // check current directory
                String curDir = System.getProperty("user.dir");
                if (curDir != null && !curDir.equals("")) {
                    dir = new File(curDir);
                }
                break;
            case 3:
                // check parent directory
                String kidDir = System.getProperty("user.dir");
                if (kidDir != null && !kidDir.equals("")) {
                    dir = new File(kidDir).getParentFile();
                }
                break;
            default:
                // give up
                done = true;
                break;
            }

            // quit if we've gone through all the possibilities
            if (done) {
                break;
            }

            // if directory exists...
            if (dir != null && dir.isDirectory()) {
                // ...and all expected directories exist...
                if (new File(dir, "dash").isDirectory() &&
                    new File(dir, "src").isDirectory() &&
                    (new File(dir, "target").isDirectory() ||
                     new File(dir, "StringHub").isDirectory()))
                {
                    return dir;
                }
            }
        }

        return null;
    }

    /**
     * Set the configuration directory location.
     *
     * @param dir path to configuration directory
     */
    public static void setConfigDirectory(String path)
    {
        if (path == null || path.equals("")) {
            throw new IllegalArgumentException("Path argument is not set");
        }

        File dir = new File(path);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Path \"" + dir +
                                               "\" is not a directory");
        }

        CONFIG_DIR = dir;
    }
}
