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

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 *
 * @version
 */
final class EBCDICRecognizer extends XMLDeclRecognizer {
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
                                                   boolean allowJavaEncodingName) throws Exception
    {
        XMLEntityHandler.EntityReader reader = null;
        byte b0 = data.byteAt(0);
        byte b1 = data.byteAt(1);
        byte b2 = data.byteAt(2);
        byte b3 = data.byteAt(3);
        boolean debug = false;

        if (b0 != 0x4c || b1 != 0x6f || b2 != (byte)0xa7 || b3 != (byte)0x94)
            return reader;
        XMLEntityHandler.EntityReader declReader = readerFactory.createCharReader(entityHandler, errorReporter, sendCharDataAsCharArray, new InputStreamReader(data, "CP037"), stringPool);
        int encoding = prescanXMLDeclOrTextDecl(declReader, xmlDecl);
        if (encoding == -1) {
            data.rewind();
            // REVISIT - The document is not well-formed.  There is no encoding, yet the file is
            //   clearly not UTF8.
            throw new UnsupportedEncodingException(null);
        }
        String enc = stringPool.orphanString(encoding).toUpperCase();
        if ("ISO-10646-UCS-2".equals(enc)) throw new UnsupportedEncodingException(enc);
        if ("ISO-10646-UCS-4".equals(enc)) throw new UnsupportedEncodingException(enc);
        if ("UTF-16".equals(enc)) throw new UnsupportedEncodingException(enc);
        String javaencname = MIME2Java.convert(enc);
        if (null == javaencname) {
            if (allowJavaEncodingName) {
                javaencname = enc;
            } else {
                throw new UnsupportedEncodingException(enc);
            }
        }
        try {
            data.rewind();
            reader = readerFactory.createCharReader(entityHandler, errorReporter, sendCharDataAsCharArray, new InputStreamReader(data, javaencname), stringPool);
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (Exception e) {
            if( debug == true )
                e.printStackTrace();            // Internal Error
        }
        return reader;
    }
}
