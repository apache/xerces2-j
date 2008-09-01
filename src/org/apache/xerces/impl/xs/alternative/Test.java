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

import org.apache.xerces.impl.xpath.XPath20;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;

/**
 * XML schema type alternative test attribute
 * 
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class Test {

	/** The type alternative to which the test belongs */
    protected final XSTypeAlternativeImpl fTypeAlternative;

    /** XPath 2.0 expression */
    protected final XPath20 fXPath;

    /** Constructs a test for type alternatives */
    public Test(XPath20 xpath, XSTypeAlternativeImpl typeAlternative) {
        fXPath = xpath;
        fTypeAlternative = typeAlternative;
    }

    public XSTypeAlternativeImpl getTypeAlternative() {
        return fTypeAlternative;
    }
	
    /** Returns the test XPath */
    public XPath20 getXPath() {
        return fXPath;
    }

    /** Evaluate the test expression with respect to the specified element and its attributes */
    public boolean evaluateTest(QName element, XMLAttributes attributes) {
        if (fXPath != null) {
            return fXPath.traverseTree(element, attributes);
        }
        else {
            return false;
        }
    }

    public String toString() {
        return fXPath.toString();
    }
}
