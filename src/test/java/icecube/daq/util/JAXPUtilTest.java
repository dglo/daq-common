package icecube.daq.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.*;
import static org.junit.Assert.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

public class JAXPUtilTest
{
    @Test
    public void testLoadXMLDocument()
        throws JAXPUtilException, IOException
    {
        String sample = "<a><b val=\"123\">" +
            "<bx num=\"1\" name=\"one\"/>" +
            "<bx num=\"3\" name=\"three\"/>" +
            "</b>" +
            "<c><cx>/dev/null</cx></c>" +
            "<d name=\"xyz\"/>" +
            "</a>";

        File tmpFile = File.createTempFile("comp", ".xml");
        tmpFile.deleteOnExit();

        PrintWriter out = new PrintWriter(tmpFile);
        out.print(sample);
        out.close();

        Document doc = JAXPUtil.loadXMLDocument(tmpFile.getParentFile(),
                                                tmpFile.getName());
        //dumpDoc(System.err, doc);

        Element ad = (Element) JAXPUtil.extractNode(doc, "a/d");
        assertNotNull("Cannot find a/d", ad);
        assertEquals("Bad attribute", "xyz", ad.getAttribute("name"));

        NodeList elems = JAXPUtil.extractNodeList(doc, "a/b/bx[@num='1']");
        assertNotNull("Cannot find nodes", elems);
        assertFalse("Empty list of nodes", elems.getLength() == 0);

        boolean foundOne = false;
        for (int i = 0; i < elems.getLength(); i++) {
            assertFalse("Found multiple top nodes", foundOne);
            foundOne = true;

            Element elem = (Element) elems.item(i);
            assertTrue("Element #" + i + " has no attributes",
                       elem.hasAttributes());

            boolean foundNum = false;
            boolean foundName = false;
            NamedNodeMap attrs = elem.getAttributes();
            for (int j = 0; j < attrs.getLength(); j++) {
                Attr attr = (Attr) attrs.item(j);

                String name = attr.getName();
                if (name == "num") {
                    assertFalse("Found multiple 'num' attributes", foundNum);
                    foundNum = true;

                    assertEquals("Bad 'num' value", "1", attr.getValue());
                } else if (name == "name") {
                    assertFalse("Found multiple 'name' attributes", foundName);
                    foundName = true;

                    assertEquals("Bad 'name' value", "one", attr.getValue());
                } else {
                    fail("Unknown attribute '" + attr.getName() +
                         "'");
                }
            }

            assertTrue("Did not find 'num' attribute", foundNum);
            assertTrue("Did not find 'name' attribute", foundName);
        }

        assertTrue("Did not find top node", foundOne);
    }
}
