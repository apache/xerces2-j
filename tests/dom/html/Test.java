/*
 * The Apache Software License, Version 1.1
 * 
 * Copyright (c) 1999-2000 The Apache Software Foundation.  All rights 
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

/* $Id*/

package dom.html;
import org.w3c.dom.*;
import org.w3c.dom.html.*;
import org.apache.html.dom.HTMLDOMImplementationImpl;
import dom.util.Assertion;

/**
 * HTML DOM regression tests
 */
public class Test {
    /**
     * Create a basic HTML document.
     */
    private HTMLDocument createHTMLDocument() {
        HTMLDOMImplementation domImpl = HTMLDOMImplementationImpl.getHTMLDOMImplementation();
        return domImpl.createHTMLDocument("test document");
    }

    /**
     * Test for HTML DOM Elements overriding hidden NodeList methods
     * in ElementImpl.  This occured because HTMLFormElement and
     * HTMLSelectElement.
     */
    private void testNodeListShadowing() {
        // Build up a test document
        HTMLDocument document = createHTMLDocument();
        HTMLElement body = document.getBody();

        HTMLFormElement form = (HTMLFormElement)document.createElement("FORM");
        body.appendChild(form);
        Attr attr3 = document.createAttribute("action");
        form.setAttributeNode(attr3);
        
        Node node4 = document.createTextNode("http://www.nowhere.com");
        attr3.appendChild(node4);
        
        node4 = document.createComment(" comment 1 ");
        form.appendChild(node4);
        
        node4 = document.createComment(" comment 2 ");
        form.appendChild(node4);
        
        Element elem4 = document.createElement("INPUT");
        form.appendChild(elem4);
        Attr attr4 = document.createAttribute("type");
        elem4.setAttributeNode(attr4);
        
        Node node5 = document.createTextNode("SUBMIT");
        attr4.appendChild(node5);
        attr4 = document.createAttribute("value");
        elem4.setAttributeNode(attr4);
        
        node5 = document.createTextNode("Fred");
        attr4.appendChild(node5);
        
        node4 = document.createComment(" comment 3 ");
        form.appendChild(node4);
        
        HTMLSelectElement select = (HTMLSelectElement)document.createElement("SELECT");
        body.appendChild(select);
        
        node4 = document.createComment(" comment 1 ");
        select.appendChild(node4);
        
        node4 = document.createComment(" comment 2 ");
        select.appendChild(node4);
        
        elem4 = document.createElement("OPTION");
        select.appendChild(elem4);
        
        node5 = document.createTextNode("opt1");
        elem4.appendChild(node5);
        
        elem4 = document.createElement("OPTION");
        select.appendChild(elem4);
        
        node5 = document.createTextNode("opt2 ");
        elem4.appendChild(node5);
        
        node5 = document.createComment(" comment 3 ");
        elem4.appendChild(node5);
        
        Assertion.assert(form.getChildNodes().getLength() == 4,
                         "form.getChildNodes().getLength() != 4");
        Assertion.assert(form.getLength() == 1,
                         "form.getLength() != 1");
        Assertion.assert(select.getChildNodes().getLength() == 4,
                         "select.getChildNodes().getLength() != 4");
        Assertion.assert(select.getLength() == 2,
                         "select.getLength() != 2");
    }

    /**
     * Entry.
     */
    public static void main(String[] args) {
        Test test = new Test();
        test.testNodeListShadowing();
    }
}

