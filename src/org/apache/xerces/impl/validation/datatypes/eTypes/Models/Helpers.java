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

package org.apache.xerces.impl.validation.datatypes.eTypes.Models;




import java.lang.Class;
import java.lang.reflect.Method;
/**
 * This class provides static methods which can be used to implement certain frequently
 * occurring 'eval' and 'merge' methods.  As well as a method which turns the exceptions
 * thrown by Method.invoke into RuntimeExceptions
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class Helpers {

   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.FALSE.  
   */
   public static final Method andMethod;
   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.FALSE.  
   */
   public static final Method allFalse;
   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.FALSE.  
   */
   public static final Method orMethod;
   public static final Method maxMethod;
   public static final Method minMethod;
   public static final Method constrainBelowClosed;
   public static final Method constrainBelowOpen;
   public static final Method constrainAboveClosed;
   public static final Method constrainAboveOpen;

   static {
      try {
         andMethod = Helpers.class.getMethod("and",new Class[]{Object.class,Object.class});
         allFalse = Helpers.class.getMethod("allFalse",new Class[]{Object.class,Object.class});
         orMethod = Helpers.class.getMethod("or",new Class[]{Object.class,Object.class});
         minMethod = Helpers.class.getMethod("min",new Class[]{Object.class,Object.class});
         maxMethod = Helpers.class.getMethod("max",new Class[]{Object.class,Object.class});
         constrainAboveOpen  = Helpers.class.getMethod("constrainAboveOpen",new Class[]{Object.class,Object.class});
         constrainAboveClosed  = Helpers.class.getMethod("constrainAboveClosed",new Class[]{Object.class,Object.class});
         constrainBelowOpen  = Helpers.class.getMethod("constrainBelowOpen",new Class[]{Object.class,Object.class});
         constrainBelowClosed  = Helpers.class.getMethod("constrainBelowClosed",new Class[]{Object.class,Object.class});

      } catch (Exception e) {
         throw new RuntimeException("One (or more) of static methods Helpers.{and,or,max,min}(Object,Object) not found");
      }
   }
   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.TRUE.  
         @return <code>( thisObject==null ) ? null : accumulator</code>
   */
   public static Object allFalse(Object thisObject, Object accumulator){
      return( thisObject!=null ) ? null : accumulator;
   }
   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.TRUE.  
         @return  <code>( thisObject==null ) ? null : accumulator</code> */
   public static Object and(Object thisObject, Object accumulator){
      return( thisObject==null ) ? null : accumulator;
   }
   /** 
         @param obj must be a String which can be converted into Double or Long.  
         @param obj constraint be a Number.
         @return null or the Double/Long repr of String (depending on type of constraint)
    */
   public static final Number constrainAboveClosed(Object obj, Object constraint){
      Number num;
      boolean isLong = constraint instanceof Long;
      if ( constraint != null && ! ( constraint instanceof Number ) ) {
         return null;
      }
      if ( ! ( obj instanceof Number ) ) {
         num = (isLong) ? (Number)new Long((String)obj) : (Number) new Double((String)obj);
      } else {
         num = (Number) obj;
      }
      Number n = (Number)constraint;
      return( !isLong ) ?
      ( ( num . doubleValue() <= ((Double)constraint) . doubleValue() ) ? num : null )
      :
      ( ( num . longValue() <= ((Long)constraint) . longValue() ) ? num : null );
   }
   /** 
         @param obj must be a String which can be converted into Double or Long.  
         @param obj constraint be a Number.
         @return null or the Double/Long repr of String (depending on type of constraint)
    */
   public static final Number constrainAboveOpen(Object obj, Object constraint){
      Number num;
      boolean isLong = constraint instanceof Long;
      if ( constraint != null && ! ( constraint instanceof Number ) ) {
         return null;
      }
      if ( ! ( obj instanceof Number ) ) {
         num = (isLong) ? (Number)new Long((String)obj) : (Number) new Double((String)obj);
      } else {
         num = (Number) obj;
      }
      return( !isLong ) ?
      ( ( num . doubleValue() < ((Double)constraint) . doubleValue() ) ? num : null )
      :
      ( ( num . longValue() < ((Long)constraint) . longValue() ) ? num : null );
   }
   /** 
         @param obj must be a String which can be converted into Double or Long.  
         @param obj constraint be a Number.
         @return null or the Double/Long repr of String (depending on type of constraint)
    */
   public static final Number constrainBelowClosed(Object obj, Object constraint){
      Number num;
      boolean isLong = constraint instanceof Long;
      if ( constraint != null && ! ( constraint instanceof Number ) ) {
         return null;
      }
      if ( ! ( obj instanceof Number ) ) {
         num = (isLong) ? (Number)new Long((String)obj) : (Number) new Double((String)obj);
      } else {
         num = (Number) obj;
      }
      return( !isLong ) ?
      ( ( num . doubleValue() >= ((Double)constraint) . doubleValue() ) ? num : null )
      :
      ( ( num . longValue() >= ((Long)constraint) . longValue() ) ? num : null );
   }
   /** 
         @param obj must be a Number or String which can be converted into Double or Long.  
         @param obj constraint be a Number.
         @return null or the Double/Long repr of String (depending on type of constraint)
    */
   public static final Number constrainBelowOpen(Object obj, Object constraint){
      Number num;
      boolean isLong = constraint instanceof Long;
      if ( constraint != null && ! ( constraint instanceof Number ) ) {
         return null;
      }
      if ( ! ( obj instanceof Number ) ) {
         num = (isLong) ? (Number)new Long((String)obj) : (Number) new Double((String)obj);
      } else {
         num = (Number) obj;
      }
      return( !isLong ) ?
      ( ( num . doubleValue() > ((Double)constraint) . doubleValue() ) ? num : null )
      :
      ( ( num . longValue() > ((Long)constraint) . longValue() ) ? num : null );
   }
   /** Invokes static method, m, on two arguments.  Turns Exceptions thrown by invoke into RuntimeExceptions */
   public static Object mInvoke(Method m, Object first, Object second){
      try {
         return m . invoke( null , new Object[] { first , second} );
      } catch (Exception e) {
         throw new RuntimeException
         ("Failure in invoking static method: " + m . getName() + " with arguments\n" +
          first . toString() + " and " + second . toString());
      }
   }
   public static void main(String[] args){
   }
   /** Objects should be instances of java.class.Number.  max( a , null ) == a, max( null , a ) == null
         @return Double or Long.  max if both non-null, null if instance==null, instance if accum == null
*/
   static public Object max(Object instance, Object accum){
      if ( instance instanceof Long || accum instanceof Long ) {
         throw new RuntimeException("Long not yet handled");
      }
      if ( instance == null ) {
         return null;
      }
      if ( accum == null ) {
         return instance;
      }
      double x = ((Number)instance) . doubleValue();
      double y = ((Number)accum) . doubleValue();
      return new Double( (x<y) ? y : x );
   }
   /** Objects should be instances of java.class.Number.  min( a , null ) == a, max( null , a ) == null
         @return Double or Long.  min if both non-null, null if instance==null, instance if accum == null
*/
   static public Object min(Object instance, Object accum){
      if ( instance instanceof Long || accum instanceof Long ) {
         throw new RuntimeException("Long not yet handled");
      }
      if ( instance == null ) {
         return null;
      }
      if ( accum == null ) {
         return instance;
      }
      double x = ((Number)instance) . doubleValue();
      double y = ((Number)accum) . doubleValue();
      return new Double( (x>y) ? y : x );
   }
   /** When this method is used as merge, the accumulator should be initialized to
         Boolean.FALSE.  
         @return  <code>( thisObject!=null ) ? thisObject : accumulator</code> */
   public static Object or(Object thisObject, Object accumulator){
      return( thisObject!=null ) ? Boolean.TRUE : accumulator;
   }
}
