/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.xs;

import java.util.Stack;
import java.util.Vector;

import org.apache.xerces.dom.PSVIAttrNSImpl;
import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.parser.XMLAssertAdapter;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DefaultDynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DefaultEvaluator;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.Evaluator;
import org.eclipse.wst.xml.xpath2.processor.JFlexCupParser;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.StaticChecker;
import org.eclipse.wst.xml.xpath2.processor.StaticNameResolver;
import org.eclipse.wst.xml.xpath2.processor.XPathParser;
import org.eclipse.wst.xml.xpath2.processor.XPathParserException;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.eclipse.wst.xml.xpath2.processor.function.FnFunctionLibrary;
import org.eclipse.wst.xml.xpath2.processor.function.XSCtrLibrary;
import org.eclipse.wst.xml.xpath2.processor.internal.Focus;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSBoolean;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The implementation of the XPath interface. This class interfaces with the
 * PsychoPath XPath 2.0 engine.
 * 
 * @version $Id$
 * @author Mukul Gandhi, IBM
 * @author Ken Cai, IBM
 */

public class XMLAssertPsychopathImpl extends XMLAssertAdapter {

    // class variable declarations
    DynamicContext fDynamicContext;
    XSModel fSchema = null;

    // a factory Document object to construct DOM tree nodes
    Document assertDocument = null;

    // an element to track construction of assertion DOM tree. This object changes
    // as per the XNI document events.
    Element currentAssertDomNode = null;

    // a stack holding the DOM root for assertions evaluation
    Stack assertRootStack = null;

    // a stack parallel to 'assertRootStack' storing all assertions for a single
    // assert tree
    Stack assertListStack = null;

    // XMLSchemaValidator reference. set from the XMLSchemaValidator object
    // itself.
    XMLSchemaValidator validator = null;

