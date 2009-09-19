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

import org.apache.xerces.xs.XSModel;
import org.eclipse.wst.xml.xpath2.processor.DefaultDynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DefaultEvaluator;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.DynamicError;
import org.eclipse.wst.xml.xpath2.processor.Evaluator;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.StaticChecker;
import org.eclipse.wst.xml.xpath2.processor.StaticError;
import org.eclipse.wst.xml.xpath2.processor.StaticNameResolver;
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
 * A base class, providing common services for XPath 2.0 evaluation, with PsychoPath.
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id: AbstractPsychoPathImpl.java 814163 2009-09-12 13:43:19Z mukulg $
 */
public class AbstractPsychoPathImpl {
    
    private DynamicContext fDynamicContext = null;
    private Document domDoc = null;
    
    protected DynamicContext initDynamicContext(XSModel schema,
                                                Document document) {
        fDynamicContext = new DefaultDynamicContext(schema, document);                
        fDynamicContext.add_namespace("xs", "http://www.w3.org/2001/XMLSchema");
        fDynamicContext.add_namespace("fn", "http://www.w3.org/2005/xpath-functions");
        fDynamicContext.add_function_library(new FnFunctionLibrary());
        fDynamicContext.add_function_library(new XSCtrLibrary());        
        domDoc = document;
        
        return fDynamicContext; 
    } //initDynamicContext
    
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
    
} //AbstractPsychoPathImpl
