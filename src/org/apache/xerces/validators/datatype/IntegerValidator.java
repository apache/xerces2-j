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
 * IntegerValidator validates that content satisfies the W3C XML Datatype for Integer
 *
 * @author Ted Leung
 * @version
 */

public class IntegerValidator implements DatatypeValidator {
	
	int fMaxInclusive = 0;
	boolean fIsMaxInclusive = false;
	int fMaxExclusive = 0;
	boolean fIsMaxExclusive = false;
	int fMinInclusive = 0;
	boolean fIsMinInclusive = false;
	int fMinExclusive = 0;
	boolean fIsMinExclusive = false;
	int fEnumValues[] = null;
	boolean fHasEnums = false;
	IntegerValidator fBaseValidator = null;
	private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
	private Locale fLocale = null;
	
	/**
     * validate that a string matches the integer datatype
     *
     * validate returns true or false depending on whether the string content is a
     * W3C integer type.
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C integer type
     */

	public void validate(String content) throws InvalidDatatypeValueException {
	    int i = 0;
        try {
            i = Integer.parseInt(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException(
				getErrorString(DatatypeMessageProvider.NotInteger,
							   DatatypeMessageProvider.MSG_NONE,
							   new Object [] { content }));
        }
        boundsCheck(i);
        if (fHasEnums)
            enumCheck(i);
	}
			
	public void validate(int contentIndex) throws InvalidDatatypeValueException {
	}
	
	//REVISIT: candidate for public API
	boolean ensureFacetsAreConsistent(Hashtable facets) {
	    boolean facetsAreConsistent = true;
	    for (Enumeration e = facets.keys(); facetsAreConsistent && e.hasMoreElements();) {
	        String key = (String) e.nextElement();
	        String value = null;
	        if (key.equals(SchemaSymbols.ELT_ENUMERATION)) 
                continue;  // ENUM values passed as a vector & handled after bounds facets	    
    	    value = (String) facets.get(key);   
	        int integerValue = 0;
	        try {
	            integerValue = Integer.parseInt(value);
	        } catch (NumberFormatException nfe) {
                facetsAreConsistent = false;
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE) && fIsMinInclusive) {
                facetsAreConsistent = fMinInclusive <= integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) && fIsMinExclusive) {
	            facetsAreConsistent = fMinExclusive < integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) && fIsMaxInclusive) {
	            facetsAreConsistent = fMaxInclusive >= integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) && fIsMaxExclusive) {
	            facetsAreConsistent = fMaxExclusive > integerValue;
	        }
	    }
	    return facetsAreConsistent;
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
	        int integerValue = 0;
	        try {
	            integerValue = Integer.parseInt(value);
	        } catch (NumberFormatException nfe) {
	            throw new IllegalFacetValueException(
					getErrorString(DatatypeMessageProvider.IllegalFacetValue,
								   DatatypeMessageProvider.MSG_NONE,
								   new Object [] { value, key }));
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                fIsMinInclusive = true;
	            fMinInclusive = integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
	            fIsMinExclusive = true;
	            fMinExclusive = integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
	            fIsMaxInclusive = true;
	            fMaxInclusive = integerValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
	            fIsMaxExclusive = true;
	            fMaxExclusive = integerValue;
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
	        fEnumValues = new int[v.size()];
	        for (int i = 0; i < v.size(); i++)
	            try {
	                fEnumValues[i] = Integer.parseInt((String) v.elementAt(i));
	                boundsCheck(fEnumValues[i]);
	            } catch (InvalidDatatypeValueException idve) {
	                throw new IllegalFacetValueException(
						getErrorString(DatatypeMessageProvider.InvalidEnumValue,
									   DatatypeMessageProvider.MSG_NONE,
									   new Object [] { v.elementAt(i)}));
	            } catch (NumberFormatException nfe) {
	                System.out.println("Internal Error parsing enumerated values for integer type");
	            }
	    }

	}
	
	public void setFacets(int facets[]) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
	}

    public void setBasetype(DatatypeValidator base) {
	    fBaseValidator = (IntegerValidator) base;
    }

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(int i) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;
        if (fIsMaxInclusive)
            maxOk = (i <= fMaxInclusive);
        else if (fIsMaxExclusive)
            maxOk = (i < fMaxExclusive);
        else 
            maxOk = (!fIsMaxInclusive && !fIsMaxExclusive);
        
        if (fIsMinInclusive)
            minOk = (i >= fMinInclusive);
        else if (fIsMinExclusive) 
            minOk = (i > fMinInclusive);
        else 
            minOk = (!fIsMinInclusive && !fIsMinExclusive);
        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException(
				getErrorString(DatatypeMessageProvider.OutOfBounds,
							   DatatypeMessageProvider.MSG_NONE,
							   new Object [] { new Integer(i) }));
    }
    
    private void enumCheck(int v) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumValues.length; i++) {
            if (v == fEnumValues[i]) return;
        }
        throw new InvalidDatatypeValueException(
			getErrorString(DatatypeMessageProvider.NotAnEnumValue,
						   DatatypeMessageProvider.MSG_NONE,
						   new Object [] { new Integer(v) }));
    }

    /**
     * set the locate to be used for error messages
     */
    public void setLocale(Locale locale) {
        fLocale = locale;
    }
    
    private String getErrorString(int major, int minor, Object args[]) {
         try {
             return fMessageProvider.createMessage(fLocale, major, minor, args);
         } catch (Exception e) {
             return "Illegal Errorcode "+minor;
         }
    }
    
    /*
    public static void main(String args[]) {
        // simple unit test
        try {
            DatatypeValidator v = new IntegerValidator();
            Hashtable facets = new Hashtable();
            facets.put("minInclusive","0");
            DatatypeValidator nonneg = new IntegerValidator();
            nonneg.setBasetype(v);
            nonneg.setFacets(facets);
            facets = new Hashtable();
            facets.put("minInclusive","-1");
            DatatypeValidator bad = new IntegerValidator();
            bad.setBasetype(nonneg);
            bad.setFacets(facets);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public int compare( DatatypeValidator o1, DatatypeValidator o2){
        return 0;
    }
}
