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

package xni;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.xerces.parsers.XMLDocumentParser;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XMLAttributes;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Provides a complete trace of XNI document and DTD events for 
 * files parsed.
 *
 * @author Andy Clark, IBM
 * @author Arnaud Le Hors, IBM
 *
 * @version $Id$
 */
public class DocumentTracer 
    extends XMLDocumentParser
    implements ErrorHandler {

    //
    // Data
    //

    /** Temporary QName. */
    private QName fQName = new QName();

    /** Indent level. */
    private int fIndent;

    //
    // Constructors
    //

    /** Default constructor. */
    public DocumentTracer() {
        setErrorHandler(this);
    } // <init>()

    //
    // Public methods
    //

    /** Resets the test parser. */
    public void reset() throws SAXException {
        super.reset();
        fIndent = 0;
    } // reset()

    //
    // XMLDocumentHandler methods
    //

    public void startDocument() throws SAXException {
        printIndent();
        System.out.println("startDocument()");
        fIndent++;
    }

    public void xmlDecl(String version, String encoding, String actualEncoding, String standalone)
        throws SAXException {
        printIndent();
        System.out.print("xmlDecl(");
        System.out.print("version="+quoteString(version));
        System.out.print(',');
        System.out.print("encoding="+quoteString(encoding));
        System.out.print(',');
        System.out.print("actualEncoding="+quoteString(actualEncoding));
        System.out.print(',');
        System.out.print("standalone="+quoteString(standalone));
        System.out.println(')');
    }

    public void doctypeDecl(String rootElement, String publicId, String systemId)
        throws SAXException {
        printIndent();
        System.out.print("doctypeDecl(");
        System.out.print("rootElement="+quoteString(rootElement));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
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

    public void startElement(QName element, XMLAttributes attributes)
        throws SAXException {
        printIndent();
        System.out.print("startElement(");
        System.out.print("element={"+element+'}');
        System.out.print(',');
        System.out.print("attributes=");
        if (attributes == null) {
            System.out.println("null");
        }
        else {
            System.out.print('{');
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                attributes.getName(i, fQName);
                if (i > 0) {
                    System.out.print(',');
                }
                System.out.print('{');
                System.out.print(fQName);
                System.out.print("}=");
                System.out.print(quoteString(attributes.getValue(i)));
                int entityCount = attributes.getEntityCount(i);
                for (int j = 0; j < entityCount; j++) {
                    System.out.print(",[");
                    System.out.print(quoteString(attributes.getEntityName(i, j)));
                    System.out.print(',');
                    System.out.print(attributes.getEntityOffset(i, j));
                    System.out.print(',');
                    System.out.print(attributes.getEntityLength(i, j));
                    System.out.print(']');
                }
            }
            System.out.print('}');
        }
        System.out.println(')');
        fIndent++;
    }

    public void characters(XMLString text) throws SAXException {
        printIndent();
        System.out.print("characters(");
        System.out.print("text="+quoteString(text.toString()));
        System.out.println(')');
    }

    public void ignorableWhitespace(XMLString text) throws SAXException {
        printIndent();
        System.out.print("ignorableWhitespace(");
        System.out.print("text="+quoteString(text.toString()));
        System.out.println(')');
    }

    public void endElement(QName element) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endElement(");
        System.out.print("element={"+element);
        System.out.println("})");
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endPrefixMapping(");
        System.out.print("prefix="+quoteString(prefix));
        System.out.println(')');
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

    public void endDocument() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endDocument()");
    }

    //
    // XMLDocumentHandler and XMLDTDHandler methods
    //

    public void startEntity(String name, String publicId, String systemId, 
                            String encoding) throws SAXException {
        printIndent();
        System.out.print("startEntity(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.print(',');
        System.out.print("encoding="+quoteString(encoding));
        System.out.println(')');
        fIndent++;
    }

    public void textDecl(String version, String encoding) throws SAXException {
        printIndent();
        System.out.print("textDecl(");
        System.out.print("version="+quoteString(version));
        System.out.print(',');
        System.out.print("encoding="+quoteString(encoding));
        System.out.println(')');
    }

    public void comment(XMLString text) throws SAXException {
        printIndent();
        System.out.print("comment(");
        System.out.print("text="+quoteString(text.toString()));
        System.out.println(')');
    }

    public void processingInstruction(String target, XMLString data)
        throws SAXException {
        printIndent();
        System.out.print("processingInstruction(");
        System.out.print("target="+quoteString(target));
        System.out.print(',');
        System.out.print("data="+quoteString(data.toString()));
        System.out.println(')');
    }

    public void endEntity(String name) throws SAXException {
        fIndent--;
        printIndent();
        System.out.print("endEntity(");
        System.out.print("name="+quoteString(name));
        System.out.println(')');
    }

    //
    // XMLDTDHandler methods
    //

    public void startDTD() throws SAXException {
        printIndent();
        System.out.println("startDTD()");
        fIndent++;
    } // startDTD

    public void elementDecl(String name, String contentModel)
        throws SAXException {
        printIndent();
        System.out.print("elementDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("contentModel="+quoteString(contentModel));
        System.out.println(')');
    } // elementDecl

    public void startAttlist(String elementName) throws SAXException {
        printIndent();
        System.out.print("startAttlist(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.println(')');
        fIndent++;
    } // startAttlist

    public void attributeDecl(String elementName, String attributeName, 
                              String type, String[] enumeration, 
                              String defaultType, XMLString defaultValue)
        throws SAXException {
        printIndent();
        System.out.print("attributeDecl(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.print(',');
        System.out.print("attributeName="+quoteString(attributeName));
        System.out.print(',');
        System.out.print("type="+quoteString(type));
        System.out.print(',');
        System.out.print("enumeration=");
        if (enumeration == null) {
            System.out.print("null");
        }
        else {
            System.out.print('{');
            for (int i = 0; i < enumeration.length; i++) {
                System.out.print(quoteString(enumeration[i]));
                if (i < enumeration.length - 1) {
                    System.out.print(',');
                }
            }
            System.out.print('}');
        }
        System.out.print(',');
        System.out.print("defaultType="+quoteString(defaultType));
        System.out.print(',');
        System.out.print("defaultValue=");
        if (defaultValue == null) {
            System.out.print("null");
        }
        else {
            System.out.print(quoteString(defaultValue.toString()));
        }
        System.out.println(')');
    } // attributeDecl

    public void endAttlist() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endAttlist()");
    } // endAttlist

    public void internalEntityDecl(String name, XMLString text)
        throws SAXException {
        printIndent();
        System.out.print("internalEntityDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("text="+quoteString(text.toString()));
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

    public void unparsedEntityDecl(String name, String publicId, String systemId, String notation)
        throws SAXException {
        printIndent();
        System.out.print("externalEntityDecl(");
        System.out.print("name="+quoteString(name));
        System.out.print(',');
        System.out.print("publicId="+quoteString(publicId));
        System.out.print(',');
        System.out.print("systemId="+quoteString(systemId));
        System.out.print(',');
        System.out.print("notation="+quoteString(notation));
        System.out.println(')');
    } // unparsedEntityDecl

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
    } // notationDecl

    public void startConditional(short type) throws SAXException {
        printIndent();
        System.out.print("startConditional(");
        System.out.print("type=");
        switch (type) {
            case XMLDTDHandler.CONDITIONAL_IGNORE: {
                System.out.print("CONDITIONAL_IGNORE");
                break;
            }
            case XMLDTDHandler.CONDITIONAL_INCLUDE: {
                System.out.print("CONDITIONAL_INCLUDE");
                break;
            }
            default: {
                System.out.print("??? ("+type+')');
            }
        }
        System.out.println(')');
        fIndent++;
    } // startConditional

    public void endConditional() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endConditional()");
    } // endConditional

    public void endDTD() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endDTD()");
    } // endDTD

    //
    // XMLDTDContentModelHandler methods
    //

    public void startContentModel(String elementName, short type)
        throws SAXException {
        printIndent();
        System.out.print("startContentModel(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.print(',');
        System.out.print("type=");
        switch (type) {
            case XMLDTDContentModelHandler.TYPE_ANY: {
                System.out.print("TYPE_ANY");
                break;
            }
            case XMLDTDContentModelHandler.TYPE_EMPTY: {
                System.out.print("TYPE_EMPTY");
                break;
            }
            case XMLDTDContentModelHandler.TYPE_MIXED: {
                System.out.print("TYPE_MIXED");
                break;
            }
            case XMLDTDContentModelHandler.TYPE_CHILDREN: {
                System.out.print("TYPE_CHILDREN");
                break;
            }
            default: {
                System.out.print("??? ("+type+')');
            }
        }
        System.out.println(')');
        fIndent++;
    } // startContentModel

    public void mixedElement(String elementName) throws SAXException {
        printIndent();
        System.out.print("mixedElement(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.println(')');
    } // mixedElement

    public void childrenStartGroup() throws SAXException {
        printIndent();
        System.out.println("childrenStartGroup()");
        fIndent++;
    } // childrenStartGroup

    public void childrenElement(String elementName) throws SAXException {
        printIndent();
        System.out.print("childrenElement(");
        System.out.print("elementName="+quoteString(elementName));
        System.out.println(')');
    } // childrenElement

    public void childrenSeparator(short separator) throws SAXException {
        printIndent();
        System.out.print("childrenSeparator(");
        System.out.print("separator=");
        switch (separator) {
            case XMLDTDContentModelHandler.SEPARATOR_CHOICE: {
                System.out.print("SEPARATOR_CHOICE");
                break;
            }
            case XMLDTDContentModelHandler.SEPARATOR_SEQUENCE: {
                System.out.print("SEPARATOR_SEQUENCE");
                break;
            }
            default: {
                System.out.print("??? ("+separator+')');
            }
        }
        System.out.println(')');
    } // childrenSeparator

    public void childrenOccurrence(short occurrence) throws SAXException {
        printIndent();
        System.out.print("childrenOccurrence(");
        System.out.print("occurrence=");
        switch (occurrence) {
            case XMLDTDContentModelHandler.OCCURS_ONE_OR_MORE: {
                System.out.print("OCCURS_ONE_OR_MORE");
                break;
            }
            case XMLDTDContentModelHandler.OCCURS_ZERO_OR_MORE: {
                System.out.print("OCCURS_ZERO_OR_MORE");
                break;
            }
            case XMLDTDContentModelHandler.OCCURS_ZERO_OR_ONE: {
                System.out.print("OCCURS_ZERO_OR_ONE");
                break;
            }
            default: {
                System.out.print("??? ("+occurrence+')');
            }
        }
        System.out.println(')');
    } // childrenOccurrence

    public void childrenEndGroup() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("childrenEndGroup()");
    } // childrenEndGroup

    public void endContentModel() throws SAXException {
        fIndent--;
        printIndent();
        System.out.println("endContentModel()");
    } // endContentModel

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
        XMLDocumentParser parser = new DocumentTracer();
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            System.err.println("# argv["+i+"]: "+arg);
            print(arg);
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
