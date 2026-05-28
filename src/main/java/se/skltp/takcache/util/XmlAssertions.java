package se.skltp.takcache.util;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

/**
 * Utility class for XML assertions using standard Java XPath API.
 * Replaces the xmlunit dependency with built-in JDK functionality.
 * This was done because as of MAY 2026, the xmlunit-assertj and xmlunit-core libraries
 *     have not been maintained since OCT 2025 and have known security vulnerabilities.
 */
public class XmlAssertions {

    private final String xmlContent;
    private final Document document;

    /**
     * Create an XmlAssertions instance from XML string content.
     *
     * @param xmlContent the XML content as a string
     * @return XmlAssertions instance
     * @throws IllegalArgumentException if XML parsing fails
     */
    public static XmlAssertions assertThat(String xmlContent) {
        return new XmlAssertions(xmlContent);
    }

    /**
     * Private constructor that parses the XML content.
     *
     * @param xmlContent the XML content as a string
     * @throws IllegalArgumentException if XML parsing fails
     */
    private XmlAssertions(String xmlContent) {
        this.xmlContent = xmlContent;
        this.document = parseXml(xmlContent);
    }

    /**
     * Assert that the XML document contains the specified XPath.
     *
     * @param xpathExpression the XPath expression to evaluate
     * @throws AssertionError if the XPath is not found in the document
     */
    public XmlAssertions hasXPath(String xpathExpression) {
        try {
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            Boolean result = (Boolean) xpath.evaluate(
                    xpathExpression,
                    document,
                    XPathConstants.BOOLEAN
            );

            if (!result) {
                throw new AssertionError(
                        String.format("XPath expression '%s' not found in XML document", xpathExpression)
                );
            }
        } catch (XPathExpressionException e) {
            throw new AssertionError(
                    String.format("Invalid XPath expression '%s': %s", xpathExpression, e.getMessage()),
                    e
            );
        }

        return this;
    }

    /**
     * Parse XML string content into a DOM Document.
     *
     * @param xmlContent the XML content as a string
     * @return parsed Document
     * @throws IllegalArgumentException if parsing fails
     */
    private static Document parseXml(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Disable DTD processing for security
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xmlContent));

            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    String.format("Failed to parse XML content: %s", e.getMessage()),
                    e
            );
        }
    }
}