/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.
 * All rights reserved.
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
import java.io.FileInputStream;
import java.io.FilterReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.io.ASCIIReader;
import org.apache.xerces.impl.io.UCSReader;
import org.apache.xerces.impl.io.UTF8Reader;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;

import org.apache.xerces.util.EncodingMap;
import org.apache.xerces.util.XMLStringBuffer;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.URI;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XMLResourceIdentifierImpl;

import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * The entity manager handles the registration of general and parameter
 * entities; resolves entities; and starts entities. The entity manager
 * is a central component in a standard parser configuration and this
 * class works directly with the entity scanner to manage the underlying
 * xni.
 * <p>
 * This component requires the following features and properties from the
 * component manager that uses it:
 * <ul>
 *  <li>http://xml.org/sax/features/validation</li>
 *  <li>http://xml.org/sax/features/external-general-entities</li>
 *  <li>http://xml.org/sax/features/external-parameter-entities</li>
 *  <li>http://apache.org/xml/features/allow-java-encodings</li>
 *  <li>http://apache.org/xml/properties/internal/symbol-table</li>
 *  <li>http://apache.org/xml/properties/internal/error-reporter</li>
 *  <li>http://apache.org/xml/properties/internal/entity-resolver</li>
 * </ul>
 *
 *
 * @author Andy Clark, IBM
 * @author Arnaud  Le Hors, IBM
 *
 * @version $Id$
 */
