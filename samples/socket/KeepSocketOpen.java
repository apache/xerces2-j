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

package socket;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import socket.io.WrappedInputStream;
import socket.io.WrappedOutputStream;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This sample provides a solution to the problem of 1) sending multiple
 * XML documents over a single socket connection or 2) sending other types
 * of data after the XML document without closing the socket connection.
 * <p>
 * The first situation is a problem because the XML specification does
 * not allow a document to contain multiple root elements. Therefore a
 * document stream must end (or at least appear to end) for the XML
 * parser to accept it as the end of the document.
 * <p>
 * The second situation is a problem because the XML parser buffers the
 * input stream in specified block sizes for performance reasons. This
 * could cause the parser to accidentally read additional bytes of data
 * beyond the end of the document. This actually relates to the first
 * problem if the documents are encoding in two different international
 * encodings.
 * <p>
 * The solution that this sample introduces wraps both the input and
 * output stream on both ends of the socket. The stream wrappers 
 * introduce a protocol that allows arbitrary length data to be sent
 * as separate, localized input streams. While the socket stream
 * remains open, a separate input stream is created to "wrap" an
 * incoming document and make it appear as if it were a standalone
 * input stream.
 *
 * @author Andy Clark, IBM
 *
 * @version $Id$
 */
public class KeepSocketOpen {

    //
    // MAIN
    //

    /** Main program entry. */
    public static void main(String[] argv) throws Exception {

        // constants
        final int port = 6789;

        // check args
        if (argv.length == 0) {
            System.out.println("usage: java socket.KeepSocketOpen file(s)");
            System.exit(1);
        }

        // create server and client
        Server server = new Server(port, argv);
        Client client = new Client("localhost", port);

        // start it running
        new Thread(server).start();
        new Thread(client).start();

    } // main(String[])

    //
    // Classes
    //

    /** 
     * Server. 
     *
     * @author Andy Clark, IBM
     */
    public static final class Server 
        extends ServerSocket 
        implements Runnable {

        //
        // Data
        //

        /** Files to send. */
        private String[] fFilenames;

        /** Verbose mode. */
        private boolean fVerbose;

        /** Buffer. */
        private byte[] fBuffer;

        //
        // Constructors
        //

        /** 
         * Constructs a server on the specified port and with the given
         * file list in terse mode. 
         */
        public Server(int port, String[] filenames) throws IOException {
            this(port, filenames, false);
        }

        /** 
         * Constructs a server on the specified port and with the given
         * file list and verbosity.
         */
        public Server(int port, String[] filenames, boolean verbose) 
            throws IOException {
            super(port);
            fFilenames = filenames;
            fVerbose = verbose;
            fBuffer = new byte[1024];
        } // <init>(int,String[])

        //
        // Runnable methods
        //

        /** Runs the server. */
        public void run() {

            System.out.println("Server: Started.");
            final Random random = new Random(System.currentTimeMillis());
            try {

                // accept connection
                if (fVerbose) System.out.println("Server: Waiting for Client connection...");
                final Socket clientSocket = accept();
                final OutputStream clientStream = clientSocket.getOutputStream();
                System.out.println("Server: Client connected.");

                // send files, one at a time
                for (int i = 0; i < fFilenames.length; i++) {

                    // open file
                    String filename = fFilenames[i];
                    System.out.println("Server: Opening file \""+filename+'"');
                    FileInputStream fileIn = new FileInputStream(filename);
                    
                    // wrap stream
                    if (fVerbose) System.out.println("Server: Wrapping output stream.");
                    WrappedOutputStream wrappedOut = new WrappedOutputStream(clientStream);
                    
                    // read file, writing to output
                    int total = 0;
                    while (true) {

                        // read random amount
                        int length = (Math.abs(random.nextInt()) + 1) % fBuffer.length;
                        if (fVerbose) System.out.println("Server: Attempting to read "+length+" byte(s).");
                        int count = fileIn.read(fBuffer, 0, length);
                        if (fVerbose) System.out.println("Server: Read "+length+" byte(s).");
                        if (count == -1) {
                            break;
                        }
                        if (fVerbose) System.out.println("Server: Writing "+count+" byte(s) to wrapped output stream.");
                        wrappedOut.write(fBuffer, 0, count);
                        total += count;
                    }
                    System.out.println("Server: Wrote "+total+" byte(s) total.");

                    // close stream
                    if (fVerbose) System.out.println("Server: Closing output stream.");
                    wrappedOut.close();
                    
                    // close file
                    if (fVerbose) System.out.println("Server: Closing file.");
                    fileIn.close();
                }

                // close connection to client
                if (fVerbose) System.out.println("Server: Closing socket.");
                clientSocket.close();

            }
            catch (IOException e) {
                System.out.println("Server ERROR: "+e.getMessage());
            }
            System.out.println("Server: Exiting.");

        } // run()

    } // class Server

