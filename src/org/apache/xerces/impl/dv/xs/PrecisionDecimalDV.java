/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.xerces.impl.dv.xs;

import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidationContext;

/**
 * Validator for <precisionDecimal> datatype (W3C Schema 1.1)
 * 
 * @xerces.experimental
 * 
 * @author Ankit Pasricha, IBM
 * 
 * @version $Id$
 */
class PrecisionDecimalDV extends TypeValidator {

    private static class XPrecisionDecimal {

        // sign: 0 for absent; 1 for positive values; -1 for negative values (except in case of INF, -INF)
        int sign = 1;
        // total digits. >= 1
        int totalDigits = 0;
        // integer digits when sign != 0
        int intDigits = 0;
        // fraction digits when sign != 0
        int fracDigits = 0;
        //precision
        int precision = 0;
        // the string representing the integer part
        String ivalue = "";
        // the string representing the fraction part
        String fvalue = "";
        
        int pvalue = 0;

        XPrecisionDecimal(String content) throws NumberFormatException {
            if(content.equals("NaN")) {
                ivalue = "NaN";
                sign = 0;
                return;
            }
            if(content.equals("+INF") || content.equals("INF")) {
                ivalue = "INF";
                return;
            }
            if(content.equals("-INF")) {
                ivalue = "-INF";
                return;
            }
            initD(content);
        }

        void initD(String content) throws NumberFormatException {
            int len = content.length();
            if (len == 0) {
                throw new NumberFormatException();
            }

            // these 4 variables are used to indicate where the integre/fraction
            // parts start/end.
            int intStart = 0, intEnd = 0, fracStart = 0, fracEnd = 0;

            // Deal with leading sign symbol if present
            if (content.charAt(0) == '+') {
                // skip '+', so intStart should be 1
                intStart = 1;
            }
            else if (content.charAt(0) == '-') {
                intStart = 1;
                sign = -1;
            }

            // skip leading zeroes in integer part
            int actualIntStart = intStart;
            while (actualIntStart < len && content.charAt(actualIntStart) == '0') {
                actualIntStart++;
            }

            // Find the ending position of the integer part
            for (intEnd = actualIntStart; intEnd < len && TypeValidator.isDigit(content.charAt(intEnd)); intEnd++);

            // Not reached the end yet
            if (intEnd < len) {
                // the remaining part is not ".DDD" or "EDDD" or "eDDD", error
                if (content.charAt(intEnd) != '.' && content.charAt(intEnd) != 'E' && content.charAt(intEnd) != 'e') {
                    throw new NumberFormatException();
                }

                if (content.charAt(intEnd) == '.') {
                    // fraction part starts after '.', and ends at the end of the input
                    fracStart = intEnd + 1;

                    // find location of E or e (if present)
                    // Find the ending position of the fracion part
                    for (fracEnd = fracStart;
                         fracEnd < len && TypeValidator.isDigit(content.charAt(fracEnd));
                         fracEnd++);

                    fracDigits = fracEnd - fracStart;
                    if (fracDigits > 0) {
                        fvalue = content.substring(fracStart, fracEnd);
                    }
                    if (fracEnd < len) {
                        if (content.charAt(fracEnd) != 'E' && content.charAt(fracEnd) != 'e') {
                            throw new NumberFormatException();
                        }
                        if (content.charAt(fracEnd + 1) == '+') {
                            fracEnd++;
                        }
                        pvalue = Integer.parseInt(content.substring(fracEnd + 1, len));
                    }
                }
                else {
                    final int increment = (content.charAt(intEnd + 1) == '+') ? 1 :0;
                    pvalue = Integer.parseInt(content.substring(intEnd + 1 + increment, len));
                }
            }

            // no integer part, no fraction part, error.
            if (intStart == intEnd && fracStart == fracEnd) {
                throw new NumberFormatException();
            }

            intDigits = intEnd - actualIntStart;
            if (intDigits > 0) {
                ivalue = content.substring(actualIntStart, intEnd);
                totalDigits = intDigits + fracDigits;
            }
            else {
                totalDigits = fracDigits;
                for (int i = 0; i < fracDigits; i++,totalDigits--) {
                    if (fvalue.charAt(i) != '0') {
                        break;
                    }
                }
                if (totalDigits == 0) {
                    totalDigits = 1;
                }
            }

            precision = fracDigits - pvalue;
        }

