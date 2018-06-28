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

package org.apache.xerces.impl.xs.traversers;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.EqualityHelper;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.util.XInt;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.Element;

/**
 * The attribute declaration schema component traverser.
 *
 * &lt;attribute
 *   default = string
 *   fixed = string
 *   form = (qualified | unqualified)
 *   id = ID
 *   name = NCName
 *   ref = QName
 *   type = QName
 *   use = (optional | prohibited | required) : optional
 *   inheritable = boolean
 *   {any attributes with non-schema namespace . . .}&gt;
 *   Content: (annotation?, (simpleType?))
 * &lt;/attribute&gt;
 *
 * @xerces.internal 
 *
 * @author Sandy Gao, IBM
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @version $Id$
 */
class XSDAttributeTraverser extends XSDAbstractTraverser {
    
    public XSDAttributeTraverser (XSDHandler handler,
            XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    
    protected XSAttributeUseImpl traverseLocal(Element attrDecl,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar,
            XSObject enclosingParent) {
        
        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(attrDecl, false, schemaDoc);
        
        String defaultAtt = (String) attrValues[XSAttributeChecker.ATTIDX_DEFAULT];
        String fixedAtt   = (String) attrValues[XSAttributeChecker.ATTIDX_FIXED];
        String nameAtt    = (String) attrValues[XSAttributeChecker.ATTIDX_NAME];
        QName  refAtt     = (QName)  attrValues[XSAttributeChecker.ATTIDX_REF];
        XInt   useAtt     = (XInt)   attrValues[XSAttributeChecker.ATTIDX_USE];
        Boolean inheritableAtt = (Boolean) attrValues[XSAttributeChecker.ATTIDX_INHERITABLE];        
        
        // get 'attribute declaration'
        XSAttributeDecl attribute = null;
        XSAnnotationImpl annotation = null;
        if (attrDecl.getAttributeNode(SchemaSymbols.ATT_REF) != null) {
            if (refAtt != null) {
                attribute = (XSAttributeDecl)fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.ATTRIBUTE_TYPE, refAtt, attrDecl);
                
                Element child = DOMUtil.getFirstChildElement(attrDecl);
                if (child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    annotation = traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                    child = DOMUtil.getNextSiblingElement(child);
                }
                else {
                    String text = DOMUtil.getSyntheticAnnotation(attrDecl);
                    if (text != null) {
                        annotation = traverseSyntheticAnnotation(attrDecl, text, attrValues, false, schemaDoc);
                    }
                }

                if (child != null) {
                    reportSchemaError("src-attribute.3.2", new Object[]{refAtt.rawname}, child);
                }
                // for error reporting
                nameAtt = refAtt.localpart;
            } else {
                attribute = null;
            }
        } else {
            attribute = traverseNamedAttr(attrDecl, attrValues, schemaDoc, grammar, false, enclosingParent);
        }
        
        // get 'value constraint'
        short consType = XSConstants.VC_NONE;
        if (defaultAtt != null) {
            consType = XSConstants.VC_DEFAULT;
        } else if (fixedAtt != null) {
            consType = XSConstants.VC_FIXED;
            defaultAtt = fixedAtt;
            fixedAtt = null;
        }
        
        XSAttributeUseImpl attrUse = null;
        if (attribute != null) {
            if (fSchemaHandler.fDeclPool !=null) {
                attrUse = fSchemaHandler.fDeclPool.getAttributeUse();
            } else {
                attrUse = new XSAttributeUseImpl();
            }
            attrUse.fAttrDecl = attribute;
            attrUse.fUse = useAtt.shortValue();
            attrUse.fConstraintType = consType;
            if (defaultAtt != null) {
                attrUse.fDefault = new ValidatedInfo();
                attrUse.fDefault.normalizedValue = defaultAtt;
            }
            
            if (fSchemaHandler.fSchemaVersion >= Constants.SCHEMA_VERSION_1_1) {
                if (attrDecl.getAttributeNode(SchemaSymbols.ATT_INHERITABLE) != null) {
                    attrUse.fInheritable = inheritableAtt.booleanValue();
                }
                else {
                    attrUse.fInheritable = attribute.getInheritable();  
                }
            }
            else {
                attrUse.fInheritable = false;
            }
            
            // Get the annotation associated with the local attr decl
            if (attrDecl.getAttributeNode(SchemaSymbols.ATT_REF) == null) {
                attrUse.fAnnotations = attribute.getAnnotations();
            } else {
                XSObjectList annotations;
                if (annotation != null) {
                    annotations = new XSObjectListImpl();
                    ((XSObjectListImpl) annotations).addXSObject(annotation);
                } else {
                    annotations = XSObjectListImpl.EMPTY_LIST;
                }
                attrUse.fAnnotations = annotations;
            }
        }
        
        //src-attribute
        
        // 1 default and fixed must not both be present.
        if (defaultAtt != null && fixedAtt != null) {
            reportSchemaError("src-attribute.1", new Object[]{nameAtt}, attrDecl);
        }
        
        // 2 If default and use are both present, use must have the actual value optional.
        if (consType == XSConstants.VC_DEFAULT &&
                useAtt != null && useAtt.intValue() != SchemaSymbols.USE_OPTIONAL) {
            reportSchemaError("src-attribute.2", new Object[]{nameAtt}, attrDecl);
            // Recover by honouring the default value
            attrUse.fUse = SchemaSymbols.USE_OPTIONAL;
        }
        
        // 5 If fixed and use are both present, use must not have the actual value prohibited
        if (consType == XSConstants.VC_FIXED &&
                useAtt != null && useAtt.intValue() == SchemaSymbols.USE_PROHIBITED && fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            reportSchemaError("src-attribute.5", new Object[]{nameAtt}, attrDecl);
            // Recover by honouring the default value
            attrUse.fUse = SchemaSymbols.USE_OPTIONAL;
        }
        
        // a-props-correct
        
        if (defaultAtt != null && attrUse != null) {
            // 2 if there is a {value constraint}, the canonical lexical representation of its value must be valid with respect to the {type definition} as defined in String Valid (3.14.4).
            fValidationState.setNamespaceSupport(schemaDoc.fNamespaceSupport);
            fValidationState.setDatatypeXMLVersion(schemaDoc.fDatatypeXMLVersion);
            try {
                checkDefaultValid(attrUse);
            }
            catch (InvalidDatatypeValueException ide) {
                reportSchemaError (ide.getKey(), ide.getArgs(), attrDecl);
                reportSchemaError ("a-props-correct.2", new Object[]{nameAtt, defaultAtt}, attrDecl);
                // Recover by removing the default value
                attrUse.fDefault = null;
                attrUse.fConstraintType = XSConstants.VC_NONE;
            }
            
            // 3 If the {type definition} is or is derived from ID then there must not be a {value constraint}.
            //
            // Only applies to XML Schema 1.0
            if (fSchemaHandler.fSchemaVersion < Constants.SCHEMA_VERSION_1_1 && ((XSSimpleType)attribute.getTypeDefinition()).isIDType() ) {
                reportSchemaError ("a-props-correct.3", new Object[]{nameAtt}, attrDecl);
                // Recover by removing the default value
                attrUse.fDefault = null;
                attrUse.fConstraintType = XSConstants.VC_NONE;
            }
            
            // check 3.5.6 constraint
            // Attribute Use Correct
            // 2 If the {attribute declaration} has a fixed {value constraint}, then if the attribute use itself has a {value constraint}, it must also be fixed and its value must match that of the {attribute declaration}'s {value constraint}.
            if (attrUse.fAttrDecl.getConstraintType() == XSConstants.VC_FIXED &&
                    attrUse.fConstraintType != XSConstants.VC_NONE) {
                if (attrUse.fConstraintType != XSConstants.VC_FIXED ||
                        !EqualityHelper.isEqual(attrUse.fAttrDecl.getValInfo(), attrUse.fDefault, fSchemaHandler.fSchemaVersion)) {
                    reportSchemaError ("au-props-correct.2", new Object[]{nameAtt, attrUse.fAttrDecl.getValInfo().stringValue()}, attrDecl);
                    // Recover by using the decl's {value constraint}
                    attrUse.fDefault = attrUse.fAttrDecl.getValInfo();
                    attrUse.fConstraintType = XSConstants.VC_FIXED;
                }
            }
        }
        
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        return attrUse;
    }
    
