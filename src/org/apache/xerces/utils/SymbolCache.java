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

/**
 *
 * @version
 */
public final class SymbolCache {
    //
    // Symbol Cache
    //
    public static final int CHAR_OFFSET = 0;
    public static final int INDEX_OFFSET = 1;
    public static final int NEXT_OFFSET = 2;
    public static final int CACHE_RECORD_SIZE = 3;
    //
    public static final int INITIAL_CACHE_RECORD_COUNT = 4; // start with 4 entries per level
    //
    public char[] fSymbolChars = new char[8192];
    public int fSymbolCharsOffset = 0;
    public int[][] fCacheLines = new int[8][];
    public int fCacheLineCount = 0;
    //
    //
    //
    public SymbolCache() {
        fCacheLines[fCacheLineCount++] = new int[1+(INITIAL_CACHE_RECORD_COUNT*CACHE_RECORD_SIZE)];
    }
    public void reset() {
        fSymbolCharsOffset = 0;
        fCacheLineCount = 0;
        fCacheLines[fCacheLineCount++] = new int[1+(INITIAL_CACHE_RECORD_COUNT*CACHE_RECORD_SIZE)];
    }
    //
    //
    //
    public char[] getSymbolChars() {
        return fSymbolChars;
    }
    //
    // Symbol interfaces
    //
    public String createSymbol(int symbolHandle, int startOffset, int entry, int[] entries, int offset) {
        int slen = fSymbolCharsOffset - startOffset;
        String str = new String(fSymbolChars, startOffset, slen);
        try {
            entries[offset + SymbolCache.INDEX_OFFSET] = symbolHandle;
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new RuntimeException("untested 1");
        }
        return str;
    }
    public int addSymbolToCache(String str, int slen, int symbolHandle) {
        int charsOffset = fSymbolCharsOffset;
        if (slen == 0) // EMPTY_STRING cannot be a legal "symbol"
            return charsOffset;
        int strIndex = 0;
        char ch = str.charAt(strIndex++);
        try {
            fSymbolChars[fSymbolCharsOffset] = ch;
        } catch (ArrayIndexOutOfBoundsException ex) {
            char[] newChars = new char[fSymbolChars.length * 2];
            System.arraycopy(fSymbolChars, 0, newChars, 0, fSymbolChars.length);
            fSymbolChars = newChars;
            fSymbolChars[fSymbolCharsOffset] = ch;
        }
        fSymbolCharsOffset++;
        int entry = 0;
        int[] entries = fCacheLines[entry];
        int count = entries[0];
        int i = 0;
        int offset = 1;
        while (true) {
            if (i == count)
                break;
            if (entries[offset + CHAR_OFFSET] != ch) {
                i++;
                offset += CACHE_RECORD_SIZE;
                continue;
            }
            if (strIndex == slen) {
                if (entries[offset + INDEX_OFFSET] != -1) {
                    // How did we miss this during lookup ?
                    throw new RuntimeException("addSymbolToCache");
                }
                entries[offset + INDEX_OFFSET] = symbolHandle;
                return charsOffset;
            }
            ch = str.charAt(strIndex++);
            try {
                fSymbolChars[fSymbolCharsOffset] = ch;
            } catch (ArrayIndexOutOfBoundsException ex) {
                char[] newChars = new char[fSymbolChars.length * 2];
                System.arraycopy(fSymbolChars, 0, newChars, 0, fSymbolChars.length);
                fSymbolChars = newChars;
                fSymbolChars[fSymbolCharsOffset] = ch;
            }
            fSymbolCharsOffset++;
            entry = entries[offset + NEXT_OFFSET];
            try {
                entries = fCacheLines[entry];
            } catch (ArrayIndexOutOfBoundsException ex) {
                if (entry == -1) {
                    entry = fCacheLineCount++;
                    entries[offset + NEXT_OFFSET] = entry;
                    entries = new int[1+(INITIAL_CACHE_RECORD_COUNT*CACHE_RECORD_SIZE)];
                    try {
                        fCacheLines[entry] = entries;
                    } catch (ArrayIndexOutOfBoundsException ex2) {
                        int[][] newCache = new int[entry * 2][];
                        System.arraycopy(fCacheLines, 0, newCache, 0, entry);
                        fCacheLines = newCache;
                        fCacheLines[entry] = entries;
                    }
                } else {
                    entries = fCacheLines[entry];
                    throw new RuntimeException("untested 2");
                }
            }
            count = entries[0];
            i = 0;
            offset = 1;
        }
        while (true) {
            entries[0]++;
            try {
                entries[offset + CHAR_OFFSET] = ch;
            } catch (ArrayIndexOutOfBoundsException ex) {
                int newSize = 1 + ((offset - 1) * 2);
                int[] newEntries = new int[newSize];
                System.arraycopy(entries, 0, newEntries, 0, offset);
                fCacheLines[entry] = entries = newEntries;
                entries[offset + CHAR_OFFSET] = ch;
            }
            if (strIndex == slen) {
                entries[offset + INDEX_OFFSET] = symbolHandle;
                entries[offset + NEXT_OFFSET] = -1;
                break;
            }
            entry = fCacheLineCount++;
            entries[offset + INDEX_OFFSET] = -1;
            entries[offset + NEXT_OFFSET] = entry;
            entries = new int[1+(INITIAL_CACHE_RECORD_COUNT*CACHE_RECORD_SIZE)];
            try {
                fCacheLines[entry] = entries;
            } catch (ArrayIndexOutOfBoundsException ex) {
                int[][] newCache = new int[entry * 2][];
                System.arraycopy(fCacheLines, 0, newCache, 0, entry);
                fCacheLines = newCache;
                fCacheLines[entry] = entries;
            }
            offset = 1;
            ch = str.charAt(strIndex++);
            try {
                fSymbolChars[fSymbolCharsOffset] = ch;
            } catch (ArrayIndexOutOfBoundsException ex) {
                char[] newChars = new char[fSymbolChars.length * 2];
                System.arraycopy(fSymbolChars, 0, newChars, 0, fSymbolChars.length);
                fSymbolChars = newChars;
                fSymbolChars[fSymbolCharsOffset] = ch;
            }
            fSymbolCharsOffset++;
        }
        return charsOffset;
    }
    public void updateCacheLine(int charsOffset, int totalMisses, int length) {
//System.err.println("found symbol " + toString(symbolIndex) + " after " + totalMisses + " total misses (" + (totalMisses/length) + " misses per character).");
        int entry = 0;
        int[] entries = fCacheLines[0];
        int ch = fSymbolChars[charsOffset++];
        int count = entries[0];
        int offset = 1 + ((count - 1) * CACHE_RECORD_SIZE);
        int misses = 0;
        while (true) {
            if (ch != entries[offset + CHAR_OFFSET]) {
                offset -= CACHE_RECORD_SIZE;
                misses++;
                continue;
            }
            if (misses > 4) {
                int symIndex = entries[offset + INDEX_OFFSET];
                int nextIndex = entries[offset + NEXT_OFFSET];
                System.arraycopy(entries, offset + CACHE_RECORD_SIZE, entries, offset, misses * CACHE_RECORD_SIZE);
                offset = 1 + ((count - 1) * CACHE_RECORD_SIZE);
                entries[offset + CHAR_OFFSET] = ch;
                entries[offset + INDEX_OFFSET] = symIndex;
                entries[offset + NEXT_OFFSET] = nextIndex;
            }
            if (--length == 0)
                break;
            entry = entries[offset + NEXT_OFFSET];
            entries = fCacheLines[entry];
            ch = fSymbolChars[charsOffset++];
            count = entries[0];
            offset = 1 + ((count - 1) * CACHE_RECORD_SIZE);
            misses = 0;
        }
    }
}
