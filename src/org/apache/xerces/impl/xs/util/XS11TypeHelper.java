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

package org.apache.xerces.impl.xs.util;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.TypeValidatorHelper;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.validation.ValidationState;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.xs.alternative.XSTypeAlternativeImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.eclipse.wst.xml.xpath2.processor.ResultSequence;
import org.eclipse.wst.xml.xpath2.processor.ResultSequenceFactory;
import org.eclipse.wst.xml.xpath2.processor.internal.types.AnyType;

/**
 * Class defining utility/helper methods to support XML Schema 1.1 implementation.
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * @version $Id$
 */
public class XS11TypeHelper {
    
    
    /*
     * Class constructor.
     */
    private XS11TypeHelper() {
       // a private constructor, to prohibit instantiating this class from an outside class/application.
       // this is a good practice, since all methods of this class are "static".
    }
    
    /*
     * Checks if the two schema type components are identical.
     */
    public static boolean isSchemaTypesIdentical(XSTypeDefinition typeDefn1, XSTypeDefinition typeDefn2) {
        boolean typesIdentical = false;
        
        String type1Name = typeDefn1.getName();
        String type2Name = typeDefn2.getName();
        
        if (("anyType".equals(type1Name) && "anyType".equals(type2Name)) ||
            ("anySimpleType".equals(type1Name) && "anySimpleType".equals(type2Name))) {
               typesIdentical = true;  
        }
        
        if (!typesIdentical && isURIEqual(typeDefn1.getNamespace(), typeDefn2.getNamespace())) {                        
            // if targetNamespace of types are same, then check for  equality of type names and of the base type
            if ((type1Name == null && type2Name == null) ||
                    (type1Name != null && type1Name.equals(type2Name)) && (isSchemaTypesIdentical(typeDefn1.getBaseType(), typeDefn2.getBaseType()))) {
                typesIdentical = true;   
            }
        }
        
        return typesIdentical;
        
    } // isSchemaTypesIdentical
    
    
    /*
     * Check if two URI values are equal.
     */
    public static boolean isURIEqual(String uri1, String uri2) {
        return (uri1 == uri2 || (uri1 != null && uri1.equals(uri2)));
    } // isURIEqual  
    
    
    /*
     * Determine if a string value is valid with respect to any of the simpleType -> union's member types which are in XML Schema namespace.
     * If this method returns a boolean 'true', then the value is valid with respect to entire union schema component. 
     */
    public static boolean isAtomicStrValueValidForSTUnion(XSObjectList memberTypes, String content, ValidatedInfo validatedInfo, short schemaVersion) {
        
        boolean isValueValid = false;
        
        // check the union member types in order to find validity of an atomic value. the validity of atomic value wrt
        // to the 1st available type in union's member type collection, is sufficient to achieve the objective of this method.
        for (int memTypeIdx = 0; memTypeIdx < memberTypes.getLength(); memTypeIdx++) {
            XSSimpleType simpleTypeDv = (XSSimpleType) memberTypes.item(memTypeIdx);
            if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(simpleTypeDv.getNamespace()) && XS11TypeHelper.isStrValueValidForASimpleType(content, simpleTypeDv, schemaVersion)) {
                isValueValid = true;
                validatedInfo.memberType = simpleTypeDv; 
                break;  
            }
        }
        
