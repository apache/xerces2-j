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

package org.apache.xerces.readers;

import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.ChunkyByteArray;
import org.apache.xerces.utils.StringPool;
import java.util.Stack;

/**
 * Abstract base class for encoding recognizers.
 *
 * When we encounter an external entity, including the document entity,
 * and do not know what the encoding of the underlying byte stream is,
 * we need to look at the contents of the stream to find out.  We do this
 * by asking a set of "recognizers" to look at the stream data and if
 * the recognizer can understand the encoding it will try to read an
 * XML or text declaration, if present, and construct the appropriate
 * reader for that encoding.  The recognizer subclasses will typically
 * use the prescanXMLDeclOrTextDecl() method if the stream looks like
 * it does begin with such a declaration using a temporary reader that
 * can support the calls needed to scan through the encoding declaration.
 */
public abstract class XMLDeclRecognizer {

    /**
     * Register the standard recognizers.
     *
     * @param recognizerStack The stack of recognizers used by the parser.
     */
    public static void registerDefaultRecognizers(Stack recognizerStack) {
        recognizerStack.push(new EBCDICRecognizer());
        recognizerStack.push(new UCSRecognizer());
        recognizerStack.push(new UTF8Recognizer());
    }

    /**
     * Subclasses override this method to support recognizing their encodings.
     *
     * @param readerFactory the factory object to use when constructing the entity reader.
     * @param entityHandler the entity handler to get entity readers from
     * @param errorReporter where to report errors
     * @param sendCharDataAsCharArray true if the reader should use char arrays, not string handles.
     * @param stringPool the <code>StringPool</code> to put strings in
     * @param data initial bytes to perform recognition on
     * @param xmlDecl true if attempting to recognize fron an XMLDecl, false if trying to recognize from a TextDecl.
     * @param allowJavaEncodingName true if Java's encoding names are allowed, false if they are not.
     * @return The reader that will be used to process the contents of the data stream.
     * @exception java.lang.Exception
     */
    public abstract XMLEntityHandler.EntityReader recognize(XMLEntityReaderFactory readerFactory,
                                                            XMLEntityHandler entityHandler,
                                                            XMLErrorReporter errorReporter,
                                                            boolean sendCharDataAsCharArray,
                                                            StringPool stringPool,
                                                            ChunkyByteArray data,
                                                            boolean xmlDecl,
                                                            boolean allowJavaEncodingName) throws Exception;

    //
    // From the standard:
    //
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    // [81] EncName ::= [A-Za-z] ([A-Za-z0-9._] | '-')*
    // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
    //
    /**
     * Support for getting the value of an EncodingDecl using an XMLReader.
     *
     * This is the minimal logic from the scanner to recognize an XMLDecl or TextDecl using
     * the XMLReader interface.
     *
     * @param entityReader data source for prescan
     * @param xmlDecl true if attempting to recognize from an XMLDecl, false if trying to recognize from a TextDecl.
     * @return <code>StringPool</code> handle to the name of the encoding recognized
     * @exception java.lang.Exception
     */
    protected int prescanXMLDeclOrTextDecl(XMLEntityHandler.EntityReader entityReader, boolean xmlDecl) throws Exception
    {
        if (!entityReader.lookingAtChar('<', true)) {
            return -1;
        }
        if (!entityReader.lookingAtChar('?', true)) {
            return -1;
        }
        if (!entityReader.skippedString(xml_string)) {
            return -1;
        }
        entityReader.skipPastSpaces();
        boolean single;
        char qchar;
        if (entityReader.skippedString(version_string)) {
            entityReader.skipPastSpaces();
            if (!entityReader.lookingAtChar('=', true)) {
                return -1;
            }
            entityReader.skipPastSpaces();
            int versionIndex = entityReader.scanStringLiteral();
            if (versionIndex < 0) {
                return -1;
            }
            if (!entityReader.lookingAtSpace(true)) {
                return -1;
            }
            entityReader.skipPastSpaces();
        }
        else if (xmlDecl) {
            return -1;
        }
        if (!entityReader.skippedString(encoding_string)) {
            return -1;
        }
        entityReader.skipPastSpaces();
        if (!entityReader.lookingAtChar('=', true)) {
            return -1;
        }
        entityReader.skipPastSpaces();
        int encodingIndex = entityReader.scanStringLiteral();
        return encodingIndex;
    }
    //
    // [23] XMLDecl ::= '<?xml' VersionInfo EncodingDecl? SDDecl? S? '?>'
    // [77] TextDecl ::= '<?xml' VersionInfo? EncodingDecl S? '?>'
    //
    private static final char[] xml_string = { 'x','m','l' };
    //
    // [24] VersionInfo ::= S 'version' Eq (' VersionNum ' | " VersionNum ")
    //
    private static final char[] version_string = { 'v','e','r','s','i','o','n' };
    //
    // [80] EncodingDecl ::= S 'encoding' Eq ('"' EncName '"' |  "'" EncName "'" )
    //
    private static final char[] encoding_string = { 'e','n','c','o','d','i','n','g' };
}
