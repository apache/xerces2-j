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

package sax;                    
                    
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import sax.helpers.AttributesImpl;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A sample SAX2 writer. This sample program illustrates how to
 * register a SAX2 ContentHandler and receive the callbacks in
 * order to print a document that is parsed.
 *
 * @version $Id$
 */
public class SAX2Writer 
    extends DefaultHandler {

    //
    // Constants
    //

    /** Default parser name. */
    private static final String 
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";

    //
    // Data
    //

    /** Print writer. */
    protected PrintWriter out;

    /** Canonical output. */
    protected boolean canonical;

    //
    // Constructors
    //

    /** Default constructor. */
    public SAX2Writer(boolean canonical) throws UnsupportedEncodingException {
        this(null, canonical);
    }

    protected SAX2Writer(String encoding, boolean canonical) throws UnsupportedEncodingException {

        if (encoding == null) {
            encoding = "UTF8";
        }

        out = new PrintWriter(new OutputStreamWriter(System.out, encoding));
        this.canonical = canonical;

    } // <init>(String,boolean)

    //
    // Public static methods
    //

    /** Prints the output from the SAX callbacks. */
    public static void print(String parserName, String uri, boolean canonical) {

        try {
            DefaultHandler handler = new SAX2Writer(canonical);

            XMLReader parser = (XMLReader)Class.forName(parserName).newInstance();
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            parser.parse(uri);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }

    } // print(String,String,boolean)

    //
    // DocumentHandler methods
    //

    /** Processing instruction. */
    public void processingInstruction(String target, String data) {

        out.print("<?");
        out.print(target);
        if (data != null && data.length() > 0) {
            out.print(' ');
            out.print(data);
        }
        out.print("?>");
        out.flush();

    } // processingInstruction(String,String)

    /** Start document. */
    public void startDocument() {

        if (!canonical) {
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            out.flush();
        }

    } // startDocument()

    /** Start element. */
    public void startElement(String uri, String local, String raw, 
                             Attributes attrs) {

        out.print('<');
        out.print(raw);
        if (attrs != null) {
            attrs = sortAttributes(attrs);
            int len = attrs.getLength();
            for (int i = 0; i < len; i++) {
                out.print(' ');
                out.print(attrs.getQName(i));
                out.print("=\"");
                out.print(normalize(attrs.getValue(i)));
                out.print('"');
            }
        }
        out.print('>');
        out.flush();

    } // startElement(String,String,String,Attributes)

    /** Characters. */
    public void characters(char ch[], int start, int length) {

        out.print(normalize(new String(ch, start, length)));
        out.flush();

    } // characters(char[],int,int);

    /** Ignorable whitespace. */
    public void ignorableWhitespace(char ch[], int start, int length) {

        characters(ch, start, length);
        out.flush();

    } // ignorableWhitespace(char[],int,int);

    /** End element. */
    public void endElement(String uri, String local, String raw) {

        out.print("</");
        out.print(raw);
        out.print('>');
        out.flush();

    } // endElement(String)

    //
    // ErrorHandler methods
    //

    /** Warning. */
    public void warning(SAXParseException ex) {
        System.err.println("[Warning] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /** Error. */
    public void error(SAXParseException ex) {
        System.err.println("[Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
    }

    /** Fatal error. */
    public void fatalError(SAXParseException ex) throws SAXException {
        System.err.println("[Fatal Error] "+
                           getLocationString(ex)+": "+
                           ex.getMessage());
        throw ex;
    }

    /** Returns a string of the location. */
    private String getLocationString(SAXParseException ex) {
        StringBuffer str = new StringBuffer();

        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1) 
                systemId = systemId.substring(index + 1);
            str.append(systemId);
        }
        str.append(':');
        str.append(ex.getLineNumber());
        str.append(':');
        str.append(ex.getColumnNumber());

        return str.toString();

    } // getLocationString(SAXParseException):String

    //
    // Protected static methods
    //

    /** Normalizes the given string. */
    protected String normalize(String s) {
        StringBuffer str = new StringBuffer();

        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '<': {
                    str.append("&lt;");
                    break;
                }
                case '>': {
                    str.append("&gt;");
                    break;
                }
                case '&': {
                    str.append("&amp;");
                    break;
                }
                case '"': {
                    str.append("&quot;");
                    break;
                }
                case '\r':
                case '\n': {
                    if (canonical) {
                        str.append("&#");
                        str.append(Integer.toString(ch));
                        str.append(';');
                        break;
                    }
                    // else, default append char
                }
                default: {
                    str.append(ch);
                }
            }
        }

        return str.toString();

    } // normalize(String):String

    /** Returns a sorted list of attributes. */
    protected Attributes sortAttributes(Attributes attrs) {

        AttributesImpl attributes = new AttributesImpl();

        int len = (attrs != null) ? attrs.getLength() : 0;
        for (int i = 0; i < len; i++) {
            String name = attrs.getQName(i);
            int count = attributes.getLength();
            int j = 0;
            while (j < count) {
                if (name.compareTo(attributes.getQName(j)) < 0) {
                    break;
                }
                j++;
            }
            attributes.insertAttributeAt(j, name, attrs.getType(i), 
                                         attrs.getValue(i));
        }

        return attributes;

    } // sortAttributes(AttributeList):AttributeList

    //
    // Main
    //

    /** Main program entry point. */
    public static void main(String argv[]) {

        // is there anything to do?
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }

        // vars
        String  parserName = DEFAULT_PARSER_NAME;
        boolean canonical  = false;

        // check parameters
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];

            // options
            if (arg.startsWith("-")) {
                if (arg.equals("-p")) {
                    if (i == argv.length - 1) {
                        System.err.println("error: missing parser name");
                        System.exit(1);
                    }
                    parserName = argv[++i];
                    continue;
                }

                if (arg.equals("-c")) {
                    canonical = true;
                    continue;
                }

                if (arg.equals("-h")) {
                    printUsage();
                    System.exit(1);
                }
            }

            // print uri
            System.err.println(arg+':');
            print(parserName, arg, canonical);
            System.out.println();
        }

    } // main(String[])

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java sax.SAX2Writer (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name  Specify SAX parser by name.");
        System.err.println("           Default parser: "+DEFAULT_PARSER_NAME);
        System.err.println("  -c       Canonical XML output.");
        System.err.println("  -h       This help screen.");

    } // printUsage()

} // class SAX2Writer
