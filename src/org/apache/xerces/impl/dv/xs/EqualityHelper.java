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
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.xs.ListDV.ListData;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSConstants;

/**
 * @xerces.internal
 * 
 * @version $Id$
 */
public class EqualityHelper {

    // private constructor
    private EqualityHelper() {}

    // public methods
    
    /**
     * Compare 2 actual values
     */
    public static boolean isEqual(Object value1, Object value2,
            short value1Type, short value2Type,
            ShortList typeList1, ShortList typeList2,
            short schemaVersion) {
        if (schemaVersion == Constants.SCHEMA_VERSION_1_1) {
            return isEqual11(value1, value2, value1Type, value2Type, typeList1, typeList2);
        }
        
        return isEqual(value1, value2, value1Type, value2Type, typeList1, typeList2);
    }
    
    public static boolean isEqual(ValidatedInfo value1, ValidatedInfo value2, short schemaVersion) {
        if (schemaVersion == Constants.SCHEMA_VERSION_1_1) {
            return isEqual11(value1.actualValue, value2.actualValue,
                    value1.actualValueType, value2.actualValueType,
                    value1.itemValueTypes, value2.itemValueTypes);
        }
        
        return isEqual(value1.actualValue, value2.actualValue,
                value1.actualValueType, value2.actualValueType,
                value1.itemValueTypes, value2.itemValueTypes);
    }

    // private XML Schema 1.0 methods
    private static boolean isEqual(Object value1, Object value2,
            short value1Type, short value2Type,
            ShortList typeList1, ShortList typeList2) {

        if (!isTypeComparable(value1Type, value2Type, typeList1, typeList2)) {
            return false;
        }

        if (value1 == value2) {
            return true;
        }

        if (value1 == null || value2 == null) {
            return false;
        }

        return value1.equals(value2);
    }

    private static boolean isTypeComparable(short type1, short type2,
            ShortList typeList1, ShortList typeList2) {

        final short primitiveType1 = convertToPrimitiveKind(type1);
        final short primitiveType2 = convertToPrimitiveKind(type2);

        // Same types
        if (primitiveType1 == primitiveType2) {
            if (primitiveType1 == XSConstants.LIST_DT) {
                return isListTypeComparable(typeList1, typeList2);
            }

            return true;
        }

        // Different types
        return (primitiveType1 == XSConstants.ANYSIMPLETYPE_DT && primitiveType2 == XSConstants.STRING_DT ||
                primitiveType1 == XSConstants.STRING_DT && primitiveType2 == XSConstants.ANYSIMPLETYPE_DT);        
    }

    // private XML Schema 1.1 methods
    private static boolean isEqual11(Object value1, Object value2,
            short value1Type, short value2Type,
            ShortList typeList1, ShortList typeList2) {

        if (!isType11Comparable(value1Type, value2Type, typeList1, typeList2)) {
            return false;
        }

        if (value1 == value2) {
            return true;
        }

        if (value1 == null || value2 == null) {
            return false;
        }
        
        if (value1 instanceof ListData) {
            if (!(value2 instanceof ListData)) {
                final ListData listData = (ListData) value1;
                if (listData.getLength() != 1) {
                    return false;
                }
                value1 = listData.get(0);
            }
        }
        else if (value2 instanceof ListData) {
            final ListData listData = (ListData) value2;
            if (listData.getLength() != 1) {
                return false;
            }
            value2 = listData.get(0);
        }

        return value1.equals(value2);
    }
    
    private static boolean isType11Comparable(short type1, short type2,
            ShortList typeList1, ShortList typeList2) {

        final short primitiveType1 = convertToPrimitiveKind(type1);
        final short primitiveType2 = convertToPrimitiveKind(type2);

        // Same types
        if (primitiveType1 == primitiveType2) {
            if (primitiveType1 == XSConstants.LIST_DT) {
                return isListTypeComparable(typeList1, typeList2);
            }

            return true;
        }

        // Different types
        // Check singleton list vs atomic
        if (primitiveType1 == XSConstants.LIST_DT) {
            if (typeList1 == null || typeList1.getLength() != 1) {
                return false;
            }
            return isType11Comparable(typeList1.item(0), primitiveType2, null, null);
        }
        else if (primitiveType2 == XSConstants.LIST_DT) {
            if (typeList2 == null || typeList2.getLength() != 1) {
                return false;
            }
            return isType11Comparable(primitiveType1, typeList2.item(0), null, null);
        }

        return (primitiveType1 == XSConstants.ANYSIMPLETYPE_DT && primitiveType2 == XSConstants.STRING_DT ||
                primitiveType1 == XSConstants.STRING_DT && primitiveType2 == XSConstants.ANYSIMPLETYPE_DT);        
    }

    // Private common methods
    private static short convertToPrimitiveKind(short valueType) {
        /** Primitive datatypes. */
        if (valueType <= XSConstants.NOTATION_DT) {
            return valueType;
        }
        /** Types derived from string. */
        if (valueType <= XSConstants.ENTITY_DT) {
            return XSConstants.STRING_DT;
        }
        /** Types derived from decimal. */
        if (valueType <= XSConstants.POSITIVEINTEGER_DT) {
            return XSConstants.DECIMAL_DT;
        }
        
        /** List datatypes */
        if (valueType == XSConstants.LIST_DT || valueType == XSConstants.LISTOFUNION_DT) {
            return XSConstants.LIST_DT;
        }
        /** Other types. */
        return valueType;
    }

    private static boolean isListTypeComparable(ShortList typeList1, ShortList typeList2) {
        final int typeList1Length = typeList1 != null ? typeList1.getLength() : 0;
        final int typeList2Length = typeList2 != null ? typeList2.getLength() : 0;
        if (typeList1Length != typeList2Length) {
            return false;
        }
        for (int i = 0; i < typeList1Length; ++i) {
            final short primitiveItem1 = convertToPrimitiveKind(typeList1.item(i));
            final short primitiveItem2 = convertToPrimitiveKind(typeList2.item(i));
            if (primitiveItem1 != primitiveItem2) {
                if (primitiveItem1 == XSConstants.ANYSIMPLETYPE_DT && primitiveItem2 == XSConstants.STRING_DT ||
                    primitiveItem1 == XSConstants.STRING_DT && primitiveItem2 == XSConstants.ANYSIMPLETYPE_DT) {
                    continue;
                }
                return false;
            }
        }
        
        return true;
    }

}
