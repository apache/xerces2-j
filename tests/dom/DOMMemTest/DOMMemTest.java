/* $Id$ */
/*
 * The Apache Software License, Version 1.1
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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.ibm.com .  For more information
 * on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

//
//  Various DOM tests.
//     Contents include
//       1.  Basic functionality for DOMString
//       2.  Regression tests for bugs fixed.
//     All individual are wrapped in a memory leak checker.
//
//     This is NOT a complete test of DOM functionality.
//

package dom.DOMMemTest;
import org.w3c.dom.*;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xerces.dom.NotationImpl;


public class DOMMemTest {


    public static void main(String argv[])
{
    Assertion assertion = new Assertion();

    System.out.print("DOM Memory Test.\n");

    //
    //  Test Doc01      Create a new empty document
    //
    {
        Document    doc;
        doc = new DocumentImpl();
    }
    

    //
    //  Test Doc02      Create one of each kind of node using the
    //                  document createXXX methods.
    //                  Watch for memory leaks.
    //
    {
        //  Do all operations in a preconditioning step, to force the
        //  creation of implementation objects that are set up on first use.
        //  Don't watch for leaks in this block (no  / )
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
        DocumentFragment frag = doc.createDocumentFragment ();
        Text  text = doc.createTextNode("Doc02TextNode");
        Comment comment = doc.createComment("Doc02Comment");
        CDATASection  cdataSec = doc.createCDATASection("Doc02CDataSection");
        DocumentType  docType = doc.getImplementation().createDocumentType("Doc02DocumentType", null, null, null);
        Notation notation = ((DocumentImpl) doc).createNotation("Doc02Notation");
        ProcessingInstruction pi = doc.createProcessingInstruction("Doc02PITarget",
                                    "Doc02PIData");
        NodeList    nodeList = doc.getElementsByTagName("*");
    }


    
    {
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
    }
    

    
    {
        Document    doc = new DocumentImpl();
        DocumentFragment frag = doc.createDocumentFragment ();
    };
    


    
    {
        Document doc = new DocumentImpl();
        Element     el = doc.createElement("Doc02Element");
    }
    

    
    {
        Document doc = new DocumentImpl();
        Text  text = doc.createTextNode("Doc02TextNode");
    }
    

    
    {
        Document doc = new DocumentImpl();
        Comment comment = doc.createComment("Doc02Comment");
    }
    

    
    {
        Document doc = new DocumentImpl();
        CDATASection  cdataSec = doc.createCDATASection("Doc02CDataSection");
    }
    


    
    {
        Document doc = new DocumentImpl();
        DocumentType  docType = doc.getImplementation().createDocumentType("Doc02DocumentType", null, null, null);
    }
    


    
    {
        Document doc = new DocumentImpl();
        Notation notation = ((DocumentImpl)doc).createNotation("Doc02Notation");
    }
    


    
    {
        Document doc = new DocumentImpl();
        ProcessingInstruction pi = doc.createProcessingInstruction("Doc02PITarget",
                                    "Doc02PIData");
    }
    

    
    {
        Document doc = new DocumentImpl();
        Attr  attribute = doc.createAttribute("Doc02Attribute");
    }
    


    
    {
        Document doc = new DocumentImpl();
        EntityReference  er = doc.createEntityReference("Doc02EntityReference");
    }
    

    
    {
        Document doc = new DocumentImpl();
        NodeList    nodeList = doc.getElementsByTagName("*");
    }
    

    
    //
    //  Doc03 - Create a small document tree
    //
    
    {
        Document    doc = new DocumentImpl();
        Element     rootEl = doc.createElement("Doc03RootElement");
        doc.appendChild(rootEl);
        Text        textNode = doc.createTextNode("Doc03 text stuff");
        rootEl.appendChild(textNode);

        NodeList    nodeList = doc.getElementsByTagName("*");
    };
    


    //
    //  Attr01
    //
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        {
            Attr        attr01  = doc.createAttribute("Attr01");
            rootEl.setAttributeNode(attr01);
        }
        
        
        {
            Attr attr02 = doc.createAttribute("Attr01");
            rootEl.setAttributeNode(attr02);  
        }
        
    };

    //
    //  Attr02
    //
    
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        Attr        attr01  = doc.createAttribute("Attr02");
        rootEl.setAttributeNode(attr01);
        Attr        attr02 = doc.createAttribute("Attr02");
        rootEl.setAttributeNode(attr02);  
    }
    


    //
    //  Attr03
    //
    
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);
        Attr        attr01  = doc.createAttribute("Attr03");
        rootEl.setAttributeNode(attr01);

        attr01.setValue("Attr03Value1");
        attr01.setValue("Attr03Value2");
    }
    



    //
    //  Text01
    //
    
    {
        Document    doc = new DocumentImpl();
        Element     rootEl  = doc.createElement("RootElement");
        doc.appendChild(rootEl);


        Text        txt1 = doc.createTextNode("Hello Goodbye");
        rootEl.appendChild(txt1);

        txt1.splitText(6);
        rootEl.normalize();

    }
    


    //
    //  Notation01
    //
    
    { 
	/*
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        DocumentType    dt  =
	  impl.createDocumentType("DocType_for_Notation01", null, null, null);
        doc.appendChild(dt);


        NamedNodeMap notationMap = dt.getNotations();
        Notation    nt1 = ((DocumentImpl) doc).createNotation("Notation01");
        ((NotationImpl) nt1).setPublicId("Notation01PublicId");
        notationMap.setNamedItem (nt1);
        Notation    nt2 = (Notation)notationMap.getNamedItem("Notation01");
        assertion.assert(nt1==nt2);
        nt2 = new NotationImpl((DocumentImpl)doc, null);
        nt1 = null;
        nt2 = (Notation)notationMap.getNamedItem("Notation01");
      
    */
    }
    


    //
    //  NamedNodeMap01 - comparison operators.
    //
    
    {
        NamedNodeMap    nnm = null;
        assertion.assert(nnm == null);

        Document        doc = new DocumentImpl();
        nnm = doc.getAttributes();    // Should be null, because node type
                                      //   is not Element.
        assertion.assert(nnm == null);
        assertion.assert(!(nnm != null));

        Element el = doc.createElement("NamedNodeMap01");
        NamedNodeMap nnm2 = el.getAttributes();    // Should be an empty, but non-null map.
        assertion.assert(nnm2 != null);
        assertion.assert(nnm != nnm2);
        nnm = nnm2;
        assertion.assert(nnm == nnm2);
    }
    


    //
    //  importNode quick test
    //
    
    {
        Document    doc1 = new DocumentImpl();
        Document    doc2 = new DocumentImpl();
        
        Element     el1  = doc1.createElement("abc");
        doc1.appendChild(el1);
        assertion.assert(el1.getParentNode() != null);
        Node        el2  = doc2.importNode(el1, true);
        assertion.assert(el2.getParentNode() == null);
        String       tagName = el2.getNodeName();
        assertion.equals(tagName, "abc");
        assertion.assert(el2.getOwnerDocument() == doc2);
        assertion.assert(doc1 != doc2);
    }
    

    //
    //  getLength() tests.  Both Node CharacterData and NodeList implement
    //                  getLength().  Early versions of the DOM had a clash
    //                  between the two, originating in the implementation class
    //                  hirearchy, which has NodeList as a (distant) base class
    //                  of CharacterData.  This is a regression test to verify
    //                  that the problem stays fixed.
    //
    
    {
        Document     doc = new DocumentImpl();
        Text          tx = doc.createTextNode("Hello");
        Element       el = doc.createElement("abc");
        el.appendChild(tx);

        int     textLength = tx.getLength();
        assertion.assert(textLength == 5);

        NodeList      nl = tx.getChildNodes();
        int      nodeListLen = nl.getLength();
        assertion.assert(nodeListLen == 0);

        nl = el.getChildNodes();
        nodeListLen = nl.getLength();
        assertion.assert(nodeListLen == 1);
    }


    //
    //  NodeList - comparison operators, basic operation.
    //
    
    {
        NodeList    nl = null;
        NodeList    nl2 = null;
        assertion.assert(nl == null);
        assertion.assert(!(nl != null));
        assertion.assert(nl == nl2);

        Document        doc = new DocumentImpl();
        nl = doc.getChildNodes();    // Should be non-null, but empty

        assertion.assert(nl != null);
        int len = nl.getLength();
        assertion.assert(len == 0);

        Element el = doc.createElement("NodeList01");
        doc.appendChild(el);
        len = nl.getLength();
        assertion.assert(len == 1);
        assertion.assert(nl != nl2);
        nl2 = nl;
        assertion.assert(nl == nl2);
    }
    


 
    //
    //  Name validity checking.
    //
    
    {
         Document        doc = new DocumentImpl();
         try
         {
             Element el = doc.createElement("!@@ bad element name");
             assertion.assert(false);  // Exception above should prevent us reaching here.
         }
         catch ( DOMException e)
         {
             assertion.assert(e.code == DOMException.INVALID_CHARACTER_ERR);
         }
         catch ( Exception e )
         {
             assertion.assert(false);  // Wrong exception thrown.
         }
    }
    


    //
    //  Assignment ops return value
    //
    
    {
        Document        doc = new DocumentImpl();
        Element el = doc.createElement("NodeList01");
        doc.appendChild(el);
        
        Element n1, n2, n3;
        
        n1 = n2 = n3 = el;
        assertion.assert(n1 == n2);
        assertion.assert(n1 == n3);
        assertion.assert(n1 == el);
        assertion.assert(n1 != null);
        n1 = n2 = n3 = null;
        assertion.assert(n1 == null);
    }
    


    //
    //  Cloning of a node with attributes. Regression test for a ref counting 
    //  bug in attributes of cloned nodes that occured when the "owned" flag
    //  was not set in the clone.
    //
    
    {
        Document    doc = new DocumentImpl();
        Element     root = doc.createElement("CTestRoot");
        root.setAttribute("CTestAttr", "CTestAttrValue");

        String s = root.getAttribute("CTestAttr");
        assertion.equals(s, "CTestAttrValue");

        Element     cloned = (Element)root.cloneNode(true);
        Attr a = cloned.getAttributeNode("CTestAttr");
        assertion.assert(a != null);
        s = a.getValue();
        assertion.equals(s, "CTestAttrValue");
        a = null;

        a = cloned.getAttributeNode("CTestAttr");
        assertion.assert(a != null);
        s = a.getValue();
        assertion.equals(s, "CTestAttrValue");

    }
    


    //
    //  DOM Level 2 tests.  These should be split out as a separate test.
    //


    //
    // hasFeature.  The set of supported options tested here is for Xerces 1.1
    //
    
    {
        DOMImplementation  impl = DOMImplementationImpl.getDOMImplementation();
        assertion.assert(impl.hasFeature("XML", "2.0")    == true);
        assertion.assert(impl.hasFeature("XML", null)       == true);
        //  We also support 1.0
        assertion.assert(impl.hasFeature("XML", "1.0")    == true);
        assertion.assert(impl.hasFeature("XML", "3.0")    == false);
        assertion.assert(impl.hasFeature("Traversal", null) == true);


        assertion.assert(impl.hasFeature("HTML", null)           == false);
        assertion.assert(impl.hasFeature("Views", null)          == false);
        assertion.assert(impl.hasFeature("StyleSheets", null)    == false);
        assertion.assert(impl.hasFeature("CSS", null)            == false);
        assertion.assert(impl.hasFeature("CSS2", null)           == false);
        assertion.assert(impl.hasFeature("Events", null)         == true);
        assertion.assert(impl.hasFeature("UIEvents", null)       == false);
        assertion.assert(impl.hasFeature("MouseEvents", null)    == false);
        assertion.assert(impl.hasFeature("MutationEvents", null) == true);
        assertion.assert(impl.hasFeature("HTMLEvents", null)     == false);
        assertion.assert(impl.hasFeature("Range", null)          == false);
    }
    


    //
    // CreateDocumentType
    //
    
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        String intSubSet = "Internal subsets are not parsed by this call!";
        
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId, intSubSet);
        
        assertion.assert(dt != null);
        assertion.assert(dt.getNodeType() == Node.DOCUMENT_TYPE_NODE);
        assertion.equals(dt.getNodeName(), qName);
        assertion.equals(dt.getPublicId(), pubId);
        assertion.equals(dt.getSystemId(), sysId);
        assertion.equals(dt.getInternalSubset(), intSubSet);
        
        NamedNodeMap nnm = dt.getEntities();
        assertion.assert(nnm.getLength() == 0);
        nnm = dt.getNotations();
        assertion.assert(nnm.getLength() == 0);
    }
    
    

    //
    //  DOMImplementation.CreateDocument
    //
    
    {
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        String intSubSet = "Internal subsets are not parsed by this call!";
        
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId, intSubSet);
        
        String docNSURI = "http://document.namespace";
        Document doc = impl.createDocument(docNSURI, qName, dt);

        assertion.assert(doc.getNodeType() == Node.DOCUMENT_NODE);
        assertion.assert(doc.getDoctype() == dt);
        assertion.equals(doc.getNodeName(), "#document");
        assertion.assert(doc.getNodeValue() == null);

        Element el = doc.getDocumentElement();

        assertion.equals(el.getLocalName(), "docName");
        assertion.equals(el.getNamespaceURI(), docNSURI);
        assertion.equals(el.getNodeName(), qName);
        assertion.assert(el.getOwnerDocument() == doc);
        assertion.assert(el.getParentNode() == doc);
        assertion.equals(el.getPrefix(), "foo");
        assertion.equals(el.getTagName(), qName);
        assertion.assert(el.hasChildNodes() == false);

        //
        // Creating a second document with the same docType object should fail.
        //
        try
        {
            Document doc2 = impl.createDocument(docNSURI, qName, dt);
            assertion.assert(false);  // should not reach here.
        }
        catch ( DOMException e)
        {
            assertion.assert(e.code == DOMException.WRONG_DOCUMENT_ERR);
        }
        catch ( Exception e )
        {
            assertion.assert(false);  // Wrong exception thrown.
        }
    }
    
    



    //
    //  CreateElementNS methods
    //
    
    {
        
        // Set up an initial (root element only) document.
        // 
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        String intSubSet = "Internal subsets are not parsed by this call!";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId, intSubSet);
        
        String docNSURI = "http://document.namespace";
	Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();

        //
        // CreateElementNS
        //
        Element ela = doc.createElementNS("http://nsa", "a:ela");  // prefix and URI
        Element elb = doc.createElementNS("http://nsb", "elb");    //  URI, no prefix.
        Element elc = doc.createElementNS("", "elc");              // No URI, no prefix.

        rootEl.appendChild(ela);
        rootEl.appendChild(elb);
        rootEl.appendChild(elc);

        assertion.equals(ela.getNodeName(), "a:ela");
        assertion.equals(ela.getNamespaceURI(), "http://nsa");
        assertion.equals(ela.getPrefix(), "a");
        assertion.equals(ela.getLocalName(), "ela");
        assertion.equals(ela.getTagName(), "a:ela");

        assertion.equals(elb.getNodeName(), "elb");
        assertion.equals(elb.getNamespaceURI(), "http://nsb");
        assertion.assert(elb.getPrefix() == null);
        assertion.equals(elb.getLocalName(), "elb");
        assertion.equals(elb.getTagName(), "elb");

        assertion.equals(elc.getNodeName(), "elc");
        assertion.equals(elc.getNamespaceURI(), "");
        assertion.assert(elc.getPrefix() ==  null);
        assertion.equals(elc.getLocalName(), "elc");
        assertion.equals(elc.getTagName(), "elc");

        // Badly formed qualified name
        //EXCEPTION_TEST(doc.createElementNS("http://nsa", "a:a:a"), DOMException.NAMESPACE_ERR);     

        // Prefix == xml, namespace != http://www.w3.org/XML/1998/namespace
        //EXCEPTION_TEST(doc.createElementNS("http://nsa", "xml:a", DOMException.NAMESPACE_ERR));     

        // A couple of corner cases that should not fail.
        assertion.assert(doc.createElementNS("http://www.w3.org/XML/1998/namespace", "xml:a") != null);
	/*
        assertion.assert(doc.createElementNS("http://www.w3.org/XML/1998/namespace", "")      != null);
        assertion.assert(doc.createElementNS("http://www.w3.org/XML/1998/namespace", null)    != null);
	*/



    }
    



    //
    //  CreateAttributeNS methods
    //
    
    {
        
        // Set up an initial (root element only) document.
        // 
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        String intSubSet = "Internal subsets are not parsed by this call!";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId, intSubSet);
        
        String docNSURI = "http://document.namespace";
        Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();

        //
        // CreateAttributeNS
        //
        Attr attra = doc.createAttributeNS("http://nsa", "a:attra");       // prefix and URI
        Attr attrb = doc.createAttributeNS("http://nsb", "attrb");         //  URI, no prefix.
        Attr attrc = doc.createAttributeNS("", "attrc");    // No URI, no prefix.

        assertion.equals(attra.getNodeName(), "a:attra");
        assertion.equals(attra.getNamespaceURI(), "http://nsa");
        assertion.equals(attra.getPrefix(), "a");
        assertion.equals(attra.getLocalName(), "attra");
        assertion.equals(attra.getName(), "a:attra");

        assertion.equals(attrb.getNodeName(), "attrb");
        assertion.equals(attrb.getNamespaceURI(), "http://nsb");
        assertion.equals(attrb.getPrefix(), null);
        assertion.equals(attrb.getLocalName(), "attrb");
        assertion.equals(attrb.getName(), "attrb");

        assertion.equals(attrc.getNodeName(), "attrc");
        assertion.equals(attrc.getNamespaceURI(), "");
        assertion.equals(attrc.getPrefix(), null);
        assertion.equals(attrc.getLocalName(), "attrc");
        assertion.equals(attrc.getName(), "attrc");

    }
    


    //
    //  getElementsByTagNameNS
    //
    
    {
        
        // Set up an initial (root element only) document.
        // 
        DOMImplementation impl = DOMImplementationImpl.getDOMImplementation();
        
        String qName = "foo:docName";
        String pubId = "pubId";
        String sysId = "http://sysId";
        String intSubSet = "Internal subsets are not parsed by this call!";
        DocumentType dt = impl.createDocumentType(qName, pubId, sysId, intSubSet);
        
        String docNSURI = "http://document.namespace";
	Document doc = impl.createDocument(docNSURI, qName, dt);
        Element rootEl = doc.getDocumentElement();

        //
        // Populate the document
        //
        Element ela = doc.createElementNS("http://nsa", "a:ela");  
        rootEl.appendChild(ela);
        Element elb = doc.createElementNS("http://nsb", "elb");   
        rootEl.appendChild(elb);
        Element elc = doc.createElementNS("",           "elc");  
        rootEl.appendChild(elc);
        Element eld = doc.createElementNS("http://nsa", "d:ela");
        rootEl.appendChild(eld);
        Element ele = doc.createElementNS("http://nse", "elb");   
        rootEl.appendChild(ele);


    }
    




    //
    //  Print Final allocation stats for full test
    //
    //    DomMemDebug().print();
    
    };
}    
