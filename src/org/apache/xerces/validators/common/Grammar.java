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

import  org.w3c.dom.Node;
import  org.w3c.dom.Document;
import  org.apache.xerces.validators.common.XMLValidator;
import  org.apache.xerces.validators.common.XMLContentModel;
import  org.apache.xerces.validators.schema.SchemaSymbols;
import  org.apache.xerces.utils.StringPool;
import  org.apache.xerces.validators.datatype.DatatypeValidator;
import  org.apache.xerces.utils.QName;


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
public  class Grammar {
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


    /**
     * This takes a Root Node.
     * It could also take a Document fragment Node ( feature not
     * implemented yet).
     * It then compiles the Grammar into an internal
     * compiler representation of the Grammar that
     * validators then can use to validate.
     * 
     * @param node
     */
    public void                compileGrammar( Node node ) {

    }


    public Document            getGrammarDocument() {
        return null;
    }



    /**
     * Sets compile representation Element type
     * SimpleType | ComplexType
     * 
     * @param elementType
     * @see org.apache.xerces.validators.schema.SchemaSymbols
     */
    protected void  setElementType( String elementType ) {
    }

    /**
     * Sets compiled representation elementValidator, a reference
     * to datatype validator
     * 
     * @param elementValidator
     * @see           org.apache.xerces.validators.datatype.DatatypeValidator
     */
    protected void  setElementValidator( DatatypeValidator elementValidator ) {
    }

    /**
     * Sets Element Content Model compiled representation
     * 
     * @param elementContentModel
     */
    protected void  setElementContentModel( XMLContentModel  elementContentModel ) {
    }

    /**
     * Sets internal representation of Element Attribute
     * 
     * @param elementAttribute
     */
    protected void  setElementAttribute( int elementAttribute ) {
    }

    /**
     * Sets compiled representation Attribute validator, a reference
     * to datatype validator
     * 
     * @param attributeValidator
     * @see org.apache.xerces.validators.datatype.DatatypeValidator
     */
    protected void  setAttributeValidator( DatatypeValidator attributeValidator ){
    }

    /**
     * Validator Interface method
     * Get Element type
     * 
     * @param localPart
     * @param scope
     * @return 
     *         Return type as a String.
     *         SimpleType | CompleType
     */
    public String getElementType( String localPart, int scope ) {
        return null;
    }

    /**
     * Validator Interface method
     * Get Element Datatype Validator
     * 
     * @param localPart
     * @param scope
     * @return 
     * @see org.apache.xerces.validators.datatype.DatatypeValidator
     */
    public DatatypeValidator getElementValidator( String localPart, int scope ){
        return null;
    }

    /**
     * Validator Interface method
     * Get Element ContentModel
     * 
     * @param localPart
     * @param scope
     * @return 
     * @see          org.apache.xerces.validators.common.XMLContentModel
     */
    public XMLContentModel    getElementContentModel( String localPart, int scope ) {
        return null;
    }                         

    /**
     * Validator Interface method
     * Get get Element Attribute
     * 
     * @param elementIndex
     * @return 
     */
    public  int               getElementAttribute(int elementIndex ) {
        return 0;
    }
    /**
     * Validator Interface method
     * Get Attribute Validator
     * 
     * @param localPart
     * @param scope
     * @return 
     */
    public  DatatypeValidator getAttributeValidator( String localPart, int scope ){
        return null;
    }

    /**
     * Validator Interface method
     * Get Element index
     * 
     * @param localPart
     * @param scope
     * @return 
     */
    public int getElementIndex( String localPart, int scope ) {
        return 0;
    }




    /**
     * This inner classes embodies the array representation
     * of the Grammar ( either Schema, DTD, etc) as seen
     * internally by the parser.
     * 
     * @author Jeffrey Rodriguez
     */
    public static class ArrayRepresentation {

