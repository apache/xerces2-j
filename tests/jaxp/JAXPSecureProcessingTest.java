/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jaxp;

import java.io.IOException;
import java.util.Locale;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Tests for JAXP secure processing features.
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
public class JAXPSecureProcessingTest extends TestCase {
    
    private static final String ENTITY_EXPANSION_LIMIT_PROPERTY_NAME = "jdk.xml.entityExpansionLimit";
    private static final String MAX_OCCUR_LIMIT_PROPERTY_NAME = "jdk.xml.maxOccur";
    private static final String TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.totalEntitySizeLimit";
    private static final String MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.maxGeneralEntitySizeLimit";
    private static final String MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME = "jdk.xml.maxParameterEntitySizeLimit";
    private static final String RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME = "jdk.xml.resolveExternalEntities";
    
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("javax.xml.parsers.SAXParserFactory", "org.apache.xerces.jaxp.SAXParserFactoryImpl");
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI, 
                "org.apache.xerces.jaxp.validation.XMLSchemaFactory");
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration", 
                "org.apache.xerces.parsers.SecureProcessingConfiguration");
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "0");
        System.setProperty(MAX_OCCUR_LIMIT_PROPERTY_NAME, "0");
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "0");
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "0");
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "0");
        System.setProperty(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, "true");
        // English Locale required for the exception message assertions to match the correct number format
        Locale.setDefault(Locale.ENGLISH);
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        Locale.setDefault(DEFAULT_LOCALE);
    }
    
    public void testSAXEntityExpansionLimitSG() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "10000");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"10,000\"") != -1);
        }
    }
    
    public void testSAXEntityExpansionLimitSP() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "20000");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"20,000\"") != -1);
        }
    }
    
    public void testSAXEntityExpansionLimitDG() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "30000");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"30,000\"") != -1);
        }
    }
    
    public void testSAXEntityExpansionLimitDP() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "40000");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"40,000\"") != -1);
        }
    }
    
    public void testDOMEntityExpansionLimitSG() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "50000");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"50,000\"") != -1);
        }
    }
    
    public void testDOMEntityExpansionLimitSP() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "60000");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"60,000\"") != -1);
        }
    }
    
    public void testDOMEntityExpansionLimitDG() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "70000");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"70,000\"") != -1);
        }
    }
    
    public void testDOMEntityExpansionLimitDP() throws Exception {
        System.setProperty(ENTITY_EXPANSION_LIMIT_PROPERTY_NAME, "80000");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"80,000\"") != -1);
        }
    }
    
    public void testSAXTotalEntitySizeLimitSG() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "1");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"1\"") != -1);
        }
    }
    
    public void testSAXTotalEntitySizeLimitSP() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "10000");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"10,000\"") != -1);
        }
    }
    
    public void testSAXTotalEntitySizeLimitDG() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "2");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"2\"") != -1);
        }
    }
    
    public void testSAXTotalEntitySizeLimitDP() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "20000");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"20,000\"") != -1);
        }
    }
    
    public void testDOMTotalEntitySizeLimitSG() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "3");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"3\"") != -1);
        }
    }
    
    public void testDOMTotalEntitySizeLimitSP() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "30000");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"30,000\"") != -1);
        }
    }
    
    public void testDOMTotalEntitySizeLimitDG() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "4");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"4\"") != -1);
        }
    }
    
    public void testDOMTotalEntitySizeLimitDP() throws Exception {
        System.setProperty(TOTAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "40000");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"40,000\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitSG() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "1");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"1\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitSG11() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "1");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("gEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"1\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitSP() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "10000");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"10,000\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitSP11() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "10000");
        XMLReader reader = newSecureXMLReader();
        try {
            reader.parse(new InputData("pEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"10,000\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitDG() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "2");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"2\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitDG11() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "2");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("gEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"2\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitDP() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "20000");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"20,000\"") != -1);
        }
    }
    
    public void testSAXEntitySizeLimitDP11() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "20000");
        XMLReader reader = newDefaultXMLReader();
        try {
            reader.parse(new InputData("pEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"20,000\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitSG() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "3");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"3\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitSG11() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "3");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"3\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitSP() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "30000");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"30,000\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitSP11() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "30000");
        DocumentBuilder reader = newSecureDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"30,000\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitDG() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "4");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"4\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitDG11() throws Exception {
        System.setProperty(MAX_GENERAL_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "4");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("gEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"4\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitDP() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "40000");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"40,000\"") != -1);
        }
    }
    
    public void testDOMEntitySizeLimitDP11() throws Exception {
        System.setProperty(MAX_PARAMETER_ENTITY_SIZE_LIMIT_PROPERTY_NAME, "40000");
        DocumentBuilder reader = newDefaultDocumentBuilder();
        try {
            reader.parse(new InputData("pEntitySP11.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("\"40,000\"") != -1);
        }
    }
    
    public void testSAXMaxOccursLimit() throws Exception {
        System.setProperty(MAX_OCCUR_LIMIT_PROPERTY_NAME, "2500");
        XMLReader reader = newSecureSchemaAwareXMLReader();
        try {
            reader.parse(new InputData("maxOccursSP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("2,500") != -1);
        }
    }
    
    public void testDOMMaxOccursLimit() throws Exception {
        System.setProperty(MAX_OCCUR_LIMIT_PROPERTY_NAME, "3500");
        DocumentBuilder reader = newSecureSchemaAwareDocumentBuilder();
        try {
            reader.parse(new InputData("maxOccursSP.xml"));
            fail("Expected SAXParseException");
        }
        catch (SAXParseException se) {
            assertTrue(se.getMessage().indexOf("3,500") != -1);
        }
    }
    
    public void testSAXEnableExternalEntityResolution() throws Exception {
        System.setProperty(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, "true");
        XMLReader reader = newSecureXMLReader();
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                assertEquals("xerces:///x:/this/does/not/exist.xml", systemId);
                return null;
            }
        });
        try {
            reader.parse(new InputData("badExternalEntity.xml"));
            fail("Expected IOException");
        }
        catch (IOException ioe) {}
    }
    
    public void testDOMEnableExternalEntityResolution() throws Exception {
        System.setProperty(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, "true");
        DocumentBuilder reader = newSecureDocumentBuilder();
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                assertEquals("xerces:///x:/this/does/not/exist.xml", systemId);
                return null;
            }
        });
        try {
            reader.parse(new InputData("badExternalEntity.xml"));
            fail("Expected IOException");
        }
        catch (IOException ioe) {}
    }
    
    public void testSAXDisableExternalEntityResolution() throws Exception {
        System.setProperty(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, "false");
        XMLReader reader = newSecureXMLReader();
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                fail("resolveEntity call not expected.");
                return null;
            }
        });
        reader.setContentHandler(new ContentHandler() {
            final int START_DOCUMENT = 0;
            final int START_ELEMENT = 1;
            final int SKIPPED_ENTITY = 2;
            final int END_ELEMENT = 3;
            final int END_DOCUMENT = 4;
            int state = START_DOCUMENT;
            public void startPrefixMapping(String prefix, String uri) {
                fail("startPrefixMapping not expected.");
            }
            public void startElement(String uri, String localName, 
                    String qName, Attributes atts) throws SAXException {
                assertEquals(START_ELEMENT, state);
                assertEquals("root", localName);
                state = SKIPPED_ENTITY; 
            }
            public void startDocument() throws SAXException {
                assertEquals(START_DOCUMENT, state);
                state = START_ELEMENT;
            }
            public void skippedEntity(String name) throws SAXException {
                assertEquals(SKIPPED_ENTITY, state);
                assertEquals("badEntity", name);
                state = END_ELEMENT;  
            }
            public void setDocumentLocator(Locator locator) {
                // NO-OP
            }
            public void processingInstruction(String target, String data)
                    throws SAXException {
                fail("processingInstruction not expected.");
            }
            public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {
                fail("ignorableWhitespace not expected.");
            }
            public void endPrefixMapping(String arg0) throws SAXException {
                fail("endPrefixMapping not expected.");
            }
            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                assertEquals(END_ELEMENT, state);
                assertEquals("root", localName);
                state = END_DOCUMENT;
            }
            public void endDocument() throws SAXException {
                assertEquals(END_DOCUMENT, state); 
            }
            public void characters(char[] ch, int start, int length) throws SAXException {
                fail("characters not expected.");
            }
        });
        reader.parse(new InputData("badExternalEntity.xml"));
    }
    
    public void testDOMDisableExternalEntityResolution() throws Exception {
        System.setProperty(RESOLVE_EXTERNAL_ENTITIES_PROPERTY_NAME, "false");
        DocumentBuilder reader = newSecureDocumentBuilder();
        reader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                fail("resolveEntity call not expected.");
                return null;
            }
        });
        Document doc = reader.parse(new InputData("badExternalEntity.xml"));
        Element e = doc.getDocumentElement();
        assertEquals("root", e.getLocalName());
        NodeList nl = e.getChildNodes();
        assertEquals(1, nl.getLength());
        Node n = nl.item(0);
        assertEquals(Node.ENTITY_REFERENCE_NODE, n.getNodeType());
        assertEquals("badEntity", n.getNodeName());
        assertEquals(0, n.getChildNodes().getLength());
    }
    
    private static XMLReader newSecureXMLReader() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return spf.newSAXParser().getXMLReader();
    }
    
    private static XMLReader newSecureSchemaAwareXMLReader() throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        spf.setSchema(sf.newSchema());
        spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return spf.newSAXParser().getXMLReader();
    }
    
    private static XMLReader newDefaultXMLReader() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        return spf.newSAXParser().getXMLReader();
    }
    
    private static DocumentBuilder newSecureDocumentBuilder() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setExpandEntityReferences(false);
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return dbf.newDocumentBuilder();
    }
    
    private static DocumentBuilder newSecureSchemaAwareDocumentBuilder() throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setExpandEntityReferences(false);
        dbf.setSchema(sf.newSchema());
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return dbf.newDocumentBuilder();
    }
    
    private static DocumentBuilder newDefaultDocumentBuilder() throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setExpandEntityReferences(false);
        return dbf.newDocumentBuilder();
    }
}
