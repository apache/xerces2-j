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

package org.apache.xerces.impl.validation.datatypes.eTypes.Data;

import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.*;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.Helpers;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.datatypes.regex.Match;



import java.lang.Class;
import java.lang.reflect.Method;


/**
 * Supports constraints on length, range (using lexicographic order) and  regular expression.
 *                Empty string ("") may be valid, null string always <it>invalid</it>!
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class BasicStringProperty extends PartialOrder implements StringProperty, Property {
   /** length constraints are inclusive.  If negative, no constraint*/
   final public static int MAX_LEN = PartialOrder.classNumberSubProperties + StringProperty.MAX_LEN;
   /** length constraints are inclusive.  If negative, no constraint*/
   final public static int MIN_LEN = PartialOrder.classNumberSubProperties + StringProperty.MIN_LEN;
   final public static int PATTERN = PartialOrder.classNumberSubProperties + StringProperty.PATTERN;
   final public static int classNumberSubProperties = 
   PartialOrder.classNumberSubProperties + StringProperty.classNumberSubProperties;
   {
      setSubPropName( MAX_LEN , "BasicStringProperty.MAX_LEN" );
      setSubPropName( MIN_LEN , "BasicStringProperty.MIN_LEN" );
      setSubPropName( PATTERN , "BasicStringProperty.PATTERN" );

   }
   public static final Method evalMinLenOpen;
   public static final Method evalMinLenClosed;
   public static final Method evalMaxLenOpen;
   public static final Method evalMaxLenClosed;
   public static final Method evalPattern;

   static {
      try {
         evalMinLenOpen = BasicStringProperty.class.getMethod("evalMinLenOpen",new Class[]{Object.class,Object.class});
         evalMinLenClosed = BasicStringProperty.class.getMethod("evalMinLenClosed",new Class[]{Object.class,Object.class});
         evalMaxLenClosed = BasicStringProperty.class.getMethod("evalMaxLenClosed",new Class[]{Object.class,Object.class});
         evalMaxLenOpen = BasicStringProperty.class.getMethod("evalMaxLenOpen",new Class[]{Object.class,Object.class});
         evalPattern = BasicStringProperty.class.getMethod("evalPattern",new Class[]{Object.class,Object.class});
      } catch (Exception e) {
         throw new RuntimeException("One (or more) of static methods AbstractProperty.{and,or,max,min}(Object,Object) not found");
      }
   }


   public java.lang.String sValue;
   public BasicStringProperty() {
      this( String.class , "BasicStringProperty" );
   }
   public BasicStringProperty( Class cl , String str ){
      super( cl , str );
   }
   public BasicStringProperty( String str ) {
      this( String.class , str );
   }
   // Not handled properly for types with alternate validate methods, e.g. StringProperty */
   public boolean accumulate(String dt){
      setPossible( validate( dt ), instance . intValue() );
      if ( getPossible( instance . intValue() ) ) {
         merge( this );
      } else {
         setPossible( false, constraint. intValue() );
      }
      return getPossible( instance . intValue() ) ;
   }
   /** Compare using java.lang.String.compareTo.  Will compare one a BasicStringProperty or a String */
   public Integer compareTo(Object other) {
      if ((other instanceof BasicStringProperty)) {
         return new Integer(getSValue().compareTo(((BasicStringProperty)other).getSValue()));
      } else if ((other instanceof String)) {
         return new Integer(getSValue().compareTo(((String)other)));
      }
      return null;
   }
   protected void enableSubProp( String name ){
      int index = getSubPropNameIndex( name );
      if ( index < 0 ) {
         throw new RuntimeException("Attemtp to enable subProp: " + name + "\n\tname must be qualified\n");
      }
      Class tClass = BasicStringProperty.class;
      try {
         if ( index == MAX_LEN ) {
            setSubPropEval( index , evalMaxLenClosed );
            setSubPropMerge( index , Helpers . maxMethod );
         } else if ( index == MIN_LEN ) {
            setSubPropEval( index , evalMinLenClosed );
            setSubPropMerge( index , Helpers . minMethod );
         } else if ( index == PATTERN ) {
            setSubPropEval( index , evalPattern );
         }
         /* abstract method in parent
            else
            super . enableSubProp( name );
         */
      } catch (Exception e) {
         throw new RuntimeException("method not found:" + e);
      }
   }
   /** Determines whether obj (which is String) has length &lt;= constraint (which
   is String or Integer) Returns length if yes, else null 
   */
   public final static Object evalMaxLenClosed(Object obj, Object constraint ){
      Object rv = null;
      if ( constraint == null ) {
         return null;
      }
      int oLen = ((String)obj).length();
      int cVal = (constraint instanceof Integer ) ? 
                 ((Integer)constraint) . intValue() : ((String)constraint) . length();
      if ( oLen <= cVal ) {
         rv = new Integer( oLen );
      }
      return rv;
   }
   /** Determines whether obj (which is String) has length &lt; constraint (which
   is String or Integer) Returns length if yes, else null 
   */
   public final static Object evalMaxLenOpen(Object obj, Object constraint ){
      Object rv = null;
      if ( constraint == null ) {
         return null;
      }
      int oLen = ((String)obj).length();
      int cVal = (constraint instanceof Integer ) ? 
                 ((Integer)constraint) . intValue() : ((String)constraint) . length();
      if ( oLen < cVal ) {
         rv = new Integer( oLen );
      }
      return rv;
   }
   /** Determines whether obj (which is String) has length &gt;> constraint (which
   is String or Integer).  Returns length if yes, else null This method works for maxLen, minLen, and Length 
   */
   public final static Object evalMinLenClosed(Object obj, Object constraint ){
      Object rv = null;
      if ( constraint == null ) {
         return null;
      }
      int oLen = ((String)obj).length();
      int cVal = (constraint instanceof Integer ) ? 
                 ((Integer)constraint) . intValue() : ((String)constraint) . length();
      if ( oLen >= cVal ) {
         rv = new Integer( oLen );
      }
      return rv;
   }
   /** Determines whether obj (which is String) has length &gt; constraint (which
   is String or Integer).  Returns length if yes, else null This method works for maxLen, minLen, and Length 
   */
   public final static Object evalMinLenOpen(Object obj, Object constraint ){
      Object rv = null;
      if ( constraint == null ) {
         return null;
      }
      int oLen = ((String)obj).length();
      int cVal = (constraint instanceof Integer ) ? 
                 ((Integer)constraint) . intValue() : ((String)constraint) . length();
      if ( oLen > cVal ) {
         rv = new Integer( oLen );
      }
      return rv;
   }
   /** Determines whether obj (which is String) matches Pattern contained in
         constraint.  If <code>constraint instanceof RegularExpression </code> it returns
         Boolean.TRUE or null.  If <code>constraint instanceof Object[] && constraint[0]
         instanceof RegularExpression && constraint[1] instanceof Match</code> 
        returns the match (after matching) or null 
   */
   public final static Object evalPattern(Object obj, Object constraint ){
      boolean rv = false;
      if ( constraint == null ) {
         throw new RuntimeException("evalPattern called with null pattern");
         //						return null;
      }
      if ( obj == null ) {
         return null;
      }
      String target = (String) obj;
      RegularExpression re;
      Object match = Boolean.TRUE;
      if ( constraint instanceof RegularExpression ) {
         re = (RegularExpression) constraint;
         rv =  re . matches( target );
      } else {

         re = (RegularExpression) ((Object[])constraint)[ 0 ];
         match = ((Object[])constraint)[ 1 ];
         rv = re . matches( target , (Match) match );
      }
      return( rv ) ? match : null;
   }
   /** If &gt; 0, length of longest string  which may be valid */
   public int getMaxLength(Integer type) {
      return( (Integer) getSubProp( MAX_LEN , type ) ) . intValue();
   }
   /** If &gt; 0, length of shortest string  which may be valid */
   public int getMinLength(Integer type) {
      return( (Integer) getSubProp( MIN_LEN , type ) ) . intValue();
   }
   /**
    * Returns pattern (Perl 5 syntax) which restricts form of valid strings
    * @return java.lang.String
    */
   public String getPattern() {
      RegularExpression re = (RegularExpression) getSubProp( PATTERN , Property.constraint );
      return( re == null ) ? null : re . getPattern();
   }
   /**
    * Returns options assoc with pattern (Perl 5 syntax) (regex.jar)
    * @return java.lang.String
    */
   public String getPatternOptions() {
      RegularExpression re = (RegularExpression) getSubProp( PATTERN , Property.constraint );
      return( re == null ) ? null : re . getOptions();
   }
