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

package org.apache.xerces.impl.xpath;

import java.io.StringReader;

import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.TypeValidator;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.XSConstants;

/**
 * Bare minimal XPath 2.0 implementation for schema type alternatives.
 *
 * @author Hiranya Jayathilaka, University of Moratuwa
 * @version $Id$
 */
public class XPath20 {

    protected final String fExpression;
    protected final NamespaceContext fContext;

    private XPathSyntaxTreeNode fRootNode;

    public XPath20(String xpath, SymbolTable symbolTable, NamespaceContext context) throws XPathException {
        fExpression = xpath;
        fContext = context;
        
        //The parser expects '\n' at the end of the expression. So insert it.
        StringReader reader = new StringReader(fExpression + "\n");
        XPath20Parser parser = new XPath20Parser(reader);
        fRootNode = parser.parseExpression();
    }

    /**
     * Evaluate the test XPath expression using the attribute information and element information.
     * 
     * @param element QName of the current element
     * @param attributes attributes collection of the current element
     * @return true if the test evaluates successfully and false otherwise
     */
    public boolean evaluateTest(QName element, XMLAttributes attributes) {
        try {
            return fRootNode.evaluate(element, attributes);
        } catch (Exception e) {
            return false;
        }
    }

}

abstract class XPathSyntaxTreeNode {

    public static final int TYPE_UNDEFINED  = -1;
    public static final int TYPE_DOUBLE     = 0;    //for all numerics (double, integer, float, decimal etc)
    public static final int TYPE_STRING     = 1;    //for strings (xs:string)
    public static final int TYPE_UNTYPED    = 2;    //for attributes (untypedAtomic)
    public static final int TYPE_OTHER      = 3;    //for everything else which should have some valid type

    private static final String SCHEMA11_FACTORY_CLASS = "org.apache.xerces.impl.dv.xs.Schema11DVFactoryImpl";
    protected static SchemaDVFactory dvFactory;
    
    static {
        dvFactory = SchemaDVFactory.getInstance(SCHEMA11_FACTORY_CLASS);
    }

    public abstract boolean evaluate(QName element, XMLAttributes attributes) throws Exception;
    
    public Object getValue(XMLAttributes attributes) throws Exception {
        return null;
    }
    
    public String getStringValue() {
        return null;
    }

    public int getType() {
        return TYPE_UNDEFINED;
    }

    public String getTypeName() {
        return null;
    }
}

class LiteralNode extends XPathSyntaxTreeNode {
    private String value;
    private boolean isNumeric;

    public LiteralNode(String value, boolean isNumeric) {
        this.value = value;
        this.isNumeric = isNumeric;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) throws Exception {
        Object obj = getValue(attributes);
        if (isNumeric) {
            return obj != null && 0.0 != ((Double) obj).doubleValue();
        } 
        else {
            return obj != null;
        }
    }

    public Object getValue(XMLAttributes attributes) throws Exception {
        XSSimpleType type;
        if (isNumeric) {
            type = dvFactory.getBuiltInType("double");
        } else {
            type = dvFactory.getBuiltInType("string");
        }
        return type.validate(value, null, null);
    }
    
    public String getStringValue() {
        return value;
    }

    public int getType() {
        return isNumeric ? TYPE_DOUBLE : TYPE_STRING;
    }
}

class ConjunctionNode extends XPathSyntaxTreeNode {
    private int conjunction;
    private XPathSyntaxTreeNode child1;
    private XPathSyntaxTreeNode child2;

    public static final int OR = 0;
    public static final int AND = 1;

    public ConjunctionNode(int conjunction, XPathSyntaxTreeNode child1, XPathSyntaxTreeNode child2) {
        this.conjunction = conjunction;
        this.child1 = child1;
        this.child2 = child2;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) throws Exception {
        boolean lhs = child1.evaluate(element, attributes);
        boolean rhs = child2.evaluate(element, attributes);
        if (conjunction == OR) {
            return lhs || rhs;
        } else {
            return lhs && rhs;
        }
    }
}

class AttrNode extends XPathSyntaxTreeNode {
    private String name;

    public AttrNode(String name) {
        this.name = name;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) throws Exception {
        String attrValue = attributes.getValue(name);
        if (attrValue == null || attrValue.length() == 0) {
            return false;
        }
        return true;
    }

    public Object getValue(XMLAttributes attributes) throws Exception {
        String attrValue = attributes.getValue(name);
        if (attrValue == null) {
            throw new XPathException("Attribute value is null");
        }
        return attrValue;
    }

