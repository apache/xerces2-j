/*
 * Copyright 2004, 2005 The Apache Software Foundation.
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
package org.apache.xerces.xs.datatypes;

/**
 * <p><b>EXPERIMENTAL: This interface should not be considered stable.
 * It is likely it may be altered or replaced in the future.</b></p>
 * 
 * <p>Interface to expose the values for all date-time related types.</p>
 * 
 * @author Ankit Pasricha, IBM
 * 
 * @version $Id$
 */
public interface XSDateTime {
	
    /**
     * @return years – can be negative for datetime;
     *          returns 0 for duration types
     */
    public int getYears();
    
    /**
     * @return months – can be negative only for durations;
     *                  For duration types, it returns years*12 + months
     */
    public int getMonths();
    
    /**
     * @return days – cannot be negative;
     *          returns 0 for duration types 
     */
    public int getDays();
    
    /**
     * @return hours – cannot be negative;
     *          returns 0 for duration types
     */
    public int getHours();
    
    /**
     * @return minutes – cannot be negative;
     *          returns 0 for duration types
     */
    public int getMinutes();
    
    /**
     * @return seconds – can be negative only for durations;
     *                   For duration types, it returns days*24*3600 + hours*3600 
     *                                                  + minutes*60 + seconds 
     */
    public double getSeconds();
    
    /**
     * @return boolean (true when timezone exists)
     */
    public boolean hasTimeZone();
    
    /**
     * @return timezone hours (for GMT-xx:xx this will be negative),
     *                          not valid for duration types
     */
    public int getTimeZoneHours();
    
    /**
     * @return timezone minutes (for GMT-xx:xx this will be negative),
     *                          not valid for duration types
     */
    public int getTimeZoneMinutes();
    
    /**
     * @return the original lexical value
     */
    public String getLexicalValue();
    
    /**
     * @return a new datetime object with normalized values
     *         (has no effect on durations or objects already
     *          normalized)
     */
    public XSDateTime normalize();
    
    /**
     * @return whether a datetime object is normalized or not
     *         (value is not useful for durations)
     */
    public boolean isNormalized();
}