    protected XSAttributeDecl traverseGlobal(Element attrDecl,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar) {
        
        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(attrDecl, true, schemaDoc);
        XSAttributeDecl attribute = traverseNamedAttr(attrDecl, attrValues, schemaDoc, grammar, true, null);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        return attribute;
        
    }
    
    /**
     * Traverse a globally declared attribute.
     *
     * @param  attrDecl
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @param  isGlobal
     * @return the attribute declaration index
     */
    XSAttributeDecl traverseNamedAttr(Element attrDecl,
            Object[] attrValues,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar,
            boolean isGlobal,
            XSObject enclosingParent) {
        
        String  defaultAtt     = (String) attrValues[XSAttributeChecker.ATTIDX_DEFAULT];
        String  fixedAtt       = (String) attrValues[XSAttributeChecker.ATTIDX_FIXED];
        XInt    formAtt        = (XInt)   attrValues[XSAttributeChecker.ATTIDX_FORM];
        String  nameAtt        = (String) attrValues[XSAttributeChecker.ATTIDX_NAME];
        String  targetNsAtt    = (String) attrValues[XSAttributeChecker.ATTIDX_TARGETNAMESPACE];
        QName   typeAtt        = (QName)  attrValues[XSAttributeChecker.ATTIDX_TYPE];
        Boolean  inheritableAtt   = (Boolean) attrValues[XSAttributeChecker.ATTIDX_INHERITABLE];
        
        // Step 1: get declaration information
        XSAttributeDecl attribute = null;
        if (fSchemaHandler.fDeclPool != null) {
            attribute = fSchemaHandler.fDeclPool.getAttributeDecl();
        } else {
            attribute = new XSAttributeDecl();
        }
        
        // get 'name'
        if (nameAtt != null)
            nameAtt = fSymbolTable.addSymbol(nameAtt);
        
        // get 'target namespace'
        String tnsAtt = null;
        XSComplexTypeDecl enclCT = null;
        XSObject enclParent = null;
        short scope = XSAttributeDecl.SCOPE_ABSENT;
        if (isGlobal) {
            tnsAtt = schemaDoc.fTargetNamespace;
            scope = XSAttributeDecl.SCOPE_GLOBAL;
        }
        else {
            // For XML 1.0, we only store information about enclosing complex types
            // For XML 1.1, we store information about enclosing complex types or attribute groups
            if (enclosingParent != null) {
                if (enclosingParent instanceof XSComplexTypeDecl) {
                    enclCT = (XSComplexTypeDecl) enclosingParent;
                    enclParent = enclosingParent;
                    scope = XSAttributeDecl.SCOPE_LOCAL;
                }
                else if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                    enclParent = enclosingParent;
                    scope = XSAttributeDecl.SCOPE_LOCAL;
                }
            }
            if (targetNsAtt != null) {
                // XML Schema 1.1, set the target namespace to be the value of the targetNamespace attribute if one is defined
                tnsAtt = fSymbolTable.addSymbol(targetNsAtt);
            } else if (formAtt != null) {
                if (formAtt.intValue() == SchemaSymbols.FORM_QUALIFIED)
                    tnsAtt = schemaDoc.fTargetNamespace;
            } else if (schemaDoc.fAreLocalAttributesQualified) {
                tnsAtt = schemaDoc.fTargetNamespace;
            } 
        }
        // get 'value constraint'
        // for local named attribute, value constraint is absent
        ValidatedInfo attDefault = null;
        short constraintType = XSConstants.VC_NONE;
        if (isGlobal) {
            if (fixedAtt != null) {
                attDefault = new ValidatedInfo();
                attDefault.normalizedValue = fixedAtt;
                constraintType = XSConstants.VC_FIXED;
            } else if (defaultAtt != null) {
                attDefault = new ValidatedInfo();
                attDefault.normalizedValue = defaultAtt;
                constraintType = XSConstants.VC_DEFAULT;
            } 
        }
        
