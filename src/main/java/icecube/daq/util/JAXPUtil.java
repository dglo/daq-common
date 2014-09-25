package icecube.daq.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

public abstract class JAXPUtil
{
    public static final void dumpDocument(PrintStream out, Document doc)
    {
        dumpNode(out, doc.getDocumentElement(), "");
    }

    private static void dumpNode(PrintStream out, Node node, String indent)
    {
        if (node == null) {
            return;
        }

        switch (node.getNodeType()) {
        case Node.ATTRIBUTE_NODE:
            out.printf("%s<!-- Ignoring ATTRIBUTE -->\n", indent);
            break;
        case Node.CDATA_SECTION_NODE:
            out.printf("%s<!-- Ignoring CDATA_SECTION -->\n", indent);
            break;
        case Node.COMMENT_NODE:
            out.printf("%s<!-- Ignoring COMMENT -->\n", indent);
            break;
        case Node.DOCUMENT_FRAGMENT_NODE:
            out.printf("%s<!-- Ignoring DOCUMENT_FRAGMENT -->\n", indent);
            break;
        case Node.DOCUMENT_NODE:
            out.printf("%s<!-- Ignoring DOCUMENT -->\n", indent);
            break;
        case Node.ELEMENT_NODE:
            dumpElement(out, (Element) node, indent);
            break;
        case Node.ENTITY_NODE:
            out.printf("%s<!-- Ignoring ENTITY -->\n", indent);
            break;
        case Node.ENTITY_REFERENCE_NODE:
            out.printf("%s<!-- Ignoring ENTITY_REFERENCE -->\n", indent);
            break;
        case Node.NOTATION_NODE:
            out.printf("%s<!-- Ignoring NOTATION -->\n", indent);
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            out.printf("%s<!-- Ignoring PROCESSING_INSTRUCTION -->\n", indent);
            break;
        case Node.TEXT_NODE:
            out.printf("%s%s\n", indent, ((Text) node).getData());
            break;
        default:
            out.printf("%s<!-- Ignoring NodeType#%d -->\n", indent,
                       node.getNodeType());
            break;
        }
    }

    private static void dumpElement(PrintStream out, Element elem,
                                    String indent)
    {
        out.printf("%s<%s", indent, elem.getTagName());

        NamedNodeMap attrs = elem.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);

            out.printf(" %s=\"%s\"", attr.getName(), attr.getValue());
        }

        NodeList kids = elem.getChildNodes();
        String text = elem.getTextContent();

        if (kids == null || kids.getLength() == 0) {
            out.println("/>");
        } else {
            out.println(">");
            final String indent2 = indent + "  ";

            for (int i = 0; i < kids.getLength(); i++) {
                dumpNode(out, kids.item(i), indent2);
            }

            out.printf("%s</%s>\n", indent, elem.getTagName());
        }
    }

    public static final Node extractNode(Node topNode, String pathExpr)
        throws JAXPUtilException
    {
        return extractNode(XPathFactory.newInstance().newXPath(), topNode,
                           pathExpr);
    }

    public static final Node extractNode(XPath xpath, Node topNode,
                                         String pathExpr)
        throws JAXPUtilException
    {
        try {
            return (Node) xpath.evaluate(pathExpr, topNode,
                                         XPathConstants.NODE);
        } catch (XPathExpressionException xpex) {
            throw new JAXPUtilException("Bad xpath " + pathExpr, xpex);
        }
    }

    public static final NodeList extractNodeList(Node topNode, String pathExpr)
        throws JAXPUtilException
    {
        return extractNodeList(XPathFactory.newInstance().newXPath(), topNode,
                               pathExpr);
    }

    public static final NodeList extractNodeList(XPath xpath, Node topNode,
                                                 String pathExpr)
        throws JAXPUtilException
    {
        try {
            return (NodeList) xpath.evaluate(pathExpr, topNode,
                                         XPathConstants.NODESET);
        } catch (XPathExpressionException xpex) {
            throw new JAXPUtilException("Bad xpath " + pathExpr, xpex);
        }
    }

    public static final String extractText(Node topNode, String pathExpr)
        throws JAXPUtilException
    {
        return extractText(XPathFactory.newInstance().newXPath(), topNode,
                           pathExpr);
    }

    public static final String extractText(XPath xpath, Node topNode,
                                           String pathExpr)
        throws JAXPUtilException
    {
        try {
            return xpath.evaluate(pathExpr, topNode);
        } catch (XPathExpressionException xpex) {
            throw new JAXPUtilException("Bad xpath " + pathExpr, xpex);
        }
    }

    /**
     * Load a file as an XML document.
     *
     * @param dir directory path
     * @param name XML file name
     *
     * @return XML Document object
     *
     * @throws JAXPUtilException if there is a problem
     */
    public static Document loadXMLDocument(File dir, String name)
        throws JAXPUtilException
    {
        // build config file path
        File xmlFile = new File(dir, name);
        if (!xmlFile.exists()) {
            xmlFile = new File(dir, name + ".xml");
            if (!xmlFile.exists()) {
                throw new JAXPUtilException("Couldn't find " + name +
                                           " in " + dir);
            }
        }

        DocumentBuilder bldr;
        try {
            bldr = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            throw new JAXPUtilException("Cannot build XML parser", pce);
        }

        try {
            return bldr.parse(xmlFile);
        } catch (IOException ioe) {
            throw new JAXPUtilException("Cannot read " + xmlFile, ioe);
        } catch (SAXException se) {
            throw new JAXPUtilException("Cannot parse " + xmlFile, se);
        }
    }
}
