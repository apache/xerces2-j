/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xerces" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xerces.impl.xs.traversers;

import org.apache.xerces.impl.dv.InvalidDatatypeFacetException;
import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.XSFacets;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSAttributeGroupDecl;
import org.apache.xerces.impl.xs.XSAttributeUseImpl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSConstraints;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.XSWildcardDecl;
import org.apache.xerces.impl.xs.psvi.XSConstants;
import org.apache.xerces.impl.xs.psvi.XSObjectList;
import org.apache.xerces.impl.xs.psvi.XSTypeDefinition;
import org.apache.xerces.impl.xs.util.XInt;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.xni.QName;
import org.w3c.dom.Element;

/**
 * A complex type definition schema component traverser.
 *
 * <complexType
 *   abstract = boolean : false
 *   block = (#all | List of (extension | restriction))
 *   final = (#all | List of (extension | restriction))
 *   id = ID
 *   mixed = boolean : false
 *   name = NCName
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?, (simpleContent | complexContent |
 *            ((group | all | choice | sequence)?,
 *            ((attribute | attributeGroup)*, anyAttribute?))))
 * </complexType>
 * @version $Id$
 */

class  XSDComplexTypeTraverser extends XSDAbstractParticleTraverser {

    // size of stack to hold globals:
    private final static int GLOBAL_NUM = 10;

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

    private XSParticleDecl fEmptyParticle = null;

    // our own little stack to retain state when getGlobalDecls is called:
    private Object [] fGlobalStore = null;
    private int fGlobalStorePos = 0;

    XSDComplexTypeTraverser (XSDHandler handler,
                             XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }


    private static final boolean DEBUG=false;

    private SchemaDVFactory schemaFactory = SchemaDVFactory.getInstance();

