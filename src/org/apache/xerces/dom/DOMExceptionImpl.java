/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.dom;

import org.w3c.dom.DOMException;

/**
 * DOMExceptions are thrown when one of the
 * DOM implementation classes discovers an error condition. The
 * architecture is defined in terms of the ExceptionCode, but in the
 * standard Java API this has been extended so the exception may also
 * carry a text string. Standard exception codes are:
 * <UL>
 * <LI>
 * INDEX_SIZE_ERR: index or size is negative, or greater than the
 * allowed value.
 * <LI>
 * WSTRING_SIZE_ERR: the specified range of text does not fit into the
 * string representation used in this implementation. Only arises in
 * implementations whose strings have a maximum-size limit. The DOM
 * provides workarounds to support those languages, generally by
 * having the DOM extract a managable substring on their behalf.
 * <LI>
 * HIERARCHY_REQUEST_ERR: user tried to insert a node somewhere it
 * doesn't belong... such as adding a child to a Text node.
 * <LI>
 * WRONG_DOCUMENT_ERR: a node is used in a different document than the
 * one that created it (that doesn't support it). Currently, the DOM
 * doesn't promise that nodes can be moved from document to document
 * even if they're both using the same underlying implementation.
 * Level 2 of the DOM spec may address this.
 * <LI>
 * INVALID_NAME_ERR: an invalid name is specified.
 * <LI>
 * NO_DATA_ALLOWED_ERR: data is specified for a node which does not
 * support data.
 * <LI>
 * NO_MODIFICATION_ALLOWED_ERR: an attempt is made to modify an object
 * where modifications are not allowed.
 * <LI>
 * NOT_FOUND_ERR: an attempt was made to reference a node in a context
 * where it does not exist.
 * <LI>
 * NOT_SUPPORTED_ERR: the implementation does not support the type of
 * object requested.
 * <LI>
 * INUSE_ATTRIBUTE_ERR: an attempt is made to add an attribute object
 * that is already in use elsewhere.
 * </UL>
 *
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public class DOMExceptionImpl 
    extends DOMException {

    //
    // Constants
    //

    // DOM has named these but hasn't values yet. Stopgap:
    public static final short           UNSPECIFIED_EVENT_TYPE= 100;
    public static final short           UNSUPPORTED_EVENT_TYPE= 101;
    
    //
    // Constructors
    //

    /** Constructs a dom exception. */
    public DOMExceptionImpl(short code, String message) {
	    super(code, message);
    }    

} // class DOMExceptionImpl
