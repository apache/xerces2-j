/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  
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

package org.apache.xerces.xni.parser;

import org.apache.xerces.xni.XNIException;

/**
 * A parsing exception. This exception is different from the standard
 * XNI exception in that it stores the location in the document (or
 * its entities) where the exception occurred.
 * 
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class XMLParseException
    extends XNIException {

    //
    // Data
    //

    /** Public identifier. */
    protected String fPublicId;

    /** System identifier. */
    protected String fSystemId;

    /** Base system identifier. */
    protected String fBaseSystemId;

    /** Line number. */
    protected int fLineNumber;
    
    /** Column number. */
    protected int fColumnNumber;

    //
    // Constructors
    //

    /** Constructs a parse exception. */
    public XMLParseException(String publicId, String systemId,
                             String baseSystemId,
                             int lineNumber, int columnNumber,
                             String message) {
        super(message);
        fPublicId = publicId;
        fSystemId = systemId;
        fBaseSystemId = baseSystemId;
        fLineNumber = lineNumber;
        fColumnNumber = columnNumber;
    } // <init>(String,String,String,int,int,String)

    /** Constructs a parse exception. */
    public XMLParseException(String publicId, String systemId,
                             String baseSystemId,
                             int lineNumber, int columnNumber,
                             String message, Exception exception) {
        super(message, exception);
        fPublicId = publicId;
        fSystemId = systemId;
        fBaseSystemId = baseSystemId;
        fLineNumber = lineNumber;
        fColumnNumber = columnNumber;
    } // <init>(String,String,String,int,int,String,Exception)

    //
    // Public methods
    //

    /** Returns the public identifier. */
    public String getPublicId() {
        return fPublicId;
    } // getPublicId():String

    /** Returns the system identifier. */
    public String getSystemId() {
        return fSystemId;
    } // getSystemId():String

    /** Returns the base system identifier. */
    public String getBaseSystemId() {
        return fBaseSystemId;
    } // getBaseSystemId():String

    /** Returns the line number. */
    public int getLineNumber() {
        return fLineNumber;
    } // getLineNumber():int

    /** Returns the row number. */
    public int getColumnNumber() {
        return fColumnNumber;
    } // getRowNumber():int

    //
    // Object methods
    //

    /** Returns a string representation of this object. */
    public String toString() {

        StringBuffer str = new StringBuffer();
        if (fPublicId != null) {
            str.append(fPublicId);
        }
        str.append(':');
        if (fPublicId != null) {
            str.append(fPublicId);
        }
        str.append(':');
        if (fSystemId != null) {
            str.append(fSystemId);
        }
        str.append(':');
        if (fBaseSystemId != null) {
            str.append(fBaseSystemId);
        }
        str.append(':');
        str.append(fLineNumber);
        str.append(':');
        str.append(fColumnNumber);
        str.append(':');
        String message = getMessage();
        if (message == null) {
            Exception exception = getException();
            if (exception != null) {
                message = exception.getMessage();
            }
        }
        if (message != null) {
            str.append(message);
        }
        return str.toString();

    } // toString():String

} // XMLParseException