    /**
     * Client.
     *
     * @author Andy Clark, IBM
     */
    public static final class Client
        extends HandlerBase
        implements Runnable {

        //
        // Data
        //

        /** Socket. */
        private Socket fServerSocket;

        /** Verbose mode. */
        private boolean fVerbose;

        /** Buffer. */
        private byte[] fBuffer;

        /** Parser. */
        private SAXParser fParser;

        //
        // Constructors
        //

        /** 
         * Constructs a Client that connects to the given port in terse
         * output mode. 
         */
        public Client(String address, int port) throws IOException {
            this(address, port, false);
            fParser = new SAXParser();
            fParser.setDocumentHandler(this);
            fParser.setErrorHandler(this);
        }

        /** 
         * Constructs a Client that connects to the given address:port and
         * with the specified verbosity. 
         */
        public Client(String address, int port, boolean verbose) 
            throws IOException {
            fServerSocket = new Socket(address, port);
            fVerbose = verbose;
            fBuffer = new byte[1024];
        } // <init>(String,int)

        //
        // Runnable methods
        //

        /** Runs the client. */
        public void run() {

            System.out.println("Client: Started.");
            try {
                // get input stream
                final InputStream serverStream = fServerSocket.getInputStream();

                // read files from server
                while (!Thread.interrupted()) {
                    // wrap input stream
                    if (fVerbose) System.out.println("Client: Wrapping input stream.");
                    InputStream wrappedIn = new WrappedInputStream(serverStream);
                    InputStream in = new InputStreamReporter(wrappedIn);

                    // parse file
                    if (fVerbose) System.out.println("Client: Parsing file.");
                    InputSource source = new InputSource(in);
                    fParser.parse(source);

                    // close stream
                    if (fVerbose) System.out.println("Client: Closing input stream.");
                    in.close();

                }

                // close socket
                if (fVerbose) System.out.println("Client: Closing socket.");
                fServerSocket.close();

            }
            catch (Exception e) {
                System.out.println("Client ERROR: "+e.getMessage());
            }
            System.out.println("Client: Exiting.");

        } // run()

        //
        // DocumentHandler methods
        //

        /** Start element. */
        public void startElement(String name, AttributeList attrs) {

            System.out.println("Client: ("+name);
            int length = attrs != null ? attrs.getLength() : 0;
            for (int i = 0; i < length; i++) {
                System.out.println("Client: A"+attrs.getName(i)+' '+attrs.getValue(i));
            }

        } // startElement(String,AttributeList)

        /** End element. */
        public void endElement(String name) {
            System.out.println("Client: )"+name);
        } // endElement(String)

        //
        // ErrorHandler methods
        //

        /** Warning. */
        public void warning(SAXParseException e) throws SAXException {
            System.out.println("Client: [warning] "+e.getMessage());
        } // warning(SAXParseException)

        /** Error. */
        public void error(SAXParseException e) throws SAXException {
            System.out.println("Client: [error] "+e.getMessage());
        } // error(SAXParseException)

        /** Fatal error. */
        public void fatalError(SAXParseException e) throws SAXException {
            System.out.println("Client: [fatal error] "+e.getMessage());
        } // fatalError(SAXParseException)

        //
        // Classes
        //

        class InputStreamReporter
            extends FilterInputStream {

            private long total;

            public InputStreamReporter(InputStream stream) {
                super(stream);
            }

            public int read() throws IOException {
                int b = super.in.read();
                if (b == -1) {
                    System.out.println("Client: Read "+total+" byte(s) total.");
                    return -1;
                }
                total++;
                return b;
            }

            public int read(byte[] b, int offset, int length) throws IOException {
                int count = super.in.read(b, offset, length);
                if (count == -1) {
                    System.out.println("Client: Read "+total+" byte(s) total.");
                    return -1;
                }
                total += count;
                if (Client.this.fVerbose) System.out.println("Client: Actually read "+count+" byte(s).");
                return count;
            }

        } // class InputStreamReporter

    } // class Client

} // class KeepSocketOpen
