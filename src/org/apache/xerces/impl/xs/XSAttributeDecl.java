/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.xs;

import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.ValidatedInfo;

/**
 * The XML representation for an attribute declaration
 * schema component is an <attribute> element information item
 *
 * @author Elena Litani, IBM
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSAttributeDecl {

    // types of value constraint
    public final static short     NO_CONSTRAINT       = 0;
    public final static short     DEFAULT_VALUE       = 1;
    public final static short     FIXED_VALUE         = 2;

    // scopes
    public final static short     SCOPE_ABSENT        = 0;
    public final static short     SCOPE_GLOBAL        = 1;
    public final static short     SCOPE_LOCAL         = 2;

    // the name of the attribute
    public String fName = null;
    // the target namespace of the attribute
    public String fTargetNamespace = null;
    // the simple type of the attribute
    public XSSimpleType fType = null;
    // value constraint type: default, fixed or !specified
    short fMiscFlags = 0;
    // scope
    short fScope = SCOPE_ABSENT;
    // enclosing complex type, when the scope is local
    XSComplexTypeDecl fEnclosingCT = null;
    // value constraint value
    public ValidatedInfo fDefault = null;

    // methods to get/set misc flag

    public short getConstraintType() {
        return  fMiscFlags;
    }
    public boolean isGlobal() {
        return fScope == SCOPE_GLOBAL;
    }

    public void setConstraintType(short constraintType) {
        fMiscFlags = constraintType;
    }
    public void setIsGlobal() {
        fScope = SCOPE_GLOBAL;
    }
    public void setIsLocal(XSComplexTypeDecl enclosingCT) {
        fScope = SCOPE_LOCAL;
        fEnclosingCT = enclosingCT;
    }

    public void reset(){
        fName = null;
        fTargetNamespace = null;
        fType = null;
        fMiscFlags = 0;
        fDefault = null;
    }

} // class XSAttributeDecl
