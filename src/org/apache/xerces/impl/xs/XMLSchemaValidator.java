/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.xs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.XMLConstants;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.RevalidationHandler;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.DatatypeException;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.EqualityHelper;
import org.apache.xerces.impl.dv.xs.TypeValidatorHelper;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.assertion.XSAssertConstants;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.identity.Selector;
import org.apache.xerces.impl.xs.identity.XPathMatcher;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.impl.xs.util.XS11TypeHelper;
import org.apache.xerces.util.AugmentationsImpl;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLSymbols;
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
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.AttributePSVI;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * The XML Schema validator. The validator implements a document
 * filter: receiving document events from the scanner; validating
 * the content and structure; augmenting the InfoSet, if applicable;
 * and notifying the parser of the information resulting from the
 * validation process.
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/validation</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 * </ul>
 *
 * @xerces.internal
 *
 * @author Sandy Gao IBM
 * @author Elena Litani IBM
 * @author Andy Clark IBM
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @version $Id$
 */
public class XMLSchemaValidator extends XMLSchemaValidatorBase implements XMLComponent, XMLDocumentFilter, RevalidationHandler {

    //
    // XMLComponent methods
    //

    /**
     * Returns a list of feature identifiers that are recognized by
     * this component. This method may return null if no features
     * are recognized by this component.
     */
    public String[] getRecognizedFeatures() {
        return (String[]) (RECOGNIZED_FEATURES.clone());
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
    public void setFeature(String featureId, boolean state) throws XMLConfigurationException {
    } // setFeature(String,boolean)

    /**
     * Returns a list of property identifiers that are recognized by
     * this component. This method may return null if no properties
     * are recognized by this component.
     */
    public String[] getRecognizedProperties() {
        return (String[]) (RECOGNIZED_PROPERTIES.clone());
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
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        if (propertyId.equals(ROOT_TYPE_DEF)) {
            if (value == null) {
                fRootTypeQName = null;
                fRootTypeDefinition = null;
            }
            else if (value instanceof javax.xml.namespace.QName) {
                fRootTypeQName = (javax.xml.namespace.QName) value;
                fRootTypeDefinition = null;
            }
            else {
                fRootTypeDefinition = (XSTypeDefinition) value;
                fRootTypeQName = null;
            }
        }
        else if (propertyId.equals(ROOT_ELEMENT_DECL)) {
            if (value == null) {
                fRootElementDeclQName = null;
                fRootElementDeclaration = null;
            }
            else if (value instanceof javax.xml.namespace.QName) {
                fRootElementDeclQName = (javax.xml.namespace.QName) value;
                fRootElementDeclaration = null;
            }
            else {
                fRootElementDeclaration = (XSElementDecl) value;
                fRootElementDeclQName = null;
            }
        }
        else if (propertyId.equals(XML_SCHEMA_VERSION)) {
            fSchemaLoader.setProperty(XML_SCHEMA_VERSION, value);
            fSchemaVersion = fSchemaLoader.getSchemaVersion();
            fXSConstraints = fSchemaLoader.getXSConstraints();
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                if (fIDContext == null) {
                    fIDContext = new IDContext();
                }
                fValidationState.setIDContext(fIDContext);
            }
            else {
                fValidationState.setIDContext(null);
                fValidationState.setDatatypeXMLVersion(Constants.XML_VERSION_1_0);
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

    //
    // XMLDocumentSource methods
    //

    /** Sets the document handler to receive information about the document. */
    public void setDocumentHandler(XMLDocumentHandler documentHandler) {
        fDocumentHandler = documentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    /** Returns the document handler */
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    } // setDocumentHandler(XMLDocumentHandler)

    //
    // XMLDocumentHandler methods
    //

    /** Sets the document source */
    public void setDocumentSource(XMLDocumentSource source) {
        fDocumentSource = source;
    } // setDocumentSource

    /** Returns the document source */
    public XMLDocumentSource getDocumentSource() {
        return fDocumentSource;
    } // getDocumentSource

    /**
     * The start of the document.
     *
     * @param locator The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param namespaceContext
     *                 The namespace context in effect at the
     *                 start of this document.
     *                 This object represents the current context.
     *                 Implementors of this class are responsible
     *                 for copying the namespace bindings from the
     *                 the current context (and its parent contexts)
     *                 if that information is important.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startDocument(
        XMLLocator locator,
        String encoding,
        NamespaceContext namespaceContext,
        Augmentations augs)
        throws XNIException {

        fValidationState.setNamespaceSupport(namespaceContext);        
        fState4XsiType.setNamespaceSupport(namespaceContext);
        fState4ApplyDefault.setNamespaceSupport(namespaceContext);
        fLocator = locator;

        handleStartDocument(locator, encoding);
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startDocument(locator, encoding, namespaceContext, augs);
        }
        
        fNamespaceContext = namespaceContext;        
        fAssertionValidator = new XSDAssertionValidator(this);

    } // startDocument(XMLLocator,String)

    /**
     * Notifies of the presence of an XMLDecl line in the document. If
     * present, this method will be called immediately following the
     * startDocument call.
     *
     * @param version    The XML version.
     * @param encoding   The IANA encoding name of the document, or null if
     *                   not specified.
     * @param standalone The standalone value, or null if not specified.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.xmlDecl(version, encoding, standalone, augs);
        }

        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            if (fDatatypeXMLVersion == null) {
                fValidationState.setDatatypeXMLVersion("1.0".equals(version)
                        ? Constants.XML_VERSION_1_0 : Constants.XML_VERSION_1_1);
            }
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
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void doctypeDecl(
        String rootElement,
        String publicId,
        String systemId,
        Augmentations augs)
        throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.doctypeDecl(rootElement, publicId, systemId, augs);
        }

    } // doctypeDecl(String,String,String)

    /**
     * The start of an element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {

        Augmentations modifiedAugs = handleStartElement(element, attributes, augs);
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startElement(element, attributes, modifiedAugs);
        }

    } // startElement(QName,XMLAttributes, Augmentations)
    

    /**
     * An empty element.
     *
     * @param element    The name of the element.
     * @param attributes The element attributes.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs)
        throws XNIException {

        Augmentations modifiedAugs = handleStartElement(element, attributes, augs);

        // in the case where there is a {value constraint}, and the element
        // doesn't have any text content, change emptyElement call to
        // start + characters + end
        fDefaultValue = null;
        // fElementDepth == -2 indicates that the schema validator was removed
        // from the pipeline. then we don't need to call handleEndElement.
        if (fElementDepth != -2)
            modifiedAugs = handleEndElement(element, modifiedAugs);

        // call handlers
        if (fDocumentHandler != null) {
            if (!fSchemaElementDefault || fDefaultValue == null) {
                fDocumentHandler.emptyElement(element, attributes, modifiedAugs);
            } else {
                fDocumentHandler.startElement(element, attributes, modifiedAugs);
                fDocumentHandler.characters(fDefaultValue, null);
                fDocumentHandler.endElement(element, modifiedAugs);
            }
        }
    } // emptyElement(QName,XMLAttributes, Augmentations)

    /**
     * Character content.
     *
     * @param text The content.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void characters(XMLString text, Augmentations augs) throws XNIException {

        text = handleCharacters(text);
        // call handlers
        if (fDocumentHandler != null) {
            if (fNormalizeData && fUnionType) {
                // for union types we can't normalize data
                // thus we only need to send augs information if any;
                // the normalized data for union will be send
                // after normalization is performed (at the endElement())
                if (augs != null)
                    fDocumentHandler.characters(fEmptyXMLStr, augs);
            } else {
                fDocumentHandler.characters(text, augs);
            }
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
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void ignorableWhitespace(XMLString text, Augmentations augs) throws XNIException {

        handleIgnorableWhitespace(text);
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.ignorableWhitespace(text, augs);
        }

    } // ignorableWhitespace(XMLString)

    /**
     * The end of an element.
     *
     * @param element The name of the element.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endElement(QName element, Augmentations augs) throws XNIException {

        // in the case where there is a {value constraint}, and the element
        // doesn't have any text content, add a characters call.
        fDefaultValue = null;
        Augmentations modifiedAugs = handleEndElement(element, augs);
        // call handlers
        if (fDocumentHandler != null) {
            if (!fSchemaElementDefault || fDefaultValue == null) {
                fDocumentHandler.endElement(element, modifiedAugs);
            } else {
                fDocumentHandler.characters(fDefaultValue, null);
                fDocumentHandler.endElement(element, modifiedAugs);
            }
        }
    } // endElement(QName, Augmentations)

    /**
    * The start of a CDATA section.
    *
    * @param augs     Additional information that may include infoset augmentations
    *
    * @throws XNIException Thrown by handler to signal an error.
    */
    public void startCDATA(Augmentations augs) throws XNIException {

        // REVISIT: what should we do here if schema normalization is on??
        fInCDATA = true;
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startCDATA(augs);
        }

    } // startCDATA()

    /**
     * The end of a CDATA section.
     *
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endCDATA(Augmentations augs) throws XNIException {

        // call handlers
        fInCDATA = false;
        if (fDocumentHandler != null) {
            fDocumentHandler.endCDATA(augs);
        }

    } // endCDATA()

    /**
     * The end of the document.
     *
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void endDocument(Augmentations augs) throws XNIException {

        handleEndDocument();

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.endDocument(augs);
        }
        fLocator = null;
        fAssertionValidator = null;

    } // endDocument(Augmentations)

    //
    // DOMRevalidationHandler methods
    //





    public boolean characterData(String data, Augmentations augs) {

        fSawText = fSawText || data.length() > 0;

        // REVISIT: this methods basically duplicates implementation of
        //          handleCharacters(). We should be able to reuse some code

        // if whitespace == -1 skip normalization, because it is a complexType
        // or a union type.
        if (fNormalizeData && fWhiteSpace != -1 && fWhiteSpace != XSSimpleType.WS_PRESERVE) {
            // normalize data
            normalizeWhitespace(data, fWhiteSpace == XSSimpleType.WS_COLLAPSE);
            fBuffer.append(fNormalizedStr.ch, fNormalizedStr.offset, fNormalizedStr.length);
        } else {
            if (fAppendBuffer)
                fBuffer.append(data);
        }

        // When it's a complex type with element-only content, we need to
        // find out whether the content contains any non-whitespace character.
        boolean allWhiteSpace = true;
        if (fCurrentType != null
            && fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_ELEMENT) {
                // data outside of element content
                for (int i = 0; i < data.length(); i++) {
                    if (!XMLChar.isSpace(data.charAt(i))) {
                        allWhiteSpace = false;
                        fSawCharacters = true;
                        break;
                    }
                }
            }
        }

        return allWhiteSpace;
    }

    public void elementDefault(String data) {
        // no-op
    }

    //
    // XMLDocumentHandler and XMLDTDHandler methods
    //

    /**
     * This method notifies the start of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name     The name of the general entity.
     * @param identifier The resource identifier.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     * @param augs     Additional information that may include infoset augmentations
     *
     * @exception XNIException Thrown by handler to signal an error.
     */
    public void startGeneralEntity(
        String name,
        XMLResourceIdentifier identifier,
        String encoding,
        Augmentations augs)
        throws XNIException {

        // REVISIT: what should happen if normalize_data_ is on??
        fEntityRef = true;
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.startGeneralEntity(name, identifier, encoding, augs);
        }

    } // startEntity(String,String,String,String,String)

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
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void textDecl(String version, String encoding, Augmentations augs) throws XNIException {

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.textDecl(version, encoding, augs);
        }

    } // textDecl(String,String)

    /**
     * A comment.
     *
     * @param text The text in the comment.
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by application to signal an error.
     */
    public void comment(XMLString text, Augmentations augs) throws XNIException {
        
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1 && fCommentsAndPIsForAssert) {
            fAssertionValidator.comment(text);  
        }
        
        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.comment(text, augs);
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
     * @param augs     Additional information that may include infoset augmentations
     *
     * @throws XNIException Thrown by handler to signal an error.
     */
    public void processingInstruction(String target, XMLString data, Augmentations augs)
        throws XNIException {
        
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1 && fCommentsAndPIsForAssert) {
            fAssertionValidator.processingInstruction(target, data);  
        }

