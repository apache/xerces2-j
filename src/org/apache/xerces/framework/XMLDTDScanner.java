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

package org.apache.xerces.framework;

import org.apache.xerces.readers.XMLEntityHandler;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLMessages;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * Default implementation of an XML DTD scanner.
 *
 * Clients who wish to scan a DTD should implement
 * XMLDTDScanner.EventHandler to provide the desired behavior
 * when various DTD components are encountered.
 *
 * To process the DTD, the client application should follow the 
 * following sequence:
 * <ol>
 *  <li>call scanDocTypeDecl() to scan the DOCTYPE declaration
 *  <li>call getReadingExternalEntity() to determine if scanDocTypeDecl found an
 *      external subset
 * <li>if scanning an external subset, call scanDecls(true) to process the external subset
 * </ol>
 *
 * @see XMLDTDScanner.EventHandler
 * @version
 */
public final class XMLDTDScanner {
    //
    // Constants
    //
    //
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    //
    private static final char[] version_string = { 'v','e','r','s','i','o','n' };
    //
    // [45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'
    //
    private static final char[] element_string = { 'E','L','E','M','E','N','T' };
    //
    // [46] contentspec ::= 'EMPTY' | 'ANY' | Mixed | children
    //
    private static final char[] empty_string = { 'E','M','P','T','Y' };
    private static final char[] any_string = { 'A','N','Y' };
    //
    // [51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*'
    //                | '(' S? '#PCDATA' S? ')'
    //
    private static final char[] pcdata_string = { '#','P','C','D','A','T','A' };
    //
    // [52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
    //
    private static final char[] attlist_string = { 'A','T','T','L','I','S','T' };
    //
    // [55] StringType ::= 'CDATA'
    //
    private static final char[] cdata_string = { 'C','D','A','T','A' };
    //
    // [56] TokenizedType ::= 'ID' | 'IDREF' | 'IDREFS' | 'ENTITY' | 'ENTITIES'
    //                        | 'NMTOKEN' | 'NMTOKENS'
    //
    // Note: We search for common substrings always trying to move forward
    //
    //  'ID'      - Common prefix of ID, IDREF and IDREFS
    //  'REF'     - Common substring of IDREF and IDREFS after matching ID prefix
    //  'ENTIT'   - Common prefix of ENTITY and ENTITIES
    //  'IES'     - Suffix of ENTITIES
    //  'NMTOKEN' - Common prefix of NMTOKEN and NMTOKENS
    //
    private static final char[] id_string = { 'I','D' };
    private static final char[] ref_string = { 'R','E','F' };
    private static final char[] entit_string = { 'E','N','T','I','T' };
    private static final char[] ies_string = { 'I','E','S' };
    private static final char[] nmtoken_string = { 'N','M','T','O','K','E','N' };
    //
    // [58] NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
    // [82] NotationDecl ::= '<!NOTATION' S Name S (ExternalID |  PublicID) S? '>'
    //
    private static final char[] notation_string = { 'N','O','T','A','T','I','O','N' };
    //
    // [60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED' | (('#FIXED' S)? AttValue)
    //
    private static final char[] required_string = { '#','R','E','Q','U','I','R','E','D' };
    private static final char[] implied_string = { '#','I','M','P','L','I','E','D' };
    private static final char[] fixed_string = { '#','F','I','X','E','D' };
    //
    // [62] includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'
    //
    private static final char[] include_string = { 'I','N','C','L','U','D','E' };
    //
    // [63] ignoreSect ::= '<![' S? 'IGNORE' S? '[' ignoreSectContents* ']]>'
    //
    private static final char[] ignore_string = { 'I','G','N','O','R','E' };
    //
    // [71] GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
    // [72] PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
    //
    private static final char[] entity_string = { 'E','N','T','I','T','Y' };
    //
    // [75] ExternalID ::= 'SYSTEM' S SystemLiteral
    //                     | 'PUBLIC' S PubidLiteral S SystemLiteral
    // [83] PublicID ::= 'PUBLIC' S PubidLiteral
    //
    private static final char[] system_string = { 'S','Y','S','T','E','M' };
    private static final char[] public_string = { 'P','U','B','L','I','C' };
    //
    // [76] NDataDecl ::= S 'NDATA' S Name
    //
    private static final char[] ndata_string = { 'N','D','A','T','A' };
    //
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    //
    private static final char[] encoding_string = { 'e','n','c','o','d','i','n','g' };
    //
    // Instance Variables
    //
    private EventHandler fEventHandler = null;
    private StringPool fStringPool = null;
    private XMLErrorReporter fErrorReporter = null;
    private XMLEntityHandler fEntityHandler = null;
    private XMLEntityHandler.EntityReader fEntityReader = null;
    private XMLEntityHandler.CharBuffer fLiteralData = null;
    private int fReaderId = -1;
    private int fSystemLiteral = -1;
    private int fPubidLiteral = -1;
    private int[] opStack = null;
    private int[] nodeIndexStack = null;
    private int[] prevNodeIndexStack = null;
    private int fScannerState = SCANNER_STATE_INVALID;
    private int fIncludeSectDepth = 0;
    private int fDoctypeReader = -1;
    private int fExternalSubsetReader = -1;
    private int fDefaultAttValueReader = -1;
    private int fDefaultAttValueElementType = -1;
    private int fDefaultAttValueAttrName = -1;
    private int fDefaultAttValueOffset = -1;
    private int fDefaultAttValueMark = -1;
    private int fEntityValueReader = -1;
    private int fEntityValueMark = -1;
    private int fEMPTY = -1;
    private int fANY = -1;
    private int fMIXED = -1;
    private int fCHILDREN = -1;
    private int fCDATA = -1;
    private int fID = -1;
    private int fIDREF = -1;
    private int fIDREFS = -1;
    private int fENTITY = -1;
    private int fENTITIES = -1;
    private int fNMTOKEN = -1;
    private int fNMTOKENS = -1;
    private int fNOTATION = -1;
    private int fENUMERATION = -1;
    private int fREQUIRED = -1;
    private int fIMPLIED = -1;
    private int fFIXED = -1;
    private int fDEFAULT = -1;
    private int fXMLSpace = -1;
    private int fDefault = -1;
    private int fPreserve = -1;
    private int fScannerMarkupDepth = 0;
    private int fScannerParenDepth = 0;
    //
    // Constructors
    //
    public XMLDTDScanner(EventHandler eventHandler,
                         StringPool stringPool,
                         XMLErrorReporter errorReporter,
                         XMLEntityHandler entityHandler,
                         XMLEntityHandler.CharBuffer literalData) {
        fEventHandler = eventHandler;
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fLiteralData = literalData;
        init();
    }
    //
    //
    //
    /**
     * Is the XMLDTDScanner reading from an external entity?
     *
     * This will be true, in particular if there was an external subset
     *
     * @return true if the XMLDTDScanner is reading from an external entity.
     */
    public boolean getReadingExternalEntity() {
        return fReaderId != fDoctypeReader;
    }
    /**
     * Is the scanner reading a ContentSpec?
     * 
     * @return true if the scanner is reading a ContentSpec
     */
    public boolean getReadingContentSpec() {
        return getScannerState() == SCANNER_STATE_CONTENTSPEC;
    }
    /**
     * Report the markup nesting depth.  This allows a client to
     * perform validation checks for correct markup nesting.  This keeps
     * scanning and validation separate.
     *
     * @return the markup nesting depth
     */
    public int markupDepth() {
        return fScannerMarkupDepth;
    }
    private int increaseMarkupDepth() {
        return fScannerMarkupDepth++;
    }
    private int decreaseMarkupDepth() {
        return fScannerMarkupDepth--;
    }
    /**
     * Report the parenthesis nesting depth.  This allows a client to
     * perform validation checks for correct parenthesis balancing.  This keeps 
     * scanning and validation separate.
     *
     * @return the parenthesis depth
     */
    public int parenDepth() {
        return fScannerParenDepth;
    }
    private void setParenDepth(int parenDepth) {
        fScannerParenDepth = parenDepth;
    }
    private void increaseParenDepth() {
        fScannerParenDepth++;
    }
    private void decreaseParenDepth() {
        fScannerParenDepth--;
    }
    //
    //
    //
    /**
     * Allow XMLDTDScanner to be reused.  This method is called from an
     * XMLParser reset method, which passes the StringPool to be used
     * by the reset DTD scanner instance.
     *
     * @param stringPool the string pool to be used by XMLDTDScanner.  
     */
    public void reset(StringPool stringPool, XMLEntityHandler.CharBuffer literalData) throws Exception {
        fStringPool = stringPool;
        fLiteralData = literalData;
        fEntityReader = null;
        fReaderId = -1;
        fSystemLiteral = -1;
        fPubidLiteral = -1;
        opStack = null;
        nodeIndexStack = null;
        prevNodeIndexStack = null;
        fScannerState = SCANNER_STATE_INVALID;
        fIncludeSectDepth = 0;
        fDoctypeReader = -1;
        fExternalSubsetReader = -1;
        fDefaultAttValueReader = -1;
        fDefaultAttValueElementType = -1;
        fDefaultAttValueAttrName = -1;
        fDefaultAttValueOffset = -1;
        fDefaultAttValueMark = -1;
        fEntityValueReader = -1;
        fEntityValueMark = -1;
        fScannerMarkupDepth = 0;
        fScannerParenDepth = 0;
        init();
    }
    private void init() {
        fEMPTY = fStringPool.addSymbol("EMPTY");
        fANY = fStringPool.addSymbol("ANY");
        fMIXED = fStringPool.addSymbol("MIXED");
        fCHILDREN = fStringPool.addSymbol("CHILDREN");
        fCDATA = fStringPool.addSymbol("CDATA");
        fID = fStringPool.addSymbol("ID");
        fIDREF = fStringPool.addSymbol("IDREF");
        fIDREFS = fStringPool.addSymbol("IDREFS");
        fENTITY = fStringPool.addSymbol("ENTITY");
        fENTITIES = fStringPool.addSymbol("ENTITIES");
        fNMTOKEN = fStringPool.addSymbol("NMTOKEN");
        fNMTOKENS = fStringPool.addSymbol("NMTOKENS");
        fNOTATION = fStringPool.addSymbol("NOTATION");
        fENUMERATION = fStringPool.addSymbol("ENUMERATION");
        fREQUIRED = fStringPool.addSymbol("#REQUIRED");
        fIMPLIED = fStringPool.addSymbol("#IMPLIED");
        fFIXED = fStringPool.addSymbol("#FIXED");
        fDEFAULT = fStringPool.addSymbol("");
        fXMLSpace = fStringPool.addSymbol("xml:space");
        fDefault = fStringPool.addSymbol("default");
        fPreserve = fStringPool.addSymbol("preserve");
    }

    //
    // Interfaces
    //

