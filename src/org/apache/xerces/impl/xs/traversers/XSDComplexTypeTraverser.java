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

import java.util.Vector;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.dv.InvalidDatatypeFacetException;
import org.apache.xerces.impl.dv.XSFacets;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xpath.XPath20Assert;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaNamespaceSupport;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAnnotationImpl;
import org.apache.xerces.impl.xs.XSAttributeGroupDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSOpenContentDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.impl.xs.assertion.Test;
import org.apache.xerces.impl.xs.assertion.XSAssertImpl;
import org.apache.xerces.impl.xs.util.XInt;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.w3c.dom.Element;

/**
 * A complex type definition schema component traverser.
 *
 * <complexType
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction)) 
 *   final = (#all | List of (extension | restriction)) 
 *   id = ID
 *   mixed = boolean
 *   name = NCName
 *   defaultAttributesApply = boolean : true
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleContent | complexContent | (openContent?, (group | all |
 *   choice | sequence)?, ((attribute | attributeGroup)*, anyAttribute?), assert*)))
 * </complexType>
 * 
 * @xerces.internal  
 * 
 * @version $Id$
 */

class  XSDComplexTypeTraverser extends XSDAbstractParticleTraverser {
    
    // size of stack to hold globals:
    private final static int GLOBAL_NUM = 13;
    
    private static XSParticleDecl fErrorContent = null;
    private static XSWildcardDecl fErrorWildcard = null;
    private static XSParticleDecl getErrorContent() {
        if (fErrorContent == null) {
            XSParticleDecl particle = new XSParticleDecl();
            particle.fType = XSParticleDecl.PARTICLE_WILDCARD;
            particle.fValue = getErrorWildcard();
            particle.fMinOccurs = 0;
            particle.fMaxOccurs = SchemaSymbols.OCCURRENCE_UNBOUNDED;
            XSModelGroupImpl group = new XSModelGroupImpl();
            group.fCompositor = XSModelGroupImpl.MODELGROUP_SEQUENCE;
            group.fParticleCount = 1;
            group.fParticles = new XSParticleDecl[1];
            group.fParticles[0] = particle;
            XSParticleDecl errorContent = new XSParticleDecl();
            errorContent.fType = XSParticleDecl.PARTICLE_MODELGROUP;
            errorContent.fValue = group;
            fErrorContent = errorContent;
        }
        return fErrorContent;
    }
    private static XSWildcardDecl getErrorWildcard() {
        if (fErrorWildcard == null) {
            XSWildcardDecl wildcard = new XSWildcardDecl();
            wildcard.fProcessContents = XSWildcardDecl.PC_SKIP;
            fErrorWildcard = wildcard;
        }
        return fErrorWildcard;
    }
    
    // globals for building XSComplexTypeDecls
    private String fName = null;
    private String fTargetNamespace = null;
    private short fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
    private short fFinal = XSConstants.DERIVATION_NONE;
    private short fBlock = XSConstants.DERIVATION_NONE;
    private short fContentType = XSComplexTypeDecl.CONTENTTYPE_EMPTY;
    private XSTypeDefinition fBaseType = null;
    private XSAttributeGroupDecl fAttrGrp = null;
    private XSSimpleType fXSSimpleType = null;
    private XSParticleDecl fParticle = null;
    private boolean fIsAbstract = false;
    private XSComplexTypeDecl fComplexTypeDecl = null;
    private XSAnnotationImpl [] fAnnotations = null;
    private XSOpenContentDecl fOpenContent = null;
    private XSAssertImpl[] fAssertions = null;
    
    // our own little stack to retain state when getGlobalDecls is called:
    private Object [] fGlobalStore = null;
    private int fGlobalStorePos = 0;
    
    XSDComplexTypeTraverser (XSDHandler handler,
            XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }
    
    
    private static final boolean DEBUG=false;
    
    private static final class ComplexTypeRecoverableError extends Exception {
        
        private static final long serialVersionUID = 6802729912091130335L;
        
        Object[] errorSubstText=null;
        Element  errorElem = null;
        ComplexTypeRecoverableError() {
            super();
        }
        ComplexTypeRecoverableError(String msgKey, Object[] args, Element e) {
            super(msgKey);
            errorSubstText=args;
            errorElem = e;
        }
        
    }
    
    /**
     * Traverse local complexType declarations
     *
     * @param Element
     * @param XSDocumentInfo
     * @param SchemaGrammar
     * @return XSComplexTypeDecl
     */
    XSComplexTypeDecl traverseLocal(Element complexTypeNode,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar,
            XSObject context) {
        
        
        Object[] attrValues = fAttrChecker.checkAttributes(complexTypeNode, false,
                schemaDoc);
        String complexTypeName = genAnonTypeName(complexTypeNode);
        contentBackup();
        XSComplexTypeDecl type = traverseComplexTypeDecl (complexTypeNode,
                complexTypeName, attrValues, schemaDoc, grammar, context);
        contentRestore();
        // need to add the type to the grammar for later constraint checking
        grammar.addComplexTypeDecl(type, fSchemaHandler.element2Locator(complexTypeNode));
        type.setIsAnonymous();
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
        return type;
    }
    
