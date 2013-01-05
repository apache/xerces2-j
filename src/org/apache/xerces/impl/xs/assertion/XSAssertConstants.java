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

/**
 * A class containing few constants related to XML Schema 1.1 assertion implementation.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * 
 * @version $Id$
 */
public class XSAssertConstants {

    // a list of assertion components
    public static String assertList = "ASSERT_LIST";
    
    // are there any assertions present within an attribute's type
    public static String isAttrHaveAsserts = "ATTRIBUTES_HAVE_ASSERTS";
    
    // is assertion processing needed for a simpleType->union for an element
    public static String isAssertProcNeededForUnionElem = "ASSERT_PROC_NEEDED_FOR_UNION_ELEM";
    
    // is assertion processing needed for a simpleType->union for attributes of one element
    public static String isAssertProcNeededForUnionAttr = "ASSERT_PROC_NEEDED_FOR_UNION_ATTR";
    
} // class XSAssertConstants