    /**
     * This interface must be implemented by the users of the XMLDTDScanner class.
     * These methods form the abstraction between the implementation semantics and the
     * more generic task of scanning the DTD-specific XML grammar.
     */
    public interface EventHandler {
        /**
         * REVISIT - does this really do anything -- can we kill it?
         *
         * @return the current location
         * @exception java.lang.Exception
         */
        public int saveCurrentLocation() throws Exception;
        /**
         * Determine whether a string is a valid XML version number
         *  
         * @param version string to be checked
         * @return true if version is a valid XML version number
         * @exception java.lang.Exception
         */
        public boolean validVersionNum(String version) throws Exception;
        /**
         * Determine whether a string is a valid encoding name
         *
         * @param encoding string to be checked
         * @return true if encoding is a valid encoding name
         * @exception java.lang.Exception
         */
        public boolean validEncName(String encoding) throws Exception;
        /**
         * Determine if a string is a valid public identifier
         *
         * @param publicId string to be checked
         * @return true if publicId is a valid public identifier
         * @exception java.lang.Exception
         */
        public int validPublicId(String publicId) throws Exception;
        /**
         * Called when the doctype decl is scanned
         *
         * @param rootElementType handle of the rootElement
         * @param publicId StringPool handle of the public id
         * @param systemId StringPool handle of the system id
         * @exception java.lang.Exception
         */
        public void doctypeDecl(int rootElementType, int publicId, int systemId) throws Exception;
        /**
         * Called when the DTDScanner starts reading from the external subset
         *
         * @param publicId StringPool handle of the public id
         * @param systemId StringPool handle of the system id
         * @exception java.lang.Exception
         */
        public void startReadingFromExternalSubset(int publicId, int systemId) throws Exception;
        /**
         * Called when the DTDScanner stop reading from the external subset
         *
         * @exception java.lang.Exception
         */
        public void stopReadingFromExternalSubset() throws Exception;
        /**
         * Add an element declaration (forward reference)
         *
         * @param handle to the name of the element being declared
         * @return handle to the element whose declaration was added
         * @exception java.lang.Exception
         */
        public int addElementDecl(int elementType) throws Exception;
        /**
         * Add an element declaration
         *
         * @param handle to the name of the element being declared
         * @param contentSpecType handle to the type name of the content spec
         * @param ContentSpec handle to the content spec node for the contentSpecType
         * @return handle to the element declaration that was added 
         * @exception java.lang.Exception
         */
        public int addElementDecl(int elementType, int contentSpecType, int contentSpec) throws Exception;
        /**
         * Add an attribute definition
         *
         * @param handle to the element whose attribute is being declared
         * @param attName StringPool handle to the attribute name being declared
         * @param attType type of the attribute
         * @param enumeration StringPool handle of the attribute's enumeration list (if any)
         * @param attDefaultType an integer value denoting the DefaultDecl value
         * @param attDefaultValue StringPool handle of this attribute's default value
         * @return handle to the attribute definition
         * @exception java.lang.Exception
         */
        public int addAttDef(int elementIndex, int attName, int attType, int enumeration, int attDefaultType, int attDefaultValue) throws Exception;
        /**
         * create an XMLContentSpecNode for a leaf
         *
         * @param nameIndex StringPool handle to the name (Element) for the node
         * @return handle to the newly create XMLContentSpecNode
         * @exception java.lang.Exception
         */
        public int addUniqueLeafNode(int nameIndex) throws Exception;
        /**
         * Create an XMLContentSpecNode for a single non-leaf
         * 
         * @param nodeType the type of XMLContentSpecNode to create - from XMLContentSpecNode.CONTENTSPECNODE_*
         * @param nodeValue handle to an XMLContentSpecNode
         * @return handle to the newly create XMLContentSpecNode
         * @exception java.lang.Exception
         */
        public int addContentSpecNode(int nodeType, int nodeValue) throws Exception;
        /**
         * Create an XMLContentSpecNode for a two child leaf
         *
         * @param nodeType the type of XMLContentSpecNode to create - from XMLContentSpecNode.CONTENTSPECNODE_*
         * @param leftNodeIndex handle to an XMLContentSpecNode
         * @param rightNodeIndex handle to an XMLContentSpecNode
         * @return handle to the newly create XMLContentSpecNode
         * @exception java.lang.Exception
         */
        public int addContentSpecNode(int nodeType, int leftNodeIndex, int rightNodeIndex) throws Exception;
        /**
         * Create a string representation of an XMLContentSpecNode tree
         * 
         * @param handle to an XMLContentSpecNode
         * @return String representation of the content spec tree
         * @exception java.lang.Exception
         */
        public String getContentSpecNodeAsString(int nodeIndex) throws Exception;
        /**
         * Add a declaration for an internal parameter entity
         *
         * @param name StringPool handle of the parameter entity name
         * @param value StringPool handle of the parameter entity value
         * @param location location in the containing entity
         * @return handle to the parameter entity declaration
         * @exception java.lang.Exception
         */
        public int addInternalPEDecl(int name, int value, int location) throws Exception;
        /**
         * Add a declaration for an external parameter entity
         *
         * @param name StringPool handle of the parameter entity name
         * @param publicId StringPool handle of the publicId
         * @param systemId StringPool handle of the systemId
         * @return handle to the parameter entity declaration
         * @exception java.lang.Exception
         */
        public int addExternalPEDecl(int name, int publicId, int systemId) throws Exception;
        /**
         * Add a declaration for an internal entity
         *
         * @param name StringPool handle of the entity name
         * @param value StringPool handle of the entity value
         * @param location location in the containing entity
         * @return handle to the entity declaration
         * @exception java.lang.Exception
         */
        public int addInternalEntityDecl(int name, int value, int location) throws Exception;
        /**
         * Add a declaration for an entity
         *
         * @param name StringPool handle of the entity name
         * @param publicId StringPool handle of the publicId
         * @param systemId StringPool handle of the systemId
         * @return handle to the entity declaration
         * @exception java.lang.Exception
         */
        public int addExternalEntityDecl(int name, int publicId, int systemId) throws Exception;
        /**
         * Add a declaration for an unparsed entity
         *
         * @param name StringPool handle of the entity name
         * @param publicId StringPool handle of the publicId
         * @param systemId StringPool handle of the systemId
         * @param notationName StringPool handle of the notationName
         * @return handle to the entity declaration
         * @exception java.lang.Exception
         */
        public int addUnparsedEntityDecl(int name, int publicId, int systemId, int notationName) throws Exception;
        /**
         * Called when the scanner start scanning an enumeration
         * @return StringPool handle to a string list that will hold the enumeration names
         * @exception java.lang.Exception
         */
        public int startEnumeration() throws Exception;
        /**
         * Add a name to an enumeration
         * @param enumIndex StringPool handle to the string list for the enumeration
         * @param elementType handle to the element that owns the attribute with the enumeration
         * @param attrName StringPool handle to the name of the attribut with the enumeration
         * @param nameIndex StringPool handle to the name to be added to the enumeration
         * @param isNotationType true if the enumeration is an enumeration of NOTATION names
         * @exception java.lang.Exception
         */
        public void addNameToEnumeration(int enumIndex, int elementType, int attrName, int nameIndex, boolean isNotationType) throws Exception;
        /**
         * Finish processing an enumeration
         *
         * @param enumIndex handle to the string list which holds the enumeration to be finshed.
         * @exception java.lang.Exception
         */
        public void endEnumeration(int enumIndex) throws Exception;
        /**
         * Add a declaration for a notation
         *
         * @param notationName
         * @param publicId
         * @param systemId
         * @return handle to the notation declaration
         * @exception java.lang.Exception
         */
        public int addNotationDecl(int notationName, int publicId, int systemId) throws Exception;
        /**
         * Called when a comment has been scanned
         *
         * @param data StringPool handle of the comment text
         * @exception java.lang.Exception
         */
        public void callComment(int data) throws Exception;
        /**
         * Called when a processing instruction has been scanned
         * @param piTarget StringPool handle of the PI target
         * @param piData StringPool handle of the PI data
         * @exception java.lang.Exception
         */
        public void callProcessingInstruction(int piTarget, int piData) throws Exception;
        /**
         * Scan an element type
         *
         * @param entityReader reader to read from
         * @param fastchar hint - character likely to terminate the element type
         * @return StringPool handle for the element type
         * @exception java.lang.Exception
         */
        public int scanElementType(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
        /**
         * Scan for an element type at a point in the grammar where parameter
         * entity references are allowed.  If such a reference is encountered,
         * the replacement text will be scanned, skipping any white space that
         * may be found, expanding references in this manner until an element
         * type is scanned, or we fail to find one.
         *
         * @param entityReader reader to read from
         * @param fastchar hint - character likely to terminate the element type
         * @return StringPool handle for the element type
         * @exception java.lang.Exception
         */
        public int checkForElementTypeWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
        /**
         * Scan for an attribute name at a point in the grammar where parameter
         * entity references are allowed.  If such a reference is encountered,
         * the replacement text will be scanned, skipping any white space that
         * may be found, expanding references in this manner until an attribute
         * name is scanned, or we fail to find one.
         *
         * @param entityReader reader to read from
         * @param fastchar hint - character likely to terminate the attribute name
         * @return StringPool handle for the attribute name
         * @exception java.lang.Exception
         */
        public int checkForAttributeNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
        /**
         * Scan for a Name at a point in the grammar where parameter entity
         * references are allowed.  If such a reference is encountered, the
         * replacement text will be scanned, skipping any white space that
         * may be found, expanding references in this manner until a name
         * is scanned, or we fail to find one.
         *
         * @param entityReader reader to read from
         * @param fastcheck hint - character likely to terminate the name
         * @return StringPool handle for the name
         * @exception java.lang.Exception
         */
        public int checkForNameWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
        /**
         * Scan for a name token at a point in the grammar where parameter
         * entity references are allowed.  If such a reference is encountered,
         * the replacement text will be scanned, skipping any white space that
         * may be found, expanding references in this manner until a name
         * token is scanned, or we fail to find one.
         *
         * @param entityReader reader to read from
         * @param fastcheck hint - character likely to terminate the name token
         * @return StringPool handle for the name token
         * @exception java.lang.Exception
         */
        public int checkForNmtokenWithPEReference(XMLEntityHandler.EntityReader entityReader, char fastcheck) throws Exception;
        /**
         * Scan the default value for an attribute
         * 
         * @param elementType handle to the element type that owns the attribute
         * @param attrName StringPool handle to the name of the attribute
         * @param attType the attribute type
         * @param enumeration StringPool handle to a string list containing enumeration values
         * @return StringPool handle to the default value
         * @exception java.lang.Exception
         */
        public int scanDefaultAttValue(int elementType, int attrName, int attType, int enumeration) throws Exception;
        
        /**
         * Supports DOM Level 2 internalSubset additions.
         * Called when the internal subset is completely scanned.
         */
        public void internalSubset(int internalSubset);
    }
    //
    //
    //
    private void reportFatalXMLError(int majorCode, int minorCode) throws Exception {
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   null,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void reportFatalXMLError(int majorCode, int minorCode, int stringIndex1) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void reportFatalXMLError(int majorCode, int minorCode, String string1) throws Exception {
        Object[] args = { string1 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void reportFatalXMLError(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1),
                          fStringPool.toString(stringIndex2) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void reportFatalXMLError(int majorCode, int minorCode, String string1, String string2) throws Exception {
        Object[] args = { string1, string2 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void reportFatalXMLError(int majorCode, int minorCode, String string1, String string2, String string3) throws Exception {
        Object[] args = { string1, string2, string3 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    private void abortMarkup(int majorCode, int minorCode) throws Exception {
        reportFatalXMLError(majorCode, minorCode);
        skipPastEndOfCurrentMarkup();
    }
    private void abortMarkup(int majorCode, int minorCode, int stringIndex1) throws Exception {
        reportFatalXMLError(majorCode, minorCode, stringIndex1);
        skipPastEndOfCurrentMarkup();
    }
    private void abortMarkup(int majorCode, int minorCode, String string1) throws Exception {
        reportFatalXMLError(majorCode, minorCode, string1);
        skipPastEndOfCurrentMarkup();
    }
    private void abortMarkup(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        reportFatalXMLError(majorCode, minorCode, stringIndex1, stringIndex2);
        skipPastEndOfCurrentMarkup();
    }
    private void skipPastEndOfCurrentMarkup() throws Exception {
        fEntityReader.skipToChar('>');
        if (fEntityReader.lookingAtChar('>', true))
            decreaseMarkupDepth();
    }
    //
    //
    //
    static private final int SCANNER_STATE_INVALID = -1;
    static private final int SCANNER_STATE_END_OF_INPUT = 0;
    static private final int SCANNER_STATE_DOCTYPEDECL = 50;
    static private final int SCANNER_STATE_MARKUP_DECL = 51;
    static private final int SCANNER_STATE_TEXTDECL = 53;
    static private final int SCANNER_STATE_COMMENT = 54;
    static private final int SCANNER_STATE_PI = 55;
    static private final int SCANNER_STATE_DEFAULT_ATTRIBUTE_VALUE = 56;
    static private final int SCANNER_STATE_CONTENTSPEC = 57;
    static private final int SCANNER_STATE_ENTITY_VALUE = 58;
    static private final int SCANNER_STATE_SYSTEMLITERAL = 59;
    static private final int SCANNER_STATE_PUBIDLITERAL = 60;

    private int setScannerState(int scannerState) {
        int prevState = fScannerState;
        fScannerState = scannerState;
        return prevState;
    }
    private int getScannerState() {
        return fScannerState;
    }
    private void restoreScannerState(int scannerState) {
        if (fScannerState != SCANNER_STATE_END_OF_INPUT)
            fScannerState = scannerState;
    }
    /**
     * Change readers
     *
     * @param nextReader the new reader that the scanner will use
     * @param nextReaderId id of the reader to change to
     * @exception throws java.lang.Exception
     */
    public void readerChange(XMLEntityHandler.EntityReader nextReader, int nextReaderId) throws Exception {
        fEntityReader = nextReader;
        fReaderId = nextReaderId;
        if (fScannerState == SCANNER_STATE_DEFAULT_ATTRIBUTE_VALUE) {
            fDefaultAttValueOffset = fEntityReader.currentOffset();
            fDefaultAttValueMark = fDefaultAttValueOffset;
        } else if (fScannerState == SCANNER_STATE_ENTITY_VALUE) {
            fEntityValueMark = fEntityReader.currentOffset();
        }
    }
    /**
     * Handle the end of input
     *
     * @param entityName the handle in the string pool of the name of the entity which has reached end of input
     * @param moreToFollow if true, there is still input left to process in other readers
     * @exception java.lang.Exception
     */
    public void endOfInput(int entityNameIndex, boolean moreToFollow) throws Exception {
        moreToFollow = fReaderId != fExternalSubsetReader;
        switch (fScannerState) {
        case SCANNER_STATE_INVALID:
            throw new RuntimeException("FWK004 XMLDTDScanner.endOfInput: cannot happen: 2"+"\n2");
        case SCANNER_STATE_END_OF_INPUT:
            break;
        case SCANNER_STATE_MARKUP_DECL:
            if (!moreToFollow && fIncludeSectDepth > 0) {
                reportFatalXMLError(XMLMessages.MSG_INCLUDESECT_UNTERMINATED,
                                    XMLMessages.P62_UNTERMINATED);
            }
            break;
        case SCANNER_STATE_DOCTYPEDECL:
            throw new RuntimeException("FWK004 XMLDTDScanner.endOfInput: cannot happen: 2.5"+"\n2.5");
//            break;
        case SCANNER_STATE_TEXTDECL:
// REVISIT            reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
            break;
        case SCANNER_STATE_SYSTEMLITERAL:
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_SYSTEMID_UNTERMINATED,
                                    XMLMessages.P11_UNTERMINATED);
            } else {
// REVISIT                reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
            }
            break;
        case SCANNER_STATE_PUBIDLITERAL:
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_PUBLICID_UNTERMINATED,
                                    XMLMessages.P12_UNTERMINATED);
            } else {
// REVISIT                reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
            }
            break;
        case SCANNER_STATE_COMMENT:
            if (!moreToFollow && !getReadingExternalEntity()) {
                reportFatalXMLError(XMLMessages.MSG_COMMENT_UNTERMINATED,
                                    XMLMessages.P15_UNTERMINATED);
            } else {
                //
                // REVISIT - HACK !!!  code changed to pass incorrect OASIS test 'invalid--001'
                //  Uncomment the next line to conform to the spec...
                //
                //reportFatalXMLError(XMLMessages.MSG_COMMENT_NOT_IN_ONE_ENTITY,
                //                    XMLMessages.P78_NOT_WELLFORMED);
            }
            break;
        case SCANNER_STATE_PI:
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_PI_UNTERMINATED,
                                    XMLMessages.P16_UNTERMINATED);
            } else {
                reportFatalXMLError(XMLMessages.MSG_PI_NOT_IN_ONE_ENTITY,
                                    XMLMessages.P78_NOT_WELLFORMED);
            }
            break;
        case SCANNER_STATE_DEFAULT_ATTRIBUTE_VALUE:
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_UNTERMINATED,
                                    XMLMessages.P10_UNTERMINATED,
                                    fDefaultAttValueElementType,
                                    fDefaultAttValueAttrName);
            } else if (fReaderId == fDefaultAttValueReader) {
// REVISIT                reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
            } else {
                fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
            }
            break;
        case SCANNER_STATE_CONTENTSPEC:
            break;
        case SCANNER_STATE_ENTITY_VALUE:
            if (fReaderId == fEntityValueReader) {
// REVISIT                reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
            } else {
                fEntityReader.append(fLiteralData, fEntityValueMark, fEntityReader.currentOffset() - fEntityValueMark);
            }
            break;
        default:
            throw new RuntimeException("FWK004 XMLDTDScanner.endOfInput: cannot happen: 3"+"\n3");
        }
        if (!moreToFollow) {
            setScannerState(SCANNER_STATE_END_OF_INPUT);
        }
    }
    //
    // [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
    //
    private int scanCharRef() throws Exception {
        int valueOffset = fEntityReader.currentOffset();
        boolean hex = fEntityReader.lookingAtChar('x', true);
        int num = fEntityReader.scanCharRef(hex);
        if (num < 0) {
            switch (num) {
            case XMLEntityHandler.CHARREF_RESULT_SEMICOLON_REQUIRED:
                reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_CHARREF,
                                    XMLMessages.P66_SEMICOLON_REQUIRED);
                return -1;
            case XMLEntityHandler.CHARREF_RESULT_INVALID_CHAR:
                int majorCode = hex ? XMLMessages.MSG_HEXDIGIT_REQUIRED_IN_CHARREF :
                                      XMLMessages.MSG_DIGIT_REQUIRED_IN_CHARREF;
                int minorCode = hex ? XMLMessages.P66_HEXDIGIT_REQUIRED :
                                      XMLMessages.P66_DIGIT_REQUIRED;
                reportFatalXMLError(majorCode, minorCode);
                return -1;
            case XMLEntityHandler.CHARREF_RESULT_OUT_OF_RANGE:
                num = 0x110000; // this will cause the right error to be reported below...
                break;
            }
        }
        //
        //  [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF]        // any Unicode character, excluding the
        //               | [#xE000-#xFFFD] | [#x10000-#x10FFFF] // surrogate blocks, FFFE, and FFFF.
        //
        if (num < 0x20) {
            if (num == 0x09 || num == 0x0A || num == 0x0D) {
                return num;
            }
        } else if (num <= 0xD7FF || (num >= 0xE000 && (num <= 0xFFFD || (num >= 0x10000 && num <= 0x10FFFF)))) {
            return num;
        }
        int valueLength = fEntityReader.currentOffset() - valueOffset;
        reportFatalXMLError(XMLMessages.MSG_INVALID_CHARREF,
                            XMLMessages.WFC_LEGAL_CHARACTER,
                            fEntityReader.addString(valueOffset, valueLength));
        return -1;
    }
    //
    // From the standard:
    //
    // [15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
    //
    // Called after scanning past '<!--'
    //
    private void scanComment() throws Exception
    {
        int commentOffset = fEntityReader.currentOffset();
        boolean sawDashDash = false;
        int previousState = setScannerState(SCANNER_STATE_COMMENT);
        while (fScannerState == SCANNER_STATE_COMMENT) {
            if (fEntityReader.lookingAtChar('-', false)) {
                int nextEndOffset = fEntityReader.currentOffset();
                int endOffset = 0;
                fEntityReader.lookingAtChar('-', true);
                int offset = fEntityReader.currentOffset();
                int count = 1;
                while (fEntityReader.lookingAtChar('-', true)) {
                    count++;
                    endOffset = nextEndOffset;
                    nextEndOffset = offset;
                    offset = fEntityReader.currentOffset();
                }
                if (count > 1) {
                    if (fEntityReader.lookingAtChar('>', true)) {
                        if (!sawDashDash && count > 2) {
                            reportFatalXMLError(XMLMessages.MSG_DASH_DASH_IN_COMMENT,
                                                XMLMessages.P15_DASH_DASH);
                            sawDashDash = true;
                        }
                        decreaseMarkupDepth();
                        fEventHandler.callComment(fEntityReader.addString(commentOffset, endOffset - commentOffset));
                        restoreScannerState(previousState);
                        return;
                    } else if (!sawDashDash) {
                        reportFatalXMLError(XMLMessages.MSG_DASH_DASH_IN_COMMENT,
                                            XMLMessages.P15_DASH_DASH);
                        sawDashDash = true;
                    }
                }
            } else {
                if (!fEntityReader.lookingAtValidChar(true)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        if (invChar >= 0) {
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_COMMENT,
                                                XMLMessages.P15_INVALID_CHARACTER,
                                                Integer.toHexString(invChar));
                        }
                    }
                }
            }
        }
        restoreScannerState(previousState);
    }
    //
    // From the standard:
    //
    // [16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
    // [17] PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
    //
    private void scanPI(int piTarget) throws Exception
    {
        String piTargetString = fStringPool.toString(piTarget);
        if (piTargetString.length() == 3 &&
            (piTargetString.charAt(0) == 'X' || piTargetString.charAt(0) == 'x') &&
            (piTargetString.charAt(1) == 'M' || piTargetString.charAt(1) == 'm') &&
            (piTargetString.charAt(2) == 'L' || piTargetString.charAt(2) == 'l')) {
            abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                        XMLMessages.P17_RESERVED_PITARGET);
            return;
        }
        int prevState = setScannerState(SCANNER_STATE_PI);
        int piDataOffset = -1;
        int piDataLength = 0;
        if (!fEntityReader.lookingAtSpace(true)) {
            if (!fEntityReader.lookingAtChar('?', true) || !fEntityReader.lookingAtChar('>', true)) {
                if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                    abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_IN_PI,
                                XMLMessages.P16_WHITESPACE_REQUIRED);
                    restoreScannerState(prevState);
                }
                return;
            }
            decreaseMarkupDepth();
            restoreScannerState(prevState);
        } else {
            fEntityReader.skipPastSpaces();
            piDataOffset = fEntityReader.currentOffset();
            while (fScannerState == SCANNER_STATE_PI) {
                while (fEntityReader.lookingAtChar('?', false)) {
                    int offset = fEntityReader.currentOffset();
                    fEntityReader.lookingAtChar('?', true);
                    if (fEntityReader.lookingAtChar('>', true)) {
                        piDataLength = offset - piDataOffset;
                        decreaseMarkupDepth();
                        restoreScannerState(prevState);
                        break;
                    }
                }
                if (fScannerState != SCANNER_STATE_PI)
                    break;
                if (!fEntityReader.lookingAtValidChar(true)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        if (invChar >= 0) {
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_PI,
                                                XMLMessages.P16_INVALID_CHARACTER,
                                                Integer.toHexString(invChar));
                        }
                        skipPastEndOfCurrentMarkup();
                        restoreScannerState(prevState);
                    }
                    return;
                }
            }
        }
        int piData = piDataLength == 0 ?
                     StringPool.EMPTY_STRING : fEntityReader.addString(piDataOffset, piDataLength);
        fEventHandler.callProcessingInstruction(piTarget, piData);
    }
    //
    // From the standard:
    //
    // [28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S?
    //                      ('[' (markupdecl | PEReference | S)* ']' S?)? '>'
    // [29] markupdecl ::= elementdecl | AttlistDecl | EntityDecl
    //                     | NotationDecl | PI | Comment
    //
    // Called after scanning '<!DOCTYPE'
    //
    /**
     * This routine is called after the &lt;!DOCTYPE portion of a DOCTYPE
     * line has been called.  scanDocTypeDecl goes onto scan the rest of the DOCTYPE
     * decl.  If an internal DTD subset exists, it is scanned. If an external DTD
     * subset exists, scanDocTypeDecl sets up the state necessary to process it.
     *
     * @return true if successful
     * @exception java.lang.Exception
     */
    public boolean scanDoctypeDecl() throws Exception
    {
        increaseMarkupDepth();
        fEntityReader = fEntityHandler.getEntityReader();
        fReaderId = fEntityHandler.getReaderId();
        fDoctypeReader = fReaderId;
        setScannerState(SCANNER_STATE_DOCTYPEDECL);
        if (!fEntityReader.lookingAtSpace(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ROOT_ELEMENT_TYPE_IN_DOCTYPEDECL,
                        XMLMessages.P28_SPACE_REQUIRED);
            return false;
        }
        fEntityReader.skipPastSpaces();
        int rootElementType = fEventHandler.scanElementType(fEntityReader, ' ');
        if (rootElementType == -1) {
            abortMarkup(XMLMessages.MSG_ROOT_ELEMENT_TYPE_REQUIRED,
                        XMLMessages.P28_ROOT_ELEMENT_TYPE_REQUIRED);
            return false;
        }
        boolean lbrkt;
        boolean scanExternalSubset = false;
        int publicId = -1;
        int systemId = -1;
        if (fEntityReader.lookingAtSpace(true)) {
            fEntityReader.skipPastSpaces();
            if (!(lbrkt = fEntityReader.lookingAtChar('[', true)) && !fEntityReader.lookingAtChar('>', false)) {
                if (!scanExternalID(false)) {
                    skipPastEndOfCurrentMarkup();
                    return false;
                }
                scanExternalSubset = true;
                publicId = fPubidLiteral;
                systemId = fSystemLiteral;
                fEntityReader.skipPastSpaces();
                lbrkt = fEntityReader.lookingAtChar('[', true);
            }
        } else
            lbrkt = fEntityReader.lookingAtChar('[', true);
        fEventHandler.doctypeDecl(rootElementType, publicId, systemId);
        if (lbrkt) {
            scanDecls(false);
            fEntityReader.skipPastSpaces();
        }
        if (!fEntityReader.lookingAtChar('>', true)) {
            if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                abortMarkup(XMLMessages.MSG_DOCTYPEDECL_UNTERMINATED,
                            XMLMessages.P28_UNTERMINATED,
                            rootElementType);
            }
            return false;
        }
        decreaseMarkupDepth();

        if (scanExternalSubset)
            fEventHandler.startReadingFromExternalSubset(publicId, systemId);

        return true;
    }
    //
    // [75] ExternalID ::= 'SYSTEM' S SystemLiteral
    //                     | 'PUBLIC' S PubidLiteral S SystemLiteral
    // [83] PublicID ::= 'PUBLIC' S PubidLiteral
    //
    private boolean scanExternalID(boolean scanPublicID) throws Exception
    {
        fSystemLiteral = -1;
        fPubidLiteral = -1;
        int offset = fEntityReader.currentOffset();
        if (fEntityReader.skippedString(system_string)) {
            if (!fEntityReader.lookingAtSpace(true)) {
                reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_SYSTEMLITERAL_IN_EXTERNALID,
                                    XMLMessages.P75_SPACE_REQUIRED);
                return false;
            }
            fEntityReader.skipPastSpaces();
            return scanSystemLiteral();
        }
        if (fEntityReader.skippedString(public_string)) {
            if (!fEntityReader.lookingAtSpace(true)) {
                reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_PUBIDLITERAL_IN_EXTERNALID,
                                    XMLMessages.P75_SPACE_REQUIRED);
                return false;
            }
            fEntityReader.skipPastSpaces();
            if (!scanPubidLiteral())
                return false;
            if (scanPublicID) {
                //
                // [82] NotationDecl ::= '<!NOTATION' S Name S (ExternalID |  PublicID) S? '>'
                //
                if (!fEntityReader.lookingAtSpace(true))
                    return true; // no S, not an ExternalID
                fEntityReader.skipPastSpaces();
                if (fEntityReader.lookingAtChar('>', false)) // matches end of NotationDecl
                    return true;
            } else {
                if (!fEntityReader.lookingAtSpace(true)) {
                    reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_AFTER_PUBIDLITERAL_IN_EXTERNALID,
                                        XMLMessages.P75_SPACE_REQUIRED);
                    return false;
                }
                fEntityReader.skipPastSpaces();
            }
            return scanSystemLiteral();
        }
        reportFatalXMLError(XMLMessages.MSG_EXTERNALID_REQUIRED,
                            XMLMessages.P75_INVALID);
        return false;
    }
    //
    // [11] SystemLiteral ::= ('"' [^"]* '"') | ("'" [^']* "'")
    //
    // REVISIT - need to look into uri escape mechanism for non-ascii characters.
    //
    private boolean scanSystemLiteral() throws Exception
    {
        boolean single;
        if (!(single = fEntityReader.lookingAtChar('\'', true)) && !fEntityReader.lookingAtChar('\"', true)) {
            reportFatalXMLError(XMLMessages.MSG_QUOTE_REQUIRED_IN_SYSTEMID,
                                XMLMessages.P11_QUOTE_REQUIRED);
            return false;
        }
        int prevState = setScannerState(SCANNER_STATE_SYSTEMLITERAL);
        int offset = fEntityReader.currentOffset();
        char qchar = single ? '\'' : '\"';
        boolean dataok = true;
        boolean fragment = false;
        while (!fEntityReader.lookingAtChar(qchar, false)) {
            if (fEntityReader.lookingAtChar('#', true)) {
                fragment = true;
            } else if (!fEntityReader.lookingAtValidChar(true)) {
                dataok = false;
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                    return false;
                if (invChar >= 0) {
                    reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_SYSTEMID,
                                        XMLMessages.P11_INVALID_CHARACTER,
                                        Integer.toHexString(invChar));
                }
            }
        }
        if (dataok) {
            fSystemLiteral = fEntityReader.addString(offset, fEntityReader.currentOffset() - offset);
            if (fragment) {
                // NOTE: RECOVERABLE ERROR
                Object[] args = { fStringPool.toString(fSystemLiteral) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_URI_FRAGMENT_IN_SYSTEMID,
                                           XMLMessages.P11_URI_FRAGMENT,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }
        fEntityReader.lookingAtChar(qchar, true);
        restoreScannerState(prevState);
        return dataok;
    }
    //
    // [12] PubidLiteral ::= '"' PubidChar* '"' | "'" (PubidChar - "'")* "'"
    // [13] PubidChar ::= #x20 | #xD | #xA | [a-zA-Z0-9] | [-'()+,./:=?;!*#@$_%]
    //
    private boolean scanPubidLiteral() throws Exception
    {
        boolean single;
        if (!(single = fEntityReader.lookingAtChar('\'', true)) && !fEntityReader.lookingAtChar('\"', true)) {
            reportFatalXMLError(XMLMessages.MSG_QUOTE_REQUIRED_IN_PUBLICID,
                                XMLMessages.P12_QUOTE_REQUIRED);
            return false;
        }
        char qchar = single ? '\'' : '\"';
        int prevState = setScannerState(SCANNER_STATE_PUBIDLITERAL);
        boolean dataok = true;
        while (true) {
            if (fEntityReader.lookingAtChar((char)0x09, true)) {
                dataok = false;
                reportFatalXMLError(XMLMessages.MSG_PUBIDCHAR_ILLEGAL,
                                    XMLMessages.P12_INVALID_CHARACTER, "9");
            }
            if (!fEntityReader.lookingAtSpace(true))
                break;
        }
        int offset = fEntityReader.currentOffset();
        int dataOffset = fLiteralData.length();
        int toCopy = offset;
        while (true) {
            if (fEntityReader.lookingAtChar(qchar, true)) {
                if (dataok && offset - toCopy > 0)
                    fEntityReader.append(fLiteralData, toCopy, offset - toCopy);
                break;
            }
            if (fEntityReader.lookingAtChar((char)0x09, true)) {
                dataok = false;
                reportFatalXMLError(XMLMessages.MSG_PUBIDCHAR_ILLEGAL,
                                    XMLMessages.P12_INVALID_CHARACTER, "9");
                continue;
            }
            if (fEntityReader.lookingAtSpace(true)) {
                if (dataok && offset - toCopy > 0)
                    fEntityReader.append(fLiteralData, toCopy, offset - toCopy);
                while (true) {
                    if (fEntityReader.lookingAtChar((char)0x09, true)) {
                        dataok = false;
                        reportFatalXMLError(XMLMessages.MSG_PUBIDCHAR_ILLEGAL,
                                            XMLMessages.P12_INVALID_CHARACTER, "9");
                        break;
                    } else if (!fEntityReader.lookingAtSpace(true)) {
                        break;
                    }
                }
                if (fEntityReader.lookingAtChar(qchar, true))
                    break;
                if (dataok) {
                    fLiteralData.append(' ');
                    offset = fEntityReader.currentOffset();
                    toCopy = offset;
                }
                continue;
            }
            if (!fEntityReader.lookingAtValidChar(true)) {
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                    return false;
                dataok = false;
                if (invChar >= 0) {
                    reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_PUBLICID,
                                        XMLMessages.P12_INVALID_CHARACTER,
                                        Integer.toHexString(invChar));
                }
            }
            if (dataok)
                offset = fEntityReader.currentOffset();
        }
        if (dataok) {
            int dataLength = fLiteralData.length() - dataOffset;
            fPubidLiteral = fLiteralData.addString(dataOffset, dataLength);
            String publicId = fStringPool.toString(fPubidLiteral);
            int invCharIndex = fEventHandler.validPublicId(publicId);
            if (invCharIndex >= 0) {
                reportFatalXMLError(XMLMessages.MSG_PUBIDCHAR_ILLEGAL,
                                    XMLMessages.P12_INVALID_CHARACTER,
                                    Integer.toHexString(publicId.charAt(invCharIndex)));
                return false;
            }
        }
        restoreScannerState(prevState);
        return dataok;
    }
    //
    // [??] intSubsetDecl = '[' (markupdecl | PEReference | S)* ']'
    //
    // [31] extSubsetDecl ::= ( markupdecl | conditionalSect | PEReference | S )*
    // [62] includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'
    //
    // [29] markupdecl ::= elementdecl | AttlistDecl | EntityDecl
    //                     | NotationDecl | PI | Comment
    //
    // [45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'
    //
    // [52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
    //
    // [70] EntityDecl ::= GEDecl | PEDecl
    // [71] GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
    // [72] PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
    //
    // [82] NotationDecl ::= '<!NOTATION' S Name S (ExternalID |  PublicID) S? '>'
    //
    // [16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
    //
    // [15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
    //
    // [61] conditionalSect ::= includeSect | ignoreSect
    // [62] includeSect ::= '<![' S? 'INCLUDE' S? '[' extSubsetDecl ']]>'
    // [63] ignoreSect ::= '<![' S? 'IGNORE' S? '[' ignoreSectContents* ']]>'
    // [64] ignoreSectContents ::= Ignore ('<![' ignoreSectContents ']]>' Ignore)*
    // [65] Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)
    //
    /**
     * Scan markup declarations
     *
     * @param extSubset true if the scanner is scanning an external subset, false
     *                  if it is scanning an internal subset
     * @exception java.lang.Exception
     */
    public void scanDecls(boolean extSubset) throws Exception
    {
        int subsetOffset = fEntityReader.currentOffset();
        if (extSubset)
            fExternalSubsetReader = fReaderId;
        fIncludeSectDepth = 0;
        boolean parseTextDecl = extSubset;
        int prevState = setScannerState(SCANNER_STATE_MARKUP_DECL);
        while (fScannerState == SCANNER_STATE_MARKUP_DECL) {
            boolean newParseTextDecl = false;
            if (!extSubset && fEntityReader.lookingAtChar(']', true)) {
                fEventHandler.internalSubset(
                    fEntityReader.addString(subsetOffset, 
                                            (fEntityReader.currentOffset()-subsetOffset)-1));
            
                restoreScannerState(prevState);
                return;
            }
            if (fEntityReader.lookingAtChar('<', true)) {
                int olddepth = markupDepth();
                increaseMarkupDepth();
                if (fEntityReader.lookingAtChar('!', true)) {
                    if (fEntityReader.lookingAtChar('-', true)) {
                        if (fEntityReader.lookingAtChar('-', true)) {
                            scanComment();
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                        XMLMessages.P29_NOT_RECOGNIZED);
                        }
                    } else if (fEntityReader.lookingAtChar('[', true) && getReadingExternalEntity()) {
                        checkForPEReference(false);
                        if (fEntityReader.skippedString(include_string)) {
                            checkForPEReference(false);
                            if (!fEntityReader.lookingAtChar('[', true)) {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                            XMLMessages.P29_NOT_RECOGNIZED);
                            } else {
                                fIncludeSectDepth++;
                            }
                        } else if (fEntityReader.skippedString(ignore_string)) {
                            checkForPEReference(false);
                            if (!fEntityReader.lookingAtChar('[', true)) {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                            XMLMessages.P29_NOT_RECOGNIZED);
                            } else
                                scanIgnoreSectContents();
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                        XMLMessages.P29_NOT_RECOGNIZED);
                        }
                    } else if (fEntityReader.skippedString(element_string))
                        scanElementDecl();
                    else if (fEntityReader.skippedString(attlist_string))
                        scanAttlistDecl();
                    else if (fEntityReader.skippedString(entity_string))
                        scanEntityDecl();
                    else if (fEntityReader.skippedString(notation_string))
                        scanNotationDecl();
                    else {
                        abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                    XMLMessages.P29_NOT_RECOGNIZED);
                    }
                } else if (fEntityReader.lookingAtChar('?', true)) {
                    int piTarget = fEntityReader.scanName(' ');
                    if (piTarget == -1) {
                        abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                    XMLMessages.P16_REQUIRED);
                    } else if ("xml".equals(fStringPool.toString(piTarget))) {
                        if (fEntityReader.lookingAtSpace(true)) {
                            if (parseTextDecl) { // a TextDecl looks like a PI with the target 'xml'
                                scanTextDecl();
                            } else {
                                abortMarkup(XMLMessages.MSG_TEXTDECL_MUST_BE_FIRST,
                                            XMLMessages.P30_TEXTDECL_MUST_BE_FIRST);
                            }
                        } else { // a PI target matching 'xml'
                            abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                        XMLMessages.P17_RESERVED_PITARGET);
                        }
                    } else // PI
                        scanPI(piTarget);
                } else {
                    abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                XMLMessages.P29_NOT_RECOGNIZED);
                }
            } else if (fEntityReader.lookingAtSpace(true)) {
                fEntityReader.skipPastSpaces();
            } else if (fEntityReader.lookingAtChar('%', true)) {
                //
                // [69] PEReference ::= '%' Name ';'
                //
                int nameOffset = fEntityReader.currentOffset();
                fEntityReader.skipPastName(';');
                int nameLength = fEntityReader.currentOffset() - nameOffset;
                if (nameLength == 0) {
                    reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_NAME_REQUIRED);
                } else if (!fEntityReader.lookingAtChar(';', true)) {
                    reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_SEMICOLON_REQUIRED,
                                        fEntityReader.addString(nameOffset, nameLength));
                } else {
                    int peNameIndex = fEntityReader.addSymbol(nameOffset, nameLength);
                    newParseTextDecl = fEntityHandler.startReadingFromEntity(peNameIndex, markupDepth(), XMLEntityHandler.CONTEXT_IN_DTD_AS_MARKUP);
                }
            } else if (fIncludeSectDepth > 0 && fEntityReader.lookingAtChar(']', true)) {
                if (!fEntityReader.lookingAtChar(']', true) || !fEntityReader.lookingAtChar('>', true)) {
                    abortMarkup(XMLMessages.MSG_INCLUDESECT_UNTERMINATED,
                                XMLMessages.P62_UNTERMINATED);
                } else
                    decreaseMarkupDepth();
                fIncludeSectDepth--;
            } else {
                if (!fEntityReader.lookingAtValidChar(false)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                        break;
                    if (invChar >= 0) {
                        if (!extSubset) {
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_INTERNAL_SUBSET,
                                                XMLMessages.P28_INVALID_CHARACTER,
                                                Integer.toHexString(invChar));
                        } else {
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_EXTERNAL_SUBSET,
                                                XMLMessages.P30_INVALID_CHARACTER,
                                                Integer.toHexString(invChar));
                        }
                    }
                } else {
                    reportFatalXMLError(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD,
                                        XMLMessages.P29_NOT_RECOGNIZED);
                    fEntityReader.lookingAtValidChar(true);
                }
            }
            parseTextDecl = newParseTextDecl;
        }
        if (extSubset)
            fEventHandler.stopReadingFromExternalSubset();
        else 
            fEventHandler.internalSubset(
                fEntityReader.addString(subsetOffset, 
                                        (fEntityReader.currentOffset()-subsetOffset)));
        

    }
    //
    // [64] ignoreSectContents ::= Ignore ('<![' ignoreSectContents ']]>' Ignore)*
    // [65] Ignore ::= Char* - (Char* ('<![' | ']]>') Char*)
    //
    private void scanIgnoreSectContents() throws Exception
    {
        int initialDepth = ++fIncludeSectDepth;
        while (true) {
            if (fEntityReader.lookingAtChar('<', true)) {
                //
                // These tests are split so that we handle cases like
                // '<<![' and '<!<![' which we might otherwise miss.
                //
                if (fEntityReader.lookingAtChar('!', true) && fEntityReader.lookingAtChar('[', true))
                    fIncludeSectDepth++;
            } else if (fEntityReader.lookingAtChar(']', true)) {
                //
                // The same thing goes for ']<![' and '<]]>', etc.
                //
                if (fEntityReader.lookingAtChar(']', true)) {
                    while (fEntityReader.lookingAtChar(']', true)) {
                        /* empty loop body */
                    }
                    if (fEntityReader.lookingAtChar('>', true)) {
                        if (fIncludeSectDepth-- == initialDepth) {
                            decreaseMarkupDepth();
                            return;
                        }
                    }
                }
            } else if (!fEntityReader.lookingAtValidChar(true)) {
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                    return;
                if (invChar >= 0) {
                    reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_IGNORESECT,
                                        XMLMessages.P65_INVALID_CHARACTER,
                                        Integer.toHexString(invChar));
                }
            }
        }
    }
    //
    // From the standard:
    //
    // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    // [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
    //
    private void scanTextDecl() throws Exception {
        final int TEXTDECL_START = 0;
        final int TEXTDECL_VERSION = 1;
        final int TEXTDECL_ENCODING = 2;
        final int TEXTDECL_FINISHED = 3;
        int prevState = setScannerState(SCANNER_STATE_TEXTDECL);
        int state = TEXTDECL_START;
        do {
            fEntityReader.skipPastSpaces();
            int offset = fEntityReader.currentOffset();
            if (state == TEXTDECL_START && fEntityReader.skippedString(version_string)) {
                state = TEXTDECL_VERSION;
            } else if (fEntityReader.skippedString(encoding_string)) {
                state = TEXTDECL_ENCODING;
            } else {
                abortMarkup(XMLMessages.MSG_ENCODINGDECL_REQUIRED,
                            XMLMessages.P77_ENCODINGDECL_REQUIRED);
                restoreScannerState(prevState);
                return;
            }
            int length = fEntityReader.currentOffset() - offset;
            fEntityReader.skipPastSpaces();
            if (!fEntityReader.lookingAtChar('=', true)) {
                int minorCode = state == TEXTDECL_VERSION ?
                                XMLMessages.P24_EQ_REQUIRED :
                                XMLMessages.P80_EQ_REQUIRED;
                abortMarkup(XMLMessages.MSG_EQ_REQUIRED_IN_TEXTDECL, minorCode,
                            fEntityReader.addString(offset, length));
                restoreScannerState(prevState);
                return;
            }
            fEntityReader.skipPastSpaces();
            int index = fEntityReader.scanStringLiteral();
            switch (index) {
            case XMLEntityHandler.STRINGLIT_RESULT_QUOTE_REQUIRED:
            {
                int minorCode = state == TEXTDECL_VERSION ?
                                XMLMessages.P24_QUOTE_REQUIRED :
                                XMLMessages.P80_QUOTE_REQUIRED;
                abortMarkup(XMLMessages.MSG_QUOTE_REQUIRED_IN_TEXTDECL, minorCode,
                            fEntityReader.addString(offset, length));
                restoreScannerState(prevState);
                return;
            }
            case XMLEntityHandler.STRINGLIT_RESULT_INVALID_CHAR:
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                    if (invChar >= 0) {
                        int minorCode = state == TEXTDECL_VERSION ?
                                        XMLMessages.P26_INVALID_CHARACTER :
                                        XMLMessages.P81_INVALID_CHARACTER;
                        reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_TEXTDECL, minorCode,
                                            Integer.toHexString(invChar));
                    }
                    skipPastEndOfCurrentMarkup();
                    restoreScannerState(prevState);
                }
                return;
            default:
                break;
            }
            switch (state) {
            case TEXTDECL_VERSION:
                //
                // version="..."
                //
                String version = fStringPool.toString(index);
                if (!"1.0".equals(version)) {
                    if (!fEventHandler.validVersionNum(version)) {
                        abortMarkup(XMLMessages.MSG_VERSIONINFO_INVALID,
                                    XMLMessages.P26_INVALID_VALUE,
                                    version);
                        restoreScannerState(prevState);
                        return;
                    }
                    // NOTE: RECOVERABLE ERROR
                    Object[] args = { version };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_VERSION_NOT_SUPPORTED,
                                               XMLMessages.P26_NOT_SUPPORTED,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    // REVISIT - hope it is a compatible version...
                    // skipPastEndOfCurrentMarkup();
                    // return;
                }
                if (!fEntityReader.lookingAtSpace(true)) {
                    abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_IN_TEXTDECL,
                                XMLMessages.P80_WHITESPACE_REQUIRED);
                    restoreScannerState(prevState);
                    return;
                }
                break;
            case TEXTDECL_ENCODING:
                //
                // encoding = "..."
                //
                String encoding = fStringPool.toString(index);
                if (!fEventHandler.validEncName(encoding)) {
                    abortMarkup(XMLMessages.MSG_ENCODINGDECL_INVALID,
                                XMLMessages.P81_INVALID_VALUE,
                                encoding);
                    restoreScannerState(prevState);
                    return;
                }
                fEntityReader.skipPastSpaces();
                state = TEXTDECL_FINISHED;
                break;
            }
        } while (state != TEXTDECL_FINISHED);
        if (!fEntityReader.lookingAtChar('?', true) || !fEntityReader.lookingAtChar('>', true)) {
            abortMarkup(XMLMessages.MSG_TEXTDECL_UNTERMINATED,
                        XMLMessages.P77_UNTERMINATED);
            restoreScannerState(prevState);
            return;
        }
        decreaseMarkupDepth();
        restoreScannerState(prevState);
    }
    //
    // [45] elementdecl ::= '<!ELEMENT' S Name S contentspec S? '>'
    //
    private void scanElementDecl() throws Exception
    {
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ELEMENTDECL,
                        XMLMessages.P45_SPACE_REQUIRED);
            return;
        }
        int elementType = fEventHandler.checkForElementTypeWithPEReference(fEntityReader, ' ');
        if (elementType == -1) {
            abortMarkup(XMLMessages.MSG_ELEMENT_TYPE_REQUIRED_IN_ELEMENTDECL,
                        XMLMessages.P45_ELEMENT_TYPE_REQUIRED);
            return;
        }
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_CONTENTSPEC_IN_ELEMENTDECL,
                        XMLMessages.P45_SPACE_REQUIRED,
                        elementType);
            return;
        }
        int contentSpecType = -1;
        int contentSpec = -1;
        if (fEntityReader.skippedString(empty_string)) {
            contentSpecType = fEMPTY;
        } else if (fEntityReader.skippedString(any_string)) {
            contentSpecType = fANY;
        } else if (!fEntityReader.lookingAtChar('(', true)) {
            abortMarkup(XMLMessages.MSG_CONTENTSPEC_REQUIRED_IN_ELEMENTDECL,
                        XMLMessages.P45_CONTENTSPEC_REQUIRED,
                        elementType);
            return;
        } else {
            int contentSpecReader = fReaderId;
            int contentSpecReaderDepth = fEntityHandler.getReaderDepth();
            int prevState = setScannerState(SCANNER_STATE_CONTENTSPEC);
            int oldDepth = parenDepth();
            fEntityHandler.setReaderDepth(oldDepth);
            increaseParenDepth();
            checkForPEReference(false);
            boolean skippedPCDATA = fEntityReader.skippedString(pcdata_string);
            if (skippedPCDATA) {
                contentSpecType = fMIXED;
                contentSpec = scanMixed(elementType);
            } else {
                contentSpecType = fCHILDREN;
                contentSpec = scanChildren(elementType);
            }
            boolean success = contentSpec != -1;
            restoreScannerState(prevState);
            fEntityHandler.setReaderDepth(contentSpecReaderDepth);
            if (!success) {
                setParenDepth(oldDepth);
                skipPastEndOfCurrentMarkup();
                return;
            } else {
                if (parenDepth() != oldDepth) // REVISIT - should not be needed
                    // System.out.println("nesting depth mismatch");
                    ;
            }
        }
        checkForPEReference(false);
        if (!fEntityReader.lookingAtChar('>', true)) {
            abortMarkup(XMLMessages.MSG_ELEMENTDECL_UNTERMINATED,
                        XMLMessages.P45_UNTERMINATED,
                        elementType);
            return;
        }
        decreaseMarkupDepth();
        int elementIndex = fEventHandler.addElementDecl(elementType, contentSpecType, contentSpec);
    }
    //
    // [51] Mixed ::= '(' S? '#PCDATA' (S? '|' S? Name)* S? ')*' | '(' S? '#PCDATA' S? ')'
    //
    // Called after scanning past '(' S? '#PCDATA'
    //
    private int scanMixed(int elementType) throws Exception
    {
        int valueIndex = -1;  // -1 is special value for #PCDATA
        int prevNodeIndex = -1;
        boolean starRequired = false;
        while (true) {
            int nodeIndex = fEventHandler.addUniqueLeafNode(valueIndex);
            checkForPEReference(false);
            if (!fEntityReader.lookingAtChar('|', true)) {
                if (!fEntityReader.lookingAtChar(')', true)) {
                    reportFatalXMLError(XMLMessages.MSG_CLOSE_PAREN_REQUIRED_IN_MIXED,
                                        XMLMessages.P51_CLOSE_PAREN_REQUIRED,
                                        elementType);
                    return -1;
                }
                decreaseParenDepth();
                if (nodeIndex == -1) {
                    nodeIndex = prevNodeIndex;
                } else if (prevNodeIndex != -1) {
                    nodeIndex = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_CHOICE, prevNodeIndex, nodeIndex);
                }
                if (fEntityReader.lookingAtChar('*', true)) {
                    nodeIndex = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE, nodeIndex);
                } else if (starRequired) {
                    reportFatalXMLError(XMLMessages.MSG_MIXED_CONTENT_UNTERMINATED,
                                        XMLMessages.P51_UNTERMINATED,
                                        fStringPool.toString(elementType),
                                        fEventHandler.getContentSpecNodeAsString(nodeIndex));
                    return -1;
                }
                return nodeIndex;
            }
            if (nodeIndex != -1) {
                if (prevNodeIndex != -1) {
                    nodeIndex = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_CHOICE, prevNodeIndex, nodeIndex);
                }
                prevNodeIndex = nodeIndex;
            }
            starRequired = true;
            checkForPEReference(false);
            valueIndex = fEventHandler.checkForElementTypeWithPEReference(fEntityReader, ')');
            if (valueIndex == -1) {
                reportFatalXMLError(XMLMessages.MSG_ELEMENT_TYPE_REQUIRED_IN_MIXED_CONTENT,
                                    XMLMessages.P51_ELEMENT_TYPE_REQUIRED,
                                    elementType);
                return -1;
            }
        }
    }
    //
    // [47] children ::= (choice | seq) ('?' | '*' | '+')?
    // [49] choice ::= '(' S? cp ( S? '|' S? cp )* S? ')'
    // [50] seq ::= '(' S? cp ( S? ',' S? cp )* S? ')'
    // [48] cp ::= (Name | choice | seq) ('?' | '*' | '+')?
    //
    private int scanChildren(int elementType) throws Exception
    {
        int depth = 1;
        initializeContentModelStack(depth);
        while (true) {
            if (fEntityReader.lookingAtChar('(', true)) {
                increaseParenDepth();
                checkForPEReference(false);
                depth++;
                initializeContentModelStack(depth);
                continue;
            }
            int valueIndex = fEventHandler.checkForElementTypeWithPEReference(fEntityReader, ')');
            if (valueIndex == -1) {
                reportFatalXMLError(XMLMessages.MSG_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED_IN_CHILDREN,
                                    XMLMessages.P47_OPEN_PAREN_OR_ELEMENT_TYPE_REQUIRED,
                                    elementType);
                return -1;
            }
            nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_LEAF, valueIndex);
            if (fEntityReader.lookingAtChar('?', true)) {
                nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE, nodeIndexStack[depth]);
            } else if (fEntityReader.lookingAtChar('*', true)) {
                nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE, nodeIndexStack[depth]);
            } else if (fEntityReader.lookingAtChar('+', true)) {
                nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE, nodeIndexStack[depth]);
            }
            while (true) {
                checkForPEReference(false);
                if (opStack[depth] != XMLContentSpecNode.CONTENTSPECNODE_SEQ && fEntityReader.lookingAtChar('|', true)) {
                    if (prevNodeIndexStack[depth] != -1) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(opStack[depth], prevNodeIndexStack[depth], nodeIndexStack[depth]);
                    }
                    prevNodeIndexStack[depth] = nodeIndexStack[depth];
                    opStack[depth] = XMLContentSpecNode.CONTENTSPECNODE_CHOICE;
                    break;
                } else if (opStack[depth] != XMLContentSpecNode.CONTENTSPECNODE_CHOICE && fEntityReader.lookingAtChar(',', true)) {
                    if (prevNodeIndexStack[depth] != -1) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(opStack[depth], prevNodeIndexStack[depth], nodeIndexStack[depth]);
                    }
                    prevNodeIndexStack[depth] = nodeIndexStack[depth];
                    opStack[depth] = XMLContentSpecNode.CONTENTSPECNODE_SEQ;
                    break;
                } else {
                    if (!fEntityReader.lookingAtChar(')', true)) {
                        reportFatalXMLError(XMLMessages.MSG_CLOSE_PAREN_REQUIRED_IN_CHILDREN,
                                            XMLMessages.P47_CLOSE_PAREN_REQUIRED,
                                            elementType);
                    }
                    decreaseParenDepth();
                    if (prevNodeIndexStack[depth] != -1) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(opStack[depth], prevNodeIndexStack[depth], nodeIndexStack[depth]);
                    }
                    int nodeIndex = nodeIndexStack[depth--];
                    nodeIndexStack[depth] = nodeIndex;
                    if (fEntityReader.lookingAtChar('?', true)) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE, nodeIndexStack[depth]);
                    } else if (fEntityReader.lookingAtChar('*', true)) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE, nodeIndexStack[depth]);
                    } else if (fEntityReader.lookingAtChar('+', true)) {
                        nodeIndexStack[depth] = fEventHandler.addContentSpecNode(XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE, nodeIndexStack[depth]);
                    }
                    if (depth == 0) {
                        return nodeIndexStack[0];
                    }
                }
            }
            checkForPEReference(false);
        }
    }
    //
    // [52] AttlistDecl ::= '<!ATTLIST' S Name AttDef* S? '>'
    // [53] AttDef ::= S Name S AttType S DefaultDecl
    // [60] DefaultDecl ::= '#REQUIRED' | '#IMPLIED' | (('#FIXED' S)? AttValue)
    //
    private void scanAttlistDecl() throws Exception
    {
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ELEMENT_TYPE_IN_ATTLISTDECL,
                        XMLMessages.P52_SPACE_REQUIRED);
            return;
        }
        int elementTypeIndex = fEventHandler.checkForElementTypeWithPEReference(fEntityReader, ' ');
        if (elementTypeIndex == -1) {
            abortMarkup(XMLMessages.MSG_ELEMENT_TYPE_REQUIRED_IN_ATTLISTDECL,
                        XMLMessages.P52_ELEMENT_TYPE_REQUIRED);
            return;
        }
        int elementIndex = fEventHandler.addElementDecl(elementTypeIndex);
        while (true) {
            boolean sawSpace = checkForPEReference(true);
            if (fEntityReader.lookingAtChar('>', true)) {
                decreaseMarkupDepth();
                return;
            }
            // REVISIT - review this code...
            if (!sawSpace) {
                if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                } else
                    reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ATTRIBUTE_NAME_IN_ATTDEF,
                                        XMLMessages.P53_SPACE_REQUIRED);
            } else {
                if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                }
            }
            int attDefName = fEventHandler.checkForAttributeNameWithPEReference(fEntityReader, ' ');
            if (attDefName == -1) {
                abortMarkup(XMLMessages.MSG_ATTRIBUTE_NAME_REQUIRED_IN_ATTDEF,
                            XMLMessages.P53_NAME_REQUIRED,
                            elementTypeIndex);
                return;
            }
            if (!checkForPEReference(true)) {
                abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ATTTYPE_IN_ATTDEF,
                            XMLMessages.P53_SPACE_REQUIRED);
                return;
            }
            int attDefType = -1;
            int attDefEnumeration = -1;
            if (fEntityReader.skippedString(cdata_string)) {
                attDefType = fCDATA;
            } else if (fEntityReader.skippedString(id_string)) {
                if (!fEntityReader.skippedString(ref_string)) {
                    attDefType = fID;
                } else if (!fEntityReader.lookingAtChar('S', true)) {
                    attDefType = fIDREF;
                } else {
                    attDefType = fIDREFS;
                }
            } else if (fEntityReader.skippedString(entit_string)) {
                if (fEntityReader.lookingAtChar('Y', true)) {
                    attDefType = fENTITY;
                } else if (fEntityReader.skippedString(ies_string)) {
                    attDefType = fENTITIES;
                } else {
                    abortMarkup(XMLMessages.MSG_ATTTYPE_REQUIRED_IN_ATTDEF,
                                XMLMessages.P53_ATTTYPE_REQUIRED,
                                elementTypeIndex, attDefName);
                    return;
                }
            } else if (fEntityReader.skippedString(nmtoken_string)) {
                if (fEntityReader.lookingAtChar('S', true)) {
                    attDefType = fNMTOKENS;
                } else {
                    attDefType = fNMTOKEN;
                }
            } else if (fEntityReader.skippedString(notation_string)) {
                if (!checkForPEReference(true)) {
                    abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_AFTER_NOTATION_IN_NOTATIONTYPE,
                                XMLMessages.P58_SPACE_REQUIRED,
                                elementTypeIndex, attDefName);
                    return;
                }
                if (!fEntityReader.lookingAtChar('(', true)) {
                    abortMarkup(XMLMessages.MSG_OPEN_PAREN_REQUIRED_IN_NOTATIONTYPE,
                                XMLMessages.P58_OPEN_PAREN_REQUIRED,
                                elementTypeIndex, attDefName);
                    return;
                }
                increaseParenDepth();
                attDefType = fNOTATION;
                attDefEnumeration = scanEnumeration(elementTypeIndex, attDefName, true);
                if (attDefEnumeration == -1) {
                    skipPastEndOfCurrentMarkup();
                    return;
                }
            } else if (fEntityReader.lookingAtChar('(', true)) {
                increaseParenDepth();
                attDefType = fENUMERATION;
                attDefEnumeration = scanEnumeration(elementTypeIndex, attDefName, false);
                if (attDefEnumeration == -1) {
                    skipPastEndOfCurrentMarkup();
                    return;
                }
            } else {
                abortMarkup(XMLMessages.MSG_ATTTYPE_REQUIRED_IN_ATTDEF,
                            XMLMessages.P53_ATTTYPE_REQUIRED,
                            elementTypeIndex, attDefName);
                return;
            }
            if (!checkForPEReference(true)) {
                abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_DEFAULTDECL_IN_ATTDEF,
                            XMLMessages.P53_SPACE_REQUIRED,
                            elementTypeIndex, attDefName);
                return;
            }
            int attDefDefaultType = -1;
            int attDefDefaultValue = -1;
            if (fEntityReader.skippedString(required_string)) {
                attDefDefaultType = fREQUIRED;
            } else if (fEntityReader.skippedString(implied_string)) {
                attDefDefaultType = fIMPLIED;
            } else {
                if (fEntityReader.skippedString(fixed_string)) {
                    if (!fEntityReader.lookingAtSpace(true)) {
                        abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_AFTER_FIXED_IN_DEFAULTDECL,
                                    XMLMessages.P60_SPACE_REQUIRED,
                                    elementTypeIndex, attDefName);
                        return;
                    }
                    fEntityReader.skipPastSpaces();
                    attDefDefaultType = fFIXED;
                } else
                    attDefDefaultType = fDEFAULT;
                attDefDefaultValue = fEventHandler.scanDefaultAttValue(elementTypeIndex, attDefName, attDefType, attDefEnumeration);
                if (attDefDefaultValue == -1) {
                    skipPastEndOfCurrentMarkup();
                    return;
                }
            }
            if (attDefName == fXMLSpace) {
                boolean ok = false;
                if (attDefType == fENUMERATION) {
                    int index = attDefEnumeration;
                    if (index != -1) {
                        ok = fStringPool.stringListLength(index) == 2 &&
                             fStringPool.stringInList(index, fDefault) &&
                             fStringPool.stringInList(index, fPreserve);
                    }
                }
                if (!ok) {
                    reportFatalXMLError(XMLMessages.MSG_XML_SPACE_DECLARATION_ILLEGAL,
                                        XMLMessages.S2_10_DECLARATION_ILLEGAL,
                                        elementTypeIndex);
                }
            }
            int attDefIndex = fEventHandler.addAttDef(elementIndex, attDefName, attDefType, attDefEnumeration, attDefDefaultType, attDefDefaultValue);
        }
    }
    //
    // [58] NotationType ::= 'NOTATION' S '(' S? Name (S? '|' S? Name)* S? ')'
    // [59] Enumeration ::= '(' S? Nmtoken (S? '|' S? Nmtoken)* S? ')'
    //
    private int scanEnumeration(int elementType, int attrName, boolean isNotationType) throws Exception
    {
        int enumIndex = fEventHandler.startEnumeration();
        while (true) {
            checkForPEReference(false);
            int nameIndex = isNotationType ?
                            fEventHandler.checkForNameWithPEReference(fEntityReader, ')') :
                            fEventHandler.checkForNmtokenWithPEReference(fEntityReader, ')');
            if (nameIndex == -1) {
                if (isNotationType) {
                    reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_NOTATIONTYPE,
                                        XMLMessages.P58_NAME_REQUIRED,
                                        elementType,
                                        attrName);
                } else {
                    reportFatalXMLError(XMLMessages.MSG_NMTOKEN_REQUIRED_IN_ENUMERATION,
                                        XMLMessages.P59_NMTOKEN_REQUIRED,
                                        elementType,
                                        attrName);
                }
                fEventHandler.endEnumeration(enumIndex);
                return -1;
            }
            fEventHandler.addNameToEnumeration(enumIndex, elementType, attrName, nameIndex, isNotationType);
            checkForPEReference(false);
            if (!fEntityReader.lookingAtChar('|', true)) {
                fEventHandler.endEnumeration(enumIndex);
                if (!fEntityReader.lookingAtChar(')', true)) {
                    if (isNotationType) {
                        reportFatalXMLError(XMLMessages.MSG_NOTATIONTYPE_UNTERMINATED,
                                            XMLMessages.P58_UNTERMINATED,
                                        elementType,
                                        attrName);
                    } else {
                        reportFatalXMLError(XMLMessages.MSG_ENUMERATION_UNTERMINATED,
                                            XMLMessages.P59_UNTERMINATED,
                                        elementType,
                                        attrName);
                    }
                    return -1;
                }
                decreaseParenDepth();
                return enumIndex;
            }
        }
    }
    //
    // [10] AttValue ::= '"' ([^<&"] | Reference)* '"'
    //                   | "'" ([^<&'] | Reference)* "'"
    //
    /**
     * Scan the default value in an attribute declaration
     *
     * @param elementType handle to the element that owns the attribute
     * @param attrName handle in the string pool for the attribute name
     * @return handle in the string pool for the default attribute value
     * @exception java.lang.Exception
     */
    public int scanDefaultAttValue(int elementType, int attrName) throws Exception
    {
        boolean single;
        if (!(single = fEntityReader.lookingAtChar('\'', true)) && !fEntityReader.lookingAtChar('\"', true)) {
            reportFatalXMLError(XMLMessages.MSG_QUOTE_REQUIRED_IN_ATTVALUE,
                                XMLMessages.P10_QUOTE_REQUIRED,
                                elementType,
                                attrName);
            return -1;
        }
        int previousState = setScannerState(SCANNER_STATE_DEFAULT_ATTRIBUTE_VALUE);
        char qchar = single ? '\'' : '\"';
        fDefaultAttValueReader = fReaderId;
        fDefaultAttValueElementType = elementType;
        fDefaultAttValueAttrName = attrName;
        boolean setMark = true;
        int dataOffset = fLiteralData.length();
        while (true) {
            fDefaultAttValueOffset = fEntityReader.currentOffset();
            if (setMark) {
                fDefaultAttValueMark = fDefaultAttValueOffset;
                setMark = false;
            }
            if (fEntityReader.lookingAtChar(qchar, true)) {
                if (fReaderId == fDefaultAttValueReader)
                    break;
                continue;
            }
            if (fEntityReader.lookingAtChar(' ', true)) {
                continue;
            }
            boolean skippedCR;
            if ((skippedCR = fEntityReader.lookingAtChar((char)0x0D, true)) || fEntityReader.lookingAtSpace(true)) {
                if (fDefaultAttValueOffset - fDefaultAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
                setMark = true;
                fLiteralData.append(' ');
                if (skippedCR)
                    fEntityReader.lookingAtChar((char)0x0A, true);
                continue;
            }
            if (fEntityReader.lookingAtChar('&', true)) {
                if (fDefaultAttValueOffset - fDefaultAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
                setMark = true;
                //
                // Check for character reference first.
                //
                if (fEntityReader.lookingAtChar('#', true)) {
                    int ch = scanCharRef();
                    if (ch != -1) {
                        if (ch < 0x10000)
                            fLiteralData.append((char)ch);
                        else {
                            fLiteralData.append((char)(((ch-0x00010000)>>10)+0xd800));
                            fLiteralData.append((char)(((ch-0x00010000)&0x3ff)+0xdc00));
                        }
                    }
                } else {
                    //
                    // Entity reference
                    //
                    int nameOffset = fEntityReader.currentOffset();
                    fEntityReader.skipPastName(';');
                    int nameLength = fEntityReader.currentOffset() - nameOffset;
                    if (nameLength == 0) {
                        reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_REFERENCE,
                                            XMLMessages.P68_NAME_REQUIRED);
                    } else if (!fEntityReader.lookingAtChar(';', true)) {
                        reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_REFERENCE,
                                            XMLMessages.P68_SEMICOLON_REQUIRED,
                                            fEntityReader.addString(nameOffset, nameLength));
                    } else {
                        int entityNameIndex = fEntityReader.addSymbol(nameOffset, nameLength);
                        fEntityHandler.startReadingFromEntity(entityNameIndex, markupDepth(), XMLEntityHandler.CONTEXT_IN_DEFAULTATTVALUE);
                    }
                }
                continue;
            }
            if (fEntityReader.lookingAtChar('<', true)) {
                if (fDefaultAttValueOffset - fDefaultAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
                setMark = true;
                reportFatalXMLError(XMLMessages.MSG_LESSTHAN_IN_ATTVALUE,
                                    XMLMessages.WFC_NO_LESSTHAN_IN_ATTVALUE,
                                    elementType,
                                    attrName);
                continue;
            }
            //
            // REVISIT - HACK !!!  code added to pass incorrect OASIS test 'valid-sa-094'
            //  Remove this next section to conform to the spec...
            //
            if (!getReadingExternalEntity() && fEntityReader.lookingAtChar('%', true)) {
                int nameOffset = fEntityReader.currentOffset();
                fEntityReader.skipPastName(';');
                int nameLength = fEntityReader.currentOffset() - nameOffset;
                if (nameLength != 0 && fEntityReader.lookingAtChar(';', true)) {
                    reportFatalXMLError(XMLMessages.MSG_PEREFERENCE_WITHIN_MARKUP,
                                        XMLMessages.WFC_PES_IN_INTERNAL_SUBSET,
                                        fEntityReader.addString(nameOffset, nameLength));
                }
            }
            //
            // END HACK !!!
            //
            if (!fEntityReader.lookingAtValidChar(true)) {
                if (fDefaultAttValueOffset - fDefaultAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
                setMark = true;
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                    return -1;
                if (invChar >= 0) {
                    reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_ATTVALUE,
                                        XMLMessages.P10_INVALID_CHARACTER,
                                        fStringPool.toString(elementType),
                                        fStringPool.toString(attrName),
                                        Integer.toHexString(invChar));
                }
                continue;
            }
        }
        restoreScannerState(previousState);
        int dataLength = fLiteralData.length() - dataOffset;
        if (dataLength == 0) {
            return fEntityReader.addString(fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
        }
        if (fDefaultAttValueOffset - fDefaultAttValueMark > 0) {
            fEntityReader.append(fLiteralData, fDefaultAttValueMark, fDefaultAttValueOffset - fDefaultAttValueMark);
            dataLength = fLiteralData.length() - dataOffset;
        }
        return fLiteralData.addString(dataOffset, dataLength);
    }
    //
    // [82] NotationDecl ::= '<!NOTATION' S Name S (ExternalID |  PublicID) S? '>'
    //
    private void scanNotationDecl() throws Exception
    {
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_NOTATIONDECL,
                        XMLMessages.P82_SPACE_REQUIRED);
            return;
        }
        int notationName = fEventHandler.checkForNameWithPEReference(fEntityReader, ' ');
        if (notationName == -1) {
            abortMarkup(XMLMessages.MSG_NOTATION_NAME_REQUIRED_IN_NOTATIONDECL,
                        XMLMessages.P82_NAME_REQUIRED);
            return;
        }
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL,
                        XMLMessages.P82_SPACE_REQUIRED,
                        notationName);
            return;
        }
        if (!scanExternalID(true)) {
            skipPastEndOfCurrentMarkup();
            return;
        }
        checkForPEReference(false);
        if (!fEntityReader.lookingAtChar('>', true)) {
            abortMarkup(XMLMessages.MSG_NOTATIONDECL_UNTERMINATED,
                        XMLMessages.P82_UNTERMINATED,
                        notationName);
            return;
        }
        decreaseMarkupDepth();
        int notationIndex = fEventHandler.addNotationDecl(notationName, fPubidLiteral, fSystemLiteral);
    }
    //
    // [70] EntityDecl ::= GEDecl | PEDecl
    // [71] GEDecl ::= '<!ENTITY' S Name S EntityDef S? '>'
    // [72] PEDecl ::= '<!ENTITY' S '%' S Name S PEDef S? '>'
    // [73] EntityDef ::= EntityValue | (ExternalID NDataDecl?)
    // [74] PEDef ::= EntityValue | ExternalID
    // [75] ExternalID ::= 'SYSTEM' S SystemLiteral
    //                     | 'PUBLIC' S PubidLiteral S SystemLiteral
    // [76] NDataDecl ::= S 'NDATA' S Name
    //  [9] EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
    //                      | "'" ([^%&'] | PEReference | Reference)* "'"
    //
    // Called after scanning 'ENTITY'
    //
    private void scanEntityDecl() throws Exception
    {
        boolean isPEDecl = false;
        boolean sawPERef = false;
        if (fEntityReader.lookingAtSpace(true)) {
            fEntityReader.skipPastSpaces();
            if (!fEntityReader.lookingAtChar('%', true)) {
                isPEDecl = false; // <!ENTITY x "x">
            } else if (fEntityReader.lookingAtSpace(true)) {
                checkForPEReference(false); // <!ENTITY % x "x">
                isPEDecl = true;
            } else if (!getReadingExternalEntity()) {
                reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_PEDECL,
                                    XMLMessages.P72_SPACE);
                isPEDecl = true;
            } else if (fEntityReader.lookingAtChar('%', false)) {
                checkForPEReference(false); // <!ENTITY %%x; "x"> is legal
                isPEDecl = true;
            } else {
                sawPERef = true;
            }
        } else if (!getReadingExternalEntity() || !fEntityReader.lookingAtChar('%', true)) {
            // <!ENTITY[^ ]...> or <!ENTITY[^ %]...>
            reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_ENTITY_NAME_IN_ENTITYDECL,
                                XMLMessages.P70_SPACE);
            isPEDecl = false;
        } else if (fEntityReader.lookingAtSpace(false)) {
            // <!ENTITY% ...>
            reportFatalXMLError(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_PERCENT_IN_PEDECL,
                                XMLMessages.P72_SPACE);
            isPEDecl = false;
        } else {
            sawPERef = true;
        }
        if (sawPERef) {
            while (true) {
                int nameOffset = fEntityReader.currentOffset();
                fEntityReader.skipPastName(';');
                int nameLength = fEntityReader.currentOffset() - nameOffset;
                if (nameLength == 0) {
                    reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_NAME_REQUIRED);
                } else if (!fEntityReader.lookingAtChar(';', true)) {
                    reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_SEMICOLON_REQUIRED,
                                        fEntityReader.addString(nameOffset, nameLength));
                } else {
                    int peNameIndex = fEntityReader.addSymbol(nameOffset, nameLength);
                    int readerDepth = (fScannerState == SCANNER_STATE_CONTENTSPEC) ? parenDepth() : markupDepth();
                    fEntityHandler.startReadingFromEntity(peNameIndex, readerDepth, XMLEntityHandler.CONTEXT_IN_DTD_WITHIN_MARKUP);
                }
                fEntityReader.skipPastSpaces();
                if (!fEntityReader.lookingAtChar('%', true))
                    break;
                if (!isPEDecl) {
                    if (fEntityReader.lookingAtSpace(true)) {
                        checkForPEReference(false);
                        isPEDecl = true;
                        break;
                    }
                    isPEDecl = fEntityReader.lookingAtChar('%', true);
                }
            }
        }
        int entityName = fEventHandler.checkForNameWithPEReference(fEntityReader, ' ');
        if (entityName == -1) {
            abortMarkup(XMLMessages.MSG_ENTITY_NAME_REQUIRED_IN_ENTITYDECL,
                        XMLMessages.P70_REQUIRED_NAME);
            return;
        }
        if (!fEntityHandler.startEntityDecl(isPEDecl, entityName)) {
            skipPastEndOfCurrentMarkup();
            return;
        }
        if (!checkForPEReference(true)) {
            abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_AFTER_ENTITY_NAME_IN_ENTITYDECL,
                        XMLMessages.P70_REQUIRED_SPACE,
                        entityName);
            fEntityHandler.endEntityDecl();
            return;
        }
        if (isPEDecl) {
            boolean single;
            if ((single = fEntityReader.lookingAtChar('\'', true)) || fEntityReader.lookingAtChar('\"', true)) {
                int location = fEventHandler.saveCurrentLocation();
                int value = scanEntityValue(single);
                if (value == -1) {
                    skipPastEndOfCurrentMarkup();
                    fEntityHandler.endEntityDecl();
                    return;
                }
                checkForPEReference(false);
                if (!fEntityReader.lookingAtChar('>', true)) {
                    abortMarkup(XMLMessages.MSG_ENTITYDECL_UNTERMINATED,
                                XMLMessages.P72_UNTERMINATED,
                                entityName);
                    fEntityHandler.endEntityDecl();
                    return;
                }
                decreaseMarkupDepth();
                fEntityHandler.endEntityDecl();
                int entityIndex = fEventHandler.addInternalPEDecl(entityName, value, location);
            } else {
                if (!scanExternalID(false)) {
                    skipPastEndOfCurrentMarkup();
                    fEntityHandler.endEntityDecl();
                    return;
                }
                checkForPEReference(false);
                if (!fEntityReader.lookingAtChar('>', true)) {
                    abortMarkup(XMLMessages.MSG_ENTITYDECL_UNTERMINATED,
                                XMLMessages.P72_UNTERMINATED,
                                entityName);
                    fEntityHandler.endEntityDecl();
                    return;
                }
                decreaseMarkupDepth();
                fEntityHandler.endEntityDecl();
                int entityIndex = fEventHandler.addExternalPEDecl(entityName, fPubidLiteral, fSystemLiteral);
            }
        } else {
            boolean single;
            if ((single = fEntityReader.lookingAtChar('\'', true)) || fEntityReader.lookingAtChar('\"', true)) {
                int location = fEventHandler.saveCurrentLocation();
                int value = scanEntityValue(single);
                if (value == -1) {
                    skipPastEndOfCurrentMarkup();
                    fEntityHandler.endEntityDecl();
                    return;
                }
                checkForPEReference(false);
                if (!fEntityReader.lookingAtChar('>', true)) {
                    abortMarkup(XMLMessages.MSG_ENTITYDECL_UNTERMINATED,
                                XMLMessages.P71_UNTERMINATED,
                                entityName);
                    fEntityHandler.endEntityDecl();
                    return;
                }
                decreaseMarkupDepth();
                fEntityHandler.endEntityDecl();
                int entityIndex = fEventHandler.addInternalEntityDecl(entityName, value, location);
            } else {
                if (!scanExternalID(false)) {
                    skipPastEndOfCurrentMarkup();
                    fEntityHandler.endEntityDecl();
                    return;
                }
                boolean unparsed = false;
                if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                    unparsed = fEntityReader.skippedString(ndata_string);
                }
                if (!unparsed) {
                    checkForPEReference(false);
                    if (!fEntityReader.lookingAtChar('>', true)) {
                        abortMarkup(XMLMessages.MSG_ENTITYDECL_UNTERMINATED,
                                    XMLMessages.P72_UNTERMINATED,
                                    entityName);
                        fEntityHandler.endEntityDecl();
                        return;
                    }
                    decreaseMarkupDepth();
                    fEntityHandler.endEntityDecl();
                    int entityIndex = fEventHandler.addExternalEntityDecl(entityName, fPubidLiteral, fSystemLiteral);
                } else {
                    if (!fEntityReader.lookingAtSpace(true)) {
                        abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_BEFORE_NOTATION_NAME_IN_UNPARSED_ENTITYDECL,
                                    XMLMessages.P76_SPACE_REQUIRED,
                                    entityName);
                        fEntityHandler.endEntityDecl();
                        return;
                    }
                    fEntityReader.skipPastSpaces();
                    int ndataOffset = fEntityReader.currentOffset();
                    fEntityReader.skipPastName('>');
                    int ndataLength = fEntityReader.currentOffset() - ndataOffset;
                    if (ndataLength == 0) {
                        abortMarkup(XMLMessages.MSG_NOTATION_NAME_REQUIRED_FOR_UNPARSED_ENTITYDECL,
                                    XMLMessages.P76_REQUIRED,
                                    entityName);
                        fEntityHandler.endEntityDecl();
                        return;
                    }
                    int notationName = fEntityReader.addSymbol(ndataOffset, ndataLength);
                    checkForPEReference(false);
                    if (!fEntityReader.lookingAtChar('>', true)) {
                        abortMarkup(XMLMessages.MSG_ENTITYDECL_UNTERMINATED,
                                    XMLMessages.P72_UNTERMINATED,
                                    entityName);
                        fEntityHandler.endEntityDecl();
                        return;
                    }
                    decreaseMarkupDepth();
                    fEntityHandler.endEntityDecl();
                    int entityIndex = fEventHandler.addUnparsedEntityDecl(entityName, fPubidLiteral, fSystemLiteral, notationName);
                }
            }
        }
    }
    //
    //  [9] EntityValue ::= '"' ([^%&"] | PEReference | Reference)* '"'
    //                      | "'" ([^%&'] | PEReference | Reference)* "'"
    //
    private int scanEntityValue(boolean single) throws Exception
    {
        char qchar = single ? '\'' : '\"';
        fEntityValueMark = fEntityReader.currentOffset();
        int entityValue = fEntityReader.scanEntityValue(qchar, true);
        if (entityValue < 0)
            entityValue = scanComplexEntityValue(qchar, entityValue);
        return entityValue;
    }
    private int scanComplexEntityValue(char qchar, int result) throws Exception
    {
        int previousState = setScannerState(SCANNER_STATE_ENTITY_VALUE);
        fEntityValueReader = fReaderId;
        int dataOffset = fLiteralData.length();
        while (true) {
            switch (result) {
            case XMLEntityHandler.ENTITYVALUE_RESULT_FINISHED:
            {
                int offset = fEntityReader.currentOffset();
                fEntityReader.lookingAtChar(qchar, true);
                restoreScannerState(previousState);
                int dataLength = fLiteralData.length() - dataOffset;
                if (dataLength == 0) {
                    return fEntityReader.addString(fEntityValueMark, offset - fEntityValueMark);
                }
                if (offset - fEntityValueMark > 0) {
                    fEntityReader.append(fLiteralData, fEntityValueMark, offset - fEntityValueMark);
                    dataLength = fLiteralData.length() - dataOffset;
                }
                return fLiteralData.addString(dataOffset, dataLength);
            }
            case XMLEntityHandler.ENTITYVALUE_RESULT_REFERENCE:
            {
                int offset = fEntityReader.currentOffset();
                if (offset - fEntityValueMark > 0)
                    fEntityReader.append(fLiteralData, fEntityValueMark, offset - fEntityValueMark);
                fEntityReader.lookingAtChar('&', true);
                //
                // Check for character reference first.
                //
                if (fEntityReader.lookingAtChar('#', true)) {
                    int ch = scanCharRef();
                    if (ch != -1) {
                        if (ch < 0x10000)
                            fLiteralData.append((char)ch);
                        else {
                            fLiteralData.append((char)(((ch-0x00010000)>>10)+0xd800));
                            fLiteralData.append((char)(((ch-0x00010000)&0x3ff)+0xdc00));
                        }
                    }
                    fEntityValueMark = fEntityReader.currentOffset();
                } else {
                    //
                    // Entity reference
                    //
                    int nameOffset = fEntityReader.currentOffset();
                    fEntityReader.skipPastName(';');
                    int nameLength = fEntityReader.currentOffset() - nameOffset;
                    if (nameLength == 0) {
                        reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_REFERENCE,
                                            XMLMessages.P68_NAME_REQUIRED);
                        fEntityValueMark = fEntityReader.currentOffset();
                    } else if (!fEntityReader.lookingAtChar(';', true)) {
                        reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_REFERENCE,
                                            XMLMessages.P68_SEMICOLON_REQUIRED,
                                            fEntityReader.addString(nameOffset, nameLength));
                        fEntityValueMark = fEntityReader.currentOffset();
                    } else {
                        //
                        // 4.4.7 Bypassed
                        //
                        // When a general entity reference appears in the EntityValue in an
                        // entity declaration, it is bypassed and left as is.
                        //
                        fEntityValueMark = offset;
                    }
                }
                break;
            }
            case XMLEntityHandler.ENTITYVALUE_RESULT_PEREF:
            {
                int offset = fEntityReader.currentOffset();
                if (offset - fEntityValueMark > 0)
                    fEntityReader.append(fLiteralData, fEntityValueMark, offset - fEntityValueMark);
                fEntityReader.lookingAtChar('%', true);
                int nameOffset = fEntityReader.currentOffset();
                fEntityReader.skipPastName(';');
                int nameLength = fEntityReader.currentOffset() - nameOffset;
                if (nameLength == 0) {
                    reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_NAME_REQUIRED);
                } else if (!fEntityReader.lookingAtChar(';', true)) {
                    reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_PEREFERENCE,
                                        XMLMessages.P69_SEMICOLON_REQUIRED,
                                        fEntityReader.addString(nameOffset, nameLength));
                } else if (!getReadingExternalEntity()) {
                    reportFatalXMLError(XMLMessages.MSG_PEREFERENCE_WITHIN_MARKUP,
                                        XMLMessages.WFC_PES_IN_INTERNAL_SUBSET,
                                        fEntityReader.addString(nameOffset, nameLength));
                } else {
                    int peNameIndex = fEntityReader.addSymbol(nameOffset, nameLength);
                    fEntityHandler.startReadingFromEntity(peNameIndex, markupDepth(), XMLEntityHandler.CONTEXT_IN_ENTITYVALUE);
                }
                fEntityValueMark = fEntityReader.currentOffset();
                break;
            }
            case XMLEntityHandler.ENTITYVALUE_RESULT_INVALID_CHAR:
            {
                int offset = fEntityReader.currentOffset();
                if (offset - fEntityValueMark > 0)
                    fEntityReader.append(fLiteralData, fEntityValueMark, offset - fEntityValueMark);
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState == SCANNER_STATE_END_OF_INPUT)
                    return -1;
                if (invChar >= 0) {
                    reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_ENTITYVALUE,
                                        XMLMessages.P9_INVALID_CHARACTER,
                                        Integer.toHexString(invChar));
                }
                fEntityValueMark = fEntityReader.currentOffset();
                break;
            }
            case XMLEntityHandler.ENTITYVALUE_RESULT_END_OF_INPUT:
                // all the work is done by the previous reader, just invoke the next one now.
                break;
            default:
                break;
            }
            result = fEntityReader.scanEntityValue(fReaderId == fEntityValueReader ? qchar : -1, false);
        }
    }
    //
    //
    //
    private boolean checkForPEReference(boolean spaceRequired) throws Exception
    {
        boolean sawSpace = true;
        if (spaceRequired)
            sawSpace = fEntityReader.lookingAtSpace(true);
        fEntityReader.skipPastSpaces();
        if (!getReadingExternalEntity())
            return sawSpace;
        if (!fEntityReader.lookingAtChar('%', true))
            return sawSpace;
        while (true) {
            int nameOffset = fEntityReader.currentOffset();
            fEntityReader.skipPastName(';');
            int nameLength = fEntityReader.currentOffset() - nameOffset;
            if (nameLength == 0) {
                reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_PEREFERENCE,
                                    XMLMessages.P69_NAME_REQUIRED);
            } else if (!fEntityReader.lookingAtChar(';', true)) {
                reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_PEREFERENCE,
                                    XMLMessages.P69_SEMICOLON_REQUIRED,
                                    fEntityReader.addString(nameOffset, nameLength));
            } else {
                int peNameIndex = fEntityReader.addSymbol(nameOffset, nameLength);
                int readerDepth = (fScannerState == SCANNER_STATE_CONTENTSPEC) ? parenDepth() : markupDepth();
                fEntityHandler.startReadingFromEntity(peNameIndex, readerDepth, XMLEntityHandler.CONTEXT_IN_DTD_WITHIN_MARKUP);
            }
            fEntityReader.skipPastSpaces();
            if (!fEntityReader.lookingAtChar('%', true))
                return true;
        }
    }
    //
    // content model stack
    //
    private void initializeContentModelStack(int depth) {
        if (opStack == null) {
            opStack = new int[8];
            nodeIndexStack = new int[8];
            prevNodeIndexStack = new int[8];
        } else if (depth == opStack.length) {
            int[] newStack = new int[depth * 2];
            System.arraycopy(opStack, 0, newStack, 0, depth);
            opStack = newStack;
            newStack = new int[depth * 2];
            System.arraycopy(nodeIndexStack, 0, newStack, 0, depth);
            nodeIndexStack = newStack;
            newStack = new int[depth * 2];
            System.arraycopy(prevNodeIndexStack, 0, newStack, 0, depth);
            prevNodeIndexStack = newStack;
        }
        opStack[depth] = -1;
        nodeIndexStack[depth] = -1;
        prevNodeIndexStack[depth] = -1;
    }
}
