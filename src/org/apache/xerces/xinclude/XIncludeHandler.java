/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2003, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.xinclude;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.ObjectFactory;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDFilter;
import org.apache.xerces.xni.parser.XMLDTDSource;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * @author Peter McCracken, IBM
 */
public class XIncludeHandler
    implements XMLComponent, XMLDocumentFilter, XMLDTDFilter {

    public final static String XINCLUDE_DEFAULT_CONFIGURATION =
        "org.apache.xerces.parsers.XIncludeParserConfiguration";

    public final static String XINCLUDE_NS_URI =
        "http://www.w3.org/2001/XInclude".intern();
    public final static String XINCLUDE_INCLUDE = "include".intern();
    public final static String XINCLUDE_FALLBACK = "fallback".intern();

    public final static String XINCLUDE_PARSE_XML = "xml".intern();
    public final static String XINCLUDE_PARSE_TEXT = "text".intern();

    public final static String XINCLUDE_ATTR_HREF = "href".intern();
    public final static String XINCLUDE_ATTR_PARSE = "parse".intern();
    public final static String XINCLUDE_ATTR_ENCODING = "encoding".intern();

    // Top Level Information Items have [included] property in infoset
    public final static String XINCLUDE_INCLUDED = "[included]".intern();

    // used for adding [base URI] attributes
    public final static String XINCLUDE_BASE = "base";
    public final static QName XML_BASE_QNAME =
        new QName(
            XMLSymbols.PREFIX_XML,
            XINCLUDE_BASE,
            XMLSymbols.PREFIX_XML + ":" + XINCLUDE_BASE,
            NamespaceContext.XML_URI);

    public final static QName NEW_NS_ATTR_QNAME =
        new QName(
            XMLSymbols.PREFIX_XMLNS,
            "",
            XMLSymbols.PREFIX_XMLNS + ":",
            NamespaceContext.XMLNS_URI);

    // Processing States
    private final static int STATE_NORMAL_PROCESSING = 1;
    private final static int STATE_IGNORE = 2;
    private final static int STATE_EXPECT_FALLBACK = 3;

    // recognized features and properties

    /** Feature identifier: allow notation and unparsed entity events to be sent out of order. */
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS =
        Constants.SAX_FEATURE_PREFIX
            + Constants.ALLOW_DTD_EVENTS_AFTER_ENDDTD_FEATURE;

    /** Property identifier: error reporter. */
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: grammar pool . */
    protected static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES =
        { ALLOW_UE_AND_NOTATION_EVENTS };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = { Boolean.TRUE };

    /** Recognized properties. */

    private static final String[] RECOGNIZED_PROPERTIES =
        { ERROR_REPORTER, ENTITY_RESOLVER };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = { null, null };

    // instance variables

    // for XMLDocumentFilter
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;

    // for XMLDTDFilter
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDSource fDTDSource;

    // for XIncludeHandler
    protected XIncludeHandler fParentXIncludeHandler;

    // for caching
    protected XMLParserConfiguration fChildConfig;

    protected XMLLocator fDocLocation;
    protected XIncludeNamespaceSupport fNamespaceContext;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityResolver fEntityResolver;

    // used for passing features on to child XIncludeHandler objects
    protected ParserConfigurationSettings fSettings;

    // The current element depth.
    // This is used to access the appropriate level of the following stacks.
    private int fDepth;

    // The depth of the first element to actually be part of the result infoset.
    // This will normally be 1, but it could be larger when the top-level item
    // is an include, and processing goes to the fallback.
    private int fRootDepth;

    // this value must be at least 1
    private static final int INITIAL_SIZE = 8;

    // Used to ensure that fallbacks are always children of include elements,
    // and that include elements are never children of other include elements.
    // An index contains true if the ancestor of the current element which resides
    // at that depth was an include element.
    private boolean[] fSawInclude = new boolean[INITIAL_SIZE];

    // Ensures that only one fallback element can be at a single depth.
    // An index contains true if we have seen any fallback elements at that depth,
    // and it is only reset to false when the end tag of the parent is encountered.
    private boolean[] fSawFallback = new boolean[INITIAL_SIZE];

    // The state of the processor at each given depth.
    private int[] fState = new int[INITIAL_SIZE];

    // buffering the necessary DTD events
    private Vector fNotations;
    private Vector fUnparsedEntities;

    private boolean fSendUEAndNotationEvents;

    // Constructors

    public XIncludeHandler() {
        fDepth = 0;
        fRootDepth = 0;

        fSawFallback[fDepth] = false;
        fSawInclude[fDepth] = false;
        fState[fDepth] = STATE_NORMAL_PROCESSING;
        fNotations = new Vector();
        fUnparsedEntities = new Vector();
    }

    // XMLComponent methods

    public void reset(XMLComponentManager componentManager)
        throws XNIException {
        fNamespaceContext = null;
        fDepth = 0;
        fRootDepth = 0;
        fNotations = new Vector();
        fUnparsedEntities = new Vector();

        for (int i = 0; i < fState.length; i++) {
            // these three arrays will always be the same length, so this is safe
            fSawFallback[i] = false;
            fSawInclude[i] = false;
            fState[i] = STATE_NORMAL_PROCESSING;
        }

        try {
            fSendUEAndNotationEvents =
                componentManager.getFeature(ALLOW_UE_AND_NOTATION_EVENTS);
            if (fChildConfig != null) {
                fChildConfig.setFeature(
                    ALLOW_UE_AND_NOTATION_EVENTS,
                    fSendUEAndNotationEvents);
            }
        }
        catch (XMLConfigurationException e) {
        }

        try {
            XMLErrorReporter value =
                (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
            if (value != null) {
                setErrorReporter(value);
                if (fChildConfig != null) {
                    // REVISIT: see setErrorReporter()
                    fChildConfig.setProperty(ERROR_REPORTER, value);
                }
            }
        }
        catch (XMLConfigurationException e) {
            fErrorReporter = null;
        }

        try {
            XMLEntityResolver value =
                (XMLEntityResolver)componentManager.getProperty(
                    ENTITY_RESOLVER);

            if (value != null) {
                fEntityResolver = value;
                if (fChildConfig != null) {
                    fChildConfig.setProperty(ENTITY_RESOLVER, value);
                }
            }
        }
        catch (XMLConfigurationException e) {
            fEntityResolver = null;
        }

        fSettings = new ParserConfigurationSettings();
        copyFeatures(componentManager, fSettings);
        // Don't reset fChildConfig -- we don't want it to share the same components.
        // It will be reset when it is actually used to parse something.
    } // reset(XMLComponentManager)

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return RECOGNIZED_FEATURES;
    } // getRecognizedFeatures():String[]

    /**
     * Sets the state of a feature. This method is called by the component
     * manager any time after reset when a feature changes state.
     * <p>
     * <strong>Note:</strong> Components should silently ignore features
     * that do not affect the operation of the component.
     *
     * @param featureId The feature identifier.
     * @param state     The state of the feature.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (featureId.equals(ALLOW_UE_AND_NOTATION_EVENTS)) {
            fSendUEAndNotationEvents = state;
        }
        if (fSettings != null) {
            fSettings.setFeature(featureId, state);
        }
    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return RECOGNIZED_PROPERTIES;
    } // getRecognizedProperties():String[]

    /**
     * Sets the value of a property. This method is called by the component
     * manager any time after reset when a property changes value.
     * <p>
     * <strong>Note:</strong> Components should silently ignore properties
     * that do not affect the operation of the component.
     *
     * @param propertyId The property identifier.
     * @param value      The value of the property.
     *
     * @throws SAXNotRecognizedException The component should not throw
     *                                   this exception.
     * @throws SAXNotSupportedException The component should not throw
     *                                  this exception.
     */
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (propertyId.equals(ERROR_REPORTER)) {
            setErrorReporter((XMLErrorReporter)value);
            if (fChildConfig != null) {
                // REVISIT: see setErrorReporter()
                fChildConfig.setProperty(propertyId, value);
            }
        }
        if (propertyId.equals(ENTITY_RESOLVER)) {
            fEntityResolver = (XMLEntityResolver)value;
            if (fChildConfig != null) {
                fChildConfig.setProperty(propertyId, value);
            }
        }
    } // setProperty(String,Object)

    /** 
     * Returns the default state for a feature, or null if this
     * component does not want to report a default value for this
     * feature.
     *
     * @param featureId The feature identifier.
     *
     * @since Xerces 2.2.0
     */
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } // getFeatureDefault(String):Boolean

    /** 
     * Returns the default state for a property, or null if this
     * component does not want to report a default value for this
     * property. 
     *
     * @param propertyId The property identifier.
     *
     * @since Xerces 2.2.0
     */
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } // getPropertyDefault(String):Object

    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    }

    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    }

    // XMLDocumentHandler methods

    public void startDocument(
        XMLLocator locator,
        String encoding,
        NamespaceContext namespaceContext,
        Augmentations augs)
        throws XNIException {

        try {
            if (!isRootDocument()
                && fParentXIncludeHandler.searchForRecursiveIncludes(locator)) {
                throw new XIncludeFatalError("RecursiveInclude", null);
            }
        }
        catch (XIncludeFatalError e) {
            reportFatalError(e.getKey(), e.getArgs());
        }

        if (!(namespaceContext instanceof XIncludeNamespaceSupport)) {
            throw new XIncludeFatalError("IncompatibleNamespaceContext", null);
        }
        fNamespaceContext = (XIncludeNamespaceSupport)namespaceContext;
        fDocLocation = locator;

        if (isRootDocument() && fDocumentHandler != null) {
            fDocumentHandler.startDocument(
                locator,
                encoding,
                namespaceContext,
                augs);
        }
    }

    public void xmlDecl(
        String version,
        String encoding,
        String standalone,
        Augmentations augs)
        throws XNIException {
        if (isRootDocument() && fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone, augs);
        }
    }

    public void doctypeDecl(
        String rootElement,
        String publicId,
        String systemId,
        Augmentations augs)
        throws XNIException {
        if (isRootDocument() && fDocumentHandler != null) {
            fDocumentHandler.doctypeDecl(rootElement, publicId, systemId, augs);
        }
    }

    public void comment(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDepth++;
            augs = modifyAugmentations(augs);
            fDocumentHandler.comment(text, augs);
            fDepth--;
        }
    }

    public void processingInstruction(
        String target,
        XMLString data,
        Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            // we need to change the depth like this so that modifyAugmentations() works
            fDepth++;
            augs = modifyAugmentations(augs);
            fDocumentHandler.processingInstruction(target, data, augs);
            fDepth--;
        }
    }

    public void startElement(
        QName element,
        XMLAttributes attributes,
        Augmentations augs)
        throws XNIException {
        fDepth++;
        setState(getState(fDepth - 1));
        if (isIncludeElement(element)) {
            try {
                this.handleIncludeElement(attributes);
                // since the above call returned, we've completed the include
                // ignore any fallback elements, or other children, of the include element
                setState(STATE_IGNORE);
            }
            catch (XIncludeResourceError e) {
                setState(STATE_EXPECT_FALLBACK);
            }
        }
        else if (isFallbackElement(element)) {
            this.handleFallbackElement();
        }
        else if (
            fDocumentHandler != null
                && getState() == STATE_NORMAL_PROCESSING) {
            if (fRootDepth < 1) {
                fRootDepth = fDepth;
            }
            augs = modifyAugmentations(augs);
            attributes = processAttributes(attributes);
            fDocumentHandler.startElement(element, attributes, augs);
        }
    }

    public void emptyElement(
        QName element,
        XMLAttributes attributes,
        Augmentations augs)
        throws XNIException {
        fDepth++;
        setState(getState(fDepth - 1));
        if (isIncludeElement(element)) {
            try {
                this.handleIncludeElement(attributes);
                setState(STATE_IGNORE);
            }
            catch (XIncludeResourceError e) {
                reportFatalError("NoFallback");
            }
        }
        else if (isFallbackElement(element)) {
            this.handleFallbackElement();
        }
        else if (hasXIncludeNamespace(element)) {
            if (getSawInclude(fDepth - 1)) {
                reportFatalError(
                    "IncludeChild",
                    new Object[] { element.rawname });
            }
        }
        else if (
            fDocumentHandler != null
                && getState() == STATE_NORMAL_PROCESSING) {
            if (fRootDepth < 1) {
                fRootDepth = fDepth;
            }
            augs = modifyAugmentations(augs);
            attributes = processAttributes(attributes);
            fDocumentHandler.emptyElement(element, attributes, augs);
            if (fRootDepth == fDepth) {
                fRootDepth = 0;
            }
        }
        fDepth--;
    }

    public void endElement(QName element, Augmentations augs)
        throws XNIException {

        if (isIncludeElement(element)) {
            // if we're ending an include element, and we were expecting a fallback
            // we check to see if the children of this include element contained a fallback
            if (getState() == STATE_EXPECT_FALLBACK
                && !getSawFallback(fDepth + 1)) {
                reportFatalError("NoFallback");
            }
        }
        if (isFallbackElement(element)) {
            // the state would have been set to normal processing if we were expecting the fallback element
            // now that we're done processing it, we should ignore all the other children of the include element
            if (getState() == STATE_NORMAL_PROCESSING) {
                setState(STATE_IGNORE);
            }
        }
        else if (
            fDocumentHandler != null
                && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.endElement(element, augs);
            if (fRootDepth == fDepth) {
                fRootDepth = 0;
            }
        }

        // reset the out of scope stack elements
        setSawFallback(fDepth + 1, false);
        setSawInclude(fDepth + 1, false);

        fDepth--;
    }

    public void startGeneralEntity(
        String name,
        XMLResourceIdentifier resId,
        String encoding,
        Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.startGeneralEntity(name, resId, encoding, augs);
        }
    }

    public void textDecl(String version, String encoding, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.textDecl(version, encoding, augs);
        }
    }

    public void endGeneralEntity(String name, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.endGeneralEntity(name, augs);
        }
    }

    public void characters(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            // we need to change the depth like this so that modifyAugmentations() works
            fDepth++;
            augs = modifyAugmentations(augs);
            fDocumentHandler.characters(text, augs);
            fDepth--;
        }
    }

    public void ignorableWhitespace(XMLString text, Augmentations augs)
        throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.ignorableWhitespace(text, augs);
        }
    }

    public void startCDATA(Augmentations augs) throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.startCDATA(augs);
        }
    }

    public void endCDATA(Augmentations augs) throws XNIException {
        if (fDocumentHandler != null
            && getState() == STATE_NORMAL_PROCESSING) {
            fDocumentHandler.endCDATA(augs);
        }
    }

    public void endDocument(Augmentations augs) throws XNIException {
        if (isRootDocument() && fDocumentHandler != null) {
            fDocumentHandler.endDocument(augs);
        }
    }

    public void setDocumentSource(XMLDocumentSource source) {
        fDocumentSource = source;
    }

    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    }

    // DTDHandler methods

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#attributeDecl(java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    public void attributeDecl(
        String elementName,
        String attributeName,
        String type,
        String[] enumeration,
        String defaultType,
        XMLString defaultValue,
        XMLString nonNormalizedDefaultValue,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.attributeDecl(
                elementName,
                attributeName,
                type,
                enumeration,
                defaultType,
                defaultValue,
                nonNormalizedDefaultValue,
                augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#elementDecl(java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void elementDecl(
        String name,
        String contentModel,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.elementDecl(name, contentModel, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endAttlist(org.apache.xerces.xni.Augmentations)
     */
    public void endAttlist(Augmentations augmentations) throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.endAttlist(augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endConditional(org.apache.xerces.xni.Augmentations)
     */
    public void endConditional(Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.endConditional(augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endDTD(org.apache.xerces.xni.Augmentations)
     */
    public void endDTD(Augmentations augmentations) throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.endDTD(augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endExternalSubset(org.apache.xerces.xni.Augmentations)
     */
    public void endExternalSubset(Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.endExternalSubset(augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endParameterEntity(java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void endParameterEntity(String name, Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.endParameterEntity(name, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#externalEntityDecl(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, org.apache.xerces.xni.Augmentations)
     */
    public void externalEntityDecl(
        String name,
        XMLResourceIdentifier identifier,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.externalEntityDecl(name, identifier, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#getDTDSource()
     */
    public XMLDTDSource getDTDSource() {
        return fDTDSource;
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#ignoredCharacters(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    public void ignoredCharacters(XMLString text, Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.ignoredCharacters(text, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#internalEntityDecl(java.lang.String, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    public void internalEntityDecl(
        String name,
        XMLString text,
        XMLString nonNormalizedText,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.internalEntityDecl(
                name,
                text,
                nonNormalizedText,
                augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#notationDecl(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, org.apache.xerces.xni.Augmentations)
     */
    public void notationDecl(
        String name,
        XMLResourceIdentifier identifier,
        Augmentations augmentations)
        throws XNIException {
        this.addNotation(name, identifier, augmentations);
        if (fDTDHandler != null) {
            fDTDHandler.notationDecl(name, identifier, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#setDTDSource(org.apache.xerces.xni.parser.XMLDTDSource)
     */
    public void setDTDSource(XMLDTDSource source) {
        fDTDSource = source;
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startAttlist(java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void startAttlist(String elementName, Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.startAttlist(elementName, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startConditional(short, org.apache.xerces.xni.Augmentations)
     */
    public void startConditional(short type, Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.startConditional(type, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startDTD(org.apache.xerces.xni.XMLLocator, org.apache.xerces.xni.Augmentations)
     */
    public void startDTD(XMLLocator locator, Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.startDTD(locator, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startExternalSubset(org.apache.xerces.xni.XMLResourceIdentifier, org.apache.xerces.xni.Augmentations)
     */
    public void startExternalSubset(
        XMLResourceIdentifier identifier,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.startExternalSubset(identifier, augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startParameterEntity(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void startParameterEntity(
        String name,
        XMLResourceIdentifier identifier,
        String encoding,
        Augmentations augmentations)
        throws XNIException {
        if (fDTDHandler != null) {
            fDTDHandler.startParameterEntity(
                name,
                identifier,
                encoding,
                augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#unparsedEntityDecl(java.lang.String, org.apache.xerces.xni.XMLResourceIdentifier, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void unparsedEntityDecl(
        String name,
        XMLResourceIdentifier identifier,
        String notation,
        Augmentations augmentations)
        throws XNIException {
        this.addUnparsedEntity(name, identifier, notation, augmentations);
        if (fDTDHandler != null) {
            fDTDHandler.unparsedEntityDecl(
                name,
                identifier,
                notation,
                augmentations);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLDTDSource#getDTDHandler()
     */
    public XMLDTDHandler getDTDHandler() {
        return fDTDHandler;
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLDTDSource#setDTDHandler(org.apache.xerces.xni.XMLDTDHandler)
     */
    public void setDTDHandler(XMLDTDHandler handler) {
        fDTDHandler = handler;
    }

    // XIncludeHandler methods

    private void setErrorReporter(XMLErrorReporter reporter) {
        // REVISIT:
        // This results in the incorrect location being displayed, because
        // the XMLLocator is shared across all of the files.
        //
        // Howver, we do want to share the error reporter,
        // since it might be something other than the default.
        //
        // This problem only surfaces when the error is reported
        // somewhere other than in XIncludeHandler, since the XInclude#reportFatalError()
        // method uses the right XMLLocator
        fErrorReporter = reporter;
        if (fErrorReporter != null) {
            fErrorReporter.putMessageFormatter(
                XIncludeMessageFormatter.XINCLUDE_DOMAIN,
                new XIncludeMessageFormatter());
        }
    }

    protected void handleFallbackElement() {
        setSawInclude(fDepth, false);
        fNamespaceContext.setContextInvalid();
        if (!getSawInclude(fDepth - 1)) {
            reportFatalError("FallbackParent");
        }

        if (getSawFallback(fDepth)) {
            reportFatalError("MultipleFallbacks");
        }
        else {
            setSawFallback(fDepth, true);
        }

        // either the state is STATE_EXPECT_FALLBACK or it's STATE_IGNORE
        // if we're ignoring, we want to stay ignoring. But if we're expecting this fallback element,
        // we want to signal that we should process the children
        if (getState() == STATE_EXPECT_FALLBACK) {
            setState(STATE_NORMAL_PROCESSING);
        }
    }

    protected void handleIncludeElement(XMLAttributes attributes)
        throws XNIException {
        setSawInclude(fDepth, true);
        fNamespaceContext.setContextInvalid();
        if (getSawInclude(fDepth - 1)) {
            reportFatalError("IncludeChild", new Object[] { XINCLUDE_INCLUDE });
        }
        if (getState() == STATE_IGNORE)
            return;

        String href = attributes.getValue(XINCLUDE_ATTR_HREF);
        String parse = attributes.getValue(XINCLUDE_ATTR_PARSE);

        if (href == null) {
            reportFatalError("HrefMissing");
        }
        if (parse == null) {
            parse = XINCLUDE_PARSE_XML;
        }

        XMLResourceIdentifier resourceIdentifier =
            new XMLResourceIdentifierImpl(
                null,
                href,
                fDocLocation.getBaseSystemId(),
                null);

        XMLInputSource includedSource = null;
        if (fEntityResolver != null) {
            try {
                includedSource =
                    fEntityResolver.resolveEntity(resourceIdentifier);
            }
            catch (IOException e) {
                throw new XIncludeResourceError(
                    "XMLResourceError",
                    new Object[] { e.getMessage()});
            }
        }

        if (includedSource == null) {
            includedSource =
                new XMLInputSource(null, href, fDocLocation.getBaseSystemId());
        }

        if (parse.equals(XINCLUDE_PARSE_XML)) {
            // Instead of always creating a new configuration, the first one can be reused
            if (fChildConfig == null) {
                String parserName = XINCLUDE_DEFAULT_CONFIGURATION;

                fChildConfig =
                    (XMLParserConfiguration)ObjectFactory.newInstance(
                        parserName,
                        ObjectFactory.findClassLoader(),
                        true);

                // use the same error reporter
                // REVISIT: see setErrorReporter()
                fChildConfig.setProperty(ERROR_REPORTER, fErrorReporter);
                // use the same namespace context
                fChildConfig.setProperty(
                    Constants.XERCES_PROPERTY_PREFIX
                        + Constants.NAMESPACE_CONTEXT_PROPERTY,
                    fNamespaceContext);

                XIncludeHandler newHandler =
                    (XIncludeHandler)fChildConfig.getProperty(
                        Constants.XERCES_PROPERTY_PREFIX
                            + Constants.XINCLUDE_HANDLER_PROPERTY);
                newHandler.setParent(this);
                newHandler.setDocumentHandler(this.getDocumentHandler());
            }

            // set all features on parserConfig to match this parser configuration
            copyFeatures(fSettings, fChildConfig);

            // we don't want a schema validator on the new pipeline, 
            // so we set it to false, regardless of what was copied above
            fChildConfig.setFeature(
                Constants.XERCES_FEATURE_PREFIX
                    + Constants.SCHEMA_VALIDATION_FEATURE,
                false);

            try {
                fNamespaceContext.pushScope();
                fChildConfig.parse(includedSource);
            }
            catch (XIncludeFatalError e) {
                reportFatalError(e.getKey(), e.getArgs());
            }
            catch (XNIException e) {
                reportFatalError("XMLParseError");
            }
            catch (IOException e) {
                throw new XIncludeResourceError(
                    "XMLResourceError",
                    new Object[] { e.getMessage()});
            }
            finally {
                fNamespaceContext.popScope();
            }
        }
        else if (parse.equals(XINCLUDE_PARSE_TEXT)) {
            String encoding = attributes.getValue(XINCLUDE_ATTR_ENCODING);
            includedSource.setEncoding(encoding);

            XIncludeTextReader reader = null;
            try {
                reader = new XIncludeTextReader(includedSource, this);
                reader.parse();
            }
            catch (IOException e) {
                throw new XIncludeResourceError(
                    "TextResourceError",
                    new Object[] { e.getMessage()});
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        throw new XIncludeResourceError(
                            "TextResourceError",
                            new Object[] { e.getMessage()});
                    }
                }
            }
        }
        else {
            reportFatalError("InvalidParseValue", new Object[] { parse });
        }
    }

    protected boolean hasXIncludeNamespace(QName element) {
        return element.uri == XINCLUDE_NS_URI
            || fNamespaceContext.getURI(element.prefix) == XINCLUDE_NS_URI;
    }

    protected boolean isIncludeElement(QName element) {
        return element.localpart.equals(XINCLUDE_INCLUDE)
            && hasXIncludeNamespace(element);
    }

    protected boolean isFallbackElement(QName element) {
        return element.localpart.equals(XINCLUDE_FALLBACK)
            && hasXIncludeNamespace(element);
    }

    protected boolean sameBaseURISourceAsParent() {
        if (fDepth == fRootDepth) {
            try {
                // We test if the included file is in the same directory as it's parent
                // by creating a new URL, with the filename of the included file and the
                // base of the parent, and checking if it is the same file as the included file
                // TODO: does Java use IURIs by default?
                //       [Definition: An internationalized URI reference, or IURI, is a URI reference that directly uses [Unicode] characters.]
                // TODO: figure out what section 4.1.1 of the XInclude spec is talking about
                //       has to do with disallowed ASCII character escaping
                //       this ties in with the above IURI section, but I suspect Java already does it
                URL input = new URL(fDocLocation.getExpandedSystemId());
                URL parent =
                    new URL(
                        fParentXIncludeHandler
                            .fDocLocation
                            .getExpandedSystemId());
                URL test = new URL(parent, new File(input.getFile()).getName());
                return input.sameFile(test);
            }
            catch (MalformedURLException e) {
                return false;
            }
        }
        return true;
    }

    protected boolean searchForRecursiveIncludes(XMLLocator includedSource) {
        String includedSystemId = includedSource.getExpandedSystemId();

        if (includedSystemId == null) {
            try {
                includedSystemId =
                    XMLEntityManager.expandSystemId(
                        includedSource.getLiteralSystemId(),
                        includedSource.getBaseSystemId(),
                        false);
            }
            catch (MalformedURIException e) {
                throw new XIncludeFatalError("ExpandedSystemId", null);
            }
        }

        if (includedSystemId.equals(fDocLocation.getExpandedSystemId())) {
            return true;
        }

        if (fParentXIncludeHandler == null) {
            return false;
        }
        return fParentXIncludeHandler.searchForRecursiveIncludes(
            includedSource);
    }

    protected boolean isTopLevelIncludedItem() {
        return isTopLevelIncludedItemViaInclude()
            || isTopLevelIncludedItemViaFallback();
    }

    protected boolean isTopLevelIncludedItemViaInclude() {
        return fDepth == 1 && !isRootDocument();
    }

    protected boolean isTopLevelIncludedItemViaFallback() {
        return getSawFallback(fDepth - 1);
    }

    /**
     * Processes the XMLAttributes object of startElement() calls.  Performs the following tasks:
     * <ul>
     * <li> If the element is a top level included item whose [base URI] is different from the
     * [base URI] of the include parent, then an xml:base attribute is added to specify the
     * true [base URI]
     * <li> For all namespace prefixes which are in-scope in an included item, but not in scope
     * in the include parent, a xmlns:prefix attribute is added
     * <li> For all attributes with a type of ENTITY, ENTITIES or NOTATIONS, the notations and
     * unparsed entities are processed as described in the spec, sections 4.5.1 and 4.5.2
     * </ul>
     * @param attributes
     * @return
     */
    protected XMLAttributes processAttributes(XMLAttributes attributes) {
        if (isTopLevelIncludedItem()) {
            // Modify attributes to fix the base URI (spec 4.5.5).
            // We only do it to top level included elements, which have a different
            // base URI than their include parent.
            if (!sameBaseURISourceAsParent()) {
                if (attributes == null) {
                    attributes = new XMLAttributesImpl();
                }

                // this causes errors with schema validation, since the schema doesn't specify that these elements can have an xml:base attribute
                // TODO: add a user option to turn this off?
                // TODO: [base URI] is still an open issue with the working group.
                //       They're deciding if xml:base should be added if the [base URI] is different in terms
                //       of resolving relative references, or if it should be added if they are different at all.
                //       Revisit this after a final decision has been made.
                // TODO: Output a relative URI instead of an absolute one.
                int index =
                    attributes.addAttribute(
                        XML_BASE_QNAME,
                        XMLSymbols.fCDATASymbol,
                        fDocLocation.getBaseSystemId());
                attributes.setSpecified(index, true);
            }

            // Modify attributes of included items to do namespace-fixup. (spec 4.5.4)
            Enumeration inscopeNS = fNamespaceContext.getAllPrefixes();
            while (inscopeNS.hasMoreElements()) {
                String prefix = (String)inscopeNS.nextElement();
                String parentURI =
                    fNamespaceContext.getURIFromIncludeParent(prefix);
                String uri = fNamespaceContext.getURI(prefix);
                if (parentURI != uri && attributes != null) {
                    if (prefix == XMLSymbols.EMPTY_STRING) {
                        if (attributes
                            .getValue(
                                NamespaceContext.XMLNS_URI,
                                XMLSymbols.PREFIX_XMLNS)
                            == null) {
                            if (attributes == null) {
                                attributes = new XMLAttributesImpl();
                            }

                            QName ns = (QName)NEW_NS_ATTR_QNAME.clone();
                            ns.localpart = XMLSymbols.PREFIX_XMLNS;
                            ns.rawname = XMLSymbols.PREFIX_XMLNS;
                            attributes.addAttribute(
                                ns,
                                XMLSymbols.fCDATASymbol,
                                uri);
                        }
                    }
                    else if (
                        attributes.getValue(NamespaceContext.XMLNS_URI, prefix)
                            == null) {
                        if (attributes == null) {
                            attributes = new XMLAttributesImpl();
                        }

                        QName ns = (QName)NEW_NS_ATTR_QNAME.clone();
                        ns.localpart = prefix;
                        ns.rawname += prefix;
                        attributes.addAttribute(
                            ns,
                            XMLSymbols.fCDATASymbol,
                            uri);
                    }
                }
            }
        }

        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String type = attributes.getType(i);
                String value = attributes.getValue(i);
                if (type == XMLSymbols.fENTITYSymbol) {
                    this.checkUnparsedEntity(value);
                }
                if (type == XMLSymbols.fENTITIESSymbol) {
                    // 4.5.1 - Unparsed Entities
                    StringTokenizer st = new StringTokenizer(value);
                    while (st.hasMoreTokens()) {
                        String entName = st.nextToken();
                        this.checkUnparsedEntity(entName);
                    }
                }
                else if (type == XMLSymbols.fNOTATIONSymbol) {
                    // 4.5.2 - Notations
                    this.checkNotation(value);
                }
                /* We actually don't need to do anything for 4.5.3, because at this stage the
                 * value of the attribute is just a string. It will be taken care of later
                 * in the pipeline, when the IDREFs are actually resolved against IDs.
                 * 
                 * if (type == XMLSymbols.fIDREFSymbol || type == XMLSymbols.fIDREFSSymbol) { }
                 */
            }
        }

        return attributes;
    }

    protected Augmentations modifyAugmentations(Augmentations augs) {
        return modifyAugmentations(augs, false);
    }

    protected Augmentations modifyAugmentations(
        Augmentations augs,
        boolean force) {
        if (force || isTopLevelIncludedItem()) {
            if (augs == null) {
                augs = new AugmentationsImpl();
            }
            augs.putItem(XINCLUDE_INCLUDED, Boolean.TRUE);
        }
        return augs;
    }

    protected int getState(int depth) {
        return fState[depth];
    }

    protected int getState() {
        return fState[fDepth];
    }

    protected void setState(int state) {
        if (fDepth >= fState.length) {
            int[] newarray = new int[fDepth * 2];
            System.arraycopy(fState, 0, newarray, 0, fState.length);
            fState = newarray;
        }
        fState[fDepth] = state;
    }

    protected void setSawFallback(int depth, boolean val) {
        if (depth >= fSawFallback.length) {
            boolean[] newarray = new boolean[depth * 2];
            System.arraycopy(fSawFallback, 0, newarray, 0, fSawFallback.length);
            fSawFallback = newarray;
        }
        fSawFallback[depth] = val;
    }

    protected boolean getSawFallback(int depth) {
        if (depth >= fSawFallback.length) {
            return false;
        }
        return fSawFallback[depth];
    }

    /**
     * Records that an &lt;include&gt; was encountered at the specified depth,
     * as an ancestor of the current item.
     * 
     * @param depth
     * @param val
     */
    protected void setSawInclude(int depth, boolean val) {
        if (depth >= fSawInclude.length) {
            boolean[] newarray = new boolean[depth * 2];
            System.arraycopy(fSawInclude, 0, newarray, 0, fSawInclude.length);
            fSawInclude = newarray;
        }
        fSawInclude[depth] = val;
    }

    /**
     * Return whether an &lt;include&gt; was encountered at the specified depth,
     * as an ancestor of the current item.
     * 
     * @param depth
     * @return
     */
    protected boolean getSawInclude(int depth) {
        if (depth >= fSawInclude.length) {
            return false;
        }
        return fSawInclude[depth];
    }

    protected void reportFatalError(String key) {
        this.reportFatalError(key, null);
    }

    protected void reportFatalError(String key, Object[] args) {
        if (fErrorReporter != null) {
            fErrorReporter.reportError(
                fDocLocation,
                XIncludeMessageFormatter.XINCLUDE_DOMAIN,
                key,
                args,
                XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        // we won't worry about when error reporter is null, since there should always be
        // at least the default error reporter
    }

    /**
     * Set the parent of this XIncludeHandler in the tree
     * @param parent
     */
    protected void setParent(XIncludeHandler parent) {
        fParentXIncludeHandler = parent;
    }

    // used to know whether to pass declarations to the document handler
    protected boolean isRootDocument() {
        return fParentXIncludeHandler == null;
    }

    /**
     * Caches an unparsed entity.
     * @param name the name of the unparsed entity
     * @param identifier the location of the unparsed entity
     * @param augmentations any Augmentations that were on the original unparsed entity declaration
     */
    protected void addUnparsedEntity(
        String name,
        XMLResourceIdentifier identifier,
        String notation,
        Augmentations augmentations) {
        UnparsedEntity ent = new UnparsedEntity();
        ent.name = name;
        ent.systemId = identifier.getLiteralSystemId();
        ent.publicId = identifier.getPublicId();
        ent.baseURI = identifier.getBaseSystemId();
        ent.notation = notation;
        ent.augmentations = augmentations;
        fUnparsedEntities.add(ent);
    }

    /**
     * Caches a notation.
     * @param name the name of the notation
     * @param identifier the location of the notation
     * @param augmentations any Augmentations that were on the original notation declaration
     */
    protected void addNotation(
        String name,
        XMLResourceIdentifier identifier,
        Augmentations augmentations) {
        Notation not = new Notation();
        not.name = name;
        not.systemId = identifier.getLiteralSystemId();
        not.publicId = identifier.getPublicId();
        not.baseURI = identifier.getBaseSystemId();
        not.augmentations = augmentations;
        fNotations.add(not);
    }

    /**
     * Checks if an UnparsedEntity with the given name was declared in the DTD of the document
     * for the current pipeline.  If so, then the notation for the UnparsedEntity is checked.
     * If that turns out okay, then the UnparsedEntity is passed to the root pipeline to
     * be checked for conflicts, and sent to the root DTDHandler.
     * 
     * @param entName the name of the UnparsedEntity to check
     */
    protected void checkUnparsedEntity(String entName) {
        UnparsedEntity ent = new UnparsedEntity();
        ent.name = entName;
        int index = fUnparsedEntities.indexOf(ent);
        if (index != -1) {
            ent = (UnparsedEntity)fUnparsedEntities.get(index);
            // first check the notation of the unparsed entity
            try {
                checkNotation(ent.notation);
            }
            catch (XIncludeFatalError e) {
                reportFatalError(e.getKey(), e.getArgs());
            }

            try {
                checkAndSendUnparsedEntity(ent);
            }
            catch (XIncludeFatalError e) {
                reportFatalError(e.getKey(), e.getArgs());
            }
        }
    }

    /**
     * Checks if a Notation with the given name was declared in the DTD of the document
     * for the current pipeline.  If so, that Notation is passed to the root pipeline to
     * be checked for conflicts, and sent to the root DTDHandler
     * 
     * @param notName the name of the Notation to check
     */
    protected void checkNotation(String notName) {
        Notation not = new Notation();
        not.name = notName;
        int index = fNotations.indexOf(not);
        if (index != -1) {
            not = (Notation)fNotations.get(index);
            try {
                checkAndSendNotation(not);
            }
            catch (XIncludeFatalError e) {
                reportFatalError(e.getKey(), e.getArgs());
            }
        }
    }

    /**
     * The purpose of this method is to check if an UnparsedEntity conflicts with a previously
     * declared entity in the current pipeline stack.  If there is no conflict, the
     * UnparsedEntity is sent by the root pipeline.
     * 
     * @param ent the UnparsedEntity to check for conflicts
     * @throws XIncludeFatalError if there is an UnparsedEntity conflict as described in 4.5.1
     */
    protected void checkAndSendUnparsedEntity(UnparsedEntity ent)
        throws XIncludeFatalError {
        if (isRootDocument()) {
            int index = fUnparsedEntities.indexOf(ent);
            if (index == -1) {
                // There is no unparsed entity with the same name that we have sent.
                // Calling unparsedEntityDecl() will add the entity to our local store,
                // and also send the unparsed entity to the DTDHandler
                XMLResourceIdentifier id =
                    new XMLResourceIdentifierImpl(
                        ent.publicId,
                        ent.systemId,
                        ent.baseURI,
                        null);
                this.addUnparsedEntity(
                    ent.name,
                    id,
                    ent.notation,
                    ent.augmentations);
                if (fSendUEAndNotationEvents && fDTDHandler != null) {
                    fDTDHandler.unparsedEntityDecl(
                        ent.name,
                        id,
                        ent.notation,
                        ent.augmentations);
                }
            }
            else {
                UnparsedEntity localEntity =
                    (UnparsedEntity)fUnparsedEntities.get(index);
                if (!ent.isDuplicate(localEntity)) {
                    throw new XIncludeFatalError(
                        "NonDuplicateUnparsedEntity",
                        new Object[] { ent.name });
                }
            }
        }
        else {
            fParentXIncludeHandler.checkAndSendUnparsedEntity(ent);
        }
    }

    /**
     * The purpose of this method is to check if a Notation conflicts with a previously
     * declared notation in the current pipeline stack.  If there is no conflict, the
     * Notation is sent by the root pipeline.
     * 
     * @param not the Notation to check for conflicts
     * @throws XIncludeFatalError if there is a Notation conflict as described in 4.5.2
     */
    protected void checkAndSendNotation(Notation not)
        throws XIncludeFatalError {
        if (isRootDocument()) {
            int index = fNotations.indexOf(not);
            if (index == -1) {
                // There is no notation with the same name that we have sent.
                XMLResourceIdentifier id =
                    new XMLResourceIdentifierImpl(
                        not.publicId,
                        not.systemId,
                        not.baseURI,
                        null);
                this.addNotation(not.name, id, not.augmentations);
                if (fSendUEAndNotationEvents && fDTDHandler != null) {
                    fDTDHandler.notationDecl(not.name, id, not.augmentations);
                }
            }
            else {
                Notation localNotation = (Notation)fNotations.get(index);
                if (!not.isDuplicate(localNotation)) {
                    throw new XIncludeFatalError(
                        "NonDuplicateNotation",
                        new Object[] { not.name });
                }
            }
        }
        else {
            fParentXIncludeHandler.checkAndSendNotation(not);
        }
    }

    // It would be nice if we didn't have to repeat code like this, but there's no interface that has
    // setFeature() and addRecognizedFeatures() that the objects have in common.
    protected void copyFeatures(
        XMLComponentManager from,
        ParserConfigurationSettings to) {
        Enumeration features = Constants.getXercesFeatures();
        copyFeatures1(features, Constants.XERCES_FEATURE_PREFIX, from, to);
        features = Constants.getSAXFeatures();
        copyFeatures1(features, Constants.SAX_FEATURE_PREFIX, from, to);
    }

    protected void copyFeatures(
        XMLComponentManager from,
        XMLParserConfiguration to) {
        Enumeration features = Constants.getXercesFeatures();
        copyFeatures1(features, Constants.XERCES_FEATURE_PREFIX, from, to);
        features = Constants.getSAXFeatures();
        copyFeatures1(features, Constants.SAX_FEATURE_PREFIX, from, to);
    }

    private void copyFeatures1(
        Enumeration features,
        String featurePrefix,
        XMLComponentManager from,
        ParserConfigurationSettings to) {
        while (features.hasMoreElements()) {
            String featureId = featurePrefix + (String)features.nextElement();

            to.addRecognizedFeatures(new String[] { featureId });

            try {
                to.setFeature(featureId, from.getFeature(featureId));
            }
            catch (XMLConfigurationException e) {
                // componentManager doesn't support this feature,
                // so we won't worry about it
            }
        }
    }

    private void copyFeatures1(
        Enumeration features,
        String featurePrefix,
        XMLComponentManager from,
        XMLParserConfiguration to) {
        while (features.hasMoreElements()) {
            String featureId = featurePrefix + (String)features.nextElement();
            boolean value = from.getFeature(featureId);

            try {
                to.setFeature(featureId, value);
            }
            catch (XMLConfigurationException e) {
                // componentManager doesn't support this feature,
                // so we won't worry about it
            }
        }
    }

    // This is a storage class to hold information about the notations.
    // We're not using XMLNotationDecl because we don't want to lose the augmentations.
    protected class Notation {
        public String name;
        public String systemId;
        public String baseURI;
        public String publicId;
        public Augmentations augmentations;

        // equals() returns true if two Notations have the same name.
        // Useful for searching Vectors for notations with the same name
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof Notation) {
                Notation other = (Notation)obj;
                return name.equals(other.name);
            }
            return false;
        }

        // from 4.5.2
        // Notation items with the same [name], [system identifier],
        // [public identifier], and [declaration base URI] are considered
        // to be duplicate
        public boolean isDuplicate(Object obj) {
            if (obj != null && obj instanceof Notation) {
                Notation other = (Notation)obj;
                return name.equals(other.name)
                    && (systemId == other.systemId
                        || (systemId != null && systemId.equals(other.systemId)))
                    && (publicId == other.publicId
                        || (publicId != null && publicId.equals(other.publicId)))
                    && (baseURI == other.baseURI
                        || (baseURI != null && baseURI.equals(other.baseURI)));
            }
            return false;
        }
    }

    // This is a storage class to hold information about the unparsed entities.
    // We're not using XMLEntityDecl because we don't want to lose the augmentations.
    protected class UnparsedEntity {
        public String name;
        public String systemId;
        public String baseURI;
        public String publicId;
        public String notation;
        public Augmentations augmentations;

        // equals() returns true if two UnparsedEntities have the same name.
        // Useful for searching Vectors for entities with the same name
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj instanceof UnparsedEntity) {
                UnparsedEntity other = (UnparsedEntity)obj;
                return name.equals(other.name);
            }
            return false;
        }

        // from 4.5.1:
        // Unparsed entity items with the same [name], [system identifier],
        // [public identifier], [declaration base URI], [notation name], and
        // [notation] are considered to be duplicate
        public boolean isDuplicate(Object obj) {
            if (obj != null && obj instanceof UnparsedEntity) {
                UnparsedEntity other = (UnparsedEntity)obj;
                return name.equals(other.name)
                    && (systemId == other.systemId
                        || (systemId != null && systemId.equals(other.systemId)))
                    && (publicId == other.publicId
                        || (publicId != null && publicId.equals(other.publicId)))
                    && (baseURI == other.baseURI
                        || (baseURI != null && baseURI.equals(other.baseURI)))
                    && (notation == other.notation
                        || (notation != null && notation.equals(other.notation)));
            }
            return false;
        }
    }
} // class XIncludeHandler