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

    /** String. */
    protected XMLString fString = new XMLString();

    /** String buffer. */
    protected XMLStringBuffer fStringBuffer = new XMLStringBuffer();

    /** Error reporter. */
    protected XMLErrorReporter fErrorReporter;


    // private data

    // symbols

    /** Symbol: "version". */
    private String fVersionSymbol;

    /** Symbol: "encoding". */
    private String fEncodingSymbol;

    /** Symbol: "standalone". */
    private String fStandaloneSymbol;

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
        
        // save built-in entity names
        fVersionSymbol = fSymbolTable.addSymbol("version");
        fEncodingSymbol = fSymbolTable.addSymbol("encoding");
        fStandaloneSymbol = fSymbolTable.addSymbol("standalone");

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
            String name = scanPseudoAttribute(fString);
            switch (state) {
                case STATE_VERSION: {
                    if (name == fVersionSymbol) {
                        version = fString.toString();
                        state = STATE_ENCODING;
                        if (!version.equals("1.0")) {
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "VersionNotSupported", 
                                                       new Object[]{version}, 
                                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
                        }
                    }
                    else if (name == fEncodingSymbol) {
                        if (!scanningTextDecl) {
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "VersionInfoRequired", 
                                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                        }
                        encoding = fString.toString();
                        state = scanningTextDecl ? STATE_DONE : STATE_STANDALONE;
                    }
                    else {
                        if (scanningTextDecl) {
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "EncodingDeclRequired", 
                                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                        }
                        else {
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "VersionInfoRequired", 
                                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
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
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "SDDeclInvalid", 
                                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                        }
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                   "EncodingDeclRequired", 
                                                   null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    break;
                }
                case STATE_STANDALONE: {
                    if (name == fStandaloneSymbol) {
                        standalone = fString.toString();
                        state = STATE_DONE;
                        if (!standalone.equals("yes") && !standalone.equals("no")) {
                            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                       "SDDeclInvalid", 
                                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                        }
                    }
                    else {
                        fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                                   "EncodingDeclRequired", 
                                                   null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                    }
                    break;
                }
                default: {
                    fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                               "NoMorePseudoAttributes", 
                                               null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
                }
            }
            fEntityScanner.skipSpaces();
        }
        if (scanningTextDecl && state != STATE_DONE) {
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN, 
                                       "MorePseudoAttributes", 
                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }

        // end
        if (!fEntityScanner.skipChar('?')) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "XMLDeclUnterminated", 
                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        if (!fEntityScanner.skipChar('>')) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "XMLDeclUnterminated", 
                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);

        }

        // fill in return array
        pseudoAttributeValues[0] = version;
        pseudoAttributeValues[1] = encoding;
        pseudoAttributeValues[2] = standalone;

    } // scanXMLDeclOrTextDecl(boolean)

    /**
     * Scans a pseudo attribute.
     *
     * @param value The string to fill in with the attribute value
     * @return The name of the attribute
     *
     * <strong>Note:</strong> This method uses fString, anything in it
     * at the time of calling is lost.
     */
    public String scanPseudoAttribute(XMLString value) 
        throws IOException, SAXException {

        String name = fEntityScanner.scanName();
        if (name == null) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "PseudoAttrNameExpected", 
                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        fEntityScanner.skipSpaces();
        if (!fEntityScanner.skipChar('=')) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "EqRequiredInTextDecl", 
                                       new Object[]{name}, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        fEntityScanner.skipSpaces();
        int quote = fEntityScanner.peekChar();
        if (quote != '\'' && quote != '"') {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "QuoteRequiredInTextDecl", 
                                       new Object[]{name}, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        fEntityScanner.scanChar();
        if (fEntityScanner.scanLiteral(quote, value) != quote) {
            fStringBuffer.clear();
            do {
                fStringBuffer.append(value);
            } while (fEntityScanner.scanLiteral(quote, value) != quote);
            fStringBuffer.append(value);
            value.setValues(fStringBuffer);
        }
        if (!fEntityScanner.skipChar(quote)) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "CloseQuoteMissingInTextDecl", 
                                       new Object[]{name}, XMLErrorReporter.SEVERITY_FATAL_ERROR);
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
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "PITargetRequired", 
                                        null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
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
                fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "ReservedPITarget", 
                                            null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
            }
        }

        // data
        // REVISIT: handle invalid character, eof
        if (fEntityScanner.scanData("?>", data)) {
            fStringBuffer.clear();
            do {
                fStringBuffer.append(data);
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
        }
        text.append(fString);
        if (!fEntityScanner.skipChar('>')) {
            fErrorReporter.reportError( XMLMessageFormatter.XML_DOMAIN, "DashDashInComment", 
                                        null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }

    } // scanComment()


    /**
     * Scans a character reference.
     * <p>
     * <pre>
     * [66] CharRef ::= '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
     * </pre>
     *
     * <strong>Note:</strong> This method uses fStringBuffer, anything in it
     * at the time of calling is lost.
     *
     * @return the character value
     */
    protected int scanCharReferenceValue() 
        throws IOException, SAXException {

        // scan hexadecimal value
        boolean hex = false;
        if (fEntityScanner.skipChar('x')) {
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
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "SemicolonRequiredInCharRef",
                                       null, XMLErrorReporter.SEVERITY_FATAL_ERROR);
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
        if (!XMLChar.isValid((char)value)) {
            fErrorReporter.reportError(XMLMessageFormatter.XML_DOMAIN,
                                       "InvalidCharRef",
                                       new Object[]{ String.valueOf((char)value) },
                                       XMLErrorReporter.SEVERITY_FATAL_ERROR);
        }
        return value;
    }

} // class XMLScanner
