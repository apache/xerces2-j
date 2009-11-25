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

import java.math.BigDecimal;
import java.math.BigInteger;
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
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDate;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDateTime;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDecimal;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSDouble;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSFloat;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInt;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSInteger;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSLong;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSString;
import org.eclipse.wst.xml.xpath2.processor.internal.types.XSTime;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A base class, providing common services for XPath 2 evaluation,
 * with PsychoPath XPath 2.0 engine.
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
     * Initialize the PsychoPath XPath 2 dynamic context
     */
    protected DynamicContext initDynamicContext(XSModel schema,
                                                Document document,
                                                Map psychoPathParams) {
        
        fDynamicContext = new DefaultDynamicContext(schema, document);        
        
        // populate the PsychoPath XPath 2.0 static context, with namespace bindings
        // derived from the XSD Schema document
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
    } //initDynamicContext
    
    /*
     * Evaluate XPath expression with PsychoPath engine
     */
    protected boolean evaluatePsychoPathExpr(XPath xp,
                                 String xPathDefaultNamespace,
                                 Element contextNode)
                                 throws StaticError, DynamicError {
        if (xPathDefaultNamespace != null) {
           fDynamicContext.add_namespace(null, xPathDefaultNamespace);  
        }
        StaticChecker sc = new StaticNameResolver(fDynamicContext);
        sc.check(xp);
       
        Evaluator eval = new DefaultEvaluator(fDynamicContext, domDoc);
        
        // change focus to the top most element
        ResultSequence nodeEvalRS = ResultSequenceFactory.create_new();
        nodeEvalRS.add(new ElementType(contextNode, 
                           fDynamicContext.node_position(contextNode)));

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
        
        return result;
    } //evaluatePsychoPathExpr
    
    /*
     * Compile the XPath string, and return the compiled XPath expression
     */
    protected XPath compileXPathStr(String xpathStr,
                                    XSAssertImpl assertImpl,
                                    XSDHandler fSchemaHandler) {        
        XPathParser xpp = new JFlexCupParser();
        XPath xp = null;
        
        try {
            xp = xpp.parse("boolean(" + xpathStr + ")");
        } catch (XPathParserException ex) {
            // error compiling XPath expression
            reportError("cvc-xpath.3.13.4.2", assertImpl, fSchemaHandler);
        }  
        
        return xp;
    }
    
    /*
     * Get psychopath xpath2 typed value, corresponding to the XSD language
     * type.
     * 
     * @param xsdTypeName a XSD built in type name, like "string", "date" etc.
     * @param value a "string value", that need to be converted to, the
     *        psychopath xpath2 XSD typed value.  
     */
    protected Object getPsychoPathTypeForXSDType(String xsdTypeName,
                                                 String value) {
        Object psychoPathType = null;
        
        if ("string".equals(xsdTypeName)) {
            psychoPathType = new XSString(value);   
        }
        else if ("date".equals(xsdTypeName)) {       
            psychoPathType = XSDate.parse_date(value);
        }
        else if ("int".equals(xsdTypeName)) {      
            psychoPathType = new XSInt(new BigInteger(value));
        }
        else if ("long".equals(xsdTypeName)) {     
           psychoPathType = new XSLong(new BigInteger(value));
        }
        else if ("integer".equals(xsdTypeName)) {      
           psychoPathType = new XSInteger(new BigInteger(value));
        }
        else if ("double".equals(xsdTypeName)) {       
           psychoPathType = new XSDouble(Double.parseDouble(value));
        }
        else if ("float".equals(xsdTypeName)) {        
           psychoPathType = new XSFloat(Float.parseFloat(value));
        }
        else if ("decimal".equals(xsdTypeName)) {      
           psychoPathType = new XSDecimal(new BigDecimal(value));
        }
        else if ("dateTime".equals(xsdTypeName)) {
           psychoPathType = XSDateTime.parseDateTime(value);
        }
        else if ("time".equals(xsdTypeName)) {
           psychoPathType = XSTime.parse_time(value);
        }
        else if ("boolean".equals(xsdTypeName)) {
           psychoPathType = new XSBoolean(Boolean.valueOf(value).booleanValue());
        }
        else if ("NOTATION".equals(xsdTypeName)) {
           psychoPathType = new XSString(value);
        }
        else {
           // create a XSString value, as fallback option 
           psychoPathType = new XSString(value);
        } 
        
        return psychoPathType;
    }
    
    /*
     * Method to report error messages
     */
    private void reportError(String key, XSAssertImpl assertImpl,
                                         XSDHandler fSchemaHandler) {
        XSTypeDefinition typeDef = assertImpl.getTypeDefinition();
        String typeString = "";
        
        if (typeDef != null) {
           typeString = (typeDef.getName() != null) ? typeDef.getName() : "#anonymous";   
        }
        else {
           typeString = "#anonymous"; 
        }
        
        fSchemaHandler.reportSchemaError(key, new Object[] {
                               assertImpl.getTest().getXPath().toString(),
                               typeString }, null);
    }
    
} //AbstractPsychoPathImpl
