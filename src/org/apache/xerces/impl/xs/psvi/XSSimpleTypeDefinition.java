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

import java.util.Enumeration;

/**
 * 4.1.1 The Simple Type Definition Schema Component (XML Schema Part 2).
 *
 * @author Elena Litani, IBM
 * @version $Id$
 */
public interface XSSimpleTypeDefinition extends XSTypeDefinition {

    /**
     * <code>XSSimpleType</code> variety
     */
    public static final short VARIETY_ABSENT        = 0;
    /**
     * <code>XSSimpleType</code> variety
     */
    public static final short VARIETY_ATOMIC        = 1;
    /**
     * <code>XSSimpleType</code> variety
     */
    public static final short VARIETY_LIST          = 2;
    /**
     * <code>XSSimpleType</code> variety
     */
    public static final short VARIETY_UNION         = 3;

    /**
     * constants defined for the 'ordered' fundamental facet.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#rf-fund-facets'> XML
     * Schema Part 2: Datatypes </a>
     */
    /** not ordered */
    public static final short ORDERED_FALSE         = 0;
    /** partically ordered */
    public static final short ORDERED_PARTIAL       = 1;
    /** totally ordered */
    public static final short ORDERED_TOTAL         = 2;

    // Facets
    /**
     * none
     */
    public static final short FACET_NONE            = 0;
    /**
     * 4.3.1 Length
     */
    public static final short FACET_LENGTH          = 1<<1;
    /**
     * 4.3.2 minLength.
     */
    public static final short FACET_MINLENGTH       = 1<<2;
    /**
     * 4.3.3 maxLength.
     */
    public static final short FACET_MAXLENGTH       = 1<<3;
    /**
     * 4.3.4 pattern.
     */
    public static final short FACET_PATTERN         = 1<<4;
    /**
     * 4.3.5 enumeration.
     */
    public static final short FACET_ENUMERATION     = 1<<5;
    /**
     * 4.3.6 whitespace.
     */
    public static final short FACET_WHITESPACE      = 1<<6;
    /**
     * 4.3.7 maxInclusive.
     */
    public static final short FACET_MAXINCLUSIVE    = 1<<7;
    /**
     * 4.3.9 maxExclusive.
     */
    public static final short FACET_MAXEXCLUSIVE    = 1<<8;
    /**
     * 4.3.9 minExclusive.
     */
    public static final short FACET_MINEXCLUSIVE    = 1<<9;
    /**
     * 4.3.10 minInclusive.
     */
    public static final short FACET_MININCLUSIVE    = 1<<10;
    /**
     * 4.3.11 totalDigits .
     */
    public static final short FACET_TOTALDIGITS     = 1<<11;
    /**
     * 4.3.12 fractionDigits.
     */
    public static final short FACET_FRACTIONDIGITS  = 1<<12;

    /**
     * {Facets} Check whether a facet is defined on this type.
     * @param facetName The name of the facet.
     * @return          true if the facet is defined; false othereise.
     */
    public boolean getIsDefinedFacet(short facetName);

    /**
     * {Facets} Get all facets defined on this type.
     * @return  bit combination of FACET_XXX constants of all defined facets.
     */
    public short getDefinedFacets();

    /**
     * {Facets} Check whether a facet is defined and fixed on this type.
     * @param facetName The name of the facet.
     * @return          true if the facet is defined and fixed; false othereise.
     */
    public boolean getIsFixedFacet(short facetName);

    /**
     * {Facets} Get all facets defined and fixed on this type.
     * @return  bit combination of FACET_XXX constants of all fixed facets.
     */
    public short getFixedFacets();

    /**
     * Convenience method. Returns a value of a single constraining facet for
     * this simple type definition. This method must not be used to retrieve
     * values for <code>enumeration</code> and <code>pattern</code> facets.
     * @param facetName The name of the facet, i.e.
     *   <code>FACET_LENGTH, FACET_TOTALDIGITS </code> (see
     *   <code>XSConstants</code>).To retrieve value for pattern or
     *   enumeration, see <code>enumeration</code> and <code>pattern</code>.
     * @return A value of the facet specified in <code>facetName</code> for
     *   this simple type definition or <code>null</code>.
     */
    public String getLexicalFacetValue(short facetName);

    /**
     * Returns a list of enumeration values, as <code>String</code>'s.
     */
    public Enumeration getLexicalEnumerations();

    /**
     * Returns a list of pattern values, as <code>String</code>'s.
     */
    public Enumeration getLexicalPatterns();

    /**
     * Fundamental Facet: [Definition:] An order relation on a value space is
     * a mathematical relation that imposes a total order or a partial order
     * on the members of the value space.
     */
    public short getOrdered();

    /**
     * Fundamental Facet: [Definition:] Every value space has associated with
     * it the concept of cardinality. Some value spaces are finite, some are
     * countably infinite while still others could conceivably be
     * uncountably infinite (although no value space defined by this
     * specification is uncountable infinite). A datatype is said to have
     * the cardinality of its value space.
     */
    public boolean getIsFinite();

    /**
     * Fundamental Facet: [Definition:] A datatype is bounded if its value
     * space has either an inclusive upper bound or an exclusive upper bound
     * and either an inclusive lower bound and an exclusive lower bound.
     * Should bounded be of type boolean? Should this facet provide more
     * information (discontinuous)?
     */
    public boolean getIsBounded();

    /**
     * Fundamental Facet: [Definition:] A datatype is said to be numeric if
     * its values are conceptually quantities (in some mathematical number
     * system). [Definition:] A datatype whose values are not numeric is
     * said to be non-numeric.
     */
    public boolean getIsNumeric();

    /**
     * {variety} One of {atomic, list, union}. The valid constant values
     * defined in <code>XSConstants</code> are <code>UNION</code>,
     * <code>LIST</code>, <code>ATOMIC</code>.
     */
    public short getVariety();

    /**
     * If variety is <code>atomic</code> the primitive type definition (a
     * built-in primitive datatype definition or the simple ur-type
     * definition) is available, otherwise <code>null</code>.
     */
    public XSSimpleTypeDefinition getPrimitiveType();

    /**
     * If variety is <code>list</code> the item type definition (an atomic or
     * union simple type definition) is available, otherwise
     * <code>null</code>.
     */
    public XSSimpleTypeDefinition getItemType();

    /**
     * If variety is <code>union</code> the list of member type definitions (a
     * non-empty sequence of simple type definitions) is available,
     * otherwise <code>null</code>.
     */
    public XSObjectList getMemberTypes();

    /**
     * Optional. Annotation.
     */
    public XSAnnotation getAnnotation();

}
