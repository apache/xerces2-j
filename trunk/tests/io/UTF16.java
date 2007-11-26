/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.xerces.impl.io.UTF16Reader;
import org.apache.xerces.util.XMLChar;

/**
 * This program tests the customized UTF-16 reader for the parser,
 * comparing it with the Java UTF-16 reader.
 *
 * @version $Id$
 */
public class UTF16 {
    
    //
    // MAIN
    //

    /** Main program entry. */
    public static void main(String[] argv) throws Exception {
        testUTF16Decoder(true);
        testUTF16Decoder(false);
    } // main(String[])
    
    //
    // Public static methods
    //
    
    public static void testUTF16Decoder(boolean isBigEndian) throws Exception {
        
        final int BLOCK_READ_SIZE = 2048;
        final String encoding = isBigEndian ? "UnicodeBig" : "UnicodeLittle";
        final String shortName = isBigEndian ? "BE" : "LE";

        //
        // Test Java reference implementation of UTF-16 decoder
        //

        System.err.println("#");
        System.err.println("# Testing Java UTF-16" + shortName + " decoder");
        System.err.println("#");

        // test character by character
        try {
            InputStream stream = new UTF16Producer(isBigEndian);
            Reader reader = new InputStreamReader(stream, encoding);
            long time = testCharByChar(reader);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        // test character array
        try {
            InputStream stream = new UTF16Producer(isBigEndian);
            Reader reader = new InputStreamReader(stream, encoding);
            long time = testCharArray(reader, BLOCK_READ_SIZE);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        //
        // Test custom implementation of UTF-16 decoder
        //

        System.err.println("#");
        System.err.println("# Testing custom UTF-16" + shortName + " decoder");
        System.err.println("#");

        // test character by character
        try {
            InputStream stream = new UTF16Producer(isBigEndian);
            Reader reader = new UTF16Reader(stream, isBigEndian);
            long time = testCharByChar(reader);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        // test character array
        try {
            InputStream stream = new UTF16Producer(isBigEndian);
            Reader reader = new UTF16Reader(stream, isBigEndian);
            long time = testCharArray(reader, BLOCK_READ_SIZE);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
    }

    /** This function tests the specified reader character by character. */
    public static long testCharByChar(Reader reader) throws Exception {

        long before = System.currentTimeMillis();
        System.err.println("# Testing character by character");

        System.err.println("testing 0x000000 -> 0x00D7FF");
        for (int i = 0; i < 0xD800; i++) {
            int c = reader.read();
            if (c != i) {
                UTF8.expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x00E000 -> 0x00FFFD");
        for (int i = 0xE000; i < 0xFFFE; i++) {
            int c = reader.read();
            if (c != i) {
                UTF8.expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x010000 -> 0x10FFFF");
        for (int i = 0x10000; i < 0x110000; i++) {
            // vars
            int uuuuu = (i >> 16) & 0x001F;
            int wwww = uuuuu - 1;
            int zzzz = (i >> 12) & 0x000F;
            int yyyyyy = (i >> 6) & 0x003F;
            int xxxxxx = i & 0x003F;
            int hs = 0xD800 | (wwww << 6) | (zzzz << 2) | (yyyyyy >> 4);
            int ls = 0xDC00 | ((yyyyyy << 6) & 0x03C0) | xxxxxx;
            // high surrogate
            int c = reader.read();
            if (c != hs) {
                UTF8.expectedChar("high surrogate", hs, c);
            }
            // low surrogate
            c = reader.read();
            if (c != ls) {
                UTF8.expectedChar("low surrogate", ls, c);
            }
        }
        System.err.println("checking EOF");
        int c = reader.read();
        if (c != -1) {
            UTF8.extraChar(c);
        }
        long after = System.currentTimeMillis();

        return after - before;

    } // testCharByChar(Reader):long

    /**
     * This function tests the given reader by performing block character
     * reads of the specified size.
     */
    public static long testCharArray(Reader reader, int size) throws Exception {

        long before = System.currentTimeMillis();
        System.err.println("# Testing character array of size "+size);

        char[] ch = new char[size];
        int count = 0;
        int position = 0;

        System.err.println("testing 0x000000 -> 0x00D7FF");
        for (int i = 0; i < 0xD800; i++) {
            if (position == count) {
                count = UTF8.load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                UTF8.expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x00E000 -> 0x00FFFD");
        for (int i = 0xE000; i < 0xFFFE; i++) {
            if (position == count) {
                count = UTF8.load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                UTF8.expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x010000 -> 0x110000");
        for (int i = 0x10000; i < 0x110000; i++) {
            // vars
            int uuuuu = (i >> 16) & 0x001F;
            int wwww = uuuuu - 1;
            int zzzz = (i >> 12) & 0x000F;
            int yyyyyy = (i >> 6) & 0x003F;
            int xxxxxx = i & 0x003F;
            int hs = 0xD800 | (wwww << 6) | (zzzz << 2) | (yyyyyy >> 4);
            int ls = 0xDC00 | ((yyyyyy << 6) & 0x03C0) | xxxxxx;
            // high surrogate
            if (position == count) {
                count = UTF8.load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != hs) {
                UTF8.expectedChar("high surrogate", hs, c);
            }
            // low surrogate
            if (position == count) {
                count = UTF8.load(reader, ch);
                position = 0;
            }
            c = ch[position++];
            if (c != ls) {
                UTF8.expectedChar("low surrogate", ls, c);
            }
        }
        System.err.println("checking EOF");
        if (position == count) {
            count = UTF8.load(reader, ch);
            position = 0;
        }
        if (count != -1) {
            UTF8.extraChar(ch[position]);
        }
        long after = System.currentTimeMillis();

        return after - before;

    } // testCharArray(Reader):long
    
    //
    // Classes
    //
    
    /**
     * This classes produces a stream of UTF-16 byte sequences for all 
     * valid Unicode characters.
     */
    public static class UTF16Producer
        extends InputStream {
        
        //
        // Data
        //

        /** The current code point. */
        private int fCodePoint;

        /** The current byte of the current code point. */
        private int fByte;
        
        /** Endianness. */
        private final boolean fIsBigEndian;
        
        //
        // Constructors
        //
        
        public UTF16Producer(boolean isBigEndian) {
            fIsBigEndian = isBigEndian;
        }
        
        //
        // InputStream methods
        //

        /** Reads the next character. */
        public int read() throws IOException {
            
            if (fCodePoint < 0xFFFE) {
                // skip surrogate blocks
                if (fCodePoint == 0xD800) {
                    fCodePoint = 0xE000;
                }
                switch (fByte) {
                    case 0: {
                        final int b;
                        if (fIsBigEndian) {
                            b = fCodePoint >> 8;
                        }
                        else {
                            b = fCodePoint & 0xff;
                        }
                        fByte++;
                        return b;
                    }
                    case 1: {
                        final int b;
                        if (fIsBigEndian) {
                            b = fCodePoint & 0xff;
                        }
                        else {
                            b = fCodePoint >> 8;
                        }
                        fCodePoint++;
                        fByte = 0;
                        return b;
                    }
                    default: {
                        throw new RuntimeException("byte "+fByte+" of 2 byte UTF-8 sequence");
                    }
                }
            }
            if (fCodePoint == 0xFFFE) {
                fCodePoint = 0x10000;
            }
            if (fCodePoint < 0x110000) {
                switch (fByte) {
                    case 0: {
                        final int b;
                        if (fIsBigEndian) {
                            b = XMLChar.highSurrogate(fCodePoint) >> 8;
                        }
                        else {
                            b = XMLChar.highSurrogate(fCodePoint) & 0xff;
                        }
                        fByte++;
                        return b;
                    }
                    case 1: {
                        final int b;
                        if (fIsBigEndian) {
                            b = XMLChar.highSurrogate(fCodePoint) & 0xff;
                        }
                        else {
                            b = XMLChar.highSurrogate(fCodePoint) >> 8;
                        }
                        fByte++;
                        return b;
                    }
                    case 2: {
                        final int b;
                        if (fIsBigEndian) {
                            b = XMLChar.lowSurrogate(fCodePoint) >> 8;
                        }
                        else {
                            b = XMLChar.lowSurrogate(fCodePoint) & 0xff;
                        }
                        fByte++;
                        return b;
                    }
                    case 3: {
                        final int b;
                        if (fIsBigEndian) {
                            b = XMLChar.lowSurrogate(fCodePoint) & 0xff;
                        }
                        else {
                            b = XMLChar.lowSurrogate(fCodePoint) >> 8;
                        }
                        fCodePoint++;
                        fByte = 0;
                        return b;
                    }
                    default: {
                        throw new RuntimeException("byte "+fByte+" of 2 byte UTF-8 sequence");
                    }
                }
            }
            return -1;
        }
    }
}
