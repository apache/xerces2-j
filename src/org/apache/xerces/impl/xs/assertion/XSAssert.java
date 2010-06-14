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

import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;

/**
 * This interface represents the XML Schema 1.1 assertion component. Assertion
 * components were first introduced in the XML Schema structures 1.1
 * specification, as a means of constraining the existence and values of 
 * related elements and attributes.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public interface XSAssert extends XSObject {

    /**
     * A sequence of [annotations] or an empty <code>XSObjectList</code>
     */
    public XSObjectList getAnnotations();

    /**
     * [test]: an XPath 2.0 expression
     */
    public Test getTest();

    /**
     * The type associated with the assertion
     */
    public XSTypeDefinition getTypeDefinition();
}
