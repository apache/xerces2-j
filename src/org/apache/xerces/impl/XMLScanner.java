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

package org.apache.xerces.impl;

import java.io.EOFException;
import java.io.IOException;

import org.apache.xerces.impl.XMLEntityScanner;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLComponent;
import org.apache.xerces.xni.XMLComponentManager;
import org.apache.xerces.xni.XMLString;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * This class is responsible for holding scanning methods common to
 * scanning the XML document structure and content as well as the DTD
 * structure and content. Both XMLDocumentScanner and XMLDTDScanner inherit
 * from this base class.
 *
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-manager</li>
 * </ul>
 *
 * @author Andy Clark, IBM
 * @author Arnaud  Le Hors, IBM
 *
 * @version $Id$
 */
public abstract class XMLScanner 
    implements XMLComponent {

    //
    // Data
    //

    // properties

    /** Symbol table. */
    protected SymbolTable fSymbolTable;

    /** Entity manager. */
    protected XMLEntityManager fEntityManager;

    // protected data

    /** Entity scanner. */
    protected XMLEntityScanner fEntityScanner;

    /** Entity depth. */
    protected int fEntityDepth;

    /** String. */
    protected XMLString fString = new XMLString();

    /** String buffer. */
    protected XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    /** String buffer. */
    protected XMLStringBuffer fStringBuffer2 = new XMLStringBuffer();

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;
    
    /** Attribute entity stack. */
    protected AttrEntityStack fAttributeEntityStack = new AttrEntityStack();

    /** Attribute value offset. */
    protected int fAttributeOffset;

    /** Scanning attribute. */
    protected boolean fScanningAttribute;

    // debugging

    /** Debug attribute entities. */
    protected static final boolean DEBUG_ATTR_ENTITIES = false;

    // private data

    // symbols

    /** Symbol: "version". */
    protected String fVersionSymbol;

    /** Symbol: "encoding". */
    protected String fEncodingSymbol;

    /** Symbol: "standalone". */
    protected String fStandaloneSymbol;

    /** Symbol: "amp". */
    protected String fAmpSymbol;

    /** Symbol: "lt". */
    protected String fLtSymbol;

    /** Symbol: "gt". */
    protected String fGtSymbol;

    /** Symbol: "quot". */
    protected String fQuotSymbol;

    /** Symbol: "apos". */
    protected String fAposSymbol;

    // pseudo-attribute values

    /** Pseudo-attribute string buffer. */
    private XMLStringBuffer fPseudoAttrStringBuffer = new XMLStringBuffer();

    //
    // XMLComponent methods
    //

    /**
     * 
     * 
     * @param componentManager The component manager.
     *
     * @throws SAXException Throws exception if required features and
     *                      properties cannot be found.
     */
    public void reset(XMLComponentManager componentManager)
        throws SAXException {

        // Xerces properties
        final String SYMBOL_TABLE = Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
        fSymbolTable = (SymbolTable)componentManager.getProperty(SYMBOL_TABLE);
        final String ERROR_REPORTER = Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        final String ENTITY_MANAGER = Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
        fEntityManager = (XMLEntityManager)componentManager.getProperty(ENTITY_MANAGER);

        // initialize scanner
        fEntityScanner = fEntityManager.getEntityScanner();
        
        // initialize vars
        fEntityDepth = 0;

        // save built-in entity names
        fVersionSymbol = fSymbolTable.addSymbol("version");
        fEncodingSymbol = fSymbolTable.addSymbol("encoding");
        fStandaloneSymbol = fSymbolTable.addSymbol("standalone");
        fAmpSymbol = fSymbolTable.addSymbol("amp");
        fLtSymbol = fSymbolTable.addSymbol("lt");
        fGtSymbol = fSymbolTable.addSymbol("gt");
        fQuotSymbol = fSymbolTable.addSymbol("quot");
        fAposSymbol = fSymbolTable.addSymbol("apos");

    } // reset(XMLComponentManager)

    /**
     * Sets the value of a property during parsing.
     * 
     * @param propertyId 
     * @param value 
     */
    public void setProperty(String propertyId, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
        
        // Xerces properties
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            String property =
               propertyId.substring(Constants.XERCES_PROPERTY_PREFIX.length());
            if (property.equals(Constants.SYMBOL_TABLE_PROPERTY)) {
                fSymbolTable = (SymbolTable)value;
            }
            else if (property.equals(Constants.ERROR_REPORTER_PROPERTY)) {
                fErrorReporter = (XMLErrorReporter)value;
            }
            else if (property.equals(Constants.ENTITY_MANAGER_PROPERTY)) {
                fEntityManager = (XMLEntityManager)value;
            }
            return;
        }

    } // setProperty(String,Object)

    //
    // Protected methods
    //

    // common scanning methods

    /**
     * Scans an XML or text declaration.
     * <p>
     * <pre>
     * [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
     * [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
     * [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
     * [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
     * [32] SDDecl ::= S 'standalone' Eq (("'" ('yes' | 'no') "'")
     *                 | ('"' ('yes' | 'no') '"'))
     *
     * [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
     * </pre>
     *
     * @param scanningTextDecl True if a text declaration is to
     *                         be scanned instead of an XML
     *                         declaration.
     * @param pseudoAttributeValues An array of size 3 to return the version,
     *                         encoding and standalone pseudo attribute values
     *                         (in that order).
     *
     * <strong>Note:</strong> This method uses fString, anything in it
     * at the time of calling is lost.
     */
    protected void scanXMLDeclOrTextDecl(boolean scanningTextDecl,
                                         String[] pseudoAttributeValues) 
        throws IOException, SAXException {

        // pseudo-attribute values
        String version = null;
        String encoding = null;
        String standalone = null;

        // scan pseudo-attributes
        final int STATE_VERSION = 0;
        final int STATE_ENCODING = 1;
        final int STATE_STANDALONE = 2;
        final int STATE_DONE = 3;
        int state = STATE_VERSION;
        fEntityScanner.skipSpaces();
        while (fEntityScanner.peekChar() != '?') {
            String name = scanPseudoAttribute(scanningTextDecl, fString);
            switch (state) {
                case STATE_VERSION: {
                    if (name == fVersionSymbol) {
                        version = fString.toString();
                        state = STATE_ENCODING;
                        if (!version.equals("1.0")) {
                            reportFatalError("VersionNotSupported", 
                                             new Object[]{version});
                        }
                    }
                    else if (name == fEncodingSymbol) {
                        if (!scanningTextDecl) {
                            reportFatalError("VersionInfoRequired", null);
                        }
                        encoding = fString.toString();
                        state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                    }
                    else {
                        if (scanningTextDecl) {
                            reportFatalError("EncodingDeclRequired", null);
                        }
                        else {
                            reportFatalError("VersionInfoRequired", null);
                        }
                    }
                    break;
                }
                case STATE_ENCODING: {
                    if (name == fEncodingSymbol) {
                        encoding = fString.toString();
                        state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                        // TODO: check encoding name; set encoding on
                        //       entity scanner
                    }
                    else if (!scanningTextDecl && name == fStandaloneSymbol) {
                        standalone = fString.toString();
                        state = STATE_DONE;
                        if (!standalone.equals("yes") && !standalone.equals("no")) {
                            reportFatalError("SDDeclInvalid", null);
                        }
                    }
                    else {
                        reportFatalError("EncodingDeclRequired", null);
                    }
                    break;
                }
                case STATE_STANDALONE: {
                    if (name == fStandaloneSymbol) {
                        standalone = fString.toString();
                        state = STATE_DONE;
                        if (!standalone.equals("yes") && !standalone.equals("no")) {
                            reportFatalError("SDDeclInvalid", null);
                        }
                    }
                    else {
                        reportFatalError("EncodingDeclRequired", null);
                    }
                    break;
                }
                default: {
                    reportFatalError("NoMorePseudoAttributes", null);
                }
            }
            fEntityScanner.skipSpaces();
        }
        if (scanningTextDecl && state != STATE_DONE) {
            reportFatalError("MorePseudoAttributes", null);
        }

        // end
        if (!fEntityScanner.skipChar('?')) {
            reportFatalError("XMLDeclUnterminated", null);
        }
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("XMLDeclUnterminated", null);

        }

        // fill in return array
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;

    } // scanXMLDeclOrTextDecl(boolean)

    /**
     * Scans a pseudo attribute.
     *
     * @param scanningTextDecl True if scanning this pseudo-attribute for a
     *                         TextDecl; false if scanning XMLDecl. This 
     *                         flag is needed to report the correct type of
     *                         error.
     * @param value            The string to fill in with the attribute 
     *                         value.
     *
     * @return The name of the attribute
     *
     * <strong>Note:</strong> This method uses fPseudoAttrStringBuffer, anything in it
     * at the time of calling is lost.
     */
    public String scanPseudoAttribute(boolean scanningTextDecl, 
                                      XMLString value) 
        throws IOException, SAXException {

        String name = fEntityScanner.scanName();
        if (name == null) {
            reportFatalError("PseudoAttrNameExpected", null);
        }
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            reportFatalError("EqRequiredInTextDecl", new Object[]{name});
        }
        fEntityScanner.skipSpaces();
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
            reportFatalError("QuoteRequiredInTextDecl", new Object[]{name});
        }
        fEntityScanner.scanChar();
        int c = fEntityScanner.scanLiteral(quote, value);
        if (c != quote) {
            fPseudoAttrStringBuffer.clear();
            do {
                fPseudoAttrStringBuffer.append(value);
                if (c != -1) {
                    if (c == '&' || c == '%' || c == '<' || c == ']') {
                        fPseudoAttrStringBuffer.append((char)fEntityScanner.scanChar());
                    }
                    else if (XMLChar.isInvalid(c)) {
                        String key = scanningTextDecl 
                                   ? "InvalidCharInTextDecl" 
                                   : "InvalidCharInXMLDecl";
                        reportFatalError(key,
                                       new Object[] {Integer.toString(c, 16)});
                        fEntityScanner.scanChar();
                    }
                }
                c = fEntityScanner.scanLiteral(quote, value);
            } while (c != quote);
            fPseudoAttrStringBuffer.append(value);
            value.setValues(fPseudoAttrStringBuffer);
        }
        if (!fEntityScanner.skipChar(quote)) {
            reportFatalError("CloseQuoteMissingInTextDecl",
                             new Object[]{name});
        }

        // return
        return name;

    } // scanPseudoAttribute(XMLString):String
    
    /**
     * Scans a processing instruction.
     * <p>
     * <pre>
     * [16] PI ::= '&lt;?' PITarget (S (Char* - (Char* '?>' Char*)))? '?>'
     * [17] PITarget ::= Name - (('X' | 'x') ('M' | 'm') ('L' | 'l'))
     * </pre>
     * <strong>Note:</strong> This method uses fString, anything in it
     * at the time of calling is lost.
     */
    protected void scanPI() throws IOException, SAXException {

        // target
        String target = fEntityScanner.scanName();
        if (target == null) {
            reportFatalError("PITargetRequired", null);
        }

        // scan data
        scanPIData(target, fString);

    } // scanPI()

    /**
     * Scans a processing data. This is needed to handle the situation
     * where a document starts with a processing instruction whose 
     * target name <em>starts with</em> "xml". (e.g. xmlfoo)
     *
     * <strong>Note:</strong> This method uses fStringBuffer, anything in it
     * at the time of calling is lost.
     *
     * @param target The PI target
     * @param data The string to fill in with the data
     */
    protected void scanPIData(String target, XMLString data) 
        throws IOException, SAXException {

        // check target
        if (target.length() == 3) {
            char c0 = Character.toLowerCase(target.charAt(0));
            char c1 = Character.toLowerCase(target.charAt(1));
            char c2 = Character.toLowerCase(target.charAt(2));
            if (c0 == 'x' && c1 == 'm' && c2 == 'l') {
                reportFatalError("ReservedPITarget", null);
            }
        }

        // spaces
        if (!fEntityScanner.skipSpaces()) {
            if (fEntityScanner.skipString("?>")) {
                // we found the end, there is no data
                data.clear();
                return;
            }
            else {
                // if there is data there should be some space
                reportFatalError("SpaceRequiredInPI", null);
            }
        }

        // data
        if (fEntityScanner.scanData("?>", data)) {
            fStringBuffer.clear();
            do {
                fStringBuffer.append(data);
                int c = fEntityScanner.peekChar();
                if (c != -1 && XMLChar.isInvalid(c)) {
                    reportFatalError("InvalidCharInPI",
                                     new Object[] { Integer.toHexString(c) });
                    fEntityScanner.scanChar();
                }
            } while (fEntityScanner.scanData("?>", data));
            fStringBuffer.append(data);
            data.setValues(fStringBuffer);
        }

    } // scanPIData(String,XMLString)

    /**
     * Scans a comment.
     * <p>
     * <pre>
     * [15] Comment ::= '&lt!--' ((Char - '-') | ('-' (Char - '-')))* '-->'
     * </pre>
     * <p>
     * <strong>Note:</strong> Called after scanning past '&lt;!--'
     * <strong>Note:</strong> This method uses fString, anything in it
     * at the time of calling is lost.
     *
     * @param text The buffer to fill in with the text.
     */
    protected void scanComment(XMLStringBuffer text)
        throws IOException, SAXException {

        // text
        // REVISIT: handle invalid character, eof
        text.clear();
        while (fEntityScanner.scanData("--", fString)) {
            text.append(fString);
            /***/
            int c = fEntityScanner.peekChar();
            if (XMLChar.isInvalid(c)) {
                reportFatalError("InvalidCharInComment",
                                 new Object[] { Integer.toHexString(c) }); 
                fEntityScanner.scanChar();
            }
        }
        text.append(fString);
        if (!fEntityScanner.skipChar('>')) {
            reportFatalError("DashDashInComment", null);
        }

    } // scanComment()

    /**
     * Scans an attribute value.
     * 
     * [10] AttValue ::= '"' ([^<&"] | Reference)* '"' | "'" ([^<&'] | Reference)* "'"
     *
     * @param value The XMLString to fill in with the value.
     * @param atName The name of the attribute being parsed (for error msgs).
     * @param attributes The attributes list for the scanned attribute.
     * @param attrIndex The index of the attribute to use from the list.
     *
     * <strong>Note:</strong> This method uses fStringBuffer2, anything in it
     * at the time of calling is lost.
     **/
    protected void scanAttributeValue(XMLString value, String atName,
                                      XMLAttributes attributes, int attrIndex)
        throws IOException, SAXException
    {
        // quote
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
            reportFatalError("OpenQuoteExpected", new Object[]{atName});
        }

        fEntityScanner.scanChar();
        int entityDepth = fEntityDepth;

        int c = fEntityScanner.scanLiteral(quote, value);
        if (c != quote) {
            fScanningAttribute = true;
            if (DEBUG_ATTR_ENTITIES) {
                System.out.println("*** reset attribute entity stack");
            }
            fAttributeEntityStack.reset(attributes, attrIndex);
            fAttributeOffset = 0;
            if (DEBUG_ATTR_ENTITIES) {
                System.out.println("*** set attribute offset: "+fAttributeOffset);
            }
            fStringBuffer2.clear();
            do {
                fStringBuffer2.append(value);
                fAttributeOffset += value.length;
                if (DEBUG_ATTR_ENTITIES) {
                    System.out.println("*** increment attribute offset: "+fAttributeOffset);
                }
                if (c == '&') {
                    fEntityScanner.skipChar('&');
                    if (fEntityScanner.skipChar('#')) {
                        int ch = scanCharReferenceValue(fStringBuffer2);
                        if (ch != -1) {
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                    }
                    else {
                        String entityName = fEntityScanner.scanName();
                        if (entityName == null) {
                            reportFatalError("NameRequiredInReference", null);
                        }
                        if (!fEntityScanner.skipChar(';')) {
                            reportFatalError("SemicolonRequiredInReference",
                                             null);
                        }
                        if (entityName == fAmpSymbol) {
                            fStringBuffer2.append('&');
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                        else if (entityName == fAposSymbol) {
                            fStringBuffer2.append('\'');
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                        else if (entityName == fLtSymbol) {
                            fStringBuffer2.append('<');
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                        else if (entityName == fGtSymbol) {
                            fStringBuffer2.append('>');
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                        else if (entityName == fQuotSymbol) {
                            fStringBuffer2.append('"');
                            fAttributeOffset++;
                            if (DEBUG_ATTR_ENTITIES) {
                                System.out.println("*** increment attribute offset: "+fAttributeOffset);
                            }
                        }
                        else {
                            if (fEntityManager.isExternalEntity(entityName)) {
                                reportFatalError("ReferenceToExternalEntity",
                                                 new Object[] { entityName });
                            }
                            else {
                                fEntityManager.startEntity(entityName, false);
                            }
                        }
                    }
                }
                else if (c == '<') {
                    reportFatalError("LessthanInAttValue",
                                     new Object[] { null, atName });
                }
                else if (c == '%') {
                    fStringBuffer2.append((char)fEntityScanner.scanChar());
                }
                else if (c != -1 && XMLChar.isHighSurrogate(c)) {
                    scanSurrogates(fStringBuffer2);
                }
                else if (c != -1 && XMLChar.isInvalid(c)) {
                    reportFatalError("InvalidCharInAttValue",
                                     new Object[] {Integer.toString(c, 16)});
                    fEntityScanner.scanChar();
                }
                while (true) {
                    c = fEntityScanner.scanLiteral(quote, value);
                    if (c != quote || entityDepth == fEntityDepth) {
                        break;
                    }
                    fStringBuffer2.append(value);
                    fStringBuffer2.append((char)fEntityScanner.scanChar());
                }
            } while (c != quote);
            fAttributeOffset += value.length;
            fStringBuffer2.append(value);
            value.setValues(fStringBuffer2);
            int attrEntityCount = fAttributeEntityStack.size();
            if (DEBUG_ATTR_ENTITIES) {
                System.out.println("*** add remaining attribute entities: "+attrEntityCount);
            }
            for (int i = 0; i < attrEntityCount; i++) {
                if (DEBUG_ATTR_ENTITIES) {
                    System.out.println("*** popAttrEntity("+fAttributeOffset+')');
                }
                fAttributeEntityStack.popAttrEntity(fAttributeOffset);
            }
            fScanningAttribute = false;
        }

        // quote
        int cquote = fEntityScanner.scanChar();
        if (cquote != cquote) {
            reportFatalError("CloseQuoteExpected", new Object[]{atName});
        }
    } // scanAttributeValue()


    //
    // XMLEntityHandler methods
    //

    /**
     * This method notifies of the start of an entity. The document entity
     * has the pseudo-name of "[xml]"; the DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entities are just
     * specified by their name.
     * 
     * @param name     The name of the entity.
     * @param publicId The public identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param systemId The system identifier of the entity if the entity
     *                 is external, null otherwise.
     * @param encoding The auto-detected IANA encoding name of the entity
     *                 stream. This value will be null in those situations
     *                 where the entity encoding is not auto-detected (e.g.
     *                 internal entities or a document entity that is
     *                 parsed from a java.io.Reader).
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void startEntity(String name, String publicId, String systemId,
                            String encoding) throws SAXException {

        // keep track of the entity depth
        fEntityDepth++;

        // keep track of entities appearing in attribute values
        if (fScanningAttribute) {
            if (DEBUG_ATTR_ENTITIES) {
                System.out.println("*** pushAttrEntity("+name+','+fAttributeOffset+')');
            }
            fAttributeEntityStack.pushAttrEntity(name, fAttributeOffset);
        }
    } // startEntity(String,String,String,String)

    /**
     * This method notifies the end of an entity. The document entity has
     * the pseudo-name of "[xml]"; the DTD has the pseudo-name of "[dtd]; 
     * parameter entity names start with '%'; and general entities are just
     * specified by their name.
     * 
     * @param name The name of the entity.
     *
     * @throws SAXException Thrown by handler to signal an error.
     */
    public void endEntity(String name) throws SAXException {

        // keep track of the entity depth
        fEntityDepth--;

        // keep track of entities appearing in attribute values
        if (fScanningAttribute) {
            if (DEBUG_ATTR_ENTITIES) {
                System.out.println("*** popAttrEntity("+fAttributeOffset+") \""+name+'"');
            }
            fAttributeEntityStack.popAttrEntity(fAttributeOffset);
        }
    }



    /**
     * Scans a character reference and append the corresponding chars to the
     * specified buffer.
     *
     * <p>
     * <pre>
     * [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
     * </pre>
     *
     * <strong>Note:</strong> This method uses fStringBuffer, anything in it
     * at the time of calling is lost.
     *
     * @param buf the character buffer to append chars to
     *
     * @return the character value
     */
    protected int scanCharReferenceValue(XMLStringBuffer buf) 
        throws IOException, SAXException {

        // scan hexadecimal value
        boolean hex = false;
        if (fEntityScanner.skipChar('x')) {
            hex = true;
            fStringBuffer.clear();
            boolean digit = true;
            do {
                int c = fEntityScanner.peekChar();
                digit = (c >= '0' && c <= '9') ||
                        (c >= 'a' && c <= 'f') ||
                        (c >= 'A' && c <= 'F');
                if (digit) {
                    fEntityScanner.scanChar();
                    fStringBuffer.append((char)c);
                }
            } while (digit);
        }

        // scan decimal value
        else {
            fStringBuffer.clear();
            boolean digit = true;
            do {
                int c = fEntityScanner.peekChar();
                digit = c >= '0' && c <= '9';
                if (digit) {
                    fEntityScanner.scanChar();
                    fStringBuffer.append((char)c);
                }
            } while (digit);
        }

        // end
        if (!fEntityScanner.skipChar(';')) {
            reportFatalError("SemicolonRequiredInCharRef", null);
        }
        
        // convert string to number
        int value = -1;
        try {
            value = Integer.parseInt(fStringBuffer.toString(),
                                     hex ? 16 : 10);
        }
        catch (NumberFormatException e) {
            // let -1 value drop through
        }

        // character reference must be a valid XML character
        if (!XMLChar.isValid(value)) {
            reportFatalError("InvalidCharRef",
                             new Object[]{Integer.toString(value, 16)}); 
        }

        // append corresponding chars to the given buffer
        if (!XMLChar.isSupplemental(value)) {
            buf.append((char) value);
        }
        else {
            // character is supplemental, split it into surrogate chars
            buf.append(XMLChar.highSurrogate(value));
            buf.append(XMLChar.lowSurrogate(value));
        }

        return value;
    }


    /**
     * Scans surrogates and append them to the specified buffer.
     * <p>
     * <strong>Note:</strong> This assumes the current char has already been
     * identified as a high surrogate.
     *
     * @returns True if it succeeded.
     */
    protected boolean scanSurrogates(XMLStringBuffer buf)
        throws IOException, SAXException {

        int high = fEntityScanner.scanChar();
        int low = fEntityScanner.peekChar();
        if (!XMLChar.isLowSurrogate(low)) {
            reportFatalError("InvalidCharInContent",
                             new Object[] {Integer.toString(high, 16)});
            return false;
        }
        fEntityScanner.scanChar();

        // fill in the buffer
        buf.append((char)high);
        buf.append((char)low);

        return true;

    } // scanSurrogates():boolean



    /**
     * Convenience function used in all XML scanners.
     */
    protected void reportFatalError(String msgId, Object[] args)
        throws SAXException {
        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                   msgId, args,
                                   XMLErrorReporter.SEVERITY_FATAL_ERROR);
    }


    /**
     * A stack for keeping track of entity offsets and lengths in
     * attribute values. This stack adds the attribute entities to
     * a specified XMLAttribute object.
     *
     * @author Andy Clark, IBM
     */
    protected static class AttrEntityStack {

        //
        // Data
        //

        /** Attributes. */
        protected XMLAttributes fAttributes;

        /** The index of the attribute where to add entities. */
        protected int fAttributeIndex;

        // stack information

        /** The size of the stack. */
        protected int fSize;

        /** The entity indexes on the stack. */
        protected int[] fEntityIndexes = new int[4];

        //
        // Public methods
        //

        /** 
         * Resets the attribute entity stack and sets the attributes
         * object to add entities to.
         *
         * @param attributes The attributes object where new attribute
         *                   entities are added.
         * @param attrIndex  The index of the attribute where to add
         *                   entities.
         */
        public void reset(XMLAttributes attributes, int attrIndex) {
            fAttributes = attributes;
            fAttributeIndex = attrIndex;
            fSize = 0;
        } // reset(XMLAttributes,int)

        /** Returns the size of the stack. */
        public int size() {
            return fSize;
        } // size():int

        /** 
         * Pushes a new entity onto the stack. 
         *
         * @param entityName   The entity name.
         * @param entityOffset The entity offset.
         */
        public void pushAttrEntity(String entityName, int entityOffset) {
            if (fSize == fEntityIndexes.length) {
                int[] indexarray = new int[fEntityIndexes.length * 2];
                System.arraycopy(fEntityIndexes, 0, indexarray, 0, fEntityIndexes.length);
                fEntityIndexes = indexarray;
            }
            fEntityIndexes[fSize] = 
                fAttributes.addAttributeEntity(fAttributeIndex, entityName, 
                                               entityOffset, -1);
            fSize++;
        } // pushAttrEntity(String,int)

        /**
         * Pops the current entity off of the stack and adds it to the
         * list of entities for the attribute in the XMLAttributes object.
         *
         * @param endOffset The entity's ending offset.
         */
        public void popAttrEntity(int endOffset) {
            fSize--;
            int entityIndex = fEntityIndexes[fSize];
            int offset = fAttributes.getEntityOffset(fAttributeIndex, entityIndex);
            int length = endOffset - offset;
            fAttributes.setEntityLength(fAttributeIndex, entityIndex, length);
        } // popAttrEntity(int)

    } // class AttrEntityStack

} // class XMLScanner
