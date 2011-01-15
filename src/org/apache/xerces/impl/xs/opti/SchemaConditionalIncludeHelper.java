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
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.xni.QName;

/**
 * This class provides supporting functionality for XML Schema conditional include
 * pre-processing (newly introduced in XML Schema 1.1).
 * 
 * ref: http://www.w3.org/TR/xmlschema11-1/#cip
 * 
 * @xerces.internal
 * 
 * @author Mukul Gandhi, IBM
 * 
 * @version $Id$
 */
public class SchemaConditionalIncludeHelper {
    
    // instance variables holding "natively supported" XML Schema types and facets by Xerces-J XML Schema 1.1 engine.    
    List typesSupported = null;
    List facetsSupported = null;
    
    
    /*
     * Class constructor.
     */
    public SchemaConditionalIncludeHelper() {
        typesSupported = new ArrayList();
        facetsSupported = new ArrayList();
        initialize();
    }
    
    
    /*
     * Initializing the supported XML Schema types and facets.
     */
    private void initialize() {
        initSupportedTypes();
        initSupportedFacets();
    }
    
    
    /*
     * Initializing the supported XML Schema types.
     */
    private void initSupportedTypes() {
        
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ANYTYPE, 
                                     SchemaSymbols.ATTVAL_ANYTYPE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ANYSIMPLETYPE, 
                                     SchemaSymbols.ATTVAL_ANYSIMPLETYPE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ANYATOMICTYPE, 
                                     SchemaSymbols.ATTVAL_ANYATOMICTYPE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_STRING, 
                                     SchemaSymbols.ATTVAL_STRING, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_BOOLEAN, 
                                     SchemaSymbols.ATTVAL_BOOLEAN, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DECIMAL, 
                                     SchemaSymbols.ATTVAL_DECIMAL, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_PRECISIONDECIMAL, 
                                     SchemaSymbols.ATTVAL_PRECISIONDECIMAL, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_FLOAT, 
                                     SchemaSymbols.ATTVAL_FLOAT, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DOUBLE, 
                                     SchemaSymbols.ATTVAL_DOUBLE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DURATION, 
                                     SchemaSymbols.ATTVAL_DURATION, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DATETIME, 
                                     SchemaSymbols.ATTVAL_DATETIME, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_TIME, 
                                     SchemaSymbols.ATTVAL_TIME, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DATE, 
                                     SchemaSymbols.ATTVAL_DATE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_YEARMONTH, 
                                     SchemaSymbols.ATTVAL_YEARMONTH, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_YEAR, 
                                     SchemaSymbols.ATTVAL_YEAR, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_MONTHDAY, 
                                     SchemaSymbols.ATTVAL_MONTHDAY, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DAY, 
                                     SchemaSymbols.ATTVAL_DAY, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_MONTH, 
                                     SchemaSymbols.ATTVAL_MONTH, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_HEXBINARY, 
                                     SchemaSymbols.ATTVAL_HEXBINARY, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_BASE64BINARY, 
                                     SchemaSymbols.ATTVAL_BASE64BINARY, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ANYURI, 
                                     SchemaSymbols.ATTVAL_ANYURI, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_QNAME, 
                                     SchemaSymbols.ATTVAL_QNAME, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NOTATION, 
                                     SchemaSymbols.ATTVAL_NOTATION, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NORMALIZEDSTRING, 
                                     SchemaSymbols.ATTVAL_NORMALIZEDSTRING, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_TOKEN, 
                                     SchemaSymbols.ATTVAL_TOKEN, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_LANGUAGE, 
                                     SchemaSymbols.ATTVAL_LANGUAGE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NMTOKEN, 
                                     SchemaSymbols.ATTVAL_NMTOKEN, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NMTOKENS, 
                                     SchemaSymbols.ATTVAL_NMTOKENS, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NAME, 
                                     SchemaSymbols.ATTVAL_NAME, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NCNAME, 
                                     SchemaSymbols.ATTVAL_NCNAME, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ID, 
                                     SchemaSymbols.ATTVAL_ID, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_IDREF, 
                                     SchemaSymbols.ATTVAL_IDREF, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_IDREFS, 
                                     SchemaSymbols.ATTVAL_IDREFS, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ENTITY, 
                                     SchemaSymbols.ATTVAL_ENTITY, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_ENTITIES, 
                                     SchemaSymbols.ATTVAL_ENTITIES, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_INTEGER, 
                                     SchemaSymbols.ATTVAL_INTEGER, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, 
                                     SchemaSymbols.ATTVAL_NONPOSITIVEINTEGER, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NEGATIVEINTEGER, 
                                     SchemaSymbols.ATTVAL_NEGATIVEINTEGER, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_LONG, 
                                     SchemaSymbols.ATTVAL_LONG, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_INT, 
                                     SchemaSymbols.ATTVAL_INT, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_SHORT, 
                                     SchemaSymbols.ATTVAL_SHORT, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_BYTE, 
                                     SchemaSymbols.ATTVAL_BYTE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, 
                                     SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_UNSIGNEDLONG, 
                                     SchemaSymbols.ATTVAL_UNSIGNEDLONG, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_UNSIGNEDINT, 
                                     SchemaSymbols.ATTVAL_UNSIGNEDINT, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_UNSIGNEDSHORT, 
                                     SchemaSymbols.ATTVAL_UNSIGNEDSHORT, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_UNSIGNEDBYTE, 
                                     SchemaSymbols.ATTVAL_UNSIGNEDBYTE, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_POSITIVEINTEGER, 
                                     SchemaSymbols.ATTVAL_POSITIVEINTEGER, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_YEARMONTHDURATION, 
                                     SchemaSymbols.ATTVAL_YEARMONTHDURATION, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DAYTIMEDURATION, 
                                     SchemaSymbols.ATTVAL_DAYTIMEDURATION, Constants.NS_XMLSCHEMA));
        typesSupported.add(new QName(null, SchemaSymbols.ATTVAL_DATETIMESTAMP, 
                                     SchemaSymbols.ATTVAL_DATETIMESTAMP, Constants.NS_XMLSCHEMA));
        
    } // initSupportedTypes
    
    
    /*
     * Initializing the supported XML Schema facets.
     */
    private void initSupportedFacets() {
        
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_LENGTH, 
                                      SchemaSymbols.ELT_LENGTH, Constants.NS_XMLSCHEMA));        
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MINLENGTH, 
                                      SchemaSymbols.ELT_MINLENGTH, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MAXLENGTH, 
                                      SchemaSymbols.ELT_MAXLENGTH, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_PATTERN, 
                                      SchemaSymbols.ELT_PATTERN, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_ENUMERATION, 
                                      SchemaSymbols.ELT_ENUMERATION, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_WHITESPACE, 
                                      SchemaSymbols.ELT_WHITESPACE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MAXINCLUSIVE, 
                                      SchemaSymbols.ELT_MAXINCLUSIVE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MAXEXCLUSIVE, 
                                      SchemaSymbols.ELT_MAXEXCLUSIVE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MININCLUSIVE, 
                                      SchemaSymbols.ELT_MININCLUSIVE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_TOTALDIGITS, 
                                      SchemaSymbols.ELT_TOTALDIGITS, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MINEXCLUSIVE, 
                                      SchemaSymbols.ELT_MINEXCLUSIVE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_FRACTIONDIGITS, 
                                      SchemaSymbols.ELT_FRACTIONDIGITS, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MAXSCALE, 
                                      SchemaSymbols.ELT_MAXSCALE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_MINSCALE, 
                                      SchemaSymbols.ELT_MINSCALE, Constants.NS_XMLSCHEMA));
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_ASSERTION, 
                                      SchemaSymbols.ELT_ASSERTION, Constants.NS_XMLSCHEMA));        
        facetsSupported.add(new QName(null, SchemaSymbols.ELT_EXPLICITTIMEZONE, 
                                      SchemaSymbols.ELT_EXPLICITTIMEZONE, Constants.NS_XMLSCHEMA));
        
    } // initSupportedFacets
    
    
    /* 
     * Method to check if a schema type specified by method arguments (the QName components of the schema type) is
     * supported by Xerces-J natively.
     */
    public boolean isTypeSupported(String localName, String uri) {
        
       boolean typeSupported = false;
       
       for (Iterator iter = typesSupported.iterator(); iter.hasNext(); ) {
          QName typeQname = (QName) iter.next();
          if (typeQname.localpart.equals(localName) && typeQname.uri.equals(uri)) {
              typeSupported = true;
              break;
          }
       }
       
       return typeSupported;
       
    } // isTypeSupported 
    
    
    /* 
     * Method to check if a schema facet specified by method arguments (the QName components of a schema facet) is 
     * supported by Xerces-J natively.
     */
    public boolean isFacetSupported(String localName, String uri) {
        
        boolean facetSupported = false;
        
        for (Iterator iter = facetsSupported.iterator(); iter.hasNext(); ) {
           QName typeQname = (QName) iter.next();
           if (typeQname.localpart.equals(localName) && typeQname.uri.equals(uri)) {
               facetSupported = true;
               break;
           }
        }
        
        return facetSupported;
        
    } // isFacetSupported

} // class SchemaConditionalIncludeHelper
