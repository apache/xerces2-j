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
 * RealValidator validates that content satisfies the W3C XML Datatype for Real
 *
 * @author Ted Leung
 * @version
 */

public class DoubleValidator implements DatatypeValidator {
	
	double fMaxInclusive = 0;
	boolean fIsMaxInclusive = false;
	double fMaxExclusive = 0;
	boolean fIsMaxExclusive = false;
	double fMinInclusive = 0;
	boolean fIsMinInclusive = false;
	double fMinExclusive = 0;
	boolean fIsMinExclusive = false;
	double fEnumValues[] = null;
	boolean fHasEnums = false;
	RealValidator fBaseValidator = null;
	private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
	private Locale fLocale = null;
	
	/**
     * validate that a string matches the real datatype
     *
     * validate returns true or false depending on whether the string content is a
     * W3C real type.
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C real type
     */

	public void validate(String content, boolean list) throws InvalidDatatypeValueException {
	    double d = 0;
        try {
            d = Double.valueOf(content).doubleValue();
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException(
				getErrorString(DatatypeMessageProvider.NotReal,
							   DatatypeMessageProvider.MSG_NONE,
							   new Object [] { content }));
        }
        boundsCheck(d);
        if (fHasEnums)
            enumCheck(d);
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
	        double realValue = 0;
	        try {
	            realValue = Double.valueOf(value).doubleValue();
	        } catch (NumberFormatException nfe) {
                facetsAreConsistent = false;
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE) && fIsMinInclusive) {
                facetsAreConsistent = fMinInclusive <= realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) && fIsMinExclusive) {
	            facetsAreConsistent = fMinExclusive < realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) && fIsMaxInclusive) {
	            facetsAreConsistent = fMaxInclusive >= realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) && fIsMaxExclusive) {
	            facetsAreConsistent = fMaxExclusive > realValue;
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
	        double realValue = 0;
	        try {
	            realValue = Double.valueOf(value).doubleValue();
	        } catch (NumberFormatException nfe) {
	            throw new IllegalFacetValueException(
					getErrorString(DatatypeMessageProvider.IllegalFacetValue,
								   DatatypeMessageProvider.MSG_NONE,
								   new Object [] { value, key }));
	        }
	        if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                fIsMinInclusive = true;
	            fMinInclusive = realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
	            fIsMinExclusive = true;
	            fMinExclusive = realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
	            fIsMaxInclusive = true;
	            fMaxInclusive = realValue;
	        } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
	            fIsMaxExclusive = true;
	            fMaxExclusive = realValue;
	        } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
	        } else if (key.equals(SchemaSymbols.ELT_PRECISION) ||
                     key.equals(SchemaSymbols.ELT_SCALE) ||
                     key.equals(SchemaSymbols.ELT_LENGTH) ||
                     key.equals(SchemaSymbols.ELT_MINLENGTH) ||
                     key.equals(SchemaSymbols.ELT_MAXLENGTH) ||
                     key.equals(SchemaSymbols.ELT_PERIOD) ||
                     key.equals(SchemaSymbols.ELT_ENCODING) ||
                     key.equals(SchemaSymbols.ELT_PATTERN) )
                throw new IllegalFacetException(
					getErrorString(DatatypeMessageProvider.IllegalRealFacet,
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
	        fEnumValues = new double[v.size()];
	        for (int i = 0; i < v.size(); i++)
	            try {
	                fEnumValues[i] = Double.valueOf((String) v.elementAt(i)).doubleValue();
	                boundsCheck(fEnumValues[i]);
	            } catch (InvalidDatatypeValueException idve) {
	                throw new IllegalFacetValueException(
						getErrorString(DatatypeMessageProvider.InvalidEnumValue,
									   DatatypeMessageProvider.MSG_NONE,
									   new Object [] { v.elementAt(i) }));
	            } catch (NumberFormatException nfe) {
	                System.out.println("Internal Error parsing enumerated values for real type");
	            }
	    }

	}
	
	public void setFacets(int facets[]) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException {
	}

    public void setBasetype(DatatypeValidator base) {
	    fBaseValidator = (RealValidator) base;
    }

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(double d) throws InvalidDatatypeValueException {
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
							   new Object [] { new Double(d) }));
    }
    
    private void enumCheck(double v) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumValues.length; i++) {
            if (v == fEnumValues[i]) return;
        }
		throw new InvalidDatatypeValueException(
			getErrorString(DatatypeMessageProvider.NotAnEnumValue,
						   DatatypeMessageProvider.MSG_NONE,
						   new Object [] { new Double(v) }));
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

    public int compare( DatatypeValidator o1, DatatypeValidator o2){
        return 0;
    }
}
