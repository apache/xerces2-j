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
 * originally based on software copyright (c) 1999, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.impl.dv;

import org.apache.xerces.impl.xs.XSTypeDecl;

/**
 * Any simple type would implement this interface. It inherits from both
 * XSTypeDecl (for schema) and DatatypeValidator (for the validate method).
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public interface XSSimpleType extends XSTypeDecl, DatatypeValidator {

    /**
     * The bit combination of the following constants are used to tell
     * which facets are fixed, and which ones are present
     */
    public static final short FACET_LENGTH         = 1<<0;
    public static final short FACET_MINLENGTH      = 1<<1;
    public static final short FACET_MAXLENGTH      = 1<<2;
    public static final short FACET_PATTERN        = 1<<3;
    public static final short FACET_ENUMERATION    = 1<<4;
    public static final short FACET_WHITESPACE     = 1<<5;
    public static final short FACET_MAXINCLUSIVE   = 1<<6;
    public static final short FACET_MAXEXCLUSIVE   = 1<<7;
    public static final short FACET_MINEXCLUSIVE   = 1<<8;
    public static final short FACET_MININCLUSIVE   = 1<<9;
    public static final short FACET_TOTALDIGITS    = 1<<10;
    public static final short FACET_FRACTIONDIGITS = 1<<11;

    /**
     * Variety constants
     */
    public static final short VARIETY_ATOMIC    = 1;
    public static final short VARIETY_LIST      = 2;
    public static final short VARIETY_UNION     = 3;

    /**
     * whiteSpace constants
     */
    public static final short WS_PRESERVE = 0;
    public static final short WS_REPLACE  = 1;
    public static final short WS_COLLAPSE = 2;

    /**
     * order constants
     */
    public static final short ORDERED_FALSE     = 1;
    public static final short ORDERED_PARTIAL   = 2;
    public static final short ORDERED_TOTAL     = 3;

    /**
     * cardinality constants
     */
    public static final short CARDINALITY_FINITE             = 1;
    public static final short CARDINALITY_COUNTABLY_INFINITE = 2;

    /**
     * If this type is created from restriction, then some facets can be applied
     * to the simple type.
     *
     * @param facets        the value of all the facets
     * @param presentFacets which facets are present
     * @param fixedFacets   which facets are fixed
     */
    public void applyFacets(XSFacets facets, short presentFacet, short fixedFacet)
        throws InvalidDatatypeFacetException;

    /**
     * Get the variety of the simple type: atomic, list or union.
     *
     * @return  a constant corresponding to the variety
     */
    public short getVariety();

    /**
     * Check whether two actual values are equal.
     *
     * @param value1  the first value
     * @prarm value2  the second value
     * @return        true if the two value are equal
     */
    public boolean isEqual(Object value1, Object value2);

    /**
     * Check the order of the two actual values. (May not be supported by all
     * simple types.
     * REVISIT: Andy believes that a compare() method is necessary.
     *          I don't see the necessity for schema (the only place where we
     *          need to compare two values is to check min/maxIn/Exclusive
     *          facets, but we only need a private method for this case.)
     *          But Andy thinks XPATH potentially needs this compare() method.
     *
     * @param value1  the first value
     * @prarm value2  the second value
     * @return        > 0 if value1 > value2
     *                = 0 if value1 == value2
     *                < = if value1 < value2
     */
    //public short compare(Object value1, Object value2);

    /**
     * Return which facets are defined in this simple type.
     *
     * @return  the bit combination of the constants corresponding to the
     *          defined facets
     */
    public short getDefinedFacets();

    /**
     * Check whether this type is or is derived from ID.
     * REVISIT: this method makes ID special, which is not a good design.
     *          but since ID is not a primitive, there doesn't seem to be a
     *          clean way of doing it except to define special method like this.
     */
    public boolean isIDType();

    // REVISIT: the following methods are for PSVI. they are not implemented yet.
    //public XSFacet[] getFacets();
    // the following four are for fundamental facets
    //public short getOrderedFacet();
    //public boolean isBounded();
    //public boolean isNumeric();
    //public short getCardinalityFacet();
}
