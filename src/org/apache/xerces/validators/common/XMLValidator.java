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

package org.apache.xerces.validators.common;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.framework.XMLDocumentHandler;
import org.apache.xerces.framework.XMLDocumentScanner;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.readers.DefaultEntityHandler;
import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.ChunkyCharArray;
import org.apache.xerces.utils.Hash2intTable;
import org.apache.xerces.utils.NamespacesScope;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLCharacterProperties;
import org.apache.xerces.utils.XMLMessages;
import org.apache.xerces.utils.ImplementationMessages;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.xerces.validators.dtd.DTDImporter;
import org.apache.xerces.validators.schema.SchemaImporter;
import org.apache.xerces.validators.schema.SchemaMessageProvider;
import org.apache.xerces.validators.schema.DatatypeContentModel;
import org.apache.xerces.validators.datatype.InvalidDatatypeValueException;

/**
 * This class is the super all-in-one validator used by the parser.
 *
 * @version $Id$
 */
public final class XMLValidator
    implements DefaultEntityHandler.EventHandler,
               XMLEntityHandler.CharDataHandler,
               XMLDocumentScanner.EventHandler,
               NamespacesScope.NamespacesHandler {

    //
    // Constants
    //

    // debugging

    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    private static final boolean DEBUG_PRINT_ATTRIBUTES = false;
    private static final boolean DEBUG_PRINT_CONTENT = false;

    // Chunk size constants

    private static final int CHUNK_SHIFT = 8;           // 2^8 = 256
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));   // 2^10 = 1k

    //
    // Data
    //

    // REVISIT: The data should be regrouped and re-organized so that
    //          it's easier to find a meaningful field.

    // debugging

        private static XMLValidator schemaValidator = null;
    private static boolean DEBUG = false;

    // Element list
    
    private int fElementCount = 0;
    // REVISIT: Validation. Convert elementType to <uri,localpart> tuple!
    private int[][] fElementType = new int[INITIAL_CHUNK_COUNT][];
    // REVISIT: For now, Qname seems to be a overkill 
    private QName[][] fElementQName = new QName[INITIAL_CHUNK_COUNT][];
    private int[][] fScope = new int[INITIAL_CHUNK_COUNT][];

    private byte[][] fElementDeclIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpecType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpec = new int[INITIAL_CHUNK_COUNT][];
    private XMLContentModel[][] fContentModel = new XMLContentModel[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistHead = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistTail = new int[INITIAL_CHUNK_COUNT][];
    
    // ContentSpecNode list

    private int fNodeCount = 0;
    private byte[][] fNodeType = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNodeValue = new int[INITIAL_CHUNK_COUNT][];

    // AttDef list

    private int fAttDefCount = 0;
    // REVISIT: Validation. I don't think we need to store the prefix.
    //          The namespace URI is important.
    private int[][] fAttPrefix = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttName = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttType = new int[INITIAL_CHUNK_COUNT][];
    private AttributeValidator[][] fAttValidator = new AttributeValidator[INITIAL_CHUNK_COUNT][];
    private int[][] fEnumeration = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttDefaultType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttValue = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fAttDefIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNextAttDef = new int[INITIAL_CHUNK_COUNT][];

    // other

    private Hashtable fIdDefs = null;
    private Hashtable fIdRefs = null;
    private Object fNullValue = null;

    // attribute validators

    // REVISIT: Validation. A validator per element declaration and
    //          attribute declaration is required to accomodate
    //          Schema facets on simple types.
    private AttributeValidator fAttValidatorCDATA = null;
    private AttributeValidator fAttValidatorID = null;
    private AttributeValidator fAttValidatorIDREF = null;
    private AttributeValidator fAttValidatorIDREFS = null;
    private AttributeValidator fAttValidatorENTITY = null;
    private AttributeValidator fAttValidatorENTITIES = null;
    private AttributeValidator fAttValidatorNMTOKEN = null;
    private AttributeValidator fAttValidatorNMTOKENS = null;
    private AttributeValidator fAttValidatorNOTATION = null;
    private AttributeValidator fAttValidatorENUMERATION = null;
    private AttributeValidator fAttValidatorDATATYPE = null;

    // Package access for use by AttributeValidator classes.
    
    StringPool fStringPool = null;
    boolean fValidating = false;
    boolean fInElementContent = false;
    int fStandaloneReader = -1;

    // REVISIT: should this be pacakge access?
    GrammarPool fGrammarPool = null;

    // settings

    private boolean fValidationEnabled = false;
    private boolean fDynamicValidation = false;
    private boolean fValidationEnabledByDynamic = false;
    private boolean fDynamicDisabledByValidation = false;
    private boolean fWarningOnDuplicateAttDef = false;
    private boolean fWarningOnUndeclaredElements = false;

    // declarations

    private ContentSpecImpl fContentSpecImpl = null;
    private int fDeclaration[];
    private XMLErrorReporter fErrorReporter = null;
    private DefaultEntityHandler fEntityHandler = null;
    private QName fCurrentElement = new QName();

    //REVISIT: validation
    private int[] fScopeStack = new int[8];
    private int[] fSchemaURIStack = new int[8];

    private int[] fElementTypeStack = new int[8];
    private int[] fElementEntityStack = new int[8];
    private int[] fElementIndexStack = new int[8];
    private int[] fContentSpecTypeStack = new int[8];
    private int[] fElementChildCount = new int[8];
    private int[][] fElementChildren = new int[8][];
    private int fElementDepth = -1;
    private boolean fNamespacesEnabled = false;
    private NamespacesScope fNamespacesScope = null;
    private int fNamespacesPrefix = -1;
    private QName fRootElement = new QName();
    private int fAttrListHandle = -1;
    private int fElementDeclCount = 0;
    private int fCurrentElementEntity = -1;
    private int fCurrentElementIndex = -1;
    private int fCurrentContentSpecType = -1;
    private boolean fSeenDoctypeDecl = false;

    private final int TOP_LEVEL_SCOPE = 0;
    private int fCurrentScope = TOP_LEVEL_SCOPE;
    private int fCurrentSchemaURI = -1;
    private Hash2intTable fNameScopeToIndex = new Hash2intTable();

    // state and stuff

    private boolean fScanningDTD = false;
    private DTDImporter fDTDImporter = null;
    private SchemaImporter fSchemaImporter = null;
    private XMLDocumentScanner fDocumentScanner = null;
    private boolean fCalledStartDocument = false;
    private XMLDocumentHandler fDocumentHandler = null;
    private XMLDocumentHandler.DTDHandler fDTDHandler = null;
    private boolean fSeenRootElement = false;
    private XMLAttrList fAttrList = null;
    private int fXMLLang = -1;
    private LocatorImpl fAttrNameLocator = null;
    private boolean fCheckedForSchema = false;
    private Document fSchemaDocument = null;
    private boolean fDeclsAreExternal = false;
    private StringPool.CharArrayRange fCurrentElementCharArrayRange = null;
    private char[] fCharRefData = null;
    private boolean fSendCharDataAsCharArray = false;
    private boolean fBufferDatatype = false;
    private StringBuffer fDatatypeBuffer = new StringBuffer();

    private QName fTempQName = new QName();
    //REVISIT: eriye, use this temp QName whenever we can!!

    // symbols

    private int fEMPTYSymbol = -1;
    private int fANYSymbol = -1;
    private int fMIXEDSymbol = -1;
    private int fCHILDRENSymbol = -1;
    private int fCDATASymbol = -1;
    private int fIDSymbol = -1;
    private int fIDREFSymbol = -1;
    private int fIDREFSSymbol = -1;
    private int fENTITYSymbol = -1;
    private int fENTITIESSymbol = -1;
    private int fNMTOKENSymbol = -1;
    private int fNMTOKENSSymbol = -1;
    private int fNOTATIONSymbol = -1;
    private int fENUMERATIONSymbol = -1;
    private int fREQUIREDSymbol = -1;
    private int fFIXEDSymbol = -1;
    private int fDATATYPESymbol = -1;
    private int fEpsilonIndex = -1;

    // building content models

    private int fLeafCount = 0;
    private int fCount = 0;
    private int[] fContentList = new int[64];

    //
    // Constructors
    //

    /** Constructs an XML validator. */
    public XMLValidator(StringPool stringPool,
                        XMLErrorReporter errorReporter,
                        DefaultEntityHandler entityHandler,
                        XMLDocumentScanner documentScanner) {

        // keep references
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fDocumentScanner = documentScanner;

        //REVISIT: get the only instance of GrammarPool
        fGrammarPool = GrammarPool.instanceGrammarPool();

        // initialize
        fAttrList = new XMLAttrList(fStringPool);
        entityHandler.setEventHandler(this);
        entityHandler.setCharDataHandler(this);
        fDocumentScanner.setEventHandler(this);
        init();

        if (DEBUG) {
            // REVISIT: Synchronization.
            if (schemaValidator == null) {
                // only set for the first one!!
                schemaValidator = this;
            }
        }

    } // <init>(StringPool,XMLErrorReporter,DefaultEntityHandler,XMLDocumentScanner)

    //
    // Public methods
    //

    // initialization

    /** Set char data processing preference and handlers. */
    public void initHandlers(boolean sendCharDataAsCharArray,
                             XMLDocumentHandler docHandler,
                             XMLDocumentHandler.DTDHandler dtdHandler) {

                //****DEBUG****
                if (DEBUG) print("(GEN) XMLValidator.initHandlers\n");
                //****DEBUG****

        fSendCharDataAsCharArray = sendCharDataAsCharArray;
        fEntityHandler.setSendCharDataAsCharArray(fSendCharDataAsCharArray);
        fDocumentHandler = docHandler;
        fDTDHandler = dtdHandler;

    } // initHandlers(boolean,XMLDocumentHandler,XMLDocumentHandler.DTDHandler)

    /** Reset or copy. */
    public void resetOrCopy(StringPool stringPool) throws Exception {
        fAttrList = new XMLAttrList(stringPool);
        resetCommon(stringPool);
    }

    /** Reset. */
    public void reset(StringPool stringPool) throws Exception {
        fAttrList.reset(stringPool);
        resetCommon(stringPool);
    }

    // schema information

    /** Returns the schema document. */
    public Document getSchemaDocument() {
        // REVISIT: Is this method needed? Should it be removed?
        return fSchemaDocument;
    }
    
    // settings

    /**
     * Turning on validation/dynamic turns on validation if it is off, and 
     * this is remembered.  Turning off validation DISABLES validation/dynamic
     * if it is on.  Turning off validation/dynamic DOES NOT turn off
     * validation if it was explicitly turned on, only if it was turned on
     * BECAUSE OF the call to turn validation/dynamic on.  Turning on
     * validation will REENABLE and turn validation/dynamic back on if it
     * was disabled by a call that turned off validation while 
     * validation/dynamic was enabled.
     */
    public void setValidationEnabled(boolean flag) throws Exception {
        fValidationEnabled = flag;
        fValidationEnabledByDynamic = false;
        if (fValidationEnabled) {
            if (fDynamicDisabledByValidation) {
                fDynamicValidation = true;
                fDynamicDisabledByValidation = false;
            }
        } else if (fDynamicValidation) {
            fDynamicValidation = false;
            fDynamicDisabledByValidation = true;
        }
        fValidating = fValidationEnabled;
    }

    /** Returns true if validation is enabled. */
    public boolean getValidationEnabled() {
        return fValidationEnabled;
    }

    /** Sets whether validation is dynamic. */
    public void setDynamicValidationEnabled(boolean flag) throws Exception {
        fDynamicValidation = flag;
        fDynamicDisabledByValidation = false;
        if (!fDynamicValidation) {
            if (fValidationEnabledByDynamic) {
                fValidationEnabled = false;
                fValidationEnabledByDynamic = false;
            }
        } else if (!fValidationEnabled) {
            fValidationEnabled = true;
            fValidationEnabledByDynamic = true;
        }
        fValidating = fValidationEnabled;
    }

    /** Returns true if validation is dynamic. */
    public boolean getDynamicValidationEnabled() {
        return fDynamicValidation;
    }

    /** Sets whether namespaces are enabled. */
    public void setNamespacesEnabled(boolean flag) {
        fNamespacesEnabled = flag;
    }

    /** Returns true if namespaces are enabled. */
    public boolean getNamespacesEnabled() {
        return fNamespacesEnabled;
    }

    /** Sets whether duplicate attribute definitions signal a warning. */
    public void setWarningOnDuplicateAttDef(boolean flag) {
        fWarningOnDuplicateAttDef = flag;
    }

    /** Returns true if duplicate attribute definitions signal a warning. */
    public boolean getWarningOnDuplicateAttDef() {
        return fWarningOnDuplicateAttDef;
    }

    /** Sets whether undeclared elements signal a warning. */
    public void setWarningOnUndeclaredElements(boolean flag) {
        fWarningOnUndeclaredElements = flag;
    }

    /** Returns true if undeclared elements signal a warning. */
    public boolean getWarningOnUndeclaredElements() {
        return fWarningOnUndeclaredElements;
    }

    //
    // DefaultEntityHandler.EventHandler methods
    //

    /** Start entity reference. */
    public void startEntityReference(int entityName, int entityType, int entityContext) throws Exception {
        fDocumentHandler.startEntityReference(entityName, entityType, entityContext);
    }

    /** End entity reference. */
    public void endEntityReference(int entityName, int entityType, int entityContext) throws Exception {
        fDocumentHandler.endEntityReference(entityName, entityType, entityContext);
    }

    /** Send end of input notification. */
    public void sendEndOfInputNotifications(int entityName, boolean moreToFollow) throws Exception {
        fDocumentScanner.endOfInput(entityName, moreToFollow);
        if (fScanningDTD) {
            fDTDImporter.sendEndOfInputNotifications(entityName, moreToFollow);
        }
    }

    /** Send reader change notifications. */
    public void sendReaderChangeNotifications(XMLEntityHandler.EntityReader reader, int readerId) throws Exception {
        fDocumentScanner.readerChange(reader, readerId);
        if (fScanningDTD) {
            fDTDImporter.sendReaderChangeNotifications(reader, readerId);
        }
    }

    /** External entity standalone check. */
    public boolean externalEntityStandaloneCheck() {
        return (fStandaloneReader != -1 && fValidating);
    }

    /** Return true if validating. */
    public boolean getValidating() {
        return fValidating;
    }

    //
    // XMLEntityHandler.CharDataHandler methods
    //

    /** Process characters. */
    public void processCharacters(char[] chars, int offset, int length) throws Exception {
        if (fValidating) {
            if (fInElementContent || fCurrentContentSpecType == fEMPTYSymbol) {
                charDataInContent();
            }
            if (fBufferDatatype) {
                fDatatypeBuffer.append(chars, offset, length);
            }
        }
        fDocumentHandler.characters(chars, offset, length);
    }

    /** Process characters. */
    public void processCharacters(int data) throws Exception {
        if (fValidating) {
            if (fInElementContent || fCurrentContentSpecType == fEMPTYSymbol) {
                charDataInContent();
            }
            if (fBufferDatatype) {
                fDatatypeBuffer.append(fStringPool.toString(data));
            }
        }
        fDocumentHandler.characters(data);
    }

    /** Process whitespace. */
    public void processWhitespace(char[] chars, int offset, int length) 
        throws Exception {

        if (fInElementContent) {
            if (fStandaloneReader != -1 && fValidating && getElementDeclIsExternal(fCurrentElementIndex)) {
                reportRecoverableXMLError(XMLMessages.MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION);
            }
            fDocumentHandler.ignorableWhitespace(chars, offset, length);
        } 
        else {
            if (fCurrentContentSpecType == fEMPTYSymbol) {
                charDataInContent();
            }
            fDocumentHandler.characters(chars, offset, length);
        }

    } // processWhitespace(char[],int,int)

    /** Process whitespace. */
    public void processWhitespace(int data) throws Exception {

        if (fInElementContent) {
            if (fStandaloneReader != -1 && fValidating && getElementDeclIsExternal(fCurrentElementIndex)) {
                reportRecoverableXMLError(XMLMessages.MSG_WHITE_SPACE_IN_ELEMENT_CONTENT_WHEN_STANDALONE,
                                          XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION);
            }
            fDocumentHandler.ignorableWhitespace(data);
        } else {
            if (fCurrentContentSpecType == fEMPTYSymbol) {
                charDataInContent();
            }
            fDocumentHandler.characters(data);
        }

    } // processWhitespace(int)

    //
    // XMLDocumentScanner.EventHandler methods
    //

    /** Scans element type. */
    public void scanElementType(XMLEntityHandler.EntityReader entityReader, 
                                char fastchar, QName element) throws Exception {

        if (!fNamespacesEnabled) {
            element.clear();
            element.localpart = entityReader.scanName(fastchar);
            element.rawname = element.localpart;
        } 
        else {
            entityReader.scanQName(fastchar, element);
                        if (entityReader.lookingAtChar(':', false)) {
                                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                                                   XMLMessages.XML_DOMAIN,
                                                                                   XMLMessages.MSG_TWO_COLONS_IN_QNAME,
                                                                                   XMLMessages.P5_INVALID_CHARACTER,
                                                                                   null,
                                                                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
                                entityReader.skipPastNmtoken(' ');
                        }
                }

                //****DEBUG****
        if (DEBUG) {
            String nsFlag = "";
            if ( fNamespacesEnabled ) {
                nsFlag = "NameSpacesEnabled";
            }
            print("(SCN) XMLValidator.scanElementType: " + param("elementType",element.rawname) + nsFlag + "\n");
        }
                //****DEBUG****

    } // scanElementType(XMLEntityHandler.EntityReader,char,QName)

    /** Scans expected element type. */
    public boolean scanExpectedElementType(XMLEntityHandler.EntityReader entityReader, 
                                           char fastchar, QName element) 
        throws Exception {

                //****DEBUG****
                if (DEBUG) print("(SCN) XMLValidator.scanExpectedElementType ... \n");
                //****DEBUG****

        if (fCurrentElementCharArrayRange == null) {
            fCurrentElementCharArrayRange = fStringPool.createCharArrayRange();
        }
        fStringPool.getCharArrayRange(fCurrentElement.rawname, fCurrentElementCharArrayRange);
        return entityReader.scanExpectedName(fastchar, fCurrentElementCharArrayRange);

    } // scanExpectedElementType(XMLEntityHandler.EntityReader,char,QName)

    /** Scans attribute name. */
    public void scanAttributeName(XMLEntityHandler.EntityReader entityReader, 
                                  QName element, QName attribute) 
        throws Exception {

        if (!fSeenRootElement) {
            fSeenRootElement = true;
            rootElementSpecified(element);
            fStringPool.resetShuffleCount();
        }

        if (!fNamespacesEnabled) {
            attribute.clear();
            attribute.localpart = entityReader.scanName('=');
            attribute.rawname = attribute.localpart;
        } 
        else {
            entityReader.scanQName('=', attribute);
                        if (entityReader.lookingAtChar(':', false)) {
                                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                                                   XMLMessages.XML_DOMAIN,
                                                                                   XMLMessages.MSG_TWO_COLONS_IN_QNAME,
                                                                                   XMLMessages.P5_INVALID_CHARACTER,
                                                                                   null,
                                                                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
                                entityReader.skipPastNmtoken(' ');
                        }
                }

                //****DEBUG****
                if (DEBUG) print("(SCN) XMLValidator.scanAttributeName: " + param("elementType",element.rawname) + param("attrName",attribute.rawname) + "\n");
                //****DEBUG****

    } // scanAttributeName(XMLEntityHandler.EntityReader,QName,QName)

    /** Call start document. */
    public void callStartDocument() throws Exception {

                //****DEBUG****
                if (DEBUG) print("\n(VAL) XMLValidator.callStartDocument\n");
                //****DEBUG****

        if (!fCalledStartDocument) {
            fDocumentHandler.startDocument();
            fCalledStartDocument = true;
        }
    }

    /** Call end document. */
    public void callEndDocument() throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callEndDocument\n\n");
                //****DEBUG****

        if (fCalledStartDocument) {
            fDocumentHandler.endDocument();
        }
    }

    /** Call XML declaration. */
    public void callXMLDecl(int version, int encoding, int standalone) throws Exception {
        fDocumentHandler.xmlDecl(version, encoding, standalone);
    }

    /** Call text declaration. */
    public void callTextDecl(int version, int encoding) throws Exception {
        fDocumentHandler.textDecl(version, encoding);
    }

    /** Call start element. */
    public void callStartElement(QName element) throws Exception {

        //
        // Check after all specified attrs are scanned
        // (1) report error for REQUIRED attrs that are missing (V_TAGc)
        // (2) add default attrs (FIXED and NOT_FIXED)
        //

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callStartElement: " + param("elementType",element.rawname) + "\n");
                //****DEBUG****

        if (!fSeenRootElement) {
            fSeenRootElement = true;
            rootElementSpecified(element);
            fStringPool.resetShuffleCount();
        }
        fCheckedForSchema = true;
        if (fNamespacesEnabled) {
            bindNamespacesToElementAndAttributes(element, fAttrList);
        }
        validateElementAndAttributes(element, fAttrList);
        fDocumentHandler.startElement(element, fAttrList, fAttrListHandle);
        fAttrListHandle = -1;
        if (fElementDepth >= 0) {
            int[] children = fElementChildren[fElementDepth];
            int childCount = fElementChildCount[fElementDepth];
            try {
                // REVISIT: Validation
                children[childCount] = element.rawname;
            } 
            catch (NullPointerException ex) {
                children = fElementChildren[fElementDepth] = new int[256];
                childCount = 0; // should really assert this...
                // REVISIT: Validation
                children[childCount] = element.rawname;
            } 
            catch (ArrayIndexOutOfBoundsException ex) {
                int[] newChildren = new int[childCount * 2];
                System.arraycopy(children, 0, newChildren, 0, childCount);
                children = fElementChildren[fElementDepth] = newChildren;
                // REVISIT: Validation
                children[childCount] = element.rawname;
            }
            fElementChildCount[fElementDepth] = ++childCount;
        }
        fElementDepth++;
        if (fElementDepth == fElementTypeStack.length) {
            int[] newStack = new int[fElementDepth * 2];
            
            // REVISIT: validation
            System.arraycopy(fScopeStack, 0, newStack, 0, fElementDepth);
            fScopeStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fSchemaURIStack, 0, newStack, 0, fElementDepth);
            fSchemaURIStack = newStack;
            
            newStack = new int[fElementDepth * 2];


            System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
            fElementTypeStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementEntityStack, 0, newStack, 0, fElementDepth);
            fElementEntityStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementIndexStack, 0, newStack, 0, fElementDepth);
            fElementIndexStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fContentSpecTypeStack, 0, newStack, 0, fElementDepth);
            fContentSpecTypeStack = newStack;
            newStack = new int[fElementDepth * 2];
            System.arraycopy(fElementChildCount, 0, newStack, 0, fElementDepth);
            fElementChildCount = newStack;
            int[][] newContentStack = new int[fElementDepth * 2][];
            System.arraycopy(fElementChildren, 0, newContentStack, 0, fElementDepth);
            fElementChildren = newContentStack;
        }
        fCurrentElement.setValues(element);
        fCurrentElementEntity = fEntityHandler.getReaderId();
        fElementTypeStack[fElementDepth] = fCurrentElement.rawname;
        fElementEntityStack[fElementDepth] = fCurrentElementEntity;
        fElementIndexStack[fElementDepth] = fCurrentElementIndex;
        fContentSpecTypeStack[fElementDepth] = fCurrentContentSpecType;
        fElementChildCount[fElementDepth] = 0;

        //REVISIT: Validation
        if ( fCurrentElementIndex > -1 ) {
            int chunk = fCurrentElementIndex >> CHUNK_SHIFT;
            int index = fCurrentElementIndex & CHUNK_MASK;
            fCurrentScope = fScope[chunk][index];
        }
        fScopeStack[fElementDepth] = fCurrentScope;
        fSchemaURIStack[fElementDepth] = fCurrentSchemaURI;

    } // callStartElement(QName)

    /** Call end element. */
    public void callEndElement(int readerId) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callEndElement: " + param("readerId",readerId) + "\n");
                //****DEBUG****

        int prefixIndex = fCurrentElement.prefix;
        // REVISIT: Validation
        int elementType = fCurrentElement.rawname;
        if (fCurrentElementEntity != readerId) {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       XMLMessages.XML_DOMAIN,
                                       XMLMessages.MSG_ELEMENT_ENTITY_MISMATCH,
                                       XMLMessages.P78_NOT_WELLFORMED,
                                       new Object[] { fStringPool.toString(elementType) },
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }
        fDocumentHandler.endElement(fCurrentElement);
        if (fValidating) {
            int elementIndex = fCurrentElementIndex;
            if (elementIndex != -1 && fCurrentContentSpecType != -1) {
                int childCount = peekChildCount();
                int result = checkContent(elementIndex, childCount, peekChildren());
                if (result != -1) {
                    int majorCode = result != childCount ? XMLMessages.MSG_CONTENT_INVALID : XMLMessages.MSG_CONTENT_INCOMPLETE;
                    reportRecoverableXMLError(majorCode,
                                              0,
                                              fStringPool.toString(elementType),
                                              getContentSpecAsString(elementIndex));
                }
            }
        }
        if (fNamespacesEnabled) {
            fNamespacesScope.decreaseDepth();
        }

        // now pop this element off the top of the element stack
        if (fElementDepth-- < 0) {
            throw new RuntimeException("FWK008 Element stack underflow");
        }
        if (fElementDepth < 0) {
            fCurrentElement.clear();
            fCurrentElementEntity = -1;
            fCurrentElementIndex = -1;
            fCurrentContentSpecType = -1;
            fInElementContent = false;
            //
            // Check after document is fully parsed
            // (1) check that there was an element with a matching id for every
            //   IDREF and IDREFS attr (V_IDREF0)
            //
            if (fValidating && fIdRefs != null) {
                checkIdRefs();
            }
            return;
        }

        //restore enclosing element to all the "current" variables
        // REVISIT: Validation. This information needs to be stored.
        fCurrentElement.prefix = -1;
        fCurrentElement.localpart = fElementTypeStack[fElementDepth];
        fCurrentElement.rawname = fElementTypeStack[fElementDepth];
        fCurrentElementEntity = fElementEntityStack[fElementDepth];
        fCurrentElementIndex = fElementIndexStack[fElementDepth];
        fCurrentContentSpecType = fContentSpecTypeStack[fElementDepth];

        //REVISIT: Validation
        fCurrentScope = fScopeStack[fElementDepth];
        // if enclosing element's Schema is different, need to switch "context"
        if ( fCurrentSchemaURI != fSchemaURIStack[fElementDepth] ) {
            fCurrentSchemaURI = fSchemaURIStack[fElementDepth];
            switchSchema(fCurrentSchemaURI);
        }

        if (fValidating) {
            fBufferDatatype = false;
        }
        fInElementContent = (fCurrentContentSpecType == fCHILDRENSymbol);

    } // callEndElement(int)

    /** Returns true if the version number is valid. */
    public boolean validVersionNum(String version) {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.validVersionNum: version=" + version + "\n");
                //****DEBUG****

        return XMLCharacterProperties.validVersionNum(version);
    }

    /** Returns true if the encoding name is valid. */
    public boolean validEncName(String encoding) {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.validEncName: encoding=" + encoding + "\n");
                //****DEBUG****

        return XMLCharacterProperties.validEncName(encoding);
    }

    /** Call start CDATA section. */
    public void callStartCDATA() throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callStartCDATA\n");
                //****DEBUG****

        fDocumentHandler.startCDATA();
    }

    /** Call end CDATA section. */
    public void callEndCDATA() throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callEndCDATA\n");
                //****DEBUG****

        fDocumentHandler.endCDATA();
    }

    /** Call characters. */
    public void callCharacters(int ch) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.callCharacters: ... \n");
                //****DEBUG****

        if (fCharRefData == null) {
            fCharRefData = new char[2];
        }
        int count = (ch < 0x10000) ? 1 : 2;
        if (count == 1) {
            fCharRefData[0] = (char)ch;
        }
        else {
            fCharRefData[0] = (char)(((ch-0x00010000)>>10)+0xd800);
            fCharRefData[1] = (char)(((ch-0x00010000)&0x3ff)+0xdc00);
        }
        if (fValidating && (fInElementContent || fCurrentContentSpecType == fEMPTYSymbol)) {
            charDataInContent();
        }
        if (fSendCharDataAsCharArray) {
            fDocumentHandler.characters(fCharRefData, 0, count);
        } 
        else {
            int index = fStringPool.addString(new String(fCharRefData, 0, count));
            fDocumentHandler.characters(index);
        }

    } // callCharacters(int)

    /** Call processing instruction. */
    public void callProcessingInstruction(int target, int data) throws Exception {
        fDocumentHandler.processingInstruction(target, data);
    }

    /** Call comment. */
    public void callComment(int comment) throws Exception {
        fDocumentHandler.comment(comment);
    }

    /** Scan doctype declaration. */
    public void scanDoctypeDecl(boolean standalone) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(SCN) XMLValidator.scanDoctypeDecl\n");
                //****DEBUG****

                //****DEBUG****
                if (DEBUG) print("\n\n**** BEGIN DTD ****\n\n");
                //****DEBUG****

        fScanningDTD = true;
        fCheckedForSchema = true;
        fSeenDoctypeDecl = true;
        fStandaloneReader = standalone ? fEntityHandler.getReaderId() : -1;
        fDeclsAreExternal = false;
        if (fDTDImporter == null) {
            fDTDImporter = new DTDImporter(fStringPool, fErrorReporter, fEntityHandler, this);
        } 
        else {
            fDTDImporter.reset(fStringPool);
        }
        fDTDImporter.initHandlers(fDTDHandler);
        fDTDImporter.setValidating(fValidating);
        fDTDImporter.setNamespacesEnabled(fNamespacesEnabled);
        if (fDTDImporter.scanDoctypeDecl(standalone) && fValidating) {
            // check declared elements
            if (fWarningOnUndeclaredElements) {
                checkDeclaredElements();
            }

            // check required notations
            fEntityHandler.checkRequiredNotations();
        }
        fScanningDTD = false;

                //****DEBUG****
                if (DEBUG) print("\n\n**** END DTD ****\n\n");
                //****DEBUG****

    } // scanDoctypeDecl(boolean)

    /** Scan attribute value. */
    public int scanAttValue(QName element, QName attribute) throws Exception {

        fAttrNameLocator = getLocatorImpl(fAttrNameLocator);
        int attValue = fDocumentScanner.scanAttValue(element, attribute, fValidating/* && attType != fCDATASymbol*/);
        if (attValue == -1) {
            return XMLDocumentScanner.RESULT_FAILURE;
        }

                //****DEBUG****
                if (DEBUG) print("(SCN) XMLValidator.scanAttValue: " + param ("elementType",element.rawname) +  param ("attrName",attribute.rawname) + 
                                                        param ("attValue",attValue) + "\n" );
                //****DEBUG****

                //
                // Check for Schema and load
                //

        /*
        if (!fCheckedForSchema) {
            fCheckedForSchema = true;
            if (attrName == fStringPool.addSymbol("xmlns")) { // default namespacedecl
                                
                if (fSchemaImporter == null) {
                    fSchemaImporter = new SchemaImporter(fStringPool, fErrorReporter, fEntityHandler, this);
                } else {
                    fSchemaImporter.reset(fStringPool);
                }
                String fs = fEntityHandler.expandSystemId(fStringPool.toString(attValue));
                EntityResolver resolver = fEntityHandler.getEntityResolver();
                InputSource is = resolver == null ? null : resolver.resolveEntity(null, fs);
                if (is == null) {
                    is = new InputSource(fs);
                }

                                //****DEBUG****
                                if (DEBUG) print( "\n\n**** BEGIN SCHEMA ****\n\n" );
                                //****DEBUG****

                fSchemaImporter.loadSchema(is);

                                //****DEBUG****
                                if (DEBUG) print( "\n\n**** END SCHEMA ****\n\n" );
                                //****DEBUG****

                fSchemaDocument = fSchemaImporter.getSchemaDocument();
            }
        }
        */

        if (!fValidating && fAttDefCount == 0) {
            int attType = fCDATASymbol;
            if (fAttrListHandle == -1)
                fAttrListHandle = fAttrList.startAttrList();
            // REVISIT: Should this be localpart or rawname?
            if (fAttrList.addAttr(attribute, attValue, attType, true, true) == -1) {
                return XMLDocumentScanner.RESULT_DUPLICATE_ATTR;
            }
            return XMLDocumentScanner.RESULT_SUCCESS;
        }

        // REVISIT: Validation. What should these be?
        int attDefIndex = getAttDef(element, attribute);
        if (attDefIndex == -1) {
            if (fValidating) {
                // REVISIT - cache the elem/attr tuple so that we only give
                //  this error once for each unique occurrence
                Object[] args = { fStringPool.toString(element.rawname),
                                  fStringPool.toString(attribute.rawname) };
                fErrorReporter.reportError(fAttrNameLocator,
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ATTRIBUTE_NOT_DECLARED,
                                           XMLMessages.VC_ATTRIBUTE_VALUE_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
            int attType = fCDATASymbol;
            if (fAttrListHandle == -1) {
                fAttrListHandle = fAttrList.startAttrList();
            }
            // REVISIT: Validation. What should the name be?
            if (fAttrList.addAttr(attribute, attValue, attType, true, true) == -1) {
                return XMLDocumentScanner.RESULT_DUPLICATE_ATTR;
            }
            return XMLDocumentScanner.RESULT_SUCCESS;
        }

        int attType = getAttType(attDefIndex);
        if (attType != fCDATASymbol) {
            AttributeValidator av = getAttributeValidator(attDefIndex);
            int enumHandle = getEnumeration(attDefIndex);
            // REVISIT: Validation. What should these be?
            attValue = av.normalize(element, attribute, 
                                    attValue, attType, enumHandle);
        }

        if (fAttrListHandle == -1) {
            fAttrListHandle = fAttrList.startAttrList();
        }
        // REVISIT: Validation. What should the name be?
        if (fAttrList.addAttr(attribute, attValue, attType, true, true) == -1) {
            return XMLDocumentScanner.RESULT_DUPLICATE_ATTR;
        }

        return XMLDocumentScanner.RESULT_SUCCESS;

    } // scanAttValue(QName,QName):int

    //
    // NamespacesScope.NamespacesHandler methods
    //

    /** Start a new namespace declaration scope. */
    public void startNamespaceDeclScope(int prefix, int uri) throws Exception {
                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.startNamespaceDeclScope: " + param("prefix",prefix) + param("uri",uri) + "\n");
                //****DEBUG****
        fDocumentHandler.startNamespaceDeclScope(prefix, uri);
    }

    /** End a namespace declaration scope. */
    public void endNamespaceDeclScope(int prefix) throws Exception {
                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.endNamespaceDeclScope: " + param("prefix",prefix) + "\n");
                //****DEBUG****
        fDocumentHandler.endNamespaceDeclScope(prefix);
    }

    // attributes

    /** Normalize attribute value. */
    public int normalizeAttValue(QName element, QName attribute, 
                                 int attValue, int attType, 
                                 int enumHandle) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.normalizeAttValue: " + param("elementType",element.rawname) + param("attValue",attValue) +
                                                        param("attType",attType) + param("enumHandle",enumHandle) + "\n");
                //****DEBUG****
        AttributeValidator av = getValidatorForAttType(attType);
        return av.normalize(element, attribute, attValue, attType, enumHandle);

    } // normalizeAttValue(QName,QName,int,int,int):int

    // other

    /** Sets the root element. */
    public void setRootElementType(QName rootElement) {
        fRootElement.setValues(rootElement);
    }

    /** Adds an element. */
    public int addElement(QName element) {

        // REVISIT: What is the difference between addElement and 
        //          addElementDecl?

        /***
        System.out.println("XMLValidator#addElement(QName)");
        System.out.println("  elementDecl: "+element);
        System.out.println("    prefix: "+fStringPool.toString(element.prefix));
        System.out.println("    localpart: "+fStringPool.toString(element.localpart));
        System.out.println("    rawname: "+fStringPool.toString(element.rawname));
        System.out.println("    uri: "+fStringPool.toString(element.uri));
        //try { throw new Exception("!!! STACK TRACE !!!"); }
        //catch (Exception e) { e.printStackTrace(); }
        /***/

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addElement: " + param("elementType",element.rawname) + "\n");
                //****DEBUG****

        int elementIndex = getDeclaration(element);
        if (elementIndex != -1) {
            return elementIndex;
        }
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        fElementType[chunk][index] = element.rawname;
        fElementDeclIsExternal[chunk][index] = 0;
        fContentSpecType[chunk][index] = -1;
        fContentSpec[chunk][index] = -1;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        setDeclaration(element, fElementCount);
        return fElementCount++;

    } // addElement(QName):int
    
    // a convenience method for TraverseSchema 
    public int addElementDecl(QName elementDecl, int scopeDefined,
                              int contentSpecType, int contentSpec, 
                              boolean isExternal) {
        int elementIndex = 
            addElementDecl(elementDecl,contentSpecType,contentSpec,isExternal);

        if (elementIndex > -1) {
 
            int chunk = elementIndex >> CHUNK_SHIFT;
            int index = elementIndex & CHUNK_MASK;
            fScope[chunk][index] = scopeDefined;
        }
        
        return elementIndex;

    }

    /** Adds an element declaration. */
    public int addElementDecl(QName elementDecl, 
                              int contentSpecType, int contentSpec, 
                              boolean isExternal) {
        /***
        System.out.println("XMLValidator#addElementDecl(QName,int,int,boolean)");
        System.out.println("  elementDecl: "+elementDecl);
        System.out.println("    prefix: "+fStringPool.toString(elementDecl.prefix));
        System.out.println("    localpart: "+fStringPool.toString(elementDecl.localpart));
        System.out.println("    rawname: "+fStringPool.toString(elementDecl.rawname));
        System.out.println("    uri: "+fStringPool.toString(elementDecl.uri));
        //try { throw new Exception("!!! STACK TRACE !!!"); }
        //catch (Exception e) { e.printStackTrace(); }
        /***/

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addElementDecl: " + param("elementType",elementDecl.rawname) + "\n");
                //****DEBUG****

                int elementIndex = getDeclaration(elementDecl);
        if (elementIndex != -1) {
            int chunk = elementIndex >> CHUNK_SHIFT;
            int index = elementIndex & CHUNK_MASK;
            if (fContentSpecType[chunk][index] != -1) {
                return -1;
            }
            fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
            fContentSpecType[chunk][index] = contentSpecType;
            fContentSpec[chunk][index] = contentSpec;
            fContentModel[chunk][index] = null;
            fElementDeclCount++;
            return elementIndex;
        }
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        // REVISIT: Validation. Store as tuple.
        fElementType[chunk][index] = elementDecl.rawname;
        fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fContentSpecType[chunk][index] = contentSpecType;
        fContentSpec[chunk][index] = contentSpec;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        setDeclaration(elementDecl, fElementCount);
        fElementDeclCount++;
        return fElementCount++;

    } // addElementDecl(QName,int,int,boolean):int

    /** Gets an element type. */
    public void getElementType(int elementIndex, QName element) {

        if (elementIndex < 0 || elementIndex >= fElementCount) {
            element.clear();
            return;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        // REVISIT: Validation. Copy in <uri, localpart> tuple.
        element.clear();
        element.localpart = fElementType[chunk][index];
        element.rawname = element.localpart;

    } // getElementType(int,QName)

    /** 
     * Returns true if the element declaration is external. 
     * <p>
     * <strong>Note:</strong> This method is primarilly useful for
     * DTDs with internal and external subsets.
     */
    private boolean getElementDeclIsExternal(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return false;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return (fElementDeclIsExternal[chunk][index] != 0);
    }

    /** Returns the content spec type for an element index. */
    public int getContentSpecType(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return -1;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpecType[chunk][index];
    }

    /** Returns the XMLContentSpec for an element index. */
    public XMLContentSpec getContentSpec(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return null;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        if (fContentSpecImpl == null) {
            fContentSpecImpl = new ContentSpecImpl();
        }
        fContentSpecImpl.fStringPool = fStringPool;
        fContentSpecImpl.fHandle = fContentSpec[chunk][index];
        fContentSpecImpl.fType = fContentSpecType[chunk][index];
        return fContentSpecImpl;
    }

    /** Returns the content spec handle for an element index. */
    public int getContentSpecHandle(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return -1;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpec[chunk][index];
    }

    /** Adds a content spec node. */
    public int addContentSpecNode(int nodeType, int nodeValue, 
                                  int otherNodeValue, 
                                  boolean mustBeUnique) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addContentSpecNode: " + param("nodeType",nodeType) + param("nodeValue",nodeValue) + "\n");
                //****DEBUG****

        if (mustBeUnique) // REVISIT - merge these methods...
            return addContentSpecLeafNode(nodeValue);
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        switch (nodeType) {
            case XMLContentSpec.CONTENTSPECNODE_LEAF:
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE:
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE:
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                fNodeType[chunk][index] = (byte)nodeType;
                fNodeValue[chunk][index] = nodeValue;
                return fNodeCount++;
            }
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
            case XMLContentSpec.CONTENTSPECNODE_SEQ: {
                fNodeType[chunk][index] = (byte)nodeType;
                fNodeValue[chunk][index] = nodeValue;
                int nodeIndex = fNodeCount++;
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    ensureNodeCapacity(chunk);
                    index = 0;
                }
                fNodeType[chunk][index] = (byte)(nodeType | 64); // flag second entry for consistancy checking
                fNodeValue[chunk][index] = otherNodeValue;
                fNodeCount++;
                return nodeIndex;
            }
            default: {
                return -1;
            }
        }

    } // addContentSpecNode(int,int.int,boolean):int

    /** Returns a string representation of a content spec node. */
    public String contentSpecNodeAsString(int contentSpecIndex) {

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        StringBuffer sb = new StringBuffer();
        switch (type) {
            case XMLContentSpec.CONTENTSPECNODE_LEAF: {
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")");
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE: {
                chunk = value >> CHUNK_SHIFT;
                index = value & CHUNK_MASK;
                if (fNodeType[chunk][index] == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                    value = fNodeValue[chunk][index];
                    sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")?");
                } 
                else {
                    appendContentSpecNode(contentSpecIndex, sb, false);
                }
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: {
                chunk = value >> CHUNK_SHIFT;
                index = value & CHUNK_MASK;
                if (fNodeType[chunk][index] == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                    value = fNodeValue[chunk][index];
                    sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")*");
                } 
                else {
                    appendContentSpecNode(contentSpecIndex, sb, false);
                }
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                chunk = value >> CHUNK_SHIFT;
                index = value & CHUNK_MASK;
                if (fNodeType[chunk][index] == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                    value = fNodeValue[chunk][index];
                    sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")+");
                } 
                else {
                    appendContentSpecNode(contentSpecIndex, sb, false);
                }
                break;
            }
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
            case XMLContentSpec.CONTENTSPECNODE_SEQ: {
                appendContentSpecNode(contentSpecIndex, sb, false);
                break;
            }
            default: {
                return null;
            }
        }
        return sb.toString();

    } // contentSpecNodeAsString(int):String

        /** addAttDef. */
    public int addAttDef(QName elementDecl, QName attributeDecl, 
                         int attType, int enumeration, 
                         int attDefaultType, int attDefaultValue, 
                         boolean isExternal) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addAttDef\n");
                //****DEBUG****

        //
        // check fields
        //
        // REVISIT: Validation. This needs to be tuple.
        int elementIndex = getDeclaration(elementDecl);
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int dupID = -1;
        int dupNotation = -1;
        while (attlistIndex != -1) {
            int attrChunk = attlistIndex >> CHUNK_SHIFT;
            int attrIndex = attlistIndex & CHUNK_MASK;
            // REVISIT: Validation. Attributes are also tuples.
            if (fStringPool.equalNames(fAttName[attrChunk][attrIndex], attributeDecl.rawname)) {
                if (fWarningOnDuplicateAttDef) {
                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                      fStringPool.toString(attributeDecl.rawname) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_DUPLICATE_ATTDEF,
                                               XMLMessages.P53_DUPLICATE,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                return -1;
            }
            if (fValidating) {
                if (attType == fIDSymbol && fAttType[attrChunk][attrIndex] == fIDSymbol) {
                    dupID = fAttName[attrChunk][attrIndex];
                }
                if (attType == fNOTATIONSymbol && fAttType[attrChunk][attrIndex] == fNOTATIONSymbol) {
                    dupNotation = fAttName[attrChunk][attrIndex];
                }
            }
            attlistIndex = fNextAttDef[attrChunk][attrIndex];
        }
        if (fValidating) {
            if (dupID != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupID),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_ID_ATTRIBUTE,
                                           XMLMessages.VC_ONE_ID_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
            if (dupNotation != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupNotation),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE,
                                           XMLMessages.VC_ONE_NOTATION_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
        }
        //
        // save the fields
        //
        int chunk = fAttDefCount >> CHUNK_SHIFT;
        int index = fAttDefCount & CHUNK_MASK;
        ensureAttrCapacity(chunk);
        fAttName[chunk][index] = attributeDecl.rawname;
        fAttType[chunk][index] = attType;
        fAttValidator[chunk][index] = getValidatorForAttType(attType);
        fEnumeration[chunk][index] = enumeration;
        fAttDefaultType[chunk][index] = attDefaultType;
        fAttDefIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fAttValue[chunk][index] = attDefaultValue;
        //
        // add to the attr list for this element
        //
        int nextIndex = -1;
        if (attDefaultValue != -1) {
            nextIndex = fAttlistHead[elemChunk][elemIndex];
            fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            }
        } else {
            nextIndex = fAttlistTail[elemChunk][elemIndex];
            fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            }
            else {
                fNextAttDef[nextIndex >> CHUNK_SHIFT][nextIndex & CHUNK_MASK] = fAttDefCount;
                nextIndex = -1;
            }
        }
        fNextAttDef[chunk][index] = nextIndex;
        return fAttDefCount++;

    } // addAttDef(QName,QName,int,int,int,int,boolean):int

    // REVISIT addAttDef a hack for TraverseSchema 
    public int addAttDef(int attlistHeadIndex, QName attributeDecl, 
                         int attType, int enumeration, 
                         int attDefaultType, int attDefaultValue, 
                         boolean isExternal) throws Exception {

        int attlistIndex = attlistHeadIndex;

        int dupID = -1;
        int dupNotation = -1;
        while (attlistIndex != -1) {
            int attrChunk = attlistIndex >> CHUNK_SHIFT;
            int attrIndex = attlistIndex & CHUNK_MASK;
            // REVISIT: Validation. Attributes are also tuples.
            if (fStringPool.equalNames(fAttName[attrChunk][attrIndex], attributeDecl.rawname)) {
                // REVISIT
                /*if (fWarningOnDuplicateAttDef) {
                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                      fStringPool.toString(attributeDecl.rawname) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_DUPLICATE_ATTDEF,
                                               XMLMessages.P53_DUPLICATE,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }*/
                return -1;
            }
            if (fValidating) {
                if (attType == fIDSymbol && fAttType[attrChunk][attrIndex] == fIDSymbol) {
                    dupID = fAttName[attrChunk][attrIndex];
                }
                if (attType == fNOTATIONSymbol && fAttType[attrChunk][attrIndex] == fNOTATIONSymbol) {
                    dupNotation = fAttName[attrChunk][attrIndex];
                }
            }
            attlistIndex = fNextAttDef[attrChunk][attrIndex];
        }
        if (fValidating) {
            //REVISIT
            /*if (dupID != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupID),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_ID_ATTRIBUTE,
                                           XMLMessages.VC_ONE_ID_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }*/
            /*if (dupNotation != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupNotation),
                                  fStringPool.toString(attributeDecl.rawname) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE,
                                           XMLMessages.VC_ONE_NOTATION_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }*/
        }
        //
        // save the fields
        //
        int chunk = fAttDefCount >> CHUNK_SHIFT;
        int index = fAttDefCount & CHUNK_MASK;
        ensureAttrCapacity(chunk);
        fAttName[chunk][index] = attributeDecl.rawname;
        fAttType[chunk][index] = attType;
        fAttValidator[chunk][index] = getValidatorForAttType(attType);
        fEnumeration[chunk][index] = enumeration;
        fAttDefaultType[chunk][index] = attDefaultType;
        fAttDefIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fAttValue[chunk][index] = attDefaultValue;
        //
        // add to the attr list for this element
        //
        int nextIndex = -1;
        /*        if (attDefaultValue != -1) {
            nextIndex = fAttlistHead[elemChunk][elemIndex];
            fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            }
        } else {
            nextIndex = fAttlistTail[elemChunk][elemIndex];
            fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1) {
                fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            }
            else {
                fNextAttDef[nextIndex >> CHUNK_SHIFT][nextIndex & CHUNK_MASK] = fAttDefCount;
                nextIndex = -1;
            }
        }*/
        nextIndex = attlistHeadIndex;
        fNextAttDef[chunk][index] = nextIndex;
        
        //return fAttDefCount++;
        return fAttDefCount++;

    } // addAttDef(QName,QName,int,int,int,int,boolean):int

    /** Copy attributes. */
    public void copyAtts(QName fromElement, QName toElement) {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.copyAtts: " + param("fromElementType",fromElement.rawname) + param("toElementType",toElement.rawname) + "\n");
                //****DEBUG****

        // REVISIT: Validation.
        int fromElementIndex = getDeclaration(fromElement);
        int toElementIndex = getDeclaration(toElement);
        if (fromElementIndex == -1) {
            return;
        }
        int chunk = fromElementIndex >> CHUNK_SHIFT;
        int index = fromElementIndex & CHUNK_MASK;
        int attDefIndex = fAttlistHead[chunk][index];
        while (attDefIndex != -1) {
            chunk = attDefIndex >> CHUNK_SHIFT;
            index = attDefIndex & CHUNK_MASK;
            try {
                // REVISIT: Validation. Needs to be tuples.
                int attName = fAttName[chunk][index];
                fTempQName.setValues(-1, attName, attName, -1);
                addAttDef(toElement, fTempQName, 
                          fAttType[chunk][index],
                          fEnumeration[chunk][index], 
                          fAttDefaultType[chunk][index], 
                          fAttValue[chunk][index], 
                          fAttDefIsExternal[chunk][index] != 0);
            } catch (Exception ex) {
                // REVISIT: What should happen here?
            }
            attDefIndex = fNextAttDef[chunk][index];
        }

    } // copyAtts(QName,QName)

    // a hack for traverseElementDecl in Traverse Schema
    public void copyAttsForSchema(int fromAttDefIndex, QName toElement) {

                //****DEBUG****
                //if (DEBUG) print("(POP) XMLValidator.copyAtts: " + param("fromElementType",fromElement.rawname) + param("toElementType",toElement.rawname) + "\n");
                //****DEBUG****

        // REVISIT: Validation.
        int toElementIndex = getDeclaration(toElement);
        if (fromAttDefIndex == -1) {
            return;
        }

        int chunk;
        int index;

        int attDefIndex = fromAttDefIndex;
        while (attDefIndex != -1) {
            chunk = attDefIndex >> CHUNK_SHIFT;
            index = attDefIndex & CHUNK_MASK;
            try {
                // REVISIT: Validation. Needs to be tuples.
                int attName = fAttName[chunk][index];
                fTempQName.setValues(-1, attName, attName, -1);
                addAttDef(toElement, fTempQName, 
                          fAttType[chunk][index],
                          fEnumeration[chunk][index], 
                          fAttDefaultType[chunk][index], 
                          fAttValue[chunk][index], 
                          fAttDefIsExternal[chunk][index] != 0);
            } catch (Exception ex) {
                // REVISIT: What should happen here?
            }
            attDefIndex = fNextAttDef[chunk][index];
        }

    } // copyAtts(QName,QName)

    //
    // Protected methods
    //

    // error reporting
    
    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode) 
        throws Exception {

        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   null,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int)

    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode, 
                                             int stringIndex1) 
        throws Exception {

        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int,int)

    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode, 
                                             String string1) throws Exception {

        Object[] args = { string1 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int,String)

    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode, 
                                             int stringIndex1, int stringIndex2) 
        throws Exception {

        Object[] args = { fStringPool.toString(stringIndex1), fStringPool.toString(stringIndex2) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int,int,int)

    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode, 
                                             String string1, String string2) 
        throws Exception {

        Object[] args = { string1, string2 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int,String,String)

    /** Report a recoverable xml error. */
    protected void reportRecoverableXMLError(int majorCode, int minorCode, 
                                             String string1, String string2, 
                                             String string3) throws Exception {

        Object[] args = { string1, string2, string3 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);

    } // reportRecoverableXMLError(int,int,String,String,String)

    // content spec

    /** Protected for access by ContentSpecImpl. */
    protected void getContentSpecNode(int contentSpecIndex, 
                                      XMLContentSpec csn) {

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        csn.type = fNodeType[chunk][index];
        csn.value = fNodeValue[chunk][index];
        if (csn.type == XMLContentSpec.CONTENTSPECNODE_CHOICE || csn.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
            csn.otherValue = fNodeValue[chunk][index];
        } 
        else {
            csn.otherValue = -1;
        }

    } // getContentSpecNode(int,XMLContentSpec)

    /** Returns a string representation of a content spec. */
    protected String getContentSpecAsString(int elementIndex) {

        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return null;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        int contentSpecType = fContentSpecType[chunk][index];
        if (contentSpecType == fMIXEDSymbol || contentSpecType == fCHILDRENSymbol) {
            return contentSpecNodeAsString(fContentSpec[chunk][index]);
        }
        else {
            return fStringPool.toString(contentSpecType);
        }

    } // getContentSpecAsString(int):String

    /**
     * Returns information about which elements can be placed at a particular point
     * in the passed element's content model.
     * <p>
     * Note that the incoming content model to test must be valid at least up to
     * the insertion point. If not, then -1 will be returned and the info object
     * will not have been filled in.
     * <p>
     * If, on return, the info.isValidEOC flag is set, then the 'insert after'
     * elemement is a valid end of content, i.e. nothing needs to be inserted
     * after it to make the parent element's content model valid.
     *
     * @param elementIndex The index within the <code>ElementDeclPool</code> of the
     *                     element which is being querying.
     * @param fullyValid Only return elements that can be inserted and still
     *                   maintain the validity of subsequent elements past the
     *                   insertion point (if any).  If the insertion point is at
     *                   the end, and this is true, then only elements that can
     *                   be legal final states will be returned.
     * @param info An object that contains the required input data for the method,
     *             and which will contain the output information if successful.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed before the insertion point. If the value
     *         returned is equal to the number of children, then the specified
     *         children are valid but additional content is required to reach a
     *         valid ending state.
     *
     * @exception Exception Thrown on error.
     *
     * @see InsertableElementsInfo
     */
    protected int whatCanGoHere(int elementIndex, boolean fullyValid,
                                InsertableElementsInfo info) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.whatCanGoHere: ...\n");
                //****DEBUG****

        //
        //  Do some basic sanity checking on the info packet. First, make sure
        //  that insertAt is not greater than the child count. It can be equal,
        //  which means to get appendable elements, but not greater. Or, if
        //  the current children array is null, that's bad too.
        //
        //  Since the current children array must have a blank spot for where
        //  the insert is going to be, the child count must always be at least
        //  one.
        //
        //  Make sure that the child count is not larger than the current children
        //  array. It can be equal, which means get appendable elements, but not
        //  greater.
        //
        if (info.insertAt > info.childCount || info.curChildren == null ||  
            info.childCount < 1 || info.childCount > info.curChildren.length) {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       ImplementationMessages.VAL_WCGHI,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }

        int retVal = 0;
        try {
            // Get the content model for this element
            final XMLContentModel cmElem = getContentModel(elementIndex);

            // And delegate this call to it
            retVal = cmElem.whatCanGoHere(fullyValid, info);
        }
        catch (CMException excToCatch) {
            // REVISIT - Translate caught error to the protected error handler interface
            int majorCode = excToCatch.getErrorCode();
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       majorCode,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }
        return retVal;

    } // whatCanGoHere(int,boolean,InsertableElementsInfo):int

    // attribute information

    /** Protected for use by AttributeValidator classes. */
    protected boolean getAttDefIsExternal(QName element, QName attribute) {
        int attDefIndex = getAttDef(element, attribute);
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return (fAttDefIsExternal[chunk][index] != 0);
    }

    /** addId. */
    protected boolean addId(int idIndex) {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addId" + param("idIndex",idIndex) + "\n");
                //****DEBUG****

                //System.err.println("addId(" + fStringPool.toString(idIndex) + ") " + idIndex);
        Integer key = new Integer(idIndex);
        if (fIdDefs == null) {
            fIdDefs = new Hashtable();
        }
        else if (fIdDefs.containsKey(key)) {
            return false;
        }
        if (fNullValue == null) {
            fNullValue = new Object();
        }
        fIdDefs.put(key, fNullValue/*new Integer(elementType)*/);
        return true;

    } // addId(int):boolean

    /** addIdRef. */
    protected void addIdRef(int idIndex) {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addIdRef" + param("idIndex",idIndex) + "\n");
                //****DEBUG****

                //System.err.println("addIdRef(" + fStringPool.toString(idIndex) + ") " + idIndex);
        Integer key = new Integer(idIndex);
        if (fIdDefs != null && fIdDefs.containsKey(key)) {
            return;
        }
        if (fIdRefs == null) {
            fIdRefs = new Hashtable();
        }
        else if (fIdRefs.containsKey(key)) {
            return;
        }
        if (fNullValue == null) {
            fNullValue = new Object();
        }
        fIdRefs.put(key, fNullValue/*new Integer(elementType)*/);

    } // addIdRef(int)

    //
    // Private methods
    //

    // other

    /** Returns true if using a standalone reader. */
    private boolean usingStandaloneReader() {
        return fStandaloneReader == -1 || fEntityHandler.getReaderId() == fStandaloneReader;
    }

    /** Returns a locator implementation. */
    private LocatorImpl getLocatorImpl(LocatorImpl fillin) {

                //****DEBUG****
                if (DEBUG) print("(INF) XMLValidator.getLocatorImpl: ...\n");
                //****DEBUG****

        Locator here = fErrorReporter.getLocator();
        if (fillin == null)
            return new LocatorImpl(here);
        fillin.setPublicId(here.getPublicId());
        fillin.setSystemId(here.getSystemId());
        fillin.setLineNumber(here.getLineNumber());
        fillin.setColumnNumber(here.getColumnNumber());
        return fillin;

    } // getLocatorImpl(LocatorImpl):LocatorImpl

    // content models

    /**
     * When the element has a 'CHILDREN' model, this method is called to
     * create the content model object. It looks for some special case simple
     * models and creates SimpleContentModel objects for those. For the rest
     * it creates the standard DFA style model.
     */
    private XMLContentModel createChildModel(int elementIndex) 
        throws CMException {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.createChildModel: " + param("elementIndex",elementIndex) + "\n");
                //****DEBUG****

        //
        //  Get the content spec node for the element we are working on.
        //  This will tell us what kind of node it is, which tells us what
        //  kind of model we will try to create.
        //
        XMLContentSpec specNode = new XMLContentSpec();
        int contentSpecIndex = getContentSpecHandle(elementIndex);
        getContentSpecNode(contentSpecIndex, specNode);

        //
        //  Check that the left value is not -1, since any content model
        //  with PCDATA should be MIXED, so we should not have gotten here.
        //
        if (specNode.value == -1) {
            throw new CMException(ImplementationMessages.VAL_NPCD);
        }

        if (specNode.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            //
            //  Its a single leaf, so its an 'a' type of content model, i.e.
            //  just one instance of one element. That one is definitely a
            //  simple content model.
            //
            return new SimpleContentModel(specNode.value, -1, specNode.type);
        }
        else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_CHOICE || 
                 specNode.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
            //
            //  Lets see if both of the children are leafs. If so, then it
            //  it has to be a simple content model
            //
            XMLContentSpec specLeft = new XMLContentSpec();
            XMLContentSpec specRight = new XMLContentSpec();
            getContentSpecNode(specNode.value, specLeft);
            getContentSpecNode(specNode.otherValue, specRight);

            if (specLeft.type == XMLContentSpec.CONTENTSPECNODE_LEAF && 
                specRight.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                //
                //  Its a simple choice or sequence, so we can do a simple
                //  content model for it.
                //
                return new SimpleContentModel(specLeft.value, 
                                              specRight.value, 
                                              specNode.type);
            }
        }
        else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE ||  
                 specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE || 
                 specNode.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
            //
            //  Its a repetition, so see if its one child is a leaf. If so
            //  its a repetition of a single element, so we can do a simple
            //  content model for that.
            //
            XMLContentSpec specLeft = new XMLContentSpec();
            getContentSpecNode(specNode.value, specLeft);

            if (specLeft.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                //
                //  It is, so we can create a simple content model here that
                //  will check for this repetition. We pass -1 for the unused
                //  right node.
                //
                return new SimpleContentModel(specLeft.value, -1, specNode.type);
            }
        }
        else {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        //
        //  Its not a simple content model, so here we have to create a DFA
        //  for this element. So we create a DFAContentModel object. He
        //  encapsulates all of the work to create the DFA.
        //
        fLeafCount = 0;
        CMNode cmn = buildSyntaxTree(contentSpecIndex, specNode);
        return new DFAContentModel(fStringPool, cmn, fLeafCount);

    } // createChildModel(int):XMLContentModel

    /**
     * This method will handle the querying of the content model for a
     * particular element. If the element does not have a content model, then
     * it will be created.
     */
    private XMLContentModel getContentModel(int elementIndex) 
        throws CMException {

                //****DEBUG****
                if (DEBUG) print("(INF) XMLValidator.getContentModel: " + param("elementIndex",elementIndex) + "\n");
                //****DEBUG****

        // See if a content model already exists first
        XMLContentModel cmRet = getElementContentModel(elementIndex);

        // If we have one, just return that. Otherwise, gotta create one
        if (cmRet != null) {
            return cmRet;
        }

        // Get the type of content this element has
        final int contentSpec = getContentSpecType(elementIndex);

        // And create the content model according to the spec type
        if (contentSpec == fMIXEDSymbol) {
            //
            //  Just create a mixel content model object. This type of
            //  content model is optimized for mixed content validation.
            //
            XMLContentSpec specNode = new XMLContentSpec();
            int contentSpecIndex = getContentSpecHandle(elementIndex);
            makeContentList(contentSpecIndex, specNode);
            cmRet = new MixedContentModel(fCount, fContentList);
        }
        else if (contentSpec == fCHILDRENSymbol) {
            //
            //  This method will create an optimal model for the complexity
            //  of the element's defined model. If its simple, it will create
            //  a SimpleContentModel object. If its a simple list, it will
            //  create a SimpleListContentModel object. If its complex, it
            //  will create a DFAContentModel object.
            //
            cmRet = createChildModel(elementIndex);
        }
        else if (contentSpec == fDATATYPESymbol) {
            cmRet = fSchemaImporter.createDatatypeContentModel(elementIndex);
        }
        else {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        // Add the new model to the content model for this element
        setContentModel(elementIndex, cmRet);

        return cmRet;

    } // getContentModel(int):XMLContentModel

    /**
     * This method will build our syntax tree by recursively going though
     * the element's content model and creating new CMNode type node for
     * the model, and rewriting '?' and '+' nodes along the way.
     * <p>
     * On final return, the head node of the syntax tree will be returned.
     * This top node will be a sequence node with the left side being the
     * rewritten content, and the right side being a special end of content
     * node.
     * <p>
     * We also count the non-epsilon leaf nodes, which is an important value
     * that is used in a number of places later.
     */
    private CMNode buildSyntaxTree(int startNode, XMLContentSpec specNode) 
        throws CMException {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.buildSyntaxTree: ... \n");
                //****DEBUG****

        // We will build a node at this level for the new tree
        CMNode nodeRet = null;

        getContentSpecNode(startNode, specNode);

        //
        //  If this node is a leaf, then its an easy one. We just add it
        //  to the tree.
        //
        if (specNode.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            //
            //  Create a new leaf node, and pass it the current leaf count,
            //  which is its DFA state position. Bump the leaf count after
            //  storing it. This makes the positions zero based since we
            //  store first and then increment.
            //
            nodeRet = new CMLeaf(specNode.type, specNode.value, fLeafCount++);
        }
        else {
            //
            //  Its not a leaf, so we have to recurse its left and maybe right
            //  nodes. Save both values before we recurse and trash the node.
            //
            final int leftNode = specNode.value;
            final int rightNode = specNode.otherValue;

            if (specNode.type == XMLContentSpec.CONTENTSPECNODE_CHOICE || 
                specNode.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
                //
                //  Recurse on both children, and return a binary op node
                //  with the two created sub nodes as its children. The node
                //  type is the same type as the source.
                //

                nodeRet = new CMBinOp(specNode.type, 
                                      buildSyntaxTree(leftNode, specNode), 
                                      buildSyntaxTree(rightNode, specNode));
            }
            else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE) {
                // This one is fine as is, just change to our form
                nodeRet = new CMUniOp(specNode.type, 
                                      buildSyntaxTree(leftNode, specNode));
            }
            else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE) {
                // Convert to (x|epsilon)
                nodeRet = new CMBinOp(XMLContentSpec.CONTENTSPECNODE_CHOICE, 
                                      buildSyntaxTree(leftNode, specNode), 
                                      new CMLeaf(XMLContentSpec.CONTENTSPECNODE_LEAF, fEpsilonIndex));
            }
            else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
                // Convert to (x,x*)
                nodeRet = new CMBinOp(XMLContentSpec.CONTENTSPECNODE_SEQ, 
                                      buildSyntaxTree(leftNode, specNode), 
                                      new CMUniOp(XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE, 
                                                  buildSyntaxTree(leftNode, specNode))
                                      );
            }
            else {
                throw new CMException(ImplementationMessages.VAL_CST);
            }
        }

        // And return our new node for this level
        return nodeRet;

    } // buildSyntaxTree(int,XMLContentSpec):CMNode

    /** Makes a content list. */
    private void makeContentList(int startNode, XMLContentSpec specNode) 
        throws CMException {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.makeContentList: ...\n");
                //****DEBUG****

        //
        //  Ok, we need to build up an array of the possible children
        //  under this element. The mixed content model can only be a
        //  repeated series of alternations with no numeration or ordering.
        //  So we call a local recursive method to iterate the tree and
        //  build up the array.
        //
        //  So we get the content spec of the element, which gives us the
        //  starting node. Everything else kicks off from there. We pass
        //  along a content node for each iteration to use so that it does
        //  not have to create and trash lots of objects.
        //
        while (true) {
            fCount = 0;

            try {
                fCount = buildContentList(startNode, 0, specNode);
            }
            catch(IndexOutOfBoundsException excToCatch) {
                //
                //  Expand the array and try it again. Yes, this is
                //  piggy, but the odds of it ever actually happening
                //  are slim to none.
                //
                fContentList = new int[fContentList.length * 2];
                fCount = 0;
                continue;
            }

            // We survived, so break out
            break;
        }

    } // makeContentList(int,XMLContentSpec)

    /** Builds a content list. */
    private int buildContentList(int startNode, int count, 
                                 XMLContentSpec specNode) 
        throws CMException {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.buildContentList: ...\n");
                //****DEBUG****

        // Get the content spec for the passed start node
        getContentSpecNode(startNode, specNode);

        // If this node is a leaf, then add it to our list and return.
        if (specNode.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            fContentList[count++] = specNode.value;
            return count;
        }

        //
        //  Its not a leaf, so we have to recurse its left and maybe right
        //  nodes. Save both values before we recurse and trash the node.
        //
        final int leftNode = specNode.value;
        final int rightNode = specNode.otherValue;

        if (specNode.type == XMLContentSpec.CONTENTSPECNODE_CHOICE || 
            specNode.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
            //
            //  Recurse on the left and right nodes of this guy, making sure
            //  to keep the count correct.
            //
            count = buildContentList(leftNode, count, specNode);
            count = buildContentList(rightNode, count, specNode);
        }
        else if (specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE || 
                 specNode.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE ||
                 specNode.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
            // Just do the left node on this one
            count = buildContentList(leftNode, count, specNode);
        }
        else {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        // And return our accumlated new count
        return count;

    } // buildContentList(int,int,XMLContentSpec):int

    // initialization

    /** Reset pool. */
    private void poolReset() {

        int chunk = 0;
        int index = 0;
        for (int i = 0; i < fElementCount; i++) {
            fContentModel[chunk][index] = null;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        fElementCount = 0;
        fNodeCount = 0;
        fAttDefCount = 0;
        if (fIdDefs != null) {
            fIdDefs.clear();
        }
        if (fIdRefs != null) {
            fIdRefs.clear();
        }

    } // poolReset()

    /** Reset common. */
    private void resetCommon(StringPool stringPool) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.resetCommon\n");
                //****DEBUG****
        fStringPool = stringPool;
        fValidating = fValidationEnabled;
        fValidationEnabledByDynamic = false;
        fDynamicDisabledByValidation = false;
        poolReset();
        fCalledStartDocument = false;
        fStandaloneReader = -1;
        fElementDepth = -1;
        fSeenRootElement = false;
        fSeenDoctypeDecl = false;
        fNamespacesScope = null;
        fNamespacesPrefix = -1;
        fRootElement.clear();
        fAttrListHandle = -1;
        fElementDeclCount = 0;
        fCheckedForSchema = false;
        fSchemaDocument = null;
        init();

    } // resetCommon(StringPool)

    /** Initialize. */
    private void init() {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.init\n");
                //****DEBUG****
        fEMPTYSymbol = fStringPool.addSymbol("EMPTY");
        fANYSymbol = fStringPool.addSymbol("ANY");
        fMIXEDSymbol = fStringPool.addSymbol("MIXED");
        fCHILDRENSymbol = fStringPool.addSymbol("CHILDREN");
        fCDATASymbol = fStringPool.addSymbol("CDATA");
        fIDSymbol = fStringPool.addSymbol("ID");
        fIDREFSymbol = fStringPool.addSymbol("IDREF");
        fIDREFSSymbol = fStringPool.addSymbol("IDREFS");
        fENTITYSymbol = fStringPool.addSymbol("ENTITY");
        fENTITIESSymbol = fStringPool.addSymbol("ENTITIES");
        fNMTOKENSymbol = fStringPool.addSymbol("NMTOKEN");
        fNMTOKENSSymbol = fStringPool.addSymbol("NMTOKENS");
        fNOTATIONSymbol = fStringPool.addSymbol("NOTATION");
        fENUMERATIONSymbol = fStringPool.addSymbol("ENUMERATION");
        fREQUIREDSymbol = fStringPool.addSymbol("#REQUIRED");
        fFIXEDSymbol = fStringPool.addSymbol("#FIXED");
        fDATATYPESymbol = fStringPool.addSymbol("DATATYPE");
        fEpsilonIndex = fStringPool.addSymbol("<<CMNODE_EPSILON>>");
        fXMLLang = fStringPool.addSymbol("xml:lang");

    } // init()

    // other

    /** Appends a content spec node to a string buffer. */
    private void appendContentSpecNode(int contentSpecIndex, 
                                       StringBuffer sb, boolean noParen) {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.appendContentSpecNode: ...\n");
                //****DEBUG****

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        switch (type) {
            case XMLContentSpec.CONTENTSPECNODE_LEAF: {
                sb.append(value == -1 ? "#PCDATA" : fStringPool.toString(value));
                return;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE: {
                appendContentSpecNode(value, sb, false);
                sb.append('?');
                return;
            }
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: {
                appendContentSpecNode(value, sb, false);
                sb.append('*');
                return;
            }
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                appendContentSpecNode(value, sb, false);
                sb.append('+');
                return;
            }
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
            case XMLContentSpec.CONTENTSPECNODE_SEQ: {
                if (!noParen) {
                    sb.append('(');
                }
                int leftChunk = value >> CHUNK_SHIFT;
                int leftIndex = value & CHUNK_MASK;
                int leftType = fNodeType[leftChunk][leftIndex];
                appendContentSpecNode(value, sb, leftType == type);
                sb.append(type == XMLContentSpec.CONTENTSPECNODE_CHOICE ? '|' : ',');
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    index = 0;
                }
                appendContentSpecNode(fNodeValue[chunk][index], sb, false);
                if (!noParen) {
                    sb.append(')');
                }
                return;
            }
            default: {
                return;
            }
        }

    } // appendContentSpecNode(int,StringBuffer,boolean)

    // default attribute

        /** addDefaultAttributes. */
    private int addDefaultAttributes(int elementIndex, XMLAttrList attrList, int attrIndex, boolean validationEnabled, boolean standalone) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addDefaultAttributes\n");
                //****DEBUG****

        //
        // Check after all specified attrs are scanned
        // (1) report error for REQUIRED attrs that are missing (V_TAGc)
        // (2) check that FIXED attrs have matching value (V_TAGd)
        // (3) add default attrs (FIXED and NOT_FIXED)
        //
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int firstCheck = attrIndex;
        int lastCheck = -1;
        while (attlistIndex != -1) {
            int adChunk = attlistIndex >> CHUNK_SHIFT;
            int adIndex = attlistIndex & CHUNK_MASK;
            int attPrefix = fAttPrefix[adChunk][adIndex];
            int attName = fAttName[adChunk][adIndex];
            int attType = fAttType[adChunk][adIndex];
            int attDefType = fAttDefaultType[adChunk][adIndex];
            int attValue = fAttValue[adChunk][adIndex];
            boolean specified = false;
            boolean required = attDefType == fREQUIREDSymbol;
            if (firstCheck != -1) {
                boolean cdata = attType == fCDATASymbol;
                if (!cdata || required || attValue != -1) {
                    int i = attrList.getFirstAttr(firstCheck);
                    while (i != -1 && (lastCheck == -1 || i <= lastCheck)) {
                        if (fStringPool.equalNames(attrList.getAttrName(i), attName)) {
                            if (validationEnabled && attDefType == fFIXEDSymbol) {
                                int alistValue = attrList.getAttValue(i);
                                if (alistValue != attValue &&
                                    !fStringPool.toString(alistValue).equals(fStringPool.toString(attValue))) {
                                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                                    fStringPool.toString(attName),
                                                    fStringPool.toString(alistValue),
                                                    fStringPool.toString(attValue) };
                                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                            XMLMessages.XML_DOMAIN,
                                                            XMLMessages.MSG_FIXED_ATTVALUE_INVALID,
                                                            XMLMessages.VC_FIXED_ATTRIBUTE_DEFAULT,
                                                            args,
                                                            XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                                }
                            }
                            specified = true;
                            break;
                        }
                        i = attrList.getNextAttr(i);
                    }
                }
            }
            if (!specified) {
                if (required) {
                    if (validationEnabled) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_REQUIRED_ATTRIBUTE,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                } 
                else if (attValue != -1) {
                    if (validationEnabled && standalone && fAttDefIsExternal[adChunk][adIndex] != 0) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                    if (attType == fIDREFSymbol) {
                        addIdRef(attValue);
                    } 
                    else if (attType == fIDREFSSymbol) {
                        StringTokenizer tokenizer = new StringTokenizer(fStringPool.toString(attValue));
                        while (tokenizer.hasMoreTokens()) {
                            String idName = tokenizer.nextToken();
                            addIdRef(fStringPool.addSymbol(idName));
                        }
                    }
                    if (attrIndex == -1) {
                        attrIndex = attrList.startAttrList();
                    }
                    // REVISIT: Validation. What should the prefix be?
                    fTempQName.setValues(attPrefix, attName, attName);
                    int newAttr = attrList.addAttr(fTempQName, 
                                                   attValue, attType, 
                                                   false, false);
                    if (lastCheck == -1) {
                        lastCheck = newAttr;
                    }
                }
            }
            attlistIndex = fNextAttDef[adChunk][adIndex];
        }
        return attrIndex;

    } // addDefaultAttributes(int,XMLAttrList,int,boolean,boolean):int

    public void setCurrentScope(int scope) {
        fCurrentScope = scope;
    }

    public int getCurrentScope() {
        return fCurrentScope;
    }

    
    // string pool to declaration mapping

    /** Sets the string pool to declaration mapping. */
    /*private void setDeclaration(QName qname, int decl) {
        // REVISIT: Validation. Key from <uri, localpart> tuple.
        int stringIndex = qname.rawname;
        ensureDeclarationCapacity(stringIndex);
        fDeclaration[stringIndex] = decl;
    }*/

    //REVISIT: ye
    private void setDeclaration(QName qname, int decl) {
        // REVISIT: 1)Where will the fCurrentScope come from?
        //          2)Should uri be checked if present? 
        int uri = qname.uri;

        if (uri==-1) {
            fNameScopeToIndex.put(qname.localpart, fCurrentScope, decl);
        }
        else {
            fNameScopeToIndex.put(qname.localpart, TOP_LEVEL_SCOPE, decl);
        }
    }

    /** Returns the string pool to declaration mapping. */
    /*private int getDeclaration(QName qname) {
        // REVISIT: Validation. Key from <uri, localpart> tuple.
        int stringIndex = qname.rawname;
        if (fDeclaration == null || 
            stringIndex < 0 || stringIndex >= fDeclaration.length) {
            return -1;
        }
        return fDeclaration[stringIndex];
    }*/

    //REVISIT: ye
    private int getDeclaration(QName qname) {
    // REVISIT: should we pass in the scope? switchNS(uri) should be done
        //      before we come in.
        int uri = qname.uri;
        if (uri == -1) {
            return fNameScopeToIndex.get(qname.localpart,fCurrentScope);
        }
        else {
            return fNameScopeToIndex.get(qname.localpart, TOP_LEVEL_SCOPE);
        }
        /*else if ( uri == fCurrentSchemaURI ) {
            return fNameScopeToIndex.get(qname.localpart, TOP_LEVEL_SCOPE);
        }
        else {
            switchNS(uri); //REVISIT: or this should be done before we come in.?? 
            return fNameScopeToIndex.get(qname.localpart, TOP_LEVEL_SCOPE);
        }*/
   }

   public int getDeclaration(int localpart, int scope) {
            return fNameScopeToIndex.get(localpart, scope);
   }


    // content specs

    /** Adds a content spec leaf node. */
    private int addContentSpecLeafNode(int nodeValue) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(POP) XMLValidator.addContentSpecLeafNode: " + param("nodeValue",nodeValue) + "\n");
                //****DEBUG****

        //
        // Check that we have not seen this value before...
        //
        if (nodeValue != -1) {
            int nodeCount = fNodeCount;
            int chunk = fNodeCount >> CHUNK_SHIFT;
            int index = fNodeCount & CHUNK_MASK;
            while (true) {
                if (index-- == 0) {
                    index = CHUNK_SIZE - 1;
                    chunk--;
                }
                int nodeType = fNodeType[chunk][index];
                if (nodeType == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                    int otherNodeValue = fNodeValue[chunk][index];
                    if (otherNodeValue == -1) {
                        break;
                    }
                    if (otherNodeValue == nodeValue) {
                        return -1;
                    }
                }
            }
        }
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        fNodeType[chunk][index] = (byte)XMLContentSpec.CONTENTSPECNODE_LEAF;
        fNodeValue[chunk][index] = nodeValue;
        return fNodeCount++;

    } // addContentSpecLeafNode(int):int

    // content models

    /** Queries the content model for the specified element index. */
    private XMLContentModel getElementContentModel(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return null;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentModel[chunk][index];
    }

    /** Sets the content model for the specified element index. */
    private void setContentModel(int elementIndex, XMLContentModel cm) {
        if (elementIndex < 0 || elementIndex >= fElementCount) {
            return;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        fContentModel[chunk][index] = cm;
    }

    // ensure capacity

    /** Ensures that there is enough storage for element information. */
    private boolean ensureElementCapacity(int chunk) {

        try {
            return fElementType[chunk][0] == 0;
        } 
        catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fElementDeclIsExternal, 0, newByteArray, 0, chunk);
            fElementDeclIsExternal = newByteArray;
            //REVISIT: fElementType            
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fElementType, 0, newIntArray, 0, chunk);
            fElementType = newIntArray;
            //REVISIT: fElementQName           
            QName[][] newQNameArray = new QName[chunk * 2][];
            System.arraycopy(fElementQName, 0, newQNameArray, 0, chunk);
            fElementQName = newQNameArray;
            //REVISIT: fScope             
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fScope, 0, newIntArray, 0, chunk);
            fScope = newIntArray;

            newIntArray = new int[chunk * 2][];
            System.arraycopy(fContentSpecType, 0, newIntArray, 0, chunk);
            fContentSpecType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fContentSpec, 0, newIntArray, 0, chunk);
            fContentSpec = newIntArray;
            XMLContentModel[][] newContentModel = new XMLContentModel[chunk * 2][];
            System.arraycopy(fContentModel, 0, newContentModel, 0, chunk);
            fContentModel = newContentModel;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttlistHead, 0, newIntArray, 0, chunk);
            fAttlistHead = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttlistTail, 0, newIntArray, 0, chunk);
            fAttlistTail = newIntArray;
        } catch (NullPointerException ex) {
            // ignore
        }
        //REVISIT: fElementType, fElementQName, fScope
        fElementType[chunk] = new int[CHUNK_SIZE];
        fElementQName[chunk] = new QName[CHUNK_SIZE];
        fScope[chunk] = new int[CHUNK_SIZE];
        //by default, all the scope should be top-level
        for (int i=0; i<CHUNK_SIZE; i++) {
            fScope[chunk][i] = TOP_LEVEL_SCOPE;
        }

        fElementDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
        fContentSpecType[chunk] = new int[CHUNK_SIZE];
        fContentSpec[chunk] = new int[CHUNK_SIZE];
        fContentModel[chunk] = new XMLContentModel[CHUNK_SIZE];
        fAttlistHead[chunk] = new int[CHUNK_SIZE];
        fAttlistTail[chunk] = new int[CHUNK_SIZE];
        return true;

    } // ensureElementCapacity(int):boolean

    /** Ensures that there is enough storage for node information. */
        private boolean ensureNodeCapacity(int chunk) {

        try {
            return fNodeType[chunk][0] == 0;
        } 
        catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fNodeType, 0, newByteArray, 0, chunk);
            fNodeType = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fNodeValue, 0, newIntArray, 0, chunk);
            fNodeValue = newIntArray;
        } 
        catch (NullPointerException ex) {
            // ignore
        }
        fNodeType[chunk] = new byte[CHUNK_SIZE];
        fNodeValue[chunk] = new int[CHUNK_SIZE];
        return true;

    } // ensureNodeCapacity(int):boolean

    /** Ensures that there's enough storage for attribute information. */
    private boolean ensureAttrCapacity(int chunk) {

        try {
            return fAttName[chunk][0] == 0;
        } 
        catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fAttDefIsExternal, 0, newByteArray, 0, chunk);
            fAttDefIsExternal = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttPrefix, 0, newIntArray, 0, chunk);
            fAttPrefix = newIntArray;
            System.arraycopy(fAttName, 0, newIntArray, 0, chunk);
            fAttName = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttType, 0, newIntArray, 0, chunk);
            fAttType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fEnumeration, 0, newIntArray, 0, chunk);
            fEnumeration = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttDefaultType, 0, newIntArray, 0, chunk);
            fAttDefaultType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttValue, 0, newIntArray, 0, chunk);
            fAttValue = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fNextAttDef, 0, newIntArray, 0, chunk);
            fNextAttDef = newIntArray;
            AttributeValidator[][] newValidatorArray = new AttributeValidator[chunk * 2][];
            System.arraycopy(fAttValidator, 0, newValidatorArray, 0, chunk);
            fAttValidator = newValidatorArray;
        } 
        catch (NullPointerException ex) {
            // ignore
        }
        fAttDefIsExternal[chunk] = new byte[CHUNK_SIZE];
        fAttPrefix[chunk] = new int[CHUNK_SIZE];
        fAttName[chunk] = new int[CHUNK_SIZE];
        fAttType[chunk] = new int[CHUNK_SIZE];
        fAttValidator[chunk] = new AttributeValidator[CHUNK_SIZE];
        fEnumeration[chunk] = new int[CHUNK_SIZE];
        fAttDefaultType[chunk] = new int[CHUNK_SIZE];
        fAttValue[chunk] = new int[CHUNK_SIZE];
        fNextAttDef[chunk] = new int[CHUNK_SIZE];
        return true;

    } // ensureAttrCapacity(int):boolean

    /** 
     * Ensures that there is enough storage for the string pool to
     * declaration mappings.
     */
    private void ensureDeclarationCapacity(int stringIndex) {
        if (fDeclaration == null) {
            fDeclaration = new int[stringIndex + 1];
            for (int i = 0; i < stringIndex; i++) {
                fDeclaration[i] = -1;
            }
        }
        else if (fDeclaration.length < stringIndex + 1) {
            int newint[] = new int[stringIndex + 1];
            System.arraycopy(fDeclaration, 0, newint, 0, fDeclaration.length);
            for (int i = fDeclaration.length; i < stringIndex - 1; i++) {
                newint[i] = -1;
            }
            fDeclaration = newint;
        }
    }

    // query attribute information

    /** Returns the validatator for an attribute type. */
    private AttributeValidator getValidatorForAttType(int attType) {
        if (attType == fCDATASymbol) {
            if (fAttValidatorCDATA == null) {
                fAttValidatorCDATA = new AttValidatorCDATA();
            }
            return fAttValidatorCDATA;
        }
        if (attType == fIDSymbol) {
            if (fAttValidatorID == null) {
                fAttValidatorID = new AttValidatorID();
            }
            return fAttValidatorID;
        }
        if (attType == fIDREFSymbol) {
            if (fAttValidatorIDREF == null) {
                fAttValidatorIDREF = new AttValidatorIDREF();
            }
            return fAttValidatorIDREF;
        }
        if (attType == fIDREFSSymbol) {
            if (fAttValidatorIDREFS == null) {
                fAttValidatorIDREFS = new AttValidatorIDREFS();
            }
            return fAttValidatorIDREFS;
        }
        if (attType == fENTITYSymbol) {
            if (fAttValidatorENTITY == null) {
                fAttValidatorENTITY = new AttValidatorENTITY();
            }
            return fAttValidatorENTITY;
        }
        if (attType == fENTITIESSymbol) {
            if (fAttValidatorENTITIES == null) {
                fAttValidatorENTITIES = new AttValidatorENTITIES();
            }
            return fAttValidatorENTITIES;
        }
        if (attType == fNMTOKENSymbol) {
            if (fAttValidatorNMTOKEN == null) {
                fAttValidatorNMTOKEN = new AttValidatorNMTOKEN();
            }
            return fAttValidatorNMTOKEN;
        }
        if (attType == fNMTOKENSSymbol) {
            if (fAttValidatorNMTOKENS == null) {
                fAttValidatorNMTOKENS = new AttValidatorNMTOKENS();
            }
            return fAttValidatorNMTOKENS;
        }
        if (attType == fNOTATIONSymbol) {
            if (fAttValidatorNOTATION == null) {
                fAttValidatorNOTATION = new AttValidatorNOTATION();
            }
            return fAttValidatorNOTATION;
        }
        if (attType == fENUMERATIONSymbol) {
            if (fAttValidatorENUMERATION == null) {
                fAttValidatorENUMERATION = new AttValidatorENUMERATION();
            }
            return fAttValidatorENUMERATION;
        }
        if (attType == fDATATYPESymbol) {
            if (fAttValidatorDATATYPE == null) {
                fAttValidatorDATATYPE = fSchemaImporter.createDatatypeAttributeValidator();
            }
            return fAttValidatorDATATYPE;
        }
        throw new RuntimeException("getValidatorForAttType(" + fStringPool.toString(attType) + ")");
    }

    /** Returns an attribute definition for an element type. */
    private int getAttDef(QName element, QName attribute) {

        int elementIndex = getDeclaration(element);
        if (elementIndex == -1) {
            return -1;
        }
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        int attDefIndex = fAttlistHead[chunk][index];
        while (attDefIndex != -1) {
            chunk = attDefIndex >> CHUNK_SHIFT;
            index = attDefIndex & CHUNK_MASK;
            // REVISIT: Validation. This should be the tuple.
            if (fAttName[chunk][index] == attribute.rawname || 
                fStringPool.equalNames(fAttName[chunk][index], attribute.rawname)) {
            //if (fAttQName[chunk][index].localpart == attribute.localpart ||
                // fAttQName[chunk][index].uri == attribute.uri)  {
                return attDefIndex;
            }
            attDefIndex = fNextAttDef[chunk][index];
        }
        return -1;

    } // getAttDef(QName,QName)

    /** Returns an attribute's name from its definition index. */
    private int getAttName(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttName[chunk][index];
    }

    /** Returns an attribute's value from its definition index. */
    private int getAttValue(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttValue[chunk][index];
    }

    /** Returns a validator from its definition index. */
    private AttributeValidator getAttributeValidator(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttValidator[chunk][index];
    }

    /** Returns an attribute's type from its definition index. */
    private int getAttType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttType[chunk][index];
    }

    /** Returns an attribute's default type from its definition index. */
    private int getAttDefaultType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttDefaultType[chunk][index];
    }

    /** Returns an attribute's enumeration values from its definition index. */
    private int getEnumeration(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fEnumeration[chunk][index];
    }

    // validation

    /** Root element specified. */
    private void rootElementSpecified(QName rootElement) throws Exception {

        if (fDynamicValidation && !fSeenDoctypeDecl) {
            fValidating = false;
        }
        if (fValidating) {
            if (fRootElement.rawname != -1) {
                String root1 = fStringPool.toString(fRootElement.rawname);
                String root2 = fStringPool.toString(rootElement.rawname);
                if (!root1.equals(root2)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ROOT_ELEMENT_TYPE,
                                              XMLMessages.VC_ROOT_ELEMENT_TYPE,
                                              fRootElement.rawname, 
                                              rootElement.rawname);
                }
            }
        }
        if (fNamespacesEnabled) {
            if (fNamespacesScope == null) {
                fNamespacesScope = new NamespacesScope(this);
                fNamespacesPrefix = fStringPool.addSymbol("xmlns");
                fNamespacesScope.setNamespaceForPrefix(fNamespacesPrefix, -1);
                int xmlSymbol = fStringPool.addSymbol("xml");
                int xmlNamespace = fStringPool.addSymbol("http://www.w3.org/XML/1998/namespace");
                fNamespacesScope.setNamespaceForPrefix(xmlSymbol, xmlNamespace);
            }
        }

    } // rootElementSpecified(QName)

    /** Switchs to correct validating symbol tables when Schema changes.*/

    private void switchSchema (int schemaURI) {
        Grammar newSchema = fGrammarPool.getGrammar(fStringPool.toString(schemaURI));
        if ( newSchema == null ) {
            //REVISIT: should try to read the schema again here.
        }
        else {
            //REVISIT: copy all the reference the validating tables from the grammar to all the tables in hand.
            //         Don't forget to copy all the counters as well.
        }
    }
    

    /** Binds namespaces to the element and attributes. */
    private void bindNamespacesToElementAndAttributes(QName element, 
                                                      XMLAttrList attrList)
        throws Exception {

        fNamespacesScope.increaseDepth();
        int prefix = element.prefix;
        if (fAttrListHandle != -1) {
            int index = attrList.getFirstAttr(fAttrListHandle);
            while (index != -1) {
                int attName = attrList.getAttrName(index);
                int attPrefix = attrList.getAttrPrefix(index);
                if (fStringPool.equalNames(attName, fXMLLang)) {
                    /***
                    // NOTE: This check is done in the validateElementsAndAttributes
                    //       method.
                    fDocumentScanner.checkXMLLangAttributeValue(attrList.getAttValue(index));
                    /***/
                } 
                else if (fStringPool.equalNames(attName, fNamespacesPrefix)) {
                    int uri = fStringPool.addSymbol(attrList.getAttValue(index));
                    fNamespacesScope.setNamespaceForPrefix(StringPool.EMPTY_STRING, uri);
                } 
                else {
                    if (attPrefix == fNamespacesPrefix) {
                        attPrefix = attrList.getAttrLocalpart(index);
                        int uri = fStringPool.addSymbol(attrList.getAttValue(index));
                        fNamespacesScope.setNamespaceForPrefix(attPrefix, uri);
                    }
                }
                index = attrList.getNextAttr(index);
            }
        }
        int elementURI;
        if (prefix == -1) {
            elementURI = fNamespacesScope.getNamespaceForPrefix(StringPool.EMPTY_STRING);
            if (elementURI != -1) {
                element.uri = elementURI;
            }
        } 
        else {
            elementURI = fNamespacesScope.getNamespaceForPrefix(prefix);
            if (elementURI == -1) {
                Object[] args = { fStringPool.toString(prefix) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XMLNS_DOMAIN,
                                           XMLMessages.MSG_PREFIX_DECLARED,
                                           XMLMessages.NC_PREFIX_DECLARED,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
            element.uri = elementURI;
        }

        //REVISIT: is this the right place to check on if the Schema has changed?
        if (element.uri != fCurrentSchemaURI) {
            fCurrentSchemaURI = element.uri;
            switchSchema(fCurrentSchemaURI);
        }

        if (fAttrListHandle != -1) {
            int index = attrList.getFirstAttr(fAttrListHandle);
            while (index != -1) {
                int attName = attrList.getAttrName(index);
                if (!fStringPool.equalNames(attName, fNamespacesPrefix)) {
                    int attPrefix = attrList.getAttrPrefix(index);
                    if (attPrefix != fNamespacesPrefix) {
                        if (attPrefix != -1) {
                            int uri = fNamespacesScope.getNamespaceForPrefix(attPrefix);
                            if (uri == -1) {
                                Object[] args = { fStringPool.toString(attPrefix) };
                                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                           XMLMessages.XMLNS_DOMAIN,
                                                           XMLMessages.MSG_PREFIX_DECLARED,
                                                           XMLMessages.NC_PREFIX_DECLARED,
                                                           args,
                                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                            }
                            attrList.setAttrURI(index, uri);
                        }
                    }
                }
                index = attrList.getNextAttr(index);
            }
        }

    } // bindNamespacesToElementAndAttributes(QName,XMLAttrList)

        /** Validates element and attributes. */
    private void validateElementAndAttributes(QName element, 
                                              XMLAttrList attrList) 
        throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.validateElementAndAttributes: " + param("elementType",element.rawname) + " !!!!\n");
                //****DEBUG****

        if (fElementDeclCount == 0 && fAttDefCount == 0 && 
            !fValidating && !fNamespacesEnabled) {
            fCurrentElementIndex = -1;
            fCurrentContentSpecType = -1;
            fInElementContent = false;
            if (fAttrListHandle != -1) {
                fAttrList.endAttrList();
                int index = fAttrList.getFirstAttr(fAttrListHandle);
                while (index != -1) {
                    if (fStringPool.equalNames(fAttrList.getAttrName(index), fXMLLang)) {
                        fDocumentScanner.checkXMLLangAttributeValue(fAttrList.getAttValue(index));
                        break;
                    }
                    index = fAttrList.getNextAttr(index);
                }
            }
            return;
        }
        // REVISIT: Validation
        int elementIndex = getDeclaration(element);
        int contentSpecType = (elementIndex == -1) ? -1 : getContentSpecType(elementIndex);
        if (contentSpecType == -1 && fValidating) {
            reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_NOT_DECLARED,
                                      XMLMessages.VC_ELEMENT_VALID,
                                      element.rawname);
        }
        if (fAttDefCount != 0 && elementIndex != -1) {
            fAttrListHandle = addDefaultAttributes(elementIndex, attrList, fAttrListHandle, fValidating, fStandaloneReader != -1);
        }
        if (fAttrListHandle != -1) {
            fAttrList.endAttrList();
        }

        if (DEBUG_PRINT_ATTRIBUTES) {
            String elementStr = fStringPool.toString(element.rawname);
            System.out.print("startElement: <" + elementStr);
            if (fAttrListHandle != -1) {
                int index = attrList.getFirstAttr(fAttrListHandle);
                while (index != -1) {
                    System.out.print(" " + fStringPool.toString(attrList.getAttrName(index)) + "=\"" +
                            fStringPool.toString(attrList.getAttValue(index)) + "\"");
                    index = attrList.getNextAttr(index);
                }
            }
            System.out.println(">");
        }
        // REVISIT: Validation. Do we need to recheck for the xml:lang
        //          attribute? It was already checked above -- perhaps
        //          this is to check values that are defaulted in? If
        //          so, this check could move to the attribute decl
        //          callback so we can check the default value before
        //          it is used.
        if (fAttrListHandle != -1) {
            int index = fAttrList.getFirstAttr(fAttrListHandle);
            while (index != -1) {
                if (fStringPool.equalNames(attrList.getAttrName(index), fXMLLang)) {
                    fDocumentScanner.checkXMLLangAttributeValue(attrList.getAttValue(index));
                    break;
                }
                index = fAttrList.getNextAttr(index);
            }
        }
        fCurrentElementIndex = elementIndex;
        fCurrentContentSpecType = contentSpecType;
        if (fValidating && contentSpecType == fDATATYPESymbol) {
            fBufferDatatype = true;
            fDatatypeBuffer.setLength(0);
        }
        fInElementContent = (contentSpecType == fCHILDRENSymbol);

    } // validateElementAndAttributes(QName,XMLAttrList)

    /** Character data in content. */
    private void charDataInContent() {

                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.charDataInContent\n");
                //****DEBUG****

        int[] children = fElementChildren[fElementDepth];
        int childCount = fElementChildCount[fElementDepth];
        try {
            children[childCount] = -1;
        } 
        catch (NullPointerException ex) {
            children = fElementChildren[fElementDepth] = new int[256];
            childCount = 0; // should really assert this...
            children[childCount] = -1;
        } 
        catch (ArrayIndexOutOfBoundsException ex) {
            int[] newChildren = new int[childCount * 2];
            System.arraycopy(children, 0, newChildren, 0, childCount);
            children = fElementChildren[fElementDepth] = newChildren;
            children[childCount] = -1;
        }
        fElementChildCount[fElementDepth] = ++childCount;

    } // charDataInCount()

    /** Peek child count. */
    private int peekChildCount() {

                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.peekChildCount\n");
                //****DEBUG****

        return fElementChildCount[fElementDepth];
    }

    /** Peek children. */
    private int[] peekChildren() {

                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.peekChildren\n");
                //****DEBUG****

        return fElementChildren[fElementDepth];
    }

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
     * @param children The children of this element.  Each integer is an index within
     *                 the <code>StringPool</code> of the child element name.  An index
     *                 of -1 is used to indicate an occurrence of non-whitespace character
     *                 data.
     *
     * @return The value -1 if fully valid, else the 0 based index of the child
     *         that first failed. If the value returned is equal to the number
     *         of children, then additional content is required to reach a valid
     *         ending state.
     *
     * @exception Exception Thrown on error.
     */
    private int checkContent(int elementIndex, 
                             int childCount, int[] children) throws Exception {

                //****DEBUG****
                if (DEBUG) print("(VAL) XMLValidator.checkContent: " + param("elementIndex",elementIndex) + "... \n");
                //****DEBUG****

        // Get the element name index from the element
        // REVISIT: Validation
        final int elementType = fCurrentElement.rawname;

        if (DEBUG_PRINT_CONTENT) {
            String strTmp = fStringPool.toString(elementType);
            System.out.println("Name: "+strTmp+", "+
                               "Count: "+childCount+", "+
                               "ContentSpec: "+getContentSpecAsString(elementIndex));
            for (int index = 0; index < childCount && index < 10; index++) {
                if (index == 0) {
                    System.out.print("  (");
                }
                String childName = (children[index] == -1) ? "#PCDATA" : fStringPool.toString(children[index]);
                if (index + 1 == childCount) {
                    System.out.println(childName + ")");
                }
                else if (index + 1 == 10) {
                    System.out.println(childName + ",...)");
                }
                else {
                    System.out.print(childName + ",");
                }
            }
        }

        // Get out the content spec for this element
        final int contentSpec = fCurrentContentSpecType;

        //
        //  Deal with the possible types of content. We try to optimized here
        //  by dealing specially with content models that don't require the
        //  full DFA treatment.
        //
        if (contentSpec == fEMPTYSymbol) {
            //
            //  If the child count is greater than zero, then this is
            //  an error right off the bat at index 0.
            //
            if (childCount != 0) {
                return 0;
            }
        }
        else if (contentSpec == fANYSymbol) {
            //
            //  This one is open game so we don't pass any judgement on it
            //  at all. Its assumed to fine since it can hold anything.
            //
        }
        else if (contentSpec == fMIXEDSymbol ||  contentSpec == fCHILDRENSymbol) {
            // Get the content model for this element, faulting it in if needed
            XMLContentModel cmElem = null;
            try {
                cmElem = getContentModel(elementIndex);
                return cmElem.validateContent(childCount, children);
            }
            catch(CMException excToCatch) {
                // REVISIT - Translate the caught exception to the protected error API
                int majorCode = excToCatch.getErrorCode();
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                           majorCode,
                                           0,
                                           null,
                                           XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
            }
        }
        else if (contentSpec == -1) {
            reportRecoverableXMLError(XMLMessages.MSG_ELEMENT_NOT_DECLARED,
                                      XMLMessages.VC_ELEMENT_VALID,
                                      elementType);
        }
        else if (contentSpec == fStringPool.addSymbol("DATATYPE")) {

            XMLContentModel cmElem = null;
            try {
                cmElem = getContentModel(elementIndex);
                return cmElem.validateContent(1, new int[] { fStringPool.addString(fDatatypeBuffer.toString()) });
            } 
            catch (CMException cme) {
                System.out.println("Internal Error in datatype validation");
            } 
            catch (InvalidDatatypeValueException idve) {
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           SchemaMessageProvider.SCHEMA_DOMAIN,
                                           SchemaMessageProvider.DatatypeError,
                                           SchemaMessageProvider.MSG_NONE,
                                           new Object [] { idve.getMessage() },
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
            /*
            boolean DEBUG_DATATYPES = false;
            if (DEBUG_DATATYPES) {
                System.out.println("Checking content of datatype");
                String strTmp = fStringPool.toString(elementTypeIndex);
                int contentSpecIndex = fElementDeclPool.getContentSpec(elementIndex);
                XMLContentSpec csn = new XMLContentSpec();
                fElementDeclPool.getContentSpecNode(contentSpecIndex, csn);
                String contentSpecString = fStringPool.toString(csn.value);
                System.out.println
                (
                    "Name: "
                    + strTmp
                    + ", Count: "
                    + childCount
                    + ", ContentSpec: "
                    + contentSpecString
                );
                for (int index = 0; index < childCount && index < 10; index++) {
                    if (index == 0) System.out.print("  (");
                    String childName = (children[index] == -1) ? "#PCDATA" : fStringPool.toString(children[index]);
                    if (index + 1 == childCount)
                        System.out.println(childName + ")");
                    else if (index + 1 == 10)
                        System.out.println(childName + ",...)");
                    else
                        System.out.print(childName + ",");
                }
            }
            try { // REVISIT - integrate w/ error handling
                int contentSpecIndex = fElementDeclPool.getContentSpec(elementIndex);
                XMLContentSpec csn = new XMLContentSpec();
                fElementDeclPool.getContentSpecNode(contentSpecIndex, csn);
                String type = fStringPool.toString(csn.value);
                DatatypeValidator v = fDatatypeRegistry.getValidatorFor(type);
                if (v != null)
                    v.validate(fDatatypeBuffer.toString());
                else
                    System.out.println("No validator for datatype "+type);
            } catch (InvalidDatatypeValueException idve) {
                System.out.println("Incorrect datatype: "+idve.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Internal error in datatype validation");
            }
            */
        }
        else {
            fErrorReporter.reportError(fErrorReporter.getLocator(),
                                       ImplementationMessages.XERCES_IMPLEMENTATION_DOMAIN,
                                       ImplementationMessages.VAL_CST,
                                       0,
                                       null,
                                       XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
        }

        // We succeeded
        return -1;

    } // checkContent(int,int,int[]):int

    /**
     * Check that all ID references were to ID attributes present in the document.
     * <p>
     * This method is a convenience call that allows the validator to do any id ref
     * checks above and beyond those done by the scanner. The scanner does the checks
     * specificied in the XML spec, i.e. that ID refs refer to ids which were
     * eventually defined somewhere in the document.
     * <p>
     * If the validator is for a Schema perhaps, which defines id semantics beyond
     * those of the XML specificiation, this is where that extra checking would be
     * done. For most validators, this is a no-op.
     *
     * @exception Exception Thrown on error.
     */
    private void checkIdRefs() throws Exception {

                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.checkIdRefs\n");
                //****DEBUG****

        if (fIdRefs == null)
            return;
        Enumeration en = fIdRefs.keys();
        while (en.hasMoreElements()) {
            Integer key = (Integer)en.nextElement();
            if (fIdDefs == null || !fIdDefs.containsKey(key)) {
                Object[] args = { fStringPool.toString(key.intValue()) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ELEMENT_WITH_ID_REQUIRED,
                                           XMLMessages.VC_IDREF,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }

    } // checkIdRefs()

    /** 
     * Checks that all declared elements refer to declared elements
     * in their content models. This method calls out to the error
     * handler to indicate warnings.
     */
    private void checkDeclaredElements() throws Exception {

                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.checkDeclaredElements\n");
                //****DEBUG****

        for (int i = 0; i < fElementCount; i++) {
            int type = getContentSpecType(i);
            if (type == fMIXEDSymbol || type == fCHILDRENSymbol) {
                int chunk = i >> CHUNK_SHIFT;
                int index = i &  CHUNK_MASK;
                int contentSpecIndex = fContentSpec[chunk][index];
                checkDeclaredElements(i, contentSpecIndex);
            }
        }

    } // checkUndeclaredElements()

    /** 
     * Does a recursive (if necessary) check on the specified element's
     * content spec to make sure that all children refer to declared
     * elements.
     * <p>
     * This method assumes that it will only be called when there is
     * a validation handler.
     */
    private void checkDeclaredElements(int elementIndex, 
                                       int contentSpecIndex) throws Exception {
        
                //****DEBUG****
                if (DEBUG) print("(???) XMLValidator.checkDeclaredElements\n");
                //****DEBUG****

        // get spec type and value
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex &  CHUNK_MASK;
        int type  = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];

        // temp vars
        // REVISIT: Validation. Use tuples.
        QName qname = new QName(-1, type, type);

        // handle type
        switch (type) {
        
            // #PCDATA | element
            case XMLContentSpec.CONTENTSPECNODE_LEAF: {
                // perform check for declared element
                if (value != -1 && getDeclaration(qname) == -1) {
                    int elemChunk = elementIndex >> CHUNK_SHIFT;
                    int elemIndex = elementIndex &  CHUNK_MASK;
                    int elementType = fElementType[elemChunk][elemIndex];
                    Object[] args = { fStringPool.toString(elementType),
                                      fStringPool.toString(value) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               XMLMessages.P45_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                break;
            }

            // (...)? | (...)* | (...)+
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE:
            case XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE: 
            case XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE: {
                checkDeclaredElements(elementIndex, value);
                break;
            }

            // (... , ...) | (... | ...)
            case XMLContentSpec.CONTENTSPECNODE_CHOICE:
            case XMLContentSpec.CONTENTSPECNODE_SEQ: {
                checkDeclaredElements(elementIndex, value);
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    index = 0;
                }
                checkDeclaredElements(elementIndex, fNodeValue[chunk][index]);
                break;
            }
        }
    }

    // debugging

    /** Returns the string value for a string pool index. */
        private String nameOf(int stringPoolIndex) {
                return fStringPool.toString(stringPoolIndex);
        }

    /** Returns a contatenated name and its associated string pool value. */
        private String param(String name, int stringPoolIndex) {
                return name + "=\"" + fStringPool.toString(stringPoolIndex) + "\" ";
        }

    /** Prints a message to standard error. */
        private void print(String message) {
                if (this == schemaValidator ) {
                        System.err.println(message);
                }
        }
        
    //
    // Interfaces
    //

    /**
     * AttributeValidator.
     */
    public interface AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValue, int attType, int enumHandle) 
        throws Exception;

    } // interface AttributeValidator

    //
    // Classes
    //

    /**
     * Content spec implementation. 
     */
    final class ContentSpecImpl 
        implements XMLContentSpec {

        //
        // Data
        //

        /** String pool. */
        protected StringPool fStringPool;

        /** Handle. */
        protected int fHandle;

        /** Type. */
        protected int fType;

        //
        // Public methods
        //

        /** Returns the handle. */
        public int getHandle() {
            return fHandle;
        }

        /** Returns the type. */
        public int getType() {
            return fType;
        }

        /** 
         * Fills in the XMLContentSpec with the information
         * associated with the specified handle. 
         */
        public void getNode(int handle, XMLContentSpec node) {
            getContentSpecNode(handle, node);
        }

        //
        // Object methods
        //

        /** Returns a string representation of the object. */
        public String toString() {
            if (fType == fMIXEDSymbol || fType == fCHILDRENSymbol)
                return contentSpecNodeAsString(fHandle);
            else
                return fStringPool.toString(fType);
        }

    } // class ContentSpecImpl

        /**
         * AttValidatorCDATA.
     */
    final class AttValidatorCDATA 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            // Normalize attribute based upon attribute type...
            return attValueHandle;
        }

    } // class AttValidatorCDATA

        /**
     *  AttValidatorID.
     */
    final class AttValidatorID 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validName(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ID_INVALID,
                                              XMLMessages.VC_ID,
                                              fStringPool.toString(attribute.rawname), newAttValue);
                }
                //
                // ID - check that the id value is unique within the document (V_TAG8)
                //
                if (element.rawname != -1 && !addId(attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ID_NOT_UNIQUE,
                                              XMLMessages.VC_ID,
                                              fStringPool.toString(attribute.rawname), newAttValue);
                }
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalong attribute definition. */
        
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorID

    /**
     * AttValidatorIDREF.
     */
    final class AttValidatorIDREF 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validName(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_IDREF_INVALID,
                                              XMLMessages.VC_IDREF,
                                              fStringPool.toString(attribute.rawname), newAttValue);
                }
                //
                // IDREF - remember the id value
                //
                if (element.rawname != -1)
                    addIdRef(attValueHandle);
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorIDREF

    /**
     * AttValidatorIDREFS.
     */
    final class AttValidatorIDREFS 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String idName = tokenizer.nextToken();
                    if (fValidating) {
                        if (!XMLCharacterProperties.validName(idName)) {
                            ok = false;
                        }
                        //
                        // IDREFS - remember the id values
                        //
                        if (element.rawname != -1) {
                            addIdRef(fStringPool.addSymbol(idName));
                        }
                    }
                    sb.append(idName);
                    if (!tokenizer.hasMoreTokens())
                        break;
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_IDREFS_INVALID,
                                          XMLMessages.VC_IDREF,
                                          fStringPool.toString(attribute.rawname), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(element, attribute)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attribute.rawname), attValue, newAttValue);
                }
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorIDREFS
        
    /**
     * AttValidatorENTITY.
     */
    final class AttValidatorENTITY 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // ENTITY - check that the value is an unparsed entity name (V_TAGa)
                //
                if (!fEntityHandler.isUnparsedEntity(attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ENTITY_INVALID,
                                              XMLMessages.VC_ENTITY_NAME,
                                              fStringPool.toString(attribute.rawname), newAttValue);
                }
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorENTITY

    /**
     * AttValidatorENTITIES.
     */
    final class AttValidatorENTITIES 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String entityName = tokenizer.nextToken();
                    //
                    // ENTITIES - check that each value is an unparsed entity name (V_TAGa)
                    //
                    if (fValidating && !fEntityHandler.isUnparsedEntity(fStringPool.addSymbol(entityName))) {
                        ok = false;
                    }
                    sb.append(entityName);
                    if (!tokenizer.hasMoreTokens()) {
                        break;
                    }
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_ENTITIES_INVALID,
                                          XMLMessages.VC_ENTITY_NAME,
                                          fStringPool.toString(attribute.rawname), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(element, attribute)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attribute.rawname), attValue, newAttValue);
                }
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorENTITIES

    /**
     * AttValidatorNMTOKEN.
     */
    final class AttValidatorNMTOKEN 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                if (!XMLCharacterProperties.validNmtoken(newAttValue)) {
                    reportRecoverableXMLError(XMLMessages.MSG_NMTOKEN_INVALID,
                                              XMLMessages.VC_NAME_TOKEN,
                                              fStringPool.toString(attribute.rawname), newAttValue);
                }
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorNMTOKEN

    /**
     * AttValidatorNMTOKENS.
     */
    final class AttValidatorNMTOKENS 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            StringTokenizer tokenizer = new StringTokenizer(attValue);
            StringBuffer sb = new StringBuffer(attValue.length());
            boolean ok = true;
            if (tokenizer.hasMoreTokens()) {
                while (true) {
                    String nmtoken = tokenizer.nextToken();
                    if (fValidating && !XMLCharacterProperties.validNmtoken(nmtoken)) {
                        ok = false;
                    }
                    sb.append(nmtoken);
                    if (!tokenizer.hasMoreTokens()) {
                        break;
                    }
                    sb.append(' ');
                }
            }
            String newAttValue = sb.toString();
            if (fValidating && (!ok || newAttValue.length() == 0)) {
                reportRecoverableXMLError(XMLMessages.MSG_NMTOKENS_INVALID,
                                          XMLMessages.VC_NAME_TOKEN,
                                          fStringPool.toString(attribute.rawname), newAttValue);
            }
            if (!newAttValue.equals(attValue)) {
                attValueHandle = fStringPool.addString(newAttValue);
                if (fValidating && invalidStandaloneAttDef(element, attribute)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                              XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                              fStringPool.toString(attribute.rawname), attValue, newAttValue);
                }
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorNMTOKENS

    /**
     * AttValidatorNOTATION.
     */
    final class AttValidatorNOTATION 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // NOTATION - check that the value is in the AttDef enumeration (V_TAGo)
                //
                if (!fStringPool.stringInList(enumHandle, attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_NOT_IN_LIST,
                                              XMLMessages.VC_NOTATION_ATTRIBUTES,
                                              fStringPool.toString(attribute.rawname),
                                              newAttValue, fStringPool.stringListAsString(enumHandle));
                }
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorNOTATION

    /**
     * AttValidatorENUMERATION.
     */
    final class AttValidatorENUMERATION 
        implements AttributeValidator {

        //
        // AttributeValidator methods
        //

        /** Normalize. */
        public int normalize(QName element, QName attribute, 
                             int attValueHandle, int attType, 
                             int enumHandle) throws Exception {
            //
            // Normalize attribute based upon attribute type...
            //
            String attValue = fStringPool.toString(attValueHandle);
            String newAttValue = attValue.trim();
            if (fValidating) {
                // REVISIT - can we release the old string?
                if (newAttValue != attValue) {
                    if (invalidStandaloneAttDef(element, attribute)) {
                        reportRecoverableXMLError(XMLMessages.MSG_ATTVALUE_CHANGED_DURING_NORMALIZATION_WHEN_STANDALONE,
                                                  XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                  fStringPool.toString(attribute.rawname), attValue, newAttValue);
                    }
                    attValueHandle = fStringPool.addSymbol(newAttValue);
                } 
                else {
                    attValueHandle = fStringPool.addSymbol(attValueHandle);
                }
                //
                // ENUMERATION - check that value is in the AttDef enumeration (V_TAG9)
                //
                if (!fStringPool.stringInList(enumHandle, attValueHandle)) {
                    reportRecoverableXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_NOT_IN_LIST,
                                              XMLMessages.VC_ENUMERATION,
                                              fStringPool.toString(attribute.rawname),
                                              newAttValue, fStringPool.stringListAsString(enumHandle));
                }
            } 
            else if (newAttValue != attValue) {
                // REVISIT - can we release the old string?
                attValueHandle = fStringPool.addSymbol(newAttValue);
            }
            return attValueHandle;

        } // normalize(QName,QName,int,int,int):int

        //
        // Package methods
        //

        /** Returns true if invalid standalone attribute definition. */
        boolean invalidStandaloneAttDef(QName element, QName attribute) {
            if (fStandaloneReader == -1) {
                return false;
            }
            // we are normalizing a default att value...  this ok?
            if (element.rawname == -1) {
                return false;
            }
            return getAttDefIsExternal(element, attribute);
        }

    } // class AttValidatorENUMERATION

} // class XMLValidator