    private class ComplexTypeRecoverableError extends Exception {

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
                                    SchemaGrammar grammar) {


        Object[] attrValues = fAttrChecker.checkAttributes(complexTypeNode, false,
                                                           schemaDoc);
        String complexTypeName = genAnonTypeName(complexTypeNode);
        contentBackup();
        XSComplexTypeDecl type = traverseComplexTypeDecl (complexTypeNode,
                                                          complexTypeName, attrValues, schemaDoc, grammar);
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
                                                          complexTypeName, attrValues, schemaDoc, grammar);
        contentRestore();
        if (complexTypeName == null) {
            reportSchemaError("s4s-att-must-appear", new Object[]{SchemaSymbols.ELT_COMPLEXTYPE, SchemaSymbols.ATT_NAME}, complexTypeNode);
        } else {
            grammar.addGlobalTypeDecl(type);
        }
        // need to add the type to the grammar for later constraint checking
        grammar.addComplexTypeDecl(type, fSchemaHandler.element2Locator(complexTypeNode));
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        return type;
    }


    private XSComplexTypeDecl traverseComplexTypeDecl(Element complexTypeDecl,
                                                      String complexTypeName,
                                                      Object[] attrValues,
                                                      XSDocumentInfo schemaDoc,
                                                      SchemaGrammar grammar) {

        fComplexTypeDecl = new XSComplexTypeDecl();
        fAttrGrp = new XSAttributeGroupDecl();
        Boolean abstractAtt  = (Boolean) attrValues[XSAttributeChecker.ATTIDX_ABSTRACT];
        XInt    blockAtt     = (XInt)    attrValues[XSAttributeChecker.ATTIDX_BLOCK];
        Boolean mixedAtt     = (Boolean) attrValues[XSAttributeChecker.ATTIDX_MIXED];
        XInt    finalAtt     = (XInt)    attrValues[XSAttributeChecker.ATTIDX_FINAL];

        fName = complexTypeName;
        fComplexTypeDecl.setName(fName);
        fTargetNamespace = schemaDoc.fTargetNamespace;
        fBlock = blockAtt == null ?
                             schemaDoc.fBlockDefault : blockAtt.shortValue();
        fFinal = finalAtt == null ?
                             schemaDoc.fFinalDefault : finalAtt.shortValue();
        if (abstractAtt != null && abstractAtt.booleanValue())
            fIsAbstract = true;


        Element child = null;

        try {
            // ---------------------------------------------------------------
            // First, handle any ANNOTATION declaration and get next child
            // ---------------------------------------------------------------
            child = DOMUtil.getFirstChildElement(complexTypeDecl);

            if (child != null) {
                // traverse annotation if any
                if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                    child = DOMUtil.getNextSiblingElement(child);
                }
                if (child !=null && DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                           new Object[]{fName,SchemaSymbols.ELT_ANNOTATION},
                           child);
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
                fBaseType = SchemaGrammar.fAnyType;
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
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
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
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                                                          new Object[]{fName,siblingName},
                                                          elemTmp);
                }
            }
            else {
                //
                // We must have ....
                // GROUP, ALL, SEQUENCE or CHOICE, followed by optional attributes
                // Note that it's possible that only attributes are specified.
                //

                // set the base to the anyType
                fBaseType = SchemaGrammar.fAnyType;
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
                fAttrGrp, fXSSimpleType, fParticle);
        return fComplexTypeDecl; 
    }


    private void traverseSimpleContent(Element simpleContentElement,
                                       XSDocumentInfo schemaDoc,
                                       SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {


        Object[] attrValues = fAttrChecker.checkAttributes(simpleContentElement, false,
                                                           schemaDoc);

        // -----------------------------------------------------------------------
        // Set content type
        // -----------------------------------------------------------------------
        fContentType = XSComplexTypeDecl.CONTENTTYPE_SIMPLE;
        fParticle = null;

        Element simpleContent = DOMUtil.getFirstChildElement(simpleContentElement);
        if (simpleContent != null) {
            // traverse annotation if any
            if (DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(simpleContent, attrValues, false, schemaDoc);
                simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
            }
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        // If there are no children, return
        if (simpleContent==null) {
            throw new ComplexTypeRecoverableError("src-ct.0.2",
                            new Object[]{fName,SchemaSymbols.ELT_SIMPLECONTENT},
                            simpleContentElement);
        }

        // -----------------------------------------------------------------------
        // The content should be either "restriction" or "extension"
        // -----------------------------------------------------------------------
        String simpleContentName = DOMUtil.getLocalName(simpleContent);
        if (simpleContentName.equals(SchemaSymbols.ELT_RESTRICTION))
            fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
        else if (simpleContentName.equals(SchemaSymbols.ELT_EXTENSION))
            fDerivedBy = XSConstants.DERIVATION_EXTENSION;
        else {
            throw new ComplexTypeRecoverableError("src-ct.0.1",
                            new Object[]{fName,simpleContentName},
                            simpleContent);
        }
        Element elemTmp = DOMUtil.getNextSiblingElement(simpleContent);
        if (elemTmp != null) {
            String siblingName = DOMUtil.getLocalName(elemTmp);
            throw new ComplexTypeRecoverableError("src-ct.0.1",
                            new Object[]{fName,siblingName},
                            elemTmp);
        }

        attrValues = fAttrChecker.checkAttributes(simpleContent, false,
                                                  schemaDoc);
        QName baseTypeName = (QName)  attrValues[XSAttributeChecker.ATTIDX_BASE];
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);


        // -----------------------------------------------------------------------
        // Need a base type.
        // -----------------------------------------------------------------------
        if (baseTypeName==null) {
            throw new ComplexTypeRecoverableError("src-ct.0.3",
                            new Object[]{fName}, simpleContent);
        }

        XSTypeDefinition type = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc,
                                      XSDHandler.TYPEDECL_TYPE, baseTypeName,
                                      simpleContent);
        if (type==null)
            throw new ComplexTypeRecoverableError();

        fBaseType = type;

        XSSimpleType baseValidator = null;
        XSComplexTypeDecl baseComplexType = null;
        int baseFinalSet = 0;

        // If the base type is complex, it must have simpleContent
        if ((type.getTypeCategory() == XSTypeDefinition.COMPLEX_TYPE)) {

            baseComplexType = (XSComplexTypeDecl)type;
            if (baseComplexType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_SIMPLE) {
                throw new ComplexTypeRecoverableError("src-ct.2",
                                new Object[]{fName}, simpleContent);
            }
            baseFinalSet = baseComplexType.getFinal();
            baseValidator = (XSSimpleType)baseComplexType.getSimpleType();
        }
        else {
            baseValidator = (XSSimpleType)type;
            if (fDerivedBy == XSConstants.DERIVATION_RESTRICTION) {
                throw new ComplexTypeRecoverableError("src-ct.2",
                                new Object[]{fName}, simpleContent);
            }
            baseFinalSet=baseValidator.getFinal();
        }

        // -----------------------------------------------------------------------
        // Check that the base permits the derivation
        // -----------------------------------------------------------------------
        if ((baseFinalSet & fDerivedBy)!=0) {
            String errorKey = (fDerivedBy==XSConstants.DERIVATION_EXTENSION) ?
                              "cos-ct-extends.1.1" : "derivation-ok-restriction.1";
            throw new ComplexTypeRecoverableError(errorKey,
                                new Object[]{fName}, simpleContent);
        }

        // -----------------------------------------------------------------------
        // Skip over any potential annotations
        // -----------------------------------------------------------------------
        simpleContent = DOMUtil.getFirstChildElement(simpleContent);
        if (simpleContent != null) {
            // traverse annotation if any

            if (DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(simpleContent, null, false, schemaDoc);
                simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
            }

            if (simpleContent !=null &&
                DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_ANNOTATION)){
                throw new ComplexTypeRecoverableError("src-ct.0.1",
                       new Object[]{fName,SchemaSymbols.ELT_ANNOTATION},
                       simpleContent);
            }
        }

        // -----------------------------------------------------------------------
        // Process a RESTRICTION
        // -----------------------------------------------------------------------
        if (fDerivedBy == XSConstants.DERIVATION_RESTRICTION) {

            // -----------------------------------------------------------------------
            // There may be a simple type definition in the restriction element
            // The data type validator will be based on it, if specified
            // -----------------------------------------------------------------------
            if (simpleContent !=null &&
            DOMUtil.getLocalName(simpleContent).equals(SchemaSymbols.ELT_SIMPLETYPE )) {

                XSSimpleType dv = fSchemaHandler.fSimpleTypeTraverser.traverseLocal(
                      simpleContent, schemaDoc, grammar);
                if (dv == null)
                    throw new ComplexTypeRecoverableError();

                //check that this datatype validator is validly derived from the base
                //according to derivation-ok-restriction 5.1.1

                if (!XSConstraints.checkSimpleDerivationOk(dv, baseValidator,
                                                           baseValidator.getFinal())) {
                    throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.1.1",
                           new Object[]{fName},
                           simpleContent);
                }
                baseValidator = dv;
                simpleContent = DOMUtil.getNextSiblingElement(simpleContent);
            }

            // -----------------------------------------------------------------------
            // Traverse any facets
            // -----------------------------------------------------------------------
            Element attrNode = null;
            XSFacets facetData = null;
            short presentFacets = 0 ;
            short fixedFacets = 0 ;

            if (simpleContent!=null) {
                FacetInfo fi = traverseFacets(simpleContent, baseValidator, schemaDoc);
                attrNode = fi.nodeAfterFacets;
                facetData = fi.facetdata;
                presentFacets = fi.fPresentFacets;
                fixedFacets = fi.fFixedFacets;
            }

            fXSSimpleType = schemaFactory.createTypeRestriction(null,schemaDoc.fTargetNamespace,(short)0,baseValidator);
            try{
                fValidationState.setNamespaceSupport(schemaDoc.fNamespaceSupport);
                fXSSimpleType.applyFacets(facetData, presentFacets, fixedFacets, fValidationState);
            }catch(InvalidDatatypeFacetException ex){
                reportSchemaError(ex.getKey(), ex.getArgs(), simpleContent);
            }

            // -----------------------------------------------------------------------
            // Traverse any attributes
            // -----------------------------------------------------------------------
            if (attrNode != null) {
                if (!isAttrOrAttrGroup(attrNode)) {
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                             new Object[]{fName,DOMUtil.getLocalName(attrNode)},
                             attrNode);
                }
                Element node=traverseAttrsAndAttrGrps(attrNode,fAttrGrp,
                                                      schemaDoc,grammar,fComplexTypeDecl);
                if (node!=null) {
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                             new Object[]{fName,DOMUtil.getLocalName(node)},
                             node);
                }
            }

            mergeAttributes(baseComplexType.getAttrGrp(), fAttrGrp, fName, false, simpleContentElement);
            // Prohibited uses must be removed after merge for RESTRICTION
            fAttrGrp.removeProhibitedAttrs();

            String errorCode=fAttrGrp.validRestrictionOf(baseComplexType.getAttrGrp());
            if (errorCode != null) {
                throw new ComplexTypeRecoverableError(errorCode,
                             new Object[]{fName}, attrNode);
            }

        }
        // -----------------------------------------------------------------------
        // Process a EXTENSION
        // -----------------------------------------------------------------------
        else {
            fXSSimpleType = baseValidator;
            if (simpleContent != null) {
                // -----------------------------------------------------------------------
                // Traverse any attributes
                // -----------------------------------------------------------------------
                Element attrNode = simpleContent;
                if (!isAttrOrAttrGroup(attrNode)) {
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                                                          new Object[]{fName,DOMUtil.getLocalName(attrNode)},
                                                          attrNode);
                }
                Element node=traverseAttrsAndAttrGrps(attrNode,fAttrGrp,
                                                      schemaDoc,grammar,fComplexTypeDecl);

                if (node!=null) {
                    throw new ComplexTypeRecoverableError("src-ct.0.1",
                                                          new Object[]{fName,DOMUtil.getLocalName(node)},
                                                          node);
                }
                // Remove prohibited uses.   Should be done prior to any merge.
                fAttrGrp.removeProhibitedAttrs();
            }

            if (baseComplexType != null) {
                mergeAttributes(baseComplexType.getAttrGrp(), fAttrGrp, fName, true, simpleContentElement);
            }
        }
    }

    private void traverseComplexContent(Element complexContentElement,
                                        boolean mixedOnType, XSDocumentInfo schemaDoc,
                                        SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {


        Object[] attrValues = fAttrChecker.checkAttributes(complexContentElement, false,
                                                           schemaDoc);


        // -----------------------------------------------------------------------
        // Determine if this is mixed content
        // -----------------------------------------------------------------------
        boolean mixedContent = mixedOnType;
        Boolean mixedAtt     = (Boolean) attrValues[XSAttributeChecker.ATTIDX_MIXED];
        if (mixedAtt != null) {
            mixedContent = mixedAtt.booleanValue();
        }


        // -----------------------------------------------------------------------
        // Since the type must have complex content, set the simple type validators
        // to null
        // -----------------------------------------------------------------------
        fXSSimpleType = null;

        Element complexContent = DOMUtil.getFirstChildElement(complexContentElement);
        if (complexContent != null) {
            // traverse annotation if any
            if (DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(complexContent, attrValues, false, schemaDoc);
                complexContent = DOMUtil.getNextSiblingElement(complexContent);
            }
        }

        fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        // If there are no children, return
        if (complexContent==null) {
            throw new ComplexTypeRecoverableError("src-ct.0.2",
                      new Object[]{fName,SchemaSymbols.ELT_COMPLEXCONTENT},
                      complexContentElement);
        }

        // -----------------------------------------------------------------------
        // The content should be either "restriction" or "extension"
        // -----------------------------------------------------------------------
        String complexContentName = DOMUtil.getLocalName(complexContent);
        if (complexContentName.equals(SchemaSymbols.ELT_RESTRICTION))
            fDerivedBy = XSConstants.DERIVATION_RESTRICTION;
        else if (complexContentName.equals(SchemaSymbols.ELT_EXTENSION))
            fDerivedBy = XSConstants.DERIVATION_EXTENSION;
        else {
            throw new ComplexTypeRecoverableError("src-ct.0.1",
                      new Object[]{fName, complexContentName}, complexContent);
        }
        Element elemTmp = DOMUtil.getNextSiblingElement(complexContent);
        if (elemTmp != null) {
            String siblingName = DOMUtil.getLocalName(elemTmp);
            throw new ComplexTypeRecoverableError("src-ct.0.1",
                      new Object[]{fName, siblingName}, elemTmp);
        }

        attrValues = fAttrChecker.checkAttributes(complexContent, false,
                                                  schemaDoc);
        QName baseTypeName = (QName)  attrValues[XSAttributeChecker.ATTIDX_BASE];
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);


        // -----------------------------------------------------------------------
        // Need a base type.  Check that it's a complex type
        // -----------------------------------------------------------------------
        if (baseTypeName==null) {
            throw new ComplexTypeRecoverableError("src-ct.0.3",
                      new Object[]{fName}, complexContent);
        }

        XSTypeDefinition type = (XSTypeDefinition)fSchemaHandler.getGlobalDecl(schemaDoc,
                                                                   XSDHandler.TYPEDECL_TYPE,
                                                                   baseTypeName,
                                                                   complexContent);

        if (type==null)
            throw new ComplexTypeRecoverableError();

        if (! (type instanceof XSComplexTypeDecl)) {
            throw new ComplexTypeRecoverableError("src-ct.1",
                      new Object[]{fName}, complexContent);
        }
        XSComplexTypeDecl baseType = (XSComplexTypeDecl)type;
        fBaseType = baseType;

        // -----------------------------------------------------------------------
        // Check that the base permits the derivation
        // -----------------------------------------------------------------------
        if ((baseType.getFinal() & fDerivedBy)!=0) {
            String errorKey = (fDerivedBy==XSConstants.DERIVATION_EXTENSION) ?
                              "cos-ct-extends.1.1" : "derivation-ok-restriction.1";
            throw new ComplexTypeRecoverableError(errorKey,
                                new Object[]{fName}, complexContent);
        }

        // -----------------------------------------------------------------------
        // Skip over any potential annotations
        // -----------------------------------------------------------------------
        complexContent = DOMUtil.getFirstChildElement(complexContent);

        if (complexContent != null) {
            // traverse annotation if any
            if (DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(complexContent, null, false, schemaDoc);
                complexContent = DOMUtil.getNextSiblingElement(complexContent);
            }
            if (complexContent !=null &&
               DOMUtil.getLocalName(complexContent).equals(SchemaSymbols.ELT_ANNOTATION)){
                throw new ComplexTypeRecoverableError("src-ct.0.1",
                       new Object[]{fName,SchemaSymbols.ELT_ANNOTATION}, complexContent);
            }
        }
        // -----------------------------------------------------------------------
        // Process the content.  Note:  should I try to catch any complexType errors
        // here in order to return the attr array?
        // -----------------------------------------------------------------------
        processComplexContent(complexContent, mixedContent, true, schemaDoc,
                              grammar);

        // -----------------------------------------------------------------------
        // Compose the final content and attribute uses
        // -----------------------------------------------------------------------
        XSParticleDecl baseContent = (XSParticleDecl)baseType.getParticle();
        if (fDerivedBy==XSConstants.DERIVATION_RESTRICTION) {

            // This is an RESTRICTION

            // N.B. derivation-ok-restriction.5.2 is checked under schema 
            // full checking.   That's because we need to wait until locals are 
            // traversed so that occurrence information is correct.

            
            if (fParticle!=null && baseContent==null) {
                //REVISIT - need better error msg
                throw new ComplexTypeRecoverableError("derivation-ok-restriction.5.3",
                                          new Object[]{fName}, complexContent);
            }

            mergeAttributes(baseType.getAttrGrp(), fAttrGrp, fName, false, complexContent);
            if (baseType != SchemaGrammar.fAnyType) {
                String error = fAttrGrp.validRestrictionOf(baseType.getAttrGrp());
                if (error != null) {
                    throw new ComplexTypeRecoverableError(error,
                            new Object[]{fName}, complexContent);
                }
            }

            // Remove prohibited uses.   Must be done after merge for RESTRICTION.
            fAttrGrp.removeProhibitedAttrs();

        }
        else {

            // This is an EXTENSION

            // Create the particle
            if (fParticle == null) {
                fContentType = baseType.getContentType();
                fParticle = baseContent;
            }
            else if (baseType.getContentType() == XSComplexTypeDecl.CONTENTTYPE_EMPTY) {
            }
            else {
                // if the content of either type is an "all" model group, error.
                if (fParticle.fType == XSParticleDecl.PARTICLE_MODELGROUP &&
                    ((XSModelGroupImpl)fParticle.fValue).fCompositor == XSModelGroupImpl.MODELGROUP_ALL ||
                    ((XSParticleDecl)baseType.getParticle()).fType == XSParticleDecl.PARTICLE_MODELGROUP &&
                    ((XSModelGroupImpl)(((XSParticleDecl)baseType.getParticle())).fValue).fCompositor == XSModelGroupImpl.MODELGROUP_ALL) {
                    throw new ComplexTypeRecoverableError("cos-all-limited.1.2",
                          null, complexContent);
                }
                // the "sequence" model group to contain both particles
                XSModelGroupImpl group = new XSModelGroupImpl();
                group.fCompositor = XSModelGroupImpl.MODELGROUP_SEQUENCE;
                group.fParticleCount = 2;
                group.fParticles = new XSParticleDecl[2];
                group.fParticles[0] = (XSParticleDecl)baseType.getParticle();
                group.fParticles[1] = fParticle;
                // the particle to contain the above sequence
                XSParticleDecl particle = new XSParticleDecl();
                particle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
                particle.fValue = group;
                
                fParticle = particle;
            }

            //
            // Check if the contentType of the base is consistent with the new type
            // cos-ct-extends.1.4.2.2
            if (baseType.getContentType() != XSComplexTypeDecl.CONTENTTYPE_EMPTY) {
                if (((baseType.getContentType() ==
                      XSComplexTypeDecl.CONTENTTYPE_ELEMENT) &&
                     fContentType != XSComplexTypeDecl.CONTENTTYPE_ELEMENT) ||
                    ((baseType.getContentType() ==
                      XSComplexTypeDecl.CONTENTTYPE_MIXED) &&
                      fContentType != XSComplexTypeDecl.CONTENTTYPE_MIXED)) {

                    throw new ComplexTypeRecoverableError("cos-ct-extends.1.4.2.2.2.2.1",
                          new Object[]{fName}, complexContent);
                }

            }

            // Remove prohibited uses.   Must be done before merge for EXTENSION.
            fAttrGrp.removeProhibitedAttrs();
            mergeAttributes(baseType.getAttrGrp(), fAttrGrp, fName, true, complexContent);

        }

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
        XSAttributeUseImpl existingAttrUse, duplicateAttrUse =  null, oneAttrUse;
        int attrCount = attrUseS.getLength();
        for (int i=0; i<attrCount; i++) {
            oneAttrUse = (XSAttributeUseImpl)attrUseS.item(i);
            existingAttrUse = toAttrGrp.getAttributeUse(oneAttrUse.fAttrDecl.getNamespace(),
                                                        oneAttrUse.fAttrDecl.getName());
            if (existingAttrUse == null) {

                String idName = toAttrGrp.addAttributeUse(oneAttrUse);
                if (idName != null) {
                    throw new ComplexTypeRecoverableError("ct-props-correct.5",
                          new Object[]{typeName, idName, oneAttrUse.fAttrDecl.getName()},
                          elem);
                }
            }
            else {
                if (extension) {
                    throw new ComplexTypeRecoverableError("ct-props-correct.4",
                          new Object[]{typeName, existingAttrUse.fAttrDecl.getName()},
                          elem);
                }
            }
        }
        // For extension, the wildcard must be formed by doing a union of the wildcards
        if (extension) {
            if (toAttrGrp.fAttributeWC==null) {
                toAttrGrp.fAttributeWC = fromAttrGrp.fAttributeWC;
            }
            else if (fromAttrGrp.fAttributeWC != null) {
                toAttrGrp.fAttributeWC = toAttrGrp.fAttributeWC.performUnionWith(fromAttrGrp.fAttributeWC, toAttrGrp.fAttributeWC.fProcessContents);
            }

        }
    }

    private void processComplexContent(Element complexContentChild,
                                       boolean isMixed, boolean isDerivation,
                                       XSDocumentInfo schemaDoc, SchemaGrammar grammar)
    throws ComplexTypeRecoverableError {

        Element attrNode = null;
        XSParticleDecl particle = null;

        if (complexContentChild != null) {
            // -------------------------------------------------------------
            // GROUP, ALL, SEQUENCE or CHOICE, followed by attributes, if specified.
            // Note that it's possible that only attributes are specified.
            // -------------------------------------------------------------


            String childName = DOMUtil.getLocalName(complexContentChild);

            if (childName.equals(SchemaSymbols.ELT_GROUP)) {

                particle = fSchemaHandler.fGroupTraverser.traverseLocal(complexContentChild,
                                                                        schemaDoc, grammar);
                attrNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particle = traverseSequence(complexContentChild,schemaDoc,grammar,
                                            NOT_ALL_CONTEXT,fComplexTypeDecl);
                if (particle != null) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        particle = null;
                }
                attrNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particle = traverseChoice(complexContentChild,schemaDoc,grammar,
                                          NOT_ALL_CONTEXT,fComplexTypeDecl);
                if (particle != null && particle.fMinOccurs == 0) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        particle = null;
                }
                attrNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                particle = traverseAll(complexContentChild,schemaDoc,grammar,
                                       PROCESSING_ALL_GP,fComplexTypeDecl);
                if (particle != null) {
                    XSModelGroupImpl group = (XSModelGroupImpl)particle.fValue;
                    if (group.fParticleCount == 0)
                        particle = null;
                }
                attrNode = DOMUtil.getNextSiblingElement(complexContentChild);
            }
            else {
                // Should be attributes here - will check below...
                attrNode = complexContentChild;
            }
        }

        if (particle == null && isMixed) {
            if (fEmptyParticle == null) {
                XSModelGroupImpl group = new XSModelGroupImpl();
                group.fCompositor = XSModelGroupImpl.MODELGROUP_SEQUENCE;
                group.fParticleCount = 0;
                group.fParticles = null;
                fEmptyParticle = new XSParticleDecl();
                fEmptyParticle.fType = XSParticleDecl.PARTICLE_MODELGROUP;
                fEmptyParticle.fValue = group;
            }
            particle = fEmptyParticle;
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
        if (attrNode != null) {
            if (!isAttrOrAttrGroup(attrNode)) {
                throw new ComplexTypeRecoverableError("src-ct.0.1",
                                                      new Object[]{fName,DOMUtil.getLocalName(attrNode)},
                                                      attrNode);
            }
            Element node =
            traverseAttrsAndAttrGrps(attrNode,fAttrGrp,schemaDoc,grammar,fComplexTypeDecl);
            if (node!=null) {
                throw new ComplexTypeRecoverableError("src-ct.0.1",
                                                      new Object[]{fName,DOMUtil.getLocalName(node)},
                                                      node);
            }
            // Only remove prohibited attribute uses if this isn't a derived type
            // Derivation-specific code worries about this elsewhere
            if (!isDerivation) {
                fAttrGrp.removeProhibitedAttrs();
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

    private void traverseSimpleContentDecl(Element simpleContentDecl) {
    }

    private void traverseComplexContentDecl(Element complexContentDecl,
                                            boolean mixedOnComplexTypeDecl) {
    }

    /*
     * Generate a name for an anonymous type
     */
    private String genAnonTypeName(Element complexTypeDecl) {

        // Generate a unique name for the anonymous type by concatenating together the
        // names of parent nodes
        // The name is quite good for debugging/error purposes, but we may want to
        // revisit how this is done for performance reasons (LM).
        String typeName;
        Element node = DOMUtil.getParent(complexTypeDecl);
        typeName="#AnonType_";
        while (node != null && (node != DOMUtil.getRoot(DOMUtil.getDocument(node)))) {
            typeName = typeName+node.getAttribute(SchemaSymbols.ATT_NAME);
            node = DOMUtil.getParent(node);
        }
        return typeName;
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
        fContentType = XSComplexTypeDecl.CONTENTTYPE_MIXED;
        fParticle = getErrorContent();
        // REVISIT: do we need to remove all attribute uses already added into
        // the attribute group? maybe it's ok to leave them there. -SG
        fAttrGrp.fAttributeWC = getErrorWildcard();

        return;

    }

    private XSParticleDecl getErrorContent() {
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

        return errorContent;
    }

    private XSWildcardDecl getErrorWildcard() {
        XSWildcardDecl errorWildcard = new XSWildcardDecl();
        errorWildcard.fProcessContents = XSWildcardDecl.PC_SKIP;
        return errorWildcard;
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
    }

    private void contentRestore() {
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
}
