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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.text.ParseException;
import org.apache.xerces.validators.schema.SchemaSymbols;

/**
 *
 * TimeInstantValidator validates that XML content is a W3C timeInstant type.
 *
 * @author Ted Leung, George Joseph
 * @version
 */

public class TimeInstantValidator implements DatatypeValidator {

	public static final int CACHE_LIMIT = 300;
	public static final int CACHE_INITIAL_SIZE = 307;
	
	Calendar fMaxInclusive = null;
	boolean fIsMaxInclusive = false;
	Calendar fMaxExclusive = null;
	boolean fIsMaxExclusive = false;
	Calendar fMinInclusive = null;
	boolean fIsMinInclusive = false;
	Calendar fMinExclusive = null;
	boolean fIsMinExclusive = false;
	Calendar[] fEnumValues = null;
	boolean fHasEnums = false;
	String ovalue = null;
	Locale fLocale = null;
	Hashtable facetData = null;	
	TimeDurationValidator fBaseValidator = null;
	private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
	private Hashtable cache = new Hashtable(CACHE_INITIAL_SIZE);

	/**
     * validate that a string is a W3C timeInstant type
     *
     * validate returns true or false depending on whether the string content is an
     * instance of the W3C string datatype
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  not a W3C timeInstant type
     */

	public void validate(String content) throws InvalidDatatypeValueException 
	{
		ovalue=content;
		Calendar c = (Calendar)cache.get(content);
		if (c == null)
		{
			c = normalizeInstant(content.toCharArray(), 0, content.length());
			if (cache.size() < CACHE_LIMIT) cache.put(content,c);
		}
      boundsCheck(c);
      if (fHasEnums)enumCheck(c);
		return;
	}
			
	public void validate(int contentIndex) throws InvalidDatatypeValueException {
	}
	
	public void setFacets(Hashtable facets) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
        if (fBaseValidator != null)
            if (!fBaseValidator.ensureFacetsAreConsistent(facets))
                throw new IllegalFacetValueException(
					getErrorString(DatatypeMessageProvider.FacetsInconsistent,
								   DatatypeMessageProvider.MSG_NONE,
								   null));

