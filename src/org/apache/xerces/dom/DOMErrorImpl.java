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

package org.apache.xerces.dom;

import org.apache.xerces.xni.parser.XMLParseException;

import org.apache.xerces.dom3.DOMError;
import org.apache.xerces.dom3.DOMLocator;


/**
 * <code>DOMErrorImpl</code> is an implementation that describes an error.
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-Core-20010913'>Document Object Model (DOM) Level 3 Core Specification</a>.
 *
 * @author Gopal Sharma, SUN Microsystems Inc.
 */

//
// REVISIT: 
// implementation does not allow to reuse the DOMError obj
// to allow reuse we need to implement setter methods 
// we also create new DOMLocator for each error.  -- el
// 

public class DOMErrorImpl implements DOMError {

    //
    // Data
    //

    short fSeverity = -1;
    String fMessage = null;    
    DOMLocator fLocation = null;
    Exception fException = null;


    //
    // Constructor
    //

    public DOMErrorImpl (short severity, XMLParseException exception) {
        fSeverity = severity;
        fException = exception;
        fMessage = exception.getMessage();
        fLocation = createDOMLocator (exception);
    }

    /**
     * The severity of the error, either <code>SEVERITY_WARNING</code>, 
     * <code>SEVERITY_ERROR</code>, or <code>SEVERITY_FATAL_ERROR</code>.
     */

    public short getSeverity() {
        return fSeverity;
    }

    /**
     * An implementation specific string describing the error that occured.
     */

    public String getMessage() {
        return fMessage;
    }

    /**
     * The byte or character offset into the input source, if we're parsing a 
     * file or a byte stream then this will be the byte offset into that 
     * stream, but if a character media is parsed then the offset will be 
     * the character offset.exception is a reserved word, we need to rename 
     * it.
     */

    public Object getException() {
        return fException;
    } 

    /**
     * The location of the error.
     */

    public DOMLocator getLocation() {
        return fLocation;
    }

    // method to get the DOMLocator Object
    private DOMLocator createDOMLocator(XMLParseException exception) {
        return new DOMLocatorImpl(exception.getLineNumber(),
                                  exception.getColumnNumber(),
                                  exception.getSystemId()
                                 );
    } // createDOMLocator()

}// class DOMErrorImpl