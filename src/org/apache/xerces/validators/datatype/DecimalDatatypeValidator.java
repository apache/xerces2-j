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
 * DecimalValidator validates that content satisfies the W3C XML Datatype for decimal
 *
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @version $Id$
 */

public class DecimalDatatypeValidator extends AbstractDatatypeValidator {
    private Locale            fLocale           = null;
    private DatatypeValidator fBaseValidator    = null; // Null means a native datatype
    private boolean           fDerivedByList    = false; //Derived by restriction is defaul
    private BigDecimal[]      fEnumDecimal      = null;
    private String            fPattern          = null;
    private BigDecimal        fMaxInclusive     = null;
    private BigDecimal        fMaxExclusive     = null;
    private BigDecimal        fMinInclusive     = null;
    private BigDecimal        fMinExclusive     = null;
    private int               fFacetsDefined    = 0;
    private int               fScale            = 0;
    private int               fPrecision        = 0;
    private boolean           isMaxExclusiveDefined = false;
    private boolean           isMaxInclusiveDefined = false;
    private boolean           isMinExclusiveDefined = false;
    private boolean           isMinInclusiveDefined = false;
    private boolean           isScaleDefined        = false;
    private boolean           isPrecisionDefined    = false;

    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private RegularExpression       fRegex           = null;



