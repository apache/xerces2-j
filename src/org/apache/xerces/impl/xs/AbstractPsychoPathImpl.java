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
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSTypeDefinition;
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
 * A base class providing common services for XPath expression evaluation, with 
 * 'PsychoPath XPath 2.0' engine.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class AbstractPsychoPathImpl {
    
    private DynamicContext fDynamicContext = null;
    private Document domDoc = null;
    
    
    /*
     * Initialize the 'PsychoPath XPath 2' dynamic context.
     */
    protected DynamicContext initDynamicContext(XSModel schema, Document document, Map psychoPathParams) {
        
        fDynamicContext = new DefaultDynamicContext(schema, document);        
        
        // populate the 'PsychoPath XPath 2' static context, with namespace bindings derived from the XML Schema document.
        NamespaceSupport xpath2NamespaceContext = (NamespaceSupport) psychoPathParams.get("XPATH2_NS_CONTEXT");
        Enumeration currPrefixes = xpath2NamespaceContext.getAllPrefixes();
        while (currPrefixes.hasMoreElements()) {
            String prefix = (String)currPrefixes.nextElement();
            String uri = xpath2NamespaceContext.getURI(prefix);
            fDynamicContext.add_namespace(prefix, uri);
        }
        
        fDynamicContext.add_function_library(new FnFunctionLibrary());
        fDynamicContext.add_function_library(new XSCtrLibrary());        
        domDoc = document;
        
        return fDynamicContext;
        
    } // initDynamicContext
    
    
    /*
     * Evaluate XPath expression with PsychoPath engine.
     */
    protected boolean evaluateXPathExpr(XPath xp, String xPathDefaultNamespace, Element contextNode)
                                        throws StaticError, DynamicError, Exception {
        
        StaticChecker sc = new StaticNameResolver(fDynamicContext);
        sc.check(xp);
       
        Evaluator eval = null;
        if (contextNode != null) {
           eval = new DefaultEvaluator(fDynamicContext, domDoc);           
           // change focus to the top most element
           ResultSequence nodeEvalRS = ResultSequenceFactory.create_new();
           nodeEvalRS.add(new ElementType(contextNode, fDynamicContext.node_position(contextNode)));
           if (xPathDefaultNamespace != null) {
             fDynamicContext.add_namespace(null, xPathDefaultNamespace);  
           }
           
           fDynamicContext.set_focus(new Focus(nodeEvalRS));
        }
        else {
           eval = new DefaultEvaluator(fDynamicContext, null);   
        }
        
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
        
        return result;
        
    } // evaluateXPathExpr
    
    
    /*
     * Compile an XPath string, and return the compiled XPath expression.
     */
    protected XPath compileXPathStr(String xpathStr, XSAssertImpl assertImpl, XSDHandler fSchemaHandler) {        
        XPathParser xpp = new JFlexCupParser();
        XPath xp = null;
        
        try {
            xp = xpp.parse("boolean(" + xpathStr + ")");
        } catch (XPathParserException ex) {
            // error compiling XPath expression
            reportError("cvc-xpath.3.13.4.2", assertImpl, fSchemaHandler);
        }  
        
        return xp;
        
    } // compileXPathStr
    
    
    /*
     * Method to report error messages.
     */
    private void reportError(String key, XSAssertImpl assertImpl, XSDHandler fSchemaHandler) {
        XSTypeDefinition typeDef = assertImpl.getTypeDefinition();
        String typeString = "";
        
        if (typeDef != null) {
           typeString = (typeDef.getName() != null) ? typeDef.getName() : "#anonymous";   
        }
        else {
           typeString = "#anonymous"; 
        }
        
        fSchemaHandler.reportSchemaError(key, new Object[] {assertImpl.getTest().getXPath().toString(), typeString }, null);
        
    } // reportError
    
} // class AbstractPsychoPathImpl
