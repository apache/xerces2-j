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
 * This class recognizes most of the grammer for an XML processor.
 * Additional support is provided by the XMLEntityHandler, via the
 * XMLEntityReader instances it creates, which are used to process
 * simple constructs like string literals and character data between
 * markup.  The XMLDTDScanner class contains the remaining support
 * for the grammer of DTD declarations.  When a &lt;!DOCTYPE ...&gt; is
 * found in the document, the scanDoctypeDecl method will then be
 * called and the XMLDocumentScanner subclass is responsible for
 * "connecting" that method to the corresponding method provided
 * by the XMLDTDScanner class.
 *
 * @version
 */
public final class XMLDocumentScanner {
    //
    // Constants
    //

    //
    // These character arrays are used as parameters for calls to the
    // XMLEntityHandler.EntityReader skippedString() method.  Some have
    // package access for use by the inner dispatcher classes.
    //

    //
    // [19] CDStart ::= '<![CDATA['
    //
    static final char[] cdata_string = { '[','C','D','A','T','A','[' };
    //
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
    //
    static final char[] xml_string = { 'x','m','l' };
    //
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    //
    private static final char[] version_string = { 'v','e','r','s','i','o','n' };
    //
    // [28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S?
    //                      ('[' (markupdecl | PEReference | S)* ']' S?)? '>'
    //
    static final char[] doctype_string = { 'D','O','C','T','Y','P','E' };
    //
    // [32] SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
    //                 | ('"' ('yes' | 'no') '"'))
    //
    private static final char[] standalone_string = { 's','t','a','n','d','a','l','o','n','e' };
    //
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    //
    private static final char[] encoding_string = { 'e','n','c','o','d','i','n','g' };

    /*
     * Return values for the EventHandler scanAttValue method.
     */
    public static final int
        RESULT_SUCCESS          =  0,
        RESULT_FAILURE          = -1,
        RESULT_DUPLICATE_ATTR   = -2;

    /** Scanner states */
    static final int
        SCANNER_STATE_XML_DECL                  =  0,
        SCANNER_STATE_START_OF_MARKUP           =  1,
        SCANNER_STATE_COMMENT                   =  2,
        SCANNER_STATE_PI                        =  3,
        SCANNER_STATE_DOCTYPE                   =  4,
        SCANNER_STATE_PROLOG                    =  5,
        SCANNER_STATE_ROOT_ELEMENT              =  6,
        SCANNER_STATE_CONTENT                   =  7,
        SCANNER_STATE_REFERENCE                 =  8,
        SCANNER_STATE_ATTRIBUTE_LIST            =  9,
        SCANNER_STATE_ATTRIBUTE_NAME            = 10,
        SCANNER_STATE_ATTRIBUTE_VALUE           = 11,
        SCANNER_STATE_TRAILING_MISC             = 12,
        SCANNER_STATE_END_OF_INPUT              = 13,
        SCANNER_STATE_TERMINATED                = 14;

    //
    // Instance Variables
    //
    ScannerDispatcher fDispatcher = null;
    EventHandler fEventHandler = null;
    StringPool fStringPool = null;
    XMLErrorReporter fErrorReporter = null;
    XMLEntityHandler fEntityHandler = null;
    XMLEntityHandler.EntityReader fEntityReader = null;
    XMLEntityHandler.CharBuffer fLiteralData = null;
    boolean fSeenRootElement = false;
    boolean fSeenDoctypeDecl = false;
    boolean fStandalone = false;
    boolean fParseTextDecl = false;
    int fScannerState = SCANNER_STATE_XML_DECL;
    int fReaderId = -1;
    int fAttValueReader = -1;
    int fAttValueElementType = -1;
    int fAttValueAttrName = -1;
    int fAttValueOffset = -1;
    int fAttValueMark = -1;
    int fScannerMarkupDepth = 0;

    //
    // Interfaces
    //

    /**
     * This interface must be implemented by the users of the XMLDocumentScanner class.
     * These methods form the abstraction between the implementation semantics and the
     * more generic task of scanning the XML non-DTD grammar.
     */
    public interface EventHandler {
        /**
         * Scan an element type.
         *
         * If namespaces are supported, this method should scan a QName instead of a
         * Name and report any related well-formedness errors.
         *
         * @param entityReader The entity reader.
         * @param fastchar A likely non-name character that might terminate the element type.
         * @return The handle in the string pool for the element type scanned, or -1 if
         *         we were not able to scan an element type at the current location.
         * @exception java.lang.Exception
         */
        public int scanElementType(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
        /**
         * Scan the expected element type.
         *
         * This method is used to scan the matching end tag for the element type at the current
         * nesting depth of the document.
         *
         * @param entityReader The entity reader.
         * @param fastchar A likely non-name character that might terminate the element type.
         * @return <code>true</code> if we scanned the expected element type; otherwise
         *         <code>false</code> if we were not able to scan an element type at the
         *         current location, or if that element type was not the one we expected to
         *         find.
         * @exception java.lang.Exception
         */
        public boolean scanExpectedElementType(XMLEntityHandler.EntityReader entityReader, char fastchar) throws Exception;
        /**
         * Scan an attribute name.
         *
         * If namespaces are supported, this method should scan a QName instead of a
         * Name and report any related well-formedness errors.
         *
         * @param entityReader The entity reader.
         * @param elementType The element type for this attribute.
         * @return The handle in the string pool for the attribute name scanned, or -1 if
         *         we were not able to scan an attribute name at the current location.
         * @exception java.lang.Exception
         */
        public int scanAttributeName(XMLEntityHandler.EntityReader entityReader, int elementType) throws Exception;
        /**
         * Signal the start of a document
         *
         * @param version the handle in the string pool for the version number
         * @param encoding the handle in the string pool for the encoding
         * @param standalong the handle in the string pool for the standalone value
         * @exception java.lang.Exception
         */
        public void callStartDocument(int version, int encoding, int standalone) throws Exception;
        /**
         * Signal the end of a document
         *
         * @exception java.lang.Exception
         */
        public void callEndDocument() throws Exception;
        /**
         * signal the scanning of a start element tag
         * 
         * @param elementType handle to the elementType being scanned
         * @exception java.lang.Exception
         */
        public void callStartElement(int elementType) throws Exception;
        /**
         * signal the scanning of an end element tag
         *
         * @param readerId the Id of the reader being used to scan the end tag.
         * @exception java.lang.Exception
         */
        public boolean callEndElement(int readerId) throws Exception;
        /**
         * Check for a valid XML version number
         *
         * @param version a string representing an XML version number
         * @return true if the parser can process this version of XML
         * @exception java.lang.Exception
         */
        public boolean validVersionNum(String version) throws Exception;
        /**
         * Check for a valid encoding name
         *
         * @param encoding a string containing an encoding naem
         * @return true if the encoding name is valid
         * @exception java.lang.Exception
         */
        public boolean validEncName(String encoding) throws Exception;
        /**
         * Signal the start of a CDATA section
         * @exception java.lang.Exception
         */
        public void startCDATA() throws Exception;
        /**
         * Signal the end of a CDATA section
         * @exception java.lang.Exception
         */
        public void endCDATA() throws Exception;
        /**
         * Report the scanning of character data
         *
         * @param ch the handle in the string pool of the character data that was scanned
         * @exception java.lang.Exception
         */
        public void callCharacters(int ch) throws Exception;
        /**
         * Report the scanning of a processing instruction
         *
         * @param piTarget the handle in the string pool of the processing instruction targe
         * @param piData the handle in the string pool of the processing instruction data
         * @exception java.lang.Exception
         */
        public void callProcessingInstruction(int piTarget, int piData) throws Exception;
        /**
         * Report the scanning of a comment
         *
         * @param data the handle in the string pool of the comment text
         * @exception java.lang.Exception
         */
        public void callComment(int data) throws Exception;
        /**
         * Scan the document type declaration
         *
         * @param standalone true if there was a standalone document declaration in
         *        the XMLDecl
         * @exception java.lang.Exception
         */
        public void scanDoctypeDecl(boolean standalone) throws Exception;
        /**
         * Scan the value of an attribute and include it in the set of specified
         * attributes for the element.
         *
         * @param elementType handle for the element type of the attribute.
         * @param attrName handle for the attribute name.
         * @return XMLDocumentScanner.RESULT_SUCCESS if the attribute was created,
         *         XMLDocumentScanner.RESULT_NOT_WELL_FORMED if the scan failed, or
         *         XMLDocumentScanner.RESULT_DUPLICATE_ATTR if the attribute is a duplicate.
         * @exception java.lang.Exception
         */
        public int scanAttValue(int elementType, int attrName) throws Exception;
    }

