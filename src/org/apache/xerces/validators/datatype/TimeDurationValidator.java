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
 * TimeDurationValidator validates that XML content is a W3C timeDuration.
 *
 * @author Ted Leung, George Joseph
 * @version
 */

public class TimeDurationValidator implements DatatypeValidator {

	public static final int CACHE_LIMIT = 300;
	public static final int CACHE_INITIAL_SIZE = 307;
	
	long fMaxInclusive = 0;
	boolean fIsMaxInclusive = false;
	long fMaxExclusive = 0;
	boolean fIsMaxExclusive = false;
	long fMinInclusive = 0;
	boolean fIsMinInclusive = false;
	long fMinExclusive = 0;
	boolean fIsMinExclusive = false;
	long fEnumValues[] = null;
	boolean fHasEnums = false;
	String ovalue = null;
	Locale fLocale = null;
	Hashtable facetData = null;	
	TimeDurationValidator fBaseValidator = null;
	private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
	private Hashtable cache = new Hashtable(CACHE_INITIAL_SIZE);

	/**
     * validate that a string is a W3C timeDuration type
     *
     * validate returns true or false depending on whether the string content is an
     * instance of the W3C timeDuration datatype
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  not a W3C timeDuration type
     */

	public void validate(String content) throws InvalidDatatypeValueException 
	{
		ovalue=content;
		Long d = ((Long)cache.get(content));
		if (d==null)
		{
			d = new Long(normalizeDuration(content.toCharArray(), 0, content.length()));
			if (cache.size() < CACHE_LIMIT) cache.put(content, d);
		}
      boundsCheck(d.longValue());
      if (fHasEnums)enumCheck(d.longValue());
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
	        long dValue = 0;
	        try {
	            dValue = normalizeDuration(value.toCharArray(), 0, value.length());
	        } catch (InvalidDatatypeValueException nfe) {
	            throw new IllegalFacetValueException(
					getErrorString(DatatypeMessageProvider.IllegalFacetValue,
								   DatatypeMessageProvider.MSG_NONE,
								   new Object [] { value, key }));
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                fIsMinInclusive = true;
	            fMinInclusive = dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
	            fIsMinExclusive = true;
	            fMinExclusive = dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
	            fIsMaxInclusive = true;
	            fMaxInclusive = dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
	            fIsMaxExclusive = true;
	            fMaxExclusive = dValue;
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
	        fEnumValues = new long[v.size()];
	        for (int i = 0; i < v.size(); i++)
	            try {
	                fEnumValues[i] = normalizeDuration(((String)v.elementAt(i)).toCharArray(), 0, ((String)v.elementAt(i)).length());
	                boundsCheck(fEnumValues[i]);
	            } catch (InvalidDatatypeValueException idve) {
	                throw new IllegalFacetValueException(
						getErrorString(DatatypeMessageProvider.InvalidEnumValue,
									   DatatypeMessageProvider.MSG_NONE,
									   new Object [] { v.elementAt(i)}));
	            } catch (NumberFormatException nfe) {
	                System.out.println("Internal Error parsing enumerated values for timeDuration type");
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
	        long dValue = 0;
	        try {
	            dValue = normalizeDuration(value.toCharArray(), 0, value.length());
	        } catch (InvalidDatatypeValueException nfe) {
                facetsAreConsistent = false;
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE) && fIsMinInclusive) {
                facetsAreConsistent = fMinInclusive <= dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) && fIsMinExclusive) {
	            facetsAreConsistent = fMinExclusive < dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) && fIsMaxInclusive) {
	            facetsAreConsistent = fMaxInclusive >= dValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) && fIsMaxExclusive) {
	            facetsAreConsistent = fMaxExclusive > dValue;
	        }
	    }
	    return facetsAreConsistent;
	}
    private void boundsCheck(long d) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;
        if (fIsMaxInclusive)
            maxOk = (d <= fMaxInclusive);
        else if (fIsMaxExclusive)
            maxOk = (d < fMaxExclusive);
        else 
            maxOk = (!fIsMaxInclusive && !fIsMaxExclusive);
        
        if (fIsMinInclusive)
            minOk = (d >= fMinInclusive);
        else if (fIsMinExclusive) 
            minOk = (d > fMinInclusive);
        else 
            minOk = (!fIsMinInclusive && !fIsMinExclusive);
        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException(
				getErrorString(DatatypeMessageProvider.OutOfBounds,
							   DatatypeMessageProvider.MSG_NONE,
							   new Object [] { ovalue }));
    }
    
    private void enumCheck(long d) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumValues.length; i++) {
            if (d == fEnumValues[i]) return;
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
	public static long normalizeDuration(char[] value, int start, int length) throws InvalidDatatypeValueException
	{
		int i=0, j=0, k=0, l=0, m=0;
		int sepindex=0;
		int index=start;
		int lindex=0;
		int endindex=(start+length)-1;
		int pendindex=endindex;

		final char[] dseps = {'Y','M','D'};
		final char[] tseps = {'H','M','S'};
		final char[] msc = {'0','0','0'};

		final int[] buckets = new int[Calendar.FIELD_COUNT];
		for(i=0;i<buckets.length;i++) buckets[i]=0;
		boolean intime=false;
		boolean fixed=false;
		boolean p1negative=false;
		boolean p2negative=false;
		boolean p1specified=false;
		boolean p2specified=false;
		GregorianCalendar cstart = null;
		GregorianCalendar cend = null;

//	Start phase 1: capture start and/or end instant.
		try
		{
			if (value[index]=='-')
			{
				p1negative=true;
			}
//	Look for the forward slash.
			int ix = TimeInstantValidator.indexOf(value, start, '/');

			if (ix > -1 && ix < endindex)
			{
				if (value[ix+1]=='-') 
				{
					p2negative=true;
				}
//  If the first term starts with a 'P', pin it for later parsing 
				if (value[(p1negative?index+1:index)]=='P')
				{
					if(p1negative)index++;
					p1specified=true;
					pendindex=ix-1;
				}
//	Otherwise parse it for a timeInstant
				else
				{
					cstart = (GregorianCalendar)TimeInstantValidator.normalizeInstant(value, index, ix-index);
				}
//  If the second term starts with a 'P', pin it for later parsing 
				if (value[(p2negative?(ix+2):(ix+1))]=='P')
				{
					p2specified=true;
					index=(p2negative?(ix+2):(ix+1));
				}
//	Otherwise parse it for a timeInstant
				else
				{
					ix++;
					cend = (GregorianCalendar)TimeInstantValidator.normalizeInstant(value,ix,(endindex-ix)+1);
				}
			}
//	Only one term specified.
			else
			{
			 	index=(p1negative?(start+1):(start));
			}
//	If both terms are instants, return the millisecond difference
			if(cstart != null && cend != null)
			{
				return((cend.getTime().getTime() - cstart.getTime().getTime()));
			}
// If both terms are 'P', error.
			if (p1specified && p2specified)
				throw new ParseException("Period cannot be expressed as 2 durations.", 0);
	
			if (p1specified && value[index] != 'P')
			{
				throw new ParseException("Invalid start character for timeDuration:"+value[index], index);
			}
			if (p2specified && value[index] != 'P')
			{
				throw new ParseException("Invalid start character for timeDuration:"+value[index], index);
			}
		}
		catch(Exception e)
		{
			throw new InvalidDatatypeValueException(e.toString());
		}
//	Second phase....parse 'P' term
		try
		{

			lindex=index+1;
			for(i=index+1;i<=pendindex;i++)
			{
//	Accumulate digits.
				if (Character.isDigit(value[i]) || value[i]=='.')
				{
					if (value[i]=='.')fixed=true;
					continue;
				}
				if (value[i]=='T')
				{
					intime=true;
					sepindex=0;
					lindex=i+1;
					continue;
				}
//	If you get a separator, it must be appropriate for the section.
				sepindex = TimeInstantValidator.indexOf((intime?tseps:dseps), sepindex, value[i]);
				if (sepindex == -1)
					throw new ParseException("Illegal or misplaced separator.", i);
				sepindex++;
//	Fractional digits are allowed only for seconds.
				if (fixed && value[i]!='S')
					throw new ParseException("Fractional digits allowed only for 'seconds'.", i);

				j=0;
				switch(value[i])
				{
					case('Y'):
					{
						if(intime)throw new ParseException("Year must be specified before 'T' separator.", i);
						buckets[Calendar.YEAR]=TimeInstantValidator.parseInt(value, lindex, i-lindex);
						break;
					}
					case('D'):
					{
						if(intime)throw new ParseException("Days must be specified before 'T' separator.", i);
						buckets[Calendar.DAY_OF_MONTH]=TimeInstantValidator.parseInt(value, lindex, i-lindex);
						break;
					}
					case('H'):
					{
						if(!intime)throw new ParseException("Hours must be specified after 'T' separator.", i);
						buckets[Calendar.HOUR_OF_DAY]=TimeInstantValidator.parseInt(value, lindex, i-lindex);
						break;
					}
					case('M'):
					{
						buckets[(intime?Calendar.MINUTE:Calendar.MONTH)]=TimeInstantValidator.parseInt(value, lindex, i-lindex);
						break;
					}
					case('S'):
					{
						if(!intime)throw new ParseException("Seconds must be specified after 'T' separator.", i);
						if(!fixed)buckets[Calendar.SECOND]=TimeInstantValidator.parseInt(value, lindex, i-lindex);
						else
						{
							int ps = TimeInstantValidator.indexOf(value, lindex, '.');
							buckets[Calendar.SECOND]=TimeInstantValidator.parseInt(value, lindex, ps-lindex);
							ps++;k=0;
							while((ps <= pendindex) && (k<3) && Character.isDigit(value[ps]))
								msc[k++]=value[ps++];
							buckets[Calendar.MILLISECOND]=TimeInstantValidator.parseInt(msc, 0, 3);
							fixed=false;
						}
						break;
					}
					default:
					{
						throw new ParseException("Illegal 'picture' character: "+value[i], i);
					}
				}
			lindex=i+1;
			}
		}
		catch(Exception e)
		{
			throw new InvalidDatatypeValueException(e.toString());
		}
//	Third phase, make the calculations.
		try
		{
//	Roll the start calendar forward and return difference.
			if (cstart !=null)
			{
				long st = cstart.getTime().getTime();
				for(k=0;k<buckets.length;k++)
					if(buckets[k]!=0)cstart.add(k, (p2negative?-buckets[k]:buckets[k]));
				long ms = cstart.getTime().getTime();
				return((ms-st));
			}
//	Roll the end calendar backward and return difference.
			if (cend !=null)
			{
				long st = cend.getTime().getTime();
				for(k=0;k<buckets.length;k++) 
					if(buckets[k]>0) cend.add(k, (p1negative?buckets[k]:-buckets[k]));
				long ms = cend.getTime().getTime();
				return((ms-st));
			}
//	Otherwise roll the relative specification forward and reverse the sing as appropriate.	
			long r=(((long)(( (buckets[Calendar.YEAR]*31104000L)+
									(buckets[Calendar.MONTH]*2592000L)+
									(buckets[Calendar.DAY_OF_MONTH]*86400L)+
									(buckets[Calendar.HOUR_OF_DAY]*3600L)+
									(buckets[Calendar.MINUTE]*60L)+
									(buckets[Calendar.SECOND]))*1000L)+
									(buckets[Calendar.MILLISECOND])));
	
	  		return((p1negative?-r:r));
		}
		catch(Exception e)
		{
			throw new InvalidDatatypeValueException(e.toString());
		}
   }

        public int compare( DatatypeValidator o1, DatatypeValidator o2){
            return 0;
        }
}
