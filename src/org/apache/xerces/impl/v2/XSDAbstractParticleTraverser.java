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

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.XInt;
import org.w3c.dom.Element;

/**
 * @author Elena Litani, IBM
 * @author Sandy Gao, IBM
 * @version $Id$
 */
abstract class XSDAbstractParticleTraverser extends XSDAbstractTraverser {

    private XSParticleDecl fParticle = new XSParticleDecl();

    XSDAbstractParticleTraverser (XSDHandler handler,
                                  XMLErrorReporter errorReporter,
                                  XSAttributeChecker gAttrCheck) {

        super(handler, errorReporter, gAttrCheck);
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
    int traverseAll(Element allDecl,
                    XSDocumentInfo schemaDoc,
                    SchemaGrammar grammar,
                    int allContextFlags) {

        // General Attribute Checking

        Object[] attrValues = fAttrChecker.checkAttributes(allDecl, false, schemaDoc.fNamespaceSupport);

        Element content = DOMUtil.getFirstChildElement(allDecl);
        Element child = checkContent(allDecl, content, true);

        int left = SchemaGrammar.I_EMPTY_DECL;
        int right = SchemaGrammar.I_EMPTY_DECL;
        String childName = null;
        int particleIndex;
        for (; child != null; child = DOMUtil.getNextSiblingElement(child)) {

            particleIndex = SchemaGrammar.I_EMPTY_DECL;
            childName = child.getLocalName();

            // Only elements are allowed in <all>
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particleIndex = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar, PROCESSING_ALL_EL);
            }
            else {
                Object[] args = { childName};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           "AllContentRestricted",
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
            }

            if (left == SchemaGrammar.I_EMPTY_DECL) {
                left = particleIndex;
            }
            else if (right == SchemaGrammar.I_EMPTY_DECL) {
                right = particleIndex;
            }
            else {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ALL, left, right);
                right = particleIndex;
            }
        }

        if (right != SchemaGrammar.I_EMPTY_DECL)
            left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ALL, left, right);

        XInt minAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt maxAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];
        XInt defaultVals = (XInt)attrValues[XSAttributeChecker.ATTIDX_FROMDEFAULT];
        grammar.setParticleMinMax(left, minAtt.intValue(), maxAtt.intValue());

        fParticle = checkOccurrences(fParticle,
                                     SchemaSymbols.ELT_ALL,
                                     (Element)allDecl.getParentNode(),
                                     allContextFlags,
                                     defaultVals.intValue());

        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

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
    int traverseSequence (Element seqDecl,
                          XSDocumentInfo schemaDoc,
                          SchemaGrammar grammar,
                          int allContextFlags) {

        return traverseSeqChoice (seqDecl, schemaDoc, grammar, allContextFlags, false);
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
    int traverseChoice (Element choiceDecl,
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
    private int traverseSeqChoice (Element decl, 
                                   XSDocumentInfo schemaDoc, 
                                   SchemaGrammar grammar,
                                   int allContextFlags,
                                   boolean choice) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(decl, false, schemaDoc.fNamespaceSupport);

        Element content = DOMUtil.getFirstChildElement(decl);
        Element child = checkContent(decl,content, true);


        int left = SchemaGrammar.I_EMPTY_DECL;
        int right = SchemaGrammar.I_EMPTY_DECL;
        boolean hadContent = false;
        String childName = null;
        int particleIndex;
        for (;child != null;child = DOMUtil.getNextSiblingElement(child)) {

            particleIndex = SchemaGrammar.I_EMPTY_DECL;

            childName = child.getLocalName();
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particleIndex = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar, NOT_ALL_CONTEXT);

            }
            else if (childName.equals(SchemaSymbols.ELT_GROUP)) {

                particleIndex = fSchemaHandler.fGroupTraverser.traverseLocal(child, schemaDoc, grammar);

                // A content type of all can only appear
                // as the content type of a complex type definition.
                if (hasAllContent(particleIndex, grammar)) {
                    Object [] args;
                    if (choice) {
                        args = new Object[]{"choice"};
                    }
                    else {
                        args = new Object[]{"sequence"};
                    }
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "AllContentLimited",
                                               args,
                                               XMLErrorReporter.SEVERITY_ERROR);
                }

            }
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particleIndex = traverseChoice( child,schemaDoc, grammar, NOT_ALL_CONTEXT);
            }
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particleIndex = traverseSequence(child,schemaDoc, grammar, NOT_ALL_CONTEXT);
            }
            else if (childName.equals(SchemaSymbols.ELT_ANY)) {
                particleIndex = fSchemaHandler.fWildCardTraverser.traverseAny(child, schemaDoc, grammar);
            }
            else {
                Object [] args;
                if (choice) {
                    args = new Object[]{"choice"};
                }
                else {
                    args = new Object[]{"sequence"};
                }
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           "SeqChoiceContentRestricted",
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);                
            }


            if (left == SchemaGrammar.I_EMPTY_DECL) {
                left = particleIndex;
            }
            else if (right == SchemaGrammar.I_EMPTY_DECL) {
                right = particleIndex;
            }
            else {
                if (choice) {
                    left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_CHOICE ,left,right);
                }
                else {
                    left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE ,left,right);
                }
                right = particleIndex;
            }
        }

        if (right != SchemaGrammar.I_EMPTY_DECL) //|| fSchemaGrammar.getDeferContentSpecExpansion())
            if (choice) {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_CHOICE, left, right);
            }
            else {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE, left, right);
            }

        XInt minAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
        XInt maxAtt = (XInt)attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];
        XInt defaultVals = (XInt)attrValues[XSAttributeChecker.ATTIDX_FROMDEFAULT];
        grammar.setParticleMinMax(left, minAtt.intValue(), maxAtt.intValue());

        fParticle = checkOccurrences(fParticle,
                                     choice ? SchemaSymbols.ELT_CHOICE : SchemaSymbols.ELT_SEQUENCE,
                                     (Element)decl.getParentNode(),
                                     allContextFlags,
                                     defaultVals.intValue());

        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return left;    
    }

    // Determines whether a content spec tree represents an "all" content model
    protected boolean hasAllContent(int particleIndex, SchemaGrammar grammar) {
        // If the content is not empty, is the top node ALL?
        if (particleIndex != SchemaGrammar.I_EMPTY_DECL) {

            fParticle = grammar.getParticleDecl(particleIndex, fParticle);

            // REVISIT: defered?
            // An ALL node could be optional, so we have to be prepared
            // to look one level below a ZERO_OR_ONE node for an ALL.
            //if (fParticle.type == XSParticleDecl.CONTENTSPECNODE_ZERO_OR_ONE) {
            //    fSchemaGrammar.getContentSpec(content.value, content);
            //}

            return(fParticle.type == XSParticleDecl.PARTICLE_ALL);
        }

        return false;
    }

}