    public DecimalDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public DecimalDatatypeValidator ( DatatypeValidator base, Hashtable facets,
                                      boolean derivedByList ) throws InvalidDatatypeFacetException {
        setBasetype( base ); // Set base type 


        fDerivedByList = derivedByList;

        if ( facets != null ) {   // Set Facet
            if ( fDerivedByList == false ) { // Derivation by Constraint 
                Vector enumeration = null;
                String value       = null;
                for (Enumeration e = facets.keys(); e.hasMoreElements();) {
                    String key   = (String) e.nextElement();
                    try {
                        if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_PATTERN;
                            fPattern        = value;
                            if( fPattern != null )
                                fRegex = new RegularExpression(fPattern, "X" );
                        } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                            fFacetsDefined += DatatypeValidator.FACET_ENUMERATION;
                            enumeration     = (Vector)facets.get(key);
                        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                            fMaxInclusive    = new BigDecimal(value);
                        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                            fMaxExclusive   = new BigDecimal(value);
                        } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                            fMinInclusive   = new BigDecimal(value);
                        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                            fMinExclusive   = new BigDecimal(value);
                        } else if (key.equals(SchemaSymbols.ELT_PRECISION)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_PRECISSION;
                            isPrecisionDefined = true;
                            fPrecision      = Integer.parseInt(value );
                        } else if (key.equals(SchemaSymbols.ELT_SCALE)) {
                            value = ((String) facets.get(key ));
                            fFacetsDefined += DatatypeValidator.FACET_SCALE;
                            isScaleDefined  = true;
                            fScale          = Integer.parseInt( value );
                        } else {
                            throw new InvalidDatatypeFacetException(
                                                                   getErrorString( DatatypeMessageProvider.MSG_FORMAT_FAILURE,
                                                                                   DatatypeMessageProvider.MSG_NONE, null));
                        }
                    } catch ( Exception ex ){
                        throw new InvalidDatatypeFacetException( getErrorString(
                                                                               DatatypeMessageProvider.IllegalFacetValue, 
                                                                               DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                    }
                }
                isMaxExclusiveDefined = ((fFacetsDefined & 
                                          DatatypeValidator.FACET_MAXEXCLUSIVE ) != 0 )?true:false;
                isMaxInclusiveDefined = ((fFacetsDefined & 
                                          DatatypeValidator.FACET_MAXINCLUSIVE ) != 0 )?true:false;
                isMinExclusiveDefined = ((fFacetsDefined &
                                          DatatypeValidator.FACET_MINEXCLUSIVE ) != 0 )?true:false;
                isMinInclusiveDefined = ((fFacetsDefined &
                                          DatatypeValidator.FACET_MININCLUSIVE ) != 0 )?true:false;


                if ( isMaxExclusiveDefined && isMaxInclusiveDefined ) {
                    throw new InvalidDatatypeFacetException(
                                                "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." ); 
                }
                if ( isMinExclusiveDefined && isMinInclusiveDefined ) {
                    throw new InvalidDatatypeFacetException(
                                                "It is an error for both minInclusive and minExclusive to be specified for the same datatype." ); 
                }

                if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
                    if (enumeration != null) {
                        fEnumDecimal = new BigDecimal[enumeration.size()];
                        int i = 0;
                            try {
                                for ( ; i < enumeration.size(); i++) {
                                    fEnumDecimal[i] = 
                                          new BigDecimal( ((String) enumeration.elementAt(i)));
                                    boundsCheck(fEnumDecimal[i]); // Check against max,min Inclusive, Exclusives
                                }
                            } catch( Exception idve ){
                                throw new InvalidDatatypeFacetException(
                                      getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                               DatatypeMessageProvider.MSG_NONE,
                                                       new Object [] { enumeration.elementAt(i)}));
                            }
                    }
                }
            } else { // Derivation by List
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

        if ( fDerivedByList == false ) { //derived by restriction

            if ( (fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
              if ( fRegex == null || fRegex.matches( content) == false )
                  throw new InvalidDatatypeValueException("Value'"+content+
                                                      "does not match regular expression facet" + fPattern );
            }

            BigDecimal d = null; // Is content a Decimal 
            try {
                d = new BigDecimal(content);
            } 
            catch (Exception nfe) {
                throw new InvalidDatatypeValueException(
                   getErrorString(DatatypeMessageProvider.NotDecimal,
                  DatatypeMessageProvider.MSG_NONE,
                                      new Object[] { "'" + content +"'"}));
            }
            //} 
            //catch (IOException ex ) {
              //  throw new InvalidDatatypeValueException(
                //  getErrorString(DatatypeMessageProvider.NotDecimal,
                // DatatypeMessageProvider.MSG_NONE,
              //                       new Object[] { "'" + content +"'"}));
            //}


            if( isScaleDefined == true ) {
                 if (d.scale() > fScale)
                      throw new InvalidDatatypeValueException(
                                                  getErrorString(DatatypeMessageProvider.ScaleExceeded,
                                                          DatatypeMessageProvider.MSG_NONE,
                                                          new Object[] { content}));
            }
            if( isPrecisionDefined == true ) {
                 int precision = d.movePointRight(d.scale()).toString().length() - 
                            ((d.signum() < 0) ? 1 : 0); // account for minus sign
                 if (precision > fPrecision)
                      throw new InvalidDatatypeValueException(
                              getErrorString(DatatypeMessageProvider.PrecisionExceeded,
                                               DatatypeMessageProvider.MSG_NONE,
                                                    new Object[] {content} ));
            }
            boundsCheck(d);
            if (  fEnumDecimal != null )
                 enumCheck(d);


        } else { //derivation by list

        }
    return null;
    }

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(BigDecimal d) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;

        if ( isMaxInclusiveDefined)
            maxOk = (d.compareTo(fMaxInclusive) <= 0);
        else if ( isMaxExclusiveDefined)
            maxOk = (d.compareTo(fMaxExclusive) < 0);
        else
            maxOk = (!isMaxInclusiveDefined && ! isMaxExclusiveDefined);

        if ( isMinInclusiveDefined)
            minOk = (d.compareTo(fMinInclusive) >= 0);
        else if ( isMinExclusiveDefined)
            minOk = (d.compareTo(fMinInclusive) > 0);
        else
            minOk = (!isMinInclusiveDefined && !isMinExclusiveDefined);
        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException(
                   getErrorString(DatatypeMessageProvider.OutOfBounds,
                   DatatypeMessageProvider.MSG_NONE,
                                           new Object [] { d}));

    }

    private void enumCheck(BigDecimal v) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumDecimal.length; i++) {
            if (v.equals(fEnumDecimal[i] ))
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

    public Hashtable getFacets(){
        return null;
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


    /*
    public static void main(String args[]) {
        // simple unit test
        try {
            DatatypeValidator v = new DecimalValidator();
            Hashtable facets = new Hashtable();
            facets.put("minInclusive","0");
            DatatypeValidator nonneg = new DecimalValidator();
            nonneg.setBasetype(v);
            nonneg.setFacets(facets);
            facets = new Hashtable();
            facets.put("minInclusive","-1");
            DatatypeValidator bad = new DecimalValidator();
            bad.setBasetype(nonneg);
            bad.setFacets(facets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public int compare( String content1, String content2){
        return 0;
    }


    private void setBasetype(DatatypeValidator base) {
        fBaseValidator =  base;
    }


}

