/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights
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

package org.apache.xerces.validators.datatype;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.io.IOException;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;

/**
 *
 * DecimalDatatypeValidator validates that content satisfies the W3C XML Datatype for decimal
 *
 * @author  Elena Litani
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */

public class DecimalDatatypeValidator extends AbstractDatatypeValidator {
    private Locale                  fLocale                 = null;
    private BigDecimal[]            fEnumDecimal            = null;
    private String                  fPattern                = null;
    private BigDecimal              fMaxInclusive           = null;
    private BigDecimal              fMaxExclusive           = null;
    private BigDecimal              fMinInclusive           = null;
    private BigDecimal              fMinExclusive           = null;
    private int                     fFacetsDefined          = 0;
    private int                     fTotalDigits            = 0;
    private int                     fFractionDigits         = 0;
    private boolean                 isMaxExclusiveDefined   = false;
    private boolean                 isMaxInclusiveDefined   = false;
    private boolean                 isMinExclusiveDefined   = false;
    private boolean                 isMinInclusiveDefined   = false;
    private boolean                 isTotalDigitsDefined    = false;
    private boolean                 isFractionDigitsDefined = false;
    private DatatypeMessageProvider fMessageProvider        = new DatatypeMessageProvider();
    private RegularExpression       fRegex                  = null;

