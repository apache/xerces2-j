/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.v2.new_datatypes;

//internal imports
import org.apache.xerces.impl.v2.datatypes.InvalidDatatypeValueException;
import org.apache.xerces.impl.v2.datatypes.DatatypeMessageProvider;

//java imports
import java.math.BigDecimal;

/**
 * @version $Id$
 */
public class DecimalDV extends AbstractNumericDV{
    // for most DV classes, this is the same as the DV_?? value defined
    // in XSSimpleTypeDecl that's corresponding to that class. But for
    // ID/IDREF/ENTITY, the privitivaDV is DV_STRING.
    public short getPrimitiveDV(){
	      return XSSimpleTypeDecl.DV_DECIMAL;
    }

    /**
    * return the facets allowed by Decimal
    */
    public short getAllowedFacets(){
	      return (short)(super.getAllowedFacets() |XSSimpleTypeDecl.DEFINED_TOTALDIGITS | XSSimpleTypeDecl.DEFINED_FRACTIONDIGITS);
    }//getAllowedFacets()


    // convert a string to a compiled form. for example,
    // for number types (decimal, double, float, and types derived from them),
    // get the BigDecimal, Double, Flout object.
    // for some types (string and derived), they just return the string itself
    public Object getCompiledValue(String content) throws InvalidDatatypeValueException{

	  BigDecimal bigDecimal = null; // Is content a Decimal
        try {
            bigDecimal = new BigDecimal( stripPlusIfPresent( content));
        }
        catch (Exception nfe) {
            String msg = getErrorString(
                                       DatatypeMessageProvider.fgMessageKeys[DatatypeMessageProvider.NOT_DECIMAL],
                                       new Object[] { "'" + content +"'"});
            throw new InvalidDatatypeValueException(msg);
        }

    	return bigDecimal;
    } //getCompiledValue()


    // the parameters are in compiled form (from getCompiledValue)
    public boolean isEqual(Object value1, Object value2){
        if(value1 instanceof BigDecimal && value2 instanceof BigDecimal)
            return value1.equals(value2);
        else //REVISIT: to be taken care in XSSimpleTypeDecl.
            return false;
    }

    // the following methods might not be supported by every DV.
    // but XSSimpleTypeDecl should know which type supports which methods,
    // and it's an *internal* error if a method is called on a DV that
    // doesn't support it.



    // the  parameters are in compiled form (from getCompiledValue)
    public int compare(Object value1, Object value2){
        if(value1 instanceof BigDecimal && value2 instanceof BigDecimal)
	          return ((BigDecimal)value1).compareTo((BigDecimal)value2);
        else
            return -1;
    }//compare()


    // the parameters are in compiled form (from getCompiledValue)
    public int getTotalDigits(Object value){
        if(value instanceof BigDecimal)
	          return ((BigDecimal)value).movePointRight(((BigDecimal)value).scale()).toString().length() -
                             ((((BigDecimal)value).signum() < 0) ? 1 : 0); // account for minus sign
        else
            return -1;
    }

    // the parameters are in compiled form (from getCompiledValue)
    public int getFractionDigits(Object value){
	      if(value instanceof BigDecimal)
            return ((BigDecimal)value).scale();
        else
            return -1;
    }//getFractionDigits()

     /**
     * This class deals with a bug in BigDecimal class
     * present up to version 1.1.2. 1.1.3 knows how
     * to deal with the + sign.
     *
     * This method strips the first '+' if it found
     * alone such as.
     * +33434.344
     *
     * If we find +- then nothing happens we just
     * return the string passed
     *
     * @param value
     * @return
     */
    static private String stripPlusIfPresent( String value ) {
        String strippedPlus = value;

        if (value.length() >= 2 && value.charAt(0) == '+' && value.charAt(1) != '-') {
            strippedPlus = value.substring(1);
        }
        return strippedPlus;
    }//getStripPlusIfPresent()

} // class DecimalDV
