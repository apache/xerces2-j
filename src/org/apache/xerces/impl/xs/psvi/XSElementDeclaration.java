/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2002 The Apache Software Foundation.
 * All rights reserved.
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
 * originally based on software copyright (c) 2002, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.xs.psvi;

/**
 * 3.3.1 The Element Declaration Schema Component.
 *
 * @author Elena Litani, IBM
 * @version $Id$
 */
public interface XSElementDeclaration extends XSTerm {

    /**
     * Either a simple type definition or a complex type definition.
     */
    public XSTypeDefinition getTypeDefinition();

    /**
     * Optional. Either global or a complex type definition (
     * <code>ctDefinition</code>). This property is absent in the case of
     * declarations within named model groups: their scope will be
     * determined when they are used in the construction of complex type
     * definitions.
     */
    public short getScope();

    /**
     * Locally scoped declarations are available for use only within the
     * complex type definition identified by the <code>scope</code>
     * property.
     */
    public XSComplexTypeDefinition getEnclosingCTDefinition();

    /**
     * A value constraint: one of default, fixed.
     */
    public short getConstraintType();

    /**
     * A value constraint: The actual value (with respect to the {type
     * definition})
     */
    public String getConstraintValue();

    /**
     * If {nillable} is true, then an element may also be valid if it carries
     * the namespace qualified attribute with [local name] nil from
     * namespace http://www.w3.org/2001/XMLSchema-instance and value true
     * (see xsi:nil (2.6.2)) even if it has no text or element content
     * despite a {content type} which would otherwise require content.
     */
    public boolean getIsNillable();

    /**
     * {identity-constraint definitions} A set of constraint definitions.
     */
    public XSNamedMap getIdentityConstraints();

    /**
     * {substitution group affiliation} Optional. A top-level element
     * definition.
     */
    public XSElementDeclaration getSubstitutionGroupAffiliation();

    /**
     * Convenience method. Check if <code>exclusion</code> is a substitution
     * group exclusion for this element declaration.
     * @param exclusion Extension, restriction or none. Represents final
     *   set for the element.
     * @return True if <code>exclusion</code> is a part of the substitution
     *   group exclusion subset.
     */
    public boolean getIsSubstitutionGroupExclusion(short exclusion);

    /**
     * Specifies if this declaration can be nominated as
     * the {substitution group affiliation} of other
     * element declarations having the same {type definition}
     * or types derived therefrom.
     *
     * @return A bit flag representing {extension, restriction} or NONE.
     */
    public short getSubstitutionGroupExclusions();

    /**
     * Convenience method. Check if <code>disallowed</code> is a disallowed
     * substitution for this element declaration.
     * @param disallowed Substitution, extension, restriction or none.
     *   Represents a block set for the element.
     * @return True if <code>disallowed</code> is a part of the substitution
     *   group exclusion subset.
     */
    public boolean getIsDisallowedSubstition(short disallowed);

    /**
     * The supplied values for {disallowed substitutions}
     *
     * @return A bit flag representing {substitution, extension, restriction} or NONE.
     */
    public short getDisallowedSubstitutions();

    /**
     * {abstract} A boolean.
     */
    public boolean getIsAbstract();

    /**
     * Optional. Annotation.
     */
    public XSAnnotation getAnnotation();

}