        return isValueValid;
        
    } // isAtomicStrValueValidForSTUnion
    
    
    /*
     * Determine if a string value is valid with a given simpleType definition. Using Xerces API 'XSSimpleType.validate' for this need.
     */
    public static boolean isStrValueValidForASimpleType(String value, XSSimpleType simplType, short schemaVersion) {
        
        boolean isStrValueValid = true;
        
        try {
            // construct necessary context objects
            ValidatedInfo validatedInfo = new ValidatedInfo();
            ValidationState validationState = new ValidationState();
            validationState.setTypeValidatorHelper(TypeValidatorHelper.getInstance(schemaVersion));
            
            // attempt to validate the "string value" with a simpleType definition
            simplType.validate(value, validationState, validatedInfo);
        } 
        catch(InvalidDatatypeValueException ex){
            isStrValueValid = false;
        }
        
        return isStrValueValid;
        
    } // isStrValueValidForASimpleType
    
    
    /*
     * Validate a QName value (check lexical form for correctness, and if the prefix is declared), and report errors if there are any.
     */
    public static void validateQNameValue(String qNameStr, NamespaceContext namespaceContext, XMLErrorReporter errorReporter) {
        
        String[] parsedQname = parseQnameString(qNameStr);
        String prefix = parsedQname[0]; 
        String localpart = parsedQname[1];
        
        // both prefix (if any) and localpart of QName, must be valid NCName
        if ((prefix.length() > 0 && !XMLChar.isValidNCName(prefix)) || !XMLChar.isValidNCName(localpart)) {
            errorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "cvc-datatype-valid.1.2.1", new Object[] {qNameStr, "QName"}, XMLErrorReporter.SEVERITY_ERROR);
        }

        // try to resolve QName prefix to a namespace URI, and report an error if resolution fails.
        String uri = namespaceContext.getURI(prefix.intern());
        if (prefix.length() > 0 && uri == null) {
            errorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "UndeclaredPrefix", new Object[] {qNameStr, prefix}, XMLErrorReporter.SEVERITY_ERROR);
        }
        
    } // validateQNameValue
    
    
    /*
     * Parse a QName string value into prefix and local-name pairs.
     */
    private static String[] parseQnameString(String qNameStr) {
        
        String[] parsedQName = new String[2];
        
        String prefix, localpart;
        int colonptr = qNameStr.indexOf(':');
        if (colonptr > 0) {
            prefix = qNameStr.substring(0, colonptr);
            localpart = qNameStr.substring(colonptr + 1);
        } else {
            prefix = SchemaSymbols.EMPTY_STRING;
            localpart = qNameStr;
        }
        parsedQName[0] = prefix;
        parsedQName[1] = localpart;
        
        return parsedQName; 
        
    } // parseQnameString 
    
    
    /*
     * Get assertions list of a simpleType definition.
     */
    public static Vector getAssertsFromSimpleType(XSSimpleTypeDefinition simplType) {

        Vector simpleTypeAsserts = new Vector();
        
        XSObjectListImpl facetList = (XSObjectListImpl) simplType.getMultiValueFacets();         
        for (int facetIdx = 0; facetIdx < facetList.getLength(); facetIdx++) {
            XSMultiValueFacet facet = (XSMultiValueFacet) facetList.item(facetIdx);
            if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                simpleTypeAsserts = facet.getAsserts();
            }
        }
        
        return simpleTypeAsserts;
        
    } // getAssertsFromSimpleType
    
    
    /*
     * Check if a simple type has assertion facets.
     */
    public static boolean simpleTypeHasAsserts(XSSimpleTypeDefinition simpleType) {
        
        boolean simpleTypehasAsserts = false;
        
        XSObjectList simpleTypeFacets = simpleType.getMultiValueFacets();
        final int simpleTypeFacetsLength = simpleTypeFacets.getLength();
        for (int facetIdx = 0; facetIdx < simpleTypeFacetsLength; facetIdx++) {
            XSMultiValueFacet facet = (XSMultiValueFacet) simpleTypeFacets.item(facetIdx);
            if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT && facet.getAsserts().size() > 0) {
                simpleTypehasAsserts = true;
                break;
            }
        }
        
        return simpleTypehasAsserts;

    } // simpleTypeHasAsserts
    
    
    /*
     * Find if a list contains a specified schema type.
     */
    public static boolean isListContainsType(List typeList, XSTypeDefinition targetType) {
        
        boolean typeExists = false;
        for (Iterator iter = typeList.iterator(); iter.hasNext();) {
            if (XS11TypeHelper.isSchemaTypesIdentical((XSTypeDefinition) iter.next(), targetType)) {
                typeExists = true;
                break;
            }
        }
        return typeExists;
        
    } // isListContainsType
    
    
    /*
     * Find if a complexType is derived from a simpleType->list component.
     */
    public static boolean isComplexTypeDerivedFromSTList(XSComplexTypeDefinition complexTypeDef, short derivationMethod) {
        
        XSTypeDefinition baseType = complexTypeDef.getBaseType();
        return complexTypeDef.getDerivationMethod() == derivationMethod && baseType instanceof XSSimpleTypeDefinition &&
               ((XSSimpleTypeDefinition)baseType).getVariety() == XSSimpleTypeDefinition.VARIETY_LIST;
        
    } // isComplexTypeDerivedFromSTList
    
    
    /*
     * Get name of an XSD type definition as a string value (which will typically be the value of "name" attribute of a
     * type definition, or an internal name determined by the validator for anonymous types).
     */
    public static String getSchemaTypeName(XSTypeDefinition typeDefn) {
        
        String typeNameStr = "";
        if (typeDefn instanceof XSSimpleTypeDefinition) {
            typeNameStr = ((XSSimpleTypeDecl) typeDefn).getTypeName();
        }
        else {
            typeNameStr = ((XSComplexTypeDecl) typeDefn).getTypeName();
        }
        
        return typeNameStr;
        
    } // getSchemaTypeName
    
    
    /*
     * Check if a simpleType definition is one of special types (i.e xs:anyAtomicType or xs:anySimpleType).
     */
    public static boolean isSpecialSimpleType(XSSimpleType simpleType) {        
        boolean isSpecialSimpleType = false;
        String typeName = simpleType.getName(); 
        if (Constants.NS_XMLSCHEMA.equals(simpleType.getNamespace()) && (SchemaSymbols.ATTVAL_ANYATOMICTYPE.equals(typeName) || SchemaSymbols.ATTVAL_ANYSIMPLETYPE.equals(typeName))) {
            isSpecialSimpleType = true; 
        }        
        return isSpecialSimpleType;        
    } // isSpecialSimpleType
    
    
    /*
     * Construct an PsychoPath XPath2 "result sequence" given a list of XDM items as input.
     */
    public static ResultSequence getXPath2ResultSequence(List xdmItems) {
        
        ResultSequence xpath2Seq = ResultSequenceFactory.create_new();
        
        for (Iterator iter = xdmItems.iterator(); iter.hasNext(); ) {
            xpath2Seq.add((AnyType) iter.next()); 
        }
        
        return xpath2Seq;
        
    } // getXPath2ResultSequence
    
    
    /*
     * Check if two type tables can be compared.
     */
    public static boolean isTypeTablesComparable(XSTypeAlternativeImpl[] typeTable1, XSTypeAlternativeImpl[] typeTable2) {
       boolean typeTablesComparable = true;
       
       if (typeTable1 == null && typeTable2 == null) {
           typeTablesComparable = false;  
       }
       
       return typeTablesComparable; 
    } // isTypeTablesComparable
    
    
} // class XS11TypeHelper
