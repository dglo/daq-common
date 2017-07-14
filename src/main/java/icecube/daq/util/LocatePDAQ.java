package icecube.daq.util;

import java.io.File;

/**
 * Utility methods used to locate important pDAQ directories
 */
public final class LocatePDAQ
{
    /** Name of property holding path for pDAQ configuration directory */
    public static final String CONFIG_DIR_PROPERTY =
        "icecube.daq.component.configDir";

    /** Cached configuration directory */
    private static File CONFIG_DIR;
    /** Cached pDAQ trunk directory */
    private static File META_DIR;

    /**
     * Clear all cached paths.
     *
     * NOTE: This is intended for use in unit tests and should probably not
     * be used in normal operation.
     */
    public static void clearCache()
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
    public static synchronized File findConfigDirectory()
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
                // check inside pDAQ build directory
                //  (should only be used for Jenkins builds)
                final File trunkDir = findTrunk();
                if (trunkDir != null && trunkDir.length() > 0) {
                    dir = new File(trunkDir, "config");
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
    public static synchronized File findTrunk()
    {
        if (META_DIR != null) {
            return META_DIR;
        }

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
                    META_DIR = dir;
                    return META_DIR;
                }
            }
        }

        throw new IllegalArgumentException("Cannot find pDAQ trunk directory");
    }

    /**
     * Set the configuration directory location.
     *
     * @param path path to configuration directory
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
