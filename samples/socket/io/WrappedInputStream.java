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

package socket.io;

import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * This input stream works in conjunction with the WrappedOutputStream
 * to introduce a protocol for reading arbitrary length data in a
 * uniform way.
 * <p>
 * <strong>Note:</strong> See the javadoc for WrappedOutputStream for
 * more information.
 *
 * @see WrappedOutputStream
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class WrappedInputStream
    extends FilterInputStream {

    //
    // Data
    //

    /** Bytes left on input stream for current packet. */
    protected int fPacketCount;

    /** 
     * Data input stream. This stream is used to input the block sizes
     * from the data stream that are written by the WrappedOutputStream.
     * <p>
     * <strong>Note:</strong> The data input stream is only used for
     * reading the byte count for performance reasons. We avoid the
     * method indirection for reading the byte data.
     */
    protected DataInputStream fDataInputStream;

    /** To mark that the stream is "closed". */
    protected boolean fClosed;

    //
    // Constructors
    //

    /** Constructs a wrapper for the given an input stream. */
    public WrappedInputStream(InputStream stream) {
        super(stream);
        fDataInputStream = new DataInputStream(stream);
    } // <init>(InputStream)

    //
    // InputStream methods
    //

    /** Reads a single byte. */
    public int read() throws IOException {

        // ignore, if already closed
        if (fClosed) {
            return -1;
        }

        // read packet header
        if (fPacketCount == 0) {
            fPacketCount = fDataInputStream.readShort() & 0x0000FFFF;
            if (fPacketCount == 0) {
                fClosed = true;
                return -1;
            }
        }

        // read a byte from the packet
        fPacketCount--;
        return super.in.read();

    } // read():int

    /** 
     * Reads a block of bytes and returns the total number of bytes read. 
     */
    public int read(byte[] b, int offset, int length) throws IOException {

        // ignore, if already closed
        if (fClosed) {
            return -1;
        }

        // read packet header
        if (fPacketCount == 0) {
            fPacketCount = fDataInputStream.readShort() & 0x0000FFFF;
            if (fPacketCount == 0) {
                fClosed = true;
                return -1;
            }
        }

        // read bytes from packet
        if (length > fPacketCount) {
            length = fPacketCount;
        }
        int count = super.in.read(b, offset, length);
        if (count == -1) {
            // NOTE: This condition should not happen. The end of 
            //       the stream should always be designated by a 
            //       byte count header of 0. -Ac
            fClosed = true;
            return -1;
        }
        fPacketCount -= count;

        // return total bytes read
        return count;

    } // read(byte[],int,int):int

    /** Skips the specified number of bytes from the input stream. */
    public long skip(long n) throws IOException {
        if (!fClosed) {
            // NOTE: This should be rewritten to be more efficient. -Ac
            for (long i = 0; i < n; i++) {
                int b = read();
                if (b == -1) {
                    return i + 1;
                }
            }
            return n;
        }
        return 0;
    } // skip(long):long

    /** 
     * Closes the input stream. This method will search for the end of
     * the wrapped input, positioning the stream at after the end packet.
     * <p>
     * <strong>Note:</strong> This method does not close the underlying
     * input stream.
     */
    public void close() throws IOException {
        if (!fClosed) {
            fClosed = true;
            do {
                super.in.skip(fPacketCount);
                fPacketCount = fDataInputStream.readShort() & 0x0000FFFF;
            } while (fPacketCount > 0);
        }
    } // close()

} // class WrappedInputStream
