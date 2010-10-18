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

package org.apache.xerces.impl.scd;

/**
 * This class represents an Axis of a Step
 * @author Ishan Jayawardena udeshike@gmail.com
 * @version $Id$
 */
final class Axis {
    private Axis() {}
    public static final String[] AXIS_TYPES = { 
        "schemaAttribute", "schemaElement", "type", "attributeGroup", "group",
        "identityConstraint", "assertion", "alternative", "notation", "model",
        "anyAttribute", "any", "facet", "scope", "context",
        "substitutionGroup", "baseType", "itemType", "memberType", "primitiveType",
        "key", "annotation", "component", "currentComponent", "attributeUse",
        "particle", null};

    public static final short SCHEMA_ATTRIBUTE   = 0;              
    public static final short SCHEMA_ELEMENT     = 1;
    public static final short TYPE               = 2;
    public static final short ATTRIBUTE_GROUP    = 3;
    public static final short GROUP              = 4;
    public static final short IDENTITY_CONSTRAINT= 5;
    public static final short ASSERTION          = 6;
    public static final short ALTERNATIVE        = 7;
    public static final short NOTATION           = 8;
    public static final short MODEL              = 9;
    public static final short ANY_ATTRIBUTE      = 10;
    public static final short ANY                = 11;
    public static final short FACET              = 12;
    public static final short SCOPE              = 13;
    public static final short CONTEXT            = 14;
    public static final short SUBSTITUTION_GROUP = 15;
    public static final short BASE_TYPE          = 16;
    public static final short ITEM_TYPE          = 17;
    public static final short MEMBER_TYPE        = 18;
    public static final short PRIMITIVE_TYPE     = 19;
    public static final short KEY                = 20;
    public static final short ANNOTATION         = 21;
    public static final short COMPONENT          = 22;
    public static final short CURRENT_COMPONENT  = 23;
    public static final short ATTRIBUTE_USE      = 24;
    public static final short PARTICLE           = 25;
    public static final short EXTENSION_AXIS     = 26;
    public static final short SPECIAL_COMPONENT  = 27;

    private static final short UNKNOWN_AXIS      = -1;
    public static final short NO_AXIS           = 100;

    /**
     * returns the string representation of an axis
     * @param axis the input axis type
     * @return the name of the axis type as a string
     */
    public static String axisToString(short axis) {
        if (axis >= 0 && axis < AXIS_TYPES.length) {
            return AXIS_TYPES[axis];
        }
        return null;
    }

    /**
     * returns the axis type of a name
     * @param name : the qname that needs to be tested against the axis names
     * @return the axis type of the qname
     */
    public static short qnameToAxis(String name) {
        for (short i = 0; i < AXIS_TYPES.length; ++i) {
            if (AXIS_TYPES[i].equals(name)) {
                return i;
            }
        }
        return UNKNOWN_AXIS;
    }
} // class Axis