    /*
     * The class constructor
     */
    public XMLAssertPsychopathImpl() {
        // initializing the class variables
        this.assertDocument = new PSVIDocumentImpl();
        this.assertRootStack = new Stack();
        this.assertListStack = new Stack();
    }

    
    private void initXPathProcessor() throws Exception {
        validator = (XMLSchemaValidator) getProperty("http://apache.org/xml/properties/assert/validator");
        fDynamicContext = new DefaultDynamicContext(fSchema, assertDocument);
        // add variable "value" to the XPath context
        fDynamicContext.add_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName(
                                                                   "value"));        
        fDynamicContext.add_namespace("xs", "http://www.w3.org/2001/XMLSchema");
        fDynamicContext.add_namespace("fn", "http://www.w3.org/2005/xpath-functions");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#startElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
     */
    public void startElement(QName element, XMLAttributes attributes,
                                               Augmentations augs) {
        if (currentAssertDomNode == null) {
           currentAssertDomNode = assertDocument.createElementNS(
                                              element.uri, element.rawname);
           assertDocument.appendChild(currentAssertDomNode);
        } else {
            Element elem = assertDocument.createElementNS(element.uri, element.rawname);
            currentAssertDomNode.appendChild(elem);
            currentAssertDomNode = elem;
        }

        // add attributes to the element
        for (int attIndex = 0; attIndex < attributes.getLength(); attIndex++) {
            String attrUri = attributes.getURI(attIndex);
            String attQName = attributes.getQName(attIndex);
            String attValue = attributes.getValue(attIndex);
            
            PSVIAttrNSImpl attrNode = new PSVIAttrNSImpl((PSVIDocumentImpl)assertDocument, attrUri, attQName);
            attrNode.setNodeValue(attValue);
            
            // set PSVI information for the attribute
            Augmentations attrAugs = attributes.getAugmentations(attIndex);
            AttributePSVImpl attrPSVI = (AttributePSVImpl)attrAugs.getItem(Constants.ATTRIBUTE_PSVI);
            attrNode.setPSVI(attrPSVI);
            
            currentAssertDomNode.setAttributeNode(attrNode);
        }

        Object assertion = augs.getItem("ASSERT");
        // if we have assertion on this element, store the element reference
        // and the assertions on it, on the stack objects
        if (assertion != null) {
            assertRootStack.push(currentAssertDomNode);
            assertListStack.push(assertion);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#endElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws Exception {
        if (currentAssertDomNode != null) {
            // set PSVI information on the element
            ElementPSVI elemPSVI = (ElementPSVI)augs.getItem(Constants.ELEMENT_PSVI);
            ((PSVIElementNSImpl)currentAssertDomNode).setPSVI(elemPSVI);
            
            if (!assertRootStack.empty() && (currentAssertDomNode == assertRootStack.peek())) {               
                 // get XSModel                
                 fSchema =  ((PSVIElementNSImpl)currentAssertDomNode).getSchemaInformation();  
                 
                 /*              
                 // debugging code. can be present till the code is final.
                 // print the tree on which assertions are evaluated
                 try {
                   DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
                   DOMImplementationLS impl = (DOMImplementationLS) registry
                                       .getDOMImplementation("LS");
                   LSSerializer writer = impl.createLSSerializer();
                   LSOutput output = impl.createLSOutput();
                   output.setByteStream(System.out);
                   writer.write(currentAssertDomNode, output);
                   System.out.println("\n");
                 }
                 catch (Exception ex) {
                   ex.printStackTrace();
                 }
                 */
                 
                 assertRootStack.pop(); // pop the stack, to go one level up
                 Object assertions = assertListStack.pop(); // get assertions, and go one level up
                
                 // initialize the XPath engine
                 initXPathProcessor();
                 
                 // evaluate assertions
                 if (assertions instanceof XSObjectList) {
                    // assertions from a complex type definition
                    XSObjectList assertList = (XSObjectList) assertions;
                    for (int i = 0; i < assertList.size(); i++) {
                        XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);
                        evaluateAssertion(element, assertImpl);
                    }
                } else if (assertions instanceof Vector) {
                    // assertions from a simple type definition
                    Vector assertList = (Vector) assertions;                    
                    for (int i = 0; i < assertList.size(); i++) {
                        XSAssertImpl assertImpl = (XSAssertImpl) assertList.get(i);
                        evaluateAssertion(element, assertImpl);
                    }
                }
            }

            if (currentAssertDomNode.getParentNode() instanceof Element) {
              currentAssertDomNode = (Element)currentAssertDomNode.getParentNode();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLAssertAdapter#characters(org.apache.xerces.xni.XMLString)
     */
    public void characters(XMLString text) {
        // add a child text node to the assertions, DOM tree
        if (currentAssertDomNode != null) {
            currentAssertDomNode.appendChild(assertDocument
                    .createTextNode(new String(text.ch, text.offset,
                            text.length)));
        }
    }

    /*
     * Helper method to evaluate assertions
     */
    private void evaluateAssertion(QName element, XSAssertImpl assertImpl) {
        fDynamicContext.add_function_library(new FnFunctionLibrary());
        fDynamicContext.add_function_library(new XSCtrLibrary());

        XPathParser xpp = new JFlexCupParser();
        XPath xp = null;
        try {
            xp = xpp.parse("boolean("
                    + assertImpl.getTest().getXPath().toString() + ")");
        } catch (XPathParserException ex) {
            // error compiling XPath expression
            reportError("cvc-xpath.3.13.4.2", element, assertImpl);
        }
        
        StaticChecker sc = new StaticNameResolver(fDynamicContext);
        
        try {
            sc.check(xp);        
             
            // assign value to variable, "value"
            String value = "";
            NodeList childList = currentAssertDomNode.getChildNodes();
            if (childList.getLength() == 1) {
                Node node = childList.item(0);
                if (node.getNodeType() == Node.TEXT_NODE) {
                    value = node.getNodeValue();
                }
            }

            fDynamicContext.set_variable(
                    new org.eclipse.wst.xml.xpath2.processor.internal.types.QName(
                            "value"), new XSString(value));
            
            Evaluator eval = new DefaultEvaluator(fDynamicContext, assertDocument);
            
            // change focus to the top most element
            ResultSequence nodeEvalRS = ResultSequenceFactory.create_new();
            nodeEvalRS.add(new ElementType(currentAssertDomNode, 
                               fDynamicContext.node_position(currentAssertDomNode)));
            fDynamicContext.set_focus(new Focus(nodeEvalRS));

            ResultSequence rs = eval.evaluate(xp);

            boolean result = false;

            if (rs == null) {
                result = false;
            } else {
                if (rs.size() == 1) {
                    AnyType rsReturn = rs.get(0);
                    if (rsReturn instanceof XSBoolean) {
                        XSBoolean returnResultBool = (XSBoolean) rsReturn;
                        result = returnResultBool.value();
                    } else {
                        result = false;
                    }
                } else {
                    result = false;
                }
            }

            if (!result) {
                // assertion evaluation is false
                reportError("cvc-assertion.3.13.4.1", element, assertImpl);
            }

        } catch (Exception ex) {
            reportError("cvc-assertion.3.13.4.1", element, assertImpl);
        }

    }

    private void reportError(String key, QName element, XSAssertImpl assertImpl) {
        XSTypeDefinition typeDef = assertImpl.getTypeDefinition();
        String typeString = "";
        if (typeDef != null) {
           typeString = (typeDef.getName() != null) ? typeDef.getName() : "#anonymous";   
        }
        else {
           typeString = "#anonymous"; 
        }
        validator.reportSchemaError(key, new Object[] { element.rawname,
                               assertImpl.getTest().getXPath().toString(),
                               typeString });
    }
}
