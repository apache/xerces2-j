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

import  org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.DOMUtil;
import  org.w3c.dom.Element;

abstract class XSDAbstractParticleTraverser extends XSDAbstractTraverser {


    // Flags for handleOccurrences to indicate any special
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
                    SchemaGrammar grammar) {

        // General Attribute Checking

        Object[] attrValues = fAttrChecker.checkAttributes(allDecl, false, schemaDoc.fNamespaceSupport);

        Element content = DOMUtil.getFirstChildElement(allDecl);
        Element child = checkContent(allDecl, content, true);

        int left = -2;
        int right = -2;
        String childName = null;
        int particleIndex;
        for (; child != null; child = DOMUtil.getNextSiblingElement(child)) {

            particleIndex = -2;
            childName = child.getLocalName();

            // Only elements are allowed in <all>
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particleIndex = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar);                  
                particleIndex = handleOccurrences(particleIndex, child, PROCESSING_ALL_EL, grammar);
            }
            else {
                Object[] args = { childName};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           "AllContentRestricted",
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
            }

            if (left == -2) {
                left = particleIndex;
            }
            else if (right == -2) {
                right = particleIndex;
            }
            else {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ALL, left, right);
                right = particleIndex;
            }
        }

        if (right != -2) //|| fSchemaGrammar.getDeferContentSpecExpansion())
            left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_ALL, left, right);


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
                          SchemaGrammar grammar) {

        return traverseSeqChoice (seqDecl, schemaDoc, grammar, false);
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
                        SchemaGrammar grammar) {

        return traverseSeqChoice (choiceDecl, schemaDoc, grammar, true);
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
                                   boolean choice) {

        // General Attribute Checking
        Object[] attrValues = fAttrChecker.checkAttributes(decl, false, schemaDoc.fNamespaceSupport);

        Element content = DOMUtil.getFirstChildElement(decl);
        Element child = checkContent(decl,content, true);


        int left = -2;
        int right = -2;
        boolean hadContent = false;
        String childName = null;
        int particleIndex;
        boolean seeParticle;
        for (;child != null;child = DOMUtil.getNextSiblingElement(child)) {

            particleIndex = -2;
            seeParticle = false;

            childName = child.getLocalName();
            if (childName.equals(SchemaSymbols.ELT_ELEMENT)) {
                particleIndex = fSchemaHandler.fElementTraverser.traverseLocal(child, schemaDoc, grammar);                  
                seeParticle = true;

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

                seeParticle = true;

            }
            else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                particleIndex = traverseChoice( child,schemaDoc, grammar);
                seeParticle = true;

            }
            else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                particleIndex = traverseSequence(child,schemaDoc, grammar);
                seeParticle = true;

            }
            else if (childName.equals(SchemaSymbols.ELT_ANY)) {
                particleIndex = fSchemaHandler.fWildCardTraverser.traverseAny(child, schemaDoc, grammar);
                seeParticle = true;
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


            if (seeParticle) {
                particleIndex = handleOccurrences( particleIndex, child,  NOT_ALL_CONTEXT, grammar);
            }
            if (left == -2) {
                left = particleIndex;
            }
            else if (right == -2) {
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

        if (right != -2) //|| fSchemaGrammar.getDeferContentSpecExpansion())
            if (choice) {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_CHOICE, left, right);
            }
            else {
                left = grammar.addParticleDecl(XSParticleDecl.PARTICLE_SEQUENCE, left, right);
            }

        return left;    
    }




    // Helper functions:
    // Should include modified handleOccurrences* methods.
    //
    // Checks constraints for minOccurs, maxOccurs and expands content model
    // accordingly
    protected int handleOccurrences(int particleIndex, Element particle,
                                     int allContextFlags, SchemaGrammar grammar) {

        // if particleIndex is invalid, return
        if (particleIndex < 0)
            return particleIndex;

        String minOccurs =
        particle.getAttribute(SchemaSymbols.ATT_MINOCCURS).trim();
        String maxOccurs =
        particle.getAttribute(SchemaSymbols.ATT_MAXOCCURS).trim();
        boolean processingAllEl = ((allContextFlags & PROCESSING_ALL_EL) != 0);
        boolean processingAllGP = ((allContextFlags & PROCESSING_ALL_GP) != 0);
        boolean groupRefWithAll = ((allContextFlags & GROUP_REF_WITH_ALL) != 0);
        boolean isGroupChild    = ((allContextFlags & CHILD_OF_GROUP) != 0);

        // Neither minOccurs nor maxOccurs may be specified
        // for the child of a model group definition.
        if (isGroupChild &&
            (minOccurs.length() != 0 || maxOccurs.length() != 0)) {
            Element group = (Element)particle.getParentNode();
            Object[] args = new Object[]{group.getAttribute(SchemaSymbols.ATT_NAME),
                particle.getNodeName()};
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                       "MinMaxOnGroupChild",
                                       args,
                                       XMLErrorReporter.SEVERITY_ERROR);
            minOccurs = (maxOccurs = "1");
        }

        // If minOccurs=maxOccurs=0, no component is specified
        if (minOccurs.equals("0") && maxOccurs.equals("0")) {
            return -2;
        }

        int min=1, max=1;

        if (minOccurs.length() == 0) {
            minOccurs = "1";
        }
        if (maxOccurs.length() == 0) {
            maxOccurs = "1";
        }

        // For the elements referenced in an <all>, minOccurs attribute
        // must be zero or one, and maxOccurs attribute must be one.
        // For a complex type definition that contains an <all> or a
        // reference a <group> whose model group is an all model group,
        // minOccurs and maxOccurs must be one.
        if (processingAllEl || groupRefWithAll || processingAllGP) {
            String errorMsg;
            if ((processingAllGP||groupRefWithAll||!minOccurs.equals("0")) &&
                !minOccurs.equals("1")) {


                if (processingAllEl) {
                    errorMsg = "BadMinMaxForAllElem";
                }
                else if (processingAllGP) {
                    errorMsg = "BadMinMaxForAllGp";
                }
                else {
                    errorMsg = "BadMinMaxForGroupWithAll";
                }
                Object[] args = new Object [] {"minOccurs", minOccurs};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           errorMsg,
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
                minOccurs = "1";
            }

            if (!maxOccurs.equals("1")) {

                if (processingAllEl) {
                    errorMsg = "BadMinMaxForAllElem";
                }
                else if (processingAllGP) {
                    errorMsg = "BadMinMaxForAllGp";
                }
                else {
                    errorMsg = "BadMinMaxForGroupWithAll";
                }

                Object[] args = new Object [] {"maxOccurs", maxOccurs};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           errorMsg,
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
                maxOccurs = "1";
            }
        }

        try {
            min = Integer.parseInt(minOccurs);
        }
        catch (Exception e) {
            Object[] args = new Object [] { "illegal value for minOccurs or maxOccurs : '" +e.getMessage()+ "' "};
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                       "GenericError",
                                       args,
                                       XMLErrorReporter.SEVERITY_ERROR);
        }

        if (maxOccurs.equals("unbounded")) {
            max = SchemaSymbols.OCCURRENCE_UNBOUNDED;
        }
        else {
            try {
                max = Integer.parseInt(maxOccurs);
            }
            catch (Exception e) {

                Object[] args = new Object [] { "illegal value for minOccurs or maxOccurs : '" +e.getMessage()+ "' "};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                           "GenericError",
                                           args,
                                           XMLErrorReporter.SEVERITY_ERROR);
            }

            // Check that minOccurs isn't greater than maxOccurs.
            // p-props-correct 2.1
            if (min > max) {
                reportGenericSchemaError("p-props-correct:2.1 Value of minOccurs '" + minOccurs + "' must not be greater than value of maxOccurs '" + maxOccurs +"'");
            }

            if (max < 1) {
                reportGenericSchemaError("p-props-correct:2.2 Value of maxOccurs " + maxOccurs + " is invalid.  It must be greater than or equal to 1");
            }
        }
        //REVISIT: assume we alwasy defer particle expantion.
        grammar.setParticleMinMax(particleIndex, min, max);
        return particleIndex;

    }

    // Determines whether a content spec tree represents an "all" content model
    protected boolean hasAllContent(int particleIndex, SchemaGrammar grammar) {
        // If the content is not empty, is the top node ALL?
        if (particleIndex > -1) {

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