	    fIsMinInclusive = fIsMinExclusive = fIsMaxInclusive = fIsMaxExclusive = fHasEnums = false;
	    for (Enumeration e = facets.keys(); e.hasMoreElements();) {
	        String key = (String) e.nextElement();
	        String value = null;
	        if (key.equals(SchemaSymbols.ELT_ENUMERATION)) 
                continue;  // ENUM values passed as a vector & handled after bounds facets	    
    	    value = (String) facets.get(key);   
	        Calendar cValue = null;
	        try {
	            cValue = normalizeInstant(value.toCharArray(), 0, value.length());
	        } catch (InvalidDatatypeValueException nfe) {
	            throw new IllegalFacetValueException(
					getErrorString(DatatypeMessageProvider.IllegalFacetValue,
								   DatatypeMessageProvider.MSG_NONE,
								   new Object [] { value, key }));
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                fIsMinInclusive = true;
	            fMinInclusive = cValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
	            fIsMinExclusive = true;
	            fMinExclusive = cValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
	            fIsMaxInclusive = true;
	            fMaxInclusive = cValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
	            fIsMaxExclusive = true;
	            fMaxExclusive = cValue;
	        } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
	        } else if (key.equals(SchemaSymbols.ELT_PRECISION) ||
                     key.equals(SchemaSymbols.ELT_SCALE) ||
                     key.equals(SchemaSymbols.ELT_LENGTH) ||
                     key.equals(SchemaSymbols.ELT_MINLENGTH) ||
                     key.equals(SchemaSymbols.ELT_MAXLENGTH) ||
                     key.equals(SchemaSymbols.ELT_ENCODING) ||
                     key.equals(SchemaSymbols.ELT_PERIOD) ||
                     key.equals(SchemaSymbols.ELT_PATTERN) )
                throw new IllegalFacetException(
					getErrorString(DatatypeMessageProvider.IllegalIntegerFacet,
								   DatatypeMessageProvider.MSG_NONE,
								   null));
            else 
                throw new UnknownFacetException(
					getErrorString(DatatypeMessageProvider.UnknownFacet,
								   DatatypeMessageProvider.MSG_NONE,
								   new Object [] { key }));
	    }
	    
        // check the enum values after any range constraints are in place
        Vector v = (Vector) facets.get(SchemaSymbols.ELT_ENUMERATION);    
	    if (v != null) {
	        fHasEnums = true;
	        fEnumValues = new Calendar[v.size()];
	        for (int i = 0; i < v.size(); i++)
	            try {
	                fEnumValues[i] = normalizeInstant(((String)v.elementAt(i)).toCharArray(), 0, ((String)v.elementAt(i)).length());
	                boundsCheck(fEnumValues[i]);
	            } catch (InvalidDatatypeValueException idve) {
	                throw new IllegalFacetValueException(
						getErrorString(DatatypeMessageProvider.InvalidEnumValue,
									   DatatypeMessageProvider.MSG_NONE,
									   new Object [] { v.elementAt(i)}));
	            } catch (NumberFormatException nfe) {
	                System.out.println("Internal Error parsing enumerated values for timeInstant type");
	            }
	    }

	}

	public void setBasetype(DatatypeValidator base) {
	    fBaseValidator = (TimeDurationValidator) base;

	}

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
	boolean ensureFacetsAreConsistent(Hashtable facets) {
	    boolean facetsAreConsistent = true;
	    for (Enumeration e = facets.keys(); facetsAreConsistent && e.hasMoreElements();) {
	        String key = (String) e.nextElement();
	        String value = null;
	        if (key.equals(SchemaSymbols.ELT_ENUMERATION)) 
                continue;  // ENUM values passed as a vector & handled after bounds facets	    
    	    value = (String) facets.get(key);   
	        Calendar cValue = null;
	        try {
	            cValue = normalizeInstant(value.toCharArray(), 0, value.length());
	        } catch (InvalidDatatypeValueException nfe) {
                facetsAreConsistent = false;
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE) && fIsMinInclusive) {
                facetsAreConsistent = fMinInclusive.before(cValue) || fMinInclusive.equals(cValue);
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) && fIsMinExclusive) {
	            facetsAreConsistent = fMinExclusive.before(cValue);
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) && fIsMaxInclusive) {
	            facetsAreConsistent = fMaxInclusive.after(cValue) || fMaxInclusive.equals(cValue);
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) && fIsMaxExclusive) {
	            facetsAreConsistent = fMaxExclusive.after(cValue);
	        }
	    }
	    return facetsAreConsistent;
	}
    private void boundsCheck(Calendar c) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;
        if (fIsMaxInclusive)
            maxOk = (c.before(fMaxInclusive) || c.equals(fMaxInclusive));
        else if (fIsMaxExclusive)
            maxOk = c.before(fMaxInclusive);
        else 
            maxOk = (!fIsMaxInclusive && !fIsMaxExclusive);
        
        if (fIsMinInclusive)
            minOk = (c.after(fMinInclusive) || c.equals(fMinInclusive));
        else if (fIsMinExclusive) 
            minOk = c.after(fMinInclusive);
        else 
            minOk = (!fIsMinInclusive && !fIsMinExclusive);
        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException(
				getErrorString(DatatypeMessageProvider.OutOfBounds,
							   DatatypeMessageProvider.MSG_NONE,
							   new Object [] { ovalue }));
    }
    
    private void enumCheck(Calendar c) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumValues.length; i++) {
            if (c.equals(fEnumValues[i])) return;
        }
        throw new InvalidDatatypeValueException(
			getErrorString(DatatypeMessageProvider.NotAnEnumValue,
						   DatatypeMessageProvider.MSG_NONE,
						   new Object [] { ovalue }));
    }
	public void setFacets(int facets[]) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
	}
    private String getErrorString(int major, int minor, Object args[]) {
         try {
             return fMessageProvider.createMessage(fLocale, major, minor, args);
         } catch (Exception e) {
             return "Illegal Errorcode "+minor;
         }
    }
	public static Calendar normalizeInstant(char[] value, int start, int length) throws InvalidDatatypeValueException
	{
		boolean negative=false;
		boolean tznegative=false;
		int tzoffset=0;
		int tzhh=0,tzmm=0;
		int i=start,j=0,k=0,l=0,m=0;
		final char[]ms={'0','0','0'};
		final Calendar cal = new GregorianCalendar();
		final int endindex = (start+length)-1;

		try
		{
			if (length < 16) throw new ParseException("Value is too short.",0);
			cal.clear();
  			cal.setLenient(false);
//	If there's a leading sign, set the appropriate Era.
  			if(value[i]=='-'||value[i]=='+')
  			{
  				cal.set(Calendar.ERA, (value[i]=='-'?GregorianCalendar.BC:GregorianCalendar.AD));
  				i++;
  			}
//	Grab the year (might be > 9999), month, day, hour and minute fields  	
  			j=indexOf(value,i,'-',i+5);
  			if (j==-1 || j>endindex)throw new ParseException("Year separator is missing or misplaced.", i);
  			cal.set(Calendar.YEAR, parseInt(value,i,j-i));
  			i=j+1;
  			cal.set(Calendar.MONTH, parseInt(value,i,2)-1);
  			i+=2;
			if (value[i]!='-')throw new ParseException("Month separator is missing or misplaced.",i);
  			cal.set(Calendar.DAY_OF_MONTH, parseInt(value,i+1,2));
  			i+=3;
  			if (value[i]!='T')throw new ParseException("Time separator is missing or misplaced.",i);
  			cal.set(Calendar.HOUR_OF_DAY, parseInt(value,i+1,2));
  			i+=3;
  			if (value[i]!=':')throw new ParseException("Hour separator is missing or misplaced.",i);
  			cal.set(Calendar.MINUTE, parseInt(value,i+1,2));
  			i+=3;
//	Seconds are optional
 			if((endindex-i)>1 && (value[i]==':'))
  			{
  				cal.set(Calendar.SECOND, parseInt(value,i+1,2));
				i+=3;
// Grab optional fractional seconds to 3 decimal places.
				if (i<endindex && value[i]=='.')
				{
					i++;k=0;
					while((i <= endindex) && (k<3) && Character.isDigit(value[i]))
						ms[k++]=value[i++];

  					cal.set(Calendar.MILLISECOND, parseInt(ms,0,3));
				}
//	Eat any remaining digits.
	  			while(i<=endindex && Character.isDigit(value[i]))  i++;
  			}
//	Check for timezone.
  			if(i<=endindex)
  			{
  				if(value[i]=='Z')
  				{
  					cal.set(Calendar.ZONE_OFFSET, 0);
  				}
//  				else if ((endindex-i)==2 || (endindex-i)==5)
  				else if (value[i]=='-' || value[i]=='+')
  				{
  					tznegative = (value[i]=='-');
  					tzhh=parseInt(value,i+1,2);
  					if ((endindex-i)==5)
  					{
  						if (value[i+3] != ':')throw new ParseException("time zone must be 'hh:mm'.",i);
  						tzmm=parseInt(value,i+4,2);
  					}
  					tzoffset=((tzhh*3600000)+(tzmm*60000));
  					cal.set(Calendar.ZONE_OFFSET, (tznegative?-tzoffset:tzoffset));
  		      }
  				else throw new ParseException("Unrecognized time zone.",i);
			}
			return(cal);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new InvalidDatatypeValueException("Unable to parse timeInstant "+e.toString());
		}
   }

	public static final int indexOf(char[] value, int start, char s)
	{
		return(indexOf(value,start,s,value.length-1));
	}
	public static final int indexOf(char[] value, int start, char s, int max)
	{
		for(int i=start;i<=max;i++)if(value[i]==s) return(i);
		return(-1);
	}
	public static final int indexOneOf(char[] value, int start, String s)
	{
		return(indexOneOf(value,start,s,value.length-1));
	}
	public static final int indexOneOf(char[] value, int start, String s, int max)
	{
		for(int i=start;i<max;i++) 
			for(int j=0;j<s.length();j++) if(value[i] == s.charAt(j))return(i);
		return(-1);
	}
