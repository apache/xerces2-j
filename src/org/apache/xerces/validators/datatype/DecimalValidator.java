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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import org.apache.xerces.validators.schema.SchemaSymbols;

/**
 *
 * DecimalValidator validates that content satisfies the W3C XML Datatype for decimal
 *
 * @author Ted Leung
 * @version
 */

public class DecimalValidator implements DatatypeValidator {

    BigDecimal fMaxInclusive = null;
    boolean fIsMaxInclusive = false;
    BigDecimal fMaxExclusive = null;
    boolean fIsMaxExclusive = false;
    BigDecimal fMinInclusive = null;
    boolean fIsMinInclusive = false;
    BigDecimal fMinExclusive = null;
    boolean fIsMinExclusive = false;
    BigDecimal fEnumValues[] = null;
    boolean fHasEnums = false;
    int fPrecision = 0;
    boolean fIsPrecision = false;
    int fScale = 0;
    boolean fIsScale = false;
    DecimalValidator fBaseValidator = null;
    private DatatypeMessageProvider fMessageProvider = new DatatypeMessageProvider();
    private Locale fLocale = null;

    /**
     * validate that a string matches the decimal datatype
     *
     * validate returns true or false depending on whether the string content is a
     * W3C decimal type.
     * 
     * @param content A string containing the content to be validated
     *
     * @exception throws InvalidDatatypeException if the content is
     *  is not a W3C decimal type
     */

