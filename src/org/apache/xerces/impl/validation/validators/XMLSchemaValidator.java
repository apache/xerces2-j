/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999,2000 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.validation.validators;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLValidator;
import org.apache.xerces.impl.validation.Grammar;
import org.apache.xerces.impl.validation.GrammarPool;
import org.apache.xerces.impl.validation.grammars.DTDGrammar;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentFilter;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDFilter;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelFilter;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Eric Ye, IBM
 *
 * @version $Id$
 */
public class XMLSchemaValidator
    extends XMLValidator {

    //
    // Data
    //

    //
    // Constructors
    //

    //
    // XMLComponent methods
    //


    //
    // XMLDocumentHandler methods
    //

    /**
     * The start of the document.
     *
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *     
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDocument(String systemId, String encoding) 
        throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(systemId, encoding);
        }

    } // startDocument(String,String)

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     * 
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void xmlDecl(String version, String encoding, String standalone)
        throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone);
        }
    
    } // xmlDecl(String,String,String)

    /**
     * Notifies of the presence of the DOCTYPE line in the document.
     * 
     * @param rootElement The name of the root element.
     * @param publicId    The public identifier if an external DTD or null
     *                    if the external DTD is specified using SYSTEM.
     * @param systemId    The system identifier if an external DTD, null
     *                    otherwise.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void doctypeDecl(String rootElement, String publicId, String systemId)
        throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.doctypeDecl(rootElement, publicId, systemId);
        }

    } // doctypeDecl(String,String,String)

    /**
     * The start of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     * @param uri    The URI bound to the prefix.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startPrefixMapping(prefix, uri);
        }
    
    } // startPrefixMapping(String,String)

    /**
     * The start of an element. If the document specifies the start element
     * by using an empty tag, then the startElement method will immediately
     * be followed by the endElement method, with no intervening methods.
     * 
     * @param element    The name of the element.
     * @param attributes The element attributes.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes)
        throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(element, attributes);
        }
    
    } // startElement(QName,XMLAttributes)

    /**
     * Character content.
     * 
     * @param text The content.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void characters(XMLString text) throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(text);
        }
    
    } // characters(XMLString)

    /**
     * Ignorable whitespace. For this method to be called, the document
     * source must have some way of determining that the text containing
     * only whitespace characters should be considered ignorable. For
     * example, the validator can determine if a length of whitespace
     * characters in the document are ignorable based on the element
     * content model.
     * 
     * @param text The ignorable whitespace.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text) throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(text);
        }
    
    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     * 
     * @param element The name of the element.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endElement(QName element) throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(element);
        }
    
    } // endElement(QName)

    /**
     * The end of a namespace prefix mapping. This method will only be
     * called when namespace processing is enabled.
     * 
     * @param prefix The namespace prefix.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endPrefixMapping(String prefix) throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endPrefixMapping(prefix);
        }
    
    } // endPrefixMapping(String)

    /** 
     * The start of a CDATA section. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startCDATA() throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA();
        }
    
    } // startCDATA()

    /**
     * The end of a CDATA section. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endCDATA() throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA();
        }
    
    } // endCDATA()

    /**
     * The end of the document.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDocument() throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument();
        }
    
    } // endDocument()

    //
    // XMLDocumentHandler and XMLDTDHandler methods
    //

    /**
     * This method notifies of the start of an entity. The document entity
     * has the pseudo-name of "[xml]"; The DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entity names are
     * just the entity name.
     * <p>
     * <strong>Note:</strong> Since the document is an entity, the handler
     * will be notified of the start of the document entity by calling the
     * startEntity method with the entity name "[xml]" <em>before</em> calling
     * the startDocument method. When exposing entity boundaries through the
     * SAX API, the document entity is never reported, however.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the start of the DTD entity by calling the
     * startEntity method with the entity name "[dtd]" <em>before</em> calling
     * the startDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal parameter entities).
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startEntity(String name, String publicId, String systemId,
                            String encoding) throws SAXException {

        // call handlers
        if (fInDTD) {
            fDTDGrammar.startEntity(name, publicId, systemId, encoding);
            if (fDTDHandler != null) {
                fDTDHandler.startEntity(name, publicId, systemId, encoding);
            }
        }
        else {
            if (fDocumentHandler != null) {
                fDocumentHandler.startEntity(name, publicId, systemId, encoding);
            }
        }

    } // startEntity(String,String,String,String)

    /**
     * Notifies of the presence of a TextDecl line in an entity. If present,
     * this method will be called immediately following the startEntity call.
     * <p>
     * <strong>Note:</strong> This method will never be called for the
     * document entity; it is only called for external general entities
     * referenced in document content.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param version  The XML version, or null if not specified.
     * @param encoding The IANA encoding name of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding) throws SAXException {

        // call handlers
        if (fInDTD) {
            fDTDGrammar.textDecl(version, encoding);
            if (fDTDHandler != null) {
                fDTDHandler.textDecl(version, encoding);
            }
        }
        else {
            if (fDocumentHandler != null) {
                fDocumentHandler.textDecl(version, encoding);
            }
        }

    } // textDecl(String,String)

    /**
     * A comment.
     * 
     * @param text The text in the comment.
     *
     * @throws SAXException Thrown by application to signal an error.
     */
    public void comment(XMLString text) throws SAXException {

        // call handlers
        if (fInDTD) {
            fDTDGrammar.comment(text);
            if (fDTDHandler != null) {
                fDTDHandler.comment(text);
            }
        }
        else {
            if (fDocumentHandler != null) {
                fDocumentHandler.comment(text);
            }
        }

    } // comment(XMLString)

    /**
     * A processing instruction. Processing instructions consist of a
     * target name and, optionally, text data. The data is only meaningful
     * to the application.
     * <p>
     * Typically, a processing instruction's data will contain a series
     * of pseudo-attributes. These pseudo-attributes follow the form of
     * element attributes but are <strong>not</strong> parsed or presented
     * to the application as anything other than text. The application is
     * responsible for parsing the data.
     * 
     * @param target The target.
     * @param data   The data or null if none specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data)
        throws SAXException {

        // call handlers
        if (fInDTD) {
            fDTDGrammar.processingInstruction(target, data);
            if (fDTDHandler != null && fDTDHandler != fDocumentHandler) {
                fDTDHandler.processingInstruction(target, data);
            }
        }
        else {
            if (fDocumentHandler != null) {
                fDocumentHandler.processingInstruction(target, data);
            }
        }

    } // processingInstruction(String,XMLString)

    /**
     * This method notifies the end of an entity. The document entity has
     * the pseudo-name of "[xml]"; the DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entity names are
     * just the entity name.
     * <p>
     * <strong>Note:</strong> Since the document is an entity, the handler
     * will be notified of the end of the document entity by calling the
     * endEntity method with the entity name "[xml]" <em>after</em> calling
     * the endDocument method. When exposing entity boundaries through the
     * SAX API, the document entity is never reported, however.
     * <p>
     * <strong>Note:</strong> Since the DTD is an entity, the handler
     * will be notified of the end of the DTD entity by calling the
     * endEntity method with the entity name "[dtd]" <em>after</em> calling
     * the endDTD method.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     * 
     * @param name The name of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws SAXException {

        // call handlers
        if (fInDTD) {
            fDTDGrammar.endEntity(name);
            if (fDTDHandler != null) {
                fDocumentHandler.endEntity(name);
            }
        }
        else {
            if (fDocumentHandler != null) {
                fDocumentHandler.endEntity(name);
            }
        }

    } // endEntity(String)


} // class XMLSchemaValidator
