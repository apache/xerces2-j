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
 * originally based on software copyright (c) 2001, International
 * Business Machines, Inc., http://www.apache.org.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.xerces.validators.datatype;

import java.util.Vector;
import java.util.Enumeration;
import org.apache.xerces.utils.regex.RegularExpression;
import org.apache.xerces.validators.schema.SchemaSymbols;
import java.util.Hashtable;


/**
 * This is the base class of all date/time datatype validators.
 * It implements common code for parsing, validating and comparing datatypes.
 * Classes that extend this class, must implement parse() method.
 * 
 * @author Elena Litani
 * @author Len Berman  
 *
 * @version $Id$
 */

public abstract class DateTimeValidator extends AbstractDatatypeValidator {

    //debugging    
    private static final boolean DEBUG=false;

    //define facets allowed on schema date/time
    protected int[]  fMaxInclusive = null;
    protected int[]  fMaxExclusive = null;
    protected int[]  fMinInclusive = null;
    protected int[]  fMinExclusive = null;
    protected int[][] fEnumeration  = null;
    protected int  fEnumSize  = 0;

    //define shared variables for date/time

    //define constants
    protected final static int CY = 0,  M = 1, D = 2, h = 3, 
    m = 4, s = 5, ms = 6, utc=7, hh=0, mm=1;

    //comparison
    protected static final short LESS_THAN=-1;
    protected static final short EQUAL=0;
    protected static final short GREATER_THAN=1;
    protected static final short INDETERMINATE=2;

    //size for all objects must have the same fields: 
    //CCYY, MM, DD, h, m, s, ms + timeZone
    protected final static int TOTAL_SIZE = 8;

    //date obj size for gMonth datatype (without time zone): --09--
    protected final static int MONTH_SIZE = 6; 

    //date obj must have at least 6 chars after year (without time zone): "-MM-DD"
    private final static int YEARMONTH_SIZE = 6;

    //define constants to be used in assigning default values for 
    //all date/time excluding duration
    protected final static int YEAR=2000;
    protected final static int MONTH=01;
    protected final static int DAY = 15;

    //obj to store timeZone for date/time object excluding duration
    protected int[] timeZone = new int[2];


    //size of string buffer
    protected int fEnd = 30; 
    protected int fStart = 0;

    //storage for string value of date/time object 
    protected StringBuffer fBuffer = new StringBuffer(fEnd);     

    //obj to store all date/time objects with fields:
    // {CY, M, D, h, m, s, ms, utc}
    protected int[] fDateValue = new int[TOTAL_SIZE];
    private int[] fTempDate = new int[TOTAL_SIZE];

    //error message buffer
    protected StringBuffer message = new StringBuffer(TOTAL_SIZE);
    //
    //REVISIT:  more error checking, general debuging/common code clean up
    //


    //default constractor

    public  DateTimeValidator () throws InvalidDatatypeFacetException {
        this( null, null, false ); // Native, No Facets defined, Restriction

    }

