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
 *
 * @version
 */
public final class ChunkyCharArray implements XMLEntityHandler.CharBuffer {

    /**
     * Constructor
     */
    public ChunkyCharArray(StringPool stringPool) {
        fStringPool = stringPool;
        fCurrentChunk = CharDataChunk.createChunk(stringPool, null);
    }

    /**
     *
     */
    public int length() {
        return fLength;
    }

    /**
     *
     */
    public void append(char ch) {
        try {
            fCurrentData[fCurrentIndex] = ch;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (fCurrentIndex == CharDataChunk.CHUNK_SIZE) {
                fCurrentChunk = CharDataChunk.createChunk(fStringPool, fCurrentChunk);
                fCurrentData = new char[INITIAL_CHUNK_SIZE];
                fCurrentIndex = 0;
            } else {
                char[] newData = new char[fCurrentIndex * 2];
                System.arraycopy(fCurrentData, 0, newData, 0, fCurrentIndex);
                fCurrentData = newData;
            }
            fCurrentChunk.setCharArray(fCurrentData);
            fCurrentData[fCurrentIndex] = ch;
        } catch (NullPointerException ex) {
            fCurrentData = new char[INITIAL_CHUNK_SIZE];
            fCurrentChunk.setCharArray(fCurrentData);
            fCurrentData[fCurrentIndex] = ch;
        }
        fCurrentIndex++;
        fLength++;
    }

    /**
     * Append a <code>String</code> to this buffer
     *
     * @param s the string to append.
     */
    public void append(String s) {
        int slen = s.length();
        for (int i = 0; i < slen; i++)
            append(s.charAt(i));
    }

    /**
     *
     */
    public void append(char[] ch, int offset, int length) {
        while (length-- > 0)
            append(ch[offset++]);
    }

    /**
     * Append a <code>ChunkyCharArray</code> to this buffer.
     *
     * @param charArray buffer to be appended.
     * @param offset The offset within charArray of the first character.
     * @param length The number of characters to append.
     */
    public void append(ChunkyCharArray charArray, int offset, int length) {
        fCurrentChunk.append(charArray, offset, length);
    }

    /**
     *
     */
    public int addString(int offset, int length) {
        if (length == 0)
            return 0;
        return fCurrentChunk.addString(offset, length);
    }

    /**
     * Add a region of this buffer to the <code>StringPool</code> as a symbol
     * 
     * @param offset The offset within this buffer of the first character of the string
     * @param length The number of characters in the symbol
     */
    public int addSymbol(int offset, int length) {
        if (length == 0)
            return 0;
        return fCurrentChunk.addSymbol(offset, length, 0);
    }

    //
    // Chunk size constants
    //
    private static final int INITIAL_CHUNK_SHIFT = 7;        // 2^7 = 128
    private static final int INITIAL_CHUNK_SIZE = (1 << INITIAL_CHUNK_SHIFT);

    //
    // Instance variables
    //
    private StringPool fStringPool = null;
    private CharDataChunk fCurrentChunk = null;
    private char[] fCurrentData = null;
    private int fCurrentIndex = 0;
    private int fLength = 0;
}
