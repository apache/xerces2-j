/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.xs.opti;


import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.XMLChar;

import org.w3c.dom.Document;

/**
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class SchemaDOMParser extends DefaultXMLDocumentHandler {

    //
    // Data
    //
    
    /** Property identifier: error reporter. */
    public static final String ERROR_REPORTER =
    Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    // the locator containing line/column information
    protected XMLLocator   fLocator;

    SchemaDOM schemaDOM;
   
    XMLParserConfiguration config;

    //
    // Constructors
    //

    /** Default constructor. */
    public SchemaDOMParser(XMLParserConfiguration config) {
        schemaDOM = new SchemaDOM();
        this.config = config;
    }


    // Where xs:appinfo or xs:documentation starts;
    // -1 means not in the scope of either of the two elements.
    private int fAnnotationDepth = -1;
    // The current element depth
    private int fDepth = -1;
    // Use to report the error when characters are not allowed.
    XMLErrorReporter fErrorReporter;


    //
    // XMLDocumentHandler methods
    //

    public void startDocument(XMLLocator locator, String encoding, 
                              NamespaceContext namespaceContext, Augmentations augs)
        throws XNIException {
        fLocator = locator;
    } // startDocument(XMLLocator,String,Augmentations)

    /**
     * The end of the document.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocument(Augmentations augs) throws XNIException {
	// To debug the DOM created uncomment the line below
	// schemaDOM.printDOM();
    } // endDocument()


    /**
     * Character content.
     * 
     * @param text   The content.
     * @param augs   Additional information that may include infoset augmentations
     *               
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        // when it's not within xs:appinfo or xs:documentation
        if (fAnnotationDepth == -1) {
            for (int i=text.offset; i<text.offset+text.length; i++) {
                // and there is a non-whitespace character
                if (!XMLChar.isSpace(text.ch[i])) {
                    // only get the error reporter when reporting an error
                    if (fErrorReporter == null) {
                        try {
                            fErrorReporter = (XMLErrorReporter)config.getProperty(ERROR_REPORTER);
                        } catch (Exception e) {
                            //ignore the excpetion
                        }
                        if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                            XSMessageFormatter xmft = new XSMessageFormatter();
                            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
                        }
                    }
                    // the string we saw: starting from the first non-whitespace character.
                    String txt = new String(text.ch, i, text.length+text.offset-i);
                    // report an error
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "s4s-elt-character",
                                               new Object[]{txt},
                                               XMLErrorReporter.SEVERITY_ERROR);
                    break;
                }
            }
            // don't call super.characters() when it's not within one of the 2
            // annotation elements: the traversers ignore them anyway. We can
            // save time/memory creating the text nodes.
        }
        // when it's within either of the 2 elements, characters are allowed
        // and we need to store them.
        else {
            schemaDOM.characters(text, augs);
        }

    }


    /**
     * The start of an element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {

        schemaDOM.startElement(element, attributes, augs,
                               fLocator.getLineNumber(),
                               fLocator.getColumnNumber());

        fDepth++;
        // if it's not within either element, check whether it's one of them
        // if so, record the current depth, so that any element with larger
        // depth is allowed to have character data.
        if (fAnnotationDepth == -1) {
            if (element.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA &&
                (element.localpart == SchemaSymbols.ELT_APPINFO ||
                element.localpart == SchemaSymbols.ELT_DOCUMENTATION)) {
                fAnnotationDepth = fDepth;
            }
        }

    }


    /**
     * An empty element.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs       Additional information that may include infoset augmentations
     *                   
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {

        schemaDOM.emptyElement(element, attributes, augs,
                               fLocator.getLineNumber(),
                               fLocator.getColumnNumber());

    }


    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     * @param augs    Additional information that may include infoset augmentations
     *                
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endElement(QName element, Augmentations augs) throws XNIException {

	schemaDOM.endElement(element,  augs);
    // when we reach the endElement of xs:appinfo or xs:documentation,
    // change fAnnotationDepth to -1
    if (fAnnotationDepth == fDepth)
        fAnnotationDepth = -1;
    fDepth--;

    }
    
    
    //
    // other methods
    //
    
    /**
     * Returns the DOM document object.
     */
    public Document getDocument() {
        return schemaDOM;
    }

}