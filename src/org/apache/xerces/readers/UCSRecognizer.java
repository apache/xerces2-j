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

import java.io.IOException;

/**
 *
 * @version
 */
final class UCSRecognizer extends XMLDeclRecognizer {
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
        if (b0 == 0) {
            int b1 = data.byteAt(1);
            if (b1 == 0) {
                if (data.byteAt(2) == 0 && data.byteAt(3) == '<')
                    reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS4B, stringPool);
            } else if (b1 == '<') {
                if (data.byteAt(2) == 0 && data.byteAt(3) == '?')
                    reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS2B_NOBOM, stringPool);
            }
        } else if (b0 == '<') {
            int b1 = data.byteAt(1);
            if (b1 == 0) {
                int b2 = data.byteAt(2);
                if (data.byteAt(3) == 0) {
                    if (b2 == 0)
                        reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS4L, stringPool);
                    else if (b2 == '?')
                        reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS2L_NOBOM, stringPool);
                }
            }
        } else if (b0 == (byte)0xfe) {
            if (data.byteAt(1) == (byte)0xff)
                reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS2B, stringPool);
        } else if (b0 == (byte)0xff) {
            if (data.byteAt(1) == (byte)0xfe)
                reader = new UCSReader(entityHandler, errorReporter, sendCharDataAsCharArray, data, UCSReader.E_UCS2L, stringPool);
        }
        return reader;
    }
}
