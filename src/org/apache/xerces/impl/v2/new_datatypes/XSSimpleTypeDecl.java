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

package org.apache.xerces.impl.v2.new_datatypes;

import org.apache.xerces.impl.v2.XSTypeDecl;
import org.apache.xerces.impl.v2.datatypes.InvalidDatatypeValueException;
import org.apache.xerces.impl.v2.datatypes.DatatypeMessageProvider;
import org.apache.xerces.impl.v2.msg.XMLMessages;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.util.XIntPool;
import org.apache.xerces.util.XInt;
import org.apache.xerces.impl.v2.util.regex.RegularExpression;

import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.Locale;



/**
 * @version $Id$
 */
public class XSSimpleTypeDecl implements XSTypeDecl {

    public static final short INDEX_LENGTH         = 0;
    public static final short INDEX_MINLENGTH      = 1;
    public static final short INDEX_MAXLENGTH      = 2;
    public static final short INDEX_PATTERN        = 3;
    public static final short INDEX_ENUMERATION    = 4;
    public static final short INDEX_WHITESPACE     = 5;
    public static final short INDEX_MAXINCLUSIVE   = 6;
    public static final short INDEX_MAXEXCLUSIVE   = 7;
    public static final short INDEX_MINEXCLUSIVE   = 8;
    public static final short INDEX_MININCLUSIVE   = 9;
    public static final short INDEX_TOTALDIGITS    = 10;
    public static final short INDEX_FRACTIONDIGITS = 11;
    public static final short INDEX_TOKENTYPE      = 12;

    public static final short FACET_PARAM_SIZE     = 13;

    public static final short DEFINED_LENGTH         = 1<<0;
    public static final short DEFINED_MINLENGTH      = 1<<1;
    public static final short DEFINED_MAXLENGTH      = 1<<2;
    public static final short DEFINED_PATTERN        = 1<<3;
    public static final short DEFINED_ENUMERATION    = 1<<4;
    public static final short DEFINED_WHITESPACE     = 1<<5;
    public static final short DEFINED_MAXINCLUSIVE   = 1<<6;
    public static final short DEFINED_MAXEXCLUSIVE   = 1<<7;
    public static final short DEFINED_MINEXCLUSIVE   = 1<<8;
    public static final short DEFINED_MININCLUSIVE   = 1<<9;
    public static final short DEFINED_TOTALDIGITS    = 1<<10;
    public static final short DEFINED_FRACTIONDIGITS = 1<<11;
    public static final short DEFINED_TOKENTYPE      = 1<<12;

    public static final short VARIETY_NON    = 0;
    public static final short VARIETY_ATOMIC = 1;
    public static final short VARIETY_LIST   = 2;
    public static final short VARIETY_UNION  = 3;

    //REVISIT: trying to compile
    public static final short PRESERVE  = 0;
    public static final short COLLAPSE = 1;
    public static final short REPLACE = 2;

    public static final short DV_ANYSIMPLETYPE = 0;
    public static final short DV_STRING        = 1;
    public static final short DV_BOOLEAN       = 2;
    public static final short DV_DECIMAL       = 3;
    public static final short DV_FLOAT         = 4;
    public static final short DV_DOUBLE        = 5;
    public static final short DV_DURATION      = 6;
    public static final short DV_DATETIME      = 7;
    public static final short DV_TIME          = 8;
    public static final short DV_DATE          = 9;
    public static final short DV_GYEARMONTH    = 10;
    public static final short DV_GYEAR         = 11;
    public static final short DV_GMONTHDAY     = 12;
    public static final short DV_GDAY          = 13;
    public static final short DV_GMONTH        = 14;
    public static final short DV_HEXBINARY     = 15;
    public static final short DV_BASE64BINARY  = 16;
    public static final short DV_ANYURI        = 17;
    public static final short DV_QNAME         = 18;
    public static final short DV_NOTATION      = 19;
    public static final short DV_ID            = 19;
    public static final short DV_IDREF         = 20;
    public static final short DV_ENTITY        = 21;
    public static final short DV_LIST          = 22;
    public static final short DV_UNION         = 23;

    //REVISIT: check if it's required.
    protected static final short INDETERMINATE=2;

    static final TypeValidator[] fDVs = {
/*
        new AnySimpleTypeDV(),
        new StringDV(),
        new BooleanDV(),
        new DecimalDV(),
        new FloatDV(),
        new DoubleDV(),
        new DurationDV(),
        new DateTimeDV(),
        new TimeDV(),
        new DateDV(),
        new YearMonthDV(),
        new YearDV(),
        new MonthDayDV(),
        new DayDV(),
        new MonthDV(),
        new HexBinaryDV(),
        new Base64BinaryDV(),
        new AnyURIDV(),
        new QNameDV(),
        new NotationDV(),
        new IdDV(),
        new IdRefDV(),
        new EntityDV(),
        new ListDV(),
        new UnionDV(),
*/
    };

    //REVISIT: usage
    static XIntPool xIntPool = new XIntPool();

    public static final XInt TOKEN_NONE        = xIntPool.getXInt(0);
    public static final XInt TOKEN_NAME        = xIntPool.getXInt(1);
    public static final XInt TOKEN_NCNAME      = xIntPool.getXInt(2);
    public static final XInt TOKEN_IDNAME      = xIntPool.getXInt(3);
    public static final XInt TOKEN_IDNCNAME    = xIntPool.getXInt(4);
    public static final XInt TOKEN_IDREFNAME   = xIntPool.getXInt(5);
    public static final XInt TOKEN_IDREFNCNAME = xIntPool.getXInt(6);
    public static final XInt TOKEN_ENTITY      = xIntPool.getXInt(7);
    public static final XInt TOKEN_NMTOKEN     = xIntPool.getXInt(8);

    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private Locale            fLocale  = null;


    // REVISIT: we can have only one array
    private XSSimpleTypeDecl fBase;
    private XSSimpleTypeDecl fItemType;
    private XSSimpleTypeDecl [] fMemberTypes;

    private String fTypeName;
    private String fTargetNamespace;
    private short fFinalSet = 0;

    private int fBaseIdx = -1;
    private String fBaseUri;

    private short fFacetsDefined = 0;
    private int fLength;
    private int fMinLength;
    private int fMaxLength;
    private Vector fPattern;
    private Object fEnumeration[];
    private short fWhiteSpace;
    private Object fMaxInclusive;
    private Object fMaxExclusive;
    private Object fMinExclusive;
    private Object fMinInclusive;
    private int fTotalDigits;
    private int fFractionDigits;
    private short fTokeyType = 0;
    private short fFixedFacet = 0;
    private short fVariety;
    private short fPrimitiveDV;
    private short fValidateDV;
    private int fFundFacets;

    public short getXSType () {
        return SIMPLE_TYPE;
    }

    public String getXSTypeName() {
        return fTypeName;
    }



    //Create a new simple type.
    public XSSimpleTypeDecl(XSSimpleTypeDecl base, String name, String uri, short finalSet) {
        fTypeName = name;
        fTargetNamespace = uri;
        fFinalSet = finalSet;
    }

    /**
     * for built-in primitive types (and id/idref/entity)
     */
    public void init4BuiltInType(short validateDV) {
        fVariety = VARIETY_ATOMIC;
        fPrimitiveDV = fDVs[validateDV].getPrimitiveDV();
        fValidateDV = validateDV;
    }

