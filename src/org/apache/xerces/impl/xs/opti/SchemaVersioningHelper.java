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

package org.apache.xerces.impl.xs.opti;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.QName;

/**
 * This class provides supporting functionality for schema versioning
 * pre-processing, during conditional inclusion of schema components (newly
 * introduced in XML Schema 1.1).
 * 
 * 
 * ref: http://www.w3.org/TR/xmlschema11-1/#cip
 * 
 * @author Mukul Gandhi, IBM
 * 
 * @version $Id$
 */
public class SchemaVersioningHelper {
    
    // list variables holding 'natively supported' XML Schema 1.1 types and facets
    // by Xerces-J XML Schema 1.1 engine.    
    List typesSupported = null;
    List facetsSupported = null;
    
    // class constructor
    public SchemaVersioningHelper() {
        typesSupported = new ArrayList();
        facetsSupported = new ArrayList();
        initialize();
    }
    
    // initializing the supported XML Schema types and facets
    private void initialize() {
        initSupportedTypes();
        initSupportedFacets();
    }
    
    // initializing the supported XML Schema types
    private void initSupportedTypes() {
        
        typesSupported.add(new QName(null, "anyType", "anyType", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "anySimpleType", "anySimpleType", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "anyAtomicType", "anyAtomicType", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "string", "string", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "boolean", "boolean", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "decimal", "decimal", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "precisionDecimal", "precisionDecimal", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "float", "float", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "double", "double", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "duration", "duration", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "dateTime", "dateTime", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "time", "time", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "date", "date", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "gYearMonth", "gYearMonth", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "gYear", "gYear", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "gMonthDay", "gMonthDay", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "gDay", "gDay", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "gMonth", "gMonth", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "hexBinary", "hexBinary", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "base64Binary", "base64Binary", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "anyURI", "anyURI", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "QName", "QName", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "NOTATION", "NOTATION", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "normalizedString", "normalizedString", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "token", "token", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "language", "language", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "NMTOKEN", "NMTOKEN", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "NMTOKENS", "NMTOKENS", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "Name", "Name", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "NCName", "NCName", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "ID", "ID", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "IDREF", "IDREF", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "IDREFS", "IDREFS", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "ENTITY", "ENTITY", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "ENTITIES", "ENTITIES", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "integer", "integer", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "nonPositiveInteger", "nonPositiveInteger", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "negativeInteger", "negativeInteger", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "long", "long", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "int", "int", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "short", "short", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "byte", "byte", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "nonNegativeInteger", "nonNegativeInteger", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "unsignedLong", "unsignedLong", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "unsignedInt", "unsignedInt", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "unsignedShort", "unsignedShort", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "unsignedByte", "unsignedByte", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "positiveInteger", "positiveInteger", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "yearMonthDuration", "yearMonthDuration", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "dayTimeDuration", "dayTimeDuration", Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, "dateTimeStamp", "dateTimeStamp", Constants.NS_XMLSCHEMA));
        
    } // initSupportedTypes
    
    
    // initializing the supported XML Schema facets
    private void initSupportedFacets() {
        
        facetsSupported.add(new QName(null, "length", "length", Constants.NS_XMLSCHEMA));        
        facetsSupported.add(new QName(null, "minLength", "minLength", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "maxLength", "maxLength", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "pattern", "pattern", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "enumeration", "enumeration", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "whiteSpace", "whiteSpace", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "maxInclusive", "maxInclusive", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "maxExclusive", "maxExclusive", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "minInclusive", "minInclusive", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "totalDigits", "totalDigits", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "minExclusive", "minExclusive", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "fractionDigits", "fractionDigits", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "maxScale", "maxScale", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "minScale", "minScale", Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, "assertion", "assertion", Constants.NS_XMLSCHEMA));        
        facetsSupported.add(new QName(null, "explicitTimezone", "explicitTimezone", Constants.NS_XMLSCHEMA));
        
    } // initSupportedFacets
    
    
    // Method to check if the schema type specified by method arguments (the QName
    // components of the schema type) is supported by Xerces-J natively.
    public boolean isTypeSupported(String localName, String uri) {
       boolean typeSupported = false;
       
       for (Iterator iter = typesSupported.iterator(); iter.hasNext(); ) {
          QName typeQname = (QName) iter.next();
          if (localName.equals(typeQname.localpart) &&
              uri.equals(typeQname.uri)) {
                typeSupported = true;
                break;
          }
       }
       
       return typeSupported;
       
    } // isTypeSupported 
    
    
    // Method to check if the schema facet specified by method arguments (the QName
    // components of a schema facet) is supported by Xerces-J natively.
    public boolean isFacetSupported(String localName, String uri) {
        boolean facetSupported = false;
        
        for (Iterator iter = facetsSupported.iterator(); iter.hasNext(); ) {
           QName typeQname = (QName) iter.next();
           if (localName.equals(typeQname.localpart) &&
               uri.equals(typeQname.uri)) {
                 facetSupported = true;
                 break;
           }
        }
        
        return facetSupported;
        
    } // isFacetSupported

}
