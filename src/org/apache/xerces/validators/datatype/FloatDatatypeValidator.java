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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 *
 * @author  Elena Litani
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version  $Id$
 */

public class FloatDatatypeValidator extends AbstractDatatypeValidator {
    private Locale    fLocale                   = null;
    private float[]   fEnumFloats               = null;
    private String    fPattern                  = null;
    private float     fMaxInclusive             = Float.POSITIVE_INFINITY ;
    private float     fMaxExclusive             = Float.POSITIVE_INFINITY;
    private float     fMinInclusive             = Float.NEGATIVE_INFINITY;
    private float     fMinExclusive             = Float.NEGATIVE_INFINITY;
    private int       fFacetsDefined            = 0;

    private boolean   isMaxExclusiveDefined             = false;
    private boolean   isMaxInclusiveDefined             = false;
    private boolean   isMinExclusiveDefined             = false;
    private boolean   isMinInclusiveDefined             = false;
    private DatatypeMessageProvider fMessageProvider    = new DatatypeMessageProvider();
    private RegularExpression      fRegex               = null;


    public FloatDatatypeValidator () throws InvalidDatatypeFacetException{
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public FloatDatatypeValidator ( DatatypeValidator base, Hashtable facets,
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
                String key = (String) e.nextElement();
                String value = null;
                try {
                    if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                        fFacetsDefined += DatatypeValidator.FACET_PATTERN;
                        fPattern = (String)facets.get(key);
                        if ( fPattern != null )
                            fRegex = new RegularExpression(fPattern, "X" );
                    } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                        fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                        enumeration = (Vector)facets.get(key);
                    } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMaxInclusiveDefined = true;
                        fMaxInclusive = fValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMaxExclusiveDefined = true;
                        fMaxExclusive = fValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMinInclusiveDefined = true;
                        fMinInclusive  = fValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MINEXCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMinExclusiveDefined = true;
                        fMinExclusive  = fValueOf(value);
                    } else {
                        throw new InvalidDatatypeFacetException( getErrorString(  DatatypeMessageProvider.MSG_FORMAT_FAILURE,
                                                                                    DatatypeMessageProvider.MSG_NONE,
                                                                                    null));
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
                        base.validate (Float.toString(fMaxInclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxInclusive = '" + fMaxInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.8.c0 must: maxInclusive value from the value space of base
                if ( isMaxExclusiveDefined ) {
                    try {
                        base.validate (Float.toString(fMaxExclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxExclusive = '" + fMaxExclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.9.c0 must: minInclusive value from the value space of base
                if ( isMinInclusiveDefined ) {
                    try {
                        base.validate (Float.toString(fMinInclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of minInclusive = '" + fMinInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.10.c0 must: minInclusive value from the value space of base
                if ( isMinExclusiveDefined ) {
                    try {
                        base.validate (Float.toString(fMinExclusive), null);
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
                if ( fMinInclusive > fMaxInclusive )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be <= maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
            if ( isMaxExclusiveDefined && isMinExclusiveDefined ){
                if ( fMinExclusive > fMaxExclusive )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be <= maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.9.c2 must: minExclusive < maxInclusive
            if ( isMaxInclusiveDefined && isMinExclusiveDefined ){
                if ( fMinExclusive >= fMaxInclusive )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be < maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if ( isMaxExclusiveDefined && isMinInclusiveDefined ){
                if ( fMinInclusive >= fMaxExclusive )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be < maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }

            if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
                if (enumeration != null) {
                    fEnumFloats = new float[enumeration.size()];
                    int i = 0;
                    try {
                        for ( ; i < enumeration.size(); i++) {
                            fEnumFloats[i] = fValueOf((String) enumeration.elementAt(i));
                        }
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                                DatatypeMessageProvider.MSG_NONE,
                                                                                new Object [] { enumeration.elementAt(i)}));
                    }
                }
            }

            if (base != null && base instanceof FloatDatatypeValidator) {
                FloatDatatypeValidator floatBase = (FloatDatatypeValidator)base;

                // check 4.3.7.c2 error:
                // maxInclusive > base.maxInclusive
                // maxInclusive >= base.maxExclusive
                // maxInclusive < base.minInclusive
                // maxInclusive <= base.minExclusive
                if ( isMaxInclusiveDefined ) {
                    if ( floatBase.isMaxInclusiveDefined && fMaxInclusive > floatBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 floatBase.fMaxInclusive + "'." );
                    if ( floatBase.isMaxExclusiveDefined && fMaxInclusive >= floatBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be < base.maxExclusive value ='" +
                                                                floatBase.fMaxExclusive + "'." );
                    if ( floatBase.isMinInclusiveDefined && fMaxInclusive < floatBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be >= base.minInclusive value ='" +
                                                                 floatBase.fMinInclusive + "'." );
                    if ( floatBase.isMinExclusiveDefined && fMaxInclusive <= floatBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be > base.minExclusive value ='" +
                                                                floatBase.fMinExclusive + "'." );
                }

                // check 4.3.8.c3 error:
                // maxExclusive > base.maxExclusive
                // maxExclusive > base.maxInclusive
                // maxExclusive <= base.minInclusive
                // maxExclusive <= base.minExclusive
                if ( isMaxExclusiveDefined ) {
                    if ( floatBase.isMaxExclusiveDefined && fMaxExclusive > floatBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxExclusive value ='" +
                                                                 floatBase.fMaxExclusive + "'." );
                    if ( floatBase.isMaxInclusiveDefined && fMaxExclusive > floatBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxInclusive value ='" +
                                                                 floatBase.fMaxInclusive + "'." );
                    if ( floatBase.isMinExclusiveDefined && fMaxExclusive <= floatBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minExclusive value ='" +
                                                                 floatBase.fMinExclusive + "'." );
                    if ( floatBase.isMinInclusiveDefined && fMaxExclusive <= floatBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minInclusive value ='" +
                                                                 floatBase.fMinInclusive + "'." );
                }

                // check 4.3.9.c3 error:
                // minExclusive < base.minExclusive
                // minExclusive > base.maxInclusive ??? minExclusive >= base.maxInclusive
                // minExclusive < base.minInclusive
                // minExclusive >= base.maxExclusive
                if ( isMinExclusiveDefined ) {
                    if ( floatBase.isMinExclusiveDefined && fMinExclusive < floatBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be >= base.minExclusive value ='" +
                                                                 floatBase.fMinExclusive + "'." );
                    if ( floatBase.isMaxInclusiveDefined && fMinExclusive > floatBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be <= base.maxInclusive value ='" +
                                                                floatBase.fMaxInclusive + "'." );
                    if ( floatBase.isMinInclusiveDefined && fMinExclusive < floatBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be >= base.minInclusive value ='" +
                                                                floatBase.fMinInclusive + "'." );
                    if ( floatBase.isMaxExclusiveDefined && fMinExclusive >= floatBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be < base.maxExclusive value ='" +
                                                                 floatBase.fMaxExclusive + "'." );
                }

                // check 4.3.10.c2 error:
                // minInclusive < base.minInclusive
                // minInclusive > base.maxInclusive
                // minInclusive <= base.minExclusive
                // minInclusive >= base.maxExclusive
                if ( isMinInclusiveDefined ) {
                    if ( floatBase.isMinInclusiveDefined && fMinInclusive < floatBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be >= base.minInclusive value ='" +
                                                                 floatBase.fMinInclusive + "'." );
                    if ( floatBase.isMaxInclusiveDefined && fMinInclusive > floatBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 floatBase.fMaxInclusive + "'." );
                    if ( floatBase.isMinExclusiveDefined && fMinInclusive <= floatBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be > base.minExclusive value ='" +
                                                                 floatBase.fMinExclusive + "'." );
                    if ( floatBase.isMaxExclusiveDefined && fMinInclusive >= floatBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be < base.maxExclusive value ='" +
                                                                 floatBase.fMaxExclusive + "'." );
                }

                // inherit enumeration
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) == 0 &&
                     (floatBase.fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                    fEnumFloats = floatBase.fEnumFloats;
                }
                // inherit maxExclusive
                if ( floatBase.isMaxExclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxExclusiveDefined = true;
                    fFacetsDefined += FACET_MAXEXCLUSIVE;
                    fMaxExclusive = floatBase.fMaxExclusive;
                }
                // inherit maxInclusive
                if ( floatBase.isMaxInclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxInclusiveDefined = true;
                    fFacetsDefined += FACET_MAXINCLUSIVE;
                    fMaxInclusive = floatBase.fMaxInclusive;
                }
                // inherit minExclusive
                if ( floatBase.isMinExclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinExclusiveDefined = true;
                    fFacetsDefined += FACET_MINEXCLUSIVE;
                    fMinExclusive = floatBase.fMinExclusive;
                }
                // inherit minExclusive
                if ( floatBase.isMinInclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinInclusiveDefined = true;
                    fFacetsDefined += FACET_MININCLUSIVE;
                    fMinInclusive = floatBase.fMinInclusive;
                }
            }
        }// End facet setup
    }

    /**
     * Validate string content to be a valid float as
     * defined 3.2.3. Datatype.
     * IEEE single-precision 32-bit floatin point type
     * [IEEE] 754-1985]. The basic value space of float
     * consists of the values mx2^e, where m is an integer
     * whose absolute value is less than 2^24, and e
     * is an integer between -149 and 104 inclusive.
     *
     * @param content A string containing the content to be validated
     * @param state
     * @return
     * @exception throws InvalidDatatypeException if the content is
     *                   is not a W3C real type
     * @exception InvalidDatatypeValueException
     */
    public Object validate(String content, Object state)
    throws InvalidDatatypeValueException {
        checkContent (content, state, null, false);
        return null;
    }

     /**
     * validate if the content is valid against base datatype and facets (if any)
     * this function might be called directly from UnionDatatype or ListDatatype
     *
     * @param content A string containing the content to be validated
     * @param enumeration A vector with enumeration strings
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C float type;
     * @exception throws InvalidDatatypeFacetException if enumeration is not float
     */
     protected void checkContentEnum(String content, Object state, Vector enumeration)
     throws InvalidDatatypeValueException {
        checkContent (content, state, enumeration, false);
     }

     protected void checkContent(String content, Object state, Vector enumeration, boolean asBase)
     throws InvalidDatatypeValueException {
        // validate against parent type if any
        if ( this.fBaseValidator != null ) {
            // validate content as a base type
            if (fBaseValidator instanceof FloatDatatypeValidator) {
                ((FloatDatatypeValidator)fBaseValidator).checkContent(content, state, enumeration, true);
            } else {
                this.fBaseValidator.validate( content, state );
            }
        }

        // we check pattern first
        if ( (fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
            if ( fRegex == null || fRegex.matches( content) == false )
                throw new InvalidDatatypeValueException("Value'"+content+
                                                        "does not match regular expression facet" + fPattern );
        }

        // if this is a base validator, we only need to check pattern facet
        // all other facet were inherited by the derived type
        if (asBase)
            return;

        float f = 0;
        try {
            f = fValueOf(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.NotFloat,
                                                     DatatypeMessageProvider.MSG_NONE,
                                                     new Object [] {content}));
        }

        //enumeration is passed from List or Union datatypes
        if (enumeration != null) {
            int size =  enumeration.size();
            float[]     enumFloats = new float[size];
            int i=0;
            try {
                for (; i < size; i++)
                    enumFloats[i] = fValueOf((String) enumeration.elementAt(i));

            } catch (NumberFormatException nfe) {
                throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                         DatatypeMessageProvider.MSG_NONE,
                                                         new Object [] { enumeration.elementAt(i)}));
            }
            enumCheck(f, enumFloats);
        }

        boundsCheck(f);

        if (((fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 &&
            (fEnumFloats != null) ) )
            enumCheck(f, fEnumFloats);
    }


    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(float d) throws InvalidDatatypeValueException {

        boolean minOk = false;
        boolean maxOk = false;
        String  upperBound =  (fMaxExclusive != Float.MAX_VALUE )? (   Float.toString( fMaxExclusive)) :
                              ( ( fMaxInclusive != Float.MAX_VALUE )? Float.toString( fMaxInclusive):"");

        String  lowerBound =  (fMinExclusive != Float.MIN_VALUE )? ( Float.toString( fMinExclusive ) ):
                              (( fMinInclusive != Float.MIN_VALUE )? Float.toString( fMinInclusive ):"");
        String  lowerBoundIndicator = "";
        String  upperBoundIndicator = "";

        if ( isMaxInclusiveDefined) {
            maxOk = (d <= fMaxInclusive);
            upperBound          = Float.toString( fMaxInclusive );
            if ( upperBound != null ) {
                upperBoundIndicator = "<=";
            } else {
                upperBound="";
            }
        } else if ( isMaxExclusiveDefined) {
            maxOk = (d < fMaxExclusive );
            upperBound = Float.toString(fMaxExclusive );
            if ( upperBound != null ) {
                upperBoundIndicator = "<";
            } else {
                upperBound = "";
            }
        } else {
            maxOk = (!isMaxInclusiveDefined && ! isMaxExclusiveDefined);
        }

        if ( isMinInclusiveDefined) {

            minOk = (d >=  fMinInclusive );
            lowerBound = Float.toString( fMinInclusive );
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<=";
            } else {
                lowerBound = "";
            }
        } else if ( isMinExclusiveDefined) {
            minOk = (d > fMinExclusive);
            lowerBound = Float.toString( fMinExclusive  );
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<";
            } else {
                lowerBound = "";
            }
        } else {
            minOk = (!isMinInclusiveDefined && !isMinExclusiveDefined);
        }

        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException (
                             getErrorString(DatatypeMessageProvider.OutOfBounds,
                                  DatatypeMessageProvider.MSG_NONE,
                                      new Object [] { Float.toString(d) ,  lowerBound ,
                                          upperBound, lowerBoundIndicator, upperBoundIndicator}));

    }


    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    public int compare( String value1, String value2){
        try {
            float f1 = fValueOf(value1);
            float f2 = fValueOf(value2);
            int f1V = Float.floatToIntBits(f1);
            int f2V = Float.floatToIntBits(f2);
            if (f1 > f2) {
                return 1;
            }
            if (f1 < f2) {
                return -1;
            }
            if  (f1V==f2V){
                return 0;
            }
            return (f1V < f2V) ? -1 : 1;
       } catch (NumberFormatException e){
           //REVISIT: should we throw exception??
           return -1;
       }
    }


    /**
      * Returns a copy of this object.
      */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }


    private void enumCheck(float v, float[] enumFloats) throws InvalidDatatypeValueException {
       for (int i = 0; i < enumFloats.length; i++) {
           if (v == enumFloats[i]) return;
       }
       throw new InvalidDatatypeValueException(
                                              getErrorString(DatatypeMessageProvider.NotAnEnumValue,
                                                             DatatypeMessageProvider.MSG_NONE,
                                                             new Object [] { new Float(v)}));
   }


    private String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        } catch (Exception e) {
            return "Illegal Errorcode "+minor;
        }
    }


    private void setBasetype(DatatypeValidator base) {
        fBaseValidator =  base;
    }


    private static float fValueOf(String s) throws NumberFormatException {
        float f;
        try {
             f = Float.valueOf(s).floatValue();
        } catch (NumberFormatException nfe) {
            if( s.equals("INF") ){
                f = Float.POSITIVE_INFINITY;
            } else if( s.equals("-INF") ){
                f = Float.NEGATIVE_INFINITY;
            } else if( s.equals("NaN" ) ) {
                f = Float.NaN;
            } else {
                throw nfe;
            }
        }
        return f;
    }
}
