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

package dom;                    
                    
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM filter. This sample program illustrates how to
 * use the Document#getElementsByTagName() method to quickly 
 * and easily locate elements by name.
 *
 * @version
 */
public class DOMFilter {

    //
    // Constants
    //

    /** Default parser name. */
    private static final String 
        DEFAULT_PARSER_NAME = "dom.wrappers.DOMParser";

    //
    // Public static methods
    //

    /** Prints the specified elements in the given document. */
    public static void print(String parserWrapperName, String uri, 
                             String elementName, String attributeName) {

        try {
            // parse document
            DOMParserWrapper parser = 
                (DOMParserWrapper)Class.forName(parserWrapperName).newInstance();
            Document document = parser.parse(uri);

            // get elements that match
            NodeList elements = document.getElementsByTagName(elementName);

            // print nodes
            print(elements, attributeName);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }

    } // print(String,String,String,String)

    //
    // Private static methods
    //

    /** 
     * Prints the contents of the given element node list. If the given
     * attribute name is non-null, then all of the elements are printed 
     * out
     */
    private static void print(NodeList elements, String attributeName) {

        // is there anything to do?
        if (elements == null) {
            return;
        }

        // print all elements
        if (attributeName == null) {
            int elementCount = elements.getLength();
            for (int i = 0; i < elementCount; i++) {
                Element element = (Element)elements.item(i);
                print(element, element.getAttributes());
            }
        }

        // print elements with given attribute name
        else {
            int elementCount = elements.getLength();
            for (int i = 0; i < elementCount; i++) {
                Element      element    = (Element)elements.item(i);
                NamedNodeMap attributes = element.getAttributes();
                if (attributes.getNamedItem(attributeName) != null) {
                    print(element, attributes);
                }
            }
        }

    } // print(NodeList,String)

    /** Prints the specified element. */
    private static void print(Element element, NamedNodeMap attributes) {

        System.out.print('<');
        System.out.print(element.getNodeName());
        if (attributes != null) {
            int attributeCount = attributes.getLength();
            for (int i = 0; i < attributeCount; i++) {
                Attr attribute = (Attr)attributes.item(i);
                System.out.print(' ');
                System.out.print(attribute.getNodeName());
                System.out.print("=\"");
                System.out.print(normalize(attribute.getNodeValue()));
                System.out.print('"');
            }
        }
        System.out.println('>');

    } // print(Element,NamedNodeMap)

    /** Normalizes the given string. */
    private static String normalize(String s) {
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
                    str.append("&#");
                    str.append(Integer.toString(ch));
                    str.append(';');
                    break;
                }
                default: {
                    str.append(ch);
                }
            }
        }

        return str.toString();

    } // normalize(String):String

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
        String parserName    = DEFAULT_PARSER_NAME;
        String elementName   = "*"; // all elements
        String attributeName = null;

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

                if (arg.equals("-e")) {
                    if (i == argv.length - 1) {
                        System.err.println("error: missing element name");
                        System.exit(1);
                    }
                    elementName = argv[++i];
                    continue;
                }

                if (arg.equals("-a")) {
                    if (i == argv.length - 1) {
                        System.err.println("error: missing attribute name");
                        System.exit(1);
                    }
                    attributeName = argv[++i];
                    continue;
                }

                if (arg.equals("-h")) {
                    printUsage();
                    System.exit(1);
                }
            }

            // print uri
            System.err.println(arg+':');
            print(parserName, arg, elementName, attributeName);
        }

    } // main(String[])

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java dom.DOMFilter (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name  Specify DOM parser wrapper by name.");
        System.err.println("           Default parser: "+DEFAULT_PARSER_NAME);
        System.err.println("  -e name  Specify element name to search for. Default is \"*\".");
        System.err.println("  -a name  Specify attribute name of specified elements.");
        System.err.println("  -h       This help screen.");

    } // printUsage()

} // class DOMFilter
