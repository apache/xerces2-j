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

package org.apache.xerces.validators.dtd;

import org.apache.xerces.framework.XMLAttrList;
import org.apache.xerces.framework.XMLContentSpecNode;
import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLMessages;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.LocatorImpl;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 * @version
 */
public final class ElementDeclPool {
    //
    // Chunk size constants
    //
    private static final int CHUNK_SHIFT = 5;           // 2^5 = 32
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));   // 2^10 = 1k
    //
    // Instance variables
    //
    private StringPool fStringPool = null;
    private XMLErrorReporter fErrorReporter = null;
    //
    // Element list
    //
    private int fElementCount = 0;
    private int[][] fElementType = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fElementDeclIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpecType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fContentSpec = new int[INITIAL_CHUNK_COUNT][];
    private XMLContentModel[][] fContentModel = new XMLContentModel[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistHead = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttlistTail = new int[INITIAL_CHUNK_COUNT][];
    //
    // ContentSpecNode list
    //
    private int fNodeCount = 0;
    private byte[][] fNodeType = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNodeValue = new int[INITIAL_CHUNK_COUNT][];
    //
    // AttDef list
    //
    private int fAttDefCount = 0;
    private int[][] fAttName = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fEnumeration = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttDefaultType = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fAttValue = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fAttDefIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int[][] fNextAttDef = new int[INITIAL_CHUNK_COUNT][];
    //
    // Element Type Hashtable
    //
    private static final int INITIAL_BUCKET_SIZE = 4;
    private static final int HASHTABLE_SIZE = 128;
    private int[][] fElementTypeHashtable = new int[HASHTABLE_SIZE][];
    //
    //
    //
    private int fIDSymbol = -1;
    private int fNotationSymbol = -1;
    private int fMIXEDSymbol = -1;
    private int fCHILDRENSymbol = -1;
    private Hashtable fIdDefs = null;
    private Hashtable fIdRefs = null;
    private Object fNullValue = null;
    //
    //
    //
    public ElementDeclPool(StringPool stringPool, XMLErrorReporter errorReporter) {
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
    }
    //
    //
    //
    public void reset(StringPool stringPool) {
        fStringPool = stringPool;
        int chunk = 0;
        int index = 0;
        for (int i = 0; i < fElementCount; i++) {
            fContentModel[chunk][index] = null;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        for (int i = 0; i < HASHTABLE_SIZE; i++)
            fElementTypeHashtable[i] = null;
        fElementCount = 0;
        fNodeCount = 0;
        fAttDefCount = 0;
        fIDSymbol = -1;
        fNotationSymbol = -1;
        fMIXEDSymbol = -1;
        fCHILDRENSymbol = -1;
        if (fIdDefs != null)
            fIdDefs.clear();
        if (fIdRefs != null)
            fIdRefs.clear();
    }
    //
    // Element entries
    //
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
        }
        fElementType[chunk] = new int[CHUNK_SIZE];
        fElementDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
        fContentSpecType[chunk] = new int[CHUNK_SIZE];
        fContentSpec[chunk] = new int[CHUNK_SIZE];
        fContentModel[chunk] = new XMLContentModel[CHUNK_SIZE];
        fAttlistHead[chunk] = new int[CHUNK_SIZE];
        fAttlistTail[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    public int getElement(int elementType) {
//System.out.println("Pool " + this + " get " + elementType + " (" + fStringPool.toString(elementType) + ")");
        elementType = fStringPool.getFullNameForQName(elementType);
        int hc = elementType % HASHTABLE_SIZE;
        int[] bucket = fElementTypeHashtable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == elementType)
                    return bucket[j+1];
                j += 2;
            }
        }
        return -1;
    }
    public int addElement(int elementTypeIndex) {
        int elementType = fStringPool.getFullNameForQName(elementTypeIndex);
        int hc = elementType % HASHTABLE_SIZE;
        int[] bucket = fElementTypeHashtable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == elementType)
                    return bucket[j+1];
                j += 2;
            }
        }
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        fElementType[chunk][index] = elementTypeIndex;
        fElementDeclIsExternal[chunk][index] = 0;
        fContentSpecType[chunk][index] = -1;
        fContentSpec[chunk][index] = -1;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        if (bucket == null) {
            bucket = new int[1 + (INITIAL_BUCKET_SIZE * 2)];
            bucket[0] = 1;
            bucket[1] = elementType;
            bucket[2] = fElementCount;
            fElementTypeHashtable[hc] = bucket;
        } else {
            int count = bucket[0];
            int offset = 1 + (count * 2);
            if (offset == bucket.length) {
                int newSize = count + INITIAL_BUCKET_SIZE;
                int[] newBucket = new int[1 + (newSize * 2)];
                System.arraycopy(bucket, 0, newBucket, 0, offset);
                bucket = newBucket;
                fElementTypeHashtable[hc] = bucket;
            }
            bucket[offset++] = elementType;
            bucket[offset++] = fElementCount;
            bucket[0] = ++count;
        }
        return fElementCount++;
    }
    public int addElementDecl(int elementTypeIndex, int contentSpecType, int contentSpec, boolean isExternal) {
//System.out.println("Pool " + this + " add " + decl.elementType + " (" + fStringPool.toString(decl.elementType) + ")");
        int elementType = fStringPool.getFullNameForQName(elementTypeIndex);
        int hc = elementType % HASHTABLE_SIZE;
        int[] bucket = fElementTypeHashtable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == elementType) {
                    int elementIndex = bucket[j+1];
                    int chunk = elementIndex >> CHUNK_SHIFT;
                    int index = elementIndex & CHUNK_MASK;
                    if (fContentSpecType[chunk][index] != -1)
                        return -1;
                    fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
                    fContentSpecType[chunk][index] = contentSpecType;
                    fContentSpec[chunk][index] = contentSpec;
                    fContentModel[chunk][index] = null;
                    return elementIndex;
                }
                j += 2;
            }
        }
        int chunk = fElementCount >> CHUNK_SHIFT;
        int index = fElementCount & CHUNK_MASK;
        ensureElementCapacity(chunk);
        fElementType[chunk][index] = elementTypeIndex;
        fElementDeclIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fContentSpecType[chunk][index] = contentSpecType;
        fContentSpec[chunk][index] = contentSpec;
        fContentModel[chunk][index] = null;
        fAttlistHead[chunk][index] = -1;
        fAttlistTail[chunk][index] = -1;
        if (bucket == null) {
            bucket = new int[1 + (INITIAL_BUCKET_SIZE * 2)];
            bucket[0] = 1;
            bucket[1] = elementType;
            bucket[2] = fElementCount;
            fElementTypeHashtable[hc] = bucket;
        } else {
            int count = bucket[0];
            int offset = 1 + (count * 2);
            if (offset == bucket.length) {
                int newSize = count + INITIAL_BUCKET_SIZE;
                int[] newBucket = new int[1 + (newSize * 2)];
                System.arraycopy(bucket, 0, newBucket, 0, offset);
                bucket = newBucket;
                fElementTypeHashtable[hc] = bucket;
            }
            bucket[offset++] = elementType;
            bucket[offset++] = fElementCount;
            bucket[0] = ++count;
        }
        return fElementCount++;
    }
    public int getElementType(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fElementType[chunk][index];
    }
    public boolean getElementDeclIsExternal(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return false;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return (fElementDeclIsExternal[chunk][index] != 0);
    }
    public int getContentSpecType(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpecType[chunk][index];
    }
    public int getContentSpec(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return -1;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentSpec[chunk][index];
    }
    
    // added by twl
    public void setContentSpec(int elementIndex, int value) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        fContentSpec[chunk][index] =  value;
    }
    
    public String getContentSpecAsString(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return null;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        int contentSpecType = fContentSpecType[chunk][index];
        if (fMIXEDSymbol == -1) {
            fMIXEDSymbol = fStringPool.addSymbol("MIXED");
            fCHILDRENSymbol = fStringPool.addSymbol("CHILDREN");
        }
        if (contentSpecType == fMIXEDSymbol || contentSpecType == fCHILDRENSymbol)
            return getContentSpecNodeAsString(fContentSpec[chunk][index]);
        else
            return fStringPool.toString(contentSpecType);
    }
    public XMLContentModel getContentModel(int elementIndex) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return null;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        return fContentModel[chunk][index];
    }
    public void setContentModel(int elementIndex, XMLContentModel cm) {
        if (elementIndex < 0 || elementIndex >= fElementCount)
            return;
        int chunk = elementIndex >> CHUNK_SHIFT;
        int index = elementIndex & CHUNK_MASK;
        fContentModel[chunk][index] = cm;
    }
    //
    // contentspec entries
    //
    private boolean ensureNodeCapacity(int chunk) {
        try {
            return fNodeType[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fNodeType, 0, newByteArray, 0, chunk);
            fNodeType = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fNodeValue, 0, newIntArray, 0, chunk);
            fNodeValue = newIntArray;
        } catch (NullPointerException ex) {
        }
        fNodeType[chunk] = new byte[CHUNK_SIZE];
        fNodeValue[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    //
    //
    //
    private int addUniqueLeafNode(int nodeValue) throws Exception {
        //
        // Check that we have not seen this value before...
        //
        if (nodeValue != -1) {
            int nodeCount = fNodeCount;
            int chunk = fNodeCount >> CHUNK_SHIFT;
            int index = fNodeCount & CHUNK_MASK;
            while (true) {
                if (index-- == 0) {
                    index = CHUNK_SIZE - 1;
                    chunk--;
                }
                int nodeType = fNodeType[chunk][index];
                if (nodeType == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                    int otherNodeValue = fNodeValue[chunk][index];
                    if (otherNodeValue == -1)
                        break;
                    if (otherNodeValue == nodeValue) {
                        return -1;
                    }
                }
            }
        }
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        fNodeType[chunk][index] = (byte)XMLContentSpecNode.CONTENTSPECNODE_LEAF;
        fNodeValue[chunk][index] = nodeValue;
        return fNodeCount++;
    }
    //
    //
    //
    public int addContentSpecNode(int nodeType, int nodeValue, int otherNodeValue, boolean mustBeUnique) throws Exception {
        if (mustBeUnique) // REVISIT - merge these methods...
            return addUniqueLeafNode(nodeValue);
        int chunk = fNodeCount >> CHUNK_SHIFT;
        int index = fNodeCount & CHUNK_MASK;
        ensureNodeCapacity(chunk);
        switch (nodeType) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            fNodeType[chunk][index] = (byte)nodeType;
            fNodeValue[chunk][index] = nodeValue;
            return fNodeCount++;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            fNodeType[chunk][index] = (byte)nodeType;
            fNodeValue[chunk][index] = nodeValue;
            int nodeIndex = fNodeCount++;
            if (++index == CHUNK_SIZE) {
                chunk++;
                ensureNodeCapacity(chunk);
                index = 0;
            }
            fNodeType[chunk][index] = (byte)(nodeType | 64); // flag second entry for consistancy checking
            fNodeValue[chunk][index] = otherNodeValue;
            fNodeCount++;
            return nodeIndex;
        default:
            return -1;
        }
    }
    public void getContentSpecNode(int contentSpecIndex, XMLContentSpecNode csn) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        csn.type = fNodeType[chunk][index];
        csn.value = fNodeValue[chunk][index];
        if (csn.type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE || csn.type == XMLContentSpecNode.CONTENTSPECNODE_SEQ) {
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
            csn.otherValue = fNodeValue[chunk][index];
        } else
            csn.otherValue = -1;
    }
    private void appendContentSpecNode(int contentSpecIndex, StringBuffer sb, boolean noParen) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        switch (type) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
            sb.append(value == -1 ? "#PCDATA" : fStringPool.toString(value));
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            appendContentSpecNode(value, sb, false);
            sb.append('?');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
            appendContentSpecNode(value, sb, false);
            sb.append('*');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            appendContentSpecNode(value, sb, false);
            sb.append('+');
            return;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            if (!noParen)
                sb.append('(');
            int leftChunk = value >> CHUNK_SHIFT;
            int leftIndex = value & CHUNK_MASK;
            int leftType = fNodeType[leftChunk][leftIndex];
            appendContentSpecNode(value, sb, leftType == type);
            sb.append(type == XMLContentSpecNode.CONTENTSPECNODE_CHOICE ? '|' : ',');
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
            appendContentSpecNode(fNodeValue[chunk][index], sb, false);
            if (!noParen)
                sb.append(')');
            return;
        default:
            return;
        }
    }
    public String getContentSpecNodeAsString(int contentSpecIndex) {
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex & CHUNK_MASK;
        int type = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];
        StringBuffer sb = new StringBuffer();
        switch (type) {
        case XMLContentSpecNode.CONTENTSPECNODE_LEAF:
            sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")");
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")?");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")*");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE:
            chunk = value >> CHUNK_SHIFT;
            index = value & CHUNK_MASK;
            if (fNodeType[chunk][index] == XMLContentSpecNode.CONTENTSPECNODE_LEAF) {
                value = fNodeValue[chunk][index];
                sb.append("(" + (value == -1 ? "#PCDATA" : fStringPool.toString(value)) + ")+");
            } else
                appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
        case XMLContentSpecNode.CONTENTSPECNODE_SEQ:
            appendContentSpecNode(contentSpecIndex, sb, false);
            break;
        default:
            return null;
        }
        return sb.toString();
    }
    //
    // attribute list interfaces
    //
    private boolean ensureAttrCapacity(int chunk) {
        try {
            return fAttName[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fAttDefIsExternal, 0, newByteArray, 0, chunk);
            fAttDefIsExternal = newByteArray;
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttName, 0, newIntArray, 0, chunk);
            fAttName = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttType, 0, newIntArray, 0, chunk);
            fAttType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fEnumeration, 0, newIntArray, 0, chunk);
            fEnumeration = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttDefaultType, 0, newIntArray, 0, chunk);
            fAttDefaultType = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fAttValue, 0, newIntArray, 0, chunk);
            fAttValue = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fNextAttDef, 0, newIntArray, 0, chunk);
            fNextAttDef = newIntArray;
        } catch (NullPointerException ex) {
        }
        fAttDefIsExternal[chunk] = new byte[CHUNK_SIZE];
        fAttName[chunk] = new int[CHUNK_SIZE];
        fAttType[chunk] = new int[CHUNK_SIZE];
        fEnumeration[chunk] = new int[CHUNK_SIZE];
        fAttDefaultType[chunk] = new int[CHUNK_SIZE];
        fAttValue[chunk] = new int[CHUNK_SIZE];
        fNextAttDef[chunk] = new int[CHUNK_SIZE];
        return true;
    }
    //
    public int addAttDef(int elementIndex, int attName, int attType, int enumeration, int attDefaultType, int attDefaultValue, boolean isExternal, boolean validationEnabled, boolean warnOnDuplicate) throws Exception {
        //
        // check fields
        //
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int dupID = -1;
        int dupNotation = -1;
        if (validationEnabled && fIDSymbol == -1) {
            fIDSymbol = fStringPool.addSymbol("ID");
            fNotationSymbol = fStringPool.addSymbol("NOTATION");
        }
        while (attlistIndex != -1) {
            int attrChunk = attlistIndex >> CHUNK_SHIFT;
            int attrIndex = attlistIndex & CHUNK_MASK;
            if (fStringPool.equalNames(fAttName[attrChunk][attrIndex], attName)) {
                if (warnOnDuplicate) {
                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                      fStringPool.toString(attName) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_DUPLICATE_ATTDEF,
                                               XMLMessages.P53_DUPLICATE,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                return -1;
            }
            if (validationEnabled) {
                if (attType == fIDSymbol && fAttType[attrChunk][attrIndex] == fIDSymbol)
                    dupID = fAttName[attrChunk][attrIndex];
                if (attType == fNotationSymbol && fAttType[attrChunk][attrIndex] == fNotationSymbol)
                    dupNotation = fAttName[attrChunk][attrIndex];
            }
            attlistIndex = fNextAttDef[attrChunk][attrIndex];
        }
        if (validationEnabled) {
            if (dupID != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupID),
                                  fStringPool.toString(attName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_ID_ATTRIBUTE,
                                           XMLMessages.VC_ONE_ID_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
            if (dupNotation != -1) {
                Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                  fStringPool.toString(dupNotation),
                                  fStringPool.toString(attName) };
                fErrorReporter.reportError(fErrorReporter.getLocator(),
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_MORE_THAN_ONE_NOTATION_ATTRIBUTE,
                                           XMLMessages.VC_ONE_NOTATION_PER_ELEMENT_TYPE,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                return -1;
            }
        }
        //
        // save the fields
        //
        int chunk = fAttDefCount >> CHUNK_SHIFT;
        int index = fAttDefCount & CHUNK_MASK;
        ensureAttrCapacity(chunk);
        fAttName[chunk][index] = attName;
        fAttType[chunk][index] = attType;
        fEnumeration[chunk][index] = enumeration;
        fAttDefaultType[chunk][index] = attDefaultType;
        fAttDefIsExternal[chunk][index] = (byte)(isExternal ? 1 : 0);
        fAttValue[chunk][index] = attDefaultValue;
        //
        // add to the attr list for this element
        //
        int nextIndex = -1;
        if (attDefaultValue != -1) {
            nextIndex = fAttlistHead[elemChunk][elemIndex];
            fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1)
                fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
        } else {
            nextIndex = fAttlistTail[elemChunk][elemIndex];
            fAttlistTail[elemChunk][elemIndex] = fAttDefCount;
            if (nextIndex == -1)
                fAttlistHead[elemChunk][elemIndex] = fAttDefCount;
            else {
                fNextAttDef[nextIndex >> CHUNK_SHIFT][nextIndex & CHUNK_MASK] = fAttDefCount;
                nextIndex = -1;
            }
        }
        fNextAttDef[chunk][index] = nextIndex;
        return fAttDefCount++;
    }
    public int getAttDef(int elementType, int attrNameIndex) {
        int chunk = 0;
        int index = 0;
        int elementIndex;
        for (elementIndex = 0; elementIndex < fElementCount; elementIndex++) {
            if (fStringPool.equalNames(fElementType[chunk][index], elementType)) {
                int attDefIndex = fAttlistHead[chunk][index];
                while (true) {
                    if (attDefIndex == -1)
                        return -1;
                    chunk = attDefIndex >> CHUNK_SHIFT;
                    index = attDefIndex & CHUNK_MASK;
                    if (fStringPool.equalNames(fAttName[chunk][index], attrNameIndex))
                        return attDefIndex;
                    attDefIndex = fNextAttDef[chunk][index];
                }
            }
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        return -1;
    }
    
    // added by twl
    public boolean copyAtts(int fromElementTypeIndex, int toElementTypeIndex) {
        int chunk = 0;
        int index = 0;
        int elementIndex;
        try {
           for (elementIndex = 0; elementIndex < fElementCount; elementIndex++) {
                if (fElementType[chunk][index] == fromElementTypeIndex) {
                    int attDefIndex = fAttlistHead[chunk][index];
                    while (attDefIndex != -1) {
/*                        System.out.println("copying attribute "+fStringPool.toString(getAttName(attDefIndex))+
                                  " from "+fStringPool.toString(fromElementTypeIndex)+
                                  " to "+fStringPool.toString(toElementTypeIndex)+" "+
//                                  " attDefIndex:"+
//                                  getAttType(attDefIndex)+" "+
                                  " enum:"+getEnumeration(attDefIndex)+
                                  " default:"+getAttDefaultType(attDefIndex)+
                                  " defaultvalue:"+getAttValue(attDefIndex)+
                                  " isExternal:"+getAttDefIsExternal(attDefIndex)
                        ); 
*/                        addAttDef(getElement(toElementTypeIndex), 
                                  getAttName(attDefIndex), 
                                  getAttType(attDefIndex), 
                                  getEnumeration(attDefIndex), 
                                  getAttDefaultType(attDefIndex), 
                                  getAttValue(attDefIndex), 
                                  getAttDefIsExternal(attDefIndex),
                                  true, false);
                        chunk = attDefIndex >> CHUNK_SHIFT;
                        index = attDefIndex & CHUNK_MASK;
                        attDefIndex = fNextAttDef[chunk][index];
                    }
                    return true;
                }
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    index = 0;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    public boolean getAttDefIsExternal(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return (fAttDefIsExternal[chunk][index] != 0);
    }
    public int getAttName(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttName[chunk][index];
    }
    public int getAttValue(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttValue[chunk][index];
    }
    public int getAttType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttType[chunk][index];
    }
    public int getAttDefaultType(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fAttDefaultType[chunk][index];
    }
    public int getEnumeration(int attDefIndex) {
        int chunk = attDefIndex >> CHUNK_SHIFT;
        int index = attDefIndex & CHUNK_MASK;
        return fEnumeration[chunk][index];
    }
    public int addDefaultAttributes(int elementIndex, XMLAttrList attrList, int attrIndex, boolean validationEnabled, boolean standalone) throws Exception {
        //
        // Check after all specified attrs are scanned
        // (1) report error for REQUIRED attrs that are missing (V_TAGc)
        // (2) check that FIXED attrs have matching value (V_TAGd)
        // (3) add default attrs (FIXED and NOT_FIXED)
        //
        int elemChunk = elementIndex >> CHUNK_SHIFT;
        int elemIndex = elementIndex & CHUNK_MASK;
        int attlistIndex = fAttlistHead[elemChunk][elemIndex];
        int firstCheck = attrIndex;
        int lastCheck = -1;
        while (attlistIndex != -1) {
            int adChunk = attlistIndex >> CHUNK_SHIFT;
            int adIndex = attlistIndex & CHUNK_MASK;
            int attName = fAttName[adChunk][adIndex];
            int attType = fAttType[adChunk][adIndex];
            int attDefType = fAttDefaultType[adChunk][adIndex];
            int attValue = fAttValue[adChunk][adIndex];
            boolean specified = false;
            boolean required = attDefType == fStringPool.addSymbol("#REQUIRED");
            boolean implied = attDefType == fStringPool.addSymbol("#IMPLIED");
            if (firstCheck != -1) {
                boolean cdata = attType == fStringPool.addSymbol("CDATA");
                boolean fixed = attDefType == fStringPool.addSymbol("#FIXED");
                if (!cdata || required || attValue != -1) {
                    int i = attrList.getFirstAttr(firstCheck);
                    while (i != -1 && (lastCheck == -1 || i <= lastCheck)) {
                        if (fStringPool.equalNames(attrList.getAttrName(i), attName)) {
                            if (validationEnabled && fixed) {
                                int alistValue = attrList.getAttValue(i);
                                if (alistValue != attValue &&
                                    !fStringPool.toString(alistValue).equals(fStringPool.toString(attValue))) {
                                    Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                                    fStringPool.toString(attName),
                                                    fStringPool.toString(alistValue),
                                                    fStringPool.toString(attValue) };
                                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                            XMLMessages.XML_DOMAIN,
                                                            XMLMessages.MSG_FIXED_ATTVALUE_INVALID,
                                                            XMLMessages.VC_FIXED_ATTRIBUTE_DEFAULT,
                                                            args,
                                                            XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                                }
                            }
                            specified = true;
                            break;
                        }
                        i = attrList.getNextAttr(i);
                    }
                }
            }
            if (!specified) {
                if (required) {
                    if (validationEnabled) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_REQUIRED_ATTRIBUTE,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                } else if (attValue != -1) {
                    if (validationEnabled && standalone && fAttDefIsExternal[adChunk][adIndex] != 0) {
                        Object[] args = { fStringPool.toString(fElementType[elemChunk][elemIndex]),
                                          fStringPool.toString(attName) };
                        fErrorReporter.reportError(fErrorReporter.getLocator(),
                                                   XMLMessages.XML_DOMAIN,
                                                   XMLMessages.MSG_DEFAULTED_ATTRIBUTE_NOT_SPECIFIED,
                                                   XMLMessages.VC_STANDALONE_DOCUMENT_DECLARATION,
                                                   args,
                                                   XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
                    }
                    if (attType == fStringPool.addSymbol("IDREF")) {
                        addIdRef(attValue);
                    } else if (attType == fStringPool.addSymbol("IDREFS")) {
                        StringTokenizer tokenizer = new StringTokenizer(fStringPool.toString(attValue));
                        while (tokenizer.hasMoreTokens()) {
                            String idName = tokenizer.nextToken();
                            addIdRef(fStringPool.addSymbol(idName));
                        }
                    }
                    if (attrIndex == -1)
                        attrIndex = attrList.startAttrList();
                    int newAttr = attrList.addAttr(attName, attValue, attType, false, false);
                    if (lastCheck == -1)
                        lastCheck = newAttr;
                }
            }
            attlistIndex = fNextAttDef[adChunk][adIndex];
        }
        return attrIndex;
    }
    //
    //
    //
    public boolean addId(int idIndex) {
//        System.out.println("addId(" + fStringPool.toString(idIndex) + ")");
        Integer key = new Integer(idIndex);
        if (fIdDefs == null)
            fIdDefs = new Hashtable();
        else if (fIdDefs.containsKey(key))
            return false;
        if (fNullValue == null)
            fNullValue = new Object();
        fIdDefs.put(key, fNullValue/*new Integer(elementType)*/);
        return true;
    }
    public void addIdRef(int idIndex) {
//        System.out.println("addIdRef(" + fStringPool.toString(idIndex) + ")");
        Integer key = new Integer(idIndex);
        if (fIdDefs != null && fIdDefs.containsKey(key))
            return;
        if (fIdRefs == null)
            fIdRefs = new Hashtable();
        else if (fIdRefs.containsKey(key))
            return;
        fIdRefs.put(key, new LocatorImpl(fErrorReporter.getLocator()));
    }
    public void checkIdRefs() throws Exception {
        if (fIdRefs == null)
            return;
        Enumeration en = fIdRefs.keys();
        while (en.hasMoreElements()) {
            Integer key = (Integer)en.nextElement();
            if (fIdDefs == null || !fIdDefs.containsKey(key)) {
                Object[] args = { fStringPool.toString(key.intValue()) };
Locator loc = (Locator)fIdRefs.get(key);
if (loc == null) loc = fErrorReporter.getLocator();
                fErrorReporter.reportError(loc,
                                           XMLMessages.XML_DOMAIN,
                                           XMLMessages.MSG_ELEMENT_WITH_ID_REQUIRED,
                                           XMLMessages.VC_IDREF,
                                           args,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }
    }

    /** 
     * Checks that all declared elements refer to declared elements
     * in their content models. This method calls out to the error
     * handler to indicate warnings.
     */
    public void checkDeclaredElements() throws Exception {
        for (int i = 0; i < fElementCount; i++) {
            int type  = getContentSpecType(i);
            if (type == fStringPool.addSymbol("MIXED") ||
                type == fStringPool.addSymbol("CHILDREN")) {
                int chunk = i >> CHUNK_SHIFT;
                int index = i &  CHUNK_MASK;
                int contentSpecIndex = fContentSpec[chunk][index];
                checkDeclaredElements(i, contentSpecIndex);
            }
        }
    }

    /** 
     * Does a recursive (if necessary) check on the specified element's
     * content spec to make sure that all children refer to declared
     * elements.
     * <p>
     * This method assumes that it will only be called when there is
     * a validation handler.
     */
    private void checkDeclaredElements(int elementIndex, int contentSpecIndex) throws Exception {
        
        // get spec type and value
        int chunk = contentSpecIndex >> CHUNK_SHIFT;
        int index = contentSpecIndex &  CHUNK_MASK;
        int type  = fNodeType[chunk][index];
        int value = fNodeValue[chunk][index];

        // handle type
        switch (type) {
        
            // #PCDATA | element
            case XMLContentSpecNode.CONTENTSPECNODE_LEAF: {
                // perform check for declared element
                if (value != -1 && getElement(value) == -1) {
                    int elemChunk = elementIndex >> CHUNK_SHIFT;
                    int elemIndex = elementIndex &  CHUNK_MASK;
                    int elementType = fElementType[elemChunk][elemIndex];
                    Object[] args = { fStringPool.toString(elementType),
                                      fStringPool.toString(value) };
                    fErrorReporter.reportError(fErrorReporter.getLocator(),
                                               XMLMessages.XML_DOMAIN,
                                               XMLMessages.MSG_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               XMLMessages.P45_UNDECLARED_ELEMENT_IN_CONTENTSPEC,
                                               args,
                                               XMLErrorReporter.ERRORTYPE_WARNING);
                }
                break;
            }

            // (...)? | (...)* | (...)+
            case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_ONE:
            case XMLContentSpecNode.CONTENTSPECNODE_ZERO_OR_MORE: 
            case XMLContentSpecNode.CONTENTSPECNODE_ONE_OR_MORE: {
                checkDeclaredElements(elementIndex, value);
                break;
            }

            // (... , ...) | (... | ...)
            case XMLContentSpecNode.CONTENTSPECNODE_CHOICE:
            case XMLContentSpecNode.CONTENTSPECNODE_SEQ: {
                checkDeclaredElements(elementIndex, value);
                if (++index == CHUNK_SIZE) {
                    chunk++;
                    index = 0;
                }
                checkDeclaredElements(elementIndex, fNodeValue[chunk][index]);
                break;
            }
        }
    }
}