//	parseInt is a copy of the Integer.parseInt method, modified to accept
// a character array.
	public static final int parseInt(char[] s, int start, int length)	throws NumberFormatException
   {
		if (s == null) throw new NumberFormatException("null");
		int radix=10;
		int result = 0;
		boolean negative = false;
		int i= start;
		int limit;
		int multmin;
		int digit=0;

		if (length <= 0) throw new NumberFormatException(new String(s,start,length));
   	if (s[i] == '-') 
		{
			negative = true;
			limit = Integer.MIN_VALUE;
			i++;
    	} 
		else if(s[i]=='+')
		{
			negative = false;
			limit = -Integer.MAX_VALUE;
			i++;
    	} 
		else 
		{
			limit = -Integer.MAX_VALUE;
    	}
    	multmin = limit / radix;
    	if (i < (start+length)) 
		{
			digit = Character.digit(s[i++],radix);
			if (digit < 0) throw new NumberFormatException(new String(s,start,length));
			else result = -digit;
      }
    	while (i < (start+length)) 
		{
			digit = Character.digit(s[i++],radix);
			if (digit < 0) throw new NumberFormatException(new String(s,start,length));
			if (result < multmin) throw new NumberFormatException(new String(s,start,length));
			result *= radix;
			if (result < limit + digit) throw new NumberFormatException(new String(s,start,length));
			result -= digit;
		}

		if (negative) 
		{
	    	if (i > 1) return result;
	    	else throw new NumberFormatException(new String(s,start,length));
	   }
		return -result;
   }
   
}
