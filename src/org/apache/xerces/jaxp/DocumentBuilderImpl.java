/*
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000-2002 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:  
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 1999, Sun Microsystems, Inc., 
 * http://www.sun.com.  For more information on the Apache Software 
 * Foundation, please see <http://www.apache.org/>.
 */


package org.apache.xerces.jaxp;

import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.DocumentType;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.helpers.DefaultHandler;

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.impl.Constants;

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
            throw new IllegalArgumentException("InputSource cannot be null");
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