    /**
     * If <restriction> is chosen, or built-in derived types by restriction
     */
    public void init4Restriction(XSSimpleTypeDecl base, Object[] facets,
                                 short presentFacet, short fixedFacet) {
        fVariety = base.fVariety;
        int result = 0 ;
        switch (fVariety) {
        case VARIETY_ATOMIC:
            fPrimitiveDV = base.fPrimitiveDV;
            fValidateDV = base.fValidateDV;
        break;
        case VARIETY_LIST:
            fItemType = base.fItemType;
            fValidateDV = DV_LIST;
        break;
        case VARIETY_UNION:
            fMemberTypes = base.fMemberTypes;
            fValidateDV = DV_UNION;
        break;
        }

        // step 1: inherit fixed facets from base
        fFacetsDefined = fFixedFacet = base.fFixedFacet;

        // length
        if ((fFixedFacet & DEFINED_LENGTH) != 0)
            fLength = base.fLength;
        // minLength
        if ((fFixedFacet & DEFINED_MINLENGTH) != 0)
            fMinLength = base.fMinLength;
        // maxLength
        if ((fFixedFacet & DEFINED_MAXLENGTH) != 0)
            fMaxLength = base.fMaxLength;
        // pattern
        if ((fFixedFacet & DEFINED_PATTERN) != 0)
            fPattern = base.fPattern;
        else
            fPattern = new Vector();
        // enumeration
        if ((fFixedFacet & DEFINED_ENUMERATION) != 0)
            fEnumeration = base.fEnumeration;
        // whiteSpace
        if ((fFixedFacet & DEFINED_WHITESPACE) != 0)
            fWhiteSpace = base.fWhiteSpace;
        // maxInclusive
        if ((fFixedFacet & DEFINED_MAXINCLUSIVE) != 0)
            fMaxInclusive = base.fMaxInclusive;
        // maxExclusive
        if ((fFixedFacet & DEFINED_MAXEXCLUSIVE) != 0)
            fMaxExclusive = base.fMaxExclusive;
        // minExclusive
        if ((fFixedFacet & DEFINED_MINEXCLUSIVE) != 0)
            fMinExclusive = base.fMinExclusive;
        // minInclusive
        if ((fFixedFacet & DEFINED_MININCLUSIVE) != 0)
            fMinInclusive = base.fMinInclusive;
        // totalDigits
        if ((fFixedFacet & DEFINED_TOTALDIGITS) != 0)
            fTotalDigits = base.fTotalDigits;
        // fractionDigits
        if ((fFixedFacet & DEFINED_FRACTIONDIGITS) != 0)
            fFractionDigits = base.fFractionDigits;

        if (facets == null)
            return;

        // step 2: parse present facets: check against base fixed

        short allowedFacet = fDVs[fValidateDV].getAllowedFacets();

        // length
        if ((presentFacet & DEFINED_LENGTH) != 0) {
            if ((allowedFacet & DEFINED_LENGTH) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_LENGTH) == 0) {
                reportError("fixed facet in base");
            } else {
                fLength = ((Integer)facets[INDEX_LENGTH]).intValue();
                fFacetsDefined |= DEFINED_LENGTH;
                if ((fixedFacet & DEFINED_LENGTH) == 0)
                    fFixedFacet |= DEFINED_LENGTH;
                // check 4.3.1.c0 must: length >= 0
                if (fLength < 0)
                    reportError("length value '"+facets[INDEX_LENGTH]+"' must be a nonNegativeInteger.");
            }
        }
        // minLength
        if ((presentFacet & DEFINED_MINLENGTH) != 0) {
            if ((allowedFacet & DEFINED_MINLENGTH) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MINLENGTH) == 0) {
                reportError("fixed facet in base");
            } else {
                fMinLength = ((Integer)facets[INDEX_MINLENGTH]).intValue();
                fFacetsDefined |= DEFINED_MINLENGTH;
                if ((fixedFacet & DEFINED_MINLENGTH) == 0)
                    fFixedFacet |= DEFINED_MINLENGTH;
                // check 4.3.2.c0 must: minLength >= 0
                if (fMinLength < 0)
                    reportError("minLength value '"+facets[INDEX_MINLENGTH]+"' must be a nonNegativeInteger.");
            }
        }
        // maxLength
        if ((presentFacet & DEFINED_MAXLENGTH) != 0) {
            if ((allowedFacet & DEFINED_MAXLENGTH) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MAXLENGTH) == 0) {
                reportError("fixed facet in base");
            } else {
                fMaxLength = ((Integer)facets[INDEX_MAXLENGTH]).intValue();
                fFacetsDefined |= DEFINED_MAXLENGTH;
                if ((fixedFacet & DEFINED_MAXLENGTH) == 0)
                    fFixedFacet |= DEFINED_MAXLENGTH;
                // check 4.3.3.c0 must: maxLength >= 0
                if (fMaxLength < 0)
                    reportError("maxLength value '"+facets[INDEX_MAXLENGTH]+"' must be a nonNegativeInteger.");
            }
        }
        // pattern
        if ((presentFacet & DEFINED_PATTERN) != 0) {
            if ((allowedFacet & DEFINED_PATTERN) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_PATTERN) == 0) {
                reportError("fixed facet in base");
            } else {
                //REVISIT:
                //fPattern.addElement(new RegularExpression((String)facts[INDEX_PATTERN], "X"));
                fPattern.addElement(facets[INDEX_PATTERN]);
                fFacetsDefined |= DEFINED_PATTERN;
                if ((fixedFacet & DEFINED_PATTERN) == 0)
                    fFixedFacet |= DEFINED_PATTERN;
            }
        }

        // enumeration
        if ((presentFacet & DEFINED_ENUMERATION) != 0) {
            if ((allowedFacet & DEFINED_ENUMERATION) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_ENUMERATION) == 0) {
                reportError("fixed facet in base");
            } else {
                String[] enumVals = (String[])facets[INDEX_ENUMERATION];
                fEnumeration = new Object[enumVals.length];
                for (int i = 0; i < fEnumeration.length; i++) {
                    try {
                        fEnumeration[i] = getCompiledValue(enumVals[i]);
                    } catch (InvalidDatatypeValueException ide) {
                        reportError("Value of enumeration '" + enumVals[i] + "' must be from the value space of base");
                        fEnumeration[i] = "";
                        //REVISIT: uncomment it.
                        //fEnumeration[i] = XSDHandler.EMPTY_STRING;
                    }
                }
                fFacetsDefined |= DEFINED_ENUMERATION;
                if ((fixedFacet & DEFINED_ENUMERATION) == 0)
                    fFixedFacet |= DEFINED_ENUMERATION;
            }
        }
        // whiteSpace
        if ((presentFacet & DEFINED_WHITESPACE) != 0) {
            if ((allowedFacet & DEFINED_WHITESPACE) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_WHITESPACE) == 0) {
                reportError("fixed facet in base");
            } else {
                fWhiteSpace = ((Integer)facets[INDEX_WHITESPACE]).shortValue();
                fFacetsDefined |= DEFINED_WHITESPACE;
                if ((fixedFacet & DEFINED_WHITESPACE) == 0)
                    fFixedFacet |= DEFINED_WHITESPACE;
            }
        }
        // maxInclusive
        if ((presentFacet & DEFINED_MAXINCLUSIVE) != 0) {
            if ((allowedFacet & DEFINED_MAXINCLUSIVE) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MAXINCLUSIVE) == 0) {
                reportError("fixed facet in base");
            } else {
                try {
                    fMaxInclusive = getCompiledValue((String)facets[INDEX_MAXINCLUSIVE]);
                    fFacetsDefined |= DEFINED_MAXINCLUSIVE;
                    if ((fixedFacet & DEFINED_MAXINCLUSIVE) == 0)
                        fFixedFacet |= DEFINED_MAXINCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError("maxInclusive value '"+facets[INDEX_MAXINCLUSIVE]+"' is invalid.");
                }
            }
        }
        // maxExclusive
        if ((presentFacet & DEFINED_MAXEXCLUSIVE) != 0) {
            if ((allowedFacet & DEFINED_MAXEXCLUSIVE) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MAXEXCLUSIVE) == 0) {
                reportError("fixed facet in base");
            } else {
                try {
                    fMaxExclusive = getCompiledValue((String)facets[INDEX_MAXEXCLUSIVE]);
                    fFacetsDefined |= DEFINED_MAXEXCLUSIVE;
                    if ((fixedFacet & DEFINED_MAXEXCLUSIVE) == 0)
                        fFixedFacet |= DEFINED_MAXEXCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError("maxExclusive value '"+facets[INDEX_MAXEXCLUSIVE]+"' is invalid.");
                }
            }
        }
        // minExclusive
        if ((presentFacet & DEFINED_MINEXCLUSIVE) != 0) {
            if ((allowedFacet & DEFINED_MINEXCLUSIVE) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MINEXCLUSIVE) == 0) {
                reportError("fixed facet in base");
            } else {
                try {
                    fMinExclusive = getCompiledValue((String)facets[INDEX_MINEXCLUSIVE]);
                    fFacetsDefined |= DEFINED_MINEXCLUSIVE;
                    if ((fixedFacet & DEFINED_MINEXCLUSIVE) == 0)
                        fFixedFacet |= DEFINED_MINEXCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError("minExclusive value '"+facets[INDEX_MINEXCLUSIVE]+"' is invalid.");
                }
            }
        }
        // minInclusive
        if ((presentFacet & DEFINED_MININCLUSIVE) != 0) {
            if ((allowedFacet & DEFINED_MININCLUSIVE) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_MININCLUSIVE) == 0) {
                reportError("fixed facet in base");
            } else {
                try {
                    fMinInclusive = getCompiledValue((String)facets[INDEX_MININCLUSIVE]);
                    fFacetsDefined |= DEFINED_MININCLUSIVE;
                    if ((fixedFacet & DEFINED_MININCLUSIVE) == 0)
                        fFixedFacet |= DEFINED_MININCLUSIVE;
                } catch (InvalidDatatypeValueException ide) {
                    reportError("minInclusive value '"+facets[INDEX_MININCLUSIVE]+"' is invalid.");
                }
            }
        }
        // totalDigits
        if ((presentFacet & DEFINED_TOTALDIGITS) != 0) {
            if ((allowedFacet & DEFINED_TOTALDIGITS) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_TOTALDIGITS) == 0) {
                reportError("fixed facet in base");
            } else {
                fTotalDigits = ((Integer)facets[INDEX_TOTALDIGITS]).intValue();
                fFacetsDefined |= DEFINED_TOTALDIGITS;
                if ((fixedFacet & DEFINED_TOTALDIGITS) == 0)
                    fFixedFacet |= DEFINED_TOTALDIGITS;
                // check 4.3.11.c0 must: totalDigits > 0
                if (fTotalDigits <= 0)
                    reportError("totalDigits value '"+facets[INDEX_TOTALDIGITS]+"' must be a positiveInteger.");
            }
        }
        // fractionDigits
        if ((presentFacet & DEFINED_FRACTIONDIGITS) != 0) {
            if ((allowedFacet & DEFINED_FRACTIONDIGITS) == 0) {
                reportError("non-supported facet");
            } else if ((fFixedFacet & DEFINED_FRACTIONDIGITS) == 0) {
                reportError("fixed facet in base");
            } else {
                fFractionDigits = ((Integer)facets[INDEX_FRACTIONDIGITS]).intValue();
                fFacetsDefined |= DEFINED_FRACTIONDIGITS;
                if ((fixedFacet & DEFINED_FRACTIONDIGITS) == 0)
                    fFixedFacet |= DEFINED_FRACTIONDIGITS;
                // check 4.3.12.c0 must: fractionDigits >= 0
                if (fFractionDigits < 0)
                    reportError("fractionDigits value '"+facets[INDEX_FRACTIONDIGITS]+"' must be a positiveInteger.");
            }
        }
        // token type: internal use, so do less checking
        if ((presentFacet & DEFINED_TOKENTYPE) != 0) {
            fTokeyType = ((Integer)facets[INDEX_TOKENTYPE]).shortValue();
            fFacetsDefined |= DEFINED_TOKENTYPE;
        }


        // step 3: check facets against each other: length, bounds

        if(fFacetsDefined != 0){

            // check 4.3.1.c1 error: length & (maxLength | minLength)
            if((fFacetsDefined & DEFINED_LENGTH) != 0 ){
              if( (fFacetsDefined & DEFINED_MINLENGTH) != 0 ){
                reportError("it is an error for both length and min length to be present." );
              }
              else if((fFacetsDefined & DEFINED_MAXLENGTH) != 0 ){
                reportError("it is an error for both length and max length to be present." );
              }
            }

            // check 4.3.2.c1 must: minLength <= maxLength
            if(((fFacetsDefined & DEFINED_MINLENGTH ) != 0 ) && ((fFacetsDefined & DEFINED_MAXLENGTH) != 0))
            {
              if(fMinLength > fMaxLength)
                reportError("value of minLength = " + fMinLength + "must  be less than value of maxLength = "+ fMaxLength);
            }

            // check 4.3.8.c1 error: maxInclusive + maxExclusive
            if (((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                reportError( "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." );
            }

            // check 4.3.9.c1 error: minInclusive + minExclusive
            if (((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) &&
              ((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
                reportError("It is an error for both minInclusive and minExclusive to be specified for the same datatype." );
            }

            // check 4.3.7.c1 must: minInclusive <= maxInclusive
            if (((fFacetsDefined &  DEFINED_MAXINCLUSIVE) != 0) &&
            ((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
              result =  fDVs[fValidateDV].compare(fMinInclusive, fMaxInclusive);
              if (result == 1 || result == INDETERMINATE){
                reportError("minInclusive value ='" + getStringValue(fMinInclusive) + "'must be <= maxInclusive value ='" +
                    getStringValue(fMaxInclusive) + "'. " );
              }
            }

            // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
            if (((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0)) {
              result =  fDVs[fValidateDV].compare(fMinExclusive, fMaxExclusive);
              if (result == 1 || result == INDETERMINATE)
                reportError( "minExclusive value ='" + getStringValue(fMinExclusive) + "'must be <= maxExclusive value ='" +
                                                        getStringValue(fMaxExclusive) + "'. " );
            }

            // check 4.3.9.c2 must: minExclusive < maxInclusive
            if (((fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0) && ((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0)) {
              if (fDVs[fValidateDV].compare(fMinExclusive, fMaxInclusive) != -1)
                reportError( "minExclusive value ='" + getStringValue(fMinExclusive) + "'must be > maxInclusive value ='" +
                                                                     getStringValue(fMaxInclusive) + "'. " );
            }

            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if (((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
              if (fDVs[fValidateDV].compare(fMinInclusive, fMaxExclusive) != -1)
                reportError( "minInclusive value ='" + getStringValue(fMinInclusive) + "'must be < maxExclusive value ='" +
                                                                     getStringValue(fMaxExclusive) + "'. " );
            }

            // check 4.3.12.c1 must: fractionDigits <= totalDigits
            if (((fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0) &&
                ((fFacetsDefined & DEFINED_TOTALDIGITS) != 0)) {
                if (fFractionDigits > fTotalDigits)
                    reportError( "fractionDigits value ='" + this.fFractionDigits + "'must be <= totalDigits value ='" +
                                                             this.fTotalDigits + "'. " );
            }

            // step 4: check facets against base
            if (base != null) {

                // check 4.3.1.c1 error: length & (base.maxLength | base.minLength)
                if ( ((fFacetsDefined & DEFINED_LENGTH ) != 0 ) ) {

                    if ( ((base.fFacetsDefined & DEFINED_MAXLENGTH ) != 0 ) ) {
                        reportError("It is an error for both length and maxLength to be members of facets." );
                    }
                    else if ( ((base.fFacetsDefined & DEFINED_MINLENGTH ) != 0 ) ) {
                        reportError("It is an error for both length and minLength to be members of facets." );
                    }
                    else if ( (base.fFacetsDefined & DEFINED_LENGTH) != 0 ) {
                        // check 4.3.1.c2 error: length != base.length
                        if ( fLength != base.fLength )
                            reportError( "Value of length = '" + fLength +
                                                                     "' must be = the value of base.length = '" + base.fLength + "'.");
                    }
                }

                // check 4.3.1.c1 error: base.length & (maxLength | minLength)
                if ( ((base.fFacetsDefined & DEFINED_LENGTH ) != 0 ) ) {
                    if ( ((fFacetsDefined & DEFINED_MAXLENGTH ) != 0 ) ) {
                        reportError("It is an error for both length and maxLength to be members of facets." );
                    }
                    else if ( ((fFacetsDefined & DEFINED_MINLENGTH ) != 0 ) ) {
                        reportError("It is an error for both length and minLength to be members of facets." );
                    }
                }



                // check 4.3.2.c1 must: minLength <= base.maxLength
                if ( ((fFacetsDefined & DEFINED_MINLENGTH ) != 0 ) ) {
                    if ( (base.fFacetsDefined & DEFINED_MAXLENGTH ) != 0 ) {
                        if ( fMinLength > base.fMaxLength ) {
                            reportError( "Value of minLength = '" + fMinLength +
                                                                     "'must be <= the value of maxLength = '" + fMaxLength + "'.");
                        }
                    }
                    else if ( (base.fFacetsDefined & DEFINED_MINLENGTH) != 0 ) {
                        if ( (base.fFixedFacet & DEFINED_MINLENGTH) != 0 && fMinLength != base.fMinLength ) {
                            reportError( "minLength value = '" + fMinLength +
                                                                     "' must be equal to base.minLength value = '" +
                                                                     base.fMinLength + "' with attribute {fixed} = true" );
                        }

                        // check 4.3.2.c2 error: minLength < base.minLength
                        if ( fMinLength < base.fMinLength ) {
                            reportError( "Value of minLength = '" + fMinLength +
                                                                     "' must be >= the value of base.minLength = '" + base.fMinLength + "'.");
                        }
                    }
                }


                // check 4.3.2.c1 must: maxLength < base.minLength
                if ( ((fFacetsDefined & DEFINED_MAXLENGTH ) != 0 ) && ((base.fFacetsDefined & DEFINED_MINLENGTH ) != 0 ))
                {
                    if ( fMaxLength < base.fMinLength) {
                        reportError( "Value of maxLength = '" + fMaxLength +
                                                                 "'must be >= the value of base.minLength = '" + base.fMinLength + "'.");
                    }
                }

                // check 4.3.3.c1 error: maxLength > base.maxLength
                if ( (fFacetsDefined & DEFINED_MAXLENGTH) != 0 ) {
                    if ( (base.fFacetsDefined & DEFINED_MAXLENGTH) != 0 ){
                        if(( (base.fFixedFacet & DEFINED_MAXLENGTH) != 0 )&& fMaxLength != base.fMaxLength ) {
                            reportError( "maxLength value = '" + fMaxLength +
                                                                 "' must be equal to base.maxLength value = '" +
                                                                 base.fMaxLength + "' with attribute {fixed} = true" );
                        }
                        if ( fMaxLength > base.fMaxLength ) {
                            reportError( "Value of maxLength = '" + fMaxLength +
                                                                     "' must be <= the value of base.maxLength = '" + base.fMaxLength + "'.");
                        }
                    }
                }


                // check 4.3.7.c2 error:
                // maxInclusive > base.maxInclusive
                // maxInclusive >= base.maxExclusive
                // maxInclusive < base.minInclusive
                // maxInclusive <= base.minExclusive

                if (((fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                    if (((base.fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                        result = fDVs[fValidateDV].compare(fMaxInclusive, base.fMaxInclusive);

                        if ((base.fFixedFacet & DEFINED_MAXINCLUSIVE) != 0 &&
                            result != 0) {
                                reportError( "maxInclusive value = '" + getStringValue(fMaxInclusive) +
                                                                     "' must be equal to base.maxInclusive value = '" +
                                                                     getStringValue(base.fMaxInclusive) + "' with attribute {fixed} = true" );
                        }
                        if (result == 1 || result == INDETERMINATE) {
                            reportError( "maxInclusive value ='" + getStringValue(fMaxInclusive) + "' must be <= base.maxInclusive value ='" +
                                                                     getStringValue(base.fMaxInclusive) + "'." );
                        }
                    }
                    if (((base.fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMaxInclusive, base.fMaxExclusive) != -1){
                            reportError(
                                                               "maxInclusive value ='" + getStringValue(fMaxInclusive) + "' must be < base.maxExclusive value ='" +
                                                               getStringValue(base.fMaxExclusive) + "'." );
                    }

                    if ((( base.fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
                        result = fDVs[fValidateDV].compare(fMaxInclusive, base.fMinInclusive);
                        if (result == -1 || result == INDETERMINATE) {
                            reportError( "maxInclusive value ='" + getStringValue(fMaxInclusive) + "' must be >= base.minInclusive value ='" +
                                                                     getStringValue(base.fMinInclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMaxInclusive, base.fMinExclusive ) != 1)
                        reportError(
                                                               "maxInclusive value ='" + getStringValue(fMaxInclusive) + "' must be > base.minExclusive value ='" +
                                                               getStringValue(base.fMinExclusive) + "'." );
                }

                // check 4.3.8.c3 error:
                // maxExclusive > base.maxExclusive
                // maxExclusive > base.maxInclusive
                // maxExclusive <= base.minInclusive
                // maxExclusive <= base.minExclusive
                if (((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0)) {
                    if ((( base.fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0)) {
                        result= fDVs[fValidateDV].compare(fMaxExclusive, base.fMaxExclusive);

                        if ((base.fFixedFacet & DEFINED_MAXEXCLUSIVE) != 0 &&  result != 0) {
                            reportError( "maxExclusive value = '" + getStringValue(fMaxExclusive) +
                                                                     "' must be equal to base.maxExclusive value = '" +
                                                                     getStringValue(base.fMaxExclusive) + "' with attribute {fixed} = true" );
                        }
                        if (result == 1 || result == INDETERMINATE) {
                            reportError( "maxExclusive value ='" + getStringValue(fMaxExclusive) + "' must be < base.maxExclusive value ='" +
                                                                     getStringValue(base.fMaxExclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                        result= fDVs[fValidateDV].compare(fMaxExclusive, base.fMaxInclusive);
                        if (result == 1 || result == INDETERMINATE) {
                            reportError( "maxExclusive value ='" + getStringValue(fMaxExclusive) + "' must be <= base.maxInclusive value ='" +
                                                                     getStringValue(base.fMaxInclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMaxExclusive, base.fMinExclusive ) != 1)
                        reportError( "maxExclusive value ='" + getStringValue(fMaxExclusive) + "' must be > base.minExclusive value ='" +
                                                                 getStringValue(base.fMinExclusive) + "'." );

                    if ((( base.fFacetsDefined & DEFINED_MININCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMaxExclusive, base.fMinInclusive) != 1)
                        reportError( "maxExclusive value ='" + getStringValue(fMaxExclusive) + "' must be > base.minInclusive value ='" +
                                                                 getStringValue(base.fMinInclusive) + "'." );
                }

                // check 4.3.9.c3 error:
                // minExclusive < base.minExclusive
                // maxInclusive > base.maxInclusive
                // minInclusive < base.minInclusive
                // maxExclusive >= base.maxExclusive
                if (((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0)) {
                    if ((( base.fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0)) {

                        result= fDVs[fValidateDV].compare(fMinExclusive, base.fMinExclusive);
                        if ((base.fFixedFacet & DEFINED_MINEXCLUSIVE) != 0 &&
                            result != 0) {
                            reportError( "minExclusive value = '" + getStringValue(fMinExclusive) +
                                                                     "' must be equal to base.minExclusive value = '" +
                                                                     getStringValue(base.fMinExclusive) + "' with attribute {fixed} = true" );
                        }
                        if (result == -1 || result == INDETERMINATE) {
                            reportError( "minExclusive value ='" + getStringValue(fMinExclusive) + "' must be >= base.minExclusive value ='" +
                                                                     getStringValue(base.fMinExclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                        result=fDVs[fValidateDV].compare(fMinExclusive, base.fMaxInclusive);

                        if (result == 1 || result == INDETERMINATE) {
                            reportError(
                                                                   "minExclusive value ='" + getStringValue(fMinExclusive) + "' must be <= base.maxInclusive value ='" +
                                                                   getStringValue(base.fMaxInclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
                        result = fDVs[fValidateDV].compare(fMinExclusive, base.fMinInclusive);

                        if (result == -1 || result == INDETERMINATE) {
                            reportError(
                                                                   "minExclusive value ='" + getStringValue(fMinExclusive) + "' must be >= base.minInclusive value ='" +
                                                                   getStringValue(base.fMinInclusive) + "'." );
                        }
                    }

                    if ((( base.fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMinExclusive, base.fMaxExclusive) != -1)
                        reportError( "minExclusive value ='" + getStringValue(fMinExclusive) + "' must be < base.maxExclusive value ='" +
                                                                 getStringValue(base.fMaxExclusive) + "'." );
                }

                // check 4.3.10.c2 error:
                // minInclusive < base.minInclusive
                // minInclusive > base.maxInclusive
                // minInclusive <= base.minExclusive
                // minInclusive >= base.maxExclusive
                if (((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
                    if (((base.fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
                        result = fDVs[fValidateDV].compare(fMinInclusive, base.fMinInclusive);

                        if ((base.fFixedFacet & DEFINED_MININCLUSIVE) != 0 &&
                            result != 0) {
                            reportError( "minInclusive value = '" + getStringValue(fMinInclusive) +
                                                                     "' must be equal to base.minInclusive value = '" +
                                                                     getStringValue(base.fMinInclusive) + "' with attribute {fixed} = true" );
                        }
                        if (result == -1 || result == INDETERMINATE) {
                            reportError( "minInclusive value ='" + getStringValue(fMinInclusive) + "' must be >= base.minInclusive value ='" +
                                                                     getStringValue(base.fMinInclusive) + "'." );
                        }
                    }
                    if ((( base.fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
                        result=fDVs[fValidateDV].compare(fMinInclusive, base.fMaxInclusive);
                        if (result == 1 || result == INDETERMINATE) {
                            reportError( "minInclusive value ='" + getStringValue(fMinInclusive) + "' must be <= base.maxInclusive value ='" +
                                                                     getStringValue(base.fMaxInclusive) + "'." );
                        }
                    }
                    if ((( base.fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMinInclusive, base.fMinExclusive ) != 1)
                        reportError( "minInclusive value ='" + getStringValue(fMinInclusive) + "' must be > base.minExclusive value ='" +
                                                                 getStringValue(base.fMinExclusive) + "'." );
                    if ((( base.fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) &&
                        fDVs[fValidateDV].compare(fMinInclusive, base.fMaxExclusive) != -1)
                        reportError( "minInclusive value ='" + getStringValue(fMinInclusive) + "' must be < base.maxExclusive value ='" +
                                                                 getStringValue(base.fMaxExclusive) + "'." );
                }

                // check 4.3.11.c1 error: totalDigits > base.totalDigits
                if (((fFacetsDefined & DEFINED_TOTALDIGITS) != 0)) {
                    if ((( base.fFacetsDefined & DEFINED_TOTALDIGITS) != 0)) {
                        if ((base.fFixedFacet & DEFINED_TOTALDIGITS) != 0 &&  fTotalDigits != base.fTotalDigits) {
                            reportError("totalDigits value = '" + fTotalDigits +
                                                                    "' must be equal to base.totalDigits value = '" +
                                                                    base.fTotalDigits +
                                                                    "' with attribute {fixed} = true" );
                        }
                        if (fTotalDigits > base.fTotalDigits) {
                            reportError( "totalDigits value ='" + fTotalDigits + "' must be <= base.totalDigits value ='" +
                                                                     base.fTotalDigits + "'." );
                        }
                    }
                }

                // check fixed value for fractionDigits
                if (((fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0)) {
                    if ((( base.fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0)) {
                        if ((base.fFixedFacet & DEFINED_FRACTIONDIGITS) != 0 && fFractionDigits != base.fFractionDigits) {
                            reportError("fractionDigits value = '" + fFractionDigits +
                                                                    "' must be equal to base.fractionDigits value = '" +
                                                                    base.fFractionDigits +
                                                                    "' with attribute {fixed} = true" );
                        }
                    }
                }


                //REVISIT: we dont have any check for enumeration ??, do we need such a check.
                if(((fFacetsDefined & DEFINED_ENUMERATION) != 0)  && (fEnumeration != null) &&
                ((base.fFacetsDefined & DEFINED_ENUMERATION) != 0)  && (base.fEnumeration != null)){
                    // we expect that it will have compiled value
                    boolean found = false;
                    for(int i =0 ; i < fEnumeration.length  ; i++){
                        for( int j = 0 ; j < base.fEnumeration.length  ; j++){
                            if( isEqual(fEnumeration[i] , base.fEnumeration[j])){
                                found = true;
                                break; // we found the value
                            }
                        }
                    }

                  if(!found){
                    reportError("values of enumeration should be from the value space of base.");
                   }
                }

                // check 4.3.6.c1 error:
                // (whiteSpace = preserve || whiteSpace = replace) && base.whiteSpace = collapese or
                // whiteSpace = preserve && base.whiteSpace = replace

                if ( (fFacetsDefined & DEFINED_WHITESPACE) != 0 && (base.fFacetsDefined & DEFINED_WHITESPACE) != 0 ){
                    if ( (fFixedFacet & DEFINED_WHITESPACE) != 0 &&  fWhiteSpace != base.fWhiteSpace ) {
                        reportError( "whiteSpace value = '" + whiteSpaceValue(fWhiteSpace) +
                                                             "' must be equal to base.whiteSpace value = '" +
                                                             whiteSpaceValue(base.fWhiteSpace) + "' with attribute {fixed} = true" );
                    }

                    if ( (fWhiteSpace == PRESERVE || fWhiteSpace == REPLACE) &&  base.fWhiteSpace == COLLAPSE ){
                        reportError( "It is an error if whiteSpace = 'preserve' or 'replace' and base.whiteSpace = 'collapse'.");
                    }
                    if ( fWhiteSpace == PRESERVE &&  base.fWhiteSpace == REPLACE ){
                        reportError( "It is an error if whiteSpace = 'preserve' and base.whiteSpace = 'replace'.");
                    }
                }

            }//base != null
        }//fFacetsDefined != null

        // REVISIT: fTokeyType
        // step 5: inherit other facets from base (including fTokeyType)

        // inherit length
        if ( (fFacetsDefined & DEFINED_LENGTH) == 0  && (base.fFacetsDefined & DEFINED_LENGTH) != 0 ) {
                fFacetsDefined |= DEFINED_LENGTH;
                fLength = base.fLength;
        }
        // inherit minLength
        if ( (fFacetsDefined & DEFINED_MINLENGTH) == 0 && (base.fFacetsDefined & DEFINED_MINLENGTH) != 0 ) {
                fFacetsDefined |= DEFINED_MINLENGTH;
                fMinLength = base.fMinLength;
        }
        // inherit maxLength
        if ((fFacetsDefined & DEFINED_MAXLENGTH) == 0 &&  (base.fFacetsDefined & DEFINED_MAXLENGTH) != 0 ) {
                fFacetsDefined |= DEFINED_MAXLENGTH;
                fMaxLength = base.fMaxLength;
        }
        // inherit enumeration
        if ( (fFacetsDefined & DEFINED_ENUMERATION) == 0 && (base.fFacetsDefined & DEFINED_ENUMERATION) != 0 ) {
            fFacetsDefined |= DEFINED_ENUMERATION;
            fEnumeration = base.fEnumeration;
        }
        //REVISIT:
        // inherit whiteSpace
        if ( (fFacetsDefined & DEFINED_WHITESPACE) == 0 &&  (base.fFacetsDefined & DEFINED_WHITESPACE) != 0 ) {
            fFacetsDefined |= DEFINED_WHITESPACE;
            fWhiteSpace = base.fWhiteSpace;
        }
        // inherit enumeration
        if ((fFacetsDefined & DEFINED_ENUMERATION) == 0 && (base.fFacetsDefined & DEFINED_ENUMERATION) != 0) {
            fFacetsDefined |= DEFINED_ENUMERATION;
            fEnumeration = base.fEnumeration;
        }

        // inherit maxExclusive
        if ((( base.fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) &&
            !((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
            fFacetsDefined |= DEFINED_MAXEXCLUSIVE;
            fMaxExclusive = base.fMaxExclusive;
        }
        // inherit maxInclusive
        if ((( base.fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0) &&
            !((fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0)) {
            fFacetsDefined |= DEFINED_MAXINCLUSIVE;
            fMaxInclusive = base.fMaxInclusive;
        }
        // inherit minExclusive
        if ((( base.fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) &&
            !((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) && !((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
            fFacetsDefined |= DEFINED_MINEXCLUSIVE;
            fMinExclusive = base.fMinExclusive;
        }
        // inherit minExclusive
        if ((( base.fFacetsDefined & DEFINED_MININCLUSIVE) != 0) &&
            !((fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0) && !((fFacetsDefined & DEFINED_MININCLUSIVE) != 0)) {
            fFacetsDefined |= DEFINED_MININCLUSIVE;
            fMinInclusive = base.fMinInclusive;
        }
        // inherit totalDigits
        if ((( base.fFacetsDefined & DEFINED_TOTALDIGITS) != 0) &&
            !((fFacetsDefined & DEFINED_TOTALDIGITS) != 0)) {
            fFacetsDefined |= DEFINED_TOTALDIGITS;
            fTotalDigits = base.fTotalDigits;
        }
        // inherit fractionDigits
        if ((( base.fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0)
            && !((fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0)) {
            fFacetsDefined |= DEFINED_FRACTIONDIGITS;
            fFractionDigits = base.fFractionDigits;
        }


    } //init4Restriction()

    /**
     * If <list> is chosen
     */
    public void init4List(XSSimpleTypeDecl itemType) {
        fVariety = VARIETY_LIST;
        fItemType = itemType;
    }

    /**
     * If <union> is chosen
     */
     //REVISIT:it will impact SimpleTypeTraverser
    public void init4Union(XSSimpleTypeDecl [] memberTypes) {
        fVariety = VARIETY_UNION;
        fMemberTypes = memberTypes;
    }


    /**
     * validate a value, and return the compiled form
     */
    public Object validate(String content, ValidateContext context) throws InvalidDatatypeValueException {
        Object retVal = content;

        switch (fVariety) {
        case VARIETY_ATOMIC:
            retVal = validateRestriction(content, context);
            break;
        case VARIETY_LIST:
            retVal = validateList(content, context);
            break;
        case VARIETY_UNION:
            retVal = validateUnion(content, context);
            break;
        }

        return retVal;
    }// validate()

    public Object validateRestriction(String content, ValidateContext context) {
        // step 1: validate the value against the facets. we need to use the
        //         get***, compare, isEqual methods from TypeValidator
        Object ob = null;

        //REVISIT: use of fValidateDV, reporting  errors

        try
        {
            ob = getCompiledValue(content);
        }
        catch (InvalidDatatypeValueException ide) {
                        reportError("Value of content '" + content + "' must be from the value space of base");
        }


        // REVISIT: check it again
        if ( (fFacetsDefined & DEFINED_PATTERN ) != 0 ) {
            Enumeration keys = fPattern.elements() ;
            boolean matched = false;
            RegularExpression regex = new RegularExpression("X");
            while(keys.hasMoreElements() ){
                String pattern = (String)keys.nextElement();
                if (regex != null && regex.matches(pattern)){
                    matched = true;
                    break;
                }
            }
            if(!matched)
                    reportError("Value '"+content+
                                                            "' does not match regular expression facet '" + fPattern + "'." );

        }

        int length = fDVs[fValidateDV].getDataLength(ob);

	      // maxLength
        if ( (fFacetsDefined & DEFINED_MAXLENGTH) != 0 ) {
            if ( length > fMaxLength ) {
                reportError("Value '"+content+
                                                        "' with length '"+length+
                                                        "' exceeds maximum length facet of '"+fMaxLength+"'.");
            }
        }

	      //minLength
        if ( (fFacetsDefined & DEFINED_MINLENGTH) != 0 ) {
            if ( length < fMinLength ) {
                reportError("Value '"+content+
                                                        "' with length '"+length+
                                                        "' is less than minimum length facet of '"+fMinLength+"'." );
            }
        }

	      //length
        if ( (fFacetsDefined & DEFINED_LENGTH) != 0 ) {
            if ( length != fLength ) {
                reportError("Value '"+content+
                                                        "' with length '"+length+
                                                        "' is not equal to length facet '"+fLength+"'.");
            }
        }

	      //enumeration

        if ( ((fFacetsDefined & DEFINED_ENUMERATION) != 0 )&&
             (fEnumeration != null) ) {
            boolean present = false;
            for (int i = 0; i < fEnumeration.length; i++) {
              if (isEqual(ob,fEnumeration[i])) {
                  present = true;
                  break;
              }
            }
            if(!present){
               String msg = getErrorString(
                                         DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.NOT_ENUM_VALUE],
                                         new Object [] { ob});
                reportError(msg);
            }
        }



	      //fractionDigits
        if ((fFacetsDefined & DEFINED_FRACTIONDIGITS) != 0) {
        int scale = fDVs[fValidateDV].getFractionDigits(ob);
            if (scale > fFractionDigits) {
                String msg = getErrorString(
                                           DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.FRACTION_EXCEEDED],
                                           new Object[] {
                                               "'" + content + "'" + " with fractionDigits = '"+ scale +"'",
                                               "'" + fFractionDigits + "'"
                                           });
                reportError(msg);
            }
        }

	      //totalDigits
        if ((fFacetsDefined & DEFINED_TOTALDIGITS)!=0) {
            int totalDigits = fDVs[fValidateDV].getTotalDigits(ob);
            if (totalDigits > fTotalDigits) {

                String msg = getErrorString(
                                           DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.TOTALDIGITS_EXCEEDED],
                                           new Object[] {
                                               "'" + content + "'" + " with totalDigits = '"+ totalDigits +"'",
                                               "'" + fTotalDigits + "'"
                                           });
                reportError(msg);
            }
        }


        // REVISIT this part for error reporting

	      boolean minOk = true;
        boolean maxOk = true;
        String  upperBound="";

        String  lowerBound="";
        String  lowerBoundIndicator = "";
        String  upperBoundIndicator = "";
        int compare;

        //maxinclusive
        if ( (fFacetsDefined & DEFINED_MAXINCLUSIVE) != 0 ) {
            compare = fDVs[fValidateDV].compare(ob, fMaxInclusive);
            maxOk =  (compare == 1) ? false:true;
            upperBound   = fMaxInclusive.toString();
            if ( upperBound != null ) {
                upperBoundIndicator = "<=";
            }
            else {
                upperBound="";
            }
        }

        //maxExclusive
        if ( (fFacetsDefined & DEFINED_MAXEXCLUSIVE) != 0 ) {
            compare = fDVs[fValidateDV].compare(ob,  fMaxExclusive );
            maxOk = (compare==-1)?true:false;
            upperBound = fMaxExclusive.toString();
            if ( upperBound != null ) {
                upperBoundIndicator = "<";
            }
            else {
                upperBound = "";
            }
        }

        //minInclusive
        if ( (fFacetsDefined & DEFINED_MININCLUSIVE) != 0 ) {
            compare = fDVs[fValidateDV].compare(ob, fMinInclusive);
            minOk = (compare==-1)?false:true;
            lowerBound = fMinInclusive.toString();
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<=";
            }
            else {
                lowerBound = "";
            }
        }

        //minExclusive
        if ( (fFacetsDefined & DEFINED_MINEXCLUSIVE) != 0 ) {
            compare = fDVs[fValidateDV].compare(ob, fMinExclusive);
            minOk = (compare==1)?true:false;
            lowerBound = fMinExclusive.toString();
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<";
            }
            else {
                lowerBound = "";
            }
        }

        if ( !(minOk && maxOk) ){

            String msg = getErrorString(
                DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.OUT_OF_BOUNDS],
                new Object [] { ob.toString(), lowerBound, upperBound, lowerBoundIndicator, upperBoundIndicator});
            reportError(msg);
        }


        // step 2: validate the value against token_type, if there is one specified.

        // validate special kinds of token, in place of old pattern matching
        if (fTokeyType != TOKEN_NONE.shortValue() ) {

        boolean seenErr = false;
        if (fTokeyType == TOKEN_NMTOKEN.shortValue() ) {

            seenErr = !XMLChar.isValidNmtoken(content);
        }
        else if (fTokeyType == TOKEN_NAME.shortValue() ||
                   fTokeyType == TOKEN_IDNAME.shortValue() ||
                   fTokeyType == TOKEN_IDREFNAME.shortValue()) {

            seenErr = !XMLChar.isValidName(content);
        } else if (fTokeyType == TOKEN_NCNAME.shortValue() ||
                   fTokeyType == TOKEN_IDNCNAME.shortValue() ||
                   fTokeyType == TOKEN_IDREFNCNAME.shortValue() ||
                   fTokeyType == TOKEN_ENTITY.shortValue() ) {

            // REVISIT: !!!NOT IMPLEMENTED in XMLChar
            seenErr = !XMLChar.isValidNCName(content);
        }
        if (seenErr) {
            reportError(
                            "Value '"+content+"' is not a valid " + fTokeyType);
        }

        }

        // step 3: call fValidateDV for extra validation. this is useful
        //         for ID/IDREF/ENTITY/QNAME, etc.


        if(context != null){

        /* REVISIT: validateContext

        */
        }// step 3

        return ob;
    }// validateRestriction()

    private String getErrorString(String key, Object args[]) {
        try {
            return fMessageProvider.formatMessage(fLocale, key, args);
        }
        catch ( Exception e ) {
            return "Illegal Errorcode "+key;
        }
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    private boolean isEqual(Object value1 , Object value2){
        boolean retVal = false;
        short variety = fVariety;

        switch(variety){

        case VARIETY_ATOMIC:
           retVal =  fDVs[fValidateDV].isEqual(value1,value2);
        break;

        case VARIETY_LIST:
            //in case of list compiled values would be string only.
            StringTokenizer tokens1 = new StringTokenizer((String)value1);
            StringTokenizer tokens2 = new StringTokenizer((String)value2);

            if(tokens1.countTokens() != tokens2.countTokens() )
                return false;

            XSSimpleTypeDecl itemType = fItemType;
            variety = itemType.fVariety;
            //
            if(itemType.fVariety == VARIETY_ATOMIC){

                int count = tokens1.countTokens() ;

                for(int i = 0 ; i < count ; i ++){
                    Object content1 = null;
                    Object content2 = null;

                    //getCompiledValues first, for list we pass string as it is
                    try{
                        content1 = fDVs[itemType.fValidateDV].getCompiledValue(tokens1.nextToken() );
                    }catch(InvalidDatatypeValueException invalid){
                        reportError("content = " + tokens1.nextToken() + " is not from the value space of base." );
                        return false;
                    }
                    try{
                        content2 = fDVs[itemType.fValidateDV].getCompiledValue(tokens2.nextToken() );
                    }catch(InvalidDatatypeValueException invalid){
                        reportError("content = " + tokens2.nextToken() + " is not from the value space of base." );
                        return false;
                    }
                    if(! fDVs[itemType.fValidateDV].isEqual(content1,content2)) {
                        return false;
                    }
                }//end of loop

                //everything went fine.
                retVal = true;

            }
            else if(itemType.fVariety == VARIETY_UNION){
                XSSimpleTypeDecl [] memberTypes = itemType.fMemberTypes ;
                //REVISIT: still to implement
            }//REVISIT:reportError if its neither atomic or union.


        break;

        case VARIETY_UNION:
        //REVISIT: still to implement
        break;

        }
        return retVal;
    }//isEqual()


    private Object getCompiledValue(String content) throws InvalidDatatypeValueException{

        if(fVariety == VARIETY_ATOMIC)
            return fDVs[fValidateDV].getCompiledValue(content);

        else if(fVariety == VARIETY_LIST){
            StringTokenizer parsedList = new StringTokenizer(content);
            int countOfTokens = parsedList.countTokens() ;
            for(int i = 0 ; i < countOfTokens ; i ++){
                fItemType.validate(parsedList.nextToken(),null );
            }
        }

        else if(fVariety == VARIETY_UNION){
            for(int i = 0 ; i < fMemberTypes.length ; i++){
                fMemberTypes[i].validate(content,null);
            }

        }

        return content;

    }//getCompiledValue()

    private Object validateList(String content, ValidateContext context)throws InvalidDatatypeValueException{

        StringTokenizer parsedList = new StringTokenizer( content );
        int numberOfTokens = parsedList.countTokens();
        XSSimpleTypeDecl itemType = fItemType;

        // REVISIT: check it again
        if ( (fFacetsDefined & DEFINED_PATTERN ) != 0 ) {
            Enumeration keys = fPattern.elements() ;
            boolean matched = false;
            RegularExpression regex = new RegularExpression("X");
            while(keys.hasMoreElements() ){
                String pattern = (String)keys.nextElement();
                if (regex != null && regex.matches(pattern)){
                    matched = true;
                    break;
                }
            }
            if(!matched)
                    reportError("Value '"+content+
                                                            "' does not match regular expression facet '" + fPattern + "'." );

        }

        if ( (fFacetsDefined & DEFINED_MAXLENGTH) != 0 ) {
            if ( numberOfTokens > fMaxLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                "' with length ='"+  numberOfTokens + "' tokens"+
                "' exceeds maximum length facet of '"+fMaxLength+"' tokens.");
            }
        }
        if ( (fFacetsDefined & DEFINED_MINLENGTH) != 0 ) {
            if ( numberOfTokens < fMinLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                "' with length ='"+ numberOfTokens+ "' tokens" +
                "' is less than minimum length facet of '"+fMinLength+"' tokens." );
            }
        }

        if ( (fFacetsDefined & DEFINED_LENGTH) != 0 ) {
            if ( numberOfTokens != fLength ) {
                throw new InvalidDatatypeValueException("Value '"+content+
                "' with length ='"+ numberOfTokens+ "' tokens" +
                "' is not equal to length facet of '"+fLength+"' tokens.");
            }
        }

        if ((fFacetsDefined & DEFINED_ENUMERATION) != 0 ){
            boolean present = false;
            if(fEnumeration != null){
                for(int i = 0 ; i < fEnumeration.length ; i++){
                    if(isEqual(content,fEnumeration[i])){
                        present = true;
                        break;
                    }
                }
                if(!present){
                    //REVISIT: error handling
                    String msg = getErrorString(
                            DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.NOT_ENUM_VALUE],
                            new Object [] { fEnumeration });
                        throw new InvalidDatatypeValueException(msg);

                }
            }
        }


        if(itemType.fVariety == VARIETY_ATOMIC){

            for(int i = 0 ; i < numberOfTokens ; i++ )
            {
                try
                {   //check if the values are of item type
                    itemType.validate(parsedList.nextToken(),context);
                }
                catch(InvalidDatatypeValueException invalidValue){
                    //REVISIT: should we check all tokens and report error or 'get out of loop' when we get exception.
                    reportError(invalidValue.getMessage() );
                }
            }
        }
        else if(itemType.fVariety == VARIETY_UNION){
            itemType.validate(content,context);
        }

        return content;
    } //validateList()


    private Object validateUnion(String content, ValidateContext context)throws InvalidDatatypeValueException {

        XSSimpleTypeDecl [] memberTypes = fMemberTypes;

        // REVISIT: check it again
        if ( (fFacetsDefined & DEFINED_PATTERN ) != 0 ) {
            Enumeration keys = fPattern.elements() ;
            boolean matched = false;
            RegularExpression regex = new RegularExpression("X");
            while(keys.hasMoreElements() ){
                String pattern = (String)keys.nextElement();
                if (regex != null && regex.matches(pattern)){
                    matched = true;
                    break;
                }
            }
            if(!matched)
                    reportError("Value '"+content+
                                                            "' does not match regular expression facet '" + fPattern + "'." );

        }


        if ((fFacetsDefined & DEFINED_ENUMERATION) != 0 ){
            boolean present = false;
            if(fEnumeration != null){
                for(int i = 0 ; i < fEnumeration.length ; i++){
                    if(isEqual(content,fEnumeration[i])){
                        present = true;
                        break;
                    }
                }
                if(!present){
                    //REVISIT: error handling
                    String msg = getErrorString(
                            DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.NOT_ENUM_VALUE],
                            new Object [] { fEnumeration });
                        throw new InvalidDatatypeValueException(msg);

                }
            }
        }

        if(memberTypes != null){
            int noOfMemberTypes = memberTypes.length;
            boolean exceptionOccured = false;
            for(int i = 0 ; i < noOfMemberTypes ;  i ++){
                try{
                    //it should throw exception if anything goes wrong
                    memberTypes[i].validate(content,context);
                }
                catch(InvalidDatatypeValueException invalidValue){
                    exceptionOccured = true;
                }
                if(!exceptionOccured)
                    break;
            }
            //REVISIT:error handling
            if(exceptionOccured){
                String msg  = " content = " + content + " doesnt't match any of the member types " ;
                throw new InvalidDatatypeValueException(msg);
            }
        }
    return content;
    }//validateUnion()

    void reportError(String msg) {
        // REVISIT: how do we report datatype facet errors?
        // passing ErrorReporter in? return an array of errors back?
    }


    //REVISIT: error handling
    private String whiteSpaceValue(short ws){
      if(ws != REPLACE)
        return (ws == PRESERVE)?"preserve":"collapse" ;
      return "replace";

    }

    public String getStringValue(Object value){
        if(value != null)
            return value.toString() ;
        else
            return null;
    }


    public class ValidateContext {
        //REVISIT: still to complete and step 3 of validateRestriction
        //XSGrammarResolver grammarResolver;

        Hashtable IDTbl = null;
        Hashtable IDREFTbl = null;
        Object fNullValue = null;
        //EntityResolver entityResolver;

    }// class ValidateContext

} // class XSComplexTypeDecl
