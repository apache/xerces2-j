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
import java.io.InputStream;
import java.io.IOException;

import org.apache.xerces.parsers.SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * Provides a complete trace of SAX2 events for files parsed.
 *
 * @author Andy Clark, IBM
 * @author Arnaud Le Hors, IBM
 *
 * @version $Id$
 */
public class DocumentTracer 
    extends DefaultHandler
    implements ContentHandler, DTDHandler, ErrorHandler, // standard
               DeclHandler, LexicalHandler // extensions (beta)
    {

    //
    // Data
    //

    /** SAX parser. */
    protected SAXParser fSAXParser;

    /** Indent level. */
    protected int fIndent;

    //
    // Constructors
    //

    /** Default constructor. */
    public DocumentTracer() {

    } // <init>()

    //
    // ContentHandler methods
    //

    public void setDocumentLocator(Locator locator) {
        printIndent();
        System.out.println("setDocumentLocator("+locator+')');
    }

    public void startDocument() throws SAXException {
        fIndent = 0;
        printIndent();
        System.out.println("startDocument()");
        fIndent++;
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
        printIndent();
        System.out.print("processingInstruction(");
        System.out.print("target="+quoteString(target));
        System.out.print(',');
        System.out.print("data="+quoteString(data));
        System.out.println(')');
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        printIndent();
        System.out.print("startPrefixMapping(");
        System.out.print("prefix="+quoteString(prefix));
        System.out.print(',');
        System.out.print("uri="+quoteString(uri));
        System.out.println(')');
        fIndent++;
    }

    public void startElement(String uri, String localpart, String rawname, Attributes attributes)
        throws SAXException {
        printIndent();
        System.out.print("startElement(");
        System.out.print("element={"+uri+','+localpart+','+rawname+'}');
        System.out.print(',');
        System.out.print("attributes=");
        if (attributes == null) {
            System.out.println("null");
        }
        else {
            System.out.print('{');
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                if (i > 0) {
                    System.out.print(',');
                }
                String attrURI = attributes.getURI(i);
                String attrLocalpart = attributes.getLocalName(i);
                String attrRawname = attributes.getQName(i);
                System.out.print('{'+attrURI+','+attrLocalpart+','+attrRawname+"}=");
                System.out.print(quoteString(attributes.getValue(i)));
            }
            System.out.print('}');
        }
        System.out.println(')');
        fIndent++;
    }

    public void characters(char[] ch, int offset, int length) throws SAXException {
        printIndent();
        System.out.print("characters(");
        System.out.print("text="+quoteString(new String(ch, offset, length)));
        System.out.println(')');
    }

    public void ignorableWhitespace(char[] ch, int offset, int length) throws SAXException {
        printIndent();
        System.out.print("ignorableWhitespace(");
        System.out.print("text="+quoteString(new String(ch, offset, length)));
        System.out.println(')');
    }

    public void endElement(String uri, String localpart, String rawname) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endElement(");
        System.out.print("element={"+uri+','+localpart+','+rawname+'}');
        System.out.println("})");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endPrefixMapping(");
        System.out.print("prefix="+quoteString(prefix));
        System.out.println(')');
    }

    public void skippedEntity(String name) throws SAXException {
        printIndent();
        System.out.print("skippedEntity(");
        System.out.print("name="+quoteString(name));
        System.out.println(')');
    }

    public void endDocument() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endDocument()");
    }

    //
    // DTDHandler methods
    //

    public void notationDecl(String name, String publicId, String systemId) 
        throws SAXException {
        printIndent();
        System.out.print("notationDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.println(')');
    }

    public void unparsedEntityDecl(String name, String publicId, String systemId, 
                                   String notationName) throws SAXException {
        printIndent();
        System.out.print("unparsedEntityDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.print(',');
        System.out.print("notationName="+quoteString(notationName));
        System.out.println(')');
    }

    //
    // LexicalHandler methods
    //

    public void startDTD(String name, String publicId, String systemId)
        throws SAXException {
        printIndent();
        System.out.print("startDTD(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.println(')');
        fIndent++;
    }

    public void startEntity(String name) throws SAXException {
        printIndent();
        System.out.print("startEntity(");
        System.out.print("name="+quoteString(name));
        System.out.println(')');
        fIndent++;
    }

    public void startCDATA() throws SAXException {
        printIndent();
        System.out.println("startCDATA()");
        fIndent++;
    }

    public void endCDATA() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endCDATA()");
    }

    public void comment(char[] ch, int offset, int length) throws SAXException {
        printIndent();
        System.out.print("comment(");
        System.out.print("text="+quoteString(new String(ch, offset, length)));
        System.out.println(')');
    }

    public void endEntity(String name) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endEntity(");
        System.out.print("name="+quoteString(name));
        System.out.println(')');
    }

    public void endDTD() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endDTD()");
    } // endDTD

    //
    // DeclHandler methods
    //

    public void elementDecl(String name, String contentModel)
        throws SAXException {
        printIndent();
        System.out.print("elementDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("contentModel="+quoteString(contentModel));
        System.out.println(')');
    } // elementDecl

    public void attributeDecl(String elementName, String attributeName, 
                              String type, String defaultValue, String defaultType)
        throws SAXException {
        printIndent();
        System.out.print("attributeDecl(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.print(',');
        System.out.print("attributeName="+quoteString(attributeName));
        System.out.print(',');
        System.out.print("type="+quoteString(type));
        System.out.print(',');
        System.out.print("defaultValue="+quoteString(defaultValue));
        System.out.print(',');
        System.out.print("defaultType="+quoteString(defaultType));
        System.out.println(')');
    } // attributeDecl

    public void internalEntityDecl(String name, String text)
        throws SAXException {
        printIndent();
        System.out.print("internalEntityDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("text="+quoteString(text));
        System.out.println(')');
    } // internalEntityDecl

    public void externalEntityDecl(String name, String publicId, String systemId)
        throws SAXException {
        printIndent();
        System.out.print("externalEntityDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.println(')');
    } // externalEntityDecl

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

    //
    // Static methods
    //

    public static String quoteString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuffer str = new StringBuffer();
        str.append('"');
        int length = s.length();
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\n': {
                    str.append("\\n");
                    break;
                }
                case '\r': {
                    str.append("\\r");
                    break;
                }
                case '\t': {
                    str.append("\\t");
                    break;
                }
                case '\\': {
                    str.append("\\\\");
                    break;
                }
                case '"': {
                    str.append("\\\"");
                    break;
                }
                default: {
                    str.append(c);
                }
            }
        }
        str.append('"');
        return str.toString();
    }

    //
    // Private methods
    //

    /** Prints the indent. */
    private void printIndent() {
        for (int i = 0; i < fIndent; i++) {
            System.out.print(' ');
        }
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
    // MAIN
    //

    /** Main. */
    public static void main(String[] argv) throws Exception {
        
        // construct handler
        DefaultHandler handler = new DocumentTracer();

        // construct parser; set features
        XMLReader parser = new SAXParser();
        try {
            parser.setFeature("http://xml.org/sax/features/namespaces", true);
        }
        catch (SAXException e) {
            e.printStackTrace(System.err);
        }
        try {
            parser.setFeature("http://xml.org/sax/features/validation", true);
        }
        catch (SAXException e) {
            e.printStackTrace(System.err);
        }

        // set handlers
        parser.setContentHandler(handler);
        parser.setDTDHandler(handler);
        parser.setErrorHandler(handler);
        try {
            parser.setProperty("http://xml.org/sax/properties/declaration-handler", handler);
        }
        catch (SAXException e) {
            e.printStackTrace(System.err);
        }
        try {
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        }
        catch (SAXException e) {
            e.printStackTrace(System.err);
        }

        // parser files
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            System.err.println("# argv["+i+"]: "+arg);
            //print(arg);
            try {
                parser.parse(arg);
            }
            catch (SAXException e) {
                Exception ex = e.getException();
                throw ex != null ? ex : e;
            }
        }
    } // main(String[])

    /** Prints the file. */
    private static void print(String filename) throws IOException {
        InputStream in = new FileInputStream(filename);
        int c = -1;
        while ((c = in.read()) != -1) {
            System.out.print((char)c);
        }
        System.out.println();
        in.close();
    } // print(String)

} // class DocumentTracer
