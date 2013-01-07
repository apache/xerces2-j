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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.util.XS11TypeHelper;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xs.ElementPSVI;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.DynamicContext;
import org.eclipse.wst.xml.xpath2.processor.PsychoPathXPathTypeHelper;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyAtomicType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;
import org.eclipse.wst.xml.xpath2.processor.internal.types.SchemaTypeValueFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class defines a set of methods to work with XPath 2.0 dynamic context 
 * variable "$value", that is needed for assertions evaluation.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi IBM
 * 
 * @version $Id$
 */
public class XSAssertionXPath2ValueImpl implements XSAssertionXPath2Value {
    
    // reference of root node of assert tree
    private Element fRootNodeOfAssertTree = null;

    /*
     * Determine "string value" of XPath 2.0 context variable $value.
     */
    public String computeStringValueOf$value(Element rootNodeOfAssertTree, ElementPSVI pElemPSVI) throws DOMException {
        String strValueOf$value = "";
        
        XSTypeDefinition typeDef = pElemPSVI.getTypeDefinition();
        if (typeDef instanceof XSComplexTypeDefinition) {
            XSComplexTypeDefinition complexTypeDef = (XSComplexTypeDefinition)typeDef;
            if (!(complexTypeDef.getContentType() == XSComplexTypeDefinition.CONTENTTYPE_SIMPLE)) {
                strValueOf$value = null; 
            }
        }
        
        if (strValueOf$value != null) {      
            fRootNodeOfAssertTree = rootNodeOfAssertTree;
            NodeList childNodeList = rootNodeOfAssertTree.getChildNodes();
            // there could be adjacent text nodes in the DOM tree. merge them to get the value.
            StringBuffer textValueContents = new StringBuffer();
            final int childListLength = childNodeList.getLength();
            int textChildCount = 0;
            // we are only interested in text & element nodes. store count of them in this variable.
            int effectiveChildNodeCount = 0;
            for (int childNodeIndex = 0; childNodeIndex < childListLength; childNodeIndex++) {
                Node node = childNodeList.item(childNodeIndex);
                short nodeType = node.getNodeType();
                if (nodeType == Node.TEXT_NODE) {
                    textChildCount++;
                    effectiveChildNodeCount++;
                    textValueContents.append(node.getNodeValue());
                }
                else if (nodeType == Node.ELEMENT_NODE) {
                    effectiveChildNodeCount++;  
                }
            }

            if (textChildCount == effectiveChildNodeCount) {
                // the DOM tree we are inspecting has simple content. therefore we can find the desired string value.
                if ((pElemPSVI.getTypeDefinition()).derivedFrom(SchemaSymbols.URI_SCHEMAFORSCHEMA, SchemaSymbols.ATTVAL_STRING, XSConstants.DERIVATION_RESTRICTION)) {
                    // if element's schema type is derived by restriction from xs:string, white-space normalization is not needed for the
                    // string value for context variable $value.
                    strValueOf$value = textValueContents.toString();  
                }
                else {
                    // white-space normalization is needed for the string value of $value in case of derivation from non xs:string atomic types
                    strValueOf$value = XMLChar.trim(textValueContents.toString());
                }    
            }
        }
        
        return strValueOf$value;
        
    } // computeStringValueOf$value
    

    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context. This method delegates
     * to other methods of interface XSAssertionXPath2Value to carry some of it's tasks.
     */
    public void setXDMTypedValueOf$value(Element rootNodeOfAssertTree, String value, XSSimpleTypeDefinition listOrUnionType, XSTypeDefinition attrType, boolean isTypeDerivedFromList, DynamicContext xpath2DynamicContext) throws Exception {
        
        // dummy schema short code initializer
        short xsdTypecode = -100;
        
        if (listOrUnionType != null) {
            if (isTypeDerivedFromList || listOrUnionType.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST) {
                // $value is a sequence of atomic values (with type annotation xs:anyAtomicType*). tokenize the list value by a sequence of white spaces.
                StringTokenizer listStrTokens = new StringTokenizer(value, " \n\t\r");
                List xdmItemList = new ArrayList();
                while (listStrTokens.hasMoreTokens()) {
                    String itemValue = listStrTokens.nextToken();
                    if (listOrUnionType.getItemType() != null) {
                       xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue((listOrUnionType.getItemType()).getBuiltInKind(), itemValue));
                    }
                    else {
                       xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue(listOrUnionType.getBuiltInKind(), itemValue));
                    }
                }
                xpath2DynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName("value"), XS11TypeHelper.getXPath2ResultSequence(xdmItemList));                
            }
            else {
               xsdTypecode = getXercesXSDTypeCodeFor$value(listOrUnionType);
               setXDMTypedValueOf$valueForSTVarietyAtomic(value, xsdTypecode, xpath2DynamicContext);
            }
        }
        else {
           if (attrType != null) {
              // is value of an attribute
              xsdTypecode = getXercesXSDTypeCodeFor$value(attrType);
              setXDMTypedValueOf$valueForSTVarietyAtomic(value, xsdTypecode, xpath2DynamicContext);
           }
           else {
              // is "simple type" value of an element
              XSTypeDefinition typeDef = ((PSVIElementNSImpl) rootNodeOfAssertTree).getTypeDefinition();
              if (typeDef instanceof XSComplexTypeDefinition && ((XSComplexTypeDefinition) typeDef).getSimpleType() != null) {
                  setXDMValueOf$valueForCTWithSimpleContent(value, (XSComplexTypeDefinition) typeDef, xpath2DynamicContext);
              }
              else if (typeDef instanceof XSComplexTypeDefinition && ((XSComplexTypeDefinition) typeDef).getSimpleType() == null) {
                  // set xpath context variable $value to an empty sequence
                  xpath2DynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName("value"), XS11TypeHelper.getXPath2ResultSequence(new ArrayList())); 
              }
              else {
                  xsdTypecode = getXercesXSDTypeCodeFor$value(typeDef);
                  setXDMTypedValueOf$valueForSTVarietyAtomic(value, xsdTypecode, xpath2DynamicContext);
              }
           }
        }
        
    } // setXDMTypedValueOf$value
    
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType variety atomic. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyAtomic(String value, short xsdTypecode, DynamicContext xpath2DynamicContext) {
        AnyType psychoPathType = SchemaTypeValueFactory.newSchemaTypeValue(xsdTypecode, value);
        xpath2DynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName("value"), (AnyAtomicType) psychoPathType);
    } // setXDMTypedValueOf$valueForSTVarietyAtomic
    
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType variety list. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyList(Element rootNodeOfAssertTree, String listStrValue, XSSimpleTypeDefinition itemType, boolean isTypeDerivedFromList, DynamicContext xpath2DynamicContext) throws Exception {
        
       if (itemType.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION) {
           // itemType of xs:list has variety 'union'
           List xdmItemList = new ArrayList();
           XSObjectList memberTypes = itemType.getMemberTypes();
           // tokenize the list value by a sequence of white spaces
           StringTokenizer values = new StringTokenizer(listStrValue, " \n\t\r");
           while (values.hasMoreTokens()) {
               String itemValue = values.nextToken();
               XSSimpleTypeDefinition listItemTypeForUnion = getActualXDMItemTypeForSTVarietyUnion(memberTypes, itemValue);
               xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue(listItemTypeForUnion.getBuiltInKind(), itemValue));
           }
           xpath2DynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName("value"), XS11TypeHelper.getXPath2ResultSequence(xdmItemList));
       } 
       else {
          setXDMTypedValueOf$value(rootNodeOfAssertTree, listStrValue, itemType, null, isTypeDerivedFromList, xpath2DynamicContext); 
       }

    } // setXDMTypedValueOf$valueForSTVarietyList
        
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, when the value is for simpleType with variety union. 
     */
    public void setXDMTypedValueOf$valueForSTVarietyUnion(String value, XSObjectList memberTypes, DynamicContext xpath2DynamicContext) throws Exception {        
        // check member types of union in order to find that which member type can successfully validate the string value
        // first, and set the type annotation of XPath2 context variable $value with this member type definition.
        for (int memTypeIdx = 0; memTypeIdx < memberTypes.getLength(); memTypeIdx++) {
            XSSimpleType simpleTypeDv = (XSSimpleType) memberTypes.item(memTypeIdx);
            if (XS11TypeHelper.isStrValueValidForASimpleType(value, simpleTypeDv, Constants.SCHEMA_VERSION_1_1)) {
              if (simpleTypeDv.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST) {
                  setXDMTypedValueOf$valueForSTVarietyList(fRootNodeOfAssertTree, value, simpleTypeDv, ((XSSimpleTypeDefinition)simpleTypeDv.getBaseType()).getVariety() == XSSimpleTypeDefinition.VARIETY_LIST, xpath2DynamicContext);
              }
              else {
                  setXDMTypedValueOf$valueForSTVarietyAtomic(value, getXercesXSDTypeCodeFor$value(simpleTypeDv), xpath2DynamicContext);
              }
              break;
            }            
        }        
    } // setXDMTypedValueOf$valueForSTVarietyUnion
    
    
    /*
     * Given a string value, this method sets an XPath 2.0 typed value for variable "$value" in XPath dynamic context, if element has a complex type with simple content. 
     */
    protected void setXDMValueOf$valueForCTWithSimpleContent(String value, XSComplexTypeDefinition typeDef, DynamicContext xpath2DynamicContext) throws Exception {
        
        XSComplexTypeDefinition cmplxTypeDef = (XSComplexTypeDefinition)typeDef;
        XSSimpleTypeDefinition complexTypeSimplContentType = cmplxTypeDef.getSimpleType();
        if (complexTypeSimplContentType.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST) {
            // simple content type has variety xs:list
            XSSimpleTypeDefinition listItemType = complexTypeSimplContentType.getItemType();
            // $value is a sequence of atomic values (with type annotation xs:anyAtomicType*). tokenize the list value by a sequence of white spaces.
            StringTokenizer values = new StringTokenizer(value, " \n\t\r");
            List xdmItemList = new ArrayList();
            if (listItemType.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION) {
               // itemType of xs:list has variety 'union'
               final XSObjectList memberTypes = listItemType.getMemberTypes();
               while (values.hasMoreTokens()) {
                   String itemValue = values.nextToken();
                   XSSimpleTypeDefinition listItemTypeForUnion = getActualXDMItemTypeForSTVarietyUnion(memberTypes, itemValue);
                   xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue(listItemTypeForUnion.getBuiltInKind(), itemValue));
               }                                  
            }
            else {
               // every list item has a same type (the itemType of xs:list)
               while (values.hasMoreTokens()) {
                   String itemValue = values.nextToken();
                   xdmItemList.add(SchemaTypeValueFactory.newSchemaTypeValue(listItemType.getBuiltInKind(), itemValue)); 
               }                                  
            }

            // assign an XPath2 sequence to xpath context variable $value
            xpath2DynamicContext.set_variable(new org.eclipse.wst.xml.xpath2.processor.internal.types.QName("value"), XS11TypeHelper.getXPath2ResultSequence(xdmItemList));
        }
        else if (complexTypeSimplContentType.getVariety() == XSSimpleTypeDefinition.VARIETY_UNION) {
            // simple content type has variety xs:union
            XSSimpleTypeDefinition simpleContentTypeForUnion = getActualXDMItemTypeForSTVarietyUnion(complexTypeSimplContentType.getMemberTypes(), value);
            if (simpleContentTypeForUnion.getVariety() == XSSimpleTypeDefinition.VARIETY_LIST) {
                setXDMTypedValueOf$valueForSTVarietyList(fRootNodeOfAssertTree, value, simpleContentTypeForUnion, ((XSSimpleTypeDefinition)simpleContentTypeForUnion.getBaseType()).getVariety() == XSSimpleTypeDefinition.VARIETY_LIST, xpath2DynamicContext);
            }
            else {
                setXDMTypedValueOf$valueForSTVarietyAtomic(value, getXercesXSDTypeCodeFor$value(simpleContentTypeForUnion), xpath2DynamicContext);
            }
        }
        else {
            // simple content type has variety atomic
            setXDMTypedValueOf$valueForSTVarietyAtomic(value, getXercesXSDTypeCodeFor$value(cmplxTypeDef.getSimpleType()), xpath2DynamicContext);
        }
          
    } // setXDMValueOf$valueForCTWithSimpleContent
    
    
    /* 
     * Find the built-in Xerces schema 'type code' for XPath2 variable $value. This function recursively searches the XML schema type hierarchy navigating
     * up the base types, to find the needed built-in type.
     */
    private short getXercesXSDTypeCodeFor$value(XSTypeDefinition elementType) {

        if (Constants.NS_XMLSCHEMA.equals(elementType.getNamespace())) {
            short typeCode = -100; // dummy initializer

            boolean isxsd11Type = false;

            // the below 'if else' clauses are written to process few special cases handling few of schema types within PsychoPath XPath engine
            final String elementTypeName = elementType.getName();
            if ("dayTimeDuration".equals(elementTypeName)) {
                typeCode = PsychoPathXPathTypeHelper.DAYTIMEDURATION_DT;
                isxsd11Type = true;
            }
            else if ("yearMonthDuration".equals(elementTypeName)) {
                typeCode = PsychoPathXPathTypeHelper.YEARMONTHDURATION_DT;
                isxsd11Type = true;
            }

            return (isxsd11Type) ? typeCode : ((XSSimpleTypeDefinition) elementType).getBuiltInKind();    
        }
        else {
            return getXercesXSDTypeCodeFor$value(elementType.getBaseType()); 
        }

    } // getXercesXSDTypeCodeFor$value
    
    
    /*
     * Find the actual schema type of XDM item instance, if source schema type is simpleType with variety union. 
     */
    private XSSimpleTypeDefinition getActualXDMItemTypeForSTVarietyUnion(XSObjectList memberTypes, String xdmItemStrValue) {

        XSSimpleTypeDefinition xdmItemType = null;
        
        // iterate the member types of union in order, to find that which schema type can successfully validate an atomic value first
        final int memberTypesLength = memberTypes.getLength();
        for (int memTypeIdx = 0; memTypeIdx < memberTypesLength; memTypeIdx++) {
           XSSimpleType memSimpleType = (XSSimpleType) memberTypes.item(memTypeIdx);
           if (XS11TypeHelper.isStrValueValidForASimpleType(xdmItemStrValue, memSimpleType, Constants.SCHEMA_VERSION_1_1)) {
              // no more memberTypes need to be checked
              xdmItemType = memSimpleType; 
              break; 
           }
        }
        
        return xdmItemType;
        
    } // getActualXDMItemTypeForSTVarietyUnion

} // class XSAssertionXPath2ValueImpl
