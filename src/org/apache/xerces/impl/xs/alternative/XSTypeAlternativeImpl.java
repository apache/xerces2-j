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

import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeAlternative;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * XML schema type alternative component.
 * 
 * @xerces.internal
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class XSTypeAlternativeImpl implements XSTypeAlternative {

	/** Name of the owning element */
    protected final String fElementName;

    /** The restricted XPath 2.0 expression that represents the test attribute */
    protected Test fTestExpr = null;

    /** The type definition associated with the type alternative component */
    protected XSTypeDefinition fTypeDefinition;

    /** Optional annotations */
    protected XSObjectList fAnnotations = null;

    /** Default XPath namespace */
    protected String fXPathDefaultNamespace = null;
    
    protected NamespaceSupport fNamespaceContext = null;
    
    protected String fBaseURI = null;

    /** Constructor */
    public XSTypeAlternativeImpl(String elementName, XSTypeDefinition type, XSObjectList annotations) {
        //A type alternative must belong to some element decl and
        //also must have a type definition property
        fElementName = elementName;
        fTypeDefinition = type;
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
    
    public void setNamespaceContext(NamespaceSupport namespaceContext) {
        fNamespaceContext = namespaceContext;
    }
    
    public void setBaseURI(String baseUri) {
       fBaseURI = baseUri;  
    }
    
    public NamespaceSupport getNamespaceContext() {        
        NamespaceSupport namespaceContext = fNamespaceContext;        
        if (namespaceContext == null && fTestExpr != null) {
            namespaceContext = fTestExpr.getNamespaceContext();  
        }        
        return namespaceContext;        
    } // getNamespaceContext
    
    public String getBaseURI() {
       return fBaseURI; 
    }

    //gets the name of the owning element
	public String getElementName() {
        return fElementName;
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

    public boolean equals(XSTypeAlternativeImpl typeAlternative) {
        //ToDo
        return false;
    }

    /** Get the optional annotations */
    public XSObjectList getAnnotations() {
        return fAnnotations;
    }

    public String getTestStr() {
        return (fTestExpr != null) ? fTestExpr.toString() : null;
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
        return XSConstants.TYPE_ALTERNATIVE;
    }

}