/**
 * 
 * @return java.lang.String
 */
   public java.lang.String getSValue() {
      return sValue;
   }
   public String                       mapString(String o) {
      return o;
   }
   /** length constraints are inclusive */
   public void setMaxLength(int newMaxLength, Integer type) {
      setSubProp( new Integer( newMaxLength ) , MAX_LEN, type );
   }
   /** length constraints are inclusive */
   public void setMinLength(int newMinLength, Integer type) {
      setSubProp( new Integer( newMinLength ) , MIN_LEN , type );
      //				minLength = newMinLength;
   }
   /**
    * Sets pattern which restricts form of valid strings
    * @param newPattern java.lang.String
    */
   public void setPattern(String newPattern) {
      setPattern( newPattern , null );
   }
   public void setPattern(String newPattern , String options){
      enableSubProp( "BasicStringProperty.PATTERN" );
      if ( newPattern != null ) {
         setSubProp( new RegularExpression( newPattern , options ), 
                     PATTERN ,
                     Property.constraint );
      } else {
         setSubProp( null , PATTERN , Property.constraint );            
      }
   }
/**
 * 
 * @param newSValue java.lang.String
 */
   public void setSValue(java.lang.String newSValue) {
      sValue = newSValue;
   }
   /** Regular expressions are mutable so 'clone' it */
   public Property twin(){
      BasicStringProperty twin = (BasicStringProperty) super . twin();
      String re = getPattern();
      if ( re != null ) {
         twin . setPattern( getPattern() , getPatternOptions() );
      }
      return twin;
   }
   /** Null string is always <it>invalid</it>! 
     */
   public boolean validate(String val) {
      if (val == null) {
         return false;
      }
      return super.validate(val);
   }
}
