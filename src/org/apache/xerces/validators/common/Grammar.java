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

package org.apache.xerces.validators.common;

import  org.apache.xerces.validators.common.XMLValidator;
import  org.apache.xerces.validators.schema.SchemaSymbols;
import  org.apache.xerces.utils.StringPool;

import  org.w3c.dom.Node;
import  org.w3c.dom.Document;
import  org.apache.xerces.validators.common.XMLContentModel;

/**
 * Embodies a grammar. Grammars live a GrammaPool table
 * keyed on namespaces.
 * A Grammar is a holder for a Grammar.
 * When a Grammar is needed we populate a
 * DOM representation ofstructure, and then
 * we traverse this structure while building an
 * internal array representation of Grammar.
 * This Grammar are called from the XMLValidator which
 * is the Validator of all kind.
 * 
 * @author Jeffrey Rodriguez
 */
public abstract class Grammar {
    private  String                fGrammarID       = null;
    private  Document              fGrammarDocument = null;
    private StringPool             fStringPool      = null;
    private  ArrayRepresentation   fGrammarArrayRepresentation = null;

    public Grammar( String grammarID ) {
        fGrammarID = grammarID;
    }
    /**
     * 
     * @return                   String with name of grammar.
     */
    public String whatGrammarAmI(){
        return fGrammarID;
    }
    /**
     * getter class to obtain internal grammar representation.
     * 
     * @return               Grammar.arrayRepresentation
     */
    public ArrayRepresentation getGrammarArrayRepresentation(){
        return fGrammarArrayRepresentation;
    }



    abstract public void                populateGrammar( Node node );
    abstract public Document            getGrammarDocument();


    /**
     * This inner classes embodies the array representation
     * of the Grammar ( either Schema, DTD, etc) as seen
     * internally by the parser.
     * 
     * @author Jeffrey Rodriguez
     * @see            XMLValidator- Validator of all kind
     */
    public class ArrayRepresentation {
        public int                    fElementCount = 0;    // Element list
        public int[][]                fElementType  = null; 
        public byte[][]               fElementDeclIsExternal = null;
        public int[][]                fContentSpecType = null;
        public int[][]                fContentSpec     = null;
        public XMLContentModel[][]    fContentModel    = null;
        public int[][]                fAttlistHead     = null;
        public int[][]                fAttlistTail     = null;

        public int                    fNodeCount       = 0;   //ContentSpecNode list                      
        public byte[][]               fNodeType        = null; 
        public int[][]                fNodeValue       = null;

        public int                    fAttDefCount     = 0;     //AttDef list
        public int[][]                fAttPrefix       = null; 
        public int[][]                fAttName         = null;
        public int[][]                fAttType         = null; 
        public XMLValidator.AttributeValidator[][] fAttValidator    = null;
        public int[][]                fEnumeration     = null;
        public int[][]                fAttDefaultType  = null;
        public int[][]                fAttValue        = null;
        public byte[][]               fAttDefIsExternal  = null;
        public int[][]                fNextAttDef        = null ;
    }
}
