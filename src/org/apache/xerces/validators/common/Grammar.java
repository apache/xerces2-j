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

import org.apache.xerces.framework.XMLContentSpec;
import org.apache.xerces.utils.QName;
import org.apache.xerces.validators.datatype.DatatypeValidator;
import org.apache.xerces.validators.common.XMLContentModel;
import org.w3c.dom.Document;

/**
 * @version $Id$
 */
public class Grammar {

    //
    // Constants
    //

    private static final int CHUNK_SHIFT = 8; // 2^8 = 256
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT)); // 2^10 = 1k

    //
    // Data
    //

    // basic information

    private int fTargetNamespace;

    private Document fGrammarDocument;

    // element decl tables

    private int fElementDeclCount;
    private int fElementDeclNameIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclType[][] = new int[INITIAL_CHUNK_COUNT][];
    private DatatypeValidator fElementDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];
    private int fElementDeclContentSpecIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private XMLContentModel fElementDeclContentModelValidator[][] = new XMLContentModel[INITIAL_CHUNK_COUNT][];
    private int fElementDeclFirstAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclLastAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];

    // content spec tables

    private int fContentSpecCount;
    private int fContentSpecType[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fContentSpecValue[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fContentSpecOtherValue[][] = new int[INITIAL_CHUNK_COUNT][];

    // attribute decl tables

    private int fAttributeDeclCount;
    private QName fAttributeDeclName[][] = new QName[INITIAL_CHUNK_COUNT][];
    private int   fAttributeDeclType[][] = new int[INITIAL_CHUNK_COUNT][];
    private DatatypeValidator fAttributeDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];
    private String fAttributeDeclDefaultValue[][] = new String[INITIAL_CHUNK_COUNT][];
    private int fAttributeDeclNextAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];

    // scope mapping tables

    // TODO

    //
    // Public methods
    //

    public Document getGrammarDocument() {
        return fGrammarDocument;
    }

    public int getElementDeclIndex(int nameIndex, int scopeIndex) {//TODO
        

        return -1;
    }

    public boolean getElementDecl(int elementDeclIndex, XMLElementDecl elementDecl) {
        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {

           }

        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        elementDecl.name.localpart        = fElementDeclNameIndex[chunk][index];               
        elementDecl.type                  = fElementDeclType[chunk][index];                    
        elementDecl.datatypeValidator     = fElementDeclDatatypeValidator[chunk][index];       
        elementDecl.contentSpecIndex      = fElementDeclContentSpecIndex[chunk][index];        
        elementDecl.contentModelValidator = fElementDeclContentModelValidator[chunk][index];   


        //elementDecl. fElementDeclFirstAttributeDeclIndex[chunk][index];

        //elementDecl.name.uri              =
        //elementDecl.name                  = 
        //elementDecl.type                  =
        //elementDecl.datatypeValidator     =
        //elementDecl.contentSpecIndex      =
        //elementDecl.contentModelValidator =
        

        return false;
    }

    public int getFirstAttributeDeclIndex(int elementDeclIndex) {
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;
        
        return  fElementDeclFirstAttributeDeclIndex[chunk][index];
    }

    public int getNextAttributeDeclIndex(int attributeDeclIndex) {
        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex &  CHUNK_MASK;

        return fAttributeDeclNextAttributeDeclIndex[chunk][index];
    }

    public boolean getContentSpec(int contentSpecIndex, XMLContentSpec contentSpec) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

       
        contentSpec.Node.type  = fContentSpecType[chunk][index];
        contentSpec.Node.value = fContentSpecValue[chunk][index];

        if (contentSpec.Node.type == XMLContentSpec.CONTENTSPECNODE_CHOICE || 
                contentSpec.Node.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
           if (++contentSpecIndex == CHUNK_SIZE) {
               chunk++;
               contentSpecIndex = 0;
           }
           contentSpec.Node.otherValue = contentSpecOtherValue[chunk][contentSpecIndex];
       } 
       else {
           contentSpec.Node.otherValue = -1;
       }
       return false;
    }


    public boolean getElementContentModel(int elementDeclIndex,
                                                           XMLContentModel contentModel ) {
      if (elementDeclIndex < 0 || elementDeclIndex >= fElementCount)
          return false;
      int chunk = elementIndex >> CHUNK_SHIFT;
      int index = elementIndex & CHUNK_MASK;
      contentModel =  fElementDeclContentModelValidator[chunk][index];
      return true;
  }



    public boolean getAttributeDecl(int attributeDeclIndex, XMLAttributeDecl attributeDecl) {
        return false;
    }

    //
    // Protected methods
    //

    protected void setGrammarDocument(Document grammarDocument) {
        fGrammarDocument = grammarDocument;
    }

    protected int createElementDecl() {

        int chunk = fElementDeclCount >> CHUNK_SHIFT;
        int index = fElementDeclCount & CHUNK_MASK;
        if( ensureElementDeclCapacity(chunk) == true ){ // create an ElementDecl

            fElementDeclNameIndex[chunk][index]         = 
            fElementDeclType[chunk][index]             =     
            fElementDeclDatatypeValidator[chunk][index] =
            fContentSpecType[chunk][index] = contentSpecType;
            fContentSpec[chunk][index] = contentSpec;
            fContentModel[chunk][index] = null;
            fElementDeclFirstAttributeDeclIndex[chunk][index] = -1;
            fElementDeclLastAttributeDeclIndex[chunk][index] = -1;
            fStringPool.setDeclaration(elementType, fElementDeclCount);


        }

        return fElementDeclCount++;
    }

    protected void setElementDecl(int elementDeclIndex, XMLElementDecl elementDecl) {

        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {
            
        }
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        fElementDeclNameIndex[chunk][index]               = elementDecl.name.localpart;
        fElementDeclType[chunk][index]                    = elementDecl.type;
        fElementDeclDatatypeValidator[chunk][index]       = elementDecl.datatypeValidator;
        fElementDeclContentSpecIndex[chunk][index]        = elementDecl.contentSpecIndex;
        fElementDeclContentModelValidator[chunk][index]   = elementDecl.contentModelValidator;
        //fElementDeclFirstAttributeDeclIndex[chunk][index] = 
        //fElementDeclLastAttributeDeclIndex[chunk][index]  = 
    }

    public void addAttributeDecl(int attributeDeclIndex, int elementDeclIndex) {
    }


    protected int createContentSpec() {



        return fContentSpecCount++;
    }

    protected void setContentSpec(int contentSpecIndex, XMLContentSpec contentSpec) {

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

        fContentSpecType[chunk][index]  = contentSpec.Node.type;
        fContentSpecValue[chunk][index] = contentSpec.Node.value;
        fContentSpecValue[chunk][index] = contentSpec.Node.otherValue;
    }

    protected int createAttributeDecl() {
        return fAttributeDeclCount++;
    }

    protected void setContentSpec(int attributeDeclIndex, XMLAttributeDecl attributeDecl) {
    }

    //
    // Private methods
    //

    // ensure capacity

    private boolean ensureElementDeclCapacity(int chunk) {
        try {
            return fElementDeclNameIndex[chunk][0] == 0;
        } 
        catch (ArrayIndexOutOfBoundsException ex) {
            fElementDeclNameIndex = resize(fElementDeclNameIndex, fElementDeclNameIndex.length * 2);
        }
        catch (NullPointerException ex) {
            // ignore
        }
        fElementDeclNameIndex[chunk] = new int[CHUNK_SIZE];
        return true;
    }

    private boolean ensureContentSpecCapacity(int chunk) {
        return true;
    }

    private boolean ensureAttributeDeclCapacity(int chunk) {
        return true;
    }

    // resize initial chunk

    private int[][] resize(int array[][], int newsize) {
        int newarray[][] = new int[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private DatatypeValidator[][] resize(DatatypeValidator array[][], int newsize) {
        // TODO
        return array;
    }

    private XMLContentModel[][] resize(XMLContentModel array[][], int newsize) {
        // TODO
        return array;
    }

    private QName[][] resize(QName array[][], int newsize) {
        // TODO
        return array;
    }

    private String[][] resize(String array[][], int newsize) {
        // TODO
        return array;
    }

} // class Grammar
