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

package org.apache.xerces.impl.validation.datatypes;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.ParseException;
import org.apache.xerces.impl.validation.DatatypeValidator;
import org.apache.xerces.impl.validation.grammars.SchemaSymbols;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.InvalidDatatypeFacetException;
import org.apache.xerces.impl.validation.InvalidDatatypeValueException;



/**
 * TimeDuration Datatype Validator
 * @version $Id$
 */

public class TimeDurationDatatypeValidator extends AbstractDatatypeValidator {

   private Locale            fLocale           = null;
   private DatatypeValidator fBaseValidator    = null; // A Native datatype is null
   private String            fPattern          = null;
   private long              fMaxInclusive     = 0L;
   private long              fMaxExclusive     = 0L;
   private long              fMinInclusive     = 0L;
   private long              fMinExclusive     = 0L;
   private long              fDuration         = 0L;
   private long              fPeriod           = 0L;


   private boolean           isMaxExclusiveDefined = false;
   private boolean           isMaxInclusiveDefined = false;
   private boolean           isMinExclusiveDefined = false;
   private boolean           isMinInclusiveDefined = false;

   private int               fFacetsDefined        = 0;

   private boolean           fDerivedByList        = false;


   private long[]            fEnumTimeDuration = null; // Time duration is represented internally as longs

   private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();


    public  TimeDurationDatatypeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction
    }

    public  TimeDurationDatatypeValidator ( DatatypeValidator base, Hashtable facets, 
                  boolean derivedByList ) throws InvalidDatatypeFacetException {

    }


    /**
     * validates a String to be a Lexical representation
     * of a TimeDuration Datatype.
     * 
     * @param content A string containing the content to be validated
     * @param state
     * @return 
     * @exception InvalidDatatypeValueException
     *                   If String is does not represent a
     *                   valid TimeDuration datatype.
     */
    public void validate(String content, Object state) 
                                       throws InvalidDatatypeValueException{
        long normalizedValue;
    }


    /**
     * set the base type for this datatype
     *
     * @param base the validator for this type's base type
     *
     */
    public void setBasetype(DatatypeValidator base) {
        fBaseValidator = base;
    }


    /**
    * set the locate to be used for error messages
    */
    public void setLocale(Locale locale) {
    }


    public int compare( String content1, String content2) {
        return -1;
    }

    public Hashtable getFacets(){
        return null;
    }

  /**
     * Returns a copy of this object.
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }





    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(long f) throws InvalidDatatypeFacetException {
        boolean inUpperBound = false;
        boolean inLowerBound = false;

        if ( isMaxInclusiveDefined ) {
            inUpperBound = ( f <= fMaxInclusive );
        } else if ( isMaxExclusiveDefined ) {
            inUpperBound = ( f <  fMaxExclusive );
        }

        if ( isMinInclusiveDefined ) {
            inLowerBound = ( f >= fMinInclusive );
        } else if ( isMinExclusiveDefined ) {
            inLowerBound = ( f >  fMinExclusive );
        }

        if ( inUpperBound == false  || inLowerBound == false ) { // within bounds ?
            throw new InvalidDatatypeFacetException(
                                                   getErrorString(DatatypeMessageProvider.OutOfBounds,
                                                                  DatatypeMessageProvider.MSG_NONE,
                                                                  new Object [] { new Long(f),"","","",""}));//REVISIT
        }
    }

    private void enumCheck(long d) throws InvalidDatatypeValueException {
        for (int i = 0; i < this.fEnumTimeDuration.length; i++) {
            if (d == fEnumTimeDuration[i]) return;
        }
        throw new InvalidDatatypeValueException(
			getErrorString(DatatypeMessageProvider.NotAnEnumValue,
				       	   DatatypeMessageProvider.MSG_NONE,
				       	   new Object [] { new Long( d ) }));
    }




    private String getErrorString(int major, int minor, Object args[]) {
        //return fMessageProvider.createMessage(fLocale, major, minor, args);
        return fMessageProvider.formatMessage(fLocale, null, null );
    }


    private static final int indexOf(char[] value, int start, char s)
    {
        return(indexOf(value,start,s,value.length-1));
    }
    private static final int indexOf(char[] value, int start, char s, int max)
    {
        for (int i=start;i<=max;i++)if (value[i]==s) return(i);
        return(-1);
    }
    private static final int indexOneOf(char[] value, int start, String s)
    {
        return(indexOneOf(value,start,s,value.length-1));
    }
    private static final int indexOneOf(char[] value, int start, String s, int max)
    {
        for (int i=start;i<max;i++)
            for (int j=0;j<s.length();j++) if (value[i] == s.charAt(j))return(i);
        return(-1);
    }
}

