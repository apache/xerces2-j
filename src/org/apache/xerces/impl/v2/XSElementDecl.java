/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.v2;

import java.util.Vector;
import org.apache.xerces.xni.QName;

/**
 * The XML representation for an element declaration schema component is
 * an <element> element information item.
 * 
 * @author Elena Litani, IBM
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public class XSElementDecl {

    // REVISIT: should the following information stored in the form of
    // miscFlag, or separate boolean values?
    public static final short NILLABLE = 1;
    public static final short ABSTRACT = 2;
    public static final short FIXED    = 4;

    public String fName;
    public String fTargetNamespace;

    // index to the type registry: simpleType or complexType
    public String fTypeNS;
    public int fTypeIdx;

    // nillable/abstract/fixed
    public short fElementMiscFlags;

    public short fBlock;
    public short fFinal;
    // REVISIT: should be Object: compiled form
    public String fDefault;

    public String fSubGroupNS;
    public int fSubGroupIdx;


    // identity constraints

    public final Vector fUnique = new Vector();

    public final Vector fKey = new Vector();

    public final Vector fKeyRef = new Vector();


    //
    // Constructors
    //

    public XSElementDecl() {
        clear();
    }

    //REVISIT: we should never clone a particle, so we don't need this method.
    /*public XSElementDecl(XSElementDecl elementDecl) {
        setValues(elementDecl);
    }*/

    //
    // Public methods
    //

    public void clear() {
        fName = null;
        fTypeIdx = SchemaGrammar.I_EMPTY_DECL;
        fUnique.removeAllElements();
        fKey.removeAllElements();
        fKeyRef.removeAllElements();
    }

    //REVISIT: we should never clone a particle, so we don't need this method.
    /*public void setValues(XSElementDecl elementDecl) {
        fQName.setValues(elementDecl.fQName);
        fTypeNS = elementDecl.fTypeNS;
        fTypeIdx = elementDecl.fXSTypeDecl;
        fElementMiscFlags = elementDecl.fElementMiscFlags;
        fBlock = elementDecl.fBlock;
        fFinal = elementDecl.fFinal;
        fDefault = elementDecl.fDefault;
        fSubGroupNS = elementDecl.fSubGroupNS;
        fSubGroupIdx = elementDecl.fSubGroupIdx;
        copyIdentityConstraints(elementDecl.fUnique,elementDecl.fKey, elementDecl.fKeyRef);
    }*/

    public void copyIdentityConstraints (Vector unique, Vector key, Vector keyRef){
        //REVISIT: IMPLEMENT!
    }

} // class XMLElementDecl
