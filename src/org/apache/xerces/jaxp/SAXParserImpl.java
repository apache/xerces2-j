/*
 * Copyright 2000-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.jaxp;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;

import org.apache.xerces.impl.Constants;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import org.apache.xerces.util.SAXMessageFormatter;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.PSVIProvider;

/**
 * This is the implementation specific class for the
 * <code>javax.xml.parsers.SAXParser</code>.
 * 
 * @author Rajiv Mordani
 * @author Edwin Goei
 * 
 * @version $Id$
 */
public class SAXParserImpl extends javax.xml.parsers.SAXParser
    implements JAXPConstants, PSVIProvider {
    
    /** Feature identifier: namespaces. */
    private static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    
    /** Feature identifier: namespace prefixes. */
    private static final String NAMESPACE_PREFIXES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE;
    
    /** Feature identifier: validation. */
    private static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    
    /** Feature identifier: XML Schema validation */
    private static final String XMLSCHEMA_VALIDATION_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    
    /** Feature identifier: XInclude processing */
    private static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    
    /** Property identifier: security manager. */
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;

    private XMLReader xmlReader;
    private String schemaLanguage = null;     // null means DTD
    private final Schema grammar;
    
    /**
     * Create a SAX parser with the associated features
     * @param features Hashtable of SAX features, may be null
     */
    SAXParserImpl(SAXParserFactoryImpl spf, Hashtable features) 
        throws SAXException {
        this(spf, features, false);
    }
    
    /**
     * Create a SAX parser with the associated features
     * @param features Hashtable of SAX features, may be null
     */
    SAXParserImpl(SAXParserFactoryImpl spf, Hashtable features, boolean secureProcessing)
        throws SAXException
    {
        // Instantiate a SAXParser directly and not through SAX so that we
        // use the right ClassLoader
        xmlReader = new org.apache.xerces.parsers.SAXParser();

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler.
        if (spf.isValidating()) {
            xmlReader.setErrorHandler(new DefaultValidationErrorHandler());
        }

        xmlReader.setFeature(VALIDATION_FEATURE, spf.isValidating());

        // JAXP "namespaceAware" == SAX Namespaces feature
        // Note: there is a compatibility problem here with default values:
        // JAXP default is false while SAX 2 default is true!
        xmlReader.setFeature(NAMESPACES_FEATURE, spf.isNamespaceAware());

        // SAX "namespaces" and "namespace-prefixes" features should not
        // both be false.  We make them opposite for backward compatibility
        // since JAXP 1.0 apps may want to receive xmlns* attributes.
        xmlReader.setFeature(NAMESPACE_PREFIXES_FEATURE, !spf.isNamespaceAware());
        
        // Avoid setting the XInclude processing feature if the value is false.
        // This will keep the configuration from throwing an exception if it
        // does not support XInclude.
        if (spf.isXIncludeAware()) {
            xmlReader.setFeature(XINCLUDE_FEATURE, true);
        }
        
        // If the secure processing feature is on set a security manager.
        if (secureProcessing) {
            xmlReader.setProperty(SECURITY_MANAGER, new SecurityManager());
        }
        
        this.grammar = spf.getSchema();

        setFeatures(features);
    }

    /**
     * Set any features of our XMLReader based on any features set on the
     * SAXParserFactory.
     *
     * XXX Does not handle possible conflicts between SAX feature names and
     * JAXP specific feature names, eg. SAXParserFactory.isValidating()
     */
    private void setFeatures(Hashtable features)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (features != null) {
            for (Enumeration e = features.keys(); e.hasMoreElements();) {
                String feature = (String)e.nextElement();
                boolean value = ((Boolean)features.get(feature)).booleanValue();
                xmlReader.setFeature(feature, value);
            }
        }
    }

    public Parser getParser() throws SAXException {
        // Xerces2 AbstractSAXParser implements SAX1 Parser
        // assert(xmlReader instanceof Parser);
        return (Parser) xmlReader;
    }

    /**
     * Returns the XMLReader that is encapsulated by the implementation of
     * this class.
     */
    public XMLReader getXMLReader() {
        return xmlReader;
    }

    public boolean isNamespaceAware() {
        try {
            return xmlReader.getFeature(NAMESPACES_FEATURE);
        } 
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return xmlReader.getFeature(VALIDATION_FEATURE);
        } 
        catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }
    
    /**
     * Gets the XInclude processing mode for this parser
     * @return the state of XInclude processing mode
     */
    public boolean isXIncludeAware() {
        try {
            return xmlReader.getFeature(XINCLUDE_FEATURE);
        }
        catch (SAXException exc) {
            return false;
        }
    }

    /**
     * Sets the particular property in the underlying implementation of 
     * org.xml.sax.XMLReader.
     */
    public void setProperty(String name, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
            // JAXP 1.2 support            
            if ( W3C_XML_SCHEMA.equals(value) ) {
                //None of the properties will take effect till the setValidating(true) has been called                                                        
                if( isValidating() ) {
                    schemaLanguage = W3C_XML_SCHEMA;
                    xmlReader.setFeature(XMLSCHEMA_VALIDATION_FEATURE, true);
                    // this will allow the parser not to emit DTD-related
                    // errors, as the spec demands
                    xmlReader.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                }
                
            } else if (value == null) {
                schemaLanguage = null;
                xmlReader.setFeature(XMLSCHEMA_VALIDATION_FEATURE, false);
            } else {
                // REVISIT: It would be nice if we could format this message
                // using a user specified locale as we do in the underlying
                // XMLReader -- mrglavas
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(null, "schema-not-supported", null));
            }
        } 
        else if(JAXP_SCHEMA_SOURCE.equals(name)) {
            String val = (String)getProperty(JAXP_SCHEMA_LANGUAGE);
            if ( val != null && W3C_XML_SCHEMA.equals(val) ) {
                xmlReader.setProperty(name, value);
            }
            else {
                throw new SAXNotSupportedException(
                    SAXMessageFormatter.formatMessage(null, 
                    "jaxp-order-not-supported", 
                    new Object[] {JAXP_SCHEMA_LANGUAGE, JAXP_SCHEMA_SOURCE}));
            }
		}
		else {
            xmlReader.setProperty(name, value);
        }
    }

    /**
     * returns the particular property requested for in the underlying 
     * implementation of org.xml.sax.XMLReader.
     */
    public Object getProperty(String name)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
            // JAXP 1.2 support
            return schemaLanguage;
        } else {
            return xmlReader.getProperty(name);
        }
    }
    
    public Schema getSchema() {
        return grammar;
    }
    
    // TODO: Add in implementation.
    public void reset() {}
    
    /*
     * PSVIProvider methods
     */

    public ElementPSVI getElementPSVI() {
        return ((PSVIProvider)xmlReader).getElementPSVI();
    }

    public AttributePSVI getAttributePSVI(int index) {
        return ((PSVIProvider)xmlReader).getAttributePSVI(index);
    }

    public AttributePSVI getAttributePSVIByName(String uri, String localname) {
        return ((PSVIProvider)xmlReader).getAttributePSVIByName(uri, localname);
    }
}
