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
 * XML Schema assertion component
 * 
 * @author Mukul Gandhi, IBM
 */
public class XSAssertImpl implements XSAssert {

    /** The type definition associated with the assertion component */
    protected XSTypeDefinition fTypeDefinition;

    /** An XPath 2.0 expression that represents the test attribute */
    protected Test fTestExpr = null;
    
    /** Optional annotations */
    protected XSObjectList fAnnotations = null;

    /** Default XPath namespace */
    protected String fXPathDefaultNamespace = null;

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

    public void setAnnotations(XSObjectList annotations) {
        fAnnotations = annotations;
    }

    public void setXPathDefauleNamespace(String namespace) {
        fXPathDefaultNamespace = namespace;
    }

    public String getXPathDefaultNamespace() {
        return fXPathDefaultNamespace;
    }

    /** Returns a String representation of this object */
    public String toString() {
        String s = super.toString();
        int index1 = s.lastIndexOf('$');
        if (index1 != -1) {
            return s.substring(index1 + 1);
        }
        int index2 = s.lastIndexOf('.');
        if (index2 != -1) {
            return s.substring(index2 + 1);
        }
        return s;
    }

    /*
     * Tests if two asserts are equal
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

    /** Get the optional annotations */
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
}
