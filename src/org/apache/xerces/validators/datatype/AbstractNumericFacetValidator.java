/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000, 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.validators.datatype;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import java.io.IOException;
import org.apache.xerces.validators.schema.SchemaSymbols;
import org.apache.xerces.utils.regex.RegularExpression;

/**
 * AbstractNumericFacetValidator combines common code for evaluating facets constraints of schema.
 * 
 * @author Elena Litani
 * $Id$
 */

public abstract class AbstractNumericFacetValidator extends AbstractDatatypeValidator {

    protected Locale             fLocale                 = null;
    protected Object[]            fEnumeration            = null;
    protected Object              fMaxInclusive           = null;
    protected Object              fMaxExclusive           = null;
    protected Object              fMinInclusive           = null;
    protected Object              fMinExclusive           = null;
    protected int                 fTotalDigits            = 0;
    protected int                 fFractionDigits         = 0;
    protected DatatypeMessageProvider fMessageProvider        = new DatatypeMessageProvider();

    public  AbstractNumericFacetValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public AbstractNumericFacetValidator ( DatatypeValidator base, 
                                           Hashtable facets, 
                                           boolean derivedByList) throws InvalidDatatypeFacetException {         
        fBaseValidator = base;

        // list types are handled by ListDatatypeValidator, we do nothing here.
        if ( derivedByList )
            return;

        // Set Facets if any defined
        if ( facets != null ) {
            Vector enumeration = null;
            for ( Enumeration e = facets.keys(); e.hasMoreElements(); ) {
                String key   = (String) e.nextElement();
                String value = null;
                try {
                    if ( key.equals(SchemaSymbols.ELT_PATTERN) ) {
                        fFacetsDefined |= DatatypeValidator.FACET_PATTERN;
                        fPattern = (String) facets.get(key);
                        if ( fPattern != null )
                            fRegex = new RegularExpression(fPattern, "X" );
                    }
                    else if ( key.equals(SchemaSymbols.ELT_ENUMERATION) ) {
                        enumeration     = (Vector)facets.get(key);
                        fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                    }
                    else if ( key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_MAXINCLUSIVE;
                        setMaxInclusive(value);
                    }
                    else if ( key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_MAXEXCLUSIVE;
                        setMaxExclusive(value);
                    }
                    else if ( key.equals(SchemaSymbols.ELT_MININCLUSIVE) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_MININCLUSIVE;
                        setMinInclusive(value);
                    }
                    else if ( key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_MINEXCLUSIVE;
                        setMinExclusive(value);
                    }
                    else if ( key.equals(SchemaSymbols.ELT_TOTALDIGITS) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_TOTALDIGITS;
                        fTotalDigits      = Integer.parseInt(value );
                        // check 4.3.11.c0 must: totalDigits > 0
                        if ( fTotalDigits <= 0 )
                            throw new InvalidDatatypeFacetException("totalDigits value '"+fTotalDigits+"' must be a positiveInteger.");
                    }
                    else if ( key.equals(SchemaSymbols.ELT_FRACTIONDIGITS) ) {
                        value = ((String) facets.get(key ));
                        fFacetsDefined |= DatatypeValidator.FACET_FRACTIONDIGITS;
                        fFractionDigits          = Integer.parseInt( value );
                        // check 4.3.12.c0 must: fractionDigits > 0
                        if ( fFractionDigits < 0 )
                            throw new InvalidDatatypeFacetException("fractionDigits value '"+fFractionDigits+"' must be a positiveInteger.");
                    }
                    else {
                        throw new InvalidDatatypeFacetException( getErrorString( DatatypeMessageProvider.MSG_FORMAT_FAILURE,
                                                                                 DatatypeMessageProvider.MSG_NONE, null));
                    }
                }
                catch ( Exception ex ) {
                    throw new InvalidDatatypeFacetException( getErrorString( DatatypeMessageProvider.IllegalFacetValue,
                                                                             DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                }
            }


            // check 4.3.8.c1 error: maxInclusive + maxExclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) && 
                 ((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) ) {
                throw new InvalidDatatypeFacetException( "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." );
            }
            // check 4.3.9.c1 error: minInclusive + minExclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) && 
                 ((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                throw new InvalidDatatypeFacetException( "It is an error for both minInclusive and minExclusive to be specified for the same datatype." );
            }

            // check 4.3.7.c1 must: minInclusive <= maxInclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) && 
                 ((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                if ( compareValues(fMinInclusive, fMaxInclusive) == 1 )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be <= maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) ) {
                if ( compareValues(fMinExclusive, fMaxExclusive) == 1 )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be <= maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.9.c2 must: minExclusive < maxInclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) && ((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) ) {
                if ( compareValues(fMinExclusive, fMaxInclusive) != -1 )
                    throw new InvalidDatatypeFacetException( "minExclusive value ='" + this.fMinExclusive + "'must be > maxInclusive value ='" +
                                                             this.fMaxInclusive + "'. " );
            }
            // check 4.3.10.c1 must: minInclusive < maxExclusive
            if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) && ((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                if ( compareValues(fMinInclusive, fMaxExclusive) != -1 )
                    throw new InvalidDatatypeFacetException( "minInclusive value ='" + this.fMinInclusive + "'must be < maxExclusive value ='" +
                                                             this.fMaxExclusive + "'. " );
            }
            // check 4.3.12.c1 must: fractionDigits <= totalDigits
            if ( ((fFacetsDefined & DatatypeValidator.FACET_FRACTIONDIGITS) != 0) && ((fFacetsDefined & DatatypeValidator.FACET_TOTALDIGITS) != 0) ) {
                if ( fFractionDigits > fTotalDigits )
                    throw new InvalidDatatypeFacetException( "fractionDigits value ='" + this.fFractionDigits + "'must be <= totalDigits value ='" +
                                                             this.fTotalDigits + "'. " );
            }


            if ( base != null ) {
                AbstractNumericFacetValidator numBase = (AbstractNumericFacetValidator)base;

                // check 4.3.7.c2 error:
                // maxInclusive > base.maxInclusive
                // maxInclusive >= base.maxExclusive
                // maxInclusive < base.minInclusive
                // maxInclusive <= base.minExclusive
                if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) ) {
                    if ( ((numBase.fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) &&
                         compareValues(fMaxInclusive, numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( ((numBase.fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) &&
                         compareValues(fMaxInclusive, numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException(
                                                               "maxInclusive value ='" + fMaxInclusive + "' must be < base.maxExclusive value ='" +
                                                               numBase.fMaxExclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) &&
                         compareValues(fMaxInclusive, numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException( "maxInclusive value ='" + fMaxInclusive + "' must be >= base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) &&
                         compareValues(fMaxInclusive, numBase.fMinExclusive ) != 1 )
                        throw new InvalidDatatypeFacetException(
                                                               "maxInclusive value ='" + fMaxInclusive + "' must be > base.minExclusive value ='" +
                                                               numBase.fMinExclusive + "'." );
                }

                // check 4.3.8.c3 error:
                // maxExclusive > base.maxExclusive
                // maxExclusive > base.maxInclusive
                // maxExclusive <= base.minInclusive
                // maxExclusive <= base.minExclusive
                if ( ((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) ) {
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) &&
                         compareValues(fMaxExclusive, numBase.fMaxExclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) &&
                         compareValues(fMaxExclusive, numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) &&
                         compareValues(fMaxExclusive, numBase.fMinExclusive ) != 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) &&
                         compareValues(fMaxExclusive, numBase.fMinInclusive) != 1 )
                        throw new InvalidDatatypeFacetException( "maxExclusive value ='" + fMaxExclusive + "' must be > base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                }

                // check 4.3.9.c3 error:
                // minExclusive < base.minExclusive
                // minExclusive > base.maxInclusive ??? minExclusive >= base.maxInclusive
                // minExclusive < base.minInclusive
                // minExclusive >= base.maxExclusive
                if ( ((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) ) {
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) &&
                         compareValues(fMinExclusive, numBase.fMinExclusive ) == -1 )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be >= base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) &&
                         compareValues(fMinExclusive, numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException(
                                                               "minExclusive value ='" + fMinExclusive + "' must be <= base.maxInclusive value ='" +
                                                               numBase.fMaxInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) &&
                         compareValues(fMinExclusive, numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException(
                                                               "minExclusive value ='" + fMinExclusive + "' must be >= base.minInclusive value ='" +
                                                               numBase.fMinInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) &&
                         compareValues(fMinExclusive, numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException( "minExclusive value ='" + fMinExclusive + "' must be < base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                }

                // check 4.3.10.c2 error:
                // minInclusive < base.minInclusive
                // minInclusive > base.maxInclusive
                // minInclusive <= base.minExclusive
                // minInclusive >= base.maxExclusive
                if ( ((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                    if ( ((numBase.fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) &&
                         compareValues(fMinInclusive, numBase.fMinInclusive) == -1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be >= base.minInclusive value ='" +
                                                                 numBase.fMinInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) &&
                         compareValues(fMinInclusive, numBase.fMaxInclusive) == 1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be <= base.maxInclusive value ='" +
                                                                 numBase.fMaxInclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) &&
                         compareValues(fMinInclusive, numBase.fMinExclusive ) != 1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be > base.minExclusive value ='" +
                                                                 numBase.fMinExclusive + "'." );
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) &&
                         compareValues(fMinInclusive, numBase.fMaxExclusive) != -1 )
                        throw new InvalidDatatypeFacetException( "minInclusive value ='" + fMinInclusive + "' must be < base.maxExclusive value ='" +
                                                                 numBase.fMaxExclusive + "'." );
                }

                // check 4.3.11.c1 error: totalDigits > base.totalDigits
                if ( ((fFacetsDefined & DatatypeValidator.FACET_TOTALDIGITS) != 0) ) {
                    if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_TOTALDIGITS) != 0) &&
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
                    fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                    fEnumeration = numBase.fEnumeration;
                }
                // inherit maxExclusive
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) &&
                     !((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) ) {
                    fFacetsDefined |= FACET_MAXEXCLUSIVE;
                    fMaxExclusive = numBase.fMaxExclusive;
                }
                // inherit maxInclusive
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) &&
                     !((fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0) ) {
                    fFacetsDefined |= FACET_MAXINCLUSIVE;
                    fMaxInclusive = numBase.fMaxInclusive;
                }
                // inherit minExclusive
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) &&
                     !((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                    fFacetsDefined |= FACET_MINEXCLUSIVE;
                    fMinExclusive = numBase.fMinExclusive;
                }
                // inherit minExclusive
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) &&
                     !((fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0) ) {
                    fFacetsDefined |= FACET_MININCLUSIVE;
                    fMinInclusive = numBase.fMinInclusive;
                }
                // inherit totalDigits
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_TOTALDIGITS) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_TOTALDIGITS) != 0) ) {
                    fFacetsDefined |= FACET_TOTALDIGITS;
                    fTotalDigits = numBase.fTotalDigits;
                }
                // inherit fractionDigits
                if ( (( numBase.fFacetsDefined & DatatypeValidator.FACET_FRACTIONDIGITS) != 0) && !((fFacetsDefined & DatatypeValidator.FACET_FRACTIONDIGITS) != 0) ) {
                    fFacetsDefined |= FACET_FRACTIONDIGITS;
                    fFractionDigits = numBase.fFractionDigits;
                }
                // check 4.3.5.c0 must: enumeration values from the value space of base
                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION) != 0 ) {
                    if ( enumeration != null ) {
                        try {
                            setEnumeration(enumeration);
                        }
                        catch ( Exception idve ) {
                            throw new InvalidDatatypeFacetException( idve.getMessage());
                        }
                    }

                }
            }
        }//End of Facet setup
    }

    /**
     * Validate string against lexical space of datatype
     * 
     * @param content A string containing the content to be validated
     * @param state
     * @return 
     * @exception throws InvalidDatatypeException if the content is
     *                   is not a W3C decimal type
     * @exception InvalidDatatypeValueException
     */
    public Object validate(String content, Object state) throws InvalidDatatypeValueException {
        //REVISIT: should we pass state?
        checkContent(content, state, null, false);
        return null;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    /**
     * Compares values in lexical space of give datatype
     * 
     * @param value1
     * @param value2
     * @return 
     */
    abstract protected int compareValues (Object value1, Object value2);    
    
    //
    // set* functions, create appropriate object (depending on datatype
    //
    abstract protected void setMaxInclusive (String value);
    abstract protected void setMinInclusive (String value);
    abstract protected void setMaxExclusive (String value);
    abstract protected void setMinExclusive (String value);    
    abstract protected void setEnumeration (Vector enumeration) 
                            throws InvalidDatatypeValueException;

    //
    // get* functions used to output error messages
    //
    abstract protected String getMaxInclusive (boolean isBase);
    abstract protected String getMinInclusive (boolean isBase);
    abstract protected String getMaxExclusive (boolean isBase);
    abstract protected String getMinExclusive (boolean isBase);


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

    //
    // content - string value to be avaluated
    //
    abstract protected void checkContent( String content, Object State, Vector enum, boolean asBase)
                                    throws InvalidDatatypeValueException;
   

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    protected void boundsCheck(Object d) throws InvalidDatatypeValueException {

        boolean minOk = true;
        boolean maxOk = true;
        String  upperBound="";

        String  lowerBound="";
        String  lowerBoundIndicator = "";
        String  upperBoundIndicator = "";
        int compare;
        if ( (fFacetsDefined & DatatypeValidator.FACET_MAXINCLUSIVE) != 0 ) {
            compare = compareValues(d, fMaxInclusive);
            maxOk=(compare==1)?false:true;
            upperBound   = getMaxInclusive(false);
            if ( upperBound != null ) {
                upperBoundIndicator = "<=";
            }
            else {
                upperBound="";
            }
        }
        if ( (fFacetsDefined & DatatypeValidator.FACET_MAXEXCLUSIVE) != 0 ) {
            compare = compareValues(d, fMaxExclusive );
            maxOk = (compare==-1)?true:false;
            upperBound = getMaxExclusive (false);
            if ( upperBound != null ) {
                upperBoundIndicator = "<";
            }
            else {
                upperBound = "";
            }
        }

        if ( (fFacetsDefined & DatatypeValidator.FACET_MININCLUSIVE) != 0 ) {
            compare = compareValues(d, fMinInclusive);
            minOk = (compare==-1)?false:true;
            lowerBound = getMinInclusive (false);
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<=";
            }
            else {
                lowerBound = "";
            }
        }
        if ( (fFacetsDefined & DatatypeValidator.FACET_MINEXCLUSIVE) != 0 ) {
            compare = compareValues(d, fMinExclusive);
            minOk = (compare==1)?true:false;
            lowerBound = getMinExclusive (false );
            if ( lowerBound != null ) {
                lowerBoundIndicator = "<";
            }
            else {
                lowerBound = "";
            }
        }

        if ( !(minOk && maxOk) )
            throw new InvalidDatatypeValueException (
                                                    getErrorString(DatatypeMessageProvider.OutOfBounds,
                                                                   DatatypeMessageProvider.MSG_NONE,
                                                                   new Object [] { d.toString() ,  lowerBound ,
                                                                       upperBound, lowerBoundIndicator, upperBoundIndicator}));


    }

    protected String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        }
        catch ( Exception e ) {
            return "Illegal Errorcode "+minor;
        }
    }

}
