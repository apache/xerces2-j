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

/**
 * @author Elena Litani
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @author Mark Swinkles - List Validation refactoring
 * @version $Id$
 */

public class DoubleDatatypeValidator extends AbstractDatatypeValidator {
    private Locale            fLocale           = null;
    private double[]          fEnumDoubles      = null;
    private String            fPattern          = null;
    private double            fMaxInclusive     = Double.POSITIVE_INFINITY;
    private double            fMaxExclusive     = Double.POSITIVE_INFINITY;
    private double            fMinInclusive     = Double.NEGATIVE_INFINITY;
    private double            fMinExclusive     = Double.NEGATIVE_INFINITY;
    private int               fFacetsDefined    = 0;

    private boolean           isMaxExclusiveDefined = false;
    private boolean           isMaxInclusiveDefined = false;
    private boolean           isMinExclusiveDefined = false;
    private boolean           isMinInclusiveDefined = false;
    private RegularExpression      fRegex           = null;

    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();



    public DoubleDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public DoubleDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                     boolean derivedByList ) throws InvalidDatatypeFacetException  {
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
                        fMaxInclusive = dValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMaxExclusiveDefined = true;
                        fMaxExclusive = dValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMinInclusiveDefined = true;
                        fMinInclusive  = dValueOf(value);
                    } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                        fFacetsDefined += DatatypeValidator.FACET_MINEXCLUSIVE;
                        value  = ((String)facets.get(key));
                        isMinExclusiveDefined = true;
                        fMinExclusive  = dValueOf(value);
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
                        base.validate (Double.toString(fMaxInclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxInclusive = '" + fMaxInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.8.c0 must: maxInclusive value from the value space of base
                if ( isMaxExclusiveDefined ) {
                    try {
                        base.validate (Double.toString(fMaxExclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of maxExclusive = '" + fMaxExclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.9.c0 must: minInclusive value from the value space of base
                if ( isMinInclusiveDefined ) {
                    try {
                        base.validate (Double.toString(fMinInclusive), null);
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( "Value of minInclusive = '" + fMinInclusive +
                                                                 "' must be from the value space of base.");
                    }
                }
                // check 4.3.10.c0 must: minInclusive value from the value space of base
                if ( isMinExclusiveDefined ) {
                    try {
                        base.validate (Double.toString(fMinExclusive), null);
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
                if ( fMinInclusive >= fMaxExclusive )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be < maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if ( isMaxExclusiveDefined && isMinInclusiveDefined ){
                if ( fMinExclusive >= fMaxInclusive )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be < maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }

            if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
                if (enumeration != null) {
                    fEnumDoubles = new double[enumeration.size()];
                    int i = 0;
                    try {
                        for ( ; i < enumeration.size(); i++) {
                            fEnumDoubles[i] = dValueOf((String) enumeration.elementAt(i));
                        }
                    } catch ( Exception idve ){
                        throw new InvalidDatatypeFacetException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                                DatatypeMessageProvider.MSG_NONE,
                                                                                new Object [] { enumeration.elementAt(i)}));
                    }
                }
            }

            if (base != null && base instanceof DoubleDatatypeValidator) {
                DoubleDatatypeValidator doubleBase = (DoubleDatatypeValidator)base;

                // check 4.3.7.c2 error:
                // maxInclusive > base.maxInclusive
                // maxInclusive >= base.maxExclusive
                // maxInclusive < base.minInclusive
                // maxInclusive <= base.minExclusive
                if ( isMaxInclusiveDefined ) {
                    if ( doubleBase.isMaxInclusiveDefined && fMaxInclusive > doubleBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 doubleBase.fMaxInclusive + "'." );
                    if ( doubleBase.isMaxExclusiveDefined && fMaxInclusive >= doubleBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be < base.maxExclusive value ='" +
                                                                doubleBase.fMaxExclusive + "'." );
                    if ( doubleBase.isMinInclusiveDefined && fMaxInclusive < doubleBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be >= base.minInclusive value ='" +
                                                                 doubleBase.fMinInclusive + "'." );
                    if ( doubleBase.isMinExclusiveDefined && fMaxInclusive <= doubleBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "maxInclusive value ='" + fMaxInclusive + "' must be > base.minExclusive value ='" +
                                                                doubleBase.fMinExclusive + "'." );
                }

                // check 4.3.8.c3 error:
                // maxExclusive > base.maxExclusive
                // maxExclusive > base.maxInclusive
                // maxExclusive <= base.minInclusive
                // maxExclusive <= base.minExclusive
                if ( isMaxExclusiveDefined ) {
                    if ( doubleBase.isMaxExclusiveDefined && fMaxExclusive > doubleBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxExclusive value ='" +
                                                                 doubleBase.fMaxExclusive + "'." );
                    if ( doubleBase.isMaxInclusiveDefined && fMaxExclusive > doubleBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxInclusive value ='" +
                                                                 doubleBase.fMaxInclusive + "'." );
                    if ( doubleBase.isMinExclusiveDefined && fMaxExclusive <= doubleBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minExclusive value ='" +
                                                                 doubleBase.fMinExclusive + "'." );
                    if ( doubleBase.isMinInclusiveDefined && fMaxExclusive <= doubleBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minInclusive value ='" +
                                                                 doubleBase.fMinInclusive + "'." );
                }

                // check 4.3.9.c3 error:
                // minExclusive < base.minExclusive
                // minExclusive > base.maxInclusive ??? minExclusive >= base.maxInclusive
                // minExclusive < base.minInclusive
                // minExclusive >= base.maxExclusive
                if ( isMinExclusiveDefined ) {
                    if ( doubleBase.isMinExclusiveDefined && fMinExclusive < doubleBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be >= base.minExclusive value ='" +
                                                                 doubleBase.fMinExclusive + "'." );
                    if ( doubleBase.isMaxInclusiveDefined && fMinExclusive > doubleBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be <= base.maxInclusive value ='" +
                                                                doubleBase.fMaxInclusive + "'." );
                    if ( doubleBase.isMinInclusiveDefined && fMinExclusive < doubleBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException(
                                                                "minExclusive value ='" + fMinExclusive + "' must be >= base.minInclusive value ='" +
                                                                doubleBase.fMinInclusive + "'." );
                    if ( doubleBase.isMaxExclusiveDefined && fMinExclusive >= doubleBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be < base.maxExclusive value ='" +
                                                                 doubleBase.fMaxExclusive + "'." );
                }

                // check 4.3.10.c2 error:
                // minInclusive < base.minInclusive
                // minInclusive > base.maxInclusive
                // minInclusive <= base.minExclusive
                // minInclusive >= base.maxExclusive
                if ( isMinInclusiveDefined ) {
                    if ( doubleBase.isMinInclusiveDefined && fMinInclusive < doubleBase.fMinInclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be >= base.minInclusive value ='" +
                                                                 doubleBase.fMinInclusive + "'." );
                    if ( doubleBase.isMaxInclusiveDefined && fMinInclusive > doubleBase.fMaxInclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 doubleBase.fMaxInclusive + "'." );
                    if ( doubleBase.isMinExclusiveDefined && fMinInclusive <= doubleBase.fMinExclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be > base.minExclusive value ='" +
                                                                 doubleBase.fMinExclusive + "'." );
                    if ( doubleBase.isMaxExclusiveDefined && fMinInclusive >= doubleBase.fMaxExclusive )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be < base.maxExclusive value ='" +
                                                                 doubleBase.fMaxExclusive + "'." );
                }

                // inherit enumeration
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) == 0 &&
                     (doubleBase.fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                    fEnumDoubles = doubleBase.fEnumDoubles;
                }
                // inherit maxExclusive
                if ( doubleBase.isMaxExclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxExclusiveDefined = true;
                    fFacetsDefined += FACET_MAXEXCLUSIVE;
                    fMaxExclusive = doubleBase.fMaxExclusive;
                }
                // inherit maxInclusive
                if ( doubleBase.isMaxInclusiveDefined &&
                     !isMaxExclusiveDefined && !isMaxInclusiveDefined ) {
                    isMaxInclusiveDefined = true;
                    fFacetsDefined += FACET_MAXINCLUSIVE;
                    fMaxInclusive = doubleBase.fMaxInclusive;
                }
                // inherit minExclusive
                if ( doubleBase.isMinExclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinExclusiveDefined = true;
                    fFacetsDefined += FACET_MINEXCLUSIVE;
                    fMinExclusive = doubleBase.fMinExclusive;
                }
                // inherit minExclusive
                if ( doubleBase.isMinInclusiveDefined &&
                     !isMinExclusiveDefined && !isMinInclusiveDefined ) {
                    isMinInclusiveDefined = true;
                    fFacetsDefined += FACET_MININCLUSIVE;
                    fMinInclusive = doubleBase.fMinInclusive;
                }
            }
        }// End of facet setting
    }

    /**
     * validate that a string matches the real datatype
     * @param content A string containing the content to be validated
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C real type
     */

    public Object validate(String content, Object state) throws InvalidDatatypeValueException {
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
     *  is not a W3C double type;
     * @exception throws InvalidDatatypeFacetException if enumeration is not double
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
            if (fBaseValidator instanceof DoubleDatatypeValidator) {
                ((DoubleDatatypeValidator)fBaseValidator).checkContent(content, state, enumeration, true);
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

        double d = 0.0;
        try {
            d = dValueOf(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.NotDouble,
                                                     DatatypeMessageProvider.MSG_NONE,
                                                     new Object [] { content}));
        }

        if (enumeration != null) { //the call was made from List or union
            int size = enumeration.size();
            double[] enumDoubles = new double[size];
            int i=0;
            try {
                for (; i < size; i++)
                    enumDoubles[i] = dValueOf((String) enumeration.elementAt(i));
            } catch (NumberFormatException nfe) {
                throw new InvalidDatatypeValueException( getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                         DatatypeMessageProvider.MSG_NONE,
                                                         new Object [] { enumeration.elementAt(i)}));
           }

           enumCheck(d, enumDoubles);
       }

        boundsCheck(d);

        if (((fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 &&
            (fEnumDoubles != null) ) )
            enumCheck(d, fEnumDoubles);
    }


    // Private Methods start here


    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(double d) throws InvalidDatatypeValueException {

        boolean minOk = false;
        boolean maxOk = false;
        String  upperBound =  (fMaxExclusive != Double.MAX_VALUE )? (   Double.toString( fMaxExclusive)) :
                              ( ( fMaxInclusive != Double.MAX_VALUE )? Double.toString( fMaxInclusive):"");

        String  lowerBound =  (fMinExclusive != Double.MIN_VALUE )? ( Double.toString( fMinExclusive ) ):
                              (( fMinInclusive != Double.MIN_VALUE )? Double.toString( fMinInclusive ):"");
        String  lowerBoundIndicator = "";
        String  upperBoundIndicator = "";


        if ( isMaxInclusiveDefined) {
            maxOk = (d <= fMaxInclusive);
            upperBound          = Double.toString( fMaxInclusive );
            if ( upperBound != null ) {
                upperBoundIndicator = "<=";
            } else {
                upperBound="";
            }
        } else if ( isMaxExclusiveDefined) {
            maxOk = (d < fMaxExclusive );
            upperBound = Double.toString(fMaxExclusive );
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
            lowerBound = Double.toString( fMinInclusive );
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<=";
            } else {
                lowerBound = "";
            }
        } else if ( isMinExclusiveDefined) {
            minOk = (d > fMinExclusive);
            lowerBound = Double.toString( fMinExclusive  );
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
                                      new Object [] { Double.toString(d) ,  lowerBound ,
                                          upperBound, lowerBoundIndicator, upperBoundIndicator}));


    }

    private void enumCheck(double v, double[] enumDoubles) throws InvalidDatatypeValueException {
        for (int i = 0; i < enumDoubles.length; i++) {
            if (v == enumDoubles[i]) return;
        }
        throw new InvalidDatatypeValueException(
                                               getErrorString(DatatypeMessageProvider.NotAnEnumValue,
                                                              DatatypeMessageProvider.MSG_NONE,
                                                              new Object [] { new Double(v)}));
    }


   public int compare( String value1, String value2){
        try {
            double d1 = dValueOf(value1);
            double d2 = dValueOf(value2);
            long d1V = Double.doubleToLongBits(d1);
            long d2V = Double.doubleToLongBits(d2);

            if (d1 > d2) {
                return 1;
            }
            if (d1 < d2) {
                return -1;
            }
            if (d1V == d2V) {
                return 0;
            }
            return (d1V < d2V) ? -1 : 1;
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

    private static double dValueOf(String s) throws NumberFormatException {
        double f;
        try {
             f = Double.valueOf(s).doubleValue();
        } catch (NumberFormatException nfe) {
            if( s.equals("INF") ){
                f = Double.POSITIVE_INFINITY;
            } else if( s.equals("-INF") ){
                f = Double.NEGATIVE_INFINITY;
            } else if( s.equals("NaN" ) ) {
                f = Double.NaN;
            } else {
                throw nfe;
            }
        }
        return f;
    }
}
