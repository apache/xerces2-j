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

package org.apache.xerces.dom;

import java.util.Vector;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.utils.StringPool;

import org.w3c.dom.*;

/**
 * The Document interface represents the entire HTML or XML document.
 * Conceptually, it is the root of the document tree, and provides the
 * primary access to the document's data.
 * <P>
 * Since elements, text nodes, comments, processing instructions,
 * etc. cannot exist outside the context of a Document, the Document
 * interface also contains the factory methods needed to create these
 * objects. The Node objects created have a ownerDocument attribute
 * which associates them with the Document within whose context they
 * were created.
 *
 * @version
 * @since  PR-DOM-Level-1-19980818.
 */
public class DeferredDocumentImpl
    extends DocumentImpl {

    //
    // Constants
    //

    /** Serialization version. */
    static final long serialVersionUID = 5186323580749626857L;

    // debugging

    /** To include code for printing the internal tables. */
    private static final boolean DEBUG_PRINT_TABLES = false;

    /** To debug identifiers set to true and recompile. */
    private static final boolean DEBUG_IDS = false;

    // protected

    /** Chunk shift. */
    protected static final int CHUNK_SHIFT = 11;           // 2^11 = 2k

    /** Chunk size. */
    protected static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);

    /** Chunk mask. */
    protected static final int CHUNK_MASK = CHUNK_SIZE - 1;

    /** Initial chunk size. */
    protected static final int INITIAL_CHUNK_COUNT = (1 << (16 - CHUNK_SHIFT));   // 2^16 = 64k

    //
    // Data
    //

    // lazy-eval information

    /** Node count. */
    protected transient int fNodeCount = 0;

    /** Node types. */
    protected transient byte fNodeType[][];

    /** Node names. */
    protected transient int fNodeName[][];

    /** Node values. */
    protected transient int fNodeValue[][];

    /** Node parents. */
    protected transient int fNodeParent[][];

    /** Node first children. */
    protected transient int fNodeFirstChild[][];

    /** Node last children. */
    protected transient int fNodeLastChild[][];

    /** Node previous siblings. */
    protected transient int fNodePrevSib[][];

    /** Node next siblings. */
    protected transient int fNodeNextSib[][];

    /** Identifier count. */
    protected transient int fIdCount;

    /** Identifier name indexes. */
    protected transient int fIdName[];

    /** Identifier element indexes. */
    protected transient int fIdElement[];

    /** String pool cache. */
    protected transient StringPool fStringPool;

	/** DOM2: For namespace support in the deferred case.
	 */
	// Implementation Note: The deferred element and attribute must know how to
	// interpret the int representing the qname.
    protected boolean fNamespacesEnabled = false;
    //
    // Constructors
    //

    /**
     * NON-DOM: Actually creating a Document is outside the DOM's spec,
     * since it has to operate in terms of a particular implementation.
     */
    public DeferredDocumentImpl(StringPool stringPool) {
        this(stringPool, false);
    } // <init>(ParserState)

    /**
     * NON-DOM: Actually creating a Document is outside the DOM's spec,
     * since it has to operate in terms of a particular implementation.
     */
    public DeferredDocumentImpl(StringPool stringPool, boolean namespacesEnabled) {
        this(stringPool, namespacesEnabled, false);
    } // <init>(ParserState,boolean)

    /** Experimental constructor. */
    public DeferredDocumentImpl(StringPool stringPool,
                                boolean namespaces, boolean grammarAccess) {
        super(grammarAccess);

        fStringPool = stringPool;

        syncData = true;
        syncChildren = true;

        fNamespacesEnabled = namespaces;

    } // <init>(StringPool,boolean,boolean)

    //
    // Public methods
    //

    /** Returns the cached parser.getNamespaces() value.*/
    boolean getNamespacesEnabled() {
        return fNamespacesEnabled;
    }

    // internal factory methods

    /** Creates a document node in the table. */
    public int createDocument() {
        int nodeIndex = createNode(Node.DOCUMENT_NODE);
        return nodeIndex;
    }

    /** Creates a doctype. */
    public int createDocumentType(int rootElementNameIndex, int publicId, int systemId) {
        int nodeIndex = createNode(Node.DOCUMENT_TYPE_NODE);
        int chunk     = nodeIndex >> CHUNK_SHIFT;
        int index     = nodeIndex & CHUNK_MASK;
        fNodeName[chunk][index] = rootElementNameIndex;

        // added for DOM2: createDoctype factory method includes
        // name, publicID, systemID

        // make extra data node
        int extraDataIndex = createNode(Node.TEXT_NODE);
        int echunk = extraDataIndex >> CHUNK_SHIFT;
        int eindex = extraDataIndex & CHUNK_MASK;

        // save name, public id, system id
        fNodeValue[chunk][index] = extraDataIndex;
        fNodeFirstChild[echunk][eindex] = publicId;
        fNodeLastChild[echunk][eindex]  = systemId;

        return nodeIndex;
    }

    /** Creates a notation in the table. */
    public int createNotation(int notationName, int publicId, int systemId) throws Exception {

        // make entity node
        int nodeIndex = createNode(Node.NOTATION_NODE);
        int chunk     = nodeIndex >> CHUNK_SHIFT;
        int index     = nodeIndex & CHUNK_MASK;

        // make extra data node
        int extraDataIndex = createNode(Node.TEXT_NODE);
        int echunk = extraDataIndex >> CHUNK_SHIFT;
        int eindex = extraDataIndex & CHUNK_MASK;

        // save name, public id, system id, and notation name
        fNodeValue[chunk][index] = extraDataIndex;
        fNodeName[chunk][index]         = notationName;
        fNodeFirstChild[echunk][eindex] = publicId;
        fNodeLastChild[echunk][eindex]  = systemId;

        return nodeIndex;

    } // createNotation(int):int

    /** Creates an entity in the table. */
    public int createEntity(int entityName, int publicId, int systemId, int notationName) throws Exception {

        // make entity node
        int nodeIndex = createNode(Node.ENTITY_NODE);
        int chunk     = nodeIndex >> CHUNK_SHIFT;
        int index     = nodeIndex & CHUNK_MASK;

        // make extra data node
        int extraDataIndex = createNode(Node.TEXT_NODE);
        int echunk = extraDataIndex >> CHUNK_SHIFT;
        int eindex = extraDataIndex & CHUNK_MASK;

        // save name, public id, system id, and notation name
        fNodeValue[chunk][index] = extraDataIndex;
        fNodeName[chunk][index]         = entityName;
        fNodeFirstChild[echunk][eindex] = publicId;
        fNodeLastChild[echunk][eindex]  = systemId;
        fNodePrevSib[echunk][eindex]    = notationName;

        return nodeIndex;

    } // createEntity(int):int

    /** Creates an entity reference node in the table. */
    public int createEntityReference(int nameIndex) throws Exception {

        int nodeIndex = createNode(Node.ENTITY_REFERENCE_NODE);
        int chunk     = nodeIndex >> CHUNK_SHIFT;
        int index     = nodeIndex & CHUNK_MASK;

        // save name
        fNodeName[chunk][index] = nameIndex;
        return nodeIndex;

    } // createEntityReference(int):int

    /** Creates an element node in the table. */
    public int createElement(int elementNameIndex, XMLAttrList attrList, int attrListIndex) {

        // create element
        int elementNodeIndex = createNode(Node.ELEMENT_NODE);
        int elementChunk     = elementNodeIndex >> CHUNK_SHIFT;
        int elementIndex     = elementNodeIndex & CHUNK_MASK;
        fNodeName[elementChunk][elementIndex] = elementNameIndex;
        //fNodeValue[chunk][index] = attrListIndex;

        // create attributes
        if (attrListIndex != -1) {
            int first = attrList.getFirstAttr(attrListIndex);
            int lastAttrNodeIndex = -1;
            int lastAttrChunk = -1;
            int lastAttrIndex = -1;
            for (int index = first; index != -1;
                 index = attrList.getNextAttr(index)) {

                // create attribute
                int attrNodeIndex = createAttribute(attrList.getAttrName(index),
                                                    attrList.getAttValue(index),
                                                    attrList.isSpecified(index));
                int attrChunk = attrNodeIndex >> CHUNK_SHIFT;
                int attrIndex  = attrNodeIndex & CHUNK_MASK;
                fNodeParent[attrChunk][attrIndex] = elementNodeIndex;

                // add links
                if (index == first) {
                    fNodeValue[elementChunk][elementIndex] = attrNodeIndex;
                }
                else {
                    fNodeNextSib[lastAttrChunk][lastAttrIndex] = attrNodeIndex;
                    fNodePrevSib[attrChunk][attrIndex] = lastAttrNodeIndex;
                }

                // save last chunk and index
                lastAttrNodeIndex = attrNodeIndex;
                lastAttrChunk     = attrChunk;
                lastAttrIndex     = attrIndex;
            }
        }

        return elementNodeIndex;

    } // createElement(int,int):int

    /** Creates an attributes in the table. */
    public int createAttribute(int attrNameIndex, int attrValueIndex,
                               boolean specified) {

        // create attribute and set specified
        int nodeIndex = createNode(NodeImpl.ATTRIBUTE_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeName[chunk][index]  = attrNameIndex;
        fNodeValue[chunk][index] = specified ? 1 : 0;

        // append value as text node
        int textNodeIndex = createTextNode(attrValueIndex, false);
        appendChild(nodeIndex, textNodeIndex);

        // return attribute node
        return nodeIndex;

    } // createAttribute(int):int

    /** Creates an element definition in the table. */
    public int createElementDefinition(int elementNameIndex) {

        int nodeIndex = createNode(NodeImpl.ELEMENT_DEFINITION_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeName[chunk][index] = elementNameIndex;

        return nodeIndex;

    } // createElementDefinition(int):int

    /** Creates a text node in the table. */
    public int createTextNode(int dataIndex, boolean ignorableWhitespace) {

        int nodeIndex = createNode(Node.TEXT_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeValue[chunk][index] = dataIndex;
        fNodeFirstChild[chunk][index] = ignorableWhitespace ?  1 : 0;
        return nodeIndex;

    }

    /** Creates a CDATA section node in the table. */
    public int createCDATASection(int dataIndex, boolean ignorableWhitespace) {

        int nodeIndex = createNode(Node.CDATA_SECTION_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeValue[chunk][index] = dataIndex;
        fNodeFirstChild[chunk][index] = ignorableWhitespace ?  1 : 0;
        return nodeIndex;

    }

    /** Creates a processing instruction node in the table. */
    public int createProcessingInstruction(int targetIndex, int dataIndex) {

        int nodeIndex = createNode(Node.PROCESSING_INSTRUCTION_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeName[chunk][index] = targetIndex;
        fNodeValue[chunk][index] = dataIndex;

        return nodeIndex;

    } // createProcessingInstruction(int,int):int

    /** Creates a comment node in the table. */
    public int createComment(int dataIndex) {

        int nodeIndex = createNode(Node.COMMENT_NODE);
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        fNodeValue[chunk][index] = dataIndex;

        return nodeIndex;

    } // createComment(int):int

    /** Appends a child to the specified parent in the table. */
    public void appendChild(int parentIndex, int childIndex) {

        int pchunk = parentIndex >> CHUNK_SHIFT;
        int pindex = parentIndex & CHUNK_MASK;
        int chunk = childIndex >> CHUNK_SHIFT;
        int index = childIndex & CHUNK_MASK;
        fNodeParent[chunk][index] = parentIndex;
        int prev = fNodeLastChild[pchunk][pindex];
        fNodePrevSib[chunk][index] = prev;
        if (prev == -1) {
            fNodeFirstChild[pchunk][pindex] = childIndex;
            }
        else {
            int chnk = prev >> CHUNK_SHIFT;
            int indx = prev & CHUNK_MASK;
            fNodeNextSib[chnk][indx] = childIndex;
        }
        fNodeLastChild[pchunk][pindex] = childIndex;

    } // appendChild(int,int)

    /** Adds an attribute node to the specified element. */
    public int setAttributeNode(int elemIndex, int attrIndex) {

        int echunk = elemIndex >> CHUNK_SHIFT;
        int eindex = elemIndex & CHUNK_MASK;
        int achunk = attrIndex >> CHUNK_SHIFT;
        int aindex = attrIndex & CHUNK_MASK;

        // see if this attribute is already here
        String attrName = fStringPool.toString(fNodeName[achunk][aindex]);
        int oldAttrIndex = fNodeValue[echunk][eindex];
        int oachunk = -1;
        int oaindex = -1;
        while (oldAttrIndex != -1) {
            oachunk = oldAttrIndex >> CHUNK_SHIFT;
            oaindex = oldAttrIndex & CHUNK_MASK;
            String oldAttrName = fStringPool.toString(fNodeName[oachunk][oaindex]);
            if (oldAttrName.equals(attrName)) {
                break;
            }
            oldAttrIndex = fNodeNextSib[oachunk][oaindex];
        }

        // remove old attribute
        if (oldAttrIndex != -1) {

            // patch links
            int prevIndex = fNodePrevSib[oachunk][oaindex];
            int nextIndex = fNodeNextSib[oachunk][oaindex];
            if (prevIndex == -1) {
                fNodeValue[echunk][eindex] = nextIndex;
            }
            else {
                int pchunk = prevIndex >> CHUNK_SHIFT;
                int pindex = prevIndex & CHUNK_MASK;
                fNodeNextSib[pchunk][pindex] = nextIndex;
            }
            if (nextIndex != -1) {
                int nchunk = nextIndex >> CHUNK_SHIFT;
                int nindex = nextIndex & CHUNK_MASK;
                fNodePrevSib[nchunk][nindex] = prevIndex;
            }

            // remove connections to siblings
            fNodePrevSib[oachunk][oaindex] = -1;
            fNodeNextSib[oachunk][oaindex] = -1;
        }

        // add new attribute
        int nextIndex = fNodeValue[echunk][eindex];
        fNodeValue[echunk][eindex] = attrIndex;
        fNodeNextSib[achunk][aindex] = nextIndex;
        if (nextIndex != -1) {
            int nchunk = nextIndex >> CHUNK_SHIFT;
            int nindex = nextIndex & CHUNK_MASK;
            fNodePrevSib[nchunk][nindex] = attrIndex;
        }

        // return
        return oldAttrIndex;

    } // setAttributeNode(int,int)

    /** Inserts a child before the specified node in the table. */
    public int insertBefore(int parentIndex, int newChildIndex, int refChildIndex) {

        if (refChildIndex == -1) {
            appendChild(parentIndex, newChildIndex);
            return newChildIndex;
        }

        int pchunk = parentIndex >> CHUNK_SHIFT;
        int pindex = parentIndex & CHUNK_MASK;
        int nchunk = newChildIndex >> CHUNK_SHIFT;
        int nindex = newChildIndex & CHUNK_MASK;
        int rchunk = refChildIndex >> CHUNK_SHIFT;
        int rindex = refChildIndex & CHUNK_MASK;

        // 1) if ref.parent.first = ref then
        //      begin
        //        ref.parent.first := new;
        //      end;
        int firstIndex = getFirstChild(parentIndex);
        if (firstIndex == refChildIndex) {
            fNodeFirstChild[pchunk][pindex] = newChildIndex;
        }
        // 2) if ref.prev != null then
        //      begin
        //        ref.prev.next := new;
        //      end;
        int prevIndex = getPreviousSibling(refChildIndex);
        if (prevIndex != -1) {
            int chunk = prevIndex >> CHUNK_SHIFT;
            int index = prevIndex & CHUNK_MASK;
            fNodeNextSib[chunk][index] = newChildIndex;
        }
        // 3) new.prev := ref.prev;
        fNodePrevSib[nchunk][nindex] = prevIndex;
        // 4) new.next := ref;
        fNodeNextSib[nchunk][nindex] = refChildIndex;
        // 5) ref.prev := new;
        fNodePrevSib[rchunk][rindex] = newChildIndex;

        return newChildIndex;

    } // insertBefore(int,int):int

    /** Sets the first child of the parentIndex to childIndex. */
    public void setAsFirstChild(int parentIndex, int childIndex) {

        int pchunk = parentIndex >> CHUNK_SHIFT;
        int pindex = parentIndex & CHUNK_MASK;
        int chunk = childIndex >> CHUNK_SHIFT;
        int index = childIndex & CHUNK_MASK;
        fNodeFirstChild[pchunk][pindex] = childIndex;

        int next = childIndex;

        while (next != -1) {

            childIndex = next;
            next = fNodeNextSib[chunk][index];
            chunk = next >> CHUNK_SHIFT;
            index = next & CHUNK_MASK;
        }

        fNodeLastChild[pchunk][pindex] = childIndex;

    } // setAsFirstChild(int,int)

    // methods used when objects are "fluffed-up"

    /** Returns the parent node of the given node. */
    public int getParentNode(int nodeIndex) {

        // REVISIT: should this be "public"? -Ac

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeParent[chunk][index];

    } // getParentNode(int):int

    /** Returns the first child of the given node. */
    public int getFirstChild(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeFirstChild[chunk][index];

    } // getFirstChild(int):int

    /** Returns the last child of the given node. */
    public int getLastChild(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        int lastChild =  fNodeLastChild[chunk][index];

        // revisit: the following could be more efficient.

        // if the last child is a TEXT_NODE
        if (lastChild != -1 && fNodeType[chunk][index] == Node.TEXT_NODE) {
            // get the previous sibling.
            int previousIndex = fNodePrevSib[chunk][index];
            chunk = previousIndex >> CHUNK_SHIFT;
            index = previousIndex & CHUNK_MASK;
            // if the previous Sibling is a TEXT_NODE
            if (previousIndex != -1 && fNodeType[chunk][index] == Node.TEXT_NODE) {
                // while previous sibling is a text node.
                while (previousIndex != -1 && fNodeType[chunk][index] == Node.TEXT_NODE) {
                    nodeIndex = previousIndex;
                    previousIndex = fNodePrevSib[chunk][index];
                    chunk = previousIndex >> CHUNK_SHIFT;
                    index = previousIndex & CHUNK_MASK;
                }
                return nodeIndex;
            }
        }

        return lastChild;

    } // getLastChild(int):int

    /** Returns the previous sibling of the given node. */
    public int getPreviousSibling(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        int previousIndex = fNodePrevSib[chunk][index];

        // revisit: the following could be more efficient.

        // if the previous sibling exists and this node is not a text node.
        if (previousIndex != -1
        && fNodeType[chunk][index] != Node.TEXT_NODE) {
            chunk = previousIndex >> CHUNK_SHIFT;
            index = previousIndex & CHUNK_MASK;
            // if the previous node is a text node.
            if (fNodeType[chunk][index] == Node.TEXT_NODE) {
                // while the previous node exists and is a text node.
                while (previousIndex != -1
                    && fNodeType[chunk][index] == Node.TEXT_NODE) {
                    nodeIndex = previousIndex;
                    previousIndex = fNodePrevSib[chunk][index];
                    chunk = previousIndex >> CHUNK_SHIFT;
                    index = previousIndex & CHUNK_MASK;
                }
                return nodeIndex;
            }
        }

        return previousIndex;

    } // getPreviousSibling(int):int

    /** Returns the next sibling of the given node.
     *  This is post-normalization of Text Nodes.
     */
    public int getNextSibling(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        nodeIndex = fNodeNextSib[chunk][index];

        while (nodeIndex != -1 && fNodeType[chunk][index] == Node.TEXT_NODE) {
            nodeIndex = fNodeNextSib[chunk][index];
            chunk = nodeIndex >> CHUNK_SHIFT;
            index = nodeIndex & CHUNK_MASK;
        }

        return nodeIndex;

    } // getNextSibling(int):int

    /**
     * Returns the <i>real</i> next sibling of the given node,
     * directly from the data structures. Used by TextImpl#getNodeValue()
     * to normalize values.
     */
    public int getRealNextSibling(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeNextSib[chunk][index];

        } // getReadNextSibling(int):int

    /**
     * Returns the index of the element definition in the table
     * with the specified name index, or -1 if no such definition
     * exists.
     */
    public int lookupElementDefinition(int elementNameIndex) {

        if (fNodeCount > 1) {

            // find doctype
            int docTypeIndex = -1;
            for (int index = getFirstChild(0); // 0 == document node
                 index != -1;
                 index = getNextSibling(index)) {
                if (getNodeType(index) == Node.DOCUMENT_TYPE_NODE) {
                    docTypeIndex = index;
                    break;
                }
            }

            // find element definition
            for (int index = getFirstChild(docTypeIndex);
                 index != -1;
                 index = getNextSibling(index)) {
                if (getNodeName(index) == elementNameIndex) {
                    return index;
                }
            }
        }

        return -1;

    } // lookupElementDefinition(int):int

    /** Returns the attribute list index for elements. */
    public int getAttributeList(int elementNodeIndex) {

        if (elementNodeIndex == -1) {
            return -1;
        }

        int chunk = elementNodeIndex >> CHUNK_SHIFT;
        int index = elementNodeIndex & CHUNK_MASK;
        return fNodeValue[chunk][index];

    } // getAttributeList(int):int

    /** Instantiates the requested node object. */
    public DeferredNode getNodeObject(int nodeIndex) {

        // is there anything to do?
        if (nodeIndex == -1) {
            return null;
        }

        // get node type
        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];

        // create new node
        DeferredNode node = null;
        switch (type) {

            //
            // Standard DOM node types
            //

            case Node.ATTRIBUTE_NODE: {
                node = new DeferredAttrImpl(this, nodeIndex);
                break;
            }

            case Node.CDATA_SECTION_NODE: {
                node = new DeferredCDATASectionImpl(this, nodeIndex);
                break;
            }

            case Node.COMMENT_NODE: {
                node = new DeferredCommentImpl(this, nodeIndex);
                break;
            }

            // NOTE: Document fragments can never be "fast".
            //
            //       The parser will never ask to create a document
            //       fragment during the parse. Document fragments
            //       are used by the application *after* the parse.
            //
            // case Node.DOCUMENT_FRAGMENT_NODE: { break; }
            /***
            case Node.DOCUMENT_NODE: {
                // this node is never "fast"
                node = this;
                break;
            }
            /***/

            case Node.DOCUMENT_TYPE_NODE: {
                node = new DeferredDocumentTypeImpl(this, nodeIndex);
                // save the doctype node
                docType = (DocumentTypeImpl)node;
                break;
            }

            case Node.ELEMENT_NODE: {

                if (DEBUG_IDS) {
                    System.out.println("getNodeObject(ELEMENT_NODE): "+nodeIndex);
                }

                // create node
                node = new DeferredElementImpl(this, nodeIndex);

                // save the document element node
                if (docElement == null) {
                    docElement = (ElementImpl)node;
                }

                // check to see if this element needs to be
                // registered for its ID attributes
                if (fIdElement != null) {
                    int idIndex = DeferredDocumentImpl.binarySearch(fIdElement, 0, fIdCount, nodeIndex);
                    while (idIndex != -1) {

                        if (DEBUG_IDS) {
                            System.out.println("  id index: "+idIndex);
                            System.out.println("  fIdName["+idIndex+
                                               "]: "+fIdName[idIndex]);
                        }

                        // register ID
                        int nameIndex = fIdName[idIndex];
                        if (nameIndex != -1) {
                            String name = fStringPool.toString(nameIndex);
                            if (DEBUG_IDS) {
                                System.out.println("  name: "+name);
                                System.out.print("getNodeObject()#");
                            }
                            putIdentifier0(name, (Element)node);
                            fIdName[idIndex] = -1;
                        }

                        // continue if there are more IDs for
                        // this element
                        if (idIndex < fIdCount &&
                            fIdElement[idIndex + 1] == nodeIndex) {
                            idIndex++;
                        }
                        else {
                            idIndex = -1;
                        }
                    }
                }
                break;
            }

            case Node.ENTITY_NODE: {
                node = new DeferredEntityImpl(this, nodeIndex);
                break;
            }

            case Node.ENTITY_REFERENCE_NODE: {
                node = new DeferredEntityReferenceImpl(this, nodeIndex);
                break;
            }

            case Node.NOTATION_NODE: {
                node = new DeferredNotationImpl(this, nodeIndex);
                break;
            }

            case Node.PROCESSING_INSTRUCTION_NODE: {
                node = new DeferredProcessingInstructionImpl(this, nodeIndex);
                break;
            }

            case Node.TEXT_NODE: {
                node = new DeferredTextImpl(this, nodeIndex);
                break;
            }

            //
            // non-standard DOM node types
            //

            case NodeImpl.ELEMENT_DEFINITION_NODE: {
                node = new DeferredElementDefinitionImpl(this, nodeIndex);
                break;
            }

        } // switch node type

        // store and return
        if (node != null) {
            return node;
        }

        // error
        throw new IllegalArgumentException();

    } // createNodeObject(int):Node

    /** Returns the name of the given node. */
    public String getNodeNameString(int nodeIndex) {

        if (nodeIndex == -1) {
            return null;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        int nameIndex = fNodeName[chunk][index];
        if (nameIndex == -1) {
            return null;
        }

        return fStringPool.toString(nameIndex);

    } // getNodeNameString(int):String

    /** Returns the value of the given node. */
    public String getNodeValueString(int nodeIndex) {

        if (nodeIndex == -1) {
            return null;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        int valueIndex = fNodeValue[chunk][index];
        if (valueIndex == -1) {
            return null;
        }

        return fStringPool.toString(valueIndex);

    } // getNodeValueString(int):String

    /** Returns the real int name of the given node. */
    public int getNodeName(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeName[chunk][index];

    } // getNodeName(int):int

    /**
     * Returns the real int value of the given node.
     *  Used by AttrImpl to store specified value (1 == true).
     */
    public int getNodeValue(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeValue[chunk][index];

    } // getNodeValue(int):int

    /** Returns the type of the given node. */
    public short getNodeType(int nodeIndex) {

        if (nodeIndex == -1) {
            return -1;
        }

        int chunk = nodeIndex >> CHUNK_SHIFT;
        int index = nodeIndex & CHUNK_MASK;
        return fNodeType[chunk][index];

    } // getNodeType(int):int

    // identifier maintenance

    /** Registers an identifier name with a specified element node. */
    public void putIdentifier(int nameIndex, int elementNodeIndex) {

        if (DEBUG_IDS) {
            System.out.println("putIdentifier("+nameIndex+", "+elementNodeIndex+')'+
                               " // "+
                               fStringPool.toString(nameIndex)+
                               ", "+
                               fStringPool.toString(fNodeName[elementNodeIndex >> CHUNK_SHIFT][elementNodeIndex & CHUNK_MASK]));
        }

        // initialize arrays
        if (fIdName == null) {
            fIdName    = new int[64];
            fIdElement = new int[64];
        }

        // resize arrays
        if (fIdCount == fIdName.length) {
            int idName[] = new int[fIdCount * 2];
            System.arraycopy(fIdName, 0, idName, 0, fIdCount);
            fIdName = idName;

            int idElement[] = new int[idName.length];
            System.arraycopy(fIdElement, 0, idElement, 0, fIdCount);
            fIdElement = idElement;
        }

        // store identifier
        fIdName[fIdCount] = nameIndex;
        fIdElement[fIdCount] = elementNodeIndex;
        fIdCount++;

    } // putIdentifier(int,int)

    //
    // DEBUG
    //

    /** Prints out the tables. */
    public void print() {

        if (DEBUG_PRINT_TABLES) {
            // This assumes that the document is small
            System.out.println("# start table");
            for (int i = 0; i < fNodeCount; i++) {
                if (i % 10 == 0) {
                    System.out.println("num\ttype\tname\tval\tpar\tfch\tlch\tpsib\tnsib");
                }
                System.out.print(i);
                System.out.print('\t');
                System.out.print(fNodeType[0][i]);
                System.out.print('\t');
                System.out.print(fNodeName[0][i]);
                System.out.print('\t');
                System.out.print(fNodeValue[0][i]);
                System.out.print('\t');
                System.out.print(fNodeParent[0][i]);
                System.out.print('\t');
                System.out.print(fNodeFirstChild[0][i]);
                System.out.print('\t');
                System.out.print(fNodeLastChild[0][i]);
                System.out.print('\t');
                System.out.print(fNodePrevSib[0][i]);
                System.out.print('\t');
                System.out.print(fNodeNextSib[0][i]);
                System.out.println();
            }
            System.out.println("# end table");
        }

    } // print()

    //
    // Protected methods
    //

    /** access to string pool. */
    protected StringPool getStringPool() {
        return fStringPool;
    }

    /** Synchronizes the node's data. */
    protected void synchronizeData() {

        // no need to sync in the future
        syncData = false;

        // fluff up enough nodes to fill identifiers hash
        if (fIdElement != null) {

            // REVISIT: There has to be a more efficient way of
            //          doing this. But keep in mind that the
            //          tree can have been altered and re-ordered
            //          before all of the element nodes with ID
            //          attributes have been registered. For now
            //          this is reasonable and safe. -Ac

            IntVector path = new IntVector();
            for (int i = 0; i < fIdCount; i++) {

                // ignore if it's already been registered
                int elementNodeIndex = fIdElement[i];
                int idNameIndex      = fIdName[i];
                if (idNameIndex == -1) {
                    continue;
                }

                // find path from this element to the root
                path.removeAllElements();
                int index = elementNodeIndex;
                do {
                    path.addElement(index);
                    index = getParentNode(index);
                } while (index != -1);

                // Traverse path (backwards), fluffing the elements
                // along the way. When this loop finishes, "place"
                // will contain the reference to the element node
                // we're interested in. -Ac
                Node place = this;
                for (int j = path.size() - 2; j >= 0; j--) {
                    index = path.elementAt(j);
                    Node child = place.getFirstChild();
                    while (child != null) {
                        if (child instanceof DeferredNode) {
                            int nodeIndex = ((DeferredNode)child).getNodeIndex();
                            if (nodeIndex == index) {
                                place = child;
                                break;
                            }
                        }
                        child = child.getNextSibling();
                    }
                }

                // register the element
                Element element = (Element)place;
                String  name    = fStringPool.toString(idNameIndex);
                putIdentifier0(name, element);
                fIdName[i] = -1;

                // see if there are more IDs on this element
                while (fIdElement[i + 1] == elementNodeIndex) {
                    name = fStringPool.toString(fIdName[++i]);
                    putIdentifier0(name, element);
                }
            }

        } // if identifiers

    } // synchronizeData()

    /**
     * Synchronizes the node's children with the internal structure.
     * Fluffing the children at once solves a lot of work to keep
     * the two structures in sync. The problem gets worse when
     * editing the tree -- this makes it a lot easier.
     */
    protected void synchronizeChildren() {

        // no need to sync in the future
        syncChildren = false;

        // create children and link them as siblings
        NodeImpl last = null;
        for (int index = getFirstChild(0);
             index != -1;
             index = getNextSibling(index)) {

            NodeImpl node = (NodeImpl)getNodeObject(index);
            if (last == null) {
                firstChild = node;
            }
            else {
                last.nextSibling = node;
            }
            node.parentNode = this;
            node.previousSibling = last;
            last = node;

            // save doctype and document type
            int type = node.getNodeType();
            if (type == Node.ELEMENT_NODE) {
                docElement = (ElementImpl)node;
            }
            else if (type == Node.DOCUMENT_TYPE_NODE) {
                docType = (DocumentTypeImpl)node;
            }
        }

        if (last != null) {
            lastChild = last;
        }

    } // synchronizeChildren()

    // utility methods

    /** Ensures that the internal tables are large enough. */
    protected boolean ensureCapacity(int chunk, int index) {

        // create buffers
        if (fNodeType == null) {
            fNodeType       = new byte[INITIAL_CHUNK_COUNT][];
            fNodeName       = new int[INITIAL_CHUNK_COUNT][];
            fNodeValue      = new int[INITIAL_CHUNK_COUNT][];
            fNodeParent     = new int[INITIAL_CHUNK_COUNT][];
            fNodeFirstChild = new int[INITIAL_CHUNK_COUNT][];
            fNodeLastChild  = new int[INITIAL_CHUNK_COUNT][];
            fNodePrevSib    = new int[INITIAL_CHUNK_COUNT][];
            fNodeNextSib    = new int[INITIAL_CHUNK_COUNT][];
        }

        // return true if table is already big enough
        try {
            return fNodeType[chunk][index] != 0;
        }

        // resize the tables
        catch (ArrayIndexOutOfBoundsException ex) {
            //int newsize = chunk + (int)((float)chunk * 0.5);
            int newsize = chunk * 2;
            //System.out.println("chunk: "+chunk+", newsize: "+newsize);

            byte[][] newByteArray = new byte[newsize][];
            System.arraycopy(fNodeType, 0, newByteArray, 0, chunk);
            fNodeType = newByteArray;

            int[][] newIntArray = new int[newsize][];
            System.arraycopy(fNodeName, 0, newIntArray, 0, chunk);
            fNodeName = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodeValue, 0, newIntArray, 0, chunk);
            fNodeValue = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodeParent, 0, newIntArray, 0, chunk);
            fNodeParent = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodeFirstChild, 0, newIntArray, 0, chunk);
            fNodeFirstChild = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodeLastChild, 0, newIntArray, 0, chunk);
            fNodeLastChild = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodePrevSib, 0, newIntArray, 0, chunk);
            fNodePrevSib = newIntArray;

            newIntArray = new int[newsize][];
            System.arraycopy(fNodeNextSib, 0, newIntArray, 0, chunk);
            fNodeNextSib = newIntArray;
        }

        catch (NullPointerException ex) {
            // ignore
        }

        // create chunks
        fNodeType[chunk]       = new byte[CHUNK_SIZE];
        fNodeName[chunk]       = new int[CHUNK_SIZE];
        fNodeValue[chunk]      = new int[CHUNK_SIZE];
        fNodeParent[chunk]     = new int[CHUNK_SIZE];
        fNodeFirstChild[chunk] = new int[CHUNK_SIZE];
        fNodeLastChild[chunk]  = new int[CHUNK_SIZE];
        fNodePrevSib[chunk]    = new int[CHUNK_SIZE];
        fNodeNextSib[chunk]    = new int[CHUNK_SIZE];

        // success
        //System.out.println("/ensureCapacity");
        return true;

    } // ensureCapacity(int,int):boolean

    /** Creates a node of the specified type. */
    protected int createNode(short nodeType) {

        // ensure tables are large enough
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureCapacity(chunk, index);

        // initialize node
        fNodeType[chunk][index]       = (byte)nodeType;
        fNodeName[chunk][index]       = -1;
        fNodeValue[chunk][index]      = -1;
        fNodeParent[chunk][index]     = -1;
        fNodeFirstChild[chunk][index] = -1;
        fNodeLastChild[chunk][index]  = -1;
        fNodePrevSib[chunk][index]    = -1;
        fNodeNextSib[chunk][index]    = -1;

        // return node index number
        return fNodeCount++;

    } // createNode(short):int

    /**
     * Performs a binary search for a target value in an array of
     * values. The array of values must be in ascending sorted order
     * before calling this method and all array values must be
     * non-negative.
     *
     * @param values  The array of values to search.
     * @param start   The starting offset of the search.
     * @param end     The ending offset of the search.
     * @param target  The target value.
     *
     * @return This function will return the <i>first</i> occurrence
     *         of the target value, or -1 if the target value cannot
     *         be found.
     */
    protected static int binarySearch(final int values[],
                                      int start, int end, int target) {

        if (DEBUG_IDS) {
            System.out.println("binarySearch(), target: "+target);
        }

        // look for target value
        while (start <= end) {

            // is this the one we're looking for?
            int middle = (start + end) / 2;
            int value  = values[middle];
            if (DEBUG_IDS) {
                System.out.print("  value: "+value+", target: "+target+" // ");
                print(values, start, end, middle, target);
            }
            if (value == target) {
                while (middle > 0 && values[middle - 1] == target) {
                    middle--;
                }
                if (DEBUG_IDS) {
                    System.out.println("FOUND AT "+middle);
                }
                return middle;
            }

            // is this point higher or lower?
            if (value > target) {
                end = middle - 1;
            }
            else {
                start = middle + 1;
            }

        } // while

        // not found
        if (DEBUG_IDS) {
            System.out.println("NOT FOUND!");
        }
        return -1;

    } // binarySearch(int[],int,int,int):int

    //
    // Private methods
    //

    /**
     * This version of putIdentifier is needed to avoid fluffing
     * all of the paths to ID attributes when a node object is
     * created that contains an ID attribute.
     */
    private void putIdentifier0(String idName, Element element) {

        if (DEBUG_IDS) {
            System.out.println("putIdentifier0("+
                               idName+", "+
                               element+')');
        }

        // create hashtable
        if (identifiers == null) {
            identifiers = new java.util.Hashtable();
        }

        // save ID and its associated element
        identifiers.put(idName, element);

    } // putIdentifier0(String,Element)

    /** Prints the ID array. */
    private static void print(int values[], int start, int end,
                              int middle, int target) {

        if (DEBUG_IDS) {
            System.out.print(start);
            System.out.print(" [");
            for (int i = start; i < end; i++) {
                if (middle == i) {
                    System.out.print("!");
                }
                System.out.print(values[i]);
                if (values[i] == target) {
                    System.out.print("*");
                }
                if (i < end - 1) {
                    System.out.print(" ");
                }
            }
            System.out.println("] "+end);
        }

    } // print(int[],int,int,int,int)

    //
    // Classes
    //

    /**
     * A simple integer vector.
     */
    static class IntVector {

        //
        // Data
        //

        /** Data. */
        private int data[];

        /** Size. */
        private int size;

        //
        // Public methods
        //

        /** Returns the length of this vector. */
        public int size() {
            return size;
        }

        /** Returns the element at the specified index. */
        public int elementAt(int index) {
            return data[index];
        }

        /** Appends an element to the end of the vector. */
        public void addElement(int element) {
            ensureCapacity(size + 1);
            data[size++] = element;
        }

        /** Clears the vector. */
        public void removeAllElements() {
            size = 0;
        }

        //
        // Private methods
        //

        /** Makes sure that there is enough storage. */
        private void ensureCapacity(int newsize) {

            if (data == null) {
                data = new int[newsize + 15];
            }
            else if (newsize > data.length) {
                int newdata[] = new int[newsize + 15];
                System.arraycopy(data, 0, newdata, 0, data.length);
                data = newdata;
            }

        } // ensureCapacity(int)

    } // class IntVector

} // class DeferredDocumentImpl
