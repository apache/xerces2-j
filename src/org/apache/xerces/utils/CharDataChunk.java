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

/**
 * This class provides the character buffers used by some of the
 * reader classes.  The instances of this class are reference
 * counted and placed upon a free list for reallocation when no
 * longer in use so that they are reclaimed faster and with less
 * overhead than using the garbage collector.
 *
 * @version
 */
public final class CharDataChunk implements StringPool.StringProducer {
    /**
     * Chunk size constants
     *
     * The reader classes use the chunk size directly for better performance.
     */
    public static final int CHUNK_SHIFT = 14;           // 2^14 = 16k
    public static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    public static final int CHUNK_MASK = CHUNK_SIZE - 1;
    /**
     * Public constructor (factory)
     *
     * If there are any free instances available, remove them from the
     * free list and reinitialize them.  If not, allocate a new one.
     *
     * @param stringPool The string pool.
     * @param prev The chunk that precedes this one, or null if this is
     *             the first chunk.
     * @return The instance reused or created.
     */
    public static CharDataChunk createChunk(StringPool stringPool, CharDataChunk prev) {

        CharDataChunk newChunk = null;
        synchronized (CharDataChunk.class) {
            newChunk = fgFreeChunks;
            if (newChunk != null) {
                fgFreeChunks = newChunk.fNextChunk;
            } else {
                newChunk = new CharDataChunk();
            }
        }
        newChunk.fStringPool = stringPool;
        newChunk.fRefCount = 1;             // account for the reference we return to the caller
        newChunk.fChunk = prev == null ? 0 : prev.fChunk + 1;
        newChunk.fNextChunk = null;
        newChunk.fPreviousChunk = prev;
        if (prev != null) {
            //
            // You might think that we should call prev.addRef() here,
            // and you would normally be correct.  However, the reader
            // that calls us is doing something like this:
            //
            //      fCurrentChunk = CharDataChunk.createChunk(fStringPool, fCurrentChunk);
            //
            // During this call, the fCurrentChunk changes from the
            // previous chunk to this chunk, losing the reference to
            // the previous chunk.  To avoid needing code like this:
            //
            //      CharDataChunk prevChunk = fCurrentChunk;
            //      fCurrentChunk = CharDataChunk.createChunk(fStringPool, prevChunk);
            //      prevChunk.releaseChunk();
            //
            // We "adopt the reference" to the previous chunk into our
            // fPreviousChunk field, since the addRef() followed by a
            // removeRef() from the caller after we return just cancel
            // each other out.  The previous chunk reference will go
            // away later when clearPreviousChunk is called.
            //
            prev.setNextChunk(newChunk);
        }
        return newChunk;
    }
    /**
     * Return the instance that contains the specified offset.
     *
     * This method must always be invoked on an instance that
     * contains the specified offset, or an instance the contains
     * an offset greater than, i.e. after, the instance we are
     * to return.
     *
     * @param offset The offset to find.
     * @return The instance containing the offset.
     */
    public CharDataChunk chunkFor(int offset) {
        int firstChunk = offset >> CHUNK_SHIFT;
        if (firstChunk == fChunk)
            return this;
        CharDataChunk dataChunk = fPreviousChunk;
        while (firstChunk != dataChunk.fChunk)
            dataChunk = dataChunk.fPreviousChunk;
        return dataChunk;
    }
    /**
     * Get the character array of this instance.
     *
     * The reader classes access the data of each instance directly.
     * This class only exists to manage the lifetime of the references
     * to each instance.  It is not intended to hide from the readers
     * the fact that each instance contains a buffer of character data.
     *
     * @return The character data.
     */
    public char[] toCharArray() {
        return fData;
    }
    /**
     * Set the character array for this instance.
     *
     * @param data The character data.
     */
    public void setCharArray(char[] data) {
        fData = data;
    }
    /**
     * Get the next chunk.
     *
     * @return The instance that follows this one in the list of chunks,
     *         or null if there is no such instance.
     */
    public CharDataChunk nextChunk() {
        return fNextChunk;
    }
    /**
     * Clean the previous chunk reference.
     *
     * When a reader has reached a point where it knows that it will no
     * longer call the addString, addSymbol, or append methods with an
     * offset that is contained within a chunk that precedes this one,
     * it will call this method to clear the reference from this chunk to
     * the one preceding it.  This allows the references between chunks
     * to be dropped as we go and allow the unused instances to be placed
     * upon the free list for reuse.
     *
     * @return <code>true</code> if we cleared the previous chunk pointer;
     *         otherwise <code>false</code> if the pointer is already null.
     */
    public boolean clearPreviousChunk() {
        if (fPreviousChunk != null) {
            fPreviousChunk.clearNextChunk();
            fPreviousChunk.removeRef();
            fPreviousChunk = null;
            return true;
        }
        return false;
    }
    /**
     * Release the reference to this chunk held by the reader that allocated
     * this instance.  Called at end of input to release the last chunk in the
     * list used by the reader.
     */
    public void releaseChunk() {
        removeRef();
    }
    /**
     * Add a range from this chunk to the <code>StringPool</code>
     *
     * @param offset the offset of the first character to be added
     * @param length the number of characters to add
     * @return the <code>StringPool</code> handle that was added.
     */
    public int addString(int offset, int length) {
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
    /**
     * Add a range from this chunk to the <code>StringPool</code> as a symbol
     *
     * @param offset the offset of the first character to be added
     * @param length the number of characters to add
     * @param hashcode hashcode to match to ensure uniqueness
     * @return the <code>StringPool</code> handle that was added.
     */
    public int addSymbol(int offset, int length, int hashcode) {
        int chunk = offset >> CHUNK_SHIFT;
        if (chunk != fChunk) {
            if (fPreviousChunk == null)
                throw new RuntimeException(new ImplementationMessages().createMessage(null, ImplementationMessages.INT_PCN, 0, null));
            return fPreviousChunk.addSymbol(offset, length, hashcode);
        }
        int lastChunk = (offset + length - 1) >> CHUNK_SHIFT;
        int index = offset & CHUNK_MASK;
        if (chunk == lastChunk) {
            if (hashcode == 0)
                hashcode = StringHasher.hashChars(fData, index, length);
            int symbol = fStringPool.lookupSymbol(this, offset & CHUNK_MASK, length, hashcode);
            if (symbol == -1) {
                String str = toString(offset & CHUNK_MASK, length);
                symbol = fStringPool.addNewSymbol(str, hashcode);
            }
            return symbol;
        }
        String str = toString(offset & CHUNK_MASK, length);
        return fStringPool.addSymbol(str);
    }
    /**
     * Append data from a <code>CharBuffer</code> to this chunk.
     *
     * @param charBuffer the buffer to be appended.
     * @param offset the offset of the first character to be appended.
     * @param length the number of characters to append.
     */
    public void append(XMLEntityHandler.CharBuffer charBuffer, int offset, int length) {
        //
        // Setup for the operation.
        //
        CharDataChunk dataChunk = chunkFor(offset);
        int index = offset & CHUNK_MASK;
        int nbytes = (index + length <= CHUNK_SIZE) ? length : CHUNK_SIZE - index;
        //
        // Visit each Chunk in turn until we are done.
        //
        while (true) {
            charBuffer.append(dataChunk.fData, index, nbytes);
            length -= nbytes;
            if (length == 0)
                break;
            dataChunk = dataChunk.fNextChunk;
            index = 0;
            nbytes = length <= CHUNK_SIZE ? length : CHUNK_SIZE;
        }
    }
    //
    // StringProducer interfaces
    //
    /**
     * Return a range of characters as a <code>String</code>.
     *
     * @param offset the offset of the first character to convert.
     * @param length the number of characters to convert.
     * @return the <code>String</code>
     */
    public String toString(int offset, int length) {
        if (offset + length <= CHUNK_SIZE) {
            //
            // All the chars are in the same chunk
            //
            return new String(fData, offset, length);
        }
        //
        // The data is spread across chunks, so we need to build it in pieces.
        //
        StringBuffer sb = new StringBuffer(length);
        //
        // Copy the partial data from the first chunk.
        //
        int nbytes = CHUNK_SIZE - offset;
        sb.append(fData, offset, nbytes);
        length -= nbytes;
        //
        // Use each chunk in turn until we are done.
        //
        CharDataChunk aChunk = fNextChunk;
        do {
            nbytes = length <= CHUNK_SIZE ? length : CHUNK_SIZE;
            sb.append(aChunk.fData, 0, nbytes);
            length -= nbytes;
            aChunk = aChunk.fNextChunk;
        } while (length > 0);
        String retval = sb.toString();
        sb = null; // REVISIT - does this help gc ?
        return retval;
    }
    /**
     * Release a string from this chunk
     *
     * @param offset the offset of the first character to be released
     * @param length the number of characters to release.
     */
    public void releaseString(int offset, int length) {
        removeRef();
    }
    /**
     * Compare a range in this chunk and a range in a character array for equality
     *
     * @param offset the offset of the first character in the range in this chunk
     * @param length the number of characters in the range to compare
     * @param strChars the character array to compare
     * @param strOffset the offset of the first character in the range in strChars
     * @param strLength the number of characters to release.
     * @return true if the ranges are character-wise equal, otherwise false.
     */
    public boolean equalsString(int offset, int length, char[] strChars, int strOffset, int strLength) {
        if (length != strLength)
            return false;
        if (offset + length <= CHUNK_SIZE) {
            //
            // All the chars are in the same chunk
            //
            for (int i = 0; i < length; i++) {
                if (fData[offset++] != strChars[strOffset++])
                    return false;
            }
            return true;
        }
        //
        // Compare the partial data from the first chunk.
        //
        int nbytes = CHUNK_SIZE - offset;
        length -= nbytes;
        while (nbytes-- > 0) {
            if (fData[offset++] != strChars[strOffset++])
                return false;
        }
        //
        // Check each chunk in turn until we are done.
        //
        CharDataChunk aChunk = fNextChunk;
        do {
            offset = 0;
            nbytes = length <= CHUNK_SIZE ? length : CHUNK_SIZE;
            length -= nbytes;
            while (nbytes-- > 0) {
                if (aChunk.fData[offset++] != strChars[strOffset++])
                    return false;
            }
            aChunk = aChunk.fNextChunk;
        } while (length > 0);
        return true;
    }
    //
    // Private methods
    //

    //
    // Constructor for factory method.
    //
    private CharDataChunk() {}
    //
    //
    //
    private void addRef() {
        fRefCount++;
    }
    //
    //
    //
    private void removeRef() {
        fRefCount--;
        if (fRefCount == 0) {
            fStringPool = null;
            fChunk = -1;
            fPreviousChunk = null;
            synchronized (CharDataChunk.class) {
                /*** Only keep one free chunk at a time! ***
                fNextChunk = fgFreeChunks;
                /***/
                fgFreeChunks = this;
            }
        }
    }
    //
    //
    //
    private void clearNextChunk() {
        if (fNextChunk != null)
            fNextChunk.removeRef();
        fNextChunk = null;
    }
    //
    //
    //
    private void setNextChunk(CharDataChunk nextChunk) {
        if (fNextChunk != null) {
            throw new RuntimeException("CharDataChunk::setNextChunk");
        }
        nextChunk.addRef();
        fNextChunk = nextChunk;
    }
    //
    // Private instance variables.
    //
    private StringPool fStringPool;
    private int fRefCount;
    private int fChunk;
    private char[] fData = null;
    private CharDataChunk fNextChunk;
    private CharDataChunk fPreviousChunk;
    private static CharDataChunk fgFreeChunks = null;
}
