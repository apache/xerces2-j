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

import org.apache.xerces.impl.validation.datatypes.eTypes.Data.BasicStringProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;

import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.datatypes.regex.Match;

import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.StringProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Data.datime.TimeJRep;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.lang.RuntimeException;
import java.lang.Class;


/**
 *  Handles DateTime part of ISO8601.
 * <p>
 * comparison returns null for unequal calendarType or if order is not determined before precision differs
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 */
public class ISODateTime extends PartialOrder implements StringProperty {
   private Match timeMatch = null;
   TimeJRep rep = new TimeJRep();
   private Match dateMatch = null;
   private boolean date;
   private boolean time;
   final static public int classNumberSubProperties = PartialOrder.classNumberSubProperties;

   /** Determines whether str is a valid date-time.  If there is no 'T' present, checks
      first whether it is either a data or a time.
      */

   static int flag = 0;
/**
 * 
 */
   public ISODateTime() {
      super( null , "ISODateTime" );
   }
/**
 * 
 */
   public ISODateTime( String name ) {
      super( null , name );
   }
   /**
       comparison returns null for unequal calendarType, utc or if order is not determined before precision differs
    * @return java.lang.Integer
    * @param param com.ibm.eTypes.Interfaces.PO_IF
    */
   public Integer compareTo(Object param) {
      String paramClass = param . getClass() . toString();
      if ( ! ( getClass() . isAssignableFrom( param . getClass() ) ) ) {
         return null;
      }
      return getRep() . compareTo( (( ISODateTime) param) . getRep() );
   }
   public void enableSubProp(String name){ ;}
/**
 * Calendar types are ISO8601Pattern.{calendar,ordinal,week,dayOfTheWeek}
 * @return char
 */
   public char getCalendarType() {
      return getRep().getCalendarType();
   }
   /**
    * 
    * @return com.ibm.regex.Match
    */
   Match getDateMatch() {
      return dateMatch;
   }
   TimeJRep getRep(){
      return rep;
   }
   /**
    * 
    * @return com.ibm.regex.Match
    */
   Match getTimeMatch() {
      return timeMatch;
   }
   /**
    * 
    * @return boolean
    */
   public boolean isDate() {
      return date;
   }
   /**
    * 
    * @return boolean
    */
   public boolean isTime() {
      return time;
   }
   public boolean isUTC(){
      return getRep() . isUTC();
   }
   public static void main(String[] args) throws FileNotFoundException, IOException {
      ISODateTime iso = new ISODateTime();
      // Insert code to start the application here.
      if (args == null || args.length == 0) {
         args = new String[] {"/home/berman/perl/XML/date/ex.dt"};
      }
      int i;
      /*
      FileStringRW fsrw = new FileStringRW();
      for (i = 0; i < args.length; i++) {
         fsrw.clear();
         fsrw.setFile(new String[] {args[i]});
         fsrw.read(0);
         String contents = fsrw.getContents();
         StringTokenizer tok = new StringTokenizer(contents);
         while (tok.hasMoreElements()) {
            String str = (String) tok.nextElement();
            if (!iso.validate(str)) {
               System.err.println(">>>   " + str + " not valid ISODateTime\n");
            }
         }
      }
      */
   }
   /** Determines whether str is a valid ISO8601 date */
   public boolean matchDate(String str) {
      dateMatch = new Match();
      date = false;
      if (ISO8601Pattern.calendarRE.matches(str, dateMatch)) {
         date = rep.mergeCalendarDate(dateMatch);
      } else if ( ISO8601Pattern.ordinalRE.matches(str, dateMatch) ) {
         date = rep.mergeOrdinalDate(dateMatch);
      } else if ( ISO8601Pattern.dotwRE.matches(str, dateMatch) ) {
         date = rep.mergeDOTW(dateMatch);
      } else if ( ISO8601Pattern.weekRE.matches(str, dateMatch)) {
         date = rep.mergeWeekDate(dateMatch);
      }
      return isDate();
   }
   /** Determines whether str is a valid ISO8601 time */
   public boolean matchTime(String str) {
      timeMatch = new Match();
      time = false; 
      if (ISO8601Pattern.$timeRE.matches(str, timeMatch)) {
         time = true;
         time = rep.mergeTime( timeMatch );
      }
      return isTime();
   }
   public void resetRep() {
      getRep().reset();
   }
   /**
    * 
    * @param newDateMatch com.ibm.regex.Match
    */
   void setDateMatch(Match newDateMatch) {
      dateMatch = newDateMatch;
   }
   void setRep(TimeJRep m){
      rep = m;
   }
   /**
    * 
    * @param newTimeMatch com.ibm.regex.Match
    */
   void setTimeMatch(Match newTimeMatch) {
      timeMatch = newTimeMatch;
   }
   /** If 'T' does not occur in str, and str is not an ISODate, this checks
         returns true if str is an ISOTime
   */ 
   public boolean validate(Object obj) {
      String str = ( String ) obj;
      if ( flag++ == 0 ) {
         System .out. println("Order on dates not yet implemented");
      }
      date = time = false;
      resetRep(); 
      int i = str.indexOf('T');
      if (i < 0) { // date or time
         if (str.indexOf(':') >= 0) {
            matchTime(str);
            return isTime();
         } else {
            matchTime(str);
            matchDate(str);
            return isDate() || isTime() ;
         }

      } else
         if (i == 0) { // time, beginning with 'T'
         matchTime(str.substring(1));
         return time;
      } else {
         matchDate(str.substring(0, i));
         if (!isDate()) {
            return false;
         }
         matchTime(str.substring(i + 1));
         if (!isTime()) {
            return false;
         }
      }
      return true;
   }
}
