/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.v2;

import org.apache.xerces.impl.v2.datatypes.*;
import org.apache.xerces.xni.QName;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XInt;
import org.apache.xerces.util.XIntPool;
import org.w3c.dom.Element;
import java.util.Hashtable;
import org.apache.xerces.util.DOMUtil;

/**
 * Class <code>XSDAbstractTraverser</code> serves as the base class for all
 * other <code>XSD???Traverser</code>s. It holds the common data and provide
 * a unified way to initialize these data.
 *
 * @author Elena Litani, IBM
 * @author Rahul Srivastava, Sun Microsystems Inc.
 *
 * @version $Id$
 */
abstract class XSDAbstractTraverser {

    protected static final QName ANY_TYPE = new QName(null,
                                                      SchemaSymbols.ATTVAL_ANYTYPE,
                                                      SchemaSymbols.ATTVAL_ANYTYPE,
                                                      SchemaSymbols.URI_SCHEMAFORSCHEMA);

    protected static final QName ANY_SIMPLE_TYPE = new QName(null,
                                                             SchemaSymbols.ATTVAL_ANYSIMPLETYPE,
                                                             SchemaSymbols.ATTVAL_ANYTYPE,
                                                             SchemaSymbols.URI_SCHEMAFORSCHEMA);

    protected static final String NO_NAME      = "(no name)";

    // Flags for checkOccurrences to indicate any special
    // restrictions on minOccurs and maxOccurs relating to "all".
    //    NOT_ALL_CONTEXT    - not processing an <all>
    //    PROCESSING_ALL_EL  - processing an <element> in an <all>
    //    GROUP_REF_WITH_ALL - processing <group> reference that contained <all>
    //    CHILD_OF_GROUP     - processing a child of a model group definition
    //    PROCESSING_ALL_GP  - processing an <all> group itself

    protected static final int NOT_ALL_CONTEXT    = 0;
    protected static final int PROCESSING_ALL_EL  = 1;
    protected static final int GROUP_REF_WITH_ALL = 2;
    protected static final int CHILD_OF_GROUP     = 3;
    protected static final int PROCESSING_ALL_GP  = 4;

    //Shared data
    protected XSDHandler            fSchemaHandler = null;
    protected SymbolTable           fSymbolTable = null;
    protected XSAttributeChecker    fAttrChecker = null;
    protected XMLErrorReporter      fErrorReporter = null;

    static final XIntPool fXIntPool = new XIntPool();

    XSDAbstractTraverser (XSDHandler handler,
                          XSAttributeChecker attrChecker) {
        fSchemaHandler = handler;
        fAttrChecker = attrChecker;
    }

    //REVISIT: Implement
    void reset(XMLErrorReporter errorReporter, SymbolTable symbolTable) {
        fErrorReporter = errorReporter;
        fSymbolTable = symbolTable;
    }

    // traver the annotation declaration
    // REVISIT: store annotation information for PSVI
    // REVISIT: how to pass the parentAttrs? as DOM attributes?
    //          as name/value pairs (string)? in parsed form?
    // REVISIT: what to return
    void traverseAnnotationDecl(Element annotationDecl, Object[] parentAttrs,
                                boolean isGlobal, XSDocumentInfo schemaDoc) {
        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(annotationDecl, isGlobal, schemaDoc);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        for(Element child = DOMUtil.getFirstChildElement(annotationDecl);
            child != null;
            child = DOMUtil.getNextSiblingElement(child)) {
            String name = child.getLocalName();

            // the only valid children of "annotation" are
            // "appinfo" and "documentation"
            if(!((name.equals(SchemaSymbols.ELT_APPINFO)) ||
                 (name.equals(SchemaSymbols.ELT_DOCUMENTATION)))) {
                reportGenericSchemaError("an <annotation> can only contain <appinfo> and <documentation> elements");
            }

            // General Attribute Checking
            // There is no difference between global or local appinfo/documentation,
            // so we assume it's always global.
            attrValues = fAttrChecker.checkAttributes(child, true, schemaDoc);
            fAttrChecker.returnAttrArray(attrValues, schemaDoc);
        }

        // REVISIT: an annotation decl should be returned when we support PSVI
    }


