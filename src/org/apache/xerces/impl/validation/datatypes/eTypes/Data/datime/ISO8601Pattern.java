/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999, 2000 The Apache Software Foundation.  All rights
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
package org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime;

import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;


/**
 * Contains definitions of patterns needed for IS)8601 dates
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
interface ISO8601Pattern {
   static final RegularExpression periodRE = new RegularExpression
                                             ("P(?:(\\d+)Y)?(?:(\\d+)M)?(?:(\\d+)D)?(?:T(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?)?");
   static final String splitPeriodString = "^([^/]*)/([^/]*)$";
   static final RegularExpression splitRE = new RegularExpression( splitPeriodString );

   static final String $tripleDigit = "((?:\\d\\d\\d))";
   static final String $dashOptional = "-?";
   static final String $pairOfDigits = "((?:\\d\\d)?)";
   static final String $minusOrPairOfDigits = "(?:-|(\\d\\d))";
   static final String $minusOrPodWithColon = "(?:-?|(?::?" + $pairOfDigits + "))";
   static final String $podWithColon = "(?::?" + $pairOfDigits + ")";

   // Time stuff


   static final String  $minusOrPodFollowedByColon = "(?:-|(?:(\\d\\d)):)";

   //String $openingPairOfPairs = "(?:(?:"+ $minusOrPairOfDigits + "(\\d\\d)?)|(?:--?)|(?:" +$minusOrPodFollowedByColon + 
   //$minusOrPodFollowedByColon+ "?))?";

   static final String $openTime = "(-*)(?:(\\d\\d)(:?))?(?:(\\d\\d)(:?))?";

   static final String $closingField = "(?:(\\d\\d)(?:[\\.\\,](\\d+))?)";

   //String $timePattern= $openingPairOfPairs + $closingField;

   static final String $timePattern= $openTime + $closingField;

   static final String $diffTime = "(Z?)(?:([-+])(\\d\\d):?(\\d\\d)?)?";
   static final RegularExpression $timeRE = new RegularExpression("^" + $timePattern + 
                                                                  $diffTime + "$");

   // date stuff
   //#                            CC-0                                  YY-1
   static final String $calendarDate = $pairOfDigits + $dashOptional + $pairOfDigits + $dashOptional +
                                       //#                MM -2                         DD-3           
                                       $pairOfDigits + $dashOptional + $pairOfDigits;
   static final RegularExpression calendarRE = new RegularExpression("^" + $calendarDate + "$");

   //#                            CC             YY                              DDD
   static final String $ordinalDate = $pairOfDigits + $pairOfDigits + $dashOptional + $tripleDigit;
   //#                              \d\d | -\d | ""   note -W155 is ok, W155 is not.
   static final RegularExpression ordinalRE = new RegularExpression("^" + $ordinalDate + "$");
   static final String $weekDate = $pairOfDigits + "((?:[-\\d]\\d)?)" + "-?W" + $pairOfDigits + "-?(\\d?)";
   static final RegularExpression weekRE = new RegularExpression("^" + $weekDate + "$");
   static final String $dayOfTheWeek = "---(\\d)";

   static final int WEEK = weekRE . getNumberOfGroups();
   static final int CALENDAR = calendarRE . getNumberOfGroups();
   static final int ORDINAL = ordinalRE . getNumberOfGroups();
   static final int TIME = $timeRE . getNumberOfGroups();
   static final RegularExpression dotwRE = new RegularExpression("^" + $dayOfTheWeek + "$");
   static final int DOTW = dotwRE . getNumberOfGroups();
   static final char calendar='C',ordinal='O',week='W',dayOfTheWeek='D';
}
