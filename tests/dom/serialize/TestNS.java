/*
 * The Apache Software License, Version 1.1
 * 
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights 
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
 *    permission, please contact apache\@apache.org.
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
 * individuals on behalf of the Apache Software Foundation, and was
 * originally based on software copyright (c) 2002, International
 * Business Machines, Inc., http://www.ibm.com .  For more information
 * on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package dom.serialize;

import  org.w3c.dom.*;

import  org.apache.xerces.parsers.*;
import org.apache.xml.serialize.*;
import java.io.*;


/**
 * This class is testing namespace algorithm during serialization.
 * The class takes as a parameter xml document, parses it using the DOM parser.
 * By default it will perform modifications to the tree, and then serialize
 * the document using XMLSerializer.
 * 
 * @author Elena Litani, IBM
 * @version $Id$
 */
public class TestNS {
    public static void main( String[] argv) {
        try {

            System.out.println("DOM Serializer test for namespace algorithm.");
            DOMParser parser = new DOMParser();

            if (argv.length == 0) {
                printUsage();
                System.exit(1);
            }
            int repetition = 0;
            boolean namespaces = true;
            boolean validation = false;
            boolean schemaValidation = false;
            boolean deferred = true;
            boolean modify = true;
            boolean stdout = false;
            for (int i = 0; i < argv.length; i++) {
                String arg = argv[i];
                if (arg.startsWith("-")) {
                    String option = arg.substring(1);
                    if (option.equals("x")) {
                        if (++i == argv.length) {
                            System.err.println("error: Missing argument to -x option.");
                            continue;
                        }
                        String number = argv[i];
                        try {
                            int value = Integer.parseInt(number);
                            if (value < 1) {
                                System.err.println("error: Repetition must be at least 1.");
                                continue;
                            }
                            repetition = value;
                        }
                        catch (NumberFormatException e) {
                            System.err.println("error: invalid number ("+number+").");
                        }
                        continue;
                    }

                    if (option.equalsIgnoreCase("n")) {
                        namespaces = option.equals("n");
                        continue;
                    }
                    if (option.equalsIgnoreCase("v")) {
                        validation = option.equals("v");
                        continue;
                    }
                    if (option.equalsIgnoreCase("s")) {
                        schemaValidation = option.equals("s");
                        continue;
                    }
                    if (option.equalsIgnoreCase("m")) {
                        modify = option.equals("m");
                        continue;
                    }
                    if (option.equalsIgnoreCase("o")) {
                        stdout = option.equals("o");
                        continue;
                    }
                }
                parser.setFeature( "http://xml.org/sax/features/validation", validation );
                parser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", deferred);
                parser.setFeature("http://xml.org/sax/features/external-general-entities", true);
                parser.setFeature( "http://xml.org/sax/features/namespaces",namespaces);
                parser.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", true);
                parser.parse( argv[i] );

                Document core = parser.getDocument();
                if (modify) {

                    System.out.println("Modifying document...");
                    Node element = core.getFirstChild();
                    while (element!=null && element.getNodeType() != Node.ELEMENT_NODE) {
                        element = element.getNextSibling();
                    }
                    if (element == null) {
                        System.err.println("test failed no Element node was found");
                        System.exit(1);
                    }
                    else {
                        System.out.println("Modifying information for the element: "+element.getNodeName());
                    }

                    // add element in same scope                            
                    element.appendChild(core.createComment("add element in the same scope"));
                    element.appendChild(core.createTextNode("\n"));
                    element.appendChild(core.createElementNS("urn:schemas-xmlsoap-org:soap.v1","s:bar"));
                    element.appendChild(core.createTextNode("\n\n"));


                    // add element with s bound to different namespace
                    element.appendChild(core.createComment("add element with s bound to different namespace"));
                    element.appendChild(core.createTextNode("\n"));
                    element.appendChild(core.createElementNS("s_bound_to_this","s:hello"));
                    element.appendChild(core.createTextNode("\n\n"));

                    // add element with no prefix bound to different scope than default prefix
                    element.appendChild(core.createComment("add element with no prefix bound to different scope than default prefix"));
                    element.appendChild(core.createTextNode("\n"));
                    element.appendChild(core.createElementNS("empty_rebound","emptyNS"));
                    element.appendChild(core.createTextNode("\n\n"));


                    element.appendChild(core.createComment("add element no prefix no namespace"));
                    element.appendChild(core.createTextNode("\n"));
                    element.appendChild(core.createElementNS(null,"defaultNamespace"));
                    element.appendChild(core.createTextNode("\n\n"));

                    // add doml2 element with default namespace

                    element.appendChild(core.createComment("add element with prefix s and empty namespace"));
                    element.appendChild(core.createTextNode("\n"));
                    element.appendChild(core.createElementNS("","s:noNamespace"));
                    element.appendChild(core.createTextNode("\n\n"));

                    Text text;
                    // add xmlns attribute
                    element.appendChild(core.createComment("create element: _attr_ bound to _hi_, local declaration of xmlns:attr = boo"));
                    element.appendChild(core.createTextNode("\n"));
                    Element elm = core.createElementNS("hi","attr:foo");
                    Attr attr = core.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:attr");
                    attr.setValue("boo");
                    elm.setAttributeNode(attr);
                    element.appendChild(elm);
                    element.appendChild(core.createTextNode("\n\n"));


                    // work on attributes algorithm
                    element.appendChild(core.createComment("\n1) noBooPrefixAttr had no prefix and bound to _boo_ URI (boo is not declared)."+
                                                           "\n2) dummy attribute with null namespace"+
                                                           "\n3) boowithPrefixS with declared s - no change\n"));
                    element.appendChild(core.createTextNode("\n"));
                    elm = core.createElementNS("urn:schemas-xmlsoap-org:soap.v1","s:testAttributes");

                    attr = core.createAttributeNS(null, "dummy");
                    elm.setAttributeNode(attr);
                    attr = core.createAttributeNS("urn:schemas-xmlsoap-org:soap.v1", "s:BooWithPrefixS"); 
                    elm.setAttributeNode(attr);
                    attr = core.createAttributeNS("boo", "noBooPrefixAttr"); 
                    elm.setAttributeNode(attr);
                    element.appendChild(elm);
                    element.appendChild(core.createTextNode("\n\n"));


                    element.appendChild(core.createComment("xml:space attributes, doml1 namespace declaration (NOT well-formed!!), doml2 ns decls"));
                    element.appendChild(core.createTextNode("\n"));
                    elm = core.createElementNS("","spaces");
                    attr = core.createAttributeNS("http://www.w3.org/XML/1998/namespace", "boo:space");
                    elm.setAttributeNode(attr);
                    attr = core.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:foo");
                    elm.setAttributeNode(attr);

                    attr = core.createAttribute("s:level1Node");
                    elm.setAttributeNode(attr);

                    attr = core.createAttribute("xmlns:");
                    elm.setAttributeNode(attr);
                    attr = core.createAttribute("xmlns:k");
                    elm.setAttributeNode(attr);
                    attr = core.createAttribute("xmlns:k:d");
                    elm.setAttributeNode(attr);

                    element.appendChild(elm);
                    element.appendChild(core.createTextNode("\n\n"));



                    // more attributes tests

                    element.appendChild(core.createComment("\n1) Declare _Table_ attribute with no prefix and bound to default namespace"+
                                                           "\n2) declare attribute xmlns1000:NS bound to xmlns namespace"+
                                                           "\n3) Attribute _noFooPrefixAttr_ had no prefix and bound to _foo_ URI. There is local default decl bound to _foo_"));
                    element.appendChild(core.createTextNode("\n"));
                    elm = core.createElementNS("urn:schemas-xmlsoap-org:soap.v1","s:testAttributes2");
                    attr = core.createAttributeNS("foo", "noFooPrefixAttr");
                    elm.setAttributeNode(attr);
                    attr = core.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns");
                    attr.setValue("foo");
                    elm.setAttributeNode(attr);


                    attr = core.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns1000:namespaceAttr2");
                    elm.setAttributeNode(attr);
                    attr = core.createAttributeNS("hello_world", "Table");

                    elm.setAttributeNode(attr);
                    element.appendChild(elm);

                    // bould to xmlns namespace
                    Element elm2 = core.createElementNS("have_no_correct_namespace","s:testAttributes3");
                    text = core.createTextNode("Has xmlns2000:myAttribute attribute with xmlns namespace(prefix not defined), element has no defined prefix");            
                    attr = core.createAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns2000:myAttribute");
                    elm2.appendChild(text);
                    elm2.setAttributeNode(attr);
                    elm.appendChild(core.createTextNode("\n"));
                    elm.appendChild(elm2);
                    element.appendChild(core.createTextNode("\n"));

                }
                // Serializer that ouputs tree in not pretty print format
                OutputFormat format = new OutputFormat((Document)core);
                format.setIndenting(true);
                format.setLineWidth(0);             
                format.setPreserveSpace(true);
                if (stdout) {
                
                  XMLSerializer serializer = new XMLSerializer(System.out, format);
                  serializer.asDOMSerializer();
                  serializer.serialize((Document)core);
                } else {
                
                System.out.println("Serializing output to output.xml...");
                XMLSerializer toFile = new XMLSerializer (new FileWriter("output.xml"), format);
                toFile.asDOMSerializer();
                toFile.serialize((Document)core);
                }
                /** Test SAX Serializer */
                /* System.out.println("Testing SAX Serializer");
                XMLSerializer saxSerializer;
                if (stdout) {                
                    saxSerializer = new XMLSerializer (System.out, format);
                } else {
                    saxSerializer = new XMLSerializer (new FileWriter("sax_output.xml"), format);
                }

                saxSerializer.startDocument();
                //saxSerializer.processingInstruction("foo", "bar");
                saxSerializer.startDTD("foo", "bar", "baz");
                saxSerializer.startElement("myNamespace", "a", "foo:a", null);
                saxSerializer.startElement("myNamespace", "b", "foo:b", null);
                
                saxSerializer.startElement("myNamespace", "c", "foo:c", null);
                saxSerializer.endElement("myNamespace", "c", "foo:c");
                saxSerializer.endElement("myNamespace", "b", "foo:b");

                saxSerializer.endElement("myNamespace", "a", "foo:a");
                saxSerializer.endDocument();
                System.out.println("Serializing output to sax_output.xml...");
                */

            }


        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static void printUsage() {

        System.err.println("usage: java dom.serialize.TestNS (options) uri ...");
        System.err.println();

        System.err.println("options:");
        System.err.println("  -x number   Select number of repetitions.");
        System.err.println("              NOTE: Requires use of -n.");
        System.err.println("  -o  | -O    Turn on/off serialization to standard output.");
        System.err.println("  -m  | -M    Turn on/off document modification.");
        System.err.println("  -n  | -N    Turn on/off namespace processing.");
        System.err.println("  -v  | -V    Turn on/off validation.");
        System.err.println("  -s  | -S    Turn on/off Schema validation support.");        
        System.err.println();



    } // printUsage()
}