    /**
     * Constructor
     */
    public XMLDocumentScanner(EventHandler eventHandler,
                              StringPool stringPool,
                              XMLErrorReporter errorReporter,
                              XMLEntityHandler entityHandler,
                              XMLEntityHandler.CharBuffer literalData) {
        fEventHandler = eventHandler;
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fEntityHandler = entityHandler;
        fLiteralData = literalData;
        fDispatcher = new XMLDeclDispatcher();
    }

    /**
     * reset the parser so that the instance can be reused
     *
     * @param stringPool the string pool instance to be used by the reset parser
     */
    public void reset(StringPool stringPool, XMLEntityHandler.CharBuffer literalData) throws Exception {
        fStringPool = stringPool;
        fLiteralData = literalData;
        fParseTextDecl = false;
        fSeenRootElement = false;
        fSeenDoctypeDecl = false;
        fStandalone = false;
        fDispatcher = new XMLDeclDispatcher();
        fScannerState = SCANNER_STATE_XML_DECL;
        fScannerMarkupDepth = 0;
    }

    //
    // From the standard:
    //
    // [1] document ::= prolog element Misc*
    //
    // [22] prolog ::= XMLDecl? Misc* (doctypedecl Misc*)?
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    //
    // The beginning of XMLDecl simplifies to:
    //    '<?xml' S ...
    //
    // [27] Misc ::= Comment | PI |  S
    // [15] Comment ::= '<!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
    // [16] PI ::= '<?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
    // [17] PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
    //
    // [28] doctypedecl ::= '<!DOCTYPE' S Name (S ExternalID)? S?
    //                      ('[' (markupdecl | PEReference | S)* ']' S?)? '>'
    //
    /**
     * Entry point for parsing
     *
     * @param doItAll if true the entire document is parsed otherwise just 
     *                the next segment of the document is parsed
     */
    public boolean parseSome(boolean doItAll) throws Exception
    {
        do {
            if (!fDispatcher.dispatch(doItAll))
                return false;
        } while (doItAll);
        return true;
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
        if (fScannerState == SCANNER_STATE_ATTRIBUTE_VALUE) {
            fAttValueOffset = fEntityReader.currentOffset();
            fAttValueMark = fAttValueOffset;
        }
    }

    /**
     * Handle the end of input
     *
     * @param entityName the handle in the string pool of the name of the entity which has reached end of input
     * @param moreToFollow if true, there is still input left to process in other readers
     * @exception java.lang.Exception
     */
    public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
        fDispatcher.endOfInput(entityName, moreToFollow);
    }

    /** 
     * Tell if scanner has reached end of input
     * @return true if scanner has reached end of input.
     */
    public boolean atEndOfInput() {
        return fScannerState == SCANNER_STATE_END_OF_INPUT;
    }

