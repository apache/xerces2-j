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

package sax;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Random;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;

/**
 * This sample delays the input to the SAX parser to simulate reading data
 * from a socket where data is not always immediately available. An XML
 * parser should be able to parse the input and perform the necessary
 * callbacks as data becomes available.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class DelayedInput
    extends HandlerBase {

    //
    // MAIN
    //

    /** Main program entry. */
    public static void main(String[] argv) throws Exception {

        // print usage
        if (argv.length == 0) {
            System.out.println("usage: java DelayedInput file ...");
            System.exit(1);
        }

        // create handler and setup parser
        HandlerBase handler = new DelayedInput();
        Parser parser = new SAXParser();
        parser.setDocumentHandler(handler);

        // read files
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            System.out.println("argv["+i+"]: "+arg);
            InputStream stream = new DelayedInputStream(new FileInputStream(arg));
            InputSource source = new InputSource(stream);
            source.setSystemId(arg);
            parser.parse(source);
            stream.close();
        }

    } // main(String[])

    //
    // DocumentHandler methods
    //

    /** Start element. */
    public void startElement(String name, AttributeList attrs) {

        System.out.println("("+name);
        int length = attrs != null ? attrs.getLength() : 0;
        for (int i = 0; i < length; i++) {
            System.out.println("A"+attrs.getName(i)+' '+attrs.getValue(i));
        }

    } // startElement(String,AttributeList)

    /** End element. */
    public void endElement(String name) {
        System.out.println(")"+name);
    } // endElement(String)

    //
    // Classes
    //

    /**
     * Delayed input stream filter. This class will limit block reads to a small
     * number of bytes (suitable for display on a standard 80 column terminal)
     * pausing in small increments, randomly. This lets you can verify that the
     * parser can parse the input and make the appropriate callbacks as the
     * data arrives.
     *
     * @author Andy Clark, IBM
     */
    static class DelayedInputStream
        extends FilterInputStream {

        //
        // Data
        //

        /** Random number generator. */
        private Random fRandom = new Random(System.currentTimeMillis());

        //
        // Constructors
        //

        /** Constructs a delayed input stream from the specified input stream. */
        public DelayedInputStream(InputStream in) {
            super(in);
        } // <init>(InputStream)

        //
        // InputStream methods
        //

        /** Performs a delayed block read. */
        public int read(byte[] buffer, int offset, int length) throws IOException {

            // keep read small enough for display
            if (length > 48) {
                length = 48;
            }
            int count = 0;

            // read bytes and pause
            long before = System.currentTimeMillis();
            count = in.read(buffer, offset, length);
            try {
                Thread.currentThread().sleep(Math.abs(fRandom.nextInt()) % 2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            long after = System.currentTimeMillis();

            // print output
            System.out.print("read "+count+" bytes in "+(after-before)+" ms: ");
            printBuffer(buffer, offset, count);
            System.out.println();

            // return number of characters read
            return count;

        } // read(byte[],int,int):int

        //
        // Private methods
        //

        /** Prints the specified buffer. */
        private void printBuffer(byte[] buffer, int offset, int length) {

            // is there anything to do?
            if (length <= 0) {
                System.out.print("no data read");
                return;
            }

            // print buffer
            System.out.print('[');
            for (int i = 0; i < length; i++) {
                switch ((char)buffer[offset + i]) {
                    case '\r': {
                        System.out.print("\\r");
                        break;
                    }
                    case '\n': {
                        System.out.print("\\n");
                        break;
                    }
                    default: {
                        System.out.print((char)buffer[offset + i]);
                    }
                }
            }
            System.out.print(']');

        } // printBuffer(byte[],int,int)

    } // class DelayedInputStream

} // class DelayedInput
