package se.skltp.takcache.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class XmlAssertionsTest {

    @Test
    void hasXPathShouldFindExistingElementsAndSupportChaining() {
        String xml = """
                <persistentCache>
                    <virtualiseringsInfo id=\"v1\">demo</virtualiseringsInfo>
                    <anropsBehorighetsInfo/>
                </persistentCache>
                """;

        XmlAssertions assertions = XmlAssertions.assertThat(xml);

        XmlAssertions returned = assertions
                .hasXPath("/persistentCache/virtualiseringsInfo")
                .hasXPath("/persistentCache/virtualiseringsInfo[@id='v1']")
                .hasXPath("/persistentCache/anropsBehorighetsInfo")
                .hasXPath("count(/persistentCache/*)=2");

        assertSame(assertions, returned);
    }

    @Test
    void hasXPathShouldThrowAssertionErrorWhenXPathDoesNotMatch() {
        String xml = "<root><item/></root>";

        AssertionError error = assertThrows(
                AssertionError.class,
                () -> XmlAssertions.assertThat(xml).hasXPath("/root/missing")
        );

        assertTrue(error.getMessage().contains("XPath expression '/root/missing' not found"));
    }

    @Test
    void hasXPathShouldWrapInvalidXPathExpressions() {
        String xml = "<root><item/></root>";

        AssertionError error = assertThrows(
                AssertionError.class,
                () -> XmlAssertions.assertThat(xml).hasXPath("//*[")
        );

        assertTrue(error.getMessage().contains("Invalid XPath expression"));
        assertTrue(error.getCause() instanceof javax.xml.xpath.XPathExpressionException);
    }

    @Test
    void assertThatShouldThrowIllegalArgumentExceptionForMalformedXml() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> XmlAssertions.assertThat("<root><broken></root>")
        );

        assertTrue(error.getMessage().contains("Failed to parse XML content"));
    }

    @Test
    void assertThatShouldRejectXmlWithDoctype() {
        String xmlWithDoctype = """
                <!DOCTYPE root [
                  <!ELEMENT root ANY>
                ]>
                <root><item/></root>
                """;

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> XmlAssertions.assertThat(xmlWithDoctype)
        );

        assertTrue(error.getMessage().contains("Failed to parse XML content"));
    }

    @Test
    void assertThatShouldThrowIllegalArgumentExceptionWhenXmlIsNull() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> XmlAssertions.assertThat(null)
        );

        assertTrue(error.getMessage().contains("Failed to parse XML content"));
    }

    @Test
    void hasXPathShouldSupportBooleanFunctions() {
        String xml = "<root><item code=\"A\"/><item code=\"B\"/></root>";

        assertDoesNotThrow(() -> XmlAssertions.assertThat(xml)
                .hasXPath("count(/root/item)=2")
                .hasXPath("boolean(/root/item[@code='A'])"));
    }
}