    //
    // [10] AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] | Reference)* "'"
    //
    /**
     * Scan an attribute value
     *
     * @param elementType handle to the element whose attribute value is being scanned
     * @param attrName handle in the string pool of the name of attribute being scanned
     * @param asSymbol controls whether the value is a string (duplicates allowed) or a symbol (duplicates not allowed)
     * @return handle in the string pool of the scanned value
     * @exception java.lang.Exception
     */
    public int scanAttValue(int elementType, int attrName, boolean asSymbol) throws Exception {
        boolean single;
        if (!(single = fEntityReader.lookingAtChar('\'', true)) && !fEntityReader.lookingAtChar('\"', true)) {
            reportFatalXMLError(XMLMessages.MSG_QUOTE_REQUIRED_IN_ATTVALUE,
                                XMLMessages.P10_QUOTE_REQUIRED,
                                elementType,
                                attrName);
            return -1;
        }
        char qchar = single ? '\'' : '\"';
        fAttValueMark = fEntityReader.currentOffset();
        int attValue = fEntityReader.scanAttValue(qchar, asSymbol);
        if (attValue >= 0)
            return attValue;
        int previousState = setScannerState(SCANNER_STATE_ATTRIBUTE_VALUE);
        fAttValueReader = fReaderId;
        fAttValueElementType = elementType;
        fAttValueAttrName = attrName;
        fAttValueOffset = fEntityReader.currentOffset();
        int dataOffset = fLiteralData.length();
        if (fAttValueOffset - fAttValueMark > 0)
            fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
        fAttValueMark = fAttValueOffset;
        boolean setMark = false;
        boolean skippedCR;
        while (true) {
            if (fEntityReader.lookingAtChar(qchar, true)) {
                if (fReaderId == fAttValueReader)
                    break;
            } else if (fEntityReader.lookingAtChar(' ', true)) {
                //
                // no action required
                //
            } else if ((skippedCR = fEntityReader.lookingAtChar((char)0x0D, true)) || fEntityReader.lookingAtSpace(true)) {
                if (fAttValueOffset - fAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
                setMark = true;
                fLiteralData.append(' ');
                if (skippedCR) {
                    //
                    // REVISIT - HACK !!!  code changed to pass incorrect OASIS test 'valid-sa-110'
                    //  Uncomment the next line to conform to the spec...
                    //
                    //fEntityReader.lookingAtChar((char)0x0A, true);
                }
            } else if (fEntityReader.lookingAtChar('&', true)) {
                if (fAttValueOffset - fAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
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
                        int entityName = fEntityReader.addSymbol(nameOffset, nameLength);
                        fEntityHandler.startReadingFromEntity(entityName, fScannerMarkupDepth, XMLEntityHandler.CONTEXT_IN_ATTVALUE);
                    }
                }
            } else if (fEntityReader.lookingAtChar('<', true)) {
                if (fAttValueOffset - fAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
                setMark = true;
                reportFatalXMLError(XMLMessages.MSG_LESSTHAN_IN_ATTVALUE,
                                    XMLMessages.WFC_NO_LESSTHAN_IN_ATTVALUE,
                                    elementType,
                                    attrName);
            } else if (!fEntityReader.lookingAtValidChar(true)) {
                if (fAttValueOffset - fAttValueMark > 0)
                    fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
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
            }
            fAttValueOffset = fEntityReader.currentOffset();
            if (setMark) {
                fAttValueMark = fAttValueOffset;
                setMark = false;
            }
        }
        restoreScannerState(previousState);
        int dataLength = fLiteralData.length() - dataOffset;
        if (dataLength == 0) {
            return fEntityReader.addString(fAttValueMark, fAttValueOffset - fAttValueMark);
        }
        if (fAttValueOffset - fAttValueMark > 0) {
            fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
            dataLength = fLiteralData.length() - dataOffset;
        }
        return fLiteralData.addString(dataOffset, dataLength);
    }

    /**
     * Check the value of an XML Language attribute
     * @param langValue the handle in the string pool of the value to be checked
     * @exception java.lang.Exception
     */
    public void checkXMLLangAttributeValue(int langValue) throws Exception {
        String lang = fStringPool.toString(langValue);
        int offset = -1;
        if (lang.length() >= 2) {
            char ch0 = lang.charAt(0);
            if (lang.charAt(1) == '-') {
                if (ch0 == 'i' || ch0 == 'I' || ch0 == 'x' || ch0 == 'X') {
                    offset = 1;
                }
            } else {
                char ch1 = lang.charAt(1);
                if (((ch0 >= 'a' && ch0 <= 'z') || (ch0 >= 'A' && ch0 <= 'Z')) &&
                    ((ch1 >= 'a' && ch1 <= 'z') || (ch1 >= 'A' && ch1 <= 'Z'))) {
                        offset = 2;
                }
            }
        }
        if (offset > 0 && lang.length() > offset) {
            char ch = lang.charAt(offset++);
            if (ch != '-') {
                offset = -1;
            } else {
                while (true) {
                    if (ch == '-') {
                        if (lang.length() == offset) {
                            offset = -1;
                            break;
                        }
                        ch = lang.charAt(offset++);
                        if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                            offset = -1;
                            break;
                        }
                        if (lang.length() == offset)
                            break;
                    } else if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                        offset = -1;
                        break;
                    } else if (lang.length() == offset)
                        break;
                    ch = lang.charAt(offset++);
                }
            }
        }
        if (offset == -1) {
            reportFatalXMLError(XMLMessages.MSG_XML_LANG_INVALID,
                                XMLMessages.P33_INVALID,
                                lang);
        }
    }

    //
    //
    //
    void reportFatalXMLError(int majorCode, int minorCode) throws Exception {
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   null,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void reportFatalXMLError(int majorCode, int minorCode, int stringIndex1) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void reportFatalXMLError(int majorCode, int minorCode, String string1) throws Exception {
        Object[] args = { string1 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void reportFatalXMLError(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        Object[] args = { fStringPool.toString(stringIndex1),
                          fStringPool.toString(stringIndex2) };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void reportFatalXMLError(int majorCode, int minorCode, String string1, String string2) throws Exception {
        Object[] args = { string1, string2 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void reportFatalXMLError(int majorCode, int minorCode, String string1, String string2, String string3) throws Exception {
        Object[] args = { string1, string2, string3 };
        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                   XMLMessages.XML_DOMAIN,
                                   majorCode,
                                   minorCode,
                                   args,
                                   XMLErrorReporter.ERRORTYPE_FATAL_ERROR);
    }
    void abortMarkup(int majorCode, int minorCode) throws Exception {
        reportFatalXMLError(majorCode, minorCode);
        skipPastEndOfCurrentMarkup();
    }
    void abortMarkup(int majorCode, int minorCode, int stringIndex1) throws Exception {
        reportFatalXMLError(majorCode, minorCode, stringIndex1);
        skipPastEndOfCurrentMarkup();
    }
    void abortMarkup(int majorCode, int minorCode, String string1) throws Exception {
        reportFatalXMLError(majorCode, minorCode, string1);
        skipPastEndOfCurrentMarkup();
    }
    void abortMarkup(int majorCode, int minorCode, int stringIndex1, int stringIndex2) throws Exception {
        reportFatalXMLError(majorCode, minorCode, stringIndex1, stringIndex2);
        skipPastEndOfCurrentMarkup();
    }
    void skipPastEndOfCurrentMarkup() throws Exception {
        fEntityReader.skipToChar('>');
        if (fEntityReader.lookingAtChar('>', true))
            fScannerMarkupDepth--;
    }
    //
    //
    //
    int setScannerState(int state) {
        int oldState = fScannerState;
        fScannerState = state;
        return oldState;
    }
    void restoreScannerState(int state) {
        if (fScannerState != SCANNER_STATE_END_OF_INPUT)
            fScannerState = state;
    }
    //
    //
    //
    /**
     * The main loop of the scanner is implemented by calling the dispatch method
     * of ScannerDispatcher with a flag which tells the dispatcher whether to continue
     * or return.  The scanner logic is split up into dispatchers for various syntatic
     * components of XML.  //REVISIT more rationale needed
     */
    interface ScannerDispatcher {
        /**
         * scan an XML syntactic component 
         *
         * @param keepgoing if true continue on to the next dispatcher, otherwise return
         * @return true if scanning was successful //REVISIT - does it ever return false or does it just throw?
         * @exception java.lang.Exception
         */
        boolean dispatch(boolean keepgoing) throws Exception;
        /**
         * endOfInput encapsulates the end of entity handling for each dispatcher 
         *
         * @param entityName StringPool handle of the entity that has reached the end
         * @param moreToFollow true if there is more input to be read
         * @exception
         */
        void endOfInput(int entityName, boolean moreToFollow) throws Exception;
    }
    final class XMLDeclDispatcher implements ScannerDispatcher {
        public boolean dispatch(boolean keepgoing) throws Exception {
            if (fEntityReader.lookingAtChar('<', true)) {
                fScannerMarkupDepth++;
                setScannerState(SCANNER_STATE_START_OF_MARKUP);
                if (fEntityReader.lookingAtChar('?', true)) {
                    int piTarget = fEntityReader.scanName(' ');
                    if (piTarget == -1) {
                        abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                    XMLMessages.P16_PITARGET_REQUIRED);
                    } else if ("xml".equals(fStringPool.toString(piTarget))) {
                        if (fEntityReader.lookingAtSpace(true)) { // an XMLDecl looks like a PI with the target 'xml'
                            scanXMLDeclOrTextDecl(false);
                        } else { // a PI target matching 'xml'
                            abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                        XMLMessages.P17_RESERVED_PITARGET);
                        }
                    } else { // PI
                      fEventHandler.callStartDocument(-1, -1, -1);
                      scanPI(piTarget);
                    }
                    fEventHandler.callStartDocument(-1, -1, -1);
                    fDispatcher = new PrologDispatcher();
                    restoreScannerState(SCANNER_STATE_PROLOG);
                    return true;
                }
                fEventHandler.callStartDocument(-1, -1, -1);
                if (fEntityReader.lookingAtChar('!', true)) {
                    if (fEntityReader.lookingAtChar('-', true)) { // comment ?
                        if (fEntityReader.lookingAtChar('-', true)) {
                            scanComment(); // scan through the closing '-->'
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                        XMLMessages.P22_NOT_RECOGNIZED);
                        }
                    } else {
                        if (fEntityReader.skippedString(doctype_string)) {
                            setScannerState(SCANNER_STATE_DOCTYPE);
                            fSeenDoctypeDecl = true;
                            fEventHandler.scanDoctypeDecl(fStandalone); // scan through the closing '>'
                            fScannerMarkupDepth--;
                            fDispatcher = new PrologDispatcher();
                            restoreScannerState(SCANNER_STATE_PROLOG);
                            return true;
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                        XMLMessages.P22_NOT_RECOGNIZED);
                        }
                    }
                } else {
                    fDispatcher = new ContentDispatcher();
                    restoreScannerState(SCANNER_STATE_ROOT_ELEMENT);
                    return true;
                }
            } else {
                fEventHandler.callStartDocument(-1, -1, -1);
                if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                } else if (!fEntityReader.lookingAtValidChar(false)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        if (invChar >= 0) {
                            String arg = Integer.toHexString(invChar);
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_PROLOG,
                                                XMLMessages.P22_INVALID_CHARACTER,
                                                arg);
                        }
                    }
                } else {
                    reportFatalXMLError(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                        XMLMessages.P22_NOT_RECOGNIZED);
                    fEntityReader.lookingAtValidChar(true);
                }
            }
            fDispatcher = new PrologDispatcher();
            restoreScannerState(SCANNER_STATE_PROLOG);
            return true;
        }
        public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
            switch (fScannerState) {
            case SCANNER_STATE_XML_DECL:
            case SCANNER_STATE_START_OF_MARKUP:
            case SCANNER_STATE_DOCTYPE:
                break;
            case SCANNER_STATE_COMMENT:
                if (!moreToFollow) {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_UNTERMINATED,
                                        XMLMessages.P15_UNTERMINATED);
                } else {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_NOT_IN_ONE_ENTITY,
                                        XMLMessages.P78_NOT_WELLFORMED);
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
            default:
                throw new RuntimeException("FWK001 1] ScannerState="+fScannerState+"\n" + "1\t"+fScannerState);
            }
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_ROOT_ELEMENT_REQUIRED,
                                    XMLMessages.P1_ELEMENT_REQUIRED);
                fDispatcher = new EndOfInputDispatcher();
                setScannerState(SCANNER_STATE_END_OF_INPUT);
            }
        }
    }
    final class PrologDispatcher implements ScannerDispatcher {
        public boolean dispatch(boolean keepgoing) throws Exception {
            do {
                if (fEntityReader.lookingAtChar('<', true)) {
                    fScannerMarkupDepth++;
                    setScannerState(SCANNER_STATE_START_OF_MARKUP);
                    if (fEntityReader.lookingAtChar('?', true)) {
                        int piTarget = fEntityReader.scanName(' ');
                        if (piTarget == -1) {
                            abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                        XMLMessages.P16_PITARGET_REQUIRED);
                        } else if ("xml".equals(fStringPool.toString(piTarget))) {
                            if (fEntityReader.lookingAtSpace(true)) { // an XMLDecl looks like a PI with the target 'xml'
                                abortMarkup(XMLMessages.MSG_XMLDECL_MUST_BE_FIRST,
                                            XMLMessages.P22_XMLDECL_MUST_BE_FIRST);
                            } else { // a PI target matching 'xml'
                                abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                            XMLMessages.P17_RESERVED_PITARGET);
                            }
                        } else { // PI
                            scanPI(piTarget);
                        }
                    } else if (fEntityReader.lookingAtChar('!', true)) {
                        if (fEntityReader.lookingAtChar('-', true)) { // comment ?
                            if (fEntityReader.lookingAtChar('-', true)) {
                                scanComment(); // scan through the closing '-->'
                            } else {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                            XMLMessages.P22_NOT_RECOGNIZED);
                            }
                        } else {
                            if (!fSeenDoctypeDecl && fEntityReader.skippedString(doctype_string)) {
                                setScannerState(SCANNER_STATE_DOCTYPE);
                                fSeenDoctypeDecl = true;
                                fEventHandler.scanDoctypeDecl(fStandalone); // scan through the closing '>'
                                fScannerMarkupDepth--;
                            } else {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                            XMLMessages.P22_NOT_RECOGNIZED);
                            }
                        }
                    } else {
                        fDispatcher = new ContentDispatcher();
                        restoreScannerState(SCANNER_STATE_ROOT_ELEMENT);
                        return true;
                    }
                    restoreScannerState(SCANNER_STATE_PROLOG);
                } else if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                } else if (!fEntityReader.lookingAtValidChar(false)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        if (invChar >= 0) {
                            String arg = Integer.toHexString(invChar);
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_PROLOG,
                                                XMLMessages.P22_INVALID_CHARACTER,
                                                arg);
                        }
                    }
                } else {
                    reportFatalXMLError(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                        XMLMessages.P22_NOT_RECOGNIZED);
                    fEntityReader.lookingAtValidChar(true);
                }
            } while (fScannerState != SCANNER_STATE_END_OF_INPUT && keepgoing);
            return true;
        }
        public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
            switch (fScannerState) {
            case SCANNER_STATE_PROLOG:
            case SCANNER_STATE_START_OF_MARKUP:
            case SCANNER_STATE_DOCTYPE:
                break;
            case SCANNER_STATE_COMMENT:
                if (!moreToFollow) {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_UNTERMINATED,
                                        XMLMessages.P15_UNTERMINATED);
                } else {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_NOT_IN_ONE_ENTITY,
                                        XMLMessages.P78_NOT_WELLFORMED);
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
            default:
                throw new RuntimeException("FWK001 2] ScannerState="+fScannerState+"\n" + "2\t"+fScannerState);
            }
            if (!moreToFollow) {
                reportFatalXMLError(XMLMessages.MSG_ROOT_ELEMENT_REQUIRED,
                                    XMLMessages.P1_ELEMENT_REQUIRED);
                fDispatcher = new EndOfInputDispatcher();
                setScannerState(SCANNER_STATE_END_OF_INPUT);
            }
        }
    }
    final class ContentDispatcher implements ScannerDispatcher {
        private int fContentReader = -1;
        private int fElementDepth = 0;
        private int fCurrentElementType = -1;
        private int[] fElementTypeStack = new int[8];

        void popElementType() {
            if (fElementDepth-- == 0) {
                throw new RuntimeException("FWK002 popElementType: fElementDepth-- == 0.");
            }
            if (fElementDepth == 0) {
                fCurrentElementType = - 1;
            } else {
                fCurrentElementType = fElementTypeStack[fElementDepth - 1];
            }
        }

        public boolean dispatch(boolean keepgoing) throws Exception {
            do {
                switch (fScannerState) {
                case SCANNER_STATE_ROOT_ELEMENT:
                {
                    int elementType = fEventHandler.scanElementType(fEntityReader, '>');
                    if (elementType != -1) {
                        //
                        // root element
                        //
                        fContentReader = fReaderId;
                        fSeenRootElement = true;
                        //
                        // scan element
                        //
                        if (fEntityReader.lookingAtChar('>', true)) {
                            //
                            // we have more content
                            //
                            fEventHandler.callStartElement(elementType);
                            fScannerMarkupDepth--;
                            if (fElementDepth == fElementTypeStack.length) {
                                int[] newStack = new int[fElementDepth * 2];
                                System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                fElementTypeStack = newStack;
                            }
                            fCurrentElementType = elementType;
                            fElementTypeStack[fElementDepth] = elementType;
                            fElementDepth++;
                            restoreScannerState(SCANNER_STATE_CONTENT);
                        } else if (scanElement(elementType)) {
                            //
                            // we have more content
                            //
                            if (fElementDepth == fElementTypeStack.length) {
                                int[] newStack = new int[fElementDepth * 2];
                                System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                fElementTypeStack = newStack;
                            }
                            fCurrentElementType = elementType;
                            fElementTypeStack[fElementDepth] = elementType;
                            fElementDepth++;
                            restoreScannerState(SCANNER_STATE_CONTENT);
                        } else {
                            fDispatcher = new TrailingMiscDispatcher();
                            restoreScannerState(SCANNER_STATE_TRAILING_MISC);
                            return true;
                        }
                    } else {
                        reportFatalXMLError(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_PROLOG,
                                            XMLMessages.P22_NOT_RECOGNIZED);
                        fDispatcher = new PrologDispatcher();
                        restoreScannerState(SCANNER_STATE_PROLOG);
                        return true;
                    }
                    break;
                }
                case SCANNER_STATE_START_OF_MARKUP:
                    if (fEntityReader.lookingAtChar('?', true)) {
                        int piTarget = fEntityReader.scanName(' ');
                        if (piTarget == -1) {
                            abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                        XMLMessages.P16_PITARGET_REQUIRED);
                        } else if ("xml".equals(fStringPool.toString(piTarget))) {
                            if (fEntityReader.lookingAtSpace(true)) { // an XMLDecl looks like a PI with the target 'xml'
                                if (fParseTextDecl) {
                                    scanXMLDeclOrTextDecl(true);
                                    fParseTextDecl = false;
                                } else {
                                    abortMarkup(XMLMessages.MSG_TEXTDECL_MUST_BE_FIRST,
                                                XMLMessages.P30_TEXTDECL_MUST_BE_FIRST);
                                }
                            } else { // a PI target matching 'xml'
                                abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                            XMLMessages.P17_RESERVED_PITARGET);
                            }
                        } else { // PI
                            scanPI(piTarget);
                        }
                        restoreScannerState(SCANNER_STATE_CONTENT);
                    } else if (fEntityReader.lookingAtChar('!', true)) {
                        if (fEntityReader.lookingAtChar('-', true)) { // comment ?
                            if (fEntityReader.lookingAtChar('-', true)) {
                                scanComment(); // scan through the closing '-->'
                            } else {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT,
                                            XMLMessages.P43_NOT_RECOGNIZED);
                            }
                        } else {
                            if (fEntityReader.skippedString(cdata_string)) {
                                fEntityReader.setInCDSect(true);
                                fEventHandler.startCDATA();
                            } else {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT,
                                            XMLMessages.P43_NOT_RECOGNIZED);
                            }
                        }
                    } else {
                        if (fEntityReader.lookingAtChar('/', true)) {
                            //
                            // [42] ETag ::= '</' Name S? '>'
                            //
                            if (!fEventHandler.scanExpectedElementType(fEntityReader, '>')) {
                                abortMarkup(XMLMessages.MSG_ETAG_REQUIRED,
                                            XMLMessages.P39_UNTERMINATED,
                                            fCurrentElementType);
                            } else {
                                if (!fEntityReader.lookingAtChar('>', true)) {
                                    fEntityReader.skipPastSpaces();
                                    if (!fEntityReader.lookingAtChar('>', true)) {
                                        reportFatalXMLError(XMLMessages.MSG_ETAG_UNTERMINATED,
                                                            XMLMessages.P42_UNTERMINATED,
                                                            fCurrentElementType);
                                    }
                                }
                                fScannerMarkupDepth--;
                                if (fEventHandler.callEndElement(fReaderId)) {
                                    fDispatcher = new TrailingMiscDispatcher();
                                    restoreScannerState(SCANNER_STATE_TRAILING_MISC);
                                    return true;
                                }
                                if (fElementDepth-- == 0) {
                                    throw new RuntimeException("FWK002 popElementType: fElementDepth-- == 0.");
                                }
                                if (fElementDepth == 0) {
                                    fCurrentElementType = - 1;
                                } else {
                                    fCurrentElementType = fElementTypeStack[fElementDepth - 1];
                                }
                            }
                        } else {
                            int elementType = fEventHandler.scanElementType(fEntityReader, '>');
                            if (elementType != -1) {
                                //
                                // element
                                //
                                if (fEntityReader.lookingAtChar('>', true)) {
                                    fEventHandler.callStartElement(elementType);
                                    fScannerMarkupDepth--;
                                    if (fElementDepth == fElementTypeStack.length) {
                                        int[] newStack = new int[fElementDepth * 2];
                                        System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                        fElementTypeStack = newStack;
                                    }
                                    fCurrentElementType = elementType;
                                    fElementTypeStack[fElementDepth] = elementType;
                                    fElementDepth++;
                                } else {
                                    if (scanElement(elementType)) {
                                        if (fElementDepth == fElementTypeStack.length) {
                                            int[] newStack = new int[fElementDepth * 2];
                                            System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                            fElementTypeStack = newStack;
                                        }
                                        fCurrentElementType = elementType;
                                        fElementTypeStack[fElementDepth] = elementType;
                                        fElementDepth++;
                                    }
                                }
                            } else {
                                abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT,
                                            XMLMessages.P43_NOT_RECOGNIZED);
                            }
                        }
                    }
                    restoreScannerState(SCANNER_STATE_CONTENT);
                    break;
                case SCANNER_STATE_CONTENT:
                    if (fParseTextDecl && fEntityReader.lookingAtChar('<', true)) {
                        fScannerMarkupDepth++;
                        setScannerState(SCANNER_STATE_START_OF_MARKUP);
                        continue;
                    }
                    switch (fEntityReader.scanContent(fCurrentElementType)) {
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_PI:
                        fScannerMarkupDepth++;
                        int piTarget = fEntityReader.scanName(' ');
                        if (piTarget == -1) {
                            abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                        XMLMessages.P16_PITARGET_REQUIRED);
                        } else if ("xml".equals(fStringPool.toString(piTarget))) {
                            if (fEntityReader.lookingAtSpace(true)) { // an XMLDecl looks like a PI with the target 'xml'
                                if (fReaderId == fContentReader) {
                                    abortMarkup(XMLMessages.MSG_XMLDECL_MUST_BE_FIRST,
                                                XMLMessages.P22_XMLDECL_MUST_BE_FIRST);
                                } else {
                                    abortMarkup(XMLMessages.MSG_TEXTDECL_MUST_BE_FIRST,
                                                XMLMessages.P30_TEXTDECL_MUST_BE_FIRST);
                                }
                            } else { // a PI target matching 'xml'
                                abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                            XMLMessages.P17_RESERVED_PITARGET);
                            }
                        } else { // PI
                            scanPI(piTarget);
                        }
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_COMMENT:
                        fScannerMarkupDepth++;
                        fParseTextDecl = false;
                        scanComment(); // scan through the closing '-->'
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_CDSECT:
                        fScannerMarkupDepth++;
                        fParseTextDecl = false;
                        fEntityReader.setInCDSect(true);
                        fEventHandler.startCDATA();
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_ETAG:
                        fScannerMarkupDepth++;
                        fParseTextDecl = false;
                        //
                        // [42] ETag ::= '</' Name S? '>'
                        //
                        if (!fEventHandler.scanExpectedElementType(fEntityReader, '>')) {
                            abortMarkup(XMLMessages.MSG_ETAG_REQUIRED,
                                        XMLMessages.P39_UNTERMINATED,
                                        fCurrentElementType);
                        } else {
                            if (!fEntityReader.lookingAtChar('>', true)) {
                                fEntityReader.skipPastSpaces();
                                if (!fEntityReader.lookingAtChar('>', true)) {
                                    reportFatalXMLError(XMLMessages.MSG_ETAG_UNTERMINATED,
                                                        XMLMessages.P42_UNTERMINATED,
                                                        fCurrentElementType);
                                }
                            }
                            fScannerMarkupDepth--;
                            if (fEventHandler.callEndElement(fReaderId)) {
                                fDispatcher = new TrailingMiscDispatcher();
                                restoreScannerState(SCANNER_STATE_TRAILING_MISC);
                                return true;
                            }
                            if (fElementDepth-- == 0) {
                                throw new RuntimeException("FWK002 popElementType: fElementDepth-- == 0.");
                            }
                            if (fElementDepth == 0) {
                                fCurrentElementType = - 1;
                            } else {
                                fCurrentElementType = fElementTypeStack[fElementDepth - 1];
                            }
                        }
                        restoreScannerState(SCANNER_STATE_CONTENT);
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_ELEMENT:
                    {
                        fScannerMarkupDepth++;
                        fParseTextDecl = false;
                        int elementType = fEventHandler.scanElementType(fEntityReader, '>');
                        if (elementType != -1) {
                            if (fEntityReader.lookingAtChar('>', true)) {
                                fEventHandler.callStartElement(elementType);
                                fScannerMarkupDepth--;
                                if (fElementDepth == fElementTypeStack.length) {
                                    int[] newStack = new int[fElementDepth * 2];
                                    System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                    fElementTypeStack = newStack;
                                }
                                fCurrentElementType = elementType;
                                fElementTypeStack[fElementDepth] = elementType;
                                fElementDepth++;
                            } else {
                                if (scanElement(elementType)) {
                                    if (fElementDepth == fElementTypeStack.length) {
                                        int[] newStack = new int[fElementDepth * 2];
                                        System.arraycopy(fElementTypeStack, 0, newStack, 0, fElementDepth);
                                        fElementTypeStack = newStack;
                                    }
                                    fCurrentElementType = elementType;
                                    fElementTypeStack[fElementDepth] = elementType;
                                    fElementDepth++;
                                }
                            }
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT,
                                        XMLMessages.P43_NOT_RECOGNIZED);
                        }
                        if (fScannerState != SCANNER_STATE_END_OF_INPUT)
                            fScannerState = SCANNER_STATE_CONTENT;
                        break;
                    }
                    case XMLEntityHandler.CONTENT_RESULT_MATCHING_ETAG:
                    {
                        fParseTextDecl = false;
                        if (fEventHandler.callEndElement(fReaderId)) {
                            if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                                fDispatcher = new TrailingMiscDispatcher();
                                fScannerState = SCANNER_STATE_TRAILING_MISC;
                            }
                            return true;
                        }
                        if (fElementDepth-- == 0) {
                            throw new RuntimeException("FWK002 popElementType: fElementDepth-- == 0.");
                        }
                        if (fElementDepth == 0) {
                            fCurrentElementType = - 1;
                        } else {
                            fCurrentElementType = fElementTypeStack[fElementDepth - 1];
                        }
                        if (fScannerState != SCANNER_STATE_END_OF_INPUT)
                            fScannerState = SCANNER_STATE_CONTENT;
                        break;
                    }
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_CHARREF:
                        fParseTextDecl = false;
                        //
                        // [67] Reference ::= EntityRef | CharRef
                        // [68] EntityRef ::= '&' Name ';'
                        // [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
                        //
                        setScannerState(SCANNER_STATE_REFERENCE);
                        int num = scanCharRef();
                        // if (num == -1) num = 0xfffd; // REVISIT - alternative is to use Unicode replacement char
                        if (num != -1)
                            fEventHandler.callCharacters(num);
                        restoreScannerState(SCANNER_STATE_CONTENT);
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_REFERENCE_END_OF_INPUT:
                        // REVISIT - This should hopefully get us the "reference not
                        //   contained in one entity" error when endOfInput is called.
                        //   Test that this is so...
                        //
                        // fall through...
                        //
                    case XMLEntityHandler.CONTENT_RESULT_START_OF_ENTITYREF:
                        fParseTextDecl = false;
                        //
                        // [68] EntityRef ::= '&' Name ';'
                        //
                        setScannerState(SCANNER_STATE_REFERENCE);
                        int nameOffset = fEntityReader.currentOffset();
                        fEntityReader.skipPastName(';');
                        int nameLength = fEntityReader.currentOffset() - nameOffset;
                        if (nameLength == 0) {
                            reportFatalXMLError(XMLMessages.MSG_NAME_REQUIRED_IN_REFERENCE,
                                                XMLMessages.P68_NAME_REQUIRED);
                            restoreScannerState(SCANNER_STATE_CONTENT);
                        } else if (!fEntityReader.lookingAtChar(';', true)) {
                            reportFatalXMLError(XMLMessages.MSG_SEMICOLON_REQUIRED_IN_REFERENCE,
                                                XMLMessages.P68_SEMICOLON_REQUIRED,
                                                fEntityReader.addString(nameOffset, nameLength));
                            restoreScannerState(SCANNER_STATE_CONTENT);
                        } else {
                            restoreScannerState(SCANNER_STATE_CONTENT);
                            int entityName = fEntityReader.addSymbol(nameOffset, nameLength);
                            fParseTextDecl = fEntityHandler.startReadingFromEntity(entityName, fElementDepth, XMLEntityHandler.CONTEXT_IN_CONTENT);
                        }
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_END_OF_CDSECT:
                        fParseTextDecl = false;
                        //
                        // [14] CharData ::= [^<&]* - ([^<&]* ']]>' [^<&]*)
                        // [21] CDEnd ::= ']]>'
                        //
                        if (fEntityReader.getInCDSect()) {
                            fEntityReader.setInCDSect(false);
                            fEventHandler.endCDATA();
                            fScannerMarkupDepth--;
                        } else {
                            reportFatalXMLError(XMLMessages.MSG_CDEND_IN_CONTENT,
                                                XMLMessages.P14_INVALID);
                        }
                        restoreScannerState(SCANNER_STATE_CONTENT);
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_INVALID_CHAR:
                        fParseTextDecl = false;
                        //
                        // The reader will also use this state if it
                        // encounters the end of input while reading
                        // content.  We need to check for this case.
                        //
                        if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                            if (!fEntityReader.lookingAtValidChar(false)) {
                                //
                                //  [2] Char ::= #x9 | #xA | #xD | [#x20-#xD7FF]        // any Unicode character, excluding the
                                //               | [#xE000-#xFFFD] | [#x10000-#x10FFFF] // surrogate blocks, FFFE, and FFFF.
                                //
                                int invChar = fEntityReader.scanInvalidChar();
                                if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                                    if (invChar >= 0) {
                                        if (fEntityReader.getInCDSect()) {
                                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_CDSECT,
                                                                XMLMessages.P20_INVALID_CHARACTER,
                                                                Integer.toHexString(invChar));
                                        } else {
                                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_CONTENT,
                                                                XMLMessages.P43_INVALID_CHARACTER,
                                                                Integer.toHexString(invChar));
                                        }
                                    }
                                }
                            }
                            restoreScannerState(SCANNER_STATE_CONTENT);
                        }
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_MARKUP_NOT_RECOGNIZED:
                        fParseTextDecl = false;
                        abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_CONTENT,
                                    XMLMessages.P43_NOT_RECOGNIZED);
                        break;
                    case XMLEntityHandler.CONTENT_RESULT_MARKUP_END_OF_INPUT:
                        // REVISIT - This should hopefully get us the "markup not
                        //   contained in one entity" error when endOfInput is called.
                        //   Test that this is so...
                        fScannerMarkupDepth++;
                        fParseTextDecl = false;
                        fScannerState = SCANNER_STATE_START_OF_MARKUP;
                        break;
                    default:
                        throw new RuntimeException("FWK001 3] ScannerState="+fScannerState+"\n" + "3\t"+fScannerState); // should not happen
                    }
                    break;
                default:
                    throw new RuntimeException("FWK001 4] ScannerState="+fScannerState+"\n" + "4\t"+fScannerState);
                }
            } while (fScannerState != SCANNER_STATE_END_OF_INPUT && keepgoing);
            return true;
        }
        public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
            switch (fScannerState) {
            case SCANNER_STATE_ROOT_ELEMENT:
            case SCANNER_STATE_START_OF_MARKUP:
                break;
            case SCANNER_STATE_CONTENT:
                if (fEntityReader.getInCDSect()) {
                    reportFatalXMLError(XMLMessages.MSG_CDSECT_UNTERMINATED,
                                        XMLMessages.P18_UNTERMINATED);
                }
                break;
            case SCANNER_STATE_ATTRIBUTE_LIST:
                if (!moreToFollow) {
// REVISIT                    reportFatalXMLError(XMLMessages.MSG_TAG1);
                } else {
// REVISIT                    reportFatalXMLError(XMLMessages.MSG_TAG1);
                }
                break;
            case SCANNER_STATE_ATTRIBUTE_NAME:
                if (!moreToFollow) {
// REVISIT                    reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
                } else {
// REVISIT                    reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
                }
                break;
            case SCANNER_STATE_ATTRIBUTE_VALUE:
                if (!moreToFollow) {
                    reportFatalXMLError(XMLMessages.MSG_ATTRIBUTE_VALUE_UNTERMINATED,
                                        XMLMessages.P10_UNTERMINATED,
                                        fAttValueElementType,
                                        fAttValueAttrName);
                } else if (fReaderId == fAttValueReader) {
// REVISIT                        reportFatalXMLError(XMLMessages.MSG_ATTVAL0);
                } else {
                    fEntityReader.append(fLiteralData, fAttValueMark, fAttValueOffset - fAttValueMark);
                }
                break;
            case SCANNER_STATE_COMMENT:
                if (!moreToFollow) {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_UNTERMINATED,
                                        XMLMessages.P15_UNTERMINATED);
                } else {
                    reportFatalXMLError(XMLMessages.MSG_COMMENT_NOT_IN_ONE_ENTITY,
                                        XMLMessages.P78_NOT_WELLFORMED);
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
            case SCANNER_STATE_REFERENCE:
                if (!moreToFollow) {
                    reportFatalXMLError(XMLMessages.MSG_REFERENCE_UNTERMINATED,
                                        XMLMessages.P67_UNTERMINATED);
                } else {
                    reportFatalXMLError(XMLMessages.MSG_REFERENCE_NOT_IN_ONE_ENTITY,
                                        XMLMessages.P78_NOT_WELLFORMED);
                }
                break;
            default:
                throw new RuntimeException("FWK001 5] ScannerState="+fScannerState+"\n" + "5\t"+fScannerState);
            }
            if (!moreToFollow) {
                if (fElementDepth > 0)
                    reportFatalXMLError(XMLMessages.MSG_ETAG_REQUIRED,
                                        XMLMessages.P39_UNTERMINATED,
                                        fCurrentElementType);
                fDispatcher = new EndOfInputDispatcher();
                setScannerState(SCANNER_STATE_END_OF_INPUT);
            }
        }
    }
    final class TrailingMiscDispatcher implements ScannerDispatcher {
        public boolean dispatch(boolean keepgoing) throws Exception {
            do {
                if (fEntityReader.lookingAtChar('<', true)) {
                    fScannerMarkupDepth++;
                    setScannerState(SCANNER_STATE_START_OF_MARKUP);
                    if (fEntityReader.lookingAtChar('?', true)) {
                        int piTarget = fEntityReader.scanName(' ');
                        if (piTarget == -1) {
                            abortMarkup(XMLMessages.MSG_PITARGET_REQUIRED,
                                        XMLMessages.P16_PITARGET_REQUIRED);
                        } else if ("xml".equals(fStringPool.toString(piTarget))) {
                            if (fEntityReader.lookingAtSpace(true)) { // an XMLDecl looks like a PI with the target 'xml'
                                abortMarkup(XMLMessages.MSG_XMLDECL_MUST_BE_FIRST,
                                            XMLMessages.P22_XMLDECL_MUST_BE_FIRST);
                            } else { // a PI target matching 'xml'
                                abortMarkup(XMLMessages.MSG_RESERVED_PITARGET,
                                            XMLMessages.P17_RESERVED_PITARGET);
                            }
                        } else { // PI
                            scanPI(piTarget);
                        }
                    } else if (fEntityReader.lookingAtChar('!', true)) {
                        if (fEntityReader.lookingAtChar('-', true) &&
                            fEntityReader.lookingAtChar('-', true)) { // comment ?
                            scanComment(); // scan through the closing '-->'
                        } else {
                            abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_MISC,
                                        XMLMessages.P27_NOT_RECOGNIZED);
                        }
                    } else {
                        abortMarkup(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_MISC,
                                    XMLMessages.P27_NOT_RECOGNIZED);
                    }
                    restoreScannerState(SCANNER_STATE_TRAILING_MISC);
                } else if (fEntityReader.lookingAtSpace(true)) {
                    fEntityReader.skipPastSpaces();
                } else if (!fEntityReader.lookingAtValidChar(false)) {
                    int invChar = fEntityReader.scanInvalidChar();
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        if (invChar >= 0) {
                            String arg = Integer.toHexString(invChar);
                            reportFatalXMLError(XMLMessages.MSG_INVALID_CHAR_IN_MISC,
                                                XMLMessages.P27_INVALID_CHARACTER,
                                                arg);
                        }
                    }
                } else {
                    reportFatalXMLError(XMLMessages.MSG_MARKUP_NOT_RECOGNIZED_IN_MISC,
                                        XMLMessages.P27_NOT_RECOGNIZED);
                    fEntityReader.lookingAtValidChar(true);
                }
            } while (fScannerState != SCANNER_STATE_END_OF_INPUT && keepgoing);
            return true;
        }
        public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
            if (moreToFollow)
                throw new RuntimeException("FWK003 TrailingMiscDispatcher.endOfInput moreToFollow");
            switch (fScannerState) {
            case SCANNER_STATE_TRAILING_MISC:
            case SCANNER_STATE_START_OF_MARKUP:
                break;
            case SCANNER_STATE_COMMENT:
                reportFatalXMLError(XMLMessages.MSG_COMMENT_UNTERMINATED,
                                    XMLMessages.P15_UNTERMINATED);
                break;
            case SCANNER_STATE_PI:
                reportFatalXMLError(XMLMessages.MSG_PI_UNTERMINATED,
                                    XMLMessages.P16_UNTERMINATED);
                break;
            default:
                throw new RuntimeException("FWK001 6] ScannerState="+fScannerState+"\n" + "6\t"+fScannerState);
            }
            fDispatcher = new EndOfInputDispatcher();
            setScannerState(SCANNER_STATE_END_OF_INPUT);
        }
    }
    final class EndOfInputDispatcher implements ScannerDispatcher {
        public boolean dispatch(boolean keepgoing) throws Exception {
            if (fScannerState != SCANNER_STATE_TERMINATED)
                fEventHandler.callEndDocument();
            setScannerState(SCANNER_STATE_TERMINATED);
            return false;
        }
        public void endOfInput(int entityName, boolean moreToFollow) throws Exception {
            throw new RuntimeException("FWK001 7] ScannerState="+fScannerState+"\n" + "7\t"+fScannerState);
        }
    }
    //
    // From the standard:
    //
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    // [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
    // [32] SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
    //                 | ('"' ('yes' | 'no') '"'))
    //
    // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
    //
    void scanXMLDeclOrTextDecl(boolean scanningTextDecl) throws Exception
    {
        int version = -1;
        int encoding = -1;
        int standalone = -1;
        final int XMLDECL_START = 0;
        final int XMLDECL_VERSION = 1;
        final int XMLDECL_ENCODING = 2;
        final int XMLDECL_STANDALONE = 3;
        final int XMLDECL_FINISHED = 4;
        int state = XMLDECL_START;
        do {
            fEntityReader.skipPastSpaces();
            int offset = fEntityReader.currentOffset();
            if (scanningTextDecl) {
                if (state == XMLDECL_START && fEntityReader.skippedString(version_string)) {
                    state = XMLDECL_VERSION;
                } else if (fEntityReader.skippedString(encoding_string)) {
                    state = XMLDECL_ENCODING;
                } else {
                    abortMarkup(XMLMessages.MSG_ENCODINGDECL_REQUIRED,
                                XMLMessages.P77_ENCODINGDECL_REQUIRED);
                    return;
                }
            } else {
                if (state == XMLDECL_START) {
                    if (!fEntityReader.skippedString(version_string)) {
                        abortMarkup(XMLMessages.MSG_VERSIONINFO_REQUIRED,
                                    XMLMessages.P23_VERSIONINFO_REQUIRED);
                        return;
                    }
                    state = XMLDECL_VERSION;
                } else {
                    if (state == XMLDECL_VERSION) {
                        if (fEntityReader.skippedString(encoding_string))
                            state = XMLDECL_ENCODING;
                        else
                            state = XMLDECL_STANDALONE;
                    } else
                        state = XMLDECL_STANDALONE;
                    if (state == XMLDECL_STANDALONE && !fEntityReader.skippedString(standalone_string))
                        break;
                }
            }
            int length = fEntityReader.currentOffset() - offset;
            fEntityReader.skipPastSpaces();
            if (!fEntityReader.lookingAtChar('=', true)) {
                int majorCode = scanningTextDecl ?
                                XMLMessages.MSG_EQ_REQUIRED_IN_TEXTDECL :
                                XMLMessages.MSG_EQ_REQUIRED_IN_XMLDECL;
                int minorCode = state == XMLDECL_VERSION ?
                                XMLMessages.P24_EQ_REQUIRED :
                                (state == XMLDECL_ENCODING ?
                                 XMLMessages.P80_EQ_REQUIRED :
                                 XMLMessages.P32_EQ_REQUIRED);
                abortMarkup(majorCode, minorCode, fEntityReader.addString(offset, length));
                return;
            }
            fEntityReader.skipPastSpaces();
            int result = fEntityReader.scanStringLiteral();
            switch (result) {
            case XMLEntityHandler.STRINGLIT_RESULT_QUOTE_REQUIRED:
            {
                int majorCode = scanningTextDecl ?
                                XMLMessages.MSG_QUOTE_REQUIRED_IN_TEXTDECL :
                                XMLMessages.MSG_QUOTE_REQUIRED_IN_XMLDECL;
                int minorCode = state == XMLDECL_VERSION ?
                                XMLMessages.P24_QUOTE_REQUIRED :
                                (state == XMLDECL_ENCODING ?
                                 XMLMessages.P80_QUOTE_REQUIRED :
                                 XMLMessages.P32_QUOTE_REQUIRED);
                abortMarkup(majorCode, minorCode, fEntityReader.addString(offset, length));
                return;
            }
            case XMLEntityHandler.STRINGLIT_RESULT_INVALID_CHAR:
                int invChar = fEntityReader.scanInvalidChar();
                if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                    if (invChar >= 0) {
                        int majorCode = scanningTextDecl ?
                                        XMLMessages.MSG_INVALID_CHAR_IN_TEXTDECL :
                                        XMLMessages.MSG_INVALID_CHAR_IN_XMLDECL;
                        int minorCode = state == XMLDECL_VERSION ?
                                        XMLMessages.P26_INVALID_CHARACTER :
                                        (state == XMLDECL_ENCODING ?
                                         XMLMessages.P81_INVALID_CHARACTER :
                                         XMLMessages.P32_INVALID_CHARACTER);
                        reportFatalXMLError(majorCode, minorCode, Integer.toHexString(invChar));
                    }
                    skipPastEndOfCurrentMarkup();
                }
                return;
            default:
                break;
            }
            switch (state) {
            case XMLDECL_VERSION:
                //
                // version="..."
                //
                version = result;
                String versionString = fStringPool.toString(version);
                if (!"1.0".equals(versionString)) {
                    if (!fEventHandler.validVersionNum(versionString)) {
                        abortMarkup(XMLMessages.MSG_VERSIONINFO_INVALID,
                                            XMLMessages.P26_INVALID_VALUE,
                                            versionString);
                        return;
                    }
                    // NOTE: RECOVERABLE ERROR
                    Object[] args = { versionString };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_VERSION_NOT_SUPPORTED,
                                               XMLMessages.P26_NOT_SUPPORTED,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    // REVISIT - hope it is compatible...
                    // skipPastEndOfCurrentMarkup();
                    // return;
                }
                if (!fEntityReader.lookingAtSpace(true)) {
                    if (scanningTextDecl) {
                        abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_IN_TEXTDECL,
                                    XMLMessages.P80_WHITESPACE_REQUIRED);
                        return;
                    }
                    state = XMLDECL_FINISHED;
                }
                break;
            case XMLDECL_ENCODING:
                //
                // encoding = "..."
                //
                encoding = result;
                String encodingString = fStringPool.toString(encoding);
                if (!fEventHandler.validEncName(encodingString)) {
                    abortMarkup(XMLMessages.MSG_ENCODINGDECL_INVALID,
                                XMLMessages.P81_INVALID_VALUE,
                                encodingString);
                    return;
                }
                if (!fEntityReader.lookingAtSpace(true)) {
                    state = XMLDECL_FINISHED;
                } else if (scanningTextDecl) {
                    fEntityReader.skipPastSpaces();
                    state = XMLDECL_FINISHED;
                }
                break;
            case XMLDECL_STANDALONE:
                //
                // standalone="..."
                //
                standalone = result;
                String standaloneString = fStringPool.toString(standalone);
                boolean yes = "yes".equals(standaloneString);
                if (!yes && !"no".equals(standaloneString)) {
                    abortMarkup(XMLMessages.MSG_SDDECL_INVALID,
                                XMLMessages.P32_INVALID_VALUE,
                                standaloneString);
                    return;
                }
                fStandalone = yes;
                fEntityReader.skipPastSpaces();
                state = XMLDECL_FINISHED;
                break;
            }
        } while (state != XMLDECL_FINISHED);
        if (!fEntityReader.lookingAtChar('?', true) || !fEntityReader.lookingAtChar('>', true)) {
            int majorCode, minorCode;
            if (scanningTextDecl) {
                majorCode = XMLMessages.MSG_TEXTDECL_UNTERMINATED;
                minorCode = XMLMessages.P77_UNTERMINATED;
            } else {
                majorCode = XMLMessages.MSG_XMLDECL_UNTERMINATED;
                minorCode = XMLMessages.P23_UNTERMINATED;
            }
            abortMarkup(majorCode, minorCode);
            return;
        }
        fScannerMarkupDepth--;
        if (!scanningTextDecl) {
            //
            // Now that we have hit '?>' we are done with XML decl. Call the
            // handler before returning.
            //
            fEventHandler.callStartDocument(version, encoding, standalone);
        }
    }
    //
    // From the standard:
    //
    // [39] element ::= EmptyElemTag | STag content ETag
    // [44] EmptyElemTag ::= '<' Name (S Attribute)* S? '/>'
    // [40] STag ::= '<' Name (S Attribute)* S? '>'
    // [41] Attribute ::= Name Eq AttValue
    // [10] AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] | Reference)* "'"
    // [67] Reference ::= EntityRef | CharRef
    // [68] EntityRef ::= '&' Name ';'
    // [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
    // [43] content ::= (element | CharData | Reference | CDSect | PI | Comment)*
    // [42] ETag ::= '</' Name S? '>'
    //
    // Note: We have already scanned Name.
    //
    boolean scanElement(int elementType) throws Exception
    {
        //
        // Scan for attributes
        //
        boolean greater = false;
        boolean slash = false;
        if (greater = fEntityReader.lookingAtChar('>', true)) {
            // no attributes
        } else if (fEntityReader.lookingAtSpace(true)) {
            int previousState = setScannerState(SCANNER_STATE_ATTRIBUTE_LIST);
            while (true) {
                fEntityReader.skipPastSpaces();
                //
                // [41] Attribute ::= Name Eq AttValue
                //
                if ((greater = fEntityReader.lookingAtChar('>', true)) || (slash = fEntityReader.lookingAtChar('/', true)))
                    break;
                //
                // Name
                //
                setScannerState(SCANNER_STATE_ATTRIBUTE_NAME);
                int attrName = fEventHandler.scanAttributeName(fEntityReader, elementType);
                if (attrName == -1) {
                    break;
                }
                //
                // Eq
                //
                fEntityReader.skipPastSpaces();
                if (!fEntityReader.lookingAtChar('=', true)) {
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        abortMarkup(XMLMessages.MSG_EQ_REQUIRED_IN_ATTRIBUTE,
                                    XMLMessages.P41_EQ_REQUIRED,
                                    elementType, attrName);
                        restoreScannerState(previousState);
                    }
                    return false;
                }
                fEntityReader.skipPastSpaces();
                int result = fEventHandler.scanAttValue(elementType, attrName);
                if (result == RESULT_FAILURE) {
                    if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                        skipPastEndOfCurrentMarkup();
                        restoreScannerState(previousState);
                    }
                    return false;
                } else if (result == RESULT_DUPLICATE_ATTR) {
                    reportFatalXMLError(XMLMessages.MSG_ATTRIBUTE_NOT_UNIQUE,
                                        XMLMessages.WFC_UNIQUE_ATT_SPEC,
                                        elementType, attrName);
                }
                restoreScannerState(SCANNER_STATE_ATTRIBUTE_LIST);
                if (!fEntityReader.lookingAtSpace(true)) {
                    if (!(greater = fEntityReader.lookingAtChar('>', true)))
                        slash = fEntityReader.lookingAtChar('/', true);
                    break;
                }
            }
            restoreScannerState(previousState);
        } else {
            slash = fEntityReader.lookingAtChar('/', true);
        }
        if (!greater && (!slash || !fEntityReader.lookingAtChar('>', true))) { // '>' or '/>'
            if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                abortMarkup(XMLMessages.MSG_ELEMENT_UNTERMINATED,
                            XMLMessages.P40_UNTERMINATED,
                            elementType);
            }
            return false;
        }
        fEventHandler.callStartElement(elementType);
        fScannerMarkupDepth--;
        if (slash) { // '/>'
            fEventHandler.callEndElement(fReaderId);
            return false;
        } else {
            return true;
        }
    }
    //
    // [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
    //
    int scanCharRef() throws Exception {
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
    void scanComment() throws Exception
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
                        fScannerMarkupDepth--;
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
    void scanPI(int piTarget) throws Exception
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
        int piDataLength = -1;
        if (!fEntityReader.lookingAtSpace(true)) {
            if (!fEntityReader.lookingAtChar('?', true) || !fEntityReader.lookingAtChar('>', true)) {
                if (fScannerState != SCANNER_STATE_END_OF_INPUT) {
                    abortMarkup(XMLMessages.MSG_SPACE_REQUIRED_IN_PI,
                                XMLMessages.P16_WHITESPACE_REQUIRED);
                    restoreScannerState(prevState);
                }
                return;
            }
            piDataLength = 0;
        } else {
            fEntityReader.skipPastSpaces();
            piDataOffset = fEntityReader.currentOffset();
            while (fScannerState == SCANNER_STATE_PI) {
                while (fEntityReader.lookingAtChar('?', false)) {
                    int offset = fEntityReader.currentOffset();
                    fEntityReader.lookingAtChar('?', true);
                    if (fEntityReader.lookingAtChar('>', true)) {
                        piDataLength = offset - piDataOffset;
                        break;
                    }
                }
                if (piDataLength >= 0)
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
        fScannerMarkupDepth--;
        restoreScannerState(prevState);
        int piData = piDataLength == 0 ?
                     StringPool.EMPTY_STRING : fEntityReader.addString(piDataOffset, piDataLength);
        fEventHandler.callProcessingInstruction(piTarget, piData);
    }
}
