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

package org.apache.xerces.utils;

import org.apache.xerces.readers.XMLEntityHandler;
import java.util.Vector;

//
//
//
public class UTF8DataChunk implements StringPool.StringProducer {
    //
    // Chunk size constants
    //
    public static final int CHUNK_SHIFT = 14;           // 2^14 = 16k
    public static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    public static final int CHUNK_MASK = CHUNK_SIZE - 1;
    //
    // Public constructor (factory)
    //
    public static UTF8DataChunk createChunk(StringPool stringPool, UTF8DataChunk prev) {

        synchronized (UTF8DataChunk.class) {
            if (fgFreeChunks != null) {
                UTF8DataChunk newChunk = fgFreeChunks;
                fgFreeChunks = newChunk.fNextChunk;
                newChunk.fNextChunk = null;
                newChunk.init(stringPool, prev);
                return newChunk;
            }
        }
        UTF8DataChunk chunk = new UTF8DataChunk(stringPool, prev);
        return chunk;
    }
    //
    //
    //
    public final byte[] toByteArray() {
        return fData;
    }
    //
    //
    //
    public void setByteArray(byte[] data) {
        fData = data;
    }
    //
    //
    //
    public UTF8DataChunk nextChunk() {
        return fNextChunk;
    }
    //
    //
    //
    public boolean clearPreviousChunk() {
        if (fPreviousChunk != null) {
            fPreviousChunk.setNextChunk(null);
            fPreviousChunk.removeRef();
//System.err.println("[" + fPreviousChunk.fChunk + "] " + fPreviousChunk.fRefCount + " refs after clearPreviousChunk");
//System.err.println("[" + fChunk + "] " + fRefCount + " refs after clearPreviousChunk");
            fPreviousChunk = null;
            return true;
        }
        return fChunk == 0;
    }
    //
    //
    //
    public void releaseChunk() {
        removeRef();
//System.err.println("[" + fChunk + "] " + fRefCount + " refs after releaseChunk");
    }
    //
    //
    //
    public void releaseString(int offset, int length) {
        removeRef();
    }
    //
    //
    //
    public String toString(int offset, int length) {
        int outOffset = 0;
        UTF8DataChunk dataChunk = this;
        int endOffset = offset + length;
        int index = offset & CHUNK_MASK;
        byte[] data = fData;
        boolean skiplf = false;
        while (offset < endOffset) {
            int b0 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if (b0 < 0x80) {
                if (skiplf) {
                    skiplf = false;
                    if (b0 == 0x0A)
                        continue;
                }
                if (b0 == 0x0D) {
                    b0 = 0x0A;
                    skiplf = true;
                }
                try {
                    fTempBuffer[outOffset] = (char)b0;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)b0;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)b0;
                }
                continue;
            }
            int b1 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xe0 & b0) == 0xc0) { // 110yyyyy 10xxxxxx
                int ch = ((0x1f & b0)<<6) + (0x3f & b1); // yyy yyxx xxxx (0x80 to 0x7ff)
                try {
                    fTempBuffer[outOffset] = (char)ch;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)ch;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)ch;
                }
                continue;
            }
            int b2 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xf0 & b0) == 0xe0) { // 1110zzzz 10yyyyyy 10xxxxxx
                int ch = ((0x0f & b0)<<12) + ((0x3f & b1)<<6) + (0x3f & b2); // zzzz yyyy yyxx xxxx (0x800 to 0xffff)
                try {
                    fTempBuffer[outOffset] = (char)ch;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)ch;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)ch;
                }
                continue;
            }
            int b3 = data[index++] & 0xff;  // 11110uuu 10uuzzzz 10yyyyyy 10xxxxxx
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            int ch = ((0x0f & b0)<<18) + ((0x3f & b1)<<12) + ((0x3f & b2)<<6) + (0x3f & b3);
            if (ch < 0x10000) {
                try {
                    fTempBuffer[outOffset] = (char)ch;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)ch;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)ch;
                }
            } else {
                char ch1 = (char)(((ch-0x00010000)>>10)+0xd800);
                char ch2 = (char)(((ch-0x00010000)&0x3ff)+0xdc00);
                try {
                    fTempBuffer[outOffset] = (char)ch1;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)ch1;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)ch1;
                }
                try {
                    fTempBuffer[outOffset] = (char)ch2;
                    outOffset++;
                } catch (NullPointerException ex) {
                    fTempBuffer = new char[CHUNK_SIZE];
                    fTempBuffer[outOffset++] = (char)ch2;
                } catch (ArrayIndexOutOfBoundsException ex) {
                    char[] newBuffer = new char[outOffset * 2];
                    System.arraycopy(fTempBuffer, 0, newBuffer, 0, outOffset);
                    fTempBuffer = newBuffer;
                    fTempBuffer[outOffset++] = (char)ch2;
                }
            }
        }
        return new String(fTempBuffer, 0, outOffset);
    }
    //
    //
    //
    public boolean equalsString(int offset, int length, char[] strChars, int strOffset, int strLength) {
        UTF8DataChunk dataChunk = this;
        int endOffset = offset + length;
        int index = offset & CHUNK_MASK;
        byte[] data = fData;
        boolean skiplf = false;
        while (offset < endOffset) {
            if (strLength-- == 0)
                return false;
            int b0 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if (b0 < 0x80) {
                if (skiplf) {
                    skiplf = false;
                    if (b0 == 0x0A)
                        continue;
                }
                if (b0 == 0x0D) {
                    b0 = 0x0A;
                    skiplf = true;
                }
                if (b0 != strChars[strOffset++])
                    return false;
                continue;
            }
            int b1 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xe0 & b0) == 0xc0) { // 110yyyyy 10xxxxxx
                int ch = ((0x1f & b0)<<6) + (0x3f & b1);
                if (ch != strChars[strOffset++])
                    return false;
                continue;
            }
            int b2 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xf0 & b0) == 0xe0) { // 1110zzzz 10yyyyyy 10xxxxxx
                int ch = ((0x0f & b0)<<12) + ((0x3f & b1)<<6) + (0x3f & b2);
                if (ch != strChars[strOffset++])
                    return false;
                continue;
            }
            int b3 = data[index++] & 0xff;  // 11110uuu 10uuzzzz 10yyyyyy 10xxxxxx
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            int ch = ((0x0f & b0)<<18) + ((0x3f & b1)<<12)
                   + ((0x3f & b2)<<6) + (0x3f & b3);
            if (ch < 0x10000) {
                if (ch != strChars[strOffset++])
                    return false;
            } else {
                if ((((ch-0x00010000)>>10)+0xd800) != strChars[strOffset++])
                    return false;
                if (strLength-- == 0)
                    return false;
                if ((((ch-0x00010000)&0x3ff)+0xdc00) != strChars[strOffset++])
                    return false;
            }
        }
        return (strLength == 0);
    }
    //
    //
    //
    public int addString(int offset, int length) {
        if (length == 0)
            return StringPool.EMPTY_STRING;
        int chunk = offset >> CHUNK_SHIFT;
        if (chunk != fChunk) {
            if (fPreviousChunk == null)
                throw new RuntimeException(new ImplementationMessages().createMessage(null, ImplementationMessages.INT_PCN, 0, null));
            return fPreviousChunk.addString(offset, length);
        }
        int lastChunk = (offset + length - 1) >> CHUNK_SHIFT;
        if (chunk == lastChunk) {
            addRef();
            return fStringPool.addString(this, offset & CHUNK_MASK, length);
        }
        String str = toString(offset & CHUNK_MASK, length);
        return fStringPool.addString(str);
    }
    //
    //
    //
    public int addSymbol(int offset, int length, int hashcode) {
        if (length == 0)
            return StringPool.EMPTY_STRING;
        int chunk = offset >> CHUNK_SHIFT;
        if (chunk != fChunk) {
            if (fPreviousChunk == null)
                throw new RuntimeException(new ImplementationMessages().createMessage(null, ImplementationMessages.INT_PCN, 0, null));
            return fPreviousChunk.addSymbol(offset, length, hashcode);
        }
        int lastChunk = (offset + length - 1) >> CHUNK_SHIFT;
        int index = offset & CHUNK_MASK;
        if (chunk == lastChunk) {
            if (hashcode == 0) {
                hashcode = getHashcode(index, length);
            }
            int symbol = fStringPool.lookupSymbol(this, index, length, hashcode);
            if (symbol == -1) {
                String str = toString(index, length);
                symbol = fStringPool.addNewSymbol(str, hashcode);
            }
            return symbol;
        }
        String str = toString(index, length);
        return fStringPool.addSymbol(str);
    }
    //
    //
    //
    public void append(XMLEntityHandler.CharBuffer charBuffer, int offset, int length) {
        //
        // Setup for the operation.
        //
        UTF8DataChunk dataChunk = chunkFor(offset);
        int endOffset = offset + length;
        int index = offset & CHUNK_MASK;
        byte[] data = dataChunk.fData;
        boolean skiplf = false;
        while (offset < endOffset) {
            int b0 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if (b0 < 0x80) {
                if (skiplf) {
                    skiplf = false;
                    if (b0 == 0x0A)
                        continue;
                }
                if (b0 == 0x0D) {
                    b0 = 0x0A;
                    skiplf = true;
                }
                charBuffer.append((char)b0);
                continue;
            }
            int b1 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xe0 & b0) == 0xc0) { // 110yyyyy 10xxxxxx
                int ch = ((0x1f & b0)<<6) + (0x3f & b1);
                charBuffer.append((char)ch); // yyy yyxx xxxx (0x80 to 0x7ff)
                continue;
            }
            int b2 = data[index++] & 0xff;
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            if ((0xf0 & b0) == 0xe0) { // 1110zzzz 10yyyyyy 10xxxxxx
                int ch = ((0x0f & b0)<<12) + ((0x3f & b1)<<6) + (0x3f & b2);
                charBuffer.append((char)ch); // zzzz yyyy yyxx xxxx (0x800 to 0xffff)
                continue;
            }
            int b3 = data[index++] & 0xff;  // 11110uuu 10uuzzzz 10yyyyyy 10xxxxxx
            offset++;
            if (index == CHUNK_SIZE && offset < endOffset) {
                dataChunk = dataChunk.fNextChunk;
                data = dataChunk.fData;
                index = 0;
            }
            int ch = ((0x0f & b0)<<18) + ((0x3f & b1)<<12)
                   + ((0x3f & b2)<<6) + (0x3f & b3);
            if (ch < 0x10000)
                charBuffer.append((char)ch);
            else {
                charBuffer.append((char)(((ch-0x00010000)>>10)+0xd800));
                charBuffer.append((char)(((ch-0x00010000)&0x3ff)+0xdc00));
            }
        }
    }
    //
    //
    //
    private int getHashcode(int index, int length) {
        int endIndex = index + length;
        int hashcode = 0;
        byte[] data = fData;
        while (index < endIndex) {
            int b0 = data[index++] & 0xff;
            if ((b0 & 0x80) == 0) {
                hashcode = StringHasher.hashChar(hashcode, b0);
                continue;
            }
            int b1 = data[index++] & 0xff;
            if ((0xe0 & b0) == 0xc0) { // 110yyyyy 10xxxxxx
                int ch = ((0x1f & b0)<<6) + (0x3f & b1); // yyy yyxx xxxx (0x80 to 0x7ff)
                hashcode = StringHasher.hashChar(hashcode, ch);
                continue;
            }
            int b2 = data[index++] & 0xff;
            if ((0xf0 & b0) == 0xe0) { // 1110zzzz 10yyyyyy 10xxxxxx
                int ch = ((0x0f & b0)<<12) + ((0x3f & b1)<<6) + (0x3f & b2); // zzzz yyyy yyxx xxxx (0x800 to 0xffff)
                hashcode = StringHasher.hashChar(hashcode, ch);
                continue;
            }
            int b3 = data[index++] & 0xff;  // 11110uuu 10uuzzzz 10yyyyyy 10xxxxxx
            int ch = ((0x0f & b0)<<18) + ((0x3f & b1)<<12)
                    + ((0x3f & b2)<<6) + (0x3f & b3);
            if (ch < 0x10000)
                hashcode = StringHasher.hashChar(hashcode, ch);
            else {
                hashcode = StringHasher.hashChar(hashcode, (int)(((ch-0x00010000)>>10)+0xd800));
                hashcode = StringHasher.hashChar(hashcode, (int)(((ch-0x00010000)&0x3ff)+0xdc00));
            }
        }
        return StringHasher.finishHash(hashcode);
    }
    //
    //
    //
    private void init(StringPool stringPool, UTF8DataChunk prev) {
        fStringPool = stringPool;
        fRefCount = 1;
        fChunk = prev == null ? 0 : prev.fChunk + 1;
        fNextChunk = null;
        fPreviousChunk = prev;
        if (prev != null) {
            prev.addRef();
            prev.setNextChunk(this);
            prev.removeRef();
        }
    }
    //
    // Constructor for factory method.
    //
    private UTF8DataChunk(StringPool stringPool, UTF8DataChunk prev) {
        init(stringPool, prev);
    }
    //
    //
    //
    private final UTF8DataChunk chunkFor(int offset) {
        if ((offset >> CHUNK_SHIFT) == fChunk)
            return this;
        return slowChunkFor(offset);
    }
    private UTF8DataChunk slowChunkFor(int offset) {
        int firstChunk = offset >> CHUNK_SHIFT;
        UTF8DataChunk dataChunk = this;
        while (firstChunk != dataChunk.fChunk)
            dataChunk = dataChunk.fPreviousChunk;
        return dataChunk;
    }
    //
    //
    //
    private final void addRef() {
        fRefCount++;
//System.err.println(">>[" + fChunk + "] " + (fRefCount - 1) + " -> " + fRefCount);
    }
    //
    //
    //
    private final void removeRef() {
        fRefCount--;
//System.err.println("<<[" + fChunk + "] " + (fRefCount + 1) + " -> " + fRefCount);
        if (fRefCount == 0) {
//System.err.println("[" + fChunk + "] recycled a " + fData.length + " character array");
            fStringPool = null;
            fChunk = -1;
//            fData = null;
            fPreviousChunk = null;
            synchronized (UTF8DataChunk.class) {
                /*** Only keep one free chunk at a time! ***
                fNextChunk = fgFreeChunks;
                /***/
                fNextChunk = null;
                fgFreeChunks = this;
            }
        }
    }
    //
    //
    //
    private void setNextChunk(UTF8DataChunk nextChunk) {
        if (nextChunk == null) {
            if (fNextChunk != null)
                fNextChunk.removeRef();
        } else if (fNextChunk == null) {
            nextChunk.addRef();
        } else
            throw new RuntimeException("UTF8DataChunk::setNextChunk");
        fNextChunk = nextChunk;
    }
    //
    //
    //
    private StringPool fStringPool;
    private int fRefCount;
    private int fChunk;
    private byte[] fData = null;
    private UTF8DataChunk fNextChunk;
    private UTF8DataChunk fPreviousChunk;
    private static UTF8DataChunk fgFreeChunks = null;
    private char[] fTempBuffer = null;
}
