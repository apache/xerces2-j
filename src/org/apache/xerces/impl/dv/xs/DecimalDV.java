/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001, 2002 The Apache Software Foundation.  All rights
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

package org.apache.xerces.impl.dv.xs;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Represent the schema type "decimal"
 *
 * @author Neeraj Bajaj, Sun Microsystems, inc.
 * @author Sandy Gao, IBM
 *
 * @version $Id$
 */
public class DecimalDV extends TypeValidator {

    public short getAllowedFacets(){
        return ( XSSimpleTypeDecl.FACET_PATTERN | XSSimpleTypeDecl.FACET_WHITESPACE | XSSimpleTypeDecl.FACET_ENUMERATION |XSSimpleTypeDecl.FACET_MAXINCLUSIVE |XSSimpleTypeDecl.FACET_MININCLUSIVE | XSSimpleTypeDecl.FACET_MAXEXCLUSIVE  | XSSimpleTypeDecl.FACET_MINEXCLUSIVE | XSSimpleTypeDecl.FACET_TOTALDIGITS | XSSimpleTypeDecl.FACET_FRACTIONDIGITS);
    }

    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException {
        try {
            return new MyDecimal(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "decimal"});
        }
    }

    public boolean isEqual(Object value1, Object value2) {
        if (!(value1 instanceof MyDecimal) || !(value2 instanceof MyDecimal))
            return false;
        return ((MyDecimal)value1).equals((MyDecimal)value2);
    }

    public int compare(Object value1, Object value2){
        return ((MyDecimal)value1).compareTo((MyDecimal)value2);
    }

    public int getTotalDigits(Object value){
        return ((MyDecimal)value).totalDigits;
    }

    public int getFractionDigits(Object value){
        return ((MyDecimal)value).fracDigits;
    }
    
} // class DecimalDV

// Avoid using the heavy-weight java.math.BigDecimal
class MyDecimal {
    // sign: 0 for vlaue 0; 1 for positive values; -1 for negative values
    int sign = 1;
    // total digits. >= 1
    int totalDigits = 0;
    // integer digits when sign != 0
    int intDigits = 0;
    // fraction digits when sign != 0
    int fracDigits = 0;
    // the string representing the integer part
    String ivalue = "";
    // the string representing the fraction part
    String fvalue = "";
    
    MyDecimal(String content) throws NumberFormatException {
        if (content.equals("0")) {
            int i = 0;
        }
        int len = content.length();
        if (len == 0)
            throw new NumberFormatException();

        // these 4 variables are used to indicate where the integre/fraction
        // parts start/end.
        int intStart = 0, intEnd = 0, fracStart = 0, fracEnd = 0;
        
        // Deal with leading sign symbol if present
        if (content.charAt(0) == '+') {
            // skip '+', so intStart should be 1
            intStart = 1;
        }
        else if (content.charAt(0) == '-') {
            // keep '-', so intStart is stil 0
            intStart = 1;
            sign = -1;
        }

        // skip leading zeroes in integer part
        int actualIntStart = intStart;
        while (actualIntStart < len && content.charAt(actualIntStart) == '0') {
            actualIntStart++;
        }

        // Find the ending position of the integer part
        for (intEnd = actualIntStart;
             intEnd < len && TypeValidator.isDigit(content.charAt(intEnd));
             intEnd++);

        // Not reached the end yet
        if (intEnd < len) {
            // the remaining part is not ".DDD", error
            if (content.charAt(intEnd) != '.')
                throw new NumberFormatException();

            // fraction part starts after '.', and ends at the end of the input
            fracStart = intEnd + 1;
            fracEnd = len;
        }

        // no integer part, no fraction part, error.
        if (intStart == intEnd && fracStart == fracEnd)
            throw new NumberFormatException();

        // ignore trailing zeroes in fraction part
        while (fracEnd > fracStart && content.charAt(fracEnd-1) == '0') {
            fracEnd--;
        }

        // check whether there is non-digit characters in the fraction part
        for (int fracPos = fracStart; fracPos < fracEnd; fracPos++) {
            if (!TypeValidator.isDigit(content.charAt(fracPos)))
                throw new NumberFormatException();
        }

        intDigits = intEnd - actualIntStart;
        fracDigits = fracEnd - fracStart;
        totalDigits = (intDigits == 0 ? 1 : intDigits) + fracDigits;

        if (intDigits > 0) {
            ivalue = content.substring(actualIntStart, intEnd);
            if (fracDigits > 0)
                fvalue = content.substring(fracStart, fracEnd);
        }
        else {
            if (fracDigits > 0) {
                fvalue = content.substring(fracStart, fracEnd);
            }
            else {
                // ".00", treat it as "0"
                sign = 0;
            }
        }
    }
    public boolean equals(MyDecimal val) {
        if (val == null)
            return false;
        if (val == this)
            return true;
        
        if (sign != val.sign)
           return false;
        if (sign == 0)
            return true;
        
        return intDigits == val.intDigits && fracDigits == val.fracDigits &&
               ivalue.equals(val.ivalue) && fvalue.equals(val.fvalue);
    }
    public int compareTo(MyDecimal val) {
        if (sign != val.sign)
            return sign > val.sign ? 1 : -1;
        if (sign == 0)
            return 0;
        return sign * intComp(val);
    }
    private int intComp(MyDecimal val) {
        if (intDigits != val.intDigits)
            return intDigits > val.intDigits ? 1 : -1;
        int ret = ivalue.compareTo(val.ivalue);
        if (ret != 0)
            return ret > 0 ? 1 : -1;;
        ret = fvalue.compareTo(val.fvalue);
        return ret == 0 ? 0 : (ret > 0 ? 1 : -1);
    }
    public String toString() {
        if (sign == 0)
            return "0";
        StringBuffer buffer = new StringBuffer(totalDigits+2);
        if (sign == -1)
            buffer.append('-');
        if (intDigits != 0)
            buffer.append(ivalue);
        else
            buffer.append('0');
        if (fracDigits != 0) {
            buffer.append('.');
            buffer.append(fvalue);
        }
        return buffer.toString();
    }
}
