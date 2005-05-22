/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.jaxp.validation;

import java.io.IOException;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.EntityState;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLParseException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Entity;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * <p>A validator helper for <code>DOMSource</code>s.</p>
 * 
 * @author Michael Glavassevich, IBM
 * @version $Id$
 */
final class DOMValidatorHelper implements ValidatorHelper, EntityState {
    
    //
    // Constants
    //

    /** Chunk size (1024). */
    private static final int CHUNK_SIZE = (1 << 10);
    
    /** Chunk mask (CHUNK_SIZE - 1). */
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    
    // property identifiers
    
    /** Property identifier: error reporter. */
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    
    /** Property identifier: namespace context. */
    private static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    
    /** Property identifier: XML Schema validator. */
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    
    /** Property identifier: symbol table. */
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    
    /** Property identifier: validation manager. */
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    
    //
    // Data
    //
    
    /** Error reporter. */
    private XMLErrorReporter fErrorReporter;
    
    /** The namespace context of this document: stores namespaces in scope. **/
    private NamespaceContext fNamespaceContext;
    
    /** Schema validator. **/
    private org.apache.xerces.impl.xs.XMLSchemaValidator fSchemaValidator;
    
    /** Symbol table **/
    private SymbolTable fSymbolTable;
    
    /** Validation manager. **/
    private ValidationManager fValidationManager;
    
    /** Component manager. **/
    private XMLSchemaValidatorComponentManager fComponentManager;
    
    /** REVISIT: Dummy locator object for DOM. **/
    private final XMLLocator fXMLLocator = new SimpleLocator(null, null, -1, -1, -1);
    
    /** DOM document handler. **/
    private DOMDocumentHandler fDOMValidatorHandler;
    
    /** DOM result augmentor. **/
    private final DOMResultAugmentor fDOMResultAugmentor = new DOMResultAugmentor(this);
    
    /** DOM result builder. **/
    private final DOMResultBuilder fDOMResultBuilder = new DOMResultBuilder();
    
    /** Map for tracking unparsed entities. **/
    private NamedNodeMap fEntities = null;
    
    /** Array for holding character data. **/
    private char [] fCharBuffer = new char[CHUNK_SIZE];
    
    /** Current element. **/
    private Node fCurrentElement;
    
    /** Fields for start element, end element and characters. **/
    final QName fElementQName = new QName();
    final QName fAttributeQName = new QName();
    final XMLAttributesImpl fAttributes = new XMLAttributesImpl(); 
    final XMLString fTempString = new XMLString();
    
    public DOMValidatorHelper(XMLSchemaValidatorComponentManager componentManager) {
        fComponentManager = componentManager;
        fErrorReporter = (XMLErrorReporter) fComponentManager.getProperty(ERROR_REPORTER);
        fNamespaceContext = (NamespaceContext) fComponentManager.getProperty(NAMESPACE_CONTEXT);
        fSchemaValidator = (org.apache.xerces.impl.xs.XMLSchemaValidator) fComponentManager.getProperty(SCHEMA_VALIDATOR);
        fSymbolTable = (SymbolTable) fComponentManager.getProperty(SYMBOL_TABLE);        
        fValidationManager = (ValidationManager) fComponentManager.getProperty(VALIDATION_MANAGER);
    }
    
    /*
     * ValidatorHelper methods
     */
    
    public void validate(Source source, Result result) 
        throws SAXException, IOException {
        if (result instanceof DOMResult || result == null) {
            final DOMSource domSource = (DOMSource) source;
            final DOMResult domResult = (DOMResult) result;
            Node node = domSource.getNode();
            if (node != null) {
                fComponentManager.reset();
                fValidationManager.setEntityState(this);
                fErrorReporter.setDocumentLocator(fXMLLocator);
                try {
                    // regardless of what type of node this is, fire start and end document events
                    setupEntityMap((node.getNodeType() == Node.DOCUMENT_NODE) ? (Document) node : node.getOwnerDocument());
                    setupDOMResultHandler(domSource, domResult);
                    fSchemaValidator.startDocument(fXMLLocator, null, fNamespaceContext, null);
                    validate(node);
                    fSchemaValidator.endDocument(null);
                }
                catch (XMLParseException e) {
                    throw Util.toSAXParseException(e);
                }
                catch (XNIException e) {
                    throw Util.toSAXException(e);
                }
                finally {
                    // Release references to application objects
                    fCurrentElement = null;
                    fEntities = null;
                    if (fDOMValidatorHandler != null) {
                        fDOMValidatorHandler.setDOMResult(null);
                    }
                }
            }
            return;
        }
        throw new IllegalArgumentException(JAXPValidationMessageFormatter.formatMessage(Locale.getDefault(), 
                "SourceResultMismatch", 
                new Object [] {source.getClass().getName(), result.getClass().getName()}));
    }
    
