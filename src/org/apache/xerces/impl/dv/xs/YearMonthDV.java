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
 * Validator for &lt;gYearMonth&gt; datatype (W3C Schema Datatypes)
 *
 * @xerces.internal 
 *
 * @author Elena Litani
 * @author Gopal Sharma, SUN Microsystem Inc.
 *
 * @version $Id$
 */
public class YearMonthDV extends AbstractDateTimeDV{

    /**
     * Convert a string to a compiled form
     *
     * @param  content The lexical representation of gYearMonth
     * @return a valid and normalized gYearMonth object
     */
    public Object getActualValue(String content, ValidationContext context) throws InvalidDatatypeValueException{
        try{
            return parse(content);
        } catch(Exception ex){
            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{content, "gYearMonth"});
        }
    }

    /**
     * Parses, validates and computes normalized version of gYearMonth object
     *
     * @param str    The lexical representation of gYearMonth object CCYY-MM
     *               with possible time zone Z or (-),(+)hh:mm
     * @return normalized date representation
     * @exception SchemaDateTimeException Invalid lexical representation
     */
    protected DateTimeData parse(String str) throws SchemaDateTimeException{
        DateTimeData date = new DateTimeData(this);
        int len = str.length();

        // get date
        int end = getYearMonth(str, 0, len, date);
        date.day = DAY;
        parseTimeZone (str, end, len, date);

        //validate and normalize

        validateDateTime(date);

        if ( date.utc!=0 && date.utc!='Z' ) {
            normalize(date);
        }
        return date;
    }
    
    /**
     * Given normalized values, determines order-relation
     * between give date/time objects.
     *
     * @param date1  date/time object
     * @param date2  date/time object
     * @return 0 if date1 and date2 are equal, a value less than 0 if date1 is less than date2, a value greater than 0 if date1 is greater than date2
     */
    protected short compareOrder(DateTimeData date1, DateTimeData date2) {
        if (date1.year < date2.year)
            return -1;
        if (date1.year > date2.year)
            return 1;
        if (date1.month < date2.month)
            return -1;
        if (date1.month > date2.month)
            return 1;
        if (date1.utc < date2.utc)
            return -1;
        if (date1.utc > date2.utc)
            return 1;
        return 0;
    }

    protected String dateToString(DateTimeData date) {
        StringBuffer message = new StringBuffer(25);
        append(message, date.year, 4);
        message.append('-');
        append(message, date.month, 2);
        append(message, (char)date.utc, 0);
        return message.toString();
    }

}


