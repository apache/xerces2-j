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

import java.io.IOException;
import java.io.InputStream;

/**
 * This class is used for accessing the data provided by an InputStream.
 *
 * There are two ways in which this class is used.  The first occurs
 * when we are prescanning the start of the stream to determine the
 * encoding being used.  Since we do not require that the stream be
 * positionable, we wrap it with an instance of this class.  The first
 * "chunk" of the file is read and the data may be accessed directly
 * using the byteAt(offset) method.  After we have determined the
 * encoding of the byte stream, the instance of this class is passed
 * on to the EntityReader that will process the data for the scanner.
 *
 * At this point, the reader may continue to access this instance using
 * the byteAt method, which will continue to read the contents into
 * chunks as required until end of input.  An example of this is the
 * UCSReader.
 *
 * Alternatively, the reader may access this instance as an InputStream
 * which will first return any data that has been reading into the
 * chunks, and will then return the remaining data from the original
 * InputStream directly.
 *
 * @version
 */
public final class ChunkyByteArray extends InputStream {

    /**
     * Constructor
     *
     * Reads the first chunk.
     *
     * @param is The input stream containing the data of the entity.
     */
    public ChunkyByteArray(InputStream is) throws IOException {
        fInputStream = is;
        fill();
    }

    /**
     * Read a byte.
     *
     * @return The next byte of the input data or -1 if there is no more data.
     */
    public int read() throws IOException {
        if (fData == null)
            return fInputStream == null ? -1 : fInputStream.read();
        int b = (int)(fData[0][fOffset]);
        if (++fOffset == fLength) {
            fData = null;
            if (fLength < CHUNK_SIZE)
                fInputStream = null;
        }
        return b;
    }

    /**
     * Read bytes.
     *
     * @param buffer The destination for the bytes returned.  If null, then
     *               the data will discarded instead of returned.
     * @param offset The offset within the buffer where the first returned
     *               byte should be placed.
     * @param length The maximum number of bytes to place in the buffer or discard.
     * @return The number of bytes actually placed in the buffer or discarded.
     */
    public int read(byte buffer[], int offset, int length) throws IOException {
        int bytesLeft = fLength - fOffset;
        if (bytesLeft == 0)
            return fInputStream == null ? -1 : fInputStream.read(buffer, offset, length);
        if (length <= 0)
            return 0;
        byte[] chunk = fData[0];
        if (length >= bytesLeft) {
            length = bytesLeft;
            if (fLength < CHUNK_SIZE)
                fInputStream = null;
        }
        if (buffer == null) {
            fOffset += length;
            return length;
        }
        int stop = offset + length;
        do {
            buffer[offset++] = chunk[fOffset++];
        } while (offset < stop);
        return length;
    }

    /**
     * Reset position within the data stream back to
     * the very beginning.
     */
    public void rewind() {
        fOffset = 0;
    }

    /**
     * Return a byte of input data at the given offset.
     *
     * @param offset The offset in the data stream.
     * @return The byte at the specified position within the data stream.
     */
    public byte byteAt(int offset) throws IOException {
        int chunk = offset >> CHUNK_SHIFT;
        int index = offset & CHUNK_MASK;
        try {
            return fData[chunk][index];
        }
        catch (NullPointerException ex) {
            // ignore -- let fill create new chunk
        }
        catch (ArrayIndexOutOfBoundsException e) {
            // current chunk array is not big enough; resize
            byte newdata[][] = new byte[fData.length * 2][];
            System.arraycopy(fData, 0, newdata, 0, fData.length);
            fData = newdata;
        }
        if (index == 0) {
            fill();
            return fData[chunk][index];
        }
        return 0;
    }

    /**
     * Test to see if an offset is at the end of the input data.
     *
     * @param offset A position in the data stream.
     * @return <code>true</code> if the position is at the end of the data stream;
     *         <code>false</code> otherwise.
     */
    public boolean atEOF(int offset) {
        return (offset > fLength);
    }

    //
    // Fill in the next chunk with additional data.
    //
    private void fill() throws IOException {
        int bufnum = fLength >> CHUNK_SHIFT;
        byte[] data = new byte[CHUNK_SIZE];
        fData[bufnum] = data;
        int offset = 0;
        int capacity = CHUNK_SIZE;
        int result = 0;
        do {
            result = fInputStream.read(data, offset, capacity);
            if (result == -1) {
                data[offset] = (byte)0xff;
                fInputStream.close();
                fInputStream = null;
                break;
            }
            if (result > 0) {
                fLength += result;
                offset += result;
                capacity -= result;
            }
        } while (capacity > 0);
    }
    //
    // Chunk size constants
    //
    private static final int CHUNK_SHIFT = 14;           // 2^14 = 16k
    private static final int CHUNK_SIZE = (1 << CHUNK_SHIFT);
    private static final int CHUNK_MASK = CHUNK_SIZE - 1;
    private static final int INITIAL_CHUNK_COUNT = (1 << (20 - CHUNK_SHIFT));   // 2^20 = 1m
    //
    // Instance variables
    //
    private InputStream fInputStream = null;
    private byte[][] fData = new byte[INITIAL_CHUNK_COUNT][];
    private int fLength = 0;
    private int fOffset = 0; // for read methods
}
