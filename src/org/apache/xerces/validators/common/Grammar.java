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
import org.apache.xerces.utils.Hash2intTable;
import org.apache.xerces.utils.QName;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.validators.datatype.DatatypeValidator;
import org.apache.xerces.validators.common.XMLContentModel;
import org.apache.xerces.validators.common.CMException;
import org.apache.xerces.validators.schema.identity.IdentityConstraint;
import org.apache.xerces.utils.ImplementationMessages;
import org.w3c.dom.Document;
import java.util.Vector;
import org.apache.xerces.validators.schema.SubstitutionGroupComparator;


/**
 * @version $Id$
 */
public class Grammar
implements XMLContentSpec.Provider {

    //
    // Constants
    //

    /** Signifies top level scope (-1). */
    public static final int TOP_LEVEL_SCOPE = -1;

    private static final int CHUNK_SHIFT = 8; // 2^8 = 256
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT)); // 2^10 = 1k

    private static final int LIST_FLAG = 0x8000;
    private static final int LIST_MASK = ~LIST_FLAG;

    //
    // Data
    //

    // basic information

    private int fTargetNamespace;

    private Document fGrammarDocument;

    // element decl tables

    private int fElementDeclCount = 0;
    private QName fElementDeclName[][] = new QName[INITIAL_CHUNK_COUNT][];
    private int fElementDeclType[][] = new int[INITIAL_CHUNK_COUNT][];
    private DatatypeValidator fElementDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];
    private int fElementDeclContentSpecIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclFirstAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fElementDeclLastAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclUnique[][] = new Vector[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclKey[][] = new Vector[INITIAL_CHUNK_COUNT][];
    private Vector fElementDeclKeyRef[][] = new Vector[INITIAL_CHUNK_COUNT][];

    // content spec tables

    private int fContentSpecCount = 0 ;
    private int fContentSpecType[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fContentSpecValue[][] = new int[INITIAL_CHUNK_COUNT][];
    private int fContentSpecOtherValue[][] = new int[INITIAL_CHUNK_COUNT][];
    private XMLContentModel fContentSpecValidator[][] = new XMLContentModel[INITIAL_CHUNK_COUNT][];

    // attribute decl tables

    private int fAttributeDeclCount = 0 ;
    private QName fAttributeDeclName[][] = new QName[INITIAL_CHUNK_COUNT][];
    private int   fAttributeDeclType[][] = new int[INITIAL_CHUNK_COUNT][];
    private int   fAttributeDeclEnumeration[][] = new int[INITIAL_CHUNK_COUNT][];
    private int   fAttributeDeclDefaultType[][] = new int[INITIAL_CHUNK_COUNT][];
    private DatatypeValidator fAttributeDeclDatatypeValidator[][] = new DatatypeValidator[INITIAL_CHUNK_COUNT][];
    private String fAttributeDeclDefaultValue[][] = new String[INITIAL_CHUNK_COUNT][];
    private int fAttributeDeclNextAttributeDeclIndex[][] = new int[INITIAL_CHUNK_COUNT][];

    // scope mapping tables

    private Hash2intTable fElementNameAndScopeToElementDeclIndexMapping = new Hash2intTable();

    // temp vars

    private QName fQName1 = new QName();
    private QName fQName2 = new QName();

    //
    // Public methods
    //

    public Document getGrammarDocument() {
        return fGrammarDocument;
    }

    public int getElementDeclIndex(int localpartIndex, int scopeIndex) {
        if ( localpartIndex > -1 && scopeIndex >-2 ) {
            return fElementNameAndScopeToElementDeclIndexMapping.get(StringPool.EMPTY_STRING, localpartIndex, scopeIndex);
        }
        return -1;
    }

    public int getElementDeclIndex(int uriIndex, int localpartIndex, int scopeIndex) {
        if ( localpartIndex > -1 && scopeIndex >-2 ) {
            return fElementNameAndScopeToElementDeclIndexMapping.get(uriIndex, localpartIndex, scopeIndex);
        }
        return -1;
    }

    public int getElementDeclIndex(QName element, int scopeIndex) {
        if ( element.localpart > -1 && scopeIndex >-2 ) {
            return fElementNameAndScopeToElementDeclIndexMapping.get(element.uri, element.localpart, scopeIndex);
        }
        return -1;
    }

    public boolean getElementDecl(int elementDeclIndex, XMLElementDecl elementDecl) {
        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {
            return false;
        }

        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        elementDecl.name.setValues(fElementDeclName[chunk][index]);
        if (fElementDeclType[chunk][index] == -1) {
            elementDecl.type                    = -1;
            elementDecl.list = false;
        }
        else {
            elementDecl.type                    = fElementDeclType[chunk][index] & LIST_MASK;
            elementDecl.list = (fElementDeclType[chunk][index] & LIST_FLAG) != 0;
        }
        elementDecl.datatypeValidator       = fElementDeclDatatypeValidator[chunk][index];
        elementDecl.contentSpecIndex        = fElementDeclContentSpecIndex[chunk][index];

        // copy identity constraints
        elementDecl.unique.removeAllElements();
        int ucount = fElementDeclUnique[chunk][index] != null
                   ? fElementDeclUnique[chunk][index].size() : 0;
        for (int i = 0; i < ucount; i++) {
            elementDecl.unique.addElement(fElementDeclUnique[chunk][index].elementAt(i));
        }
        elementDecl.key.removeAllElements();
        int kcount = fElementDeclKey[chunk][index] != null
                   ? fElementDeclKey[chunk][index].size() : 0;
        for (int i = 0; i < kcount; i++) {
            elementDecl.key.addElement(fElementDeclKey[chunk][index].elementAt(i));
        }
        elementDecl.keyRef.removeAllElements();
        int krcount = fElementDeclKeyRef[chunk][index] != null
                    ? fElementDeclKeyRef[chunk][index].size() : 0;
        for (int i = 0; i < krcount; i++) {
            elementDecl.keyRef.addElement(fElementDeclKeyRef[chunk][index].elementAt(i));
        }

        return true;
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
        if (contentSpecIndex < 0 || contentSpecIndex >= fContentSpecCount )
            return false;

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

        contentSpec.type       = fContentSpecType[chunk][index];
        contentSpec.value      = fContentSpecValue[chunk][index];
        contentSpec.otherValue = fContentSpecOtherValue[chunk][index];
        return true;
    }

    protected void clearContentModel(int contentSpecIndex) {
        if (contentSpecIndex < 0 || contentSpecIndex >= fContentSpecCount)
            return;

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

        fContentSpecValidator[chunk][index] = null;
    }

    protected boolean existContentModel(int contentSpecIndex) {
        if (contentSpecIndex < 0 || contentSpecIndex >= fContentSpecCount)
            return false;

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

        return (fContentSpecValidator[chunk][index] != null);
    }

    public XMLContentModel getElementContentModel(int elementDeclIndex, SubstitutionGroupComparator comparator) throws Exception {

        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount)
            return null;

        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex & CHUNK_MASK;

        int contentType = fElementDeclType[chunk][index];

        return getContentModel(fElementDeclContentSpecIndex[chunk][index],
                               contentType, comparator);
    }

    public XMLContentModel getContentModel(int contentSpecIndex, int contentType, SubstitutionGroupComparator comparator) throws Exception {
        if (contentSpecIndex < 0 || contentSpecIndex >= fContentSpecCount)
            return null;

        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;

        XMLContentModel contentModel = fContentSpecValidator[chunk][index];

        // If we have one, just return that. Otherwise, gotta create one
        if (contentModel != null)
            return contentModel;

        if (contentType == XMLElementDecl.TYPE_SIMPLE) {
            return null;
        }

        // Get the type of content this element has
        int expendedIndex = convertContentSpecTree(contentSpecIndex);

        XMLContentSpec  contentSpec = new XMLContentSpec();
        getContentSpec( expendedIndex, contentSpec );

        // And create the content model according to the spec type

        if ( contentType == XMLElementDecl.TYPE_MIXED_SIMPLE ) {
              //
              //  just create a mixed content model object. This type of
              //  content model is optimized for simple mixed content validation.
              //

              Vector vQName = new Vector();
              ChildrenList children = new ChildrenList();
              contentSpecTree(expendedIndex, contentSpec, children);
              contentModel = new MixedContentModel(children.qname,
                                                   children.type,
                                                   0, children.length, false, isDTD());
        }
        else if (contentType == XMLElementDecl.TYPE_MIXED_COMPLEX) {
              //
              // For Schema, we need a more complex model.   Create a child model as
              // per the element-only case, unless there are actually no children in
              // the model.  If that's the case, we can use the Mixed Content Model for
              // DTDs.
              contentModel = createChildModel(expendedIndex, true);
        }


        else if (contentType == XMLElementDecl.TYPE_CHILDREN) {
            //  This method will create an optimal model for the complexity
            //  of the element's defined model. If it's simple, it will create
            //  a SimpleContentModel object. If it's a simple list, it will
            //  create a SimpleListContentModel object. If it's complex, it
            //  will create a DFAContentModel object.
            //
            contentModel = createChildModel(expendedIndex, false);
        } else {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        // Add the new model to the content model for this element

        fContentSpecValidator[chunk][index] = contentModel;

        //build it  ..... in XMLValidator
        if (contentModel != null)
            contentModel.setSubstitutionGroupComparator(comparator);

        return contentModel;
    }

    public boolean getAttributeDecl(int attributeDeclIndex, XMLAttributeDecl attributeDecl) {
        if (attributeDeclIndex < 0 || attributeDeclIndex >= fAttributeDeclCount) {
            return false;
        }
        int chunk = attributeDeclIndex >> CHUNK_SHIFT;
        int index = attributeDeclIndex & CHUNK_MASK;

        attributeDecl.name.setValues(fAttributeDeclName[chunk][index]);

        if (fAttributeDeclType[chunk][index] == -1) {

            attributeDecl.type = -1;
            attributeDecl.list = false;
        }
        else {
            attributeDecl.type = fAttributeDeclType[chunk][index] & LIST_MASK;
            attributeDecl.list = (fAttributeDeclType[chunk][index] & LIST_FLAG) != 0;
        }
        attributeDecl.datatypeValidator = fAttributeDeclDatatypeValidator[chunk][index];
        attributeDecl.enumeration = fAttributeDeclEnumeration[chunk][index];
        attributeDecl.defaultType = fAttributeDeclDefaultType[chunk][index];
        attributeDecl.defaultValue = fAttributeDeclDefaultValue[chunk][index];
        return true;
    }

    //
    // Protected methods
    //

    protected void setGrammarDocument(Document grammarDocument) {
        fGrammarDocument = grammarDocument;
    }

    // SchemaGrammar overrides this and does a conversion
    protected int convertContentSpecTree(int index) {
        return index;
    }

    protected int createElementDecl() {

        int chunk = fElementDeclCount >> CHUNK_SHIFT;
        int index = fElementDeclCount & CHUNK_MASK;
        ensureElementDeclCapacity(chunk);
        fElementDeclName[chunk][index]               = new QName();
        fElementDeclType[chunk][index]                    = -1;
        fElementDeclDatatypeValidator[chunk][index]       = null;
        fElementDeclContentSpecIndex[chunk][index] = -1;
        fElementDeclFirstAttributeDeclIndex[chunk][index] = -1;
        fElementDeclLastAttributeDeclIndex[chunk][index]  = -1;
        return fElementDeclCount++;
    }

    protected void setElementDecl(int elementDeclIndex, XMLElementDecl elementDecl) {

        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {
            return;
        }
        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        fElementDeclName[chunk][index].setValues(elementDecl.name);
        fElementDeclType[chunk][index]                    = elementDecl.type;
        if (elementDecl.list) {
            fElementDeclType[chunk][index] |= LIST_FLAG;
        }
        fElementDeclDatatypeValidator[chunk][index]       = elementDecl.datatypeValidator;
        fElementDeclContentSpecIndex[chunk][index]        = elementDecl.contentSpecIndex;

        // copy identity constraints
        int ucount = elementDecl.unique.size();
        if (ucount > 0) {
            if (fElementDeclUnique[chunk][index] == null) {
                fElementDeclUnique[chunk][index] = (Vector)elementDecl.unique.clone();
            }
            else {
                fElementDeclUnique[chunk][index].removeAllElements();
                for (int i = 0; i < ucount; i++) {
                    fElementDeclUnique[chunk][index].addElement(elementDecl.unique.elementAt(i));
                }
            }
        }
        int kcount = elementDecl.key.size();
        if (kcount > 0) {
            if (fElementDeclKey[chunk][index] == null) {
                fElementDeclKey[chunk][index] = (Vector)elementDecl.key.clone();
            }
            else {
                fElementDeclKey[chunk][index].removeAllElements();
                for (int i = 0; i < kcount; i++) {
                    fElementDeclKey[chunk][index].addElement(elementDecl.key.elementAt(i));
                }
            }
        }
        int krcount = elementDecl.keyRef.size();
        if (krcount > 0) {
            if (fElementDeclKeyRef[chunk][index] == null) {
                fElementDeclKeyRef[chunk][index] = (Vector)elementDecl.keyRef.clone();
            }
            else {
                fElementDeclKeyRef[chunk][index].removeAllElements();
                for (int i = 0; i < krcount; i++) {
                    fElementDeclKeyRef[chunk][index].addElement(elementDecl.keyRef.elementAt(i));
                }
            }
        }

        // add the mapping information to the
        putElementNameMapping(elementDecl.name,
                              elementDecl.enclosingScope,
                              elementDeclIndex);
    }

    protected void putElementNameMapping(QName name, int scope,
                                         int elementDeclIndex) {
        fElementNameAndScopeToElementDeclIndexMapping.put(name.uri,
                                                          name.localpart,
                                                          scope,
                                                          elementDeclIndex);
    }

    protected void setFirstAttributeDeclIndex(int elementDeclIndex, int newFirstAttrIndex){

        if (elementDeclIndex < 0 || elementDeclIndex >= fElementDeclCount) {
            return;
        }

        int chunk = elementDeclIndex >> CHUNK_SHIFT;
        int index = elementDeclIndex &  CHUNK_MASK;

        fElementDeclFirstAttributeDeclIndex[chunk][index] = newFirstAttrIndex;
    }


    protected int createContentSpec() {
        int chunk = fContentSpecCount >> CHUNK_SHIFT;
        int index = fContentSpecCount & CHUNK_MASK;

        ensureContentSpecCapacity(chunk);
        fContentSpecType[chunk][index]       = -1;
        fContentSpecValue[chunk][index]      = -1;
        fContentSpecOtherValue[chunk][index] = -1;
        fContentSpecValidator[chunk][index] = null;

        return fContentSpecCount++;
    }

    protected void setContentSpec(int contentSpecIndex, XMLContentSpec contentSpec) {
        int   chunk = contentSpecIndex >> CHUNK_SHIFT;
        int   index = contentSpecIndex & CHUNK_MASK;

        fContentSpecType[chunk][index]       = contentSpec.type;
        fContentSpecValue[chunk][index]      = contentSpec.value;
        fContentSpecOtherValue[chunk][index] = contentSpec.otherValue;
    }

    protected int createAttributeDecl() {
        int chunk = fAttributeDeclCount >> CHUNK_SHIFT;
        int index = fAttributeDeclCount & CHUNK_MASK;

        ensureAttributeDeclCapacity(chunk);
        fAttributeDeclName[chunk][index]                    = new QName();
        fAttributeDeclType[chunk][index]                    = -1;
        fAttributeDeclDatatypeValidator[chunk][index]       = null;
        fAttributeDeclEnumeration[chunk][index] = -1;
        fAttributeDeclDefaultType[chunk][index] = XMLAttributeDecl.DEFAULT_TYPE_IMPLIED;
        fAttributeDeclDefaultValue[chunk][index]            = null;
        fAttributeDeclNextAttributeDeclIndex[chunk][index]  = -1;
        return fAttributeDeclCount++;
    }


    protected void setAttributeDecl(int elementDeclIndex, int attributeDeclIndex, XMLAttributeDecl attributeDecl) {

        int attrChunk = attributeDeclIndex >> CHUNK_SHIFT;
        int attrIndex = attributeDeclIndex &  CHUNK_MASK;

        fAttributeDeclName[attrChunk][attrIndex].setValues(attributeDecl.name);

        fAttributeDeclType[attrChunk][attrIndex]  =  attributeDecl.type;
        if (attributeDecl.list) {
            fAttributeDeclType[attrChunk][attrIndex] |= LIST_FLAG;
        }
        fAttributeDeclEnumeration[attrChunk][attrIndex]  =  attributeDecl.enumeration;
        fAttributeDeclDefaultType[attrChunk][attrIndex]  =  attributeDecl.defaultType;
        fAttributeDeclDatatypeValidator[attrChunk][attrIndex] =  attributeDecl.datatypeValidator;
        fAttributeDeclDefaultValue[attrChunk][attrIndex]      =  attributeDecl.defaultValue;

        int elemChunk     = elementDeclIndex >> CHUNK_SHIFT;
        int elemIndex     = elementDeclIndex &  CHUNK_MASK;
        int index = fElementDeclFirstAttributeDeclIndex[elemChunk][elemIndex];
        while (index != -1) {
            if (index == attributeDeclIndex) {
                break;
            }
            attrChunk = index >> CHUNK_SHIFT;
            attrIndex = index & CHUNK_MASK;
            index = fAttributeDeclNextAttributeDeclIndex[attrChunk][attrIndex];
        }
        if (index == -1) {
            if (fElementDeclFirstAttributeDeclIndex[elemChunk][elemIndex] == -1) {
                fElementDeclFirstAttributeDeclIndex[elemChunk][elemIndex] = attributeDeclIndex;
            }
            else {
                index = fElementDeclLastAttributeDeclIndex[elemChunk][elemIndex];
                attrChunk = index >> CHUNK_SHIFT;
                attrIndex = index & CHUNK_MASK;
                fAttributeDeclNextAttributeDeclIndex[attrChunk][attrIndex] = attributeDeclIndex;
            }
            fElementDeclLastAttributeDeclIndex[elemChunk][elemIndex] = attributeDeclIndex;
        }

    }

    protected boolean isDTD() {
        return false;
    }

    // debugging

    public void printElements(org.apache.xerces.utils.StringPool pool) {
        int elementDeclIndex = 0;
        XMLElementDecl elementDecl = new XMLElementDecl();
        while (getElementDecl(elementDeclIndex++, elementDecl)) {
            System.out.println("element decl: "+elementDecl.name+
                               ", "+pool.toString(elementDecl.name.rawname)+
                               ", "+XMLContentSpec.toString(this, pool, elementDecl.contentSpecIndex));
        }
    }

    public void printAttributes(int elementDeclIndex) {
        int attributeDeclIndex = getFirstAttributeDeclIndex(elementDeclIndex);
        System.out.print(elementDeclIndex);
        System.out.print(" [");
        while (attributeDeclIndex != -1) {
            System.out.print(' ');
            System.out.print(attributeDeclIndex);
            printAttribute(attributeDeclIndex);
            attributeDeclIndex = getNextAttributeDeclIndex(attributeDeclIndex);
            if (attributeDeclIndex != -1) {
                System.out.print(",");
            }
        }
        System.out.println(" ]");
    }

    //
    // Private methods
    //

    // debugging

    private void printAttribute(int attributeDeclIndex) {
        XMLAttributeDecl attributeDecl = new XMLAttributeDecl();
        if (getAttributeDecl(attributeDeclIndex, attributeDecl)) {
            System.out.print(" { ");
            System.out.print(attributeDecl.name.localpart);
            System.out.print(" }");
        }
    }

    // content models

    //
    //  When the element has a 'CHILDREN' model, this method is called to
    //  create the content model object. It looks for some special case simple
    //  models and creates SimpleContentModel objects for those. For the rest
    //  it creates the standard DFA style model.
    //
    private final XMLContentModel createChildModel(int contentSpecIndex, boolean isMixed) throws CMException
    {
        //
        //  Get the content spec node for the element we are working on.
        //  This will tell us what kind of node it is, which tells us what
        //  kind of model we will try to create.
        //
        XMLContentSpec contentSpec = new XMLContentSpec();


        getContentSpec(contentSpecIndex, contentSpec);

        if ((contentSpec.type & 0x0f ) == XMLContentSpec.CONTENTSPECNODE_ANY ||
            (contentSpec.type & 0x0f ) == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER ||
            (contentSpec.type & 0x0f ) == XMLContentSpec.CONTENTSPECNODE_ANY_NS) {
            // let fall through to build a DFAContentModel
        }
        else if (isMixed) {
            if ((contentSpec.type & 0x0f)==XMLContentSpec.CONTENTSPECNODE_ALL) {
                // All the nodes under an ALL must be additional ALL nodes and
                // ELEMENTs (or ELEMENTs under ZERO_OR_ONE nodes.)
                // We collapse the ELEMENTs into a single vector.
                AllContentModel allContent = new AllContentModel(false, true);
                gatherAllLeaves(contentSpecIndex, contentSpec, allContent);

                return allContent;
            }
            else if ((contentSpec.type & 0x0f) ==
                     XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE) {
                int zeroOrOneChildIndex = contentSpec.value;
                getContentSpec(zeroOrOneChildIndex, contentSpec);

                // An ALL node can appear under a ZERO_OR_ONE node.
                if ((contentSpec.type & 0x0f) ==
                    XMLContentSpec.CONTENTSPECNODE_ALL) {
                    AllContentModel allContent = new AllContentModel(true,true);
                    gatherAllLeaves(zeroOrOneChildIndex, contentSpec,
                                    allContent);

                    return new AllContentModel(true);
                }
            }
            // otherwise, let fall through to build a DFAContentModel
        }

        else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            //
            //  Check that the left value is not -1, since any content model
            //  with PCDATA should be MIXED, so we should not have gotten here.
            //
            if (contentSpec.value == -1 && contentSpec.otherValue == -1)
                throw new CMException(ImplementationMessages.VAL_NPCD);

            //
            //  It's a single leaf, so it's an 'a' type of content model, i.e.
            //  just one instance of one element. That one is definitely a
            //  simple content model.
            //

            fQName1.setValues(-1, contentSpec.value, contentSpec.value, contentSpec.otherValue);
            return new SimpleContentModel(fQName1, null, contentSpec.type, isDTD());
        }
        else if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_CHOICE)
                   ||  (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_SEQ)) {
            //
            //  Lets see if both of the children are leafs. If so, then
            //  it has to be a simple content model
            //
            XMLContentSpec contentSpecLeft  = new XMLContentSpec();
            XMLContentSpec contentSpecRight = new XMLContentSpec();

            getContentSpec(contentSpec.value, contentSpecLeft);
            getContentSpec(contentSpec.otherValue, contentSpecRight);

            if ((contentSpecLeft.type == XMLContentSpec.CONTENTSPECNODE_LEAF)
                &&  (contentSpecRight.type == XMLContentSpec.CONTENTSPECNODE_LEAF)) {
                //
                //  It's a simple choice or sequence, so we can do a simple
                //  content model for it.
                //
                fQName1.setValues(-1, contentSpecLeft.value, contentSpecLeft.value, contentSpecLeft.otherValue);
                fQName2.setValues(-1, contentSpecRight.value, contentSpecRight.value, contentSpecRight.otherValue);
                return new SimpleContentModel(fQName1, fQName2, contentSpec.type, isDTD());
            }
        }
        else if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE)
                   ||  (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE)
                   ||  (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE)) {
            //
            //  It's a repetition, so see if its one child is a leaf. If so
            //  it's a repetition of a single element, so we can do a simple
            //  content model for that.
            //
            XMLContentSpec contentSpecLeft = new XMLContentSpec();
            getContentSpec(contentSpec.value, contentSpecLeft);

            if (contentSpecLeft.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
                //
                //  It is, so we can create a simple content model here that
                //  will check for this repetition. We pass -1 for the unused
                //  right node.
                //
                fQName1.setValues(-1, contentSpecLeft.value, contentSpecLeft.value, contentSpecLeft.otherValue);
                return new SimpleContentModel(fQName1, null, contentSpec.type, isDTD());
            }
            else if (contentSpecLeft.type==XMLContentSpec.CONTENTSPECNODE_ALL) {
                // An ALL can be optional.  We must use the special
                // AllContentModel for such an animal.  Indicate the
                // entire content is optional on the constructor.
                AllContentModel allContent = new AllContentModel(true);

                // All of the nodes under an ALL must be additional ALL nodes
                // and ELEMENTs (or ELEMENTs under ZERO_OR_ONE nodes.)
                // We collapse the ELEMENTs into a single vector.
                gatherAllLeaves(contentSpec.value, contentSpecLeft, allContent);

                return allContent;
            }
        }
        else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ALL) {
            // All of the nodes under an ALL must be additional ALL nodes and
            // ELEMENTs (or ELEMENTs under ZERO_OR_ONE nodes.)
            // We collapse the ELEMENTs into a single vector.
            AllContentModel allContent = new AllContentModel(false);
            gatherAllLeaves(contentSpecIndex, contentSpec, allContent);

            return allContent;
        }
        else {
            throw new CMException(ImplementationMessages.VAL_CST);
        }

        //
        //  It's not a simple content model, so here we have to create a DFA
        //  for this element. So we create a DFAContentModel object. He
        //  encapsulates all of the work to create the DFA.
        //

        //int leafCount = countLeaves(contentSpecIndex);
        fLeafCount = 0;
        CMNode cmn    = buildSyntaxTree(contentSpecIndex, contentSpec);

        // REVISIT: has to be fLeafCount because we convert x+ to x,x*, one more leaf
        return new DFAContentModel(  cmn, fLeafCount, isDTD(), isMixed);
    }

    private void printSyntaxTree(CMNode cmn){
        System.out.println("CMNode : " + cmn.type());

        if (cmn.type() == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            System.out.println( "     Leaf: " + ((CMLeaf)cmn).getElement());
            return;
        }
        if (cmn instanceof CMBinOp) {
            printSyntaxTree( ((CMBinOp)cmn).getLeft());
            printSyntaxTree( ((CMBinOp)cmn).getRight());
        }
        if (cmn instanceof CMUniOp) {
            printSyntaxTree( ((CMUniOp)cmn).getChild());
        }

    }


    private int countLeaves(int contentSpecIndex) {
        return countLeaves(contentSpecIndex, new XMLContentSpec());
    }

    private int countLeaves(int contentSpecIndex, XMLContentSpec contentSpec) {

        if (contentSpecIndex == -1) {
            return 0;
        }
        /****
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fContentSpecType[chunk][index];
        if (type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            return 1;
        }
        int value = fContentSpecValue[chunk][index];
        int otherValue = fContentSpecOtherValue[chunk][index];
        return countLeaves(value) + countLeaves(otherValue);
        /***/
        getContentSpec(contentSpecIndex, contentSpec);
        if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            return 1;
        }
        int value = contentSpec.value;
        int otherValue = contentSpec.otherValue;
        return countLeaves(value, contentSpec) + countLeaves(otherValue, contentSpec);
        /***/
    }

    private int fLeafCount = 0;
    private int fEpsilonIndex = -1;
    private final CMNode buildSyntaxTree(int startNode, XMLContentSpec contentSpec) throws CMException
    {
        // We will build a node at this level for the new tree
        CMNode nodeRet = null;
        getContentSpec(startNode, contentSpec);
        if ((contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY ||
            (contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER ||
            (contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY_NS) {
            nodeRet = new CMAny(contentSpec.type, contentSpec.otherValue, fLeafCount++);
        }
        //
        //  If this node is a leaf, then it's an easy one. We just add it
        //  to the tree.
        //
        else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
            //
            //  Create a new leaf node, and pass it the current leaf count,
            //  which is its DFA state position. Bump the leaf count after
            //  storing it. This makes the positions zero based since we
            //  store first and then increment.
            //
            fQName1.setValues(-1, contentSpec.value, contentSpec.value, contentSpec.otherValue);
            nodeRet = new CMLeaf(fQName1, fLeafCount++);
        }
        else {
            //
            //  It's not a leaf, so we have to recurse its left and maybe right
            //  nodes. Save both values before we recurse and trash the node.
            //
            final int leftNode = contentSpec.value;
            final int rightNode = contentSpec.otherValue;

            if ((contentSpec.type == XMLContentSpec.CONTENTSPECNODE_CHOICE)
                ||  (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_SEQ)) {
                //
                //  Recurse on both children, and return a binary op node
                //  with the two created sub nodes as its children. The node
                //  type is the same type as the source.
                //

                nodeRet = new CMBinOp( contentSpec.type, buildSyntaxTree(leftNode, contentSpec)
                                       , buildSyntaxTree(rightNode, contentSpec));

		/* MODIFIED (Jan, 2001) */

	    } else if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE
		       || contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE
		       || contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
		nodeRet = new CMUniOp(contentSpec.type, buildSyntaxTree(leftNode, contentSpec));
	    }

		/* MODIFIED (Jan, 2001) */

	    else {
		throw new CMException(ImplementationMessages.VAL_CST);
            }
        }
        // And return our new node for this level
        return nodeRet;
    }

    /**
     *  Recursively build an AllContentModel based on a content spec tree
     *  rooted at an ALL node.
     *
     *  @param contentSpecIndex
     *              A content spec tree containing only ALL, LEAF and
     *              ZERO_OR_ONE nodes.  Any ZERO_OR_ONE nodes must be parents
     *              of LEAF nodes.
     *  @param contentSpec
     *              A temporary XMLContentSpec object used to record info.
     *              about the current node in the tree.  Used as a scratch
     *              pad by each recursive invocation of this method.
     *  @param allContent
     *              The AllContentModel that's being built for this tree
     * @exception CMException
     */
    private final void gatherAllLeaves(int contentSpecIndex,
                                       XMLContentSpec contentSpec,
                                       AllContentModel allContent)
                         throws CMException {

      if (contentSpecIndex <= -1) return;

      getContentSpec(contentSpecIndex, contentSpec);

      int value = contentSpec.value;
      int otherValue = contentSpec.otherValue;
      int type = contentSpec.type;

      if (type == XMLContentSpec.CONTENTSPECNODE_ALL) {
          // At an all node, visit left and right subtrees
          gatherAllLeaves(value, contentSpec, allContent);
          gatherAllLeaves(otherValue, contentSpec, allContent);
      }
      else if (type == XMLContentSpec.CONTENTSPECNODE_LEAF) {
          // At leaf, add the element to list of elements permitted in the all
          allContent.addElement(new QName(-1, value, value, otherValue), false);
      }
      else if (type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE) {
          // At ZERO_OR_ONE node, subtree must be an element
          // that was specified with minOccurs=0, maxOccurs=1
          getContentSpec(value, contentSpec);

          value = contentSpec.value;
          otherValue = contentSpec.otherValue;
          type = contentSpec.type;

          if (type != XMLContentSpec.CONTENTSPECNODE_LEAF)
              throw new CMException(ImplementationMessages.VAL_CST);

          // Add the optional element to list of elements permitted in the all
          allContent.addElement(new QName(-1, value, value, otherValue), true);
      }
      else {
          throw new CMException(ImplementationMessages.VAL_CST);
      }
    }

    /**
     * Build a vector of valid QNames from Content Spec
     * table.
     *
     * @param contentSpecIndex
     *               Content Spec index
     * @param vectorQName
     *               Array of QName
     * @exception CMException
     */
    private void contentSpecTree(int contentSpecIndex,
                                XMLContentSpec contentSpec,
                                ChildrenList children) throws CMException {

        // Handle any and leaf nodes
        getContentSpec( contentSpecIndex, contentSpec);
        if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_LEAF ||
            (contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY ||
            (contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY_NS ||
            (contentSpec.type & 0x0f) == XMLContentSpec.CONTENTSPECNODE_ANY_OTHER) {

            // resize arrays, if needed
            if (children.length == children.qname.length) {
                QName[] newQName = new QName[children.length * 2];
                System.arraycopy(children.qname, 0, newQName, 0, children.length);
                children.qname = newQName;
                int[] newType = new int[children.length * 2];
                System.arraycopy(children.type, 0, newType, 0, children.length);
                children.type = newType;
            }

            // save values and return length
            children.qname[children.length] = new QName(-1, contentSpec.value, contentSpec.value, contentSpec.otherValue);
            children.type[children.length] = contentSpec.type;
            children.length++;
            return;
        }

        //
        //  It's not a leaf, so we have to recurse its left and maybe right
        //  nodes. Save both values before we recurse and trash the node.
        //
        final int leftNode  = contentSpec.value;
        final int rightNode = contentSpec.otherValue;

        if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_CHOICE ||
            contentSpec.type == XMLContentSpec.CONTENTSPECNODE_SEQ) {
            contentSpecTree(leftNode, contentSpec, children);
            contentSpecTree(rightNode, contentSpec, children);
            return;
        }

        if (contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_ONE ||
            contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ZERO_OR_MORE ||
            contentSpec.type == XMLContentSpec.CONTENTSPECNODE_ONE_OR_MORE) {
            contentSpecTree(leftNode, contentSpec, children);
            return;
        }

        // error
        throw new CMException(ImplementationMessages.VAL_CST);
    }



    // ensure capacity

    private void ensureElementDeclCapacity(int chunk) {
        if (chunk >= fElementDeclName.length) {
            fElementDeclName = resize(fElementDeclName, fElementDeclName.length * 2);
            fElementDeclType = resize(fElementDeclType, fElementDeclType.length * 2);
            fElementDeclDatatypeValidator = resize(fElementDeclDatatypeValidator, fElementDeclDatatypeValidator.length * 2);
            fElementDeclContentSpecIndex = resize(fElementDeclContentSpecIndex, fElementDeclContentSpecIndex.length * 2);
            fElementDeclFirstAttributeDeclIndex = resize(fElementDeclFirstAttributeDeclIndex, fElementDeclFirstAttributeDeclIndex.length * 2);
            fElementDeclLastAttributeDeclIndex = resize(fElementDeclLastAttributeDeclIndex, fElementDeclLastAttributeDeclIndex.length * 2);
            fElementDeclUnique = resize(fElementDeclUnique, fElementDeclUnique.length * 2);
            fElementDeclKey = resize(fElementDeclKey, fElementDeclKey.length * 2);
            fElementDeclKeyRef = resize(fElementDeclKeyRef, fElementDeclKeyRef.length * 2);
        } else if (fElementDeclName[chunk] != null) {
            return;
        }
        fElementDeclName[chunk] = new QName[CHUNK_SIZE];
        fElementDeclType[chunk] = new int[CHUNK_SIZE];
        fElementDeclDatatypeValidator[chunk] = new DatatypeValidator[CHUNK_SIZE];
        fElementDeclContentSpecIndex[chunk] = new int[CHUNK_SIZE];
        fElementDeclFirstAttributeDeclIndex[chunk] = new int[CHUNK_SIZE];
        fElementDeclLastAttributeDeclIndex[chunk] = new int[CHUNK_SIZE];
        fElementDeclUnique[chunk] = new Vector[CHUNK_SIZE];
        fElementDeclKey[chunk] = new Vector[CHUNK_SIZE];
        fElementDeclKeyRef[chunk] = new Vector[CHUNK_SIZE];
    }

    private void ensureContentSpecCapacity(int chunk) {
        if (chunk >= fContentSpecType.length) {
            fContentSpecType = resize(fContentSpecType, fContentSpecType.length * 2);
            fContentSpecValue = resize(fContentSpecValue, fContentSpecValue.length * 2);
            fContentSpecOtherValue = resize(fContentSpecOtherValue, fContentSpecOtherValue.length * 2);
            fContentSpecValidator = resize(fContentSpecValidator, fContentSpecValidator.length * 2);
        } else if (fContentSpecType[chunk] != null) {
            return;
        }
        fContentSpecType[chunk] = new int[CHUNK_SIZE];
        fContentSpecValue[chunk] = new int[CHUNK_SIZE];
        fContentSpecOtherValue[chunk] = new int[CHUNK_SIZE];
        fContentSpecValidator[chunk] = new XMLContentModel[CHUNK_SIZE];
    }

    private void ensureAttributeDeclCapacity(int chunk) {
        if (chunk >= fAttributeDeclName.length) {
            fAttributeDeclName = resize(fAttributeDeclName, fAttributeDeclName.length * 2);
            fAttributeDeclType = resize(fAttributeDeclType, fAttributeDeclType.length * 2);
            fAttributeDeclEnumeration = resize(fAttributeDeclEnumeration, fAttributeDeclEnumeration.length * 2);
            fAttributeDeclDefaultType = resize(fAttributeDeclDefaultType, fAttributeDeclDefaultType.length * 2);
            fAttributeDeclDatatypeValidator = resize(fAttributeDeclDatatypeValidator, fAttributeDeclDatatypeValidator.length * 2);
            fAttributeDeclDefaultValue = resize(fAttributeDeclDefaultValue, fAttributeDeclDefaultValue.length * 2);
            fAttributeDeclNextAttributeDeclIndex = resize(fAttributeDeclNextAttributeDeclIndex, fAttributeDeclNextAttributeDeclIndex.length * 2);
        } else if (fAttributeDeclName[chunk] != null) {
            return;
        }
        fAttributeDeclName[chunk] = new QName[CHUNK_SIZE];
        fAttributeDeclType[chunk] = new int[CHUNK_SIZE];
        fAttributeDeclEnumeration[chunk] = new int[CHUNK_SIZE];
        fAttributeDeclDefaultType[chunk] = new int[CHUNK_SIZE];
        fAttributeDeclDatatypeValidator[chunk] = new DatatypeValidator[CHUNK_SIZE];
        fAttributeDeclDefaultValue[chunk] = new String[CHUNK_SIZE];
        fAttributeDeclNextAttributeDeclIndex[chunk] = new int[CHUNK_SIZE];
    }

    // resize initial chunk

    private int[][] resize(int array[][], int newsize) {
        int newarray[][] = new int[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private DatatypeValidator[][] resize(DatatypeValidator array[][], int newsize) {
        DatatypeValidator newarray[][] = new DatatypeValidator[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private XMLContentModel[][] resize(XMLContentModel array[][], int newsize) {
        XMLContentModel newarray[][] = new XMLContentModel[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private QName[][] resize(QName array[][], int newsize) {
        QName newarray[][] = new QName[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private String[][] resize(String array[][], int newsize) {
        String newarray[][] = new String[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    private Vector[][] resize(Vector array[][], int newsize) {
        Vector newarray[][] = new Vector[newsize][];
        System.arraycopy(array, 0, newarray, 0, array.length);
        return newarray;
    }

    //
    // Classes
    //

    /**
     * Children list for <code>contentSpecTree</code> method.
     */
    static class ChildrenList {
        public int length = 0;
        public QName[] qname = new QName[2];
        public int[] type = new int[2];
    }

} // class Grammar
