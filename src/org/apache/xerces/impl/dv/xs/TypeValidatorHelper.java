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

package org.apache.xerces.impl.dv.xs;

import org.apache.xerces.impl.Constants;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public abstract class TypeValidatorHelper {

    // Constants
    private static int FACETS_GROUP1 = XSSimpleTypeDecl.FACET_NONE;
    
    private static int FACETS_GROUP2 = XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_WHITESPACE;
    
    private static int FACETS_GROUP3 = FACETS_GROUP2 | XSSimpleTypeDecl.FACET_LENGTH |
        XSSimpleTypeDecl.FACET_MINLENGTH | XSSimpleTypeDecl.FACET_MAXLENGTH |
        XSSimpleTypeDecl.FACET_ENUMERATION;

    private static int FACETS_GROUP4 = FACETS_GROUP2 | XSSimpleTypeDecl.FACET_ENUMERATION |
        XSSimpleTypeDecl.FACET_MAXINCLUSIVE | XSSimpleTypeDecl.FACET_MININCLUSIVE |
        XSSimpleTypeDecl.FACET_MAXEXCLUSIVE | XSSimpleTypeDecl.FACET_MINEXCLUSIVE;

    private static int FACETS_GROUP5 = FACETS_GROUP4 | XSSimpleTypeDecl.FACET_TOTALDIGITS |
        XSSimpleTypeDecl.FACET_FRACTIONDIGITS;
    
    private static int FACETS_GROUP6 = XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_ENUMERATION;

    private static int FACETS_GROUP7 = FACETS_GROUP2 | XSSimpleTypeDecl.FACET_ASSERT;

    private static int FACETS_GROUP8 = FACETS_GROUP3 | XSSimpleTypeDecl.FACET_ASSERT;

    private static int FACETS_GROUP9 = FACETS_GROUP4 | XSSimpleTypeDecl.FACET_ASSERT;
    
    private static int FACETS_GROUP10 = FACETS_GROUP9 | XSSimpleTypeDecl.FACET_EXPLICITTIMEZONE;

    private static int FACETS_GROUP11 = FACETS_GROUP5 | XSSimpleTypeDecl.FACET_ASSERT;

    private static int FACETS_GROUP12 = FACETS_GROUP6 | XSSimpleTypeDecl.FACET_ASSERT;
    
    private static int FACETS_GROUP13 = FACETS_GROUP4 | XSSimpleTypeDecl.FACET_TOTALDIGITS |
        XSSimpleTypeDecl.FACET_ASSERT | XSSimpleTypeDecl.FACET_MAXSCALE | XSSimpleTypeDecl.FACET_MINSCALE;

    // Static data
    private static final TypeValidatorHelper fHelper1_0 = new TypeValidatorHelper1_0();
    private static final TypeValidatorHelper fHelper1_1 = new TypeValidatorHelper1_1();

    // Methods
    public static TypeValidatorHelper getInstance(short schemaVersion) {
        if (schemaVersion < Constants.SCHEMA_VERSION_1_1) {
            return fHelper1_0;
        }
        return fHelper1_1;
    }

    public boolean isXMLSchema11() {
        return false;
    }

    public abstract int getAllowedFacets(short validationDV);

    // Constructor
    protected TypeValidatorHelper() {}

    // Inner classes

    // XML Schema 1.0 
    static class TypeValidatorHelper1_0 extends TypeValidatorHelper {

        // Data
        protected static int[] fAllowedFacets;

        static {
            createAllowedFacets();
        }

        // create allowed facets
        static void createAllowedFacets() {
            fAllowedFacets = new int[] {
                FACETS_GROUP1, // anySimpleType
                FACETS_GROUP3, // string
                FACETS_GROUP2, // boolean
                FACETS_GROUP5, // decimal
                FACETS_GROUP4, // float
                FACETS_GROUP4, // double
                FACETS_GROUP4, // duration
                FACETS_GROUP4, // dateTime
                FACETS_GROUP4, // time
                FACETS_GROUP4, // date
                FACETS_GROUP4, // gYearMonth
                FACETS_GROUP4, // gYear
                FACETS_GROUP4, // gMonthDay
                FACETS_GROUP4, // gDay
                FACETS_GROUP4, // gMonth
                FACETS_GROUP3, // hexBinary
                FACETS_GROUP3, // base64Binary
                FACETS_GROUP3, // anyURI
                FACETS_GROUP3, // QName
                FACETS_GROUP1, // precisionDecimal
                FACETS_GROUP3, // NOTATION
                FACETS_GROUP3, // ID
                FACETS_GROUP3, // IDREF
                FACETS_GROUP3, // ENTITY
                FACETS_GROUP5, // integer
                FACETS_GROUP3, // list
                FACETS_GROUP6, // union
                FACETS_GROUP1, // gYearMonthDuration
                FACETS_GROUP1, // gDayTimeDuration
                FACETS_GROUP1, // anyAtomic
                FACETS_GROUP1, // error
                FACETS_GROUP1  // dateTimeStamp
            };
        }

        // TypeValidator abstract methods
        public int getAllowedFacets(short validationDV) {
            return (validationDV < fAllowedFacets.length) ? fAllowedFacets[validationDV] : FACETS_GROUP1;
        }
    }

    // XML Schema 1.1 
    static class TypeValidatorHelper1_1 extends TypeValidatorHelper {

        // Data
        protected static int[] fAllowedFacets;

        static {
            createAllowedFacets();
        }

        // create allowed facets
        static void createAllowedFacets() {
            fAllowedFacets = new int[] {
                FACETS_GROUP1,  //anySimpleType
                FACETS_GROUP8,  // string
                FACETS_GROUP7,  // boolean
                FACETS_GROUP11, // decimal
                FACETS_GROUP9,  // float
                FACETS_GROUP9,  // double
                FACETS_GROUP9,  // duration
                FACETS_GROUP10, // dateTime
                FACETS_GROUP10, // time
                FACETS_GROUP10, // date
                FACETS_GROUP10, // gYearMonth
                FACETS_GROUP10, // gYear
                FACETS_GROUP10, // gMonthDay
                FACETS_GROUP10, // gDay
                FACETS_GROUP10, // gMonth
                FACETS_GROUP8,  // hexBinary
                FACETS_GROUP8,  // base64Binary
                FACETS_GROUP8,  // anyURI
                FACETS_GROUP8,  // QName
                FACETS_GROUP13, // precisionDecimal
                FACETS_GROUP8,  // NOTATION
                FACETS_GROUP8,  // ID
                FACETS_GROUP8,  // IDREF
                FACETS_GROUP8,  // ENTITY
                FACETS_GROUP11, // integer
                FACETS_GROUP8,  // list
                FACETS_GROUP12, // union
                FACETS_GROUP9,  // gYearMonthDuration
                FACETS_GROUP9,  // gDayTimeDuration
                FACETS_GROUP1,  // anyAtomic
                FACETS_GROUP1,  // error
                FACETS_GROUP10  // dateTimeStamp
            };
        }

        // TypeValidator abstract methods

        public int getAllowedFacets(short validationDV) {
            return (validationDV < fAllowedFacets.length) ? fAllowedFacets[validationDV] : FACETS_GROUP1;
        }
        
        public boolean isXMLSchema11() {
            return true;
        }
    }
}
