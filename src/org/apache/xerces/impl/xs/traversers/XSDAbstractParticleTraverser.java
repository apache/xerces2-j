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

import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.impl.xs.util.XInt;
import org.w3c.dom.Element;

/**
 * @author Elena Litani, IBM
 * @author Sandy Gao, IBM
 * @version $Id$
 */
abstract class XSDAbstractParticleTraverser extends XSDAbstractTraverser {

    XSDAbstractParticleTraverser (XSDHandler handler,
                                  XSAttributeChecker gAttrCheck) {

        super(handler, gAttrCheck);
    }

    /**
     *
     * Traverse the "All" declaration
     *
     * <all
     *   id = ID
     *   maxOccurs = 1 : 1
     *   minOccurs = (0 | 1) : 1>
     *   Content: (annotation? , element*)
     * </all>
     **/
    XSParticleDecl traverseAll(Element allDecl,
                               XSDocumentInfo schemaDoc,
                               SchemaGrammar grammar,
                               int allContextFlags) {

        // General Attribute Checking

        Object[] attrValues = fAttrChecker.checkAttributes(allDecl, false, schemaDoc);

        Element child = DOMUtil.getFirstChildElement(allDecl);

        if (child !=null) {
            // traverse Annotation
            if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                child = DOMUtil.getNextSiblingElement(child);
            }
        }
        XSParticleDecl left = null;
        XSParticleDecl right = null;
        String childName = null;
        XSParticleDecl particle, temp;
        for (; child != null; child = DOMUtil.getNextSiblingElement(child)) {

            particle = null;
            childName = DOMUtil.getLocalName(child);

            // Only elements are allowed in <all>
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particle = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar, PROCESSING_ALL_EL);
            }
            else {
                Object[] args = { childName};
                reportSchemaError("AllContentRestricted", args, child);
            }

            if (left == null) {
                left = particle;
            }
            else if (right == null) {
                right = particle;
            }
            else {
                if (fSchemaHandler.fDeclPool !=null) {
                    temp = fSchemaHandler.fDeclPool.getParticleDecl();
                } else {        
                    temp = new XSParticleDecl();
                }
                temp.fType = XSParticleDecl.PARTICLE_ALL;
                temp.fValue = left;
                temp.fOtherValue = right;
                left = temp;
                right = particle;
            }
        }

        if (left != null) {
            if (fSchemaHandler.fDeclPool !=null) {
                temp = fSchemaHandler.fDeclPool.getParticleDecl();
            } else {        
                temp = new XSParticleDecl();
            }
            temp.fType = XSParticleDecl.PARTICLE_ALL;
            temp.fValue = left;
            temp.fOtherValue = right;
            left = temp;
        }

        // REVISIT: model group
        // Quick fix for the case that particle <all> does not have any children.
        // For now we return null. In the future we might want to return model group decl.
        if (left != null) {

            XInt minAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
            XInt maxAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];
            Long defaultVals = (Long)attrValues[XSAttributeChecker.ATTIDX_FROMDEFAULT];
            left.fMinOccurs = minAtt.intValue();
            left.fMaxOccurs = maxAtt.intValue();

            left = checkOccurrences(left,
                                    SchemaSymbols.ELT_ALL,
                                    (Element)allDecl.getParentNode(),
                                    allContextFlags,
                                    defaultVals.longValue());
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        return left;
    }

    /**
     * Traverse the Sequence declaration
     *
     * <sequence
     *   id = ID
     *   maxOccurs = string
     *   minOccurs = nonNegativeInteger>
     *   Content: (annotation? , (element | group | choice | sequence | any)*)
     * </sequence>
     *
     * @param seqDecl
     * @param schemaDoc
     * @param grammar
     * @return
     */
    XSParticleDecl traverseSequence(Element seqDecl,
                                    XSDocumentInfo schemaDoc,
                                    SchemaGrammar grammar,
                                    int allContextFlags) {

        return traverseSeqChoice(seqDecl, schemaDoc, grammar, allContextFlags, false);
    }

    /**
     * Traverse the Choice declaration
     *
     * <choice
     *   id = ID
     *   maxOccurs = string
     *   minOccurs = nonNegativeInteger>
     *   Content: (annotation? , (element | group | choice | sequence | any)*)
     * </choice>
     *
     * @param choiceDecl
     * @param schemaDoc
     * @param grammar
     * @return
     */
    XSParticleDecl traverseChoice(Element choiceDecl,
                                  XSDocumentInfo schemaDoc,
                                  SchemaGrammar grammar,
                                  int allContextFlags) {

        return traverseSeqChoice (choiceDecl, schemaDoc, grammar, allContextFlags, true);
    }

    /**
     * Common traversal for <choice> and <sequence>
     *
     * @param decl
     * @param schemaDoc
     * @param grammar
     * @param choice    If traversing <choice> this parameter is true.
     * @return
     */
    private XSParticleDecl traverseSeqChoice(Element decl,
                                             XSDocumentInfo schemaDoc,
                                             SchemaGrammar grammar,
                                             int allContextFlags,
                                             boolean choice) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(decl, false, schemaDoc);

        Element child = DOMUtil.getFirstChildElement(decl);
        if (child !=null) {
            // traverse Annotation
            if (DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                child = DOMUtil.getNextSiblingElement(child);
            }
        }
        XSParticleDecl left = null;
        XSParticleDecl right = null;
        boolean hadContent = false;
        String childName = null;
        XSParticleDecl particle, temp;
        for (;child != null;child = DOMUtil.getNextSiblingElement(child)) {

            particle = null;

            childName = DOMUtil.getLocalName(child);
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particle = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar, NOT_ALL_CONTEXT);
            }
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {
                particle = fSchemaHandler.fGroupTraverser.traverseLocal(child, schemaDoc, grammar);

                // A content type of all can only appear
                // as the content type of a complex type definition.
                if (hasAllContent(particle)) {
                    // don't insert the "all" particle, otherwise we won't be
                    // able to create DFA from this content model
                    particle = null;
                    Object [] args;
                    if (choice) {
                        args = new Object[]{SchemaSymbols.ELT_CHOICE};
                    }
                    else {
                        args = new Object[]{SchemaSymbols.ELT_SEQUENCE};
                    }
                    reportSchemaError("AllContentLimited",args, child);
                }

            }
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particle = traverseChoice( child,schemaDoc, grammar, NOT_ALL_CONTEXT);
            }
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particle = traverseSequence(child,schemaDoc, grammar, NOT_ALL_CONTEXT);
            }
            else if (childName.equals(SchemaSymbols.ELT_ANY)) {
                particle = fSchemaHandler.fWildCardTraverser.traverseAny(child, schemaDoc, grammar);
            }
            else {
                Object [] args;
                if (choice) {
                    args = new Object[]{SchemaSymbols.ELT_CHOICE};
                }
                else {
                    args = new Object[]{SchemaSymbols.ELT_SEQUENCE};
                }
                reportSchemaError("SeqChoiceContentRestricted", args, child);
            }


            if (left == null) {
                left = particle;
            }
            else if (right == null) {
                right = particle;
            }
            else {
                if (fSchemaHandler.fDeclPool !=null) {
                    temp = fSchemaHandler.fDeclPool.getParticleDecl();
                } else {        
                    temp = new XSParticleDecl();
                }
                if (choice)
                    temp.fType = XSParticleDecl.PARTICLE_CHOICE;
                else
                    temp.fType = XSParticleDecl.PARTICLE_SEQUENCE;
                temp.fValue = left;
                temp.fOtherValue = right;
                left = temp;
                right = particle;
            }
        }

        // REVISIT: model group
        // Quick fix for the case that particles <choice> | <sequence> do not have any children.
        // For now we return null. In the future we might want to return model group decl.

        if (left != null) {
            if (fSchemaHandler.fDeclPool !=null) {
                temp = fSchemaHandler.fDeclPool.getParticleDecl();
            } else {        
                temp = new XSParticleDecl();
            }
            if (choice)
                temp.fType = XSParticleDecl.PARTICLE_CHOICE;
            else
                temp.fType = XSParticleDecl.PARTICLE_SEQUENCE;
            temp.fValue = left;
            temp.fOtherValue = right;
            left = temp;

            XInt minAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
            XInt maxAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];
            Long defaultVals = (Long)attrValues[XSAttributeChecker.ATTIDX_FROMDEFAULT];
            left.fMinOccurs = minAtt.intValue();
            left.fMaxOccurs = maxAtt.intValue();
            left = checkOccurrences(left,
                                    choice ? SchemaSymbols.ELT_CHOICE : SchemaSymbols.ELT_SEQUENCE,
                                    (Element)decl.getParentNode(),
                                    allContextFlags,
                                    defaultVals.longValue());
        }
        fAttrChecker.returnAttrArray(attrValues, schemaDoc);

        return left;
    }

    // Determines whether a content spec tree represents an "all" content model
    protected boolean hasAllContent(XSParticleDecl particle) {
        // If the content is not empty, is the top node ALL?
        if (particle != null) {

            // REVISIT: defered?
            // An ALL node could be optional, so we have to be prepared
            // to look one level below a ZERO_OR_ONE node for an ALL.
            //if (fParticle.type == XSParticleDecl.CONTENTSPECNODE_ZERO_OR_ONE) {
            //    fSchemaGrammar.getContentSpec(content.value, content);
            //}

            return(particle.fType == XSParticleDecl.PARTICLE_ALL);
        }

        return false;
    }

}