    /**
     * Traverse global complexType declarations
     *
     * @param Element
     * @param XSDocumentInfo
     * @param SchemaGrammar
     * @return XSComplexTypeDecXSComplexTypeDecl
     */
    XSComplexTypeDecl traverseGlobal (Element complexTypeNode,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar) {
        
        Object[] attrValues = fAttrChecker.checkAttributes(complexTypeNode, true,
                schemaDoc);
        String complexTypeName = (String)  attrValues[XSAttributeChecker.ATTIDX_NAME];
        contentBackup();
        XSComplexTypeDecl type = traverseComplexTypeDecl (complexTypeNode,
                complexTypeName, attrValues, schemaDoc, grammar, null);
        contentRestore();
        // need to add the type to the grammar for later constraint checking
        grammar.addComplexTypeDecl(type, fSchemaHandler.element2Locator(complexTypeNode));

        if (complexTypeName == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_COMPLEXTYPE, SchemaSymbols.ATT_NAME}, complexTypeNode);
            type = null;
        } else {

            // XML Schema 1.1
            // If parent of complex type is redefine, then we need to set the
            // context of the redefined complex type
            if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                Element parent = DOMUtil.getParent(complexTypeNode);
                if (DOMUtil.getLocalName(parent).equals(SchemaSymbols.ELT_REDEFINE)) {
                    ((XSComplexTypeDecl)type.getBaseType()).setContext(type);
                }
            }
            
            if (grammar.getGlobalTypeDecl(type.getName()) == null) {
                grammar.addGlobalComplexTypeDecl(type);
            }
            
            // also add it to extended map
            final String loc = fSchemaHandler.schemaDocument2SystemId(schemaDoc);
            final XSTypeDefinition type2 = grammar.getGlobalTypeDecl(type.getName(), loc); 
            if (type2 == null) {
                grammar.addGlobalComplexTypeDecl(type, loc);
            }

            // handle duplicates
            if (fSchemaHandler.fTolerateDuplicates) {
                if (type2 != null) {
                    if (type2 instanceof XSComplexTypeDecl) {
                        type = (XSComplexTypeDecl) type2;
                    }
                }
                fSchemaHandler.addGlobalTypeDecl(type);
            }
        }

        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
        return type;
    }

    XSOpenContentDecl traverseOpenContent(Element elmNode,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar,
            boolean isDefault) {

        // General Attribute Checking for elmNode
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, isDefault, schemaDoc);

        // Create open content declaration
        XSOpenContentDecl ocDecl = new XSOpenContentDecl();

        // get attribute values
        final XInt modeAttr = (XInt) attrValues[XSAttributeChecker.ATTIDX_MODE];
        final short ocMode = modeAttr.shortValue();
        
        if (isDefault) {
            final Boolean appliesToEmptyAttr = ((Boolean) attrValues[XSAttributeChecker.ATTIDX_APPLIESTOEMPTY]);
            ocDecl.fAppliesToEmpty = appliesToEmptyAttr.booleanValue();
        }
        ocDecl.fMode = ocMode;

        // ---------------------------------------------------------------
        // First, handle any ANNOTATION declaration and get next child
        // ---------------------------------------------------------------
        Element child = DOMUtil.getFirstChildElement(elmNode);
        if (child != null) {
            if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                addAnnotation(traverseAnnotationDecl(child, attrValues, false, schemaDoc));
                child = DOMUtil.getNextSiblingElement(child);
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(elmNode);
                if (text != null) {
                    addAnnotation(traverseSyntheticAnnotation(elmNode, text, attrValues, false, schemaDoc));
                }
            }
            if (child !=null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                reportSchemaError("s4s-elt-invalid-content.1",
                        new Object[]{SchemaSymbols.ELT_OPENCONTENT,SchemaSymbols.ELT_ANNOTATION}, child);
            }
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(elmNode);
            if (text != null) {
                addAnnotation(traverseSyntheticAnnotation(elmNode, text, attrValues, false, schemaDoc));
            }
        }

        // ---------------------------------------------------------------
        // Process the wildcard
        // ---------------------------------------------------------------
        if (child == null) {
            if (ocMode != XSOpenContentDecl.MODE_NONE) {
                reportSchemaError("src-ct.6", new Object[]{fName}, elmNode);
            }
        }
        else {
            String childName = DOMUtil.getLocalName(child);
            if (!childName.equals(SchemaSymbols.ELT_ANY) || 
                    DOMUtil.getNextSiblingElement(child) != null) {                
                reportSchemaError("s4s-elt-must-match.1",
                                    new Object[]{SchemaSymbols.ELT_OPENCONTENT, "(annotation?, any?)", childName}, child);
                fAttrChecker.returnAttrArray(attrValues, schemaDoc);
                return ocDecl;
            }
            Object[] wcAttrValues = fAttrChecker.checkAttributes(child, false, schemaDoc);
            ocDecl.fWildcard = fSchemaHandler.fWildCardTraverser.traverseWildcardDecl(child, wcAttrValues, schemaDoc, grammar);
            fAttrChecker.returnAttrArray(wcAttrValues, schemaDoc);
        }

        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        return ocDecl;
    }

    private XSComplexTypeDecl traverseComplexTypeDecl(Element complexTypeDecl,
            String complexTypeName,
            Object[] attrValues,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar,
            XSObject context) {
        
        fComplexTypeDecl = new XSComplexTypeDecl();
        fAttrGrp = new XSAttributeGroupDecl();
        Boolean abstractAtt  = (Boolean) attrValues[XSAttributeChecker.ATTIDX_ABSTRACT];
        XInt    blockAtt     = (XInt)    attrValues[XSAttributeChecker.ATTIDX_BLOCK];
        Boolean mixedAtt     = (Boolean) attrValues[XSAttributeChecker.ATTIDX_MIXED];
        XInt    finalAtt     = (XInt)    attrValues[XSAttributeChecker.ATTIDX_FINAL];
        
        fName = complexTypeName;
        fComplexTypeDecl.setName(fName);
        fTargetNamespace = schemaDoc.fTargetNamespace;
        
        fBlock = blockAtt == null ? schemaDoc.fBlockDefault : blockAtt.shortValue();
        fFinal = finalAtt == null ? schemaDoc.fFinalDefault : finalAtt.shortValue();
        //discard valid Block/Final 'Default' values that are invalid for Block/Final
        fBlock &= (XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION);
        fFinal &= (XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION);
        
        fIsAbstract = (abstractAtt != null && abstractAtt.booleanValue());
        fAnnotations = null;
        fOpenContent = null;
        fAssertions = null;
        
        Element child = null;
        
        try {
            // XML Schema 1.1 - {attribute uses}
            //
            // If the defaultAttributesApply [attribute] of the <complexType> element is not present
            // or has actual value 'true', and the <schema> ancestor has an defaultAttributes attribute,
            // then properties {attribute uses} and {attribute wildcard} are computed as if there were
            // an <attributeGroup> [child] with empty content and a ref [attribute] whose actual value is
            // the same as that of the defaultAttributes [attribute].
            if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                /* NOTE: default value is true; i.e. if attribute is missing its value defaults to true */
                Boolean defaultAttributeAppliesAttr = (Boolean) attrValues[XSAttributeChecker.ATTIDX_DEFAULTATTRAPPLY];
                if (defaultAttributeAppliesAttr.booleanValue() == true && schemaDoc.fDefaultAGroup != null) {
                    mergeAttributes(schemaDoc.fDefaultAGroup, fAttrGrp, fName, true, complexTypeDecl);
                }
            }

            // ---------------------------------------------------------------
            // First, handle any ANNOTATION declaration and get next child
            // ---------------------------------------------------------------
            child = DOMUtil.getFirstChildElement(complexTypeDecl);
            if(child != null) {
                if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    addAnnotation(traverseAnnotationDecl(child, attrValues, false, schemaDoc));
                    child = DOMUtil.getNextSiblingElement(child);
                }
                else {
                    String text = DOMUtil.getSyntheticAnnotation(complexTypeDecl);
                    if (text != null) {
                        addAnnotation(traverseSyntheticAnnotation(complexTypeDecl, text, attrValues, false, schemaDoc));
                    }
                }
                if (child !=null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                            new Object[]{fName,SchemaSymbols.ELT_ANNOTATION},
                            child);
                }
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(complexTypeDecl);
                if (text != null) {
                    addAnnotation(traverseSyntheticAnnotation(complexTypeDecl, text, attrValues, false, schemaDoc));
                }
            }
            // ---------------------------------------------------------------
            // Process the content of the complex type definition
            // ---------------------------------------------------------------
            if (child==null) {
                //
                // EMPTY complexType with complexContent
                //
                
                // set the base to the anyType
                fBaseType = SchemaGrammar.getXSAnyType(fSchemaHandler.fSchemaVersion);
                fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
                
                // xsd 1.1 - the baseType and derivedBy fields are required to be set on the 
                // complex type declaration for later checking for targetNamespace constraints
                // note that the default value for derivedBy is restriction
                if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                    fComplexTypeDecl.setBaseType(fBaseType);
                }
                
                processComplexContent(child, mixedAtt.booleanValue(), false,
                        schemaDoc, grammar);
            }
            else if (DOMUtil.getLocalName(child).equals
                    (SchemaSymbols.ELT_SIMPLECONTENT)) {
                //
                // SIMPLE CONTENT
                //
                traverseSimpleContent(child, schemaDoc, grammar);
                Element elemTmp = DOMUtil.getNextSiblingElement(child);
                if (elemTmp != null) {
                    String siblingName = DOMUtil.getLocalName(elemTmp);
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                            new Object[]{fName,siblingName},
                            elemTmp);
                }
            }
            else if (DOMUtil.getLocalName(child).equals
                    (SchemaSymbols.ELT_COMPLEXCONTENT)) {
                traverseComplexContent(child, mixedAtt.booleanValue(),
                        schemaDoc, grammar);
                Element elemTmp = DOMUtil.getNextSiblingElement(child);
                if (elemTmp != null) {
                    String siblingName = DOMUtil.getLocalName(elemTmp);
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                            new Object[]{fName,siblingName},
                            elemTmp);
                }
            }
            else {
                //
                // We must have ....
                // GROUP, ALL, SEQUENCE or CHOICE, followed by optional
                // attributes and assertions
                // Note that it's possible that only attributes are specified.
                //
                
                // set the base to the anyType
                fBaseType = SchemaGrammar.getXSAnyType(fSchemaHandler.fSchemaVersion);
                fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
                
                // xsd 1.1 - these fields are set on the complex type declaration
                // for later checking for targetNamespace constraints
                // note that the default value for derivedBy is restriction
                if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                    fComplexTypeDecl.setBaseType(fBaseType);
                }
                
                processComplexContent(child, mixedAtt.booleanValue(), false,
                        schemaDoc, grammar);
            }
            
        }
        catch (ComplexTypeRecoverableError e) {
            handleComplexTypeError(e.getMessage(), e.errorSubstText,
                    e.errorElem);
        }
        
        if (DEBUG) {
            System.out.println(fName);
        }
        fComplexTypeDecl.setValues(fName, fTargetNamespace, fBaseType,
                fDerivedBy, fFinal, fBlock, fContentType, fIsAbstract,
                fAttrGrp, fXSSimpleType, fParticle, new XSObjectListImpl(fAnnotations, 
                        fAnnotations == null? 0 : fAnnotations.length), fOpenContent);

        // XML Schema 1.1
        // Store context information
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fComplexTypeDecl.setContext(context);
        }

        fComplexTypeDecl.setAssertions(fAssertions != null 
                ? new XSObjectListImpl(fAssertions, fAssertions.length) : null);
        return fComplexTypeDecl;
    }
    
    
    private void traverseSimpleContent(Element simpleContentElement,
            XSDocumentInfo schemaDoc,
            SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {
        
        
        Object[] simpleContentAttrValues = fAttrChecker.checkAttributes(simpleContentElement, false,
                schemaDoc);
        
        // -----------------------------------------------------------------------
        // Set content type
        // -----------------------------------------------------------------------
        fContentType = XSComplexTypeDecl.CONTENTTYPE_SIMPLE;
        fParticle = null;
        
        Element simpleContent = DOMUtil.getFirstChildElement(simpleContentElement);
        if (simpleContent != null && DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
            addAnnotation(traverseAnnotationDecl(simpleContent, simpleContentAttrValues, false, schemaDoc));
            simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(simpleContentElement);
            if (text != null) {
                addAnnotation(traverseSyntheticAnnotation(simpleContentElement, text, simpleContentAttrValues, false, schemaDoc));
            }
        }
        
        // If there are no children, return
        if (simpleContent==null) {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.2",
                    new Object[]{fName,SchemaSymbols.ELT_SIMPLECONTENT},
                    simpleContentElement);
        }
        
        // -----------------------------------------------------------------------
        // The content should be either "restriction" or "extension"
        // -----------------------------------------------------------------------
        String simpleContentName = DOMUtil.getLocalName(simpleContent);
        if (simpleContentName.equals(SchemaSymbols.ELT_RESTRICTION))
            fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
        else if (simpleContentName.equals(SchemaSymbols.ELT_EXTENSION)) {
            fDerivedBy = XSConstants.DERIVATION_EXTENSION;
            // xsd 1.1 - the derivedBy field is required to be set on the complex type 
            // declaration for later checking for targetNamespace constraints
            if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                fComplexTypeDecl.setDerivationMethod(XSConstants.DERIVATION_EXTENSION);
            }
        }
        else {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                    new Object[]{fName,simpleContentName},
                    simpleContent);
        }
        Element elemTmp = DOMUtil.getNextSiblingElement(simpleContent);
        if (elemTmp != null) {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            String siblingName = DOMUtil.getLocalName(elemTmp);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                    new Object[]{fName,siblingName},
                    elemTmp);
        }
        
        Object [] derivationTypeAttrValues = fAttrChecker.checkAttributes(simpleContent, false,
                schemaDoc);
        QName baseTypeName = (QName)  derivationTypeAttrValues[XSAttributeChecker.ATTIDX_BASE];
        
        
        // -----------------------------------------------------------------------
        // Need a base type.
        // -----------------------------------------------------------------------
        if (baseTypeName==null) {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-att-must-appear",
                    new Object[]{simpleContentName, "base"}, simpleContent);
        }
        
        XSTypeDefinition type = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc,
                XSDHandler.TYPEDECL_TYPE, baseTypeName,
                simpleContent);
        if (type==null) {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError();
        }
        
        fBaseType = type;
        
        // xsd 1.1 - the baseType field is set on the complex type declaration
        // for later checking for targetNamespace constraints
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fComplexTypeDecl.setBaseType(fBaseType);
        }
        
        XSSimpleType baseValidator = null;
        XSComplexTypeDecl baseComplexType = null;
        int baseFinalSet = 0;
        
        // If the base type is complex, it must have simpleContent
        if ((type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)) {
            
            baseComplexType = (XSComplexTypeDecl)type;
            baseFinalSet = baseComplexType.getFinal();
            // base is a CT with simple content (both restriction and extension are OK)
            if (baseComplexType.getContentType() == XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                baseValidator = (XSSimpleType)baseComplexType.getSimpleType();
            }
            // base is a CT with mixed/emptiable content (only restriction is OK)
            else if (fDerivedBy == XSConstants.DERIVATION_RESTRICTION &&
                    baseComplexType.getContentType() == XSComplexTypeDecl.CONTENTTYPE_MIXED &&
                    ((XSParticleDecl)baseComplexType.getParticle()).emptiable()) {
            }
            else {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("src-ct.2.1",
                        new Object[]{fName, baseComplexType.getName()}, simpleContent);
            }
        }
        else {
            baseValidator = (XSSimpleType)type;
            // base is a ST (only extension is OK)
            if (fDerivedBy == XSConstants.DERIVATION_RESTRICTION) {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("src-ct.2.1",
                        new Object[]{fName, baseValidator.getName()}, simpleContent);
            }
            baseFinalSet=baseValidator.getFinal();
        }
        
        // -----------------------------------------------------------------------
        // Check that the base permits the derivation
        // -----------------------------------------------------------------------
        if ((baseFinalSet & fDerivedBy)!=0) {
            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            String errorKey = (fDerivedBy==XSConstants.DERIVATION_EXTENSION) ?
                    "cos-ct-extends.1.1" : "derivation-ok-restriction.1";
            throw new ComplexTypeRecoverableError(errorKey,
                    new Object[]{fName, fBaseType.getName()}, simpleContent);
        }
        
        // -----------------------------------------------------------------------
        // Skip over any potential annotations
        // -----------------------------------------------------------------------
        Element scElement = simpleContent;
        simpleContent = DOMUtil.getFirstChildElement(simpleContent);
        if (simpleContent != null) {
            // traverse annotation if any
            
            if (DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                addAnnotation(traverseAnnotationDecl(simpleContent, derivationTypeAttrValues, false, schemaDoc));
                simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(scElement);
                if (text != null) {
                    addAnnotation(traverseSyntheticAnnotation(scElement, text, derivationTypeAttrValues, false, schemaDoc));
                }
            }
            
            if (simpleContent !=null &&
                    DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)){
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                        new Object[]{fName,SchemaSymbols.ELT_ANNOTATION},
                        simpleContent);
            }
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(scElement);
            if (text != null) {
                addAnnotation(traverseSyntheticAnnotation(scElement, text, derivationTypeAttrValues, false, schemaDoc));
            }
        }
        
        // add any assertions from the base types, for assertions to be processed
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            addAssertsFromBaseTypes(fBaseType);
        }
        
        // -----------------------------------------------------------------------
        // Process a RESTRICTION
        // -----------------------------------------------------------------------
        if (fDerivedBy == XSConstants.DERIVATION_RESTRICTION) {
            
            // -----------------------------------------------------------------------
            // There may be a simple type definition in the restriction element
            // The data type validator will be based on it, if specified
            // -----------------------------------------------------------------------
            boolean needToSetContext = false;
            if (simpleContent !=null &&
                    DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_SIMPLETYPE )) {
                
                XSSimpleType dv = fSchemaHandler.fSimpleTypeTraverser.traverseLocal(
                        simpleContent, schemaDoc, grammar, null);
                if (dv == null) {
                    fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError();
                }
                //check that this datatype validator is validly derived from the base
                //according to derivation-ok-restriction 5.1.2.1
                
                if (baseValidator != null &&
                        !fSchemaHandler.fXSConstraints.checkSimpleDerivationOk(dv, baseValidator,
                                baseValidator.getFinal())) {
                    fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.2.2.1",
                            new Object[]{fName, dv.getName(), baseValidator.getName()},
                            simpleContent);
                }
                // XML Schema 1.1 - need to set context of dv
                if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                   if (dv instanceof XSSimpleTypeDecl) {
                       needToSetContext = true;
                   }
                }
                baseValidator = dv;
                simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
            }
            // anySimpleType or anyAtomicType are not allowed as base type
            else if (baseValidator == SchemaGrammar.fAnySimpleType || baseValidator == SchemaGrammar.fAnyAtomicType) {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("cos-st-restricts.1.1",
                        new Object[]{baseValidator.getName(), genAnonTypeName(simpleContentElement)}, simpleContentElement);
            } 
            
            // this only happens when restricting a mixed/emptiable CT
            // but there is no <simpleType>, which is required
            if (baseValidator == null) {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("src-ct.2.2",
                        new Object[]{fName}, simpleContent);
            }
            
            // -----------------------------------------------------------------------
            // Traverse any facets
            // -----------------------------------------------------------------------
            Element attrOrAssertNode = null;
            XSFacets facetData = null;
            short presentFacets = 0 ;
            short fixedFacets = 0 ;
            
            if (simpleContent!=null) {
                FacetInfo fi = traverseFacets(simpleContent, fComplexTypeDecl, baseValidator, schemaDoc);
                attrOrAssertNode = fi.nodeAfterFacets;
                facetData = fi.facetdata;
                presentFacets = fi.fPresentFacets;
                fixedFacets = fi.fFixedFacets;
            }
            
            String name = genAnonTypeName(simpleContentElement);
            fXSSimpleType = fSchemaHandler.fDVFactory.createTypeRestriction(name,schemaDoc.fTargetNamespace,(short)0,baseValidator,null);
            try{
                fValidationState.setNamespaceSupport(schemaDoc.fNamespaceSupport);
                fXSSimpleType.applyFacets(facetData, presentFacets, fixedFacets, fValidationState);
            }catch(InvalidDatatypeFacetException ex){
                reportSchemaError(ex.getKey(), ex.getArgs(), simpleContent);
                // Recreate the type, ignoring the facets
                fXSSimpleType = fSchemaHandler.fDVFactory.createTypeRestriction(name,schemaDoc.fTargetNamespace,(short)0,baseValidator,null);
            }
            if (fXSSimpleType instanceof XSSimpleTypeDecl) {
                ((XSSimpleTypeDecl)fXSSimpleType).setAnonymous(true);
            }
            
            // set context of local simple type (baseValidator)
            if (needToSetContext) {
                ((XSSimpleTypeDecl)baseValidator).setContext(fXSSimpleType);
            }
            
            // -----------------------------------------------------------------------
            // Traverse any attributes/asserts
            // -----------------------------------------------------------------------
            if (attrOrAssertNode != null) {
                if (isAttrOrAttrGroup(attrOrAssertNode)) {
                    Element node=traverseAttrsAndAttrGrps(attrOrAssertNode,fAttrGrp,
                            schemaDoc,grammar,fComplexTypeDecl);
                    if (node != null) {
                        if (isAssert(node)) {
                            traverseAsserts(node, schemaDoc, grammar,
                                    fComplexTypeDecl);
                        } else {
                            // either XML Schema 1.0 or a non assert element
                            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                            throw new ComplexTypeRecoverableError(
                                    "s4s-elt-invalid-content.1",
                                    new Object[] { fName,
                                            DOMUtil.getLocalName(node) }, node);
                        }
                    }
                } else if (isAssert(attrOrAssertNode)) {
                    traverseAsserts(attrOrAssertNode, schemaDoc, grammar,
                            fComplexTypeDecl);
                } else  {
                    fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                            new Object[]{fName,DOMUtil.getLocalName(attrOrAssertNode)},
                            attrOrAssertNode);
                }
            }
            
            try {
                mergeAttributes(baseComplexType.getAttrGrp(), fAttrGrp, fName, false, simpleContentElement);
            } catch (ComplexTypeRecoverableError e) {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw e;
            }
            // Prohibited uses must be removed after merge for RESTRICTION
            fAttrGrp.removeProhibitedAttrs();
            
            Object[] errArgs=fAttrGrp.validRestrictionOf(fName, baseComplexType.getAttrGrp(), fSchemaHandler.fXSConstraints);
            if (errArgs != null) {
                fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError((String)errArgs[errArgs.length-1],
                        errArgs, attrOrAssertNode);
            }
            
        }
        // -----------------------------------------------------------------------
        // Process a EXTENSION
        // -----------------------------------------------------------------------
        else {
            fXSSimpleType = baseValidator;
            if (simpleContent != null) {
                // -----------------------------------------------------------------------
                // Traverse any attributes/asserts
                // -----------------------------------------------------------------------
                Element attrOrAssertNode = simpleContent;
                if (isAttrOrAttrGroup(attrOrAssertNode)) {
                    Element node = traverseAttrsAndAttrGrps(attrOrAssertNode,
                            fAttrGrp, schemaDoc, grammar, fComplexTypeDecl);

                    if (node != null) {
                        if (isAssert(node)) {
                            traverseAsserts(node, schemaDoc, grammar,
                                            fComplexTypeDecl);
                        } else {
                            // a non assert element after attributes is an error
                            fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                            throw new ComplexTypeRecoverableError(
                                    "s4s-elt-invalid-content.1",
                                    new Object[] { fName,
                                            DOMUtil.getLocalName(node) }, node);
                        }
                    }
                }
                else if (isAssert(attrOrAssertNode)) {
                    traverseAsserts(attrOrAssertNode, schemaDoc, grammar,
                            fComplexTypeDecl);
                }
                else {
                    fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                            new Object[]{fName,DOMUtil.getLocalName(attrOrAssertNode)},
                            attrOrAssertNode);
                }
                // Remove prohibited uses.   Should be done prior to any merge.
                fAttrGrp.removeProhibitedAttrs();
            }
            
            if (baseComplexType != null) {
                try {
                    mergeAttributes(baseComplexType.getAttrGrp(), fAttrGrp, fName, true, simpleContentElement);
                } catch (ComplexTypeRecoverableError e) {
                    fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw e;
                }
            }
        }
        // and finally, since we've nothing more to traverse, we can
        // return the attributes (and thereby reset the namespace support)
        fAttrChecker.returnAttrArray(simpleContentAttrValues, schemaDoc);
        fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
    }
    
    private void traverseComplexContent(Element complexContentElement,
            boolean mixedOnType, XSDocumentInfo schemaDoc,
            SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {
        
        Object[] complexContentAttrValues = fAttrChecker.checkAttributes(complexContentElement, false,
                schemaDoc);

        // -----------------------------------------------------------------------
        // Determine if this is mixed content
        // -----------------------------------------------------------------------
        boolean mixedContent = mixedOnType;
        Boolean mixedAtt     = (Boolean) complexContentAttrValues[XSAttributeChecker.ATTIDX_MIXED];
        if (mixedAtt != null) {
            mixedContent = mixedAtt.booleanValue();
        }        

        // -----------------------------------------------------------------------
        // Since the type must have complex content, set the simple type validators
        // to null
        // -----------------------------------------------------------------------
        fXSSimpleType = null;

        Element complexContent = DOMUtil.getFirstChildElement(complexContentElement);
        if (complexContent != null && DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
            addAnnotation(traverseAnnotationDecl(complexContent, complexContentAttrValues, false, schemaDoc));
            complexContent = DOMUtil.getNextSiblingElement(complexContent);
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(complexContentElement);
            if (text != null) {
                addAnnotation(traverseSyntheticAnnotation(complexContentElement, text, complexContentAttrValues, false, schemaDoc));
            }
        }

        // If there are no children, return
        if (complexContent==null) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.2",
                    new Object[]{fName,SchemaSymbols.ELT_COMPLEXCONTENT},
                    complexContentElement);
        }

        // -----------------------------------------------------------------------
        // The content should be either "restriction" or "extension"
        // -----------------------------------------------------------------------
        String complexContentName = DOMUtil.getLocalName(complexContent);
        if (complexContentName.equals(SchemaSymbols.ELT_RESTRICTION))
            fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
        else if (complexContentName.equals(SchemaSymbols.ELT_EXTENSION)) {
            fDerivedBy = XSConstants.DERIVATION_EXTENSION;
            // xsd 1.1 - the derivedBy field is set on the complex type declaration
            // for later checking for targetNamespace constraints
            if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                fComplexTypeDecl.setDerivationMethod(XSConstants.DERIVATION_EXTENSION);
            }
        }
        else {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                    new Object[]{fName, complexContentName}, complexContent);
        }
        Element elemTmp = DOMUtil.getNextSiblingElement(complexContent);
        if (elemTmp != null) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            String siblingName = DOMUtil.getLocalName(elemTmp);
            throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                    new Object[]{fName, siblingName}, elemTmp);
        }

        Object[] derivationTypeAttrValues = fAttrChecker.checkAttributes(complexContent, false,
                schemaDoc);
        QName baseTypeName = (QName)  derivationTypeAttrValues[XSAttributeChecker.ATTIDX_BASE];

        // -----------------------------------------------------------------------
        // Need a base type.  Check that it's a complex type
        // -----------------------------------------------------------------------
        if (baseTypeName==null) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("s4s-att-must-appear",
                    new Object[]{complexContentName, "base"}, complexContent);
        }

        XSTypeDefinition type = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc,
                XSDHandler.TYPEDECL_TYPE,
                baseTypeName,
                complexContent);

        if (type==null) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError();
        }

        if (! (type instanceof XSComplexTypeDecl)) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw new ComplexTypeRecoverableError("src-ct.1",
                    new Object[]{fName, type.getName()}, complexContent);
        }
        XSComplexTypeDecl baseType = (XSComplexTypeDecl)type;
        fBaseType = baseType;
        
        // xsd 1.1 - the baseType field is set on the complex type declaration
        // for later checking for targetNamespace constraints
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            fComplexTypeDecl.setBaseType(fBaseType);
        }

        // -----------------------------------------------------------------------
        // Check that the base permits the derivation
        // -----------------------------------------------------------------------
        if ((baseType.getFinal() & fDerivedBy)!=0) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            String errorKey = (fDerivedBy==XSConstants.DERIVATION_EXTENSION) ?
                    "cos-ct-extends.1.1" : "derivation-ok-restriction.1";
            throw new ComplexTypeRecoverableError(errorKey,
                    new Object[]{fName, fBaseType.getName()}, complexContent);
        }

        // -----------------------------------------------------------------------
        // Skip over any potential annotations
        // -----------------------------------------------------------------------
        complexContent = DOMUtil.getFirstChildElement(complexContent);

        if (complexContent != null) {
            // traverse annotation if any
            if (DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                addAnnotation(traverseAnnotationDecl(complexContent, derivationTypeAttrValues, false, schemaDoc));
                complexContent = DOMUtil.getNextSiblingElement(complexContent);
            }
            else {
                String text = DOMUtil.getSyntheticAnnotation(complexContent);
                if (text != null) {
                    addAnnotation(traverseSyntheticAnnotation(complexContent, text, derivationTypeAttrValues, false, schemaDoc));
                }
            }
            if (complexContent !=null &&
                    DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)){
                fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                        new Object[]{fName,SchemaSymbols.ELT_ANNOTATION}, complexContent);
            }
        }
        else {
            String text = DOMUtil.getSyntheticAnnotation(complexContent);
            if (text != null) {
                addAnnotation(traverseSyntheticAnnotation(complexContent, text, derivationTypeAttrValues, false, schemaDoc));
            }
        }
                
        // add any assertions from the base types, for assertions to be processed
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            addAssertsFromBaseTypes(fBaseType);
        }
        
        // -----------------------------------------------------------------------
        // Process the content.  Note:  should I try to catch any complexType errors
        // here in order to return the attr array?
        // -----------------------------------------------------------------------
        try {
            processComplexContent(complexContent, mixedContent, true, schemaDoc,
                    grammar);
        } catch (ComplexTypeRecoverableError e) {
            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
            throw e;
        }

        // -----------------------------------------------------------------------
        // Compose the final content and attribute uses
        // -----------------------------------------------------------------------
        XSParticleDecl baseContent = (XSParticleDecl)baseType.getParticle();
        // XML Schema 1.1
        XSOpenContentDecl explicitOpenContent = null;

        if (fDerivedBy==XSConstants.DERIVATION_RESTRICTION) {

            // This is an RESTRICTION

            // N.B. derivation-ok-restriction.5.3 is checked under schema
            // full checking.   That's because we need to wait until locals are
            // traversed so that occurrence information is correct.
            if (fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED &&
                    baseType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.4.1.2",
                        new Object[]{fName, baseType.getName()},
                        complexContent);
            }

            try {
                mergeAttributes(baseType.getAttrGrp(), fAttrGrp, fName, false, complexContent);
            } catch (ComplexTypeRecoverableError e) {
                fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw e;
            }
            // Remove prohibited uses.   Must be done after merge for RESTRICTION.
            fAttrGrp.removeProhibitedAttrs();

            if (baseType != SchemaGrammar.getXSAnyType(fSchemaHandler.fSchemaVersion)) {
                Object[] errArgs = fAttrGrp.validRestrictionOf(fName, baseType.getAttrGrp(), fSchemaHandler.fXSConstraints);
                if (errArgs != null) {
                    fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError((String)errArgs[errArgs.length-1],
                            errArgs, complexContent);
                }
            }
        }
        else {
            
            // This is an EXTENSION

            // Create the particle
            if (fParticle == null) {
                fContentType = baseType.getContentType();
                fXSSimpleType = (XSSimpleType)baseType.getSimpleType();
                fParticle = baseContent;
                if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                    explicitOpenContent = (XSOpenContentDecl) baseType.getOpenContent();
                }
            }
            else if (baseType.getContentType() == XSComplexTypeDecl.CONTENTTYPE_EMPTY) {
            }
            else {
                //
                // Check if the contentType of the base is consistent with the new type
                // cos-ct-extends.1.4.3.2
                if (fContentType == XSComplexTypeDecl.CONTENTTYPE_ELEMENT &&
                        baseType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_ELEMENT) {
                    fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.1.a",
                            new Object[]{fName}, complexContent);
                }
                else if (fContentType == XSComplexTypeDecl.CONTENTTYPE_MIXED &&
                        baseType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_MIXED) {
                    fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                    fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.1.b",
                            new Object[]{fName}, complexContent);
                }

                // if the content of either type is an "all" model group, error.
                boolean baseIsAll = (((XSParticleDecl)baseType.getParticle()).fType == XSParticleDecl.PARTICLE_MODELGROUP
                                    && ((XSModelGroupImpl)(((XSParticleDecl)baseType.getParticle())).fValue).fCompositor == XSModelGroupImpl.MODELGROUP_ALL);
                boolean derivedIsAll = (fParticle.fType == XSParticleDecl.PARTICLE_MODELGROUP
                                       && ((XSModelGroupImpl)fParticle.fValue).fCompositor == XSModelGroupImpl.MODELGROUP_ALL);

                if (baseIsAll || derivedIsAll) {
                    // XML Schema 1.1
                    //
                    // 4.2.3.2 If the {term} of the base particle has {compositor} all
                    //   and the {term} of the effective content also has {compositor}
                    //   all, then a Particle whose properties are as follows:
                    // {min occurs}
                    //      the {min occurs} of the effective content.
                    // {max occurs}
                    //      1
                    // {term}
                    //      a model group whose {compositor} is all and whose {particles}
                    //        are the {particles} of the {term} of the base particle followed
                    //        by the {particles} of the {term} of the effective content.
                    if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1 && baseIsAll && derivedIsAll) {
                        // Schema Component Constraint: Particle Valid (Extension)
                        //   3.1 E's {min occurs} is the same as B's {min occurs}.
                        if (fParticle.fMinOccurs != baseContent.fMinOccurs) {
                            throw new ComplexTypeRecoverableError("cos-particle-extends.3.1",
                                    new Object[]{}, complexContent);
                        }

                        XSModelGroupImpl group = new XSModelGroupImpl();
                        group.fCompositor = XSModelGroupImpl.MODELGROUP_ALL;
                        group.fParticleCount = ((XSModelGroupImpl)baseContent.fValue).fParticleCount + ((XSModelGroupImpl)fParticle.fValue).fParticleCount;
                        group.fParticles = new XSParticleDecl[group.fParticleCount];
                        System.arraycopy(((XSModelGroupImpl)baseContent.fValue).fParticles, 0, group.fParticles, 0, ((XSModelGroupImpl)baseContent.fValue).fParticleCount);
                        System.arraycopy(((XSModelGroupImpl)fParticle.fValue).fParticles, 0, group.fParticles, ((XSModelGroupImpl)baseContent.fValue).fParticleCount, ((XSModelGroupImpl)fParticle.fValue).fParticleCount);
                        group.fAnnotations = XSObjectListImpl.EMPTY_LIST;
                        // the particle to contain the above all
                        XSParticleDecl particle = new XSParticleDecl();
                        particle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
                        particle.fValue = group;
                        particle.fAnnotations = XSObjectListImpl.EMPTY_LIST;
                        particle.fMinOccurs = fParticle.fMinOccurs;

                        fParticle = particle;
                        explicitOpenContent = (XSOpenContentDecl) baseType.getOpenContent();
                    }
                    else {
                        fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                        fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                        throw new ComplexTypeRecoverableError("cos-all-limited.1.2",
                                new Object[]{}, complexContent);
                    }
                }
                // the "sequence" model group to contain both particles
                else {
                    XSModelGroupImpl group = new XSModelGroupImpl();
                    group.fCompositor = XSModelGroupImpl.MODELGROUP_SEQUENCE;
                    group.fParticleCount = 2;
                    group.fParticles = new XSParticleDecl[2];
                    group.fParticles[0] = (XSParticleDecl)baseType.getParticle();
                    group.fParticles[1] = fParticle;
                    group.fAnnotations = XSObjectListImpl.EMPTY_LIST;
                    // the particle to contain the above sequence
                    XSParticleDecl particle = new XSParticleDecl();
                    particle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
                    particle.fValue = group;
                    particle.fAnnotations = XSObjectListImpl.EMPTY_LIST; 
                
                    fParticle = particle;
                    if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
                        explicitOpenContent = (XSOpenContentDecl) baseType.getOpenContent();
                    }
                }
            }

            // Remove prohibited uses.   Must be done before merge for EXTENSION.
            fAttrGrp.removeProhibitedAttrs();
            try {
                mergeAttributes(baseType.getAttrGrp(), fAttrGrp, fName, true, complexContent);
            } catch (ComplexTypeRecoverableError e) {
                fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                throw e;
            }
        }

        //------------------------------------------------------------------------
        // XML Schema 1.1
        // Applies open content, if present
        //------------------------------------------------------------------------
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1) {
            XSOpenContentDecl baseOpenContent = (XSOpenContentDecl) baseType.getOpenContent();

            // Let the wildcard element be  the appropriate case among the following:
            //    5.1 If the <openContent> [child] is present , then the <openContent> [child].
            //    5.2 If the <openContent> [child] is not present, the <schema> ancestor has an <defaultOpenContent> [child], and one of the following is true
            //      5.2.1 the {variety} of the explicit content type is not empty
            //      5.2.2 the {variety} of the explicit content type is empty and the actual value of the appliesToEmpty [attribute] is true
            //      , then the <defaultOpenContent> [child] of the <schema>.
            //    5.3 otherwise absent.
            if (fOpenContent == null) {
                if (schemaDoc.fDefaultOpenContent != null) {
                    if (fContentType != XSComplexTypeDecl.CONTENTTYPE_EMPTY || schemaDoc.fDefaultOpenContent.fAppliesToEmpty) {
                        fOpenContent = schemaDoc.fDefaultOpenContent;
                    }
                }
            }

            // If the wildcard element is not absent
            if (fOpenContent != null) {
                // 6.2 If the actual value of its mode [attribute] is 'none', then an absent {open content}
                if (fOpenContent.fMode == XSOpenContentDecl.MODE_NONE) {
                    fOpenContent = null;
                }
                // 6.3 If the {variety} is empty, then a Particle as follows:
                //   {min occurs} 1
                //   {max occurs} 1
                //   {term}       a model group whose {compositor} is sequence and whose {particles} is empty.
                else if (fContentType == XSComplexTypeDecl.CONTENTTYPE_EMPTY) {
                    fParticle = XSConstraints.getEmptySequence();
                    fContentType = XSComplexTypeDecl.CONTENTTYPE_ELEMENT;
                }
            }
            // 6.1 If the wildcard element is absent, then the explicit open content.
            else {
                fOpenContent = explicitOpenContent;
            }

            if (fDerivedBy == XSConstants.DERIVATION_EXTENSION && baseType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_EMPTY) {

                // 1.4.3.2.2.3  One or more of the following is true:
                //    1.4.3.2.2.3.1 B.{content type}.{open content} (call it BOT) is absent.
                //    1.4.3.2.2.3.2 T.{content type}.{open content} (call it EOT) has {mode} interleave.
                //    1.4.3.2.2.3.3 Both BOT and EOT have {mode} suffix.
                //    1.4.3.2.2.4 If neither BOT nor EOT is absent, then BOT.{wildcard}.{namespace constraint} is a subset
                //                of EOT.{wildcard}.{namespace constraint}, as defined by Wildcard Subset (3.10.6.2).

                if (baseOpenContent != null && fOpenContent != baseOpenContent) {
                    // {open content} had a mode of 'none'
                    if (fOpenContent == null) {
                        fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                        fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                        throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.3",
                                new Object[]{fName}, complexContent);
                    }
                    else {
                        // 1.4.3.2.2.3.2
                        // 1.4.3.2.2.3.3
                        if (fOpenContent.fMode == XSOpenContentDecl.MODE_SUFFIX) {
                            if (baseOpenContent.fMode != XSOpenContentDecl.MODE_SUFFIX) {
                                fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                                fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                                throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.3.3",
                                        new Object[]{fName}, complexContent);
                            }
                        }

                        // 1.4.3.2.2.4
                        if (!fSchemaHandler.fXSConstraints.isSubsetOf(baseOpenContent.fWildcard, fOpenContent.fWildcard)) {
                            fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
                            fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
                            throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.3.2.2.3.4",
                                    new Object[]{fName}, complexContent);
                        }
                    }
                }
            }
        } // end of fSchema11Support

        // and *finally* we can legitimately return the attributes!
        fAttrChecker.returnAttrArray(complexContentAttrValues, schemaDoc);
        fAttrChecker.returnAttrArray(derivationTypeAttrValues, schemaDoc);
        
    } // end of traverseComplexContent
    
    // This method merges attribute uses from the base, into the derived set.
    // The first duplicate attribute, if any, is returned.
    // LM: may want to merge with attributeGroup processing.
    private void mergeAttributes(XSAttributeGroupDecl fromAttrGrp,
            XSAttributeGroupDecl toAttrGrp,
            String typeName,
            boolean extension,
            Element elem)
    throws ComplexTypeRecoverableError {
        
        XSObjectList attrUseS = fromAttrGrp.getAttributeUses();
        XSAttributeUseImpl  oneAttrUse = null;
        int attrCount = attrUseS.getLength();
        for (int i=0; i<attrCount; i++) {
            oneAttrUse = (XSAttributeUseImpl)attrUseS.item(i);
            XSAttributeUse existingAttrUse = toAttrGrp.getAttributeUse(oneAttrUse.fAttrDecl.getNamespace(),
                    oneAttrUse.fAttrDecl.getName());
            if (existingAttrUse == null) {
                
                String idName = toAttrGrp.addAttributeUse(oneAttrUse, fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1);
                if (idName != null) {
                    throw new ComplexTypeRecoverableError("ct-props-correct.5",
                            new Object[]{typeName, idName, oneAttrUse.fAttrDecl.getName()},
                            elem);
                }
            }
            else if (existingAttrUse != oneAttrUse) {
                if (extension) {
                    reportSchemaError("ct-props-correct.4",
                            new Object[]{typeName, oneAttrUse.fAttrDecl.getName()},
                            elem);
                    // Recover by using the attribute use from the base type,
                    // to make the resulting schema "more valid".
                    toAttrGrp.replaceAttributeUse(existingAttrUse, oneAttrUse);
                }
            }
        }
        // For extension, the wildcard must be formed by doing a union of the wildcards
        if (extension) {
            if (toAttrGrp.fAttributeWC==null) {
                toAttrGrp.fAttributeWC = fromAttrGrp.fAttributeWC;
            }
            else if (fromAttrGrp.fAttributeWC != null) {
                toAttrGrp.fAttributeWC = fSchemaHandler.fXSConstraints.performUnionWith(toAttrGrp.fAttributeWC, fromAttrGrp.fAttributeWC, toAttrGrp.fAttributeWC.fProcessContents);
                if (toAttrGrp.fAttributeWC == null) {
                    // REVISIT: XML Schema 1.0 2nd edition doesn't actually specify this constraint. It's a bug in the spec
                    // which will eventually be fixed. We're just guessing what the error code will be. If it turns out to be
                    // something else we'll need to change it. -- mrglavas
                    throw new ComplexTypeRecoverableError("src-ct.5", new Object[]{typeName}, elem);
                }
            }
        }
    }
    

    /*
     * Find all assertions up in schema type hierarchy, and add them to the list
     * of assertions to be processed.
     */
    private void addAssertsFromBaseTypes(XSTypeDefinition baseSchemaType) {
        
        if (baseSchemaType != null) {
            if (baseSchemaType instanceof XSComplexTypeDefinition) {
               // if schema type is a 'complex type'
               XSObjectList assertList = ((XSComplexTypeDefinition) baseSchemaType)
                                                 .getAssertions();
               for (int assertLstIdx = 0; assertLstIdx < assertList.size(); 
                                                                 assertLstIdx++) {
                  // add assertion to the list, only if it's already not present
                  if (!assertExists((XSAssertImpl) assertList.get(assertLstIdx))) {
                     addAssertion((XSAssertImpl) assertList.get(assertLstIdx));
                  }
               }
            }
            else if (baseSchemaType instanceof XSSimpleTypeDefinition) {
                // if schema type is a 'simple type'
                XSObjectList facets = ((XSSimpleTypeDefinition) baseSchemaType).
                                                             getMultiValueFacets();
                for (int facetIdx = 0; facetIdx < facets.getLength(); facetIdx++) {
                    XSMultiValueFacet facet = (XSMultiValueFacet) facets.
                                                                     item(facetIdx);
                    if (facet.getFacetKind() == XSSimpleTypeDefinition.FACET_ASSERT) {
                        Vector assertionFacets = facet.getAsserts();
                        for (int j = 0; j < assertionFacets.size(); j++) {
                           XSAssertImpl assertImpl = (XSAssertImpl) 
                                                            assertionFacets.get(j);
                           addAssertion(assertImpl);
                        }
                       
                        // among the facet list, there could be only one
                        // assertion facet (which is multi-valued). break from
                        // the loop.
                        break;
                       
                    }
                }
            }
            
            // invoke the method recursively (traverse up the type hierarchy)
            XSTypeDefinition ancestorType = baseSchemaType.getBaseType();
            if (ancestorType != null && !(ancestorType.getName().equals(
                "anyType") || ancestorType.derivedFrom(Constants.NS_XMLSCHEMA,
                "anyAtomicType", XSConstants.DERIVATION_RESTRICTION))) {              
               addAssertsFromBaseTypes(ancestorType);
            }
        }
        
    } // addAssertsFromBaseTypes
    
    
    /*
     * Check if an assertion already exists in the buffer.
     */
    private boolean assertExists(XSAssertImpl assertVal) {
        
      boolean assertExists = false;      
      
      if (fAssertions != null) {
        for (int i = 0; i < fAssertions.length; i++) {
          if (fAssertions[i].equals(assertVal)) {
              assertExists = true;
              break;
          }
        } 
      }
      
      return assertExists;
      
    } // assertExists
    

    private void processComplexContent(Element complexContentChild,
            boolean isMixed, boolean isDerivation,
            XSDocumentInfo schemaDoc, SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {
        
        Element attrOrAssertNode = null;
        XSParticleDecl particle = null;
        
        // whether there is a particle with empty model group
        boolean emptyParticle = false;
        String childName = (complexContentChild != null) ? DOMUtil.getLocalName(complexContentChild) : null;

        // -------------------------------------------------------------
        // OPENCONTENT?, followed by GROUP, ALL, SEQUENCE or CHOICE, 
        //   followed by attributes, if specified, followed by
        //   assertions if specified.
        // Note that it's possible that only attributes are specified.
        // -------------------------------------------------------------

        // XML Schema 1.1
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1 && childName != null &&
                childName.equals(SchemaSymbols.ELT_OPENCONTENT)) {
            fOpenContent = traverseOpenContent(complexContentChild, schemaDoc, grammar, false);
            complexContentChild = DOMUtil.getNextSiblingElement(complexContentChild);
            childName = (complexContentChild != null) ? DOMUtil.getLocalName(complexContentChild) : null;
        }

        if (childName != null) {

            if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                
                particle = fSchemaHandler.fGroupTraverser.traverseLocal(complexContentChild,
                        schemaDoc, grammar);
                attrOrAssertNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particle = traverseSequence(complexContentChild,schemaDoc,grammar,
                        NOT_ALL_CONTEXT,fComplexTypeDecl);
                if (particle != null) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        emptyParticle = true;
                }
                attrOrAssertNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particle = traverseChoice(complexContentChild,schemaDoc,grammar,
                        NOT_ALL_CONTEXT,fComplexTypeDecl);
                if (particle != null && particle.fMinOccurs == 0) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        emptyParticle = true;
                }
                attrOrAssertNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                particle = traverseAll(complexContentChild,schemaDoc,grammar,
                        PROCESSING_ALL_GP,fComplexTypeDecl);
                if (particle != null) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        emptyParticle = true;
                }
                attrOrAssertNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else {
                // Should be attributes here - will check below...
                attrOrAssertNode = complexContentChild;
            }
        }
        
        // Explicit content
        //
        // if the particle is empty because there is no non-annotation chidren,
        // we need to make the particle itself null (so that the effective
        // content is empty).
        if (emptyParticle) {
            // get the first child
            Element child = DOMUtil.getFirstChildElement(complexContentChild);
            // if it's annotation, get the next one
            if (child != null) {
                if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    child = DOMUtil.getNextSiblingElement(child);
                }
            }
            // if there is no (non-annotation) children, mark particle empty
            if (child == null)
                particle = null;
            // child != null means we might have seen an element with
            // minOccurs == maxOccurs == 0
        }

        // XML Schema 1.1
        //
        // When dealing with a complex type that does not have a complexContent
        // or a simpleContent children, handle the wildcard element rules (open content)
        if (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1 && !isDerivation) {
            // Let the wildcard element be  the appropriate case among the following:
            //    5.1 If the <openContent> [child] is present , then the <openContent> [child].
            //    5.2 If the <openContent> [child] is not present, the <schema> ancestor has an <defaultOpenContent> [child], and one of the following is true
            //      5.2.1 the {variety} of the explicit content type is not empty
            //      5.2.2 the {variety} of the explicit content type is empty and the actual value of the appliesToEmpty [attribute] is true
            //            , then the <defaultOpenContent> [child] of the <schema>.
            //    5.3 otherwise absent.
            if (fOpenContent == null) {
                if (schemaDoc.fDefaultOpenContent != null) {
                    if (particle != null || schemaDoc.fDefaultOpenContent.fAppliesToEmpty) {
                        fOpenContent = schemaDoc.fDefaultOpenContent;
                    }
                }
            }
            // 6.2 If the actual value of its mode [attribute] is 'none', then an absent {open content}
            else if (fOpenContent.fMode == XSOpenContentDecl.MODE_NONE) {
                fOpenContent = null;
            }
        }

        // Effective content
        //
        // 3.1 If the explicit content is empty then the appropriate case among the following:
        //     3.1.1 If the effective mixed is true, then A particle whose properties are as follows:
        //           {min occurs} 1
        //           {max occurs} 1
        //           {term}       a model group whose {compositor} is sequence and whose {particles} is empty
        //     3.1.2 otherwise empty
        // 3.2 otherwise the explicit content
        //
        // XML Schema 1.1
        //
        // Dealing with a complex type that has no complexContent/simpleContent child
        //
        // 6.3 Particle of Content type - based on wildcard element
        //    
        if (particle == null && (isMixed || (!isDerivation && fOpenContent != null))) {
            particle = XSConstraints.getEmptySequence();
        }
        fParticle = particle;

        // -----------------------------------------------------------------------
        // Set the content type
        // -----------------------------------------------------------------------
        if (fParticle == null)
            fContentType = XSComplexTypeDecl.CONTENTTYPE_EMPTY;
        else if (isMixed)
            fContentType = XSComplexTypeDecl.CONTENTTYPE_MIXED;
        else
            fContentType = XSComplexTypeDecl.CONTENTTYPE_ELEMENT;
        
        
        // -------------------------------------------------------------
        // Now, process attributes
        // -------------------------------------------------------------
        if (attrOrAssertNode != null) {
            if (isAttrOrAttrGroup(attrOrAssertNode)) {
                Element node = traverseAttrsAndAttrGrps(attrOrAssertNode,
                        fAttrGrp, schemaDoc, grammar, fComplexTypeDecl);

                // Only remove prohibited attribute uses if this isn't a derived type
                // Derivation-specific code worries about this elsewhere
                if (!isDerivation) {
                    fAttrGrp.removeProhibitedAttrs();
                }

                if (node != null) {
                    if (isAssert(node)) {
                        traverseAsserts(node, schemaDoc, grammar,
                                fComplexTypeDecl);
                    } else {
                        // a non assert element after attributes is an error
                        throw new ComplexTypeRecoverableError(
                                "s4s-elt-invalid-content.1", new Object[] {
                                        fName, DOMUtil.getLocalName(node) },
                                node);
                    }
                }
            }
            else if (isAssert(attrOrAssertNode)) {
                traverseAsserts(attrOrAssertNode, schemaDoc, grammar,
                        fComplexTypeDecl);
            }
            else {
                throw new ComplexTypeRecoverableError("s4s-elt-invalid-content.1",
                        new Object[]{fName,DOMUtil.getLocalName(attrOrAssertNode)},
                        attrOrAssertNode);
            }
        }
    } // end processComplexContent

    private boolean isAttrOrAttrGroup(Element e) {
        String elementName = DOMUtil.getLocalName(e);
        
        if (elementName.equals(SchemaSymbols.ELT_ATTRIBUTE) ||
                elementName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP) ||
                elementName.equals(SchemaSymbols.ELT_ANYATTRIBUTE))
            return true;
        else
            return false;
    }

    /*
     * Check if a schema instruction is an 'assert' instruction.
     */
    private boolean isAssert(Element e) {
        
        return (fSchemaHandler.fSchemaVersion == Constants.SCHEMA_VERSION_1_1
                && DOMUtil.getLocalName(e).equals(SchemaSymbols.ELT_ASSERT));
        
    }

    /*
     * Traversal support for XML Schema 1.1, 'assertions'.
     */
    private void traverseAsserts(Element assertElement, XSDocumentInfo schemaDoc,
                                 SchemaGrammar grammar, XSComplexTypeDecl 
                                 enclosingCT) throws ComplexTypeRecoverableError {

        Object[] attrValues = fAttrChecker.checkAttributes(assertElement,
                                                           false, schemaDoc);
        String test = (String) attrValues[XSAttributeChecker.ATTIDX_XPATH];
        String xpathDefaultNamespace = (String) attrValues[XSAttributeChecker.
                                                           ATTIDX_XPATHDEFAULTNS];
        if (xpathDefaultNamespace == null) {
           xpathDefaultNamespace = schemaDoc.fXpathDefaultNamespace;    
        }
        
        if (test != null) {
            // get 'annotation'
            Element childNode = DOMUtil.getFirstChildElement(assertElement);
            XSAnnotationImpl annotation = null;

            // first child could be an annotation
            if (childNode != null
                    && DOMUtil.getLocalName(childNode).equals(
                            SchemaSymbols.ELT_ANNOTATION)) {
                annotation = traverseAnnotationDecl(childNode, attrValues,
                        false, schemaDoc);
                // now move on to the next child element
                childNode = DOMUtil.getNextSiblingElement(childNode);

                if (childNode != null) {
                    // it's an error to have something after the annotation, in 'assert'
                    reportSchemaError("s4s-elt-invalid-content.1", new Object[] {
                            DOMUtil.getLocalName(assertElement),
                            DOMUtil.getLocalName(childNode) }, childNode);
                }
            } else {
                String text = DOMUtil.getSyntheticAnnotation(childNode);
                if (text != null) {
                    annotation = traverseSyntheticAnnotation(childNode, text,
                                        attrValues, false, schemaDoc);
                }
            }

            XSObjectList annotations = null;
            if (annotation != null) {
                annotations = new XSObjectListImpl();
                ((XSObjectListImpl) annotations).addXSObject(annotation);
            } else {
                // if no annotations are present assign an empty list, for
                // annotations.
                annotations = XSObjectListImpl.EMPTY_LIST;
            }
            
            // create an assertion object            
            XSAssertImpl assertImpl = new XSAssertImpl(enclosingCT,
                                                       annotations,
                                                       fSchemaHandler);
            Test testExpr = new Test(new XPath20Assert(test, fSymbolTable,
                                     new SchemaNamespaceSupport(schemaDoc.
                                     fNamespaceSupport)), assertImpl);
            assertImpl.setTest(testExpr);
            assertImpl.setXPathDefaultNamespace(xpathDefaultNamespace);
            assertImpl.setXPath2NamespaceContext(new SchemaNamespaceSupport
                                            (schemaDoc.fNamespaceSupport));
            String assertMessage = XMLChar.trim(assertElement.getAttributeNS(
                                          SchemaSymbols.URI_XERCES_EXTENSIONS, 
                                          SchemaSymbols.ATT_ASSERT_MESSAGE));
            if (!"".equals(assertMessage)) {
               assertImpl.setMessage(assertMessage);
            }

            // add assertion object, to the list of assertions to be processed
            addAssertion(assertImpl);

            Element sibling = DOMUtil.getNextSiblingElement(assertElement);
            // if there is sibling element
            if (sibling != null) {
                if (sibling.getLocalName().equals(SchemaSymbols.ELT_ASSERT)) {
                    // traverse sibling assertion elements recursively, till
                    // none is found
                    traverseAsserts(sibling, schemaDoc, grammar, enclosingCT);
                } else {
                    // a non-assert element after assert is an error
                    fAttrChecker.returnAttrArray(attrValues, schemaDoc);
                    throw new ComplexTypeRecoverableError(
                            "s4s-elt-invalid-content.1", new Object[] { fName,
                                    DOMUtil.getLocalName(sibling) }, sibling);
                }
            }
        } else {
            // 'test' attribute is mandatory in an assert element
            reportSchemaError("src-assert.3.13.1", new Object[] { DOMUtil
                    .getLocalName(assertElement) }, assertElement);
        }

        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        
    } // traverseAsserts

    /*
     * Generate a name for an anonymous type
     */
    private String genAnonTypeName(Element complexTypeDecl) {
        
        // Generate a unique name for the anonymous type by concatenating together the
        // names of parent nodes
        // The name is quite good for debugging/error purposes, but we may want to
        // revisit how this is done for performance reasons (LM).
        StringBuffer typeName = new StringBuffer("#AnonType_");
        Element node = DOMUtil.getParent(complexTypeDecl);
        while (node != null && (node != DOMUtil.getRoot(DOMUtil.getDocument(node)))) {
            typeName.append(node.getAttribute(SchemaSymbols.ATT_NAME));
            node = DOMUtil.getParent(node);
        }
        return typeName.toString();
    }
    
    
    private void handleComplexTypeError(String messageId,Object[] args,
            Element e) {
        
        if (messageId!=null) {
            reportSchemaError(messageId, args, e);
        }
        
        //
        //  Mock up the typeInfo structure so that there won't be problems during
        //  validation
        //
        fBaseType = SchemaGrammar.getXSAnyType(fSchemaHandler.fSchemaVersion);
        fContentType = XSComplexTypeDecl.CONTENTTYPE_MIXED;
        fXSSimpleType = null;
        fParticle = getErrorContent();
        // REVISIT: do we need to remove all attribute uses already added into
        // the attribute group? maybe it's ok to leave them there. -SG
        fAttrGrp.fAttributeWC = getErrorWildcard();
        
        return;
        
    }
    
    private void contentBackup() {
        if(fGlobalStore == null) {
            fGlobalStore = new Object [GLOBAL_NUM];
            fGlobalStorePos = 0;
        }
        if(fGlobalStorePos == fGlobalStore.length) {
            Object [] newArray = new Object[fGlobalStorePos+GLOBAL_NUM];
            System.arraycopy(fGlobalStore, 0, newArray, 0, fGlobalStorePos);
            fGlobalStore = newArray;
        }
        fGlobalStore[fGlobalStorePos++] = fComplexTypeDecl;
        fGlobalStore[fGlobalStorePos++] = fIsAbstract?Boolean.TRUE:Boolean.FALSE;
        fGlobalStore[fGlobalStorePos++] = fName ;
        fGlobalStore[fGlobalStorePos++] = fTargetNamespace;
        // let's save ourselves a couple of objects...
        fGlobalStore[fGlobalStorePos++] = new Integer((fDerivedBy << 16) + fFinal);
        fGlobalStore[fGlobalStorePos++] = new Integer((fBlock << 16) + fContentType);
        fGlobalStore[fGlobalStorePos++] = fBaseType;
        fGlobalStore[fGlobalStorePos++] = fAttrGrp;
        fGlobalStore[fGlobalStorePos++] = fParticle;
        fGlobalStore[fGlobalStorePos++] = fXSSimpleType;
        fGlobalStore[fGlobalStorePos++] = fAnnotations;
        fGlobalStore[fGlobalStorePos++] = fOpenContent;
        fGlobalStore[fGlobalStorePos++] = fAssertions;
    }
    
    private void contentRestore() {
        fAssertions = (XSAssertImpl[])fGlobalStore[--fGlobalStorePos];
        fOpenContent = (XSOpenContentDecl)fGlobalStore[--fGlobalStorePos];
        fAnnotations = (XSAnnotationImpl [])fGlobalStore[--fGlobalStorePos];
        fXSSimpleType = (XSSimpleType)fGlobalStore[--fGlobalStorePos];
        fParticle = (XSParticleDecl)fGlobalStore[--fGlobalStorePos];
        fAttrGrp = (XSAttributeGroupDecl)fGlobalStore[--fGlobalStorePos];
        fBaseType = (XSTypeDefinition)fGlobalStore[--fGlobalStorePos];
        int i = ((Integer)(fGlobalStore[--fGlobalStorePos])).intValue();
        fBlock = (short)(i >> 16);
        fContentType = (short)i;
        i = ((Integer)(fGlobalStore[--fGlobalStorePos])).intValue();
        fDerivedBy = (short)(i >> 16);
        fFinal = (short)i;
        fTargetNamespace = (String)fGlobalStore[--fGlobalStorePos];
        fName = (String)fGlobalStore[--fGlobalStorePos];
        fIsAbstract = ((Boolean)fGlobalStore[--fGlobalStorePos]).booleanValue();
        fComplexTypeDecl = (XSComplexTypeDecl)fGlobalStore[--fGlobalStorePos];
    }
    
    private void addAnnotation(XSAnnotationImpl annotation) {
        if(annotation == null)
            return;
        // it isn't very likely that there will be more than one annotation
        // in a complexType decl.  This saves us fromhaving to push/pop
        // one more object from the fGlobalStore, and that's bound
        // to be a savings for most applications
        if(fAnnotations == null) {
            fAnnotations = new XSAnnotationImpl[1];
        } else {
            XSAnnotationImpl [] tempArray = new XSAnnotationImpl[fAnnotations.length + 1];
            System.arraycopy(fAnnotations, 0, tempArray, 0, fAnnotations.length);
            fAnnotations = tempArray;
        }
        fAnnotations[fAnnotations.length-1] = annotation;
    }
    
    private void addAssertion(XSAssertImpl assertion) {
        if (assertion == null) {
            return;
        }
        // it isn't very likely that there will be more than one annotation
        // in a complexType decl.  This saves us fromhaving to push/pop
        // one more object from the fGlobalStore, and that's bound
        // to be a savings for most applications
        if (fAssertions == null) {
            fAssertions = new XSAssertImpl[1];
        }
        else {
            XSAssertImpl [] tempArray = new XSAssertImpl[fAssertions.length + 1];
            System.arraycopy(fAssertions, 0, tempArray, 0, fAssertions.length);
            fAssertions = tempArray;
        }
        fAssertions[fAssertions.length-1] = assertion;
    }
}
