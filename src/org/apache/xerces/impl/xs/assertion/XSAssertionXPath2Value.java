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

import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * An interface defining methods to work with XPath 2.0 dynamic context variable "$value" that is needed for assertions evaluation.
 * 
 * @xerces.internal
 * 
 * @version $Id$
 */
public interface XSAssertionXPath2Value {
    
    /*
     * Determine "string value" of XPath 2.0 context variable $value.
     */
    public String computeStringValueOf$value(Element rootNodeOfAssertTree, ElementPSVI pElemPSVI) throws DOMException;
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context. This method may
     * delegate to other methods of this interface for doing its responsibilities.
     */
    public void setXDMTypedValueOf$value(Element rootNodeOfAssertTree, String value, XSSimpleTypeDefinition listOrUnionType, XSTypeDefinition attrType, boolean isTypeDerivedFromList, DynamicContext xpath2DynamicContext) throws Exception;
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType variety atomic. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyAtomic(String value, short xsdTypecode, DynamicContext xpath2DynamicContext);
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType variety list. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyList(Element rootNodeOfAssertTree, String listStrValue, XSSimpleTypeDefinition itemType, boolean isTypeDerivedFromList, DynamicContext xpath2DynamicContext) throws Exception;
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType variety union. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyUnion(String value, XSObjectList memberTypes, DynamicContext xpath2DynamicContext);

}
