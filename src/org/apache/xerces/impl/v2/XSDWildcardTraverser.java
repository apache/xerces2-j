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
import java.util.StringTokenizer;

/**
 * The wildcard schema component traverser.
 *
 * <any
 *   id = ID
 *   maxOccurs = (nonNegativeInteger | unbounded)  : 1
 *   minOccurs = nonNegativeInteger : 1
 *   namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )  : ##any
 *   processContents = (lax | skip | strict) : strict
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?)
 * </any>
 *
 * <anyAttribute
 *   id = ID
 *   namespace = ((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )  : ##any
 *   processContents = (lax | skip | strict) : strict
 *   {any attributes with non-schema namespace . . .}>
 *   Content: (annotation?)
 * </anyAttribute>
 *
 * @author Rahul Srivastava, Sun Microsystems Inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
class XSDWildcardTraverser extends XSDAbstractTraverser {

    /**
     * constructor
     *
     * @param  handler
     * @param  errorReporter
     * @param  gAttrCheck
     */
    XSDWildcardTraverser (XSDHandler handler,
                          XSAttributeChecker gAttrCheck) {
        super(handler, gAttrCheck);
    }


    /**
     * Traverse <any>
     *
     * @param  elmNode
     * @param  schemaDoc
     * @param  grammar
     * @return the wildcard node index
     */
    XSParticleDecl traverseAny(Element elmNode,
                               XSDocumentInfo schemaDoc,
                               SchemaGrammar grammar) {

        // General Attribute Checking for elmNode
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, false, schemaDoc.fNamespaceSupport);
        XSWildcardDecl wildcard = traverseWildcardDecl(elmNode, attrValues, schemaDoc, grammar);
        XSParticleDecl particle = null;
        if (wildcard != null) {
            int min = ((XInt)attrValues[XSAttributeChecker.ATTIDX_MINOCCURS]).intValue();
            int max = ((XInt)attrValues[XSAttributeChecker.ATTIDX_MAXOCCURS]).intValue();
            if (max != 0) {
                particle = new XSParticleDecl();
                particle.fType = XSParticleDecl.PARTICLE_WILDCARD;
                particle.fValue = wildcard;
                particle.fMinOccurs = min;
                particle.fMaxOccurs = max;
            }
        }

        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return particle;
    }


    /**
     * Traverse <anyAttribute>
     *
     * @param  elmNode
     * @param  schemaDoc
     * @param  grammar
     * @return the wildcard node index
     */
    XSWildcardDecl traverseAnyAttribute(Element elmNode,
                                        XSDocumentInfo schemaDoc,
                                        SchemaGrammar grammar) {

        // General Attribute Checking for elmNode
        Object[] attrValues = fAttrChecker.checkAttributes(elmNode, false, schemaDoc.fNamespaceSupport);
        XSWildcardDecl wildcard = traverseWildcardDecl(elmNode, attrValues, schemaDoc, grammar);
        fAttrChecker.returnAttrArray(attrValues, schemaDoc.fNamespaceSupport);

        return wildcard;
    }


    /**
     *
     * @param  elmNode
     * @param  attrValues
     * @param  schemaDoc
     * @param  grammar
     * @return the wildcard node index
     */
     XSWildcardDecl traverseWildcardDecl(Element elmNode,
                                         Object[] attrValues,
                                         XSDocumentInfo schemaDoc,
                                         SchemaGrammar grammar) {

        XSWildcardDecl wildcard = new XSWildcardDecl();

        //get all attributes
        String namespaceAttr       = (String) attrValues[XSAttributeChecker.ATTIDX_NAMESPACE];
        XInt   processContentsAttr = (XInt) attrValues[XSAttributeChecker.ATTIDX_PROCESSCONTENTS];

        wildcard.fProcessContents = processContentsAttr.shortValue();

        wildcard.fType = XSWildcardDecl.WILDCARD_ANY;
        if (namespaceAttr.equals(SchemaSymbols.ATTVAL_TWOPOUNDANY)) {
            wildcard.fType = XSWildcardDecl.WILDCARD_ANY;
        }
        else if (namespaceAttr.equals(SchemaSymbols.ATTVAL_TWOPOUNDOTHER)) {
            wildcard.fType = XSWildcardDecl.WILDCARD_OTHER;
            if (schemaDoc.fTargetNamespace.length() == 0) {
                wildcard.fNamespaceList = new String[1];
                wildcard.fNamespaceList[0] = schemaDoc.fTargetNamespace;
            } else {
                wildcard.fNamespaceList = new String[2];
                wildcard.fNamespaceList[0] = schemaDoc.fTargetNamespace;
                wildcard.fNamespaceList[1] = fSchemaHandler.EMPTY_STRING;
            }
        }
        else {
            // REVISIT: namespace should be return in String[] or Vector,
            //          then we don't need to tokenize them again

            //namespace = "##local" OR
            //namespace = "##targetNamespace" OR
            //namespace = "anyURI" OR
            //namespace = "anyURI ##local" OR
            //namespace = "anyURI ##targetNamespace"

            StringTokenizer tokens = new StringTokenizer(namespaceAttr);
            String[] namespaceList = new String[tokens.countTokens()];
            int i = 0;
            String token;
            String tempNamespace;
            while (tokens.hasMoreTokens()) {
                token = tokens.nextToken();
                if (token.equals(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL)) {
                    tempNamespace = fSchemaHandler.EMPTY_STRING;
                }
                else if (token.equals(SchemaSymbols.ATTVAL_TWOPOUNDTARGETNS)) {
                    tempNamespace = schemaDoc.fTargetNamespace;
                }
                else {
                    // we have found namespace URI here
                    tempNamespace = token;
                }

                //check for duplicate namespaces in the list
                int j=0;
                for (; j<i; j++) {
                    if (tempNamespace.equals(namespaceList[j]))
                        break;
                }
                if (j == i) {
                    // this means traversed whole for loop
                    // i.e. not a duplicate namespace
                    namespaceList[i] = tempNamespace;
                    i++;
                }
            }
            if (i == namespaceList.length) {
                wildcard.fNamespaceList = namespaceList;
            } else {
                wildcard.fNamespaceList = new String[i];
                System.arraycopy(namespaceList, 0, wildcard.fNamespaceList, 0, i);
            }
        }

        //check content
        Element child = DOMUtil.getFirstChildElement(elmNode);
        if (child != null)
        {
            if (child.equals(SchemaSymbols.ELT_ANNOTATION)) {
                traverseAnnotationDecl(child, attrValues, false, schemaDoc);
                child = DOMUtil.getNextSiblingElement(child);
            }

            if (child != null) {
                Object[] args = new Object [] { "wildcard", child.getLocalName()};
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                                               "WildcardContentRestricted",
                                               args,
                                               XMLErrorReporter.SEVERITY_ERROR);
            }
        }

        return wildcard;

    } // traverseWildcardDecl

} // XSDWildcardTraverser