    /*
     * EntityState methods
     */
    
    public boolean isEntityDeclared(String name) {
        return false;
    }
    
    public boolean isEntityUnparsed(String name) {
        if (fEntities != null) {
            Entity entity = (Entity) fEntities.getNamedItem(name);
            if (entity != null) {
                return (entity.getNotationName() != null);
            }
        }
        return false;
    }
    
    /*
     * Other methods
     */
    
    /** Traverse the DOM and fire events to the schema validator. */
    private void validate(Node node) {
        final Node top = node;
        // Performs a non-recursive traversal of the DOM. This
        // will avoid a stack overflow for DOMs with high depth.
        while (node != null) {
            beginNode(node);
            Node next = node.getFirstChild();
            while (next == null) {
                finishNode(node);           
                if (top == node) {
                    break;
                }
                next = node.getNextSibling();
                if (next == null) {
                    node = node.getParentNode();
                    if (node == null || top == node) {
                        if (node != null) {
                            finishNode(node);
                        }
                        next = null;
                        break;
                    }
                }
            }
            node = next;
        }
    }
    
    /** Do processing for the start of a node. */
    private void beginNode(Node node) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                fCurrentElement = node;
                // push namespace context
                fNamespaceContext.pushContext();
                // start element
                fillQName(fElementQName, node);
                processAttributes(node.getAttributes());
                fSchemaValidator.startElement(fElementQName, fAttributes, null);
                break;
            case Node.TEXT_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    sendCharactersToValidator(node.getNodeValue());
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.characters((Text) node);
                }
                else {
                    sendCharactersToValidator(node.getNodeValue());
                }
                break;
            case Node.CDATA_SECTION_NODE:
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.setIgnoringCharacters(true);
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null);
                    fDOMValidatorHandler.setIgnoringCharacters(false);
                    fDOMValidatorHandler.cdata((CDATASection) node);
                }
                else {
                    fSchemaValidator.startCDATA(null);
                    sendCharactersToValidator(node.getNodeValue());
                    fSchemaValidator.endCDATA(null); 
                }
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                /** 
                 * The validator does nothing with processing instructions so bypass it.
                 * Send the ProcessingInstruction node directly to the result builder.
                 */
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.processingInstruction((ProcessingInstruction) node);
                }
                break;
            case Node.COMMENT_NODE:
                /** 
                 * The validator does nothing with comments so bypass it.
                 * Send the Comment node directly to the result builder.
                 */
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.comment((Comment) node);
                }
                break;
            case Node.DOCUMENT_TYPE_NODE:
                /** 
                 * Send the DocumentType node directly to the result builder.
                 */
                if (fDOMValidatorHandler != null) {
                    fDOMValidatorHandler.doctypeDecl((DocumentType) node);
                }
                break;
            default: // Ignore other node types.
                break;
        }
    }
    
    /** Do processing for the end of a node. */
    private void finishNode(Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            fCurrentElement = node;
            // end element
            fillQName(fElementQName, node);
            fSchemaValidator.endElement(fElementQName, null);
            // pop namespace context
            fNamespaceContext.popContext();
        }
    }
    
    /**
     * Extracts NamedNodeMap of entities. We need this to validate
     * elements and attributes of type xs:ENTITY, xs:ENTITIES or 
     * types dervied from them.
     */
    private void setupEntityMap(Document doc) {
        if (doc != null) {
            DocumentType docType = doc.getDoctype();
            if (docType != null) {
                fEntities = docType.getEntities();
                return;
            }
        }
        fEntities = null;
    }
    
    /**
     * Sets up handler for <code>DOMResult</code>.
     */
    private void setupDOMResultHandler(DOMSource source, DOMResult result) throws SAXException {
        // If there's no DOMResult, unset the validator handler
        if (result == null) {
            fDOMValidatorHandler = null;
            fSchemaValidator.setDocumentHandler(null);
            return;
        }
        final Node nodeResult = result.getNode();
        // If the source node and result node are the same use the DOMResultAugmentor.
        // Otherwise use the DOMResultBuilder.
        if (source.getNode() == nodeResult) {
            fDOMValidatorHandler = fDOMResultAugmentor;
            fDOMResultAugmentor.setDOMResult(result);
            fSchemaValidator.setDocumentHandler(fDOMResultAugmentor);
            return;
        }
        if (result.getNode() == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
                result.setNode(builder.newDocument());
            }
            catch (ParserConfigurationException e) {
                throw new SAXException(e);
            }
        }
        fDOMValidatorHandler = fDOMResultBuilder;
        fDOMResultBuilder.setDOMResult(result);
        fSchemaValidator.setDocumentHandler(fDOMResultBuilder);
    }
    
    private void fillQName(QName toFill, Node node) {
        final String prefix = node.getPrefix();
        final String localName = node.getLocalName();
        final String rawName = node.getNodeName();
        final String namespace = node.getNamespaceURI();
        toFill.prefix = (prefix != null) ? fSymbolTable.addSymbol(prefix) : XMLSymbols.EMPTY_STRING;
        toFill.localpart = (localName != null) ? fSymbolTable.addSymbol(localName) : XMLSymbols.EMPTY_STRING;
        toFill.rawname = (rawName != null) ? fSymbolTable.addSymbol(rawName) : XMLSymbols.EMPTY_STRING; 
        toFill.uri = (namespace != null && namespace.length() > 0) ? fSymbolTable.addSymbol(namespace) : null;
    }
    
    private void processAttributes(NamedNodeMap attrMap) {
        final int attrCount = attrMap.getLength();
        fAttributes.removeAllAttributes();
        for (int i = 0; i < attrCount; ++i) {
            Attr attr = (Attr) attrMap.item(i);
            String value = attr.getValue();
            if (value == null) {
                value = XMLSymbols.EMPTY_STRING;
            }
            fillQName(fAttributeQName, attr);
            // REVISIT: Assuming all attributes are of type CDATA. The actual type may not matter. -- mrglavas
            fAttributes.addAttributeNS(fAttributeQName, XMLSymbols.fCDATASymbol, value);
            fAttributes.setSpecified(i, attr.getSpecified());
            // REVISIT: Should we be looking at non-namespace attributes
            // for additional mappings? Should we detect illegal namespace
            // declarations and exclude them from the context? -- mrglavas
            if (fAttributeQName.uri == NamespaceContext.XMLNS_URI) {
                // process namespace attribute
                if (fAttributeQName.prefix == XMLSymbols.PREFIX_XMLNS) {
                    fNamespaceContext.declarePrefix(fAttributeQName.localpart, fSymbolTable.addSymbol(value));
                }
                else {
                    fNamespaceContext.declarePrefix(XMLSymbols.EMPTY_STRING, fSymbolTable.addSymbol(value));
                }
            }
        }
    }
    
    private void sendCharactersToValidator(String str) {
        if (str != null) {
            final int length = str.length();
            final int remainder = length & CHUNK_MASK;
            if (remainder > 0) {
                str.getChars(0, remainder, fCharBuffer, 0);
                fTempString.setValues(fCharBuffer, 0, remainder);
                fSchemaValidator.characters(fTempString, null);
            }
            int i = remainder;
            while (i < length) {
                str.getChars(i, i += CHUNK_SIZE, fCharBuffer, 0);
                fTempString.setValues(fCharBuffer, 0, CHUNK_SIZE);
                fSchemaValidator.characters(fTempString, null);
            }
        }
    }
    
    Node getCurrentElement() {
        return fCurrentElement;
    }
    
} // DOMValidatorHelper
