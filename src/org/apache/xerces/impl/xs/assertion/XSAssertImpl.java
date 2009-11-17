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
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSAssert;
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
      
    /** XPath 2.0 namespace context. Derived from XSDocumentInfo in XSD traversers. */
    protected NamespaceSupport fXPath2NamespaceContext = null;
    
    // XSDHandler object, passed on from the tarversers 
    protected XSDHandler fSchemaHandler = null;

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
        return XSConstants.ASSERTION;
    }
       
    /**
     * Get the XPath 2.0 namespace context
     */
    public NamespaceSupport getXPath2NamespaceContext() {
        return fXPath2NamespaceContext;
    }
    
    /*
     * Tests if two assert components are equal
     */
    public boolean equals(XSAssertImpl assertComponent) {
      boolean returnVal = false;
        
      String typeNameP = assertComponent.getTypeDefinition().getName();
      String xpathStrP = assertComponent.getTest().getXPath().toString();
      String typeNameThis = this.fTypeDefinition.getName();
      String xpathStrThis = this.getTest().getXPath().toString();
        
      // if type and the xpath string are same, the asserts are equal
      if (typeNameThis.equals(typeNameP) && xpathStrThis.equals(xpathStrP)) {
        returnVal = true;  
      }
        
      return returnVal;
    }
}