    public DecimalDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public DecimalDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                      boolean derivedByList ) throws InvalidDatatypeFacetException {
         // Set base type
        setBasetype( base );

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        // Set Facets if any defined
        if ( facets != null  ){
            Vector enumeration = null;
            for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                String key   = (String) e.nextElement();
                String value = null;
                try {
                    if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                        fFacetsDefined += DatatypeValidator.FACET_PATTERN;
                        fPattern = (String) facets.get(key);
                        if ( fPattern != null )
                            fRegex = new RegularExpression(fPattern, "X" );
                    } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                        enumeration     = (Vector)facets.get(key);
                        fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                    } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                        isMaxInclusiveDefined = true;
                        fMaxInclusive    = new BigDecimal(stripPlusIfPresent(value));
                    } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                        isMaxExclusiveDefined = true;
                        fMaxExclusive   = new BigDecimal(stripPlusIfPresent( value));
                    } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                        isMinInclusiveDefined = true;
                        fMinInclusive   = new BigDecimal(stripPlusIfPresent(value));
                    } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_MINEXCLUSIVE;
                        isMinExclusiveDefined = true;
                        fMinExclusive   = new BigDecimal(stripPlusIfPresent(value));
                    } else if (key.equals(SchemaSymbols.ELT_TOTALDIGITS)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_TOTALDIGITS;
                        isTotalDigitsDefined = true;
                        fTotalDigits      = Integer.parseInt(value );
                        // check 4.3.11.c0 must: totalDigits > 0
                        if ( fTotalDigits <= 0 )
                            throw new InvalidDatatypeFacetException("totalDigits value '"+fTotalDigits+"' must be a positiveInteger.");
                    } else if (key.equals(SchemaSymbols.ELT_FRACTIONDIGITS)) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined += DatatypeValidator.FACET_FRACTIONDIGITS;
                        isFractionDigitsDefined  = true;
                        fFractionDigits          = Integer.parseInt( value );
                        // check 4.3.12.c0 must: fractionDigits > 0
                        if ( fFractionDigits < 0 )
                            throw new InvalidDatatypeFacetException("fractionDigits value '"+fFractionDigits+"' must be a positiveInteger.");
                    } else {
                        throw new InvalidDatatypeFacetException( getErrorString( DatatypeMessageProvider.MSG_FORMAT_FAILURE,
                                                                                 DatatypeMessageProvider.MSG_NONE, null));
                    }
                } catch ( Exception ex ){
                    throw new InvalidDatatypeFacetException( getErrorString( DatatypeMessageProvider.IllegalFacetValue,
                                                                             DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                }
            }

            if ( base != null ) {
                // check 4.3.5.c0 must: enumeration values from the value space of base
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    if ( enumeration != null ) {
                        int i = 0;
                        try {
                            for ( ; i < enumeration.size(); i++) {
                                base.validate ((String)enumeration.elementAt(i), null);
                            }
                        } catch ( Exception idve ){
                            throw new InvalidDatatypeFacetException( "Value of enumeration = '" + enumeration.elementAt(i) +
                                                                     "' must be from the value space of base.");
                        }
                    }
                }
                // check 4.3.7.c0 must: maxInclusive value from the value space of base
                if ( isMaxInclusiveDefined ) {
                    try {
                        base.validate (fMaxInclusive.toString(), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxInclusive = '" + fMaxInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.8.c0 must: maxInclusive value from the value space of base
                if ( isMaxExclusiveDefined ) {
                    try {
                        base.validate (fMaxExclusive.toString(), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxExclusive = '" + fMaxExclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.9.c0 must: minInclusive value from the value space of base
                if ( isMinInclusiveDefined ) {
                    try {
                        base.validate (fMinInclusive.toString(), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of minInclusive = '" + fMinInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.10.c0 must: minInclusive value from the value space of base
                if ( isMinExclusiveDefined ) {
                    try {
                        base.validate (fMinExclusive.toString(), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of minExclusive = '" + fMinExclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
            }

            // check 4.3.8.c1 error: maxInclusive + maxExclusive
            if ( isMaxExclusiveDefined && isMaxInclusiveDefined ) {
                throw new InvalidDatatypeFacetException( "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." );
            }
            // check 4.3.9.c1 error: minInclusive + minExclusive
            if ( isMinExclusiveDefined && isMinInclusiveDefined ) {
                throw new InvalidDatatypeFacetException( "It is an error for both minInclusive and minExclusive to be specified for the same datatype." );
            }

            // check 4.3.7.c1 must: minInclusive <= maxInclusive
            if ( isMaxInclusiveDefined && isMinInclusiveDefined ){
                int compareTo = this.fMinInclusive.compareTo( this.fMaxInclusive );
                if ( compareTo == 1 )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be <= maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
            if ( isMaxExclusiveDefined && isMinExclusiveDefined ){
                int compareTo = this.fMinExclusive.compareTo( this.fMaxExclusive );
                if ( compareTo == 1 )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be <= maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.9.c2 must: minExclusive < maxInclusive
            if ( isMaxInclusiveDefined && isMinExclusiveDefined ){
                int compareTo = this.fMinExclusive.compareTo( this.fMaxInclusive );
                if ( compareTo != -1 )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be > maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if ( isMaxExclusiveDefined && isMinInclusiveDefined ){
                int compareTo = this.fMinInclusive.compareTo( this.fMaxExclusive );
                if ( compareTo != -1 )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be < maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.12.c1 must: fractionDigits <= totalDigits
            if ( isFractionDigitsDefined && isTotalDigitsDefined ){
                if ( fFractionDigits > fTotalDigits )
                    throw new InvalidDatatypeFacetException( "fractionDigits value ='" + this.fFractionDigits + "'must be <= totalDigits value ='" +
                                                             this.fTotalDigits + "'. " );
            }

            if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
                if (enumeration != null) {
                    fEnumDecimal = new BigDecimal[enumeration.size()];
                    int i = 0;
                    try {
                        for ( ; i < enumeration.size(); i++) {
                            fEnumDecimal[i] =
                            new BigDecimal( stripPlusIfPresent(((String) enumeration.elementAt(i))));
                        }

                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                                DatatypeMessageProvider.MSG_NONE,
                                                                                new Object [] { enumeration.elementAt(i)}));
                    }
                }
            }

            if (base != null && base instanceof DecimalDatatypeValidator) {
                DecimalDatatypeValidator numBase = (DecimalDatatypeValidator)base;

                // check 4.3.7.c2 error:
                // maxInclusive > base.maxInclusive
                // maxInclusive >= base.maxExclusive
                // maxInclusive < base.minInclusive
                // maxInclusive <= base.minExclusive
                if ( isMaxInclusiveDefined ) {
                    if ( numBase.isMaxInclusiveDefined &&
                         fMaxInclusive.compareTo (numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( numBase.isMaxExclusiveDefined &&
                         fMaxInclusive.compareTo (numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be < base.maxExclusive value ='" +
                                                                numBase.fMaxExclusive + "'." );
                    if ( numBase.isMinInclusiveDefined &&
                         fMaxInclusive.compareTo (numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be >= base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                    if ( numBase.isMinExclusiveDefined &&
                         fMaxInclusive.compareTo (numBase.fMinExclusive) != 1 )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be > base.minExclusive value ='" +
                                                                numBase.fMinExclusive + "'." );
                }

                // check 4.3.8.c3 error:
                // maxExclusive > base.maxExclusive
                // maxExclusive > base.maxInclusive
                // maxExclusive <= base.minInclusive
                // maxExclusive <= base.minExclusive
                if ( isMaxExclusiveDefined ) {
                    if ( numBase.isMaxExclusiveDefined &&
                         fMaxExclusive.compareTo (numBase.fMaxExclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                    if ( numBase.isMaxInclusiveDefined &&
                         fMaxExclusive.compareTo (numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( numBase.isMinExclusiveDefined &&
                         fMaxExclusive.compareTo (numBase.fMinExclusive) != 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( numBase.isMinInclusiveDefined &&
                         fMaxExclusive.compareTo (numBase.fMinInclusive) != 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                }

                // check 4.3.9.c3 error:
                // minExclusive < base.minExclusive
                // minExclusive > base.maxInclusive ??? minExclusive >= base.maxInclusive
                // minExclusive < base.minInclusive
                // minExclusive >= base.maxExclusive
                if ( isMinExclusiveDefined ) {
                    if ( numBase.isMinExclusiveDefined &&
                         fMinExclusive.compareTo (numBase.fMinExclusive) == -1 )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be >= base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( numBase.isMaxInclusiveDefined &&
                         fMinExclusive.compareTo (numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be <= base.maxInclusive value ='" +
                                                                numBase.fMaxInclusive + "'." );
                    if ( numBase.isMinInclusiveDefined &&
                         fMinExclusive.compareTo (numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be >= base.minInclusive value ='" +
                                                                numBase.fMinInclusive + "'." );
                    if ( numBase.isMaxExclusiveDefined &&
                         fMinExclusive.compareTo (numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be < base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                }

                // check 4.3.10.c2 error:
                // minInclusive < base.minInclusive
                // minInclusive > base.maxInclusive
                // minInclusive <= base.minExclusive
                // minInclusive >= base.maxExclusive
                if ( isMinInclusiveDefined ) {
                    if ( numBase.isMinInclusiveDefined &&
                         fMinInclusive.compareTo (numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be >= base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                    if ( numBase.isMaxInclusiveDefined &&
                         fMinInclusive.compareTo (numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( numBase.isMinExclusiveDefined &&
                         fMinInclusive.compareTo (numBase.fMinExclusive) != 1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be > base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( numBase.isMaxExclusiveDefined &&
                         fMinInclusive.compareTo (numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be < base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                }

                // check 4.3.11.c1 error: totalDigits > base.totalDigits
                if ( isTotalDigitsDefined ) {
                    if ( numBase.isTotalDigitsDefined &&
                         fTotalDigits > numBase.fTotalDigits )
                        throw new InvalidDatatypeFacetException( "totalDigits value ='" + fTotalDigits + "' must be <= base.totalDigits value ='" +
                                                                 numBase.fTotalDigits + "'." );
                }

                // check question error: fractionDigits > base.fractionDigits ???
                // check question error: fractionDigits > base.totalDigits ???
                // check question error: totalDigits conflicts with bounds ???

                // inherit enumeration
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) == 0 &&
                     (numBase.fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                    fEnumDecimal = numBase.fEnumDecimal;
                }
                // inherit maxExclusive
                if ( numBase.isMaxExclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxExclusiveDefined = true;
                    fFacetsDefined += FACET_MAXEXCLUSIVE;
                    fMaxExclusive = numBase.fMaxExclusive;
                }
                // inherit maxInclusive
                if ( numBase.isMaxInclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxInclusiveDefined = true;
                    fFacetsDefined += FACET_MAXINCLUSIVE;
                    fMaxInclusive = numBase.fMaxInclusive;
                }
                // inherit minExclusive
                if ( numBase.isMinExclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinExclusiveDefined = true;
                    fFacetsDefined += FACET_MINEXCLUSIVE;
                    fMinExclusive = numBase.fMinExclusive;
                }
                // inherit minExclusive
                if ( numBase.isMinInclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinInclusiveDefined = true;
                    fFacetsDefined += FACET_MININCLUSIVE;
                    fMinInclusive = numBase.fMinInclusive;
                }
                // inherit totalDigits
                if ( numBase.isTotalDigitsDefined && !isTotalDigitsDefined ) {
                    isTotalDigitsDefined = true;
                    fFacetsDefined += FACET_TOTALDIGITS;
                    fTotalDigits = numBase.fTotalDigits;
                }
                // inherit fractionDigits
                if ( numBase.isFractionDigitsDefined && !isFractionDigitsDefined ) {
                    isFractionDigitsDefined = true;
                    fFacetsDefined += FACET_FRACTIONDIGITS;
                    fFractionDigits = numBase.fFractionDigits;
                }
            }
        }//End of Facet setup
    }

    /**
     * validate that a string matches the decimal datatype
     *
     * validate returns true or false depending on whether the string content is a
     * W3C decimal type.
     *
     * @param content A string containing the content to be validated
     *                            cd
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C decimal type
     */

    public Object validate(String content, Object state) throws InvalidDatatypeValueException {
        //REVISIT: should we pass state?
        checkContent(content, state, null, false);
        return null;
    }

    /**
     * validate if the content is valid against base datatype and facets (if any)
     * this function might be called directly from UnionDatatype or ListDatatype
     *
     * @param content A string containing the content to be validated
     * @param enumeration A vector with enumeration strings
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C decimal type;
     * @exception throws InvalidDatatypeFacetException if enumeration is not BigDecimal
     */

    protected void checkContentEnum(String content, Object state, Vector enumeration)
    throws InvalidDatatypeValueException {
        checkContent(content, state, enumeration, false);
    }

    protected void checkContent(String content, Object state, Vector enumeration, boolean asBase)
    throws InvalidDatatypeValueException {
        // validate against parent type if any
        if ( this.fBaseValidator != null ) {
            // validate content as a base type
            if (fBaseValidator instanceof DecimalDatatypeValidator) {
                ((DecimalDatatypeValidator)fBaseValidator).checkContent(content, state, enumeration, true);
            } else {
                this.fBaseValidator.validate( content, state );
            }
        }

        // we check pattern first
        if ( (fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
            if ( fRegex == null || fRegex.matches( content) == false )
                throw new InvalidDatatypeValueException("Value'"+content+
                                                        "' does not match regular expression facet " + fRegex.getPattern() );
        }

        // if this is a base validator, we only need to check pattern facet
        // all other facet were inherited by the derived type
        if (asBase)
            return;

        BigDecimal d = null; // Is content a Decimal
        try {
            d = new BigDecimal( stripPlusIfPresent( content));
        } catch (Exception nfe) {
            throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.NotDecimal,
                                                                    DatatypeMessageProvider.MSG_NONE,
                                                                    new Object[] { "'" + content +"'"}));
        }

        if (enumeration != null) { //the call was made from List or Union
            int size= enumeration.size();
            BigDecimal[]     enumDecimal  = new BigDecimal[size];
            int i = 0;
            try {
                for ( ; i < size; i++)
                    enumDecimal[i] = new BigDecimal( stripPlusIfPresent(((String) enumeration.elementAt(i))));
            } catch (NumberFormatException nfe) {
                throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                        DatatypeMessageProvider.MSG_NONE,
                                                                        new Object [] { enumeration.elementAt(i)}));
            }
            enumCheck(d, enumDecimal);
        }

        if ( isFractionDigitsDefined == true ) {
            if (d.scale() > fFractionDigits)
                throw new InvalidDatatypeValueException(
                                                        getErrorString(DatatypeMessageProvider.FractionDigitsExceeded,
                                                                        DatatypeMessageProvider.MSG_NONE,
                                                                        new Object[] { content}));
        }
        if ( isTotalDigitsDefined == true ) {
            int totalDigits = d.movePointRight(d.scale()).toString().length() -
                            ((d.signum() < 0) ? 1 : 0); // account for minus sign
            if (totalDigits > fTotalDigits)
                throw new InvalidDatatypeValueException(
                                getErrorString(DatatypeMessageProvider.TotalDigitsExceeded,
                                 DatatypeMessageProvider.MSG_NONE,
                                 new Object[] { "'" + content + "'" + "with totalDigits = '"+ totalDigits +"'"
                                              , "'" + fTotalDigits + "'" } ));
        }
        boundsCheck(d);
        if (  fEnumDecimal != null )
            enumCheck(d, fEnumDecimal);

        return;

    }

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    public void boundsCheck(BigDecimal d) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;
        String  upperBound =  (fMaxExclusive != null )? ( fMaxExclusive.toString() ):
                              ( ( fMaxInclusive != null )?fMaxInclusive.toString():"");

        String  lowerBound =  (fMinExclusive != null )? ( fMinExclusive.toString() ):
                              (( fMinInclusive != null )?fMinInclusive.toString():"");
        String  lowerBoundIndicator = "";
        String  upperBoundIndicator = "";


        if ( isMaxInclusiveDefined){
            maxOk = (d.compareTo(fMaxInclusive) <= 0);
            upperBound          = fMaxInclusive.toString();
            if ( upperBound != null ){
                upperBoundIndicator = "<=";
            } else {
                upperBound="";
            }
        } else if ( isMaxExclusiveDefined){
            maxOk = (d.compareTo(fMaxExclusive) < 0);
            upperBound = fMaxExclusive.toString();
            if ( upperBound != null ){
                upperBoundIndicator = "<";
            } else {
                upperBound = "";
            }
        } else{
            maxOk = (!isMaxInclusiveDefined && ! isMaxExclusiveDefined);
        }


        if ( isMinInclusiveDefined){
            minOk = (d.compareTo(fMinInclusive) >= 0);
            lowerBound = fMinInclusive.toString();
            if( lowerBound != null ){
            lowerBoundIndicator = "<=";
            }else {
                lowerBound = "";
            }
        } else if ( isMinExclusiveDefined){
            minOk = (d.compareTo(fMinExclusive) > 0);
            lowerBound = fMinExclusive.toString();
            if( lowerBound != null ){
            lowerBoundIndicator = "<";
            } else {
                lowerBound = "";
            }
        } else{
            minOk = (!isMinInclusiveDefined && !isMinExclusiveDefined);
        }

        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException (
                                                    getErrorString(DatatypeMessageProvider.OutOfBounds,
                                                                   DatatypeMessageProvider.MSG_NONE,
                                                                   new Object [] { d.toString() ,  lowerBound ,
                                                                       upperBound, lowerBoundIndicator, upperBoundIndicator}));

    }

    private void enumCheck(BigDecimal v, BigDecimal[] enum) throws InvalidDatatypeValueException {
        for (int i = 0; i < enum.length; i++) {
            if (v.equals(enum[i] ))
            {
                return;
            }
        }
        throw new InvalidDatatypeValueException(
                                               getErrorString(DatatypeMessageProvider.NotAnEnumValue,
                                                              DatatypeMessageProvider.MSG_NONE,
                                                              new Object [] { v}));
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }


    private String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        } catch (Exception e) {
            return "Illegal Errorcode "+minor;
        }
    }
    /**
       * Returns a copy of this object.
       */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


    public int compare( String value1, String value2){
        try {
            //REVISIT: datatypes create lots of *new* objects..
            BigDecimal d1 = new BigDecimal(stripPlusIfPresent(value1));
            BigDecimal d2 = new BigDecimal(stripPlusIfPresent(value2));
            return d1.compareTo(d2);
        } catch (NumberFormatException e){
            //REVISIT: should we throw exception??
            return -1;
        }
    }


    private void setBasetype(DatatypeValidator base) {
        fBaseValidator =  base;
    }

    /**
     * This class deals with a bug in BigDecimal class
     * present up to version 1.1.2. 1.1.3 knows how
     * to deal with the + sign.
     *
     * This method strips the first '+' if it found
     * alone such as.
     * +33434.344
     *
     * If we find +- then nothing happens we just
     * return the string passed
     *
     * @param value
     * @return
     */
    static private String stripPlusIfPresent( String value ){
        String strippedPlus = value;

        if ( value.length() >= 2 && value.charAt(0) == '+' && value.charAt(1) != '-' ) {
            strippedPlus = value.substring(1);
        }
        return strippedPlus;
    }
}