        // get 'annotation'
        Element child = DOMUtil.getFirstChildElement(attrDecl);
        XSAnnotationImpl annotation = null;
        if (child != null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
            annotation = traverseAnnotationDecl(child, attrValues, false, schemaDoc);
            child = DOMUtil.getNextSiblingElement(child);
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(attrDecl);
            if (text != null) {
                annotation = traverseSyntheticAnnotation(attrDecl, text, attrValues, false, schemaDoc);
            }
        }
        
        // get 'type definition'
        XSSimpleType attrType = null;
        boolean haveAnonType = false;
        
        // Handle Anonymous type if there is one
        if (child != null) {
            String childName = DOMUtil.getLocalName(child);
            
            if (childName.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                attrType = fSchemaHandler.fSimpleTypeTraverser.traverseLocal(child, schemaDoc, grammar, attribute);
                haveAnonType = true;
                child = DOMUtil.getNextSiblingElement(child);
            }
        }
        
        // Handle type attribute
        if (attrType == null && typeAtt != null) {
            XSTypeDefinition type = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.TYPEDECL_TYPE, typeAtt, attrDecl);
            if (type != null && type.getTypeCategory() == XSTypeDefinition.SIMPLE_TYPE) {
                attrType = (XSSimpleType)type;
            }
            else {
                reportSchemaError("src-resolve", new Object[]{typeAtt.rawname, "simpleType definition"}, attrDecl);
                if (type == null) {
                	attribute.fUnresolvedTypeName = typeAtt;
                }
            }
        }
        
        if (attrType == null) {
            attrType = SchemaGrammar.fAnySimpleType;
        }
        
        XSObjectList annotations;
        if (annotation != null) {
            annotations = new XSObjectListImpl();
            ((XSObjectListImpl)annotations).addXSObject(annotation);
        } else {
            annotations = XSObjectListImpl.EMPTY_LIST;
        }
        
        boolean inheritable = false;
        if (inheritableAtt != null) {
            inheritable = inheritableAtt.booleanValue();   
        }
        
        attribute.setValues(nameAtt, tnsAtt, attrType, constraintType, scope,
                attDefault, enclParent, annotations, inheritable);
        
        // Step 3: check against schema for schemas
        
        // required attributes
        if (nameAtt == null) {
            if (isGlobal)
                reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_ATTRIBUTE, SchemaSymbols.ATT_NAME}, attrDecl);
            else
                reportSchemaError("src-attribute.3.1", null, attrDecl);
            nameAtt = NO_NAME;
        }
        
        // element
        if (child != null) {
            reportSchemaError("s4s-elt-must-match.1", new Object[]{nameAtt, "(annotation?, (simpleType?))", DOMUtil.getLocalName(child)}, child);
        }
        
        // Step 4: check 3.2.3 constraints
        
        // src-attribute
        
        // 1 default and fixed must not both be present.
        if (defaultAtt != null && fixedAtt != null) {
            reportSchemaError("src-attribute.1", new Object[]{nameAtt}, attrDecl);
        }
        
        // 2 If default and use are both present, use must have the actual value optional.
        // This is checked in "traverse" method
        
        // 3 If the item's parent is not <schema>, then all of the following must be true:
        // 3.1 One of ref or name must be present, but not both.
        // This is checked in XSAttributeChecker
        
        // 3.2 If ref is present, then all of <simpleType>, form and type must be absent.
        // Attributes are checked in XSAttributeChecker, elements are checked in "traverse" method
        
        // 4 type and <simpleType> must not both be present.
        if (haveAnonType && (typeAtt != null)) {
            reportSchemaError( "src-attribute.4", new Object[]{nameAtt}, attrDecl);
        }
        
        // Step 5: check 3.2.6 constraints
        // check for NOTATION type
        // Only for XML Schema 1.0
        if (fSchemaHandler.fSchemaVersion < Constants.SCHEMA_VERSION_1_1) {
            checkNotationType(nameAtt, attrType, attrDecl);
        }
        
        // 6 If the targetNamespace attribute is present then all of the following must be true:         
        if (targetNsAtt != null) {
            // 6.2 The form attribute must not be present. 
            if (formAtt != null) {
                reportSchemaError ("src-attribute.6.2", new Object[] {nameAtt}, attrDecl);                
            }
            // 6.3 If the ancestor <schema> does not have a targetNamespace [attribute] or its 'actual value' is different from the 'actual value' of targetNamespace of <attribute>:
            String schemaTns = schemaDoc.fTargetNamespace;
            if (schemaTns==null || tnsAtt!=schemaTns) {
                // 6.3.1 <attribute> must have <complexType> as an ancestor
                if (enclCT == null) {
                    reportSchemaError ("src-attribute.6.3.1", new Object[] {nameAtt}, attrDecl);
                }
                // 6.3.2 There must be a <restriction> ancestor between the <attribute> and the nearest <complexType> ancestor, and the 'actual value' of the base [attribute] of <restriction> does not 'match' the name of 'xs:anyType'.
                else if ((enclCT.getDerivationMethod() != XSConstants.DERIVATION_RESTRICTION) || enclCT.getBaseType() == SchemaGrammar.getXSAnyType(fSchemaHandler.fSchemaVersion)) {
                    reportSchemaError ("src-attribute.6.3.2", new Object[] {nameAtt}, attrDecl);
                }
            }
        }
        
        // a-props-correct
        
        // 2 if there is a {value constraint}, the canonical lexical representation of its value must be valid with respect to the {type definition} as defined in String Valid (3.14.4).
        if (attDefault != null) {
            fValidationState.setNamespaceSupport(schemaDoc.fNamespaceSupport);
            fValidationState.setDatatypeXMLVersion(schemaDoc.fDatatypeXMLVersion);
            try {
                checkDefaultValid(attribute);
            }
            catch (InvalidDatatypeValueException ide) {
                reportSchemaError (ide.getKey(), ide.getArgs(), attrDecl);
                reportSchemaError ("a-props-correct.2", new Object[]{nameAtt, attDefault.normalizedValue}, attrDecl);
                // Recover by removing the default value
                attDefault = null;
                constraintType = XSConstants.VC_NONE;
                attribute.setValues(nameAtt, tnsAtt, attrType, constraintType, scope,
                        attDefault, enclParent, annotations, inheritable);
            }
        }
        
        // 3 If the {type definition} is or is derived from ID then there must not be a {value constraint}.
        //
        // Only applies to XML Schema 1.0
        if (fSchemaHandler.fSchemaVersion < Constants.SCHEMA_VERSION_1_1 && attDefault != null) {
            if (attrType.isIDType() ) {
                reportSchemaError ("a-props-correct.3", new Object[]{nameAtt}, attrDecl);
                // Recover by removing the default value
                attDefault = null;
                constraintType = XSConstants.VC_NONE;
                attribute.setValues(nameAtt, tnsAtt, attrType, constraintType, scope,
                        attDefault, enclParent, annotations, inheritable);
            }
        }
        
        // no-xmlns
        
        // The {name} of an attribute declaration must not match xmlns.
        if (nameAtt != null && nameAtt.equals(XMLSymbols.PREFIX_XMLNS)) {
            reportSchemaError("no-xmlns", null, attrDecl);
            return null;
        }
        
        // no-xsi
        
        // The {target namespace} of an attribute declaration, whether local or top-level, must not match http://www.w3.org/2001/XMLSchema-instance (unless it is one of the four built-in declarations given in the next section).
        if (tnsAtt != null && tnsAtt.equals(SchemaSymbols.URI_XSI)) {
            reportSchemaError("no-xsi", new Object[]{SchemaSymbols.URI_XSI}, attrDecl);
            return null;
        }
        
        // Attribute without a name. Return null.
        if (nameAtt.equals(NO_NAME))
            return null;

        // Step 2: register attribute decl to the grammar
        if (isGlobal) {
            if (grammar.getGlobalAttributeDecl(nameAtt) == null) {
                grammar.addGlobalAttributeDecl(attribute);
            }

            // also add it to extended map
            final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
            final XSAttributeDecl attribute2 = grammar.getGlobalAttributeDecl(nameAtt, loc);
            if (attribute2  == null) {
                grammar.addGlobalAttributeDecl(attribute, loc);
            }

            if (fSchemaHandler.fTolerateDuplicates) {
                if (attribute2  != null) {
                    attribute = attribute2;
                }
                fSchemaHandler.addGlobalAttributeDecl(attribute);
            }
        }

        return attribute;
    }

    // throws an error if the constraint value is invalid for the given type
    void checkDefaultValid(XSAttributeDecl attribute) throws InvalidDatatypeValueException {
        // validate the original lexical rep, and set the actual value
        ((XSSimpleType)attribute.getTypeDefinition()).validate(attribute.getValInfo().normalizedValue, fValidationState, attribute.getValInfo());
        // validate the canonical lexical rep
        ((XSSimpleType)attribute.getTypeDefinition()).validate(attribute.getValInfo().stringValue(), fValidationState, attribute.getValInfo());
    }
    
    // throws an error if the constraint value is invalid for the given type
    void checkDefaultValid(XSAttributeUseImpl attrUse) throws InvalidDatatypeValueException {
        // validate the original lexical rep, and set the actual value
        ((XSSimpleType)attrUse.fAttrDecl.getTypeDefinition()).validate(attrUse.fDefault.normalizedValue, fValidationState, attrUse.fDefault);
        // validate the canonical lexical rep
        ((XSSimpleType)attrUse.fAttrDecl.getTypeDefinition()).validate(attrUse.fDefault.stringValue(), fValidationState, attrUse.fDefault);
    }
    
}
