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
public final class StringPool {
    //
    // Debugging
    //
    private static final boolean DEBUG_ADDITIONS = false;
    /**
     * Constants
     */
    public static final int NULL_STRING = -1;   // null
    public static final int EMPTY_STRING = 0;   // ""
    /**
     *
     */
    public interface StringProducer {
        /**
         *
         */
        public String toString(int offset, int length);
        /**
         *
         */
        public void releaseString(int offset, int length);
        /**
         *
         */
        public boolean equalsString(int offset, int length, char[] strChars, int strOffset, int strLength);
    };
    //
    // Chunk size constants
    //
    private static final int INITIAL_CHUNK_SHIFT = 8;    // 2^8 = 256
    private static final int INITIAL_CHUNK_SIZE = (1 << INITIAL_CHUNK_SHIFT);
    private static final int CHUNK_SHIFT = 13;           // 2^13 = 8k
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (16 - CHUNK_SHIFT));   // 2^16 = 64k
    //
    // Instance variables
    //
    //
    // String and Symbol arrays
    //
    private int fStringCount = 0;
    private int fStringFreeList = -1;
    private String[][] fString = new String[INITIAL_CHUNK_COUNT][];
    private StringPool.StringProducer[][] fStringProducer = new StringPool.StringProducer[INITIAL_CHUNK_COUNT][];
    private int[][] fOffset = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fLength = new int[INITIAL_CHUNK_COUNT][];
    private int[][] fCharsOffset = new int[INITIAL_CHUNK_COUNT][];
    //
    // String Lists
    //
    private int fStringListCount = 0;
    private int fActiveStringList = -1;
    private int[][] fStringList = new int[INITIAL_CHUNK_COUNT][];
    //
    // Symbol Hashtable
    //
    private static final int INITIAL_BUCKET_SIZE = 4;
    private static final int HASHTABLE_SIZE = 128;
    private int[][] fSymbolTable = new int[HASHTABLE_SIZE][];
    //
    // Symbol Cache
    //
    private SymbolCache fSymbolCache = null;
    //
    //
    //
    public StringPool() {
        fSymbolCache = new SymbolCache();
        if (addSymbol("") != EMPTY_STRING)
            throw new RuntimeException("UTL002 cannot happen");
    }
    //
    //
    //
    public void reset() {
        int chunk = 0;
        int index = 0;
        for (int i = 0; i < fStringCount; i++) {
            fString[chunk][index] = null;
            if (fStringProducer[chunk][index] != null)
                fStringProducer[chunk][index].releaseString(fOffset[chunk][index], fLength[chunk][index]);
            fStringProducer[chunk][index] = null;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        for (int i = 0; i < HASHTABLE_SIZE; i++)
            fSymbolTable[i] = null;
        fStringCount = 0;
        fStringFreeList = -1;
        fStringListCount = 0;
        fActiveStringList = -1;
        fSymbolCache.reset();
        fShuffleCount = 0;
        if (addSymbol("") != EMPTY_STRING)
            throw new RuntimeException("UTL002 cannot happen");
    }
    //
    // String interfaces
    //
    private boolean ensureCapacity(int chunk, int index) {
        try {
            return fOffset[chunk][index] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (index == 0) {
                String[][] newString = new String[chunk * 2][];
                System.arraycopy(fString, 0, newString, 0, chunk);
                fString = newString;
                StringPool.StringProducer[][] newProducer = new StringPool.StringProducer[chunk * 2][];
                System.arraycopy(fStringProducer, 0, newProducer, 0, chunk);
                fStringProducer = newProducer;
                int[][] newInt = new int[chunk * 2][];
                System.arraycopy(fOffset, 0, newInt, 0, chunk);
                fOffset = newInt;
                newInt = new int[chunk * 2][];
                System.arraycopy(fLength, 0, newInt, 0, chunk);
                fLength = newInt;
                newInt = new int[chunk * 2][];
                System.arraycopy(fCharsOffset, 0, newInt, 0, chunk);
                fCharsOffset = newInt;
            } else {
                String[] newString = new String[index * 2];
                System.arraycopy(fString[chunk], 0, newString, 0, index);
                fString[chunk] = newString;
                StringPool.StringProducer[] newProducer = new StringPool.StringProducer[index * 2];
                System.arraycopy(fStringProducer[chunk], 0, newProducer, 0, index);
                fStringProducer[chunk] = newProducer;
                int[] newInt = new int[index * 2];
                System.arraycopy(fOffset[chunk], 0, newInt, 0, index);
                fOffset[chunk] = newInt;
                newInt = new int[index * 2];
                System.arraycopy(fLength[chunk], 0, newInt, 0, index);
                fLength[chunk] = newInt;
                newInt = new int[index * 2];
                System.arraycopy(fCharsOffset[chunk], 0, newInt, 0, index);
                fCharsOffset[chunk] = newInt;
                return true;
            }
        } catch (NullPointerException ex) {
        }
        fString[chunk] = new String[INITIAL_CHUNK_SIZE];
        fStringProducer[chunk] = new StringPool.StringProducer[INITIAL_CHUNK_SIZE];
        fOffset[chunk] = new int[INITIAL_CHUNK_SIZE];
        fLength[chunk] = new int[INITIAL_CHUNK_SIZE];
        fCharsOffset[chunk] = new int[INITIAL_CHUNK_SIZE];
        return true;
    }
    public int addString(String str) {
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        fString[chunk][index] = str;
        fStringProducer[chunk][index] = null;
        fOffset[chunk][index] = 0;
        fLength[chunk][index] = str.length();
        fCharsOffset[chunk][index] = -1;
        if (DEBUG_ADDITIONS)
            System.err.println("addString(" + str + ") " + stringIndex);
        return stringIndex;
    }
    public int addString(StringPool.StringProducer stringProducer, int offset, int length)
    {
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        fString[chunk][index] = null;
        fStringProducer[chunk][index] = stringProducer;
        fOffset[chunk][index] = offset;
        fLength[chunk][index] = length;
        fCharsOffset[chunk][index] = -1;
        if (DEBUG_ADDITIONS)
            System.err.println("addString(" + stringProducer.toString(offset, length) + ") " + stringIndex);
        return stringIndex;
    }
    //
    // Symbol interfaces
    //
    public SymbolCache getSymbolCache() {
        return fSymbolCache;
    }
    //private static int fShuffleCount = 0;
    private int fShuffleCount = 0;
    public void resetShuffleCount() {
        fShuffleCount = 0;
    }
    public void updateCacheLine(int symbolIndex, int totalMisses, int length) {
//System.err.println("found symbol " + toString(symbolIndex) + " after " + totalMisses + " total misses (" + (totalMisses/length) + " misses per character).");
        if (++fShuffleCount > 200) {
//            if (fShuffleCount == 201) System.out.println("Stopped shuffling...");
            return;
        }
//        if ((fShuffleCount % 10) == 0) System.out.println("Shuffling pass " + fShuffleCount + " ...");
        int chunk = symbolIndex >> CHUNK_SHIFT;
        int index = symbolIndex & CHUNK_MASK;
        int charsOffset = fCharsOffset[chunk][index];
        fSymbolCache.updateCacheLine(charsOffset, totalMisses, length);
    }
    public int createNonMatchingSymbol(int startOffset,
                                       int entry,
                                       int[] entries,
                                       int offset) throws Exception
    {
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        String str = fSymbolCache.createSymbol(stringIndex, startOffset, entry, entries, offset);
        int slen = str.length();
        fString[chunk][index] = str;
        fStringProducer[chunk][index] = null;
        fOffset[chunk][index] = -1;
        fLength[chunk][index] = slen;
        fCharsOffset[chunk][index] = startOffset;

        int hashcode = StringHasher.hashString(str, slen);
        int hc = hashcode % HASHTABLE_SIZE;
        int[] bucket = fSymbolTable[hc];
        hashSymbol(bucket, hashcode, chunk, index);
        if (DEBUG_ADDITIONS)
            System.err.println("addSymbolNew(" + str + ") " + stringIndex);
        return stringIndex;
    }
    private void hashSymbol(int[] bucket, int hashcode, int chunk, int index) {
        if (bucket == null) {
            bucket = new int[1 + (INITIAL_BUCKET_SIZE * 3)];
            bucket[0] = 1;
            bucket[1] = hashcode;
            bucket[2] = chunk;
            bucket[3] = index;
            int hc = hashcode % HASHTABLE_SIZE;
            fSymbolTable[hc] = bucket;
        } else {
            int count = bucket[0];
            int offset = 1 + (count * 3);
            if (offset == bucket.length) {
                int newSize = count + INITIAL_BUCKET_SIZE;
                int[] newBucket = new int[1 + (newSize * 3)];
                System.arraycopy(bucket, 0, newBucket, 0, offset);
                bucket = newBucket;
                int hc = hashcode % HASHTABLE_SIZE;
                fSymbolTable[hc] = bucket;
            }
            bucket[offset++] = hashcode;
            bucket[offset++] = chunk;
            bucket[offset++] = index;
            bucket[0] = ++count;
        }
    }
    public int addSymbol(String str) {
        int slen = str.length();
        int hashcode = StringHasher.hashString(str, slen);
        int hc = hashcode % HASHTABLE_SIZE;
        int[] bucket = fSymbolTable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == hashcode) {
                    int chunk = bucket[j+1];
                    int index = bucket[j+2];
                    if (slen == fLength[chunk][index]) {
                        int symoff = fCharsOffset[chunk][index];
                        boolean match = true;
                        char[] symbolChars = fSymbolCache.getSymbolChars();
                        for (int k = 0; k < slen; k++) {
                            if (symbolChars[symoff++] != str.charAt(k)) {
                                match = false;
                                break;
                            }
                        }
                        if (match) {
                            return (chunk << CHUNK_SHIFT) + index;
                        }
                    }
                }
                j += 3;
            }
        }
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        fString[chunk][index] = str;
        fStringProducer[chunk][index] = null;
        fOffset[chunk][index] = -1;
        fLength[chunk][index] = slen;
        fCharsOffset[chunk][index] = fSymbolCache.addSymbolToCache(str, slen, stringIndex);

        hashSymbol(bucket, hashcode, chunk, index);
        if (DEBUG_ADDITIONS)
            System.err.println("addSymbolNew(" + str + ") " + stringIndex);
        return stringIndex;
    }
    public int addSymbol(StringPool.StringProducer stringProducer, int offset, int length, int hashcode) {
        int hc = hashcode % HASHTABLE_SIZE;
        int[] bucket = fSymbolTable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == hashcode) {
                    int chunk = bucket[j+1];
                    int index = bucket[j+2];
                    char[] symbolChars = fSymbolCache.getSymbolChars();
                    if (stringProducer.equalsString(offset, length, symbolChars, fCharsOffset[chunk][index], fLength[chunk][index])) {
                        stringProducer.releaseString(offset, length);
                        return (chunk << CHUNK_SHIFT) + index;
                    }
                }
                j += 3;
            }
        }
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        String str = stringProducer.toString(offset, length);
        stringProducer.releaseString(offset, length);
        int slen = str.length();
        fString[chunk][index] = str;
        fStringProducer[chunk][index] = null;
        fOffset[chunk][index] = -1;
        fLength[chunk][index] = slen;
        fCharsOffset[chunk][index] = fSymbolCache.addSymbolToCache(str, slen, stringIndex);

        hashSymbol(bucket, hashcode, chunk, index);
        if (DEBUG_ADDITIONS)
            System.err.println("addSymbol(" + str + ") " + stringIndex);
        return stringIndex;
    }
    public int lookupSymbol(StringPool.StringProducer stringProducer, int offset, int length, int hashcode) {
        int hc = hashcode % HASHTABLE_SIZE;
        int[] bucket = fSymbolTable[hc];
        if (bucket != null) {
            int j = 1;
            for (int i = 0; i < bucket[0]; i++) {
                if (bucket[j] == hashcode) {
                    int chunk = bucket[j+1];
                    int index = bucket[j+2];
                    char[] symbolChars = fSymbolCache.getSymbolChars();
                    if (stringProducer.equalsString(offset, length, symbolChars, fCharsOffset[chunk][index], fLength[chunk][index])) {
                        return (chunk << CHUNK_SHIFT) + index;
                    }
                }
                j += 3;
            }
        }
        return -1;
    }
    public int addNewSymbol(String str, int hashcode) {
        int hc = hashcode % HASHTABLE_SIZE;
        int[] bucket = fSymbolTable[hc];
        int chunk;
        int index;
        int stringIndex;
        if (fStringFreeList != -1) {
            stringIndex = fStringFreeList;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            fStringFreeList = fOffset[chunk][index];
        } else {
            stringIndex = fStringCount++;
            chunk = stringIndex >> CHUNK_SHIFT;
            index = stringIndex & CHUNK_MASK;
            ensureCapacity(chunk, index);
        }
        int slen = str.length();
        fString[chunk][index] = str;
        fStringProducer[chunk][index] = null;
        fOffset[chunk][index] = -1;
        fLength[chunk][index] = slen;
        fCharsOffset[chunk][index] = fSymbolCache.addSymbolToCache(str, slen, stringIndex);

        hashSymbol(bucket, hashcode, chunk, index);
        if (DEBUG_ADDITIONS)
            System.err.println("addSymbolNew(" + str + ") " + stringIndex);
        return stringIndex;
    }
    public int addSymbol(int stringIndex) {
        if (stringIndex < 0 || stringIndex >= fStringCount)
            return -1;
        int chunk = stringIndex >> CHUNK_SHIFT;
        int index = stringIndex & CHUNK_MASK;
        if (fOffset[chunk][index] == -1)
            return stringIndex;
        String s = fString[chunk][index];
        if (s == null) {
            s = fStringProducer[chunk][index].toString(fOffset[chunk][index], fLength[chunk][index]);
            fStringProducer[chunk][index].releaseString(fOffset[chunk][index], fLength[chunk][index]);
            fString[chunk][index] = s;
            fStringProducer[chunk][index] = null;
        }
        return addSymbol(s);
    }
    //
    // Get characters for defined symbols
    //
    public class CharArrayRange {
        public char[] chars;
        public int offset;
        public int length;
    }
    public CharArrayRange createCharArrayRange() {
        return new CharArrayRange();
    }
    public void getCharArrayRange(int symbolIndex, CharArrayRange r) {
        if (symbolIndex < 0 || symbolIndex >= fStringCount) {
            r.chars = null;
            r.offset = -1;
            r.length = -1;
            return;
        }
        int chunk = symbolIndex >> CHUNK_SHIFT;
        int index = symbolIndex & CHUNK_MASK;
        r.chars = fSymbolCache.getSymbolChars();
        r.offset = fCharsOffset[chunk][index];
        r.length = fLength[chunk][index];
    }
    public boolean equalNames(int stringIndex1, int stringIndex2) {
        if (stringIndex1 == stringIndex2)
            return true;
        return false;
    }
    //
    // String list support
    //
    private boolean ensureListCapacity(int chunk, int index) {
        try {
            return fStringList[chunk][index] == 0;
        } catch (ArrayIndexOutOfBoundsException ex) {
            if (index == 0) {
                int[][] newInt = new int[chunk * 2][];
                System.arraycopy(fStringList, 0, newInt, 0, chunk);
                fStringList = newInt;
            } else {
                int[] newInt = new int[index * 2];
                System.arraycopy(fStringList[chunk], 0, newInt, 0, index);
                fStringList[chunk] = newInt;
                return true;
            }
        } catch (NullPointerException ex) {
        }
        fStringList[chunk] = new int[INITIAL_CHUNK_SIZE];
        return true;
    }
    public int startStringList() {
        fActiveStringList = fStringListCount;
        return fStringListCount;
    }
    public boolean addStringToList(int stringListIndex, int stringIndex) {
        if (stringIndex == -1 || stringListIndex != fActiveStringList)
            return false;
        int chunk = fStringListCount >> CHUNK_SHIFT;
        int index = fStringListCount & CHUNK_MASK;
        ensureListCapacity(chunk, index);
        fStringList[chunk][index] = stringIndex;
        fStringListCount++;
        return true;
    }
    public void finishStringList(int stringListIndex) {
        if (stringListIndex != fActiveStringList)
            return;
        int chunk = fStringListCount >> CHUNK_SHIFT;
        int index = fStringListCount & CHUNK_MASK;
        ensureListCapacity(chunk, index);
        fStringList[chunk][index] = -1;
        fActiveStringList = -1;
        fStringListCount++;
    }
    public int stringListLength(int stringListIndex) {
        int chunk = stringListIndex >> CHUNK_SHIFT;
        int index = stringListIndex & CHUNK_MASK;
        int count = 0;
        while (true) {
            if (fStringList[chunk][index] == -1)
                return count;
            count++;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
    }
    public boolean stringInList(int stringListIndex, int stringIndex) {
        int chunk = stringListIndex >> CHUNK_SHIFT;
        int index = stringListIndex & CHUNK_MASK;
        while (true) {
            if (fStringList[chunk][index] == stringIndex)
                return true;
            if (fStringList[chunk][index] == -1)
                return false;
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
    }
    public String stringListAsString(int stringListIndex) {
        int chunk = stringListIndex >> CHUNK_SHIFT;
        int index = stringListIndex & CHUNK_MASK;
        StringBuffer sb = new StringBuffer();
        char sep = '(';
        while (fStringList[chunk][index] != -1) {
            sb.append(sep);
            sep = '|';
            sb.append(toString(fStringList[chunk][index]));
            if (++index == CHUNK_SIZE) {
                chunk++;
                index = 0;
            }
        }
        if (sep == '|')
            sb.append(')');
        return sb.toString();
    }
    //
    //
    //
    private void releaseStringInternal(int chunk, int index) {
        fString[chunk][index] = null;
        fStringProducer[chunk][index] = null;
        fLength[chunk][index] = 0;
        //
        // REVISIT - not synchronized.
        //
        fOffset[chunk][index] = fStringFreeList;
        int offset = (chunk << CHUNK_SHIFT) + index;
        fStringFreeList = offset;
    }
    //
    //
    //
    public void releaseString(int stringIndex) {
        if (stringIndex < 0 || stringIndex >= fStringCount)
            return;
        int chunk = stringIndex >> CHUNK_SHIFT;
        int index = stringIndex & CHUNK_MASK;
        if (fOffset[chunk][index] != -1) {
            if (fStringProducer[chunk][index] != null)
                fStringProducer[chunk][index].releaseString(fOffset[chunk][index], fLength[chunk][index]);
            releaseStringInternal(chunk, index);
        }
    }
    //
    // Get String value.  Cache the result.
    //
    public String toString(int stringIndex) {
        if (stringIndex >= 0 && stringIndex < fString[0].length) {
            String result = fString[0][stringIndex];
            if (result != null) {
                return result;
            }
        }

        if (stringIndex < 0 || stringIndex >= fStringCount)
            return null;
        int chunk = stringIndex >> CHUNK_SHIFT;
        int index = stringIndex & CHUNK_MASK;
        String s = fString[chunk][index];
        if (s != null)
            return s;
        s = fStringProducer[chunk][index].toString(fOffset[chunk][index], fLength[chunk][index]);
        fStringProducer[chunk][index].releaseString(fOffset[chunk][index], fLength[chunk][index]);
        fString[chunk][index] = s;
        fStringProducer[chunk][index] = null;
        return s;
    }
    //
    //
    //
    public String orphanString(int stringIndex) {
        if (stringIndex < 0 || stringIndex >= fStringCount)
            return null;
        int chunk = stringIndex >> CHUNK_SHIFT;
        int index = stringIndex & CHUNK_MASK;
        String s = fString[chunk][index];
        if (s == null) {
            s = fStringProducer[chunk][index].toString(fOffset[chunk][index], fLength[chunk][index]);
            fStringProducer[chunk][index].releaseString(fOffset[chunk][index], fLength[chunk][index]);
            releaseStringInternal(chunk, index);
        } else if (fOffset[chunk][index] != -1) {
            releaseStringInternal(chunk, index);
        }
        return s;
    }
}
