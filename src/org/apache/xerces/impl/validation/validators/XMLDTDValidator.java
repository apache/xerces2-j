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
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ContentModelValidator;
import org.apache.xerces.impl.validation.DatatypeValidator;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;
import org.apache.xerces.impl.validation.Grammar;
import org.apache.xerces.impl.validation.GrammarPool;
import org.apache.xerces.impl.validation.XMLElementDecl;
import org.apache.xerces.impl.validation.grammars.DTDGrammar;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLComponent;
import org.apache.xerces.xni.XMLComponentManager;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentFilter;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLDTDFilter;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelFilter;
import org.apache.xerces.xni.XMLDTDContentModelHandler;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author Eric Ye, IBM
 *
 * @version $Id$
 */
public class XMLDTDValidator
    extends XMLValidator {

    //
    // Data
    //

    /** Cache all infor regarding the current element */
    private QName fCurrentElement = new QName();
    private int fCurrentElementIndex = -1;
    private int fCurrentContentSpecType = -1;
    private boolean fNamespacesEnabled = false;
    private int fNamespacesPrefix = -1;
    private QName fRootElement = new QName();
    private boolean fSeenDoctypeDecl = false;

    private final int TOP_LEVEL_SCOPE = -1;
    private int fEmptyURI = - 1; 

    /** element stack */
    private int[] fElementIndexStack = new int[8];
    private int[] fContentSpecTypeStack = new int[8];
    private QName[] fElementQNamePartsStack      = new QName[8];

    /** childrent list and offset stack */
    private QName[] fElementChildren = new QName[32];
    private int fElementChildrenLength = 0;
    private int[] fElementChildrenOffsetStack = new int[32];
    private int fElementDepth = -1;

    /** validation states */
    private boolean fStandaloneIsNo = false;
    private boolean fSeenRootElement = false;

    /** temporary variables so that we create less objects */
    private XMLElementDecl fTempElementDecl = new XMLElementDecl();


    //
    // Constructors
    //


    //
    // XMLComponent methods
    //
    
    /*
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     * 
     * @param componentManager The component manager.
     *
     * @throws SAXException Thrown by component on initialization error.
     *                      For example, if a feature or property is
     *                      required for the operation of the component, the
     *                      component manager may throw a 
     *                      SAXNotRecognizedException or a
     *                      SAXNotSupportedException.
     */
    public void reset(XMLComponentManager configurationManager)
        throws SAXException {
        
        super.reset(configurationManager);

        fElementDepth = -1;

    } // reset(XMLComponentManager)


    //
    // XMLDocumentHandler methods
    //

    /**
     * The start of the document.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDocument() throws SAXException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument();
        }

    
    } // startDocument()

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

        if (standalone != null ) 
            if ( standalone.equals("no") ) {
                fStandaloneIsNo = true;
            }

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

        fRootElement.setValues(null, rootElement, rootElement, null);

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
        
        // VC: Root Element Type
        // see if the root element's name matches the one in DoctypeDecl 
        if (!fSeenRootElement) {
            fSeenRootElement = true;
            rootElementSpecified(element);
        }

        // TO DO
        //  0. resolve the element
        //  1. insert default attributes
        //  2. validate the attrivute list.

        // increment the element depth, add this element's 
        // QName to its enclosing element 's children list
        fElementDepth++;
        //if (fElementDepth >= 0) {
        if (fValidation) {
            // push current length onto stack
            if (fElementChildrenOffsetStack.length < fElementDepth) {
                int newarray[] = new int[fElementChildrenOffsetStack.length * 2];
                System.arraycopy(fElementChildrenOffsetStack, 0, newarray, 0, fElementChildrenOffsetStack.length);
                fElementChildrenOffsetStack = newarray;
            }
            fElementChildrenOffsetStack[fElementDepth] = fElementChildrenLength;

            // add this element to children
            if (fElementChildren.length <= fElementChildrenLength) {
                QName[] newarray = new QName[fElementChildrenLength * 2];
                System.arraycopy(fElementChildren, 0, newarray, 0, fElementChildren.length);
                fElementChildren = newarray;
            }
            QName qname = fElementChildren[fElementChildrenLength];
            if (qname == null) {
                for (int i = fElementChildrenLength; i < fElementChildren.length; i++) {
                    fElementChildren[i] = new QName();
                }
                qname = fElementChildren[fElementChildrenLength];
            }
            qname.setValues(element);
            fElementChildrenLength++;

            /***
            if (DEBUG_ELEMENT_CHILDREN) {
                printChildren();
                printStack();
            }
            /***/
        }


        ensureStackCapacity(fElementDepth);

        fCurrentElement.setValues(element);

        fElementQNamePartsStack[fElementDepth].setValues(fCurrentElement); 

        fElementIndexStack[fElementDepth] = fCurrentElementIndex;
        fContentSpecTypeStack[fElementDepth] = fCurrentContentSpecType;

        super.startElement(element, attributes);

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

        String prefixIndex = fCurrentElement.prefix;
        String elementType = fCurrentElement.rawname;

        fElementDepth--;
        if (fValidation) {
            int elementIndex = fCurrentElementIndex;
            if (elementIndex != -1 && fCurrentContentSpecType != -1) {
                QName children[] = fElementChildren;
                int childrenOffset = fElementChildrenOffsetStack[fElementDepth + 1] + 1;
                int childrenLength = fElementChildrenLength - childrenOffset;
                int result = checkContent(elementIndex, 
                                          children, childrenOffset, childrenLength);


                if (result != -1) {
                    // TO DO : fix this
                    /****
                    int majorCode = result != childrenLength ? XMLMessages.MSG_CONTENT_INVALID : XMLMessages.MSG_CONTENT_INCOMPLETE;
                    fGrammar.getElementDecl(elementIndex, fTempElementDecl);
                    if (fTempElementDecl.type == XMLElementDecl.TYPE_EMPTY) {
                        reportRecoverableXMLError(majorCode,
                                                  0,
                                                  fStringPool.toString(elementType),
                                                  "EMPTY");
                    } else
                        reportRecoverableXMLError(majorCode,
                                                  0,
                                                  fStringPool.toString(elementType),
                                                  XMLContentSpec.toString(fGrammar, fStringPool, fTempElementDecl.contentSpecIndex));
                                                  
                    /****/
                }
            }
            fElementChildrenLength = fElementChildrenOffsetStack[fElementDepth + 1] + 1;
        }
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(element);
        }
    
        
        // now pop this element off the top of the element stack
        if (fElementDepth < -1) {
            throw new RuntimeException("FWK008 Element stack underflow");
        }
        if (fElementDepth < 0) {
            fCurrentElement.clear();
            fCurrentElementIndex = -1;
            fCurrentContentSpecType = -1;

            // TO DO : fix this
            //
            // Check after document is fully parsed
            // (1) check that there was an element with a matching id for every
            //   IDREF and IDREFS attr (V_IDREF0)
            //
            /**
            if (fValidating ) {
                try {
                    this.fValIDRef.validate( null, this.fValidateIDRef );   
                    this.fValIDRefs.validate( null, this.fValidateIDRef );
                } catch ( InvalidDatatypeValueException ex ) {
                    reportRecoverableXMLError( ex.getMajorCode(), ex.getMinorCode(), 
                                               ex.getMessage() ); 


                }
            }
            
            try {//Reset datatypes state
               this.fValID.validate( null, this.fResetID );
               this.fValIDRef.validate(null, this.fResetIDRef );
               this.fValIDRefs.validate(null, this.fResetIDRef );
            } catch ( InvalidDatatypeValueException ex ) {
                System.err.println("Error re-Initializing: ID,IDRef,IDRefs pools" );
            }
            ***/
            return;
        }




        if (fNamespaces) { //If Namespace enable then localName != rawName
            fCurrentElement.localpart = fElementQNamePartsStack[fElementDepth].localpart;
        } else {//REVISIT - jeffreyr - This is so we still do old behavior when namespace is off 
            fCurrentElement.localpart = fElementQNamePartsStack[fElementDepth].rawname;
        }
        fCurrentElement.rawname      = fElementQNamePartsStack[fElementDepth].rawname;
        fCurrentElement.uri          = fElementQNamePartsStack[fElementDepth].uri;
        fCurrentElement.prefix       = fElementQNamePartsStack[fElementDepth].prefix;


        fCurrentElementIndex = fElementIndexStack[fElementDepth];
        fCurrentContentSpecType = fContentSpecTypeStack[fElementDepth];
    
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

    //
    // XMLDTDHandler methods
    //

    /**
     * The start of the DTD.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startDTD() throws SAXException {

        // set state
        fInDTD = true;

        // create DTD grammar
        fDTDGrammar = new DTDGrammar();

        // call handlers
        fDTDGrammar.startDTD();
        if (fDTDHandler != null) {
            fDTDHandler.startDTD();
        }

    } // startDTD()

    /**
     * An element declaration.
     * 
     * @param name         The name of the element.
     * @param contentModel The element content model.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void elementDecl(String name, String contentModel)
        throws SAXException {

        // call handlers
        fDTDGrammar.elementDecl(name, contentModel);
        if (fDTDHandler != null) {
            fDTDHandler.elementDecl(name, contentModel);
        }

    } // elementDecl(String,String)

    /**
     * The start of an attribute list.
     * 
     * @param elementName The name of the element that this attribute
     *                    list is associated with.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startAttlist(String elementName) throws SAXException {

        // call handlers
        fDTDGrammar.startAttlist(elementName);
        if (fDTDHandler != null) {
            fDTDHandler.startAttlist(elementName);
        }

    } // startAttlist(String)

    /**
     * An attribute declaration.
     * 
     * @param elementName   The name of the element that this attribute
     *                      is associated with.
     * @param attributeName The name of the attribute.
     * @param type          The attribute type. This value will be one of
     *                      the following: "CDATA", "ENTITY", "ENTITIES",
     *                      "ENUMERATION", "ID", "IDREF", "IDREFS", 
     *                      "NMTOKEN", "NMTOKENS", or "NOTATION".
     * @param enumeration   If the type has the value "ENUMERATION", this
     *                      array holds the allowed attribute values;
     *                      otherwise, this array is null.
     * @param defaultType   The attribute default type. This value will be
     *                      one of the following: "#FIXED", "#IMPLIED",
     *                      "#REQUIRED", or null.
     * @param defaultValue  The attribute default value, or null if no
     *                      default value is specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue)
        throws SAXException {

        // call handlers
        fDTDGrammar.attributeDecl(elementName, attributeName, 
                                  type, enumeration,
                                  defaultType, defaultValue);
        if (fDTDHandler != null) {
            fDTDHandler.attributeDecl(elementName, attributeName, 
                                      type, enumeration, 
                                      defaultType, defaultValue);
        }

    } // attributeDecl(String,String,String,String[],String,XMLString)

    /**
     * The end of an attribute list.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endAttlist() throws SAXException {

        // call handlers
        fDTDGrammar.endAttlist();
        if (fDTDHandler != null) {
            fDTDHandler.endAttlist();
        }

    } // endAttlist()

    /**
     * An internal entity declaration.
     * 
     * @param name The name of the entity. Parameter entity names start with
     *             '%', whereas the name of a general entity is just the 
     *             entity name.
     * @param text The value of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void internalEntityDecl(String name, XMLString text) 
        throws SAXException {

        // call handlers
        fDTDGrammar.internalEntityDecl(name, text);
        if (fDTDHandler != null) {
            fDTDHandler.internalEntityDecl(name, text);
        }

    } // internalEntityDecl(String,XMLString)

    /**
     * An external entity declaration.
     * 
     * @param name     The name of the entity. Parameter entity names start
     *                 with '%', whereas the name of a general entity is just
     *                 the entity name.
     * @param publicId The public identifier of the entity or null if the
     *                 the entity was specified with SYSTEM.
     * @param systemId The system identifier of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void externalEntityDecl(String name, 
                                   String publicId, String systemId) 
        throws SAXException {

        // call handlers
        fDTDGrammar.externalEntityDecl(name, publicId, systemId);
        if (fDTDHandler != null) {
            fDTDHandler.externalEntityDecl(name, publicId, systemId);
        }

    } // externalEntityDecl(String,String,String)

    /**
     * An unparsed entity declaration.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity, or null if not
     *                 specified.
     * @param systemId The system identifier of the entity, or null if not
     *                 specified.
     * @param notation The name of the notation.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void unparsedEntityDecl(String name, 
                                   String publicId, String systemId, 
                                   String notation) throws SAXException {

        // call handlers
        fDTDGrammar.unparsedEntityDecl(name, publicId, systemId, notation);
        if (fDTDHandler != null) {
            fDTDHandler.unparsedEntityDecl(name, publicId, systemId, notation);
        }

    } // unparsedEntityDecl(String,String,String,String)

    /**
     * A notation declaration
     * 
     * @param name     The name of the notation.
     * @param publicId The public identifier of the notation, or null if not
     *                 specified.
     * @param systemId The system identifier of the notation, or null if not
     *                 specified.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void notationDecl(String name, String publicId, String systemId)
        throws SAXException {

        // call handlers
        fDTDGrammar.notationDecl(name, publicId, systemId);
        if (fDTDHandler != null) {
            fDTDHandler.notationDecl(name, publicId, systemId);
        }

    } // notationDecl(String,String,String)

    /**
     * The start of a conditional section.
     * 
     * @param type The type of the conditional section. This value will
     *             either be CONDITIONAL_INCLUDE or CONDITIONAL_IGNORE.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see CONDITIONAL_INCLUDE
     * @see CONDITIONAL_IGNORE
     */
    public void startConditional(short type) throws SAXException {

        // set state
        fInDTDIgnore = type == XMLDTDHandler.CONDITIONAL_IGNORE;

        // call handlers
        fDTDGrammar.startConditional(type);
        if (fDTDHandler != null) {
            fDTDHandler.startConditional(type);
        }

    } // startConditional(short)

    /**
     * The end of a conditional section.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endConditional() throws SAXException {

        // set state
        fInDTDIgnore = false;

        // call handlers
        fDTDGrammar.endConditional();
        if (fDTDHandler != null) {
            fDTDHandler.endConditional();
        }

    } // endConditional()

    /**
     * The end of the DTD.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endDTD() throws SAXException {

        // set state
        fInDTD = false;

        // save grammar
        fDTDGrammar.endDTD();
        fCurrentGrammar = fDTDGrammar;
        fDTDGrammar = null;

        // call handlers
        if (fDTDHandler != null) {
            fDTDHandler.endDTD();
        }

    } // endDTD()

    //
    // XMLDTDContentModelHandler methods
    //

    /**
     * The start of a content model. Depending on the type of the content
     * model, specific methods may be called between the call to the
     * startContentModel method and the call to the endContentModel method.
     * 
     * @param elementName The name of the element.
     * @param type        The content model type.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_EMPTY
     * @see TYPE_ANY
     * @see TYPE_MIXED
     * @see TYPE_CHILDREN
     */
    public void startContentModel(String elementName, short type)
        throws SAXException {

        // call handlers
        fDTDGrammar.startContentModel(elementName, type);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.startContentModel(elementName, type);
        }

    } // startContentModel(String,short)

    /**
     * A referenced element in a mixed content model. If the mixed content 
     * model only allows text content, then this method will not be called
     * for that model. However, if this method is called for a mixed
     * content model, then the zero or more occurrence count is implied.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to 
     * the startContentModel method where the type is TYPE_MIXED.
     * 
     * @param elementName The name of the referenced element. 
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_MIXED
     */
    public void mixedElement(String elementName) throws SAXException {

        // call handlers
        fDTDGrammar.mixedElement(elementName);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.mixedElement(elementName);
        }

    } // mixedElement(elementName)

    /**
     * The start of a children group.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * <p>
     * <strong>Note:</strong> Children groups can be nested and have
     * associated occurrence counts.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenStartGroup() throws SAXException {

        // call handlers
        fDTDGrammar.childrenStartGroup();
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.childrenStartGroup();
        }

    } // childrenStartGroup()

    /**
     * A referenced element in a children content model.
     * 
     * @param elementName The name of the referenced element.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenElement(String elementName) throws SAXException {

        // call handlers
        fDTDGrammar.childrenElement(elementName);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.childrenElement(elementName);
        }

    } // childrenElement(String)

    /**
     * The separator between choices or sequences of a children content
     * model.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * 
     * @param separator The type of children separator.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see SEPARATOR_CHOICE
     * @see SEPARATOR_SEQUENCE
     * @see TYPE_CHILDREN
     */
    public void childrenSeparator(short separator) throws SAXException {

        // call handlers
        fDTDGrammar.childrenSeparator(separator);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.childrenSeparator(separator);
        }

    } // childrenSeparator(short)

    /**
     * The occurrence count for a child in a children content model.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     * 
     * @param occurrence The occurrence count for the last children element
     *                   or children group.
     *
     * @throws SAXException Thrown by handler to signal an error.
     *
     * @see OCCURS_ZERO_OR_ONE
     * @see OCCURS_ZERO_OR_MORE
     * @see OCCURS_ONE_OR_MORE
     * @see TYPE_CHILDREN
     */
    public void childrenOccurrence(short occurrence) throws SAXException {

        // call handlers
        fDTDGrammar.childrenOccurrence(occurrence);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.childrenOccurrence(occurrence);
        }

    } // childrenOccurrence(short)

    /**
     * The end of a children group.
     * <p>
     * <strong>Note:</strong> This method is only called after a call to
     * the startContentModel method where the type is TYPE_CHILDREN.
     *
     * @see TYPE_CHILDREN
     */
    public void childrenEndGroup() throws SAXException {

        // call handlers
        fDTDGrammar.childrenEndGroup();
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.childrenEndGroup();
        }

    } // childrenEndGroup()

    /**
     * The end of a content model.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endContentModel() throws SAXException {

        // call handlers
        fDTDGrammar.endContentModel();
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.endContentModel();
        }

    } // endContentModel()


    //private methods
    
    /** Root element specified. */
    private void rootElementSpecified(QName rootElement) throws SAXException {
        if (fValidation) {
            String root1 = fRootElement.rawname;
            String root2 = rootElement.rawname;
            if ( root1 == null || !root1.equals(root2)) {
                fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, 
                                            "RootElementTypeMustMatchDoctypedecl", 
                                            new Object[]{root1, root2}, 
                                            XMLErrorReporter.SEVERITY_ERROR);
            }
        }
    } // rootElementSpecified(QName)


    /**
     * Check that the content of an element is valid.
     * <p>
     * This is the method of primary concern to the validator. This method is called
     * upon the scanner reaching the end tag of an element. At that time, the
     * element's children must be structurally validated, so it calls this method.
     * The index of the element being checked (in the decl pool), is provided as
     * well as an array of element name indexes of the children. The validator must
     * confirm that this element can have these children in this order.
     * <p>
     * This can also be called to do 'what if' testing of content models just to see
     * if they would be valid.
     * <p>
     * Note that the element index is an index into the element decl pool, whereas
     * the children indexes are name indexes, i.e. into the string pool.
     * <p>
     * A value of -1 in the children array indicates a PCDATA node. All other
     * indexes will be positive and represent child elements. The count can be
     * zero, since some elements have the EMPTY content model and that must be
     * confirmed.
     *
     * @param elementIndex The index within the <code>ElementDeclPool</code> of this
     *                     element.
     * @param childCount The number of entries in the <code>children</code> array.
     * @param children The children of this element.  
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then additional content is required to reach a valid
     *         ending state.
     *
     * @exception Exception Thrown on error.
     */
    private int checkContent(int elementIndex, 
                             QName[] children,
                             int childOffset, 
                             int childCount) throws SAXException {

        fCurrentGrammar.getElementDecl(elementIndex, fTempElementDecl);

        // Get the element name index from the element
        final String elementType = fCurrentElement.rawname;

        // Get out the content spec for this element
        final int contentType = fCurrentContentSpecType;


        //
        //  Deal with the possible types of content. We try to optimized here
        //  by dealing specially with content models that don't require the
        //  full DFA treatment.
        //
        if (contentType == XMLElementDecl.TYPE_EMPTY) {
            //
            //  If the child count is greater than zero, then this is
            //  an error right off the bat at index 0.
            //
            if (childCount != 0) {
                return 0;
            }
        } else if (contentType == XMLElementDecl.TYPE_ANY) {
            //
            //  This one is open game so we don't pass any judgement on it
            //  at all. Its assumed to fine since it can hold anything.
            //
        } else if (contentType == XMLElementDecl.TYPE_MIXED ||  
                   contentType == XMLElementDecl.TYPE_CHILDREN) {
            // Get the content model for this element, faulting it in if needed
            ContentModelValidator cmElem = null;
            cmElem = fTempElementDecl.contentModelValidator;
            int result = cmElem.validate(children, childOffset, childCount);
            return result;
        } else if (contentType == -1) {
            /****
            reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_NOT_DECLARED,
                                      XMLMessages.VC_ELEMENT_VALID,
                                      elementType);
            /****/
        } else if (contentType == XMLElementDecl.TYPE_SIMPLE ) {

            //REVISIT
            // this should never be reached in the case of DTD validation.

        } else {
            /****
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       ImplementationMessages.VAL_CST,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            /****/                           
        }

        // We succeeded
        return -1;

    } // checkContent(int,int,QName[]):int


    /** ensure element stack capacity */
    private void ensureStackCapacity ( int newElementDepth) {

        if (newElementDepth == fElementQNamePartsStack.length ) {
            int[] newStack = new int[newElementDepth * 2];

            QName[] newStackOfQueue = new QName[newElementDepth * 2];
            System.arraycopy(this.fElementQNamePartsStack, 0, newStackOfQueue, 0, newElementDepth );
            fElementQNamePartsStack      = newStackOfQueue;

            QName qname = fElementQNamePartsStack[newElementDepth];
            if (qname == null) {
                for (int i = newElementDepth; i < fElementQNamePartsStack.length; i++) {
                    fElementQNamePartsStack[i] = new QName();
                }
            }

            newStack = new int[newElementDepth * 2];
            System.arraycopy(fElementIndexStack, 0, newStack, 0, newElementDepth);
            fElementIndexStack = newStack;

            newStack = new int[newElementDepth * 2];
            System.arraycopy(fContentSpecTypeStack, 0, newStack, 0, newElementDepth);
            fContentSpecTypeStack = newStack;

        }
    } // ensureStackCapacity


} // class XMLDTDValidator