        private final  int TOP_LEVEL_SCOPE = 0;
        private final int CHUNK_SHIFT = 8;           // 2^8 = 256
        private final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
        private final int CHUNK_MASK = CHUNK_SIZE - 1;
        private final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));

        public int                    fElementCount          = 0;    // Element list
        public int[][]                fElementType           = null; 

        private QName[][]             fElementQName          = null;
        private int[][]               fScope                 = null;

        public byte[][]               fElementDeclIsExternal = null;
        public DatatypeValidator[][]  fElementValidator      = null;

        public int[][]                fContentSpecType       = null;
        public int[][]                fContentSpec           = null;
        public XMLContentModel[][]    fContentModel          = null;

        public int[][]                fAttlistHead           = null;
        public int[][]                fAttlistTail           = null;

        public int                    fNodeCount             = 0; //ContentSpecNode list                      
        public byte[][]               fNodeType              = null; 
        public int[][]                fNodeValue             = null;

        public int                    fAttDefCount           = 0;     //AttDef list
        public int[][]                fAttPrefix             = null; 
        public int[][]                fAttName               = null;
        public int[][]                fAttType               = null; 
        public DatatypeValidator [][] fAttValidator          = null;

        public int[][]                fEnumeration           = null;
        public int[][]                fAttDefaultType        = null;
        public int[][]                fAttValue              = null;
        public byte[][]               fAttDefIsExternal      = null;
        public int[][]                fNextAttDef            = null ;


        /**
         * Ensures that there is enough storage for element information.
         * 
         * @param chunk
         * @return         boolean
         */
        private boolean ensureElementCapacity(int chunk) {
            try {
                return fElementType[chunk][0] == 0;
            } catch (ArrayIndexOutOfBoundsException ex) {
                byte[][] newByteArray = new byte[chunk * 2][];
                System.arraycopy(fElementDeclIsExternal, 0, newByteArray, 0, chunk);
                fElementDeclIsExternal = newByteArray;

                int[][] newIntArray = new int[chunk * 2][];
                System.arraycopy(fElementType, 0, newIntArray, 0, chunk);
                fElementType = newIntArray;

                QName[][] newQNameArray = new QName[chunk * 2][];
                System.arraycopy(fElementQName, 0, newQNameArray, 0, chunk);
                fElementQName = newQNameArray;

                //REVISIT: fScope             
                newIntArray = new int[chunk * 2][];
                System.arraycopy(fScope, 0, newIntArray, 0, chunk);
                fScope = newIntArray;

                newIntArray = new int[chunk * 2][];
                System.arraycopy(fContentSpecType, 0, newIntArray, 0, chunk);
                fContentSpecType = newIntArray;

                newIntArray = new int[chunk * 2][];
                System.arraycopy(fContentSpec, 0, newIntArray, 0, chunk);
                fContentSpec = newIntArray;

                XMLContentModel[][] newContentModel = new XMLContentModel[chunk * 2][];
                System.arraycopy(fContentModel, 0, newContentModel, 0, chunk);
                fContentModel = newContentModel;

                newIntArray = new int[chunk * 2][];
                System.arraycopy(fAttlistHead, 0, newIntArray, 0, chunk);
                fAttlistHead = newIntArray;

                newIntArray = new int[chunk * 2][];
                System.arraycopy(fAttlistTail, 0, newIntArray, 0, chunk);
                fAttlistTail = newIntArray;
            } catch (NullPointerException ex) {
                // ignore
            }

            fElementType[chunk]  = new int[CHUNK_SIZE];
            fElementQName[chunk] = new QName[CHUNK_SIZE];
            fScope[chunk]        = new int[CHUNK_SIZE];
            //by default, all the scope should be top-level
            for (int i=0; i<CHUNK_SIZE; i++) {
                fScope[chunk][i] = TOP_LEVEL_SCOPE;
            }

            fElementDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
            fContentSpecType[chunk]       = new int[CHUNK_SIZE];
            fContentSpec[chunk]           = new int[CHUNK_SIZE];
            fContentModel[chunk]          = new XMLContentModel[CHUNK_SIZE];
            fAttlistHead[chunk]           = new int[CHUNK_SIZE];
            fAttlistTail[chunk]           = new int[CHUNK_SIZE];
            return true;

        } // ensureElementCapacity(int):boolean
    }
}
