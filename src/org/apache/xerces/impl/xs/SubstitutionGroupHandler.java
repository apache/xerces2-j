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

package org.apache.xerces.impl.xs;

import org.apache.xerces.xni.QName;

/**
 * To store and validate information about substitutionGroup
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class SubstitutionGroupHandler {

    // grammar resolver
    XSGrammarResolver fGrammarResolver;

    /**
     * Default constructor
     */
    public SubstitutionGroupHandler(XSGrammarResolver grammarResolver) {
        fGrammarResolver = grammarResolver;
    }

    /**
     * clear the internal registry of substitutionGroup information
     */
    public void reset() {
        // REVISIT: to implement
    }

    /**
     * add one substitution group pair
     */
    public void addSubstitutionGroup(XSElementDecl element) {
        // REVISIT: to implement
    }

    /**
     * get all elements that can substitute the given element,
     * according to the spec, we shouldn't consider the {block} constraints.
     */
    public XSElementDecl[] getSubstitutionGroup(String elementUri, String elementName) {
        // REVISIT: to implement
        return null;
    }

    // 3.9.4 Element Sequence Locally Valid (Particle) 2.3.3
    // check whether one element decl matches an element with the given qname
    public XSElementDecl getMatchingElemDecl(QName element, XSElementDecl exemplar) {
        if (element.localpart == exemplar.fName &&
            element.uri == exemplar.fTargetNamespace) {
            return exemplar;
        }

        // if the exemplar is not a global element decl, then it's not possible
        // to be substituted by another element.
        if (!exemplar.isGlobal())
            return null;

        // if the decl blocks substitution, return false
        if ((exemplar.fBlock & SchemaSymbols.SUBSTITUTION) != 0)
            return null;

        // get grammar of the element
        SchemaGrammar sGrammar = fGrammarResolver.getGrammar(element.uri);
        if (sGrammar == null)
            return null;

        // get the decl for the element
        XSElementDecl eDecl = sGrammar.getGlobalElementDecl(element.localpart);
        if (eDecl == null)
            return null;

        // and check by using substitutionGroup information
        if (substitutionGroupOK(eDecl, exemplar, exemplar.fBlock))
            return eDecl;

        return null;
    }

    // 3.3.6 Substitution Group OK (Transitive)
    // check whether element can substitute exemplar
    protected boolean substitutionGroupOK(XSElementDecl element, XSElementDecl exemplar, short blockingConstraint) {
        // For an element declaration (call it D) together with a blocking constraint (a subset of {substitution, extension, restriction}, the value of a {disallowed substitutions}) to be validly substitutable for another element declaration (call it C) all of the following must be true:
        // 1 The blocking constraint does not contain substitution.
        if ((blockingConstraint & SchemaSymbols.SUBSTITUTION) != 0)
            return false;

        // prepare the combination of {derivation method} and
        // {disallowed substitution}
        short devMethod = 0, blockConstraint = blockingConstraint;

        // initialize the derivation method to be that of the type of D
        XSTypeDecl type = element.fType;
        if (type.getXSType() == XSTypeDecl.COMPLEX_TYPE)
            devMethod = ((XSComplexTypeDecl)type).fDerivedBy;
        else
            devMethod = SchemaSymbols.RESTRICTION;

        // initialize disallowed substitution to the passed in blocking constraint
        type = exemplar.fType;
        if (type.getXSType() == XSTypeDecl.COMPLEX_TYPE)
            blockConstraint |= ((XSComplexTypeDecl)type).fBlock;

        // 2 There is a chain of {substitution group affiliation}s from D to C, that is, either D's {substitution group affiliation} is C, or D's {substitution group affiliation}'s {substitution group affiliation} is C, or . . .
        XSElementDecl subGroup = element.fSubGroup;
        while (subGroup != null && subGroup != exemplar) {
            // add the derivation method and disallowed substitution info
            // of the current type to the corresponding variables
            type = subGroup.fType;
            if (type.getXSType() == XSTypeDecl.COMPLEX_TYPE) {
                devMethod |= ((XSComplexTypeDecl)type).fDerivedBy;
                blockConstraint |= ((XSComplexTypeDecl)type).fBlock;
            } else {
                devMethod |= SchemaSymbols.RESTRICTION;
            }
            subGroup = subGroup.fSubGroup;
        }

        if (subGroup == null)
            return false;

        // 3 The set of all {derivation method}s involved in the derivation of D's {type definition} from C's {type definition} does not intersect with the union of the blocking constraint, C's {prohibited substitutions} (if C is complex, otherwise the empty set) and the {prohibited substitutions} (respectively the empty set) of any intermediate {type definition}s in the derivation of D's {type definition} from C's {type definition}.
        if ((devMethod & blockConstraint) != 0)
            return false;

        return true;
    }

    // check whether element is in exemplar's substitution group
    public boolean inSubstitutionGroup(XSElementDecl element, XSElementDecl exemplar) {
        // [Definition:]  Every element declaration in the {element declarations} of a schema defines a substitution group, a subset of those {element declarations}, as follows:
        // 1 The element declaration itself is in the group;
        // 2 The group is closed with respect to {substitution group affiliation}, that is, if any element declaration in the {element declarations} has a {substitution group affiliation} in the group, then it is also in the group itself.
        while (element != null && element != exemplar) {
            element = element.fSubGroup;
        }
        return (element != null);
    }

} // class SubstitutionGroupHandler
