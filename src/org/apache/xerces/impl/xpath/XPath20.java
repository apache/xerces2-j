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

import javax.xml.XMLConstants;

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
    protected final NamespaceContext fNsContext;

    private XPathSyntaxTreeNode fRootNode;

    public XPath20(String xpath, SymbolTable symbolTable, NamespaceContext nsContext) throws XPathException {
        fExpression = xpath;
        fNsContext = nsContext;
        
        //The parser expects '\n' at the end of the expression. So insert it.
        StringReader reader = new StringReader(fExpression + "\n");
        XPath20Parser parser = new XPath20Parser(reader, nsContext);
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
            return fRootNode.evaluate(element, attributes, fNsContext);
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getXPathStrValue() {
       return fExpression; 
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

    public abstract boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception;
    
    public Object getValue(XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        return null;
    }
    
    public String getStringValue() {
        return null;
    }

    public int getType() {
        return TYPE_UNDEFINED;
    }

    public XSSimpleType getSimpleType() {
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

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        Object obj = getValue(attributes, nsContext);
        if (isNumeric) {
            return obj != null && 0.0 != ((Double) obj).doubleValue();
        } 
        else {
            return obj != null;
        }
    }

    public Object getValue(XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
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

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        boolean lhs = child1.evaluate(element, attributes, nsContext);
        boolean rhs = child2.evaluate(element, attributes, nsContext);
        if (conjunction == OR) {
            return lhs || rhs;
        } else {
            return lhs && rhs;
        }
    }
}

class AttrNode extends XPathSyntaxTreeNode {
    private QName name;

    public AttrNode(QName name) {
        this.name = name;
    }

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        String attrValue = attributes.getValue(name.uri, name.localpart);
        if (attrValue == null || attrValue.length() == 0) {
            return false;
        }
        return true;
    }

    public Object getValue(XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        String attrValue = attributes.getValue(name.uri, name.localpart);
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

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        int type1 = child1.getType();
        int type2 = child2.getType();
        Object obj1, obj2;
        XSSimpleTypeDecl simpleType;

        if (type1 == TYPE_UNTYPED && type2 == TYPE_DOUBLE) {
            // attribute and numeral
            String attrValue = child1.getValue(attributes, nsContext).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            //cast the attribute value into double as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrValue, null, null);
            obj2 = child2.getValue(attributes, nsContext);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_STRING) {
            // attribute and string
            String attrValue = child1.getValue(attributes, nsContext).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            //cast the attribute value into string as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrValue, null, null);
            obj2 = child2.getValue(attributes, nsContext);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_DOUBLE && type2 == TYPE_UNTYPED) {
            // numeral and attribute
            String attrValue = child2.getValue(attributes, nsContext).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            obj1 = child1.getValue(attributes, nsContext);
            //cast the attribute value into double as per the XPath 2.0 general comparison rules
            obj2 = simpleType.validate(attrValue, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_STRING && type2 == TYPE_UNTYPED) {
            // string and attribute
            String attrValue = child2.getValue(attributes, nsContext).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            obj1 = child1.getValue(attributes, nsContext);
            //cast the attribute value into string as per the XPath 2.0 general comparison rules
            obj2 = simpleType.validate(attrValue, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_UNTYPED) {
            // attr and attr
            String attrVal1 = child1.getValue(attributes, nsContext).toString();
            String attrVal2 = child2.getValue(attributes, nsContext).toString();
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            //cast the both attribute values into string as per the XPath 2.0 general comparison rules
            obj1 = simpleType.validate(attrVal1, null, null);
            obj2 = simpleType.validate(attrVal2, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_UNTYPED && type2 == TYPE_OTHER) {
            // attr and cast expr
            String attrVal = child1.getValue(attributes, nsContext).toString();
            
            simpleType = (XSSimpleTypeDecl)child2.getSimpleType();
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            //try to cast the attribute value into the type of the cast expression
            obj1 = simpleType.validate(attrVal, null, null);
            obj2 = child2.getValue(attributes, nsContext);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_OTHER && type2 == TYPE_UNTYPED) {
            // cast expr and attr
            String attrVal = child2.getValue(attributes, nsContext).toString();
            
            simpleType = (XSSimpleTypeDecl)child1.getSimpleType();
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            obj1 = child1.getValue(attributes, nsContext);
            //try to cast the attribute value into the type of the cast expression
            obj2 = simpleType.validate(attrVal, null, null);
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_OTHER && type2 == TYPE_OTHER) {
            //cast expr and cast expr
            simpleType = (XSSimpleTypeDecl)child1.getSimpleType();
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            short dt1 = simpleType.getBuiltInKind();
            
            simpleType = (XSSimpleTypeDecl)child2.getSimpleType();
            if (simpleType == null) {
                throw new XPathException("Casted type is not a built-in type");
            }
            short dt2 = simpleType.getBuiltInKind();
            
            // check whether the two types are comparable
            if (DataMatcher.isComparable(dt1, dt2, null, null)) {
                obj1 = simpleType.validate(child1.getValue(attributes, nsContext), null, null);
                obj2 = child2.getValue(attributes, nsContext);
                return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            } else {
                throw new XPathException("Invalid comparison between incompatible types");
            }
            
        } else if (type1 == TYPE_DOUBLE && type2 == TYPE_DOUBLE) {
            // numeric and numeric
            obj1 = child1.getValue(attributes, nsContext);
            obj2 = child2.getValue(attributes, nsContext);
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("double");
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else if (type1 == TYPE_STRING && type2 == TYPE_STRING) {
            // string and string
            obj1 = child1.getValue(attributes, nsContext);
            obj2 = child2.getValue(attributes, nsContext);
            simpleType = (XSSimpleTypeDecl) dvFactory.getBuiltInType("string");
            return DataMatcher.compareActualValues(obj1, obj2, comp, simpleType);
            
        } else {
            throw new XPathException("Invalid comparison");
        }
 
    }
}

class CastNode extends XPathSyntaxTreeNode {
    private XSSimpleType castedType;
    private XPathSyntaxTreeNode child;

    public CastNode(XPathSyntaxTreeNode child, QName type) throws XPathException {
        this.child = child;
        if (!XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.uri)) {
            throw new XPathException("Casted type is not a built-in type");
        }
        castedType = dvFactory.getBuiltInType(type.localpart);
        if (castedType == null) {
            throw new XPathException("Casted type is not a built-in type");
        }
    }

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        Object obj = getValue(attributes, nsContext);
        // Implement XPath fn:boolean() function:
        // If $arg is the empty sequence, fn:boolean returns false.
        // TODO: how is this checked?
        if (obj == null) {
            return false;
        }
        // If $arg is a sequence whose first item is a node, fn:boolean returns true.
        // TODO: how to implement this?
        // If $arg is a singleton value of type xs:boolean or a derived from xs:boolean, fn:boolean returns $arg.
        if (obj instanceof Boolean) {
            return ((Boolean)obj).booleanValue();
        }
        // If $arg is a singleton value of type xs:string or a type derived from xs:string, xs:anyURI or a type derived from xs:anyURI or xs:untypedAtomic, fn:boolean returns false if the operand value has zero length; otherwise it returns true.
        if (obj instanceof String) {
            return ((String)obj).length() > 0;
        }
        // If $arg is a singleton value of any numeric type or a type derived from a numeric type, fn:boolean returns false if the operand value is NaN or is numerically equal to zero; otherwise it returns true.
        // TODO: should check type and handle different numeric types differntly
        if (obj instanceof Double) {
            return ((Double)obj).doubleValue() != 0;
        }
        // In all other cases, fn:boolean raises a type error [err:FORG0006].
        // TODO: need to distinguish between node (true) and other simple types (false).
        return true;
    }

    public Object getValue(XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        XSSimpleType type = getSimpleType();
        
        Object obj;
        if (child.getType() == TYPE_UNTYPED) {
            //attribute cast
            String attrValue = child.getValue(attributes, nsContext).toString();
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

    public XSSimpleType getSimpleType() {
        return castedType;
    }

    public int getType() {
        if (castedType.getNumeric()) {
            return TYPE_DOUBLE;
        } else if (castedType.getName().equals("string")) {
            return TYPE_STRING;
        } else {
            return TYPE_OTHER;
        }
    }
}

class FunctionNode extends XPathSyntaxTreeNode {
    private QName name;
    private XPathSyntaxTreeNode child;

    public FunctionNode(QName name, XPathSyntaxTreeNode child) throws XPathException {
        if (!"not".equals(name.localpart) || !"http://www.w3.org/2005/xpath-functions".equals(name.uri)) {
            throw new XPathException("Only support fn:not function.");
        }
        this.name = name;
        this.child = child;
    }

    public boolean evaluate(QName element, XMLAttributes attributes, NamespaceContext nsContext) throws Exception {
        return !child.evaluate(element, attributes, nsContext);
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


