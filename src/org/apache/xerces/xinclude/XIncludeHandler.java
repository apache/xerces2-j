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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.xinclude;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dtd.DTDGrammar;
import org.apache.xerces.impl.dtd.XMLDTDDescription;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.ObjectFactory;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;

/**
 * @author Peter McCracken, IBM
 */
public class XIncludeHandler implements XMLComponent, XMLDocumentFilter {

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
    private final static Integer STATE_NORMAL_PROCESSING = new Integer(1);
    private final static Integer STATE_IGNORE = new Integer(2);
    private final static Integer STATE_EXPECT_FALLBACK = new Integer(3);

    // recognized features and properties

    /** Property identifier: error handler. */
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;

    /** Property identifier: grammar pool . */
    protected static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    /** Recognized features. */
    private static final String[] RECOGNIZED_FEATURES = {
    };

    /** Feature defaults. */
    private static final Boolean[] FEATURE_DEFAULTS = {
    };

    /** Recognized properties. */

    // TODO: make DEFAULT_XINCLUDE_PIPELINE a property?
    private static final String[] RECOGNIZED_PROPERTIES =
        { ERROR_REPORTER, GRAMMAR_POOL };

    /** Property defaults. */
    private static final Object[] PROPERTY_DEFAULTS = { null, null, null, };

    // Data

    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDocumentSource fDocumentSource;

    protected XIncludeHandler fParentXIncludeHandler;

    protected XMLLocator fDocLocation;
    protected XIncludeNamespaceSupport fNamespaceContext;
    protected XMLErrorReporter fErrorReporter;
    protected XMLGrammarPool fGrammarPool;
    protected XMLGrammarDescription fGrammarDesc;
    protected DTDGrammar fDTDGrammar;

    // The current element depth.
    // This is used to access the appropriate level of the following stacks.
    private int fDepth;

    // The depth of the first element to actually be part of the result infoset.
    // This will normally be 1, but it could be larger when the top-level item
    // is an include, and processing goes to the fallback.
    private int fRootDepth;

    // TODO: for performance, change these to expanding arrays

    // Used to ensure that fallbacks are always children of include elements,
    // and that include elements are never children of other include elements.
    // An index contains true if the ancestor of the current element which resides
    // at that depth was an include element.
    private Vector fSawInclude;

    // Ensures that only one fallback element can be at a single depth.
    // An index contains true if we have seen any fallback elements at that depth,
    // and it is only reset to false when the end tag of the parent is encountered.
    private Vector fSawFallback;

    // The state of the processor at each given depth.
    private Vector fState;

    // Constructors

    public XIncludeHandler() {
        this(null);
    }

    private XIncludeHandler(XIncludeHandler parent) {
        fParentXIncludeHandler = parent;

        fDepth = 0;
        fRootDepth = 0;

        fSawFallback = new Vector();
        fSawFallback.add(Boolean.FALSE);
        fSawInclude = new Vector();
        fSawInclude.add(Boolean.FALSE);
        fState = new Vector();
        fState.add(STATE_NORMAL_PROCESSING);
    }

    // XMLComponent methods