    public DateTimeValidator (DatatypeValidator base, Hashtable facets, boolean derivedByList ) 
    throws InvalidDatatypeFacetException {
        if ( base != null ) {
            fBaseValidator = base; // Set base type 
            fFacets = facets;
        }

        String value;
        if ( facets != null ) {

            for ( Enumeration e = facets.keys(); e.hasMoreElements(); ) {

                String key = (String) e.nextElement();
                if ( key.equals(SchemaSymbols.ELT_PATTERN) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_PATTERN;
                    fPattern = (String)facets.get(key);
                    if ( fPattern != null )
                        fRegex = new RegularExpression(fPattern, "X" );
                }
                else if ( key.equals(SchemaSymbols.ELT_ENUMERATION) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_ENUMERATION;
                    continue; //Treat the enumaration after this for loop
                }
                else if ( key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MAXINCLUSIVE;
                    value = ((String)facets.get(key));
                    try {
                        fMaxInclusive = parse(value, null);
                    }
                    catch ( Exception exc ) {
                        reportError("fMaxInclusive", value);
                        fMaxInclusive = null;
                    }
                }
                else if ( key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MAXEXCLUSIVE;
                    value  = ((String)facets.get(key));
                    try {
                        fMaxExclusive = parse(value, null);
                    }
                    catch ( Exception exc ) {
                        reportError("fMaxExclusive", value);
                        fMaxExclusive = null;
                    }
                }
                else if ( key.equals(SchemaSymbols.ELT_MININCLUSIVE) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MININCLUSIVE;
                    value  = ((String)facets.get(key));
                    try {
                        fMinInclusive = parse(value, null);
                    }
                    catch ( Exception exc ) {
                        reportError("fMinInclusive", value);
                        fMinInclusive = null;
                    }
                }
                else if ( key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) ) {
                    fFacetsDefined |= DatatypeValidator.FACET_MINEXCLUSIVE;
                    value  = ((String)facets.get(key));
                    try {
                        fMinExclusive = parse(value, null);
                    }
                    catch ( Exception exc ) {
                        reportError("fMinExclusive", value);
                        fMinExclusive = null;
                    }
                }
                else {
                    reportError("Invalid datatype facet", key);
                }

            }
        }// End Facet definition
        
        // check 4.3.8.c1 error: maxInclusive + maxExclusive
        if ( fMaxExclusive!=null && fMaxInclusive != null ) {
            System.err.println( "[Error] It is an error for both maxInclusive and maxExclusive to be specified for the same datatype." );
        }
        // check 4.3.9.c1 error: minInclusive + minExclusive
        if ( fMinExclusive !=null && fMinInclusive !=null ) {
            System.err.println("[Error] It is an error for both minInclusive and minExclusive to be specified for the same datatype." );
        }

        // check 4.3.7.c1 must: minInclusive <= maxInclusive
        int compare;
        
        if ( fMaxInclusive != null && fMinInclusive !=null ) {
            compare = compareDates(fMinInclusive, fMaxInclusive, false);
            if ( compare == GREATER_THAN || compare == INDETERMINATE )
                System.err.println("[Error] minInclusive value ='" + dateToString(fMinInclusive) 
                                    + "'must be <= maxInclusive value ='" +
                                    dateToString(fMaxInclusive) + "'. " );
        }
       
        // check 4.3.8.c2 must: minExclusive <= maxExclusive ??? minExclusive < maxExclusive
        if ( fMaxExclusive !=null && fMinExclusive !=null ) {
            compare = compareDates(fMinExclusive,fMaxExclusive, false);
            if ( compare == GREATER_THAN || compare == INDETERMINATE )
                System.err.println( "[Error] minExclusive value ='" +dateToString(fMinExclusive)
                                    + "'must be <= maxExclusive value ='" +
                                    dateToString(fMaxExclusive) + "'. " );
        }
         
        // check 4.3.9.c2 must: minExclusive < maxInclusive
        if ( fMaxInclusive != null && fMinExclusive !=null ) {
            compare = compareDates(fMinExclusive, fMaxInclusive, true);
            if ( compare != LESS_THAN )
                System.err.println( "[Error] minExclusive value ='" + dateToString(fMinExclusive) 
                                    + "'must be < maxInclusive value ='" +
                                    dateToString(fMaxInclusive) + "'. " );
        }
        // check 4.3.10.c1 must: minInclusive < maxExclusive
        if ( fMaxExclusive !=null && fMinInclusive !=null ) {
            compare = compareDates(fMinInclusive, fMaxExclusive, true); 
            if ( compare != LESS_THAN )
                System.err.println( "[Error] minInclusive value ='" + dateToString(fMinInclusive) 
                                    + "'must be < maxExclusive value ='" +
                                    dateToString(fMaxExclusive) + "'. " );
        }
        //
        //REVISIT: Inherit facets from the base type
        //

