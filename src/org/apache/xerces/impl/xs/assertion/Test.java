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

import org.apache.xerces.xni.NamespaceContext;

/**
 * Representation of XML Schema 1.1 assertion "test" attribute.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class Test {

    /** The XPath expression string for the assertion component */
    protected final String fExpressionStr;
    
    /** The Namespace context information for assert component */
    protected final NamespaceContext fNsContext;
    
    /** The assertion component to which the test belongs */
    protected final XSAssert fAssert;

    /** Constructs a test for assertion component */
    public Test(String xpathStr, NamespaceContext nsContext, XSAssert assertion) {
        fExpressionStr = xpathStr;
        fNsContext = nsContext; 
        fAssert = assertion;
    }

    /** Returns the test XPath string value */
    public String getXPathStr() {
        return fExpressionStr;
    }

    /** Returns the namespace context for assertion component */
    public NamespaceContext getNamespaceContext() {
        return fNsContext;
    }
    
    /** Returns the assertion component */
    public XSAssert getAssertion() {
        return fAssert;
    }
}
