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

import org.apache.xerces.impl.validation.ValidationContext;
import org.apache.xerces.impl.xs.XSTypeDecl;

/**
 * This interface <code>XSSimpleType</code> represents the simple type
 * definition of schema component and defines methods to query the information
 * contained.
 * Any simple type (atomic, list or union) will implement this interface.
 * It inherits from <code>XSTypeDecl</code>.
 *
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public interface XSSimpleType extends XSTypeDecl {

    /**
     * Constant defined for the constraining facets as defined in schema.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#rf-facets'> XML Schema
     * Part 2: Datatypes </a>
     * The bit combination of the following constants are used to tell
     * which facets are fixed, and which ones are present
     */
    /** the length facet */
    public static final short FACET_LENGTH         = 1<<0;
    /** the minLength facet */
    public static final short FACET_MINLENGTH      = 1<<1;
    /** the maxLength facet */
    public static final short FACET_MAXLENGTH      = 1<<2;
    /** the pattern facet */
    public static final short FACET_PATTERN        = 1<<3;
    /** the enumeration facet */
    public static final short FACET_ENUMERATION    = 1<<4;
    /** the whiteSpace facet */
    public static final short FACET_WHITESPACE     = 1<<5;
    /** the maxInclusive facet */
    public static final short FACET_MAXINCLUSIVE   = 1<<6;
    /** the maxExclusive facet */
    public static final short FACET_MAXEXCLUSIVE   = 1<<7;
    /** the minExclusive facet */
    public static final short FACET_MINEXCLUSIVE   = 1<<8;
    /** the minInclusive facet */
    public static final short FACET_MININCLUSIVE   = 1<<9;
    /** the totalDigits facet */
    public static final short FACET_TOTALDIGITS    = 1<<10;
    /** the fractionDigits facet */
    public static final short FACET_FRACTIONDIGITS = 1<<11;

    /**
     * constants defined for the 'variety' property of Simple Type schema component.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#defn-variety'> XML Schema
     * Part 2: Datatypes </a>
     */
    /** the absent variety, for anySimpleType */
    public static final short VARIETY_ABSENT    = 0;
    /** the atomic variety */
    public static final short VARIETY_ATOMIC    = 1;
    /** the list variety */
    public static final short VARIETY_LIST      = 2;
    /** the union variety */
    public static final short VARIETY_UNION     = 3;

    /**
     * constants defined for the values of 'whitespace' facet.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#dt-whiteSpace'> XML Schema
     * Part 2: Datatypes </a>
     */
    /** preserve the white spaces */
    public static final short WS_PRESERVE = 0;
    /** replace the white spaces */
    public static final short WS_REPLACE  = 1;
    /** collapse the white spaces */
    public static final short WS_COLLAPSE = 2;

    /**
     * constants defined for the 'ordered' fundamental facet.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#rf-fund-facets'> XML
     * Schema Part 2: Datatypes </a>
     */
    /** not ordered */
    public static final short ORDERED_FALSE     = 1;
    /** partically ordered */
    public static final short ORDERED_PARTIAL   = 2;
    /** totally ordered */
    public static final short ORDERED_TOTAL     = 3;

    /**
     * constants defined for the 'cardinality' fundamental facet.
     * see <a href='http://www.w3.org/TR/xmlschema-2/#rf-fund-facets'> XML
     * Schema Part 2: Datatypes </a>
     */
    /** finite cardinality */
    public static final short CARDINALITY_FINITE             = 1;
    /** countably infinite cardinality */
    public static final short CARDINALITY_COUNTABLY_INFINITE = 2;

    /**
     * validate a given string against this simple type.
     *
     * @param content       the string value that needs to be validated
     * @param context       the validation context
     * @param validatedInfo used to store validation result
     *
     * @return              the actual value (QName, Boolean) of the string value
     */
    public Object validate(String content, ValidationContext context, ValidatedInfo validatedInfo)
        throws InvalidDatatypeValueException;

    /**
     * validate an actual value against this simple type.
     *
     * @param value         the actual value that needs to be validated
     * @param context       the validation context
     * @param validatedInfo used to provide the actual value and member types
     * @exception InvalidDatatypeValueException  exception for invalid values.
     */
    public void validate(ValidationContext context, ValidatedInfo validatedInfo)
        throws InvalidDatatypeValueException;

    /**
     * If this type is created from restriction, then some facets can be applied
     * to the simple type. <code>XSFacets</code> is used to pass the value of
     * different facets.
     *
     * @param facets        the value of all the facets
     * @param presentFacets bit combination value of the costraining facet
     *                      constants which are present.
     * @param fixedFacets   bit combination value of the costraining facet
     *                      constants which are fixed.
     * @param ValidationContext the validation context
     * @exception InvalidDatatypeFacetException  exception for invalid facet values.
     */
    public void applyFacets(XSFacets facets, short presentFacet, short fixedFacet, ValidationContext context)
        throws InvalidDatatypeFacetException;

    /**
     * Get the variety of the simple type: atomic, list or union.
     *
     * @return  a constant corresponding to the variety, as defined above.
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
     * bit combination of the constants defined in this simple type.
     *
     * @return  the bit combination of the constants corresponding to the
     *          constraining facets, as defined above.
     */
    public short getDefinedFacets();

    /**
     * Check whether this type is or is derived from ID.
     * REVISIT: this method makes ID special, which is not a good design.
     *          but since ID is not a primitive, there doesn't seem to be a
     *          clean way of doing it except to define special method like this.
     *
     * @return  whether this simple type is or is derived from ID.
     */
    public boolean isIDType();

    // REVISIT: it's not decided yet how to return the facets,
    //          as String's or as values (Object's).
    //public XSFacet[] getFacets();

    /**
     * Return the value of the "ordered" fundamental facet.
     *
     * @return  a constant corresponding to the "ordered" facet.
     */
    public short getOrderedFacet();

    /**
     * Return the value of the "bounded" fundamental facet.
     *
     * @return  whether the this type is bounded.
     */
    public boolean isBounded();

    /**
     * Return the value of the "numeric" fundamental facet.
     *
     * @return  whether the this type is numeric.
     */
    public boolean isNumeric();

    /**
     * Return the value of the "cardinality" fundamental facet.
     *
     * @return  a constant corresponding to the "cardinality" facet.
     */
    public short getCardinalityFacet();

    
    /**
     * Return the whitespace corresponding to this datatype.
     * 
     * @return valid values are WS_PRESERVE, WS_REPLACE, WS_COLLAPSE.
     * @exception DatatypeException
     *                   union datatypes don't have whitespace facet associated with them
     */
    public short getWhitespace () throws DatatypeException;
}
