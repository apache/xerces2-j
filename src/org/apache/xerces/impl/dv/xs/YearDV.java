/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * Validator for <gYear> datatype (W3C Schema Datatypes)
 *
 * @author Elena Litani
 * @author Gopal Sharma, SUN Microsystem Inc.
 *
 * @version $Id$
 */

public class YearDV extends AbstractDateTimeDV {

    /**
     * Convert a string to a compiled form
     *
     * @param  content The lexical representation of time
     * @return a valid and normalized time object
     */
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return new DateTimeData(parse(content), this);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gYear"});
        }
    }

    /**
     * Parses, validates and computes normalized version of gYear object
     *
     * @param str    The lexical representation of year object CCYY
     *               with possible time zone Z or (-),(+)hh:mm
     * @param date   uninitialized date object
     * @return normalized date representation
     * @exception SchemaDateTimeException Invalid lexical representation
     */
    protected int[] parse(String str) throws SchemaDateTimeException{
        int len = str.length();
        int[] date = new int[TOTAL_SIZE];
        int[] timeZone = new int[2];

        // check for preceding '-' sign
        int start = 0;
        if (str.charAt(0)=='-') {
            start = 1;
        }
        int sign = findUTCSign(str, start, len);
        if (sign == -1) {
            date[CY]=parseIntYear(str, len);
        }
        else {
            date[CY]=parseIntYear(str, sign);
            getTimeZone (str, date, sign, len, timeZone);
        }

        //initialize values
        date[M]=MONTH;
        date[D]=1;

        //validate and normalize
        validateDateTime(date, timeZone);

        if ( date[utc]!=0 && date[utc]!='Z' ) {
            normalize(date, timeZone);
        }
        return date;
    }

    /**
     * Converts year object representation to String
     *
     * @param date   year object
     * @return lexical representation of month: CCYY with optional time zone sign
     */
    protected String dateToString(int[] date) {
        StringBuffer message = new StringBuffer(5);
        append(message, date[CY], 4);
        append(message, (char)date[utc], 0);
        return message.toString();
    }

}