        public boolean equals(Object val) {
            if (val == this) {
                return true;
            }

            if (!(val instanceof XPrecisionDecimal)) {
                return false;
            }

            final XPrecisionDecimal oval = (XPrecisionDecimal)val;
            if (sign == 0 && oval.sign == 0) {
                // Both are NaN. Treat as "equal".
                return true;
            }
            return this.compareTo(oval) == EQUAL;
        }

        /**
         * @return
         */
        private int compareFractionalPart(XPrecisionDecimal oval) {
            if (fvalue.equals(oval.fvalue)) {
                return EQUAL;
            }

            StringBuffer temp1 = new StringBuffer(fvalue);
            StringBuffer temp2 = new StringBuffer(oval.fvalue);

            truncateTrailingZeros(temp1, temp2);
            return temp1.toString().compareTo(temp2.toString());
        }

        private void truncateTrailingZeros(StringBuffer fValue, StringBuffer otherFValue) {
            for (int i = fValue.length() - 1;i >= 0; i--) {
                if (fValue.charAt(i) == '0') {
                    fValue.deleteCharAt(i);
                }
                else {
                    break;
                }
            }

            for (int i = otherFValue.length() - 1;i >= 0; i--) {
                if(otherFValue.charAt(i) == '0') {
                    otherFValue.deleteCharAt(i);
                }
                else {
                    break;
                }
            }
        }

        public int compareTo(XPrecisionDecimal val) {
            // seen NaN
            if (sign == 0 || val.sign == 0) {
                return INDETERMINATE;
            }

            //INF is greater than everything and equal to itself
            if (ivalue.equals("INF") || val.ivalue.equals("INF")) {
                if (ivalue.equals(val.ivalue)) {
                    return EQUAL;
                }
                else if (ivalue.equals("INF")) {
                    return GREATER_THAN;
                }
                return LESS_THAN;
            }

            //-INF is smaller than everything and equal itself
            if (ivalue.equals("-INF") || val.ivalue.equals("-INF")) {
                if (ivalue.equals(val.ivalue)) {
                    return EQUAL;
                }
                else if (ivalue.equals("-INF")) {
                    return LESS_THAN;
                }
                return GREATER_THAN;
            }

            if (sign != val.sign) {
                // Return equal if both are 0
                if (isZero() && val.isZero()) {
                    return EQUAL;
                }
                return sign > val.sign ? GREATER_THAN : LESS_THAN;
            }

            return sign * compare(val);
        }
        private boolean isZero() {
            // Either "000" or "0.00"
            return totalDigits == 1 && intDigits == 0 &&
                   (fracDigits == 0 || fvalue.charAt(fracDigits-1) == '0');
        }

        // To enable comparison - the exponent part of the decimal will be limited
        // to the max value of int.
        private int compare(XPrecisionDecimal val) {
            if (pvalue == val.pvalue) {
                return intComp(val);
            }
            else if (pvalue > val.pvalue) {
               int expDiff = pvalue - val.pvalue;
               StringBuffer buffer = new StringBuffer(ivalue);
               StringBuffer fbuffer = new StringBuffer(fvalue);
               for(int i = 0;i < expDiff; i++) {
                    if (i < fracDigits) {
                        buffer.append(fvalue.charAt(i));
                        fbuffer.deleteCharAt(0);
                    }
                    else  {
                        buffer.append('0');
                    }
                }
                // remove leading zeroes in integer part
                while (buffer.length() > 0 && buffer.charAt(0) == '0') buffer.deleteCharAt(0);
                return compareDecimal(buffer.toString(), fbuffer.toString(), val.ivalue, val.fvalue);
            }
            else {
                int expDiff = val.pvalue - pvalue;
                StringBuffer buffer = new StringBuffer(val.ivalue);
                StringBuffer fbuffer = new StringBuffer(val.fvalue);
                for(int i = 0;i < expDiff; i++) {
                    if (i < val.fracDigits) {
                        buffer.append(val.fvalue.charAt(i));
                        fbuffer.deleteCharAt(0);
                    }
                    else  {
                        buffer.append('0');
                    }
                }
                // remove leading zeroes in integer part
                while (buffer.length() > 0 && buffer.charAt(0) == '0') buffer.deleteCharAt(0);
                return compareDecimal(ivalue, fvalue, buffer.toString(), fbuffer.toString());
            }            
        }

