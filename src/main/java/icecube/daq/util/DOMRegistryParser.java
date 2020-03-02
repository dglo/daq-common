package icecube.daq.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class DOMRegistryParser
    extends DefaultHandler
{
    private static final Log LOG = LogFactory.getLog(DOMRegistryParser.class);

    private StringBuilder xmlChars = new StringBuilder();
    private DOMInfo currentDOM = new DOMInfo();
    private int currentHubId = DOMInfo.NO_VALUE;
    private int originalString = DOMInfo.NO_VALUE;

    private HashMap<Long, DOMInfo> doms = new HashMap<Long, DOMInfo>();
    private DOMInfo[] domsByChannelId;

    private DOMRegistry reg;

    DOMRegistryParser(File configDir, int maxChannelIDs)
        throws ParserConfigurationException, SAXException, IOException
    {
        domsByChannelId = new DOMInfo[maxChannelIDs];
        reg = new DOMRegistry(doms, domsByChannelId);

        if (configDir == null || !configDir.exists()) {
            throw new FileNotFoundException("Configuration directory \"" +
                                            configDir + "\" does not exist");
        }

        File file = new File(configDir, IDOMRegistry.DEFAULT_DOM_GEOMETRY);
        if (!file.exists()) {
            throw new FileNotFoundException("Registry does not exist in \"" +
                                            file + "\"");
        }

        FileInputStream is = new FileInputStream(file);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            parser.parse(is, this);
            reg.tabulateDistances();
        } finally {
            is.close();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
        throws SAXException
    {
        super.characters(ch, start, length);
        xmlChars.append(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
        throws SAXException
    {
        super.endElement(uri, localName, qName);
        String txt = xmlChars.toString().trim();
        if (localName.equalsIgnoreCase("dom")) {
            if (currentHubId < 0) {
                throw new Error("Unknown hub ID for " + currentDOM);
            }

            currentDOM.hubId = currentHubId;

            if (originalString >= 0) {
                currentDOM.string = originalString;
            } else {
                currentDOM.string = currentHubId;
            }

            if (currentDOM.location < 0) {
                throw new Error("Unknown location for " + currentDOM);
            }

            if (currentDOM.isRealDOM()) {
                short xchan;
                try {
                    xchan = DOMInfo.computeChannelId(currentDOM.string,
                                                         currentDOM.location);
                } catch (Error err) {
                    LOG.error("Cannot compute channel ID for " +
                              currentDOM, err);
                    xchan = currentDOM.channelId;
                }

                if (xchan != currentDOM.channelId) {
                    LOG.error("DOM " + currentDOM +
                              " channel ID updated to " + xchan);
                    currentDOM.channelId = xchan;
                }
            }

            if (doms.containsKey(currentDOM.numericMainboardId)) {
                DOMInfo oldDOM = doms.get(currentDOM.numericMainboardId);

                LOG.error(String.format("Found multiple entries for %012x:" +
                                        " %s and %s",
                                        currentDOM.numericMainboardId,
                                        oldDOM.getDeploymentLocation(),
                                        currentDOM.getDeploymentLocation()));
            }

            doms.put(currentDOM.numericMainboardId, currentDOM);
            if (currentDOM.isRealDOM() || currentDOM.isScintillator() ||
                currentDOM.isIceACT())
            {
                if (currentDOM.channelId < 0 ||
                    currentDOM.channelId >= domsByChannelId.length)
                {
                    LOG.error("Not adding " + currentDOM +
                              " to doms->channel lookup table");
                } else if (domsByChannelId[currentDOM.channelId] != null) {
                    DOMInfo oldDOM = domsByChannelId[currentDOM.channelId];

                    LOG.error("DOMsByChannelId collision between " +
                              oldDOM + " and " + currentDOM);
                } else {
                    domsByChannelId[currentDOM.channelId] = currentDOM;
                }
            }

            currentDOM = new DOMInfo();
            originalString = DOMInfo.NO_VALUE;
        } else if (localName.equalsIgnoreCase("position")) {
            currentDOM.location   = Integer.parseInt(txt);
        } else if (localName.equalsIgnoreCase("channelId")) {
            currentDOM.channelId = Short.parseShort(txt);
        } else if (localName.equalsIgnoreCase("mainBoardId")) {
            currentDOM.mainboardId = txt;
            currentDOM.numericMainboardId =
                Long.parseLong(currentDOM.mainboardId, 16);
        } else if (localName.equalsIgnoreCase("name")) {
            currentDOM.name = txt;
        } else if (localName.equalsIgnoreCase("productionId")) {
            currentDOM.prodId = txt;
        } else if (localName.equalsIgnoreCase("xCoordinate")) {
            currentDOM.x = Double.parseDouble(txt);
        } else if (localName.equalsIgnoreCase("yCoordinate")) {
            currentDOM.y = Double.parseDouble(txt);
        } else if (localName.equalsIgnoreCase("zCoordinate")) {
            currentDOM.z = Double.parseDouble(txt);
        } else if (localName.equalsIgnoreCase("number")) {
            currentHubId = Integer.parseInt(txt);
        } else if (localName.equals("originalString")) {
            originalString = Integer.parseInt(txt);
        } else if (localName.equals("string")) {
            currentHubId = DOMInfo.NO_VALUE;
        }
    }

    public DOMRegistry getRegistry()
    {
        return reg;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes)
        throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);
        xmlChars.setLength(0);
    }
}
