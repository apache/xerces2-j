/*
 * Copyright 2000-2004 The Apache Software Foundation.
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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Id$
 */
public class DocumentBuilderImpl extends DocumentBuilder
        implements JAXPConstants
{
    private EntityResolver er = null;
    private ErrorHandler eh = null;
    private DOMParser domParser = null;

    DocumentBuilderImpl(DocumentBuilderFactory dbf, Hashtable dbfAttrs)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        domParser = new DOMParser();

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler
        if (dbf.isValidating()) {
            setErrorHandler(new DefaultValidationErrorHandler());
        }

        domParser.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.VALIDATION_FEATURE, dbf.isValidating());

        // "namespaceAware" == SAX Namespaces feature
        domParser.setFeature(Constants.SAX_FEATURE_PREFIX +
                             Constants.NAMESPACES_FEATURE,
                             dbf.isNamespaceAware());

        // Set various parameters obtained from DocumentBuilderFactory
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.INCLUDE_IGNORABLE_WHITESPACE,
                             !dbf.isIgnoringElementContentWhitespace());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.CREATE_ENTITY_REF_NODES_FEATURE,
                             !dbf.isExpandEntityReferences());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.INCLUDE_COMMENTS_FEATURE,
                             !dbf.isIgnoringComments());
        domParser.setFeature(Constants.XERCES_FEATURE_PREFIX +
                             Constants.CREATE_CDATA_NODES_FEATURE,
                             !dbf.isCoalescing());

        setDocumentBuilderFactoryAttributes(dbfAttrs);
    }

    /**
     * Set any DocumentBuilderFactory attributes of our underlying DOMParser
     *
     * Note: code does not handle possible conflicts between DOMParser
     * attribute names and JAXP specific attribute names,
     * eg. DocumentBuilderFactory.setValidating()
     */
    private void setDocumentBuilderFactoryAttributes(Hashtable dbfAttrs)
        throws SAXNotSupportedException, SAXNotRecognizedException
    {
        if (dbfAttrs == null) {
            // Nothing to do
            return;
        }

        for (Enumeration e = dbfAttrs.keys(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            Object val = dbfAttrs.get(name);
            if (val instanceof Boolean) {
                // Assume feature
                domParser.setFeature(name, ((Boolean)val).booleanValue());
            } else {
                // Assume property
                if (JAXP_SCHEMA_LANGUAGE.equals(name)) {
                    // JAXP 1.2 support
                    //None of the properties will take effect till the setValidating(true) has been called                                        
                    if ( W3C_XML_SCHEMA.equals(val) ) {
                        if( isValidating() ) {
                            domParser.setFeature(
                                Constants.XERCES_FEATURE_PREFIX +
                                Constants.SCHEMA_VALIDATION_FEATURE, true);
                            // this should allow us not to emit DTD errors, as expected by the 
                            // spec when schema validation is enabled
                            domParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
                        }
                    }
        		} else if(JAXP_SCHEMA_SOURCE.equals(name)){
               		if( isValidating() ) {
						String value=(String)dbfAttrs.get(JAXP_SCHEMA_LANGUAGE);
						if(value !=null && W3C_XML_SCHEMA.equals(value)){
            				domParser.setProperty(name, val);
						}else{
                            throw new IllegalArgumentException(
                                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                                "jaxp-order-not-supported",
                                new Object[] {JAXP_SCHEMA_LANGUAGE, JAXP_SCHEMA_SOURCE}));
						}
					}
            	} else {
                    // Let Xerces code handle the property
                    domParser.setProperty(name, val);
				}
			}
		}
	}

    /**
     * Non-preferred: use the getDOMImplementation() method instead of this
     * one to get a DOM Level 2 DOMImplementation object and then use DOM
     * Level 2 methods to create a DOM Document object.
     */
    public Document newDocument() {
        return new org.apache.xerces.dom.DocumentImpl();
    }

    public DOMImplementation getDOMImplementation() {
        return DOMImplementationImpl.getDOMImplementation();
    }

    public Document parse(InputSource is) throws SAXException, IOException {
        if (is == null) {
            throw new IllegalArgumentException(
                DOMMessageFormatter.formatMessage(DOMMessageFormatter.DOM_DOMAIN, 
                "jaxp-null-input-source", null));
        }

        if (er != null) {
            domParser.setEntityResolver(er);
        }

        if (eh != null) {
            domParser.setErrorHandler(eh);      
        }

        domParser.parse(is);
        return domParser.getDocument();
    }

    public boolean isNamespaceAware() {
        try {
            return domParser.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.NAMESPACES_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public boolean isValidating() {
        try {
            return domParser.getFeature(Constants.SAX_FEATURE_PREFIX +
                                        Constants.VALIDATION_FEATURE);
        } catch (SAXException x) {
            throw new IllegalStateException(x.getMessage());
        }
    }

    public void setEntityResolver(org.xml.sax.EntityResolver er) {
        this.er = er;
    }

    public void setErrorHandler(org.xml.sax.ErrorHandler eh) {
        // If app passes in a ErrorHandler of null, then ignore all errors
        // and warnings
        this.eh = (eh == null) ? new DefaultHandler() : eh;
    }

    // package private
    DOMParser getDOMParser() {
        return domParser;
    }
}
