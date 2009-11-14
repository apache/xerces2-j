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

import org.apache.xerces.xs.XSAssert;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * XML Schema 1.1 'assertion' component
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XSAssertImpl implements XSAssert {

    /** The type definition associated with the assertion component */
    protected XSTypeDefinition fTypeDefinition;

    /** An XPath 2.0 expression that represents the assert 'test' attribute */
    protected Test fTestExpr = null;
    
    /** Optional annotations */
    protected XSObjectList fAnnotations = null;

    /** Default XPath namespace */
    protected String fXPathDefaultNamespace = null;
    
    /** XSD namespace prefix, present on <schema> element */
    protected String xsdNamespacePrefix = null;

    /** Constructor */
    public XSAssertImpl(XSTypeDefinition type,
            XSObjectList annotations) {
        // An assert component must correspond to a type        
        fTypeDefinition = type;
        fAnnotations = annotations;
    }

    /** Sets the test attribute value */
    public void setTest(Test expr) {
        fTestExpr = expr;
    }

    /** Sets the assertion annotations */
    public void setAnnotations(XSObjectList annotations) {
        fAnnotations = annotations;
    }

    /** Sets the xpath default namespace */
    public void setXPathDefauleNamespace(String namespace) {
        fXPathDefaultNamespace = namespace;
    }
    
    /** Sets the XSD namespace, prefix */
    public void setXsdNamespacePrefix(String nsPrefix) {
        xsdNamespacePrefix = nsPrefix;
    }
   
    public XSObjectList getAnnotations() {
        return fAnnotations;
    }

    public String getTestStr() {
        return fTestExpr.toString();
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
     * 
     * Get the XSD namespace prefix
     */
    public String getXsdNamespacePrefix() {
        return xsdNamespacePrefix;
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
