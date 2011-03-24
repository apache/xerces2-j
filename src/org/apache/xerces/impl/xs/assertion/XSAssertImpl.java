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

package org.apache.xerces.impl.xs.assertion;

import org.apache.xerces.impl.xs.AbstractPsychoPathXPath2Impl;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;
import org.w3c.dom.Element;

/**
 * XML Schema 1.1 'assertion' component.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XSAssertImpl extends AbstractPsychoPathXPath2Impl implements XSAssert {

    // The kind of assertion this is
    private short fAssertKind = XSConstants.ASSERTION;
    
    // The type definition associated with the assertion component
    private XSTypeDefinition fTypeDefinition;

    // Xerces object representing the assert 'test' attribute
    private Test fTestExpr = null;
    
    // Compiled XPath 2.0 expression
    private XPath fCompiledXPathExpr = null;
    
    // Optional annotations
    private XSObjectList fAnnotations = null;

    // Default XPath namespace
    private String fXPathDefaultNamespace = null;
      
    // XPath 2.0 namespace context. Derived from XSDocumentInfo in Xerces schema "component traversers".
    private NamespaceSupport fXPath2NamespaceContext = null;
    
    // a non-null value of this object indicates that this assertion belongs to an attribute's schema type, and
    // value of this object would be the attribute's name.
    private String fAttrName = null;
    
    // a non-null value of this object indicates that this assertion belongs to an attribute's schema type, and
    // value of this object would be the attribute's value.
    private String fAttrValue = null;
    
    // XSDHandler object passed on from the Xerces XSModel traversers. 
    private XSDHandler fSchemaHandler = null;
    
    // an user-defined message to provide to the user context, during assertion failures.
    private String fMessage = null;
    
    // represents the schema type's variety if an assertion belongs to a schema simpleType.
    private short fVariety = 0;

    /*
     * Class constructor.
     */
    public XSAssertImpl(XSTypeDefinition type, XSObjectList annotations, XSDHandler schemaHandler) {
        // an assert component corresponds to a schema type        
        fTypeDefinition = type;
        
        fSchemaHandler = schemaHandler; 
        fAnnotations = annotations;
    }
    
    public void setTest(Test expr, Element schemaContextElem) {
        fTestExpr = expr;        
        // compile the XPath string, and keep compiled representation into this object for later use (this helps us to
        // optimize assertions evaluations).
        setCompiledExpr(compileXPathStr(expr.toString(), this, fSchemaHandler, schemaContextElem));
    }
    
    public void setCompiledExpr(XPath compiledXPathExpr) {
        fCompiledXPathExpr = compiledXPathExpr;  
    }

    public void setAnnotations(XSObjectList annotations) {
        fAnnotations = annotations;
    }

    public void setXPathDefaultNamespace(String namespace) {
        fXPathDefaultNamespace = namespace;
    }
      
    public void setXPath2NamespaceContext(NamespaceSupport namespaceContext) {
        fXPath2NamespaceContext = namespaceContext;       
    }
    
    /**
     * Sets the kind of assertion this is. This could be one of the following:
     * -> an assertion from a complexType      :   XSConstants.ASSERTION
     * -> an assertion facet from a "complexType -> simpleContent" 
     *                                         :   XSConstants.ASSERTION_FACET
     * -> an assertion facet from a simpleType :   XSConstants.ASSERTION_FACET
     */
    public void setAssertKind(short assertKind) {
        this.fAssertKind = assertKind;
    }
    
    public void setAttrName(String attrName) {
        this.fAttrName = attrName;   
    }
    
    public void setAttrValue(String attrValue) {
        this.fAttrValue = attrValue;   
    }
    
    public void setTypeDefinition(XSTypeDefinition typeDefn) {
        fTypeDefinition = typeDefn;  
    }

    public void setMessage(String message) {
       this.fMessage = message;    
    }
    
    public void setVariety(short variety) {
        fVariety = variety;  
    }
    
    public XSObjectList getAnnotations() {
        return fAnnotations;
    }

    public String getTestStr() {
        return fTestExpr.toString();
    }
    
    public XPath getCompiledXPathExpr() {
        return fCompiledXPathExpr;
    }

    public Test getTest() {
        return fTestExpr;
    }
    
    public XSTypeDefinition getTypeDefinition() {
        return fTypeDefinition;
    }
    
    public String getXPathDefaultNamespace() {
        return fXPathDefaultNamespace;
    }

    /**
     * @see org.apache.xerces.xs.XSObject#getName()
     */
    public String getName() {
        return null;
    }
    
    /**
     * @see org.apache.xerces.xs.XSObject#getNamespace()
     */
    public String getNamespace() {
        return null;
    }

    /**
     * @see org.apache.xerces.xs.XSObject#getNamespaceItem()
     */
    public XSNamespaceItem getNamespaceItem() {
        return null;
    }

    public short getType() {
        return fAssertKind;
    }
    
    public String getAttrName() {
        return fAttrName;  
    }
    
    public String getAttrValue() {
        return fAttrValue;  
    }
       
    public NamespaceSupport getXPath2NamespaceContext() {
        return fXPath2NamespaceContext;
    }
    
    public String getMessage() {
       return fMessage;   
    }
    
    public short getVariety() {
       return fVariety;  
    }
    
    public XSDHandler getSchemaHandler() {
       return fSchemaHandler;  
    }
    
    public short getAssertKind() {
       return fAssertKind;  
    }
    
    /*
     * Tests if two assertion components are equal. Xerces has a specific notion of assertions equality, 
     * as described by the algorithm in this method.
     */
    public boolean equals(XSAssertImpl pAssertion) {
        
        boolean returnVal = false;

        String xpathStr = pAssertion.getTest().getXPath().toString();
        String currXpathStr = this.getTest().getXPath().toString();        

        // if type and the xpath string are same, the asserts are equal
        if (XSTypeHelper.isSchemaTypesIdentical(pAssertion.getTypeDefinition(), fTypeDefinition) && 
                currXpathStr.equals(xpathStr)) {
            returnVal = true;  
        }

        return returnVal;
        
    } // equals
    
} // class XSAssertImpl
