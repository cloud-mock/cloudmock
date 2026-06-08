package io.cloudmock.s3.cli;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

final class XmlUtil {

    private XmlUtil() {}

    static List<String> extractAll(String xml, String xpathExpr) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xml)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) xpath.evaluate(xpathExpr, doc, XPathConstants.NODESET);
            List<String> results = new ArrayList<>();
            for (int i = 0; i < nodes.getLength(); i++) {
                results.add(nodes.item(i).getTextContent());
            }
            return results;
        } catch (Exception e) {
            return List.of();
        }
    }
}
