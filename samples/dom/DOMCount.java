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

import org.apache.xerces.dom.TextImpl;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A sample DOM counter. This sample program illustrates how to
 * traverse a DOM tree in order to information about the document.
 *
 * @version
 */
public class DOMCount {

    //
    // Constants
    //

    /** Default parser name. */
    private static final String
        DEFAULT_PARSER_NAME = "dom.wrappers.DOMParser";

    //
    // Data
    //

    /** Elements. */
    private long elements;

    /** Attributes. */
    private long attributes;

    /** Characters. */
    private long characters;

    /** Ignorable whitespace. */
    private long ignorableWhitespace;

    //
    // Public static methods
    //

    /** Counts the resulting document tree. */
    public static void count(String parserWrapperName, String uri) {

        try {
            DOMParserWrapper parser =
                (DOMParserWrapper)Class.forName(parserWrapperName).newInstance();
            DOMCount counter = new DOMCount();
            long before = System.currentTimeMillis();
            Document document = parser.parse(uri);
            counter.traverse(document);
            long after = System.currentTimeMillis();
            counter.printResults(uri, after - before);
        }
        catch (org.xml.sax.SAXParseException spe) {
        }
        catch (org.xml.sax.SAXException se) {
            if (se.getException() != null)
                se.getException().printStackTrace(System.err);
            else
                se.printStackTrace(System.err);
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }

    } // print(String,String,boolean)

    //
    // Public methods
    //

    /** Traverses the specified node, recursively. */
    public void traverse(Node node) {

        // is there anything to do?
        if (node == null) {
            return;
        }

        int type = node.getNodeType();
        switch (type) {
            // print document
            case Node.DOCUMENT_NODE: {
                elements            = 0;
                attributes          = 0;
                characters          = 0;
                ignorableWhitespace = 0;
                traverse(((Document)node).getDocumentElement());
                break;
            }

            // print element with attributes
            case Node.ELEMENT_NODE: {
                elements++;
                NamedNodeMap attrs = node.getAttributes();
                if (attrs != null) {
                    attributes += attrs.getLength();
                }
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        traverse(children.item(i));
                    }
                }
                break;
            }

            // handle entity reference nodes
            case Node.ENTITY_REFERENCE_NODE: {
                NodeList children = node.getChildNodes();
                if (children != null) {
                    int len = children.getLength();
                    for (int i = 0; i < len; i++) {
                        traverse(children.item(i));
                    }
                }
                break;
            }

            // print text
            case Node.CDATA_SECTION_NODE: {
                characters += node.getNodeValue().length();
                break;
            }
            case Node.TEXT_NODE: {
                if (node instanceof TextImpl) {
                    if (((TextImpl)node).isIgnorableWhitespace())
                        ignorableWhitespace += node.getNodeValue().length();
                    else
                        characters += node.getNodeValue().length();
                } else
                    characters += node.getNodeValue().length();
                break;
            }
        }

    } // traverse(Node)

    /** Prints the results. */
    public void printResults(String uri, long time) {

        // filename.xml: 631 ms (4 elems, 0 attrs, 78 spaces, 0 chars)
        System.out.print(uri);
        System.out.print(": ");
        System.out.print(time);
        System.out.print(" ms (");
        System.out.print(elements);
        System.out.print(" elems, ");
        System.out.print(attributes);
        System.out.print(" attrs, ");
        System.out.print(ignorableWhitespace);
        System.out.print(" spaces, ");
        System.out.print(characters);
        System.out.print(" chars)");
        System.out.println();

    } // printResults(String,long)

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

                if (arg.equals("-h")) {
                    printUsage();
                    System.exit(1);
                }
            }

            // count uri
            count(parserName, arg);
        }

    } // main(String[])

    /** Prints the usage. */
    private static void printUsage() {

        System.err.println("usage: java dom.DOMCount (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name  Specify DOM parser wrapper by name.");
        System.err.println("           Default parser: "+DEFAULT_PARSER_NAME);
        System.err.println("  -h       This help screen.");

    } // printUsage()

} // class DOMCount
