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


package org.apache.xerces.impl.validation.datatypes.eTypes.Data.Number;

import org.apache.xerces.impl.validation.datatypes.eTypes.Data.BasicStringProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.PartialOrder;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.Helpers;
import org.apache.xerces.impl.validation.datatypes.eTypes.Models.AbstractProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.PO_IF;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.StringProperty;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.Property;
import org.apache.xerces.impl.validation.datatypes.regex.RegularExpression;
import org.apache.xerces.impl.validation.datatypes.regex.Match;

import java.util.StringTokenizer;
import java.io.IOException;
import java.lang.Class;
import java.lang.reflect.Method;


/**
 *  This is the basic class for determining  whether strings which represent numbers.
 * obey certain syntactic constraints.  The constraints are very close to the 'usual' definitions.
 * 
 *        They differ from XML definitions in that an XML Schema integer can contain a decimal point while
 *        mInteger cannot and an XML float (or double) need not contain a decimal or scientific notation,
 *        while mFloat and mDouble must have both.
 * 
 *        In addition, arbitrary length or precision numbers are not supported.
 * 
 * 
 * However, they have been selected so that the string being validated can be reconstructed from
 * the value and the properties: signed, fixed, integral, precision, scale.
 * 
 *        We supply 4 properites:
 * 1) mInteger - does not have a decimal point and does not use scientific notation
 * 2) mFixed - a number with a decimal point and without scientific notation
 * 3) mFloat, mDouble - numbers written with a decimal point and scientific notation
 * 4) mNumber - validates any of the above.  Note 123e4 does not currently validate.
 * 
 * Note that since the subProp's introduced here are immutable, setTwin is not overriden.
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public class mNumber extends PartialOrder implements StringProperty {
   Number number;
   final public static int FIXED = PartialOrder.classNumberSubProperties;
   final public static int INTEGRAL = FIXED + 1;
   final public static int PRECISION = INTEGRAL + 1;
   final public static int SCALE = PRECISION + 1;
   /** constraint on the 'SIGNED'<br> subProp == true ==>> all numbers must have sign
         <br> subProp == false ==>> no numbers may have sign 
   */
   final public static int SIGNED = SCALE + 1;
   final public static int classNumberSubProperties = SIGNED + 1;
   {
      setSubPropName( FIXED , "mNumber.FIXED" );
      setSubPropName( INTEGRAL , "mNumber.INTEGRAL" );
      setSubPropName( PRECISION , "mNumber.PRECISION" );
      setSubPropName( SCALE , "mNumber.SCALE" );
      setSubPropName( SIGNED , "mNumber.SIGNED" );
   }
   public static final Method isFixed;
   public static final Method isSigned;
   public static final Method isIntegral;
   public static final Method scale;
   public static final Method precision;

   private final Number value = null;

   private final static RegularExpression numberRE= new RegularExpression("^([-+]?)(\\d*)(?:\\.(\\d*)(?:([eE])([+-]?)(\\d+))?)?$");
   /** Matches optional sign, followed by optional digits, a decimal point, and more optional digits */
   private final static RegularExpression decodeNonIntegerRE = new RegularExpression("[-+]?(\\d*)\\.(\\d*)");

   /** This method insures that the next call to validate is independent of the current
         state of the type.
   */
   static int resetFlag = 0;
   static {
      try {
         isFixed = mNumber.class.getMethod("isFixed",new Class[]{Object.class,Object.class});
         isIntegral = mNumber.class.getMethod("isIntegral",new Class[]{Object.class,Object.class});
         isSigned = mNumber.class.getMethod("isSigned",new Class[]{Object.class,Object.class});
         scale = mNumber.class.getMethod("scale",new Class[]{Object.class,Object.class});
         precision = mNumber.class.getMethod("precision",new Class[]{Object.class,Object.class});
      } catch (Exception e) {
         throw new RuntimeException("One (or more) of static methods mNumber.{and,or,max,min}(Object,Object) not found");
      }
   }

   public mNumber(Number num) {
      super();
      number = num;
   }
   public mNumber( String name ) {
      super( null , name );
   }
   /** Returns false if this and right are not from the same class.  Note that this may not be what is expected. 
   Also, the current implementation only compares to the precision of Double.  */

   public Integer compareTo(Object right) {
      Integer rVal;
      Object lte;
      Object gte;
      lte = Helpers . mInvoke( Helpers . constrainAboveClosed, getValue() , right );
      gte = Helpers . mInvoke( Helpers . constrainBelowClosed, getValue() , right );
      if ( gte == null ) {
         rVal = new Integer( 1 );
      } else if ( lte == null ) {
         rVal = new Integer( -1 );
      } else {
         rVal = new Integer( 0 );
      }
      return rVal;
   }
   public void enableSubProp( String name ){
      int index = getSubPropNameIndex( name );
      try {
         if ( index == FIXED ) {
            setSubPropEval( index, isFixed);
            setSubPropMerge( index, Helpers . andMethod );
         } else if ( index == INTEGRAL ) {
            setSubPropEval( index, isIntegral );
            setSubPropMerge( index, Helpers . andMethod );
         } else if ( index == PRECISION ) {
            setSubPropEval( index, precision );
            setSubPropMerge( index, Helpers . maxMethod );
         } else if ( index == SCALE ) {
            setSubPropEval( index, scale );
            setSubPropMerge( index, Helpers . maxMethod );
         } else if ( index == SIGNED ) {
            setSubPropEval( index, isSigned );
            setSubPropMerge( index, Helpers . orMethod );
         }
         /*  abstract in parent 
         else {
               super . enableSubProp( name );
         }
         */
      } catch (Exception e) {
         throw new RuntimeException("method not found:" + e);
      }
   }
   public final Number getValue() {
      return value;
   }
   int intValue(){
      return number.intValue();
   }
   /** Returns true if (a) matches decodeNonIntegerRE and (2) at least one digit must be present
   before or after the decimal point 
   */
   public static final Boolean isFixed(Object obj, Object constraint){
      String s;
      try {
         s = (String)obj;
      } catch (ClassCastException e) {
         return null;
      }
      Match m = new Match();
      if ( ! decodeNonIntegerRE . matches( s , m ) ) {
         return null;
      }
      if ( m . getBeginning( 0 ) != 0 || // match whole string
           m . getEnd( 0 ) != s . length() ||
           m . getCapturedText( 1 ) . length() + m . getCapturedText( 2 ) . length() == 0 ) {
         return null;
      }
      return Boolean.TRUE;
   }
   /** @return <code>true</code> if obj is a String of one or more digits */
   public static final Boolean isIntegral(Object obj, Object constraint){
      String s;
      try {
         s = (String)obj;
      } catch (ClassCastException e) {
         return null;
      }
      boolean sign = s.charAt(0)=='+' || s.charAt(0)=='-' ;
      if ( sign ) {
         s = s . substring(1);
      }
      return( ( new RegularExpression("^\\d+$") ) . matches( s ) ) ?
      Boolean.TRUE : null;
   }
   /** @return true if first character of String is '+' or '-' */
   public static final Boolean isSigned(Object obj, Object constraint){
      String s;
      try {
         s = (String)obj;
      } catch (ClassCastException e) {
         return null;
      }
      return( s.charAt(0)=='+' || s.charAt(0)=='-' ) ? Boolean.TRUE : null ;
   }
   long longValue(){
      return number.longValue();
   }
   public static void main(String[] args) throws IOException {
      mNumber me = new mNumber( "unknown number type" );
      if (args == null || args.length == 0) {
         args = new String[] {"/home/berman/perl/XML/number/number.ex"};
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
            //								me . reset();
            if (me.validate(str)) {
               System.out.println(str + " is a number\n");
            } else {
               System.out.println(str + " not a number number\n");
            }
         }
      }
      */
   }
   /** Note: does not verify that obj is a properly formatted number, merely that the part which matches
         decodeNonIntegerRE has proper form.
         @param constraint should be a java.lang.Number
         @return number of decimal digits in obj or null if this number is  greater than constraint
    */
   public static final Integer precision(Object obj, Object constraint){
      String s;
      int iConstraint = Integer.MAX_VALUE;
      try {
         s = (String)obj;
         if (constraint != null ) {
            iConstraint = ((Number)constraint) . intValue();
         }
      } catch (ClassCastException e) {
         return null;
      }
      Match m = new Match();
      if ( ! decodeNonIntegerRE . matches( s , m ) ) {
         return null;
      }
      int prec = m . getCapturedText( 1 ) . length() + m . getCapturedText( 2 ) . length();
      if ( prec > iConstraint ) {
         return null;
      }
      return new Integer( prec );
   }
   /** @param newFixed.  If true, number must contain a decimal point and no non-digits except
         and optional leading sign */
   public void requireFixed(boolean newFixed) {
      setSubProp( (newFixed) ? Boolean.TRUE : null , FIXED, Property.constraint );
      setSubProp( null , FIXED , Property.accumulate );
      if ( newFixed ) {
         setSubPropEval( FIXED , isFixed );
         setSubPropMerge( FIXED, Helpers . andMethod );
      } else {
         setSubPropEval( FIXED , null );
         setSubPropMerge( FIXED, null );
      }
   }
   public void requireIntegral(boolean newIntegral) {
      setSubProp( (newIntegral) ? Boolean.TRUE : null , INTEGRAL, Property.constraint );
      setSubProp( null , INTEGRAL , Property.accumulate );
      if ( newIntegral ) {
         setSubPropEval( INTEGRAL , isIntegral );
         setSubPropMerge( INTEGRAL, Helpers . andMethod );
      } else {
         setSubPropEval( INTEGRAL , null );
         setSubPropMerge( INTEGRAL, null );
      }
   }
   public void requireSigned(Boolean newSigned) {
      setSubProp( newSigned , SIGNED, Property.constraint );
      setSubProp( null , SIGNED , Property.accumulate );
      if ( newSigned == null ) {
         setSubPropEval( SIGNED , null );
         setSubPropMerge( SIGNED, null );
      } else if ( newSigned . booleanValue() ) {
         setSubPropEval( SIGNED , isSigned );
         setSubPropMerge( SIGNED, Helpers . andMethod );
      } else {
         setSubPropEval( SIGNED , isSigned );
         setSubPropMerge( SIGNED, Helpers . allFalse );
      }
   }
   /** Note: does not verify that obj is a properly formatted number, merely that the part which matches
         decodeNonIntegerRE has proper form.
         @param constraint should be a java.lang.Number
         @return number of decimal digits in fractional part of obj or null if  precision &gt; constraint
    */
   public static final Integer scale(Object obj, Object constraint){
      String s;
      int iConstraint = Integer.MAX_VALUE;
      try {
         s = (String)obj;
         if (constraint != null ) {
            iConstraint = ((Number)constraint) . intValue();
         }
      } catch (ClassCastException e) {
         return null;
      }
      Match m = new Match();
      if ( ! decodeNonIntegerRE . matches( s , m ) ) {
         return null;
      }
      int scale = m . getCapturedText( 2 ) . length();
      if ( scale > iConstraint ) {
         return null;
      }
      return new Integer( scale );
   }
   /**
    * Determines whether the string meets the syntactic requirements expressed through the fields:

    fixed, integral, signed, precision, and scale.

    On return, the values of these fields have been set to the numbers appropriate to the string.
    THEREFORE, if validate is called again, reset() should be called unless you want to know
    whether the second argument has the same syntactic restrictions as the first.
   */
   public boolean validate(Object obj) {
      Match m = new Match();
      if ( ! ( obj instanceof String ) ) {
         return false;
      }
      String str = (String) obj;
      if (!numberRE.matches(str, m)) {
         return false;
      }
      // first make sure that value is in range, if bounds are set
      if (!super.validate( str )) {
         return false;
      }
      return true;
   }
   /**
    * Determines whether the string meets the syntactic requirements expressed through the fields:

    fixed, integral, signed, precision, and scale.

    On return, the values of these fields have been set to the numbers appropriate to the string.
    THEREFORE, if validate is called again, reset() should be called unless you want to know
    whether the second argument has the same syntactic restrictions as the first.
   */
   public boolean validate(String str) {
      Match m = new Match();
      if (!numberRE.matches(str, m)) {
         return false;
      }
      // first make sure that value is in range, if bounds are set
      if (!super.validate( str )) {
         return false;
      }
      return true;
   }
}
