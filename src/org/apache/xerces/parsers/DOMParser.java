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

package org.apache.xerces.parsers;

import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLDocumentHandler;
import org.apache.xerces.framework.XMLParser;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.schema.XUtil;

import org.apache.xerces.dom.DeferredDocumentImpl;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.dom.EntityImpl;
import org.apache.xerces.dom.NotationImpl;
import org.apache.xerces.dom.ElementDefinitionImpl;
import org.apache.xerces.dom.AttrImpl;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.dom.ElementImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Entity;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * DOMParser provides a parser which produces a W3C DOM tree as its output
 *
 * @version $Id$
 */
public class DOMParser
    extends XMLParser
    implements XMLDocumentHandler, XMLDocumentHandler.DTDHandler
    {

    //
    // Constants
    //

    // public

    /** Default programmatic document class name (org.apache.xerces.dom.DocumentImpl). */
    public static final String DEFAULT_DOCUMENT_CLASS_NAME = "org.apache.xerces.dom.DocumentImpl";

    // debugging

    /** Set to true to debug attribute list declaration calls. */
    private static final boolean DEBUG_ATTLIST_DECL = false;

    // features and properties

    /** Features recognized by this parser. */
    private static final String RECOGNIZED_FEATURES[] = {
        // SAX2 core features
        // Xerces features
        "http://apache.org/xml/features/dom/defer-node-expansion",
        "http://apache.org/xml/features/dom/create-entity-ref-nodes",
        "http://apache.org/xml/features/dom/include-ignorable-whitespace",
        // Experimental features
        "http://apache.org/xml/features/domx/grammar-access",
    };

    /** Properties recognized by this parser. */
    private static final String RECOGNIZED_PROPERTIES[] = {
        // SAX2 core properties
        // Xerces properties
        "http://apache.org/xml/properties/dom/document-class-name",
        "http://apache.org/xml/properties/dom/current-element-node",
    };

    /** For experimental grammar access. */
    private static final Hashtable TYPES = new Hashtable();

    //
    // Data
    //

    // common data

    protected Document fDocument;

    // deferred expansion data

    protected DeferredDocumentImpl fDeferredDocumentImpl;
    protected int                  fDocumentIndex;
    protected int                  fDocumentTypeIndex;
    protected int                  fCurrentNodeIndex;

    // full expansion data

    protected DocumentImpl fDocumentImpl;
    protected DocumentType fDocumentType;
    protected Node         fCurrentElementNode;

    // state

    protected boolean fWithinElement;
    protected boolean fInCDATA;

    // features
    private boolean fGrammarAccess;

    // properties

    // REVISIT: Even though these have setters and getters, should they
    //          be protected visibility? -Ac
    private String  fDocumentClassName;
    private boolean fDeferNodeExpansion;
    private boolean fCreateEntityReferenceNodes;
    private boolean fIncludeIgnorableWhitespace;

    // built-in entities

    protected int fAmpIndex;
    protected int fLtIndex;
    protected int fGtIndex;
    protected int fAposIndex;
    protected int fQuotIndex;

    private boolean fSeenRootElement;

    private XMLAttrList fAttrList;

    //
    // Static initializer
    //

    static {
        String types[][] = {
            { "CDATA",       "minOccurs", "maxOccurs" },
            { "ENUMERATION", "collection", "order", "export" },
            { "NMTOKEN",     "name", "ref" },
        };
        for (int i = 0; i < types.length; i++) {
            String typeName = types[i][0];
            for (int j = 1; j < types[i].length; j++) {
                TYPES.put(types[i][j], typeName);
            }
        }
    }

    //
    // Constructors
    //

    /** Default constructor. */
    public DOMParser() {

        initHandlers(false, this, this);

        // setup parser state
        init();

        // set default values
        try {
            setDocumentClassName(DEFAULT_DOCUMENT_CLASS_NAME);
            setCreateEntityReferenceNodes(true);
            setDeferNodeExpansion(true);
            setIncludeIgnorableWhitespace(true);
        } catch (SAXException e) {
            throw new RuntimeException("PAR001 Fatal error constructing DOMParser.");
        }

    } // <init>()

    //
    // Public methods
    //

    // document

    /** Returns the document. */
    public Document getDocument() {
        if (fDocumentImpl != null) {
            fDocumentImpl.setErrorChecking(true);
        }
        return fDocument;
    }

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

    // resetting

    /** Resets the parser. */
    public void reset() throws Exception {
        super.reset();
        init();
    }

    /** Resets or copies the parser. */
    public void resetOrCopy() throws Exception {
        super.resetOrCopy();
        init();
    }

    //
    // Protected methods
    //

    // initialization

    /**
     * Initializes the parser to a pre-parse state. This method is
     * called between calls to <code>parse()</code>.
     */
    protected void init() {

        // init common
        fDocument = null;

        // init deferred expansion
        fDeferredDocumentImpl = null;
        fDocumentIndex = -1;
        fDocumentTypeIndex = -1;
        fCurrentNodeIndex = -1;

        // init full expansion
        fDocumentImpl = null;
        fDocumentType = null;
        fCurrentElementNode = null;

        // state
        fWithinElement = false;
        fInCDATA = false;

        // built-in entities
        fAmpIndex = fStringPool.addSymbol("amp");
        fLtIndex = fStringPool.addSymbol("lt");
        fGtIndex = fStringPool.addSymbol("gt");
        fAposIndex = fStringPool.addSymbol("apos");
        fQuotIndex = fStringPool.addSymbol("quot");

        fSeenRootElement = false;

        fAttrList = new XMLAttrList(fStringPool);

    } // init()

    // features

    /**
     * This method sets whether the expansion of the nodes in the default
     * DOM implementation are deferred.
     *
     * @see #getDeferNodeExpansion
     * @see #setDocumentClassName
     */
    protected void setDeferNodeExpansion(boolean deferNodeExpansion) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fDeferNodeExpansion = deferNodeExpansion;
    }

    /**
     * Returns true if the expansion of the nodes in the default DOM
     * implementation are deferred.
     *
     * @see #setDeferNodeExpansion
     */
    protected boolean getDeferNodeExpansion() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fDeferNodeExpansion;
    }

    /**
     * This feature determines whether entity references within
     * the document are included in the document tree as
     * EntityReference nodes.
     * <p>
     * Note: The children of the entity reference are always
     * added to the document. This feature only affects
     * whether an EntityReference node is also included
     * as the parent of the entity reference children.
     *
     * @param create True to create entity reference nodes; false
     *               to only insert the entity reference children.
     *
     * @see #getCreateEntityReferenceNodes
     */
    protected void setCreateEntityReferenceNodes(boolean create) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fCreateEntityReferenceNodes = create;
    }

    /**
     * Returns true if entity references within the document are
     * included in the document tree as EntityReference nodes.
     *
     * @see #setCreateEntityReferenceNodes
     */
    public boolean getCreateEntityReferenceNodes() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fCreateEntityReferenceNodes;
    }

    /**
     * This feature determines whether text nodes that can be
     * considered "ignorable whitespace" are included in the DOM
     * tree.
     * <p>
     * Note: The only way that the parser can determine if text
     * is ignorable is by reading the associated grammar
     * and having a content model for the document. When
     * ignorable whitespace text nodes *are* included in
     * the DOM tree, they will be flagged as ignorable.
     * The ignorable flag can be queried by calling the
     * TextImpl#isIgnorableWhitespace():boolean method.
     *
     * @param include True to include ignorable whitespace text nodes;
     *                false to not include ignorable whitespace text
     *                nodes.
     *
     * @see #getIncludeIgnorableWhitespace
     */
    public void setIncludeIgnorableWhitespace(boolean include) 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        fIncludeIgnorableWhitespace = include;
    }

    /**
     * Returns true if ignorable whitespace text nodes are included
     * in the DOM tree.
     *
     * @see #setIncludeIgnorableWhitespace
     */
    public boolean getIncludeIgnorableWhitespace() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fIncludeIgnorableWhitespace;
    }
    
    // properties

    /**
     * This method allows the programmer to decide which document
     * factory to use when constructing the DOM tree. However, doing
     * so will lose the functionality of the default factory. Also,
     * a document class other than the default will lose the ability
     * to defer node expansion on the DOM tree produced.
     *
     * @param documentClassName The fully qualified class name of the
     *                      document factory to use when constructing
     *                      the DOM tree.
     *
     * @see #getDocumentClassName
     * @see #setDeferNodeExpansion
     * @see #DEFAULT_DOCUMENT_CLASS_NAME
     */
    protected void setDocumentClassName(String documentClassName) 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        // normalize class name
        if (documentClassName == null) {
            documentClassName = DEFAULT_DOCUMENT_CLASS_NAME;
        }

        // verify that this class exists and is of the right type
        try {
            Class _class = Class.forName(documentClassName);
            //if (!_class.isAssignableFrom(Document.class)) {
            if (!Document.class.isAssignableFrom(_class)) {
                throw new IllegalArgumentException("PAR002 Class, \""+documentClassName+"\", is not of type org.w3c.dom.Document."+"\n"+documentClassName);
            }
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("PAR003 Class, \""+documentClassName+"\", not found."+"\n"+documentClassName);
        }

        // set document class name
        fDocumentClassName = documentClassName;
        if (!documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
            setDeferNodeExpansion(false);
        }

    } // setDocumentClassName(String)

    /**
     * Returns the fully qualified class name of the document factory
     * used when constructing the DOM tree.
     *
     * @see #setDocumentClassName
     */
    protected String getDocumentClassName() 
        throws SAXNotRecognizedException, SAXNotSupportedException {
        return fDocumentClassName;
    }

    /**
     * Returns the current element node.
     * <p>
     * Note: This method is not supported when the "deferNodeExpansion"
     *       property is set to true and the document factory is set to
     *       the default factory.
     */
    protected Element getCurrentElementNode() 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        if (fCurrentElementNode != null &&
            fCurrentElementNode.getNodeType() == Node.ELEMENT_NODE) {
            return (Element)fCurrentElementNode;
        }
        return null;

    } // getCurrentElementNode():Element

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
     * @exception SAXNotRecognizedException If the requested feature is
     *                                      not known.
     * @exception SAXNotSupportedException If the requested feature is
     *                                     known, but the requested state
     *                                     is not supported.
     */
    public void setFeature(String featureId, boolean state)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 core features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            //
            // No additional SAX properties defined for DOMParser.
            // Pass request off to XMLParser for the common cases.
            //
        }

        //
        // Xerces features
        //

        else if (featureId.startsWith(XERCES_FEATURES_PREFIX)) {
            String feature = featureId.substring(XERCES_FEATURES_PREFIX.length());
            //
            // http://apache.org/xml/features/dom/defer-node-expansion
            //   Allows the document tree returned by getDocument()
            //   to be constructed lazily. In other words, the DOM
            //   nodes are constructed as the tree is traversed.
            //   This allows the document to be returned sooner with
            //   the expense of holding all of the blocks of character
            //   data held in memory. Then again, lots of DOM nodes
            //   use a lot of memory as well.
            //
            if (feature.equals("dom/defer-node-expansion")) {
                if (fParseInProgress) {
                    throw new SAXNotSupportedException("PAR004 Cannot setFeature("+featureId + "): parse is in progress."+"\n"+featureId);
                }
                setDeferNodeExpansion(state);
                return;
            }
            //
            // http://apache.org/xml/features/dom/create-entity-ref-nodes
            //   This feature determines whether entity references within
            //   the document are included in the document tree as
            //   EntityReference nodes.
            //   Note: The children of the entity reference are always
            //         added to the document. This feature only affects
            //         whether an EntityReference node is also included
            //         as the parent of the entity reference children.
            //
            if (feature.equals("dom/create-entity-ref-nodes")) {
                setCreateEntityReferenceNodes(state);
                return;
            }

            //
            // http://apache.org/xml/features/dom/include-ignorable-whitespace
            //   This feature determines whether text nodes that can be
            //   considered "ignorable whitespace" are included in the DOM
            //   tree.
            //   Note: The only way that the parser can determine if text
            //         is ignorable is by reading the associated grammar
            //         and having a content model for the document. When
            //         ignorable whitespace text nodes *are* included in
            //         the DOM tree, they will be flagged as ignorable.
            //         The ignorable flag can be queried by calling the
            //         TextImpl#isIgnorableWhitespace():boolean method.
            //
            if (feature.equals("dom/include-ignorable-whitespace")) {
                setIncludeIgnorableWhitespace(state);
                return;
            }
            
            //
            // Experimental features
            //

            //
            // http://apache.org/xml/features/domx/grammar-access
            //   Allows grammar access in the DOM tree. Currently, this
            //   means that there is an XML Schema document tree as a
            //   child of the Doctype node.
            //
            if (feature.equals("domx/grammar-access")) {
                fGrammarAccess = state;
                return;
            }

            //
            // Pass request off to XMLParser for the common cases.
            //
        }

        //
        // Pass request off to XMLParser for the common cases.
        //
        super.setFeature(featureId, state);

    } // setFeature(String,boolean)

    /**
     * Query the current state of any feature in a SAX2 parser.  The
     * parser might not recognize the feature.
     *
     * @param featureId The unique identifier (URI) of the feature
     *                  being set.
     *
     * @return The current state of the feature.
     *
     * @exception SAXNotRecognizedException If the requested feature is
     *                                      not known.
     */
    public boolean getFeature(String featureId) 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // SAX2 core features
        //

        if (featureId.startsWith(SAX2_FEATURES_PREFIX)) {
            //
            // No additional SAX properties defined for DOMParser.
            // Pass request off to XMLParser for the common cases.
            //
        }

        //
        // Xerces features
        //

        else if (featureId.startsWith(XERCES_FEATURES_PREFIX)) {
            String feature = featureId.substring(XERCES_FEATURES_PREFIX.length());
            //
            // http://apache.org/xml/features/dom/defer-node-expansion
            //   Allows the document tree returned by getDocument()
            //   to be constructed lazily. In other words, the DOM
            //   nodes are constructed as the tree is traversed.
            //   This allows the document to be returned sooner with
            //   the expense of holding all of the blocks of character
            //   data held in memory. Then again, lots of DOM nodes
            //   use a lot of memory as well.
            //
            if (feature.equals("dom/defer-node-expansion")) {
                return getDeferNodeExpansion();
            }
            //
            // http://apache.org/xml/features/dom/create-entity-ref-nodes
            //   This feature determines whether entity references within
            //   the document are included in the document tree as
            //   EntityReference nodes.
            //   Note: The children of the entity reference are always
            //         added to the document. This feature only affects
            //         whether an EntityReference node is also included
            //         as the parent of the entity reference children.
            //
            else if (feature.equals("dom/create-entity-ref-nodes")) {
                return getCreateEntityReferenceNodes();
            }

            //
            // http://apache.org/xml/features/dom/include-ignorable-whitespace
            //   This feature determines whether text nodes that can be
            //   considered "ignorable whitespace" are included in the DOM
            //   tree.
            //   Note: The only way that the parser can determine if text
            //         is ignorable is by reading the associated grammar
            //         and having a content model for the document. When
            //         ignorable whitespace text nodes *are* included in
            //         the DOM tree, they will be flagged as ignorable.
            //         The ignorable flag can be queried by calling the
            //         TextImpl#isIgnorableWhitespace():boolean method.
            //
            if (feature.equals("dom/include-ignorable-whitespace")) {
                return getIncludeIgnorableWhitespace();
            }

            //
            // Experimental features
            //

            //
            // http://apache.org/xml/features/domx/grammar-access
            //   Allows grammar access in the DOM tree. Currently, this
            //   means that there is an XML Schema document tree as a
            //   child of the Doctype node.
            //
            if (feature.equals("domx/grammar-access")) {
                return fGrammarAccess;
            }

            //
            // Pass request off to XMLParser for the common cases.
            //
        }

        //
        // Pass request off to XMLParser for the common cases.
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
     * @exception SAXNotRecognizedException If the requested property is
     *                                      not known.
     * @exception SAXNotSupportedException If the requested property is
     *                                     known, but the requested
     *                                     value is not supported.
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // Xerces properties
        //

        if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(XERCES_PROPERTIES_PREFIX.length());
            //
            // http://apache.org/xml/properties/dom/current-element-node
            //   Returns the current element node as the DOM Parser is
            //   parsing. This property is useful for determining the
            //   relative location of the document when an error is
            //   encountered. Note that this feature does *not* work
            //   when the http://apache.org/xml/features/dom/defer-node-expansion
            //   is set to true.
            //
            if (property.equals("dom/current-element-node")) {
                throw new SAXNotSupportedException("PAR005 Property, \""+propertyId+"\" is read-only.\n"+propertyId);
            }
            //
            // http://apache.org/xml/properties/dom/document-class-name
            //   This property can be used to set/query the name of the
            //   document factory.
            //
            else if (property.equals("dom/document-class-name")) {
                if (value != null && !(value instanceof String)) {
                    throw new SAXNotSupportedException("PAR006 Property value must be of type java.lang.String.");
                }
                setDocumentClassName((String)value);
                return;
            }
        }

        //
        // Pass request off to XMLParser for the common cases.
        //
        super.setProperty(propertyId, value);

    } // setProperty(String,Object)

    /**
     * Return the current value of a property in a SAX2 parser.
     * The parser might not recognize the property.
     *
     * @param propertyId The unique identifier (URI) of the property
     *                   being set.
     *
     * @return The current value of the property.
     *
     * @exception SAXNotRecognizedException If the requested property is
     *                                      not known.
     *
     * @see Configurable#getProperty
     */
    public Object getProperty(String propertyId) 
        throws SAXNotRecognizedException, SAXNotSupportedException {

        //
        // Xerces properties
        //

        if (propertyId.startsWith(XERCES_PROPERTIES_PREFIX)) {
            String property = propertyId.substring(XERCES_PROPERTIES_PREFIX.length());
            //
            // http://apache.org/xml/properties/dom/current-element-node
            //   Returns the current element node as the DOM Parser is
            //   parsing. This property is useful for determining the
            //   relative location of the document when an error is
            //   encountered. Note that this feature does *not* work
            //   when the http://apache.org/xml/features/dom/defer-node-expansion
            //   is set to true.
            //
            if (property.equals("dom/current-element-node")) {
                boolean throwException = false;
                try {
                    throwException = getFeature(XERCES_FEATURES_PREFIX+"dom/defer-node-expansion");
                }
                catch (SAXNotSupportedException e) {
                    // ignore
                }
                catch (SAXNotRecognizedException e) {
                    // ignore
                }
                if (throwException) {
                    throw new SAXNotSupportedException("PAR007 Current element node cannot be queried when node expansion is deferred.");
                }
                return getCurrentElementNode();
            }
            //
            // http://apache.org/xml/properties/dom/document-class-name
            //   This property can be used to set/query the name of the
            //   document factory.
            //
            else if (property.equals("dom/document-class-name")) {
                return getDocumentClassName();
            }
        }

        //
        // Pass request off to XMLParser for the common cases.
        //
        return super.getProperty(propertyId);

    } // getProperty(String):Object

    //
    // XMLParser methods
    //

    /** Start document. */
    public void startDocument() {

        // deferred expansion
        String documentClassName = null;
        try {
            documentClassName = getDocumentClassName();
        } catch (SAXException e) {
            throw new RuntimeException("PAR008 Fatal error getting document factory.");
        }
        boolean deferNodeExpansion = true;
        try {
            deferNodeExpansion = getDeferNodeExpansion();
        } catch (SAXException e) {
            throw new RuntimeException("PAR009 Fatal error reading expansion mode.");
        }
        if (documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME) && deferNodeExpansion) {
            boolean nsEnabled = false;
            try { nsEnabled = getNamespaces(); }
            catch (SAXException s) {}
            fDocument = fDeferredDocumentImpl =
                new DeferredDocumentImpl(fStringPool, nsEnabled, fGrammarAccess);
            fDocumentIndex = fDeferredDocumentImpl.createDocument();
            fCurrentNodeIndex = fDocumentIndex;
        }

        // full expansion
        else {

            if (documentClassName.equals(DEFAULT_DOCUMENT_CLASS_NAME)) {
                fDocument = fDocumentImpl = new DocumentImpl(fGrammarAccess);
                fDocumentImpl.setErrorChecking(false);
            }
            else {
                try {
                    Class documentClass = Class.forName(documentClassName);
                    fDocument = (Document)documentClass.newInstance();
                }
                catch (Exception e) {
                    // REVISIT: We've already checked the type of the factory
                    //          in the setDocumentClassName() method. The only
                    //          exception that can occur here is if the class
                    //          doesn't have a zero-arg constructor. -Ac
                }
            }

            fCurrentElementNode = fDocument;
        }

    } // startDocument()

    /** End document. */
    public void endDocument() throws Exception {}

    /** XML declaration. */
    public void xmlDecl(int versionIndex, int encodingIndex, int standaloneIndex) throws Exception {

        // release strings
        fStringPool.releaseString(versionIndex);
        fStringPool.releaseString(encodingIndex);
        fStringPool.releaseString(standaloneIndex);
    }

    /** Text declaration. */
    public void textDecl(int versionIndex, int encodingIndex) throws Exception {

        // release strings
        fStringPool.releaseString(versionIndex);
        fStringPool.releaseString(encodingIndex);
    }

    /** Report the start of the scope of a namespace declaration. */
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception {}

    /** Report the end of the scope of a namespace declaration. */
    public void endNamespaceDeclScope(int prefix) throws Exception {}
    
    

    /** Start element. */
    public void startElement(int elementTypeIndex,
                             XMLAttrList xmlAttrList, int attrListIndex)
        throws Exception {

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            int element = fDeferredDocumentImpl.createElement(elementTypeIndex, xmlAttrList, attrListIndex);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, element);
            fCurrentNodeIndex = element;
            fWithinElement = true;

            // identifier registration
            int index = xmlAttrList.getFirstAttr(attrListIndex);
            while (index != -1) {
                if (xmlAttrList.getAttType(index) == fStringPool.addSymbol("ID")) {
                    int nameIndex = xmlAttrList.getAttValue(index);
                    fDeferredDocumentImpl.putIdentifier(nameIndex, element);
                }
                index = xmlAttrList.getNextAttr(index);
            }

            // copy schema grammar, if needed
            if (!fSeenRootElement) {
                fSeenRootElement = true;
                if (fGrammarAccess) {
                    Document schemaDocument = fValidator.getSchemaDocument();
                    if (schemaDocument != null) {
                        if (fDocumentTypeIndex == -1) {
                            fDocumentTypeIndex = fDeferredDocumentImpl.createDocumentType(elementTypeIndex, -1, -1);
                            fDeferredDocumentImpl.appendChild(0, fDocumentTypeIndex);
                        }

                        Element schema = schemaDocument.getDocumentElement();
                        copyInto(schema, fDocumentTypeIndex);
                    }
                }
            }
        }

        // full expansion
        else {

            boolean nsEnabled = false;
            try { nsEnabled = getNamespaces(); }
            catch (SAXException s) {}

            String elementName = fStringPool.toString(elementTypeIndex);
            Element e;
            if (nsEnabled) {
                e = fDocument.createElementNS(
                        fStringPool.toString(fStringPool.getURIForQName(elementTypeIndex)),
                        fStringPool.toString(elementTypeIndex)
                    );
            } else {
                e = fDocument.createElement(elementName);
            }
            int attrHandle = xmlAttrList.getFirstAttr(attrListIndex);
            while (attrHandle != -1) {
                if (nsEnabled) {
                    int attName = xmlAttrList.getAttrName(attrHandle);
                    String attNameStr = fStringPool.toString(attName);
		    int nsURIIndex = fStringPool.getURIForQName(attName);
		    String namespaceURI = fStringPool.toString(nsURIIndex);
		    // DOM Level 2 wants all namespace declaration attributes
		    // to be bound to "http://www.w3.org/2000/xmlns/"
		    // So as long as the XML parser doesn't do it, it needs to
		    // done here.
		    int prefixIndex = fStringPool.getPrefixForQName(attName);
		    String prefix = fStringPool.toString(prefixIndex);
		    if (namespaceURI == null) {
			if (prefix != null) {
			    if (prefix.equals("xmlns")) {
				namespaceURI = "http://www.w3.org/2000/xmlns/";
			    }
			} else if (attNameStr.equals("xmlns")) {
			    namespaceURI = "http://www.w3.org/2000/xmlns/";
			}
		    }
                    e.setAttributeNS(namespaceURI,
				     attNameStr,
                                     fStringPool.toString(xmlAttrList.getAttValue(attrHandle)));
                } else {
                    String attrName = fStringPool.toString(xmlAttrList.getAttrName(attrHandle));
                    String attrValue = fStringPool.toString(xmlAttrList.getAttValue(attrHandle));
                    e.setAttribute(attrName, attrValue);
                    if (fDocumentImpl != null && !xmlAttrList.isSpecified(attrHandle)) {
                        ((AttrImpl)e.getAttributeNode(attrName)).setSpecified(false);
                    }
                }
                attrHandle = xmlAttrList.getNextAttr(attrHandle);
            }
            fCurrentElementNode.appendChild(e);
            fCurrentElementNode = e;
            fWithinElement = true;

            // identifier registration
            if (fDocumentImpl != null) {
                int index = xmlAttrList.getFirstAttr(attrListIndex);
                while (index != -1) {
                    if (xmlAttrList.getAttType(index) == fStringPool.addSymbol("ID")) {
                        String name = fStringPool.toString(xmlAttrList.getAttValue(index));
                        fDocumentImpl.putIdentifier(name, e);
                    }
                    index = xmlAttrList.getNextAttr(index);
                }
            }

            xmlAttrList.releaseAttrList(attrListIndex);

            // copy schema grammar, if needed
            if (!fSeenRootElement) {
                fSeenRootElement = true;
                if (fDocumentImpl != null && fGrammarAccess) {
                    Document schemaDocument = fValidator.getSchemaDocument();
                    if (schemaDocument != null) {
                        if (fDocumentType == null) {
                            String rootName = elementName;
                            String systemId = ""; // REVISIT: How do we get this value? -Ac
                            String publicId = ""; // REVISIT: How do we get this value? -Ac
                            fDocumentType = fDocumentImpl.createDocumentType(rootName, publicId, systemId);
                            fDocument.appendChild(fDocumentType);
                        }

                        Element schema = schemaDocument.getDocumentElement();
                        XUtil.copyInto(schema, fDocumentType);
                    }
                }
            }
        }

    } // startElement(int,XMLAttrList,int)

    /** End element. */
    public void endElement(int elementTypeIndex)
        throws Exception {

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {
            fCurrentNodeIndex = fDeferredDocumentImpl.getParentNode(fCurrentNodeIndex, false);
            fWithinElement = false;
        }

        // full node expansion
        else {
            fCurrentElementNode = fCurrentElementNode.getParentNode();
            fWithinElement = false;
        }

    } // endElement(int)

    /** Characters. */
    public void characters(int dataIndex)
        throws Exception {

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {

            int text;

            if (fInCDATA) {
                text = fDeferredDocumentImpl.createCDATASection(dataIndex, false);
            } else {
                // The Text normalization is taken care of within the Text Node
                // in the DEFERRED case.
                text = fDeferredDocumentImpl.createTextNode(dataIndex, false);
            }
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, text);
        }

        // full node expansion
        else {

            Text text;

            if (fInCDATA) {
                text = fDocument.createCDATASection(fStringPool.orphanString(dataIndex));
            }
            else {

                if (fWithinElement && fCurrentElementNode.getNodeType() == Node.ELEMENT_NODE) {
                    Node lastChild = fCurrentElementNode.getLastChild();
                    if (lastChild != null
                        && lastChild.getNodeType() == Node.TEXT_NODE) {
                        // Normalization of Text Nodes - append rather than create.
                        ((Text)lastChild).appendData(fStringPool.orphanString(dataIndex));
                        return;
                    }
                }
                text = fDocument.createTextNode(fStringPool.orphanString(dataIndex));
            }

            fCurrentElementNode.appendChild(text);

        }

    } // characters(int)

    /** Ignorable whitespace. */
    public void ignorableWhitespace(int dataIndex) throws Exception {

        // ignore the whitespace
        if (!fIncludeIgnorableWhitespace) {
            fStringPool.orphanString(dataIndex);
            return;
        }

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {

            int text;

            if (fInCDATA) {
                text = fDeferredDocumentImpl.createCDATASection(dataIndex, true);
            } else {
                // The Text normalization is taken care of within the Text Node
                // in the DEFERRED case.
                text = fDeferredDocumentImpl.createTextNode(dataIndex, true);
            }
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, text);
        }

        // full node expansion
        else {

            Text text;

            if (fInCDATA) {
                text = fDocument.createCDATASection(fStringPool.orphanString(dataIndex));
            }
            else {

                if (fWithinElement && fCurrentElementNode.getNodeType() == Node.ELEMENT_NODE) {
                    Node lastChild = fCurrentElementNode.getLastChild();
                    if (lastChild != null
                        && lastChild.getNodeType() == Node.TEXT_NODE) {
                        // Normalization of Text Nodes - append rather than create.
                        ((Text)lastChild).appendData(fStringPool.orphanString(dataIndex));
                        return;
                    }
                }
                text = fDocument.createTextNode(fStringPool.orphanString(dataIndex));
            }

            if (fDocumentImpl != null) {
                ((TextImpl)text).setIgnorableWhitespace(true);
            }

            fCurrentElementNode.appendChild(text);

        }

    } // ignorableWhitespace(int)

    /** Processing instruction. */
    public void processingInstruction(int targetIndex, int dataIndex)
        throws Exception {

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {
            int pi = fDeferredDocumentImpl.createProcessingInstruction(targetIndex, dataIndex);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, pi);
        }

        // full node expansion
        else {
            String target = fStringPool.orphanString(targetIndex);
            String data = fStringPool.orphanString(dataIndex);
            ProcessingInstruction pi = fDocument.createProcessingInstruction(target, data);
            fCurrentElementNode.appendChild(pi);
        }

    } // processingInstruction(int,int)

    /** Comment. */
    public void comment(int dataIndex) throws Exception {

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {
            int comment = fDeferredDocumentImpl.createComment(dataIndex);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, comment);
        }

        // full node expansion
        else {
            Comment comment = fDocument.createComment(fStringPool.orphanString(dataIndex));
            fCurrentElementNode.appendChild(comment);
        }

    } // comment(int)

    /** Not called. */
    public void characters(char ch[], int start, int length) throws Exception {}

    /** Not called. */
    public void ignorableWhitespace(char ch[], int start, int length) throws Exception {}

    //
    // XMLDocumentScanner methods
    //

    /** Start CDATA section. */
    public void startCDATA() throws Exception {
        fInCDATA = true;
    }

    /** End CDATA section. */
    public void endCDATA() throws Exception {
        fInCDATA = false;
    }

    //
    // XMLEntityHandler methods
    //

    /** Start entity reference. */
    public void startEntityReference(int entityName, int entityType,
                                     int entityContext) throws Exception {

        // are we ignoring entity reference nodes?
        if (!fCreateEntityReferenceNodes) {
            return;
        }

        // ignore built-in entities
        if (entityName == fAmpIndex ||
            entityName == fGtIndex ||
            entityName == fLtIndex ||
            entityName == fAposIndex ||
            entityName == fQuotIndex) {
            return;
        }

        // we only support one context for entity references right now...
        if (entityContext != XMLEntityHandler.ENTITYREF_IN_CONTENT) {
            return;
        }

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {

            int entityRefIndex = fDeferredDocumentImpl.createEntityReference(entityName);
            fDeferredDocumentImpl.appendChild(fCurrentNodeIndex, entityRefIndex);

            fCurrentNodeIndex = entityRefIndex;
        }

        // full node expansion
        else {

            EntityReference er = fDocument.createEntityReference(fStringPool.toString(entityName));

            fCurrentElementNode.appendChild(er);
            fCurrentElementNode = er;
        }

    } // startEntityReference(int,int,int)

    /** End entity reference. */
    public void endEntityReference(int entityName, int entityType,
                                   int entityContext) throws Exception {

        // are we ignoring entity reference nodes?
        if (!fCreateEntityReferenceNodes) {
            return;
        }

        // ignore built-in entities
        if (entityName == fAmpIndex ||
            entityName == fGtIndex ||
            entityName == fLtIndex ||
            entityName == fAposIndex ||
            entityName == fQuotIndex) {
            return;
        }

        // we only support one context for entity references right now...
        if (entityContext != XMLEntityHandler.ENTITYREF_IN_CONTENT) {
            return;
        }

        // deferred node expansion
        if (fDeferredDocumentImpl != null) {

            String name = fStringPool.toString(entityName);

            int erChild = fCurrentNodeIndex;
            fCurrentNodeIndex = fDeferredDocumentImpl.getParentNode(erChild, false);

            // should never be true - we should not return here.
            if (fDeferredDocumentImpl.getNodeType(erChild, false) != Node.ENTITY_REFERENCE_NODE)  return;

            erChild = fDeferredDocumentImpl.getFirstChild(erChild, false); // first Child of EntityReference

            if (fDocumentTypeIndex != -1) {
                // find Entity decl for this EntityReference.
                int entityDecl = fDeferredDocumentImpl.getFirstChild(fDocumentTypeIndex, false);
                while (entityDecl != -1) {
                    if (fDeferredDocumentImpl.getNodeType(entityDecl, false) == Node.ENTITY_NODE
                    && fDeferredDocumentImpl.getNodeNameString(entityDecl, false).equals(name)) // string compare...
                    {
                        break;
                    }
                    entityDecl = fDeferredDocumentImpl.getNextSibling(entityDecl, false);
                }

                if (entityDecl != -1
                    && fDeferredDocumentImpl.getFirstChild(entityDecl, false) == -1) {
                    // found entityDecl with same name as this reference
                    // AND it doesn't have any children.

                    // we don't need to iterate, because the whole structure
                    // should already be connected to the 1st child.
                    fDeferredDocumentImpl.setAsFirstChild(entityDecl, erChild);
                }
            }

        }

        // full node expansion
        else {

            Node erNode = fCurrentElementNode;//fCurrentElementNode.getParentNode();
            fCurrentElementNode = erNode.getParentNode();

            if (fDocumentImpl != null) {

                NamedNodeMap entities = fDocumentType.getEntities();
                String name = fStringPool.toString(entityName);
                Node entityNode = entities.getNamedItem(name);

                // We could simply return here if there is no entity for the reference.
                if (entityNode == null || entityNode.hasChildNodes()) {
                    return;
                }

                Entity entity = (Entity)entityNode;

                if (erNode.hasChildNodes()) {
                    NodeList list = erNode.getChildNodes();
                    int len = list.getLength();
                    for (int i = 0; i < len; i++) {
                        Node childClone = list.item(i).cloneNode(true);
                        entity.appendChild(childClone);
                    }
                }
            }
        }

    } // endEntityReference(int,int,int)

    //
    // DTDValidator.EventHandler methods
    //

    /**
     *  This function will be called when a &lt;!DOCTYPE...&gt; declaration is
     *  encountered.
     */
    public void startDTD(int rootElementType, int publicId, int systemId)
        throws Exception {

        // full expansion
        if (fDocumentImpl != null) {
            String rootElementName = fStringPool.toString(rootElementType);
            String publicString = fStringPool.toString(publicId);
            String systemString = fStringPool.toString(systemId);
            fDocumentType = fDocumentImpl.
                createDocumentType(rootElementName, publicString, systemString);
            fDocumentImpl.appendChild(fDocumentType);

            if (fGrammarAccess) {
                Element schema = fDocument.createElement("schema");
                // REVISIT: What should the namespace be? -Ac
                schema.setAttribute("xmlns", "http://www.w3.org/1999/XMLSchema");
                ((AttrImpl)schema.getAttributeNode("xmlns")).setSpecified(false);
                schema.setAttribute("finalDefault", "");
                ((AttrImpl)schema.getAttributeNode("finalDefault")).setSpecified(false);
                schema.setAttribute("exactDefault", "");
                ((AttrImpl)schema.getAttributeNode("exactDefault")).setSpecified(false);
                fDocumentType.appendChild(schema);
            }
        }

        // deferred expansion
        else if (fDeferredDocumentImpl != null) {
            fDocumentTypeIndex =
                fDeferredDocumentImpl.
                    createDocumentType(rootElementType, publicId, systemId);
            fDeferredDocumentImpl.appendChild(fDocumentIndex, fDocumentTypeIndex);

            if (fGrammarAccess) {
                int handle = fAttrList.startAttrList();
                fAttrList.addAttr(
                    fStringPool.addSymbol("xmlns"),
                    fStringPool.addString("http://www.w3.org/1999/XMLSchema"),
                    fStringPool.addSymbol("CDATA"),
                    false,
                    false); // search
                fAttrList.addAttr(
                    fStringPool.addSymbol("finalDefault"),
                    fStringPool.addString(""),
                    fStringPool.addSymbol("CDATA"),
                    false,
                    false); // search
                fAttrList.addAttr(
                    fStringPool.addSymbol("exactDefault"),
                    fStringPool.addString(""),
                    fStringPool.addSymbol("CDATA"),
                    false,
                    false); // search
                fAttrList.endAttrList();
                int schemaIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("schema"), fAttrList, handle);
                // REVISIT: What should the namespace be? -Ac
                fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, schemaIndex);
            }
        }

    } // startDTD(int,int,int)
    
    /**
     * Supports DOM Level 2 internalSubset additions.
     * Called when the internal subset is completely scanned.
     */
    public  void internalSubset(int internalSubset) {
        
        //System.out.println("internalSubset callback:"+fStringPool.toString(internalSubset));
        
        // full expansion
        if (fDocumentImpl != null && fDocumentType != null) {
            ((DocumentTypeImpl)fDocumentType).setInternalSubset(fStringPool.toString(internalSubset));
        }

        // deferred expansion
        else if (fDeferredDocumentImpl != null) {
            fDeferredDocumentImpl.setInternalSubset(fDocumentTypeIndex, internalSubset);
        }
        
    }
    

    /**
     *  This function will be called at the end of the DTD.
     */
    public void endDTD() throws Exception {}

    /**
     * &lt;!ELEMENT Name contentspec&gt;
     */
    public void elementDecl(int elementTypeIndex, XMLContentSpec contentSpec)
        throws Exception {

        if (DEBUG_ATTLIST_DECL) {
            String contentModel = contentSpec.toString();
            System.out.println("elementDecl(" + fStringPool.toString(elementTypeIndex) + ", " +
                                                contentModel + ")");
        }

        //
        // Create element declaration
        //

        if (fGrammarAccess) {

            if (fDeferredDocumentImpl != null) {

                //
                // Build element
                //

                // get element declaration; create if necessary
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String elementName = fStringPool.toString(elementTypeIndex);
                int elementIndex = getFirstChildElement(schemaIndex, "element", "name", elementName);
                if (elementIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(elementName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("minOccurs"), // name
                        fStringPool.addString("1"), // value
                        fStringPool.addSymbol("NMTOKEN"), // type
                        false, // specified
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("nullable"), // name
                        fStringPool.addString("false"), // value
                        fStringPool.addSymbol("ENUMERATION"), // type
                        false, // specified
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("abstract"), // name
                        fStringPool.addString("false"), // value
                        fStringPool.addSymbol("ENUMERATION"), // type
                        false, // specified
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("final"), // name
                        fStringPool.addString("false"), // value
                        fStringPool.addSymbol("ENUMERATION"), // type
                        false, // specified
                        false); // search
                    fAttrList.endAttrList();
                    elementIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("element"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, elementIndex);
                }

                //
                // Build content model
                //

                // <!ELEMENT name (#PCDATA)>
                int contentType = contentSpec.getType();
                String contentTypeName = fStringPool.toString(contentType);
                XMLContentSpec.Node node = new XMLContentSpec.Node();
                int contentSpecHandle = contentSpec.getHandle();
                if (contentSpecHandle != -1) {
                    contentSpec.getNode(contentSpecHandle, node);
                }

                // (#PCDATA)
                if (contentTypeName.equals("MIXED") && node.type == 0) {
                    int attrNameIndex = fStringPool.addSymbol("type");
                    int attrValueIndex = fStringPool.addString("string");
                    boolean attrSpecified = true;
                    int attrIndex = fDeferredDocumentImpl.createAttribute(attrNameIndex, attrValueIndex, attrSpecified);
                    fDeferredDocumentImpl.setAttributeNode(elementIndex, attrIndex);
                }

                // other content models
                else {
                    // get type element; create if necessary
                    int typeIndex = getFirstChildElement(elementIndex, "type");
                    if (typeIndex == -1) {
                        typeIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("type"), null, -1);
                        // REVISIT: Check for type redeclaration? -Ac
                        fDeferredDocumentImpl.insertBefore(elementIndex, typeIndex, getFirstChildElement(elementIndex));
                    }

                    // <!ELEMENT name EMPTY>
                    if (contentTypeName.equals("EMPTY")) {
                        int attributeIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("content"), fStringPool.addString("empty"), true);
                        fDeferredDocumentImpl.setAttributeNode(typeIndex, attributeIndex);
                    }

                    // <!ELEMENT name ANY>
                    else if (contentTypeName.equals("ANY")) {
                        int anyIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("any"), null, -1);
                        fDeferredDocumentImpl.insertBefore(typeIndex, anyIndex, getFirstChildElement(typeIndex));
                    }

                    // <!ELEMENT name (a,b,...)> or <!ELEMENT name (a|b|...)>
                    else if (contentTypeName.equals("CHILDREN")) {
                        int attributeIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("content"), fStringPool.addString("elementOnly"), true);
                        fDeferredDocumentImpl.setAttributeNode(typeIndex, attributeIndex);
                        //attributeIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("order"), fStringPool.addString("seq"), false);
                        //fDeferredDocumentImpl.setAttributeNode(typeIndex, attributeIndex);

                        int contentSpecIndex = contentSpec.getHandle();
                        contentSpec.getNode(contentSpecIndex, node);
                        Element model = createContentModel(contentSpec, node);

                        int modelIndex = createDeferredContentModel(model);
                        int firstChildIndex = getFirstChildElement(typeIndex);
                        fDeferredDocumentImpl.insertBefore(typeIndex, modelIndex, firstChildIndex);
                    }

                    // <!ELEMENT name (#PCDATA|a|...)*>
                    else {
                        // REVISIT: Any chance of getting other than MIXED? -Ac
                        // set content type
                        int attrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("content"), fStringPool.addString("mixed"), true);
                        fDeferredDocumentImpl.setAttributeNode(typeIndex, attrIndex);

                        // skip '*' node
                        contentSpec.getNode(node.value, node);

                        // add leaves (on descent)
                        do {
                            int index = node.value;
                            int handle = fAttrList.startAttrList();
                            contentSpec.getNode(node.otherValue, node);
                            String elementRefName = fStringPool.toString(node.value);
                            fAttrList.addAttr(
                                fStringPool.addSymbol("ref"),
                                fStringPool.addString(elementRefName),
                                fStringPool.addSymbol("NMTOKEN"),
                                true,
                                false); // search
                            fAttrList.endAttrList();
                            int elementRefIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("element"), fAttrList, handle);
                            fDeferredDocumentImpl.insertBefore(typeIndex, elementRefIndex, getFirstChildElement(typeIndex, "element"));
                            contentSpec.getNode(index, node);
                        } while (node.type != XMLContentSpec.CONTENTSPECNODE_LEAF);
                    }
                }

            } // if defer-node-expansion

            else if (fDocumentImpl != null) {

                //
                // Build element
                //

                // get element declaration; create if necessary
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                String elementName = fStringPool.toString(elementTypeIndex);
                Element element = XUtil.getFirstChildElement(schema, "element", "name", elementName);
                if (element == null) {
                    element = fDocument.createElement("element");
                    element.setAttribute("name", elementName);
                    element.setAttribute("minOccurs", "1");
                    ((AttrImpl)element.getAttributeNode("minOccurs")).setSpecified(false);
                    element.setAttribute("nullable", "false");
                    ((AttrImpl)element.getAttributeNode("nullable")).setSpecified(false);
                    element.setAttribute("abstract", "false");
                    ((AttrImpl)element.getAttributeNode("abstract")).setSpecified(false);
                    element.setAttribute("final", "false");
                    ((AttrImpl)element.getAttributeNode("final")).setSpecified(false);
                    schema.appendChild(element);
                }

                //
                // Build content model
                //

                // <!ELEMENT name (#PCDATA)>
                int contentType = contentSpec.getType();
                String contentTypeName = fStringPool.toString(contentType);
                XMLContentSpec.Node node = new XMLContentSpec.Node();
                int contentSpecHandle = contentSpec.getHandle();
                if (contentSpecHandle != -1) {
                    contentSpec.getNode(contentSpecHandle, node);
                }

                // (#PCDATA)
                if (contentTypeName.equals("MIXED") && node.type == 0) {
                    element.setAttribute("type", "string");
                }

                // other content models
                else {
                    // get type element; create if necessary
                    Element type = XUtil.getFirstChildElement(element, "type");
                    if (type == null) {
                        type = fDocumentImpl.createElement("type");
                        // REVISIT: Check for type redeclaration? -Ac
                        element.insertBefore(type, XUtil.getFirstChildElement(element));
                    }

                    // <!ELEMENT name EMPTY>
                    if (contentTypeName.equals("EMPTY")) {
                        type.setAttribute("content", "empty");
                    }

                    // <!ELEMENT name ANY>
                    else if (contentTypeName.equals("ANY")) {
                        Element any = fDocumentImpl.createElement("any");
                        type.insertBefore(any, XUtil.getFirstChildElement(type));
                    }

                    // <!ELEMENT name (a,b,...)> or <!ELEMENT name (a|b|...)>
                    else if (contentTypeName.equals("CHILDREN")) {
                        type.setAttribute("content", "elementOnly");
                        //((AttrImpl)type.getAttributeNode("content")).setSpecified(false);
                        //type.setAttribute("order", "seq");
                        //((AttrImpl)type.getAttributeNode("order")).setSpecified(false);

                        int contentSpecIndex = contentSpec.getHandle();
                        contentSpec.getNode(contentSpecIndex, node);
                        Element model = createContentModel(contentSpec, node);
                        Node fragment = fDocument.createDocumentFragment();
                        XUtil.copyInto(model, fragment);
                        Element firstChild = XUtil.getFirstChildElement(type);
                        type.insertBefore(fragment, firstChild);
                    }

                    // <!ELEMENT name (#PCDATA|a|...)*>
                    else {
                        // REVISIT: Any chance of getting other than MIXED? -Ac
                        // set content type
                        type.setAttribute("content", "mixed");

                        // skip '*' node
                        contentSpec.getNode(node.value, node);

                        // add leaves (on descent)
                        do {
                            int index = node.value;
                            int handle = fAttrList.startAttrList();
                            contentSpec.getNode(node.otherValue, node);
                            String elementRefName = fStringPool.toString(node.value);
                            Element elementRef = fDocumentImpl.createElement("element");
                            elementRef.setAttribute("ref", elementRefName);
                            type.insertBefore(elementRef, XUtil.getFirstChildElement(type, "element"));
                            contentSpec.getNode(index, node);
                        } while (node.type != XMLContentSpec.CONTENTSPECNODE_LEAF);
                    }
                }

            } // if NOT defer-node-expansion

        } // if grammar-access

    } // elementDecl(int,String)

    /**
     * &lt;!ATTLIST Name AttDef&gt;
     */
    public void attlistDecl(int elementTypeIndex,
                            int attrNameIndex, int attType,
                            String enumString,
                            int attDefaultType, int attDefaultValue)
        throws Exception {

        if (DEBUG_ATTLIST_DECL) {
            System.out.println("attlistDecl(" + fStringPool.toString(elementTypeIndex) + ", " +
                                                fStringPool.toString(attrNameIndex) + ", " +
                                                fStringPool.toString(attType) + ", " +
                                                enumString + ", " +
                                                fStringPool.toString(attDefaultType) + ", " +
                                                fStringPool.toString(attDefaultValue) + ")");
        }

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            // get the default value
            if (attDefaultValue != -1) {
                if (DEBUG_ATTLIST_DECL) {
                    System.out.println("  adding default attribute value: "+
                                       fStringPool.toString(attDefaultValue));
                }

                // get element definition
                int elementDefIndex  = fDeferredDocumentImpl.lookupElementDefinition(elementTypeIndex);

                // create element definition if not already there
                if (elementDefIndex == -1) {
                    elementDefIndex = fDeferredDocumentImpl.createElementDefinition(elementTypeIndex);
                    fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, elementDefIndex);
                }

                // add default attribute
                int attrIndex = fDeferredDocumentImpl.createAttribute(attrNameIndex, attDefaultValue, false);
                fDeferredDocumentImpl.appendChild(elementDefIndex, attrIndex);

            }

            //
            // Create attribute declaration
            //

            if (fGrammarAccess) {

                // get element declaration; create it if necessary
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String elementName = fStringPool.toString(elementTypeIndex);
                int elementIndex = getFirstChildElement(schemaIndex, "element", "name", elementName);
                if (elementIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(elementName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); //search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("export"),
                        fStringPool.addString("true"),
                        fStringPool.addSymbol("ENUMERATION"),
                        false,
                        false); // search
                    fAttrList.endAttrList();
                    elementIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("element"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, elementIndex);
                }

                // get type element; create it if necessary
                int typeIndex = getFirstChildElement(elementIndex, "type");
                if (typeIndex == -1) {
                    typeIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("type"), null, -1);
                    fDeferredDocumentImpl.insertBefore(elementIndex, typeIndex, getFirstChildElement(elementIndex));
                }

                // create attribute and set its attributes
                String attributeName = fStringPool.toString(attrNameIndex);
                int attributeIndex = getFirstChildElement(elementIndex, "attribute", "name", attributeName);
                if (attributeIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(attributeName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    /***
                    fAttrList.addAttr(
                        fStringPool.addSymbol("type"),
                        fStringPool.addString("string"),
                        fStringPool.addSymbol("CDATA"),
                        true,
                        false); // search
                    /***/
                    fAttrList.addAttr(
                        fStringPool.addSymbol("minOccurs"),
                        fStringPool.addString("0"),
                        fStringPool.addSymbol("CDATA"),
                        false,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("maxOccurs"),
                        fStringPool.addString("1"),
                        fStringPool.addSymbol("CDATA"),
                        false,
                        false); // search
                    fAttrList.endAttrList();
                    attributeIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("attribute"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(typeIndex, attributeIndex);

                    // attribute type: CDATA, ENTITY, ... , NMTOKENS; ENUMERATION
                    String attributeTypeName = fStringPool.toString(attType);
                    if (attributeTypeName.equals("CDATA")) {
                        int typeAttrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("type"), fStringPool.addString("string"), false);
                        fDeferredDocumentImpl.setAttributeNode(attributeIndex, typeAttrIndex);
                    }
                    else if (attributeTypeName.equals("ENUMERATION")) {
                        handle = fAttrList.startAttrList();
                        fAttrList.addAttr(
                            fStringPool.addSymbol("source"),
                            fStringPool.addString("NMTOKEN"),
                            fStringPool.addSymbol("CDATA"),
                            true,
                            false); // search
                        fAttrList.endAttrList();
                        int datatypeIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("datatype"), fAttrList, handle);
                        fDeferredDocumentImpl.appendChild(attributeIndex, datatypeIndex);

                        String tokenizerString = enumString.substring(1, enumString.length() - 1);
                        StringTokenizer tokenizer = new StringTokenizer(tokenizerString, "|");
                        while (tokenizer.hasMoreTokens()) {
                            handle = fAttrList.startAttrList();
                            fAttrList.addAttr(
                                fStringPool.addSymbol("value"),
                                fStringPool.addString(tokenizer.nextToken()),
                                fStringPool.addSymbol("CDATA"),
                                true,
                                false); // search
                            fAttrList.endAttrList();
                            int enumerationIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("enumeration"), fAttrList, handle);
                            fDeferredDocumentImpl.appendChild(datatypeIndex, enumerationIndex);
                        }
                    }
                    else {
                        // REVISIT: Could we ever get an unknown data type? -Ac
                        int typeAttrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("type"), fStringPool.addString(attributeTypeName), true);
                        fDeferredDocumentImpl.setAttributeNode(attributeIndex, typeAttrIndex);
                    }

                    // attribute default type: #IMPLIED, #REQUIRED, #FIXED
                    boolean fixed = false;
                    if (attDefaultType != -1) {
                        String attributeDefaultTypeName = fStringPool.toString(attDefaultType);
                        if (attributeDefaultTypeName.equals("#REQUIRED")) {
                            int minOccursAttrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("minOccurs"), fStringPool.addString("1"), true);
                            int oldMinOccursAttrIndex = fDeferredDocumentImpl.setAttributeNode(attributeIndex, minOccursAttrIndex);
                            fStringPool.releaseString(fDeferredDocumentImpl.getNodeValue(oldMinOccursAttrIndex, false));
                        }
                        else if (attributeDefaultTypeName.equals("#FIXED")) {
                            fixed = true;
                            int fixedAttrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("fixed"), attDefaultValue, true);
                            fDeferredDocumentImpl.setAttributeNode(attributeIndex, fixedAttrIndex);
                        }
                    }

                    // attribute default value
                    if (!fixed && attDefaultValue != -1) {
                        int defaultAttrIndex = fDeferredDocumentImpl.createAttribute(fStringPool.addSymbol("default"), attDefaultValue, true);
                        fDeferredDocumentImpl.setAttributeNode(attributeIndex, defaultAttrIndex);
                    }
                }
            }

        }

        // full expansion
        else if (fDocumentImpl != null) {

            // get the default value
            if (attDefaultValue != -1) {
                if (DEBUG_ATTLIST_DECL) {
                    System.out.println("  adding default attribute value: "+
                                       fStringPool.toString(attDefaultValue));
                }

                // get element name
                String elementName = fStringPool.toString(elementTypeIndex);

                // get element definition node
                NamedNodeMap elements = ((DocumentTypeImpl)fDocumentType).getElements();
                ElementDefinitionImpl elementDef = (ElementDefinitionImpl)elements.getNamedItem(elementName);
                if (elementDef == null) {
                    elementDef = fDocumentImpl.createElementDefinition(elementName);
                    ((DocumentTypeImpl)fDocumentType).getElements().setNamedItem(elementDef);
                }

                // REVISIT: Check for uniqueness of element name? -Ac

                // get attribute name and value index
                String attrName      = fStringPool.toString(attrNameIndex);
                String attrValue     = fStringPool.toString(attDefaultValue);

                // create attribute and set properties
                AttrImpl attr = (AttrImpl)fDocumentImpl.createAttribute(attrName);
                attr.setValue(attrValue);
                attr.setSpecified(false);

                // add default attribute to element definition
                elementDef.getAttributes().setNamedItem(attr);
            }

            //
            // Create attribute declaration
            //

            if (fGrammarAccess) {

                // get element declaration; create it if necessary
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                String elementName = fStringPool.toString(elementTypeIndex);
                Element element = XUtil.getFirstChildElement(schema, "element", "name", elementName);
                if (element == null) {
                    element = fDocument.createElement("element");
                    element.setAttribute("name", elementName);
                    //element.setAttribute("export", "true");
                    //((AttrImpl)element.getAttributeNode("export")).setSpecified(false);
                    schema.appendChild(element);
                }

                // get type element; create it if necessary
                Element type = XUtil.getFirstChildElement(element, "type");
                if (type == null) {
                    type = fDocument.createElement("type");
                    // REVISIT: Check for archetype redeclaration? -Ac
                    element.insertBefore(type, XUtil.getFirstChildElement(element));
                }

                // create attribute and set its attributes
                String attributeName = fStringPool.toString(attrNameIndex);
                Element attribute = XUtil.getFirstChildElement(element, "attribute", "name", attributeName);
                if (attribute == null) {
                    attribute = fDocument.createElement("attribute");
                    attribute.setAttribute("name", attributeName);
                    attribute.setAttribute("minOccurs", "0");
                    ((AttrImpl)attribute.getAttributeNode("minOccurs")).setSpecified(false);
                    attribute.setAttribute("maxOccurs", "1");
                    ((AttrImpl)attribute.getAttributeNode("maxOccurs")).setSpecified(false);
                    type.appendChild(attribute);

                    // attribute type: CDATA, ENTITY, ... , NMTOKENS; ENUMERATION
                    String attributeTypeName = fStringPool.toString(attType);
                    if (attributeTypeName.equals("CDATA")) {
                        attribute.setAttribute("type", "string");
                        ((AttrImpl)attribute.getAttributeNode("type")).setSpecified(false);
                    }
                    else if (attributeTypeName.equals("ENUMERATION")) {
                        Element datatype = fDocumentImpl.createElement("datatype");
                        datatype.setAttribute("source", "NMTOKEN");
                        attribute.appendChild(datatype);
                        String tokenizerString = enumString.substring(1, enumString.length() - 1);
                        StringTokenizer tokenizer = new StringTokenizer(tokenizerString, "|");
                        while (tokenizer.hasMoreTokens()) {
                            Element enumeration = fDocument.createElement("enumeration");
                            enumeration.setAttribute("value", tokenizer.nextToken());
                            datatype.appendChild(enumeration);
                        }
                    }
                    else {
                        // REVISIT: Could we ever get an unknown data type? -Ac
                        attribute.setAttribute("type", attributeTypeName);
                    }

                    // attribute default type: #IMPLIED, #REQUIRED, #FIXED
                    boolean fixed = false;
                    if (attDefaultType != -1) {
                        String attributeDefaultTypeName = fStringPool.toString(attDefaultType);
                        if (attributeDefaultTypeName.equals("#REQUIRED")) {
                            attribute.setAttribute("minOccurs", "1");
                            ((AttrImpl)attribute.getAttributeNode("minOccurs")).setSpecified(true);
                        }
                        else if (attributeDefaultTypeName.equals("#FIXED")) {
                            fixed = true;
                            String fixedValue = fStringPool.toString(attDefaultValue);
                            attribute.setAttribute("fixed", fixedValue);
                        }
                    }

                    // attribute default value
                    if (!fixed && attDefaultValue != -1) {
                        String defaultValue = fStringPool.toString(attDefaultValue);
                        attribute.setAttribute("default", defaultValue);
                    }
                }
            }

        } // if NOT defer-node-expansion

    } // attlistDecl(int,int,int,String,int,int)

    /**
     * &lt;!ENTITY % Name EntityValue&gt; (internal)
     */
    public void internalPEDecl(int entityName, int entityValue) throws Exception {}

    /**
     * &lt;!ENTITY % Name ExternalID>                (external)
     */
    public void externalPEDecl(int entityName, int publicId, int systemId) throws Exception {}

    /**
     * &lt;!ENTITY Name EntityValue&gt; (internal)
     */
    public void internalEntityDecl(int entityNameIndex, int entityValueIndex)
        throws Exception {

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            if (fDocumentTypeIndex == -1) return; //revisit: should never happen. Exception?

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            int newEntityIndex = fDeferredDocumentImpl.createEntity(entityNameIndex, -1, -1, -1);
            fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, newEntityIndex);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // create internal entity declaration
            if (fGrammarAccess) {
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String entityName = fStringPool.toString(entityNameIndex);
                int textEntityIndex = getFirstChildElement(schemaIndex, "textEntity", "name", entityName);
                if (textEntityIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(entityName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("export"),
                        fStringPool.addString("true"),
                        fStringPool.addSymbol("ENUMERATION"),
                        false,
                        false); // search
                    fAttrList.endAttrList();

                    textEntityIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("textEntity"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, textEntityIndex);

                    int textIndex = fDeferredDocumentImpl.createTextNode(entityValueIndex, false);
                    fDeferredDocumentImpl.appendChild(textEntityIndex, textIndex);
                }
            }
            /***/
        }

        // full expansion
        else if (fDocumentImpl != null) {
            if (fDocumentType == null) return; //revisit: should never happen. Exception?

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            String entityName = fStringPool.toString(entityNameIndex);

            Entity entity = fDocumentImpl.createEntity(entityName);
            fDocumentType.getEntities().setNamedItem(entity);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // create internal entity declaration
            if (fGrammarAccess) {
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                Element textEntity = XUtil.getFirstChildElement(schema, "textEntity", "name", entityName);
                if (textEntity == null) {
                    textEntity = fDocument.createElement("textEntity");
                    textEntity.setAttribute("name", entityName);
                    textEntity.setAttribute("export", "true");
                    ((AttrImpl)textEntity.getAttributeNode("export")).setSpecified(false);
                    String entityValue = fStringPool.toString(entityValueIndex);
                    Text value = fDocument.createTextNode(entityValue);
                    textEntity.appendChild(value);
                    schema.appendChild(textEntity);
                }
            }
            /***/
        }

    } // internalEntityDecl(int,int)

    /**
     * &lt;!ENTITY Name ExternalID>                (external)
     */
    public void externalEntityDecl(int entityNameIndex, int publicIdIndex, int systemIdIndex)
        throws Exception {

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            int newEntityIndex = fDeferredDocumentImpl.createEntity(entityNameIndex, publicIdIndex, systemIdIndex, -1);

            fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, newEntityIndex);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // create external entity declaration
            if (fGrammarAccess) {
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String entityName = fStringPool.toString(entityNameIndex);
                int externalEntityIndex = getFirstChildElement(schemaIndex, "externalEntity", "name", entityName);
                if (externalEntityIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(entityName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("export"),
                        fStringPool.addString("true"),
                        fStringPool.addSymbol("ENUMERATION"),
                        false,
                        false); // search
                    if (publicIdIndex != -1) {
                        fAttrList.addAttr(
                            fStringPool.addSymbol("public"),
                            publicIdIndex,
                            fStringPool.addSymbol("CDATA"),
                            true,
                            false); // search
                    }
                    fAttrList.addAttr(
                        fStringPool.addSymbol("system"),
                        systemIdIndex,
                        fStringPool.addSymbol("CDATA"),
                        true,
                        false); // search
                    fAttrList.endAttrList();
                    externalEntityIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("externalEntity"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, externalEntityIndex);
                }
            }
            /***/
        }

        // full expansion
        else if (fDocumentImpl != null) {

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            String entityName = fStringPool.toString(entityNameIndex);
            String publicId = fStringPool.toString(publicIdIndex);
            String systemId = fStringPool.toString(systemIdIndex);

            EntityImpl entity = (EntityImpl)fDocumentImpl.createEntity(entityName);
            if (publicIdIndex != -1) {
                entity.setPublicId(publicId);
            }
            entity.setSystemId(systemId);
            fDocumentType.getEntities().setNamedItem(entity);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // create external entity declaration
            if (fGrammarAccess) {
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                Element externalEntity = XUtil.getFirstChildElement(schema, "externalEntity", "name", entityName);
                if (externalEntity == null) {
                    externalEntity = fDocument.createElement("externalEntity");
                    externalEntity.setAttribute("name", entityName);
                    externalEntity.setAttribute("export", "true");
                    ((AttrImpl)externalEntity.getAttributeNode("export")).setSpecified(false);
                    if (publicIdIndex != -1) {
                        externalEntity.setAttribute("public", publicId);
                    }
                    externalEntity.setAttribute("system", systemId);
                    schema.appendChild(externalEntity);
                }
            }
            /***/
        }

    } // externalEntityDecl(int,int,int)

    /**
     * &lt;!ENTITY Name ExternalID NDataDecl>      (unparsed)
     */
    public void unparsedEntityDecl(int entityNameIndex,
                                   int publicIdIndex, int systemIdIndex,
                                   int notationNameIndex) throws Exception {

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            int newEntityIndex = fDeferredDocumentImpl.createEntity(entityNameIndex, publicIdIndex, systemIdIndex, notationNameIndex);

            fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, newEntityIndex);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // add unparsed entity declaration
            if (fGrammarAccess) {
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String entityName = fStringPool.toString(entityNameIndex);
                int unparsedEntityIndex = getFirstChildElement(schemaIndex, "unparsedEntity", "name", entityName);
                if (unparsedEntityIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(entityName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("export"),
                        fStringPool.addString("true"),
                        fStringPool.addSymbol("ENUMERATION"),
                        false,
                        false); // search
                    if (publicIdIndex != -1) {
                        fAttrList.addAttr(
                            fStringPool.addSymbol("public"),
                            publicIdIndex,
                            fStringPool.addSymbol("CDATA"),
                            true,
                            false); // search
                    }
                    fAttrList.addAttr(
                        fStringPool.addSymbol("system"),
                        systemIdIndex,
                        fStringPool.addSymbol("CDATA"),
                        true,
                        false); // search
                    fAttrList.addAttr(
                        fStringPool.addSymbol("notation"),
                        fStringPool.addString(fStringPool.toString(notationNameIndex)),
                        fStringPool.addSymbol("CDATA"),
                        true,
                        false); // search
                    fAttrList.endAttrList();
                    unparsedEntityIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("unparsedEntity"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, unparsedEntityIndex);
                }
            }
            /***/
        }

        // full expansion
        else if (fDocumentImpl != null) {

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            String entityName = fStringPool.toString(entityNameIndex);
            String publicId = fStringPool.toString(publicIdIndex);
            String systemId = fStringPool.toString(systemIdIndex);
            String notationName = fStringPool.toString(notationNameIndex);

            EntityImpl entity = (EntityImpl)fDocumentImpl.createEntity(entityName);
            if (publicIdIndex != -1) {
                entity.setPublicId(publicId);
            }
            entity.setSystemId(systemId);
            entity.setNotationName(notationName);
            fDocumentType.getEntities().setNamedItem(entity);

            /***
            // REVISIT: Entities were removed from latest working draft. -Ac
            // add unparsed entity declaration
            if (fGrammarAccess) {
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                Element unparsedEntity = XUtil.getFirstChildElement(schema, "unparsedEntity", "name", entityName);
                if (unparsedEntity == null) {
                    unparsedEntity = fDocument.createElement("unparsedEntity");
                    unparsedEntity.setAttribute("name", entityName);
                    unparsedEntity.setAttribute("export", "true");
                    ((AttrImpl)unparsedEntity.getAttributeNode("export")).setSpecified(false);
                    if (publicIdIndex != -1) {
                        unparsedEntity.setAttribute("public", publicId);
                    }
                    unparsedEntity.setAttribute("system", systemId);
                    unparsedEntity.setAttribute("notation", notationName);
                    schema.appendChild(unparsedEntity);
                }
            }
            /***/
        }

    } // unparsedEntityDecl(int,int,int,int)

    /**
     * &lt;!NOTATION Name ExternalId>
     */
    public void notationDecl(int notationNameIndex, int publicIdIndex, int systemIdIndex)
        throws Exception {

        // deferred expansion
        if (fDeferredDocumentImpl != null) {

            //revisit: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            int newNotationIndex = fDeferredDocumentImpl.createNotation(notationNameIndex, publicIdIndex, systemIdIndex);

            fDeferredDocumentImpl.appendChild(fDocumentTypeIndex, newNotationIndex);

            // create notation declaration
            if (fGrammarAccess) {
                int schemaIndex = getFirstChildElement(fDocumentTypeIndex, "schema");
                String notationName = fStringPool.toString(notationNameIndex);
                int notationIndex = getFirstChildElement(schemaIndex, "notation", "name", notationName);
                if (notationIndex == -1) {
                    int handle = fAttrList.startAttrList();
                    fAttrList.addAttr(
                        fStringPool.addSymbol("name"),
                        fStringPool.addString(notationName),
                        fStringPool.addSymbol("NMTOKEN"),
                        true,
                        false); // search
                    /***
                    fAttrList.addAttr(
                        fStringPool.addSymbol("export"),
                        fStringPool.addString("true"),
                        fStringPool.addSymbol("ENUMERATION"),
                        false,
                        false); // search
                    /***/
                    if (publicIdIndex == -1) {
                        publicIdIndex = 0; // empty string in string pool
                    }
                    fAttrList.addAttr(
                        fStringPool.addSymbol("public"),
                        publicIdIndex,
                        fStringPool.addSymbol("CDATA"),
                        true,
                        false); // search
                    if (systemIdIndex != -1) {
                        fAttrList.addAttr(
                            fStringPool.addSymbol("system"),
                            systemIdIndex,
                            fStringPool.addSymbol("CDATA"),
                            true,
                            false); // search
                    }
                    fAttrList.endAttrList();
                    notationIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol("notation"), fAttrList, handle);
                    fDeferredDocumentImpl.appendChild(schemaIndex, notationIndex);
                }
            }
        }

        // full expansion
        else if (fDocumentImpl != null) {

            // REVISIT: how to check if entity was already declared.
            // XML spec says that 1st Entity decl is binding.

            String notationName = fStringPool.toString(notationNameIndex);
            String publicId = fStringPool.toString(publicIdIndex);
            String systemId = fStringPool.toString(systemIdIndex);

            NotationImpl notationImpl = (NotationImpl)fDocumentImpl.createNotation(notationName);
            notationImpl.setPublicId(publicId);
            if (systemIdIndex != -1) {
                notationImpl.setSystemId(systemId);
            }

            fDocumentType.getNotations().setNamedItem(notationImpl);

            // create notation declaration
            if (fGrammarAccess) {
                Element schema = XUtil.getFirstChildElement(fDocumentType, "schema");
                Element notation = XUtil.getFirstChildElement(schema, "notation", "name", notationName);
                if (notation == null) {
                    notation = fDocument.createElement("notation");
                    notation.setAttribute("name", notationName);
                    //notation.setAttribute("export", "true");
                    //((AttrImpl)notation.getAttributeNode("export")).setSpecified(false);
                    if (publicId == null) {
                        publicId = "";
                    }
                    notation.setAttribute("public", publicId);
                    if (systemIdIndex != -1) {
                        notation.setAttribute("system", systemId);
                    }
                    schema.appendChild(notation);
                }
            }
        }

    } // notationDecl(int,int,int)

    //
    // Private methods
    //

    /**
     * Creates a content model from the specified content spec node.
     * This method will always return a <em>group</em> element as the
     * containing element, even when the content model contains a
     * single element reference.
     */
    private Element createContentModel(XMLContentSpec contentSpec, XMLContentSpec.Node node) {

        Element model = createContentModel(contentSpec, node, 
                                           new DocumentImpl(), null);
        return model;

    } // createContentModel(XMLContentSpec.Node):Element

    /**
     * This is the real <em>createContentModel</em> method. This is a
     * recursive solution.
     */
    private Element createContentModel(XMLContentSpec contentSpec,
                                       XMLContentSpec.Node node, 
                                       Document factory, 
                                       Element parent) {

        // figure out occurrence count
        int minOccur = 1;
        int maxOccur = 1;
        switch (node.type) {
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                minOccur = 1;
                maxOccur = -1;
                contentSpec.getNode(node.value, node);
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: {
                minOccur = 0;
                maxOccur = -1;
                contentSpec.getNode(node.value, node);
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE: {
                minOccur = 0;
                maxOccur = 1;
                contentSpec.getNode(node.value, node);
                break;
            }
        }

        // flatten model
        int nodeType = node.type;
        switch (nodeType) {

            // CHOICE or SEQUENCE
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
            case XMLContentSpec.CONTENTSPECNODE_SEQ: {

                // go down left side
                int leftIndex  = node.value;
                int rightIndex = node.otherValue;
                contentSpec.getNode(leftIndex, node);
                Element left = createContentModel(contentSpec, node, 
                                                  factory, parent);

                // go down right side
                contentSpec.getNode(rightIndex, node);
                Element right = createContentModel(contentSpec, node, 
                                                   factory, null);

                // append left children
                String  type  = nodeType == XMLContentSpec.CONTENTSPECNODE_CHOICE
                              ? "choice"
                              : "seq";
                Element model = left;
                if (!left.getAttribute("order").equals(type)) {
                    String minOccurs = left.getAttribute("minOccurs");
                    String maxOccurs = left.getAttribute("maxOccurs");
                    if (parent == null ||
                        ((minOccurs.equals("1") || minOccurs.length() == 0) &&
                        (maxOccurs.equals("1") || maxOccurs.length() == 0))) {
                        model = factory.createElement("group");
                        model.setAttribute("order", type);
                        if (type.equals("seq")) {
                            ((AttrImpl)model.getAttributeNode("order")).setSpecified(false);
                        }
                        model.appendChild(left);
                    }
                    else {
                        model = parent;
                    }
                }

                // set occurrence count
                setOccurrenceCount(model, minOccur, maxOccur);

                // append right children
                model.appendChild(right);

                // return model
                return model;
            }

            // LEAF
            case XMLContentSpec.CONTENTSPECNODE_LEAF: {
                String  name = fStringPool.toString(node.value);
                Element leaf = factory.createElement("element");
                leaf.setAttribute("ref", name);

                // set occurrence count and return
                setOccurrenceCount(leaf, minOccur, maxOccur);
                return leaf;
            }

        } // switch node type

        // error
        return null;

    } // createContentModel(XMLContentSpec.Node,Element):Element

    /**
     * Sets the appropriate occurrence count attributes on the specified
     * model element.
     */
    private void setOccurrenceCount(Element model, int minOccur, int maxOccur) {

        // min
        model.setAttribute("minOccurs", Integer.toString(minOccur));
        if (minOccur == 1) {
            ((AttrImpl)model.getAttributeNode("minOccurs")).setSpecified(false);
        }

        // max
        if (maxOccur == -1) {
            model.setAttribute("maxOccurs", "*");
        }
        else if (maxOccur != 1) {
            model.setAttribute("maxOccurs", Integer.toString(maxOccur));
        }

    } // setOccurrenceCount(Element,int,int)

    /** Returns the first child element of the specified node. */
    private int getFirstChildElement(int nodeIndex) {
        int childIndex = fDeferredDocumentImpl.getFirstChild(nodeIndex, false);
        while (childIndex != -1) {
            if (fDeferredDocumentImpl.getNodeType(childIndex, false) == Node.ELEMENT_NODE) {
                return childIndex;
            }
            childIndex = fDeferredDocumentImpl.getNextSibling(childIndex, false);
        }
        return -1;
    }

    /** Returns the next sibling element of the specified node. */
    private int getNextSiblingElement(int nodeIndex) {
        int siblingIndex = fDeferredDocumentImpl.getNextSibling(nodeIndex, false);
        while (siblingIndex != -1) {
            if (fDeferredDocumentImpl.getNodeType(siblingIndex, false) == Node.ELEMENT_NODE) {
                return siblingIndex;
            }
            siblingIndex = fDeferredDocumentImpl.getNextSibling(siblingIndex, false);
        }
        return -1;
    }

    /** Returns the first child element with the given name. */
    private int getFirstChildElement(int nodeIndex, String elementName) {
        int childIndex = getFirstChildElement(nodeIndex);
        if (childIndex != -1) {
            while (childIndex != -1) {
                String nodeName = fDeferredDocumentImpl.getNodeNameString(childIndex, false);
                if (nodeName.equals(elementName)) {
                    return childIndex;
                }
                childIndex = getNextSiblingElement(childIndex);
            }
        }
        return -1;
    }

    /** Returns the next sibling element with the given name. */
    private int getNextSiblingElement(int nodeIndex, String elementName) {
        int siblingIndex = getNextSiblingElement(nodeIndex);
        if (siblingIndex != -1) {
            while (siblingIndex != -1) {
                String nodeName = fDeferredDocumentImpl.getNodeNameString(siblingIndex, false);
                if (nodeName.equals(elementName)) {
                    return siblingIndex;
                }
                siblingIndex = getNextSiblingElement(siblingIndex);
            }
        }
        return -1;
    }

    /** Returns the first child element with the given name. */
    private int getFirstChildElement(int nodeIndex, String elemName, String attrName, String attrValue) {
        int childIndex = getFirstChildElement(nodeIndex, elemName);
        if (childIndex != -1) {
            while (childIndex != -1) {
                int attrIndex = fDeferredDocumentImpl.getNodeValue(childIndex, false);
                while (attrIndex != -1) {
                    String nodeName = fDeferredDocumentImpl.getNodeNameString(attrIndex, false);
                    if (nodeName.equals(attrName)) {
                        // REVISIT: Do we need to normalize the text? -Ac
                        int textIndex = fDeferredDocumentImpl.getFirstChild(attrIndex, false);
                        String nodeValue = fDeferredDocumentImpl.getNodeValueString(textIndex, false);
                        if (nodeValue.equals(attrValue)) {
                            return childIndex;
                        }
                    }
                    attrIndex = fDeferredDocumentImpl.getNextSibling(attrIndex, false);
                }
                childIndex = getNextSiblingElement(childIndex, elemName);
            }
        }
        return -1;
    }

    /** Returns the next sibling element with the given name and attribute. */
    private int getNextSiblingElement(int nodeIndex, String elemName, String attrName, String attrValue) {
        int siblingIndex = getNextSiblingElement(nodeIndex, elemName);
        if (siblingIndex != -1) {
            int attributeNameIndex = fStringPool.addSymbol(attrName);
            while (siblingIndex != -1) {
                int attrIndex = fDeferredDocumentImpl.getNodeValue(siblingIndex, false);
                while (attrIndex != -1) {
                    int attrValueIndex = fDeferredDocumentImpl.getNodeValue(attrIndex, false);
                    if (attrValue.equals(fStringPool.toString(attrValueIndex))) {
                        return siblingIndex;
                    }
                    attrIndex = fDeferredDocumentImpl.getNextSibling(attrIndex, false);
                }
                siblingIndex = getNextSiblingElement(siblingIndex, elemName);
            }
        }
        return -1;
    }

    /**
     * Copies the source tree into the specified place in a destination
     * tree. The source node and its children are appended as children
     * of the destination node.
     * <p>
     * <em>Note:</em> This is an iterative implementation.
     */
    private void copyInto(Node src, int destIndex) throws Exception {

        // for ignorable whitespace features
        boolean domimpl = src != null && src instanceof DocumentImpl;

        // placement variables
        Node start  = src;
        Node parent = src;
        Node place  = src;

        // traverse source tree
        while (place != null) {

            // copy this node
            int nodeIndex = -1;
            short type = place.getNodeType();
            switch (type) {
                case Node.CDATA_SECTION_NODE: {
                    boolean ignorable = domimpl && ((TextImpl)place).isIgnorableWhitespace();
                    nodeIndex = fDeferredDocumentImpl.createCDATASection(fStringPool.addString(place.getNodeValue()), ignorable);
                    break;
                }
                case Node.COMMENT_NODE: {
                    nodeIndex = fDeferredDocumentImpl.createComment(fStringPool.addString(place.getNodeValue()));
                    break;
                }
                case Node.ELEMENT_NODE: {
                    XMLAttrList attrList = null;
                    int handle = -1;
                    NamedNodeMap attrs = place.getAttributes();
                    if (attrs != null) {
                        int length = attrs.getLength();
                        if (length > 0) {
                            handle = fAttrList.startAttrList();
                            for (int i = 0; i < length; i++) {
                                Attr attr = (Attr)attrs.item(i);
                                String attrName = attr.getNodeName();
                                String attrValue = attr.getNodeValue();
                                fAttrList.addAttr(
                                    fStringPool.addSymbol(attrName),
                                    fStringPool.addString(attrValue),
                                    fStringPool.addSymbol("CDATA"), // REVISIT
                                    attr.getSpecified(),
                                    false); // search
                            }
                            fAttrList.endAttrList();
                            attrList = fAttrList;
                        }
                    }
                    nodeIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol(place.getNodeName()), attrList, handle);
                    break;
                }
                case Node.ENTITY_REFERENCE_NODE: {
                    nodeIndex = fDeferredDocumentImpl.createEntityReference(fStringPool.addSymbol(place.getNodeName()));
                    break;
                }
                case Node.PROCESSING_INSTRUCTION_NODE: {
                    nodeIndex = fDeferredDocumentImpl.createProcessingInstruction(fStringPool.addSymbol(place.getNodeName()), fStringPool.addString(place.getNodeValue()));
                    break;
                }
                case Node.TEXT_NODE: {
                    boolean ignorable = domimpl && ((TextImpl)place).isIgnorableWhitespace();
                    nodeIndex = fDeferredDocumentImpl.createTextNode(fStringPool.addString(place.getNodeValue()), ignorable);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("PAR010 Can't copy node type, "+
                                                       type+" ("+
                                                       place.getNodeName()+')'
                                                       +"\n"+type+"\t"+place.getNodeName());
                }
            }
            fDeferredDocumentImpl.appendChild(destIndex, nodeIndex);

            // iterate over children
            if (place.hasChildNodes()) {
                parent = place;
                place = place.getFirstChild();
                destIndex = nodeIndex;
            }

            // advance
            else {
                place = place.getNextSibling();
                while (place == null && parent != start) {
                    place = parent.getNextSibling();
                    parent = parent.getParentNode();
                    destIndex = fDeferredDocumentImpl.getParentNode(destIndex, false);
                }
            }

        }

    } // copyInto(Node,int)

    /** Creates the content model elements for the deferred DOM tree. */
    private int createDeferredContentModel(Node model) throws Exception {

        int nodeType = model.getNodeType();
        switch (nodeType) {
            case Node.ELEMENT_NODE: {
                NamedNodeMap attrs = model.getAttributes();
                int handle = fAttrList.startAttrList();
                int length = attrs.getLength();
                for (int i = 0; i < length; i++) {
                    Attr attr = (Attr)attrs.item(i);
                    String attrName = attr.getNodeName();
                    String attrValue = attr.getNodeValue();

                    fAttrList.addAttr(
                        fStringPool.addSymbol(attrName),
                        fStringPool.addString(attrValue),
                        fStringPool.addSymbol((String)TYPES.get(attrName)),
                        attr.getSpecified(),
                        false); // search
                }
                fAttrList.endAttrList();

                int modelIndex = fDeferredDocumentImpl.createElement(fStringPool.addSymbol(model.getNodeName()), fAttrList, handle);

                Node child = model.getFirstChild();
                while (child != null) {
                    int childIndex = createDeferredContentModel(child);
                    fDeferredDocumentImpl.appendChild(modelIndex, childIndex);
                    child = child.getNextSibling();
                }

                return modelIndex;
            }
            case Node.TEXT_NODE: {
                return fDeferredDocumentImpl.createTextNode(fStringPool.addString(model.getNodeValue()), false);
            }
        }

        return -1;

    } // createDeferredContentModel(Node):int

} // class DOMParser
