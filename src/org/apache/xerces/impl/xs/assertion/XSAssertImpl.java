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

import org.apache.xerces.impl.xs.AbstractPsychoPathImpl;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.apache.xerces.impl.xs.util.XSTypeHelper;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.ast.XPath;

/**
 * XML Schema 1.1 'assertion' component.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XSAssertImpl extends AbstractPsychoPathImpl implements XSAssert {

    protected short assertKind = XSConstants.ASSERTION;
    
    /** The type definition associated with the assertion component */
    protected XSTypeDefinition fTypeDefinition;

    /** Xerces object representing the assert 'test' attribute */
    protected Test fTestExpr = null;
    
    /** Compiled XPath 2.0 expression */
    protected XPath fCompiledXPath = null;
    
    /** Optional annotations */
    protected XSObjectList fAnnotations = null;

    /** Default XPath namespace */
    protected String fXPathDefaultNamespace = null;
      
    /** XPath 2.0 namespace context. Derived from XSDocumentInfo in Xerces
        schema "component traversers".
    */
    protected NamespaceSupport fXPath2NamespaceContext = null;
    
    // a non null value of this object indicates, that this assertion belongs
    // to an attribute's schema type, and value of this object would be the
    // attribute's name.
    protected String attrName = null;
    
    // a non null value of this object indicates, that this assertion belongs
    // to an attribute's schema type, and value of this object would be the
    // attribute's value.
    protected String attrValue = null;
    
    // XSDHandler object, passed on from the Xerces schema "component
    // traversers". 
    protected XSDHandler fSchemaHandler = null;
    
    // user-defined message to provide to an user context, during assertion
    // failures.
    protected String message = null;
    
    // representing the schema type's variety, if an assertion belongs to a
    // schema simpleType.
    protected short fVariety = 0;

    /** Constructor */
    public XSAssertImpl(XSTypeDefinition type,
            XSObjectList annotations, XSDHandler schemaHandler) {
        // An assert component must correspond to a schema type        
        fTypeDefinition = type;
        
        fSchemaHandler = schemaHandler; 
        fAnnotations = annotations;
    }

    /** Sets the test attribute value */
    public void setTest(Test expr) {
        fTestExpr = expr;        
        // compile the XPath string, into an object
        fCompiledXPath = compileXPathStr(expr.toString(),
                                         this,
                                         fSchemaHandler);
    }

    /** Sets the assertion annotations */
    public void setAnnotations(XSObjectList annotations) {
        fAnnotations = annotations;
    }

    /** Sets the xpath default namespace */
    public void setXPathDefaultNamespace(String namespace) {
        fXPathDefaultNamespace = namespace;
    }
      
    /** Sets the XPath 2.0 namespace context */
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
        this.assertKind = assertKind;
    }
    
    /**
     * Sets the attribute name
     */
    public void setAttrName(String attrName) {
        this.attrName = attrName;   
    }
    
    /**
     * Sets the attribute value
     */
    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;   
    }
    
   
    public XSObjectList getAnnotations() {
        return fAnnotations;
    }

    public String getTestStr() {
        return fTestExpr.toString();
    }
    
    public XPath getCompiledXPath() {
        return fCompiledXPath;
    }

    public Test getTest() {
        return fTestExpr;
    }
    
    public XSTypeDefinition getTypeDefinition() {
        return fTypeDefinition;
    }
    
    public void setTypeDefinition(XSTypeDefinition typeDefn) {
        fTypeDefinition = typeDefn;  
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

    /**
     * Get the type of the object
     */
    public short getType() {
        return assertKind;
    }
    
    /**
     * Get the attribute name
     */
    public String getAttrName() {
        return attrName;  
    }
    
    /**
     * Get the attribute value
     */
    public String getAttrValue() {
        return attrValue;  
    }
       
    /**
     * Get the XPath 2.0 namespace context
     */
    public NamespaceSupport getXPath2NamespaceContext() {
        return fXPath2NamespaceContext;
    }
    
    /*
     * Tests if two assertion components are equal. Xerces has a specific
     * notion of assertions equality, as described by the algorithm in this
     * method.
     */
    public boolean equals(XSAssertImpl pAssertion) {
      boolean returnVal = false;
      
      String xpathStr = pAssertion.getTest().getXPath().toString();
      String currXpathStr = this.getTest().getXPath().toString();        
      
      // if type and the xpath string are same, the asserts are equal
      if (XSTypeHelper.schemaTypesIdentical(pAssertion.getTypeDefinition(),
                                            fTypeDefinition) && 
                                            currXpathStr.equals(xpathStr)) {
         returnVal = true;  
      }
        
      return returnVal;
    }

    /*
     * Set error message, for assertions failures.
     */
    public void setMessage(String message) {
       this.message = message;    
    }
    
    /* 
     * Get the error message string.
     */
    public String getMessage() {
       return message;   
    }

    /*
     * If the assertion belongs to a simpleType, set the variety
     * of the type.
     */
    public void setVariety(short variety) {
        fVariety = variety;  
    }
    
    
    /*
     * Get the value of simpleType's variety.
     */
    public short getVariety() {
       return fVariety;  
    }
}
