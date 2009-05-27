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

import org.apache.xerces.impl.xpath.XPath20Assert;
import org.apache.xerces.xs.XSAssert;

/**
 * XML schema assertion 'test' attribute
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class Test {

    /** The assertion component to which the test belongs */
    protected final XSAssert fAssert;

    /** XPath 2.0 expression */
    protected final XPath20Assert fXPath;

    /** Constructs a test for assertion component */
    public Test(XPath20Assert xpath, XSAssert assertion) {
        fXPath = xpath;
        fAssert = assertion;
    }

    public XSAssert getAssertion() {
        return fAssert;
    }

    /** Returns the test XPath */
    public XPath20Assert getXPath() {
        return fXPath;
    }

    public String toString() {
        return fXPath.toString();
    }
}
