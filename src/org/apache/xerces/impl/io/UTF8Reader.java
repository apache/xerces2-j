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

package org.apache.xerces.impl.io;

import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UTFDataFormatException;

/**
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class UTF8Reader
    extends Reader {

    //
    // Constants
    //

    /** Default byte buffer size (2048). */
    public static final int DEFAULT_BUFFER_SIZE = 2048;

    //
    // Data
    //

    /** Input stream. */
    protected InputStream fInputStream;

    /** Byte buffer. */
    protected byte[] fBuffer;

    /** Surrogate character. */
    private int fSurrogate;

    /** Buffer offset to start reading from. */
    private int fOffset;

    //
    // Constructors
    //

    /** 
     * Constructs a UTF-8 reader from the specified input stream 
     * using the default buffer size.
     *
     * @param inputStream The input stream.
     */
    public UTF8Reader(InputStream inputStream) {
        this(inputStream, DEFAULT_BUFFER_SIZE);
    } // <init>(InputStream)

    /** 
     * Constructs a UTF-8 reader from the specified input stream 
     * and buffer size.
     *
     * @param inputStream The input stream.
     * @param size        The initial buffer size.
     */
    public UTF8Reader(InputStream inputStream, int size) {
        fInputStream = inputStream;
        fBuffer = new byte[size];
    } // <init>(InputStream,int)

    //
    // Reader methods
    //

    /**
     * Read a single character.  This method will block until a character is
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * <p> Subclasses that intend to support efficient single-character input
     * should override this method.
     *
     * @return     The character read, as an integer in the range 0 to 16383
     *             (<tt>0x00-0xffff</tt>), or -1 if the end of the stream has
     *             been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read() throws IOException {
        int c = fSurrogate;
        fSurrogate = -1;
        if (fSurrogate == -1) {
            int b0 = fInputStream.read();
            if ((b0 & 0x80) != 0) {
                throw new RuntimeException("not implemented");
            }
            else {
                c = b0;
            }
        }
        return c;
    } // read():int

    /**
     * Read characters into a portion of an array.  This method will block
     * until some input is available, an I/O error occurs, or the end of the
     * stream is reached.
     *
     * @param      ch     Destination buffer
     * @param      offset Offset at which to start storing characters
     * @param      length Maximum number of characters to read
     *
     * @return     The number of characters read, or -1 if the end of the
     *             stream has been reached
     *
     * @exception  IOException  If an I/O error occurs
     */
    public int read(char ch[], int offset, int length) throws IOException {

        // adjust length to read
        if (length > fBuffer.length - fOffset) {
            length = fBuffer.length - fOffset;
        }

        // read bytes
        int count = fInputStream.read(fBuffer, fOffset, length);

        // convert bytes to characters
        fOffset = 0;
        final int total = count;
        for (int in = 0, out = offset; in < total; in++) {
            int b0 = fBuffer[in];

            // UTF-8:   [0xxx xxxx]
            // Unicode: [0000 0000] [0xxx xxxx]
            if (b0 < 0x80) {
                ch[offset + in] = (char)b0;
                continue;
            }

            // UTF-8:   [110y yyyy] [10xx xxxx]
            // Unicode: [0000 0yyy] [yyxx xxxx]
            int mask = b0 & 0xF8;
            if (mask == 0xC0) {
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fOffset = 1;
                    count -= fOffset;
                    break;
                }
                int b1 = fBuffer[in];
                if ((b1 & 0xC0) != 0x80) {
                    invalidByte(2, 2, b1);
                }
                int c = ((b0 >> 2) & 0x03) | ((b0 << 6) & 0xFF) | (b1 & 0x3F);
                ch[out++] = (char)c;
                continue;
            }

            // UTF-8:   [1110 zzzz] [10yy yyyy] [10xx xxxx]
            // Unicode: [zzzz yyyy] [yyxx xxxx]
            if (mask == 0xE0) {
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fOffset = 1;
                    count -= fOffset;
                    break;
                }
                int b1 = fBuffer[in];
                if ((b1 & 0xC0) != 0x80) {
                    invalidByte(2, 3, b1);
                }
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fBuffer[1] = (byte)b1;
                    fOffset = 2;
                    count -= fOffset;
                    break;
                }
                int b2 = fBuffer[in];
                if ((b2 & 0xC0) != 0x80) {
                    invalidByte(3, 3, b2);
                }
                int c = ((b0 << 4) & 0xF0) | ((b1 >> 2) & 0x0F) |
                        ((b1 << 6) & 0xFC) | (b2 & 0x3F);
                ch[out++] = (char)c;
                continue;
            }

            // UTF-8:   [1111 0uuu] [10uu zzzz] [10yy yyyy] [10xx xxxx]*
            // Unicode: [1101 10ww] [wwzz zzyy] (high surrogate)
            //          [1101 11yy] [yyxx xxxx] (low surrogate)
            //          * uuuuu = wwww + 1
            if (mask == 0xF0) {
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fOffset = 1;
                    count -= fOffset;
                    break;
                }
                int b1 = fBuffer[in];
                if ((b1 & 0xC0) != 0x80) {
                    invalidByte(2, 4, b1);
                }
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fBuffer[1] = (byte)b1;
                    fOffset = 2;
                    count -= fOffset;
                    break;
                }
                int b2 = fBuffer[in];
                if ((b2 & 0xC0) != 0x80) {
                    invalidByte(3, 4, b2);
                }
                if (++in == total) {
                    fBuffer[0] = (byte)b0;
                    fBuffer[1] = (byte)b1;
                    fBuffer[2] = (byte)b2;
                    fOffset = 3;
                    count -= fOffset;
                    break;
                }
                int b3 = fBuffer[in];
                if ((b3 & 0xC0) != 0x80) {
                    invalidByte(4, 4, b3);
                }
                int uuuuu = ((b0 << 5) & 0xE0) | ((b1 >> 4) & 0x0F);
                int wwww = uuuuu - 1;
                int hs = 0xD800 | 
                         ((wwww & 0x0C) << 8) | ((wwww & 0x03) << 6) |
                         ((b1 << 2) & 0x3C) | ((b2 >> 4) & 0x03);
                ch[out++] = (char)hs;
                int ls = 0xDC00 | ((b2 & 0x0F) << 6) | (b3 & 0x3F);
                fSurrogate = ls;
                continue;
            }

            // error
            invalidByte(1, 1, b0);
        }

        // return number of characters converted
        return count;

    } // read(char[],int,int)

    /**
     * Skip characters.  This method will block until some characters are
     * available, an I/O error occurs, or the end of the stream is reached.
     *
     * @param  n  The number of characters to skip
     *
     * @return    The number of characters actually skipped
     *
     * @exception  IOException  If an I/O error occurs
     */
    public long skip(long n) throws IOException {
        return fInputStream.skip(n);
    } // skip(long):long

    /**
     * Tell whether this stream is ready to be read.
     *
     * @return True if the next read() is guaranteed not to block for input,
     * false otherwise.  Note that returning false does not guarantee that the
     * next read will block.
     *
     * @exception  IOException  If an I/O error occurs
     */
    public boolean ready() throws IOException {
	    return false;
    } // ready()

    /**
     * Tell whether this stream supports the mark() operation.
     */
    public boolean markSupported() {
	    return fInputStream.markSupported();
    } // markSupported()

    /**
     * Mark the present position in the stream.  Subsequent calls to reset()
     * will attempt to reposition the stream to this point.  Not all
     * character-input streams support the mark() operation.
     *
     * @param  readAheadLimit  Limit on the number of characters that may be
     *                         read while still preserving the mark.  After
     *                         reading this many characters, attempting to
     *                         reset the stream may fail.
     *
     * @exception  IOException  If the stream does not support mark(),
     *                          or if some other I/O error occurs
     */
    public void mark(int readAheadLimit) throws IOException {
	    fInputStream.mark(readAheadLimit);
    } // mark(int)

    /**
     * Reset the stream.  If the stream has been marked, then attempt to
     * reposition it at the mark.  If the stream has not been marked, then
     * attempt to reset it in some way appropriate to the particular stream,
     * for example by repositioning it to its starting point.  Not all
     * character-input streams support the reset() operation, and some support
     * reset() without supporting mark().
     *
     * @exception  IOException  If the stream has not been marked,
     *                          or if the mark has been invalidated,
     *                          or if the stream does not support reset(),
     *                          or if some other I/O error occurs
     */
    public void reset() throws IOException {
        fInputStream.reset();
    } // reset()

    /**
     * Close the stream.  Once a stream has been closed, further read(),
     * ready(), mark(), or reset() invocations will throw an IOException.
     * Closing a previously-closed stream, however, has no effect.
     *
     * @exception  IOException  If an I/O error occurs
     */
     public void close() throws IOException {
         fInputStream.close();
     } // close()

     //
     // Private methods
     //

     /** Throws an exception for invalid byte. */
     private void invalidByte(int position, int count, int c) 
        throws UTFDataFormatException {

         StringBuffer str = new StringBuffer();
         str.append("invalid byte ");
         str.append(position);
         str.append(" of ");
         str.append(count);
         str.append("-byte UTF-8 sequence (0x");
         str.append(Integer.toHexString(c));
         str.append(')');

         String message = str.toString();
         throw new UTFDataFormatException(message);

     } // invalidByte(int,int,int)

} // class UTF8Reader