    //
    // Traverse a set of attribute and attribute group elements
    // Needed by complexType and attributeGroup traversal
    //
    boolean traverseAttrsAndAttrGrps(Element firstAttr, XSAttributeGroupDecl attrGrp,
                                     XSDocumentInfo schemaDoc, SchemaGrammar grammar ) {

        Element child=null;
        XSAttributeGroupDecl tempAttrGrp = null;
        XSAttributeUse tempAttrUse = null;
        String childName;

        for (child=firstAttr; child!=null; child=DOMUtil.getNextSiblingElement(child)) {
            childName = child.getLocalName();
            if (childName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                tempAttrUse = fSchemaHandler.fAttributeTraverser.traverseLocal(child,
                              schemaDoc, grammar);
             	if (attrGrp.getAttributeUse(tempAttrUse.fAttrDecl.fTargetNamespace,
                                            tempAttrUse.fAttrDecl.fName)==null) {
                   attrGrp.addAttributeUse(tempAttrUse);
                }
                else {
		    reportGenericSchemaError("Duplicate attribute " + 
                                tempAttrUse.fAttrDecl.fName + " found ");
                }
            }
            else if (childName.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                //REVISIT: do we need to save some state at this point??
                tempAttrGrp = fSchemaHandler.fAttributeGroupTraverser.traverseLocal(
                              child, schemaDoc, grammar);
                XSAttributeUse[] attrUseS = tempAttrGrp.getAttributeUses();
                XSAttributeUse existingAttrUse = null;
                for (int i=0; i<attrUseS.length; i++) {
                	existingAttrUse = attrGrp.getAttributeUse(attrUseS[i].fAttrDecl.fTargetNamespace,
                	                                          attrUseS[i].fAttrDecl.fName);
                	if (existingAttrUse == null) {
				attrGrp.addAttributeUse(attrUseS[i]);
			}
			else {
				reportGenericSchemaError("Duplicate attribute " + 
                                existingAttrUse.fAttrDecl.fName + " found ");
			}
                }
                
                if (tempAttrGrp.fAttributeWC != null) {
                	if (attrGrp.fAttributeWC == null) {
                		attrGrp.fAttributeWC = tempAttrGrp.fAttributeWC;
                	}
                	// perform intersection of attribute wildcard
                	else {
                		attrGrp.fAttributeWC = attrGrp.fAttributeWC.
                				       performIntersectionWith(tempAttrGrp.fAttributeWC);
                	}
                }
            }
            else
                break;
        } // for

        if (child != null) {
            childName = child.getLocalName();
            if (childName.equals(SchemaSymbols.ELT_ANYATTRIBUTE)) {
                XSWildcardDecl tempAttrWC = fSchemaHandler.fWildCardTraverser.
                			    traverseAnyAttribute(child, schemaDoc, grammar);
                if (attrGrp.fAttributeWC == null) {
                	attrGrp.fAttributeWC = tempAttrWC;
                }
                // perform intersection of attribute wildcard
                else {
                	attrGrp.fAttributeWC = attrGrp.fAttributeWC.
                			       performIntersectionWith(tempAttrWC);
                }
                child = DOMUtil.getNextSiblingElement(child);
            }

            if (child != null) {
                // Error - the element is not an attribute, attributeGroup or anyAttr
                return false;
            }
        }

        // Success
        return true;

    }

