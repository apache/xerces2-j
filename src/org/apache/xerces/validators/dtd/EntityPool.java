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

import org.apache.xerces.framework.XMLErrorReporter;
import org.apache.xerces.utils.StringPool;
import org.apache.xerces.utils.XMLMessages;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;
import java.util.Vector;

/**
 *
 * @version
 */
public final class EntityPool {
    //
    // Constants
    //

    //
    // Chunk size constants
    //
    static final int CHUNK_SHIFT = 5;           // 2^5 = 32
    static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    static final int CHUNK_MASK = CHUNK_SIZE - 1;
    static final int INITIAL_CHUNK_COUNT = (1 << (10 - CHUNK_SHIFT));   // 2^10 = 1k
    //
    // Instance variables
    //
    private StringPool fStringPool = null;
    private XMLErrorReporter fErrorReporter = null;
    //
    // We store both EntityDecl and NotationDecl instances in this pool.
    // A NotationDecl has an fName field of -1.  The fNotationDeclHead
    // index is -1 if the NotationDecl list is empty, otherwise it contains
    // the index of the the last NotationDecl in the list and the fValue
    // field contains the index of the previous NotationDecl, or -1 when at
    // the end of the list.
    //
    private int fEntityCount = 0;
    private int[][] fName = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fValue = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fLocation = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fPublicId = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fSystemId = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fNotationName = new int[INITIAL_CHUNK_COUNT][];
    private byte[][] fDeclIsExternal = new byte[INITIAL_CHUNK_COUNT][];
    private int fNotationListHead = -1;
    private boolean fCreateStandardEntities = false;
    private Vector fRequiredNotations = null;
    //
    // Constructor
    //
    public EntityPool(StringPool stringPool, XMLErrorReporter errorReporter, boolean createStandardEntities) {
        fStringPool = stringPool;
        fErrorReporter = errorReporter;
        fCreateStandardEntities = createStandardEntities;
        if (fCreateStandardEntities) {
            createInternalEntity("lt", "&#60;");
            createInternalEntity("gt", ">");
            createInternalEntity("amp", "&#38;");
            createInternalEntity("apos", "\'");
            createInternalEntity("quot", "\"");
        }
    }
    //
    //
    //
    public void reset(StringPool stringPool) {
        fStringPool = stringPool;
        fEntityCount = 0;
        fNotationListHead = -1;
        if (fRequiredNotations != null)
            fRequiredNotations.removeAllElements();
        if (fCreateStandardEntities) {
            createInternalEntity("lt", "&#60;");
            createInternalEntity("gt", ">");
            createInternalEntity("amp", "&#38;");
            createInternalEntity("apos", "\'");
            createInternalEntity("quot", "\"");
        }
    }
    //
    //
    //
    private void createInternalEntity(String name, String value) {
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = fStringPool.addSymbol(name);
        fValue[chunk][index] = fStringPool.addString(value);
        fLocation[chunk][index] = -1;
        fPublicId[chunk][index] = -1;
        fSystemId[chunk][index] = -1;
        fNotationName[chunk][index] = -1;
        fEntityCount++;
    }
    //
    //
    //
    private boolean ensureCapacity(int chunk) {
        try {
            return fName[chunk][0] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            int[][] newIntArray = new int[chunk * 2][];
            System.arraycopy(fName, 0, newIntArray, 0, chunk);
            fName = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fValue, 0, newIntArray, 0, chunk);
            fValue = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fLocation, 0, newIntArray, 0, chunk);
            fLocation = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fPublicId, 0, newIntArray, 0, chunk);
            fPublicId = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fSystemId, 0, newIntArray, 0, chunk);
            fSystemId = newIntArray;
            newIntArray = new int[chunk * 2][];
            System.arraycopy(fNotationName, 0, newIntArray, 0, chunk);
            fNotationName = newIntArray;
            byte[][] newByteArray = new byte[chunk * 2][];
            System.arraycopy(fDeclIsExternal, 0, newByteArray, 0, chunk);
            fDeclIsExternal = newByteArray;
        } catch (NullPointerException ex) {
        }
        fName[chunk] = new int[CHUNK_SIZE];
        fValue[chunk] = new int[CHUNK_SIZE];
        fLocation[chunk] = new int[CHUNK_SIZE];
        fPublicId[chunk] = new int[CHUNK_SIZE];
        fSystemId[chunk] = new int[CHUNK_SIZE];
        fNotationName[chunk] = new int[CHUNK_SIZE];
        fDeclIsExternal[chunk] = new byte[CHUNK_SIZE];
        return true;
    }
    public int addEntityDecl(int name, int value, int location, int publicId, int systemId, int notationName, boolean isExternal) {
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = name;
        fValue[chunk][index] = value;
        fLocation[chunk][index] = location;
        fPublicId[chunk][index] = publicId;
        fSystemId[chunk][index] = systemId;
        fNotationName[chunk][index] = notationName;
        fDeclIsExternal[chunk][index] = isExternal ? (byte)0x80 : (byte)0;
        int entityIndex = fEntityCount++;
        return entityIndex;
    }
    public int addNotationDecl(int notationName, int publicId, int systemId, boolean isExternal) {
        int nIndex = fNotationListHead;
        while (nIndex != -1) {
            int chunk = nIndex >> CHUNK_SHIFT;
            int index = nIndex & CHUNK_MASK;
            if (fNotationName[chunk][index] == notationName)
                return -1;
            nIndex = fValue[chunk][index];
        }
        int chunk = fEntityCount >> CHUNK_SHIFT;
        int index = fEntityCount & CHUNK_MASK;
        ensureCapacity(chunk);
        fName[chunk][index] = -1;
        fValue[chunk][index] = fNotationListHead;
        fPublicId[chunk][index] = publicId;
        fSystemId[chunk][index] = systemId;
        fNotationName[chunk][index] = notationName;
        fDeclIsExternal[chunk][index] = isExternal ? (byte)0x80 : (byte)0;
        fNotationListHead = fEntityCount++;
        return fNotationListHead;
    }
    public int lookupEntity(int nameIndex) {
        if (nameIndex == -1)
            return -1;
        int chunk = 0;
        int index = 0;
        for (int entityIndex = 0; entityIndex < fEntityCount; entityIndex++) {
            if (fName[chunk][index] == nameIndex)
                return entityIndex;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        return -1;
    }
    public boolean isExternalEntity(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fValue[chunk][index] == -1);
    }
    public boolean isUnparsedEntity(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fNotationName[chunk][index] != -1);
    }
    public boolean getEntityDeclIsExternal(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fDeclIsExternal[chunk][index] < 0);
    }
    public int getEntityName(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fName[chunk][index];
    }
    public int getEntityValue(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fValue[chunk][index];
    }
    public int getEntityLocation(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fLocation[chunk][index];
    }
    public int getPublicId(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fPublicId[chunk][index];
    }
    public int getSystemId(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fSystemId[chunk][index];
    }
    public int lookupNotation(int nameIndex) {
        int nIndex = fNotationListHead;
        while (nIndex != -1) {
            int chunk = nIndex >> CHUNK_SHIFT;
            int index = nIndex & CHUNK_MASK;
            if (fNotationName[chunk][index] == nameIndex)
                return nIndex;
            nIndex = fValue[chunk][index];
        }
        return -1;
    }
    public boolean getNotationDeclIsExternal(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return (fDeclIsExternal[chunk][index] < 0);
    }
    public int getNotationName(int entityIndex) {
        int chunk = entityIndex >> CHUNK_SHIFT;
        int index = entityIndex & CHUNK_MASK;
        return fNotationName[chunk][index];
    }
    class RequiredNotation {
        RequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args) {
            fNotationName = notationName;
            fLocator = new LocatorImpl(locator); // snapshot of the current location
            fMajorCode = majorCode;
            fMinorCode = minorCode;
            fArgs = args;
        }
        int fNotationName;
        LocatorImpl fLocator;
        int fMajorCode;
        int fMinorCode;
        Object[] fArgs;
    };
    public void addRequiredNotation(int notationName, Locator locator, int majorCode, int minorCode, Object[] args) {
        if (fRequiredNotations == null)
            fRequiredNotations = new Vector();
        for (int index = 0; index < fRequiredNotations.size(); index++) {
            RequiredNotation rn = (RequiredNotation)fRequiredNotations.elementAt(index);
            if (rn.fNotationName == notationName)
                return; // REVISIT - do we want to keep just the first, or all of them?
        }
        fRequiredNotations.addElement(new RequiredNotation(notationName, locator, majorCode, minorCode, args));
    }
    public void checkRequiredNotations() throws Exception {
        if (fRequiredNotations == null)
            return;
        for (int index = 0; index < fRequiredNotations.size(); index++) {
            RequiredNotation rn = (RequiredNotation)fRequiredNotations.elementAt(index);
            if (lookupNotation(rn.fNotationName) == -1) {
                fErrorReporter.reportError(rn.fLocator,
                                           XMLMessages.XML_DOMAIN,
                                           rn.fMajorCode,
                                           rn.fMinorCode,
                                           rn.fArgs,
                                           XMLErrorReporter.ERRORTYPE_RECOVERABLE_ERROR);
            }
        }
    }
}
