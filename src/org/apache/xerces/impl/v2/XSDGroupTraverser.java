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
import org.apache.xerces.xni.QName;
import  org.w3c.dom.Element;
import  java.util.Stack;
import  java.util.Hashtable;

/**
 * The model group schema component traverser.
 * 
 * <group
 *   name = NCName>
 *   Content: (annotation?, (all | choice | sequence))
 * </group>
 * 
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Elena Litani, IBM
 * @version $Id$
 */
class  XSDGroupTraverser extends XSDAbstractParticleTraverser {

    private XSParticleDecl fParticle = new XSParticleDecl();
    XSDGroupTraverser (XSDHandler handler,
                       XMLErrorReporter errorReporter,
                       XSAttributeChecker gAttrCheck) {

        super(handler, errorReporter, gAttrCheck);
    }

    int traverseLocal(Element elmNode,
                      XSDocumentInfo schemaDoc,
                      SchemaGrammar grammar) {

        // General Attribute Checking for elmNode declared locally
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, false, schemaDoc.fNamespaceSupport);
        int elemIdx = traverseGroupDecl(elmNode, attrValues, schemaDoc, grammar, true);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return elemIdx;
    }

    int traverseGlobal(Element elmNode,
                       XSDocumentInfo schemaDoc,
                       SchemaGrammar grammar) {

        // General Attribute Checking for elmNode declared globally
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, true, schemaDoc.fNamespaceSupport);
        int groupIdx = traverseGroupDecl(elmNode, attrValues, schemaDoc, grammar, true);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return groupIdx;
    }

    /**
     * Traverse the group element.
     *
     * @param  elmNode
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @param  isGlobal
     * @return the element declaration index
     */
    int traverseGroupDecl(Element elmNode,
                          Object[] attrValues,
                          XSDocumentInfo schemaDoc,
                          SchemaGrammar grammar,
                          boolean isGlobal) {

        String  l_strNameAttr   = (String)  attrValues[XSAttributeChecker.ATTIDX_NAME];
        QName   refAttr     = (QName)  attrValues[XSAttributeChecker.ATTIDX_REF];
        Integer l_nMinAttr  = (Integer) attrValues[XSAttributeChecker.ATTIDX_MINOCCURS];
        Integer l_nMaxAttr  = (Integer) attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS];

        int index = -1;

        // get targetNamespace
        String l_strNamespace = XSDHandler.EMPTY_STRING;
        if (isGlobal) {
            l_strNamespace = schemaDoc.fTargetNamespace;
        }
        else if (schemaDoc.fAreLocalElementsQualified) {
            l_strNamespace = schemaDoc.fTargetNamespace;
        }

        // check for the constraints on the group element.
        // REVISIT: when parent of group is <redifine>
        if (isGlobal) {
            // must have a name
            if (l_strNameAttr.length() == 0) {
                reportGenericSchemaError("Global group declaration must have a name.");
            }
            if (refAttr != null) {
                reportGenericSchemaError ( "A group with \"ref\" present must not have <schema> or <redefine> as its parent");
            }
            // must have at least one child
            Element l_elmChild = DOMUtil.getFirstChildElement(elmNode);
            if (l_elmChild == null) {
                reportGenericSchemaError("Global group declaration must have a child.");
            }
            else {

                String childName = l_elmChild.getLocalName();
                if (l_elmChild.equals(SchemaSymbols.ELT_ANNOTATION)) {
                    traverseAnnotationDecl(l_elmChild, attrValues, isGlobal, schemaDoc);
                    l_elmChild = DOMUtil.getNextSiblingElement(l_elmChild);
                }

                if (l_elmChild == null) {
                    reportGenericSchemaError("Global group element must have a child <all>, <choice> or <sequence>.");
                }
                else if (childName.equals(SchemaSymbols.ELT_ALL)) {
                    index = traverseAll(l_elmChild, schemaDoc, grammar);
                    index = handleOccurrences(index, l_elmChild, CHILD_OF_GROUP, grammar);
                }
                else if (childName.equals(SchemaSymbols.ELT_CHOICE)) {
                    index = traverseChoice(l_elmChild, schemaDoc, grammar);
                    index = handleOccurrences(index, l_elmChild, CHILD_OF_GROUP, grammar);
                }
                else if (childName.equals(SchemaSymbols.ELT_SEQUENCE)) {
                    index = traverseSequence(l_elmChild, schemaDoc, grammar);
                    index = handleOccurrences(index, l_elmChild, CHILD_OF_GROUP, grammar);
                }
                else {
                    Object[] args = new Object [] { "group", childName};
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "GroupContentRestricted",
                                               args,
                                               XMLErrorReporter.SEVERITY_ERROR);
                }

                if (DOMUtil.getNextSiblingElement(l_elmChild) != null) {
                    Object[] args = new Object [] { "group", childName};
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "GroupContentRestricted",
                                               args,
                                               XMLErrorReporter.SEVERITY_ERROR);
                }

                // add global group declaration to the grammar
                grammar.fGlobalGroupDecls.put(l_strNameAttr, index);

            } //if
        }
        else {
            // local declaration. Corresponds to a Particle component.

            // ref should be here.
            if (refAttr == null) {
                reportGenericSchemaError("Local group declaration should have ref.");
                return index;
            }
            // can't have name attribute in the local group decl
            if (l_strNameAttr.length() !=0) {
                reportGenericSchemaError ( "group " + l_strNameAttr + " cannot refer to another group, but it refers to " + refAttr.localpart);
            }

            // get global decl
            // index is a particle index.
            index = fSchemaHandler.getGlobalDecl(schemaDoc, XSDHandler.GROUP_TYPE, refAttr);

            // no children are allowed
            if (DOMUtil.getFirstChildElement(elmNode) != null) {
                reportGenericSchemaError("Local group declaration cannot have a child.");
            }

            // either circular definition (detected by schema handler) or non existing 
            // group decl
            //
            if (index < 0) {
                if (index == XSDHandler.I_NOT_FOUND) {
                    reportGenericSchemaError("Reference made to non-existent group element");
                }
                return index;
            }

            int minOccurs = l_nMinAttr.intValue();
            int maxOccurs = l_nMaxAttr.intValue();

            // empty particle
            if (minOccurs == 0 && maxOccurs == 0) {
                return XSDHandler.I_EMPTY_PARTICLE;
            }

            if (!( minOccurs == 1 && maxOccurs == 1)) {
                // if minOccurs==maxOccurs==1 we don't need to create new particle
                // create new particle in the grammar if minOccurs<maxOccurs
                short type = grammar.getParticleType(index);               
                fParticle.clear();
                fParticle.type = type;
                fParticle.uri = schemaDoc.fTargetNamespace;
                fParticle.value = index;
                index = grammar.addParticleDecl(fParticle);
                grammar.setParticleMinMax(index, minOccurs, maxOccurs);
            }
        } // if

        return index;

    } // traverseGroupDecl()

}
