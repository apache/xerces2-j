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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.validation;

import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;

import java.util.Hashtable;
import java.util.Enumeration;


public class  ValidationState implements ValidationContext {

    // 
    // private data
    //
    private EntityState fEntityState            = null;
    private NamespaceSupport fNamespaceSupport  = null;
    private SymbolTable fSymbolTable            = null;

    private final Hashtable fIdTable    = new Hashtable();
    private final Hashtable fIdRefTable = new Hashtable();

    private final static Object fNullValue = new Object();

    //
    // public methods
    //
    public void setEntityState(EntityState state) {
        fEntityState = state;
    }

    public void setNamespaceSupport(NamespaceSupport namespace) {
        fNamespaceSupport = namespace;
    }

    public void setSymbolTable(SymbolTable sTable) {
        fSymbolTable = sTable;
    }

    public boolean checkIDRefID () {
        Enumeration en = fIdRefTable.keys();

        while (en.hasMoreElements()) {
            String key = (String)en.nextElement();
            if (fIdTable == null || !fIdTable.containsKey(key)) {
                  return false;
            }
        }
        return true;
    }

    public void reset () {
        fIdTable.clear();
        fIdRefTable.clear();
        fEntityState = null;
        fNamespaceSupport = null;
        fSymbolTable = null;
    }
    //
    // implementation of ValidationContext methods
    //

    // entity
    public boolean isEntityDeclared (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityDeclared(name);
        }
        return false;
    }
    public boolean isEntityUnparsed (String name) {
        if (fEntityState !=null) {
            return fEntityState.isEntityUnparsed(name);
        }
        return false;
    }

    // id
    public boolean isIdDeclared(String name) {
        return fIdTable.containsKey(name);
    }
    public void    addId(String name) {
        fIdTable.put(name, fNullValue);
    }

    // idref
    public void addIdRef(String name) {
        if (fIdRefTable.containsKey(name)) {
            return;
        }
        fIdRefTable.put(name, fNullValue);
    }
    // get symbols

    public String getSymbol (String symbol) {
        return fSymbolTable.addSymbol(symbol);
    }
    // qname, notation
    public String getURI(String prefix) {
        if (fNamespaceSupport !=null) {
            return fNamespaceSupport.getURI(prefix);
        }
        return null;
    }

}
