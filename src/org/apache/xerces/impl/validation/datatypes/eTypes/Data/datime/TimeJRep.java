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
import org.apache.xerces.impl.validation.datatypes.regex.Match;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.AbstractProperty;

import java.lang.RuntimeException;


/**
 * Underlying representation used for date/time/period classes
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class TimeJRep {
   Integer[] when = new Integer[TimeJRep.fieldIndicator];
   Integer[] utcPart = new Integer[] { null , null};
   char utcSign = '@';
   /** one of : ISO8601Pattern.calendar regular calendar , ISO8601Pattern.ordinal ordinal , ISO8601Pattern.dayOfTheWeek day of the week*/
   char calendarType = '@'; 
   /** fieldIndicator: the number of integral fields in a point in time */
   private static int fieldIndicator = 0;

   public final static int CURRENT = -2;
   public final static int YEAR_OF_CURRENT_DECADE = -3;
   final static int C = fieldIndicator++, Y = fieldIndicator++, W = fieldIndicator++, 
   M = W, D = fieldIndicator++, h = fieldIndicator++, 
   m =   fieldIndicator++, s = fieldIndicator++, fp = fieldIndicator++; 
   final static int CENTURY = C, YEAR = Y, WEEK = W, MONTH = M, DAY = D, HOUR = h,
   MINUTE = m, SECOND = s, FRACTIONAL = fp; 
   public final static char[] indicatorChar = {'C', 'Y', 'W', 'M', 'D', 'h', 'm', 's'};
   public final static String indicator = "CYMDhms";

   public String message;

   static int[] timeIndexer = new int[] { HOUR , MINUTE, SECOND};
   /** comparison returns null for unequal calendarType, utc or if order is not determined before precision differs */
   public Integer compareTo(TimeJRep other){
      if ( getCalendarType() != other . getCalendarType() ) {
         return null;
      }
      if ( getUTCSign() != other . getUTCSign() ) {
         return null;
      } else if ( getUTCHour() != other . getUTCHour() ) {
         return null;
      } else if ( getUTCMinute() != other . getUTCMinute() ) {
         return null;
      }
      int i;
      for ( i = 0 ; i < FRACTIONAL ; i++ ) {
         if ( ( getWhen( i ) == null && other . getWhen( i ) != null ) ||
              ( getWhen( i ) != null && other . getWhen( i ) == null ) ) {
            return null;
         }
         if ( getWhen( i ) == null && other . getWhen( i ) == null ) {
            continue;
         }
         int here = getWhen( i ) . intValue() , there = other . getWhen( i ) . intValue();
         if ( here < there ) {
            return new Integer( -1 );
         } else if ( there < here ) {
            return new Integer( 1 );
         }
      }

      return new Integer( 0 );
   }
   public char getCalendarType() {
      return calendarType;
   }
   final public static String getField(Match m, int i) {
      try {
         return m.getCapturedText(i);
      } catch (Exception e) {
         return null;
      }
   }
   public String getMessage() {
      return message;
   }
   public Integer getUTCHour() {
      return utcPart[0];
   }
   public Integer getUTCMinute() {
      return utcPart[1];
   }
   public char getUTCSign() {
      return utcSign;
   }
   public Integer getWhen(int i){
      return when[ i ];
   }
   public boolean isUTC() {
      return utcSign != '@';
   }
   public boolean isValid() {
      int flag = 0;
      // A valid date considers of a single sequence of non-nulls.
      for (int i = 0; i < FRACTIONAL ; i++) {
         if (flag == 0) { // only seen nulls so far
            if (getWhen(i) != null) {
               flag++;
            }
            continue;
         }
         if (flag == 1) { // in initial sequence of non-nulls
            if (getWhen(i) == null) {
               flag++;
            }
            continue;
         }
         if (flag == 2) {
            if (getWhen(i) != null) {
               return false;
            }
         }
      }
      return flag != 0;
   }
   boolean mergeCalendarDate(Match m) {
      // First group of match is the whole string.
      // Skip the optional dashes
      if (getCalendarType() != '@') {
         throw new RuntimeException("Attempt to set calendar type to monthly calendar ('C') when it is " + " already set.  Call reset.  Current calendar type: " + calendarType);
      }
      setCalendarType( ISO8601Pattern.calendar );
      // 0^th match is whole group
      for (int i = 1; i < ISO8601Pattern.CALENDAR ; i++) {
         String txt = getField(m, i );
         if (txt.length() == 2) {
            setWhen(i - 1, new Integer(txt));
         }
      }
      return isValid();
   }
   boolean mergeDOTW(Match m) {
      int i = new Integer(getField(m, 1)).intValue();
      if (getCalendarType() != '@') {
         throw new RuntimeException("Attempt to set calendar type to day of the week ('D') when it is " + " already set.  Call reset.  Current calendar type: " + calendarType);
      } else {
         setCalendarType( ISO8601Pattern.dayOfTheWeek );
      }
      if (0 < i || i <= 7) {
         setWhen(DAY, new Integer(i));
      } else {
         setMessage("Day number must be in [1..7]: " + getField(m, 0));
         return false;
      }
      return isValid();
   }
   /**
    * Processes match for ISO8601 ordinal, i.e. day 1--365.  Coordinated with setWhen
    * @return boolean
    * @param calMatch com.ibm.regex.Match
    */
   boolean mergeOrdinalDate(Match m) {
      if (getCalendarType() != '@') {
         throw new RuntimeException("Attempt to set calendar type to ordinal ('O') when it is " + " already set.  Call reset.  Current calendar type: " + calendarType);
      } else {
         setCalendarType( ISO8601Pattern.ordinal );
         setWhen( MONTH , new Integer("0") );
      }
      // First group of match is the whole string.
      // Skip the optional dashes
      String field0 = m.getCapturedText(CENTURY + 1);
      String field1 = m.getCapturedText(YEAR+ 1);
      String field2 = m.getCapturedText(WEEK + 1);
      if ( field1 . length() == 0 ) {
         if ( field0 . length() == 2 ) {
            setWhen( YEAR , new Integer( field0 ));
         }
      } else {
         setWhen( CENTURY, new Integer(field0));
         setWhen( YEAR, new Integer( field1 ));
      }
      setWhen( DAY , new Integer( field2 ));

      if ( getWhen( YEAR ) == null && getField(m,0) . indexOf('-') < 0 ) {
         setMessage("Ordinal date must have year or '-': " + getField(m,0) );
         return false;
      }
      return isValid();
   }
   /**
    * Fills the when[] from the match.  Note the time match is handled differently
    than other matches.  In the time match, 1 is string of leading '-'s, , 2,3
    correspond to the first digit pair and  colon; 4,5 to second digit pair, colon
    6,7 corresponds to the digit pair which (may) have a decimal part.  2,4 are digits
    if they are non-null.  If 5 == ':', 3 must == ':'.  
    * @return boolean
    * @param calMatch com.ibm.regex.Match
    */
   public boolean mergeTime(Match m) {
      String s1 = m.getCapturedText(1); // leading -'s
      String s2 = m.getCapturedText(2); // first digit pair
      String s3 = m.getCapturedText(3); // first :
      String s4 = m.getCapturedText(4); // second digit pair
      String s5 = m.getCapturedText(5); // second :
      String s6 = m.getCapturedText(6); // final digit pair
      String s7 = m.getCapturedText(7); // fractional part
      int i = 0;
      String txt = null;
      // colon must be used everywhere if it is used anywhere.
      if (":".equals(s3)) {
         if (s5 != null && !s3.equals(s5)) {
            return false;
         }
      } else
         if (":".equals(s5)) {
         if (s3 != null && !s5.equals(s3)) {
            return false;
         }
      }
      // determine  location of the closing field?
      int firstField = s1.length();
      int numberOfFieldsPresent = 1 + // closing field
                                  ((s2 != null && s2.length() == 2) ? 1 : 0) + // leading digit pair
                                  ((s4 != null && s4.length() == 2) ? 1 : 0); // final opening digit pair
      // next predicates determine precision of time
      if (firstField + numberOfFieldsPresent > 3) {
         return false;
      }
      boolean secondsPresent = firstField + numberOfFieldsPresent == 3;
      boolean minutesLead = firstField == 1;
      boolean hoursPresent = firstField == 0;
      boolean hoursOnly = numberOfFieldsPresent == 1 && hoursPresent;
      int nextField = firstField;
      if (firstField == 0) {
         if (s2 != null) {
            setWhen(HOUR, new Integer(s2));
            if (s4 != null) {
               setWhen(MINUTE, new Integer(s4));
               setWhen(SECOND, new Integer(s6));
            } else {
               setWhen(MINUTE, new Integer(s6));
            }
         } else
            if (s4 != null) {
            setWhen(HOUR, new Integer(s4));
            setWhen(MINUTE, new Integer(s6));
         } else {
            setWhen(HOUR, new Integer(s6));
         }
      }
      // MINUTES lead
      else
         if (firstField == 1) {
         if (s2 != null) {
            setWhen(MINUTE, new Integer(s2));
            //Assert.isTrue(s4 == null);
            setWhen(SECOND, new Integer(s6));
         } else
            if (s4 != null) {
            setWhen(MINUTE, new Integer(s4));
            setWhen(SECOND, new Integer(s6));
         } else {
            setWhen(MINUTE, new Integer(s6));
         }
      }
      // SECONDS lead
      else
         if (firstField == 2) {
         //Assert.isTrue(s2 == null && s4 == null);
         setWhen(SECOND, new Integer(s6));
      } else {
         setMessage("No time present: " + m.getCapturedText(0));
         return false;
      }
      if (s7 != null) {
         setWhen(FRACTIONAL, new Integer(s7));
      }
      // Now onto the UTC part
      i = 8;
      txt = getField(m, i);
      // Now UTC
      if (txt.equals("Z")) {
         setUTCSign('Z');
         if (m.getBeginning(i + 1) >= 0) {
            setMessage("'Z' UTC indicator is incompatible with relative time");
            return false;
         }
      } else
         if (m.getBeginning(i + 2) >= 0) { // UTC
         if (getWhen(HOUR) == null) {
            setMessage("relative UTC format must have hour in local time: " + m.getCapturedText(0));
            return false;
         }
         i++;
         txt = getField(m, i);
         if (txt == null || txt.equals("")) {
            setMessage("UTC format must have sign if offset is present: " + m.getCapturedText(0));
            return false;
         }
         setUTCSign(txt.charAt(0));
         // next  place number of hours into txt 
         txt = getField(m, i + 1);
         if (txt == null || txt.equals("")) {
            setMessage("UTC format must have hour if time is present: " + m.getCapturedText(0));
            return false;
         }
         setUTCHour(new Integer(txt));
         i++;
         txt = getField(m, i + 1);
         if (txt == null || txt.equals("")) {
            setUTCMinute(new Integer(0));
         } else {
            setUTCMinute(new Integer(txt));
         }
      }
      return isValid();
   }
   boolean mergeWeekDate(Match m) {
      if (getCalendarType() != '@') {
         throw new RuntimeException("Attempt to set calendar type to day of the week ('W') when it is " + " already set.  Call reset.  Current calendar type: " + calendarType);
      } else {
         setCalendarType( ISO8601Pattern.week );
      }
      // 0^th match is whole group
      // if 1st or 2nd is "", the century is null.
      String sC = getField(m, CENTURY + 1);
      String sY = getField(m, YEAR + 1);
      String sW = getField(m, WEEK + 1);
      String sD = getField(m, DAY + 1);
      if (sC.length() == 0 || sY.length() == 0 ) {
         if (sC.length() != 0 || sY.length() != 0 ) {
            setWhen( YEAR, new Integer(  (sC.length()==0) ? sY : sC ));
         }
      } else {
         setWhen( CENTURY , new Integer( sC ));
         setWhen( YEAR , new Integer( sY ));
      }
      if ( sW . length() != 0 ) {
         setWhen( WEEK, new Integer( sW ));
      }
      if ( sD . length() != 0 ) {
         setWhen( DAY, new Integer( sD ));
      }
      Integer val = getWhen(DAY);
      return isValid();
   }
   private String out( int i ){
      String output = "" + i;
      while ( output . length() < 2 ) {
         output = "0" + output;
      }
      return output;
   }
   final public void reset() {
      int i;
      for (i = 0; i < when.length; i++) {
         when[i] = null;
      }
      utcPart[0] = utcPart[1] = null;
      utcSign = '@';
      message = "";
      setCalendarType( '@' );
   }
   protected void setCalendarType(char newCalendarType) {
      calendarType = newCalendarType;
   }
   public void setMessage(String newMessage) {
      message += "\n" + newMessage;
   }
   public void setUTCHour(Integer h) {
      utcPart[0] = h;
   }
   public void setUTCMinute(Integer h) {
      utcPart[1] = h;
   }
   public void setUTCSign(char c) {
      utcSign = c;
   }
   // Should put a checker in here to make sure values are in bound
   public void setWhen(int i, Integer v) {
      switch (i) {
      case 0 : // CENTURY :
      case 1 : //YEAR :
         // pattern guarantees that this is
         break;
      case 2 :
         { // MONTH : // NOTE that MONTH and WEEK share an integer
            if (getCalendarType() == ISO8601Pattern.calendar) {
               if (v.intValue() > 12 || v.intValue() < 1) {
                  setMessage("MONTH must be in 1-12.  Illegal value>> " + v.intValue());
                  return;
               }
            } else
               if (getCalendarType() == ISO8601Pattern.week) {
               if (v.intValue() > 53 || v.intValue() < 1) {
                  setMessage("MONTH must be in 1-12.  Illegal value>> " + v.intValue());
                  return;
               }
               if (v.intValue() == 53) {
                  setMessage("Warning.  WEEK is 53");
                  return;
               }
            } else
               if (getCalendarType() == ISO8601Pattern.ordinal) {
               when[i++] = new Integer(-1);
               if (v.intValue() > 366 || v.intValue() < 1) {
                  if (v.intValue() == 366 && (when[2] != null && when[2].intValue() % 4 == 0)) {
                     ; // OK
                  } else {
                     setMessage("Ordinal day must be in range 1-365.If leap year, 366 and set year first.\n\t  Illegal value>> " + v.intValue() + "Year == " + when[2]);
                     return;
                  }
               }
            }
            break;
         }
      case 3 :
         { // DAY :
            if (getCalendarType() == ISO8601Pattern.calendar) {
               if (v.intValue() > 31 || v.intValue() < 1) {
                  setMessage("DAY must be in 1-31.  Illegal value>> " + v.intValue());
                  return;
               }
            } else {
               if (getCalendarType() == ISO8601Pattern.dayOfTheWeek || getCalendarType() == ISO8601Pattern.week ) {
                  if (v.intValue() > 7 || v.intValue() < 1) {
                     setMessage("Day of the week must be in 1-7.  Illegal value>> " + v.intValue());
                     return;
                  }
               } else {
                  if (getCalendarType() == ISO8601Pattern.ordinal) {
                     if (v.intValue() > 366 || v.intValue() < 1) {
                        if (v.intValue() == 366 && (when[2] != null && when[2].intValue() % 4 == 0)) {
                           ; // OK
                        } else {
                           setMessage("Ordinal day must be in range 1-365.If leap year, 366 and set year first.\n\t  Illegal value>> " + v.intValue() + "Year == " + when[2]);
                           return;
                        }
                     }
                  } else {
                     setMessage("Calendar type must be set to one of 'C','D','O'.  It is" + (('@' == getCalendarType()) ? "unset " : new String("calendarType")));
                     return;
                  }
               }
            }
            break;
         }
      case 4 : // HOUR:
         if (v.intValue() > 24) {
            setMessage("Hour must be 0-24");
            return;
         } else
            if (v.intValue() == 24 && when[5] != null && when[5].intValue() != 0) {
            setMessage("Hour must be 0-24, if 24, minutes should be 0.  Minutes =" + when[5]);
            return;
         }
         break;
      case 5 : //MINUTE: 
         if (v.intValue() > 59) {
            setMessage("Minute must be 0-59");
            return;
         }
         break;
      case 6 : // SECOND
         if (v.intValue() > 59) {
            setMessage("Second must be 0-59");
            return;
         }
         break;
      case 7: // FRACTIONAL
         break;
      default :
         setMessage("setWhen called with unexpected int: " + i);
         return;
      }
      when[i] = v;
   }
   public String toString(){
      int start = 0;
      int stop = fieldIndicator;
      StringBuffer sb = new StringBuffer("");
      int j;
      for ( j = start ; j < stop ; j++ ) {
         if ( j == h && sb . toString() . length() != 0 ) {
            sb . append("T");
         }
         int i = when[ j ].intValue();
         if ( j <= TimeJRep . D ) {
            sb . append( (i < 0) ? "-" : out( i ) );
         } else {
            sb . append( (i < 0) ? ":" : out( i ) );
         }
      }
      return sb . toString();
   }
}