public class XML11EntityManager
    extends XMLEntityManager {

    //
    // Constructors
    //

    /** Default constructor. */
    public XML11EntityManager() {
        this(null);
    } // <init>()

    /**
     * Constructs an entity manager that shares the specified entity
     * declarations during each parse.
     * <p>
     * <strong>REVISIT:</strong> We might want to think about the "right"
     * way to expose the list of declared entities. For now, the knowledge
     * how to access the entity declarations is implicit.
     */
    public XML11EntityManager(XMLEntityManager entityManager) {

        // create scanner
        super(entityManager);

    } // <init>(XMLEntityManager)

    //
    // Protected methods
    //

    protected XMLEntityScanner createEntityScanner() {
        return new XML11EntityScanner();
    } // createEntityScanner():  XMLEntityScanner
    // Classes

    /**
     * Implements the entity scanner methods.
     *
     * @author Andy Clark, IBM
     */
    protected class XML11EntityScanner
        extends XMLEntityManager.EntityScanner {

        //
        // Constructors
        //

        /** Default constructor. */
        public XML11EntityScanner() {
            super();
        } // <init>()

        //
        // XMLEntityScanner methods
        //

        /**
         * Returns the next character on the input.
         * <p>
         * <strong>Note:</strong> The character is <em>not</em> consumed.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public int peekChar() throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            // peek at character
            int c = fCurrentEntity.ch[fCurrentEntity.position];

            // return peeked character
            if (fCurrentEntity.isExternal()) {
                return (c != '\r' && c != 0x85 && c != 0x2028) ? c : '\n';
            }
            else {
                return c;
            }

        } // peekChar():int

        /**
         * Returns the next character on the input.
         * <p>
         * <strong>Note:</strong> The character is consumed.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public int scanChar() throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            // scan character
            int c = fCurrentEntity.ch[fCurrentEntity.position++];
            boolean external = false;
            if (c == '\n' ||
                ((c == '\r' || c == 0x85 || c == 0x2028) && (external = fCurrentEntity.isExternal()))) {
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber = 1;
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    fCurrentEntity.ch[0] = (char)c;
                    load(1, false);
                }
                if ((c == '\r' || c == 0x85) && external) {
                    if (fCurrentEntity.ch[fCurrentEntity.position++] != '\n') {
                        fCurrentEntity.position--;
                    }
                    c = '\n';
                }
            }

            // return character that was scanned
            fCurrentEntity.columnNumber++;
            return c;

        } // scanChar():int

        /**
         * Scans a range of parsed character data, setting the fields of the
         * XMLString structure, appropriately.
         * <p>
         * <strong>Note:</strong> The characters are consumed.
         * <p>
         * <strong>Note:</strong> This method does not guarantee to return
         * the longest run of parsed character data. This method may return
         * before markup due to reaching the end of the input buffer or any
         * other reason.
         * <p>
         * <strong>Note:</strong> The fields contained in the XMLString
         * structure are not guaranteed to remain valid upon subsequent calls
         * to the entity scanner. Therefore, the caller is responsible for
         * immediately using the returned character data or making a copy of
         * the character data.
         *
         * @param content The content structure to fill.
         *
         * @return Returns the next character on the input, if known. This
         *         value may be -1 but this does <em>note</em> designate
         *         end of file.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public int scanContent(XMLString content) throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }
            else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
                load(1, false);
                fCurrentEntity.position = 0;
            }

            // normalize newlines
            int offset = fCurrentEntity.position;
            int c = fCurrentEntity.ch[offset];
            int newlines = 0;
            boolean external = fCurrentEntity.isExternal();
            if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
                do {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if ((c == '\r' || c == 0x85) && external) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                        if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                            fCurrentEntity.position++;
                            offset++;
                        }
                        /*** NEWLINE NORMALIZATION ***/
                        else {
                            newlines++;
                        }
                    }
                    else if (c == '\n' || c == 0x2028) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.position--;
                        break;
                    }
                } while (fCurrentEntity.position < fCurrentEntity.count - 1);
                for (int i = offset; i < fCurrentEntity.position; i++) {
                    fCurrentEntity.ch[i] = '\n';
                }
                int length = fCurrentEntity.position - offset;
                if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                    content.setValues(fCurrentEntity.ch, offset, length);
                    return -1;
                }
            }

            // inner loop, scanning for content
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (!XMLChar.isContent(c) && c != 0x85 && c != 0x2028) {
                    fCurrentEntity.position--;
                    break;
                }
            }
            int length = fCurrentEntity.position - offset;
            fCurrentEntity.columnNumber += length - newlines;
            content.setValues(fCurrentEntity.ch, offset, length);

            // return next character
            if (fCurrentEntity.position != fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position];
                // REVISIT: Does this need to be updated to fix the
                //          #x0D ^#x0A newline normalization problem? -Ac
                if ((c == '\r' || c == 0x85 || c == 0x2028) && external) {
                    c = '\n';
                }
            }
            else {
                c = -1;
            }
            return c;

        } // scanContent(XMLString):int

        /**
         * Scans a range of attribute value data, setting the fields of the
         * XMLString structure, appropriately.
         * <p>
         * <strong>Note:</strong> The characters are consumed.
         * <p>
         * <strong>Note:</strong> This method does not guarantee to return
         * the longest run of attribute value data. This method may return
         * before the quote character due to reaching the end of the input
         * buffer or any other reason.
         * <p>
         * <strong>Note:</strong> The fields contained in the XMLString
         * structure are not guaranteed to remain valid upon subsequent calls
         * to the entity scanner. Therefore, the caller is responsible for
         * immediately using the returned character data or making a copy of
         * the character data.
         *
         * @param quote   The quote character that signifies the end of the
         *                attribute value data.
         * @param content The content structure to fill.
         *
         * @return Returns the next character on the input, if known. This
         *         value may be -1 but this does <em>note</em> designate
         *         end of file.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public int scanLiteral(int quote, XMLString content)
            throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }
            else if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                fCurrentEntity.ch[0] = fCurrentEntity.ch[fCurrentEntity.count - 1];
                load(1, false);
                fCurrentEntity.position = 0;
            }

            // normalize newlines
            int offset = fCurrentEntity.position;
            int c = fCurrentEntity.ch[offset];
            int newlines = 0;
            boolean external = fCurrentEntity.isExternal();
            if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
                do {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if ((c == '\r' || c == 0x85) && external) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                        if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                            fCurrentEntity.position++;
                            offset++;
                        }
                        /*** NEWLINE NORMALIZATION ***/
                        else {
                            newlines++;
                        }
                    }
                    else if (c == '\n' || c == 0x2028) {
                        newlines++;
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count) {
                            offset = 0;
                            fCurrentEntity.position = newlines;
                            if (load(newlines, false)) {
                                break;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.position--;
                        break;
                    }
                } while (fCurrentEntity.position < fCurrentEntity.count - 1);
                for (int i = offset; i < fCurrentEntity.position; i++) {
                    fCurrentEntity.ch[i] = '\n';
                }
                int length = fCurrentEntity.position - offset;
                if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                    content.setValues(fCurrentEntity.ch, offset, length);
                    return -1;
                }
            }

            // scan literal value
            while (fCurrentEntity.position < fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position++];
                if ((c == quote &&
                     (!fCurrentEntity.literal || external))
                    || c == '%' || (!XMLChar.isContent(c) && c != 0x85 && c != 0x2028)) {
                    fCurrentEntity.position--;
                    break;
                }
            }
            int length = fCurrentEntity.position - offset;
            fCurrentEntity.columnNumber += length - newlines;
            content.setValues(fCurrentEntity.ch, offset, length);

            // return next character
            if (fCurrentEntity.position != fCurrentEntity.count) {
                c = fCurrentEntity.ch[fCurrentEntity.position];
                // NOTE: We don't want to accidentally signal the
                //       end of the literal if we're expanding an
                //       entity appearing in the literal. -Ac
                if (c == quote && fCurrentEntity.literal) {
                    c = -1;
                }
            }
            else {
                c = -1;
            }
            return c;

        } // scanLiteral(int,XMLString):int

        /**
         * Scans a range of character data up to the specicied delimiter,
         * setting the fields of the XMLString structure, appropriately.
         * <p>
         * <strong>Note:</strong> The characters are consumed.
         * <p>
         * <strong>Note:</strong> This assumes that the internal buffer is
         * at least the same size, or bigger, than the length of the delimiter
         * and that the delimiter contains at least one character.
         * <p>
         * <strong>Note:</strong> This method does not guarantee to return
         * the longest run of character data. This method may return before
         * the delimiter due to reaching the end of the input buffer or any
         * other reason.
         * <p>
         * <strong>Note:</strong> The fields contained in the XMLString
         * structure are not guaranteed to remain valid upon subsequent calls
         * to the entity scanner. Therefore, the caller is responsible for
         * immediately using the returned character data or making a copy of
         * the character data.
         *
         * @param delimiter The string that signifies the end of the character
         *                  data to be scanned.
         * @param data      The data structure to fill.
         *
         * @return Returns true if there is more data to scan, false otherwise.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public boolean scanData(String delimiter, XMLStringBuffer buffer)
            throws IOException {

            boolean done = false;
            int delimLen = delimiter.length();
            char charAt0 = delimiter.charAt(0);
            boolean external = fCurrentEntity.isExternal();
            do {
                // load more characters, if needed
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    load(0, true);
                }
                else if (fCurrentEntity.position >= fCurrentEntity.count - delimLen) {
                    System.arraycopy(fCurrentEntity.ch, fCurrentEntity.position,
                                     fCurrentEntity.ch, 0, fCurrentEntity.count - fCurrentEntity.position);
                    load(fCurrentEntity.count - fCurrentEntity.position, false);
                    fCurrentEntity.position = 0;
                }
    
                if (fCurrentEntity.position >= fCurrentEntity.count - delimLen) {
                    // something must be wrong with the input:  e.g., file ends  an unterminated comment
                    int length = fCurrentEntity.count - fCurrentEntity.position;
                    buffer.append (fCurrentEntity.ch, fCurrentEntity.position, length); 
                    fCurrentEntity.columnNumber += fCurrentEntity.count;
                    fCurrentEntity.position = fCurrentEntity.count;
                    load(0,true);
                    return false;
                }
    
                // normalize newlines
                int offset = fCurrentEntity.position;
                int c = fCurrentEntity.ch[offset];
                int newlines = 0;
                if (c == '\n' || ((c == '\r' || c == 0x85 || c == 0x2028) && external)) {
                    do {
                        c = fCurrentEntity.ch[fCurrentEntity.position++];
                        if ((c == '\r' || c == 0x85) && external) {
                            newlines++;
                            fCurrentEntity.lineNumber++;
                            fCurrentEntity.columnNumber = 1;
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                offset = 0;
                                fCurrentEntity.position = newlines;
                                if (load(newlines, false)) {
                                    break;
                                }
                            }
                            if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                                fCurrentEntity.position++;
                                offset++;
                            }
                            /*** NEWLINE NORMALIZATION ***/
                            else {
                                newlines++;
                            }
                        }
                        else if (c == '\n' || c == 0x2028) {
                            newlines++;
                            fCurrentEntity.lineNumber++;
                            fCurrentEntity.columnNumber = 1;
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                offset = 0;
                                fCurrentEntity.position = newlines;
                                fCurrentEntity.count = newlines;
                                if (load(newlines, false)) {
                                    break;
                                }
                            }
                        }
                        else {
                            fCurrentEntity.position--;
                            break;
                        }
                    } while (fCurrentEntity.position < fCurrentEntity.count - 1);
                    for (int i = offset; i < fCurrentEntity.position; i++) {
                        fCurrentEntity.ch[i] = '\n';
                    }
                    int length = fCurrentEntity.position - offset;
                    if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                        buffer.append(fCurrentEntity.ch, offset, length);
                        return true;
                    }
                }
    
                // iterate over buffer looking for delimiter
                OUTER: while (fCurrentEntity.position < fCurrentEntity.count) {
                    c = fCurrentEntity.ch[fCurrentEntity.position++];
                    if (c == charAt0) {
                        // looks like we just hit the delimiter
                        int delimOffset = fCurrentEntity.position - 1;
                        for (int i = 1; i < delimLen; i++) {
                            if (fCurrentEntity.position == fCurrentEntity.count) {
                                fCurrentEntity.position -= i;
                                break OUTER;
                            }
                            c = fCurrentEntity.ch[fCurrentEntity.position++];
                            if (delimiter.charAt(i) != c) {
                                fCurrentEntity.position--;
                                break;
                            }
                        }
                        if (fCurrentEntity.position == delimOffset + delimLen) {
                            done = true;
                            break;
                        }
                    }
                    else if (c == '\n' || (external && (c == '\r' || c == 0x85 || c == 0x2028))) {
                        fCurrentEntity.position--;
                        break;
                    }
                    else if (XMLChar.isInvalid(c)) {
                        fCurrentEntity.position--;
                        int length = fCurrentEntity.position - offset;
                        fCurrentEntity.columnNumber += length - newlines;
                        buffer.append(fCurrentEntity.ch, offset, length); 
                        return true;
                    }
                }
                int length = fCurrentEntity.position - offset;
                fCurrentEntity.columnNumber += length - newlines;
                if (done) {
                    length -= delimLen;
                }
                buffer.append(fCurrentEntity.ch, offset, length);
    
                // return true if string was skipped
            } while (!done);
            return !done;

        } // scanData(String,XMLString)

        /**
         * Skips a character appearing immediately on the input.
         * <p>
         * <strong>Note:</strong> The character is consumed only if it matches
         * the specified character.
         *
         * @param c The character to skip.
         *
         * @return Returns true if the character was skipped.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public boolean skipChar(int c) throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            // skip character
            int cc = fCurrentEntity.ch[fCurrentEntity.position];
            if (cc == c) {
                fCurrentEntity.position++;
                if (c == '\n') {
                    fCurrentEntity.lineNumber++;
                    fCurrentEntity.columnNumber = 1;
                }
                else {
                    fCurrentEntity.columnNumber++;
                }
                return true;
            }
            else if (c == '\n' && cc == 0x2028) {
                fCurrentEntity.position++;
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber = 1;
                return true;
            }
            else if (c == '\n' && (cc == '\r' || cc == 0x85 ) && fCurrentEntity.isExternal()) {
                // handle newlines
                if (fCurrentEntity.position == fCurrentEntity.count) {
                    fCurrentEntity.ch[0] = (char)cc;
                    load(1, false);
                }
                fCurrentEntity.position++;
                if (fCurrentEntity.ch[fCurrentEntity.position] == '\n') {
                    fCurrentEntity.position++;
                }
                fCurrentEntity.lineNumber++;
                fCurrentEntity.columnNumber = 1;
                return true;
            }

            // character was not skipped
            return false;

        } // skipChar(int):boolean

        /**
         * Skips space characters appearing immediately on the input.
         * <p>
         * <strong>Note:</strong> The characters are consumed only if they are
         * space characters.
         *
         * @return Returns true if at least one space character was skipped.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         *
         * @see org.apache.xerces.util.XMLChar#isSpace
         */
        public boolean skipSpaces() throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            // skip spaces
            int c = fCurrentEntity.ch[fCurrentEntity.position];
            if (XMLChar.isXML11Space(c)) {
                boolean external = fCurrentEntity.isExternal();
                do {
                    boolean entityChanged = false;
                    // handle newlines
                    if (c == '\n' || (external && (c == '\r' || c == 0x85 || c == 0x2028))) {
                        fCurrentEntity.lineNumber++;
                        fCurrentEntity.columnNumber = 1;
                        if (fCurrentEntity.position == fCurrentEntity.count - 1) {
                            fCurrentEntity.ch[0] = (char)c;
                            entityChanged = load(1, true);
                            if (!entityChanged)
                                // the load change the position to be 1,
                                // need to restore it when entity not changed
                                fCurrentEntity.position = 0;
                        }
                        if ((c == '\r' || c == 0x85) && external) {
                            // REVISIT: Does this need to be updated to fix the
                            //          #x0D ^#x0A newline normalization problem? -Ac
                            if (fCurrentEntity.ch[++fCurrentEntity.position] != '\n') {
                                fCurrentEntity.position--;
                            }
                        }
                    }
                    else {
                        fCurrentEntity.columnNumber++;
                    }
                    // load more characters, if needed
                    if (!entityChanged)
                        fCurrentEntity.position++;
                    if (fCurrentEntity.position == fCurrentEntity.count) {
                        load(0, true);
                    }
                } while (XMLChar.isXML11Space(c = fCurrentEntity.ch[fCurrentEntity.position]));
                return true;
            }

            // no spaces were found
            return false;

        } // skipSpaces():boolean

        /**
         * Skips the specified string appearing immediately on the input.
         * <p>
         * <strong>Note:</strong> The characters are consumed only if they are
         * space characters.
         *
         * @param s The string to skip.
         *
         * @return Returns true if the string was skipped.
         *
         * @throws IOException  Thrown if i/o error occurs.
         * @throws EOFException Thrown on end of file.
         */
        public boolean skipString(String s) throws IOException {

            // load more characters, if needed
            if (fCurrentEntity.position == fCurrentEntity.count) {
                load(0, true);
            }

            // skip string
            final int length = s.length();
            for (int i = 0; i < length; i++) {
                char c = fCurrentEntity.ch[fCurrentEntity.position++];
                if (c != s.charAt(i)) {
                    fCurrentEntity.position -= i + 1;
                    return false;
                }
                if (i < length - 1 && fCurrentEntity.position == fCurrentEntity.count) {
                    System.arraycopy(fCurrentEntity.ch, fCurrentEntity.count - i - 1, fCurrentEntity.ch, 0, i + 1);
                    // REVISIT: Can a string to be skipped cross an
                    //          entity boundary? -Ac
                    if (load(i + 1, false)) {
                        fCurrentEntity.position -= i + 1;
                        return false;
                    }
                }
            }
            fCurrentEntity.columnNumber += length;
            return true;

        } // skipString(String):boolean

    } // class XML11EntityScanner

} // class XMLEntityManager
