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
import org.apache.xerces.utils.StringPool;
import org.xml.sax.InputSource;
import java.io.InputStream;
import java.io.Reader;

/**
 * This is the factory interface used to create readers.
 *
 * @version
 */
public interface XMLEntityReaderFactory {
    /**
     * Adds a recognizer.
     *
     * @param recognizer The XML recognizer to add.
     */
    public void addRecognizer(XMLDeclRecognizer recognizer);

    /**
     * Set char data processing preference.
     */
    public void setSendCharDataAsCharArray(boolean flag);

    /**
     *
     */
    public void setAllowJavaEncodingName(boolean flag);

    /**
     *
     */
    public boolean getAllowJavaEncodingName();

    /**
     * Create an entity reader for the source.
     *
     * @param source The input source.
     * @param systemId The system identifier for the input.
     * @param xmlDecl <code>true</code> if an XMLDecl may be present; otherwise
     *                <code>false</code> if a TextDecl may be present.
     * @param stringPool The string pool.
     * @return The reader that will process the source.
     * @exception java.lang.Exception
     */
    public XMLEntityHandler.EntityReader createReader(XMLEntityHandler entityHandler,
                                                      XMLErrorReporter errorReporter,
                                                      InputSource source,
                                                      String systemId,
                                                      boolean xmlDecl,
                                                      StringPool stringPool) throws Exception;

    /**
     * Create an entity reader for a character stream.
     *
     * @param enityHandler The entity handler.
     * @param errorReporter The error reporter.
     * @param sendCharDataAsCharArray true if char data should be reported using
     *                                char arrays instead of string handles.
     * @param reader The character stream.
     * @param stringPool The string pool.
     * @return The reader that will process the character data.
     * @exception java.lang.Exception
     */
    public XMLEntityHandler.EntityReader createCharReader(XMLEntityHandler entityHandler,
                                                          XMLErrorReporter errorReporter,
                                                          boolean sendCharDataAsCharArray,
                                                          Reader reader,
                                                          StringPool stringPool) throws Exception;

    /**
     * Create an entity reader for a byte stream encoded in UTF-8.
     *
     * @param enityHandler The entity handler.
     * @param errorReporter The error reporter.
     * @param sendCharDataAsCharArray true if char data should be reported using
     *                                char arrays instead of string handles.
     * @param data The byte stream.
     * @param stringPool The string pool.
     * @return The reader that will process the UTF-8 data.
     * @exception java.lang.Exception
     */
    public XMLEntityHandler.EntityReader createUTF8Reader(XMLEntityHandler entityHandler,
                                                          XMLErrorReporter errorReporter,
                                                          boolean sendCharDataAsCharArray,
                                                          InputStream data,
                                                          StringPool stringPool) throws Exception;

    /**
     * Create an entity reader for data from a String.
     *
     * @param entityHandler The current entity handler.
     * @param errorReporter The current error reporter.
     * @param sendCharDataAsCharArray true if char data should be reported using
     *                                char arrays instead of string handles.
     * @param lineNumber The line number to return as our position.
     * @param columnNumber The column number to return as our position.
     * @param stringHandle The StringPool handle for the data to process.
     * @param stringPool The string pool.
     * @param addEnclosingSpaces If true, treat the data to process as if
     *                           there were a leading and trailing space
     *                           character enclosing the string data.
     * @return The reader that will process the string data.
     * @exception java.lang.Exception
     */
    public XMLEntityHandler.EntityReader createStringReader(XMLEntityHandler entityHandler,
                                                            XMLErrorReporter errorReporter,
                                                            boolean sendCharDataAsCharArray,
                                                            int lineNumber,
                                                            int columnNumber,
                                                            int stringHandle,
                                                            StringPool stringPool,
                                                            boolean addEnclosingSpaces) throws Exception;
}
