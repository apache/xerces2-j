/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights 
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

package org.apache.xerces.validators.common;

import org.apache.xerces.utils.QName;
import org.apache.xerces.validators.datatype.DatatypeValidator;

/**
 * @version $Id$
 */
public class XMLElementDecl {

    //
    // Data
    //

    // basic information

    public QName name = new QName();

    public int type;

    // simple types

    public DatatypeValidator datatypeValidator;

    // complex types

    public int contentSpecIndex;

    public XMLContentModel contentModelValidator;

    // enclosingScope where this element is declared, should always be -1 with DTD Validation.
    public int enclosingScope;

    // Attribute List Head and Tail Index
    public int firstAttributeDeclIndex;
    public int lastAttributeDeclIndex;

    //
    // Constructors
    //

    public XMLElementDecl() {
        clear();
    }

    public XMLElementDecl(XMLElementDecl elementDecl) {
        setValues(elementDecl);
    }

    //
    // Public methods
    //

    public void clear() {
        name.clear();
        type = - 1;
        datatypeValidator = null;
        contentSpecIndex = -1;
        contentModelValidator = null;
	enclosingScope = -1;
        firstAttributeDeclIndex = -1;
        lastAttributeDeclIndex = -1;
    }

    public void setValues(XMLElementDecl elementDecl) {
        name.setValues(elementDecl.name);
        type = elementDecl.type;
        datatypeValidator = elementDecl.datatypeValidator;
        contentSpecIndex = elementDecl.contentSpecIndex;
        contentModelValidator = elementDecl.contentModelValidator;
	enclosingScope = elementDecl.enclosingScope;
	firstAttributeDeclIndex = elementDecl.firstAttributeDeclIndex;
	lastAttributeDeclIndex = elementDecl.lastAttributeDeclIndex;
    }

    //
    // Object methods
    //

    public int hashCode() {
        // TODO
        return super.hashCode();
    }

    public boolean equals(Object object) {
        // TODO
        return super.equals(object);
    }

} // class XMLElementDecl
