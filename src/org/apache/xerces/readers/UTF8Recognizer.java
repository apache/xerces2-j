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
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @version
 */
final class UTF8Recognizer extends XMLDeclRecognizer {
    //
    //
    //
    public XMLEntityHandler.EntityReader recognize(XMLEntityReaderFactory readerFactory,
                                                   XMLEntityHandler entityHandler,
                                                   XMLErrorReporter errorReporter,
                                                   boolean sendCharDataAsCharArray,
                                                   StringPool stringPool,
                                                   ChunkyByteArray data,
                                                   boolean xmlDecl,
                                                   boolean allowJavaEncodingName) throws Exception {
        XMLEntityHandler.EntityReader reader = null;
        byte b0 = data.byteAt(0);
        boolean debug = false;

        if (b0 == '<') {
            int b1 = data.byteAt(1);
            if (b1 == '?') {
                if (data.byteAt(2) == 'x' && data.byteAt(3) == 'm' && data.byteAt(4) == 'l') {
                    int b5 = data.byteAt(5);
                    if (b5 == 0x20 || b5 == 0x09 || b5 == 0x0a || b5 == 0x0d) {
                        XMLEntityHandler.EntityReader declReader = new XMLDeclReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, stringPool);
                        int encoding = prescanXMLDeclOrTextDecl(declReader, xmlDecl);
                        if (encoding != -1) {
                            String encname = stringPool.orphanString(encoding);
                            String enc = encname.toUpperCase();
                            if ("ISO-10646-UCS-2".equals(enc)) throw new UnsupportedEncodingException(encname);
                            if ("ISO-10646-UCS-4".equals(enc)) throw new UnsupportedEncodingException(encname);
                            if ("UTF-16".equals(enc)) throw new UnsupportedEncodingException(encname);

                            String javaencname = MIME2Java.convert(enc);
                            if (null == javaencname) {
                                // Not supported
                                if (allowJavaEncodingName) {
                                    javaencname = encname;
                                } else {
                                    throw new UnsupportedEncodingException(encname);
                                }
                            }
                            try {
                                data.rewind();
                                if ("UTF-8".equalsIgnoreCase(javaencname) || "UTF8".equalsIgnoreCase(javaencname)) {
                                    reader = readerFactory.createUTF8Reader(entityHandler, errorReporter, sendCharDataAsCharArray, data, stringPool);
                                } else {
                                    reader = readerFactory.createCharReader(entityHandler, errorReporter, sendCharDataAsCharArray,
                                                                            new InputStreamReader(data, javaencname), stringPool);
                                }
                            } catch (UnsupportedEncodingException e) {
                                throw new UnsupportedEncodingException(encname);
                            } catch (Exception e) {
                                if( debug == true )
                                   e.printStackTrace();            // Internal Error
                            }
                        } else {
                            data.rewind();
                            reader = readerFactory.createUTF8Reader(entityHandler, errorReporter, sendCharDataAsCharArray, data, stringPool);
                        }
                    }
                }
            }
        }
        return reader;
    }

    final class XMLDeclReader extends XMLEntityReader {
        //
        //
        //
        private StringPool fStringPool = null;
        private ChunkyByteArray fData = null;
        //
        //
        //
        XMLDeclReader(XMLEntityHandler entityHandler, XMLErrorReporter errorReporter, boolean sendCharDataAsCharArray, ChunkyByteArray data, StringPool stringPool) {
            super(entityHandler, errorReporter, sendCharDataAsCharArray);
            fStringPool = stringPool;
            fData = data;
        }
        //
        // These methods are used to parse XMLDecl/TextDecl.
        //
        public boolean lookingAtChar(char ch, boolean skipPastChar) throws IOException {
            if (fData.byteAt(fCurrentOffset) != ch)
                return false;
            if (skipPastChar)
                fCurrentOffset++;
            return true;
        }
        public boolean lookingAtSpace(boolean skipPastChar) throws IOException {
            int ch = fData.byteAt(fCurrentOffset) & 0xff;
            if (ch != 0x20 && ch != 0x09 && ch != 0x0A && ch != 0x0D)
                return false;
            if (skipPastChar)
                fCurrentOffset++;
            return true;
        }
        public void skipPastSpaces() throws IOException {
            while (true) {
                int ch = fData.byteAt(fCurrentOffset) & 0xff;
                if (ch != 0x20 && ch != 0x09 && ch != 0x0A && ch != 0x0D)
                    return;
                fCurrentOffset++;
            }
        }
        public boolean skippedString(char[] s) throws IOException {
            int offset = fCurrentOffset;
            for (int i = 0; i < s.length; i++) {
                if (fData.byteAt(offset) != s[i])
                    return false;
                offset++;
            }
            fCurrentOffset = offset;
            return true;
        }
        public int scanStringLiteral() throws Exception {
            boolean single;
            if (!(single = lookingAtChar('\'', true)) && !lookingAtChar('\"', true)) {
                return XMLEntityHandler.STRINGLIT_RESULT_QUOTE_REQUIRED;
            }
            int offset = fCurrentOffset;
            char qchar = single ? '\'' : '\"';
            while (true) {
                byte b = fData.byteAt(fCurrentOffset);
                if (b == qchar)
                    break;
                if (b == -1)
                    return XMLEntityHandler.STRINGLIT_RESULT_QUOTE_REQUIRED;
                fCurrentOffset++;
            }
            int length = fCurrentOffset - offset;
            StringBuffer str = new StringBuffer(length);
            for (int i = 0; i < length; i++) {
                str.append((char)fData.byteAt(offset + i));
            }
            int stringIndex = fStringPool.addString(str.toString());
            fCurrentOffset++; // move past qchar
            return stringIndex;
        }
        //
        // The rest of the methods in XMLReader are not used for parsing XMLDecl/TextDecl.
        //
        public void append(XMLEntityHandler.CharBuffer charBuffer, int offset, int length) {
            throw new RuntimeException("RDR002 cannot happen");
        }
        public int addString(int offset, int length) {
            throw new RuntimeException("RDR002 cannot happen");
        }
        public int addSymbol(int offset, int length) {
            throw new RuntimeException("RDR002 cannot happen");
        }
        public void skipToChar(char ch) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public void skipPastName(char fastcheck) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public void skipPastNmtoken(char fastcheck) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public boolean lookingAtValidChar(boolean skipPastChar) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanInvalidChar() throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanCharRef(boolean hex) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanAttValue(char qchar, boolean asSymbol) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanEntityValue(int qchar, boolean createString) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public boolean scanExpectedName(char fastcheck, StringPool.CharArrayRange expectedName) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public void scanQName(char fastcheck, QName qname) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanName(char fastcheck) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
        public int scanContent(QName element) throws IOException {
            throw new IOException("RDR002 cannot happen");
        }
    }
}
