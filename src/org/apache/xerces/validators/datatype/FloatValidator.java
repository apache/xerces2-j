/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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

/**
 *
 * @author Ted Leung
 * @author Jeffrey Rodriguez
 * @version
 */

public class FloatValidator implements DatatypeValidator {
   private Locale    fLocale        = null;
   private String    fBaseValidator = "native";
   private float[] _enumFloats      = null;
   private String  _pattern         = null;
   
   private float   _maxInclusive    = Float.MAX_VALUE;
   private float   _maxExclusive    = Float.MAX_VALUE;
   private float   _minInclusive    = Float.MIN_VALUE;
   private float   _minExclusive    = Float.MIN_VALUE;

   private int     _facetsDefined   = 0;

   private boolean isMaxExclusiveDefined = false;
   private boolean isMaxInclusiveDefined = false;
   private boolean isMinExclusiveDefined = false;
   private boolean isMinInclusiveDefined = false;

   private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();

    /**
     * validate that a string matches the real datatype
     * @param content A string containing the content to be validated
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C real type
     */

    public void validate(String content, boolean derivedBylist) 
                  throws InvalidDatatypeValueException {
        if( ! derivedBylist  ){ 
             float f = 0;
             try {
                 f = Float.valueOf(content).floatValue();
             } catch (NumberFormatException nfe) {
                 throw new InvalidDatatypeValueException(
                                                       getErrorString(DatatypeMessageProvider.NotReal,
                                                       DatatypeMessageProvider.MSG_NONE,
                                                       new Object [] { content }));
             }
             boundsCheck(f);

             if (((_facetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) )
                 enumCheck(f);

             } 
         else {
             ;// TODO Derived by list 
             }
    }

    public void setFacets(Hashtable facets) throws UnknownFacetException, 
                           IllegalFacetException, IllegalFacetValueException, ConstrainException {

         for (Enumeration e = facets.keys(); e.hasMoreElements();) {
             String key = (String) e.nextElement();

             if (key.equals(SchemaSymbols.ELT_PATTERN)) {
                 _facetsDefined += DatatypeValidator.FACET_PATTERN;
                 _pattern = (String)facets.get(key);
             } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
                 _facetsDefined += DatatypeValidator.FACET_ENUMERATION;
                 continue; //Treat the enumaration after this for loop
             } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                 _facetsDefined += DatatypeValidator.FACET_MAXINCLUSIVE;
                 String value = null;
                 try {
                      value  = ((String)facets.get(key));
                     _maxInclusive = Float.valueOf(value).floatValue();
                 } catch (NumberFormatException ex ) {
                     throw new IllegalFacetValueException( getErrorString(
                        DatatypeMessageProvider.IllegalFacetValue, 
                        DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                 }
             } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                 _facetsDefined += DatatypeValidator.FACET_MAXEXCLUSIVE;
                 String value = null;
                 try  {
                      value  = ((String)facets.get(key));
                    _maxExclusive = Float.valueOf(value).floatValue();
                 } catch (NumberFormatException ex ) {
                     throw new IllegalFacetValueException( getErrorString(
                        DatatypeMessageProvider.IllegalFacetValue, 
                        DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                 }    
             } else if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                 _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                 String value = null;
                 try {
                     value  = ((String)facets.get(key));
                    _minInclusive  = Float.valueOf(value).floatValue();
                 } catch (NumberFormatException ex ) {
                     throw new IllegalFacetValueException( getErrorString(
                        DatatypeMessageProvider.IllegalFacetValue, 
                        DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                 }
             } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                 _facetsDefined += DatatypeValidator.FACET_MININCLUSIVE;
                 String value = null;
                 try {
                    value  = ((String)facets.get(key));
                   _minExclusive  = Float.valueOf(value).floatValue();
                 } catch (NumberFormatException ex ) {
                    throw new IllegalFacetValueException( getErrorString(
                       DatatypeMessageProvider.IllegalFacetValue, 
                       DatatypeMessageProvider.MSG_NONE, new Object [] { value, key}));
                 }
             } else {
                 throw new IllegalFacetException( getErrorString(  DatatypeMessageProvider.MSG_FORMAT_FAILURE,
                                                                   DatatypeMessageProvider.MSG_NONE,
                                                                   null));
             }
         }
        isMaxExclusiveDefined = ((_facetsDefined & 
                                  DatatypeValidator.FACET_MAXEXCLUSIVE ) != 0 )?true:false;
        isMaxInclusiveDefined = ((_facetsDefined & 
                                  DatatypeValidator.FACET_MAXINCLUSIVE ) != 0 )?true:false;
        isMinExclusiveDefined = ((_facetsDefined &
                                  DatatypeValidator.FACET_MINEXCLUSIVE ) != 0 )?true:false;
        isMinInclusiveDefined = ((_facetsDefined &
                                  DatatypeValidator.FACET_MININCLUSIVE ) != 0 )?true:false;


        if ( isMaxExclusiveDefined && isMaxInclusiveDefined ) {
           throw new ConstrainException(
                                       "It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." ); 
        }
        if ( isMinExclusiveDefined && isMinInclusiveDefined ) {
           throw new ConstrainException(
                                       "It is an error for both minInclusive and minExclusive to be specified for the same datatype." ); 
        }



        if( (_facetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ){
            Vector v = (Vector) facets.get(SchemaSymbols.ELT_ENUMERATION);    
            if (v != null) {
                 _enumFloats = new float[v.size()];
                 for (int i = 0; i < v.size(); i++)
                     try {
                         _enumFloats[i] = Float.valueOf((String) v.elementAt(i)).floatValue();
                         boundsCheck(_enumFloats[i]); // Check against max,min Inclusive, Exclusives
                 } catch (InvalidDatatypeValueException idve) {
                    throw new IllegalFacetValueException(
                                                        getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                       DatatypeMessageProvider.MSG_NONE,
                                                                       new Object [] { v.elementAt(i)}));
                } catch (NumberFormatException nfe) {
                    System.out.println("Internal Error parsing enumerated values for real type");
                }
            }
        }
    }


    public void setBasetype(String base) {
        fBaseValidator =  base;
    }



    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(float f) throws InvalidDatatypeValueException {
        boolean inUpperBound = false;
        boolean inLowerBound = false;

        if( isMaxInclusiveDefined ){
            inUpperBound = ( f <= _maxInclusive );
        }else if( isMaxExclusiveDefined ){
            inUpperBound = ( f <  _maxExclusive );
        }

        if( isMinInclusiveDefined ){
            inLowerBound = ( f >= _minInclusive );
        }else if( isMinExclusiveDefined ){
            inLowerBound = ( f >  _minExclusive );
        }

        if( inUpperBound == false  || inLowerBound == false ) { // within bounds ?
            getErrorString(DatatypeMessageProvider.OutOfBounds,
                           DatatypeMessageProvider.MSG_NONE,
                           new Object [] { new Float(f)});
        }
    }

    private void enumCheck(float v) throws InvalidDatatypeValueException {
        for (int i = 0; i < _enumFloats.length; i++) {
            if (v == _enumFloats[i]) return;
        }
        throw new InvalidDatatypeValueException(
                                               getErrorString(DatatypeMessageProvider.NotAnEnumValue,
                                                              DatatypeMessageProvider.MSG_NONE,
                                                              new Object [] { new Float(v)}));
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }

    public int compare( DatatypeValidator o1, DatatypeValidator o2){
        return 0;
    }

    private String getErrorString(int major, int minor, Object args[]) {
        try {
            return fMessageProvider.createMessage(fLocale, major, minor, args);
        } catch (Exception e) {
            return "Illegal Errorcode "+minor;
        }
    }

}
