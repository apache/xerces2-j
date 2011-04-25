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

package org.apache.xerces.impl.xs.alternative;

import java.util.HashMap;
import java.util.Map;

import org.apache.xerces.dom.PSVIAttrNSImpl;
import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.impl.xpath.XPath20;
import org.apache.xerces.impl.xs.AbstractPsychoPathXPath2Impl;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Representation of XML Schema 1.1 'type alternatives', "test" attribute.
 * 
 * @xerces.internal
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @author Mukul Gandhi IBM
 * @version $Id$
 */
public class Test extends AbstractPsychoPathXPath2Impl {

	/** The type alternative to which the test belongs */
    protected final XSTypeAlternativeImpl fTypeAlternative;

    /** XPath 2.0 expression. Xerces-J native XPath 2.0 subset. */
    protected final XPath20 fXPath;
    
    /** XPath 2.0 expression. PsychoPath XPath 2.0 expression object. */
    protected final XPath fXPathPsychoPath;
        
    /** XPath 2.0 namespace context. Derived from XSDocumentInfo in XSD traversers. */
    protected final NamespaceSupport fXPath2NamespaceContext;

    /** Constructs a "test" for type alternatives */
    public Test(XPath20 xpath, XSTypeAlternativeImpl typeAlternative, NamespaceSupport namespaceContext) {
        fXPath = xpath;
        fXPathPsychoPath = null;
        fTypeAlternative = typeAlternative;
        fXPath2NamespaceContext = namespaceContext;
    }
    
    /*
     * Constructs a "test" for type alternatives. An overloaded constructor, for PsychoPath XPath processor.
     */
    public Test(XPath xpath, XSTypeAlternativeImpl typeAlternative, NamespaceSupport namespaceContext) {
        fXPath = null;
        fXPathPsychoPath = xpath;
        fTypeAlternative = typeAlternative;
        fXPath2NamespaceContext = namespaceContext;
    }

    public XSTypeAlternativeImpl getTypeAlternative() {
        return fTypeAlternative;
    }
	
    /*
     * Returns the test XPath expression object. Return the native Xerces XPath object or the PsychoPath XPath object, 
     * whichever is available. 
     */
    public Object getXPath() {
        Object xpath = null;
        
        if (fXPath != null) {
            xpath = fXPath;    
        } else if (fXPathPsychoPath != null) {
            xpath = fXPathPsychoPath;    
        }
        
        return xpath;
    }
    
    /** Evaluate the test expression with respect to the specified element and its attributes */
    public boolean evaluateTest(QName element, XMLAttributes attributes) {        
        if (fXPath != null) {
            return fXPath.evaluateTest(element, attributes);
        } else if (fXPathPsychoPath != null) {
            return evaluateTestExprWithPsychoPathEngine(element, attributes);  
        }
        else {
            return false;
        }        
    }

    public String toString() {
        return fXPath.toString();
    }
    
    /*
     * Evaluate the XPath "test" expression on an XDM instance, consisting of the specified element and its attributes.
     * Uses PsychoPath XPath 2.0 engine for the evaluation. 
     */
    private boolean evaluateTestExprWithPsychoPathEngine(QName element, XMLAttributes attributes) {
        
        boolean evaluationResult = false;

        try { 
            // an untyped PSVI DOM tree is constructed, to provide to PsychoPath engine for evaluation.
            Document document = new PSVIDocumentImpl();
            Element elem = document.createElementNS(element.uri, element.rawname);
            for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {         
                PSVIAttrNSImpl attrNode = new PSVIAttrNSImpl((PSVIDocumentImpl)document, attributes.getURI(attrIndx), attributes.getQName(attrIndx));
                attrNode.setNodeValue(attributes.getValue(attrIndx));
                elem.setAttributeNode(attrNode);
            }
            document.appendChild(elem);

            // construct parameter values for psychopath xpath processor
            Map psychoPathParams = new HashMap();
            psychoPathParams.put("XPATH2_NS_CONTEXT", fXPath2NamespaceContext);
            DynamicContext xpath2DynamicContext = initDynamicContext(null, document, psychoPathParams);
            if (fTypeAlternative.fXPathDefaultNamespace != null) {
                xpath2DynamicContext.add_namespace(null, fTypeAlternative.fXPathDefaultNamespace);  
            }
            evaluationResult = evaluateXPathExpr(fXPathPsychoPath, elem);
        } 
        catch(Exception ex) {
            evaluationResult = false;  
        }

        return evaluationResult;
       
    } // evaluateTestExprWithPsychoPathEngine
    
}
