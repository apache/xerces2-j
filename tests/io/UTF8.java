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

package io;

import java.io.EOFException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.xerces.impl.io.UTF8Reader;

/**
 * This program tests the ability of the customized UTF-8 reader used in
 * Xerces to correctly handle all valid Unicode characters. The UTF-8
 * reader included in the JDK is also tested as a reference but this
 * reader currently has a problem with surrogate characters (up to Java
 * 1.3).
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class UTF8 {

    //
    // MAIN
    //

    /** Main program entry. */
    public static void main(String[] argv) throws Exception {

        final int BLOCK_READ_SIZE = 2048;

        //
        // Test Java reference implementation of UTF-8 decoder
        //

        System.err.println("#");
        System.err.println("# Testing Java UTF-8 decoder");
        System.err.println("#");

        // test character by character
        try {
            InputStream stream = new UTF8Producer();
            Reader reader = new InputStreamReader(stream, "UTF8");
            long time = testCharByChar(reader);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        // test character array
        try {
            InputStream stream = new UTF8Producer();
            Reader reader = new InputStreamReader(stream, "UTF8");
            long time = testCharArray(reader, BLOCK_READ_SIZE);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        //
        // Test custom implementation of UTF-8 decoder
        //

        System.err.println("#");
        System.err.println("# Testing custom UTF-8 decoder");
        System.err.println("#");

        // test character by character
        try {
            InputStream stream = new UTF8Producer();
            Reader reader = new UTF8Reader(stream);
            long time = testCharByChar(reader);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
        // test character array
        try {
            InputStream stream = new UTF8Producer();
            Reader reader = new UTF8Reader(stream);
            long time = testCharArray(reader, BLOCK_READ_SIZE);
            System.err.println("PASS ("+time+" ms)");
            reader.close();
        } 
        catch (IOException e) {
            System.err.println("FAIL: "+e.getMessage());
        }
        
    } // main(String[])

    //
    // Public static methods
    //

    /** This function tests the specified reader character by character. */
    public static long testCharByChar(Reader reader) throws Exception {

        long before = System.currentTimeMillis();
        System.err.println("# Testing character by character");

        System.err.println("testing 0x000000 -> 0x00007F");
        for (int i = 0; i < 0x0080; i++) {
            int c = reader.read();
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x000080 -> 0x0007FF");
        for (int i = 0x0080; i < 0x0800; i++) {
            int c = reader.read();
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x000800 -> 0x00D7FF");
        for (int i = 0x0800; i < 0xD800; i++) {
            int c = reader.read();
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x00E000 -> 0x00FFFF");
        for (int i = 0xE000; i < 0x010000; i++) {
            int c = reader.read();
            if (c != i) {
                expectedChar(null, i, c);
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
            int c = reader.read();
            if (c != hs) {
                expectedChar("high surrogate", hs, c);
            }
            // low surrogate
            c = reader.read();
            if (c != ls) {
                expectedChar("low surrogate", ls, c);
            }
        }
        System.err.println("checking EOF");
        int c = reader.read();
        if (c != -1) {
            extraChar(c);
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

        System.err.println("testing 0x000000 -> 0x00007F");
        for (int i = 0; i < 0x0080; i++) {
            if (position == count) {
                count = load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x000080 -> 0x0007FF");
        for (int i = 0x0080; i < 0x0800; i++) {
            if (position == count) {
                count = load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x000800 -> 0x00D7FF");
        for (int i = 0x0800; i < 0xD800; i++) {
            if (position == count) {
                count = load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                expectedChar(null, i, c);
            }
        }
        System.err.println("testing 0x00E000 -> 0x00FFFF");
        for (int i = 0xE000; i < 0x010000; i++) {
            if (position == count) {
                count = load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != i) {
                expectedChar(null, i, c);
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
                count = load(reader, ch);
                position = 0;
            }
            int c = ch[position++];
            if (c != hs) {
                expectedChar("high surrogate", hs, c);
            }
            // low surrogate
            if (position == count) {
                count = load(reader, ch);
                position = 0;
            }
            c = ch[position++];
            if (c != ls) {
                expectedChar("low surrogate", ls, c);
            }
        }
        System.err.println("checking EOF");
        if (position == count) {
            count = load(reader, ch);
            position = 0;
        }
        if (count != -1) {
            extraChar(ch[position]);
        }
        long after = System.currentTimeMillis();

        return after - before;

    } // testCharArray(Reader):long

    //
    // Private static methods
    //

    /** Loads another block of characters from the reader. */
    private static int load(Reader reader, char[] ch) throws IOException {
        int count = reader.read(ch, 0, ch.length);
        return count;
    } // load(Reader,char[]):int

    /** Creates an I/O exception for expected character. */
    private static void expectedChar(String prefix, int ec, int fc) throws IOException {
        StringBuffer str = new StringBuffer();
        str.append("expected ");
        if (prefix != null) {
            str.append(prefix);
            str.append(' ');
        }
        str.append("0x");
        str.append(Integer.toHexString(ec));
        str.append(" but found 0x");
        if (fc != -1) {
            str.append(Integer.toHexString(fc));
        }
        else {
            str.append("EOF");
        }
        String message = str.toString();
        throw new IOException(message);
    } // expectedChar(String,int,int)

    /** Creates an I/O exception for extra character. */
    private static void extraChar(int c) throws IOException {
        StringBuffer str = new StringBuffer();
        str.append("found extra character 0x");
        str.append(Integer.toHexString(c));
        String message = str.toString();
        throw new IOException(message);
    } // extraChar(int)

    //
    // Classes
    //

    /**
     * This classes produces a stream of UTF-8 byte sequences for all 
     * valid Unicode characters.
     *
     * @author Andy Clark, IBM
     */
    public static class UTF8Producer
        extends InputStream {

        //
        // Data
        //

        /** The current code point. */
        private int fCodePoint;

        /** The current byte of the current code point. */
        private int fByte;

        //
        // InputStream methods
        //

        /** Reads the next character. */
        public int read() throws IOException {

            // UTF-8:   [0xxx xxxx]
            // Unicode: [0000 0000] [0xxx xxxx]
            if (fCodePoint < 0x0080) {
                int b = fCodePoint;
                fCodePoint++;
                fByte = 0;
                return b;
            }

            // UTF-8:   [110y yyyy] [10xx xxxx]
            // Unicode: [0000 0yyy] [yyxx xxxx]
            if (fCodePoint < 0x0800) {
                switch (fByte) {
                    case 0: {
                        int b = 0x00C0 | ((fCodePoint >> 6) & 0x001F);
                        fByte++;
                        return b;
                    }
                    case 1: {
                        int b = 0x0080 | (fCodePoint & 0x003F);
                        fCodePoint++;
                        fByte = 0;
                        return b;
                    }
                    default: {
                        throw new RuntimeException("byte "+fByte+" of 2 byte UTF-8 sequence");
                    }
                }
            }

            // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
            // Unicode: [zzzz yyyy] [yyxx xxxx]*
            if (fCodePoint < 0x10000) {
                switch (fByte) {
                    case 0: {
                        int b = 0x00E0 | ((fCodePoint >> 12) & 0x000F);
                        fByte++;
                        return b;
                    }
                    case 1: {
                        int b = 0x0080 | ((fCodePoint >> 6) & 0x003F);
                        fByte++;
                        return b;
                    }
                    case 2: {
                        int b = 0x0080 | (fCodePoint & 0x003F);
                        fCodePoint++;
                        // skip surrogate blocks
                        if (fCodePoint == 0xD800) {
                            fCodePoint = 0xE000;
                        }
                        fByte = 0;
                        return b;
                    }
                    default: {
                        throw new RuntimeException("byte "+fByte+" of 3 byte UTF-8 sequence");
                    }
                }
            }

            // UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
            // Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
            //          [1101 11yy] [yyxx xxxx] (low surrogate)
            //          * uuuuu = wwww + 1
            //          [0000 0000] [000u uuuu] [zzzz yyyy] [yyxx xxxx]
            if (fCodePoint < 0x110000) {
                switch (fByte) {
                    case 0: {
                        int uuuuu = (fCodePoint >> 16) & 0x001F;
                        int b = 0x00F0 | (uuuuu >> 2);
                        fByte++;
                        return b;
                    }
                    case 1: {
                        int uuuuu = (fCodePoint >> 16) & 0x001F;
                        int zzzz = (fCodePoint >> 12) & 0x000F;
                        int b = 0x0080 | ((uuuuu << 4) & 0x0030) | zzzz;
                        fByte++;
                        return b;
                    }
                    case 2: {
                        int yyyyyy = (fCodePoint >> 6) & 0x003F;
                        int b = 0x0080 | yyyyyy;
                        fByte++;
                        return b;
                    }
                    case 3: {
                        int xxxxxx = fCodePoint & 0x003F;
                        int b = 0x0080 | xxxxxx;
                        fCodePoint++;
                        fByte = 0;
                        return b;
                    }
                    default: {
                        throw new RuntimeException("byte "+fByte+" of 4 byte UTF-8 sequence");
                    }
                }
            }
            
            // done
            return -1;

        } // read():int

    } // class UTF8Producer

} // class UTF8