    public void reset(XMLComponentManager componentManager)
        throws XNIException {
        fNamespaceContext = null;
        fDepth = 0;
        fRootDepth = 0;

        try {
            setErrorReporter(
                (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER));
        }
        catch (XMLConfigurationException e) {
            fErrorReporter = null;
        }
        try {
            fGrammarPool =
                (XMLGrammarPool)componentManager.getProperty(GRAMMAR_POOL);
        }
        catch (XMLConfigurationException e) {
            fGrammarPool = null;
        }
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
        }
        if (propertyId.equals(GRAMMAR_POOL)) {
            fGrammarPool = (XMLGrammarPool)value;
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

    private void setErrorReporter(XMLErrorReporter reporter) {
        fErrorReporter = reporter;
        if (fErrorReporter != null) {
            fErrorReporter.putMessageFormatter(
                XIncludeMessageFormatter.XINCLUDE_DOMAIN,
                new XIncludeMessageFormatter());
        }
    }

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

        if (!isRootDocument()
            && fParentXIncludeHandler.searchForRecursiveIncludes(locator)) {
            throw new XIncludeFatalError("RecursiveInclude", null);
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

        String eid = null;
        try {
            eid =
                XMLEntityManager.expandSystemId(
                    systemId,
                    fDocLocation.getExpandedSystemId(),
                    false);
        }
        catch (java.io.IOException e) {
        }

        fGrammarDesc =
            new XMLDTDDescription(
                publicId,
                systemId,
                fDocLocation.getExpandedSystemId(),
                eid,
                rootElement);

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
            reportFatalError("IncludeParent");
        }
        if (getState() == STATE_IGNORE)
            return;

        String href = attributes.getValue(XINCLUDE_ATTR_HREF);

        XMLInputSource includedSource = null;
        if (href == null) {
            reportFatalError("HrefMissing");
        }

        includedSource =
            new XMLInputSource(null, href, fDocLocation.getBaseSystemId());

        String parse = attributes.getValue(XINCLUDE_ATTR_PARSE);
        if (parse == null) {
            parse = XINCLUDE_PARSE_XML;
        }

        if (parse.equals(XINCLUDE_PARSE_XML)) {
            // create pipeline with no schema validator
            // TODO: implement DEFAULT_XINCLUDE_PROPERTY?
            String parserName = XINCLUDE_DEFAULT_CONFIGURATION;
            XMLParserConfiguration parserConfig =
                (XMLParserConfiguration)ObjectFactory.newInstance(
                    parserName,
                    ObjectFactory.findClassLoader(),
                    true);

            // TODO: set all features on parserConfig to match this parser configuration

            // we don't want a schema validator on the new pipeline
            parserConfig.setFeature(
                Constants.XERCES_FEATURE_PREFIX
                    + Constants.SCHEMA_VALIDATION_FEATURE,
                false);

            // use the same error reporter
            parserConfig.setProperty(ERROR_REPORTER, fErrorReporter);
            // use the same namespace context
            parserConfig.setProperty(
                Constants.XERCES_PROPERTY_PREFIX
                    + Constants.NAMESPACE_CONTEXT_PROPERTY,
                fNamespaceContext);

            XIncludeHandler newHandler =
                (XIncludeHandler)parserConfig.getProperty(
                    Constants.XERCES_PROPERTY_PREFIX
                        + Constants.XINCLUDE_HANDLER_PROPERTY);
            newHandler.setParent(this);
            newHandler.setDocumentHandler(this.getDocumentHandler());

            try {
                fNamespaceContext.pushScope();

                parserConfig.parse(includedSource);
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
        // TODO: fix parsing as text.
        // Suggest writing a ParserConfiguration with an appropriate Scanner,
        // so that it will be parallel to how we treat parsing as XML
        else if (parse.equals(XINCLUDE_PARSE_TEXT)) {
            // TODO: This is what the spec says on encoding. Make sure it is being done.
            //     The encoding of such a resource is determined by:
            //         * external encoding information, if available, otherwise
            //         * if the media type of the resource is text/xml, application/xml, or matches the conventions text/*+xml or application/*+xml as described in XML Media Types [IETF RFC 3023], the encoding is recognized as specified in XML 1.0, otherwise
            //         * the value of the encoding attribute if one exists, otherwise
            //         * UTF-8.
            //     Byte sequences outside the range allowed by the encoding are a fatal error. Characters that are not permitted in XML documents also are a fatal error.

            // encoding only matters when parse="text"
            String encoding = attributes.getValue(XINCLUDE_ATTR_ENCODING);
            if (encoding != null) {
                includedSource.setEncoding(encoding);
            }

            String systemId = includedSource.getSystemId();
            InputStreamReader reader = null;
            try {
                URL url = this.createURL(includedSource);
                if (encoding != null) {
                    reader = new InputStreamReader(url.openStream(), encoding);
                }
                else {
                    reader = new InputStreamReader(url.openStream(), "UTF-8");
                }

                XMLStringBuffer buffer = new XMLStringBuffer();
                while (reader.ready()) {
                    buffer.append((char)reader.read());
                }
                if (fDocumentHandler != null) {
                    fDocumentHandler.characters(
                        buffer,
                        modifyAugmentations(null, true));
                }
                reader.close();
            }
            catch (IOException e) {
                throw new XIncludeResourceError(
                    "TextResourceError",
                    new Object[] { e.getMessage()});
            }
        }
        else {
            reportFatalError("InvalidParseValue", new Object[] { parse });
        }
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

    protected boolean isIncludeElement(QName element) {
        return element.localpart.equals(XINCLUDE_INCLUDE)
            && fNamespaceContext.getURI(element.prefix).equals(XINCLUDE_NS_URI);
    }

    protected boolean isFallbackElement(QName element) {
        return element.localpart.equals(XINCLUDE_FALLBACK)
            && fNamespaceContext.getURI(element.prefix).equals(XINCLUDE_NS_URI);
    }

    protected boolean sameBaseURISourceAsParent() {
        if (fDepth == fRootDepth) {
            try {
                // We test if the included file is in the same directory as it's parent
                // by creating a new URL, with the filename of the included file and the
                // base of the parent, and checking if it is the same file as the included file
                URL input = createURL(fDocLocation);
                URL parent = createURL(fParentXIncludeHandler.fDocLocation);
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
                reportFatalError("ExpandedSystemId");
                // returning false opens the possibility of attempting an infinite
                // recursive parse, in the case when:
                //   we can't resolve the system id
                //   AND continue-after-fatal-error is set
                //   AND there is actually a recursive parse.
                // But that's probably a pretty slim chance.
                return false;
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
                int index =
                    attributes.addAttribute(
                        XML_BASE_QNAME,
                        XMLSymbols.fCDATASymbol,
                        this.fDocLocation.getBaseSystemId());
                attributes.setSpecified(index, true);
            }

            // Modify attributes of included items to do namespace-fixup. (spec 4.5.4)
            Enumeration inscopeNS = fNamespaceContext.getAllPrefixes();
            while (inscopeNS.hasMoreElements()) {
                String prefix = (String)inscopeNS.nextElement();
                String parentURI =
                    fNamespaceContext.getURIFromIncludeParent(prefix);
                String uri = fNamespaceContext.getURI(prefix);
                if (attributes.getValue(NamespaceContext.XMLNS_URI, prefix)
                    == null
                    && parentURI != uri) {
                    QName ns = (QName)NEW_NS_ATTR_QNAME.clone();
                    ns.localpart = prefix;
                    ns.rawname += prefix;
                    attributes.addAttribute(ns, XMLSymbols.fCDATASymbol, uri);
                }
            }
        }

        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                String type = attributes.getType(i);
                String value = attributes.getValue(i);
                if (checkGrammar()) {
                    // TODO: 4.5.1
                    if (type == XMLSymbols.fENTITYSymbol) {

                    }
                    if (type == XMLSymbols.fENTITIESSymbol) {

                    }
                    else if (type == XMLSymbols.fNOTATIONSymbol) {
                        // TODO: 4.5.2
                        //       Obtain [notations] property from DTD grammar
                        // TODO: 4.5.2 -- don't forget about searching unparsed entities for notations, too
                    }
                }
                /* We actually don't need to do anything for 4.5.3, because at this stage the
                 * value of the attribute is just a string. It will be taken care of later
                 * in the pipeline, when the IDREFs are actually resolved against elements.
                 *
                 * TODO: what about when XInclude processing comes after schema validation? 
                 * 
                 * if (type == XMLSymbols.fIDREFSymbol
                 *    || type == XMLSymbols.fIDREFSSymbol) {
                 *    // look at ValidationState for IDREFs (it should be a property)
                 * }
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

    protected Integer getState(int val) {
        return (Integer)this.fState.elementAt(val);
    }

    protected Integer getState() {
        return this.getState(fDepth);
    }

    protected void setState(Integer state) {
        while (fDepth >= fState.size()) {
            this.fState.add(STATE_NORMAL_PROCESSING);
        }
        this.fState.set(fDepth, state);
    }

    protected void setSawFallback(int depth, boolean val) {
        this.setSawFallback(depth, val ? Boolean.TRUE : Boolean.FALSE);
    }

    protected void setSawFallback(int depth, Boolean val) {
        while (depth >= fSawFallback.size()) {
            this.fSawFallback.add(Boolean.FALSE);
        }
        this.fSawFallback.set(depth, val);
    }

    protected boolean getSawFallback(int depth) {
        if (depth >= this.fSawFallback.size()) {
            return false;
        }
        return ((Boolean)this.fSawFallback.elementAt(depth)).booleanValue();
    }

    protected void setSawInclude(int depth, boolean val) {
        this.setSawInclude(depth, val ? Boolean.TRUE : Boolean.FALSE);
    }

    protected void setSawInclude(int depth, Boolean val) {
        while (depth >= fSawInclude.size()) {
            this.fSawInclude.add(Boolean.FALSE);
        }
        this.fSawInclude.set(depth, val);
    }

    protected boolean getSawInclude(int depth) {
        if (depth >= this.fSawInclude.size()) {
            return false;
        }
        return ((Boolean)this.fSawInclude.elementAt(depth)).booleanValue();
    }

    // TODO: this method really doesn't belong here; it's only temporary to make text include work
    //       see comment about XINCLUDE_PARSE_TEXT
    // TODO: does Java use IURIs by default?
    //       [Definition: An internationalized URI reference, or IURI, is a URI reference that directly uses [Unicode] characters.]
    // TODO: figure out what section 4.1.1 of the XInclude spec is talking about
    //       has to do with disallowed ASCII character escaping
    //       this ties in with the above IURI section, but I suspect Java already does it
    private URL createURL(XMLLocator source) throws MalformedURLException {
        return new URL(
            new URL(source.getBaseSystemId()),
            source.getExpandedSystemId());
    }

    private URL createURL(XMLInputSource source) throws MalformedURLException {
        return new URL(new URL(source.getBaseSystemId()), source.getSystemId());
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

    protected void setParent(XIncludeHandler parent) {
        this.fParentXIncludeHandler = parent;
    }

    // used to know whether to pass declarations to the document handler
    protected boolean isRootDocument() {
        return this.fParentXIncludeHandler == null;
    }

    private boolean checkGrammar() {
        /*        System.err.println("checking grammar");
                if (fDTDGrammar == null) {
                    System.err.println("grammar is null");
                    if (fGrammarDesc == null || fGrammarPool == null) {
                        System.err.println("bailed out");
                        return false;
                    }
                    fDTDGrammar =
                        (DTDGrammar)fGrammarPool.retrieveGrammar(fGrammarDesc);
                    System.err.println(fDTDGrammar);
                    return fDTDGrammar != null;
                }
                else {
                    System.err.println("have grammar!");
                    return true;
                }
        */
        return false;
    }
} // class XIncludeHandler