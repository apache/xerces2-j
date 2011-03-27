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

import java.util.Enumeration;
import java.util.Map;

import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSModel;
import org.eclipse.wst.xml.xpath2.processor.DefaultDynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DefaultEvaluator;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.Evaluator;
import org.eclipse.wst.xml.xpath2.processor.JFlexCupParser;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.StaticChecker;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class providing common services for XPath expression evaluation, with 'PsychoPath XPath 2.0' engine.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class AbstractPsychoPathXPath2Impl {
    
    private DynamicContext fXpath2DynamicContext = null;
    private Document fDomDoc = null;
    
    /*
     * Initialize the "PsychoPath XPath 2" dynamic context.
     */
    protected DynamicContext initDynamicContext(XSModel schema, Document document, Map psychoPathParams) {
        
        fXpath2DynamicContext = new DefaultDynamicContext(schema, document);        
        
        // populate the 'PsychoPath XPath 2' static context, with namespace bindings derived from the XML Schema document
        NamespaceSupport xpath2NamespaceContext = (NamespaceSupport) psychoPathParams.get("XPATH2_NS_CONTEXT");
        Enumeration currPrefixes = xpath2NamespaceContext.getAllPrefixes();
        while (currPrefixes.hasMoreElements()) {
            String prefix = (String)currPrefixes.nextElement();
            String uri = xpath2NamespaceContext.getURI(prefix);
            fXpath2DynamicContext.add_namespace(prefix, uri);
        }        
        fXpath2DynamicContext.add_function_library(new FnFunctionLibrary());
        fXpath2DynamicContext.add_function_library(new XSCtrLibrary());        
        fDomDoc = document;
        
        return fXpath2DynamicContext;
        
    } // initDynamicContext
    
    
    /*
     * Evaluate XPath expression with PsychoPath XPath2 engine.
     */
    protected boolean evaluateXPathExpr(XPath xpathObject, Element contextNode) throws StaticError, DynamicError, Exception {
        
        StaticChecker sc = new StaticNameResolver(fXpath2DynamicContext);
        sc.check(xpathObject);       
        Evaluator xpath2Evaluator = null;
        if (contextNode != null) {
            xpath2Evaluator = new DefaultEvaluator(fXpath2DynamicContext, fDomDoc);           
            // change focus to the top most element
            ResultSequence contextNodeResultSet = ResultSequenceFactory.create_new();
            contextNodeResultSet.add(new ElementType(contextNode, fXpath2DynamicContext.node_position(contextNode)));           
            fXpath2DynamicContext.set_focus(new Focus(contextNodeResultSet));
        }
        else {           
           xpath2Evaluator = new DefaultEvaluator(fXpath2DynamicContext, null);
        }
        
        ResultSequence resultSeq = xpath2Evaluator.evaluate(xpathObject);

        boolean result = false;
        if (resultSeq == null) {
            result = false;
        } else if (resultSeq.size() == 1) {
            AnyType rsReturn = resultSeq.get(0);
            if (rsReturn instanceof XSBoolean) {
                XSBoolean returnResultBool = (XSBoolean) rsReturn;
                result = returnResultBool.value();
            } else {
                result = false;
            }
        } else {
            result = false;
        }
        
        return result;
        
    } // evaluateXPathExpr
    
    
    /*
     * Compile an XPath string and return the compiled XPath expression.
     */
    protected XPath compileXPathStr(String xpathStr, XSAssertImpl assertImpl, XSDHandler fSchemaHandler, Element schemaContextElem) {        
        
        XPathParser xpathParser = new JFlexCupParser();
        XPath xpathObject = null;

        try {
            xpathObject = xpathParser.parse("boolean(" + xpathStr + ")", true);
        } catch (XPathParserException ex) {
            // error compiling XPath expression
            if (SchemaSymbols.ASSERT_XPATHEXPR_COMPILE_ERR_MESG_1.equals(ex.getMessage())) {               
                fSchemaHandler.reportSchemaError("cvc-xpath.3.13.4.2b", new Object[] {assertImpl.getTest().getXPath().toString(), XSTypeHelper.getSchemaTypeName(assertImpl.getTypeDefinition())}, schemaContextElem);
            }
            else {               
                fSchemaHandler.reportSchemaError("cvc-xpath.3.13.4.2a", new Object[] {assertImpl.getTest().getXPath().toString(), XSTypeHelper.getSchemaTypeName(assertImpl.getTypeDefinition())}, schemaContextElem);
            }
        }  

        return xpathObject;
        
    } // compileXPathStr
    
} // class AbstractPsychoPathXPath2Impl
