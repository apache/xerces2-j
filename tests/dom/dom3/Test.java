/*
 * The Apache Software License, Version 1.1
 *
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


package dom.dom3;
import  org.apache.xerces.dom.*;
import  org.w3c.dom.*;
import  org.w3c.dom.ls.*;

import java.io.Reader;

import java.io.StringReader;

import dom.util.Assertion;

/**
 * The program tests vacarious DOM Level 3 functionality
 */
public class Test implements DOMErrorHandler{
    public static void main( String[] argv) {
        try {
            boolean namespaces = true;
            System.out.println("Running dom.dom3.Test...");
            System.setProperty(DOMImplementationRegistry.PROPERTY,"org.apache.xerces.dom.DOMImplementationSourceImpl");

            
            DOMImplementationLS impl = (DOMImplementationLS)DOMImplementationRegistry.newInstance().getDOMImplementation("LS-Load");

            Assertion.assert(impl!=null, "domImplementation != null");

            DOMBuilder builder = impl.createDOMBuilder(DOMImplementationLS.MODE_SYNCHRONOUS, 
                                                       null);

            DOMWriter writer = impl.createDOMWriter();
            builder.setFeature("http://xml.org/sax/features/namespaces",namespaces);
            builder.setFeature("http://xml.org/sax/features/validation",false);

            //************************
            // TEST: lookupNamespacePrefix
            //       isDefaultNamespace
            //       lookupNamespaceURI
            //************************
            System.out.println("TEST #1: lookupNamespacePrefix, isDefaultNamespace, lookupNamespaceURI, input: tests/dom/dom3/input.xml");
            {

                Document doc = builder.parseURI("tests/dom/dom3/input.xml");
                NodeList ls = doc.getElementsByTagName("a:elem_a"); 

                NodeImpl elem = (NodeImpl)ls.item(0);
                if (namespaces) {
                    //System.out.println("[a:elem_a].lookupNamespacePrefix('http://www.example.com', true) == null");
                    Assertion.assert(elem.lookupNamespacePrefix(
                                                               "http://www.example.com", true) == null, 
                                     "[a:elem_a].lookupNamespacePrefix(http://www.example.com)==null");


                    //System.out.println("[a:elem_a].isDefaultNamespace('http://www.example.com') == true");
                    Assertion.assert(elem.isDefaultNamespace("http://www.example.com") == true, 
                                     "[a:elem_a].isDefaultNamespace(http://www.example.com)==true");


                    //System.out.println("[a:elem_a].lookupNamespacePrefix('http://www.example.com', false) == ns1");
                    Assertion.assert(elem.lookupNamespacePrefix(
                                                               "http://www.example.com", false).equals("ns1"), 
                                     "[a:elem_a].lookupNamespacePrefix(http://www.example.com)==ns1");


                    Assertion.assert(elem.lookupNamespaceURI("xsi").equals("http://www.w3.org/2001/XMLSchema-instance"), 
                                     "[a:elem_a].lookupNamespaceURI('xsi') == 'http://www.w3.org/2001/XMLSchema-instance'" );

                } else {
                    Assertion.assert( elem.lookupNamespacePrefix(
                                                                "http://www.example.com", false) == null,"lookupNamespacePrefix(http://www.example.com)==null"); 
                }

                ls = doc.getElementsByTagName("bar:leaf");
                elem = (NodeImpl)ls.item(0);
                Assertion.assert(elem.lookupNamespacePrefix("url1:",false).equals("foo"), 
                                 "[bar:leaf].lookupNamespacePrefix('url1:', false) == foo");
                //System.out.println("[bar:leaf].lookupNamespacePrefix('url1:', false) == "+ );

                //System.out.println("==>Create b:baz with namespace 'b:' and xmlns:x='b:'");
                ls = doc.getElementsByTagName("baz");
                elem = (NodeImpl)ls.item(0);
                ls = doc.getElementsByTagName("elem8");
                elem = (NodeImpl)ls.item(0);
                Element e1 = doc.createElementNS("b:","p:baz");
                e1.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:x", "b:");
                elem.appendChild(e1);


                Assertion.assert(((NodeImpl)e1).lookupNamespacePrefix("b:",false).equals("p"), 
                                 "[p:baz].lookupNamespacePrefix('b:', false) == p");



                //System.out.println("[p:baz].lookupNamespacePrefix('b:', false) == "+ ((NodeImpl)e1).lookupNamespacePrefix("b:",false));

                Assertion.assert(elem.lookupNamespaceURI("xsi").equals("http://www.w3.org/2001/XMLSchema-instance"), 
                                 "[bar:leaf].lookupNamespaceURI('xsi') == 'http://www.w3.org/2001/XMLSchema-instance'" );

            }

            //************************
            //* Test normalizeDocument()
            //************************
            System.out.println("TEST #2: normalizeDocumention() - 3 errors, input: tests/dom/dom3/schema.xml");
            {
                builder.setFeature("validate", true);
                DocumentImpl core = (DocumentImpl)builder.parseURI("tests/dom/dom3/schema.xml");


                NodeList ls2 = core.getElementsByTagName("decVal");
                Element testElem = (Element)ls2.item(0);
                testElem.removeAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns");

                ls2 = core.getElementsByTagName("v02:decVal");
                testElem = (Element)ls2.item(0);
                testElem.setPrefix("myPrefix");
                Element root = core.getDocumentElement();

                Element newElem = core.createElementNS(null, "decVal");
                String data="4.5";
                if (true) {
                        data = "string";
                } 
                newElem.appendChild(core.createTextNode(data));
                root.insertBefore(newElem, testElem);

                newElem = core.createElementNS(null,  "notInSchema");
                newElem.appendChild(core.createTextNode("added new element"));
                root.insertBefore(newElem, testElem);

                root.appendChild(core.createElementNS("UndefinedNamespace", "NS1:foo"));
                core.setErrorHandler(new Test());
                core.setNormalizationFeature("validate", true);
                core.normalizeDocument();

                core.setNormalizationFeature("validate", false);
                core.setNormalizationFeature("comments", false);
                core.normalizeDocument();


                builder.setFeature("validate", false);
                

            }


            //************************
            //* Test normalizeDocument(): core tests
            //************************
            System.out.println("TEST #3: normalizeDocument() core");
            {

                Document doc= new DocumentImpl(); 
                Element root = doc.createElementNS("http://www.w3.org/1999/XSL/Transform", "xsl:stylesheet");
                doc.appendChild(root);
                root.setAttributeNS("http://attr1", "xsl:attr1","");

                Element child1 = doc.createElementNS("http://child1", "NS2:child1");
                child1.setAttributeNS("http://attr2", "NS2:attr2","");
                root.appendChild(child1);

                Element child2 = doc.createElementNS("http://child2","NS4:child2");
                child2.setAttributeNS("http://attr3","attr3", "");
                root.appendChild(child2);

                Element child3 = doc.createElementNS("http://www.w3.org/1999/XSL/Transform","xsl:child3");
                child3.setAttributeNS("http://a1","attr1", "");
                child3.setAttributeNS("http://a2","xsl:attr2", "");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a1", "http://a1");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsl", "http://a2");
                
                Element child4 = doc.createElementNS(null, "child4");
                child4.setAttributeNS("http://a1", "xsl:attr1", "");
                child4.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "default");

                child3.appendChild(child4);
                root.appendChild(child3);

                doc.normalizeDocument();
                //
                // make sure algorithm works correctly
                //

                // xsl:stylesheet should include 2 namespace declarations
                String name = root.getNodeName();
                Assertion.assert(name.equals("xsl:stylesheet"), "xsl:stylesheet");

                String value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.assert(value!=null, "xmlns:xsl != null");
                Assertion.assert(value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                
                value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");

                Assertion.assert(value!=null && 
                                 value.equals("http://attr1"), "xmlns:NS1="+value);

                // child includes 2 namespace decls

                Assertion.assert(child1.getNodeName().equals("NS2:child1"), "NS2:child1");
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS2");
                Assertion.assert(value!=null && 
                                 value.equals("http://child1"), "xmlns:NS2="+value);

                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS3");
                Assertion.assert(value!=null && 
                                 value.equals("http://attr2"), "xmlns:NS3="+value);


                // child3
                

                Assertion.assert(child3.getNodeName().equals("xsl:child3"), "xsl:child3");
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS6");
                Assertion.assert(value!=null && 
                                 value.equals("http://a2"), "xmlns:NS6="+value);


                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "a1");
                Assertion.assert(value!=null && 
                                 value.equals("http://a1"), "xmlns:a1="+value);


                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.assert(value!=null && 
                                 value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);

                
                Attr attr = child3.getAttributeNodeNS("http://a2", "attr2");
                Assertion.assert(attr != null, "NS6:attr2 !=null");

                Assertion.assert(child3.getAttributes().getLength() == 5, "xsl:child3 has 5 attrs");
                

            }


            //************************
            //* Test normalizeDocument(): core tests
            //************************
            System.out.println("TEST #4: namespace fixup during serialization");
            {

                Document doc= new DocumentImpl(); 
                Element root = doc.createElementNS("http://www.w3.org/1999/XSL/Transform", "xsl:stylesheet");
                doc.appendChild(root);
                root.setAttributeNS("http://attr1", "xsl:attr1","");

                Element child1 = doc.createElementNS("http://child1", "NS2:child1");
                child1.setAttributeNS("http://attr2", "NS2:attr2","");
                root.appendChild(child1);

                Element child2 = doc.createElementNS("http://child2","NS4:child2");
                child2.setAttributeNS("http://attr3","attr3", "");
                root.appendChild(child2);

                Element child3 = doc.createElementNS("http://www.w3.org/1999/XSL/Transform","xsl:child3");
                child3.setAttributeNS("http://a1","attr1", "");
                child3.setAttributeNS("http://a2","xsl:attr2", "");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:a1", "http://a1");
                child3.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsl", "http://a2");
                
                Element child4 = doc.createElementNS(null, "child4");
                child4.setAttributeNS("http://a1", "xsl:attr1", "");
                child4.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "default");

                child3.appendChild(child4);
                root.appendChild(child3);


                // serialize data
                String xmlData = writer.writeToString(doc);
                Reader r = new StringReader(xmlData);
                DOMInputSource in = impl.createDOMInputSource();
                in.setCharacterStream(r);
                doc = builder.parse(in);

                //
                // make sure algorithm works correctly
                //

                root = doc.getDocumentElement();
                child1 = (Element)root.getFirstChild();
                child2 = (Element)child1.getNextSibling();
                child3 = (Element)child2.getNextSibling();


                // xsl:stylesheet should include 2 namespace declarations
                String name = root.getNodeName();
                Assertion.assert(name.equals("xsl:stylesheet"), "xsl:stylesheet");

                String value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.assert(value!=null, "xmlns:xsl != null");
                Assertion.assert(value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);
                
                value = root.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS1");

                Assertion.assert(value!=null && 
                                 value.equals("http://attr1"), "xmlns:NS1="+value);

                // child includes 2 namespace decls

                Assertion.assert(child1.getNodeName().equals("NS2:child1"), "NS2:child1");
                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS2");
                Assertion.assert(value!=null && 
                                 value.equals("http://child1"), "xmlns:NS2="+value);

                value = child1.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS3");
                Assertion.assert(value!=null && 
                                 value.equals("http://attr2"), "xmlns:NS3="+value);


                // child3
                

                Assertion.assert(child3.getNodeName().equals("xsl:child3"), "xsl:child3");
                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "NS6");
                Assertion.assert(value!=null && 
                                 value.equals("http://a2"), "xmlns:NS6="+value);


                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "a1");
                Assertion.assert(value!=null && 
                                 value.equals("http://a1"), "xmlns:a1="+value);


                value = child3.getAttributeNS("http://www.w3.org/2000/xmlns/", "xsl");
                Assertion.assert(value!=null && 
                                 value.equals("http://www.w3.org/1999/XSL/Transform"), "xmlns:xsl="+value);

                
                Attr attr = child3.getAttributeNodeNS("http://a2", "attr2");
                Assertion.assert(attr != null, "NS6:attr2 !=null");

                Assertion.assert(child3.getAttributes().getLength() == 5, "xsl:child3 has 5 attrs");


                
                //OutputFormat format = new OutputFormat((Document)doc);
                //format.setLineSeparator(LineSeparator.Windows);
                //format.setIndenting(true);
                //format.setLineWidth(0);             
                //format.setPreserveSpace(true);

                //XMLSerializer serializer = new XMLSerializer(System.out, format);
                //serializer.serialize(doc);                

            }


            //************************
            // TEST: replaceWholeText()
            //       getWholeText()
            //       
            //************************
           
            System.out.println("TEST #4: wholeText, input: tests/dom/dom3/wholeText.xml");
           {
           
            builder.setFeature("validate", false);
            builder.setFeature("entities", true);
            DocumentImpl doc = (DocumentImpl)builder.parseURI("tests/dom/dom3/wholeText.xml");

            Element root = doc.getDocumentElement();
            Element test = (Element)doc.getElementsByTagName("elem").item(0);
            
            test.appendChild(doc.createTextNode("Address: "));
            test.appendChild(doc.createEntityReference("ent2"));
            test.appendChild(doc.createTextNode("City: "));
            
            test.appendChild(doc.createEntityReference("ent1"));
            DocumentType doctype = doc.getDoctype();
            Node entity = doctype.getEntities().getNamedItem("ent3");

            NodeList ls = test.getChildNodes();
            Assertion.assert(ls.getLength()==5, "List length");
            
            String compare1 = "Home Address: 1900 Dallas Road (East) City: Dallas. California. USA  PO #5668";
            Assertion.assert(((TextImpl)ls.item(0)).getWholeText().equals(compare1), "Compare1");
            String compare2 = "Address: 1900 Dallas Road (East) City: Dallas. California. USA  PO #5668";
            Assertion.assert(((TextImpl)ls.item(1)).getWholeText().equals(compare2), "Compare2");
            

            //TEST replaceWholeText()
            ((NodeImpl)ls.item(0)).setReadOnly(true, true);
            
            TextImpl original = (TextImpl)ls.item(0);
            Node newNode = original.replaceWholeText("Replace with this text");
            ls = test.getChildNodes();
            Assertion.assert(ls.getLength() == 1, "Length == 1");
            Assertion.assert(ls.item(0).getNodeValue().equals("Replace with this text"), "Replacement works");
            Assertion.assert(newNode != original, "New node created");

            // replace text for node which is not yet attached to the tree
            Text text = doc.createTextNode("readonly");
            ((NodeImpl)text).setReadOnly(true, true);
            text = text.replaceWholeText("Data");
            Assertion.assert(text.getNodeValue().equals("Data"), "New value 'Data'");

            // test with second child that does not have any content
            test = (Element)doc.getElementsByTagName("elem").item(1);
            try {            
                ((Text)test.getFirstChild()).replaceWholeText("can't replace");
            } catch (DOMException e){
               Assertion.assert(e !=null);
            }
            String compare3 = "Test: The Content ends here. ";
            //Assertion.assert(((Text)test.getFirstChild()).getWholeText().equals(compare3), "Compare3");
            
           }

        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }


    public boolean handleError(DOMError error){
        short severity = error.getSeverity();
        if (severity == error.SEVERITY_ERROR) {
            System.out.println(error.getMessage());
        }

        if (severity == error.SEVERITY_WARNING) {
            System.out.println("[Warning]: "+error.getMessage());
        }
        return true;

    }
}