        /**
         * @param val
         * @return
         */
        private int intComp(XPrecisionDecimal val) {
            if (intDigits != val.intDigits) {
                return intDigits > val.intDigits ? GREATER_THAN : LESS_THAN;
            }

            return compareDecimal(ivalue, fvalue, val.ivalue, val.fvalue);
        }

        /**
         * @param val
         * @return
         */
        private int compareDecimal(String iValue, String fValue, String otherIValue, String otherFValue) {            
            if (iValue.length() != otherIValue.length()) {
                return iValue.length() > otherIValue.length() ? GREATER_THAN : LESS_THAN;
            }

            int ret = iValue.compareTo(otherIValue);
            if (ret != 0) {
                return ret > 0 ? GREATER_THAN : LESS_THAN;
            }
            
            if (fValue.equals(otherFValue)) {
                return EQUAL;
            }

            final StringBuffer temp1 = new StringBuffer(fValue);
            final StringBuffer temp2 = new StringBuffer(otherFValue);
            truncateTrailingZeros(temp1, temp2);
            ret = temp1.toString().compareTo(temp2.toString());
            return ret == 0 ? EQUAL : (ret > 0 ? GREATER_THAN : LESS_THAN);
        }

        private String canonical;
        public synchronized String toString() {
            if (canonical == null) {
                makeCanonical();
            }
            return canonical;
        }

        private void makeCanonical() {
            // REVISIT

            // A workaround for now
            if (ivalue.equals("INF") || ivalue.equals("-INF") || ivalue.equals("NaN")) {
                canonical = ivalue;
            }
            else {
                final StringBuffer tempBuf = new StringBuffer();
                if (intDigits > 0) {
                    tempBuf.append(ivalue);
                }
                if (fracDigits > 0) {
                    tempBuf.append('.');
                    tempBuf.append(fvalue);
                }
                if (pvalue != 0) {
                    tempBuf.append('E');
                    tempBuf.append(pvalue);
                }
                canonical = tempBuf.toString();
            }
        }

        /**
         * @param decimal
         * @return
         */
        public boolean isIdentical(XPrecisionDecimal decimal) {
            if (ivalue.equals(decimal.ivalue) && (ivalue.equals("INF") || ivalue.equals("-INF") || ivalue.equals("NaN"))) {
                return true;
            }
            
            if (sign == decimal.sign && intDigits == decimal.intDigits && fracDigits == decimal.fracDigits && pvalue == decimal.pvalue 
                    && ivalue.equals(decimal.ivalue) && fvalue.equals(decimal.fvalue)) {
                return true;
            }
            return false;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.impl.dv.xs.TypeValidator#getActualValue(java.lang.String, org.apache.xerces.impl.dv.ValidationContext)
     */
    public Object getActualValue(String content, ValidationContext context)
    throws InvalidDatatypeValueException {
        try {
            return new XPrecisionDecimal(content);
        } catch (NumberFormatException nfe) {
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "precisionDecimal"});
        }
    }

    public int compare(Object value1, Object value2) {
        return ((XPrecisionDecimal)value1).compareTo((XPrecisionDecimal)value2);
    }

    public int getTotalDigits(Object value) {
        return ((XPrecisionDecimal)value).totalDigits;
    }

    public boolean isIdentical(Object value1, Object value2) {
        if (!(value2 instanceof XPrecisionDecimal) || !(value1 instanceof XPrecisionDecimal)) {
            return false;
        }
        return ((XPrecisionDecimal)value1).isIdentical((XPrecisionDecimal)value2);	
    }

    public int getPrecision(Object value){
        return ((XPrecisionDecimal)value).precision;
    }

    public boolean hasPrecision(Object value){
        XPrecisionDecimal pd = (XPrecisionDecimal)value;
        // Can't be NaN (sign==0) or +-INF. != is OK. See initD().
        return pd.sign != 0 && pd.ivalue != "INF" && pd.ivalue != "-INF";
    }
}