        if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
            Vector v = (Vector) facets.get(SchemaSymbols.ELT_ENUMERATION);    
            if ( v != null ) {
                fEnumSize = v.size();
                fEnumeration = new int[fEnumSize][];
                for ( int i=0; i<fEnumSize; i++ ) {
                    try {
                        fEnumeration[i] = parse((String)v.elementAt(i), null);
                    }
                    catch ( Exception exc ) {
                        fEnumSize=i;
                        reportError("enumeration", (String)v.elementAt(i));
                        break;
                    }
                }
            }
        }
        
    }
    /**
     * Implemented by each subtype, calling appropriate function to parse
     * given date/time
     * 
     * @param content String value of the date/time
     * @param date    Storage to represent date/time object.
     *                If null - new object will be created, otherwise
     *                date will be reset and reused
     * @return updated date/time object
     * @exception Exception
     */
    abstract protected int[] parse(String content, int[] date) throws Exception;

    /**
     * Validate that a string is a W3C date/time type
     * 
     * @param content string value of date/time
     * @param state
     * @return  
     * @exception InvalidDatatypeValueException
     */
    public Object validate(String content, Object state) throws InvalidDatatypeValueException{

        try {
            resetDateObj(fDateValue);
            parse(content, fDateValue);
        }
        catch ( Exception e ) {
            throw new InvalidDatatypeValueException("Value '"+content+
                                                    "' is not legal value for current datatype" );
        }
        validateDate (fDateValue, content);
        return null;
    }

    /**
     * Validates date object against facet and base datatype
     * 
     * @param date    represents date/time obj
     * @param content lexical representation of date/time obj
     * @exception InvalidDatatypeValueException
     */
    protected void validateDate (int[] date, String content) throws InvalidDatatypeValueException{

        if ( this.fBaseValidator != null ) {//validate against parent type if any
            if ( (fFacetsDefined & DatatypeValidator.FACET_PATTERN ) != 0 ) {
                if ( fRegex == null || fRegex.matches( content) == false )
                    throw new InvalidDatatypeValueException("Value'"+content+
                                                            "' does not match regular expression facet " + fRegex.getPattern() );
            }
            //validate against base type
            ((DateTimeValidator)this.fBaseValidator).validateDate( date, content);
            if ( (fFacetsDefined & DatatypeValidator.FACET_ENUMERATION ) != 0 ) {
                int count=0;
                boolean valid = false;
                while ( count < fEnumSize ) {
                    if ( compareDates(date, fEnumeration[count], false) == EQUAL ) {
                        valid = true;
                        break;
                    }
                    count++;
                }
                if ( !valid ) {
                    throw new InvalidDatatypeValueException("Value'"+content+
                                                            "' does not match enumeration values" );
                }
            }

            // REVISIT: output values for facets in error message
            short c;
            if ( fMinInclusive != null ) {

                c = compareDates(date, fMinInclusive, false);
                if ( c == LESS_THAN || c == INDETERMINATE ) {
                    throw new InvalidDatatypeValueException("Value '"+content+
                                                            "' is less than minInclusive: " +dateToString(fMinInclusive) );
                }
            }
            if ( fMinExclusive != null ) {

                if ( compareDates(date, fMinExclusive, true) != GREATER_THAN ) {
                    throw new InvalidDatatypeValueException("Value '"+content+
                                                            "' is less than or equal to minExclusive: " +dateToString(fMinExclusive));
                }
            }

            if ( fMaxInclusive != null ) {

                c = compareDates(date, fMaxInclusive, false );
                if ( c  == GREATER_THAN  || c == INDETERMINATE ) {
                    throw new InvalidDatatypeValueException("Value '"+content+
                                                            "' is greater than maxInclusive: " +dateToString(fMaxInclusive) );
                }
            }

            if ( fMaxExclusive != null ) {

                if ( compareDates(date, fMaxExclusive, true ) != LESS_THAN ) {
                    throw new InvalidDatatypeValueException("Value '"+content+
                                                            "' is greater than or equal to maxExlusive: " +dateToString(fMaxExclusive) );
                }
            }
        }
        else {
            return;
        }

    }

    public int compare( String content1, String content2) {
        //implement compareDates using the compare() method
        return -1;
    }


    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("clone() is not supported in "+this.getClass().getName());
    }



    /**
     * Compare algorithm described in dateDime (3.2.7).
     * Duration datatype overwrites this method
     * 
     * @param date1  normalized date representation of the first value
     * @param date2  normalized date representation of the second value
     * @param strict
     * @return less, greater, less_equal, greater_equal, equal
     */
    protected  short compareDates(int[] date1, int[] date2, boolean strict) {
        if ( date1[utc]==date2[utc] ) {
            return compareOrder(date1, date2);    
        }
        short c1, c2;

        if ( date1[utc]=='Z' ) {

            //compare date1<=date1<=(date2 with time zone -14)
            //
            cloneDate(date2); //clones date1 value to global temporary storage: fTempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            fTempDate[utc]='+';
            normalize(fTempDate);
            c1 = compareOrder(date1, fTempDate);

            //compare date1>=(date2 with time zone +14)
            //
            cloneDate(date2); //clones date1 value to global temporary storage: fTempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            fTempDate[utc]='-';
            normalize(fTempDate);
            c2 = compareOrder(date1, fTempDate);

            if ( (c1==LESS_THAN && c2==GREATER_THAN) ||
                 (c1==GREATER_THAN && c2==LESS_THAN) ) {
                return INDETERMINATE; 
            }
            //REVISIT: wait for clarification on this case from schema
            return(c1!=INDETERMINATE)?c1:c2;
        }
        else if ( date2[utc]=='Z' ) {

            //compare (date1 with time zone -14)<=date2 
            //
            cloneDate(date1); //clones date1 value to global temporary storage: fTempDate
            timeZone[hh]=14;
            timeZone[mm]=0;

            fTempDate[utc]='+';
            if (DEBUG) {
               System.out.println("fTempDate=" + dateToString(fTempDate));
            }
            normalize(fTempDate);
            c1 = compareOrder(fTempDate, date2);
            if (DEBUG) {
                System.out.println("date=" + dateToString(date2));
                System.out.println("fTempDate=" + dateToString(fTempDate));
            }
            //compare (date1 with time zone +14)<=date2 
            //
            cloneDate(date1); //clones date1 value to global temporary storage: fTempDate
            timeZone[hh]=14;
            timeZone[mm]=0;
            fTempDate[utc]='-';
            normalize(fTempDate);
            c2 = compareOrder(fTempDate, date2);
            if (DEBUG) {
               System.out.println("fTempDate=" + dateToString(fTempDate));
            }
            if ( (c1==LESS_THAN && c2==GREATER_THAN) ||
                 (c1==GREATER_THAN && c2==LESS_THAN) ) {
                return INDETERMINATE; 
            }
            //REVISIT: wait for clarification on this case from schema
            return(c1!=INDETERMINATE)?c1:c2;
        }
        return INDETERMINATE;

    }


    /**
     * Given normalized values, determines order-relation
     * between give date/time objects.
     * 
     * @param date1  date/time object
     * @param date2  date/time object
     * @return 
     */
    protected short compareOrder (int[] date1, int[] date2) {
        for ( int i=0;i<TOTAL_SIZE;i++ ) {
            if ( date1[i]<date2[i] ) {
                return LESS_THAN;
            }
            else if ( date1[i]>date2[i] ) {
                return GREATER_THAN;
            }
        }
        return EQUAL;
    }


    /**
     * Parses time hh:mm:ss.sss and time zone if any
     * 
     * @param start
     * @param end
     * @param data
     * @return 
     * @exception Exception
     */
    protected  void getTime (int start, int end, int[] data) throws Exception{
        //get hours (hh)
        data[h]=parseInt(start,start+2);

        //get minutes (mm)
        start+=3;
        data[m]=parseInt(start,start+2);

        //get seconds (ss)
        start+=3;                
        data[s]=parseInt(start,start+2);

        //get miliseconds (ms)
        int milisec = indexOf(start, end, '.');

        //find UTC sign if any
        int sign = findUTCSign((milisec!=-1)?milisec:start, end);

        //parse miliseconds 
        if ( milisec != -1 ) {

            if ( sign<0 ) {

                //get all digits after "." 
                data[ms]=parseInt(milisec+1,fEnd);
            }
            else {

                //get ms before UTC sign
                data[ms]=parseInt(milisec+1,sign);
            }

        }

        //parse UTC time zone (hh:mm) or (hh)
        if ( sign>0 ) {
            getTimeZone(data,sign);
        }
    }


    /**
     * Parses date CCYY-MM-DD
     * 
     * @param start
     * @param end
     * @param data
     * @return 
     * @exception Exception
     */
    protected void getDate (int start, int end, int[] date) throws Exception{

        getYearMonth(start, end, date);

        //fStart points to the first '-' after year.
        int stop =fStart+YEARMONTH_SIZE; 
        date[D]=parseInt(stop-2, stop);
    }

    /**
     * Parses date CCYY-MM
     * 
     * @param start
     * @param end
     * @param data
     * @return 
     * @exception Exception
     */
    protected void getYearMonth (int start, int end, int[] date) throws Exception{

        if ( fBuffer.charAt(0)=='-' ) {
            // REVISIT: date starts with preceding '-' sign
            //          do we have to do anything with it?
            //
            start++;
        }
        int i = indexOf(start, end, '-');
        if ( i==-1 ) throw new Exception("Year separator is missing or misplaced.");
        fStart=i; //position after the Year
        date[CY]=parseInt(start,i);
        start = ++i;
        date[M]=parseInt(start, start+2);
    }



    /**
     * Shared code from Date and YearMonth datatypes.
     * Finds if time zone sign is present
     * 
     * @param end
     * @param date
     * @return 
     * @exception Exception
     */
    protected void parseTimeZone (int end, int[] date) throws Exception{

        //fStart points to first '-' after the year
        int start = fStart+YEARMONTH_SIZE;
        if ( start<fEnd ) {
            int sign = findUTCSign(start, fEnd);
            if ( sign<0 ) {
                throw new Exception ("Error in month parsing");
            }
            else {
                getTimeZone(date, sign);
            }
        }
    }

    /**
     * Parses time zone: 'Z' or {+,-} followed by hh or hh:mm
     * 
     * @param data
     * @param sign
     * @return 
     */
    protected void getTimeZone (int[] data, int sign) throws Exception{
        data[utc]=fBuffer.charAt(sign);
        if ( fBuffer.charAt(sign) == 'Z' ) {
            if (fEnd>(++sign)) {
                throw new Exception("Error in parsing time zone");
            }
            return;
        }
        if ( sign<=(fEnd-3) ) {
            
            //parse [hh]
            timeZone[hh]=parseInt(++sign, sign+2);
            if ( data[utc]=='-' ) {
                timeZone[hh]*=-1;
            }
            sign+=3;

            //parse [ss]
            if ( (sign+2)<=fEnd ) {
                timeZone[mm]=parseInt(sign, sign+2);
                if ( data[utc]=='-' ) {
                    timeZone[mm]*=-1;
                }
                if ( sign+2!=fEnd ) {
                    //REVISIT: report an error
                }
            }
        }
        else {
            throw new Exception("Error in parsing time zone");
        }
        if ( DEBUG ) {
            System.out.println("time[hh]="+timeZone[hh] + " time[mm]=" +timeZone[mm]);
        }
    }



    /**
     * Computes index of given char within StringBuffer
     * 
     * @param start
     * @param end
     * @param ch     character to look for in StringBuffer
     * @return index of ch within StringBuffer
     */
    protected  int indexOf (int start, int end, char ch) {
        for ( int i=start;i<end;i++ ) {
            if ( fBuffer.charAt(i) == ch ) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Validates given date/time object accoring to W3C PR Schema 
     * [D.1 ISO 8601 Conventions]
     * 
     * @param data
     * @return 
     */
    protected boolean validateDateTime (int[]  data) {

        //REVISIT: should we throw an exception for not valid dates
        //          or reporting an error message should be sufficient?  
        if ( data[CY]==0 ) {
            System.err.println("[Error]: The year \"0000\" is an illegal year value.");
            return false;
        }

        if ( data[M]<1 || data[M]>12 ) {
            System.err.println("[Error]: The month must have values 1 to 12.");
            return false;
        }

        //validate days
        if ( data[D]>maxDayInMonthFor(data[CY], data[M]) ) {
            System.err.println("[Error]: The day must have values 1 to 31.");
        }

        //validate hours
        if ( data[h]>23 || data[h]<0 ) {
            System.err.println("[Error] Hour must have values 0-23.");
            return false;
        }

        //validate
        if ( data[m]>59 || data[m]<0 ) {
            System.err.println("[Error] Minute must have values 0-59.");
            return false;
        }

        //validate
        if ( data[s]>60 || data[s]<0 ) {
            System.err.println("[Error] Second must have values 0-60.");
            return false;
        }

        //validate
        if ( timeZone[hh]>14 || timeZone[hh]<-14 ) {
            System.err.println("[Error] Time zone should have range -14..+14.");
            return false;
        }

        //validate
        if ( timeZone[mm]>59 || timeZone[mm]<-59 ) {
            System.err.println("[Error] Minute must have values 0-59.");
            return false;
        }
        return true;
    }


    /**
     * Return index of UTC char: 'Z', '+', '-'
     * 
     * @param start
     * @param end
     * @return 
     */
    protected int findUTCSign (int start, int end) {
        int c;
        for ( int i=start;i<end;i++ ) {
            c=fBuffer.charAt(i);
            if ( c == 'Z' || c=='+' || c=='-' ) {
                return i;
            }

        }
        return -1;
    }


    /**
     * Given start and end position, parses string value
     * 
     * @param value  string to parse
     * @param start  Start position
     * @param end    end position
     * @return  return integer representation of characters
     */
    protected  int parseInt (int start, int end) 
    throws NumberFormatException{ 
        //REVISIT: more testing on this parsing needs to be done.
        int radix=10;
        int result = 0;
        int digit=0;
        int limit = -Integer.MAX_VALUE;
        int multmin = limit / radix;
        int i = start;
        do {
            digit = Character.digit(fBuffer.charAt(i),radix);
            if ( digit < 0 ) throw new NumberFormatException();
            if ( result < multmin ) throw new NumberFormatException();
            result *= radix;
            if ( result < limit + digit ) throw new NumberFormatException();
            result -= digit;

        }while ( ++i < end );
        return -result;
    }


    /**
     * If timezone present - normalize dateTime  [E Adding durations to dateTimes]
     * 
     * @param date   CCYY-MM-DDThh:mm:ss+03
     * @return CCYY-MM-DDThh:mm:ssZ
     */
    protected  void normalize (int[] date) {

        //add minutes (from time zone)
        int negate = 1;
        if (date[utc]=='-') {
            negate = -1;
        }
        if ( DEBUG ) {
            System.out.println("==>date[m]"+date[m]);
            System.out.println("==>timeZone[mm]" +timeZone[mm]);
        }
        int temp = date[m] + negate*timeZone[mm];
        int carry = fQuotient (temp, 60);
        date[m]= mod(temp, 60, carry);
        //revisit? negative value

        if ( DEBUG ) {
            System.out.println("==>carry: " + carry);
        }
        //add hours
        temp = date[h] + negate*timeZone[hh] + carry;
        carry = fQuotient(temp, 24);
        date[h]=mod(temp, 24, carry);
        if ( DEBUG ) {
            System.out.println("==>date[h]"+date[h]);
            System.out.println("==>carry: " + carry);
        }

        //REVISIT: remove common code
        //add days
        date[D]=date[D]+carry;

        while ( true ) {
            temp=maxDayInMonthFor(date[CY], date[M]);
            if ( date[D]>temp ) {
                date[D]=date[D]-temp;
                carry=1;
            }
            else {
                break;
            }
            temp=date[M]+carry;
            date[M]=modulo(temp, 1, 13);
            date[CY]=date[CY]+fQuotient(temp, 1, 13);
        }

        date[utc]='Z';  
    }


    /**
     * Resets fBuffer to store string representation of 
     * date/time
     * 
     * @param str    Lexical representation of date/time
     */
    protected void resetBuffer (String str) {
        fBuffer.setLength(0);
        fStart=fEnd=0;
        timeZone[hh]=timeZone[mm]=0;
        fBuffer.append(str);
        fEnd = fBuffer.length();
    }


    /**
     * Resets object representation of date/time
     * 
     * @param data   date/time object
     */
    protected void resetDateObj (int[] data) {
        for ( int i=0;i<TOTAL_SIZE;i++ ) {
            data[i]=0;
        }
    }


    /**
     * Given {year,month} computes maximum 
     * number of days for given month
     * 
     * @param year
     * @param month
     * @return 
     */
    protected int maxDayInMonthFor(int year, int month) {
        //validate days
        if ( month==4 || month==6 || month==9 || month==11 ) {
            return 30;
        }
        else if ( month==2 ) {
            if ( isLeapYear(year) ) {
                return 29;
            }
            else {
                return 28;
            }
        }
        else {
            return 31;
        }
    }


    private boolean isLeapYear(int year) {

        //REVISIT: should we take care about Julian calendar? 
        return((year%4 == 0) && ((year%100 != 0) || (year%400 == 0))); 
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //    
    protected int mod (int a, int b, int quotient) {
        //modulo(a, b) = a - fQuotient(a,b)*b 
        return (a - quotient*b) ;
    }
    
    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int fQuotient (int a, int b) {
        
        //fQuotient(a, b) = the greatest integer less than or equal to a/b 
        return (int)Math.floor((float)a/b);
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //    
    protected int modulo (int temp, int low, int high) {
        //modulo(a - low, high - low) + low 
        int a = temp - low;
        int b = high - low;
        return (mod (a, b, fQuotient(a, b)) + low) ;
    }

    //
    // help function described in W3C PR Schema [E Adding durations to dateTimes]
    //
    protected int fQuotient (int temp, int low, int high) {
        //fQuotient(a - low, high - low) 
  
        return fQuotient(temp - low, high - low);
    }


    protected String dateToString(int[] date) {
        message.setLength(0);
        int negate = 1;
        if ( date[CY]<0 ) {
            message.append('-');
            negate=-1;
        }
        message.append(negate * date[CY]);
        message.append('-');
        message.append(negate * date[M]);
        message.append('-');
        message.append(negate * date[D]);
        message.append('T');
        message.append(negate * date[h]);
        message.append(':');
        message.append(negate * date[m]);
        message.append(':');
        message.append(negate * date[s]);
        message.append('.');
        message.append(negate * date[ms]);
        message.append((char)date[utc]);
        return message.toString();
    }


    /**
     * Use this function to report errors in constructor
     * 
     * @param msg
     * @param value
     */
    protected void reportError(String msg, String value) {
        System.err.println("[Error]: " +msg+": Value  '"+value+"' is not legal for current datatype");
    }


    //
    //Private help functions
    //

    private void cloneDate (int[] finalValue) {
        resetDateObj(fTempDate);
        for ( int i=0;i<TOTAL_SIZE;i++ ) {
            fTempDate[i]=finalValue[i];
        }
    }

}