    public int getType() {
        return TYPE_UNTYPED;
    }
}

class CompNode extends XPathSyntaxTreeNode {
    private int comp;
    private XPathSyntaxTreeNode child1;
    private XPathSyntaxTreeNode child2;

    public static final int EQ = 0;
    public static final int NE = 1;
    public static final int LT = 2;
    public static final int GT = 3;
    public static final int LE = 4;
    public static final int GE = 5;

    public CompNode(int comp, XPathSyntaxTreeNode child1,
            XPathSyntaxTreeNode child2) {
        this.comp = comp;
        this.child1 = child1;
        this.child2 = child2;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) throws Exception {
        int type1 = child1.getType();
        int type2 = child2.getType();
        Object obj1, obj2;
        XSSimpleTypeDecl simpleType;

        if (type1 == TYPE_UNTYPED && type2 == TYPE_DOUBLE) {
            // attribute and numeral
            String attrValue = child1.getValue(attributes).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            //cast the attribute value into double as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrValue, null, null);
            obj2 = child2.getValue(attributes);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_STRING) {
            // attribute and string
            String attrValue = child1.getValue(attributes).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            //cast the attribute value into string as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrValue, null, null);
            obj2 = child2.getValue(attributes);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_DOUBLE && type2 == TYPE_UNTYPED) {
            // numeral and attribute
            String attrValue = child2.getValue(attributes).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            obj1 = child1.getValue(attributes);
            //cast the attribute value into double as per the XPath 2.0 general comparison rules
            obj2 = simpleType.validate(attrValue, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_STRING && type2 == TYPE_UNTYPED) {
            // string and attribute
            String attrValue = child2.getValue(attributes).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            obj1 = child1.getValue(attributes);
            //cast the attribute value into string as per the XPath 2.0 general comparison rules
            obj2 = simpleType.validate(attrValue, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_UNTYPED) {
            // attr and attr
            String attrVal1 = child1.getValue(attributes).toString();
            String attrVal2 = child2.getValue(attributes).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            //cast the both attribute values into string as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrVal1, null, null);
            obj2 = simpleType.validate(attrVal2, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_OTHER) {
            // attr and cast expr
            String type = child2.getTypeName();
            String attrVal = child1.getValue(attributes).toString();
            
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(type);
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            //try to cast the attribute value into the type of the cast expression
            obj1 = simpleType.validate(attrVal, null, null);
            obj2 = child2.getValue(attributes);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_OTHER && type2 == TYPE_UNTYPED) {
            // cast expr and attr
            String type = child1.getTypeName();
            String attrVal = child2.getValue(attributes).toString();
            
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(type);
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            obj1 = child1.getValue(attributes);
            //try to cast the attribute value into the type of the cast expression
            obj2 = simpleType.validate(attrVal, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_OTHER && type2 == TYPE_OTHER) {
            //cast expr and cast expr
            String typeName1 = child1.getTypeName();
            String typeName2 = child2.getTypeName();
            
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(typeName1);
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            short dt1 = simpleType.getBuiltInKind();
            
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(typeName2);
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            short dt2 = simpleType.getBuiltInKind();
            
            // check whether the two types are comparable
            if (DataMatcher.isComparable(dt1, dt2, null, null)) {
                obj1 = simpleType.validate(child1.getValue(attributes), null, null);
                obj2 = child2.getValue(attributes);
                return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            } else {
                throw new XPathException("Invalid comparison between incompatible types");
            }
            
        } else if (type1 == TYPE_DOUBLE && type2 == TYPE_DOUBLE) {
            // numeric and numeric
            obj1 = child1.getValue(attributes);
            obj2 = child2.getValue(attributes);
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_STRING && type2 == TYPE_STRING) {
            // string and string
            obj1 = child1.getValue(attributes);
            obj2 = child2.getValue(attributes);
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else {
            throw new XPathException("Invalid comparison");
        }
 
    }
}

class CastNode extends XPathSyntaxTreeNode {
    private String castedType;
    private XPathSyntaxTreeNode child;

    public CastNode(XPathSyntaxTreeNode child, String castedType) {
        this.child = child;
        this.castedType = castedType;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) throws Exception {
        Object obj = getValue(attributes);
        XSSimpleTypeDecl simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(getTypeName());
        if (simpleType.getNumeric()) {
            return obj != null && 0.0 != ((Double) obj).doubleValue();
        } 
        else {
            return obj != null;
        }
    }

    public Object getValue(XMLAttributes attributes) throws Exception {
        XSSimpleType type = dvFactory.getBuiltInType(getTypeName());
        if (type == null) {
            throw new XPathException("Casted type is not a built-in type");
        }
        
        Object obj;
        if (child.getType() == TYPE_UNTYPED) {
            //attribute cast
            String attrValue = child.getValue(attributes).toString();
            obj = type.validate(attrValue, null, null);
        } else {
            //literal cast (perform using the string value of the literal node)
            obj = type.validate(child.getStringValue(), null, null);
        }
        
        //Workaround (this is here because double validator can only validate double values)
        if (type.getNumeric()) {
           obj = dvFactory.getBuiltInType("double").validate(obj, null, null);
        }
        return obj;
    }

    public String getTypeName() {
        String localname = castedType;
        int index = localname.indexOf(':');
        if (index != -1) {
            localname = localname.substring(index + 1);
        }
        return localname;
    }

    public int getType() {
        String type = getTypeName();
        XSSimpleTypeDecl simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType(type);
        if (simpleType.getNumeric()) {
            return TYPE_DOUBLE;
        } else if (type.equals("string")) {
            return TYPE_STRING;
        } else {
            return TYPE_OTHER;
        }
    }
}

class FunctionNode extends XPathSyntaxTreeNode {
    private String name;
    private XPathSyntaxTreeNode child;

    public FunctionNode(String name, XPathSyntaxTreeNode child) {
        this.name = name;
        this.child = child;
    }

    public boolean evaluate(QName element, XMLAttributes attributes) {
        return false;
    }

    public String getValue(QName element, XMLAttributes attributes) {
        return null;
    }
}

class DataMatcher {

    public static boolean compareActualValues(Object value1, Object value2, int comparator, XSSimpleTypeDecl type) {

        TypeValidator typeValidator = type.getTypeValidator();
        short ordered = type.getOrdered();

        if (ordered == XSSimpleTypeDecl.ORDERED_FALSE) {
            //if the type is not ordered then only equality can be tested. delegate the test to the type.
            if (comparator == CompNode.EQ) {
                return type.isEqual(value1, value2);
            }
            else if (comparator == CompNode.NE) {
                return !type.isEqual(value1, value2);
            }
            else {
                //only equality can be tested upon unordered types
                return false;
            }
        }

        //if the type is ordered then the corresponding TypeValidator should know how to compare the values
        switch (comparator) {
           case CompNode.EQ: return typeValidator.compare(value1, value2) == 0;
           case CompNode.NE: return typeValidator.compare(value1, value2) != 0;
           case CompNode.GT: return typeValidator.compare(value1, value2) > 0;
           case CompNode.GE: return typeValidator.compare(value1, value2) >= 0;
           case CompNode.LT: return typeValidator.compare(value1, value2) < 0;
           case CompNode.LE: return typeValidator.compare(value1, value2) <= 0;
        }
        
        return false;
        
    }

    /**
     * Checks whether two specified data types are comparable. The types passed
     * into this method should be defined in XSConstants as *_DT values.
     */
    public static boolean isComparable(short type1, short type2, ShortList typeList1, ShortList typeList2) {

        short primitiveType1 = convertToPrimitiveKind(type1);
        short primitiveType2 = convertToPrimitiveKind(type2);

        if (primitiveType1 != primitiveType2) {
            return (primitiveType1 == XSConstants.ANYSIMPLETYPE_DT && primitiveType2 == XSConstants.STRING_DT ||
                    primitiveType1 == XSConstants.STRING_DT && primitiveType2 == XSConstants.ANYSIMPLETYPE_DT);
        }
        else if (primitiveType1 == XSConstants.LIST_DT || primitiveType1 == XSConstants.LISTOFUNION_DT) {
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
        }
        return true;
    }

    private static short convertToPrimitiveKind(short valueType) {
        //Primitive data types
        if (valueType <= XSConstants.NOTATION_DT) {
            return valueType;
        }
        // Types derived from string
        if (valueType <= XSConstants.ENTITY_DT) {
            return XSConstants.STRING_DT;
        }
        // Types derived from decimal
        if (valueType <= XSConstants.POSITIVEINTEGER_DT) {
            return XSConstants.DECIMAL_DT;
        }
        // Other types
        return valueType;
    }
    
} // class XPath20


