/*
 * $Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

/**
 * @author Rajiv Mordani
 * @author Edwin Goei
 * @version $Revision$
 */
public class DocumentBuilderImpl extends DocumentBuilder {
    /** Xerces features */
    static final String XERCES_FEATURE_PREFIX =
                                        "http://apache.org/xml/features/";
    static final String CREATE_ENTITY_REF_NODES_FEATURE =
                                        "dom/create-entity-ref-nodes";
    static final String INCLUDE_IGNORABLE_WHITESPACE =
                                        "dom/include-ignorable-whitespace";

    private EntityResolver er = null;
    private ErrorHandler eh = null;
    private DOMParser domParser = null;

    private boolean namespaceAware = false;
    private boolean validating = false;

    DocumentBuilderImpl(DocumentBuilderFactory dbf, Hashtable dbfAttrs)
        throws SAXNotRecognizedException, SAXNotSupportedException
    {
        domParser = new DOMParser();

        // Validation
        validating = dbf.isValidating();
        String validation = "http://xml.org/sax/features/validation";
        domParser.setFeature(validation, validating);

        // If validating, provide a default ErrorHandler that prints
        // validation errors with a warning telling the user to set an
        // ErrorHandler
        if (validating) {
            setErrorHandler(new DefaultValidationErrorHandler());
        }

        // "namespaceAware" ==  SAX Namespaces feature
        namespaceAware = dbf.isNamespaceAware();
        domParser.setFeature("http://xml.org/sax/features/namespaces",
                             namespaceAware);

        // Set various parameters obtained from DocumentBuilderFactory
        //
        // XXX Note: Ignore features that are not yet implemented in
        // Xerces 2.  This code is different than in Xerces 1!
        try {
            domParser.setFeature(XERCES_FEATURE_PREFIX +
                                 INCLUDE_IGNORABLE_WHITESPACE,
                                 !dbf.isIgnoringElementContentWhitespace());
            domParser.setFeature(XERCES_FEATURE_PREFIX +
                                 CREATE_ENTITY_REF_NODES_FEATURE,
                                 !dbf.isExpandEntityReferences());
        } catch (Exception e) {
        }

        // XXX No way to control dbf.isIgnoringComments() or
        // dbf.isCoalescing()

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
        if (dbfAttrs != null) {
            for (Enumeration e = dbfAttrs.keys(); e.hasMoreElements();) {
                String name = (String)e.nextElement();
                Object val = dbfAttrs.get(name);
                if (val instanceof Boolean) {
                    // Assume feature
                    domParser.setFeature(name, ((Boolean)val).booleanValue());
                } else {
                    // Assume property
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
        return namespaceAware;
    }

    public boolean isValidating() {
        return validating;
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