        // call handlers
        if (fDocumentHandler != null) {
            fDocumentHandler.processingInstruction(target, data, augs);
        }

    } // processingInstruction(String,XMLString)

    /**
     * This method notifies the end of a general entity.
     * <p>
     * <strong>Note:</strong> This method is not called for entity references
     * appearing as part of attribute values.
     *
     * @param name   The name of the entity.
     * @param augs   Additional information that may include infoset augmentations
     *
     * @exception XNIException
     *                   Thrown by handler to signal an error.
     */
    public void endGeneralEntity(String name, Augmentations augs) throws XNIException {

        // call handlers
        fEntityRef = false;
        if (fDocumentHandler != null) {
            fDocumentHandler.endGeneralEntity(name, augs);
        }

    } // endEntity(String)

    //
    // Constructors
    //

    /** Default constructor. */
    public XMLSchemaValidator() {
        fState4XsiType.setExtraChecking(false);
        fState4ApplyDefault.setFacetChecking(false);
        fSchemaVersion = fSchemaLoader.getSchemaVersion();
        fXSConstraints = fSchemaLoader.getXSConstraints();        
        fTypeAlternativeValidator = new XSDTypeAlternativeValidator(this);
    } // <init>()

    /*
     * Resets the component. The component can query the component manager
     * about any features and properties that affect the operation of the
     * component.
     *
     * @param componentManager The component manager.
     *
     * @throws SAXException Thrown by component on finitialization error.
     *                      For example, if a feature or property is
     *                      required for the operation of the component, the
     *                      component manager may throw a
     *                      SAXNotRecognizedException or a
     *                      SAXNotSupportedException.
     */
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {

        fIdConstraint = false;
        //reset XSDDescription
        fLocationPairs.clear();
        fExpandedLocationPairs.clear();

        // cleanup id table
        fValidationState.resetIDTables();

        // reset schema loader
        fSchemaLoader.reset(componentManager);

        // initialize state
        fCurrentElemDecl = null;
        fCurrentCM = null;
        fCurrCMState = null;
        fSkipValidationDepth = -1;
        fNFullValidationDepth = -1;
        fNNoneValidationDepth = -1;
        fElementDepth = -1;
        fSubElement = false;
        fSchemaDynamicValidation = false;

        // datatype normalization
        fEntityRef = false;
        fInCDATA = false;

        fMatcherStack.clear();

        // get error reporter
        fXSIErrorReporter.reset((XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER));

        boolean parser_settings;
        try {
            parser_settings = componentManager.getFeature(PARSER_SETTINGS);
        }
        catch (XMLConfigurationException e){
            parser_settings = true;
        }

        if (!parser_settings) {
            // parser settings have not been changed
            fValidationManager.addValidationState(fValidationState);
            // the node limit on the SecurityManager may have changed so need to refresh.
            nodeFactory.reset();
            // Re-parse external schema location properties.
            XMLSchemaLoader.processExternalHints(
                fExternalSchemas,
                fExternalNoNamespaceSchema,
                fLocationPairs,
                fXSIErrorReporter.fErrorReporter);
            return;
        }
        
        // pass the component manager to the factory..
        nodeFactory.reset(componentManager);

        // get symbol table. if it's a new one, add symbols to it.
        SymbolTable symbolTable = (SymbolTable) componentManager.getProperty(SYMBOL_TABLE);
        if (symbolTable != fSymbolTable) {
            fSymbolTable = symbolTable;
        }
        
        try {
            fNamespaceGrowth = componentManager.getFeature(NAMESPACE_GROWTH);
        } catch (XMLConfigurationException e) {
            fNamespaceGrowth = false;
        }

        try {
            fDynamicValidation = componentManager.getFeature(DYNAMIC_VALIDATION);
        } catch (XMLConfigurationException e) {
            fDynamicValidation = false;
        }

        if (fDynamicValidation) {
            fDoValidation = true;
        } else {
            try {
                fDoValidation = componentManager.getFeature(VALIDATION);
            } catch (XMLConfigurationException e) {
                fDoValidation = false;
            }
        }

        if (fDoValidation) {
            try {
                fDoValidation = componentManager.getFeature(XMLSchemaValidator.SCHEMA_VALIDATION);
            } catch (XMLConfigurationException e) {
            }
        }

        try {
            fFullChecking = componentManager.getFeature(SCHEMA_FULL_CHECKING);
        } catch (XMLConfigurationException e) {
            fFullChecking = false;
        }

        try {
            fNormalizeData = componentManager.getFeature(NORMALIZE_DATA);
        } catch (XMLConfigurationException e) {
            fNormalizeData = false;
        }

        try {
            fSchemaElementDefault = componentManager.getFeature(SCHEMA_ELEMENT_DEFAULT);
        } catch (XMLConfigurationException e) {
            fSchemaElementDefault = false;
        }

        try {
            fAugPSVI = componentManager.getFeature(SCHEMA_AUGMENT_PSVI);
        } catch (XMLConfigurationException e) {
            fAugPSVI = true;
        }
        try {
            fSchemaType =
                (String) componentManager.getProperty(
                    Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE);
        } catch (XMLConfigurationException e) {
            fSchemaType = null;
        }
        
        try {
            fUseGrammarPoolOnly = componentManager.getFeature(USE_GRAMMAR_POOL_ONLY);
        } 
        catch (XMLConfigurationException e) {
            fUseGrammarPoolOnly = false;
        }

        fEntityResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_MANAGER);
        
        // reset ID Context
        if (fIDContext != null) {
            fIDContext.clear();
        }

        final TypeValidatorHelper typeValidatorHelper = TypeValidatorHelper.getInstance(fSchemaVersion);
        
        fValidationManager = (ValidationManager) componentManager.getProperty(VALIDATION_MANAGER);
        fValidationManager.addValidationState(fValidationState);
        fValidationState.setSymbolTable(fSymbolTable);
        fValidationState.setTypeValidatorHelper(typeValidatorHelper);
        
        try {
            final Object rootType = componentManager.getProperty(ROOT_TYPE_DEF);
            if (rootType == null) {
                fRootTypeQName = null;
                fRootTypeDefinition = null;
            }
            else if (rootType instanceof javax.xml.namespace.QName) {
                fRootTypeQName = (javax.xml.namespace.QName) rootType;
                fRootTypeDefinition = null;
            }
            else {
                fRootTypeDefinition = (XSTypeDefinition) rootType;
                fRootTypeQName = null;
            }
        } 
        catch (XMLConfigurationException e) {
            fRootTypeQName = null;
            fRootTypeDefinition = null;
        }
        
        try {
            final Object rootDecl = componentManager.getProperty(ROOT_ELEMENT_DECL);
            if (rootDecl == null) {
                fRootElementDeclQName = null;
                fRootElementDeclaration = null;
            }
            else if (rootDecl instanceof javax.xml.namespace.QName) {
                fRootElementDeclQName = (javax.xml.namespace.QName) rootDecl;
                fRootElementDeclaration = null;
            }
            else {
                fRootElementDeclaration = (XSElementDecl) rootDecl;
                fRootElementDeclQName = null;
            }
        }
        catch (XMLConfigurationException e) {
            fRootElementDeclQName = null;
            fRootElementDeclaration = null;
        }
        
        boolean ignoreXSIType;
        try {
            ignoreXSIType = componentManager.getFeature(IGNORE_XSI_TYPE);
        } 
        catch (XMLConfigurationException e) {
            ignoreXSIType = false;
        }
        // An initial value of -1 means that the root element considers itself
        // below the depth where xsi:type stopped being ignored (which means that
        // xsi:type attributes will not be ignored for the entire document)
        fIgnoreXSITypeDepth = ignoreXSIType ? 0 : -1;
        
        try {
            fIDCChecking = componentManager.getFeature(IDENTITY_CONSTRAINT_CHECKING);
        } 
        catch (XMLConfigurationException e) {
            fIDCChecking = true;
        }
        
        try {
            fValidationState.setIdIdrefChecking(componentManager.getFeature(ID_IDREF_CHECKING));
        }
        catch (XMLConfigurationException e) {
            fValidationState.setIdIdrefChecking(true);
        }
        
        try {
            fValidationState.setUnparsedEntityChecking(componentManager.getFeature(UNPARSED_ENTITY_CHECKING));
        }
        catch (XMLConfigurationException e) {
            fValidationState.setUnparsedEntityChecking(true);
        }

        try {
            fTypeAlternativesChecking = componentManager.getFeature(TYPE_ALTERNATIVES_CHECKING);
        }
        catch (XMLConfigurationException e) {
            fTypeAlternativesChecking = true;
        }
        
        try {
            fCommentsAndPIsForAssert = componentManager.getFeature(ASSERT_COMMENT_PI_CHECKING);
        }
        catch (XMLConfigurationException e) {
            fCommentsAndPIsForAssert = true;
        }
        
        // get schema location properties
        try {
            fExternalSchemas = (String) componentManager.getProperty(SCHEMA_LOCATION);
            fExternalNoNamespaceSchema =
                (String) componentManager.getProperty(SCHEMA_NONS_LOCATION);
        } catch (XMLConfigurationException e) {
            fExternalSchemas = null;
            fExternalNoNamespaceSchema = null;
        }

        // get datatype xml version
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            try {
                final Object xmlVer = componentManager.getProperty(DATATYPE_XML_VERSION);
                if (xmlVer instanceof String) {
                    fDatatypeXMLVersion = (String) xmlVer;
                    if ("1.1".equals(xmlVer)) {
                        fValidationState.setDatatypeXMLVersion(Constants.XML_VERSION_1_1);
                    }
                    else {
                        fValidationState.setDatatypeXMLVersion(Constants.XML_VERSION_1_0);
                    }
                }
            }
            catch (XMLConfigurationException e) {
                fDatatypeXMLVersion = null;
                fValidationState.setDatatypeXMLVersion(Constants.XML_VERSION_1_0);
            }
        }

        // store the external schema locations. they are set when reset is called,
        // so any other schemaLocation declaration for the same namespace will be
        // effectively ignored. becuase we choose to take first location hint
        // available for a particular namespace.
        XMLSchemaLoader.processExternalHints(
            fExternalSchemas,
            fExternalNoNamespaceSchema,
            fLocationPairs,
            fXSIErrorReporter.fErrorReporter);

        try {
            fJaxpSchemaSource = componentManager.getProperty(JAXP_SCHEMA_SOURCE);
        } catch (XMLConfigurationException e) {
            fJaxpSchemaSource = null;

        }

        // clear grammars, and put the one for schema namespace there
        try {
            fGrammarPool = (XMLGrammarPool) componentManager.getProperty(XMLGRAMMAR_POOL);
        } catch (XMLConfigurationException e) {
            fGrammarPool = null;
        }

        fState4XsiType.setSymbolTable(symbolTable);
        fState4ApplyDefault.setSymbolTable(symbolTable);
        
        fState4XsiType.setTypeValidatorHelper(typeValidatorHelper);
        fState4ApplyDefault.setTypeValidatorHelper(typeValidatorHelper);

    } // reset(XMLComponentManager)
    
    //
    // Protected methods
    //

    /** ensure element stack capacity */
    void ensureStackCapacity() {

        if (fElementDepth == fElemDeclStack.length) {
            int newSize = fElementDepth + INC_STACK_SIZE;
            boolean[] newArrayB = new boolean[newSize];
            System.arraycopy(fSubElementStack, 0, newArrayB, 0, fElementDepth);
            fSubElementStack = newArrayB;

            XSElementDecl[] newArrayE = new XSElementDecl[newSize];
            System.arraycopy(fElemDeclStack, 0, newArrayE, 0, fElementDepth);
            fElemDeclStack = newArrayE;

            newArrayB = new boolean[newSize];
            System.arraycopy(fNilStack, 0, newArrayB, 0, fElementDepth);
            fNilStack = newArrayB;

            XSNotationDecl[] newArrayN = new XSNotationDecl[newSize];
            System.arraycopy(fNotationStack, 0, newArrayN, 0, fElementDepth);
            fNotationStack = newArrayN;

            XSTypeDefinition[] newArrayT = new XSTypeDefinition[newSize];
            System.arraycopy(fTypeStack, 0, newArrayT, 0, fElementDepth);
            fTypeStack = newArrayT;

            XSCMValidator[] newArrayC = new XSCMValidator[newSize];
            System.arraycopy(fCMStack, 0, newArrayC, 0, fElementDepth);
            fCMStack = newArrayC;

            newArrayB = new boolean[newSize];
            System.arraycopy(fSawTextStack, 0, newArrayB, 0, fElementDepth);
            fSawTextStack = newArrayB;

            newArrayB = new boolean[newSize];
            System.arraycopy(fStringContent, 0, newArrayB, 0, fElementDepth);
            fStringContent = newArrayB;

            newArrayB = new boolean[newSize];
            System.arraycopy(fStrictAssessStack, 0, newArrayB, 0, fElementDepth);
            fStrictAssessStack = newArrayB;

            int[][] newArrayIA = new int[newSize][];
            System.arraycopy(fCMStateStack, 0, newArrayIA, 0, fElementDepth);
            fCMStateStack = newArrayIA;
        }

    } // ensureStackCapacity

    // handle start document
    void handleStartDocument(XMLLocator locator, String encoding) {
        if (fIDCChecking) {
            fValueStoreCache.startDocument();
        }
        if (fAugPSVI) {
            fCurrentPSVI.fGrammars = null;
            fCurrentPSVI.fSchemaInformation = null;
        }
    } // handleStartDocument(XMLLocator,String)
    
    void handleEndDocument() {
        if (fIDCChecking) {
            fValueStoreCache.endDocument();
        }
    } // handleEndDocument()

    // handle character contents
    // returns the normalized string if possible, otherwise the original string
    XMLString handleCharacters(XMLString text) {

        if (fSkipValidationDepth >= 0) {
            // delegate to assertions validator subcomponent
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                fAssertionValidator.characterDataHandler(text);
            }
            return text;
        }

        fSawText = fSawText || text.length > 0;

        // Note: data in EntityRef and CDATA is normalized as well
        // if whitespace == -1 skip normalization, because it is a complexType
        // or a union type.
        if (fNormalizeData && fWhiteSpace != -1 && fWhiteSpace != XSSimpleType.WS_PRESERVE) {
            // normalize data
            normalizeWhitespace(text, fWhiteSpace == XSSimpleType.WS_COLLAPSE);
            text = fNormalizedStr;
        }
        if (fAppendBuffer)
            fBuffer.append(text.ch, text.offset, text.length);

        // When it's a complex type with element-only content, we need to
        // find out whether the content contains any non-whitespace character.
        if (fCurrentType != null
            && fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_ELEMENT) {
                // data outside of element content
                for (int i = text.offset; i < text.offset + text.length; i++) {
                    if (!XMLChar.isSpace(text.ch[i])) {
                        fSawCharacters = true;
                        break;
                    }
                }
            }
        }
        
        // delegate to assertions validator subcomponent
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fAssertionValidator.characterDataHandler(text);
        }

        return text;
    } // handleCharacters(XMLString)

    /**
     * Normalize whitespace in an XMLString according to the rules defined
     * in XML Schema specifications.
     * @param value    The string to normalize.
     * @param collapse replace or collapse
     */
    private void normalizeWhitespace(XMLString value, boolean collapse) {
        boolean skipSpace = collapse;
        boolean sawNonWS = false;
        boolean leading = false;
        boolean trailing = false;
        char c;
        int size = value.offset + value.length;

        // ensure the ch array is big enough
        if (fNormalizedStr.ch == null || fNormalizedStr.ch.length < value.length + 1) {
            fNormalizedStr.ch = new char[value.length + 1];
        }
        // don't include the leading ' ' for now. might include it later.
        fNormalizedStr.offset = 1;
        fNormalizedStr.length = 1;

        for (int i = value.offset; i < size; i++) {
            c = value.ch[i];
            if (XMLChar.isSpace(c)) {
                if (!skipSpace) {
                    // take the first whitespace as a space and skip the others
                    fNormalizedStr.ch[fNormalizedStr.length++] = ' ';
                    skipSpace = collapse;
                }
                if (!sawNonWS) {
                    // this is a leading whitespace, record it
                    leading = true;
                }
            } else {
                fNormalizedStr.ch[fNormalizedStr.length++] = c;
                skipSpace = false;
                sawNonWS = true;
            }
        }
        if (skipSpace) {
            if (fNormalizedStr.length > 1) {
                // if we finished on a space trim it but also record it
                fNormalizedStr.length--;
                trailing = true;
            } else if (leading && !fFirstChunk) {
                // if all we had was whitespace we skipped record it as
                // trailing whitespace as well
                trailing = true;
            }
        }

        if (fNormalizedStr.length > 1) {
            if (!fFirstChunk && (fWhiteSpace == XSSimpleType.WS_COLLAPSE)) {
                if (fTrailing) {
                    // previous chunk ended on whitespace
                    // insert whitespace
                    fNormalizedStr.offset = 0;
                    fNormalizedStr.ch[0] = ' ';
                } else if (leading) {
                    // previous chunk ended on character,
                    // this chunk starts with whitespace
                    fNormalizedStr.offset = 0;
                    fNormalizedStr.ch[0] = ' ';
                }
            }
        }

        // The length includes the leading ' '. Now removing it.
        fNormalizedStr.length -= fNormalizedStr.offset;

        fTrailing = trailing;

        if (trailing || sawNonWS)
            fFirstChunk = false;
    }

    private void normalizeWhitespace(String value, boolean collapse) {
        boolean skipSpace = collapse;
        char c;
        int size = value.length();

        // ensure the ch array is big enough
        if (fNormalizedStr.ch == null || fNormalizedStr.ch.length < size) {
            fNormalizedStr.ch = new char[size];
        }
        fNormalizedStr.offset = 0;
        fNormalizedStr.length = 0;

        for (int i = 0; i < size; i++) {
            c = value.charAt(i);
            if (XMLChar.isSpace(c)) {
                if (!skipSpace) {
                    // take the first whitespace as a space and skip the others
                    fNormalizedStr.ch[fNormalizedStr.length++] = ' ';
                    skipSpace = collapse;
                }
            } else {
                fNormalizedStr.ch[fNormalizedStr.length++] = c;
                skipSpace = false;
            }
        }
        if (skipSpace) {
            if (fNormalizedStr.length != 0)
                // if we finished on a space trim it but also record it
                fNormalizedStr.length--;
        }
    }

    // handle ignorable whitespace
    void handleIgnorableWhitespace(XMLString text) {

        if (fSkipValidationDepth >= 0)
            return;

        // REVISIT: the same process needs to be performed as handleCharacters.
        // only it's simpler here: we know all characters are whitespaces.

    } // handleIgnorableWhitespace(XMLString)

    /** Handle element. */
    Augmentations handleStartElement(QName element, XMLAttributes attributes, Augmentations augs) {

        if (DEBUG) {
            System.out.println("==>handleStartElement: " + element);
        }

        // root element
        if (fElementDepth == -1 && fValidationManager.isGrammarFound()) {
            if (fSchemaType == null) {
                // schemaType is not specified
                // if a DTD grammar is found, we do the same thing as Dynamic:
                // if a schema grammar is found, validation is performed;
                // otherwise, skip the whole document.
                fSchemaDynamicValidation = true;
            } else {
                // [1] Either schemaType is DTD, and in this case validate/schema is turned off
                // [2] Validating against XML Schemas only
                //   [a] dynamic validation is false: report error if SchemaGrammar is not found
                //   [b] dynamic validation is true: if grammar is not found ignore.
            }

        }
        
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
           // reset the list for next element
           fIsAssertProcessingNeededForSTUnionAttrs.clear();
        }

        // get xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes,
        // parse them to get the grammars. But only do this if the grammar can grow.
        if (!fUseGrammarPoolOnly) {
            String sLocation =
                attributes.getValue(SchemaSymbols.URI_XSI, SchemaSymbols.XSI_SCHEMALOCATION);
            String nsLocation =
                attributes.getValue(SchemaSymbols.URI_XSI, SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION);
            //store the location hints..  we need to do it so that we can defer the loading of grammar until
            //there is a reference to a component from that namespace. To provide location hints to the
            //application for a namespace
            storeLocations(sLocation, nsLocation);
        }

        // if we are in the content of "skip", then just skip this element
        // REVISIT:  is this the correct behaviour for ID constraints?  -NG
        if (fSkipValidationDepth >= 0) {
            fElementDepth++;
            if (fAugPSVI)
                augs = getEmptyAugs(augs);
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                assertionValidatorStartElementDelegate(element, attributes, augs);                        
            }
            return augs;
        }

        // if we are not skipping this element, and there is a content model,
        // we try to find the corresponding decl object for this element.
        // the reason we move this part of code here is to make sure the
        // error reported here (if any) is stored within the parent element's
        // context, instead of that of the current element.
        Object decl = null;
        if (fCurrentCM != null) {
            decl = fCurrentCM.oneTransition(element, fCurrCMState, fSubGroupHandler, this);
            // it could be an element decl or a wildcard decl
            if (fCurrCMState[0] == XSCMValidator.FIRST_ERROR) {
                XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
                //REVISIT: is it the only case we will have particle = null?
                Vector next;
                if (ctype.fParticle != null
                    && (next = fCurrentCM.whatCanGoHere(fCurrCMState)).size() > 0) {
                    String expected = expectedStr(next);
                    final int[] occurenceInfo = fCurrentCM.occurenceInfo(fCurrCMState);                    
                    String elemExpandedQname = (element.uri != null) ? "{"+'"'+element.uri+'"'+":"+element.localpart+"}" : element.localpart;                    
                    if (occurenceInfo != null) {
                        final int minOccurs = occurenceInfo[0];
                        final int maxOccurs = occurenceInfo[1];
                        final int count = occurenceInfo[2];
                        // Check if this is a violation of minOccurs
                        if (count < minOccurs) {
                            final int required = minOccurs - count;
                            if (required > 1) {
                                reportSchemaError("cvc-complex-type.2.4.h", new Object[] { element.rawname, 
                                        fCurrentCM.getTermName(occurenceInfo[3]), Integer.toString(minOccurs), Integer.toString(required) });
                            }
                            else {
                                reportSchemaError("cvc-complex-type.2.4.g", new Object[] { element.rawname, 
                                                   fCurrentCM.getTermName(occurenceInfo[3]), Integer.toString(minOccurs) });
                            }
                        }
                        // Check if this is a violation of maxOccurs
                        else if (count >= maxOccurs && maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                            reportSchemaError("cvc-complex-type.2.4.e", new Object[] { element.rawname, 
                                               expected, Integer.toString(maxOccurs) });
                        }
                        else {
                            reportSchemaError("cvc-complex-type.2.4.a", new Object[] { elemExpandedQname, expected });
                        }
                    }
                    else {
                        reportSchemaError("cvc-complex-type.2.4.a", new Object[] { elemExpandedQname, expected });
                    }
                }
                else {
                    final int[] occurenceInfo = fCurrentCM.occurenceInfo(fCurrCMState);
                    if (occurenceInfo != null) {
                        final int maxOccurs = occurenceInfo[1];
                        final int count = occurenceInfo[2];
                        // Check if this is a violation of maxOccurs
                        if (count >= maxOccurs && maxOccurs != SchemaSymbols.OCCURRENCE_UNBOUNDED) {                            
                            reportSchemaError("cvc-complex-type.2.4.f", new Object[] { fCurrentCM.getTermName(occurenceInfo[3]), Integer.toString(maxOccurs) });
                        }
                        else {
                            reportSchemaError("cvc-complex-type.2.4.d", new Object[] { element.rawname });
                        }
                    }
                    else {
                        reportSchemaError("cvc-complex-type.2.4.d", new Object[] { element.rawname });
                    }
                }
            }
        }

        // if it's not the root element, we push the current states in the stacks
        if (fElementDepth != -1) {
            ensureStackCapacity();
            fSubElementStack[fElementDepth] = true;
            fSubElement = false;
            fElemDeclStack[fElementDepth] = fCurrentElemDecl;
            fNilStack[fElementDepth] = fNil;
            fNotationStack[fElementDepth] = fNotation;
            fTypeStack[fElementDepth] = fCurrentType;
            fStrictAssessStack[fElementDepth] = fStrictAssess;
            fCMStack[fElementDepth] = fCurrentCM;
            fCMStateStack[fElementDepth] = fCurrCMState;
            fSawTextStack[fElementDepth] = fSawText;
            fStringContent[fElementDepth] = fSawCharacters;
        }

        // increase the element depth after we've saved
        // all states for the parent element
        fElementDepth++;
        fCurrentElemDecl = null;
        XSWildcardDecl wildcard = null;
        fCurrentType = null;
        fStrictAssess = true;
        fNil = false;
        fNotation = null;

        // and the buffer to hold the value of the element
        fBuffer.setLength(0);
        fSawText = false;
        fSawCharacters = false;

        // check what kind of declaration the "decl" from
        // oneTransition() maps to
        if (decl != null) {
            if (decl instanceof XSElementDecl) {
                fCurrentElemDecl = (XSElementDecl) decl;
            } else if (decl instanceof XSOpenContentDecl) {
                wildcard = (XSWildcardDecl) ((XSOpenContentDecl)decl).getWildcard();
            } else {
                wildcard = (XSWildcardDecl) decl;
            }
        }

        // if the wildcard is skip
        if (wildcard != null && wildcard.fProcessContents == XSWildcardDecl.PC_SKIP) {
            fSkipValidationDepth = fElementDepth;
            if (fAugPSVI)
                augs = getEmptyAugs(augs);
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                assertionValidatorStartElementDelegate(element, attributes, augs);                        
            }
            return augs;
        }
        
        if (fElementDepth == 0) {
            // 1.1.1.1 An element declaration was stipulated by the processor
            if (fRootElementDeclaration != null) {
                fCurrentElemDecl = fRootElementDeclaration;
                checkElementMatchesRootElementDecl(fCurrentElemDecl, element);
            }
            else if (fRootElementDeclQName != null) {
                processRootElementDeclQName(fRootElementDeclQName, element);
            }
            // 1.2.1.1 A type definition was stipulated by the processor
            else if (fRootTypeDefinition != null) {
                fCurrentType = fRootTypeDefinition;
            }
            else if (fRootTypeQName != null) {
                processRootTypeQName(fRootTypeQName);
            }
        }
        
        // if there was no processor stipulated type
        if (fCurrentType == null) {
            // try again to get the element decl:
            // case 1: find declaration for root element
            // case 2: find declaration for element from another namespace
            if (fCurrentElemDecl == null) {
                // try to find schema grammar by different means..
                SchemaGrammar sGrammar =
                    findSchemaGrammar(
                        XSDDescription.CONTEXT_ELEMENT,
                        element.uri,
                        null,
                        element,
                        attributes);
                if (sGrammar != null) {
                    fCurrentElemDecl = sGrammar.getGlobalElementDecl(element.localpart);
                }
            }
            
            if (fCurrentElemDecl != null) {
                // then get the type
                fCurrentType = fCurrentElemDecl.fType;
            }
        }

        //process type alternatives
        if (fTypeAlternativesChecking && fCurrentElemDecl != null) {
           fTypeAlternative = fTypeAlternativeValidator.getTypeAlternative(fCurrentElemDecl, element, attributes, fInheritableAttrList, fNamespaceContext, fLocator.getExpandedSystemId());           
           if (fTypeAlternative != null) {
               fCurrentType = fTypeAlternative.getTypeDefinition();
           }
        }

        // check if we should be ignoring xsi:type on this element
        if (fElementDepth == fIgnoreXSITypeDepth && fCurrentElemDecl == null) {
            fIgnoreXSITypeDepth++;
        }
        
        // process xsi:type attribute information
        String xsiType = null;
        if (fElementDepth >= fIgnoreXSITypeDepth) {
            xsiType = attributes.getValue(SchemaSymbols.URI_XSI, SchemaSymbols.XSI_TYPE);
        }
        
        // if no decl/type found for the current element
        final boolean isSchema11 = (fSchemaVersion == Constants.SCHEMA_VERSION_1_1);
        boolean needToPushErrorContext = false;
        
        if (fCurrentType == null && xsiType == null) {
            // if this is the validation root, report an error, because
            // we can't find eith decl or type for this element
            // REVISIT: should we report error, or warning?
            if (fElementDepth == 0) {
                // for dynamic validation, skip the whole content,
                // because no grammar was found.
                if (fDynamicValidation || fSchemaDynamicValidation) {
                    // no schema grammar was found, but it's either dynamic
                    // validation, or another kind of grammar was found (DTD,
                    // for example). The intended behavior here is to skip
                    // the whole document. To improve performance, we try to
                    // remove the validator from the pipeline, since it's not
                    // supposed to do anything.
                    if (fDocumentSource != null) {
                        fDocumentSource.setDocumentHandler(fDocumentHandler);
                        if (fDocumentHandler != null)
                            fDocumentHandler.setDocumentSource(fDocumentSource);
                        // indicate that the validator was removed.
                        fElementDepth = -2;
                        return augs;
                    }

                    fSkipValidationDepth = fElementDepth;
                    if (fAugPSVI)
                        augs = getEmptyAugs(augs);
                    return augs;
                }
                // We don't call reportSchemaError here, because the spec
                // doesn't think it's invalid not to be able to find a
                // declaration or type definition for an element. Xerces is
                // reporting it as an error for historical reasons, but in
                // PSVI, we shouldn't mark this element as invalid because
                // of this. - SG
                fXSIErrorReporter.fErrorReporter.reportError(
                    XSMessageFormatter.SCHEMA_DOMAIN,
                    "cvc-elt.1.a",
                    new Object[] { element.rawname },
                    XMLErrorReporter.SEVERITY_ERROR);
            }
            // if wildcard = strict, report error.
            // needs to be called before fXSIErrorReporter.pushContext()
            // so that the error belongs to the parent element.
            else if (wildcard != null && wildcard.fProcessContents == XSWildcardDecl.PC_STRICT) {
                // report error, because wilcard = strict
                reportSchemaError("cvc-complex-type.2.4.c", new Object[] { element.rawname });
            }           
            
            // no element decl or type found for this element.
            // Allowed by the spec, we can choose to either laxly assess this
            // element, or to skip it. Now we choose lax assessment.
            fCurrentType = SchemaGrammar.getXSAnyType(fSchemaVersion);
            fStrictAssess = false;
            fNFullValidationDepth = fElementDepth;
            // any type has mixed content, so we don't need to append buffer
            fAppendBuffer = false;

            // push error reporter context: record the current position
            // This has to happen after we process skip contents,
            // otherwise push and pop won't be correctly paired.
            if (isSchema11) {
                needToPushErrorContext = true;
            }
            else {
                fXSIErrorReporter.pushContext();
            }
        } else {
            // push error reporter context: record the current position
            // This has to happen after we process skip contents,
            // otherwise push and pop won't be correctly paired.
            if (isSchema11) {
                needToPushErrorContext = true;
            }
            else {
                fXSIErrorReporter.pushContext();
            }

            // get xsi:type
            if (xsiType != null) {
                XSTypeDefinition oldType = fCurrentType;
                if (isSchema11) {
                    if (fXSITypeErrors.size() > 0) {
                        fXSITypeErrors.clear();
                    }
                    fCurrentType = getAndCheckXsiType(element, xsiType, attributes, fXSITypeErrors);
                }
                else {
                    fCurrentType = getAndCheckXsiType(element, xsiType, attributes);
                }
                // If it fails, use the old type. Use anyType if ther is no old type.
                if (fCurrentType == null) {
                    if (oldType == null)
                        fCurrentType = SchemaGrammar.getXSAnyType(fSchemaVersion);
                    else
                        fCurrentType = oldType;
                }
            }

            fNNoneValidationDepth = fElementDepth;
            // if the element has a fixed value constraint, we need to append
            if (fCurrentElemDecl != null
                && fCurrentElemDecl.getConstraintType() == XSConstants.VC_FIXED) {
                fAppendBuffer = true;
            }
            // if the type is simple, we need to append
            else if (fCurrentType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                fAppendBuffer = true;
            } else {
                // if the type is simple content complex type, we need to append
                XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
                fAppendBuffer = (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE);
            }
        }

        // EDC rule
        if (isSchema11) {
            if (wildcard != null && fCurrentCM != null) {
                XSElementDecl elemDecl = findLocallyDeclaredType(element, fCurrentCM, fTypeStack[fElementDepth-1].getBaseType());
                if (elemDecl != null) {
                    final XSTypeDefinition elemType = elemDecl.getTypeDefinition();
                    // types need to be equivalent
                    if (fCurrentType != elemType) {
                        short block = elemDecl.fBlock;
                        if (elemType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                            block |= ((XSComplexTypeDecl) elemType).fBlock;
                        }
                        if (!fXSConstraints.checkTypeDerivationOk(fCurrentType, elemType, block)) {
                            reportSchemaError(
                                    "cos-element-consistent.4.a",
                                    new Object[] { element.rawname, fCurrentType, elemType.getName()});
                        }
                    }
                }
            }

            if (needToPushErrorContext) {
                fXSIErrorReporter.pushContext();
                final int errorSize = fXSITypeErrors.size();
                if (errorSize > 0) {
                    for (int i=0; i<errorSize; ++i) {
                        reportSchemaError((String)fXSITypeErrors.get(i), (Object[])fXSITypeErrors.get(++i));
                    }
                    fXSITypeErrors.clear();
                }
            }
        }

        // Element Locally Valid (Element)
        // 2 Its {abstract} must be false.
        if (fCurrentElemDecl != null && fCurrentElemDecl.getAbstract())
            reportSchemaError("cvc-elt.2", new Object[] { element.rawname });

        // make the current element validation root
        if (fElementDepth == 0) {
            fValidationRoot = element.rawname;
        }

        // update normalization flags
        if (fNormalizeData) {
            // reset values
            fFirstChunk = true;
            fTrailing = false;
            fUnionType = false;
            fWhiteSpace = -1;
        }

        // Element Locally Valid (Type)
        // 2 Its {abstract} must be false.
        if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
            if (ctype.getAbstract()) {
                reportSchemaError("cvc-type.2", new Object[] { element.rawname });
            }
            if (fNormalizeData) {
                // find out if the content type is simple and if variety is union
                // to be able to do character normalization
                if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                    if (ctype.fXSSimpleType.getVariety() == XSSimpleType.VARIETY_UNION) {
                        fUnionType = true;
                    } else {
                        try {
                            fWhiteSpace = ctype.fXSSimpleType.getWhitespace();
                        } catch (DatatypeException e) {
                            // do nothing
                        }
                    }
                }
            }
        }
        // normalization: simple type
        else if (fNormalizeData) {
            // if !union type
            XSSimpleType dv = (XSSimpleType) fCurrentType;
            if (dv.getVariety() == XSSimpleType.VARIETY_UNION) {
                fUnionType = true;
            } else {
                try {
                    fWhiteSpace = dv.getWhitespace();
                } catch (DatatypeException e) {
                    // do nothing
                }
            }
        }

        // then try to get the content model
        fCurrentCM = null;
        if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            fCurrentCM = ((XSComplexTypeDecl) fCurrentType).getContentModel(fCMBuilder);
        }

        // and get the initial content model state
        fCurrCMState = null;
        if (fCurrentCM != null)
            fCurrCMState = fCurrentCM.startContentModel();

        // get information about xsi:nil
        String xsiNil = attributes.getValue(SchemaSymbols.URI_XSI, SchemaSymbols.XSI_NIL);
        // only deal with xsi:nil when there is an element declaration
        if (xsiNil != null && fCurrentElemDecl != null)
            fNil = getXsiNil(element, xsiNil);

        // now validate everything related with the attributes
        // first, get the attribute group
        XSAttributeGroupDecl attrGrp = null;
        if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
            XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
            attrGrp = ctype.getAttrGrp();
        }
        
        if (fIDCChecking) {
            // activate identity constraints
            fValueStoreCache.startElement();
            fMatcherStack.pushContext();
            //if (fCurrentElemDecl != null && fCurrentElemDecl.fIDCPos > 0 && !fIgnoreIDC) {
            if (fCurrentElemDecl != null && fCurrentElemDecl.fIDCPos > 0) {
                fIdConstraint = true;
                // initialize when identity constrains are defined for the elem
                fValueStoreCache.initValueStoresFor(fCurrentElemDecl, this);
            }
        }
        
        // Push the ID context
        // attributes of type ID identifies the current element 
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fIDContext.pushContext();
        }
        
        processAttributes(element, attributes, attrGrp);

        // add default attributes
        if (attrGrp != null) {
            addDefaultAttributes(element, attributes, attrGrp);
        }
        
        // assert check for union types, for attributes
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fAssertionValidator.extraCheckForSTUnionAssertsAttrs(attributes);
        }
        
        // Set ID scope to parent
        // simple content of an element identifies the parent of the element
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fIDContext.setCurrentScopeToParent();
        }                

        // call all active identity constraints
        int count = fMatcherStack.getMatcherCount();
        for (int i = 0; i < count; i++) {
            XPathMatcher matcher = fMatcherStack.getMatcherAt(i);
            matcher.startElement( element, attributes);
        }
        
        if (fAugPSVI) {
            augs = getEmptyAugs(augs);

            // PSVI: add validation context
            fCurrentPSVI.fValidationContext = fValidationRoot;
            // PSVI: add element declaration
            fCurrentPSVI.fDeclaration = fCurrentElemDecl;
            // PSVI: add element type
            fCurrentPSVI.fTypeDecl = fCurrentType;
            // PSVI: add notation attribute
            fCurrentPSVI.fNotation = fNotation;
            // PSVI: add nil
            fCurrentPSVI.fNil = fNil;
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
               // PSVI: add type alternative
               fCurrentPSVI.fTypeAlternative = fTypeAlternative;
               // PSVI: add inherited attributes
               fInhrAttrCountStack.push(fInheritableAttrList.size());
               fCurrentPSVI.fInheritedAttributes = fTypeAlternativeValidator.getInheritedAttributesForPSVI();               
               // PSVI: add failed assertions
               fCurrentPSVI.fFailedAssertions = fFailedAssertions;               
            }
        }                
                
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            // find attributes among the attributes of the current element which are declared inheritable, and store them for later processing
            fTypeAlternativeValidator.saveInheritableAttributes(fCurrentElemDecl, attributes);
            // for each attribute, update its augmentation information to tell if its simpleType->union requires assertion evaluations
            XMLAttributesImpl attrsImpl = (XMLAttributesImpl)attributes;
            for (int attrIdx = 0; attrIdx < attrsImpl.getLength(); attrIdx++) {
                Augmentations attrAugs = attrsImpl.getAugmentations(attrIdx);
                attrAugs.putItem(XSAssertConstants.isAssertProcNeededForUnionAttr, fIsAssertProcessingNeededForSTUnionAttrs.get(attrIdx));
            }
            assertionValidatorStartElementDelegate(element, attributes, augs);
        }

        return augs;

    } // handleStartElement(QName,XMLAttributes,boolean)

    private XSElementDecl findLocallyDeclaredType(QName element,
            XSCMValidator currentCM, XSTypeDefinition baseType) {
        XSElementDecl elemDecl = null;
        if (currentCM != null) {
           elemDecl = currentCM.findMatchingElemDecl(element, fSubGroupHandler);
        }
        if (elemDecl == null) {
            if (baseType.getTypeCategory() != XSTypeDefinition.SIMPLE_TYPE &&
                    baseType != SchemaGrammar.getXSAnyType(fSchemaVersion)) {
                currentCM = ((XSComplexTypeDecl) baseType).getContentModel(fCMBuilder);
                return findLocallyDeclaredType(element, currentCM, baseType.getBaseType());
            }
        }
        return elemDecl;
    }
    
    /*
     * Delegate to assertions validator startElement handler.
     */
    private void assertionValidatorStartElementDelegate(QName element, XMLAttributes attributes, Augmentations augs) {
        try {
            fAssertionValidator.handleStartElement(element, attributes);
        }
        catch(Exception ex) {
            throw new XMLParseException(fLocator, ex.getMessage());
        } 
    } // assertionValidatorStartElementDelegate

    /**
     *  Handle end element. If there is not text content, and there is a
     *  {value constraint} on the corresponding element decl, then
     * set the fDefaultValue XMLString representing the default value.
     */
    Augmentations handleEndElement(QName element, Augmentations augs) {
        
        if (DEBUG) {
            System.out.println("==>handleEndElement:" + element);
        }
        
        // if we are skipping, return
        if (fSkipValidationDepth >= 0) {
            // but if this is the top element that we are skipping,
            // restore the states.
            if (fSkipValidationDepth == fElementDepth && fSkipValidationDepth > 0) {
                // set the partial validation depth to the depth of parent
                fNFullValidationDepth = fSkipValidationDepth - 1;
                fSkipValidationDepth = -1;
                fElementDepth--;
                fSubElement = fSubElementStack[fElementDepth];
                fCurrentElemDecl = fElemDeclStack[fElementDepth];
                fNil = fNilStack[fElementDepth];
                fNotation = fNotationStack[fElementDepth];
                fCurrentType = fTypeStack[fElementDepth];
                fCurrentCM = fCMStack[fElementDepth];
                fStrictAssess = fStrictAssessStack[fElementDepth];
                fCurrCMState = fCMStateStack[fElementDepth];
                fSawText = fSawTextStack[fElementDepth];
                fSawCharacters = fStringContent[fElementDepth];
            } 
            else {
                fElementDepth--;
            }

            // PSVI: validation attempted:
            // use default values in psvi item for
            // validation attempted, validity, and error codes

            // check extra schema constraints on root element
            if (fElementDepth == -1 && fFullChecking && !fUseGrammarPoolOnly) {
                fXSConstraints.fullSchemaChecking(
                    fGrammarBucket,
                    fSubGroupHandler,
                    fCMBuilder,
                    fXSIErrorReporter.fErrorReporter);
            }

            if (fAugPSVI)
                augs = getEmptyAugs(augs);
            
            // delegate to assertions validator subcomponent
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                assertionValidatorEndElementDelegate(element);            
            }
            
            return augs;
        }

        // now validate the content of the element
        processElementContent(element);
        
        // Pop the ID context
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fIDContext.popContext();
        }

        if (fIDCChecking) {
            // Element Locally Valid (Element)
            // 6 The element information item must be valid with respect to each of the {identity-constraint definitions} as per Identity-constraint Satisfied (3.11.4).
            
            // call matchers and de-activate context
            int oldCount = fMatcherStack.getMatcherCount();
            for (int i = oldCount - 1; i >= 0; i--) {
                XPathMatcher matcher = fMatcherStack.getMatcherAt(i);
                if (fCurrentElemDecl == null) {
                    matcher.endElement(element, fCurrentType, false, fValidatedInfo.actualValue, fValidatedInfo.actualValueType, fValidatedInfo.itemValueTypes);
                }
                else {
                    matcher.endElement(
                            element,
                            fCurrentType,
                            fCurrentElemDecl.getNillable(),
                            fDefaultValue == null
                                ? fValidatedInfo.actualValue
                                : fCurrentElemDecl.fDefault.actualValue,
                            fDefaultValue == null
                                ? fValidatedInfo.actualValueType
                                : fCurrentElemDecl.fDefault.actualValueType,
                            fDefaultValue == null
                                ? fValidatedInfo.itemValueTypes
                                : fCurrentElemDecl.fDefault.itemValueTypes);
                }
            }
            
            if (fMatcherStack.size() > 0) {
                fMatcherStack.popContext();
            }
            
            int newCount = fMatcherStack.getMatcherCount();
            // handle everything *but* keyref's.
            for (int i = oldCount - 1; i >= newCount; i--) {
                XPathMatcher matcher = fMatcherStack.getMatcherAt(i);
                if (matcher instanceof Selector.Matcher) {
                    Selector.Matcher selMatcher = (Selector.Matcher) matcher;
                    IdentityConstraint id;
                    if ((id = selMatcher.getIdentityConstraint()) != null
                            && id.getCategory() != IdentityConstraint.IC_KEYREF) {
                        fValueStoreCache.transplant(id, selMatcher.getInitialDepth());
                    }
                }
            }
            
            // now handle keyref's/...
            for (int i = oldCount - 1; i >= newCount; i--) {
                XPathMatcher matcher = fMatcherStack.getMatcherAt(i);
                if (matcher instanceof Selector.Matcher) {
                    Selector.Matcher selMatcher = (Selector.Matcher) matcher;
                    IdentityConstraint id;
                    if ((id = selMatcher.getIdentityConstraint()) != null
                            && id.getCategory() == IdentityConstraint.IC_KEYREF) {
                        ValueStoreBase values =
                            fValueStoreCache.getValueStoreFor(id, selMatcher.getInitialDepth());
                        if (values != null) {    
                            values.endDocumentFragment();   // nothing to do if nothing matched                                         
                            /*if (values.fValuesCount != values.fFieldCount) {
                                // report error if not all fields are present
                                reportSchemaError("KeyRefNotEnoughValues", new Object[] { element.rawname, values.getIdentityConstraint().getName() }); 
                            } */
                        }                                               
                    }
                }
            }
            fValueStoreCache.endElement();
        }
        
        // delegate to assertions validator subcomponent
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            try {
               assertionValidatorEndElementDelegate(element); 
            }
            catch(Exception ex) {
               throw new XMLParseException(fLocator, ex.getMessage());
            }
        }

        // Check if we should modify the xsi:type ignore depth
        // This check is independent of whether this is the validation root,
        // and should be done before the element depth is decremented.
        if (fElementDepth < fIgnoreXSITypeDepth) {
            fIgnoreXSITypeDepth--;
        }
        
        SchemaGrammar[] grammars = null;
        // have we reached the end tag of the validation root?
        if (fElementDepth == 0) {
            // 7 If the element information item is the validation root, it must be valid per Validation Root Valid (ID/IDREF) (3.3.4).
            Iterator invIdRefs = fValidationState.checkIDRefID();
            fValidationState.resetIDTables();
            if (invIdRefs != null) {
                while (invIdRefs.hasNext()) {
                    reportSchemaError("cvc-id.1", new Object[] { invIdRefs.next() });
                }    
            }
            // check extra schema constraints
            if (fFullChecking && !fUseGrammarPoolOnly) {
                fXSConstraints.fullSchemaChecking(
                    fGrammarBucket,
                    fSubGroupHandler,
                    fCMBuilder,
                    fXSIErrorReporter.fErrorReporter);
            }

            grammars = fGrammarBucket.getGrammars();
            // return the final set of grammars validator ended up with
            if (fGrammarPool != null) {
                // Set grammars as immutable
                for (int k=0; k < grammars.length; k++) {
                    grammars[k].setImmutable(true);
                }
                fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_SCHEMA, grammars);
            }
            augs = endElementPSVI(true, grammars, augs);
        } else {
            augs = endElementPSVI(false, grammars, augs);

            // decrease element depth and restore states
            fElementDepth--;

            // get the states for the parent element.
            fSubElement = fSubElementStack[fElementDepth];
            fCurrentElemDecl = fElemDeclStack[fElementDepth];
            fNil = fNilStack[fElementDepth];
            fNotation = fNotationStack[fElementDepth];
            fCurrentType = fTypeStack[fElementDepth];
            fCurrentCM = fCMStack[fElementDepth];
            fStrictAssess = fStrictAssessStack[fElementDepth];
            fCurrCMState = fCMStateStack[fElementDepth];
            fSawText = fSawTextStack[fElementDepth];
            fSawCharacters = fStringContent[fElementDepth];

            // We should have a stack for whitespace value, and pop it up here.
            // But when fWhiteSpace != -1, and we see a sub-element, it must be
            // an error (at least for Schema 1.0). So for valid documents, the
            // only value we are going to push/pop in the stack is -1.
            // Here we just mimic the effect of popping -1. -SG
            fWhiteSpace = -1;
            // Same for append buffer. Simple types and elements with fixed
            // value constraint don't allow sub-elements. -SG
            fAppendBuffer = false;
            // same here.
            fUnionType = false;
        }                

        return augs;
    } // handleEndElement(QName,boolean)*/

    /*
     * Delegate to assertions validator endElement handler.
     */
    private void assertionValidatorEndElementDelegate(QName element) {
        
        // initialize augmentation information to be passed to assertions processor
        Augmentations assertAugs = new AugmentationsImpl();
        ElementPSVImpl assertElemPSVI = new ElementPSVImpl();
        assertElemPSVI.fDeclaration = fCurrentElemDecl;
        assertElemPSVI.fTypeDecl = fCurrentType;
        assertElemPSVI.fNotation = fNotation;
        assertElemPSVI.fGrammars = fGrammarBucket.getGrammars();
        assertAugs.putItem(Constants.ELEMENT_PSVI, assertElemPSVI);
        assertAugs.putItem(XSAssertConstants.isAssertProcNeededForUnionElem, Boolean.valueOf(fIsAssertProcessingNeededForSTUnionElem));            
        fAssertionValidator.handleEndElement(element, assertAugs);
        fFailedAssertions = assertElemPSVI.fFailedAssertions;
        if (fAugPSVI && fIsAssertProcessingNeededForSTUnionElem) {
            // update PSVI
            fValidatedInfo.memberType = assertElemPSVI.fValue.memberType;                
        } 
        fIsAssertProcessingNeededForSTUnionElem = true;
        
    } // assertionValidatorEndElementDelegate

    final Augmentations endElementPSVI(
        boolean root,
        SchemaGrammar[] grammars,
        Augmentations augs) {

        if (fAugPSVI) {
            augs = getEmptyAugs(augs);

            // the 5 properties sent on startElement calls
            fCurrentPSVI.fDeclaration = this.fCurrentElemDecl;
            fCurrentPSVI.fTypeDecl = this.fCurrentType;
            fCurrentPSVI.fNotation = this.fNotation;
            fCurrentPSVI.fValidationContext = this.fValidationRoot;
            fCurrentPSVI.fNil = this.fNil;
            if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                fCurrentPSVI.fTypeAlternative = this.fTypeAlternative;
                ObjectList inheritedAttributesForPsvi = null;
                if (fInhrAttrCountStack.size() > 0) {
                    fInheritableAttrList.setSize(fInhrAttrCountStack.pop());
                    inheritedAttributesForPsvi = fTypeAlternativeValidator.getInheritedAttributesForPSVI();
                }
                fCurrentPSVI.fInheritedAttributes = inheritedAttributesForPsvi;
                fCurrentPSVI.fFailedAssertions = this.fFailedAssertions;
            }
            // PSVI: validation attempted
            // nothing below or at the same level has none or partial
            // (which means this level is strictly assessed, and all chidren
            // are full), so this one has full
            if (fElementDepth > fNFullValidationDepth) {
                fCurrentPSVI.fValidationAttempted = ElementPSVI.VALIDATION_FULL;
            }
            // nothing below or at the same level has full or partial
            // (which means this level is not strictly assessed, and all chidren
            // are none), so this one has none
            else if (fElementDepth > fNNoneValidationDepth) {
                fCurrentPSVI.fValidationAttempted = ElementPSVI.VALIDATION_NONE;
            }
            // otherwise partial, and anything above this level will be partial
            else {
                fCurrentPSVI.fValidationAttempted = ElementPSVI.VALIDATION_PARTIAL;
            }
            
            // this guarantees that depth settings do not cross-over between sibling nodes
            if (fNFullValidationDepth == fElementDepth) {
                fNFullValidationDepth = fElementDepth - 1;
            }
            if (fNNoneValidationDepth == fElementDepth) {
                fNNoneValidationDepth = fElementDepth - 1;
            }

            if (fDefaultValue != null)
                fCurrentPSVI.fSpecified = true;
            fCurrentPSVI.fValue.copyFrom(fValidatedInfo);

            if (fStrictAssess) {
                // get all errors for the current element, its attribute,
                // and subelements (if they were strictly assessed).
                // any error would make this element invalid.
                // and we merge these errors to the parent element.
                String[] errors = fXSIErrorReporter.mergeContext();

                // PSVI: error codes
                fCurrentPSVI.fErrors = errors;
                // PSVI: validity
                fCurrentPSVI.fValidity =
                    (errors == null) ? ElementPSVI.VALIDITY_VALID : ElementPSVI.VALIDITY_INVALID;
            } else {
                // PSVI: validity
                fCurrentPSVI.fValidity = ElementPSVI.VALIDITY_NOTKNOWN;
                // Discard the current context: ignore any error happened within
                // the sub-elements/attributes of this element, because those
                // errors won't affect the validity of the parent elements.
                fXSIErrorReporter.popContext();
            }

            if (root) {
                // store [schema information] in the PSVI
                fCurrentPSVI.fGrammars = grammars;
                fCurrentPSVI.fSchemaInformation = null;
            }
        }

        return augs;

    }

    Augmentations getEmptyAugs(Augmentations augs) {
        if (augs == null) {
            augs = fAugmentations;
            augs.removeAllItems();
        }
        augs.putItem(Constants.ELEMENT_PSVI, fCurrentPSVI);
        fCurrentPSVI.reset();

        return augs;
    }

    void storeLocations(String sLocation, String nsLocation) {
        if (sLocation != null) {
            if (!XMLSchemaLoader.tokenizeSchemaLocationStr(sLocation, fLocationPairs, fLocator == null ? null : fLocator.getExpandedSystemId())) {
                // error!
                fXSIErrorReporter.reportError(
                    XSMessageFormatter.SCHEMA_DOMAIN,
                    "SchemaLocation",
                    new Object[] { sLocation },
                    XMLErrorReporter.SEVERITY_WARNING);
            }
        }
        if (nsLocation != null) {
            XMLSchemaLoader.LocationArray la =
                ((XMLSchemaLoader.LocationArray) fLocationPairs.get(XMLSymbols.EMPTY_STRING));
            if (la == null) {
                la = new XMLSchemaLoader.LocationArray();
                fLocationPairs.put(XMLSymbols.EMPTY_STRING, la);
            }
            if (fLocator != null) {
                try {
                    nsLocation = XMLEntityManager.expandSystemId(nsLocation, fLocator.getExpandedSystemId(), false);
                } catch (MalformedURIException e) {
                }
            }
            la.addLocation(nsLocation);
        }

    } //storeLocations

    private boolean isValidBuiltInTypeName(String localpart) {
        if (fSchemaVersion == Constants.SCHEMA_VERSION_1_0_EXTENDED) {
            if (localpart.equals("duration") ||
                    localpart.equals("yearMonthDuration") ||
                    localpart.equals("dayTimeDuration")) {
                return false;
            }
        }
        return true;
    }

    XSTypeDefinition getAndCheckXsiType(QName element, String xsiType, XMLAttributes attributes) {
        // This method also deals with clause 1.2.1.2 of the constraint
        // Validation Rule: Schema-Validity Assessment (Element)

        // Element Locally Valid (Element)
        // 4 If there is an attribute information item among the element information item's [attributes] whose [namespace name] is identical to http://www.w3.org/2001/XMLSchema-instance and whose [local name] is type, then all of the following must be true:
        // 4.1 The normalized value of that attribute information item must be valid with respect to the built-in QName simple type, as defined by String Valid (3.14.4);
        QName typeName = null;
        try {
            typeName = (QName) fQNameDV.validate(xsiType, fValidationState, null);
        } catch (InvalidDatatypeValueException e) {
            reportSchemaError(e.getKey(), e.getArgs());
            reportSchemaError(
                "cvc-elt.4.1",
                new Object[] {
                    element.rawname,
                    SchemaSymbols.URI_XSI + "," + SchemaSymbols.XSI_TYPE,
                    xsiType });
            return null;
        }

        // 4.2 The local name and namespace name (as defined in QName Interpretation (3.15.3)), of the actual value of that attribute information item must resolve to a type definition, as defined in QName resolution (Instance) (3.15.4)
        XSTypeDefinition type = null;
        // if the namespace is schema namespace, first try built-in types
        if (typeName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA) {
            if (isValidBuiltInTypeName(typeName.localpart)) {
                SchemaGrammar s4s = SchemaGrammar.getS4SGrammar(fSchemaVersion);
                type = s4s.getGlobalTypeDecl(typeName.localpart);
            }
        }
        // if it's not schema built-in types, then try to get a grammar
        if (type == null) {
            //try to find schema grammar by different means....
            SchemaGrammar grammar =
                findSchemaGrammar(
                    XSDDescription.CONTEXT_XSITYPE,
                    typeName.uri,
                    element,
                    typeName,
                    attributes);

            if (grammar != null)
                type = grammar.getGlobalTypeDecl(typeName.localpart);
        }
        // still couldn't find the type, report an error
        if (type == null) {
            reportSchemaError("cvc-elt.4.2", new Object[] { element.rawname, xsiType });
            return null;
        }

        // if there is no current type, set this one as current.
        // and we don't need to do extra checking
        if (fCurrentType != null) {
            short block = XSConstants.DERIVATION_NONE;
            // 4.3 The local type definition must be validly derived from the {type definition} given the union of the {disallowed substitutions} and the {type definition}'s {prohibited substitutions}, as defined in Type Derivation OK (Complex) (3.4.6) (if it is a complex type definition), or given {disallowed substitutions} as defined in Type Derivation OK (Simple) (3.14.6) (if it is a simple type definition).
            // Note: It's possible to have fCurrentType be non-null and fCurrentElemDecl
            // be null, if the current type is set using the property "root-type-definition".
            // In that case, we don't disallow any substitutions. -PM
            if (fCurrentElemDecl != null) {
                block = fCurrentElemDecl.fBlock;
            }
            if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                block |= ((XSComplexTypeDecl) fCurrentType).fBlock;
            }
            if (!fXSConstraints.checkTypeDerivationOk(type, fCurrentType, block)) {
                reportSchemaError(
                        "cvc-elt.4.3",
                        new Object[] { element.rawname, xsiType, XS11TypeHelper.getSchemaTypeName(fCurrentType)});
            }
        }

        return type;
    } //getAndCheckXsiType
    
    XSTypeDefinition getAndCheckXsiType(QName element, String xsiType, XMLAttributes attributes,
            ArrayList errorList) {
        // This method also deals with clause 1.2.1.2 of the constraint
        // Validation Rule: Schema-Validity Assessment (Element)

        // Element Locally Valid (Element)
        // 4 If there is an attribute information item among the element information item's [attributes] whose [namespace name] is identical to http://www.w3.org/2001/XMLSchema-instance and whose [local name] is type, then all of the following must be true:
        // 4.1 The normalized value of that attribute information item must be valid with respect to the built-in QName simple type, as defined by String Valid (3.14.4);
        QName typeName = null;
        try {
            typeName = (QName) fQNameDV.validate(xsiType, fValidationState, null);
        } catch (InvalidDatatypeValueException e) {
            errorList.add(e.getKey());
            errorList.add(e.getArgs());
            errorList.add("cvc-elt.4.1");
            errorList.add(new Object[] {
                    element.rawname,
                    SchemaSymbols.URI_XSI + "," + SchemaSymbols.XSI_TYPE,
                    xsiType });
            return null;
        }

        // 4.2 The local name and namespace name (as defined in QName Interpretation (3.15.3)), of the actual value of that attribute information item must resolve to a type definition, as defined in QName resolution (Instance) (3.15.4)
        XSTypeDefinition type = null;
        // if the namespace is schema namespace, first try built-in types
        if (typeName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA) {
            if (isValidBuiltInTypeName(typeName.localpart)) {
                SchemaGrammar s4s = SchemaGrammar.getS4SGrammar(fSchemaVersion);
                type = s4s.getGlobalTypeDecl(typeName.localpart);
            }
        }
        // if it's not schema built-in types, then try to get a grammar
        if (type == null) {
            //try to find schema grammar by different means....
            SchemaGrammar grammar =
                findSchemaGrammar(
                    XSDDescription.CONTEXT_XSITYPE,
                    typeName.uri,
                    element,
                    typeName,
                    attributes);

            if (grammar != null)
                type = grammar.getGlobalTypeDecl(typeName.localpart);
        }
        // still couldn't find the type, report an error
        if (type == null) {
            errorList.add("cvc-elt.4.2");
            errorList.add(new Object[] { element.rawname, xsiType });
            return null;
        }

        // if there is no current type, set this one as current.
        // and we don't need to do extra checking
        if (fCurrentType != null) {
            short block = XSConstants.DERIVATION_NONE;
            // 4.3 The local type definition must be validly derived from the {type definition} given the union of the {disallowed substitutions} and the {type definition}'s {prohibited substitutions}, as defined in Type Derivation OK (Complex) (3.4.6) (if it is a complex type definition), or given {disallowed substitutions} as defined in Type Derivation OK (Simple) (3.14.6) (if it is a simple type definition).
            // Note: It's possible to have fCurrentType be non-null and fCurrentElemDecl
            // be null, if the current type is set using the property "root-type-definition".
            // In that case, we don't disallow any substitutions. -PM
            if (fCurrentElemDecl != null) {
                block = fCurrentElemDecl.fBlock;
            }
            if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                block |= ((XSComplexTypeDecl) fCurrentType).fBlock;
            }
            if (!fXSConstraints.checkTypeDerivationOk(type, fCurrentType, block)) {
                errorList.add("cvc-elt.4.3");
                errorList.add(new Object[] { element.rawname, xsiType, XS11TypeHelper.getSchemaTypeName(fCurrentType)});
            }
        }

        return type;
    } //getAndCheckXsiType

    boolean getXsiNil(QName element, String xsiNil) {
        // Element Locally Valid (Element)
        // 3 The appropriate case among the following must be true:
        // 3.1 If {nillable} is false, then there must be no attribute information item among the element information item's [attributes] whose [namespace name] is identical to http://www.w3.org/2001/XMLSchema-instance and whose [local name] is nil.
        if (fCurrentElemDecl != null && !fCurrentElemDecl.getNillable()) {
            reportSchemaError(
                "cvc-elt.3.1",
                new Object[] {
                    element.rawname,
                    SchemaSymbols.URI_XSI + "," + SchemaSymbols.XSI_NIL });
        }
        // 3.2 If {nillable} is true and there is such an attribute information item and its actual value is true , then all of the following must be true:
        // 3.2.2 There must be no fixed {value constraint}.
        else {
            String value = XMLChar.trim(xsiNil);
            if (value.equals(SchemaSymbols.ATTVAL_TRUE)
                || value.equals(SchemaSymbols.ATTVAL_TRUE_1)) {
                if (fCurrentElemDecl != null
                    && fCurrentElemDecl.getConstraintType() == XSConstants.VC_FIXED) {
                    reportSchemaError(
                        "cvc-elt.3.2.2",
                        new Object[] {
                            element.rawname,
                            SchemaSymbols.URI_XSI + "," + SchemaSymbols.XSI_NIL });
                }
                return true;
            }
        }
        return false;
    }

    boolean allowAttribute(XSWildcardDecl attrWildcard, QName name, SchemaGrammar grammar) {
        if (attrWildcard.allowQName(name)) {
            if (grammar == null || !attrWildcard.fDisallowedDefined) {
                return true;
            }
            return (grammar.getGlobalAttributeDecl(name.localpart) == null);
        }
        return false;
    }

    void processAttributes(QName element, XMLAttributes attributes, XSAttributeGroupDecl attrGrp) {

        if (DEBUG) {
            System.out.println("==>processAttributes: " + attributes.getLength());
        }
        
        // whether we have seen a Wildcard ID.
        String wildcardIDName = null;

        // for each present attribute
        int attCount = attributes.getLength();

        Augmentations augs = null;
        AttributePSVImpl attrPSVI = null;

        boolean isSimple =
            fCurrentType == null || fCurrentType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE;

        XSObjectList attrUses = null;
        int useCount = 0;
        XSWildcardDecl attrWildcard = null;
        if (!isSimple) {
            attrUses = attrGrp.getAttributeUses();
            useCount = attrUses.getLength();
            attrWildcard = attrGrp.fAttributeWC;
        }

        // Element Locally Valid (Complex Type)
        // 3 For each attribute information item in the element information item's [attributes] excepting
        // those whose [namespace name] is identical to http://www.w3.org/2001/XMLSchema-instance and whose
        // [local name] is one of type, nil, schemaLocation or noNamespaceSchemaLocation, the appropriate 
        // case among the following must be true:
        // get the corresponding attribute decl
        for (int index = 0; index < attCount; index++) {

            attributes.getName(index, fTempQName);

            if (DEBUG) {
                System.out.println("==>process attribute: " + fTempQName);
            }

            if (fAugPSVI || fIdConstraint) {
                augs = attributes.getAugmentations(index);
                attrPSVI = (AttributePSVImpl) augs.getItem(Constants.ATTRIBUTE_PSVI);
                if (attrPSVI != null) {
                    attrPSVI.reset();
                } else {
                    attrPSVI = new AttributePSVImpl();
                    augs.putItem(Constants.ATTRIBUTE_PSVI, attrPSVI);
                }
                // PSVI attribute: validation context
                attrPSVI.fValidationContext = fValidationRoot;
            }

            // Element Locally Valid (Type)
            // 3.1.1 The element information item's [attributes] must be empty, excepting those
            // whose [namespace name] is identical to http://www.w3.org/2001/XMLSchema-instance and
            // whose [local name] is one of type, nil, schemaLocation or noNamespaceSchemaLocation.

            // for the 4 xsi attributes, get appropriate decl, and validate
            if (fTempQName.uri == SchemaSymbols.URI_XSI) {
                XSAttributeDecl attrDecl = null;
                if (fTempQName.localpart == SchemaSymbols.XSI_TYPE) {
                    attrDecl = XSI_TYPE;
                }
                else if (fTempQName.localpart == SchemaSymbols.XSI_NIL) {
                    attrDecl = XSI_NIL;
                }
                else if (fTempQName.localpart == SchemaSymbols.XSI_SCHEMALOCATION) {
                    attrDecl = XSI_SCHEMALOCATION;
                }
                else if (fTempQName.localpart == SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION) {
                    attrDecl = XSI_NONAMESPACESCHEMALOCATION;
                }
                if (attrDecl != null) {
                    processOneAttribute(element, attributes, index, attrDecl, null, attrPSVI);
                    continue;
                }
            }

            // for namespace attributes, no_validation/unknow_validity
            if (fTempQName.rawname == XMLSymbols.PREFIX_XMLNS
                || fTempQName.rawname.startsWith("xmlns:")) {
                continue;
            }

            // simple type doesn't allow any other attributes
            if (isSimple) {
                reportSchemaError(
                    "cvc-type.3.1.1",
                    new Object[] { element.rawname, fTempQName.rawname });
                continue;
            }

            // it's not xmlns, and not xsi, then we need to find a decl for it
            XSAttributeUseImpl currUse = null, oneUse;
            for (int i = 0; i < useCount; i++) {
                oneUse = (XSAttributeUseImpl) attrUses.item(i);
                if (oneUse.fAttrDecl.fName == fTempQName.localpart
                    && oneUse.fAttrDecl.fTargetNamespace == fTempQName.uri) {
                    currUse = oneUse;
                    break;
                }
            }

            // 3.2 otherwise all of the following must be true:
            // 3.2.1 There must be an {attribute wildcard}.
            // 3.2.2 The attribute information item must be valid with respect to it as defined in Item Valid (Wildcard) (3.10.4).

            // if failed, get it from wildcard
            if (currUse == null) {
                //if (attrWildcard == null)
                //    reportSchemaError("cvc-complex-type.3.2.1", new Object[]{element.rawname, fTempQName.rawname});
                SchemaGrammar grammar = (fSchemaVersion < Constants.SCHEMA_VERSION_1_1)
                    ? null : findSchemaGrammar(
                        XSDDescription.CONTEXT_ATTRIBUTE, fTempQName.uri, element, fTempQName, attributes);
                if (attrWildcard == null || !allowAttribute(attrWildcard, fTempQName, grammar)) {
                    // so this attribute is not allowed
                    reportSchemaError(
                        "cvc-complex-type.3.2.2",
                        new Object[] { element.rawname, fTempQName.rawname });
                    
                    // We have seen an attribute that was not declared
                    fNFullValidationDepth = fElementDepth;
                    
                    continue;
                }
            }

            XSAttributeDecl currDecl = null;
            if (currUse != null) {
                currDecl = currUse.fAttrDecl;
            } else {
                // which means it matches a wildcard
                // skip it if processContents is skip
                if (attrWildcard.fProcessContents == XSWildcardDecl.PC_SKIP)
                    continue;

                //try to find grammar by different means...
                SchemaGrammar grammar =
                    findSchemaGrammar(
                        XSDDescription.CONTEXT_ATTRIBUTE,
                        fTempQName.uri,
                        element,
                        fTempQName,
                        attributes);

                if (grammar != null) {
                    currDecl = grammar.getGlobalAttributeDecl(fTempQName.localpart);
                }

                // if can't find
                if (currDecl == null) {                    
                    // if strict, report error
                    if (attrWildcard.fProcessContents == XSWildcardDecl.PC_STRICT) {
                        reportSchemaError(
                           "cvc-complex-type.3.2.2",
                           new Object[] { element.rawname, fTempQName.rawname });    
                     }
                    
                    // then continue to the next attribute
                    continue;
                } else {
                    // 5 Let [Definition:]  the wild IDs be the set of all attribute information item to which clause 3.2 applied and whose validation resulted in a context-determined declaration of mustFind or no context-determined declaration at all, and whose [local name] and [namespace name] resolve (as defined by QName resolution (Instance) (3.15.4)) to an attribute declaration whose {type definition} is or is derived from ID. Then all of the following must be true:
                    // 5.1 There must be no more than one item in wild IDs.
                	//
                	// Only applies to XML Schema 1.0
                    if (fSchemaVersion < Constants.SCHEMA_VERSION_1_1
                        && currDecl.fType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE
                        && ((XSSimpleType) currDecl.fType).isIDType()) {
                        if (wildcardIDName != null) {
                            reportSchemaError(
                                "cvc-complex-type.5.1",
                                new Object[] { element.rawname, currDecl.fName, wildcardIDName });
                        } else
                            wildcardIDName = currDecl.fName;
                    }
                }
            }

            processOneAttribute(element, attributes, index, currDecl, currUse, attrPSVI);
        } // end of for (all attributes)

        // 5.2 If wild IDs is non-empty, there must not be any attribute uses among the {attribute uses} whose {attribute declaration}'s {type definition} is or is derived from ID.
        //
        // Only applies to XML Schema 1.0
        // Since we do not set the wildcardIDName if it's 1.1, no need to explicitly check for the version
        if (!isSimple && attrGrp.fIDAttrName != null && wildcardIDName != null) {
            reportSchemaError(
                "cvc-complex-type.5.2",
                new Object[] { element.rawname, wildcardIDName, attrGrp.fIDAttrName });
        }

    } //processAttributes

    void processOneAttribute(
        QName element,
        XMLAttributes attributes,
        int index,
        XSAttributeDecl currDecl,
        XSAttributeUseImpl currUse,
        AttributePSVImpl attrPSVI) {

        String attrValue = attributes.getValue(index);
        fXSIErrorReporter.pushContext();

        // Attribute Locally Valid
        // For an attribute information item to be locally valid with respect to an attribute declaration all of the following must be true:
        // 1 The declaration must not be absent (see Missing Sub-components (5.3) for how this can fail to be the case).
        // 2 Its {type definition} must not be absent.
        // 3 The item's normalized value must be locally valid with respect to that {type definition} as per String Valid (3.14.4).
        // get simple type
        XSSimpleType attDV = currDecl.fType;

        Object actualValue = null;
        try {
            actualValue = attDV.validate(attrValue, fValidationState, fValidatedInfo);
            
            // store the normalized value
            if (fNormalizeData) {
                attributes.setValue(index, fValidatedInfo.normalizedValue);
            }
            // PSVI: element notation
            if (attDV.getVariety() == XSSimpleType.VARIETY_ATOMIC
                && attDV.getPrimitiveKind() == XSSimpleType.PRIMITIVE_NOTATION) {
                QName qName = (QName) actualValue;
                SchemaGrammar grammar = fGrammarBucket.getGrammar(qName.uri);

                //REVISIT: is it possible for the notation to be in different namespace than the attribute
                //with which it is associated, CHECK !!  <fof n1:att1 = "n2:notation1" ..>
                // should we give chance to the application to be able to  retrieve a grammar - nb
                //REVISIT: what would be the triggering component here.. if it is attribute value that
                // triggered the loading of grammar ?? -nb

                if (grammar != null) {
                    fNotation = grammar.getGlobalNotationDecl(qName.localpart);
                }
            }
        } 
        catch (InvalidDatatypeValueException idve) {
            reportSchemaError(idve.getKey(), idve.getArgs());
            reportSchemaError(
                "cvc-attribute.3",
                new Object[] { element.rawname, fTempQName.rawname, attrValue, 
                        (attDV instanceof XSSimpleTypeDecl) ? 
                                ((XSSimpleTypeDecl) attDV).getTypeName() : attDV.getName()});
        }

        // get the value constraint from use or decl
        // 4 The item's actual value must match the value of the {value constraint}, if it is present and fixed.                 // now check the value against the simpleType
        if (actualValue != null && currDecl.getConstraintType() == XSConstants.VC_FIXED) {
            if (!EqualityHelper.isEqual(fValidatedInfo, currDecl.fDefault, fSchemaVersion)) {
                reportSchemaError(
                    "cvc-attribute.4",
                    new Object[] {
                        element.rawname,
                        fTempQName.rawname,
                        attrValue,
                        currDecl.fDefault.stringValue()});
            }
        }

        // 3.1 If there is among the {attribute uses} an attribute use with an {attribute declaration} whose {name} matches the attribute information item's [local name] and whose {target namespace} is identical to the attribute information item's [namespace name] (where an absent {target namespace} is taken to be identical to a [namespace name] with no value), then the attribute information must be valid with respect to that attribute use as per Attribute Locally Valid (Use) (3.5.4). In this case the {attribute declaration} of that attribute use is the context-determined declaration for the attribute information item with respect to Schema-Validity Assessment (Attribute) (3.2.4) and Assessment Outcome (Attribute) (3.2.5).
        if (actualValue != null
            && currUse != null
            && currUse.fConstraintType == XSConstants.VC_FIXED) {
            if (!EqualityHelper.isEqual(fValidatedInfo, currUse.fDefault, fSchemaVersion)) {
                reportSchemaError(
                    "cvc-complex-type.3.1",
                    new Object[] {
                        element.rawname,
                        fTempQName.rawname,
                        attrValue,
                        currUse.fDefault.stringValue()});
            }
        }
        if (fIdConstraint) {
            attrPSVI.fValue.copyFrom(fValidatedInfo);
        }

        if (fAugPSVI) {
            // PSVI: attribute declaration
            attrPSVI.fDeclaration = currDecl;
            // PSVI: attribute type
            attrPSVI.fTypeDecl = attDV;

            // PSVI: attribute normalized value
            // NOTE: we always store the normalized value, even if it's invlid,
            // because it might still be useful to the user. But when the it's
            // not valid, the normalized value is not trustable.
            attrPSVI.fValue.copyFrom(fValidatedInfo);

            // PSVI: validation attempted:
            attrPSVI.fValidationAttempted = AttributePSVI.VALIDATION_FULL;
   
            // We have seen an attribute that was declared.
            fNNoneValidationDepth = fElementDepth;
            
            String[] errors = fXSIErrorReporter.mergeContext();
            // PSVI: error codes
            attrPSVI.fErrors = errors;
            // PSVI: validity
            attrPSVI.fValidity =
                (errors == null) ? AttributePSVI.VALIDITY_VALID : AttributePSVI.VALIDITY_INVALID;
        }
    }

    void addDefaultAttributes(
        QName element,
        XMLAttributes attributes,
        XSAttributeGroupDecl attrGrp) {
        // Check after all specified attrs are scanned
        // (1) report error for REQUIRED attrs that are missing (V_TAGc)
        // REVISIT: should we check prohibited attributes?
        // (2) report error for PROHIBITED attrs that are present (V_TAGc)
        // (3) add default attrs (FIXED and NOT_FIXED)
        //
        if (DEBUG) {
            System.out.println("==>addDefaultAttributes: " + element);
        }
        XSObjectList attrUses = attrGrp.getAttributeUses();
        int useCount = attrUses.getLength();
        XSAttributeUseImpl currUse;
        XSAttributeDecl currDecl;
        short constType;
        ValidatedInfo defaultValue;
        boolean isSpecified;
        QName attName;
        // for each attribute use
        for (int i = 0; i < useCount; i++) {

            currUse = (XSAttributeUseImpl) attrUses.item(i);
            currDecl = currUse.fAttrDecl;
            // get value constraint
            constType = currUse.fConstraintType;
            defaultValue = currUse.fDefault;
            if (constType == XSConstants.VC_NONE) {
                constType = currDecl.getConstraintType();
                defaultValue = currDecl.fDefault;
            }
            // whether this attribute is specified
            isSpecified = attributes.getValue(currDecl.fTargetNamespace, currDecl.fName) != null;

            // Element Locally Valid (Complex Type)
            // 4 The {attribute declaration} of each attribute use in the {attribute uses} whose
            // {required} is true matches one of the attribute information items in the element
            // information item's [attributes] as per clause 3.1 above.
            if (currUse.fUse == SchemaSymbols.USE_REQUIRED) {
                if (!isSpecified)                    
                    reportSchemaError(
                        "cvc-complex-type.4",
                        new Object[] { element.rawname, currDecl.fName });
            }
            // if the attribute is not specified, then apply the value constraint
            if (!isSpecified && constType != XSConstants.VC_NONE) {
                
                // Apply extra checking rules (ID/IDREF/ENTITY)
                final XSSimpleType attDV = currDecl.fType;
                final boolean facetChecking = fValidationState.needFacetChecking(); 
                try {
                    fValidationState.setFacetChecking(false);
                    attDV.validate(fValidationState, defaultValue);
                } 
                catch (InvalidDatatypeValueException idve) {
                    reportSchemaError(idve.getKey(), idve.getArgs());
                }
                fValidationState.setFacetChecking(facetChecking);
                
                attName =
                    new QName(null, currDecl.fName, currDecl.fName, currDecl.fTargetNamespace);
                String normalized = (defaultValue != null) ? defaultValue.stringValue() : "";
                int attrIndex;
                if (attributes instanceof XMLAttributesImpl) {
                    XMLAttributesImpl attrs = (XMLAttributesImpl) attributes;
                    attrIndex = attrs.getLength();
                    attrs.addAttributeNS(attName, "CDATA", normalized);
                }
                else {
                    attrIndex = attributes.addAttribute(attName, "CDATA", normalized);
                }

                if (fAugPSVI) {

                    // PSVI: attribute is "schema" specified
                    Augmentations augs = attributes.getAugmentations(attrIndex);
                    AttributePSVImpl attrPSVI = new AttributePSVImpl();
                    augs.putItem(Constants.ATTRIBUTE_PSVI, attrPSVI);

                    attrPSVI.fDeclaration = currDecl;
                    attrPSVI.fTypeDecl = currDecl.fType;
                    attrPSVI.fValue.copyFrom(defaultValue);
                    attrPSVI.fValidationContext = fValidationRoot;
                    attrPSVI.fValidity = AttributePSVI.VALIDITY_VALID;
                    attrPSVI.fValidationAttempted = AttributePSVI.VALIDATION_FULL;
                    attrPSVI.fSpecified = true;
                }
            }

        } // for
    } // addDefaultAttributes

    /**
     *  If there is not text content, and there is a
     *  {value constraint} on the corresponding element decl, then return
     *  an XMLString representing the default value.
     */
    void processElementContent(QName element) {
        // 1 If the item is ?valid? with respect to an element declaration as per Element Locally Valid (Element) (?3.3.4) and the {value constraint} is present, but clause 3.2 of Element Locally Valid (Element) (?3.3.4) above is not satisfied and the item has no element or character information item [children], then schema. Furthermore, the post-schema-validation infoset has the canonical lexical representation of the {value constraint} value as the item's [schema normalized value] property.
        if (fCurrentElemDecl != null
            && fCurrentElemDecl.fDefault != null
            && !fSawText
            && !fSubElement
            && !fNil) {

            String strv = fCurrentElemDecl.fDefault.stringValue();
            int bufLen = strv.length();
            if (fNormalizedStr.ch == null || fNormalizedStr.ch.length < bufLen) {
                fNormalizedStr.ch = new char[bufLen];
            }
            strv.getChars(0, bufLen, fNormalizedStr.ch, 0);
            fNormalizedStr.offset = 0;
            fNormalizedStr.length = bufLen;
            fDefaultValue = fNormalizedStr;
        }
        // fixed values are handled later, after xsi:type determined.

        fValidatedInfo.normalizedValue = null;

        // Element Locally Valid (Element)
        // 3.2.1 The element information item must have no character or element information item [children].
        if (fNil) {
            if (fSubElement || fSawText) {
                reportSchemaError(
                    "cvc-elt.3.2.1",
                    new Object[] {
                        element.rawname,
                        SchemaSymbols.URI_XSI + "," + SchemaSymbols.XSI_NIL });
            }
        }

        this.fValidatedInfo.reset();

        // 5 The appropriate case among the following must be true:
        // 5.1 If the declaration has a {value constraint}, the item has neither element nor character [children] and clause 3.2 has not applied, then all of the following must be true:
        if (fCurrentElemDecl != null
            && fCurrentElemDecl.getConstraintType() != XSConstants.VC_NONE
            && !fSubElement
            && !fSawText
            && !fNil) {
            // 5.1.1 If the actual type definition is a local type definition then the canonical lexical representation of the {value constraint} value must be a valid default for the actual type definition as defined in Element Default Valid (Immediate) (3.3.6).
            if (fCurrentType != fCurrentElemDecl.fType) {
                //REVISIT:we should pass ValidatedInfo here.
                if (fXSConstraints
                    .ElementDefaultValidImmediate(
                        fCurrentType,
                        fCurrentElemDecl.fDefault.stringValue(),
                        fState4XsiType,
                        null)
                    == null)
                    reportSchemaError(
                        "cvc-elt.5.1.1",
                        new Object[] {
                            element.rawname,
                            fCurrentType.getName(),
                            fCurrentElemDecl.fDefault.stringValue()});
            }
            // 5.1.2 The element information item with the canonical lexical representation of the {value constraint} value used as its normalized value must be valid with respect to the actual type definition as defined by Element Locally Valid (Type) (3.3.4).
            // REVISIT: don't use toString, but validateActualValue instead
            //          use the fState4ApplyDefault
            elementLocallyValidType(element, fCurrentElemDecl.fDefault.stringValue());
        } else {
            // The following method call also deal with clause 1.2.2 of the constraint
            // Validation Rule: Schema-Validity Assessment (Element)

            // 5.2 If the declaration has no {value constraint} or the item has either element or character [children] or clause 3.2 has applied, then all of the following must be true:
            // 5.2.1 The element information item must be valid with respect to the actual type definition as defined by Element Locally Valid (Type) (3.3.4).
            Object actualValue = elementLocallyValidType(element, fBuffer);
            // 5.2.2 If there is a fixed {value constraint} and clause 3.2 has not applied, all of the following must be true:
            if (fCurrentElemDecl != null
                && fCurrentElemDecl.getConstraintType() == XSConstants.VC_FIXED
                && !fNil) {
                String content = fBuffer.toString();
                // 5.2.2.1 The element information item must have no element information item [children].
                if (fSubElement)
                    reportSchemaError("cvc-elt.5.2.2.1", new Object[] { element.rawname });
                // 5.2.2.2 The appropriate case among the following must be true:
                if (fCurrentType.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE) {
                    XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;
                    // 5.2.2.2.1 If the {content type} of the actual type definition is mixed, then the initial value of the item must match the canonical lexical representation of the {value constraint} value.
                    if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                        // REVISIT: how to get the initial value, does whiteSpace count?
                        if (!fCurrentElemDecl.fDefault.normalizedValue.equals(content))
                            reportSchemaError(
                                "cvc-elt.5.2.2.2.1",
                                new Object[] {
                                    element.rawname,
                                    content,
                                    fCurrentElemDecl.fDefault.normalizedValue });
                    }
                    // 5.2.2.2.2 If the {content type} of the actual type definition is a simple type definition, then the actual value of the item must match the canonical lexical representation of the {value constraint} value.
                    else if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                        if (actualValue != null &&
                                !EqualityHelper.isEqual(fValidatedInfo, fCurrentElemDecl.fDefault, fSchemaVersion)) {
                            reportSchemaError(
                                "cvc-elt.5.2.2.2.2",
                                new Object[] {
                                    element.rawname,
                                    content,
                                    fCurrentElemDecl.fDefault.stringValue()});
                        }
                    }
                } else if (fCurrentType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                    if (actualValue != null &&
                            !EqualityHelper.isEqual(fValidatedInfo, fCurrentElemDecl.fDefault, fSchemaVersion)) {
                        // REVISIT: the spec didn't mention this case: fixed
                        //          value with simple type
                        reportSchemaError(
                            "cvc-elt.5.2.2.2.2",
                            new Object[] {
                                element.rawname,
                                content,
                                fCurrentElemDecl.fDefault.stringValue()});
                    }
                }
            }
        }

        if (fDefaultValue == null && fNormalizeData && fDocumentHandler != null && fUnionType) {
            // for union types we need to send data because we delayed sending
            // this data when we received it in the characters() call.
            String content = fValidatedInfo.normalizedValue;
            if (content == null)
                content = fBuffer.toString();

            int bufLen = content.length();
            if (fNormalizedStr.ch == null || fNormalizedStr.ch.length < bufLen) {
                fNormalizedStr.ch = new char[bufLen];
            }
            content.getChars(0, bufLen, fNormalizedStr.ch, 0);
            fNormalizedStr.offset = 0;
            fNormalizedStr.length = bufLen;
            fDocumentHandler.characters(fNormalizedStr, null);
        }
    } // processElementContent

    Object elementLocallyValidType(QName element, Object textContent) {
        if (fCurrentType == null)
            return null;

        Object retValue = null;
        // Element Locally Valid (Type)
        // 3 The appropriate case among the following must be true:
        // 3.1 If the type definition is a simple type definition, then all of the following must be true:
        if (fCurrentType.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
            // 3.1.2 The element information item must have no element information item [children].
            if (fSubElement)
                reportSchemaError("cvc-type.3.1.2", new Object[] { element.rawname });
            // 3.1.3 If clause 3.2 of Element Locally Valid (Element) (3.3.4) did not apply, then the normalized value must be valid with respect to the type definition as defined by String Valid (3.14.4).
            if (!fNil) {
                XSSimpleType dv = (XSSimpleType) fCurrentType;
                try {
                    if (!fNormalizeData || fUnionType) {
                        fValidationState.setNormalizationRequired(true);
                    }
                    retValue = dv.validate(textContent, fValidationState, fValidatedInfo);
                    
                    // additional check for assertions processing, for simple type having variety 'union'                    
                    if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                        fAssertionValidator.extraCheckForSTUnionAssertsElem(dv, String.valueOf(textContent), fValidatedInfo);
                    }
                } catch (InvalidDatatypeValueException e) {
                    fIsAssertProcessingNeededForSTUnionElem = false;
                    reportSchemaError(e.getKey(), e.getArgs());
                    reportSchemaError(
                        "cvc-type.3.1.3",
                        new Object[] { element.rawname, textContent });
                }
            }
        } else {
            // 3.2 If the type definition is a complex type definition, then the element information item must be valid with respect to the type definition as per Element Locally Valid (Complex Type) (3.4.4);
            retValue = elementLocallyValidComplexType(element, textContent);
        }

        return retValue;
    } // elementLocallyValidType

    Object elementLocallyValidComplexType(QName element, Object textContent) {
        Object actualValue = null;
        XSComplexTypeDecl ctype = (XSComplexTypeDecl) fCurrentType;

        // Element Locally Valid (Complex Type)
        // For an element information item to be locally valid with respect to a complex type definition all of the following must be true:
        // 1 {abstract} is false.
        // 2 If clause 3.2 of Element Locally Valid (Element) (3.3.4) did not apply, then the appropriate case among the following must be true:
        if (!fNil) {
            // 2.1 If the {content type} is empty, then the element information item has no character or element information item [children].
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_EMPTY
                && (fSubElement || fSawText)) {
                reportSchemaError("cvc-complex-type.2.1", new Object[] { element.rawname });
            }
            // 2.2 If the {content type} is a simple type definition, then the element information item has no element information item [children], and the normalized value of the element information item is valid with respect to that simple type definition as defined by String Valid (3.14.4).
            else if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                if (fSubElement)
                    reportSchemaError("cvc-complex-type.2.2", new Object[] { element.rawname });
                XSSimpleType dv = ctype.fXSSimpleType;
                try {
                    if (!fNormalizeData || fUnionType) {
                        fValidationState.setNormalizationRequired(true);
                    }
                    actualValue = dv.validate(textContent, fValidationState, fValidatedInfo);
                    
                    // additional check for assertions processing, for simple type having variety 'union'                    
                    if (fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                        fAssertionValidator.extraCheckForSTUnionAssertsElem(dv, String.valueOf(textContent), fValidatedInfo);
                    }
                } catch (InvalidDatatypeValueException e) {
                    fIsAssertProcessingNeededForSTUnionElem = false;
                    reportSchemaError(e.getKey(), e.getArgs());
                    reportSchemaError("cvc-complex-type.2.2", new Object[] { element.rawname });
                }
                // REVISIT: eventually, this method should return the same actualValue as elementLocallyValidType...
                // obviously it'll return null when the content is complex.
            }
            // 2.3 If the {content type} is element-only, then the element information item has no character information item [children] other than those whose [character code] is defined as a white space in [XML 1.0 (Second Edition)].
            else if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_ELEMENT) {
                if (fSawCharacters) {
                    reportSchemaError("cvc-complex-type.2.3", new Object[] { element.rawname });
                }
            }
            // 2.4 If the {content type} is element-only or mixed, then the sequence of the element information item's element information item [children], if any, taken in order, is valid with respect to the {content type}'s particle, as defined in Element Sequence Locally Valid (Particle) (3.9.4).
            if (ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_ELEMENT
                || ctype.fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                // if the current state is a valid state, check whether
                // it's one of the final states.
                if (DEBUG) {
                    System.out.println(fCurrCMState);
                }
                if (fCurrCMState[0] >= 0 && !fCurrentCM.endContentModel(fCurrCMState)) {
                    String expected = expectedStr(fCurrentCM.whatCanGoHere(fCurrCMState));
                    final int[] occurenceInfo = fCurrentCM.occurenceInfo(fCurrCMState);
                    if (occurenceInfo != null) {
                        final int minOccurs = occurenceInfo[0];
                        final int count = occurenceInfo[2];
                        // Check if this is a violation of minOccurs
                        if (count < minOccurs) {
                            final int required = minOccurs - count;
                            if (required > 1) {
                                reportSchemaError("cvc-complex-type.2.4.j", new Object[] { element.rawname, 
                                        fCurrentCM.getTermName(occurenceInfo[3]), Integer.toString(minOccurs), Integer.toString(required) });
                            }
                            else {
                                reportSchemaError("cvc-complex-type.2.4.i", new Object[] { element.rawname, 
                                        fCurrentCM.getTermName(occurenceInfo[3]), Integer.toString(minOccurs) });
                            }
                        }
                        else {
                            reportSchemaError("cvc-complex-type.2.4.b", new Object[] { element.rawname, expected });
                        }
                    }
                    else {
                        reportSchemaError("cvc-complex-type.2.4.b", new Object[] { element.rawname, expected });
                    }
                }
            }
        }
        return actualValue;
    } // elementLocallyValidComplexType
    
    void processRootTypeQName(final javax.xml.namespace.QName rootTypeQName) {
        String rootTypeNamespace = rootTypeQName.getNamespaceURI();
        // Add namespace to symbol table, to make sure it's interned.
        // This namespace may be later compared with other values using ==.
        rootTypeNamespace = fSymbolTable.addSymbol(rootTypeNamespace);
        if (rootTypeNamespace != null && rootTypeNamespace.equals(XMLConstants.NULL_NS_URI)) {
            rootTypeNamespace = null;
        }
        if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(rootTypeNamespace)) {
            String rootLocalPart = rootTypeQName.getLocalPart();
            if (isValidBuiltInTypeName(rootLocalPart)) {
                SchemaGrammar s4s = SchemaGrammar.getS4SGrammar(fSchemaVersion);
                fCurrentType = s4s.getGlobalTypeDecl(rootLocalPart);
            }
        }
        else {
            final SchemaGrammar grammarForRootType = findSchemaGrammar(
                    XSDDescription.CONTEXT_ELEMENT, rootTypeNamespace, null, null, null);
            if (grammarForRootType != null) {
                fCurrentType = grammarForRootType.getGlobalTypeDecl(rootTypeQName.getLocalPart());
            }
        }
        if (fCurrentType == null) {
            String typeName = (rootTypeQName.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) ?
                    rootTypeQName.getLocalPart() :
                        rootTypeQName.getPrefix()+":"+rootTypeQName.getLocalPart();
                    reportSchemaError("cvc-type.1", new Object[] {typeName});
        }
    } // processRootTypeQName
    
    void processRootElementDeclQName(final javax.xml.namespace.QName rootElementDeclQName, final QName element) {
        String rootElementDeclNamespace = rootElementDeclQName.getNamespaceURI();
        // Add namespace to symbol table, to make sure it's interned.
        // This namespace may be later compared with other values using ==.
        rootElementDeclNamespace = fSymbolTable.addSymbol(rootElementDeclNamespace);
        if (rootElementDeclNamespace != null && rootElementDeclNamespace.equals(XMLConstants.NULL_NS_URI)) {
            rootElementDeclNamespace = null;
        }
        final SchemaGrammar grammarForRootElement = findSchemaGrammar(
                XSDDescription.CONTEXT_ELEMENT, rootElementDeclNamespace, null, null, null);
        if (grammarForRootElement != null) {
            fCurrentElemDecl = grammarForRootElement.getGlobalElementDecl(rootElementDeclQName.getLocalPart());
        }
        if (fCurrentElemDecl == null) {
            String declName = (rootElementDeclQName.getPrefix().equals(XMLConstants.DEFAULT_NS_PREFIX)) ?
                    rootElementDeclQName.getLocalPart() :
                        rootElementDeclQName.getPrefix()+":"+rootElementDeclQName.getLocalPart();
                    reportSchemaError("cvc-elt.1.a", new Object[] {declName});
        }
        else {
            checkElementMatchesRootElementDecl(fCurrentElemDecl, element);
        }
    } // processRootElementDeclQName
    
    void checkElementMatchesRootElementDecl(final XSElementDecl rootElementDecl, final QName element) {
        // Report an error if the name of the element does 
        // not match the name of the specified element declaration.
        if (element.localpart != rootElementDecl.fName ||
            element.uri != rootElementDecl.fTargetNamespace) {
            reportSchemaError("cvc-elt.1.b", new Object[] {element.rawname, rootElementDecl.fName});
        }
    } // checkElementMatchesRootElementDecl
    
    private String expectedStr(Vector expected) {
        StringBuffer ret = new StringBuffer("{");
        int size = expected.size();
        for (int i = 0; i < size; i++) {
            if (i > 0)
                ret.append(", ");
            ret.append(expected.elementAt(i).toString());
        }
        ret.append('}');
        return ret.toString();
    }
    
} // class XMLSchemaValidator