    public void validate(String content, boolean list) throws InvalidDatatypeValueException {
        BigDecimal d = null;
        try {
            d = new BigDecimal(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException(
                                                   getErrorString(DatatypeMessageProvider.NotDecimal,
                                                                  DatatypeMessageProvider.MSG_NONE,
                                                                  new Object[] { content}));
        }
        if (fIsScale)
            if (d.scale() > fScale)
                throw new InvalidDatatypeValueException(
                                                       getErrorString(DatatypeMessageProvider.ScaleExceeded,
                                                                      DatatypeMessageProvider.MSG_NONE,
                                                                      new Object[] { content}));
        if (fIsPrecision) {
            int precision = d.movePointRight(d.scale()).toString().length() - 
                            ((d.signum() < 0) ? 1 : 0); // account for minus sign
            if (precision > fPrecision)
                throw new InvalidDatatypeValueException(
                                                       getErrorString(DatatypeMessageProvider.PrecisionExceeded,
                                                                      DatatypeMessageProvider.MSG_NONE,
                                                                      new Object[] {content} ));
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
            BigDecimal decimalValue = null;
            try {
                decimalValue = new BigDecimal(value);
            } catch (NumberFormatException nfe) {
                facetsAreConsistent = false;
            }
            if (key.equals(SchemaSymbols.ELT_MININCLUSIVE ) && fIsMinInclusive) {
                facetsAreConsistent = (fMinInclusive.compareTo(decimalValue) < 0);
            } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE) && fIsMinExclusive) {
                facetsAreConsistent = (fMinExclusive.compareTo(decimalValue) < 0);
            } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE) && fIsMaxInclusive) {
                facetsAreConsistent = (fMaxInclusive.compareTo(decimalValue) >= 0);
            } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE) && fIsMaxExclusive) {
                facetsAreConsistent = (fMaxExclusive.compareTo(decimalValue) > 0);
            } else if (key.equals(SchemaSymbols.ELT_SCALE) && fIsScale && fIsPrecision) {
                facetsAreConsistent = fScale <= fPrecision;
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
            BigDecimal decimalValue = null;
            try {
                decimalValue = new BigDecimal(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalFacetValueException(
                                                    getErrorString(DatatypeMessageProvider.IllegalFacetValue,
                                                                   DatatypeMessageProvider.MSG_NONE,
                                                                   new Object [] { value, key}));
            }
            if (key.equals(SchemaSymbols.ELT_MININCLUSIVE)) {
                fIsMinInclusive = true;
                fMinInclusive = decimalValue;
            } else if (key.equals(SchemaSymbols.ELT_MINEXCLUSIVE)) {
                fIsMinExclusive = true;
                fMinExclusive = decimalValue;
            } else if (key.equals(SchemaSymbols.ELT_MAXINCLUSIVE)) {
                fIsMaxInclusive = true;
                fMaxInclusive = decimalValue;
            } else if (key.equals(SchemaSymbols.ELT_MAXEXCLUSIVE)) {
                fIsMaxExclusive = true;
                fMaxExclusive = decimalValue;
            } else if (key.equals(SchemaSymbols.ELT_ENUMERATION)) {
            } else if (key.equals(SchemaSymbols.ELT_PRECISION)) {
                fIsPrecision = true;
                fPrecision = decimalValue.intValue();
            } else if (key.equals(SchemaSymbols.ELT_SCALE)) {
                fIsScale = true;
                fScale = decimalValue.intValue();
            } else if (key.equals(SchemaSymbols.ELT_LENGTH) ||
                       key.equals(SchemaSymbols.ELT_MINLENGTH) ||
                       key.equals(SchemaSymbols.ELT_MAXLENGTH) ||
                       key.equals(SchemaSymbols.ELT_ENCODING) ||
                       key.equals(SchemaSymbols.ELT_PERIOD) ||
                       key.equals(SchemaSymbols.ELT_PATTERN) )
                throw new IllegalFacetException(
                                               getErrorString(DatatypeMessageProvider.IllegalDecimalFacet,
                                                              DatatypeMessageProvider.MSG_NONE,
                                                              null));
            else
                throw new UnknownFacetException(
                                               getErrorString(DatatypeMessageProvider.UnknownFacet,
                                                              DatatypeMessageProvider.MSG_NONE,
                                                              new Object [] { key}));
        }

        // check for scale <= precision
        if (fIsScale && fIsPrecision && fScale > fPrecision)
            throw new IllegalFacetException(
                                           getErrorString(DatatypeMessageProvider.ScaleLargerThanPrecision,
                                                          DatatypeMessageProvider.MSG_NONE,
                                                          null));

        // check the enum values after any range constraints are in place
        Vector v = (Vector) facets.get(SchemaSymbols.ELT_ENUMERATION);    
        if (v != null) {
            fHasEnums = true;
            fEnumValues = new BigDecimal[v.size()];
            for (int i = 0; i < v.size(); i++)
                try {
                    fEnumValues[i] = new BigDecimal((String) v.elementAt(i));
                    boundsCheck(fEnumValues[i]);
                } catch (InvalidDatatypeValueException idve) {
                    throw new IllegalFacetValueException(
                                                        getErrorString(DatatypeMessageProvider.InvalidEnumValue,
                                                                       DatatypeMessageProvider.MSG_NONE,
                                                                       new Object [] { v.elementAt(i)}));
                } catch (NumberFormatException nfe) {
                    System.out.println("Internal Error parsing enumerated values for decimal type");
                }
        }

    }

    public void setFacets(int facets[]) throws UnknownFacetException, IllegalFacetException, IllegalFacetValueException, ConstrainException {
    }

    public void setBasetype(String base) {
        //fBaseValidator = (DecimalValidator) base;
    }

    /*
     * check that a facet is in range, assumes that facets are compatible -- compatibility ensured by setFacets
     */
    private void boundsCheck(BigDecimal d) throws InvalidDatatypeValueException {
        boolean minOk = false;
        boolean maxOk = false;
        if (fIsMaxInclusive)
            maxOk = (d.compareTo(fMaxInclusive) <= 0);
        else if (fIsMaxExclusive)
            maxOk = (d.compareTo(fMaxExclusive) < 0);
        else
            maxOk = (!fIsMaxInclusive && !fIsMaxExclusive);

        if (fIsMinInclusive)
            minOk = (d.compareTo(fMinInclusive) >= 0);
        else if (fIsMinExclusive)
            minOk = (d.compareTo(fMinInclusive) > 0);
        else
            minOk = (!fIsMinInclusive && !fIsMinExclusive);
        if (!(minOk && maxOk))
            throw new InvalidDatatypeValueException(
                                                   getErrorString(DatatypeMessageProvider.OutOfBounds,
                                                                  DatatypeMessageProvider.MSG_NONE,
                                                                  new Object [] { d}));
    }

    private void enumCheck(BigDecimal v) throws InvalidDatatypeValueException {
        for (int i = 0; i < fEnumValues.length; i++) {
            if (v == fEnumValues[i]) return;
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

    public int compare( DatatypeValidator o1, DatatypeValidator o2){
        return 0;
    }
}
