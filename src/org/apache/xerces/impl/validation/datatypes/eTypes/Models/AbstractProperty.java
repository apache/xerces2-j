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

import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.Property;
import org.apache.xerces.impl.validation.datatypes.eTypes.Interfaces.StringProperty;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class models a property which consists of a set of sub-properties, each of
 *      which can be constrained.  Each of these sub-properties corresponds to a column in the
 *      <code>subProp</code> array and requires two static methods.  One method (eval)
 *      expects the argument to validate and the sub-property constraint as parameters.  It
 *      returns null if and only if the object fails to satisfy the constraint.  The second
 *      (merge) expects the result of eval and a 'running total' as arguments and returns a
 *      new running total.  <em>Note the (1) twin (used by factory methods) assumes that the
 *      objects used as constraints are not modified and (2) accumulation assumes that a
 *      newly constructed object has <code>null</code> in the third row of
 *      subProp[3][#sub-properties].</em>
 * 
 * @author Leonard C. Berman
 * @author Jeffrey Rodriguez
 * @version $Id$
 */
public abstract class AbstractProperty implements Property, Cloneable {
   private String name;
   private Class jClass;
   protected Boolean required;
   private AbstractSummary  report = new AbstractSummary();
   /** subProperties are the things that can be accumulated.  The following static field should be 
         hidden as sub-properties are added in derived classes.
   */
   final public static int classNumberSubProperties = 
   Property.classNumberSubProperties;
   /** Table use to keep track of classNumberSubProperties for different classes */

   public static Hashtable ht = new Hashtable();
   /** Holds objects which represent subProperties of this property.  This a a 3xk
   array: instance, constraint, accumulate.  If the constraint field is non-null,
   the instance field must be non-null or validate fails. */
   private Object[][] subProp;
   private String[] subPropName;
   /** Holds methods which (1) evaluate and (2) results of validate. */
   private Method[][] subPropMethod;
   /** Holds methods used to merge the subProp computed for an object into the accumulator.  These */
   {
      int i = 0;
      Object o = ht . get( getClass() );
      if ( o == null ) {
         subProp = getSubPropArray(getClass());
         if ( subProp != null )
            i = subProp[ 0 ] . length;
         ht . put( getClass() , new Integer( i ) );
      } else {
         i = ((Integer) o ) . intValue();
         if ( i != 0 )
            subProp = new Object[3][ i ];
      }
      if ( i != 0 ) {
         subPropName = new String[ i ];
         subPropMethod = new Method[ 2 ][ i ];
      }
   }
   public class AbstractSummary extends Vector implements ValidationSummary {
      AbstractSummary(){
         ;
      }
      protected AbstractSummary(String msg){
         v . addElement( msg );
      }
      Vector v = new Vector();
      public void clear(){
         v . removeAllElements();
      }
      public String summaryString(){
         //return Perl.join("\n",report . elements());
         return null;
      }
      public void merge(ValidationSummary as) {
         if (as == null) {
            return;
         }
         v.addElement(as);
      }
   }
   private boolean[] possible = new boolean[] { true , true , true};
   public AbstractProperty() {
      this(null,null);
      setName( getClass() . toString() );
   }
   /** sets underlying type of property */
   protected AbstractProperty(Class cl){
      this( cl , null );
      setName( getClass() . toString() );
   }
   /** Sets  underlying type and name of property */
   protected AbstractProperty(Class cl, String str){
      str = ( str == null ) ? ((cl==null)?"null":cl.toString()) : str;
      setJClass(cl);
      setName( str );
   }
   public boolean accumulate(Object dt){
      setPossible( validate( dt ), instance . intValue() );
      if ( getPossible( instance . intValue() ) ) {
         merge( );
      } else {
         setPossible( false, constraint. intValue() );
         setPossible( false, accumulate. intValue() );            
      }
      return getPossible( instance . intValue() ) ;
   }
   public void add2Report(String msg) {}
   public Object clone() {
      Object obj = null;
      try {
         obj = super.clone();
      } catch (CloneNotSupportedException e) {
         throw new RuntimeException("Attempt to clone object: " + toString() + " false\n");
      }
      return obj;
   }
   /** The next method is included to remind developer's that they are responsible for
   setting the eval and merge functions for subProp's for classes which they define.
   */
   protected abstract void enableSubProp(String name);
   /**
    * The property uses a particular class for its internal representation.  This
    * function returns that class.
    * @return java.lang.Class
    */
   public Class getJClass() {
      return jClass;
   }
   /**
    * Name of property
    * @return java.lang.String
    */
   public String getName() {
      return name;
   }
   /**
    * 
    * @return boolean
    * @param i int
    */
   public boolean getPossible(int i) {
      return possible[ i ];
   }
   public ValidationSummary                      getReport(){
      return report;
   }
   final public Object getSubProp( int i , Integer type ){
      return subProp[ type . intValue()][ i ];
   }
   private Object[][] getSubPropArray(Class cl)    {
      try {
         Field f = cl.getField("classNumberSubProperties");
         int i = f . getInt( this );
         if ( i > 0 ) {
            return new Object[3][ i ];
         }
      } catch (NoSuchFieldException e) {
         Class xclass = cl . getSuperclass();
         return getSubPropArray( xclass );
      } catch (IllegalAccessException f) {
         throw new RuntimeException( f.toString() );
      }
      return null;
   }
   protected Method getSubPropEval(int i){
      return subPropMethod[ Property . eval ][ i ];
   }
   protected Method getSubPropMerge(int i){
      return subPropMethod[ Property . merge ][ i ];
   }
   public String getSubPropName(int i){
      return subPropName[ i ];
   }
   public int getSubPropNameIndex(String s){
      int index = -1;
      int i = 0;
      if ( s != null )
         while ( i < subPropName . length ) {
            if ( s . equals( subPropName[ i ] ) ) {
               index = i;
               break;
            }
            i++;
         }
      return index;
   }
   /** For each subProp.  If there is a subPropMerge method, use it to accumulate the 
         results of a sequence of tests.
   */
   public  void merge(){
      int i;
      for ( i = 0 ; i < subProp . length ; i++ ) {
         Method merge = getSubPropMerge( i );
         if ( merge == null ) {
            continue;
         }
         Object result;
         try {
            result = merge . invoke( null , 
                                     new Object[] { 
                                        getSubProp( i , Property . instance ) ,
                                        getSubProp( i , Property . accumulate )
                                     } );
         } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessException:" + e);
         } catch (InvocationTargetException f) {
            throw new RuntimeException("InvocationTargetException:" + f);
         }

         setSubProp( result , i , Property . accumulate );
      }
   }
   final public void msg(String m){
      System . out . println( m );
   }
   final public void resetAccumulator() {
      int i = 0;
      int accumulatorId = Property.accumulate.intValue();
      for ( i = 0 ; i < subProp[ accumulatorId ] . length ; i++ ) {
         setSubProp( null , i , Property.accumulate );
      }
   }
   final public void resetConstraint() {
      int i = 0;
      int id = Property.constraint.intValue();
      for ( i = 0 ; i < subProp[ id ] . length ; i++ ) {
         setSubProp( null , i , Property.constraint );
      }
   }
   final public void resetInstance() {
      int i = 0;
      int id = Property.instance.intValue();
      for ( i = 0 ; i < subProp[ id ] . length ; i++ ) {
         setSubProp( null , i , Property.instance );
      }
   }
   public void setJClass(Class newJClass) {
      jClass = newJClass;
   }
   public void setName(String newName) {
      name = newName;
   }
   /**
    * 
    * @param newPossible boolean
    */
   public void setPossible(boolean newPossible, int i) {
      possible[ i ] = newPossible;
   }
   public void setRequired(Boolean newRequired) {
      required = newRequired;
   }
   final public void setSubProp(Object o , int i , Integer type){
      subProp[ type . intValue() ][ i ] = o;
   }
   final public void setSubProp(Object o , int i, String name, Integer type){
      setSubProp( o , i , type );
      setSubPropName( i , name );
   }
   /** Sets the method that will be used to determine whether an object satisfies a constraint. representing subProp-i
         on a sequence of objects validated by this property.  <br>
         mergeMethod should have signature: <br>
         <code>Object mergeMethod(Object currentEval, Object runningAccumulation)</code>.  <br>
         The object it returns will be the
         runningAccumulation argument on the next call to mergeMethod.
   */
   public void setSubPropEval(int i, Method m){
      if ( null != subPropMethod[ Property . eval ][ i ] &&
           m != subPropMethod[ Property . eval ][ i ] ) {
         System . err . println("Warning: Eval method for " + getName() + "." +
                                subPropName[ i ] + " changed from " +
                                subPropMethod[ Property . eval ][ i ] + " to " +
                                m);
      }
      subPropMethod[ Property . eval ][ i ] = m;
      if ( getSubProp( i , Property.constraint ) == null ) {
         setSubProp( Boolean.TRUE , i , Property . constraint );
      }
   }
   /** Sets the method that will be used to accumulate an object representing subProp-i
         on a sequence of objects validated by this property.  <br>
         mergeMethod should have signature: <br>
         <code>Object mergeMethod(Object currentEval, Object runningAccumulation)</code>.  <br>
         The object it returns will be the
         runningAccumulation argument on the next call to mergeMethod.

         Note, if mergeMethod is set, it is called at each validation whether or not
         constraint is present.  If constraint is not present, currentEval ==
         currentObject.

         Also note that for Helpers.and and Helpers.allFalse, the accumulator is initialized to 
         Boolean.TRUE.  
   */
   public void setSubPropMerge(int i, Method mergeMethod){
      if ( null != subPropMethod[ Property . merge ][ i ] &&
           mergeMethod != subPropMethod[ Property . merge ][ i ] ) {
         System . err . println("Warning: Eval method for " + getName() + "." +
                                subPropName[ i ] + " changed from " +
                                subPropMethod[ Property . merge ][ i ] + " to " +
                                mergeMethod);
      }
      if ( mergeMethod == Helpers . allFalse || mergeMethod == Helpers . andMethod ) {
         setSubProp( Boolean.TRUE , i , Property . accumulate );
      }
      subPropMethod[ Property . merge ][ i ] = mergeMethod;
      if ( getSubProp( i , Property.constraint )   == null ) {
         setSubProp( Boolean.TRUE , i , Property . constraint );
      }
   }
   protected void setSubPropName(int i , String s){
      subPropName[ i ] = s;
   }
   final public String toString(){
      return getName();
   }
   /** Returns 'virgin' Property which has same constraint as this, and methods as this
         but no experience, i.e.  any instance or accumulation data is set to null.  This
         requires 'cloning' any mutable Objects held in AbstractProperty (or derived
         classes).  Current fields include: <p>

         <em>non-mutable: </em>name, jClass, required, sValue, method
         <br>
         <em>mutable: </em>possible, subProp, subPropName, subPropMethod, re, enumValues, report
              
         
*/
   public Property twin(){
      AbstractProperty twin = (AbstractProperty)clone();
      int i,j;
      twin . possible = new boolean[]{ true , true , true};
      twin . subProp = (Object[][])subProp.clone();
      twin . subPropName = (String[])subPropName.clone();
      twin . subPropMethod = (Method[][])subPropMethod.clone();
      for ( j = 0 ; j < subProp . length ; j++ ) {
         twin . subPropName[ j ] = subPropName[ j ];
         for ( i = 0 ; i < 3 ; i+=2 ) {
            twin . subProp[ i ][ j ] = null;
            twin . subPropMethod[ i / 2 ][ j ] = subPropMethod[ i / 2 ][ j ];
         }
      }
      return twin;
   }
   /** validate returns false if (a) for some non-null subProp[ Property.constraint ],
         the result of subPropEval is null, or the validate method of the derived class
         sets possible[ instance ] = false.  If there is a subPropMerge method, use it to
         accumulate the results of a sequence of tests.  If a subProp has no eval method, this object is
         referenced as the result of 
   */
   public  boolean validate( Object obj ){
      int i;
      boolean val = true;
      for ( i = 0 ; i < subPropName . length ; i++ ) {
         Object constraint = getSubProp( i , Property . constraint );
         if ( constraint == null ) {
            continue;
         }
         Method eval = getSubPropEval( i );
         if ( eval == null ) {
            setSubProp( obj , i , Property . instance );
            continue;
         }
         Object result;
         try {
            result = eval . invoke( null , new Object[] { obj , constraint} );
         } catch (Exception e) {
            return false;
         }
         setSubProp( result , i , Property . instance );
         val &= result != null;
      }
      setPossible( val , Property . instance . intValue() );
      return val;
   }
}
