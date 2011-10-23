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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.xml.XMLConstants;

import org.apache.xerces.dom.CoreDocumentImpl;
import org.apache.xerces.dom.PSVIAttrNSImpl;
import org.apache.xerces.dom.PSVIDocumentImpl;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xpath.XPath20;
import org.apache.xerces.impl.xs.AbstractPsychoPathXPath2Impl;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xni.NamespaceContext;
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
    
    /** String representation of the XPath */
    protected final String fExpression;

    /** XPath 2.0 expression. Xerces-J native XPath 2.0 subset. */
    protected final XPath20 fXPath;
    
    /** XPath 2.0 expression. PsychoPath XPath 2.0 expression object. */
    protected final XPath fXPathPsychoPath;
        
    /** XPath 2.0 namespace context. Derived from XSDocumentInfo in XSD traversers. */
    protected final NamespaceSupport fXPath2NamespaceContext;

    /** Constructs a "test" for type alternatives */
    public Test(XPath20 xpath, XSTypeAlternativeImpl typeAlternative, NamespaceSupport namespaceContext) {
        fXPath = xpath;
        fExpression = xpath == null ? "" : xpath.getXPathStrValue();
        fXPathPsychoPath = null;
        fTypeAlternative = typeAlternative;
        fXPath2NamespaceContext = namespaceContext;
    }
    
    /*
     * Constructs a "test" for type alternatives. An overloaded constructor, for PsychoPath XPath processor.
     */
    public Test(XPath xpath, String expression, XSTypeAlternativeImpl typeAlternative, NamespaceSupport namespaceContext) {
        fXPath = null;
        fExpression = expression == null ? "" : expression;
        fXPathPsychoPath = xpath;
        fTypeAlternative = typeAlternative;
        fXPath2NamespaceContext = namespaceContext;
    }

    public NamespaceSupport getNamespaceContext() {
        return fXPath2NamespaceContext;
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
    public boolean evaluateTest(QName element, XMLAttributes attributes, NamespaceContext instanceNamespaceContext, String expandedSystemId) {        
        if (fXPath != null) {
            return fXPath.evaluateTest(element, attributes);
        } else if (fXPathPsychoPath != null) {
            return evaluateTestWithPsychoPathXPathEngine(element, attributes, instanceNamespaceContext, expandedSystemId);  
        }
        else {
            return false;
        }        
    }

    public String toString() {
        return fExpression;
    }
    
    /*
     * Evaluate the XPath "test" expression on an XDM instance, for CTA evaluation. Uses PsychoPath XPath 2.0 engine for the evaluation. 
     */
    private boolean evaluateTestWithPsychoPathXPathEngine(QName element, XMLAttributes attributes, NamespaceContext instanceNamespaceContext, String expandedSystemId) {
        
        boolean evaluationResult = false;

        try { 
            // an untyped PSVI DOM tree (consisting only of the top most element node and it's attributes) is constructed,
            // to provide to PsychoPath XPath engine for evaluation.
            Document document = new PSVIDocumentImpl();
            document.setDocumentURI(expandedSystemId); // an approximation (the URI of the parent document) of the document URI for this <alternative>, document tree
            Element elem = new PSVIElementNSImpl((CoreDocumentImpl) document, element.uri, element.rawname);            
            for (int attrIndx = 0; attrIndx < attributes.getLength(); attrIndx++) {         
                PSVIAttrNSImpl attrNode = new PSVIAttrNSImpl((PSVIDocumentImpl)document, attributes.getURI(attrIndx), attributes.getQName(attrIndx));
                attrNode.setNodeValue(attributes.getValue(attrIndx));
                elem.setAttributeNode(attrNode);
            }

            // add in-scope namespaces on the document tree
            Enumeration currPrefixes = instanceNamespaceContext.getAllPrefixes();
            while (currPrefixes.hasMoreElements()) {
                String prefix = (String)currPrefixes.nextElement();
                String nsUri = instanceNamespaceContext.getURI(prefix);
                if (!(XMLConstants.XML_NS_PREFIX.equals(prefix) || XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))) {
                   String attrName = (prefix!=null && !SchemaSymbols.EMPTY_STRING.equals(prefix)) ? XMLConstants.XMLNS_ATTRIBUTE+":"+prefix : XMLConstants.XMLNS_ATTRIBUTE;  
                   elem.setAttribute(attrName, nsUri);  
                }
            } 
            
            document.appendChild(elem);

            // construct parameter values for psychopath xpath processor
            Map psychoPathParams = new HashMap();
            psychoPathParams.put(Constants.XPATH2_NAMESPACE_CONTEXT, fXPath2NamespaceContext);
            psychoPathParams.put(Constants.IS_CTA_EVALUATOR, Boolean.TRUE);
            DynamicContext xpath2DynamicContext = initXPath2DynamicContext(null, document, psychoPathParams);
            xpath2DynamicContext.set_base_uri(fTypeAlternative.getBaseURI()); // set base-uri property in XPath2 static context, to the URI of XSD document
            if (fTypeAlternative.fXPathDefaultNamespace != null) {
                addNamespaceBindingToXPath2DynamicContext(null, fTypeAlternative.fXPathDefaultNamespace);
            }
            evaluationResult = evaluateXPathExpr(fXPathPsychoPath, elem);
        } 
        catch(Exception ex) {
            evaluationResult = false;  
        }

        return evaluationResult;
       
    } // evaluateTestWithPsychoPathXPathEngine
    
}