    void reportSchemaError (String key, Object[] args) {
        fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                   key, args,
                                   XMLErrorReporter.SEVERITY_ERROR);
    }

    // REVISIT: is it how we want to handle error reporting?
    void reportGenericSchemaError (String error) {
        fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                   "General",
                                   new Object[]{error},
                                   XMLErrorReporter.SEVERITY_ERROR);
    }

    //
    // Evaluates content of Annotation if present.
    //
    // @param: content - the first child of <code>elm</code> that needs to be checked
    // @param parentAttrs:  attributes of the parent element; needed for PSVI.
    // @param schemaDoc:  the currently active schema document.
    //

    //REVISIT: if we want to expose annotation information to the application,
    //         then it may be difficult to use this method because traversers
    //         will need to store the <annotation> component in the grammar.
    //          We would then have to provide some means of accessing that component.
    // Note that it is assumed this method is never invoked in a global context.

    Element checkContent( Element content, Object[] parentAttrs, XSDocumentInfo schemaDoc ) {

        if (content == null) {
             return content;
        }

        if (content.getLocalName().equals(SchemaSymbols.ELT_ANNOTATION)) {

             traverseAnnotationDecl(content, parentAttrs, false, schemaDoc);

             content = DOMUtil.getNextSiblingElement(content);
        }
            
        return content;
    }

    /**
     * Element/Attribute traversers call this method to check whether
     * the type is NOTATION without enumeration facet
     */
    void checkNotationType(String refName, XSTypeDecl typeDecl) {
        if (typeDecl instanceof NOTATIONDatatypeValidator) {
            //REVISIT: to check whether there is an enumeration facet
            //if (((DatatypeValidator)typeDecl).hasEnumFacet) {
            if (false) {
                reportGenericSchemaError("[enumeration-required-notation] It is an error for NOTATION to be used "+
                                         "directly in a schema in element/attribute '"+refName+"'. " +
                                         "Only datatypes that are derived from NOTATION by specifying a value for enumeration can be used in a schema.");
            }
        }
    }

    // Checks constraints for minOccurs, maxOccurs
    protected XSParticleDecl checkOccurrences(XSParticleDecl particle,
                                              String particleName, Element parent,
                                              int allContextFlags,
                                              long defaultVals) {

        int min = particle.fMinOccurs;
        int max = particle.fMaxOccurs;
        boolean defaultMin = (defaultVals & (1 << XSAttributeChecker.ATTIDX_MINOCCURS)) != 0;
        boolean defaultMax = (defaultVals & (1 << XSAttributeChecker.ATTIDX_MAXOCCURS)) != 0;

        boolean processingAllEl = ((allContextFlags & PROCESSING_ALL_EL) != 0);
        boolean processingAllGP = ((allContextFlags & PROCESSING_ALL_GP) != 0);
        boolean groupRefWithAll = ((allContextFlags & GROUP_REF_WITH_ALL) != 0);
        boolean isGroupChild    = ((allContextFlags & CHILD_OF_GROUP) != 0);

        // Neither minOccurs nor maxOccurs may be specified
        // for the child of a model group definition.
        if (isGroupChild && (!defaultMin || !defaultMax)) {
            Object[] args = new Object[]{parent.getAttribute(SchemaSymbols.ATT_NAME),
                                         particleName};
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                       "MinMaxOnGroupChild",
                                       args,
                                       XMLErrorReporter.SEVERITY_ERROR);
            min = max = 1;
        }

        // If minOccurs=maxOccurs=0, no component is specified
        if (min == 0 && max== 0) {
            particle.fType = XSParticleDecl.PARTICLE_EMPTY;
            return null;
        }

        // For the elements referenced in an <all>, minOccurs attribute
        // must be zero or one, and maxOccurs attribute must be one.
        // For a complex type definition that contains an <all> or a
        // reference a <group> whose model group is an all model group,
        // minOccurs and maxOccurs must be one.
        if (processingAllEl || groupRefWithAll || processingAllGP) {
            String errorMsg;
            if ((processingAllGP||groupRefWithAll||min!=0) && min !=1) {
                if (processingAllEl) {
                    errorMsg = "BadMinMaxForAllElem";
                }
                else if (processingAllGP) {
                    errorMsg = "BadMinMaxForAllGp";
                }
                else {
                    errorMsg = "BadMinMaxForGroupWithAll";
                }
                Object[] args = new Object [] {"minOccurs", fXIntPool.getXInt(min)};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           errorMsg,
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
                min = 1;
            }

            if (max != 1) {

                if (processingAllEl) {
                    errorMsg = "BadMinMaxForAllElem";
                }
                else if (processingAllGP) {
                    errorMsg = "BadMinMaxForAllGp";
                }
                else {
                    errorMsg = "BadMinMaxForGroupWithAll";
                }

                Object[] args = new Object [] {"maxOccurs", fXIntPool.getXInt(max)};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           errorMsg,
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
                max = 1;
            }
        }

        particle.fMaxOccurs = min;
        particle.fMaxOccurs = max;

        return particle;
    }
}
