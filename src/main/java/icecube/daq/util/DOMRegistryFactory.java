package icecube.daq.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.SAXException;

/**
 * The DOM registry is a utility class for looking up DOM information.
 * @author krokodil
 */
public final class DOMRegistryFactory
{
    /** Maximum channel ID */
    private static final int MAX_CHANNEL_ID = 6171;

    private static File cachedPath;
    private static DOMRegistry cachedRegistry;

    public static IDOMRegistry load()
        throws DOMRegistryException
        //throws ParserConfigurationException, SAXException, IOException
    {
        return load(LocatePDAQ.findConfigDirectory());
    }

    public static IDOMRegistry load(String path)
        throws DOMRegistryException
        //throws ParserConfigurationException, SAXException, IOException
    {
        return load(new File(path));
    }

    public static synchronized IDOMRegistry load(File path)
        throws DOMRegistryException
        //throws ParserConfigurationException, SAXException, IOException
    {
        if (cachedRegistry == null || cachedPath == null ||
            !path.equals(cachedPath))
        {
            DOMRegistryParser parser;
            try {
                parser = new DOMRegistryParser(path, MAX_CHANNEL_ID + 1);
            } catch (ParserConfigurationException pce) {
                throw new DOMRegistryException("Cannot parse " + path, pce);
            } catch (SAXException se) {
                throw new DOMRegistryException("Cannot parse " + path, se);
            } catch (IOException ioe) {
                throw new DOMRegistryException("Cannot parse " + path, ioe);
            }

            DOMRegistry reg = parser.getRegistry();
            reg.tabulateDistances();

            cachedPath = path;
            cachedRegistry = reg;
        }

        return cachedRegistry;
    }
}
