/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.parsers;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLParser;
import org.apache.xerces.framework.XMLValidator;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.StringPool;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.misc.DeclHandler;
import org.xml.sax.misc.LexicalHandler;
import org.xml.sax.misc.NamespaceHandler;

/**
 * SAXParser provides a parser which implements the SAX1 and SAX2
 * parser APIs
 *
 * @version
 */
public class SAXParser
    extends XMLParser
    implements Parser {

    //
    // Constants
    //

    // private

    /** Features recognized by this parser. */
    private static final String RECOGNIZED_FEATURES[] = {
        // SAX2 core
        "http://xml.org/sax/features/normalize-text",
        "http://xml.org/sax/features/use-locator",
        // Xerces
    };

    /** Properties recognized by this parser. */
    private static final String RECOGNIZED_PROPERTIES[] = {
        // SAX2 core
        "http://xml.org/sax/properties/dom-node",
        "http://xml.org/sax/handlers/DeclHandler",
        "http://xml.org/sax/handlers/LexicalHandler",
        "http://xml.org/sax/handlers/NamespaceHandler",
        // Xerces
    };

    // debugging

    /** Set to true and recompile to debug callbacks. */
    private static final boolean DEBUG_CALLBACKS = false;

    //
    // Data
    //

    // handlers

    /** DTD handler. */
    private DTDHandler fDTDHandler;

    /** Document handler. */
    private DocumentHandler fDocumentHandler;

    /** Decl handler. */
    private DeclHandler fDeclHandler;

    /** Lexical handler. */
    private LexicalHandler fLexicalHandler;

    /** Namespace handler. */
    private NamespaceHandler fNamespaceHandler;

    //
    // Constructors
    //

    /** Default constructor. */
    public SAXParser() {}

    //
    // Public methods
    //

    // features and properties

    /**
     * Returns a list of features that this parser recognizes.
     * This method will never return null; if no features are
     * recognized, this method will return a zero length array.
     *
     * @see #isFeatureRecognized
     * @see #setFeature
     * @see #getFeature
     */
    public String[] getFeaturesRecognized() {

        // get features that super/this recognizes
        String superRecognized[] = super.getFeaturesRecognized();
        String thisRecognized[] = RECOGNIZED_FEATURES;

        // is one or the other the empty set?
        int thisLength = thisRecognized.length;
        if (thisLength == 0) {
            return superRecognized;
        }
        int superLength = superRecognized.length;
        if (superLength == 0) {
            return thisRecognized;
        }

        // combine the two lists and return
        String recognized[] = new String[superLength + thisLength];
        System.arraycopy(superRecognized, 0, recognized, 0, superLength);
        System.arraycopy(thisRecognized, 0, recognized, superLength, thisLength);
        return recognized;

    } // getFeaturesRecognized():String[]

    /**
     * Returns a list of properties that this parser recognizes.
     * This method will never return null; if no properties are
     * recognized, this method will return a zero length array.
     *
     * @see #isPropertyRecognized
     * @see #setProperty
     * @see #getProperty
     */
    public String[] getPropertiesRecognized() {

        // get properties that super/this recognizes
        String superRecognized[] = super.getPropertiesRecognized();
        String thisRecognized[] = RECOGNIZED_PROPERTIES;

        // is one or the other the empty set?
        int thisLength = thisRecognized.length;
        if (thisLength == 0) {
            return superRecognized;
        }
        int superLength = superRecognized.length;
        if (superLength == 0) {
            return thisRecognized;
        }

        // combine the two lists and return
        String recognized[] = new String[superLength + thisLength];
        System.arraycopy(superRecognized, 0, recognized, 0, superLength);
        System.arraycopy(thisRecognized, 0, recognized, superLength, thisLength);
        return recognized;

    }

    //
    // Protected methods
    //

    // SAX2 core features

    /**
     * <b>Note: Currently, the parser does not support this feature.</b>
     * Setting this feature to true will throw a SAXNotSupportedException.
     * <p>
     * Ensures that all consecutive text is returned in a single callback
     * to the DocumentHandler.characters or DocumentHandler.ignorableWhitespace
     * methods.
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/normalize-text
     * <pre>
     *
     * @param normalize True to normalize; false not to normalize.
     *
     * @see #getNormalizeText
     * @see #setFeature
     */
    protected void setNormalizeText(boolean normalize) throws SAXException {
        if (normalize) {
            throw new SAXNotSupportedException("http://xml.org/sax/features/normalize-text");
        }
    }

    /**
     * <b>Note: This feature is always false.</b>
     * <p>
     * Returns true if the parser normalizes all consecutive text into
     * a single callback to the DocumentHandler.characters or
     * DocumentHandler.ignorableWhitespace methods.
     *
     * @see #setNormalizeText
     */
    protected boolean getNormalizeText() throws SAXException {
        return false;
    }

    /**
     * <b>Note: Currently, this parser always sets the locator.</b>
     * Setting this feature to false will throw a SAXNotSupportedException.
     * <p>
     * Provide a Locator using the DocumentHandler.setDocumentLocator
     * callback (true), or explicitly do not provide one (false).
     * <p>
     * This method is the equivalent to the feature:
     * <pre>
     * http://xml.org/sax/features/use-locator
     * </pre>
     *
     * @see #getUseLocator
     * @see #setFeature
     */
    protected void setUseLocator(boolean use) throws SAXException {
        if (!use) {
            throw new SAXNotSupportedException("http://xml.org/sax/features/use-locator");
        }
    }

    /**
     * <b>Note: This feature is always true.</b>
     * <p>
     * Returns true if the locator is always set.
     *
     * @see #setUseLocator
     */
    protected boolean getUseLocator() throws SAXException {
        return true;
    }

    // SAX2 core properties

    /**
     * Set the DTD declaration event handler.
     * <p>
     * This method is the equivalent to the property:
     * <pre>
     * http://xml.org/sax/handlers/DeclHandler
     * </pre>
     *
     * @param handler The new handler.
     *
     * @see #getDeclHandler
     * @see #setProperty
     */
    protected void setDeclHandler(DeclHandler handler) throws SAXException {
        if (fParseInProgress) {
            // REVISIT: Localize this message.
            throw new SAXNotSupportedException("http://xml.org/sax/handlers/DeclHandler: parse is in progress");
        }
        fDeclHandler = handler;
    }

    /**
     * Returns the DTD declaration event handler.
     *
     * @see #setDeclHandler
     */
    protected DeclHandler getDeclHandler() throws SAXException {
        return fDeclHandler;
    }

    /**
     * Set the lexical event handler.
     * <p>
     * This method is the equivalent to the property:
     * <pre>
     * http://xml.org/sax/handlers/LexicalHandler
     * </pre>
     *
     * @param handler lexical event handler
     *
     * @see #getLexicalHandler
     * @see #setProperty
     */
    protected void setLexicalHandler(LexicalHandler handler)
        throws SAXException {
        if (fParseInProgress) {
            // REVISIT: Localize this message.
            throw new SAXNotSupportedException("http://xml.org/sax/handlers/LexicalHandler: parse is in progress");
        }
        fLexicalHandler = handler;
    }

    /**
     * Returns the lexical handler.
     *
     * @see #setLexicalHandler
     */
    protected LexicalHandler getLexicalHandler() throws SAXException {
        return fLexicalHandler;
    }

    /**
     * Set the namespace declaration scope event handler.
     * <p>
     * This method is the equivalent to the property:
     * <pre>
     * http://xml.org/sax/handlers/NamespaceHandler
     * </pre>
     *
     * @param handler namespace event handler
     *
     * @see #getNamespaceHandler
     * @see #setProperty
     */
    protected void setNamespaceHandler(NamespaceHandler handler)
        throws SAXException {
        if (fParseInProgress) {
            // REVISIT: Localize this message.
            throw new SAXNotSupportedException("http://xml.org/sax/handlers/NamespaceHandler: parse is in progress");
        }
        fNamespaceHandler = handler;
    }

    /**
     * Returns the namespace declaration scope event handler.
     *
     * @see #setNamespaceHandler
     */
    protected NamespaceHandler getNamespaceHandler() throws SAXException {
        return fNamespaceHandler;
    }

    //
    // Configurable methods
    //

    /**
     * Set the state of any feature in a SAX2 parser.  The parser
     * might not recognize the feature, and if it does recognize
     * it, it might not be able to fulfill the request.
     *
     * @param featureId The unique identifier (URI) of the feature.
     * @param state The requested state of the feature (true or false).
     *
     * @exception SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception SAXNotSupportedException If the
     *            requested feature is known, but the requested
     *            state is not supported.
     * @exception SAXException If there is any other
     *            problem fulfilling the request.
     */
    public void setFeature(String featureId, boolean state)
        throws SAXException {

        //
        // SAX2 Features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            String feature = featureId.substring(SAX2_FEATURES_PREFIX.length());
            //
            // http://xml.org/sax/features/normalize-text
            //   Ensure that all consecutive text is returned in a single callback to
            //   DocumentHandler.characters or DocumentHandler.ignorableWhitespace
            //   (true) or explicitly do not require it (false).
            //
            if (feature.equals("normalize-text")) {
                setNormalizeText(state);
                return;
            }
            //
            // http://xml.org/sax/features/use-locator
            //   Provide a Locator using the DocumentHandler.setDocumentLocator
            //   callback (true), or explicitly do not provide one (false).
            //
            if (feature.equals("use-locator")) {
                setUseLocator(state);
                return;
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // Xerces Features
        //

        /*
        else if (featureId.startsWith(XERCES_FEATURES_PREFIX)) {
            String feature = featureId.substring(XERCES_FEATURES_PREFIX.length());
            //
            // Drop through and perform default processing
            //
        }
        */

        //
        // Perform default processing
        //

        super.setFeature(featureId, state);

    } // setFeature(String,boolean)

    /**
     * Query the state of a feature.
     *
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     * @return The current state of the feature.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested feature is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     */
    public boolean getFeature(String featureId) throws SAXException {

        //
        // SAX2 Features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            String feature = featureId.substring(SAX2_FEATURES_PREFIX.length());
            //
            // http://xml.org/sax/features/normalize-text
            //   Ensure that all consecutive text is returned in a single callback to
            //   DocumentHandler.characters or DocumentHandler.ignorableWhitespace
            //   (true) or explicitly do not require it (false).
            //
            if (feature.equals("normalize-text")) {
                return getNormalizeText();
            }
            //
            // http://xml.org/sax/features/use-locator
            //   Provide a Locator using the DocumentHandler.setDocumentLocator
            //   callback (true), or explicitly do not provide one (false).
            //
            if (feature.equals("use-locator")) {
                return getUseLocator();
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // Perform default processing
        //

        return super.getFeature(featureId);

    } // getFeature(String):boolean

    /**
     * Set the value of any property in a SAX2 parser.  The parser
     * might not recognize the property, and if it does recognize
     * it, it might not support the requested value.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @param Object The value to which the property is being set.
     *
     * @exception SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception SAXNotSupportedException If the
     *            requested property is known, but the requested
     *            value is not supported.
     * @exception SAXException If there is any other
     *            problem fulfilling the request.
     */
    public void setProperty(String propertyId, Object value)
        throws SAXException {

        //
        // SAX2 core properties
        //

        if (propertyId.startsWith(SAX2_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(SAX2_PROPERTIES_PREFIX.length());
            //
            // http://xml.org/sax/properties/dom-node
            // Value type: DOM Node
            // Access: read-only
            //   Get the DOM node currently being visited, if the SAX parser is
            //   iterating over a DOM tree.  If the parser recognises and supports
            //   this property but is not currently visiting a DOM node, it should
            //   return null (this is a good way to check for availability before the
            //   parse begins).
            //
            if (property.equals("dom-node")) {
                throw new SAXNotSupportedException(propertyId); // read-only property
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // SAX2 core handlers
        //

        else if (propertyId.startsWith(SAX2_HANDLERS_PREFIX)) {
            String property = propertyId.substring(SAX2_HANDLERS_PREFIX.length());
            //
            // http://xml.org/sax/handlers/DeclHandler
            // Value type: org.xml.sax.misc.DeclHandler
            // Access: read/write, pre-parse only
            //   Set the DTD declaration event handler.
            //
            if (property.equals("DeclHandler")) {
                try {
                    setDeclHandler((DeclHandler)value);
                }
                catch (ClassCastException e) {
                    throw new SAXNotSupportedException(propertyId);
                }
                return;
            }
            //
            // http://xml.org/sax/handlers/LexicalHandler
            // Value type: org.xml.sax.misc.LexicalHandler
            // Access: read/write, pre-parse only
            //   Set the lexical event handler.
            //
            if (property.equals("LexicalHandler")) {
                try {
                    setLexicalHandler((LexicalHandler)value);
                }
                catch (ClassCastException e) {
                    throw new SAXNotSupportedException(propertyId);
                }
                return;
            }
            //
            // http://xml.org/sax/handlers/NamespaceHandler
            // Value type: org.xml.sax.misc.NamespaceHandler
            // Access: read/write, pre-parse only
            //   Set the namespace declaration scope event handler.
            //
            if (property.equals("NamespaceHandler")) {
                try {
                    setNamespaceHandler((NamespaceHandler)value);
                }
                catch (ClassCastException e) {
                    throw new SAXNotSupportedException(propertyId);
                }
                return;
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // Xerces Properties
        //

        /*
        else if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            //
            // Drop through and perform default processing
            //
        }
        */

        //
        // Perform default processing
        //

        super.setProperty(propertyId, value);

    } // setProperty(String,Object)

    /**
     * Query the value of a property.
     *
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     * @return The current value of the property.
     * @exception org.xml.sax.SAXNotRecognizedException If the
     *            requested property is not known.
     * @exception org.xml.sax.SAXException If there is any other
     *            problem fulfilling the request.
     * @see org.xml.sax.Configurable#getProperty
     */
    public Object getProperty(String propertyId) throws SAXException {

        //
        // SAX2 core properties
        //

        if (propertyId.startsWith(SAX2_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(SAX2_PROPERTIES_PREFIX.length());
            //
            // http://xml.org/sax/properties/dom-node
            // Value type: DOM Node
            // Access: read-only
            //   Get the DOM node currently being visited, if the SAX parser is
            //   iterating over a DOM tree.  If the parser recognises and supports
            //   this property but is not currently visiting a DOM node, it should
            //   return null (this is a good way to check for availability before the
            //   parse begins).
            //
            if (property.equals("dom-node")) {
                throw new SAXNotSupportedException(propertyId); // we are not iterating a DOM tree
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // SAX2 core handlers
        //

        else if (propertyId.startsWith(SAX2_HANDLERS_PREFIX)) {
            String property = propertyId.substring(SAX2_HANDLERS_PREFIX.length());
            //
            // http://xml.org/sax/handlers/DeclHandler
            // Value type: org.xml.sax.misc.DeclHandler
            // Access: read/write, pre-parse only
            //   Set the DTD declaration event handler.
            //
            if (property.equals("DeclHandler")) {
                return getDeclHandler();
            }
            //
            // http://xml.org/sax/handlers/LexicalHandler
            // Value type: org.xml.sax.misc.LexicalHandler
            // Access: read/write, pre-parse only
            //   Set the lexical event handler.
            //
            if (property.equals("LexicalHandler")) {
                return getLexicalHandler();
            }
            //
            // http://xml.org/sax/handlers/NamespaceHandler
            // Value type: org.xml.sax.misc.NamespaceHandler
            // Access: read/write, pre-parse only
            //   Set the namespace declaration scope event handler.
            //
            if (property.equals("NamespaceHandler")) {
                return getNamespaceHandler();
            }
            //
            // Drop through and perform default processing
            //
        }

        //
        // Xerces properties
        //

        /*
        else if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            //
            // Drop through and perform default processing
            //
        }
        */

        //
        // Perform default processing
        //

        return super.getProperty(propertyId);

    } // getProperty(String):Object

    //
    // Parser methods
    //

    /** Sets the DTD handler. */
    public void setDTDHandler(DTDHandler handler) {
        fDTDHandler = handler;
    }

    /** Sets the document handler. */
    public void setDocumentHandler(DocumentHandler handler) {
        fDocumentHandler = handler;
        setSendCharDataAsCharArray(true);
    }

    //
    // XMLParser methods
    //

    /**
     * This function will be called when a &lt;!DOCTYPE...&gt; declaration is
     * encountered.
     */
    public void startDTD(int rootElementType, int publicId, int systemId) throws Exception {
        if (fLexicalHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(rootElementType);
            String pubid = fStringPool.toString(publicId);
            String sysid = fStringPool.toString(systemId);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("startDTD(" + name + ", " + pubid + ", " + sysid + ")");
            }
            if (fLexicalHandler != null) {
                fLexicalHandler.startDTD(name, pubid, sysid);
            }
        }
    }

    /**
     *  This function will be called at the end of the DTD.
     */
    public void endDTD() throws Exception {
        if (DEBUG_CALLBACKS) {
            System.err.println("endDTD()");
        }
        if (fLexicalHandler != null) {
            fLexicalHandler.endDTD();
        }
    }

    /**
     * Report an element type declaration.
     *
     * The content model will consist of the string "EMPTY", the
     * string "ANY", or a parenthesised group, optionally followed
     * by an occurrence indicator.  The model will be normalized so
     * that all whitespace is removed.
     *
     * @param name The element type name.
     * @param model The content model as a normalized string.
     * @exception SAXException The application may raise an exception.
     */
    public void elementDecl(int elementType, XMLValidator.ContentSpec contentSpec) throws Exception {

        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(elementType);
            String contentModel = contentSpec.toString();

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("elementDecl(" + name + ", " + contentModel + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.elementDecl(name, contentModel);
            }
        }

    }

    /**
     * Report an attribute type declaration.
     *
     * Only the effective (first) declaration for an attribute will
     * be reported.  The type will be one of the strings "CDATA",
     * "ID", "IDREF", "IDREFS", "NMTOKEN", "NMTOKENS", "ENTITY",
     * "ENTITIES", or "NOTATION", or a parenthesized token group with
     * the separator "|" and all whitespace removed.
     *
     * @param eName The name of the associated element.
     * @param aName The name of the attribute.
     * @param type A string representing the attribute type.
     * @param valueDefault A string representing the attribute default
     *        ("#IMPLIED", "#REQUIRED", or "#FIXED") or null if
     *        none of these applies.
     * @param value A string representing the attribute's default value,
     *        or null if there is none.
     * @exception SAXException The application may raise an exception.
     */
    public void attlistDecl(int elementTypeIndex,
                            int attrNameIndex,
                            int attType,
                            String enumString,
                            int attDefaultType,
                            int attDefaultValue) throws Exception
    {
        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String eName = fStringPool.toString(elementTypeIndex);
            String aName = fStringPool.toString(attrNameIndex);
            String aType;
            if (attType == fStringPool.addSymbol("ENUMERATION"))
                aType = enumString;
            else
                aType = fStringPool.toString(attType);
            String aDefaultType;
            if (attDefaultType == StringPool.EMPTY_STRING)
                aDefaultType = null;
            else
                aDefaultType = fStringPool.toString(attDefaultType);
            String aDefaultValue = fStringPool.toString(attDefaultValue);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("attributeDecl(" +
                                    eName + ", " +
                                    aName + ", " +
                                    aType + ", " +
                                    aDefaultType + ", " +
                                    aDefaultValue + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.attributeDecl(eName, aName, aType, aDefaultType, aDefaultValue);
            }
        }
    }

    /**
     * Report an internal parameter entity declaration.
     */
    public void internalPEDecl(int entityName, int entityValue) throws Exception {

        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = "%" + fStringPool.toString(entityName);
            String value = fStringPool.toString(entityValue);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("internalEntityDecl(" + name + ", " + value + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.internalEntityDecl(name, value);
            }
        }

    }

    /**
     * Report a parsed external parameter entity declaration.
     */
    public void externalPEDecl(int entityName, int publicId, int systemId) throws Exception {

        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = "%" + fStringPool.toString(entityName);
            String pubid = fStringPool.toString(publicId);
            String sysid = fStringPool.toString(systemId);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("externalEntityDecl(" + name + ", " + pubid + ", " + sysid + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.externalEntityDecl(name, pubid, sysid);
            }
        }

    }

    /**
     * Report an internal general entity declaration.
     */
    public void internalEntityDecl(int entityName, int entityValue) throws Exception {

        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(entityName);
            String value = fStringPool.toString(entityValue);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("internalEntityDecl(" + name + ", " + value + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.internalEntityDecl(name, value);
            }
        }

    }

    /**
     * Report a parsed external general entity declaration.
     */
    public void externalEntityDecl(int entityName, int publicId, int systemId) throws Exception {

        if (fDeclHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(entityName);
            String pubid = fStringPool.toString(publicId);
            String sysid = fStringPool.toString(systemId);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("externalEntityDecl(" + name + ", " + pubid + ", " + sysid + ")");
            }
            if (fDeclHandler != null) {
                fDeclHandler.externalEntityDecl(name, pubid, sysid);
            }
        }

    }

    /**
     * Receive notification of an unparsed entity declaration event.
     */
    public void unparsedEntityDecl(int entityName, int publicId, int systemId, int notationName) throws Exception {

        if (fDTDHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(entityName);
            String pubid = fStringPool.toString(publicId);
            String sysid = fStringPool.toString(systemId);
            String notation = fStringPool.toString(notationName);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("unparsedEntityDecl(" + name + ", " + pubid + ", " + sysid + ", " + notation + ")");
            }
            if (fDTDHandler != null) {
                fDTDHandler.unparsedEntityDecl(name, pubid, sysid, notation);
            }
        }

    }

    /**
     * Receive notification of a notation declaration event.
     */
    public void notationDecl(int notationName, int publicId, int systemId) throws Exception {

        if (fDTDHandler != null || DEBUG_CALLBACKS) {

            // strings
            String name = fStringPool.toString(notationName);
            String pubid = fStringPool.toString(publicId);
            String sysid = fStringPool.toString(systemId);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("notationDecl(" + name + ", " + pubid + ", " + sysid + ")");
            }
            if (fDTDHandler != null) {
                fDTDHandler.notationDecl(name, pubid, sysid);
            }
        }

    }

    /** Start document. */
    public void startDocument(int versionIndex, int encodingIndex, int standaloneIndex)
        throws Exception {

        // perform callbacks
        if (DEBUG_CALLBACKS) {
            System.err.println("setDocumentLocator(<locator>)");
            String notes = "";
            if (versionIndex != -1)
                notes += " version='" + fStringPool.toString(versionIndex) + "'";
            if (encodingIndex != -1)
                notes += " encoding='" + fStringPool.toString(encodingIndex) + "'";
            if (standaloneIndex != -1)
                notes += " standalone='" + fStringPool.toString(standaloneIndex) + "'";
            System.err.println("startDocument()" + notes);
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.setDocumentLocator(getLocator());
            fDocumentHandler.startDocument();
        }

        // release strings
        fStringPool.releaseString(versionIndex);
        fStringPool.releaseString(encodingIndex);
        fStringPool.releaseString(standaloneIndex);

    } // startDocument(int,int,int)

    /** End document. */
    public void endDocument() throws Exception {

        // perform callback
        if (DEBUG_CALLBACKS) {
            System.err.println("endDocument()");
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument();
        }

    } // endDocument()

    /**
     * Report the start of the scope of a namespace declaration.
     */
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception {

        if (fNamespaceHandler != null || DEBUG_CALLBACKS) {

            // strings
            String p = fStringPool.toString(prefix);
            String ns = fStringPool.toString(uri);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("startNamespaceDeclScope(" + p + ", " + ns + ")");
            }
            if (fNamespaceHandler != null) {
                fNamespaceHandler.startNamespaceDeclScope(p, ns);
            }
        }

    }

    /**
     * Report the end of the scope of a namespace declaration.
     */
    public void endNamespaceDeclScope(int prefix) throws Exception {

        if (fNamespaceHandler != null || DEBUG_CALLBACKS) {

            // strings
            String p = fStringPool.toString(prefix);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("endNamespaceDeclScope(" + p + ")");
            }
            if (fNamespaceHandler != null) {
                fNamespaceHandler.endNamespaceDeclScope(p);
            }
        }

    }

    /** Start element */
    public void startElement(int elementType, XMLAttrList attrList, int attrListIndex)
        throws Exception {

        // parameters
        String name = fStringPool.toString(elementType);
        AttributeList attrs = attrList.getAttributeList(attrListIndex);

        // perform callback
        if (DEBUG_CALLBACKS) {
            String atts = attrs.getLength() > 0 ? "" : " ";
            for (int i = 0; i < attrs.getLength(); i++) {
                atts += " " + attrs.getName(i) + "='" + attrs.getValue(i) + "'";
            }
            System.err.println("startElement(" + name + "," + atts + ")");
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(name, attrs);
        }

        // free attribute list
        attrList.releaseAttrList(attrListIndex);

    } // startElement(int,int)

    /** End element. */
    public void endElement(int elementType) throws Exception {

        // perform callback
        if (DEBUG_CALLBACKS) {
            System.err.println("endElement(" + fStringPool.toString(elementType) + ")");
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.endElement(fStringPool.toString(elementType));
        }

    } // endElement(int)

    /** Start entity reference. */
    public void startEntityReference(int entityName, int entityType, int entityContext) throws Exception {
        if (fLexicalHandler != null || DEBUG_CALLBACKS) {
            switch (entityType) {
            case XMLEntityHandler.ENTITYTYPE_INTERNAL_PE:
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL_PE:
                if (DEBUG_CALLBACKS) {
                    System.err.println("startEntity(%" + fStringPool.toString(entityName) + ")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.startEntity("%" + fStringPool.toString(entityName));
                }
                break;
            case XMLEntityHandler.ENTITYTYPE_INTERNAL:
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL:
                if (DEBUG_CALLBACKS) {
                    System.err.println("startEntity(" + fStringPool.toString(entityName) + ")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.startEntity(fStringPool.toString(entityName));
                }
                break;
            case XMLEntityHandler.ENTITYTYPE_UNPARSED:   // these are mentioned by name, not referenced
                throw new RuntimeException("startEntityReference(): ENTITYTYPE_UNPARSED");
            case XMLEntityHandler.ENTITYTYPE_DOCUMENT:
                break;                  // not reported
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL_SUBSET:
                if (DEBUG_CALLBACKS) {
                    System.err.println("startEntity(\"[dtd]\")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.startEntity("[dtd]");
                }
                break;
            }
        }
    }

    /** End entity reference. */
    public void endEntityReference(int entityName, int entityType, int entityContext) throws Exception {
        if (fLexicalHandler != null || DEBUG_CALLBACKS) {
            switch (entityType) {
            case XMLEntityHandler.ENTITYTYPE_INTERNAL_PE:
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL_PE:
                if (DEBUG_CALLBACKS) {
                    System.err.println("endEntity(%" + fStringPool.toString(entityName) + ")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.endEntity("%" + fStringPool.toString(entityName));
                }
                break;
            case XMLEntityHandler.ENTITYTYPE_INTERNAL:
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL:
                if (DEBUG_CALLBACKS) {
                    System.err.println("endEntity(" + fStringPool.toString(entityName) + ")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.endEntity(fStringPool.toString(entityName));
                }
                break;
            case XMLEntityHandler.ENTITYTYPE_UNPARSED:   // these are mentioned by name, not referenced
                throw new RuntimeException("endEntityReference(): ENTITYTYPE_UNPARSED");
            case XMLEntityHandler.ENTITYTYPE_DOCUMENT:
                break;                  // not reported
            case XMLEntityHandler.ENTITYTYPE_EXTERNAL_SUBSET:
                if (DEBUG_CALLBACKS) {
                    System.err.println("endEntity(\"[dtd]\")");
                }
                if (fLexicalHandler != null) {
                    fLexicalHandler.endEntity("[dtd]");
                }
                break;
            }
        }
    }

    /** Start CDATA section. */
    public void startCDATA() throws Exception {
        if (DEBUG_CALLBACKS) {
            System.err.println("startCDATA()");
        }
        if (fLexicalHandler != null) {
            fLexicalHandler.startCDATA();
        }
    }

    /** End CDATA section. */
    public void endCDATA() throws Exception {
        if (DEBUG_CALLBACKS) {
            System.err.println("endCDATA()");
        }
        if (fLexicalHandler != null) {
            fLexicalHandler.endCDATA();
        }
    }

    /** Not called. */
    public void characters(int dataIndex) throws Exception {
        throw new RuntimeException("cannot happen 5");
    }

    /** Not called. */
    public void ignorableWhitespace(int dataIndex) throws Exception {
        throw new RuntimeException("cannot happen 6");
    }

    /** Processing instruction. */
    public void processingInstruction(int piTarget, int piData) throws Exception {

        if (fDocumentHandler != null || DEBUG_CALLBACKS) {
            //
            // REVISIT - I keep running into SAX apps that expect
            //   null data to be an empty string, which is contrary
            //   to the comment for this method in the SAX API.
            //

            // strings
            String target = fStringPool.orphanString(piTarget);
            String data = piData == -1 ? "" : fStringPool.orphanString(piData);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("processingInstruction(" + target + ", " + data + ")");
            }
            if (fDocumentHandler != null) {
                fDocumentHandler.processingInstruction(target, data);
            }

        } else {
            fStringPool.releaseString(piTarget);
            fStringPool.releaseString(piData);
        }

    }
    public void processingInstructionInDTD(int piTarget, int piData) throws Exception {
        processingInstruction(piTarget, piData);
    }

    /** Comment. */
    public void comment(int dataIndex) throws Exception {

        if (fLexicalHandler != null || DEBUG_CALLBACKS) {

            // strings
            String data = fStringPool.orphanString(dataIndex);

            // perform callback
            if (DEBUG_CALLBACKS) {
                System.err.println("comment(" + data + ")");
            }
            if (fLexicalHandler != null) {
                fLexicalHandler.comment(data.toCharArray(), 0, data.length());
            }
        } else {
            fStringPool.releaseString(dataIndex);
        }
    }
    public void commentInDTD(int dataIndex) throws Exception {
        comment(dataIndex);
    }

    /** Characters. */
    public void characters(char ch[], int start, int length) throws Exception {

        // perform callback
        if (DEBUG_CALLBACKS) {
            System.err.println("characters(<char-data>) length " + length);
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.characters(ch, start, length);
        }

    }

    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length) throws Exception {

        // perform callback
        if (DEBUG_CALLBACKS) {
            System.err.println("ignorableWhitespace(<white-space>)");
        }
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(ch, start, length);
        }

    }

} // class SAXParser
